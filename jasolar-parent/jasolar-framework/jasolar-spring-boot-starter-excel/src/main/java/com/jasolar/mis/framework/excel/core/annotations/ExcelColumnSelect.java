package com.jasolar.mis.framework.excel.core.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.jasolar.mis.framework.excel.core.function.ExcelColumnSelectFunction;

/**
 * 给 Excel 列添加字典下拉选择数据
 *
 * 其中 {@link #dictType()} 和 {@link #functionName()} 二选一
 *
 * @author zhaohuang
 */
@Target({ ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface ExcelColumnSelect {

    /**
     * 通过字典获取下拉
     * 
     * @return 字典类型
     */
    String dictType() default "";

    /**
     * @return 获取下拉数据源的类, 注意必须是Spring托管的bean
     */
    Class<? extends ExcelColumnSelectFunction> beanClass() default ExcelColumnSelectFunction.class;

    /**
     * beanName有值则优先通过name读取
     * 
     * @return 获取下拉数据源的bean那么, 注意必须是Spring托管的bean
     */
    String beanName() default "";

}
