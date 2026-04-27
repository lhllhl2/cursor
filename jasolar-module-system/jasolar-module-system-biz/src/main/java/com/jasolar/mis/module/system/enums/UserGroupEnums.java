package com.jasolar.mis.module.system.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Description:
 * Author : Zhou Hai
 * Date : 24/07/2025 10:10
 * Version : 1.0
 */
public interface UserGroupEnums {


    @Getter
    @AllArgsConstructor
    enum Type{

        MENU("1","菜单类型"),
        REPORT("2","报表类型"),
        ORG("3","组织类型"),
        DATA("4","数据类型");

        private final String code;

        private final String desc;

    }

}
