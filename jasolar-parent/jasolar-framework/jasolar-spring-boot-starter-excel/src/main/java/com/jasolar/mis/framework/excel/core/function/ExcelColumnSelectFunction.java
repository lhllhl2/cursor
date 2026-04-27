package com.jasolar.mis.framework.excel.core.function;

import java.util.List;

/**
 * Excel 列下拉数据源获取接口
 *
 * 为什么不直接解析字典还搞个接口？考虑到有的下拉数据不是从字典中获取的所有需要做一个兼容
 * 
 * @author zhaohuang
 */
public interface ExcelColumnSelectFunction {
    /**
     * 获得列下拉数据源, 注意国际化
     * 
     * @param args 通过{@link ExcelColumnSelectFunctionArgs#setArgs(Object)}方法注入的参数
     *
     * @return 下拉数据源列表
     */
    List<String> listOptions(Object args);

    /**
     * 将数据格式化为excel中显示的值
     * 
     * @param value 数据
     * @param args 通过{@link ExcelColumnSelectFunctionArgs#setArgs(Object)}方法注入的参数
     * 
     * @return Excel中显示的值,必然属于{@link #listOptions()}中的某个值
     */
    String format(Object value, Object args);

    /**
     * 
     * 解析选择的下拉数据
     * 
     * @param option 选择的数据,必然属于{@link #listOptions()}中的某个值
     * @param args 通过{@link ExcelColumnSelectFunctionArgs#setArgs(Object)}方法注入的参数
     * 
     * @return 写入表中的代码
     */
    Object parse(String option, Object args);

}
