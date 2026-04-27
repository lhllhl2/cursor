package com.jasolar.mis.framework.common.exception.enums;

import com.jasolar.mis.framework.common.exception.ErrorCode;

/**
 * Description:
 * Author : Zhou Hai
 * Date : 21/07/2025 17:00
 * Version : 1.0
 */
public interface UserErrorConstants {

    String I18B_PREFIX = "err.user.";

    UserErrorCode USER_NO_EXISTS = new UserErrorCode("3001","用户不存在");

    UserErrorCode USER_HAS_BAN = new UserErrorCode("3002","用户已禁用");


    UserErrorCode USER_NAME_OR_PWD_ERR = new UserErrorCode("3003","用户名或密码错误");

    UserErrorCode USER_PWD_NEED_CHANGE = new UserErrorCode("3004","用户初始密码须修改");

    UserErrorCode USER_TWO_MUST_NOT_EQ = new UserErrorCode("3005","新密码和原密码不能一致");

    UserErrorCode USER_PWD_ERR = new UserErrorCode("3006","原密码错误");

    UserErrorCode USER_PWD_TWICE_NOT_SAME = new UserErrorCode("3007","新密码两次输入不一致！");



    static class UserErrorCode extends ErrorCode{


        private String i18nCode;

        public UserErrorCode(String code, String message) {
            super(code, message);
            i18nCode = I18B_PREFIX + code;
        }

        public UserErrorCode(String code, String i18nCode, String message) {
            super(code, message);
            this.i18nCode = i18nCode;
        }

        public String getI18nCode() {
            return i18nCode;
        }
    }




}
