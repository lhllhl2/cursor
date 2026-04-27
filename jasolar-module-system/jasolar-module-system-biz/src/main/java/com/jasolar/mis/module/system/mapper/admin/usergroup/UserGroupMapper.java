package com.jasolar.mis.module.system.mapper.admin.usergroup;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.jasolar.mis.framework.common.enums.CommonEnum;
import com.jasolar.mis.framework.mybatis.core.mapper.BaseMapperX;
import com.jasolar.mis.module.system.controller.admin.usergroup.resp.*;
import com.jasolar.mis.module.system.controller.admin.usergroup.vo.SearchListVo;
import com.jasolar.mis.module.system.controller.admin.usergroup.vo.SearchSimpleListVo;
import com.jasolar.mis.module.system.domain.admin.usergroup.SystemUserGroupDo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * Description:
 * Author : Zhou Hai
 * Date : 23/07/2025 11:10
 * Version : 1.0
 */
@Mapper
public interface UserGroupMapper extends BaseMapperX<SystemUserGroupDo> {


   default boolean hasName(String name){
       LambdaQueryWrapper<SystemUserGroupDo> wrapper = new LambdaQueryWrapper<SystemUserGroupDo>()
               .eq(SystemUserGroupDo::getName, name)
               .eq(SystemUserGroupDo::getDeleted,0);
       Long count = selectCount(wrapper);
       return count > 0;
   }


    List<UserGroupRoleResp> searchRoleByUserGroup(@Param("id") Long id,
                                                  @Param("type") String type,
                                                  @Param("del") boolean del);


    IPage<SearchListResp> getListPage(IPage<SearchListResp> page, @Param("searchVo") SearchListVo searchListVo);

    default void logicDelById(Long id){
        LambdaUpdateWrapper<SystemUserGroupDo> updateWrapper = new LambdaUpdateWrapper<SystemUserGroupDo>()
                .eq(SystemUserGroupDo::getId, id)
                .set(SystemUserGroupDo::getDeleted, CommonEnum.Deleted.YES.getStatus());
        update(updateWrapper);
    }

    IPage<GroupUserResp> searchGroupUser(IPage<GroupUserResp> page,@Param("id") Long id);


    List<SearchSimpleListResp> getSimpleList(SearchSimpleListVo searchSimpleListVo);

    List<GroupUserByRoleResp> searchUserGroupByRole(@Param("roleId") Long roleId,@Param("menuType") String menuType);

    List<GroupUserResp> searchGroupUserList(@Param("id") Long id);


    List<CurrentUserGroupResp> currentUserGroupByUserId(@Param("userId")Long userId,@Param("type") String type);


    List<SearchSimpleListResp> getByReportId(@Param("reportId") String reportId);

   default List<SystemUserGroupDo> getByNames(List<String> userGroupNameSet){
       LambdaQueryWrapper<SystemUserGroupDo> wrapper = new LambdaQueryWrapper<SystemUserGroupDo>()
               .in(SystemUserGroupDo::getName, userGroupNameSet);
       return selectList(wrapper);
   }

}
