package com.jasolar.mis.framework.gateway.filter.security;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import com.jasolar.mis.framework.common.enums.UserTypeEnum;
import com.jasolar.mis.framework.common.redis.RedisKeyConstants;
import com.jasolar.mis.framework.common.security.LoginUser;

import cn.hutool.core.text.StrPool;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class SignatureClientProvider implements SignatureUserProvider {
    // 常量定义
    private static final String HEADER_AUTH_TOKEN = BaseGlobalFilter.HEADER_AUTH_TOKEN; // "X-Auth-Token";
    private static final String HEADER_CLIENT_ID = BaseGlobalFilter.HEADER_CLIENT_ID; // "X-Client-Id";
    private static final String HEADER_SIGN_UUID = "X-Sign-UUID";
    private static final String HEADER_SIGN_TIMESTAMP = "X-Sign-Timestamp";
    private static final String HEADER_SIGN_TYPE = "X-Sign-Type";
    private static final long UNAUTHORIZED_USER_ID = -100L;
    private static final String UNAUTHORIZED_USER_NO = "UNAUTHORIZED";

    /** token过期等情况返回的数据 */
    public static final LoginUser EXPIRED = LoginUser.builder().expiresTime(LocalDateTime.now().minusDays(10)).build();

    private final RedissonClient redisson;

    public SignatureClientProvider(RedissonClient redisson) {
        this.redisson = redisson;
    }

    @Override
    public Mono<LoginUser> getUser(ServerWebExchange exchange) {
        // 1. 获取并验证必填参数
        List<String> missingParams = validateRequiredHeaders(exchange);
        if (!missingParams.isEmpty()) {
            log.error("请求：{},缺少必要的认证参数;{}",  exchange.getRequest().getPath(), String.join(", ", missingParams));
            return buildErrorResponse("缺少必要的认证参数: " + String.join(", ", missingParams));
        }

        // 2. 获取各个header值
        String authToken = exchange.getRequest().getHeaders().getFirst(HEADER_AUTH_TOKEN);
        String clientId = exchange.getRequest().getHeaders().getFirst(HEADER_CLIENT_ID);
        String signUuid = exchange.getRequest().getHeaders().getFirst(HEADER_SIGN_UUID);
        String timestamp = exchange.getRequest().getHeaders().getFirst(HEADER_SIGN_TIMESTAMP);
        String signType = exchange.getRequest().getHeaders().getFirst(HEADER_SIGN_TYPE);

        // 3. 验证时间戳
        try {
            // 时间戳两分钟之内
            long ts = Long.parseLong(timestamp);
            if (Math.abs(System.currentTimeMillis() - ts) > TimeUnit.MINUTES.toMillis(2)) {
                log.error("请求：{},请求时间戳已过期;",  exchange.getRequest().getPath());
                return buildErrorResponse("请求时间戳已过期");
            }
        } catch (NumberFormatException e) {
            log.error("请求：{},无效的时间戳格式;",  exchange.getRequest().getPath());
            return buildErrorResponse("无效的时间戳格式");
        }

        // 4. 验证UUID是否重复
        if (!checkAndCacheUuid(signUuid)) {
            log.error("请求：{},请求UUID已被使用;",  exchange.getRequest().getPath());
            return buildErrorResponse("请求UUID已被使用");
        }

        // 5. 获取密钥
        String secret = getClientSecret(clientId);
        if (secret == null) {
            log.error("请求：{},无效的客户端ID;",  exchange.getRequest().getPath());
            return buildErrorResponse("无效的客户端ID");
        }

        // 6. 验证签名
        if (!validateSignature(authToken, clientId, signUuid, timestamp, signType, secret)) {
            log.error("请求：{},签名验证失败;",  exchange.getRequest().getPath());
            return buildErrorResponse("签名验证失败");
        }

        // 7. 验证通过,返回虚拟用户
        return buildSuccessResponse(clientId);
    }

    private List<String> validateRequiredHeaders(ServerWebExchange exchange) {
        List<String> missingParams = new ArrayList<>();
        if (StringUtils.isBlank(exchange.getRequest().getHeaders().getFirst(HEADER_AUTH_TOKEN))) {
            missingParams.add(HEADER_AUTH_TOKEN);
        }
        if (StringUtils.isBlank(exchange.getRequest().getHeaders().getFirst(HEADER_CLIENT_ID))) {
            missingParams.add(HEADER_CLIENT_ID);
        }
        if (StringUtils.isBlank(exchange.getRequest().getHeaders().getFirst(HEADER_SIGN_UUID))) {
            missingParams.add(HEADER_SIGN_UUID);
        }
        if (StringUtils.isBlank(exchange.getRequest().getHeaders().getFirst(HEADER_SIGN_TIMESTAMP))) {
            missingParams.add(HEADER_SIGN_TIMESTAMP);
        }
        if (StringUtils.isBlank(exchange.getRequest().getHeaders().getFirst(HEADER_SIGN_TYPE))) {
            missingParams.add(HEADER_SIGN_TYPE);
        }
        return missingParams;
    }

    private boolean checkAndCacheUuid(String signUuid) {
        String uuidKey = "SIGNATURE:EXTERNAL:UUID:" + signUuid;
        RBucket<String> nonceBucket = redisson.getBucket(uuidKey);
        // 调用方每次请求必须生成一个唯一的UUID,5分钟内不可重复
        try {
            // 原子操作：仅当Key不存在时设置值
            return nonceBucket.setIfAbsent("", Duration.ofMinutes(5));
        } catch (Exception e) {
            log.error("Redis操作失败[{}]: {}", signUuid, e.getMessage());
            return false;
        }
    }

    private String getClientSecret(String clientId) {
        String cacheKey = RedisKeyConstants.OAUTH_CLIENT_SECRET + StrPool.COLON + clientId;
        RBucket<String> bucket = redisson.getBucket(cacheKey);
        return bucket.get();
    }

    private boolean validateSignature(String authToken, String clientId, String signUuid, String timestamp, String signType,
            String secret) {
        String[] signParts = { clientId, signUuid, timestamp, signType, secret };
        String signContent = String.join("&", signParts);
        String expectedSign = DigestUtils.sha256Hex(signContent);

        if (log.isDebugEnabled()) {
            log.debug("Validating signature for client: {}", clientId);
        }

        return Objects.equals(authToken, expectedSign);
    }

    private Mono<LoginUser> buildErrorResponse(String message) {
        log.warn("=====>签名认证不通过：{}", message);
        return Mono.just(LoginUser.builder().id(UNAUTHORIZED_USER_ID).userType(UserTypeEnum.SERVER.getValue()).no(UNAUTHORIZED_USER_NO)
                .name(message).expiresTime(LocalDateTime.now().minusDays(1)).build());
    }

    private Mono<LoginUser> buildSuccessResponse(String clientId) {
        return Mono.just(LoginUser.builder().id(UNAUTHORIZED_USER_ID).no(clientId).name("Server-" + clientId)
                .userType(UserTypeEnum.SERVER.getValue()).build());
    }
}