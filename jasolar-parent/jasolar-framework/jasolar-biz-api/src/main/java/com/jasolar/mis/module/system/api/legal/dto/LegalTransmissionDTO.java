package com.jasolar.mis.module.system.api.legal.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class LegalTransmissionDTO {
    private String entityCode; // 管报法人代码 (主键)
    private String entityName; // 管报法人名称
    private String fiCode; // 财报法人代码
    private String erpType; // ERP系统
    private String sapCode; // SAP法人代碼
    private String factoryCode; // 總賬工廠代碼
    private String serverName; // 主機
    private Boolean flag; // 是否有效
    private String curCode; // 法人本币
    private String currencyName; // 币别名稱
    private String entitySystem; // 法人体系
    private LocalDateTime updateTime; // 更新时间
}
