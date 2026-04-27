package com.jasolar.mis.module.system.controller.admin.role.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

/**
 * Description:
 * Author : Zhou Hai
 * Date : 24/07/2025 17:30
 * Version : 1.0
 */
@Schema(description = "角色新增参数")
@Data
public class RoleSaveVo {

    @Schema(description = "角色名称")
    @NotBlank(message = "角色名称不能为空")
    private String name;

    @Schema(description = "角色编码")
    @NotBlank(message = "角色编码不能为空")
    private String code;


    @Schema(description = "角色状态")
    @NotBlank(message = "角色状态")
    private String status;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "菜单ids")
    private List<Long> menuIds;

    @Schema(description = "用户组ids")
    private List<Long> groupIds;



}
