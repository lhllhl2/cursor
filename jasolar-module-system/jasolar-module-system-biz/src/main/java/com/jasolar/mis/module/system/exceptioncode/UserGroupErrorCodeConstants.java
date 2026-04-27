package com.jasolar.mis.module.system.exceptioncode;

import com.jasolar.mis.framework.common.exception.ErrorCode;

/**
 * Description:
 * Author : Zhou Hai
 * Date : 23/07/2025 13:37
 * Version : 1.0
 */
public interface UserGroupErrorCodeConstants {

    String I18B_PREFIX = "err.userGroup.";


    UserGroupErrorCode NAME_REPLICATION = new UserGroupErrorCode("4001","用户组名称重复");

    UserGroupErrorCode USER_GROUP_NOT_EXIST = new UserGroupErrorCode("4002","用户组不存在");




     static class UserGroupErrorCode extends ErrorCode {

        private String i18nCode;

        public UserGroupErrorCode(String code, String message) {
            super(code, message);
            i18nCode = I18B_PREFIX + code;
        }

        public UserGroupErrorCode(String code, String i18nCode, String message) {
            super(code, message);
            this.i18nCode = i18nCode;
        }

        public String getI18nCode() {
            return i18nCode;
        }

    }
}
