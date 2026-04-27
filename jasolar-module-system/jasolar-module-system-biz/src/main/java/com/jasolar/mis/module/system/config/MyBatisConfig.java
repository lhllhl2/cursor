package com.jasolar.mis.module.system.config;

import com.baomidou.mybatisplus.autoconfigure.ConfigurationCustomizer;
import com.jasolar.mis.module.system.properties.DataSchemaProperties;
import io.swagger.v3.oas.annotations.media.SchemaProperties;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ContextRefreshedEvent;

import java.util.Collection;
import java.util.Properties;

/**
 * MyBatis 配置类
 * 确保 OracleSchemaInterceptor 被正确注册
 *
 * @author Auto Generated
 * @date 2025-01-XX
 */
@Slf4j
@Configuration
public class MyBatisConfig implements ApplicationListener<ContextRefreshedEvent> {

    @Autowired
    private OracleSchemaInterceptor oracleSchemaInterceptor;

    /**
     * 确保拦截器被注册到所有 SqlSessionFactory
     * 在上下文刷新完成后注册，确保 SqlSessionFactory 已经创建
     * 通过 ApplicationContext 获取 SqlSessionFactory，避免循环依赖
     */
    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        // 只处理根上下文的事件，避免重复处理
        ApplicationContext applicationContext = event.getApplicationContext();
        if (applicationContext.getParent() != null) {
            return; // 忽略子上下文事件
        }
        
        log.info("========== 开始注册 OracleSchemaInterceptor ==========");
        Collection<SqlSessionFactory> sqlSessionFactories = applicationContext.getBeansOfType(SqlSessionFactory.class).values();
        
        if (sqlSessionFactories != null && !sqlSessionFactories.isEmpty()) {
            for (SqlSessionFactory sqlSessionFactory : sqlSessionFactories) {
                org.apache.ibatis.session.Configuration configuration = 
                    sqlSessionFactory.getConfiguration();
                
                // 打印所有已注册的拦截器
                log.info("========== 当前已注册的拦截器数量: {} ==========", configuration.getInterceptors().size());
                configuration.getInterceptors().forEach(interceptor -> {
                    log.info("========== 已注册拦截器: {} ==========", interceptor.getClass().getName());
                });
                
                // 检查拦截器是否已存在
                boolean exists = configuration.getInterceptors().stream()
                    .anyMatch(interceptor -> interceptor instanceof OracleSchemaInterceptor);
                
                if (!exists) {
                    // 如果不存在，添加拦截器（MyBatis 会自动将其添加到拦截器链）
                    configuration.addInterceptor(oracleSchemaInterceptor);
                    log.info("========== OracleSchemaInterceptor 已注册到 SqlSessionFactory ==========");
                } else {
                    log.info("========== OracleSchemaInterceptor 已存在，跳过注册 ==========");
                }
                
                log.info("========== 注册后拦截器数量: {} ==========", configuration.getInterceptors().size());
            }
        } else {
            log.warn("========== 未找到 SqlSessionFactory，无法注册拦截器 ==========");
        }
    }


    @Bean
    public ConfigurationCustomizer myBatisConfigurationCustomizer(DataSchemaProperties dataSchemaProperties) {
        return configuration -> {
            Properties variables = new Properties();
            variables.setProperty("dataIntegration", dataSchemaProperties.getDataIntegration());
            configuration.setVariables(variables);
        };
    }

}

