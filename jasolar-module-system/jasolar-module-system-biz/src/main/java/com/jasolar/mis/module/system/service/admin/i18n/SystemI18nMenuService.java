package com.jasolar.mis.module.system.service.admin.i18n;

import com.jasolar.mis.module.system.controller.admin.i18n.vo.SystemI18nMenuTreeRespVO;

import java.util.List;
import java.util.Map;

/**
 * 菜单国际化配置 Service 接口
 *
 * @author jasolar
 */
public interface SystemI18nMenuService {

    /**
     * 保存或更新菜单国际化配置
     *
     * @param menuId 菜单ID
     * @param jsonData 翻译数据
     * @return 操作结果
     */
    boolean saveOrUpdateI18nConfig(Long menuId, Map<String, Map<String, String>> jsonData);

    /**
     * 根据菜单ID查询国际化配置
     *
     * @param menuId 菜单ID
     * @return 国际化配置Map，key为语言类型，value为翻译数据Map
     */
    Map<String, Map<String, String>> getI18nConfigByMenuId(Long menuId);

    /**
     * 获取所有菜单的国际化配置（树形结构）
     *
     * @return 菜单树形结构列表，包含多语言翻译数据
     */
    List<SystemI18nMenuTreeRespVO> getAllMenuI18nConfig();
} 