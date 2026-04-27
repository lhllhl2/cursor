package com.jasolar.mis.module.system.domain.admin.org;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.jasolar.mis.framework.mybatis.core.dataobject.BaseDO;
import lombok.*;

/**
 * 组织机构 DO
 *
 * @author lingma
 */
@TableName(value = "system_org", autoResultMap = true)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class SystemOrgDO extends BaseDO {

    /**
     * 主键ID
     */
    @TableId(value = "ID")
    private String id;

    /**
     * 组织名称
     */
    private String orgName;

    /**
     * 上级组织ID
     */
    private String parentId;

    private String rootNode;

    /**
     * 公司编码
     */
    private String companyCode;

    /**
     * 公司名称
     */
    private String companyName;

    /**
     * 成本组织名称
     */
    private String costOrgName;

    /**
     * 组织负责人ID
     */
    private String orgHeadId;

    /**
     * 组织法人ID
     */
    private String orgFgId;

    /**
     * 组织属性
     */
    private String orgAttribute;

    /**
     * 组织全路径
     */
    private String orgFullPath;
}