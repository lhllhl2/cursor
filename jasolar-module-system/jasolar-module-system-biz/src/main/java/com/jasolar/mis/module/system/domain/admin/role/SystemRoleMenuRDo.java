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
 * 角色菜单关联 DO
 *
 * @author ruoyi
 */
@TableName(value = "system_role_menu_r")
@Data
@Builder
@EqualsAndHashCode(callSuper = true)
public class SystemRoleMenuRDo extends BaseDO {

    /**
     * 主键ID
     */
    @TableId(value = "id",type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 角色ID
     */
    private Long roleId;

    /**
     * 菜单ID
     */
    private Long menuId;

}
