package com.jasolar.mis.framework.i18n;

import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;

import com.jasolar.mis.framework.common.exception.ErrorCode;

import jakarta.annotation.Nullable;
import lombok.extern.slf4j.Slf4j;

/**
 * 国际化快捷工具
 *
 * @author zhahuang
 */
@Slf4j
public class I18nUtils {

    /** 分隔符 */
    public static final String SEPARATOR = ".";

    /** 字典的国际化前缀 */
    public static final String DICT_PREFIX = "dict" + SEPARATOR;
    /** 异常消息的国际化前缀 */
    public static final String ERR_PREFIX = "err" + SEPARATOR;

    /** 国际化消息对象,Spring Bean */
    private static MessageSource MESSAGE_SOURCE;

    public I18nUtils(MessageSource messageSource) {
        I18nUtils.MESSAGE_SOURCE = messageSource;
    }

    /**
     * 无参数的国际化消息
     * 
     * @param code 国际化编码
     * @return
     */
    public static String getMessage(String code) {
        return getMessage(code, code);
    }

    /**
     * 无参数的国际化消息
     * 
     * @param code 国际化编码
     * @param defaultMessage code不存在时的默认消息
     * @return
     */
    public static String getMessage(String code, String defaultMessage) {
        return getMessage(code, null, defaultMessage);
    }

    /**
     * 参数化的获取国际化信息
     *
     * @param code 国际化编码
     * @param args 参数
     * @return
     */
    public static String getMessage(String code, @Nullable Object[] args) {
        return getMessage(code, args, code);
    }

    /**
     * 参数化的获取国际化信息
     *
     * @param code 国际化编码
     * @param args 参数
     * @param defaultMessage code不存在时的默认消息
     * @return
     */
    public static String getMessage(String code, @Nullable Object[] args, String defaultMessage) {
        try {
            return MESSAGE_SOURCE.getMessage(code, args, defaultMessage, LocaleContextHolder.getLocale());
        } catch (IllegalArgumentException ex) {
            log.warn("获取国际化消息异常, 可能是参数与配置的消息不匹配", ex);
            return defaultMessage;
        } catch (Exception ex) {
            log.warn("获取国际化消息异常", ex);
            return defaultMessage;
        }
    }

    /**
     * 通过错误码获取国际化消息
     * 
     * @param err 错误
     * @param args 参数
     * @return
     */
    public static String getMessage(ErrorCode err, @Nullable Object[] args) {
        return getMessage(err.getCode(), args, err.getMsg());
    }

    /**
     * 组合消息的key
     * 
     * @param key1
     * @param key2
     * @param keys
     * @return 以{@link #SEPARATOR}链接起来的消息key
     */
    public static String joinKey(Object key1, Object key2, Object... keys) {
        StringBuilder buf = new StringBuilder();
        buf.append(key1).append(SEPARATOR).append(key2);
        if (keys != null) {
            for (Object k : keys) {
                buf.append(SEPARATOR).append(k);
            }
        }
        return buf.toString();
    }

}