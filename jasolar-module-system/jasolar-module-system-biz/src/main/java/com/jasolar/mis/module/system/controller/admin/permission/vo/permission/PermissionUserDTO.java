package com.jasolar.mis.module.system.controller.admin.permission.vo.permission;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 数据权限查询人员维度权限数据
 *
 * @author zhangj
 * @date 2025-07-08 15:11
 */
@Data
public class PermissionUserDTO {

    @Schema(description = "人员工号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotBlank(message = "人员工号不能为空")
    private String userNo;

    @Schema(description = "人员姓名")
    private String userName;
}
