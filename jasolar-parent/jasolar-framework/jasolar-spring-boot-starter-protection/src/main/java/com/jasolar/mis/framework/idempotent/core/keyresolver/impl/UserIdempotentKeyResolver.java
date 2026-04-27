package com.jasolar.mis.framework.idempotent.core.keyresolver.impl;

import org.aspectj.lang.JoinPoint;

import com.jasolar.mis.framework.common.enums.UserTypeEnum;
import com.jasolar.mis.framework.idempotent.core.annotation.Idempotent;
import com.jasolar.mis.framework.idempotent.core.keyresolver.IdempotentKeyResolver;
import com.jasolar.mis.framework.web.core.util.WebFrameworkUtils;

import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.text.StrPool;
import cn.hutool.crypto.SecureUtil;

/**
 * 用户级别的幂等 Key 解析器，使用 userType:userNo:md5(方法名 + 方法参数)，组装成一个 Key
 *
 * 为了避免 Key 过长，使用 MD5 进行“压缩”
 *
 * @author zhaohuang
 */
public class UserIdempotentKeyResolver implements IdempotentKeyResolver {

    static final String PREFIX = "USER:";

    @Override
    public String resolver(JoinPoint joinPoint, Idempotent idempotent) {
        String methodName = joinPoint.getSignature().toString();
        String argsStr = CharSequenceUtil.join(",", joinPoint.getArgs());
        // Long userId = WebFrameworkUtils.getLoginUserId();
        String userNo = WebFrameworkUtils.getLoginUserNo();
        UserTypeEnum userType = UserTypeEnum.valueOf(WebFrameworkUtils.getLoginUserType());
        return PREFIX + userType.name() + StrPool.COLON + userNo + StrPool.COLON + SecureUtil.md5(methodName + argsStr);
    }

}
