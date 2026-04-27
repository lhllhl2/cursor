package com.jasolar.mis.framework.datapermission.core.scope;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

import com.jasolar.mis.framework.common.util.servlet.ServletUtils;

import jakarta.annotation.Nullable;

/**
 * 用于接收由前端接口中传递的header参数.
 * 用于确定请求的菜单ID, 以及读/写操作等。如果Header中有则优先于注解中的配置
 * 
 * @author galuo
 * @date 2025-03-05 10:23
 *
 */
public final class RequestContextHolder {

    // /** */
    // private static final ThreadLocal<ReadWrite> RW = new InheritableThreadLocal<>();

    /** 数据权限读写分离的header,可用于前端传入，以及feign往下传递 */
    public static final String READWRITE_HEADER_NAME = "x-data-permission-rw";

    /** 菜单ID的header,可用于前端传入，以及feign往下传递 */
    public static final String MENU_HEADER_NAME = "x-menu-id";

    /**
     * 获取当前请求中的header，判断是读/写操作
     * 
     * @return 请求header中的读写操作，没有header则返回null
     */
    @Nullable
    public static ReadWrite readWrite() {
        String header = ServletUtils.getRequestHeader(READWRITE_HEADER_NAME);
        if (StringUtils.isNotBlank(header)) {
            return ReadWrite.of(NumberUtils.toInt(header));
        }

        return null;
    }

    /**
     * 获取当前请求header中的菜单ID
     * 
     * @return 没有header则返回null, 使用默认权限
     */
    @Nullable
    public static Long menuId() {
        String header = ServletUtils.getRequestHeader(MENU_HEADER_NAME);
        if (StringUtils.isNotBlank(header)) {
            return NumberUtils.toLong(header);
        }
        return null;
    }

}
