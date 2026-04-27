package com.jasolar.mis.module.system.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Description:
 * Author : Zhou Hai
 * Date : 24/12/2025 17:08
 * Version : 1.0
 */
public interface SubjectEnums {

    @Getter
    @AllArgsConstructor
    enum ExcelChangeType{
        CHANGE("修改"),
        ADD("新增"),
        UN_CHANGE("不变");

        private final String desc;

    }

    @Getter
    @AllArgsConstructor
    enum ISLeaf{
        YES("TRUE"),
        NO("FALSE");

        private final String desc;
    }

}
