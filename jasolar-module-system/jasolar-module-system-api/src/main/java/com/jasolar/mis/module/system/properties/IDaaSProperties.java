package com.jasolar.mis.module.system.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Description:
 * Author : Zhou Hai
 * Date : 18/07/2025 11:21
 * Version : 1.0
 */
@Data
@Configuration
@ConfigurationProperties("idaas")
public class IDaaSProperties {

    private String init;

    private String clientId;

    /**
     * provides by IAM
     */
    private String clientSecret;


    private String appId;


    private SsoProperties sso;


}
