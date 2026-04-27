package com.jasolar.mis.framework.gateway.filter.security;

import org.apache.commons.lang3.StringUtils;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.server.ServerWebExchange;

import com.jasolar.mis.framework.common.exception.enums.GlobalErrorCodeConstants;
import com.jasolar.mis.framework.common.security.LoginReactiveUtils;
import com.jasolar.mis.framework.common.security.LoginUser;
import com.jasolar.mis.framework.common.security.LoginUserUtils;
import com.jasolar.mis.framework.common.security.SecurityProperties;
import com.jasolar.mis.framework.gateway.AuthWhitelist;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

/**
 * Token 过滤器，验证 token 的有效性
 * 1. 验证通过时，将 userId、userType、tenantId 通过 Header 转发给服务
 * 2. 验证不通过，还是会转发给服务。因为，接口是否需要登录的校验，还是交给服务自身处理.
 * 当请求中有token参数时, 只有当token过期才会调用 {@link #shouldNotFilter(ServerHttpRequest)} 判断是否需要过滤.
 *
 * @author zhaohuang
 */
@RequiredArgsConstructor
public class TokenAuthenticationFilter extends BaseGlobalFilter implements GlobalFilter, Ordered {

    private final SecurityProperties props;

    private final LoginUserProvider userProvider;

    private final AuthWhitelist authWhitelist;

    // public TokenAuthenticationFilter(SecurityProperties props, LoginUserProvider userProvider) {
    // super(props.getPermitAllUrls());
    // this.props = props;
    // this.userProvider = userProvider;
    // }

    @Override
    protected boolean shouldNotFilter(ServerHttpRequest request) {
        // 签名请求或者无token则直接过滤
        return isSignatureRequest(request)
                || StringUtils.isBlank(LoginReactiveUtils.getToken(request, props.getTokenHeader(), props.getTokenParameter()));
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        request = request.mutate().headers(headers -> headers.remove(LoginUserUtils.HEADER_LOGIN_USER)).build();
        return super.filter(exchange.mutate().request(request).build(), chain);
    }

    @Override
    protected Mono<Void> doFilter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String token = LoginReactiveUtils.getToken(exchange.getRequest(), props.getTokenHeader(), props.getTokenParameter());
        if (props.getMockEnable()) {
            // mock token的处理
            LoginUser user = LoginUserUtils.mock(token, props.getMockSecret());
            if (user != null) {
                return this.doFilter(exchange, chain, user);
            }
        }

        return userProvider.getUser(exchange, token).flatMap(user -> {
            if (!user.isExpired()) {
                // 获取到登录用户
                return this.doFilter(exchange, chain, user);
            }

            if (this.permit(exchange.getRequest())) {
                // 视为没有登录用户进行访问, 删除token
                ServerHttpRequest request = exchange.getRequest();
                request = request.mutate().headers(headers -> {
                    headers.remove(LoginUserUtils.HEADER_LOGIN_USER);
                    headers.remove(props.getTokenHeader());
                    headers.set(LoginUserUtils.HEADER_TOKEN_REMOVED, Boolean.TRUE.toString());
                }).build();
                return chain.filter(exchange.mutate().request(request).build());
            }

            // token过期并且是需要过滤的path
            return unauthorized(exchange);

        });
    }

    public Mono<Void> doFilter(final ServerWebExchange exchange, GatewayFilterChain chain, LoginUser user) {
        if (authWhitelist.isEnabled() && !authWhitelist.accept(user.getNo())) {
            // 冒烟测试期间不允许登录
            return error(exchange, HttpStatus.OK, GlobalErrorCodeConstants.MAINTENANCE);
        }

        // 2.1 有用户，则设置登录用户
        LoginReactiveUtils.setLoginUser(exchange, user);

        // 2.2 将 user 并设置到 login-user 的请求头，使用 json 存储值
        // ServerWebExchange newExchange = exchange.mutate().request(builder -> {
        // builder.header(LoginUserUtils.HEADER_LOGIN_USER_TYPE, Integer.toString(user.getUserType()));
        // builder.header(LoginUserUtils.HEADER_LOGIN_USER, LoginUserUtils.toRequestHeader(user));
        // }).build();

        ServerHttpRequest request = exchange.getRequest();
        request = request.mutate().headers(headers -> {
            headers.set(LoginUserUtils.HEADER_LOGIN_USER_TYPE, Integer.toString(user.getUserType()));
            headers.set(LoginUserUtils.HEADER_LOGIN_USER, LoginUserUtils.toRequestHeader(user));
        }).build();

        return chain.filter(exchange.mutate().request(request).build());
    }

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE - 100000;
    }

}
