package com.jasolar.mis.module.system.service.admin.role;

import cn.hutool.core.collection.CollectionUtil;
import com.jasolar.mis.framework.common.exception.ServiceException;
import com.jasolar.mis.framework.common.pojo.PageResult;
import com.jasolar.mis.framework.common.pojo.PrimaryParam;
import com.jasolar.mis.module.system.controller.admin.permission.vo.menu.MenuMetaVO;
import com.jasolar.mis.module.system.controller.admin.permission.vo.menu.MenuRespVO;
import com.jasolar.mis.module.system.controller.admin.role.vo.RoleEditVo;
import com.jasolar.mis.module.system.controller.admin.role.vo.RolePageVo;
import com.jasolar.mis.module.system.controller.admin.role.vo.RoleSaveVo;
import com.jasolar.mis.module.system.controller.admin.usergroup.resp.GroupUserByRoleResp;
import com.jasolar.mis.module.system.domain.admin.permission.MenuDO;
import com.jasolar.mis.module.system.domain.admin.role.SystemRoleDo;
import com.jasolar.mis.module.system.domain.admin.role.SystemRoleMenuRDo;
import com.jasolar.mis.module.system.domain.admin.role.SystemUserGroupRoleRDo;
import com.jasolar.mis.module.system.enums.UserGroupEnums;
import com.jasolar.mis.module.system.exceptioncode.RoleErrorCodeConstants;
import com.jasolar.mis.module.system.mapper.admin.role.SystemRoleMapper;
import com.jasolar.mis.module.system.mapper.admin.role.SystemRoleMenuRMapper;
import com.jasolar.mis.module.system.mapper.admin.role.SystemUserGroupRoleRMapper;
import com.jasolar.mis.module.system.mapper.admin.usergroup.UserGroupMapper;
import com.jasolar.mis.module.system.service.permission.MenuService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Description:
 * Author : Zhou Hai
 * Date : 24/07/2025 17:36
 * Version : 1.0
 */
@Service
public class SystemRoleServiceImpl implements SystemRoleService{

    @Autowired
    private SystemRoleMapper systemRoleMapper;

    @Autowired
    private SystemRoleMenuRMapper systemRoleMenuRMapper;

    @Autowired
    private SystemUserGroupRoleRMapper systemUserGroupRoleRMapper;

    @Autowired
    private UserGroupMapper userGroupMapper;

    @Autowired
    private MenuService menuService;


    @Transactional
    @Override
    public void saveRole(RoleSaveVo roleSaveVo) {
        boolean repName = systemRoleMapper.replicateName(roleSaveVo.getName());
        if(repName){
            throw new ServiceException(RoleErrorCodeConstants.ROLE_NAME_REPLICATION);
        }
        boolean repCode = systemRoleMapper.replicateCode(roleSaveVo.getCode());
        if(repCode){
            throw new ServiceException(RoleErrorCodeConstants.ROLE_CODE_REPLICATION);
        }
        LocalDateTime now = LocalDateTime.now();
        SystemRoleDo systemRoleDo = SystemRoleDo.builder()
                .name(roleSaveVo.getName())
                .code(roleSaveVo.getCode())
                .status(roleSaveVo.getStatus())
                .remark(roleSaveVo.getRemark())
                .build();
        systemRoleMapper.insert(systemRoleDo);

        if(CollectionUtil.isNotEmpty(roleSaveVo.getMenuIds())){
            List<SystemRoleMenuRDo> menuRDos = roleSaveVo.getMenuIds().stream()
                    .map(x -> {
                        SystemRoleMenuRDo roleMenuRDo = SystemRoleMenuRDo.builder()
                                .roleId(systemRoleDo.getId())
                                .menuId(x)
                                .build();
                        return roleMenuRDo;
                    }).toList();
            systemRoleMenuRMapper.insertBatch(menuRDos);
        }
        if(CollectionUtil.isNotEmpty(roleSaveVo.getGroupIds())){
            List<SystemUserGroupRoleRDo> groupRoleRDos = roleSaveVo.getGroupIds().stream()
                    .map(x -> {
                        SystemUserGroupRoleRDo groupRoleRDo = SystemUserGroupRoleRDo.builder()
                                .groupId(x)
                                .roleId(systemRoleDo.getId())
                                .build();
                        return groupRoleRDo;
                    }).toList();
            systemUserGroupRoleRMapper.insertBatch(groupRoleRDos);
        }
    }


    @Override
    public PageResult<SystemRoleDo> rolePage(RolePageVo rolePageVo) {

        PageResult<SystemRoleDo> pageResult = systemRoleMapper.rolePage(rolePageVo);

        return pageResult;
    }


    @Transactional
    @Override
    public void delete(PrimaryParam primaryParam) {
        SystemRoleDo systemRoleDo = systemRoleMapper.selectById(primaryParam.getId());
        if(Objects.isNull(systemRoleDo)){
            throw new ServiceException(RoleErrorCodeConstants.ROLE_NOT_EXIST);
        }
        systemRoleMapper.deleteById(systemRoleDo);
        systemUserGroupRoleRMapper.logicDelUserGroupRole(primaryParam.getId());
        systemRoleMenuRMapper.logicDelMenuRole(primaryParam.getId());
    }


    @Transactional
    @Override
    public void editRole(RoleEditVo roleEditVo) {
        boolean repName = systemRoleMapper.replicateNameNotSelf(roleEditVo.getName(),roleEditVo.getId());
        if(repName){
           throw new ServiceException(RoleErrorCodeConstants.ROLE_NAME_REPLICATION);
        }
        boolean repCode = systemRoleMapper.replicateCodeNotSelf(roleEditVo.getCode(),roleEditVo.getId());
        if(repCode){
            throw new ServiceException(RoleErrorCodeConstants.ROLE_CODE_REPLICATION);
        }
        SystemRoleDo systemRoleDo = SystemRoleDo.builder()
                .id(roleEditVo.getId())
                .name(roleEditVo.getName())
                .code(roleEditVo.getCode())
                .status(roleEditVo.getStatus())
                .remark(roleEditVo.getRemark())
                .build();
        systemRoleMapper.updateById(systemRoleDo);

        // Menus

       List<SystemRoleMenuRDo> roleMenuRDos = systemRoleMenuRMapper.selectByRoleId(roleEditVo.getId());
       if(CollectionUtil.isEmpty(roleMenuRDos)){
           if(CollectionUtil.isNotEmpty(roleEditVo.getMenuIds())){
               List<SystemRoleMenuRDo> roleMenuRDoList = roleEditVo.getMenuIds().stream()
                       .map(x -> {
                           SystemRoleMenuRDo roleMenuRDo = SystemRoleMenuRDo.builder()
                                   .roleId(systemRoleDo.getId())
                                   .menuId(x)
                                   .build();
                           return roleMenuRDo;
                       }).toList();
               systemRoleMenuRMapper.insertBatch(roleMenuRDoList);
           }
       }else {
           List<Long> oldMenus = roleMenuRDos.stream().map(SystemRoleMenuRDo::getMenuId).toList();
           List<Long> newMenuIds = roleEditVo.getMenuIds();
           List<Long> deleteMenuIds = CollectionUtil.subtractToList(oldMenus, newMenuIds);
           if(CollectionUtil.isNotEmpty(deleteMenuIds)){
               systemRoleMenuRMapper.deleteLogic(roleEditVo.getId(),deleteMenuIds);
           }
           List<Long> addMenuIds = CollectionUtil.subtractToList(newMenuIds, oldMenus);
           if(CollectionUtil.isNotEmpty(addMenuIds)){
               List<SystemRoleMenuRDo> addMenuRs = addMenuIds.stream()
                       .map(x -> {
                           SystemRoleMenuRDo roleMenuRDo = SystemRoleMenuRDo.builder()
                                   .roleId(systemRoleDo.getId())
                                   .menuId(x)
                                   .build();
                           return roleMenuRDo;
                       }).toList();

               systemRoleMenuRMapper.insertBatch(addMenuRs);
           }
       }




        // GroupUsers
        List<SystemUserGroupRoleRDo> userGroupRoleRDos = systemUserGroupRoleRMapper.selectByRoleId(roleEditVo.getId());
        if(CollectionUtil.isEmpty(userGroupRoleRDos)){
            if(CollectionUtil.isNotEmpty(roleEditVo.getGroupIds())){
                List<SystemUserGroupRoleRDo> userGroupRoleRDoList = roleEditVo.getGroupIds().stream()
                        .map(x -> {
                            SystemUserGroupRoleRDo userGroupRoleRDo = SystemUserGroupRoleRDo.builder()
                                    .roleId(systemRoleDo.getId())
                                    .groupId(x)
                                    .build();
                            return userGroupRoleRDo;
                        }).toList();
                systemUserGroupRoleRMapper.insertBatch(userGroupRoleRDoList);
            }
            return;
        }

        List<Long> oldGroupIds = userGroupRoleRDos.stream().map(SystemUserGroupRoleRDo::getGroupId).toList();
        List<Long> newGroupIds = roleEditVo.getGroupIds();
        List<Long> delGroupIds = CollectionUtil.subtractToList(oldGroupIds, newGroupIds);
        if(CollectionUtil.isNotEmpty(delGroupIds)){
            systemUserGroupRoleRMapper.deleteLogic(roleEditVo.getId(),delGroupIds);
        }
        List<Long> addGroupIds = CollectionUtil.subtractToList(newGroupIds, oldGroupIds);
        if(CollectionUtil.isNotEmpty(addGroupIds)){
            List<SystemUserGroupRoleRDo> addGroupRList = addGroupIds.stream()
                    .map(x -> {
                        SystemUserGroupRoleRDo userGroupRoleRDo = SystemUserGroupRoleRDo.builder()
                                .roleId(systemRoleDo.getId())
                                .groupId(x)
                                .build();
                        return userGroupRoleRDo;
                    }).toList();
            systemUserGroupRoleRMapper.insertBatch(addGroupRList);
        }

    }

    @Override
    public List<GroupUserByRoleResp> searchUserGroupByRole(PrimaryParam primaryParam) {
        return userGroupMapper.searchUserGroupByRole(primaryParam.getId(), UserGroupEnums.Type.MENU.getCode());
    }

    @Override
    public List<MenuRespVO> getMenuTreeByRoleId(Long roleId) {
        // 1. 根据角色ID查询角色信息（验证角色是否存在）
        SystemRoleDo role = systemRoleMapper.selectById(roleId);
        if (Objects.isNull(role)) {
            throw new ServiceException(RoleErrorCodeConstants.ROLE_NOT_EXIST);
        }

        // 2. 根据角色ID查询菜单ID列表
        List<Long> menuIds = systemRoleMenuRMapper.selectMenuIdsByRoleId(roleId);
        if (menuIds.isEmpty()) {
            return Collections.emptyList();
        }

        // 3. 根据菜单ID列表查询菜单数据
        List<MenuDO> menuList = menuService.getMenuList(menuIds);
        if (menuList.isEmpty()) {
            return Collections.emptyList();
        }

        // 4. 转换为MenuRespVO列表
        List<MenuRespVO> menuRespVOList = menuList.stream()
                .map(this::convertToMenuRespVO)
                .collect(Collectors.toList());

        // 5. 构建菜单树
        return buildMenuTree(menuRespVOList);
    }

    /**
     * 将MenuDO转换为MenuRespVO
     */
    private MenuRespVO convertToMenuRespVO(MenuDO menu) {
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
        menuRespVO.setCreator(menu.getCreator());
        menuRespVO.setUpdater(menu.getUpdater());

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
        meta.setIframeSrc(menu.getIframeSrc());
        meta.setKeepAlive(menu.getKeepAlive() != null && menu.getKeepAlive() == 1);
        meta.setLink(menu.getLink());
        meta.setMaxNumOfOpenTab(menu.getMaxNumOfOpenTab());
        meta.setNoBasicLayout(menu.getNoBasicLayout() != null && menu.getNoBasicLayout() == 1);
        meta.setOpenInNewWindow(menu.getOpenInNewWindow() != null && menu.getOpenInNewWindow() == 1);
        meta.setMenuOrder(menu.getMenuOrder());
        meta.setQuery(menu.getQueryParams());
        meta.setTitle(menu.getTitle());
        meta.setIcon(menu.getIcon());

        menuRespVO.setMeta(meta);

        return menuRespVO;
    }

    /**
     * 构建菜单树形结构
     */
    private List<MenuRespVO> buildMenuTree(List<MenuRespVO> menuList) {
        if (menuList.isEmpty()) {
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
}
