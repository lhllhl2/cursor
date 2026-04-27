package com.jasolar.mis.framework.data.core;

import java.io.Serializable;

import com.jasolar.mis.framework.common.enums.CommonStatusEnum;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * 法人
 * 
 * @author galuo
 * @date 2025-03-28 13:40
 *
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@SuppressWarnings("serial")
public class Legal implements Serializable {

    /** 法人代码 */
    private String legalCode;

    /** 法人名称 */
    private String legalName;

    /** 有效状态, 参见 {@link CommonStatusEnum} 枚举 */
    private int status;

    /** 法人本币 */
    private String currency;

    /**
     * 财报法人代码
     */
    private String fiCode;
    /**
     * ERP系统
     */
    private String erpType;
    /**
     * SAP法人代碼
     */
    private String sapCode;
    /**
     * 總賬工廠代碼
     */
    private String factoryCode;
    /**
     * 主機
     */
    private String serverName;
    /**
     * 法人体系
     */
    private String entitySystem;

    /**
     * 国家
     */
    private String country;

    /**
     * 联系方式
     */
    private String contactInformation;

    /**
     * 银行账号
     */
    private String bankAccount;

    /**
     * 开户银行名称
     */
    private String bankName;


    /**
     * 注册地址/公司所在地（台湾法人）
     */
    private String registeredAddress;



    /**
     * 营业执照编号
     */
    private String licenseNo;
}
