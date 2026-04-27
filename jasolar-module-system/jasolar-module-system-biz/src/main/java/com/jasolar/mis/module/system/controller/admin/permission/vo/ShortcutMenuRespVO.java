package com.jasolar.mis.module.system.controller.admin.permission.vo;

import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - 用户快捷菜单 Response VO")
@Data
@ExcelIgnoreUnannotated
public class ShortcutMenuRespVO {

    @Schema(description = "用户NO")
    @ExcelProperty("用户NO")
    private String userNo;

    @Schema(description = "用户ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "29523")
    @ExcelProperty("用户ID")
    private Long userId;

    @Schema(description = "菜单ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "30189")
    @ExcelProperty("菜单ID")
    private Long menuId;

    @Schema(description = "排序", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("排序")
    private Integer sort;

    @Schema(description = "是否置顶")
    @ExcelProperty("是否置顶")
    private Boolean isPinned;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

}