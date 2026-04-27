package com.jasolar.mis.module.system.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Description:
 * Author : Zhou Hai
 * Date : 26/12/2025 16:24
 * Version : 1.0
 */
public interface ProjectEnum {


    @Getter
    @AllArgsConstructor
    enum ExcelChangeType{
        CHANGE("修改"),
        UN_CHANGE("不变");

        private final String desc;

    }


    @Getter
    @AllArgsConstructor
    enum AuthType{
        ADD("新增"),
        UN_CHANGE("不变");

        private final String desc;
    }

}
