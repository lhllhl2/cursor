package com.jasolar.mis.module.system.controller.admin.permission.vo.menu;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Schema(description = "管理后台 - 菜单 Request VO")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MenuReqVO {

    @Schema(description = "菜单名称，模糊匹配", example = "jasolar")
    private String name;

    @Schema(description = "菜单类型", example = "menu")
    private String type;

    @Schema(description = "后端权限标识", example = "system:user:query")
    private String authCode;

    @Schema(description = "路由路径", example = "/system/user")
    private String path;

    @Schema(description = "展示状态，参见 CommonStatusEnum 枚举类", example = "1")
    private Integer status;

} 