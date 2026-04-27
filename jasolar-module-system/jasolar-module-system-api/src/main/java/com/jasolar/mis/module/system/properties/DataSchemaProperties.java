package com.jasolar.mis.module.system.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Description:
 * Author : Zhou Hai
 * Date : 17/12/2025 14:12
 * Version : 1.0
 */
@Data
@Configuration
@ConfigurationProperties("schemas")
public class DataSchemaProperties {

    private String dataIntegration;

}
