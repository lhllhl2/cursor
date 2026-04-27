package com.jasolar.mis.framework.apilog.core.filter;

import static com.jasolar.mis.framework.apilog.core.interceptor.ApiAccessLogInterceptor.ATTRIBUTE_HANDLER_METHOD;
import static com.jasolar.mis.framework.common.util.json.JsonUtils.toJsonString;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.method.HandlerMethod;

import com.fasterxml.jackson.databind.JsonNode;
import com.jasolar.mis.framework.apilog.core.annotation.ApiAccessLog;
import com.jasolar.mis.framework.apilog.core.enums.OperateTypeEnum;
import com.jasolar.mis.framework.common.exception.enums.GlobalErrorCodeConstants;
import com.jasolar.mis.framework.common.pojo.CommonResult;
import com.jasolar.mis.framework.common.security.LoginUser;
import com.jasolar.mis.framework.common.util.json.JsonUtils;
import com.jasolar.mis.framework.common.util.monitor.TracerUtils;
import com.jasolar.mis.framework.common.util.servlet.ServletUtils;
import com.jasolar.mis.framework.web.config.WebProperties;
import com.jasolar.mis.framework.web.core.filter.ApiRequestFilter;
import com.jasolar.mis.framework.web.core.util.WebFrameworkUtils;
import com.jasolar.mis.module.log.api.ApiAccessLogApi;
import com.jasolar.mis.module.log.api.dto.ApiAccessLogCreateReqDTO;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.exceptions.ExceptionUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.BooleanUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * API 访问日志 Filter
 * <p>
 * 目的：记录 API 访问日志到数据库中
 *
 * @author zhaohuang
 */
@Slf4j
public class ApiAccessLogFilter extends ApiRequestFilter {

    private static final String[] SANITIZE_KEYS = new String[] { "password", "token", "accessToken", "refreshToken" };

    private final String applicationName;

    private final ApiAccessLogApi apiAccessLogApi;

    @Value("${jasolar.debug:false}")
    @Setter
    private boolean debug;

    /** 不需要记录日志的接口地址，主要用于排除查询接口 */ // :/**/*page,/**/list*,/**/get*
    @Value("${jasolar.log.access.excludes:/**/*page,/**/list*,/**/get*}")
    @Setter
    private Set<String> excludes = new HashSet<>();

    private AntPathMatcher pathMatcher = new AntPathMatcher();

    // /** 缓存的不记录日志URL */
    // private Map<String, Boolean> caches = new ConcurrentHashMap<>();

    /** 缓存URL是否不记录日志 */
    private final LoadingCache<String, Boolean> caches = CacheBuilder.newBuilder().maximumSize(2000)
            .refreshAfterWrite(Duration.ofMinutes(1200L)).build(CacheLoader.from(uri -> exclude(uri)));

    /**
     * 是否不需要记录日志的URI
     * 
     * @param uri
     * @return
     */
    protected boolean exclude(String uri) {
        if (StringUtils.isBlank(uri)) {
            return false;
        }
        
        long startTime = System.currentTimeMillis();
        try {
            // 使用普通stream避免parallelStream可能的线程池问题
            boolean result = excludes.stream().anyMatch(pattern -> {
                try {
                    return pathMatcher.match(pattern, uri);
                } catch (Exception e) {
                    log.warn("模式匹配异常: pattern={}, uri={}", pattern, uri, e);
                    return false;
                }
            });
            long duration = System.currentTimeMillis() - startTime;
            log.debug("URI排除模式匹配完成: {}, 结果: {}, 耗时: {}ms", uri, result, duration);
            return result;
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            log.warn("URI排除检查异常: {}, 耗时: {}ms", uri, duration, e);
            return false;
        }
    }

    public ApiAccessLogFilter(WebProperties webProperties, String applicationName, ApiAccessLogApi apiAccessLogApi) {
        super(webProperties);
        this.applicationName = applicationName;
        this.apiAccessLogApi = apiAccessLogApi;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        if (super.shouldNotFilter(request)) {
            return true;
        }

        String method = request.getMethod();
        if (!HttpMethod.POST.matches(method) && !HttpMethod.PUT.matches(method) && !HttpMethod.DELETE.matches(method)
                && !HttpMethod.PATCH.matches(method)) {
            return true;
        }

        // 是否需要排除的URI
        String uri = request.getRequestURI();
        long startTime = System.currentTimeMillis();
        try {
            log.debug("开始检查URI是否需要排除: {}", uri);
            boolean shouldExclude = caches.get(uri);
            long duration = System.currentTimeMillis() - startTime;
            log.debug("URI排除检查完成: {}, 结果: {}, 耗时: {}ms", uri, shouldExclude, duration);
            return shouldExclude;
        } catch (ExecutionException ex) {
            long duration = System.currentTimeMillis() - startTime;
            log.warn("判断请求{}是否需要记录日志时出现异常，耗时: {}ms", uri, duration, ex);
            return false;
        }
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        // 获得开始时间
        LocalDateTime beginTime = LocalDateTime.now();
        // 提前获得参数，避免 XssFilter 过滤处理
        Map<String, String> queryString = ServletUtils.getParamMap(request);
        String requestBody = ServletUtils.isJsonRequest(request) ? ServletUtils.getBody(request) : null;

        // 输出文本日志
        log.debug("HTTP Request {} {}: body={}, parameters={}", request.getMethod(), request.getRequestURL(), requestBody, queryString);
        Exception exception = null;
        try {
            // 继续过滤器
            filterChain.doFilter(request, response);

            // createApiAccessLog(request, beginTime, queryString, requestBody, null);
        } catch (Exception ex) {
            // 异常执行，记录日志
            // createApiAccessLog(request, beginTime, queryString, requestBody, ex);
            exception = ex;
            throw ex;
        } finally {
            // 记录访问日志
            createApiAccessLog(request, beginTime, queryString, requestBody, exception);
        }
    }

    private void createApiAccessLog(HttpServletRequest request, LocalDateTime beginTime, Map<String, String> queryString,
            String requestBody, Exception ex) {
        ApiAccessLogCreateReqDTO accessLog = new ApiAccessLogCreateReqDTO();
        try {
            boolean enable = buildApiAccessLog(accessLog, request, beginTime, queryString, requestBody, ex);
            if (!enable) {
                return;
            }
            apiAccessLogApi.createApiAccessLogAsync(accessLog);
        } catch (Exception th) {
            if (debug) {
                log.error("[createApiAccessLog][url({}) log({}) 发生异常]", request.getRequestURI(), toJsonString(accessLog), th);
            }
        }
    }

    private boolean buildApiAccessLog(ApiAccessLogCreateReqDTO accessLog, HttpServletRequest request, LocalDateTime beginTime,
            Map<String, String> queryString, String requestBody, Exception ex) {
        // 判断：是否要记录操作日志
        HandlerMethod handlerMethod = (HandlerMethod) request.getAttribute(ATTRIBUTE_HANDLER_METHOD);
        ApiAccessLog accessLogAnnotation = null;
        if (handlerMethod != null) {
            accessLogAnnotation = handlerMethod.getMethodAnnotation(ApiAccessLog.class);
            if (accessLogAnnotation != null && BooleanUtil.isFalse(accessLogAnnotation.enable())) {
                return false;
            }
        }

        // 处理用户信息
        LoginUser user = WebFrameworkUtils.getLoginUser();
        accessLog.setUserId(user.getId());
        accessLog.setUserType(user.getUserType());
        accessLog.setUserNo(user.getNo());
        accessLog.setUserName(user.getName());

        // 设置访问结果
        CommonResult<?> result = WebFrameworkUtils.getCommonResult(request);
        if (result != null) {
            accessLog.setResultCode(result.getCode());
            accessLog.setResultMsg(result.getMsg());
        } else if (ex != null) {
            accessLog.setResultCode(GlobalErrorCodeConstants.INTERNAL_SERVER_ERROR.getCode());
            accessLog.setResultMsg(ExceptionUtil.getRootCauseMessage(ex));
        } else {
            accessLog.setResultCode(GlobalErrorCodeConstants.SUCCESS.getCode());
            accessLog.setResultMsg("");
        }
        // 设置请求字段
        accessLog.setTraceId(TracerUtils.getTraceId());
        accessLog.setApplicationName(applicationName);
        accessLog.setRequestUrl(request.getRequestURI());
        accessLog.setRequestMethod(request.getMethod());
        accessLog.setUserAgent(ServletUtils.getUserAgent(request));
        accessLog.setUserIp(ServletUtils.getClientIP(request));
        String[] sanitizeKeys = accessLogAnnotation != null ? accessLogAnnotation.sanitizeKeys() : null;
        Boolean requestEnable = accessLogAnnotation != null ? accessLogAnnotation.requestEnable() : Boolean.TRUE;
        if (!BooleanUtil.isFalse(requestEnable)) { // 默认记录，所以判断 !false
            Map<String, Object> requestParams = MapUtil.<String, Object>builder().put("query", sanitizeMap(queryString, sanitizeKeys))
                    .put("body", sanitizeJson(requestBody, sanitizeKeys)).build();
            accessLog.setRequestParams(toJsonString(requestParams));
        }
        Boolean responseEnable = accessLogAnnotation != null ? accessLogAnnotation.responseEnable() : Boolean.FALSE;
        if (BooleanUtil.isTrue(responseEnable)) { // 默认不记录，默认强制要求 true
            accessLog.setResponseBody(sanitizeJson(result, sanitizeKeys));
        }
        // 持续时间
        accessLog.setBeginTime(beginTime);
        accessLog.setEndTime(LocalDateTime.now());
        accessLog.setDuration((int) LocalDateTimeUtil.between(accessLog.getBeginTime(), accessLog.getEndTime(), ChronoUnit.MILLIS));

        // 操作模块
        if (handlerMethod != null) {
            Tag tagAnnotation = handlerMethod.getBeanType().getAnnotation(Tag.class);
            Operation operationAnnotation = handlerMethod.getMethodAnnotation(Operation.class);
            String operateModule = accessLogAnnotation != null ? accessLogAnnotation.operateModule()
                    : tagAnnotation != null ? CharSequenceUtil.nullToDefault(tagAnnotation.name(), tagAnnotation.description()) : null;
            String operateName = accessLogAnnotation != null ? accessLogAnnotation.operateName()
                    : operationAnnotation != null ? operationAnnotation.summary() : null;
            OperateTypeEnum operateType = accessLogAnnotation != null && accessLogAnnotation.operateType().length > 0
                    ? accessLogAnnotation.operateType()[0]
                    : parseOperateLogType(request);
            accessLog.setOperateModule(operateModule);
            accessLog.setOperateName(operateName);
            accessLog.setOperateType(operateType.getType());
        }
        return true;
    }

    // ========== 解析 @ApiAccessLog、@Swagger 注解 ==========

    private static OperateTypeEnum parseOperateLogType(HttpServletRequest request) {
        RequestMethod requestMethod = RequestMethod.resolve(request.getMethod());
        if (requestMethod == null) {
            return OperateTypeEnum.OTHER;
        }
        switch (requestMethod) {
        case GET:
            return OperateTypeEnum.GET;
        case POST:
            return OperateTypeEnum.CREATE;
        case PUT:
            return OperateTypeEnum.UPDATE;
        case DELETE:
            return OperateTypeEnum.DELETE;
        default:
            return OperateTypeEnum.OTHER;
        }
    }

    // ========== 请求和响应的脱敏逻辑，移除类似 password、token 等敏感字段 ==========

    private static String sanitizeMap(Map<String, ?> map, String[] sanitizeKeys) {
        if (CollUtil.isEmpty(map)) {
            return null;
        }
        if (sanitizeKeys != null) {
            MapUtil.removeAny(map, sanitizeKeys);
        }
        MapUtil.removeAny(map, SANITIZE_KEYS);
        return JsonUtils.toJsonString(map);
    }

    private static String sanitizeJson(String jsonString, String[] sanitizeKeys) {
        if (CharSequenceUtil.isEmpty(jsonString)) {
            return null;
        }
        try {
            JsonNode rootNode = JsonUtils.parseTree(jsonString);
            sanitizeJson(rootNode, sanitizeKeys);
            return JsonUtils.toJsonString(rootNode);
        } catch (Exception e) {
            // 脱敏失败的情况下，直接忽略异常，避免影响用户请求
            log.error("[sanitizeJson][脱敏({}) 发生异常]", jsonString, e);
            return jsonString;
        }
    }

    private static String sanitizeJson(CommonResult<?> commonResult, String[] sanitizeKeys) {
        if (commonResult == null) {
            return null;
        }
        String jsonString = toJsonString(commonResult);
        try {
            JsonNode rootNode = JsonUtils.parseTree(jsonString);
            sanitizeJson(rootNode.get("data"), sanitizeKeys); // 只处理 data 字段，不处理 code、msg 字段，避免错误被脱敏掉
            return JsonUtils.toJsonString(rootNode);
        } catch (Exception e) {
            // 脱敏失败的情况下，直接忽略异常，避免影响用户请求
            log.error("[sanitizeJson][脱敏({}) 发生异常]", jsonString, e);
            return jsonString;
        }
    }

    private static void sanitizeJson(JsonNode node, String[] sanitizeKeys) {
        // 情况一：数组，遍历处理
        if (node.isArray()) {
            for (JsonNode childNode : node) {
                sanitizeJson(childNode, sanitizeKeys);
            }
            return;
        }
        // 情况二：非 Object，只是某个值，直接返回
        if (!node.isObject()) {
            return;
        }
        // 情况三：Object，遍历处理
        Iterator<Map.Entry<String, JsonNode>> iterator = node.properties().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, JsonNode> entry = iterator.next();
            if (ArrayUtil.contains(sanitizeKeys, entry.getKey()) || ArrayUtil.contains(SANITIZE_KEYS, entry.getKey())) {
                iterator.remove();
                continue;
            }
            sanitizeJson(entry.getValue(), sanitizeKeys);
        }
    }

}
