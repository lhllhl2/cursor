package com.jasolar.mis.module.system.controller.admin.permission.vo.interfaceResource;

import com.jasolar.mis.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

import static com.jasolar.mis.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - 菜单接口关联分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class MenuInterfacePageReqVO extends PageParam {

    @Schema(description = "菜单编号", example = "23135")
    private Long menuId;

    @Schema(description = "接口资源编号", example = "32725")
    private Long interfaceId;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;


}