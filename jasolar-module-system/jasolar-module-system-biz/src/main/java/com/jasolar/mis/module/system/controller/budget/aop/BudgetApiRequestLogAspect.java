package com.jasolar.mis.module.system.controller.budget.aop;

import com.jasolar.mis.framework.common.util.json.JsonUtils;
import com.jasolar.mis.framework.common.util.servlet.ServletUtils;
import com.jasolar.mis.module.system.domain.budget.BudgetApiRequestLog;
import com.jasolar.mis.module.system.service.budget.BudgetApiRequestLogService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;


/**
 * 预算接口请求报文记录切面
 * 拦截预算相关的Controller方法，自动记录请求和响应报文
 */
@Aspect
@Component
@Slf4j
public class BudgetApiRequestLogAspect {

    @Resource
    private BudgetApiRequestLogService budgetApiRequestLogService;

    /**
     * 拦截预算相关的Controller方法
     * 包括：BudgetApplicationController, BudgetClaimController, BudgetContractController,
     *      BudgetAdjustController, BudgetQueryController, BudgetOrgController
     */
    @Around("execution(* com.jasolar.mis.module.system.controller.budget.commonapi.BudgetApplicationController.*(..)) || " +
            "execution(* com.jasolar.mis.module.system.controller.budget.commonapi.BudgetClaimController.*(..)) || " +
            "execution(* com.jasolar.mis.module.system.controller.budget.commonapi.BudgetContractController.*(..)) || " +
            "execution(* com.jasolar.mis.module.system.controller.budget.commonapi.BudgetAdjustController.*(..)) || " +
            "execution(* com.jasolar.mis.module.system.controller.budget.commonapi.BudgetQueryController.*(..)) || " +
            "execution(* com.jasolar.mis.module.system.controller.budget.commonapi.BudgetOrgController.*(..))")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        // 记录开始时间
        long startTime = System.currentTimeMillis();
        
        // 获取请求信息
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            // 如果不是HTTP请求，直接执行原方法
            return joinPoint.proceed();
        }
        
        HttpServletRequest request = attributes.getRequest();
        
        // 创建日志实体
        BudgetApiRequestLog apiRequestLog = BudgetApiRequestLog.builder()
                .requestUrl(request.getRequestURI())
                .requestMethod(request.getMethod())
                .controllerName(joinPoint.getTarget().getClass().getSimpleName())
                .methodName(joinPoint.getSignature().getName())
                .userIp(ServletUtils.getClientIP(request))
                .userAgent(ServletUtils.getUserAgent(request))
                .build();
        
        // 记录请求参数
        Object[] args = joinPoint.getArgs();
        if (args != null && args.length > 0) {
            try {
                // 只记录第一个参数（通常是请求体）
                if (args[0] != null) {
                    apiRequestLog.setRequestParams(JsonUtils.toJsonString(args[0]));
                }
            } catch (Exception e) {
                log.warn("序列化请求参数失败", e);
                apiRequestLog.setRequestParams("序列化失败: " + e.getMessage());
            }
        }
        
        Object result = null;
        String errorMsg = null;
        String status = "SUCCESS";
        
        try {
            // 执行原方法
            result = joinPoint.proceed();
            
            // 记录响应结果
            if (result != null) {
                try {
                    apiRequestLog.setResponseResult(JsonUtils.toJsonString(result));
                } catch (Exception e) {
                    log.warn("序列化响应结果失败", e);
                    apiRequestLog.setResponseResult("序列化失败: " + e.getMessage());
                }
            }
            
        } catch (Throwable throwable) {
            // 记录异常信息
            status = "ERROR";
            errorMsg = throwable.getMessage();
            apiRequestLog.setStatus(status);
            apiRequestLog.setErrorMsg(errorMsg);
            apiRequestLog.setResponseResult("请求失败: " + errorMsg);
            
            // 重新抛出异常，不干扰原有异常处理逻辑
            throw throwable;
        } finally {
            // 计算执行时长
            long executeTime = System.currentTimeMillis() - startTime;
            apiRequestLog.setExecuteTime((int) executeTime);
            apiRequestLog.setStatus(status);
            if (errorMsg != null) {
                apiRequestLog.setErrorMsg(errorMsg);
            }
            
            // 异步记录日志（不阻塞主流程）
            budgetApiRequestLogService.recordRequestLog(apiRequestLog);
        }
        
        return result;
    }
}

