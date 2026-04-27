package com.jasolar.mis.module.system.controller.admin.role.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * Description:
 * Author : Zhou Hai
 * Date : 25/07/2025 13:41
 * Version : 1.0
 */
@Schema(description = "角色修改参数")
@Data
public class RoleEditVo extends RoleSaveVo{

    @Schema(description = "主键")
    private Long id;

}
