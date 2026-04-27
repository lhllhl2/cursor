package com.jasolar.mis.module.system.service.admin.i18n;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.jasolar.mis.framework.common.util.json.JsonUtils;
import com.jasolar.mis.module.system.controller.admin.i18n.vo.SystemI18nMenuTreeRespVO;
import com.jasolar.mis.module.system.domain.admin.i18n.SystemI18nMenuDO;
import com.jasolar.mis.module.system.domain.admin.permission.MenuDO;
import com.jasolar.mis.module.system.mapper.admin.i18n.SystemI18nMenuMapper;
import com.jasolar.mis.module.system.service.permission.MenuService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 国际化菜单翻译 Service 实现类
 *
 * @author jasolar
 */
@Service
@Validated
@Slf4j
public class SystemI18nMenuServiceImpl implements SystemI18nMenuService {

    @Resource
    private SystemI18nMenuMapper systemI18nMenuMapper;

    @Resource
    private MenuService menuService;

    @Override
    public boolean saveOrUpdateI18nConfig(Long menuId, Map<String, Map<String, String>> jsonData) {
        try {
            // 根据menuId查询菜单表获取title
            String title = getMenuTitleByMenuId(menuId);
            
            // 查询是否已存在记录（locale 固定为 ALL，MyBatis-Plus 会自动添加 deleted = 0 的条件）
            LambdaQueryWrapper<SystemI18nMenuDO> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(SystemI18nMenuDO::getMenuId, menuId);
            
            SystemI18nMenuDO existingRecord = systemI18nMenuMapper.selectOne(queryWrapper);
            
            if (existingRecord != null) {
                // 更新现有记录
                existingRecord.setTitle(title);
                existingRecord.setJsonData(JsonUtils.toJsonString(jsonData));
                existingRecord.setUpdater("system"); // 这里可以从当前用户上下文获取
                
                int result = systemI18nMenuMapper.updateById(existingRecord);
                log.info("更新国际化配置 - menuId: {}, result: {}", menuId, result);
                return result > 0;
            } else {
                // 创建新记录
                SystemI18nMenuDO newRecord = new SystemI18nMenuDO();
                // 不设置ID，让MyBatis-Plus自动使用雪花算法生成
                newRecord.setMenuId(menuId);
                newRecord.setTitle(title);
                newRecord.setLocale("ALL"); // 固定为 ALL
                newRecord.setJsonData(JsonUtils.toJsonString(jsonData));
                newRecord.setCreator("system"); // 这里可以从当前用户上下文获取
                newRecord.setUpdater("system");
                
                int result = systemI18nMenuMapper.insert(newRecord);
                log.info("创建国际化配置 - menuId: {}, result: {}", menuId, result);
                return result > 0;
            }
        } catch (Exception e) {
            log.error("保存国际化配置失败 - menuId: {}", menuId, e);
            return false;
        }
    }

    @Override
    public Map<String, Map<String, String>> getI18nConfigByMenuId(Long menuId) {
        try {
            // 查询指定菜单ID的所有国际化配置（MyBatis-Plus 会自动添加 deleted = 0 的条件）
            LambdaQueryWrapper<SystemI18nMenuDO> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(SystemI18nMenuDO::getMenuId, menuId);
            
            List<SystemI18nMenuDO> records = systemI18nMenuMapper.selectList(queryWrapper);
            
            // 由于数据库中locale固定为"ALL"，我们直接返回jsonData的内容
            if (!records.isEmpty()) {
                SystemI18nMenuDO record = records.get(0); // 取第一条记录
                String jsonData = record.getJsonData();
                
                if (jsonData != null && !jsonData.isEmpty()) {
                    try {
                        // 将JSON字符串转换为Map<String, Map<String, String>>结构
                        // 使用TypeReference来正确处理泛型类型
                        Map<String, Map<String, String>> result = JsonUtils.parseObject(
                            jsonData, 
                            new TypeReference<Map<String, Map<String, String>>>() {}
                        );
                        log.info("查询国际化配置成功 - menuId: {}, 解析到 {} 个翻译键", menuId, result.size());
                        return result;
                    } catch (Exception e) {
                        log.error("解析JSON数据失败 - menuId: {}, jsonData: {}", menuId, jsonData, e);
                        // 如果解析失败，返回空的Map
                        return new HashMap<>();
                    }
                } else {
                    // 如果jsonData为空，返回空的Map
                    log.info("查询国际化配置成功 - menuId: {}, 但jsonData为空", menuId);
                    return new HashMap<>();
                }
            } else {
                // 如果没有找到记录，返回空的Map
                log.info("查询国际化配置成功 - menuId: {}, 未找到记录", menuId);
                return new HashMap<>();
            }
            
        } catch (Exception e) {
            log.error("查询国际化配置失败 - menuId: {}", menuId, e);
            return new HashMap<>();
        }
    }

    @Override
    public List<SystemI18nMenuTreeRespVO> getAllMenuI18nConfig() {
        try {
            // 1. 获取所有未删除的菜单数据
            List<MenuDO> allMenus = menuService.getMenuList();
            List<MenuDO> activeMenus = allMenus.stream()
                    .filter(menu -> !Boolean.TRUE.equals(menu.getDeleted()))
                    .collect(Collectors.toList());

            // 2. 获取所有国际化配置数据（MyBatis-Plus 会自动添加 deleted = 0 的条件）
            List<SystemI18nMenuDO> allI18nConfigs = systemI18nMenuMapper.selectList(new LambdaQueryWrapper<>());

            // 3. 按菜单ID分组国际化配置，合并多语言数据
            Map<Long, Map<String, Map<String, String>>> menuI18nDataMap = new HashMap<>();
            for (SystemI18nMenuDO i18nConfig : allI18nConfigs) {
                Long menuId = i18nConfig.getMenuId();
                String jsonData = i18nConfig.getJsonData();
                
                if (jsonData != null && !jsonData.isEmpty()) {
                    try {
                        // 解析JSON数据为多语言嵌套结构
                        Map<String, Map<String, String>> parsedData = JsonUtils.parseObject(
                            jsonData, 
                            new TypeReference<Map<String, Map<String, String>>>() {}
                        );
                        
                        // 合并到菜单的国际化数据中
                        menuI18nDataMap.computeIfAbsent(menuId, k -> new HashMap<>()).putAll(parsedData);
                    } catch (Exception e) {
                        log.error("解析JSON数据失败 - menuId: {}, jsonData: {}", menuId, jsonData, e);
                    }
                }
            }

            // 4. 构建统一的菜单树形结构
            List<SystemI18nMenuTreeRespVO> result = buildUnifiedMenuTree(activeMenus, menuI18nDataMap);

            log.info("获取所有菜单国际化配置成功 - 菜单数量: {}, 国际化配置数量: {}", activeMenus.size(), allI18nConfigs.size());
            return result;

        } catch (Exception e) {
            log.error("获取所有菜单国际化配置失败", e);
            return new ArrayList<>();
        }
    }

    /**
     * 构建统一的菜单树形结构，包含多语言翻译数据
     */
    private List<SystemI18nMenuTreeRespVO> buildUnifiedMenuTree(List<MenuDO> menuList, 
                                                                Map<Long, Map<String, Map<String, String>>> menuI18nDataMap) {
        if (CollUtil.isEmpty(menuList)) {
            return Collections.emptyList();
        }

        // 转换为SystemI18nMenuTreeRespVO列表
        List<SystemI18nMenuTreeRespVO> treeNodeList = menuList.stream()
                .map(menu -> {
                    SystemI18nMenuTreeRespVO treeNode = new SystemI18nMenuTreeRespVO();
                    treeNode.setId(menu.getId());
                    treeNode.setName(menu.getName()); // 设置菜单名称
                    treeNode.setTitle(menu.getTitle());
                    treeNode.setPid(menu.getPid());
                    
                    // 获取该菜单的多语言翻译数据
                    Map<String, Map<String, String>> i18nData = menuI18nDataMap.get(menu.getId());
                    if (i18nData != null && !i18nData.isEmpty()) {
                        treeNode.setJsonData(i18nData);
                    } else {
                        // 如果没有国际化配置，设置空的JSON
                        treeNode.setJsonData(new HashMap<>());
                    }
                    
                    return treeNode;
                })
                .collect(Collectors.toList());

        // 使用LinkedHashMap保持顺序
        Map<Long, SystemI18nMenuTreeRespVO> treeNodeMap = new LinkedHashMap<>();
        treeNodeList.forEach(menu -> treeNodeMap.put(menu.getId(), menu));

        // 处理父子关系
        treeNodeMap.values().stream()
                .filter(node -> !MenuDO.ID_ROOT.equals(node.getPid()))
                .forEach(childNode -> {
                    Long parentId = childNode.getPid();
                    SystemI18nMenuTreeRespVO parentNode = treeNodeMap.get(parentId);
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
     * 根据菜单ID获取菜单标题
     * @param menuId 菜单ID
     * @return 菜单标题
     */
    private String getMenuTitleByMenuId(Long menuId) {
        try {
            MenuDO menu = menuService.getMenu(menuId);
            if (menu != null) {
                // 优先使用title字段，如果没有则使用name字段
                return menu.getTitle() != null ? menu.getTitle() : menu.getName();
            } else {
                log.warn("未找到菜单，menuId: {}", menuId);
                return "未知菜单";
            }
        } catch (Exception e) {
            log.error("获取菜单标题失败，menuId: {}", menuId, e);
            return "未知菜单";
        }
    }

} 