package com.jasolar.mis.module.system.service.oauth;

import com.jasolar.mis.module.system.oauth.SsoCallbackParams;
import com.jasolar.mis.module.system.oauth.SsoLogInUrlInfo;
import com.jasolar.mis.module.system.oauth.TokenResponse;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Description:
 * Author : Zhou Hai
 * Date : 18/07/2025 11:00
 * Version : 1.0
 */
public interface OauthSsoService {


    /**
     * 获取 sso 的登录地址
     * @return
     */
    SsoLogInUrlInfo getSsoLoginRedirectUrl();

    /**
     * sso 回调处理
     * @return
     */
    TokenResponse ssoCallBack(SsoCallbackParams ssoCallbackParams);

    /**
     * 登出
     * @param response
     */
    void logout(HttpServletResponse response);

}
