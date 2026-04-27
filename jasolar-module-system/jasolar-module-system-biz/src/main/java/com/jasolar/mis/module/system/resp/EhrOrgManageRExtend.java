package com.jasolar.mis.module.system.resp;

import com.jasolar.mis.module.system.domain.ehr.EhrOrgManageR;
import lombok.Data;
import lombok.ToString;

/**
 * Description:
 * Author : Zhou Hai
 * Date : 19/12/2025 16:13
 * Version : 1.0
 */
@ToString
@Data
public class EhrOrgManageRExtend extends EhrOrgManageR {

    private String nodeLevel;


}
