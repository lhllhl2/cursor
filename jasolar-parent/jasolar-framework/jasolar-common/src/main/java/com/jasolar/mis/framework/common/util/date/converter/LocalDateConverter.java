package com.jasolar.mis.framework.common.util.date.converter;

import java.time.LocalDate;
import java.util.Set;

import org.springframework.core.convert.converter.Converter;

import cn.hutool.core.date.LocalDateTimeUtil;
import jakarta.annotation.Nullable;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * 字符串转换为LocalDate
 * 
 * @author galuo
 * @date 2025-04-15 12:44
 *
 */
@Slf4j
public class LocalDateConverter implements Converter<String, LocalDate> {
    /** 默认使用的日期格式 */
    public static final String[] DEFAULT_FORMATS = { com.jasolar.mis.framework.common.util.date.DateUtils.FORMAT_DATE,
            com.jasolar.mis.framework.common.util.date.DateUtils.FORMAT_DATE_PURE };

    /** 默认对象 */
    public static final LocalDateConverter DEFAULT = new LocalDateConverter();

    /** 使用的日期格式 */
    @Getter
    @Setter
    private String[] formats = DEFAULT_FORMATS;

    @Override
    public LocalDate convert(String source) {
        return this.convert(source, null);
    }

    /**
     * 尝试转换为日期
     * 
     * @param source 日期字符串
     * @param ignores 不需要处理的格式, 可以为null
     * @return 转换的日期, 转换失败返回NULL
     */
    public LocalDate convert(String source, @Nullable Set<String> ignores) {
        for (String format : formats) {
            if (ignores != null && ignores.contains(source)) {
                continue;
            }
            try {
                return LocalDateTimeUtil.parseDate(source, format);
            } catch (Exception ignore) {
                // 忽略异常
            }
        }
        log.warn("{} 无法转换为LocalDate", source);
        return null;
    }

}
