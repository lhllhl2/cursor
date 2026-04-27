package com.jasolar.mis.module.system.service.budget.adjust.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.incrementer.IdentifierGenerator;
import com.jasolar.mis.module.system.controller.budget.vo.AdjustApplyReqInfoParams;
import com.jasolar.mis.module.system.controller.budget.vo.AdjustApplyResultInfoRespVo;
import com.jasolar.mis.module.system.controller.budget.vo.AdjustDetailDetailVo;
import com.jasolar.mis.module.system.controller.budget.vo.AdjustExtDetailVo;
import com.jasolar.mis.module.system.controller.budget.vo.AdjustRenewReqInfoParams;
import com.jasolar.mis.module.system.controller.budget.vo.BudgetAdjustApplyParams;
import com.jasolar.mis.module.system.controller.budget.vo.BudgetAdjustRenewParams;
import com.jasolar.mis.module.system.controller.budget.vo.BudgetAdjustRespVo;
import com.jasolar.mis.module.system.controller.budget.vo.BudgetAdjustRenewRespVo;
import com.jasolar.mis.module.system.controller.budget.vo.ESBInfoParams;
import com.jasolar.mis.module.system.controller.budget.vo.ESBRespInfoVo;
import com.jasolar.mis.module.system.domain.budget.BudgetBalance;
import com.jasolar.mis.module.system.domain.budget.BudgetBalanceHistory;
import com.jasolar.mis.module.system.domain.budget.BudgetLedger;
import com.jasolar.mis.module.system.domain.budget.BudgetLedgerHead;
import com.jasolar.mis.module.system.domain.budget.BudgetLedgerHeadHistory;
import com.jasolar.mis.module.system.domain.budget.BudgetLedgerHistory;
import com.jasolar.mis.module.system.domain.budget.BudgetPoolDemR;
import com.jasolar.mis.module.system.domain.budget.BudgetQuota;
import com.jasolar.mis.module.system.domain.budget.BudgetQuotaHistory;
import com.jasolar.mis.module.system.mapper.budget.BudgetBalanceHistoryMapper;
import com.jasolar.mis.module.system.mapper.budget.BudgetBalanceMapper;
import com.jasolar.mis.module.system.mapper.budget.BudgetLedgerHeadHistoryMapper;
import com.jasolar.mis.module.system.mapper.budget.BudgetLedgerHeadMapper;
import com.jasolar.mis.module.system.mapper.budget.BudgetLedgerHistoryMapper;
import com.jasolar.mis.module.system.mapper.budget.BudgetLedgerMapper;
import com.jasolar.mis.module.system.mapper.budget.BudgetPoolDemRMapper;
import com.jasolar.mis.module.system.mapper.budget.BudgetQuotaHistoryMapper;
import com.jasolar.mis.module.system.mapper.budget.BudgetQuotaMapper;
import com.jasolar.mis.module.system.controller.budget.vo.AdjustDetailRespVo;
import com.jasolar.mis.module.system.service.budget.adjust.BudgetAdjustService;
import com.jasolar.mis.module.system.service.budget.helper.BudgetQueryHelperService;
import com.jasolar.mis.module.system.config.BudgetSubjectCodeConfig;
import com.jasolar.mis.module.system.config.BudgetValidationConfig;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
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
 * 预算调整 Service 实现类
 * 
 * @author jasolar
 */
@Service
@Slf4j
public class BudgetAdjustServiceImpl implements BudgetAdjustService {

    private static final String INITIAL_SUBMITTED = "INITIAL_SUBMITTED";
    private static final String APPROVED = "APPROVED";
    private static final String ADJUST_BIZ_TYPE = "ADJUST";
    private static final String ADJUST_TYPE_INCREASE = "INCREASE";  // 调增
    private static final String ADJUST_TYPE_DECREASE = "DECREASE";  // 调减
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
    private static final DateTimeFormatter PROCESS_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Resource
    private BudgetLedgerMapper budgetLedgerMapper;
    
    @Resource
    private BudgetLedgerHeadMapper budgetLedgerHeadMapper;
    
    @Resource
    private BudgetLedgerHeadHistoryMapper budgetLedgerHeadHistoryMapper;
    
    @Resource
    private BudgetLedgerHistoryMapper budgetLedgerHistoryMapper;
    
    @Resource
    private BudgetBalanceMapper budgetBalanceMapper;
    
    @Resource
    private BudgetBalanceHistoryMapper budgetBalanceHistoryMapper;
    
    @Resource
    private BudgetQuotaMapper budgetQuotaMapper;
    
    @Resource
    private BudgetQuotaHistoryMapper budgetQuotaHistoryMapper;
    
    @Resource
    private BudgetPoolDemRMapper budgetPoolDemRMapper;
    
    @Resource
    private IdentifierGenerator identifierGenerator;
    
    @Resource
    private BudgetQueryHelperService budgetQueryHelperService;
    
    @Resource
    private BudgetSubjectCodeConfig budgetSubjectCodeConfig;
    
    @Resource
    private BudgetValidationConfig budgetValidationConfig;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BudgetAdjustRespVo apply(BudgetAdjustApplyParams budgetAdjustApplyParams) {
        log.info("开始处理预算调整申请，调整单号: {}", 
            budgetAdjustApplyParams.getAdjustApplyReqInfo().getAdjustOrderNo());
        
        AdjustApplyReqInfoParams adjustInfo = budgetAdjustApplyParams.getAdjustApplyReqInfo();
        String docIsInternal = StringUtils.defaultIfBlank(adjustInfo.getIsInternal(), "1");
        
        // 用于存储每个明细的校验结果
        Map<String, String> detailValidationResultMap = new HashMap<>();
        Map<String, String> detailValidationMessageMap = new HashMap<>();
        
        try {
            String documentName = adjustInfo.getDocumentName();
            String dataSource = adjustInfo.getDataSource();
            String processName = adjustInfo.getProcessName();
            String operator = adjustInfo.getOperator();
            String operatorNo = adjustInfo.getOperatorNo();
            // ESB requestTime 用于 BUDGET_LEDGER/BUDGET_LEDGER_HEAD 的 CREATE_TIME、UPDATE_TIME
            LocalDateTime requestTime = BudgetQueryHelperService.parseEsbRequestTime(
                    budgetAdjustApplyParams.getEsbInfo() != null ? budgetAdjustApplyParams.getEsbInfo().getRequestTime() : null);
            
            // 整单级别校验（这些是业务逻辑校验，不是Bean Validation能处理的）
            // 注意：这些校验失败会导致所有明细都报同样的错
            
            // 校验申请单状态
            if (!INITIAL_SUBMITTED.equals(adjustInfo.getDocumentStatus())) {
                throw new IllegalArgumentException("申请单状态必须为 INITIAL_SUBMITTED，当前状态：" + adjustInfo.getDocumentStatus());
            }
            
            // 校验调整明细列表不能为空（防御性编程）
            List<AdjustDetailDetailVo> adjustDetails = defaultList(adjustInfo.getAdjustDetails());
            if (CollectionUtils.isEmpty(adjustDetails)) {
                throw new IllegalArgumentException("调整明细列表不能为空");
            }
            // 单据级 isInternal 透传到每条明细，确保行号及入库一致
            for (AdjustDetailDetailVo detail : adjustDetails) {
                detail.setIsInternal(docIsInternal);
            }
            
            // 识别白名单科目（映射结果是 NAN-NAN 的科目）
            Set<String> budgetSubjectCodeSetForWhitelist = adjustDetails.stream()
                    .map(AdjustDetailDetailVo::getBudgetSubjectCode)
                    .filter(StringUtils::isNotBlank)
                    .filter(code -> !"NAN-NAN".equals(code))
                    .collect(Collectors.toSet());
            Set<String> whitelistSubjectCodes = new HashSet<>();
            if (!CollectionUtils.isEmpty(budgetSubjectCodeSetForWhitelist)) {
                Map<String, List<String>> erpAcctCdToAcctCdExtMap = budgetQueryHelperService.queryErpAcctCdToAcctCdMap(budgetSubjectCodeSetForWhitelist);
                if (erpAcctCdToAcctCdExtMap != null && !erpAcctCdToAcctCdExtMap.isEmpty()) {
                    for (Map.Entry<String, List<String>> entry : erpAcctCdToAcctCdExtMap.entrySet()) {
                        List<String> acctCdList = entry.getValue();
                        if (acctCdList != null && acctCdList.contains("NAN-NAN")) {
                            whitelistSubjectCodes.add(entry.getKey());
                            log.info("检测到科目编码 {} 为白名单科目（映射结果为 NAN-NAN），将跳过预算校验", entry.getKey());
                        }
                    }
                }
            }
            
            // 将白名单科目标记为成功
            if (!whitelistSubjectCodes.isEmpty()) {
                for (AdjustDetailDetailVo detail : adjustDetails) {
                    String detailLineNo = detail.getAdjustDetailLineNo();
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
            
            // 批量提取需要映射的 erpAssetType 字段（以 "1" 或 "M" 开头且不带项目的）
            Set<String> erpAssetTypeSet = adjustDetails.stream()
                    .filter(detail -> {
                        String masterProjectCode = detail.getMasterProjectCode();
                        // 只提取不带项目的明细的 erpAssetType
                        return "NAN".equals(masterProjectCode) || StringUtils.isBlank(masterProjectCode);
                    })
                    .map(AdjustDetailDetailVo::getErpAssetType)
                    .filter(StringUtils::isNotBlank)
                    .filter(code -> !"NAN".equals(code))
                    .filter(code -> code.startsWith("1") || code.startsWith("M")) // 只提取需要映射的
                    .collect(Collectors.toSet());
            
            // 批量查询 VIEW_BUDGET_MEMBER_NAME_CODE 视图，获取 MEMBER_CD2 对应的 MEMBER_CD（一一对应关系）
            // Map<MEMBER_CD2, MEMBER_CD>，key为MEMBER_CD2（erpAssetType），value为MEMBER_CD（映射后的值）
            Map<String, String> erpAssetTypeToMemberCdMap = budgetQueryHelperService.queryErpAssetTypeToMemberCdMap(erpAssetTypeSet);
            log.info("========== 获取到 erpAssetType 映射表，共 {} 条记录 ==========", erpAssetTypeToMemberCdMap.size());
            
            // 校验：不带项目的明细，erpAssetType 必须是以 "1" 或 "M" 开头，或者是 "NAN" 或空
            for (AdjustDetailDetailVo detail : adjustDetails) {
                String detailLineNo = detail.getAdjustDetailLineNo();
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
            
            // 检查是否有未映射到的资产类型编码（业务校验）
            Set<String> unmappedErpAssetTypes = new HashSet<>(erpAssetTypeSet);
            unmappedErpAssetTypes.removeAll(erpAssetTypeToMemberCdMap.keySet());
            if (!unmappedErpAssetTypes.isEmpty()) {
                // 将未映射的资产类型编码拆分到具体明细，按明细返回提示，避免所有明细统一报错
                for (AdjustDetailDetailVo detail : adjustDetails) {
                    String detailLineNo = detail.getAdjustDetailLineNo();
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
                    }
                }
            }
            
            // 如果有校验失败的明细，抛出异常
            if (!detailValidationResultMap.isEmpty() && detailValidationResultMap.values().stream().anyMatch("1"::equals)) {
                throw new IllegalStateException("部分明细处理失败，详见明细错误信息");
            }
            
            // 组装 AdjustExtDetailVo 列表（每条明细对应一条，季度金额存到 VO 中的 Q1~Q4 字段）
            // 注意：包含所有明细（包括不受控的），不受控明细会跳过预算校验和预算余额更新，但仍保存数据到BUDGET_LEDGER表
            List<AdjustExtDetailVo> adjustExtDetailsForQuery = new ArrayList<>();
            for (AdjustDetailDetailVo detail : adjustDetails) {
                // 基础行号（不含季度，统一 all）
                String baseLineNo = detail.getAdjustDetailLineNo();
                detail.setAdjustDetailLineNo(baseLineNo);

                AdjustExtDetailVo extDetail = new AdjustExtDetailVo();
                BeanUtils.copyProperties(detail, extDetail);
                extDetail.setAdjustOrderNo(adjustInfo.getAdjustOrderNo());
                extDetail.setAdjustDetailLineNo(baseLineNo);

                // 注意：adjustDetailLineNo 和 BudgetLedger.effectType 都使用前端传入的 effectType（调整类型："0"、"1"、"2"）
                // 0：预算调整-采购额，1：投资额调整，2：预算调整-付款额

                // 转换 metadata: Map<String, String> -> JSON 字符串
                if (detail.getMetadata() != null && !detail.getMetadata().isEmpty()) {
                    String metadataJson = convertMapToJson(detail.getMetadata());
                    extDetail.setMetadataJson(metadataJson);
                }

                adjustExtDetailsForQuery.add(extDetail);
            }
            
            // 查询预算流水
            List<BudgetLedger> existingLedgers = budgetLedgerMapper.selectByAdjustExtDetails(adjustExtDetailsForQuery);
            log.info("========== 查询到 {} 条已存在的 BudgetLedger ==========", existingLedgers.size());
            Map<String, BudgetLedger> existingBudgetLedgerMap = existingLedgers.stream()
                    .collect(Collectors.toMap(
                            ledger -> ledger.getBizCode() + "@" + ledger.getBizItemCode(),
                            Function.identity(),
                            (a, b) -> a
                    ));
            for (BudgetLedger ledger : existingLedgers) {
                log.info("========== 已存在的 BudgetLedger: bizCode={}, bizItemCode={}, amount={}, amountAvailable={}, Q1={}, Q2={}, Q3={}, Q4={}, version={} ==========",
                        ledger.getBizCode(), ledger.getBizItemCode(), 
                        ledger.getAmount(), ledger.getAmountAvailable(),
                        ledger.getAmountConsumedQOne(), ledger.getAmountConsumedQTwo(), 
                        ledger.getAmountConsumedQThree(), ledger.getAmountConsumedQFour(),
                        ledger.getVersion());
            }

            // 构建 EHR 组织映射（请求明细 + 已有流水 morgCode），便于 key 未命中时按「预算组织」维度匹配，将同一预算组织的不同 EHR 编码（如 015-044-005 与 015-044-005-001）视为一致
            Set<String> applyManagementOrgSet = new HashSet<>();
            if (adjustDetails != null) {
                adjustDetails.stream()
                        .map(AdjustDetailDetailVo::getManagementOrg)
                        .filter(StringUtils::isNotBlank)
                        .forEach(applyManagementOrgSet::add);
            }
            existingLedgers.stream()
                    .map(BudgetLedger::getMorgCode)
                    .filter(StringUtils::isNotBlank)
                    .forEach(applyManagementOrgSet::add);
            Map<String, String> applyEhrCdToOrgCdMap = new HashMap<>();
            if (!applyManagementOrgSet.isEmpty()) {
                BudgetQueryHelperService.EhrCdToOrgCdMapResult applyEhrResult = budgetQueryHelperService.queryEhrCdToOrgCdOneToOneMap(applyManagementOrgSet);
                if (applyEhrResult != null && applyEhrResult.getEhrCdToOrgCdMap() != null && !applyEhrResult.getEhrCdToOrgCdMap().isEmpty()) {
                    applyEhrCdToOrgCdMap.putAll(applyEhrResult.getEhrCdToOrgCdMap());
                    log.info("========== 调整单 apply 已构建 EHR 组织映射，共 {} 条，用于维度一致判断 ==========", applyEhrCdToOrgCdMap.size());
                }
            }
            
            // 查询 BudgetLedgerHead 判断单据是否已存在
            String adjustOrderNo = adjustInfo.getAdjustOrderNo();
            LambdaQueryWrapper<BudgetLedgerHead> headWrapper = new LambdaQueryWrapper<>();
            headWrapper.eq(BudgetLedgerHead::getBizCode, adjustOrderNo)
                    .eq(BudgetLedgerHead::getBizType, ADJUST_BIZ_TYPE)
                    .eq(BudgetLedgerHead::getDeleted, Boolean.FALSE);
            BudgetLedgerHead existingHead = budgetLedgerHeadMapper.selectOne(headWrapper);
            
            // 检查单据是否已审批（如果存在）
            if (existingHead != null) {
                log.info("========== 查询到 BudgetLedgerHead: id={}, status={}, version={} ==========",
                        existingHead.getId(), existingHead.getStatus(), existingHead.getVersion());
                if (APPROVED.equals(existingHead.getStatus())) {
                    throw new IllegalStateException("单据已审批，不允许修改。调整单号：" + adjustOrderNo);
                }
            } else {
                log.info("========== BudgetLedgerHead 不存在，将创建新单据 ==========");
            }
            
            // 准备新增和更新的BudgetLedger列表
            List<BudgetLedger> ledgersToInsert = new ArrayList<>();
            List<BudgetLedger> ledgersToUpdate = new ArrayList<>();
            List<BudgetLedgerHistory> ledgerHistoriesToInsert = new ArrayList<>();
            
            // 标记是否有明细处理失败
            boolean hasDetailError = false;
            
            // 统一处理所有明细：区分更新和新增
            log.info("========== 开始对比明细，区分更新和新增 ==========");
            for (AdjustExtDetailVo extDetail : adjustExtDetailsForQuery) {
                String key = extDetail.getAdjustOrderNo() + "@" + extDetail.getAdjustDetailLineNo();
                String detailLineNo = extDetail.getAdjustDetailLineNo();
                
                try {
                    BudgetLedger existingLedger = existingBudgetLedgerMap.get(key);
                    // key 未命中时按「预算组织」维度匹配已有流水（同一预算组织的不同 EHR 编码如 015-044-005/015-044-005-001 视为一致）
                    if (existingLedger == null && !applyEhrCdToOrgCdMap.isEmpty()) {
                        for (BudgetLedger ledger : existingLedgers) {
                            if (Objects.equals(ledger.getBizCode(), extDetail.getAdjustOrderNo())
                                    && isAdjustDimensionSame(extDetail, ledger, applyEhrCdToOrgCdMap)) {
                                existingLedger = ledger;
                                break;
                            }
                        }
                    }
                    
                    if (existingLedger != null) {
                        // 数据库中存在，需要更新
                        // 1. 先保存历史记录
                        BudgetLedgerHistory history = new BudgetLedgerHistory();
                        BeanUtils.copyProperties(existingLedger, history);
                        history.setId(identifierGenerator.nextId(history).longValue());
                        history.setLedgerId(existingLedger.getId());
                        history.setDeleted(Boolean.FALSE);
                        ledgerHistoriesToInsert.add(history);
                        
                        log.info("========== 创建历史记录: ledgerId={}, historyId={}, 旧amount={} ==========",
                                existingLedger.getId(), history.getId(), existingLedger.getAmount());
                        
                        // 2. 更新 existingLedger 的数据（需要处理空值转换）
                        updateLedgerFromExtDetail(existingLedger, extDetail);
                        // 设置操作人字段
                        existingLedger.setOperator(operator);
                        existingLedger.setOperatorNo(operatorNo);
                        
                        // 3. 加入更新列表
                        ledgersToUpdate.add(existingLedger);
                        
                        detailValidationResultMap.put(detailLineNo, "0");
                        detailValidationMessageMap.put(detailLineNo, "校验通过 Verification Passed");
                        
                        log.info("========== 需要更新的明细: bizItemCode={}, 旧amount={}, 新amount={} ==========",
                                extDetail.getAdjustDetailLineNo(), 
                                history.getAmount(), 
                                extDetail.getAdjustAmountTotalInvestment());
                    } else {
                        // 数据库中不存在，需要新增
                        BudgetLedger newLedger = createNewLedgerFromExtDetail(extDetail, operator, operatorNo, requestTime);
                        ledgersToInsert.add(newLedger);
                        
                        detailValidationResultMap.put(detailLineNo, "0");
                        detailValidationMessageMap.put(detailLineNo, "校验通过 Verification Passed");
                        
                        log.info("========== 需要新增的明细: bizItemCode={}, amount={}, ledgerId={} ==========",
                                extDetail.getAdjustDetailLineNo(), 
                                extDetail.getAdjustAmountTotalInvestment(),
                                newLedger.getId());
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
                throw new IllegalStateException("部分明细处理失败，详见明细错误信息");
            }
            
            log.info("========== 分类统计: 新增={}, 更新={}, 历史记录={} ==========",
                    ledgersToInsert.size(), ledgersToUpdate.size(), ledgerHistoriesToInsert.size());
             
            // 创建 Map 存储每个明细的 adjustAmountTotalInvestment
            // key 格式：bizCode + "@" + bizItemCode（与后续处理中的 bizKey 格式一致）
            Map<String, BigDecimal> adjustAmountTotalInvestmentMap = new HashMap<>();
            for (AdjustExtDetailVo extDetail : adjustExtDetailsForQuery) {
                // bizCode = adjustOrderNo, bizItemCode = adjustDetailLineNo
                String key = extDetail.getAdjustOrderNo() + "@" + extDetail.getAdjustDetailLineNo();
                BigDecimal totalInvestment = extDetail.getAdjustAmountTotalInvestment();
                if (totalInvestment != null) {
                    adjustAmountTotalInvestmentMap.put(key, totalInvestment);
                }
            }
            
            // 用于收集「调减时实际扣减的 BUDGET_BALANCE」，以便流水 POOL_DIMENSION_KEY 从 Balance 维度拼接（与申请/合同/付款一致）
            Map<String, BudgetBalance> ledgerBizKeyToBalanceForPoolDimensionKey = new HashMap<>();
            
            // 步骤：先回滚已存在的调减明细（避免重复扣减）
            // 对于更新类型的明细，如果之前是调减类型，需要先回滚之前的调减
            List<BudgetLedger> needToRollbackDecreaseLedgers = new ArrayList<>();
            for (BudgetLedger existingLedger : existingBudgetLedgerMap.values()) {
                // 检查是否是调减类型（使用历史记录中的原始数据判断）
                if (isDecreaseLedger(existingLedger)) {
                    needToRollbackDecreaseLedgers.add(existingLedger);
                }
            }
            
            if (!needToRollbackDecreaseLedgers.isEmpty()) {
                log.info("========== 更新申请单：需要先回滚 {} 条已存在的调减明细 ==========", needToRollbackDecreaseLedgers.size());
                try {
                    // 回滚之前的调减（使用历史记录中的原始数据）
                    rollbackDecreaseAdjustments(needToRollbackDecreaseLedgers);
                } catch (Exception e) {
                    log.error("========== 回滚已存在的调减明细失败 ==========", e);
                    throw new IllegalStateException("回滚已存在的调减明细失败: " + e.getMessage(), e);
                }
            }
            
            // 处理调减类型的明细：需要校验预算并扣减 BudgetBalance 和 BudgetQuota
            // effectType="0"或"2"：根据季度金额判断（负数视为调减）
            // effectType="1"：根据 adjustAmountTotalInvestment 判断（负数视为调减）
            List<BudgetLedger> decreaseLedgers = new ArrayList<>();
            for (BudgetLedger ledger : ledgersToInsert) {
                if (isDecreaseLedger(ledger)) {
                    decreaseLedgers.add(ledger);
                }
            }
            for (BudgetLedger ledger : ledgersToUpdate) {
                if (isDecreaseLedger(ledger)) {
                    decreaseLedgers.add(ledger);
                }
            }
            
            if (!decreaseLedgers.isEmpty()) {
                // 与审批调增一致：若所有调减明细的科目都不在白名单且不带项目且不带资产类型编码，则跳过对资金池的调减处理
                boolean allNotInWhitelist = true;
                boolean allNoAssetType = true;
                for (BudgetLedger ledger : decreaseLedgers) {
                    String code = ledger.getBudgetSubjectCode();
                    String masterProjectCode = ledger.getMasterProjectCode();
                    String erpAssetType = ledger.getErpAssetType();
                    boolean isSubjectCodeInWhitelist = StringUtils.isNotBlank(code) && !"NAN-NAN".equals(code) && budgetSubjectCodeConfig.isInWhitelist(code);
                    boolean hasProjectCode = StringUtils.isNotBlank(masterProjectCode) && !"NAN".equals(masterProjectCode);
                    if (StringUtils.isNotBlank(erpAssetType) && !"NAN".equals(erpAssetType)) {
                        allNoAssetType = false;
                    }
                    if (isSubjectCodeInWhitelist || hasProjectCode) {
                        allNotInWhitelist = false;
                        break;
                    }
                }
                if (!(allNotInWhitelist && allNoAssetType)) {
                    boolean skipBudgetValidation = budgetValidationConfig != null && budgetValidationConfig.isSkipBudgetValidation();
                    if (skipBudgetValidation) {
                        log.info("========== 预算校验已关闭(budget.validation.mode=0)，调减将直接扣减不校验余额 ==========");
                    }
                    log.info("========== 开始处理调减明细，共 {} 条 ==========", decreaseLedgers.size());
                    try {
                        processDecreaseAdjustments(decreaseLedgers, adjustAmountTotalInvestmentMap, skipBudgetValidation, ledgerBizKeyToBalanceForPoolDimensionKey);
                    } catch (Exception e) {
                        // 尝试提取明细标识
                        if (!tryExtractDetailError(e, detailValidationResultMap, detailValidationMessageMap)) {
                            throw e; // 无法识别具体明细，作为整单错误抛出
                        }
                        hasDetailError = true;
                    }
                } else {
                    log.info("========== 所有调减明细的科目编码都不在白名单中且不带项目编码且不带资产类型编码，跳过对资金池的调减处理 ==========");
                }
            }
            
            // 再次检查是否有明细级别的错误
            if (hasDetailError) {
                throw new IllegalStateException("部分明细处理失败，详见明细错误信息");
            }
            
            // 根据实际扣减的 BUDGET_BALANCE 填充流水的 POOL_DIMENSION_KEY（与申请/合同/付款一致，便于重跑后根据本字段+季度查到同一资金池）
            fillPoolDimensionKeyFromBalanceForLedgers(ledgersToInsert, ledgersToUpdate, ledgerBizKeyToBalanceForPoolDimensionKey);
            
            // 执行数据库操作
            performDatabaseOperations(ledgerHistoriesToInsert, ledgersToUpdate, ledgersToInsert, 
                    adjustOrderNo, documentName, dataSource, processName, existingHead, operator, operatorNo, requestTime);
            
            // 提取组织和科目编码，查询名称映射（包含所有明细，包括白名单科目，用于返回响应时显示名称）
            Set<String> managementOrgSet = adjustInfo != null && adjustInfo.getAdjustDetails() != null 
                ? adjustInfo.getAdjustDetails().stream()
                    .map(AdjustDetailDetailVo::getManagementOrg)
                    .filter(StringUtils::isNotBlank)
                    .collect(Collectors.toSet())
                : Collections.emptySet();
            Set<String> budgetSubjectCodeSet = adjustInfo != null && adjustInfo.getAdjustDetails() != null 
                ? adjustInfo.getAdjustDetails().stream()
                    .map(AdjustDetailDetailVo::getBudgetSubjectCode)
                    .filter(StringUtils::isNotBlank)
                    .filter(code -> !"NAN-NAN".equals(code))
                    .collect(Collectors.toSet())
                : Collections.emptySet();
            
            Map<String, String> ehrCdToEhrNmMap = CollectionUtils.isEmpty(managementOrgSet) 
                ? Collections.emptyMap() 
                : budgetQueryHelperService.queryEhrCdToOrgCdOneToOneMap(managementOrgSet).getEhrCdToEhrNmMap();
            Map<String, String> erpAcctCdToErpAcctNmMap = CollectionUtils.isEmpty(budgetSubjectCodeSet) 
                ? Collections.emptyMap() 
                : budgetQueryHelperService.queryErpAcctCdToAcctCdOneToOneMap(budgetSubjectCodeSet).getErpAcctCdToErpAcctNmMap();
            
            // 构建返回结果（带明细校验结果）
            return buildResponse(budgetAdjustApplyParams, detailValidationResultMap, detailValidationMessageMap, ehrCdToEhrNmMap, erpAcctCdToErpAcctNmMap);
            
        } catch (IllegalStateException e) {
            // 明细级别的错误，返回带有明细错误信息的响应
            if ("部分明细处理失败，详见明细错误信息".equals(e.getMessage())) {
                log.error("部分明细处理失败", e);
                // 提取组织和科目编码，查询名称映射
                Set<String> managementOrgSet = adjustInfo != null && adjustInfo.getAdjustDetails() != null 
                    ? adjustInfo.getAdjustDetails().stream()
                        .map(AdjustDetailDetailVo::getManagementOrg)
                        .filter(StringUtils::isNotBlank)
                        .collect(Collectors.toSet())
                    : Collections.emptySet();
                Set<String> budgetSubjectCodeSet = adjustInfo != null && adjustInfo.getAdjustDetails() != null 
                    ? adjustInfo.getAdjustDetails().stream()
                        .map(AdjustDetailDetailVo::getBudgetSubjectCode)
                        .filter(StringUtils::isNotBlank)
                        .filter(code -> !"NAN-NAN".equals(code))
                        .collect(Collectors.toSet())
                    : Collections.emptySet();
                Map<String, String> ehrCdToEhrNmMap = CollectionUtils.isEmpty(managementOrgSet) 
                    ? Collections.emptyMap() 
                    : budgetQueryHelperService.queryEhrCdToOrgCdOneToOneMap(managementOrgSet).getEhrCdToEhrNmMap();
                Map<String, String> erpAcctCdToErpAcctNmMap = CollectionUtils.isEmpty(budgetSubjectCodeSet) 
                    ? Collections.emptyMap() 
                    : budgetQueryHelperService.queryErpAcctCdToAcctCdOneToOneMap(budgetSubjectCodeSet).getErpAcctCdToErpAcctNmMap();
                return buildResponseWithDetailErrors(budgetAdjustApplyParams, detailValidationResultMap, detailValidationMessageMap, ehrCdToEhrNmMap, erpAcctCdToErpAcctNmMap);
            }
            // 其他 IllegalStateException，作为整单错误处理
            log.error("预算调整申请处理失败", e);
            return buildErrorResponseForAllDetails(budgetAdjustApplyParams, e);
        } catch (Exception e) {
            // 其他异常，作为整单错误处理
            log.error("预算调整申请处理失败", e);
            return buildErrorResponseForAllDetails(budgetAdjustApplyParams, e);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BudgetAdjustRenewRespVo authOrCancel(BudgetAdjustRenewParams budgetAdjustRenewParams) {
        log.info("开始处理预算调整审批/撤回，调整单号: {}", 
            budgetAdjustRenewParams.getAdjustRenewReqInfo().getAdjustOrderNo());
        
        try {
            AdjustRenewReqInfoParams adjustRenewReqInfo = budgetAdjustRenewParams.getAdjustRenewReqInfo();
            String adjustOrderNo = adjustRenewReqInfo.getAdjustOrderNo();
            String documentStatus = adjustRenewReqInfo.getDocumentStatus();
            
            LocalDateTime requestTime = BudgetQueryHelperService.parseEsbRequestTime(
                    budgetAdjustRenewParams.getEsbInfo() != null ? budgetAdjustRenewParams.getEsbInfo().getRequestTime() : null);
            if ("APPROVED".equals(documentStatus)) {
                // 场景一：APPROVED
                return handleApproved(adjustOrderNo, budgetAdjustRenewParams, requestTime);
                
            } else if ("REJECTED".equals(documentStatus) || "CANCELLED".equals(documentStatus)) {
                // 场景二：REJECTED 或 CANCELLED
                return handleRejectedOrCancelled(adjustOrderNo, documentStatus, budgetAdjustRenewParams, requestTime);
                
            } else {
                // 场景三：其他状态报错
                throw new IllegalArgumentException("不支持的单据状态：" + documentStatus);
            }
            
        } catch (Exception e) {
            log.error("预算调整审批/撤回处理失败", e);
            return buildErrorResponse(budgetAdjustRenewParams, e);
        }
    }

    /**
     * 处理审批通过场景
     */
    private BudgetAdjustRenewRespVo handleApproved(String adjustOrderNo, BudgetAdjustRenewParams budgetAdjustRenewParams, LocalDateTime requestTime) {
        log.info("========== 处理审批通过，adjustOrderNo={} ==========", adjustOrderNo);
        
        // 1. 查询并更新 BudgetLedgerHead 状态
        LambdaQueryWrapper<BudgetLedgerHead> headWrapper = new LambdaQueryWrapper<>();
        headWrapper.eq(BudgetLedgerHead::getBizCode, adjustOrderNo)
                .eq(BudgetLedgerHead::getBizType, ADJUST_BIZ_TYPE)
                .eq(BudgetLedgerHead::getDeleted, Boolean.FALSE);
        BudgetLedgerHead existingHead = budgetLedgerHeadMapper.selectOne(headWrapper);
        
        if (existingHead == null) {
            throw new IllegalStateException("未找到对应的预算流水头，调整单号：" + adjustOrderNo);
        }
        
        // 2. 查询该单据下的所有 BudgetLedger（在更新状态之前查询，用于回滚）
        LambdaQueryWrapper<BudgetLedger> ledgerWrapper = new LambdaQueryWrapper<>();
        ledgerWrapper.eq(BudgetLedger::getBizCode, adjustOrderNo)
                .eq(BudgetLedger::getBizType, ADJUST_BIZ_TYPE)
                .eq(BudgetLedger::getDeleted, Boolean.FALSE);
        List<BudgetLedger> allLedgers = budgetLedgerMapper.selectList(ledgerWrapper);
        
        // 3. 如果单据已经审批通过，需要先回滚之前的调增（避免重复调增）
        if (APPROVED.equals(existingHead.getStatus())) {
            log.info("========== 单据已审批通过，需要先回滚之前的调增 ==========");
            List<BudgetLedger> existingIncreaseLedgers = allLedgers.stream()
                    .filter(this::isIncreaseLedger)
                    .collect(Collectors.toList());
            
            if (!existingIncreaseLedgers.isEmpty()) {
                log.info("========== 找到 {} 条已存在的调增明细，开始回滚 ==========", existingIncreaseLedgers.size());
                try {
                    rollbackIncreaseAdjustments(existingIncreaseLedgers);
                } catch (Exception e) {
                    log.error("========== 回滚已存在的调增明细失败 ==========", e);
                    throw new IllegalStateException("回滚已存在的调增明细失败: " + e.getMessage(), e);
                }
            }
        }
        
        // 4. 更新 BudgetLedgerHead 状态
        existingHead.setStatus(APPROVED);
        existingHead.setVersion(String.valueOf(identifierGenerator.nextId(null)));
        if (requestTime != null) {
            existingHead.setUpdateTime(requestTime);
        }
        budgetLedgerHeadMapper.updateById(existingHead);
        log.info("========== 更新 BudgetLedgerHead 状态为 APPROVED ==========");
        
        // 5. 白名单校验：如果所有记录的科目编码都不在白名单中且不带项目编码且不带资产类型编码，直接返回成功，不进行后续处理
        if (!allLedgers.isEmpty()) {
            boolean allNotInWhitelist = true;
            boolean allNoAssetType = true;
            for (BudgetLedger ledger : allLedgers) {
                String code = ledger.getBudgetSubjectCode();
                String masterProjectCode = ledger.getMasterProjectCode();
                String erpAssetType = ledger.getErpAssetType();

                // 科目编码在白名单中
                boolean isSubjectCodeInWhitelist = StringUtils.isNotBlank(code) && !"NAN-NAN".equals(code) && budgetSubjectCodeConfig.isInWhitelist(code);

                // 带项目的明细（masterProjectCode 不为空且不是 "NAN"），即使科目编码不在白名单中，也需要校验
                boolean hasProjectCode = StringUtils.isNotBlank(masterProjectCode) && !"NAN".equals(masterProjectCode);

                // 带资产类型编码的明细（erpAssetType 不为空且不是 "NAN"），需要正常处理调增
                if (StringUtils.isNotBlank(erpAssetType) && !"NAN".equals(erpAssetType)) {
                    allNoAssetType = false;
                }

                if (isSubjectCodeInWhitelist || hasProjectCode) {
                    // 发现有白名单中的科目编码或带项目编码，需要正常处理
                    allNotInWhitelist = false;
                    break;
                }
            }

            if (allNotInWhitelist && allNoAssetType) {
                // 所有记录的科目编码都不在白名单中且不带项目编码且不带资产类型编码，直接返回成功，不进行后续处理
                log.info("所有记录的科目编码都不在白名单中且不带项目编码且不带资产类型编码，直接返回成功响应，跳过预算校验");
                return buildSuccessResponse(budgetAdjustRenewParams, "预算调整审批成功");
            }
        }
        
        // 6. 筛选出存在调增金额的 ledger
        // effectType="0"或"2"：根据季度金额判断（正数视为调增）
        // effectType="1"：根据 adjustAmountTotalInvestment 判断（正数视为调增）
        List<BudgetLedger> increaseLedgers = allLedgers.stream()
                .filter(this::isIncreaseLedger)
                .collect(Collectors.toList());
        
        if (increaseLedgers.isEmpty()) {
            log.info("========== 没有调增明细，只更新状态 ==========");
            return buildSuccessResponse(budgetAdjustRenewParams, "预算调整审批成功");
        }
        
        log.info("========== 找到 {} 条调增明细，开始处理 ==========", increaseLedgers.size());
        
        // 创建 Map 存储每个明细的 adjustAmountTotalInvestment（从 ledger 的 amount 字段中读取）
        Map<String, BigDecimal> adjustAmountTotalInvestmentMap = new HashMap<>();
        for (BudgetLedger ledger : allLedgers) {
            String key = ledger.getBizCode() + "@" + ledger.getBizItemCode();
            // adjustAmountTotalInvestment 直接存储在 BudgetLedger.amount 中
            BigDecimal totalInvestment = ledger.getAmount();
            if (totalInvestment != null && totalInvestment.compareTo(BigDecimal.ZERO) != 0) {
                adjustAmountTotalInvestmentMap.put(key, totalInvestment);
            }
        }
        
        // 7. 处理调增：增加 BudgetBalance 和 BudgetQuota
        processIncreaseAdjustments(increaseLedgers, adjustAmountTotalInvestmentMap);
        
        log.info("========== 审批通过处理完成 ==========");
        return buildSuccessResponse(budgetAdjustRenewParams, "预算调整审批成功");
    }

    /**
     * 处理拒绝或撤回场景
     */
    private BudgetAdjustRenewRespVo handleRejectedOrCancelled(String adjustOrderNo, String documentStatus, 
                                                               BudgetAdjustRenewParams budgetAdjustRenewParams, LocalDateTime requestTime) {
        log.info("========== 处理拒绝/撤回，adjustOrderNo={}, documentStatus={} ==========", adjustOrderNo, documentStatus);
        
        // 步骤一：查询 BUDGET_LEDGER_HEAD
        LambdaQueryWrapper<BudgetLedgerHead> headWrapper = new LambdaQueryWrapper<>();
        headWrapper.eq(BudgetLedgerHead::getBizCode, adjustOrderNo)
                .eq(BudgetLedgerHead::getBizType, ADJUST_BIZ_TYPE)
                .eq(BudgetLedgerHead::getDeleted, Boolean.FALSE);
        BudgetLedgerHead existingHead = budgetLedgerHeadMapper.selectOne(headWrapper);
        
        if (existingHead == null) {
            throw new IllegalStateException("未找到对应的预算流水头，调整单号：" + adjustOrderNo);
        }

        // 步骤二：查询 BUDGET_LEDGER
        LambdaQueryWrapper<BudgetLedger> ledgerWrapper = new LambdaQueryWrapper<>();
        ledgerWrapper.eq(BudgetLedger::getBizCode, adjustOrderNo)
                .eq(BudgetLedger::getBizType, ADJUST_BIZ_TYPE)
                .eq(BudgetLedger::getDeleted, Boolean.FALSE);
        List<BudgetLedger> allLedgers = budgetLedgerMapper.selectList(ledgerWrapper);

        // 不再进行白名单校验，所有明细都需要处理（不受控明细会跳过预算余额回滚，但仍删除数据）

        // 构建 Map<String, BudgetLedger>，key 为 bizCode + "@" + bizItemCode
        Map<String, BudgetLedger> needToCancelBudgetLedgerMap = allLedgers.stream()
                .collect(Collectors.toMap(
                        ledger -> ledger.getBizCode() + "@" + ledger.getBizItemCode(),
                        Function.identity(),
                        (a, b) -> a
                ));
        
        log.info("========== 查询到 {} 条 BudgetLedger 需要处理 ==========", allLedgers.size());

        // 步骤三：如果 needToCancelBudgetLedgerMap 为空，直接删除头
        if (needToCancelBudgetLedgerMap.isEmpty()) {
            BudgetLedgerHeadHistory headHistory = new BudgetLedgerHeadHistory();
            BeanUtils.copyProperties(existingHead, headHistory);
            headHistory.setId(identifierGenerator.nextId(headHistory).longValue());
            headHistory.setLedgerHeadId(existingHead.getId());
            headHistory.setDeleted(Boolean.FALSE);
            budgetLedgerHeadHistoryMapper.insertBatch(Collections.singletonList(headHistory));
            budgetLedgerHeadMapper.deleteById(existingHead.getId());
            
            log.info("========== 拒绝/撤回处理完成（无流水） ==========");
            String msg = "REJECTED".equals(documentStatus) ? "预算调整驳回成功" : "预算调整撤销成功";
            return buildSuccessResponse(budgetAdjustRenewParams, msg);
        }

        // 过滤出存在调减金额的 BudgetLedger（调减在申请时就生效了，需要回滚）
        // effectType="0"或"2"：根据季度金额判断（负数视为调减）
        // effectType="1"：根据 adjustAmountTotalInvestment 判断（负数视为调减）
        Map<String, BudgetLedger> decreaseLedgerMap = needToCancelBudgetLedgerMap.entrySet().stream()
                .filter(entry -> isDecreaseLedger(entry.getValue()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        
        // 过滤出存在调增金额的 BudgetLedger（如果审批通过后被撤回，需要回滚调增）
        // effectType="0"或"2"：根据季度金额判断（正数视为调增）
        // effectType="1"：根据 adjustAmountTotalInvestment 判断（正数视为调增）
        Map<String, BudgetLedger> increaseLedgerMap = needToCancelBudgetLedgerMap.entrySet().stream()
                .filter(entry -> isIncreaseLedger(entry.getValue()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        
        log.info("========== 过滤出 {} 条调减类型的 BudgetLedger 需要回滚 ==========", decreaseLedgerMap.size());
        log.info("========== 过滤出 {} 条调增类型的 BudgetLedger 需要回滚 ==========", increaseLedgerMap.size());

        // 初始化回滚相关的集合
        List<BudgetBalanceHistory> needToAddBudgetBalanceHistory = new ArrayList<>();
        List<BudgetQuotaHistory> needToAddBudgetQuotaHistory = new ArrayList<>();
        Map<String, BudgetBalance> needToRollBackBudgetBalanceMap = new HashMap<>();
        Map<String, BudgetQuota> needToRollBackBudgetQuotaMap = new HashMap<>();

        // 步骤四：如果有调减类型的流水，查询所有季度的 BudgetQuota 和 BudgetBalance
        if (!decreaseLedgerMap.isEmpty()) {
            // 批量提取 managementOrg 字段（对应 EHR_CD）
            Set<String> managementOrgSet = decreaseLedgerMap.values().stream()
                    .map(BudgetLedger::getMorgCode)
                    .filter(StringUtils::isNotBlank)
                    .collect(Collectors.toSet());
            
            // 批量查询 EHR_ORG_MANAGE_R 表，获取 EHR_CD 对应的 ORG_CD（一一对应关系）
            // Map<EHR_CD, ORG_CD>
            BudgetQueryHelperService.EhrCdToOrgCdMapResult ehrCdToOrgCdMapResult = budgetQueryHelperService.queryEhrCdToOrgCdOneToOneMap(managementOrgSet);
            Map<String, String> ehrCdToOrgCdMap = ehrCdToOrgCdMapResult.getEhrCdToOrgCdMap();
            Map<String, String> ehrCdToEhrNmMap = ehrCdToOrgCdMapResult.getEhrCdToEhrNmMap();
            
            // 批量查询 EHR_ORG_MANAGE_EXT_R 表，获取 EHR_CD 对应的所有 ORG_CD（一对多关系，用于识别不受控组织）
            // Map<EHR_CD, List<ORG_CD>>
            Map<String, List<String>> ehrCdToOrgCdExtMap = budgetQueryHelperService.queryEhrCdToOrgCdMap(managementOrgSet);
            if (ehrCdToOrgCdMap != null && !ehrCdToOrgCdMap.isEmpty()) {
                for (Map.Entry<String, String> e : ehrCdToOrgCdMap.entrySet()) {
                    if (!ehrCdToOrgCdExtMap.containsKey(e.getKey())) {
                        ehrCdToOrgCdExtMap.put(e.getKey(), Collections.singletonList(e.getValue()));
                    }
                }
            }
            
            // 批量提取 budgetSubjectCode 字段
            Set<String> budgetSubjectCodeSet = decreaseLedgerMap.values().stream()
                    .map(BudgetLedger::getBudgetSubjectCode)
                    .filter(StringUtils::isNotBlank)
                    .filter(code -> !"NAN-NAN".equals(code))
                    .collect(Collectors.toSet());
            
            // 批量查询 SUBJECT_INFO 表，获取 ERP_ACCT_CD 对应的 ACCT_CD（一一对应关系）
            // Map<ERP_ACCT_CD, ACCT_CD>
            BudgetQueryHelperService.ErpAcctCdToAcctCdMapResult erpAcctCdToAcctCdMapResult = budgetQueryHelperService.queryErpAcctCdToAcctCdOneToOneMap(budgetSubjectCodeSet);
            Map<String, String> erpAcctCdToAcctCdMap = erpAcctCdToAcctCdMapResult.getErpAcctCdToAcctCdMap();
            Map<String, String> erpAcctCdToErpAcctNmMap = erpAcctCdToAcctCdMapResult.getErpAcctCdToErpAcctNmMap();
            
            // 批量查询 SUBJECT_EXT_INFO 表，获取 ERP_ACCT_CD 对应的所有 ACCT_CD（一对多关系，用于识别不受控科目）
            // Map<ERP_ACCT_CD, List<ACCT_CD>>
            Map<String, List<String>> erpAcctCdToAcctCdExtMap = budgetQueryHelperService.queryErpAcctCdToAcctCdMap(budgetSubjectCodeSet);
            
            // 批量提取需要映射的 erpAssetType 字段（以 "1" 或 "M" 开头且不带项目的）
            Set<String> erpAssetTypeSet = decreaseLedgerMap.values().stream()
                    .filter(ledger -> {
                        String masterProjectCode = ledger.getMasterProjectCode();
                        // 只提取不带项目的明细的 erpAssetType
                        return "NAN".equals(masterProjectCode) || StringUtils.isBlank(masterProjectCode);
                    })
                    .map(BudgetLedger::getErpAssetType)
                    .filter(StringUtils::isNotBlank)
                    .filter(code -> !"NAN".equals(code))
                    .filter(code -> code.startsWith("1") || code.startsWith("M")) // 只提取需要映射的
                    .collect(Collectors.toSet());
            
            // 批量查询 VIEW_BUDGET_MEMBER_NAME_CODE 视图，获取 MEMBER_CD2 对应的 MEMBER_CD（一一对应关系）
            Map<String, String> erpAssetTypeToMemberCdMap = budgetQueryHelperService.queryErpAssetTypeToMemberCdMap(erpAssetTypeSet);
            log.info("========== 获取到 erpAssetType 映射表，共 {} 条记录 ==========", erpAssetTypeToMemberCdMap.size());
            
            // 过滤掉不受控的ledger，只查询受控的ledger的quota和balance（用于回滚）
            Map<String, BudgetLedger> controlledDecreaseLedgerMap = new HashMap<>();
            for (Map.Entry<String, BudgetLedger> entry : decreaseLedgerMap.entrySet()) {
                BudgetLedger ledger = entry.getValue();
                // 跳过不受控的ledger（不受控明细在回滚时也会跳过预算余额回滚）
                if (!isUncontrolledLedger(ledger, ehrCdToOrgCdExtMap, erpAcctCdToAcctCdExtMap)) {
                    controlledDecreaseLedgerMap.put(entry.getKey(), ledger);
                }
            }
            
            BudgetQueryHelperService.BudgetQuotaBalanceResult result = 
                    budgetQueryHelperService.queryQuotaAndBalanceByAllQuarters(controlledDecreaseLedgerMap, ehrCdToOrgCdMap, erpAcctCdToAcctCdMap, erpAssetTypeToMemberCdMap);
            
            needToRollBackBudgetQuotaMap = result.getQuotaMap();
            needToRollBackBudgetBalanceMap = result.getBalanceMap();

            // 步骤五：回滚 BudgetBalance 和 BudgetQuota（根据 effectType 分别处理）
            for (BudgetLedger ledger : decreaseLedgerMap.values()) {
                String bizKey = ledger.getBizCode() + "@" + ledger.getBizItemCode();
                String effectType = ledger.getEffectType(); // 调整类型："0"、"1"、"2"

                // 项目非 NAN 且 isInternal=1 时跳过回滚
                if (!"NAN".equals(ledger.getMasterProjectCode()) && "1".equals(ledger.getIsInternal())) {
                    log.info("========== 回滚调减 - 内部项目跳过回滚: bizKey={}, masterProjectCode={}, isInternal={} ==========",
                            bizKey, ledger.getMasterProjectCode(), ledger.getIsInternal());
                    continue;
                }
                
                // 判断是否不受控（需要跳过预算余额回滚，但仍删除数据）
                if (isUncontrolledLedger(ledger, ehrCdToOrgCdExtMap, erpAcctCdToAcctCdExtMap)) {
                    log.info("========== 回滚调减 - 不受控明细跳过预算余额回滚: bizKey={}, morgCode={}, budgetSubjectCode={}, masterProjectCode={} ==========",
                            bizKey, ledger.getMorgCode(), ledger.getBudgetSubjectCode(), ledger.getMasterProjectCode());
                    continue;
                }

                // effectType="1"：投资额调整，只处理 adjustAmountTotalInvestment（仅q1）
                if ("1".equals(effectType)) {
                    BigDecimal adjustAmountTotalInvestment = ledger.getAmount(); // adjustAmountTotalInvestment 存储在 amount 字段
                    if (adjustAmountTotalInvestment == null || adjustAmountTotalInvestment.compareTo(BigDecimal.ZERO) >= 0) {
                        continue; // 没有调减金额，跳过
                    }
                    
                    BigDecimal rollbackAmount = adjustAmountTotalInvestment.abs();
                    String quarter = "q1";
                    String bizKeyQuarter = bizKey + "@" + quarter;
                    
                    // 获取对应的 balance（仅q1）
                    BudgetBalance balance = needToRollBackBudgetBalanceMap.get(bizKeyQuarter);
                    if (balance == null) {
                        log.error("========== bizKeyQuarter={} 在 needToRollBackBudgetBalanceMap 中找不到对应的 balance ==========", bizKeyQuarter);
                        throw new IllegalStateException(
                            String.format("明细 [%s] 回滚时在季度 %s 未找到对应的预算余额。" +
                                         "请检查 BUDGET_POOL_DEM_R 表是否配置了该维度组合的预算。",
                                         bizKey, quarter)
                        );
                    }

                    Long poolId = balance.getPoolId();

                    // 保存 Balance 历史（每个 balance 只保存一次）
                    if (!needToAddBudgetBalanceHistory.stream().anyMatch(h -> h.getBalanceId().equals(balance.getId()))) {
                        BudgetBalanceHistory balanceHistory = new BudgetBalanceHistory();
                        BeanUtils.copyProperties(balance, balanceHistory);
                        balanceHistory.setId(identifierGenerator.nextId(balanceHistory).longValue());
                        balanceHistory.setBalanceId(balance.getId());
                        balanceHistory.setDeleted(Boolean.FALSE);
                        needToAddBudgetBalanceHistory.add(balanceHistory);
                    }

                    // 获取对应的 Quota（仅q1）
                    BudgetQuota quota = needToRollBackBudgetQuotaMap.get(bizKeyQuarter);
                    if (quota != null) {
                        // 保存 Quota 历史（每个 quota 只保存一次）
                        if (!needToAddBudgetQuotaHistory.stream().anyMatch(h -> h.getQuotaId().equals(quota.getId()))) {
                            BudgetQuotaHistory quotaHistory = new BudgetQuotaHistory();
                            BeanUtils.copyProperties(quota, quotaHistory);
                            quotaHistory.setId(identifierGenerator.nextId(quotaHistory).longValue());
                            quotaHistory.setQuotaId(quota.getId());
                            quotaHistory.setDeleted(Boolean.FALSE);
                            needToAddBudgetQuotaHistory.add(quotaHistory);
                        }
                    }

                    // effectType="1"：回滚 quota.amountAdj 和 balance.amountAvailable
                    BigDecimal currentAmountAdj = quota != null && quota.getAmountAdj() != null ? quota.getAmountAdj() : BigDecimal.ZERO;
                    BigDecimal currentAvailable = balance.getAmountAvailable() == null ? BigDecimal.ZERO : balance.getAmountAvailable();
                    
                    balance.setAmountAvailable(currentAvailable.add(rollbackAmount)); // 回滚，加回去
                    balance.setAmountAvailableVchanged(rollbackAmount);
                    balance.setVersion(ledger.getVersion());
                    
                    if (quota != null) {
                        quota.setAmountAdj(currentAmountAdj.add(rollbackAmount)); // 回滚，加回去
                        quota.setVersion(ledger.getVersion());
                        log.info("========== 回滚调减（effectType=1）quota.amountAdj: poolId={}, quarter={}, 回滚前={}, 回滚金额=+{}, 回滚后={} ==========",
                                poolId, quarter, currentAmountAdj, rollbackAmount, quota.getAmountAdj());
                    }
                    
                    log.info("========== 回滚调减（effectType=1）balance.amountAvailable: poolId={}, quarter={}, 回滚前={}, 回滚金额=+{}, 回滚后={} ==========",
                            poolId, quarter, currentAvailable, rollbackAmount, balance.getAmountAvailable());
                    
                    // 将需要更新的 balance 和 quota 加入 Map
                    needToRollBackBudgetBalanceMap.put(bizKeyQuarter, balance);
                    if (quota != null) {
                        needToRollBackBudgetQuotaMap.put(bizKeyQuarter, quota);
                    }
                    
                    continue; // effectType="1" 处理完成，继续下一个 ledger
                }

                // effectType="0" 或 "2"：处理 adjustAmountQ1-Q4
                BigDecimal[] quarterAmounts = new BigDecimal[] {
                        ledger.getAmountConsumedQOne() == null ? BigDecimal.ZERO : ledger.getAmountConsumedQOne(),
                        ledger.getAmountConsumedQTwo() == null ? BigDecimal.ZERO : ledger.getAmountConsumedQTwo(),
                        ledger.getAmountConsumedQThree() == null ? BigDecimal.ZERO : ledger.getAmountConsumedQThree(),
                        ledger.getAmountConsumedQFour() == null ? BigDecimal.ZERO : ledger.getAmountConsumedQFour()
                };
                String[] quarters = new String[] {"q1", "q2", "q3", "q4"};

                for (int i = 0; i < quarters.length; i++) {
                    BigDecimal adjustAmount = quarterAmounts[i];
                    // 只有负数才需要回滚（调减在申请时已生效）；0 和 正数代表未调减，直接跳过
                    if (adjustAmount.compareTo(BigDecimal.ZERO) >= 0) {
                        continue;
                    }
                    BigDecimal rollbackAmount = adjustAmount.abs();
                    String quarter = quarters[i];
                    String bizKeyQuarter = bizKey + "@" + quarter;

                    // 获取对应的 balance
                    BudgetBalance balance = needToRollBackBudgetBalanceMap.get(bizKeyQuarter);
                    if (balance == null) {
                        log.error("========== bizKeyQuarter={} 在 needToRollBackBudgetBalanceMap 中找不到对应的 balance ==========", bizKeyQuarter);
                        throw new IllegalStateException(
                            String.format("明细 [%s] 回滚时在季度 %s 未找到对应的预算余额。" +
                                         "请检查 BUDGET_POOL_DEM_R 表是否配置了该维度组合的预算。",
                                         bizKey, quarter)
                        );
                    }

                    Long poolId = balance.getPoolId();

                    // 保存 Balance 历史（每个 balance 只保存一次）
                    if (!needToAddBudgetBalanceHistory.stream().anyMatch(h -> h.getBalanceId().equals(balance.getId()))) {
                        BudgetBalanceHistory balanceHistory = new BudgetBalanceHistory();
                        BeanUtils.copyProperties(balance, balanceHistory);
                        balanceHistory.setId(identifierGenerator.nextId(balanceHistory).longValue());
                        balanceHistory.setBalanceId(balance.getId());
                        balanceHistory.setDeleted(Boolean.FALSE);
                        needToAddBudgetBalanceHistory.add(balanceHistory);
                    }

                    // 获取对应的 Quota
                    BudgetQuota quota = needToRollBackBudgetQuotaMap.get(bizKeyQuarter);
                    if (quota != null) {
                        // 保存 Quota 历史（每个 quota 只保存一次）
                        if (!needToAddBudgetQuotaHistory.stream().anyMatch(h -> h.getQuotaId().equals(quota.getId()))) {
                            BudgetQuotaHistory quotaHistory = new BudgetQuotaHistory();
                            BeanUtils.copyProperties(quota, quotaHistory);
                            quotaHistory.setId(identifierGenerator.nextId(quotaHistory).longValue());
                            quotaHistory.setQuotaId(quota.getId());
                            quotaHistory.setDeleted(Boolean.FALSE);
                            needToAddBudgetQuotaHistory.add(quotaHistory);
                        }
                    }

                    // 判断维度类型
                    DimensionType dimensionType = getDimensionType(bizKey);
                    
                    // effectType="0"：根据维度类型使用不同的字段
                    if ("0".equals(effectType)) {
                        BigDecimal currentAvailable = balance.getAmountAvailable() == null ? BigDecimal.ZERO : balance.getAmountAvailable();
                        balance.setAmountAvailable(currentAvailable.add(rollbackAmount)); // 回滚，加回去
                        balance.setAmountAvailableVchanged(rollbackAmount);
                        balance.setVersion(ledger.getVersion());
                        
                        if (quota != null) {
                            // 根据维度类型决定更新哪个字段
                            if (dimensionType == DimensionType.ORG_SUBJECT || dimensionType == DimensionType.PROJECT || dimensionType == DimensionType.ORG_ASSET_TYPE) {
                                // 组织+科目、项目、组织+资产类型：使用 amountAvailableAdj
                                BigDecimal currentAmountAvailableAdj = quota.getAmountAvailableAdj() == null ? BigDecimal.ZERO : quota.getAmountAvailableAdj();
                                quota.setAmountAvailableAdj(currentAmountAvailableAdj.add(rollbackAmount)); // 回滚，加回去
                                quota.setVersion(ledger.getVersion());
                                log.info("========== 回滚调减（effectType=0, {}）quota.amountAvailableAdj: poolId={}, quarter={}, 回滚前={}, 回滚金额=+{}, 回滚后={} ==========",
                                        dimensionType, poolId, quarter, currentAmountAvailableAdj, rollbackAmount, quota.getAmountAvailableAdj());
                            }
                        }
                        
                        log.info("========== 回滚调减（effectType=0, {}）balance.amountAvailable: poolId={}, quarter={}, 回滚前={}, 回滚金额=+{}, 回滚后={} ==========",
                                dimensionType, poolId, quarter, currentAvailable, rollbackAmount, balance.getAmountAvailable());
                    }
                    // effectType="2"：根据维度类型使用不同的字段
                    else if ("2".equals(effectType)) {
                        if (dimensionType == DimensionType.ORG_SUBJECT) {
                            // 组织+科目：effectType=2 也使用 amountAvailableAdj（和 effectType=0 一样）
                            BigDecimal currentAvailable = balance.getAmountAvailable() == null ? BigDecimal.ZERO : balance.getAmountAvailable();
                            balance.setAmountAvailable(currentAvailable.add(rollbackAmount)); // 回滚，加回去
                            balance.setAmountAvailableVchanged(rollbackAmount);
                            balance.setVersion(ledger.getVersion());
                            
                            if (quota != null) {
                                BigDecimal currentAmountAvailableAdj = quota.getAmountAvailableAdj() == null ? BigDecimal.ZERO : quota.getAmountAvailableAdj();
                                quota.setAmountAvailableAdj(currentAmountAvailableAdj.add(rollbackAmount)); // 回滚，加回去
                                quota.setVersion(ledger.getVersion());
                                log.info("========== 回滚调减（effectType=2, 组织+科目）quota.amountAvailableAdj: poolId={}, quarter={}, 回滚前={}, 回滚金额=+{}, 回滚后={} ==========",
                                        poolId, quarter, currentAmountAvailableAdj, rollbackAmount, quota.getAmountAvailableAdj());
                            }
                            
                            log.info("========== 回滚调减（effectType=2, 组织+科目）balance.amountAvailable: poolId={}, quarter={}, 回滚前={}, 回滚金额=+{}, 回滚后={} ==========",
                                    poolId, quarter, currentAvailable, rollbackAmount, balance.getAmountAvailable());
                        } else {
                            // 项目、组织+资产类型：回滚 balance.amountPayAvailable 和 quota.amountPayAdj
                            BigDecimal currentAmountPayAvailable = balance.getAmountPayAvailable() == null ? BigDecimal.ZERO : balance.getAmountPayAvailable();
                            balance.setAmountPayAvailable(currentAmountPayAvailable.add(rollbackAmount)); // 回滚，加回去
                            balance.setAmountPayAvailableVchanged(rollbackAmount);
                            balance.setVersion(ledger.getVersion());
                            
                            if (quota != null) {
                                BigDecimal currentAmountPayAdj = quota.getAmountPayAdj() == null ? BigDecimal.ZERO : quota.getAmountPayAdj();
                                quota.setAmountPayAdj(currentAmountPayAdj.add(rollbackAmount)); // 回滚，加回去
                                quota.setVersion(ledger.getVersion());
                                log.info("========== 回滚调减（effectType=2, {}）quota.amountPayAdj: poolId={}, quarter={}, 回滚前={}, 回滚金额=+{}, 回滚后={} ==========",
                                        dimensionType, poolId, quarter, currentAmountPayAdj, rollbackAmount, quota.getAmountPayAdj());
                            }
                            
                            log.info("========== 回滚调减（effectType=2, {}）balance.amountPayAvailable: poolId={}, quarter={}, 回滚前={}, 回滚金额=+{}, 回滚后={} ==========",
                                    dimensionType, poolId, quarter, currentAmountPayAvailable, rollbackAmount, balance.getAmountPayAvailable());
                        }
                    }

                    // 将需要更新的 balance 和 quota 加入 Map
                    needToRollBackBudgetBalanceMap.put(bizKeyQuarter, balance);
                    if (quota != null) {
                        needToRollBackBudgetQuotaMap.put(bizKeyQuarter, quota);
                    }
                }
            }
        }

        // 步骤五（续）：如果有调增类型的流水且已审批通过，需要回滚调增
        if (!increaseLedgerMap.isEmpty() && APPROVED.equals(existingHead.getStatus())) {
            // 批量提取 managementOrg 字段（对应 EHR_CD）
            Set<String> managementOrgSet = increaseLedgerMap.values().stream()
                    .map(BudgetLedger::getMorgCode)
                    .filter(StringUtils::isNotBlank)
                    .collect(Collectors.toSet());
            
            // 批量查询 EHR_ORG_MANAGE_R 表，获取 EHR_CD 对应的 ORG_CD（一一对应关系）
            // Map<EHR_CD, ORG_CD>
            BudgetQueryHelperService.EhrCdToOrgCdMapResult ehrCdToOrgCdMapResult = budgetQueryHelperService.queryEhrCdToOrgCdOneToOneMap(managementOrgSet);
            Map<String, String> ehrCdToOrgCdMap = ehrCdToOrgCdMapResult.getEhrCdToOrgCdMap();
            Map<String, String> ehrCdToEhrNmMap = ehrCdToOrgCdMapResult.getEhrCdToEhrNmMap();
            
            // 批量查询 EHR_ORG_MANAGE_EXT_R 表，获取 EHR_CD 对应的所有 ORG_CD（一对多关系，用于识别不受控组织）
            // Map<EHR_CD, List<ORG_CD>>
            Map<String, List<String>> increaseEhrCdToOrgCdExtMap = budgetQueryHelperService.queryEhrCdToOrgCdMap(managementOrgSet);
            
            // 批量提取 budgetSubjectCode 字段
            Set<String> budgetSubjectCodeSet = increaseLedgerMap.values().stream()
                    .map(BudgetLedger::getBudgetSubjectCode)
                    .filter(StringUtils::isNotBlank)
                    .filter(code -> !"NAN-NAN".equals(code))
                    .collect(Collectors.toSet());
            
            // 批量查询 SUBJECT_INFO 表，获取 ERP_ACCT_CD 对应的 ACCT_CD（一一对应关系）
            // Map<ERP_ACCT_CD, ACCT_CD>
            BudgetQueryHelperService.ErpAcctCdToAcctCdMapResult erpAcctCdToAcctCdMapResult = budgetQueryHelperService.queryErpAcctCdToAcctCdOneToOneMap(budgetSubjectCodeSet);
            Map<String, String> erpAcctCdToAcctCdMap = erpAcctCdToAcctCdMapResult.getErpAcctCdToAcctCdMap();
            Map<String, String> erpAcctCdToErpAcctNmMap = erpAcctCdToAcctCdMapResult.getErpAcctCdToErpAcctNmMap();
            
            // 批量查询 SUBJECT_EXT_INFO 表，获取 ERP_ACCT_CD 对应的所有 ACCT_CD（一对多关系，用于识别不受控科目）
            // Map<ERP_ACCT_CD, List<ACCT_CD>>
            Map<String, List<String>> increaseErpAcctCdToAcctCdExtMap = budgetQueryHelperService.queryErpAcctCdToAcctCdMap(budgetSubjectCodeSet);
            
            // 批量提取需要映射的 erpAssetType 字段（以 "1" 或 "M" 开头且不带项目的）
            Set<String> erpAssetTypeSet = increaseLedgerMap.values().stream()
                    .filter(ledger -> {
                        String masterProjectCode = ledger.getMasterProjectCode();
                        // 只提取不带项目的明细的 erpAssetType
                        return "NAN".equals(masterProjectCode) || StringUtils.isBlank(masterProjectCode);
                    })
                    .map(BudgetLedger::getErpAssetType)
                    .filter(StringUtils::isNotBlank)
                    .filter(code -> !"NAN".equals(code))
                    .filter(code -> code.startsWith("1") || code.startsWith("M")) // 只提取需要映射的
                    .collect(Collectors.toSet());
            
            // 批量查询 VIEW_BUDGET_MEMBER_NAME_CODE 视图，获取 MEMBER_CD2 对应的 MEMBER_CD（一一对应关系）
            Map<String, String> erpAssetTypeToMemberCdMap = budgetQueryHelperService.queryErpAssetTypeToMemberCdMap(erpAssetTypeSet);
            log.info("========== 获取到 erpAssetType 映射表，共 {} 条记录 ==========", erpAssetTypeToMemberCdMap.size());
            
            BudgetQueryHelperService.BudgetQuotaBalanceResult result = 
                    budgetQueryHelperService.queryQuotaAndBalanceByAllQuarters(increaseLedgerMap, ehrCdToOrgCdMap, erpAcctCdToAcctCdMap, erpAssetTypeToMemberCdMap);
            
            Map<String, BudgetQuota> increaseQuotaMap = result.getQuotaMap();
            Map<String, BudgetBalance> increaseBalanceMap = result.getBalanceMap();

            for (BudgetLedger ledger : increaseLedgerMap.values()) {
                String bizKey = ledger.getBizCode() + "@" + ledger.getBizItemCode();
                String effectType = ledger.getEffectType(); // 调整类型："0"、"1"、"2"

                // 项目非 NAN 且 isInternal=1 时跳过回滚
                if (!"NAN".equals(ledger.getMasterProjectCode()) && "1".equals(ledger.getIsInternal())) {
                    log.info("========== 回滚调增 - 内部项目跳过回滚: bizKey={}, masterProjectCode={}, isInternal={} ==========",
                            bizKey, ledger.getMasterProjectCode(), ledger.getIsInternal());
                    continue;
                }
                
                // 判断是否不受控（需要跳过预算余额回滚，但仍删除数据）
                if (isUncontrolledLedger(ledger, increaseEhrCdToOrgCdExtMap, increaseErpAcctCdToAcctCdExtMap)) {
                    log.info("========== 回滚调增 - 不受控明细跳过预算余额回滚: bizKey={}, morgCode={}, budgetSubjectCode={}, masterProjectCode={} ==========",
                            bizKey, ledger.getMorgCode(), ledger.getBudgetSubjectCode(), ledger.getMasterProjectCode());
                    continue;
                }

                // effectType="1"：投资额调整，只处理 adjustAmountTotalInvestment（仅q1）
                if ("1".equals(effectType)) {
                    BigDecimal adjustAmountTotalInvestment = ledger.getAmount(); // adjustAmountTotalInvestment 存储在 amount 字段
                    if (adjustAmountTotalInvestment == null || adjustAmountTotalInvestment.compareTo(BigDecimal.ZERO) <= 0) {
                        continue; // 没有调增金额，跳过
                    }
                    
                    String quarter = "q1";
                    String bizKeyQuarter = bizKey + "@" + quarter;
                    
                    // 获取对应的 balance（仅q1）
                    BudgetBalance balance = increaseBalanceMap.get(bizKeyQuarter);
                    if (balance == null) {
                        log.error("========== bizKeyQuarter={} 在 increaseBalanceMap 中找不到对应的 balance ==========", bizKeyQuarter);
                        throw new IllegalStateException(
                            String.format("明细 [%s] 回滚调增时在季度 %s 未找到对应的预算余额。" +
                                         "请检查 BUDGET_POOL_DEM_R 表是否配置了该维度组合的预算。",
                                         bizKey, quarter)
                        );
                    }

                    Long poolId = balance.getPoolId();

                    // 保存 Balance 历史（每个 balance 只保存一次）
                    if (!needToAddBudgetBalanceHistory.stream().anyMatch(h -> h.getBalanceId().equals(balance.getId()))) {
                        BudgetBalanceHistory balanceHistory = new BudgetBalanceHistory();
                        BeanUtils.copyProperties(balance, balanceHistory);
                        balanceHistory.setId(identifierGenerator.nextId(balanceHistory).longValue());
                        balanceHistory.setBalanceId(balance.getId());
                        balanceHistory.setDeleted(Boolean.FALSE);
                        needToAddBudgetBalanceHistory.add(balanceHistory);
                    }

                    // 获取对应的 Quota（仅q1）
                    BudgetQuota quota = increaseQuotaMap.get(bizKeyQuarter);
                    if (quota != null) {
                        // 保存 Quota 历史（每个 quota 只保存一次）
                        if (!needToAddBudgetQuotaHistory.stream().anyMatch(h -> h.getQuotaId().equals(quota.getId()))) {
                            BudgetQuotaHistory quotaHistory = new BudgetQuotaHistory();
                            BeanUtils.copyProperties(quota, quotaHistory);
                            quotaHistory.setId(identifierGenerator.nextId(quotaHistory).longValue());
                            quotaHistory.setQuotaId(quota.getId());
                            quotaHistory.setDeleted(Boolean.FALSE);
                            needToAddBudgetQuotaHistory.add(quotaHistory);
                        }
                    }

                    // effectType="1"：回滚 quota.amountAdj 和 balance.amountAvailable
                    BigDecimal currentAmountAdj = quota != null && quota.getAmountAdj() != null ? quota.getAmountAdj() : BigDecimal.ZERO;
                    BigDecimal currentAvailable = balance.getAmountAvailable() == null ? BigDecimal.ZERO : balance.getAmountAvailable();
                    
                    balance.setAmountAvailable(currentAvailable.subtract(adjustAmountTotalInvestment)); // 回滚，减去
                    balance.setAmountAvailableVchanged(adjustAmountTotalInvestment.negate());
                    balance.setVersion(ledger.getVersion());
                    
                    if (quota != null) {
                        quota.setAmountAdj(currentAmountAdj.subtract(adjustAmountTotalInvestment)); // 回滚，减去
                        quota.setVersion(ledger.getVersion());
                        log.info("========== 回滚调增（effectType=1）quota.amountAdj: poolId={}, quarter={}, 回滚前={}, 回滚金额=-{}, 回滚后={} ==========",
                                poolId, quarter, currentAmountAdj, adjustAmountTotalInvestment, quota.getAmountAdj());
                    }
                    
                    log.info("========== 回滚调增（effectType=1）balance.amountAvailable: poolId={}, quarter={}, 回滚前={}, 回滚金额=-{}, 回滚后={} ==========",
                            poolId, quarter, currentAvailable, adjustAmountTotalInvestment, balance.getAmountAvailable());
                    
                    // 将需要更新的 balance 和 quota 加入 Map
                    needToRollBackBudgetBalanceMap.put(bizKeyQuarter, balance);
                    if (quota != null) {
                        needToRollBackBudgetQuotaMap.put(bizKeyQuarter, quota);
                    }
                    
                    continue; // effectType="1" 处理完成，继续下一个 ledger
                }

                // effectType="0" 或 "2"：处理 adjustAmountQ1-Q4
                BigDecimal[] quarterAmounts = new BigDecimal[] {
                        ledger.getAmountConsumedQOne() == null ? BigDecimal.ZERO : ledger.getAmountConsumedQOne(),
                        ledger.getAmountConsumedQTwo() == null ? BigDecimal.ZERO : ledger.getAmountConsumedQTwo(),
                        ledger.getAmountConsumedQThree() == null ? BigDecimal.ZERO : ledger.getAmountConsumedQThree(),
                        ledger.getAmountConsumedQFour() == null ? BigDecimal.ZERO : ledger.getAmountConsumedQFour()
                };
                String[] quarters = new String[] {"q1", "q2", "q3", "q4"};

                for (int i = 0; i < quarters.length; i++) {
                    BigDecimal adjustAmount = quarterAmounts[i];
                    // 只有正数才需要回滚（调增在审批通过后已生效）；0 和 负数代表未调增，直接跳过
                    if (adjustAmount.compareTo(BigDecimal.ZERO) <= 0) {
                        continue;
                    }
                    String quarter = quarters[i];
                    String bizKeyQuarter = bizKey + "@" + quarter;

                    // 获取对应的 balance
                    BudgetBalance balance = increaseBalanceMap.get(bizKeyQuarter);
                    if (balance == null) {
                        log.error("========== bizKeyQuarter={} 在 increaseBalanceMap 中找不到对应的 balance ==========", bizKeyQuarter);
                        throw new IllegalStateException(
                            String.format("明细 [%s] 回滚调增时在季度 %s 未找到对应的预算余额。" +
                                         "请检查 BUDGET_POOL_DEM_R 表是否配置了该维度组合的预算。",
                                         bizKey, quarter)
                        );
                    }

                    Long poolId = balance.getPoolId();

                    // 保存 Balance 历史（每个 balance 只保存一次）
                    if (!needToAddBudgetBalanceHistory.stream().anyMatch(h -> h.getBalanceId().equals(balance.getId()))) {
                        BudgetBalanceHistory balanceHistory = new BudgetBalanceHistory();
                        BeanUtils.copyProperties(balance, balanceHistory);
                        balanceHistory.setId(identifierGenerator.nextId(balanceHistory).longValue());
                        balanceHistory.setBalanceId(balance.getId());
                        balanceHistory.setDeleted(Boolean.FALSE);
                        needToAddBudgetBalanceHistory.add(balanceHistory);
                    }

                    // 获取对应的 Quota
                    BudgetQuota quota = increaseQuotaMap.get(bizKeyQuarter);
                    if (quota != null) {
                        // 保存 Quota 历史（每个 quota 只保存一次）
                        if (!needToAddBudgetQuotaHistory.stream().anyMatch(h -> h.getQuotaId().equals(quota.getId()))) {
                            BudgetQuotaHistory quotaHistory = new BudgetQuotaHistory();
                            BeanUtils.copyProperties(quota, quotaHistory);
                            quotaHistory.setId(identifierGenerator.nextId(quotaHistory).longValue());
                            quotaHistory.setQuotaId(quota.getId());
                            quotaHistory.setDeleted(Boolean.FALSE);
                            needToAddBudgetQuotaHistory.add(quotaHistory);
                        }
                    }

                    // 判断维度类型
                    DimensionType dimensionType = getDimensionType(bizKey);
                    
                    // effectType="0"：根据维度类型使用不同的字段
                    if ("0".equals(effectType)) {
                        BigDecimal currentAvailable = balance.getAmountAvailable() == null ? BigDecimal.ZERO : balance.getAmountAvailable();
                        balance.setAmountAvailable(currentAvailable.subtract(adjustAmount)); // 回滚，减去
                        balance.setAmountAvailableVchanged(adjustAmount.negate());
                        balance.setVersion(ledger.getVersion());
                        
                        if (quota != null) {
                            // 根据维度类型决定更新哪个字段
                            if (dimensionType == DimensionType.ORG_SUBJECT || dimensionType == DimensionType.PROJECT || dimensionType == DimensionType.ORG_ASSET_TYPE) {
                                // 组织+科目、项目、组织+资产类型：使用 amountAvailableAdj
                                BigDecimal currentAmountAvailableAdj = quota.getAmountAvailableAdj() == null ? BigDecimal.ZERO : quota.getAmountAvailableAdj();
                                quota.setAmountAvailableAdj(currentAmountAvailableAdj.subtract(adjustAmount)); // 回滚，减去
                                quota.setVersion(ledger.getVersion());
                                log.info("========== 回滚调增（effectType=0, {}）quota.amountAvailableAdj: poolId={}, quarter={}, 回滚前={}, 回滚金额=-{}, 回滚后={} ==========",
                                        dimensionType, poolId, quarter, currentAmountAvailableAdj, adjustAmount, quota.getAmountAvailableAdj());
                            }
                        }
                        
                        log.info("========== 回滚调增（effectType=0, {}）balance.amountAvailable: poolId={}, quarter={}, 回滚前={}, 回滚金额=-{}, 回滚后={} ==========",
                                dimensionType, poolId, quarter, currentAvailable, adjustAmount, balance.getAmountAvailable());
                    }
                    // effectType="2"：根据维度类型使用不同的字段
                    else if ("2".equals(effectType)) {
                        if (dimensionType == DimensionType.ORG_SUBJECT) {
                            // 组织+科目：effectType=2 也使用 amountAvailableAdj（和 effectType=0 一样）
                            BigDecimal currentAvailable = balance.getAmountAvailable() == null ? BigDecimal.ZERO : balance.getAmountAvailable();
                            balance.setAmountAvailable(currentAvailable.subtract(adjustAmount)); // 回滚，减去
                            balance.setAmountAvailableVchanged(adjustAmount.negate());
                            balance.setVersion(ledger.getVersion());
                            
                            if (quota != null) {
                                BigDecimal currentAmountAvailableAdj = quota.getAmountAvailableAdj() == null ? BigDecimal.ZERO : quota.getAmountAvailableAdj();
                                quota.setAmountAvailableAdj(currentAmountAvailableAdj.subtract(adjustAmount)); // 回滚，减去
                                quota.setVersion(ledger.getVersion());
                                log.info("========== 回滚调增（effectType=2, 组织+科目）quota.amountAvailableAdj: poolId={}, quarter={}, 回滚前={}, 回滚金额=-{}, 回滚后={} ==========",
                                        poolId, quarter, currentAmountAvailableAdj, adjustAmount, quota.getAmountAvailableAdj());
                            }
                            
                            log.info("========== 回滚调增（effectType=2, 组织+科目）balance.amountAvailable: poolId={}, quarter={}, 回滚前={}, 回滚金额=-{}, 回滚后={} ==========",
                                    poolId, quarter, currentAvailable, adjustAmount, balance.getAmountAvailable());
                        } else {
                            // 项目、组织+资产类型：回滚 balance.amountPayAvailable 和 quota.amountPayAdj
                            BigDecimal currentAmountPayAvailable = balance.getAmountPayAvailable() == null ? BigDecimal.ZERO : balance.getAmountPayAvailable();
                            balance.setAmountPayAvailable(currentAmountPayAvailable.subtract(adjustAmount)); // 回滚，减去
                            balance.setAmountPayAvailableVchanged(adjustAmount.negate());
                            balance.setVersion(ledger.getVersion());
                            
                            if (quota != null) {
                                BigDecimal currentAmountPayAdj = quota.getAmountPayAdj() == null ? BigDecimal.ZERO : quota.getAmountPayAdj();
                                quota.setAmountPayAdj(currentAmountPayAdj.subtract(adjustAmount)); // 回滚，减去
                                quota.setVersion(ledger.getVersion());
                                log.info("========== 回滚调增（effectType=2, {}）quota.amountPayAdj: poolId={}, quarter={}, 回滚前={}, 回滚金额=-{}, 回滚后={} ==========",
                                        dimensionType, poolId, quarter, currentAmountPayAdj, adjustAmount, quota.getAmountPayAdj());
                            }
                            
                            log.info("========== 回滚调增（effectType=2, {}）balance.amountPayAvailable: poolId={}, quarter={}, 回滚前={}, 回滚金额=-{}, 回滚后={} ==========",
                                    dimensionType, poolId, quarter, currentAmountPayAvailable, adjustAmount, balance.getAmountPayAvailable());
                        }
                    }

                    // 将需要更新的 balance 和 quota 加入 Map
                    needToRollBackBudgetBalanceMap.put(bizKeyQuarter, balance);
                    if (quota != null) {
                        needToRollBackBudgetQuotaMap.put(bizKeyQuarter, quota);
                    }
                }
            }
        }

        // 步骤六：新建 needToAddBudgetLedgerHistory 和 needToCancelBudgetLedgerSet
        List<BudgetLedgerHistory> needToAddBudgetLedgerHistory = new ArrayList<>();
        Set<Long> needToCancelBudgetLedgerSet = new HashSet<>();
        
        for (BudgetLedger ledger : needToCancelBudgetLedgerMap.values()) {
            BudgetLedgerHistory history = new BudgetLedgerHistory();
            BeanUtils.copyProperties(ledger, history);
            history.setId(identifierGenerator.nextId(history).longValue());
            history.setLedgerId(ledger.getId());
            history.setDeleted(Boolean.FALSE);
            needToAddBudgetLedgerHistory.add(history);
            needToCancelBudgetLedgerSet.add(ledger.getId());
        }

        // 步骤七：更新数据库
        if (!needToRollBackBudgetBalanceMap.isEmpty()) {
            List<BudgetBalance> sortedBalances = sortBalancesById(new ArrayList<>(needToRollBackBudgetBalanceMap.values()));
            budgetBalanceMapper.updateBatchById(sortedBalances);
            log.info("========== 更新 BudgetBalance 完成，共 {} 条 ==========", sortedBalances.size());
        }
        if (!needToAddBudgetBalanceHistory.isEmpty()) {
            budgetBalanceHistoryMapper.insertBatch(needToAddBudgetBalanceHistory);
            log.info("========== 插入 BudgetBalanceHistory 完成，共 {} 条 ==========", needToAddBudgetBalanceHistory.size());
        }
        if (!needToRollBackBudgetQuotaMap.isEmpty()) {
            List<BudgetQuota> sortedQuotas = sortQuotasById(new ArrayList<>(needToRollBackBudgetQuotaMap.values()));
            budgetQuotaMapper.updateBatchById(sortedQuotas);
            log.info("========== 更新 BudgetQuota 完成，共 {} 条 ==========", sortedQuotas.size());
        }
        if (!needToAddBudgetQuotaHistory.isEmpty()) {
            budgetQuotaHistoryMapper.insertBatch(needToAddBudgetQuotaHistory);
            log.info("========== 插入 BudgetQuotaHistory 完成，共 {} 条 ==========", needToAddBudgetQuotaHistory.size());
        }
        if (!needToCancelBudgetLedgerSet.isEmpty()) {
            budgetLedgerMapper.deleteByIds(new ArrayList<>(needToCancelBudgetLedgerSet));
            log.info("========== 删除 BudgetLedger 完成，共 {} 条 ==========", needToCancelBudgetLedgerSet.size());
        }
        if (!needToAddBudgetLedgerHistory.isEmpty()) {
            budgetLedgerHistoryMapper.insertBatch(needToAddBudgetLedgerHistory);
            log.info("========== 插入 BudgetLedgerHistory 完成，共 {} 条 ==========", needToAddBudgetLedgerHistory.size());
        }

        // 步骤八：处理 BudgetLedgerHead
        BudgetLedgerHeadHistory headHistory = new BudgetLedgerHeadHistory();
        BeanUtils.copyProperties(existingHead, headHistory);
        headHistory.setId(identifierGenerator.nextId(headHistory).longValue());
        headHistory.setLedgerHeadId(existingHead.getId());
        headHistory.setDeleted(Boolean.FALSE);
        budgetLedgerHeadHistoryMapper.insertBatch(Collections.singletonList(headHistory));
        budgetLedgerHeadMapper.deleteById(existingHead.getId());
        log.info("========== 删除 BudgetLedgerHead 完成 ==========");

        log.info("========== 拒绝/撤回处理完成 ==========");
        String msg = "REJECTED".equals(documentStatus) ? "预算调整驳回成功" : "预算调整撤销成功";
        return buildSuccessResponse(budgetAdjustRenewParams, msg);
    }

    /**
     * 回滚调减明细（用于更新申请单时，先回滚之前的调减，避免重复扣减）
     * 
     * @param decreaseLedgers 需要回滚的调减明细列表
     */
    private void rollbackDecreaseAdjustments(List<BudgetLedger> decreaseLedgers) {
        if (decreaseLedgers.isEmpty()) {
            return;
        }
        
        log.info("========== 开始回滚调减明细，共 {} 条 ==========", decreaseLedgers.size());
        
        // 将 ledger 转换为 Map
        Map<String, BudgetLedger> decreaseLedgerMap = decreaseLedgers.stream()
                .collect(Collectors.toMap(
                        ledger -> ledger.getBizCode() + "@" + ledger.getBizItemCode(),
                        Function.identity(),
                        (a, b) -> a
                ));
        
        // 批量提取 managementOrg 字段（对应 EHR_CD）
        Set<String> managementOrgSet = decreaseLedgers.stream()
                .map(BudgetLedger::getMorgCode)
                .filter(StringUtils::isNotBlank)
                .collect(Collectors.toSet());
        
        // 批量查询 EHR_ORG_MANAGE_R 表，获取 EHR_CD 对应的 ORG_CD（一一对应关系）
        BudgetQueryHelperService.EhrCdToOrgCdMapResult ehrCdToOrgCdMapResult = budgetQueryHelperService.queryEhrCdToOrgCdOneToOneMap(managementOrgSet);
        Map<String, String> ehrCdToOrgCdMap = ehrCdToOrgCdMapResult.getEhrCdToOrgCdMap();
        
        // 批量查询 EHR_ORG_MANAGE_EXT_R 表，获取 EHR_CD 对应的所有 ORG_CD（一对多关系，用于识别不受控组织）
        Map<String, List<String>> ehrCdToOrgCdExtMap = budgetQueryHelperService.queryEhrCdToOrgCdMap(managementOrgSet);
        if (ehrCdToOrgCdMap != null && !ehrCdToOrgCdMap.isEmpty()) {
            for (Map.Entry<String, String> e : ehrCdToOrgCdMap.entrySet()) {
                if (!ehrCdToOrgCdExtMap.containsKey(e.getKey())) {
                    ehrCdToOrgCdExtMap.put(e.getKey(), Collections.singletonList(e.getValue()));
                }
            }
        }
        
        // 批量提取 budgetSubjectCode 字段
        Set<String> budgetSubjectCodeSet = decreaseLedgers.stream()
                .map(BudgetLedger::getBudgetSubjectCode)
                .filter(StringUtils::isNotBlank)
                .filter(code -> !"NAN-NAN".equals(code))
                .collect(Collectors.toSet());
        
        // 批量查询 SUBJECT_INFO 表，获取 ERP_ACCT_CD 对应的 ACCT_CD（一一对应关系）
        BudgetQueryHelperService.ErpAcctCdToAcctCdMapResult erpAcctCdToAcctCdMapResult = budgetQueryHelperService.queryErpAcctCdToAcctCdOneToOneMap(budgetSubjectCodeSet);
        Map<String, String> erpAcctCdToAcctCdMap = erpAcctCdToAcctCdMapResult.getErpAcctCdToAcctCdMap();
        
        // 批量查询 SUBJECT_INFO_EXT_R 表，获取 ERP_ACCT_CD 对应的所有 ACCT_CD（一对多关系，用于识别不受控科目）
        Map<String, List<String>> erpAcctCdToAcctCdExtMap = budgetQueryHelperService.queryErpAcctCdToAcctCdMap(budgetSubjectCodeSet);
        
        // 批量提取 erpAssetType 字段
        Set<String> erpAssetTypeSet = decreaseLedgers.stream()
                .map(BudgetLedger::getErpAssetType)
                .filter(StringUtils::isNotBlank)
                .filter(type -> !"NAN".equals(type))
                .collect(Collectors.toSet());
        
        // 批量查询 VIEW_BUDGET_MEMBER_NAME_CODE 视图，获取 MEMBER_CD2 对应的 MEMBER_CD（一一对应关系）
        Map<String, String> erpAssetTypeToMemberCdMap = budgetQueryHelperService.queryErpAssetTypeToMemberCdMap(erpAssetTypeSet);
        
        // 过滤掉不受控的ledger，只查询受控的ledger的quota和balance（用于回滚）
        Map<String, BudgetLedger> controlledDecreaseLedgerMap = new HashMap<>();
        for (Map.Entry<String, BudgetLedger> entry : decreaseLedgerMap.entrySet()) {
            BudgetLedger ledger = entry.getValue();
            // 跳过不受控的ledger（不受控明细在回滚时也会跳过预算余额回滚）
            if (!isUncontrolledLedger(ledger, ehrCdToOrgCdExtMap, erpAcctCdToAcctCdExtMap)) {
                controlledDecreaseLedgerMap.put(entry.getKey(), ledger);
            }
        }
        
        // 查询所有季度的 BudgetQuota 和 BudgetBalance
        BudgetQueryHelperService.BudgetQuotaBalanceResult result = 
                budgetQueryHelperService.queryQuotaAndBalanceByAllQuarters(controlledDecreaseLedgerMap, ehrCdToOrgCdMap, erpAcctCdToAcctCdMap, erpAssetTypeToMemberCdMap);
        
        Map<String, BudgetQuota> needToRollBackBudgetQuotaMap = result.getQuotaMap();
        Map<String, BudgetBalance> needToRollBackBudgetBalanceMap = result.getBalanceMap();
        
        // 回滚 BudgetBalance 和 BudgetQuota（根据 effectType 分别处理）
        for (BudgetLedger ledger : decreaseLedgerMap.values()) {
            String bizKey = ledger.getBizCode() + "@" + ledger.getBizItemCode();
            String effectType = ledger.getEffectType();
            
            // 项目非 NAN 且 isInternal=1 时跳过回滚
            if (!"NAN".equals(ledger.getMasterProjectCode()) && "1".equals(ledger.getIsInternal())) {
                log.info("========== 回滚调减 - 内部项目跳过回滚: bizKey={}, masterProjectCode={}, isInternal={} ==========",
                        bizKey, ledger.getMasterProjectCode(), ledger.getIsInternal());
                continue;
            }
            
            // 判断是否不受控（需要跳过预算余额回滚）
            if (isUncontrolledLedger(ledger, ehrCdToOrgCdExtMap, erpAcctCdToAcctCdExtMap)) {
                log.info("========== 回滚调减 - 不受控明细跳过预算余额回滚: bizKey={}, morgCode={}, budgetSubjectCode={}, masterProjectCode={} ==========",
                        bizKey, ledger.getMorgCode(), ledger.getBudgetSubjectCode(), ledger.getMasterProjectCode());
                continue;
            }
            
            // effectType="1"：投资额调整，只处理 adjustAmountTotalInvestment（仅q1）
            if ("1".equals(effectType)) {
                BigDecimal adjustAmountTotalInvestment = ledger.getAmount();
                if (adjustAmountTotalInvestment == null || adjustAmountTotalInvestment.compareTo(BigDecimal.ZERO) >= 0) {
                    continue; // 没有调减金额，跳过
                }
                
                BigDecimal rollbackAmount = adjustAmountTotalInvestment.abs();
                String quarter = "q1";
                String bizKeyQuarter = bizKey + "@" + quarter;
                
                BudgetBalance balance = needToRollBackBudgetBalanceMap.get(bizKeyQuarter);
                if (balance == null) {
                    log.warn("========== bizKeyQuarter={} 在 needToRollBackBudgetBalanceMap 中找不到对应的 balance，跳过回滚 ==========", bizKeyQuarter);
                    continue;
                }
                
                BudgetQuota quota = needToRollBackBudgetQuotaMap.get(bizKeyQuarter);
                
                // 回滚
                BigDecimal currentAvailable = balance.getAmountAvailable() == null ? BigDecimal.ZERO : balance.getAmountAvailable();
                balance.setAmountAvailable(currentAvailable.add(rollbackAmount)); // 回滚，加回去
                
                if (quota != null) {
                    BigDecimal currentAmountAdj = quota.getAmountAdj() == null ? BigDecimal.ZERO : quota.getAmountAdj();
                    quota.setAmountAdj(currentAmountAdj.add(rollbackAmount)); // 回滚，加回去
                }
                
                log.info("========== 回滚调减（effectType=1）: bizKey={}, quarter={}, 回滚金额={}, balance回滚前={}, balance回滚后={} ==========",
                        bizKey, quarter, rollbackAmount, currentAvailable, balance.getAmountAvailable());
                
                continue;
            }
            
            // effectType="0" 或 "2"：处理 adjustAmountQ1-Q4
            BigDecimal[] quarterAmounts = new BigDecimal[] {
                    ledger.getAmountConsumedQOne() == null ? BigDecimal.ZERO : ledger.getAmountConsumedQOne(),
                    ledger.getAmountConsumedQTwo() == null ? BigDecimal.ZERO : ledger.getAmountConsumedQTwo(),
                    ledger.getAmountConsumedQThree() == null ? BigDecimal.ZERO : ledger.getAmountConsumedQThree(),
                    ledger.getAmountConsumedQFour() == null ? BigDecimal.ZERO : ledger.getAmountConsumedQFour()
            };
            String[] quarters = new String[] {"q1", "q2", "q3", "q4"};
            
            for (int i = 0; i < quarters.length; i++) {
                BigDecimal adjustAmount = quarterAmounts[i];
                // 只有负数才需要回滚（调减在申请时已生效）
                if (adjustAmount.compareTo(BigDecimal.ZERO) >= 0) {
                    continue;
                }
                BigDecimal rollbackAmount = adjustAmount.abs();
                String quarter = quarters[i];
                String bizKeyQuarter = bizKey + "@" + quarter;
                
                BudgetBalance balance = needToRollBackBudgetBalanceMap.get(bizKeyQuarter);
                if (balance == null) {
                    log.warn("========== bizKeyQuarter={} 在 needToRollBackBudgetBalanceMap 中找不到对应的 balance，跳过回滚 ==========", bizKeyQuarter);
                    continue;
                }
                
                BudgetQuota quota = needToRollBackBudgetQuotaMap.get(bizKeyQuarter);
                
                // 判断维度类型
                DimensionType dimensionType = getDimensionType(bizKey);
                
                // 回滚
                if ("0".equals(effectType)) {
                    BigDecimal currentAvailable = balance.getAmountAvailable() == null ? BigDecimal.ZERO : balance.getAmountAvailable();
                    balance.setAmountAvailable(currentAvailable.add(rollbackAmount)); // 回滚，加回去
                    
                    if (quota != null) {
                        // 根据维度类型决定更新哪个字段
                        if (dimensionType == DimensionType.ORG_SUBJECT || dimensionType == DimensionType.PROJECT || dimensionType == DimensionType.ORG_ASSET_TYPE) {
                            // 组织+科目、项目、组织+资产类型：使用 amountAvailableAdj
                            BigDecimal currentAmountAvailableAdj = quota.getAmountAvailableAdj() == null ? BigDecimal.ZERO : quota.getAmountAvailableAdj();
                            quota.setAmountAvailableAdj(currentAmountAvailableAdj.add(rollbackAmount)); // 回滚，加回去
                        }
                    }
                    
                    log.info("========== 回滚调减（effectType=0, {}）: bizKey={}, quarter={}, 回滚金额={}, balance回滚前={}, balance回滚后={} ==========",
                            dimensionType, bizKey, quarter, rollbackAmount, currentAvailable, balance.getAmountAvailable());
                } else if ("2".equals(effectType)) {
                    if (dimensionType == DimensionType.ORG_SUBJECT) {
                        // 组织+科目：effectType=2 也使用 amountAvailableAdj（和 effectType=0 一样）
                        BigDecimal currentAvailable = balance.getAmountAvailable() == null ? BigDecimal.ZERO : balance.getAmountAvailable();
                        balance.setAmountAvailable(currentAvailable.add(rollbackAmount)); // 回滚，加回去
                        
                        if (quota != null) {
                            BigDecimal currentAmountAvailableAdj = quota.getAmountAvailableAdj() == null ? BigDecimal.ZERO : quota.getAmountAvailableAdj();
                            quota.setAmountAvailableAdj(currentAmountAvailableAdj.add(rollbackAmount)); // 回滚，加回去
                        }
                        
                        log.info("========== 回滚调减（effectType=2, 组织+科目）: bizKey={}, quarter={}, 回滚金额={}, balance回滚前={}, balance回滚后={} ==========",
                                bizKey, quarter, rollbackAmount, currentAvailable, balance.getAmountAvailable());
                    } else {
                        // 项目、组织+资产类型：回滚 balance.amountPayAvailable 和 quota.amountPayAdj
                        BigDecimal currentAmountPayAvailable = balance.getAmountPayAvailable() == null ? BigDecimal.ZERO : balance.getAmountPayAvailable();
                        balance.setAmountPayAvailable(currentAmountPayAvailable.add(rollbackAmount)); // 回滚，加回去
                        
                        if (quota != null) {
                            BigDecimal currentAmountPayAdj = quota.getAmountPayAdj() == null ? BigDecimal.ZERO : quota.getAmountPayAdj();
                            quota.setAmountPayAdj(currentAmountPayAdj.add(rollbackAmount)); // 回滚，加回去
                        }
                        
                        log.info("========== 回滚调减（effectType=2, {}）: bizKey={}, quarter={}, 回滚金额={}, amountPayAvailable回滚前={}, amountPayAvailable回滚后={} ==========",
                                dimensionType, bizKey, quarter, rollbackAmount, currentAmountPayAvailable, balance.getAmountPayAvailable());
                    }
                }
            }
        }
        
        // 更新数据库
        if (!needToRollBackBudgetBalanceMap.isEmpty()) {
            List<BudgetBalance> sortedBalances = sortBalancesById(new ArrayList<>(needToRollBackBudgetBalanceMap.values()));
            budgetBalanceMapper.updateBatchById(sortedBalances);
            log.info("========== 回滚调减：更新 BudgetBalance 完成，共 {} 条 ==========", sortedBalances.size());
        }
        if (!needToRollBackBudgetQuotaMap.isEmpty()) {
            List<BudgetQuota> sortedQuotas = sortQuotasById(new ArrayList<>(needToRollBackBudgetQuotaMap.values()));
            budgetQuotaMapper.updateBatchById(sortedQuotas);
            log.info("========== 回滚调减：更新 BudgetQuota 完成，共 {} 条 ==========", sortedQuotas.size());
        }
        
        log.info("========== 回滚调减明细完成 ==========");
    }

    /**
     * 回滚调增明细（用于审批通过时，如果单据已经审批过，先回滚之前的调增，避免重复调增）
     * 
     * @param increaseLedgers 需要回滚的调增明细列表
     */
    private void rollbackIncreaseAdjustments(List<BudgetLedger> increaseLedgers) {
        if (increaseLedgers.isEmpty()) {
            return;
        }
        
        log.info("========== 开始回滚调增明细，共 {} 条 ==========", increaseLedgers.size());
        
        // 将 ledger 转换为 Map
        Map<String, BudgetLedger> increaseLedgerMap = increaseLedgers.stream()
                .collect(Collectors.toMap(
                        ledger -> ledger.getBizCode() + "@" + ledger.getBizItemCode(),
                        Function.identity(),
                        (a, b) -> a
                ));
        
        // 批量提取 managementOrg 字段（对应 EHR_CD）
        Set<String> managementOrgSet = increaseLedgers.stream()
                .map(BudgetLedger::getMorgCode)
                .filter(StringUtils::isNotBlank)
                .collect(Collectors.toSet());
        
        // 批量查询 EHR_ORG_MANAGE_R 表，获取 EHR_CD 对应的 ORG_CD（一一对应关系）
        BudgetQueryHelperService.EhrCdToOrgCdMapResult ehrCdToOrgCdMapResult = budgetQueryHelperService.queryEhrCdToOrgCdOneToOneMap(managementOrgSet);
        Map<String, String> ehrCdToOrgCdMap = ehrCdToOrgCdMapResult.getEhrCdToOrgCdMap();
        
        // 批量查询 EHR_ORG_MANAGE_EXT_R 表，获取 EHR_CD 对应的所有 ORG_CD（一对多关系，用于识别不受控组织）
        Map<String, List<String>> increaseEhrCdToOrgCdExtMap = budgetQueryHelperService.queryEhrCdToOrgCdMap(managementOrgSet);
        
        // 批量提取 budgetSubjectCode 字段
        Set<String> budgetSubjectCodeSet = increaseLedgers.stream()
                .map(BudgetLedger::getBudgetSubjectCode)
                .filter(StringUtils::isNotBlank)
                .filter(code -> !"NAN-NAN".equals(code))
                .collect(Collectors.toSet());
        
        // 批量查询 SUBJECT_INFO 表，获取 ERP_ACCT_CD 对应的 ACCT_CD（一一对应关系）
        BudgetQueryHelperService.ErpAcctCdToAcctCdMapResult erpAcctCdToAcctCdMapResult = budgetQueryHelperService.queryErpAcctCdToAcctCdOneToOneMap(budgetSubjectCodeSet);
        Map<String, String> erpAcctCdToAcctCdMap = erpAcctCdToAcctCdMapResult.getErpAcctCdToAcctCdMap();
        
        // 批量查询 SUBJECT_EXT_INFO 表，获取 ERP_ACCT_CD 对应的所有 ACCT_CD（一对多关系，用于识别不受控科目）
        Map<String, List<String>> increaseErpAcctCdToAcctCdExtMap = budgetQueryHelperService.queryErpAcctCdToAcctCdMap(budgetSubjectCodeSet);
        
        // 批量提取需要映射的 erpAssetType 字段（以 "1" 或 "M" 开头且不带项目的）
        Set<String> erpAssetTypeSet = increaseLedgers.stream()
                .filter(ledger -> {
                    String masterProjectCode = ledger.getMasterProjectCode();
                    // 只提取不带项目的明细的 erpAssetType
                    return "NAN".equals(masterProjectCode) || StringUtils.isBlank(masterProjectCode);
                })
                .map(BudgetLedger::getErpAssetType)
                .filter(StringUtils::isNotBlank)
                .filter(code -> !"NAN".equals(code))
                .filter(code -> code.startsWith("1") || code.startsWith("M")) // 只提取需要映射的
                .collect(Collectors.toSet());
        
        // 批量查询 VIEW_BUDGET_MEMBER_NAME_CODE 视图，获取 MEMBER_CD2 对应的 MEMBER_CD（一一对应关系）
        Map<String, String> erpAssetTypeToMemberCdMap = budgetQueryHelperService.queryErpAssetTypeToMemberCdMap(erpAssetTypeSet);
        
        // 查询所有季度的 BudgetQuota 和 BudgetBalance（只查询受控的ledger）
        Map<String, BudgetLedger> controlledLedgerMap = new HashMap<>();
        for (Map.Entry<String, BudgetLedger> entry : increaseLedgerMap.entrySet()) {
            BudgetLedger ledger = entry.getValue();
            // 跳过不受控的ledger
            if (!isUncontrolledLedger(ledger, increaseEhrCdToOrgCdExtMap, increaseErpAcctCdToAcctCdExtMap)) {
                controlledLedgerMap.put(entry.getKey(), ledger);
            }
        }
        
        BudgetQueryHelperService.BudgetQuotaBalanceResult result = 
                budgetQueryHelperService.queryQuotaAndBalanceByAllQuarters(controlledLedgerMap, ehrCdToOrgCdMap, erpAcctCdToAcctCdMap, erpAssetTypeToMemberCdMap);
        
        Map<String, BudgetQuota> needToRollBackBudgetQuotaMap = result.getQuotaMap();
        Map<String, BudgetBalance> needToRollBackBudgetBalanceMap = result.getBalanceMap();
        
        // 回滚 BudgetBalance 和 BudgetQuota（根据 effectType 分别处理）
        for (BudgetLedger ledger : increaseLedgerMap.values()) {
            String bizKey = ledger.getBizCode() + "@" + ledger.getBizItemCode();
            String effectType = ledger.getEffectType();
            
            // 项目非 NAN 且 isInternal=1 时跳过回滚
            if (!"NAN".equals(ledger.getMasterProjectCode()) && "1".equals(ledger.getIsInternal())) {
                log.info("========== 回滚调增 - 内部项目跳过回滚: bizKey={}, masterProjectCode={}, isInternal={} ==========",
                        bizKey, ledger.getMasterProjectCode(), ledger.getIsInternal());
                continue;
            }
            
            // 判断是否不受控（需要跳过预算余额回滚）
            if (isUncontrolledLedger(ledger, increaseEhrCdToOrgCdExtMap, increaseErpAcctCdToAcctCdExtMap)) {
                log.info("========== 回滚调增 - 不受控明细跳过预算余额回滚: bizKey={}, morgCode={}, budgetSubjectCode={}, masterProjectCode={} ==========",
                        bizKey, ledger.getMorgCode(), ledger.getBudgetSubjectCode(), ledger.getMasterProjectCode());
                continue;
            }
            
            // effectType="1"：投资额调整，只处理 adjustAmountTotalInvestment（仅q1）
            if ("1".equals(effectType)) {
                BigDecimal adjustAmountTotalInvestment = ledger.getAmount();
                if (adjustAmountTotalInvestment == null || adjustAmountTotalInvestment.compareTo(BigDecimal.ZERO) <= 0) {
                    continue; // 没有调增金额，跳过
                }
                
                String quarter = "q1";
                String bizKeyQuarter = bizKey + "@" + quarter;
                
                BudgetBalance balance = needToRollBackBudgetBalanceMap.get(bizKeyQuarter);
                if (balance == null) {
                    log.warn("========== bizKeyQuarter={} 在 needToRollBackBudgetBalanceMap 中找不到对应的 balance，跳过回滚 ==========", bizKeyQuarter);
                    continue;
                }
                
                BudgetQuota quota = needToRollBackBudgetQuotaMap.get(bizKeyQuarter);
                
                // 回滚
                BigDecimal currentAvailable = balance.getAmountAvailable() == null ? BigDecimal.ZERO : balance.getAmountAvailable();
                balance.setAmountAvailable(currentAvailable.subtract(adjustAmountTotalInvestment)); // 回滚，减去
                
                if (quota != null) {
                    BigDecimal currentAmountAdj = quota.getAmountAdj() == null ? BigDecimal.ZERO : quota.getAmountAdj();
                    quota.setAmountAdj(currentAmountAdj.subtract(adjustAmountTotalInvestment)); // 回滚，减去
                }
                
                log.info("========== 回滚调增（effectType=1）: bizKey={}, quarter={}, 回滚金额={}, balance回滚前={}, balance回滚后={} ==========",
                        bizKey, quarter, adjustAmountTotalInvestment, currentAvailable, balance.getAmountAvailable());
                
                continue;
            }
            
            // effectType="0" 或 "2"：处理 adjustAmountQ1-Q4
            BigDecimal[] quarterAmounts = new BigDecimal[] {
                    ledger.getAmountConsumedQOne() == null ? BigDecimal.ZERO : ledger.getAmountConsumedQOne(),
                    ledger.getAmountConsumedQTwo() == null ? BigDecimal.ZERO : ledger.getAmountConsumedQTwo(),
                    ledger.getAmountConsumedQThree() == null ? BigDecimal.ZERO : ledger.getAmountConsumedQThree(),
                    ledger.getAmountConsumedQFour() == null ? BigDecimal.ZERO : ledger.getAmountConsumedQFour()
            };
            String[] quarters = new String[] {"q1", "q2", "q3", "q4"};
            
            for (int i = 0; i < quarters.length; i++) {
                BigDecimal adjustAmount = quarterAmounts[i];
                // 只有正数才需要回滚（调增在审批通过后已生效）
                if (adjustAmount.compareTo(BigDecimal.ZERO) <= 0) {
                    continue;
                }
                String quarter = quarters[i];
                String bizKeyQuarter = bizKey + "@" + quarter;
                
                BudgetBalance balance = needToRollBackBudgetBalanceMap.get(bizKeyQuarter);
                if (balance == null) {
                    log.warn("========== bizKeyQuarter={} 在 needToRollBackBudgetBalanceMap 中找不到对应的 balance，跳过回滚 ==========", bizKeyQuarter);
                    continue;
                }
                
                BudgetQuota quota = needToRollBackBudgetQuotaMap.get(bizKeyQuarter);
                
                // 判断维度类型
                DimensionType dimensionType = getDimensionType(bizKey);
                
                // 回滚
                if ("0".equals(effectType)) {
                    BigDecimal currentAvailable = balance.getAmountAvailable() == null ? BigDecimal.ZERO : balance.getAmountAvailable();
                    balance.setAmountAvailable(currentAvailable.subtract(adjustAmount)); // 回滚，减去
                    
                    if (quota != null) {
                        // 根据维度类型决定更新哪个字段
                        if (dimensionType == DimensionType.ORG_SUBJECT || dimensionType == DimensionType.PROJECT || dimensionType == DimensionType.ORG_ASSET_TYPE) {
                            // 组织+科目、项目、组织+资产类型：使用 amountAvailableAdj
                            BigDecimal currentAmountAvailableAdj = quota.getAmountAvailableAdj() == null ? BigDecimal.ZERO : quota.getAmountAvailableAdj();
                            quota.setAmountAvailableAdj(currentAmountAvailableAdj.subtract(adjustAmount)); // 回滚，减去
                        }
                    }
                    
                    log.info("========== 回滚调增（effectType=0, {}）: bizKey={}, quarter={}, 回滚金额={}, balance回滚前={}, balance回滚后={} ==========",
                            dimensionType, bizKey, quarter, adjustAmount, currentAvailable, balance.getAmountAvailable());
                } else if ("2".equals(effectType)) {
                    if (dimensionType == DimensionType.ORG_SUBJECT) {
                        // 组织+科目：effectType=2 也使用 amountAvailableAdj（和 effectType=0 一样）
                        BigDecimal currentAvailable = balance.getAmountAvailable() == null ? BigDecimal.ZERO : balance.getAmountAvailable();
                        balance.setAmountAvailable(currentAvailable.subtract(adjustAmount)); // 回滚，减去
                        
                        if (quota != null) {
                            BigDecimal currentAmountAvailableAdj = quota.getAmountAvailableAdj() == null ? BigDecimal.ZERO : quota.getAmountAvailableAdj();
                            quota.setAmountAvailableAdj(currentAmountAvailableAdj.subtract(adjustAmount)); // 回滚，减去
                        }
                        
                        log.info("========== 回滚调增（effectType=2, 组织+科目）: bizKey={}, quarter={}, 回滚金额={}, balance回滚前={}, balance回滚后={} ==========",
                                bizKey, quarter, adjustAmount, currentAvailable, balance.getAmountAvailable());
                    } else {
                        // 项目、组织+资产类型：回滚 balance.amountPayAvailable 和 quota.amountPayAdj
                        BigDecimal currentAmountPayAvailable = balance.getAmountPayAvailable() == null ? BigDecimal.ZERO : balance.getAmountPayAvailable();
                        balance.setAmountPayAvailable(currentAmountPayAvailable.subtract(adjustAmount)); // 回滚，减去
                        
                        if (quota != null) {
                            BigDecimal currentAmountPayAdj = quota.getAmountPayAdj() == null ? BigDecimal.ZERO : quota.getAmountPayAdj();
                            quota.setAmountPayAdj(currentAmountPayAdj.subtract(adjustAmount)); // 回滚，减去
                        }
                        
                        log.info("========== 回滚调增（effectType=2, {}）: bizKey={}, quarter={}, 回滚金额={}, amountPayAvailable回滚前={}, amountPayAvailable回滚后={} ==========",
                                dimensionType, bizKey, quarter, adjustAmount, currentAmountPayAvailable, balance.getAmountPayAvailable());
                    }
                }
            }
        }
        
        // 更新数据库
        if (!needToRollBackBudgetBalanceMap.isEmpty()) {
            List<BudgetBalance> sortedBalances = sortBalancesById(new ArrayList<>(needToRollBackBudgetBalanceMap.values()));
            budgetBalanceMapper.updateBatchById(sortedBalances);
            log.info("========== 回滚调增：更新 BudgetBalance 完成，共 {} 条 ==========", sortedBalances.size());
        }
        if (!needToRollBackBudgetQuotaMap.isEmpty()) {
            List<BudgetQuota> sortedQuotas = sortQuotasById(new ArrayList<>(needToRollBackBudgetQuotaMap.values()));
            budgetQuotaMapper.updateBatchById(sortedQuotas);
            log.info("========== 回滚调增：更新 BudgetQuota 完成，共 {} 条 ==========", sortedQuotas.size());
        }
        
        log.info("========== 回滚调增明细完成 ==========");
    }

    /**
     * 构建成功响应（带明细校验结果）
     */
    private BudgetAdjustRespVo buildResponse(BudgetAdjustApplyParams budgetAdjustApplyParams,
                                            Map<String, String> detailValidationResultMap,
                                            Map<String, String> detailValidationMessageMap,
                                            Map<String, String> ehrCdToEhrNmMap,
                                            Map<String, String> erpAcctCdToErpAcctNmMap) {
        ESBInfoParams esbInfo = budgetAdjustApplyParams.getEsbInfo();
        AdjustApplyReqInfoParams adjustInfo = budgetAdjustApplyParams.getAdjustApplyReqInfo();
        
        String instId = esbInfo.getInstId();
        if (instId == null || instId.isEmpty()) {
            String uuid = UUID.randomUUID().toString().replace("-", "");
            instId = "N" + uuid.substring(0, 8) + "." + uuid.substring(8, 16) + ".N"
                    + uuid.substring(16, 18) + "." + uuid.substring(18, 28) + ".N" + uuid.substring(28, 32);
        }
        String responseTime = LocalDateTime.now().format(DATE_TIME_FORMATTER);
        
        ESBRespInfoVo esbRespInfo = ESBRespInfoVo.builder()
                .instId(instId)
                .requestTime(esbInfo.getRequestTime())
                .attr1(esbInfo.getAttr1())
                .attr2(esbInfo.getAttr2())
                .attr3(esbInfo.getAttr3())
                .returnCode("A0001-ADJUST")
                .returnMsg("预算调整申请处理成功")
                .returnStatus("S")
                .responseTime(responseTime)
                .build();

        // 构建返回的调整明细列表（包含每个明细的校验结果）
        List<AdjustDetailRespVo> resultAdjustDetails = new ArrayList<>();
        List<AdjustDetailDetailVo> adjustDetails = adjustInfo.getAdjustDetails();
        if (adjustDetails != null) {
            for (AdjustDetailDetailVo detail : adjustDetails) {
                AdjustDetailRespVo resultDetail = new AdjustDetailRespVo();
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
                String detailLineNo = detail.getAdjustDetailLineNo();
                resultDetail.setValidationResult(detailValidationResultMap.getOrDefault(detailLineNo, "0"));
                resultDetail.setValidationMessage(detailValidationMessageMap.getOrDefault(detailLineNo, "处理成功"));
                
                resultAdjustDetails.add(resultDetail);
            }
        }

        AdjustApplyResultInfoRespVo adjustApplyResult = new AdjustApplyResultInfoRespVo();
        adjustApplyResult.setAdjustOrderNo(adjustInfo.getAdjustOrderNo());
        adjustApplyResult.setProcessTime(LocalDateTime.now().format(PROCESS_TIME_FORMATTER));
        adjustApplyResult.setIsInternal(adjustInfo.getIsInternal());
        adjustApplyResult.setAdjustDetails(resultAdjustDetails);

        BudgetAdjustRespVo respVo = new BudgetAdjustRespVo();
        respVo.setEsbInfo(esbRespInfo);
        respVo.setAdjustApplyResult(adjustApplyResult);
        return respVo;
    }

    /**
     * 构建整单错误响应（所有明细都报同样的错）
     */
    private BudgetAdjustRespVo buildErrorResponseForAllDetails(BudgetAdjustApplyParams params, Exception e) {
        ESBInfoParams esbInfo = params.getEsbInfo();
        AdjustApplyReqInfoParams adjustInfo = params.getAdjustApplyReqInfo();
        
        String errorMessage = e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName();
        
        String instId = esbInfo.getInstId();
        if (instId == null || instId.isEmpty()) {
            String uuid = UUID.randomUUID().toString().replace("-", "");
            instId = "N" + uuid.substring(0, 8) + "." + uuid.substring(8, 16) + ".N"
                    + uuid.substring(16, 18) + "." + uuid.substring(18, 28) + ".N" + uuid.substring(28, 32);
        }
        String responseTime = LocalDateTime.now().format(DATE_TIME_FORMATTER);
        
        // 提取组织和科目编码，查询名称映射
        Set<String> managementOrgSet = adjustInfo != null && adjustInfo.getAdjustDetails() != null 
            ? adjustInfo.getAdjustDetails().stream()
                .map(AdjustDetailDetailVo::getManagementOrg)
                .filter(StringUtils::isNotBlank)
                .collect(Collectors.toSet())
            : Collections.emptySet();
        Set<String> budgetSubjectCodeSet = adjustInfo != null && adjustInfo.getAdjustDetails() != null 
            ? adjustInfo.getAdjustDetails().stream()
                .map(AdjustDetailDetailVo::getBudgetSubjectCode)
                .filter(StringUtils::isNotBlank)
                .filter(code -> !"NAN-NAN".equals(code))
                .collect(Collectors.toSet())
            : Collections.emptySet();
        Map<String, String> ehrCdToEhrNmMap = CollectionUtils.isEmpty(managementOrgSet) 
            ? Collections.emptyMap() 
            : budgetQueryHelperService.queryEhrCdToOrgCdOneToOneMap(managementOrgSet).getEhrCdToEhrNmMap();
        Map<String, String> erpAcctCdToErpAcctNmMap = CollectionUtils.isEmpty(budgetSubjectCodeSet) 
            ? Collections.emptyMap() 
            : budgetQueryHelperService.queryErpAcctCdToAcctCdOneToOneMap(budgetSubjectCodeSet).getErpAcctCdToErpAcctNmMap();
        
        // 判断是否为业务异常：IllegalArgumentException 和 IllegalStateException 是业务异常
        boolean isBusinessException = e instanceof IllegalArgumentException || e instanceof IllegalStateException;
        
        // 根据异常类型判断返回状态：
        // - OA 和 HLY：统一返回 S（部分失败也返回 S）
        String returnStatus = "S";
        
        ESBRespInfoVo esbRespInfo = ESBRespInfoVo.builder()
                .instId(instId)
                .requestTime(esbInfo.getRequestTime())
                .attr1(esbInfo.getAttr1())
                .attr2(esbInfo.getAttr2())
                .attr3(esbInfo.getAttr3())
                .returnCode("E0001-ADJUST")
                .returnMsg(errorMessage)
                .returnStatus(returnStatus)
                .responseTime(responseTime)
                .build();

        // 构建返回的调整明细列表（所有明细都标记为失败）
        List<AdjustDetailRespVo> resultAdjustDetails = new ArrayList<>();
        if (adjustInfo != null && adjustInfo.getAdjustDetails() != null) {
            for (AdjustDetailDetailVo detail : adjustInfo.getAdjustDetails()) {
                AdjustDetailRespVo resultDetail = new AdjustDetailRespVo();
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
                
                resultDetail.setValidationResult("1");
                resultDetail.setValidationMessage(errorMessage);
                resultAdjustDetails.add(resultDetail);
            }
        }

        AdjustApplyResultInfoRespVo adjustApplyResult = new AdjustApplyResultInfoRespVo();
        adjustApplyResult.setAdjustOrderNo(adjustInfo != null ? adjustInfo.getAdjustOrderNo() : null);
        adjustApplyResult.setProcessTime(LocalDateTime.now().format(PROCESS_TIME_FORMATTER));
        adjustApplyResult.setIsInternal(adjustInfo != null ? adjustInfo.getIsInternal() : null);
        adjustApplyResult.setAdjustDetails(resultAdjustDetails);

        BudgetAdjustRespVo respVo = new BudgetAdjustRespVo();
        respVo.setEsbInfo(esbRespInfo);
        respVo.setAdjustApplyResult(adjustApplyResult);
        return respVo;
    }
    
    /**
     * 构建明细级别错误响应（部分明细成功，部分明细失败）
     */
    private BudgetAdjustRespVo buildResponseWithDetailErrors(BudgetAdjustApplyParams budgetAdjustApplyParams,
                                                            Map<String, String> detailValidationResultMap,
                                                            Map<String, String> detailValidationMessageMap,
                                                            Map<String, String> ehrCdToEhrNmMap,
                                                            Map<String, String> erpAcctCdToErpAcctNmMap) {
        ESBInfoParams esbInfo = budgetAdjustApplyParams.getEsbInfo();
        AdjustApplyReqInfoParams adjustInfo = budgetAdjustApplyParams.getAdjustApplyReqInfo();
        
        String instId = esbInfo.getInstId();
        if (instId == null || instId.isEmpty()) {
            String uuid = UUID.randomUUID().toString().replace("-", "");
            instId = "N" + uuid.substring(0, 8) + "." + uuid.substring(8, 16) + ".N"
                    + uuid.substring(16, 18) + "." + uuid.substring(18, 28) + ".N" + uuid.substring(28, 32);
        }
        String responseTime = LocalDateTime.now().format(DATE_TIME_FORMATTER);
        
        // 判断是否有失败的明细
        boolean hasError = detailValidationResultMap.values().stream().anyMatch(result -> "1".equals(result));
        
        // 根据 dataSource 判断返回状态：
        // - OA 和 HLY：部分失败也返回 S（保持原状）
        String returnStatus = "S";
        
        ESBRespInfoVo esbRespInfo = ESBRespInfoVo.builder()
                .instId(instId)
                .requestTime(esbInfo.getRequestTime())
                .attr1(esbInfo.getAttr1())
                .attr2(esbInfo.getAttr2())
                .attr3(esbInfo.getAttr3())
                .returnCode(hasError ? "E0001-ADJUST" : "A0001-ADJUST")
                .returnMsg(hasError ? "部分明细处理失败" : "预算调整申请处理成功")
                .returnStatus(returnStatus)
                .responseTime(responseTime)
                .build();

        // 构建返回的调整明细列表（包含每个明细的校验结果）
        List<AdjustDetailRespVo> resultAdjustDetails = new ArrayList<>();
        List<AdjustDetailDetailVo> adjustDetails = adjustInfo.getAdjustDetails();
        if (adjustDetails != null) {
            for (AdjustDetailDetailVo detail : adjustDetails) {
                AdjustDetailRespVo resultDetail = new AdjustDetailRespVo();
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
                String detailLineNo = detail.getAdjustDetailLineNo();
                resultDetail.setValidationResult(detailValidationResultMap.getOrDefault(detailLineNo, "1"));
                resultDetail.setValidationMessage(detailValidationMessageMap.getOrDefault(detailLineNo, "未处理"));
                
                resultAdjustDetails.add(resultDetail);
            }
        }

        AdjustApplyResultInfoRespVo adjustApplyResult = new AdjustApplyResultInfoRespVo();
        adjustApplyResult.setAdjustOrderNo(adjustInfo.getAdjustOrderNo());
        adjustApplyResult.setProcessTime(LocalDateTime.now().format(PROCESS_TIME_FORMATTER));
        adjustApplyResult.setIsInternal(adjustInfo.getIsInternal());
        adjustApplyResult.setAdjustDetails(resultAdjustDetails);

        BudgetAdjustRespVo respVo = new BudgetAdjustRespVo();
        respVo.setEsbInfo(esbRespInfo);
        respVo.setAdjustApplyResult(adjustApplyResult);
        return respVo;
    }

    /**
     * 尝试从异常信息中提取明细标识并记录错误
     * 
     * @param e 异常
     * @param detailValidationResultMap 明细校验结果Map
     * @param detailValidationMessageMap 明细校验消息Map
     * @return true 如果成功提取并记录了明细错误；false 如果无法识别具体明细
     */
    private boolean tryExtractDetailError(Exception e, 
                                         Map<String, String> detailValidationResultMap,
                                         Map<String, String> detailValidationMessageMap) {
        String errorMsg = e.getMessage();
        if (errorMsg == null) {
            return false;
        }
        
        // 支持两种格式：1. "明细 [bizCode@bizItemCode]"  2. "调减明细 [bizCode@bizItemCode]"
        String searchPattern = "明细 [";
        int patternIndex = errorMsg.indexOf(searchPattern);
        if (patternIndex == -1) {
            // 尝试查找"调减明细 ["
            searchPattern = "调减明细 [";
            patternIndex = errorMsg.indexOf(searchPattern);
            if (patternIndex == -1) {
                return false;
            }
        }
        
        // 提取 bizKey（格式：明细 [bizCode@bizItemCode] 或 调减明细 [bizCode@bizItemCode]）
        int start = errorMsg.indexOf("[", patternIndex) + 1;
        int end = errorMsg.indexOf("]", start);
        if (start <= 0 || end <= start) {
            return false;
        }
        
        String bizKey = errorMsg.substring(start, end);
        String[] parts = bizKey.split("@");
        if (parts.length < 2) {
            return false;
        }
        
        // bizKey 格式：bizCode@bizItemCode
        // bizCode = adjustOrderNo (parts[0])
        // bizItemCode = adjustDetailLineNo (parts[1] 及之后的所有部分，因为 bizItemCode 本身可能包含 @)
        // 所以需要从 parts[1] 开始重新组合
        StringBuilder detailLineNoBuilder = new StringBuilder(parts[1]);
        for (int i = 2; i < parts.length; i++) {
            detailLineNoBuilder.append("@").append(parts[i]);
        }
        String detailLineNo = detailLineNoBuilder.toString();
        
        log.info("========== 提取明细错误信息: bizKey={}, parts.length={}, detailLineNo={} ==========", 
                bizKey, parts.length, detailLineNo);
        
        // 从错误消息中移除 "明细 [bizKey] " 或 "调减明细 [bizKey] " 部分，只保留实际的错误描述
        String cleanErrorMessage = errorMsg;
        String prefixToRemove1 = "明细 [" + bizKey + "] ";
        String prefixToRemove2 = "调减明细 [" + bizKey + "] ";
        if (errorMsg.startsWith(prefixToRemove1)) {
            cleanErrorMessage = errorMsg.substring(prefixToRemove1.length());
        } else if (errorMsg.startsWith(prefixToRemove2)) {
            cleanErrorMessage = errorMsg.substring(prefixToRemove2.length());
        }
        
        detailValidationResultMap.put(detailLineNo, "1");
        detailValidationMessageMap.put(detailLineNo, cleanErrorMessage);
        log.error("明细 {} 处理失败: {}", detailLineNo, errorMsg);
        
        return true;
    }

    /**
     * 构建审批/撤回成功响应
     */
    private BudgetAdjustRenewRespVo buildSuccessResponse(BudgetAdjustRenewParams budgetAdjustRenewParams, String message) {
        String currentTime = LocalDateTime.now().format(DATE_TIME_FORMATTER);
        String instId = budgetAdjustRenewParams != null && budgetAdjustRenewParams.getEsbInfo() != null ? 
                budgetAdjustRenewParams.getEsbInfo().getInstId() : null;
        
        ESBRespInfoVo esbInfo = ESBRespInfoVo.builder()
                .instId(instId)
                .requestTime(budgetAdjustRenewParams != null && budgetAdjustRenewParams.getEsbInfo() != null ? 
                        budgetAdjustRenewParams.getEsbInfo().getRequestTime() : null)
                .attr1(budgetAdjustRenewParams != null && budgetAdjustRenewParams.getEsbInfo() != null ? 
                        budgetAdjustRenewParams.getEsbInfo().getAttr1() : null)
                .attr2(budgetAdjustRenewParams != null && budgetAdjustRenewParams.getEsbInfo() != null ? 
                        budgetAdjustRenewParams.getEsbInfo().getAttr2() : null)
                .attr3(budgetAdjustRenewParams != null && budgetAdjustRenewParams.getEsbInfo() != null ? 
                        budgetAdjustRenewParams.getEsbInfo().getAttr3() : null)
                .returnCode("A0001-ADJUST")
                .returnMsg(message)
                .returnStatus("S")
                .responseTime(currentTime)
                .build();

        BudgetAdjustRenewRespVo response = new BudgetAdjustRenewRespVo();
        response.setEsbInfo(esbInfo);
        return response;
    }

    /**
     * 构建审批/撤回错误响应
     */
    private BudgetAdjustRenewRespVo buildErrorResponse(BudgetAdjustRenewParams params, Exception e) {
        String currentTime = LocalDateTime.now().format(DATE_TIME_FORMATTER);
        String instId = params != null && params.getEsbInfo() != null ? 
                params.getEsbInfo().getInstId() : null;
        
        String errorMessage = e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName();
        
        // 统一返回 S（部分失败也返回 S）
        // 注意：审批/撤回参数中没有 dataSource 字段，默认使用 HLY 行为（保持原状）
        boolean isBusinessException = e instanceof IllegalArgumentException || e instanceof IllegalStateException;
        String returnStatus = "S";
        
        ESBRespInfoVo esbInfo = ESBRespInfoVo.builder()
                .instId(instId)
                .requestTime(params != null && params.getEsbInfo() != null ? 
                        params.getEsbInfo().getRequestTime() : null)
                .attr1(params != null && params.getEsbInfo() != null ? 
                        params.getEsbInfo().getAttr1() : null)
                .attr2(params != null && params.getEsbInfo() != null ? 
                        params.getEsbInfo().getAttr2() : null)
                .attr3(params != null && params.getEsbInfo() != null ? 
                        params.getEsbInfo().getAttr3() : null)
                .returnCode("E0001-ADJUST")
                .returnMsg(errorMessage)
                .returnStatus(returnStatus)
                .responseTime(currentTime)
                .build();

        BudgetAdjustRenewRespVo response = new BudgetAdjustRenewRespVo();
        response.setEsbInfo(esbInfo);
        return response;
    }

    /**
     * 将null转换为空列表
     */
    private List<AdjustDetailDetailVo> defaultList(List<AdjustDetailDetailVo> adjustDetails) {
        return adjustDetails == null ? Collections.emptyList() : adjustDetails;
    }

    /**
     * 根据实际扣减的 BUDGET_BALANCE 填充流水的 POOL_DIMENSION_KEY，与申请/合同/付款一致，便于重跑后根据本字段+季度查到同一资金池。
     */
    private void fillPoolDimensionKeyFromBalanceForLedgers(List<BudgetLedger> ledgersToInsert,
                                                          List<BudgetLedger> ledgersToUpdate,
                                                          Map<String, BudgetBalance> ledgerBizKeyToBalanceForPoolDimensionKey) {
        if (ledgerBizKeyToBalanceForPoolDimensionKey == null || ledgerBizKeyToBalanceForPoolDimensionKey.isEmpty()) {
            return;
        }
        for (BudgetLedger ledger : ledgersToInsert) {
            String bizKey = ledger.getBizCode() + "@" + ledger.getBizItemCode();
            BudgetBalance balance = ledgerBizKeyToBalanceForPoolDimensionKey.get(bizKey);
            if (balance != null) {
                ledger.setPoolDimensionKey(BudgetLedger.buildPoolDimensionKeyFromBalance(balance));
            }
        }
        for (BudgetLedger ledger : ledgersToUpdate) {
            String bizKey = ledger.getBizCode() + "@" + ledger.getBizItemCode();
            BudgetBalance balance = ledgerBizKeyToBalanceForPoolDimensionKey.get(bizKey);
            if (balance != null) {
                ledger.setPoolDimensionKey(BudgetLedger.buildPoolDimensionKeyFromBalance(balance));
            }
        }
    }

    /**
     * 执行数据库操作
     */
    private void performDatabaseOperations(List<BudgetLedgerHistory> ledgerHistoriesToInsert,
                                          List<BudgetLedger> ledgersToUpdate,
                                          List<BudgetLedger> ledgersToInsert,
                                          String adjustOrderNo,
                                          String documentName,
                                          String dataSource,
                                          String processName,
                                          BudgetLedgerHead existingHead,
                                          String operator,
                                          String operatorNo,
                                          LocalDateTime requestTime) {
        log.info("========== 开始执行数据库操作 ==========");
        
        // 1. 插入历史记录（如果有）
        if (!ledgerHistoriesToInsert.isEmpty()) {
            budgetLedgerHistoryMapper.insertBatch(ledgerHistoriesToInsert);
            log.info("========== 插入 BudgetLedgerHistory 完成，共 {} 条 ==========", ledgerHistoriesToInsert.size());
        }
        
        // 2. 更新现有的 BudgetLedger（如果有）
        if (!ledgersToUpdate.isEmpty()) {
            if (requestTime != null) {
                for (BudgetLedger ledger : ledgersToUpdate) {
                    ledger.setUpdateTime(requestTime);
                }
            }
            List<BudgetLedger> sortedLedgers = sortLedgersById(ledgersToUpdate);
            budgetLedgerMapper.updateBatchById(sortedLedgers);
            log.info("========== 更新 BudgetLedger 完成，共 {} 条 ==========", sortedLedgers.size());
        }
        
        // 3. 插入新的 BudgetLedger（如果有）
        if (!ledgersToInsert.isEmpty()) {
            if (requestTime != null) {
                for (BudgetLedger ledger : ledgersToInsert) {
                    ledger.setCreateTime(requestTime);
                    ledger.setUpdateTime(requestTime);
                }
            }
            budgetLedgerMapper.insertBatch(ledgersToInsert);
            log.info("========== 插入 BudgetLedger 完成，共 {} 条 ==========", ledgersToInsert.size());
        }
        
        // 4. 创建或更新 BudgetLedgerHead
        budgetQueryHelperService.createOrUpdateBudgetLedgerHead(
                adjustOrderNo,
                ADJUST_BIZ_TYPE,
                documentName,
                dataSource,
                processName,
                "SUBMITTED",
                identifierGenerator,
                operator,
                operatorNo,
                requestTime
        );
        log.info("========== {} BudgetLedgerHead 完成，adjustOrderNo={}, status=SUBMITTED ==========",
                existingHead != null ? "更新" : "创建", adjustOrderNo);
        
        log.info("========== 数据库操作完成 ==========");
    }

    /**
     * 从扩展明细创建新的 BudgetLedger
     */
    private BudgetLedger createNewLedgerFromExtDetail(AdjustExtDetailVo extDetail, String operator, String operatorNo, LocalDateTime requestTime) {
        Long ledgerId = identifierGenerator.nextId(null).longValue();
        
        BudgetLedger ledger = budgetQueryHelperService.createBudgetLedger(
                ledgerId,
                ADJUST_BIZ_TYPE,
                extDetail.getAdjustOrderNo(),
                extDetail.getAdjustDetailLineNo(),
                extDetail.getAdjustYear(),
                extDetail.getAdjustMonth(),
                extDetail.getIsInternal(),
                extDetail.getManagementOrg(),
                extDetail.getBudgetSubjectCode(),
                extDetail.getMasterProjectCode(),
                extDetail.getErpAssetType(),
                extDetail.getCurrency(),
                BigDecimal.ZERO, // 金额后续按四个季度汇总设置
                null,  // versionPre 为 null（新建）
                identifierGenerator,
                operator,
                operatorNo,
                requestTime
        );

        // 四个季度金额
        BigDecimal q1 = extDetail.getAdjustAmountQ1();
        BigDecimal q2 = extDetail.getAdjustAmountQ2();
        BigDecimal q3 = extDetail.getAdjustAmountQ3();
        BigDecimal q4 = extDetail.getAdjustAmountQ4();
        if (q1 == null) q1 = BigDecimal.ZERO;
        if (q2 == null) q2 = BigDecimal.ZERO;
        if (q3 == null) q3 = BigDecimal.ZERO;
        if (q4 == null) q4 = BigDecimal.ZERO;

        // 全年合计金额（Q1~Q4 + adjustAmountTotalInvestment）
        BigDecimal totalInvestment = extDetail.getAdjustAmountTotalInvestment() == null
                ? BigDecimal.ZERO
                : extDetail.getAdjustAmountTotalInvestment();
        // 总金额仅由全年合计调整金额决定，季度金额单独用于付款额度
        BigDecimal totalAmount = totalInvestment;
        ledger.setAmount(totalAmount);
        ledger.setAmountAvailable(totalAmount);
        ledger.setAmountConsumedQOne(q1);
        ledger.setAmountConsumedQTwo(q2);
        ledger.setAmountConsumedQThree(q3);
        ledger.setAmountConsumedQFour(q4);

        // 设置 effectType：使用前端传入的调整类型（"0"、"1"、"2"）
        // 0：预算调整-采购额，1：投资额调整，2：预算调整-付款额
        ledger.setEffectType(extDetail.getEffectType());

        // 设置 metadata 字段（仅透传前端/上游传入的 metadataJson）
        if (extDetail.getMetadataJson() != null) {
            ledger.setMetadata(extDetail.getMetadataJson());
        }
        
        return ledger;
    }

    /**
     * 使用扩展明细的数据更新现有 BudgetLedger
     */
    private void updateLedgerFromExtDetail(BudgetLedger existingLedger, AdjustExtDetailVo extDetail) {
        existingLedger.setYear(extDetail.getAdjustYear());
        existingLedger.setMonth(extDetail.getAdjustMonth());
        existingLedger.setIsInternal(extDetail.getIsInternal());
        existingLedger.setMorgCode(extDetail.getManagementOrg());
        
        // 空值转换：budgetSubjectCode 为空 → "NAN-NAN"
        existingLedger.setBudgetSubjectCode(
                StringUtils.isBlank(extDetail.getBudgetSubjectCode()) ? "NAN-NAN" : extDetail.getBudgetSubjectCode());
        // 空值转换：masterProjectCode 为空 → "NAN"
        existingLedger.setMasterProjectCode(
                StringUtils.isBlank(extDetail.getMasterProjectCode()) ? "NAN" : extDetail.getMasterProjectCode());
        // 空值转换：erpAssetType 为空 → "NAN"
        existingLedger.setErpAssetType(
                StringUtils.isBlank(extDetail.getErpAssetType()) ? "NAN" : extDetail.getErpAssetType());
        
        // 四个季度金额
        BigDecimal q1 = extDetail.getAdjustAmountQ1();
        BigDecimal q2 = extDetail.getAdjustAmountQ2();
        BigDecimal q3 = extDetail.getAdjustAmountQ3();
        BigDecimal q4 = extDetail.getAdjustAmountQ4();
        if (q1 == null) q1 = BigDecimal.ZERO;
        if (q2 == null) q2 = BigDecimal.ZERO;
        if (q3 == null) q3 = BigDecimal.ZERO;
        if (q4 == null) q4 = BigDecimal.ZERO;

        // 全年合计金额（Q1~Q4 + adjustAmountTotalInvestment）
        BigDecimal totalInvestment = extDetail.getAdjustAmountTotalInvestment() == null
                ? BigDecimal.ZERO
                : extDetail.getAdjustAmountTotalInvestment();
        // 总金额仅由全年合计调整金额决定，季度金额单独用于付款额度
        BigDecimal totalAmount = totalInvestment;
        existingLedger.setAmount(totalAmount);
        existingLedger.setAmountAvailable(totalAmount);
        existingLedger.setAmountConsumedQOne(q1);
        existingLedger.setAmountConsumedQTwo(q2);
        existingLedger.setAmountConsumedQThree(q3);
        existingLedger.setAmountConsumedQFour(q4);
        existingLedger.setCurrency(extDetail.getCurrency());
        
        // 设置 effectType：使用前端传入的调整类型（"0"、"1"、"2"）
        // 0：预算调整-采购额，1：投资额调整，2：预算调整-付款额
        existingLedger.setEffectType(extDetail.getEffectType());

        // 设置 metadata 字段（仅透传前端/上游传入的 metadataJson）
        if (extDetail.getMetadataJson() != null) {
            existingLedger.setMetadata(extDetail.getMetadataJson());
        }
        
        // 更新版本信息
        existingLedger.setVersionPre(existingLedger.getVersion());
        existingLedger.setVersion(String.valueOf(identifierGenerator.nextId(null)));
        
        log.info("========== 版本更新: 旧version={}, 新version={}, effectType={} ==========",
                existingLedger.getVersionPre(), existingLedger.getVersion(), existingLedger.getEffectType());
    }

    /**
     * 处理调增类型的调整：增加 BudgetBalance 和更新 BudgetQuota
     * 根据 effectType 和维度类型分别处理：
     * - effectType="0"：
     *   - 组织+科目/项目/组织+资产类型：调整 quota.amountAvailableAdj 和 balance.amountAvailable（使用 adjustAmountQ1-Q4）
     * - effectType="2"：
     *   - 组织+科目：调整 quota.amountAvailableAdj 和 balance.amountAvailable（使用 adjustAmountQ1-Q4）
     *   - 项目/组织+资产类型：调整 quota.amountPayAdj 和 balance.amountPayAvailable（使用 adjustAmountQ1-Q4）
     * - effectType="1"：调整 quota.amountAdj 和 balance.amountAvailable（使用 adjustAmountTotalInvestment，仅q1，仅项目维度）
     * 
     * @param increaseLedgers 调增明细列表
     * @param adjustAmountTotalInvestmentMap 每个明细的 adjustAmountTotalInvestment Map，key 为 bizCode + "@" + bizItemCode
     */
    private void processIncreaseAdjustments(List<BudgetLedger> increaseLedgers, Map<String, BigDecimal> adjustAmountTotalInvestmentMap) {
        // 1. 将 ledger 转换为 Map，用于查询 quota 和 balance
        Map<String, BudgetLedger> ledgerMap = new HashMap<>();
        for (BudgetLedger ledger : increaseLedgers) {
            String key = ledger.getBizCode() + "@" + ledger.getBizItemCode();
            ledgerMap.put(key, ledger);
        }
        
        // 批量提取 managementOrg 字段（对应 EHR_CD）
        Set<String> managementOrgSet = increaseLedgers.stream()
                .map(BudgetLedger::getMorgCode)
                .filter(StringUtils::isNotBlank)
                .collect(Collectors.toSet());
        
        // 批量查询 EHR_ORG_MANAGE_R 表，获取 EHR_CD 对应的 ORG_CD（一一对应关系）
        // Map<EHR_CD, ORG_CD>
        BudgetQueryHelperService.EhrCdToOrgCdMapResult ehrCdToOrgCdMapResult = budgetQueryHelperService.queryEhrCdToOrgCdOneToOneMap(managementOrgSet);
        Map<String, String> ehrCdToOrgCdMap = ehrCdToOrgCdMapResult.getEhrCdToOrgCdMap();
        
        // 批量查询 EHR_ORG_MANAGE_EXT_R 表，获取 EHR_CD 对应的所有 ORG_CD（一对多关系，用于识别不受控组织）
        // Map<EHR_CD, List<ORG_CD>>
        Map<String, List<String>> ehrCdToOrgCdExtMap = budgetQueryHelperService.queryEhrCdToOrgCdMap(managementOrgSet);
        if (ehrCdToOrgCdMap != null && !ehrCdToOrgCdMap.isEmpty()) {
            for (Map.Entry<String, String> e : ehrCdToOrgCdMap.entrySet()) {
                if (!ehrCdToOrgCdExtMap.containsKey(e.getKey())) {
                    ehrCdToOrgCdExtMap.put(e.getKey(), Collections.singletonList(e.getValue()));
                }
            }
        }
        
        // 批量提取 budgetSubjectCode 字段
        Set<String> budgetSubjectCodeSet = increaseLedgers.stream()
                .map(BudgetLedger::getBudgetSubjectCode)
                .filter(StringUtils::isNotBlank)
                .filter(code -> !"NAN-NAN".equals(code))
                .collect(Collectors.toSet());
        
        // 批量查询 SUBJECT_INFO 表，获取 ERP_ACCT_CD 对应的 ACCT_CD（一一对应关系）
        // Map<ERP_ACCT_CD, ACCT_CD>
        BudgetQueryHelperService.ErpAcctCdToAcctCdMapResult erpAcctCdToAcctCdMapResult = budgetQueryHelperService.queryErpAcctCdToAcctCdOneToOneMap(budgetSubjectCodeSet);
        Map<String, String> erpAcctCdToAcctCdMap = erpAcctCdToAcctCdMapResult.getErpAcctCdToAcctCdMap();
        
        // 批量查询 SUBJECT_EXT_INFO 表，获取 ERP_ACCT_CD 对应的所有 ACCT_CD（一对多关系，用于识别不受控科目）
        // Map<ERP_ACCT_CD, List<ACCT_CD>>
        Map<String, List<String>> erpAcctCdToAcctCdExtMap = budgetQueryHelperService.queryErpAcctCdToAcctCdMap(budgetSubjectCodeSet);
        
        // 批量提取需要映射的 erpAssetType 字段（以 "1" 或 "M" 开头且不带项目的）
        Set<String> erpAssetTypeSet = increaseLedgers.stream()
                .filter(ledger -> {
                    String masterProjectCode = ledger.getMasterProjectCode();
                    // 只提取不带项目的明细的 erpAssetType
                    return "NAN".equals(masterProjectCode) || StringUtils.isBlank(masterProjectCode);
                })
                .map(BudgetLedger::getErpAssetType)
                .filter(StringUtils::isNotBlank)
                .filter(code -> !"NAN".equals(code))
                .filter(code -> code.startsWith("1") || code.startsWith("M")) // 只提取需要映射的
                .collect(Collectors.toSet());
        
        // 批量查询 VIEW_BUDGET_MEMBER_NAME_CODE 视图，获取 MEMBER_CD2 对应的 MEMBER_CD（一一对应关系）
        Map<String, String> erpAssetTypeToMemberCdMap = budgetQueryHelperService.queryErpAssetTypeToMemberCdMap(erpAssetTypeSet);
        log.info("========== 获取到 erpAssetType 映射表，共 {} 条记录 ==========", erpAssetTypeToMemberCdMap.size());
        
        // 2. 过滤掉不受控的ledger，只查询受控的ledger的quota和balance
        Map<String, BudgetLedger> controlledLedgerMap = new HashMap<>();
        for (Map.Entry<String, BudgetLedger> entry : ledgerMap.entrySet()) {
            BudgetLedger ledger = entry.getValue();
            // 跳过不受控的ledger
            if (!isUncontrolledLedger(ledger, ehrCdToOrgCdExtMap, erpAcctCdToAcctCdExtMap)) {
                controlledLedgerMap.put(entry.getKey(), ledger);
            }
        }
        
        // 2.1 按业务维度聚合（不区分 effectType），同一维度只用一个代表 ledger 参与查询，避免同一项目 effectType=1 与 effectType=2 各建一套导致 8 条记录
        Map<String, BudgetLedger> dimensionKeyToRepresentativeLedger = new LinkedHashMap<>();
        for (Map.Entry<String, BudgetLedger> entry : controlledLedgerMap.entrySet()) {
            String dimensionKey = buildDimensionKey(entry.getKey());
            dimensionKeyToRepresentativeLedger.putIfAbsent(dimensionKey, entry.getValue());
        }
        
        // 3. 查询所有季度的 BudgetQuota 和 BudgetBalance（按维度 key 查询，每个维度只查一次）
        BudgetQueryHelperService.BudgetQuotaBalanceResult result = 
                budgetQueryHelperService.queryQuotaAndBalanceByAllQuarters(dimensionKeyToRepresentativeLedger, ehrCdToOrgCdMap, erpAcctCdToAcctCdMap, erpAssetTypeToMemberCdMap);
        
        Map<String, BudgetQuota> quotaMap = result.getQuotaMap();
        Map<String, BudgetBalance> balanceMap = result.getBalanceMap();
        
        // 3. 维护本次操作中每个 balance 和 quota 的累计变化（用于处理多条明细映射到同一个 balance/quota 的情况）
        Map<Long, BigDecimal> balanceVchangedMap = new HashMap<>(); // amountAvailable 的变更值
        Map<Long, BigDecimal> balancePayAvailableVchangedMap = new HashMap<>(); // amountPayAvailable 的变更值
        Map<Long, BigDecimal> quotaTotalVchangedMap = new HashMap<>(); // amountTotal 的变更值
        Map<Long, BigDecimal> quotaPayVchangedMap = new HashMap<>(); // amountPay 的变更值
        Map<Long, BigDecimal> quotaAdjVchangedMap = new HashMap<>(); // amountAdj 的变更值（仅q1）
        Map<Long, BigDecimal> quotaAvailableAdjVchangedMap = new HashMap<>(); // amountAvailableAdj 的变更值
        Map<Long, BigDecimal> quotaPayAdjVchangedMap = new HashMap<>(); // amountPayAdj 的变更值
        // 保存每个 balance 的原始 amountAvailable 值（用于确保数据一致性）
        Map<Long, BigDecimal> balanceOriginalAmountAvailableMap = new HashMap<>();
        Set<Long> processedBalanceIds = new HashSet<>();
        Set<Long> processedQuotaIds = new HashSet<>();
        
        // 用于收集需要新增的 BudgetPoolDemR、BudgetQuota、BudgetBalance
        Map<String, BudgetPoolDemR> needToAddPoolDemRMap = new HashMap<>();
        List<BudgetQuota> needToAddQuotaList = new ArrayList<>();
        List<BudgetBalance> needToAddBalanceList = new ArrayList<>();
        
        // 4. 遍历每个调增明细，增加预算
        List<BudgetBalance> balancesToUpdate = new ArrayList<>();
        List<BudgetBalanceHistory> balanceHistories = new ArrayList<>();
        List<BudgetQuota> quotasToUpdate = new ArrayList<>();
        List<BudgetQuotaHistory> quotaHistories = new ArrayList<>();
        
        for (BudgetLedger ledger : increaseLedgers) {
            String bizKey = ledger.getBizCode() + "@" + ledger.getBizItemCode();
            String dimensionKey = buildDimensionKey(bizKey); // 同一维度不区分 effectType，共用一套 pool/quota/balance
            String effectType = ledger.getEffectType(); // 调整类型："0"、"1"、"2"

            // 项目非 NAN 且 isInternal=1 时跳过处理
            if (!"NAN".equals(ledger.getMasterProjectCode()) && "1".equals(ledger.getIsInternal())) {
                log.info("========== 调增处理 - 内部项目跳过: bizKey={}, masterProjectCode={}, isInternal={} ==========",
                        bizKey, ledger.getMasterProjectCode(), ledger.getIsInternal());
                continue;
            }
            
            // 判断是否不受控（需要跳过预算余额更新，但仍保存数据到BUDGET_LEDGER表）
            if (isUncontrolledLedger(ledger, ehrCdToOrgCdExtMap, erpAcctCdToAcctCdExtMap)) {
                log.info("========== 调增处理 - 不受控明细跳过预算余额更新: bizKey={}, morgCode={}, budgetSubjectCode={}, masterProjectCode={} ==========",
                        bizKey, ledger.getMorgCode(), ledger.getBudgetSubjectCode(), ledger.getMasterProjectCode());
                continue;
            }

            // effectType="1"：投资额调整，只处理 adjustAmountTotalInvestment（仅q1）
            if ("1".equals(effectType)) {
                BigDecimal adjustAmountTotalInvestment = adjustAmountTotalInvestmentMap.get(bizKey);
                if (adjustAmountTotalInvestment == null || adjustAmountTotalInvestment.compareTo(BigDecimal.ZERO) <= 0) {
                    continue; // 没有调增金额，跳过
                }
                
                String quarter = "q1";
                String bizKeyQuarter = bizKey + "@" + quarter;
                String dimensionKeyQuarter = dimensionKey + "@" + quarter;
                
                // 获取对应的 balance（仅q1），优先用维度 key 取，使同维度 effectType=2 复用 effectType=1 创建的记录
                // 查询返回的 balanceMap 的 key 为 bizKeyQuarter（含 effectType、科目），故 dimensionKeyQuarter 可能查不到，需再按 bizKeyQuarter 查找，避免误创建新记录
                BudgetBalance balance = balanceMap.get(dimensionKeyQuarter);
                if (balance == null) {
                    balance = balanceMap.get(bizKeyQuarter);
                }
                if (balance == null) {
                    // 查询不到预算维度，自动创建新记录（会为所有4个季度创建）
                    balance = createBudgetRecordsIfNotExists(ledger, bizKeyQuarter, quarter,
                            ehrCdToOrgCdMap, erpAcctCdToAcctCdMap, erpAssetTypeToMemberCdMap,
                            needToAddPoolDemRMap, quotaMap, balanceMap);
                    if (balance == null) {
                        // 创建失败或已存在，重新获取（先试 dimensionKey，再试 bizKey）
                        balance = balanceMap.get(dimensionKeyQuarter);
                        if (balance == null) {
                            balance = balanceMap.get(bizKeyQuarter);
                        }
                        if (balance == null) {
                            throw new IllegalStateException(
                                String.format("调增明细 [%s] 在季度 %s 的维度组合未找到对应的预算余额，且创建失败。" +
                                             "维度信息：年度=%s, 管理组织=%s, 预算科目=%s, 项目=%s, 资产类型=%s",
                                             bizKey, quarter, ledger.getYear(), ledger.getMorgCode(),
                                             ledger.getBudgetSubjectCode(), ledger.getMasterProjectCode(),
                                             ledger.getErpAssetType())
                            );
                        }
                    } else {
                        // 新创建的记录，需要将所有4个季度的记录加入到新增列表，并同步到 dimensionKey，供同维度 effectType=2 复用
                        String[] allQuarters = {"q1", "q2", "q3", "q4"};
                        for (String q : allQuarters) {
                            String bizKeyQ = bizKey + "@" + q;
                            String dimKeyQ = dimensionKey + "@" + q;
                            if (needToAddPoolDemRMap.containsKey(bizKeyQ)) {
                                // 说明是新创建的记录
                                BudgetQuota quota = quotaMap.get(bizKeyQ);
                                BudgetBalance bal = balanceMap.get(bizKeyQ);
                                if (quota != null && !needToAddQuotaList.contains(quota)) {
                                    needToAddQuotaList.add(quota);
                                }
                                if (bal != null && !needToAddBalanceList.contains(bal)) {
                                    needToAddBalanceList.add(bal);
                                }
                                // 同维度 key 也指向同一套记录，避免同项目 effectType=2 再建一套
                                if (quota != null) {
                                    quotaMap.put(dimKeyQ, quota);
                                }
                                if (bal != null) {
                                    balanceMap.put(dimKeyQ, bal);
                                }
                            }
                        }
                    }
                }

                // 保存 Balance 历史（每个 balance 只保存一次）
                if (!processedBalanceIds.contains(balance.getId())) {
                    BudgetBalanceHistory balanceHistory = new BudgetBalanceHistory();
                    BeanUtils.copyProperties(balance, balanceHistory);
                    balanceHistory.setId(identifierGenerator.nextId(balanceHistory).longValue());
                    balanceHistory.setBalanceId(balance.getId());
                    balanceHistory.setDeleted(Boolean.FALSE);
                    balanceHistories.add(balanceHistory);
                    processedBalanceIds.add(balance.getId());
                }

                // 获取对应的 Quota（仅q1），优先用维度 key
                BudgetQuota quota = quotaMap.get(dimensionKeyQuarter);
                if (quota == null) {
                    quota = quotaMap.get(bizKeyQuarter);
                }
                if (quota != null) {
                    // 保存 Quota 历史（每个 quota 只保存一次）
                    if (!processedQuotaIds.contains(quota.getId())) {
                        BudgetQuotaHistory quotaHistory = new BudgetQuotaHistory();
                        BeanUtils.copyProperties(quota, quotaHistory);
                        quotaHistory.setId(identifierGenerator.nextId(quotaHistory).longValue());
                        quotaHistory.setQuotaId(quota.getId());
                        quotaHistory.setDeleted(Boolean.FALSE);
                        quotaHistories.add(quotaHistory);
                        processedQuotaIds.add(quota.getId());
                    }
                }

                // effectType="1"：调整 quota.amountAdj 和 balance.amountAvailable
                // 保存原始的 amountAvailable 值（第一次处理时保存，用于确保数据一致性）
                if (!balanceOriginalAmountAvailableMap.containsKey(balance.getId())) {
                    balanceOriginalAmountAvailableMap.put(balance.getId(), 
                        balance.getAmountAvailable() == null ? BigDecimal.ZERO : balance.getAmountAvailable());
                }
                BigDecimal currentAvailable = balance.getAmountAvailable() == null ? BigDecimal.ZERO : balance.getAmountAvailable();
                balance.setAmountAvailable(currentAvailable.add(adjustAmountTotalInvestment));
                balanceVchangedMap.merge(balance.getId(), adjustAmountTotalInvestment, BigDecimal::add);
                
                if (quota != null) {
                    BigDecimal currentAmountAdj = quota.getAmountAdj() == null ? BigDecimal.ZERO : quota.getAmountAdj();
                    quota.setAmountAdj(currentAmountAdj.add(adjustAmountTotalInvestment));
                    quotaAdjVchangedMap.merge(quota.getId(), adjustAmountTotalInvestment, BigDecimal::add);
                    log.info("========== 调增（effectType=1）quota.amountAdj: poolId={}, quarter={}, 调增前={}, 调增金额={}, 调增后={} ==========",
                            quota.getPoolId(), quarter, currentAmountAdj, adjustAmountTotalInvestment, quota.getAmountAdj());
                }
                
                log.info("========== 调增（effectType=1）balance.amountAvailable: poolId={}, quarter={}, 调增前={}, 调增金额={}, 调增后={} ==========",
                        balance.getPoolId(), quarter, currentAvailable, adjustAmountTotalInvestment, balance.getAmountAvailable());

                // 将需要更新的 balance 和 quota 加入列表（去重）
                if (!balancesToUpdate.contains(balance)) {
                    balancesToUpdate.add(balance);
                }
                if (quota != null && !quotasToUpdate.contains(quota)) {
                    quotasToUpdate.add(quota);
                }
                
                continue; // effectType="1" 处理完成，继续下一个 ledger
            }

            // effectType="0" 或 "2"：处理 adjustAmountQ1-Q4
            BigDecimal[] quarterAmounts = new BigDecimal[] {
                    ledger.getAmountConsumedQOne() == null ? BigDecimal.ZERO : ledger.getAmountConsumedQOne(),
                    ledger.getAmountConsumedQTwo() == null ? BigDecimal.ZERO : ledger.getAmountConsumedQTwo(),
                    ledger.getAmountConsumedQThree() == null ? BigDecimal.ZERO : ledger.getAmountConsumedQThree(),
                    ledger.getAmountConsumedQFour() == null ? BigDecimal.ZERO : ledger.getAmountConsumedQFour()
            };
            String[] quarters = new String[] {"q1", "q2", "q3", "q4"};

            for (int i = 0; i < quarters.length; i++) {
                BigDecimal adjustAmount = quarterAmounts[i];
                // 只有正数才视为调增；0 或 负数都跳过
                if (adjustAmount.compareTo(BigDecimal.ZERO) <= 0) {
                    continue;
                }
                String quarter = quarters[i];
                String bizKeyQuarter = bizKey + "@" + quarter;
                String dimensionKeyQuarter = dimensionKey + "@" + quarter;

                // 获取对应的 balance，优先用维度 key 使同维度 effectType=2 复用 effectType=1 创建的记录
                // 查询返回的 balanceMap 的 key 为 bizKeyQuarter（含 effectType、科目），故 dimensionKeyQuarter 可能查不到，需再按 bizKeyQuarter 查找，避免误创建新记录
                BudgetBalance balance = balanceMap.get(dimensionKeyQuarter);
                if (balance == null) {
                    balance = balanceMap.get(bizKeyQuarter);
                }
                if (balance == null) {
                    // 查询不到预算维度，自动创建新记录（会为所有4个季度创建）
                    balance = createBudgetRecordsIfNotExists(ledger, bizKeyQuarter, quarter,
                            ehrCdToOrgCdMap, erpAcctCdToAcctCdMap, erpAssetTypeToMemberCdMap,
                            needToAddPoolDemRMap, quotaMap, balanceMap);
                    if (balance == null) {
                        // 创建失败或已存在，重新获取（先试 dimensionKey，再试 bizKey）
                        balance = balanceMap.get(dimensionKeyQuarter);
                        if (balance == null) {
                            balance = balanceMap.get(bizKeyQuarter);
                        }
                        if (balance == null) {
                            throw new IllegalStateException(
                                String.format("调增明细 [%s] 在季度 %s 的维度组合未找到对应的预算余额，且创建失败。" +
                                             "维度信息：年度=%s, 管理组织=%s, 预算科目=%s, 项目=%s, 资产类型=%s",
                                             bizKey, quarter, ledger.getYear(), ledger.getMorgCode(),
                                             ledger.getBudgetSubjectCode(), ledger.getMasterProjectCode(),
                                             ledger.getErpAssetType())
                            );
                        }
                    } else {
                        // 新创建的记录，需要将所有4个季度的记录加入到新增列表，并同步到 dimensionKey，供同维度 effectType=1 或其他季度复用
                        String[] allQuarters = {"q1", "q2", "q3", "q4"};
                        for (String q : allQuarters) {
                            String bizKeyQ = bizKey + "@" + q;
                            String dimKeyQ = dimensionKey + "@" + q;
                            if (needToAddPoolDemRMap.containsKey(bizKeyQ)) {
                                // 说明是新创建的记录
                                BudgetQuota quota = quotaMap.get(bizKeyQ);
                                BudgetBalance bal = balanceMap.get(bizKeyQ);
                                if (quota != null && !needToAddQuotaList.contains(quota)) {
                                    needToAddQuotaList.add(quota);
                                }
                                if (bal != null && !needToAddBalanceList.contains(bal)) {
                                    needToAddBalanceList.add(bal);
                                }
                                // 同维度 key 也指向同一套记录，避免同项目多 effectType 各建一套
                                if (quota != null) {
                                    quotaMap.put(dimKeyQ, quota);
                                }
                                if (bal != null) {
                                    balanceMap.put(dimKeyQ, bal);
                                }
                            }
                        }
                    }
                }

                log.info("========== 调增处理: bizKey={}, effectType={}, quarter={}, 调增金额={} ==========",
                        bizKey, effectType, quarter, adjustAmount);

                // 保存 Balance 历史（每个 balance 只保存一次）
                if (!processedBalanceIds.contains(balance.getId())) {
                    BudgetBalanceHistory balanceHistory = new BudgetBalanceHistory();
                    BeanUtils.copyProperties(balance, balanceHistory);
                    balanceHistory.setId(identifierGenerator.nextId(balanceHistory).longValue());
                    balanceHistory.setBalanceId(balance.getId());
                    balanceHistory.setDeleted(Boolean.FALSE);
                    balanceHistories.add(balanceHistory);
                    processedBalanceIds.add(balance.getId());
                }

                // 获取对应的 Quota，优先用维度 key
                BudgetQuota quota = quotaMap.get(dimensionKeyQuarter);
                if (quota == null) {
                    quota = quotaMap.get(bizKeyQuarter);
                }
                if (quota != null) {
                    // 保存 Quota 历史（每个 quota 只保存一次）
                    if (!processedQuotaIds.contains(quota.getId())) {
                        BudgetQuotaHistory quotaHistory = new BudgetQuotaHistory();
                        BeanUtils.copyProperties(quota, quotaHistory);
                        quotaHistory.setId(identifierGenerator.nextId(quotaHistory).longValue());
                        quotaHistory.setQuotaId(quota.getId());
                        quotaHistory.setDeleted(Boolean.FALSE);
                        quotaHistories.add(quotaHistory);
                        processedQuotaIds.add(quota.getId());
                    }
                }

                // 判断维度类型
                DimensionType dimensionType = getDimensionType(bizKey);
                
                // effectType="0"：根据维度类型使用不同的字段
                if ("0".equals(effectType)) {
                    // 所有维度类型：调整 balance.amountAvailable
                    BigDecimal currentAvailable = balance.getAmountAvailable() == null ? BigDecimal.ZERO : balance.getAmountAvailable();
                    balance.setAmountAvailable(currentAvailable.add(adjustAmount));
                    balanceVchangedMap.merge(balance.getId(), adjustAmount, BigDecimal::add);
                    
                    if (quota != null) {
                        // 根据维度类型决定更新哪个字段
                        if (dimensionType == DimensionType.ORG_SUBJECT || dimensionType == DimensionType.PROJECT || dimensionType == DimensionType.ORG_ASSET_TYPE) {
                            // 组织+科目、项目、组织+资产类型：使用 amountAvailableAdj
                            BigDecimal currentAmountAvailableAdj = quota.getAmountAvailableAdj() == null ? BigDecimal.ZERO : quota.getAmountAvailableAdj();
                            quota.setAmountAvailableAdj(currentAmountAvailableAdj.add(adjustAmount));
                            quotaAvailableAdjVchangedMap.merge(quota.getId(), adjustAmount, BigDecimal::add);
                            log.info("========== 调增（effectType=0, {}）quota.amountAvailableAdj: poolId={}, quarter={}, 调增前={}, 调增金额={}, 调增后={} ==========",
                                    dimensionType, quota.getPoolId(), quarter, currentAmountAvailableAdj, adjustAmount, quota.getAmountAvailableAdj());
                        }
                    }
                    
                    log.info("========== 调增（effectType=0, {}）balance.amountAvailable: poolId={}, quarter={}, 调增前={}, 调增金额={}, 调增后={} ==========",
                            dimensionType, balance.getPoolId(), quarter, currentAvailable, adjustAmount, balance.getAmountAvailable());
                }
                // effectType="2"：根据维度类型使用不同的字段
                else if ("2".equals(effectType)) {
                    if (dimensionType == DimensionType.ORG_SUBJECT) {
                        // 组织+科目：effectType=2 也使用 amountAvailableAdj（和 effectType=0 一样）
                        BigDecimal currentAvailable = balance.getAmountAvailable() == null ? BigDecimal.ZERO : balance.getAmountAvailable();
                        balance.setAmountAvailable(currentAvailable.add(adjustAmount));
                        balanceVchangedMap.merge(balance.getId(), adjustAmount, BigDecimal::add);
                        
                        if (quota != null) {
                            BigDecimal currentAmountAvailableAdj = quota.getAmountAvailableAdj() == null ? BigDecimal.ZERO : quota.getAmountAvailableAdj();
                            quota.setAmountAvailableAdj(currentAmountAvailableAdj.add(adjustAmount));
                            quotaAvailableAdjVchangedMap.merge(quota.getId(), adjustAmount, BigDecimal::add);
                            log.info("========== 调增（effectType=2, 组织+科目）quota.amountAvailableAdj: poolId={}, quarter={}, 调增前={}, 调增金额={}, 调增后={} ==========",
                                    quota.getPoolId(), quarter, currentAmountAvailableAdj, adjustAmount, quota.getAmountAvailableAdj());
                        }
                        
                        log.info("========== 调增（effectType=2, 组织+科目）balance.amountAvailable: poolId={}, quarter={}, 调增前={}, 调增金额={}, 调增后={} ==========",
                                balance.getPoolId(), quarter, currentAvailable, adjustAmount, balance.getAmountAvailable());
                    } else {
                        // 项目、组织+资产类型：调整 balance.amountPayAvailable 和 quota.amountPayAdj
                        BigDecimal currentAmountPayAvailable = balance.getAmountPayAvailable() == null ? BigDecimal.ZERO : balance.getAmountPayAvailable();
                        balance.setAmountPayAvailable(currentAmountPayAvailable.add(adjustAmount));
                        balancePayAvailableVchangedMap.merge(balance.getId(), adjustAmount, BigDecimal::add);
                        
                        if (quota != null) {
                            BigDecimal currentAmountPayAdj = quota.getAmountPayAdj() == null ? BigDecimal.ZERO : quota.getAmountPayAdj();
                            quota.setAmountPayAdj(currentAmountPayAdj.add(adjustAmount));
                            quotaPayAdjVchangedMap.merge(quota.getId(), adjustAmount, BigDecimal::add);
                            log.info("========== 调增（effectType=2, {}）quota.amountPayAdj: poolId={}, quarter={}, 调增前={}, 调增金额={}, 调增后={} ==========",
                                    dimensionType, quota.getPoolId(), quarter, currentAmountPayAdj, adjustAmount, quota.getAmountPayAdj());
                        }
                        
                        log.info("========== 调增（effectType=2, {}）balance.amountPayAvailable: poolId={}, quarter={}, 调增前={}, 调增金额={}, 调增后={} ==========",
                                dimensionType, balance.getPoolId(), quarter, currentAmountPayAvailable, adjustAmount, balance.getAmountPayAvailable());
                    }
                }

                // 将需要更新的 balance 和 quota 加入列表（去重）
                if (!balancesToUpdate.contains(balance)) {
                    balancesToUpdate.add(balance);
                }
                if (quota != null && !quotasToUpdate.contains(quota)) {
                    quotasToUpdate.add(quota);
                }
            }
        }
        
        // 5. 设置每个 balance 和 quota 的本次操作累计变化量
        for (BudgetBalance balance : balancesToUpdate) {
            BigDecimal totalVchanged = balanceVchangedMap.getOrDefault(balance.getId(), BigDecimal.ZERO);
            balance.setAmountAvailableVchanged(totalVchanged);  // 调增为正数
            
            // 确保 amountAvailable 也被正确更新：基于原始值和累计变化量重新计算
            // 这样可以避免多条明细映射到同一个 balance 时，amountAvailable 累加不一致的问题
            if (totalVchanged.compareTo(BigDecimal.ZERO) != 0) {
                // 从保存的原始值Map中获取，如果不存在说明是新创建的balance（初始值为0或null）
                BigDecimal originalAmountAvailable = balanceOriginalAmountAvailableMap.get(balance.getId());
                if (originalAmountAvailable == null) {
                    // 如果Map中没有，说明这个balance没有被处理过（理论上不应该发生）
                    // 为了安全起见，使用当前值减去总变化量作为原始值
                    BigDecimal currentAmountAvailable = balance.getAmountAvailable() == null ? BigDecimal.ZERO : balance.getAmountAvailable();
                    originalAmountAvailable = currentAmountAvailable.subtract(totalVchanged);
                    log.warn("========== Balance原始值未找到，使用当前值反推: poolId={}, 当前值={}, 总变化={}, 反推原始值={} ==========",
                            balance.getPoolId(), currentAmountAvailable, totalVchanged, originalAmountAvailable);
                }
                // 基于原始值和累计变化量重新计算，确保数据一致性
                BigDecimal recalculatedAmountAvailable = originalAmountAvailable.add(totalVchanged);
                balance.setAmountAvailable(recalculatedAmountAvailable);
                log.info("========== Balance重新计算amountAvailable: poolId={}, 原始值={}, 累计变化={}, 重新计算后={} ==========",
                        balance.getPoolId(), originalAmountAvailable, totalVchanged, recalculatedAmountAvailable);
            }
            
            BigDecimal totalPayAvailableVchanged = balancePayAvailableVchangedMap.getOrDefault(balance.getId(), BigDecimal.ZERO);
            balance.setAmountPayAvailableVchanged(totalPayAvailableVchanged);  // 调增为正数
            log.info("========== Balance最终变化: poolId={}, amountAvailable={}, amountAvailableVchanged={}, amountPayAvailableVchanged={} ==========",
                    balance.getPoolId(), balance.getAmountAvailable(), balance.getAmountAvailableVchanged(), balance.getAmountPayAvailableVchanged());
        }
        for (BudgetQuota quota : quotasToUpdate) {
            BigDecimal totalTotalVchanged = quotaTotalVchangedMap.getOrDefault(quota.getId(), BigDecimal.ZERO);
            quota.setAmountTotalVchanged(totalTotalVchanged);  // 调增为正数（effectType=0时使用，但已改为使用新字段）
            BigDecimal totalPayVchanged = quotaPayVchangedMap.getOrDefault(quota.getId(), BigDecimal.ZERO);
            quota.setAmountPayVchanged(totalPayVchanged);  // 调增为正数（effectType=2时使用，但已改为使用新字段）
            // 注意：amountAdj、amountAvailableAdj、amountPayAdj 没有对应的 Vchanged 字段，直接更新值即可
            // 新字段的变化量已在上面处理时直接更新到字段值中
            log.info("========== Quota最终变化: poolId={}, amountTotalVchanged={}, amountPayVchanged={}, amountAvailableAdj={}, amountPayAdj={} ==========",
                    quota.getPoolId(), quota.getAmountTotalVchanged(), quota.getAmountPayVchanged(), 
                    quota.getAmountAvailableAdj(), quota.getAmountPayAdj());
        }
        
        // 6. 批量插入新创建的记录（必须在更新之前插入）
        if (!needToAddPoolDemRMap.isEmpty()) {
            List<BudgetPoolDemR> poolDemRsToAdd = new ArrayList<>(needToAddPoolDemRMap.values());
            for (BudgetPoolDemR poolDemR : poolDemRsToAdd) {
                budgetPoolDemRMapper.insert(poolDemR);
            }
            log.info("========== 插入 BudgetPoolDemR 完成，共 {} 条 ==========", poolDemRsToAdd.size());
        }
        if (!needToAddQuotaList.isEmpty()) {
            budgetQuotaMapper.insertBatch(needToAddQuotaList);
            log.info("========== 插入 BudgetQuota（新增）完成，共 {} 条 ==========", needToAddQuotaList.size());
        }
        if (!needToAddBalanceList.isEmpty()) {
            budgetBalanceMapper.insertBatch(needToAddBalanceList);
            log.info("========== 插入 BudgetBalance（新增）完成，共 {} 条 ==========", needToAddBalanceList.size());
        }
        
        // 7. 批量更新数据库
        if (!balanceHistories.isEmpty()) {
            budgetBalanceHistoryMapper.insertBatch(balanceHistories);
            log.info("========== 插入 BudgetBalanceHistory 完成，共 {} 条 ==========", balanceHistories.size());
        }
        if (!balancesToUpdate.isEmpty()) {
            List<BudgetBalance> sortedBalances = sortBalancesById(balancesToUpdate);
            budgetBalanceMapper.updateBatchById(sortedBalances);
            log.info("========== 更新 BudgetBalance 完成，共 {} 条 ==========", sortedBalances.size());
        }
        if (!quotaHistories.isEmpty()) {
            budgetQuotaHistoryMapper.insertBatch(quotaHistories);
            log.info("========== 插入 BudgetQuotaHistory 完成，共 {} 条 ==========", quotaHistories.size());
        }
        if (!quotasToUpdate.isEmpty()) {
            List<BudgetQuota> sortedQuotas = sortQuotasById(quotasToUpdate);
            budgetQuotaMapper.updateBatchById(sortedQuotas);
            log.info("========== 更新 BudgetQuota 完成，共 {} 条 ==========", sortedQuotas.size());
        }
        
        log.info("========== 调增处理完成 ==========");
    }

    /**
     * 处理调减类型的调整：扣减 BudgetBalance 和更新 BudgetQuota
     * 根据 effectType 和维度类型分别处理：
     * - effectType="0"：
     *   - 组织+科目/项目/组织+资产类型：调整 quota.amountAvailableAdj 和 balance.amountAvailable（使用 adjustAmountQ1-Q4）
     * - effectType="2"：
     *   - 组织+科目：调整 quota.amountAvailableAdj 和 balance.amountAvailable（使用 adjustAmountQ1-Q4）
     *   - 项目/组织+资产类型：调整 quota.amountPayAdj 和 balance.amountPayAvailable（使用 adjustAmountQ1-Q4）
     * - effectType="1"：调整 quota.amountAdj 和 balance.amountAvailable（使用 adjustAmountTotalInvestment，仅q1，仅项目维度）
     * 
     * @param decreaseLedgers 调减明细列表
     * @param adjustAmountTotalInvestmentMap 每个明细的 adjustAmountTotalInvestment Map，key 为 bizCode + "@" + bizItemCode
     * @param skipBudgetValidation 为 true 时跳过预算余额校验，直接扣减
     */
    /**
     * 处理调减明细：校验并扣减 BudgetBalance/BudgetQuota。
     * @param ledgerBizKeyToBalanceForPoolDimensionKey 输出参数：扣减时实际使用的 Balance（bizKey -> 任一季度的 Balance），用于后续填充流水的 POOL_DIMENSION_KEY；可为空 Map，不可为 null
     */
    private void processDecreaseAdjustments(List<BudgetLedger> decreaseLedgers, Map<String, BigDecimal> adjustAmountTotalInvestmentMap, boolean skipBudgetValidation,
                                            Map<String, BudgetBalance> ledgerBizKeyToBalanceForPoolDimensionKey) {
        // 1. 将 ledger 转换为 Map，用于查询 quota 和 balance
        Map<String, BudgetLedger> ledgerMap = new HashMap<>();
        for (BudgetLedger ledger : decreaseLedgers) {
            String key = ledger.getBizCode() + "@" + ledger.getBizItemCode();
            ledgerMap.put(key, ledger);
        }
        
        // 批量提取 managementOrg 字段（对应 EHR_CD）
        Set<String> managementOrgSet = decreaseLedgers.stream()
                .map(BudgetLedger::getMorgCode)
                .filter(StringUtils::isNotBlank)
                .collect(Collectors.toSet());
        
        // 批量查询 EHR_ORG_MANAGE_R 表，获取 EHR_CD 对应的 ORG_CD（一一对应关系）
        // Map<EHR_CD, ORG_CD>
        BudgetQueryHelperService.EhrCdToOrgCdMapResult ehrCdToOrgCdMapResult = budgetQueryHelperService.queryEhrCdToOrgCdOneToOneMap(managementOrgSet);
        Map<String, String> ehrCdToOrgCdMap = ehrCdToOrgCdMapResult.getEhrCdToOrgCdMap();
        
        // 批量查询 EHR_ORG_MANAGE_EXT_R 表，获取 EHR_CD 对应的所有 ORG_CD（一对多关系，用于控制层级聚合）
        // Map<EHR_CD, List<ORG_CD>>
        Map<String, List<String>> ehrCdToOrgCdExtMap = budgetQueryHelperService.queryEhrCdToOrgCdMap(managementOrgSet);
        // 扩展表可能无某 EHR 编码（仅一对一表有），用一对一结果补全扩展映射，便于 queryQuotaAndBalanceByAllQuartersAllDem 能解析流水上的 morgCode（如 015-044-005-001）
        if (ehrCdToOrgCdMap != null && !ehrCdToOrgCdMap.isEmpty()) {
            for (Map.Entry<String, String> e : ehrCdToOrgCdMap.entrySet()) {
                if (!ehrCdToOrgCdExtMap.containsKey(e.getKey())) {
                    ehrCdToOrgCdExtMap.put(e.getKey(), Collections.singletonList(e.getValue()));
                }
            }
        }
        
        // 批量提取 budgetSubjectCode 字段
        Set<String> budgetSubjectCodeSet = decreaseLedgers.stream()
                .map(BudgetLedger::getBudgetSubjectCode)
                .filter(StringUtils::isNotBlank)
                .filter(code -> !"NAN-NAN".equals(code))
                .collect(Collectors.toSet());
        
        // 批量查询 SUBJECT_INFO 表，获取 ERP_ACCT_CD 对应的 ACCT_CD（一一对应关系）
        // Map<ERP_ACCT_CD, ACCT_CD>
        BudgetQueryHelperService.ErpAcctCdToAcctCdMapResult erpAcctCdToAcctCdMapResult = budgetQueryHelperService.queryErpAcctCdToAcctCdOneToOneMap(budgetSubjectCodeSet);
        Map<String, String> erpAcctCdToAcctCdMap = erpAcctCdToAcctCdMapResult.getErpAcctCdToAcctCdMap();
        
        // 批量查询 SUBJECT_EXT_INFO 表，获取 ERP_ACCT_CD 对应的所有 ACCT_CD（一对多关系，用于控制层级聚合）
        // Map<ERP_ACCT_CD, List<ACCT_CD>>
        Map<String, List<String>> erpAcctCdToAcctCdExtMap = budgetQueryHelperService.queryErpAcctCdToAcctCdMap(budgetSubjectCodeSet);
        
        // 批量提取 masterProjectCode 字段（用于项目扩展映射）
        Set<String> masterProjectCodeSet = decreaseLedgers.stream()
                .map(BudgetLedger::getMasterProjectCode)
                .filter(StringUtils::isNotBlank)
                .filter(code -> !"NAN".equals(code))
                .collect(Collectors.toSet());
        
        // 批量查询项目关联映射，获取项目编码对应的所有关联项目编码（一对多关系，用于控制层级聚合）
        // Map<PRJ_CD, List<RELATED_PRJ_CD>>
        Map<String, List<String>> prjCdToRelatedPrjCdExtMap = budgetQueryHelperService.queryPrjCdToRelatedPrjCdMap(masterProjectCodeSet);
        
        // 批量提取需要映射的 erpAssetType 字段（以 "1" 或 "M" 开头且不带项目的）
        Set<String> erpAssetTypeSet = decreaseLedgers.stream()
                .filter(ledger -> {
                    String masterProjectCode = ledger.getMasterProjectCode();
                    // 只提取不带项目的明细的 erpAssetType
                    return "NAN".equals(masterProjectCode) || StringUtils.isBlank(masterProjectCode);
                })
                .map(BudgetLedger::getErpAssetType)
                .filter(StringUtils::isNotBlank)
                .filter(code -> !"NAN".equals(code))
                .filter(code -> code.startsWith("1") || code.startsWith("M")) // 只提取需要映射的
                .collect(Collectors.toSet());
            
        // 批量查询 VIEW_BUDGET_MEMBER_NAME_CODE 视图，获取 MEMBER_CD2 对应的 MEMBER_CD（一一对应关系）
        Map<String, String> erpAssetTypeToMemberCdMap = budgetQueryHelperService.queryErpAssetTypeToMemberCdMap(erpAssetTypeSet);
        log.info("========== 获取到 erpAssetType 映射表，共 {} 条记录 ==========", erpAssetTypeToMemberCdMap.size());
        
        // 2. 过滤掉不受控的ledger，只查询受控的ledger的quota和balance
        Map<String, BudgetLedger> controlledLedgerMap = new HashMap<>();
        for (Map.Entry<String, BudgetLedger> entry : ledgerMap.entrySet()) {
            BudgetLedger ledger = entry.getValue();
            // 跳过不受控的ledger
            if (!isUncontrolledLedger(ledger, ehrCdToOrgCdExtMap, erpAcctCdToAcctCdExtMap)) {
                controlledLedgerMap.put(entry.getKey(), ledger);
            }
        }
        
        // 3. 查询所有季度的 BudgetQuota 和 BudgetBalance（使用扩展映射，聚合控制层级下的所有资金池，只查询受控的ledger）
        BudgetQueryHelperService.BudgetQuotaBalanceSimpleResult result = 
                budgetQueryHelperService.queryQuotaAndBalanceByAllQuartersAllDem(
                        controlledLedgerMap, 
                        Collections.emptyMap(), // needToUpdateSameDemBudgetQuotaMap（不需要）
                        Collections.emptyMap(), // needToUpdateSameDemBudgetBalanceMap（不需要）
                        ehrCdToOrgCdMap, 
                        ehrCdToOrgCdExtMap, 
                        erpAcctCdToAcctCdMap, 
                        erpAcctCdToAcctCdExtMap, 
                        prjCdToRelatedPrjCdExtMap, 
                        erpAssetTypeToMemberCdMap);
        
        // 注意：返回的 balanceMap 是 Map<String, List<BudgetBalance>>，用于聚合控制层级下的所有资金池
        Map<String, List<BudgetBalance>> balanceListMap = result.getBalanceMap();
        Map<String, List<BudgetQuota>> quotaListMap = result.getQuotaMap();
        
        // 调减时按“当前明细对应维度”匹配资金池：有则扣在该维度上，无则先新增资金池再扣减（与调增一致）。
        // 不再使用 balanceList.get(0)，避免扣到别的维度资金池（如 801）上。
        Map<String, BudgetBalance> balanceMap = new HashMap<>();
        Map<String, BudgetQuota> quotaMap = new HashMap<>();
        
        // 3. 维护本次操作中每个 balance 和 quota 的累计变化（用于处理多条明细映射到同一个 balance/quota 的情况）
        Map<Long, BigDecimal> balanceVchangedMap = new HashMap<>(); // amountAvailable 的变更值
        Map<Long, BigDecimal> balancePayAvailableVchangedMap = new HashMap<>(); // amountPayAvailable 的变更值
        Map<Long, BigDecimal> quotaTotalVchangedMap = new HashMap<>(); // amountTotal 的变更值
        Map<Long, BigDecimal> quotaPayVchangedMap = new HashMap<>(); // amountPay 的变更值
        Map<Long, BigDecimal> quotaAvailableAdjVchangedMap = new HashMap<>(); // amountAvailableAdj 的变更值
        Map<Long, BigDecimal> quotaPayAdjVchangedMap = new HashMap<>(); // amountPayAdj 的变更值
        Set<Long> processedBalanceIds = new HashSet<>();
        Set<Long> processedQuotaIds = new HashSet<>();
        
        // 用于收集需要新增的 BudgetPoolDemR、BudgetQuota、BudgetBalance
        Map<String, BudgetPoolDemR> needToAddPoolDemRMap = new HashMap<>();
        List<BudgetQuota> needToAddQuotaList = new ArrayList<>();
        List<BudgetBalance> needToAddBalanceList = new ArrayList<>();
        
        // 4. 遍历每个调减明细，校验并扣减预算
        List<BudgetBalance> balancesToUpdate = new ArrayList<>();
        List<BudgetBalanceHistory> balanceHistories = new ArrayList<>();
        List<BudgetQuota> quotasToUpdate = new ArrayList<>();
        List<BudgetQuotaHistory> quotaHistories = new ArrayList<>();
        
        for (BudgetLedger ledger : decreaseLedgers) {
            String bizKey = ledger.getBizCode() + "@" + ledger.getBizItemCode();
            String effectType = ledger.getEffectType(); // 调整类型："0"、"1"、"2"

            // 项目非 NAN 且 isInternal=1 时跳过处理
            if (!"NAN".equals(ledger.getMasterProjectCode()) && "1".equals(ledger.getIsInternal())) {
                log.info("========== 调减处理 - 内部项目跳过: bizKey={}, masterProjectCode={}, isInternal={} ==========",
                        bizKey, ledger.getMasterProjectCode(), ledger.getIsInternal());
                continue;
            }
            
            // 判断是否不受控（需要跳过预算余额更新，但仍保存数据到BUDGET_LEDGER表）
            if (isUncontrolledLedger(ledger, ehrCdToOrgCdExtMap, erpAcctCdToAcctCdExtMap)) {
                log.info("========== 调减处理 - 不受控明细跳过预算余额更新: bizKey={}, morgCode={}, budgetSubjectCode={}, masterProjectCode={} ==========",
                        bizKey, ledger.getMorgCode(), ledger.getBudgetSubjectCode(), ledger.getMasterProjectCode());
                continue;
            }

            // effectType="1"：投资额调整，只处理 adjustAmountTotalInvestment（仅q1）
            if ("1".equals(effectType)) {
                BigDecimal adjustAmountTotalInvestment = adjustAmountTotalInvestmentMap.get(bizKey);
                if (adjustAmountTotalInvestment == null || adjustAmountTotalInvestment.compareTo(BigDecimal.ZERO) >= 0) {
                    continue; // 没有调减金额，跳过
                }
                
                BigDecimal adjustAbs = adjustAmountTotalInvestment.abs();
                String quarter = "q1";
                String bizKeyQuarter = bizKey + "@" + quarter;
                String dimensionKey = buildDimensionKey(bizKey);
                String dimensionKeyQuarter = dimensionKey + "@" + quarter;
                // 带项目时用 dimensionKeyQuarter 查找，使同项目两条明细（科目/NAN-NAN）共用同一资金池
                boolean isProjectDecrease = !"NAN".equals(ledger.getMasterProjectCode());
                String lookupKeyQuarter = isProjectDecrease ? dimensionKeyQuarter : bizKeyQuarter;
                
                // 按当前明细维度匹配资金池：有则扣在该维度上，无则新增资金池再扣减（与调增一致，不扣到别的维度）
                // 查询返回的 balanceListMap 的 key 为 bizKeyQuarter，带项目时 lookupKeyQuarter=dimensionKeyQuarter 可能查不到，需再按 bizKeyQuarter 查找，避免误创建新记录
                String[] dimension = getLedgerDimensionForDecreaseMatch(ledger, ehrCdToOrgCdMap, erpAcctCdToAcctCdMap);
                List<BudgetBalance> q1BalanceListForMatch = balanceListMap.get(lookupKeyQuarter);
                if (CollectionUtils.isEmpty(q1BalanceListForMatch)) {
                    q1BalanceListForMatch = balanceListMap.get(bizKeyQuarter);
                }
                // 资金池中存的是映射后的 erpAssetType（MEMBER_CD），匹配时用映射后的值与 balance.getErpAssetType() 比较
                String erpAssetTypeForMatch = mapErpAssetTypeForDecreaseMatch(ledger.getErpAssetType(), ledger.getMasterProjectCode(), erpAssetTypeToMemberCdMap);
                BudgetBalance balance = (dimension != null && dimension.length >= 2)
                        ? findBalanceMatchingLedgerDimension(q1BalanceListForMatch, dimension[0], dimension[1],
                                ledger.getMasterProjectCode(), erpAssetTypeForMatch)
                        : null;
                if (balance == null) {
                    // 本维度无资金池，自动创建新记录（会为所有4个季度创建），再在该资金池上扣减（允许扣为负数）
                    balance = createBudgetRecordsIfNotExists(ledger, bizKeyQuarter, quarter,
                            ehrCdToOrgCdMap, erpAcctCdToAcctCdMap, erpAssetTypeToMemberCdMap,
                            needToAddPoolDemRMap, quotaMap, balanceMap);
                    if (balance == null) {
                        balance = balanceMap.get(bizKeyQuarter);
                    }
                    if (balance == null) {
                        throw new IllegalStateException(
                                String.format("调减明细 [%s] 在季度 %s 的维度组合未找到对应的预算余额，且创建失败。" +
                                         "维度信息：年度=%s, 管理组织=%s, 预算科目=%s, 项目=%s, 资产类型=%s",
                                         bizKey, quarter, ledger.getYear(), ledger.getMorgCode(),
                                         ledger.getBudgetSubjectCode(), ledger.getMasterProjectCode(),
                                         ledger.getErpAssetType())
                        );
                    }
                    // 新创建的记录加入 balanceListMap，使校验（聚合可用预算）包含该资金池；带项目时同时放入 dimensionKeyQuarter 供同项目另一条明细复用
                    addBalanceToListMap(balanceListMap, bizKeyQuarter, balance);
                    if (isProjectDecrease) {
                        addBalanceToListMap(balanceListMap, dimensionKeyQuarter, balance);
                    }
                    // 新创建的记录，需要将所有4个季度的记录加入到新增列表
                    String[] allQuarters = {"q1", "q2", "q3", "q4"};
                    for (String q : allQuarters) {
                        String bizKeyQ = bizKey + "@" + q;
                        if (needToAddPoolDemRMap.containsKey(bizKeyQ)) {
                            BudgetQuota quota = quotaMap.get(bizKeyQ);
                            BudgetBalance bal = balanceMap.get(bizKeyQ);
                            if (quota != null && !needToAddQuotaList.contains(quota)) {
                                needToAddQuotaList.add(quota);
                            }
                            if (bal != null && !needToAddBalanceList.contains(bal)) {
                                needToAddBalanceList.add(bal);
                            }
                        }
                    }
                    // 带项目时按 dimensionKey 写入各季度，供同项目另一条明细 q2/q3/q4 直接复用，避免重复创建
                    if (isProjectDecrease) {
                        for (String q : allQuarters) {
                            String bizKeyQ = bizKey + "@" + q;
                            String dimKeyQ = dimensionKey + "@" + q;
                            BudgetBalance bal = balanceMap.get(bizKeyQ);
                            if (bal != null) {
                                balanceMap.put(dimKeyQ, bal);
                                addBalanceToListMap(balanceListMap, dimKeyQ, bal);
                            }
                        }
                    }
                }
                ledgerBizKeyToBalanceForPoolDimensionKey.putIfAbsent(bizKey, balance);

                // 校验预算是否充足（使用聚合控制层级下的所有资金池）
                // effectType="1" 只处理 q1，所以累积可用预算就是 q1 的聚合金额
                List<BudgetBalance> q1BalanceList = balanceListMap.get(bizKeyQuarter);
                BigDecimal aggregatedAmountAvailable = BigDecimal.ZERO;
                if (!CollectionUtils.isEmpty(q1BalanceList)) {
                    // 累加 q1 所有BudgetBalance的amountAvailable（如果为null则忽略）
                    for (BudgetBalance bal : q1BalanceList) {
                        BigDecimal amtAvailable = bal.getAmountAvailable();
                        if (amtAvailable != null) {
                            aggregatedAmountAvailable = aggregatedAmountAvailable.add(amtAvailable);
                        }
                    }
                }
                
                // 校验聚合后的预算是否充足（即使单个资金池为负数，只要聚合后够用就可以调减）；skipBudgetValidation 时跳过校验
                if (!skipBudgetValidation && aggregatedAmountAvailable.compareTo(adjustAbs) < 0) {
                    throw new IllegalStateException(
                        String.format("调减明细 [%s] 在季度 %s 预算不足。需要扣减=%s, 聚合可用预算=%s, 缺口=%s",
                                     bizKey, quarter, adjustAbs, aggregatedAmountAvailable, adjustAbs.subtract(aggregatedAmountAvailable))
                    );
                }

                log.info("========== 调减校验通过 Verification Passed（effectType=1）: bizKey={}, quarter={}, 扣减金额={}, 聚合可用预算={} ==========",
                        bizKey, quarter, adjustAbs, aggregatedAmountAvailable);

                // 保存 Balance 历史（每个 balance 只保存一次）
                if (!processedBalanceIds.contains(balance.getId())) {
                    BudgetBalanceHistory balanceHistory = new BudgetBalanceHistory();
                    BeanUtils.copyProperties(balance, balanceHistory);
                    balanceHistory.setId(identifierGenerator.nextId(balanceHistory).longValue());
                    balanceHistory.setBalanceId(balance.getId());
                    balanceHistory.setDeleted(Boolean.FALSE);
                    balanceHistories.add(balanceHistory);
                    processedBalanceIds.add(balance.getId());
                }

                // 获取对应的 Quota（仅q1）：优先按资金池维度从 quotaListMap 中取，否则从 create 时写入的 quotaMap 取
                BudgetQuota quota = getQuotaForBalance(balance, quotaListMap, bizKeyQuarter);
                if (quota == null) {
                    quota = quotaMap.get(bizKeyQuarter);
                }
                if (quota != null) {
                    // 保存 Quota 历史（每个 quota 只保存一次）
                    if (!processedQuotaIds.contains(quota.getId())) {
                        BudgetQuotaHistory quotaHistory = new BudgetQuotaHistory();
                        BeanUtils.copyProperties(quota, quotaHistory);
                        quotaHistory.setId(identifierGenerator.nextId(quotaHistory).longValue());
                        quotaHistory.setQuotaId(quota.getId());
                        quotaHistory.setDeleted(Boolean.FALSE);
                        quotaHistories.add(quotaHistory);
                        processedQuotaIds.add(quota.getId());
                    }
                }

                // effectType="1"：调整 quota.amountAdj 和 balance.amountAvailable
                BigDecimal currentAvailable = balance.getAmountAvailable() == null ? BigDecimal.ZERO : balance.getAmountAvailable();
                balance.setAmountAvailable(currentAvailable.subtract(adjustAbs));
                balanceVchangedMap.merge(balance.getId(), adjustAbs.negate(), BigDecimal::add);
                
                if (quota != null) {
                    BigDecimal currentAmountAdj = quota.getAmountAdj() == null ? BigDecimal.ZERO : quota.getAmountAdj();
                    quota.setAmountAdj(currentAmountAdj.subtract(adjustAbs));
                    log.info("========== 调减（effectType=1）quota.amountAdj: poolId={}, quarter={}, 调减前={}, 调减金额={}, 调减后={} ==========",
                            quota.getPoolId(), quarter, currentAmountAdj, adjustAbs, quota.getAmountAdj());
                }
                
                log.info("========== 调减（effectType=1）balance.amountAvailable: poolId={}, quarter={}, 调减前={}, 调减金额={}, 调减后={} ==========",
                        balance.getPoolId(), quarter, currentAvailable, adjustAbs, balance.getAmountAvailable());

                // 将需要更新的 balance 和 quota 加入列表（去重）
                if (!balancesToUpdate.contains(balance)) {
                    balancesToUpdate.add(balance);
                }
                if (quota != null && !quotasToUpdate.contains(quota)) {
                    quotasToUpdate.add(quota);
                }
                
                continue; // effectType="1" 处理完成，继续下一个 ledger
            }

            // effectType="0" 或 "2"：处理 adjustAmountQ1-Q4
            BigDecimal[] quarterAmounts = new BigDecimal[] {
                    ledger.getAmountConsumedQOne() == null ? BigDecimal.ZERO : ledger.getAmountConsumedQOne(),
                    ledger.getAmountConsumedQTwo() == null ? BigDecimal.ZERO : ledger.getAmountConsumedQTwo(),
                    ledger.getAmountConsumedQThree() == null ? BigDecimal.ZERO : ledger.getAmountConsumedQThree(),
                    ledger.getAmountConsumedQFour() == null ? BigDecimal.ZERO : ledger.getAmountConsumedQFour()
            };
            String[] quarters = new String[] {"q1", "q2", "q3", "q4"};

            for (int i = 0; i < quarters.length; i++) {
                BigDecimal adjustAmount = quarterAmounts[i];
                // 只有负数才视为调减；0 或 正数都跳过
                if (adjustAmount.compareTo(BigDecimal.ZERO) >= 0) {
                    continue;
                }
                BigDecimal adjustAbs = adjustAmount.abs();
                String quarter = quarters[i];
                String bizKeyQuarter = bizKey + "@" + quarter;
                String dimensionKey = buildDimensionKey(bizKey);
                String dimensionKeyQuarter = dimensionKey + "@" + quarter;
                boolean isProjectDecrease = !"NAN".equals(ledger.getMasterProjectCode());
                String lookupKeyQuarter = isProjectDecrease ? dimensionKeyQuarter : bizKeyQuarter;

                // 按当前明细维度匹配资金池：有则扣在该维度上，无则新增资金池再扣减（与调增一致，不扣到别的维度）
                // 查询返回的 balanceListMap 的 key 为 bizKeyQuarter，带项目时 lookupKeyQuarter=dimensionKeyQuarter 可能查不到，需再按 bizKeyQuarter 查找，避免误创建新记录
                String[] dimension = getLedgerDimensionForDecreaseMatch(ledger, ehrCdToOrgCdMap, erpAcctCdToAcctCdMap);
                List<BudgetBalance> balanceListForQuarter = balanceListMap.get(lookupKeyQuarter);
                if (CollectionUtils.isEmpty(balanceListForQuarter)) {
                    balanceListForQuarter = balanceListMap.get(bizKeyQuarter);
                }
                // 诊断日志：确认调减时“匹配用维度”与“查询到的资金池维度”是否一致，便于定位误创建新记录原因
                if (dimension != null && dimension.length >= 2) {
                    int listSize = balanceListForQuarter != null ? balanceListForQuarter.size() : 0;
                    log.debug("========== 调减匹配 - bizKeyQuarter={}, dimension=[{}, {}], balanceListSize={} ==========",
                            bizKeyQuarter, dimension[0], dimension[1], listSize);
                    if (listSize > 0) {
                        for (int idx = 0; idx < balanceListForQuarter.size(); idx++) {
                            BudgetBalance b = balanceListForQuarter.get(idx);
                            String balSubject = (b != null && StringUtils.isNotBlank(b.getCustomCode()) && StringUtils.isNotBlank(b.getAccountSubjectCode()))
                                    ? b.getCustomCode() + "-" + b.getAccountSubjectCode() : (b != null ? b.getAccountSubjectCode() : null);
                            log.debug("========== 调减匹配 - balance[{}] poolId={}, morgCode={}, acctSubject={}, projectCode={}, erpAssetType={} ==========",
                                    idx, b != null ? b.getPoolId() : null, b != null ? b.getMorgCode() : null, balSubject,
                                    b != null ? b.getProjectCode() : null, b != null ? b.getErpAssetType() : null);
                        }
                    }
                } else {
                    log.debug("========== 调减匹配 - bizKeyQuarter={}, dimension为null或长度<2, dimension={} ==========", bizKeyQuarter, dimension);
                }
                // 资金池中存的是映射后的 erpAssetType（MEMBER_CD），匹配时用映射后的值与 balance.getErpAssetType() 比较
                String erpAssetTypeForMatch = mapErpAssetTypeForDecreaseMatch(ledger.getErpAssetType(), ledger.getMasterProjectCode(), erpAssetTypeToMemberCdMap);
                BudgetBalance balance = (dimension != null && dimension.length >= 2)
                        ? findBalanceMatchingLedgerDimension(balanceListForQuarter, dimension[0], dimension[1],
                                ledger.getMasterProjectCode(), erpAssetTypeForMatch)
                        : null;
                if (balance == null) {
                    // 本维度无资金池，自动创建新记录（会为所有4个季度创建），再在该资金池上扣减（允许扣为负数）
                    balance = createBudgetRecordsIfNotExists(ledger, bizKeyQuarter, quarter,
                            ehrCdToOrgCdMap, erpAcctCdToAcctCdMap, erpAssetTypeToMemberCdMap,
                            needToAddPoolDemRMap, quotaMap, balanceMap);
                    if (balance == null) {
                        balance = balanceMap.get(bizKeyQuarter);
                    }
                    if (balance == null) {
                        throw new IllegalStateException(
                                String.format("调减明细 [%s] 在季度 %s 的维度组合未找到对应的预算余额，且创建失败。" +
                                         "维度信息：年度=%s, 管理组织=%s, 预算科目=%s, 项目=%s, 资产类型=%s",
                                         bizKey, quarter, ledger.getYear(), ledger.getMorgCode(),
                                         ledger.getBudgetSubjectCode(), ledger.getMasterProjectCode(),
                                         ledger.getErpAssetType())
                        );
                    }
                    // 新创建的记录加入 balanceListMap；带项目时同时放入 dimensionKeyQuarter 供同项目另一条明细复用
                    addBalanceToListMap(balanceListMap, bizKeyQuarter, balance);
                    if (isProjectDecrease) {
                        addBalanceToListMap(balanceListMap, dimensionKeyQuarter, balance);
                    }
                    // 新创建的记录，需要将所有4个季度的记录加入到新增列表
                    String[] allQuarters = {"q1", "q2", "q3", "q4"};
                    for (String q : allQuarters) {
                        String bizKeyQ = bizKey + "@" + q;
                        if (needToAddPoolDemRMap.containsKey(bizKeyQ)) {
                            BudgetQuota quota = quotaMap.get(bizKeyQ);
                            BudgetBalance bal = balanceMap.get(bizKeyQ);
                            if (quota != null && !needToAddQuotaList.contains(quota)) {
                                needToAddQuotaList.add(quota);
                            }
                            if (bal != null && !needToAddBalanceList.contains(bal)) {
                                needToAddBalanceList.add(bal);
                            }
                        }
                    }
                    // 带项目时按 dimensionKey 写入各季度，供同项目另一条明细 q2/q3/q4 直接复用，避免重复创建
                    if (isProjectDecrease) {
                        for (String q : allQuarters) {
                            String bizKeyQ = bizKey + "@" + q;
                            String dimKeyQ = dimensionKey + "@" + q;
                            BudgetBalance bal = balanceMap.get(bizKeyQ);
                            if (bal != null) {
                                balanceMap.put(dimKeyQ, bal);
                                addBalanceToListMap(balanceListMap, dimKeyQ, bal);
                            }
                        }
                    }
                }
                ledgerBizKeyToBalanceForPoolDimensionKey.putIfAbsent(bizKey, balance);

                log.info("========== 调减处理: bizKey={}, effectType={}, quarter={}, 调减金额={} ==========",
                        bizKey, effectType, quarter, adjustAbs);

                // 保存 Balance 历史（每个 balance 只保存一次）
                if (!processedBalanceIds.contains(balance.getId())) {
                    BudgetBalanceHistory balanceHistory = new BudgetBalanceHistory();
                    BeanUtils.copyProperties(balance, balanceHistory);
                    balanceHistory.setId(identifierGenerator.nextId(balanceHistory).longValue());
                    balanceHistory.setBalanceId(balance.getId());
                    balanceHistory.setDeleted(Boolean.FALSE);
                    balanceHistories.add(balanceHistory);
                    processedBalanceIds.add(balance.getId());
                }

                // 获取对应的 Quota：优先按资金池维度从 quotaListMap 中取，否则从 create 时写入的 quotaMap 取
                BudgetQuota quota = getQuotaForBalance(balance, quotaListMap, bizKeyQuarter);
                if (quota == null) {
                    quota = quotaMap.get(bizKeyQuarter);
                }
                if (quota != null) {
                    // 保存 Quota 历史（每个 quota 只保存一次）
                    if (!processedQuotaIds.contains(quota.getId())) {
                        BudgetQuotaHistory quotaHistory = new BudgetQuotaHistory();
                        BeanUtils.copyProperties(quota, quotaHistory);
                        quotaHistory.setId(identifierGenerator.nextId(quotaHistory).longValue());
                        quotaHistory.setQuotaId(quota.getId());
                        quotaHistory.setDeleted(Boolean.FALSE);
                        quotaHistories.add(quotaHistory);
                        processedQuotaIds.add(quota.getId());
                    }
                }

                // 判断维度类型
                DimensionType dimensionType = getDimensionType(bizKey);
                
                // effectType="0"：根据维度类型使用不同的字段
                if ("0".equals(effectType)) {
                    // 计算从 q1 到当前季度的累积可用预算（聚合控制层级下的所有资金池）
                    BigDecimal cumulativeAmountAvailable = calculateCumulativeAmountAvailable(bizKey, quarter, balanceListMap);
                    // 计算从 q1 到当前季度的累积扣减金额
                    BigDecimal cumulativeDecreaseAmount = calculateCumulativeDecreaseAmount(quarterAmounts, quarter);
                    
                    log.info("========== 调减校验详情: bizKey={}, quarter={}, quarterAmounts=[Q1={}, Q2={}, Q3={}, Q4={}], 累积扣减金额={}, 累积可用预算={} ==========",
                            bizKey, quarter, quarterAmounts[0], quarterAmounts[1], quarterAmounts[2], quarterAmounts[3],
                            cumulativeDecreaseAmount, cumulativeAmountAvailable);
                    
                    // 校验累积预算是否充足（使用聚合后的总金额）；skipBudgetValidation 时跳过校验
                    if (!skipBudgetValidation && cumulativeAmountAvailable.compareTo(cumulativeDecreaseAmount) < 0) {
                        throw new IllegalStateException(
                            String.format("调减明细 [%s] 从第一季度到季度 %s 累积预算不足。需要扣减=%s, 累积可用预算=%s, 缺口=%s",
                                         bizKey, quarter, cumulativeDecreaseAmount, cumulativeAmountAvailable, 
                                         cumulativeDecreaseAmount.subtract(cumulativeAmountAvailable))
                        );
                    }
                    
                    // 所有维度类型：调整 balance.amountAvailable
                    BigDecimal currentAvailable = balance.getAmountAvailable() == null ? BigDecimal.ZERO : balance.getAmountAvailable();
                    balance.setAmountAvailable(currentAvailable.subtract(adjustAbs));
                    balanceVchangedMap.merge(balance.getId(), adjustAbs.negate(), BigDecimal::add);
                    
                    if (quota != null) {
                        // 根据维度类型决定更新哪个字段
                        if (dimensionType == DimensionType.ORG_SUBJECT || dimensionType == DimensionType.PROJECT || dimensionType == DimensionType.ORG_ASSET_TYPE) {
                            // 组织+科目、项目、组织+资产类型：使用 amountAvailableAdj
                            BigDecimal currentAmountAvailableAdj = quota.getAmountAvailableAdj() == null ? BigDecimal.ZERO : quota.getAmountAvailableAdj();
                            quota.setAmountAvailableAdj(currentAmountAvailableAdj.subtract(adjustAbs));
                            quotaAvailableAdjVchangedMap.merge(quota.getId(), adjustAbs.negate(), BigDecimal::add);
                            log.info("========== 调减（effectType=0, {}）quota.amountAvailableAdj: poolId={}, quarter={}, 调减前={}, 调减金额={}, 调减后={} ==========",
                                    dimensionType, quota.getPoolId(), quarter, currentAmountAvailableAdj, adjustAbs, quota.getAmountAvailableAdj());
                        }
                    }
                    
                    log.info("========== 调减（effectType=0, {}）balance.amountAvailable: poolId={}, quarter={}, 调减前={}, 调减金额={}, 调减后={} ==========",
                            dimensionType, balance.getPoolId(), quarter, currentAvailable, adjustAbs, balance.getAmountAvailable());
                }
                // effectType="2"：根据维度类型使用不同的字段
                else if ("2".equals(effectType)) {
                    if (dimensionType == DimensionType.ORG_SUBJECT) {
                        // 组织+科目：effectType=2 也使用 amountAvailableAdj（和 effectType=0 一样）
                        // 计算从 q1 到当前季度的累积可用预算（聚合控制层级下的所有资金池）
                        BigDecimal cumulativeAmountAvailable = calculateCumulativeAmountAvailable(bizKey, quarter, balanceListMap);
                        // 计算从 q1 到当前季度的累积扣减金额
                        BigDecimal cumulativeDecreaseAmount = calculateCumulativeDecreaseAmount(quarterAmounts, quarter);
                        
                        // 校验累积预算是否充足；skipBudgetValidation 时跳过校验
                        if (!skipBudgetValidation && cumulativeAmountAvailable.compareTo(cumulativeDecreaseAmount) < 0) {
                            throw new IllegalStateException(
                                String.format("调减明细 [%s] 从第一季度到季度 %s 累积预算不足。需要扣减=%s, 累积可用预算=%s, 缺口=%s",
                                             bizKey, quarter, cumulativeDecreaseAmount, cumulativeAmountAvailable, 
                                             cumulativeDecreaseAmount.subtract(cumulativeAmountAvailable))
                            );
                        }
                        
                        BigDecimal currentAvailable = balance.getAmountAvailable() == null ? BigDecimal.ZERO : balance.getAmountAvailable();
                        balance.setAmountAvailable(currentAvailable.subtract(adjustAbs));
                        balanceVchangedMap.merge(balance.getId(), adjustAbs.negate(), BigDecimal::add);
                        
                        if (quota != null) {
                            BigDecimal currentAmountAvailableAdj = quota.getAmountAvailableAdj() == null ? BigDecimal.ZERO : quota.getAmountAvailableAdj();
                            quota.setAmountAvailableAdj(currentAmountAvailableAdj.subtract(adjustAbs));
                            quotaAvailableAdjVchangedMap.merge(quota.getId(), adjustAbs.negate(), BigDecimal::add);
                            log.info("========== 调减（effectType=2, 组织+科目）quota.amountAvailableAdj: poolId={}, quarter={}, 调减前={}, 调减金额={}, 调减后={} ==========",
                                    quota.getPoolId(), quarter, currentAmountAvailableAdj, adjustAbs, quota.getAmountAvailableAdj());
                        }
                        
                        log.info("========== 调减（effectType=2, 组织+科目）balance.amountAvailable: poolId={}, quarter={}, 调减前={}, 调减金额={}, 调减后={} ==========",
                                balance.getPoolId(), quarter, currentAvailable, adjustAbs, balance.getAmountAvailable());
                    } else {
                        // 项目、组织+资产类型：调整 balance.amountPayAvailable 和 quota.amountPayAdj
                        // 计算从 q1 到当前季度的累积可用付款预算（聚合控制层级下的所有资金池）
                        BigDecimal cumulativeAmountPayAvailable = calculateCumulativeAmountPayAvailable(bizKey, quarter, balanceListMap);
                        // 计算从 q1 到当前季度的累积扣减金额
                        BigDecimal cumulativeDecreaseAmount = calculateCumulativeDecreaseAmount(quarterAmounts, quarter);
                        
                        // 校验累积付款预算是否充足；skipBudgetValidation 时跳过校验
                        if (!skipBudgetValidation && cumulativeAmountPayAvailable.compareTo(cumulativeDecreaseAmount) < 0) {
                            throw new IllegalStateException(
                                String.format("调减明细 [%s] 从第一季度到季度 %s 累积付款预算不足。需要扣减=%s, 累积可用付款预算=%s, 缺口=%s",
                                             bizKey, quarter, cumulativeDecreaseAmount, cumulativeAmountPayAvailable, 
                                             cumulativeDecreaseAmount.subtract(cumulativeAmountPayAvailable))
                            );
                        }
                        
                        BigDecimal currentAmountPayAvailable = balance.getAmountPayAvailable() == null ? BigDecimal.ZERO : balance.getAmountPayAvailable();
                        balance.setAmountPayAvailable(currentAmountPayAvailable.subtract(adjustAbs));
                        balancePayAvailableVchangedMap.merge(balance.getId(), adjustAbs.negate(), BigDecimal::add);
                        
                        if (quota != null) {
                            BigDecimal currentAmountPayAdj = quota.getAmountPayAdj() == null ? BigDecimal.ZERO : quota.getAmountPayAdj();
                            quota.setAmountPayAdj(currentAmountPayAdj.subtract(adjustAbs));
                            quotaPayAdjVchangedMap.merge(quota.getId(), adjustAbs.negate(), BigDecimal::add);
                            log.info("========== 调减（effectType=2, {}）quota.amountPayAdj: poolId={}, quarter={}, 调减前={}, 调减金额={}, 调减后={} ==========",
                                    dimensionType, quota.getPoolId(), quarter, currentAmountPayAdj, adjustAbs, quota.getAmountPayAdj());
                        }
                        
                        log.info("========== 调减（effectType=2, {}）balance.amountPayAvailable: poolId={}, quarter={}, 调减前={}, 调减金额={}, 调减后={} ==========",
                                dimensionType, balance.getPoolId(), quarter, currentAmountPayAvailable, adjustAbs, balance.getAmountPayAvailable());
                    }
                }

                // 将需要更新的 balance 和 quota 加入列表（去重）
                if (!balancesToUpdate.contains(balance)) {
                    balancesToUpdate.add(balance);
                }
                if (quota != null && !quotasToUpdate.contains(quota)) {
                    quotasToUpdate.add(quota);
                }
            }
        }
        
        // 5. 设置每个 balance 和 quota 的本次操作累计变化量
        for (BudgetBalance balance : balancesToUpdate) {
            BigDecimal totalVchanged = balanceVchangedMap.getOrDefault(balance.getId(), BigDecimal.ZERO);
            balance.setAmountAvailableVchanged(totalVchanged);  // 调减为负数
            BigDecimal totalPayAvailableVchanged = balancePayAvailableVchangedMap.getOrDefault(balance.getId(), BigDecimal.ZERO);
            balance.setAmountPayAvailableVchanged(totalPayAvailableVchanged);  // 调减为负数
            log.info("========== Balance最终变化: poolId={}, amountAvailableVchanged={}, amountPayAvailableVchanged={} ==========",
                    balance.getPoolId(), balance.getAmountAvailableVchanged(), balance.getAmountPayAvailableVchanged());
        }
        for (BudgetQuota quota : quotasToUpdate) {
            BigDecimal totalTotalVchanged = quotaTotalVchangedMap.getOrDefault(quota.getId(), BigDecimal.ZERO);
            quota.setAmountTotalVchanged(totalTotalVchanged);  // 调减为负数（effectType=0时使用，但已改为使用新字段）
            BigDecimal totalPayVchanged = quotaPayVchangedMap.getOrDefault(quota.getId(), BigDecimal.ZERO);
            quota.setAmountPayVchanged(totalPayVchanged);  // 调减为负数（effectType=2时使用，但已改为使用新字段）
            // 注意：amountAvailableAdj、amountPayAdj 没有对应的 Vchanged 字段，直接更新值即可
            // 新字段的变化量已在上面处理时直接更新到字段值中
            log.info("========== Quota最终变化: poolId={}, amountTotalVchanged={}, amountPayVchanged={}, amountAvailableAdj={}, amountPayAdj={} ==========",
                    quota.getPoolId(), quota.getAmountTotalVchanged(), quota.getAmountPayVchanged(), 
                    quota.getAmountAvailableAdj(), quota.getAmountPayAdj());
        }
        
        // 6. 批量插入新创建的记录（必须在更新之前插入）
        if (!needToAddPoolDemRMap.isEmpty()) {
            List<BudgetPoolDemR> poolDemRsToAdd = new ArrayList<>(needToAddPoolDemRMap.values());
            for (BudgetPoolDemR poolDemR : poolDemRsToAdd) {
                budgetPoolDemRMapper.insert(poolDemR);
            }
            log.info("========== 插入 BudgetPoolDemR 完成，共 {} 条 ==========", poolDemRsToAdd.size());
        }
        if (!needToAddQuotaList.isEmpty()) {
            budgetQuotaMapper.insertBatch(needToAddQuotaList);
            log.info("========== 插入 BudgetQuota（新增）完成，共 {} 条 ==========", needToAddQuotaList.size());
        }
        if (!needToAddBalanceList.isEmpty()) {
            budgetBalanceMapper.insertBatch(needToAddBalanceList);
            log.info("========== 插入 BudgetBalance（新增）完成，共 {} 条 ==========", needToAddBalanceList.size());
        }
        
        // 7. 批量更新数据库
        if (!balanceHistories.isEmpty()) {
            budgetBalanceHistoryMapper.insertBatch(balanceHistories);
            log.info("========== 插入 BudgetBalanceHistory 完成，共 {} 条 ==========", balanceHistories.size());
        }
        if (!balancesToUpdate.isEmpty()) {
            List<BudgetBalance> sortedBalances = sortBalancesById(balancesToUpdate);
            budgetBalanceMapper.updateBatchById(sortedBalances);
            log.info("========== 更新 BudgetBalance 完成，共 {} 条 ==========", sortedBalances.size());
        }
        if (!quotaHistories.isEmpty()) {
            budgetQuotaHistoryMapper.insertBatch(quotaHistories);
            log.info("========== 插入 BudgetQuotaHistory 完成，共 {} 条 ==========", quotaHistories.size());
        }
        if (!quotasToUpdate.isEmpty()) {
            List<BudgetQuota> sortedQuotas = sortQuotasById(quotasToUpdate);
            budgetQuotaMapper.updateBatchById(sortedQuotas);
            log.info("========== 更新 BudgetQuota 完成，共 {} 条 ==========", sortedQuotas.size());
        }
        
        log.info("========== 调减处理完成 ==========");
    }

    /**
     * 将 Map<String, String> 转换为 JSON 字符串
     *
     * @param map 要转换的 Map
     * @return JSON 字符串
     */
    private String convertMapToJson(Map<String, String> map) {
        if (map == null || map.isEmpty()) {
            return null;
        }
        
        StringBuilder json = new StringBuilder("{");
        boolean first = true;
        for (Map.Entry<String, String> entry : map.entrySet()) {
            if (!first) {
                json.append(",");
            }
            first = false;
            // 转义 JSON 特殊字符
            String key = escapeJson(entry.getKey());
            String value = escapeJson(entry.getValue());
            json.append("\"").append(key).append("\":\"").append(value).append("\"");
        }
        json.append("}");
        return json.toString();
    }

    /**
     * 转义 JSON 字符串中的特殊字符
     *
     * @param str 原始字符串
     * @return 转义后的字符串
     */
    private String escapeJson(String str) {
        if (str == null) {
            return "";
        }
        return str.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    /**
     * 将 JSON 字符串转换为 Map<String, String>
     *
     * @param json JSON 字符串
     * @return Map
     */
    private Map<String, String> parseJsonToMap(String json) {
        Map<String, String> map = new HashMap<>();
        if (StringUtils.isBlank(json)) {
            return map;
        }
        // 简单的 JSON 解析（只处理简单的 key-value 对）
        json = json.trim();
        if (json.startsWith("{") && json.endsWith("}")) {
            json = json.substring(1, json.length() - 1);
            String[] pairs = json.split(",");
            for (String pair : pairs) {
                String[] kv = pair.split(":", 2);
                if (kv.length == 2) {
                    String key = kv[0].trim().replaceAll("^\"|\"$", "");
                    String value = kv[1].trim().replaceAll("^\"|\"$", "");
                    map.put(key, value);
                }
            }
        }
        return map;
    }

    /**
     * 从 metadata JSON 字符串中提取 adjustAmountTotalInvestment
     *
     * @param metadataJson metadata JSON 字符串
     * @return adjustAmountTotalInvestment，如果不存在则返回 null
     */
    private BigDecimal extractAdjustAmountTotalInvestmentFromMetadata(String metadataJson) {
        if (StringUtils.isBlank(metadataJson)) {
            return null;
        }
        Map<String, String> metadataMap = parseJsonToMap(metadataJson);
        String value = metadataMap.get("adjustAmountTotalInvestment");
        if (StringUtils.isNotBlank(value)) {
            try {
                return new BigDecimal(value);
            } catch (NumberFormatException e) {
                log.warn("无法解析 adjustAmountTotalInvestment: {}", value);
                return null;
            }
        }
        return null;
    }

    /**
     * 转换月份为季度
     */
    private String convertMonthToQuarter(String month) {
        if (StringUtils.isBlank(month)) {
            return null;
        }
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
        return null;
    }

    /**
     * 计算从 q1 到指定季度的累积可用预算（amountAvailable）
     * 参考 AbstractBudgetService.calculateTotalAmountAvailable 的逻辑，聚合控制层级下的所有资金池
     * 
     * @param bizKey 业务键
     * @param currentQuarter 当前季度（q1, q2, q3, q4）
     * @param balanceListMap 预算余额Map（key为 bizKey + "@" + quarter，value为 BudgetBalance 列表，用于聚合控制层级下的所有资金池）
     * @return 累积可用预算（聚合控制层级下所有资金池的金额）
     */
    private BigDecimal calculateCumulativeAmountAvailable(String bizKey, String currentQuarter, Map<String, List<BudgetBalance>> balanceListMap) {
        BigDecimal cumulative = BigDecimal.ZERO;
        String[] allQuarters = new String[] {"q1", "q2", "q3", "q4"};
        int currentIndex = -1;
        for (int i = 0; i < allQuarters.length; i++) {
            if (allQuarters[i].equals(currentQuarter)) {
                currentIndex = i;
                break;
            }
        }
        if (currentIndex == -1) {
            return BigDecimal.ZERO;
        }
        
        // 累加从 q1 到当前季度的所有可用预算（聚合控制层级下的所有资金池）
        for (int i = 0; i <= currentIndex; i++) {
            String quarter = allQuarters[i];
            String bizKeyQuarter = bizKey + "@" + quarter;
            List<BudgetBalance> balanceList = balanceListMap.get(bizKeyQuarter);
            if (!CollectionUtils.isEmpty(balanceList)) {
                // 累加该季度所有BudgetBalance的amountAvailable（如果为null则忽略）
                BigDecimal quarterAmountAvailable = BigDecimal.ZERO;
                for (BudgetBalance balance : balanceList) {
                    BigDecimal amountAvailable = balance.getAmountAvailable();
                    // 如果amountAvailable为null，忽略该balance；如果有数字（包括0），才累加
                    if (amountAvailable != null) {
                        quarterAmountAvailable = quarterAmountAvailable.add(amountAvailable);
                    }
                }
                cumulative = cumulative.add(quarterAmountAvailable);
                log.info("========== 调减校验 - 累积季度余额: bizKey={}, quarter={}, amountAvailable={}, 累计amountAvailable={}, balanceList.size()={} ==========",
                        bizKey, quarter, quarterAmountAvailable, cumulative, balanceList.size());
            } else {
                log.info("========== 调减校验 - 季度余额为空: bizKey={}, quarter={}, bizKeyQuarter={}, balanceList为空 ==========",
                        bizKey, quarter, bizKeyQuarter);
            }
        }
        return cumulative;
    }

    /**
     * 计算从 q1 到指定季度的累积可用付款预算（amountPayAvailable）
     * 参考 AbstractBudgetService.calculateTotalAmountAvailable 的逻辑，聚合控制层级下的所有资金池
     * 
     * @param bizKey 业务键
     * @param currentQuarter 当前季度（q1, q2, q3, q4）
     * @param balanceListMap 预算余额Map（key为 bizKey + "@" + quarter，value为 BudgetBalance 列表，用于聚合控制层级下的所有资金池）
     * @return 累积可用付款预算（聚合控制层级下所有资金池的金额）
     */
    private BigDecimal calculateCumulativeAmountPayAvailable(String bizKey, String currentQuarter, Map<String, List<BudgetBalance>> balanceListMap) {
        BigDecimal cumulative = BigDecimal.ZERO;
        String[] allQuarters = new String[] {"q1", "q2", "q3", "q4"};
        int currentIndex = -1;
        for (int i = 0; i < allQuarters.length; i++) {
            if (allQuarters[i].equals(currentQuarter)) {
                currentIndex = i;
                break;
            }
        }
        if (currentIndex == -1) {
            return BigDecimal.ZERO;
        }
        
        // 累加从 q1 到当前季度的所有可用付款预算（聚合控制层级下的所有资金池）
        for (int i = 0; i <= currentIndex; i++) {
            String quarter = allQuarters[i];
            String bizKeyQuarter = bizKey + "@" + quarter;
            List<BudgetBalance> balanceList = balanceListMap.get(bizKeyQuarter);
            if (!CollectionUtils.isEmpty(balanceList)) {
                // 累加该季度所有BudgetBalance的amountPayAvailable
                BigDecimal quarterAmountPayAvailable = BigDecimal.ZERO;
                for (BudgetBalance balance : balanceList) {
                    BigDecimal amountPayAvailable = balance.getAmountPayAvailable() == null ? BigDecimal.ZERO : balance.getAmountPayAvailable();
                    quarterAmountPayAvailable = quarterAmountPayAvailable.add(amountPayAvailable);
                }
                cumulative = cumulative.add(quarterAmountPayAvailable);
                log.debug("========== 调减校验 - 累积季度付款余额: bizKey={}, quarter={}, amountPayAvailable={}, 累计amountPayAvailable={} ==========",
                        bizKey, quarter, quarterAmountPayAvailable, cumulative);
            }
        }
        return cumulative;
    }

    /**
     * 计算当前季度的调减金额（只计算当前季度，不考虑前面季度，因为前面季度的调减已经在处理时扣减过了）
     * 
     * @param quarterAmounts 四个季度的金额数组 [q1, q2, q3, q4]
     * @param currentQuarter 当前季度（q1, q2, q3, q4）
     * @return 当前季度的调减金额（绝对值），如果当前季度不是调减则返回0
     */
    private BigDecimal calculateCumulativeDecreaseAmount(BigDecimal[] quarterAmounts, String currentQuarter) {
        String[] allQuarters = new String[] {"q1", "q2", "q3", "q4"};
        int currentIndex = -1;
        for (int i = 0; i < allQuarters.length; i++) {
            if (allQuarters[i].equals(currentQuarter)) {
                currentIndex = i;
                break;
            }
        }
        if (currentIndex == -1) {
            return BigDecimal.ZERO;
        }
        
        // 只计算当前季度的调减金额
        BigDecimal currentQuarterAmount = quarterAmounts[currentIndex];
        if (currentQuarterAmount == null || currentQuarterAmount.compareTo(BigDecimal.ZERO) >= 0) {
            // 当前季度不是调减，返回0
            log.info("========== 计算当前季度调减金额: currentQuarter={}, quarterAmount={}, 非调减，返回0 ==========",
                    currentQuarter, currentQuarterAmount);
            return BigDecimal.ZERO;
        }
        
        BigDecimal currentQuarterDecrease = currentQuarterAmount.abs(); // 当前季度的调减金额
        log.info("========== 计算当前季度调减金额: currentQuarter={}, quarterAmount={}, 调减金额={} ==========",
                currentQuarter, currentQuarterAmount, currentQuarterDecrease);
        
        return currentQuarterDecrease;
    }

    /**
     * 判断调整明细与预算流水维度是否一致。当传入 ehrCdToOrgCdMap 时，管理组织按「预算组织」比较：
     * 若请求与流水的 EHR 编码（如 015-044-005 与 015-044-005-001）映射到同一 ORG_CD，则视为同一组织。
     */
    private boolean isAdjustDimensionSame(AdjustExtDetailVo extDetail, BudgetLedger ledger, Map<String, String> ehrCdToOrgCdMap) {
        String extBudgetSubjectCode = StringUtils.isBlank(extDetail.getBudgetSubjectCode()) ? "NAN-NAN" : extDetail.getBudgetSubjectCode();
        String extMasterProjectCode = StringUtils.isBlank(extDetail.getMasterProjectCode()) ? "NAN" : extDetail.getMasterProjectCode();
        String extErpAssetType = StringUtils.isBlank(extDetail.getErpAssetType()) ? "NAN" : extDetail.getErpAssetType();
        String extIsInternal = StringUtils.isBlank(extDetail.getIsInternal()) ? "1" : extDetail.getIsInternal();
        String ledgerIsInternal = StringUtils.isBlank(ledger.getIsInternal()) ? "1" : ledger.getIsInternal();
        boolean needCheckIsInternal = !"NAN".equals(extMasterProjectCode);

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
     * 判断一条 BudgetLedger 是否为调减类型
     * effectType="0"或"2"：根据季度金额判断（任意季度金额 < 0 视为调减）
     * effectType="1"：根据 adjustAmountTotalInvestment 判断（amount < 0 视为调减）
     */
    private boolean isDecreaseLedger(BudgetLedger ledger) {
        String effectType = ledger.getEffectType();
        
        // effectType="1"：根据 adjustAmountTotalInvestment（存储在 amount 字段）判断
        if ("1".equals(effectType)) {
            BigDecimal adjustAmountTotalInvestment = ledger.getAmount();
            return adjustAmountTotalInvestment != null && adjustAmountTotalInvestment.compareTo(BigDecimal.ZERO) < 0;
        }
        
        // effectType="0"或"2"：根据季度金额判断
        BigDecimal q1 = ledger.getAmountConsumedQOne() == null ? BigDecimal.ZERO : ledger.getAmountConsumedQOne();
        BigDecimal q2 = ledger.getAmountConsumedQTwo() == null ? BigDecimal.ZERO : ledger.getAmountConsumedQTwo();
        BigDecimal q3 = ledger.getAmountConsumedQThree() == null ? BigDecimal.ZERO : ledger.getAmountConsumedQThree();
        BigDecimal q4 = ledger.getAmountConsumedQFour() == null ? BigDecimal.ZERO : ledger.getAmountConsumedQFour();
        return q1.compareTo(BigDecimal.ZERO) < 0
                || q2.compareTo(BigDecimal.ZERO) < 0
                || q3.compareTo(BigDecimal.ZERO) < 0
                || q4.compareTo(BigDecimal.ZERO) < 0;
    }

    /**
     * 判断一条 BudgetLedger 是否为调增类型
     * effectType="0"或"2"：根据季度金额判断（任意季度金额 > 0 视为调增）
     * effectType="1"：根据 adjustAmountTotalInvestment 判断（amount > 0 视为调增）
     */
    private boolean isIncreaseLedger(BudgetLedger ledger) {
        String effectType = ledger.getEffectType();
        
        // effectType="1"：根据 adjustAmountTotalInvestment（存储在 amount 字段）判断
        if ("1".equals(effectType)) {
            BigDecimal adjustAmountTotalInvestment = ledger.getAmount();
            return adjustAmountTotalInvestment != null && adjustAmountTotalInvestment.compareTo(BigDecimal.ZERO) > 0;
        }
        
        // effectType="0"或"2"：根据季度金额判断
        BigDecimal q1 = ledger.getAmountConsumedQOne() == null ? BigDecimal.ZERO : ledger.getAmountConsumedQOne();
        BigDecimal q2 = ledger.getAmountConsumedQTwo() == null ? BigDecimal.ZERO : ledger.getAmountConsumedQTwo();
        BigDecimal q3 = ledger.getAmountConsumedQThree() == null ? BigDecimal.ZERO : ledger.getAmountConsumedQThree();
        BigDecimal q4 = ledger.getAmountConsumedQFour() == null ? BigDecimal.ZERO : ledger.getAmountConsumedQFour();
        return q1.compareTo(BigDecimal.ZERO) > 0
                || q2.compareTo(BigDecimal.ZERO) > 0
                || q3.compareTo(BigDecimal.ZERO) > 0
                || q4.compareTo(BigDecimal.ZERO) > 0;
    }
    
    /**
     * 判断一条 BudgetLedger 是否不受控（需要跳过预算校验和预算余额更新，但仍保存数据到BUDGET_LEDGER表）
     * 三种不受控情况：
     * 1. 科目编码不在白名单中（不以配置前缀开头）且不是带项目的明细
     * 2. SUBJECT_EXT_INFO表查询结果包含"NAN-NAN"的科目
     * 3. EHR_ORG_MANAGE_EXT_R表查询结果包含"NAN"的组织
     * 
     * @param ledger 预算流水
     * @param ehrCdToOrgCdExtMap EHR_ORG_MANAGE_EXT_R表查询结果，Map<EHR_CD, List<ORG_CD>>
     * @param erpAcctCdToAcctCdExtMap SUBJECT_EXT_INFO表查询结果，Map<ERP_ACCT_CD, List<ACCT_CD>>
     * @return true表示不受控，false表示受控
     */
    private boolean isUncontrolledLedger(BudgetLedger ledger, 
                                         Map<String, List<String>> ehrCdToOrgCdExtMap,
                                         Map<String, List<String>> erpAcctCdToAcctCdExtMap) {
        String morgCode = ledger.getMorgCode();
        String budgetSubjectCode = ledger.getBudgetSubjectCode();
        String masterProjectCode = ledger.getMasterProjectCode();
        
        // 第三种逻辑：EHR_ORG_MANAGE_EXT_R表查询结果包含"NAN"的组织
        if (StringUtils.isNotBlank(morgCode) && ehrCdToOrgCdExtMap != null) {
            List<String> orgCdList = ehrCdToOrgCdExtMap.get(morgCode);
            if (orgCdList != null && orgCdList.contains("NAN")) {
                log.debug("========== 检测到组织编码 {} 为不受控组织（映射结果包含NAN） ==========", morgCode);
                return true;
            }
        }
        
        // 第二种逻辑：SUBJECT_EXT_INFO表查询结果包含"NAN-NAN"的科目
        if (StringUtils.isNotBlank(budgetSubjectCode) && !"NAN-NAN".equals(budgetSubjectCode) && erpAcctCdToAcctCdExtMap != null) {
            List<String> acctCdList = erpAcctCdToAcctCdExtMap.get(budgetSubjectCode);
            if (acctCdList != null && acctCdList.contains("NAN-NAN")) {
                log.debug("========== 检测到科目编码 {} 为不受控科目（映射结果包含NAN-NAN） ==========", budgetSubjectCode);
                return true;
            }
        }
        
        // 第一种逻辑：科目编码不在白名单中（不以配置前缀开头）且不是带项目的明细
        // 科目编码为空或"NAN-NAN"的情况已在第二种逻辑中处理（如果查询结果包含"NAN-NAN"）
        // 这里只处理科目编码不为空且不是"NAN-NAN"的情况
        if (StringUtils.isNotBlank(budgetSubjectCode) && !"NAN-NAN".equals(budgetSubjectCode)) {
            boolean isSubjectCodeInWhitelist = budgetSubjectCodeConfig.isInWhitelist(budgetSubjectCode);
            boolean hasProjectCode = StringUtils.isNotBlank(masterProjectCode) && !"NAN".equals(masterProjectCode);
            if (!isSubjectCodeInWhitelist && !hasProjectCode) {
                log.debug("========== 检测到科目编码 {} 不在白名单中且不带项目，为不受控科目 ==========", budgetSubjectCode);
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
     * 按id排序BudgetLedger列表，避免死锁
     * 
     * @param list 待排序的列表
     * @return 排序后的列表
     */
    private List<BudgetLedger> sortLedgersById(List<BudgetLedger> list) {
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
                .collect(Collectors.toList());
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
                .collect(Collectors.toList());
    }

    /**
     * 创建预算记录（BudgetPoolDemR、BudgetQuota、BudgetBalance）如果不存在
     * 参考 AbstractBudgetService.processSameDimensionUpdate 中的逻辑
     * 为所有4个季度创建记录
     * 
     * @param ledger 预算流水
     * @param bizKeyQuarter 业务key + 季度（格式：bizKey + "@" + quarter）
     * @param quarter 季度（q1、q2、q3、q4）- 当前需要处理的季度
     * @param ehrCdToOrgCdMap EHR组织编码到管理组织编码的映射
     * @param erpAcctCdToAcctCdMap ERP科目编码到科目编码的映射
     * @param erpAssetTypeToMemberCdMap erpAssetType 映射表
     * @param needToAddPoolDemRMap 需要新增的 BudgetPoolDemR Map（输出参数）
     * @param quotaMap 预算额度 Map（会被更新）
     * @param balanceMap 预算余额 Map（会被更新）
     * @return 当前季度对应的 BudgetBalance 对象，如果已存在则返回 null
     */
    /**
     * 调减匹配时使用的 erpAssetType：资金池表存的是映射后的 MEMBER_CD，需用映射后的值与 balance.getErpAssetType() 比较，否则会误判为无匹配而重复创建记录。
     */
    private String mapErpAssetTypeForDecreaseMatch(String originalErpAssetType, String masterProjectCode,
                                                    Map<String, String> erpAssetTypeToMemberCdMap) {
        if (originalErpAssetType == null) {
            return null;
        }
        if (erpAssetTypeToMemberCdMap != null && erpAssetTypeToMemberCdMap.containsKey(originalErpAssetType)) {
            return erpAssetTypeToMemberCdMap.get(originalErpAssetType);
        }
        return originalErpAssetType;
    }

    /**
     * 调减时获取当前明细对应的维度（管理组织、科目编码），用于在资金池列表中匹配“本维度”资金池。
     * 逻辑与 createBudgetRecordsIfNotExists 中获取 selfMorgCode、selfAcctCd 一致。
     * 带项目时 BUDGET_POOL_DEMR/BUDGET_BALANCE 中存的是 NAN、NAN-NAN，故返回 NAN/NAN-NAN 才能匹配到已有资金池，避免误创建新记录。
     */
    private String[] getLedgerDimensionForDecreaseMatch(BudgetLedger ledger,
                                                        Map<String, String> ehrCdToOrgCdMap,
                                                        Map<String, String> erpAcctCdToAcctCdMap) {
        String masterProjectCode = ledger.getMasterProjectCode();
        boolean isProjectQuery = !"NAN".equals(masterProjectCode);
        if (isProjectQuery) {
            // 带项目时资金池维度统一为 NAN、NAN-NAN，必须用其匹配，否则 findBalanceMatchingLedgerDimension 会匹配不到导致误创建
            return new String[] { "NAN", "NAN-NAN" };
        }
        String selfMorgCode = ehrCdToOrgCdMap != null ? ehrCdToOrgCdMap.get(ledger.getMorgCode()) : null;
        String selfAcctCd;
        if ("NAN-NAN".equals(ledger.getBudgetSubjectCode())) {
            selfAcctCd = "NAN-NAN";
        } else {
            selfAcctCd = erpAcctCdToAcctCdMap != null ? erpAcctCdToAcctCdMap.get(ledger.getBudgetSubjectCode()) : null;
        }
        if (StringUtils.isBlank(selfMorgCode) || StringUtils.isBlank(selfAcctCd)) {
            log.debug("========== 调减匹配维度 - 返回null: bizKey={}, selfMorgCode={}, selfAcctCd={}, ledgerMorgCode={} ==========",
                    ledger.getBizCode() + "@" + ledger.getBizItemCode(), selfMorgCode, selfAcctCd, ledger.getMorgCode());
            return null;
        }
        log.debug("========== 调减匹配维度（一对一映射）: bizKey={}, selfMorgCode={}, selfAcctCd={}, ledgerMorgCode={} ==========",
                ledger.getBizCode() + "@" + ledger.getBizItemCode(), selfMorgCode, selfAcctCd, ledger.getMorgCode());
        return new String[] { selfMorgCode, selfAcctCd };
    }

    /**
     * 在资金池列表中查找与当前明细维度一致的资金池（按 morgCode、科目、projectCode、erpAssetType 匹配）。
     * 用于调减时只扣在本维度资金池上，不扣到别的维度。
     * 带项目时（selfMorgCode=NAN, selfAcctCd=NAN-NAN）：列表已由 bizKeyQuarter(含项目编码) 过滤，只含该项目本季度的 balance，
     * 直接返回第一个即可；库里 BUDGET_BALANCE 可能存的是映射后的 morgCode/科目（如 E010102033210/CU10901-...），按字段匹配会误跳过导致重复创建。
     */
    private BudgetBalance findBalanceMatchingLedgerDimension(List<BudgetBalance> balanceList,
                                                             String selfMorgCode, String selfAcctCd,
                                                             String masterProjectCode, String erpAssetType) {
        if (CollectionUtils.isEmpty(balanceList)) {
            return null;
        }
        boolean isProjectDimension = "NAN".equals(selfMorgCode) && "NAN-NAN".equals(selfAcctCd);
        if (isProjectDimension) {
            // 项目维度：列表 key 已含该项目，表中 balance 可能为 NAN 或映射值，不再按字段匹配，直接复用第一个
            return balanceList.get(0);
        }
        for (BudgetBalance bal : balanceList) {
            if (!org.springframework.util.ObjectUtils.nullSafeEquals(bal.getMorgCode(), selfMorgCode)) {
                log.debug("========== 调减匹配 - 跳过balance: poolId={}, morgCode不一致 bal={} vs self={} ==========",
                        bal.getPoolId(), bal.getMorgCode(), selfMorgCode);
                continue;
            }
            String balSubject = StringUtils.isNotBlank(bal.getCustomCode()) && StringUtils.isNotBlank(bal.getAccountSubjectCode())
                    ? bal.getCustomCode() + "-" + bal.getAccountSubjectCode() : bal.getAccountSubjectCode();
            if (balSubject == null) {
                balSubject = "";
            }
            if (!org.springframework.util.ObjectUtils.nullSafeEquals(balSubject, selfAcctCd)) {
                log.debug("========== 调减匹配 - 跳过balance: poolId={}, acctSubject不一致 bal={} vs self={} ==========",
                        bal.getPoolId(), balSubject, selfAcctCd);
                continue;
            }
            if (!org.springframework.util.ObjectUtils.nullSafeEquals(bal.getProjectCode(), masterProjectCode)) {
                log.debug("========== 调减匹配 - 跳过balance: poolId={}, projectCode不一致 bal={} vs self={} ==========",
                        bal.getPoolId(), bal.getProjectCode(), masterProjectCode);
                continue;
            }
            if (!org.springframework.util.ObjectUtils.nullSafeEquals(bal.getErpAssetType(), erpAssetType)) {
                log.debug("========== 调减匹配 - 跳过balance: poolId={}, erpAssetType不一致 bal={} vs self={} ==========",
                        bal.getPoolId(), bal.getErpAssetType(), erpAssetType);
                continue;
            }
            log.debug("========== 调减匹配 - 匹配成功: poolId={}, morgCode={} ==========", bal.getPoolId(), bal.getMorgCode());
            return bal;
        }
        log.debug("========== 调减匹配 - 未匹配到任何balance: selfMorgCode={}, selfAcctCd={}, listSize={} ==========",
                selfMorgCode, selfAcctCd, balanceList.size());
        return null;
    }

    /**
     * 根据资金池在 quotaListMap 中查找同 poolId、同季度的 BudgetQuota。
     */
    private BudgetQuota getQuotaForBalance(BudgetBalance balance,
                                           Map<String, List<BudgetQuota>> quotaListMap,
                                           String bizKeyQuarter) {
        if (balance == null) {
            return null;
        }
        List<BudgetQuota> quotaList = quotaListMap != null ? quotaListMap.get(bizKeyQuarter) : null;
        if (CollectionUtils.isEmpty(quotaList)) {
            return null;
        }
        Long poolId = balance.getPoolId();
        for (BudgetQuota q : quotaList) {
            if (q != null && poolId != null && poolId.equals(q.getPoolId())) {
                return q;
            }
        }
        return null;
    }

    /**
     * 将新创建的资金池余额加入 balanceListMap，使后续校验（聚合可用预算）包含该资金池。
     */
    private void addBalanceToListMap(Map<String, List<BudgetBalance>> balanceListMap,
                                     String bizKeyQuarter, BudgetBalance balance) {
        if (balanceListMap == null || balance == null || StringUtils.isBlank(bizKeyQuarter)) {
            return;
        }
        List<BudgetBalance> list = balanceListMap.get(bizKeyQuarter);
        if (list != null) {
            list.add(balance);
        } else {
            list = new ArrayList<>();
            list.add(balance);
            balanceListMap.put(bizKeyQuarter, list);
        }
    }

    private BudgetBalance createBudgetRecordsIfNotExists(BudgetLedger ledger, String bizKeyQuarter, String quarter,
                                                         Map<String, String> ehrCdToOrgCdMap,
                                                         Map<String, String> erpAcctCdToAcctCdMap,
                                                         Map<String, String> erpAssetTypeToMemberCdMap,
                                                         Map<String, BudgetPoolDemR> needToAddPoolDemRMap,
                                                         Map<String, BudgetQuota> quotaMap,
                                                         Map<String, BudgetBalance> balanceMap) {
        // 提取 bizKey（不包含季度）
        String bizKey = bizKeyQuarter.substring(0, bizKeyQuarter.lastIndexOf("@"));
        
        // 检查是否已经为所有4个季度创建了记录
        String[] quarters = {"q1", "q2", "q3", "q4"};
        boolean allQuartersExist = true;
        for (String q : quarters) {
            String bizKeyQ = bizKey + "@" + q;
            if (!balanceMap.containsKey(bizKeyQ)) {
                allQuartersExist = false;
                break;
            }
        }
        
        // 如果所有季度都已存在，直接返回当前季度的 balance
        if (allQuartersExist) {
            return balanceMap.get(bizKeyQuarter);
        }
        
        log.warn("========== 预算调整：查询不到预算维度，自动创建新记录（所有4个季度）: bizKey={} ==========", bizKey);
        
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
        // 带项目的预算在 BUDGET_BALANCE/POOL/QUOTA 中不存科目维度，统一使用 NAN-NAN；非项目才按 ledger 或映射取科目
        String selfAcctCd;
        if ("NAN-NAN".equals(ledger.getBudgetSubjectCode())) {
            selfAcctCd = "NAN-NAN";
        } else if (isProjectQuery) {
            // 带项目的单据：新增项目预算时科目维度必须为空，只建一套维度
            selfAcctCd = "NAN-NAN";
        } else {
            // 非项目查询：必须要有映射
            selfAcctCd = erpAcctCdToAcctCdMap != null ? erpAcctCdToAcctCdMap.get(ledger.getBudgetSubjectCode()) : null;
        }
        
        if (StringUtils.isBlank(selfMorgCode) || StringUtils.isBlank(selfAcctCd)) {
            log.error("========== 无法获取维度映射信息: selfMorgCode={}, selfAcctCd={} ==========", selfMorgCode, selfAcctCd);
            throw new IllegalStateException(
                String.format("调增明细 [%s] 无法获取维度映射信息,还请联系管理员维护映射。[%s][%s][%s] Invalid Dimension",
                             ledger.getBizCode() + "@" + ledger.getBizItemCode(), 
                             ledger.getMorgCode(), ledger.getBudgetSubjectCode(), ledger.getMasterProjectCode())
            );
        }
        
        String year = ledger.getYear();
        // masterProjectCode 已在上面声明，这里不需要重复声明
        String originalErpAssetType = ledger.getErpAssetType();
        // 映射 erpAssetType：如果以 "1" 或 "M" 开头，需要通过映射表映射
        // 注意：带项目时不需要映射 erpAssetType
        String erpAssetType = mapErpAssetType(originalErpAssetType, masterProjectCode, erpAssetTypeToMemberCdMap, 
                "创建 BudgetPoolDemR 时，预算调整明细 [" + ledger.getBizCode() + "@" + ledger.getBizItemCode() + "]");
        // 当项目是 NAN 时，isInternal 必须是 1
        String isInternal = "NAN".equals(masterProjectCode) ? "1" : ledger.getIsInternal();
        String currency = ledger.getCurrency() != null ? ledger.getCurrency() : "CNY";
        String version = ledger.getVersion() != null ? ledger.getVersion() : String.valueOf(identifierGenerator.nextId(null));
        
        // 为所有4个季度创建记录
        BudgetBalance currentQuarterBalance = null;
        for (String q : quarters) {
            String bizKeyQ = bizKey + "@" + q;
            
            // 如果已经存在，跳过
            if (balanceMap.containsKey(bizKeyQ)) {
                if (q.equals(quarter)) {
                    currentQuarterBalance = balanceMap.get(bizKeyQ);
                }
                continue;
            }
            
            // 创建 BudgetPoolDemR
            BudgetPoolDemR poolDemR = new BudgetPoolDemR();
            Long poolId = identifierGenerator.nextId(poolDemR).longValue();
            poolDemR.setId(poolId);
            poolDemR.setYear(year);
            poolDemR.setQuarter(q);
            poolDemR.setIsInternal(isInternal);
            poolDemR.setMorgCode(selfMorgCode);
            poolDemR.setBudgetSubjectCode(selfAcctCd);
            poolDemR.setMasterProjectCode(masterProjectCode);
            // 注意：PROJECT_ID 暂时不设置，后续可以通过 syncQuotaDataFromOriginal 的自动补充逻辑填充
            // 如果需要立即设置，可以通过 masterProjectCode 从 SYSTEM_PROJECT_BUDGET 查询 PROJECT_ID
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
            quota.setQuarter(q);
            quota.setCurrency(currency);
            quota.setVersion(version);
            quota.setAmountTotal(BigDecimal.ZERO);
            quota.setAmountTotalVchanged(BigDecimal.ZERO);
            quota.setAmountAdj(BigDecimal.ZERO);
            quota.setAmountPay(BigDecimal.ZERO);
            quota.setAmountPayVchanged(BigDecimal.ZERO);
            quota.setAmountAvailableAdj(BigDecimal.ZERO);
            quota.setAmountPayAdj(BigDecimal.ZERO);
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
            newBalance.setQuarter(q);
            newBalance.setCurrency(currency);
            newBalance.setVersion(version);
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
            
            // 将数据放入 Map
            needToAddPoolDemRMap.put(bizKeyQ, poolDemR);
            quotaMap.put(bizKeyQ, quota);
            balanceMap.put(bizKeyQ, newBalance);
            
            // 如果是当前季度，保存引用
            if (q.equals(quarter)) {
                currentQuarterBalance = newBalance;
            }
            
            log.info("========== 预算调整：成功创建新记录: bizKeyQuarter={}, poolId={}, quotaId={}, balanceId={} ==========",
                    bizKeyQ, poolId, quotaId, balanceId);
        }
        
        log.info("========== 预算调整：成功创建所有4个季度的记录: bizKey={} ==========", bizKey);
        
        return currentQuarterBalance;
    }
    
    /**
     * 映射 erpAssetType（如果以 "1" 或 "M" 开头，需要通过映射表映射）
     * 参考 AbstractBudgetService.mapErpAssetType 的逻辑
     * 
     * @param originalErpAssetType 原始的 erpAssetType（映射前的值）
     * @param masterProjectCode 主项目编码，如果不为 "NAN" 则带项目，不需要映射 erpAssetType
     * @param erpAssetTypeToMemberCdMap 映射表（MEMBER_CD2 -> MEMBER_CD），从 VIEW_BUDGET_MEMBER_NAME_CODE 视图获取
     * @param errorContext 错误上下文信息（用于错误提示）
     * @return 映射后的 erpAssetType（如果不需要映射或映射不到，返回原值或 "NAN"）
     * @throws IllegalArgumentException 如果需要映射但映射不到时抛出异常
     */
    private String mapErpAssetType(String originalErpAssetType, String masterProjectCode, 
                                   Map<String, String> erpAssetTypeToMemberCdMap, String errorContext) {
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
     * 维度类型枚举
     */
    private enum DimensionType {
        ORG_SUBJECT,      // 组织+科目
        PROJECT,          // 项目
        ORG_ASSET_TYPE   // 组织+资产类型
    }

    /**
     * 从 bizKey 构建“业务维度 key”（不区分 effectType），用于同一维度下 effectType=1 与 effectType=2 共用一套 pool/quota/balance。
     * bizKey 格式：bizCode@adjustYear@all@effectType@isInternal@managementOrg@budgetSubjectCode@masterProjectCode@erpAssetType
     * 将 parts[3]（effectType）统一为 "0"，得到 dimensionKey。
     * 带项目时（masterProjectCode 非 NAN）：parts[6]（budgetSubjectCode）统一为 "NAN-NAN"，保证同一项目只对应一套预算维度，BUDGET_BALANCE 不出现科目维度。
     *
     * @param bizKey 业务 key（bizCode + "@" + bizItemCode）
     * @return 维度 key，若格式不符合则返回原 bizKey
     */
    private String buildDimensionKey(String bizKey) {
        if (StringUtils.isBlank(bizKey)) {
            return bizKey;
        }
        String[] parts = bizKey.split("@");
        if (parts.length < 9) {
            return bizKey;
        }
        // parts[3] 为 effectType，统一为 "0" 使同一业务维度共用一套记录
        parts[3] = "0";
        // 带项目时科目维度统一为 NAN-NAN，同一项目只一套预算，BUDGET_BALANCE 不存科目
        if (parts.length > 7 && !"NAN".equals(parts[7])) {
            parts[6] = "NAN-NAN";
        }
        return String.join("@", parts);
    }

    /**
     * 判断bizKey的维度类型
     * bizKey格式：bizCode@bizItemCode，其中 bizItemCode = adjustDetailLineNo = adjustYear@all@effectType@isInternal@managementOrg@budgetSubjectCode@masterProjectCode@erpAssetType
     * 所以完整格式为：bizCode@adjustYear@all@effectType@isInternal@managementOrg@budgetSubjectCode@masterProjectCode@erpAssetType
     * 
     * @param bizKey 业务key
     * @return 维度类型
     */
    private DimensionType getDimensionType(String bizKey) {
        if (StringUtils.isBlank(bizKey)) {
            // 默认返回组织+科目
            return DimensionType.ORG_SUBJECT;
        }
        
        // 解析bizKey
        String[] parts = bizKey.split("@");
        if (parts.length < 9) {
            // 格式不对，默认返回组织+科目（至少需要9个部分：bizCode + adjustDetailLineNo的8个部分）
            return DimensionType.ORG_SUBJECT;
        }
        
        // parts[0] = bizCode
        // parts[1] = adjustYear
        // parts[2] = "all"
        // parts[3] = effectType
        // parts[4] = isInternal
        // parts[5] = managementOrg
        // parts[6] = budgetSubjectCode
        // parts[7] = masterProjectCode
        // parts[8] = erpAssetType
        String masterProjectCode = parts.length > 7 ? parts[7] : "NAN";
        String erpAssetType = parts.length > 8 ? parts[8] : "NAN";
        
        // 判断维度类型
        boolean hasProject = !"NAN".equals(masterProjectCode) && StringUtils.isNotBlank(masterProjectCode);
        boolean hasAssetType = !"NAN".equals(erpAssetType) && StringUtils.isNotBlank(erpAssetType);
        
        if (hasProject) {
            // 带项目：项目维度
            return DimensionType.PROJECT;
        } else if (hasAssetType) {
            // 不带项目但带资产类型：组织+资产类型维度
            return DimensionType.ORG_ASSET_TYPE;
        } else {
            // 不带项目且不带资产类型：组织+科目维度
            return DimensionType.ORG_SUBJECT;
        }
    }

}


