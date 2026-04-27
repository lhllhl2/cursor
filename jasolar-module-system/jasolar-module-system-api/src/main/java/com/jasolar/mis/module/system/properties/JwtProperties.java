package com.jasolar.mis.module.system.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Description:
 * Author : Zhou Hai
 * Date : 22/07/2025 11:15
 * Version : 1.0
 */
@Data
@Configuration
@ConfigurationProperties("jwt")
public class JwtProperties {


    private String tokenKey;

    private String subject;



}
