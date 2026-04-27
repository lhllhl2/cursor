package com.jasolar.mis.module.infra.api.sn;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.jasolar.mis.bizapi.Apis;
import com.jasolar.mis.framework.common.pojo.CommonResult;

import io.swagger.v3.oas.annotations.Operation;

@FeignClient(name = Apis.INFRA)
public interface DocumentSnApi {

    String PREFIX = Apis.INFRA_PREFIX + "/sn";

    @GetMapping(PREFIX + "/current")
    @Operation(summary = "根据单据类型、当前日期值查询单据序列号")
    CommonResult<Long> currentSerialNumber(@RequestParam("type") String type, @RequestParam("dateValue") String dateValue);

}
