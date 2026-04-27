package com.jasolar.mis.framework.idempotent.core.keyresolver.impl;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.ParameterNameDiscoverer;

import com.jasolar.mis.framework.common.util.spring.SpelUtils;
import com.jasolar.mis.framework.idempotent.core.annotation.Idempotent;
import com.jasolar.mis.framework.idempotent.core.keyresolver.IdempotentKeyResolver;

import cn.hutool.core.util.ArrayUtil;

/**
 * 基于 Spring EL 表达式，
 *
 * @author zhaohuang
 */
public class ExpressionIdempotentKeyResolver implements IdempotentKeyResolver {

    static final String PREFIX = "SPEL:";

    private final ParameterNameDiscoverer parameterNameDiscoverer = new DefaultParameterNameDiscoverer();

    // private final ExpressionParser expressionParser = new SpelExpressionParser();

    @Override
    public String resolver(JoinPoint joinPoint, Idempotent idempotent) {
        // 获得被拦截方法参数名列表
        Method method = getMethod(joinPoint);
        Object[] args = joinPoint.getArgs();
        String[] parameterNames = this.parameterNameDiscoverer.getParameterNames(method);
        // 准备 Spring EL 表达式解析的上下文
        // StandardEvaluationContext evaluationContext = new StandardEvaluationContext();
        Map<String, Object> vars = new HashMap<>();
        if (ArrayUtil.isNotEmpty(parameterNames)) {
            for (int i = 0; i < parameterNames.length; i++) {
                vars.put(parameterNames[i], args[i]);
            }
        }

        return PREFIX + SpelUtils.getValue(String.class, idempotent.keyArg(), vars);

        // 解析参数
        // Expression expression = expressionParser.parseExpression(idempotent.keyArg());
        // return expression.getValue(evaluationContext, String.class);
    }

    private static Method getMethod(JoinPoint point) {
        // 处理，声明在类上的情况
        MethodSignature signature = (MethodSignature) point.getSignature();
        Method method = signature.getMethod();
        if (!method.getDeclaringClass().isInterface()) {
            return method;
        }

        // 处理，声明在接口上的情况
        try {
            return point.getTarget().getClass().getDeclaredMethod(point.getSignature().getName(), method.getParameterTypes());
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

}
