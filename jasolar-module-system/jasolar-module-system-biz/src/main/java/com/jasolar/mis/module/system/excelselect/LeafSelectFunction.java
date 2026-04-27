package com.jasolar.mis.module.system.excelselect;

import com.jasolar.mis.framework.excel.core.function.ExcelColumnSelectFunction;
import com.jasolar.mis.module.system.enums.SubjectEnums;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

/**
 * Description:
 * Author : Zhou Hai
 * Date : 25/12/2025 15:52
 * Version : 1.0
 */
@Component
public class LeafSelectFunction implements ExcelColumnSelectFunction {

    @Override
    public List<String> listOptions(Object args) {
        return Arrays.stream(SubjectEnums.ISLeaf.values()).map(SubjectEnums.ISLeaf::getDesc).toList();
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
