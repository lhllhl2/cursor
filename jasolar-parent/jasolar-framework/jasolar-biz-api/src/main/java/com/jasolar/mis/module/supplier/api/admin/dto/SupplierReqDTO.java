package com.jasolar.mis.module.supplier.api.admin.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.io.Serializable;

@Schema(description = "管理后台 - 供应商主新增/修改 Request VO")
@Data
public class SupplierReqDTO implements Serializable {

    @Schema(description = "主键", example = "21180")
    private Long id;

    @Schema(description = "供应商门户系统登录账号", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "供应商门户系统登录账号不能为空")
    private String supplierNo;

    @Schema(description = "注册状态 1.草稿 2.注册中 3.已注册 4.已拒绝", requiredMode = Schema.RequiredMode.REQUIRED, example = "2")
    @NotEmpty(message = "注册状态 1.草稿 2.注册中 3.已注册 4.已拒绝不能为空")
    private String regStatus;

    @Schema(description = "供应商状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotEmpty(message = "供应商状态不能为空")
    private String status;

    @Schema(description = "供应商类型")
    private String supplierClass;

    @Schema(description = "是否强势供应商")
    private Short powerful;

    @Schema(description = "供应商注册名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "赵六")
    @NotEmpty(message = "供应商注册名称不能为空")
    private String supplierName;

    @Schema(description = "供应商限制类型", example = "2")
    private String restrictionType;

    @Schema(description = "限制性供应商有效期")
    private String restrictionValidity;

    @Schema(description = "供应商类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotEmpty(message = "供应商类型不能为空")
    private String supplierType;

    @Schema(description = "启用限制性费用代码 1启用 0不启用")
    private Short enableRestrictedFeeCode;

    @Schema(description = "供应商名称（中文）")
    private String supplierNameCn;

    @Schema(description = "供应商名称（英文）")
    private String supplierNameEn;

    @Schema(description = "供应商名称（非中英文）")
    private String supplierNameOther;

    @Schema(description = "税务登记证号码", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "税务登记证号码不能为空")
    private String taxRegNumber;

    @Schema(description = "注册人名", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "注册人名不能为空")
    private String registrant;

    @Schema(description = "注册人名电话", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "注册人名电话不能为空")
    private String registrantPhone;

    @Schema(description = "注册人名邮箱", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "注册人名邮箱不能为空")
    private String registrantEmail;

    @Schema(description = "注册国家（区域）编码", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "注册国家（区域）编码不能为空")
    private String regCountryCode;

    @Schema(description = "注册国家（区域）名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "赵六")
    @NotEmpty(message = "注册国家（区域）名称不能为空")
    private String regCountryName;

    @Schema(description = "供应商子集团名称", example = "王五")
    private String subGroupName;

    @Schema(description = "子集团码")
    private String subGroupCode;

    @Schema(description = "注册类型", example = "2")
    private String regType;

    @Schema(description = "注册省/直辖市编码CODE", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "注册省/直辖市编码CODE不能为空")
    private String regProvinceCode;

    @Schema(description = "注册省/直辖市名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "xshe")
    @NotEmpty(message = "注册省/直辖市名称不能为空")
    private String regProvinceName;

    @Schema(description = "缴纳社保人数", example = "20506")
    private Integer socialSecurityCount;

    @Schema(description = "注册城市")
    private String regCity;

    @Schema(description = "注册资本")
    private Short registeredCapital;

    @Schema(description = "注册资本单位")
    private String registeredCapitalUnit;

    @Schema(description = "关联企业名称", example = "xshe")
    private String associatedEnterpriseName;

    @Schema(description = "实缴资本")
    private Short paidInCapital;

    @Schema(description = "实缴资本单位")
    private String paidInCapitalUnit;

    @Schema(description = "经营范围")
    private String businessScope;

    @Schema(description = "主营产品")
    private String mainProducts;

    @Schema(description = "供应商注册地址")
    private String supplierRegAddress;

    @Schema(description = "供应商生产地址")
    private String supplierProdAddress;

    @Schema(description = "供应商网址")
    private String supplierWebsite;

//    @Schema(description = "邀请人", requiredMode = Schema.RequiredMode.REQUIRED)
//    @NotEmpty(message = "邀请人不能为空")
//    private String inviter;

    @Schema(description = "邀请人名字", requiredMode = Schema.RequiredMode.REQUIRED, example = "张三")
    @NotEmpty(message = "邀请人名字不能为空")
    private String inviterName;

    @Schema(description = "邀请人电话", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "邀请人电话不能为空")
    private String inviterPhone;

    @Schema(description = "邀请人邮箱", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "邀请人邮箱不能为空")
    private String inviterEmail;

    @Schema(description = "供应商注册编号")
    private String supplierRegNo;

    @Schema(description = "新增理由原因")
    private String reasonForAddition;

    @Schema(description = "新增理由详细说明")
    private String detailedReasonForAddition;

    @Schema(description = "评鉴")
    private String evaluation;

    @Schema(description = "评鉴等级")
    private String evaluationLevel;

    @Schema(description = "评鉴说明", example = "你说的对")
    private String evaluationDescription;

    @Schema(description = "厂区位置")
    private String factoryLocation;

    @Schema(description = "事业群编码")
    private String businessGroupCode;

    @Schema(description = "事业群名称", example = "张三")
    private String businessGroupName;

    @Schema(description = "事业处编码", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "事业处编码不能为空")
    private String businessUnitCode;

    @Schema(description = "事业处名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "赵六")
    @NotEmpty(message = "事业处名称不能为空")
    private String businessUnitName;

    @Schema(description = "课级单位")
    private String departmentLevel;

    @Schema(description = "经管人员")
    private String mgtPerson;

    @Schema(description = "经管人员分机")
    private String mgtPersonExtension;

    @Schema(description = "采购人员")
    private String purchaser;

    @Schema(description = "采购人员分机")
    private String purchaserExtension;

    @Schema(description = "核买形态")
    private String checkMode;

    @Schema(description = "采购保荐人")
    private String sponsor;

    @Schema(description = "采购保荐人电话")
    private String sponsorPhone;

    @Schema(description = "采购保荐人职务")
    private String sponsorPosition;

    @Schema(description = "采购保荐人邮箱")
    private String sponsorEmail;

    @Schema(description = "采购保荐人部门")
    private String sponsorDepartment;

    @Schema(description = "设置法人CODE")
    private String legalCode;

    @Schema(description = "数据归属人员CODE")
    private String userNo;;

    @Schema(description = "数据所属部门CODE")
    private String deptCode;

}