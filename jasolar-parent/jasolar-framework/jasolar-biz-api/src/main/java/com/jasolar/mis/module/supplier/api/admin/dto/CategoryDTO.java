package com.jasolar.mis.module.supplier.api.admin.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.io.Serializable;

@Schema(description = "管理后台 - 产品类别新增/修改 Request VO")
@Data
public class CategoryDTO implements Serializable {

    @Schema(description = "主键")
    private Long id;

    @Schema(description = "供应商ID")
    private Long supplierId;

    @Schema(description = "一阶类别", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "一阶类别不能为空")
    private String firstLevelCode;

    @Schema(description = "二阶类别", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "二阶类别不能为空")
    private String secondLevelCode;

    @Schema(description = "三阶类别", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "三阶类别不能为空")
    private String thirdLevelCode;

    @Schema(description = "产品（服务）名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "张三")
    @NotEmpty(message = "产品（服务）名称不能为空")
    private String productServiceName;

    @Schema(description = "顺序")
    private Integer sort;

}