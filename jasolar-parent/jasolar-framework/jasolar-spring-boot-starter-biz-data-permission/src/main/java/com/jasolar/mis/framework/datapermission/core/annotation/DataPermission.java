package com.jasolar.mis.framework.datapermission.core.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.AnnotatedElement;

import com.jasolar.mis.framework.datapermission.core.rule.CompositeDataPermissionRule;
import com.jasolar.mis.framework.datapermission.core.scope.ReadWrite;

/**
 * 
 * 数据权限注解。可声明在类或者方法上，标识使用的数据权限规则.
 * 对于未添加注解的类，默认会使用所有权限规则，并且按照默认的表/字段名进行匹配
 * 在使用时，通过{@link AnnotatedElement#getAnnotationsByType(Class)}获取注解的所有权限规则，然后使用AND进行合并
 * 每个注解最终会转换为一个 {@link CompositeDataPermissionRule } 来实现SQL注入
 * 
 * @author galuo
 * @date 2025-03-04 18:31
 *
 */
@Documented
@Target({ ElementType.TYPE, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(DataPermissions.class)
@Inherited
public @interface DataPermission {

    /**
     * 要使用的权限规则列表，此参数为空则表示使用所有规则。规则之间是OR的关系
     * 
     * @return
     */
    DataRule[] value() default {};

    /**
     * 当没有配置{@link #value()}时，为所有默认权限指定的读写类型。
     * 当{@link #value()}不为空时，则使用{@link DataRule#readWrite()}配置每个规则的读写权限
     */
    ReadWrite readWrite() default ReadWrite.ALL;

}
