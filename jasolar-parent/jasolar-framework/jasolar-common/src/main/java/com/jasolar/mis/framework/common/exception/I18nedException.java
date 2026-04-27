package com.jasolar.mis.framework.common.exception;

/**
 * 消息文本已经国际化, 直接返回前端不做处理
 * 
 * @author galuo
 * @date 2025-04-10 20:16
 *
 */
@SuppressWarnings("serial")
public class I18nedException extends RuntimeException {

    public I18nedException() {
        super();
    }

    public I18nedException(String message, Throwable cause) {
        super(message, cause);
    }

    public I18nedException(String message) {
        super(message);
    }

}
