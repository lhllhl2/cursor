package com.jasolar.mis.framework.web.core.handler;

import static com.jasolar.mis.framework.common.exception.enums.GlobalErrorCodeConstants.BAD_REQUEST;
import static com.jasolar.mis.framework.common.exception.enums.GlobalErrorCodeConstants.INTERNAL_SERVER_ERROR;
import static com.jasolar.mis.framework.common.exception.enums.GlobalErrorCodeConstants.METHOD_NOT_ALLOWED;
import static com.jasolar.mis.framework.common.exception.enums.GlobalErrorCodeConstants.NOT_FOUND;
import static com.jasolar.mis.framework.common.exception.enums.GlobalErrorCodeConstants.NOT_IMPLEMENTED;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.util.Assert;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.jasolar.mis.framework.common.exception.DataServiceException;
import com.jasolar.mis.framework.common.exception.I18nedException;
import com.jasolar.mis.framework.common.exception.ServiceException;
import com.jasolar.mis.framework.common.exception.enums.GlobalErrorCodeConstants;
import com.jasolar.mis.framework.common.exception.util.ServiceExceptionUtil;
import com.jasolar.mis.framework.common.pojo.CommonResult;
import com.jasolar.mis.framework.common.security.LoginUser;
import com.jasolar.mis.framework.common.util.collection.SetUtils;
import com.jasolar.mis.framework.common.util.json.JsonUtils;
import com.jasolar.mis.framework.common.util.monitor.TracerUtils;
import com.jasolar.mis.framework.common.util.servlet.ServletUtils;
import com.jasolar.mis.framework.i18n.I18nUtils;
import com.jasolar.mis.framework.web.core.util.WebFrameworkUtils;
import com.jasolar.mis.module.log.api.ApiErrorLogApi;
import com.jasolar.mis.module.log.api.dto.ApiErrorLogCreateReqDTO;

import cn.hutool.core.exceptions.ExceptionUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.extra.servlet.JakartaServletUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.ValidationException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 全局异常处理器，将 Exception 翻译成 CommonResult + 对应的异常编号
 *
 * @author zhaohuang
 */
@RestControllerAdvice
@AllArgsConstructor
@Slf4j
@Order(GlobalExceptionHandler.ORDER)
public class GlobalExceptionHandler {

    /** 定义顺序 */
    public static final int ORDER = Ordered.LOWEST_PRECEDENCE;

    /**
     * 忽略的 ServiceException 错误提示，避免打印过多 logger
     */
    protected static final Set<String> IGNORE_ERROR_MESSAGES = SetUtils.asSet("无效的刷新令牌");

    private final String applicationName;

    private final ApiErrorLogApi apiErrorLogApi;

    @Value("${jasolar.debug:false}")
    private boolean debug;

    /**
     * 处理所有异常，主要是提供给 Filter 使用 因为 Filter 不走 SpringMVC
     * 的流程，但是我们又需要兜底处理异常，所以这里提供一个全量的异常处理过程，保持逻辑统一。
     *
     * @param request 请求
     * @param ex 异常
     * @return 通用返回
     */
    public CommonResult<?> allExceptionHandler(HttpServletRequest request, Throwable ex) {
        if (ex instanceof MissingServletRequestParameterException) {
            return missingServletRequestParameterExceptionHandler((MissingServletRequestParameterException) ex);
        }
        if (ex instanceof MethodArgumentTypeMismatchException) {
            return methodArgumentTypeMismatchExceptionHandler((MethodArgumentTypeMismatchException) ex);
        }
        if (ex instanceof MethodArgumentNotValidException) {
            return methodArgumentNotValidExceptionExceptionHandler((MethodArgumentNotValidException) ex);
        }
        if (ex instanceof BindException) {
            return bindExceptionHandler((BindException) ex);
        }
        if (ex instanceof ConstraintViolationException) {
            return constraintViolationExceptionHandler((ConstraintViolationException) ex);
        }
        if (ex instanceof ValidationException) {
            return validationException((ValidationException) ex);
        }
        if (ex instanceof NoHandlerFoundException) {
            return noHandlerFoundExceptionHandler((NoHandlerFoundException) ex);
        }
        if (ex instanceof NoResourceFoundException) {
            return noResourceFoundExceptionHandler(request, (NoResourceFoundException) ex);
        }
        if (ex instanceof HttpRequestMethodNotSupportedException) {
            return httpRequestMethodNotSupportedExceptionHandler((HttpRequestMethodNotSupportedException) ex);
        }
        if (ex instanceof ServiceException) {
            return serviceExceptionHandler((ServiceException) ex);
        }
        if (ex instanceof I18nedException) {
            return i18nedException((I18nedException) ex);
        }
        return defaultExceptionHandler(request, ex);
    }

    /**
     * 处理 SpringMVC 请求参数缺失
     * <p>
     * 例如说，接口上设置了 @RequestParam("xx") 参数，结果并未传递 xx 参数
     */
    @ExceptionHandler(value = MissingServletRequestParameterException.class)
    public CommonResult<?> missingServletRequestParameterExceptionHandler(MissingServletRequestParameterException ex) {
        log.warn("[missingServletRequestParameterExceptionHandler]", ex);
        return CommonResult.error(BAD_REQUEST.getCode(), String.format("请求参数缺失:%s", ex.getParameterName()));
    }

    /**
     * 处理 SpringMVC 请求参数类型错误
     * <p>
     * 例如说，接口上设置了 @RequestParam("xx") 参数为 Integer，结果传递 xx 参数类型为 String
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public CommonResult<?> methodArgumentTypeMismatchExceptionHandler(MethodArgumentTypeMismatchException ex) {
        log.warn("[methodArgumentTypeMismatchExceptionHandler]", ex);
        return CommonResult.error(BAD_REQUEST.getCode(), String.format("请求参数类型错误:%s", ex.getMessage()));
    }

    /**
     * 处理 SpringMVC 参数校验不正确
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public CommonResult<?> methodArgumentNotValidExceptionExceptionHandler(MethodArgumentNotValidException ex) {
        log.warn("[methodArgumentNotValidExceptionExceptionHandler]", ex);
        FieldError fieldError = ex.getBindingResult().getFieldError();
        assert fieldError != null; // 断言，避免告警
        return CommonResult.error(BAD_REQUEST.getCode(), String.format("请求参数不正确:%s", fieldError.getDefaultMessage()));
    }

    /**
     * 处理 SpringMVC 参数绑定不正确，本质上也是通过 Validator 校验
     */
    @ExceptionHandler(BindException.class)
    public CommonResult<?> bindExceptionHandler(BindException ex) {
        log.warn("[handleBindException]", ex);
        FieldError fieldError = ex.getFieldError();
        assert fieldError != null; // 断言，避免告警
        return CommonResult.error(BAD_REQUEST.getCode(), String.format("请求参数不正确:%s", fieldError.getDefaultMessage()));
    }

    /**
     * 处理 SpringMVC 请求参数类型错误
     * <p>
     * 例如说，接口上设置了 @RequestBody实体中 xx 属性类型为 Integer，结果传递 xx 参数类型为 String
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public CommonResult<?> methodArgumentTypeInvalidFormatExceptionHandler(HttpMessageNotReadableException ex) {
        log.warn("[methodArgumentTypeInvalidFormatExceptionHandler]", ex);
        if (ex.getCause() instanceof InvalidFormatException) {
            InvalidFormatException invalidFormatException = (InvalidFormatException) ex.getCause();
            return CommonResult.error(BAD_REQUEST.getCode(), String.format("请求参数类型错误:%s", invalidFormatException.getValue()));
        } else {
            return defaultExceptionHandler(ServletUtils.getRequest(), ex);
        }
    }

    /**
     * 处理 Validator 校验不通过产生的异常
     */
    @ExceptionHandler(value = ConstraintViolationException.class)
    public CommonResult<?> constraintViolationExceptionHandler(ConstraintViolationException ex) {
        log.warn("[constraintViolationExceptionHandler]", ex);
        ConstraintViolation<?> constraintViolation = ex.getConstraintViolations().iterator().next();
        return CommonResult.error(BAD_REQUEST.getCode(), String.format("请求参数不正确:%s", constraintViolation.getMessage()));
    }

    /**
     * 处理 Dubbo Consumer 本地参数校验时，抛出的 ValidationException 异常
     */
    @ExceptionHandler(value = ValidationException.class)
    public CommonResult<?> validationException(ValidationException ex) {
        log.warn("[constraintViolationExceptionHandler]", ex);
        // 无法拼接明细的错误信息，因为 Dubbo Consumer 抛出 ValidationException 异常时，是直接的字符串信息，且人类不可读
        return CommonResult.error(BAD_REQUEST);
    }

    /**
     * 处理 SpringMVC 请求地址不存在
     * <p>
     * 注意，它需要设置如下两个配置项： 1. spring.mvc.throw-exception-if-no-handler-found 为 true 2.
     * spring.mvc.static-path-pattern 为 /statics/**
     */
    @ExceptionHandler(NoHandlerFoundException.class)
    public CommonResult<?> noHandlerFoundExceptionHandler(NoHandlerFoundException ex) {
        log.warn("[noHandlerFoundExceptionHandler]", ex);
        return CommonResult.error(NOT_FOUND.getCode(), String.format("请求地址不存在:%s", ex.getRequestURL()));
    }

    /**
     * 处理 SpringMVC 请求地址不存在
     */
    @ExceptionHandler(NoResourceFoundException.class)
    private CommonResult<?> noResourceFoundExceptionHandler(HttpServletRequest req, NoResourceFoundException ex) {
        log.warn("[noResourceFoundExceptionHandler]", ex);
        return CommonResult.error(NOT_FOUND.getCode(), String.format("请求地址不存在:%s", ex.getResourcePath()));
    }

    /**
     * 处理 SpringMVC 请求方法不正确
     * <p>
     * 例如说，A 接口的方法为 GET 方式，结果请求方法为 POST 方式，导致不匹配
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public CommonResult<?> httpRequestMethodNotSupportedExceptionHandler(HttpRequestMethodNotSupportedException ex) {
        log.warn("[httpRequestMethodNotSupportedExceptionHandler]", ex);
        return CommonResult.error(METHOD_NOT_ALLOWED.getCode(), String.format("请求方法不正确:%s", ex.getMessage()));
    }

    /**
     * 处理业务异常 ServiceException
     * <p>
     * 例如说，商品库存不足，用户手机号已存在。
     */
    @ExceptionHandler(value = ServiceException.class)
    public CommonResult<?> serviceExceptionHandler(ServiceException ex) {
        // 不包含的时候，才进行打印，避免 ex 堆栈过多
        if (!IGNORE_ERROR_MESSAGES.contains(ex.getMessage())) {
            // 即使打印，也只打印第一层 StackTraceElement，并且使用 warn 在控制台输出，更容易看到
            try {
                StackTraceElement[] stackTraces = ex.getStackTrace();
                for (StackTraceElement stackTrace : stackTraces) {
                    if (ObjectUtil.notEqual(stackTrace.getClassName(), ServiceExceptionUtil.class.getName())) {
                        log.warn("[serviceExceptionHandler]\n\t{}", stackTrace);
                        break;
                    }
                }
            } catch (Exception ignored) {
                // 忽略日志，避免影响主流程
            }
        }
        // 国际化异常消息
        String msg = I18nUtils.getMessage(ex.getCode(), ex.getArgs(), ex.getMessage());
        log.warn("接口请求失败: " + msg, ex);
        CommonResult<Object> err = CommonResult.error(ex.getCode(), msg);
        if (ex instanceof DataServiceException dex) {
            // 包含数据的异常信息
            err.setData(dex.getData());
        }
        return err;
    }

    /**
     * 处理已经国际化的异常. 比如导入excel的验证,可能会将所有错误全部得到后一次性抛出异常
     * 
     * @param ex 异常
     * @return
     */
    @ExceptionHandler(value = I18nedException.class)
    public CommonResult<?> i18nedException(I18nedException ex) {
        log.warn("接口请求失败", ex);
        return CommonResult.error(GlobalErrorCodeConstants.ERROR.getCode(), ex.getMessage());
    }

    /**
     * 处理系统异常，兜底处理所有的一切
     */
    @ExceptionHandler(value = Exception.class)
    public CommonResult<?> defaultExceptionHandler(HttpServletRequest req, Throwable ex) {
        // 情况一：处理表不存在的异常
        CommonResult<?> tableNotExistsResult = handleTableNotExists(ex);
        if (tableNotExistsResult != null) {
            return tableNotExistsResult;
        }

        // 情况二：处理异常
        log.error("[defaultExceptionHandler]", ex);
        // 插入异常日志
        createExceptionLog(req, ex);
        // 返回 ERROR CommonResult
        if (debug) {
            // 调试模式抛出异常堆栈
            return CommonResult.error(INTERNAL_SERVER_ERROR.getCode(), ExceptionUtils.getStackTrace(ex));
        }

        // 屏蔽异常,生产上需要这样处理
        String msg = I18nUtils.getMessage(INTERNAL_SERVER_ERROR.getI18nCode(), new Object[] { TracerUtils.getTraceId() },
                INTERNAL_SERVER_ERROR.getMsg());
        return CommonResult.error(INTERNAL_SERVER_ERROR.getCode(), msg);
    }

    private void createExceptionLog(HttpServletRequest req, Throwable e) {
        // 插入错误日志
        ApiErrorLogCreateReqDTO errorLog = new ApiErrorLogCreateReqDTO();
        try {
            // 初始化 errorLog
            buildExceptionLog(errorLog, req, e);
            // 执行插入 errorLog
            apiErrorLogApi.createApiErrorLogAsync(errorLog);
        } catch (Exception ignore) {
            if (debug) {
                log.error("[createExceptionLog][url({}) log({}) 发生异常]", req.getRequestURI(), JsonUtils.toJsonString(errorLog), ignore);
            }
        }
    }

    private void buildExceptionLog(ApiErrorLogCreateReqDTO errorLog, HttpServletRequest request, Throwable e) {
        // 处理用户信息
        LoginUser user = WebFrameworkUtils.getLoginUser();
        errorLog.setUserId(user.getId());
        errorLog.setUserType(user.getUserType());
        errorLog.setUserNo(user.getNo());
        errorLog.setUserName(user.getName());

        // 设置异常字段
        errorLog.setExceptionName(e.getClass().getName());
        errorLog.setExceptionMessage(ExceptionUtil.getMessage(e));
        errorLog.setExceptionRootCauseMessage(ExceptionUtil.getRootCauseMessage(e));
        errorLog.setExceptionStackTrace(ExceptionUtil.stacktraceToString(e));
        StackTraceElement[] stackTraceElements = e.getStackTrace();
        Assert.notEmpty(stackTraceElements, "异常 stackTraceElements 不能为空");
        StackTraceElement stackTraceElement = stackTraceElements[0];
        errorLog.setExceptionClassName(stackTraceElement.getClassName());
        errorLog.setExceptionFileName(stackTraceElement.getFileName());
        errorLog.setExceptionMethodName(stackTraceElement.getMethodName());
        errorLog.setExceptionLineNumber(stackTraceElement.getLineNumber());
        // 设置其它字段
        errorLog.setTraceId(TracerUtils.getTraceId());
        errorLog.setApplicationName(applicationName);
        errorLog.setRequestUrl(request.getRequestURI());
        Map<String, Object> requestParams = MapUtil.<String, Object>builder().put("query", JakartaServletUtil.getParamMap(request)).build();
        errorLog.setRequestParams(JsonUtils.toJsonString(requestParams));
        errorLog.setRequestMethod(request.getMethod());
        errorLog.setUserAgent(ServletUtils.getUserAgent(request));
        errorLog.setUserIp(JakartaServletUtil.getClientIP(request));
        errorLog.setExceptionTime(LocalDateTime.now());
    }

    /**
     * 处理 Table 不存在的异常情况
     *
     * @param ex 异常
     * @return 如果是 Table 不存在的异常，则返回对应的 CommonResult
     */
    private CommonResult<?> handleTableNotExists(Throwable ex) {
        String message = ExceptionUtil.getRootCauseMessage(ex);
        if (!message.contains("doesn't exist")) {
            return null;
        }
        // 1. 数据报表
        if (message.contains("report_")) {
            log.error(
                    "[报表模块 jasolar-module-report - 表结构未导入][参考 https://www.yuque.com/xiangdong-m7nzy/rr/rk9z40n8v7hveron?singleDoc#/report/ 开启]");
            return CommonResult.error(NOT_IMPLEMENTED.getCode(),
                    "[报表模块 jasolar-module-report - 表结构未导入][参考 https://www.yuque.com/xiangdong-m7nzy/rr/rk9z40n8v7hveron?singleDoc#/report/ 开启]");
        }
        // 2. 工作流
        if (message.contains("bpm_")) {
            log.error(
                    "[工作流模块 jasolar-module-bpm - 表结构未导入][参考 https://www.yuque.com/xiangdong-m7nzy/rr/rk9z40n8v7hveron?singleDoc#/bpm/ 开启]");
            return CommonResult.error(NOT_IMPLEMENTED.getCode(),
                    "[工作流模块 jasolar-module-bpm - 表结构未导入][参考 https://www.yuque.com/xiangdong-m7nzy/rr/rk9z40n8v7hveron?singleDoc#/bpm/ 开启]");
        }
        // 3. 微信公众号
        if (message.contains("mp_")) {
            log.error(
                    "[微信公众号 jasolar-module-mp - 表结构未导入][参考 https://www.yuque.com/xiangdong-m7nzy/rr/rk9z40n8v7hveron?singleDoc#/mp/build/ 开启]");
            return CommonResult.error(NOT_IMPLEMENTED.getCode(),
                    "[微信公众号 jasolar-module-mp - 表结构未导入][参考 https://www.yuque.com/xiangdong-m7nzy/rr/rk9z40n8v7hveron?singleDoc#/mp/build/ 开启]");
        }
        // 4. 商城系统
        if (CharSequenceUtil.containsAny(message, "product_", "promotion_", "trade_")) {
            log.error(
                    "[商城系统 jasolar-module-mall - 已禁用][参考 https://www.yuque.com/xiangdong-m7nzy/rr/rk9z40n8v7hveron?singleDoc#/mall/build/ 开启]");
            return CommonResult.error(NOT_IMPLEMENTED.getCode(),
                    "[商城系统 jasolar-module-mall - 已禁用][参考 https://www.yuque.com/xiangdong-m7nzy/rr/rk9z40n8v7hveron?singleDoc#/mall/build/ 开启]");
        }
        // 5. ERP 系统
        if (message.contains("erp_")) {
            log.error(
                    "[ERP 系统 jasolar-module-erp - 表结构未导入][参考 https://www.yuque.com/xiangdong-m7nzy/rr/rk9z40n8v7hveron?singleDoc#/erp/build/ 开启]");
            return CommonResult.error(NOT_IMPLEMENTED.getCode(),
                    "[ERP 系统 jasolar-module-erp - 表结构未导入][参考 https://www.yuque.com/xiangdong-m7nzy/rr/rk9z40n8v7hveron?singleDoc#/erp/build/ 开启]");
        }
        // 6. CRM 系统
        if (message.contains("crm_")) {
            log.error(
                    "[CRM 系统 jasolar-module-crm - 表结构未导入][参考 https://www.yuque.com/xiangdong-m7nzy/rr/rk9z40n8v7hveron?singleDoc#/crm/build/ 开启]");
            return CommonResult.error(NOT_IMPLEMENTED.getCode(),
                    "[CRM 系统 jasolar-module-crm - 表结构未导入][参考 https://www.yuque.com/xiangdong-m7nzy/rr/rk9z40n8v7hveron?singleDoc#/crm/build/ 开启]");
        }
        // 7. 支付平台
        if (message.contains("pay_")) {
            log.error(
                    "[支付模块 jasolar-module-pay - 表结构未导入][参考 https://www.yuque.com/xiangdong-m7nzy/rr/rk9z40n8v7hveron?singleDoc#/pay/build/ 开启]");
            return CommonResult.error(NOT_IMPLEMENTED.getCode(),
                    "[支付模块 jasolar-module-pay - 表结构未导入][参考 https://www.yuque.com/xiangdong-m7nzy/rr/rk9z40n8v7hveron?singleDoc#/pay/build/ 开启]");
        }
        // 8. AI 大模型
        if (message.contains("ai_")) {
            log.error(
                    "[AI 大模型 jasolar-module-ai - 表结构未导入][参考 https://www.yuque.com/xiangdong-m7nzy/rr/rk9z40n8v7hveron?singleDoc#/ai/build/ 开启]");
            return CommonResult.error(NOT_IMPLEMENTED.getCode(),
                    "[AI 大模型 jasolar-module-ai - 表结构未导入][参考 https://www.yuque.com/xiangdong-m7nzy/rr/rk9z40n8v7hveron?singleDoc#/ai/build/ 开启]");
        }
        // 9. IOT 物联网
        if (message.contains("iot_")) {
            log.error(
                    "[IOT 物联网 jasolar-module-iot - 表结构未导入][参考 https://www.yuque.com/xiangdong-m7nzy/rr/rk9z40n8v7hveron?singleDoc#/iot/build/ 开启]");
            return CommonResult.error(NOT_IMPLEMENTED.getCode(),
                    "[IOT 物联网 jasolar-module-iot - 表结构未导入][参考 https://www.yuque.com/xiangdong-m7nzy/rr/rk9z40n8v7hveron?singleDoc#/iot/build/ 开启]");
        }
        return null;
    }

}
