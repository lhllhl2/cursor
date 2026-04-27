package com.jasolar.mis.module.system.excelselect;

import com.jasolar.mis.framework.excel.core.function.ExcelColumnSelectFunction;
import com.jasolar.mis.module.system.enums.EhrEnums;
import com.jasolar.mis.module.system.enums.SubjectEnums;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

/**
 * Description:
 * Author : Zhou Hai
 * Date : 19/12/2025 20:32
 * Version : 1.0
 */
@Component
public class ChangeSelectFunction implements ExcelColumnSelectFunction {

    @Override
    public List<String> listOptions(Object args) {
        return Arrays.stream(EhrEnums.ChangeType.values()).map(EhrEnums.ChangeType::getDesc).toList();

    }

    @Override
    public String format(Object value, Object args) {
        if (value == null) {
            return "";
        }
        return value.toString();
    }

    @Override
    public Object parse(String option, Object args) {
        if (option == null || option.isEmpty()) {
            return null;
        }
        return option;
    }
}
