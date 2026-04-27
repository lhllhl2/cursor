package com.jasolar.mis.framework.datapermission.core.rpc;

import java.io.IOException;

import org.springframework.web.filter.OncePerRequestFilter;

import com.jasolar.mis.framework.datapermission.core.aop.DataPermissionContextHolder;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * 处理禁用数据权限的header
 *
 * @author zhaohuang
 */
public class DataPermissionWebFilter extends OncePerRequestFilter {

    /** 禁用数据权限的的header,用于Feign调用时往下传递 */
    public static final String HEADER_DISABLED = "x-data-permission-disabled";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String disabled = request.getHeader(HEADER_DISABLED);
        if (Boolean.parseBoolean(disabled)) {
            // 禁用数据权限
            DataPermissionContextHolder.enable(false);
            try {
                chain.doFilter(request, response);
            } finally {
                DataPermissionContextHolder.remove();
            }
        } else {
            chain.doFilter(request, response);
        }
    }

}
