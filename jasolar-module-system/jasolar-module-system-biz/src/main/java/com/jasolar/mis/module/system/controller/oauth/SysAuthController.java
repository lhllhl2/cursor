package com.jasolar.mis.module.system.controller.oauth;

import com.jasolar.mis.framework.common.pojo.CommonResult;
import com.jasolar.mis.module.system.api.oauth.AuthApi;
import com.jasolar.mis.module.system.oauth.VerifyRequest;
import com.jasolar.mis.module.system.oauth.VerifyResponse;
import com.jasolar.mis.module.system.service.oauth.SysAuthService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Description:
 * Author : Zhou Hai
 * Date : 17/07/2025 16:37
 * Version : 1.0
 */
@RestController
@RequestMapping("/auth")
@Slf4j
public class SysAuthController implements AuthApi{

    @Autowired
    private SysAuthService sysAuthService;

    @Override
    public CommonResult<VerifyResponse> verify(@RequestBody VerifyRequest verifyRequest,@RequestParam("token") String token) {
        VerifyResponse verifyResponse = sysAuthService.verify(verifyRequest,token);
        return CommonResult.success(verifyResponse);
    }



}
