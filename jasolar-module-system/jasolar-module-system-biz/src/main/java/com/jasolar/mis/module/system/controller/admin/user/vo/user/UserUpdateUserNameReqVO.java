package com.jasolar.mis.module.system.controller.admin.user.vo.user;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Schema(description = "管理后台 - 用户修改用户名 Request VO")
@Data
public class UserUpdateUserNameReqVO {

    @Schema(description = "用户编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    @NotNull(message = "用户编号不能为空")
    private Long id;

    @Schema(description = "新用户名", requiredMode = Schema.RequiredMode.REQUIRED, example = "newUsername")
    @NotBlank(message = "用户名不能为空")
    private String userName;

} 