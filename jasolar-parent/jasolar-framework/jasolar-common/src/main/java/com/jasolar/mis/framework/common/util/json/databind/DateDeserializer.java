package com.jasolar.mis.framework.common.util.json.databind;

import java.io.IOException;
import java.text.DateFormat;
import java.util.Date;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.jasolar.mis.framework.common.util.collection.SetUtils;
import com.jasolar.mis.framework.common.util.date.DateUtils;
import com.jasolar.mis.framework.common.util.date.converter.DateConverter;

import lombok.Getter;
import lombok.Setter;

/**
 * Date类型的反序列化
 * 
 * @author galuo
 * @date 2025-04-24 12:49
 *
 */
@SuppressWarnings("serial")
public class DateDeserializer extends com.fasterxml.jackson.databind.deser.std.DateDeserializers.DateDeserializer {

    /** 默认格式转换失败后尝试进行日期转换的Converter */
    @Getter
    @Setter
    private DateConverter converter = DateConverter.DEFAULT;

    /** 常量实例 */
    public final static DateDeserializer INSTANCE = new DateDeserializer();

    /** 优先使用的默认格式 */
    protected String format;

    /** 默认构造函数, 默认使用日期格式:{@link DateUtils#FORMAT_DATETIME} */
    public DateDeserializer() {
        super();
        this.format = DateUtils.FORMAT_DATETIME;
    }

    /**
     * 指定具体的日期格式, 注意解析时只是优先使用此格式
     * 
     * @param format
     */
    public DateDeserializer(String format) {
        super();
        this.format = format;
    }

    /**
     * 继承父类构造函数, {@link #withDateFormat(DateFormat, String)}方法使用
     * 
     * @param base
     * @param df
     * @param formatString
     */
    protected DateDeserializer(com.fasterxml.jackson.databind.deser.std.DateDeserializers.DateDeserializer base, DateFormat df,
            String formatString) {
        super(base, df, formatString);
    }

    @Override
    protected DateDeserializer withDateFormat(DateFormat df, String formatString) {
        return new DateDeserializer(this, df, formatString);
    }

    @Override
    public Date deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        return super.deserialize(p, ctxt);
    }

    @Override
    protected Date _parseDate(String value, DeserializationContext ctxt) throws IOException {
        try {
            return super._parseDate(value, ctxt);
        } catch (InvalidFormatException ex) {
            return converter.convert(value.trim(), StringUtils.isBlank(format) ? null : SetUtils.asSet(format));
        }
    }

}
