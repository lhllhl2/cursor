package com.jasolar.mis.module.system.mapper.admin.permission;

import com.jasolar.mis.framework.common.pojo.PageResult;
import com.jasolar.mis.framework.mybatis.core.mapper.BaseMapperX;
import com.jasolar.mis.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.jasolar.mis.module.system.controller.admin.permission.vo.interfaceResource.MenuInterfacePageReqVO;
import com.jasolar.mis.module.system.domain.admin.permission.MenuInterfaceDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 菜单接口关联 Mapper
 *
 * @author zhahuang
 */
@Mapper
public interface MenuInterfaceMapper extends BaseMapperX<MenuInterfaceDO> {


    /**
     * 获得菜单接口关联列表
     *
     * @param reqVO 菜单接口关联分页查询
     * @return 菜单接口关联列表
     */
    default PageResult<MenuInterfaceDO> selectPage(MenuInterfacePageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<MenuInterfaceDO>()
                .eqIfPresent(MenuInterfaceDO::getMenuId, reqVO.getMenuId())
                .eqIfPresent(MenuInterfaceDO::getInterfaceId, reqVO.getInterfaceId())
                .betweenIfPresent(MenuInterfaceDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(MenuInterfaceDO::getId));
    }


    /**
     * 获得菜单接口关联列表
     *
     * @param menuId 菜单编号
     * @return 菜单接口关联列表
     */
    default List<MenuInterfaceDO> selectListByMenuId(Long menuId) {
        return selectList(MenuInterfaceDO::getMenuId, menuId);
    }

    /**
     * 根据给定的菜单清单，获取菜单接口关联列表
     *
     * @param menuIds 菜单编号列表
     * @return 菜单接口关联列表
     */
    default List<MenuInterfaceDO> selectListByMenuIds(List<Long> menuIds) {
        return selectList(new LambdaQueryWrapperX<MenuInterfaceDO>().inIfPresent(MenuInterfaceDO::getMenuId, menuIds));
    }

    /**
     * 根据ID清单，进行物理删除
     *
     * @param ids ID清单
     * @return
     */
    int removeByIds(List<Long> ids);
}