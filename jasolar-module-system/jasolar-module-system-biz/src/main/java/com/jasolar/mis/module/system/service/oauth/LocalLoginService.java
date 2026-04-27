package com.jasolar.mis.module.system.service.oauth;

import com.jasolar.mis.module.system.controller.oauth.vo.LocalLoginVo;
import com.jasolar.mis.module.system.oauth.LocalLoginResponse;

/**
 * Description:
 * Author : Zhou Hai
 * Date : 05/09/2025 10:09
 * Version : 1.0
 */
public interface LocalLoginService {


    /**
     * 本地登录
     * @param localLoginVo
     * @return
     */
    LocalLoginResponse localLogin(LocalLoginVo localLoginVo);


    /**
     * 登出
     */
    void localLogout();


}
