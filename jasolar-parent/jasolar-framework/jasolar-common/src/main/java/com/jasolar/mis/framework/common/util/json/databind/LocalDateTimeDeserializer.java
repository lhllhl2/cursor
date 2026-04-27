package com.jasolar.mis.framework.common.util.json.databind;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonFormat.Shape;
import com.fasterxml.jackson.annotation.JsonFormat.Value;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.fasterxml.jackson.datatype.jsr310.deser.JSR310DateTimeDeserializerBase;
import com.jasolar.mis.framework.common.util.collection.SetUtils;
import com.jasolar.mis.framework.common.util.date.converter.LocalDateTimeConverter;

import lombok.Getter;
import lombok.Setter;

/**
 * LocalDateTime的反序列化
 * 
 * @author galuo
 * @date 2025-04-17 19:45
 *
 */
@SuppressWarnings("serial")
public class LocalDateTimeDeserializer extends com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer {

    /** 默认格式转换失败后尝试进行日期转换的Converter */
    @Getter
    @Setter
    private LocalDateTimeConverter converter = LocalDateTimeConverter.DEFAULT;

    /** 优先使用的默认格式 */
    protected String format;

    public LocalDateTimeDeserializer(String pattern) {
        this(DateTimeFormatter.ofPattern(pattern));
        this.format = pattern;
    }

    public LocalDateTimeDeserializer(DateTimeFormatter f) {
        super(f);
    }

    protected LocalDateTimeDeserializer(LocalDateTimeDeserializer base, Boolean leniency, DateTimeFormatter formatter, Shape shape,
            Boolean readTimestampsAsNanosOverride) {
        super(base, leniency, formatter, shape, readTimestampsAsNanosOverride);
    }

    protected LocalDateTimeDeserializer(LocalDateTimeDeserializer base, Boolean leniency) {
        super(base, leniency);
    }

    @Override
    protected LocalDateTimeDeserializer withDateFormat(DateTimeFormatter dtf) {
        LocalDateTimeDeserializer deser = new LocalDateTimeDeserializer(this, _isLenient, dtf, _shape, _readTimestampsAsNanosOverride);
        deser.converter = this.converter;
        return deser;
    }

    @Override
    protected LocalDateTimeDeserializer withLeniency(Boolean leniency) {
        LocalDateTimeDeserializer deser = new LocalDateTimeDeserializer(this, leniency);
        deser.converter = this.converter;
        return deser;
    }

    @Override
    protected JSR310DateTimeDeserializerBase<?> _withFormatOverrides(DeserializationContext ctxt, BeanProperty property,
            Value formatOverrides) {
        LocalDateTimeDeserializer deser = (LocalDateTimeDeserializer) super._withFormatOverrides(ctxt, property, formatOverrides);
        Boolean readTimestampsAsNanosOverride = formatOverrides.getFeature(JsonFormat.Feature.READ_DATE_TIMESTAMPS_AS_NANOSECONDS);
        if (!Objects.equals(readTimestampsAsNanosOverride, deser._readTimestampsAsNanosOverride)) {
            deser = new LocalDateTimeDeserializer(deser, deser._isLenient, deser._formatter, deser._shape, readTimestampsAsNanosOverride);
        }
        deser.converter = this.converter;
        return deser;
    }

    @Override
    protected LocalDateTime _fromString(JsonParser p, DeserializationContext ctxt, String string0) throws IOException {
        try {
            return super._fromString(p, ctxt, string0);
        } catch (InvalidFormatException ex) {
            return converter.convert(string0.trim(), StringUtils.isBlank(format) ? null : SetUtils.asSet(format));
        }
    }

}
