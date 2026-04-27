package com.jasolar.mis.framework.datapermission.config;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;

import com.jasolar.mis.framework.datapermission.core.rpc.DataPermissionInterceptor;

/**
 * 数据权限针对 RPC 的自动配置类
 *
 * @author zhaohuang
 */
@AutoConfiguration
@ConditionalOnClass(name = "feign.RequestInterceptor")
public class JasolarDataPermissionRpcAutoConfiguration {

    @Bean
    public DataPermissionInterceptor dataPermissionRequestInterceptor() {
        return new DataPermissionInterceptor();
    }

}
