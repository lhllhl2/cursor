package com.jasolar.mis.framework.biztask;

import org.apache.commons.lang3.StringUtils;

import com.jasolar.mis.framework.biztask.mq.BizTaskPublisher;
import com.jasolar.mis.framework.common.enums.UserTypeEnum;
import com.jasolar.mis.framework.common.security.LoginServletUtils;
import com.jasolar.mis.framework.common.security.LoginUser;
import com.jasolar.mis.framework.data.util.UserUtils;

import jakarta.annotation.Nullable;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 业务中使用的Service
 * 
 * @author galuo
 * @date 2025-04-14 18:01
 *
 */
@Slf4j
@RequiredArgsConstructor
public class BizTaskPublisherService {

    private final BizTaskPublisher bizTaskPublisher;

    /**
     * 创建待办任务, 待办人名称自动获取. 模块与spring.application.name一致, 发送人为当前登录人.
     * 
     * @param userType 人员类型
     * @param userNo 人员工号
     * @param taskType 任务类型
     * @param bizType 业务类型
     * @param bizId 业务ID
     * @param bizNo 业务单号
     * @param title 任务标题
     * @param content 任务内容/描述
     */
    public void assignTask(UserTypeEnum userType, String userNo, String taskType, String bizType, Long bizId, String bizNo, String title,
            String content) {
        this.assignTask(userType, userNo, null, taskType, bizType, bizId, bizNo, title, content);
    }

    /**
     * 创建待办任务, 模块与spring.application.name一致, 发送人为当前登录人
     * 
     * @param userType 人员类型
     * @param userNo 人员工号
     * @param userName 人员名称, 可以为空
     * @param taskType 任务类型
     * @param bizType 业务类型
     * @param bizId 业务ID
     * @param bizNo 业务单号
     * @param title 任务标题
     * @param content 任务内容/描述
     */
    public void assignTask(UserTypeEnum userType, String userNo, @Nullable String userName, String taskType, String bizType, Long bizId,
            String bizNo, String title, String content) {
        BizTaskKey key = BizTaskKey.of(taskType, bizType, bizId, bizNo, userType, userNo);
        BizTask task = BizTask.of(key, title, content);
        if (StringUtils.isBlank(userName)) {
            userName = switch (userType) {
            case SUPPLIER -> userNo; // 供应商类型直接使用userNo作为userName
            case ADMIN -> UserUtils.getName(userNo);
            default -> userNo; // 对于其他类型，直接使用userNo作为userName
            };
        }
        task.setUserName(userName);

        this.assignTask(task);
    }

    /**
     * 创建待办任务
     * 
     * @param task 任务数据
     */
    public void assignTask(BizTask task) {
        if (task.getSenderType() == null || StringUtils.isBlank(task.getSenderNo())) {
            // 发送人信息, 默认获取当前登录人
            LoginUser sender = LoginServletUtils.getLoginUser();
            if (sender != null) {
                task.setSenderType(sender.userType());
                task.setSenderNo(sender.getNo());
                task.setSenderName(sender.getName());
            }
        }

        bizTaskPublisher.create(task.toMessage());
    }

    /**
     * 完成指定人员的待办, 模块与spring.application.name一致
     * 
     * @param userType 人员类型
     * @param userNo 人员工号
     * @param taskType 任务类型
     * @param bizType 业务类型
     * @param bizId 业务ID
     * @param bizNo 业务单号
     */
    public void completeTask(UserTypeEnum userType, String userNo, String taskType, String bizType, Long bizId, String bizNo) {
        BizTaskKey key = BizTaskKey.of(taskType, bizType, bizId, bizNo, userType, userNo);
        completeTask(key);
    }

    /**
     * 完成当前登录人员的待办, 模块与spring.application.name一致
     * 
     * @param taskType 任务类型
     * @param bizType 业务类型
     * @param bizId 业务ID
     * @param bizNo 业务单号
     */
    public void completeTask(String taskType, String bizType, Long bizId, String bizNo) {
        BizTaskKey key = BizTaskKey.of(taskType, bizType, bizId, bizNo);
        completeTask(key);
    }

    /**
     * 完成待办任务
     * 
     * @param taskKey 任务主键
     */
    public void completeTask(BizTaskKey taskKey) {
        bizTaskPublisher.complete(taskKey.toMessage());
    }

    /**
     * 完成指定人员的待办, 模块与spring.application.name一致
     * 
     * @param userType 人员类型
     * @param userNo 人员工号
     * @param taskType 任务类型
     * @param bizType 业务类型
     * @param bizId 业务ID
     * @param bizNo 业务单号
     */
    public void deleteTask(UserTypeEnum userType, String userNo, String taskType, String bizType, Long bizId, String bizNo) {
        BizTaskKey key = BizTaskKey.of(taskType, bizType, bizId, bizNo, userType, userNo);
        deleteTask(key);
    }

    /**
     * 完成当前登录人员的待办, 模块与spring.application.name一致
     * 
     * @param taskType 任务类型
     * @param bizType 业务类型
     * @param bizId 业务ID
     * @param bizNo 业务单号
     */
    public void deleteTask(String taskType, String bizType, Long bizId, String bizNo) {
        BizTaskKey key = BizTaskKey.of(taskType, bizType, bizId, bizNo);
        deleteTask(key);
    }

    /**
     * 删除待办任务
     * 
     * @param taskKey 任务主键
     */
    public void deleteTask(BizTaskKey taskKey) {
        bizTaskPublisher.delete(taskKey.toMessage());
    }
}
