package com.jasolar.mis.module.system.domain.admin.permission;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;
import com.jasolar.mis.framework.mybatis.core.dataobject.BaseIdentityDO;

/**
 * 用户快捷菜单 DO
 *
 * @author 管理员
 */
@TableName("system_shortcut_menu")
@Data
@SuppressWarnings("serial")
public class ShortcutMenuDO extends BaseIdentityDO {

    /**
     * 用户NO
     */
    private String userNo;
    /**
     * 用户ID
     */
    private Long userId;
    /**
     * 菜单ID
     */
    private Long menuId;
    /**
     * 排序
     */
    private Integer sort;
    /**
     * 是否置顶
     */
    private Boolean isPinned;

}