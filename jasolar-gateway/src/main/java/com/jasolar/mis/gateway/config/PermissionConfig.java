package com.jasolar.mis.gateway.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.Map;

/**
 * @Description permission configuration
 * @Author qiafu
 * @Date 16/11/2022 18:38
 */
@Configuration
@ConfigurationProperties(prefix = "permission")
@RefreshScope
@Data
public class PermissionConfig {

    /**
     * 不拦截的urls
     */
    private List<String> ignoreUrls;

    /**
     * 白名单的urls(不做权限校验)
     */
    private List<String> whiteUrls;

    /**
     * 黑名单的urls
     */
    private List<String> blackUrls;

    /**
     * open-api的urls
     */
    private List<String> openUrls;


    private List<String> outerUrls;


    /**
     * SecretMap ak:sk
     */
    private Map<String,String> secretMap;

}
