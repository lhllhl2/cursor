package com.jasolar.mis.framework.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Description:
 * Author : Zhou Hai
 * Date : 24/07/2025 9:15
 * Version : 1.0
 */


public interface CommonEnum {

    @Getter
    @AllArgsConstructor
    enum Deleted{

        YES(1,"删除"),
        NO(0,"不删除");

        /**
         * 状态值
         */
        private final Integer status;
        /**
         * 状态名
         */
        private final String name;

    }

}
