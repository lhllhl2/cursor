package com.jasolar.mis.module.bpm.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 微服务枚举
 */
@Getter
@AllArgsConstructor
public enum ModuleEnum {
    SYSTEM("system-service", "系统管理服务"),
    INFRA("infra-service", "基础设施服务"),
    BPM("bpm-service", "流程中心服务"),
    LOG("log-service", "日志服务"),
    INTEGRATION("intergration-service", "集成中心服务"),
    MASTERDATA("masterdata-service", "主数据服务"),
    SUPPLIER("supplier-service", "供应商服务"),
    BUDGET("budget-service", "预算服务"),
    SOURCING("sourcing-service", "寻源服务"),
    ORDER("order-service", "订单服务"),
    SETTLEMENT("settlement-service", "结算服务"),
    ENGINEERING("engineering-service", "工程管理");


    /**
     * 服务ID
     */
    private final String name;

    /**
     * 服务描述
     */
    private final String desc;

    /**
     * 通过服务ID获取枚举
     */
    public static ModuleEnum getByName(String moduleName) {
        if (moduleName == null) {
            return null;
        }
        for (ModuleEnum value : values()) {
            if (value.getName().equals(moduleName)) {
                return value;
            }
        }
        return null;
    }


    @JsonCreator
    public static ModuleEnum fromName(String moduleName) {
        if (moduleName == null) {
            return null;
        }
        for (ModuleEnum value : values()) {
            if (value.name.equals(moduleName)) {
                return value;
            }
        }
        throw new IllegalArgumentException("Unknown enum type " + moduleName);
    }

    @JsonValue
    public String getName() {
        return name;
    }
} 