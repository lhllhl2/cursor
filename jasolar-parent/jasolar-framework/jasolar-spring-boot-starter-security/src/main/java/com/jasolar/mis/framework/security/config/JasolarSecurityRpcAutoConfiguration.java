package com.jasolar.mis.framework.security.config;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.cloud.openfeign.EnableFeignClients;

import com.jasolar.mis.module.system.api.oauth2.OAuth2TokenApi;

/**
 * Security 使用到 Feign 的配置项
 *
 * @author zhaohuang
 */
@AutoConfiguration
@EnableFeignClients(clients = { OAuth2TokenApi.class })
public class JasolarSecurityRpcAutoConfiguration {

}
