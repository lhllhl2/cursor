package com.jasolar.mis.framework.rpc.core.transformer;

import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Data;

/**
 * 用于配置每个服务对一个的地址参数
 */
@ConfigurationProperties(prefix = "jasolar.feign")
@Data
public class ServiceUriProperties {

    /** 未特殊配置时指定的默认配置 */
    public static final String DEFAULT = "default";

    /** 配置为#号表示不做拦截 */
    public static final String NONE = "none";

    /** 所有需要替换的服务地址配置 */
    private List<ServiceUri> uris;

    /**
     * 每个服务的URI配置
     */
    @Data
    public static class ServiceUri {

        /** 服务名 */
        private String name;

        /** 替换的地址,必须是绝对地址, http(s)://xxx/xx */
        private String uri;

    }
}
