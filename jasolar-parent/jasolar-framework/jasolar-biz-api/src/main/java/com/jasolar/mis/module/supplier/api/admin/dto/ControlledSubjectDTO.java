package com.jasolar.mis.module.supplier.api.admin.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * <pre>
 * |
 * </pre>
 *
 * <br>
 * JDK 版本：17
 *
 * @author yxiacq
 * @version 1.0
 * @since 2025-05-08
 */

@Data
public class ControlledSubjectDTO {

    @Schema(description = "供应商登录号")
    private String supplierNO;

    @Schema(description = "法人code")
    private String legalCode;

    @Schema(description = "统制科目")
    private String controlledSubject;

    @Schema(description = "统制科目名称")
    private String controlledSubjectName;
}
