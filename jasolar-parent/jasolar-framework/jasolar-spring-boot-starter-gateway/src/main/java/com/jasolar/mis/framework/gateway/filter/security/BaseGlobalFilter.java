package com.jasolar.mis.framework.gateway.filter.security;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.server.ServerWebExchange;

import com.jasolar.mis.framework.common.exception.ErrorCode;
import com.jasolar.mis.framework.common.exception.ServerException;
import com.jasolar.mis.framework.common.exception.enums.GlobalErrorCodeConstants;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

/**
 * 可以指定路径不做过滤
 * 
 * @author galuo
 * @date 2025-05-14 10:18
 *
 */
@Slf4j
public abstract class BaseGlobalFilter implements GlobalFilter, InitializingBean {

    /** 全局的系统白名单路径 */
    public static final Set<String> WHITELIST_PATHS = new HashSet<>(
            Arrays.asList("/**/login/**", "/**/logout/**", "/**/login", "/**/logout", "/**/refresh-token", "/swagger-ui/**",
                    "/**/v3/api-docs/**", "/**/doc.html", "/**/webjars/**", "/**/favicon.ico", "/**/error", "/**/actuator/**"));

    /** 其他服务请求的token */
    public static final String HEADER_AUTH_TOKEN = "X-Auth-Token";
    /** 其他服务请求的clientId */
    public static final String HEADER_CLIENT_ID = "X-Client-Id";

    /** 配置的不需要token的路径 */
    @Getter
    protected final Set<String> permitAllUrls = new LinkedHashSet<>();

    /** 缓存已判断的URI */
    @Getter
    protected final Map<String, Boolean> permitedUris = new ConcurrentHashMap<>(1024);

    /** 用于匹配路径 */
    protected final AntPathMatcher pathMatcher = new AntPathMatcher();

    public BaseGlobalFilter() {
        super();
    }

    public BaseGlobalFilter(Collection<String> permitAllUrls) {
        super();
        this.permitAllUrls.addAll(permitAllUrls);
    }

    /**
     * token过期返回401
     * 
     * @param exchange
     * @return
     */
    public static Mono<Void> unauthorized(ServerWebExchange exchange) {
        return error(exchange, HttpStatus.UNAUTHORIZED, GlobalErrorCodeConstants.UNAUTHORIZED);
    }

    /**
     * token过期返回401
     * 
     * @param exchange
     * @return
     */
    public static Mono<Void> error(ServerWebExchange exchange, HttpStatus status, ErrorCode errorCode) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(status);
        return Mono.error(new ServerException(errorCode));
    }

    /**
     * 指定的请求是否不需要过滤, 返回true则不需要处理
     * 
     * @param ServerHttpRequest 请求对象
     * @return 返回true则表示直接放行,不做{@link #doFilter(ServerWebExchange, GatewayFilterChain)}方法
     */
    protected boolean shouldNotFilter(ServerHttpRequest request) {
        return permit(request);
    }

    /**
     * 是否配置了放行的URL请求
     * 
     * @param request
     * @return 请求的URI包含在{@link #permitAllUrls}中则返回true,否则返回false
     */
    protected boolean permit(ServerHttpRequest request) {
        String requestURI = request.getURI().getPath();
        if (permitedUris.containsKey(requestURI)) {
            return permitedUris.get(requestURI);
        }

        boolean permit = permitAllUrls.parallelStream().anyMatch(path -> pathMatcher.match(path, requestURI));
        permitedUris.put(requestURI, permit);
        return permit;
    }

    /**
     * 指定的请求是否是外部系统清秀, 返回true则是
     *
     * @param request 请求对象
     * @return 返回true则为外部系统调用
     */
    protected boolean isSignatureRequest(ServerHttpRequest request) {
        HttpHeaders headers = request.getHeaders();
        return headers.containsKey(HEADER_AUTH_TOKEN) && headers.containsKey(HEADER_CLIENT_ID);
    }

    /**
     * 具体的过滤器执行方法
     * 
     * @param exchange
     * @param chain
     * @return
     */
    protected abstract Mono<Void> doFilter(ServerWebExchange exchange, GatewayFilterChain chain);

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        if (this.shouldNotFilter(exchange.getRequest())) {
            // log.info("{} {} 不需要处理的请求，直接放行: {}", exchange.getAttributeOrDefault(AccessLogFilter.TRACE_ID, ""), this.getClass(),
            // exchange.getRequest().getURI().getPath());
            return chain.filter(exchange);
        }

        // log.info("{} {} 需要处理的请求: {}", exchange.getAttributeOrDefault(AccessLogFilter.TRACE_ID, ""), this.getClass(),
        // exchange.getRequest().getURI().getPath());
        return this.doFilter(exchange, chain);
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        permitAllUrls.addAll(WHITELIST_PATHS);
    }
}
