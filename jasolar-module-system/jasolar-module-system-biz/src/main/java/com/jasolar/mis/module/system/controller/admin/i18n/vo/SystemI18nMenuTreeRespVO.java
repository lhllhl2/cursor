package com.jasolar.mis.module.system.controller.admin.i18n.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Schema(description = "管理后台 - 菜单国际化树形结构 Response VO")
@Data
public class SystemI18nMenuTreeRespVO {

    @Schema(description = "菜单ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long id;

    @Schema(description = "菜单名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "用户管理")
    private String name;

    @Schema(description = "菜单标题", requiredMode = Schema.RequiredMode.REQUIRED, example = "系统管理")
    private String title;

    @Schema(description = "父菜单ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "0")
    private Long pid;

    @Schema(description = "国际化翻译数据", requiredMode = Schema.RequiredMode.REQUIRED, example = "{\"button.add\": {\"zh-CN\": \"新增\", \"en-US\": \"Add\"}}")
    private Map<String, Map<String, String>> jsonData;

    @Schema(description = "子菜单列表")
    private List<SystemI18nMenuTreeRespVO> children;
} 