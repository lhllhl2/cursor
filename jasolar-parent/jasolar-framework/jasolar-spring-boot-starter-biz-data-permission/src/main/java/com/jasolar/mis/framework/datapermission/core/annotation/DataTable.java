package com.jasolar.mis.framework.datapermission.core.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.jasolar.mis.framework.datapermission.core.rule.impl.BpmTaskDataPermissionRule;

/**
 * 
 * 用于配置权限规则使用的表. 如果不配置则会使用默认的表名和字段名.
 * 
 * @author galuo
 * @date 2025-03-04 18:32
 *
 */
@Documented
@Target({ ElementType.ANNOTATION_TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface DataTable {

    /** schema名称 */
    String schema() default "";

    /** 原始表名 */
    String name() default "";

    /** 表别名。 表别名和原始表名可以二选一设值，如果两个字段都配置了，则需要两个字段都相同 */
    String alias() default "";

    /** 使用的列名,可以有多个列， 多个列之间用逗号隔开，如果一个表有多个列名用于规则，则多列之间是OR。 未指定则使用配置的规则的默认列名 */
    String[] columns() default {};

    /** 使用的BPM_TASK表对象, 用于 {@link BpmTaskDataPermissionRule}。仅读取第一个配置，这里定义为数组是方便默认为空不做配置 */
    BpmTaskTable[] bpmTaskTable() default {};

    /** 允许查询NULL值的列, 必须属于{@link #columns()}中的某一列 */
    String[] nullableColumns() default {};

}
