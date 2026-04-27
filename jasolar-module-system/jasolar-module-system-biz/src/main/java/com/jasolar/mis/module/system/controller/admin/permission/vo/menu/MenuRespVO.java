package com.jasolar.mis.module.system.controller.admin.permission.vo.menu;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "管理后台 - 菜单信息 Response VO")
@Data
@ToString
public class MenuRespVO {

    @Schema(description = "菜单ID")
    private Long id;

    @Schema(description = "菜单名称")
    private String name;

    @Schema(description = "路由路径")
    private String path;

    @Schema(description = "菜单类型：catalog目录,menu菜单,embedded内嵌,link外链,button按钮")
    private String type;

    @Schema(description = "状态：1启用，0禁用")
    private Integer status;

    @Schema(description = "父级菜单ID，顶级菜单为0", example = "0")
    private Long pid;

    @Schema(description = "后端权限标识")
    private String authCode;

    @Schema(description = "重定向路径")
    private String redirect;

    @Schema(description = "组件路径")
    private String component;

    @Schema(description = "子菜单列表")
    private List<MenuRespVO> children;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;

    @Schema(description = "创建者")
    private String creator;

    @Schema(description = "更新者")
    private String updater;

    @Schema(description = "菜单Meta配置信息")
    private MenuMetaVO meta;
}
