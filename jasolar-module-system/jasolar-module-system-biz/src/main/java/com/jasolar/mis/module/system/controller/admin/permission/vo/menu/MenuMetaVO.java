package com.jasolar.mis.module.system.controller.admin.permission.vo.menu;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.Map;

@Schema(description = "管理后台 - 菜单Meta信息 VO")
@Data
public class MenuMetaVO {

    @Schema(description = "激活时显示的图标", example = "ep:user-filled")
    private String activeIcon;

    @Schema(description = "作为路由时，需要激活的菜单的Path", example = "/system/user")
    private String activePath;

    @Schema(description = "固定在标签栏", example = "false")
    private Boolean affixTab;

    @Schema(description = "在标签栏固定的顺序", example = "1")
    private Integer affixTabOrder;

    @Schema(description = "徽标内容", example = "99+")
    private String badge;

    @Schema(description = "徽标类型", example = "normal")
    private String badgeType;

    @Schema(description = "徽标颜色", example = "destructive")
    private String badgeVariants;

    @Schema(description = "在菜单中隐藏下级", example = "false")
    private Boolean hideChildrenInMenu;

    @Schema(description = "在面包屑中隐藏", example = "false")
    private Boolean hideInBreadcrumb;

    @Schema(description = "在菜单中隐藏", example = "false")
    private Boolean hideInMenu;

    @Schema(description = "在标签栏中隐藏", example = "false")
    private Boolean hideInTab;

    @Schema(description = "菜单图标", example = "ep:user")
    private String icon;

    @Schema(description = "内嵌Iframe的URL", example = "https://example.com")
    private String iframeSrc;

    @Schema(description = "是否缓存页面", example = "true")
    private Boolean keepAlive;

    @Schema(description = "外链页面的URL", example = "https://external.com")
    private String link;

    @Schema(description = "同一个路由最大打开的标签数", example = "5")
    private Integer maxNumOfOpenTab;

    @Schema(description = "无需基础布局", example = "false")
    private Boolean noBasicLayout;

    @Schema(description = "是否在新窗口打开", example = "false")
    private Boolean openInNewWindow;

    @Schema(description = "菜单排序", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "菜单排序不能为空")
    private Integer menuOrder;

    @Schema(description = "额外的路由参数", example = "{\"id\": 1, \"type\": \"admin\"}")
    private Map<String, Object> query;

    @Schema(description = "菜单标题", requiredMode = Schema.RequiredMode.REQUIRED, example = "system.user.add")
    @Size(max = 100, message = "菜单标题长度不能超过100个字符")
    private String title;
} 