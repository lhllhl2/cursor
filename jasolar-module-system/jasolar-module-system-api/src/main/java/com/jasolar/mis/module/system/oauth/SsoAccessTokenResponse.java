package com.jasolar.mis.module.system.oauth;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.ToString;

/**
 * Description:
 * Author : Zhou Hai
 * Date : 18/07/2025 14:00
 * Version : 1.0
 */
@ToString
@Data
public class SsoAccessTokenResponse {

    @JsonFormat(pattern = "access_token")
    private String accessToken;

    @JsonFormat(pattern = "token_type")
    private String tokenType;

    @JsonFormat(pattern = "expires_in")
    private int expiresIn;

    private String scope;

    private String jti;




}
