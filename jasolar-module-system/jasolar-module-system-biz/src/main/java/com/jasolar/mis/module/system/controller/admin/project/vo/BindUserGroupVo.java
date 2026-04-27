package com.jasolar.mis.module.system.controller.admin.project.vo;

import com.jasolar.mis.module.system.enums.ImportModeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

/**
 * Description:
 * Author : Zhou Hai
 * Date : 30/12/2025 14:51
 * Version : 1.0
 */
@Schema(description = "管理后台 - 项目绑定用户组 Request VO")
@Data
public class BindUserGroupVo {

    @Schema(description = "项目ID列表", requiredMode = Schema.RequiredMode.REQUIRED, example = "[1, 2, 3]")
    @NotEmpty(message = "项目ID列表不能为空")
    private List<Long> projectIds;

    @Schema(description = "用户组ID列表", requiredMode = Schema.RequiredMode.REQUIRED, example = "[1, 2, 3]")
    @NotEmpty(message = "用户组ID列表不能为空")
    private List<Long> userGroupIds;

    @Schema(description = "导入模式，APPEND：追加模式（只插入不存在的记录），OVERWRITE：覆盖模式（先删除再插入），默认为OVERWRITE", example = "APPEND")
    private ImportModeEnum importMode;

}
