package com.jasolar.mis.module.system;

import cn.torna.swaggerplugin.SwaggerPlugin;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 项目的启动类
 *
 * @author zhaohuang
 */
@EnableScheduling
@SpringBootApplication
public class SystemServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(SystemServerApplication.class, args);
        SwaggerPlugin.pushDoc();
    }

}

