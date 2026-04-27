package com.jasolar.mis.framework.gateway;

import java.util.List;

import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.client.loadbalancer.reactive.ReactorLoadBalancerExchangeFilterFunction;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jasolar.mis.framework.common.security.SecurityProperties;
import com.jasolar.mis.framework.common.util.json.JsonUtils;
import com.jasolar.mis.framework.gateway.filter.security.ApiSecurityFilter;
import com.jasolar.mis.framework.gateway.filter.security.LoginUserProvider;
import com.jasolar.mis.framework.gateway.filter.security.SignatureAuthenticationFilter;
import com.jasolar.mis.framework.gateway.filter.security.SignatureClientProvider;
import com.jasolar.mis.framework.gateway.filter.security.SignatureUserProvider;
import com.jasolar.mis.framework.gateway.filter.security.TokenAuthenticationFilter;

import cn.hutool.core.collection.CollUtil;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration(proxyBeanMethods = false)
@ComponentScan(basePackageClasses = { GatewayAutoConfiguration.class })
@EnableConfigurationProperties({ SecurityProperties.class, AuthWhitelist.class })
public class GatewayAutoConfiguration {

    @Bean
    public JsonUtils jsonUtils(List<ObjectMapper> objectMappers) {
        JsonUtils.config(objectMappers);
        JsonUtils.init(CollUtil.getFirst(objectMappers));

        log.info("[init][初始化 JsonUtils 成功]");
        return new JsonUtils();
    }

    @Bean
    @ConditionalOnMissingBean
    TokenAuthenticationFilter tokenAuthenticationFilter(SecurityProperties props, LoginUserProvider loginUserProvider,
            AuthWhitelist authWhitelist) {
        return new TokenAuthenticationFilter(props, loginUserProvider, authWhitelist);
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = "jasolar.security.signature", name = "enabled", havingValue = "true", matchIfMissing = true)
    SignatureClientProvider signatureClientProvider(RedissonClient redisson) {
        return new SignatureClientProvider(redisson);
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = "jasolar.security.signature", name = "enabled", havingValue = "true", matchIfMissing = true)
    SignatureAuthenticationFilter serverAuthenticationFilter(SecurityProperties props, SignatureUserProvider serveProvider) {
        return new SignatureAuthenticationFilter(props, serveProvider);
    }

    /**
     * 构建WebClinet对象用于接口请求
     * 
     * @param lb LoadBalancer对象
     * @param usingLoadBalancer 是否使用LoadBalancer对象, 如果此参数为false,则请求的接口地址应该配置为全地址的格式http://ip/api,一般用于本地调试.
     *     为true则只需要配置服务名:如http://system-service/rpc-api/system/oauth2/token/check
     * @return
     */
    @Bean
    public WebClient webClient(ReactorLoadBalancerExchangeFilterFunction lb,
            @Value("${jasolar.gateway.webclient.usingLoadBalancer:true}") boolean usingLoadBalancer) {
        return usingLoadBalancer ? WebClient.builder().filter(lb).build() : WebClient.builder().build();
    }

    /**
     * 根据配置决定是否启用API安全过滤器
     */
    @Bean
    @ConditionalOnProperty(prefix = "jasolar.security", name = "api-check-enable", havingValue = "true")
    public ApiSecurityFilter apiSecurityGatewayFilter(WebClient webClient, RedissonClient redisson, SecurityProperties securityProperties) {
        log.info("[init][初始化 ApiSecurityFilter 成功]");
        return new ApiSecurityFilter(webClient, redisson, securityProperties.getPermitAllUrls());
    }

}
