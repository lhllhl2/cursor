package com.jasolar.mis.framework.excel.core.convert;

import org.apache.commons.lang3.StringUtils;

import com.alibaba.excel.converters.Converter;
import com.alibaba.excel.enums.CellDataTypeEnum;
import com.alibaba.excel.metadata.GlobalConfiguration;
import com.alibaba.excel.metadata.data.ReadCellData;
import com.alibaba.excel.metadata.data.WriteCellData;
import com.alibaba.excel.metadata.property.ExcelContentProperty;
import com.jasolar.mis.framework.excel.core.annotations.ExcelColumnSelect;
import com.jasolar.mis.framework.excel.core.function.ExcelColumnSelectFunction;
import com.jasolar.mis.framework.excel.core.function.ExcelColumnSelectFunctionArgs;

import cn.hutool.core.convert.Convert;
import cn.hutool.extra.spring.SpringUtil;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SelectFunctionConvert implements Converter<Object> {

    @Override
    public Class<?> supportJavaTypeKey() {
        return Object.class;
    }

    @Override
    public CellDataTypeEnum supportExcelTypeKey() {
        return CellDataTypeEnum.STRING;
    }

    /**
     * 获取Spring托管的bean对象
     * 
     * @param contentProperty
     * @return
     */
    public static ExcelColumnSelectFunction getBean(ExcelContentProperty contentProperty) {
        ExcelColumnSelect anno = contentProperty.getField().getAnnotation(ExcelColumnSelect.class);
        return getBean(anno);
    }

    /**
     * 根据注解获取Spring托管的bean对象
     * 
     * @param anno
     * @return
     */
    public static ExcelColumnSelectFunction getBean(ExcelColumnSelect anno) {
        String beanName = anno.beanName();
        if (StringUtils.isNotBlank(beanName)) {
            return SpringUtil.getBean(beanName);
        }

        Class<? extends ExcelColumnSelectFunction> beanClass = anno.beanClass();
        return SpringUtil.getBean(beanClass);
    }

    @Override
    public Object convertToJavaData(ReadCellData<?> readCellData, ExcelContentProperty contentProperty,
            GlobalConfiguration globalConfiguration) {
        ExcelColumnSelectFunction bean = getBean(contentProperty);
        String label = readCellData.getStringValue();
        if (StringUtils.isBlank(label)) {
            return null;
        }
        Object v = bean.parse(label, ExcelColumnSelectFunctionArgs.getArgs());
        if (v == null) {
            log.warn("[convertToExcelData][数据无法识别({})]", label);
            return null;
        }

        Class<?> fieldClazz = contentProperty.getField().getType();
        return Convert.convert(fieldClazz, v);
    }

    @Override
    public WriteCellData<String> convertToExcelData(Object object, ExcelContentProperty contentProperty,
            GlobalConfiguration globalConfiguration) {
        ExcelColumnSelectFunction bean = getBean(contentProperty);
        return new WriteCellData<>(bean.format(object, ExcelColumnSelectFunctionArgs.getArgs()));

    }
}
