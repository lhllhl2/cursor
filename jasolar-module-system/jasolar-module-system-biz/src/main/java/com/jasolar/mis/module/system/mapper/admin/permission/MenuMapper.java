package com.jasolar.mis.module.system.mapper.admin.permission;

import com.jasolar.mis.framework.common.pojo.PageResult;
import com.jasolar.mis.framework.mybatis.core.mapper.BaseMapperX;
import com.jasolar.mis.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.jasolar.mis.module.system.controller.admin.permission.resp.SimpleMenuResp;
import com.jasolar.mis.module.system.controller.admin.permission.vo.menu.MenuListReqVO;
import com.jasolar.mis.module.system.controller.admin.permission.vo.menu.MenuReqVO;
import com.jasolar.mis.module.system.domain.admin.permission.MenuDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface MenuMapper extends BaseMapperX<MenuDO> {

    default MenuDO selectByParentIdAndName(Long parentId, String name) {
        return selectOne(MenuDO::getPid, parentId, MenuDO::getName, name);
    }

    default MenuDO selectByName(String name) {
        return selectOne(MenuDO::getName, name);
    }

    default Long selectCountByParentId(Long parentId) {
        return selectCount(MenuDO::getPid, parentId);
    }

    default List<MenuDO> selectList(MenuListReqVO reqVO) {
        return selectList(new LambdaQueryWrapperX<MenuDO>()
                .likeIfPresent(MenuDO::getName, reqVO.getName())
                .eqIfPresent(MenuDO::getType, reqVO.getType())
                .likeIfPresent(MenuDO::getAuthCode, reqVO.getAuthCode())
                .likeIfPresent(MenuDO::getPath, reqVO.getPath())
                .eqIfPresent(MenuDO::getStatus, reqVO.getStatus())
                .orderByAsc(MenuDO::getMenuOrder));
    }

    default List<MenuDO> selectList(MenuReqVO reqVO) {
        return selectList(new LambdaQueryWrapperX<MenuDO>()
                .likeIfPresent(MenuDO::getName, reqVO.getName())
                .eqIfPresent(MenuDO::getType, reqVO.getType())
                .likeIfPresent(MenuDO::getAuthCode, reqVO.getAuthCode())
                .likeIfPresent(MenuDO::getPath, reqVO.getPath())
                .eqIfPresent(MenuDO::getStatus, reqVO.getStatus())
                .orderByAsc(MenuDO::getMenuOrder));
    }

    default PageResult<MenuDO> selectPage(MenuListReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<MenuDO>()
                .likeIfPresent(MenuDO::getName, reqVO.getName())
                .eqIfPresent(MenuDO::getType, reqVO.getType())
                .likeIfPresent(MenuDO::getAuthCode, reqVO.getAuthCode())
                .likeIfPresent(MenuDO::getPath, reqVO.getPath())
                .eqIfPresent(MenuDO::getStatus, reqVO.getStatus())
                .orderByAsc(MenuDO::getMenuOrder));
    }

    default List<MenuDO> selectListByPermission(String permission) {
        return selectList(MenuDO::getAuthCode, permission);
    }

    List<MenuDO> getSimpleByRoleIds(@Param("roleIds") List<Long> roleIds);


    List<SimpleMenuResp> getAllMenu();

}
