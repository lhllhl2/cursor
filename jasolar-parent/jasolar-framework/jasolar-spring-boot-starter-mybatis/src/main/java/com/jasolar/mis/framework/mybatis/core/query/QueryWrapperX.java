package com.jasolar.mis.framework.mybatis.core.query;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.springframework.util.StringUtils;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.enums.SqlLike;
import com.baomidou.mybatisplus.core.toolkit.ArrayUtils;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.sql.SqlUtils;
import com.jasolar.mis.framework.mybatis.core.enums.ExtSqlKeyword;
import com.jasolar.mis.framework.mybatis.core.util.JdbcUtils;
import com.jasolar.mis.module.system.api.filter.FilterCondition;
import com.jasolar.mis.module.system.api.filter.FilterReqVO;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;

/**
 * 拓展 MyBatis Plus QueryWrapper 类，主要增加如下功能：
 * 1. 拼接条件的方法，增加 xxxIfPresent 方法，用于判断值不存在的时候，不要拼接到条件中。
 * 2. 新增了高级筛选条件功能，支持多条件的组合查询
 *
 * @param <T> 数据类型
 * @author DTT
 */
@SuppressWarnings("serial")
public class QueryWrapperX<T> extends QueryWrapper<T> implements ExtWrapper<T, String, QueryWrapperX<T>> {

    @Override
    public QueryWrapperX<T> ilikeValue(ExtSqlKeyword keyword, String column, Object val, SqlLike sqlLike) {
        return (QueryWrapperX<T>) maybeDo(true,
                () -> appendSqlSegments(columnToSqlSegment(column), keyword, () -> formatParam(null, SqlUtils.concatLike(val, sqlLike))));
    }

    public QueryWrapperX<T> likeIfPresent(String column, String val) {
        if (StringUtils.hasText(val)) {
            return (QueryWrapperX<T>) super.like(column, val);
        }
        return this;
    }

    public QueryWrapperX<T> inIfPresent(String column, Collection<?> values) {
        if (!CollectionUtils.isEmpty(values)) {
            return (QueryWrapperX<T>) super.in(column, values);
        }
        return this;
    }

    public QueryWrapperX<T> inIfPresent(String column, Object... values) {
        if (!ArrayUtils.isEmpty(values)) {
            return (QueryWrapperX<T>) super.in(column, values);
        }
        return this;
    }

    public QueryWrapperX<T> eqIfPresent(String column, Object val) {
        if (val != null) {
            return (QueryWrapperX<T>) super.eq(column, val);
        }
        return this;
    }

    public QueryWrapperX<T> neIfPresent(String column, Object val) {
        if (val != null) {
            return (QueryWrapperX<T>) super.ne(column, val);
        }
        return this;
    }

    public QueryWrapperX<T> gtIfPresent(String column, Object val) {
        if (val != null) {
            return (QueryWrapperX<T>) super.gt(column, val);
        }
        return this;
    }

    public QueryWrapperX<T> geIfPresent(String column, Object val) {
        if (val != null) {
            return (QueryWrapperX<T>) super.ge(column, val);
        }
        return this;
    }

    public QueryWrapperX<T> ltIfPresent(String column, Object val) {
        if (val != null) {
            return (QueryWrapperX<T>) super.lt(column, val);
        }
        return this;
    }

    public QueryWrapperX<T> leIfPresent(String column, Object val) {
        if (val != null) {
            return (QueryWrapperX<T>) super.le(column, val);
        }
        return this;
    }

    public QueryWrapperX<T> betweenIfPresent(String column, Object val1, Object val2) {
        if (val1 != null && val2 != null) {
            return (QueryWrapperX<T>) super.between(column, val1, val2);
        }
        if (val1 != null) {
            return (QueryWrapperX<T>) ge(column, val1);
        }
        if (val2 != null) {
            return (QueryWrapperX<T>) le(column, val2);
        }
        return this;
    }

    public QueryWrapperX<T> betweenIfPresent(String column, Object[] values) {
        if (values != null && values.length != 0 && values[0] != null && values[1] != null) {
            return (QueryWrapperX<T>) super.between(column, values[0], values[1]);
        }
        if (values != null && values.length != 0 && values[0] != null) {
            return (QueryWrapperX<T>) ge(column, values[0]);
        }
        if (values != null && values.length != 0 && values[1] != null) {
            return (QueryWrapperX<T>) le(column, values[1]);
        }
        return this;
    }

    // ========== 重写父类方法，方便链式调用 ==========

    @Override
    public QueryWrapperX<T> eq(boolean condition, String column, Object val) {
        super.eq(condition, column, val);
        return this;
    }

    @Override
    public QueryWrapperX<T> eq(String column, Object val) {
        super.eq(column, val);
        return this;
    }

    @Override
    public QueryWrapperX<T> orderByDesc(String column) {
        super.orderByDesc(true, column);
        return this;
    }

    @Override
    public QueryWrapperX<T> last(String lastSql) {
        super.last(lastSql);
        return this;
    }

    @Override
    public QueryWrapperX<T> in(String column, Collection<?> coll) {
        super.in(column, coll);
        return this;
    }

    /**
     * 设置只返回最后一条
     * <p>
     *
     * @return this
     */
    public QueryWrapperX<T> limitN(int n) {
        DbType dbType = JdbcUtils.getDbType();
        switch (dbType) {
        case ORACLE_12C, ORACLE:
            super.le("ROWNUM", n);
            break;
        case SQL_SERVER2005, SQL_SERVER:
            super.select("TOP " + n + " *"); // 由于 SQL Server 是通过 SELECT TOP 1 实现限制一条，所以只好使用 * 查询剩余字段
            break;
        default: // MySQL、PostgreSQL、DM 达梦、KingbaseES 大金都是采用 LIMIT 实现
            super.last("LIMIT " + n);
        }
        return this;
    }

    /**
     * 应用高级筛选条件
     *
     * @param filterReqVO 筛选条件
     * @return this
     */
    public QueryWrapperX<T> applyFilter(FilterReqVO filterReqVO) {
        if (filterReqVO == null) {
            return this;
        }

        // 处理包含条件
        Map<String, List<FilterCondition>> includeMap = filterReqVO.getIncludeMap();
        if (CollUtil.isNotEmpty(includeMap)) {
            includeMap.forEach((field, conditions) -> {
                if (CollUtil.isNotEmpty(conditions)) {
                    and(wrapper -> conditions.forEach(condition -> applyCondition((QueryWrapperX<T>) wrapper, field, condition, true)));
                }
            });
        }

        // 处理排除条件
        Map<String, List<FilterCondition>> excludeMap = filterReqVO.getExcludeMap();
        if (CollUtil.isNotEmpty(excludeMap)) {
            excludeMap.forEach((field, conditions) -> {
                if (CollUtil.isNotEmpty(conditions)) {
                    and(wrapper -> conditions.forEach(condition -> applyCondition((QueryWrapperX<T>) wrapper, field, condition, false)));
                }
            });
        }

        return this;
    }

    /**
     * 应用单个筛选条件
     *
     * @param wrapper QueryWrapper对象
     * @param field 字段名
     * @param condition 筛选条件
     * @param isInclude 是否为包含条件
     */
    private void applyCondition(QueryWrapperX<T> wrapper, String field, FilterCondition condition, boolean isInclude) {
        if (condition == null || CharSequenceUtil.isEmpty(condition.getOperator())) {
            return;
        }

        String operator = condition.getOperator();
        Object value = condition.getValue();

        switch (operator) {
        case "eq" -> wrapper.eq(isInclude, field, value);
        case "neq" -> wrapper.ne(isInclude, field, value);
        case "gt" -> wrapper.gt(isInclude, field, value);
        case "gte" -> wrapper.ge(isInclude, field, value);
        case "lt" -> wrapper.lt(isInclude, field, value);
        case "lte" -> wrapper.le(isInclude, field, value);
        case "range" -> {
            if (value instanceof List<?> list && list.size() == 2) {
                if (isInclude) {
                    wrapper.between(field, list.get(0), list.get(1));
                } else {
                    wrapper.notBetween(field, list.get(0), list.get(1));
                }
            }
        }
        case "like" -> wrapper.like(isInclude, field, value);
        default -> {
            // 不支持的操作符，忽略处理
        }
        }
    }

}
