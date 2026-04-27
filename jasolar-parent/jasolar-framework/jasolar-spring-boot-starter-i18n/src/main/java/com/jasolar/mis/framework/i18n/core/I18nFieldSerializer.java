package com.jasolar.mis.framework.i18n.core;

import cn.hutool.core.util.ReflectUtil;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.ContextualSerializer;
import com.jasolar.mis.framework.i18n.I18nUtils;
import com.jasolar.mis.framework.i18n.annotation.I18nField;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;

/**
 * I18nField 序列化器
 * 用于序列化 I18nField 注解的字段
 *
 * @author zhahuang
 * @see I18nField
 * @see I18nUtils
 */
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor
public class I18nFieldSerializer extends JsonSerializer<Object> implements ContextualSerializer {

    /**
     * 要序列化的字段, 字段上必然有{@link I18nField}注解
     */
    private I18nField i18n;

    @Override
    public JsonSerializer<?> createContextual(SerializerProvider serializerProvider, BeanProperty beanProperty) {
        I18nField anno = beanProperty.getAnnotation(I18nField.class);
        if (anno == null) {
            return this;
        }
        return new I18nFieldSerializer(anno);
    }

    /**
     * 获取当前字段的国际化消息CODE
     *
     * @param gen JsonGenerator
     * @return string
     */
    private String getI18nCode(JsonGenerator gen, Object value) {
        Object currentValue = gen.currentValue();
        Object v = StringUtils.isBlank(i18n.value()) ? value : ReflectUtil.getFieldValue(currentValue, i18n.value());
        if (v == null) {
            return StringUtils.EMPTY;
        }
        if (v instanceof String && StringUtils.isBlank(v.toString())) {
            return StringUtils.EMPTY;
        }
        if (StringUtils.isNotBlank(i18n.prefix())) {
            return i18n.prefix() + v;
        }
        return v.toString();
    }

    @Override
    public void serialize(Object value, JsonGenerator gen, SerializerProvider provider) throws IOException {
        String i18nCode = this.getI18nCode(gen, value);
        String msg = StringUtils.isBlank(i18nCode) ? value.toString() : I18nUtils.getMessage(i18nCode, value.toString());
        gen.writeString(msg);
    }
}