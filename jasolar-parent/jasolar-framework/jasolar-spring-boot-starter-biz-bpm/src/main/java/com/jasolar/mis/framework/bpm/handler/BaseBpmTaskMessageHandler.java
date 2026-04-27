package com.jasolar.mis.framework.bpm.handler;

import java.io.IOException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.jasolar.mis.framework.bpm.mq.context.BpmMessageContext;
import com.jasolar.mis.framework.bpm.util.BpmBizTaskStatusEnum;
import com.jasolar.mis.framework.bpm.util.BpmUtils;
import com.jasolar.mis.framework.mybatis.core.dataobject.BaseBpmTaskDO;
import com.jasolar.mis.framework.mybatis.core.mapper.BaseMapperX;
import com.jasolar.mis.module.bpm.api.message.dto.BaseBpmMessageDTO;
import com.jasolar.mis.module.bpm.api.message.dto.BpmTaskMessageDTO;

import jakarta.annotation.Resource;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * 默认回写任务的BPM消息处理
 * 
 * @author galuo
 * @date 2025-04-02 16:50
 *
 * @param <Task>
 * @param <TaskMapper>
 */
@Slf4j
@Transactional(readOnly = true, rollbackFor = Throwable.class)
public abstract class BaseBpmTaskMessageHandler<Task extends BaseBpmTaskDO, TaskMapper extends BaseMapperX<Task>>
        implements BpmActionMessageHandler, InitializingBean {

    /** xxx_bpm_task表的Mapper */
    protected TaskMapper taskMapper;

    /** Task任务实体类泛型的class */
    @Getter
    protected Class<Task> taskClass;

    /** 初始化计算实体DO泛型的class */
    @SuppressWarnings("unchecked")
    public BaseBpmTaskMessageHandler() {
        Type superClass = getClass().getGenericSuperclass();
        taskClass = (Class<Task>) ((ParameterizedType) superClass).getActualTypeArguments()[0];
    }

    /**
     * 指定Task的具体类型
     * 
     * @param taskClass
     */
    public BaseBpmTaskMessageHandler(Class<Task> taskClass) {
        this.taskClass = taskClass;
    }

    /**
     * 设置taskMapper, {@link Resource}注解必须加在方法上, 不然会因为泛型造成注入失败
     * 
     * @param taskMapper
     */
    @Resource
    public void setTaskMapper(TaskMapper taskMapper) {
        this.taskMapper = taskMapper;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        log.info("BPM消息监听初始完成: {}", this.getClass());
    }

    @Override
    public void handleMessage(BaseBpmMessageDTO message, BpmMessageContext context) throws IOException {
        if (log.isInfoEnabled()) {
            log.info("{}, {}, {}, {} 接收到BPM消息: {}", message.getAction(), message.getModule(), message.getSubModule(),
                    message.getBusinessKey(), message);
        }

        BpmUtils.execute(message.getSubModule(), message.getBusinessKey(), () -> {
            try {
                // 默认调用旧方法，实现向后兼容
                handleMessage(message);

                // 成功处理后自动确认
                context.ack();
            } catch (Exception ex) {
                log.error("{}, {}, {}, {} BPM消息处理异常: {}", message.getAction(), message.getModule(), message.getSubModule(),
                        message.getBusinessKey(), ex.getMessage());

                // 放入死信队列记录日志
                context.reject(false);

                throw ex;
            }

            return true;
        });
    }

    /**
     * 根据procInstId和taskId获取任务对象
     * 
     * @param procInstId 流程实例ID
     * @param taskId 任务ID
     * @return 数据库实体对象
     */
    protected Task getTask(String procInstId, String taskId) {
        LambdaQueryWrapper<Task> wrapper = Wrappers.lambdaQuery(this.getTaskClass());
        wrapper.eq(Task::getProcInstId, procInstId).eq(Task::getTaskId, taskId);
        Task task = taskMapper.selectOne(wrapper);
        return task;
    }

    /**
     * 将消息转换为Task对象
     * 
     * @param message 消息
     * @param status 任务状态
     * @return 任务第项
     */
    protected Task convert(BpmTaskMessageDTO message) {
        Task task = getTask(message.getProcessInstanceId(), message.getTaskId());
        if (task == null) {
            // 创建一个任务
            task = BeanUtils.instantiateClass(taskClass);
        }
        task.setProcInstId(message.getProcessInstanceId());
        task.setProcDefKey(message.getProcessDefinitionKey());
        task.setProcDefName(message.getProcessInstanceName());
        task.setTaskDefKey(message.getTaskDefinitionKey());
        task.setTaskDefName(message.getTaskName());
        task.setTaskId(message.getTaskId());
        task.setUserNo(message.getCurrentTaskAssigneeUserNo());
        task.setUserName(message.getCurrentTaskAssigneeUserNick());
        task.setDelegateeNo(message.getDelegateeNo());
        task.setDelegateeName(message.getDelegateeName());

        task.setBizType(message.getSubModule());
        task.setBizNo(message.getBusinessKey());
        task.setRemark(message.getComment());
        return task;
    }

    /**
     * 保存任务数据
     * 
     * @param message 接收的BPM消息
     * @param status 任务状态
     * @return 保存后的任务对象
     */
    protected Task saveTask(BpmTaskMessageDTO message, BpmBizTaskStatusEnum status) {
        Task task = convert(message);
        if (BpmBizTaskStatusEnum.PENDING != status && task.getId() == null) {
            log.info("任务不存在. 可能是尚未收到taskAssign的回调. message: {}", message);
        }
        if (task.getId() != null) {
            BpmBizTaskStatusEnum prevStatus = BpmBizTaskStatusEnum.valueOf(task.getTaskStatus());
            switch (prevStatus) {
            case PENDING:
                break;
            case RETURNED:
                if (BpmBizTaskStatusEnum.PENDING == status) {
                    // 退回后重新提交,更新所有挂起状态的任务为PENDING
                    LambdaUpdateWrapper<Task> taskWrapper = Wrappers.lambdaUpdate(getTaskClass())
                            .eq(Task::getProcInstId, task.getProcInstId()).eq(Task::getTaskStatus, BpmBizTaskStatusEnum.SUSPENDED.name())
                            .set(Task::getTaskStatus, BpmBizTaskStatusEnum.PENDING.name());
                    this.taskMapper.update(taskWrapper);
                }
                break;
            case APPROVED, REJECTED, CANCELLED:
                log.warn("任务已经审批结束,可能是先收到了taskApprove的消息,不需要再处理本次回调. message:{}", message);
                return task;
            default:
                break;

            }
        }

        if (StringUtils.isBlank(task.getUserNo())) {
            // 审批人为null, 可能是自动审批通过
            task.setUserNo(StringUtils.EMPTY);
            task.setUserName(StringUtils.EMPTY);
        }

        task.setTaskStatus(status.name());
        if (task.getId() == null) {
            taskMapper.insert(task);
        } else {
            taskMapper.updateById(task);
        }

        return task;
    }

    @Override
    public void doTaskAssign(BpmTaskMessageDTO message) {
        saveTask(message, BpmBizTaskStatusEnum.PENDING);
    }

    @Override
    public void doTaskApprove(BpmTaskMessageDTO message) {
        saveTask(message, BpmBizTaskStatusEnum.APPROVED);
    }

    @Override
    public void doTaskReject(BpmTaskMessageDTO message) {
        saveTask(message, BpmBizTaskStatusEnum.REJECTED);
    }

    @Override
    public void doTaskReturn(BpmTaskMessageDTO message) {
        saveTask(message, BpmBizTaskStatusEnum.RETURNED);
    }

    @Override
    public void doTaskCancel(BpmTaskMessageDTO message) {
        saveTask(message, BpmBizTaskStatusEnum.CANCELLED);
    }

}
