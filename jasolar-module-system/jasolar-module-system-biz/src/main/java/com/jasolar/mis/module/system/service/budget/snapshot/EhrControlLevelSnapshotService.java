package com.jasolar.mis.module.system.service.budget.snapshot;

import java.util.Collection;
import java.util.Map;

/**
 * EHR控制层级快照查询服务（第一步：直连视图，不使用Redis）
 */
public interface EhrControlLevelSnapshotService {

    /**
     * 批量按 EHR_CD 查询快照信息。
     * 未命中的 EHR_CD 也会返回默认值（NAN）。
     *
     * @param ehrCds EHR_CD 集合
     * @return key=EHR_CD，value=快照信息
     */
    Map<String, EhrControlLevelSnapshotValue> getSnapshotByEhrCds(Collection<String> ehrCds);

    /**
     * 全量重建快照缓存。
     */
    void refreshAllCache();
}

