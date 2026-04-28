package com.jasolar.mis.module.system.controller.oauth.vo;

import com.fasterxml.jackson.annotation.JsonAlias;

import lombok.Data;

/**
 * Description:
 * Author : Zhou Hai
 * Date : 05/09/2025 10:14
 * Version : 1.0
 */
@Data
public class LocalLoginVo {


    @JsonAlias("username")
    private String userName;

    /** 明文密码；前端部分请求体字段名为 password，与 pwd 等价 */
    @JsonAlias("password")
    private String pwd;





}
