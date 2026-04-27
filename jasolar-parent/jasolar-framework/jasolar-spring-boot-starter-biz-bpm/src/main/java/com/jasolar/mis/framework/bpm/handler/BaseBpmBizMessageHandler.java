package com.jasolar.mis.framework.bpm.handler;

import cn.hutool.core.text.StrPool;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.jasolar.mis.framework.bpm.util.BpmBizTaskStatusEnum;
import com.jasolar.mis.framework.mybatis.core.dataobject.BaseBpmBizDO;
import com.jasolar.mis.framework.mybatis.core.dataobject.BaseBpmTaskDO;
import com.jasolar.mis.framework.mybatis.core.enums.BpmStatusEnum;
import com.jasolar.mis.framework.mybatis.core.mapper.BaseMapperX;
import com.jasolar.mis.module.bpm.api.message.dto.BaseBpmMessageDTO;
import com.jasolar.mis.module.bpm.api.message.dto.BpmProcessMessageDTO;
import com.jasolar.mis.module.bpm.api.message.dto.BpmTaskMessageDTO;
import jakarta.annotation.Resource;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 适用于各业务对BPM流程的监听回调. 当前类中仅默认修改了流程状态{@link BpmStatusEnum}
 *
 * @param <Biz>        Biz业务实体类泛型的class
 * @param <BizMapper>  Biz的Mapper类
 * @param <Task>       Task任务实体类泛型的class
 * @param <TaskMapper> Task的Mapper类
 * @author galuo
 * @date 2025-04-02 16:21
 */
@Slf4j
@Transactional
public abstract class BaseBpmBizMessageHandler<Biz extends BaseBpmBizDO, BizMapper extends BaseMapperX<Biz>, Task extends BaseBpmTaskDO, TaskMapper extends BaseMapperX<Task>>
        extends BaseBpmTaskMessageHandler<Task, TaskMapper> implements BpmActionMessageHandler {

    protected BizMapper bizMapper;

    /**
     * Biz业务实体类泛型的class
     */
    @Getter
    protected Class<Biz> bizClass;

    /**
     * 初始化计算实体DO泛型的class
     */
    @SuppressWarnings("unchecked")
    public BaseBpmBizMessageHandler() {
        Type superClass = getClass().getGenericSuperclass();
        bizClass = (Class<Biz>) ((ParameterizedType) superClass).getActualTypeArguments()[0];
        taskClass = (Class<Task>) ((ParameterizedType) superClass).getActualTypeArguments()[2];
    }

    /**
     * 指定Task和Biz的具体类型
     *
     * @param taskClass
     * @param bizClass
     */
    public BaseBpmBizMessageHandler(Class<Biz> bizClass, Class<Task> taskClass) {
        super(taskClass);
        this.bizClass = bizClass;
    }

    /**
     * 设置bizMapper, {@link Resource}注解必须加在方法上, 不然会因为泛型造成注入失败
     *
     * @param bizMapper
     */
    @Resource
    public void setBizMapper(BizMapper bizMapper) {
        this.bizMapper = bizMapper;
    }

    /**
     * 根据单号获取业务对象
     *
     * @param bizNo 业务单号
     * @return 数据库实体对象
     */
    protected Biz getBiz(String bizNo) {
        // TableInfo table = TableInfoHelper.getTableInfo(getBizClass());
        // TableFieldInfo f = table.getFieldList().get(0);
        LambdaQueryWrapper<Biz> wrapper = Wrappers.lambdaQuery(this.getBizClass());
        // wrapper.eq(Biz::getNo, bizNo).eq(Biz::getBizType, bizType);
        wrapper.eq(Biz::getNo, bizNo);
        Biz biz = bizMapper.selectOne(wrapper);
        return biz;
    }

    /**
     * 流程任务处理后更新业务数据的当前审批人
     *
     * @param message 监听到的消息
     */
    protected void updateBizCurrentApprover(BpmTaskMessageDTO message) {
        String bizNo = message.getBusinessKey();
        Biz biz = this.getBiz(bizNo);
        if (biz == null) {
            log.warn("更新当前审批人失败, 业务数据不存在, 可能是在事务提交前收到了MQ回调: bizType={}, bizNo={}", message.getSubModule(), bizNo);
            return;
        }
     /*   if (!BpmStatusEnum.APPROVING.name().equals(biz.getStatus())) {
            log.info("业务数据不是处理中状态(bizType={}, bizNo={}),无需更新当前审批人", message.getSubModule(), bizNo);
            return;
        }*/
        if (!isApproving(biz)) {
            log.info("业务数据不是处理中状态(bizType={}, bizNo={}),无需更新当前审批人", message.getSubModule(), bizNo);
            return;
        }

        LambdaQueryWrapper<Task> wrapper = Wrappers.lambdaQuery(this.getTaskClass());
        wrapper.eq(Task::getProcInstId, message.getProcessInstanceId());
        // wrapper.eq(Task::getBizNo, bizNo);
        wrapper.eq(Task::getTaskStatus, BpmBizTaskStatusEnum.PENDING.name());
        List<Task> list = this.taskMapper.selectList(wrapper);
        biz.setCurrentApproverName(null);
        list.stream().map(Task::getUserName).distinct().reduce((a, b) -> a + StrPool.COMMA + b).ifPresent(biz::setCurrentApproverName);
        this.bizMapper.updateById(biz);
    }

    /**
     * 判断是否审批中 不同流程有可能审批中状态不同, 需要自己覆盖继承设置
     *
     * @param biz
     * @return
     */
    public boolean isApproving(Biz biz) {
        return BpmStatusEnum.APPROVING.name().equals(biz.getStatus());
    }


    /**
     * 流程消息监听后更新业务申请单状态, 主要用于审批完成后的回调
     *
     * @param message 监听的消息
     * @param status
     */
    protected void updateBiz(BaseBpmMessageDTO message, BpmStatusEnum status) {
        String bizNo = message.getBusinessKey();

        Biz biz = this.getBiz(bizNo);
        if (biz == null) {
            log.warn("更新业务状态失败, 业务数据不存在, 可能是在事务提交前收到了MQ回调: bizType={}, bizNo={}", message.getSubModule(), bizNo);
            return;
        }
        biz.setStatus(status.name());

        switch (status) {
        case APPROVED:
        case REJECTED:
            // 审批完成
            biz.setCompleteTime(LocalDateTime.now());
            biz.setCurrentApproverName(null);
            break;
        case RETURNED:
            biz.setCurrentApproverName(null);
            // 审批退回到申请人, 更新所有PENDING任务为挂起状态. 退回后重新提交在任务回调中(任务从RETURNED变PENDING状态则表示审批退回后重新提交)处理
            LambdaUpdateWrapper<Task> taskWrapper = Wrappers.lambdaUpdate(getTaskClass()).eq(Task::getProcInstId, biz.getProcInstId())
                    .eq(Task::getTaskStatus, BpmBizTaskStatusEnum.PENDING.name())
                    .set(Task::getTaskStatus, BpmBizTaskStatusEnum.SUSPENDED.name());
            this.taskMapper.update(taskWrapper);
            break;
        case DRAFT:
            // 流程被取消,可能是申请人进行流程撤回
            biz.setCurrentApproverName(null);
            break;
        default:
            break;
        }
        this.bizMapper.updateById(biz);
    }

    @Override
    public void doTaskAssign(BpmTaskMessageDTO message) {
        super.doTaskAssign(message);
        this.updateBizCurrentApprover(message);
    }

    @Override
    public void doTaskApprove(BpmTaskMessageDTO message) {
        super.doTaskApprove(message);
        this.updateBizCurrentApprover(message);
    }

    // @Override
    // public void doTaskReject(BpmTaskMessageDTO message) {
    // super.doTaskReject(message);
    // // this.updateBizCurrentApprover(message);
    // // 流程事件会触发doProcessReject, 因此不用更新流程状态
    // }
    //
    // @Override
    // public void doTaskReturn(BpmTaskMessageDTO message) {
    // super.doTaskReturn(message);
    // // 流程事件会触发doProcessReturn, 因此不用更新流程状态
    // }
    //
    // @Override
    // public void doTaskCancel(BpmTaskMessageDTO message) {
    // super.doTaskCancel(message);
    // // 流程事件会触发doProcessCancel, 因此不用更新流程状态
    // }

    /**
     * 注意: 此方法一般需要业务方重写业务
     */
    @Override
    public void doProcessApproved(BpmProcessMessageDTO message) {
        this.updateBiz(message, BpmStatusEnum.APPROVED);
    }

    @Override
    public void doProcessReject(BpmProcessMessageDTO message) {
        this.updateBiz(message, BpmStatusEnum.REJECTED);
    }

    @Override
    public void doProcessReturn(BpmProcessMessageDTO message) {
        this.updateBiz(message, BpmStatusEnum.RETURNED);
    }

    @Override
    public void doProcessCancel(BpmProcessMessageDTO message) {
        this.updateBiz(message, BpmStatusEnum.DRAFT);

        // 所有待办均取消
        LambdaUpdateWrapper<Task> cancelTaskWrapper = Wrappers.lambdaUpdate(getTaskClass())
                .eq(Task::getProcInstId, message.getProcessInstanceId()).in(Task::getTaskStatus, BpmBizTaskStatusEnum.PENDING.name(),
                        BpmBizTaskStatusEnum.RETURNED, BpmBizTaskStatusEnum.SUSPENDED.name())
                .set(Task::getTaskStatus, BpmBizTaskStatusEnum.CANCELLED.name());
        this.taskMapper.update(cancelTaskWrapper);
    }

}
