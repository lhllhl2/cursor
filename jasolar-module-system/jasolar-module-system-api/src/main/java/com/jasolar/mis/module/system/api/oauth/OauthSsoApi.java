package com.jasolar.mis.module.system.api.oauth;

import com.jasolar.mis.framework.common.pojo.CommonResult;
import com.jasolar.mis.module.system.oauth.SsoLogInUrlInfo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

/**
 * Minimal SSO API contract exposed for gateway redirection flows.
 */
@FeignClient(name = "jasolar-system-service", contextId = "oauthSsoApi", path = "/oauth-api/sso")
public interface OauthSsoApi {

    @PostMapping("/getSsoLoginRedirectUrl")
    CommonResult<SsoLogInUrlInfo> getSsoLoginRedirectUrl();
}
