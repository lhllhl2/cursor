package com.jasolar.mis.module.bpm.api.dto;

import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "管理后台 - 审批历史 Response DTO")
@Data
public class BizOperationDTO {

    @Schema(description = "主键ID")
    private Long id;

    @Schema(description = " 字典:bpm_biz_type。业务类型;流程业务类型, 和IBizType的实现类对象, 用于流程的相关配置,如获取ID,获取流程定义key",
            requiredMode = Schema.RequiredMode.REQUIRED, example = "2")
    private String bizType;

    @Schema(description = "业务ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "31526")
    private Long bizId;

    @Schema(description = "业务单号;传给流程的businessKey", requiredMode = Schema.RequiredMode.REQUIRED)
    // @ExcelProperty("业务单号;传给流程的businessKey")
    private String bizNo;

    @Schema(description = "流程标识;画流程图时定义的流程标识", requiredMode = Schema.RequiredMode.REQUIRED)
    // @ExcelProperty("流程标识;画流程图时定义的流程标识")
    private String procDefKey;

    @Schema(description = "流程名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "王五")
    // @ExcelProperty("流程名称")
    private String procDefName;

    @Schema(description = "流程实例ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "15298")
    // @ExcelProperty("流程实例ID")
    private String procInstId;

    @Schema(description = "流程实例的标题", requiredMode = Schema.RequiredMode.REQUIRED, example = "王五")
    // @ExcelProperty("流程实例的标题")
    private String procInstName;

    @Schema(description = "任务节点标识;画流程图时定义的节点ID")
    // @ExcelProperty("任务节点标识;画流程图时定义的节点ID")
    private String taskDefKey;

    @Schema(description = "任务名称", example = "王五")
    // @ExcelProperty("任务名称")
    private String taskDefName;

    @Schema(description = "任务ID;非BPM任务则task相关字段为空", example = "24986")
    // @ExcelProperty("任务ID;非BPM任务则task相关字段为空")
    private String taskId;

    @Schema(description = "任务开始时间;任务开始时间,非BPM任务则此字段为空")
    // @ExcelProperty("任务开始时间;任务开始时间,非BPM任务则此字段为空")
    private LocalDateTime taskStartTime;

    @Schema(description = "处理人ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "15960")
    // @ExcelProperty("处理人ID")
    private Long userId;

    @Schema(description = "处理人工号;人员维度的权限控制。无权限控制的表删除此字段", requiredMode = Schema.RequiredMode.REQUIRED)
    // @ExcelProperty("处理人工号;人员维度的权限控制。无权限控制的表删除此字段")
    private String userNo;

    @Schema(description = "处理人名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "王五")
    // @ExcelProperty("处理人名称")
    private String userName;

    @Schema(description = "任务Owner ID;审批人委托给其他人审批, 则owner_id为委托人, user_id为处理任务的被委托人", example = "10870")
    // @ExcelProperty("任务Owner ID;审批人委托给其他人审批, 则owner_id为委托人, user_id为处理任务的被委托人")
    private Long ownerId;

    @Schema(description = "任务Owner账号")
    // @ExcelProperty("任务Owner账号")
    private String ownerNo;

    @Schema(description = "任务Owner名称", example = "王五")
    // @ExcelProperty("任务Owner名称")
    private String ownerName;

    @Schema(description = "完成时间", requiredMode = Schema.RequiredMode.REQUIRED)
    // @ExcelProperty("完成时间")
    private LocalDateTime completeTime;

    @Schema(description = " 字典:bpm_biz_operation_action。签核动作", requiredMode = Schema.RequiredMode.REQUIRED)
    // @ExcelProperty(" 字典:bpm_biz_operation_action。签核动作")
    private String action;

    @Schema(description = "处理意见", example = "你猜")
    // @ExcelProperty("处理意见")
    private String remark;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    // @ExcelProperty("创建时间")
    private LocalDateTime createTime;

}