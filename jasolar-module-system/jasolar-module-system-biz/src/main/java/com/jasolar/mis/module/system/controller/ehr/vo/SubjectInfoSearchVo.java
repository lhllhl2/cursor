package com.jasolar.mis.module.system.controller.ehr.vo;

import com.jasolar.mis.framework.common.pojo.PageParam;
import lombok.Data;

/**
 * ProjectInfo 查询 VO
 */
@Data
public class SubjectInfoSearchVo extends PageParam {

    private String year;

    private String cust1Cd;

    private String cust1Nm;

    private String acctKey;

    private String erpAcctKey;

    private String controlAcctCd;

    private String controlAcctNm;





}