package com.jasolar.mis.module.system.resp;

import lombok.Data;

import java.util.List;

/**
 * Description:
 * Author : Zhou Hai
 * Date : 22/12/2025 11:18
 * Version : 1.0
 */
@Data
public class BudgetOrgResp {

    private String orgCd;

    private String orgNm;

    private String parCd;

    private String parNm;

    List<BudgetOrgResp> children;

}
