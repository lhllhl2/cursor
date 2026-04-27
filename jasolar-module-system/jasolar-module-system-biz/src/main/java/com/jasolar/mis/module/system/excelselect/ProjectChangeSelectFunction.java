package com.jasolar.mis.module.system.excelselect;

import com.jasolar.mis.framework.excel.core.function.ExcelColumnSelectFunction;
import com.jasolar.mis.module.system.enums.ProjectEnum;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

/**
 * Description:
 * Author : Zhou Hai
 * Date : 26/12/2025 16:23
 * Version : 1.0
 */
@Component
public class ProjectChangeSelectFunction implements ExcelColumnSelectFunction {
    @Override
    public List<String> listOptions(Object args) {
        return Arrays.stream(ProjectEnum.ExcelChangeType.values()).map(ProjectEnum.ExcelChangeType::getDesc).toList();
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
