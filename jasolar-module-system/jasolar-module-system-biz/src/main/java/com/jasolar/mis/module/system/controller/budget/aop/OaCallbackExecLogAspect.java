package com.jasolar.mis.module.system.controller.budget.aop;

import com.jasolar.mis.framework.common.util.json.JsonUtils;
import com.jasolar.mis.module.system.controller.budget.oa.OaCallbackSqlTraceContext;
import com.jasolar.mis.module.system.domain.budget.BudgetOaCallbackExecLog;
import com.jasolar.mis.module.system.service.budget.BudgetOaCallbackExecLogService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import java.util.StringJoiner;

/**
 * OA回调执行日志切面
 */
@Slf4j
@Aspect
@Component
public class OaCallbackExecLogAspect {

    @Resource
    private BudgetOaCallbackExecLogService budgetOaCallbackExecLogService;

    @Around("execution(* com.jasolar.mis.module.system.controller.budget.oa.OaCallbackSubmitController.submit(..)) || " +
            "execution(* com.jasolar.mis.module.system.controller.budget.oa.OaApprovalFlowCronController.hello(..))")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        BudgetOaCallbackExecLog execLog = BudgetOaCallbackExecLog.builder().build();
        try {
            Object[] args = joinPoint.getArgs();
            if (args != null && args.length > 0) {
                execLog.setRequestParams(JsonUtils.toJsonString(args));
            }
        } catch (Exception ex) {
            execLog.setRequestParams("serialize request failed: " + ex.getMessage());
        }

        OaCallbackSqlTraceContext.start();
        Object result = null;
        Throwable throwable = null;
        try {
            result = joinPoint.proceed();
            return result;
        } catch (Throwable ex) {
            throwable = ex;
            throw ex;
        } finally {
            try {
                if (throwable != null) {
                    execLog.setResponseResult("error: " + throwable.getMessage());
                } else {
                    execLog.setResponseResult(JsonUtils.toJsonString(result));
                }
            } catch (Exception ex) {
                execLog.setResponseResult("serialize response failed: " + ex.getMessage());
            }

            StringJoiner joiner = new StringJoiner(";");
            OaCallbackSqlTraceContext.getSqlList().forEach(joiner::add);
            execLog.setRunSql(joiner.toString());
            OaCallbackSqlTraceContext.clear();
            budgetOaCallbackExecLogService.record(execLog);
        }
    }
}
