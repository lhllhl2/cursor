package com.jasolar.mis.module.system.controller.admin.usergroup.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

/**
 * Description:
 * Author : Zhou Hai
 * Date : 23/07/2025 14:42
 * Version : 1.0
 */
@Schema(description = "主键参数")
@Data
@Builder
public class SearchRoleByUserGroupVo {

    @Schema(description = "主键")
    private Long id;

}
