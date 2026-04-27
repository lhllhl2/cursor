package com.jasolar.mis.module.supplier.api.admin.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

@Schema(description = "管理后台 - 银行账户信息新增/修改 Request VO")
@Data
public class BankAccountDTO implements Serializable {

    @Schema(description = "主键")
    private Long id;

    @Schema(description = "供应商ID")
    private Long supplierId;

    @Schema(description = "Fii法人代码", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "Fii法人代码不能为空")
    private String legalCode;

    @Schema(description = "Fii法人名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "张三")
    @NotEmpty(message = "Fii法人名称不能为空")
    private String legalName;

    @Schema(description = "银行账户名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "xshe")
    @NotEmpty(message = "银行账户名称不能为空")
    private String accountName;

    @Schema(description = "开户银行所在国家", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "开户银行所在国家不能为空")
    private String bankCountry;

    @Schema(description = "银行代码类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotEmpty(message = "银行代码类型不能为空")
    private String bankCodeType;

    @Schema(description = "银行代码", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "银行代码不能为空")
    private String bankCode;

    @Schema(description = "银行名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "李四")
    @NotEmpty(message = "银行名称不能为空")
    private String bankName;

    @Schema(description = "分行名称", example = "赵六")
    private String branchName;

    @Schema(description = "银行地址")
    private String bankAddress;

    @Schema(description = "银行账号", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "银行账号不能为空")
    private String accountNo;

    @Schema(description = "手续费", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "手续费不能为空")
    private BigDecimal fee;

    @Schema(description = "BankType", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotEmpty(message = "BankType不能为空")
    private String bankType;

    @Schema(description = "交易币别", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "交易币别不能为空")
    private String currency;

    @Schema(description = "账户类型 1.主要账户 2.基本账户", requiredMode = Schema.RequiredMode.REQUIRED, example = "2")
    @NotEmpty(message = "账户类型 1.主要账户 2.基本账户不能为空")
    private String accountType;

    @Schema(description = "账户类别1.供应商账号2.金融公司账号", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "账户类别1.供应商账号2.金融公司账号不能为空")
    private String accountCategory;

    @Schema(description = "0.有效1.无效", requiredMode = Schema.RequiredMode.REQUIRED, example = "2")
    @NotEmpty(message = "0.有效1.无效不能为空")
    private String status;

    @Schema(description = "切结状态")
    private String graftStatus;

    @Schema(description = "付款厂商名称")
    private String manufacturerCode;

    @Schema(description = "付款厂商名称")
    private String manufacturerName;

    @Schema(description = "切结银行账户名称")
    private String graftAccountName;

    @Schema(description = "切结开户银行所在国家")
    private String graftBankCountry;

    @Schema(description = "切结银行代码类型")
    private String graftBankCodeType;

    @Schema(description = "切结银行代码")
    private String graftBankCode;

    @Schema(description = "切结银行名称")
    private String graftBankName;

    @Schema(description = "切结分行名称")
    private String graftBranchName;

    @Schema(description = "切结银行地址")
    private String graftBankAddress;

    @Schema(description = "切结银行账号")
    private String graftAccountNo;

    @Schema(description = "切结手续费")
    private BigDecimal graftFee;

    @Schema(description = "切结BankType")
    private String graftBankType;

}