package com.jasolar.mis.module.system.service.admin.user;

import com.jasolar.mis.framework.common.pojo.PageResult;
import com.jasolar.mis.module.system.controller.admin.user.resp.CurrentUserInfoResp;
import com.jasolar.mis.module.system.controller.admin.user.vo.user.*;
import com.jasolar.mis.module.system.controller.admin.usergroup.resp.GroupUserResp;
import com.jasolar.mis.module.system.domain.admin.user.SystemUserDo;
import jakarta.validation.Valid;

import java.util.List;

/**
 * Description:
 * Author : Zhou Hai
 * Date : 21/07/2025 15:35
 * Version : 1.0
 */
public interface SystemUserService {

    /**
     * 根据用户查询用户
     * @param username
     * @return
     */
    SystemUserDo getByUserName(String username);


    /**
     * 查询列表
     * @param userPageReqVO
     * @return
     */
    PageResult<UserPageVo> userPage(UserPageReqVO userPageReqVO);


    /**
     * 根据搜索查询用户
     * @param forGroupVo
     * @return
     */
    List<GroupUserResp> userForGroup(UserForGroupVo forGroupVo);

    /**
     * 获取当前登录人的信息
     * @return
     */
    CurrentUserInfoResp currentUserInfo();

    /**
     * 根据用户ID修改用户名
     * @param reqVO 修改用户名的请求参数
     */
    void updateUserName(UserUpdateUserNameReqVO reqVO);


    /**
     * 修改密码
     * @param userUpdatePwdVo
     */
    void updatePwd(UserUpdatePwdVo userUpdatePwdVo);

    /**
     * 获取所有活跃用户信息（格式：工号_姓名）
     * @return 用户信息Set
     */
    java.util.Set<String> getAllActiveUserInfo();

    /**
     * 从Excel创建用户
     * @param userNo 工号
     * @param userName 姓名
     */
    void createUserFromExcel(String userNo, String userName);

    /**
     * 获取未删除的用户列表（用于导出），按工号升序
     */
    List<SystemUserDo> listActiveUsersForExport();

    void resetPwd(@Valid ResetPwdReqVO reqVO);
}
