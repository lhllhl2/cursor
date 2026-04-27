package com.jasolar.mis.module.system.controller.budget.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.lang.reflect.Field;

/**
 * 互斥必填验证器
 * 验证两个字段必须二选一（不能都为空，也不能都有值）
 * 
 * @author jasolar
 */
public class MutuallyExclusiveNotNullValidator implements ConstraintValidator<MutuallyExclusiveNotNull, Object> {
    
    private String field1;
    private String field2;
    private String field1Name;
    private String field2Name;
    private String message;
    
    @Override
    public void initialize(MutuallyExclusiveNotNull constraintAnnotation) {
        this.field1 = constraintAnnotation.field1();
        this.field2 = constraintAnnotation.field2();
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
            Object fieldValue1 = getFieldValue(value, field1);
            Object fieldValue2 = getFieldValue(value, field2);
            
            boolean field1IsEmpty = isEmpty(fieldValue1);
            boolean field2IsEmpty = isEmpty(fieldValue2);
            
            // 两个都为空 或 两个都有值，都是不合法的
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
            return ((String) value).trim().isEmpty();
        }
        return false;
    }
}

