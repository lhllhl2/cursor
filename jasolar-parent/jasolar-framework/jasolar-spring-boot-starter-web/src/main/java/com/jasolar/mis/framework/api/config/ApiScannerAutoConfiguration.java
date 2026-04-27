package com.jasolar.mis.framework.api.config;

import com.jasolar.mis.framework.api.ApiScannerController;
import com.jasolar.mis.framework.api.ApiScannerUtil;
import com.jasolar.mis.framework.api.InterfaceResourceCaller;
import com.jasolar.mis.framework.api.SimpleInterfaceResourceCaller;
import com.jasolar.mis.module.system.api.permission.InterfaceResourceApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

/**
 * API扫描自动配置类
 * 用于启用API接口扫描和资源导入功能
 */
@AutoConfiguration
@ConditionalOnProperty(prefix = "jasolar.scanner", name = "enabled", matchIfMissing = false)
@EnableConfigurationProperties(ApiScannerProperties.class)
@EnableFeignClients(clients = {InterfaceResourceApi.class})
public class ApiScannerAutoConfiguration {


    @Value("${spring.application.name:unknown}")
    private String applicationName = "unknown";

    /**
     * 可选：如果没有自定义的资源调用器，则使用默认的SimpleInterfaceResourceCaller
     */
    @Bean
    @ConditionalOnMissingBean(InterfaceResourceCaller.class)
    public InterfaceResourceCaller interfaceResourceCaller(InterfaceResourceApi interfaceResourceApi) {
        return new SimpleInterfaceResourceCaller(interfaceResourceApi);
    }


    /**
     * 配置API扫描工具
     */
    @Bean
    public ApiScannerUtil apiScannerUtil(
            RequestMappingHandlerMapping requestMappingHandlerMapping,
            InterfaceResourceCaller resourceCaller,
            ApiScannerProperties properties) {
        return new ApiScannerUtil(
                requestMappingHandlerMapping,
                resourceCaller,
                properties,
                applicationName);
    }


    /**
     * 可选：如果有API扫描控制器，也在这里配置
     */
    @Bean
    public ApiScannerController apiScannerController(ApiScannerUtil apiScannerUtil, InterfaceResourceCaller interfaceResourceCaller) {
        return new ApiScannerController(apiScannerUtil, interfaceResourceCaller);
    }
} 