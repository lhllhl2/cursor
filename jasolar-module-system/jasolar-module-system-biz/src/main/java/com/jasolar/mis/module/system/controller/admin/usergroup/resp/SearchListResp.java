package com.jasolar.mis.module.system.controller.admin.usergroup.resp;

import lombok.Data;

import java.util.Date;

/**
 * Description:
 * Author : Zhou Hai
 * Date : 23/07/2025 15:54
 * Version : 1.0
 */
@Data
public class SearchListResp {

    private Long id;

    private String code;

    private String name;

    private String type;

    private String typeDes;

    private Integer userCount;

    private String creator;

    private Date createTime;

    private String remark;

    private String updater;

    private Date updateTime;


}
