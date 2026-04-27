package com.jasolar.mis.framework.tracer.core.filter;

import java.io.IOException;

import org.springframework.web.filter.OncePerRequestFilter;

import com.jasolar.mis.framework.common.util.monitor.TracerUtils;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Trace 过滤器，打印 traceId 到 header 中返回
 *
 * @author zhaohuang
 */
public class TraceFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        // 设置响应 traceId
        response.addHeader(TracerUtils.HEADER_TRACE_ID, TracerUtils.getTraceId());
        try {
            // 继续过滤
            chain.doFilter(request, response);
        } finally {
            TracerUtils.clear();
        }
    }

}
