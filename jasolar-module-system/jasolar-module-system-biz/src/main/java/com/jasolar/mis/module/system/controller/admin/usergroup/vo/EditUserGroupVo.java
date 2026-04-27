package com.jasolar.mis.module.system.controller.admin.usergroup.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * Description:
 * Author : Zhou Hai
 * Date : 24/07/2025 15:36
 * Version : 1.0
 */
@Schema(description = "用户-用户组关联关系参数")
@Data
public class EditUserGroupVo {

    @Schema(description = "用户组id")
    private Long id;

    @Schema(description = "用户组名称")
    private String name;

    @Schema(description = "remark")
    private String remark;

}
