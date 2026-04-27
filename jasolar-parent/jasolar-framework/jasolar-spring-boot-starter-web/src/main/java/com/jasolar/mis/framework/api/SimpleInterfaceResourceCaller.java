package com.jasolar.mis.framework.api;

import com.jasolar.mis.framework.common.pojo.CommonResult;
import com.jasolar.mis.module.system.api.permission.InterfaceResourceApi;
import com.jasolar.mis.module.system.api.permission.dto.ApiImportResult;
import com.jasolar.mis.module.system.api.permission.dto.InterfaceResourceDTO;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * InterfaceResourceCaller的默认实现
 * 通过Feign API调用system-service
 */
@Slf4j
@AllArgsConstructor
public class SimpleInterfaceResourceCaller implements InterfaceResourceCaller {

    private InterfaceResourceApi interfaceResourceApi;

    @Override
    public ApiImportResult importData(List<InterfaceResourceDTO> resources) {
        if (!isAvailable()) {
            log.warn("InterfaceResourceApi不可用，无法导入接口资源");
            return new ApiImportResult();
        }
        try {
            CommonResult<ApiImportResult> result = this.interfaceResourceApi.importData(resources);
            if (result != null && result.isSuccess()) {
                return result.getData();
            }
            log.error("调用接口资源导入API失败: {}", result != null ? result.getMsg() : "未知错误");
        } catch (Exception e) {
            log.error("调用接口资源导入API异常", e);
        }
        return new ApiImportResult();
    }

    @Override
    public boolean isAvailable() {
        return interfaceResourceApi != null;
    }
} 