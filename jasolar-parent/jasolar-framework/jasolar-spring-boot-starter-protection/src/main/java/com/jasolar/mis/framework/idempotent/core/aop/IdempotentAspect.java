package com.jasolar.mis.framework.idempotent.core.aop;

import java.util.List;
import java.util.Map;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.util.Assert;

import com.jasolar.mis.framework.common.exception.ServiceException;
import com.jasolar.mis.framework.common.exception.enums.GlobalErrorCodeConstants;
import com.jasolar.mis.framework.common.util.collection.CollectionUtils;
import com.jasolar.mis.framework.idempotent.core.IdempotentKeyOps;
import com.jasolar.mis.framework.idempotent.core.annotation.Idempotent;
import com.jasolar.mis.framework.idempotent.core.keyresolver.IdempotentKeyResolver;

import lombok.extern.slf4j.Slf4j;

/**
 * 拦截声明了 {@link Idempotent} 注解的方法，实现幂等操作
 *
 * @author zhaohuang
 */
@Aspect
@Slf4j
public class IdempotentAspect {

    /** IdempotentKeyResolver 集合 */
    private final Map<Class<? extends IdempotentKeyResolver>, IdempotentKeyResolver> keyResolvers;

    /** 操作缓存key */
    private final IdempotentKeyOps idempotentKeyOps;

    public IdempotentAspect(List<IdempotentKeyResolver> keyResolvers, IdempotentKeyOps idempotentKeyOps) {
        this.keyResolvers = CollectionUtils.convertMap(keyResolvers, IdempotentKeyResolver::getClass);
        this.idempotentKeyOps = idempotentKeyOps;
    }

    @Around(value = "@annotation(idempotent)")
    public Object aroundPointCut(ProceedingJoinPoint joinPoint, Idempotent idempotent) throws Throwable {
        // 获得 IdempotentKeyResolver
        IdempotentKeyResolver keyResolver = keyResolvers.get(idempotent.keyResolver());
        Assert.notNull(keyResolver, "找不到对应的 IdempotentKeyResolver");
        // 解析 Key
        String key = keyResolver.resolver(joinPoint, idempotent);

        // 1. 锁定 Key
        boolean success = idempotentKeyOps.put(key, idempotent.timeout(), idempotent.timeUnit());
        // 锁定失败，抛出异常
        if (!success) {
            log.info("[aroundPointCut][方法({}) 参数({}) 存在重复请求]", joinPoint.getSignature().toString(), joinPoint.getArgs());

            throw new ServiceException(idempotent.errorCode(), GlobalErrorCodeConstants.REPEATED_REQUESTS.getMsg());
        }

        // 2. 执行逻辑
        try {
            Object result = joinPoint.proceed();

            // 执行完成删除key
            idempotentKeyOps.delete(key);
            return result;
        } catch (Throwable throwable) {
            // 3. 异常时，删除 Key
            // 参考美团 GTIS 思路：https://tech.meituan.com/2016/09/29/distributed-system-mutually-exclusive-idempotence-cerberus-gtis.html
            if (idempotent.deleteKeyWhenException()) {
                idempotentKeyOps.delete(key);
            }
            throw throwable;
        }
    }

}
