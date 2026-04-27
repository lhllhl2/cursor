package com.jasolar.mis.module.log.api;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import com.jasolar.mis.bizapi.Apis;
import com.jasolar.mis.framework.common.pojo.CommonResult;
import com.jasolar.mis.framework.common.pojo.PageResult;
import com.jasolar.mis.module.log.api.dto.ApiErrorLogCreateReqDTO;
import com.jasolar.mis.module.log.api.dto.ApiErrorLogPageReqDTO;
import com.jasolar.mis.module.log.api.dto.ApiErrorLogRespDTO;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@FeignClient(name = Apis.LOG)
@Tag(name = "RPC 服务 - API 异常日志")
public interface ApiErrorLogApi {

    String PREFIX = Apis.LOG_PREFIX + "/api-error-log";

    @PostMapping(PREFIX + "/create")
    @Operation(summary = "创建 API 异常日志")
    CommonResult<Boolean> createApiErrorLog(@Valid @RequestBody ApiErrorLogCreateReqDTO createDTO);

    /**
     * 【异步】创建 API 异常日志
     *
     * @param createDTO 异常日志 DTO
     */
    @Async
    default void createApiErrorLogAsync(ApiErrorLogCreateReqDTO createDTO) {
        try {
            createApiErrorLog(createDTO).checkError();
        } catch (Exception ignore) {
            // 忽略日志记录异常
        }
    }

    @PostMapping(PREFIX + "/page")
    @Operation(summary = "获得 API 错误日志分页")
    CommonResult<PageResult<ApiErrorLogRespDTO>> getApiErrorLogPage(@Valid @RequestBody ApiErrorLogPageReqDTO pageReqDTO);

    @PutMapping("/update-status")
    @Operation(summary = "更新 API 错误日志的状态")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @Parameter(name = "processStatus", description = "处理状态", required = true, example = "1")
    CommonResult<Boolean> updateApiErrorLogProcess(@RequestParam("id") Long id, @RequestParam("processStatus") Integer processStatus,
            @RequestParam("processUserId") Long processUserId);
}
