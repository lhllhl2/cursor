package com.jasolar.mis.module.system.controller.admin.log.resp;

import lombok.Data;

import java.util.Date;

/**
 * Description:
 * Author : Zhou Hai
 * Date : 27/08/2025 15:04
 * Version : 1.0
 */
@Data
public class LogResp {

    private Long id;

    private String userName;

    private String displayName;

    private String ip;

    private String logType;

    private Date createTime;


}
