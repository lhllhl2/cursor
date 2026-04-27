package com.jasolar.mis.module.system.domain.ehr;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * EHR组织管理层级视图
 * 功能：通过EHR_ORG_MANAGE_R表，对所有EHR_CD向上追溯对应的ERP_DEPART、CONTROL_LEVEL=1的EHR_CD、ORG_CD等信息
 * 
 * @author Auto Generated
 * @date 2025-01-XX
 */
@TableName(value = "V_EHR_ORG_MANAGE_HIERARCHY", autoResultMap = true)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EhrOrgManageHierarchyView {

    /**
     * EHR组织编码（EHR_CD）
     */
    @TableField(value = "EHR_ORG_CODE")
    private String ehrOrgCode;

    /**
     * ERP部门编码（向上追溯的ERP_DEPART）
     */
    @TableField(value = "ERP_DEPT_CODE")
    private String erpDeptCode;

    /**
     * 控制层级EHR编码（向上追溯CONTROL_LEVEL=1的EHR_CD）
     */
    @TableField(value = "PLAN_ORG_CODE")
    private String planOrgCode;

    /**
     * 管理组织编码（向上追溯的ORG_CD）
     */
    @TableField(value = "MORG_CODE")
    private String morgCode;

    /**
     * 管理组织名称（对应ORG_CD记录的ORG_NM）
     */
    @TableField(value = "MORG_NAME")
    private String morgName;

    /**
     * 父级管理组织编码（对应ORG_CD记录的PAR_CD）
     */
    @TableField(value = "PARENT_MORG_CODE")
    private String parentMorgCode;

    /**
     * 创建时间
     */
    @TableField(value = "CREATE_TIME")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField(value = "UPDATE_TIME")
    private LocalDateTime updateTime;
}

