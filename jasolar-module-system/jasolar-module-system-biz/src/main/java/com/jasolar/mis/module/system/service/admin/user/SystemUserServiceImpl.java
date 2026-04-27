package com.jasolar.mis.module.system.service.admin.user;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jasolar.mis.framework.common.exception.ServiceException;
import com.jasolar.mis.framework.common.exception.enums.UserErrorConstants;
import com.jasolar.mis.framework.common.pojo.PageResult;
import com.jasolar.mis.framework.common.security.LoginUser;
import com.jasolar.mis.framework.redis.util.RedisUtils;
import com.jasolar.mis.framework.web.core.util.WebFrameworkUtils;
import com.jasolar.mis.module.system.constant.AuthConstant;
import com.jasolar.mis.module.system.controller.admin.dict.vo.DictEditVo;
import com.jasolar.mis.module.system.controller.admin.dict.vo.DictInfoByCodeVo;
import com.jasolar.mis.module.system.controller.admin.permission.vo.menu.MenuMetaVO;
import com.jasolar.mis.module.system.controller.admin.permission.vo.menu.MenuRespVO;
import com.jasolar.mis.module.system.controller.admin.role.resp.RoleSimpleResp;
import com.jasolar.mis.module.system.controller.admin.user.resp.CurrentUserInfoResp;
import com.jasolar.mis.module.system.controller.admin.user.vo.user.*;
import com.jasolar.mis.module.system.controller.admin.usergroup.resp.CurrentUserGroupResp;
import com.jasolar.mis.module.system.controller.admin.usergroup.resp.GroupUserResp;
import com.jasolar.mis.module.system.domain.admin.permission.MenuDO;
import com.jasolar.mis.module.system.domain.admin.user.SystemUserDo;
import com.jasolar.mis.module.system.domain.admin.usergroup.SystemUserGroupDo;
import com.jasolar.mis.module.system.domain.admin.usergroup.SystemUserGroupRDo;
import com.jasolar.mis.module.system.enums.UserEnums;
import com.jasolar.mis.module.system.enums.UserGroupEnums;
import com.jasolar.mis.module.system.mapper.admin.permission.MenuMapper;
import com.jasolar.mis.module.system.mapper.admin.role.SystemRoleMapper;
import com.jasolar.mis.module.system.mapper.admin.user.SystemUserMapper;
import com.jasolar.mis.module.system.mapper.admin.usergroup.SystemUserGroupRMapper;
import com.jasolar.mis.module.system.mapper.admin.usergroup.UserGroupMapper;
import com.jasolar.mis.module.system.service.admin.dict.SystemDictService;
import com.jasolar.mis.module.system.service.permission.MenuService;
import com.jasolar.mis.module.system.util.BCryptUtil;
import com.jasolar.mis.module.system.util.IPageToPageResultUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Description:
 * Author : Zhou Hai
 * Date : 21/07/2025 15:35
 * Version : 1.0
 */
@Slf4j
@Service
public class SystemUserServiceImpl implements SystemUserService{


    @Autowired
    private SystemUserMapper systemUserMapper;

    @Autowired
    private UserGroupMapper userGroupMapper;

    @Autowired
    private SystemRoleMapper systemRoleMapper;

    @Autowired
    private MenuMapper menuMapper;

    @Autowired
    private MenuService  menuService;
    @Autowired
    private SystemUserGroupRMapper systemUserGroupRMapper;


//    @Autowired
//    private ThreadPoolTaskExecutor taskExecutor;

    @Autowired
    private SystemDictService systemDictService;
    // 批量重置用户密码
    @Override
    @Transactional
    public void resetPwd(ResetPwdReqVO reqVO) {
        // 校验当前用户是否是管理员 即 判定当前用户是否在管理员用户组 中
        Long loginUserId = WebFrameworkUtils.getLoginUserId();
        List<SystemUserGroupRDo> systemUserGroupRDos = systemUserGroupRMapper.selectList(
                new LambdaQueryWrapper<SystemUserGroupRDo>()
                        .eq(SystemUserGroupRDo::getUserId, loginUserId)
                        .eq(SystemUserGroupRDo::getType, 1)
        );
        List<SystemUserGroupDo> systemUserGroupDos = userGroupMapper.selectList(
                new LambdaQueryWrapper<SystemUserGroupDo>()
                        .in(SystemUserGroupDo::getId, systemUserGroupRDos.stream().distinct().map(item -> item.getGroupId()).toList())
                        .eq(SystemUserGroupDo::getDeleted, 0)
        );
        if (systemUserGroupDos.isEmpty()) {
            throw new ServiceException("500","用户无权限");
        }
        if (!systemUserGroupDos.stream().anyMatch(item -> item.getName().equals("管理员用户组"))) {
            throw new ServiceException("500","用户无权限");
        }


        String defaultPassword = "jasolar123";
        String encodedPassword = BCryptUtil.encode(defaultPassword);
        List<SystemUserDo> systemUserDOS = systemUserMapper.selectBatchIds(reqVO.getUserIds());
        for (int i = 0; i < systemUserDOS.size(); i++) {
            systemUserDOS.get(i).setPwd(encodedPassword);
            systemUserDOS.get(i).setPwdChanged("0");
        }

        systemUserMapper.updateBatch(systemUserDOS);
    }






    @Override
    public SystemUserDo getByUserName(String username) {
        return systemUserMapper.getByUserName(username);
    }


    @Override
    public PageResult<UserPageVo> userPage(UserPageReqVO userPageReqVO) {
        IPage<UserPageVo> page = new Page<>(userPageReqVO.getPageNo(),userPageReqVO.getPageSize());
        IPage<UserPageVo> userPage = systemUserMapper.userPage(page, userPageReqVO);

        PageResult<UserPageVo> transfer = IPageToPageResultUtils.transfer(userPage);
        List<UserPageVo> rows = transfer.getList();
        if(CollectionUtil.isNotEmpty(rows)){
            List<String> types = List.of("gender");
            DictInfoByCodeVo info = DictInfoByCodeVo.builder()
                    .codes(types)
                    .build();
            Map<String, DictEditVo> dictMap = systemDictService.getByCode(info);

            // 批量查询所有用户的用户组ID
            List<Long> userIds = rows.stream().map(UserPageVo::getId).collect(Collectors.toList());
            Map<Long, List<Long>> userGroupIdsMap = new HashMap<>();
            Map<Long, List<UserGroupInfoVo>> userGroupInfos = new HashMap<>();
            if (!userIds.isEmpty()) {
                List<UserGroupIdVo> userGroupIdsList = systemUserMapper.getBatchUserGroupIds(userIds);
                
                // 将查询结果按userId分组
                for (UserGroupIdVo vo : userGroupIdsList) {
                    userGroupIdsMap.computeIfAbsent(vo.getUserId(), k -> new ArrayList<>()).add(vo.getGroupId());
                    userGroupInfos.computeIfAbsent(vo.getUserId(), k -> new ArrayList<>()).add(UserGroupInfoVo.builder()
                            .groupId(vo.getGroupId())
                            .groupName(vo.getGroupName())
                            .build());
                }
            }

            for (UserPageVo row : rows) {
                String genderDes = systemDictService.getFieldLabel(dictMap, "gender", row.getGender());
                row.setGenderDes(genderDes);
                // 设置用户的用户组ID列表
                List<Long> groupIds = userGroupIdsMap.getOrDefault(row.getId(), new ArrayList<>());
                row.setGroupIds(groupIds);
                List<UserGroupInfoVo> userGroupInfoVos = userGroupInfos.get(row.getId());
                row.setUserGroupInfos(userGroupInfoVos);
            }
        }
        return IPageToPageResultUtils.transfer(userPage);
    }


    @Override
    public List<GroupUserResp> userForGroup(UserForGroupVo forGroupVo) {
        return systemUserMapper.userForGroup(forGroupVo);
    }



    @Override
    public CurrentUserInfoResp currentUserInfo() {
        LoginUser loginUser = WebFrameworkUtils.getLoginUser();
        return getCurrentUserInfoResp(loginUser);
    }

    private CurrentUserInfoResp getCurrentUserInfoResp(LoginUser loginUser) {
        CurrentUserInfoResp currentUserInfo = CurrentUserInfoResp.builder()
                .id(loginUser.getId())
                .userName(loginUser.getNo())
                .displayName(loginUser.getName())
                .build();

        SystemUserDo systemUserDo = RedisUtils.get(AuthConstant.USER_INFO_KEY_PRE + loginUser.getId(), SystemUserDo.class);
        if(!Objects.isNull(systemUserDo)){
            currentUserInfo.setEmail(systemUserDo.getEmail());
            currentUserInfo.setPost(systemUserDo.getPost());
        }

        // 用户组
        List<CurrentUserGroupResp> currentUserGroup = userGroupMapper.currentUserGroupByUserId(loginUser.getId(), UserGroupEnums.Type.MENU.getCode());
        if(CollectionUtil.isEmpty(currentUserGroup)){
            return currentUserInfo;
        }
        currentUserInfo.setUserGroupList(currentUserGroup);
        // 角色
        List<Long> userGroupIds = currentUserGroup.stream().map(CurrentUserGroupResp::getId).toList();
        List<RoleSimpleResp> roleSimpleRespList = systemRoleMapper.getByUserGroupIds(userGroupIds);
        if(CollectionUtil.isEmpty(roleSimpleRespList)){
            return currentUserInfo;
        }
        currentUserInfo.setRoleSimpleRespList(roleSimpleRespList);
        // 菜单
        List<Long> roleIds = roleSimpleRespList.stream().map(RoleSimpleResp::getId).toList();

        List<MenuDO>  menuRespList =  menuMapper.getSimpleByRoleIds(roleIds);
        if(CollectionUtil.isEmpty(menuRespList)){
            return currentUserInfo;
        }
        List<MenuRespVO> menuList = menuRespList.stream()
                .filter(x -> Objects.equals(x.getType(),"menu") || Objects.equals(x.getType(),"catalog"))
                .map(this::convertToMenuRespVO)
                .collect(Collectors.toList());

        // 3. 构建树形结构（内部已包含排序）
        List<MenuRespVO> treeList = buildMenuTree(menuList);
        currentUserInfo.setMenuList(treeList);

        // 4.button
        List<MenuRespVO> buttonList = menuRespList.stream()
                .filter(x -> Objects.equals(x.getType(),"button"))
                .map(this::convertToMenuRespVO)
                .toList();
        currentUserInfo.setButtonList(buttonList);


//        taskExecutor.execute(() -> {
//            synchronized (this){
//                frReportService.writeReportSubDate(loginUser.getId());
//            }
//
//        });
        return currentUserInfo;
    }

    private List<MenuRespVO> buildMenuTree(List<MenuRespVO> menuList) {
        if (CollUtil.isEmpty(menuList)) {
            return Collections.emptyList();
        }

        // 使用LinkedHashMap保持顺序（数据已从数据库按menuOrder排序）
        Map<Long, MenuRespVO> treeNodeMap = new LinkedHashMap<>();
        menuList.forEach(menu -> treeNodeMap.put(menu.getId(), menu));

        // 处理父子关系
        treeNodeMap.values().stream()
                .filter(node -> !MenuDO.ID_ROOT.equals(node.getPid()))
                .forEach(childNode -> {
                    Long parentId = childNode.getPid();
                    MenuRespVO parentNode = treeNodeMap.get(parentId);
                    if (parentNode != null) {
                        if (parentNode.getChildren() == null) {
                            parentNode.setChildren(new ArrayList<>());
                        }
                        parentNode.getChildren().add(childNode);
                    }
                });

        // 返回根节点（pid为0的节点）
        return treeNodeMap.values().stream()
                .filter(node -> MenuDO.ID_ROOT.equals(node.getPid()))
                .collect(Collectors.toList());
    }




    /**
     * 将MenuDO转换为MenuRespVO
     */
    private MenuRespVO convertToMenuRespVO(MenuDO menu) {
        // 创建MenuRespVO
        MenuRespVO menuRespVO = new MenuRespVO();

        // 设置核心字段
        menuRespVO.setId(menu.getId());
        menuRespVO.setName(menu.getName());
        menuRespVO.setPath(menu.getPath());
        menuRespVO.setType(menu.getType());
        menuRespVO.setStatus(menu.getStatus());
        menuRespVO.setPid(menu.getPid());
        menuRespVO.setAuthCode(menu.getAuthCode());
        menuRespVO.setRedirect(menu.getRedirect());
        menuRespVO.setComponent(menu.getComponent());

        // 设置审计字段
        menuRespVO.setCreateTime(menu.getCreateTime());
        menuRespVO.setUpdateTime(menu.getUpdateTime());
//        menuRespVO.setCreator(menu.getCreator());
//        menuRespVO.setUpdater(menu.getUpdater());

        // 创建并设置MenuMetaVO
        MenuMetaVO meta = new MenuMetaVO();
        meta.setActiveIcon(menu.getActiveIcon());
        meta.setActivePath(menu.getActivePath());
        meta.setAffixTab(menu.getAffixTab() != null && menu.getAffixTab() == 1);
        meta.setAffixTabOrder(menu.getAffixTabOrder());
        meta.setBadge(menu.getBadge());
        meta.setBadgeType(menu.getBadgeType());
        meta.setBadgeVariants(menu.getBadgeVariants());
        meta.setHideChildrenInMenu(menu.getHideChildrenInMenu() != null && menu.getHideChildrenInMenu() == 1);
        meta.setHideInBreadcrumb(menu.getHideInBreadcrumb() != null && menu.getHideInBreadcrumb() == 1);
        meta.setHideInMenu(menu.getHideInMenu() != null && menu.getHideInMenu() == 1);
        meta.setHideInTab(menu.getHideInTab() != null && menu.getHideInTab() == 1);
        meta.setIcon(menu.getIcon());
        meta.setIframeSrc(menu.getIframeSrc());
        meta.setKeepAlive(menu.getKeepAlive() != null && menu.getKeepAlive() == 1);
        meta.setLink(menu.getLink());
        meta.setMaxNumOfOpenTab(menu.getMaxNumOfOpenTab());
        meta.setNoBasicLayout(menu.getNoBasicLayout() != null && menu.getNoBasicLayout() == 1);
        meta.setOpenInNewWindow(menu.getOpenInNewWindow() != null && menu.getOpenInNewWindow() == 1);
        meta.setMenuOrder(menu.getMenuOrder());
        meta.setQuery(menu.getQueryParams()); // 直接映射queryParams到query
        meta.setTitle(menu.getTitle());

        menuRespVO.setMeta(meta);

        return menuRespVO;
    }

    @Override
    public void updateUserName(UserUpdateUserNameReqVO reqVO) {
        // 1. 根据ID查询用户是否存在
        SystemUserDo user = systemUserMapper.selectById(reqVO.getId());
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        
        // 2. 检查新用户名是否已存在
        SystemUserDo existingUser = systemUserMapper.getByUserName(reqVO.getUserName());
        if (existingUser != null && !existingUser.getId().equals(reqVO.getId())) {
            throw new RuntimeException("用户名已存在");
        }
        
        // 3. 更新用户名
        user.setUserName(reqVO.getUserName());
        systemUserMapper.updateById(user);
    }


    @Override
    public void updatePwd(UserUpdatePwdVo userUpdatePwdVo) {
        String pwd = userUpdatePwdVo.getPwd();
        String newPwd = userUpdatePwdVo.getNewPwd();
        String confirm = userUpdatePwdVo.getConfirmPwd();

        if(!Objects.equals(newPwd,confirm)){
            throw new ServiceException(UserErrorConstants.USER_PWD_TWICE_NOT_SAME);
        }

        if(Objects.equals(pwd,newPwd)){
            throw new ServiceException(UserErrorConstants.USER_TWO_MUST_NOT_EQ);
        }

        SystemUserDo systemUserDo = systemUserMapper.getByUserName(WebFrameworkUtils.getLoginUserNo());
        if(Objects.isNull(systemUserDo)){
            throw new ServiceException(UserErrorConstants.USER_NO_EXISTS);
        }
        if(Objects.equals(systemUserDo.getStatus(),UserEnums.Status.BAN.getCode())){
            throw new ServiceException(UserErrorConstants.USER_HAS_BAN);
        }
        boolean matches = BCryptUtil.matches(pwd, systemUserDo.getPwd());
        if(!matches){
            throw new ServiceException(UserErrorConstants.USER_PWD_ERR);
        }
        String encodePwd = BCryptUtil.encode(newPwd);
        systemUserDo.setPwd(encodePwd);
        systemUserDo.setPwdChanged(UserEnums.PwdChanged.YES.getCode());

        systemUserMapper.updateById(systemUserDo);

        // 删除redis中的key

        String userInfoKey =  AuthConstant.USER_INFO_KEY_PRE + systemUserDo.getId();
        RedisUtils.del(userInfoKey);
    }

    @Override
    public Set<String> getAllActiveUserInfo() {
        List<String> userInfoList = systemUserMapper.getAllActiveUserInfo();
        return new HashSet<>(userInfoList);
    }

    @Override
    public void createUserFromExcel(String userNo, String userName) {
        // 检查用户是否已存在
        SystemUserDo existingUser = systemUserMapper.getByUserName(userNo);
        if (existingUser != null) {
            throw new RuntimeException("用户已存在: " + userNo);
        }
        
        // 设置默认密码为 "jasolar123"
        String defaultPassword = "jasolar123";
        String encodedPassword = BCryptUtil.encode(defaultPassword);
        
        // 创建新用户
        SystemUserDo newUser = SystemUserDo.builder()
                .userName(userNo)
                .displayName(userName)
                .pwd(encodedPassword) // 设置加密后的默认密码
                .status("1") // 启用状态
                .pwdChanged("0") // 未修改密码
                .build();
        
        // 插入数据库
        systemUserMapper.insert(newUser);
    }

    @Override
    public List<SystemUserDo> listActiveUsersForExport() {
        LambdaQueryWrapper<SystemUserDo> wrapper =
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<SystemUserDo>()
                        .eq(SystemUserDo::getDeleted, 0)
                        .orderByAsc(SystemUserDo::getUserName);
        return systemUserMapper.selectList(wrapper);
    }
}
