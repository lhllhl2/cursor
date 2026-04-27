package com.jasolar.mis.module.system.service.budget.helper;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.jasolar.mis.module.system.domain.budget.BudgetBalance;
import com.jasolar.mis.module.system.domain.budget.BudgetLedger;
import com.jasolar.mis.module.system.domain.budget.BudgetLedgerHead;
import com.jasolar.mis.module.system.domain.budget.BudgetLedgerHeadHistory;
import com.jasolar.mis.module.system.domain.budget.BudgetLedgerSelfR;
import com.jasolar.mis.module.system.domain.budget.BudgetPoolDemR;
import com.jasolar.mis.module.system.domain.budget.BudgetQuota;
import com.jasolar.mis.module.system.domain.ehr.EhrOrgManageExtR;
import com.jasolar.mis.module.system.domain.ehr.EhrOrgManageOneR;
import com.jasolar.mis.module.system.domain.ehr.EhrOrgManageR;
import com.jasolar.mis.module.system.domain.ehr.MapHspCustom2;
import com.jasolar.mis.module.system.domain.ehr.ProjectControlExtR;
import com.jasolar.mis.module.system.domain.ehr.SubjectExtInfo;
import com.jasolar.mis.module.system.domain.ehr.SubjectInfo;
import com.jasolar.mis.module.system.mapper.budget.BudgetBalanceMapper;
import com.jasolar.mis.module.system.mapper.budget.BudgetLedgerHeadHistoryMapper;
import com.jasolar.mis.module.system.mapper.budget.BudgetLedgerHeadMapper;
import com.jasolar.mis.module.system.mapper.budget.BudgetLedgerMapper;
import com.jasolar.mis.module.system.mapper.budget.BudgetLedgerSelfRMapper;
import com.jasolar.mis.module.system.domain.budget.SystemProjectBudget;
import com.jasolar.mis.module.system.mapper.budget.BudgetPoolDemRMapper;
import com.jasolar.mis.module.system.mapper.budget.BudgetQuotaMapper;
import com.jasolar.mis.module.system.mapper.budget.SystemProjectBudgetMapper;
import com.jasolar.mis.module.system.mapper.ehr.EhrOrgManageExtRMapper;
import com.jasolar.mis.module.system.mapper.ehr.EhrOrgManageOneRMapper;
import com.jasolar.mis.module.system.mapper.ehr.EhrOrgManageRMapper;
import com.jasolar.mis.module.system.mapper.ehr.MapHspCustom2Mapper;
import com.jasolar.mis.module.system.mapper.ehr.ProjectControlExtRMapper;
import com.jasolar.mis.module.system.mapper.ehr.SubjectExtInfoMapper;
import com.jasolar.mis.module.system.mapper.ehr.SubjectInfoMapper;
import com.jasolar.mis.module.system.mapper.budget.BudgetMemberNameCodeViewMapper;
import com.jasolar.mis.module.system.domain.budget.BudgetMemberNameCodeView;
import com.jasolar.mis.module.system.service.budget.snapshot.EhrControlLevelSnapshotService;
import com.jasolar.mis.module.system.service.budget.snapshot.EhrControlLevelSnapshotValue;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 预算查询辅助 Service
 * 提供通用的预算数据查询方法
 *
 * @author Auto Generated
 */
@Service
@Slf4j
public class BudgetQueryHelperService {

    /** ESB requestTime 格式：yyyy-MM-dd HH:mm:ss.SSS */
    private static final DateTimeFormatter ESB_REQUEST_TIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
    /** 兼容无毫秒格式 */
    private static final DateTimeFormatter ESB_REQUEST_TIME_FORMAT_NO_MS = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private static final String CLAIM_BIZ_TYPE = "CLAIM";
    /**
     * 付款/报销实际日期来源开关：1 表示沿用 month，0 表示优先使用 actualMonth（若有值）
     */
    private static final int CLAIM_ACTUAL_DATE_SOURCE_FLAG = 1;

    /**
     * 将 ESB 的 requestTime 字符串解析为 LocalDateTime。
     * 支持格式：yyyy-MM-dd HH:mm:ss.SSS 或 yyyy-MM-dd HH:mm:ss。
     *
     * @param requestTime ESB 请求时间字符串，可为 null 或空
     * @return 解析后的时间，解析失败或为空时返回 null
     */
    public static LocalDateTime parseEsbRequestTime(String requestTime) {
        if (StringUtils.isBlank(requestTime)) {
            return null;
        }
        String s = requestTime.trim();
        try {
            if (s.length() > 19) {
                return LocalDateTime.parse(s, ESB_REQUEST_TIME_FORMAT);
            }
            return LocalDateTime.parse(s, ESB_REQUEST_TIME_FORMAT_NO_MS);
        } catch (DateTimeParseException e) {
            log.warn("parseEsbRequestTime 解析失败，使用 null: requestTime={}", requestTime, e);
            return null;
        }
    }

    @Resource
    private BudgetPoolDemRMapper budgetPoolDemRMapper;

    @Resource
    private SystemProjectBudgetMapper systemProjectBudgetMapper;

    @Resource
    private BudgetQuotaMapper budgetQuotaMapper;

    @Resource
    private BudgetBalanceMapper budgetBalanceMapper;

    @Resource
    private BudgetLedgerHeadMapper budgetLedgerHeadMapper;

    @Resource
    private BudgetLedgerHeadHistoryMapper budgetLedgerHeadHistoryMapper;

    @Resource
    private BudgetLedgerMapper budgetLedgerMapper;

    @Resource
    private BudgetLedgerSelfRMapper budgetLedgerSelfRMapper;

    @Resource
    private EhrOrgManageExtRMapper ehrOrgManageExtRMapper;
    
    @Resource
    private EhrOrgManageRMapper ehrOrgManageRMapper;
    
    @Resource
    private EhrOrgManageOneRMapper ehrOrgManageOneRMapper;
    
    @Resource
    private SubjectInfoMapper subjectInfoMapper;
    
    @Resource
    private SubjectExtInfoMapper subjectExtInfoMapper;
    
    @Resource
    private ProjectControlExtRMapper projectControlExtRMapper;
    
    @Resource
    private MapHspCustom2Mapper mapHspCustom2Mapper;
    
    @Resource
    private BudgetMemberNameCodeViewMapper budgetMemberNameCodeViewMapper;

    @Resource
    private EhrControlLevelSnapshotService ehrControlLevelSnapshotService;

    /**
     * 根据 ledger map 查询所有季度（q1-q4）的 BudgetQuota 和 BudgetBalance
     * 
     * @param ledgerMap key 为 bizCode + "@" + bizItemCode，value 为 BudgetLedger
     * @param ehrCdToOrgCdMap EHR组织编码到管理组织编码的映射，key为EHR_CD，value为ORG_CD（一对一关系，可为null）
     * @param erpAcctCdToAcctCdMap ERP科目编码到科目编码的映射，key为ERP_ACCT_CD，value为ACCT_CD（可为null）
     * @param erpAssetTypeToMemberCdMap erpAssetType 映射表（MEMBER_CD2 -> MEMBER_CD），用于将映射前的 erpAssetType 映射为映射后的值
     * @return 包含两个 Map 的结果对象：
     *         - quotaMap: key 为 bizCode + "@" + bizItemCode + "@" + quarter，value 为 BudgetQuota（一对一关系）
     *         - balanceMap: key 为 bizCode + "@" + bizItemCode + "@" + quarter，value 为 BudgetBalance（一对一关系）
     */
    public BudgetQuotaBalanceResult queryQuotaAndBalanceByAllQuarters(Map<String, ? extends com.jasolar.mis.module.system.domain.budget.BudgetLedger> ledgerMap,
                                                                        Map<String, String> ehrCdToOrgCdMap,
                                                                        Map<String, String> erpAcctCdToAcctCdMap,
                                                                        Map<String, String> erpAssetTypeToMemberCdMap) {
        if (CollectionUtils.isEmpty(ledgerMap)) {
            return new BudgetQuotaBalanceResult(Collections.emptyMap(), Collections.emptyMap(), Collections.emptyMap());
        }
        // 构建查询参数：对每个 ledger，查询所有季度（q1, q2, q3, q4）
        // 区分项目查询和非项目查询的参数列表
        List<BudgetPoolDemRMapper.DimensionParam> projectDimensionParams = new ArrayList<>();
        List<BudgetPoolDemRMapper.DimensionParam> nonProjectDimensionParams = new ArrayList<>();
        Map<String, com.jasolar.mis.module.system.domain.budget.BudgetLedger> bizKeyToLedgerMap = new HashMap<>();
        
        String[] quarters = {"q1", "q2", "q3", "q4"};
        for (Map.Entry<String, ? extends com.jasolar.mis.module.system.domain.budget.BudgetLedger> entry : ledgerMap.entrySet()) {
            String bizKey = entry.getKey(); // bizCode + "@" + bizItemCode
            com.jasolar.mis.module.system.domain.budget.BudgetLedger ledger = entry.getValue();
            bizKeyToLedgerMap.put(bizKey, ledger);
            
            String year = ledger.getYear();
            String isInternal = "NAN".equals(ledger.getMasterProjectCode()) ? "1" : ledger.getIsInternal();
            String ehrMorgCode = ledger.getMorgCode();
            String budgetSubjectCode = ledger.getBudgetSubjectCode();
            String masterProjectCode = ledger.getMasterProjectCode();
            String originalErpAssetType = ledger.getErpAssetType();
            
            // 映射 erpAssetType：如果以 "1" 或 "M" 开头，需要通过映射表映射
            // 注意：带项目时不需要映射 erpAssetType
            MapErpAssetTypeResult erpAssetTypeResult = mapErpAssetTypeForQuery(originalErpAssetType, masterProjectCode, 
                    erpAssetTypeToMemberCdMap, "查询 BudgetPoolDemR 时，明细 [" + bizKey + "]");
            if (erpAssetTypeResult.hasError()) {
                return new BudgetQuotaBalanceResult(Collections.emptyMap(), Collections.emptyMap(), Collections.emptyMap(),
                        erpAssetTypeResult.getErrorMessage(), bizKey);
            }
            String erpAssetType = erpAssetTypeResult.getMappedValue();
            
            log.debug("========== queryQuotaAndBalanceByAllQuarters - 维度信息: bizKey={}, year={}, isInternal={}, ehrMorgCode={}, budgetSubjectCode={}, masterProjectCode={}, originalErpAssetType={}, mappedErpAssetType={} ==========", 
                    bizKey, year, isInternal, ehrMorgCode, budgetSubjectCode, masterProjectCode, originalErpAssetType, erpAssetType);

            // 如果 masterProjectCode 不为 "NAN"，只查询项目维度（year, quarter, isInternal, masterProjectCode）
            // 注意：带项目时，morgCode 不参与匹配
            // 如果 masterProjectCode 为 "NAN"，查询所有维度
            boolean isProjectQuery = !"NAN".equals(masterProjectCode);

            // EHR 组织编码到管理组织编码的映射（一对一关系）
            // 对于带项目的单据，如果映射表为空，直接使用传入的 EHR 组织编码
            String morgCode;
            if (isProjectQuery) {
                // 带项目的单据：如果映射表为空，直接使用 EHR 组织编码
                if (ehrCdToOrgCdMap == null || ehrCdToOrgCdMap.isEmpty()) {
                    morgCode = ehrMorgCode;
                    log.info("========== queryQuotaAndBalanceByAllQuarters - 带项目的单据，EHR组织编码映射表为空，直接使用传入的EHR组织编码: {} ==========", ehrMorgCode);
                } else {
                    morgCode = ehrCdToOrgCdMap.get(ehrMorgCode);
                    // 如果映射表中没有找到，也直接使用 EHR 组织编码
                    if (StringUtils.isBlank(morgCode)) {
                        morgCode = ehrMorgCode;
                        log.info("========== queryQuotaAndBalanceByAllQuarters - 带项目的单据，EHR组织编码 {} 未找到映射，直接使用传入的EHR组织编码 ==========", ehrMorgCode);
                    }
                }
            } else {
                // 非项目查询：必须要有映射
                if (ehrCdToOrgCdMap == null || ehrCdToOrgCdMap.isEmpty()) {
                    return new BudgetQuotaBalanceResult(Collections.emptyMap(), Collections.emptyMap(), Collections.emptyMap(),
                            "EHR组织编码映射表为空，无法映射组织编码: " + ehrMorgCode, bizKey);
                }
                morgCode = ehrCdToOrgCdMap.get(ehrMorgCode);
                if (StringUtils.isBlank(morgCode)) {
                    return new BudgetQuotaBalanceResult(Collections.emptyMap(), Collections.emptyMap(), Collections.emptyMap(),
                            "EHR组织编码 " + ehrMorgCode + " 未找到对应的管理组织编码映射", bizKey);
                }
            }
            
            for (String quarter : quarters) {
                if (isProjectQuery) {
                    // 项目查询：只传入 year, quarter, isInternal, masterProjectCode
                    // morgCode、budgetSubjectCode 和 erpAssetType 传入占位符（DimensionParam 构造函数需要所有参数）
                    projectDimensionParams.add(new BudgetPoolDemRMapper.DimensionParam(year, quarter, isInternal, "NAN", "NAN-NAN", masterProjectCode, "NAN"));
                } else {
                    // 非项目查询：需要映射科目编码
                    // ERP科目编码到科目编码的映射（一对一关系）
                    String acctCd;
                    // 如果 budgetSubjectCode 是 "NAN-NAN"，直接使用 "NAN-NAN" 作为 acctCd，不需要映射表
                    if ("NAN-NAN".equals(budgetSubjectCode)) {
                        acctCd = "NAN-NAN";
                    } else {
                        // 只有当 budgetSubjectCode 不是 "NAN-NAN" 时，才需要检查映射表
                        // 如果映射表为空，说明无法映射非 "NAN-NAN" 的科目编码
                        if (erpAcctCdToAcctCdMap == null || erpAcctCdToAcctCdMap.isEmpty()) {
                            return new BudgetQuotaBalanceResult(Collections.emptyMap(), Collections.emptyMap(), Collections.emptyMap(),
                                    "ERP科目编码映射表为空，无法映射科目编码: " + budgetSubjectCode, bizKey);
                        }
                        acctCd = erpAcctCdToAcctCdMap.get(budgetSubjectCode);
                        if (StringUtils.isBlank(acctCd)) {
                            return new BudgetQuotaBalanceResult(Collections.emptyMap(), Collections.emptyMap(), Collections.emptyMap(),
                                    "ERP科目编码 " + budgetSubjectCode + " 未找到对应的科目编码映射", bizKey);
                        }
                    }
                    // 非项目查询：传入所有维度
                    log.debug("========== queryQuotaAndBalanceByAllQuarters - 非项目查询参数: year={}, quarter={}, isInternal={}, morgCode={}, acctCd={}, masterProjectCode={}, erpAssetType={} ==========", 
                            year, quarter, isInternal, morgCode, acctCd, masterProjectCode, erpAssetType);
                    nonProjectDimensionParams.add(new BudgetPoolDemRMapper.DimensionParam(year, quarter, isInternal, morgCode, acctCd, masterProjectCode, erpAssetType));
                }
            }
        }
        
        // 批量查询 BUDGET_POOL_DEM_R
        // 分别查询项目维度和非项目维度
        List<BudgetPoolDemR> poolDemRs = new ArrayList<>();
        if (!projectDimensionParams.isEmpty()) {
            log.info("========== queryQuotaAndBalanceByAllQuarters - 项目查询参数数量: {} ==========", projectDimensionParams.size());
            // 分批查询，避免IN子句参数过多导致超时（每个批次150个参数，4个字段，总共约600个表达式，在Oracle的1000限制内）
            List<BudgetPoolDemR> projectResults = batchSelectByDimensionsWithYearAndQuarterForProject(projectDimensionParams, 150);
            poolDemRs.addAll(projectResults);
            // 若按 masterProjectCode 查不到，按 PROJECT_ID 回退查找（项目编码变更后避免预算调整单重复创建 pool）
            if (projectResults.isEmpty()) {
                List<BudgetPoolDemR> fallbackByProjectId = queryPoolByProjectIdFallback(projectDimensionParams);
                if (!fallbackByProjectId.isEmpty()) {
                    poolDemRs.addAll(fallbackByProjectId);
                    log.info("========== queryQuotaAndBalanceByAllQuarters - 按 PROJECT_ID 回退查找到 {} 条 BudgetPoolDemR ==========", fallbackByProjectId.size());
                }
            }
        }
        if (!nonProjectDimensionParams.isEmpty()) {
            log.info("========== queryQuotaAndBalanceByAllQuarters - 非项目查询参数数量: {} ==========", nonProjectDimensionParams.size());
            // 打印前几个查询参数用于调试
            if (log.isDebugEnabled() && !nonProjectDimensionParams.isEmpty()) {
                for (int i = 0; i < Math.min(3, nonProjectDimensionParams.size()); i++) {
                    BudgetPoolDemRMapper.DimensionParam param = nonProjectDimensionParams.get(i);
                    log.debug("========== queryQuotaAndBalanceByAllQuarters - 非项目查询参数[{}]: year={}, quarter={}, isInternal={}, morgCode={}, budgetSubjectCode={}, masterProjectCode={}, erpAssetType={} ==========", 
                            i, param.getYear(), param.getQuarter(), param.getIsInternal(), param.getMorgCode(), param.getBudgetSubjectCode(), param.getMasterProjectCode(), param.getErpAssetType());
                }
            }
            // 分批查询，避免IN子句参数过多导致超时（每个批次100个参数，Oracle IN子句限制约1000个表达式）
            List<BudgetPoolDemR> nonProjectResults = batchSelectByDimensionsWithYearAndQuarter(nonProjectDimensionParams, 100);
            poolDemRs.addAll(nonProjectResults);
        }
        log.info("========== queryQuotaAndBalanceByAllQuarters - 查询到 {} 条 BudgetPoolDemR ==========", poolDemRs.size());
        if (poolDemRs.isEmpty() && !nonProjectDimensionParams.isEmpty()) {
            log.warn("========== queryQuotaAndBalanceByAllQuarters - 查询结果为空，可能的原因：1. 数据库中不存在对应维度的 BudgetPoolDemR 记录；2. 维度映射不正确 ==========");
            // 打印第一个查询参数用于排查
            if (!nonProjectDimensionParams.isEmpty()) {
                BudgetPoolDemRMapper.DimensionParam firstParam = nonProjectDimensionParams.get(0);
                log.warn("========== queryQuotaAndBalanceByAllQuarters - 第一个查询参数: year={}, quarter={}, isInternal={}, morgCode={}, budgetSubjectCode={}, masterProjectCode={}, erpAssetType={} ==========", 
                        firstParam.getYear(), firstParam.getQuarter(), firstParam.getIsInternal(), firstParam.getMorgCode(), firstParam.getBudgetSubjectCode(), firstParam.getMasterProjectCode(), firstParam.getErpAssetType());
            }
        }
        
        // 建立 poolId 到 (bizKey, quarter) 的映射（一个poolId可能对应多个bizKey）
        // 使用 Set 去重，避免同一 poolId 对同一 bizKeyQuarter 的重复映射导致 balance 被重复累加
        Map<Long, Set<String>> poolIdToBizKeyQuarterMap = new HashMap<>(); // poolId -> Set<bizKey+"@"+quarter>
        Set<Long> poolIdSet = new HashSet<>();
        for (BudgetPoolDemR poolDemR : poolDemRs) {
            Long poolId = poolDemR.getId();
            poolIdSet.add(poolId);
            
            // 找到对应的 bizKey
            String year = poolDemR.getYear();
            String quarter = poolDemR.getQuarter();
            String isInternal = poolDemR.getIsInternal();
            String morgCode = poolDemR.getMorgCode();
            String budgetSubjectCode = poolDemR.getBudgetSubjectCode();
            String masterProjectCode = poolDemR.getMasterProjectCode();
            String erpAssetType = poolDemR.getErpAssetType();
            
            // 遍历所有 ledger，找到所有匹配的 bizKey（不要 break，支持多个明细映射到同一个 poolId）
            for (Map.Entry<String, com.jasolar.mis.module.system.domain.budget.BudgetLedger> entry : bizKeyToLedgerMap.entrySet()) {
                String bizKey = entry.getKey();
                com.jasolar.mis.module.system.domain.budget.BudgetLedger ledger = entry.getValue();
                boolean masterProjectIsNan = "NAN".equals(ledger.getMasterProjectCode());
                // EHR 组织编码到管理组织编码的映射（一对一关系）
                // 对于带项目的单据，如果映射表为空，直接使用传入的 EHR 组织编码
                String ledgerEhrMorgCode = ledger.getMorgCode();
                String ledgerMorgCode;
                if (masterProjectIsNan) {
                    // 非项目查询：必须要有映射
                    if (ehrCdToOrgCdMap == null || ehrCdToOrgCdMap.isEmpty()) {
                        return new BudgetQuotaBalanceResult(Collections.emptyMap(), Collections.emptyMap(), Collections.emptyMap(),
                                "EHR组织编码映射表为空，无法映射组织编码: " + ledgerEhrMorgCode, bizKey);
                    }
                    ledgerMorgCode = ehrCdToOrgCdMap.get(ledgerEhrMorgCode);
                    if (StringUtils.isBlank(ledgerMorgCode)) {
                        return new BudgetQuotaBalanceResult(Collections.emptyMap(), Collections.emptyMap(), Collections.emptyMap(),
                                "EHR组织编码 " + ledgerEhrMorgCode + " 未找到对应的管理组织编码映射", bizKey);
                    }
                } else {
                    // 带项目的单据：如果映射表为空，直接使用 EHR 组织编码
                    if (ehrCdToOrgCdMap == null || ehrCdToOrgCdMap.isEmpty()) {
                        ledgerMorgCode = ledgerEhrMorgCode;
                        log.debug("========== queryQuotaAndBalanceByAllQuarters - 带项目的单据，EHR组织编码映射表为空，直接使用传入的EHR组织编码: {} ==========", ledgerEhrMorgCode);
                    } else {
                        ledgerMorgCode = ehrCdToOrgCdMap.get(ledgerEhrMorgCode);
                        // 如果映射表中没有找到，也直接使用 EHR 组织编码
                        if (StringUtils.isBlank(ledgerMorgCode)) {
                            ledgerMorgCode = ledgerEhrMorgCode;
                            log.debug("========== queryQuotaAndBalanceByAllQuarters - 带项目的单据，EHR组织编码 {} 未找到映射，直接使用传入的EHR组织编码 ==========", ledgerEhrMorgCode);
                        }
                    }
                }
                
                // ERP科目编码到科目编码的映射（一对一关系）
                // 对于带项目的单据，如果映射表为空，直接使用传入的科目编码
                String ledgerBudgetSubjectCode = ledger.getBudgetSubjectCode();
                String ledgerAcctCd;
                // 如果 ledgerBudgetSubjectCode 是 "NAN-NAN"，直接使用 "NAN-NAN" 作为 ledgerAcctCd，不需要映射表
                if ("NAN-NAN".equals(ledgerBudgetSubjectCode)) {
                    ledgerAcctCd = "NAN-NAN";
                } else {
                    if (masterProjectIsNan) {
                        // 非项目查询：必须要有映射
                        if (erpAcctCdToAcctCdMap == null || erpAcctCdToAcctCdMap.isEmpty()) {
                            return new BudgetQuotaBalanceResult(Collections.emptyMap(), Collections.emptyMap(), Collections.emptyMap(),
                                    "ERP科目编码映射表为空，无法映射科目编码: " + ledgerBudgetSubjectCode, bizKey);
                        }
                        ledgerAcctCd = erpAcctCdToAcctCdMap.get(ledgerBudgetSubjectCode);
                        if (StringUtils.isBlank(ledgerAcctCd)) {
                            return new BudgetQuotaBalanceResult(Collections.emptyMap(), Collections.emptyMap(), Collections.emptyMap(),
                                    "ERP科目编码 " + ledgerBudgetSubjectCode + " 未找到对应的科目编码映射", bizKey);
                        }
                    } else {
                        // 带项目的单据：如果映射表为空，直接使用传入的科目编码
                        if (erpAcctCdToAcctCdMap == null || erpAcctCdToAcctCdMap.isEmpty()) {
                            ledgerAcctCd = ledgerBudgetSubjectCode;
                            log.debug("========== queryQuotaAndBalanceByAllQuarters - 带项目的单据，ERP科目编码映射表为空，直接使用传入的科目编码: {} ==========", ledgerBudgetSubjectCode);
                        } else {
                            ledgerAcctCd = erpAcctCdToAcctCdMap.get(ledgerBudgetSubjectCode);
                            // 如果映射表中没有找到，也直接使用传入的科目编码
                            if (StringUtils.isBlank(ledgerAcctCd)) {
                                ledgerAcctCd = ledgerBudgetSubjectCode;
                                log.debug("========== queryQuotaAndBalanceByAllQuarters - 带项目的单据，ERP科目编码 {} 未找到映射，直接使用传入的科目编码 ==========", ledgerBudgetSubjectCode);
                            }
                        }
                    }
                }
                
                // 计算查询时使用的 isInternal 值（与查询参数构建时的逻辑保持一致）
                // 如果 masterProjectCode 是 "NAN"，则 isInternal 强制为 "1"；否则使用 ledger 的原始 isInternal 值
                String queryIsInternal = "NAN".equals(ledger.getMasterProjectCode()) ? "1" : ledger.getIsInternal();
                
                // 映射 ledger 的 erpAssetType（与查询参数构建时的逻辑保持一致）
                // 注意：带项目时不需要映射 erpAssetType
                String ledgerOriginalErpAssetType = ledger.getErpAssetType();
                MapErpAssetTypeResult ledgerErpAssetTypeResult = mapErpAssetTypeForQuery(ledgerOriginalErpAssetType, ledger.getMasterProjectCode(), 
                        erpAssetTypeToMemberCdMap, "匹配 BudgetPoolDemR 时，明细 [" + bizKey + "]");
                String ledgerErpAssetType;
                if (ledgerErpAssetTypeResult.hasError()) {
                    // 如果映射失败，跳过该 ledger，继续匹配下一个
                    continue;
                }
                ledgerErpAssetType = ledgerErpAssetTypeResult.getMappedValue();
                
                // 如果 ledger 的 masterProjectCode 不为 "NAN"，只匹配项目维度（year, masterProjectCode, isInternal）
                // 注意：项目查询时，morgCode 不参与查询条件，也不参与匹配（与查询逻辑保持一致）
                // 如果 ledger 的 masterProjectCode 为 "NAN"，匹配所有维度
                boolean ledgerIsProjectQuery = !masterProjectIsNan;
                boolean match;
                
                if (ledgerIsProjectQuery) {
                    // 项目查询：只匹配 year, masterProjectCode, isInternal（morgCode 不参与匹配）
                    match = year.equals(ledger.getYear()) 
                            && masterProjectCode.equals(ledger.getMasterProjectCode())
                            && isInternal.equals(queryIsInternal);
                } else {
                    // 非项目查询：匹配所有维度（使用映射后的 erpAssetType）
                    match = year.equals(ledger.getYear()) 
                            && ledgerMorgCode.equals(morgCode) 
                            && ledgerAcctCd.equals(budgetSubjectCode) 
                            && masterProjectCode.equals(ledger.getMasterProjectCode())
                            && erpAssetType.equals(ledgerErpAssetType)
                            && isInternal.equals(queryIsInternal);
                    
                    // 添加调试日志，帮助排查匹配问题
                    if (!match && log.isDebugEnabled()) {
                        log.debug("========== 维度匹配失败: bizKey={}, quarter={} ==========", bizKey, quarter);
                        log.debug("  poolDemR维度: year={}, morgCode={}, budgetSubjectCode={}, masterProjectCode={}, erpAssetType={}, isInternal={}", 
                                year, morgCode, budgetSubjectCode, masterProjectCode, erpAssetType, isInternal);
                        log.debug("  ledger维度: year={}, morgCode={}, acctCd={}, masterProjectCode={}, erpAssetType={}, isInternal={}", 
                                ledger.getYear(), ledgerMorgCode, ledgerAcctCd, ledger.getMasterProjectCode(), ledgerErpAssetType, queryIsInternal);
                        log.debug("  匹配结果: year={}, morgCode={}, acctCd={}, masterProjectCode={}, erpAssetType={}, isInternal={}", 
                                year.equals(ledger.getYear()), ledgerMorgCode.equals(morgCode), ledgerAcctCd.equals(budgetSubjectCode),
                                masterProjectCode.equals(ledger.getMasterProjectCode()), erpAssetType.equals(ledgerErpAssetType),
                                isInternal.equals(queryIsInternal));
                    }
                }
                
                if (match) {
                    String bizKeyQuarter = bizKey + "@" + quarter;
                    poolIdToBizKeyQuarterMap.computeIfAbsent(poolId, k -> new LinkedHashSet<>()).add(bizKeyQuarter);
                    log.debug("========== 维度匹配成功: bizKey={}, quarter={}, poolId={} ==========", bizKey, quarter, poolId);
                    // ✅ 不再 break，继续查找其他匹配的 bizKey
                }
            }
        }
        
        // 查询 BUDGET_QUOTA 和 BUDGET_BALANCE
        List<Long> poolIds = new ArrayList<>(poolIdSet);
        List<BudgetQuota> quotas = loadBudgetQuotasByPoolIds(poolIds);
        List<BudgetBalance> balances = loadBudgetBalancesByPoolIds(poolIds);
        
        log.info("========== 查询到 {} 条 BudgetQuota，{} 条 BudgetBalance ==========", quotas.size(), balances.size());
        
        // 组装结果 Map
        // key 格式：bizCode + "@" + bizItemCode + "@" + quarter
        Map<String, BudgetQuota> quotaMap = new HashMap<>();
        Map<String, BudgetBalance> balanceMap = new HashMap<>();
        
        for (BudgetQuota quota : quotas) {
            Set<String> bizKeyQuarters = poolIdToBizKeyQuarterMap.get(quota.getPoolId());
            if (bizKeyQuarters != null) {
                for (String bizKeyQuarter : bizKeyQuarters) {
                    if (quotaMap.containsKey(bizKeyQuarter)) {
                        log.warn("========== bizKeyQuarter={} 已存在 BudgetQuota，将覆盖: poolId={}, amountTotal={}, amountAdj={}, quarter={} ==========",
                                bizKeyQuarter, quota.getPoolId(), quota.getAmountTotal(), quota.getAmountAdj(), quota.getQuarter());
                    }
                    quotaMap.put(bizKeyQuarter, quota);
                    log.info("========== 添加 BudgetQuota: bizKeyQuarter={}, poolId={}, amountTotal={}, amountAdj={}, quarter={} ==========",
                            bizKeyQuarter, quota.getPoolId(), quota.getAmountTotal(), quota.getAmountAdj(), quota.getQuarter());
                }
            }
        }
        // 先建立 bizKeyQuarter -> List<BudgetBalance> 的映射，用于处理一个 bizKeyQuarter 对应多个 poolId 的情况
        Map<String, List<BudgetBalance>> bizKeyQuarterToBalancesMap = new HashMap<>();
        for (BudgetBalance balance : balances) {
            Set<String> bizKeyQuarters = poolIdToBizKeyQuarterMap.get(balance.getPoolId());
            if (bizKeyQuarters != null) {
                for (String bizKeyQuarter : bizKeyQuarters) {
                    List<BudgetBalance> existingBalances = bizKeyQuarterToBalancesMap.computeIfAbsent(bizKeyQuarter, k -> new ArrayList<>());
                    boolean duplicatedBalance = existingBalances.stream()
                            .anyMatch(existing -> Objects.equals(existing.getId(), balance.getId()));
                    if (!duplicatedBalance) {
                        existingBalances.add(balance);
                    } else {
                        log.warn("========== 跳过重复 BudgetBalance 映射: bizKeyQuarter={}, poolId={}, balanceId={}, quarter={} ==========",
                                bizKeyQuarter, balance.getPoolId(), balance.getId(), balance.getQuarter());
                    }
                }
            }
        }
        
        // 组装 balanceMap：如果同一个 bizKeyQuarter 对应多个 balance，则累加 amountAvailable 和 amountPayAvailable
        for (Map.Entry<String, List<BudgetBalance>> entry : bizKeyQuarterToBalancesMap.entrySet()) {
            String bizKeyQuarter = entry.getKey();
            List<BudgetBalance> balanceList = entry.getValue();
            
            if (balanceList.size() == 1) {
                // 只有一个 balance，直接使用
                BudgetBalance balance = balanceList.get(0);
                    balanceMap.put(bizKeyQuarter, balance);
                log.info("========== 添加 BudgetBalance: bizKeyQuarter={}, poolId={}, amountAvailable={}, amountFrozen={}, amountPayAvailable={}, quarter={} ==========",
                        bizKeyQuarter, balance.getPoolId(), balance.getAmountAvailable(), balance.getAmountFrozen(), balance.getAmountPayAvailable(), balance.getQuarter());
            } else {
                // 多个 balance，需要合并：累加 amountAvailable 和 amountPayAvailable
                BudgetBalance firstBalance = balanceList.get(0);
                BudgetBalance mergedBalance = new BudgetBalance();
                BeanUtils.copyProperties(firstBalance, mergedBalance);
                
                BigDecimal totalAmountAvailable = firstBalance.getAmountAvailable() == null ? BigDecimal.ZERO : firstBalance.getAmountAvailable();
                BigDecimal totalAmountPayAvailable = firstBalance.getAmountPayAvailable() == null ? BigDecimal.ZERO : firstBalance.getAmountPayAvailable();
                BigDecimal totalAmountFrozen = firstBalance.getAmountFrozen() == null ? BigDecimal.ZERO : firstBalance.getAmountFrozen();
                
                // 累加其他 balance 的金额
                for (int i = 1; i < balanceList.size(); i++) {
                    BudgetBalance balance = balanceList.get(i);
                    totalAmountAvailable = totalAmountAvailable.add(balance.getAmountAvailable() == null ? BigDecimal.ZERO : balance.getAmountAvailable());
                    totalAmountPayAvailable = totalAmountPayAvailable.add(balance.getAmountPayAvailable() == null ? BigDecimal.ZERO : balance.getAmountPayAvailable());
                    totalAmountFrozen = totalAmountFrozen.add(balance.getAmountFrozen() == null ? BigDecimal.ZERO : balance.getAmountFrozen());
                }
                
                mergedBalance.setAmountAvailable(totalAmountAvailable);
                mergedBalance.setAmountPayAvailable(totalAmountPayAvailable);
                mergedBalance.setAmountFrozen(totalAmountFrozen);
                
                balanceMap.put(bizKeyQuarter, mergedBalance);
                log.warn("========== bizKeyQuarter={} 匹配到 {} 个 BudgetBalance，已合并: 合并后amountAvailable={}, amountPayAvailable={}, amountFrozen={}, quarter={}, poolIds={} ==========",
                        bizKeyQuarter, balanceList.size(), totalAmountAvailable, totalAmountPayAvailable, totalAmountFrozen, firstBalance.getQuarter(),
                        balanceList.stream().map(BudgetBalance::getPoolId).map(String::valueOf).collect(java.util.stream.Collectors.joining(",")));
            }
        }
        
        // 查询关联的 BudgetLedger
        Map<String, List<BudgetLedger>> relatedBudgetLedgerMap = new HashMap<>();
        if (!CollectionUtils.isEmpty(bizKeyToLedgerMap)) {
            // 获取所有 BudgetLedger 的 id
            Set<Long> ledgerIds = new HashSet<>();
            for (BudgetLedger ledger : bizKeyToLedgerMap.values()) {
                if (ledger.getId() != null) {
                    ledgerIds.add(ledger.getId());
                }
            }
            
            if (!CollectionUtils.isEmpty(ledgerIds)) {
                // 从第一个 ledger 获取 bizType 来确定当前业务类型
                BudgetLedger firstLedger = bizKeyToLedgerMap.values().iterator().next();
                String currentBizType = firstLedger.getBizType();
                
                List<BudgetLedgerSelfR> relatedSelfRs = new ArrayList<>();
                
                if ("CONTRACT".equals(currentBizType)) {
                    // 合同：通过 id 查询关联的 APPLY 申请单
                    // selectByIdsAndBizType 查询的是 id IN (ledgerIds) AND bizType = 'APPLY'
                    // 返回的 BudgetLedgerSelfR 中，id 是关联的 BudgetLedger 的 id，relatedId 是原始的 BudgetLedger 的 id
                    List<BudgetLedgerSelfR> applySelfRs = budgetLedgerSelfRMapper.selectByIdsAndBizType(ledgerIds, "APPLY");
                    if (!CollectionUtils.isEmpty(applySelfRs)) {
                        relatedSelfRs.addAll(applySelfRs);
                    }
                } else if ("CLAIM".equals(currentBizType)) {
                    // 付款报销：通过 id 查询关联的 APPLY 和 CONTRACT
                    List<BudgetLedgerSelfR> applySelfRs = budgetLedgerSelfRMapper.selectByIdsAndBizType(ledgerIds, "APPLY");
                    List<BudgetLedgerSelfR> contractSelfRs = budgetLedgerSelfRMapper.selectByIdsAndBizType(ledgerIds, "CONTRACT");
                    
                    // CLAIM：同一条「付款/报销流水 ledgerId」下，可能存在多条 APPLY/CONTRACT selfR。
                    // 规则：如果同一 ledgerId 同时存在 CONTRACT 和 APPLY，则该 ledgerId 只保留 CONTRACT；否则保留所有 APPLY。
                    // 重要：不要用 Map<ledgerId, selfR> 只保留一条，否则会吞掉多行 APPLY，导致 REJECTED/CANCELLED 回滚释放金额不完整。
                    Set<Long> ledgerIdsWithContract = contractSelfRs == null
                            ? Collections.emptySet()
                            : contractSelfRs.stream()
                            .map(BudgetLedgerSelfR::getId)
                            .filter(Objects::nonNull)
                            .collect(Collectors.toSet());
                    
                    if (!CollectionUtils.isEmpty(contractSelfRs)) {
                        relatedSelfRs.addAll(contractSelfRs);
                    }
                    
                    if (!CollectionUtils.isEmpty(applySelfRs)) {
                        for (BudgetLedgerSelfR selfR : applySelfRs) {
                            Long ledgerId = selfR.getId();
                            // 如果该 ledgerId 存在 CONTRACT，则跳过 APPLY；否则保留 APPLY 的每一行
                            if (ledgerId != null && !ledgerIdsWithContract.contains(ledgerId)) {
                                relatedSelfRs.add(selfR);
                            }
                        }
                    }
                }
                
                // 通过 BudgetLedgerSelfR 的 relatedId 字段（对应关联的 BudgetLedger 的 id）查询关联的 BudgetLedger
                if (!CollectionUtils.isEmpty(relatedSelfRs)) {
                    Set<Long> relatedLedgerIds = new HashSet<>();
                    for (BudgetLedgerSelfR selfR : relatedSelfRs) {
                        if (selfR.getRelatedId() != null) {
                            relatedLedgerIds.add(selfR.getRelatedId());
                        }
                    }
                    
                    if (!CollectionUtils.isEmpty(relatedLedgerIds)) {
                        LambdaQueryWrapper<BudgetLedger> relatedLedgerWrapper = new LambdaQueryWrapper<>();
                        relatedLedgerWrapper.in(BudgetLedger::getId, relatedLedgerIds)
                                .eq(BudgetLedger::getDeleted, Boolean.FALSE);
                        List<BudgetLedger> relatedLedgers = budgetLedgerMapper.selectList(relatedLedgerWrapper);
                        
                        if (!CollectionUtils.isEmpty(relatedLedgers)) {
                            // 建立 id（原始 BudgetLedger 的 id）到 BudgetLedger 的映射
                            Map<Long, List<BudgetLedger>> idToLedgersMap = new HashMap<>();
                            for (BudgetLedgerSelfR selfR : relatedSelfRs) {
                                Long originalLedgerId = selfR.getId(); // 原始的 BudgetLedger 的 id
                                Long relatedLedgerId = selfR.getRelatedId(); // 关联的 BudgetLedger 的 id
                                for (BudgetLedger relatedLedger : relatedLedgers) {
                                    if (relatedLedger.getId().equals(relatedLedgerId)) {
                                        idToLedgersMap.computeIfAbsent(originalLedgerId, k -> new ArrayList<>())
                                                .add(relatedLedger);
                                        break;
                                    }
                                }
                            }
                            
                            // 组装成 map：key 是 bizCode + "@" + bizItemCode，value 是 List<BudgetLedger>
                            for (Map.Entry<String, BudgetLedger> entry : bizKeyToLedgerMap.entrySet()) {
                                String bizKey = entry.getKey();
                                BudgetLedger ledger = entry.getValue();
                                Long ledgerId = ledger.getId();
                                
                                List<BudgetLedger> relatedLedgerList = idToLedgersMap.get(ledgerId);
                                if (!CollectionUtils.isEmpty(relatedLedgerList)) {
                                    relatedBudgetLedgerMap.put(bizKey, relatedLedgerList);
                                }
                            }
                        }
                    }
                }
            }
        }
        
        return new BudgetQuotaBalanceResult(quotaMap, balanceMap, relatedBudgetLedgerMap);
    }

    /**
     * 根据 ledger map 查询所有季度（q1-q4）的 BudgetQuota 和 BudgetBalance（支持扩展映射）
     * 
     * @param needUpdateSameDemBudgetLedgerMap key 为 bizCode + "@" + bizItemCode，value 为 BudgetLedger
     * @param needToUpdateSameDemBudgetQuotaMap 需要更新的预算额度Map（用于检查是否已存在）
     * @param needToUpdateSameDemBudgetBalanceMap 需要更新的预算余额Map（用于检查是否已存在）
     * @param ehrCdToOrgCdMap EHR组织编码到管理组织编码的映射（一对一关系）
     * @param ehrCdToOrgCdExtMap EHR组织编码到管理组织编码的映射（一对多关系）
     * @param erpAcctCdToAcctCdMap ERP科目编码到科目编码的映射（一对一关系）
     * @param erpAcctCdToAcctCdExtMap ERP科目编码到科目编码的映射（一对多关系）
     * @param prjCdToRelatedPrjCdExtMap 项目编码到关联项目编码的映射（一对多关系）
     * @param erpAssetTypeToMemberCdMap erpAssetType 映射表（MEMBER_CD2 -> MEMBER_CD），用于将映射前的 erpAssetType 映射为映射后的值
     * @return 包含两个 Map 的结果对象：
     *         - quotaMap: key 为 bizCode + "@" + bizItemCode + "@" + quarter，value 为 BudgetQuota
     *         - balanceMap: key 为 bizCode + "@" + bizItemCode + "@" + quarter，value 为 BudgetBalance
     */
    public BudgetQuotaBalanceSimpleResult queryQuotaAndBalanceByAllQuartersAllDem(
            Map<String, BudgetLedger> needUpdateSameDemBudgetLedgerMap,
            Map<String, BudgetQuota> needToUpdateSameDemBudgetQuotaMap,
            Map<String, BudgetBalance> needToUpdateSameDemBudgetBalanceMap,
            Map<String, String> ehrCdToOrgCdMap,
            Map<String, List<String>> ehrCdToOrgCdExtMap,
            Map<String, String> erpAcctCdToAcctCdMap,
            Map<String, List<String>> erpAcctCdToAcctCdExtMap,
            Map<String, List<String>> prjCdToRelatedPrjCdExtMap,
            Map<String, String> erpAssetTypeToMemberCdMap) {
        if (CollectionUtils.isEmpty(needUpdateSameDemBudgetLedgerMap)) {
            return new BudgetQuotaBalanceSimpleResult(new HashMap<>(), new HashMap<>());
        }
        // 构建查询参数：对每个 ledger，根据扩展映射创建多个查询参数
        // 区分项目查询和非项目查询的参数列表
        List<BudgetPoolDemRMapper.DimensionParam> projectDimensionParams = new ArrayList<>();
        List<BudgetPoolDemRMapper.DimensionParam> nonProjectDimensionParams = new ArrayList<>();
        Map<String, BudgetLedger> bizKeyToLedgerMap = new HashMap<>();
        // 用于记录每个 bizKeyQuarter 对应的所有维度组合（用于后续匹配）
        Map<String, List<String>> bizKeyQuarterToDimensionKeysMap = new HashMap<>();
        
        String[] quarters = {"q1", "q2", "q3", "q4"};
        List<String> errorMessages = new ArrayList<>(); // 收集所有错误信息，不提前返回
        for (Map.Entry<String, BudgetLedger> entry : needUpdateSameDemBudgetLedgerMap.entrySet()) {
            String bizKey = entry.getKey(); // bizCode + "@" + bizItemCode
            BudgetLedger ledger = entry.getValue();
            bizKeyToLedgerMap.put(bizKey, ledger);
            
            String year = ledger.getYear();
            String isInternal = "NAN".equals(ledger.getMasterProjectCode()) ? "1" : ledger.getIsInternal();
            String ehrMorgCode = ledger.getMorgCode();
            String erpBudgetSubjectCode = ledger.getBudgetSubjectCode();
            String masterProjectCode = ledger.getMasterProjectCode();
            String originalErpAssetType = ledger.getErpAssetType();
            
            // 映射 erpAssetType：如果以 "1" 或 "M" 开头，需要通过映射表映射
            // 注意：带项目时不需要映射 erpAssetType
            MapErpAssetTypeResult erpAssetTypeResult = mapErpAssetTypeForQuery(originalErpAssetType, masterProjectCode, 
                    erpAssetTypeToMemberCdMap, "查询 BudgetPoolDemR 时，明细 [" + bizKey + "]");
            if (erpAssetTypeResult.hasError()) {
                errorMessages.add(erpAssetTypeResult.getErrorMessage());
                log.warn("========== queryQuotaAndBalanceByAllQuartersAllDem - 跳过明细（erpAssetType映射失败）: bizKey={}, errorMessage={} ==========",
                        bizKey, erpAssetTypeResult.getErrorMessage());
                continue; // 跳过该明细，继续处理其他明细
            }
            String erpAssetType = erpAssetTypeResult.getMappedValue();

            // 获取主要的 morgCode（一对一映射）
            String mainMorgCode = null;
            if (ehrCdToOrgCdMap != null && !ehrCdToOrgCdMap.isEmpty()) {
                mainMorgCode = ehrCdToOrgCdMap.get(ehrMorgCode);
            }
            
            // 获取所有相关的 morgCode（一对多映射）
            Set<String> allMorgCodes = new HashSet<>();
            if (ehrCdToOrgCdExtMap != null && !ehrCdToOrgCdExtMap.isEmpty()) {
                List<String> extMorgCodes = ehrCdToOrgCdExtMap.get(ehrMorgCode);
                if (!CollectionUtils.isEmpty(extMorgCodes)) {
                    allMorgCodes.addAll(extMorgCodes);
                }
            }

            // 获取主要的 acctCd（一对一映射）
            String mainAcctCd = null;
            // 如果 erpBudgetSubjectCode 是 "NAN-NAN"，直接使用 "NAN-NAN" 作为 mainAcctCd
            if ("NAN-NAN".equals(erpBudgetSubjectCode)) {
                mainAcctCd = "NAN-NAN";
            } else if (erpAcctCdToAcctCdMap != null && !erpAcctCdToAcctCdMap.isEmpty()) {
                mainAcctCd = erpAcctCdToAcctCdMap.get(erpBudgetSubjectCode);
            }
            
            // 获取所有相关的 acctCd（一对多映射）
            Set<String> allAcctCds = new HashSet<>();
            // 判断是否为项目查询（masterProjectCode 不为 "NAN"）
            boolean isProjectQueryByCode = !"NAN".equals(masterProjectCode);
            
            // 如果 erpBudgetSubjectCode 是 "NAN-NAN"，直接使用 "NAN-NAN" 作为 acctCd
            if ("NAN-NAN".equals(erpBudgetSubjectCode)) {
                allAcctCds.add("NAN-NAN");
            } else {
                if (erpAcctCdToAcctCdExtMap != null && !erpAcctCdToAcctCdExtMap.isEmpty()) {
                    List<String> extAcctCds = erpAcctCdToAcctCdExtMap.get(erpBudgetSubjectCode);
                    if (!CollectionUtils.isEmpty(extAcctCds)) {
                        allAcctCds.addAll(extAcctCds);
                    }
                }
                // 对于带项目的单据，如果科目编码映射为空，直接使用占位符 "NAN-NAN"（项目查询时使用占位符）
                if (allAcctCds.isEmpty()) {
                    if (isProjectQueryByCode) {
                        // 带项目的单据，科目编码映射未找到时，使用占位符 "NAN-NAN"（项目查询时使用）
                        allAcctCds.add("NAN-NAN");
                        log.info("========== queryQuotaAndBalanceByAllQuartersAllDem - 带项目的单据，科目编码 {} 未找到映射，使用占位符 NAN-NAN ==========", erpBudgetSubjectCode);
                    } else {
                        // 不带项目的单据，科目编码映射未找到时跳过
                        String errorMessage = "明细 [" + bizKey + "] 的ERP科目编码 " + erpBudgetSubjectCode + " 未找到对应的科目编码映射（扩展映射表）";
                        errorMessages.add(errorMessage);
                        log.warn("========== queryQuotaAndBalanceByAllQuartersAllDem - 跳过明细（科目编码映射失败）: bizKey={}, errorMessage={} ==========",
                                bizKey, errorMessage);
                        continue; // 跳过该明细，继续处理其他明细
                    }
                }
            }

            // 获取所有相关的 prjCd（一对多映射）
            Set<String> allPrjCds = new HashSet<>();
            // 如果 masterProjectCode 是 "NAN"，直接使用 "NAN" 作为 prjCd
            if ("NAN".equals(masterProjectCode)) {
                allPrjCds.add("NAN");
            } else {
                if (prjCdToRelatedPrjCdExtMap != null && !prjCdToRelatedPrjCdExtMap.isEmpty()) {
                    List<String> extPrjCds = prjCdToRelatedPrjCdExtMap.get(masterProjectCode);
                    if (!CollectionUtils.isEmpty(extPrjCds)) {
                        allPrjCds.addAll(extPrjCds);
                    }
                }
                // 如果没有映射到任何 prjCd，跳过该明细
                if (allPrjCds.isEmpty()) {
                    String errorMessage = "[" + masterProjectCode + "]未找到对应的关联项目,还请联系管理员增加项目映射 Invalid Project Code";
                    errorMessages.add(errorMessage);
                    log.warn("========== queryQuotaAndBalanceByAllQuartersAllDem - 跳过明细（项目编码映射失败）: bizKey={}, masterProjectCode={}, errorMessage={} ==========",
                            bizKey, masterProjectCode, errorMessage);
                    continue; // 跳过该明细，继续处理其他明细
                }
            }

            log.info("========== queryQuotaAndBalanceByAllQuartersAllDem - 维度信息汇总: bizKey={}, year={}, isInternal={}, ehrMorgCode={}, erpBudgetSubjectCode={}, masterProjectCode={}, originalErpAssetType={}, mappedErpAssetType={} ==========", 
                    bizKey, year, isInternal, ehrMorgCode, erpBudgetSubjectCode, masterProjectCode, originalErpAssetType, erpAssetType);
            log.info("========== queryQuotaAndBalanceByAllQuartersAllDem - 映射结果: allMorgCodes={}, allAcctCds={}, allPrjCds={} ==========", 
                    allMorgCodes, allAcctCds, allPrjCds);

            // 判断是否为项目查询（allPrjCds 不为空且不全是 "NAN"）
            boolean isProjectQuery = !allPrjCds.isEmpty() && !allPrjCds.stream().allMatch("NAN"::equals);
            
            // 对于带项目的单据，如果映射表为空，直接使用传入的 EHR 组织编码
            if (isProjectQuery && allMorgCodes.isEmpty()) {
                allMorgCodes.add(ehrMorgCode);
                log.info("========== queryQuotaAndBalanceByAllQuartersAllDem - 带项目的单据，EHR组织编码映射表为空，直接使用传入的EHR组织编码: {} ==========", ehrMorgCode);
            } else if (!isProjectQuery && allMorgCodes.isEmpty()) {
                // 非项目查询：如果没有映射到任何 morgCode，跳过该明细
                String errorMessage = "明细 [" + bizKey + "] 的EHR组织编码 " + ehrMorgCode + " 未找到对应的管理组织编码映射（扩展映射表）";
                errorMessages.add(errorMessage);
                log.warn("========== queryQuotaAndBalanceByAllQuartersAllDem - 跳过明细（组织编码映射失败）: bizKey={}, errorMessage={} ==========",
                        bizKey, errorMessage);
                continue; // 跳过该明细，继续处理其他明细
            }

            // 为每个维度组合创建查询参数
            for (String quarter : quarters) {
                String bizKeyQuarter = bizKey + "@" + quarter;
                
                if (isProjectQuery) {
                    // 项目查询：只使用项目维度，morgCode、acctCd 和 erpAssetType 使用占位符
                    // 遍历所有项目，构建项目查询参数和维度键（不遍历组织，因为带项目时组织维度不参与匹配）
                    for (String prjCd : allPrjCds) {
                        // 项目查询的 dimensionKey：year + "@" + quarter + "@" + isInternal + "@" + "NAN" + "@" + "NAN-NAN" + "@" + prjCd + "@" + "NAN"
                        String dimensionKey = year + "@" + quarter + "@" + isInternal + "@" + "NAN" + "@" + "NAN-NAN" + "@" + prjCd + "@" + "NAN";
                        // 项目查询参数：morgCode 使用 "NAN"，acctCd 使用 "NAN-NAN"，erpAssetType 使用 "NAN"
                        projectDimensionParams.add(new BudgetPoolDemRMapper.DimensionParam(year, quarter, isInternal, "NAN", "NAN-NAN", prjCd, "NAN"));
                        
                        bizKeyQuarterToDimensionKeysMap.computeIfAbsent(bizKeyQuarter, k -> new ArrayList<>()).add(dimensionKey);
                    }
                } else {
                    // 非项目查询：使用所有维度
                    // 遍历所有组织、科目和项目，构建非项目查询参数和维度键
                    for (String morgCode : allMorgCodes) {
                        for (String acctCd : allAcctCds) {
                            for (String prjCd : allPrjCds) {
                                String dimensionKey = year + "@" + quarter + "@" + isInternal + "@" + morgCode + "@" + acctCd + "@" + prjCd + "@" + erpAssetType;
                                nonProjectDimensionParams.add(new BudgetPoolDemRMapper.DimensionParam(year, quarter, isInternal, morgCode, acctCd, prjCd, erpAssetType));
                                
                                bizKeyQuarterToDimensionKeysMap.computeIfAbsent(bizKeyQuarter, k -> new ArrayList<>()).add(dimensionKey);
                            }
                        }
                    }
                }
            }
        }
        
        // 分别查询项目维度和非项目维度
        List<BudgetPoolDemR> poolDemRs = new ArrayList<>();
        if (!projectDimensionParams.isEmpty()) {
            log.info("========== queryQuotaAndBalanceByAllQuartersAllDem - 项目查询参数数量: {} ==========", projectDimensionParams.size());
            // 分批查询，避免IN子句参数过多导致超时（每个批次150个参数，4个字段，总共约600个表达式，在Oracle的1000限制内）
            List<BudgetPoolDemR> projectResults = batchSelectByDimensionsWithYearAndQuarterForProject(projectDimensionParams, 150);
            poolDemRs.addAll(projectResults);
        }
        if (!nonProjectDimensionParams.isEmpty()) {
            log.info("========== queryQuotaAndBalanceByAllQuartersAllDem - 非项目查询参数数量: {} ==========", nonProjectDimensionParams.size());
            // 分批查询，避免IN子句参数过多导致超时（每个批次100个参数，Oracle IN子句限制约1000个表达式）
            List<BudgetPoolDemR> nonProjectResults = batchSelectByDimensionsWithYearAndQuarter(nonProjectDimensionParams, 100);
            poolDemRs.addAll(nonProjectResults);
        }
        
        log.info("========== queryQuotaAndBalanceByAllQuartersAllDem - 查询到 {} 条 BudgetPoolDemR ==========", poolDemRs.size());
        
        // 建立 poolId 到 (bizKey, quarter) 的映射
        // 使用 Set 去重，避免同一 poolId@quarter 重复映射到同一个 bizKeyQuarter 时发生重复累加
        Map<Long, Set<String>> poolIdToBizKeyQuarterMap = new HashMap<>();
        Set<Long> poolIdSet = new HashSet<>();
        for (BudgetPoolDemR poolDemR : poolDemRs) {
            Long poolId = poolDemR.getId();
            poolIdSet.add(poolId);
            
            String year = poolDemR.getYear();
            String quarter = poolDemR.getQuarter();
            String isInternal = poolDemR.getIsInternal();
            String morgCode = poolDemR.getMorgCode();
            String budgetSubjectCode = poolDemR.getBudgetSubjectCode();
            String masterProjectCode = poolDemR.getMasterProjectCode();
            String erpAssetType = poolDemR.getErpAssetType();
            
            // 判断是否为项目查询（masterProjectCode 不为 "NAN"）
            boolean isProjectQuery = !"NAN".equals(masterProjectCode);
            String dimensionKey;
            if (isProjectQuery) {
                // 项目查询的 dimensionKey：year + "@" + quarter + "@" + isInternal + "@" + "NAN" + "@" + "NAN-NAN" + "@" + masterProjectCode + "@" + "NAN"
                // 注意：带项目时，morgCode 不参与匹配，使用 "NAN" 作为占位符
                dimensionKey = year + "@" + quarter + "@" + isInternal + "@" + "NAN" + "@" + "NAN-NAN" + "@" + masterProjectCode + "@" + "NAN";
            } else {
                // 非项目查询的 dimensionKey：使用所有维度
                dimensionKey = year + "@" + quarter + "@" + isInternal + "@" + morgCode + "@" + budgetSubjectCode + "@" + masterProjectCode + "@" + erpAssetType;
            }
            
            // 找到所有匹配的 bizKeyQuarter
            boolean matched = false;
            for (Map.Entry<String, List<String>> entry : bizKeyQuarterToDimensionKeysMap.entrySet()) {
                String bizKeyQuarter = entry.getKey();
                List<String> dimensionKeys = entry.getValue();
                if (dimensionKeys.contains(dimensionKey)) {
                    poolIdToBizKeyQuarterMap.computeIfAbsent(poolId, k -> new LinkedHashSet<>()).add(bizKeyQuarter);
                    matched = true;
                    log.debug("========== queryQuotaAndBalanceByAllQuartersAllDem - 匹配成功: poolId={}, dimensionKey={}, bizKeyQuarter={} ==========",
                            poolId, dimensionKey, bizKeyQuarter);
                }
            }
            if (!matched) {
                log.warn("========== queryQuotaAndBalanceByAllQuartersAllDem - 未匹配到bizKeyQuarter: poolId={}, dimensionKey={}, 所有可能的bizKeyQuarter={} ==========",
                        poolId, dimensionKey, bizKeyQuarterToDimensionKeysMap.keySet());
            }
        }
        
        // 查询 BUDGET_QUOTA 和 BUDGET_BALANCE
        List<Long> poolIds = new ArrayList<>(poolIdSet);
        List<BudgetQuota> quotas = loadBudgetQuotasByPoolIds(poolIds);
        List<BudgetBalance> balances = loadBudgetBalancesByPoolIds(poolIds);
        
        log.info("========== queryQuotaAndBalanceByAllQuartersAllDem - 查询到 {} 条 BudgetQuota，{} 条 BudgetBalance ==========", quotas.size(), balances.size());
        
        // 组装结果 Map
        Map<String, List<BudgetQuota>> quotaMap = new HashMap<>();
        Map<String, List<BudgetBalance>> balanceMap = new HashMap<>();
        
        // 组装 quotaMap：优先使用处理后的数据
        for (BudgetQuota quota : quotas) {
            Set<String> bizKeyQuarters = poolIdToBizKeyQuarterMap.get(quota.getPoolId());
            if (bizKeyQuarters != null) {
                for (String bizKeyQuarter : bizKeyQuarters) {
                    // 检查 needToUpdateSameDemBudgetQuotaMap 中是否有处理后的 quota
                    BudgetQuota updatedQuota = needToUpdateSameDemBudgetQuotaMap != null ? 
                            needToUpdateSameDemBudgetQuotaMap.get(bizKeyQuarter) : null;
                    // 如果存在处理后的 quota 且 poolId 相同，使用处理后的 quota；否则使用数据库查询的 quota
                    if (updatedQuota != null && updatedQuota.getPoolId() != null && 
                            updatedQuota.getPoolId().equals(quota.getPoolId())) {
                        quotaMap.computeIfAbsent(bizKeyQuarter, k -> new ArrayList<>()).add(updatedQuota);
                    } else {
                        quotaMap.computeIfAbsent(bizKeyQuarter, k -> new ArrayList<>()).add(quota);
                    }
                }
            }
        }
        
        // 组装 balanceMap：优先使用处理后的数据
        // 当多个明细共享同一资金池时，同一 poolId 会对应多个 bizKeyQuarter；需按 poolId+quarter 查找已回滚更新过的 balance，
        // 否则按 bizKeyQuarter 查找可能取到其它明细的 balance（poolId 不同）而误用数据库旧值；且必须限定 quarter，避免把 q1 的 balance 误用到 q2
        for (BudgetBalance balance : balances) {
            Set<String> bizKeyQuarterSet = poolIdToBizKeyQuarterMap.get(balance.getPoolId());
            if (bizKeyQuarterSet == null || bizKeyQuarterSet.isEmpty()) {
                log.warn("========== queryQuotaAndBalanceByAllQuartersAllDem - balance未匹配到bizKeyQuarter: poolId={}, amountAvailable={}, amountPayAvailable={} ==========",
                        balance.getPoolId(), balance.getAmountAvailable(), balance.getAmountPayAvailable());
                continue;
            }
            List<String> bizKeyQuarters = new ArrayList<>(bizKeyQuarterSet);
            String balanceQuarter = balance.getQuarter();
            // 季度比较统一转小写，与系统内 bizKeyQuarter 格式（xxx@q1）保持一致，避免大小写导致匹配不到
            String balanceQuarterLower = (balanceQuarter != null) ? balanceQuarter.toLowerCase() : null;
            // 只处理与当前 balance 同一季度的 bizKeyQuarter，避免 q1 的 balance 被加到 q2 等
            List<String> sameQuarterBizKeyQuarters = (balanceQuarterLower != null)
                    ? bizKeyQuarters.stream().filter(bq -> bq.toLowerCase().endsWith("@" + balanceQuarterLower)).collect(Collectors.toList())
                    : bizKeyQuarters;
            if (sameQuarterBizKeyQuarters.isEmpty()) {
                // quarter 非空但未匹配到任何 key 时，不打满到全量，避免误把本季度 balance 塞入其他季度
                if (balanceQuarterLower != null) {
                    log.warn("========== queryQuotaAndBalanceByAllQuartersAllDem - balance 季度与 bizKeyQuarter 无匹配: poolId={}, quarter={}, 可选keys示例={} ==========",
                            balance.getPoolId(), balanceQuarter, bizKeyQuarters.size() > 3 ? bizKeyQuarters.subList(0, 3) + "..." : bizKeyQuarters);
                    continue;
                }
                sameQuarterBizKeyQuarters = bizKeyQuarters;
            }
            // 按 poolId+quarter 在“已处理 map”中查找已更新的 balance（如回滚后），供本 pool 本季度下所有 bizKeyQuarter 共用
            BudgetBalance updatedBalanceForPool = null;
            if (needToUpdateSameDemBudgetBalanceMap != null && balance.getPoolId() != null && balanceQuarterLower != null) {
                for (Map.Entry<String, BudgetBalance> entry : needToUpdateSameDemBudgetBalanceMap.entrySet()) {
                    if (balance.getPoolId().equals(entry.getValue().getPoolId())
                            && entry.getKey().toLowerCase().endsWith("@" + balanceQuarterLower)) {
                        updatedBalanceForPool = entry.getValue();
                        break;
                    }
                }
            }
            BudgetBalance balanceToAdd = (updatedBalanceForPool != null) ? updatedBalanceForPool : balance;
            for (String bizKeyQuarter : sameQuarterBizKeyQuarters) {
                balanceMap.computeIfAbsent(bizKeyQuarter, k -> new ArrayList<>()).add(balanceToAdd);
                log.debug("========== queryQuotaAndBalanceByAllQuartersAllDem - 添加{}: poolId={}, quarter={}, bizKeyQuarter={}, amountAvailable={}, amountPayAvailable={} ==========",
                        (updatedBalanceForPool != null ? "处理后的balance" : "查询的balance"),
                        balance.getPoolId(), balanceQuarter, bizKeyQuarter, balanceToAdd.getAmountAvailable(), balanceToAdd.getAmountPayAvailable());
            }
        }
        
        log.info("========== queryQuotaAndBalanceByAllQuartersAllDem - 最终balanceMap大小: {}, keys={} ==========",
                balanceMap.size(), balanceMap.keySet());
        
        // 如果有错误信息，记录警告日志，并带回给调用方用于拼到「找不到预算余额」等异常消息
        if (!errorMessages.isEmpty()) {
            log.warn("========== queryQuotaAndBalanceByAllQuartersAllDem - 处理过程中有 {} 个明细被跳过: errors={} ==========",
                    errorMessages.size(), errorMessages);
        }
        
        return errorMessages.isEmpty()
                ? new BudgetQuotaBalanceSimpleResult(quotaMap, balanceMap)
                : new BudgetQuotaBalanceSimpleResult(quotaMap, balanceMap, new ArrayList<>(errorMessages));
    }

    /**
     * 映射结果类
     */
    public static class MapErpAssetTypeResult {
        private final String mappedValue;
        private final String errorMessage;
        
        public MapErpAssetTypeResult(String mappedValue, String errorMessage) {
            this.mappedValue = mappedValue;
            this.errorMessage = errorMessage;
        }
        
        public String getMappedValue() {
            return mappedValue;
        }
        
        public String getErrorMessage() {
            return errorMessage;
        }
        
        public boolean hasError() {
            return StringUtils.isNotBlank(errorMessage);
        }
    }
    
    /**
     * 映射 erpAssetType（如果以 "1" 或 "M" 开头，需要通过映射表映射）
     * 用于查询 BudgetPoolDemR, BudgetQuota, BudgetBalance 时使用
     * 
     * @param originalErpAssetType 原始的 erpAssetType（映射前的值，来自 BudgetLedger 或入参明细）
     * @param masterProjectCode 主项目编码，如果不为 "NAN" 则带项目，不需要映射 erpAssetType
     * @param erpAssetTypeToMemberCdMap 映射表（MEMBER_CD2 -> MEMBER_CD），从 VIEW_BUDGET_MEMBER_NAME_CODE 视图获取
     * @param errorContext 错误上下文信息（用于错误提示）
     * @return 映射结果，包含映射后的值和错误信息
     */
    public MapErpAssetTypeResult mapErpAssetTypeForQuery(String originalErpAssetType, String masterProjectCode, 
            Map<String, String> erpAssetTypeToMemberCdMap, String errorContext) {
        // 如果带项目（masterProjectCode 不为 "NAN"），不需要映射 erpAssetType，直接返回 "NAN"
        if (!"NAN".equals(masterProjectCode)) {
            return new MapErpAssetTypeResult("NAN", null);
        }
        
        // 如果为空或 "NAN"，直接返回 "NAN"
        if (StringUtils.isBlank(originalErpAssetType) || "NAN".equals(originalErpAssetType)) {
            return new MapErpAssetTypeResult("NAN", null);
        }
        
        // 检查是否需要映射（以 "1" 或 "M" 开头）
        boolean needMapping = originalErpAssetType.startsWith("1") || originalErpAssetType.startsWith("M");
        
        if (!needMapping) {
            // 不需要映射，直接返回原值
            return new MapErpAssetTypeResult(originalErpAssetType, null);
        }
        
        // 需要映射，从映射表中查找
        if (erpAssetTypeToMemberCdMap == null || erpAssetTypeToMemberCdMap.isEmpty()) {
            String errorMessage = String.format("erpAssetType [%s] 需要映射但映射表为空。%s", 
                    originalErpAssetType, errorContext != null ? errorContext : "");
            log.error("========== {}", errorMessage);
            return new MapErpAssetTypeResult(null, errorMessage);
        }
        
        String mappedValue = erpAssetTypeToMemberCdMap.get(originalErpAssetType);
        if (StringUtils.isBlank(mappedValue)) {
            // 映射不到，返回错误信息
            String errorMessage = String.format("未找到erpAssetType资产类型编码映射 [%s]。%s", 
                    originalErpAssetType, errorContext != null ? errorContext : "");
            log.error("========== {}", errorMessage);
            return new MapErpAssetTypeResult(null, errorMessage);
        }
        
        log.debug("========== erpAssetType 映射（查询用）: {} -> {} ==========", originalErpAssetType, mappedValue);
        return new MapErpAssetTypeResult(mappedValue, null);
    }
    
    /**
     * 映射 erpAssetType（如果以 "1" 或 "M" 开头，需要通过映射表映射）
     * 用于查询 BudgetPoolDemR, BudgetQuota, BudgetBalance 时使用
     * 
     * @deprecated 请使用 {@link #mapErpAssetTypeForQuery(String, String, Map, String)} 方法，该方法支持带项目时跳过映射
     * @param originalErpAssetType 原始的 erpAssetType（映射前的值，来自 BudgetLedger 或入参明细）
     * @param erpAssetTypeToMemberCdMap 映射表（MEMBER_CD2 -> MEMBER_CD）
     * @param errorContext 错误上下文信息（用于错误提示）
     * @return 映射后的 erpAssetType（如果不需要映射或映射不到，返回原值或 "NAN"）
     * @throws IllegalArgumentException 如果需要映射但映射不到时抛出异常
     */
    @Deprecated
    public String mapErpAssetTypeForQuery(String originalErpAssetType, Map<String, String> erpAssetTypeToMemberCdMap, String errorContext) {
        // 如果为空或 "NAN"，直接返回 "NAN"
        if (StringUtils.isBlank(originalErpAssetType) || "NAN".equals(originalErpAssetType)) {
            return "NAN";
        }
        
        // 检查是否需要映射（以 "1" 或 "M" 开头）
        boolean needMapping = originalErpAssetType.startsWith("1") || originalErpAssetType.startsWith("M");
        
        if (!needMapping) {
            // 不需要映射，直接返回原值
            return originalErpAssetType;
        }
        
        // 需要映射，从映射表中查找
        if (erpAssetTypeToMemberCdMap == null || erpAssetTypeToMemberCdMap.isEmpty()) {
            String errorMessage = String.format("erpAssetType [%s] 需要映射但映射表为空。%s", 
                    originalErpAssetType, errorContext != null ? errorContext : "");
            log.error("========== {}", errorMessage);
            throw new IllegalArgumentException(errorMessage);
        }
        
        String mappedValue = erpAssetTypeToMemberCdMap.get(originalErpAssetType);
        if (StringUtils.isBlank(mappedValue)) {
            // 映射不到，抛出异常
            String errorMessage = String.format("未找到erpAssetType资产类型编码映射 [%s]。%s", 
                    originalErpAssetType, errorContext != null ? errorContext : "");
            log.error("========== {}", errorMessage);
            throw new IllegalArgumentException(errorMessage);
        }
        
        log.debug("========== erpAssetType 映射（查询用）: {} -> {} ==========", originalErpAssetType, mappedValue);
        return mappedValue;
    }
    
    /**
     * 根据 poolId 列表查询 BudgetQuota（支持分批查询，避免IN子句超过1000个限制）
     */
    public List<BudgetQuota> loadBudgetQuotasByPoolIds(List<Long> poolIds) {
        if (CollectionUtils.isEmpty(poolIds)) {
            return Collections.emptyList();
        }
        
        // 如果poolIds数量小于等于1000，直接查询
        if (poolIds.size() <= 1000) {
            LambdaQueryWrapper<BudgetQuota> wrapper = new LambdaQueryWrapper<>();
            wrapper.in(BudgetQuota::getPoolId, poolIds)
                    .eq(BudgetQuota::getDeleted, Boolean.FALSE);
            return budgetQuotaMapper.selectList(wrapper);
        }
        
        // 分批查询，每批最多1000个poolId
        List<BudgetQuota> allResults = new ArrayList<>();
        int batchSize = 1000;
        int totalBatches = (poolIds.size() + batchSize - 1) / batchSize;
        log.info("========== 分批查询BudgetQuota - 总poolId数: {}, 批次大小: {}, 总批次数: {} ==========", 
                poolIds.size(), batchSize, totalBatches);
        
        for (int i = 0; i < poolIds.size(); i += batchSize) {
            int end = Math.min(i + batchSize, poolIds.size());
            List<Long> batch = new ArrayList<>(poolIds.subList(i, end));
            
            int currentBatch = (i / batchSize) + 1;
            log.debug("========== 执行第 {}/{} 批查询BudgetQuota，poolId数量: {} ==========", currentBatch, totalBatches, batch.size());
            
            LambdaQueryWrapper<BudgetQuota> wrapper = new LambdaQueryWrapper<>();
            wrapper.in(BudgetQuota::getPoolId, batch)
                    .eq(BudgetQuota::getDeleted, Boolean.FALSE);
            List<BudgetQuota> batchResults = budgetQuotaMapper.selectList(wrapper);
            if (!CollectionUtils.isEmpty(batchResults)) {
                allResults.addAll(batchResults);
            }
        }
        
        log.info("========== 分批查询BudgetQuota完成，总共查询到 {} 条记录 ==========", allResults.size());
        return allResults;
    }

    /**
     * 根据 poolId 列表查询 BudgetBalance（支持分批查询，避免IN子句超过1000个限制）
     */
    public List<BudgetBalance> loadBudgetBalancesByPoolIds(List<Long> poolIds) {
        if (CollectionUtils.isEmpty(poolIds)) {
            return Collections.emptyList();
        }
        
        // 如果poolIds数量小于等于1000，直接查询
        if (poolIds.size() <= 1000) {
            LambdaQueryWrapper<BudgetBalance> wrapper = new LambdaQueryWrapper<>();
            wrapper.in(BudgetBalance::getPoolId, poolIds)
                    .eq(BudgetBalance::getDeleted, Boolean.FALSE);
            return budgetBalanceMapper.selectList(wrapper);
        }
        
        // 分批查询，每批最多1000个poolId
        List<BudgetBalance> allResults = new ArrayList<>();
        int batchSize = 1000;
        int totalBatches = (poolIds.size() + batchSize - 1) / batchSize;
        log.info("========== 分批查询BudgetBalance - 总poolId数: {}, 批次大小: {}, 总批次数: {} ==========", 
                poolIds.size(), batchSize, totalBatches);
        
        for (int i = 0; i < poolIds.size(); i += batchSize) {
            int end = Math.min(i + batchSize, poolIds.size());
            List<Long> batch = new ArrayList<>(poolIds.subList(i, end));
            
            int currentBatch = (i / batchSize) + 1;
            log.debug("========== 执行第 {}/{} 批查询BudgetBalance，poolId数量: {} ==========", currentBatch, totalBatches, batch.size());
            
            LambdaQueryWrapper<BudgetBalance> wrapper = new LambdaQueryWrapper<>();
            wrapper.in(BudgetBalance::getPoolId, batch)
                    .eq(BudgetBalance::getDeleted, Boolean.FALSE);
            List<BudgetBalance> batchResults = budgetBalanceMapper.selectList(wrapper);
            if (!CollectionUtils.isEmpty(batchResults)) {
                allResults.addAll(batchResults);
            }
        }
        
        log.info("========== 分批查询BudgetBalance完成，总共查询到 {} 条记录 ==========", allResults.size());
        return allResults;
    }

    /**
     * 计算需要回滚的季度
     * 根据 ledger 的 amountAvailable 和季度消费金额，从当前季度往前查找需要回滚的季度
     * 
     * @param ledger BudgetLedger 对象
     * @return RollbackQuartersResult 包含需要回滚的季度列表和每个季度对应的回滚金额
     */
    public RollbackQuartersResult calculateRollbackQuarters(BudgetLedger ledger) {
        // 获取 ledger 的 amountAvailable（需要回滚的总金额）
        BigDecimal amountAvailable = ledger.getAmountAvailable() == null ? BigDecimal.ZERO : ledger.getAmountAvailable();
        if (amountAvailable.compareTo(BigDecimal.ZERO) <= 0) {
            log.info("========== calculateRollbackQuarters - amountAvailable <= 0，无需回滚: bizCode={}, bizItemCode={}, amountAvailable={} ==========",
                    ledger.getBizCode(), ledger.getBizItemCode(), amountAvailable);
            return new RollbackQuartersResult(Collections.emptyList(), Collections.emptyMap(), BigDecimal.ZERO);
        }
        
        // 获取当前季度：CLAIM 且开关为 0 时优先 actualMonth，否则使用 month
        String monthForQuarter = ledger.getMonth();
        if (CLAIM_BIZ_TYPE.equals(ledger.getBizType())) {
            if (CLAIM_ACTUAL_DATE_SOURCE_FLAG == 0 && StringUtils.isNotBlank(ledger.getActualMonth())) {
                monthForQuarter = ledger.getActualMonth();
            }
        }
        String currentQuarter = convertMonthToQuarter(monthForQuarter);
        if (currentQuarter == null) {
            log.warn("========== calculateRollbackQuarters - 无法确定当前季度: bizCode={}, bizItemCode={}, monthForQuarter={} ==========",
                    ledger.getBizCode(), ledger.getBizItemCode(), monthForQuarter);
            return new RollbackQuartersResult(Collections.emptyList(), Collections.emptyMap(), BigDecimal.ZERO);
        }
        
        // 获取四个季度的消费金额
        BigDecimal amountConsumedQOne = ledger.getAmountConsumedQOne() == null ? BigDecimal.ZERO : ledger.getAmountConsumedQOne();
        BigDecimal amountConsumedQTwo = ledger.getAmountConsumedQTwo() == null ? BigDecimal.ZERO : ledger.getAmountConsumedQTwo();
        BigDecimal amountConsumedQThree = ledger.getAmountConsumedQThree() == null ? BigDecimal.ZERO : ledger.getAmountConsumedQThree();
        BigDecimal amountConsumedQFour = ledger.getAmountConsumedQFour() == null ? BigDecimal.ZERO : ledger.getAmountConsumedQFour();
        
        // 从当前季度开始，往前查找需要回滚的季度
        BigDecimal remainingAmount = amountAvailable;
        String quarter = currentQuarter;
        List<String> quartersToRollback = new ArrayList<>();
        Map<String, BigDecimal> quarterRollbackAmountMap = new HashMap<>();
        
        while (remainingAmount.compareTo(BigDecimal.ZERO) > 0 && quarter != null) {
            // 根据季度获取对应的消费金额
            BigDecimal quarterConsumedAmount;
            switch (quarter) {
                case "q1":
                    quarterConsumedAmount = amountConsumedQOne;
                    break;
                case "q2":
                    quarterConsumedAmount = amountConsumedQTwo;
                    break;
                case "q3":
                    quarterConsumedAmount = amountConsumedQThree;
                    break;
                case "q4":
                    quarterConsumedAmount = amountConsumedQFour;
                    break;
                default:
                    quarterConsumedAmount = BigDecimal.ZERO;
                    break;
            }
            
            if (remainingAmount.compareTo(quarterConsumedAmount) < 0) {
                // amountAvailable < 当前季度的消费金额，只回滚当前季度，回滚金额为 remainingAmount
                quartersToRollback.add(quarter);
                quarterRollbackAmountMap.put(quarter, remainingAmount);
                log.info("========== calculateRollbackQuarters - 回滚季度: bizCode={}, bizItemCode={}, quarter={}, rollbackAmount={}, quarterConsumedAmount={} ==========",
                        ledger.getBizCode(), ledger.getBizItemCode(), quarter, remainingAmount, quarterConsumedAmount);
                remainingAmount = BigDecimal.ZERO;
            } else {
                // amountAvailable >= 当前季度的消费金额，回滚当前季度（金额为当前季度的消费金额）
                quartersToRollback.add(quarter);
                quarterRollbackAmountMap.put(quarter, quarterConsumedAmount);
                remainingAmount = remainingAmount.subtract(quarterConsumedAmount);
                log.info("========== calculateRollbackQuarters - 回滚季度: bizCode={}, bizItemCode={}, quarter={}, rollbackAmount={}, remainingAmount={} ==========",
                        ledger.getBizCode(), ledger.getBizItemCode(), quarter, quarterConsumedAmount, remainingAmount);
                
                // 继续往前找上一季度
                quarter = getPreviousQuarter(quarter);
            }
        }
        
        log.info("========== calculateRollbackQuarters - 需要回滚的季度: bizCode={}, bizItemCode={}, quarters={}, totalAmount={} ==========",
                ledger.getBizCode(), ledger.getBizItemCode(), quartersToRollback, amountAvailable);
        
        return new RollbackQuartersResult(quartersToRollback, quarterRollbackAmountMap, amountAvailable);
    }
    
    /**
     * 将月份转换为季度
     * 
     * @param month 月份字符串（1-12）
     * @return 季度字符串（q1, q2, q3, q4），如果月份无效则返回 null
     */
    public String convertMonthToQuarter(String month) {
        if (StringUtils.isBlank(month)) {
            return null;
        }
        try {
            int monthInt = Integer.parseInt(month);
            if (monthInt >= 1 && monthInt <= 3) {
                return "q1";
            } else if (monthInt >= 4 && monthInt <= 6) {
                return "q2";
            } else if (monthInt >= 7 && monthInt <= 9) {
                return "q3";
            } else if (monthInt >= 10 && monthInt <= 12) {
                return "q4";
            }
        } catch (NumberFormatException e) {
            log.warn("========== convertMonthToQuarter - 月份格式错误: month={} ==========", month);
        }
        return null;
    }
    
    /**
     * 获取上一季度
     * 
     * @param quarter 当前季度（q1, q2, q3, q4）
     * @return 上一季度，如果当前是 q1 则返回 null
     */
    public String getPreviousQuarter(String quarter) {
        if ("q1".equals(quarter)) {
            return null; // q1 没有上一季度
        } else if ("q2".equals(quarter)) {
            return "q1";
        } else if ("q3".equals(quarter)) {
            return "q2";
        } else if ("q4".equals(quarter)) {
            return "q3";
        }
        return null;
    }

    /**
     * 按流水 {@link BudgetLedger#getPoolDimensionKey()} 与回滚季度定位 {@link BudgetBalance}，与扣减时
     * {@link com.jasolar.mis.module.system.domain.budget.BudgetLedger#buildPoolDimensionKeyFromBalance} 一致。
     * <p>
     * 格式：year@ {@code isInternal} @morgCode@（CUSTOM_CODE-ACCOUNT_SUBJECT_CODE）@projectCode@erpAssetType；
     * 季度来自 {@link #calculateRollbackQuarters} 所用的 {@code rollbackQuarter}（与 ledger 的 month 推导的 q1–q4 一致）。
     */
    public BudgetBalance selectBudgetBalanceByPoolDimensionKey(String poolDimensionKey, String rollbackQuarter) {
        if (StringUtils.isBlank(poolDimensionKey) || StringUtils.isBlank(rollbackQuarter)) {
            throw new IllegalStateException("按 POOL_DIMENSION_KEY 查询 BUDGET_BALANCE 时 poolDimensionKey 与 rollbackQuarter 均不能为空");
        }
        String[] parts = poolDimensionKey.split("@", -1);
        if (parts.length != 6) {
            log.error("POOL_DIMENSION_KEY 段数异常(应为6): key={}, segments={}", poolDimensionKey, parts.length);
            throw new IllegalStateException(String.format(
                    "POOL_DIMENSION_KEY 格式无效，应为 year@isInternal@morgCode@预算科目@projectCode@erpAssetType，当前: [%s]",
                    poolDimensionKey));
        }
        String year = parts[0];
        String isInternal = parts[1];
        String morgCode = parts[2];
        String subjectCompound = parts[3];
        String projectCode = parts[4];
        String erpAssetType = parts[5];

        final String customCode;
        final String accountSubjectCode;
        if (StringUtils.isBlank(subjectCompound) || "NAN-NAN".equals(subjectCompound)) {
            customCode = "NAN";
            accountSubjectCode = "NAN";
        } else {
            int dashIdx = subjectCompound.indexOf('-');
            if (dashIdx > 0 && dashIdx < subjectCompound.length() - 1) {
                customCode = subjectCompound.substring(0, dashIdx);
                accountSubjectCode = subjectCompound.substring(dashIdx + 1);
            } else {
                customCode = "NAN";
                accountSubjectCode = subjectCompound;
            }
        }

        String quarterNorm = rollbackQuarter.trim().toLowerCase(Locale.ROOT);

        LambdaQueryWrapper<BudgetBalance> w = new LambdaQueryWrapper<>();
        w.eq(BudgetBalance::getYear, year)
                .eq(BudgetBalance::getQuarter, quarterNorm)
                .eq(BudgetBalance::getIsInternal, isInternal)
                .eq(BudgetBalance::getMorgCode, morgCode)
                .eq(BudgetBalance::getCustomCode, customCode)
                .eq(BudgetBalance::getAccountSubjectCode, accountSubjectCode)
                .eq(BudgetBalance::getProjectCode, projectCode)
                .eq(BudgetBalance::getErpAssetType, erpAssetType);
        List<BudgetBalance> list = budgetBalanceMapper.selectList(w);
        if (CollectionUtils.isEmpty(list)) {
            throw new IllegalStateException(String.format(
                    "按 POOL_DIMENSION_KEY 未找到预算余额: poolDimensionKey=%s, quarter=%s。请确认 BUDGET_BALANCE 存在该维度与季度的记录。",
                    poolDimensionKey, quarterNorm));
        }
        if (list.size() > 1) {
            log.warn("========== POOL_DIMENSION_KEY 匹配到多条 BUDGET_BALANCE，取 id 最小的一条: key={}, quarter={}, count={} ==========",
                    poolDimensionKey, quarterNorm, list.size());
            list.sort(Comparator.comparing(BudgetBalance::getId, Comparator.nullsLast(Long::compareTo)));
        }
        return list.get(0);
    }

    /**
     * 重置 BudgetLedger 的所有季度消耗金额为 0
     *
     * @param ledger BudgetLedger 对象
     */
    public void resetQuarterlyConsumedAmounts(BudgetLedger ledger) {
        if (ledger == null) {
            return;
        }
        ledger.setAmountConsumedQOne(BigDecimal.ZERO);
        ledger.setAmountConsumedQTwo(BigDecimal.ZERO);
        ledger.setAmountConsumedQThree(BigDecimal.ZERO);
        ledger.setAmountConsumedQFour(BigDecimal.ZERO);
    }

    /**
     * 更新 BudgetLedger 的金额、可用金额、月份和版本，并重置所有季度消耗金额为 0
     *
     * @param ledger BudgetLedger 对象
     * @param newAmount 新的金额
     * @param newMonth 新的月份（可选，如果为 null 则不更新月份）
     * @param identifierGenerator ID 生成器，用于生成新版本号
     */
    public void updateBudgetLedgerAmountAndVersion(BudgetLedger ledger, BigDecimal newAmount, String newMonth, 
                                                     com.baomidou.mybatisplus.core.incrementer.IdentifierGenerator identifierGenerator) {
        if (ledger == null || newAmount == null || identifierGenerator == null) {
            return;
        }
        
        // 更新版本信息
        ledger.setVersionPre(ledger.getVersion());
        ledger.setVersion(String.valueOf(identifierGenerator.nextId(null)));
        
        // 更新月份（如果提供）
        if (newMonth != null) {
            ledger.setMonth(newMonth);
        }
        
        // 更新金额和可用金额
        ledger.setAmount(newAmount);
        ledger.setAmountAvailable(newAmount);
        
        // 重置所有季度消耗金额为 0
        resetQuarterlyConsumedAmounts(ledger);
    }

    /**
     * 创建新的 BudgetLedger 对象，并设置基本字段和金额字段，所有季度消耗金额初始化为 0
     *
     * @param id BudgetLedger 的 ID（如果为 null 则不设置）
     * @param bizType 业务类型
     * @param bizCode 业务单号
     * @param bizItemCode 业务行号
     * @param year 年度
     * @param month 月份
     * @param isInternal 是否内部项目
     * @param morgCode 管理组织编码
     * @param budgetSubjectCode 预算科目编码
     * @param masterProjectCode 主数据项目编码
     * @param currency 币种
     * @param amount 金额（会同时设置为 amount 和 amountAvailable）
     * @param versionPre 上一版本号（可选）
     * @param identifierGenerator ID 生成器，用于生成新版本号
     * @param operator 操作人（可选）
     * @param operatorNo 操作人工号（可选）
     * @param requestTime ESB 请求时间，用于 CREATE_TIME/UPDATE_TIME，可为 null（则使用系统时间）
     * @return 创建的 BudgetLedger 对象
     */
    public BudgetLedger createBudgetLedger(Long id, String bizType, String bizCode, String bizItemCode,
                                           String year, String month, String isInternal, String morgCode, String budgetSubjectCode,
                                           String masterProjectCode, String erpAssetType, String currency, BigDecimal amount,
                                           String versionPre, com.baomidou.mybatisplus.core.incrementer.IdentifierGenerator identifierGenerator,
                                           String operator, String operatorNo, LocalDateTime requestTime) {
        BudgetLedger ledger = new BudgetLedger();
        
        if (id != null) {
            ledger.setId(id);
        }
        ledger.setBizType(bizType);
        ledger.setBizCode(bizCode);
        ledger.setBizItemCode(bizItemCode);
        ledger.setYear(year);
        ledger.setMonth(month);
        ledger.setMorgCode(morgCode);
        fillEhrControlSnapshotFields(ledger, morgCode);
        ledger.setBudgetSubjectCode(StringUtils.isBlank(budgetSubjectCode) ? "NAN-NAN" : budgetSubjectCode);
        String resolvedMasterProjectCode = StringUtils.isBlank(masterProjectCode) ? "NAN" : masterProjectCode;
        ledger.setMasterProjectCode(resolvedMasterProjectCode);
        ledger.setErpAssetType(StringUtils.isBlank(erpAssetType) ? "NAN" : erpAssetType);
        ledger.setIsInternal(isInternal);
        ledger.setCurrency(currency);
        
        BigDecimal amountValue = amount == null ? BigDecimal.ZERO : amount;
        ledger.setAmount(amountValue);
        ledger.setAmountAvailable(amountValue);
        
        // 重置所有季度消耗金额为 0
        resetQuarterlyConsumedAmounts(ledger);
        
        ledger.setVersion(String.valueOf(identifierGenerator.nextId(null)));
        if (versionPre != null) {
            ledger.setVersionPre(versionPre);
        }
        ledger.setDeleted(Boolean.FALSE);
        
        // 设置操作人字段
        ledger.setOperator(operator);
        ledger.setOperatorNo(operatorNo);
        // 使用 ESB requestTime 作为 CREATE_TIME/UPDATE_TIME（若有）
        if (requestTime != null) {
            ledger.setCreateTime(requestTime);
            ledger.setUpdateTime(requestTime);
        }
        // 扣减来源由 fillDeductionSourceKeys 在落库前统一填充：扣减在资金池时用实际扣减的 BUDGET_BALANCE 维度（映射后）拼接 poolDimensionKey，
        // 扣减在关联流水时填 deductionFromLedgerBizKey 并清空 poolDimensionKey。此处不预填，避免使用流水自身维度（未映射编码）。
        ledger.setPoolDimensionKey(null);

        return ledger;
    }

    private void fillEhrControlSnapshotFields(BudgetLedger ledger, String ehrCd) {
        EhrControlLevelSnapshotValue fallback = EhrControlLevelSnapshotValue.nanValue();
        if (ledger == null) {
            return;
        }
        if (StringUtils.isBlank(ehrCd)) {
            ledger.setControlEhrCd(fallback.getControlEhrCd());
            ledger.setControlEhrNm(fallback.getControlEhrNm());
            ledger.setBudgetOrgCd(fallback.getBudgetOrgCd());
            ledger.setBudgetOrgNm(fallback.getBudgetOrgNm());
            return;
        }
        try {
            Map<String, EhrControlLevelSnapshotValue> snapshotMap =
                    ehrControlLevelSnapshotService.getSnapshotByEhrCds(Collections.singleton(ehrCd));
            EhrControlLevelSnapshotValue value = snapshotMap != null ? snapshotMap.get(ehrCd) : null;
            if (value == null) {
                value = fallback;
            }
            ledger.setControlEhrCd(value.getControlEhrCd());
            ledger.setControlEhrNm(value.getControlEhrNm());
            ledger.setBudgetOrgCd(value.getBudgetOrgCd());
            ledger.setBudgetOrgNm(value.getBudgetOrgNm());
        } catch (Exception e) {
            log.warn("createBudgetLedger 填充EHR控制层级快照失败，ehrCd={}", ehrCd, e);
            ledger.setControlEhrCd(fallback.getControlEhrCd());
            ledger.setControlEhrNm(fallback.getControlEhrNm());
            ledger.setBudgetOrgCd(fallback.getBudgetOrgCd());
            ledger.setBudgetOrgNm(fallback.getBudgetOrgNm());
        }
    }

    /**
     * 查询结果封装类
     */
    public static class BudgetQuotaBalanceResult {
        private final Map<String, BudgetQuota> quotaMap;
        private final Map<String, BudgetBalance> balanceMap;
        private final Map<String, List<BudgetLedger>> relatedBudgetLedgerMap;
        private final String errorMessage;
        private final String errorDetailKey; // 用于标识具体是哪个明细出错，格式：bizCode + "@" + bizItemCode

        public BudgetQuotaBalanceResult(Map<String, BudgetQuota> quotaMap, Map<String, BudgetBalance> balanceMap, Map<String, List<BudgetLedger>> relatedBudgetLedgerMap) {
            this(quotaMap, balanceMap, relatedBudgetLedgerMap, null, null);
        }

        public BudgetQuotaBalanceResult(Map<String, BudgetQuota> quotaMap, Map<String, BudgetBalance> balanceMap, Map<String, List<BudgetLedger>> relatedBudgetLedgerMap, String errorMessage, String errorDetailKey) {
            this.quotaMap = quotaMap;
            this.balanceMap = balanceMap;
            this.relatedBudgetLedgerMap = relatedBudgetLedgerMap;
            this.errorMessage = errorMessage;
            this.errorDetailKey = errorDetailKey;
        }

        public Map<String, BudgetQuota> getQuotaMap() {
            return quotaMap;
        }

        public Map<String, BudgetBalance> getBalanceMap() {
            return balanceMap;
        }

        public Map<String, List<BudgetLedger>> getRelatedBudgetLedgerMap() {
            return relatedBudgetLedgerMap;
        }

        public String getErrorMessage() {
            return errorMessage;
        }

        public String getErrorDetailKey() {
            return errorDetailKey;
        }

        public boolean hasError() {
            return StringUtils.isNotBlank(errorMessage);
        }
    }
    
    /**
     * 查询结果封装类（简化版，只包含 quotaMap 和 balanceMap）
     * detailErrorMessages：查询时因映射失败等被跳过的明细原因，用于拼到「找不到预算余额」等异常消息便于排查
     */
    public static class BudgetQuotaBalanceSimpleResult {
        private final Map<String, List<BudgetQuota>> quotaMap;
        private final Map<String, List<BudgetBalance>> balanceMap;
        private final String errorMessage;
        private final String errorDetailKey; // 用于标识具体是哪个明细出错，格式：bizCode + "@" + bizItemCode
        private final List<String> detailErrorMessages; // 被跳过的明细原因列表，可拼到「找不到预算余额」等返回

        public BudgetQuotaBalanceSimpleResult(Map<String, List<BudgetQuota>> quotaMap, Map<String, List<BudgetBalance>> balanceMap) {
            this(quotaMap, balanceMap, null, null, null);
        }

        public BudgetQuotaBalanceSimpleResult(Map<String, List<BudgetQuota>> quotaMap, Map<String, List<BudgetBalance>> balanceMap, String errorMessage, String errorDetailKey) {
            this(quotaMap, balanceMap, errorMessage, errorDetailKey, null);
        }

        public BudgetQuotaBalanceSimpleResult(Map<String, List<BudgetQuota>> quotaMap, Map<String, List<BudgetBalance>> balanceMap, List<String> detailErrorMessages) {
            this(quotaMap, balanceMap, null, null, detailErrorMessages);
        }

        private BudgetQuotaBalanceSimpleResult(Map<String, List<BudgetQuota>> quotaMap, Map<String, List<BudgetBalance>> balanceMap,
                String errorMessage, String errorDetailKey, List<String> detailErrorMessages) {
            this.quotaMap = quotaMap;
            this.balanceMap = balanceMap;
            this.errorMessage = errorMessage;
            this.errorDetailKey = errorDetailKey;
            this.detailErrorMessages = detailErrorMessages;
        }

        public Map<String, List<BudgetQuota>> getQuotaMap() {
            return quotaMap;
        }

        public Map<String, List<BudgetBalance>> getBalanceMap() {
            return balanceMap;
        }

        public String getErrorMessage() {
            return errorMessage;
        }

        public String getErrorDetailKey() {
            return errorDetailKey;
        }

        public List<String> getDetailErrorMessages() {
            return detailErrorMessages;
        }

        public boolean hasError() {
            return StringUtils.isNotBlank(errorMessage);
        }
    }
    
    /**
     * 回滚季度计算结果封装类
     */
    public static class RollbackQuartersResult {
        private final List<String> quartersToRollback;
        private final Map<String, BigDecimal> quarterRollbackAmountMap;
        private final BigDecimal totalAmount;

        public RollbackQuartersResult(List<String> quartersToRollback, Map<String, BigDecimal> quarterRollbackAmountMap, BigDecimal totalAmount) {
            this.quartersToRollback = quartersToRollback;
            this.quarterRollbackAmountMap = quarterRollbackAmountMap;
            this.totalAmount = totalAmount;
        }

        public List<String> getQuartersToRollback() {
            return quartersToRollback;
        }

        public Map<String, BigDecimal> getQuarterRollbackAmountMap() {
            return quarterRollbackAmountMap;
        }

        public BigDecimal getTotalAmount() {
            return totalAmount;
        }
    }
    
    /**
     * EHR组织编码到管理组织编码映射结果封装类
     */
    public static class EhrCdToOrgCdMapResult {
        private final Map<String, String> ehrCdToOrgCdMap;
        private final Map<String, String> ehrCdToEhrNmMap;

        public EhrCdToOrgCdMapResult(Map<String, String> ehrCdToOrgCdMap, Map<String, String> ehrCdToEhrNmMap) {
            this.ehrCdToOrgCdMap = ehrCdToOrgCdMap;
            this.ehrCdToEhrNmMap = ehrCdToEhrNmMap;
        }

        public Map<String, String> getEhrCdToOrgCdMap() {
            return ehrCdToOrgCdMap;
        }

        public Map<String, String> getEhrCdToEhrNmMap() {
            return ehrCdToEhrNmMap;
        }
    }
    
    /**
     * ERP科目编码到科目编码映射结果封装类
     */
    public static class ErpAcctCdToAcctCdMapResult {
        private final Map<String, String> erpAcctCdToAcctCdMap;
        private final Map<String, String> erpAcctCdToErpAcctNmMap;

        public ErpAcctCdToAcctCdMapResult(Map<String, String> erpAcctCdToAcctCdMap, Map<String, String> erpAcctCdToErpAcctNmMap) {
            this.erpAcctCdToAcctCdMap = erpAcctCdToAcctCdMap;
            this.erpAcctCdToErpAcctNmMap = erpAcctCdToErpAcctNmMap;
        }

        public Map<String, String> getErpAcctCdToAcctCdMap() {
            return erpAcctCdToAcctCdMap;
        }

        public Map<String, String> getErpAcctCdToErpAcctNmMap() {
            return erpAcctCdToErpAcctNmMap;
        }
    }
    
    /**
     * 创建或更新 BudgetLedgerHead
     * 
     * @param bizCode 业务单号
     * @param bizType 业务类型
     * @param documentName 单据名称
     * @param dataSource 数据来源
     * @param processName 流程名称（可选，用于展示）
     * @param status 状态
     * @param identifierGenerator ID 生成器
     * @param operator 操作人（可选）
     * @param operatorNo 操作人工号（可选）
     * @param requestTime ESB 请求时间，用于 CREATE_TIME/UPDATE_TIME，可为 null（则使用系统时间）
     * @return BudgetLedgerHead 对象（新建或更新后的对象）
     */
    public BudgetLedgerHead createOrUpdateBudgetLedgerHead(String bizCode, String bizType, 
                                                            String documentName, String dataSource, 
                                                            String processName,
                                                            String status, 
                                                            com.baomidou.mybatisplus.core.incrementer.IdentifierGenerator identifierGenerator,
                                                            String operator, String operatorNo, LocalDateTime requestTime) {
        // 根据 bizCode 查询 BUDGET_LEDGER_HEAD
        LambdaQueryWrapper<BudgetLedgerHead> headWrapper = new LambdaQueryWrapper<>();
        headWrapper.eq(BudgetLedgerHead::getBizCode, bizCode)
                .eq(BudgetLedgerHead::getDeleted, Boolean.FALSE);
        BudgetLedgerHead existingHead = budgetLedgerHeadMapper.selectOne(headWrapper);
        
        if (existingHead != null) {
            // 如果存在，创建历史记录并更新
            BudgetLedgerHeadHistory headHistory = new BudgetLedgerHeadHistory();
            BeanUtils.copyProperties(existingHead, headHistory);
            headHistory.setId(identifierGenerator.nextId(headHistory).longValue());
            headHistory.setLedgerHeadId(existingHead.getId());
            headHistory.setDeleted(Boolean.FALSE);
            
            // 插入历史记录
            budgetLedgerHeadHistoryMapper.insert(headHistory);
            
            // 更新状态和版本
            existingHead.setDocumentName(documentName);
            existingHead.setDataSource(dataSource);
            existingHead.setProcessName(processName);
            existingHead.setVersion(String.valueOf(identifierGenerator.nextId(null)));
            existingHead.setStatus(status);
            // 设置操作人字段
            existingHead.setOperator(operator);
            existingHead.setOperatorNo(operatorNo);
            if (requestTime != null) {
                existingHead.setUpdateTime(requestTime);
            }
            budgetLedgerHeadMapper.updateById(existingHead);
            
            return existingHead;
        } else {
            // 如果不存在，创建新记录
            BudgetLedgerHead newHead = new BudgetLedgerHead();
            newHead.setBizType(bizType);
            newHead.setBizCode(bizCode);
            newHead.setDocumentName(documentName);
            newHead.setDataSource(dataSource);
            newHead.setProcessName(processName);
            newHead.setVersion(String.valueOf(identifierGenerator.nextId(null)));
            newHead.setStatus(status);
            newHead.setDeleted(Boolean.FALSE);
            // 设置操作人字段
            newHead.setOperator(operator);
            newHead.setOperatorNo(operatorNo);
            if (requestTime != null) {
                newHead.setCreateTime(requestTime);
                newHead.setUpdateTime(requestTime);
            }
            budgetLedgerHeadMapper.insert(newHead);
            
            return newHead;
        }
    }

    /**
     * 批量查询 EHR_ORG_MANAGE_EXT_R 表，根据 EHR_CD 列表获取对应的所有 ORG_CD
     * 
     * @param ehrCds EHR组织编码列表（对应数据库字段EHR_CD）
     * @return Map<EHR_CD, List<ORG_CD>>，key为EHR_CD，value为该EHR_CD对应的所有ORG_CD列表
     * @throws IllegalArgumentException 如果参数为空或没有映射到数据
     */
    public Map<String, List<String>> queryEhrCdToOrgCdMap(Collection<String> ehrCds) {
        if (CollectionUtils.isEmpty(ehrCds)) {
            throw new IllegalArgumentException("EHR组织编码列表不能为空");
        }
        
        // 过滤空值
        Set<String> validEhrCds = ehrCds.stream()
                .filter(StringUtils::isNotBlank)
                .collect(Collectors.toSet());
        
        if (validEhrCds.isEmpty()) {
            throw new IllegalArgumentException("EHR组织编码列表中没有有效的编码");
        }
        
        // 批量查询 EHR_ORG_MANAGE_EXT_R 表
        List<EhrOrgManageExtR> ehrOrgManageExtRList = ehrOrgManageExtRMapper.selectList(
                new LambdaQueryWrapper<EhrOrgManageExtR>()
                        .in(EhrOrgManageExtR::getEhrCd, validEhrCds)
                        .eq(EhrOrgManageExtR::getDeleted, false)
        );
        
        // 转换成 Map<EHR_CD, List<ORG_CD>> 格式
        Map<String, List<String>> ehrCdToOrgCdMap = ehrOrgManageExtRList.stream()
                .filter(e -> StringUtils.isNotBlank(e.getEhrCd()) && StringUtils.isNotBlank(e.getOrgCd()))
                .collect(Collectors.groupingBy(
                        EhrOrgManageExtR::getEhrCd,
                        Collectors.mapping(
                                EhrOrgManageExtR::getOrgCd,
                                Collectors.toList()
                        )
                ));
        
        // 检查是否有未映射到的EHR_CD（仅记录日志，不抛出异常，由调用方决定如何处理）
        Set<String> unmappedEhrCds = new HashSet<>(validEhrCds);
        unmappedEhrCds.removeAll(ehrCdToOrgCdMap.keySet());
        if (!unmappedEhrCds.isEmpty()) {
            log.warn("========== queryEhrCdToOrgCdMap - 未找到映射的EHR组织编码（返回部分映射）: {} ==========", unmappedEhrCds);
        }
        
        log.info("批量查询EHR_ORG_MANAGE_EXT_R表，获取到 {} 个EHR_CD对应的ORG_CD映射", ehrCdToOrgCdMap.size());
        
        return ehrCdToOrgCdMap;
    }

    /**
     * 批量查询 EHR_ORG_MANAGE_ONE_R 表，根据 EHR_CD 列表获取对应的 ORG_CD（一一对应关系）
     * 
     * @param ehrCds EHR组织编码列表（对应数据库字段EHR_CD）
     * @return EhrCdToOrgCdMapResult，包含 ehrCdToOrgCdMap（key为EHR_CD，value为该EHR_CD对应的ORG_CD，一对一关系）
     */
    public EhrCdToOrgCdMapResult queryEhrCdToOrgCdOneToOneMap(Collection<String> ehrCds) {
        if (CollectionUtils.isEmpty(ehrCds)) {
            return new EhrCdToOrgCdMapResult(Collections.emptyMap(), Collections.emptyMap());
        }
        
        // 过滤空值
        Set<String> validEhrCds = ehrCds.stream()
                .filter(StringUtils::isNotBlank)
                .collect(Collectors.toSet());
        
        if (validEhrCds.isEmpty()) {
            return new EhrCdToOrgCdMapResult(Collections.emptyMap(), Collections.emptyMap());
        }
        
        // 批量查询 EHR_ORG_MANAGE_ONE_R 表
        List<EhrOrgManageOneR> ehrOrgManageOneRList = ehrOrgManageOneRMapper.selectList(
                new LambdaQueryWrapper<EhrOrgManageOneR>()
                        .in(EhrOrgManageOneR::getEhrCd, validEhrCds)
                        .eq(EhrOrgManageOneR::getDeleted, false)
        );
        
        // 转换成 Map<EHR_CD, ORG_CD> 格式（一对一关系，如果有重复key，保留第一个）
        Map<String, String> ehrCdToOrgCdMap = ehrOrgManageOneRList.stream()
                .filter(e -> StringUtils.isNotBlank(e.getEhrCd()) && StringUtils.isNotBlank(e.getOrgCd()))
                .collect(Collectors.toMap(
                        EhrOrgManageOneR::getEhrCd,
                        EhrOrgManageOneR::getOrgCd,
                        (existing, replacement) -> existing // 如果有重复key，保留第一个
                ));
        
        // EHR_ORG_MANAGE_ONE_R 表没有 EHR_NM 字段，需要从 EHR_ORG_MANAGE_R 表查询
        // 批量查询 EHR_ORG_MANAGE_R 表获取 EHR_NM
        List<EhrOrgManageR> ehrOrgManageRList = ehrOrgManageRMapper.selectList(
                new LambdaQueryWrapper<EhrOrgManageR>()
                        .in(EhrOrgManageR::getEhrCd, validEhrCds)
                        .eq(EhrOrgManageR::getDeleted, false)
        );
        
        // 转换成 Map<EHR_CD, EHR_NM> 格式（一对一关系，如果有重复key，保留第一个）
        Map<String, String> ehrCdToEhrNmMap = ehrOrgManageRList.stream()
                .filter(e -> StringUtils.isNotBlank(e.getEhrCd()) && StringUtils.isNotBlank(e.getEhrNm()))
                .collect(Collectors.toMap(
                        EhrOrgManageR::getEhrCd,
                        EhrOrgManageR::getEhrNm,
                        (existing, replacement) -> existing // 如果有重复key，保留第一个
                ));
        
        log.info("批量查询EHR_ORG_MANAGE_ONE_R表，获取到 {} 个EHR_CD对应的ORG_CD映射", ehrCdToOrgCdMap.size());
        
        return new EhrCdToOrgCdMapResult(ehrCdToOrgCdMap, ehrCdToEhrNmMap);
    }

    /**
     * 批量查询 SUBJECT_INFO 表，根据 ERP_ACCT_CD 列表获取对应的 ACCT_CD（一一对应关系）
     * 
     * @param erpAcctCds ERP科目编码列表（对应数据库字段ERP_ACCT_CD）
     * @return ErpAcctCdToAcctCdMapResult，包含 erpAcctCdToAcctCdMap（key为ERP_ACCT_CD，value为该ERP_ACCT_CD对应的ACCT_CD，格式为 cust1Cd + "-" + acctCd，一对一关系）和 erpAcctCdToErpAcctNmMap（key为ERP_ACCT_CD，value为该ERP_ACCT_CD对应的ERP_ACCT_NM）
     */
    public ErpAcctCdToAcctCdMapResult queryErpAcctCdToAcctCdOneToOneMap(Collection<String> erpAcctCds) {
        if (CollectionUtils.isEmpty(erpAcctCds)) {
            return new ErpAcctCdToAcctCdMapResult(Collections.emptyMap(), Collections.emptyMap());
        }
        
        // 过滤空值
        Set<String> validErpAcctCds = erpAcctCds.stream()
                .filter(StringUtils::isNotBlank)
                .collect(Collectors.toSet());
        
        if (validErpAcctCds.isEmpty()) {
            return new ErpAcctCdToAcctCdMapResult(Collections.emptyMap(), Collections.emptyMap());
        }
        
        // 批量查询 SUBJECT_INFO 表
        List<SubjectInfo> subjectInfoList = subjectInfoMapper.selectList(
                new LambdaQueryWrapper<SubjectInfo>()
                        .in(SubjectInfo::getErpAcctCd, validErpAcctCds)
                        .eq(SubjectInfo::getDeleted, false)
        );
        
        // 转换成 Map<ERP_ACCT_CD, ACCT_CD> 格式（一对一关系，如果有重复key，保留第一个）
        // ACCT_CD 格式为 cust1Cd + "-" + acctCd，与 BudgetPoolDemR.budgetSubjectCode 格式一致
        Map<String, String> erpAcctCdToAcctCdMap = subjectInfoList.stream()
                .filter(e -> StringUtils.isNotBlank(e.getErpAcctCd()) && StringUtils.isNotBlank(e.getAcctCd()))
                .collect(Collectors.toMap(
                        SubjectInfo::getErpAcctCd,
                        e -> {
                            // 构建 cust1Cd + "-" + acctCd 格式
                            String cust1Cd = StringUtils.isNotBlank(e.getCust1Cd()) ? e.getCust1Cd() : "";
                            return cust1Cd + "-" + e.getAcctCd();
                        },
                        (existing, replacement) -> existing // 如果有重复key，保留第一个
                ));
        
        // 转换成 Map<ERP_ACCT_CD, ERP_ACCT_NM> 格式（一对一关系，如果有重复key，保留第一个）
        Map<String, String> erpAcctCdToErpAcctNmMap = subjectInfoList.stream()
                .filter(e -> StringUtils.isNotBlank(e.getErpAcctCd()) && StringUtils.isNotBlank(e.getErpAcctNm()))
                .collect(Collectors.toMap(
                        SubjectInfo::getErpAcctCd,
                        SubjectInfo::getErpAcctNm,
                        (existing, replacement) -> existing // 如果有重复key，保留第一个
                ));
        
        log.info("批量查询SUBJECT_INFO表，获取到 {} 个ERP_ACCT_CD对应的ACCT_CD映射（格式为 cust1Cd + \"-\" + acctCd）", erpAcctCdToAcctCdMap.size());
        
        return new ErpAcctCdToAcctCdMapResult(erpAcctCdToAcctCdMap, erpAcctCdToErpAcctNmMap);
    }

    /**
     * 批量查询 SUBJECT_EXT_INFO 表，根据 ERP_ACCT_CD 列表获取对应的所有 ACCT_CD
     * 
     * @param erpAcctCds ERP科目编码列表（对应数据库字段ERP_ACCT_CD）
     * @return Map<ERP_ACCT_CD, List<ACCT_CD>>，key为ERP_ACCT_CD，value为该ERP_ACCT_CD对应的所有ACCT_CD列表
     * @throws IllegalArgumentException 如果没有映射到数据
     */
    public Map<String, List<String>> queryErpAcctCdToAcctCdMap(Collection<String> erpAcctCds) {
        if (CollectionUtils.isEmpty(erpAcctCds)) {
            return Collections.emptyMap();
        }
        
        // 过滤空值
        Set<String> validErpAcctCds = erpAcctCds.stream()
                .filter(StringUtils::isNotBlank)
                .collect(Collectors.toSet());
        
        log.info("========== queryErpAcctCdToAcctCdMap - 输入参数: validErpAcctCds={} ==========", validErpAcctCds);
        
        if (validErpAcctCds.isEmpty()) {
            return Collections.emptyMap();
        }
        
        // 批量查询 SUBJECT_EXT_INFO 表
        List<SubjectExtInfo> subjectExtInfoList = subjectExtInfoMapper.selectList(
                new LambdaQueryWrapper<SubjectExtInfo>()
                        .in(SubjectExtInfo::getErpAcctCd, validErpAcctCds)
                        .eq(SubjectExtInfo::getDeleted, false)
        );
        
        log.info("========== queryErpAcctCdToAcctCdMap - 查询SUBJECT_EXT_INFO表，查询到 {} 条记录 ==========", subjectExtInfoList.size());
        // 记录查询到的详细信息
        for (SubjectExtInfo info : subjectExtInfoList) {
            log.info("========== queryErpAcctCdToAcctCdMap - 查询结果: erpAcctCd=[{}], acctCd=[{}], deleted=[{}] ==========", 
                    info.getErpAcctCd(), info.getAcctCd(), info.getDeleted());
        }
        
        // 转换成 Map<ERP_ACCT_CD, List<ACCT_CD>> 格式
        // 注意：ACCT_CD 为 null 或空时视为 NAN-NAN（不受控），否则扩展表存 NULL 时会被本 filter 排除，导致 isUncontrolledLedger 无法识别
        Map<String, List<String>> erpAcctCdToAcctCdMap = subjectExtInfoList.stream()
                .filter(e -> StringUtils.isNotBlank(e.getErpAcctCd()))
                .collect(Collectors.groupingBy(
                        SubjectExtInfo::getErpAcctCd,
                        Collectors.mapping(
                                e -> StringUtils.isNotBlank(e.getAcctCd()) ? e.getAcctCd() : "NAN-NAN",
                                Collectors.toList()
                        )
                ));
        
        log.info("========== queryErpAcctCdToAcctCdMap - 转换后的Map: erpAcctCdToAcctCdMap.keySet()={}, erpAcctCdToAcctCdMap.size()={} ==========", 
                erpAcctCdToAcctCdMap.keySet(), erpAcctCdToAcctCdMap.size());
        // 记录Map的详细内容
        for (Map.Entry<String, List<String>> entry : erpAcctCdToAcctCdMap.entrySet()) {
            log.info("========== queryErpAcctCdToAcctCdMap - Map内容: erpAcctCd=[{}], acctCdList={} ==========", 
                    entry.getKey(), entry.getValue());
        }
        
        // 检查是否有未映射到的ERP_ACCT_CD（仅记录日志，不抛出异常，由调用方决定如何处理）
        Set<String> unmappedErpAcctCds = new HashSet<>(validErpAcctCds);
        unmappedErpAcctCds.removeAll(erpAcctCdToAcctCdMap.keySet());
        
        log.info("========== queryErpAcctCdToAcctCdMap - 匹配检查: validErpAcctCds={}, erpAcctCdToAcctCdMap.keySet()={}, unmappedErpAcctCds={} ==========", 
                validErpAcctCds, erpAcctCdToAcctCdMap.keySet(), unmappedErpAcctCds);
        
        if (!unmappedErpAcctCds.isEmpty()) {
            log.warn("========== queryErpAcctCdToAcctCdMap - 未找到映射的ERP科目编码（返回部分映射）: {} ==========", unmappedErpAcctCds);
        }
        
        log.info("批量查询SUBJECT_EXT_INFO表，获取到 {} 个ERP_ACCT_CD对应的ACCT_CD映射", erpAcctCdToAcctCdMap.size());
        
        return erpAcctCdToAcctCdMap;
    }

    /**
     * 批量查询 PROJECT_CONTROL_EXT_R 表，根据 PRJ_CD 列表获取对应的所有 RELATED_PRJ_CD
     * 
     * @param prjCds 项目编码列表（对应数据库字段PRJ_CD）
     * @return Map<PRJ_CD, List<RELATED_PRJ_CD>>，key为PRJ_CD，value为该PRJ_CD对应的所有RELATED_PRJ_CD列表
     * @throws IllegalArgumentException 如果没有映射到数据
     */
    public Map<String, List<String>> queryPrjCdToRelatedPrjCdMap(Collection<String> prjCds) {
        if (CollectionUtils.isEmpty(prjCds)) {
            return Collections.emptyMap();
        }
        
        // 过滤空值
        Set<String> validPrjCds = prjCds.stream()
                .filter(StringUtils::isNotBlank)
                .collect(Collectors.toSet());
        
        if (validPrjCds.isEmpty()) {
            return Collections.emptyMap();
        }
        
        // 批量查询 PROJECT_CONTROL_EXT_R 表
        List<ProjectControlExtR> projectControlExtRList = projectControlExtRMapper.selectList(
                new LambdaQueryWrapper<ProjectControlExtR>()
                        .in(ProjectControlExtR::getPrjCd, validPrjCds)
                        .eq(ProjectControlExtR::getDeleted, false)
        );
        
        // 转换成 Map<PRJ_CD, List<RELATED_PRJ_CD>> 格式
        Map<String, List<String>> prjCdToRelatedPrjCdMap = projectControlExtRList.stream()
                .filter(e -> StringUtils.isNotBlank(e.getPrjCd()) && StringUtils.isNotBlank(e.getRelatedPrjCd()))
                .collect(Collectors.groupingBy(
                        ProjectControlExtR::getPrjCd,
                        Collectors.mapping(
                                ProjectControlExtR::getRelatedPrjCd,
                                Collectors.toList()
                        )
                ));
        
        // 检查是否有未映射到的PRJ_CD（仅记录日志，不抛出异常，由调用方决定如何处理）
        Set<String> unmappedPrjCds = new HashSet<>(validPrjCds);
        unmappedPrjCds.removeAll(prjCdToRelatedPrjCdMap.keySet());
        if (!unmappedPrjCds.isEmpty()) {
            log.warn("========== queryPrjCdToRelatedPrjCdMap - 未找到映射的项目编码（返回部分映射）: {} ==========", unmappedPrjCds);
        }
        
        log.info("批量查询PROJECT_CONTROL_EXT_R表，获取到 {} 个PRJ_CD对应的RELATED_PRJ_CD映射", prjCdToRelatedPrjCdMap.size());
        
        return prjCdToRelatedPrjCdMap;
    }

    /**
     * 查询 MAP_HSP_CUSTOM2 表，将 flexValue 作为 key，bgtCust2Cd 作为 value 返回 Map
     * 
     * @return Map<flexValue, bgtCust2Cd>，key为flexValue，value为bgtCust2Cd
     */
    public Map<String, String> queryFlexValueToBgtCust2CdMap() {
        // 查询所有记录
        List<MapHspCustom2> mapHspCustom2List = mapHspCustom2Mapper.selectList(null);
        
        if (CollectionUtils.isEmpty(mapHspCustom2List)) {
            log.warn("========== queryFlexValueToBgtCust2CdMap - MAP_HSP_CUSTOM2表为空 ==========");
            return Collections.emptyMap();
        }
        
        // 转换成 Map<flexValue, bgtCust2Cd> 格式
        // 过滤掉 flexValue 为空的记录，如果有重复的 flexValue，保留第一个
        Map<String, String> flexValueToBgtCust2CdMap = mapHspCustom2List.stream()
                .filter(e -> StringUtils.isNotBlank(e.getFlexValue()))
                .collect(Collectors.toMap(
                        MapHspCustom2::getFlexValue,
                        e -> StringUtils.isNotBlank(e.getBgtCust2Cd()) ? e.getBgtCust2Cd() : "",
                        (existing, replacement) -> existing // 如果有重复key，保留第一个
                ));
        
        log.info("批量查询MAP_HSP_CUSTOM2表，获取到 {} 个flexValue对应的bgtCust2Cd映射", flexValueToBgtCust2CdMap.size());
        
        return flexValueToBgtCust2CdMap;
    }
    
    /**
     * 批量查询 VIEW_BUDGET_MEMBER_NAME_CODE 视图，将 MEMBER_CD2 作为 key，MEMBER_CD 作为 value 返回 Map
     * 用于资产类型编码映射：如果 erpAssetType 以 "1" 或 "M" 开头，需要通过此视图映射
     * 
     * @param erpAssetTypes 需要映射的资产类型编码列表（对应 MEMBER_CD2）
     * @return Map<MEMBER_CD2, MEMBER_CD>，key为MEMBER_CD2（erpAssetType），value为MEMBER_CD（映射后的值）
     */
    public Map<String, String> queryErpAssetTypeToMemberCdMap(Collection<String> erpAssetTypes) {
        if (CollectionUtils.isEmpty(erpAssetTypes)) {
            log.warn("========== queryErpAssetTypeToMemberCdMap - erpAssetTypes为空 ==========");
            return Collections.emptyMap();
        }
        
        // 过滤掉空值和"NAN"
        List<String> validErpAssetTypes = erpAssetTypes.stream()
                .filter(StringUtils::isNotBlank)
                .filter(code -> !"NAN".equals(code))
                .collect(Collectors.toList());
        
        if (CollectionUtils.isEmpty(validErpAssetTypes)) {
            log.warn("========== queryErpAssetTypeToMemberCdMap - 过滤后erpAssetTypes为空 ==========");
            return Collections.emptyMap();
        }
        
        // 批量查询 VIEW_BUDGET_MEMBER_NAME_CODE 视图
        // 使用 MEMBER_CD2 作为查询条件，查询对应的 MEMBER_CD
        List<BudgetMemberNameCodeView> viewList = budgetMemberNameCodeViewMapper.selectByMemberCd2s(validErpAssetTypes);
        
        if (CollectionUtils.isEmpty(viewList)) {
            log.warn("========== queryErpAssetTypeToMemberCdMap - VIEW_BUDGET_MEMBER_NAME_CODE视图查询结果为空 ==========");
            return Collections.emptyMap();
        }
        
        // 转换成 Map<MEMBER_CD2, MEMBER_CD> 格式
        // 过滤掉 MEMBER_CD2 或 MEMBER_CD 为空的记录，如果有重复的 MEMBER_CD2，保留第一个
        Map<String, String> erpAssetTypeToMemberCdMap = viewList.stream()
                .filter(e -> StringUtils.isNotBlank(e.getMemberCd2()) && StringUtils.isNotBlank(e.getMemberCd()))
                .collect(Collectors.toMap(
                        BudgetMemberNameCodeView::getMemberCd2,
                        BudgetMemberNameCodeView::getMemberCd,
                        (existing, replacement) -> existing // 如果有重复key，保留第一个
                ));
        
        // 检查是否有未映射的 erpAssetType
        Set<String> unmappedErpAssetTypes = new HashSet<>(validErpAssetTypes);
        unmappedErpAssetTypes.removeAll(erpAssetTypeToMemberCdMap.keySet());
        if (!unmappedErpAssetTypes.isEmpty()) {
            log.warn("========== queryErpAssetTypeToMemberCdMap - 未找到映射的资产类型编码（返回部分映射）: {} ==========", unmappedErpAssetTypes);
        }
        
        log.info("批量查询VIEW_BUDGET_MEMBER_NAME_CODE视图，获取到 {} 个MEMBER_CD2对应的MEMBER_CD映射", erpAssetTypeToMemberCdMap.size());
        
        return erpAssetTypeToMemberCdMap;
    }

    /**
     * 分批查询BudgetPoolDemR，避免IN子句参数过多导致超时
     * 
     * @param dimensionParams 维度参数列表
     * @param batchSize 每批查询的参数数量（建议100，因为每个参数包含7个字段，总共约700个表达式，在Oracle的1000限制内）
     * @return 查询结果列表
     */
    public List<BudgetPoolDemR> batchSelectByDimensionsWithYearAndQuarter(
            List<BudgetPoolDemRMapper.DimensionParam> dimensionParams, int batchSize) {
        if (CollectionUtils.isEmpty(dimensionParams)) {
            return Collections.emptyList();
        }
        
        List<BudgetPoolDemR> allResults = new ArrayList<>();
        
        // 如果参数数量小于等于批次大小，直接查询
        if (dimensionParams.size() <= batchSize) {
            return budgetPoolDemRMapper.selectByDimensionsWithYearAndQuarter(dimensionParams);
        }
        
        // 分批查询
        int totalBatches = (dimensionParams.size() + batchSize - 1) / batchSize;
        log.info("========== 分批查询BudgetPoolDemR - 总参数数: {}, 批次大小: {}, 总批次数: {} ==========", 
                dimensionParams.size(), batchSize, totalBatches);
        
        for (int i = 0; i < dimensionParams.size(); i += batchSize) {
            int end = Math.min(i + batchSize, dimensionParams.size());
            // 创建新的ArrayList，避免subList的潜在问题（subList是原始列表的视图，可能在多线程环境下有问题）
            List<BudgetPoolDemRMapper.DimensionParam> batch = new ArrayList<>(dimensionParams.subList(i, end));
            
            int currentBatch = (i / batchSize) + 1;
            log.debug("========== 执行第 {}/{} 批查询，参数数量: {} ==========", currentBatch, totalBatches, batch.size());
            
            List<BudgetPoolDemR> batchResults = budgetPoolDemRMapper.selectByDimensionsWithYearAndQuarter(batch);
            if (!CollectionUtils.isEmpty(batchResults)) {
                allResults.addAll(batchResults);
            }
        }
        
        log.info("========== 分批查询完成，总共查询到 {} 条记录 ==========", allResults.size());
        return allResults;
    }

    /**
     * 分批查询BudgetPoolDemR（项目维度），避免IN子句参数过多导致超时
     * 
     * @param dimensionParams 维度参数列表
     * @param batchSize 每批查询的参数数量（建议150，因为每个参数包含4个字段，总共约600个表达式，在Oracle的1000限制内）
     * @return 查询结果列表
     */
    public List<BudgetPoolDemR> batchSelectByDimensionsWithYearAndQuarterForProject(
            List<BudgetPoolDemRMapper.DimensionParam> dimensionParams, int batchSize) {
        if (CollectionUtils.isEmpty(dimensionParams)) {
            return Collections.emptyList();
        }
        
        List<BudgetPoolDemR> allResults = new ArrayList<>();
        
        // 如果参数数量小于等于批次大小，直接查询
        if (dimensionParams.size() <= batchSize) {
            return budgetPoolDemRMapper.selectByDimensionsWithYearAndQuarterForProject(dimensionParams);
        }
        
        // 分批查询
        int totalBatches = (dimensionParams.size() + batchSize - 1) / batchSize;
        log.info("========== 分批查询BudgetPoolDemR（项目维度） - 总参数数: {}, 批次大小: {}, 总批次数: {} ==========", 
                dimensionParams.size(), batchSize, totalBatches);
        
        for (int i = 0; i < dimensionParams.size(); i += batchSize) {
            int end = Math.min(i + batchSize, dimensionParams.size());
            // 创建新的ArrayList，避免subList的潜在问题（subList是原始列表的视图，可能在多线程环境下有问题）
            List<BudgetPoolDemRMapper.DimensionParam> batch = new ArrayList<>(dimensionParams.subList(i, end));
            
            int currentBatch = (i / batchSize) + 1;
            log.debug("========== 执行第 {}/{} 批查询（项目维度），参数数量: {} ==========", currentBatch, totalBatches, batch.size());
            
            List<BudgetPoolDemR> batchResults = budgetPoolDemRMapper.selectByDimensionsWithYearAndQuarterForProject(batch);
            if (!CollectionUtils.isEmpty(batchResults)) {
                allResults.addAll(batchResults);
            }
        }
        
        log.info("========== 分批查询（项目维度）完成，总共查询到 {} 条记录 ==========", allResults.size());
        return allResults;
    }

    /**
     * 按 PROJECT_ID 回退查找 pool（项目编码变更后，按 masterProjectCode 查不到时使用）
     * 从 SYSTEM_PROJECT_BUDGET 用 project=masterProjectCode 查出 projectId，再按 (year, quarter, isInternal, projectId) 查 BUDGET_POOL_DEM_R
     */
    private List<BudgetPoolDemR> queryPoolByProjectIdFallback(
            List<BudgetPoolDemRMapper.DimensionParam> projectDimensionParams) {
        if (CollectionUtils.isEmpty(projectDimensionParams)) {
            return Collections.emptyList();
        }
        Set<String> masterProjectCodeYearKeys = new HashSet<>();
        Map<String, String> masterProjectCodeYearToProjectId = new HashMap<>();
        for (BudgetPoolDemRMapper.DimensionParam p : projectDimensionParams) {
            String key = p.getMasterProjectCode() + "@" + p.getYear();
            if (masterProjectCodeYearKeys.add(key)) {
                LambdaQueryWrapper<SystemProjectBudget> wrapper = new LambdaQueryWrapper<>();
                wrapper.eq(SystemProjectBudget::getDeleted, Boolean.FALSE)
                        .eq(SystemProjectBudget::getYear, p.getYear())
                        .eq(SystemProjectBudget::getProject, p.getMasterProjectCode());
                List<SystemProjectBudget> list = systemProjectBudgetMapper.selectList(wrapper);
                SystemProjectBudget one = CollectionUtils.isEmpty(list) ? null : list.get(0);
                if (one != null && StringUtils.isNotBlank(one.getProjectId())) {
                    masterProjectCodeYearToProjectId.put(key, one.getProjectId());
                }
            }
        }
        if (masterProjectCodeYearToProjectId.isEmpty()) {
            return Collections.emptyList();
        }
        List<BudgetPoolDemRMapper.ProjectIdDimensionParam> projectIdParams = new ArrayList<>();
        for (BudgetPoolDemRMapper.DimensionParam p : projectDimensionParams) {
            String key = p.getMasterProjectCode() + "@" + p.getYear();
            String projectId = masterProjectCodeYearToProjectId.get(key);
            if (StringUtils.isNotBlank(projectId)) {
                projectIdParams.add(new BudgetPoolDemRMapper.ProjectIdDimensionParam(
                        p.getYear(), p.getQuarter(), p.getIsInternal(), projectId));
            }
        }
        if (projectIdParams.isEmpty()) {
            return Collections.emptyList();
        }
        return budgetPoolDemRMapper.selectByProjectIdDimensions(projectIdParams);
    }
}

