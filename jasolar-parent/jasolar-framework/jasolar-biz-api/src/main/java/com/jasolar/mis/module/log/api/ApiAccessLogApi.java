package com.jasolar.mis.module.log.api;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.jasolar.mis.bizapi.Apis;
import com.jasolar.mis.framework.common.pojo.CommonResult;
import com.jasolar.mis.framework.common.pojo.PageResult;
import com.jasolar.mis.module.log.api.dto.ApiAccessLogCreateReqDTO;
import com.jasolar.mis.module.log.api.dto.ApiAccessLogPageReqDTO;
import com.jasolar.mis.module.log.api.dto.ApiAccessLogRespDTO;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@FeignClient(name = Apis.LOG)
@Tag(name = "RPC 服务 - API 访问日志")
public interface ApiAccessLogApi {

    String PREFIX = Apis.LOG_PREFIX + "/api-access-log";

    @PostMapping(PREFIX + "/create")
    @Operation(summary = "创建 API 访问日志")
    CommonResult<Boolean> createApiAccessLog(@Valid @RequestBody ApiAccessLogCreateReqDTO createDTO);

    /**
     * 【异步】创建 API 访问日志
     *
     * @param createDTO 访问日志 DTO
     */
    @Async
    default void createApiAccessLogAsync(ApiAccessLogCreateReqDTO createDTO) {
        try {
            createApiAccessLog(createDTO).checkError();
        } catch (Exception ignore) {
            // 忽略日志记录异常
        }
    }

    @PostMapping(PREFIX + "/page")
    @Operation(summary = "获得API 访问日志分页")
    CommonResult<PageResult<ApiAccessLogRespDTO>> getApiAccessLogPage(@Valid @RequestBody ApiAccessLogPageReqDTO pageReqDTO);

}
