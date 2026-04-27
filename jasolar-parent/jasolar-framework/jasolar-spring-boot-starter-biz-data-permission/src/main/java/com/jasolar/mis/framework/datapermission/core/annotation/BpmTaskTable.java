package com.jasolar.mis.framework.datapermission.core.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.jasolar.mis.framework.mybatis.core.dataobject.BaseBpmTaskDO;

/**
 * 此注解用于添加到实体类上或者用在 {@link DataTable}注解中，用于为指定表的查询权限添加xxx_bpm_task表的数据权限
 * 
 * @author galuo
 * @date 2025-03-11 16:34
 *
 */
@Documented
@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface BpmTaskTable {

    /**
     * 业务对应的审批人xxx_bpm_task表，每个服务的表名前缀不一样。如寻源为sourcing_bpm_task,预算为budget_bpm_task.
     * 如果在单一服务中只有一个xxx_bpm_task表, 则一般不需要配置
     */
    Class<? extends BaseBpmTaskDO> value();

    /** 数据库表对应的流程标识.因为业务表的ID是使用雪花算法生成的，全局唯一。所以一般不用指定流程定义，直接使用id即可，如果指定了此参数，则会添加procDefKey字段作为查询条件 */
    String[] bizTypes() default {};
}
