package com.jasolar.mis.framework.i18n.config;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Data;

/**
 * 国际化配置类
 */
@Data
@ConfigurationProperties(prefix = "jasolar.i18n")
public class I18nProperties {
    /** 是否开启 */
    private Boolean enable = true;

    /** 是否开启Nacos加载配置 */
    private Boolean nacos = true;

    /**
     * 配置分割符, 从nacos读取配置的dataId格式为: applicationName_messages_zh-CN. locale不使用此分隔符,而是使用{@link Locale#toLanguageTag()}方法获取
     */
    private String split = "_";

    /** 配置的基础名称 */
    private List<String> basenames = Arrays.asList("messages", "errors", "dicts");

    /** 默认语言, 默认为zh-CN */
    private Locale defaultLocale = Locale.SIMPLIFIED_CHINESE;

    /** 语言清单设置. 默认支持zh-CN, zh-TW, en-US, vi-VN四种语言 */
    private List<Locale> locales = Arrays.asList(Locale.SIMPLIFIED_CHINESE, Locale.TRADITIONAL_CHINESE, Locale.US,
            Locale.forLanguageTag("vi-VN"));
    /**
     * 文件格式
     */
    private String configFormat = ".properties";

}
