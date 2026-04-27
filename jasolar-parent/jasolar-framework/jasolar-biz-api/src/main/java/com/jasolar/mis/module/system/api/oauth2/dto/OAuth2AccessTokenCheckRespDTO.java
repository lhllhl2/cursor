package com.jasolar.mis.module.system.api.oauth2.dto;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

import com.jasolar.mis.framework.common.security.LoginAdminUser;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@SuppressWarnings("serial")
@Schema(description = "RPC 服务 - OAuth2 访问令牌的校验 Response DTO")
@Data
public class OAuth2AccessTokenCheckRespDTO implements Serializable {

    @Schema(description = "用户编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "10")
    private Long userId;
    @Schema(description = "用户账号", requiredMode = Schema.RequiredMode.REQUIRED, example = "200010")
    private String userNo;
    @Schema(description = "用户名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "张三")
    private String userName;

    @Schema(description = "用户类型，参见 UserTypeEnum 枚举", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Integer userType;

    @Schema(description = "用户信息", example = "{\"nickname\": \"fiifoxconn\"}")
    private LoginAdminUser userInfo;

    @Schema(description = "租户编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long tenantId;

    @Schema(description = "授权范围的数组", example = "user_info")
    private List<String> scopes;

    @Schema(description = "过期时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime expiresTime;

}
