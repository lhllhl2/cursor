package com.jasolar.mis.module.log.api.dto;

import com.jasolar.mis.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

@SuppressWarnings("serial")
@Schema(description = "管理后台 - API 访问日志分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@AllArgsConstructor
@NoArgsConstructor
public class ApiAccessLogPageReqDTO extends PageParam {

    @Schema(description = "用户编号", example = "666")
    private Long userId;

    @Schema(description = "用户类型", example = "2")
    private Integer userType;

    @Schema(description = "应用名", example = "dashboard")
    private String applicationName;

    @Schema(description = "请求地址，模糊匹配", example = "/xxx/yyy")
    private String requestUrl;

    @Schema(description = "开始时间", example = "[2022-07-01 00:00:00, 2022-07-01 23:59:59]")
    @NotNull(message = "请求时间不能为空")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime[] beginTime;

    @Schema(description = "执行时长,大于等于，单位：毫秒", example = "100")
    private Integer duration;

    @Schema(description = "结果码", example = "0")
    private Integer resultCode;

}
