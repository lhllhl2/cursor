package com.jasolar.mis.module.system.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "erp.oracle")
public class ErpOracleProperties {

    private String url;

    private String username;

    private String password;

    private String driverClassName = "oracle.jdbc.OracleDriver";
}
