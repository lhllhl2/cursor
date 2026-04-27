package com.jasolar.mis.module.system.controller.admin.permission.vo.interfaceResource;

import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - 菜单接口关联 Response VO")
@Data
@ExcelIgnoreUnannotated
public class MenuInterfaceRespVO {

    @Schema(description = "编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "31362")
    @ExcelProperty("编号")
    private Long id;

    @Schema(description = "菜单编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "23135")
    @ExcelProperty("菜单编号")
    private Long menuId;

    @Schema(description = "接口资源编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "32725")
    @ExcelProperty("接口资源编号")
    private Long interfaceId;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("创建时间")
    private LocalDateTime createTime;


    @Schema(description = "接口详情信息", example = "23135")
    private InterfaceResourceRespVO interfaceResource;
}