package com.jasolar.mis.module.system.resp;

import com.jasolar.mis.module.system.domain.ehr.SubjectInfo;
import lombok.Builder;
import lombok.Data;

/**
 * Description:
 * Author : Zhou Hai
 * Date : 24/12/2025 10:58
 * Version : 1.0
 */
@Builder
@Data
public class SubjectComBo {

    private boolean hasControlLevel;

    private SubjectInfo subjectInfo;

}
