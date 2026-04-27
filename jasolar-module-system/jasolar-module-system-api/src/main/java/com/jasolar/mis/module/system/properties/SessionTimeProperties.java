package com.jasolar.mis.module.system.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Description:
 * Author : Zhou Hai
 * Date : 22/07/2025 10:10
 * Version : 1.0
 */
@Data
@Configuration
@ConfigurationProperties("jasolar-session")
public class SessionTimeProperties {

    // 小时
    private Integer expirePeriod;


    private String type;

}
