package com.jasolar.mis.framework.i18n.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.fasterxml.jackson.annotation.JacksonAnnotationsInside;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.jasolar.mis.framework.i18n.core.I18nFieldSerializer;

/**
 * 国际化字段注解
 * 用于标记需要国际化的字段
 *
 * @author zhahuang
 */
@Target({ ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
@Documented

@JacksonAnnotationsInside // 此注解是其他所有 jackson 注解的元注解，打上了此注解的注解表明是 jackson 注解的一部分
@JsonSerialize(using = I18nFieldSerializer.class)
public @interface I18nField {

    /** 获取国际化值的来源字段名, 如果为空则表示其自身. 用于Excel导出时,忽略此参数值,固定使用字段自己的值,因为excel的converter中无法获取整个bean的值 */
    String value() default "";

    /** 国际化消息前缀，如 menu、dict 等 */
    String prefix() default "";
    
}