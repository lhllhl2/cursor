package com.jasolar.mis.module.supplier.api.admin.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;

@Schema(description = "管理后台 - 联系人信息新增/修改 Request DTO")
@Data
public class ContactDTO implements Serializable {

    @Schema(description = "主键")
    private Long id;

    @Schema(description = "供应商ID")
    @NotNull(message = "供应商ID不能为空")
    private Long supplierId;

    @Schema(description = "业务范围", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "业务范围不能为空")
    private String businessScope;

    @Schema(description = "联系人名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "赵六")
    @NotEmpty(message = "联系人名称不能为空")
    private String contactName;

    @Schema(description = "证件号码")
    private String idCardNumber;

    @Schema(description = "职务", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "职务不能为空")
    private String contactPosition;

    @Schema(description = "电话", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "电话不能为空")
    private String phone;

    @Schema(description = "邮箱", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "邮箱不能为空")
    private String email;

    @Schema(description = "传真", requiredMode = Schema.RequiredMode.REQUIRED)
    private String fax;

    @Schema(description = "类型 1.注册联系人 2.业务联系人", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotEmpty(message = "类型 1.注册联系人 2.业务联系人不能为空")
    private String contactType;

    @Schema(description = "备注")
    private String remarks;

}