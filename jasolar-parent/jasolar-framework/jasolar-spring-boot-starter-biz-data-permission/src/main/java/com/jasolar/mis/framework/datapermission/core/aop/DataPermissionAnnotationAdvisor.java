package com.jasolar.mis.framework.datapermission.core.aop;

import java.io.Serializable;

import org.aopalliance.aop.Advice;
import org.springframework.aop.Pointcut;
import org.springframework.aop.support.AbstractPointcutAdvisor;
import org.springframework.aop.support.ComposablePointcut;
import org.springframework.aop.support.annotation.AnnotationMatchingPointcut;

import com.jasolar.mis.framework.datapermission.core.annotation.DataPermission;
import com.jasolar.mis.framework.datapermission.core.annotation.EnableDataPermission;

import lombok.EqualsAndHashCode;
import lombok.Getter;

/**
 * {@link DataPermission} {@link EnableDataPermission} 注解的 Advisor 实现类
 *
 * @author zhaohuang
 */
@Getter
@EqualsAndHashCode(callSuper = true)
public class DataPermissionAnnotationAdvisor extends AbstractPointcutAdvisor implements Serializable {

    private static final long serialVersionUID = 1L;

    private final transient Advice advice;

    private final Pointcut pointcut;

    public DataPermissionAnnotationAdvisor() {
        this.advice = new DataPermissionAnnotationInterceptor();
        this.pointcut = new ComposablePointcut(new AnnotationMatchingPointcut(DataPermission.class, true))
                .union(new AnnotationMatchingPointcut(null, DataPermission.class, true))
                .union(new AnnotationMatchingPointcut(EnableDataPermission.class, true))
                .union(new AnnotationMatchingPointcut(null, EnableDataPermission.class, true));
    }

}
