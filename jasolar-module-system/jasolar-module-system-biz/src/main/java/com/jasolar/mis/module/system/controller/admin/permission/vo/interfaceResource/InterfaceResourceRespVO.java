package com.jasolar.mis.module.system.controller.admin.permission.vo.interfaceResource;

import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - 接口资源 Response VO")
@Data
@ExcelIgnoreUnannotated
public class InterfaceResourceRespVO {

    @Schema(description = "接口资源编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "15412")
    @ExcelProperty("接口资源编号")
    private Long id;

    @Schema(description = "微服务名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "张三")
    @ExcelProperty("微服务名称")
    private String serviceName;

    @Schema(description = "接口分类名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "xshe")
    @ExcelProperty("接口分类名称")
    private String categoryName;

    @Schema(description = "接口所属控制器名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "王五")
    @ExcelProperty("接口所属控制器名称")
    private String controllerName;

    @Schema(description = "接口所在方法名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "张三")
    @ExcelProperty("接口所在方法名称")
    private String functionName;

    @Schema(description = "接口名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "张三")
    @ExcelProperty("接口名称")
    private String name;

    @Schema(description = "接口URL", requiredMode = Schema.RequiredMode.REQUIRED, example = "http://www.roselife.com")
    @ExcelProperty("接口URL")
    private String url;

    @Schema(description = "HTTP方法(GET,POST,PUT,DELETE等)", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("HTTP方法(GET,POST,PUT,DELETE等)")
    private String method;

    @Schema(description = "接口描述", example = "你说的对")
    @ExcelProperty("接口描述")
    private String description;

    @Schema(description = "状态(0-禁用, 1-启用)", requiredMode = Schema.RequiredMode.REQUIRED, example = "2")
    @ExcelProperty("状态(0-禁用, 1-启用)")
    private Short status;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

}