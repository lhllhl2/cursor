package com.jasolar.mis.framework.datapermission.core.rule;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.CollectionUtils;

import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import com.jasolar.mis.framework.common.security.LoginServletUtils;
import com.jasolar.mis.framework.common.security.LoginUser;
import com.jasolar.mis.framework.datapermission.core.annotation.BpmTaskTable;
import com.jasolar.mis.framework.datapermission.core.annotation.DataRule;
import com.jasolar.mis.framework.datapermission.core.annotation.DataTable;
import com.jasolar.mis.framework.datapermission.core.rule.impl.BpmTaskDataPermissionRule;
import com.jasolar.mis.framework.datapermission.core.scope.DataScope;
import com.jasolar.mis.framework.datapermission.core.scope.ReadWrite;
import com.jasolar.mis.framework.datapermission.core.scope.RequestContextHolder;
import com.jasolar.mis.framework.datapermission.core.scope.ScopeLevel;
import com.jasolar.mis.framework.datapermission.core.scope.ScopeType;
import com.jasolar.mis.framework.datapermission.core.service.DataScopeService;
import com.jasolar.mis.framework.mybatis.core.util.MyBatisUtils;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import jakarta.annotation.Resource;
import lombok.Getter;
import lombok.Setter;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.StringValue;
import net.sf.jsqlparser.expression.operators.relational.EqualsTo;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.expression.operators.relational.InExpression;
import net.sf.jsqlparser.expression.operators.relational.IsNullExpression;
import net.sf.jsqlparser.expression.operators.relational.ParenthesedExpressionList;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;

/**
 * {@link DataPermissionRule} 的默认实现。 注意实现了{@link InitializingBean}接口，在{@link #afterPropertiesSet()}方法中初始化，注入所有需要数据权限的表格
 * 
 * @author galuo
 * @date 2025-03-03 09:51
 *
 */
public abstract class BaseDataPermissionRule implements DataPermissionRule, InitializingBean {

    /** 此维度默认注册的所有表信息，为空则需要在方法上通过{@link DataTable}注入使用的表信息 */
    protected List<DataPermissionTable> defaultTables = Collections.emptyList();

    /** 使用的读/写权限，用于读写分离。 默认读/写权限不分离 */
    @Getter
    @Setter
    protected ReadWrite readWrite = ReadWrite.ALL;

    // /** 是否允许查询NULL值 */
    // @Getter
    // protected boolean nullable;

    /** 通过注解{@link DataTable}注入使用的表和列信息。会在AOP拦截时读取注解并初始化 */
    protected final List<DataPermissionTable> injectTables = new ArrayList<>();

    /** 是否由注解注入的表格信息，如果由注解注入，则使用{@link #injectTables}进行判断，否则使用{@link #defaultTables} */
    protected boolean injected;

    @Resource
    @Setter
    protected DataScopeService dataPermissionService;

    /**
     * 读写分离权限
     * 
     * @return 读/写权限
     */
    public ReadWrite readWrite() {
        // 优先读取请求传入的header
        ReadWrite rw = RequestContextHolder.readWrite();
        return rw == null ? this.readWrite : rw;
    }

    @Override
    public boolean accept(Table table) {
        return (injected ? injectTables : defaultTables).stream().anyMatch(t -> t.match(table));
    }

    /** @return 使用的权限类型，用于过滤要处理的权限范围 */
    public abstract ScopeType scopeType();

    /** 用于过滤权限范围,权限类型一致并且读/写一致才返回true */
    protected boolean accept(DataScope scope) {
        if (this.scopeType() != scope.getType()) {
            return false;
        }
        ReadWrite rw = readWrite();
        return rw == ReadWrite.ALL || scope.getRw() == ReadWrite.ALL || rw == scope.getRw();
    }

    /**
     * 查询数据权限范围
     * 
     * @param user 人员
     * @param table 查询的表
     * @return 权限范围,没有权限则返回空列表
     */
    @Nonnull
    protected List<DataScope> findDataScopes(LoginUser user, Table table) {
        Long menuId = RequestContextHolder.menuId();
        // 理论上每种维度应该只有一个数据权限范围，多个之间用OR的关系
        List<DataScope> scopes = this.dataPermissionService.findDataPermissionScopes(user.getNo(), menuId, s -> accept(s));
        return scopes == null ? Collections.emptyList() : scopes;
    }

    /**
     * 构造单个数据权限的查询SQL
     * 
     * @param dataTable 数据权限表配置
     * @param table 表
     * @param columnName 列名
     * @param scope 数据权限范围
     * @return SQL查询表达式
     */
    @Nullable
    protected Expression getExpression(DataPermissionTable dataTable, Table table, String columnName, DataScope scope) {
        Column column = MyBatisUtils.buildColumn(table.getName(), table.getAlias(), columnName);
        Expression isnull = scope.isNullable()
                ? DataPermissionRule.anyOf(new IsNullExpression(column), new EqualsTo(column, new StringValue(StringUtils.EMPTY)))
                : null;
        if (CollectionUtils.isEmpty(scope.getDataIds())) {
            // 未指定数据ID
            return isnull;
        }

        // Parenthesis 的目的，是提供 (1,2,3) 的 () 左右括号
        InExpression express = new InExpression(column, new ParenthesedExpressionList<StringValue>(
                new ExpressionList<StringValue>(scope.getDataIds().stream().map(StringValue::new).toList())));

        return isnull == null ? express : DataPermissionRule.anyOf(express, isnull);
    }

    @Override
    public Expression getExpression(Table table) {
        LoginUser user = LoginServletUtils.getLoginUser();
        if (user == null || !this.accept(user.userType())) {
            // 用户类型与当前规则不匹配
            return null;
        }

        List<DataScope> scopes = findDataScopes(user, table);
        if (scopes.isEmpty()) {
            // 没有满足此规则的数据权限配置
            return null;
        }

        if (scopes.stream().anyMatch(scope -> ScopeLevel.ALL == scope.getLevel())) {
            // 配置了所有权限
            return ALL;
        }

        List<DataPermissionTable> tables = injected ? injectTables : defaultTables;

        List<Expression> exprs = new ArrayList<>();
        // 理论上只会match出一个表
        tables.stream().filter(t -> !CollectionUtils.isEmpty(t.getColumns()) && t.match(table)).forEach(t -> {
            for (String columnName : t.getColumns()) {
                for (DataScope scope : scopes) {
                    Expression expr = this.getExpression(t, table, columnName, scope);
                    if (expr != null) {
                        exprs.add(expr);
                    }
                }
            }
        });

        return exprs.isEmpty() ? null : DataPermissionRule.anyOf(exprs);
    }

    @Override
    public DataPermissionRule createRule(DataRule rule) {
        if (!rule.value().isAssignableFrom(getClass())) {
            // 外部调用时应该保证参数rule配置的规则就是this对象
            throw new IllegalArgumentException(rule.value() + "cannot be assignable from this class:" + this.getClass());
        }

        BaseDataPermissionRule r = BeanUtils.instantiateClass(this.getClass());
        r.dataPermissionService = this.dataPermissionService;
        r.defaultTables = this.defaultTables;
        r.readWrite = rule.readWrite();

        DataTable[] tables = rule.tables();

        r.injected = true;

        for (DataTable t : tables) {
            DataPermissionTable dt = DataPermissionTable.of(t.schema(), t.name(), t.alias(), t.columns());
            BpmTaskTable[] bpms = t.bpmTaskTable();
            if (bpms.length > 0) {
                BpmTaskTableCfg cfg = BpmTaskDataPermissionRule.cfg(bpms[0]);
                dt.setBpmTaskTable(cfg);
            }

            r.injectTables.add(dt);
        }
        return r;
    }

    @Override
    public DataPermissionRule copy() {
        BaseDataPermissionRule r = BeanUtils.instantiateClass(this.getClass());
        r.dataPermissionService = this.dataPermissionService;
        r.defaultTables = this.defaultTables;
        r.readWrite = this.readWrite;
        r.injected = this.injected;
        r.injectTables.addAll(this.injectTables);
        return r;
    }

    // /**
    // * 添加一个默认的表。用于对象初始化时统一设置所有表的权限字段
    // *
    // * @param entityClass 实体类对象
    // * @param field 使用单个列字段
    // */
    // @SuppressWarnings("unchecked")
    // public <T> DataPermissionTable addDefaultTable(Class<T> entityClass, SFunction<? extends T, ?> field) {
    // return addDefaultTables(entityClass, field);
    // }

    /**
     * 添加一个默认的表。用于对象初始化时统一设置所有表的权限字段
     * 
     * @param entityClass 实体类对象
     * @param fields 使用的多个列字段
     */
    @SuppressWarnings("unchecked")
    public <T> DataPermissionTable addDefaultTable(Class<T> entityClass, SFunction<? extends T, ?>... fields) {
        DataPermissionTable t = DataPermissionTable.of(entityClass, fields);
        if (defaultTables == null || defaultTables.isEmpty()) {
            // 重新初始化，默认值Collections.emptyList()无法添加数据
            defaultTables = new ArrayList<>();
        }
        defaultTables.add(t);

        return t;
    }

}
