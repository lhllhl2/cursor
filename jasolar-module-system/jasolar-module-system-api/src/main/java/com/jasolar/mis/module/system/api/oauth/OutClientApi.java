package com.jasolar.mis.module.system.api.oauth;

import com.jasolar.mis.framework.common.pojo.CommonResult;
import com.jasolar.mis.module.system.oauth.ClientInfoResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Lightweight client information API contract for outer system integrations.
 */
@FeignClient(name = "jasolar-system-service", contextId = "outClientApi", path = "/oauth-api/out-client")
public interface OutClientApi {

    @GetMapping("/getClientInfo")
    CommonResult<ClientInfoResponse> getClientInfo(@RequestParam("clientName") String clientName);
}
