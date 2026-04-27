package com.jasolar.mis.framework.mybatis.core.query;

import org.springframework.util.StringUtils;

import com.baomidou.mybatisplus.core.enums.SqlLike;
import com.jasolar.mis.framework.mybatis.core.enums.ExtSqlKeyword;

/**
 * 扩展的wrapper, 可以使用{@link ExtSqlKeyword}定义的关键字
 * 
 * @author galuo
 * @date 2025-04-09 10:27
 *
 * @param <T> 查询对象类型
 * @param <Column> 列类型
 * @param <Child> 具体实现类
 */
public interface ExtWrapper<T, Column, Child extends ExtWrapper<T, Column, ?>> {

    /**
     * 使用ILIKE忽略大小写查询
     * 
     * @param condition
     * @param keyword
     * @param column
     * @param val
     * @param sqlLike
     * @return this
     */
    Child ilikeValue(ExtSqlKeyword keyword, Column column, Object val, SqlLike sqlLike);

    /**
     * 使用ILIKE忽略大小写模糊查询
     * 
     * @param column 查询的列
     * @param val 查询的值, 会自动添加%, 如果为空则不会添加任何查询条件
     * @return this
     */
    @SuppressWarnings("unchecked")
    default Child ilikeIfPresent(Column column, String val) {
        if (StringUtils.hasText(val)) {
            return ilikeValue(ExtSqlKeyword.ILIKE, column, val, SqlLike.DEFAULT);
        }
        return (Child) this;
    }

    /**
     * 使用ILIKE忽略大小写进行左匹配查询
     * 
     * @param column 查询的列
     * @param val 查询的值, 会自动添加%, 如果为空则不会添加任何查询条件
     * @return this
     */
    @SuppressWarnings("unchecked")
    default Child ilikeLeftIfPresent(Column column, String val) {
        if (StringUtils.hasText(val)) {
            return ilikeValue(ExtSqlKeyword.ILIKE, column, val, SqlLike.LEFT);
        }
        return (Child) this;
    }

    /**
     * 使用ILIKE忽略大小写进行右匹配查询
     * 
     * @param column 查询的列
     * @param val 查询的值, 会自动添加%, 如果为空则不会添加任何查询条件
     * @return this
     */
    @SuppressWarnings("unchecked")
    default Child ilikeRightIfPresent(Column column, String val) {
        if (StringUtils.hasText(val)) {
            return ilikeValue(ExtSqlKeyword.ILIKE, column, val, SqlLike.RIGHT);
        }
        return (Child) this;
    }
}
