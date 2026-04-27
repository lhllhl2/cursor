package com.jasolar.mis.framework.datapermission.config;

import static com.jasolar.mis.framework.common.enums.WebFilterOrderEnum.TENANT_CONTEXT_FILTER;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;

import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.DataPermissionInterceptor;
import com.jasolar.mis.framework.datapermission.core.aop.DataPermissionAnnotationAdvisor;
import com.jasolar.mis.framework.datapermission.core.rpc.DataPermissionWebFilter;
import com.jasolar.mis.framework.datapermission.core.rule.DataPermissionRule;
import com.jasolar.mis.framework.datapermission.core.rule.DataPermissionRuleFactory;
import com.jasolar.mis.framework.datapermission.core.rule.DataPermissionRuleFactoryImpl;
import com.jasolar.mis.framework.datapermission.core.rule.DataPermissionRuleHandler;
import com.jasolar.mis.framework.mybatis.core.util.MyBatisUtils;

import lombok.extern.slf4j.Slf4j;

/**
 * 数据权限的自动配置类
 *
 * @author zhaohuang
 */
@AutoConfiguration
@Slf4j
public class JasolarDataPermissionAutoConfiguration {
    @Bean
    public DataPermissionRuleFactory dataPermissionRuleFactory(List<DataPermissionRule> rules) {
        return new DataPermissionRuleFactoryImpl(rules);
    }

    @Bean
    public DataPermissionRuleHandler dataPermissionRuleHandler(MybatisPlusInterceptor interceptor, DataPermissionRuleFactory ruleFactory,
            @Value("${jasolar.permission.data.enabled:false}") boolean enabled) {
        log.info("数据权限开关: {}", enabled);
        // 创建 DataPermissionInterceptor 拦截器
        DataPermissionRuleHandler handler = new DataPermissionRuleHandler(ruleFactory, enabled);
        DataPermissionInterceptor inner = new DataPermissionInterceptor(handler);
        // 添加到 interceptor 中
        // 需要加在首个，主要是为了在分页插件前面。这个是 MyBatis Plus 的规定
        MyBatisUtils.addInterceptor(interceptor, inner, 0);
        return handler;
    }

    @Bean
    public DataPermissionAnnotationAdvisor dataPermissionAnnotationAdvisor() {
        return new DataPermissionAnnotationAdvisor();
    }

    @Bean
    public FilterRegistrationBean<DataPermissionWebFilter> dataPermissionRpcFilter() {
        FilterRegistrationBean<DataPermissionWebFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(new DataPermissionWebFilter());
        registrationBean.setOrder(TENANT_CONTEXT_FILTER - 1); // 顺序没有绝对的要求，在租户 Filter 前面稳妥点
        return registrationBean;
    }

}
