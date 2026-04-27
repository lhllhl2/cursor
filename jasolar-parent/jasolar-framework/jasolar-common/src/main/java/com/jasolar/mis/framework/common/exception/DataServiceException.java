package com.jasolar.mis.framework.common.exception;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 可以返回数据到前端的业务异常
 * 
 * @author galuo
 * @date 2025-05-22 00:58
 *
 */
@SuppressWarnings("serial")
@Data
@EqualsAndHashCode(callSuper = true)
public class DataServiceException extends ServiceException {

    /** 要返回的数据 */
    private Object data;

    public DataServiceException(Object data, ErrorCode errorCode, Object... args) {
        super(errorCode, args);
        this.data = data;
    }

    public DataServiceException(Object data, ErrorCode errorCode) {
        super(errorCode);
        this.data = data;
    }

    public DataServiceException(Object data, String code, String message, Object... args) {
        super(code, message, args);
        this.data = data;
    }

    public DataServiceException(Object data, String code, String message) {
        super(code, message);
        this.data = data;
    }

}
