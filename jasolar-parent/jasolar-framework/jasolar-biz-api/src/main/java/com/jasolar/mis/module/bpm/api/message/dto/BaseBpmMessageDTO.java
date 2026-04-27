package com.jasolar.mis.module.bpm.api.message.dto;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.jasolar.mis.module.bpm.enums.BpmActionEnum;
import com.jasolar.mis.module.bpm.enums.ModuleEnum;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * BPM消息基础DTO
 * 包含所有消息通用的属性
 */
@SuppressWarnings("serial")
@Data
@SuperBuilder
@NoArgsConstructor
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "messageType")
@JsonSubTypes({ @JsonSubTypes.Type(value = BpmProcessMessageDTO.class, name = "process"),
        @JsonSubTypes.Type(value = BpmTaskMessageDTO.class, name = "task") })
public class BaseBpmMessageDTO implements Serializable {

    /**
     * 目标服务模块
     */
    @NotNull
    private ModuleEnum module;

    /**
     * 操作类型
     */
    @NotNull
    private BpmActionEnum action;

    /**
     * 业务key
     */
    @NotNull
    private String businessKey;

    /**
     * 流程实例ID
     */
    private String processInstanceId;

    /**
     * 流程定义Key
     */
    private String processDefinitionKey;

    /**
     * 流程实例名称
     */
    private String processInstanceName;

    /**
     * 流程发起人ID
     */
    private String startUserId;

    /**
     * 流程发起人工号
     */
    private String startUserNo;

    /**
     * 流程发起人昵称
     */
    private String startUserNick;

    /**
     * 子模块
     */
    private String subModule;
}