package com.jasolar.mis.module.system.controller.budget.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 条件必填验证注解
 * 根据条件字段的值来决定哪些字段必须不为空
 * 
 * @author jasolar
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = ConditionalRequiredValidator.class)
@Documented
@Repeatable(ConditionalRequired.List.class)
public @interface ConditionalRequired {
    
    String message() default "字段不能为空";
    
    Class<?>[] groups() default {};
    
    Class<? extends Payload>[] payload() default {};
    
    /**
     * 条件字段名（用于判断是否需要校验）
     */
    String conditionField();
    
    /**
     * 条件值（当条件字段等于这些值时，需要校验指定字段）
     */
    String[] conditionValues();
    
    /**
     * 需要校验的字段名列表（当条件满足时，这些字段不能为空）
     */
    String[] requiredFields();
    
    /**
     * 字段显示名称（用于错误消息，顺序与 requiredFields 对应）
     */
    String[] fieldNames() default {};

    /**
     * 支持在同一个类上重复使用该注解
     */
    @Target({ElementType.TYPE})
    @Retention(RetentionPolicy.RUNTIME)
    @Documented
    @interface List {
        ConditionalRequired[] value();
    }
}

