package com.jasolar.mis.module.system.service.admin.usergroup;

import com.jasolar.mis.framework.common.pojo.CommonResult;
import com.jasolar.mis.framework.common.pojo.PageResult;
import com.jasolar.mis.framework.common.pojo.PrimaryParam;
import com.jasolar.mis.module.system.controller.admin.usergroup.resp.*;
import com.jasolar.mis.module.system.controller.admin.usergroup.vo.*;

import java.util.List;

/**
 * Description:
 * Author : Zhou Hai
 * Date : 23/07/2025 11:01
 * Version : 1.0
 */
public interface UserGroupService {


    /**
     * 新增
     * @param userGroupAddVo
     */
    void addUserGroup(UserGroupAddVo userGroupAddVo);

    /**
     * 根据用户组id查询角色
     * @param searchRoleByUserGroupVo
     * @return
     */
    List<UserGroupRoleResp> searchRoleByUserGroup(SearchRoleByUserGroupVo searchRoleByUserGroupVo);


    /**
     * 列表查询
     * @param searchListVo
     * @return
     */
    PageResult<SearchListResp> getList(SearchListVo searchListVo);

    /**
     * 删除
     * @param primaryParam
     */
    void del(PrimaryParam primaryParam);

    /**
     * 根据groupId 查询该组的人
     * @param searchPrimaryPage
     * @return
     */
    PageResult<GroupUserResp> searchGroupUser(SearchPrimaryPage searchPrimaryPage);

    /**
     * 用户组成员保存
     * @param groupUserSaveVo
     */
    void groupUserSave(GroupUserSaveVo groupUserSaveVo);

    /**
     * 查询
     * @param searchSimpleListVo
     * @return
     */
    List<SearchSimpleListResp> getSimpleList(SearchSimpleListVo searchSimpleListVo);

    List<GroupUserResp> searchGroupList(PrimaryParam primaryParam);

    /**
     * 根据组织ID查询用户组ID列表
     * @param orgId 组织ID
     * @return 用户组ID列表
     */
    List<Long> getGroupIdsByOrgId(Long orgId);

    /**
     * 根据用户组名称检查是否存在
     * @param name 用户组名称
     * @return true-存在，false-不存在
     */
    boolean existsByName(String name);

    /**
     * 从Excel创建用户组（不检查重复）
     * @param name 用户组名称
     * @param type 用户组类型
     */
    void createUserGroupFromExcel(String name, String type);

    /**
     * 根据用户组名称获取ID
     * @param name 用户组名称
     * @return 用户组ID，不存在返回null
     */
    Long getIdByName(String name);

    /**
     * 获取所有用户组-用户关系（格式：groupId_userId）
     * @return 关系集合
     */
    java.util.Set<String> getAllUserGroupRelations();

    /**
     * 添加用户组-用户关系
     * @param groupId 用户组ID
     * @param userId 用户ID
     */
    void addUserGroupRelation(Long groupId, Long userId);


    /**
     * 根据组织编码获取组织ID
     * @param orgCode 组织编码
     * @return 组织ID，不存在返回null
     */
    Long getOrganizationIdByCode(String orgCode);


    /**
     * 获取所有组织-用户组关系（格式：userGroupId_organizationId）
     * @return 关系集合
     */
    java.util.Set<String> getAllOrgUserGroupRelations();

    /**
     * 添加组织-用户组关系
     * @param userGroupId 用户组ID
     * @param organizationId 组织ID
     */
    void addOrgUserGroupRelation(Long userGroupId, Long organizationId);


    List<UserGroupTreeResp> getUserGroupTree();

    CommonResult saveUserGroup(UserGroupTreeSaveVo userGroupTreeSaveVo);

    CommonResult<Void> copyUserGroupTree(CopyUserGroupTreeVo copyUserGroupTreeVo);

    CommonResult<Void> addUserGroupTree(CopyUserGroupTreeVo addUserGroupTreeVo);
    CommonResult<Void> editUserGroup(EditUserGroupVo editUserGroupVo);
}
