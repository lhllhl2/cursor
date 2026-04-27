package com.jasolar.mis.module.system.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * @Description SSO单点登录相关配置项
 * @Author Kevin Jia
 * @Date 12/11/2022 10:40
 */
@Data
@Configuration
@ConfigurationProperties("idaas.sso")
public class SsoProperties {

    /**
     * Authorize URL
     */
    private String authorizeUri;

    /**
     * Get Token URL
     */
    private String getTokenUri;

    /**
     * Get Userinfo URL
     */
    private String getUserinfoUri;

    /**
     * Logout URL
     */
    private String logoutUri;

    /**
     * The callback address, provided by the application system, must not contain special symbols
     */
    private String ssoCallBackUri;

    /**
     * The callback address when getting token, provided by the application system, must not contain special symbols
     */
    private String tokenCallBackUri;

    /**
     * Authorization mode, where the fixed value is "code"
     */
    private String responseType;

    /**
     * The permission scope of the application is fixed value "UserProfile.me"
     */
    private String scope;

    /**
     * Arbitrary value here, e.g."xyz"
     */
    private String state;

    /**
     * Issuance type, where the fixed value is "authorization_code"
     */
    private String grantType;

    /**
     * portal redirect uri
     */
    private String portalRedirectUri;




}
