package com.jasolar.mis.framework.datapermission.core.aop;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Set;

import com.alibaba.ttl.TransmittableThreadLocal;
import com.jasolar.mis.framework.datapermission.core.annotation.DataPermission;

import jakarta.annotation.Nullable;

/**
 * {@link DataPermission} 注解的 Context 上下文
 *
 * @author zhaohuang
 */
public class DataPermissionContextHolder {

    private DataPermissionContextHolder() {
    }

    /** 使用 Stack 的原因，可能存在方法的嵌套调用 */
    private static final ThreadLocal<Deque<DataPermissionContext>> CTX = TransmittableThreadLocal.withInitial(ArrayDeque::new);

    /**
     * 获得当前的 DataPermission 注解
     *
     * @return DataPermission 注解
     */
    @Nullable
    public static DataPermissionContext get() {
        Deque<DataPermissionContext> stack = CTX.get();
        return stack.isEmpty() ? null : stack.peek();
    }

    /**
     * 入栈 DataPermission 注解
     *
     * @param dataPermission AOP中获取到的DataPermission注解
     * 
     * @return 如果已经禁用了数据权限则返回false,并且再同一个AOP中后续不要再调用{@link #remove()}方法
     */
    public static boolean add(@Nullable Set<DataPermission> permissions) {
        if (permissions == null || permissions.isEmpty() || isDisabled()) {
            // 已关闭权限,则后续的所有权限注解均不再生效,除非通过注解@EnableDataPermission重新启动
            return false;
        }

        CTX.get().push(DataPermissionContext.of(permissions));
        return true;
    }

    /**
     * 启用/禁用权限
     * 
     * @param enable true表示启用,false表示禁用.一旦禁用,则后续的权限注解均不做处理
     */
    public static void enable(boolean enable) {
        CTX.get().push(enable ? DataPermissionContext.enable() : DataPermissionContext.disable());
    }

    /** @return 当前是否禁用数据权限 */
    public static boolean isDisabled() {
        DataPermissionContext ctx = get();
        return ctx != null && ctx.getEnabled() != null && !ctx.getEnabled();
    }

    /**
     * 出栈 DataPermission 注解
     *
     * @return DataPermission 注解
     */
    public static DataPermissionContext remove() {
        Deque<DataPermissionContext> stack = CTX.get();
        if (stack.isEmpty()) {
            CTX.remove();
            return null;
        }

        DataPermissionContext ctx = stack.pop();
        // 无元素时，清空 ThreadLocal
        if (stack.isEmpty()) {
            CTX.remove();
        }
        return ctx;
    }

    /**
     * 清空上下文
     *
     * 目前仅仅用于单测
     */
    public static void clear() {
        CTX.remove();
    }

}
