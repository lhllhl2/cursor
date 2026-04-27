package com.jasolar.mis.module.system.domain.admin.permission;

import com.baomidou.mybatisplus.annotation.TableName;
import com.jasolar.mis.framework.mybatis.core.dataobject.BaseIdentityDO;
import lombok.*;
import com.jasolar.mis.framework.mybatis.core.dataobject.BaseDO;

/**
 * 菜单接口关联 DO
 *
 * @author zhahuang
 */
@TableName("system_menu_interface")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MenuInterfaceDO extends BaseIdentityDO {

    /**
     * 菜单编号
     */
    private Long menuId;
    /**
     * 接口资源编号
     */
    private Long interfaceId;

}