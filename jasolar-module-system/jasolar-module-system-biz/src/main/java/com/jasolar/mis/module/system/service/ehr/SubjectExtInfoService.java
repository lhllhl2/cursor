package com.jasolar.mis.module.system.service.ehr;

import com.jasolar.mis.module.system.domain.ehr.SubjectInfo;

import java.util.List;

/**
 * 科目扩展信息 Service 接口
 */
public interface SubjectExtInfoService {

    /**
     * 同步科目信息数据
     * @param subjectInfoList 科目信息列表（包含 cust1Cd 和 acctCd）
     * @return 处理结果描述
     */
    String syncSubjectInfoData(List<SubjectInfo> subjectInfoList);

}

