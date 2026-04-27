package com.jasolar.mis.framework.common.exception.enums;

import com.jasolar.mis.framework.common.exception.ErrorCode;

/**
 * Description:
 * Author : Zhou Hai
 * Date : 18/07/2025 10:17
 * Version : 1.0
 */
public interface AuthErrorCodeConstants {

    String I18B_PREFIX = "err.auth.";





    AuthErrorCode LOGIN_STATE_UN_NORMAL = new AuthErrorCode("2001","登录状态有误");

    AuthErrorCode LOGIN_TOKEN_EMPTY = new AuthErrorCode("2002","token为空");

    AuthErrorCode AUTH_VERIFY_ERROR = new AuthErrorCode("2003","权限校验有误");

    AuthErrorCode GET_LOGIN_URL_ERROR = new AuthErrorCode("2004","获取登录地址有误");

    AuthErrorCode GET_OAUTH_TOKEN_ERROR = new AuthErrorCode("2005","oauth token 获取有误");

    AuthErrorCode TOKEN_VERIFY_ERROR = new AuthErrorCode("2006","token 校验失败，请重新登录");

    AuthErrorCode AUTH_EXPIRE = new AuthErrorCode("2007","登录权限过期，请重新登录");

    AuthErrorCode AUTH_SSO_URL_GET = new AuthErrorCode("2008","SSO登录地址获取失败");



    static class AuthErrorCode extends ErrorCode{

        private String i18nCode;

        public AuthErrorCode(String code, String message) {
            super(code, message);
            i18nCode = I18B_PREFIX + code;
        }

        public AuthErrorCode(String code, String i18nCode, String message) {
            super(code, message);
            this.i18nCode = i18nCode;
        }

        public String getI18nCode() {
            return i18nCode;
        }


    }


}
