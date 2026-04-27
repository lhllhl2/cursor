package com.jasolar.mis.module.supplier.api.admin.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.io.Serializable;

/**
 * <pre>|</pre>
 *
 * <br>JDK 版本：17
 *
 * @author yxiacq
 * @version 1.0
 * @since 2025-04-07
 */

@Data
public class BankAccountQueryDTO implements Serializable {

    @Schema(description = "供应商ID",requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "供应商ID不能为空")
    private Long supplierId;

    @Schema(description = "法人代码",requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "法人代码不能为空")
    private String legalCode;

}
