package com.jasolar.mis.module.supplier.api.admin.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.io.Serializable;

@Schema(description = "管理后台 - 供应商-法人参股人信息新增/修改 Request VO")
@Data
public class PrincipalDTO implements Serializable {

    @Schema(description = "主键")
    private Long id;

    @Schema(description = "供应商ID")
    private Long supplierId;

    @Schema(description = "人员类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "2")
    @NotEmpty(message = "人员类型不能为空")
    private String principalType;

    @Schema(description = "名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "赵六")
    @NotEmpty(message = "名称不能为空")
    private String principalName;

    @Schema(description = "证件号码", requiredMode = Schema.RequiredMode.REQUIRED)
    private String idCardNo;

    @Schema(description = "电话", requiredMode = Schema.RequiredMode.REQUIRED)
    private String phone;

    @Schema(description = "传真", requiredMode = Schema.RequiredMode.REQUIRED)
    private String fax;

    @Schema(description = "邮箱", requiredMode = Schema.RequiredMode.REQUIRED)
    private String email;

}