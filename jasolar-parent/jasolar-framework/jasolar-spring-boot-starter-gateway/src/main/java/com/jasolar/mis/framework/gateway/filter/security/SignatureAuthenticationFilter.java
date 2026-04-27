package com.jasolar.mis.framework.gateway.filter.security;

import static com.jasolar.mis.framework.common.exception.enums.GlobalErrorCodeConstants.UNAUTHORIZED;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.server.ServerWebExchange;

import com.jasolar.mis.framework.common.exception.ServerException;
import com.jasolar.mis.framework.common.security.LoginReactiveUtils;
import com.jasolar.mis.framework.common.security.LoginUser;
import com.jasolar.mis.framework.common.security.LoginUserUtils;
import com.jasolar.mis.framework.common.security.SecurityProperties;

import reactor.core.publisher.Mono;

/**
 * 外部系统签名认证
 * 1. 验证通过时，将 userId、userType、tenantId 通过 Header 转发给服务
 * 2. 验证不通过，还是会转发给服务。因为，接口是否需要登录的校验，还是交给服务自身处理
 *
 * @author andou
 */
public class SignatureAuthenticationFilter extends BaseGlobalFilter implements GlobalFilter, Ordered {

    private final SignatureUserProvider signatureUserProvider;

    public SignatureAuthenticationFilter(SecurityProperties props, SignatureUserProvider serveProvider) {
        super(props.getPermitAllUrls());
        this.signatureUserProvider = serveProvider;
    }

    @Override
    protected boolean shouldNotFilter(ServerHttpRequest request) {
        return !isSignatureRequest(request);
    }

    @Override
    protected Mono<Void> doFilter(ServerWebExchange exchange, GatewayFilterChain chain) {
        return signatureUserProvider.getUser(exchange).flatMap(user -> {
            if (user.isExpired()) {
                if (shouldNotFilter(exchange.getRequest())) {
                    // 视为没有登录用户进行访问
                    ServerHttpRequest request = exchange.getRequest();
                    request = request.mutate().headers(headers -> {
                        headers.set(LoginUserUtils.HEADER_TOKEN_REMOVED, Boolean.TRUE.toString());
                    }).build();
                    return chain.filter(exchange.mutate().request(request).build());
                } else {
                    ServerHttpResponse response = exchange.getResponse();
                    response.setStatusCode(HttpStatus.UNAUTHORIZED);
                    return Mono.error(new ServerException(UNAUTHORIZED.getCode(), user.getName()));
                }
            }

            return this.doFilter(exchange, chain, user);
        });
    }

    public Mono<Void> doFilter(final ServerWebExchange exchange, GatewayFilterChain chain, LoginUser user) {
        // 2.1 有用户，则设置登录用户
        LoginReactiveUtils.setLoginUser(exchange, user);

        // 2.2 将 user 并设置到 login-user 的请求头，使用 json 存储值
        ServerHttpRequest request = exchange.getRequest();
        request = request.mutate().headers(headers -> {
            headers.set(LoginUserUtils.HEADER_LOGIN_USER_TYPE, Integer.toString(user.getUserType()));
            headers.set(LoginUserUtils.HEADER_LOGIN_USER, LoginUserUtils.toRequestHeader(user));
        }).build();

        return chain.filter(exchange.mutate().request(request).build());
    }

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE - 10000;
    }

}
