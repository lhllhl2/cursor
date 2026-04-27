package com.jasolar.mis.module.system.controller.admin.role.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Schema(description = "管理后台 - 根据角色ID查询菜单树 Request VO")
@Data
public class RoleMenuTreeReqVO {

    @Schema(description = "角色ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    @NotNull(message = "角色ID不能为空")
    private Long roleId;
} 