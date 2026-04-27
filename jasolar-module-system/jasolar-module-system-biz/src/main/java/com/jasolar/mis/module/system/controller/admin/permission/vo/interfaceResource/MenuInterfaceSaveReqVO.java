package com.jasolar.mis.module.system.controller.admin.permission.vo.interfaceResource;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Schema(description = "管理后台 - 菜单接口关联新增/修改 Request VO")
@Data
public class MenuInterfaceSaveReqVO {


    @Schema(description = "菜单编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "23135")
    @NotNull(message = "菜单编号不能为空")
    private Long menuId;

    @Schema(description = "移除的菜单和接口关联表数据ID列表", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<Long> removeIds;

    @Schema(description = "新增关联的接口ID列表", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<Long> addInterfaceIds;

}