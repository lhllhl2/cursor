package com.jasolar.mis.module.system.api.permission;

import com.jasolar.mis.bizapi.Apis;
import com.jasolar.mis.framework.common.pojo.CommonResult;
import com.jasolar.mis.module.system.api.permission.dto.ApiImportResult;
import com.jasolar.mis.module.system.api.permission.dto.InterfaceResourceDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Collection;

/**
 * @author zhahuang
 */
@FeignClient(name = Apis.SYSTEM)
@Tag(name = "RPC 服务 - 接口")
public interface InterfaceResourceApi {

    String PREFIX = Apis.SYSTEM_PREFIX + "/api/resource";

    @PostMapping(PREFIX + "/import-data")
    @Operation(summary = "导入接口资源数据")
    CommonResult<ApiImportResult> importData(@RequestBody Collection<InterfaceResourceDTO> interfaceResourceList);
}
