package com.jasolar.mis.module.system.excelselect;

import com.jasolar.mis.framework.excel.core.function.ExcelColumnSelectFunction;
import com.jasolar.mis.module.system.enums.ProjectEnum;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

/**
 * Description:
 * Author : Zhou Hai
 * Date : 04/01/2026 17:37
 * Version : 1.0
 */
@Component
public class ProjectAuthFunction  implements ExcelColumnSelectFunction {

    @Override
    public List<String> listOptions(Object args) {
        return Arrays.stream(ProjectEnum.AuthType.values()).map(ProjectEnum.AuthType::getDesc).toList();
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
