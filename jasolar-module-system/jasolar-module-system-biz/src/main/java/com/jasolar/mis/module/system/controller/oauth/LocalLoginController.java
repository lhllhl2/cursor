package com.jasolar.mis.module.system.controller.oauth;

import com.jasolar.mis.framework.common.pojo.CommonResult;
import com.jasolar.mis.module.system.controller.oauth.vo.LocalLoginVo;
import com.jasolar.mis.module.system.oauth.LocalLoginResponse;
import com.jasolar.mis.module.system.oauth.TokenResponse;
import com.jasolar.mis.module.system.service.oauth.LocalLoginService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Description:
 * Author : Zhou Hai
 * Date : 05/09/2025 10:06
 * Version : 1.0
 */
@Tag(name = "管理后台 - localLogin")
@RequestMapping("/local")
@RestController
public class LocalLoginController {



    @Autowired
    private LocalLoginService localLoginService;

    @PostMapping("/login")
    public CommonResult<TokenResponse> localLogin(@RequestBody LocalLoginVo localLoginVo){
        LocalLoginResponse tokenResponse = localLoginService.localLogin(localLoginVo);
        return CommonResult.success(tokenResponse);
    }



    @PostMapping("/logout")
    public CommonResult<Void> localLogOut(){
        localLoginService.localLogout();
        return CommonResult.success();
    }


}
