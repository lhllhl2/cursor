package com.jasolar.mis.module.system.service.budget.quota.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.incrementer.IdentifierGenerator;
import com.jasolar.mis.module.system.domain.budget.*;
import com.jasolar.mis.module.system.mapper.budget.*;
import com.jasolar.mis.module.system.service.budget.quota.BudgetQuotaService;
import com.jasolar.mis.module.system.service.budget.helper.BudgetQueryHelperService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 预算额度 Service 实现
 */
@Service
@Slf4j
public class BudgetQuotaServiceImpl implements BudgetQuotaService {

    private static final String DEFAULT_CURRENCY = "CNY";
    private static final String QUARTER_Q1 = "q1";
    private static final String QUARTER_Q2 = "q2";
    private static final String QUARTER_Q3 = "q3";
    private static final String QUARTER_Q4 = "q4";

    @Resource
    private BudgetPoolDemRMapper budgetPoolDemRMapper;
    @Resource
    private SystemProjectBudgetMapper systemProjectBudgetMapper;
    @Resource
    private BudgetQuotaMapper budgetQuotaMapper;
    @Resource
    private BudgetBalanceMapper budgetBalanceMapper;
    @Resource
    private BudgetQuotaHistoryMapper budgetQuotaHistoryMapper;
    @Resource
    private BudgetBalanceHistoryMapper budgetBalanceHistoryMapper;
    @Resource
    private IdentifierGenerator identifierGenerator;
    @Resource
    private BudgetQueryHelperService budgetQueryHelperService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String syncQuotaDataFromOriginal(String year) {
        log.info("开始同步原始预算数据，year={}", year);

        // 步骤一：构建 poolRelationMap 并分类 SYSTEM_PROJECT_BUDGET 数据
        Map<String, Long> poolRelationMap = buildPoolRelationMap();
        // 构建 PROJECT_ID 到 poolId 的映射（用于场景4和5，支持PROJECT_CD变更）
        Map<String, Map<String, Long>> projectIdToPoolIdMap = buildProjectIdToPoolIdMap(year);
        List<SystemProjectBudget> originalBudgetList = loadOriginalBudgets(year);
        log.info("步骤一：poolRelationMap大小={}, projectIdToPoolIdMap大小={}, originalBudgetList大小={}", 
                poolRelationMap.size(), projectIdToPoolIdMap.size(), originalBudgetList.size());
        
        Map<String, Map<String, SystemProjectBudget>> existingBudgetForCompareMap = new HashMap<>();
        Map<String, Map<String, SystemProjectBudget>> existingBudgetWithoutProjectForCompareMap = new HashMap<>();
        Map<String, Map<String, SystemProjectBudget>> needToAddBudgetMap = new HashMap<>();
        Map<String, Map<String, SystemProjectBudget>> needToAddBudgetProjectMap = new HashMap<>();
        
        for (SystemProjectBudget budget : originalBudgetList) {
            // 遍历所有季度查找对应的 poolId
            boolean found = false;
            for (String quarter : new String[]{QUARTER_Q1, QUARTER_Q2, QUARTER_Q3, QUARTER_Q4}) {
                Long poolId = null;
                
                // 对于场景4和5（带项目），优先通过PROJECT_ID查找poolId
                if (StringUtils.isNotBlank(budget.getProject()) && StringUtils.isNotBlank(budget.getProjectId())) {
                    Map<String, Long> quarterMap = projectIdToPoolIdMap.get(budget.getProjectId());
                    if (quarterMap != null) {
                        poolId = quarterMap.get(quarter);
                    }
                }
                
                // 如果通过PROJECT_ID没找到，使用原来的PROJECT_CD逻辑
                if (poolId == null) {
                    // 如果有资产类型编码，则预算科目编码设置为 NAN-NAN（两者互斥）
                    // BUDGET_POOL_DEM_R 的 budget_subject_code 使用 custom1 + "-" + account 的形式
                    String budgetSubjectCode = StringUtils.isNotBlank(budget.getCustom3())
                            ? "NAN-NAN"
                            : buildBudgetSubjectCode(budget.getCustom1(), budget.getAccount());
                    String key = buildPoolRelationKey(budget.getYear(), quarter,
                            budget.getIsInternal(),
                            budget.getCustom2(),
                            budgetSubjectCode,
                            budget.getProject() != null ? budget.getProject() : "NAN",
                            budget.getCustom3() != null ? budget.getCustom3() : "NAN");
                    poolId = poolRelationMap.get(key);
                }
                
                // 如果通过PROJECT_ID和PROJECT_CD都没找到，但SYSTEM_PROJECT_BUDGET中有PROJECT_ID，
                // 说明PROJECT_CD/组织编码/资产类型等已变更，尝试通过项目ID（及年季度等）查找匹配的poolId
                // 项目预算只按PROJECT_ID匹配，不要求组织编码(morgCode)、资产类型(erpAssetType)一致，避免组织/资产类型变更时重复新增
                if (poolId == null && StringUtils.isNotBlank(budget.getProject()) && StringUtils.isNotBlank(budget.getProjectId())) {
                    // 对于场景4和5，按 year + quarter + isInternal + projectId 查找已有池子（不要求 morgCode、erpAssetType 匹配）
                    for (Map.Entry<String, Long> entry : poolRelationMap.entrySet()) {
                        String key = entry.getKey();
                        Long candidatePoolId = entry.getValue();

                        // 解析key: year@quarter@isInternal@morgCode@budgetSubjectCode@masterProjectCode@erpAssetType
                        String[] parts = key.split("@");
                        if (parts.length >= 7) {
                            String keyYear = parts[0];
                            String keyQuarter = parts[1];
                            String keyIsInternal = parts[2];
                            String keyMasterProjectCode = parts[5];

                            // 项目预算只匹配：年、季度、是否内部、带项目；不要求 morgCode、budgetSubjectCode、erpAssetType 一致
                            if (budget.getYear().equals(keyYear)
                                    && quarter.equals(keyQuarter)
                                    && StringUtils.equals(budget.getIsInternal(), keyIsInternal)
                                    && !"NAN".equals(keyMasterProjectCode)) {
                                BudgetPoolDemR candidatePoolDemR = budgetPoolDemRMapper.selectById(candidatePoolId);
                                if (candidatePoolDemR != null) {
                                    // 仅按 PROJECT_ID 匹配：pool 必须有 project_id 且与 budget 一致
                                    // 预算调整单等创建的 pool 未设置 project_id，不纳入 fallback 匹配范围，由补足模式（单独参数控制）按 project_code 处理
                                    boolean projectIdMatch = StringUtils.equals(budget.getProjectId(), candidatePoolDemR.getProjectId());
                                    if (projectIdMatch) {
                                        // 命中：按项目ID找到池子，同步 MASTER_PROJECT_CODE / MORG_CODE / ERP_ASSET_TYPE（pool 已有 project_id）
                                        boolean needUpdate = false;
                                        if (!budget.getProject().equals(candidatePoolDemR.getMasterProjectCode())) {
                                            candidatePoolDemR.setMasterProjectCode(budget.getProject());
                                            needUpdate = true;
                                        }
                                        String newMorgCode = budget.getCustom2() != null ? budget.getCustom2() : "NAN";
                                        if (!newMorgCode.equals(candidatePoolDemR.getMorgCode())) {
                                            candidatePoolDemR.setMorgCode(newMorgCode);
                                            needUpdate = true;
                                        }
                                        String newErpAssetType = budget.getCustom3() != null ? budget.getCustom3() : "NAN";
                                        if (!newErpAssetType.equals(candidatePoolDemR.getErpAssetType())) {
                                            candidatePoolDemR.setErpAssetType(newErpAssetType);
                                            needUpdate = true;
                                        }
                                        if (needUpdate) {
                                            budgetPoolDemRMapper.updateById(candidatePoolDemR);
                                            log.info("项目预算维度变更：按PROJECT_ID找到池子并更新维度: poolId={}, projectId={}, morgCode/erpAssetType已同步",
                                                    candidatePoolId, budget.getProjectId());
                                            projectIdToPoolIdMap.computeIfAbsent(budget.getProjectId(), k -> new HashMap<>())
                                                    .put(quarter, candidatePoolId);
                                        }
                                        poolId = candidatePoolId;
                                        break;
                                    }
                                }
                            }
                        }
                    }
                }
                
                if (poolId != null) {
                    String poolIdKey = String.valueOf(poolId);
                    
                    // 如果通过PROJECT_CD找到了poolId，并且SYSTEM_PROJECT_BUDGET中有PROJECT_ID，
                    // 但BUDGET_POOL_DEM_R中没有PROJECT_ID，需要更新BUDGET_POOL_DEM_R的PROJECT_ID字段
                    if (StringUtils.isNotBlank(budget.getProject()) && StringUtils.isNotBlank(budget.getProjectId())) {
                        // 检查BUDGET_POOL_DEM_R是否有PROJECT_ID
                        BudgetPoolDemR poolDemR = budgetPoolDemRMapper.selectById(poolId);
                        if (poolDemR != null && StringUtils.isBlank(poolDemR.getProjectId())) {
                            // 更新PROJECT_ID字段
                            poolDemR.setProjectId(budget.getProjectId());
                            budgetPoolDemRMapper.updateById(poolDemR);
                            log.info("更新BUDGET_POOL_DEM_R的PROJECT_ID: poolId={}, projectId={}, projectCode={}", 
                                    poolId, budget.getProjectId(), budget.getProject());
                        }
                    }
                    
                    // 判断是否需要放入 existingBudgetForCompareMap
                    // 前提：project 为空或空字符串（场景1-3）
                    if (StringUtils.isBlank(budget.getProject())) {
                        if (StringUtils.isBlank(budget.getCustom3())) {
                            // 如果没有资产类型编码，使用 "default" 作为 key
                            existingBudgetForCompareMap.computeIfAbsent(poolIdKey, k -> new HashMap<>()).put("default", budget);
                        } else {
                            // 如果有资产类型编码，只放入特定的科目编码
                            // 如果 account 为空，默认使用 "NAN"
                            String account = StringUtils.isNotBlank(budget.getAccount()) ? budget.getAccount() : "NAN";
                            if ("A01030112".equals(account) || "A010301150102".equals(account)) {
                                existingBudgetForCompareMap.computeIfAbsent(poolIdKey, k -> new HashMap<>()).put(account, budget);
                            }
                            // 其他科目编码直接忽略，不放入 Map
                        }
                    } else {
                        // project 不为空（场景4-5），判断特定科目编码
                        // 如果 account 为空，默认使用 "NAN"
                        String account = StringUtils.isNotBlank(budget.getAccount()) ? budget.getAccount() : "NAN";
                        if ("A01030115010102".equals(account) || "A010301150102".equals(account)) {
                            existingBudgetWithoutProjectForCompareMap.computeIfAbsent(poolIdKey, k -> new HashMap<>()).put(account, budget);
                        }
                    }
                    
                    found = true;
                    // 不 break，因为同一个 budget 可能对应多个季度（多个 poolId）
                }
            }
            if (!found) {
                // 如果没有找到对应的 poolId，将其放入 needToAddBudgetMap
                // 前提：project 为空或空字符串（场景1-3）
                if (StringUtils.isBlank(budget.getProject())) {
                    for (String quarter : new String[]{QUARTER_Q1, QUARTER_Q2, QUARTER_Q3, QUARTER_Q4}) {
                        if (StringUtils.isBlank(budget.getCustom3())) {
                            // 没有资产类型编码
                            // BUDGET_POOL_DEM_R 的 budget_subject_code 使用 custom1 + "-" + account 的形式
                            String budgetSubjectCode = buildBudgetSubjectCode(budget.getCustom1(), budget.getAccount());
                            String newKey = buildPoolRelationKey(budget.getYear(), quarter,
                                    budget.getIsInternal(),
                                    budget.getCustom2(),
                                    budgetSubjectCode,
                                    "NAN",
                                    "NAN");
                            needToAddBudgetMap.computeIfAbsent(newKey, k -> new HashMap<>()).put("default", budget);
                        } else {
                            // 有资产类型编码，只放入特定的科目编码
                            // 如果 account 为空，默认使用 "NAN"
                            String account = StringUtils.isNotBlank(budget.getAccount()) ? budget.getAccount() : "NAN";
                            if ("A01030112".equals(account) || "A010301150102".equals(account)) {
                                String newKey = buildPoolRelationKey(budget.getYear(), quarter,
                                        budget.getIsInternal(),
                                        budget.getCustom2(),
                                        "NAN-NAN",
                                        "NAN",
                                        budget.getCustom3());
                                needToAddBudgetMap.computeIfAbsent(newKey, k -> new HashMap<>()).put(account, budget);
                            }
                            // 其他科目编码直接忽略
                        }
                    }
                } else {
                    // project 不为空（场景4-5），判断特定科目编码
                    // 如果 account 为空，默认使用 "NAN"
                    String account = StringUtils.isNotBlank(budget.getAccount()) ? budget.getAccount() : "NAN";
                    if ("A01030115010102".equals(account) || "A010301150102".equals(account)) {
                        for (String quarter : new String[]{QUARTER_Q1, QUARTER_Q2, QUARTER_Q3, QUARTER_Q4}) {
                            // 对于带项目的场景，使用 "NAN-NAN" 作为 budget_subject_code
                            String newKey = buildPoolRelationKey(budget.getYear(), quarter,
                                    budget.getIsInternal(),
                                    budget.getCustom2(),
                                    "NAN-NAN",
                                    budget.getProject(),
                                    "NAN");
                            needToAddBudgetProjectMap.computeIfAbsent(newKey, k -> new HashMap<>()).put(account, budget);
                        }
                    }
                }
            }
        }
        
        log.info("步骤一完成：existingBudgetForCompareMap包含的poolId={}, existingBudgetWithoutProjectForCompareMap包含的poolId={}", 
                existingBudgetForCompareMap.keySet(), existingBudgetWithoutProjectForCompareMap.keySet());
        log.info("步骤一完成：existingBudgetForCompareMap大小={}, existingBudgetWithoutProjectForCompareMap大小={}, needToAddBudgetMap大小={}, needToAddBudgetProjectMap大小={}", 
                existingBudgetForCompareMap.size(), existingBudgetWithoutProjectForCompareMap.size(), 
                needToAddBudgetMap.size(), needToAddBudgetProjectMap.size());

        // 步骤二：查询 BUDGET_QUOTA 和 BUDGET_BALANCE 数据（只查询指定年份的数据）
        Map<Long, BudgetQuota> existingBudgetQuotaMap = loadBudgetQuotas(year);
        Map<Long, BudgetBalance> existingBudgetBalanceMap = loadBudgetBalances(year);
        log.info("步骤二：existingBudgetQuotaMap大小={}, 包含的poolId={}", existingBudgetQuotaMap.size(), existingBudgetQuotaMap.keySet());

        // 步骤三：处理已存在的预算数据
        List<BudgetQuota> needToUpdateBudgetQuota = new ArrayList<>();
        List<BudgetQuotaHistory> needToAddBudgetQuotaHistory = new ArrayList<>();
        List<BudgetBalance> needToUpdateBudgetBalance = new ArrayList<>();
        List<BudgetBalanceHistory> needToAddBudgetBalanceHistory = new ArrayList<>();
        // 收集需要更新PROJECT_CODE/MASTER_PROJECT_CODE的BUDGET_POOL_DEM_R记录
        Map<Long, String> needToUpdatePoolDemRProjectCode = new HashMap<>();
        // 收集需要更新MORG_CODE的BUDGET_POOL_DEM_R记录（组织编码变更时）
        Map<Long, String> needToUpdatePoolDemRMorgCode = new HashMap<>();

        for (Map.Entry<Long, BudgetQuota> entry : existingBudgetQuotaMap.entrySet()) {
            Long poolId = entry.getKey();
            BudgetQuota existingQuota = entry.getValue();
            
            String poolIdKey = String.valueOf(poolId);
            Map<String, SystemProjectBudget> budgetMap = existingBudgetForCompareMap.get(poolIdKey);
            Map<String, SystemProjectBudget> budgetWithoutProjectMap = existingBudgetWithoutProjectForCompareMap.get(poolIdKey);
            if ((budgetMap == null || budgetMap.isEmpty()) && (budgetWithoutProjectMap == null || budgetWithoutProjectMap.isEmpty())) {
                // 详细日志：记录出错的 poolId 和对应的 BudgetQuota 信息
                log.warn("跳过处理：poolId={} 对应的 SYSTEM_PROJECT_BUDGET 数据不存在，跳过该记录继续处理其他数据", poolId);
                
                // 从 poolRelationMap 中查找对应的 key 和 budgetSubjectCode
                String foundKey = null;
                String budgetSubjectCode = null;
                for (Map.Entry<String, Long> poolEntry : poolRelationMap.entrySet()) {
                    if (poolEntry.getValue().equals(poolId)) {
                        foundKey = poolEntry.getKey();
                        // 解析 key 获取 budgetSubjectCode: year@quarter@isInternal@morgCode@budgetSubjectCode@masterProjectCode@erpAssetType
                        String[] parts = foundKey.split("@");
                        if (parts.length >= 5) {
                            budgetSubjectCode = parts[4];
                        }
                        log.warn("在poolRelationMap中找到poolId={}, 对应的key={}, budgetSubjectCode={}", poolId, foundKey, budgetSubjectCode);
                        break;
                    }
                }
                if (foundKey == null) {
                    log.warn("poolId={} 在 poolRelationMap 中不存在", poolId);
                }
                
                log.warn("跳过的BudgetQuota详细信息：id={}, year={}, quarter={}, poolId={}, projectCode={}, erpAssetType={}, morgCode={}, budgetSubjectCode={}, isInternal={}", 
                        existingQuota.getId(), existingQuota.getYear(), existingQuota.getQuarter(), 
                        existingQuota.getPoolId(), existingQuota.getProjectCode(), existingQuota.getErpAssetType(),
                        existingQuota.getMorgCode(), budgetSubjectCode, existingQuota.getIsInternal());
                
                // 检查原始预算数据中是否有部分匹配的数据（用于排查问题）
                int matchedCount = 0;
                boolean hasProjectCode = !"NAN".equals(existingQuota.getProjectCode());
                String expectedProjectCode = hasProjectCode ? existingQuota.getProjectCode() : null;
                List<String> foundProjectCodes = new ArrayList<>();
                
                for (SystemProjectBudget budget : originalBudgetList) {
                    if (budget.getYear().equals(existingQuota.getYear()) && 
                        budget.getCustom2() != null && budget.getCustom2().equals(existingQuota.getMorgCode())) {
                        matchedCount++;
                        
                        // 如果有项目编码，检查项目编码是否匹配
                        if (hasProjectCode && budget.getProject() != null) {
                            if (!foundProjectCodes.contains(budget.getProject())) {
                                foundProjectCodes.add(budget.getProject());
                            }
                        }
                        
                        if (matchedCount <= 3) { // 只记录前3条，避免日志过多
                            log.warn("找到部分匹配的预算数据（year和morgCode匹配）：year={}, custom2={}, account={}, project={}, custom1={}, custom3={}, isInternal={}, 构建的budgetSubjectCode={}", 
                                    budget.getYear(), budget.getCustom2(), budget.getAccount(), budget.getProject(), 
                                    budget.getCustom1(), budget.getCustom3(), budget.getIsInternal(),
                                    StringUtils.isNotBlank(budget.getCustom3()) ? "NAN-NAN" : buildBudgetSubjectCode(budget.getCustom1(), budget.getAccount()));
                        }
                    }
                }
                
                // 根据匹配情况输出详细的日志
                if (matchedCount == 0) {
                    log.warn("在SYSTEM_PROJECT_BUDGET表中未找到任何匹配的数据（year={}, morgCode={}），可能是数据缺失", 
                            existingQuota.getYear(), existingQuota.getMorgCode());
                } else if (hasProjectCode) {
                    // 有项目编码的情况，检查项目编码是否匹配
                    if (foundProjectCodes.contains(expectedProjectCode)) {
                        log.warn("找到{}条部分匹配的预算数据（year和morgCode匹配），且项目编码{}存在，但budgetSubjectCode不匹配，无法建立关联", 
                                matchedCount, expectedProjectCode);
                    } else {
                        log.warn("找到{}条部分匹配的预算数据（year和morgCode匹配），但项目编码不匹配：期望projectCode={}，实际找到的projectCode列表={}，无法建立关联", 
                                matchedCount, expectedProjectCode, foundProjectCodes.size() > 10 ? foundProjectCodes.subList(0, 10) + "..." : foundProjectCodes);
                    }
                } else {
                    log.warn("找到{}条部分匹配的预算数据（year和morgCode匹配），但budgetSubjectCode不匹配，无法建立关联", matchedCount);
                }
                
                // 跳过该记录，继续处理下一条
                continue;
            }
            
            // 根据是否有项目编码，决定从哪个 Map 获取预算数据
            String mapKey;
            SystemProjectBudget compareBudget;
            
            if ("NAN".equals(existingQuota.getProjectCode())) {
                // 没有项目编码，从 budgetMap 获取
                if ("NAN".equals(existingQuota.getErpAssetType())) {
                    // 没有资产类型，使用 "default"
                    mapKey = "default";
                } else {
                    // 有资产类型，使用固定的采购额科目编码
                    mapKey = "A01030112";
                }
                compareBudget = budgetMap.get(mapKey);
            } else {
                // 有项目编码，从 budgetWithoutProjectMap 获取
                // 只使用采购额科目（A01030115010102），付款额不能用于 amountAvailable
                mapKey = "A01030115010102";
                compareBudget = budgetWithoutProjectMap != null ? budgetWithoutProjectMap.get(mapKey) : null;
                // 如果不存在采购额科目，检查是否有付款额科目
                if (compareBudget == null) {
                    SystemProjectBudget paymentBudget = budgetWithoutProjectMap != null ? budgetWithoutProjectMap.get("A010301150102") : null;
                    if (paymentBudget != null) {
                        // 只有付款额没有采购额，compareBudget 为 null，后续 amountAvailable 会设为null（与新增逻辑保持一致）
                        log.info("带项目场景（更新）：只有付款额科目（A010301150102）没有采购额科目（A01030115010102），amountAvailable 将设为null：poolId={}", poolId);
                    } else {
                        // 既没有采购额也没有付款额，跳过处理
                        log.warn("跳过处理：poolId={}, mapKey={} 对应的 SYSTEM_PROJECT_BUDGET 数据不存在，既没有采购额科目（A01030115010102）也没有付款额科目（A010301150102），跳过该记录继续处理其他数据", poolId, mapKey);
                        
                        // 从 poolRelationMap 中查找对应的 budgetSubjectCode
                        String budgetSubjectCode = null;
                        for (Map.Entry<String, Long> poolEntry : poolRelationMap.entrySet()) {
                            if (poolEntry.getValue().equals(poolId)) {
                                String foundKey = poolEntry.getKey();
                                // 解析 key 获取 budgetSubjectCode: year@quarter@isInternal@morgCode@budgetSubjectCode@masterProjectCode@erpAssetType
                                String[] parts = foundKey.split("@");
                                if (parts.length >= 5) {
                                    budgetSubjectCode = parts[4];
                                }
                                break;
                            }
                        }
                        
                        log.warn("跳过的BudgetQuota详细信息：id={}, year={}, quarter={}, poolId={}, projectCode={}, erpAssetType={}, morgCode={}, budgetSubjectCode={}, isInternal={}", 
                                existingQuota.getId(), existingQuota.getYear(), existingQuota.getQuarter(), 
                                existingQuota.getPoolId(), existingQuota.getProjectCode(), existingQuota.getErpAssetType(),
                                existingQuota.getMorgCode(), budgetSubjectCode, existingQuota.getIsInternal());
                        log.warn("查找的mapKey={}, 使用的Map={}", mapKey, "NAN".equals(existingQuota.getProjectCode()) ? "existingBudgetForCompareMap" : "existingBudgetWithoutProjectForCompareMap");
                        if ("NAN".equals(existingQuota.getProjectCode())) {
                            log.warn("existingBudgetForCompareMap中poolId={}的所有key={}", poolIdKey, budgetMap != null ? budgetMap.keySet() : "null");
                        } else {
                            log.warn("existingBudgetWithoutProjectForCompareMap中poolId={}的所有key={}", poolIdKey, budgetWithoutProjectMap != null ? budgetWithoutProjectMap.keySet() : "null");
                        }
                        // 跳过该记录，继续处理下一条
                        continue;
                    }
                }
            }
            
            // 对于不带项目的情况，如果 compareBudget 为 null，需要检查是否有付款额（资产类型场景）
            if (compareBudget == null && "NAN".equals(existingQuota.getProjectCode())) {
                // 如果是资产类型场景，检查是否有付款额
                if (!"NAN".equals(existingQuota.getErpAssetType())) {
                    SystemProjectBudget paymentBudget = budgetMap.get("A010301150102");
                    if (paymentBudget != null) {
                        // 只有付款额没有采购额，compareBudget 保持为 null，后续 amountAvailable 会设为null（与新增逻辑保持一致）
                        log.info("资产类型场景（更新）：只有付款额科目（A010301150102）没有采购额科目（A01030112），amountAvailable 将设为null：poolId={}", poolId);
                    } else {
                        // 既没有采购额也没有付款额，跳过处理
                        log.warn("跳过处理：poolId={}, mapKey={} 对应的 SYSTEM_PROJECT_BUDGET 数据不存在，既没有采购额科目（A01030112）也没有付款额科目（A010301150102），跳过该记录继续处理其他数据", poolId, mapKey);
                        
                        // 从 poolRelationMap 中查找对应的 budgetSubjectCode
                        String budgetSubjectCode = null;
                        for (Map.Entry<String, Long> poolEntry : poolRelationMap.entrySet()) {
                            if (poolEntry.getValue().equals(poolId)) {
                                String foundKey = poolEntry.getKey();
                                // 解析 key 获取 budgetSubjectCode: year@quarter@isInternal@morgCode@budgetSubjectCode@masterProjectCode@erpAssetType
                                String[] parts = foundKey.split("@");
                                if (parts.length >= 5) {
                                    budgetSubjectCode = parts[4];
                                }
                                break;
                            }
                        }
                        
                        log.warn("跳过的BudgetQuota详细信息：id={}, year={}, quarter={}, poolId={}, projectCode={}, erpAssetType={}, morgCode={}, budgetSubjectCode={}, isInternal={}", 
                                existingQuota.getId(), existingQuota.getYear(), existingQuota.getQuarter(), 
                                existingQuota.getPoolId(), existingQuota.getProjectCode(), existingQuota.getErpAssetType(),
                                existingQuota.getMorgCode(), budgetSubjectCode, existingQuota.getIsInternal());
                        log.warn("查找的mapKey={}, 使用的Map={}", mapKey, "NAN".equals(existingQuota.getProjectCode()) ? "existingBudgetForCompareMap" : "existingBudgetWithoutProjectForCompareMap");
                        log.warn("existingBudgetForCompareMap中poolId={}的所有key={}", poolIdKey, budgetMap != null ? budgetMap.keySet() : "null");
                        // 跳过该记录，继续处理下一条
                        continue;
                    }
                } else {
                    // 非资产类型场景，如果没有采购额，跳过处理
                    log.warn("跳过处理：poolId={}, mapKey={} 对应的 SYSTEM_PROJECT_BUDGET 数据不存在，跳过该记录继续处理其他数据", poolId, mapKey);
                    
                    // 从 poolRelationMap 中查找对应的 budgetSubjectCode
                    String budgetSubjectCode = null;
                    for (Map.Entry<String, Long> poolEntry : poolRelationMap.entrySet()) {
                        if (poolEntry.getValue().equals(poolId)) {
                            String foundKey = poolEntry.getKey();
                            // 解析 key 获取 budgetSubjectCode: year@quarter@isInternal@morgCode@budgetSubjectCode@masterProjectCode@erpAssetType
                            String[] parts = foundKey.split("@");
                            if (parts.length >= 5) {
                                budgetSubjectCode = parts[4];
                            }
                            break;
                        }
                    }
                    
                    log.warn("跳过的BudgetQuota详细信息：id={}, year={}, quarter={}, poolId={}, projectCode={}, erpAssetType={}, morgCode={}, budgetSubjectCode={}, isInternal={}", 
                            existingQuota.getId(), existingQuota.getYear(), existingQuota.getQuarter(), 
                            existingQuota.getPoolId(), existingQuota.getProjectCode(), existingQuota.getErpAssetType(),
                            existingQuota.getMorgCode(), budgetSubjectCode, existingQuota.getIsInternal());
                    log.warn("查找的mapKey={}, 使用的Map={}", mapKey, "NAN".equals(existingQuota.getProjectCode()) ? "existingBudgetForCompareMap" : "existingBudgetWithoutProjectForCompareMap");
                    log.warn("existingBudgetForCompareMap中poolId={}的所有key={}", poolIdKey, budgetMap != null ? budgetMap.keySet() : "null");
                    // 跳过该记录，继续处理下一条
                    continue;
                }
            }

            // 根据 QUARTER 计算比较值
            // 带项目时，如果没有采购额，compareAmount 为 null
            BigDecimal compareAmount = compareBudget != null 
                    ? calculateCompareAmount(existingQuota.getQuarter(), compareBudget)
                    : null;
            BigDecimal existingAmount = existingQuota.getAmountTotal() == null ? BigDecimal.ZERO : existingQuota.getAmountTotal();
            
            // 如果 compareAmount 为 null（没有采购额），需要特殊处理
            if (compareAmount == null) {
                // 带项目或资产类型场景且没有采购额，需要考虑预算调整单的调整金额
                String newVersionId = String.valueOf(identifierGenerator.nextId(null));
                
                // 检查PROJECT_CD是否变更（带项目场景，即使没有采购额也需要更新项目编码）
                String newProjectCode = null;
                // 检查组织编码是否变更（带项目场景）
                String newMorgCode = null;
                if (!"NAN".equals(existingQuota.getProjectCode())) {
                    // 带项目场景：从 budgetWithoutProjectMap 获取付款额科目，检查 PROJECT 字段
                    SystemProjectBudget paymentBudgetForProjectCheck = budgetWithoutProjectMap != null ? budgetWithoutProjectMap.get("A010301150102") : null;
                    if (paymentBudgetForProjectCheck != null && StringUtils.isNotBlank(paymentBudgetForProjectCheck.getProjectId())) {
                        String newProjectCodeFromPayment = paymentBudgetForProjectCheck.getProject();
                        if (StringUtils.isNotBlank(newProjectCodeFromPayment) && !newProjectCodeFromPayment.equals(existingQuota.getProjectCode())) {
                            newProjectCode = newProjectCodeFromPayment;
                            log.info("PROJECT_CD变更（无采购额场景）：poolId={}, 原PROJECT_CODE={}, 新PROJECT_CODE={}, PROJECT_ID={}", 
                                    poolId, existingQuota.getProjectCode(), newProjectCode, paymentBudgetForProjectCheck.getProjectId());
                        }
                        String newMorgCodeFromPayment = paymentBudgetForProjectCheck.getCustom2();
                        if (StringUtils.isNotBlank(newMorgCodeFromPayment) && !newMorgCodeFromPayment.equals(existingQuota.getMorgCode())) {
                            newMorgCode = newMorgCodeFromPayment;
                            log.info("MORG_CODE变更（无采购额场景）：poolId={}, 原MORG_CODE={}, 新MORG_CODE={}, PROJECT_ID={}",
                                    poolId, existingQuota.getMorgCode(), newMorgCode, paymentBudgetForProjectCheck.getProjectId());
                        }
                    }
                }
                
                BudgetBalance existingBalance = existingBudgetBalanceMap.get(poolId);
                if (existingBalance != null) {
                    BudgetBalance newBalance = new BudgetBalance();
                    BeanUtils.copyProperties(existingBalance, newBalance);
                    newBalance.setVersion(newVersionId);
                    
                    // 如果PROJECT_CD变更，同步更新BUDGET_BALANCE的PROJECT_CODE字段
                    if (newProjectCode != null) {
                        newBalance.setProjectCode(newProjectCode);
                    }
                    // 如果组织编码变更，同步更新BUDGET_BALANCE的MORG_CODE字段
                    if (newMorgCode != null) {
                        newBalance.setMorgCode(newMorgCode);
                    }
                    
                    // 计算新的基础预算值：原始采购额（null）+ 调整金额
                    BigDecimal newBaseAmount = calculateBaseBudgetAmount(null, existingQuota);
                    // 计算旧的基础预算值：原始采购额（旧的，即existingQuota.getAmountTotal()）+ 调整金额
                    BigDecimal oldBaseAmount = calculateBaseBudgetAmount(existingQuota.getAmountTotal(), existingQuota);
                    // 计算新的amountAvailable，保留已使用的金额（冻结、占用、实际）
                    BigDecimal currentAmountAvailable = newBalance.getAmountAvailable();
                    BigDecimal newAmountAvailable = calculateNewAmountAvailable(newBaseAmount, oldBaseAmount, currentAmountAvailable);
                    
                    // 计算变更量
                    BigDecimal current = currentAmountAvailable != null ? currentAmountAvailable : BigDecimal.ZERO;
                    BigDecimal newValue = newAmountAvailable != null ? newAmountAvailable : BigDecimal.ZERO;
                    BigDecimal diff = newValue.subtract(current);
                    
                    if (diff.compareTo(BigDecimal.ZERO) != 0) {
                        newBalance.setAmountAvailableVchanged(diff);
                        newBalance.setAmountAvailable(newAmountAvailable);
                        log.info("没有采购额但存在调整金额：poolId={}, quarter={}, 旧基础预算值={}, 新基础预算值={}, 当前amountAvailable={}, 新amountAvailable={}, 变更量={}", 
                                poolId, existingQuota.getQuarter(), oldBaseAmount, newBaseAmount, currentAmountAvailable, newAmountAvailable, diff);
                    }
                    
                    // 如果是资产类型场景，还需要处理付款额
                    if ("NAN".equals(existingQuota.getProjectCode()) && !"NAN".equals(existingQuota.getErpAssetType())) {
                        // 资产类型场景：获取付款额科目的预算数据
                        SystemProjectBudget paymentBudget = budgetMap.get("A010301150102");
                        BigDecimal paymentCompareAmount = paymentBudget != null 
                                ? calculateCompareAmount(existingQuota.getQuarter(), paymentBudget) 
                                : null;
                        
                        // 计算新的基础付款预算值：原始付款额 + 调整金额（amountPayAdj）
                        BigDecimal newBasePayAmount = calculateBasePayBudgetAmount(paymentCompareAmount, existingQuota);
                        // 计算旧的基础付款预算值：原始付款额（旧的，即existingQuota.getAmountPay()）+ 调整金额
                        BigDecimal oldBasePayAmount = calculateBasePayBudgetAmount(existingQuota.getAmountPay(), existingQuota);
                        // 计算新的amountPayAvailable，保留已使用的金额（冻结、占用、实际）
                        BigDecimal currentPayAvailable = newBalance.getAmountPayAvailable() == null ? BigDecimal.ZERO : newBalance.getAmountPayAvailable();
                        BigDecimal newAmountPayAvailable = calculateNewAmountPayAvailable(newBasePayAmount, oldBasePayAmount, currentPayAvailable);
                        
                        // 计算变更量
                        BigDecimal payDiff = newAmountPayAvailable.subtract(currentPayAvailable);
                        
                        // 预算系统下发的数据必须如实同步，即使付款额变成负数也要更新
                        if (payDiff.compareTo(BigDecimal.ZERO) != 0) {
                            newBalance.setAmountPayAvailableVchanged(payDiff);
                            newBalance.setAmountPayAvailable(newAmountPayAvailable);

                            // 同时更新Quota的amountPay
                            BigDecimal existingAmountPay = existingQuota.getAmountPay() == null ? BigDecimal.ZERO : existingQuota.getAmountPay();
                            BigDecimal quotaPayDiff = (paymentCompareAmount != null ? paymentCompareAmount : BigDecimal.ZERO).subtract(existingAmountPay);

                            BudgetQuota newQuota = new BudgetQuota();
                            BeanUtils.copyProperties(existingQuota, newQuota);
                            newQuota.setVersion(newVersionId);
                            if (quotaPayDiff.compareTo(BigDecimal.ZERO) != 0) {
                                newQuota.setAmountPayVchanged(quotaPayDiff);
                                newQuota.setAmountPay(existingAmountPay.add(quotaPayDiff));
                            }
                            needToUpdateBudgetQuota.add(newQuota);

                            BudgetQuotaHistory quotaHistory = new BudgetQuotaHistory();
                            BeanUtils.copyProperties(existingQuota, quotaHistory);
                            quotaHistory.setId(identifierGenerator.nextId(quotaHistory).longValue());
                            quotaHistory.setQuotaId(existingQuota.getId());
                            quotaHistory.setDeleted(Boolean.FALSE);
                            needToAddBudgetQuotaHistory.add(quotaHistory);

                            if (newAmountPayAvailable.compareTo(BigDecimal.ZERO) < 0) {
                                log.info("资产类型场景付款额调减后变为负数（允许）：poolId={}, quarter={}, 原始付款额={}, 新可支付金额={}, 变更量={}",
                                        poolId, existingQuota.getQuarter(), paymentCompareAmount, newAmountPayAvailable, payDiff);
                            } else {
                                log.info("资产类型场景付款额更新：poolId={}, quarter={}, 原始付款额={}, 调整金额={}, 最终值={}, 变更量={}",
                                        poolId, existingQuota.getQuarter(), paymentCompareAmount,
                                        existingQuota.getAmountPayAdj(), newAmountPayAvailable, payDiff);
                            }
                        }
                    } else if (!"NAN".equals(existingQuota.getProjectCode())) {
                        // 带项目场景：处理付款额和PROJECT_CODE更新
                        // 获取付款额科目的预算数据
                        SystemProjectBudget paymentBudget = budgetWithoutProjectMap != null ? budgetWithoutProjectMap.get("A010301150102") : null;
                        if (paymentBudget != null) {
                            BigDecimal paymentCompareAmount = calculateCompareAmount(existingQuota.getQuarter(), paymentBudget);
                            
                            // 计算新的基础付款预算值：原始付款额 + 调整金额（amountPayAdj）
                            BigDecimal newBasePayAmount = calculateBasePayBudgetAmount(paymentCompareAmount, existingQuota);
                            // 计算旧的基础付款预算值：原始付款额（旧的，即existingQuota.getAmountPay()）+ 调整金额
                            BigDecimal oldBasePayAmount = calculateBasePayBudgetAmount(existingQuota.getAmountPay(), existingQuota);
                            // 计算新的amountPayAvailable，保留已使用的金额（冻结、占用、实际）
                            BigDecimal currentPayAvailable = newBalance.getAmountPayAvailable() == null ? BigDecimal.ZERO : newBalance.getAmountPayAvailable();
                            BigDecimal newAmountPayAvailable = calculateNewAmountPayAvailable(newBasePayAmount, oldBasePayAmount, currentPayAvailable);
                            
                            // 计算变更量
                            BigDecimal payDiff = newAmountPayAvailable.subtract(currentPayAvailable);
                            
                            // 预算系统下发的数据必须如实同步，即使付款额变成负数也要更新
                            if (payDiff.compareTo(BigDecimal.ZERO) != 0) {
                                newBalance.setAmountPayAvailableVchanged(payDiff);
                                newBalance.setAmountPayAvailable(newAmountPayAvailable);
                                if (newAmountPayAvailable.compareTo(BigDecimal.ZERO) < 0) {
                                    log.info("付款额调减后变为负数（允许）：poolId={}, quarter={}, 当前可支付金额={}, 新可支付金额={}, 变更量={}",
                                            poolId, existingQuota.getQuarter(), currentPayAvailable, newAmountPayAvailable, payDiff);
                                }
                            }
                        }

                        // 更新Quota的PROJECT_CODE/MORG_CODE和amountPay（独立于付款额预算检查）
                        BudgetQuota newQuota = new BudgetQuota();
                        BeanUtils.copyProperties(existingQuota, newQuota);
                        newQuota.setVersion(newVersionId);

                        // 如果PROJECT_CD变更，更新PROJECT_CODE字段
                        if (newProjectCode != null) {
                            newQuota.setProjectCode(newProjectCode);
                            // 记录需要更新BUDGET_POOL_DEM_R的MASTER_PROJECT_CODE
                            needToUpdatePoolDemRProjectCode.put(poolId, newProjectCode);
                        }
                        // 如果组织编码变更，更新MORG_CODE字段
                        if (newMorgCode != null) {
                            newQuota.setMorgCode(newMorgCode);
                            needToUpdatePoolDemRMorgCode.put(poolId, newMorgCode);
                        }

                        // 更新付款额（如果有变更）
                        boolean hasPayAmountChange = false;
                        if (paymentBudget != null) {
                            BigDecimal existingAmountPay = existingQuota.getAmountPay() == null ? BigDecimal.ZERO : existingQuota.getAmountPay();
                            BigDecimal paymentCompareAmount = calculateCompareAmount(existingQuota.getQuarter(), paymentBudget);
                            BigDecimal quotaPayDiff = paymentCompareAmount.subtract(existingAmountPay);

                            if (quotaPayDiff.compareTo(BigDecimal.ZERO) != 0) {
                                newQuota.setAmountPayVchanged(quotaPayDiff);
                                newQuota.setAmountPay(existingAmountPay.add(quotaPayDiff));
                                hasPayAmountChange = true;
                            }
                        }

                        // 如果有PROJECT_CODE变更、MORG_CODE变更或付款额变更，需要更新Quota
                        if (newProjectCode != null || newMorgCode != null || hasPayAmountChange) {
                            needToUpdateBudgetQuota.add(newQuota);

                            BudgetQuotaHistory quotaHistory = new BudgetQuotaHistory();
                            BeanUtils.copyProperties(existingQuota, quotaHistory);
                            quotaHistory.setId(identifierGenerator.nextId(quotaHistory).longValue());
                            quotaHistory.setQuotaId(existingQuota.getId());
                            quotaHistory.setDeleted(Boolean.FALSE);
                            needToAddBudgetQuotaHistory.add(quotaHistory);

                            // 记录组织编码变更日志
                            if (newMorgCode != null) {
                                log.info("BUDGET_QUOTA组织编码变更已加入更新队列: poolId={}, quarter={}, 原MORG_CODE={}, 新MORG_CODE={}",
                                        poolId, existingQuota.getQuarter(), existingQuota.getMorgCode(), newMorgCode);
                            }
                        }
                    }
                    
                    needToUpdateBudgetBalance.add(newBalance);
                    
                    BudgetBalanceHistory balanceHistory = new BudgetBalanceHistory();
                    BeanUtils.copyProperties(existingBalance, balanceHistory);
                    balanceHistory.setId(identifierGenerator.nextId(balanceHistory).longValue());
                    balanceHistory.setBalanceId(existingBalance.getId());
                    balanceHistory.setDeleted(Boolean.FALSE);
                    needToAddBudgetBalanceHistory.add(balanceHistory);
                }
                // 跳过后续的比较逻辑，继续处理下一条
                continue;
            }
            
            int compareResult = existingAmount.compareTo(compareAmount);
            
            // 先检查PROJECT_CD是否变更（场景4和5），即使金额差异为0也需要更新项目编码
            String newProjectCode = null;
            // 检查组织编码是否变更（场景4和5），即使金额差异为0也需要更新组织编码
            String newMorgCode = null;
            if (!"NAN".equals(existingQuota.getProjectCode()) && compareBudget != null
                    && StringUtils.isNotBlank(compareBudget.getProjectId())) {
                newProjectCode = compareBudget.getProject();
                if (StringUtils.isNotBlank(newProjectCode) && !newProjectCode.equals(existingQuota.getProjectCode())) {
                    // PROJECT_CD变更，需要更新PROJECT_CODE字段
                    log.info("PROJECT_CD变更：poolId={}, 原PROJECT_CODE={}, 新PROJECT_CODE={}, PROJECT_ID={}",
                            poolId, existingQuota.getProjectCode(), newProjectCode, compareBudget.getProjectId());
                } else {
                    newProjectCode = null; // 没有变更
                }
                String newMorgCodeFromCompare = compareBudget.getCustom2();
                if (StringUtils.isNotBlank(newMorgCodeFromCompare) && !newMorgCodeFromCompare.equals(existingQuota.getMorgCode())) {
                    newMorgCode = newMorgCodeFromCompare;
                    log.info("MORG_CODE变更：poolId={}, 原MORG_CODE={}, 新MORG_CODE={}, PROJECT_ID={}",
                            poolId, existingQuota.getMorgCode(), newMorgCode, compareBudget.getProjectId());
                }
            }
            
            // 计算付款额差异（资产类型或带项目场景）
            BigDecimal payDiff = BigDecimal.ZERO;
            if ("NAN".equals(existingQuota.getProjectCode())) {
                // projectCode 为 NAN，从 budgetMap 获取
                if (!"NAN".equals(existingQuota.getErpAssetType())) {
                    // 获取付款额科目的预算数据
                    SystemProjectBudget paymentBudget = budgetMap.get("A010301150102");
                    if (paymentBudget != null) {
                        BigDecimal paymentCompareAmount = calculateCompareAmount(existingQuota.getQuarter(), paymentBudget);
                        BigDecimal existingAmountPay = existingQuota.getAmountPay() == null ? BigDecimal.ZERO : existingQuota.getAmountPay();
                        // payDiff 为目标值减去当前值：正数表示调增，负数表示调减
                        payDiff = paymentCompareAmount.subtract(existingAmountPay);
                    }
                }
            } else {
                // projectCode 不为空，从 budgetWithoutProjectMap 获取
                // 获取付款额科目的预算数据
                SystemProjectBudget paymentBudget = budgetWithoutProjectMap != null ? budgetWithoutProjectMap.get("A010301150102") : null;
                if (paymentBudget != null) {
                    BigDecimal paymentCompareAmount = calculateCompareAmount(existingQuota.getQuarter(), paymentBudget);
                    BigDecimal existingAmountPay = existingQuota.getAmountPay() == null ? BigDecimal.ZERO : existingQuota.getAmountPay();
                    // payDiff 为目标值减去当前值：正数表示调增，负数表示调减
                    payDiff = paymentCompareAmount.subtract(existingAmountPay);
                }
            }
            
            // 如果金额差异为0且付款额差异为0且没有项目编码变更且没有组织编码变更，跳过更新
            if (compareResult == 0 && payDiff.compareTo(BigDecimal.ZERO) == 0 && newProjectCode == null && newMorgCode == null) {
                // 金额和付款额都没有变化，且项目编码/组织编码也没有变更，跳过
                continue;
            }
            
            if (compareResult < 0) {
                // 金额调大，直接调整
                BigDecimal diff = compareAmount.subtract(existingAmount);
                String newVersionId = String.valueOf(identifierGenerator.nextId(null));
                
                BudgetQuota newQuota = new BudgetQuota();
                BeanUtils.copyProperties(existingQuota, newQuota);
                newQuota.setVersion(newVersionId);
                
                // 如果金额差异不为0，才更新金额
                if (diff.compareTo(BigDecimal.ZERO) != 0) {
                    newQuota.setAmountTotalVchanged(diff);
                    newQuota.setAmountTotal(newQuota.getAmountTotal().add(diff));
                }
                
                // 如果PROJECT_CD变更，更新PROJECT_CODE字段
                if (newProjectCode != null) {
                    newQuota.setProjectCode(newProjectCode);
                    // 记录需要更新BUDGET_POOL_DEM_R的MASTER_PROJECT_CODE
                    needToUpdatePoolDemRProjectCode.put(poolId, newProjectCode);
                }
                // 如果组织编码变更，更新MORG_CODE字段
                if (newMorgCode != null) {
                    newQuota.setMorgCode(newMorgCode);
                    needToUpdatePoolDemRMorgCode.put(poolId, newMorgCode);
                }
                
                // 如果付款额差异不为0，才更新付款额字段
                if (payDiff.compareTo(BigDecimal.ZERO) != 0) {
                    // 如果是资产类型预算或带项目场景，还需要设置付款额字段
                    if (!"NAN".equals(existingQuota.getErpAssetType()) || !"NAN".equals(existingQuota.getProjectCode())) {
                        BigDecimal existingAmountPay = existingQuota.getAmountPay() == null ? BigDecimal.ZERO : existingQuota.getAmountPay();
                        // 设置 Quota 的付款额字段
                        newQuota.setAmountPayVchanged(payDiff);
                        newQuota.setAmountPay(existingAmountPay.add(payDiff));
                    }
                }
                
                needToUpdateBudgetQuota.add(newQuota);
                
                BudgetQuotaHistory quotaHistory = new BudgetQuotaHistory();
                BeanUtils.copyProperties(existingQuota, quotaHistory);
                quotaHistory.setId(identifierGenerator.nextId(quotaHistory).longValue());
                quotaHistory.setQuotaId(existingQuota.getId());
                quotaHistory.setDeleted(Boolean.FALSE);
                needToAddBudgetQuotaHistory.add(quotaHistory);
                
                BudgetBalance existingBalance = existingBudgetBalanceMap.get(poolId);
                if (existingBalance != null) {
                    BudgetBalance newBalance = new BudgetBalance();
                    BeanUtils.copyProperties(existingBalance, newBalance);
                    newBalance.setVersion(newVersionId);
                    
                    // 如果PROJECT_CD变更，同步更新BUDGET_BALANCE的PROJECT_CODE字段
                    if (newProjectCode != null) {
                        newBalance.setProjectCode(newProjectCode);
                    }
                    // 如果组织编码变更，同步更新BUDGET_BALANCE的MORG_CODE字段
                    if (newMorgCode != null) {
                        newBalance.setMorgCode(newMorgCode);
                    }
                    
                    // 计算新的基础预算值：原始采购额（更新后） + 调整金额
                    BigDecimal newBaseAmount = calculateBaseBudgetAmount(compareAmount, newQuota);
                    // 计算旧的基础预算值：原始采购额（旧的，即existingQuota.getAmountTotal()）+ 调整金额
                    BigDecimal oldBaseAmount = calculateBaseBudgetAmount(existingQuota.getAmountTotal(), existingQuota);
                    // 计算新的amountAvailable，保留已使用的金额（冻结、占用、实际）
                    BigDecimal currentAmountAvailable = newBalance.getAmountAvailable();
                    BigDecimal newAmountAvailable = calculateNewAmountAvailable(newBaseAmount, oldBaseAmount, currentAmountAvailable);
                    
                    // 计算变更量
                    BigDecimal current = currentAmountAvailable != null ? currentAmountAvailable : BigDecimal.ZERO;
                    BigDecimal newValue = newAmountAvailable != null ? newAmountAvailable : BigDecimal.ZERO;
                    BigDecimal availableDiff = newValue.subtract(current);
                    
                    if (availableDiff.compareTo(BigDecimal.ZERO) != 0) {
                        newBalance.setAmountAvailableVchanged(availableDiff);
                        newBalance.setAmountAvailable(newAmountAvailable);
                        log.info("金额调增时更新amountAvailable：poolId={}, quarter={}, 旧基础预算值={}, 新基础预算值={}, 当前amountAvailable={}, 新amountAvailable={}, 变更量={}", 
                                poolId, existingQuota.getQuarter(), oldBaseAmount, newBaseAmount, currentAmountAvailable, newAmountAvailable, availableDiff);
                    }
                    
                    // 如果是资产类型预算或带项目场景，还需要调整可支付金额
                    if (!"NAN".equals(existingQuota.getErpAssetType()) || !"NAN".equals(existingQuota.getProjectCode())) {
                        // 获取原始付款额（更新后的值）
                        BigDecimal updatedAmountPay = newQuota.getAmountPay() != null ? newQuota.getAmountPay() : BigDecimal.ZERO;
                        
                        // 计算新的基础付款预算值：原始付款额（更新后） + 调整金额（amountPayAdj）
                        BigDecimal newBasePayAmount = calculateBasePayBudgetAmount(updatedAmountPay, newQuota);
                        // 计算旧的基础付款预算值：原始付款额（旧的，即existingQuota.getAmountPay()）+ 调整金额
                        BigDecimal oldBasePayAmount = calculateBasePayBudgetAmount(existingQuota.getAmountPay(), existingQuota);
                        // 计算新的amountPayAvailable，保留已使用的金额（冻结、占用、实际）
                        BigDecimal currentPayAvailable = newBalance.getAmountPayAvailable() == null ? BigDecimal.ZERO : newBalance.getAmountPayAvailable();
                        BigDecimal newAmountPayAvailable = calculateNewAmountPayAvailable(newBasePayAmount, oldBasePayAmount, currentPayAvailable);
                        
                        // 计算变更量
                        BigDecimal payAvailableDiff = newAmountPayAvailable.subtract(currentPayAvailable);
                        
                        // 预算系统下发的数据必须如实同步，即使付款额变成负数也要更新
                        if (payAvailableDiff.compareTo(BigDecimal.ZERO) != 0) {
                            newBalance.setAmountPayAvailableVchanged(payAvailableDiff);
                            newBalance.setAmountPayAvailable(newAmountPayAvailable);

                            if (newAmountPayAvailable.compareTo(BigDecimal.ZERO) < 0) {
                                log.info("金额调增场景付款额调减后变为负数（允许）：poolId={}, quarter={}, 旧基础付款预算值={}, 新基础付款预算值={}, 当前amountPayAvailable={}, 新amountPayAvailable={}, 变更量={}",
                                        poolId, existingQuota.getQuarter(), oldBasePayAmount, newBasePayAmount, currentPayAvailable, newAmountPayAvailable, payAvailableDiff);
                            } else {
                                log.info("金额调增时更新amountPayAvailable：poolId={}, quarter={}, 旧基础付款预算值={}, 新基础付款预算值={}, 当前amountPayAvailable={}, 新amountPayAvailable={}, 变更量={}",
                                        poolId, existingQuota.getQuarter(), oldBasePayAmount, newBasePayAmount, currentPayAvailable, newAmountPayAvailable, payAvailableDiff);
                            }
                        }
                    }

                    // 只要有任何变更都需要更新Balance
                    needToUpdateBudgetBalance.add(newBalance);

                    BudgetBalanceHistory balanceHistory = new BudgetBalanceHistory();
                    BeanUtils.copyProperties(existingBalance, balanceHistory);
                    balanceHistory.setId(identifierGenerator.nextId(balanceHistory).longValue());
                    balanceHistory.setBalanceId(existingBalance.getId());
                    balanceHistory.setDeleted(Boolean.FALSE);
                    needToAddBudgetBalanceHistory.add(balanceHistory);

                    if (newMorgCode != null) {
                        log.info("BUDGET_BALANCE组织编码变更已加入更新队列: poolId={}, quarter={}, 原MORG_CODE={}, 新MORG_CODE={}",
                                poolId, existingQuota.getQuarter(), existingBalance.getMorgCode(), newMorgCode);
                    }
                }
            } else if (compareResult > 0) {
                // 金额调小：预算调整场景允许可用额为负，不因“可用金额不足”拦截，直接按新基础值重算 amountAvailable
                BigDecimal budgetDecreaseNeedCheckAmount = existingAmount.subtract(compareAmount);
                
                BudgetBalance existingBalance = existingBudgetBalanceMap.get(poolId);
                if (existingBalance == null) {
                    throw new IllegalStateException("数据有误，没有预算余额的数据：poolId=" + poolId);
                }
                BigDecimal availableAmount = existingBalance.getAmountAvailable() == null ? BigDecimal.ZERO : existingBalance.getAmountAvailable();
                if (budgetDecreaseNeedCheckAmount.compareTo(availableAmount) > 0) {
                    log.info("预算调整同步：poolId={}, 需要减少={}, 当前可用金额={}，允许继续更新（允许可用额为负）", poolId, budgetDecreaseNeedCheckAmount, availableAmount);
                }
                
                // 预算系统下发的数据必须如实同步，不再校验付款额是否充足（允许变为负数）

                // 如果金额差异为0且付款额差异为0且没有项目编码变更且没有组织编码变更，跳过更新
                if (budgetDecreaseNeedCheckAmount.compareTo(BigDecimal.ZERO) == 0
                        && payDiff.compareTo(BigDecimal.ZERO) == 0
                        && newProjectCode == null
                        && newMorgCode == null) {
                    // 金额和付款额都没有变化，且项目编码/组织编码也没有变更，跳过
                    continue;
                }
                
                String newVersionId = String.valueOf(identifierGenerator.nextId(null));
                
                BudgetQuota newQuota = new BudgetQuota();
                BeanUtils.copyProperties(existingQuota, newQuota);
                newQuota.setVersion(newVersionId);
                
                // 如果金额差异不为0，才更新金额
                if (budgetDecreaseNeedCheckAmount.compareTo(BigDecimal.ZERO) != 0) {
                    newQuota.setAmountTotalVchanged(budgetDecreaseNeedCheckAmount.negate());
                    BigDecimal amountTotalForDecrease = newQuota.getAmountTotal();
                    if (amountTotalForDecrease == null) {
                        log.warn("amountTotal为null，按0处理后继续调减：quotaId={}, poolId={}, year={}, quarter={}",
                                existingQuota.getId(), poolId, existingQuota.getYear(), existingQuota.getQuarter());
                        amountTotalForDecrease = BigDecimal.ZERO;
                    }
                    newQuota.setAmountTotal(amountTotalForDecrease.subtract(budgetDecreaseNeedCheckAmount));
                }
                
                // 如果PROJECT_CD变更，更新PROJECT_CODE字段
                if (newProjectCode != null) {
                    newQuota.setProjectCode(newProjectCode);
                    // 记录需要更新BUDGET_POOL_DEM_R的MASTER_PROJECT_CODE
                    needToUpdatePoolDemRProjectCode.put(poolId, newProjectCode);
                }
                // 如果组织编码变更，更新MORG_CODE字段
                if (newMorgCode != null) {
                    newQuota.setMorgCode(newMorgCode);
                    needToUpdatePoolDemRMorgCode.put(poolId, newMorgCode);
                }
                
                // 如果付款额差异不为0，才更新付款额字段
                if (payDiff.compareTo(BigDecimal.ZERO) != 0) {
                    // 如果是资产类型预算，还需要更新 Quota 的付款额字段
                    if (!"NAN".equals(existingQuota.getErpAssetType()) || !"NAN".equals(existingQuota.getProjectCode())) {
                        BigDecimal existingAmountPay = existingQuota.getAmountPay() == null ? BigDecimal.ZERO : existingQuota.getAmountPay();
                        newQuota.setAmountPayVchanged(payDiff);
                        newQuota.setAmountPay(existingAmountPay.add(payDiff));
                    }
                }
                
                needToUpdateBudgetQuota.add(newQuota);
                
                BudgetQuotaHistory quotaHistory = new BudgetQuotaHistory();
                BeanUtils.copyProperties(existingQuota, quotaHistory);
                quotaHistory.setId(identifierGenerator.nextId(quotaHistory).longValue());
                quotaHistory.setQuotaId(existingQuota.getId());
                quotaHistory.setDeleted(Boolean.FALSE);
                needToAddBudgetQuotaHistory.add(quotaHistory);
                
                BudgetBalance newBalance = new BudgetBalance();
                BeanUtils.copyProperties(existingBalance, newBalance);
                newBalance.setVersion(newVersionId);
                
                // 如果PROJECT_CD变更，同步更新BUDGET_BALANCE的PROJECT_CODE字段
                if (newProjectCode != null) {
                    newBalance.setProjectCode(newProjectCode);
                }
                // 如果组织编码变更，同步更新BUDGET_BALANCE的MORG_CODE字段
                if (newMorgCode != null) {
                    newBalance.setMorgCode(newMorgCode);
                }
                
                // 计算新的基础预算值：原始采购额（更新后） + 调整金额
                BigDecimal newBaseAmount = calculateBaseBudgetAmount(compareAmount, newQuota);
                // 计算旧的基础预算值：原始采购额（旧的，即existingQuota.getAmountTotal()）+ 调整金额
                BigDecimal oldBaseAmount = calculateBaseBudgetAmount(existingQuota.getAmountTotal(), existingQuota);
                // 计算新的amountAvailable，保留已使用的金额（冻结、占用、实际）
                BigDecimal currentAmountAvailable = newBalance.getAmountAvailable();
                BigDecimal newAmountAvailable = calculateNewAmountAvailable(newBaseAmount, oldBaseAmount, currentAmountAvailable);
                
                // 计算变更量
                BigDecimal current = currentAmountAvailable != null ? currentAmountAvailable : BigDecimal.ZERO;
                BigDecimal newValue = newAmountAvailable != null ? newAmountAvailable : BigDecimal.ZERO;
                BigDecimal availableDiff = newValue.subtract(current);
                
                if (availableDiff.compareTo(BigDecimal.ZERO) != 0) {
                    newBalance.setAmountAvailableVchanged(availableDiff);
                    newBalance.setAmountAvailable(newAmountAvailable);
                    log.info("金额调减时更新amountAvailable：poolId={}, quarter={}, 旧基础预算值={}, 新基础预算值={}, 当前amountAvailable={}, 新amountAvailable={}, 变更量={}", 
                            poolId, existingQuota.getQuarter(), oldBaseAmount, newBaseAmount, currentAmountAvailable, newAmountAvailable, availableDiff);
                }
                
                // 如果是资产类型预算或带项目场景，还需要调整可支付金额
                if (!"NAN".equals(existingQuota.getErpAssetType()) || !"NAN".equals(existingQuota.getProjectCode())) {
                    // 获取原始付款额（更新后的值）
                    BigDecimal updatedAmountPay = newQuota.getAmountPay() != null ? newQuota.getAmountPay() : BigDecimal.ZERO;

                    // 计算新的基础付款预算值：原始付款额（更新后） + 调整金额（amountPayAdj）
                    BigDecimal newBasePayAmount = calculateBasePayBudgetAmount(updatedAmountPay, newQuota);
                    // 计算旧的基础付款预算值：原始付款额（旧的，即existingQuota.getAmountPay()）+ 调整金额
                    BigDecimal oldBasePayAmount = calculateBasePayBudgetAmount(existingQuota.getAmountPay(), existingQuota);
                    // 计算新的amountPayAvailable，保留已使用的金额（冻结、占用、实际）
                    BigDecimal currentAmountPayAvailable = newBalance.getAmountPayAvailable() == null ? BigDecimal.ZERO : newBalance.getAmountPayAvailable();
                    BigDecimal newAmountPayAvailable = calculateNewAmountPayAvailable(newBasePayAmount, oldBasePayAmount, currentAmountPayAvailable);

                    // 计算变更量
                    BigDecimal payAvailableDiff = newAmountPayAvailable.subtract(currentAmountPayAvailable);

                    // 预算系统下发的数据必须如实同步，即使付款额变成负数也要更新
                    if (payAvailableDiff.compareTo(BigDecimal.ZERO) != 0) {
                        newBalance.setAmountPayAvailableVchanged(payAvailableDiff);
                        newBalance.setAmountPayAvailable(newAmountPayAvailable);
                        log.info("金额调减时更新amountPayAvailable：poolId={}, quarter={}, 旧基础付款预算值={}, 新基础付款预算值={}, 当前amountPayAvailable={}, 新amountPayAvailable={}, 变更量={}",
                                poolId, existingQuota.getQuarter(), oldBasePayAmount, newBasePayAmount, currentAmountPayAvailable, newAmountPayAvailable, payAvailableDiff);
                    }
                }

                // 只要有任何变更都需要更新Balance（不再做复杂判断，只要有变更就更新）
                needToUpdateBudgetBalance.add(newBalance);

                BudgetBalanceHistory balanceHistory = new BudgetBalanceHistory();
                BeanUtils.copyProperties(existingBalance, balanceHistory);
                balanceHistory.setId(identifierGenerator.nextId(balanceHistory).longValue());
                balanceHistory.setBalanceId(existingBalance.getId());
                needToAddBudgetBalanceHistory.add(balanceHistory);

                if (newMorgCode != null) {
                    log.info("BUDGET_BALANCE组织编码变更已加入更新队列(金额调减): poolId={}, quarter={}, 原MORG_CODE={}, 新MORG_CODE={}",
                            poolId, existingQuota.getQuarter(), existingBalance.getMorgCode(), newMorgCode);
                }
            } else {
                // compareResult == 0：金额没有变化，但可能需要更新项目编码或付款额
                // 如果项目编码有变更或付款额有差异，需要更新
                if (newProjectCode != null || newMorgCode != null || payDiff.compareTo(BigDecimal.ZERO) != 0) {
                    String newVersionId = String.valueOf(identifierGenerator.nextId(null));
                    
                    BudgetQuota newQuota = new BudgetQuota();
                    BeanUtils.copyProperties(existingQuota, newQuota);
                    newQuota.setVersion(newVersionId);
                    
                    // 如果PROJECT_CD变更，更新PROJECT_CODE字段
                    if (newProjectCode != null) {
                        newQuota.setProjectCode(newProjectCode);
                        // 记录需要更新BUDGET_POOL_DEM_R的MASTER_PROJECT_CODE
                        needToUpdatePoolDemRProjectCode.put(poolId, newProjectCode);
                    }
                    // 如果组织编码变更，更新MORG_CODE字段
                    if (newMorgCode != null) {
                        newQuota.setMorgCode(newMorgCode);
                        needToUpdatePoolDemRMorgCode.put(poolId, newMorgCode);
                    }
                    
                    // 如果付款额差异不为0，才更新付款额字段
                    if (payDiff.compareTo(BigDecimal.ZERO) != 0) {
                        // 如果是资产类型预算，还需要更新 Quota 的付款额字段
                        if (!"NAN".equals(existingQuota.getErpAssetType()) || !"NAN".equals(existingQuota.getProjectCode())) {
                            BigDecimal existingAmountPay = existingQuota.getAmountPay() == null ? BigDecimal.ZERO : existingQuota.getAmountPay();
                            newQuota.setAmountPayVchanged(payDiff);
                            newQuota.setAmountPay(existingAmountPay.add(payDiff));
                        }
                    }
                    
                    needToUpdateBudgetQuota.add(newQuota);
                    
                    BudgetQuotaHistory quotaHistory = new BudgetQuotaHistory();
                    BeanUtils.copyProperties(existingQuota, quotaHistory);
                    quotaHistory.setId(identifierGenerator.nextId(quotaHistory).longValue());
                    quotaHistory.setQuotaId(existingQuota.getId());
                    quotaHistory.setDeleted(Boolean.FALSE);
                    needToAddBudgetQuotaHistory.add(quotaHistory);
                    
                    BudgetBalance existingBalance = existingBudgetBalanceMap.get(poolId);
                    if (existingBalance != null) {
                        BudgetBalance newBalance = new BudgetBalance();
                        BeanUtils.copyProperties(existingBalance, newBalance);
                        newBalance.setVersion(newVersionId);
                        
                        // 如果PROJECT_CD变更，同步更新BUDGET_BALANCE的PROJECT_CODE字段
                        if (newProjectCode != null) {
                            newBalance.setProjectCode(newProjectCode);
                        }
                        // 如果组织编码变更，同步更新BUDGET_BALANCE的MORG_CODE字段
                        if (newMorgCode != null) {
                            newBalance.setMorgCode(newMorgCode);
                        }
                        
                        // 即使金额没有变化，如果调整金额有变化，也需要更新amountAvailable
                        // 计算新的基础预算值：原始采购额 + 调整金额
                        BigDecimal newBaseAmount = calculateBaseBudgetAmount(compareAmount, newQuota);
                        // 计算旧的基础预算值：原始采购额（旧的，即existingQuota.getAmountTotal()）+ 调整金额
                        BigDecimal oldBaseAmount = calculateBaseBudgetAmount(existingQuota.getAmountTotal(), existingQuota);
                        // 计算新的amountAvailable，保留已使用的金额（冻结、占用、实际）
                        BigDecimal currentAmountAvailable = newBalance.getAmountAvailable();
                        BigDecimal newAmountAvailable = calculateNewAmountAvailable(newBaseAmount, oldBaseAmount, currentAmountAvailable);
                        
                        // 计算变更量
                        BigDecimal current = currentAmountAvailable != null ? currentAmountAvailable : BigDecimal.ZERO;
                        BigDecimal newValue = newAmountAvailable != null ? newAmountAvailable : BigDecimal.ZERO;
                        BigDecimal availableDiff = newValue.subtract(current);
                        
                        if (availableDiff.compareTo(BigDecimal.ZERO) != 0) {
                            newBalance.setAmountAvailableVchanged(availableDiff);
                            newBalance.setAmountAvailable(newAmountAvailable);
                            log.info("金额无变化但调整金额有变化，更新amountAvailable：poolId={}, quarter={}, 旧基础预算值={}, 新基础预算值={}, 当前amountAvailable={}, 新amountAvailable={}, 变更量={}", 
                                    poolId, existingQuota.getQuarter(), oldBaseAmount, newBaseAmount, currentAmountAvailable, newAmountAvailable, availableDiff);
                        }
                        
                        // 如果是资产类型预算或带项目场景，还需要调整可支付金额
                        if (!"NAN".equals(existingQuota.getErpAssetType()) || !"NAN".equals(existingQuota.getProjectCode())) {
                            // 获取原始付款额（更新后的值）
                            BigDecimal updatedAmountPay = newQuota.getAmountPay() != null ? newQuota.getAmountPay() : BigDecimal.ZERO;
                            
                            // 计算新的基础付款预算值：原始付款额（更新后） + 调整金额（amountPayAdj）
                            BigDecimal newBasePayAmount = calculateBasePayBudgetAmount(updatedAmountPay, newQuota);
                            // 计算旧的基础付款预算值：原始付款额（旧的，即existingQuota.getAmountPay()）+ 调整金额
                            BigDecimal oldBasePayAmount = calculateBasePayBudgetAmount(existingQuota.getAmountPay(), existingQuota);
                            // 计算新的amountPayAvailable，保留已使用的金额（冻结、占用、实际）
                            BigDecimal currentAmountPayAvailable = newBalance.getAmountPayAvailable() == null ? BigDecimal.ZERO : newBalance.getAmountPayAvailable();
                            BigDecimal newAmountPayAvailable = calculateNewAmountPayAvailable(newBasePayAmount, oldBasePayAmount, currentAmountPayAvailable);
                            
                            // 计算变更量
                            BigDecimal payAvailableDiff = newAmountPayAvailable.subtract(currentAmountPayAvailable);
                            
                            // 预算系统下发的数据必须如实同步，即使付款额变成负数也要更新
                            if (payAvailableDiff.compareTo(BigDecimal.ZERO) != 0) {
                                newBalance.setAmountPayAvailableVchanged(payAvailableDiff);
                                newBalance.setAmountPayAvailable(newAmountPayAvailable);
                                log.info("金额无变化但付款额有变化，更新amountPayAvailable：poolId={}, quarter={}, 旧基础付款预算值={}, 新基础付款预算值={}, 当前amountPayAvailable={}, 新amountPayAvailable={}, 变更量={}",
                                        poolId, existingQuota.getQuarter(), oldBasePayAmount, newBasePayAmount, currentAmountPayAvailable, newAmountPayAvailable, payAvailableDiff);
                            }
                        }

                        // 只要有任何变更都需要更新Balance
                        needToUpdateBudgetBalance.add(newBalance);

                        BudgetBalanceHistory balanceHistory = new BudgetBalanceHistory();
                        BeanUtils.copyProperties(existingBalance, balanceHistory);
                        balanceHistory.setId(identifierGenerator.nextId(balanceHistory).longValue());
                        balanceHistory.setBalanceId(existingBalance.getId());
                        balanceHistory.setDeleted(Boolean.FALSE);
                        needToAddBudgetBalanceHistory.add(balanceHistory);

                        if (newMorgCode != null) {
                            log.info("BUDGET_BALANCE组织编码变更已加入更新队列(金额无变化): poolId={}, quarter={}, 原MORG_CODE={}, 新MORG_CODE={}",
                                    poolId, existingQuota.getQuarter(), existingBalance.getMorgCode(), newMorgCode);
                        }
                    }
                }
                // 如果金额差异为0且付款额差异为0且没有项目编码变更，不做任何操作（已在上面跳过）
            }
        }

        // 步骤四：批量插入/更新数据（分批处理，避免数据库超时）
        final int batchSize = 500; // 每批处理500条，避免一次性更新太多数据导致超时
        
        // a. 批量更新 BUDGET_QUOTA（分批处理）
        if (!needToUpdateBudgetQuota.isEmpty()) {
            List<BudgetQuota> sortedQuotas = sortQuotasById(needToUpdateBudgetQuota);
            log.info("开始分批更新 BUDGET_QUOTA，共 {} 条，每批 {} 条", sortedQuotas.size(), batchSize);
            for (int i = 0; i < sortedQuotas.size(); i += batchSize) {
                int end = Math.min(i + batchSize, sortedQuotas.size());
                List<BudgetQuota> batch = sortedQuotas.subList(i, end);
                budgetQuotaMapper.updateBatchById(batch);
                log.info("已更新 BUDGET_QUOTA 第 {}-{} 条，共 {} 条", i + 1, end, batch.size());
            }
            log.info("完成批量更新 BUDGET_QUOTA，共 {} 条", sortedQuotas.size());
        }
        
        // b. 批量插入 BUDGET_QUOTA_HISTORY（分批处理）
        if (!needToAddBudgetQuotaHistory.isEmpty()) {
            log.info("开始分批插入 BUDGET_QUOTA_HISTORY，共 {} 条，每批 {} 条", needToAddBudgetQuotaHistory.size(), batchSize);
            for (int i = 0; i < needToAddBudgetQuotaHistory.size(); i += batchSize) {
                int end = Math.min(i + batchSize, needToAddBudgetQuotaHistory.size());
                List<BudgetQuotaHistory> batch = needToAddBudgetQuotaHistory.subList(i, end);
                budgetQuotaHistoryMapper.insertBatch(batch);
                log.info("已插入 BUDGET_QUOTA_HISTORY 第 {}-{} 条，共 {} 条", i + 1, end, batch.size());
            }
            log.info("完成批量插入 BUDGET_QUOTA_HISTORY，共 {} 条", needToAddBudgetQuotaHistory.size());
        }
        
        // c. 批量更新 BUDGET_BALANCE（分批处理）
        if (!needToUpdateBudgetBalance.isEmpty()) {
            List<BudgetBalance> sortedBalances = sortBalancesById(needToUpdateBudgetBalance);
            log.info("开始分批更新 BUDGET_BALANCE，共 {} 条，每批 {} 条", sortedBalances.size(), batchSize);
            for (int i = 0; i < sortedBalances.size(); i += batchSize) {
                int end = Math.min(i + batchSize, sortedBalances.size());
                List<BudgetBalance> batch = sortedBalances.subList(i, end);
                budgetBalanceMapper.updateBatchById(batch);
                log.info("已更新 BUDGET_BALANCE 第 {}-{} 条，共 {} 条", i + 1, end, batch.size());
            }
            log.info("完成批量更新 BUDGET_BALANCE，共 {} 条", sortedBalances.size());
        }
        
        // d. 批量插入 BUDGET_BALANCE_HISTORY（分批处理）
        if (!needToAddBudgetBalanceHistory.isEmpty()) {
            log.info("开始分批插入 BUDGET_BALANCE_HISTORY，共 {} 条，每批 {} 条", needToAddBudgetBalanceHistory.size(), batchSize);
            for (int i = 0; i < needToAddBudgetBalanceHistory.size(); i += batchSize) {
                int end = Math.min(i + batchSize, needToAddBudgetBalanceHistory.size());
                List<BudgetBalanceHistory> batch = needToAddBudgetBalanceHistory.subList(i, end);
                budgetBalanceHistoryMapper.insertBatch(batch);
                log.info("已插入 BUDGET_BALANCE_HISTORY 第 {}-{} 条，共 {} 条", i + 1, end, batch.size());
            }
            log.info("完成批量插入 BUDGET_BALANCE_HISTORY，共 {} 条", needToAddBudgetBalanceHistory.size());
        }
        
        // d-1. 批量更新 BUDGET_POOL_DEM_R 的 MASTER_PROJECT_CODE（PROJECT_CD变更时）
        if (!needToUpdatePoolDemRProjectCode.isEmpty()) {
            log.info("开始批量更新 BUDGET_POOL_DEM_R 的 MASTER_PROJECT_CODE，共 {} 条", needToUpdatePoolDemRProjectCode.size());
            List<BudgetPoolDemR> poolDemRsToUpdate = new ArrayList<>();
            for (Map.Entry<Long, String> entry : needToUpdatePoolDemRProjectCode.entrySet()) {
                Long poolId = entry.getKey();
                String newProjectCode = entry.getValue();
                BudgetPoolDemR poolDemR = budgetPoolDemRMapper.selectById(poolId);
                if (poolDemR != null && !newProjectCode.equals(poolDemR.getMasterProjectCode())) {
                    poolDemR.setMasterProjectCode(newProjectCode);
                    poolDemRsToUpdate.add(poolDemR);
                }
            }
            if (!poolDemRsToUpdate.isEmpty()) {
                // BudgetPoolDemRMapper 没有 updateBatchById 方法，使用循环 updateById
                log.info("开始批量更新 BUDGET_POOL_DEM_R 的 MASTER_PROJECT_CODE，共 {} 条", poolDemRsToUpdate.size());
                for (BudgetPoolDemR poolDemR : poolDemRsToUpdate) {
                    budgetPoolDemRMapper.updateById(poolDemR);
                }
                log.info("完成批量更新 BUDGET_POOL_DEM_R 的 MASTER_PROJECT_CODE，共 {} 条", poolDemRsToUpdate.size());
            }
        }

        // d-2. 批量更新 BUDGET_POOL_DEM_R 的 MORG_CODE（组织编码变更时）
        if (!needToUpdatePoolDemRMorgCode.isEmpty()) {
            log.info("开始批量更新 BUDGET_POOL_DEM_R 的 MORG_CODE，共 {} 条", needToUpdatePoolDemRMorgCode.size());
            List<BudgetPoolDemR> poolDemRsToUpdate = new ArrayList<>();
            for (Map.Entry<Long, String> entry : needToUpdatePoolDemRMorgCode.entrySet()) {
                Long poolId = entry.getKey();
                String newMorgCode = entry.getValue();
                BudgetPoolDemR poolDemR = budgetPoolDemRMapper.selectById(poolId);
                if (poolDemR != null && !newMorgCode.equals(poolDemR.getMorgCode())) {
                    poolDemR.setMorgCode(newMorgCode);
                    poolDemRsToUpdate.add(poolDemR);
                }
            }
            if (!poolDemRsToUpdate.isEmpty()) {
                log.info("开始批量更新 BUDGET_POOL_DEM_R 的 MORG_CODE，共 {} 条", poolDemRsToUpdate.size());
                for (BudgetPoolDemR poolDemR : poolDemRsToUpdate) {
                    budgetPoolDemRMapper.updateById(poolDemR);
                }
                log.info("完成批量更新 BUDGET_POOL_DEM_R 的 MORG_CODE，共 {} 条", poolDemRsToUpdate.size());
            }
        }
        
        // e. 处理新增数据
        // 批量提取需要映射的 erpAssetType 字段（以 "1" 或 "M" 开头且不带项目的）
        Set<String> erpAssetTypeSet = new HashSet<>();
        for (String poolRelationKey : needToAddBudgetMap.keySet()) {
            // 解析 poolRelationKey: year@quarter@isInternal@morgCode@budgetSubjectCode@masterProjectCode@erpAssetType
            String[] parts = poolRelationKey.split("@");
            if (parts.length >= 7) {
                String masterProjectCode = parts[5];
                String originalErpAssetType = parts[6];
                // 只提取不带项目的明细的 erpAssetType
                boolean isNoProject = "NAN".equals(masterProjectCode) || StringUtils.isBlank(masterProjectCode);
                if (isNoProject && StringUtils.isNotBlank(originalErpAssetType) && !"NAN".equals(originalErpAssetType)
                        && (originalErpAssetType.startsWith("1") || originalErpAssetType.startsWith("M"))) {
                    erpAssetTypeSet.add(originalErpAssetType);
                }
            }
        }
        
        // 批量查询 VIEW_BUDGET_MEMBER_NAME_CODE 视图，获取 MEMBER_CD2 对应的 MEMBER_CD（一一对应关系）
        Map<String, String> erpAssetTypeToMemberCdMap = budgetQueryHelperService.queryErpAssetTypeToMemberCdMap(erpAssetTypeSet);
        log.info("========== 获取到 erpAssetType 映射表，共 {} 条记录 ==========", erpAssetTypeToMemberCdMap.size());
        
        List<BudgetPoolDemR> newPoolDemRList = new ArrayList<>();
        List<BudgetQuota> newBudgetQuotaList = new ArrayList<>();
        List<BudgetBalance> newBudgetBalanceList = new ArrayList<>();
        
        for (Map.Entry<String, Map<String, SystemProjectBudget>> entry : needToAddBudgetMap.entrySet()) {
            String poolRelationKey = entry.getKey();
            Map<String, SystemProjectBudget> budgetMap = entry.getValue();
            
            // 解析 poolRelationKey: year@quarter@isInternal@morgCode@budgetSubjectCode@masterProjectCode@erpAssetType
            String[] parts = poolRelationKey.split("@");
            String budgetYear = parts[0];
            String quarter = parts[1];
            String isInternal = parts[2];
            String morgCode = parts[3];
            String budgetSubjectCode = parts[4];
            String masterProjectCode = parts[5];
            String originalErpAssetType = parts[6];
            
            // 映射 erpAssetType：如果以 "1" 或 "M" 开头，需要通过映射表映射
            BudgetQueryHelperService.MapErpAssetTypeResult erpAssetTypeResult = budgetQueryHelperService.mapErpAssetTypeForQuery(
                    originalErpAssetType, masterProjectCode, erpAssetTypeToMemberCdMap,
                    "创建 BudgetPoolDemR 时，poolRelationKey [" + poolRelationKey + "]");
            if (erpAssetTypeResult.hasError()) {
                throw new IllegalStateException(erpAssetTypeResult.getErrorMessage());
            }
            String erpAssetType = erpAssetTypeResult.getMappedValue();
            
            // 获取主预算数据：根据 erpAssetType 决定使用哪个 key
            SystemProjectBudget mainBudget;
            SystemProjectBudget paymentBudget = null; // 付款额预算（资产类型场景使用）
            if ("NAN".equals(erpAssetType)) {
                // 没有资产类型，使用 "default"
                mainBudget = budgetMap.get("default");
            } else {
                // 有资产类型，获取采购额科目（A01030112）
                mainBudget = budgetMap.get("A01030112");
                // 获取付款额科目（A010301150102）
                paymentBudget = budgetMap.get("A010301150102");
                
                // 资产类型场景：如果采购额不存在，检查是否有付款额
                if (mainBudget == null) {
                    if (paymentBudget == null) {
                        // 既没有采购额也没有付款额，跳过
                        log.warn("找不到对应的预算数据（既没有采购额也没有付款额）：poolRelationKey={}, budgetMap keys={}", poolRelationKey, budgetMap.keySet());
                        continue;
                    } else {
                        // 只有付款额没有采购额，mainBudget保持为null，继续处理
                        log.info("资产类型场景：只有付款额科目（A010301150102）没有采购额科目（A01030112），amountAvailable 将设为null：poolRelationKey={}", poolRelationKey);
                    }
                }
            }
            
            // 非资产类型场景：如果没有主预算数据，跳过
            if (mainBudget == null && "NAN".equals(erpAssetType)) {
                log.warn("找不到对应的预算数据：poolRelationKey={}, budgetMap keys={}", poolRelationKey, budgetMap.keySet());
                continue;
            }
            
            // 为每个预算数据生成独立的版本号
            String needToAddBudgetVersion = String.valueOf(identifierGenerator.nextId(null));
            
            // 生成预算额度ID
            Long needToAddBudgetQuota = identifierGenerator.nextId(null).longValue();
            
            // 组装 BudgetPoolDemR
            BudgetPoolDemR poolDemR = new BudgetPoolDemR();
            poolDemR.setId(identifierGenerator.nextId(poolDemR).longValue());
            poolDemR.setYear(budgetYear);
            poolDemR.setQuarter(quarter);
            poolDemR.setIsInternal(isInternal);
            poolDemR.setMorgCode(morgCode);
            poolDemR.setBudgetSubjectCode(budgetSubjectCode);
            poolDemR.setMasterProjectCode(masterProjectCode);
            // 设置PROJECT_ID（场景4和5，不带项目的场景通常没有PROJECT_ID）
            // 如果mainBudget为null，尝试从paymentBudget获取（资产类型场景）
            if (mainBudget != null && StringUtils.isNotBlank(mainBudget.getProjectId()) && !"NAN".equals(masterProjectCode)) {
                poolDemR.setProjectId(mainBudget.getProjectId());
            } else if (mainBudget == null && paymentBudget != null && StringUtils.isNotBlank(paymentBudget.getProjectId()) && !"NAN".equals(masterProjectCode)) {
                poolDemR.setProjectId(paymentBudget.getProjectId());
            }
            poolDemR.setErpAssetType(erpAssetType);
            poolDemR.setDeleted(Boolean.FALSE);
            newPoolDemRList.add(poolDemR);
            
            // 根据季度获取对应的金额（采购额）
            BigDecimal quarterAmount;
            if (mainBudget != null) {
                if (QUARTER_Q1.equals(quarter)) {
                    quarterAmount = mainBudget.getQ1() == null ? BigDecimal.ZERO : mainBudget.getQ1();
                } else if (QUARTER_Q2.equals(quarter)) {
                    quarterAmount = mainBudget.getQ2() == null ? BigDecimal.ZERO : mainBudget.getQ2();
                } else if (QUARTER_Q3.equals(quarter)) {
                    quarterAmount = mainBudget.getQ3() == null ? BigDecimal.ZERO : mainBudget.getQ3();
                } else {
                    quarterAmount = mainBudget.getQ4() == null ? BigDecimal.ZERO : mainBudget.getQ4();
                }
            } else {
                // 没有采购额，quarterAmount 为null
                quarterAmount = null;
            }
            
            // 组装 BudgetQuota
            BudgetQuota quota = new BudgetQuota();
            quota.setId(needToAddBudgetQuota);
            quota.setPoolId(poolDemR.getId());
            quota.setMorgCode(poolDemR.getMorgCode());
            quota.setProjectCode(poolDemR.getMasterProjectCode());
            quota.setIsInternal(poolDemR.getIsInternal());
            // 如果有资产类型，customCode 和 accountSubjectCode 设置为 NAN（两者互斥）
            if ("NAN".equals(erpAssetType)) {
                quota.setCustomCode(mainBudget.getCustom1());
                quota.setAccountSubjectCode(mainBudget.getAccount());
            } else {
                quota.setCustomCode("NAN");
                quota.setAccountSubjectCode("NAN");
            }
            quota.setErpAssetType(poolDemR.getErpAssetType());
            quota.setYear(budgetYear);
            quota.setQuarter(quarter);
            quota.setBudgetType(null); // 先留空
            quota.setCurrency(DEFAULT_CURRENCY);
            quota.setVersion(needToAddBudgetVersion);
            quota.setAmountTotal(quarterAmount);
            quota.setAmountTotalVchanged(quarterAmount);
            quota.setAmountAdj(BigDecimal.ZERO);
            
            // 如果是资产类型预算，还需要设置 amountPay
            if (!"NAN".equals(erpAssetType)) {
                // paymentBudget 已经在上面获取了
                if (paymentBudget != null) {
                    BigDecimal paymentAmount;
                    if (QUARTER_Q1.equals(quarter)) {
                        paymentAmount = paymentBudget.getQ1() == null ? BigDecimal.ZERO : paymentBudget.getQ1();
                    } else if (QUARTER_Q2.equals(quarter)) {
                        paymentAmount = paymentBudget.getQ2() == null ? BigDecimal.ZERO : paymentBudget.getQ2();
                    } else if (QUARTER_Q3.equals(quarter)) {
                        paymentAmount = paymentBudget.getQ3() == null ? BigDecimal.ZERO : paymentBudget.getQ3();
                    } else {
                        paymentAmount = paymentBudget.getQ4() == null ? BigDecimal.ZERO : paymentBudget.getQ4();
                    }
                    quota.setAmountPay(paymentAmount);
                    quota.setAmountPayVchanged(paymentAmount);
                } else {
                    // 没有付款额，amountPay 设置为 0
                    quota.setAmountPay(BigDecimal.ZERO);
                    quota.setAmountPayVchanged(BigDecimal.ZERO);
                }
            } else {
                // 非资产类型预算，amountPay 设置为 0
                quota.setAmountPay(BigDecimal.ZERO);
                quota.setAmountPayVchanged(BigDecimal.ZERO);
            }
            
            quota.setDeleted(Boolean.FALSE);
            newBudgetQuotaList.add(quota);
            
            // 组装 BudgetBalance (id 留空，插入时自动生成)
            BudgetBalance balance = new BudgetBalance();
            balance.setPoolId(poolDemR.getId());
            balance.setQuotaId(needToAddBudgetQuota);
            balance.setMorgCode(poolDemR.getMorgCode());
            balance.setProjectCode(poolDemR.getMasterProjectCode());
            balance.setIsInternal(poolDemR.getIsInternal());
            // 如果有资产类型，customCode 和 accountSubjectCode 设置为 NAN（两者互斥）
            if ("NAN".equals(erpAssetType)) {
                balance.setCustomCode(mainBudget.getCustom1());
                balance.setAccountSubjectCode(mainBudget.getAccount());
            } else {
                balance.setCustomCode("NAN");
                balance.setAccountSubjectCode("NAN");
            }
            balance.setErpAssetType(poolDemR.getErpAssetType());
            balance.setYear(budgetYear);
            balance.setQuarter(quarter);
            balance.setCurrency(DEFAULT_CURRENCY);
            balance.setVersion(needToAddBudgetVersion);
            balance.setAmountFrozen(BigDecimal.ZERO);
            balance.setAmountFrozenVchanged(BigDecimal.ZERO);
            balance.setAmountOccupied(BigDecimal.ZERO);
            balance.setAmountOccupiedVchanged(BigDecimal.ZERO);
            // 默认实际金额置零（资产类型使用可支付金额字段）
            balance.setAmountActual(BigDecimal.ZERO);
            balance.setAmountActualVchanged(BigDecimal.ZERO);
            
            // 设置 amountAvailable：如果采购额存在，使用采购额；如果采购额不存在，设为null
            balance.setAmountAvailable(quarterAmount);
            balance.setAmountAvailableVchanged(quarterAmount);
            
            // 如果是资产类型预算，还需要设置可支付金额（从付款额科目获取）
            if (!"NAN".equals(erpAssetType)) {
                // paymentBudget 已经在上面获取了
                if (paymentBudget != null) {
                    BigDecimal paymentAmount;
                    if (QUARTER_Q1.equals(quarter)) {
                        paymentAmount = paymentBudget.getQ1() == null ? null : paymentBudget.getQ1();
                    } else if (QUARTER_Q2.equals(quarter)) {
                        paymentAmount = paymentBudget.getQ2() == null ? null : paymentBudget.getQ2();
                    } else if (QUARTER_Q3.equals(quarter)) {
                        paymentAmount = paymentBudget.getQ3() == null ? null : paymentBudget.getQ3();
                    } else {
                        paymentAmount = paymentBudget.getQ4() == null ? null : paymentBudget.getQ4();
                    }
                    // 付款额有（包括0）→ 设为具体值；付款额没有（null）→ 设为null
                    balance.setAmountPayAvailable(paymentAmount);
                    balance.setAmountPayAvailableVchanged(paymentAmount);
                } else {
                    // 没有付款额，设为null
                    balance.setAmountPayAvailable(null);
                    balance.setAmountPayAvailableVchanged(null);
                }
            } else {
                balance.setAmountPayAvailable(BigDecimal.ZERO);
                balance.setAmountPayAvailableVchanged(BigDecimal.ZERO);
            }
            
            balance.setDeleted(Boolean.FALSE);
            newBudgetBalanceList.add(balance);
        }

        // e-1. 处理新增数据（带项目编码的场景）
        // 注意：带项目的场景不需要映射 erpAssetType，直接使用 "NAN"
        for (Map.Entry<String, Map<String, SystemProjectBudget>> entry : needToAddBudgetProjectMap.entrySet()) {
            String poolRelationKey = entry.getKey();
            Map<String, SystemProjectBudget> budgetWithProMap = entry.getValue();

            // 解析 poolRelationKey: year@quarter@isInternal@morgCode@budgetSubjectCode@masterProjectCode@erpAssetType
            String[] parts = poolRelationKey.split("@");
            String budgetYear = parts[0];
            String quarter = parts[1];
            String isInternal = parts[2];
            String morgCode = parts[3];
            String budgetSubjectCode = parts[4];
            String masterProjectCode = parts[5];
            String originalErpAssetType = parts[6];
            
            // 带项目的场景，erpAssetType 直接使用 "NAN"（不需要映射）
            String erpAssetType = "NAN";

            // 获取主预算数据（带项目的场景只使用采购额科目 A01030115010102，付款额不能用于 amountAvailable）
            SystemProjectBudget mainBudget = budgetWithProMap.get("A01030115010102");
            if (mainBudget == null) {
                // 如果采购额科目不存在，检查是否有付款额科目
                SystemProjectBudget paymentBudget = budgetWithProMap.get("A010301150102");
                if (paymentBudget == null) {
                    log.warn("找不到对应的预算数据：poolRelationKey={}, 既没有采购额科目（A01030115010102）也没有付款额科目（A010301150102）, budgetWithProMap keys={}", poolRelationKey, budgetWithProMap.keySet());
                    continue;
                }
                // 只有付款额没有采购额，mainBudget 为 null，后续 amountAvailable 会设为null
                log.info("带项目场景：只有付款额科目（A010301150102）没有采购额科目（A01030115010102），amountAvailable 将设为null：poolRelationKey={}", poolRelationKey);
            }

            // 为每个预算数据生成独立的版本号
            String needToAddBudgetVersion = String.valueOf(identifierGenerator.nextId(null));

            // 生成预算额度ID
            Long needToAddBudgetQuota = identifierGenerator.nextId(null).longValue();

            // 组装 BudgetPoolDemR
            BudgetPoolDemR poolDemR = new BudgetPoolDemR();
            poolDemR.setId(identifierGenerator.nextId(poolDemR).longValue());
            poolDemR.setYear(budgetYear);
            poolDemR.setQuarter(quarter);
            poolDemR.setIsInternal(isInternal);
            poolDemR.setMorgCode(morgCode);
            poolDemR.setBudgetSubjectCode(budgetSubjectCode);
            poolDemR.setMasterProjectCode(masterProjectCode);
            // 设置PROJECT_ID（场景4和5）
            if (mainBudget != null && StringUtils.isNotBlank(mainBudget.getProjectId()) && !"NAN".equals(masterProjectCode)) {
                poolDemR.setProjectId(mainBudget.getProjectId());
            } else if (budgetWithProMap != null && !budgetWithProMap.isEmpty()) {
                // 如果mainBudget为null，尝试从budgetWithProMap中获取PROJECT_ID
                SystemProjectBudget anyBudget = budgetWithProMap.values().iterator().next();
                if (anyBudget != null && StringUtils.isNotBlank(anyBudget.getProjectId()) && !"NAN".equals(masterProjectCode)) {
                    poolDemR.setProjectId(anyBudget.getProjectId());
                }
            }
            poolDemR.setErpAssetType(erpAssetType);
            poolDemR.setDeleted(Boolean.FALSE);
            newPoolDemRList.add(poolDemR);

            // 根据季度获取对应的金额（只有采购额科目存在时才获取，否则为null）
            BigDecimal quarterAmount;
            if (mainBudget != null) {
                if (QUARTER_Q1.equals(quarter)) {
                    quarterAmount = mainBudget.getQ1() == null ? BigDecimal.ZERO : mainBudget.getQ1();
                } else if (QUARTER_Q2.equals(quarter)) {
                    quarterAmount = mainBudget.getQ2() == null ? BigDecimal.ZERO : mainBudget.getQ2();
                } else if (QUARTER_Q3.equals(quarter)) {
                    quarterAmount = mainBudget.getQ3() == null ? BigDecimal.ZERO : mainBudget.getQ3();
                } else {
                    quarterAmount = mainBudget.getQ4() == null ? BigDecimal.ZERO : mainBudget.getQ4();
                }
            } else {
                // 没有采购额科目，quarterAmount 为null
                quarterAmount = null;
            }

            // 组装 BudgetQuota
            BudgetQuota quota = new BudgetQuota();
            quota.setId(needToAddBudgetQuota);
            quota.setPoolId(poolDemR.getId());
            quota.setMorgCode(poolDemR.getMorgCode());
            quota.setProjectCode(poolDemR.getMasterProjectCode());
            quota.setIsInternal(poolDemR.getIsInternal());
            quota.setCustomCode("NAN");
            quota.setAccountSubjectCode("NAN");
            quota.setErpAssetType(poolDemR.getErpAssetType());
            quota.setYear(budgetYear);
            quota.setQuarter(quarter);
            quota.setBudgetType(null); // 先留空
            quota.setCurrency(DEFAULT_CURRENCY);
            quota.setVersion(needToAddBudgetVersion);
            quota.setAmountTotal(quarterAmount);
            quota.setAmountTotalVchanged(quarterAmount);
            quota.setAmountAdj(BigDecimal.ZERO);

            // 带项目编码：设置 amountPay（从付款额科目 A010301150102 获取）
            SystemProjectBudget paymentBudget = budgetWithProMap.get("A010301150102");
            if (paymentBudget != null) {
                BigDecimal paymentAmount;
                if (QUARTER_Q1.equals(quarter)) {
                    paymentAmount = paymentBudget.getQ1() == null ? BigDecimal.ZERO : paymentBudget.getQ1();
                } else if (QUARTER_Q2.equals(quarter)) {
                    paymentAmount = paymentBudget.getQ2() == null ? BigDecimal.ZERO : paymentBudget.getQ2();
                } else if (QUARTER_Q3.equals(quarter)) {
                    paymentAmount = paymentBudget.getQ3() == null ? BigDecimal.ZERO : paymentBudget.getQ3();
                } else {
                    paymentAmount = paymentBudget.getQ4() == null ? BigDecimal.ZERO : paymentBudget.getQ4();
                }
                quota.setAmountPay(paymentAmount);
                quota.setAmountPayVchanged(paymentAmount);
            } else {
                quota.setAmountPay(BigDecimal.ZERO);
                quota.setAmountPayVchanged(BigDecimal.ZERO);
            }

            quota.setDeleted(Boolean.FALSE);
            newBudgetQuotaList.add(quota);

            // 组装 BudgetBalance (id 留空，插入时自动生成)
            BudgetBalance balance = new BudgetBalance();
            balance.setPoolId(poolDemR.getId());
            balance.setQuotaId(needToAddBudgetQuota);
            balance.setMorgCode(poolDemR.getMorgCode());
            balance.setProjectCode(poolDemR.getMasterProjectCode());
            balance.setIsInternal(poolDemR.getIsInternal());
            balance.setCustomCode("NAN");
            balance.setAccountSubjectCode("NAN");
            balance.setErpAssetType(poolDemR.getErpAssetType());
            balance.setYear(budgetYear);
            balance.setQuarter(quarter);
            balance.setCurrency(DEFAULT_CURRENCY);
            balance.setVersion(needToAddBudgetVersion);
            balance.setAmountFrozen(BigDecimal.ZERO);
            balance.setAmountFrozenVchanged(BigDecimal.ZERO);
            balance.setAmountOccupied(BigDecimal.ZERO);
            balance.setAmountOccupiedVchanged(BigDecimal.ZERO);
            balance.setAmountActual(BigDecimal.ZERO);
            balance.setAmountActualVchanged(BigDecimal.ZERO);
            // 带项目编码：amountAvailable 只使用采购额，如果没有采购额则设为null
            balance.setAmountAvailable(quarterAmount);
            balance.setAmountAvailableVchanged(quarterAmount);

            // 带项目编码：设置可支付金额（从付款额科目 A010301150102 获取）
            SystemProjectBudget paymentBudgetForBalance = budgetWithProMap.get("A010301150102");
            if (paymentBudgetForBalance != null) {
                BigDecimal paymentAmount;
                if (QUARTER_Q1.equals(quarter)) {
                    paymentAmount = paymentBudgetForBalance.getQ1() == null ? BigDecimal.ZERO : paymentBudgetForBalance.getQ1();
                } else if (QUARTER_Q2.equals(quarter)) {
                    paymentAmount = paymentBudgetForBalance.getQ2() == null ? BigDecimal.ZERO : paymentBudgetForBalance.getQ2();
                } else if (QUARTER_Q3.equals(quarter)) {
                    paymentAmount = paymentBudgetForBalance.getQ3() == null ? BigDecimal.ZERO : paymentBudgetForBalance.getQ3();
                } else {
                    paymentAmount = paymentBudgetForBalance.getQ4() == null ? BigDecimal.ZERO : paymentBudgetForBalance.getQ4();
                }
                balance.setAmountPayAvailable(paymentAmount);
                balance.setAmountPayAvailableVchanged(paymentAmount);
            } else {
                balance.setAmountPayAvailable(BigDecimal.ZERO);
                balance.setAmountPayAvailableVchanged(BigDecimal.ZERO);
            }

            balance.setDeleted(Boolean.FALSE);
            newBudgetBalanceList.add(balance);
        }
        
        // 批量插入新数据（分批处理，避免数据库超时）
        if (!newPoolDemRList.isEmpty()) {
            log.info("开始分批插入 BUDGET_POOL_DEM_R，共 {} 条，每批 {} 条", newPoolDemRList.size(), batchSize);
            for (int i = 0; i < newPoolDemRList.size(); i += batchSize) {
                int end = Math.min(i + batchSize, newPoolDemRList.size());
                List<BudgetPoolDemR> batch = newPoolDemRList.subList(i, end);
                budgetPoolDemRMapper.insertBatch(batch);
                log.info("已插入 BUDGET_POOL_DEM_R 第 {}-{} 条，共 {} 条", i + 1, end, batch.size());
            }
            log.info("完成批量插入 BUDGET_POOL_DEM_R，共 {} 条", newPoolDemRList.size());
        }
        if (!newBudgetQuotaList.isEmpty()) {
            log.info("开始分批插入 BUDGET_QUOTA（新增），共 {} 条，每批 {} 条", newBudgetQuotaList.size(), batchSize);
            for (int i = 0; i < newBudgetQuotaList.size(); i += batchSize) {
                int end = Math.min(i + batchSize, newBudgetQuotaList.size());
                List<BudgetQuota> batch = newBudgetQuotaList.subList(i, end);
                budgetQuotaMapper.insertBatch(batch);
                log.info("已插入 BUDGET_QUOTA（新增）第 {}-{} 条，共 {} 条", i + 1, end, batch.size());
            }
            log.info("完成批量插入 BUDGET_QUOTA（新增），共 {} 条", newBudgetQuotaList.size());
        }
        if (!newBudgetBalanceList.isEmpty()) {
            log.info("开始分批插入 BUDGET_BALANCE（新增），共 {} 条，每批 {} 条", newBudgetBalanceList.size(), batchSize);
            for (int i = 0; i < newBudgetBalanceList.size(); i += batchSize) {
                int end = Math.min(i + batchSize, newBudgetBalanceList.size());
                List<BudgetBalance> batch = newBudgetBalanceList.subList(i, end);
                budgetBalanceMapper.insertBatch(batch);
                log.info("已插入 BUDGET_BALANCE（新增）第 {}-{} 条，共 {} 条", i + 1, end, batch.size());
            }
            log.info("完成批量插入 BUDGET_BALANCE（新增），共 {} 条", newBudgetBalanceList.size());
        }

        return String.format("同步完成：更新配额 %d 条，新增配额 %d 条，更新余额 %d 条，新增余额 %d 条",
                needToUpdateBudgetQuota.size(), newBudgetQuotaList.size(),
                needToUpdateBudgetBalance.size(), newBudgetBalanceList.size());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String complementProjectId(String year) {
        log.info("开始补足 BUDGET_POOL_DEM_R 的 project_id，year={}", year);

        // 1. 查询 project_id 为空的 pool（预算调整单等创建的）
        LambdaQueryWrapper<BudgetPoolDemR> poolWrapper = new LambdaQueryWrapper<>();
        poolWrapper.eq(BudgetPoolDemR::getDeleted, Boolean.FALSE)
                .eq(BudgetPoolDemR::getYear, year)
                .and(w -> w.isNull(BudgetPoolDemR::getProjectId).or().eq(BudgetPoolDemR::getProjectId, ""));
        List<BudgetPoolDemR> poolsWithoutProjectId = budgetPoolDemRMapper.selectList(poolWrapper);
        if (poolsWithoutProjectId.isEmpty()) {
            log.info("无 project_id 为空的 pool，无需补足");
            return "补足 project_id 完成：无待补足数据";
        }

        // 2. 从 SYSTEM_PROJECT_BUDGET 构建 project_code -> project_id 映射
        // 说明：补足 project_id 的目的，是为了后续按 project_id 同步/纠正项目编码、组织等维度，因此这里不应把 morgCode 作为匹配条件
        List<SystemProjectBudget> budgets = loadOriginalBudgets(year);
        Map<String, String> projectCodeToProjectId = new HashMap<>();
        Set<String> ambiguousProjectCodes = new HashSet<>();
        for (SystemProjectBudget budget : budgets) {
            if (StringUtils.isNotBlank(budget.getProject()) && StringUtils.isNotBlank(budget.getProjectId())
                    && !"NAN".equals(budget.getProject())) {
                String projectCode = budget.getProject();
                String projectId = budget.getProjectId();
                String existing = projectCodeToProjectId.get(projectCode);
                if (existing == null) {
                    projectCodeToProjectId.put(projectCode, projectId);
                } else if (!StringUtils.equals(existing, projectId)) {
                    ambiguousProjectCodes.add(projectCode);
                }
            }
        }

        // 3. 按 project_code 匹配，只更新 project_id
        int updated = 0;
        int skippedAmbiguous = 0;
        for (BudgetPoolDemR pool : poolsWithoutProjectId) {
            if (StringUtils.isBlank(pool.getMasterProjectCode()) || "NAN".equals(pool.getMasterProjectCode())) {
                continue;
            }
            String projectCode = pool.getMasterProjectCode();
            if (ambiguousProjectCodes.contains(projectCode)) {
                skippedAmbiguous++;
                log.warn("补足 project_id 跳过：SYSTEM_PROJECT_BUDGET 中同一项目编码存在多个 project_id，masterProjectCode={}, poolId={}",
                        projectCode, pool.getId());
                continue;
            }
            String projectId = projectCodeToProjectId.get(projectCode);
            if (StringUtils.isNotBlank(projectId)) {
                pool.setProjectId(projectId);
                budgetPoolDemRMapper.updateById(pool);
                updated++;
                log.debug("补足 project_id: poolId={}, masterProjectCode={}, projectId={}",
                        pool.getId(), projectCode, projectId);
            }
        }

        log.info("补足 project_id 完成，year={}，待补足 {} 条，实际更新 {} 条，跳过歧义项目编码 {} 条",
                year, poolsWithoutProjectId.size(), updated, skippedAmbiguous);
        return String.format("补足 project_id 完成：待补足 %d 条，实际更新 %d 条，跳过歧义 %d 条",
                poolsWithoutProjectId.size(), updated, skippedAmbiguous);
    }

    /**
     * 构建 project_code + morgCode 的 key（用于补足 project_id 时的匹配）
     */
    private String buildProjectCodeMorgKey(String projectCode, String morgCode) {
        return (projectCode != null ? projectCode : "") + "@" + (morgCode != null ? morgCode : "NAN");
    }

    /**
     * 构建预算池关系 Map
     */
    private Map<String, Long> buildPoolRelationMap() {
        LambdaQueryWrapper<BudgetPoolDemR> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BudgetPoolDemR::getDeleted, Boolean.FALSE);
        List<BudgetPoolDemR> list = budgetPoolDemRMapper.selectList(wrapper);
        
        Map<String, Long> map = new HashMap<>();
        for (BudgetPoolDemR relation : list) {
            String key = buildPoolRelationKey(relation.getYear(), relation.getQuarter(),
                    relation.getIsInternal(),
                    relation.getMorgCode(), 
                    relation.getBudgetSubjectCode(), relation.getMasterProjectCode(),
                    relation.getErpAssetType());
            map.put(key, relation.getId());
        }
        return map;
    }

    /**
     * 构建 PROJECT_ID 到 poolId 的映射（用于场景4和5，支持PROJECT_CD变更）
     * 返回 Map<PROJECT_ID, Map<QUARTER, POOL_ID>>
     * 注意：只查询有PROJECT_ID的记录，已有数据如果没有PROJECT_ID，会通过PROJECT_CD逻辑处理
     */
    private Map<String, Map<String, Long>> buildProjectIdToPoolIdMap(String year) {
        LambdaQueryWrapper<BudgetPoolDemR> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BudgetPoolDemR::getDeleted, Boolean.FALSE)
                .eq(BudgetPoolDemR::getYear, year)
                .isNotNull(BudgetPoolDemR::getProjectId)
                .ne(BudgetPoolDemR::getProjectId, "");
        List<BudgetPoolDemR> list = budgetPoolDemRMapper.selectList(wrapper);
        
        Map<String, Map<String, Long>> map = new HashMap<>();
        for (BudgetPoolDemR relation : list) {
            String projectId = relation.getProjectId();
            if (StringUtils.isNotBlank(projectId)) {
                map.computeIfAbsent(projectId, k -> new HashMap<>())
                   .put(relation.getQuarter(), relation.getId());
            }
        }
        return map;
    }

    /**
     * 构建预算池关系 Key
     */
    private String buildPoolRelationKey(String year, String quarter, String isInternal, String morgCode, String budgetSubjectCode, String masterProjectCode, String erpAssetType) {
        return year + "@" + quarter + "@" + isInternal + "@" + morgCode + "@" + budgetSubjectCode + "@" + masterProjectCode + "@" + erpAssetType;
    }

    /**
     * 构建预算科目编码
     * 格式：custom1 + "-" + account
     * 如果 custom1 或 account 为空，返回 "NAN-NAN"（表示无效组合，与有资产类型编码的情况保持一致）
     */
    private String buildBudgetSubjectCode(String custom1, String account) {
        if (StringUtils.isBlank(custom1) || StringUtils.isBlank(account)) {
            return "NAN-NAN";
        }
        return custom1 + "-" + account;
    }

    /**
     * 加载原始预算数据
     */
    private List<SystemProjectBudget> loadOriginalBudgets(String year) {
        LambdaQueryWrapper<SystemProjectBudget> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SystemProjectBudget::getDeleted, Boolean.FALSE)
                .eq(SystemProjectBudget::getYear, year);
        return systemProjectBudgetMapper.selectList(wrapper);
    }

    /**
     * 加载预算额度数据（只查询指定年份的数据）
     * 
     * @param year 预算年度
     * @return Map<PoolId, BudgetQuota>
     */
    private Map<Long, BudgetQuota> loadBudgetQuotas(String year) {
        LambdaQueryWrapper<BudgetQuota> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BudgetQuota::getDeleted, Boolean.FALSE);
        wrapper.eq(BudgetQuota::getYear, year);
        List<BudgetQuota> list = budgetQuotaMapper.selectList(wrapper);
        
        Map<Long, BudgetQuota> map = new HashMap<>();
        for (BudgetQuota quota : list) {
            map.put(quota.getPoolId(), quota);
        }
        return map;
    }

    /**
     * 加载预算余额数据（只查询指定年份的数据）
     * 
     * @param year 预算年度
     * @return Map<PoolId, BudgetBalance>
     */
    private Map<Long, BudgetBalance> loadBudgetBalances(String year) {
        LambdaQueryWrapper<BudgetBalance> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BudgetBalance::getDeleted, Boolean.FALSE);
        wrapper.eq(BudgetBalance::getYear, year);
        List<BudgetBalance> list = budgetBalanceMapper.selectList(wrapper);
        
        Map<Long, BudgetBalance> map = new HashMap<>();
        for (BudgetBalance balance : list) {
            map.put(balance.getPoolId(), balance);
        }
        return map;
    }

    /**
     * 根据季度计算比较金额
     */
    private BigDecimal calculateCompareAmount(String quarter, SystemProjectBudget budget) {
        BigDecimal q1 = budget.getQ1() == null ? BigDecimal.ZERO : budget.getQ1();
        BigDecimal q2 = budget.getQ2() == null ? BigDecimal.ZERO : budget.getQ2();
        BigDecimal q3 = budget.getQ3() == null ? BigDecimal.ZERO : budget.getQ3();
        BigDecimal q4 = budget.getQ4() == null ? BigDecimal.ZERO : budget.getQ4();
        
        if (QUARTER_Q1.equals(quarter)) {
            return q1;
        } else if (QUARTER_Q2.equals(quarter)) {
            return q2;
        } else if (QUARTER_Q3.equals(quarter)) {
            return q3;
        } else if (QUARTER_Q4.equals(quarter)) {
            return q4;
        }
        return BigDecimal.ZERO;
    }
    
    /**
     * 计算基础预算值（原始采购额 + 调整金额）
     * 注意：这不是最终的amountAvailable，amountAvailable = 基础预算值 - 冻结 - 占用 - 实际
     * 
     * @param originalAmount 原始采购额（来自SYSTEM_PROJECT_BUDGET，可能为null）
     * @param quota 预算额度对象，用于获取调整字段
     * @return 基础预算值，如果原始值和调整值都为0或null，返回null
     */
    private BigDecimal calculateBaseBudgetAmount(BigDecimal originalAmount, BudgetQuota quota) {
        if (quota == null) {
            // 如果没有quota，直接返回原始值（如果为null则返回null）
            return originalAmount;
        }
        
        // 获取调整字段
        BigDecimal amountAvailableAdj = quota.getAmountAvailableAdj(); // effectType=0 或 effectType=2（组织+科目）
        BigDecimal amountAdj = quota.getAmountAdj(); // effectType=1（仅Q1）
        
        // 计算调整金额总和
        BigDecimal totalAdjust = BigDecimal.ZERO;
        if (amountAvailableAdj != null && amountAvailableAdj.compareTo(BigDecimal.ZERO) != 0) {
            totalAdjust = totalAdjust.add(amountAvailableAdj);
        }
        // Q1季度时，还需要加上amountAdj（effectType=1）
        if (QUARTER_Q1.equals(quota.getQuarter()) && amountAdj != null && amountAdj.compareTo(BigDecimal.ZERO) != 0) {
            totalAdjust = totalAdjust.add(amountAdj);
        }
        
        // 计算基础预算值
        BigDecimal original = originalAmount != null ? originalAmount : BigDecimal.ZERO;
        BigDecimal baseAmount = original.add(totalAdjust);
        
        // 如果基础预算值为0且没有原始值和调整值，返回null
        if (baseAmount.compareTo(BigDecimal.ZERO) == 0 
            && originalAmount == null 
            && (amountAvailableAdj == null || amountAvailableAdj.compareTo(BigDecimal.ZERO) == 0)
            && (amountAdj == null || amountAdj.compareTo(BigDecimal.ZERO) == 0)) {
            return null;
        }
        
        return baseAmount;
    }
    
    /**
     * 计算基础预算值的变更量，并更新amountAvailable
     * 公式：新的amountAvailable = 当前的amountAvailable + (新的基础预算值 - 旧的基础预算值)
     * 
     * @param newBaseAmount 新的基础预算值（原始采购额（新的） + 调整金额）
     * @param oldBaseAmount 旧的基础预算值（原始采购额（旧的） + 调整金额）
     * @param currentAmountAvailable 当前的amountAvailable（已经扣减了冻结、占用、实际）
     * @return 新的amountAvailable
     */
    private BigDecimal calculateNewAmountAvailable(BigDecimal newBaseAmount, BigDecimal oldBaseAmount, BigDecimal currentAmountAvailable) {
        // 计算基础预算值的变更量
        BigDecimal oldBase = oldBaseAmount != null ? oldBaseAmount : BigDecimal.ZERO;
        BigDecimal newBase = newBaseAmount != null ? newBaseAmount : BigDecimal.ZERO;
        BigDecimal baseAmountDiff = newBase.subtract(oldBase);
        
        // 如果基础预算值没有变化，且新的基础预算值为null，返回null
        if (baseAmountDiff.compareTo(BigDecimal.ZERO) == 0) {
            if (newBaseAmount == null && oldBaseAmount == null) {
                return null;
            }
            // 基础预算值没有变化，返回当前的amountAvailable
            return currentAmountAvailable;
        }
        
        // 计算新的amountAvailable = 当前的amountAvailable + 基础预算值的变更量
        BigDecimal current = currentAmountAvailable != null ? currentAmountAvailable : BigDecimal.ZERO;
        BigDecimal newAmountAvailable = current.add(baseAmountDiff);
        
        // 预算系统下发的数据必须如实同步，即使amountAvailable为负数也返回（不再返回null）
        return newAmountAvailable;
    }
    
    /**
     * 计算基础付款预算值（原始付款额 + 调整金额）
     * 注意：这不是最终的amountPayAvailable，amountPayAvailable = 基础付款预算值 - 冻结 - 占用 - 实际
     * 
     * @param originalPayAmount 原始付款额（来自SYSTEM_PROJECT_BUDGET，可能为null）
     * @param quota 预算额度对象，用于获取调整字段
     * @return 基础付款预算值
     */
    private BigDecimal calculateBasePayBudgetAmount(BigDecimal originalPayAmount, BudgetQuota quota) {
        if (quota == null) {
            // 如果没有quota，返回原始值（如果为null则返回0）
            return originalPayAmount != null ? originalPayAmount : BigDecimal.ZERO;
        }
        
        // 获取调整字段
        BigDecimal amountPayAdj = quota.getAmountPayAdj(); // effectType=2（项目/组织+资产类型）
        
        // 计算基础付款预算值
        BigDecimal original = originalPayAmount != null ? originalPayAmount : BigDecimal.ZERO;
        BigDecimal adjust = amountPayAdj != null ? amountPayAdj : BigDecimal.ZERO;
        BigDecimal baseAmount = original.add(adjust);
        
        return baseAmount;
    }
    
    /**
     * 计算基础付款预算值的变更量，并更新amountPayAvailable
     * 公式：新的amountPayAvailable = 当前的amountPayAvailable + (新的基础付款预算值 - 旧的基础付款预算值)
     * 
     * @param newBasePayAmount 新的基础付款预算值（原始付款额（新的） + 调整金额）
     * @param oldBasePayAmount 旧的基础付款预算值（原始付款额（旧的） + 调整金额）
     * @param currentAmountPayAvailable 当前的amountPayAvailable（已经扣减了冻结、占用、实际）
     * @return 新的amountPayAvailable
     */
    private BigDecimal calculateNewAmountPayAvailable(BigDecimal newBasePayAmount, BigDecimal oldBasePayAmount, BigDecimal currentAmountPayAvailable) {
        // 计算基础付款预算值的变更量
        BigDecimal oldBase = oldBasePayAmount != null ? oldBasePayAmount : BigDecimal.ZERO;
        BigDecimal newBase = newBasePayAmount != null ? newBasePayAmount : BigDecimal.ZERO;
        BigDecimal basePayAmountDiff = newBase.subtract(oldBase);
        
        // 如果基础付款预算值没有变化，返回当前的amountPayAvailable
        if (basePayAmountDiff.compareTo(BigDecimal.ZERO) == 0) {
            return currentAmountPayAvailable != null ? currentAmountPayAvailable : BigDecimal.ZERO;
        }
        
        // 计算新的amountPayAvailable = 当前的amountPayAvailable + 基础付款预算值的变更量
        BigDecimal current = currentAmountPayAvailable != null ? currentAmountPayAvailable : BigDecimal.ZERO;
        BigDecimal newAmountPayAvailable = current.add(basePayAmountDiff);

        // 预算系统下发的数据必须如实同步，即使amountPayAvailable为负数也返回（不再强制为0）
        return newAmountPayAvailable;
    }
    
    /**
     * 按id排序BudgetBalance列表，避免死锁
     * 
     * @param list 待排序的列表
     * @return 排序后的列表
     */
    private List<BudgetBalance> sortBalancesById(List<BudgetBalance> list) {
        if (list == null || list.isEmpty()) {
            return list;
        }
        return list.stream()
                .sorted((a, b) -> {
                    Long idA = a.getId();
                    Long idB = b.getId();
                    if (idA == null && idB == null) {
                        return 0;
                    }
                    if (idA == null) {
                        return 1;
                    }
                    if (idB == null) {
                        return -1;
                    }
                    return idA.compareTo(idB);
                })
                .collect(java.util.stream.Collectors.toList());
    }
    
    /**
     * 按id排序BudgetQuota列表，避免死锁
     * 
     * @param list 待排序的列表
     * @return 排序后的列表
     */
    private List<BudgetQuota> sortQuotasById(List<BudgetQuota> list) {
        if (list == null || list.isEmpty()) {
            return list;
        }
        return list.stream()
                .sorted((a, b) -> {
                    Long idA = a.getId();
                    Long idB = b.getId();
                    if (idA == null && idB == null) {
                        return 0;
                    }
                    if (idA == null) {
                        return 1;
                    }
                    if (idB == null) {
                        return -1;
                    }
                    return idA.compareTo(idB);
                })
                .collect(java.util.stream.Collectors.toList());
    }
}

