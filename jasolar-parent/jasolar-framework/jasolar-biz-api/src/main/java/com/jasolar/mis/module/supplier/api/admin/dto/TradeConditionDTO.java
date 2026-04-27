package com.jasolar.mis.module.supplier.api.admin.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;

@Schema(description = "管理后台 - 交易条件新增/修改 Request VO")
@Data
public class TradeConditionDTO implements Serializable {

    @Schema(description = "主键")
    private Long id;

    @Schema(description = "供应商ID")
    private Long supplierId;

    @Schema(description = "交易条件", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "交易条件不能为空")
    private String transactionConditions;

    @Schema(description = "地点", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "地点不能为空")
    private String location;

    @Schema(description = "使用税别", requiredMode = Schema.RequiredMode.REQUIRED, example = "2")
    @NotEmpty(message = "使用税别不能为空")
    private String taxType;

    @Schema(description = "付款基准日", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "付款基准日不能为空")
    private String paymentBaseDay;

    @Schema(description = "付款期限起算日", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "付款期限起算日不能为空")
    private String paymentTermStartDay;

    @Schema(description = "付款期限", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "付款期限不能为空")
    private Integer paymentTerm;

    @Schema(description = "付款条件", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "付款条件不能为空")
    private String paymentConditions;

    @Schema(description = "付款条件代码", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "付款条件代码不能为空")
    private String paymentConditionCode;

    @Schema(description = "顺序")
    private Integer sort;

    @Schema(description = "Fii法人代码")
    private String legalCode;

    @Schema(description = "Fii法人名称")
    private String legalName;


    @Schema(description = "税别代码")
    private String taxCode;
    @Schema(description = "税别名称")
    private String taxName;

}