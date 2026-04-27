package com.jasolar.mis.framework.datapermission.core.field;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Objects;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.ContextualSerializer;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.jasolar.mis.framework.datapermission.core.annotation.FieldPermission;
import com.jasolar.mis.framework.desensitize.core.base.handler.DesensitizationHandler;

import cn.hutool.core.annotation.AnnotationUtil;
import cn.hutool.core.lang.Singleton;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.ReflectUtil;
import lombok.Getter;
import lombok.Setter;

/**
 * 字段数据数据权限序列化器
 * <p>
 * 实现 JSON 返回数据时，使用 {@link DesensitizationHandler} 对声明注解的字段，进行权限判断并且返回原值或者空
 *
 * @author zhangj
 */
public class FieldPermissionSerializer extends StdSerializer<Object> implements ContextualSerializer {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    @Getter
    @Setter
    private FieldPermissionHandler fieldPermissionHandler;

    protected FieldPermissionSerializer() {
        super(Object.class);
    }

    @Override
    public JsonSerializer<?> createContextual(SerializerProvider serializerProvider, BeanProperty beanProperty) {
        FieldPermission annotation = beanProperty.getAnnotation(FieldPermission.class);
        if (annotation == null) {
            return this;
        }
        FieldPermissionSerializer serializer = new FieldPermissionSerializer();
        serializer.setFieldPermissionHandler(Singleton.get(annotation.handler()));
        return serializer;
    }

    @Override
    public void serialize(Object value, JsonGenerator gen, SerializerProvider serializerProvider) throws IOException {
        if (Objects.isNull(value)) {
            gen.writeNull();
            return;
        }
        // 获取序列化字段
        Field field = getField(gen);
        // 自定义处理器
        FieldPermission[] annotations = AnnotationUtil.getCombinationAnnotations(field, FieldPermission.class);
        if (ArrayUtil.isEmpty(annotations)) {
            gen.writeObject(value);
            return;
        }
        for (FieldPermission annotation : annotations) {
            boolean passed = this.fieldPermissionHandler.hasPermission(value, annotation);
            if (passed) {
                gen.writeObject(value);
                return;
            }
        }
        gen.writeNull();
    }

    /**
     * 获取字段
     *
     * @param generator JsonGenerator
     * @return 字段
     */
    private Field getField(JsonGenerator generator) {
        String currentName = generator.getOutputContext().getCurrentName();
        Object currentValue = generator.currentValue();
        Class<?> currentValueClass = currentValue.getClass();
        return ReflectUtil.getField(currentValueClass, currentName);
    }
}
