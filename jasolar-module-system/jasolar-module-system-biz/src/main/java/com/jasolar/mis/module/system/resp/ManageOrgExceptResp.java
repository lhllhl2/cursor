package com.jasolar.mis.module.system.resp;

import lombok.Data;

/**
 * Description:
 * Author : Zhou Hai
 * Date : 29/12/2025 18:11
 * Version : 1.0
 */
@Data
public class ManageOrgExceptResp {

    /**
     * 组织ID
     */
    private Long id;

    /**
     * 组织名称
     */
    private String name;

    /**
     * 组织编码
     */
    private String code;
    /**
     * 父级组织编码，顶级组织为空或0
     */
    private String pCode;

    private String pName;

    private Boolean isLastLvl;

    private String toAdd;

}
