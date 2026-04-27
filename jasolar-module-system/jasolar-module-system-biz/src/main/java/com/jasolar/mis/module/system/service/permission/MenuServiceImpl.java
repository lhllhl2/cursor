package com.jasolar.mis.module.system.service.permission;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.google.common.collect.Lists;
import com.jasolar.mis.framework.common.enums.CommonStatusEnum;
import com.jasolar.mis.framework.common.pojo.PageResult;
import com.jasolar.mis.framework.common.redis.RedisKeyConstants;
import com.jasolar.mis.framework.common.util.object.BeanUtils;
import com.jasolar.mis.framework.redis.util.RedisUtils;
import com.jasolar.mis.module.system.controller.admin.permission.vo.menu.*;
import com.jasolar.mis.module.system.domain.admin.i18n.SystemI18nMenuDO;
import com.jasolar.mis.module.system.domain.admin.permission.MenuDO;
import com.jasolar.mis.module.system.enums.MenuTypeEnum;
import com.jasolar.mis.module.system.mapper.admin.i18n.SystemI18nMenuMapper;
import com.jasolar.mis.module.system.mapper.admin.permission.MenuMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

import static com.jasolar.mis.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.jasolar.mis.framework.common.util.collection.CollectionUtils.convertList;
import static com.jasolar.mis.framework.common.util.collection.CollectionUtils.convertMap;
import static com.jasolar.mis.module.system.domain.admin.permission.MenuDO.ID_ROOT;
import static com.jasolar.mis.module.system.enums.ErrorCodeConstants.*;

/**
 * 菜单 Service 实现
 *
 * @author zhaohuang
 */
@Service
@Slf4j
public class MenuServiceImpl implements MenuService {

    @Resource
    private MenuMapper menuMapper;

    @Resource
    private SystemI18nMenuMapper systemI18nMenuMapper;

    @Override
    // @CacheEvict(value = RedisKeyConstants.PERMISSION_MENU_ID_LIST, key = "#createReqVO.permission",
    //         condition = "#createReqVO.permission != null")
    public Long createMenu(MenuSaveVO createReqVO) {
        // 校验父菜单存在
        validateParentMenu(createReqVO.getPid(), null);
        // 校验菜单（自己）
        validateMenu(createReqVO.getPid(), createReqVO.getName(), null);

        // 插入数据库
        MenuDO menu = BeanUtils.toBean(createReqVO, MenuDO.class);
        // 处理meta字段
        if (createReqVO.getMeta() != null) {
            MenuMetaVO meta = createReqVO.getMeta();
            menu.setActiveIcon(meta.getActiveIcon());
            menu.setActivePath(meta.getActivePath());
            menu.setAffixTab(meta.getAffixTab() != null ? (meta.getAffixTab() ? 1 : 0) : null);
            menu.setAffixTabOrder(meta.getAffixTabOrder());
            menu.setBadge(meta.getBadge());
            menu.setBadgeType(meta.getBadgeType());
            menu.setBadgeVariants(meta.getBadgeVariants());
            menu.setHideChildrenInMenu(meta.getHideChildrenInMenu() != null ? (meta.getHideChildrenInMenu() ? 1 : 0) : null);
            menu.setHideInBreadcrumb(meta.getHideInBreadcrumb() != null ? (meta.getHideInBreadcrumb() ? 1 : 0) : null);
            menu.setHideInMenu(meta.getHideInMenu() != null ? (meta.getHideInMenu() ? 1 : 0) : null);
            menu.setHideInTab(meta.getHideInTab() != null ? (meta.getHideInTab() ? 1 : 0) : null);
            menu.setIcon(meta.getIcon());
            menu.setIframeSrc(meta.getIframeSrc());
            menu.setKeepAlive(meta.getKeepAlive() != null ? (meta.getKeepAlive() ? 1 : 0) : null);
            menu.setLink(meta.getLink());
            menu.setMaxNumOfOpenTab(meta.getMaxNumOfOpenTab());
            menu.setNoBasicLayout(meta.getNoBasicLayout() != null ? (meta.getNoBasicLayout() ? 1 : 0) : null);
            menu.setOpenInNewWindow(meta.getOpenInNewWindow() != null ? (meta.getOpenInNewWindow() ? 1 : 0) : null);
            menu.setMenuOrder(meta.getMenuOrder());
            menu.setQueryParams(meta.getQuery());
            menu.setTitle(meta.getTitle());
        }
        initMenuProperty(menu);
        menuMapper.insert(menu);
        // 返回
        return menu.getId();
    }

    @Override
    // @CacheEvict(value = RedisKeyConstants.PERMISSION_MENU_ID_LIST, allEntries = true)
    public void updateMenu(MenuSaveVO updateReqVO) {
        // 校验更新的菜单是否存在
        if (menuMapper.selectById(updateReqVO.getId()) == null) {
            throw exception(MENU_NOT_EXISTS);
        }
        // 校验父菜单存在
        validateParentMenu(updateReqVO.getPid(), updateReqVO.getId());
        // 校验菜单（自己）
        validateMenu(updateReqVO.getPid(), updateReqVO.getName(), updateReqVO.getId());

        // 更新到数据库
        MenuDO updateObj = BeanUtils.toBean(updateReqVO, MenuDO.class);
        // 处理meta字段
        if (updateReqVO.getMeta() != null) {
            MenuMetaVO meta = updateReqVO.getMeta();
            updateObj.setActiveIcon(meta.getActiveIcon());
            updateObj.setActivePath(meta.getActivePath());
            updateObj.setAffixTab(meta.getAffixTab() != null ? (meta.getAffixTab() ? 1 : 0) : null);
            updateObj.setAffixTabOrder(meta.getAffixTabOrder());
            updateObj.setBadge(meta.getBadge());
            updateObj.setBadgeType(meta.getBadgeType());
            updateObj.setBadgeVariants(meta.getBadgeVariants());
            updateObj.setHideChildrenInMenu(meta.getHideChildrenInMenu() != null ? (meta.getHideChildrenInMenu() ? 1 : 0) : null);
            updateObj.setHideInBreadcrumb(meta.getHideInBreadcrumb() != null ? (meta.getHideInBreadcrumb() ? 1 : 0) : null);
            updateObj.setHideInMenu(meta.getHideInMenu() != null ? (meta.getHideInMenu() ? 1 : 0) : null);
            updateObj.setHideInTab(meta.getHideInTab() != null ? (meta.getHideInTab() ? 1 : 0) : null);
            updateObj.setIcon(meta.getIcon());
            updateObj.setIframeSrc(meta.getIframeSrc());
            updateObj.setKeepAlive(meta.getKeepAlive() != null ? (meta.getKeepAlive() ? 1 : 0) : null);
            updateObj.setLink(meta.getLink());
            updateObj.setMaxNumOfOpenTab(meta.getMaxNumOfOpenTab());
            updateObj.setNoBasicLayout(meta.getNoBasicLayout() != null ? (meta.getNoBasicLayout() ? 1 : 0) : null);
            updateObj.setOpenInNewWindow(meta.getOpenInNewWindow() != null ? (meta.getOpenInNewWindow() ? 1 : 0) : null);
            updateObj.setMenuOrder(meta.getMenuOrder());
            updateObj.setQueryParams(meta.getQuery());
            updateObj.setTitle(meta.getTitle());
        }
        initMenuProperty(updateObj);
        menuMapper.updateById(updateObj);

        // 删除授予给角色的权限, 因为有可能修改了菜单关联的接口
        // permissionService.processMenuUpdated(updateObj.getId());
    }

    @Override
    // @CacheEvict(value = RedisKeyConstants.PERMISSION_MENU_ID_LIST, allEntries = true)
    public void updateMenu(MenuUpdateReqVO updateReqVO) {
        // 校验更新的菜单是否存在
        if (menuMapper.selectById(updateReqVO.getId()) == null) {
            throw exception(MENU_NOT_EXISTS);
        }
        // 校验父菜单存在
        validateParentMenu(updateReqVO.getPid(), updateReqVO.getId());
        // 校验菜单（自己）
        validateMenu(updateReqVO.getPid(), updateReqVO.getName(), updateReqVO.getId());

        // 更新到数据库
        MenuDO updateObj = BeanUtils.toBean(updateReqVO, MenuDO.class);
        // 处理meta字段
        if (updateReqVO.getMeta() != null) {
            MenuMetaVO meta = updateReqVO.getMeta();
            updateObj.setActiveIcon(meta.getActiveIcon());
            updateObj.setActivePath(meta.getActivePath());
            updateObj.setAffixTab(meta.getAffixTab() != null ? (meta.getAffixTab() ? 1 : 0) : null);
            updateObj.setAffixTabOrder(meta.getAffixTabOrder());
            updateObj.setBadge(meta.getBadge());
            updateObj.setBadgeType(meta.getBadgeType());
            updateObj.setBadgeVariants(meta.getBadgeVariants());
            updateObj.setHideChildrenInMenu(meta.getHideChildrenInMenu() != null ? (meta.getHideChildrenInMenu() ? 1 : 0) : null);
            updateObj.setHideInBreadcrumb(meta.getHideInBreadcrumb() != null ? (meta.getHideInBreadcrumb() ? 1 : 0) : null);
            updateObj.setHideInMenu(meta.getHideInMenu() != null ? (meta.getHideInMenu() ? 1 : 0) : null);
            updateObj.setHideInTab(meta.getHideInTab() != null ? (meta.getHideInTab() ? 1 : 0) : null);
            updateObj.setIcon(meta.getIcon());
            updateObj.setIframeSrc(meta.getIframeSrc());
            updateObj.setKeepAlive(meta.getKeepAlive() != null ? (meta.getKeepAlive() ? 1 : 0) : null);
            updateObj.setLink(meta.getLink());
            updateObj.setMaxNumOfOpenTab(meta.getMaxNumOfOpenTab());
            updateObj.setNoBasicLayout(meta.getNoBasicLayout() != null ? (meta.getNoBasicLayout() ? 1 : 0) : null);
            updateObj.setOpenInNewWindow(meta.getOpenInNewWindow() != null ? (meta.getOpenInNewWindow() ? 1 : 0) : null);
            updateObj.setMenuOrder(meta.getMenuOrder());
            updateObj.setQueryParams(meta.getQuery());
            updateObj.setTitle(meta.getTitle());
        }
        initMenuProperty(updateObj);
        menuMapper.updateById(updateObj);

        // 同步更新system_i18n_menu表中的title字段
        try {
            // 根据menuId查询system_i18n_menu表是否有对应数据
            LambdaQueryWrapper<SystemI18nMenuDO> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(SystemI18nMenuDO::getMenuId, updateObj.getId());
            
            SystemI18nMenuDO existingI18nRecord = systemI18nMenuMapper.selectOne(queryWrapper);
            
            if (existingI18nRecord != null) {
                // 如果存在记录，更新title字段
                String newTitle = updateObj.getTitle() != null ? updateObj.getTitle() : updateObj.getName();
                existingI18nRecord.setTitle(newTitle);
                existingI18nRecord.setUpdater("system"); // 这里可以从当前用户上下文获取
                
                int updateResult = systemI18nMenuMapper.updateById(existingI18nRecord);
                if (updateResult > 0) {
                    log.info("同步更新system_i18n_menu表成功 - menuId: {}, newTitle: {}", updateObj.getId(), newTitle);
                    
                    // 删除Redis缓存
                    try {
                        RedisUtils.del("i18n");
                        log.info("清除Redis缓存成功 - key: i18n");
                    } catch (Exception e) {
                        log.error("清除Redis缓存失败 - key: i18n", e);
                        // 缓存清除失败不影响主流程，只记录日志
                    }
                } else {
                    log.warn("同步更新system_i18n_menu表失败 - menuId: {}", updateObj.getId());
                }
            } else {
                log.info("system_i18n_menu表中未找到对应记录，无需更新 - menuId: {}", updateObj.getId());
            }
        } catch (Exception e) {
            log.error("同步更新system_i18n_menu表时发生异常 - menuId: {}", updateObj.getId(), e);
            // 国际化配置更新失败不影响菜单更新主流程，只记录日志
        }

        // 删除授予给角色的权限, 因为有可能修改了菜单关联的接口
        // permissionService.processMenuUpdated(updateObj.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    // @CacheEvict(value = RedisKeyConstants.PERMISSION_MENU_ID_LIST, allEntries = true)
    public void deleteMenu(Long id) {
        // 校验是否还有子菜单
        if (menuMapper.selectCountByParentId(id) > 0) {
            throw exception(MENU_EXISTS_CHILDREN);
        }
        // 校验删除的菜单是否存在
        if (menuMapper.selectById(id) == null) {
            throw exception(MENU_NOT_EXISTS);
        }
        // 标记删除
        menuMapper.deleteById(id);
        
        // 同步删除system_i18n_menu表中的对应记录
        try {
            // 根据menuId查询system_i18n_menu表是否有对应数据
            LambdaQueryWrapper<SystemI18nMenuDO> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(SystemI18nMenuDO::getMenuId, id);
            
            SystemI18nMenuDO existingI18nRecord = systemI18nMenuMapper.selectOne(queryWrapper);
            
            if (existingI18nRecord != null) {
                // 如果存在记录，删除该记录
                int deleteResult = systemI18nMenuMapper.deleteById(existingI18nRecord.getId());
                if (deleteResult > 0) {
                    log.info("同步删除system_i18n_menu表记录成功 - menuId: {}, i18nRecordId: {}", id, existingI18nRecord.getId());
                    
                    // 删除Redis缓存
                    try {
                        RedisUtils.del("i18n");
                        log.info("清除Redis缓存成功 - key: i18n");
                    } catch (Exception e) {
                        log.error("清除Redis缓存失败 - key: i18n", e);
                        // 缓存清除失败不影响主流程，只记录日志
                    }
                } else {
                    log.warn("同步删除system_i18n_menu表记录失败 - menuId: {}, i18nRecordId: {}", id, existingI18nRecord.getId());
                }
            } else {
                log.info("system_i18n_menu表中未找到对应记录，无需删除 - menuId: {}", id);
            }
        } catch (Exception e) {
            log.error("同步删除system_i18n_menu表记录时发生异常 - menuId: {}", id, e);
            // 国际化配置删除失败不影响菜单删除主流程，只记录日志
        }
        
        // 删除授予给角色的权限
        // permissionService.processMenuDeleted(id);
    }

    @Override
    public List<MenuDO> getMenuList() {
        return menuMapper.selectList();
    }

    @Override
    public List<MenuDO> getMenuListByTenant(MenuListReqVO reqVO) {
        // 查询所有菜单，并过滤掉关闭的节点
        List<MenuDO> menus = getMenuList(reqVO);
        // 开启多租户的情况下，需要过滤掉未开通的菜单
        return menus;
    }

    @Override
    public List<MenuDO> filterDisableMenus(List<MenuDO> menuList) {
        if (CollUtil.isEmpty(menuList)) {
            return Collections.emptyList();
        }
        Map<Long, MenuDO> menuMap = convertMap(menuList, MenuDO::getId);

        // 遍历 menu 菜单，查找不是禁用的菜单，添加到 enabledMenus 结果
        List<MenuDO> enabledMenus = new ArrayList<>();
        Set<Long> disabledMenuCache = new HashSet<>(); // 存下递归搜索过被禁用的菜单，防止重复的搜索
        for (MenuDO menu : menuList) {
            if (isMenuDisabled(menu, menuMap, disabledMenuCache)) {
                continue;
            }
            enabledMenus.add(menu);
        }
        return enabledMenus;
    }

    private boolean isMenuDisabled(MenuDO node, Map<Long, MenuDO> menuMap, Set<Long> disabledMenuCache) {
        // 如果已经判定是禁用的节点，直接结束
        if (disabledMenuCache.contains(node.getId())) {
            return true;
        }

        // 1. 先判断自身是否禁用
        if (CommonStatusEnum.isDisable(node.getStatus())) {
            disabledMenuCache.add(node.getId());
            return true;
        }

        // 2. 遍历到 pid 为根节点，则无需判断
        Long parentId = node.getPid();
        if (ObjectUtil.equal(parentId, ID_ROOT)) {
            return false;
        }

        // 3. 继续遍历 parent 节点
        MenuDO parent = menuMap.get(parentId);
        if (parent == null || isMenuDisabled(parent, menuMap, disabledMenuCache)) {
            disabledMenuCache.add(node.getId());
            return true;
        }
        return false;
    }

    @Override
    public List<MenuDO> getMenuList(MenuListReqVO reqVO) {
        return menuMapper.selectList(reqVO);
    }

    @Override
    public List<MenuDO> getMenuList(MenuReqVO reqVO) {
        return menuMapper.selectList(reqVO);
    }

    @Override
    public PageResult<MenuDO> getMenuPage(MenuListReqVO reqVO) {
        return menuMapper.selectPage(reqVO);
    }

    @Override
    @Cacheable(value = RedisKeyConstants.PERMISSION_MENU_ID_LIST, key = "#permission")
    public List<Long> getMenuIdListByPermissionFromCache(String permission) {
        List<MenuDO> menus = menuMapper.selectListByPermission(permission);
        return convertList(menus, MenuDO::getId);
    }

    @Override
    public MenuDO getMenu(Long id) {
        return menuMapper.selectById(id);
    }

    @Override
    public List<MenuDO> getMenuList(Collection<Long> ids) {
        // 当 ids 为空时，返回一个空的实例对象
        if (CollUtil.isEmpty(ids)) {
            return Lists.newArrayList();
        }
        return menuMapper.selectBatchIds(ids);
    }

    /**
     * 校验父菜单是否合法
     * <p>
     * 1. 不能设置自己为父菜单
     * 2. 父菜单不存在
     *
     * @param parentId 父菜单编号
     * @param childId 当前菜单编号
     */
    // @VisibleForTesting
    void validateParentMenu(Long parentId, Long childId) {
        if (ID_ROOT.equals(parentId)) {
            return;
        }
        // 不能设置自己为父菜单
        if (parentId.equals(childId)) {
            throw exception(MENU_PARENT_ERROR);
        }
        MenuDO menu = menuMapper.selectById(parentId);
        // 父菜单不存在
        if (menu == null) {
            throw exception(MENU_PARENT_NOT_EXISTS);
        }
        // 父菜单必须是目录或者菜单类型
        if (!MenuTypeEnum.CATALOG.getType().equals(menu.getType()) && !MenuTypeEnum.MENU.getType().equals(menu.getType())) {
            throw exception(MENU_PARENT_NOT_DIR_OR_MENU);
        }
    }

    /**
     * 校验菜单是否合法
     * <p>
     * 1. 校验相同父菜单下，菜单名称是否重复
     *
     * @param parentId 父菜单编号
     * @param name 菜单名称
     * @param id 菜单编号
     */
    // @VisibleForTesting
    void validateMenu(Long parentId, String name, Long id) {
        MenuDO menu = menuMapper.selectByParentIdAndName(parentId, name);
        if (menu == null) {
            return;
        }
        // 如果 id 为空，说明不用比较是否为相同 id 的菜单
        if (id == null) {
            throw exception(MENU_NAME_DUPLICATE);
        }
        if (!menu.getId().equals(id)) {
            throw exception(MENU_NAME_DUPLICATE);
        }
    }

    /**
     * 初始化菜单的通用属性。
     * <p>
     * 例如说，只有目录或者菜单类型的菜单，才设置 icon
     *
     * @param menu 菜单
     */
    private void initMenuProperty(MenuDO menu) {
        // 菜单为按钮类型时，无需 component、icon、path 属性，进行置空
        if (MenuTypeEnum.BUTTON.getType().equals(menu.getType())) {
            menu.setComponent("");
            menu.setIcon("");
            // menu.setPath("");
        }
    }

    @Override
    public Boolean checkMenuNameDuplicate(MenuAddCheckReqVO reqVO) {
        // 查询数据库中是否已存在相同名称的菜单（全局检查，因为name用于翻译）
        MenuDO existingMenu = menuMapper.selectByName(reqVO.getName());
        // 如果存在则返回true（重复），否则返回false（不重复）
        return existingMenu != null;
    }

    /**
     * 测试Oracle null值处理问题是否已解决
     * 这个方法用于验证MyBatis配置是否正确处理了Oracle的null值
     */
    public MenuDO testOracleNullHandling(Long id) {
        // 测试selectOne方法，这是之前出错的地方
        return menuMapper.selectOne(MenuDO::getId, id);
    }











}
