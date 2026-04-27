package com.jasolar.mis.framework.mybatis.core.mapper;

import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import com.jasolar.mis.framework.mybatis.core.query.FilterUtils;
import com.jasolar.mis.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.jasolar.mis.module.system.api.filter.FilterCondition;

import java.util.List;
import java.util.Map;

/**
 * 基础过滤 Mapper 接口，提供通用的高级筛选功能
 *
 * @author DTT
 * @since 2024-01-01
 * @param <T> 实体类型
 */
public interface BaseFilterMapper<T> extends BaseMapperX<T> {

    /**
     * 获取字段映射，用于将前端字段名映射到实体类字段
     * 子类必须实现此方法，提供具体的字段映射关系
     *
     * @return 字段映射，key为前端字段名，value为实体类字段函数
     */
    Map<String, SFunction<T, ?>> getFieldMap();

    /**
     * 应用高级筛选条件到查询包装器
     * 此方法封装了通用的筛选逻辑，子类可以直接调用
     *
     * @param queryWrapper 查询包装器，用于构建SQL查询条件
     * @param includeMap 包含条件映射，key为字段名，value为过滤条件列表
     * @param excludeMap 排除条件映射，key为字段名，value为过滤条件列表
     */
    default void applyFilter(final LambdaQueryWrapperX<T> queryWrapper,
                           final Map<String, List<FilterCondition>> includeMap,
                           final Map<String, List<FilterCondition>> excludeMap) {
        FilterUtils.apply(queryWrapper, includeMap, excludeMap, getFieldMap());
    }
} 