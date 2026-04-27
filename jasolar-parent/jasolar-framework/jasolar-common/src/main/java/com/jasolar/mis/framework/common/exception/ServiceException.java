package com.jasolar.mis.framework.common.exception;

import com.jasolar.mis.framework.common.exception.enums.ServiceErrorCodeRange;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 业务逻辑异常 Exception
 */
@SuppressWarnings("serial")
@Data
@EqualsAndHashCode(callSuper = true)
public class ServiceException extends RuntimeException {

    /**
     * 业务错误码
     *
     * @see ServiceErrorCodeRange
     */
    private String code;
    /**
     * 错误提示
     */
    private String message;

    /** 消息国际化参数 */
    private Object[] args;

    /**
     * 空构造方法，避免反序列化问题
     */
    public ServiceException() {
    }

    public ServiceException(Throwable cause) {
        super(cause);
        if (cause instanceof ServiceException se) {
            this.code = se.code;
            this.message = se.message;
            this.args = se.args;
        }
    }

    public ServiceException(ErrorCode errorCode) {
        this.code = errorCode.getCode();
        this.message = errorCode.getMsg();
    }

    public ServiceException(String code, String message) {
        this.code = code;
        this.message = message;
    }

    public ServiceException(ErrorCode errorCode, Object... args) {
        this.code = errorCode.getCode();
        this.message = errorCode.getMsg();
        this.args = args;
    }

    public ServiceException(String code, String message, Object... args) {
        this.code = code;
        this.message = message;
        this.args = args;
    }

    public String getCode() {
        return code;
    }

    public ServiceException setCode(String code) {
        this.code = code;
        return this;
    }

    @Override
    public String getMessage() {
        return message;
    }

    public ServiceException setMessage(String message) {
        this.message = message;
        return this;
    }

}
