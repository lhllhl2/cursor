package com.jasolar.mis.module.infra.api.biztask;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.jasolar.mis.bizapi.Apis;
import com.jasolar.mis.framework.common.pojo.CommonResult;
import com.jasolar.mis.framework.common.pojo.PageResult;
import com.jasolar.mis.module.infra.api.biztask.dto.BizTaskDTO;
import com.jasolar.mis.module.infra.api.biztask.dto.BizTaskQueryDTO;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * 业务待办API
 * 
 * @author galuo
 * @date 2025-04-14 16:04
 *
 */
@FeignClient(name = Apis.INFRA)
@Tag(name = "RPC 服务 - 业务待办流程实例")
public interface BizTaskApi {

    String PREFIX = Apis.INFRA_PREFIX + "/biztasks";

    /**
     * 查询待办
     * 
     * @param query
     * @return
     */
    @PostMapping(PREFIX + "/search")
    @Operation(summary = "查询待办")
    CommonResult<PageResult<BizTaskDTO>> search(@RequestBody BizTaskQueryDTO query);

}
