package com.jasolar.mis.module.system.controller.ehr.vo.converter;

import com.alibaba.excel.converters.Converter;
import com.alibaba.excel.enums.CellDataTypeEnum;
import com.alibaba.excel.metadata.GlobalConfiguration;
import com.alibaba.excel.metadata.data.ReadCellData;
import com.alibaba.excel.metadata.data.WriteCellData;
import com.alibaba.excel.metadata.property.ExcelContentProperty;

/**
 * Long ID 转 String 转换器
 * 用于确保ID在Excel中以文本格式显示，避免科学计数法导致精度丢失
 * 
 * @author system
 */
public class LongIdStringConverter implements Converter<String> {

    @Override
    public Class<?> supportJavaTypeKey() {
        return String.class;
    }

    @Override
    public CellDataTypeEnum supportExcelTypeKey() {
        return CellDataTypeEnum.STRING;
    }

    /** 零宽空格字符，用于确保Excel将ID识别为文本格式 */
    private static final String ZERO_WIDTH_SPACE = "\u200B";

    @Override
    public String convertToJavaData(ReadCellData<?> cellData, ExcelContentProperty contentProperty,
            GlobalConfiguration globalConfiguration) {
        // 读取时，去除可能的前缀字符，返回纯数字字符串
        String value = cellData.getStringValue();
        if (value == null || value.isEmpty()) {
            return null;
        }
        // 去除开头的零宽空格字符（如果存在）
        if (value.startsWith(ZERO_WIDTH_SPACE)) {
            value = value.substring(1);
        }
        return value.trim();
    }

    @Override
    public WriteCellData<?> convertToExcelData(String value, ExcelContentProperty contentProperty,
            GlobalConfiguration globalConfiguration) {
        if (value == null || value.isEmpty()) {
            return new WriteCellData<>("");
        }
        // 在ID前添加零宽空格字符，确保Excel将其识别为文本格式，避免自动转换为数字
        // 零宽空格字符完全不可见，不会影响显示和导入
        return new WriteCellData<>(ZERO_WIDTH_SPACE + value);
    }
}

