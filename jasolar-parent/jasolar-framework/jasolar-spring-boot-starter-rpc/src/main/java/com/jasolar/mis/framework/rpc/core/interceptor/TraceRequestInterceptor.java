package com.jasolar.mis.framework.rpc.core.interceptor;

import com.jasolar.mis.framework.common.util.monitor.TracerUtils;

import feign.RequestInterceptor;
import feign.RequestTemplate;

/** 为Feign调用注入traceId */
public class TraceRequestInterceptor implements RequestInterceptor {

    @Override
    public void apply(RequestTemplate requestTemplate) {
        requestTemplate.header(TracerUtils.HEADER_TRACE_ID, TracerUtils.getTraceId());
    }

}
