package com.jasolar.mis.module.masterdata.api.consignCenter;

import com.jasolar.mis.bizapi.Apis;
import com.jasolar.mis.framework.common.pojo.CommonResult;
import com.jasolar.mis.module.masterdata.dto.ConsignCenterRespDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(name = Apis.MASTERDATA)
@Tag(name = "收货中心信息")
public interface ConsignCenterApi {

    String PREFIX = Apis.MASTERDATA_PREFIX + "/consign-center";

    @GetMapping(PREFIX +"/get")
    @Operation(summary = "查询收货中心信息")
    CommonResult<ConsignCenterRespDTO> getByCode(@RequestParam("code") String code);

    @PostMapping(PREFIX + "/list")
    @Operation(summary = "批量查询收货中心信息")
    CommonResult<List<ConsignCenterRespDTO>> listByCodes(@RequestBody List<String> codes);

    @GetMapping(PREFIX +"/listByCodeOrName")
    @Operation(summary = "查询收货中心信息通过code和name模糊查询")
    CommonResult<List<ConsignCenterRespDTO>> listByCodeOrName(@RequestParam(value = "codeOrName", required = false) String codeOrName,
                                                              @RequestParam(value = "codes", required = false) List<String> codes);
}
