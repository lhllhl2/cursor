package com.jasolar.mis.module.system.service.oauth;

import com.jasolar.mis.module.system.oauth.VerifyRequest;
import com.jasolar.mis.module.system.oauth.VerifyResponse;

/**
 * Description:
 * Author : Zhou Hai
 * Date : 22/07/2025 16:27
 * Version : 1.0
 */
public interface SysAuthService {

    /**
     * 鉴权
     * @param verifyRequest
     * @param token
     * @return
     */
    VerifyResponse verify(VerifyRequest verifyRequest, String token);
}
