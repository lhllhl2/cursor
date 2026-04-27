package com.jasolar.mis.module.system.controller.admin.user.vo.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Schema(description = "管理后台 - 用户精简信息 Response VO")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserSimpleRespVO {

    @Schema(description = "用户编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long id;

    @Schema(description = "登录账号", requiredMode = Schema.RequiredMode.REQUIRED, example = "jasolar")
    private String username;

    @Schema(description = "用户昵称", requiredMode = Schema.RequiredMode.REQUIRED, example = "jasolar")
    private String nickname;

    @Schema(description = "部门ID", example = "我是一个用户")
    private Long deptId;

    @Schema(description = "部门Code")
    private String deptCode;

    @Schema(description = "部门名称", example = "IT 部")
    private String deptName;

    @Schema(description = "拼音")
    private String pinyin;

    @Schema(description = "英文名称")
    private String englishName;

    @Schema(description = "法人编码")
    private String legalCode;

    @Schema(description = "法人名称")
    private String legalName;

    @Schema(description = "用户邮箱")
    private String email;
    @Schema(description = "手机号码")
    private String mobile;

}
