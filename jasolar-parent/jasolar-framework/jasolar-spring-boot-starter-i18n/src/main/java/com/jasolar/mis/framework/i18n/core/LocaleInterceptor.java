package com.jasolar.mis.framework.i18n.core;

import java.util.Locale;

import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpHeaders;
import org.springframework.web.servlet.HandlerInterceptor;

import org.apache.commons.lang3.StringUtils;

import cn.hutool.core.text.StrPool;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

/**
 * 注入语言环境.
 * 前端直接使用zh-CN的语言,直接使用默认的 org.springframework.web.servlet.i18n.AcceptHeaderLocaleResolver 即可
 *
 * @author zhahuang
 */
@Slf4j
public class LocaleInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String lang = request.getHeader(HttpHeaders.ACCEPT_LANGUAGE);
        if (StringUtils.isNotBlank(lang) && lang.contains(StrPool.UNDERLINE)) {
            // 前端一般是传zh-CN的形式, 如果传zh_CN则AcceptHeaderLocaleResolver无法解析
            Locale locale = resolveLocale(lang);
            log.info("Language is: {}, locale: {}", lang, locale);
            LocaleContextHolder.setLocale(locale);
        }
        return true;
    }

    /**
     * 解析locale
     * 
     * @param acceptLanguage 语言locale
     * @return 返回解析到的JVM支持的locale,不支持则返回默认的locale
     */
    public static Locale resolveLocale(String acceptLanguage) {
        // 从传入的语言标签创建 Locale 对象
        Locale requestLocale = Locale.forLanguageTag(acceptLanguage.replace(StrPool.UNDERLINE, StrPool.DASHED));

        // 获取 JVM 支持的所有 Locale
        Locale[] availableLocales = Locale.getAvailableLocales();

        // 查找最佳匹配的 Locale
        for (Locale locale : availableLocales) {
            if (locale.equals(requestLocale)) {
                return locale;
            }
        }
        // 如果找到了匹配项，则返回该 Locale；否则返回默认 Locale
        return Locale.getDefault();
    }
}
