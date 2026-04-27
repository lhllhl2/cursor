package com.jasolar.mis.module.system.oauth;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

/**
 * Description:
 * Author : Zhou Hai
 * Date : 17/07/2025 17:51
 * Version : 1.0
 */
@Data
@Builder
public class SsoLogInUrlInfo implements Serializable {

    private static final long serialVersionUID = -1328653488973233226L;

    /**
     * 重定向URL
     */
    private String redirectUri;

}
