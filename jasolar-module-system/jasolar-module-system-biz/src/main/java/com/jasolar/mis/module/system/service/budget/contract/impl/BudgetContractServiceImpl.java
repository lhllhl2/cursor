package com.jasolar.mis.module.system.service.budget.contract.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.incrementer.IdentifierGenerator;
import com.jasolar.mis.module.system.controller.budget.vo.BudgetContractApplyParams;
import com.jasolar.mis.module.system.controller.budget.vo.BudgetContractRenewParams;
import com.jasolar.mis.module.system.controller.budget.vo.BudgetContractRenewRespVo;
import com.jasolar.mis.module.system.controller.budget.vo.BudgetContractRespVo;
import com.jasolar.mis.module.system.controller.budget.vo.BudgetParams;
import com.jasolar.mis.module.system.controller.budget.vo.BudgetRespVo;
import com.jasolar.mis.module.system.controller.budget.vo.ContractRenewReqInfoParams;
import com.jasolar.mis.module.system.controller.budget.vo.ContractApplyDetailVo;
import com.jasolar.mis.module.system.controller.budget.vo.ContractApplyReqInfoParams;
import com.jasolar.mis.module.system.controller.budget.vo.ContractApplyResultInfoRespVo;
import com.jasolar.mis.module.system.controller.budget.vo.ContractDetailDetailVo;
import com.jasolar.mis.module.system.controller.budget.vo.ContractDetailRespVo;
import com.jasolar.mis.module.system.controller.budget.vo.DetailDetailVo;
import com.jasolar.mis.module.system.controller.budget.vo.ReqInfoParams;
import com.jasolar.mis.module.system.controller.budget.vo.SubDetailVo;
import com.jasolar.mis.module.system.controller.budget.vo.ContractExtDetailVo;
import com.jasolar.mis.module.system.controller.budget.vo.ContractApplyExtDetailVo;
import com.jasolar.mis.module.system.controller.budget.vo.ExtDetailVo;
import com.jasolar.mis.module.system.controller.budget.vo.ESBInfoParams;
import com.jasolar.mis.module.system.controller.budget.vo.ESBRespInfoVo;
import com.jasolar.mis.module.system.domain.budget.BudgetBalance;
import com.jasolar.mis.module.system.domain.budget.BudgetBalanceHistory;
import com.jasolar.mis.module.system.domain.budget.BudgetLedger;
import com.jasolar.mis.module.system.domain.budget.BudgetLedgerCompositeKey;
import com.jasolar.mis.module.system.domain.budget.BudgetLedgerHead;
import com.jasolar.mis.module.system.domain.budget.BudgetLedgerHeadHistory;
import com.jasolar.mis.module.system.domain.budget.BudgetLedgerHistory;
import com.jasolar.mis.module.system.domain.budget.BudgetLedgerSelfR;
import com.jasolar.mis.module.system.domain.budget.BudgetLedgerSelfRHistory;
import com.jasolar.mis.module.system.domain.budget.BudgetPoolDemR;
import com.jasolar.mis.module.system.domain.budget.BudgetQuota;
import com.jasolar.mis.module.system.domain.budget.BudgetQuotaHistory;
import com.jasolar.mis.module.system.domain.budget.SystemProjectBudget;
import com.jasolar.mis.module.system.mapper.budget.BudgetBalanceMapper;
import com.jasolar.mis.module.system.mapper.budget.BudgetBalanceHistoryMapper;
import com.jasolar.mis.module.system.mapper.budget.BudgetLedgerHeadMapper;
import com.jasolar.mis.module.system.mapper.budget.BudgetLedgerHeadHistoryMapper;
import com.jasolar.mis.module.system.mapper.budget.BudgetLedgerMapper;
import com.jasolar.mis.module.system.mapper.budget.BudgetLedgerHistoryMapper;
import com.jasolar.mis.module.system.mapper.budget.BudgetLedgerSelfRHistoryMapper;
import com.jasolar.mis.module.system.mapper.budget.BudgetLedgerSelfRMapper;
import com.jasolar.mis.module.system.mapper.budget.BudgetPoolDemRMapper;
import com.jasolar.mis.module.system.mapper.budget.BudgetQuotaMapper;
import com.jasolar.mis.module.system.mapper.budget.BudgetQuotaHistoryMapper;
import com.jasolar.mis.module.system.mapper.budget.SystemProjectBudgetMapper;
import com.jasolar.mis.module.system.service.budget.AbstractBudgetService;
import com.jasolar.mis.module.system.service.budget.contract.BudgetContractService;
import com.jasolar.mis.module.system.service.budget.exception.DetailValidationException;
import com.jasolar.mis.module.system.service.budget.helper.BudgetQueryHelperService;
import com.jasolar.mis.module.system.config.BudgetSubjectCodeConfig;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
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
 * 预算合同 Service 实现
 *
 * @author Auto Generated
 * @date 2025-01-XX
 */
@Service
@Slf4j
public class BudgetContractServiceImpl extends AbstractBudgetService implements BudgetContractService {

    private static final String INITIAL_SUBMITTED = "INITIAL_SUBMITTED";
    private static final String DEFAULT_BIZ_TYPE = "CONTRACT";
    private static final String APPLY_BIZ_TYPE = "APPLY";
    private static final String PROJECT_SKIP_BUDGET_SUBJECT_CODE = "000000";
    private static final String DEFAULT_CURRENCY = "CNY";
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
    private static final DateTimeFormatter PROCESS_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private boolean isCurrentYear(String year) {
        return StringUtils.isNotBlank(year) && String.valueOf(LocalDate.now().getYear()).equals(StringUtils.trim(year));
    }

    private String extractContractYear(List<ContractDetailDetailVo> contractDetails) {
        if (CollectionUtils.isEmpty(contractDetails)) {
            return null;
        }
        for (ContractDetailDetailVo detail : contractDetails) {
            if (detail != null && StringUtils.isNotBlank(detail.getContractYear())) {
                return StringUtils.trim(detail.getContractYear());
            }
        }
        return null;
    }

    private String resolveRenewYear(String contractNo, ContractRenewReqInfoParams renewInfo) {
        if (StringUtils.isBlank(contractNo)) {
            return null;
        }
        LambdaQueryWrapper<BudgetLedger> ledgerWrapper = new LambdaQueryWrapper<>();
        ledgerWrapper.eq(BudgetLedger::getBizCode, contractNo)
                .eq(BudgetLedger::getBizType, DEFAULT_BIZ_TYPE)
                .eq(BudgetLedger::getDeleted, Boolean.FALSE);
        List<BudgetLedger> ledgers = budgetLedgerMapper.selectList(ledgerWrapper);
        if (CollectionUtils.isEmpty(ledgers)) {
            return null;
        }
        for (BudgetLedger ledger : ledgers) {
            if (ledger != null && StringUtils.isNotBlank(ledger.getYear())) {
                return StringUtils.trim(ledger.getYear());
            }
        }
        return null;
    }

    private String resolveRenewSuccessMessage(String documentStatus) {
        if ("APPROVED".equals(documentStatus)) {
            return "预算合同审批成功";
        }
        if ("REJECTED".equals(documentStatus)) {
            return "预算合同驳回成功";
        }
        if ("CANCELLED".equals(documentStatus)) {
            return "预算合同撤销成功";
        }
        if ("CLOSED".equals(documentStatus)) {
            return "预算合同关闭成功";
        }
        return "预算合同处理成功";
    }

    private boolean isProjectSkipBudgetDetail(String budgetSubjectCode, String masterProjectCode) {
        return PROJECT_SKIP_BUDGET_SUBJECT_CODE.equals(StringUtils.trim(budgetSubjectCode))
                && StringUtils.isNotBlank(masterProjectCode)
                && !"NAN".equals(StringUtils.trim(masterProjectCode));
    }

    private boolean isProjectSkipBudgetLedger(BudgetLedger ledger) {
        return ledger != null && isProjectSkipBudgetDetail(ledger.getBudgetSubjectCode(), ledger.getMasterProjectCode());
    }

    private void resetLedgerConsumedFields(BudgetLedger ledger, BigDecimal amount) {
        BigDecimal safe = amount == null ? BigDecimal.ZERO : amount;
        ledger.setAmount(safe);
        ledger.setAmountAvailable(safe);
        ledger.setAmountConsumedQOne(BigDecimal.ZERO);
        ledger.setAmountConsumedQTwo(BigDecimal.ZERO);
        ledger.setAmountConsumedQThree(BigDecimal.ZERO);
        ledger.setAmountConsumedQFour(BigDecimal.ZERO);
    }

    private void archiveAndDeleteContractLedgers(Set<Long> ledgerIds) {
        if (CollectionUtils.isEmpty(ledgerIds)) {
            return;
        }
        budgetLedgerMapper.deleteByIds(new ArrayList<>(ledgerIds));
        int deletedByIdCount = budgetLedgerSelfRMapper.deleteByIdsAndBizType(ledgerIds, APPLY_BIZ_TYPE);
        if (deletedByIdCount > 0) {
            log.info("========== 删除关联的预算流水关系（ID IN）: 删除了 {} 条关系记录 ==========", deletedByIdCount);
        }
        LambdaQueryWrapper<BudgetLedgerSelfR> deleteWrapper = new LambdaQueryWrapper<>();
        deleteWrapper.in(BudgetLedgerSelfR::getRelatedId, ledgerIds)
                .eq(BudgetLedgerSelfR::getBizType, APPLY_BIZ_TYPE);
        int deletedByRelatedIdCount = budgetLedgerSelfRMapper.delete(deleteWrapper);
        if (deletedByRelatedIdCount > 0) {
            log.info("========== 删除关联的预算流水关系（RELATED_ID IN）: 删除了 {} 条关系记录 ==========", deletedByRelatedIdCount);
        }
    }

    private BudgetContractRespVo handleApplyCrossYearNoDeduction(BudgetContractApplyParams params,
                                                                  ContractApplyReqInfoParams applyInfo) {
        BudgetParams budgetParams = convertToBudgetParams(params);
        ReqInfoParams reqInfo = budgetParams.getReqInfo();
        LocalDateTime requestTime = BudgetQueryHelperService.parseEsbRequestTime(
                params.getEsbInfo() != null ? params.getEsbInfo().getRequestTime() : null);

        List<DetailDetailVo> detailList = reqInfo.getDetails() == null ? Collections.emptyList() : reqInfo.getDetails();
        List<BudgetLedger> existingLedgers = budgetLedgerMapper.selectList(
                new LambdaQueryWrapper<BudgetLedger>()
                        .eq(BudgetLedger::getBizType, DEFAULT_BIZ_TYPE)
                        .eq(BudgetLedger::getBizCode, reqInfo.getDocumentNo())
                        .eq(BudgetLedger::getDeleted, Boolean.FALSE)
        );
        Map<String, BudgetLedger> existingMap = existingLedgers.stream()
                .collect(Collectors.toMap(l -> l.getBizCode() + "@" + l.getBizItemCode(), Function.identity(), (a, b) -> a));
        Set<String> retainedKeys = detailList.stream()
                .map(d -> reqInfo.getDocumentNo() + "@" + d.getDetailLineNo())
                .collect(Collectors.toSet());

        List<BudgetLedger> toDeleteLedgers = existingLedgers.stream()
                .filter(ledger -> !retainedKeys.contains(ledger.getBizCode() + "@" + ledger.getBizItemCode()))
                .collect(Collectors.toList());
        if (!toDeleteLedgers.isEmpty()) {
            List<BudgetLedgerHistory> deleteHistories = new ArrayList<>();
            Set<Long> deleteIds = new HashSet<>();
            for (BudgetLedger ledger : toDeleteLedgers) {
                BudgetLedgerHistory history = new BudgetLedgerHistory();
                BeanUtils.copyProperties(ledger, history);
                history.setId(identifierGenerator.nextId(history).longValue());
                history.setLedgerId(ledger.getId());
                history.setDeleted(Boolean.FALSE);
                deleteHistories.add(history);
                deleteIds.add(ledger.getId());
            }
            budgetLedgerHistoryMapper.insertBatch(deleteHistories);
            archiveAndDeleteContractLedgers(deleteIds);
        }

        List<BudgetLedgerHistory> updateHistories = new ArrayList<>();
        List<BudgetLedger> needUpdate = new ArrayList<>();
        List<BudgetLedger> needInsert = new ArrayList<>();
        Map<String, String> detailValidationResultMap = new HashMap<>();
        Map<String, String> detailValidationMessageMap = new HashMap<>();
        for (DetailDetailVo detail : reqInfo.getDetails()) {
            String key = reqInfo.getDocumentNo() + "@" + detail.getDetailLineNo();
            BudgetLedger existing = existingMap.get(key);
            if (existing != null) {
                BudgetLedgerHistory history = new BudgetLedgerHistory();
                BeanUtils.copyProperties(existing, history);
                history.setId(identifierGenerator.nextId(history).longValue());
                history.setLedgerId(existing.getId());
                history.setDeleted(Boolean.FALSE);
                updateHistories.add(history);

                existing.setYear(detail.getYear());
                existing.setMonth(detail.getMonth());
                existing.setIsInternal(detail.getIsInternal());
                existing.setMorgCode(detail.getManagementOrg());
                existing.setBudgetSubjectCode(StringUtils.defaultIfBlank(detail.getBudgetSubjectCode(), "NAN-NAN"));
                existing.setMasterProjectCode(StringUtils.defaultIfBlank(detail.getMasterProjectCode(), "NAN"));
                existing.setErpAssetType(StringUtils.defaultIfBlank(detail.getErpAssetType(), "NAN"));
                existing.setCurrency(StringUtils.defaultIfBlank(detail.getCurrency(), DEFAULT_CURRENCY));
                existing.setMetadata(detail.getMetadata());
                resetLedgerConsumedFields(existing, detail.getAmount());
                existing.setVersionPre(existing.getVersion());
                existing.setVersion(String.valueOf(identifierGenerator.nextId(null)));
                existing.setOperator(reqInfo.getOperator());
                existing.setOperatorNo(reqInfo.getOperatorNo());
                if (requestTime != null) {
                    existing.setUpdateTime(requestTime);
                }
                needUpdate.add(existing);
            } else {
                Long id = identifierGenerator.nextId(null).longValue();
                BudgetLedger ledger = budgetQueryHelperService.createBudgetLedger(
                        id, DEFAULT_BIZ_TYPE, reqInfo.getDocumentNo(), detail.getDetailLineNo(),
                        detail.getYear(), detail.getMonth(), detail.getIsInternal(), detail.getManagementOrg(),
                        detail.getBudgetSubjectCode(), detail.getMasterProjectCode(), detail.getErpAssetType(),
                        StringUtils.defaultIfBlank(detail.getCurrency(), DEFAULT_CURRENCY),
                        detail.getAmount() == null ? BigDecimal.ZERO : detail.getAmount(),
                        null, identifierGenerator, reqInfo.getOperator(), reqInfo.getOperatorNo(), requestTime);
                ledger.setMetadata(detail.getMetadata());
                resetLedgerConsumedFields(ledger, detail.getAmount());
                needInsert.add(ledger);
            }
            detailValidationResultMap.put(detail.getDetailLineNo(), "0");
            detailValidationMessageMap.put(detail.getDetailLineNo(), "处理成功");
        }

        if (!updateHistories.isEmpty()) {
            budgetLedgerHistoryMapper.insertBatch(updateHistories);
        }
        if (!needUpdate.isEmpty()) {
            budgetLedgerMapper.updateBatchById(sortLedgersById(needUpdate));
        }
        if (!needInsert.isEmpty()) {
            budgetLedgerMapper.insertBatch(needInsert);
        }

        budgetQueryHelperService.createOrUpdateBudgetLedgerHead(reqInfo.getDocumentNo(), DEFAULT_BIZ_TYPE,
                reqInfo.getDocumentName(), reqInfo.getDataSource(), reqInfo.getProcessName(), "SUBMITTED",
                identifierGenerator, reqInfo.getOperator(), reqInfo.getOperatorNo(), requestTime);

        return buildResponseWithDetailErrors(params, detailValidationResultMap, detailValidationMessageMap);
    }

    private BudgetContractRespVo handleDetailDeletedCrossYearNoRollback(BudgetContractApplyParams params,
                                                                         ContractApplyReqInfoParams applyInfo) {
        BudgetParams budgetParams = convertToBudgetParams(params);
        ReqInfoParams reqInfo = budgetParams.getReqInfo();
        LocalDateTime requestTime = BudgetQueryHelperService.parseEsbRequestTime(
                params.getEsbInfo() != null ? params.getEsbInfo().getRequestTime() : null);

        LambdaQueryWrapper<BudgetLedger> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BudgetLedger::getBizCode, reqInfo.getDocumentNo())
                .eq(BudgetLedger::getBizType, DEFAULT_BIZ_TYPE)
                .eq(BudgetLedger::getDeleted, Boolean.FALSE);
        List<BudgetLedger> allLedgers = budgetLedgerMapper.selectList(wrapper);
        Map<String, BudgetLedger> ledgerByLine = allLedgers.stream()
                .collect(Collectors.toMap(BudgetLedger::getBizItemCode, Function.identity(), (a, b) -> a));

        List<BudgetLedgerHistory> histories = new ArrayList<>();
        Set<Long> deleteIds = new HashSet<>();
        Map<String, String> detailValidationResultMap = new HashMap<>();
        Map<String, String> detailValidationMessageMap = new HashMap<>();
        for (DetailDetailVo detail : reqInfo.getDetails()) {
            BudgetLedger ledger = ledgerByLine.get(detail.getDetailLineNo());
            if (ledger == null) {
                detailValidationResultMap.put(detail.getDetailLineNo(), "1");
                detailValidationMessageMap.put(detail.getDetailLineNo(), "未找到可删除的明细");
                continue;
            }
            BudgetLedgerHistory history = new BudgetLedgerHistory();
            BeanUtils.copyProperties(ledger, history);
            history.setId(identifierGenerator.nextId(history).longValue());
            history.setLedgerId(ledger.getId());
            history.setDeleted(Boolean.FALSE);
            histories.add(history);
            ledger.setOperator(reqInfo.getOperator());
            ledger.setOperatorNo(reqInfo.getOperatorNo());
            if (requestTime != null) {
                ledger.setUpdateTime(requestTime);
            }
            budgetLedgerMapper.updateById(ledger);
            deleteIds.add(ledger.getId());
            detailValidationResultMap.put(detail.getDetailLineNo(), "0");
            detailValidationMessageMap.put(detail.getDetailLineNo(), "明细删除成功");
        }

        if (!histories.isEmpty()) {
            budgetLedgerHistoryMapper.insertBatch(histories);
        }
        archiveAndDeleteContractLedgers(deleteIds);

        LambdaQueryWrapper<BudgetLedger> remainedWrapper = new LambdaQueryWrapper<>();
        remainedWrapper.eq(BudgetLedger::getBizCode, reqInfo.getDocumentNo())
                .eq(BudgetLedger::getBizType, DEFAULT_BIZ_TYPE)
                .eq(BudgetLedger::getDeleted, Boolean.FALSE);
        Long remainedCount = budgetLedgerMapper.selectCount(remainedWrapper);
        if (remainedCount != null && remainedCount == 0L) {
            LambdaQueryWrapper<BudgetLedgerHead> headWrapper = new LambdaQueryWrapper<>();
            headWrapper.eq(BudgetLedgerHead::getBizCode, reqInfo.getDocumentNo())
                    .eq(BudgetLedgerHead::getBizType, DEFAULT_BIZ_TYPE)
                    .eq(BudgetLedgerHead::getDeleted, Boolean.FALSE);
            BudgetLedgerHead head = budgetLedgerHeadMapper.selectOne(headWrapper);
            if (head != null) {
                BudgetLedgerHeadHistory headHistory = new BudgetLedgerHeadHistory();
                BeanUtils.copyProperties(head, headHistory);
                headHistory.setId(identifierGenerator.nextId(headHistory).longValue());
                headHistory.setLedgerHeadId(head.getId());
                headHistory.setDeleted(Boolean.FALSE);
                budgetLedgerHeadHistoryMapper.insertBatch(Collections.singletonList(headHistory));
                budgetLedgerHeadMapper.deleteById(head.getId());
            }
        }
        return buildResponseWithDetailErrors(params, detailValidationResultMap, detailValidationMessageMap);
    }

    private BudgetContractRenewRespVo handleRejectedOrCancelledSkipRollback(String contractNo, String documentStatus,
                                                                            BudgetContractRenewParams params) {
        LambdaQueryWrapper<BudgetLedgerHead> headWrapper = new LambdaQueryWrapper<>();
        headWrapper.eq(BudgetLedgerHead::getBizCode, contractNo)
                .eq(BudgetLedgerHead::getDeleted, Boolean.FALSE);
        BudgetLedgerHead existingHead = budgetLedgerHeadMapper.selectOne(headWrapper);
        if (existingHead == null) {
            throw new IllegalStateException("未找到对应的预算流水头，业务单号：" + contractNo);
        }

        LambdaQueryWrapper<BudgetLedger> ledgerWrapper = new LambdaQueryWrapper<>();
        ledgerWrapper.eq(BudgetLedger::getBizCode, contractNo)
                .eq(BudgetLedger::getBizType, DEFAULT_BIZ_TYPE)
                .eq(BudgetLedger::getDeleted, Boolean.FALSE);
        List<BudgetLedger> allLedgers = budgetLedgerMapper.selectList(ledgerWrapper);
        if (!allLedgers.isEmpty()) {
            List<BudgetLedgerHistory> histories = new ArrayList<>();
            Set<Long> deleteIds = new HashSet<>();
            for (BudgetLedger ledger : allLedgers) {
                BudgetLedgerHistory history = new BudgetLedgerHistory();
                BeanUtils.copyProperties(ledger, history);
                history.setId(identifierGenerator.nextId(history).longValue());
                history.setLedgerId(ledger.getId());
                history.setDeleted(Boolean.FALSE);
                histories.add(history);
                deleteIds.add(ledger.getId());
            }
            budgetLedgerHistoryMapper.insertBatch(histories);
            archiveAndDeleteContractLedgers(deleteIds);
        }

        BudgetLedgerHeadHistory headHistory = new BudgetLedgerHeadHistory();
        BeanUtils.copyProperties(existingHead, headHistory);
        headHistory.setId(identifierGenerator.nextId(headHistory).longValue());
        headHistory.setLedgerHeadId(existingHead.getId());
        headHistory.setDeleted(Boolean.FALSE);
        budgetLedgerHeadHistoryMapper.insertBatch(Collections.singletonList(headHistory));
        budgetLedgerHeadMapper.deleteById(existingHead.getId());
        return buildSuccessResponse(params, resolveRenewSuccessMessage(documentStatus));
    }

    @Resource
    private BudgetLedgerHeadMapper budgetLedgerHeadMapper;

    @Resource
    private BudgetLedgerHeadHistoryMapper budgetLedgerHeadHistoryMapper;
    @Resource
    private SystemProjectBudgetMapper systemProjectBudgetMapper;

    @Resource
    private BudgetLedgerMapper budgetLedgerMapper;

    @Resource
    private BudgetQuotaMapper budgetQuotaMapper;

    @Resource
    private BudgetBalanceMapper budgetBalanceMapper;

    @Resource
    private BudgetPoolDemRMapper budgetPoolDemRMapper;

    @Resource
    private BudgetBalanceHistoryMapper budgetBalanceHistoryMapper;

    @Resource
    private BudgetQuotaHistoryMapper budgetQuotaHistoryMapper;

    @Resource
    private BudgetLedgerHistoryMapper budgetLedgerHistoryMapper;

    @Resource
    private BudgetLedgerSelfRMapper budgetLedgerSelfRMapper;

    @Resource
    private BudgetLedgerSelfRHistoryMapper budgetLedgerSelfRHistoryMapper;

    @Resource
    private BudgetQueryHelperService budgetQueryHelperService;
    
    @Resource
    private BudgetSubjectCodeConfig budgetSubjectCodeConfig;

    @Resource
    private IdentifierGenerator identifierGenerator;

    /**
     * 处理采购合同的申请操作
     *
     * @param budgetContractApplyParams 预算合同申请参数
     * @return 预算合同响应
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public BudgetContractRespVo apply(BudgetContractApplyParams budgetContractApplyParams) {
        log.info("开始处理预算合同申请，params={}", budgetContractApplyParams);

        try {
            // 整单级别校验：校验申请单状态
            ContractApplyReqInfoParams contractApplyReqInfo = budgetContractApplyParams.getContractApplyReqInfo();
            if (contractApplyReqInfo == null) {
                throw new IllegalArgumentException("合同申请信息不能为空");
            }
            // DETAIL_DELETED 分支不走“初始提交”校验（允许已审批通过后按明细删除）
            // 兼容历史调用可能传入的 "DETAIL-DELETED"
            String documentStatus = contractApplyReqInfo.getDocumentStatus();
            boolean isDetailDeleted = "DETAIL_DELETED".equals(documentStatus)
                    || "DETAIL-DELETED".equalsIgnoreCase(documentStatus);
            String contractYear = extractContractYear(contractApplyReqInfo.getContractDetails());
            if (isDetailDeleted && StringUtils.isNotBlank(contractYear) && !isCurrentYear(contractYear)) {
                log.info("预算合同明细删除跨年跳过预算回滚，contractNo={}, year={}, currentYear={}",
                        contractApplyReqInfo.getContractNo(), contractYear, LocalDate.now().getYear());
                return handleDetailDeletedCrossYearNoRollback(budgetContractApplyParams, contractApplyReqInfo);
            }
            if (!isDetailDeleted) {
                if (!INITIAL_SUBMITTED.equals(documentStatus)) {
                    throw new IllegalArgumentException("申请单状态必须为 INITIAL_SUBMITTED，当前状态：" + documentStatus);
                }
                // 校验：已审批通过的合同申请单不允许再次提交
                if (StringUtils.isNotBlank(contractApplyReqInfo.getContractNo())) {
                    LambdaQueryWrapper<BudgetLedgerHead> headCheckWrapper = new LambdaQueryWrapper<>();
                    headCheckWrapper.eq(BudgetLedgerHead::getBizCode, contractApplyReqInfo.getContractNo())
                            .eq(BudgetLedgerHead::getBizType, DEFAULT_BIZ_TYPE)
                            .eq(BudgetLedgerHead::getDeleted, Boolean.FALSE);
                    BudgetLedgerHead existingHead = budgetLedgerHeadMapper.selectOne(headCheckWrapper);
                    if (existingHead != null && "APPROVED".equals(existingHead.getStatus())) {
                        throw new IllegalArgumentException("合同申请单已审批通过，不允许再次提交，单号：" + contractApplyReqInfo.getContractNo());
                    }
                }
                if (StringUtils.isNotBlank(contractYear) && !isCurrentYear(contractYear)) {
                    log.info("预算合同跨年提交跳过预算校验与扣减，contractNo={}, year={}, currentYear={}",
                            contractApplyReqInfo.getContractNo(), contractYear, LocalDate.now().getYear());
                    return handleApplyCrossYearNoDeduction(budgetContractApplyParams, contractApplyReqInfo);
                }
            }

            BudgetParams budgetParams = convertToBudgetParams(budgetContractApplyParams);
            
            // 调用父类通用处理逻辑
            BudgetRespVo respVo = superApply(budgetParams, DEFAULT_BIZ_TYPE);
            
            // 将通用的 BudgetRespVo 转换为 BudgetContractRespVo
            return convertToBudgetContractRespVo(respVo);
        } catch (DetailValidationException e) {
            // 明细级别的错误（由父类superApply抛出）
            log.error("部分明细处理失败", e);
            return buildResponseWithDetailErrors(budgetContractApplyParams, 
                    e.getDetailValidationResultMap(), e.getDetailValidationMessageMap());
        } catch (IllegalStateException e) {
            // 业务状态错误，作为整单错误处理
            log.error("预算合同申请处理失败", e);
            return buildErrorResponseForAllDetails(budgetContractApplyParams, e);
        } catch (Exception e) {
            // 其他异常，作为整单错误处理
            log.error("预算合同申请处理失败", e);
            return buildErrorResponseForAllDetails(budgetContractApplyParams, e);
        }
    }

    /**
     * 将 BudgetContractApplyParams 转换为 BudgetParams
     *
     * @param budgetContractApplyParams 预算合同申请参数
     * @return BudgetParams 预算参数
     */
    private BudgetParams convertToBudgetParams(BudgetContractApplyParams budgetContractApplyParams) {
        BudgetParams budgetParams = new BudgetParams();
        budgetParams.setEsbInfo(budgetContractApplyParams.getEsbInfo());

        ContractApplyReqInfoParams contractApplyReqInfo = budgetContractApplyParams.getContractApplyReqInfo();
        ReqInfoParams reqInfo = new ReqInfoParams();
        
        // 字段映射：contractNo -> documentNo, contractAnnualAmount -> totalBudgetAmount
        reqInfo.setDocumentNo(contractApplyReqInfo.getContractNo());
        reqInfo.setTotalBudgetAmount(contractApplyReqInfo.getContractAnnualAmount());
        
        // 直接复制的字段
        BeanUtils.copyProperties(contractApplyReqInfo, reqInfo, "contractNo", "contractAnnualAmount", "contractDetails");

        // 转换 contractDetails 为 details
        List<DetailDetailVo> details = new ArrayList<>();
        if (contractApplyReqInfo.getContractDetails() != null) {
            for (ContractDetailDetailVo contractDetail : contractApplyReqInfo.getContractDetails()) {
                DetailDetailVo detail = convertContractDetailToDetail(contractDetail);
                details.add(detail);
            }
        }
        reqInfo.setDetails(details);
        budgetParams.setReqInfo(reqInfo);
        
        return budgetParams;
    }

    /**
     * 将 ContractDetailDetailVo 转换为 DetailDetailVo
     *
     * @param contractDetail 合同明细
     * @return DetailDetailVo 明细信息
     */
    private DetailDetailVo convertContractDetailToDetail(ContractDetailDetailVo contractDetail) {
        DetailDetailVo detail = new DetailDetailVo();
        
        // 字段映射：contractDetailLineNo -> detailLineNo, contractYear -> year, 
        // contractMonth -> month, contractAmount -> amount
        detail.setDetailLineNo(contractDetail.getContractDetailLineNo());
        detail.setYear(contractDetail.getContractYear());
        detail.setMonth(contractDetail.getContractMonth());
        detail.setAmount(contractDetail.getContractAmount());
        
        // 直接复制的字段（包括 managementOrg, budgetSubjectCode, masterProjectCode, erpAssetType, isInternal 等）
        BeanUtils.copyProperties(contractDetail, detail, "contractDetailLineNo", "contractYear", 
                "contractMonth", "contractAmount", "contractApplyDetails", "metadata");

        // 转换 metadata: Map<String, String> -> JSON 字符串
        if (contractDetail.getMetadata() != null && !contractDetail.getMetadata().isEmpty()) {
            String metadataJson = convertMapToJson(contractDetail.getMetadata());
            detail.setMetadata(metadataJson);
        }

        // 转换 contractApplyDetails 为 applyDetails
        List<SubDetailVo> applyDetails = new ArrayList<>();
        if (contractDetail.getContractApplyDetails() != null) {
            for (ContractApplyDetailVo contractApplyDetail : contractDetail.getContractApplyDetails()) {
                SubDetailVo subDetail = new SubDetailVo();
                subDetail.setDocumentNo(contractApplyDetail.getDemandOrderNo());
                subDetail.setDetailLineNo(contractApplyDetail.getDemandDetailLineNo());
                applyDetails.add(subDetail);
            }
        }
        detail.setApplyDetails(applyDetails);
        
        return detail;
    }

    /**
     * 将 DetailDetailVo 转换为 ContractDetailDetailVo
     *
     * @param detail 明细信息
     * @return ContractDetailDetailVo 合同明细
     */
    private ContractDetailDetailVo convertDetailToContractDetail(DetailDetailVo detail) {
        ContractDetailDetailVo contractDetail = new ContractDetailDetailVo();
        
        // 字段映射：detailLineNo -> contractDetailLineNo, year -> contractYear, 
        // month -> contractMonth, amount -> contractAmount
        contractDetail.setContractDetailLineNo(detail.getDetailLineNo());
        contractDetail.setContractYear(detail.getYear());
        contractDetail.setContractMonth(detail.getMonth());
        contractDetail.setContractAmount(detail.getAmount());
        
        // 直接复制的字段（包括 managementOrg, budgetSubjectCode, masterProjectCode, erpAssetType 等）
        BeanUtils.copyProperties(detail, contractDetail, "detailLineNo", "year", 
                "month", "amount", "applyDetails", "contractDetails", "metadata");

        // 转换 metadata: JSON 字符串 -> Map<String, String>
        if (StringUtils.isNotBlank(detail.getMetadata())) {
            Map<String, String> metadataMap = parseJsonToMap(detail.getMetadata());
            contractDetail.setMetadata(metadataMap);
        }

        // 转换 applyDetails 为 contractApplyDetails
        List<ContractApplyDetailVo> contractApplyDetails = new ArrayList<>();
        if (detail.getApplyDetails() != null) {
            for (SubDetailVo subDetail : detail.getApplyDetails()) {
                ContractApplyDetailVo contractApplyDetail = new ContractApplyDetailVo();
                contractApplyDetail.setDemandOrderNo(subDetail.getDocumentNo());
                // demandDetailLineNo 会自动从父级计算，这里不需要设置
                // 设置父级引用，以便自动计算 demandDetailLineNo
                contractApplyDetail.setParentDetail(contractDetail);
                contractApplyDetails.add(contractApplyDetail);
            }
        }
        contractDetail.setContractApplyDetails(contractApplyDetails);
        
        return contractDetail;
    }

    /**
     * 将 BudgetRespVo 转换为 BudgetContractRespVo
     *
     * @param respVo 预算响应VO
     * @return BudgetContractRespVo 预算合同响应VO
     */
    private BudgetContractRespVo convertToBudgetContractRespVo(BudgetRespVo respVo) {
        BudgetContractRespVo contractRespVo = new BudgetContractRespVo();
        contractRespVo.setEsbInfo(respVo.getEsbInfo());
        
        // 转换 resultInfo 为 contractApplyResult
        if (respVo.getResultInfo() != null) {
            ContractApplyResultInfoRespVo contractApplyResult = new ContractApplyResultInfoRespVo();
            contractApplyResult.setContractNo(respVo.getResultInfo().getDocumentNo());
            contractApplyResult.setProcessTime(respVo.getResultInfo().getProcessTime());
            
            // 转换 details 为 contractDetails（使用ContractDetailRespVo）
            List<ContractDetailRespVo> contractDetails = new ArrayList<>();
            if (respVo.getResultInfo().getDetails() != null) {
                for (DetailDetailVo detail : respVo.getResultInfo().getDetails()) {
                    ContractDetailRespVo contractDetail = new ContractDetailRespVo();
                    ContractDetailDetailVo baseDetail = convertDetailToContractDetail(detail);
                    
                    // 先复制基础字段（排除预算相关字段，因为这些字段在 ContractDetailDetailVo 中不存在）
                    BeanUtils.copyProperties(baseDetail, contractDetail);
                    
                    // 设置明细级别的校验结果
                    contractDetail.setValidationResult(detail.getValidationResult());
                    contractDetail.setValidationMessage(detail.getValidationMessage());
                    
                    // 设置预算相关数值字段（这些字段只在 ContractDetailRespVo 中定义，不在 ContractDetailDetailVo 中）
                    BigDecimal availableBudgetRatio = detail.getAvailableBudgetRatio();
                    BigDecimal amountQuota = detail.getAmountQuota();
                    BigDecimal amountFrozen = detail.getAmountFrozen();
                    BigDecimal amountActual = detail.getAmountActual();
                    BigDecimal amountAvailable = detail.getAmountAvailable();
                    
                    log.info("========== buildResponse - 从 detail 获取预算相关数值字段: detailLineNo={}, availableBudgetRatio={}, amountQuota={}, amountFrozen={}, amountActual={}, amountAvailable={} ==========",
                            detail.getDetailLineNo(), availableBudgetRatio, amountQuota, amountFrozen, amountActual, amountAvailable);
                    
                    contractDetail.setAvailableBudgetRatio(availableBudgetRatio);
                    contractDetail.setAmountQuota(amountQuota);
                    contractDetail.setAmountFrozen(amountFrozen);
                    contractDetail.setAmountActual(amountActual);
                    contractDetail.setAmountAvailable(amountAvailable);
                    
                    // 验证设置后的值
                    log.info("========== buildResponse - 设置后验证 contractDetail 的预算相关数值字段: detailLineNo={}, availableBudgetRatio={}, amountQuota={}, amountFrozen={}, amountActual={}, amountAvailable={} ==========",
                            detail.getDetailLineNo(), contractDetail.getAvailableBudgetRatio(), contractDetail.getAmountQuota(), 
                            contractDetail.getAmountFrozen(), contractDetail.getAmountActual(), contractDetail.getAmountAvailable());
                    
                    // 在添加到列表前再次验证（确保值没有被覆盖）
                    log.info("========== buildResponse - 添加到列表前验证 contractDetail: detailLineNo={}, availableBudgetRatio={}, amountQuota={}, amountFrozen={}, amountActual={}, amountAvailable={}, contractDetail.toString()={} ==========",
                            detail.getDetailLineNo(), contractDetail.getAvailableBudgetRatio(), contractDetail.getAmountQuota(), 
                            contractDetail.getAmountFrozen(), contractDetail.getAmountActual(), contractDetail.getAmountAvailable(), contractDetail.toString());
                    
                    contractDetails.add(contractDetail);
                }
            }
            contractApplyResult.setContractDetails(contractDetails);
            
            // 在设置到响应对象前验证
            log.info("========== buildResponse - 设置到响应对象前验证: contractDetails.size()={} ==========", contractDetails.size());
            if (!contractDetails.isEmpty()) {
                ContractDetailRespVo firstDetail = contractDetails.get(0);
                log.info("========== buildResponse - 第一个 contractDetail 的预算相关数值字段: availableBudgetRatio={}, amountQuota={}, amountFrozen={}, amountActual={}, amountAvailable={} ==========",
                        firstDetail.getAvailableBudgetRatio(), firstDetail.getAmountQuota(), 
                        firstDetail.getAmountFrozen(), firstDetail.getAmountActual(), firstDetail.getAmountAvailable());
            }
            
            contractRespVo.setContractApplyResult(contractApplyResult);
        }
        
        return contractRespVo;
    }

    @Override
    protected List<BudgetLedger> queryExistingLedgers(List<ExtDetailVo> extDetailsForQuery, String type) {
        // 只处理 CONTRACT 类型的查询
        if (!DEFAULT_BIZ_TYPE.equals(type)) {
            return Collections.emptyList();
        }
        
        // 将 ExtDetailVo 转换为 ContractExtDetailVo
        List<ContractExtDetailVo> contractExtDetailsForQuery = new ArrayList<>();
        for (ExtDetailVo extDetail : extDetailsForQuery) {
            ContractExtDetailVo contractExtDetail = new ContractExtDetailVo();
            // 字段映射
            contractExtDetail.setContractNo(extDetail.getDocumentNo());
            contractExtDetail.setContractDetailLineNo(extDetail.getDetailLineNo());
            contractExtDetail.setContractYear(extDetail.getYear());
            contractExtDetail.setContractMonth(extDetail.getMonth());
            contractExtDetail.setCompany(extDetail.getCompany());
            contractExtDetail.setDepartment(extDetail.getDepartment());
            contractExtDetail.setManagementOrg(extDetail.getManagementOrg());
            contractExtDetail.setBudgetSubjectCode(extDetail.getBudgetSubjectCode());
            contractExtDetail.setBudgetSubjectName(extDetail.getBudgetSubjectName());
            contractExtDetail.setMasterProjectCode(extDetail.getMasterProjectCode());
            contractExtDetail.setMasterProjectName(extDetail.getMasterProjectName());
            contractExtDetail.setErpAssetType(extDetail.getErpAssetType());
            contractExtDetail.setContractAmount(extDetail.getAmount());
            contractExtDetail.setCurrency(extDetail.getCurrency());
            contractExtDetailsForQuery.add(contractExtDetail);
        }
        
        // 查询预算流水
        return budgetLedgerMapper.selectByContractExtDetails(contractExtDetailsForQuery);
    }

    @Override
    protected boolean shouldSkipProjectMappingValidation(DetailDetailVo detail) {
        if (detail == null) {
            return false;
        }
        return isProjectSkipBudgetDetail(detail.getBudgetSubjectCode(), detail.getMasterProjectCode());
    }

    @Override
    protected boolean isUncontrolledLedger(BudgetLedger ledger,
                                           Map<String, List<String>> ehrCdToOrgCdExtMap,
                                           Map<String, List<String>> erpAcctCdToAcctCdExtMap) {
        if (isProjectSkipBudgetLedger(ledger)) {
            log.info("预算合同命中科目000000且带项目，按不受控处理并跳过预算占用/回滚，bizKey={}@{}",
                    ledger.getBizCode(), ledger.getBizItemCode());
            return true;
        }
        return super.isUncontrolledLedger(ledger, ehrCdToOrgCdExtMap, erpAcctCdToAcctCdExtMap);
    }

    @Override
    protected void rollbackBalanceAmountForDiffDimension(BudgetBalance balance, BudgetBalanceHistory balanceHistory,
                                                          BudgetLedger ledger, String rollbackQuarter, 
                                                          BigDecimal rollbackAmount, Long poolId) {
        BigDecimal amountOccupiedBefore = balance.getAmountOccupied() == null ? BigDecimal.ZERO : balance.getAmountOccupied();
        BigDecimal amountAvailableBefore = balance.getAmountAvailable() == null ? BigDecimal.ZERO : balance.getAmountAvailable();
        
        log.info("========== processDiffDimensionRollback - 释放占用金额: poolId={}, bizItemCode={}, quarter={}, rollbackAmount={}, 释放前 amountOccupied={}, amountAvailable={} ==========",
                poolId, ledger.getBizItemCode(), rollbackQuarter, rollbackAmount, amountOccupiedBefore, amountAvailableBefore);
        
        // 维度不一致的回滚，不能扣减为负数，如果回滚金额大于当前占用金额，则最多回滚当前占用金额
        BigDecimal actualRollbackAmount = rollbackAmount;
        if (rollbackAmount.compareTo(amountOccupiedBefore) > 0) {
            actualRollbackAmount = amountOccupiedBefore;
            log.warn("========== processDiffDimensionRollback - 回滚金额大于当前占用金额，调整为当前占用金额: poolId={}, bizItemCode={}, quarter={}, 原回滚金额={}, 调整后回滚金额={} ==========",
                    poolId, ledger.getBizItemCode(), rollbackQuarter, rollbackAmount, actualRollbackAmount);
        }
        
        BigDecimal newAmountOccupied = amountOccupiedBefore.subtract(actualRollbackAmount);
        balance.setAmountOccupied(newAmountOccupied);
        balance.setAmountOccupiedVchanged(actualRollbackAmount.negate());
        // 释放占用后，金额直接回到可用金额
        balance.setAmountAvailable(amountAvailableBefore.add(actualRollbackAmount));
        balance.setAmountAvailableVchanged(actualRollbackAmount);
    }

    @Override
    protected Map<Long, BigDecimal> getRollbackAmountsForRelatedLedgers(BudgetLedger ledger, String rollbackQuarter,
                                                                        BigDecimal rollbackAmount,
                                                                        Map<String, List<BudgetLedger>> relatedBudgetLedgerMap) {
        String bizKey = ledger.getBizCode() + "@" + ledger.getBizItemCode();
        List<BudgetLedger> relatedLedgers = relatedBudgetLedgerMap != null ? relatedBudgetLedgerMap.get(bizKey) : null;
        if (CollectionUtils.isEmpty(relatedLedgers)) {
            return Collections.emptyMap();
        }
        // 合同单关联申请单(APPLY)
        Set<Long> relatedLedgerIds = relatedLedgers.stream().map(BudgetLedger::getId).filter(Objects::nonNull).collect(Collectors.toSet());
        List<BudgetLedgerSelfR> selfRList = budgetLedgerSelfRMapper.selectByRelatedIdsAndBizType(relatedLedgerIds, "APPLY");
        Map<Long, BudgetLedgerSelfR> selfRMap = selfRList.stream()
                .filter(selfR -> selfR.getId() != null && selfR.getRelatedId() != null)
                .filter(selfR -> selfR.getId().equals(ledger.getId()))
                .collect(Collectors.toMap(BudgetLedgerSelfR::getRelatedId, Function.identity(), (v1, v2) -> v1));
        Map<Long, BigDecimal> out = new HashMap<>();
        for (BudgetLedger relatedLedger : relatedLedgers) {
            if (relatedLedger.getId() == null) continue;
            BudgetLedgerSelfR selfR = selfRMap.get(relatedLedger.getId());
            BigDecimal amount = BigDecimal.ZERO;
            if (selfR != null) {
                switch (rollbackQuarter) {
                    case "q1": amount = selfR.getAmountConsumedQOne() != null ? selfR.getAmountConsumedQOne() : BigDecimal.ZERO; break;
                    case "q2": amount = selfR.getAmountConsumedQTwo() != null ? selfR.getAmountConsumedQTwo() : BigDecimal.ZERO; break;
                    case "q3": amount = selfR.getAmountConsumedQThree() != null ? selfR.getAmountConsumedQThree() : BigDecimal.ZERO; break;
                    case "q4": amount = selfR.getAmountConsumedQFour() != null ? selfR.getAmountConsumedQFour() : BigDecimal.ZERO; break;
                    default: break;
                }
            } else {
                BigDecimal total = relatedLedgers.stream().map(l -> l.getAmountAvailable() != null ? l.getAmountAvailable() : BigDecimal.ZERO).reduce(BigDecimal.ZERO, BigDecimal::add);
                if (total.compareTo(BigDecimal.ZERO) > 0) {
                    BigDecimal av = relatedLedger.getAmountAvailable() != null ? relatedLedger.getAmountAvailable() : BigDecimal.ZERO;
                    amount = rollbackAmount.multiply(av.divide(total, 10, RoundingMode.HALF_UP)).setScale(2, RoundingMode.HALF_UP);
                } else {
                    amount = rollbackAmount.divide(BigDecimal.valueOf(relatedLedgers.size()), 2, RoundingMode.HALF_UP);
                }
            }
            if (amount.compareTo(BigDecimal.ZERO) > 0) {
                out.put(relatedLedger.getId(), amount);
            }
        }
        return out;
    }

    @Override
    protected Map<String, List<BudgetLedger>> rollbackBalanceAmountForSameDimension(BudgetBalance balance, BudgetBalanceHistory balanceHistory,
                                                        BudgetLedger ledger, String rollbackQuarter, 
                                                        BigDecimal rollbackAmount, Long poolId,
                                                        Map<String, List<BudgetLedger>> relatedBudgetLedgerMap,
                                                        Map<Long, Map<String, BigDecimal>> aggregatedRelatedRollbackMap,
                                                        Set<String> appliedRelatedLedgerQuarterSet) {
        // 通过 ledger 的 bizCode + "@" + bizItemCode 去 relatedBudgetLedgerMap 获取对应的集合数据
        String bizKey = ledger.getBizCode() + "@" + ledger.getBizItemCode();
        List<BudgetLedger> relatedLedgers = relatedBudgetLedgerMap != null ? relatedBudgetLedgerMap.get(bizKey) : null;
        
        BigDecimal amountOccupiedBefore = balance.getAmountOccupied() == null ? BigDecimal.ZERO : balance.getAmountOccupied();
        BigDecimal amountAvailableBefore = balance.getAmountAvailable() == null ? BigDecimal.ZERO : balance.getAmountAvailable();
        
        if (CollectionUtils.isEmpty(relatedLedgers)) {
            // 如果获取到的集合数据为空，则获取 amountOccupiedBefore 和 amountAvailableBefore
            // amountOccupiedBefore 直接扣减 rollbackAmount
            // amountAvailableBefore 直接加 rollbackAmount
            log.info("========== processSameDimensionRollback - 释放占用金额（无关联流水）: poolId={}, bizItemCode={}, quarter={}, rollbackAmount={}, 释放前 amountOccupied={}, amountAvailable={} ==========",
                    poolId, ledger.getBizItemCode(), rollbackQuarter, rollbackAmount, amountOccupiedBefore, amountAvailableBefore);
            
            balance.setAmountOccupied(amountOccupiedBefore.subtract(rollbackAmount));
            balance.setAmountOccupiedVchanged(rollbackAmount.negate());
            balance.setAmountAvailable(amountAvailableBefore.add(rollbackAmount));
            balance.setAmountAvailableVchanged(rollbackAmount);
        } else {
            // 如果不为空，amountOccupiedBefore 直接扣减 rollbackAmount
            // amountFrozenBefore 加 rollbackAmount
            BigDecimal amountFrozenBefore = balance.getAmountFrozen() == null ? BigDecimal.ZERO : balance.getAmountFrozen();
            
            log.info("========== processSameDimensionRollback - 释放占用金额（有关联流水）: poolId={}, bizItemCode={}, quarter={}, rollbackAmount={}, 释放前 amountOccupied={}, amountFrozen={}, amountAvailable={}, 关联流水数量={} ==========",
                    poolId, ledger.getBizItemCode(), rollbackQuarter, rollbackAmount, amountOccupiedBefore, amountFrozenBefore, amountAvailableBefore, relatedLedgers.size());
            
            balance.setAmountOccupied(amountOccupiedBefore.subtract(rollbackAmount));
            balance.setAmountOccupiedVchanged(rollbackAmount.negate());
            balance.setAmountFrozen(amountFrozenBefore.add(rollbackAmount));
            balance.setAmountFrozenVchanged(rollbackAmount);
            
            // 使用 BUDGET_LEDGER_SELF_R 记录的金额精确回滚每个关联单据
            // 查询 BUDGET_LEDGER_SELF_R 表，获取每个关联单据的扣减金额（按季度）
            Set<Long> relatedLedgerIds = relatedLedgers.stream()
                    .map(BudgetLedger::getId)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());
            
            List<BudgetLedgerSelfR> selfRList = budgetLedgerSelfRMapper.selectByRelatedIdsAndBizType(relatedLedgerIds, "APPLY");
            Map<Long, BudgetLedgerSelfR> selfRMap = selfRList.stream()
                    .filter(selfR -> selfR.getId() != null && selfR.getRelatedId() != null)
                    .filter(selfR -> selfR.getId().equals(ledger.getId())) // 只取当前单据的记录
                    .collect(Collectors.toMap(BudgetLedgerSelfR::getRelatedId, Function.identity(), (v1, v2) -> v1));
            
            // 遍历所有关联单据，使用记录的金额精确回滚（若已汇总则用汇总金额且每个关联流水本季度只施加一次）
            for (BudgetLedger relatedLedger : relatedLedgers) {
                if (relatedLedger.getId() == null) {
                    continue;
                }
                BigDecimal amountToRollback;
                if (aggregatedRelatedRollbackMap != null && appliedRelatedLedgerQuarterSet != null) {
                    String appliedKey = relatedLedger.getId() + "@" + rollbackQuarter;
                    BigDecimal aggregatedAmount = aggregatedRelatedRollbackMap.getOrDefault(relatedLedger.getId(), Collections.emptyMap()).get(rollbackQuarter);
                    if (aggregatedAmount == null || aggregatedAmount.compareTo(BigDecimal.ZERO) <= 0 || appliedRelatedLedgerQuarterSet.contains(appliedKey)) {
                        continue;
                    }
                    appliedRelatedLedgerQuarterSet.add(appliedKey);
                    amountToRollback = aggregatedAmount;
                } else {
                    BudgetLedgerSelfR selfR = selfRMap.get(relatedLedger.getId());
                    amountToRollback = BigDecimal.ZERO;
                    if (selfR != null) {
                        switch (rollbackQuarter) {
                            case "q1":
                                amountToRollback = selfR.getAmountConsumedQOne() != null ? selfR.getAmountConsumedQOne() : BigDecimal.ZERO;
                                break;
                            case "q2":
                                amountToRollback = selfR.getAmountConsumedQTwo() != null ? selfR.getAmountConsumedQTwo() : BigDecimal.ZERO;
                                break;
                            case "q3":
                                amountToRollback = selfR.getAmountConsumedQThree() != null ? selfR.getAmountConsumedQThree() : BigDecimal.ZERO;
                                break;
                            case "q4":
                                amountToRollback = selfR.getAmountConsumedQFour() != null ? selfR.getAmountConsumedQFour() : BigDecimal.ZERO;
                                break;
                        }
                    } else {
                        BigDecimal totalAmountAvailable = relatedLedgers.stream()
                                .map(l -> l.getAmountAvailable() != null ? l.getAmountAvailable() : BigDecimal.ZERO)
                                .reduce(BigDecimal.ZERO, BigDecimal::add);
                        if (totalAmountAvailable.compareTo(BigDecimal.ZERO) > 0) {
                            BigDecimal relatedAmountAvailable = relatedLedger.getAmountAvailable() != null ? relatedLedger.getAmountAvailable() : BigDecimal.ZERO;
                            amountToRollback = rollbackAmount.multiply(relatedAmountAvailable.divide(totalAmountAvailable, 10, RoundingMode.HALF_UP)).setScale(2, RoundingMode.HALF_UP);
                        } else {
                            amountToRollback = rollbackAmount.divide(BigDecimal.valueOf(relatedLedgers.size()), 2, RoundingMode.HALF_UP);
                        }
                    }
                    if (amountToRollback.compareTo(BigDecimal.ZERO) <= 0) {
                        continue;
                    }
                }
                
                // 回滚该关联单据的 amountAvailable（约定：关联流水的 amountConsumedQ* 只表示其本单从池子各季度扣的，不被下游改写，故撤销时只恢复 amountAvailable）
                BigDecimal relatedAmountAvailableBefore = relatedLedger.getAmountAvailable() == null ? BigDecimal.ZERO : relatedLedger.getAmountAvailable();
                BigDecimal newRelatedAmountAvailable = relatedAmountAvailableBefore.add(amountToRollback);
                relatedLedger.setAmountAvailable(newRelatedAmountAvailable);
                
                log.info("========== processSameDimensionRollback - 精确回滚关联流水: ledgerId={}, relatedLedgerId={}, bizCode={}, bizItemCode={}, quarter={}, 回滚金额={}, 更新前 amountAvailable={}, 更新后 amountAvailable={} ==========",
                        ledger.getId(), relatedLedger.getId(), relatedLedger.getBizCode(), relatedLedger.getBizItemCode(), rollbackQuarter,
                        amountToRollback, relatedAmountAvailableBefore, newRelatedAmountAvailable);
            }
        }
        
        return relatedBudgetLedgerMap;
    }

    @Override
    protected String getBizType() {
        return DEFAULT_BIZ_TYPE;
    }

    @Override
    protected BudgetValidationResult getBudgetValidationResult(String bizKey, 
                                                               Map<String, List<BudgetLedger>> updatedRelatedBudgetLedgerMap) {
        // 通过 bizKey 去获取 updatedRelatedBudgetLedgerMap 的集合数据
        List<BudgetLedger> relatedLedgers = updatedRelatedBudgetLedgerMap != null ? updatedRelatedBudgetLedgerMap.get(bizKey) : null;
        
        // 如果为空，保持不变
        if (CollectionUtils.isEmpty(relatedLedgers)) {
            return new BudgetValidationResult(false, null, BigDecimal.ZERO);
        }
        
        // 如果不为空，计算金额合计：遍历 List<BudgetLedger> 下 BudgetLedger 下 amountAvailable 的合计
        BigDecimal totalAmount = BigDecimal.ZERO;
        String relatedBizType = null;
        for (BudgetLedger ledger : relatedLedgers) {
            BigDecimal amountAvailable = ledger.getAmountAvailable() == null ? BigDecimal.ZERO : ledger.getAmountAvailable();
            totalAmount = totalAmount.add(amountAvailable);
            // 从第一个 ledger 获取 bizType
            if (relatedBizType == null && ledger.getBizType() != null) {
                relatedBizType = ledger.getBizType();
            }
        }
        
        log.info("========== getBudgetValidationResult - 使用自定义计算: bizKey={}, 关联流水数量={}, 金额合计={}, relatedBizType={} ==========",
                bizKey, relatedLedgers.size(), totalAmount, relatedBizType);
        
        return new BudgetValidationResult(true, relatedBizType, totalAmount);
    }

    @Override
    protected BigDecimal getCurrentAmountOperated(List<BudgetBalance> balanceList) {
        // CONTRACT 类型返回 amountOccupied 之和
        BigDecimal totalAmountOccupied = BigDecimal.ZERO;
        for (BudgetBalance balance : balanceList) {
            BigDecimal amountOccupied = balance.getAmountOccupied() == null ? BigDecimal.ZERO : balance.getAmountOccupied();
            totalAmountOccupied = totalAmountOccupied.add(amountOccupied);
        }
        return totalAmountOccupied;
    }

    @Override
    protected void performMultiQuarterDeduction(Long currentLedgerId, String bizKey, String currentQuarter, 
                                                BigDecimal totalAmountToOperate,
                                                Map<String, List<BudgetBalance>> balanceMap,
                                                Map<String, BudgetBalance> needToUpdateSameDemBudgetBalanceMap,
                                                Map<String, BigDecimal> quarterOperateAmountMap,
                                                Map<String, List<BudgetLedger>> updatedRelatedBudgetLedgerMap,
                                                Map<String, BigDecimal> relatedLedgerDeductionAmountMap) {
        BigDecimal remainingAmount = totalAmountToOperate;
        int currentQuarterNum = getQuarterNumber(currentQuarter);

        // 用 bizKey 去获取 updatedRelatedBudgetLedgerMap 中的集合数据
        List<BudgetLedger> relatedLedgers = updatedRelatedBudgetLedgerMap != null ? updatedRelatedBudgetLedgerMap.get(bizKey) : null;

        // 从当前季度开始，依次往上季度扣减（往前推，即从当前季度到 q1）
        String[] quarters = {"q1", "q2", "q3", "q4"};
        for (int i = currentQuarterNum - 1; i >= 0 && remainingAmount.compareTo(BigDecimal.ZERO) > 0; i--) {
            String quarter = quarters[i];
            String bizKeyQuarter = bizKey + "@" + quarter;

            // 从 needToUpdateSameDemBudgetBalanceMap 获取单个 balance（扣减时应该只使用单个 balance，和回滚逻辑一致）
            BudgetBalance balance = needToUpdateSameDemBudgetBalanceMap.get(bizKeyQuarter);
            // 当资金池无该维度 balance 但有关联预算流水时，仅对关联流水扣减，不抛异常（与付款单逻辑一致）
            if (balance == null) {
                if (!CollectionUtils.isEmpty(relatedLedgers)) {
                    log.info("========== performMultiQuarterDeduction - 季度 {} 无 balance 但有关联预算流水，仅扣减关联流水 ==========", quarter);
                    BigDecimal totalAmountAvailableFromRelated = BigDecimal.ZERO;
                    for (BudgetLedger rl : relatedLedgers) {
                        totalAmountAvailableFromRelated = totalAmountAvailableFromRelated.add(rl.getAmountAvailable() == null ? BigDecimal.ZERO : rl.getAmountAvailable());
                    }
                    BigDecimal amountToFreezeThisQuarter = remainingAmount.min(totalAmountAvailableFromRelated);
                    if (amountToFreezeThisQuarter.compareTo(BigDecimal.ZERO) > 0) {
                        BigDecimal existingAmt = quarterOperateAmountMap.getOrDefault(quarter, BigDecimal.ZERO);
                        quarterOperateAmountMap.put(quarter, existingAmt.add(amountToFreezeThisQuarter));
                        BigDecimal remainingToDeduct = amountToFreezeThisQuarter;
                        BigDecimal totalAvailable = totalAmountAvailableFromRelated;
                        if (totalAvailable.compareTo(BigDecimal.ZERO) > 0) {
                            BigDecimal totalDeducted = BigDecimal.ZERO;
                            for (int idx = 0; idx < relatedLedgers.size(); idx++) {
                                BudgetLedger rl = relatedLedgers.get(idx);
                                BigDecimal av = rl.getAmountAvailable() == null ? BigDecimal.ZERO : rl.getAmountAvailable();
                                if (av.compareTo(BigDecimal.ZERO) <= 0) continue;
                                BigDecimal proportion = av.divide(totalAvailable, 10, RoundingMode.HALF_UP);
                                BigDecimal toDeduct = idx == relatedLedgers.size() - 1
                                        ? remainingToDeduct.subtract(totalDeducted)
                                        : remainingToDeduct.multiply(proportion).setScale(2, RoundingMode.HALF_UP);
                                toDeduct = toDeduct.min(av);
                                // 只扣减关联流水的 amountAvailable，不写关联流水的 amountConsumedQ*（约定：各单只在自己流水上写本单从池子各季度扣的金额）
                                rl.setAmountAvailable(av.subtract(toDeduct));
                                totalDeducted = totalDeducted.add(toDeduct);
                                if (currentLedgerId != null && rl.getId() != null) {
                                    String deductionKey = currentLedgerId + "@" + rl.getId() + "@" + quarter;
                                    relatedLedgerDeductionAmountMap.merge(deductionKey, toDeduct, BigDecimal::add);
                                }
                            }
                        }
                        remainingAmount = remainingAmount.subtract(amountToFreezeThisQuarter);
                    }
                    break;
                }
                log.error("========== performMultiQuarterDeduction - 季度 {} 的 balance 不存在 ==========", quarter);
                throw new IllegalStateException(
                        String.format("明细 [%s] 的维度组合在季度 %s 未找到对应的预算余额。" +
                                      "请检查 BUDGET_POOL_DEM_R 表是否配置了该维度组合在该季度的预算。",
                                      bizKey, quarter)
                );
            }

            // 从 balanceMap 获取 balanceList 备用（用于校验可用余额的合计值，但实际扣减只操作单个 balance）
            List<BudgetBalance> balanceList = balanceMap.get(bizKeyQuarter);

            // 累加 balanceList 下所有 BudgetBalance 的 amountAvailable 和 amountOccupied 合计（用于校验）
            BigDecimal totalAmountAvailableFromList = BigDecimal.ZERO;
            BigDecimal totalAmountOccupiedFromList = BigDecimal.ZERO;
            if (!CollectionUtils.isEmpty(balanceList)) {
                for (BudgetBalance balanceItem : balanceList) {
                    BigDecimal amountAvailable = balanceItem.getAmountAvailable() == null ? BigDecimal.ZERO : balanceItem.getAmountAvailable();
                    BigDecimal amountOccupied = balanceItem.getAmountOccupied() == null ? BigDecimal.ZERO : balanceItem.getAmountOccupied();
                    totalAmountAvailableFromList = totalAmountAvailableFromList.add(amountAvailable);
                    totalAmountOccupiedFromList = totalAmountOccupiedFromList.add(amountOccupied);
                }
            }

            // 使用单个 balance 的值
            BigDecimal quarterAmountAvailable = balance.getAmountAvailable() == null ? BigDecimal.ZERO : balance.getAmountAvailable();
            BigDecimal quarterAmountOccupied = balance.getAmountOccupied() == null ? BigDecimal.ZERO : balance.getAmountOccupied();

            if (CollectionUtils.isEmpty(relatedLedgers)) {
                // 如果获取到的 List<BudgetLedger> 集合数据为空
                // 使用 balanceList 的合计值来校验可用余额（totalAmountAvailableFromList），但实际扣减只操作单个 balance
                // 关闭预算校验（重跑）时也先按可用额上限分摊，不再一次性把剩余金额全扣在当前季度
                BigDecimal amountToOperateThisQuarter = remainingAmount.min(totalAmountAvailableFromList);

                if (amountToOperateThisQuarter.compareTo(BigDecimal.ZERO) > 0) {
                    // 记录本季度操作的金额（累加，如果该季度已经有操作金额）
                    BigDecimal existingAmount = quarterOperateAmountMap.getOrDefault(quarter, BigDecimal.ZERO);
                    quarterOperateAmountMap.put(quarter, existingAmount.add(amountToOperateThisQuarter));

                    // 只操作单个 balance（和回滚逻辑一致，不按比例分配到多个 balance）
                    balance.setAmountAvailable(quarterAmountAvailable.subtract(amountToOperateThisQuarter));
                    balance.setAmountOccupied(quarterAmountOccupied.add(amountToOperateThisQuarter));

                    // 更新变化量
                    BigDecimal amountOccupiedVchanged = balance.getAmountOccupiedVchanged() == null ? BigDecimal.ZERO : balance.getAmountOccupiedVchanged();
                    BigDecimal amountAvailableVchanged = balance.getAmountAvailableVchanged() == null ? BigDecimal.ZERO : balance.getAmountAvailableVchanged();
                    balance.setAmountOccupiedVchanged(amountOccupiedVchanged.add(amountToOperateThisQuarter));
                    balance.setAmountAvailableVchanged(amountAvailableVchanged.subtract(amountToOperateThisQuarter));

                    remainingAmount = remainingAmount.subtract(amountToOperateThisQuarter);
                    markSubmitPoolBudgetBalanceTouched(bizKey);

                    log.info("========== performMultiQuarterDeduction - 季度 {} 扣减（无关联流水）: 操作金额={}, 剩余待操作={}, 扣减后amountAvailable={}, amountOccupied={} ==========",
                            quarter, amountToOperateThisQuarter, remainingAmount, balance.getAmountAvailable(), balance.getAmountOccupied());
                }
            } else {
                // 如果获取到的 List<BudgetLedger> 集合数据不为空
                // 每个季度要获取的就是 List<BudgetLedger> 集合数据下对应季度的值作为要扣减的数
                // 累加 balanceList 下所有BudgetBalance的amountFrozen合计（用于校验），但实际扣减只操作单个balance
                BigDecimal totalAmountFrozenFromList = BigDecimal.ZERO;
                if (!CollectionUtils.isEmpty(balanceList)) {
                    for (BudgetBalance balanceItem : balanceList) {
                        BigDecimal amountFrozen = balanceItem.getAmountFrozen() == null ? BigDecimal.ZERO : balanceItem.getAmountFrozen();
                        totalAmountFrozenFromList = totalAmountFrozenFromList.add(amountFrozen);
                    }
                }
                
                // 使用单个 balance 的值
                BigDecimal quarterAmountFrozen = balance.getAmountFrozen() == null ? BigDecimal.ZERO : balance.getAmountFrozen();
                
                // 计算 List<BudgetLedger> 集合数据下 BudgetLedger 对应季度值的合计
                BigDecimal totalQuarterConsumedAmount = BigDecimal.ZERO;
                for (BudgetLedger ledger : relatedLedgers) {
                    BigDecimal quarterConsumedAmount = getQuarterConsumedAmount(ledger, quarter);
                    totalQuarterConsumedAmount = totalQuarterConsumedAmount.add(quarterConsumedAmount);
                }
                
                // 计算所有关联单据的 amountAvailable 总和（作为可扣减金额使用）
                BigDecimal totalAmountAvailableFromRelatedLedgers = BigDecimal.ZERO;
                for (BudgetLedger ledger : relatedLedgers) {
                    BigDecimal ledgerAmountAvailable = ledger.getAmountAvailable() == null ? BigDecimal.ZERO : ledger.getAmountAvailable();
                    totalAmountAvailableFromRelatedLedgers = totalAmountAvailableFromRelatedLedgers.add(ledgerAmountAvailable);
                }
                
                // 始终使用关联流水的 amountAvailable 作为可扣减金额
                // 这是因为预算校验已经通过，说明关联流水的 amountAvailable 是足够的
                // totalQuarterConsumedAmount 表示已消耗金额，不能作为可扣减金额使用
                // 应该使用 totalAmountAvailableFromRelatedLedgers（总可用金额）作为可扣减金额
                BigDecimal availableAmountFromRelatedLedgers = totalAmountAvailableFromRelatedLedgers;
                
                log.info("========== performMultiQuarterDeduction - 关联流水金额计算: quarter={}, totalQuarterConsumedAmount={}, totalAmountAvailableFromRelatedLedgers={}, availableAmountFromRelatedLedgers={} ==========",
                        quarter, totalQuarterConsumedAmount, totalAmountAvailableFromRelatedLedgers, availableAmountFromRelatedLedgers);
                
                // 比较 remainingAmount 和 availableAmountFromRelatedLedgers 看谁小，谁就是 amountToFreezeThisQuarter
                BigDecimal amountToFreezeThisQuarter = remainingAmount.min(availableAmountFromRelatedLedgers);
                
                if (amountToFreezeThisQuarter.compareTo(BigDecimal.ZERO) > 0) {
                    // 记录本季度操作的金额（累加，如果该季度已经有操作金额）
                    BigDecimal existingAmount = quarterOperateAmountMap.getOrDefault(quarter, BigDecimal.ZERO);
                    quarterOperateAmountMap.put(quarter, existingAmount.add(amountToFreezeThisQuarter));
                    
                    // 扣减 List<BudgetLedger> 集合数据下 BudgetLedger 对应季度值
                    // 按比例扣减：根据每个关联单据的 amountAvailable 比例分配扣减金额
                    BigDecimal remainingToDeduct = amountToFreezeThisQuarter;
                    
                    // 计算所有关联单据的 amountAvailable 总和（按季度）
                    BigDecimal totalAmountAvailable = BigDecimal.ZERO;
                    for (BudgetLedger ledger : relatedLedgers) {
                        BigDecimal ledgerAmountAvailable = ledger.getAmountAvailable() == null ? BigDecimal.ZERO : ledger.getAmountAvailable();
                        totalAmountAvailable = totalAmountAvailable.add(ledgerAmountAvailable);
                    }
                    
                    // 如果总可用金额为0，跳过扣减
                    if (totalAmountAvailable.compareTo(BigDecimal.ZERO) <= 0) {
                        log.warn("========== performMultiQuarterDeduction - 所有关联单据的 amountAvailable 总和为0，跳过扣减: quarter={}, remainingToDeduct={} ==========",
                                quarter, remainingToDeduct);
                        continue;
                    }
                    
                    // 按比例扣减每个关联单据
                    BigDecimal totalDeducted = BigDecimal.ZERO;
                    for (int ledgerIndex = 0; ledgerIndex < relatedLedgers.size(); ledgerIndex++) {
                        BudgetLedger ledger = relatedLedgers.get(ledgerIndex);
                        BigDecimal ledgerAmountAvailable = ledger.getAmountAvailable() == null ? BigDecimal.ZERO : ledger.getAmountAvailable();
                        
                        if (ledgerAmountAvailable.compareTo(BigDecimal.ZERO) <= 0) {
                            continue;
                        }
                        
                        // 计算该关联单据应该扣减的金额（按比例）
                        BigDecimal proportion = ledgerAmountAvailable.divide(totalAmountAvailable, 10, RoundingMode.HALF_UP);
                        BigDecimal amountToDeductFromLedger;
                        
                        if (ledgerIndex == relatedLedgers.size() - 1) {
                            // 最后一个关联单据，扣减剩余的所有金额（避免精度问题）
                            amountToDeductFromLedger = remainingToDeduct.subtract(totalDeducted);
                        } else {
                            amountToDeductFromLedger = remainingToDeduct.multiply(proportion).setScale(2, RoundingMode.HALF_UP);
                        }
                        
                        // 不能超过该关联单据的 amountAvailable
                        amountToDeductFromLedger = amountToDeductFromLedger.min(ledgerAmountAvailable);
                        
                        // 只扣减关联流水的 amountAvailable，不写关联流水的 amountConsumedQ*（约定：各单只在自己流水上写本单从池子各季度扣的金额）
                        ledger.setAmountAvailable(ledgerAmountAvailable.subtract(amountToDeductFromLedger));
                        
                        totalDeducted = totalDeducted.add(amountToDeductFromLedger);
                        
                        // 记录扣减金额到 relatedLedgerDeductionAmountMap（同一 key 累加，避免同一关联流水重复出现时覆盖）
                        // key格式：currentLedgerId + "@" + relatedLedgerId + "@" + quarter
                        if (currentLedgerId != null && ledger.getId() != null) {
                            String deductionKey = currentLedgerId + "@" + ledger.getId() + "@" + quarter;
                            relatedLedgerDeductionAmountMap.merge(deductionKey, amountToDeductFromLedger, BigDecimal::add);
                        }
                        
                        log.info("========== performMultiQuarterDeduction - BudgetLedger {} 季度 {} 按比例扣减: amountAvailable扣减={}, 比例={} ==========",
                                ledger.getId(), quarter, amountToDeductFromLedger, proportion);
                    }
                    
                    // 验证：所有金额都应该被扣减完毕
                    if (totalDeducted.compareTo(remainingToDeduct) != 0) {
                        log.warn("========== performMultiQuarterDeduction - 按比例扣减后金额不匹配: totalDeducted={}, remainingToDeduct={}, 差异={} ==========",
                                totalDeducted, remainingToDeduct, totalDeducted.subtract(remainingToDeduct));
                    }
                    
                    // 然后才是扣减 balance 的 amountFrozen，只操作单个 balance（和回滚逻辑一致，不按比例分配到多个balance）
                    BigDecimal balanceAmountOccupied = balance.getAmountOccupied() == null ? BigDecimal.ZERO : balance.getAmountOccupied();
                    
                    // 扣减 balance 的 amountFrozen
                    balance.setAmountFrozen(quarterAmountFrozen.subtract(amountToFreezeThisQuarter));
                    
                    // balance 的 amountOccupied 就叠加
                    balance.setAmountOccupied(balanceAmountOccupied.add(amountToFreezeThisQuarter));
                    
                    // 更新变化量
                    BigDecimal amountFrozenVchanged = balance.getAmountFrozenVchanged() == null ? BigDecimal.ZERO : balance.getAmountFrozenVchanged();
                    BigDecimal amountOccupiedVchanged = balance.getAmountOccupiedVchanged() == null ? BigDecimal.ZERO : balance.getAmountOccupiedVchanged();
                    balance.setAmountFrozenVchanged(amountFrozenVchanged.subtract(amountToFreezeThisQuarter));
                    balance.setAmountOccupiedVchanged(amountOccupiedVchanged.add(amountToFreezeThisQuarter));
                    
                    remainingAmount = remainingAmount.subtract(amountToFreezeThisQuarter);
                    markSubmitPoolBudgetBalanceTouched(bizKey);
                    
                    log.info("========== performMultiQuarterDeduction - 季度 {} 扣减（有关联流水）: 操作金额={}, 剩余待操作={}, 扣减后amountFrozen={}, amountOccupied={} ==========",
                            quarter, amountToFreezeThisQuarter, remainingAmount, balance.getAmountFrozen(), balance.getAmountOccupied());
                }
            }
        }

        // 关闭预算校验（重跑场景）时：若跨季度用光所有正向可用额后仍有缺口，则只允许在 q1 继续透支
        if (isSkipBudgetValidation() && remainingAmount.compareTo(BigDecimal.ZERO) > 0) {
            String q1Key = bizKey + "@q1";
            BudgetBalance q1Balance = needToUpdateSameDemBudgetBalanceMap.get(q1Key);
            if (q1Balance == null) {
                log.error("========== performMultiQuarterDeduction - Q1 的 balance 不存在，无法进行透支扣减: bizKey={} ==========", bizKey);
                throw new IllegalStateException(
                        String.format("明细 [%s] 的维度组合在季度 q1 未找到对应的预算余额，无法进行重跑透支扣减。", bizKey)
                );
            }

            BigDecimal q1Avail = q1Balance.getAmountAvailable() == null ? BigDecimal.ZERO : q1Balance.getAmountAvailable();
            BigDecimal q1Occupied = q1Balance.getAmountOccupied() == null ? BigDecimal.ZERO : q1Balance.getAmountOccupied();
            BigDecimal q1AvailVchanged = q1Balance.getAmountAvailableVchanged() == null ? BigDecimal.ZERO : q1Balance.getAmountAvailableVchanged();
            BigDecimal q1OccupiedVchanged = q1Balance.getAmountOccupiedVchanged() == null ? BigDecimal.ZERO : q1Balance.getAmountOccupiedVchanged();

            // 记录 Q1 的操作金额（累加模式）
            BigDecimal existingQ1 = quarterOperateAmountMap.getOrDefault("q1", BigDecimal.ZERO);
            quarterOperateAmountMap.put("q1", existingQ1.add(remainingAmount));

            // 允许 Q1 变成负数：amountAvailable = 原可用 - 剩余缺口
            q1Balance.setAmountAvailable(q1Avail.subtract(remainingAmount));
            q1Balance.setAmountOccupied(q1Occupied.add(remainingAmount));
            q1Balance.setAmountAvailableVchanged(q1AvailVchanged.subtract(remainingAmount));
            q1Balance.setAmountOccupiedVchanged(q1OccupiedVchanged.add(remainingAmount));
            markSubmitPoolBudgetBalanceTouched(bizKey);

            log.info("========== performMultiQuarterDeduction - Q1 透支扣减: 透支金额={}, 扣减后amountAvailable={}, amountOccupied={} ==========",
                    remainingAmount, q1Balance.getAmountAvailable(), q1Balance.getAmountOccupied());

            remainingAmount = BigDecimal.ZERO;
        }

        // 验证：所有金额都应该被扣减完毕（因为已经通过预算校验）；绕过预算校验时允许有剩余不拦
        if (remainingAmount.compareTo(BigDecimal.ZERO) > 0 && !isSkipBudgetValidation()) {
            log.error("========== performMultiQuarterDeduction - 扣减后仍有剩余金额未操作: remainingAmount={} ==========", remainingAmount);
            throw new IllegalStateException("扣减后仍有剩余金额未操作，remainingAmount=" + remainingAmount);
        }
    }
    
    /**
     * 获取 BudgetLedger 指定季度的消耗金额
     *
     * @param ledger BudgetLedger
     * @param quarter 季度（q1、q2、q3、q4）
     * @return 季度消耗金额
     */
    private BigDecimal getQuarterConsumedAmount(BudgetLedger ledger, String quarter) {
        switch (quarter) {
            case "q1":
                return ledger.getAmountConsumedQOne() == null ? BigDecimal.ZERO : ledger.getAmountConsumedQOne();
            case "q2":
                return ledger.getAmountConsumedQTwo() == null ? BigDecimal.ZERO : ledger.getAmountConsumedQTwo();
            case "q3":
                return ledger.getAmountConsumedQThree() == null ? BigDecimal.ZERO : ledger.getAmountConsumedQThree();
            case "q4":
                return ledger.getAmountConsumedQFour() == null ? BigDecimal.ZERO : ledger.getAmountConsumedQFour();
            default:
                return BigDecimal.ZERO;
        }
    }
    
    /**
     * 设置 BudgetLedger 指定季度的消耗金额
     *
     * @param ledger BudgetLedger
     * @param quarter 季度（q1、q2、q3、q4）
     * @param amount 要设置的金额
     */
    private void setQuarterConsumedAmount(BudgetLedger ledger, String quarter, BigDecimal amount) {
        switch (quarter) {
            case "q1":
                ledger.setAmountConsumedQOne(amount);
                break;
            case "q2":
                ledger.setAmountConsumedQTwo(amount);
                break;
            case "q3":
                ledger.setAmountConsumedQThree(amount);
                break;
            case "q4":
                ledger.setAmountConsumedQFour(amount);
                break;
            default:
                log.warn("========== setQuarterConsumedAmount - 未知季度: {} ==========", quarter);
                break;
        }
    }

    /**
     * 加载预算数据用于比较
     *
     * @param year 年度
     * @return 预算数据Map，key为维度组合，value为SystemProjectBudget
     */
    @Override
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
     * 构建整单错误响应（所有明细都报同样的错）
     */
    private BudgetContractRespVo buildErrorResponseForAllDetails(BudgetContractApplyParams budgetContractApplyParams, Exception e) {
        ESBInfoParams esbInfo = budgetContractApplyParams.getEsbInfo();
        ContractApplyReqInfoParams contractApplyReqInfo = budgetContractApplyParams.getContractApplyReqInfo();
        String errorMessage = StringUtils.isNotBlank(e.getMessage()) ? e.getMessage() : e.getClass().getSimpleName();

        String instId = esbInfo != null ? esbInfo.getInstId() : null;
        if (StringUtils.isBlank(instId)) {
            String uuid = UUID.randomUUID().toString().replace("-", "");
            instId = "N" + uuid.substring(0, 8) + "." + uuid.substring(8, 16) + ".N"
                    + uuid.substring(16, 18) + "." + uuid.substring(18, 28) + ".N" + uuid.substring(28, 32);
        }
        String responseTime = LocalDateTime.now().format(DATE_TIME_FORMATTER);

        // 判断是否为业务异常：IllegalArgumentException 和 IllegalStateException 是业务异常
        boolean isBusinessException = e instanceof IllegalArgumentException || e instanceof IllegalStateException;
        
        // 根据 dataSource 判断返回状态：
        // - OA 和 HLY：统一返回 S（部分失败也返回 S）
        String returnStatus = "S";

        // 提取组织和科目编码，查询名称映射
        Set<String> managementOrgSet = contractApplyReqInfo != null && contractApplyReqInfo.getContractDetails() != null 
            ? contractApplyReqInfo.getContractDetails().stream()
                .map(ContractDetailDetailVo::getManagementOrg)
                .filter(StringUtils::isNotBlank)
                .collect(Collectors.toSet())
            : Collections.emptySet();
        Set<String> budgetSubjectCodeSet = contractApplyReqInfo != null && contractApplyReqInfo.getContractDetails() != null 
            ? contractApplyReqInfo.getContractDetails().stream()
                .map(ContractDetailDetailVo::getBudgetSubjectCode)
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

        ESBRespInfoVo esbRespInfo = ESBRespInfoVo.builder()
                .instId(instId)
                .requestTime(esbInfo != null ? esbInfo.getRequestTime() : null)
                .attr1(esbInfo != null ? esbInfo.getAttr1() : null)
                .attr2(esbInfo != null ? esbInfo.getAttr2() : null)
                .attr3(esbInfo != null ? esbInfo.getAttr3() : null)
                .returnCode("E0001-CONTRACT")
                .returnMsg(errorMessage)
                .returnStatus(returnStatus)
                .responseTime(responseTime)
                .build();

        // 构建返回的合同明细列表（所有明细都标记为失败）
        List<ContractDetailRespVo> resultContractDetails = new ArrayList<>();
        if (contractApplyReqInfo != null && contractApplyReqInfo.getContractDetails() != null) {
            for (ContractDetailDetailVo detail : contractApplyReqInfo.getContractDetails()) {
                ContractDetailRespVo resultDetail = new ContractDetailRespVo();
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
                resultContractDetails.add(resultDetail);
            }
        }

        ContractApplyResultInfoRespVo resultInfo = new ContractApplyResultInfoRespVo();
        resultInfo.setContractNo(contractApplyReqInfo != null ? contractApplyReqInfo.getContractNo() : null);
        resultInfo.setProcessTime(LocalDateTime.now().format(PROCESS_TIME_FORMATTER));
        resultInfo.setContractDetails(resultContractDetails);

        BudgetContractRespVo respVo = new BudgetContractRespVo();
        respVo.setEsbInfo(esbRespInfo);
        respVo.setContractApplyResult(resultInfo);
        return respVo;
    }

    /**
     * 构建明细级别错误响应（部分明细成功，部分明细失败）
     */
    private BudgetContractRespVo buildResponseWithDetailErrors(BudgetContractApplyParams budgetContractApplyParams,
                                                              Map<String, String> detailValidationResultMap,
                                                              Map<String, String> detailValidationMessageMap) {
        ESBInfoParams esbInfo = budgetContractApplyParams.getEsbInfo();
        ContractApplyReqInfoParams contractApplyReqInfo = budgetContractApplyParams.getContractApplyReqInfo();
        
        String instId = esbInfo != null ? esbInfo.getInstId() : null;
        if (instId == null || instId.isEmpty()) {
            String uuid = UUID.randomUUID().toString().replace("-", "");
            instId = "N" + uuid.substring(0, 8) + "." + uuid.substring(8, 16) + ".N"
                    + uuid.substring(16, 18) + "." + uuid.substring(18, 28) + ".N" + uuid.substring(28, 32);
        }
        String responseTime = LocalDateTime.now().format(DATE_TIME_FORMATTER);
        
        // 提取组织和科目编码，查询名称映射
        Set<String> managementOrgSet = contractApplyReqInfo != null && contractApplyReqInfo.getContractDetails() != null 
            ? contractApplyReqInfo.getContractDetails().stream()
                .map(ContractDetailDetailVo::getManagementOrg)
                .filter(StringUtils::isNotBlank)
                .collect(Collectors.toSet())
            : Collections.emptySet();
        Set<String> budgetSubjectCodeSet = contractApplyReqInfo != null && contractApplyReqInfo.getContractDetails() != null 
            ? contractApplyReqInfo.getContractDetails().stream()
                .map(ContractDetailDetailVo::getBudgetSubjectCode)
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
        
        // 兜底：进入明细错误响应分支时，必须按失败返回，避免 map 丢标导致误返回成功
        boolean hasError = CollectionUtils.isEmpty(detailValidationResultMap)
                || detailValidationResultMap.values().stream().anyMatch("1"::equals);
        
        // 根据 dataSource 判断返回状态：
        // - OA 和 HLY：部分失败也返回 S（保持原状）
        String returnStatus = "S";
        
        ESBRespInfoVo esbRespInfo = ESBRespInfoVo.builder()
                .instId(instId)
                .requestTime(esbInfo != null ? esbInfo.getRequestTime() : null)
                .attr1(esbInfo != null ? esbInfo.getAttr1() : null)
                .attr2(esbInfo != null ? esbInfo.getAttr2() : null)
                .attr3(esbInfo != null ? esbInfo.getAttr3() : null)
                .returnCode(hasError ? "E0001-CONTRACT" : "A0001-CONTRACT")
                .returnMsg(hasError ? "部分明细处理失败" : "预算合同处理成功")
                .returnStatus(returnStatus)
                .responseTime(responseTime)
                .build();

        // 构建返回的合同明细列表（包含每个明细的校验结果）
        List<ContractDetailRespVo> resultContractDetails = new ArrayList<>();
        if (contractApplyReqInfo != null && contractApplyReqInfo.getContractDetails() != null) {
            for (ContractDetailDetailVo detail : contractApplyReqInfo.getContractDetails()) {
                ContractDetailRespVo resultDetail = new ContractDetailRespVo();
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
                
                // 设置明细级别的校验结果（使用 contractDetailLineNo 作为 key）
                String detailLineNo = detail.getContractDetailLineNo();
                resultDetail.setValidationResult(detailValidationResultMap.getOrDefault(detailLineNo, "1"));
                resultDetail.setValidationMessage(detailValidationMessageMap.getOrDefault(detailLineNo, "未处理"));
                
                resultContractDetails.add(resultDetail);
            }
        }

        ContractApplyResultInfoRespVo resultInfo = new ContractApplyResultInfoRespVo();
        resultInfo.setContractNo(contractApplyReqInfo != null ? contractApplyReqInfo.getContractNo() : null);
        resultInfo.setProcessTime(LocalDateTime.now().format(PROCESS_TIME_FORMATTER));
        resultInfo.setContractDetails(resultContractDetails);

        BudgetContractRespVo respVo = new BudgetContractRespVo();
        respVo.setEsbInfo(esbRespInfo);
        respVo.setContractApplyResult(resultInfo);
        return respVo;
    }

    /**
     * 处理采购合同的审批及撤回操作
     *
     * @param budgetContractRenewParams 预算合同审批/撤回参数
     * @return 预算合同审批/撤回响应
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public BudgetContractRenewRespVo authOrCancel(BudgetContractRenewParams budgetContractRenewParams) {
        log.info("开始处理预算合同审批/撤回，params={}", budgetContractRenewParams);

        try {
            ContractRenewReqInfoParams contractRenewReqInfo = budgetContractRenewParams.getContractRenewReqInfo();
            String contractNo = contractRenewReqInfo.getContractNo();
            String documentStatus = contractRenewReqInfo.getDocumentStatus();
            LocalDateTime requestTime = BudgetQueryHelperService.parseEsbRequestTime(
                    budgetContractRenewParams.getEsbInfo() != null ? budgetContractRenewParams.getEsbInfo().getRequestTime() : null);
            String renewYear = resolveRenewYear(contractNo, contractRenewReqInfo);
            if (StringUtils.isNotBlank(renewYear) && !isCurrentYear(renewYear)
                    && ("REJECTED".equals(documentStatus) || "CANCELLED".equals(documentStatus) || "CLOSED".equals(documentStatus))) {
                log.info("预算合同跨年跳过回滚，仅执行删除，contractNo={}, documentStatus={}, year={}, currentYear={}",
                        contractNo, documentStatus, renewYear, LocalDate.now().getYear());
                return handleRejectedOrCancelledSkipRollback(contractNo, documentStatus, budgetContractRenewParams);
            }
            if ("APPROVED".equals(documentStatus)) {
                // 场景一：APPROVED
                return handleApprovedWithoutAmount(contractNo, budgetContractRenewParams, requestTime);
                
            } else if ("REJECTED".equals(documentStatus) || "CANCELLED".equals(documentStatus) || "CLOSED".equals(documentStatus)) {
                // 场景二：REJECTED、CANCELLED 或 CLOSED
                return handleRejectedOrCancelled(contractNo, documentStatus, budgetContractRenewParams);
                
            } else {
                // 场景三：其他状态报错
                throw new IllegalArgumentException("不支持的单据状态：" + documentStatus);
            }
        } catch (IllegalArgumentException e) {
            // 业务参数校验错误，接口调用成功，返回 "S"
            log.error("预算合同审批/撤回参数校验失败", e);
            return buildErrorResponse(budgetContractRenewParams, e, true);
        } catch (IllegalStateException e) {
            // 业务状态错误，接口调用成功，返回 "S"
            log.error("预算合同审批/撤回业务处理失败", e);
            return buildErrorResponse(budgetContractRenewParams, e, true);
        } catch (Exception e) {
            // 系统异常，接口调用失败，返回 "F"
            log.error("预算合同审批/撤回处理失败", e);
            return buildErrorResponse(budgetContractRenewParams, e, false);
        }
    }

    /**
     * 处理 APPROVED 但明细没有金额的场景
     *
     * @param contractNo 合同号
     * @param budgetContractRenewParams 预算合同审批/撤回参数
     * @param requestTime ESB 请求时间，用于 HEAD 的 UPDATE_TIME
     * @return 预算合同审批/撤回响应
     */
    private BudgetContractRenewRespVo handleApprovedWithoutAmount(String contractNo, BudgetContractRenewParams budgetContractRenewParams, LocalDateTime requestTime) {
        log.info("========== 场景一：APPROVED，contractNo={} ==========", contractNo);
        
        LambdaQueryWrapper<BudgetLedgerHead> headWrapper = new LambdaQueryWrapper<>();
        headWrapper.eq(BudgetLedgerHead::getBizCode, contractNo)
                .eq(BudgetLedgerHead::getDeleted, Boolean.FALSE);
        BudgetLedgerHead existingHead = budgetLedgerHeadMapper.selectOne(headWrapper);
        
        if (existingHead == null) {
            throw new IllegalStateException("未找到对应的预算流水头，业务单号：" + contractNo);
        }

        // 白名单校验：查询该单据下的所有 BudgetLedger，如果所有记录的科目编码都不在白名单中且不带项目编码，直接返回成功
        LambdaQueryWrapper<BudgetLedger> ledgerWrapper = new LambdaQueryWrapper<>();
        ledgerWrapper.eq(BudgetLedger::getBizCode, contractNo)
                .eq(BudgetLedger::getBizType, DEFAULT_BIZ_TYPE)
                .eq(BudgetLedger::getDeleted, Boolean.FALSE);
        List<BudgetLedger> allLedgers = budgetLedgerMapper.selectList(ledgerWrapper);
        
        if (!allLedgers.isEmpty()) {
            boolean allNotInWhitelist = true;
            for (BudgetLedger ledger : allLedgers) {
                String code = ledger.getBudgetSubjectCode();
                String masterProjectCode = ledger.getMasterProjectCode();
                
                // 科目编码在白名单中
                boolean isSubjectCodeInWhitelist = StringUtils.isNotBlank(code) && !"NAN-NAN".equals(code) && budgetSubjectCodeConfig.isInWhitelist(code);
                
                // 带项目的明细（masterProjectCode 不为空且不是 "NAN"），即使科目编码不在白名单中，也需要校验
                boolean hasProjectCode = StringUtils.isNotBlank(masterProjectCode) && !"NAN".equals(masterProjectCode);
                
                if (isSubjectCodeInWhitelist || hasProjectCode) {
                    // 发现有白名单中的科目编码或带项目编码，需要正常处理
                    allNotInWhitelist = false;
                    break;
                }
            }
            
            if (allNotInWhitelist) {
                // 所有记录的科目编码都不在白名单中且不带项目编码，跳过预算校验，但仍需要更新 BudgetLedgerHead 的 status 为 APPROVED
                log.info("所有记录的科目编码都不在白名单中且不带项目编码，跳过预算校验，但仍更新审批状态");
                existingHead.setStatus("APPROVED");
                existingHead.setVersion(String.valueOf(identifierGenerator.nextId(null)));
                if (requestTime != null) {
                    existingHead.setUpdateTime(requestTime);
                }
                budgetLedgerHeadMapper.updateById(existingHead);
                log.info("========== 场景一处理完成（不受控明细，跳过预算校验） ==========");
                return buildSuccessResponse(budgetContractRenewParams, "预算合同审批成功");
            }
        }

        existingHead.setStatus("APPROVED");
        existingHead.setVersion(String.valueOf(identifierGenerator.nextId(null)));
        if (requestTime != null) {
            existingHead.setUpdateTime(requestTime);
        }
        budgetLedgerHeadMapper.updateById(existingHead);

        log.info("========== 场景一处理完成 ==========");
        return buildSuccessResponse(budgetContractRenewParams, "预算合同审批成功");
    }

    /**
     * 处理 REJECTED、CANCELLED 或 CLOSED 的场景
     *
     * @param contractNo 合同号
     * @param documentStatus 单据状态
     * @param budgetContractRenewParams 预算合同审批/撤回参数
     * @return 预算合同审批/撤回响应
     */
    private BudgetContractRenewRespVo handleRejectedOrCancelled(String contractNo, String documentStatus, BudgetContractRenewParams budgetContractRenewParams) {
        log.info("========== 场景二：REJECTED、CANCELLED 或 CLOSED，contractNo={}, documentStatus={} ==========", contractNo, documentStatus);
        
        // 查询 BUDGET_LEDGER_HEAD
        LambdaQueryWrapper<BudgetLedgerHead> headWrapper = new LambdaQueryWrapper<>();
        headWrapper.eq(BudgetLedgerHead::getBizCode, contractNo)
                .eq(BudgetLedgerHead::getDeleted, Boolean.FALSE);
        BudgetLedgerHead existingHead = budgetLedgerHeadMapper.selectOne(headWrapper);
        
        if (existingHead == null) {
            throw new IllegalStateException("未找到对应的预算流水头，业务单号：" + contractNo);
        }

        // 查询 BUDGET_LEDGER
        LambdaQueryWrapper<BudgetLedger> ledgerWrapper = new LambdaQueryWrapper<>();
        ledgerWrapper.eq(BudgetLedger::getBizCode, contractNo)
                .eq(BudgetLedger::getBizType, DEFAULT_BIZ_TYPE)
                .eq(BudgetLedger::getDeleted, Boolean.FALSE);
        List<BudgetLedger> allLedgers = budgetLedgerMapper.selectList(ledgerWrapper);

        // 白名单校验：与事前申请一致——仅当所有明细均「科目不在白名单、无项目、无资产类型」时才跳过余额/额度回滚；否则走完整回滚
        if (!allLedgers.isEmpty()) {
            boolean allNotInWhitelist = true;
            for (BudgetLedger ledger : allLedgers) {
                String code = ledger.getBudgetSubjectCode();
                String masterProjectCode = ledger.getMasterProjectCode();
                String erpAssetType = ledger.getErpAssetType();
                // 科目编码在白名单中
                if (StringUtils.isNotBlank(code) && !"NAN-NAN".equals(code) && budgetSubjectCodeConfig.isInWhitelist(code)) {
                    allNotInWhitelist = false;
                    break;
                }
                // 带项目的明细（masterProjectCode 不为空且不是 "NAN"），即使科目编码不在白名单中，也需要走完整回滚
                if (StringUtils.isNotBlank(masterProjectCode) && !"NAN".equals(masterProjectCode)) {
                    allNotInWhitelist = false;
                    break;
                }
                // 带资产类型（erpAssetType 不为空且不是 "NAN"），组织+资产类型维度需走完整回滚
                if (StringUtils.isNotBlank(erpAssetType) && !"NAN".equals(erpAssetType)) {
                    allNotInWhitelist = false;
                    break;
                }
            }
            
            if (allNotInWhitelist) {
                // 所有记录的科目编码都不在白名单中且不带项目编码且不带资产类型编码，跳过预算余额/额度回滚，但仍需归档并删除流水与头
                log.info("所有记录的科目编码都不在白名单中且不带项目编码且不带资产类型编码，跳过预算校验，仍执行流水与头的归档删除");
                // 归档并删除 BudgetLedger
                if (!allLedgers.isEmpty()) {
                    List<BudgetLedgerHistory> needToAddBudgetLedgerHistory = new ArrayList<>();
                    Set<Long> needToCancelBudgetLedgerSet = new HashSet<>();
                    for (BudgetLedger ledger : allLedgers) {
                        BudgetLedgerHistory history = new BudgetLedgerHistory();
                        BeanUtils.copyProperties(ledger, history);
                        history.setId(identifierGenerator.nextId(history).longValue());
                        history.setLedgerId(ledger.getId());
                        history.setDeleted(Boolean.FALSE);
                        needToAddBudgetLedgerHistory.add(history);
                        needToCancelBudgetLedgerSet.add(ledger.getId());
                    }
                    budgetLedgerHistoryMapper.insertBatch(needToAddBudgetLedgerHistory);
                    budgetLedgerMapper.deleteByIds(new ArrayList<>(needToCancelBudgetLedgerSet));
                    // 合同的流水只会主动关联 APPLY 的，所以 bizType 传 APPLY_BIZ_TYPE
                    int deletedByIdCount = budgetLedgerSelfRMapper.deleteByIdsAndBizType(needToCancelBudgetLedgerSet, APPLY_BIZ_TYPE);
                    if (deletedByIdCount > 0) {
                        log.info("========== 白名单外撤回：删除关联的预算流水关系（ID IN）{} 条 ==========", deletedByIdCount);
                    }
                    LambdaQueryWrapper<BudgetLedgerSelfR> deleteWrapper = new LambdaQueryWrapper<>();
                    deleteWrapper.in(BudgetLedgerSelfR::getRelatedId, needToCancelBudgetLedgerSet)
                            .eq(BudgetLedgerSelfR::getBizType, APPLY_BIZ_TYPE);
                    int deletedByRelatedIdCount = budgetLedgerSelfRMapper.delete(deleteWrapper);
                    if (deletedByRelatedIdCount > 0) {
                        log.info("========== 白名单外撤回：删除关联的预算流水关系（RELATED_ID IN）{} 条 ==========", deletedByRelatedIdCount);
                    }
                }
                // 归档并删除 BudgetLedgerHead
                BudgetLedgerHeadHistory headHistory = new BudgetLedgerHeadHistory();
                BeanUtils.copyProperties(existingHead, headHistory);
                headHistory.setId(identifierGenerator.nextId(headHistory).longValue());
                headHistory.setLedgerHeadId(existingHead.getId());
                headHistory.setDeleted(Boolean.FALSE);
                budgetLedgerHeadHistoryMapper.insertBatch(Collections.singletonList(headHistory));
                budgetLedgerHeadMapper.deleteById(existingHead.getId());
                log.info("========== 场景二处理完成（白名单外撤回，已归档删除流水与头） ==========");
                String msg;
                if ("REJECTED".equals(documentStatus)) {
                    msg = "预算合同驳回成功";
                } else if ("CANCELLED".equals(documentStatus)) {
                    msg = "预算合同撤销成功";
                } else if ("CLOSED".equals(documentStatus)) {
                    msg = "预算合同关闭成功";
                } else {
                    msg = "预算合同处理成功";
                }
                return buildSuccessResponse(budgetContractRenewParams, msg);
            }
        }

        // 构建 Map<String, BudgetLedger>，key 为 bizCode + "@" + bizItemCode
        Map<String, BudgetLedger> needToCancelBudgetLedgerMap = allLedgers.stream()
                .collect(Collectors.toMap(
                        ledger -> ledger.getBizCode() + "@" + ledger.getBizItemCode(),
                        Function.identity(),
                        (a, b) -> a
                ));
        
        log.info("========== 查询到 {} 条 BudgetLedger 需要回滚 ==========", allLedgers.size());

        if (needToCancelBudgetLedgerMap.isEmpty()) {
            // 如果没有流水，直接删除头
            BudgetLedgerHeadHistory headHistory = new BudgetLedgerHeadHistory();
            BeanUtils.copyProperties(existingHead, headHistory);
            headHistory.setId(identifierGenerator.nextId(headHistory).longValue());
            headHistory.setLedgerHeadId(existingHead.getId());
            headHistory.setDeleted(Boolean.FALSE);
            budgetLedgerHeadHistoryMapper.insertBatch(Collections.singletonList(headHistory));
            budgetLedgerHeadMapper.deleteById(existingHead.getId());
            
            log.info("========== 场景二处理完成（无流水） ==========");
            String msg;
            if ("REJECTED".equals(documentStatus)) {
                msg = "预算合同驳回成功";
            } else if ("CANCELLED".equals(documentStatus)) {
                msg = "预算合同撤销成功";
            } else if ("CLOSED".equals(documentStatus)) {
                msg = "预算合同关闭成功";
            } else {
                msg = "预算合同处理成功";
            }
            return buildSuccessResponse(budgetContractRenewParams, msg);
        }

        // 批量提取 managementOrg 字段（对应 EHR_CD）
        Set<String> managementOrgSet = allLedgers.stream()
                .map(BudgetLedger::getMorgCode)
                .filter(StringUtils::isNotBlank)
                .collect(Collectors.toSet());
        
        // 批量查询 EHR_ORG_MANAGE_R 表，获取 EHR_CD 对应的 ORG_CD（一一对应关系）
        // Map<EHR_CD, ORG_CD>
        BudgetQueryHelperService.EhrCdToOrgCdMapResult ehrCdToOrgCdMapResult = budgetQueryHelperService.queryEhrCdToOrgCdOneToOneMap(managementOrgSet);
        Map<String, String> ehrCdToOrgCdMap = ehrCdToOrgCdMapResult.getEhrCdToOrgCdMap();
        
        // 批量提取 budgetSubjectCode 字段
        Set<String> budgetSubjectCodeSet = allLedgers.stream()
                .map(BudgetLedger::getBudgetSubjectCode)
                .filter(StringUtils::isNotBlank)
                .filter(code -> !"NAN-NAN".equals(code))
                .collect(Collectors.toSet());
        
        // 批量查询 SUBJECT_INFO 表，获取 ERP_ACCT_CD 对应的 ACCT_CD（一一对应关系）
        // Map<ERP_ACCT_CD, ACCT_CD>
        BudgetQueryHelperService.ErpAcctCdToAcctCdMapResult erpAcctCdToAcctCdMapResult = budgetQueryHelperService.queryErpAcctCdToAcctCdOneToOneMap(budgetSubjectCodeSet);
        Map<String, String> erpAcctCdToAcctCdMap = erpAcctCdToAcctCdMapResult.getErpAcctCdToAcctCdMap();
        
        // 批量查询 EHR_ORG_MANAGE_EXT_R 表，获取 EHR_CD 对应的所有 ORG_CD（一对多关系，用于识别不受控组织）
        // Map<EHR_CD, List<ORG_CD>>
        Map<String, List<String>> ehrCdToOrgCdExtMap = budgetQueryHelperService.queryEhrCdToOrgCdMap(managementOrgSet);
        
        // 批量查询 SUBJECT_EXT_INFO 表，获取 ERP_ACCT_CD 对应的所有 ACCT_CD（一对多关系，用于识别不受控科目）
        // Map<ERP_ACCT_CD, List<ACCT_CD>>
        Map<String, List<String>> erpAcctCdToAcctCdExtMap = budgetQueryHelperService.queryErpAcctCdToAcctCdMap(budgetSubjectCodeSet);

        // 批量提取需要映射的 erpAssetType 字段（以 "1" 或 "M" 开头且不带项目的）
        Set<String> erpAssetTypeSet = needToCancelBudgetLedgerMap.values().stream()
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
        
        // 使用 helper 方法查询所有季度的 BudgetQuota 和 BudgetBalance
        BudgetQueryHelperService.BudgetQuotaBalanceResult result = 
                budgetQueryHelperService.queryQuotaAndBalanceByAllQuarters(needToCancelBudgetLedgerMap, ehrCdToOrgCdMap, erpAcctCdToAcctCdMap, erpAssetTypeToMemberCdMap);
        
        Map<String, BudgetQuota> needToRollBackBudgetQuotaMap = result.getQuotaMap();
        Map<String, BudgetBalance> needToRollBackBudgetBalanceMap = result.getBalanceMap();
        
        // 获取关联预算流水Map（用于判断是否有关联单据）
        Map<String, List<BudgetLedger>> relatedBudgetLedgerMap = new HashMap<>();
        if (result.getRelatedBudgetLedgerMap() != null) {
            relatedBudgetLedgerMap.putAll(result.getRelatedBudgetLedgerMap());
        }

        List<BudgetQuotaHistory> needToAddBudgetQuotaHistory = new ArrayList<>();
        List<BudgetBalanceHistory> needToAddBudgetBalanceHistory = new ArrayList<>();
        
        // 用于收集需要更新的关联预算流水
        Map<String, List<BudgetLedger>> updatedRelatedBudgetLedgerMap = new HashMap<>();

        // 回滚 BudgetBalance 和 BudgetQuota
        // 遍历所有 ledger，执行回滚
        for (BudgetLedger ledger : allLedgers) {
            String bizKey = ledger.getBizCode() + "@" + ledger.getBizItemCode();
            
            // 项目非 NAN 且 isInternal=1 时跳过回滚
            if (!"NAN".equals(ledger.getMasterProjectCode()) && "1".equals(ledger.getIsInternal())) {
                log.info("========== handleRejectedOrCancelled - 内部项目跳过回滚: bizKey={}, masterProjectCode={}, isInternal={} ==========",
                        bizKey, ledger.getMasterProjectCode(), ledger.getIsInternal());
                continue;
            }
            
            // 判断是否不受控（需要跳过预算余额回滚，但仍删除数据）
            if (isUncontrolledLedger(ledger, ehrCdToOrgCdExtMap, erpAcctCdToAcctCdExtMap)) {
                log.info("========== handleRejectedOrCancelled - 不受控明细跳过预算余额回滚: bizKey={}, morgCode={}, budgetSubjectCode={}, masterProjectCode={} ==========",
                        bizKey, ledger.getMorgCode(), ledger.getBudgetSubjectCode(), ledger.getMasterProjectCode());
                continue;
            }

            // 回滚规则：先按 SELF_R 的 amountConsumedQ* 回滚关联流水，再按本单 amountConsumedQ* 回滚资金池（多季度）
            BudgetQueryHelperService.RollbackQuartersResult rollbackResult = budgetQueryHelperService.calculateRollbackQuarters(ledger);
            List<String> quartersToRollback = rollbackResult.getQuartersToRollback();
            Map<String, BigDecimal> quarterRollbackAmountMap = rollbackResult.getQuarterRollbackAmountMap();

            if (quartersToRollback.isEmpty()) {
                log.info("========== handleRejectedOrCancelled - 合同流水无需回滚: bizKey={} ==========", bizKey);
                continue;
            }

            for (String rollbackQuarter : quartersToRollback) {
                BigDecimal releaseAmount = quarterRollbackAmountMap.get(rollbackQuarter);
                String bizKeyQuarter = bizKey + "@" + rollbackQuarter;
                BudgetBalance balance;
                if (StringUtils.isNotBlank(ledger.getPoolDimensionKey())) {
                    balance = budgetQueryHelperService.selectBudgetBalanceByPoolDimensionKey(ledger.getPoolDimensionKey(), rollbackQuarter);
                    needToRollBackBudgetBalanceMap.put(bizKeyQuarter, balance);
                } else {
                    balance = needToRollBackBudgetBalanceMap.get(bizKeyQuarter);
                }
                if (balance == null) {
                    log.error("========== handleRejectedOrCancelled - 合同流水在季度 {} 未找到对应的预算余额: bizKeyQuarter={} ==========",
                            rollbackQuarter, bizKeyQuarter);
                    throw new IllegalStateException(
                            String.format("明细 [%s] 回滚时在季度 %s 未找到对应的预算余额。请检查 BUDGET_POOL_DEM_R 表是否配置了该维度组合的预算。",
                                    bizKey, rollbackQuarter)
                    );
                }

                Long poolId = balance.getPoolId();
                BudgetBalanceHistory balanceHistory = new BudgetBalanceHistory();
                BeanUtils.copyProperties(balance, balanceHistory);
                balanceHistory.setId(identifierGenerator.nextId(balanceHistory).longValue());
                balanceHistory.setBalanceId(balance.getId());
                balanceHistory.setDeleted(Boolean.FALSE);

                Map<String, List<BudgetLedger>> returnedRelatedBudgetLedgerMap = rollbackBalanceAmountForSameDimension(
                        balance, balanceHistory, ledger, rollbackQuarter, releaseAmount, poolId, relatedBudgetLedgerMap, null, null);

                if (returnedRelatedBudgetLedgerMap != null && !returnedRelatedBudgetLedgerMap.isEmpty()) {
                    updatedRelatedBudgetLedgerMap.putAll(returnedRelatedBudgetLedgerMap);
                }

                balance.setVersion(ledger.getVersion());
                needToAddBudgetBalanceHistory.add(balanceHistory);
            }
        }

        // 创建 BudgetLedgerHistory
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

        // 更新数据库（按 balanceId 去重）
        if (!needToRollBackBudgetBalanceMap.isEmpty()) {
            Map<Long, BudgetBalance> uniqueBalances = new LinkedHashMap<>();
            for (BudgetBalance b : needToRollBackBudgetBalanceMap.values()) {
                if (b != null && b.getId() != null) {
                    uniqueBalances.putIfAbsent(b.getId(), b);
                }
            }
            List<BudgetBalance> sortedBalances = sortBalancesById(new ArrayList<>(uniqueBalances.values()));
            budgetBalanceMapper.updateBatchById(sortedBalances);
        }
        if (!needToAddBudgetBalanceHistory.isEmpty()) {
            budgetBalanceHistoryMapper.insertBatch(needToAddBudgetBalanceHistory);
        }
        if (!needToRollBackBudgetQuotaMap.isEmpty()) {
            List<BudgetQuota> sortedQuotas = sortQuotasById(new ArrayList<>(needToRollBackBudgetQuotaMap.values()));
            budgetQuotaMapper.updateBatchById(sortedQuotas);
        }
        if (!needToAddBudgetQuotaHistory.isEmpty()) {
            budgetQuotaHistoryMapper.insertBatch(needToAddBudgetQuotaHistory);
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
                // 去重（根据 id）：同一关联流水可能被多个合同明细引用，保留 amountAvailable 最大的那条（回滚后的最终状态）
                Map<Long, BudgetLedger> uniqueRelatedLedgersMap = new HashMap<>();
                for (BudgetLedger ledger : relatedLedgersToUpdate) {
                    if (ledger == null || ledger.getId() == null) continue;
                    BudgetLedger existing = uniqueRelatedLedgersMap.get(ledger.getId());
                    if (existing == null) {
                        uniqueRelatedLedgersMap.put(ledger.getId(), ledger);
                    } else {
                        BigDecimal avNew = ledger.getAmountAvailable() == null ? BigDecimal.ZERO : ledger.getAmountAvailable();
                        BigDecimal avExisting = existing.getAmountAvailable() == null ? BigDecimal.ZERO : existing.getAmountAvailable();
                        // 保留回滚更完整的那条（amountAvailable 更大 = 回滚后状态），同一对象上 amountConsumedQ1-Q4 已同步回滚
                        if (avNew.compareTo(avExisting) > 0) {
                            uniqueRelatedLedgersMap.put(ledger.getId(), ledger);
                        }
                    }
                }
                List<BudgetLedger> uniqueRelatedLedgers = new ArrayList<>(uniqueRelatedLedgersMap.values());
                budgetLedgerMapper.updateBatchById(uniqueRelatedLedgers);
                log.info("========== 更新关联预算流水: 更新了 {} 条记录 ==========", uniqueRelatedLedgers.size());
            }
        }
        
        if (!needToCancelBudgetLedgerSet.isEmpty()) {
            budgetLedgerMapper.deleteByIds(new ArrayList<>(needToCancelBudgetLedgerSet));
            
            // 删除与 needToCancelBudgetLedgerSet 相关联的预算流水关系
            // 1. 删除 BUDGET_LEDGER_SELF_R 表中 ID IN needToCancelBudgetLedgerSet 的记录
            // 注意：合同的流水只会主动关联 APPLY 的，所以 bizType 传 APPLY_BIZ_TYPE
            int deletedByIdCount = budgetLedgerSelfRMapper.deleteByIdsAndBizType(needToCancelBudgetLedgerSet, APPLY_BIZ_TYPE);
            if (deletedByIdCount > 0) {
                log.info("========== 删除关联的预算流水关系（ID IN）: 删除了 {} 条关系记录 ==========", deletedByIdCount);
            }
            
            // 2. 删除 BUDGET_LEDGER_SELF_R 表中 RELATED_ID IN needToCancelBudgetLedgerSet 的记录
            // 注意：合同的流水只会主动关联 APPLY 的，所以 bizType 传 APPLY_BIZ_TYPE
            LambdaQueryWrapper<BudgetLedgerSelfR> deleteWrapper = new LambdaQueryWrapper<>();
            deleteWrapper.in(BudgetLedgerSelfR::getRelatedId, needToCancelBudgetLedgerSet)
                    .eq(BudgetLedgerSelfR::getBizType, APPLY_BIZ_TYPE);
            int deletedByRelatedIdCount = budgetLedgerSelfRMapper.delete(deleteWrapper);
            if (deletedByRelatedIdCount > 0) {
                log.info("========== 删除关联的预算流水关系（RELATED_ID IN）: 删除了 {} 条关系记录 ==========", deletedByRelatedIdCount);
            }
        }
        if (!needToAddBudgetLedgerHistory.isEmpty()) {
            budgetLedgerHistoryMapper.insertBatch(needToAddBudgetLedgerHistory);
        }

        // 处理 BudgetLedgerHead
        BudgetLedgerHeadHistory headHistory = new BudgetLedgerHeadHistory();
        BeanUtils.copyProperties(existingHead, headHistory);
        headHistory.setId(identifierGenerator.nextId(headHistory).longValue());
        headHistory.setLedgerHeadId(existingHead.getId());
        headHistory.setDeleted(Boolean.FALSE);
        budgetLedgerHeadHistoryMapper.insertBatch(Collections.singletonList(headHistory));
        budgetLedgerHeadMapper.deleteById(existingHead.getId());

        log.info("========== 场景二处理完成 ==========");
        String msg;
        if ("REJECTED".equals(documentStatus)) {
            msg = "预算合同驳回成功";
        } else if ("CANCELLED".equals(documentStatus)) {
            msg = "预算合同撤销成功";
        } else if ("CLOSED".equals(documentStatus)) {
            msg = "预算合同关闭成功";
        } else {
            msg = "预算合同处理成功";
        }
        return buildSuccessResponse(budgetContractRenewParams, msg);
    }

    /**
     * 构建成功响应
     *
     * @param params 预算合同审批/撤回参数
     * @param message 成功信息
     * @return 预算合同审批/撤回响应
     */
    private BudgetContractRenewRespVo buildSuccessResponse(BudgetContractRenewParams params, String message) {
        String currentTime = LocalDateTime.now().format(DATE_TIME_FORMATTER);
        String instId = params != null && params.getEsbInfo() != null ? 
                params.getEsbInfo().getInstId() : null;
        
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
                .returnCode("A0001-CONTRACT")
                .returnMsg(message)
                .returnStatus("S")
                .responseTime(currentTime)
                .build();

        BudgetContractRenewRespVo response = new BudgetContractRenewRespVo();
        response.setEsbInfo(esbInfo);
        return response;
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
     * 将 JSON 字符串解析为 Map<String, String>
     *
     * @param json JSON 字符串
     * @return Map<String, String>
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
                    // 反转义 JSON 特殊字符
                    value = unescapeJson(value);
                    map.put(key, value);
                }
            }
        }
        return map;
    }

    /**
     * 反转义 JSON 字符串中的特殊字符
     *
     * @param str 转义后的字符串
     * @return 原始字符串
     */
    private String unescapeJson(String str) {
        if (str == null) {
            return "";
        }
        return str.replace("\\t", "\t")
                .replace("\\r", "\r")
                .replace("\\n", "\n")
                .replace("\\\"", "\"")
                .replace("\\\\", "\\");
    }

    /**
     * 构建错误响应
     *
     * @param params 预算合同审批/撤回参数
     * @param errorMessage 错误信息
     * @return 预算合同审批/撤回响应
     */
    private BudgetContractRenewRespVo buildErrorResponse(BudgetContractRenewParams params, Exception e, boolean isBusinessException) {
        String errorMessage = StringUtils.isNotBlank(e.getMessage()) ? e.getMessage() : e.getClass().getSimpleName();
        String currentTime = LocalDateTime.now().format(DATE_TIME_FORMATTER);
        String instId = params != null && params.getEsbInfo() != null ? 
                params.getEsbInfo().getInstId() : null;
        
        // 统一返回 S（部分失败也返回 S）
        // 注意：审批/撤回参数中没有 dataSource 字段，默认使用 HLY 行为（保持原状）
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
                .returnCode("E0001-CONTRACT")
                .returnMsg(errorMessage)
                .returnStatus(returnStatus)
                .responseTime(currentTime)
                .build();

        BudgetContractRenewRespVo response = new BudgetContractRenewRespVo();
        response.setEsbInfo(esbInfo);
        return response;
    }
}

