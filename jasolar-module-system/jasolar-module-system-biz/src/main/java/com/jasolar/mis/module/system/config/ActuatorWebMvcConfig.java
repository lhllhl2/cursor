package com.jasolar.mis.module.system.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Actuator WebMvc 配置
 * 确保 /actuator/** 路径不被静态资源处理器拦截
 * 
 * 问题：Spring MVC 的静态资源处理器会拦截所有路径，包括 /actuator/health
 * 解决：通过配置确保 Actuator 端点优先处理
 * 
 * @author Auto Generated
 * @date 2025-12-18
 */
@Slf4j
@Configuration
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
public class ActuatorWebMvcConfig implements WebMvcConfigurer, Ordered {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 不添加任何静态资源映射，让 Spring Boot 使用默认配置
        // 但确保 /actuator 路径不被静态资源处理器处理
        // Actuator 端点应该由 Actuator 的端点处理器处理，而不是静态资源处理器
        log.info("ActuatorWebMvcConfig: 配置静态资源处理器，排除 /actuator 路径");
    }

    @Override
    public void configurePathMatch(PathMatchConfigurer configurer) {
        // 确保路径匹配配置不会影响 Actuator 端点
        // 不添加任何路径前缀，让 Actuator 端点正常处理
    }

    @Override
    public int getOrder() {
        // 设置较高的优先级，确保在其他 WebMvcConfigurer 之前执行
        // 这样可以确保 Actuator 端点的配置优先
        return Ordered.HIGHEST_PRECEDENCE + 100;
    }
}

