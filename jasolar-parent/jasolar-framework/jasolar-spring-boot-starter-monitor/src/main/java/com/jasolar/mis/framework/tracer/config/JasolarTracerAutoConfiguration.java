package com.jasolar.mis.framework.tracer.config;

/**
 * Tracer 配置类
 *
 * @author zhaohuang
 */
// @AutoConfiguration
// @ConditionalOnClass(value = { BizTraceAspect.class }, name = "jakarta.servlet.Filter")
// @EnableConfigurationProperties(TracerProperties.class)
// @ConditionalOnProperty(prefix = "jasolar.tracer", value = "enable", matchIfMissing = true)
public class JasolarTracerAutoConfiguration {

    // /**
    // * 创建 TraceFilter 过滤器，响应 header 设置 traceId
    // */
    // @Bean
    // public FilterRegistrationBean<TraceFilter> traceFilter() {
    // FilterRegistrationBean<TraceFilter> registrationBean = new FilterRegistrationBean<>();
    // registrationBean.setFilter(new TraceFilter());
    // registrationBean.setOrder(WebFilterOrderEnum.TRACE_FILTER);
    // return registrationBean;
    // }

}
