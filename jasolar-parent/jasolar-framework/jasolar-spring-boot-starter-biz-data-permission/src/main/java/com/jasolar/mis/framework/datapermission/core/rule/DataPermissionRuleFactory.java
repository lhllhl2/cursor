package com.jasolar.mis.framework.datapermission.core.rule;

import java.util.List;

/**
 * 数据权限规则工厂,用于读取当前线程中的注解配置初始化权限规则列表
 * 
 * @author galuo
 * @date 2025-03-04 19:54
 *
 */
public interface DataPermissionRuleFactory {

    /**
     * 获取当前线程使用的数据权限规则列表. 获取到的多个规则之间是AND的关系
     * 
     * @return
     */
    List<DataPermissionRule> currentRules();

}
