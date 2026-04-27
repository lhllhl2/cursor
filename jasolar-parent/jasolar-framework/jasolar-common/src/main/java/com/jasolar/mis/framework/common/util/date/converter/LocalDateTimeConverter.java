package com.jasolar.mis.framework.common.util.date.converter;

import java.time.LocalDateTime;
import java.util.Set;

import org.springframework.core.convert.converter.Converter;

import cn.hutool.core.date.LocalDateTimeUtil;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * 字符串转换为LocalDateTime
 * 
 * @author galuo
 * @date 2025-04-17 19:43
 *
 */
@Slf4j
public class LocalDateTimeConverter implements Converter<String, LocalDateTime> {

    /** 默认对象 */
    public static final LocalDateTimeConverter DEFAULT = new LocalDateTimeConverter();

    /** 使用的日期格式 */
    @Getter
    @Setter
    private String[] formats = DateConverter.DEFAULT_FORMATS;

    @Override
    public LocalDateTime convert(String source) {
        return this.convert(source, null);
    }

    /**
     * 尝试转换为日期
     * 
     * @param source 日期字符串
     * @param ignores 不需要处理的格式, 可以为null
     * @return 转换的日期, 转换失败返回NULL
     */
    public LocalDateTime convert(String source, Set<String> ignores) {
        for (String format : formats) {
            if (ignores != null && ignores.contains(source)) {
                continue;
            }
            try {
                return LocalDateTimeUtil.parse(source, format);
            } catch (Exception ignore) {
                // 忽略异常
            }
        }
        log.warn("{} 无法转换为LocalDateTime", source);
        return null;
    }
}