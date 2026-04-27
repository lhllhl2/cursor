package com.jasolar.mis.framework.mybatis.core.util;

import java.util.List;
import java.util.function.Function;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jasolar.mis.framework.common.pojo.PageParam;
import com.jasolar.mis.framework.common.pojo.PageResult;

/**
 * 分页工具类,用于转换mybatis-plush的分页参数和项目中自定义的分页参数
 * 
 * @author galuo
 * @date 2025-03-28 11:23
 *
 */
public class PageUtils extends com.jasolar.mis.framework.common.util.object.PageUtils {

    /**
     * 转换项目中的分页参数为mybatis-plush的分页参数
     * 
     * @param <T>
     * @param param 项目中的分页参数类型
     * @return mybatis-plush的分页参数
     */
    public static <T> IPage<T> convert(PageParam param) {
        return new Page<>(param.getPageNo(), param.getPageSize());
    }

    /**
     * 转换mybutis-plus查询的分页结果为项目中的分页结果
     * 
     * @param <T>
     * @param result mybutis-plus查询结果
     * @return 项目中的分页结果
     */
    public static <T> PageResult<T> convert(IPage<T> result) {
        return new PageResult<>(result.getRecords(), result.getTotal());
    }

    /**
     * 转换mybutis-plus查询的分页结果为项目中的分页结果
     * 
     * @param <T> 转换前的数据类型
     * @param <R> 转换后的数据类型
     * @param result mybutis-plus查询结果
     * @return 转换为数R类型的数据分页结果
     */
    public static <T, R> PageResult<R> convert(IPage<T> result, Function<List<T>, List<R>> converter) {
        return new PageResult<>(converter.apply(result.getRecords()), result.getTotal());
    }

    /**
     * 转换查询的分页结果类型为新的类型
     * 
     * @param <T> 转换前的数据类型
     * @param <R> 转换后的数据类型
     * @param result 查询结果
     * @return 转换为数R类型的数据分页结果
     */
    public static <T, R> PageResult<R> convert(PageResult<T> result, Function<List<T>, List<R>> converter) {
        return new PageResult<>(converter.apply(result.getList()), result.getTotal());
    }

}
