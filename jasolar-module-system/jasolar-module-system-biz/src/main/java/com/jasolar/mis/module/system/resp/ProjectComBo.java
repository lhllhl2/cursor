package com.jasolar.mis.module.system.resp;

import com.jasolar.mis.module.system.domain.ehr.ProjectControlR;
import lombok.Builder;
import lombok.Data;

/**
 * Description:
 * Author : Zhou Hai
 * Date : 26/12/2025 10:59
 * Version : 1.0
 */
@Builder
@Data
public class ProjectComBo {

    private boolean hasControlLevel;

    private ProjectControlR projectControlR;
}
