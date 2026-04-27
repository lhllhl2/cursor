package com.jasolar.mis.module.system.controller.budget.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * 条件必填验证器
 * 根据条件字段的值来决定哪些字段必须不为空
 * 
 * @author jasolar
 */
public class ConditionalRequiredValidator implements ConstraintValidator<ConditionalRequired, Object> {
    
    private String conditionField;
    private Set<String> conditionValues;
    private String[] requiredFields;
    private String[] fieldNames;
    
    @Override
    public void initialize(ConditionalRequired constraintAnnotation) {
        this.conditionField = constraintAnnotation.conditionField();
        this.conditionValues = new HashSet<>(Arrays.asList(constraintAnnotation.conditionValues()));
        this.requiredFields = constraintAnnotation.requiredFields();
        this.fieldNames = constraintAnnotation.fieldNames();
    }
    
    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }
        
        try {
            // 获取条件字段的值
            Object conditionValue = getFieldValue(value, conditionField);
            String conditionValueStr = conditionValue == null ? null : String.valueOf(conditionValue);
            
            // 如果条件字段的值不在指定的条件值列表中，不进行校验
            if (conditionValueStr == null || !conditionValues.contains(conditionValueStr)) {
                return true;
            }
            
            // 条件满足，检查必填字段
            boolean isValid = true;
            for (int i = 0; i < requiredFields.length; i++) {
                String fieldName = requiredFields[i];
                Object fieldValue = getFieldValue(value, fieldName);
                
                if (isEmpty(fieldValue)) {
                    isValid = false;
                    String displayName = (fieldNames != null && i < fieldNames.length && !fieldNames[i].isEmpty()) 
                            ? fieldNames[i] : fieldName;
                    
                    // 为每个字段创建独立的约束违规
                    context.disableDefaultConstraintViolation();
                    context.buildConstraintViolationWithTemplate(
                        String.format("%s不能为空", displayName)
                    ).addPropertyNode(fieldName).addConstraintViolation();
                }
            }
            
            return isValid;
            
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

