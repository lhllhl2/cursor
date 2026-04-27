package com.jasolar.mis.module.system.controller.admin.usergroup.vo;

import com.jasolar.mis.module.system.controller.admin.usergroup.resp.UserGroupTreeResp;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Description:
 * Author : Zhou Hai
 * Date : 24/07/2025 15:36
 * Version : 1.0
 */
@Schema(description = "用户-用户组关联关系参数")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserGroupTreeSaveVo {

    @Schema(description = "用户组id")
    private Long userId;
    private List<UserGroupTreeResp> tree;

}
