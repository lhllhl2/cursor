package com.jasolar.mis.module.system.mapper.admin.permission;

import com.jasolar.mis.framework.common.pojo.PageResult;
import com.jasolar.mis.framework.mybatis.core.mapper.BaseMapperX;
import com.jasolar.mis.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.jasolar.mis.module.system.controller.admin.permission.vo.interfaceResource.InterfaceResourcePageReqVO;
import com.jasolar.mis.module.system.domain.admin.permission.InterfaceResourceDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 接口资源 Mapper
 *
 * @author zhahuang
 */
@Mapper
public interface InterfaceResourceMapper extends BaseMapperX<InterfaceResourceDO> {

    default PageResult<InterfaceResourceDO> selectPage(InterfaceResourcePageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<InterfaceResourceDO>()
                .likeIfPresent(InterfaceResourceDO::getServiceName, reqVO.getServiceName())
                .likeIfPresent(InterfaceResourceDO::getCategoryName, reqVO.getCategoryName())
                .likeIfPresent(InterfaceResourceDO::getControllerName, reqVO.getControllerName())
                .likeIfPresent(InterfaceResourceDO::getFunctionName, reqVO.getFunctionName())
                .likeIfPresent(InterfaceResourceDO::getName, reqVO.getName())
                .likeIfPresent(InterfaceResourceDO::getUrl, reqVO.getUrl())
                .eqIfPresent(InterfaceResourceDO::getMethod, reqVO.getMethod())
                .eqIfPresent(InterfaceResourceDO::getDescription, reqVO.getDescription())
                .eqIfPresent(InterfaceResourceDO::getStatus, reqVO.getStatus())
                .betweenIfPresent(InterfaceResourceDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(InterfaceResourceDO::getId));
    }


    /**
     * 拉取所有的服务名称
     *
     * @return 服务名称列表
     */
    @Select("""
            SELECT DISTINCT service_name
            FROM system_interface_resource
            WHERE service_name IS NOT NULL AND service_name != ''
            """)
    List<String> selectAllServiceName();


    /**
     * 拉取所有的分类名称
     *
     * @param serviceName 服务名称
     * @return 分类名称列表
     */
    @Select("""
            SELECT DISTINCT category_name
            FROM system_interface_resource
            WHERE service_name = #{serviceName} AND category_name IS NOT NULL AND category_name != ''
            """)
    List<String> selectAllCategoryName(String serviceName);

}