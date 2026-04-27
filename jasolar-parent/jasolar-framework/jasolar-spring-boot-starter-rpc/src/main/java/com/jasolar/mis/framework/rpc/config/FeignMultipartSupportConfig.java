package com.jasolar.mis.framework.rpc.config;

import feign.codec.Encoder;
import feign.form.spring.SpringFormEncoder;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.cloud.openfeign.support.SpringEncoder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** *
 * @Title:
 * @Author: yiptan
 * @Date: 22/05/2025 19:50
 * @Description:   Feign 多部分文件上传支持
 */
@Configuration
public class FeignMultipartSupportConfig {
    
    @Autowired
    private ObjectFactory<HttpMessageConverters> messageConverters;
    
    @Bean
    public Encoder feignFormEncoder() {
        return new SpringFormEncoder(new SpringEncoder(messageConverters));
    }
}