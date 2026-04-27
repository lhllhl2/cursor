package com.jasolar.mis.framework.web.core.filter;

import java.io.IOException;

import org.jboss.logging.MDC;
import org.springframework.web.filter.OncePerRequestFilter;

import com.jasolar.mis.framework.common.util.monitor.TracerUtils;
import com.jasolar.mis.framework.common.util.servlet.ServletUtils;
import com.jasolar.mis.framework.web.core.util.WebFrameworkUtils;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * 用于注入日志MDC的参数
 */
public class MDCInjectingRequestFilter extends OncePerRequestFilter {

    /** 请求的tenant */
    public static final String TENANT = " tenant";

    /** 请求的traceId */
    public static final String TRACE_ID = "traceId";

    /** 请求的method */
    public static final String HTTP_METHOD = "httpMethod";
    /** 请求的method */
    public static final String REQUEST_URL = "requestUrl";
    /** 请求的来源IP */
    public static final String CLIENT_IP = "clientIp";
    /** 请求的登录账号 */
    public static final String USER = "user";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        try {
            Long tenantId = WebFrameworkUtils.getTenantId(request);
            MDC.put(TENANT, tenantId == null ? "#" : tenantId.toString());
            MDC.put(TRACE_ID, TracerUtils.getTraceId());
            MDC.put(HTTP_METHOD, request.getMethod());
            MDC.put(REQUEST_URL, request.getRequestURL().toString());
            MDC.put(CLIENT_IP, ServletUtils.getClientIP(request));
            MDC.put(USER, WebFrameworkUtils.getLoginUserNo(request));

            filterChain.doFilter(request, response);

            MDC.put(USER, WebFrameworkUtils.getLoginUserNo(request));
        } finally {
            MDC.remove(TENANT);
            MDC.remove(TRACE_ID);
            MDC.remove(HTTP_METHOD);
            MDC.remove(REQUEST_URL);
            MDC.remove(CLIENT_IP);
            MDC.remove(USER);
        }

    }

}
