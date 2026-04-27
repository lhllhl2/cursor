package com.jasolar.mis.framework.datapermission.core.aop;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.core.MethodClassKey;
import org.springframework.core.annotation.AnnotatedElementUtils;

import com.jasolar.mis.framework.datapermission.core.annotation.DataPermission;
import com.jasolar.mis.framework.datapermission.core.annotation.EnableDataPermission;

import lombok.Getter;

/**
 * {@link DataPermission} {@link EnableDataPermission} 注解的拦截器
 * 1. 在执行方法前，将注解入栈
 * 2. 在执行方法后，将注解出栈
 * 注意如果使用{@link EnableDataPermission} 禁用数据权限后, 后续的{@link DataPermission}将不再入栈
 *
 * @author zhaohuang
 */
public class DataPermissionAnnotationInterceptor implements MethodInterceptor {

    @Getter
    private final Map<MethodClassKey, DataPermissionContext> caches = new ConcurrentHashMap<>();

    @Override
    public Object invoke(MethodInvocation methodInvocation) throws Throwable {
        DataPermissionContext ctx = find(methodInvocation);
        if (ctx.getEnabled() != null) {
            // 有禁用/启用权限
            DataPermissionContextHolder.enable(ctx.getEnabled());
        }

        // 是否有入栈权限
        boolean hasPermission = DataPermissionContextHolder.add(ctx.getPermissions());
        try {
            // 执行逻辑
            return methodInvocation.proceed();
        } finally {
            if (hasPermission) {
                // 有入栈权限, 出栈
                DataPermissionContextHolder.remove();
            }
            if (ctx.getEnabled() != null) {
                // 有禁用/启用权限, 出栈
                DataPermissionContextHolder.remove();
            }
        }
    }

    /**
     * 获取注解并缓存
     * 
     * @param methodInvocation
     * @return
     */
    private DataPermissionContext find(MethodInvocation methodInvocation) {
        Method method = methodInvocation.getMethod();
        Object targetObject = methodInvocation.getThis();
        Class<?> clazz = targetObject != null ? targetObject.getClass() : method.getDeclaringClass();

        MethodClassKey methodClassKey = new MethodClassKey(method, clazz);
        DataPermissionContext cache = caches.get(methodClassKey);
        if (cache == null) {
            // 初始化数据并缓存
            cache = new DataPermissionContext();
            // 首先解析EnableDataPermission注解,判断是否有启用或者禁用数据权限
            // 在方法上查找
            EnableDataPermission enable = AnnotatedElementUtils.findMergedAnnotation(method, EnableDataPermission.class);
            if (enable == null) {
                // 从类上查找
                enable = AnnotatedElementUtils.findMergedAnnotation(clazz, EnableDataPermission.class);
            }
            if (enable != null) {
                // 有禁用/启用权限
                cache.setEnabled(enable.value());
                // DataPermissionContextHolder.enable(enable.value());
            }

            // 其次解析DataPermission注解,判断是否有权限规则配置
            // 在方法上查找
            Set<DataPermission> permissions = AnnotatedElementUtils.findMergedRepeatableAnnotations(method, DataPermission.class);
            if (permissions.isEmpty()) {
                // 从类上查找
                permissions = AnnotatedElementUtils.findMergedRepeatableAnnotations(clazz, DataPermission.class);
            }
            if (!permissions.isEmpty()) {
                // 如果当前栈中最后一次是禁用数据权限,则不会入栈,并返回false
                // hasPermission = DataPermissionContextHolder.add(permissions);
                cache.setPermissions(permissions);
            }
            caches.put(methodClassKey, cache);
        }
        return cache;
    }

}
