package com.jasolar.mis.module.system.domain.admin.org;

import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.jasolar.mis.framework.mybatis.core.dataobject.BaseDO;
import lombok.*;

/**
 * 用户组组织关系 DO
 *
 * @author jasolar
 */
@TableName("system_user_group_organization_r")
@KeySequence("system_user_group_organization_r_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserGroupOrganizationDO extends BaseDO {

    /**
     * 主键ID
     */
    @TableId
    private Long id;

    /**
     * 用户组ID
     */
    private Long userGroupId;

    /**
     * 组织ID
     */
    private Long organizationId;

} 