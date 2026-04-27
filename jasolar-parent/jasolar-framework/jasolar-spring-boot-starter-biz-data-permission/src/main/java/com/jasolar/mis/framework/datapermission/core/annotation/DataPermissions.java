package com.jasolar.mis.framework.datapermission.core.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 
 * 使数据权限注解{@link DataPermission} 可重复.
 * 
 * @author galuo
 * @date 2025-03-04 18:32
 *
 */
@Target({ ElementType.TYPE, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
public @interface DataPermissions {

    /**
     * 使用的多个权限，权限之间使用AND进行合并
     * 
     * @return 使用的多个权限
     */
    DataPermission[] value();
}
