package com.jasolar.mis.framework.idempotent.config;

import java.util.List;

import org.redisson.api.RedissonClient;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

import com.jasolar.mis.framework.idempotent.core.IdempotentKeyOps;
import com.jasolar.mis.framework.idempotent.core.aop.IdempotentAspect;
import com.jasolar.mis.framework.idempotent.core.keyresolver.IdempotentKeyResolver;
import com.jasolar.mis.framework.idempotent.core.keyresolver.impl.GlobalIdempotentKeyResolver;
import com.jasolar.mis.framework.idempotent.core.keyresolver.impl.ExpressionIdempotentKeyResolver;
import com.jasolar.mis.framework.idempotent.core.keyresolver.impl.UserIdempotentKeyResolver;
import com.jasolar.mis.framework.idempotent.core.redis.IdempotentRedisDAO;
import com.jasolar.mis.framework.redis.config.JasolarRedisAutoConfiguration;

@AutoConfiguration(after = JasolarRedisAutoConfiguration.class)
public class JasolarIdempotentConfiguration {

    @Bean
    @ConditionalOnMissingBean(IdempotentKeyOps.class)
    public IdempotentKeyOps idempotentKeyOps(RedissonClient redisson) {
        return new IdempotentRedisDAO(redisson);
    }

    @Bean
    public IdempotentAspect idempotentAspect(List<IdempotentKeyResolver> keyResolvers, IdempotentRedisDAO idempotentRedisDAO) {
        return new IdempotentAspect(keyResolvers, idempotentRedisDAO);
    }

    // ========== 各种 IdempotentKeyResolver Bean ==========

    @Bean
    public GlobalIdempotentKeyResolver globalIdempotentKeyResolver() {
        return new GlobalIdempotentKeyResolver();
    }

    @Bean
    public UserIdempotentKeyResolver userIdempotentKeyResolver() {
        return new UserIdempotentKeyResolver();
    }

    @Bean
    public ExpressionIdempotentKeyResolver expressionIdempotentKeyResolver() {
        return new ExpressionIdempotentKeyResolver();
    }

}
