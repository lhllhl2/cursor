package com.jasolar.mis.framework.datapermission.core.rule;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import com.jasolar.mis.framework.common.enums.UserTypeEnum;
import com.jasolar.mis.framework.common.security.LoginServletUtils;
import com.jasolar.mis.framework.common.security.LoginUser;
import com.jasolar.mis.framework.datapermission.core.annotation.DataRule;
import com.jasolar.mis.framework.datapermission.core.aop.DataPermissionContext;
import com.jasolar.mis.framework.datapermission.core.aop.DataPermissionContextHolder;

import lombok.RequiredArgsConstructor;

/**
 * 默认的 DataPermissionRuleFactoryImpl 实现类
 * 支持通过 {@link DataPermissionContextHolder} 过滤数据权限
 *
 * @author zhaohuang
 */
@RequiredArgsConstructor
public class DataPermissionRuleFactoryImpl implements DataPermissionRuleFactory {

    /** 所有Spring容器管理的数据权限规则 */
    private final List<DataPermissionRule> rules;

    /**
     * @return 所有Spring容器管理的数据权限规则
     */
    public List<DataPermissionRule> all() {
        return rules;
    }

    @Override
    public List<DataPermissionRule> currentRules() {
        // 1. 无数据权限
        if (rules == null || rules.isEmpty()) {
            return Collections.emptyList();
        }
        LoginUser user = LoginServletUtils.getLoginUser();
        if (user == null || UserTypeEnum.SERVER == user.userType()) {
            // 未登录或者是第三方服务调用
            return null;
        }

        UserTypeEnum userType = user.userType();
        if (UserTypeEnum.SUPPLIER == userType) {
            // 供应商登录，无论什么接口都开启供应商权限控制
            return Arrays.asList(CompositeDataPermissionRule.of(rules.stream().filter(r -> r.accept(userType)).toList()));
        }

        // 2. 未配置，或者配置了禁用,或者没有配置权限控制规则,则不做数据权限控制
        DataPermissionContext ctx = DataPermissionContextHolder.get();
        if (ctx == null || !ctx.hasRule()) {
            return null;
        }

        return ctx.getPermissions().stream().map(p -> {
            DataRule[] drs = p.value();
            if (drs.length < 1) {
                // 默认使用全部权限规则,并使用默认的表配置
                return (DataPermissionRule) CompositeDataPermissionRule.of(rules.stream().filter(r -> r.accept(userType)).map(r -> {
                    DataPermissionRule copy = r.copy();
                    copy.setReadWrite(p.readWrite());
                    return copy;
                }).toList());
            }

            List<DataPermissionRule> list = new ArrayList<>();
            for (DataRule r : drs) {
                // 筛选注解配置的规则
                List<DataPermissionRule> filtered = rules.stream().filter(rule -> !rule.accept(userType) && r.value().isInstance(rule))
                        .map(rule -> rule.createRule(r)).toList();
                list.addAll(filtered);
            }
            return list.isEmpty() ? null : (DataPermissionRule) CompositeDataPermissionRule.of(list);
        }).filter(Objects::nonNull).toList();

    }

}
