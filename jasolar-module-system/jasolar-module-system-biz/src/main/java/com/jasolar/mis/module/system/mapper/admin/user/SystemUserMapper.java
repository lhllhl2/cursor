package com.jasolar.mis.module.system.mapper.admin.user;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.jasolar.mis.framework.mybatis.core.mapper.BaseMapperX;
import com.jasolar.mis.module.system.controller.admin.user.vo.user.UserForGroupVo;
import com.jasolar.mis.module.system.controller.admin.user.vo.user.UserGroupIdVo;
import com.jasolar.mis.module.system.controller.admin.user.vo.user.UserPageReqVO;
import com.jasolar.mis.module.system.controller.admin.user.vo.user.UserPageVo;
import com.jasolar.mis.module.system.controller.admin.usergroup.resp.GroupUserResp;
import com.jasolar.mis.module.system.domain.admin.user.SystemUserDo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * Description:
 * Author : Zhou Hai
 * Date : 21/07/2025 16:34
 * Version : 1.0
 */
@Mapper
public interface SystemUserMapper extends BaseMapperX<SystemUserDo> {


    default SystemUserDo getByUserName(String username){
        LambdaQueryWrapper<SystemUserDo> wrapperX = new LambdaQueryWrapper<SystemUserDo>()
                .eq(SystemUserDo::getUserName, username)
                .eq(SystemUserDo::getDeleted, 0);
        return selectOne(wrapperX);


    }


    IPage<UserPageVo> userPage(IPage<UserPageVo> page,@Param("search") UserPageReqVO userPageReqVO);

    List<GroupUserResp> userForGroup(UserForGroupVo forGroupVo);

    /**
     * 获取所有活跃用户信息（格式：工号_姓名）
     * @return 用户信息列表
     */
    List<String> getAllActiveUserInfo();
    
    /**
     * 批量查询用户的用户组ID
     * @param userIds 用户ID列表
     * @return 用户组ID列表
     */
    List<UserGroupIdVo> getBatchUserGroupIds(@Param("userIds") List<Long> userIds);

}
