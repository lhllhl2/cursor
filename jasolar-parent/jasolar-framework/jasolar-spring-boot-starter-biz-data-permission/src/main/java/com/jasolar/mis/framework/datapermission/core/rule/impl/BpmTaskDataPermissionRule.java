package com.jasolar.mis.framework.datapermission.core.rule.impl;

import java.util.Arrays;
import java.util.List;

import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.util.CollectionUtils;

import com.baomidou.mybatisplus.core.metadata.TableInfo;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.jasolar.mis.framework.common.security.LoginUser;
import com.jasolar.mis.framework.common.util.collection.SetUtils;
import com.jasolar.mis.framework.datapermission.core.annotation.BpmTaskTable;
import com.jasolar.mis.framework.datapermission.core.rule.BaseDataPermissionRule;
import com.jasolar.mis.framework.datapermission.core.rule.BpmTaskTableCfg;
import com.jasolar.mis.framework.datapermission.core.rule.DataPermissionRule;
import com.jasolar.mis.framework.datapermission.core.rule.DataPermissionTable;
import com.jasolar.mis.framework.datapermission.core.scope.DataScope;
import com.jasolar.mis.framework.datapermission.core.scope.ReadWrite;
import com.jasolar.mis.framework.datapermission.core.scope.ScopeLevel;
import com.jasolar.mis.framework.datapermission.core.scope.ScopeType;
import com.jasolar.mis.framework.mybatis.core.dataobject.BaseBpmTaskDO;
import com.jasolar.mis.framework.mybatis.core.dataobject.IBpmBizDO;
import com.jasolar.mis.framework.mybatis.core.util.MyBatisUtils;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.StringValue;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.expression.operators.relational.InExpression;
import net.sf.jsqlparser.expression.operators.relational.ParenthesedExpressionList;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.select.PlainSelect;

/**
 * BPM的业务流程权限。为查询添加 id IN ( SELECT biz_id FROM xxx_bpm_user WHERE user_no IN ('xxx') ) 的查询条件.
 * 此规则仅用于查询权限，不用于写权限。正常过的增删改查应该不会用到流程人员的权限。如果流程处理人需要修改流程中的数据，需要在接口中单独判断权限
 * 
 * @author galuo
 * @date 2025-03-11 16:53
 *
 */
public class BpmTaskDataPermissionRule extends BaseDataPermissionRule {

    @Override
    public ScopeType scopeType() {
        return ScopeType.USER;
    }

    @Override
    @Nonnull
    protected List<DataScope> findDataScopes(LoginUser user, Table table) {
        DataScope scope = DataScope.builder().dataIds(SetUtils.asSet(user.getNo())).type(ScopeType.USER).rw(ReadWrite.ALL)
                .level(ScopeLevel.SELF).build();

        return Arrays.asList(scope);
    }

    /**
     * 构造单个数据权限的查询SQL
     * 
     * @param table 表
     * @param columnName 列名
     * @param scope 数据权限范围
     * @return SQL查询表达式
     */
    @Nullable
    @Override
    protected Expression getExpression(DataPermissionTable dataTable, Table table, String columnName, DataScope scope) {
        if (CollectionUtils.isEmpty(scope.getDataIds())) {
            // 未指定数据ID
            return null;
        }
        Column column = MyBatisUtils.buildColumn(table.getName(), table.getAlias(), columnName);

        BpmTaskTableCfg cfg = dataTable.getBpmTaskTable();

        Table bpmTaskTable = new Table(cfg.getName());
        Column bizColumn = MyBatisUtils.buildColumn(cfg.getName(), null, cfg.getBizColumn());
        // Column userColumn = MyBatisUtils.buildColumn(cfg.getName(), null, cfg.getUserColumns()[0]);

        ExpressionList<StringValue> in = new ExpressionList<StringValue>(scope.getDataIds().stream().map(StringValue::new).toList());

        // bpm表中有2个任务处理人字段, 一个是任务所有者, 一个是被委托人(如没有委托则为空)都可以查询到自己的数据
        Expression where = DataPermissionRule.anyOf(Arrays.stream(cfg.getUserColumns()).map(userColumnName -> {
            Column userColumn = MyBatisUtils.buildColumn(cfg.getName(), null, userColumnName);
            return new InExpression(userColumn, new ParenthesedExpressionList<StringValue>(in));
        }).toList());

        if (cfg.getBizTypes() != null && cfg.getBizTypes().length > 0) {
            Column procColumn = MyBatisUtils.buildColumn(cfg.getName(), null, cfg.getBizTypeColumn());
            InExpression proc = new InExpression(procColumn, new ParenthesedExpressionList<StringValue>(
                    new ExpressionList<StringValue>(Arrays.stream(cfg.getBizTypes()).map(StringValue::new).toList())));
            where = DataPermissionRule.allOf(Arrays.asList(where, proc));
        }

        PlainSelect s = new PlainSelect(Arrays.asList(bizColumn), bpmTaskTable, where);
        return new InExpression(column, new ParenthesedExpressionList<>(new ExpressionList<>(s)));
    }

    @SuppressWarnings("unchecked")
    @Override
    public void afterPropertiesSet() throws Exception {
        // 初始化所有业务表的人员字段
        List<TableInfo> tables = TableInfoHelper.getTableInfos();

        List<TableInfo> defaultTaskTables = tables.stream().filter(t -> BaseBpmTaskDO.class.isAssignableFrom(t.getEntityType())).toList();
        BpmTaskTableCfg defaultCfg;
        if (defaultTaskTables.size() == 1) {
            defaultCfg = cfg(defaultTaskTables.get(0));
        } else {
            defaultCfg = null;
        }
        tables.stream().filter(t -> IBpmBizDO.class.isAssignableFrom(t.getEntityType())).forEach(table -> {
            Class<IBpmBizDO> clazz = (Class<IBpmBizDO>) table.getEntityType();
            DataPermissionTable t = this.addDefaultTable(clazz, IBpmBizDO::getNo);
            BpmTaskTable anno = AnnotatedElementUtils.findMergedAnnotation(clazz, BpmTaskTable.class);
            if (anno != null) {
                // 有特殊配置，主要用于一个服务下有多个bpm_task表的情况
                t.setBpmTaskTable(cfg(anno));
            } else {
                // 没有添加注解则注入默认的bpm_task表
                t.setBpmTaskTable(defaultCfg);
            }
        });
    }

    /**
     * 将表信息转换为{@link BpmTaskTableCfg}配置
     * 
     * @param ti 表信息
     * @return
     */
    @SuppressWarnings("unchecked")
    public static BpmTaskTableCfg cfg(TableInfo ti) {
        Class<? extends BaseBpmTaskDO> tableClass = (Class<? extends BaseBpmTaskDO>) ti.getEntityType();
        return BpmTaskTableCfg.builder().name(ti.getTableName())
                .userColumns(new String[] { DataPermissionTable.getColumnName(tableClass, BaseBpmTaskDO::getUserNo),
                        DataPermissionTable.getColumnName(tableClass, BaseBpmTaskDO::getDelegateeNo) })
                .bizTypeColumn(DataPermissionTable.getColumnName(tableClass, BaseBpmTaskDO::getBizType))
                .bizColumn(DataPermissionTable.getColumnName(tableClass, BaseBpmTaskDO::getBizNo)).build();
    }

    /**
     * 将注解信息转换为{@link BpmTaskTableCfg}配置
     * 
     * @param anno 注解信息
     * @return
     */
    public static BpmTaskTableCfg cfg(BpmTaskTable anno) {
        Class<? extends BaseBpmTaskDO> tableClass = anno.value();
        TableInfo ti = TableInfoHelper.getTableInfo(tableClass);
        return cfg(ti);
    }

}