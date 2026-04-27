package com.jasolar.mis.module.system.controller.admin.permission.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "管理后台 - 用户快捷菜单新增/修改 Request VO")
@Data
public class ShortcutMenuSaveReqVO {


    @Schema(description = "用户NO")
    private String userNo;

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "菜单ID")
    private Long menuId;

    @Schema(description = "排序")
    private Integer sort;

    @Schema(description = "是否置顶")
    private Boolean isPinned;

}