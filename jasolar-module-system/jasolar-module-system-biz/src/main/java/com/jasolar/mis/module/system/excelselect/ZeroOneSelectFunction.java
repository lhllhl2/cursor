package com.jasolar.mis.module.system.excelselect;

import com.jasolar.mis.framework.excel.core.function.ExcelColumnSelectFunction;
import org.springframework.stereotype.Component;
import java.util.Arrays;
import java.util.List;

/**
 * 0/1下拉选择数据源
 *
 * @author your-name
 */
@Component
public class ZeroOneSelectFunction implements ExcelColumnSelectFunction {

    @Override
    public List<String> listOptions(Object args) {
        // 返回可选的值：0和1
        return List.of("1");
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