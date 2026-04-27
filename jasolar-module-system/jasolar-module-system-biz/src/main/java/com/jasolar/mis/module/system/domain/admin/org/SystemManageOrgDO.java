package com.jasolar.mis.module.system.domain.admin.org;

import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.jasolar.mis.framework.mybatis.core.dataobject.BaseDO;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * 组织 DO
 *
 * @author jasolar
 */
@TableName(value = "SYSTEM_MANAGE_ORG", autoResultMap = true)
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class SystemManageOrgDO extends BaseDO {

    /**
     * 组织ID
     */
    @TableId
    private Long id;

    /**
     * 组织名称
     */
    private String name;
    /**
     * 组织编码
     */
    private String code;
    /**
     * 父级组织编码，顶级组织为空或0
     */
    private String pCode;

    private String pName;

    /**
     * 是否末级
     */
    private Boolean isLastLvl;

    /**
     * 组织类型
     */
    private String orgType;

    /**
     * 是否审批末级节点（1=是，0=否）
     */
    private Boolean isApprovalLastLvl;

    /**
     * 脚本类型
     */
    private String scriptType;

    /**
     * 员工工号
     */
    private String employeeNo;

} 