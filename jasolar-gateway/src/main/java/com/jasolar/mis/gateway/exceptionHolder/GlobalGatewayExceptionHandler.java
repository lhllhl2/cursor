package com.jasolar.mis.gateway.exceptionHolder;

import com.jasolar.mis.framework.common.util.json.JsonUtils;
import com.jasolar.mis.gateway.resp.GlobalResponse;
import io.netty.channel.ConnectTimeoutException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import javax.naming.ServiceUnavailableException;
import java.net.ConnectException;
import java.net.NoRouteToHostException;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;

/**
 * Description:
 * Author : Zhou Hai
 * Date : 17/07/2025 18:34
 * Version : 1.0
 */
@Slf4j
@Component
@Order(-1) // 优先级高于默认的 DefaultErrorWebExceptionHandler
public class GlobalGatewayExceptionHandler implements ErrorWebExceptionHandler {


    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR); // 统一返回400
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        int code;
        String message;

        if (ex instanceof ResponseStatusException) {
            ResponseStatusException rse = (ResponseStatusException) ex;
            code = rse.getStatusCode().value();
            if (code == 504 && rse.getMessage().contains("timeout")) {
                message = "网关请求超时，请稍后重试";
            } else {
                message = "请求路径不存在";
            }
        } else if (ex instanceof ConnectTimeoutException) {
            code = 504;
            message = "后端服务响应超时";
        } else if (ex instanceof ServiceUnavailableException) {
            code = 503;
            message = "服务不可用";
        } else if (ex instanceof NoRouteToHostException) {
            code = 503;
            message = "找不到对应的微服务";
        } else if (ex instanceof ConnectException) {
            code = 503;
            message = "无法连接到目标微服务";
        } else if (ex instanceof UnknownHostException) {
            code = 503;
            message = "目标微服务地址无法解析";
        } else {
            code = 500;
            message = "Gateway error ...";
        }
        log.error("Gateway exception occurred - code:{}, msg:{}, request path:{}", 
                 code, ex.getMessage(), 
                 exchange.getRequest().getPath().pathWithinApplication().value(), ex);

        GlobalResponse globalResponse = GlobalResponse.builder()
                .code(code)
                .msg(message)
                .build();

        String resp = JsonUtils.toJsonString(globalResponse);
        DataBuffer buffer = response.bufferFactory()
                .wrap(resp.getBytes(StandardCharsets.UTF_8));

        return response.writeWith(Mono.just(buffer));
    }
}
