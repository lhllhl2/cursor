package com.jasolar.mis.module.system.service.budget.snapshot.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.jasolar.mis.framework.common.util.json.JsonUtils;
import com.jasolar.mis.framework.redis.util.RedisUtils;
import com.jasolar.mis.module.system.domain.ehr.EhrControlLevelView;
import com.jasolar.mis.module.system.mapper.ehr.EhrControlLevelViewMapper;
import com.jasolar.mis.module.system.service.budget.snapshot.EhrControlLevelSnapshotService;
import com.jasolar.mis.module.system.service.budget.snapshot.EhrControlLevelSnapshotValue;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import jakarta.annotation.PostConstruct;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * EHR控制层级快照查询服务实现（第一步：直连 V_EHR_CONTROL_LEVEL）
 */
@Service
@Slf4j
public class EhrControlLevelSnapshotServiceImpl implements EhrControlLevelSnapshotService {

    /**
     * Oracle IN 子句限制 1000，预留余量。
     */
    private static final int BATCH_SIZE = 900;
    private static final String CACHE_KEY_PREFIX = "budget:ehr_control_level_snapshot:";
    private static final long CACHE_TTL_HOURS = 24L;

    @Resource
    private EhrControlLevelViewMapper ehrControlLevelViewMapper;

    @PostConstruct
    public void warmupCacheOnStartup() {
        try {
            refreshAllCache();
        } catch (Exception e) {
            // 预热失败不影响主流程，查询时仍可回查DB
            log.warn("EHR控制层级快照缓存预热失败，将在查询时回查DB。err={}", e.getMessage());
        }
    }

    @Override
    public Map<String, EhrControlLevelSnapshotValue> getSnapshotByEhrCds(Collection<String> ehrCds) {
        if (ehrCds == null || ehrCds.isEmpty()) {
            return Collections.emptyMap();
        }

        // 先去重并过滤空值，保证返回 key 稳定
        LinkedHashSet<String> normalizedSet = new LinkedHashSet<>();
        for (String ehrCd : ehrCds) {
            if (StringUtils.isNotBlank(ehrCd)) {
                normalizedSet.add(ehrCd);
            }
        }
        List<String> normalized = new ArrayList<>(normalizedSet);
        if (normalized.isEmpty()) {
            return Collections.emptyMap();
        }

        Map<String, EhrControlLevelSnapshotValue> result = new LinkedHashMap<>();
        for (String ehrCd : normalized) {
            result.put(ehrCd, EhrControlLevelSnapshotValue.nanValue());
        }

        // 1) Redis 命中优先
        List<String> missEhrCds = new ArrayList<>();
        for (String ehrCd : normalized) {
            EhrControlLevelSnapshotValue cached = getFromCache(ehrCd);
            if (cached == null) {
                missEhrCds.add(ehrCd);
                continue;
            }
            result.put(ehrCd, cached);
        }
        if (missEhrCds.isEmpty()) {
            return result;
        }

        // 2) miss 回查 DB
        List<EhrControlLevelView> views = batchQueryByEhrCds(missEhrCds);
        Map<String, EhrControlLevelView> viewMap = new HashMap<>();
        if (views != null) {
            for (EhrControlLevelView view : views) {
                if (view == null || StringUtils.isBlank(view.getEhrCd())) {
                    continue;
                }
                viewMap.putIfAbsent(view.getEhrCd(), view);
            }
        }

        // 3) 回填缓存（命中和未命中都回填，未命中写 NAN）
        for (String ehrCd : missEhrCds) {
            EhrControlLevelView view = viewMap.get(ehrCd);
            EhrControlLevelSnapshotValue value = toSnapshotValue(view);
            result.put(ehrCd, value);
            setToCache(ehrCd, value);
        }

        return result;
    }

    @Override
    public void refreshAllCache() {
        clearAllCache();
        List<EhrControlLevelView> all = ehrControlLevelViewMapper.selectList(new LambdaQueryWrapper<>());
        if (all == null || all.isEmpty()) {
            log.warn("EHR控制层级快照缓存刷新完成，但视图无数据");
            return;
        }
        int count = 0;
        for (EhrControlLevelView view : all) {
            if (view == null || StringUtils.isBlank(view.getEhrCd())) {
                continue;
            }
            setToCache(view.getEhrCd(), toSnapshotValue(view));
            count++;
        }
        log.info("EHR控制层级快照缓存刷新完成，缓存条数={}", count);
    }

    private List<EhrControlLevelView> batchQueryByEhrCds(List<String> ehrCds) {
        List<EhrControlLevelView> result = new ArrayList<>();
        for (int i = 0; i < ehrCds.size(); i += BATCH_SIZE) {
            int end = Math.min(i + BATCH_SIZE, ehrCds.size());
            List<String> batch = ehrCds.subList(i, end);
            List<EhrControlLevelView> batchResult = ehrControlLevelViewMapper.selectByEhrCds(batch);
            if (batchResult != null && !batchResult.isEmpty()) {
                result.addAll(batchResult);
            }
        }
        return result;
    }

    private EhrControlLevelSnapshotValue toSnapshotValue(EhrControlLevelView view) {
        if (view == null) {
            return EhrControlLevelSnapshotValue.nanValue();
        }
        return EhrControlLevelSnapshotValue.builder()
                .controlEhrCd(defaultNan(view.getControlEhrCd()))
                .controlEhrNm(defaultNan(view.getControlEhrNm()))
                .budgetOrgCd(defaultNan(view.getBudgetOrgCd()))
                .budgetOrgNm(defaultNan(view.getBudgetOrgNm()))
                .build();
    }

    private EhrControlLevelSnapshotValue getFromCache(String ehrCd) {
        try {
            String cacheValue = RedisUtils.getStr(buildCacheKey(ehrCd));
            if (StringUtils.isBlank(cacheValue)) {
                return null;
            }
            return JsonUtils.parseObject(cacheValue, EhrControlLevelSnapshotValue.class);
        } catch (Exception e) {
            log.warn("读取EHR控制层级快照缓存失败，ehrCd={}, err={}", ehrCd, e.getMessage());
            return null;
        }
    }

    private void setToCache(String ehrCd, EhrControlLevelSnapshotValue value) {
        try {
            RedisUtils.set(buildCacheKey(ehrCd), JsonUtils.toJsonString(value), CACHE_TTL_HOURS, TimeUnit.HOURS);
        } catch (Exception e) {
            log.warn("写入EHR控制层级快照缓存失败，ehrCd={}, err={}", ehrCd, e.getMessage());
        }
    }

    private void clearAllCache() {
        try {
            List<String> keys = RedisUtils.scan(CACHE_KEY_PREFIX + "*");
            if (keys != null && !keys.isEmpty()) {
                RedisUtils.del(keys.toArray(new String[0]));
            }
        } catch (Exception e) {
            log.warn("清理EHR控制层级快照缓存失败，err={}", e.getMessage());
        }
    }

    private String buildCacheKey(String ehrCd) {
        return CACHE_KEY_PREFIX + ehrCd;
    }

    private String defaultNan(String value) {
        return StringUtils.isBlank(value) ? EhrControlLevelSnapshotValue.DEFAULT_NAN : value;
    }
}

