package com.jasolar.mis.framework.common.security;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

import org.apache.commons.lang3.StringUtils;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.server.ServerWebExchange;

import com.jasolar.mis.framework.common.util.json.JsonUtils;

/**
 * Reactive的项目的工具类
 * 
 * @author galuo
 * @date 2025-03-18 10:56
 *
 */
public interface LoginReactiveUtils {

    /**
     * 从请求中获取登录token
     * 
     * @param request 请求
     * @param headerName 获取token的请求头,优先
     * @param parameterName 获取token的请求QueryString参数,没有header时从这里查找
     * @return
     */
    public static String getToken(ServerHttpRequest request, String headerName, String parameterName) {
        String removedHeader = request.getHeaders().getFirst(LoginUserUtils.HEADER_TOKEN_REMOVED);
        boolean removed = Boolean.parseBoolean(removedHeader);
        if (removed) {
            // 没有token
            return null;
        }
        String token = request.getHeaders().getFirst(headerName);
        if (StringUtils.isBlank(token)) {
            token = request.getQueryParams().getFirst(parameterName);
        }
        if (StringUtils.isBlank(token)) {
            return null;
        }
        token = token.trim();
        // 去除 Token 中带的 Bearer
        if (StringUtils.startsWithIgnoreCase(token, LoginUserUtils.AUTHORIZATION_BEARER)) {
            return token.substring(LoginUserUtils.AUTHORIZATION_BEARER.length()).trim();
        }
        return token;
    }

    /**
     * 从请求中查找登录用户信息.
     * 
     * @param exchange ServerWebExchange
     * @return 登录信息,没有登录则返回null
     */
    static LoginUser getLoginUser(ServerWebExchange exchange) {
        LoginUser user = (LoginUser) exchange.getAttribute(LoginUserUtils.ATTR_LOGIN_USER);
        if (user != null) {
            return user;
        }
        String header = exchange.getRequest().getHeaders().getFirst(LoginUserUtils.HEADER_LOGIN_USER);
        if (StringUtils.isBlank(header)) {
            return null;
        }
        String json = URLDecoder.decode(header, StandardCharsets.UTF_8); // 解码，解决中文乱码问题
        user = JsonUtils.parseObject(json, LoginUser.class);
        setLoginUser(exchange, user);
        return user;
    }

    /**
     * 将登陆信息写入到请求属性中
     * 
     * @param exchange ServerWebExchange
     * @param user 登录信息
     */
    static void setLoginUser(ServerWebExchange exchange, LoginUser user) {
        exchange.getAttributes().put(LoginUserUtils.ATTR_LOGIN_USER, user);
        exchange.getAttributes().put(LoginUserUtils.ATTR_LOGIN_USER_TYPE, user.getUserType());
    }
}
