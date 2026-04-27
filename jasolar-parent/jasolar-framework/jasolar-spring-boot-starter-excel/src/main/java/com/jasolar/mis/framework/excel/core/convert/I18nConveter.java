package com.jasolar.mis.framework.excel.core.convert;

import org.apache.commons.lang3.StringUtils;

import com.alibaba.excel.converters.Converter;
import com.alibaba.excel.enums.CellDataTypeEnum;
import com.alibaba.excel.metadata.GlobalConfiguration;
import com.alibaba.excel.metadata.data.WriteCellData;
import com.alibaba.excel.metadata.property.ExcelContentProperty;
import com.jasolar.mis.framework.i18n.I18nUtils;
import com.jasolar.mis.framework.i18n.annotation.I18nField;

/**
 * I18N转换, 暂不考虑从国际化转回来
 * 
 * @author galuo
 * @date 2025-04-01 16:14
 *
 */
public class I18nConveter implements Converter<Object> {
    @Override
    public Class<?> supportJavaTypeKey() {
        return Object.class;
    }

    @Override
    public CellDataTypeEnum supportExcelTypeKey() {
        return CellDataTypeEnum.STRING;
    }

    @Override
    public WriteCellData<?> convertToExcelData(Object value, ExcelContentProperty contentProperty, GlobalConfiguration globalConfiguration)
            throws Exception {
        I18nField i18n = contentProperty.getField().getAnnotation(I18nField.class);
        String prefix = StringUtils.EMPTY;
        if (i18n != null) {
            prefix = i18n.prefix();
        }

        return new WriteCellData<>(I18nUtils.getMessage(prefix + value));
    }

}
