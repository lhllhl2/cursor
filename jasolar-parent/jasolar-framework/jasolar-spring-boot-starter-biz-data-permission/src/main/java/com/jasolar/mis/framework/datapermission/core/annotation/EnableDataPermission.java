package com.jasolar.mis.framework.datapermission.core.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 用于控制是否开启数据权限.
 * 最好将此注解到接口上方法上,此接口不可继承
 * 
 * @author galuo
 * @date 2025-03-03 11:37
 *
 */
@Documented
@Target({ ElementType.TYPE, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
public @interface EnableDataPermission {

    /**
     * 是否开启数据权限
     * 
     * @return 为false则不开始数据权限. 默认为true
     */
    boolean value() default true;
}
