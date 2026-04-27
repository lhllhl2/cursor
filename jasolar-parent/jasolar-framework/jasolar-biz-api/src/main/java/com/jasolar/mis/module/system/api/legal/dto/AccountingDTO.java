package com.jasolar.mis.module.system.api.legal.dto;

import java.io.Serializable;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 会计科目
 * 
 * @author galuo
 * @date 2025-05-08 12:58
 *
 */
@SuppressWarnings("serial")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class AccountingDTO implements Serializable {

    @Schema(description = "科目代码", requiredMode = Schema.RequiredMode.REQUIRED)
    private String accountingCode;

    @Schema(description = "科目名称", requiredMode = Schema.RequiredMode.REQUIRED)
    private String accountingName;

}