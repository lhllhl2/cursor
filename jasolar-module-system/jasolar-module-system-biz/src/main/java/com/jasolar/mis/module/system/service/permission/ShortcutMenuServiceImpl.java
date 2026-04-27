package com.jasolar.mis.module.system.service.permission;

import cn.hutool.core.collection.CollUtil;
import com.jasolar.mis.framework.common.security.LoginServletUtils;
import com.jasolar.mis.framework.common.security.LoginUser;
import com.jasolar.mis.framework.common.util.object.BeanUtils;
import com.jasolar.mis.framework.mybatis.core.dataobject.BaseIdentityDO;
import com.jasolar.mis.module.system.controller.admin.permission.vo.ShortcutMenuRespVO;
import com.jasolar.mis.module.system.controller.admin.permission.vo.ShortcutMenuSaveReqVO;
import com.jasolar.mis.module.system.domain.admin.permission.RoleMenuDO;
import com.jasolar.mis.module.system.domain.admin.permission.ShortcutMenuDO;
import com.jasolar.mis.module.system.domain.admin.permission.UserRoleDO;
import com.jasolar.mis.module.system.mapper.admin.permission.RoleMenuMapper;
import com.jasolar.mis.module.system.mapper.admin.permission.ShortcutMenuMapper;
import com.jasolar.mis.module.system.mapper.admin.permission.UserRoleMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;


/**
 * 用户快捷菜单 Service 实现类
 *
 * @author 管理员
 */
@Service
@Validated
public class ShortcutMenuServiceImpl implements ShortcutMenuService {

    @Resource
    private ShortcutMenuMapper shortcutMenuMapper;
    @Resource
    private RoleMenuMapper roleMenuMapper;
    @Resource
    private UserRoleMapper userRoleMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean saveShortcutMenu(List<ShortcutMenuSaveReqVO> createReqVOs) {
        LoginUser loginUser = LoginServletUtils.getLoginUser();
        //查询当前用户已设置的菜单
        List<ShortcutMenuDO> existingMenus = shortcutMenuMapper.selectList(loginUser.getId(),null);
        Map<Long, ShortcutMenuDO> existingMenuMap = existingMenus.stream()
                .collect(Collectors.toMap(ShortcutMenuDO::getMenuId, Function.identity(), (a, b) -> a));


        // 删除不在当前参数中的菜单
        List<Long> menuIds = createReqVOs.stream().map(ShortcutMenuSaveReqVO::getMenuId).toList();
        List<Long> deleteIds = existingMenus.stream()
                .filter(existingMenu -> !menuIds.contains(existingMenu.getMenuId())).map(BaseIdentityDO::getId).toList();
        if (!deleteIds.isEmpty()){
            shortcutMenuMapper.deleteBatchIds(deleteIds);
        }
        // 添加或更新菜单
        List<ShortcutMenuDO> addMenus = new ArrayList<>();
        List<ShortcutMenuDO> updateMenus = new ArrayList<>();
        for (ShortcutMenuSaveReqVO reqVO : createReqVOs) {
            if (existingMenuMap.containsKey(reqVO.getMenuId())) {
                // 更新已存在的菜单
                ShortcutMenuDO existingMenu = existingMenuMap.get(reqVO.getMenuId());
                existingMenu.setSort(reqVO.getSort());
                existingMenu.setIsPinned(reqVO.getIsPinned());
                updateMenus.add(existingMenu);
            } else {
                // 添加新菜单
                ShortcutMenuDO newMenu = BeanUtils.toBean(reqVO, ShortcutMenuDO.class);
                newMenu.setUserId(loginUser.getId());
                newMenu.setUserNo(loginUser.getNo());
                addMenus.add(newMenu);
            }
        }
        if (!addMenus.isEmpty()){
            shortcutMenuMapper.insertBatch(addMenus);
        }
        if (!updateMenus.isEmpty()){
            shortcutMenuMapper.updateBatch(updateMenus);
        }
        return Boolean.TRUE;
    }


    @Override
    public List<ShortcutMenuRespVO> listShortcutMenu() {
        //获取用户角色
        List<UserRoleDO> userRoleDOS = userRoleMapper.selectListByUserId(LoginServletUtils.getLoginUserId());
        if (CollUtil.isEmpty(userRoleDOS)){
            return Collections.emptyList();
        }
        //获取快捷菜单
        List<ShortcutMenuDO> shortcutMenuDOS = shortcutMenuMapper.selectList(LoginServletUtils.getLoginUser().getId(), null);
        if (CollUtil.isNotEmpty(shortcutMenuDOS)){

            List<ShortcutMenuRespVO> results = BeanUtils.toBean(shortcutMenuDOS, ShortcutMenuRespVO.class);
            //如果是管理员不用过滤
            List<Long> roleIds = userRoleDOS.stream().map(UserRoleDO::getRoleId).toList();
            // 暂时跳过超级管理员检查，因为RoleService不存在
            // TODO: 如果需要超级管理员检查，需要实现相应的逻辑

            //拿取当前用户所拥有的权限菜单菜单列表
            List<RoleMenuDO> roleMenuDOS = roleMenuMapper.selectListByRoleId(roleIds);
            if (CollUtil.isEmpty(roleMenuDOS)){
                return Collections.emptyList();
            }
            //过滤调没有权限的菜单
            List<Long> menuIds = roleMenuDOS.stream().map(RoleMenuDO::getMenuId).toList();
            return results.stream().filter(result -> menuIds.contains(result.getMenuId())).toList();
        }
        return Collections.emptyList();
    }

}