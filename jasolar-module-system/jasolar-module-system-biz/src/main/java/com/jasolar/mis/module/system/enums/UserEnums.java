package com.jasolar.mis.module.system.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Description:
 * Author : Zhou Hai
 * Date : 11/08/2025 13:57
 * Version : 1.0
 */
public interface UserEnums {


    @Getter
    @AllArgsConstructor
    enum Status{

        RUN("1","启用"),
        BAN("0","禁用");

        private final String code;

        private final String desc;

    }

    @Getter
    @AllArgsConstructor
    enum PwdChanged{

        YES("1","初始密码已修改"),
        NO("0","初始密码未修改");

        private final String code;

        private final String desc;
    }


}
