package com.jasolar.mis.module.system.service.budget.exception;

import java.util.Map;

/**
 * 明细级别校验异常
 * 用于传递明细级别的校验错误信息
 *
 * @author Auto Generated
 */
public class DetailValidationException extends IllegalStateException {
    
    private final Map<String, String> detailValidationResultMap;
    private final Map<String, String> detailValidationMessageMap;
    
    public DetailValidationException(String message, 
                                    Map<String, String> detailValidationResultMap,
                                    Map<String, String> detailValidationMessageMap) {
        super(message);
        this.detailValidationResultMap = detailValidationResultMap;
        this.detailValidationMessageMap = detailValidationMessageMap;
    }
    
    public Map<String, String> getDetailValidationResultMap() {
        return detailValidationResultMap;
    }
    
    public Map<String, String> getDetailValidationMessageMap() {
        return detailValidationMessageMap;
    }
}

