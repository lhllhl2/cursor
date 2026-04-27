package com.jasolar.mis.module.system.controller.budget.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Field;

/**
 * 条件互斥必填验证器
 * 根据条件字段的值来决定是否要求两个字段二选一
 * - 如果条件字段为空或"NAN"：两个字段必须二选一（不能都为空，也不能都有值）
 * - 如果条件字段有值且不是"NAN"：两个字段可以都为空，也可以只填一个，但不能同时有值
 * 
 * @author jasolar
 */
public class ConditionalMutuallyExclusiveNotNullValidator implements ConstraintValidator<ConditionalMutuallyExclusiveNotNull, Object> {
    
    private String field1;
    private String field2;
    private String conditionField;
    private String field1Name;
    private String field2Name;
    private String message;
    
    @Override
    public void initialize(ConditionalMutuallyExclusiveNotNull constraintAnnotation) {
        this.field1 = constraintAnnotation.field1();
        this.field2 = constraintAnnotation.field2();
        this.conditionField = constraintAnnotation.conditionField();
        this.field1Name = constraintAnnotation.field1Name().isEmpty() ? field1 : constraintAnnotation.field1Name();
        this.field2Name = constraintAnnotation.field2Name().isEmpty() ? field2 : constraintAnnotation.field2Name();
        this.message = constraintAnnotation.message();
    }
    
    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }
        
        try {
            // 获取条件字段的值
            Object conditionValue = getFieldValue(value, conditionField);
            boolean conditionIsEmpty = isEmpty(conditionValue) || "NAN".equals(conditionValue);
            
            // 获取两个字段的值
            Object fieldValue1 = getFieldValue(value, field1);
            Object fieldValue2 = getFieldValue(value, field2);
            
            boolean field1IsEmpty = isEmpty(fieldValue1);
            boolean field2IsEmpty = isEmpty(fieldValue2);
            
            // 如果条件字段为空或"NAN"：必须二选一（不能都为空，也不能都有值）
            if (conditionIsEmpty) {
                if (field1IsEmpty && field2IsEmpty) {
                    // 构建错误消息
                    context.disableDefaultConstraintViolation();
                    context.buildConstraintViolationWithTemplate(
                        String.format("%s和%s必须二选一，不能都为空", field1Name, field2Name)
                    ).addConstraintViolation();
                    return false;
                }
                
                if (!field1IsEmpty && !field2IsEmpty) {
                    // 构建错误消息
                    context.disableDefaultConstraintViolation();
                    context.buildConstraintViolationWithTemplate(
                        String.format("%s和%s必须二选一，不能同时有值", field1Name, field2Name)
                    ).addConstraintViolation();
                    return false;
                }
            } else {
                // 如果条件字段有值且不是"NAN"：可以都为空，也可以只填一个，但不能同时有值
                if (!field1IsEmpty && !field2IsEmpty) {
                    // 构建错误消息
                    context.disableDefaultConstraintViolation();
                    context.buildConstraintViolationWithTemplate(
                        String.format("%s和%s不能同时有值", field1Name, field2Name)
                    ).addConstraintViolation();
                    return false;
                }
            }
            
            return true;
            
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * 获取字段值
     */
    private Object getFieldValue(Object object, String fieldName) throws Exception {
        Field field = object.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        return field.get(object);
    }
    
    /**
     * 判断字段是否为空
     */
    private boolean isEmpty(Object value) {
        if (value == null) {
            return true;
        }
        if (value instanceof String) {
            return StringUtils.isBlank((String) value);
        }
        return false;
    }
}

