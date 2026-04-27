package com.jasolar.mis.module.system.controller.admin.permission.vo.interfaceResource;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Schema(description = "管理后台 - 接口资源新增/修改 Request VO")
@Data
public class InterfaceResourceSaveReqVO {

    @Schema(description = "接口资源编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "15412")
    private Long id;

    @Schema(description = "微服务名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "张三")
    @NotEmpty(message = "微服务名称不能为空")
    private String serviceName;

    @Schema(description = "接口分类名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "xshe")
    @NotEmpty(message = "接口分类名称不能为空")
    private String categoryName;

    @Schema(description = "接口所属控制器名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "王五")
    @NotEmpty(message = "接口所属控制器名称不能为空")
    private String controllerName;

    @Schema(description = "接口所在方法名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "张三")
    @NotEmpty(message = "接口所在方法名称不能为空")
    private String functionName;

    @Schema(description = "接口名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "张三")
    @NotEmpty(message = "接口名称不能为空")
    private String name;

    @Schema(description = "接口URL", requiredMode = Schema.RequiredMode.REQUIRED, example = "http://www.roselife.com")
    @NotEmpty(message = "接口URL不能为空")
    private String url;

    @Schema(description = "HTTP方法(GET,POST,PUT,DELETE等)", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "HTTP方法(GET,POST,PUT,DELETE等)不能为空")
    private String method;

    @Schema(description = "接口描述", example = "你说的对")
    private String description;

    @Schema(description = "状态(0-禁用, 1-启用)", requiredMode = Schema.RequiredMode.REQUIRED, example = "2")
    @NotNull(message = "状态(0-禁用, 1-启用)不能为空")
    private Short status;

}