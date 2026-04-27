package com.jasolar.mis.module.system.oauth;

import lombok.Data;
import lombok.experimental.SuperBuilder;

/**
 * Description:
 * Author : Zhou Hai
 * Date : 18/07/2025 13:42
 * Version : 1.0
 */
@Data
@SuperBuilder
public class TokenResponse {

    private String token;

    private String type;

    private Integer expiresIn;

}
