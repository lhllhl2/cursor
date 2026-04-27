package com.jasolar.mis.module.system.exceptioncode;

import com.jasolar.mis.framework.common.exception.ErrorCode;

/**
 * Description: 字典模块错误码常量
 * Author : Zhou Hai
 * Date : 24/07/2025 17:59
 * Version : 1.0
 */
public interface DictErrorCodeConstants {

    String I18B_PREFIX = "err.dict.";

    DictErrorCode DICT_CODE_REPLICATION = new DictErrorCode("7001", "字典编码重复");
    
    DictErrorCode DICT_TITLE_REPLICATION = new DictErrorCode("7002", "字典标题重复");
    
    DictErrorCode DICT_NOT_EXIST = new DictErrorCode("7003", "字典不存在");

    static class DictErrorCode extends ErrorCode {

        private String i18nCode;

        public DictErrorCode(String code, String message) {
            super(code, message);
            i18nCode = I18B_PREFIX + code;
        }

        public DictErrorCode(String code, String i18nCode, String message) {
            super(code, message);
            this.i18nCode = i18nCode;
        }

        public String getI18nCode() {
            return i18nCode;
        }

    }
}