package com.jasolar.mis.module.system.mapper.admin.usergroup;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.jasolar.mis.framework.common.enums.CommonEnum;
import com.jasolar.mis.framework.mybatis.core.mapper.BaseMapperX;
import com.jasolar.mis.module.system.domain.admin.usergroup.SystemUserGroupRDo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * Description:
 * Author : Zhou Hai
 * Date : 23/07/2025 14:23
 * Version : 1.0
 */
@Mapper
public interface SystemUserGroupRMapper extends BaseMapperX<SystemUserGroupRDo>
{

    void logicDelByGroupId(@Param("id") Long id);

    default List<SystemUserGroupRDo> searchByGroupId(Long groupId){

        LambdaQueryWrapper<SystemUserGroupRDo> wrapper = new LambdaQueryWrapper<SystemUserGroupRDo>()
                .eq(SystemUserGroupRDo::getGroupId, groupId);

        return selectList(wrapper);
    }



   default void logicDelByGroupIdAndUserId(Long groupId, List<Long> deletes){

       LambdaUpdateWrapper<SystemUserGroupRDo> set = new LambdaUpdateWrapper<SystemUserGroupRDo>()
               .eq(SystemUserGroupRDo::getGroupId, groupId)
               .in(SystemUserGroupRDo::getUserId, deletes)
               .set(SystemUserGroupRDo::getDeleted, CommonEnum.Deleted.YES.getStatus());
       update(set);


   }


}
