package com.jasolar.mis.framework.api.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * API扫描器配置属性
 */
@Data
@ConfigurationProperties(prefix = "jasolar.scanner")
public class ApiScannerProperties {

    /**
     * 是否启用API接口扫描
     */
    private boolean enabled = false;

    /**
     * 是否排除Feign接口
     */
    private boolean excludeFeign = true;

    /**
     * 是否自动导入数据库
     */
    private boolean autoImport = false;
} 