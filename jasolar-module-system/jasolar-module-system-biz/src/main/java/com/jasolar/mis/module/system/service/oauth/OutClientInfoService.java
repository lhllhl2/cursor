package com.jasolar.mis.module.system.service.oauth;

import com.jasolar.mis.module.system.oauth.ClientInfoResponse;

/**
 * Description:
 * Author : Zhou Hai
 * Date : 04/11/2025 14:28
 * Version : 1.0
 */
public interface OutClientInfoService {


    ClientInfoResponse getClientInfo(String clientName);
}
