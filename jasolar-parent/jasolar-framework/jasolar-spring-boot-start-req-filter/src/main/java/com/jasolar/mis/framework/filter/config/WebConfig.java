package com.jasolar.mis.framework.filter.config;

import com.jasolar.mis.framework.filter.intercepter.AuthInterceptor;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Description:
 * Author : Zhou Hai
 * Date : 28/07/2025 9:45
 * Version : 1.0
 */
@AutoConfiguration
public class WebConfig implements WebMvcConfigurer {


    @Override
    public void addInterceptors(InterceptorRegistry registry) {

        registry.addInterceptor(new AuthInterceptor());

    }
}
