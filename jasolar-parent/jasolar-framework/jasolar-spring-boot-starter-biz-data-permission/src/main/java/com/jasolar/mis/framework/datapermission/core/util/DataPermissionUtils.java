package com.jasolar.mis.framework.datapermission.core.util;

import java.util.concurrent.Callable;

import com.jasolar.mis.framework.datapermission.core.aop.DataPermissionContextHolder;

import lombok.SneakyThrows;

/**
 * 数据权限 Util
 *
 * @author zhaohuang
 */
public class DataPermissionUtils {

    private DataPermissionUtils() {
    }

    /**
     * 忽略数据权限，执行对应的逻辑
     *
     * @param runnable 逻辑
     */
    public static void executeIgnore(Runnable runnable) {
        DataPermissionContextHolder.enable(false);
        try {
            // 执行 runnable
            runnable.run();
        } finally {
            DataPermissionContextHolder.remove();
        }
    }

    /**
     * 忽略数据权限，执行对应的逻辑
     *
     * @param callable 逻辑
     * @return 执行结果
     */
    @SneakyThrows
    public static <T> T executeIgnore(Callable<T> callable) {
        DataPermissionContextHolder.enable(false);
        try {
            // 执行 callable
            return callable.call();
        } finally {
            DataPermissionContextHolder.remove();
        }
    }

}
