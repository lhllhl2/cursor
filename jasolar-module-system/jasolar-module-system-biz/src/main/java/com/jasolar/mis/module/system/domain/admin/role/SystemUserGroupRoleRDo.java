package com.jasolar.mis.module.system.domain.admin.role;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.jasolar.mis.framework.mybatis.core.dataobject.BaseDO;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 用户组角色关联 DO
 *
 * @author ruoyi
 */
@TableName(value = "system_user_group_role_r")
@Data
@Builder
@EqualsAndHashCode(callSuper = true)
public class SystemUserGroupRoleRDo extends BaseDO {

    /**
     * 主键ID
     */
    @TableId(value = "id",type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 用户组ID
     */
    private Long groupId;

    /**
     * 角色ID
     */
    private Long roleId;

}
