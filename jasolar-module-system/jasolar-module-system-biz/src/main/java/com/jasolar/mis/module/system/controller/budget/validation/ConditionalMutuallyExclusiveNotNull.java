package com.jasolar.mis.module.system.controller.budget.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

/**
 * 条件互斥必填验证注解
 * 根据条件字段的值来决定是否要求两个字段二选一
 * - 如果条件字段为空或"NAN"：两个字段必须二选一（不能都为空，也不能都有值）
 * - 如果条件字段有值且不是"NAN"：两个字段可以都为空，也可以只填一个，但不能同时有值
 * 
 * @author jasolar
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = ConditionalMutuallyExclusiveNotNullValidator.class)
@Documented
public @interface ConditionalMutuallyExclusiveNotNull {
    
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
     * 条件字段名（用于判断是否需要二选一）
     */
    String conditionField();
    
    /**
     * 第一个字段的显示名称（用于错误消息）
     */
    String field1Name() default "";
    
    /**
     * 第二个字段的显示名称（用于错误消息）
     */
    String field2Name() default "";
}

