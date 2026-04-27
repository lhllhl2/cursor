package com.jasolar.mis.module.system.domain.budget;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * HSP 规划单元与成员映射视图实体
 * 对应视图：DATAINTEGRATION.V_HSP_PLANNING_UNIT_WITH_MEMBER
 */
@TableName(value = "DATAINTEGRATION.V_HSP_PLANNING_UNIT_WITH_MEMBER", autoResultMap = true)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HspPlanningUnitWithMemberView {

    @TableField("PLAN_UNIT_ID")
    private String planUnitId;

    @TableField("SCENARIO_ID")
    private String scenarioId;

    @TableField("VERSION_ID")
    private String versionId;

    @TableField("ENTITY_ID")
    private String entityId;

    @TableField("PROCESS_STATE")
    private Integer processState;

    @TableField("MORG_CODE")
    private String morgCode;

    @TableField("PATH_PRIMARY_MEMBER_CD")
    private String pathPrimaryMemberCd;

    @TableField("MORG_NAME")
    private String morgName;

    @TableField("PARENT_MORG_CODE")
    private String parentMorgCode;

    @TableField("SCENARIO_NAME")
    private String scenarioName;

    @TableField("VERSION_NAME")
    private String versionName;

    @TableField("ORG_TYPE")
    private String orgType;

    @TableField("IS_APPROVAL_LAST_LVL")
    private Integer isApprovalLastLvl;

    @TableField("SCRIPT_TYPE")
    private String scriptType;
}
