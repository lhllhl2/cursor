package com.jasolar.mis.module.system.controller.admin.usergroup.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Data;

import java.util.Set;

/**
 * Description:
 * Author : Zhou Hai
 * Date : 23/07/2025 10:51
 * Version : 1.0
 */
@Schema(description = "后台管理 - 用户组新增")
@Data
@Builder
public class UserGroupAddVo {

    @Schema(description = "用户组名称",requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "用户组名称不能为空")
    private String name;

    @Schema(description = "菜单类型",requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "菜单类型不能为空")
    private String type;

    @Schema(description = "说明")
    private String remark;

    @Schema(description = "用户ids")
    private Set<Long> userIds;


}
