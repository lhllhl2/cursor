package com.jasolar.mis.module.system.service.permission;

import cn.hutool.core.collection.CollUtil;
import com.jasolar.mis.framework.common.pojo.PageResult;
import com.jasolar.mis.framework.common.util.object.BeanUtils;
import com.jasolar.mis.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.jasolar.mis.module.system.api.permission.dto.ApiImportResult;
import com.jasolar.mis.module.system.api.permission.dto.InterfaceResourceDTO;
import com.jasolar.mis.module.system.controller.admin.permission.vo.interfaceResource.InterfaceResourcePageReqVO;
import com.jasolar.mis.module.system.controller.admin.permission.vo.interfaceResource.InterfaceResourceSaveReqVO;
import com.jasolar.mis.module.system.domain.admin.permission.InterfaceResourceDO;
import com.jasolar.mis.module.system.mapper.admin.permission.InterfaceResourceMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.Collection;
import java.util.List;

import static com.jasolar.mis.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.jasolar.mis.module.system.enums.ErrorCodeConstants.INTERFACE_RESOURCE_NOT_EXISTS;

/**
 * 接口资源 Service 实现类
 *
 * @author 举个栗子
 */
@Service
@Validated
public class InterfaceResourceServiceImpl implements InterfaceResourceService {

    @Resource
    private InterfaceResourceMapper interfaceResourceMapper;

    @Override
    public Long createInterfaceResource(InterfaceResourceSaveReqVO createReqVO) {
        // 插入
        InterfaceResourceDO interfaceResource = BeanUtils.toBean(createReqVO, InterfaceResourceDO.class);
        interfaceResourceMapper.insert(interfaceResource);
        // 返回
        return interfaceResource.getId();
    }

    @Override
    public void updateInterfaceResource(InterfaceResourceSaveReqVO updateReqVO) {
        // 校验存在
        validateInterfaceResourceExists(updateReqVO.getId());
        // 更新
        InterfaceResourceDO updateObj = BeanUtils.toBean(updateReqVO, InterfaceResourceDO.class);
        interfaceResourceMapper.updateById(updateObj);
    }

    @Override
    public void deleteInterfaceResource(Long id) {
        // 校验存在
        validateInterfaceResourceExists(id);
        // 删除
        interfaceResourceMapper.deleteById(id);
    }

    private void validateInterfaceResourceExists(Long id) {
        if (interfaceResourceMapper.selectById(id) == null) {
            throw exception(INTERFACE_RESOURCE_NOT_EXISTS);
        }
    }

    @Override
    public InterfaceResourceDO getInterfaceResource(Long id) {
        return interfaceResourceMapper.selectById(id);
    }

    @Override
    public PageResult<InterfaceResourceDO> getInterfaceResourcePage(InterfaceResourcePageReqVO pageReqVO) {
        return interfaceResourceMapper.selectPage(pageReqVO);
    }

    @Override
    public ApiImportResult importData(Collection<InterfaceResourceDTO> interfaceResourceDTOList) {

        ApiImportResult apiImportResult = new ApiImportResult();

        if (CollUtil.isEmpty(interfaceResourceDTOList)) {
            return apiImportResult;
        }

        // 统计导入的接口资源数量
        apiImportResult.setTotalCount(interfaceResourceDTOList.size());
        int successCount = 0;
        int failCount = 0;
        int existCount = 0;
        for (InterfaceResourceDTO interfaceResourceDTO : interfaceResourceDTOList) {
            InterfaceResourceDO interfaceResourceDO = BeanUtils.toBean(interfaceResourceDTO, InterfaceResourceDO.class);
            LambdaQueryWrapperX<InterfaceResourceDO> queryWrapperX = new LambdaQueryWrapperX<InterfaceResourceDO>()
                    .eqIfPresent(InterfaceResourceDO::getServiceName, interfaceResourceDO.getServiceName())
                    .eqIfPresent(InterfaceResourceDO::getUrl, interfaceResourceDO.getUrl())
                    .eqIfPresent(InterfaceResourceDO::getMethod, interfaceResourceDO.getMethod());
            // 判断是否存在
            InterfaceResourceDO result = interfaceResourceMapper.selectOne(queryWrapperX);
            if (result != null) {
                existCount++;
                if (!result.equals(interfaceResourceDO)) {
                    //对比发现不一致，则更新
                    interfaceResourceDO.setId(result.getId());
                    interfaceResourceMapper.updateById(interfaceResourceDO);
                }
                //否则则跳过
                continue;
            }
            // 不存在则插入
            int a = interfaceResourceMapper.insert(interfaceResourceDO);
            if (a > 0) {
                successCount++;
            } else {
                failCount++;
            }
        }
        apiImportResult.setSuccessCount(successCount);
        apiImportResult.setExistCount(existCount);
        apiImportResult.setFailureCount(failCount);
        return apiImportResult;
    }

    @Override
    public List<String> getAllServiceName() {
        return interfaceResourceMapper.selectAllServiceName();
    }

    @Override
    public List<String> getAllCategoryNamesByServiceName(String serviceName) {
        return interfaceResourceMapper.selectAllCategoryName(serviceName);
    }
}