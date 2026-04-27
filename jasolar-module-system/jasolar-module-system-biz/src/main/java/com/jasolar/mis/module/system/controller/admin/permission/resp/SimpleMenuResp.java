package com.jasolar.mis.module.system.controller.admin.permission.resp;

import lombok.Data;

import java.util.List;

/**
 * Description:
 * Author : Zhou Hai
 * Date : 01/08/2025 16:19
 * Version : 1.0
 */
@Data
public class SimpleMenuResp {

    private Long id;

    private Long pid;

    private String name;

    private String type;

    private String path;

    private String pathCode;

    private String component;


    private List<SimpleMenuResp> children;





}
