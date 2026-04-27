package com.jasolar.mis.framework.datapermission.core.rule;

import java.util.Arrays;
import java.util.List;

import com.jasolar.mis.framework.common.enums.UserTypeEnum;
import com.jasolar.mis.framework.datapermission.core.annotation.DataRule;
import com.jasolar.mis.framework.datapermission.core.scope.ReadWrite;

import jakarta.annotation.Nullable;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.conditional.OrExpression;
import net.sf.jsqlparser.expression.operators.relational.EqualsTo;
import net.sf.jsqlparser.expression.operators.relational.ParenthesedExpressionList;
import net.sf.jsqlparser.schema.Table;

/**
 * 单个维度的数据权限规则接口. 每个维度都必须定义一个权限规则
 * 通过实现接口，自定义数据规则。例如说，
 *
 * @author zhaohuang
 */
public interface DataPermissionRule {

    /** 如果某个权限维度配置了所有权限,则返回此表达式,忽略所有其他维度 */
    EqualsTo ALL = new EqualsTo(new LongValue(1), new LongValue(1));

    /** 没有任何匹配的权限 */
    EqualsTo NONE = new EqualsTo(new LongValue(1), new LongValue(0));

    /**
     * 
     * 设置读写分离权限
     * 
     * @param rw
     */
    default void setReadWrite(ReadWrite rw) {
    }

    /**
     * 是否可以处理某个表， 注意表名最好不要区分大小写
     * 
     * @param tableName 从SQL中解析的原始表名
     * @return 返回true则表示要处理这个表，否则返回false
     */
    boolean accept(Table table);

    /**
     * 是否可以处理某种登录人员
     * 
     * @param userType 人员类型
     * @return
     */
    default boolean accept(UserTypeEnum userType) {
        return UserTypeEnum.ADMIN == userType;
    }

    /**
     * 生成表对应的过滤条件。注意：调用此方法前必须通过{@link #accept(Table)}方法判断是否可以处理{@code table}
     *
     * @param table 表对象，必须是通过{@link #accept(Table)}方法验证过的
     * @return 过滤条件 Expression 表达式
     */
    @Nullable
    Expression getExpression(Table table);

    /**
     * 根据指定的注解参数复制一个数据权限规则.
     *
     * @param rule 注解{@link DataRule}参数
     * @return 新的数据权限规则
     */
    DataPermissionRule createRule(DataRule rule);

    // /**
    // * 根据指定的注解参数复制一个数据权限规则.
    // *
    // * @param rule 注解{@link DataPermission}参数
    // * @return 新的数据权限规则
    // */
    // DataPermissionRule createRule(DataPermission dp);

    /**
     * 复制规则
     * 
     * @return
     */
    default DataPermissionRule copy() {
        return null;
    }

    /**
     * 所有条件之间AND
     * 
     * @param expressions 要AND的条件列表
     * @return 新的条件
     */
    static Expression allOf(List<? extends Expression> expressions) {
        if (expressions == null || expressions.isEmpty()) {
            return null;
        }

        Expression injectExpression = expressions.get(0);
        if (expressions.size() > 1) {
            for (int i = 1; i < expressions.size(); i++) {
                injectExpression = new AndExpression(injectExpression, expressions.get(i));
            }
        }

        return injectExpression;
    }

    /**
     * 所有条件之间AND
     * 
     * @param expressions 要AND的条件列表
     * @return 新的条件
     */
    static Expression allOf(Expression... expressions) {
        if (expressions == null || expressions.length < 1) {
            return null;
        }
        if (expressions.length == 1) {
            return expressions[0];
        }

        return allOf(Arrays.asList(expressions));
    }

    /**
     * 所有条件之间OR
     * 
     * 
     * @param expressions 要OR的条件列表
     * @return 新的条件
     */
    static Expression anyOf(List<? extends Expression> expressions) {
        if (expressions == null || expressions.isEmpty()) {
            return null;
        }
        Expression injectExpression = expressions.get(0);
        if (expressions.size() > 1) {
            for (int i = 1; i < expressions.size(); i++) {
                injectExpression = new OrExpression(injectExpression, expressions.get(i));
            }
        }

        // OR的所有条件用括号包起来
        return new ParenthesedExpressionList<Expression>(injectExpression);
    }

    /**
     * 所有条件之间OR
     * 
     * 
     * @param expressions 要OR的条件列表
     * @return 新的条件
     */
    static Expression anyOf(Expression... expressions) {
        if (expressions == null || expressions.length < 1) {
            return null;
        }
        if (expressions.length == 1) {
            return expressions[0];
        }

        return anyOf(Arrays.asList(expressions));
    }

}
