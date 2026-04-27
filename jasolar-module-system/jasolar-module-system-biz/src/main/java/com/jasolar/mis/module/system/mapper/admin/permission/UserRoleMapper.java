package com.jasolar.mis.module.system.mapper.admin.permission;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.jasolar.mis.framework.common.pojo.PageResult;
import com.jasolar.mis.framework.mybatis.core.mapper.BaseMapperX;
import com.jasolar.mis.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.jasolar.mis.module.system.controller.admin.permission.vo.role.RolePageReqVO;
import com.jasolar.mis.module.system.domain.admin.permission.UserRoleDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.Collection;
import java.util.List;

@Mapper
public interface UserRoleMapper extends BaseMapperX<UserRoleDO> {

    default List<UserRoleDO> selectListByUserId(Long userId) {
        return selectList(UserRoleDO::getUserId, userId);
    }

    default void deleteListByUserIdAndRoleIdIds(Long userId, Collection<Long> roleIds) {
        delete(new LambdaQueryWrapper<UserRoleDO>()
                .eq(UserRoleDO::getUserId, userId)
                .in(UserRoleDO::getRoleId, roleIds));
    }

    default void deleteListByUserId(Long userId) {
        delete(new LambdaQueryWrapper<UserRoleDO>().eq(UserRoleDO::getUserId, userId));
    }

    default void deleteListByRoleId(Long roleId) {
        delete(new LambdaQueryWrapper<UserRoleDO>().eq(UserRoleDO::getRoleId, roleId));
    }

    default List<UserRoleDO> selectListByRoleIds(Collection<Long> roleIds) {
        return selectList(UserRoleDO::getRoleId, roleIds);
    }

    default PageResult<UserRoleDO> selectPageByRoleId(RolePageReqVO rolePageReqVO) {
        return selectPage(rolePageReqVO, new LambdaQueryWrapperX<UserRoleDO>()
                .eqIfPresent(UserRoleDO::getRoleId, rolePageReqVO.getId())
                .orderByAsc(UserRoleDO::getCreateTime));
    }


    default List<UserRoleDO> selectListByRoleId(Long roleId) {
        return selectList( new LambdaQueryWrapperX<UserRoleDO>()
                .eqIfPresent(UserRoleDO::getRoleId, roleId)
                .orderByAsc(UserRoleDO::getCreateTime));
    }
}
