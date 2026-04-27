package com.jasolar.mis.module.system.controller.ehr.vo;

import com.jasolar.mis.framework.common.pojo.PageParam;
import lombok.Data;

/**
 * Description:
 * Author : Zhou Hai
 * Date : 09/12/2025 15:03
 * Version : 1.0
 */
@Data
public class EhrSearchVo extends PageParam {

    private String year;

    private String manageOrgKey;

    private String ehrOrgKey;

    /** ERP部门，模糊搜索 */
    private String erpDepart;

}
