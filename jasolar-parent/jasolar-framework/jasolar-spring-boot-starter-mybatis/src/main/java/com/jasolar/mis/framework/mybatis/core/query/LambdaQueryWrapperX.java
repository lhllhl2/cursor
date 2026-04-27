package com.jasolar.mis.framework.mybatis.core.query;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import org.springframework.util.StringUtils;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.enums.SqlLike;
import com.baomidou.mybatisplus.core.toolkit.sql.SqlUtils;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import com.jasolar.mis.framework.common.util.collection.ArrayUtils;
import com.jasolar.mis.framework.mybatis.core.enums.ExtSqlKeyword;
import com.jasolar.mis.module.system.api.filter.FilterCondition;
import com.jasolar.mis.module.system.api.filter.FilterReqVO;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.ObjectUtil;

/**
 * 拓展 MyBatis Plus QueryWrapper 类，主要增加如下功能：
 * <p>
 * 1. 拼接条件的方法，增加 xxxIfPresent 方法，用于判断值不存在的时候，不要拼接到条件中。
 * 2. 新增了高级筛选条件功能，支持多条件的组合查询
 *
 * @param <T> 数据类型
 * @author DTT
 */
@SuppressWarnings("serial")
public class LambdaQueryWrapperX<T> extends LambdaQueryWrapper<T> implements ExtWrapper<T, SFunction<T, ?>, LambdaQueryWrapperX<T>> {

    public LambdaQueryWrapperX() {
        super();
    }

    public LambdaQueryWrapperX(Class<T> entityClass) {
        super(entityClass);
    }

    public LambdaQueryWrapperX(T entity) {
        super(entity);
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public LambdaQueryWrapperX<T> or() {
        return (LambdaQueryWrapperX) super.or();
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public LambdaQueryWrapperX<T> or(boolean condition, Consumer<LambdaQueryWrapper<T>> consumer) {
        // TODO Auto-generated method stub
        return (LambdaQueryWrapperX) super.or(condition, consumer);
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public LambdaQueryWrapperX<T> or(boolean condition) {
        // TODO Auto-generated method stub
        return (LambdaQueryWrapperX) super.or(condition);
    }

    @Override
    public LambdaQueryWrapperX<T> ilikeValue(ExtSqlKeyword keyword, SFunction<T, ?> column, Object val, SqlLike sqlLike) {
        maybeDo(true,
                () -> appendSqlSegments(columnToSqlSegment(column), keyword, () -> formatParam(null, SqlUtils.concatLike(val, sqlLike))));
        return this;
    }

    public LambdaQueryWrapperX<T> likeIfPresent(SFunction<T, ?> column, String val) {
        if (StringUtils.hasText(val)) {
            return (LambdaQueryWrapperX<T>) super.like(column, val);
        }
        return this;
    }

    public LambdaQueryWrapperX<T> inIfPresent(SFunction<T, ?> column, Collection<?> values) {
        if (ObjectUtil.isAllNotEmpty(values) && !ArrayUtil.isEmpty(values)) {
            return (LambdaQueryWrapperX<T>) super.in(column, values);
        }
        return this;
    }

    public LambdaQueryWrapperX<T> inIfPresent(SFunction<T, ?> column, Object... values) {
        if (ObjectUtil.isAllNotEmpty(values) && !ArrayUtil.isEmpty(values)) {
            return (LambdaQueryWrapperX<T>) super.in(column, values);
        }
        return this;
    }

    public LambdaQueryWrapperX<T> eqIfPresent(SFunction<T, ?> column, Object val) {
        if (ObjectUtil.isNotEmpty(val)) {
            return (LambdaQueryWrapperX<T>) super.eq(column, val);
        }
        return this;
    }

    public LambdaQueryWrapperX<T> neIfPresent(SFunction<T, ?> column, Object val) {
        if (ObjectUtil.isNotEmpty(val)) {
            return (LambdaQueryWrapperX<T>) super.ne(column, val);
        }
        return this;
    }

    public LambdaQueryWrapperX<T> gtIfPresent(SFunction<T, ?> column, Object val) {
        if (val != null) {
            return (LambdaQueryWrapperX<T>) super.gt(column, val);
        }
        return this;
    }

    public LambdaQueryWrapperX<T> geIfPresent(SFunction<T, ?> column, Object val) {
        if (val != null) {
            return (LambdaQueryWrapperX<T>) super.ge(column, val);
        }
        return this;
    }

    public LambdaQueryWrapperX<T> ltIfPresent(SFunction<T, ?> column, Object val) {
        if (val != null) {
            return (LambdaQueryWrapperX<T>) super.lt(column, val);
        }
        return this;
    }

    public LambdaQueryWrapperX<T> leIfPresent(SFunction<T, ?> column, Object val) {
        if (val != null) {
            return (LambdaQueryWrapperX<T>) super.le(column, val);
        }
        return this;
    }

    public LambdaQueryWrapperX<T> betweenIfPresent(SFunction<T, ?> column, Object val1, Object val2) {
        if (val1 != null && val2 != null) {
            return (LambdaQueryWrapperX<T>) super.between(column, val1, val2);
        }
        if (val1 != null) {
            return (LambdaQueryWrapperX<T>) ge(column, val1);
        }
        if (val2 != null) {
            return (LambdaQueryWrapperX<T>) le(column, val2);
        }
        return this;
    }

    public LambdaQueryWrapperX<T> betweenIfPresent(SFunction<T, ?> column, Object[] values) {
        Object val1 = ArrayUtils.get(values, 0);
        Object val2 = ArrayUtils.get(values, 1);
        return betweenIfPresent(column, val1, val2);
    }

    // ========== 重写父类方法，方便链式调用 ==========

    @Override
    public LambdaQueryWrapperX<T> eq(boolean condition, SFunction<T, ?> column, Object val) {
        super.eq(condition, column, val);
        return this;
    }

    @Override
    public LambdaQueryWrapperX<T> eq(SFunction<T, ?> column, Object val) {
        super.eq(column, val);
        return this;
    }

    @Override
    public LambdaQueryWrapperX<T> orderByDesc(SFunction<T, ?> column) {
        super.orderByDesc(true, column);
        return this;
    }

    @Override
    public LambdaQueryWrapperX<T> last(String lastSql) {
        super.last(lastSql);
        return this;
    }

    @Override
    public LambdaQueryWrapperX<T> in(SFunction<T, ?> column, Collection<?> coll) {
        super.in(column, coll);
        return this;
    }

    /**
     * 应用高级筛选条件
     *
     * @param fieldMap 字段映射，key为字段名，value为对应的Lambda表达式
     * @param filterReqVO 筛选条件
     * @return this
     */
    public LambdaQueryWrapperX<T> applyFilter(Map<String, SFunction<T, ?>> fieldMap, FilterReqVO filterReqVO) {
        if (fieldMap == null || filterReqVO == null) {
            return this;
        }

        // 处理包含条件
        Map<String, List<FilterCondition>> includeMap = filterReqVO.getIncludeMap();
        if (CollUtil.isNotEmpty(includeMap)) {
            includeMap.forEach((fieldName, conditions) -> {
                SFunction<T, ?> field = fieldMap.get(fieldName);
                if (field != null && CollUtil.isNotEmpty(conditions)) {
                    and(wrapper -> conditions
                            .forEach(condition -> applyCondition((LambdaQueryWrapperX<T>) wrapper, field, condition, true)));
                }
            });
        }

        // 处理排除条件
        Map<String, List<FilterCondition>> excludeMap = filterReqVO.getExcludeMap();
        if (CollUtil.isNotEmpty(excludeMap)) {
            excludeMap.forEach((fieldName, conditions) -> {
                SFunction<T, ?> field = fieldMap.get(fieldName);
                if (field != null && CollUtil.isNotEmpty(conditions)) {
                    and(wrapper -> conditions
                            .forEach(condition -> applyCondition((LambdaQueryWrapperX<T>) wrapper, field, condition, false)));
                }
            });
        }

        return this;
    }

    /**
     * 应用单个筛选条件
     *
     * @param wrapper QueryWrapper对象
     * @param field 字段
     * @param condition 筛选条件
     * @param isInclude 是否为包含条件
     */
    private void applyCondition(LambdaQueryWrapperX<T> wrapper, SFunction<T, ?> field, FilterCondition condition, boolean isInclude) {
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
