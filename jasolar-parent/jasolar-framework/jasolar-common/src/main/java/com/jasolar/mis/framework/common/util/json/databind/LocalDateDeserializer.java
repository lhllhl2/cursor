package com.jasolar.mis.framework.common.util.json.databind;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.annotation.JsonFormat.Shape;
import com.fasterxml.jackson.annotation.JsonFormat.Value;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.fasterxml.jackson.datatype.jsr310.deser.JSR310DateTimeDeserializerBase;
import com.jasolar.mis.framework.common.util.collection.SetUtils;
import com.jasolar.mis.framework.common.util.date.converter.LocalDateConverter;

import lombok.Getter;
import lombok.Setter;

/**
 * 
 * LocalDate的反序列化
 * 
 * @author galuo
 * @date 2025-04-17 19:56
 *
 */
@SuppressWarnings("serial")
public class LocalDateDeserializer extends com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer {

    /** 默认格式转换失败后尝试进行日期转换的Converter */
    @Getter
    @Setter
    private LocalDateConverter converter = LocalDateConverter.DEFAULT;

    /** 优先使用的默认格式 */
    protected String format;

    public LocalDateDeserializer(String pattern) {
        this(DateTimeFormatter.ofPattern(pattern));
        this.format = pattern;
    }

    public LocalDateDeserializer(DateTimeFormatter dtf) {
        super(dtf);
    }

    public LocalDateDeserializer(LocalDateDeserializer base, DateTimeFormatter dtf) {
        super(base, dtf);
    }

    protected LocalDateDeserializer(LocalDateDeserializer base, Boolean leniency) {
        super(base, leniency);
    }

    protected LocalDateDeserializer(LocalDateDeserializer base, Shape shape) {
        super(base, shape);
    }

    @Override
    protected LocalDateDeserializer withDateFormat(DateTimeFormatter dtf) {
        LocalDateDeserializer desr = new LocalDateDeserializer(this, dtf);
        desr.converter = this.converter;
        return desr;
    }

    @Override
    protected LocalDateDeserializer withLeniency(Boolean leniency) {
        LocalDateDeserializer desr = new LocalDateDeserializer(this, leniency);
        desr.converter = this.converter;
        return desr;
    }

    @Override
    protected LocalDateDeserializer withShape(Shape shape) {
        LocalDateDeserializer desr = new LocalDateDeserializer(this, shape);
        desr.converter = this.converter;
        return desr;
    }

    @Override
    protected JSR310DateTimeDeserializerBase<?> _withFormatOverrides(DeserializationContext ctxt, BeanProperty property,
            Value formatOverrides) {
        LocalDateDeserializer deser = (LocalDateDeserializer) super._withFormatOverrides(ctxt, property, formatOverrides);
        deser.converter = this.converter;
        return deser;
    }

    @Override
    protected LocalDate _fromString(JsonParser p, DeserializationContext ctxt, String string0) throws IOException {
        try {
            return super._fromString(p, ctxt, string0);
        } catch (InvalidFormatException ex) {
            return converter.convert(string0.trim(), StringUtils.isBlank(format) ? null : SetUtils.asSet(format));
        }
    }

}
