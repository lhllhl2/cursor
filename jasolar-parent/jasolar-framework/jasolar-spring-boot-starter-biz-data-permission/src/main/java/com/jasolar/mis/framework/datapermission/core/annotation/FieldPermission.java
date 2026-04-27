package com.jasolar.mis.framework.datapermission.core.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.fasterxml.jackson.annotation.JacksonAnnotationsInside;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.jasolar.mis.framework.datapermission.core.field.DefaultFieldPermissionHandler;
import com.jasolar.mis.framework.datapermission.core.field.FieldPermissionHandler;
import com.jasolar.mis.framework.datapermission.core.field.FieldPermissionSerializer;

/**
 * 返回字段的权限
 *
 * @author zhangj
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@JacksonAnnotationsInside // 此注解是其他所有 jackson 注解的元注解，打上了此注解的注解表明是 jackson 注解的一部分
@JsonSerialize(using = FieldPermissionSerializer.class)
public @interface FieldPermission {

    /**
     * 数据字段权限标识
     *
     * @return
     */
    String value() default "";

    /**
     * 数据字段权限处理器
     */
    Class<? extends FieldPermissionHandler> handler() default DefaultFieldPermissionHandler.class;
}
