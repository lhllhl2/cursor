package com.jasolar.mis.module.system.controller.oauth;

import com.jasolar.mis.framework.common.pojo.CommonResult;
import com.jasolar.mis.module.system.api.oauth.OauthSsoApi;
import com.jasolar.mis.module.system.oauth.SsoCallbackParams;
import com.jasolar.mis.module.system.oauth.SsoLogInUrlInfo;
import com.jasolar.mis.module.system.oauth.TokenResponse;
import com.jasolar.mis.module.system.service.oauth.OauthSsoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * Description:
 * Author : Zhou Hai
 * Date : 18/07/2025 10:57
 * Version : 1.0
 */
@Tag(name = "管理后台 - sso")
@RequestMapping("/sso")
@RestController
public class OauthSsoController implements OauthSsoApi  {


    @Autowired
    private OauthSsoService oauthSsoService;

    @Operation(summary = "1.获取sso登录地址")
//    @PostMapping("/getSsoLoginRedirectUrl")
    public CommonResult<SsoLogInUrlInfo> getSsoLoginRedirectUrl() {
        SsoLogInUrlInfo ssoLogInUrlInfo = oauthSsoService.getSsoLoginRedirectUrl();
        return CommonResult.success(ssoLogInUrlInfo);
    }


    @PostMapping("/oauth2/callback")
    public CommonResult<TokenResponse> ssoCallBack(@RequestBody SsoCallbackParams ssoCallbackParams){
        TokenResponse tokenResponse = oauthSsoService.ssoCallBack(ssoCallbackParams);
        return CommonResult.success(tokenResponse);
    }



    @GetMapping("/oauth2/logout")
    public void logout( HttpServletResponse response){
        oauthSsoService.logout(response);

    }











}
