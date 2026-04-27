package com.jasolar.mis.module.system.controller.admin.usergroup.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * Description:
 * Author : Zhou Hai
 * Date : 24/07/2025 15:36
 * Version : 1.0
 */
@Schema(description = "用户-用户组关联关系参数")
@Data
public class GroupUserSaveVo {

    @Schema(description = "用户组id")
    private Long groupId;

    @Schema(description = "类型")
    private String type;

    @Schema(description = "用户ids")
    private List<Long> userIds;

}
