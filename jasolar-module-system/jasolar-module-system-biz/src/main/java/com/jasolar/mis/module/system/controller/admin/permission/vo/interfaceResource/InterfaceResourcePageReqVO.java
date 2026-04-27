package com.jasolar.mis.module.system.controller.admin.permission.vo.interfaceResource;

import com.jasolar.mis.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

import static com.jasolar.mis.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - 接口资源分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class InterfaceResourcePageReqVO extends PageParam {

    @Schema(description = "微服务名称", example = "张三")
    private String serviceName;

    @Schema(description = "接口分类名称", example = "xshe")
    private String categoryName;

    @Schema(description = "接口所属控制器名称", example = "王五")
    private String controllerName;

    @Schema(description = "接口所在方法名称", example = "张三")
    private String functionName;

    @Schema(description = "接口名称", example = "张三")
    private String name;

    @Schema(description = "接口URL", example = "http://www.roselife.com")
    private String url;

    @Schema(description = "HTTP方法(GET,POST,PUT,DELETE等)")
    private String method;

    @Schema(description = "接口描述", example = "你说的对")
    private String description;

    @Schema(description = "状态(0-禁用, 1-启用)", example = "2")
    private Short status;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

}