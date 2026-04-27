package com.jasolar.mis.module.log.api.dto;

import com.jasolar.mis.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

@SuppressWarnings("serial")
@Schema(description = "管理后台 - API 错误日志分页 Request VO")
@Data
public class ApiErrorLogPageReqDTO extends PageParam {

    @Schema(description = "用户编号", example = "666")
    private Long userId;

    @Schema(description = "用户类型", example = "1")
    private Integer userType;

    @Schema(description = "应用名", example = "dashboard")
    private String applicationName;

    @Schema(description = "请求地址", example = "/xx/yy")
    private String requestUrl;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "异常发生时间")
    private LocalDateTime[] exceptionTime;

    @Schema(description = "处理状态", example = "0")
    private Integer processStatus;

}
