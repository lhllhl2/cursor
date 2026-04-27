package com.jasolar.mis.framework.gateway.filter.logging;

import static cn.hutool.core.date.DatePattern.NORM_DATETIME_MS_FORMATTER;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.apache.skywalking.apm.toolkit.trace.TraceContext;
import org.reactivestreams.Publisher;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.filter.factory.rewrite.CachedBodyOutputMessage;
import org.springframework.cloud.gateway.filter.factory.rewrite.GzipMessageBodyResolver;
import org.springframework.cloud.gateway.filter.factory.rewrite.ModifyRequestBodyGatewayFilterFactory;
import org.springframework.cloud.gateway.support.BodyInserterContext;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ReactiveHttpOutputMessage;
import org.springframework.http.codec.CodecConfigurer;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpRequestDecorator;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.http.server.reactive.ServerHttpResponseDecorator;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserter;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.server.ServerWebExchange;

import org.apache.commons.lang3.StringUtils;
import com.jasolar.mis.framework.common.security.LoginReactiveUtils;
import com.jasolar.mis.framework.common.security.LoginUser;
import com.jasolar.mis.framework.common.util.json.JsonUtils;
import com.jasolar.mis.framework.common.util.monitor.TracerUtils;
import com.jasolar.mis.framework.gateway.util.WebFrameworkUtils;

import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.text.StrPool;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.json.JSONUtil;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * 网关的访问日志过滤器
 * <p>
 * 从功能上，它类似 jasolar-spring-boot-starter-web 的 ApiAccessLogFilter 过滤器
 *
 * @author zhaohuang
 */
@Slf4j
@Component
public class AccessLogFilter implements GlobalFilter, Ordered {

    /** 请求的traceId */
    public static final String TRACE_ID = "traceId";

    @Resource
    private CodecConfigurer codecConfigurer;

    @Resource
    private GzipMessageBodyResolver gzipMessageBodyResolver;

    /** 是否记录body */
    @Value("${jasolar.gateway.logging.response.body.enabled:true}")
    private boolean loggingResponseBody = true;

    /** 不记录body的接口 */
    @Value("${jasolar.gateway.logging.response.body.excludes:/admin-api/system/dict-data/simple-list}")
    private Set<String> loggingResponseBodyExcludes = new HashSet<>();

    /**
     * 打印日志
     *
     * @param gatewayLog 网关日志
     */
    private void writeAccessLog(AccessLog gatewayLog) {
        if (!log.isDebugEnabled()) {
            return;
        }
        // 打印到控制台，方便排查错误
        Map<String, Object> values = MapUtil.newHashMap(15, true); // 手工拼接，保证排序；15 保证不用扩容
        values.put("userId", gatewayLog.getUserId());
        values.put("userNo", gatewayLog.getUserNo());
        values.put("userType", gatewayLog.getUserType());
        values.put("routeId", gatewayLog.getRoute() != null ? gatewayLog.getRoute().getId() : null);
        values.put("schema", gatewayLog.getSchema());
        values.put("requestUrl", gatewayLog.getRequestUrl());
        values.put("queryParams", gatewayLog.getQueryParams().toSingleValueMap());
        values.put("requestBody", JsonUtils.isJson(gatewayLog.getRequestBody()) ? // 保证 body 的展示好看
                JSONUtil.parse(gatewayLog.getRequestBody()) : gatewayLog.getRequestBody());
        values.put("requestHeaders", JsonUtils.toJsonString(gatewayLog.getRequestHeaders().toSingleValueMap()));
        values.put("userIp", gatewayLog.getUserIp());
        if (loggingResponseBody && !loggingResponseBodyExcludes.contains(gatewayLog.getRequestUrl())) {
            values.put("responseBody", JsonUtils.isJson(gatewayLog.getResponseBody()) ? // 保证 body 的展示好看
                    JSONUtil.parse(gatewayLog.getResponseBody()) : gatewayLog.getResponseBody());
        }
        values.put("responseHeaders",
                gatewayLog.getResponseHeaders() != null ? JsonUtils.toJsonString(gatewayLog.getResponseHeaders().toSingleValueMap())
                        : null);
        values.put("httpStatus", gatewayLog.getHttpStatus());
        values.put("startTime", LocalDateTimeUtil.format(gatewayLog.getStartTime(), NORM_DATETIME_MS_FORMATTER));
        values.put("endTime", LocalDateTimeUtil.format(gatewayLog.getEndTime(), NORM_DATETIME_MS_FORMATTER));
        values.put("duration", gatewayLog.getDuration() != null ? gatewayLog.getDuration() + " ms" : null);
        values.put("traceId", gatewayLog.getTraceId());
        log.debug("[writeAccessLog][网关日志：{}]", JsonUtils.toJsonPrettyString(values));
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        // 将 Request 中可以直接获取到的参数，设置到网关日志
        ServerHttpRequest request = exchange.getRequest();

        AccessLog gatewayLog = new AccessLog();
        gatewayLog.setRoute(WebFrameworkUtils.getGatewayRoute(exchange));
        gatewayLog.setSchema(request.getURI().getScheme());
        gatewayLog.setRequestMethod(request.getMethod().name());
        gatewayLog.setRequestUrl(request.getURI().getRawPath());
        gatewayLog.setQueryParams(request.getQueryParams());
        gatewayLog.setRequestHeaders(request.getHeaders());
        gatewayLog.setStartTime(LocalDateTime.now());
        gatewayLog.setUserIp(WebFrameworkUtils.getClientIP(exchange));

        String traceId = TraceContext.traceId();
        if (StringUtils.isBlank(traceId)) {
            // 注入自行生成的traceId
            traceId = UUID.randomUUID().toString().replace(StrPool.DASHED, StringUtils.EMPTY);
            request = request.mutate().header(TracerUtils.HEADER_TRACE_ID, traceId).build();

            exchange.getAttributes().put(TRACE_ID, traceId);
        }
        gatewayLog.setTraceId(traceId);

        // log.info("Gateway Request: {} {}", request.getMethod().name(), request.getURI().toString());

        // 继续 filter 过滤
        MediaType mediaType = request.getHeaders().getContentType();
        if (MediaType.APPLICATION_FORM_URLENCODED.isCompatibleWith(mediaType) || MediaType.APPLICATION_JSON.isCompatibleWith(mediaType)) {
            // 适合 JSON 和 Form 提交的请求
            return filterWithRequestBody(exchange.mutate().request(request).build(), chain, gatewayLog);
        }

        return filterWithoutRequestBody(exchange.mutate().request(request).build(), chain, gatewayLog);
    }

    private Mono<Void> filterWithoutRequestBody(ServerWebExchange exchange, GatewayFilterChain chain, AccessLog accessLog) {
        // 包装 Response，用于记录 Response Body
        ServerHttpResponseDecorator decoratedResponse = recordResponseLog(exchange, accessLog);
        return chain.filter(exchange.mutate().response(decoratedResponse).build()).then(Mono.fromRunnable(() -> writeAccessLog(accessLog))); // 打印日志
    }

    /**
     * 参考 {@link ModifyRequestBodyGatewayFilterFactory} 实现
     * <p>
     * 差别主要在于使用 modifiedBody 来读取 Request Body 数据
     */
    private Mono<Void> filterWithRequestBody(ServerWebExchange exchange, GatewayFilterChain chain, AccessLog gatewayLog) {
        // 设置 Request Body 读取时，设置到网关日志
        // 此处 codecConfigurer.getReaders() 的目的，是解决 spring.codec.max-in-memory-size 不生效
        ServerRequest serverRequest = ServerRequest.create(exchange, codecConfigurer.getReaders());
        Mono<String> modifiedBody = serverRequest.bodyToMono(String.class).flatMap(body -> {
            gatewayLog.setRequestBody(body);
            return Mono.just(body);
        });

        // 创建 BodyInserter 对象
        BodyInserter<Mono<String>, ReactiveHttpOutputMessage> bodyInserter = BodyInserters.fromPublisher(modifiedBody, String.class);
        // 创建 CachedBodyOutputMessage 对象
        HttpHeaders headers = new HttpHeaders();
        headers.putAll(exchange.getRequest().getHeaders());
        // the new content type will be computed by bodyInserter
        // and then set in the request decorator
        headers.remove(HttpHeaders.CONTENT_LENGTH); // 移除
        CachedBodyOutputMessage outputMessage = new CachedBodyOutputMessage(exchange, headers);
        // 通过 BodyInserter 将 Request Body 写入到 CachedBodyOutputMessage 中
        return bodyInserter.insert(outputMessage, new BodyInserterContext()).then(Mono.defer(() -> {
            // 包装 Request，用于缓存 Request Body
            ServerHttpRequest decoratedRequest = requestDecorate(exchange, headers, outputMessage);
            // 包装 Response，用于记录 Response Body
            ServerHttpResponseDecorator decoratedResponse = recordResponseLog(exchange, gatewayLog);
            // 记录普通的
            return chain.filter(exchange.mutate().request(decoratedRequest).response(decoratedResponse).build())
                    .then(Mono.fromRunnable(() -> writeAccessLog(gatewayLog))); // 打印日志

        }));
    }

    /**
     * 记录响应日志 通过 DataBufferFactory 解决响应体分段传输问题。
     */
    private ServerHttpResponseDecorator recordResponseLog(ServerWebExchange exchange, AccessLog accessLog) {
        ServerHttpResponse response = exchange.getResponse();
        response.getHeaders().set(TracerUtils.HEADER_TRACE_ID, accessLog.getTraceId());
        return new ServerHttpResponseDecorator(response) {
            @Override
            public Mono<Void> writeWith(Publisher<? extends DataBuffer> body) {
                if (body instanceof Flux) {
                    DataBufferFactory bufferFactory = response.bufferFactory();
                    // 计算执行时间
                    accessLog.setEndTime(LocalDateTime.now());
                    accessLog.setDuration((int) (LocalDateTimeUtil.between(accessLog.getStartTime(), accessLog.getEndTime()).toMillis()));

                    LoginUser user = LoginReactiveUtils.getLoginUser(exchange);
                    if (user != null) {
                        accessLog.setUserId(user.getId());
                        accessLog.setUserNo(user.getNo());
                        accessLog.setUserType(user.getUserType());
                    }
                    accessLog.setResponseHeaders(response.getHeaders());
                    accessLog.setHttpStatus((HttpStatus) response.getStatusCode());
                    MediaType contentType = response.getHeaders().getContentType();
                    if (log.isDebugEnabled() && contentType != null && contentType.includes(MediaType.APPLICATION_JSON)) {
                        // JSON格式记录返回值到日志
                        return super.writeWith(Flux.from(body).buffer().map(dataBuffers -> {
                            byte[] content = readContent(dataBuffers);
                            String json = new String(isGzip(response) ? gzipMessageBodyResolver.decode(content) : content,
                                    StandardCharsets.UTF_8);
                            accessLog.setResponseBody(json);

                            // 响应
                            return bufferFactory.wrap(content);
                        }));
                    }
                }
                // if body is not a flux. never got there.
                return super.writeWith(body);
            }
        };
    }

    // ========== 参考 ModifyRequestBodyGatewayFilterFactory 中的方法 ==========

    /**
     * 请求装饰器，支持重新计算 headers、body 缓存
     *
     * @param exchange 请求
     * @param headers 请求头
     * @param outputMessage body 缓存
     * @return 请求装饰器
     */
    private ServerHttpRequestDecorator requestDecorate(ServerWebExchange exchange, HttpHeaders headers,
            CachedBodyOutputMessage outputMessage) {
        return new ServerHttpRequestDecorator(exchange.getRequest()) {
            @Override
            public HttpHeaders getHeaders() {
                long contentLength = headers.getContentLength();
                HttpHeaders httpHeaders = new HttpHeaders();
                httpHeaders.putAll(super.getHeaders());
                if (contentLength > 0) {
                    httpHeaders.setContentLength(contentLength);
                } else {
                    httpHeaders.set(HttpHeaders.TRANSFER_ENCODING, "chunked");
                }
                return httpHeaders;
            }

            @Override
            public Flux<DataBuffer> getBody() {
                return outputMessage.getBody();
            }
        };
    }

    // ========== 参考 ModifyResponseBodyGatewayFilterFactory 中的方法 ==========

    private byte[] readContent(List<? extends DataBuffer> dataBuffers) {
        // 合并多个流集合，解决返回体分段传输
        DataBufferFactory dataBufferFactory = new DefaultDataBufferFactory();
        DataBuffer join = dataBufferFactory.join(dataBuffers);
        byte[] content = new byte[join.readableByteCount()];
        join.read(content);
        // 释放掉内存
        DataBufferUtils.release(join);
        return content;
    }

    static final String GZIP = "gzip";

    /**
     * 是否开启了gzip压缩
     * 
     * @param serverHttpResponse
     * @return
     */
    public boolean isGzip(ServerHttpResponse serverHttpResponse) {
        HttpHeaders headers = serverHttpResponse.getHeaders();
        List<String> encodings = ObjectUtil.isNull(headers) ? Collections.emptyList() : headers.getOrEmpty(HttpHeaders.CONTENT_ENCODING);
        if (encodings.isEmpty()) {
            return false;
        }
        return encodings.stream().anyMatch(e -> GZIP.equalsIgnoreCase(e));
    }

}
