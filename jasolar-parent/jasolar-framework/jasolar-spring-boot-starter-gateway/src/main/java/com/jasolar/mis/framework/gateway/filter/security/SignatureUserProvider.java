package com.jasolar.mis.framework.gateway.filter.security;

import com.jasolar.mis.framework.common.security.LoginUser;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * 外部系统获取用户信息
 * 
 * @author galuo
 * @date 2025-05-16 18:25
 *
 */
public interface SignatureUserProvider {

    /**
     * 外部系统获取用户信息
     * 
     * @param exchange ServerWebExchange
     * @return
     */
    Mono<LoginUser> getUser(ServerWebExchange exchange);


}
