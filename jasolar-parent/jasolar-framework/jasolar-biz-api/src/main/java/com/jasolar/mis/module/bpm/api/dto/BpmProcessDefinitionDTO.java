package com.jasolar.mis.module.bpm.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Schema(description = "RPC - 流程定义 DTO")
@Data
public class BpmProcessDefinitionDTO {

    @Schema(description = "ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    @NotBlank
    private String id;

    @Schema(description = "流程名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "Fiifoxconn")
    @NotBlank
    private String name;

    @Schema(description = "流程定义ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "yudao")
    @NotBlank
    private String processDefinitionId;

    @Schema(description = "流程定义编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "process-1641042089407")
    @NotBlank
    private String processDefinitionKey;

    // @Schema(description = "流程图标", requiredMode = Schema.RequiredMode.REQUIRED, example = "https://www.iocoder.cn/yudao.jpg")
    // private String icon;
    @Schema(description = "流程分类", example = "1")
    @NotBlank
    private String processCategory;

    @Schema(description = "流程分类名字", example = "请假")
    @NotBlank
    private String processCategoryName;

    @Schema(description = "流程描述", example = "我是描述")
    private String description;

    // @Schema(description = "适用条件分组ID", example = "1")
    // private Long conditionGroupId;

    // @Schema(description = "流程模型的类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "10")
    // private Integer modelType; // 参见 BpmModelTypeEnum 枚举类

    // @Schema(description = "部署时间")
    // private LocalDateTime deploymentTime; // 需要从对应的 Deployment 读取，非必须返回
    //
    // @Schema(description = "BPMN XML")
    // private String bpmnXml; // 需要从对应的 BpmnModel 读取，非必须返回

    // @Schema(description = "SIMPLE 设计器模型数据 json 格式")
    // private String simpleModel; // 非必须返回

}
