package com.jasolar.mis.framework.datapermission.core.rule.impl;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import com.jasolar.mis.framework.common.security.LoginUser;
import com.jasolar.mis.framework.datapermission.core.annotation.DataRule;
import com.jasolar.mis.framework.datapermission.core.rule.BaseDataPermissionRule;
import com.jasolar.mis.framework.datapermission.core.rule.DataPermissionRule;
import com.jasolar.mis.framework.datapermission.core.rule.DataPermissionTable;
import com.jasolar.mis.framework.datapermission.core.scope.DataScope;

import jakarta.annotation.Nonnull;
import net.sf.jsqlparser.schema.Table;

/**
 * 支持查询自己所属数据的权限规则
 * 
 * @author galuo
 * @date 2025-06-17 18:35
 *
 */
public abstract class BaseSelfDataPermissionRule extends BaseDataPermissionRule {

    /** 本身 */
    protected Set<String> selfTables = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);

    /**
     * 添加一个表
     * 
     * @param table
     */
    protected void addSelfTable(DataPermissionTable table) {
        this.selfTables.add(table.getName());
    }

    /**
     * 是否为此权限维度本身的表
     * 
     * @param entityClass
     * @return
     */
    protected abstract boolean isSelf(Class<?> entityClass);

    /**
     * 构造查询自己所属的权限
     * 
     * @param user 登录人员
     * @return
     */
    protected abstract DataScope self(LoginUser user);

    /**
     * 构建查询自己所属的权限
     * 
     * @param user
     * @param table
     * @return
     */
    @Nonnull
    protected List<DataScope> findSelfDataScopes(LoginUser user, Table table) {
        if (table.getName() == null || !selfTables.contains(table.getName())) {
            return Collections.emptyList();
        }
        return Arrays.asList(this.self(user));
    }

    @Override
    @Nonnull
    protected List<DataScope> findDataScopes(LoginUser user, Table table) {
        List<DataScope> scopes = super.findDataScopes(user, table);
        if (!scopes.isEmpty()) {
            return scopes;
        }

        // 如果没有配置人员权限,则默认查询自己的数据
        return findSelfDataScopes(user, table);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> DataPermissionTable addDefaultTable(Class<T> entityClass, SFunction<? extends T, ?>... fields) {
        DataPermissionTable t = super.addDefaultTable(entityClass, fields);
        if (isSelf(entityClass)) {
            this.addSelfTable(t);
        }
        return t;
    }

    @Override
    public DataPermissionRule createRule(DataRule rule) {
        BaseSelfDataPermissionRule r = (BaseSelfDataPermissionRule) super.createRule(rule);
        r.selfTables = this.selfTables;
        return r;
    }

    @Override
    public DataPermissionRule copy() {
        BaseSelfDataPermissionRule r = (BaseSelfDataPermissionRule) super.copy();
        r.selfTables = this.selfTables;
        return r;
    }

}
