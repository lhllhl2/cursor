package com.jasolar.mis.module.system.api.oauth2;

import com.jasolar.mis.bizapi.Apis;
import com.jasolar.mis.framework.common.pojo.CommonResult;
import com.jasolar.mis.module.system.api.oauth2.dto.OAuth2AccessTokenCheckRespDTO;
import com.jasolar.mis.module.system.api.oauth2.dto.OAuth2AccessTokenCreateReqDTO;
import com.jasolar.mis.module.system.api.oauth2.dto.OAuth2AccessTokenRespDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = Apis.SYSTEM)
@Tag(name = "RPC 服务 - OAuth2.0 令牌")
public interface OAuth2TokenApi {

    String PREFIX = Apis.SYSTEM_PREFIX + "/oauth2/token";

    /** 校验 Token 的 URL 地址，主要是提供给 Gateway 使用 */
    String URL_CHECK = "http://" + Apis.SYSTEM + PREFIX + "/check";

    @PostMapping(PREFIX + "/create")
    @Operation(summary = "创建访问令牌")
    CommonResult<OAuth2AccessTokenRespDTO> createAccessToken(@Valid @RequestBody OAuth2AccessTokenCreateReqDTO reqDTO);

    @GetMapping(PREFIX + "/check")
    @Operation(summary = "校验访问令牌")
    @Parameter(name = "accessToken", description = "访问令牌", required = true, example = "tudou")
    CommonResult<OAuth2AccessTokenCheckRespDTO> checkAccessToken(@RequestParam("accessToken") String accessToken);

    @DeleteMapping(PREFIX + "/remove")
    @Operation(summary = "移除访问令牌")
    @Parameter(name = "accessToken", description = "访问令牌", required = true, example = "tudou")
    CommonResult<OAuth2AccessTokenRespDTO> removeAccessToken(@RequestParam("accessToken") String accessToken);

    @PutMapping(PREFIX + "/refresh")
    @Operation(summary = "刷新访问令牌")
    @Parameter(name = "refreshToken", description = "刷新令牌", required = true, example = "haha")
    @Parameter(name = "clientId", description = "客户端编号", required = true, example = "fiifoxconnyuanma")
    CommonResult<OAuth2AccessTokenRespDTO> refreshAccessToken(@RequestParam("refreshToken") String refreshToken,
            @RequestParam("clientId") String clientId);

}
