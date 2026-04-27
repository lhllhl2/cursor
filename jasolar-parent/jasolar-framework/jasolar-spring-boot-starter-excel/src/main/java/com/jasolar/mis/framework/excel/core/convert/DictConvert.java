package com.jasolar.mis.framework.excel.core.convert;

import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.alibaba.excel.converters.Converter;
import com.alibaba.excel.enums.CellDataTypeEnum;
import com.alibaba.excel.metadata.GlobalConfiguration;
import com.alibaba.excel.metadata.data.ReadCellData;
import com.alibaba.excel.metadata.data.WriteCellData;
import com.alibaba.excel.metadata.property.ExcelContentProperty;
import com.jasolar.mis.framework.common.exception.util.ServiceExceptionUtil;
import com.jasolar.mis.framework.data.core.DictData;
import com.jasolar.mis.framework.data.util.DictUtils;
import com.jasolar.mis.framework.excel.core.annotations.DictFormat;
import com.jasolar.mis.framework.excel.core.util.ErrorCodes;

import cn.hutool.core.convert.Convert;
import lombok.extern.slf4j.Slf4j;

/**
 * Excel 数据字典转换器
 *
 * @author zhaohuang
 */
@Slf4j
public class DictConvert implements Converter<Object> {

    @Override
    public Class<?> supportJavaTypeKey() {
        // 声明该转换器支持的所有 Java 类型
        // 这里以常见的 String、Integer、Long 为例，可以根据实际需求扩展
        return Object.class; // 表示支持任意类型的对象
    }

    @Override
    public CellDataTypeEnum supportExcelTypeKey() {
        // 声明该转换器支持的 Excel 单元格数据类型
        // 这里以字符串类型为例，因为字典值通常以字符串形式存储
        return CellDataTypeEnum.STRING;
    }

    @Override
    public Object convertToJavaData(ReadCellData<?> readCellData, ExcelContentProperty contentProperty,
            GlobalConfiguration globalConfiguration) {
        // 使用字典解析
        String type = getDictType(contentProperty);
        String label = readCellData.getStringValue();

        String v = parse(label);

        List<DictData> list = DictUtils.listData(type, a -> v.equalsIgnoreCase(a.getValue()));
        String value = list.isEmpty() ? null : list.get(0).getValue();
        if (value == null) {
            log.error("[convertToJavaData][type({}) 无法解析 label({})]", type, label);
            return null;
        }
        // 将 String 的 value 转换成对应的属性
        Class<?> fieldClazz = contentProperty.getField().getType();
        return Convert.convert(fieldClazz, value);
    }

    @Override
    public WriteCellData<String> convertToExcelData(Object object, ExcelContentProperty contentProperty,
            GlobalConfiguration globalConfiguration) {
        // 使用字典格式化
        String type = getDictType(contentProperty);
        String value = String.valueOf(object);

        DictData dict = DictUtils.getData(type, value);
        if (dict == null) {
            // 字典不存在的数据直接返回值
            return new WriteCellData<>(value);
        }
        // 字典的国际化
        String label = format(dict); // .getI18nLabel();
        if (label == null) {
            log.error("[convertToExcelData][type({}) 字典无法识别 label({})]", type, value);
            return new WriteCellData<>(StringUtils.EMPTY);
        }

        // 生成 Excel 小表格
        return new WriteCellData<>(label);
    }

    private static String getDictType(ExcelContentProperty contentProperty) {
        return contentProperty.getField().getAnnotation(DictFormat.class).value();
    }

    /** 下拉框字典的显示格式 */
    static final String FORMAT = "%s(%s)";

    /**
     * 格式化字典为下拉框的字符串, 格式为: label(value)
     * 
     * @param data 字典
     * @return 下拉框显示的字符串, 格式为: label(value)
     */
    public static String format(DictData data) {
        return String.format(FORMAT, data.getI18nLabel(), data.getValue());
    }

    /**
     * 格式化字典为下拉框的字符串, 格式为: label(value)
     * 
     * @param data 字典
     * @return 下拉框显示的字符串, 格式为: label(value)
     */
    public static String parse(String txt) {
        int x1 = txt.lastIndexOf('(');
        int x2 = txt.lastIndexOf(')');
        if (x1 < 0 || x2 < 0 || x1 > x2) {
            // 无法解析
            throw ServiceExceptionUtil.exception(ErrorCodes.DICT_PARSE_FORMAT, (Object) txt);
        }
        return txt.substring(x1 + 1, x2);
    }
}
