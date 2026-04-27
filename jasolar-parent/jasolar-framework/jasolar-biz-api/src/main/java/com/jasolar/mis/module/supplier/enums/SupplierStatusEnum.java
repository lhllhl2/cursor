
package com.jasolar.mis.module.supplier.enums;

import lombok.Getter;

@Getter
public enum SupplierStatusEnum {
    REGISTERED("registered", "已注册",""),
    QUALIFIED("qualified", "合格","0"),
    FULLY_FROZEN("fullFreeze", "完全冻结（不可下单、付款）","3"),
    FROZEN_ORDER("orderFreeze", "冻结下单（可以付款）","2"),
    FROZEN_PAYMENT("payFreeze", "冻结付款（可以下单）","1");
    ;

    private final String code;
    private final String description;
    private final String erpCode;

    SupplierStatusEnum(String code, String description, String erpCode) {
        this.code = code;
        this.description = description;
        this.erpCode = erpCode;
    }

    public static String getErpCodeByCode(String code) {
        for (SupplierStatusEnum supplierStatusEnum : SupplierStatusEnum.values()) {
            if (supplierStatusEnum.getCode().equals(code)) {
                return supplierStatusEnum.getErpCode();
            }
        }
        return null;
    }
}