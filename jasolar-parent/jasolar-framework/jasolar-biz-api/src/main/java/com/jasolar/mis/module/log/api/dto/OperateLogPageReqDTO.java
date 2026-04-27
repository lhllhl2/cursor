package com.jasolar.mis.module.log.api.dto;

import com.jasolar.mis.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

@SuppressWarnings("serial")
@Schema(name = "RPC 服务 - 操作日志分页 Request DTO")
@Data
public class OperateLogPageReqDTO extends PageParam {

    @Schema(description = "用户编号", example = "1")
    private Long userId;

    @Schema(description = "操作模块业务编号", example = "1")
    private Long bizId;

    @Schema(description = "操作模块，模拟匹配", example = "订单")
    private String type;

    @Schema(description = "操作名，模拟匹配", example = "创建订单")
    private String subType;

    @Schema(description = "操作明细，模拟匹配", example = "修改编号为 1 的用户信息")
    private String action;

    @Schema(description = "开始时间", example = "[2022-07-01 00:00:00,2022-07-01 23:59:59]")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @NotNull(message = "开始时间不能为空")
    private LocalDateTime[] createTime;

}
