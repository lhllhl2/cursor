package com.jasolar.mis.module.system.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Description:
 * Author : Zhou Hai
 * Date : 11/08/2025 15:52
 * Version : 1.0
 */
public interface UserOrgREnums {

    @Getter
    @AllArgsConstructor
    enum MainOu{

        YES("1","主要组织"),
        NO("0","非主要组织");

        private final String code;

        private final String desc;

    }

}
