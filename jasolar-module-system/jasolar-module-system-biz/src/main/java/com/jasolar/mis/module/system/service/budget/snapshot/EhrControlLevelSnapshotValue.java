package com.jasolar.mis.module.system.service.budget.snapshot;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * EHR控制层级快照值对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EhrControlLevelSnapshotValue {

    public static final String DEFAULT_NAN = "NAN";

    /**
     * 控制层级EHR组织代码
     */
    private String controlEhrCd;

    /**
     * 控制层级EHR组织名称
     */
    private String controlEhrNm;

    /**
     * 预算组织编码
     */
    private String budgetOrgCd;

    /**
     * 预算组织名称
     */
    private String budgetOrgNm;

    public static EhrControlLevelSnapshotValue nanValue() {
        return EhrControlLevelSnapshotValue.builder()
                .controlEhrCd(DEFAULT_NAN)
                .controlEhrNm(DEFAULT_NAN)
                .budgetOrgCd(DEFAULT_NAN)
                .budgetOrgNm(DEFAULT_NAN)
                .build();
    }
}

