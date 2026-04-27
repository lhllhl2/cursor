/*
 * Copyright (c) 2020, @deloitte.com.cn. All rights reserved.
 */
package com.jasolar.mis.framework.rpc.config;

import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalDateTime;

import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.openfeign.FeignFormatterRegistrar;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.jasolar.mis.framework.common.util.date.DateUtils;
import com.jasolar.mis.framework.rpc.core.interceptor.LoginUserRequestInterceptor;
import com.jasolar.mis.framework.rpc.core.interceptor.TraceRequestInterceptor;
import com.jasolar.mis.framework.rpc.core.transformer.RequestUriTransformer;
import com.jasolar.mis.framework.rpc.core.transformer.ServiceUriProperties;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.date.LocalDateTimeUtil;

/**
 * Feign的自动配置.
 *
 * @author galuo
 * @date 2020-07-06 19:39
 */
@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties({ ServiceUriProperties.class })
@AutoConfigureAfter(org.springframework.cloud.openfeign.FeignAutoConfiguration.class)
public class FeignAutoConfiguration {

    /**
     * 构建微服务URL拦截替换
     * 
     * @param props
     * @return
     */
    @Bean
    public RequestUriTransformer requestUriTransformer(ServiceUriProperties props) {
        return new RequestUriTransformer(props.getUris());
    }

    @Bean
    public TraceRequestInterceptor traceRequestInterceptor() {
        return new TraceRequestInterceptor();
    }

    // @Bean
    // @ConditionalOnMissingBean(Logger.Level.class)
    // public Logger.Level feignLoggerLevel() {
    // return Logger.Level.FULL;
    // }

    @Bean
    public LoginUserRequestInterceptor loginUserRequestInterceptor() {
        return new LoginUserRequestInterceptor();
    }

    @Bean
    public FeignFormatterRegistrar feignDateFormatterRegistrar() {
        return registry -> {
            // 添加日期转换，DateConverter可以猜测传入的多种日期格式
            registry.addConverter(Date.class, String.class, date -> DateUtil.format(date, DateUtils.FORMAT_DATETIME));
            registry.addConverter(LocalDate.class, String.class, date -> LocalDateTimeUtil.format(date, DateUtils.FORMAT_DATE));
            registry.addConverter(LocalDateTime.class, String.class, date -> LocalDateTimeUtil.format(date, DateUtils.FORMAT_DATETIME));
        };
    }
}
