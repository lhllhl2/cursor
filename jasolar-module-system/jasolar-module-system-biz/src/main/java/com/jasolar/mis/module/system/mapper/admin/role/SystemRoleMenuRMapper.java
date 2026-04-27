package com.jasolar.mis.module.system.mapper.admin.role;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.jasolar.mis.framework.common.enums.CommonEnum;
import com.jasolar.mis.framework.mybatis.core.mapper.BaseMapperX;
import com.jasolar.mis.module.system.domain.admin.role.SystemRoleMenuRDo;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Description:
 * Author : Zhou Hai
 * Date : 24/07/2025 18:23
 * Version : 1.0
 */
@Mapper
public interface SystemRoleMenuRMapper extends BaseMapperX<SystemRoleMenuRDo> {



   default void logicDelMenuRole(Long id){

       LambdaUpdateWrapper<SystemRoleMenuRDo> updateWrapper = new LambdaUpdateWrapper<SystemRoleMenuRDo>()
               .eq(SystemRoleMenuRDo::getRoleId, id)
               .set(SystemRoleMenuRDo::getDeleted, CommonEnum.Deleted.YES.getStatus());
       update(updateWrapper);


   }

   /**
    * 根据角色ID查询菜单ID列表
    * @param roleId 角色ID
    * @return 菜单ID列表
    */
   default List<Long> selectMenuIdsByRoleId(Long roleId){
       LambdaQueryWrapper<SystemRoleMenuRDo> wrapper = new LambdaQueryWrapper<SystemRoleMenuRDo>()
               .eq(SystemRoleMenuRDo::getRoleId, roleId);
       List<SystemRoleMenuRDo> list = selectList(wrapper);
       return list.stream().map(SystemRoleMenuRDo::getMenuId).collect(Collectors.toList());
   }

   default List<SystemRoleMenuRDo> selectByRoleId(Long roleId){
       LambdaQueryWrapper<SystemRoleMenuRDo> eq = new LambdaQueryWrapper<SystemRoleMenuRDo>()
               .eq(SystemRoleMenuRDo::getRoleId, roleId);
       return selectList(eq);
   }


    default void deleteLogic(Long id, List<Long> deleteMenuIds){

        LambdaUpdateWrapper<SystemRoleMenuRDo> updateWrapper = new LambdaUpdateWrapper<SystemRoleMenuRDo>()
                .eq(SystemRoleMenuRDo::getRoleId, id)
                .in(SystemRoleMenuRDo::getMenuId,deleteMenuIds)
                .set(SystemRoleMenuRDo::getDeleted, CommonEnum.Deleted.YES.getStatus());
        update(updateWrapper);


    }

}
