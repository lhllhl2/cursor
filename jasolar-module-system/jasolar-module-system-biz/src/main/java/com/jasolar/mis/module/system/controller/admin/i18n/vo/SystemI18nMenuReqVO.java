package com.jasolar.mis.module.system.controller.admin.i18n.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.Map;

@Schema(description = "管理后台 - 菜单国际化配置 Request VO")
@Data
public class SystemI18nMenuReqVO {

    @Schema(description = "菜单ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "15781450976591872")
    @NotNull(message = "菜单ID不能为空")
    private Long menuId;

    @Schema(description = "翻译数据", requiredMode = Schema.RequiredMode.REQUIRED, 
            example = "{\"button.add\": {\"title\": \"新增按钮\", \"zh-CN\": \"新增\", \"en-US\": \"Add\"}}")
    @NotNull(message = "翻译数据不能为空")
    private Map<String, Map<String, String>> jsonData;
} 