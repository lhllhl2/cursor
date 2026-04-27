package com.jasolar.mis.module.system.controller.admin.permission.vo;

import com.jasolar.mis.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

import static com.jasolar.mis.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - 用户快捷菜单分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class ShortcutMenuPageReqVO extends PageParam {

    @Schema(description = "用户NO")
    private String userNo;

    @Schema(description = "用户ID", example = "29523")
    private Long userId;

    @Schema(description = "菜单ID", example = "30189")
    private Long menuId;

    @Schema(description = "排序")
    private Integer sort;

    @Schema(description = "是否置顶")
    private Boolean isPinned;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

}