package com.jasolar.mis.module.system.api.oauth;

import com.jasolar.mis.framework.common.pojo.CommonResult;
import com.jasolar.mis.module.system.oauth.VerifyRequest;
import com.jasolar.mis.module.system.oauth.VerifyResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Description:
 * Author : Zhou Hai
 * Date : 17/07/2025 16:29
 * Version : 1.0
 */
@FeignClient(name = "jasolar-system-service", contextId = "authApi", path = "/oauth-api/auth")
public interface AuthApi {

    @PostMapping("/verify")
    CommonResult<VerifyResponse> verify(@RequestBody VerifyRequest verifyRequest,
                                        @RequestParam("token") String token);
}
