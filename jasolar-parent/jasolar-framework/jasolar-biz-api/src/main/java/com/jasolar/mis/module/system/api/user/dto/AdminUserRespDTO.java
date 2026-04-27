package com.jasolar.mis.module.system.api.user.dto;

import java.util.Set;

import com.fhs.core.trans.vo.VO;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "RPC 服务 - Admin 用户 Response DTO")
@Data
public class AdminUserRespDTO implements VO {

    @Schema(description = "用户 ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long id;

    @Schema(description = "用户账号", requiredMode = Schema.RequiredMode.REQUIRED, example = "员工号")
    private String username;

    @Schema(description = "用户昵称", requiredMode = Schema.RequiredMode.REQUIRED, example = "小王")
    private String nickname;

    @Schema(description = "帐号状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Integer status; // 参见 CommonStatusEnum 枚举

    @Schema(description = "部门编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long deptId;

    @Schema(description = "岗位编号数组", requiredMode = Schema.RequiredMode.REQUIRED, example = "[1, 3]")
    private Set<Long> postIds;

    @Schema(description = "手机号码", requiredMode = Schema.RequiredMode.REQUIRED, example = "15601691300")
    private String mobile;

    @Schema(description = "用户头像", requiredMode = Schema.RequiredMode.REQUIRED, example = "http://www.fii.com/1.png")
    private String avatar;

    @Schema(description = "拼音")
    private String pinyin;

    @Schema(description = "英文名称", example = "xshe")
    private String englishName;

    @Schema(description = "法人编码")
    private String legalCode;

    @Schema(description = "法人名称;冗余字段", example = "赵六")
    private String legalName;

    /** 部门代码,即费用代码 */
    private String deptCode;

    /** 部门名称 */
    private String deptName;

    /** 事业群CODE */
    private String businessGroupCode;
    /** 事业群名称;冗余字段 */
    private String businessGroupName;
    /** 事业处CODE */
    private String businessUnitCode;
    /** 事业处名称;冗余字段 */
    private String businessUnitName;

    @Schema(description = "邮件")
    private String email;

}
