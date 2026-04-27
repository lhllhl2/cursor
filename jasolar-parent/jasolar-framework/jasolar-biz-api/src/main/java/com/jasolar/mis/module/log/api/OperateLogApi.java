package com.jasolar.mis.module.log.api;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.jasolar.mis.bizapi.Apis;
import com.jasolar.mis.framework.common.pojo.CommonResult;
import com.jasolar.mis.framework.common.pojo.PageResult;
import com.jasolar.mis.module.log.api.dto.OperateLogCreateReqDTO;
import com.jasolar.mis.module.log.api.dto.OperateLogPageReqDTO;
import com.jasolar.mis.module.log.api.dto.OperateLogRespDTO;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@FeignClient(name = Apis.LOG)
@Tag(name = "RPC 服务 - 操作日志")
public interface OperateLogApi {

    String PREFIX = Apis.LOG_PREFIX + "/operate-log";

    @PostMapping(PREFIX + "/create")
    @Operation(summary = "创建操作日志")
    CommonResult<Boolean> createOperateLog(@Valid @RequestBody OperateLogCreateReqDTO createReqDTO);

    /**
     * 【异步】创建操作日志
     *
     * @param createReqDTO 请求
     */
    @Async
    default void createOperateLogAsync(OperateLogCreateReqDTO createReqDTO) {
        try {
            createOperateLog(createReqDTO).checkError();
        } catch (Exception ignore) {
            // 忽略日志记录异常
        }
    }

    @PostMapping(PREFIX + "/page")
    @Operation(summary = "获取指定模块的指定数据的操作日志分页")
    CommonResult<PageResult<OperateLogRespDTO>> getOperateLogPage(@Valid @RequestBody OperateLogPageReqDTO pageReqDTO);

}
