package com.jasolar.mis.framework.idempotent.core.keyresolver.impl;

import org.aspectj.lang.JoinPoint;

import com.jasolar.mis.framework.idempotent.core.annotation.Idempotent;
import com.jasolar.mis.framework.idempotent.core.keyresolver.IdempotentKeyResolver;

import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.crypto.SecureUtil;

/**
 * 默认（全局级别）幂等 Key 解析器，使用方法名 + 方法参数，组装成一个 Key
 *
 * 为了避免 Key 过长，使用 MD5 进行“压缩”
 *
 * @author zhaohuang
 */
public class GlobalIdempotentKeyResolver implements IdempotentKeyResolver {

    static final String PREFIX = "DEFAULT:";

    @Override
    public String resolver(JoinPoint joinPoint, Idempotent idempotent) {
        String methodName = joinPoint.getSignature().toString();
        String argsStr = CharSequenceUtil.join(",", joinPoint.getArgs());
        return PREFIX + SecureUtil.md5(methodName + argsStr);
    }

}
