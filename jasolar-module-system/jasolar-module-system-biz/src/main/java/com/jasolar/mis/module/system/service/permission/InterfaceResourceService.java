package com.jasolar.mis.module.system.service.permission;

import com.jasolar.mis.framework.common.pojo.PageResult;
import com.jasolar.mis.module.system.api.permission.dto.ApiImportResult;
import com.jasolar.mis.module.system.api.permission.dto.InterfaceResourceDTO;
import com.jasolar.mis.module.system.controller.admin.permission.vo.interfaceResource.InterfaceResourcePageReqVO;
import com.jasolar.mis.module.system.controller.admin.permission.vo.interfaceResource.InterfaceResourceSaveReqVO;
import com.jasolar.mis.module.system.domain.admin.permission.InterfaceResourceDO;
import jakarta.validation.Valid;

import java.util.Collection;
import java.util.List;

/**
 * 接口资源 Service 接口
 *
 * @author 举个栗子
 */
public interface InterfaceResourceService {

    /**
     * 创建接口资源
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createInterfaceResource(@Valid InterfaceResourceSaveReqVO createReqVO);

    /**
     * 更新接口资源
     *
     * @param updateReqVO 更新信息
     */
    void updateInterfaceResource(@Valid InterfaceResourceSaveReqVO updateReqVO);

    /**
     * 删除接口资源
     *
     * @param id 编号
     */
    void deleteInterfaceResource(Long id);

    /**
     * 获得接口资源
     *
     * @param id 编号
     * @return 接口资源
     */
    InterfaceResourceDO getInterfaceResource(Long id);

    /**
     * 获得接口资源分页
     *
     * @param pageReqVO 分页查询
     * @return 接口资源分页
     */
    PageResult<InterfaceResourceDO> getInterfaceResourcePage(InterfaceResourcePageReqVO pageReqVO);


    /**
     * 导入接口资源
     * @param interfaceResourceDTOList 接口资源列表
     *
     */
    ApiImportResult importData(Collection<InterfaceResourceDTO> interfaceResourceDTOList);


    /**
     * 根据给定的菜单清单，获取菜单接口关联列表
     * @return  菜单接口关联列表
     */
    List<String> getAllServiceName();

    /**
     * 根据给定的菜单清单，获取菜单接口关联列表
     * @param serviceName
     * @return
     */
    List<String> getAllCategoryNamesByServiceName(String serviceName);}