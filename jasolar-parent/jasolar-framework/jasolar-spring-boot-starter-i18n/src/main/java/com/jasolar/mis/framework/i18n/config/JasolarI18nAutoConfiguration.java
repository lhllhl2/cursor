package com.jasolar.mis.framework.i18n.config;

import java.nio.charset.StandardCharsets;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.i18n.AcceptHeaderLocaleResolver;

import com.jasolar.mis.framework.i18n.I18nUtils;
// import com.jasolar.mis.framework.i18n.core.NacosMessageConfig;
// import com.jasolar.mis.framework.i18n.core.NacosResourceBundleMessageSource;

import jakarta.annotation.Resource;

/**
 * 国际化自动配置
 */
@AutoConfiguration
@ConditionalOnProperty(prefix = "jasolar.i18n", value = "enable", matchIfMissing = true)
// 允许使用 jasolar.i18n.enable=false 禁用多国际化配置
@EnableConfigurationProperties(I18nProperties.class)
// @Import({ NacosMessageConfig.class })
public class JasolarI18nAutoConfiguration {

    @Resource
    I18nProperties props;

    @Primary
    @Bean("messageSource")
    @ConditionalOnMissingBean(MessageSource.class)
    public ReloadableResourceBundleMessageSource messageSource() {
        ReloadableResourceBundleMessageSource messageSource = new ReloadableResourceBundleMessageSource();
        messageSource.setDefaultEncoding(StandardCharsets.UTF_8.name());
        messageSource.setCacheMillis(1000);
        messageSource.addBasenames("classpath:i18n/messages");
        messageSource.setDefaultLocale(props.getDefaultLocale());
        return messageSource;
    }

    @Bean
    public LocaleResolver localeResolver() {
        AcceptHeaderLocaleResolver localeResolver = new AcceptHeaderLocaleResolver();
        localeResolver.setDefaultLocale(props.getDefaultLocale());
        return localeResolver;
    }

    // @Bean
    // public I18nSerializerModifier i18nSerializerModifier(I18nUtils i18nUtil) {
    // return new I18nSerializerModifier(i18nUtil);
    // }

    @Bean
    public I18nUtils i18nUtil(MessageSource messageSource) {
        return new I18nUtils(messageSource);
    }

}
