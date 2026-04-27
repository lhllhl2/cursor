package com.jasolar.mis.framework.apilog.config;

import java.util.HashSet;
import java.util.Set;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.jasolar.mis.framework.apilog.core.filter.ApiAccessLogFilter;
import com.jasolar.mis.framework.apilog.core.interceptor.ApiAccessLogInterceptor;
import com.jasolar.mis.framework.common.enums.WebFilterOrderEnum;
import com.jasolar.mis.framework.web.config.JasolarWebAutoConfiguration;
import com.jasolar.mis.framework.web.config.WebProperties;
import com.jasolar.mis.module.log.api.ApiAccessLogApi;

import jakarta.servlet.Filter;

@AutoConfiguration(after = JasolarWebAutoConfiguration.class)
public class JasolarApiLogAutoConfiguration implements WebMvcConfigurer {

    @Value("${jasolar.debug:false}")
    private boolean debug;

    /** 不需要记录日志的接口地址，主要用于排除查询接口 */
    @Value("${jasolar.access-log.excludes:/**/*page,/**/list*,/**/get*}")
    private Set<String> excludes = new HashSet<>();

    /**
     * 创建 ApiAccessLogFilter Bean，记录 API 请求日志
     * 配置fiifoxconn.access-log.enable=false 禁用访问日志
     */
    @Bean
    @ConditionalOnProperty(prefix = "jasolar.access-log", value = "enable", matchIfMissing = true)
    public FilterRegistrationBean<ApiAccessLogFilter> apiAccessLogFilter(WebProperties webProperties,
            @Value("${spring.application.name}") String applicationName, ApiAccessLogApi apiAccessLogApi) {
        ApiAccessLogFilter filter = new ApiAccessLogFilter(webProperties, applicationName, apiAccessLogApi);
        filter.setDebug(debug);
        filter.setExcludes(excludes);
        return createFilterBean(filter, WebFilterOrderEnum.API_ACCESS_LOG_FILTER);
    }

    private static <T extends Filter> FilterRegistrationBean<T> createFilterBean(T filter, Integer order) {
        FilterRegistrationBean<T> bean = new FilterRegistrationBean<>(filter);
        bean.setOrder(order);
        return bean;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new ApiAccessLogInterceptor());
    }

}
