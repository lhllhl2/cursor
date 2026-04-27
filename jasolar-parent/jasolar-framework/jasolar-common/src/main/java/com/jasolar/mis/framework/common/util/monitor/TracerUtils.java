package com.jasolar.mis.framework.common.util.monitor;

import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.apache.skywalking.apm.toolkit.trace.TraceContext;
import org.springframework.util.ClassUtils;

import com.jasolar.mis.framework.common.util.servlet.ServletUtils;

import cn.hutool.core.text.StrPool;
import lombok.extern.slf4j.Slf4j;

/**
 * 链路追踪工具类
 * <p>
 * 考虑到每个 starter 都需要用到该工具类，所以放到 common 模块下的 util 包下
 *
 * @author zhaohuang
 */
@Slf4j
public final class TracerUtils {

    /** 如果未启用skywalking则自行生成traceId,并且传递到下游 */
    public static final String HEADER_TRACE_ID = "X-FII-Trace-Id";

    /** 如果未启用skywalking则自行生成traceId */
    private static final InheritableThreadLocal<String> TRACE_ID = new InheritableThreadLocal<>();

    /**
     * 私有化构造方法
     */
    private TracerUtils() {
    }

    /**
     * 获得链路追踪编号，直接返回 SkyWalking 的 TraceId。 如果不存在的话为空字符串！！！
     *
     * @return 链路追踪编号
     */
    public static String getTraceId() {
        String traceId = TraceContext.traceId();
        if (StringUtils.isNotBlank(traceId)) {
            return traceId;
        }

        if (ClassUtils.isPresent("jakarta.servlet.http.HttpServletRequest", TracerUtils.class.getClassLoader())) {
            // 注意未使用Spring-WebMVC的时候这里会报错
            traceId = ServletUtils.getRequestHeader(HEADER_TRACE_ID);
            if (StringUtils.isNotBlank(traceId)) {
                // feign调用时可能直接从请求头中获取
                log.info("读取到请求头中的traceId:{}", traceId);
                return traceId;
            }
        }

        traceId = TRACE_ID.get();
        if (StringUtils.isNotBlank(traceId)) {
            return traceId;
        }

        // 生成新的traceId
        traceId = UUID.randomUUID().toString().replace(StrPool.DASHED, StringUtils.EMPTY);
        log.info("生成新的traceId:{}", traceId);
        TRACE_ID.set(traceId);
        return traceId;
    }

    /** 用于清除本地生成的traceId */
    public static void clear() {
        TRACE_ID.remove();
    }

}
