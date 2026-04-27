package com.jasolar.mis.framework.gateway.filter.security;

import org.springframework.web.server.ServerWebExchange;

import com.jasolar.mis.framework.common.security.LoginUser;

import reactor.core.publisher.Mono;

/**
 * 通过token获取用户信息
 * 
 * @author galuo
 * @date 2025-03-24 18:25
 *
 */
public interface LoginUserProvider {

    /**
     * 根据Token获取用户信息
     * 
     * @param exchange ServerWebExchange
     * @param token 登录token
     * @return
     */
    Mono<LoginUser> getUser(ServerWebExchange exchange, String token);


}
