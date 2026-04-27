package com.jasolar.mis.framework.datapermission.core.rule;

import java.util.List;
import java.util.Objects;

import com.jasolar.mis.framework.common.enums.UserTypeEnum;
import com.jasolar.mis.framework.datapermission.core.annotation.DataRule;
import com.jasolar.mis.framework.datapermission.core.scope.ReadWrite;

import lombok.Getter;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.schema.Table;

/**
 * 数据权限组合规则. 注意:
 * <ol>
 * <li>一个组合规则之间的多个权限维度之间是OR的关系.</li>
 * <li>多个组合之间是AND的关系</li>
 * <ol>
 * 
 * @author galuo
 * @date 2025-03-04 19:40
 *
 */
public class CompositeDataPermissionRule implements DataPermissionRule {

    /**
     * 根据指定的权限规则构建数据
     * 
     * @param rules 已经初始化的权限规则列表
     * @return
     */
    public static CompositeDataPermissionRule of(List<DataPermissionRule> rules) {
        CompositeDataPermissionRule r = new CompositeDataPermissionRule();
        r.rules = rules;
        return r;
    }

    /** 多个权限维度, 每个权限维度之间为OR的关系 */
    @Getter
    private List<DataPermissionRule> rules;

    @Override
    public boolean accept(Table table) {
        return rules.stream().anyMatch(r -> r.accept(table));
    }

    @Override
    public boolean accept(UserTypeEnum userType) {
        return rules.stream().anyMatch(r -> r.accept(userType));
    }

    @Override
    public Expression getExpression(Table table) {
        // LoginUser user = LoginServletUtils.getLoginUser();
        // if (user == null || user.userType() != UserTypeEnum.ADMIN) {
        // // 只有管理员类型的用户，才进行数据权限的处理.
        // // 非管理员则表示前端供应商，在供应商门户接口中会固定使用供应商权限进行控制
        // return null;
        // }
        //
        // List<DataScope> scopes = findDataScopes(user, table);
        // boolean all = scopes.parallelStream().anyMatch(s -> ScopeLevel.ALL == s.getLevel());
        // if (all) {
        // // 任意权限范围有所有权限等级
        // return DataPermissionRule.ALL;
        // }

        List<DataPermissionRule> acceptRules = rules.stream().filter(r -> r.accept(table)).toList();
        List<Expression> exprs = acceptRules.stream().map(r -> r.getExpression(table)).filter(Objects::nonNull)
                .filter(expr -> expr != DataPermissionRule.NONE).toList();
        if (exprs.contains(DataPermissionRule.ALL)) {
            // 有所有权限
            return DataPermissionRule.ALL;
        }
        return exprs.isEmpty() ? DataPermissionRule.NONE : DataPermissionRule.anyOf(exprs);
    }

    @Override
    public DataPermissionRule createRule(DataRule rule) {
        return this;
    }

    @Override
    public DataPermissionRule copy() {
        return this;
    }

    @Override
    public void setReadWrite(ReadWrite rw) {
        // 忽略
    }

}
