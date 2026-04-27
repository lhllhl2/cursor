package com.jasolar.mis.module.system.controller.admin.permission.vo.menu;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Schema(description = "管理后台 - 菜单修改 Request VO")
@Data
public class MenuUpdateReqVO {

    @Schema(description = "菜单编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    @NotNull(message = "菜单编号不能为空")
    private Long id;

    @Schema(description = "菜单名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "用户管理")
    @NotBlank(message = "菜单名称不能为空")
    @Size(max = 50, message = "菜单名称长度不能超过50个字符")
    private String name;

    @Schema(description = "路由路径", example = "/system/user")
    @Size(max = 200, message = "路由路径不能超过200个字符")
    private String path;

    /**
     * 父级菜单ID，顶级菜单为0
     */
    private Long pid;

    @Schema(description = "后端权限标识", example = "system:user:query")
    @Size(max = 100, message = "权限标识长度不能超过100个字符")
    private String authCode;

    @Schema(description = "重定向路径", example = "/system/user/list")
    @Size(max = 200, message = "重定向路径不能超过200个字符")
    private String redirect;

    @Schema(description = "菜单类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "menu")
    @NotBlank(message = "菜单类型不能为空")
    private String type;

    @Schema(description = "组件路径", example = "system/user/index")
    @Size(max = 200, message = "组件路径不能超过200个字符")
    private String component;

    @Schema(description = "状态：1启用，0禁用", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "状态不能为空")
    private Integer status;

    @Schema(description = "菜单Meta配置信息")
    @Valid
    private MenuMetaVO meta;

} 