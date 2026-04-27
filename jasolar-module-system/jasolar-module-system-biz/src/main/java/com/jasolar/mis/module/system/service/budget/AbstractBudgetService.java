package com.jasolar.mis.module.system.service.budget;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.incrementer.IdentifierGenerator;
import com.jasolar.mis.module.system.controller.budget.vo.BudgetParams;
import com.jasolar.mis.module.system.controller.budget.vo.BudgetRespVo;
import com.jasolar.mis.module.system.controller.budget.vo.DetailDetailVo;
import com.jasolar.mis.module.system.controller.budget.vo.DetailNumberVo;
import com.jasolar.mis.module.system.controller.budget.vo.ESBInfoParams;
import com.jasolar.mis.module.system.controller.budget.vo.ESBRespInfoVo;
import com.jasolar.mis.module.system.controller.budget.vo.ExtDetailVo;
import com.jasolar.mis.module.system.controller.budget.vo.ReqInfoParams;
import com.jasolar.mis.module.system.controller.budget.vo.ResultInfoRespVo;
import com.jasolar.mis.module.system.controller.budget.vo.SubDetailVo;
import com.jasolar.mis.module.system.domain.budget.BudgetBalance;
import com.jasolar.mis.module.system.domain.budget.BudgetBalanceHistory;
import com.jasolar.mis.module.system.domain.budget.BudgetLedger;
import com.jasolar.mis.module.system.domain.budget.BudgetLedgerCompositeKey;
import com.jasolar.mis.module.system.domain.budget.BudgetLedgerHead;
import com.jasolar.mis.module.system.domain.budget.BudgetLedgerHistory;
import com.jasolar.mis.module.system.domain.budget.BudgetLedgerSelfR;
import com.jasolar.mis.module.system.domain.budget.BudgetQuota;
import com.jasolar.mis.module.system.domain.budget.BudgetQuotaHistory;
import com.jasolar.mis.module.system.domain.budget.SystemProjectBudget;
import com.jasolar.mis.module.system.mapper.budget.BudgetBalanceHistoryMapper;
import com.jasolar.mis.module.system.mapper.budget.BudgetBalanceMapper;
import com.jasolar.mis.module.system.mapper.budget.BudgetLedgerHistoryMapper;
import com.jasolar.mis.module.system.mapper.budget.BudgetLedgerMapper;
import com.jasolar.mis.module.system.mapper.budget.BudgetLedgerHeadMapper;
import com.jasolar.mis.module.system.mapper.budget.BudgetLedgerSelfRMapper;
import com.jasolar.mis.module.system.mapper.budget.BudgetQuotaHistoryMapper;
import com.jasolar.mis.module.system.mapper.budget.BudgetPoolDemRMapper;
import com.jasolar.mis.module.system.mapper.budget.BudgetQuotaMapper;
import com.jasolar.mis.module.system.mapper.budget.SystemProjectBudgetMapper;
import com.jasolar.mis.module.system.domain.budget.BudgetPoolDemR;
import com.jasolar.mis.module.system.service.budget.exception.DetailValidationException;
import com.jasolar.mis.module.system.service.budget.helper.BudgetQueryHelperService;
import com.jasolar.mis.module.system.config.BudgetValidationConfig;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 预算服务抽象基类
 * 提供通用的预算处理方法
 *
 * @author Auto Generated
 */
@Slf4j
public abstract class AbstractBudgetService {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
    private static final DateTimeFormatter PROCESS_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final String APPLY_BIZ_TYPE = "APPLY";
    private static final String CLAIM_BIZ_TYPE = "CLAIM";
    private static final String CONTRACT_BIZ_TYPE = "CONTRACT";
    /**
     * 付款/报销实际日期来源开关：1 表示沿用 month，0 表示优先 actualMonth（若有值）
     */
    private static final int CLAIM_ACTUAL_DATE_SOURCE_FLAG = 1;

    /** 当前请求中 queryQuotaAndBalanceByAllQuartersAllDem 返回的「被跳过明细」原因，用于拼到「找不到预算余额」异常消息，用后须 remove */
    private static final ThreadLocal<List<String>> BALANCE_QUERY_SKIP_REASONS_THREAD_LOCAL = new ThreadLocal<>();

    /**
     * 付款单 / 合同单同维提交过程中：若某明细 bizKey 改动了 {@code needToUpdateSameDemBudgetBalanceMap} 内对应季度的池余额，记入集合，
     * 供 {@link #fillDeductionSourceKeys} 在「有关联流水」时仍强制落 {@code POOL_DIMENSION_KEY}（映射变更后按扣减当时的余额维度回滚）。用后须 remove。
     */
    private static final ThreadLocal<Set<String>> SUBMIT_POOL_BALANCE_TOUCHED_BIZ_KEYS = ThreadLocal.withInitial(HashSet::new);

    /** 标记本单 bizKey 已实际改动池侧 {@link BudgetBalance}（CLAIM、CONTRACT 提交生效） */
    protected void markSubmitPoolBudgetBalanceTouched(String bizKey) {
        if (StringUtils.isNotBlank(bizKey)
                && (CLAIM_BIZ_TYPE.equals(getBizType()) || CONTRACT_BIZ_TYPE.equals(getBizType()))) {
            SUBMIT_POOL_BALANCE_TOUCHED_BIZ_KEYS.get().add(bizKey);
        }
    }

    @Resource
    protected SystemProjectBudgetMapper systemProjectBudgetMapper;
    
    @Resource
    protected BudgetQueryHelperService budgetQueryHelperService;
    
    @Resource
    protected IdentifierGenerator identifierGenerator;
    
    @Resource
    protected BudgetBalanceMapper budgetBalanceMapper;
    
    @Resource
    protected BudgetQuotaMapper budgetQuotaMapper;
    
    @Resource
    protected BudgetBalanceHistoryMapper budgetBalanceHistoryMapper;
    
    @Resource
    protected BudgetQuotaHistoryMapper budgetQuotaHistoryMapper;
    
    @Resource
    protected BudgetLedgerMapper budgetLedgerMapper;
    
    @Resource
    protected BudgetLedgerHeadMapper budgetLedgerHeadMapper;

    @Resource
    protected BudgetLedgerHistoryMapper budgetLedgerHistoryMapper;
    
    @Resource
    protected BudgetLedgerSelfRMapper budgetLedgerSelfRMapper;
    
    @Resource
    protected BudgetPoolDemRMapper budgetPoolDemRMapper;
    
    @Resource
    protected com.jasolar.mis.module.system.mapper.ehr.EhrControlLevelViewMapper ehrControlLevelViewMapper;
    
    @Resource
    protected com.jasolar.mis.module.system.mapper.ehr.SubjectControlLevelViewMapper subjectControlLevelViewMapper;
    
    @Resource
    protected com.jasolar.mis.module.system.mapper.ehr.ProjectControlLevelViewMapper projectControlLevelViewMapper;
    
    @Resource
    protected com.jasolar.mis.module.system.config.BudgetSubjectCodeConfig budgetSubjectCodeConfig;

    @Resource
    protected BudgetValidationConfig budgetValidationConfig;

    /**
     * 是否跳过预算校验（由配置 budget.validation.mode=0 控制，跳过时直接扣减/占预算）
     */
    protected boolean isSkipBudgetValidation() {
        return budgetValidationConfig != null && budgetValidationConfig.isSkipBudgetValidation();
    }

    /**
     * 解析 ESB requestTime 为 LocalDateTime，用于 BUDGET_LEDGER/BUDGET_LEDGER_HEAD 的 CREATE_TIME/UPDATE_TIME。
     *
     * @param esbInfo ESB 信息，可为 null
     * @return 解析后的时间，为 null 或解析失败时返回 null
     */
    protected LocalDateTime parseRequestTime(ESBInfoParams esbInfo) {
        return BudgetQueryHelperService.parseEsbRequestTime(esbInfo != null ? esbInfo.getRequestTime() : null);
    }

    /**
     * 应用预算处理逻辑
     * 子类可以重写此方法来实现具体的业务逻辑
     */
    protected BudgetRespVo superApply(BudgetParams budgetParams, String type) throws Exception {
        String logMessage;
        switch (type) {
            case "APPLY":
                logMessage = "开始处理预算申请";
                break;
            case "CONTRACT":
                logMessage = "开始处理预算合同";
                break;
            case "CLAIM":
                logMessage = "开始处理付款/报销";
                break;
            default:
                logMessage = "开始处理预算";
                break;
        }
        log.info("{}, params={}, type={}", logMessage, budgetParams, type);
        
        ReqInfoParams reqInfo = budgetParams.getReqInfo();

        // 删除单据明细分支（DETAIL_DELETED）：短路默认提交逻辑，直接按明细维度删除并回滚
        // 仅使用 documentStatus=DETAIL_DELETED 触发
        if (reqInfo != null && "DETAIL_DELETED".equals(reqInfo.getDocumentStatus())) {
            return superDetailDeleted(budgetParams, type);
        }
        
        // 用于存储每个明细的校验结果
        Map<String, String> detailValidationResultMap = new HashMap<>();
        Map<String, String> detailValidationMessageMap = new HashMap<>();
        
        // 记录每个关联单据的扣减金额（按季度），key格式：ledgerId + "@" + relatedLedgerId + "@" + quarter，value为扣减金额
        // 这个Map会在 performMultiQuarterDeduction 中填充，然后在插入 BUDGET_LEDGER_SELF_R 时使用
        Map<String, BigDecimal> relatedLedgerDeductionAmountMap = new HashMap<>();
        
        String documentName = reqInfo.getDocumentName();
        String dataSource = reqInfo.getDataSource();
        String processName = reqInfo.getProcessName();
        String operator = reqInfo.getOperator();
        String operatorNo = reqInfo.getOperatorNo();
        List<DetailDetailVo> details = defaultList(reqInfo.getDetails());
        // ESB requestTime 用于 BUDGET_LEDGER/BUDGET_LEDGER_HEAD 的 CREATE_TIME、UPDATE_TIME
        LocalDateTime requestTime = parseRequestTime(budgetParams.getEsbInfo());
        
        // 整单级别校验（这些是业务逻辑校验，不是Bean Validation能处理的）
        // 注意：这些校验失败会导致所有明细都报同样的错
        
        // 校验明细列表不能为空（防御性编程）
        if (CollectionUtils.isEmpty(details)) {
            throw new IllegalArgumentException("明细列表不能为空");
        }
        
        // 组装 ExtDetailVo 列表
        List<ExtDetailVo> extDetailsForQuery = new ArrayList<>();
        
        // 批量提取 managementOrg 字段（对应 EHR_CD）
        Set<String> managementOrgSet = details.stream()
                .map(DetailDetailVo::getManagementOrg)
                .filter(StringUtils::isNotBlank)
                .collect(Collectors.toSet());
        
        // 批量查询 EHR_ORG_MANAGE_R 表，获取 EHR_CD 对应的 ORG_CD（一一对应关系）
        // Map<EHR_CD, ORG_CD>
        BudgetQueryHelperService.EhrCdToOrgCdMapResult ehrCdToOrgCdMapResult = budgetQueryHelperService.queryEhrCdToOrgCdOneToOneMap(managementOrgSet);
        Map<String, String> ehrCdToOrgCdMap = ehrCdToOrgCdMapResult.getEhrCdToOrgCdMap();
        // 转为可变 Map，避免 processDiffDimensionRollback 中 putAll 时因 emptyMap() 不可变而抛 UnsupportedOperationException
        ehrCdToOrgCdMap = ehrCdToOrgCdMap == null ? new HashMap<>() : new HashMap<>(ehrCdToOrgCdMap);
        Map<String, String> ehrCdToEhrNmMap = ehrCdToOrgCdMapResult.getEhrCdToEhrNmMap();
        
        // 批量查询 EHR_ORG_MANAGE_EXT_R 表，获取 EHR_CD 对应的所有 ORG_CD（一对多关系）
        // Map<EHR_CD, List<ORG_CD>>
        Map<String, List<String>> ehrCdToOrgCdExtMap = budgetQueryHelperService.queryEhrCdToOrgCdMap(managementOrgSet);
        
        // 检查是否有未映射到的EHR组织编码（业务校验：检查一对一映射和扩展映射）
        // 注意：带项目的明细（masterProjectCode 不为空且不是 "NAN"），如果组织映射没有找到，直接使用传入的 EHR 组织编码，不报错
        Set<String> unmappedEhrCds = new HashSet<>(managementOrgSet);
        unmappedEhrCds.removeAll(ehrCdToOrgCdMap.keySet()); // 先移除在一对一映射中找到的
        unmappedEhrCds.removeAll(ehrCdToOrgCdExtMap.keySet()); // 再移除在扩展映射中找到的
        if (!unmappedEhrCds.isEmpty()) {
            // 将未映射的 EHR 组织编码拆分到具体明细，按明细返回提示，避免所有明细统一报错
            for (DetailDetailVo detail : details) {
                String detailLineNo = detail.getDetailLineNo();
                if (StringUtils.isBlank(detailLineNo)) {
                    continue;
                }
                String managementOrg = detail.getManagementOrg();
                String masterProjectCode = detail.getMasterProjectCode();
                
                // 带项目的明细（masterProjectCode 不为空且不是 "NAN"），如果组织映射没有找到，直接使用传入的 EHR 组织编码，不报错
                boolean hasProjectCode = StringUtils.isNotBlank(masterProjectCode) && !"NAN".equals(masterProjectCode);
                
                if (unmappedEhrCds.contains(managementOrg)) {
                    if (hasProjectCode) {
                        // 带项目的明细，组织映射未找到时直接使用 EHR 组织编码，不报错
                        log.info("明细行号 {} 带项目编码 {}，组织编码 {} 未找到映射，将直接使用传入的EHR组织编码", 
                                detailLineNo, masterProjectCode, managementOrg);
                        detailValidationResultMap.putIfAbsent(detailLineNo, "0");
                        detailValidationMessageMap.putIfAbsent(detailLineNo, "校验通过 Verification Passed");
                    } else {
                        // 不带项目的明细，需要判断是否不受控
                        // 如果科目编码不在白名单中，则是不受控的，跳过组织映射校验（标记为成功）
                        String budgetSubjectCode = detail.getBudgetSubjectCode();
                        boolean isSubjectCodeInWhitelist = StringUtils.isNotBlank(budgetSubjectCode) 
                                && !"NAN-NAN".equals(budgetSubjectCode) 
                                && budgetSubjectCodeConfig != null 
                                && budgetSubjectCodeConfig.isInWhitelist(budgetSubjectCode);
                        
                        if (!isSubjectCodeInWhitelist) {
                            // 不受控明细：科目编码不在白名单中且不带项目，跳过组织映射校验
                            log.info("明细行号 {} 的科目编码 {} 不在白名单中且不带项目，为不受控明细，跳过组织映射校验", 
                                    detailLineNo, budgetSubjectCode);
                            detailValidationResultMap.putIfAbsent(detailLineNo, "0");
                            detailValidationMessageMap.putIfAbsent(detailLineNo, "处理成功");
                        } else {
                            // 科目在白名单中但组织映射未找到，报错（白名单科目应该有组织映射）
                            detailValidationResultMap.put(detailLineNo, "1");
                            detailValidationMessageMap.put(detailLineNo,
                                    "[" + managementOrg + "]未找到对应的预算组织，还请联系管理员增加组织映射 No EHR Org mapping available");
                        }
                    }
                } else {
                    // 其他明细正常，标记为通过，便于前端一一对应展示
                    detailValidationResultMap.putIfAbsent(detailLineNo, "0");
                    detailValidationMessageMap.putIfAbsent(detailLineNo, "校验通过 Verification Passed");
                }
            }
            // 检查是否还有需要报错的明细（不带项目的明细）
            boolean hasError = detailValidationResultMap.values().stream().anyMatch("1"::equals);
            if (hasError) {
                throw new DetailValidationException("部分明细处理失败，详见明细错误信息",
                        detailValidationResultMap, detailValidationMessageMap);
            }
        }
        
        // 批量提取 budgetSubjectCode 字段
        Set<String> budgetSubjectCodeSet = details.stream()
                .map(DetailDetailVo::getBudgetSubjectCode)
                .filter(StringUtils::isNotBlank)
                .filter(code -> !"NAN-NAN".equals(code))
                .collect(Collectors.toSet());
        
        // 批量查询 SUBJECT_INFO 表，获取 ERP_ACCT_CD 对应的 ACCT_CD（一一对应关系）
        // Map<ERP_ACCT_CD, ACCT_CD>
        BudgetQueryHelperService.ErpAcctCdToAcctCdMapResult erpAcctCdToAcctCdMapResult = budgetQueryHelperService.queryErpAcctCdToAcctCdOneToOneMap(budgetSubjectCodeSet);
        Map<String, String> erpAcctCdToAcctCdMap = erpAcctCdToAcctCdMapResult.getErpAcctCdToAcctCdMap();
        // 转为可变 Map，避免 processDiffDimensionRollback 中 putAll 时因 emptyMap() 不可变而抛 UnsupportedOperationException
        erpAcctCdToAcctCdMap = erpAcctCdToAcctCdMap == null ? new HashMap<>() : new HashMap<>(erpAcctCdToAcctCdMap);
        Map<String, String> erpAcctCdToErpAcctNmMap = erpAcctCdToAcctCdMapResult.getErpAcctCdToErpAcctNmMap();
        
        // 批量查询 SUBJECT_EXT_INFO 表，获取 ERP_ACCT_CD 对应的所有 ACCT_CD（一对多关系）
        // Map<ERP_ACCT_CD, List<ACCT_CD>>
        Map<String, List<String>> erpAcctCdToAcctCdExtMap = budgetQueryHelperService.queryErpAcctCdToAcctCdMap(budgetSubjectCodeSet);
        // 批量提取需要映射的 erpAssetType 字段（以 "1" 或 "M" 开头且不带项目的）
        Set<String> erpAssetTypeSet = details.stream()
                .filter(detail -> {
                    String masterProjectCode = detail.getMasterProjectCode();
                    // 只提取不带项目的明细的 erpAssetType
                    return "NAN".equals(masterProjectCode) || StringUtils.isBlank(masterProjectCode);
                })
                .map(DetailDetailVo::getErpAssetType)
                .filter(StringUtils::isNotBlank)
                .filter(code -> !"NAN".equals(code))
                .filter(code -> code.startsWith("1") || code.startsWith("M")) // 只提取需要映射的
                .collect(Collectors.toSet());
        
        // 校验：不带项目的明细，erpAssetType 必须是以 "1" 或 "M" 开头，或者是 "NAN" 或空
        for (DetailDetailVo detail : details) {
            String detailLineNo = detail.getDetailLineNo();
            if (StringUtils.isBlank(detailLineNo)) {
                continue;
            }
            String masterProjectCode = detail.getMasterProjectCode();
            String erpAssetType = detail.getErpAssetType();
            // 只检查不带项目的明细
            boolean isNoProject = "NAN".equals(masterProjectCode) || StringUtils.isBlank(masterProjectCode);
            if (isNoProject && StringUtils.isNotBlank(erpAssetType) && !"NAN".equals(erpAssetType)) {
                // 如果不是 "1" 或 "M" 开头，报错
                if (!erpAssetType.startsWith("1") && !erpAssetType.startsWith("M")) {
                    detailValidationResultMap.put(detailLineNo, "1");
                    detailValidationMessageMap.put(detailLineNo,
                            "erpAssetType资产类型编码 [" + erpAssetType + "] 必须以 \"1\" 或 \"M\" 开头");
                }
            }
        }
        
        // 如果有校验失败的明细，抛出异常
        if (!detailValidationResultMap.isEmpty() && detailValidationResultMap.values().stream().anyMatch("1"::equals)) {
            throw new DetailValidationException("部分明细处理失败，详见明细错误信息",
                    detailValidationResultMap, detailValidationMessageMap);
        }
        
        // 批量查询 VIEW_BUDGET_MEMBER_NAME_CODE 视图，获取 MEMBER_CD2 对应的 MEMBER_CD（一一对应关系）
        // Map<MEMBER_CD2, MEMBER_CD>，key为MEMBER_CD2（erpAssetType），value为MEMBER_CD（映射后的值）
        Map<String, String> erpAssetTypeToMemberCdMap = budgetQueryHelperService.queryErpAssetTypeToMemberCdMap(erpAssetTypeSet);
        erpAssetTypeToMemberCdMap = erpAssetTypeToMemberCdMap == null ? new HashMap<>() : new HashMap<>(erpAssetTypeToMemberCdMap);
        log.info("========== 获取到 erpAssetType 映射表，共 {} 条记录 ==========", erpAssetTypeToMemberCdMap.size());
        
        // 检查是否有未映射到的资产类型编码（业务校验）
        Set<String> unmappedErpAssetTypes = new HashSet<>(erpAssetTypeSet);
        unmappedErpAssetTypes.removeAll(erpAssetTypeToMemberCdMap.keySet());
        if (!unmappedErpAssetTypes.isEmpty()) {
            // 将未映射的资产类型编码拆分到具体明细，按明细返回提示，避免所有明细统一报错
            for (DetailDetailVo detail : details) {
                String detailLineNo = detail.getDetailLineNo();
                if (StringUtils.isBlank(detailLineNo)) {
                    continue;
                }
                String erpAssetType = detail.getErpAssetType();
                String masterProjectCode = detail.getMasterProjectCode();
                // 只检查不带项目且需要映射的 erpAssetType
                boolean needCheck = ("NAN".equals(masterProjectCode) || StringUtils.isBlank(masterProjectCode))
                        && StringUtils.isNotBlank(erpAssetType)
                        && !"NAN".equals(erpAssetType)
                        && (erpAssetType.startsWith("1") || erpAssetType.startsWith("M"));
                if (needCheck && unmappedErpAssetTypes.contains(erpAssetType)) {
                    detailValidationResultMap.put(detailLineNo, "1");
                    detailValidationMessageMap.put(detailLineNo,
                            "未找到erpAssetType资产类型编码映射 [" + erpAssetType + "]");
                } else {
                    // 其他明细正常，标记为通过，便于前端一一对应展示
                    detailValidationResultMap.putIfAbsent(detailLineNo, "0");
                    detailValidationMessageMap.putIfAbsent(detailLineNo, "校验通过 Verification Passed");
                }
            }
            throw new DetailValidationException("部分明细处理失败，详见明细错误信息",
                    detailValidationResultMap, detailValidationMessageMap);
        }
        
        // 识别白名单科目（映射结果是 NAN-NAN 的科目）
        Set<String> whitelistSubjectCodes = new HashSet<>();
        if (erpAcctCdToAcctCdExtMap != null && !erpAcctCdToAcctCdExtMap.isEmpty()) {
            for (Map.Entry<String, List<String>> entry : erpAcctCdToAcctCdExtMap.entrySet()) {
                List<String> acctCdList = entry.getValue();
                if (acctCdList != null && acctCdList.contains("NAN-NAN")) {
                    whitelistSubjectCodes.add(entry.getKey());
                    log.info("检测到科目编码 {} 为白名单科目（映射结果为 NAN-NAN），将跳过预算校验", entry.getKey());
                }
            }
        }
        
        // 将白名单科目标记为成功，并过滤掉它们，只对有映射的科目进行后续校验
        if (!whitelistSubjectCodes.isEmpty()) {
            for (DetailDetailVo detail : details) {
                String detailLineNo = detail.getDetailLineNo();
                String subjectCode = detail.getBudgetSubjectCode();
                if (StringUtils.isBlank(detailLineNo)) {
                    continue;
                }
                if (whitelistSubjectCodes.contains(subjectCode)) {
                    // 白名单科目标记为成功
                    detailValidationResultMap.put(detailLineNo, "0");
                    detailValidationMessageMap.put(detailLineNo, "处理成功");
                    log.info("明细行号 {} 的科目编码 {} 为白名单科目，已标记为成功", detailLineNo, subjectCode);
                }
            }
        }
        
        // 检查是否有未映射到的ERP科目编码（业务校验：检查一对一映射和扩展映射）
        // 注意：白名单科目已经从 erpAcctCdToAcctCdExtMap 中识别，这里只检查非白名单科目
        Set<String> unmappedErpAcctCds = new HashSet<>(budgetSubjectCodeSet);
        unmappedErpAcctCds.removeAll(whitelistSubjectCodes); // 先移除白名单科目
        unmappedErpAcctCds.removeAll(erpAcctCdToAcctCdMap.keySet()); // 再移除在一对一映射中找到的
        // 对于扩展映射，只移除那些有实际映射值（非 NAN-NAN）的科目
        if (erpAcctCdToAcctCdExtMap != null && !erpAcctCdToAcctCdExtMap.isEmpty()) {
            for (Map.Entry<String, List<String>> entry : erpAcctCdToAcctCdExtMap.entrySet()) {
                String erpAcctCd = entry.getKey();
                List<String> acctCdList = entry.getValue();
                // 如果映射列表中除了 NAN-NAN 还有其他值，说明有实际映射
                if (acctCdList != null && acctCdList.stream().anyMatch(acctCd -> !"NAN-NAN".equals(acctCd))) {
                    unmappedErpAcctCds.remove(erpAcctCd);
                }
            }
        }
        if (!unmappedErpAcctCds.isEmpty()) {
            // 将未映射的 ERP 科目编码拆分到具体明细，按明细返回提示，避免所有明细统一报错
            // 注意：带项目的明细（masterProjectCode 不为空且不是 "NAN"），如果科目映射没有找到，直接使用传入的科目编码，不报错
            for (DetailDetailVo detail : details) {
                String detailLineNo = detail.getDetailLineNo();
                String subjectCode = detail.getBudgetSubjectCode();
                String masterProjectCode = detail.getMasterProjectCode();
                
                if (StringUtils.isBlank(detailLineNo)) {
                    continue;
                }
                
                // 带项目的明细（masterProjectCode 不为空且不是 "NAN"），如果科目映射没有找到，直接使用传入的科目编码，不报错
                boolean hasProjectCode = StringUtils.isNotBlank(masterProjectCode) && !"NAN".equals(masterProjectCode);
                
                if (unmappedErpAcctCds.contains(subjectCode)) {
                    if (hasProjectCode) {
                        // 带项目的明细，科目映射未找到时直接使用传入的科目编码，不报错
                        log.info("明细行号 {} 带项目编码 {}，科目编码 {} 未找到映射，将直接使用传入的科目编码", 
                                detailLineNo, masterProjectCode, subjectCode);
                        detailValidationResultMap.putIfAbsent(detailLineNo, "0");
                        detailValidationMessageMap.putIfAbsent(detailLineNo, "校验通过 Verification Passed");
                    } else {
                        // 不带项目的明细，需要判断是否不受控
                        // 如果科目编码不在白名单中，则是不受控的，跳过校验（标记为成功）
                        boolean isSubjectCodeInWhitelist = StringUtils.isNotBlank(subjectCode) 
                                && !"NAN-NAN".equals(subjectCode) 
                                && budgetSubjectCodeConfig != null 
                                && budgetSubjectCodeConfig.isInWhitelist(subjectCode);
                        
                        if (!isSubjectCodeInWhitelist) {
                            // 不受控明细：科目编码不在白名单中且不带项目，跳过科目映射校验
                            log.info("明细行号 {} 的科目编码 {} 不在白名单中且不带项目，为不受控明细，跳过科目映射校验", 
                                    detailLineNo, subjectCode);
                            detailValidationResultMap.putIfAbsent(detailLineNo, "0");
                            detailValidationMessageMap.putIfAbsent(detailLineNo, "处理成功");
                        } else {
                            // 科目在白名单中但未找到映射，报错（白名单科目应该有映射）
                            detailValidationResultMap.put(detailLineNo, "1");
                            detailValidationMessageMap.put(detailLineNo,
                                    "[" + subjectCode + "]未找到对应预算科目映射,还请联系管理员增加科目映射 No Account mapping available");
                        }
                    }
                } else {
                    // 其他明细正常，标记为通过，便于前端一一对应展示
                    detailValidationResultMap.putIfAbsent(detailLineNo, "0");
                    detailValidationMessageMap.putIfAbsent(detailLineNo, "校验通过 Verification Passed");
                }
            }
            // 检查是否还有需要报错的明细（不带项目的明细）
            boolean hasError = detailValidationResultMap.values().stream().anyMatch("1"::equals);
            if (hasError) {
                throw new DetailValidationException("部分明细处理失败，详见明细错误信息",
                        detailValidationResultMap, detailValidationMessageMap);
            }
        }
        
        // 批量提取 masterProjectCode 字段
        Set<String> masterProjectCodeSet = details.stream()
                .filter(detail -> !shouldSkipProjectMappingValidation(detail))
                .map(DetailDetailVo::getMasterProjectCode)
                .filter(StringUtils::isNotBlank)
                .filter(code -> !"NAN".equals(code))
                .collect(Collectors.toSet());
        
        // 批量查询 PROJECT_CONTROL_EXT_R 表，获取 PRJ_CD 对应的所有 RELATED_PRJ_CD（一对多关系）
        // Map<PRJ_CD, List<RELATED_PRJ_CD>>
        Map<String, List<String>> prjCdToRelatedPrjCdExtMap = budgetQueryHelperService.queryPrjCdToRelatedPrjCdMap(masterProjectCodeSet);
        
        // 检查是否有未映射到的项目编码（业务校验：将未映射的项目编码拆分到具体明细，按明细返回提示，避免所有明细统一报错）
        // 带有效项目编码的明细必须能通过项目关联映射，与白名单科目无关
        Set<String> unmappedPrjCds = new HashSet<>(masterProjectCodeSet);
        unmappedPrjCds.removeAll(prjCdToRelatedPrjCdExtMap.keySet());
        if (!unmappedPrjCds.isEmpty()) {
            Set<String> normalizedUnmappedPrjCds = unmappedPrjCds.stream()
                    .filter(StringUtils::isNotBlank)
                    .map(StringUtils::trim)
                    .collect(Collectors.toSet());
            boolean hasMappedProjectErrorToDetail = false;
            // 将未映射的项目编码拆分到具体明细，按明细返回提示，避免所有明细统一报错
            for (DetailDetailVo detail : details) {
                String detailLineNo = detail.getDetailLineNo();
                String masterProjectCode = detail.getMasterProjectCode();
                String normalizedMasterProjectCode = StringUtils.trim(masterProjectCode);
                if (StringUtils.isBlank(detailLineNo)) {
                    continue;
                }
                if (shouldSkipProjectMappingValidation(detail)) {
                    detailValidationResultMap.putIfAbsent(detailLineNo, "0");
                    detailValidationMessageMap.putIfAbsent(detailLineNo, "校验通过 Verification Passed");
                    continue;
                }
                // 只检查有效的项目编码（非空且不是 "NAN"）
                if (StringUtils.isNotBlank(normalizedMasterProjectCode) && !"NAN".equals(normalizedMasterProjectCode)
                        && normalizedUnmappedPrjCds.contains(normalizedMasterProjectCode)) {
                    detailValidationResultMap.put(detailLineNo, "1");
                    detailValidationMessageMap.put(detailLineNo,
                            "[" + normalizedMasterProjectCode + "]未找到对应的关联项目,还请联系管理员增加项目映射 Invalid Project Code");
                    hasMappedProjectErrorToDetail = true;
                } else {
                    // 其他明细正常，标记为通过，便于前端一一对应展示
                    detailValidationResultMap.putIfAbsent(detailLineNo, "0");
                    detailValidationMessageMap.putIfAbsent(detailLineNo, "校验通过 Verification Passed");
                }
            }
            // 兜底：只要存在未映射项目编码，必须至少有一条明细标记为失败
            if (!hasMappedProjectErrorToDetail && !details.isEmpty()) {
                DetailDetailVo fallbackDetail = details.get(0);
                if (StringUtils.isNotBlank(fallbackDetail.getDetailLineNo())) {
                    detailValidationResultMap.put(fallbackDetail.getDetailLineNo(), "1");
                    detailValidationMessageMap.put(fallbackDetail.getDetailLineNo(),
                            "存在未映射项目编码: " + String.join(",", normalizedUnmappedPrjCds));
                }
            }
            throw new DetailValidationException("部分明细处理失败，详见明细错误信息",
                    detailValidationResultMap, detailValidationMessageMap);
        }
        
        // 组装 ExtDetailVo 列表并设置 year
        // 注意：包含所有明细（含白名单科目）。白名单科目也需生成流水明细记录，仅跳过预算校验/余额扣减（在 processSameDimensionUpdate 中通过 isUncontrolledLedger 识别并跳过）
        String year = null;
        for (DetailDetailVo detail : details) {
            ExtDetailVo extDetail = new ExtDetailVo();
            BeanUtils.copyProperties(detail, extDetail);
            extDetail.setDocumentNo(reqInfo.getDocumentNo());
            extDetailsForQuery.add(extDetail);
            if (year == null && detail.getYear() != null) {
                year = detail.getYear();
            }
        }
        
        // 查询 SYSTEM_PROJECT_BUDGET（整个方法只需要调用一次）
        // 查询预算流水
        List<BudgetLedger> existingLedgers = queryExistingLedgers(extDetailsForQuery, type);
        log.info("========== 查询到 {} 条已存在的 BudgetLedger ==========", existingLedgers.size());
        Map<String, BudgetLedger> existingBudgetLedgerMap = existingLedgers.stream()
                .collect(Collectors.toMap(
                        ledger -> ledger.getBizCode() + "@" + ledger.getBizItemCode(),
                        Function.identity(),
                        (a, b) -> a
                ));
        for (BudgetLedger ledger : existingLedgers) {
            log.info("========== 已存在的 BudgetLedger: bizCode={}, bizItemCode={}, consumedAmount={}, version={} ==========",
                    ledger.getBizCode(), ledger.getBizItemCode(), getConsumedAmountByMonth(ledger), ledger.getVersion());
        }
        
        // 补充已有流水的 morgCode 到 EHR 组织映射（一对一 + 扩展），便于：1）维度比较时将「同一预算组织」的不同 EHR 编码（如 015-044-005 与 015-044-005-001）视为一致；2）后续 processSameDimensionUpdate 查询 quota/balance 时扩展映射表能解析流水上的 morgCode
        Set<String> existingLedgerMorgCodes = existingLedgers.stream()
                .map(BudgetLedger::getMorgCode)
                .filter(StringUtils::isNotBlank)
                .collect(Collectors.toSet());
        Set<String> missingMorgCodesForMap = new HashSet<>(existingLedgerMorgCodes);
        missingMorgCodesForMap.removeAll(ehrCdToOrgCdMap.keySet());
        if (!missingMorgCodesForMap.isEmpty()) {
            BudgetQueryHelperService.EhrCdToOrgCdMapResult existingMorgMapResult = budgetQueryHelperService.queryEhrCdToOrgCdOneToOneMap(missingMorgCodesForMap);
            if (existingMorgMapResult != null && existingMorgMapResult.getEhrCdToOrgCdMap() != null && !existingMorgMapResult.getEhrCdToOrgCdMap().isEmpty()) {
                ehrCdToOrgCdMap.putAll(existingMorgMapResult.getEhrCdToOrgCdMap());
                log.info("========== 已补充已有流水 morgCode 的 EHR 组织映射，本次补充 {} 条，用于维度一致判断 ==========", existingMorgMapResult.getEhrCdToOrgCdMap().size());
                // 扩展映射表可能没有该 EHR 编码（仅一对一表有），用一对一结果补全扩展映射，便于后续 quota/balance 查询能解析流水上的 morgCode
                for (Map.Entry<String, String> e : existingMorgMapResult.getEhrCdToOrgCdMap().entrySet()) {
                    if (!ehrCdToOrgCdExtMap.containsKey(e.getKey())) {
                        ehrCdToOrgCdExtMap.put(e.getKey(), Collections.singletonList(e.getValue()));
                    }
                }
            }
            Map<String, List<String>> existingExtMap = budgetQueryHelperService.queryEhrCdToOrgCdMap(missingMorgCodesForMap);
            if (existingExtMap != null && !existingExtMap.isEmpty()) {
                ehrCdToOrgCdExtMap.putAll(existingExtMap);
                log.info("========== 已补充已有流水 morgCode 的 EHR 组织扩展映射，本次补充 {} 条，用于 quota/balance 查询 ==========", existingExtMap.size());
            }
        }

        // 再次提交（CLAIM/CONTRACT）：先把“上一版本”对资金池与上游(APPLY/CONTRACT)的占用完整回滚释放，
        // 再走后续的同维更新/重新扣减逻辑，避免历史 SELF_R 关系残留导致申请单/合同余额越扣越少。
        //
        // 触发条件：
        // - 当前是 CLAIM 或 CONTRACT 业务（由子类 getBizType() 决定）
        // - 本次是“提交/变更提交”（INITIAL_SUBMITTED / APPROVED_UPDATE）
        // - 数据库已存在本单流水（existingLedgers 非空）
        Set<String> retainedLedgerBizKeys = extDetailsForQuery.stream()
                .map(this::getBizKey)
                .filter(StringUtils::isNotBlank)
                .collect(Collectors.toSet());
        Set<String> resubmitRolledBackLedgerKeys = handleResubmitRollbackIfNeeded(reqInfo, type, existingLedgers, existingBudgetLedgerMap,
                retainedLedgerBizKeys, operator, operatorNo,
                ehrCdToOrgCdMap, erpAcctCdToAcctCdMap, erpAssetTypeToMemberCdMap,
                ehrCdToOrgCdExtMap, erpAcctCdToAcctCdExtMap, requestTime);
        
        // 分类处理
        List<ExtDetailVo> needToAddBudgetLedgerList = new ArrayList<>();
        Map<String, BudgetLedger> needRollbackDiffDemBudgetLedgerMap = new HashMap<>();
        Map<String, String> recoverMonthSameDemBudgetLedgerMap = new HashMap<>();
        Map<String, String> recoverActualYearSameDemBudgetLedgerMap = new HashMap<>();
        Map<String, String> recoverActualMonthSameDemBudgetLedgerMap = new HashMap<>();
        Map<String, BigDecimal> recoverAmountSameDemBudgetLedgerMap = new HashMap<>();
        Map<String, BudgetLedger> needUpdateSameDemBudgetLedgerMap = new HashMap<>();
        // 请求 key（getBizKey(extDetail)）-> 流水 key（bizCode@bizItemCode），用于合并关联流水时按流水 key 再存一份，便于 processSameDimensionUpdate 统一校验用流水 key 能查到
        Map<String, String> requestKeyToLedgerKeyForRelated = new HashMap<>();
        // 存储需要新增的ledger（维度不一致时创建的新记录，如果生成了新ID）
        Map<String, BudgetLedger> needToAddBudgetLedgerMap = new HashMap<>();
        // 存储需要更新的ledger对应的metadata，key为ledgerKey（bizCode + "@" + bizItemCode）
        Map<String, String> recoverMetadataSameDemBudgetLedgerMap = new HashMap<>();
        Map<String, String> recoverEffectTypeSameDemBudgetLedgerMap = new HashMap<>();
        
        // 标记是否有明细处理失败
        boolean hasDetailError = false;

        for (ExtDetailVo extDetail : extDetailsForQuery) {
            String key = getBizKey(extDetail);
            String detailLineNo = extDetail.getDetailLineNo();
            
            try {
                BudgetLedger existingLedger = existingBudgetLedgerMap.get(key);
                
                // 如果通过key没有找到，可能是维度变化了，需要遍历同一bizCode下的所有记录进行维度匹配
                if (existingLedger == null) {
                    // 遍历所有查询到的existingLedgers，查找同一bizCode下是否有维度匹配的记录
                    BudgetLedger matchedLedger = null;
                    for (BudgetLedger ledger : existingLedgers) {
                        // 只检查同一bizCode下的记录
                        if (Objects.equals(ledger.getBizCode(), extDetail.getDocumentNo())) {
                            // 检查维度是否匹配（通过 isDimensionSame 判断；传入 ehrCdToOrgCdMap 时按预算组织比较，同一预算组织的不同 EHR 编码如 015-044-005/015-044-005-001 视为一致）
                            if (isDimensionSame(extDetail, ledger, ehrCdToOrgCdMap)) {
                                matchedLedger = ledger;
                                break;
                            }
                        }
                    }
                    
                    if (matchedLedger != null) {
                        // 找到了维度匹配的记录，按照维度一致处理
                        String ledgerKey = matchedLedger.getBizCode() + "@" + matchedLedger.getBizItemCode();
                        needUpdateSameDemBudgetLedgerMap.put(ledgerKey, matchedLedger);
                        requestKeyToLedgerKeyForRelated.put(key, ledgerKey);
                        recoverMonthSameDemBudgetLedgerMap.put(key, extDetail.getMonth());
                        recoverAmountSameDemBudgetLedgerMap.put(key, extDetail.getAmount());
                        recoverActualYearSameDemBudgetLedgerMap.put(key, extDetail.getActualYear());
                        recoverActualMonthSameDemBudgetLedgerMap.put(key, extDetail.getActualMonth());
                        // 存储metadata，如果传了就用传入的，如果没传就保留数据库原有的（在更新时处理）
                        if (extDetail.getMetadata() != null) {
                            recoverMetadataSameDemBudgetLedgerMap.put(ledgerKey, extDetail.getMetadata());
                        }
                        if (StringUtils.isNotBlank(extDetail.getEffectType())) {
                            recoverEffectTypeSameDemBudgetLedgerMap.put(ledgerKey, extDetail.getEffectType());
                        }
                        detailValidationResultMap.put(detailLineNo, "0");
                        detailValidationMessageMap.put(detailLineNo, "校验通过 Verification Passed");
                        log.info("========== 维度一致，需要更新（通过维度匹配找到）: bizItemCode={}, 旧amount={}, 旧amountConsumedQOne={}, 旧amountConsumedQTwo={}, 旧amountConsumedQThree={}, 旧amountConsumedQFour={}, 新amount={}, 旧month={}, 新month={} ==========",
                                extDetail.getDetailLineNo(),
                                matchedLedger.getAmount() == null ? BigDecimal.ZERO : matchedLedger.getAmount(),
                                matchedLedger.getAmountConsumedQOne() == null ? BigDecimal.ZERO : matchedLedger.getAmountConsumedQOne(),
                                matchedLedger.getAmountConsumedQTwo() == null ? BigDecimal.ZERO : matchedLedger.getAmountConsumedQTwo(),
                                matchedLedger.getAmountConsumedQThree() == null ? BigDecimal.ZERO : matchedLedger.getAmountConsumedQThree(),
                                matchedLedger.getAmountConsumedQFour() == null ? BigDecimal.ZERO : matchedLedger.getAmountConsumedQFour(),
                                extDetail.getAmount(),
                                matchedLedger.getMonth(), extDetail.getMonth());
                    } else {
                        // 维度不一致时，通过行号匹配同一业务行的旧记录并回滚
                        // 因为行号代表业务上的同一行，维度变化（包括预算科目变化）时都应该回滚旧记录
                        // 注意：同一行号可能有多条旧记录（不同预算科目），需要全部回滚
                        List<BudgetLedger> rollbackLedgers = new ArrayList<>();
                        
                        for (BudgetLedger ledger : existingLedgers) {
                            // 只检查同一bizCode下的记录
                            if (Objects.equals(ledger.getBizCode(), extDetail.getDocumentNo())) {
                                // 提取行号部分（第一个@之前的部分）
                                String detailLineNoPrefix = extDetail.getDetailLineNo();
                                String ledgerBizItemCodePrefix = ledger.getBizItemCode();
                                String detailLineNoRowNo = detailLineNoPrefix.contains("@") ? detailLineNoPrefix.substring(0, detailLineNoPrefix.indexOf("@")) : detailLineNoPrefix;
                                String ledgerRowNo = ledgerBizItemCodePrefix.contains("@") ? ledgerBizItemCodePrefix.substring(0, ledgerBizItemCodePrefix.indexOf("@")) : ledgerBizItemCodePrefix;
                                
                                // 如果行号相同，说明是同一业务行，应该回滚（维度不一致可能是任何维度变了，包括预算科目）
                                if (Objects.equals(detailLineNoRowNo, ledgerRowNo)) {
                                    rollbackLedgers.add(ledger);
                                }
                            }
                        }
                        
                        if (!rollbackLedgers.isEmpty()) {
                            // 同一业务行号（首段）下无论 1 条还是多条旧流水（如一单多科目），改维度/科目后全部回滚并删除旧流水
                            String newSubjectCode = extractBudgetSubjectCode(extDetail.getDetailLineNo());
                            for (BudgetLedger rollbackLedger : rollbackLedgers) {
                                String ledgerKey = rollbackLedger.getBizCode() + "@" + rollbackLedger.getBizItemCode();
                                needRollbackDiffDemBudgetLedgerMap.put(ledgerKey, rollbackLedger);
                            }
                            log.info("========== 维度不一致，需要回滚（行号匹配）: 新明细行号={}, 新budgetSubjectCode={}, 同行号旧流水条数={}, 旧bizItemCodes={}, 新amount={} ==========",
                                    extDetail.getDetailLineNo(),
                                    newSubjectCode,
                                    rollbackLedgers.size(),
                                    rollbackLedgers.stream().map(BudgetLedger::getBizItemCode).collect(Collectors.toList()),
                                    extDetail.getAmount());
                            detailValidationResultMap.put(detailLineNo, "0");
                            detailValidationMessageMap.put(detailLineNo, "校验通过 Verification Passed");
                        } else {
                            // 没有找到匹配的记录，当作新增处理
                            needToAddBudgetLedgerList.add(extDetail);
                            detailValidationResultMap.put(detailLineNo, "0");
                            detailValidationMessageMap.put(detailLineNo, "校验通过 Verification Passed");
                            log.info("========== 新增 BudgetLedger: bizItemCode={}, amount={} ==========",
                                    extDetail.getDetailLineNo(), extDetail.getAmount());
                        }
                    }
                } else {
                    // 对比维度（传入 ehrCdToOrgCdMap，按预算组织比较）
                    boolean isSame = isDimensionSame(extDetail, existingLedger, ehrCdToOrgCdMap);
                    if (!isSame) {
                        String ledgerKey = existingLedger.getBizCode() + "@" + existingLedger.getBizItemCode();
                        needRollbackDiffDemBudgetLedgerMap.put(ledgerKey, existingLedger);
                        detailValidationResultMap.put(detailLineNo, "0");
                        detailValidationMessageMap.put(detailLineNo, "校验通过 Verification Passed");
                        log.info("========== 维度不一致，需要回滚: bizItemCode={}, 旧amount={}, 旧amountConsumedQOne={}, 旧amountConsumedQTwo={}, 旧amountConsumedQThree={}, 旧amountConsumedQFour={}, 新amount={} ==========",
                                extDetail.getDetailLineNo(), 
                                existingLedger.getAmount() == null ? BigDecimal.ZERO : existingLedger.getAmount(),
                                existingLedger.getAmountConsumedQOne() == null ? BigDecimal.ZERO : existingLedger.getAmountConsumedQOne(),
                                existingLedger.getAmountConsumedQTwo() == null ? BigDecimal.ZERO : existingLedger.getAmountConsumedQTwo(),
                                existingLedger.getAmountConsumedQThree() == null ? BigDecimal.ZERO : existingLedger.getAmountConsumedQThree(),
                                existingLedger.getAmountConsumedQFour() == null ? BigDecimal.ZERO : existingLedger.getAmountConsumedQFour(),
                                extDetail.getAmount());
                    } else {
                        String ledgerKey = existingLedger.getBizCode() + "@" + existingLedger.getBizItemCode();
                        needUpdateSameDemBudgetLedgerMap.put(ledgerKey, existingLedger);
                        requestKeyToLedgerKeyForRelated.put(key, ledgerKey);
                        recoverMonthSameDemBudgetLedgerMap.put(key, extDetail.getMonth());
                        recoverAmountSameDemBudgetLedgerMap.put(key, extDetail.getAmount());
                        recoverActualYearSameDemBudgetLedgerMap.put(key, extDetail.getActualYear());
                        recoverActualMonthSameDemBudgetLedgerMap.put(key, extDetail.getActualMonth());
                        // 存储metadata，如果传了就用传入的，如果没传就保留数据库原有的（在更新时处理）
                        if (extDetail.getMetadata() != null) {
                            recoverMetadataSameDemBudgetLedgerMap.put(ledgerKey, extDetail.getMetadata());
                        }
                        if (StringUtils.isNotBlank(extDetail.getEffectType())) {
                            recoverEffectTypeSameDemBudgetLedgerMap.put(ledgerKey, extDetail.getEffectType());
                        }
                        detailValidationResultMap.put(detailLineNo, "0");
                        detailValidationMessageMap.put(detailLineNo, "校验通过 Verification Passed");
                    log.info("========== 维度一致，需要更新: bizItemCode={}, 旧amount={}, 旧amountConsumedQOne={}, 旧amountConsumedQTwo={}, 旧amountConsumedQThree={}, 旧amountConsumedQFour={}, 新amount={}, 旧month={}, 新month={} ==========",
                            extDetail.getDetailLineNo(),
                            existingLedger.getAmount() == null ? BigDecimal.ZERO : existingLedger.getAmount(),
                            existingLedger.getAmountConsumedQOne() == null ? BigDecimal.ZERO : existingLedger.getAmountConsumedQOne(),
                            existingLedger.getAmountConsumedQTwo() == null ? BigDecimal.ZERO : existingLedger.getAmountConsumedQTwo(),
                            existingLedger.getAmountConsumedQThree() == null ? BigDecimal.ZERO : existingLedger.getAmountConsumedQThree(),
                            existingLedger.getAmountConsumedQFour() == null ? BigDecimal.ZERO : existingLedger.getAmountConsumedQFour(),
                            extDetail.getAmount(),
                            existingLedger.getMonth(), extDetail.getMonth());
                    }
                }
            } catch (Exception e) {
                // 明细级别的错误，只标记当前明细失败
                log.error("明细处理失败，明细行号: {}", detailLineNo, e);
                detailValidationResultMap.put(detailLineNo, "1");
                detailValidationMessageMap.put(detailLineNo, e.getMessage() != null ? e.getMessage() : "明细处理失败");
                hasDetailError = true;
            }
        }
        
        // 如果有明细级别的错误，抛出异常回滚整个事务
        if (hasDetailError) {
            throw new DetailValidationException("部分明细处理失败，详见明细错误信息", 
                    detailValidationResultMap, detailValidationMessageMap);
        }
        
        log.info("========== 分类统计: 新增={}, 维度不一致回滚={}, 维度一致更新={} ==========",
                needToAddBudgetLedgerList.size(), needRollbackDiffDemBudgetLedgerMap.size(), needUpdateSameDemBudgetLedgerMap.size());

        // 补充需要回滚/更新的旧流水资产类型映射，避免只按当前入参明细收集时漏掉历史流水的 erpAssetType
        Set<String> existingLedgerErpAssetTypeSet = new HashSet<>();
        existingLedgerErpAssetTypeSet.addAll(needRollbackDiffDemBudgetLedgerMap.values().stream()
                .map(BudgetLedger::getErpAssetType)
                .filter(StringUtils::isNotBlank)
                .filter(code -> !"NAN".equals(code))
                .filter(code -> code.startsWith("1") || code.startsWith("M"))
                .collect(Collectors.toSet()));
        existingLedgerErpAssetTypeSet.addAll(needUpdateSameDemBudgetLedgerMap.values().stream()
                .map(BudgetLedger::getErpAssetType)
                .filter(StringUtils::isNotBlank)
                .filter(code -> !"NAN".equals(code))
                .filter(code -> code.startsWith("1") || code.startsWith("M"))
                .collect(Collectors.toSet()));
        if (!existingLedgerErpAssetTypeSet.isEmpty()) {
            Set<String> missingExistingLedgerErpAssetTypes = new HashSet<>(existingLedgerErpAssetTypeSet);
            missingExistingLedgerErpAssetTypes.removeAll(erpAssetTypeToMemberCdMap.keySet());
            if (!missingExistingLedgerErpAssetTypes.isEmpty()) {
                Map<String, String> existingLedgerErpAssetTypeToMemberCdMap =
                        budgetQueryHelperService.queryErpAssetTypeToMemberCdMap(missingExistingLedgerErpAssetTypes);
                if (existingLedgerErpAssetTypeToMemberCdMap != null && !existingLedgerErpAssetTypeToMemberCdMap.isEmpty()) {
                    erpAssetTypeToMemberCdMap.putAll(existingLedgerErpAssetTypeToMemberCdMap);
                    log.info("========== 已补充旧流水 erpAssetType 映射，本次补充 {} 条 ==========",
                            existingLedgerErpAssetTypeToMemberCdMap.size());
                }
            }
        }
        
        // 处理维度不一致的数据（回滚逻辑）
        Map<String, BudgetQuota> needToUpdateDiffDemBudgetQuotaMap = new HashMap<>();
        Map<String, BudgetBalance> needToUpdateDiffDemBudgetBalanceMap = new HashMap<>();
        List<BudgetQuotaHistory> needToAddBudgetQuotaHistory = new ArrayList<>();
        List<BudgetBalanceHistory> needToAddBudgetBalanceHistory = new ArrayList<>();

        if (!needRollbackDiffDemBudgetLedgerMap.isEmpty()) {
            try {
                processDiffDimensionRollback(needRollbackDiffDemBudgetLedgerMap, needToUpdateDiffDemBudgetQuotaMap,
                        needToUpdateDiffDemBudgetBalanceMap, needToAddBudgetQuotaHistory, needToAddBudgetBalanceHistory, ehrCdToOrgCdMap, erpAcctCdToAcctCdMap, erpAssetTypeToMemberCdMap, ehrCdToOrgCdExtMap, erpAcctCdToAcctCdExtMap);
            } catch (Exception e) {
                // 尝试提取明细标识
                if (!tryExtractDetailError(e, detailValidationResultMap, detailValidationMessageMap)) {
                    throw e; // 无法识别具体明细，作为整单错误抛出
                }
                hasDetailError = true;
            }
        }
        
        // 处理维度一致的数据（回滚逻辑）
        Map<String, BudgetQuota> needToUpdateSameDemBudgetQuotaMap = new HashMap<>();
        Map<String, BudgetBalance> needToUpdateSameDemBudgetBalanceMap = new HashMap<>();
        Map<String, List<BudgetLedger>> updatedRelatedBudgetLedgerMap = new HashMap<>();

        if (!needUpdateSameDemBudgetLedgerMap.isEmpty()) {
            try {
                Set<String> ledgerKeysAlreadyRolledBack = new HashSet<>();
                if (!needRollbackDiffDemBudgetLedgerMap.isEmpty()) {
                    ledgerKeysAlreadyRolledBack.addAll(needRollbackDiffDemBudgetLedgerMap.keySet());
                }
                if (!CollectionUtils.isEmpty(resubmitRolledBackLedgerKeys)) {
                    ledgerKeysAlreadyRolledBack.addAll(resubmitRolledBackLedgerKeys);
                }
                if (ledgerKeysAlreadyRolledBack.isEmpty()) {
                    ledgerKeysAlreadyRolledBack = null;
                }
                updatedRelatedBudgetLedgerMap = processSameDimensionRollback(needUpdateSameDemBudgetLedgerMap, needToUpdateSameDemBudgetQuotaMap,
                        needToUpdateSameDemBudgetBalanceMap, needToAddBudgetQuotaHistory, needToAddBudgetBalanceHistory, ehrCdToOrgCdMap, erpAcctCdToAcctCdMap, erpAssetTypeToMemberCdMap, ehrCdToOrgCdExtMap, erpAcctCdToAcctCdExtMap, ledgerKeysAlreadyRolledBack);
            } catch (Exception e) {
                // 尝试提取明细标识
                if (!tryExtractDetailError(e, detailValidationResultMap, detailValidationMessageMap)) {
                    throw e; // 无法识别具体明细，作为整单错误抛出
                }
                hasDetailError = true;
            }
        }
        
        // 更新 BudgetLedger 的 amount、amountAvailable、month、version 和 metadata
        log.info("========== 开始更新 BudgetLedger 的 amount、amountAvailable、month、version 和 metadata，共 {} 条 ==========", needUpdateSameDemBudgetLedgerMap.size());
        for (Map.Entry<String, BudgetLedger> entry : needUpdateSameDemBudgetLedgerMap.entrySet()) {
            BudgetLedger ledger = entry.getValue();
            // 设置操作人字段
            ledger.setOperator(operator);
            ledger.setOperatorNo(operatorNo);
            String key = ledger.getBizCode() + "@" + ledger.getBizItemCode();
            BigDecimal oldAmount = ledger.getAmount() == null ? BigDecimal.ZERO : ledger.getAmount();
            BigDecimal oldAmountAvailable = ledger.getAmountAvailable() == null ? BigDecimal.ZERO : ledger.getAmountAvailable();
            String oldMonth = ledger.getMonth();
            String oldVersion = ledger.getVersion();
            BigDecimal newAmount = recoverAmountSameDemBudgetLedgerMap.get(key);
            String newMonth = recoverMonthSameDemBudgetLedgerMap.get(key);
            if (newAmount != null) {
                budgetQueryHelperService.updateBudgetLedgerAmountAndVersion(ledger, newAmount, newMonth, identifierGenerator);
                // 处理 metadata 字段：如果传了metadata就使用传入的，如果没传就保留数据库原有的
                String newMetadata = recoverMetadataSameDemBudgetLedgerMap.get(key);
                if (newMetadata != null) {
                    ledger.setMetadata(newMetadata);
                }
                if (recoverEffectTypeSameDemBudgetLedgerMap.containsKey(key)) {
                    ledger.setEffectType(recoverEffectTypeSameDemBudgetLedgerMap.get(key));
                }
                // 如果没传metadata，ledger.getMetadata()保持数据库原有的值，不需要修改
                log.info("========== 更新 BudgetLedger: bizKey={}, bizItemCode={}, 旧amount={}, 新amount={}, 旧amountAvailable={}, 新amountAvailable={}, 旧month={}, 新month={}, 旧version={}, 新version={}, metadata={} ==========",
                        entry.getKey(), ledger.getBizItemCode(), oldAmount, newAmount, oldAmountAvailable, newAmount, oldMonth, newMonth, oldVersion, ledger.getVersion(),
                        newMetadata != null ? "已更新" : "保留原值");
            }
            if (recoverActualYearSameDemBudgetLedgerMap.containsKey(key)) {
                ledger.setActualYear(recoverActualYearSameDemBudgetLedgerMap.get(key));
            }
            if (recoverActualMonthSameDemBudgetLedgerMap.containsKey(key)) {
                ledger.setActualMonth(recoverActualMonthSameDemBudgetLedgerMap.get(key));
            }
        }
        
        // 处理维度不一致时创建新的 BudgetLedger
        // 维度不一致时，通过行号匹配同一业务行的旧记录，然后创建新记录（复用旧记录的ID）
        Map<String, BudgetLedger> diffDemNewBudgetLedgerWithOldDataMap = new HashMap<>();
        Map<String, List<BudgetLedger>> diffDemRowNoToOldLedgersMap = new HashMap<>();
        for (BudgetLedger ledger : needRollbackDiffDemBudgetLedgerMap.values()) {
            String bizItemCode = ledger.getBizItemCode();
            // 提取行号部分（第一个@之前的部分）
            String rowNo = bizItemCode.contains("@") ? bizItemCode.substring(0, bizItemCode.indexOf("@")) : bizItemCode;
            diffDemNewBudgetLedgerWithOldDataMap.put(bizItemCode, ledger);
            diffDemRowNoToOldLedgersMap.computeIfAbsent(rowNo, k -> new ArrayList<>()).add(ledger);
        }
        Set<String> diffDemRowNoSet = new HashSet<>(diffDemRowNoToOldLedgersMap.keySet());
        List<ExtDetailVo> extDetailsIncludeExtNewDemForQueryBoth = new ArrayList<>();
        for (ExtDetailVo extDetail : extDetailsForQuery) {
            String detailLineNo = extDetail.getDetailLineNo();
            // 提取行号部分（第一个@之前的部分）
            String rowNo = detailLineNo.contains("@") ? detailLineNo.substring(0, detailLineNo.indexOf("@")) : detailLineNo;
            if (diffDemRowNoSet.contains(rowNo)) {
                extDetailsIncludeExtNewDemForQueryBoth.add(extDetail);
            }
        }

        // 收集需要删除的旧BudgetLedger ID（维度不一致的旧记录）
        // 注意：同一行号可能有多条旧记录（不同预算科目），需要全部删除
        Set<Long> needToDeleteOldLedgerIds = new HashSet<>();
        // 从needRollbackDiffDemBudgetLedgerMap中收集所有需要删除的旧记录ID
        for (BudgetLedger oldLedger : needRollbackDiffDemBudgetLedgerMap.values()) {
            needToDeleteOldLedgerIds.add(oldLedger.getId());
        }
        
        if (!extDetailsIncludeExtNewDemForQueryBoth.isEmpty()) {
            // 创建新的 BudgetLedger
            for (ExtDetailVo extDetail : extDetailsIncludeExtNewDemForQueryBoth) {
                // 通过行号找到对应的旧 BudgetLedger（维度不一致时，行号相同的旧记录需要被回滚）
                String detailLineNo = extDetail.getDetailLineNo();
                String rowNo = detailLineNo.contains("@") ? detailLineNo.substring(0, detailLineNo.indexOf("@")) : detailLineNo;
                List<BudgetLedger> oldLedgersForRow = diffDemRowNoToOldLedgersMap.get(rowNo);
                if (oldLedgersForRow == null || oldLedgersForRow.isEmpty()) {
                    continue;
                }
                BudgetLedger oldLedger = oldLedgersForRow.get(0);
                String oldBizItemCode = oldLedger.getBizItemCode();

                // 检查新维度的记录是否已经存在（避免使用错误的旧ID更新已存在的新记录）
                // 如果新维度的记录已经存在，应该直接更新它，而不是用旧ID创建新记录
                String newDetailLineNo = extDetail.getDetailLineNo();
                String newKey = extDetail.getDocumentNo() + "@" + newDetailLineNo;
                BudgetLedger existingNewLedger = existingBudgetLedgerMap.get(newKey);
                if (existingNewLedger != null) {
                    // 已在「维度一致更新」分支原地更新的那条流水不应物理删除；同行号其他科目旧流水仍在待删集合中
                    needToDeleteOldLedgerIds.remove(existingNewLedger.getId());
                    log.warn("========== 维度不一致创建新BudgetLedger时，发现新维度的记录已存在，跳过创建以避免覆盖: newKey={}, existingLedgerId={}, 已从待删除列表移除 ledgerId={} ==========",
                            newKey, existingNewLedger.getId(), existingNewLedger.getId());
                    continue;
                }

                // 维度不一致时，总是生成新ID，不复用旧ID
                // 因为维度不一致意味着这是完全不同的记录，应该删除旧记录，创建新记录
                Long ledgerId = identifierGenerator.nextId(null).longValue();
                log.info("========== 维度不一致创建新BudgetLedger时，生成新ID: 旧ID={}, 新ID={}, oldBizItemCode={}, newBizItemCode={} ==========", 
                        oldLedger.getId(), ledgerId, oldBizItemCode, newDetailLineNo);
                // 创建新的 BudgetLedger（createBudgetLedger 方法内部会处理空值转换）
                BigDecimal amount = extDetail.getAmount() == null ? BigDecimal.ZERO : extDetail.getAmount();
                String currency = extDetail.getCurrency() != null ? extDetail.getCurrency() : getDefaultCurrency();
                BudgetLedger newLedger = budgetQueryHelperService.createBudgetLedger(
                        ledgerId,
                        getBizType(),
                        extDetail.getDocumentNo(),
                        extDetail.getDetailLineNo(),
                        extDetail.getYear(),
                        extDetail.getMonth(),
                        extDetail.getIsInternal(),
                        extDetail.getManagementOrg(),
                        extDetail.getBudgetSubjectCode(),
                        extDetail.getMasterProjectCode(),
                        extDetail.getErpAssetType(),
                        currency,
                        amount,
                        oldLedger.getVersion(),
                        identifierGenerator,
                        operator,
                        operatorNo,
                        requestTime
                );
                newLedger.setActualYear(extDetail.getActualYear());
                newLedger.setActualMonth(extDetail.getActualMonth());
                if (StringUtils.isNotBlank(extDetail.getEffectType())) {
                    newLedger.setEffectType(extDetail.getEffectType());
                }

                // 设置 metadata 字段：如果传了metadata就使用传入的，如果没传就保留旧ledger的metadata
                if (extDetail.getMetadata() != null) {
                    newLedger.setMetadata(extDetail.getMetadata());
                } else if (oldLedger.getMetadata() != null) {
                    // 如果没传metadata，保留旧ledger的metadata
                    newLedger.setMetadata(oldLedger.getMetadata());
                }

                String ledgerKey = newLedger.getBizCode() + "@" + newLedger.getBizItemCode();
                // 维度不一致时，总是生成新ID，放入 needToAddBudgetLedgerMap 进行 INSERT
                needToAddBudgetLedgerMap.put(ledgerKey, newLedger);
                log.info("========== 维度不一致，创建新的 BudgetLedger（新ID，将INSERT）: id={}, 旧bizItemCode={}, 新bizItemCode={}, 旧amount={}, 新amount={}, 旧ledgerKey={}, 新ledgerKey={} ==========",
                        newLedger.getId(), oldBizItemCode, newLedger.getBizItemCode(),
                        oldLedger.getAmount() == null ? BigDecimal.ZERO : oldLedger.getAmount(),
                        newLedger.getAmount() == null ? BigDecimal.ZERO : newLedger.getAmount(),
                        oldLedger.getBizCode() + "@" + oldBizItemCode, ledgerKey);
                
                // 仅当新 ledger 与旧 ledger 的 bizItemCode 相同时，才从 existingBudgetLedgerMap 中移除旧 key 并放入新 ledger。
                // 若新维度与旧维度不同（不同科目等），不能移除旧 key，否则后续同一行号下「维度一致」的明细
                // （如仍为原科目）在检查 existingBudgetLedgerMap.get(newKey) 时会得到 null，导致重复创建新 ledger
                // 且不会从 needToDeleteOldLedgerIds 中移除该旧 ledger id，最终会误删已更新的流水，流水明细只剩一个科目。
                String oldLedgerKey = oldLedger.getBizCode() + "@" + oldLedger.getBizItemCode();
                if (Objects.equals(ledgerKey, oldLedgerKey)) {
                    existingBudgetLedgerMap.remove(oldLedgerKey);
                    existingBudgetLedgerMap.put(ledgerKey, newLedger);
                } else {
                    existingBudgetLedgerMap.put(ledgerKey, newLedger);
                }
            }
        }
        
        // 用于存储可用预算数值信息的 Map
        Map<String, DetailNumberVo> availableBudgetRatioMap = new HashMap<>();
        
        // 在调用 processSameDimensionUpdate 之前，先查询关联的预算流水
        Map<String, List<BudgetLedger>> queriedRelatedBudgetLedgerMap = queryRelatedBudgetLedgersByDetails(reqInfo, ehrCdToOrgCdExtMap, erpAcctCdToAcctCdExtMap);
        
        // 回滚可能已更新关联流水的 amountAvailable（如 11->300），但尚未写库，query 读到的是旧值；用回滚 map 中已更新的对象替换 query 结果中同 id 的流水，避免统一校验用到旧可用额
        Map<Long, BudgetLedger> relatedLedgerIdFromRollback = new HashMap<>();
        for (List<BudgetLedger> list : updatedRelatedBudgetLedgerMap.values()) {
            if (list != null) {
                for (BudgetLedger ledger : list) {
                    if (ledger != null && ledger.getId() != null) {
                        relatedLedgerIdFromRollback.put(ledger.getId(), ledger);
                    }
                }
            }
        }
        
        // 将查询到的关联流水合并到 updatedRelatedBudgetLedgerMap 中
        // 注意：如果 updatedRelatedBudgetLedgerMap 中已有数据，保留已有数据，不被 queriedRelatedBudgetLedgerMap 覆盖
        // 同时按「流水 key」再存一份：query 返回的 key 是请求明细 key（如 1@015-044-005@...），processSameDimensionUpdate 统一校验时用流水 key（如 1@015-044-005-001@...）查找，否则会查不到导致关联可用额只统计到部分
        if (!queriedRelatedBudgetLedgerMap.isEmpty()) {
            for (Map.Entry<String, List<BudgetLedger>> entry : queriedRelatedBudgetLedgerMap.entrySet()) {
                String key = entry.getKey();
                List<BudgetLedger> queriedLedgers = entry.getValue();
                // 用回滚后已更新的关联流水（含最新 amountAvailable）替换 query 中同 id 的流水，避免用到未提交的旧值
                List<BudgetLedger> listToPut = new ArrayList<>();
                if (queriedLedgers != null) {
                    for (BudgetLedger ledger : queriedLedgers) {
                        BudgetLedger fromRollback = ledger != null && ledger.getId() != null ? relatedLedgerIdFromRollback.get(ledger.getId()) : null;
                        listToPut.add(fromRollback != null ? fromRollback : ledger);
                    }
                }
                if (listToPut.isEmpty()) {
                    listToPut = queriedLedgers;
                }
                
                // 如果 updatedRelatedBudgetLedgerMap 中已有该 key，保留已有数据，不覆盖
                if (!updatedRelatedBudgetLedgerMap.containsKey(key)) {
                    updatedRelatedBudgetLedgerMap.put(key, listToPut);
                }
                String ledgerKey = requestKeyToLedgerKeyForRelated != null ? requestKeyToLedgerKeyForRelated.get(key) : null;
                if (StringUtils.isNotBlank(ledgerKey) && !updatedRelatedBudgetLedgerMap.containsKey(ledgerKey)) {
                    updatedRelatedBudgetLedgerMap.put(ledgerKey, listToPut);
                }
            }
        }
        
        // 用于收集所有需要新增的 BudgetPoolDemR
        Map<String, BudgetPoolDemR> allNeedToAddPoolDemRMap = new HashMap<>();

        if (CLAIM_BIZ_TYPE.equals(getBizType()) || CONTRACT_BIZ_TYPE.equals(getBizType())) {
            SUBMIT_POOL_BALANCE_TOUCHED_BIZ_KEYS.get().clear();
        }
        
        if (!needUpdateSameDemBudgetLedgerMap.isEmpty()) {
            try {
                // 若有维度不一致回滚，传入回滚后的 balanceMap，使同池同季度使用回滚后金额（含被跳过同维回滚的 ledger），避免 amountFrozen 等错误
                Map<String, BudgetBalance> diffBalanceMapForMerge = needRollbackDiffDemBudgetLedgerMap.isEmpty() ? null : needToUpdateDiffDemBudgetBalanceMap;
                processSameDimensionUpdate(needUpdateSameDemBudgetLedgerMap,
                        needToUpdateSameDemBudgetQuotaMap, needToUpdateSameDemBudgetBalanceMap,
                        needToAddBudgetQuotaHistory, needToAddBudgetBalanceHistory,
                        availableBudgetRatioMap, updatedRelatedBudgetLedgerMap,
                        ehrCdToOrgCdMap, ehrCdToOrgCdExtMap, erpAcctCdToAcctCdMap, erpAcctCdToAcctCdExtMap, prjCdToRelatedPrjCdExtMap,
                        allNeedToAddPoolDemRMap, erpAssetTypeToMemberCdMap, relatedLedgerDeductionAmountMap, diffBalanceMapForMerge);
            } catch (Exception e) {
                // 尝试提取明细标识
                if (!tryExtractDetailError(e, detailValidationResultMap, detailValidationMessageMap)) {
                    throw e; // 无法识别具体明细，作为整单错误抛出
                }
                hasDetailError = true;
            }
        }
        
        // 处理新增数据：创建新增的 BudgetLedger，单独使用 processSameDimensionUpdate 处理
        if (!needToAddBudgetLedgerList.isEmpty()) {
            for (ExtDetailVo extDetail : needToAddBudgetLedgerList) {
                String key = getBizKey(extDetail);
                
                // 使用雪花算法生成器生成 id 和 version
                Long ledgerId = identifierGenerator.nextId(null).longValue();
                BigDecimal amount = extDetail.getAmount() == null ? BigDecimal.ZERO : extDetail.getAmount();
                
                // 使用 budgetQueryHelperService.createBudgetLedger 创建 BudgetLedger（方法内部会处理空值转换）
                BudgetLedger ledger = budgetQueryHelperService.createBudgetLedger(
                        ledgerId,
                        getBizType(),
                        extDetail.getDocumentNo(),
                        extDetail.getDetailLineNo(),
                        extDetail.getYear(),
                        extDetail.getMonth(),
                        extDetail.getIsInternal(),
                        extDetail.getManagementOrg(),
                        extDetail.getBudgetSubjectCode(),
                        extDetail.getMasterProjectCode(),
                        extDetail.getErpAssetType(),
                        extDetail.getCurrency() != null ? extDetail.getCurrency() : getDefaultCurrency(),
                        amount,
                        null, // versionPre 为 null（新建）
                        identifierGenerator,
                        operator,
                        operatorNo,
                        requestTime
                );
                ledger.setActualYear(extDetail.getActualYear());
                ledger.setActualMonth(extDetail.getActualMonth());
                if (StringUtils.isNotBlank(extDetail.getEffectType())) {
                    ledger.setEffectType(extDetail.getEffectType());
                }
                
                // 设置 metadata 字段：如果传了metadata就使用传入的，如果没传就不设置（新建时可以为null）
                if (extDetail.getMetadata() != null) {
                    ledger.setMetadata(extDetail.getMetadata());
                }
                
                // 将新增的 ledger 保存到 needToAddBudgetLedgerMap 中（用于后续 insertBatch）
                needToAddBudgetLedgerMap.put(key, ledger);
                log.info("========== 创建新的 BudgetLedger: key={}, id={}, bizCode={}, bizItemCode={}, amount={}, version={} ==========",
                        key, ledgerId, ledger.getBizCode(), ledger.getBizItemCode(), amount, ledger.getVersion());
            }
        }
        
        // 单独处理新增的 ledger（包括完全新增的明细和维度不一致创建的新记录），使用 processSameDimensionUpdate 方法
        // 传入相同的 Map 参数，确保 balance 和 quota 的更新在同一 Map 中
        // 若有维度不一致回滚，传入回滚后的 balanceMap，以便同池同季度的 balance 使用回滚后的金额，避免 amountFrozen 等被重复扣减成负数
        if (!needToAddBudgetLedgerMap.isEmpty()) {
            try {
                Map<String, BudgetBalance> diffBalanceMapForMerge = needRollbackDiffDemBudgetLedgerMap.isEmpty() ? null : needToUpdateDiffDemBudgetBalanceMap;
                processSameDimensionUpdate(needToAddBudgetLedgerMap,
                        needToUpdateSameDemBudgetQuotaMap, needToUpdateSameDemBudgetBalanceMap,
                        needToAddBudgetQuotaHistory, needToAddBudgetBalanceHistory,
                        availableBudgetRatioMap, updatedRelatedBudgetLedgerMap,
                        ehrCdToOrgCdMap, ehrCdToOrgCdExtMap, erpAcctCdToAcctCdMap, erpAcctCdToAcctCdExtMap, prjCdToRelatedPrjCdExtMap,
                        allNeedToAddPoolDemRMap, erpAssetTypeToMemberCdMap, relatedLedgerDeductionAmountMap, diffBalanceMapForMerge);
            } catch (Exception e) {
                // 尝试提取明细标识
                if (!tryExtractDetailError(e, detailValidationResultMap, detailValidationMessageMap)) {
                    throw e; // 无法识别具体明细，作为整单错误抛出
                }
                hasDetailError = true;
            }
        }
        
        // 再次检查是否有明细级别的错误
        if (hasDetailError) {
            throw new DetailValidationException("部分明细处理失败，详见明细错误信息", 
                    detailValidationResultMap, detailValidationMessageMap);
        }
        
        // 批量更新数据库
        log.info("========== 准备批量更新数据库 ==========");
        batchUpdateDatabase(needToUpdateDiffDemBudgetBalanceMap, needToUpdateDiffDemBudgetQuotaMap,
                needToUpdateSameDemBudgetBalanceMap, needToUpdateSameDemBudgetQuotaMap,
                needToAddBudgetBalanceHistory, needToAddBudgetQuotaHistory,
                existingBudgetLedgerMap, needUpdateSameDemBudgetLedgerMap, needToAddBudgetLedgerMap,
                updatedRelatedBudgetLedgerMap, allNeedToAddPoolDemRMap, relatedLedgerDeductionAmountMap,
                needToDeleteOldLedgerIds, operator, operatorNo, requestTime);
        log.info("========== 批量更新数据库完成 ==========");
        
        // 处理 BUDGET_LEDGER_HEAD
        String bizCode = reqInfo.getDocumentNo();
        // INITIAL_SUBMITTED -> 持久化为 SUBMITTED，其它（如 APPROVED_UPDATE）直接透传
        String documentStatus = reqInfo.getDocumentStatus();
        String headStatus = "INITIAL_SUBMITTED".equals(documentStatus) ? "SUBMITTED" : documentStatus;
        budgetQueryHelperService.createOrUpdateBudgetLedgerHead(bizCode, getBizType(),
                documentName, dataSource, processName, headStatus, identifierGenerator, operator, operatorNo, requestTime);
        
        // 将 availableBudgetRatioMap 中的值设置到 details 中
        if (details != null) {
            for (DetailDetailVo detail : details) {
                String detailLineNo = detail.getDetailLineNo();
                DetailNumberVo detailNumberVo = availableBudgetRatioMap.get(detailLineNo);
                if (detailNumberVo != null) {
                    detail.setAvailableBudgetRatio(detailNumberVo.getAvailableBudgetRatio());
                    detail.setAmountQuota(detailNumberVo.getAmountQuota());
                    detail.setAmountFrozen(detailNumberVo.getAmountFrozen());
                    detail.setAmountActual(detailNumberVo.getAmountActual());
                    detail.setAmountAvailable(detailNumberVo.getAmountAvailable());
                    log.info("========== 设置可用预算信息到 detail: detailLineNo={}, amountAvailable={}, availableBudgetRatio={} ==========",
                            detailLineNo, detailNumberVo.getAmountAvailable(), detailNumberVo.getAvailableBudgetRatio());
                } else {
                    log.warn("========== 未找到可用预算信息: detailLineNo={}, availableBudgetRatioMap.keys={} ==========",
                            detailLineNo, availableBudgetRatioMap.keySet());
                }
            }
        }
        
        // 组装返回参数
        return buildResponse(budgetParams, type, detailValidationResultMap, detailValidationMessageMap, ehrCdToEhrNmMap, erpAcctCdToErpAcctNmMap);
    }

    /**
     * 删除单据明细分支（DETAIL_DELETED）
     * 仅对入参维度唯一定位到的明细执行逻辑删除，并回滚该明细占用的资金池/上游金额，不影响同单号其它明细。
     */
    protected BudgetRespVo superDetailDeleted(BudgetParams budgetParams, String type) {
        ReqInfoParams reqInfo = budgetParams.getReqInfo();
        ESBInfoParams esbInfo = budgetParams.getEsbInfo();
        LocalDateTime requestTime = parseRequestTime(esbInfo);

        if (reqInfo == null) {
            throw new IllegalArgumentException("请求信息不能为空");
        }
        if (CollectionUtils.isEmpty(reqInfo.getDetails())) {
            throw new IllegalArgumentException("明细列表不能为空");
        }

        String documentNo = reqInfo.getDocumentNo();
        String operator = reqInfo.getOperator();
        String operatorNo = reqInfo.getOperatorNo();

        // 1) HEAD 校验：单号+类型存在，且状态允许（APPROVED/APPROVED_UPDATE）
        LambdaQueryWrapper<BudgetLedgerHead> headWrapper = new LambdaQueryWrapper<>();
        headWrapper.eq(BudgetLedgerHead::getBizCode, documentNo)
                .eq(BudgetLedgerHead::getBizType, type)
                .eq(BudgetLedgerHead::getDeleted, Boolean.FALSE);
        BudgetLedgerHead head = budgetLedgerHeadMapper.selectOne(headWrapper);
        if (head == null) {
            throw new IllegalStateException("单据不存在，单号：" + documentNo);
        }
        String headStatus = head.getStatus();
        if (!"APPROVED".equals(headStatus) && !"APPROVED_UPDATE".equals(headStatus)) {
            throw new IllegalStateException("仅已审批通过或变更通过的单据允许明细删除，当前状态：" + headStatus);
        }

        // 2) 按维度定位唯一明细（BUDGET_LEDGER.deleted=0）
        List<ExtDetailVo> extDetailsForQuery = new ArrayList<>();
        for (DetailDetailVo d : reqInfo.getDetails()) {
            ExtDetailVo ext = new ExtDetailVo();
            BeanUtils.copyProperties(d, ext);
            ext.setDocumentNo(documentNo);
            extDetailsForQuery.add(ext);
        }
        List<BudgetLedger> candidates = queryExistingLedgers(extDetailsForQuery, type);

        Map<String, String> detailValidationResultMap = new HashMap<>();
        Map<String, String> detailValidationMessageMap = new HashMap<>();

        // 3) 逐明细删除（允许一请求多明细）
        for (DetailDetailVo reqDetail : reqInfo.getDetails()) {
            String detailLineNo = reqDetail.getDetailLineNo();
            try {
                BudgetLedger target = null;
                List<BudgetLedger> matched = new ArrayList<>();
                if (candidates != null) {
                    for (BudgetLedger l : candidates) {
                        if (l == null) continue;
                        if (!documentNo.equals(l.getBizCode())) continue;
                        if (l.getBizItemCode() != null && l.getBizItemCode().equals(detailLineNo) && Boolean.FALSE.equals(l.getDeleted())) {
                            matched.add(l);
                        }
                    }
                }

                if (matched.isEmpty()) {
                    throw new IllegalStateException("未找到可删除的明细");
                }
                if (matched.size() > 1) {
                    throw new IllegalStateException("参数不唯一，匹配到多条明细");
                }
                target = matched.get(0);

                // 4) 下游关联性校验：related_id=本明细id 的有效记录存在则不允许删除
                LambdaQueryWrapper<BudgetLedgerSelfR> downstreamWrapper = new LambdaQueryWrapper<>();
                downstreamWrapper.eq(BudgetLedgerSelfR::getRelatedId, target.getId())
                        .eq(BudgetLedgerSelfR::getDeleted, Boolean.FALSE);
                Long downstreamCount = budgetLedgerSelfRMapper.selectCount(downstreamWrapper);
                if (downstreamCount != null && downstreamCount > 0) {
                    throw new IllegalStateException("该明细已被其他流水单占用，不允许删除");
                }

                // 5) 回滚：复用现有“维度不一致回滚”流程（仅回滚该条明细）
                //    - 对 CONTRACT/CLAIM：如存在 SELF_R，会回滚到上游流水；否则回滚到资金池
                //    - 对 APPLY：直接回滚到资金池
                Map<String, BudgetLedger> needRollbackMap = new HashMap<>();
                String bizKey = target.getBizCode() + "@" + target.getBizItemCode();
                needRollbackMap.put(bizKey, target);

                Map<String, BudgetQuota> dummyQuotaMap = new HashMap<>();
                Map<String, BudgetBalance> needUpdateBalanceMap = new HashMap<>();
                List<BudgetQuotaHistory> dummyQuotaHistories = new ArrayList<>();
                List<BudgetBalanceHistory> needAddBalanceHistories = new ArrayList<>();

                // 映射表：仅为满足 queryQuotaAndBalanceByAllQuarters 入参
                Set<String> ehrCdSet = new HashSet<>();
                if (StringUtils.isNotBlank(target.getMorgCode())) {
                    ehrCdSet.add(target.getMorgCode());
                }
                BudgetQueryHelperService.EhrCdToOrgCdMapResult ehrMapResult = budgetQueryHelperService.queryEhrCdToOrgCdOneToOneMap(ehrCdSet);
                Map<String, String> ehrCdToOrgCdMap = ehrMapResult != null && ehrMapResult.getEhrCdToOrgCdMap() != null
                        ? new HashMap<>(ehrMapResult.getEhrCdToOrgCdMap())
                        : new HashMap<>();

                Set<String> subjectSet = new HashSet<>();
                if (StringUtils.isNotBlank(target.getBudgetSubjectCode()) && !"NAN-NAN".equals(target.getBudgetSubjectCode())) {
                    subjectSet.add(target.getBudgetSubjectCode());
                }
                BudgetQueryHelperService.ErpAcctCdToAcctCdMapResult acctMapResult = budgetQueryHelperService.queryErpAcctCdToAcctCdOneToOneMap(subjectSet);
                Map<String, String> erpAcctCdToAcctCdMap = acctMapResult != null && acctMapResult.getErpAcctCdToAcctCdMap() != null
                        ? new HashMap<>(acctMapResult.getErpAcctCdToAcctCdMap())
                        : new HashMap<>();

                Set<String> assetTypeSet = new HashSet<>();
                if (StringUtils.isNotBlank(target.getErpAssetType()) && !"NAN".equals(target.getErpAssetType())) {
                    assetTypeSet.add(target.getErpAssetType());
                }
                Map<String, String> erpAssetTypeToMemberCdMap = budgetQueryHelperService.queryErpAssetTypeToMemberCdMap(assetTypeSet);
                if (erpAssetTypeToMemberCdMap == null) {
                    erpAssetTypeToMemberCdMap = new HashMap<>();
                }

                Map<String, List<String>> ehrCdToOrgCdExtMap = budgetQueryHelperService.queryEhrCdToOrgCdMap(ehrCdSet);
                Map<String, List<String>> erpAcctCdToAcctCdExtMap = budgetQueryHelperService.queryErpAcctCdToAcctCdMap(subjectSet);

                processDiffDimensionRollback(needRollbackMap, dummyQuotaMap, needUpdateBalanceMap,
                        dummyQuotaHistories, needAddBalanceHistories,
                        ehrCdToOrgCdMap, erpAcctCdToAcctCdMap, erpAssetTypeToMemberCdMap,
                        ehrCdToOrgCdExtMap, erpAcctCdToAcctCdExtMap);

                // 6) 落库：更新余额、写余额历史（按 balanceId 去重）
                if (!needUpdateBalanceMap.isEmpty()) {
                    Map<Long, BudgetBalance> uniqueLogicDelBalances = new LinkedHashMap<>();
                    for (BudgetBalance b : needUpdateBalanceMap.values()) {
                        if (b != null && b.getId() != null) {
                            uniqueLogicDelBalances.putIfAbsent(b.getId(), b);
                        }
                    }
                    budgetBalanceMapper.updateBatchById(sortBalancesById(new ArrayList<>(uniqueLogicDelBalances.values())));
                }
                if (!needAddBalanceHistories.isEmpty()) {
                    for (BudgetBalanceHistory h : needAddBalanceHistories) {
                        budgetBalanceHistoryMapper.insert(h);
                    }
                }

                // 7) 逻辑删除本明细流水
                target.setOperator(operator);
                target.setOperatorNo(operatorNo);
                target.setUpdateTime(requestTime != null ? requestTime : LocalDateTime.now());
                // 先更新操作人/时间等审计字段，再通过 deleteById 触发 TableLogic 置 deleted=1（避免 updateById 未更新 deleted 的情况）
                budgetLedgerMapper.updateById(target);
                budgetLedgerMapper.deleteById(target.getId());

                // 7.1) 若该单据已无任何有效明细，则整单逻辑删除 HEAD（业务效果等价“整单删除”）
                LambdaQueryWrapper<BudgetLedger> remainingWrapper = new LambdaQueryWrapper<>();
                remainingWrapper.eq(BudgetLedger::getBizCode, documentNo)
                        .eq(BudgetLedger::getBizType, type)
                        .eq(BudgetLedger::getDeleted, Boolean.FALSE);
                Long remainingCount = budgetLedgerMapper.selectCount(remainingWrapper);
                if (remainingCount != null && remainingCount == 0L) {
                    head.setDeleted(Boolean.TRUE);
                    head.setOperator(operator);
                    head.setOperatorNo(operatorNo);
                    head.setUpdateTime(requestTime != null ? requestTime : LocalDateTime.now());
                    // 同上：先更新审计字段，再通过 deleteById 触发逻辑删除
                    budgetLedgerHeadMapper.updateById(head);
                    budgetLedgerHeadMapper.deleteById(head.getId());
                }

                // 8) 清理本明细对上游的 SELF_R 关系（逻辑删除，避免后续重复回滚/占用）
                LambdaUpdateWrapper<BudgetLedgerSelfR> selfRUpdateWrapper = new LambdaUpdateWrapper<>();
                selfRUpdateWrapper.eq(BudgetLedgerSelfR::getId, target.getId())
                        .eq(BudgetLedgerSelfR::getDeleted, Boolean.FALSE)
                        .set(BudgetLedgerSelfR::getDeleted, Boolean.TRUE)
                        .set(BudgetLedgerSelfR::getUpdateTime, requestTime != null ? requestTime : LocalDateTime.now());
                budgetLedgerSelfRMapper.update(null, selfRUpdateWrapper);

                detailValidationResultMap.put(detailLineNo, "0");
                detailValidationMessageMap.put(detailLineNo, "明细删除成功");
            } catch (Exception ex) {
                detailValidationResultMap.put(detailLineNo, "1");
                detailValidationMessageMap.put(detailLineNo, ex.getMessage() != null ? ex.getMessage() : "明细删除失败");
            }
        }

        // 9) 组装响应（沿用 BudgetRespVo 结构，逐明细返回校验结果）
        String instId = esbInfo != null ? esbInfo.getInstId() : null;
        if (instId == null || instId.isEmpty()) {
            String uuid = UUID.randomUUID().toString().replace("-", "");
            instId = "N" + uuid.substring(0, 8) + "." + uuid.substring(8, 16) + ".N"
                    + uuid.substring(16, 18) + "." + uuid.substring(18, 28) + ".N" + uuid.substring(28, 32);
        }
        boolean hasError = detailValidationResultMap.values().stream().anyMatch("1"::equals);
        String responseTime = LocalDateTime.now().format(DATE_TIME_FORMATTER);
        ESBRespInfoVo esbRespInfo = ESBRespInfoVo.builder()
                .instId(instId)
                .requestTime(esbInfo != null ? esbInfo.getRequestTime() : null)
                .attr1(esbInfo != null ? esbInfo.getAttr1() : null)
                .attr2(esbInfo != null ? esbInfo.getAttr2() : null)
                .attr3(esbInfo != null ? esbInfo.getAttr3() : null)
                .returnCode(hasError ? "E0001-BUDGET" : "A0001-BUDGET")
                .returnMsg(hasError ? "部分明细删除失败" : "明细删除处理完成")
                .returnStatus(hasError ? "E" : "S")
                .responseTime(responseTime)
                .build();

        List<DetailDetailVo> resultDetails = new ArrayList<>();
        for (DetailDetailVo d : reqInfo.getDetails()) {
            DetailDetailVo out = new DetailDetailVo();
            BeanUtils.copyProperties(d, out);
            String ln = d.getDetailLineNo();
            out.setValidationResult(detailValidationResultMap.getOrDefault(ln, "0"));
            out.setValidationMessage(detailValidationMessageMap.getOrDefault(ln, "处理成功"));
            resultDetails.add(out);
        }

        ResultInfoRespVo resultInfo = new ResultInfoRespVo();
        resultInfo.setDocumentNo(documentNo);
        resultInfo.setValidationResult(hasError ? "1" : "0");
        resultInfo.setValidationMessage(hasError ? "部分明细删除失败" : "明细删除成功");
        resultInfo.setProcessTime(LocalDateTime.now().format(PROCESS_TIME_FORMATTER));
        resultInfo.setDetails(resultDetails);

        BudgetRespVo respVo = new BudgetRespVo();
        respVo.setEsbInfo(esbRespInfo);
        respVo.setResultInfo(resultInfo);
        return respVo;
    }
    
    /**
     * 构建返回响应
     *
     * @param budgetParams 预算参数
     * @param type 业务类型（APPLY、CONTRACT、CLAIM）
     * @param detailValidationResultMap 明细校验结果Map
     * @param detailValidationMessageMap 明细校验消息Map
     * @param ehrCdToEhrNmMap EHR组织编码到组织名称的映射
     * @param erpAcctCdToErpAcctNmMap ERP科目编码到科目名称的映射
     * @return BudgetRespVo 预算响应
     */
    private BudgetRespVo buildResponse(BudgetParams budgetParams, String type,
                                       Map<String, String> detailValidationResultMap,
                                       Map<String, String> detailValidationMessageMap,
                                       Map<String, String> ehrCdToEhrNmMap,
                                       Map<String, String> erpAcctCdToErpAcctNmMap) {
        ESBInfoParams esbInfo = budgetParams.getEsbInfo();
        ReqInfoParams reqInfo = budgetParams.getReqInfo();
        
        // 生成 instId
        String instId = esbInfo.getInstId();
        if (instId == null || instId.isEmpty()) {
            String uuid = UUID.randomUUID().toString().replace("-", "");
            instId = "N" + uuid.substring(0, 8) + "." + uuid.substring(8, 16) + ".N"
                    + uuid.substring(16, 18) + "." + uuid.substring(18, 28) + ".N" + uuid.substring(28, 32);
        }
        String responseTime = LocalDateTime.now().format(DATE_TIME_FORMATTER);
        
        // 构建 ESB 响应信息
        ESBRespInfoVo esbRespInfo = ESBRespInfoVo.builder()
                .instId(instId)
                .requestTime(esbInfo.getRequestTime())
                .attr1(esbInfo.getAttr1())
                .attr2(esbInfo.getAttr2())
                .attr3(esbInfo.getAttr3())
                .returnCode("A0001-BUDGET")
                .returnMsg(getSuccessMessage(type))
                .returnStatus("S")
                .responseTime(responseTime)
                .build();
        
        // 构建返回的明细列表（包含校验结果）
        List<DetailDetailVo> resultDetails = new ArrayList<>();
        List<DetailDetailVo> reqDetails = reqInfo.getDetails();
        if (reqDetails != null) {
            for (DetailDetailVo detail : reqDetails) {
                DetailDetailVo resultDetail = new DetailDetailVo();
                BeanUtils.copyProperties(detail, resultDetail);
                
                // 设置管理组织名称
                String managementOrg = detail.getManagementOrg();
                if (StringUtils.isNotBlank(managementOrg) && ehrCdToEhrNmMap != null) {
                    String managementOrgName = ehrCdToEhrNmMap.get(managementOrg);
                    resultDetail.setManagementOrgName(managementOrgName);
                }
                
                // 设置预算科目名称
                String budgetSubjectCode = detail.getBudgetSubjectCode();
                if (StringUtils.isNotBlank(budgetSubjectCode) && erpAcctCdToErpAcctNmMap != null) {
                    String budgetSubjectName = erpAcctCdToErpAcctNmMap.get(budgetSubjectCode);
                    resultDetail.setBudgetSubjectName(budgetSubjectName);
                }
                
                // 设置明细级别的校验结果
                String detailLineNo = detail.getDetailLineNo();
                resultDetail.setValidationResult(detailValidationResultMap.getOrDefault(detailLineNo, "0"));
                resultDetail.setValidationMessage(detailValidationMessageMap.getOrDefault(detailLineNo, "处理成功"));
                
                // 设置预算相关数值字段（从 detail 中复制，因为已经在第984-1001行设置过了）
                resultDetail.setAvailableBudgetRatio(detail.getAvailableBudgetRatio());
                resultDetail.setAmountQuota(detail.getAmountQuota());
                resultDetail.setAmountFrozen(detail.getAmountFrozen());
                resultDetail.setAmountActual(detail.getAmountActual());
                resultDetail.setAmountAvailable(detail.getAmountAvailable());
                
                log.info("========== buildResponse - 设置预算相关数值字段到 resultDetail: detailLineNo={}, availableBudgetRatio={}, amountQuota={}, amountFrozen={}, amountActual={}, amountAvailable={} ==========",
                        detailLineNo, detail.getAvailableBudgetRatio(), detail.getAmountQuota(), detail.getAmountFrozen(), detail.getAmountActual(), detail.getAmountAvailable());
                
                resultDetails.add(resultDetail);
            }
        }
        
        // 构建结果信息
        ResultInfoRespVo resultInfo = new ResultInfoRespVo();
        resultInfo.setDocumentNo(reqInfo.getDocumentNo());
        resultInfo.setValidationResult("0");
        resultInfo.setValidationMessage(getSuccessMessage(type));
        resultInfo.setProcessTime(LocalDateTime.now().format(PROCESS_TIME_FORMATTER));
        resultInfo.setDetails(resultDetails);
        
        // 构建返回响应
        BudgetRespVo respVo = new BudgetRespVo();
        respVo.setEsbInfo(esbRespInfo);
        respVo.setResultInfo(resultInfo);
        return respVo;
    }
    
    /**
     * 获取成功消息
     *
     * @param type 业务类型
     * @return 成功消息
     */
    private String getSuccessMessage(String type) {
        switch (type) {
            case "APPLY":
                return "预算申请处理成功";
            case "CONTRACT":
                return "预算合同处理成功";
            case "CLAIM":
                return "付款/报销处理成功";
            default:
                return "预算处理成功";
        }
    }
    
    private List<DetailDetailVo> defaultList(List<DetailDetailVo> details) {
        return details == null ? Collections.emptyList() : details;
    }
    
    /**
     * 加载预算数据用于比较
     *
     * @param year 年度
     * @return 预算数据Map，key为维度组合，value为SystemProjectBudget
     */
    protected Map<String, SystemProjectBudget> loadBudgetDataForCompare(String year) {
        if (StringUtils.isBlank(year)) {
            return Collections.emptyMap();
        }
        LambdaQueryWrapper<SystemProjectBudget> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SystemProjectBudget::getYear, year)
                .eq(SystemProjectBudget::getDeleted, Boolean.FALSE);
        List<SystemProjectBudget> budgets = systemProjectBudgetMapper.selectList(wrapper);
        return budgets.stream()
                .collect(Collectors.toMap(
                        budget -> budget.getCustom2() + "@" + buildBudgetSubjectCode(budget.getCustom1(), budget.getAccount()) + "@" + budget.getProject(),
                        Function.identity(),
                        (a, b) -> a
                ));
    }
    
    /**
     * 构建预算科目编码
     *
     * @param custom1 自定义字段1
     * @param account 账户
     * @return 预算科目编码
     */
    private String buildBudgetSubjectCode(String custom1, String account) {
        if (StringUtils.isAnyBlank(custom1, account)) {
            return null;
        }
        return custom1 + "-" + account;
    }
    
    /**
     * 查询已存在的预算流水
     * 子类需要实现此方法以提供具体的查询逻辑
     *
     * @param extDetailsForQuery 扩展明细列表
     * @param type 业务类型（APPLY、CONTRACT、CLAIM）
     * @return 预算流水列表
     */
    protected abstract List<BudgetLedger> queryExistingLedgers(List<ExtDetailVo> extDetailsForQuery, String type);

    private static final String INITIAL_SUBMITTED_STATUS = "INITIAL_SUBMITTED";

    /**
     * 预算申请单 INITIAL_SUBMITTED 再次提交：整单回滚本单已有流水占用，并逻辑删除本次明细中未再出现的流水行。
     */
    protected void handleApplyResubmitRollbackIfNeeded(String demandOrderNo,
                                                       String documentStatus,
                                                       List<BudgetLedger> existingLedgers,
                                                       Map<String, BudgetLedger> existingBudgetLedgerMap,
                                                       Set<String> retainedLedgerBizKeys,
                                                       String operator,
                                                       String operatorNo,
                                                       Map<String, String> ehrCdToOrgCdMap,
                                                       Map<String, String> erpAcctCdToAcctCdMap,
                                                       Map<String, String> erpAssetTypeToMemberCdMap,
                                                       Map<String, List<String>> ehrCdToOrgCdExtMap,
                                                       Map<String, List<String>> erpAcctCdToAcctCdExtMap,
                                                       LocalDateTime requestTime) {
        if (!INITIAL_SUBMITTED_STATUS.equals(documentStatus)) {
            return;
        }
        if (CollectionUtils.isEmpty(existingLedgers)) {
            return;
        }
        executeResubmitLedgerRollbackAndPruneOrphans(APPLY_BIZ_TYPE, demandOrderNo, existingLedgers, existingBudgetLedgerMap,
                retainedLedgerBizKeys, operator, operatorNo, ehrCdToOrgCdMap, erpAcctCdToAcctCdMap, erpAssetTypeToMemberCdMap,
                ehrCdToOrgCdExtMap, erpAcctCdToAcctCdExtMap, requestTime);
    }

    /**
     * 再次提交：对本单有效流水整单 processDiffDimensionRollback，落库 balance，清理 SELF_R，再按 retained keys 逻辑删除多余流水并同步内存集合。
     */
    private Set<String> executeResubmitLedgerRollbackAndPruneOrphans(String logBizType,
                                                              String documentNo,
                                                              List<BudgetLedger> existingLedgers,
                                                              Map<String, BudgetLedger> existingBudgetLedgerMap,
                                                              Set<String> retainedLedgerBizKeys,
                                                              String operator,
                                                              String operatorNo,
                                                              Map<String, String> ehrCdToOrgCdMap,
                                                              Map<String, String> erpAcctCdToAcctCdMap,
                                                              Map<String, String> erpAssetTypeToMemberCdMap,
                                                              Map<String, List<String>> ehrCdToOrgCdExtMap,
                                                              Map<String, List<String>> erpAcctCdToAcctCdExtMap,
                                                              LocalDateTime requestTime) {
        Map<String, BudgetLedger> needRollbackMap = new HashMap<>();
        Set<Long> ledgerIds = new HashSet<>();
        for (BudgetLedger l : existingLedgers) {
            if (l == null) {
                continue;
            }
            if (!Boolean.FALSE.equals(l.getDeleted())) {
                continue;
            }
            if (StringUtils.isNotBlank(documentNo) && !documentNo.equals(l.getBizCode())) {
                continue;
            }
            String bizKey = l.getBizCode() + "@" + l.getBizItemCode();
            needRollbackMap.put(bizKey, l);
            if (l.getId() != null) {
                ledgerIds.add(l.getId());
            }
        }
        if (needRollbackMap.isEmpty()) {
            return Collections.emptySet();
        }

        log.info("========== {} 再次提交：开始整单回滚上游占用，documentNo={}, 需要回滚流水数={} ==========", logBizType, documentNo, needRollbackMap.size());

        Map<String, BudgetQuota> dummyQuotaMap = new HashMap<>();
        Map<String, BudgetBalance> needUpdateBalanceMap = new HashMap<>();
        List<BudgetQuotaHistory> dummyQuotaHistories = new ArrayList<>();
        List<BudgetBalanceHistory> needAddBalanceHistories = new ArrayList<>();

        processDiffDimensionRollback(needRollbackMap, dummyQuotaMap, needUpdateBalanceMap,
                dummyQuotaHistories, needAddBalanceHistories,
                ehrCdToOrgCdMap, erpAcctCdToAcctCdMap, erpAssetTypeToMemberCdMap,
                ehrCdToOrgCdExtMap, erpAcctCdToAcctCdExtMap);

        if (!needUpdateBalanceMap.isEmpty()) {
            Map<Long, BudgetBalance> uniqueResubmitBalances = new LinkedHashMap<>();
            for (BudgetBalance b : needUpdateBalanceMap.values()) {
                if (b != null && b.getId() != null) {
                    uniqueResubmitBalances.putIfAbsent(b.getId(), b);
                }
            }
            budgetBalanceMapper.updateBatchById(sortBalancesById(new ArrayList<>(uniqueResubmitBalances.values())));
        }
        if (!needAddBalanceHistories.isEmpty()) {
            for (BudgetBalanceHistory h : needAddBalanceHistories) {
                budgetBalanceHistoryMapper.insert(h);
            }
        }

        if (!ledgerIds.isEmpty()) {
            int deletedCount = budgetLedgerSelfRMapper.deleteByIdsAndBizType(ledgerIds, null);
            if (deletedCount > 0) {
                log.info("========== {} 再次提交：已清理旧 SELF_R 关系记录，删除 {} 条，ledgerIds={} ==========", logBizType, deletedCount, ledgerIds);
            }
        }

        if (retainedLedgerBizKeys != null && !retainedLedgerBizKeys.isEmpty()) {
            List<BudgetLedger> orphansRemovedFromList = new ArrayList<>();
            for (BudgetLedger l : existingLedgers) {
                if (l == null || l.getId() == null) {
                    continue;
                }
                if (!Boolean.FALSE.equals(l.getDeleted())) {
                    continue;
                }
                if (StringUtils.isNotBlank(documentNo) && !documentNo.equals(l.getBizCode())) {
                    continue;
                }
                String bizKey = l.getBizCode() + "@" + l.getBizItemCode();
                if (retainedLedgerBizKeys.contains(bizKey)) {
                    continue;
                }
                l.setOperator(operator);
                l.setOperatorNo(operatorNo);
                l.setUpdateTime(requestTime != null ? requestTime : LocalDateTime.now());
                budgetLedgerMapper.updateById(l);
                budgetLedgerMapper.deleteById(l.getId());
                log.info("========== {} 再次提交：逻辑删除本次请求未包含的旧流水: bizKey={}, id={} ==========", logBizType, bizKey, l.getId());
                orphansRemovedFromList.add(l);
                if (existingBudgetLedgerMap != null) {
                    existingBudgetLedgerMap.remove(bizKey);
                }
            }
            if (!orphansRemovedFromList.isEmpty()) {
                existingLedgers.removeAll(orphansRemovedFromList);
            }
        }

        log.info("========== {} 再次提交：整单回滚上游占用完成，documentNo={} ==========", logBizType, documentNo);
        return new HashSet<>(needRollbackMap.keySet());
    }

    /**
     * CLAIM/CONTRACT 再次提交时的“整单回滚上游占用”：
     * - 回滚资金池余额（balance）
     * - 若存在 SELF_R（付款单关联合同/申请单），同步把占用释放回上游流水（更新上游 BudgetLedger.amountAvailable 等）
     * - 清理旧的 SELF_R，确保本次重新占用时不会叠加历史关系
     * - 逻辑删除本次请求明细中已不再出现的流水行（占用已在上面整单回滚），避免库中残留旧科目行
     *
     * 说明：
     * - 只在 CLAIM/CONTRACT 且本单已有流水的情况下触发，避免影响首次提交及其他业务类型。
     */
    private Set<String> handleResubmitRollbackIfNeeded(ReqInfoParams reqInfo,
                                                String type,
                                                List<BudgetLedger> existingLedgers,
                                                Map<String, BudgetLedger> existingBudgetLedgerMap,
                                                Set<String> retainedLedgerBizKeys,
                                                String operator,
                                                String operatorNo,
                                                Map<String, String> ehrCdToOrgCdMap,
                                                Map<String, String> erpAcctCdToAcctCdMap,
                                                Map<String, String> erpAssetTypeToMemberCdMap,
                                                Map<String, List<String>> ehrCdToOrgCdExtMap,
                                                Map<String, List<String>> erpAcctCdToAcctCdExtMap,
                                                LocalDateTime requestTime) {
        if (reqInfo == null) {
            return Collections.emptySet();
        }
        String bizType = getBizType();
        boolean isClaimOrContract = CLAIM_BIZ_TYPE.equals(bizType) || CONTRACT_BIZ_TYPE.equals(bizType);
        if (!isClaimOrContract) {
            return Collections.emptySet();
        }
        if (!Objects.equals(bizType, type)) {
            return Collections.emptySet();
        }
        if (CollectionUtils.isEmpty(existingLedgers)) {
            return Collections.emptySet();
        }
        String documentStatus = reqInfo.getDocumentStatus();
        if (!INITIAL_SUBMITTED_STATUS.equals(documentStatus) && !"APPROVED_UPDATE".equals(documentStatus)) {
            return Collections.emptySet();
        }

        String documentNo = reqInfo.getDocumentNo();
        return executeResubmitLedgerRollbackAndPruneOrphans(bizType, documentNo, existingLedgers, existingBudgetLedgerMap,
                retainedLedgerBizKeys, operator, operatorNo, ehrCdToOrgCdMap, erpAcctCdToAcctCdMap, erpAssetTypeToMemberCdMap,
                ehrCdToOrgCdExtMap, erpAcctCdToAcctCdExtMap, requestTime);
    }
    
    /**
     * 获取业务key
     *
     * @param extDetail 扩展明细
     * @return 业务key（格式：业务单号 + "@" + 业务行号）
     */
    protected String getBizKey(ExtDetailVo extDetail) {
        return extDetail.getDocumentNo() + "@" + extDetail.getDetailLineNo();
    }
    
    /**
     * 判断维度是否一致（使用请求明细与流水的原始编码比较）
     *
     * @param extDetail 扩展明细
     * @param ledger 预算流水
     * @return 是否一致
     */
    protected boolean isDimensionSame(ExtDetailVo extDetail, BudgetLedger ledger) {
        return isDimensionSame(extDetail, ledger, null);
    }
    
    /**
     * 判断维度是否一致。当传入 ehrCdToOrgCdMap 时，管理组织按「预算组织」比较：
     * 若请求与流水的 EHR 编码（如 015-044-005 与 015-044-005-001）映射到同一 ORG_CD，则视为同一组织。
     *
     * @param extDetail 扩展明细
     * @param ledger 预算流水
     * @param ehrCdToOrgCdMap EHR 组织编码到预算组织编码的映射（可为 null，为 null 时按原始编码比较）
     * @return 是否一致
     */
    protected boolean isDimensionSame(ExtDetailVo extDetail, BudgetLedger ledger, Map<String, String> ehrCdToOrgCdMap) {
        // 处理空值转换（包括 null 和空字符串）
        String extBudgetSubjectCode = StringUtils.isBlank(extDetail.getBudgetSubjectCode()) ? "NAN-NAN" : extDetail.getBudgetSubjectCode();
        String extMasterProjectCode = StringUtils.isBlank(extDetail.getMasterProjectCode()) ? "NAN" : extDetail.getMasterProjectCode();
        String extErpAssetType = StringUtils.isBlank(extDetail.getErpAssetType()) ? "NAN" : extDetail.getErpAssetType();
        String extIsInternal = StringUtils.isBlank(extDetail.getIsInternal()) ? "1" : extDetail.getIsInternal();
        String ledgerIsInternal = StringUtils.isBlank(ledger.getIsInternal()) ? "1" : ledger.getIsInternal();
        boolean needCheckIsInternal = !"NAN".equals(extMasterProjectCode);
        
        // 管理组织：有映射表时按「预算组织」比较，否则按原始编码比较（同一预算组织的不同 EHR 编码如 015-044-005 / 015-044-005-001 可视为一致）
        String extMorg = extDetail.getManagementOrg();
        String ledgerMorg = ledger.getMorgCode();
        boolean morgSame;
        if (ehrCdToOrgCdMap != null && !ehrCdToOrgCdMap.isEmpty()) {
            String extNorm = StringUtils.isNotBlank(extMorg) && ehrCdToOrgCdMap.containsKey(extMorg) ? ehrCdToOrgCdMap.get(extMorg) : extMorg;
            String ledgerNorm = StringUtils.isNotBlank(ledgerMorg) && ehrCdToOrgCdMap.containsKey(ledgerMorg) ? ehrCdToOrgCdMap.get(ledgerMorg) : ledgerMorg;
            morgSame = Objects.equals(extNorm, ledgerNorm);
        } else {
            morgSame = Objects.equals(extMorg, ledgerMorg);
        }
        
        return morgSame
                && Objects.equals(extBudgetSubjectCode, ledger.getBudgetSubjectCode())
                && Objects.equals(extMasterProjectCode, ledger.getMasterProjectCode())
                && Objects.equals(extErpAssetType, ledger.getErpAssetType())
                && (!needCheckIsInternal || Objects.equals(extIsInternal, ledgerIsInternal));
    }
    
    /**
     * 根据月份获取季度消耗金额（从 BudgetLedger 的季度字段中读取）
     *
     * @param ledger 预算流水
     * @return 季度消耗金额
     */
    protected BigDecimal getConsumedAmountByMonth(BudgetLedger ledger) {
        if (ledger == null || StringUtils.isBlank(ledger.getMonth())) {
            return BigDecimal.ZERO;
        }
        String quarter = convertMonthToQuarter(ledger.getMonth());
        if (quarter == null) {
            return BigDecimal.ZERO;
        }
        switch (quarter) {
            case "q1": return ledger.getAmountConsumedQOne() == null ? BigDecimal.ZERO : ledger.getAmountConsumedQOne();
            case "q2": return ledger.getAmountConsumedQTwo() == null ? BigDecimal.ZERO : ledger.getAmountConsumedQTwo();
            case "q3": return ledger.getAmountConsumedQThree() == null ? BigDecimal.ZERO : ledger.getAmountConsumedQThree();
            case "q4": return ledger.getAmountConsumedQFour() == null ? BigDecimal.ZERO : ledger.getAmountConsumedQFour();
            default: return BigDecimal.ZERO;
        }
    }
    
    /**
     * 将月份转换为季度
     *
     * @param month 月份（1-12）
     * @return 季度（q1、q2、q3、q4）
     */
    protected String convertMonthToQuarter(String month) {
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
            log.warn("无法解析月份: {}", month);
        }
        return null;
    }
    
    /**
     * 映射 erpAssetType（如果以 "1" 或 "M" 开头，需要通过映射表映射）
     * 
     * @param originalErpAssetType 原始的 erpAssetType（映射前的值）
     * @param masterProjectCode 主项目编码，如果不为 "NAN" 则带项目，不需要映射 erpAssetType
     * @param erpAssetTypeToMemberCdMap 映射表（MEMBER_CD2 -> MEMBER_CD），从 VIEW_BUDGET_MEMBER_NAME_CODE 视图获取
     * @param errorContext 错误上下文信息（用于错误提示）
     * @return 映射后的 erpAssetType（如果不需要映射或映射不到，返回原值或 "NAN"）
     * @throws IllegalArgumentException 如果需要映射但映射不到时抛出异常
     */
    protected String mapErpAssetType(String originalErpAssetType, String masterProjectCode, Map<String, String> erpAssetTypeToMemberCdMap, String errorContext) {
        // 如果带项目（masterProjectCode 不为 "NAN"），不需要映射 erpAssetType，直接返回 "NAN"
        if (!"NAN".equals(masterProjectCode)) {
            return "NAN";
        }
        
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
        
        log.debug("========== erpAssetType 映射: {} -> {} ==========", originalErpAssetType, mappedValue);
        return mappedValue;
    } 
    
    /**
     * 处理维度不一致的数据（回滚逻辑）
     *
     * @param needRollbackDiffDemBudgetLedgerMap 需要回滚的预算流水Map
     * @param needToUpdateDiffDemBudgetQuotaMap 需要更新的预算额度Map
     * @param needToUpdateDiffDemBudgetBalanceMap 需要更新的预算余额Map
     * @param needToAddBudgetQuotaHistory 需要新增的预算额度历史列表
     * @param needToAddBudgetBalanceHistory 需要新增的预算余额历史列表
     * @param ehrCdToOrgCdMap EHR组织编码到管理组织编码的映射，key为EHR_CD，value为ORG_CD（一对一关系）
     */
    protected void processDiffDimensionRollback(Map<String, BudgetLedger> needRollbackDiffDemBudgetLedgerMap,
                                              Map<String, BudgetQuota> needToUpdateDiffDemBudgetQuotaMap,
                                              Map<String, BudgetBalance> needToUpdateDiffDemBudgetBalanceMap,
                                              List<BudgetQuotaHistory> needToAddBudgetQuotaHistory,
                                              List<BudgetBalanceHistory> needToAddBudgetBalanceHistory,
                                              Map<String, String> ehrCdToOrgCdMap,
                                              Map<String, String> erpAcctCdToAcctCdMap,
                                              Map<String, String> erpAssetTypeToMemberCdMap,
                                              Map<String, List<String>> ehrCdToOrgCdExtMap,
                                              Map<String, List<String>> erpAcctCdToAcctCdExtMap) {
        log.info("========== processDiffDimensionRollback 开始，需要处理的 ledger keys: {} ==========", needRollbackDiffDemBudgetLedgerMap.keySet());
        
        // 从需要回滚的 BudgetLedger 中提取 EHR 组织编码和预算科目编码，并查询它们的映射关系
        // 因为需要回滚的 BudgetLedger 可能包含当前请求中没有的维度信息
        Set<String> rollbackEhrCds = needRollbackDiffDemBudgetLedgerMap.values().stream()
                .map(BudgetLedger::getMorgCode)
                .filter(StringUtils::isNotBlank)
                .collect(Collectors.toSet());
        
        Set<String> rollbackBudgetSubjectCodes = needRollbackDiffDemBudgetLedgerMap.values().stream()
                .map(BudgetLedger::getBudgetSubjectCode)
                .filter(StringUtils::isNotBlank)
                .filter(code -> !"NAN-NAN".equals(code))
                .collect(Collectors.toSet());
        
        // 查询需要回滚的 BudgetLedger 的 EHR 组织编码映射（如果当前映射表中没有）
        if (!rollbackEhrCds.isEmpty()) {
            Set<String> missingEhrCds = new HashSet<>(rollbackEhrCds);
            if (ehrCdToOrgCdMap != null) {
                missingEhrCds.removeAll(ehrCdToOrgCdMap.keySet());
            }
            if (!missingEhrCds.isEmpty()) {
                log.info("========== processDiffDimensionRollback - 查询需要回滚的 BudgetLedger 的 EHR 组织编码映射，缺失的 EHR_CD: {} ==========", missingEhrCds);
                BudgetQueryHelperService.EhrCdToOrgCdMapResult rollbackEhrCdToOrgCdMapResult = budgetQueryHelperService.queryEhrCdToOrgCdOneToOneMap(missingEhrCds);
                Map<String, String> rollbackEhrCdToOrgCdMap = rollbackEhrCdToOrgCdMapResult.getEhrCdToOrgCdMap();
                if (ehrCdToOrgCdMap == null) {
                    ehrCdToOrgCdMap = new HashMap<>();
                }
                ehrCdToOrgCdMap.putAll(rollbackEhrCdToOrgCdMap);
                log.info("========== processDiffDimensionRollback - 合并后的 EHR 组织编码映射，共 {} 条 ==========", ehrCdToOrgCdMap.size());
            }
        }
        
        // 查询需要回滚的 BudgetLedger 的预算科目编码映射（如果当前映射表中没有）
        if (!rollbackBudgetSubjectCodes.isEmpty()) {
            Set<String> missingBudgetSubjectCodes = new HashSet<>(rollbackBudgetSubjectCodes);
            if (erpAcctCdToAcctCdMap != null) {
                missingBudgetSubjectCodes.removeAll(erpAcctCdToAcctCdMap.keySet());
            }
            if (!missingBudgetSubjectCodes.isEmpty()) {
                log.info("========== processDiffDimensionRollback - 查询需要回滚的 BudgetLedger 的预算科目编码映射，缺失的 ERP_ACCT_CD: {} ==========", missingBudgetSubjectCodes);
                BudgetQueryHelperService.ErpAcctCdToAcctCdMapResult rollbackErpAcctCdToAcctCdMapResult = budgetQueryHelperService.queryErpAcctCdToAcctCdOneToOneMap(missingBudgetSubjectCodes);
                Map<String, String> rollbackErpAcctCdToAcctCdMap = rollbackErpAcctCdToAcctCdMapResult.getErpAcctCdToAcctCdMap();
                if (erpAcctCdToAcctCdMap == null) {
                    erpAcctCdToAcctCdMap = new HashMap<>();
                }
                erpAcctCdToAcctCdMap.putAll(rollbackErpAcctCdToAcctCdMap);
                log.info("========== processDiffDimensionRollback - 合并后的预算科目编码映射，共 {} 条 ==========", erpAcctCdToAcctCdMap.size());
            }
        }
        
        // 过滤掉不受控的ledger，只对受控的ledger进行预算余额查询和回滚
        Map<String, BudgetLedger> controlledLedgerMap = new HashMap<>();
        for (Map.Entry<String, BudgetLedger> entry : needRollbackDiffDemBudgetLedgerMap.entrySet()) {
            BudgetLedger ledger = entry.getValue();
            // 跳过不受控的ledger（不受控明细会跳过预算余额查询和更新）
            if (!isUncontrolledLedger(ledger, ehrCdToOrgCdExtMap, erpAcctCdToAcctCdExtMap)) {
                controlledLedgerMap.put(entry.getKey(), ledger);
            } else {
                log.info("========== processDiffDimensionRollback - 不受控明细跳过预算余额查询: bizKey={}, morgCode={}, budgetSubjectCode={}, masterProjectCode={} ==========",
                        entry.getKey(), ledger.getMorgCode(), ledger.getBudgetSubjectCode(), ledger.getMasterProjectCode());
            }
        }
        
        // 使用 helper 方法查询所有季度的 BudgetQuota 和 BudgetBalance（只查询受控的ledger）
        BudgetQueryHelperService.BudgetQuotaBalanceResult result =
                budgetQueryHelperService.queryQuotaAndBalanceByAllQuarters(controlledLedgerMap, ehrCdToOrgCdMap, erpAcctCdToAcctCdMap, erpAssetTypeToMemberCdMap);

        // 检查是否有错误
        if (result.hasError()) {
            throw new IllegalStateException(result.getErrorMessage());
        }

        // 直接将查询结果放入 Map
        needToUpdateDiffDemBudgetQuotaMap.putAll(result.getQuotaMap());
        needToUpdateDiffDemBudgetBalanceMap.putAll(result.getBalanceMap());
        
        // 查询需要回滚的付款单/合同单流水的关联流水（用于回滚到关联流水上）
        // 1）合同单关联申请单、付款单关联申请单：维度变更回滚时需把占用/扣减金额加回申请单流水，否则重新提交时申请单 amountAvailable 未恢复会导致返回的 amountFrozen/amountAvailable 错误为 0
        // 2）付款单关联合同单：维度变更回滚时需把扣减金额加回合同单流水，与关联申请单同理
        Map<String, List<BudgetLedger>> relatedBudgetLedgerMapForRollback = new HashMap<>();
        Set<Long> ledgerIdsForQuery = needRollbackDiffDemBudgetLedgerMap.values().stream()
                .map(BudgetLedger::getId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        
        boolean needQueryRelatedLedgers = !ledgerIdsForQuery.isEmpty()
                && (CLAIM_BIZ_TYPE.equals(getBizType()) || CONTRACT_BIZ_TYPE.equals(getBizType()));
        if (needQueryRelatedLedgers) {
            // 查询关联的 APPLY 流水（合同单关联申请单、付款单关联申请单）
            List<BudgetLedgerSelfR> applySelfRs = budgetLedgerSelfRMapper.selectByIdsAndBizType(ledgerIdsForQuery, "APPLY");
            // 付款单关联合同单：在 CLAIM 场景下也需要查询关联的 CONTRACT 流水（并在同一 ledgerId 下优先回滚到 CONTRACT）
            List<BudgetLedgerSelfR> contractSelfRs = Collections.emptyList();
            boolean isClaimBiz = CLAIM_BIZ_TYPE.equals(getBizType());
            if (isClaimBiz) {
                contractSelfRs = budgetLedgerSelfRMapper.selectByIdsAndBizType(ledgerIdsForQuery, CONTRACT_BIZ_TYPE);
            }
            
            Set<Long> ledgerIdsWithContract = contractSelfRs == null
                    ? Collections.emptySet()
                    : contractSelfRs.stream()
                    .map(BudgetLedgerSelfR::getId)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());
            
            if (!CollectionUtils.isEmpty(applySelfRs) || (!CollectionUtils.isEmpty(contractSelfRs))) {
                Set<Long> relatedLedgerIds = applySelfRs.stream()
                        .map(BudgetLedgerSelfR::getRelatedId)
                        .filter(Objects::nonNull)
                        .collect(Collectors.toSet());
                if (!CollectionUtils.isEmpty(contractSelfRs)) {
                    relatedLedgerIds.addAll(contractSelfRs.stream()
                            .map(BudgetLedgerSelfR::getRelatedId)
                            .filter(Objects::nonNull)
                            .collect(Collectors.toSet()));
                }
                if (!relatedLedgerIds.isEmpty()) {
                    List<BudgetLedger> relatedLedgers = budgetLedgerMapper.selectBatchIds(relatedLedgerIds);
                    if (!CollectionUtils.isEmpty(relatedLedgers)) {
                        // ledgerId(当前单据流水id) -> 关联流水 relatedIds(可能多条，不能用 toMap 覆盖)
                        Map<Long, List<Long>> ledgerIdToApplyRelatedIdsMap = applySelfRs == null
                                ? Collections.emptyMap()
                                : applySelfRs.stream()
                                .filter(selfR -> selfR.getId() != null && selfR.getRelatedId() != null)
                                .collect(Collectors.groupingBy(
                                        BudgetLedgerSelfR::getId,
                                        Collectors.mapping(BudgetLedgerSelfR::getRelatedId, Collectors.toList())));
                        
                        Map<Long, List<Long>> ledgerIdToContractRelatedIdsMap = contractSelfRs == null
                                ? Collections.emptyMap()
                                : contractSelfRs.stream()
                                .filter(selfR -> selfR.getId() != null && selfR.getRelatedId() != null)
                                .collect(Collectors.groupingBy(
                                        BudgetLedgerSelfR::getId,
                                        Collectors.mapping(BudgetLedgerSelfR::getRelatedId, Collectors.toList())));

                        Map<Long, BudgetLedger> relatedLedgerMap = relatedLedgers.stream()
                                .collect(Collectors.toMap(BudgetLedger::getId, Function.identity(), (v1, v2) -> v1));
                        for (BudgetLedger ledger : needRollbackDiffDemBudgetLedgerMap.values()) {
                            if (ledger.getId() == null) continue;
                            String bizKey = ledger.getBizCode() + "@" + ledger.getBizItemCode();
                            
                            // 同一 ledgerId 下优先回滚到 CONTRACT；否则回滚到所有 APPLY 关联流水
                            if (isClaimBiz && ledgerIdsWithContract.contains(ledger.getId())) {
                                List<Long> contractRelatedIds = ledgerIdToContractRelatedIdsMap.getOrDefault(ledger.getId(), Collections.emptyList());
                                for (Long relatedId : contractRelatedIds) {
                                    BudgetLedger relatedLedger = relatedLedgerMap.get(relatedId);
                                    if (relatedLedger != null) {
                                        relatedBudgetLedgerMapForRollback.computeIfAbsent(bizKey, k -> new ArrayList<>()).add(relatedLedger);
                                    }
                                }
                            } else {
                                List<Long> applyRelatedIds = ledgerIdToApplyRelatedIdsMap.getOrDefault(ledger.getId(), Collections.emptyList());
                                for (Long relatedId : applyRelatedIds) {
                                    BudgetLedger relatedLedger = relatedLedgerMap.get(relatedId);
                                    if (relatedLedger != null) {
                                        relatedBudgetLedgerMapForRollback.computeIfAbsent(bizKey, k -> new ArrayList<>()).add(relatedLedger);
                                    }
                                }
                            }
                        }
                        log.info("========== processDiffDimensionRollback - 查询到关联的申请单流水: 单据流水数量={}, 关联申请单流水数量={} ==========",
                                needRollbackDiffDemBudgetLedgerMap.size(), relatedBudgetLedgerMapForRollback.size());
                    }
                }
            }
        }
        
        // 用于收集需要更新的关联预算流水
        Map<String, List<BudgetLedger>> updatedRelatedBudgetLedgerMap = new HashMap<>();
        
        // 执行回滚逻辑：遍历 needRollbackDiffDemBudgetLedgerMap
        for (Map.Entry<String, BudgetLedger> entry : needRollbackDiffDemBudgetLedgerMap.entrySet()) {
            String bizKey = entry.getKey(); // bizCode + "@" + bizItemCode
            BudgetLedger ledger = entry.getValue();
            
            // 判断是否不受控（需要跳过预算余额回滚，但仍保存数据到BUDGET_LEDGER表）
            if (isUncontrolledLedger(ledger, ehrCdToOrgCdExtMap, erpAcctCdToAcctCdExtMap)) {
                log.info("========== processDiffDimensionRollback - 不受控明细跳过预算余额回滚: bizKey={}, morgCode={}, budgetSubjectCode={}, masterProjectCode={} ==========",
                        bizKey, ledger.getMorgCode(), ledger.getBudgetSubjectCode(), ledger.getMasterProjectCode());
                continue;
            }
            
            // 项目非 NAN 且 isInternal=1 时跳过回滚
            if (!"NAN".equals(ledger.getMasterProjectCode()) && "1".equals(ledger.getIsInternal())) {
                log.info("========== processDiffDimensionRollback - 内部项目跳过回滚: bizKey={}, masterProjectCode={}, isInternal={} ==========",
                        bizKey, ledger.getMasterProjectCode(), ledger.getIsInternal());
                continue;
            }
            
            // 使用 helper service 计算需要回滚的季度
            BudgetQueryHelperService.RollbackQuartersResult rollbackResult = budgetQueryHelperService.calculateRollbackQuarters(ledger);
            List<String> quartersToRollback = rollbackResult.getQuartersToRollback();
            Map<String, BigDecimal> quarterRollbackAmountMap = rollbackResult.getQuarterRollbackAmountMap();
            BigDecimal amountAvailable = rollbackResult.getTotalAmount();
            
            if (quartersToRollback.isEmpty()) {
                log.info("========== processDiffDimensionRollback - 无需回滚: bizKey={} ==========", bizKey);
                continue;
            }
            
            log.info("========== processDiffDimensionRollback - 需要回滚的季度: bizKey={}, quarters={}, totalAmount={} ==========",
                    bizKey, quartersToRollback, amountAvailable);
            
            // 检查是否有关联的需求单流水
            List<BudgetLedger> relatedLedgers = relatedBudgetLedgerMapForRollback.get(bizKey);
            boolean hasRelatedLedgers = !CollectionUtils.isEmpty(relatedLedgers);
            
            if (hasRelatedLedgers) {
                log.info("========== processDiffDimensionRollback - 发现关联的需求单流水，回滚到关联流水上: bizKey={}, 关联流水数量={} ==========",
                        bizKey, relatedLedgers.size());
            } else {
                log.info("========== processDiffDimensionRollback - 无关联的需求单流水，回滚到预算池: bizKey={} ==========", bizKey);
            }
            
            // 对每个需要回滚的季度进行处理
            for (String rollbackQuarter : quartersToRollback) {
                BigDecimal rollbackAmount = quarterRollbackAmountMap.get(rollbackQuarter);
                String bizKeyQuarter = bizKey + "@" + rollbackQuarter;
                
                BudgetBalance balance;
                if (StringUtils.isNotBlank(ledger.getPoolDimensionKey())) {
                    balance = budgetQueryHelperService.selectBudgetBalanceByPoolDimensionKey(ledger.getPoolDimensionKey(), rollbackQuarter);
                    needToUpdateDiffDemBudgetBalanceMap.put(bizKeyQuarter, balance);
                } else {
                    balance = needToUpdateDiffDemBudgetBalanceMap.get(bizKeyQuarter);
                }
                if (balance == null) {
                    // 检查回滚金额是否为0，如果为0则允许继续处理，跳过余额校验
                    if (rollbackAmount != null && rollbackAmount.compareTo(BigDecimal.ZERO) == 0) {
                        log.warn("========== bizKeyQuarter={} 在 needToUpdateDiffDemBudgetBalanceMap 中找不到对应的 balance，但回滚金额为0，跳过余额校验，允许继续处理 ==========", bizKeyQuarter);
                        continue; // 跳过该季度，继续处理下一个
                    }
                    
                    log.error("========== bizKeyQuarter={} 在 needToUpdateDiffDemBudgetBalanceMap 中找不到对应的 balance ==========", bizKeyQuarter);
                    String[] bizKeyParts = bizKey.split("@");
                    String managementOrg = bizKeyParts.length > 2 ? bizKeyParts[2] : "未知";
                    String budgetSubjectCode = bizKeyParts.length > 3 ? bizKeyParts[3] : "未知";
                    // 在异常消息中包含明细标识，格式：明细 [bizKey]，以便 tryExtractDetailError 能识别
                    throw new IllegalStateException(
                        String.format("明细 [%s] 预算余额不足,还请检查可用余额并重新修改单据信息或进行预算调整。维度信息:年度=%s,季度=%s,管理组织=%s,预算科目=%s, Available Budget Insufficient",
                                     bizKey, ledger.getYear(), rollbackQuarter, managementOrg, budgetSubjectCode)
                    );
                }
                
                Long poolId = balance.getPoolId();

                // 创建预算余额历史记录
                BudgetBalanceHistory balanceHistory = new BudgetBalanceHistory();
                BeanUtils.copyProperties(balance, balanceHistory);
                balanceHistory.setId(identifierGenerator.nextId(balanceHistory).longValue());
                balanceHistory.setBalanceId(balance.getId());
                balanceHistory.setDeleted(Boolean.FALSE);
                
                if (hasRelatedLedgers) {
                    // 如果有关联的需求单流水，使用 rollbackBalanceAmountForSameDimension 方法回滚到关联流水上（维度不一致回滚不汇总，传 null）
                    Map<String, List<BudgetLedger>> returnedRelatedBudgetLedgerMap = rollbackBalanceAmountForSameDimension(balance, balanceHistory, ledger, rollbackQuarter, rollbackAmount, poolId, relatedBudgetLedgerMapForRollback, null, null);
                    
                    // 收集更新后的关联预算流水
                    if (returnedRelatedBudgetLedgerMap != null && !returnedRelatedBudgetLedgerMap.isEmpty()) {
                        updatedRelatedBudgetLedgerMap.putAll(returnedRelatedBudgetLedgerMap);
                    }
                } else {
                    // 如果没有关联的需求单流水，使用 rollbackBalanceAmountForDiffDimension 方法回滚到预算池
                    rollbackBalanceAmountForDiffDimension(balance, balanceHistory, ledger, rollbackQuarter, rollbackAmount, poolId);
                }
                
                balance.setVersion(ledger.getVersion());
                needToAddBudgetBalanceHistory.add(balanceHistory);
                
                log.info("========== processDiffDimensionRollback - 释放后: poolId={}, quarter={}, amountFrozen={}, amountAvailable={} ==========",
                        poolId, rollbackQuarter, balance.getAmountFrozen(), balance.getAmountAvailable());
            }
        }
        
        // 更新关联单据的 BudgetLedger（如果有关联单据，回滚时会更新关联单据的 amountAvailable 和季度消耗金额）
        if (!updatedRelatedBudgetLedgerMap.isEmpty()) {
            List<BudgetLedger> relatedLedgersToUpdate = new ArrayList<>();
            for (List<BudgetLedger> relatedLedgers : updatedRelatedBudgetLedgerMap.values()) {
                if (!CollectionUtils.isEmpty(relatedLedgers)) {
                    relatedLedgersToUpdate.addAll(relatedLedgers);
                }
            }
            if (!relatedLedgersToUpdate.isEmpty()) {
                // 去重（根据 id）
                Map<Long, BudgetLedger> uniqueRelatedLedgersMap = relatedLedgersToUpdate.stream()
                        .collect(Collectors.toMap(BudgetLedger::getId, Function.identity(), (existing, replacement) -> existing));
                List<BudgetLedger> uniqueRelatedLedgers = new ArrayList<>(uniqueRelatedLedgersMap.values());
                budgetLedgerMapper.updateBatchById(uniqueRelatedLedgers);
                log.info("========== processDiffDimensionRollback - 更新关联预算流水: 更新了 {} 条记录 ==========", uniqueRelatedLedgers.size());
            }
        }
    }
    
    /**
     * 处理维度一致的数据（回滚逻辑）
     *
     * @param needUpdateSameDemBudgetLedgerMap 需要更新的预算流水Map（维度一致）
     * @param needToUpdateSameDemBudgetQuotaMap 需要更新的预算额度Map
     * @param needToUpdateSameDemBudgetBalanceMap 需要更新的预算余额Map
     * @param needToAddBudgetQuotaHistory 需要新增的预算额度历史列表
     * @param needToAddBudgetBalanceHistory 需要新增的预算余额历史列表
     * @param ledgerKeysAlreadyRolledBackByDiffDimension 已在「维度不一致回滚」中处理过的 ledger key 集合，这些将跳过本次同一维度回滚，避免同一笔资金池被回滚两次；可为 null 表示不跳过
     * @return 更新后的关联预算流水Map，key为bizCode + "@" + bizItemCode，value为关联的BudgetLedger列表
     */
    protected Map<String, List<BudgetLedger>> processSameDimensionRollback(Map<String, BudgetLedger> needUpdateSameDemBudgetLedgerMap,
                                                                             Map<String, BudgetQuota> needToUpdateSameDemBudgetQuotaMap,
                                                                             Map<String, BudgetBalance> needToUpdateSameDemBudgetBalanceMap,
                                                                             List<BudgetQuotaHistory> needToAddBudgetQuotaHistory,
                                                                             List<BudgetBalanceHistory> needToAddBudgetBalanceHistory,
                                                                             Map<String, String> ehrCdToOrgCdMap,
                                                                             Map<String, String> erpAcctCdToAcctCdMap,
                                                                             Map<String, String> erpAssetTypeToMemberCdMap,
                                                                             Map<String, List<String>> ehrCdToOrgCdExtMap,
                                                                             Map<String, List<String>> erpAcctCdToAcctCdExtMap,
                                                                             Set<String> ledgerKeysAlreadyRolledBackByDiffDimension) {
        log.info("========== processSameDimensionRollback 开始，需要处理的 ledger keys: {} ==========", needUpdateSameDemBudgetLedgerMap.keySet());
        
        // 过滤掉不受控的ledger，只对受控的ledger进行预算余额查询和回滚
        Map<String, BudgetLedger> controlledLedgerMap = new HashMap<>();
        for (Map.Entry<String, BudgetLedger> entry : needUpdateSameDemBudgetLedgerMap.entrySet()) {
            BudgetLedger ledger = entry.getValue();
            // 跳过不受控的ledger（不受控明细会跳过预算余额查询和更新）
            if (!isUncontrolledLedger(ledger, ehrCdToOrgCdExtMap, erpAcctCdToAcctCdExtMap)) {
                controlledLedgerMap.put(entry.getKey(), ledger);
            } else {
                log.info("========== processSameDimensionRollback - 不受控明细跳过预算余额查询和回滚: bizKey={}, morgCode={}, budgetSubjectCode={}, masterProjectCode={} ==========",
                        entry.getKey(), ledger.getMorgCode(), ledger.getBudgetSubjectCode(), ledger.getMasterProjectCode());
            }
        }
        
        // 使用 helper 方法查询所有季度的 BudgetQuota 和 BudgetBalance（只查询受控的ledger）
        BudgetQueryHelperService.BudgetQuotaBalanceResult result = 
                budgetQueryHelperService.queryQuotaAndBalanceByAllQuarters(controlledLedgerMap, ehrCdToOrgCdMap, erpAcctCdToAcctCdMap, erpAssetTypeToMemberCdMap);
        
        // 检查是否有错误
        if (result.hasError()) {
            throw new IllegalStateException(result.getErrorMessage());
        }
        
        // 直接将查询结果放入 Map
        needToUpdateSameDemBudgetQuotaMap.putAll(result.getQuotaMap());
        needToUpdateSameDemBudgetBalanceMap.putAll(result.getBalanceMap());
        
        // 用于收集所有更新后的关联预算流水Map
        Map<String, List<BudgetLedger>> updatedRelatedBudgetLedgerMap = new HashMap<>();
        if (result.getRelatedBudgetLedgerMap() != null) {
            updatedRelatedBudgetLedgerMap.putAll(result.getRelatedBudgetLedgerMap());
        }
        
        // 按关联流水id+季度汇总回滚金额，避免同一关联流水被多条付款/合同明细各回滚一次导致重复加回（合同未付金额错误变大）
        Map<Long, Map<String, BigDecimal>> aggregatedRelatedRollbackMap = new HashMap<>();
        for (Map.Entry<String, BudgetLedger> entry : needUpdateSameDemBudgetLedgerMap.entrySet()) {
            String bizKey = entry.getKey();
            BudgetLedger ledger = entry.getValue();
            if (ledger == null) {
                continue;
            }
            BudgetQueryHelperService.RollbackQuartersResult rollbackResult = budgetQueryHelperService.calculateRollbackQuarters(ledger);
            List<String> quartersToRollback = rollbackResult.getQuartersToRollback();
            Map<String, BigDecimal> quarterRollbackAmountMap = rollbackResult.getQuarterRollbackAmountMap();
            if (quartersToRollback == null || quarterRollbackAmountMap == null) {
                continue;
            }
            for (String rollbackQuarter : quartersToRollback) {
                BigDecimal rollbackAmount = quarterRollbackAmountMap.get(rollbackQuarter);
                if (rollbackAmount == null || rollbackAmount.compareTo(BigDecimal.ZERO) <= 0) {
                    continue;
                }
                Map<Long, BigDecimal> amounts = getRollbackAmountsForRelatedLedgers(ledger, rollbackQuarter, rollbackAmount, updatedRelatedBudgetLedgerMap);
                if (amounts != null) {
                    for (Map.Entry<Long, BigDecimal> e : amounts.entrySet()) {
                        if (e.getKey() != null && e.getValue() != null && e.getValue().compareTo(BigDecimal.ZERO) > 0) {
                            aggregatedRelatedRollbackMap.computeIfAbsent(e.getKey(), k -> new HashMap<>()).merge(rollbackQuarter, e.getValue(), BigDecimal::add);
                        }
                    }
                }
            }
        }
        Set<String> appliedRelatedLedgerQuarterSet = new HashSet<>();
        
        // 查询需要回滚的付款单流水的关联需求单流水（用于回滚到关联流水上）
        // 注意：这里需要基于BUDGET_LEDGER_SELF_R表查询，以确保获取所有关联记录
        Set<Long> ledgerIdsForQuery = needUpdateSameDemBudgetLedgerMap.values().stream()
                .map(BudgetLedger::getId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        
        if (!ledgerIdsForQuery.isEmpty() && CLAIM_BIZ_TYPE.equals(getBizType())) {
            // 查询关联的 APPLY 流水
            List<BudgetLedgerSelfR> applySelfRs = budgetLedgerSelfRMapper.selectByIdsAndBizType(ledgerIdsForQuery, "APPLY");
            if (!CollectionUtils.isEmpty(applySelfRs)) {
                // 获取关联的需求单流水ID
                Set<Long> relatedLedgerIds = applySelfRs.stream()
                        .map(BudgetLedgerSelfR::getRelatedId)
                        .filter(Objects::nonNull)
                        .collect(Collectors.toSet());
                
                if (!relatedLedgerIds.isEmpty()) {
                    // 查询关联的需求单流水
                    List<BudgetLedger> relatedLedgers = budgetLedgerMapper.selectBatchIds(relatedLedgerIds);
                    if (!CollectionUtils.isEmpty(relatedLedgers)) {
                        // 构建 Map：key 为付款单流水的 bizKey，value 为关联的需求单流水列表
                        // 需要通过 BudgetLedgerSelfR 建立付款单流水和需求单流水的映射关系
                        // 注意：一个付款单可以对应多个申请单，所以需要使用groupingBy构建Map<Long, List<Long>>
                        Map<Long, List<Long>> ledgerIdToRelatedIdsMap = applySelfRs.stream()
                                .filter(selfR -> selfR.getId() != null && selfR.getRelatedId() != null)
                                .collect(Collectors.groupingBy(
                                        BudgetLedgerSelfR::getId,
                                        Collectors.mapping(BudgetLedgerSelfR::getRelatedId, Collectors.toList())
                                ));
                        
                        Map<Long, BudgetLedger> relatedLedgerMap = relatedLedgers.stream()
                                .collect(Collectors.toMap(BudgetLedger::getId, Function.identity(), (v1, v2) -> v1));
                        
                        for (BudgetLedger ledger : needUpdateSameDemBudgetLedgerMap.values()) {
                            if (ledger.getId() == null) {
                                continue;
                            }
                            List<Long> relatedIds = ledgerIdToRelatedIdsMap.get(ledger.getId());
                            if (relatedIds != null && !relatedIds.isEmpty()) {
                                String bizKey = ledger.getBizCode() + "@" + ledger.getBizItemCode();
                                for (Long relatedId : relatedIds) {
                                    BudgetLedger relatedLedger = relatedLedgerMap.get(relatedId);
                                    if (relatedLedger != null) {
                                        // 如果updatedRelatedBudgetLedgerMap中已有该bizKey的记录，需要检查是否已包含该relatedLedger，避免重复添加
                                        List<BudgetLedger> existingRelatedLedgers = updatedRelatedBudgetLedgerMap.computeIfAbsent(bizKey, k -> new ArrayList<>());
                                        // 检查是否已存在相同的relatedLedger（根据ID判断）
                                        boolean alreadyExists = existingRelatedLedgers.stream()
                                                .anyMatch(l -> l.getId() != null && l.getId().equals(relatedLedger.getId()));
                                        if (!alreadyExists) {
                                            existingRelatedLedgers.add(relatedLedger);
                                        }
                                    }
                                }
                            }
                        }
                        
                        log.info("========== processSameDimensionRollback - 查询到关联的需求单流水: 付款单流水数量={}, 关联需求单流水数量={} ==========",
                                needUpdateSameDemBudgetLedgerMap.size(), applySelfRs.size());
                    }
                }
            }
        }
        
        // 执行回滚逻辑：遍历 needUpdateSameDemBudgetLedgerMap（包括受控和不受控的ledger）
        // 注意：不受控的ledger已经在上面的查询中过滤掉了，这里只需要处理受控的ledger
        for (Map.Entry<String, BudgetLedger> entry : needUpdateSameDemBudgetLedgerMap.entrySet()) {
            String bizKey = entry.getKey(); // bizCode + "@" + bizItemCode
            BudgetLedger ledger = entry.getValue();
            boolean alreadyRolledBackByDiff = ledgerKeysAlreadyRolledBackByDiffDimension != null
                    && ledgerKeysAlreadyRolledBackByDiffDimension.contains(bizKey);

            // 若该 ledger 已在「维度不一致回滚」中处理过，跳过本次同一维度回滚，避免同一笔资金池被回滚两次（后续 processSameDimensionUpdate 会通过 merge 使用已回滚后的余额并扣减新金额）
            if (alreadyRolledBackByDiff) {
                log.info("========== processSameDimensionRollback - 该ledger已在维度不一致回滚中处理，跳过同一维度回滚: bizKey={} ==========", bizKey);
                continue;
            }

            // 判断是否不受控（需要跳过预算余额回滚，但仍保存数据到BUDGET_LEDGER表）
            if (isUncontrolledLedger(ledger, ehrCdToOrgCdExtMap, erpAcctCdToAcctCdExtMap)) {
                log.info("========== processSameDimensionRollback - 不受控明细跳过预算余额回滚: bizKey={}, morgCode={}, budgetSubjectCode={}, masterProjectCode={} ==========",
                        bizKey, ledger.getMorgCode(), ledger.getBudgetSubjectCode(), ledger.getMasterProjectCode());
                continue;
            }

            // 项目非 NAN 且 isInternal=1 时跳过回滚
            if (!"NAN".equals(ledger.getMasterProjectCode()) && "1".equals(ledger.getIsInternal())) {
                log.info("========== processSameDimensionRollback - 内部项目跳过回滚: bizKey={}, masterProjectCode={}, isInternal={} ==========",
                        bizKey, ledger.getMasterProjectCode(), ledger.getIsInternal());
                continue;
            }
            
            // 使用 helper service 计算需要回滚的季度
            BudgetQueryHelperService.RollbackQuartersResult rollbackResult = budgetQueryHelperService.calculateRollbackQuarters(ledger);
            List<String> quartersToRollback = rollbackResult.getQuartersToRollback();
            Map<String, BigDecimal> quarterRollbackAmountMap = rollbackResult.getQuarterRollbackAmountMap();
            BigDecimal amountAvailable = rollbackResult.getTotalAmount();
            
            if (quartersToRollback.isEmpty()) {
                log.info("========== processSameDimensionRollback - 无需回滚: bizKey={} ==========", bizKey);
                continue;
            }
            
            log.info("========== processSameDimensionRollback - 需要回滚的季度: bizKey={}, quarters={}, totalAmount={} ==========",
                    bizKey, quartersToRollback, amountAvailable);
            
            // 对每个需要回滚的季度进行处理
            for (String rollbackQuarter : quartersToRollback) {
                BigDecimal rollbackAmount = quarterRollbackAmountMap.get(rollbackQuarter);
                String bizKeyQuarter = bizKey + "@" + rollbackQuarter;
                
                BudgetBalance balance;
                if (StringUtils.isNotBlank(ledger.getPoolDimensionKey())) {
                    balance = budgetQueryHelperService.selectBudgetBalanceByPoolDimensionKey(ledger.getPoolDimensionKey(), rollbackQuarter);
                    needToUpdateSameDemBudgetBalanceMap.put(bizKeyQuarter, balance);
                } else {
                    balance = needToUpdateSameDemBudgetBalanceMap.get(bizKeyQuarter);
                }
                if (balance == null) {
                    // 检查回滚金额是否为0，如果为0则允许继续处理，跳过余额校验
                    if (rollbackAmount != null && rollbackAmount.compareTo(BigDecimal.ZERO) == 0) {
                        log.warn("========== bizKeyQuarter={} 在 needToUpdateSameDemBudgetBalanceMap 中找不到对应的 balance，但回滚金额为0，跳过余额校验，允许继续处理 ==========", bizKeyQuarter);
                        continue; // 跳过该季度，继续处理下一个
                    }
                    
                    log.error("========== bizKeyQuarter={} 在 needToUpdateSameDemBudgetBalanceMap 中找不到对应的 balance ==========", bizKeyQuarter);
                    String[] bizKeyParts = bizKey.split("@");
                    String managementOrg = bizKeyParts.length > 2 ? bizKeyParts[2] : "未知";
                    String budgetSubjectCode = bizKeyParts.length > 3 ? bizKeyParts[3] : "未知";
                    // 在异常消息中包含明细标识，格式：明细 [bizKey]，以便 tryExtractDetailError 能识别
                    throw new IllegalStateException(
                        String.format("明细 [%s] 预算余额不足,还请检查可用余额并重新修改单据信息或进行预算调整。维度信息:年度=%s,季度=%s,管理组织=%s,预算科目=%s, Available Budget Insufficient",
                                     bizKey, ledger.getYear(), rollbackQuarter, managementOrg, budgetSubjectCode)
                    );
                }
                
                Long poolId = balance.getPoolId();

                // 创建预算余额历史记录
                BudgetBalanceHistory balanceHistory = new BudgetBalanceHistory();
                BeanUtils.copyProperties(balance, balanceHistory);
                balanceHistory.setId(identifierGenerator.nextId(balanceHistory).longValue());
                balanceHistory.setBalanceId(balance.getId());
                balanceHistory.setDeleted(Boolean.FALSE);
                
                // 调用抽象方法，由子类实现具体的回滚逻辑（维度一致的回滚，可以扣减为负数，因为回滚完之后还会再冻结或占用）
                Map<String, List<BudgetLedger>> returnedRelatedBudgetLedgerMap = rollbackBalanceAmountForSameDimension(balance, balanceHistory, ledger, rollbackQuarter, rollbackAmount, poolId, updatedRelatedBudgetLedgerMap, aggregatedRelatedRollbackMap, appliedRelatedLedgerQuarterSet);
                
                // 合并返回的关联预算流水Map
                if (returnedRelatedBudgetLedgerMap != null) {
                    updatedRelatedBudgetLedgerMap.putAll(returnedRelatedBudgetLedgerMap);
                }
                
                balance.setVersion(ledger.getVersion());
                needToAddBudgetBalanceHistory.add(balanceHistory);
                
                log.info("========== processSameDimensionRollback - 释放后: poolId={}, quarter={}, amountFrozen={}, amountAvailable={} ==========",
                        poolId, rollbackQuarter, balance.getAmountFrozen(), balance.getAmountAvailable());
            }
        }
        
        return updatedRelatedBudgetLedgerMap;
    }
    
    /**
     * 回滚余额金额（释放冻结金额）- 维度不一致的回滚
     * 子类需要实现此方法，因为不同流程要回滚影响到的字段不一样
     * 注意：维度不一致的回滚，balance 不能扣减为负数
     *
     * @param balance 预算余额
     * @param balanceHistory 预算余额历史
     * @param ledger 预算流水
     * @param rollbackQuarter 回滚的季度
     * @param rollbackAmount 回滚金额
     * @param poolId 池ID
     */
    protected abstract void rollbackBalanceAmountForDiffDimension(BudgetBalance balance, BudgetBalanceHistory balanceHistory,
                                                                 BudgetLedger ledger, String rollbackQuarter, 
                                                                 BigDecimal rollbackAmount, Long poolId);
    
    /**
     * 回滚余额金额（释放冻结金额）- 维度一致的回滚
     * 子类需要实现此方法，因为不同流程要回滚影响到的字段不一样
     * 注意：维度一致的回滚，balance 可以扣减为负数，因为回滚完之后还会再冻结或占用
     *
     * @param balance 预算余额
     * @param balanceHistory 预算余额历史
     * @param ledger 预算流水
     * @param rollbackQuarter 回滚的季度
     * @param rollbackAmount 回滚金额
     * @param poolId 池ID
     * @param relatedBudgetLedgerMap 关联的预算流水Map，key为bizCode + "@" + bizItemCode，value为关联的BudgetLedger列表
     * @param aggregatedRelatedRollbackMap 按关联流水id+季度汇总后的回滚金额（同一合同被多条付款明细引用时只回滚一次总金额），可为 null
     * @param appliedRelatedLedgerQuarterSet 已对关联流水施加过回滚的 (relatedLedgerId + "@" + quarter) 集合，用于避免重复施加，可为 null
     * @return 关联的预算流水Map，key为bizCode + "@" + bizItemCode，value为关联的BudgetLedger列表
     */
    protected abstract Map<String, List<BudgetLedger>> rollbackBalanceAmountForSameDimension(BudgetBalance balance, BudgetBalanceHistory balanceHistory,
                                                                 BudgetLedger ledger, String rollbackQuarter, 
                                                                 BigDecimal rollbackAmount, Long poolId,
                                                                 Map<String, List<BudgetLedger>> relatedBudgetLedgerMap,
                                                                 Map<Long, Map<String, BigDecimal>> aggregatedRelatedRollbackMap,
                                                                 Set<String> appliedRelatedLedgerQuarterSet);
    
    /**
     * 获取当前单据本季度对各关联流水的回滚金额（用于按关联流水id汇总，避免同一关联流水被多条明细重复回滚）。
     * 默认返回空；Claim/Contract 子类按 BUDGET_LEDGER_SELF_R 或比例实现。
     */
    protected Map<Long, BigDecimal> getRollbackAmountsForRelatedLedgers(BudgetLedger ledger, String rollbackQuarter,
                                                                        BigDecimal rollbackAmount,
                                                                        Map<String, List<BudgetLedger>> relatedBudgetLedgerMap) {
        return Collections.emptyMap();
    }
    
    /**
     * 处理维度一致的数据（更新逻辑）
     *
     * @param needUpdateSameDemBudgetLedgerMap 需要更新的预算流水Map（维度一致）
     * @param needToUpdateSameDemBudgetQuotaMap 需要更新的预算额度Map
     * @param needToUpdateSameDemBudgetBalanceMap 需要更新的预算余额Map
     * @param needToAddBudgetQuotaHistory 需要新增的预算额度历史列表
     * @param needToAddBudgetBalanceHistory 需要新增的预算余额历史列表
     * @param availableBudgetRatioMap 可用预算数值Map（输出参数）
     * @param updatedRelatedBudgetLedgerMap 更新后的关联预算流水Map，key为bizCode + "@" + bizItemCode，value为关联的BudgetLedger列表
     */
    protected void processSameDimensionUpdate(Map<String, BudgetLedger> needUpdateSameDemBudgetLedgerMap,
                                            Map<String, BudgetQuota> needToUpdateSameDemBudgetQuotaMap,
                                            Map<String, BudgetBalance> needToUpdateSameDemBudgetBalanceMap,
                                            List<BudgetQuotaHistory> needToAddBudgetQuotaHistory,
                                            List<BudgetBalanceHistory> needToAddBudgetBalanceHistory,
                                            Map<String, DetailNumberVo> availableBudgetRatioMap,
                                            Map<String, List<BudgetLedger>> updatedRelatedBudgetLedgerMap,
                                            Map<String, String> ehrCdToOrgCdMap,
                                            Map<String, List<String>> ehrCdToOrgCdExtMap,
                                            Map<String, String> erpAcctCdToAcctCdMap,
                                            Map<String, List<String>> erpAcctCdToAcctCdExtMap,
                                            Map<String, List<String>> prjCdToRelatedPrjCdExtMap,
                                            Map<String, BudgetPoolDemR> needToAddPoolDemRMap,
                                            Map<String, String> erpAssetTypeToMemberCdMap,
                                            Map<String, BigDecimal> relatedLedgerDeductionAmountMap,
                                            Map<String, BudgetBalance> needToUpdateDiffDemBudgetBalanceMapForMerge) {
        log.info("========== processSameDimensionUpdate 开始，需要处理的 ledger keys: {} ==========", needUpdateSameDemBudgetLedgerMap.keySet());

        // 检查是否需要查询并执行查询（如果需要）
        queryQuotaAndBalanceIfNeeded(needUpdateSameDemBudgetLedgerMap, needToUpdateSameDemBudgetQuotaMap, 
                needToUpdateSameDemBudgetBalanceMap, updatedRelatedBudgetLedgerMap, 
                ehrCdToOrgCdMap, ehrCdToOrgCdExtMap, erpAcctCdToAcctCdMap, 
                erpAcctCdToAcctCdExtMap, prjCdToRelatedPrjCdExtMap, erpAssetTypeToMemberCdMap);
        // 若本批次是维度不一致后新增的 ledger，合并回滚后的 balance（同池同季度），避免用 DB 旧值再扣一次导致 amountFrozen 等变负
        // 仅合并本批次 ledger 对应的 key，避免覆盖同请求中「维度一致更新」已扣减过的 balance
        if (needToUpdateDiffDemBudgetBalanceMapForMerge != null && !needToUpdateDiffDemBudgetBalanceMapForMerge.isEmpty()) {
            mergeRollbackBalanceIntoSameMap(needToUpdateSameDemBudgetBalanceMap, needToUpdateDiffDemBudgetBalanceMapForMerge, needUpdateSameDemBudgetLedgerMap.keySet());
        }
        
        // needToAddPoolDemRMap 作为输入参数，如果为 null 则创建新的
        if (needToAddPoolDemRMap == null) {
            needToAddPoolDemRMap = new HashMap<>();
        }
        
        // 过滤掉不受控的ledger，只查询受控的ledger的quota和balance
        Map<String, BudgetLedger> controlledLedgerMap = new HashMap<>();
        for (Map.Entry<String, BudgetLedger> entry : needUpdateSameDemBudgetLedgerMap.entrySet()) {
            BudgetLedger ledger = entry.getValue();
            // 跳过不受控的ledger（不受控明细会跳过预算余额查询和更新）
            if (!isUncontrolledLedger(ledger, ehrCdToOrgCdExtMap, erpAcctCdToAcctCdExtMap)) {
                controlledLedgerMap.put(entry.getKey(), ledger);
            }
        }
        
        // 调用 queryQuotaAndBalanceByAllQuartersAllDem 查询所有季度的 quota 和 balance（支持扩展映射，只查询受控的ledger）
        BudgetQueryHelperService.BudgetQuotaBalanceSimpleResult queryResult = 
                budgetQueryHelperService.queryQuotaAndBalanceByAllQuartersAllDem(
                        controlledLedgerMap,
                        needToUpdateSameDemBudgetQuotaMap,
                        needToUpdateSameDemBudgetBalanceMap,
                        ehrCdToOrgCdMap,
                        ehrCdToOrgCdExtMap,
                        erpAcctCdToAcctCdMap,
                        erpAcctCdToAcctCdExtMap,
                        prjCdToRelatedPrjCdExtMap,
                        erpAssetTypeToMemberCdMap);
        
        // 检查是否有错误
        if (queryResult.hasError()) {
            throw new IllegalStateException(queryResult.getErrorMessage());
        }
        
        // 获取返回的 quotaMap 和 balanceMap 备用
        Map<String, List<BudgetQuota>> quotaMap = queryResult.getQuotaMap();
        Map<String, List<BudgetBalance>> balanceMap = queryResult.getBalanceMap();
        BALANCE_QUERY_SKIP_REASONS_THREAD_LOCAL.set(queryResult.getDetailErrorMessages());
        try {
        // 在扣减前，先对所有明细进行统一校验，确保总金额不超过可用余额
        // 这样可以避免顺序处理时，前一个明细扣减后导致后续明细校验时余额不足的问题
        // 按季度分组，对每个季度的明细进行统一校验
        Map<String, List<Map.Entry<String, BudgetLedger>>> quarterLedgerMap = new HashMap<>();
        for (Map.Entry<String, BudgetLedger> entry : needUpdateSameDemBudgetLedgerMap.entrySet()) {
            BudgetLedger ledger = entry.getValue();
            // 跳过不受控和内部项目的明细
            if (!"NAN".equals(ledger.getMasterProjectCode()) && "1".equals(ledger.getIsInternal())) {
                continue;
            }
            if (isUncontrolledLedger(ledger, ehrCdToOrgCdExtMap, erpAcctCdToAcctCdExtMap)) {
                continue;
            }
            String monthForQuarter = ledger.getMonth();
            if (CLAIM_BIZ_TYPE.equals(ledger.getBizType())) {
                if (CLAIM_ACTUAL_DATE_SOURCE_FLAG == 0 && StringUtils.isNotBlank(ledger.getActualMonth())) {
                    monthForQuarter = ledger.getActualMonth();
                }
            }
            String quarter = convertMonthToQuarter(monthForQuarter);
            quarterLedgerMap.computeIfAbsent(quarter, k -> new ArrayList<>()).add(entry);
        }
        
        // 对每个季度的明细进行统一校验
        for (Map.Entry<String, List<Map.Entry<String, BudgetLedger>>> quarterEntry : quarterLedgerMap.entrySet()) {
            String quarter = quarterEntry.getKey();
            List<Map.Entry<String, BudgetLedger>> ledgerEntries = quarterEntry.getValue();
            
            // 识别共享同一个控制层级资金池的明细（通过比较balanceMap中的poolId集合）
            Map<Set<Long>, List<Map.Entry<String, BudgetLedger>>> poolGroupMap = new HashMap<>();
            for (Map.Entry<String, BudgetLedger> entry : ledgerEntries) {
                String bizKey = entry.getKey();
                BudgetLedger ledger = entry.getValue();
                String bizKeyQuarter = bizKey + "@" + quarter;
                List<BudgetBalance> balanceList = balanceMap.get(bizKeyQuarter);
                if (CollectionUtils.isEmpty(balanceList)) {
                    // 如果控制层级没有数据，跳过统一校验（会在后续单个处理时处理）
                    continue;
                }
                // 提取poolId集合作为分组key
                Set<Long> poolIdSet = balanceList.stream()
                        .map(BudgetBalance::getPoolId)
                        .collect(Collectors.toSet());
                poolGroupMap.computeIfAbsent(poolIdSet, k -> new ArrayList<>()).add(entry);
            }
            
            // 对每个共享同一资金池的明细组进行统一校验
            for (Map.Entry<Set<Long>, List<Map.Entry<String, BudgetLedger>>> poolGroup : poolGroupMap.entrySet()) {
                List<Map.Entry<String, BudgetLedger>> groupLedgers = poolGroup.getValue();
                if (groupLedgers.size() <= 1) {
                    // 只有一个明细，不需要统一校验
                    continue;
                }
                
                // 计算该组所有明细的总金额
                BigDecimal totalAmount = BigDecimal.ZERO;
                for (Map.Entry<String, BudgetLedger> entry : groupLedgers) {
                    BudgetLedger ledger = entry.getValue();
                    BigDecimal amount = ledger.getAmount() == null ? BigDecimal.ZERO : ledger.getAmount();
                    totalAmount = totalAmount.add(amount);
                }
                
                // 使用第一个明细的bizKeyQuarter来获取balanceList（它们共享同一个控制层级）
                String firstBizKey = groupLedgers.get(0).getKey();
                BudgetLedger firstLedger = groupLedgers.get(0).getValue();
                String firstBizKeyQuarter = firstBizKey + "@" + quarter;
                List<BudgetBalance> balanceList = balanceMap.get(firstBizKeyQuarter);
                
                BigDecimal totalAmountAvailable = null;
                BigDecimal currentAmountOperated = null;
                
                // 付款单（CLAIM）或合同单（CONTRACT）且有关联单据时：应校验「金额 ≤ 关联单据的可用额」，不使用资金池余额
                // 按关联流水 id 去重后再汇总：同一关联流水可能被兜底逻辑匹配到多个付款明细（如申请单只有 1 条流水但报销单有多个科目），
                // 若不去重会重复累加同一流水的可用额，导致「关联可用额」虚高或误判预算不足
                if (("CLAIM".equals(firstLedger.getBizType()) || "CONTRACT".equals(firstLedger.getBizType()))
                        && updatedRelatedBudgetLedgerMap != null && !updatedRelatedBudgetLedgerMap.isEmpty()) {
                    Set<Long> relatedLedgerIdsSeen = new HashSet<>();
                    BigDecimal relatedTotalAmountAvailable = BigDecimal.ZERO;
                    for (Map.Entry<String, BudgetLedger> entry : groupLedgers) {
                        String bizKey = entry.getKey();
                        List<BudgetLedger> relatedLedgers = updatedRelatedBudgetLedgerMap.get(bizKey);
                        if (!CollectionUtils.isEmpty(relatedLedgers)) {
                            for (BudgetLedger relatedLedger : relatedLedgers) {
                                if (relatedLedger.getId() != null && relatedLedgerIdsSeen.add(relatedLedger.getId())) {
                                    BigDecimal av = relatedLedger.getAmountAvailable() == null ? BigDecimal.ZERO : relatedLedger.getAmountAvailable();
                                    relatedTotalAmountAvailable = relatedTotalAmountAvailable.add(av);
                                }
                            }
                        }
                    }
                    boolean claimFwUnified = "CLAIM".equals(firstLedger.getBizType())
                            && isClaimFrameworkContractRelated(updatedRelatedBudgetLedgerMap, firstBizKey);
                    if (claimFwUnified && !CollectionUtils.isEmpty(balanceList)) {
                        BigDecimal poolCap = calculateTotalAmountAvailable(firstBizKey, quarter, balanceMap, balanceList, "CLAIM");
                        totalAmountAvailable = relatedTotalAmountAvailable.add(poolCap);
                        currentAmountOperated = BigDecimal.ZERO;
                        log.info("========== processSameDimensionUpdate - 统一校验(框架协议付款=合同可用+资金池): 总金额={}, 关联可用额={}, 资金池上限={}, 合计上限={} ==========",
                                totalAmount, relatedTotalAmountAvailable, poolCap, totalAmountAvailable);
                    } else if (relatedTotalAmountAvailable.compareTo(BigDecimal.ZERO) > 0) {
                        totalAmountAvailable = relatedTotalAmountAvailable;
                        currentAmountOperated = BigDecimal.ZERO;
                        log.info("========== processSameDimensionUpdate - 统一校验使用关联单据可用额（{}）: 总金额={}, 关联可用额={} ==========",
                                firstLedger.getBizType(), totalAmount, totalAmountAvailable);
                    }
                }
                
                if (totalAmountAvailable == null && !CollectionUtils.isEmpty(balanceList)) {
                    // 计算从q1到当前季度的累积可用余额（与单个明细校验逻辑一致）
                    totalAmountAvailable = calculateTotalAmountAvailable(firstBizKey, quarter, balanceMap, balanceList, firstLedger.getBizType());
                    // 计算当前已操作金额
                    currentAmountOperated = getCurrentAmountOperated(balanceList);
                }
                
                if (totalAmountAvailable != null && currentAmountOperated != null) {
                    // 统一校验：使用与单个明细校验相同的逻辑（validateBudget方法中的逻辑）
                    // 申请单/合同单：使用复杂的校验逻辑
                    BigDecimal operatedAfter = currentAmountOperated.add(totalAmount);
                    boolean budgetInsufficient = false;
                    
                    if ("CLAIM".equals(firstLedger.getBizType())) {
                        // 付款单：直接判断 totalAmountAvailable >= totalAmount
                        if (totalAmountAvailable.compareTo(totalAmount) < 0) {
                            budgetInsufficient = true;
                        }
                    } else {
                        // 申请单/合同单：使用复杂的校验逻辑
                        if (currentAmountOperated.compareTo(BigDecimal.ZERO) >= 0) {
                            // currentAmountOperated >= 0，判断 totalAmountAvailable >= totalAmount
                            if (totalAmountAvailable.compareTo(totalAmount) < 0) {
                                budgetInsufficient = true;
                            }
                        } else if (operatedAfter.compareTo(BigDecimal.ZERO) >= 0) {
                            // currentAmountOperated < 0 但 operatedAfter >= 0
                            // 判断 totalAmountAvailable >= operatedAfter
                            if (totalAmountAvailable.compareTo(operatedAfter) < 0) {
                                budgetInsufficient = true;
                            }
                        } else {
                            // operatedAfter < 0
                            // 判断 totalAmountAvailable + operatedAfter >= 0
                            if (totalAmountAvailable.add(operatedAfter).compareTo(BigDecimal.ZERO) < 0) {
                                budgetInsufficient = true;
                            }
                        }
                    }
                    
                    if (budgetInsufficient && !isSkipBudgetValidation()) {
                        // 预算不足，抛出异常（配置 budget.validation.mode=0 时跳过校验）
                        // 收集所有相关明细的标识，用于错误提示
                        List<String> allBizItemCodes = new ArrayList<>();
                        for (Map.Entry<String, BudgetLedger> entry : groupLedgers) {
                            allBizItemCodes.add(entry.getValue().getBizItemCode());
                        }
                        String firstBizItemCode = firstLedger.getBizItemCode();
                        String allBizItemCodesStr = String.join(", ", allBizItemCodes);
                        // 提取维度信息用于错误提示
                        String dimensionInfo = extractDimensionInfoFromLedger(firstLedger);
                        String errorMsg = String.format("明细 [%s] 等 %d 个明细共享同一资金池，总金额 %s 超过可用余额 %s（已操作 %s，操作后 %s）。维度信息: %s。相关明细: [%s]",
                                firstBizItemCode, groupLedgers.size(), totalAmount, totalAmountAvailable, currentAmountOperated, operatedAfter, dimensionInfo, allBizItemCodesStr);
                        log.error("========== processSameDimensionUpdate - 统一校验失败: {} ==========", errorMsg);
                        throw new IllegalStateException(errorMsg);
                    }
                    
                    log.info("========== processSameDimensionUpdate - 统一校验通过 Verification Passed: 季度={}, 明细数量={}, 总金额={}, 可用余额={}, 已操作={}, 操作后={} ==========",
                            quarter, groupLedgers.size(), totalAmount, totalAmountAvailable, currentAmountOperated, operatedAfter);
                }
            }
        }
        
        // 扣减新的金额：遍历 needUpdateSameDemBudgetLedgerMap
        // 注意：relatedLedgerDeductionAmountMap 作为输入参数传入，在 performMultiQuarterDeduction 中填充
        for (Map.Entry<String, BudgetLedger> entry : needUpdateSameDemBudgetLedgerMap.entrySet()) {
            String bizKey = entry.getKey(); // bizCode + "@" + bizItemCode
            BudgetLedger ledger = entry.getValue();

            // 项目非 NAN 且 isInternal=1 时跳过处理
            if (!"NAN".equals(ledger.getMasterProjectCode()) && "1".equals(ledger.getIsInternal())) {
                log.info("========== processSameDimensionUpdate - 内部项目跳过处理: bizKey={}, masterProjectCode={}, isInternal={} ==========",
                        bizKey, ledger.getMasterProjectCode(), ledger.getIsInternal());
                continue;
            }
            
            // 判断是否不受控（需要跳过预算余额更新，但仍保存数据到BUDGET_LEDGER表）
            if (isUncontrolledLedger(ledger, ehrCdToOrgCdExtMap, erpAcctCdToAcctCdExtMap)) {
                log.info("========== processSameDimensionUpdate - 不受控明细跳过预算余额更新: bizKey={}, morgCode={}, budgetSubjectCode={}, masterProjectCode={} ==========",
                        bizKey, ledger.getMorgCode(), ledger.getBudgetSubjectCode(), ledger.getMasterProjectCode());
                continue;
            }
            
            // 判断金额是否小于等于0（需要跳过预算校验和扣减，但仍保存数据到BUDGET_LEDGER表）
            BigDecimal amount = ledger.getAmount() == null ? BigDecimal.ZERO : ledger.getAmount();
            if (amount.compareTo(BigDecimal.ZERO) <= 0) {
                log.info("========== processSameDimensionUpdate - 金额小于等于0，跳过预算校验和扣减，但仍保存到BUDGET_LEDGER表: bizKey={}, amount={} ==========",
                        bizKey, amount);
                // 为金额 <= 0 的明细设置默认的返回数据（所有预算相关数值设为0或null）
                DetailNumberVo detailNumberVo = new DetailNumberVo();
                detailNumberVo.setAmountQuota(BigDecimal.ZERO);
                detailNumberVo.setAmountFrozen(BigDecimal.ZERO);
                detailNumberVo.setAmountActual(BigDecimal.ZERO);
                detailNumberVo.setAmountAvailable(BigDecimal.ZERO);
                detailNumberVo.setAvailableBudgetRatio(BigDecimal.ZERO);
                String bizItemCode = ledger.getBizItemCode();
                availableBudgetRatioMap.put(bizItemCode, detailNumberVo);
                log.info("========== processSameDimensionUpdate - 金额小于等于0的明细，设置默认返回数据: bizItemCode={} ==========", bizItemCode);
                continue;
            }
            
            // 转换月份为季度：CLAIM 且开关为 0 时优先 actualMonth，否则使用 month
            String monthForQuarter = ledger.getMonth();
            if (CLAIM_BIZ_TYPE.equals(ledger.getBizType())) {
                if (CLAIM_ACTUAL_DATE_SOURCE_FLAG == 0 && StringUtils.isNotBlank(ledger.getActualMonth())) {
                    monthForQuarter = ledger.getActualMonth();
                }
            }
            String newQuarter = convertMonthToQuarter(monthForQuarter);
            String bizKeyQuarter = bizKey + "@" + newQuarter;
            
            // 从 Map 中获取对应的 balance 和 quota
            BudgetBalance balance = needToUpdateSameDemBudgetBalanceMap.get(bizKeyQuarter);
            if (balance == null) {
                // 如果自己维度的数据不存在，检查控制层级是否有数据，如果有则构造自己维度的数据
                List<BudgetBalance> controlLevelBalances = balanceMap.get(bizKeyQuarter);
                if (CollectionUtils.isEmpty(controlLevelBalances)) {
                    // 检查金额是否为0，如果为0则允许继续处理，跳过余额校验
                    // 注意：这里使用之前已定义的 amount 变量（第2036行已定义）
                    if (amount != null && amount.compareTo(BigDecimal.ZERO) == 0) {
                        log.warn("========== bizKeyQuarter={} 在控制层级的 balanceMap 中也找不到对应的 balance，但金额为0，跳过余额校验，允许继续处理 ==========", bizKeyQuarter);
                        continue; // 跳过该明细，继续处理下一个
                    }
                    // 已配置 budget.validation.mode=0 时，绕过预算校验：控制层级无 balance 也允许继续处理
                    if (isSkipBudgetValidation()) {
                        log.warn("========== bizKeyQuarter={} 在控制层级的 balanceMap 中也找不到对应的 balance，但已配置跳过预算校验(budget.validation.mode=0)，跳过余额校验，允许继续处理 ==========", bizKeyQuarter);
                        continue;
                    }
                    // 当该明细有关联预算流水时，不强制要求 balanceMap 必须有这条，仅使用关联流水可用额做校验与扣减
                    List<BudgetLedger> relatedLedgersForDetail = updatedRelatedBudgetLedgerMap != null ? updatedRelatedBudgetLedgerMap.get(bizKey) : null;
                    if (!CollectionUtils.isEmpty(relatedLedgersForDetail)) {
                        log.info("========== bizKeyQuarter={} 在控制层级的 balanceMap 中无对应 balance，但有关联预算流水（{} 条），将仅使用关联流水可用额进行校验与扣减 ==========", bizKeyQuarter, relatedLedgersForDetail.size());
                        BigDecimal totalAmountToOperateRelatedOnly = amount;
                        Map<String, BigDecimal> compensationMapRelatedOnly = new HashMap<>();
                        validateBudgetSufficient(bizKey, ledger, balanceMap, updatedRelatedBudgetLedgerMap, totalAmountToOperateRelatedOnly, compensationMapRelatedOnly);
                        Map<String, BigDecimal> quarterOperateAmountMapRelatedOnly = new HashMap<>();
                        performMultiQuarterDeduction(ledger.getId(), bizKey, newQuarter, totalAmountToOperateRelatedOnly, balanceMap, needToUpdateSameDemBudgetBalanceMap, quarterOperateAmountMapRelatedOnly, updatedRelatedBudgetLedgerMap, relatedLedgerDeductionAmountMap);
                        BigDecimal q1Amt = quarterOperateAmountMapRelatedOnly.getOrDefault("q1", BigDecimal.ZERO);
                        BigDecimal q2Amt = quarterOperateAmountMapRelatedOnly.getOrDefault("q2", BigDecimal.ZERO);
                        BigDecimal q3Amt = quarterOperateAmountMapRelatedOnly.getOrDefault("q3", BigDecimal.ZERO);
                        BigDecimal q4Amt = quarterOperateAmountMapRelatedOnly.getOrDefault("q4", BigDecimal.ZERO);
                        setQuarterAmountToLedgerOverwrite(ledger, q1Amt, q2Amt, q3Amt, q4Amt);
                        BigDecimal relatedAmountAvailable = BigDecimal.ZERO;
                        for (BudgetLedger rl : relatedLedgersForDetail) {
                            relatedAmountAvailable = relatedAmountAvailable.add(rl.getAmountAvailable() == null ? BigDecimal.ZERO : rl.getAmountAvailable());
                        }
                        DetailNumberVo detailNumberVo = new DetailNumberVo();
                        detailNumberVo.setAmountAvailable(relatedAmountAvailable);
                        detailNumberVo.setAvailableBudgetRatio(BigDecimal.ZERO);
                        availableBudgetRatioMap.put(ledger.getBizItemCode(), detailNumberVo);
                        continue;
                    }
                    
                    log.error("========== bizKeyQuarter={} 在控制层级的 balanceMap 中也找不到对应的 balance ==========", bizKeyQuarter);
                    // 获取映射后的 erpAssetType 用于错误提示
                    String originalErpAssetType = ledger.getErpAssetType();
                    String masterProjectCode = ledger.getMasterProjectCode();
                    String mappedErpAssetType = "NAN";
                    if ("NAN".equals(masterProjectCode) && StringUtils.isNotBlank(originalErpAssetType) && !"NAN".equals(originalErpAssetType)) {
                        // 尝试获取映射后的值（如果映射表可用）
                        if (erpAssetTypeToMemberCdMap != null && !erpAssetTypeToMemberCdMap.isEmpty()) {
                            if (originalErpAssetType.startsWith("1") || originalErpAssetType.startsWith("M")) {
                                mappedErpAssetType = erpAssetTypeToMemberCdMap.getOrDefault(originalErpAssetType, originalErpAssetType);
                            } else {
                                mappedErpAssetType = originalErpAssetType;
                            }
                        } else {
                            mappedErpAssetType = originalErpAssetType;
                        }
                    }
                    // 在异常消息中包含明细标识，格式：明细 [bizKey]，以便 tryExtractDetailError 能识别
                    throw new IllegalStateException(
                        String.format("明细 [%s] 预算余额不足,还请检查可用余额并重新修改单据信息或进行预算调整。维度信息:年度=%s,季度=%s,管理组织=%s,预算科目=%s,资产类型=%s(原始值=%s), Available Budget Insufficient",
                                     bizKey, ledger.getYear(), newQuarter, ledger.getMorgCode(), ledger.getBudgetSubjectCode(), mappedErpAssetType, originalErpAssetType)
                    );
                }
                
                // 控制层级有数据：优先从 AllDem 的 balanceMap/quotaMap 复用同池同季度的 Balance/Quota，避免多组织同控制层级时重复创建导致 BudgetBalance 条数翻倍
                String[] allQuarters = {"q1", "q2", "q3", "q4"};
                boolean filledFromControlLevel = false;
                for (String q : allQuarters) {
                    String key = bizKey + "@" + q;
                    if (!needToUpdateSameDemBudgetBalanceMap.containsKey(key)) {
                        List<BudgetBalance> bl = balanceMap != null ? balanceMap.get(key) : null;
                        if (!CollectionUtils.isEmpty(bl)) {
                            needToUpdateSameDemBudgetBalanceMap.put(key, bl.get(0));
                            filledFromControlLevel = true;
                        }
                    }
                    if (needToUpdateSameDemBudgetQuotaMap != null && !needToUpdateSameDemBudgetQuotaMap.containsKey(key)) {
                        List<BudgetQuota> ql = quotaMap != null ? quotaMap.get(key) : null;
                        if (!CollectionUtils.isEmpty(ql)) {
                            needToUpdateSameDemBudgetQuotaMap.put(key, ql.get(0));
                        }
                    }
                }
                if (filledFromControlLevel) {
                    balance = needToUpdateSameDemBudgetBalanceMap.get(bizKeyQuarter);
                }
                if (balance == null) {
                    // 控制层级有数据但无法复用（如部分季度缺失），才构造自己维度的数据
                    log.warn("========== bizKeyQuarter={} 在 needToUpdateSameDemBudgetBalanceMap 中找不到对应的 balance，但控制层级有数据，将构造自己维度的数据（所有4个季度） ==========", bizKeyQuarter);
                
                // 获取自己维度的维度信息
                // 对于带项目的单据，如果映射表为空，直接使用传入的 EHR 组织编码
                String masterProjectCode = ledger.getMasterProjectCode();
                boolean isProjectQuery = !"NAN".equals(masterProjectCode);
                String selfMorgCode;
                if (isProjectQuery) {
                    // 带项目的单据：如果映射表为空，直接使用 EHR 组织编码
                    if (ehrCdToOrgCdMap == null || ehrCdToOrgCdMap.isEmpty()) {
                        selfMorgCode = ledger.getMorgCode();
                        log.info("========== 带项目的单据，EHR组织编码映射表为空，创建 BudgetPoolDemR 时直接使用传入的EHR组织编码: {} ==========", selfMorgCode);
                    } else {
                        selfMorgCode = ehrCdToOrgCdMap.get(ledger.getMorgCode());
                        // 如果映射表中没有找到，也直接使用 EHR 组织编码
                        if (StringUtils.isBlank(selfMorgCode)) {
                            selfMorgCode = ledger.getMorgCode();
                            log.info("========== 带项目的单据，EHR组织编码 {} 未找到映射，创建 BudgetPoolDemR 时直接使用传入的EHR组织编码 ==========", ledger.getMorgCode());
                        }
                    }
                } else {
                    // 非项目查询：必须要有映射
                    selfMorgCode = ehrCdToOrgCdMap != null ? ehrCdToOrgCdMap.get(ledger.getMorgCode()) : null;
                }
                // 对于带项目的单据，如果科目映射没有找到，直接使用传入的科目编码
                String selfAcctCd;
                if ("NAN-NAN".equals(ledger.getBudgetSubjectCode())) {
                    selfAcctCd = "NAN-NAN";
                } else if (isProjectQuery) {
                    // 带项目的单据：如果映射表为空或未找到，直接使用传入的科目编码
                    if (erpAcctCdToAcctCdMap == null || erpAcctCdToAcctCdMap.isEmpty()) {
                        selfAcctCd = ledger.getBudgetSubjectCode();
                        log.info("========== 带项目的单据，科目编码映射表为空，创建 BudgetPoolDemR 时直接使用传入的科目编码: {} ==========", selfAcctCd);
                    } else {
                        selfAcctCd = erpAcctCdToAcctCdMap.get(ledger.getBudgetSubjectCode());
                        // 如果映射表中没有找到，也直接使用传入的科目编码
                        if (StringUtils.isBlank(selfAcctCd)) {
                            selfAcctCd = ledger.getBudgetSubjectCode();
                            log.info("========== 带项目的单据，科目编码 {} 未找到映射，创建 BudgetPoolDemR 时直接使用传入的科目编码 ==========", ledger.getBudgetSubjectCode());
                        }
                    }
                } else {
                    // 非项目查询：必须要有映射
                    selfAcctCd = erpAcctCdToAcctCdMap != null ? erpAcctCdToAcctCdMap.get(ledger.getBudgetSubjectCode()) : null;
                }
                
                if (StringUtils.isBlank(selfMorgCode) || StringUtils.isBlank(selfAcctCd)) {
                    log.error("========== 无法获取自己维度的映射信息: selfMorgCode={}, selfAcctCd={} ==========", selfMorgCode, selfAcctCd);
                    // 在异常消息中包含明细标识，格式：明细 [bizKey]，以便 tryExtractDetailError 能识别
                    throw new IllegalStateException(
                        String.format("明细 [%s] 无法获取维度映射信息,还请联系管理员维护映射。[%s][%s][%s] Invalid Dimension",
                                     bizKey, ledger.getMorgCode(), ledger.getBudgetSubjectCode(), ledger.getMasterProjectCode())
                    );
                }
                
                // 为所有4个季度构造数据
                String[] quarters = {"q1", "q2", "q3", "q4"};
                String year = ledger.getYear();
                // masterProjectCode 已在上面声明，这里不需要重复声明
                String originalErpAssetType = ledger.getErpAssetType();
                // 映射 erpAssetType：如果以 "1" 或 "M" 开头，需要通过映射表映射
                // 注意：带项目时不需要映射 erpAssetType
                String erpAssetType = mapErpAssetType(originalErpAssetType, masterProjectCode, erpAssetTypeToMemberCdMap, 
                        "创建 BudgetPoolDemR 时，明细 [" + bizKey + "]");
                // 当项目是 NAN 时，isInternal 必须是 1
                String isInternal = "NAN".equals(masterProjectCode) ? "1" : ledger.getIsInternal();
                
                for (String quarter : quarters) {
                    String bizKeyQuarterForAll = bizKey + "@" + quarter;
                    
                    // 如果已经存在，跳过
                    if (needToUpdateSameDemBudgetBalanceMap.containsKey(bizKeyQuarterForAll)) {
                        continue;
                    }
                    
                    // 创建 BudgetPoolDemR
                    BudgetPoolDemR poolDemR = new BudgetPoolDemR();
                    Long poolId = identifierGenerator.nextId(poolDemR).longValue();
                    poolDemR.setId(poolId);
                    poolDemR.setYear(year);
                    poolDemR.setQuarter(quarter);
                    poolDemR.setIsInternal(isInternal);
                    poolDemR.setMorgCode(selfMorgCode);
                    poolDemR.setBudgetSubjectCode(selfAcctCd);
                    poolDemR.setMasterProjectCode(masterProjectCode);
                    poolDemR.setErpAssetType(erpAssetType);
                    poolDemR.setDeleted(Boolean.FALSE);
                    poolDemR.setCreateTime(LocalDateTime.now());
                    poolDemR.setUpdateTime(LocalDateTime.now());
                    
                    // 创建 BudgetQuota
                    BudgetQuota quota = new BudgetQuota();
                    Long quotaId = identifierGenerator.nextId(quota).longValue();
                    quota.setId(quotaId);
                    quota.setPoolId(poolId);
                    // 设置维度字段（从 poolDemR 复制）
                    quota.setMorgCode(poolDemR.getMorgCode());
                    quota.setProjectCode(poolDemR.getMasterProjectCode());
                    quota.setErpAssetType(poolDemR.getErpAssetType());
                    quota.setIsInternal(poolDemR.getIsInternal());
                    // 设置其他字段
                    quota.setYear(year);
                    quota.setQuarter(quarter);
                    quota.setCurrency(ledger.getCurrency());
                    quota.setVersion(ledger.getVersion());
                    quota.setAmountTotal(BigDecimal.ZERO);
                    quota.setAmountTotalVchanged(BigDecimal.ZERO);
                    quota.setAmountAdj(BigDecimal.ZERO);
                    quota.setAmountPay(BigDecimal.ZERO);
                    quota.setAmountPayVchanged(BigDecimal.ZERO);
                    // 补齐非金额维度字段：
                    // poolDemR.getBudgetSubjectCode() 的格式通常为 "CUSTOM_CODE-ACCOUNT_SUBJECT_CODE"（如 CU1090402-A0103...）
                    // 需要拆分后分别写入，避免 CUSTOM_CODE / ACCOUNT_SUBJECT_CODE 落库为 NULL
                    String budgetSubjectCode = poolDemR.getBudgetSubjectCode();
                    if (StringUtils.isBlank(budgetSubjectCode) || "NAN-NAN".equals(budgetSubjectCode)) {
                        quota.setCustomCode("NAN");
                        quota.setAccountSubjectCode("NAN");
                    } else {
                        int dashIdx = budgetSubjectCode.indexOf('-');
                        if (dashIdx > 0 && dashIdx < budgetSubjectCode.length() - 1) {
                            quota.setCustomCode(budgetSubjectCode.substring(0, dashIdx));
                            quota.setAccountSubjectCode(budgetSubjectCode.substring(dashIdx + 1));
                        } else {
                            // 兜底：未按预期包含 '-'，则 CUSTOM_CODE 置 NAN，ACCOUNT_SUBJECT_CODE 使用原值
                            quota.setCustomCode("NAN");
                            quota.setAccountSubjectCode(budgetSubjectCode);
                        }
                    }
                    quota.setDeleted(Boolean.FALSE);
                    quota.setCreateTime(LocalDateTime.now());
                    quota.setUpdateTime(LocalDateTime.now());
                    
                    // 创建 BudgetBalance
                    BudgetBalance newBalance = new BudgetBalance();
                    Long balanceId = identifierGenerator.nextId(newBalance).longValue();
                    newBalance.setId(balanceId);
                    newBalance.setPoolId(poolId);
                    newBalance.setQuotaId(quotaId);
                    // 设置维度字段（从 poolDemR 复制）
                    newBalance.setMorgCode(poolDemR.getMorgCode());
                    newBalance.setProjectCode(poolDemR.getMasterProjectCode());
                    newBalance.setErpAssetType(poolDemR.getErpAssetType());
                    newBalance.setIsInternal(poolDemR.getIsInternal());
                    // 补齐非金额维度字段：与 quota 保持一致，避免 BUDGET_BALANCE 出现 NULL
                    newBalance.setCustomCode(quota.getCustomCode());
                    newBalance.setAccountSubjectCode(quota.getAccountSubjectCode());
                    // 设置其他字段
                    newBalance.setYear(year);
                    newBalance.setQuarter(quarter);
                    newBalance.setCurrency(ledger.getCurrency());
                    newBalance.setVersion(ledger.getVersion());
                    newBalance.setAmountAvailable(BigDecimal.ZERO);
                    newBalance.setAmountAvailableVchanged(BigDecimal.ZERO);
                    newBalance.setAmountFrozen(BigDecimal.ZERO);
                    newBalance.setAmountFrozenVchanged(BigDecimal.ZERO);
                    newBalance.setAmountOccupied(BigDecimal.ZERO);
                    newBalance.setAmountOccupiedVchanged(BigDecimal.ZERO);
                    newBalance.setAmountActual(BigDecimal.ZERO);
                    newBalance.setAmountActualVchanged(BigDecimal.ZERO);
                    newBalance.setAmountPayAvailable(BigDecimal.ZERO);
                    newBalance.setAmountPayAvailableVchanged(BigDecimal.ZERO);
                    newBalance.setDeleted(Boolean.FALSE);
                    newBalance.setCreateTime(LocalDateTime.now());
                    newBalance.setUpdateTime(LocalDateTime.now());
                    
                    // 将数据放入 Map（需要保存到数据库的列表会在 batchUpdateDatabase 中处理）
                    needToUpdateSameDemBudgetBalanceMap.put(bizKeyQuarterForAll, newBalance);
                    needToUpdateSameDemBudgetQuotaMap.put(bizKeyQuarterForAll, quota);
                    needToAddPoolDemRMap.put(bizKeyQuarterForAll, poolDemR);
                    
                    log.info("========== 构造自己维度的数据: bizKeyQuarter={}, poolId={}, quotaId={}, balanceId={} ==========",
                            bizKeyQuarterForAll, poolId, quotaId, balanceId);
                }
                
                // 获取当前季度对应的 balance
                balance = needToUpdateSameDemBudgetBalanceMap.get(bizKeyQuarter);
                log.info("========== 成功构造自己维度的数据（所有4个季度），当前季度: bizKeyQuarter={}, poolId={} ==========", 
                        bizKeyQuarter, balance != null ? balance.getPoolId() : null);
                }
            }
            
            Long poolId = balance.getPoolId();

            // 获取当前季度的操作金额（用于日志记录操作前的状态）
            BigDecimal currentAmountOperated = getCurrentAmountOperated(Collections.singletonList(balance));
            
            // 直接使用 ledger 的 amount 作为扣减数
            BigDecimal newAmountOperateConsumed = ledger.getAmount() == null ? BigDecimal.ZERO : ledger.getAmount();
            
            // 计算实际需要冻结的总金额
            // 需要获取当前季度及之前所有季度的操作金额，如果之前季度的操作金额小于0，则 totalAmountToOperate = newAmountOperateConsumed - 那个操作金额
            BigDecimal totalAmountToOperate = newAmountOperateConsumed;
            int currentQuarterNum = getQuarterNumber(newQuarter);
            String[] quarters = {"q1", "q2", "q3", "q4"};
            
            // 记录补偿信息：用于报错提示（负操作金额）与写回剔除（实际补偿金额）
            Map<String, BigDecimal> compensationMap = new HashMap<>(); // key: 季度, value: 负操作金额
            Map<String, BigDecimal> compensationNeededMap = new HashMap<>(); // key: 季度, value: 实际补偿金额(>=0)
            // 累计当前季度及之前季度的净操作金额；仅当累计后仍为负时才触发补偿
            BigDecimal cumulativeAmountOperated = BigDecimal.ZERO;
            BigDecimal cumulativeAmountAvailable = BigDecimal.ZERO;
            BigDecimal cumulativeAmountPayAvailable = BigDecimal.ZERO;
            BigDecimal cumulativeCompensation = BigDecimal.ZERO;
            
            // 遍历当前季度及之前的所有季度（从q1到当前季度）
            for (int i = 0; i < currentQuarterNum; i++) {
                String quarter = quarters[i];
                String bizKeyQuarterForCheck = bizKey + "@" + quarter;
                BudgetBalance balanceForQuarter = needToUpdateSameDemBudgetBalanceMap.get(bizKeyQuarterForCheck);
                if (balanceForQuarter != null) {
                    BigDecimal quarterAmountOperated = getCurrentAmountOperated(Collections.singletonList(balanceForQuarter));
                    BigDecimal quarterAmountAvailable = balanceForQuarter.getAmountAvailable() == null ? BigDecimal.ZERO : balanceForQuarter.getAmountAvailable();
                    BigDecimal quarterAmountPayAvailable = balanceForQuarter.getAmountPayAvailable() == null ? BigDecimal.ZERO : balanceForQuarter.getAmountPayAvailable();
                    cumulativeAmountOperated = cumulativeAmountOperated.add(quarterAmountOperated);
                    cumulativeAmountAvailable = cumulativeAmountAvailable.add(quarterAmountAvailable);
                    cumulativeAmountPayAvailable = cumulativeAmountPayAvailable.add(quarterAmountPayAvailable);
                    if (quarterAmountOperated.compareTo(BigDecimal.ZERO) < 0) {
                        BigDecimal negativeAmountAbs = quarterAmountOperated.abs(); // 当前季度负操作金额绝对值
                        
                        boolean needCompensation = false;
                        BigDecimal compensationNeeded = BigDecimal.ZERO;
                        
                        // 检查是否有关联流水
                        // 如果有关联流水，回滚是回滚给关联流水的，资金池的 amountOperated 只是记录作用
                        // 只有在资金池的操作上才会去考虑 amountOperated 的负数
                        // 所以，如果有关联流水，不需要补偿（因为回滚是回滚给关联流水的，不是资金池）
                        List<BudgetLedger> relatedLedgers = updatedRelatedBudgetLedgerMap != null ? updatedRelatedBudgetLedgerMap.get(bizKey) : null;
                        boolean hasRelatedLedgers = !CollectionUtils.isEmpty(relatedLedgers);
                        
                        if (hasRelatedLedgers) {
                            // 有关联流水时，amountOperated 的负数只是记录作用，不需要补偿
                            // 因为回滚是回滚给关联流水的 amountAvailable，不是资金池
                            log.info("========== processSameDimensionUpdate - 有关联流水，amountOperated的负数只是记录作用，不需要补偿: quarter={}, amountOperated={}, 负操作金额绝对值={} ==========",
                                    quarter, quarterAmountOperated, negativeAmountAbs);
                        } else {
                            // 无关联流水时，先按“累计净占用”判断是否需要补偿：
                            // 只有 q1..当前季度累计仍为负，才补偿净负缺口；避免单季度负值但历史正值可抵消时被过度补偿。
                            // CLAIM（项目/资产类型）使用累计 amountPayAvailable 作为覆盖能力，其他场景使用累计 amountAvailable。
                            if (cumulativeAmountOperated.compareTo(BigDecimal.ZERO) < 0) {
                                needCompensation = true;
                                BigDecimal requiredCompensation = cumulativeAmountOperated.abs();
                                boolean usePayAvailable = CLAIM_BIZ_TYPE.equals(ledger.getBizType()) && !bizKey.endsWith("@NAN@NAN");
                                BigDecimal cumulativeCoverAmount = usePayAvailable ? cumulativeAmountPayAvailable : cumulativeAmountAvailable;
                                BigDecimal totalCompensationNeeded = requiredCompensation.subtract(cumulativeCoverAmount);
                                if (totalCompensationNeeded.compareTo(BigDecimal.ZERO) < 0) {
                                    totalCompensationNeeded = BigDecimal.ZERO;
                                }
                                compensationNeeded = totalCompensationNeeded.subtract(cumulativeCompensation);
                                if (compensationNeeded.compareTo(BigDecimal.ZERO) < 0) {
                                    compensationNeeded = BigDecimal.ZERO;
                                }
                                log.info("========== processSameDimensionUpdate - 累计净占用仍为负，需补偿净缺口: quarter={}, amountOperated={}, 累计amountOperated={}, 累计coverAmount={}, 使用cover字段={}, 已补偿={}, 本次补偿={} ==========",
                                        quarter, quarterAmountOperated, cumulativeAmountOperated, cumulativeCoverAmount, usePayAvailable ? "amountPayAvailable" : "amountAvailable", cumulativeCompensation, compensationNeeded);
                            } else {
                                log.info("========== processSameDimensionUpdate - 当前季度amountOperated为负，但累计净占用已非负，不补偿: quarter={}, amountOperated={}, 累计amountOperated={} ==========",
                                        quarter, quarterAmountOperated, cumulativeAmountOperated);
                            }
                        }
                        
                        if (needCompensation && compensationNeeded.compareTo(BigDecimal.ZERO) > 0) {
                            totalAmountToOperate = totalAmountToOperate.add(compensationNeeded);
                            compensationMap.put(quarter, quarterAmountOperated);
                            compensationNeededMap.merge(quarter, compensationNeeded, BigDecimal::add);
                            cumulativeCompensation = cumulativeCompensation.add(compensationNeeded);
                            log.info("========== processSameDimensionUpdate - 调整后totalAmountToOperate={}, 累计补偿={} ==========", totalAmountToOperate, cumulativeCompensation);
                        }
                    }
                }
            }
            
            // 校验预算是否充足（使用调整后的totalAmountToOperate进行校验）
            validateBudgetSufficient(bizKey, ledger, balanceMap, updatedRelatedBudgetLedgerMap, totalAmountToOperate, compensationMap);
            
            log.info("========== processSameDimensionUpdate - 开始扣减: poolId={}, bizItemCode={}, currentQuarter={}, currentAmountOperated={}, newAmountOperateConsumed={}, totalAmountToOperate={} ==========",
                    poolId, ledger.getBizItemCode(), newQuarter, currentAmountOperated, newAmountOperateConsumed, totalAmountToOperate);

            // 执行跨季度扣减计算（抽象方法，由子类实现，因为不同子类影响的字段不同）
            Map<String, BigDecimal> quarterOperateAmountMap = new HashMap<>(); // 记录每个季度操作的金额
            performMultiQuarterDeduction(ledger.getId(), bizKey, newQuarter, totalAmountToOperate, balanceMap, needToUpdateSameDemBudgetBalanceMap, quarterOperateAmountMap, updatedRelatedBudgetLedgerMap, relatedLedgerDeductionAmountMap);
            
            // 将各季度的操作金额记录到 ledger 的对应季度字段中（约定：amountConsumedQ* 表示本单从资金池各季度扣减的金额，sum=amount）
            BigDecimal q1Amount = quarterOperateAmountMap.getOrDefault("q1", BigDecimal.ZERO);
            BigDecimal q2Amount = quarterOperateAmountMap.getOrDefault("q2", BigDecimal.ZERO);
            BigDecimal q3Amount = quarterOperateAmountMap.getOrDefault("q3", BigDecimal.ZERO);
            BigDecimal q4Amount = quarterOperateAmountMap.getOrDefault("q4", BigDecimal.ZERO);

            // 剔除补偿金额：amountConsumedQ* 只记录“本单金额”分摊，不混入补偿额
            q1Amount = q1Amount.subtract(compensationNeededMap.getOrDefault("q1", BigDecimal.ZERO));
            q2Amount = q2Amount.subtract(compensationNeededMap.getOrDefault("q2", BigDecimal.ZERO));
            q3Amount = q3Amount.subtract(compensationNeededMap.getOrDefault("q3", BigDecimal.ZERO));
            q4Amount = q4Amount.subtract(compensationNeededMap.getOrDefault("q4", BigDecimal.ZERO));
            if (q1Amount.compareTo(BigDecimal.ZERO) < 0) q1Amount = BigDecimal.ZERO;
            if (q2Amount.compareTo(BigDecimal.ZERO) < 0) q2Amount = BigDecimal.ZERO;
            if (q3Amount.compareTo(BigDecimal.ZERO) < 0) q3Amount = BigDecimal.ZERO;
            if (q4Amount.compareTo(BigDecimal.ZERO) < 0) q4Amount = BigDecimal.ZERO;
            
            setQuarterAmountToLedgerOverwrite(ledger, q1Amount, q2Amount, q3Amount, q4Amount);
            
            log.info("========== processSameDimensionUpdate - 各季度操作金额记录: poolId={}, bizItemCode={}, bizType={}, q1={}, q2={}, q3={}, q4={} ==========",
                    poolId, ledger.getBizItemCode(), ledger.getBizType(), q1Amount, q2Amount, q3Amount, q4Amount);

            // 更新当前季度的 balance（需要重新获取，因为可能在跨季度扣减时被修改）
            BudgetBalance updatedBalance = needToUpdateSameDemBudgetBalanceMap.get(bizKeyQuarter);
            BigDecimal updatedAmountOperated = getCurrentAmountOperated(Collections.singletonList(updatedBalance));
            BigDecimal updatedAmountAvailable = updatedBalance.getAmountAvailable() == null ? BigDecimal.ZERO : updatedBalance.getAmountAvailable();

            BudgetBalanceHistory balanceHistory = new BudgetBalanceHistory();
            BeanUtils.copyProperties(updatedBalance, balanceHistory);
            balanceHistory.setId(identifierGenerator.nextId(balanceHistory).longValue());
            balanceHistory.setBalanceId(updatedBalance.getId());
            balanceHistory.setDeleted(Boolean.FALSE);
            
            updatedBalance.setVersion(ledger.getVersion());
            needToAddBudgetBalanceHistory.add(balanceHistory);
            
            log.info("========== processSameDimensionUpdate - 冻结后状态: poolId={}, currentQuarter={}, amountAvailable={}, amountOperated={} ==========",
                    poolId, newQuarter, updatedAmountAvailable, updatedAmountOperated);
            
            // 计算可用预算相关数值：可用预算占比、额度、冻结、实际等
            // 计算当前季度及之前季度的累计额度/冻结/实际/可用
            String[] quartersForSum = {"q1", "q2", "q3", "q4"};
            int currentQuarterIndex = getQuarterNumber(newQuarter) - 1; // 0-based

            BigDecimal amountQuota = BigDecimal.ZERO;
            BigDecimal amountFrozen = BigDecimal.ZERO;
            BigDecimal amountActual = BigDecimal.ZERO;
            BigDecimal amountAvailable = BigDecimal.ZERO;
            BigDecimal amountPayAvailable = BigDecimal.ZERO;

            // 检查是否有关联预算流水
            // updatedRelatedBudgetLedgerMap 的 key 格式是：documentNo + "@" + detailLineNo
            // bizKey 的格式是：bizCode + "@" + bizItemCode
            // 理论上 bizCode = documentNo, bizItemCode = detailLineNo，所以 bizKey 应该等于 updatedRelatedBudgetLedgerMap 的 key
            // 但为了确保匹配，我们使用 ledger.getBizCode() + "@" + ledger.getBizItemCode() 来构造 key
            String lookupKey = ledger.getBizCode() + "@" + ledger.getBizItemCode();
            List<BudgetLedger> relatedLedgers = updatedRelatedBudgetLedgerMap != null ? updatedRelatedBudgetLedgerMap.get(lookupKey) : null;
            boolean hasRelatedLedgers = !CollectionUtils.isEmpty(relatedLedgers);
            
            // 判断是否是带项目或资产类型的预算（只使用 amountPayAvailable，与校验和扣减逻辑一致）
            boolean hasPaymentAmount = !"NAN".equals(ledger.getErpAssetType()) || !"NAN".equals(ledger.getMasterProjectCode());
            
            // 添加调试日志
            if (updatedRelatedBudgetLedgerMap != null && !updatedRelatedBudgetLedgerMap.isEmpty()) {
                log.info("========== calculateAvailableBudgetRatio - 查找关联预算流水: bizKey={}, lookupKey={}, updatedRelatedBudgetLedgerMap.keys={}, hasRelatedLedgers={} ==========",
                        bizKey, lookupKey, updatedRelatedBudgetLedgerMap.keySet(), hasRelatedLedgers);
            } else {
                log.info("========== calculateAvailableBudgetRatio - 查找关联预算流水: bizKey={}, lookupKey={}, updatedRelatedBudgetLedgerMap为空或null ==========",
                        bizKey, lookupKey);
            }

            for (int i = 0; i <= currentQuarterIndex && i < quartersForSum.length; i++) {
                String quarter = quartersForSum[i];
                String bizKeyQuarterForSum = bizKey + "@" + quarter;

                // 从 quotaMap 获取该季度的 quota 列表，遍历累加
                List<BudgetQuota> quotaListForQuarter = quotaMap.get(bizKeyQuarterForSum);
                if (!CollectionUtils.isEmpty(quotaListForQuarter)) {
                    BigDecimal quarterAmountTotal = BigDecimal.ZERO;
                    BigDecimal quarterAmountAdj = BigDecimal.ZERO;
                    BigDecimal quarterAmountPay = BigDecimal.ZERO;
                    
                    for (BudgetQuota quota : quotaListForQuarter) {
                        quarterAmountTotal = quarterAmountTotal.add(quota.getAmountTotal() == null ? BigDecimal.ZERO : quota.getAmountTotal());
                        quarterAmountAdj = quarterAmountAdj.add(quota.getAmountAdj() == null ? BigDecimal.ZERO : quota.getAmountAdj());
                        quarterAmountPay = quarterAmountPay.add(quota.getAmountPay() == null ? BigDecimal.ZERO : quota.getAmountPay());
                    }
                    
                    BigDecimal amountTotalAdj = quarterAmountTotal.add(quarterAmountAdj);
                    // 当 erpAssetType 不为 NAN 或者项目编码不为 NAN 时，额度取 amountTotalAdj 与 amountPay 的较小值，其他场景取 amountTotalAdj
                    BigDecimal quotaToAdd = hasPaymentAmount 
                        ? amountTotalAdj.min(quarterAmountPay) 
                        : amountTotalAdj;
                    amountQuota = amountQuota.add(quotaToAdd);
                }

                // 从 balanceMap 获取该季度的 balance 列表，遍历累加
                List<BudgetBalance> balanceListForQuarter = balanceMap.get(bizKeyQuarterForSum);
                if (!CollectionUtils.isEmpty(balanceListForQuarter)) {
                    for (BudgetBalance balanceItem : balanceListForQuarter) {
                        amountFrozen = amountFrozen.add(balanceItem.getAmountFrozen() == null ? BigDecimal.ZERO : balanceItem.getAmountFrozen());
                        amountActual = amountActual.add(balanceItem.getAmountActual() == null ? BigDecimal.ZERO : balanceItem.getAmountActual());
                        // amountAvailable 和 amountPayAvailable 的计算逻辑：如果有关联预算流水，使用关联流水的值；否则使用资金池的值
                        if (!hasRelatedLedgers) {
                            // 没有关联预算流水，使用资金池的值
                            amountAvailable = amountAvailable.add(balanceItem.getAmountAvailable() == null ? BigDecimal.ZERO : balanceItem.getAmountAvailable());
                            if (hasPaymentAmount) {
                                // 带项目或资产类型，需要累加 amountPayAvailable
                                amountPayAvailable = amountPayAvailable.add(balanceItem.getAmountPayAvailable() == null ? BigDecimal.ZERO : balanceItem.getAmountPayAvailable());
                            }
                        }
                        // 如果有关联预算流水，amountAvailable 会在循环外从关联流水中累加，这里不累加资金池的值
                    }
                }
            }

            // 如果有关联预算流水，使用关联预算流水的 amountAvailable（累加所有关联流水的 amountAvailable）
            if (hasRelatedLedgers) {
                BigDecimal relatedAmountAvailable = BigDecimal.ZERO;
                for (BudgetLedger relatedLedger : relatedLedgers) {
                    BigDecimal ledgerAmountAvailable = relatedLedger.getAmountAvailable() == null ? BigDecimal.ZERO : relatedLedger.getAmountAvailable();
                    relatedAmountAvailable = relatedAmountAvailable.add(ledgerAmountAvailable);
                }
                amountAvailable = relatedAmountAvailable;
                log.info("========== calculateAvailableBudgetRatio - 使用关联预算流水的 amountAvailable: bizKey={}, 关联流水数量={}, amountAvailable={} ==========",
                        bizKey, relatedLedgers.size(), amountAvailable);
            } else {
                log.info("========== calculateAvailableBudgetRatio - 使用资金池的值: bizKey={}, amountAvailable={}, amountPayAvailable={} ==========",
                        bizKey, amountAvailable, amountPayAvailable);
            }
            
            // 对于带项目或资产类型的预算，只使用 amountPayAvailable（付款额），与校验和扣减逻辑一致
            // 判断是否是项目还是资产类型
            boolean isProjectQuery = !"NAN".equals(ledger.getMasterProjectCode());
            boolean isAssetTypeQuery = !"NAN".equals(ledger.getErpAssetType());
            
            if (!hasRelatedLedgers && (isProjectQuery || isAssetTypeQuery)) {
                // 没有关联预算流水且是带项目或资产类型，只使用 amountPayAvailable（付款额）
                amountAvailable = amountPayAvailable;
                log.info("========== calculateAvailableBudgetRatio - 项目或资产类型，只使用amountPayAvailable: bizKey={}, amountPayAvailable={}, isProjectQuery={}, isAssetTypeQuery={} ==========",
                        bizKey, amountPayAvailable, isProjectQuery, isAssetTypeQuery);
            }

            BigDecimal ratio = BigDecimal.ZERO;
            if (amountQuota.compareTo(BigDecimal.ZERO) > 0) {
                ratio = amountAvailable.divide(amountQuota, 2, RoundingMode.HALF_UP);
            }

            DetailNumberVo detailNumberVo = new DetailNumberVo();
            detailNumberVo.setAmountQuota(amountQuota);
            detailNumberVo.setAmountFrozen(amountFrozen);
            detailNumberVo.setAmountActual(amountActual);
            detailNumberVo.setAmountAvailable(amountAvailable);
            detailNumberVo.setAvailableBudgetRatio(ratio);
            String bizItemCode = ledger.getBizItemCode();
            availableBudgetRatioMap.put(bizItemCode, detailNumberVo);
            log.info("========== calculateAvailableBudgetRatio - 保存可用预算信息到 availableBudgetRatioMap: bizItemCode={}, amountAvailable={}, availableBudgetRatio={} ==========",
                    bizItemCode, amountAvailable, ratio);
        }
        } finally {
            BALANCE_QUERY_SKIP_REASONS_THREAD_LOCAL.remove();
        }
    }
    
    /**
     * 获取业务类型
     * 子类需要实现此方法，返回对应的业务类型（如 "APPLY"、"CONTRACT"、"CLAIM"）
     *
     * @return 业务类型
     */
    protected abstract String getBizType();
    
    /**
     * 获取默认币种
     * 子类可以重写此方法，返回默认币种（如 "CNY"）
     * 默认返回 "CNY"
     *
     * @return 默认币种
     */
    protected String getDefaultCurrency() {
        return "CNY";
    }
    
    /**
     * 检查是否需要查询 quota 和 balance，如果需要则执行查询
     * 
     * @param needUpdateSameDemBudgetLedgerMap 需要更新的预算流水Map
     * @param needToUpdateSameDemBudgetQuotaMap 需要更新的预算额度Map（会被更新）
     * @param needToUpdateSameDemBudgetBalanceMap 需要更新的预算余额Map（会被更新）
     * @param updatedRelatedBudgetLedgerMap 更新后的关联预算流水Map（会被更新）
     * @param ehrCdToOrgCdMap EHR组织编码到管理组织编码的映射
     * @param erpAcctCdToAcctCdMap ERP科目编码到科目编码的映射
     */
    private void queryQuotaAndBalanceIfNeeded(Map<String, BudgetLedger> needUpdateSameDemBudgetLedgerMap,
                                             Map<String, BudgetQuota> needToUpdateSameDemBudgetQuotaMap,
                                             Map<String, BudgetBalance> needToUpdateSameDemBudgetBalanceMap,
                                             Map<String, List<BudgetLedger>> updatedRelatedBudgetLedgerMap,
                                             Map<String, String> ehrCdToOrgCdMap,
                                             Map<String, List<String>> ehrCdToOrgCdExtMap,
                                             Map<String, String> erpAcctCdToAcctCdMap,
                                             Map<String, List<String>> erpAcctCdToAcctCdExtMap,
                                             Map<String, List<String>> prjCdToRelatedPrjCdExtMap,
                                             Map<String, String> erpAssetTypeToMemberCdMap) {
        // 检查是否需要查询：如果传入的 Map 已经包含了所有需要的数据，则不需要重新查询
        // 找出需要查询的 ledger（即那些在 Map 中找不到对应数据的 ledger）
        Map<String, BudgetLedger> needToQueryLedgerMap = new HashMap<>();
        for (Map.Entry<String, BudgetLedger> entry : needUpdateSameDemBudgetLedgerMap.entrySet()) {
            String bizKey = entry.getKey();
            BudgetLedger ledger = entry.getValue();
            
            // 判断是否不受控（需要跳过预算余额查询，但仍保存数据到BUDGET_LEDGER表）
            if (isUncontrolledLedger(ledger, ehrCdToOrgCdExtMap, erpAcctCdToAcctCdExtMap)) {
                log.info("========== queryQuotaAndBalanceIfNeeded - 不受控明细跳过预算余额查询: bizKey={}, morgCode={}, budgetSubjectCode={}, masterProjectCode={} ==========",
                        bizKey, ledger.getMorgCode(), ledger.getBudgetSubjectCode(), ledger.getMasterProjectCode());
                continue;
            }
            
            String monthForQuarter = ledger.getMonth();
            if (CLAIM_BIZ_TYPE.equals(ledger.getBizType())) {
                if (CLAIM_ACTUAL_DATE_SOURCE_FLAG == 0 && StringUtils.isNotBlank(ledger.getActualMonth())) {
                    monthForQuarter = ledger.getActualMonth();
                }
            }
            String newQuarter = convertMonthToQuarter(monthForQuarter);
            String bizKeyQuarter = bizKey + "@" + newQuarter;
            
            // 检查是否已经存在对应的 balance 和 quota
            boolean needQuery = false;
            if (!needToUpdateSameDemBudgetBalanceMap.containsKey(bizKeyQuarter)) {
                needQuery = true;
            } else {
                // 检查所有季度是否都存在
                String[] quarters = {"q1", "q2", "q3", "q4"};
                for (String quarter : quarters) {
                    String bizKeyQuarterForCheck = bizKey + "@" + quarter;
                    if (!needToUpdateSameDemBudgetBalanceMap.containsKey(bizKeyQuarterForCheck) ||
                        !needToUpdateSameDemBudgetQuotaMap.containsKey(bizKeyQuarterForCheck)) {
                        needQuery = true;
                        break;
                    }
                }
            }
            
            if (needQuery) {
                needToQueryLedgerMap.put(bizKey, ledger);
            }
        }
        
        // 只查询需要查询的 ledger
        if (!needToQueryLedgerMap.isEmpty()) {
            log.info("========== processSameDimensionUpdate - 需要查询 {} 个 ledger 的 quota 和 balance，ledger keys: {} ==========", 
                    needToQueryLedgerMap.size(), needToQueryLedgerMap.keySet());
            BudgetQueryHelperService.BudgetQuotaBalanceResult result = 
                    budgetQueryHelperService.queryQuotaAndBalanceByAllQuarters(needToQueryLedgerMap, ehrCdToOrgCdMap, erpAcctCdToAcctCdMap, erpAssetTypeToMemberCdMap);
            
            // 检查是否有错误信息
            if (result.hasError()) {
                throw new IllegalStateException(result.getErrorMessage());
            }
            
            needToUpdateSameDemBudgetQuotaMap.putAll(result.getQuotaMap());
            needToUpdateSameDemBudgetBalanceMap.putAll(result.getBalanceMap());
            
            // 合并关联的预算流水Map
            if (result.getRelatedBudgetLedgerMap() != null && !result.getRelatedBudgetLedgerMap().isEmpty()) {
                updatedRelatedBudgetLedgerMap.putAll(result.getRelatedBudgetLedgerMap());
            }
        } else {
            log.info("========== processSameDimensionUpdate - 所有 {} 个 ledger 的 quota 和 balance 已存在，无需重新查询 ==========", 
                    needUpdateSameDemBudgetLedgerMap.size());
        }
    }
    
    /**
     * 将维度不一致回滚后的 balance（同池同季度）合并到同维度 balanceMap，避免新维度扣减时读到 DB 旧值导致 amountFrozen 等被重复扣成负数。
     * 仅合并 currentBatchBizKeys 对应的条目，避免同一请求中「维度一致更新」已扣减的 balance 被覆盖。
     */
    private void mergeRollbackBalanceIntoSameMap(Map<String, BudgetBalance> needToUpdateSameDemBudgetBalanceMap,
                                                 Map<String, BudgetBalance> needToUpdateDiffDemBudgetBalanceMapForMerge,
                                                 Set<String> currentBatchBizKeys) {
        if (needToUpdateDiffDemBudgetBalanceMapForMerge == null || needToUpdateDiffDemBudgetBalanceMapForMerge.isEmpty()) {
            return;
        }
        if (currentBatchBizKeys == null || currentBatchBizKeys.isEmpty()) {
            return;
        }
        Map<String, BudgetBalance> poolQuarterToDiffBalance = new HashMap<>();
        for (BudgetBalance b : needToUpdateDiffDemBudgetBalanceMapForMerge.values()) {
            if (b.getPoolId() != null && StringUtils.isNotBlank(b.getQuarter())) {
                String key = b.getPoolId() + "@" + b.getQuarter();
                poolQuarterToDiffBalance.putIfAbsent(key, b);
            }
        }
        for (Map.Entry<String, BudgetBalance> entry : needToUpdateSameDemBudgetBalanceMap.entrySet()) {
            String bizKeyQuarter = entry.getKey();
            int lastAt = bizKeyQuarter.lastIndexOf('@');
            if (lastAt <= 0) {
                continue;
            }
            String bizKey = bizKeyQuarter.substring(0, lastAt);
            if (!currentBatchBizKeys.contains(bizKey)) {
                continue;
            }
            BudgetBalance sameBalance = entry.getValue();
            if (sameBalance.getPoolId() == null) {
                continue;
            }
            String quarter = bizKeyQuarter.substring(lastAt + 1);
            String poolQuarterKey = sameBalance.getPoolId() + "@" + quarter;
            BudgetBalance diffBalance = poolQuarterToDiffBalance.get(poolQuarterKey);
            if (diffBalance == null) {
                continue;
            }
            sameBalance.setAmountFrozen(diffBalance.getAmountFrozen());
            sameBalance.setAmountOccupied(diffBalance.getAmountOccupied());
            sameBalance.setAmountAvailable(diffBalance.getAmountAvailable());
            sameBalance.setAmountFrozenVchanged(diffBalance.getAmountFrozenVchanged());
            sameBalance.setAmountOccupiedVchanged(diffBalance.getAmountOccupiedVchanged());
            sameBalance.setAmountAvailableVchanged(diffBalance.getAmountAvailableVchanged());
            log.debug("========== mergeRollbackBalanceIntoSameMap - 合并回滚后 balance: poolId={}, quarter={}, amountFrozen={}, amountOccupied={} ==========",
                    sameBalance.getPoolId(), quarter, sameBalance.getAmountFrozen(), sameBalance.getAmountOccupied());
        }
    }
    
    /**
     * 判断一条 BudgetLedger 是否不受控（需要跳过预算校验和预算余额更新，但仍保存数据到BUDGET_LEDGER表）
     * 三种不受控情况：
     * 1. 项目非 NAN 且 isInternal=1
     * 2. 科目编码不在白名单中（不以配置前缀开头）且不是带项目的明细且不是带资产类型的明细
     * 3. SUBJECT_EXT_INFO表查询结果包含"NAN-NAN"的科目
     * 4. EHR_ORG_MANAGE_EXT_R表查询结果包含"NAN"的组织
     * 
     * @param ledger 预算流水
     * @param ehrCdToOrgCdExtMap EHR_ORG_MANAGE_EXT_R表查询结果，Map<EHR_CD, List<ORG_CD>>
     * @param erpAcctCdToAcctCdExtMap SUBJECT_EXT_INFO表查询结果，Map<ERP_ACCT_CD, List<ACCT_CD>>
     * @return true表示不受控，false表示受控
     */
    protected boolean isUncontrolledLedger(BudgetLedger ledger,
                                          Map<String, List<String>> ehrCdToOrgCdExtMap,
                                          Map<String, List<String>> erpAcctCdToAcctCdExtMap) {
        String morgCode = ledger.getMorgCode();
        String budgetSubjectCode = ledger.getBudgetSubjectCode();
        String masterProjectCode = ledger.getMasterProjectCode();
        String isInternal = ledger.getIsInternal();

        // 1. 项目非 NAN 且 isInternal=1 时跳过处理
        if (!"NAN".equals(masterProjectCode) && "1".equals(isInternal)) {
            return true;
        }

        // 4. EHR_ORG_MANAGE_EXT_R 表中 ORG_CD 为 NAN 的组织
        if (StringUtils.isNotBlank(morgCode) && ehrCdToOrgCdExtMap != null) {
            List<String> orgCdList = ehrCdToOrgCdExtMap.get(morgCode);
            if (orgCdList != null && orgCdList.contains("NAN")) {
                log.debug("========== 检测到组织编码 {} 为不受控组织（映射结果包含NAN） ==========", morgCode);
                return true;
            }
        }

        // 3. SUBJECT_EXT_INFO 表中 ERP_ACCT_CD 为 NAN-NAN 的科目
        if (StringUtils.isNotBlank(budgetSubjectCode) && !"NAN-NAN".equals(budgetSubjectCode) && erpAcctCdToAcctCdExtMap != null) {
            List<String> acctCdList = erpAcctCdToAcctCdExtMap.get(budgetSubjectCode);
            if (acctCdList != null && acctCdList.contains("NAN-NAN")) {
                log.debug("========== 检测到科目编码 {} 为不受控科目（映射结果包含NAN-NAN） ==========", budgetSubjectCode);
                return true;
            }
        }

        // 2. 科目编码不在白名单中且不是带项目/资产类型的明细
        // 科目编码为空或"NAN-NAN"的情况，如果也不带项目，则不受控
        if (StringUtils.isNotBlank(budgetSubjectCode) && !"NAN-NAN".equals(budgetSubjectCode)) {
            boolean isSubjectCodeInWhitelist = budgetSubjectCodeConfig != null && budgetSubjectCodeConfig.isInWhitelist(budgetSubjectCode);
            boolean hasProjectCode = StringUtils.isNotBlank(masterProjectCode) && !"NAN".equals(masterProjectCode);
            String erpAssetType = ledger.getErpAssetType();
            boolean hasAssetType = StringUtils.isNotBlank(erpAssetType) && !"NAN".equals(erpAssetType);
            if (!isSubjectCodeInWhitelist && !hasProjectCode && !hasAssetType) {
                log.debug("========== 检测到科目编码 {} 不在白名单中且不带项目且不带资产类型，为不受控科目 ==========", budgetSubjectCode);
                return true;
            }
        } else if (StringUtils.isBlank(budgetSubjectCode) || "NAN-NAN".equals(budgetSubjectCode)) {
            // 科目编码为空或"NAN-NAN"的情况
            // 如果是组织+资产类型维度（有资产类型），则受控，不应该判定为不受控
            String erpAssetType = ledger.getErpAssetType();
            boolean hasAssetType = StringUtils.isNotBlank(erpAssetType) && !"NAN".equals(erpAssetType);
            boolean hasProjectCode = StringUtils.isNotBlank(masterProjectCode) && !"NAN".equals(masterProjectCode);
            
            // 如果既没有项目编码，也没有资产类型，则不受控
            if (!hasProjectCode && !hasAssetType) {
                log.debug("========== 检测到科目编码为空或NAN-NAN且不带项目且不带资产类型，为不受控明细 ==========");
                return true;
            }
            // 如果有项目编码或资产类型，则受控（组织+项目维度或组织+资产类型维度）
        }

        return false;
    }

    /**
     * 是否跳过项目关联映射校验。
     * 默认不跳过，子类可按业务覆写。
     */
    protected boolean shouldSkipProjectMappingValidation(DetailDetailVo detail) {
        return false;
    }
    
    /**
     * 从查询条件判断明细是否不受控（用于关联申请单校验时跳过校验）
     * 判断逻辑与 isUncontrolledLedger 方法一致
     * 
     * @param queryManagementOrg 查询条件中的组织编码
     * @param queryBudgetSubjectCode 查询条件中的科目编码
     * @param queryMasterProjectCode 查询条件中的项目编码
     * @param queryIsInternal 查询条件中的是否内部
     * @param queryErpAssetType 查询条件中的资产类型
     * @param ehrCdToOrgCdExtMap EHR_ORG_MANAGE_EXT_R表查询结果，Map<EHR_CD, List<ORG_CD>>
     * @param erpAcctCdToAcctCdExtMap SUBJECT_EXT_INFO表查询结果，Map<ERP_ACCT_CD, List<ACCT_CD>>
     * @return true表示不受控，false表示受控
     */
    private boolean isUncontrolledDetailFromCondition(String queryManagementOrg, String queryBudgetSubjectCode,
                                                      String queryMasterProjectCode, String queryIsInternal,
                                                      String queryErpAssetType,
                                                      Map<String, List<String>> ehrCdToOrgCdExtMap,
                                                      Map<String, List<String>> erpAcctCdToAcctCdExtMap) {
        // 1. 项目非 NAN 且 isInternal=1 时跳过处理
        if (!"NAN".equals(queryMasterProjectCode) && "1".equals(queryIsInternal)) {
            return true;
        }

        // 4. EHR_ORG_MANAGE_EXT_R 表中 ORG_CD 为 NAN 的组织
        if (StringUtils.isNotBlank(queryManagementOrg) && ehrCdToOrgCdExtMap != null) {
            List<String> orgCdList = ehrCdToOrgCdExtMap.get(queryManagementOrg);
            if (orgCdList != null && orgCdList.contains("NAN")) {
                log.debug("========== 检测到组织编码 {} 为不受控组织（映射结果包含NAN） ==========", queryManagementOrg);
                return true;
            }
        }

        // 3. SUBJECT_EXT_INFO 表中 ERP_ACCT_CD 为 NAN-NAN 的科目
        if (StringUtils.isNotBlank(queryBudgetSubjectCode) && !"NAN-NAN".equals(queryBudgetSubjectCode) && erpAcctCdToAcctCdExtMap != null) {
            List<String> acctCdList = erpAcctCdToAcctCdExtMap.get(queryBudgetSubjectCode);
            if (acctCdList != null && acctCdList.contains("NAN-NAN")) {
                log.debug("========== 检测到科目编码 {} 为不受控科目（映射结果包含NAN-NAN） ==========", queryBudgetSubjectCode);
                return true;
            }
        }

        // 2. 科目编码不在白名单中且不是带项目/资产类型的明细
        // 科目编码为空或"NAN-NAN"的情况，如果也不带项目，则不受控
        if (StringUtils.isNotBlank(queryBudgetSubjectCode) && !"NAN-NAN".equals(queryBudgetSubjectCode)) {
            boolean isSubjectCodeInWhitelist = budgetSubjectCodeConfig != null && budgetSubjectCodeConfig.isInWhitelist(queryBudgetSubjectCode);
            boolean hasProjectCode = StringUtils.isNotBlank(queryMasterProjectCode) && !"NAN".equals(queryMasterProjectCode);
            boolean hasAssetType = StringUtils.isNotBlank(queryErpAssetType) && !"NAN".equals(queryErpAssetType);
            if (!isSubjectCodeInWhitelist && !hasProjectCode && !hasAssetType) {
                log.debug("========== 检测到科目编码 {} 不在白名单中且不带项目且不带资产类型，为不受控科目 ==========", queryBudgetSubjectCode);
                return true;
            }
        } else if (StringUtils.isBlank(queryBudgetSubjectCode) || "NAN-NAN".equals(queryBudgetSubjectCode)) {
            // 科目编码为空或"NAN-NAN"的情况
            // 如果是组织+资产类型维度（有资产类型），则受控，不应该判定为不受控
            boolean hasAssetType = StringUtils.isNotBlank(queryErpAssetType) && !"NAN".equals(queryErpAssetType);
            boolean hasProjectCode = StringUtils.isNotBlank(queryMasterProjectCode) && !"NAN".equals(queryMasterProjectCode);
            
            // 如果既没有项目编码，也没有资产类型，则不受控
            if (!hasProjectCode && !hasAssetType) {
                log.debug("========== 检测到科目编码为空或NAN-NAN且不带项目且不带资产类型，为不受控明细 ==========");
                return true;
            }
            // 如果有项目编码或资产类型，则受控（组织+项目维度或组织+资产类型维度）
        }

        return false;
    }
    
    /**
     * 获取季度编号
     *
     * @param quarter 季度（q1、q2、q3、q4）
     * @return 季度编号（1、2、3、4）
     */
    protected int getQuarterNumber(String quarter) {
        if ("q1".equals(quarter)) return 1;
        if ("q2".equals(quarter)) return 2;
        if ("q3".equals(quarter)) return 3;
        if ("q4".equals(quarter)) return 4;
        throw new IllegalArgumentException("无效的季度: " + quarter);
    }
    
    /**
     * 付款单明细是否关联「框架协议」合同流水（CONTRACT 且 effectType=1）
     */
    protected boolean isClaimFrameworkContractRelated(Map<String, List<BudgetLedger>> updatedRelatedBudgetLedgerMap, String bizKey) {
        if (updatedRelatedBudgetLedgerMap == null || StringUtils.isBlank(bizKey)) {
            return false;
        }
        List<BudgetLedger> list = updatedRelatedBudgetLedgerMap.get(bizKey);
        if (CollectionUtils.isEmpty(list)) {
            return false;
        }
        for (BudgetLedger ledger : list) {
            if (ledger != null && CONTRACT_BIZ_TYPE.equals(ledger.getBizType()) && "1".equals(ledger.getEffectType())) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * 预算校验结果实体
     */
    protected static class BudgetValidationResult {
        private final boolean useCustomCalculation;
        private final String relatedBizType;
        private final BigDecimal customAmount;
        /** 付款单关联框架协议合同（CONTRACT 且 effectType=1）时为 true */
        private final boolean claimFrameworkContract;
        
        public BudgetValidationResult(boolean useCustomCalculation, String relatedBizType, BigDecimal customAmount) {
            this(useCustomCalculation, relatedBizType, customAmount, false);
        }
        
        public BudgetValidationResult(boolean useCustomCalculation, String relatedBizType, BigDecimal customAmount,
                                      boolean claimFrameworkContract) {
            this.useCustomCalculation = useCustomCalculation;
            this.relatedBizType = relatedBizType;
            this.customAmount = customAmount;
            this.claimFrameworkContract = claimFrameworkContract;
        }
        
        public boolean isUseCustomCalculation() {
            return useCustomCalculation;
        }
        
        public String getRelatedBizType() {
            return relatedBizType;
        }
        
        public BigDecimal getCustomAmount() {
            return customAmount;
        }
        
        public boolean isClaimFrameworkContract() {
            return claimFrameworkContract;
        }
    }
    
    /**
     * 获取预算校验结果
     * 子类需要实现此方法，返回是否使用自定义计算方式以及自定义金额
     *
     * @param bizKey 业务key（bizCode + "@" + bizItemCode）
     * @param updatedRelatedBudgetLedgerMap 更新后的关联预算流水Map，key为bizCode + "@" + bizItemCode，value为关联的BudgetLedger列表
     * @return BudgetValidationResult 包含是否使用自定义计算和自定义金额
     */
    protected abstract BudgetValidationResult getBudgetValidationResult(String bizKey, 
                                                                        Map<String, List<BudgetLedger>> updatedRelatedBudgetLedgerMap);
    
    /**
     * 获取当前操作金额
     * 子类需要实现此方法，返回对应的操作金额字段（如 APPLY 返回 amountFrozen，CONTRACT 返回 amountOccupied）
     *
     * @param balanceList BudgetBalance对象列表
     * @return 当前操作金额（所有balance的操作金额之和）
     */
    protected abstract BigDecimal getCurrentAmountOperated(List<BudgetBalance> balanceList);
    
    /**
     * 校验预算是否充足
     *
     * @param bizKey 业务key（bizCode + "@" + bizItemCode）
     * @param ledger BudgetLedger对象，包含月份和金额信息
     * @param balanceMap 季度余额Map（key为 bizKey + "@" + quarter，value为 BudgetBalance 列表）
     * @param updatedRelatedBudgetLedgerMap 更新后的关联预算流水Map，key为bizCode + "@" + bizItemCode，value为关联的BudgetLedger列表
     * @throws IllegalStateException 如果预算不足则抛出异常
     */
    protected void validateBudgetSufficient(String bizKey,
                                          BudgetLedger ledger,
                                          Map<String, List<BudgetBalance>> balanceMap,
                                          Map<String, List<BudgetLedger>> updatedRelatedBudgetLedgerMap) {
        // 直接使用 ledger 的 amount 作为操作金额（兼容旧调用）
        BigDecimal newAmountOperateConsumed = ledger.getAmount() == null ? BigDecimal.ZERO : ledger.getAmount();
        validateBudgetSufficient(bizKey, ledger, balanceMap, updatedRelatedBudgetLedgerMap, newAmountOperateConsumed, null);
    }
    
    protected void validateBudgetSufficient(String bizKey,
                                          BudgetLedger ledger,
                                          Map<String, List<BudgetBalance>> balanceMap,
                                          Map<String, List<BudgetLedger>> updatedRelatedBudgetLedgerMap,
                                          BigDecimal totalAmountToOperate,
                                          Map<String, BigDecimal> compensationMap) {
        if (ledger == null) {
            log.error("========== BudgetValidationService - ledger为null，无法校验预算 ==========");
            throw new IllegalArgumentException("BudgetLedger不能为null");
        }
        if (balanceMap == null) {
            log.error("========== BudgetValidationService - balanceMap为null，无法校验预算 ==========");
            throw new IllegalArgumentException("balanceMap不能为null");
        }
        if (bizKey == null) {
            log.error("========== BudgetValidationService - bizKey为null，无法校验预算 ==========");
            throw new IllegalArgumentException("bizKey不能为null");
        }

        // 转换月份为季度：CLAIM 且开关为 0 时优先 actualMonth，否则使用 month
        String monthForQuarter = ledger.getMonth();
        if (CLAIM_BIZ_TYPE.equals(ledger.getBizType())) {
            if (CLAIM_ACTUAL_DATE_SOURCE_FLAG == 0 && StringUtils.isNotBlank(ledger.getActualMonth())) {
                monthForQuarter = ledger.getActualMonth();
            }
        }
        String currentQuarter = convertMonthToQuarter(monthForQuarter);
        if (currentQuarter == null) {
            log.error("========== BudgetValidationService - 无法确定季度: bizKey={}, month={} ==========", bizKey, ledger.getMonth());
            // 在异常消息中包含明细标识，格式：明细 [bizKey]，以便 tryExtractDetailError 能识别
            throw new IllegalStateException(String.format("明细 [%s] 无法确定季度，month=%s", bizKey, ledger.getMonth()));
        }

        String bizKeyQuarter = bizKey + "@" + currentQuarter;
        
        // 从 Map 中获取对应的 balance List
        List<BudgetBalance> balanceList = balanceMap.get(bizKeyQuarter);
        // 当 balanceMap 中无该 key 但有关联预算流水时，仅使用关联流水可用额校验，不抛异常
        List<BudgetLedger> relatedLedgersForValidation = updatedRelatedBudgetLedgerMap != null ? updatedRelatedBudgetLedgerMap.get(bizKey) : null;
        boolean hasRelatedLedgersOnly = CollectionUtils.isEmpty(balanceList) && !CollectionUtils.isEmpty(relatedLedgersForValidation);
        if (CollectionUtils.isEmpty(balanceList) && !hasRelatedLedgersOnly) {
            log.error("========== BudgetValidationService - bizKeyQuarter={} 在 balanceMap 中找不到对应的 balance ==========", bizKeyQuarter);
            throw new IllegalStateException(buildBalanceNotFoundMessage(bizKey, bizKeyQuarter));
        }
        
        // 使用传入的totalAmountToOperate作为操作金额（这是调整后的总金额）
        BigDecimal newAmountOperateConsumed = totalAmountToOperate;
        log.info("========== BudgetValidationService - 开始校验预算: bizItemCode={}, quarter={}, newAmountOperateConsumed={}, totalAmountToOperate={} ==========",
                ledger.getBizItemCode(), currentQuarter, ledger.getAmount(), totalAmountToOperate);

        // 累积从q1到当前季度的所有可用余额
        BudgetValidationResult validationResult = getBudgetValidationResult(bizKey, updatedRelatedBudgetLedgerMap);
        
        BigDecimal totalAmountAvailable;
        if (hasRelatedLedgersOnly && validationResult.isUseCustomCalculation()) {
            // 仅关联流水、无资金池：直接使用关联流水可用额
            totalAmountAvailable = validationResult.getCustomAmount() == null ? BigDecimal.ZERO : validationResult.getCustomAmount();
        } else if (!validationResult.isUseCustomCalculation()) {
            // 使用默认计算方式
            totalAmountAvailable = calculateTotalAmountAvailable(bizKey, currentQuarter, balanceMap, balanceList, ledger.getBizType());
        } else {
            // 使用自定义计算方式
            totalAmountAvailable = calculateTotalAmountAvailableCustom(bizKey, currentQuarter, balanceMap, balanceList, validationResult.getCustomAmount(), validationResult.getRelatedBizType(), ledger.getBizType(), validationResult.isClaimFrameworkContract());
        }
        
        // 获取当前季度的余额
        // 累加当前季度所有BudgetBalance的amountAvailable（如果为null则忽略）；仅关联流水时无 balance 则为 0
        BigDecimal currentAmountAvailable = BigDecimal.ZERO;
        if (!CollectionUtils.isEmpty(balanceList)) {
            for (BudgetBalance balance : balanceList) {
                BigDecimal amountAvailable = balance.getAmountAvailable();
                // 如果amountAvailable为null，忽略该balance；如果有数字（包括0），才累加
                if (amountAvailable != null) {
                    currentAmountAvailable = currentAmountAvailable.add(amountAvailable);
                }
            }
        }
        BigDecimal currentAmountOperated = CollectionUtils.isEmpty(balanceList) ? BigDecimal.ZERO : getCurrentAmountOperated(balanceList);
        
        // 获取原始金额（ledger的amount）
        BigDecimal originalAmount = ledger.getAmount() == null ? BigDecimal.ZERO : ledger.getAmount();
        
        log.info("========== BudgetValidationService - 冻结前状态: 当前季度={}, 当前季度amountAvailable={}, 当前季度amountOperated={}, 累计amountAvailable={}, 原始操作金额={}, 调整后操作金额={} ==========",
                currentQuarter, currentAmountAvailable, currentAmountOperated, totalAmountAvailable, originalAmount, newAmountOperateConsumed);
        
        // 合同单且有关联来源时，按来源可用额口径校验（与付款单保持一致），避免被资金池历史占用误拦截
        boolean contractUseRelatedOnlyValidation = CONTRACT_BIZ_TYPE.equals(ledger.getBizType())
                && validationResult.isUseCustomCalculation()
                && !CollectionUtils.isEmpty(relatedLedgersForValidation);

        // 校验预算是否充足
        validateBudget(bizKey, totalAmountAvailable, currentAmountOperated, newAmountOperateConsumed, originalAmount,
                ledger.getBizItemCode(), ledger.getBizType(), compensationMap, ledger.getYear(), currentQuarter, ledger,
                contractUseRelatedOnlyValidation);
    }
    
    /**
     * 计算从q1到当前季度的累积可用余额
     *
     * @param bizKey 业务key
     * @param currentQuarter 当前季度
     * @param balanceMap 余额Map（key为 bizKey + "@" + quarter，value为 BudgetBalance 列表）
     * @param currentBalanceList 当前季度的余额列表
     * @param bizType 业务类型
     * @return 累积可用余额
     */
    protected BigDecimal calculateTotalAmountAvailable(String bizKey, String currentQuarter,
                                                     Map<String, List<BudgetBalance>> balanceMap,
                                                     List<BudgetBalance> currentBalanceList,
                                                     String bizType) {
        // 对于非 CLAIM：直接累加 totalAmountAvailable
        // 对于 CLAIM：
        // - 如果 bizKey 以 @NAN@NAN 结尾（组织+科目），只使用 totalAmountAvailable
        // - 否则（项目或资产类型），只累积 totalAmountPayAvailableForClaim（付款额），与扣减逻辑一致
        BigDecimal totalAmountAvailable = BigDecimal.ZERO;
        BigDecimal totalAmountAvailableForClaim = BigDecimal.ZERO; // 保留变量，但不再使用
        BigDecimal totalAmountPayAvailableForClaim = BigDecimal.ZERO;
        int currentQuarterNum = getQuarterNumber(currentQuarter);
        
        // 循环累积之前季度的余额（从q1到当前季度之前）
        String[] quarters = {"q1", "q2", "q3", "q4"};
        for (int i = 0; i < currentQuarterNum - 1; i++) {
            String quarter = quarters[i];
            String bizKeyQuarterForSum = bizKey + "@" + quarter;
            List<BudgetBalance> balanceListForQuarter = balanceMap.get(bizKeyQuarterForSum);
            if (CollectionUtils.isEmpty(balanceListForQuarter)) {
                log.error("========== BudgetValidationService - bizKeyQuarterForSum={} 在 balanceMap 中找不到对应的 balance ==========", bizKeyQuarterForSum);
                throw new IllegalStateException(buildBalanceNotFoundMessage(bizKey, bizKeyQuarterForSum));
            }
            
            // 累加该季度所有BudgetBalance的amountAvailable（如果为null则忽略）
            BigDecimal quarterAmountAvailable = BigDecimal.ZERO;
            log.info("========== BudgetValidationService - 开始累加历史季度amountAvailable: bizKey={}, quarter={}, balanceListSize={} ==========",
                    bizKey, quarter, balanceListForQuarter != null ? balanceListForQuarter.size() : 0);
            for (int balanceIdx = 0; balanceIdx < (balanceListForQuarter != null ? balanceListForQuarter.size() : 0); balanceIdx++) {
                BudgetBalance balance = balanceListForQuarter.get(balanceIdx);
                BigDecimal amountAvailable = balance.getAmountAvailable();
                log.info("========== BudgetValidationService - 历史季度balance[{}]: quarter={}, poolId={}, amountAvailable={}, amountPayAvailable={} ==========",
                        balanceIdx, quarter, balance.getPoolId(), amountAvailable, balance.getAmountPayAvailable());
                // 如果amountAvailable为null，忽略该balance；如果有数字（包括0），才累加
                if (amountAvailable != null) {
                    quarterAmountAvailable = quarterAmountAvailable.add(amountAvailable);
                    log.info("========== BudgetValidationService - 累加历史季度amountAvailable: quarter={}, balance[{}]的amountAvailable={}, 累加后quarterAmountAvailable={} ==========",
                            quarter, balanceIdx, amountAvailable, quarterAmountAvailable);
                }
            }
            log.info("========== BudgetValidationService - 历史季度amountAvailable累加完成: quarter={}, quarterAmountAvailable={} ==========", quarter, quarterAmountAvailable);
            
            if ("CLAIM".equals(bizType)) {
                // 付款单：
                // - 如果 bizKey 以 @NAN@NAN 结尾（erpAssetType 和项目编码同时为空，组织+科目），则不校验 amountPayAvailable，直接按季度累加 amountAvailable
                // - 否则（项目或资产类型）：只累积 amountPayAvailable（付款额），与扣减逻辑一致
                if (bizKey.endsWith("@NAN@NAN")) {
                    totalAmountAvailable = totalAmountAvailable.add(quarterAmountAvailable);
                    log.info("========== BudgetValidationService - 累积季度余额(CLAIM, erpAssetType和项目编码同时为空): quarter={}, amountAvailable={}, 累计amountAvailable={} ==========",
                            quarter, quarterAmountAvailable, totalAmountAvailable);
                } else {
                    // 判断是否为项目查询或资产类型查询
                    // bizKey格式：bizCode@isInternal@managementOrg@budgetSubjectCode@masterProjectCode@erpAssetType
                    String[] parts = bizKey.split("@");
                    boolean isProjectQuery = parts.length >= 5 && !"NAN".equals(parts[4]); // parts[4] 是 masterProjectCode
                    boolean isAssetTypeQuery = parts.length >= 6 && !"NAN".equals(parts[5]); // parts[5] 是 erpAssetType
                    
                    // 累加该季度所有BudgetBalance的amountPayAvailable
                    BigDecimal quarterAmountPayAvailable = BigDecimal.ZERO;
                    log.info("========== BudgetValidationService - 开始累加历史季度amountPayAvailable: bizKey={}, quarter={}, balanceListSize={} ==========",
                            bizKey, quarter, balanceListForQuarter != null ? balanceListForQuarter.size() : 0);
                    for (int balanceIdx2 = 0; balanceIdx2 < (balanceListForQuarter != null ? balanceListForQuarter.size() : 0); balanceIdx2++) {
                        BudgetBalance balance = balanceListForQuarter.get(balanceIdx2);
                        BigDecimal amountPayAvailable = balance.getAmountPayAvailable() == null ? BigDecimal.ZERO : balance.getAmountPayAvailable();
                        quarterAmountPayAvailable = quarterAmountPayAvailable.add(amountPayAvailable);
                        log.info("========== BudgetValidationService - 累加历史季度amountPayAvailable: quarter={}, balance[{}]的amountPayAvailable={}, 累加后quarterAmountPayAvailable={} ==========",
                                quarter, balanceIdx2, amountPayAvailable, quarterAmountPayAvailable);
                    }
                    log.info("========== BudgetValidationService - 历史季度amountPayAvailable累加完成: quarter={}, quarterAmountPayAvailable={} ==========", quarter, quarterAmountPayAvailable);
                    
                    if (isProjectQuery || isAssetTypeQuery) {
                        // 项目查询或资产类型查询：只累积 amountPayAvailable（付款额），与扣减逻辑一致
                        // 不再检查 amountAvailable，只使用 amountPayAvailable
                        totalAmountPayAvailableForClaim = totalAmountPayAvailableForClaim.add(quarterAmountPayAvailable);
                        log.info("========== BudgetValidationService - 累积季度余额(CLAIM - 项目或资产类型查询): quarter={}, amountPayAvailable={}, 累积amountPayAvailableTotal={}, isProjectQuery={}, isAssetTypeQuery={} ==========",
                                quarter, quarterAmountPayAvailable, totalAmountPayAvailableForClaim, isProjectQuery, isAssetTypeQuery);
                    } else {
                        // 组织+科目（@NAN@NAN）：只累积 amountAvailable
                        totalAmountAvailable = totalAmountAvailable.add(quarterAmountAvailable);
                        log.info("========== BudgetValidationService - 累积季度余额(CLAIM - 组织+科目): quarter={}, amountAvailable={}, 累计amountAvailable={} ==========",
                                quarter, quarterAmountAvailable, totalAmountAvailable);
                    }
                }
            } else {
                // 其他业务类型：需要判断操作金额
                // 累加该季度所有BudgetBalance的操作金额
                BigDecimal quarterAmountOperated = getCurrentAmountOperated(balanceListForQuarter);
                
                // 如果操作金额小于0，则加上操作金额
                if (quarterAmountOperated.compareTo(BigDecimal.ZERO) < 0) {
                    totalAmountAvailable = totalAmountAvailable.add(quarterAmountAvailable).add(quarterAmountOperated);
                    log.info("========== BudgetValidationService - 累积季度余额: quarter={}, amountAvailable={}, amountOperated={}, 累计amountAvailable={} ==========",
                            quarter, quarterAmountAvailable, quarterAmountOperated, totalAmountAvailable);
                } else {
                    totalAmountAvailable = totalAmountAvailable.add(quarterAmountAvailable);
                    log.info("========== BudgetValidationService - 累积季度余额: quarter={}, amountAvailable={}, 累计amountAvailable={} ==========",
                            quarter, quarterAmountAvailable, totalAmountAvailable);
                }
            }
        }
        
        // 加上当前季度的余额
        if (CollectionUtils.isEmpty(currentBalanceList)) {
            return totalAmountAvailable;
        }
        
        // 累加当前季度所有BudgetBalance的amountAvailable（如果为null则忽略）
        BigDecimal currentAmountAvailable = BigDecimal.ZERO;
        log.info("========== BudgetValidationService - 开始累加当前季度amountAvailable: bizKey={}, currentQuarter={}, balanceListSize={} ==========",
                bizKey, currentQuarter, currentBalanceList != null ? currentBalanceList.size() : 0);
        for (int currentBalanceIdx = 0; currentBalanceIdx < (currentBalanceList != null ? currentBalanceList.size() : 0); currentBalanceIdx++) {
            BudgetBalance balance = currentBalanceList.get(currentBalanceIdx);
            BigDecimal amountAvailable = balance.getAmountAvailable();
            log.info("========== BudgetValidationService - 当前季度balance[{}]: poolId={}, amountAvailable={}, amountPayAvailable={} ==========",
                    currentBalanceIdx, balance.getPoolId(), amountAvailable, balance.getAmountPayAvailable());
            // 如果amountAvailable为null，忽略该balance；如果有数字（包括0），才累加
            if (amountAvailable != null) {
                currentAmountAvailable = currentAmountAvailable.add(amountAvailable);
                log.info("========== BudgetValidationService - 累加amountAvailable: balance[{}]的amountAvailable={}, 累加后currentAmountAvailable={} ==========",
                        currentBalanceIdx, amountAvailable, currentAmountAvailable);
            }
        }
        log.info("========== BudgetValidationService - 当前季度amountAvailable累加完成: currentAmountAvailable={} ==========", currentAmountAvailable);
        
        if ("CLAIM".equals(bizType)) {
            if (bizKey.endsWith("@NAN@NAN")) {
                // erpAssetType 和项目编码同时为空：所有季度都只看 amountAvailable，逐季累加
                totalAmountAvailable = totalAmountAvailable.add(currentAmountAvailable);
                log.info("========== BudgetValidationService - 加上当前季度余额(CLAIM, erpAssetType和项目编码同时为空): amountAvailable={}, 累计amountAvailable={} ==========",
                        currentAmountAvailable, totalAmountAvailable);
            } else {
                // 非 @NAN@NAN：判断是否为项目查询或资产类型查询
                // bizKey格式：bizCode@isInternal@managementOrg@budgetSubjectCode@masterProjectCode@erpAssetType
                String[] parts = bizKey.split("@");
                boolean isProjectQuery = parts.length >= 5 && !"NAN".equals(parts[4]); // parts[4] 是 masterProjectCode
                boolean isAssetTypeQuery = parts.length >= 6 && !"NAN".equals(parts[5]); // parts[5] 是 erpAssetType
                
                // 累加当前季度所有BudgetBalance的amountPayAvailable
                BigDecimal currentAmountPayAvailable = BigDecimal.ZERO;
                log.info("========== BudgetValidationService - 开始累加当前季度amountPayAvailable: bizKey={}, currentQuarter={}, balanceListSize={} ==========",
                        bizKey, currentQuarter, currentBalanceList != null ? currentBalanceList.size() : 0);
                for (int currentBalanceIdx2 = 0; currentBalanceIdx2 < (currentBalanceList != null ? currentBalanceList.size() : 0); currentBalanceIdx2++) {
                    BudgetBalance balance = currentBalanceList.get(currentBalanceIdx2);
                    BigDecimal amountPayAvailable = balance.getAmountPayAvailable() == null ? BigDecimal.ZERO : balance.getAmountPayAvailable();
                    currentAmountPayAvailable = currentAmountPayAvailable.add(amountPayAvailable);
                    log.info("========== BudgetValidationService - 累加amountPayAvailable: balance[{}]的amountPayAvailable={}, 累加后currentAmountPayAvailable={} ==========",
                            currentBalanceIdx2, amountPayAvailable, currentAmountPayAvailable);
                }
                log.info("========== BudgetValidationService - 当前季度amountPayAvailable累加完成: currentAmountPayAvailable={} ==========", currentAmountPayAvailable);
                
                if (isProjectQuery || isAssetTypeQuery) {
                    // 项目查询或资产类型查询：只累积 amountPayAvailable（付款额），与扣减逻辑一致
                    // 不再检查 amountAvailable，只使用 amountPayAvailable
                    totalAmountPayAvailableForClaim = totalAmountPayAvailableForClaim.add(currentAmountPayAvailable);
                    totalAmountAvailable = totalAmountPayAvailableForClaim;
                    log.info("========== BudgetValidationService - 加上当前季度余额(CLAIM, 项目或资产类型查询): 当前quarterAmountPayAvailable={}, 累积amountPayAvailableTotal={}, isProjectQuery={}, isAssetTypeQuery={} ==========",
                            currentAmountPayAvailable, totalAmountAvailable, isProjectQuery, isAssetTypeQuery);
                } else {
                    // 组织+科目（@NAN@NAN）：只累积 amountAvailable
                    totalAmountAvailable = totalAmountAvailable.add(currentAmountAvailable);
                    log.info("========== BudgetValidationService - 加上当前季度余额(CLAIM, 组织+科目): amountAvailable={}, 累计amountAvailable={} ==========",
                            currentAmountAvailable, totalAmountAvailable);
                }
            }
        } else {
            totalAmountAvailable = totalAmountAvailable.add(currentAmountAvailable);
            log.info("========== BudgetValidationService - 加上当前季度余额: amountAvailable={}, 累计amountAvailable={} ==========",
                    currentAmountAvailable, totalAmountAvailable);
        }
        
        return totalAmountAvailable;
    }
    
    /**
     * 自定义计算从q1到当前季度的累积可用余额
     * 子类可以重写此方法来实现自定义的计算逻辑
     *
     * @param bizKey 业务key
     * @param currentQuarter 当前季度
     * @param balanceMap 余额Map（key为 bizKey + "@" + quarter，value为 BudgetBalance 列表）
     * @param currentBalanceList 当前季度的余额列表
     * @param customAmount 自定义金额
     * @param relatedBizType 关联业务类型
     * @param bizType 业务类型
     * @return 累积可用余额
     */
    protected BigDecimal calculateTotalAmountAvailableCustom(String bizKey, 
                                                             String currentQuarter,
                                                             Map<String, List<BudgetBalance>> balanceMap,
                                                             List<BudgetBalance> currentBalanceList,
                                                             BigDecimal customAmount,
                                                             String relatedBizType,
                                                             String bizType,
                                                             boolean claimFrameworkContract) {
        if (claimFrameworkContract && "CLAIM".equals(bizType) && "CONTRACT".equals(relatedBizType)) {
            BigDecimal contractPart = customAmount == null ? BigDecimal.ZERO : customAmount;
            BigDecimal poolPart = calculateTotalAmountAvailable(bizKey, currentQuarter, balanceMap, currentBalanceList, "CLAIM");
            BigDecimal sum = contractPart.add(poolPart);
            log.info("========== BudgetValidationService - 框架协议(CLAIM+CONTRACT): 校验上限=合同可用+资金池累计(付款口径): contract={}, pool={}, sum={} ==========",
                    contractPart, poolPart, sum);
            return sum;
        }
        BigDecimal totalAmountAvailable = customAmount == null ? BigDecimal.ZERO : customAmount;
        BigDecimal middleAmount = BigDecimal.ZERO; // 保留给其他场景使用
        // CLAIM + CONTRACT 使用以下两个变量分别累积全年的可支付金额和已占用金额
        BigDecimal totalAmountPayAvailableForClaimContract = BigDecimal.ZERO;
        BigDecimal totalAmountOccupiedForClaimContract = BigDecimal.ZERO;
        // CLAIM + APPLY 使用以下两个变量分别累积全年的可支付金额和冻结金额
        BigDecimal totalAmountPayAvailableForClaimApply = BigDecimal.ZERO;
        BigDecimal totalAmountFrozenForClaimApply = BigDecimal.ZERO;
        int currentQuarterNum = getQuarterNumber(currentQuarter);
        
        // 循环累积之前季度中操作金额小于0的金额（从q1到当前季度之前）
        String[] quarters = {"q1", "q2", "q3", "q4"};
        for (int i = 0; i < currentQuarterNum - 1; i++) {
            String quarter = quarters[i];
            String bizKeyQuarterForSum = bizKey + "@" + quarter;
            List<BudgetBalance> balanceListForQuarter = balanceMap.get(bizKeyQuarterForSum);
            if (CollectionUtils.isEmpty(balanceListForQuarter)) {
                log.error("========== BudgetValidationService - bizKeyQuarterForSum={} 在 balanceMap 中找不到对应的 balance ==========", bizKeyQuarterForSum);
                throw new IllegalStateException(buildBalanceNotFoundMessage(bizKey, bizKeyQuarterForSum));
            }
            
            if ("CLAIM".equals(bizType) && "CONTRACT".equals(relatedBizType)) {
                // 付款单 + 关联合同单：
                // - amountPayAvailable 需要从 q1 开始向后累积到当前季度
                // - 最终在当前季度时，关联单据的可用余额 和 资金池的付款额，取较小值
                // 累加该季度所有BudgetBalance的amountOccupied（仅用于日志记录，不参与校验）
                BigDecimal quarterAmountOccupied = BigDecimal.ZERO;
                for (BudgetBalance balance : balanceListForQuarter) {
                    BigDecimal amountOccupied = balance.getAmountOccupied() == null ? BigDecimal.ZERO : balance.getAmountOccupied();
                    quarterAmountOccupied = quarterAmountOccupied.add(amountOccupied);
                }
                
                if (bizKey.endsWith("@NAN@NAN")) {
                    // erpAssetType 和项目编码同时为空，不校验 amountPayAvailable，只累积已占用金额
                    totalAmountOccupiedForClaimContract = totalAmountOccupiedForClaimContract.add(quarterAmountOccupied);
                    log.info("========== BudgetValidationService - 自定义计算累积季度余额(CLAIM+CONTRACT, erpAssetType和项目编码同时为空): quarter={}, amountOccupied={}, 累积amountOccupiedTotal={} ==========",
                            quarter, quarterAmountOccupied, totalAmountOccupiedForClaimContract);
                } else {
                    // 累加该季度所有BudgetBalance的amountPayAvailable
                    BigDecimal quarterAmountPayAvailable = BigDecimal.ZERO;
                    for (BudgetBalance balance : balanceListForQuarter) {
                        BigDecimal amountPayAvailable = balance.getAmountPayAvailable() == null ? BigDecimal.ZERO : balance.getAmountPayAvailable();
                        quarterAmountPayAvailable = quarterAmountPayAvailable.add(amountPayAvailable);
                    }
                    totalAmountPayAvailableForClaimContract = totalAmountPayAvailableForClaimContract.add(quarterAmountPayAvailable);
                    totalAmountOccupiedForClaimContract = totalAmountOccupiedForClaimContract.add(quarterAmountOccupied);
                    log.info("========== BudgetValidationService - 自定义计算累积季度余额(CLAIM+CONTRACT - 累积中): quarter={}, amountPayAvailable={}, amountOccupied={}, 累积amountPayAvailableTotal={}, 累积amountOccupiedTotal={} ==========",
                            quarter, quarterAmountPayAvailable, quarterAmountOccupied,
                            totalAmountPayAvailableForClaimContract, totalAmountOccupiedForClaimContract);
                }
            } else if ("CLAIM".equals(bizType) && "APPLY".equals(relatedBizType)) {
                // 付款单 + 关联申请单：
                // - amountPayAvailable 需要从 q1 开始向后累积到当前季度
                // - 最终在当前季度时，关联单据的可用余额 和 资金池的付款额，取较小值
                // 累加该季度所有BudgetBalance的amountFrozen（仅用于日志记录，不参与校验）
                BigDecimal quarterAmountFrozen = BigDecimal.ZERO;
                for (BudgetBalance balance : balanceListForQuarter) {
                    BigDecimal amountFrozen = balance.getAmountFrozen() == null ? BigDecimal.ZERO : balance.getAmountFrozen();
                    quarterAmountFrozen = quarterAmountFrozen.add(amountFrozen);
                }
                
                if (bizKey.endsWith("@NAN@NAN")) {
                    // erpAssetType 和项目编码同时为空，不校验 amountPayAvailable，只累积冻结金额
                    totalAmountFrozenForClaimApply = totalAmountFrozenForClaimApply.add(quarterAmountFrozen);
                    log.info("========== BudgetValidationService - 自定义计算累积季度余额(CLAIM+APPLY, erpAssetType和项目编码同时为空): quarter={}, amountFrozen={}, 累积amountFrozenTotal={} ==========",
                            quarter, quarterAmountFrozen, totalAmountFrozenForClaimApply);
                } else {
                    // 累加该季度所有BudgetBalance的amountPayAvailable
                    BigDecimal quarterAmountPayAvailable = BigDecimal.ZERO;
                    for (BudgetBalance balance : balanceListForQuarter) {
                        BigDecimal amountPayAvailable = balance.getAmountPayAvailable() == null ? BigDecimal.ZERO : balance.getAmountPayAvailable();
                        quarterAmountPayAvailable = quarterAmountPayAvailable.add(amountPayAvailable);
                    }
                    totalAmountPayAvailableForClaimApply = totalAmountPayAvailableForClaimApply.add(quarterAmountPayAvailable);
                    totalAmountFrozenForClaimApply = totalAmountFrozenForClaimApply.add(quarterAmountFrozen);
                    log.info("========== BudgetValidationService - 自定义计算累积季度余额(CLAIM+APPLY - 累积中): quarter={}, amountPayAvailable={}, amountFrozen={}, 累积amountPayAvailableTotal={}, 累积amountFrozenTotal={} ==========",
                            quarter, quarterAmountPayAvailable, quarterAmountFrozen,
                            totalAmountPayAvailableForClaimApply, totalAmountFrozenForClaimApply);
                }
            } else if (!"CLAIM".equals(bizType)) {
                // 非付款单：只累加操作金额小于0的金额
                // 累加该季度所有BudgetBalance的操作金额
                BigDecimal quarterAmountOperated = getCurrentAmountOperated(balanceListForQuarter);
                
                // 如果操作金额小于0，则加上操作金额
                if (quarterAmountOperated.compareTo(BigDecimal.ZERO) < 0) {
                    totalAmountAvailable = totalAmountAvailable.add(quarterAmountOperated);
                    log.info("========== BudgetValidationService - 自定义计算累积季度余额: quarter={}, amountOperated={}, 累计amountAvailable={} ==========",
                            quarter, quarterAmountOperated, totalAmountAvailable);
                }
            }
            // 付款单但不是关联合同单/申请单：不累加，只使用 customAmount
        }
        
        // 如果是 CLAIM + CONTRACT，还要把当前季度纳入全年累积，然后在这里统一比较三方的累积值
        if (CollectionUtils.isEmpty(currentBalanceList)) {
            return totalAmountAvailable;
        }
        
        if ("CLAIM".equals(bizType) && "CONTRACT".equals(relatedBizType)) {
            // 关联合同时：除校验关联单据可用余额外，项目/资产类型还需同时校验并扣减资金池付款额，取两者较小值作为可用预算
            if (bizKey.endsWith("@NAN@NAN")) {
                log.info("========== BudgetValidationService - 自定义计算(CLAIM+CONTRACT, 组织+科目): 只使用关联单据可用余额: 关联单据可用余额={} ==========",
                        totalAmountAvailable);
            } else {
                BigDecimal currentQuarterPayAvailable = BigDecimal.ZERO;
                for (BudgetBalance b : currentBalanceList) {
                    currentQuarterPayAvailable = currentQuarterPayAvailable.add(b.getAmountPayAvailable() == null ? BigDecimal.ZERO : b.getAmountPayAvailable());
                }
                BigDecimal totalPayAvailable = totalAmountPayAvailableForClaimContract.add(currentQuarterPayAvailable);
                totalAmountAvailable = totalAmountAvailable.min(totalPayAvailable);
                log.info("========== BudgetValidationService - 自定义计算(CLAIM+CONTRACT, 项目/资产): 关联单据可用余额与资金池付款额取较小值: 关联单据可用余额={}, 资金池付款额累积={}, 最终可用={} ==========",
                        customAmount, totalPayAvailable, totalAmountAvailable);
            }
        } else if ("CLAIM".equals(bizType) && "APPLY".equals(relatedBizType)) {
            // 关联申请单时：除校验关联单据可用余额外，项目/资产类型还需同时校验并扣减资金池付款额，取两者较小值作为可用预算
            if (bizKey.endsWith("@NAN@NAN")) {
                log.info("========== BudgetValidationService - 自定义计算(CLAIM+APPLY, 组织+科目): 只使用关联单据可用余额: 关联单据可用余额={} ==========",
                        totalAmountAvailable);
            } else {
                BigDecimal currentQuarterPayAvailable = BigDecimal.ZERO;
                for (BudgetBalance b : currentBalanceList) {
                    currentQuarterPayAvailable = currentQuarterPayAvailable.add(b.getAmountPayAvailable() == null ? BigDecimal.ZERO : b.getAmountPayAvailable());
                }
                BigDecimal totalPayAvailable = totalAmountPayAvailableForClaimApply.add(currentQuarterPayAvailable);
                totalAmountAvailable = totalAmountAvailable.min(totalPayAvailable);
                log.info("========== BudgetValidationService - 自定义计算(CLAIM+APPLY, 项目/资产): 关联单据可用余额与资金池付款额取较小值: 关联单据可用余额={}, 资金池付款额累积={}, 最终可用={} ==========",
                        customAmount, totalPayAvailable, totalAmountAvailable);
            }
        }
        
        return totalAmountAvailable;
    }
    
    /**
     * 校验预算是否充足
     */
    private void validateBudget(String bizKey,
                               BigDecimal totalAmountAvailable,
                               BigDecimal currentAmountOperated,
                               BigDecimal newAmountOperateConsumed,
                               BigDecimal originalAmount,
                               String bizItemCode,
                               String bizType,
                               Map<String, BigDecimal> compensationMap,
                               String year,
                               String currentQuarter,
                               BudgetLedger ledger,
                               boolean contractUseRelatedOnlyValidation) {
        // 处理null值
        BigDecimal totalAvailable = totalAmountAvailable == null ? BigDecimal.ZERO : totalAmountAvailable;
        BigDecimal currentOperated = currentAmountOperated == null ? BigDecimal.ZERO : currentAmountOperated;
        BigDecimal newOperated = newAmountOperateConsumed == null ? BigDecimal.ZERO : newAmountOperateConsumed;
        BigDecimal original = originalAmount == null ? BigDecimal.ZERO : originalAmount;
        
        boolean budgetInsufficient = false;
        
        if ("CLAIM".equals(bizType) || contractUseRelatedOnlyValidation) {
            // 付款单：直接判断 totalAvailable >= newOperated
            if (totalAvailable.compareTo(newOperated) < 0) {
                budgetInsufficient = true;
            }

            if (contractUseRelatedOnlyValidation) {
                log.info("========== BudgetValidationService - 合同单命中来源口径校验: bizKey={}, totalAvailable={}, newOperated={} ==========",
                        bizKey, totalAvailable, newOperated);
            }
            
            if (budgetInsufficient && !isSkipBudgetValidation()) {
                log.error("========== BudgetValidationService - 付款预算不足: bizKey={}, 累计amountAvailable={}, 原始付款金额={}, 调整后需要付款={} ==========",
                        bizKey, totalAvailable, original, newOperated);
                
                // 如果原始金额和调整后的金额不同，说明有之前季度的负操作金额需要补偿
                if (original.compareTo(newOperated) != 0 && compensationMap != null && !compensationMap.isEmpty()) {
                    BigDecimal compensationAmount = newOperated.subtract(original);
                    // 解析bizKey获取维度信息
                    String dimensionInfo = parseBizKeyToDimensionInfo(bizKey);
                    // 构建补偿说明：显示哪些季度有负操作金额
                    StringBuilder compensationDesc = new StringBuilder();
                    for (Map.Entry<String, BigDecimal> entry : compensationMap.entrySet()) {
                        String quarter = entry.getKey();
                        BigDecimal negativeAmount = entry.getValue();
                        if (compensationDesc.length() > 0) {
                            compensationDesc.append("，");
                        }
                        compensationDesc.append(String.format("季度%s负操作金额=%s", quarter, negativeAmount));
                    }
                    throw new IllegalStateException(
                        String.format("明细 [%s] 付款预算不足。原始付款金额=%s，因维度[%s]的%s需要补偿=%s，实际需要付款=%s，累计可用预算=%s，缺口=%s",
                                     bizKey, original, dimensionInfo, compensationDesc.toString(), compensationAmount, newOperated, totalAvailable, newOperated.subtract(totalAvailable)));
                } else if (original.compareTo(newOperated) != 0) {
                    BigDecimal compensationAmount = newOperated.subtract(original);
                    throw new IllegalStateException(
                        String.format("明细 [%s] 付款预算不足。原始付款金额=%s，因之前季度有负操作金额需要补偿=%s，实际需要付款=%s，累计可用预算=%s，缺口=%s",
                                     bizKey, original, compensationAmount, newOperated, totalAvailable, newOperated.subtract(totalAvailable)));
                } else {
                    String[] bizKeyParts = bizKey.split("@");
                    String managementOrg = bizKeyParts.length > 2 ? bizKeyParts[2] : "未知";
                    String budgetSubjectCode = bizKeyParts.length > 3 ? bizKeyParts[3] : "未知";
                    String masterProjectCode = bizKeyParts.length > 4 ? bizKeyParts[4] : "未知";
                    String erpAssetType = bizKeyParts.length > 5 ? bizKeyParts[5] : "未知";
                    // 在异常消息中包含明细标识，格式：明细 [bizKey]，以便 tryExtractDetailError 能识别
                    throw new IllegalStateException(
                        String.format("明细 [%s] 可用付款预算额不足,维度信息:年度=%s,季度=%s,管理组织=%s,预算科目=%s,项目=%s,资产类型=%s,累计可用预算:%s Available Balance Insufficient",
                                     bizKey, year, currentQuarter, managementOrg, budgetSubjectCode, masterProjectCode, erpAssetType, totalAvailable));
                }
            }
        } else {
            // 申请单/合同单：使用复杂的校验逻辑
            BigDecimal operatedAfter = currentOperated.add(newOperated);
            
            if (currentOperated.compareTo(BigDecimal.ZERO) >= 0) {
                // currentAmountOperated >= 0，判断 totalAmountAvailable >= newAmountOperateConsumed
                if (totalAvailable.compareTo(newOperated) < 0) {
                    budgetInsufficient = true;
                }
            } else if (operatedAfter.compareTo(BigDecimal.ZERO) >= 0) {
                // currentAmountOperated < 0 但 currentAmountOperated + newAmountOperateConsumed >= 0
                // 判断 totalAmountAvailable >= (currentAmountOperated + newAmountOperateConsumed)
                if (totalAvailable.compareTo(operatedAfter) < 0) {
                    budgetInsufficient = true;
                }
            } else {
                // currentAmountOperated + newAmountOperateConsumed < 0
                // 判断 totalAmountAvailable + (currentAmountOperated + newAmountOperateConsumed) >= 0
                if (totalAvailable.add(operatedAfter).compareTo(BigDecimal.ZERO) < 0) {
                    budgetInsufficient = true;
                }
            }
            
            if (budgetInsufficient && !isSkipBudgetValidation()) {
                log.error("========== BudgetValidationService - 预算不足，操作失败: bizKey={}, 累计amountAvailable={}, currentAmountOperated={}, operatedAfter={}, 需要操作={} ==========",
                        bizKey, totalAvailable, currentOperated, operatedAfter, newOperated);
                String[] bizKeyParts = bizKey.split("@");
                String bizCode = bizKeyParts.length > 0 ? bizKeyParts[0] : "未知";
                String managementOrg = bizKeyParts.length > 2 ? bizKeyParts[2] : "未知";
                String budgetSubjectCode = bizKeyParts.length > 3 ? bizKeyParts[3] : "未知";
                String masterProjectCode = bizKeyParts.length > 4 ? bizKeyParts[4] : "未知";
                String erpAssetType = bizKeyParts.length > 5 ? bizKeyParts[5] : "未知";
                // 在异常消息中包含明细标识，格式：明细 [bizKey]，以便 tryExtractDetailError 能识别
                throw new IllegalStateException(
                    String.format("明细 [%s] 可用余额不足,维度信息:年度=%s,季度=%s,管理组织=%s,预算科目=%s,项目=%s,资产类型=%s,累计可用余额=%s Available Balance Insufficient",
                                 bizKey, year, currentQuarter, managementOrg, budgetSubjectCode, masterProjectCode, erpAssetType, totalAvailable));
            }
        }
        
        log.info("========== BudgetValidationService - 预算校验通过 Verification Passed: bizKey={} ==========", bizKey);
    }
    
    /**
     * 解析bizKey，提取维度信息用于错误提示
     * bizKey格式：bizCode@isInternal@managementOrg@budgetSubjectCode@masterProjectCode@erpAssetType
     * 
     * @param bizKey 业务key
     * @return 维度信息字符串，格式：管理组织=xxx,预算科目=xxx,项目编码=xxx,资产类型=xxx
     */
    private String parseBizKeyToDimensionInfo(String bizKey) {
        if (StringUtils.isBlank(bizKey)) {
            return "未知维度";
        }
        
        String[] parts = bizKey.split("@");
        if (parts.length < 6) {
            return bizKey; // 如果格式不对，直接返回原始bizKey
        }
        
        // parts[0] = bizCode
        // parts[1] = isInternal
        // parts[2] = managementOrg
        // parts[3] = budgetSubjectCode
        // parts[4] = masterProjectCode
        // parts[5] = erpAssetType
        
        String managementOrg = parts.length > 2 ? parts[2] : "未知";
        String budgetSubjectCode = parts.length > 3 ? parts[3] : "未知";
        String masterProjectCode = parts.length > 4 ? parts[4] : "未知";
        String erpAssetType = parts.length > 5 ? parts[5] : "未知";
        
        StringBuilder dimensionInfo = new StringBuilder();
        dimensionInfo.append("管理组织=").append(managementOrg);
        dimensionInfo.append(",预算科目=").append(budgetSubjectCode);
        
        if (!"NAN".equals(masterProjectCode)) {
            dimensionInfo.append(",项目编码=").append(masterProjectCode);
        }
        
        if (!"NAN".equals(erpAssetType)) {
            dimensionInfo.append(",资产类型=").append(erpAssetType);
        }
        
        return dimensionInfo.toString();
    }
    
    /**
     * 从BudgetLedger中提取维度信息用于错误提示
     * 
     * @param ledger 预算流水对象
     * @return 维度信息字符串，格式：管理组织=xxx,预算科目=xxx,项目编码=xxx,资产类型=xxx
     */
    private String extractDimensionInfoFromLedger(BudgetLedger ledger) {
        if (ledger == null) {
            return "未知维度";
        }
        
        String managementOrg = StringUtils.isNotBlank(ledger.getMorgCode()) ? ledger.getMorgCode() : "未知";
        String budgetSubjectCode = StringUtils.isNotBlank(ledger.getBudgetSubjectCode()) ? ledger.getBudgetSubjectCode() : "未知";
        String masterProjectCode = ledger.getMasterProjectCode();
        String erpAssetType = ledger.getErpAssetType();
        
        StringBuilder dimensionInfo = new StringBuilder();
        dimensionInfo.append("管理组织=").append(managementOrg);
        dimensionInfo.append(",预算科目=").append(budgetSubjectCode);
        
        if (StringUtils.isNotBlank(masterProjectCode) && !"NAN".equals(masterProjectCode)) {
            dimensionInfo.append(",项目编码=").append(masterProjectCode);
        }
        
        if (StringUtils.isNotBlank(erpAssetType) && !"NAN".equals(erpAssetType)) {
            dimensionInfo.append(",资产类型=").append(erpAssetType);
        }
        
        return dimensionInfo.toString();
    }
    
    /**
     * 执行跨季度扣减计算
     * 子类需要实现此方法，因为不同子类影响的字段不同（如 APPLY 影响 amountFrozen，CONTRACT 影响 amountOccupied）
     *
     * @param bizKey 业务key（bizCode + "@" + bizItemCode）
     * @param currentLedgerId 当前单据的ID（用于记录扣减金额）
     * @param bizKey 业务键（bizCode + "@" + bizItemCode）
     * @param currentQuarter 当前季度（q1、q2、q3、q4）
     * @param totalAmountToOperate 需要操作的总金额
     * @param balanceMap 季度余额Map（key为 bizKey + "@" + quarter，value为 BudgetBalance）
     * @param quarterOperateAmountMap 输出参数：每个季度操作的金额（key为季度，value为操作金额）
     * @param updatedRelatedBudgetLedgerMap 更新后的关联预算流水Map，key为bizCode + "@" + bizItemCode，value为关联的BudgetLedger列表
     * @param relatedLedgerDeductionAmountMap 输出参数：每个关联单据的扣减金额（按季度），key格式：ledgerId + "@" + relatedLedgerId + "@" + quarter，value为扣减金额
     */
    protected abstract void performMultiQuarterDeduction(Long currentLedgerId, String bizKey, String currentQuarter, 
                                                      BigDecimal totalAmountToOperate,
                                                      Map<String, List<BudgetBalance>> balanceMap,
                                                      Map<String, BudgetBalance> needToUpdateSameDemBudgetBalanceMap,
                                                      Map<String, BigDecimal> quarterOperateAmountMap,
                                                      Map<String, List<BudgetLedger>> updatedRelatedBudgetLedgerMap,
                                                      Map<String, BigDecimal> relatedLedgerDeductionAmountMap);
    
    /**
     * 将各季度的操作金额记录到 ledger 的对应季度字段中
     * 注意：这里是累加到现有值，而不是覆盖，因为季度字段应该记录累积的操作金额（用于回滚计算）
     *
     * @param ledger BudgetLedger对象
     * @param q1Amount 第一季度操作金额（本次操作分配到q1的金额）
     * @param q2Amount 第二季度操作金额（本次操作分配到q2的金额）
     * @param q3Amount 第三季度操作金额（本次操作分配到q3的金额）
     * @param q4Amount 第四季度操作金额（本次操作分配到q4的金额）
     */
    protected void setQuarterAmountToLedger(BudgetLedger ledger, 
                                           BigDecimal q1Amount, 
                                           BigDecimal q2Amount, 
                                           BigDecimal q3Amount, 
                                           BigDecimal q4Amount) {
        // 累加到现有值，而不是覆盖（因为季度字段应该记录累积的操作金额，用于回滚计算）
        BigDecimal existingQ1 = ledger.getAmountConsumedQOne() == null ? BigDecimal.ZERO : ledger.getAmountConsumedQOne();
        BigDecimal existingQ2 = ledger.getAmountConsumedQTwo() == null ? BigDecimal.ZERO : ledger.getAmountConsumedQTwo();
        BigDecimal existingQ3 = ledger.getAmountConsumedQThree() == null ? BigDecimal.ZERO : ledger.getAmountConsumedQThree();
        BigDecimal existingQ4 = ledger.getAmountConsumedQFour() == null ? BigDecimal.ZERO : ledger.getAmountConsumedQFour();
        
        ledger.setAmountConsumedQOne(existingQ1.add(q1Amount));
        ledger.setAmountConsumedQTwo(existingQ2.add(q2Amount));
        ledger.setAmountConsumedQThree(existingQ3.add(q3Amount));
        ledger.setAmountConsumedQFour(existingQ4.add(q4Amount));
    }

    /**
     * 将各季度操作金额覆盖写回到 ledger 的季度字段。
     * 用于同维度更新场景，避免在 APPROVED_UPDATE 时把历史已写入值再次累加。
     */
    protected void setQuarterAmountToLedgerOverwrite(BudgetLedger ledger,
                                                     BigDecimal q1Amount,
                                                     BigDecimal q2Amount,
                                                     BigDecimal q3Amount,
                                                     BigDecimal q4Amount) {
        ledger.setAmountConsumedQOne(q1Amount == null ? BigDecimal.ZERO : q1Amount);
        ledger.setAmountConsumedQTwo(q2Amount == null ? BigDecimal.ZERO : q2Amount);
        ledger.setAmountConsumedQThree(q3Amount == null ? BigDecimal.ZERO : q3Amount);
        ledger.setAmountConsumedQFour(q4Amount == null ? BigDecimal.ZERO : q4Amount);
    }
    
    /**
     * 批量更新数据库
     *
     * @param needToUpdateDiffDemBudgetBalanceMap 需要更新的 BudgetBalance Map（维度不一致）
     * @param needToUpdateDiffDemBudgetQuotaMap 需要更新的 BudgetQuota Map（维度不一致）
     * @param needToUpdateSameDemBudgetBalanceMap 需要更新的 BudgetBalance Map（维度一致）
     * @param needToUpdateSameDemBudgetQuotaMap 需要更新的 BudgetQuota Map（维度一致）
     * @param needToAddBudgetBalanceHistory 需要新增的 BudgetBalanceHistory 列表
     * @param needToAddBudgetQuotaHistory 需要新增的 BudgetQuotaHistory 列表
     * @param existingBudgetLedgerMap 已存在的 BudgetLedger Map
     * @param needUpdateSameDemBudgetLedgerMap 需要更新的 BudgetLedger Map（维度一致）
     * @param needToAddBudgetLedgerMap 需要新增的 BudgetLedger Map
     * @param updatedRelatedBudgetLedgerMap 更新后的关联预算流水Map，key为bizCode + "@" + bizItemCode，value为关联的BudgetLedger列表
     * @param operator 操作人
     * @param operatorNo 操作人工号
     * @param requestTime ESB 请求时间，用于 BUDGET_LEDGER/BUDGET_LEDGER_HEAD 的 CREATE_TIME/UPDATE_TIME，可为 null
     */
    protected void batchUpdateDatabase(Map<String, BudgetBalance> needToUpdateDiffDemBudgetBalanceMap,
                                       Map<String, BudgetQuota> needToUpdateDiffDemBudgetQuotaMap,
                                       Map<String, BudgetBalance> needToUpdateSameDemBudgetBalanceMap,
                                       Map<String, BudgetQuota> needToUpdateSameDemBudgetQuotaMap,
                                       List<BudgetBalanceHistory> needToAddBudgetBalanceHistory,
                                       List<BudgetQuotaHistory> needToAddBudgetQuotaHistory,
                                       Map<String, BudgetLedger> existingBudgetLedgerMap,
                                       Map<String, BudgetLedger> needUpdateSameDemBudgetLedgerMap,
                                       Map<String, BudgetLedger> needToAddBudgetLedgerMap,
                                       Map<String, List<BudgetLedger>> updatedRelatedBudgetLedgerMap,
                                       Map<String, BudgetPoolDemR> needToAddPoolDemRMap,
                                       Map<String, BigDecimal> relatedLedgerDeductionAmountMap,
                                       Set<Long> needToDeleteOldLedgerIds,
                                       String operator,
                                       String operatorNo,
                                       LocalDateTime requestTime) {
        // 0. 先插入新创建的 BudgetPoolDemR（必须在 BudgetQuota 和 BudgetBalance 之前插入）
        if (needToAddPoolDemRMap != null && !needToAddPoolDemRMap.isEmpty()) {
            List<BudgetPoolDemR> poolDemRsToAdd = new ArrayList<>(needToAddPoolDemRMap.values());
            // BaseMapperX 可能没有 insertBatch，使用循环插入
            for (BudgetPoolDemR poolDemR : poolDemRsToAdd) {
                budgetPoolDemRMapper.insert(poolDemR);
            }
            log.info("========== 批量插入 BudgetPoolDemR: {} 条 ==========", poolDemRsToAdd.size());
        }
        
        // 区分新创建的 BudgetQuota 和 BudgetBalance（需要 insert）和已存在的（需要 update）
        // 通过检查 needToAddPoolDemRMap 来判断哪些是新创建的
        Set<String> newCreatedBizKeyQuarters = needToAddPoolDemRMap != null ? 
                new HashSet<>(needToAddPoolDemRMap.keySet()) : Collections.emptySet();
        
        List<BudgetQuota> newQuotasToInsert = new ArrayList<>();
        List<BudgetQuota> existingQuotasToUpdate = new ArrayList<>();
        List<BudgetBalance> newBalancesToInsert = new ArrayList<>();
        List<BudgetBalance> existingBalancesToUpdate = new ArrayList<>();
        
        // 分离新创建的 BudgetQuota（已存在的按 id 去重，避免多组织复用同池时同一 Quota 被重复加入列表）
        Set<Long> existingQuotaIds = new HashSet<>();
        for (Map.Entry<String, BudgetQuota> entry : needToUpdateSameDemBudgetQuotaMap.entrySet()) {
            if (newCreatedBizKeyQuarters.contains(entry.getKey())) {
                newQuotasToInsert.add(entry.getValue());
            } else {
                BudgetQuota q = entry.getValue();
                if (q != null && q.getId() != null && existingQuotaIds.add(q.getId())) {
                    existingQuotasToUpdate.add(q);
                }
            }
        }
        
        // 分离新创建的 BudgetBalance（已存在的按 id 去重，避免多组织复用同池时同一 Balance 被重复加入列表）
        Set<Long> existingBalanceIds = new HashSet<>();
        for (Map.Entry<String, BudgetBalance> entry : needToUpdateSameDemBudgetBalanceMap.entrySet()) {
            if (newCreatedBizKeyQuarters.contains(entry.getKey())) {
                newBalancesToInsert.add(entry.getValue());
            } else {
                BudgetBalance b = entry.getValue();
                if (b != null && b.getId() != null && existingBalanceIds.add(b.getId())) {
                    existingBalancesToUpdate.add(b);
                }
            }
        }
        
        // 1. 更新 BUDGET_BALANCE（维度不一致，按 balanceId 去重）
        if (!needToUpdateDiffDemBudgetBalanceMap.isEmpty()) {
            Map<Long, BudgetBalance> uniqueDiffBalances = new LinkedHashMap<>();
            for (BudgetBalance b : needToUpdateDiffDemBudgetBalanceMap.values()) {
                if (b != null && b.getId() != null) {
                    uniqueDiffBalances.putIfAbsent(b.getId(), b);
                }
            }
            List<BudgetBalance> balancesToUpdate = sortBalancesById(new ArrayList<>(uniqueDiffBalances.values()));
            budgetBalanceMapper.updateBatchById(balancesToUpdate);
        }

        // 2. 更新 BUDGET_QUOTA（维度不一致）
        if (!needToUpdateDiffDemBudgetQuotaMap.isEmpty()) {
            List<BudgetQuota> quotasToUpdate = sortQuotasById(new ArrayList<>(needToUpdateDiffDemBudgetQuotaMap.values()));
            budgetQuotaMapper.updateBatchById(quotasToUpdate);
        }

        // 3. 插入新创建的 BUDGET_QUOTA（维度一致）
        if (!newQuotasToInsert.isEmpty()) {
            budgetQuotaMapper.insertBatch(newQuotasToInsert);
            log.info("========== 批量插入新创建的 BudgetQuota: {} 条 ==========", newQuotasToInsert.size());
        }
        
        // 4. 更新已存在的 BUDGET_QUOTA（维度一致）
        if (!existingQuotasToUpdate.isEmpty()) {
            List<BudgetQuota> sortedQuotas = sortQuotasById(existingQuotasToUpdate);
            budgetQuotaMapper.updateBatchById(sortedQuotas);
        }

        // 5. 插入新创建的 BUDGET_BALANCE（维度一致）
        if (!newBalancesToInsert.isEmpty()) {
            budgetBalanceMapper.insertBatch(newBalancesToInsert);
            log.info("========== 批量插入新创建的 BudgetBalance: {} 条 ==========", newBalancesToInsert.size());
        }
        
        // 6. 更新已存在的 BUDGET_BALANCE（维度一致）
        if (!existingBalancesToUpdate.isEmpty()) {
            List<BudgetBalance> sortedBalances = sortBalancesById(existingBalancesToUpdate);
            budgetBalanceMapper.updateBatchById(sortedBalances);
        }

        // 5. 插入 BUDGET_BALANCE_HISTORY
        if (!needToAddBudgetBalanceHistory.isEmpty()) {
            budgetBalanceHistoryMapper.insertBatch(needToAddBudgetBalanceHistory);
        }

        // 6. 插入 BUDGET_QUOTA_HISTORY
        if (!needToAddBudgetQuotaHistory.isEmpty()) {
            budgetQuotaHistoryMapper.insertBatch(needToAddBudgetQuotaHistory);
        }

        // 6.5 填充扣减来源：扣减在关联流水时填 deductionFromLedgerBizKey，否则填 poolDimensionKey（优先用 BUDGET_BALANCE 维度拼接）
        fillDeductionSourceKeys(needUpdateSameDemBudgetLedgerMap, needToAddBudgetLedgerMap, updatedRelatedBudgetLedgerMap, needToUpdateSameDemBudgetBalanceMap);

        // 7. 插入 BUDGET_LEDGER_HISTORY（从 existingBudgetLedgerMap 转换）
        List<BudgetLedgerHistory> ledgerHistories = new ArrayList<>();
        for (BudgetLedger ledger : existingBudgetLedgerMap.values()) {
            BudgetLedgerHistory history = new BudgetLedgerHistory();
            BeanUtils.copyProperties(ledger, history);
            history.setId(identifierGenerator.nextId(history).longValue());
            history.setLedgerId(ledger.getId());
            history.setDeleted(Boolean.FALSE);
            ledgerHistories.add(history);
        }
        if (!ledgerHistories.isEmpty()) {
            budgetLedgerHistoryMapper.insertBatch(ledgerHistories);
        }

        // 8. 更新 BUDGET_LEDGER（维度一致）
        if (!needUpdateSameDemBudgetLedgerMap.isEmpty()) {
            List<BudgetLedger> ledgersToUpdate = sortLedgersById(new ArrayList<>(needUpdateSameDemBudgetLedgerMap.values()));
            // 设置操作人字段及 UPDATE_TIME（使用 ESB requestTime 若有）
            for (BudgetLedger ledger : ledgersToUpdate) {
                ledger.setOperator(operator);
                ledger.setOperatorNo(operatorNo);
                if (requestTime != null) {
                    ledger.setUpdateTime(requestTime);
                }
            }
            budgetLedgerMapper.updateBatchById(ledgersToUpdate);
            
            // 删除这些 BudgetLedger 的 ID 对应的所有 BUDGET_LEDGER_SELF_R 关系记录（where id in）
            // 注意：需要物理删除（而不是逻辑删除），因为后面要插入相同ID的新记录，逻辑删除会导致主键唯一约束冲突
            Set<Long> ledgerIds = needUpdateSameDemBudgetLedgerMap.values().stream()
                    .map(BudgetLedger::getId)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());
            if (!ledgerIds.isEmpty()) {
                // 使用物理删除，传入 null 作为 bizType 可以删除所有 bizType 的记录
                int deletedCount = budgetLedgerSelfRMapper.deleteByIdsAndBizType(ledgerIds, null);
                if (deletedCount > 0) {
                    log.info("========== 删除 BUDGET_LEDGER_SELF_R 关系记录: 删除了 {} 条记录，ledgerIds={} ==========", deletedCount, ledgerIds);
                }
            }
        }

        // 9. 删除维度不一致的旧 BUDGET_LEDGER 记录（在插入新记录之前）
        if (needToDeleteOldLedgerIds != null && !needToDeleteOldLedgerIds.isEmpty()) {
            // 先删除 BUDGET_LEDGER_SELF_R 关系记录（物理删除）
            // 使用物理删除，传入 null 作为 bizType 可以删除所有 bizType 的记录
            int deletedSelfRCount = budgetLedgerSelfRMapper.deleteByIdsAndBizType(needToDeleteOldLedgerIds, null);
            if (deletedSelfRCount > 0) {
                log.info("========== 删除维度不一致的旧 BUDGET_LEDGER_SELF_R 关系记录: 删除了 {} 条记录，ledgerIds={} ==========", 
                        deletedSelfRCount, needToDeleteOldLedgerIds);
            }
            // 删除 BUDGET_LEDGER 记录（物理删除）
            List<Long> ledgerIdsToDelete = new ArrayList<>(needToDeleteOldLedgerIds);
            budgetLedgerMapper.deleteByIds(ledgerIdsToDelete);
            log.info("========== 删除维度不一致的旧 BUDGET_LEDGER 记录: 删除了 {} 条记录，ledgerIds={} ==========", 
                    ledgerIdsToDelete.size(), ledgerIdsToDelete);
        }

        // 10. 插入 BUDGET_LEDGER（新增）
        if (!needToAddBudgetLedgerMap.isEmpty()) {
            List<BudgetLedger> newLedgers = new ArrayList<>(needToAddBudgetLedgerMap.values());
            if (requestTime != null) {
                for (BudgetLedger ledger : newLedgers) {
                    ledger.setCreateTime(requestTime);
                    ledger.setUpdateTime(requestTime);
                }
            }
            budgetLedgerMapper.insertBatch(newLedgers);
        }
        
        // 10. 插入 BUDGET_LEDGER_SELF_R（关联关系）
        // 将 needUpdateSameDemBudgetLedgerMap 和 needToAddBudgetLedgerMap 合并，统一处理
        Map<String, BudgetLedger> allLedgerMap = new HashMap<>();
        allLedgerMap.putAll(needUpdateSameDemBudgetLedgerMap);
        allLedgerMap.putAll(needToAddBudgetLedgerMap);

        if (!allLedgerMap.isEmpty() && updatedRelatedBudgetLedgerMap != null && !updatedRelatedBudgetLedgerMap.isEmpty()) {
            List<BudgetLedgerSelfR> budgetLedgerSelfRList = new ArrayList<>();
            // 使用 Set 来去重，避免同一个 (id, bizType) 组合被重复插入
            // key 格式：ledger.getId() + "@" + relatedLedger.getBizType()
            // 因为 id = ledger.getId()，bizType = relatedLedger.getBizType()
            Set<String> uniqueKeySet = new HashSet<>();
            
            for (BudgetLedger ledger : allLedgerMap.values()) {
                if (ledger.getId() == null) {
                    continue;
                }
                
                // 通过 bizCode + "@" + bizItemCode 去 updatedRelatedBudgetLedgerMap 中查找对应的 List<BudgetLedger>
                String bizKey = ledger.getBizCode() + "@" + ledger.getBizItemCode();
                List<BudgetLedger> relatedLedgers = updatedRelatedBudgetLedgerMap.get(bizKey);
                
                if (!CollectionUtils.isEmpty(relatedLedgers)) {
                    for (BudgetLedger relatedLedger : relatedLedgers) {
                        if (relatedLedger.getId() == null) {
                            continue;
                        }
                        
                        // 创建唯一键，避免重复插入
                        // 唯一键应该包含 ledgerId、relatedId 和 bizType，因为同一个付款单可以关联多个不同的申请单
                        // id = ledger.getId()，relatedId = relatedLedger.getId()，bizType = relatedLedger.getBizType()
                        String uniqueKey = ledger.getId() + "@" + relatedLedger.getId() + "@" + relatedLedger.getBizType();
                        if (uniqueKeySet.contains(uniqueKey)) {
                            log.warn("========== 跳过重复的 BUDGET_LEDGER_SELF_R 关系记录: ledgerId={}, relatedId={}, bizType={} ==========", 
                                    ledger.getId(), relatedLedger.getId(), relatedLedger.getBizType());
                            continue;
                        }
                        uniqueKeySet.add(uniqueKey);
                        
                        // 创建 BudgetLedgerSelfR 关系
                        // 根据 BudgetQueryHelperService 的逻辑（第476-477行），正确的映射关系应该是：
                        // id = ledger.getId()（原始的 BudgetLedger 的 id，即 needUpdateSameDemBudgetLedgerMap 或 needToAddBudgetLedgerMap 中的 BudgetLedger.id）
                        // relatedId = relatedLedger.getId()（关联的 BudgetLedger 的 id，即 updatedRelatedBudgetLedgerMap 中 List<BudgetLedger> 下的 BudgetLedger.id）
                        // bizType = relatedLedger.getBizType()（从 updatedRelatedBudgetLedgerMap 中的 BudgetLedger 获取 bizType）
                        // 注意：删除时使用 deleteByIdsAndBizType(ledgerIds, null)，其中 ledgerIds 是原始 BudgetLedger 的 id，SQL 是 WHERE id IN (...)
                        // 这证明 BudgetLedgerSelfR 的 id 字段应该是原始 BudgetLedger 的 id
                        BudgetLedgerSelfR selfR = new BudgetLedgerSelfR();
                        selfR.setId(ledger.getId()); // id 是原始的 BudgetLedger 的 id
                        selfR.setBizType(relatedLedger.getBizType());
                        selfR.setRelatedId(relatedLedger.getId()); // relatedId 是关联的 BudgetLedger 的 id
                        selfR.setDeleted(Boolean.FALSE);
                        
                        // 从 relatedLedgerDeductionAmountMap 中获取每个季度的扣减金额
                        // key格式：ledgerId + "@" + relatedLedgerId + "@" + quarter
                        String q1Key = ledger.getId() + "@" + relatedLedger.getId() + "@q1";
                        String q2Key = ledger.getId() + "@" + relatedLedger.getId() + "@q2";
                        String q3Key = ledger.getId() + "@" + relatedLedger.getId() + "@q3";
                        String q4Key = ledger.getId() + "@" + relatedLedger.getId() + "@q4";
                        
                        selfR.setAmountConsumedQOne(relatedLedgerDeductionAmountMap.getOrDefault(q1Key, BigDecimal.ZERO));
                        selfR.setAmountConsumedQTwo(relatedLedgerDeductionAmountMap.getOrDefault(q2Key, BigDecimal.ZERO));
                        selfR.setAmountConsumedQThree(relatedLedgerDeductionAmountMap.getOrDefault(q3Key, BigDecimal.ZERO));
                        selfR.setAmountConsumedQFour(relatedLedgerDeductionAmountMap.getOrDefault(q4Key, BigDecimal.ZERO));
                        
                        budgetLedgerSelfRList.add(selfR);
                    }
                }
            }
            
            if (!budgetLedgerSelfRList.isEmpty()) {
                budgetLedgerSelfRMapper.insertBatch(budgetLedgerSelfRList);
                log.info("========== 插入 BUDGET_LEDGER_SELF_R 关系记录: 插入了 {} 条记录 ==========", budgetLedgerSelfRList.size());
            }
        }
        
        // 11. 更新 updatedRelatedBudgetLedgerMap 中的所有 BudgetLedger 到数据库
        // 按 id 去重：同一关联流水可能被多个付款明细引用（精确匹配+兜底匹配导致同一合同两条流水对象），
        // 保留 amountAvailable 最小的那条（扣减后的最终状态），避免后写入的旧值覆盖正确值（合同未付金额应为0却变成31000等）
        if (updatedRelatedBudgetLedgerMap != null && !updatedRelatedBudgetLedgerMap.isEmpty()) {
            Map<Long, BudgetLedger> uniqueRelatedLedgersMap = new HashMap<>();
            for (List<BudgetLedger> ledgerList : updatedRelatedBudgetLedgerMap.values()) {
                if (!CollectionUtils.isEmpty(ledgerList)) {
                    for (BudgetLedger ledger : ledgerList) {
                        if (ledger == null || ledger.getId() == null) {
                            continue;
                        }
                        BudgetLedger existing = uniqueRelatedLedgersMap.get(ledger.getId());
                        if (existing == null) {
                            uniqueRelatedLedgersMap.put(ledger.getId(), ledger);
                        } else {
                            BigDecimal avNew = ledger.getAmountAvailable() == null ? BigDecimal.ZERO : ledger.getAmountAvailable();
                            BigDecimal avExisting = existing.getAmountAvailable() == null ? BigDecimal.ZERO : existing.getAmountAvailable();
                            // 保留扣减更多的那条（amountAvailable 更小 = 最终状态），同一对象上 amountConsumedQ1-Q4 已同步更新
                            if (avNew.compareTo(avExisting) < 0) {
                                uniqueRelatedLedgersMap.put(ledger.getId(), ledger);
                            }
                        }
                    }
                }
            }
            if (!uniqueRelatedLedgersMap.isEmpty()) {
                List<BudgetLedger> sortedRelatedLedgers = sortLedgersById(new ArrayList<>(uniqueRelatedLedgersMap.values()));
                budgetLedgerMapper.updateBatchById(sortedRelatedLedgers);
                log.info("========== 更新 updatedRelatedBudgetLedgerMap 中的 BudgetLedger: 更新了 {} 条记录 ==========", sortedRelatedLedgers.size());
            }
        }
    }

    /**
     * 填充流水扣减来源：无关联流水时只填 poolDimensionKey；有关联流水时填 deductionFromLedgerBizKey。
     * <p>
     * 付款单（CLAIM）有关联合同/申请时：
     * <ul>
     *   <li>本单 {@code amountConsumedQ*} 有值：表示按季记了从资金池扣减的分摊，需落 {@code poolDimensionKey} 以便撤销按「扣减当时的余额维度」定位；</li>
     *   <li>关联框架协议合同（CONTRACT + effectType=1）：即使本单 {@code amountConsumedQ*} 全为 0，
     *       仍可能通过 {@code applyFrameworkContractTransferOnBalance} 在同维度余额上做了占用→发生的转换，撤销依赖同一条 {@link BudgetBalance}，
     *       故在能取到同 bizKey 下 {@code needToUpdateSameDemBudgetBalanceMap} 中的余额时也应写入 {@code poolDimensionKey}。</li>
     *   <li>同维提交中已标记改动池余额（{@link #markSubmitPoolBudgetBalanceTouched}）：普通关联亦须锚定当时资金池。</li>
     * </ul>
     * 合同单（CONTRACT）关联申请单流水时：若本单 {@code amountConsumedQ*} 有值或已标记改动池余额，同样落 {@code poolDimensionKey}。
     * <p>
     * 若上述条件需要落 key 但 map 中无对应 {@link BudgetBalance}（如本维度当季无余额、仅扣关联流水），则不使用未映射的流水维度强行锚池，只在本单已有
     * {@code amountConsumedQ*} 时回退 {@link BudgetLedger#buildPoolDimensionKey()} 以保持历史兼容。
     */
    protected void fillDeductionSourceKeys(Map<String, BudgetLedger> needUpdateSameDemBudgetLedgerMap,
                                           Map<String, BudgetLedger> needToAddBudgetLedgerMap,
                                           Map<String, List<BudgetLedger>> updatedRelatedBudgetLedgerMap,
                                           Map<String, BudgetBalance> needToUpdateSameDemBudgetBalanceMap) {
        final boolean trackPoolTouched = CLAIM_BIZ_TYPE.equals(getBizType()) || CONTRACT_BIZ_TYPE.equals(getBizType());
        final Set<String> submitPoolBalanceTouchedBizKeys = trackPoolTouched
                ? new HashSet<>(SUBMIT_POOL_BALANCE_TOUCHED_BIZ_KEYS.get())
                : Collections.emptySet();
        try {
            for (BudgetLedger ledger : needUpdateSameDemBudgetLedgerMap.values()) {
                fillDeductionSourceKeyForLedger(ledger, updatedRelatedBudgetLedgerMap, needToUpdateSameDemBudgetBalanceMap, submitPoolBalanceTouchedBizKeys);
            }
            for (BudgetLedger ledger : needToAddBudgetLedgerMap.values()) {
                fillDeductionSourceKeyForLedger(ledger, updatedRelatedBudgetLedgerMap, needToUpdateSameDemBudgetBalanceMap, submitPoolBalanceTouchedBizKeys);
            }
        } finally {
            if (trackPoolTouched) {
                SUBMIT_POOL_BALANCE_TOUCHED_BIZ_KEYS.remove();
            }
        }
    }

    private void fillDeductionSourceKeyForLedger(BudgetLedger ledger, Map<String, List<BudgetLedger>> updatedRelatedBudgetLedgerMap,
                                                  Map<String, BudgetBalance> needToUpdateSameDemBudgetBalanceMap,
                                                  Set<String> submitPoolBalanceTouchedBizKeys) {
        String bizKey = ledger.getBizCode() + "@" + ledger.getBizItemCode();
        List<BudgetLedger> relatedLedgers = updatedRelatedBudgetLedgerMap != null ? updatedRelatedBudgetLedgerMap.get(bizKey) : null;
        if (!CollectionUtils.isEmpty(relatedLedgers)) {
            // 扣减可能来自多条关联流水，用分号拼接所有被扣减流水的 bizKey
            String keys = BudgetLedger.buildDeductionFromLedgerBizKeyMultiple(relatedLedgers);
            ledger.setDeductionFromLedgerBizKey(keys.isEmpty() ? null : keys);
            boolean persistClaim = CLAIM_BIZ_TYPE.equals(ledger.getBizType())
                    && claimShouldPersistPoolDimensionKeyWithRelated(ledger, relatedLedgers, submitPoolBalanceTouchedBizKeys);
            boolean persistContract = CONTRACT_BIZ_TYPE.equals(ledger.getBizType())
                    && contractShouldPersistPoolDimensionKeyWithRelated(ledger, submitPoolBalanceTouchedBizKeys);
            if (persistClaim || persistContract) {
                setPoolDimensionKeyForRelatedLedgerFromBalanceOrLedger(ledger, bizKey, needToUpdateSameDemBudgetBalanceMap);
            } else {
                ledger.setPoolDimensionKey(null);
            }
        } else {
            // 扣减在资金池时，始终用实际扣减的 BUDGET_BALANCE 的维度拼接 poolDimensionKey（映射后的组织/科目/项目/资产类型），
            // 保证与资金池 BUDGET_BALANCE 维度一致，重跑后可根据本字段+季度查到同一资金池。仅当 map 中无对应 Balance 时才回退到流水自身维度。
            BudgetBalance balance = findBalanceForLedger(bizKey, needToUpdateSameDemBudgetBalanceMap);
            String key = balance != null ? BudgetLedger.buildPoolDimensionKeyFromBalance(balance) : ledger.buildPoolDimensionKey();
            ledger.setPoolDimensionKey(key);
        }
    }

    /** 单据流水上 amountConsumedQ* 是否含本单从资金池按季扣减的分摊（与 processSameDimensionUpdate 写入约定一致） */
    private static boolean ledgerHasPoolQuarterlyConsumption(BudgetLedger ledger) {
        if (ledger == null) {
            return false;
        }
        BigDecimal q1 = ledger.getAmountConsumedQOne() == null ? BigDecimal.ZERO : ledger.getAmountConsumedQOne();
        BigDecimal q2 = ledger.getAmountConsumedQTwo() == null ? BigDecimal.ZERO : ledger.getAmountConsumedQTwo();
        BigDecimal q3 = ledger.getAmountConsumedQThree() == null ? BigDecimal.ZERO : ledger.getAmountConsumedQThree();
        BigDecimal q4 = ledger.getAmountConsumedQFour() == null ? BigDecimal.ZERO : ledger.getAmountConsumedQFour();
        return q1.add(q2).add(q3).add(q4).compareTo(BigDecimal.ZERO) > 0;
    }

    private static void setPoolDimensionKeyForRelatedLedgerFromBalanceOrLedger(BudgetLedger ledger, String bizKey,
                                                                               Map<String, BudgetBalance> needToUpdateSameDemBudgetBalanceMap) {
        BudgetBalance balance = findBalanceForLedger(bizKey, needToUpdateSameDemBudgetBalanceMap);
        if (balance != null) {
            ledger.setPoolDimensionKey(BudgetLedger.buildPoolDimensionKeyFromBalance(balance));
        } else if (ledgerHasPoolQuarterlyConsumption(ledger)) {
            ledger.setPoolDimensionKey(ledger.buildPoolDimensionKey());
        } else {
            ledger.setPoolDimensionKey(null);
        }
    }

    /**
     * 是否为本单关联了框架协议合同（约定：CONTRACT + effectType=1，与付款单扣减侧判断一致）。
     */
    private static boolean isFrameworkAgreementRelated(List<BudgetLedger> relatedLedgers) {
        if (CollectionUtils.isEmpty(relatedLedgers)) {
            return false;
        }
        for (BudgetLedger rl : relatedLedgers) {
            if (rl != null && CONTRACT_BIZ_TYPE.equals(rl.getBizType()) && "1".equals(rl.getEffectType())) {
                return true;
            }
        }
        return false;
    }

    /**
     * 有关联流水时，CLAIM 是否应持久化 poolDimensionKey（优先用本次实际参与计算的 {@link BudgetBalance} 维度，降低映射变更导致错池风险）。
     */
    private static boolean claimShouldPersistPoolDimensionKeyWithRelated(BudgetLedger ledger, List<BudgetLedger> relatedLedgers,
                                                                         Set<String> submitPoolBalanceTouchedBizKeys) {
        String bizKey = ledger.getBizCode() + "@" + ledger.getBizItemCode();
        boolean touchedPoolBalance = submitPoolBalanceTouchedBizKeys != null && submitPoolBalanceTouchedBizKeys.contains(bizKey);
        return ledgerHasPoolQuarterlyConsumption(ledger) || isFrameworkAgreementRelated(relatedLedgers) || touchedPoolBalance;
    }

    /**
     * 有关联流水时，CONTRACT 是否应持久化 poolDimensionKey（关联申请单且可能同时释放冻结/叠加占用等改动 {@link BudgetBalance}）。
     */
    private static boolean contractShouldPersistPoolDimensionKeyWithRelated(BudgetLedger ledger,
                                                                            Set<String> submitPoolBalanceTouchedBizKeys) {
        String bizKey = ledger.getBizCode() + "@" + ledger.getBizItemCode();
        boolean touchedPoolBalance = submitPoolBalanceTouchedBizKeys != null && submitPoolBalanceTouchedBizKeys.contains(bizKey);
        return ledgerHasPoolQuarterlyConsumption(ledger) || touchedPoolBalance;
    }

    /** 根据 bizKey 从 balanceMap（key 为 bizKey+"@"+quarter）中取任意一个季度的 Balance，用于拼接 poolDimensionKey */
    private static BudgetBalance findBalanceForLedger(String bizKey, Map<String, BudgetBalance> needToUpdateSameDemBudgetBalanceMap) {
        if (bizKey == null || needToUpdateSameDemBudgetBalanceMap == null || needToUpdateSameDemBudgetBalanceMap.isEmpty()) {
            return null;
        }
        for (String quarter : new String[]{"q1", "q2", "q3", "q4"}) {
            BudgetBalance b = needToUpdateSameDemBudgetBalanceMap.get(bizKey + "@" + quarter);
            if (b != null) {
                return b;
            }
        }
        return null;
    }
    
    /**
     * 根据 details 查询关联的预算流水
     * 在调用 processSameDimensionUpdate 之前，先查询关联的预算流水
     *
     * @param reqInfo 请求信息，包含 documentNo 和 details
     * @param ehrCdToOrgCdExtMap EHR_ORG_MANAGE_EXT_R表查询结果，用于判断不受控明细
     * @param erpAcctCdToAcctCdExtMap SUBJECT_EXT_INFO表查询结果，用于判断不受控明细
     * @return Map<String, List<BudgetLedger>>，key 为 documentNo + "@" + detailLineNo（外层 documentNo + 内层 detailLineNo），value 为 List<BudgetLedger>
     */
    private Map<String, List<BudgetLedger>> queryRelatedBudgetLedgersByDetails(ReqInfoParams reqInfo,
                                                                                Map<String, List<String>> ehrCdToOrgCdExtMap,
                                                                                Map<String, List<String>> erpAcctCdToAcctCdExtMap) {
        Map<String, List<BudgetLedger>> result = new HashMap<>();
        
        // 取出 budgetParams 下的 documentNo 和 details 下的 detailLineNo
        String documentNo = reqInfo.getDocumentNo();
        List<DetailDetailVo> details = reqInfo.getDetails();
        
        if (StringUtils.isBlank(documentNo) || CollectionUtils.isEmpty(details)) {
            return result;
        }
        
        // 付款/合同明细 key -> 明细对象，用于判断该明细是否受控（仅对受控明细要求「至少一条流水在同一控制层级」）
        Map<String, DetailDetailVo> detailKeyToDetailMap = new HashMap<>();
        for (DetailDetailVo d : details) {
            if (StringUtils.isNotBlank(d.getDetailLineNo())) {
                detailKeyToDetailMap.put(documentNo + "@" + d.getDetailLineNo(), d);
            }
        }
        
        // 收集需要查询的 documentNo 和 detailLineNo 对，保持对应关系
        List<BudgetLedgerCompositeKey> queryConditions = new ArrayList<>();
        // 保存映射关系：bizCode + "@" + bizItemCode -> documentNo + "@" + detailLineNo
        // 其中 documentNo 是外层的（reqInfo.getDocumentNo()），detailLineNo 是内层的（DetailDetailVo.getDetailLineNo()）
        Map<String, String> bizKeyToDetailKeyMap = new HashMap<>();
        String queryBizType = null;
        
        for (DetailDetailVo detail : details) {
            // 外层 detail 的 detailLineNo
            String detailLineNo = detail.getDetailLineNo();
            
            // 首先查看 details 下的 contractDetails 是否为空
            if (!CollectionUtils.isEmpty(detail.getContractDetails())) {
                // 如果不为空，查询 details 下的 contractDetails
                for (SubDetailVo subDetail : detail.getContractDetails()) {
                    if (subDetail.getDocumentNo() != null && subDetail.getDetailLineNo() != null) {
                        BudgetLedgerCompositeKey compositeKey = new BudgetLedgerCompositeKey();
                        compositeKey.setBizType("CONTRACT");
                        compositeKey.setBizCode(subDetail.getDocumentNo());
                        compositeKey.setBizItemCode(subDetail.getDetailLineNo());
                        queryConditions.add(compositeKey);
                        
                        // 保存映射关系：bizCode + "@" + bizItemCode -> documentNo + "@" + detailLineNo
                        // bizKey 是查询条件（subDetail 的 documentNo 和 detailLineNo）
                        // detailKey 是 map 的 key（外层的 documentNo 和内层的 detailLineNo）
                        String bizKey = subDetail.getDocumentNo() + "@" + subDetail.getDetailLineNo();
                        String detailKey = documentNo + "@" + detailLineNo;
                        bizKeyToDetailKeyMap.put(bizKey, detailKey);
                        
                        if (queryBizType == null) {
                            queryBizType = "CONTRACT";
                        }
                    }
                }
            } else if (!CollectionUtils.isEmpty(detail.getApplyDetails())) {
                // 如果 contractDetails 为空，查看 details 下的 applyDetails 是否为空
                // 如果 applyDetails 不为空，查询 details 下的 applyDetails
                for (SubDetailVo subDetail : detail.getApplyDetails()) {
                    if (subDetail.getDocumentNo() != null && subDetail.getDetailLineNo() != null) {
                        BudgetLedgerCompositeKey compositeKey = new BudgetLedgerCompositeKey();
                        compositeKey.setBizType("APPLY");
                        compositeKey.setBizCode(subDetail.getDocumentNo());
                        compositeKey.setBizItemCode(subDetail.getDetailLineNo());
                        queryConditions.add(compositeKey);
                        
                        // 保存映射关系：bizCode + "@" + bizItemCode -> documentNo + "@" + detailLineNo
                        // bizKey 是查询条件（subDetail 的 documentNo 和 detailLineNo）
                        // detailKey 是 map 的 key（外层的 documentNo 和内层的 detailLineNo）
                        String bizKey = subDetail.getDocumentNo() + "@" + subDetail.getDetailLineNo();
                        String detailKey = documentNo + "@" + detailLineNo;
                        bizKeyToDetailKeyMap.put(bizKey, detailKey);
                        
                        if (queryBizType == null) {
                            queryBizType = "APPLY";
                        }
                    }
                }
            }
            // 如果 applyDetails 也为空（即到现在 applyDetails 和 contractDetails 都为空），则不用查询了
        }
        
        // 按预算组织展开查询条件：使「同一预算组织、不同EHR组织」的流水能在初始查询命中，避免走兜底导致混合比例扣减（覆盖：付款关联申请单、付款关联合同单、合同关联申请单等）
        if (("APPLY".equals(queryBizType) || "CONTRACT".equals(queryBizType)) && ehrCdToOrgCdExtMap != null && !ehrCdToOrgCdExtMap.isEmpty()) {
            // 构建「预算组织 -> 所有映射到该组织的EHR组织」反向映射
            Map<String, Set<String>> orgCdToEhrCdsMap = new HashMap<>();
            for (Map.Entry<String, List<String>> entry : ehrCdToOrgCdExtMap.entrySet()) {
                String ehrCd = entry.getKey();
                List<String> orgCdList = entry.getValue();
                if (orgCdList != null) {
                    for (String orgCd : orgCdList) {
                        orgCdToEhrCdsMap.computeIfAbsent(orgCd, k -> new HashSet<>()).add(ehrCd);
                    }
                }
            }
            String expandBizType = queryBizType;
            List<BudgetLedgerCompositeKey> expandedConditions = new ArrayList<>();
            for (BudgetLedgerCompositeKey condition : queryConditions) {
                if (!expandBizType.equals(condition.getBizType())) {
                    continue;
                }
                String bizItemCode = condition.getBizItemCode();
                if (StringUtils.isBlank(bizItemCode)) {
                    continue;
                }
                String[] parts = bizItemCode.split("@");
                if (parts.length < 2) {
                    continue;
                }
                String morg = parts[1];
                List<String> orgCds = ehrCdToOrgCdExtMap.get(morg);
                if (CollectionUtils.isEmpty(orgCds)) {
                    continue;
                }
                Set<String> sameOrgEhrCds = new HashSet<>();
                for (String orgCd : orgCds) {
                    Set<String> ehrCds = orgCdToEhrCdsMap.get(orgCd);
                    if (ehrCds != null) {
                        sameOrgEhrCds.addAll(ehrCds);
                    }
                }
                String bizKey = condition.getBizCode() + "@" + bizItemCode;
                String detailKey = bizKeyToDetailKeyMap.get(bizKey);
                if (detailKey == null) {
                    continue;
                }
                for (String otherEhrCd : sameOrgEhrCds) {
                    if (otherEhrCd.equals(morg)) {
                        continue;
                    }
                    StringBuilder sb = new StringBuilder(parts[0]);
                    for (int i = 1; i < parts.length; i++) {
                        sb.append("@").append(i == 1 ? otherEhrCd : parts[i]);
                    }
                    String newBizItemCode = sb.toString();
                    BudgetLedgerCompositeKey expandedKey = new BudgetLedgerCompositeKey();
                    expandedKey.setBizType(expandBizType);
                    expandedKey.setBizCode(condition.getBizCode());
                    expandedKey.setBizItemCode(newBizItemCode);
                    expandedConditions.add(expandedKey);
                    bizKeyToDetailKeyMap.put(condition.getBizCode() + "@" + newBizItemCode, detailKey);
                }
            }
            if (!expandedConditions.isEmpty()) {
                queryConditions.addAll(expandedConditions);
                log.info("========== queryRelatedBudgetLedgersByDetails - 按预算组织展开{}查询条件: 新增 {} 条，便于同一预算组织不同EHR组织命中 ==========", expandBizType, expandedConditions.size());
            }
        }
        
        // 如果有需要查询的数据，则查询关联的预算流水
        if (!queryConditions.isEmpty() && queryBizType != null) {
            // 记录查询条件
            log.info("========== queryRelatedBudgetLedgersByDetails - 查询关联预算流水: queryBizType={}, queryConditions数量={} ==========", 
                    queryBizType, queryConditions.size());
            for (BudgetLedgerCompositeKey condition : queryConditions) {
                log.info("========== queryRelatedBudgetLedgersByDetails - 查询条件: bizType={}, bizCode={}, bizItemCode={} ==========",
                        condition.getBizType(), condition.getBizCode(), condition.getBizItemCode());
            }
            
            // 根据 documentNo 和 detailLineNo 查询对应的 BudgetLedger
            List<BudgetLedger> relatedLedgers = budgetLedgerMapper.selectByCompositeKeys(queryConditions);
            
            log.info("========== queryRelatedBudgetLedgersByDetails - 查询结果: 找到 {} 条关联预算流水 ==========", 
                    relatedLedgers != null ? relatedLedgers.size() : 0);
            if (!CollectionUtils.isEmpty(relatedLedgers)) {
                for (BudgetLedger ledger : relatedLedgers) {
                    log.info("========== queryRelatedBudgetLedgersByDetails - 关联预算流水: bizType={}, bizCode={}, bizItemCode={}, amountAvailable={} ==========",
                            ledger.getBizType(), ledger.getBizCode(), ledger.getBizItemCode(), ledger.getAmountAvailable());
                }
            }
            
            // 记录哪些查询条件已经匹配到了结果
            Set<String> matchedConditionKeys = new HashSet<>();
            if (!CollectionUtils.isEmpty(relatedLedgers)) {
                // 根据入参结构组装 map，key 为 documentNo + "@" + detailLineNo（外层 documentNo + 内层 detailLineNo），value 为 List<BudgetLedger>
                for (BudgetLedger ledger : relatedLedgers) {
                    // 通过 bizCode + "@" + bizItemCode 找到对应的 detailKey
                    String bizKey = ledger.getBizCode() + "@" + ledger.getBizItemCode();
                    String detailKey = bizKeyToDetailKeyMap.get(bizKey);
                    // 验证日志：精确查询阶段流水与付款明细的映射，用于排查「匹配到流水但弱控仍报错」
                    log.info("========== queryRelatedBudgetLedgersByDetails - [初始匹配] bizKey={}, detailKey={}, detailKey为null={} ==========",
                            bizKey, detailKey, (detailKey == null));
                    if (detailKey != null) {
                        result.computeIfAbsent(detailKey, k -> new ArrayList<>()).add(ledger);
                        // 记录已匹配的查询条件
                        matchedConditionKeys.add(bizKey);
                    }
                }
                
                if (!result.isEmpty()) {
                    log.info("========== 查询到关联预算流水: {} 条记录 ==========", result.size());
                }
            }
            
            // 找出需要执行兜底扩展匹配的查询条件（APPLY、CONTRACT）
            // 组织+科目口径下：即使已精确命中，也继续按控制层级扩展，确保同控制层级科目都参与关联校验与扣减
            List<BudgetLedgerCompositeKey> unmatchedConditions = new ArrayList<>();
            if ("APPLY".equals(queryBizType)) {
                for (BudgetLedgerCompositeKey condition : queryConditions) {
                    if (!"APPLY".equals(condition.getBizType())) {
                        continue;
                    }
                    String conditionKey = condition.getBizCode() + "@" + condition.getBizItemCode();
                    boolean matchedExactly = matchedConditionKeys.contains(conditionKey);
                    boolean orgSubjectCondition = false;
                    if (StringUtils.isNotBlank(condition.getBizItemCode())) {
                        String[] conditionDimensions = condition.getBizItemCode().split("@");
                        orgSubjectCondition = conditionDimensions.length >= 5
                                && "NAN".equals(conditionDimensions[3])
                                && !"NAN-NAN".equals(conditionDimensions[2])
                                && "NAN".equals(conditionDimensions[4]);
                    }
                    if (!matchedExactly || orgSubjectCondition) {
                        unmatchedConditions.add(condition);
                        if (!matchedExactly) {
                            log.info("========== queryRelatedBudgetLedgersByDetails - 查询条件未匹配到结果，将执行兜底逻辑: bizCode={}, bizItemCode={} ==========",
                                    condition.getBizCode(), condition.getBizItemCode());
                        } else {
                            log.info("========== queryRelatedBudgetLedgersByDetails - 组织+科目已精确命中，继续执行兜底扩展匹配(按申请单号+组织/科目控制层级): bizCode={}, bizItemCode={} ==========",
                                    condition.getBizCode(), condition.getBizItemCode());
                        }
                    }
                }
            } else if ("CONTRACT".equals(queryBizType)) {
                for (BudgetLedgerCompositeKey condition : queryConditions) {
                    if (!"CONTRACT".equals(condition.getBizType())) {
                        continue;
                    }
                    String conditionKey = condition.getBizCode() + "@" + condition.getBizItemCode();
                    boolean matchedExactly = matchedConditionKeys.contains(conditionKey);
                    boolean projectCondition = false;
                    boolean orgSubjectCondition = false;
                    if (StringUtils.isNotBlank(condition.getBizItemCode())) {
                        String[] conditionDimensions = condition.getBizItemCode().split("@");
                        // 行号@组织@科目@项目@资产类型，项目非 NAN 视为项目单据
                        projectCondition = conditionDimensions.length >= 4
                                && StringUtils.isNotBlank(conditionDimensions[3])
                                && !"NAN".equals(conditionDimensions[3]);
                        orgSubjectCondition = conditionDimensions.length >= 5
                                && "NAN".equals(conditionDimensions[3])
                                && !"NAN-NAN".equals(conditionDimensions[2])
                                && "NAN".equals(conditionDimensions[4]);
                    }
                    // 项目单据：即使已精确命中，也继续走合同兜底补充匹配（按合同号+项目控制层级）
                    // 组织+科目口径：即使已精确命中，也继续走控制层级扩展匹配（按合同号+组织/科目控制层级）
                    if (!matchedExactly || projectCondition || orgSubjectCondition) {
                        unmatchedConditions.add(condition);
                        if (!matchedExactly) {
                            log.info("========== queryRelatedBudgetLedgersByDetails - 合同单查询条件未匹配，将执行兜底逻辑(按合同号+维度兼容匹配): bizCode={}, bizItemCode={} ==========",
                                    condition.getBizCode(), condition.getBizItemCode());
                        } else if (projectCondition) {
                            log.info("========== queryRelatedBudgetLedgersByDetails - 项目单据已精确命中，继续执行合同兜底补充匹配(按合同号+项目控制层级): bizCode={}, bizItemCode={} ==========",
                                    condition.getBizCode(), condition.getBizItemCode());
                        } else {
                            log.info("========== queryRelatedBudgetLedgersByDetails - 组织+科目已精确命中，继续执行合同兜底扩展匹配(按合同号+组织/科目控制层级): bizCode={}, bizItemCode={} ==========",
                                    condition.getBizCode(), condition.getBizItemCode());
                        }
                    }
                }
            }
            
            // 校验：如果传了关联单据但查询结果为空，或者有未匹配的查询条件，说明关联单据不存在或明细行号不匹配
            // 对于申请单（APPLY）类型，需要进一步校验组织是否一致
            // 本分支覆盖场景：付款单关联需求申请单、合同单关联需求申请单（合同申请时通过 applyDetails 传入需求单号）
            if (CollectionUtils.isEmpty(relatedLedgers) || !unmatchedConditions.isEmpty()) {
                // 如果是申请单类型，尝试根据申请单号查询所有预算流水，然后进行维度匹配和组织校验
                if ("APPLY".equals(queryBizType)) {
                    // 收集未匹配的申请单号（如果所有条件都未匹配，则收集所有申请单号）
                    Set<String> applyOrderNos = unmatchedConditions.isEmpty() 
                            ? queryConditions.stream()
                                    .filter(condition -> "APPLY".equals(condition.getBizType()))
                                    .map(BudgetLedgerCompositeKey::getBizCode)
                                    .filter(StringUtils::isNotBlank)
                                    .collect(Collectors.toSet())
                            : unmatchedConditions.stream()
                                    .map(BudgetLedgerCompositeKey::getBizCode)
                                    .filter(StringUtils::isNotBlank)
                                    .collect(Collectors.toSet());
                    
                    if (!applyOrderNos.isEmpty()) {
                        // 根据申请单号查询所有预算流水
                        List<BudgetLedger> allApplyLedgers = budgetLedgerMapper.selectList(
                                new LambdaQueryWrapper<BudgetLedger>()
                                        .eq(BudgetLedger::getBizType, "APPLY")
                                        .in(BudgetLedger::getBizCode, applyOrderNos)
                                        .eq(BudgetLedger::getDeleted, Boolean.FALSE)
                        );
                        
                        if (!CollectionUtils.isEmpty(allApplyLedgers)) {
                            log.info("========== queryRelatedBudgetLedgersByDetails - 根据申请单号查询到 {} 条预算流水，开始进行维度匹配和组织校验 ==========", 
                                    allApplyLedgers.size());
                            
                            // 补充申请单流水的 morg 到 ehrCdToOrgCdExtMap，便于后续「同预算组织」校验（报销组织与申请单组织不同但预算组织一致时仍可匹配）
                            Set<String> ledgerMorgs = new HashSet<>();
                            for (BudgetLedger ledger : allApplyLedgers) {
                                if (StringUtils.isNotBlank(ledger.getBizItemCode())) {
                                    String[] p = ledger.getBizItemCode().split("@");
                                    if (p.length >= 2 && StringUtils.isNotBlank(p[1])) {
                                        ledgerMorgs.add(p[1]);
                                    }
                                }
                            }
                            ledgerMorgs.removeAll(ehrCdToOrgCdExtMap != null ? ehrCdToOrgCdExtMap.keySet() : Collections.emptySet());
                            if (!ledgerMorgs.isEmpty()) {
                                Map<String, List<String>> extMap = budgetQueryHelperService.queryEhrCdToOrgCdMap(ledgerMorgs);
                                if (extMap != null && !extMap.isEmpty()) {
                                    ehrCdToOrgCdExtMap.putAll(extMap);
                                    log.info("========== queryRelatedBudgetLedgersByDetails - 已补充申请单流水 morg 的预算组织映射 {} 条，用于兜底组织校验 ==========", extMap.size());
                                }
                            }
                            
                            // 为每个未匹配的查询条件尝试匹配预算流水（如果所有条件都未匹配，则处理所有查询条件）
                            List<BudgetLedgerCompositeKey> conditionsToProcess = unmatchedConditions.isEmpty() 
                                    ? queryConditions.stream()
                                            .filter(condition -> "APPLY".equals(condition.getBizType()))
                                            .collect(Collectors.toList())
                                    : unmatchedConditions;
                            
                            // 收集所有涉及申请单的付款明细 key：用于弱控判断「每条明细只要关联到至少一条申请单就不报错」
                            Set<String> detailKeysWithApply = queryConditions.stream()
                                    .filter(c -> "APPLY".equals(c.getBizType()))
                                    .map(c -> bizKeyToDetailKeyMap.get(c.getBizCode() + "@" + c.getBizItemCode()))
                                    .filter(Objects::nonNull)
                                    .collect(Collectors.toSet());
                            // 仅对受控的付款明细要求「至少一条流水在同一控制层级」；不受控的组织/科目不要求
                            Set<String> detailKeysRequiringControlLevel = detailKeysWithApply.stream()
                                    .filter(detailKey -> {
                                        DetailDetailVo detail = detailKeyToDetailMap.get(detailKey);
                                        if (detail == null) {
                                            return true;
                                        }
                                        return !isUncontrolledDetailFromCondition(
                                                detail.getManagementOrg(), detail.getBudgetSubjectCode(), detail.getMasterProjectCode(),
                                                detail.getIsInternal(), detail.getErpAssetType(),
                                                ehrCdToOrgCdExtMap, erpAcctCdToAcctCdExtMap);
                                    })
                                    .collect(Collectors.toSet());
                            
                            // 同一申请单可能被多条付款/合同明细引用，按 id 复用已存在的流水对象，保证扣减作用在同一实例上
                            Map<Long, BudgetLedger> existingApplyLedgerById = new HashMap<>();
                            for (List<BudgetLedger> list : result.values()) {
                                if (list != null) {
                                    for (BudgetLedger l : list) {
                                        if (l != null && l.getId() != null) {
                                            existingApplyLedgerById.put(l.getId(), l);
                                        }
                                    }
                                }
                            }
                            // 记录每个申请单的校验结果：true表示有通过校验的流水，false表示没有通过校验的流水
                            List<BudgetLedgerCompositeKey> failedConditions = new ArrayList<>();
                            
                            for (BudgetLedgerCompositeKey condition : conditionsToProcess) {
                                if (!"APPLY".equals(condition.getBizType())) {
                                    continue;
                                }
                                
                                // 解析查询条件中的维度
                                String queryBizItemCode = condition.getBizItemCode();
                                String[] queryDimensions = queryBizItemCode.split("@");
                                if (queryDimensions.length < 5) {
                                    continue;
                                }
                                String queryIsInternal = queryDimensions[0];
                                String queryManagementOrg = queryDimensions[1];
                                String queryBudgetSubjectCode = queryDimensions[2];
                                String queryMasterProjectCode = queryDimensions[3];
                                String queryErpAssetType = queryDimensions[4];
                                
                                // 判断明细是否不受控，如果不受控则跳过关联申请单校验
                                boolean isUncontrolled = isUncontrolledDetailFromCondition(
                                        queryManagementOrg, queryBudgetSubjectCode, queryMasterProjectCode,
                                        queryIsInternal, queryErpAssetType,
                                        ehrCdToOrgCdExtMap, erpAcctCdToAcctCdExtMap);
                                if (isUncontrolled) {
                                    log.info("========== queryRelatedBudgetLedgersByDetails - 不受控明细跳过关联申请单校验: bizCode={}, bizItemCode={} ==========",
                                            condition.getBizCode(), condition.getBizItemCode());
                                    // 不受控明细跳过校验，不添加到 failedConditions，也不添加到结果中
                                    continue;
                                }
                                
                                // 判断是否带项目
                                final boolean hasProject = !"NAN".equals(queryMasterProjectCode);
                                
                                // 判断维度类型（不带项目时）
                                final boolean isOrgSubjectDimension; // 组织+科目维度
                                final boolean isOrgAssetTypeDimension; // 组织+资产类型维度
                                if (!hasProject) {
                                    isOrgSubjectDimension = !"NAN-NAN".equals(queryBudgetSubjectCode) && "NAN".equals(queryErpAssetType);
                                    isOrgAssetTypeDimension = "NAN-NAN".equals(queryBudgetSubjectCode) && !"NAN".equals(queryErpAssetType);
                                } else {
                                    isOrgSubjectDimension = false;
                                    isOrgAssetTypeDimension = false;
                                }
                                
                                // 将需要在 lambda 中使用的变量声明为 final
                                final String finalQueryIsInternal = queryIsInternal;
                                final String finalQueryManagementOrg = queryManagementOrg;
                                final String finalQueryBudgetSubjectCode = queryBudgetSubjectCode;
                                final String finalQueryMasterProjectCode = queryMasterProjectCode;
                                final String finalQueryErpAssetType = queryErpAssetType;
                                
                                // 查找匹配的预算流水（根据是否带项目决定匹配哪些维度）
                                List<BudgetLedger> matchedLedgers = allApplyLedgers.stream()
                                        .filter(ledger -> {
                                            // 必须匹配申请单号
                                            if (!condition.getBizCode().equals(ledger.getBizCode())) {
                                                return false;
                                            }
                                            String ledgerBizItemCode = ledger.getBizItemCode();
                                            if (StringUtils.isBlank(ledgerBizItemCode)) {
                                                return false;
                                            }
                                            String[] ledgerDimensions = ledgerBizItemCode.split("@");
                                            if (ledgerDimensions.length < 5) {
                                                return false;
                                            }
                                            
                                            // 如果带项目，只匹配项目
                                            if (hasProject) {
                                                return finalQueryMasterProjectCode.equals(ledgerDimensions[3]);
                                            }
                                            
                                            // 不带项目时，匹配 isInternal，并且根据维度类型匹配科目或资产类型
                                            if (!finalQueryIsInternal.equals(ledgerDimensions[0])) {
                                                return false;
                                            }
                                            
                                            // 如果是组织+科目维度：放开科目精确匹配，后续通过「组织控制层级 + 科目控制层级」过滤，
                                            // 使同控制层级科目（如 66020301/66020304）都可参与关联校验与扣减
                                            if (isOrgSubjectDimension) {
                                                return true;
                                            }
                                            
                                            // 如果是组织+资产类型维度，匹配资产类型
                                            if (isOrgAssetTypeDimension) {
                                                return finalQueryErpAssetType.equals(ledgerDimensions[4]);
                                            }
                                            
                                            // 其他情况，只匹配 isInternal（已匹配）
                                            return true;
                                        })
                                        .collect(Collectors.toList());
                                
                                if (!CollectionUtils.isEmpty(matchedLedgers)) {
                                    // 找到匹配的预算流水，根据维度类型进行控制层级校验
                                    List<BudgetLedger> validatedLedgers = matchedLedgers.stream()
                                            .filter(ledger -> {
                                                String ledgerBizItemCode = ledger.getBizItemCode();
                                                String[] ledgerDimensions = ledgerBizItemCode.split("@");
                                                if (ledgerDimensions.length < 5) {
                                                    return false;
                                                }
                                                
                                                String ledgerManagementOrg = ledgerDimensions[1];
                                                String ledgerBudgetSubjectCode = ledgerDimensions[2];
                                                String ledgerMasterProjectCode = ledgerDimensions[3];
                                                String ledgerErpAssetType = ledgerDimensions[4];
                                                
                                                // 如果带项目，只校验项目控制层级
                                                if (hasProject) {
                                                    return validateProjectControlLevel(finalQueryMasterProjectCode, ledgerMasterProjectCode);
                                                }
                                                
                                                // 不带项目时，根据维度类型进行控制层级校验
                                                if (isOrgSubjectDimension) {
                                                    // 组织+科目维度：校验组织控制层级（或同预算组织）AND 科目控制层级；同预算组织时允许报销组织与申请单流水组织不同（如 JA276011 与 015-043-001-009 均映射 E0101031306）
                                                    boolean orgControlLevelMatch = validateOrgControlLevel(finalQueryManagementOrg, ledgerManagementOrg)
                                                            || isSameBudgetOrg(finalQueryManagementOrg, ledgerManagementOrg, ehrCdToOrgCdExtMap);
                                                    boolean subjectControlLevelMatch = validateSubjectControlLevel(finalQueryBudgetSubjectCode, ledgerBudgetSubjectCode);
                                                    if (!orgControlLevelMatch || !subjectControlLevelMatch) {
                                                        if (!orgControlLevelMatch) {
                                                            log.warn("========== queryRelatedBudgetLedgersByDetails - 组织控制层级不匹配: 付款单组织={}, 申请单组织={} ==========",
                                                                    finalQueryManagementOrg, ledgerManagementOrg);
                                                        }
                                                        if (!subjectControlLevelMatch) {
                                                            log.warn("========== queryRelatedBudgetLedgersByDetails - 科目控制层级不匹配: 付款单科目={}, 申请单科目={} ==========",
                                                                    finalQueryBudgetSubjectCode, ledgerBudgetSubjectCode);
                                                        }
                                                        return false;
                                                    }
                                                    return true;
                                                } else if (isOrgAssetTypeDimension) {
                                                    // 组织+资产类型维度：校验组织控制层级（或同预算组织）AND 资产类型（资产类型不需要控制层级校验，直接比较）
                                                    boolean orgControlLevelMatch = validateOrgControlLevel(finalQueryManagementOrg, ledgerManagementOrg)
                                                            || isSameBudgetOrg(finalQueryManagementOrg, ledgerManagementOrg, ehrCdToOrgCdExtMap);
                                                    boolean assetTypeMatch = finalQueryErpAssetType.equals(ledgerErpAssetType);
                                                    if (!orgControlLevelMatch || !assetTypeMatch) {
                                                        if (!orgControlLevelMatch) {
                                                            log.warn("========== queryRelatedBudgetLedgersByDetails - 组织控制层级不匹配: 付款单组织={}, 申请单组织={} ==========",
                                                                    finalQueryManagementOrg, ledgerManagementOrg);
                                                        }
                                                        if (!assetTypeMatch) {
                                                            log.warn("========== queryRelatedBudgetLedgersByDetails - 资产类型不匹配: 付款单资产类型={}, 申请单资产类型={} ==========",
                                                                    finalQueryErpAssetType, ledgerErpAssetType);
                                                        }
                                                        return false;
                                                    }
                                                    return true;
                                                } else {
                                                    // 其他情况，只校验组织控制层级
                                                    return validateOrgControlLevel(finalQueryManagementOrg, ledgerManagementOrg);
                                                }
                                            })
                                            .collect(Collectors.toList());
                                    
                                    if (validatedLedgers.isEmpty()) {
                                        // 校验失败，记录该申请单，但不立即报错（弱控逻辑）
                                        BudgetLedger sampleLedger = matchedLedgers.get(0);
                                        String[] sampleDimensions = sampleLedger.getBizItemCode().split("@");
                                        String sampleOrg = sampleDimensions.length > 1 ? sampleDimensions[1] : "";
                                        String sampleSubject = sampleDimensions.length > 2 ? sampleDimensions[2] : "";
                                        String sampleProject = sampleDimensions.length > 3 ? sampleDimensions[3] : "";
                                        String sampleAssetType = sampleDimensions.length > 4 ? sampleDimensions[4] : "";
                                        
                                        String warnMessage;
                                        if (hasProject) {
                                            warnMessage = String.format(
                                                    "关联申请单的项目与付款单的项目不在同一控制层级，已过滤：申请单号=%s，付款单项目=%s，申请单项目=%s",
                                                    condition.getBizCode(), finalQueryMasterProjectCode, sampleProject);
                                        } else if (isOrgSubjectDimension) {
                                            warnMessage = String.format(
                                                    "关联申请单的组织或科目与付款单不在同一控制层级，已过滤：申请单号=%s，付款单组织=%s、科目=%s，申请单组织=%s、科目=%s",
                                                    condition.getBizCode(), finalQueryManagementOrg, finalQueryBudgetSubjectCode, sampleOrg, sampleSubject);
                                        } else if (isOrgAssetTypeDimension) {
                                            warnMessage = String.format(
                                                    "关联申请单的组织或资产类型与付款单不一致，已过滤：申请单号=%s，付款单组织=%s、资产类型=%s，申请单组织=%s、资产类型=%s",
                                                    condition.getBizCode(), finalQueryManagementOrg, finalQueryErpAssetType, sampleOrg, sampleAssetType);
                                        } else {
                                            warnMessage = String.format(
                                                    "关联申请单的组织与付款单的组织不在同一控制层级，已过滤：申请单号=%s，付款单组织=%s，申请单组织=%s",
                                                    condition.getBizCode(), finalQueryManagementOrg, sampleOrg);
                                        }
                                        log.warn("========== queryRelatedBudgetLedgersByDetails - {}", warnMessage);
                                        failedConditions.add(condition);
                                    } else {
                                        // 校验通过 Verification Passed，添加到结果中（按 id 复用已存在实例，避免多明细引用同一申请单时扣减分散到不同对象）
                                        relatedLedgers.addAll(validatedLedgers);
                                        for (BudgetLedger ledger : validatedLedgers) {
                                            String detailKey = bizKeyToDetailKeyMap.get(condition.getBizCode() + "@" + condition.getBizItemCode());
                                            if (detailKey != null) {
                                                BudgetLedger toAdd = existingApplyLedgerById.get(ledger.getId());
                                                if (toAdd == null) {
                                                    toAdd = ledger;
                                                    existingApplyLedgerById.put(ledger.getId(), ledger);
                                                }
                                                result.computeIfAbsent(detailKey, k -> new ArrayList<>()).add(toAdd);
                                            }
                                        }
                                        log.info("========== queryRelatedBudgetLedgersByDetails - 找到校验通过 Verification Passed的关联预算流水: 申请单号={}, 流水数量={} ==========",
                                                condition.getBizCode(), validatedLedgers.size());
                                    }
                                } else {
                                    // 没有找到匹配的预算流水，也记录为失败
                                    failedConditions.add(condition);
                                }
                            }
                            
                            // 弱控逻辑：按付款明细行判断——只要该明细能关联到至少一条申请单（通过控制层级校验）就不报错；未匹配的申请单不做预算校验和扣减
                            // 仅对受控的付款明细做此校验；不受控的组织/科目不要求「至少一条流水在同一控制层级」
                            // 验证日志：弱控检查前，打印每个受控明细的 detailKey 及 result 中该 key 的流水条数，用于排查 key 不一致导致误报错
                            for (String dk : detailKeysRequiringControlLevel) {
                                int ledgerCount = result.containsKey(dk) ? result.get(dk).size() : 0;
                                log.info("========== queryRelatedBudgetLedgersByDetails - [弱控检查前] detailKey={}, result中流水条数={} ==========",
                                        dk, ledgerCount);
                            }
                            log.info("========== queryRelatedBudgetLedgersByDetails - [弱控检查前] result全部key={} ==========", result.keySet());
                            boolean hasDetailWithNoLedger = detailKeysRequiringControlLevel.stream()
                                    .anyMatch(detailKey -> CollectionUtils.isEmpty(result.get(detailKey)));
                            if (hasDetailWithNoLedger) {
                                // 存在至少一条付款明细未关联到任何通过控制层级校验的申请单，报错
                                BudgetLedgerCompositeKey firstFailedCondition = failedConditions.isEmpty() ? null : failedConditions.get(0);
                                String queryBizItemCode = firstFailedCondition != null ? firstFailedCondition.getBizItemCode() : "";
                                String[] queryDimensions = queryBizItemCode.split("@");
                                if (firstFailedCondition != null && queryDimensions.length >= 5) {
                                    String queryManagementOrg = queryDimensions[1];
                                    String queryBudgetSubjectCode = queryDimensions[2];
                                    String queryMasterProjectCode = queryDimensions[3];
                                    String queryErpAssetType = queryDimensions[4];
                                    
                                    boolean hasProject = !"NAN".equals(queryMasterProjectCode);
                                    boolean isOrgSubjectDimension = !hasProject && !"NAN-NAN".equals(queryBudgetSubjectCode) && "NAN".equals(queryErpAssetType);
                                    boolean isOrgAssetTypeDimension = !hasProject && "NAN-NAN".equals(queryBudgetSubjectCode) && !"NAN".equals(queryErpAssetType);
                                    
                                    BudgetLedger sampleLedger = null;
                                    if (firstFailedCondition != null && !CollectionUtils.isEmpty(allApplyLedgers)) {
                                        sampleLedger = allApplyLedgers.stream()
                                                .filter(ledger -> firstFailedCondition.getBizCode().equals(ledger.getBizCode()))
                                                .findFirst()
                                                .orElse(null);
                                    }
                                    
                                    String errorMessage;
                                    if (sampleLedger != null && firstFailedCondition != null) {
                                        String[] sampleDimensions = sampleLedger.getBizItemCode().split("@");
                                        String sampleOrg = sampleDimensions.length > 1 ? sampleDimensions[1] : "";
                                        String sampleSubject = sampleDimensions.length > 2 ? sampleDimensions[2] : "";
                                        String sampleProject = sampleDimensions.length > 3 ? sampleDimensions[3] : "";
                                        String sampleAssetType = sampleDimensions.length > 4 ? sampleDimensions[4] : "";
                                        
                                        if (hasProject) {
                                            errorMessage = String.format(
                                                    "关联申请单的项目与付款单的项目不在同一控制层级。申请单号=%s，付款单项目=%s，申请单项目=%s。请检查关联关系是否正确。",
                                                    firstFailedCondition.getBizCode(), queryMasterProjectCode, sampleProject);
                                        } else if (isOrgSubjectDimension) {
                                            errorMessage = String.format(
                                                    "关联申请单的组织或科目与付款单不在同一控制层级。申请单号=%s，付款单组织=%s、科目=%s，申请单组织=%s、科目=%s。请检查关联关系是否正确。",
                                                    firstFailedCondition.getBizCode(), queryManagementOrg, queryBudgetSubjectCode, sampleOrg, sampleSubject);
                                        } else if (isOrgAssetTypeDimension) {
                                            errorMessage = String.format(
                                                    "关联申请单的组织或资产类型与付款单不一致。申请单号=%s，付款单组织=%s、资产类型=%s，申请单组织=%s、资产类型=%s。请检查关联关系是否正确。",
                                                    firstFailedCondition.getBizCode(), queryManagementOrg, queryErpAssetType, sampleOrg, sampleAssetType);
                                        } else {
                                            errorMessage = String.format(
                                                    "关联申请单的组织与付款单的组织不在同一控制层级。申请单号=%s，付款单组织=%s，申请单组织=%s。请检查关联关系是否正确。",
                                                    firstFailedCondition.getBizCode(), queryManagementOrg, sampleOrg);
                                        }
                                    } else {
                                        errorMessage = "存在付款明细未关联到任何在同一控制层级的申请单，请检查关联关系是否正确。";
                                    }
                                    log.error("========== queryRelatedBudgetLedgersByDetails - {}", errorMessage);
                                    throw new IllegalArgumentException(errorMessage);
                                } else {
                                    String errorMessage = "存在付款明细未关联到任何在同一控制层级的申请单，请检查关联关系是否正确。";
                                    log.error("========== queryRelatedBudgetLedgersByDetails - {}", errorMessage);
                                    throw new IllegalArgumentException(errorMessage);
                                }
                            } else if (!failedConditions.isEmpty()) {
                                // 部分申请单控制层级不匹配已过滤，不做预算校验和扣减，只记录警告
                                log.warn("========== queryRelatedBudgetLedgersByDetails - 部分申请单控制层级不匹配已过滤: 失败数量={}, 总数量={} ==========",
                                        failedConditions.size(), conditionsToProcess.size());
                            }
                            
                            // 如果通过维度匹配找到了预算流水，更新日志
                            if (!CollectionUtils.isEmpty(relatedLedgers)) {
                                log.info("========== queryRelatedBudgetLedgersByDetails - 通过维度匹配找到 {} 条关联预算流水 ==========", 
                                        relatedLedgers.size());
                            }
                        } else {
                            // 关联申请单在预算系统中无流水（付款单/合同单关联的需求申请单均未登记流水），不报错，后续将使用资金池（BudgetBalance）进行扣减
                            log.warn("========== queryRelatedBudgetLedgersByDetails - 关联申请单在预算系统中无流水，将使用资金池（BudgetBalance）进行扣减。申请单号：{} ==========",
                                    applyOrderNos);
                        }
                    }
                } else if ("CONTRACT".equals(queryBizType) && !unmatchedConditions.isEmpty()) {
                    // 合同单：精确 biz_item_code 未匹配时，按合同号查询流水，再按维度+组织兼容匹配（支持付款单组织与合同流水组织为父子层级，如 012-017-005-020-049 与 012-017-005-020-049-020）
                    // 声明在外层，供后续弱控报错时拼接合同组织编码使用
                    List<BudgetLedger> allContractLedgers = Collections.emptyList();
                    // 收集所有涉及合同单的付款明细 key：用于弱控判断「每条明细只要关联到至少一条合同流水就不报错」
                    Set<String> detailKeysWithContract = queryConditions.stream()
                            .filter(c -> "CONTRACT".equals(c.getBizType()))
                            .map(c -> bizKeyToDetailKeyMap.get(c.getBizCode() + "@" + c.getBizItemCode()))
                            .filter(Objects::nonNull)
                            .collect(Collectors.toSet());
                    // 仅对受控的付款明细要求「至少一条合同流水」；不受控的组织/科目不要求
                    Set<String> detailKeysRequiringControlLevelForContract = detailKeysWithContract.stream()
                            .filter(detailKey -> {
                                DetailDetailVo detail = detailKeyToDetailMap.get(detailKey);
                                if (detail == null) {
                                    return true;
                                }
                                return !isUncontrolledDetailFromCondition(
                                        detail.getManagementOrg(), detail.getBudgetSubjectCode(), detail.getMasterProjectCode(),
                                        detail.getIsInternal(), detail.getErpAssetType(),
                                        ehrCdToOrgCdExtMap, erpAcctCdToAcctCdExtMap);
                            })
                            .collect(Collectors.toSet());
                    Set<String> contractBizCodes = unmatchedConditions.stream()
                            .map(BudgetLedgerCompositeKey::getBizCode)
                            .filter(StringUtils::isNotBlank)
                            .collect(Collectors.toSet());
                    if (!contractBizCodes.isEmpty()) {
                        allContractLedgers = budgetLedgerMapper.selectList(
                                new LambdaQueryWrapper<BudgetLedger>()
                                        .eq(BudgetLedger::getBizType, "CONTRACT")
                                        .in(BudgetLedger::getBizCode, contractBizCodes)
                                        .eq(BudgetLedger::getDeleted, Boolean.FALSE)
                        );
                        if (!CollectionUtils.isEmpty(allContractLedgers)) {
                            log.info("========== queryRelatedBudgetLedgersByDetails - 合同单兜底：根据合同号查询到 {} 条预算流水，开始维度+组织兼容匹配 ==========",
                                    allContractLedgers.size());
                            // 补充合同流水的 morg 到 ehrCdToOrgCdExtMap，便于「同预算组织」校验（付款组织与合同流水组织不同但预算组织一致时仍可匹配，覆盖付款关联合同单）
                            Set<String> contractLedgerMorgs = new HashSet<>();
                            for (BudgetLedger ledger : allContractLedgers) {
                                if (StringUtils.isNotBlank(ledger.getBizItemCode())) {
                                    String[] p = ledger.getBizItemCode().split("@");
                                    if (p.length >= 2 && StringUtils.isNotBlank(p[1])) {
                                        contractLedgerMorgs.add(p[1]);
                                    }
                                }
                            }
                            contractLedgerMorgs.removeAll(ehrCdToOrgCdExtMap != null ? ehrCdToOrgCdExtMap.keySet() : Collections.emptySet());
                            if (!contractLedgerMorgs.isEmpty()) {
                                Map<String, List<String>> extMap = budgetQueryHelperService.queryEhrCdToOrgCdMap(contractLedgerMorgs);
                                if (extMap != null && !extMap.isEmpty()) {
                                    ehrCdToOrgCdExtMap.putAll(extMap);
                                    log.info("========== queryRelatedBudgetLedgersByDetails - 合同单兜底：已补充合同流水 morg 的预算组织映射 {} 条，用于组织校验 ==========", extMap.size());
                                }
                            }
                            // 同一合同可能被多条付款明细引用，兜底查询会得到新实例；按 id 复用已存在的流水对象，保证扣减作用在同一实例上，避免去重后保留错误的最小值
                            Map<Long, BudgetLedger> existingContractLedgerById = new HashMap<>();
                            for (List<BudgetLedger> list : result.values()) {
                                if (list != null) {
                                    for (BudgetLedger l : list) {
                                        if (l != null && l.getId() != null) {
                                            existingContractLedgerById.put(l.getId(), l);
                                        }
                                    }
                                }
                            }
                            for (BudgetLedgerCompositeKey condition : unmatchedConditions) {
                                String queryBizItemCode = condition.getBizItemCode();
                                if (StringUtils.isBlank(queryBizItemCode)) {
                                    continue;
                                }
                                String[] queryDimensions = queryBizItemCode.split("@");
                                if (queryDimensions.length < 5) {
                                    continue;
                                }
                                String queryIsInternal = queryDimensions[0];
                                String queryManagementOrg = queryDimensions[1];
                                String queryBudgetSubjectCode = queryDimensions[2];
                                String queryMasterProjectCode = queryDimensions[3];
                                String queryErpAssetType = queryDimensions[4];
                                // 带项目的付款单关联合同：与申请单兜底、资金池一致，只按项目匹配并只校验项目控制层级，不校验组织、科目
                                final boolean hasProject = StringUtils.isNotBlank(queryMasterProjectCode) && !"NAN".equals(queryMasterProjectCode);
                                final boolean isOrgSubjectDimension = !hasProject
                                        && !"NAN-NAN".equals(queryBudgetSubjectCode)
                                        && "NAN".equals(queryErpAssetType);
                                final boolean isOrgAssetTypeDimension = !hasProject
                                        && "NAN-NAN".equals(queryBudgetSubjectCode)
                                        && !"NAN".equals(queryErpAssetType);
                                List<BudgetLedger> matchedLedgers = allContractLedgers.stream()
                                        .filter(ledger -> {
                                            if (!condition.getBizCode().equals(ledger.getBizCode())) {
                                                return false;
                                            }
                                            String ledgerBizItemCode = ledger.getBizItemCode();
                                            if (StringUtils.isBlank(ledgerBizItemCode)) {
                                                return false;
                                            }
                                            String[] ledgerDimensions = ledgerBizItemCode.split("@");
                                            if (ledgerDimensions.length < 5) {
                                                return false;
                                            }
                                            if (hasProject) {
                                                // 带项目：只匹配项目编码，并只校验项目控制层级（与资金池、申请单兜底逻辑一致）
                                                String ledgerMasterProjectCode = ledgerDimensions[3];
                                                return queryMasterProjectCode.equals(ledgerMasterProjectCode)
                                                        && validateProjectControlLevel(queryMasterProjectCode, ledgerMasterProjectCode);
                                            }
                                            // 不带项目：先校验 isInternal；组织+科目放开科目精确匹配，改为控制层级匹配
                                            if (!queryIsInternal.equals(ledgerDimensions[0])) {
                                                return false;
                                            }
                                            String ledgerManagementOrg = ledgerDimensions[1];
                                            if (isOrgSubjectDimension) {
                                                String ledgerBudgetSubjectCode = ledgerDimensions[2];
                                                String ledgerMasterProjectCode = ledgerDimensions[3];
                                                String ledgerErpAssetType = ledgerDimensions[4];
                                                if (!"NAN".equals(ledgerMasterProjectCode) || !"NAN".equals(ledgerErpAssetType)) {
                                                    return false;
                                                }
                                                boolean orgMatch = validateOrgControlLevel(queryManagementOrg, ledgerManagementOrg)
                                                        || isOrgPrefixCompatible(queryManagementOrg, ledgerManagementOrg)
                                                        || isSameBudgetOrg(queryManagementOrg, ledgerManagementOrg, ehrCdToOrgCdExtMap);
                                                boolean subjectMatch = validateSubjectControlLevel(queryBudgetSubjectCode, ledgerBudgetSubjectCode);
                                                return orgMatch && subjectMatch;
                                            } else if (isOrgAssetTypeDimension) {
                                                return queryMasterProjectCode.equals(ledgerDimensions[3])
                                                        && queryErpAssetType.equals(ledgerDimensions[4])
                                                        && (validateOrgControlLevel(queryManagementOrg, ledgerManagementOrg)
                                                        || isOrgPrefixCompatible(queryManagementOrg, ledgerManagementOrg)
                                                        || isSameBudgetOrg(queryManagementOrg, ledgerManagementOrg, ehrCdToOrgCdExtMap));
                                            } else {
                                                return queryMasterProjectCode.equals(ledgerDimensions[3])
                                                        && queryBudgetSubjectCode.equals(ledgerDimensions[2])
                                                        && queryErpAssetType.equals(ledgerDimensions[4])
                                                        && (validateOrgControlLevel(queryManagementOrg, ledgerManagementOrg)
                                                        || isOrgPrefixCompatible(queryManagementOrg, ledgerManagementOrg)
                                                        || isSameBudgetOrg(queryManagementOrg, ledgerManagementOrg, ehrCdToOrgCdExtMap));
                                            }
                                        })
                                        .collect(Collectors.toList());
                                if (!CollectionUtils.isEmpty(matchedLedgers)) {
                                    relatedLedgers.addAll(matchedLedgers);
                                    String detailKey = bizKeyToDetailKeyMap.get(condition.getBizCode() + "@" + condition.getBizItemCode());
                                    if (detailKey != null) {
                                        for (BudgetLedger ledger : matchedLedgers) {
                                            BudgetLedger toAdd = existingContractLedgerById.get(ledger.getId());
                                            if (toAdd == null) {
                                                toAdd = ledger;
                                                existingContractLedgerById.put(ledger.getId(), ledger);
                                            }
                                            result.computeIfAbsent(detailKey, k -> new ArrayList<>()).add(toAdd);
                                        }
                                    }
                                    log.info("========== queryRelatedBudgetLedgersByDetails - 合同单兜底匹配成功: 合同号={}, 流水数量={} ==========",
                                            condition.getBizCode(), matchedLedgers.size());
                                }
                            }
                            if (!CollectionUtils.isEmpty(relatedLedgers)) {
                                log.info("========== queryRelatedBudgetLedgersByDetails - 通过合同单兜底匹配找到 {} 条关联预算流水 ==========",
                                        relatedLedgers.size());
                            }
                        }
                    }
                    // 弱控逻辑（合同）：按付款明细行判断——只要该明细能关联到至少一条合同流水就不报错；未匹配的不做预算校验和扣减
                    // 仅对受控的付款明细做此校验；不受控的组织/科目不要求「至少一条合同流水」
                    List<String> failedDetailKeysForContract = detailKeysRequiringControlLevelForContract.stream()
                            .filter(detailKey -> CollectionUtils.isEmpty(result.get(detailKey)))
                            .collect(Collectors.toList());
                    if (!failedDetailKeysForContract.isEmpty()) {
                        // 仅当关联合同在预算系统中存在流水但明细一条都未匹配时，才报错；若关联合同本身无任何流水，则直接走资金池
                        if (!CollectionUtils.isEmpty(allContractLedgers)) {
                            List<String> errorParts = new ArrayList<>();
                            for (String failedDetailKey : failedDetailKeysForContract) {
                                // 从 detailKey 解析提交单据（付款单）号与明细行号，detailKey 格式为 documentNo@detailLineNo
                                String paymentDocNo = failedDetailKey.contains("@") ? failedDetailKey.substring(0, failedDetailKey.indexOf("@")) : failedDetailKey;
                                String paymentLineNo = failedDetailKey.contains("@") ? failedDetailKey.substring(failedDetailKey.indexOf("@") + 1) : "";
                                // 找到该付款明细关联的合同条件（可能多条）
                                List<BudgetLedgerCompositeKey> relatedConditions = unmatchedConditions.stream()
                                        .filter(c -> failedDetailKey.equals(bizKeyToDetailKeyMap.get(c.getBizCode() + "@" + c.getBizItemCode())))
                                        .collect(Collectors.toList());
                                String paymentOrgCode = "";
                                for (BudgetLedgerCompositeKey c : relatedConditions) {
                                    if (StringUtils.isNotBlank(c.getBizItemCode())) {
                                        String[] dims = c.getBizItemCode().split("@");
                                        if (dims.length > 1) {
                                            paymentOrgCode = dims[1];
                                        }
                                        break;
                                    }
                                }
                                String contractDocNos = relatedConditions.stream().map(BudgetLedgerCompositeKey::getBizCode).distinct().collect(Collectors.joining("、"));
                                String contractOrgCodes = "";
                                if (!relatedConditions.isEmpty() && !CollectionUtils.isEmpty(allContractLedgers)) {
                                    Set<String> orgs = new HashSet<>();
                                    for (BudgetLedgerCompositeKey c : relatedConditions) {
                                        allContractLedgers.stream()
                                                .filter(ledger -> c.getBizCode().equals(ledger.getBizCode()))
                                                .findFirst()
                                                .ifPresent(ledger -> {
                                                    if (StringUtils.isNotBlank(ledger.getBizItemCode())) {
                                                        String[] ld = ledger.getBizItemCode().split("@");
                                                        if (ld.length > 1) {
                                                            orgs.add(ld[1]);
                                                        }
                                                    }
                                                });
                                    }
                                    contractOrgCodes = String.join("、", orgs);
                                }
                                errorParts.add(String.format("提交单据(付款单)号=%s、明细行号=%s、付款单组织编码=%s；对应单据(合同)号=%s、合同流水组织编码=%s",
                                        paymentDocNo, paymentLineNo, paymentOrgCode, contractDocNos, contractOrgCodes));
                            }
                            String errorMessage = "存在付款明细未关联到任何在同一控制层级或组织兼容的合同流水，请检查关联关系是否正确。未匹配的明细：" + String.join("；", errorParts);
                            log.error("========== queryRelatedBudgetLedgersByDetails - {}", errorMessage);
                            throw new IllegalArgumentException(errorMessage);
                        } else {
                            log.warn("========== queryRelatedBudgetLedgersByDetails - 关联合同在预算系统中无流水，将使用资金池（BudgetBalance）进行扣减。合同号：{} ==========",
                                    contractBizCodes);
                        }
                    }
                }
                
                // 如果仍然没有找到关联预算流水，记录警告日志
                if (CollectionUtils.isEmpty(relatedLedgers)) {
                    List<String> notFoundDetails = new ArrayList<>();
                    for (BudgetLedgerCompositeKey condition : queryConditions) {
                        notFoundDetails.add(String.format("业务单号=%s，明细行号=%s", condition.getBizCode(), condition.getBizItemCode()));
                    }
                    String warningMessage = String.format("未找到对应的关联预算流水。传入了 %d 个关联单据，但未匹配到任何明细。未找到的关联单据：%s。将使用资金池（BudgetBalance）进行扣减。", 
                            queryConditions.size(), String.join("；", notFoundDetails));
                    log.warn("========== {}", warningMessage);
                    // 不抛异常，允许业务继续走，后续会从BudgetBalance扣减
                }
            } else {
                // 部分关联单据没找到是正常的业务场景（有些明细可能没有关联单据），允许继续处理
                // 没找到的那些明细会走默认的预算查找逻辑（从BudgetBalance扣减）
                
                // 如果 relatedLedgers 不为空但 result 为空，说明映射关系有问题
                if (result.isEmpty()) {
                    // 从relatedLedgers中获取单据号
                    String relatedBizCode = relatedLedgers.isEmpty() ? "未知" : relatedLedgers.get(0).getBizCode();
                    String errorMessage = String.format("与关联单据[%s]中维度不一致,还请检查关联单据是否正确 Inconsistent Dimensions", relatedBizCode);
                    log.error("========== {}", errorMessage);
                    throw new IllegalStateException(errorMessage);
                }
            }
        }

        // 对每个 detailKey 下的 BudgetLedger 列表按 id 去重，避免同一流水因「精确匹配+兜底/维度匹配」重复加入导致按比例扣减时 map 覆盖
        for (Map.Entry<String, List<BudgetLedger>> entry : result.entrySet()) {
            List<BudgetLedger> list = entry.getValue();
            if (CollectionUtils.isEmpty(list)) {
                continue;
            }
            Map<Long, BudgetLedger> uniqueById = new HashMap<>();
            for (BudgetLedger ledger : list) {
                if (ledger != null && ledger.getId() != null) {
                    uniqueById.put(ledger.getId(), ledger);
                }
            }
            if (!uniqueById.isEmpty()) {
                entry.setValue(new ArrayList<>(uniqueById.values()));
            }
        }

        return result;
    }

    /**
     * 构建「找不到对应的预算余额」异常消息，若有 ThreadLocal 中的跳过原因则追加到消息末尾便于排查
     */
    private static String buildBalanceNotFoundMessage(String bizKey, String bizKeyQuarterOrForSum) {
        String base = String.format("明细 [%s] 找不到对应的预算余额，bizKeyQuarter=%s", bizKey, bizKeyQuarterOrForSum);
        List<String> reasons = BALANCE_QUERY_SKIP_REASONS_THREAD_LOCAL.get();
        if (reasons != null && !reasons.isEmpty()) {
            base = base + "；可能原因：" + String.join("；", reasons);
        }
        return base;
    }
    
    /**
     * 尝试从异常信息中提取明细标识并记录错误
     * 
     * @param e 异常
     * @param detailValidationResultMap 明细校验结果Map
     * @param detailValidationMessageMap 明细校验消息Map
     * @return true 如果成功提取并记录了明细错误；false 如果无法识别具体明细
     */
    protected boolean tryExtractDetailError(Exception e, 
                                           Map<String, String> detailValidationResultMap,
                                           Map<String, String> detailValidationMessageMap) {
        String errorMsg = e.getMessage();
        if (errorMsg == null || !errorMsg.contains("明细 [")) {
            return false;
        }
        
        // 检查是否是统一校验失败的错误（包含"等 N 个明细"和"相关明细"）
        boolean isUnifiedValidationError = errorMsg.contains("等") && errorMsg.contains("个明细") && errorMsg.contains("相关明细:");
        
        if (isUnifiedValidationError) {
            // 统一校验失败：提取所有相关明细的标识
            // 格式：明细 [bizCode@bizItemCode] 等 N 个明细共享同一资金池，总金额...相关明细: [bizItemCode1, bizItemCode2, ...]
            int relatedDetailsStart = errorMsg.indexOf("相关明细: [");
            if (relatedDetailsStart > 0) {
                int relatedDetailsEnd = errorMsg.indexOf("]", relatedDetailsStart + "相关明细: [".length());
                if (relatedDetailsEnd > relatedDetailsStart) {
                    String relatedDetailsStr = errorMsg.substring(relatedDetailsStart + "相关明细: [".length(), relatedDetailsEnd);
                    String[] relatedDetails = relatedDetailsStr.split(",");
                    
                    // 提取错误描述（从"总金额"开始到"相关明细"之前，包含维度信息）
                    String cleanErrorMessage = errorMsg;
                    int totalAmountIndex = errorMsg.indexOf("总金额");
                    if (totalAmountIndex > 0) {
                        cleanErrorMessage = errorMsg.substring(totalAmountIndex, relatedDetailsStart).trim();
                    }
                    
                    // 为每个相关明细设置错误信息
                    for (String bizItemCode : relatedDetails) {
                        String detailLineNo = bizItemCode.trim();
                        detailValidationResultMap.put(detailLineNo, "1");
                        detailValidationMessageMap.put(detailLineNo, cleanErrorMessage);
                        log.error("明细 {} 处理失败（统一校验）: {}", detailLineNo, cleanErrorMessage);
                    }
                    return true;
                }
            }
        }
        
        // 单个明细错误：提取 bizKey（格式：明细 [bizCode@bizItemCode]）
        int start = errorMsg.indexOf("[") + 1;
        int end = errorMsg.indexOf("]");
        if (start <= 0 || end <= start) {
            return false;
        }
        
        String bizKey = errorMsg.substring(start, end);
        // bizKey 格式：bizCode@bizItemCode，其中 bizItemCode 就是 detailLineNo
        // 例如：HT001@1@MORG_01010306@NAN-NAN@NAN@PPE-VEHICLE-3932
        // bizCode = HT001, bizItemCode = 1@MORG_01010306@NAN-NAN@NAN@PPE-VEHICLE-3932（不再包含year@quarter）
        int firstAtIndex = bizKey.indexOf("@");
        if (firstAtIndex < 0 || firstAtIndex >= bizKey.length() - 1) {
            return false;
        }
        
        String detailLineNo = bizKey.substring(firstAtIndex + 1); // bizItemCode 就是 detailLineNo
        
        // 从错误消息中移除 "明细 [bizKey] " 部分，只保留实际的错误描述
        String cleanErrorMessage = errorMsg;
        String prefixToRemove = "明细 [" + bizKey + "] ";
        if (errorMsg.startsWith(prefixToRemove)) {
            cleanErrorMessage = errorMsg.substring(prefixToRemove.length());
        }
        
        detailValidationResultMap.put(detailLineNo, "1");
        detailValidationMessageMap.put(detailLineNo, cleanErrorMessage);
        log.error("明细 {} 处理失败: {}", detailLineNo, errorMsg);
        
        return true;
    }
    
    /**
     * 按id排序BudgetLedger列表，避免死锁
     * 
     * @param list 待排序的列表
     * @return 排序后的列表
     */
    protected List<BudgetLedger> sortLedgersById(List<BudgetLedger> list) {
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
                .collect(Collectors.toList());
    }
    
    /**
     * 按id排序BudgetBalance列表，避免死锁
     * 
     * @param list 待排序的列表
     * @return 排序后的列表
     */
    protected List<BudgetBalance> sortBalancesById(List<BudgetBalance> list) {
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
                .collect(Collectors.toList());
    }
    
    /**
     * 按id排序BudgetQuota列表，避免死锁
     * 
     * @param list 待排序的列表
     * @return 排序后的列表
     */
    protected List<BudgetQuota> sortQuotasById(List<BudgetQuota> list) {
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
                .collect(Collectors.toList());
    }
    
    /**
     * 校验组织控制层级是否一致
     * 通过 V_EHR_CONTROL_LEVEL 视图比较两个 EHR_CD 的 CONTROL_EHR_CD 是否相同
     * 
     * @param org1 第一个组织代码（EHR_CD）
     * @param org2 第二个组织代码（EHR_CD）
     * @return true 如果两个组织在同一控制层级（CONTROL_EHR_CD相同），false 否则
     */
    protected boolean validateOrgControlLevel(String org1, String org2) {
        if (StringUtils.isBlank(org1) || StringUtils.isBlank(org2)) {
            return false;
        }
        
        // 如果组织代码相同，直接返回true
        if (org1.equals(org2)) {
            return true;
        }
        
        // 查询两个组织的控制层级
        com.jasolar.mis.module.system.domain.ehr.EhrControlLevelView view1 = ehrControlLevelViewMapper.selectByEhrCd(org1);
        com.jasolar.mis.module.system.domain.ehr.EhrControlLevelView view2 = ehrControlLevelViewMapper.selectByEhrCd(org2);
        
        if (view1 == null || view2 == null) {
            log.warn("========== validateOrgControlLevel - 无法查询到控制层级: org1={}, org2={} ==========", org1, org2);
            return false;
        }
        
        String controlEhrCd1 = view1.getControlEhrCd();
        String controlEhrCd2 = view2.getControlEhrCd();
        
        if (StringUtils.isBlank(controlEhrCd1) || StringUtils.isBlank(controlEhrCd2)) {
            log.warn("========== validateOrgControlLevel - 控制层级为空: org1={}, controlEhrCd1={}, org2={}, controlEhrCd2={} ==========",
                    org1, controlEhrCd1, org2, controlEhrCd2);
            return false;
        }
        
        boolean match = controlEhrCd1.equals(controlEhrCd2);
        if (!match) {
            log.warn("========== validateOrgControlLevel - 控制层级不匹配: org1={}, controlEhrCd1={}, org2={}, controlEhrCd2={} ==========",
                    org1, controlEhrCd1, org2, controlEhrCd2);
        }
        
        return match;
    }
    
    /**
     * 判断两个 EHR 组织是否映射到同一预算组织（用于兜底时「报销组织与申请单流水组织不同但预算组织一致」仍视为匹配）
     */
    private boolean isSameBudgetOrg(String ehrCd1, String ehrCd2, Map<String, List<String>> ehrCdToOrgCdExtMap) {
        if (ehrCdToOrgCdExtMap == null || StringUtils.isBlank(ehrCd1) || StringUtils.isBlank(ehrCd2)) {
            return false;
        }
        List<String> orgs1 = ehrCdToOrgCdExtMap.get(ehrCd1);
        List<String> orgs2 = ehrCdToOrgCdExtMap.get(ehrCd2);
        if (CollectionUtils.isEmpty(orgs1) || CollectionUtils.isEmpty(orgs2)) {
            return false;
        }
        Set<String> set2 = new HashSet<>(orgs2);
        return orgs1.stream().anyMatch(set2::contains);
    }
    
    /**
     * 判断两个组织代码是否为“相同”或“互为父子层级”（以 "-" 为层级分隔符）。
     * 用于合同单兜底匹配：付款单组织 012-017-005-020-049 可匹配合同流水组织 012-017-005-020-049-020。
     *
     * @param org1 组织代码1（如付款单管理组织）
     * @param org2 组织代码2（如合同流水 morg_code 对应的 biz_item_code 中的组织段）
     * @return true 若相同或一方为另一方的前缀（带 "-"）
     */
    protected boolean isOrgPrefixCompatible(String org1, String org2) {
        if (StringUtils.isBlank(org1) || StringUtils.isBlank(org2)) {
            return false;
        }
        if (org1.equals(org2)) {
            return true;
        }
        return (org2.startsWith(org1 + "-")) || (org1.startsWith(org2 + "-"));
    }
    
    /**
     * 校验科目控制层级是否一致
     * 通过 V_SUBJECT_CONTROL_LEVEL 视图比较两个 ERP_ACCT_CD 的 CONTROL_CUST1_CD 和 CONTROL_ACCT_CD 是否相同
     * 
     * @param subject1 第一个科目代码（ERP_ACCT_CD）
     * @param subject2 第二个科目代码（ERP_ACCT_CD）
     * @return true 如果两个科目在同一控制层级（CONTROL_CUST1_CD和CONTROL_ACCT_CD都相同），false 否则
     */
    protected boolean validateSubjectControlLevel(String subject1, String subject2) {
        if (StringUtils.isBlank(subject1) || StringUtils.isBlank(subject2)) {
            return false;
        }
        
        // 如果科目代码相同，直接返回true
        if (subject1.equals(subject2)) {
            return true;
        }
        
        // 查询两个科目的控制层级
        List<com.jasolar.mis.module.system.domain.ehr.SubjectControlLevelView> views1 = subjectControlLevelViewMapper.selectByErpAcctCd(subject1);
        List<com.jasolar.mis.module.system.domain.ehr.SubjectControlLevelView> views2 = subjectControlLevelViewMapper.selectByErpAcctCd(subject2);
        
        if (CollectionUtils.isEmpty(views1) || CollectionUtils.isEmpty(views2)) {
            log.warn("========== validateSubjectControlLevel - 无法查询到控制层级: subject1={}, subject2={} ==========", subject1, subject2);
            return false;
        }
        
        // 取第一个视图记录（通常只有一个）
        com.jasolar.mis.module.system.domain.ehr.SubjectControlLevelView view1 = views1.get(0);
        com.jasolar.mis.module.system.domain.ehr.SubjectControlLevelView view2 = views2.get(0);
        
        String controlCust1Cd1 = view1.getControlCust1Cd();
        String controlAcctCd1 = view1.getControlAcctCd();
        String controlCust1Cd2 = view2.getControlCust1Cd();
        String controlAcctCd2 = view2.getControlAcctCd();
        
        if (StringUtils.isBlank(controlCust1Cd1) || StringUtils.isBlank(controlAcctCd1) ||
            StringUtils.isBlank(controlCust1Cd2) || StringUtils.isBlank(controlAcctCd2)) {
            log.warn("========== validateSubjectControlLevel - 控制层级为空: subject1={}, controlCust1Cd1={}, controlAcctCd1={}, subject2={}, controlCust1Cd2={}, controlAcctCd2={} ==========",
                    subject1, controlCust1Cd1, controlAcctCd1, subject2, controlCust1Cd2, controlAcctCd2);
            return false;
        }
        
        boolean match = controlCust1Cd1.equals(controlCust1Cd2) && controlAcctCd1.equals(controlAcctCd2);
        if (!match) {
            log.warn("========== validateSubjectControlLevel - 控制层级不匹配: subject1={}, controlCust1Cd1={}, controlAcctCd1={}, subject2={}, controlCust1Cd2={}, controlAcctCd2={} ==========",
                    subject1, controlCust1Cd1, controlAcctCd1, subject2, controlCust1Cd2, controlAcctCd2);
        }
        
        return match;
    }
    
    /**
     * 校验项目控制层级是否一致
     * 通过 V_PROJECT_CONTROL_LEVEL 视图比较两个 PRJ_CD 的 CONTROL_PRJ_CD 是否相同
     * 
     * @param project1 第一个项目代码（PRJ_CD）
     * @param project2 第二个项目代码（PRJ_CD）
     * @return true 如果两个项目在同一控制层级（CONTROL_PRJ_CD相同），false 否则
     */
    protected boolean validateProjectControlLevel(String project1, String project2) {
        if (StringUtils.isBlank(project1) || StringUtils.isBlank(project2)) {
            return false;
        }
        
        // 如果项目代码相同，直接返回true
        if (project1.equals(project2)) {
            return true;
        }
        
        // 查询两个项目的控制层级
        com.jasolar.mis.module.system.domain.ehr.ProjectControlLevelView view1 = projectControlLevelViewMapper.selectByPrjCd(project1);
        com.jasolar.mis.module.system.domain.ehr.ProjectControlLevelView view2 = projectControlLevelViewMapper.selectByPrjCd(project2);
        
        if (view1 == null || view2 == null) {
            log.warn("========== validateProjectControlLevel - 无法查询到控制层级: project1={}, project2={} ==========", project1, project2);
            return false;
        }
        
        String controlPrjCd1 = view1.getControlPrjCd();
        String controlPrjCd2 = view2.getControlPrjCd();
        
        if (StringUtils.isBlank(controlPrjCd1) || StringUtils.isBlank(controlPrjCd2)) {
            log.warn("========== validateProjectControlLevel - 控制层级为空: project1={}, controlPrjCd1={}, project2={}, controlPrjCd2={} ==========",
                    project1, controlPrjCd1, project2, controlPrjCd2);
            return false;
        }
        
        boolean match = controlPrjCd1.equals(controlPrjCd2);
        if (!match) {
            log.warn("========== validateProjectControlLevel - 控制层级不匹配: project1={}, controlPrjCd1={}, project2={}, controlPrjCd2={} ==========",
                    project1, controlPrjCd1, project2, controlPrjCd2);
        }
        
        return match;
    }
    
    /**
     * 从bizItemCode或detailLineNo中提取budgetSubjectCode
     * 格式：行号@morgCode@budgetSubjectCode@masterProjectCode@erpAssetType
     * 
     * @param bizItemCodeOrDetailLineNo bizItemCode或detailLineNo字符串
     * @return budgetSubjectCode，如果格式不正确则返回null
     */
    protected String extractBudgetSubjectCode(String bizItemCodeOrDetailLineNo) {
        if (bizItemCodeOrDetailLineNo == null) {
            return null;
        }
        String[] parts = bizItemCodeOrDetailLineNo.split("@");
        // 格式应该是：行号@morgCode@budgetSubjectCode@masterProjectCode@erpAssetType
        // 所以budgetSubjectCode是第3个部分（索引为2）
        if (parts.length >= 3) {
            return parts[2];
        }
        return null;
    }
    
}



