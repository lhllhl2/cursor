package com.jasolar.mis.module.supplier.api.admin.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;

@Schema(description = "管理后台 - 交易主体法人新增/修改 Request VO")
@Data
public class TradeLegalDTO implements Serializable {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "20069")
    private Long id;

    @Schema(description = "供应商ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "12652")
    @NotNull(message = "供应商ID不能为空")
    private Long supplierId;

    @Schema(description = "Fii法人代码", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "Fii法人代码不能为空")
    private String legalCode;

    @Schema(description = "Fii法人名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "李四")
    @NotEmpty(message = "Fii法人名称不能为空")
    private String legalName;

    @Schema(description = "有效性（法人状态 1.	有效2.	完全冻结3.	冻结付款 4.	冻结下单）", example = "2")
    private String status;

    @Schema(description = "顺序")
    private Integer sort;

}