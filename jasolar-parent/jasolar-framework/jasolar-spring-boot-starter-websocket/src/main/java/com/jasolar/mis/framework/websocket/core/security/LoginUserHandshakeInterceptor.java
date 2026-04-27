package com.jasolar.mis.framework.websocket.core.security;

import java.util.Map;

import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.security.core.Authentication;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.server.HandshakeInterceptor;

import com.jasolar.mis.framework.common.security.LoginUser;
import com.jasolar.mis.framework.security.core.filter.TokenAuthenticationFilter;
import com.jasolar.mis.framework.security.core.util.SecurityFrameworkUtils;
import com.jasolar.mis.framework.websocket.core.util.WebSocketFrameworkUtils;

/**
 * 登录用户的 {@link HandshakeInterceptor} 实现类
 *
 * 流程如下：
 * 1. 前端连接 websocket 时，会通过拼接 ?token={token} 到 ws:// 连接后，这样它可以被 {@link TokenAuthenticationFilter} 所认证通过
 * 2. {@link LoginUserHandshakeInterceptor} 负责把 {@link LoginUser} 添加到 {@link WebSocketSession} 中
 *
 * @author zhaohuang
 */
public class LoginUserHandshakeInterceptor implements HandshakeInterceptor {

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler,
            Map<String, Object> attributes) {
        Authentication auth = SecurityFrameworkUtils.getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof LoginUser user) {
            WebSocketFrameworkUtils.setLoginUser(user, attributes);
        }
        return true;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Exception exception) {
        // do nothing
    }

}
