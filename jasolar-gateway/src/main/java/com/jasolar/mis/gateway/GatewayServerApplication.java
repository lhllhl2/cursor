package com.jasolar.mis.gateway;

import com.ulisesbocchio.jasyptspringboot.annotation.EnableEncryptableProperties;
import org.springframework.boot.SpringApplication;
//import org.springframework.boot.actuate.autoconfigure.metrics.MetricsAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

import com.jasolar.mis.framework.common.security.SecurityProperties;
import org.springframework.cloud.openfeign.EnableFeignClients;

@EnableEncryptableProperties
@SpringBootApplication(scanBasePackages ={"com.jasolar.mis.gateway"})
@EnableConfigurationProperties(SecurityProperties.class)
@EnableFeignClients(basePackages = "com.jasolar.mis.module.system")
public class GatewayServerApplication {

    public static void main(String[] args) {
        // 启动 Spring Boot 应用
        SpringApplication.run(GatewayServerApplication.class, args);
    }

}
