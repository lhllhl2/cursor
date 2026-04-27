package com.jasolar.mis.framework.common.exception.util;

import com.jasolar.mis.framework.common.exception.ErrorCode;
import com.jasolar.mis.framework.common.exception.ServiceException;
import com.jasolar.mis.framework.common.exception.enums.GlobalErrorCodeConstants;

import lombok.extern.slf4j.Slf4j;

/**
 * {@link ServiceException} 工具类
 *
 * 目的在于，格式化异常信息提示。
 * 考虑到 String.format 在参数不正确时会报错，因此使用 {} 作为占位符，并使用 {@link #doFormat(String, String, Object...)} 方法来格式化
 *
 */
@Slf4j
public class ServiceExceptionUtil {

    private ServiceExceptionUtil() {
    }
    // ========== 和 ServiceException 的集成 ==========

    public static ServiceException exception(ErrorCode errorCode) {
        return exception0(errorCode.getCode(), errorCode.getMsg());
    }

    public static ServiceException exception(ErrorCode errorCode, Object... params) {
        return exception0(errorCode.getCode(), errorCode.getMsg(), params);
    }

    public static ServiceException exception0(String code, String message, Object... params) {
        // String message = doFormat(code, messagePattern, params);
        // 全局异常国际化
        return new ServiceException(code, message, params);
    }

    public static ServiceException invalidParamException(String messagePattern, Object... params) {
        return exception0(GlobalErrorCodeConstants.BAD_REQUEST.getCode(), messagePattern, params);
    }

    // ========== 格式化方法 ==========

    /**
     * 将错误编号对应的消息使用 params 进行格式化。
     * 此方法不应该使用,交给MessageSource国际化异常消息
     *
     * @param code 错误编号
     * @param messagePattern 消息模版
     * @param params 参数
     * @return 格式化后的提示
     */
    @Deprecated
    public static String doFormat(String code, String messagePattern, Object... params) {
        StringBuilder sbuf = new StringBuilder(messagePattern.length() + 50);
        int i = 0;
        int j;
        int l;
        for (l = 0; l < params.length; l++) {
            j = messagePattern.indexOf("{}", i);
            if (j == -1) {
                log.error("[doFormat][参数过多：错误码({})|错误内容({})|参数({})", code, messagePattern, params);
                if (i == 0) {
                    return messagePattern;
                } else {
                    sbuf.append(messagePattern.substring(i));
                    return sbuf.toString();
                }
            } else {
                sbuf.append(messagePattern, i, j);
                sbuf.append(params[l]);
                i = j + 2;
            }
        }
        if (messagePattern.indexOf("{}", i) != -1) {
            log.error("[doFormat][参数过少：错误码({})|错误内容({})|参数({})", code, messagePattern, params);
        }
        sbuf.append(messagePattern.substring(i));
        return sbuf.toString();
    }

}
