package com.jasolar.mis.framework.bpm.util;

import java.util.concurrent.Callable;
import java.util.concurrent.locks.Lock;

import org.apache.commons.lang3.StringUtils;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;

import com.jasolar.mis.framework.bpm.ErrorCodeConstants;
import com.jasolar.mis.framework.bpm.transaction.BpmBizUnlockEvent;
import com.jasolar.mis.framework.bpm.transaction.BpmBizUnlockEventListener;
import com.jasolar.mis.framework.common.exception.ServiceException;
import com.jasolar.mis.framework.common.util.spring.SpelUtils;
import com.jasolar.mis.framework.data.core.DictData;
import com.jasolar.mis.framework.data.util.DictUtils;
import com.jasolar.mis.framework.redis.lock.RedisLockExecutor;
import com.jasolar.mis.module.bpm.api.dto.BpmBizDTO;
import com.jasolar.mis.module.bpm.enums.DictTypeConstants;

import cn.hutool.core.text.StrPool;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

/**
 * 
 * @author galuo
 * @date 2025-04-02 19:29
 *
 */
@Slf4j
public class BpmUtils implements BpmBizUnlockEventListener, ApplicationEventPublisherAware {

    /** 提交流程时,如果有多个符合条件的流程图,则需要在前端选择一个并且通过此header提交到后台 */
    public static final String HEADER_PROCESS_CATEGORY = "x-process-category";

    /** 提交流程时,如果流程节点审批人有多个,并且配置为手工指定审批人, 则需要在页面选择节点的审批人,然后将节点的审批人通过header提交到后台 */
    public static final String HEADER_MANUAL_ASSIGNEES = "x-manual-assignees";

    /** 在参数配置表中, 适用于所有流程分类的固定分类值 */
    public static final String PROCESS_CATEGORY_ALL = "ALL";

    private static ApplicationEventPublisher PUBLISHER;

    @Override
    public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        PUBLISHER = applicationEventPublisher;
    }

    /** 分布式锁的前缀, 主要用于对BPM业务对象添加分布式锁 */
    public static final String LOCK_PREFIX = "BPM:BIZ:";

    /** 分布式锁执行 */
    private static RedisLockExecutor LOCK_EXECUTOR;

    /**
     * 指定分布式锁执行对象
     * 
     * @param lockExecutor
     */
    public BpmUtils(RedisLockExecutor lockExecutor) {
        super();
        LOCK_EXECUTOR = lockExecutor;
    }

    /**
     * 获取流程分类
     * 
     * @param bizType 业务类型
     * @return
     */
    public static String getProcessCategory(String bizType) {
        DictData dict = DictUtils.getData(DictTypeConstants.BIZ_YTPE, bizType);
        if (dict == null) {
            log.warn("未发现字典数据bpm_biz_type: {}", bizType);
            return StringUtils.EMPTY;
        }
        if (StringUtils.isBlank(dict.getAttr1())) {
            log.warn("字典数据bpm_biz_type: {} 未配置流程分类(attr1字段)", bizType);
            return StringUtils.EMPTY;
        }
        return dict.getAttr1();
    }

    /**
     * 获取流程定义KEY, 在字典中使用attr1指定业务的流程图
     * 
     * @param bizType 业务类型
     * @return
     */
    public static String getProcDefKey(String bizType) {
        DictData dict = DictUtils.getData(DictTypeConstants.BIZ_YTPE, bizType);
        if (dict == null) {
            log.warn("未发现字典数据bpm_biz_type: {}", bizType);
            return StringUtils.EMPTY;
        }
        if (StringUtils.isBlank(dict.getAttr1())) {
            log.warn("字典数据bpm_biz_type: {} 未配置流程定义KEY(attr1字段)", bizType);
            return StringUtils.EMPTY;
        }
        return dict.getAttr1();
    }

    /**
     * 获取指定业务的详情URL配置, 在字典中使用attr2指定业务的url
     * 
     * @param root 入参对象, 一般为: com.fiifoxconn.mis.module.bpm.controller.admin.task.vo.task.TaskDTO或者BpmProcessInstanceCreateReqDTO
     * 
     * @return 解析后的URL
     */
    public static String getUrl(BpmBizDTO root) {
        String bizType = root.getBizType();
        DictData dict = DictUtils.getData(DictTypeConstants.BIZ_YTPE, bizType);
        if (dict == null) {
            log.warn("未发现字典数据bpm_biz_type: {}", bizType);
            return StringUtils.EMPTY;
        }
        String attr2 = dict.getAttr2();
        if (StringUtils.isBlank(attr2)) {
            log.warn("字典数据bpm_biz_type: {} 未配置详情URL(attr2字段)", bizType);
            return StringUtils.EMPTY;
        }

        try {
            return (String) SpelUtils.getValue(attr2, root, root.getVariables());
        } catch (Exception ignore) {
            log.warn("URL解析出错: " + attr2 + ", bpm_biz_type: " + bizType, ignore);
            return StringUtils.EMPTY;
        }
    }

    /**
     * 得到分布式锁的key
     * 
     * @param bizType 业务类型
     * @param bizNo 业务单号
     * @return 分布式锁的KEY
     */
    public static String getLockKey(String bizType, String bizNo) {
        String lockKey = LOCK_PREFIX + bizType + StrPool.COLON + bizNo;
        return lockKey;
    }

    /**
     * 在分布式锁中执行方法
     * 
     * @param <V>
     * @param bizType 业务类型
     * @param bizNo 业务单号
     * @param callable 执行的方法
     * @return
     */
    @SneakyThrows
    public static <V> V tryExecute(String bizType, String bizNo, Callable<V> callable) {
        Lock lock = LOCK_EXECUTOR.tryLock(getLockKey(bizType, bizNo));
        if (lock == null) {
            // 可能是有重复提交
            // throw new ServiceException();
            log.warn("获取分布式锁失败, 可能时有重复提交: {}, {}", bizType, bizNo);
            throw new ServiceException(ErrorCodeConstants.BPM_PROCESS_REPEATED_SUBMIT, bizNo);
        }

        try {
            return callable.call();
        } finally {
            publishUnlockEvent(lock, bizType, bizNo);
        }
    }

    /**
     * 在分布式锁中执行方法, 如果被其他事务锁住,则会等待锁释放后继续执行
     * 
     * @param <V>
     * @param bizType 业务类型
     * @param bizNo 业务单号
     * @param callable 执行的方法
     * @return
     */
    @SneakyThrows
    public static <V> V execute(String bizType, String bizNo, Callable<V> callable) {
        Lock lock = lock(bizType, bizNo);
        try {
            return callable.call();
        } finally {
            publishUnlockEvent(lock, bizType, bizNo);
        }
    }

    /**
     * 获取业务对象的锁
     * 
     * @param bizType 业务类型
     * @param bizNo 业务单号
     * @return 锁对象
     */
    public static Lock lock(String bizType, String bizNo) {
        String lockKey = getLockKey(bizType, bizNo);
        log.info("创建分布式锁: {}, {}, {}", Thread.currentThread().getId(), bizType, bizNo);
        return LOCK_EXECUTOR.lock(lockKey);
    }

    /**
     * 
     * 发布解锁事件
     * 
     * @param lock 分布式锁
     * @param bizType 业务类型
     * @param bizNo 业务单号
     */
    public static void publishUnlockEvent(Lock lock, String bizType, String bizNo) {
        PUBLISHER.publishEvent(new BpmBizUnlockEvent(lock, bizType, bizNo));
    }

    /**
     * 发布解锁事件
     * 
     * @param event 事件
     */
    public static void publishUnlockEvent(BpmBizUnlockEvent event) {
        PUBLISHER.publishEvent(event);
    }

    @Override
    public void unlock(BpmBizUnlockEvent event) {
        log.info("释放分布式锁: {}, {}, {}", event.getTid(), event.getBizType(), event.getBizNo());
        LOCK_EXECUTOR.unlock(event.getSource(), event.getTid());
    }
}
