package com.jasolar.mis.framework.security.core.handler;

import static com.jasolar.mis.framework.common.exception.enums.GlobalErrorCodeConstants.FORBIDDEN;

import org.springframework.core.annotation.Order;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.jasolar.mis.framework.common.pojo.CommonResult;
import com.jasolar.mis.framework.web.core.handler.GlobalExceptionHandler;
import com.jasolar.mis.framework.web.core.util.WebFrameworkUtils;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

/**
 * 拦截权限不足的异常统一返回到前端. 必须在 {@link GlobalExceptionHandler}之前执行
 */
@RestControllerAdvice
@Slf4j
@Order(GlobalExceptionHandler.ORDER - 10000)
public class SecurityExceptionHandler {

    /**
     * 处理 Spring Security 权限不足的异常
     * <p>
     * 来源是，使用 @PreAuthorize 注解，AOP 进行权限拦截
     */
    @ExceptionHandler(value = AccessDeniedException.class)
    public CommonResult<?> accessDeniedExceptionHandler(HttpServletRequest req, AccessDeniedException ex) {
        log.warn("[accessDeniedExceptionHandler][userId({}) 无法访问 url({})]", WebFrameworkUtils.getLoginUserId(req), req.getRequestURL(), ex);
        return CommonResult.error(FORBIDDEN);
    }

}
