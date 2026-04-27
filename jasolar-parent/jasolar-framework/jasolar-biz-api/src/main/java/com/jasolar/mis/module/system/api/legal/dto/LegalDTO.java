package com.jasolar.mis.module.system.api.legal.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

@SuppressWarnings("serial")
@Schema(description = "管理后台 - 法人新增/修改 Request VO")
@Data
public class LegalDTO implements Serializable {

    @Schema(description = "ID;雪花算法自动生成。修改时必传", example = "26946")
    private Long id;

    @Schema(description = "法人代码;有效的数据必须唯一")
    private String code;

    @Schema(description = "法人名称", example = "王五")
    private String name;

    @Schema(description = "有效状态;0有效，1无效", example = "1")
    private Integer status;

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
     * 法人本币
     */
    private String currency;
    /**
     * 法人体系
     */
    private String entitySystem;


    @Schema(description = "国别代码")
    private String countryCode ;
    @Schema(description = "国别名称")
    private String countryName ;

    @Schema(description = "国别")
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