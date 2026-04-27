package com.jasolar.mis.module.system.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Description:
 * Author : Zhou Hai
 * Date : 19/12/2025 15:37
 * Version : 1.0
 */
public interface EhrEnums {


    @Getter
    @AllArgsConstructor
    enum Level{

        FATHER("1","父级"),
        CHILD("-1","子级");

        private final String code;

        private final String desc;


        public static String getDesc(String code){
            for(Level level : values()){
                if(level.getCode().equals(code)){
                    return level.getDesc();
                }
            }
            return "";
        }
    }


    @Getter
    @AllArgsConstructor
    enum ChangeType{
        ADD("新增"),
        CHANGE("修改"),
        UN_CHANGE("不变");

        private final String desc;
    }



    @Getter
    @AllArgsConstructor
    enum CommonControlLevel{

        YES("1");

        private final String code;
    }





}
