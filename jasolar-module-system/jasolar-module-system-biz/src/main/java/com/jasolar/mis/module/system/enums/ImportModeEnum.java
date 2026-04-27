package com.jasolar.mis.module.system.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 导入模式枚举类
 *
 * @author jasolar
 */
@Getter
@AllArgsConstructor
public enum ImportModeEnum {

    /**
     * 追加模式：只插入不存在的记录，保留已存在的记录
     */
    APPEND("APPEND", "追加模式"),

    /**
     * 覆盖模式：先删除已存在的记录，再插入新记录
     */
    OVERWRITE("OVERWRITE", "覆盖模式");

    /**
     * 模式值
     */
    private final String mode;

    /**
     * 模式描述
     */
    private final String description;

}

