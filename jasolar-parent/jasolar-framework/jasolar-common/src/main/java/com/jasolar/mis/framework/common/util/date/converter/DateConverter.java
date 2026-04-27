package com.jasolar.mis.framework.common.util.date.converter;

import java.text.ParseException;
import java.util.Arrays;
import java.util.Date;
import java.util.Set;

import org.apache.commons.lang3.time.DateUtils;
import org.springframework.core.convert.converter.Converter;

import jakarta.annotation.Nullable;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * 字符串转日期
 * 
 * @author galuo
 * @date 2025-04-15 12:30
 *
 */
@Slf4j
public class DateConverter implements Converter<String, Date> {

    /** 默认使用的日期格式 */
    public static final String[] DEFAULT_FORMATS = { com.jasolar.mis.framework.common.util.date.DateUtils.FORMAT_DATETIME,
            com.jasolar.mis.framework.common.util.date.DateUtils.FORMAT_MILLIS,
            com.jasolar.mis.framework.common.util.date.DateUtils.FORMAT_DATE,
            com.jasolar.mis.framework.common.util.date.DateUtils.FORMAT_MINUTES,
            com.jasolar.mis.framework.common.util.date.DateUtils.FORMAT_DATETIME_PURE,
            com.jasolar.mis.framework.common.util.date.DateUtils.FORMAT_DATE_PURE };

    /** 默认对象 */
    public static final DateConverter DEFAULT = new DateConverter();

    /** 使用的日期格式 */
    @Getter
    @Setter
    private String[] formats = DEFAULT_FORMATS;

    @Override
    public Date convert(String source) {
        try {
            return DateUtils.parseDate(source, formats);
        } catch (ParseException ex) {
            log.warn(source + "无法转换为Date", ex);
        }
        return null;
    }

    /**
     * 尝试转换为日期
     * 
     * @param source 日期字符串
     * @param ignores 不需要处理的格式, 可以为null
     * @return 转换的日期, 转换失败返回NULL
     */
    public Date convert(String source, @Nullable Set<String> ignores) {
        if (ignores == null || ignores.isEmpty()) {
            return convert(source);
        }
        
        String[] formats = Arrays.stream(this.formats).filter(f -> !ignores.contains(f)).toArray(String[]::new);
        try {
            return DateUtils.parseDate(source, formats);
        } catch (ParseException ex) {
            log.warn(source + "无法转换为Date", ex);
        }
        return null;
    }
}
