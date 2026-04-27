package com.jasolar.mis.framework.datapermission.core.service;

import java.util.List;
import java.util.function.Predicate;

import com.jasolar.mis.framework.datapermission.core.scope.DataScope;

/**
 * 用于查询用户的数据权限范围
 */
public interface DataScopeService {

    /**
     * 获取用户的数据权限范围
     * 
     * @param userNo 用户工号
     * @param menuId 菜单ID
     * @param predicate 过滤条件
     * @return 用户的数据权限列表
     */
    List<DataScope> findDataPermissionScopes(String userNo, Long menuId, Predicate<? super DataScope> predicate);

    /**
     * 删除用户的缓存
     * 
     * @param userNo 工号
     */
    void removeCache(String userNo);

    /** 清除缓存 */
    void clearCache();
}
