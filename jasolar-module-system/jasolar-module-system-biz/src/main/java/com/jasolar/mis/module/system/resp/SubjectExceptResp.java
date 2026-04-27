package com.jasolar.mis.module.system.resp;

import lombok.Data;

/**
 * Description: 
 * Author : Zhou Hai
 * Date : 16/12/2025 16:11
 * Version : 1.0
 */
@Data
public class SubjectExceptResp {

    private String cust1Cd;

    private String cust1Nm;

    private String acctCd;
    /** 科目名称,; */
    private String acctNm;
    /** 父编码,; */
    private String parAcctCd;

    private String parAcctNm;

    /** 是否为叶节点 1是 0否,; */
    private boolean leaf;

    private String toAdd;

}