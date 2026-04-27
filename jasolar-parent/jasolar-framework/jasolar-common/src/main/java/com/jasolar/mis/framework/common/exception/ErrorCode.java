package com.jasolar.mis.framework.common.exception;

import com.jasolar.mis.framework.common.exception.enums.GlobalErrorCodeConstants;
import com.jasolar.mis.framework.common.exception.enums.ServiceErrorCodeRange;
import lombok.Data;

/**
 * 错误码对象
 *
 * 全局错误码，占用 [0, 999], 参见 {@link GlobalErrorCodeConstants}
 * 业务异常错误码，占用 [1 000 000 000, +∞)，参见 {@link ServiceErrorCodeRange}
 *
 */
@Data
public class ErrorCode {

    /**
     * 错误码
     */
    private final String code;
    /**
     * 错误提示
     */
    private final String msg;

    public ErrorCode(String code, String message) {
        this.code = code;
        this.msg = message;
    }

}
