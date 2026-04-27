package com.jasolar.mis.module.bpm.api.dto;

import java.util.List;
import java.util.Map;

import com.jasolar.mis.framework.common.validation.group.Submit;
import com.jasolar.mis.module.bpm.enums.ModuleEnum;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@SuppressWarnings("serial")
@Schema(description = "RPC 服务 - 流程实例的创建 Request DTO")
@Data
public class BpmProcessInstanceCreateReqDTO extends BpmBizDTO {

    @Schema(description = "所属的微服务")
    @NotNull
    private ModuleEnum module = ModuleEnum.BPM; // 微服务名称

    // @Schema(description = "发起流程时记录来自于哪个微服务的子模块, 字典bpm_biz_type", requiredMode = Schema.RequiredMode.REQUIRED)
    // @NotBlank(message = "子模块(bizType)不能为空")
    // @Size(max = 30)
    // private String subModule;

    @Schema(description = "使用的流程图KEY")
    @NotBlank(groups = Submit.class)
    @Size(max = 64, groups = Submit.class)
    private String processDefinitionKey;

    /**
     * 手工选择的审批人,
     * key为任务KEY,
     * 值为页面上选择的任务的处理人, 当节点的审批人有多个并且配置为需要手工选择时,保存对应节点由申请人在页面上选择的审批人
     */
    @Schema(description = "各节点手工选择的审批人")
    private Map<String, List<Long>> manualAssigness;

    // @Schema(description = "流程标题,为空则使用流程定义的名称")
    // @Size(max = 100)
    // private String name;

    // @Schema(description = "业务的唯一标识", requiredMode = Schema.RequiredMode.REQUIRED)
    // @NotBlank(message = "业务单号不能为空")
    // @Size(max = 20)
    // private String businessKey;

    // 以下为统一定义的系统所需的流程变量

    // @Schema(description = "业务ID", requiredMode = Schema.RequiredMode.REQUIRED)
    // @NotNull(message = "业务ID不能为空")
    // private Long bizId;

    // @Schema(description = "申请人账号", requiredMode = Schema.RequiredMode.REQUIRED)
    // @NotBlank(message = "申请人账号不能为空")
    // private String userNo;
    //
    // // 以上为统一定义的系统所需的流程变量

    // /**
    // * 发起人自选审批人 Map
    // * key：taskKey 任务编码
    // * value：审批人的数组
    // * 例如：{ taskKey1 :[1, 2] }，则表示 taskKey1 这个任务，提前设定了，由 userId 为 1,2 的用户进行审批
    // */
    // @Schema(description = "发起人自选审批人 Map")
    // private Map<String, List<Long>> startUserSelectAssignees;

    // @Deprecated
    // public void setBusinessKey(String bizNo) {
    // this.setBizNo(bizNo);
    // }
    //
    // /**
    // * 等价于 {@link #getBizNo()}
    // *
    // * @return
    // */
    // @Deprecated
    // public String getBusinessKey() {
    // return this.getBizNo();
    // }
    //
    // @Deprecated
    // public void setSubModule(String bizType) {
    // this.setBizType(bizType);
    // }
    //
    // /**
    // * 等价于 {@link #getBizType()}
    // *
    // * @return
    // */
    // @Deprecated
    // public String getSubModule() {
    // return this.getBizType();
    // }

}
