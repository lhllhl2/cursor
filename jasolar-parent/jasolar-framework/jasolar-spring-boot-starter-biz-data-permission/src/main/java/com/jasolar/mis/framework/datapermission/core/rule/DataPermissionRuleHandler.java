package com.jasolar.mis.framework.datapermission.core.rule;

import java.util.List;

import com.baomidou.mybatisplus.extension.plugins.handler.MultiDataPermissionHandler;

import cn.hutool.core.collection.CollUtil;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.schema.Table;

/**
 * 基于 {@link DataPermissionRule} 的数据权限处理器
 *
 * 它的底层，是基于 MyBatis Plus 的 <a href="https://baomidou.com/plugins/data-permission/">数据权限插件</a>
 * 核心原理：它会在 SQL 执行前拦截 SQL 语句，并根据用户权限动态添加权限相关的 SQL 片段。这样，只有用户有权限访问的数据才会被查询出来
 *
 * @author zhaohuang
 */
@AllArgsConstructor
@NoArgsConstructor
public class DataPermissionRuleHandler implements MultiDataPermissionHandler {

    @Setter
    private DataPermissionRuleFactory ruleFactory;

    /** 是否开启数据权限, 默认为true */
    @Setter
    @Getter
    private boolean enabled = true;

    @Override
    public Expression getSqlSegment(Table table, Expression where, String mappedStatementId) {
        if (!enabled) {
            // 关闭了数据权限
            return null;
        }

        // 获得 Mapper 对应的数据权限的规则
        List<DataPermissionRule> rules = ruleFactory.currentRules();
        if (CollUtil.isEmpty(rules)) {
            return null;
        }

        List<Expression> exprs = rules.stream().filter(r -> r.accept(table)).map(r -> r.getExpression(table))
                .filter(exp -> DataPermissionRule.ALL != exp).toList();
        return exprs.isEmpty() ? null : DataPermissionRule.allOf(exprs);
    }

}
