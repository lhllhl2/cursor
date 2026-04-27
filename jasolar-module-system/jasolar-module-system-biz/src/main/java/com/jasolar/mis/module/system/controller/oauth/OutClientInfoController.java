package com.jasolar.mis.module.system.controller.oauth;

import com.jasolar.mis.framework.common.pojo.CommonResult;
import com.jasolar.mis.module.system.api.oauth.OutClientApi;
import com.jasolar.mis.module.system.oauth.ClientInfoResponse;
import com.jasolar.mis.module.system.service.oauth.OutClientInfoService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Description:
 * Author : Zhou Hai
 * Date : 04/11/2025 14:20
 * Version : 1.0
 */
@Tag(name = "管理后台 - sso")
@RequestMapping("/client-api")
@RestController
public class OutClientInfoController implements OutClientApi {

    @Autowired
    private OutClientInfoService outClientInfoService;

    @Override
    public CommonResult<ClientInfoResponse> getClientInfo(String clientName) {
        ClientInfoResponse clientInfo = outClientInfoService.getClientInfo(clientName);
        return CommonResult.success(clientInfo);
    }
}
