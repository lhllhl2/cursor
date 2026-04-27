package com.jasolar.mis.module.system.api.oauth2.dto;

import java.io.Serializable;
import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@SuppressWarnings("serial")
@Schema(description = "RPC 服务 - OAuth2 访问令牌的信息 Response DTO")
@Data
public class OAuth2AccessTokenRespDTO implements Serializable {

    @Schema(description = "访问令牌", requiredMode = Schema.RequiredMode.REQUIRED, example = "tudou")
    private String accessToken;

    @Schema(description = "刷新令牌", requiredMode = Schema.RequiredMode.REQUIRED, example = "haha")
    private String refreshToken;

    @Schema(description = "用户编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "10")
    private Long userId;
    @Schema(description = "用户账号", requiredMode = Schema.RequiredMode.REQUIRED, example = "200010")
    private String userNo;
    @Schema(description = "用户名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "张三")
    private String userName;

    @Schema(description = "用户类型，参见 UserTypeEnum 枚举", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Integer userType;

    @Schema(description = "过期时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime expiresTime;

}
