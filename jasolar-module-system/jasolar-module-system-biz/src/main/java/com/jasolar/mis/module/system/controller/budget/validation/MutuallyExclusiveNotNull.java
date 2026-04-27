package com.jasolar.mis.module.system.controller.budget.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

/**
 * 互斥必填验证注解
 * 用于验证两个字段必须二选一（不能都为空，也不能都有值）
 * 
 * @author jasolar
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = MutuallyExclusiveNotNullValidator.class)
@Documented
public @interface MutuallyExclusiveNotNull {
    
    String message() default "字段必须二选一";
    
    Class<?>[] groups() default {};
    
    Class<? extends Payload>[] payload() default {};
    
    /**
     * 第一个字段名
     */
    String field1();
    
    /**
     * 第二个字段名
     */
    String field2();
    
    /**
     * 第一个字段的显示名称（用于错误消息）
     */
    String field1Name() default "";
    
    /**
     * 第二个字段的显示名称（用于错误消息）
     */
    String field2Name() default "";
}

