package com.jasolar.mis.module.system.resp;

import lombok.Data;

/**
 * Description:
 * Author : Zhou Hai
 * Date : 15/12/2025 10:20
 * Version : 1.0
 */
@Data
public class ProjectExceptResp {

    private Long id ;

    private String prjCd ;
    /** 项目名称,; */
    private String prjNm ;
    /** 父项目编码,; */
    private String parCd ;
    /** 父项目名称,; */
    private String parNm ;
    /** 是否为叶节点 1是 0否,; */
    private boolean leaf ;

    private String toAdd;

}
