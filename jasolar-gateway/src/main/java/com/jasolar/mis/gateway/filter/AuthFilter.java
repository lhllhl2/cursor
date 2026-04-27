package com.jasolar.mis.gateway.filter;

import com.alibaba.fastjson.JSON;
import com.jasolar.mis.framework.common.exception.ServerException;
import com.jasolar.mis.framework.common.exception.ServiceException;
import com.jasolar.mis.framework.common.exception.enums.AuthErrorCodeConstants;
import com.jasolar.mis.framework.common.exception.enums.GlobalErrorCodeConstants;
import com.jasolar.mis.framework.common.pojo.CommonResult;
import com.jasolar.mis.framework.common.security.LoginUser;
import com.jasolar.mis.framework.common.security.LoginUserUtils;
import com.jasolar.mis.gateway.config.PermissionConfig;
import com.jasolar.mis.module.system.api.oauth.AuthApi;
import com.jasolar.mis.module.system.api.oauth.OauthSsoApi;
import com.jasolar.mis.module.system.api.oauth.OutClientApi;
import com.jasolar.mis.module.system.oauth.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.CollectionUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

/**
 * Description:
 * Author : Zhou Hai
 * Date : 17/07/2025 14:15
 * Version : 1.0
 */
@Slf4j
@Component
public class AuthFilter implements GlobalFilter, Ordered, GatewayFilter {

    private final AntPathMatcher antPathMatcher = new AntPathMatcher();

    public static final String AUTH = "Authorization";

    public static final String BASIC = "Basic ";

    private final static String SECURITY_HEADER = "Strict-Transport-Security";

    private final static String SECURITY_VALUE = "max-age=31536000 ; includeSubDomains";

    private static final long MAX_TIME_DIFF = 5 * 60 * 1000; // 30秒

    @Autowired
    private PermissionConfig permissionConfig;

    @Autowired
    private AuthApi authApi;

    @Autowired
    private OutClientApi outClientApi;

    @Autowired
    private OauthSsoApi oauthSsoApi;



    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        //请求的uri
        String requestUri = request.getPath().pathWithinApplication().value();
        //请求的方法
        String method = request.getMethod().toString();
        //请求的token
        String token = request.getHeaders().getFirst(AUTH);
        log.info("Gateway permission filter start, requestUri={}, method={}", requestUri, method);
        //创建request builder对象 builder
        ServerHttpRequest newRequest = request.mutate().build();
        try {
            setTokenInResponseHeader(exchange.getResponse().getHeaders(), token);

            // 黑名单
            if (isMatch(requestUri, permissionConfig.getBlackUrls())) {
                log.info("Authority Check: BlackUrls url->[{}] ", requestUri);
                throw new ServerException(GlobalErrorCodeConstants.UNAUTHORIZED);

            } else if (isMatch(requestUri, permissionConfig.getIgnoreUrls())) { //不需要鉴权的接口
                log.info("Authority Check:IgnoreUrl url->[{}] ", requestUri);

                return chain.filter(exchange.mutate().request(newRequest).build());
            } else if (isMatch(requestUri, permissionConfig.getOpenUrls())) {  //外部接口预留
                String pw = "esb_user:esb_password";
                String base64 = Base64.getEncoder().encodeToString(pw.getBytes(StandardCharsets.UTF_8));
                if(!Objects.equals(base64,parseToken(request))) {
                    throw new ServiceException(GlobalErrorCodeConstants.UNAUTHORIZED);
                }
                return chain.filter(exchange.mutate().request(newRequest).build());
            } else if(isMatch(requestUri, permissionConfig.getOuterUrls())){
                String clientName = request.getHeaders().getFirst("X-Client");
                log.info("clientName -> {}",clientName);

                String timestamp = request.getHeaders().getFirst("X-Timestamp");
                log.info("timestamp -> {}",timestamp);

                String sign = parseToken(request);
                log.info("sign -> {}",sign);

                if(StringUtils.isEmpty(clientName) || StringUtils.isEmpty(timestamp) || StringUtils.isEmpty(sign)){
                    throw new ServiceException("6003","参数不正确");
                }
                long time = Long.parseLong(timestamp);
                long now =  System.currentTimeMillis();
                if(Math.abs(now - time) > MAX_TIME_DIFF){
                    throw new ServiceException("6004","请求超时");
                }

                ClientInfoResponse outClient = getOutClient(clientName);
                if(Objects.isNull(outClient)){
                    throw new ServiceException("6005","该客户系统未注册");
                }
                String key = clientName + ":" + outClient.getClientSecret() + ":" + timestamp;
                String base64 = Base64.getEncoder().encodeToString(key.getBytes(StandardCharsets.UTF_8));
                if(!Objects.equals(base64,parseToken(request))) {
                    throw new ServiceException("6006","鉴权失败！");
                }
                return chain.filter(exchange.mutate().request(newRequest).build());

            }else {
                log.info("Authority Check:Normal url->[{}] ", requestUri);
                log.info("Authority Check:Normal token->[{}] ", token);

                // 本地权限校验
                VerifyResponse verifyResponse = verify(requestUri, method, token);
                newRequest = setUserInfo(exchange,verifyResponse.getUser());
            }
        }catch (ServerException se){
            log.error("ServerException occurred when processing request: {}", requestUri, se);
            return toSsoLogin(exchange);
        } catch (Exception e) {
            // 内部出错，直接返回错误信息
            log.error("Exception occurred when processing request: {}", requestUri, e);
            return response(exchange,CommonResult.error(GlobalErrorCodeConstants.ERROR),token);
        }
        log.info("Routing request to backend service, requestUri={}", requestUri);
        return chain.filter(exchange.mutate().request(newRequest).build());
    }

    private Mono<Void> toSsoLogin(ServerWebExchange exchange) {
        CompletableFuture<CommonResult<SsoLogInUrlInfo>> resultFuture = CompletableFuture.supplyAsync(() -> oauthSsoApi.getSsoLoginRedirectUrl());
        CommonResult<SsoLogInUrlInfo> result = null;
        try {
             result = resultFuture.get();
        } catch (Exception e) {
            log.info("get sso url error",e);
            throw new ServiceException(AuthErrorCodeConstants.AUTH_SSO_URL_GET);
        }

        if(Objects.isNull(result)
                || Objects.isNull(result.getData())
                || !Objects.equals(result.getCode(), GlobalErrorCodeConstants.SUCCESS.getCode())){
            throw new ServiceException(AuthErrorCodeConstants.AUTH_SSO_URL_GET);
        }
        return location401(exchange,result.getData().getRedirectUri());
    }

    private ServerHttpRequest setUserInfo(ServerWebExchange exchange, UserResponse user) {

        //将用户信息设置到Header里
        ServerHttpRequest.Builder mutate = exchange.getRequest().mutate();
        LoginUser loginUser = LoginUser.builder()
                .id(user.getId())
                .no(user.getUserName())
                .name(user.getDisplayName())
                .build();
        mutate.header(LoginUserUtils.HEADER_LOGIN_USER,LoginUserUtils.toRequestHeader(loginUser));
        return mutate.build();
    }


    private ClientInfoResponse getOutClient(String clientName) throws ExecutionException, InterruptedException {
        if(StringUtils.isEmpty(clientName)){
            throw new ServiceException("6001","clientName 不能为空");
        }
        CompletableFuture<CommonResult<ClientInfoResponse>> resultFuture = CompletableFuture.supplyAsync(() -> outClientApi.getClientInfo(clientName));
        CommonResult<ClientInfoResponse> result = resultFuture.get();
        if(Objects.isNull(result)
                || !Objects.equals(result.getCode(), GlobalErrorCodeConstants.SUCCESS.getCode())
                || Objects.isNull(result.getData())
        ){
            throw new ServiceException(AuthErrorCodeConstants.AUTH_VERIFY_ERROR);
        }
        return result.getData();
    }


    private VerifyResponse verify(String url,String method,String token) throws ExecutionException, InterruptedException {

        if (StringUtils.isEmpty(url) || StringUtils.isEmpty(method)) {
            throw new ServerException(AuthErrorCodeConstants.LOGIN_STATE_UN_NORMAL);
        }

        if(StringUtils.isEmpty(token)){
            throw new ServerException(AuthErrorCodeConstants.LOGIN_TOKEN_EMPTY);
        }

        VerifyRequest verifyRequest = VerifyRequest.builder()
                .url(url)
                .method(method)
                .build();
        CompletableFuture<CommonResult<VerifyResponse>> resultFuture = CompletableFuture.supplyAsync(() -> authApi.verify(verifyRequest,token));
        CommonResult<VerifyResponse> vfResult = resultFuture.get();
        if(Objects.isNull(vfResult)
                || !Objects.equals(vfResult.getCode(), GlobalErrorCodeConstants.SUCCESS.getCode())
                || Objects.isNull(vfResult.getData())
        ){
            throw new ServerException(AuthErrorCodeConstants.AUTH_VERIFY_ERROR);
        }
        return vfResult.getData();
    }


    private String parseToken(ServerHttpRequest request) {
        String token = request.getHeaders().getFirst(AUTH);
        if (StringUtils.isNotBlank(token) && token.startsWith(BASIC)) {
            token = token.replaceFirst(BASIC, StringUtils.EMPTY);
        }
        return token;
    }


    /**
     * redirect to url
     *
     * @param exchange
     * @param redirectUrl
     * @return Mono<Void>
     */
    private Mono<Void> location401(ServerWebExchange exchange, String redirectUrl) {
        ServerHttpResponse response = exchange.getResponse();
        if (!response.isCommitted()) {
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            response.getHeaders().add(HttpHeaders.LOCATION, redirectUrl);
            response.getHeaders().add(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS, HttpHeaders.LOCATION);
            response.getHeaders().add(HttpHeaders.CONTENT_TYPE, "text/plain;charset=UTF-8");
            return response.setComplete();
        }
        return Mono.empty();
    }







    /**
     * check the uri is match
     *
     * @param requestUri request uri
     * @param urls
     * @return Boolean
     */
    private boolean isMatch(String requestUri, List<String> urls) {
        if (CollectionUtils.isEmpty(urls) || StringUtils.isEmpty(requestUri)) {
            return false;
        }
        for (String url : urls) {
            if (antPathMatcher.match(url, requestUri)) {
                return true;
            }
        }
        return false;
    }

    private static void setTokenInResponseHeader(HttpHeaders httpHeaders, String token) {
        // 这里开始是个大坑
        if (!StringUtils.isEmpty(token)) {
            httpHeaders.set(AUTH, token);
        }
    }


    private Mono<Void> response(ServerWebExchange exchange, Object object, String token) {
        //设置应答体
        String json = JSON.toJSONString(object);
        DataBuffer buffer = exchange.getResponse().bufferFactory().wrap(json.getBytes(StandardCharsets.UTF_8));
        //设置响应的http code
        exchange.getResponse().setStatusCode(HttpStatus.OK);
        //设置Header信息
        HttpHeaders httpHeaders = exchange.getResponse().getHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);
        //设置token到返回的header
        setTokenInResponseHeader(httpHeaders, token);
        httpHeaders.set(SECURITY_HEADER, SECURITY_VALUE);
        return exchange.getResponse().writeWith(Flux.just(buffer));
    }



    @Override
    public int getOrder() {
        return Integer.MIN_VALUE;
    }
}
