package com.jasolar.mis.framework.ids.config;

import org.redisson.api.RedissonClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.jasolar.mis.framework.ids.generator.DefaultGenerator;
import com.jasolar.mis.framework.ids.sn.SerialNumberProvider;
import com.jasolar.mis.module.infra.api.sn.DocumentSnApi;

@Configuration
@EnableFeignClients(clients = { DocumentSnApi.class })
public class IdsAutoConfiguration {

    // 可以根据需要注入 MyProperties
    public IdsAutoConfiguration() {
        System.out.println("IdsAutoConfiguration initialized without properties.");
    }

    @Bean
    public DefaultGenerator defaultGenerator() {
        return new DefaultGenerator();
    }

    @Bean
    public SerialNumberProvider serialNumberProvider(RedissonClient redissonClient, DocumentSnApi documentSnApi) {
        return new SerialNumberProvider(redissonClient, documentSnApi);
    }
}