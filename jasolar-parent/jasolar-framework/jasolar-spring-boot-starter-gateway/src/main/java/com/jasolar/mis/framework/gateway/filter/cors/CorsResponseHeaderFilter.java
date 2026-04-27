package com.jasolar.mis.framework.gateway.filter.cors;

import java.util.ArrayList;
import java.util.Collections;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import reactor.core.publisher.Mono;

/**
 * 解决 Spring Cloud Gateway 2.x 跨域时，出现重复 Origin 的 BUG
 *
 * 参考文档：<a href="https://blog.csdn.net/zimou5581/article/details/90043178" />
 *
 * @author zhaohuang
 */
@Component
public class CorsResponseHeaderFilter implements GlobalFilter, Ordered {

    @Override
    public int getOrder() {
        // 指定此过滤器位于 NettyWriteResponseFilter 之后
        // 即待处理完响应体后接着处理响应头
        return Ordered.LOWEST_PRECEDENCE;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        return chain.filter(exchange).then(Mono.defer(() -> {
            exchange.getResponse().getHeaders().entrySet().stream().filter(kv -> (kv.getValue() != null && kv.getValue().size() > 1))
                    .filter(kv -> (kv.getKey().equals(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN)
                            || kv.getKey().equals(HttpHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS)))
                    .forEach(kv -> kv.setValue(new ArrayList<>(Collections.singletonList(kv.getValue().get(0)))));
            return chain.filter(exchange);
        }));
    }

}
