package com.jasolar.mis.framework.mybatis.core.query;

import java.util.List;
import java.util.Map;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import com.jasolar.mis.module.system.api.filter.FilterCondition;

import lombok.extern.slf4j.Slf4j;

/**
 * 高级筛选工具类，用于处理动态查询条件
 *
 * @author DTT
 * @since 2024-01-01
 */
@Slf4j
public final class FilterUtils {

    private FilterUtils() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * 应用高级筛选条件到查询包装器
     *
     * @param queryWrapper 查询包装器，用于构建SQL查询条件
     * @param includeMap 包含条件映射，key为字段名，value为过滤条件列表
     * @param excludeMap 排除条件映射，key为字段名，value为过滤条件列表
     * @param fieldMap 字段映射，key为字段名，value为实体类字段函数
     * @param <T> 实体类型
     * @param <Q> 查询包装器类型
     */
    public static <T, Q extends LambdaQueryWrapper<T>> void apply(final Q queryWrapper, final Map<String, List<FilterCondition>> includeMap,
            final Map<String, List<FilterCondition>> excludeMap, final Map<String, SFunction<T, ?>> fieldMap) {
        if (queryWrapper == null || fieldMap == null || fieldMap.isEmpty()) {
            log.warn("Invalid parameters: queryWrapper or fieldMap is null/empty");
            return;
        }

        // 处理包含条件
        applyIncludeConditions(queryWrapper, includeMap, fieldMap);

        // 处理排除条件
        applyExcludeConditions(queryWrapper, excludeMap, fieldMap);
    }

    /**
     * 处理包含条件
     *
     * @param queryWrapper 查询包装器
     * @param includeMap 包含条件映射
     * @param fieldMap 字段映射
     * @param <T> 实体类型
     * @param <Q> 查询包装器类型
     */
    private static <T, Q extends LambdaQueryWrapper<T>> void applyIncludeConditions(final Q queryWrapper,
            final Map<String, List<FilterCondition>> includeMap, final Map<String, SFunction<T, ?>> fieldMap) {
        if (includeMap == null || includeMap.isEmpty()) {
            return;
        }

        // 不同字段之间是AND关系
        includeMap.forEach((field, conditions) -> {
            if (conditions != null && !conditions.isEmpty()) {
                SFunction<T, ?> fieldFunc = fieldMap.get(field);
                if (fieldFunc != null) {
                    // 同一字段的多个条件是OR关系
                    queryWrapper.and(wrapper -> {
                        conditions.forEach(condition -> {
                            Object value = condition.getValue();
                            if (value != null) {
                                wrapper.or(w -> applyCondition(w, fieldFunc, condition, false));
                            }
                        });
                    });
                }
            }
        });
    }

    /**
     * 处理排除条件
     *
     * @param queryWrapper 查询包装器
     * @param excludeMap 排除条件映射
     * @param fieldMap 字段映射
     * @param <T> 实体类型
     * @param <Q> 查询包装器类型
     */
    private static <T, Q extends LambdaQueryWrapper<T>> void applyExcludeConditions(final Q queryWrapper,
            final Map<String, List<FilterCondition>> excludeMap, final Map<String, SFunction<T, ?>> fieldMap) {
        if (excludeMap == null || excludeMap.isEmpty()) {
            return;
        }

        // 不同字段之间是AND关系
        excludeMap.forEach((field, conditions) -> {
            if (conditions != null && !conditions.isEmpty()) {
                SFunction<T, ?> fieldFunc = fieldMap.get(field);
                if (fieldFunc != null) {
                    // 同一字段的多个条件是OR关系
                    queryWrapper.and(wrapper -> {
                        conditions.forEach(condition -> {
                            Object value = condition.getValue();
                            if (value != null) {
                                wrapper.or(w -> applyCondition(w, fieldFunc, condition, true));
                            }
                        });
                    });
                }
            }
        });
    }

    /**
     * 应用具体的过滤条件
     *
     * @param wrapper 查询包装器
     * @param fieldFunc 字段函数
     * @param conditions 条件列表
     * @param isExclude 是否为排除条件
     * @param <T> 实体类型
     */
    public static <T> void applyConditions(final LambdaQueryWrapper<T> wrapper, final SFunction<T, ?> fieldFunc,
            final List<FilterCondition> conditions, final boolean isExclude) {
        conditions.forEach(condition -> {
            Object value = condition.getValue();
            if (value != null) {
                wrapper.or(w -> applyCondition(w, fieldFunc, condition, isExclude));
            }
        });
    }

    /**
     * 应用单个过滤条件
     *
     * @param wrapper 查询包装器
     * @param fieldFunc 字段函数
     * @param condition 过滤条件
     * @param isExclude 是否为排除条件
     * @param <T> 实体类型
     */
    private static <T> void applyCondition(final LambdaQueryWrapper<T> wrapper, final SFunction<T, ?> fieldFunc,
            final FilterCondition condition, final boolean isExclude) {
        String operator = condition.getOperator();
        Object value = condition.getValue();

        switch (operator) {
        case "eq" -> applyEqualCondition(wrapper, fieldFunc, value, isExclude);
        case "neq" -> applyNotEqualCondition(wrapper, fieldFunc, value, isExclude);
        case "gt" -> applyGreaterThanCondition(wrapper, fieldFunc, value, isExclude);
        case "gte" -> applyGreaterThanEqualCondition(wrapper, fieldFunc, value, isExclude);
        case "lt" -> applyLessThanCondition(wrapper, fieldFunc, value, isExclude);
        case "lte" -> applyLessThanEqualCondition(wrapper, fieldFunc, value, isExclude);
        case "like" -> applyLikeCondition(wrapper, fieldFunc, value, isExclude);
        case "range" -> applyRangeCondition(wrapper, fieldFunc, value, isExclude);
        default -> log.warn("Unsupported operator: {}", operator);
        }
    }

    /**
     * 应用等于条件
     */
    private static <T> void applyEqualCondition(final LambdaQueryWrapper<T> wrapper, final SFunction<T, ?> fieldFunc, final Object value,
            final boolean isExclude) {
        if (isExclude) {
            wrapper.ne(fieldFunc, value);
        } else {
            wrapper.eq(fieldFunc, value);
        }
    }

    /**
     * 应用不等于条件
     */
    private static <T> void applyNotEqualCondition(final LambdaQueryWrapper<T> wrapper, final SFunction<T, ?> fieldFunc, final Object value,
            final boolean isExclude) {
        if (isExclude) {
            wrapper.eq(fieldFunc, value);
        } else {
            wrapper.ne(fieldFunc, value);
        }
    }

    /**
     * 应用大于条件
     */
    private static <T> void applyGreaterThanCondition(final LambdaQueryWrapper<T> wrapper, final SFunction<T, ?> fieldFunc,
            final Object value, final boolean isExclude) {
        if (isExclude) {
            wrapper.le(fieldFunc, value);
        } else {
            wrapper.gt(fieldFunc, value);
        }
    }

    /**
     * 应用大于等于条件
     */
    private static <T> void applyGreaterThanEqualCondition(final LambdaQueryWrapper<T> wrapper, final SFunction<T, ?> fieldFunc,
            final Object value, final boolean isExclude) {
        if (isExclude) {
            wrapper.lt(fieldFunc, value);
        } else {
            wrapper.ge(fieldFunc, value);
        }
    }

    /**
     * 应用小于条件
     */
    private static <T> void applyLessThanCondition(final LambdaQueryWrapper<T> wrapper, final SFunction<T, ?> fieldFunc, final Object value,
            final boolean isExclude) {
        if (isExclude) {
            wrapper.ge(fieldFunc, value);
        } else {
            wrapper.lt(fieldFunc, value);
        }
    }

    /**
     * 应用小于等于条件
     */
    private static <T> void applyLessThanEqualCondition(final LambdaQueryWrapper<T> wrapper, final SFunction<T, ?> fieldFunc,
            final Object value, final boolean isExclude) {
        if (isExclude) {
            wrapper.gt(fieldFunc, value);
        } else {
            wrapper.le(fieldFunc, value);
        }
    }

    /**
     * 应用模糊匹配条件
     */
    private static <T> void applyLikeCondition(final LambdaQueryWrapper<T> wrapper, final SFunction<T, ?> fieldFunc, final Object value,
            final boolean isExclude) {
        if (isExclude) {
            wrapper.notLike(fieldFunc, value);
        } else {
            wrapper.like(fieldFunc, value);
        }
    }

    /**
     * 应用范围条件
     */
    private static <T> void applyRangeCondition(final LambdaQueryWrapper<T> wrapper, final SFunction<T, ?> fieldFunc, final Object value,
            final boolean isExclude) {
        if (!(value instanceof List<?> list) || list.size() != 2) {
            log.warn("Invalid range value: {}", value);
            return;
        }

        if (isExclude) {
            wrapper.notBetween(fieldFunc, list.get(0), list.get(1));
        } else {
            wrapper.between(fieldFunc, list.get(0), list.get(1));
        }
    }
}