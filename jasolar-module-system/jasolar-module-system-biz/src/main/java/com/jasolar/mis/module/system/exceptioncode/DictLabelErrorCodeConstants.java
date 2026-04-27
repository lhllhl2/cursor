package com.jasolar.mis.module.system.exceptioncode;

import com.jasolar.mis.framework.common.exception.ErrorCode;

/**
 * Description: 字典标签模块错误码常量
 * Author : Zhou Hai
 * Date : 24/07/2025 17:59
 * Version : 1.0
 */
public interface DictLabelErrorCodeConstants {

    String I18B_PREFIX = "err.dict.label.";

    DictLabelErrorCode DICT_LABEL_NOT_EXIST = new DictLabelErrorCode("8001", "字典标签不存在");

    DictLabelErrorCode DICT_LABEL_KEY_VALUE_REPLICATION = new DictLabelErrorCode("8003", "字典标签KEY重复");

    DictLabelErrorCode DICT_LABEL_VALUE_REPLICATION = new DictLabelErrorCode("8002", "字典标签值重复");

    static class DictLabelErrorCode extends ErrorCode {

        private String i18nCode;

        public DictLabelErrorCode(String code, String message) {
            super(code, message);
            i18nCode = I18B_PREFIX + code;
        }

        public DictLabelErrorCode(String code, String i18nCode, String message) {
            super(code, message);
            this.i18nCode = i18nCode;
        }

        public String getI18nCode() {
            return i18nCode;
        }

    }
}