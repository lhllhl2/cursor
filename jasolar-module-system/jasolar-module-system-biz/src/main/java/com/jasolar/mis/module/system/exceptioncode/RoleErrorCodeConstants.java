package com.jasolar.mis.module.system.exceptioncode;

import com.jasolar.mis.framework.common.exception.ErrorCode;

/**
 * Description:
 * Author : Zhou Hai
 * Date : 24/07/2025 17:59
 * Version : 1.0
 */
public interface RoleErrorCodeConstants {


    String I18B_PREFIX = "err.role.";


    RoleErrorCode ROLE_NAME_REPLICATION = new RoleErrorCode("6001","角色名称重复");

    RoleErrorCode ROLE_CODE_REPLICATION = new RoleErrorCode("6002","角色编码重复");

    RoleErrorCode ROLE_NOT_EXIST = new RoleErrorCode("6003","角色不存在");




    static class RoleErrorCode extends ErrorCode {



        private String i18nCode;

        public RoleErrorCode(String code, String message) {
            super(code, message);
            i18nCode = I18B_PREFIX + code;
        }

        public RoleErrorCode(String code, String i18nCode, String message) {
            super(code, message);
            this.i18nCode = i18nCode;
        }

        public String getI18nCode() {
            return i18nCode;
        }

    }
}
