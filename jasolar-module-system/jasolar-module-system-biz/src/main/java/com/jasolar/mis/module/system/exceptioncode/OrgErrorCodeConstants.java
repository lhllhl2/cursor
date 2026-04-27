package com.jasolar.mis.module.system.exceptioncode;

import com.jasolar.mis.framework.common.exception.ErrorCode;

/**
 * Description: 组织机构模块错误码常量
 * Author : lingma
 * Date : 24/07/2025 18:00
 * Version : 1.0
 */
public interface OrgErrorCodeConstants {

    String I18B_PREFIX = "err.org.";

    OrgErrorCode ORG_NOT_EXIST = new OrgErrorCode("9001", "组织机构不存在");

    OrgErrorCode ORG_NAME_REPLICATION = new OrgErrorCode("9002", "组织机构名称重复");

    OrgErrorCode ORG_CODE_REPLICATION = new OrgErrorCode("9003", "组织机构编码重复");
    
    // 添加组织已存在的错误码
    OrgErrorCode ORG_ALREADY_EXISTS = new OrgErrorCode("9004", "组织机构已存在");

    static class OrgErrorCode extends ErrorCode {

        private String i18nCode;

        public OrgErrorCode(String code, String message) {
            super(code, message);
            i18nCode = I18B_PREFIX + code;
        }

        public OrgErrorCode(String code, String i18nCode, String message) {
            super(code, message);
            this.i18nCode = i18nCode;
        }

        public String getI18nCode() {
            return i18nCode;
        }

    }
}