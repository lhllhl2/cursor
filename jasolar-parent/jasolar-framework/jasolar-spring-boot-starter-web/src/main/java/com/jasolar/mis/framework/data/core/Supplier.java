package com.jasolar.mis.framework.data.core;

import com.jasolar.mis.module.supplier.enums.SupplierStatusEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;


@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class Supplier {

    @Schema(description = "主键")
    private Long id;
    @Schema(description = "供应商门户系统登录账号")
    private String supplierNo;
    @Schema(description = "注册状态 1.草稿 2.注册中 3.已注册 4.已拒绝")
    private String regStatus;
    /**
     *{@link SupplierStatusEnum}
     */
    @Schema(description = "供应商状态")
    private String status;
    @Schema(description = "供应商类型")
    private String supplierType;
    @Schema(description = "供应商注册名称")
    private String supplierName;
    @Schema(description = "供应商名称（中文）")
    private String supplierNameCn;
    @Schema(description = "供应商名称（英文）")
    private String supplierNameEn;
    @Schema(description = "供应商名称（非中英文）")
    private String supplierNameOther;
    @Schema(description = "供应商类型")
    private String supplierClass;
    @Schema(description = "设置法人CODE")
    private String legalCode;
    @Schema(description = "数据归属人员CODE")
    private String userNo;
    @Schema(description = "数据所属部门CODE")
    private String deptCode;
    private String supplierCode;
}
