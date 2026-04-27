package com.jasolar.mis.framework.api;

import com.jasolar.mis.module.system.api.permission.dto.ApiImportResult;
import com.jasolar.mis.module.system.api.permission.dto.InterfaceResourceDTO;

import java.util.List;

/**
 * 接口资源调用抽象接口
 * 用于解耦API扫描与实际导入逻辑
 */
public interface InterfaceResourceCaller {

    /**
     * 导入接口资源数据
     * @param resources 接口资源列表
     * @return 导入结果
     */
    ApiImportResult importData(List<InterfaceResourceDTO> resources);
    
    /**
     * 检查接口资源是否可用
     * @return true表示可用
     */
    boolean isAvailable();
} 