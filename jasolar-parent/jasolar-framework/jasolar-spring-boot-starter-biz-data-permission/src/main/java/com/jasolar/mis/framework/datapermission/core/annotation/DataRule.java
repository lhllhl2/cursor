package com.jasolar.mis.framework.datapermission.core.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.jasolar.mis.framework.datapermission.core.rule.DataPermissionRule;
import com.jasolar.mis.framework.datapermission.core.scope.ReadWrite;

/**
 * 
 * 单维度权限配置
 * 
 * @author galuo
 * @date 2025-03-04 18:32
 *
 */
@Documented
@Target({ ElementType.ANNOTATION_TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface DataRule {
    /**
     * 要使用的权限规则
     * 
     * @return
     */
    Class<? extends DataPermissionRule> value() default DataPermissionRule.class;

    /** 默认不区分读写，如果配置为读/写，则只会匹配数据库中配置的读/写 */
    ReadWrite readWrite() default ReadWrite.ALL;

    /** 匹配的数据表信息， 如果一个查询中匹配到了多个表，多个表之间的条件固定是AND */
    DataTable[] tables() default {};

    /**
     * 是否允许查询NULL值的数据, 默认为false
     * 
     * @return 是否允许查询NULL值的数据
     */
    boolean nullable() default false;
}
