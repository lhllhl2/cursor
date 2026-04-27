package com.jasolar.mis.framework.gateway.filter.security;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpMethod;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ServerWebExchange;

import com.fasterxml.jackson.core.type.TypeReference;
import com.jasolar.mis.framework.common.exception.ServerException;
import com.jasolar.mis.framework.common.exception.enums.GlobalErrorCodeConstants;
import com.jasolar.mis.framework.common.pojo.CommonResult;
import com.jasolar.mis.framework.common.redis.RedisKeyConstants;
import com.jasolar.mis.framework.common.security.LoginReactiveUtils;
import com.jasolar.mis.framework.common.security.LoginUser;
import com.jasolar.mis.framework.common.util.json.JsonUtils;
import com.jasolar.mis.module.system.api.permission.PermissionApi;

import cn.hutool.core.text.StrPool;
import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

/**
 * API接口权限拦截过滤器 - Spring Cloud Gateway版本
 */
@Slf4j
public class ApiSecurityFilter extends BaseGlobalFilter implements GlobalFilter, Ordered {

    private final WebClient webClient;

    private final RedissonClient redisson;

    /** 接口URL,默认使用集群内地址 */
    @Value("${jasolar.security.permission.uri:" + PermissionApi.SERVICE_URI + "}")
    private String uri;

    private static final Pattern PATH_VARIABLE_PATTERN = Pattern.compile("\\{[^/]+\\}");

    public ApiSecurityFilter(WebClient webClient, RedissonClient redisson, Collection<String> permitAllUrls) {
        super(permitAllUrls);
        this.redisson = redisson;
        this.webClient = webClient;
    }

    @Override
    protected boolean shouldNotFilter(ServerHttpRequest request) {
        // 兜底策略, 没看到这个的作用
        // Object allPass = redisTemplate.opsForValue().get(RedisKeyConstants.ALL_URLS_PASS);
        // if (allPass != null) {
        // String s = allPass.toString();
        // if (RedisKeyConstants.ALL_URLS_PASS.equals(s)) {
        // return true;
        // }
        // }
        return HttpMethod.OPTIONS.equals(request.getMethod()) || isSignatureRequest(request) || super.shouldNotFilter(request);
    }

    @Override
    public Mono<Void> doFilter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        // 2. 获取当前登录用户
        LoginUser loginUser = LoginReactiveUtils.getLoginUser(exchange);
        if (loginUser == null || !loginUser.isAuthorized()) {
            return unauthorized(exchange);
        }

        return getUserRoleIds(loginUser.getId()).flatMap(roleIds -> {
            if (roleIds.isEmpty()) {
                return handleForbidden(exchange);
            }

            // 4. 构建权限字符串
            String requestPermission = buildPermissionString(request.getMethod().name(), request.getURI().getPath());
            log.info("=====>request url:{}", requestPermission);
            return hasPermissions(roleIds, requestPermission).flatMap(has -> {
                if (!has) {
                    log.info("用户 {} 无权访问: {} {}", loginUser.getId(), request.getMethod(), request.getURI().getPath());
                    return handleForbidden(exchange);
                }
                return chain.filter(exchange);
            });
        });

    }

    /**
     * 获取用户的角色
     * 
     * @param userId
     * @return
     */
    private Mono<Set<Long>> getUserRoleIds(Long userId) {
        String cacheKey = RedisKeyConstants.USER_ROLE_ID_LIST + StrPool.COLON + userId;
        RBucket<Set<Long>> bucket = redisson.getBucket(cacheKey);
        if (bucket.isExists()) {
            Set<Long> roleIds = bucket.get();
            log.info("读取到人员{}缓存的角色: {}", userId, roleIds);
            return Mono.just(roleIds);
        }

        log.info("从接口读取{}的角色", userId);
        Mono<String> body = webClient.get()
                .uri(uri + "/get-roleId-list-by-userId", uriBuilder -> uriBuilder.queryParam("userId", userId).build()).retrieve()
                .bodyToMono(String.class);
        return body.flatMap(json -> {
            CommonResult<Set<Long>> r = StringUtils.isBlank(json) ? null
                    : JsonUtils.parseObject(json, new TypeReference<CommonResult<Set<Long>>>() {
                    });
            Set<Long> roleIds = r != null && r.isSuccess() ? r.getData() : Collections.emptySet();
            log.info("缓存人员{}缓存的角色: {}", userId, roleIds);
            bucket.set(roleIds);
            return Mono.just(roleIds);
        });
    }

    /**
     * 判断指定的角色是否有权限
     * 
     * @param roleIds
     * @param requestPermission
     * @return
     */
    private Mono<Boolean> hasPermissions(Set<Long> roleIds, String requestPermission) {
        Set<Long> uncachedRoleIds = new HashSet<>();
        for (Long roleId : roleIds) {
            String cacheKey = RedisKeyConstants.ROLE_PERMISSION_URLS + StrPool.COLON + roleId;
            RBucket<Set<String>> bucket = redisson.getBucket(cacheKey);
            if (bucket.isExists()) {
                log.info("读取角色{}缓存的菜单权限: {}", uncachedRoleIds);
                Set<String> permissions = bucket.get();
                if (!permissions.isEmpty() && found(permissions, requestPermission)) {
                    return Mono.just(true);
                }
            } else {
                uncachedRoleIds.add(roleId);
            }
        }

        if (uncachedRoleIds.isEmpty()) {
            return Mono.just(false);
        }

        log.info("从接口读取角色的菜单权限: {}", uncachedRoleIds);
        return webClient.get()
                .uri(uri + "/get-interface-list-by-roleIds", uriBuilder -> uriBuilder.queryParam("roleIds", uncachedRoleIds).build())
                .retrieve().bodyToMono(String.class).flatMap(json -> {
                    CommonResult<Map<Long, Set<String>>> r = StringUtils.isBlank(json) ? null
                            : JsonUtils.parseObject(json, new TypeReference<CommonResult<Map<Long, Set<String>>>>() {
                            });
                    Map<Long, Set<String>> all = r != null && r.isSuccess() ? r.getData() : Collections.emptyMap();
                    // 缓存
                    Set<String> allPermissions = new HashSet<>();
                    all.forEach((roleId, permissions) -> {
                        log.info("缓存角色{}的菜单权限", roleId);
                        String cacheKey = RedisKeyConstants.ROLE_PERMISSION_URLS + StrPool.COLON + roleId;
                        RBucket<Set<String>> bucket = redisson.getBucket(cacheKey);
                        bucket.set(permissions);

                        allPermissions.addAll(permissions);
                    });

                    return Mono.just(found(allPermissions, requestPermission));
                });
    }

    /**
     * 在权限中查找
     * 
     * @param permissions
     * @param requestPermission
     * @return
     */
    private boolean found(Set<String> permissions, String requestPermission) {
        if (permissions.contains(requestPermission)) {
            return true;
        }

        for (String permission : permissions) {
            if (isPathPatternMatch(permission, requestPermission)) {
                return true;
            }
        }
        return false;
    }

    // 其他辅助方法...
    private boolean isPathPatternMatch(String permissionPattern, String requestPermission) {
        // 实现与原来相同...
        String[] patternParts = permissionPattern.split("\\|", 2);
        String[] requestParts = requestPermission.split("\\|", 2);

        if (patternParts.length != 2 || requestParts.length != 2) {
            return false;
        }

        String patternMethod = patternParts[0];
        String requestMethod = requestParts[0];
        if (!patternMethod.equals(requestMethod)) {
            return false;
        }

        String patternPath = patternParts[1];
        String requestPath = requestParts[1];

        if (patternPath.contains("{") && patternPath.contains("}")) {
            String antPattern = PATH_VARIABLE_PATTERN.matcher(patternPath).replaceAll("*");
            return pathMatcher.match(antPattern, requestPath);
        }

        return pathMatcher.match(patternPath, requestPath);
    }

    private String buildPermissionString(String method, String uri) {
        return StrUtil.isBlank(method) ? uri : method + "|" + uri;
    }

    private Mono<Void> handleForbidden(ServerWebExchange exchange) {
        return Mono.error(new ServerException(GlobalErrorCodeConstants.FORBIDDEN));
    }

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE - 9999;
    }

}