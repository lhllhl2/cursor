package com.jasolar.mis.module.supplier.api.admin.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.io.Serializable;

@Schema(description = "管理后台 - 费用代码新增/修改 Request VO")
@Data
public class FeeCodeDTO implements Serializable {

    @Schema(description = "主键")
    private Long id;

    @Schema(description = "供应商ID")
    private Long supplierId;

    @Schema(description = "费用代码", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "费用代码不能为空")
    private String feeCode;

    @Schema(description = "费用代码描述", requiredMode = Schema.RequiredMode.REQUIRED, example = "你说的对")
    @NotEmpty(message = "费用代码描述不能为空")
    private String feeCodeDescription;

    @Schema(description = "顺序")
    private Integer sort;

}