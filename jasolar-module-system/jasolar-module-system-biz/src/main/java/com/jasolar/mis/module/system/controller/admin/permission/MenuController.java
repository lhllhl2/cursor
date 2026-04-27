package com.jasolar.mis.module.system.controller.admin.permission;

import cn.hutool.core.collection.CollUtil;
import com.jasolar.mis.framework.common.enums.CommonStatusEnum;
import com.jasolar.mis.framework.common.pojo.CommonResult;
import com.jasolar.mis.framework.common.pojo.PageResult;
import com.jasolar.mis.framework.common.util.object.BeanUtils;
import com.jasolar.mis.module.system.controller.admin.permission.vo.menu.*;
import com.jasolar.mis.module.system.domain.admin.permission.MenuDO;
import com.jasolar.mis.module.system.service.permission.MenuService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

import static com.jasolar.mis.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 菜单")
@RestController
@RequestMapping("/system/menu")
@Validated
public class MenuController {

    @Resource
    private MenuService menuService;

    @PostMapping("/create")
    @Operation(summary = "创建菜单")
    public CommonResult<Long> createMenu(@Valid @RequestBody MenuSaveVO createReqVO) {
        Long menuId = menuService.createMenu(createReqVO);
        return success(menuId);
    }

    @PutMapping("/update")
    @Operation(summary = "修改菜单")
    public CommonResult<Boolean> updateMenu(@Valid @RequestBody MenuUpdateReqVO updateReqVO) {
        menuService.updateMenu(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除菜单")
    @Parameter(name = "id", description = "菜单编号", required = true, example = "1024")
    public CommonResult<Boolean> deleteMenu(@RequestParam("id") Long id) {
        menuService.deleteMenu(id);
        return success(true);
    }

    @GetMapping("/pageList")
    @Operation(summary = "获取菜单分页列表", description = "用于【菜单管理】界面")
    public CommonResult<PageResult<MenuRespVO>> getPageMenuList(MenuListReqVO reqVO) {
        // 先查询所有符合条件的数据（不分页）
        List<MenuDO> allMenus = menuService.getMenuList(reqVO);
        
        // 转换为MenuRespVO列表
        List<MenuRespVO> menuList = allMenus.stream()
                .map(this::convertToMenuRespVO)
                .collect(Collectors.toList());
        
        // 构建树形结构（内部已包含排序）
        List<MenuRespVO> treeList = buildMenuTree(menuList);
        
        // 对树形结构进行分页处理
        List<MenuRespVO> pagedTreeList = paginateTreeList(treeList, reqVO);
        
        return success(new PageResult<>(pagedTreeList, (long) treeList.size()));
    }

    @GetMapping("/list")
    @Operation(summary = "获取菜单列表", description = "用于【菜单管理】界面")
    public CommonResult<List<MenuRespVO>> getMenuList(MenuReqVO reqVO) {
        // 1. 先查询所有符合条件的数据（不分页）
        List<MenuDO> allMenus = menuService.getMenuList(reqVO);
        
        // 2. 转换为MenuRespVO列表
        List<MenuRespVO> menuList = allMenus.stream()
                .map(this::convertToMenuRespVO)
                .collect(Collectors.toList());
        
        // 3. 构建树形结构（内部已包含排序）
        List<MenuRespVO> treeList = buildMenuTree(menuList);
        
        // 4. 就返回就好了
        return success(treeList);
    }

    @GetMapping({"/list-all-simple", "simple-list"})
    @Operation(summary = "获取菜单精简信息列表", description = "只包含被开启的菜单，用于【角色分配菜单】功能的选项。" +
            "在多租户的场景下，会只返回租户所在套餐有的菜单")
    public CommonResult<List<MenuSimpleRespVO>> getSimpleMenuList() {
        List<MenuDO> list = menuService.getMenuListByTenant(
                MenuListReqVO.builder().status(CommonStatusEnum.ENABLE.getStatus()).build());
        list = menuService.filterDisableMenus(list);
        list.sort(Comparator.comparing(MenuDO::getMenuOrder));
        List<MenuSimpleRespVO> ls =   BeanUtils.toBean(list, MenuSimpleRespVO.class);
        return success(ls);
    }

    @GetMapping("/get")
    @Operation(summary = "获取菜单信息")
    public CommonResult<MenuRespVO> getMenu(Long id) {
        MenuDO menu = menuService.getMenu(id);
        return success(convertToMenuRespVO(menu));
    }

    @GetMapping("/checkname")
    @Operation(summary = "检查菜单名称是否重复", description = "用于新增菜单时检查名称是否重复（全局检查，因为name用于翻译）")
    public CommonResult<Boolean> checkMenuName(@RequestParam("name") @NotBlank(message = "菜单名称不能为空") String name) {
        MenuAddCheckReqVO reqVO = new MenuAddCheckReqVO();
        reqVO.setName(name);
        Boolean isDuplicate = menuService.checkMenuNameDuplicate(reqVO);
        return success(isDuplicate);
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



    /**
     * 构建菜单树形结构
     */
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
     * 对树形结构进行分页处理
     */
    private List<MenuRespVO> paginateTreeList(List<MenuRespVO> treeList, MenuListReqVO reqVO) {
        if (CollUtil.isEmpty(treeList)) {
            return Collections.emptyList();
        }
        
        // 计算分页参数
        int startIndex = (reqVO.getPageNo() - 1) * reqVO.getPageSize();
        int endIndex = startIndex + reqVO.getPageSize();
        
        // 如果请求的是第一页且数据量小于等于页面大小，直接返回
        if (startIndex == 0 && treeList.size() <= reqVO.getPageSize()) {
            return treeList;
        }
        
        // 对树形结构进行分页
        if (startIndex >= treeList.size()) {
            return Collections.emptyList();
        }
        
        endIndex = Math.min(endIndex, treeList.size());
        return treeList.subList(startIndex, endIndex);
    }

}
