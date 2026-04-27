package com.jasolar.mis.module.system.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 菜单类型枚举类
 *
 * @author zhaohuang
 */
@Getter
@AllArgsConstructor
public enum MenuTypeEnum {

    CATALOG("catalog", "目录"),
    MENU("menu", "菜单"),
    EMBEDDED("embedded", "内嵌"),
    LINK("link", "外链"),
    BUTTON("button", "按钮");

    /**
     * 类型值
     */
    private final String type;

    /**
     * 类型描述
     */
    private final String description;

}
