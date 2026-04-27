package com.jasolar.mis.module.system.mapper.admin.role;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.jasolar.mis.framework.common.enums.CommonEnum;
import com.jasolar.mis.framework.mybatis.core.mapper.BaseMapperX;
import com.jasolar.mis.module.system.domain.admin.role.SystemUserGroupRoleRDo;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * Description:
 * Author : Zhou Hai
 * Date : 24/07/2025 18:36
 * Version : 1.0
 */
@Mapper
public interface SystemUserGroupRoleRMapper extends BaseMapperX<SystemUserGroupRoleRDo> {


    default void logicDelUserGroupRole(Long id){
        LambdaUpdateWrapper<SystemUserGroupRoleRDo> updateWrapper = new LambdaUpdateWrapper<SystemUserGroupRoleRDo>()
                .eq(SystemUserGroupRoleRDo::getRoleId, id)
                .set(SystemUserGroupRoleRDo::getDeleted, CommonEnum.Deleted.YES.getStatus());

        update(updateWrapper);


    }

   default List<SystemUserGroupRoleRDo> selectByRoleId(Long roleId){

       LambdaQueryWrapper<SystemUserGroupRoleRDo> eq = new LambdaQueryWrapper<SystemUserGroupRoleRDo>()
               .eq(SystemUserGroupRoleRDo::getRoleId, roleId);
       return selectList(eq);

   }

    default void deleteLogic(Long id, List<Long> delGroupIds){

        LambdaUpdateWrapper<SystemUserGroupRoleRDo> updateWrapper = new LambdaUpdateWrapper<SystemUserGroupRoleRDo>()
                .eq(SystemUserGroupRoleRDo::getRoleId, id)
                .in(SystemUserGroupRoleRDo::getGroupId,delGroupIds)
                .set(SystemUserGroupRoleRDo::getDeleted, CommonEnum.Deleted.YES.getStatus());

        update(updateWrapper);

    }
}
