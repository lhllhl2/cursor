package com.jasolar.mis.module.system.service.permission;

import com.jasolar.mis.framework.api.InterfaceResourceCaller;
import com.jasolar.mis.module.system.api.permission.dto.ApiImportResult;
import com.jasolar.mis.module.system.api.permission.dto.InterfaceResourceDTO;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 系统模块自己实现InterfaceResourceCaller接口
 * @author zhahuang
 */
@Component
@Primary
public class SystemInterfaceResourceCallerImpl implements InterfaceResourceCaller {

    @Resource
    private InterfaceResourceService interfaceResourceService;

    @Override
    public ApiImportResult importData(List<InterfaceResourceDTO> resources) {
        return interfaceResourceService.importData(resources);
    }

    @Override
    public boolean isAvailable() {
        return true;
    }
}
