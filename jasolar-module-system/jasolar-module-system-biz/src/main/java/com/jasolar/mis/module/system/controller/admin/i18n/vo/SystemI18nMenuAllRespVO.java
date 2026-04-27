package com.jasolar.mis.module.system.controller.admin.i18n.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Schema(description = "管理后台 - 所有菜单国际化配置 Response VO")
@Data
public class SystemI18nMenuAllRespVO {

    @Schema(description = "中文菜单树形结构")
    @JsonProperty("zh-CN")
    private List<SystemI18nMenuTreeRespVO> zhCN;

    @Schema(description = "英文菜单树形结构")
    @JsonProperty("en-US")
    private List<SystemI18nMenuTreeRespVO> enUS;
} 