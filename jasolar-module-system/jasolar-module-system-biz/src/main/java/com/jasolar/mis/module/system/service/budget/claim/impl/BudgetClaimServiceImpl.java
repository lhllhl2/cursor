package com.jasolar.mis.module.system.service.budget.claim.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.incrementer.IdentifierGenerator;
import com.jasolar.mis.module.system.controller.budget.vo.BudgetClaimApplyParams;
import com.jasolar.mis.module.system.controller.budget.vo.BudgetClaimRenewParams;
import com.jasolar.mis.module.system.controller.budget.vo.BudgetClaimRenewRespVo;
import com.jasolar.mis.module.system.controller.budget.vo.BudgetClaimRespVo;
import com.jasolar.mis.module.system.controller.budget.vo.BudgetParams;
import com.jasolar.mis.module.system.controller.budget.vo.BudgetRespVo;
import com.jasolar.mis.module.system.controller.budget.vo.ClaimApplyResultInfoRespVo;
import com.jasolar.mis.module.system.controller.budget.vo.DetailDetailVo;
import com.jasolar.mis.module.system.controller.budget.vo.ESBInfoParams;
import com.jasolar.mis.module.system.controller.budget.vo.ESBRespInfoVo;
import com.jasolar.mis.module.system.controller.budget.vo.ClaimApplyDetailVo;
import com.jasolar.mis.module.system.controller.budget.vo.ClaimApplyExtDetailVo;
import com.jasolar.mis.module.system.controller.budget.vo.ClaimApplyReqInfoParams;
import com.jasolar.mis.module.system.controller.budget.vo.ClaimContractDetailVo;
import com.jasolar.mis.module.system.controller.budget.vo.ClaimContractExtDetailVo;
import com.jasolar.mis.module.system.controller.budget.vo.ClaimDetailDetailVo;
import com.jasolar.mis.module.system.controller.budget.vo.ClaimDetailRespVo;
import com.jasolar.mis.module.system.controller.budget.vo.ClaimExtDetailVo;
import com.jasolar.mis.module.system.controller.budget.vo.ClaimRenewReqInfoParams;
import com.jasolar.mis.module.system.controller.budget.vo.ExtDetailVo;
import com.jasolar.mis.module.system.controller.budget.vo.ReqInfoParams;
import com.jasolar.mis.module.system.controller.budget.vo.SubDetailVo;
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
import com.jasolar.mis.module.system.mapper.budget.BudgetBalanceHistoryMapper;
import com.jasolar.mis.module.system.mapper.budget.BudgetBalanceMapper;
import com.jasolar.mis.module.system.mapper.budget.BudgetLedgerHeadHistoryMapper;
import com.jasolar.mis.module.system.mapper.budget.BudgetLedgerHeadMapper;
import com.jasolar.mis.module.system.mapper.budget.BudgetLedgerHistoryMapper;
import com.jasolar.mis.module.system.mapper.budget.BudgetLedgerMapper;
import com.jasolar.mis.module.system.mapper.budget.BudgetLedgerSelfRHistoryMapper;
import com.jasolar.mis.module.system.mapper.budget.BudgetLedgerSelfRMapper;
import com.jasolar.mis.module.system.mapper.budget.BudgetPoolDemRMapper;
import com.jasolar.mis.module.system.mapper.budget.BudgetQuotaHistoryMapper;
import com.jasolar.mis.module.system.mapper.budget.BudgetQuotaMapper;
import com.jasolar.mis.module.system.mapper.budget.SystemProjectBudgetMapper;
import com.jasolar.mis.module.system.service.budget.AbstractBudgetService;
import com.jasolar.mis.module.system.service.budget.claim.BudgetClaimService;
import com.jasolar.mis.module.system.service.budget.exception.DetailValidationException;
import com.jasolar.mis.module.system.service.budget.helper.BudgetQueryHelperService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
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
 * 预算付款/报销 Service 实现
 */
@Service
@Slf4j
public class BudgetClaimServiceImpl extends AbstractBudgetService implements BudgetClaimService {

    private static final String INITIAL_SUBMITTED = "INITIAL_SUBMITTED";
    private static final String APPROVED_UPDATE = "APPROVED_UPDATE";
    private static final String DEFAULT_BIZ_TYPE = "CLAIM";
    private static final String PROJECT_SKIP_BUDGET_SUBJECT_CODE = "000000";
    /** 合同流水 effectType=1 表示框架协议（付款先扣合同可用再扣资金池） */
    private static final String FRAMEWORK_CONTRACT_EFFECT_TYPE = "1";
    private static final String CONTRACT_BIZ_TYPE = "CONTRACT";
    private static final String APPLY_BIZ_TYPE = "APPLY";
    private static final String DEFAULT_CURRENCY = "CNY";
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
    private static final DateTimeFormatter PROCESS_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private boolean isCurrentYear(String year) {
        return StringUtils.isNotBlank(year) && String.valueOf(LocalDate.now().getYear()).equals(StringUtils.trim(year));
    }

    private String extractClaimYear(List<ClaimDetailDetailVo> claimDetails) {
        if (CollectionUtils.isEmpty(claimDetails)) {
            return null;
        }
        for (ClaimDetailDetailVo detail : claimDetails) {
            if (detail != null && StringUtils.isNotBlank(detail.getClaimYear())) {
                return StringUtils.trim(detail.getClaimYear());
            }
        }
        return null;
    }

    private String resolveRenewYear(String claimOrderNo, ClaimRenewReqInfoParams renewInfo) {
        if (renewInfo != null && !CollectionUtils.isEmpty(renewInfo.getClaimDetails())) {
            for (ClaimDetailDetailVo detail : renewInfo.getClaimDetails()) {
                if (detail != null && StringUtils.isNotBlank(detail.getClaimYear())) {
                    return StringUtils.trim(detail.getClaimYear());
                }
            }
        }
        if (StringUtils.isBlank(claimOrderNo)) {
            return null;
        }
        LambdaQueryWrapper<BudgetLedger> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BudgetLedger::getBizCode, claimOrderNo)
                .eq(BudgetLedger::getBizType, DEFAULT_BIZ_TYPE)
                .eq(BudgetLedger::getDeleted, Boolean.FALSE);
        List<BudgetLedger> ledgers = budgetLedgerMapper.selectList(wrapper);
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

    private void archiveAndDeleteClaimLedgers(Set<Long> ledgerIds) {
        if (CollectionUtils.isEmpty(ledgerIds)) {
            return;
        }
        budgetLedgerMapper.deleteByIds(new ArrayList<>(ledgerIds));
        int deletedByIdCount = budgetLedgerSelfRMapper.deleteByIdsAndBizType(ledgerIds, null);
        if (deletedByIdCount > 0) {
            log.info("========== 删除关联的预算流水关系（ID IN）: 删除了 {} 条关系记录 ==========", deletedByIdCount);
        }
        LambdaQueryWrapper<BudgetLedgerSelfR> deleteWrapper = new LambdaQueryWrapper<>();
        deleteWrapper.in(BudgetLedgerSelfR::getRelatedId, ledgerIds)
                .eq(BudgetLedgerSelfR::getBizType, DEFAULT_BIZ_TYPE);
        int deletedByRelatedIdCount = budgetLedgerSelfRMapper.delete(deleteWrapper);
        if (deletedByRelatedIdCount > 0) {
            log.info("========== 删除关联的预算流水关系（RELATED_ID IN）: 删除了 {} 条关系记录 ==========", deletedByRelatedIdCount);
        }
    }

    private BudgetClaimRespVo handleApplyCrossYearNoDeduction(BudgetClaimApplyParams params, ClaimApplyReqInfoParams applyInfo) {
        BudgetParams budgetParams = convertToBudgetParams(params);
        ReqInfoParams reqInfo = budgetParams.getReqInfo();
        LocalDateTime requestTime = BudgetQueryHelperService.parseEsbRequestTime(
                params.getEsbInfo() != null ? params.getEsbInfo().getRequestTime() : null);

        List<ExtDetailVo> extDetails = new ArrayList<>();
        if (reqInfo.getDetails() != null) {
            for (DetailDetailVo d : reqInfo.getDetails()) {
                ExtDetailVo ext = new ExtDetailVo();
                BeanUtils.copyProperties(d, ext);
                ext.setDocumentNo(reqInfo.getDocumentNo());
                extDetails.add(ext);
            }
        }
        List<BudgetLedger> existingLedgers = queryExistingLedgers(extDetails, DEFAULT_BIZ_TYPE);
        Map<String, BudgetLedger> existingMap = existingLedgers.stream()
                .collect(Collectors.toMap(l -> l.getBizCode() + "@" + l.getBizItemCode(), Function.identity(), (a, b) -> a));

        Set<String> retainedKeys = extDetails.stream()
                .map(ext -> ext.getDocumentNo() + "@" + ext.getDetailLineNo())
                .collect(Collectors.toSet());
        List<BudgetLedger> toDeleteLedgers = existingLedgers.stream()
                .filter(ledger -> !retainedKeys.contains(ledger.getBizCode() + "@" + ledger.getBizItemCode()))
                .collect(Collectors.toList());
        if (!toDeleteLedgers.isEmpty()) {
            List<BudgetLedgerHistory> histories = new ArrayList<>();
            Set<Long> deleteIds = new HashSet<>();
            for (BudgetLedger ledger : toDeleteLedgers) {
                BudgetLedgerHistory history = new BudgetLedgerHistory();
                BeanUtils.copyProperties(ledger, history);
                history.setId(identifierGenerator.nextId(history).longValue());
                history.setLedgerId(ledger.getId());
                history.setDeleted(Boolean.FALSE);
                histories.add(history);
                deleteIds.add(ledger.getId());
            }
            budgetLedgerHistoryMapper.insertBatch(histories);
            archiveAndDeleteClaimLedgers(deleteIds);
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
                existing.setActualYear(detail.getActualYear());
                existing.setActualMonth(detail.getActualMonth());
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
                ledger.setActualYear(detail.getActualYear());
                ledger.setActualMonth(detail.getActualMonth());
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

    private BudgetClaimRespVo handleDetailDeletedCrossYearNoRollback(BudgetClaimApplyParams params, ClaimApplyReqInfoParams applyInfo) {
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
        archiveAndDeleteClaimLedgers(deleteIds);

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

    private BudgetClaimRenewRespVo handleRejectedOrCancelledSkipRollback(String claimOrderNo, String documentStatus,
                                                                         BudgetClaimRenewParams params) {
        LambdaQueryWrapper<BudgetLedgerHead> headWrapper = new LambdaQueryWrapper<>();
        headWrapper.eq(BudgetLedgerHead::getBizCode, claimOrderNo)
                .eq(BudgetLedgerHead::getDeleted, Boolean.FALSE);
        BudgetLedgerHead existingHead = budgetLedgerHeadMapper.selectOne(headWrapper);
        if (existingHead == null) {
            throw new IllegalStateException("未找到对应的预算流水头，业务单号：" + claimOrderNo);
        }
        LambdaQueryWrapper<BudgetLedger> ledgerWrapper = new LambdaQueryWrapper<>();
        ledgerWrapper.eq(BudgetLedger::getBizCode, claimOrderNo)
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
            archiveAndDeleteClaimLedgers(deleteIds);
        }
        BudgetLedgerHeadHistory headHistory = new BudgetLedgerHeadHistory();
        BeanUtils.copyProperties(existingHead, headHistory);
        headHistory.setId(identifierGenerator.nextId(headHistory).longValue());
        headHistory.setLedgerHeadId(existingHead.getId());
        headHistory.setDeleted(Boolean.FALSE);
        budgetLedgerHeadHistoryMapper.insertBatch(Collections.singletonList(headHistory));
        budgetLedgerHeadMapper.deleteById(existingHead.getId());
        String msg = "REJECTED".equals(documentStatus) ? "预算付款/报销驳回成功"
                : "CANCELLED".equals(documentStatus) ? "预算付款/报销撤销成功" : "预算付款/报销处理成功";
        return buildSuccessResponse(params, msg);
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
    private BudgetLedgerHistoryMapper budgetLedgerHistoryMapper;

    @Resource
    private BudgetQuotaMapper budgetQuotaMapper;

    @Resource
    private BudgetBalanceMapper budgetBalanceMapper;

    @Resource
    private BudgetBalanceHistoryMapper budgetBalanceHistoryMapper;

    @Resource
    private BudgetQuotaHistoryMapper budgetQuotaHistoryMapper;

    @Resource
    private BudgetPoolDemRMapper budgetPoolDemRMapper;

    @Resource
    private BudgetLedgerSelfRMapper budgetLedgerSelfRMapper;

    @Resource
    private BudgetQueryHelperService budgetQueryHelperService;

    @Resource
    private BudgetLedgerSelfRHistoryMapper budgetLedgerSelfRHistoryMapper;

    @Resource
    private IdentifierGenerator identifierGenerator;

    @Override
    public BudgetClaimRespVo apply(BudgetClaimApplyParams budgetClaimApplyParams) {
        log.info("开始处理预算付款/报销申请，params={}", budgetClaimApplyParams);
        try {
            // 整单级别校验：校验申请单状态
            ClaimApplyReqInfoParams claimApplyReqInfo = budgetClaimApplyParams.getClaimApplyReqInfo();
            if (claimApplyReqInfo == null) {
                throw new IllegalArgumentException("付款/报销申请信息不能为空");
            }
            String documentStatus = claimApplyReqInfo.getDocumentStatus();
            // DETAIL_DELETED 分支不走“初始提交/变更提交”校验（允许已审批通过后按明细删除）
            // 兼容历史调用可能传入的 "DETAIL-DELETED"
            boolean isDetailDeleted = "DETAIL_DELETED".equals(documentStatus)
                    || "DETAIL-DELETED".equalsIgnoreCase(documentStatus);
            String claimYear = extractClaimYear(claimApplyReqInfo.getClaimDetails());
            if (isDetailDeleted && StringUtils.isNotBlank(claimYear) && !isCurrentYear(claimYear)) {
                log.info("预算付款/报销明细删除跨年跳过预算回滚，claimOrderNo={}, year={}, currentYear={}",
                        claimApplyReqInfo.getClaimOrderNo(), claimYear, LocalDate.now().getYear());
                return handleDetailDeletedCrossYearNoRollback(budgetClaimApplyParams, claimApplyReqInfo);
            }
            if (!isDetailDeleted) {
                if (!INITIAL_SUBMITTED.equals(documentStatus) && !APPROVED_UPDATE.equals(documentStatus)) {
                    throw new IllegalArgumentException("申请单状态必须为 INITIAL_SUBMITTED 或 APPROVED_UPDATE，当前状态：" + documentStatus);
                }
                // 校验：已审批通过的付款/报销申请单不允许再次提交
                if (StringUtils.isNotBlank(claimApplyReqInfo.getClaimOrderNo())) {
                    LambdaQueryWrapper<BudgetLedgerHead> headCheckWrapper = new LambdaQueryWrapper<>();
                    headCheckWrapper.eq(BudgetLedgerHead::getBizCode, claimApplyReqInfo.getClaimOrderNo())
                            .eq(BudgetLedgerHead::getBizType, DEFAULT_BIZ_TYPE)
                            .eq(BudgetLedgerHead::getDeleted, Boolean.FALSE);
                    BudgetLedgerHead existingHead = budgetLedgerHeadMapper.selectOne(headCheckWrapper);
                    if (existingHead != null && "APPROVED".equals(existingHead.getStatus())) {
                        throw new IllegalArgumentException("付款/报销申请单已审批通过，不允许再次提交，单号：" + claimApplyReqInfo.getClaimOrderNo());
                    }
                }
                if (StringUtils.isNotBlank(claimYear) && !isCurrentYear(claimYear)) {
                    log.info("预算付款/报销跨年提交跳过预算校验与扣减，claimOrderNo={}, year={}, currentYear={}",
                            claimApplyReqInfo.getClaimOrderNo(), claimYear, LocalDate.now().getYear());
                    return handleApplyCrossYearNoDeduction(budgetClaimApplyParams, claimApplyReqInfo);
                }
            }

            BudgetParams budgetParams = convertToBudgetParams(budgetClaimApplyParams);
            
            // 调用父类通用处理逻辑
            BudgetRespVo respVo = superApply(budgetParams, DEFAULT_BIZ_TYPE);
            
            // 将通用的 BudgetRespVo 转换为 BudgetClaimRespVo
            return convertToBudgetClaimRespVo(respVo);
        } catch (DetailValidationException e) {
            // 明细级别的错误（由父类superApply抛出）
            log.error("部分明细处理失败", e);
            return buildResponseWithDetailErrors(budgetClaimApplyParams, 
                    e.getDetailValidationResultMap(), e.getDetailValidationMessageMap());
        } catch (IllegalArgumentException e) {
            // 业务参数校验错误（如科目编码映射不存在等），作为整单错误处理
            log.error("预算付款/报销申请参数校验失败", e);
            return buildErrorResponseForAllDetails(budgetClaimApplyParams, e);
        } catch (IllegalStateException e) {
            // 业务状态错误，作为整单错误处理
            log.error("预算付款/报销申请处理失败", e);
            return buildErrorResponseForAllDetails(budgetClaimApplyParams, e);
        } catch (Exception e) {
            // 其他异常，作为整单错误处理
            log.error("预算付款/报销申请处理失败", e);
            return buildErrorResponseForAllDetails(budgetClaimApplyParams, e);
        }
    }

    /**
     * 将 BudgetClaimApplyParams 转换为 BudgetParams
     *
     * @param budgetClaimApplyParams 预算付款/报销申请参数
     * @return BudgetParams 预算参数
     */
    private BudgetParams convertToBudgetParams(BudgetClaimApplyParams budgetClaimApplyParams) {
        BudgetParams budgetParams = new BudgetParams();
        budgetParams.setEsbInfo(budgetClaimApplyParams.getEsbInfo());

        ClaimApplyReqInfoParams claimApplyReqInfo = budgetClaimApplyParams.getClaimApplyReqInfo();
        ReqInfoParams reqInfo = new ReqInfoParams();
        
        // 字段映射：claimOrderNo -> documentNo
        reqInfo.setDocumentNo(claimApplyReqInfo.getClaimOrderNo());
        
        // 直接复制的字段
        BeanUtils.copyProperties(claimApplyReqInfo, reqInfo, "claimOrderNo", "claimDetails");

        // 转换 claimDetails 为 details
        List<DetailDetailVo> details = new ArrayList<>();
        if (claimApplyReqInfo.getClaimDetails() != null) {
            for (ClaimDetailDetailVo claimDetail : claimApplyReqInfo.getClaimDetails()) {
                DetailDetailVo detail = convertClaimDetailToDetail(claimDetail);
                details.add(detail);
            }
        }
        reqInfo.setDetails(details);
        budgetParams.setReqInfo(reqInfo);
        
        return budgetParams;
    }

    /**
     * 将 ClaimDetailDetailVo 转换为 DetailDetailVo
     *
     * @param claimDetail 付款/报销明细
     * @return DetailDetailVo 明细信息
     */
    private DetailDetailVo convertClaimDetailToDetail(ClaimDetailDetailVo claimDetail) {
        DetailDetailVo detail = new DetailDetailVo();
        
        // 字段映射：claimDetailLineNo -> detailLineNo, claimYear -> year, 
        // claimMonth -> month, actualAmount -> amount
        detail.setDetailLineNo(claimDetail.getClaimDetailLineNo());
        detail.setYear(claimDetail.getClaimYear());
        detail.setMonth(claimDetail.getClaimMonth());
        detail.setActualYear(claimDetail.getActualYear());
        detail.setActualMonth(claimDetail.getActualMonth());
        detail.setAmount(claimDetail.getActualAmount());
        
        // 直接复制的字段
        BeanUtils.copyProperties(claimDetail, detail, "claimDetailLineNo", "claimYear", 
                "claimMonth", "actualAmount", "claimApplyDetails", "claimContractDetails", "metadata");

        // 转换 metadata: Map<String, String> -> JSON 字符串
        if (claimDetail.getMetadata() != null && !claimDetail.getMetadata().isEmpty()) {
            String metadataJson = convertMapToJson(claimDetail.getMetadata());
            detail.setMetadata(metadataJson);
        }

        // 转换 claimApplyDetails 为 applyDetails
        List<SubDetailVo> applyDetails = new ArrayList<>();
        if (claimDetail.getClaimApplyDetails() != null) {
            for (ClaimApplyDetailVo claimApplyDetail : claimDetail.getClaimApplyDetails()) {
                SubDetailVo subDetail = new SubDetailVo();
                subDetail.setDocumentNo(claimApplyDetail.getDemandOrderNo());
                subDetail.setDetailLineNo(claimApplyDetail.getDemandDetailLineNo());
                applyDetails.add(subDetail);
            }
        }
        detail.setApplyDetails(applyDetails);

        // 转换 claimContractDetails 为 contractDetails
        List<SubDetailVo> contractDetails = new ArrayList<>();
        if (claimDetail.getClaimContractDetails() != null) {
            for (ClaimContractDetailVo claimContractDetail : claimDetail.getClaimContractDetails()) {
                SubDetailVo subDetail = new SubDetailVo();
                subDetail.setDocumentNo(claimContractDetail.getContractNo());
                subDetail.setDetailLineNo(claimContractDetail.getContractDetailLineNo());
                contractDetails.add(subDetail);
            }
        }
        detail.setContractDetails(contractDetails);
        
        return detail;
    }

    /**
     * 将 BudgetRespVo 转换为 BudgetClaimRespVo
     *
     * @param respVo 预算响应VO
     * @return BudgetClaimRespVo 预算付款/报销响应VO
     */
    private BudgetClaimRespVo convertToBudgetClaimRespVo(BudgetRespVo respVo) {
        BudgetClaimRespVo claimRespVo = new BudgetClaimRespVo();
        claimRespVo.setEsbInfo(respVo.getEsbInfo());
        
        // 转换 resultInfo 为 claimApplyResult
        if (respVo.getResultInfo() != null) {
            ClaimApplyResultInfoRespVo claimApplyResult = new ClaimApplyResultInfoRespVo();
            claimApplyResult.setClaimOrderNo(respVo.getResultInfo().getDocumentNo());
            claimApplyResult.setProcessTime(respVo.getResultInfo().getProcessTime());
            
            // 转换 details 为 claimDetails（使用ClaimDetailRespVo）
            List<ClaimDetailRespVo> claimDetails = new ArrayList<>();
            if (respVo.getResultInfo().getDetails() != null) {
                for (DetailDetailVo detail : respVo.getResultInfo().getDetails()) {
                    ClaimDetailRespVo claimDetail = new ClaimDetailRespVo();
                    ClaimDetailDetailVo baseDetail = convertDetailToClaimDetail(detail);
                    BeanUtils.copyProperties(baseDetail, claimDetail);
                    
                    // 显式复制 metadata，确保 Map 类型字段被正确复制
                    if (baseDetail.getMetadata() != null) {
                        claimDetail.setMetadata(new HashMap<>(baseDetail.getMetadata()));
                    }
                    
                    // 设置明细级别的校验结果
                    claimDetail.setValidationResult(detail.getValidationResult());
                    claimDetail.setValidationMessage(detail.getValidationMessage());
                    
                    // 设置预算相关数值字段
                    claimDetail.setAvailableBudgetRatio(detail.getAvailableBudgetRatio());
                    claimDetail.setAmountQuota(detail.getAmountQuota());
                    claimDetail.setAmountFrozen(detail.getAmountFrozen());
                    claimDetail.setAmountActual(detail.getAmountActual());
                    claimDetail.setAmountAvailable(detail.getAmountAvailable());
                    
                    claimDetails.add(claimDetail);
                }
            }
            claimApplyResult.setClaimDetails(claimDetails);
            claimRespVo.setClaimApplyResult(claimApplyResult);
        }
        
        return claimRespVo;
    }

    /**
     * 将 DetailDetailVo 转换为 ClaimDetailDetailVo
     *
     * @param detail 明细信息
     * @return ClaimDetailDetailVo 付款/报销明细
     */
    private ClaimDetailDetailVo convertDetailToClaimDetail(DetailDetailVo detail) {
        ClaimDetailDetailVo claimDetail = new ClaimDetailDetailVo();
        
        // 字段映射：detailLineNo -> claimDetailLineNo, year -> claimYear, 
        // month -> claimMonth, amount -> actualAmount
        claimDetail.setClaimDetailLineNo(detail.getDetailLineNo());
        claimDetail.setClaimYear(detail.getYear());
        claimDetail.setClaimMonth(detail.getMonth());
        claimDetail.setActualYear(detail.getActualYear());
        claimDetail.setActualMonth(detail.getActualMonth());
        claimDetail.setActualAmount(detail.getAmount());
        
        // 直接复制的字段
        BeanUtils.copyProperties(detail, claimDetail, "detailLineNo", "year", 
                "month", "amount", "applyDetails", "contractDetails");

        // 转换 applyDetails 为 claimApplyDetails
        List<ClaimApplyDetailVo> claimApplyDetails = new ArrayList<>();
        if (detail.getApplyDetails() != null) {
            for (SubDetailVo subDetail : detail.getApplyDetails()) {
                ClaimApplyDetailVo claimApplyDetail = new ClaimApplyDetailVo();
                claimApplyDetail.setDemandOrderNo(subDetail.getDocumentNo());
                // demandDetailLineNo 会自动从父级计算，这里不需要设置
                // 设置父级引用，以便自动计算 demandDetailLineNo
                claimApplyDetail.setParentDetail(claimDetail);
                claimApplyDetails.add(claimApplyDetail);
            }
        }
        claimDetail.setClaimApplyDetails(claimApplyDetails);

        // 转换 contractDetails 为 claimContractDetails
        List<ClaimContractDetailVo> claimContractDetails = new ArrayList<>();
        if (detail.getContractDetails() != null) {
            for (SubDetailVo subDetail : detail.getContractDetails()) {
                ClaimContractDetailVo claimContractDetail = new ClaimContractDetailVo();
                claimContractDetail.setContractNo(subDetail.getDocumentNo());
                // contractDetailLineNo 会自动从父级计算，这里不需要设置
                // 设置父级引用，以便自动计算 contractDetailLineNo
                claimContractDetail.setParentDetail(claimDetail);
                claimContractDetails.add(claimContractDetail);
            }
        }
        claimDetail.setClaimContractDetails(claimContractDetails);
        
        // 转换 metadata: JSON 字符串 -> Map<String, String>
        if (StringUtils.isNotBlank(detail.getMetadata())) {
            Map<String, String> metadataMap = parseJsonToMap(detail.getMetadata());
            claimDetail.setMetadata(metadataMap);
        }
        
        return claimDetail;
    }

    /**
     * 构建整单错误响应（所有明细都报同样的错）
     *
     * @param budgetClaimApplyParams 预算付款/报销申请参数
     * @param e 异常
     * @return 响应VO
     */
    private BudgetClaimRespVo buildErrorResponseForAllDetails(BudgetClaimApplyParams budgetClaimApplyParams, Exception e) {
        ESBInfoParams esbInfo = budgetClaimApplyParams.getEsbInfo();
        ClaimApplyReqInfoParams claimApplyReqInfo = budgetClaimApplyParams.getClaimApplyReqInfo();
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
        Set<String> managementOrgSet = claimApplyReqInfo != null && claimApplyReqInfo.getClaimDetails() != null 
            ? claimApplyReqInfo.getClaimDetails().stream()
                .map(ClaimDetailDetailVo::getManagementOrg)
                .filter(StringUtils::isNotBlank)
                .collect(Collectors.toSet())
            : Collections.emptySet();
        Set<String> budgetSubjectCodeSet = claimApplyReqInfo != null && claimApplyReqInfo.getClaimDetails() != null 
            ? claimApplyReqInfo.getClaimDetails().stream()
                .map(ClaimDetailDetailVo::getBudgetSubjectCode)
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
                .returnCode("E0001-CLAIM")
                .returnMsg(errorMessage)
                .returnStatus(returnStatus)
                .responseTime(responseTime)
                .build();

        // 构建返回的付款明细列表（所有明细都标记为失败）
        List<ClaimDetailRespVo> resultClaimDetails = new ArrayList<>();
        if (claimApplyReqInfo != null && claimApplyReqInfo.getClaimDetails() != null) {
            for (ClaimDetailDetailVo detail : claimApplyReqInfo.getClaimDetails()) {
                ClaimDetailRespVo resultDetail = new ClaimDetailRespVo();
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
                resultClaimDetails.add(resultDetail);
            }
        }

        ClaimApplyResultInfoRespVo resultInfo = new ClaimApplyResultInfoRespVo();
        resultInfo.setClaimOrderNo(claimApplyReqInfo != null ? claimApplyReqInfo.getClaimOrderNo() : null);
        resultInfo.setProcessTime(LocalDateTime.now().format(PROCESS_TIME_FORMATTER));
        resultInfo.setClaimDetails(resultClaimDetails);

        BudgetClaimRespVo respVo = new BudgetClaimRespVo();
        respVo.setEsbInfo(esbRespInfo);
        respVo.setClaimApplyResult(resultInfo);
        return respVo;
    }

    /**
     * 构建明细级别错误响应（部分明细成功，部分明细失败）
     */
    private BudgetClaimRespVo buildResponseWithDetailErrors(BudgetClaimApplyParams budgetClaimApplyParams,
                                                           Map<String, String> detailValidationResultMap,
                                                           Map<String, String> detailValidationMessageMap) {
        ESBInfoParams esbInfo = budgetClaimApplyParams.getEsbInfo();
        ClaimApplyReqInfoParams claimApplyReqInfo = budgetClaimApplyParams.getClaimApplyReqInfo();
        
        String instId = esbInfo != null ? esbInfo.getInstId() : null;
        if (instId == null || instId.isEmpty()) {
            String uuid = UUID.randomUUID().toString().replace("-", "");
            instId = "N" + uuid.substring(0, 8) + "." + uuid.substring(8, 16) + ".N"
                    + uuid.substring(16, 18) + "." + uuid.substring(18, 28) + ".N" + uuid.substring(28, 32);
        }
        String responseTime = LocalDateTime.now().format(DATE_TIME_FORMATTER);
        
        // 提取组织和科目编码，查询名称映射
        Set<String> managementOrgSet = claimApplyReqInfo != null && claimApplyReqInfo.getClaimDetails() != null 
            ? claimApplyReqInfo.getClaimDetails().stream()
                .map(ClaimDetailDetailVo::getManagementOrg)
                .filter(StringUtils::isNotBlank)
                .collect(Collectors.toSet())
            : Collections.emptySet();
        Set<String> budgetSubjectCodeSet = claimApplyReqInfo != null && claimApplyReqInfo.getClaimDetails() != null 
            ? claimApplyReqInfo.getClaimDetails().stream()
                .map(ClaimDetailDetailVo::getBudgetSubjectCode)
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
                .returnCode(hasError ? "E0001-CLAIM" : "A0001-CLAIM")
                .returnMsg(hasError ? "部分明细处理失败" : "预算付款/报销处理成功")
                .returnStatus(returnStatus)
                .responseTime(responseTime)
                .build();

        // 构建返回的付款明细列表（包含每个明细的校验结果）
        List<ClaimDetailRespVo> resultClaimDetails = new ArrayList<>();
        if (claimApplyReqInfo != null && claimApplyReqInfo.getClaimDetails() != null) {
            for (ClaimDetailDetailVo detail : claimApplyReqInfo.getClaimDetails()) {
                ClaimDetailRespVo resultDetail = new ClaimDetailRespVo();
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
                
                // 设置明细级别的校验结果（使用 claimDetailLineNo 作为 key）
                String detailLineNo = detail.getClaimDetailLineNo();
                resultDetail.setValidationResult(detailValidationResultMap.getOrDefault(detailLineNo, "1"));
                resultDetail.setValidationMessage(detailValidationMessageMap.getOrDefault(detailLineNo, "未处理"));
                
                resultClaimDetails.add(resultDetail);
            }
        }

        ClaimApplyResultInfoRespVo resultInfo = new ClaimApplyResultInfoRespVo();
        resultInfo.setClaimOrderNo(claimApplyReqInfo != null ? claimApplyReqInfo.getClaimOrderNo() : null);
        resultInfo.setProcessTime(LocalDateTime.now().format(PROCESS_TIME_FORMATTER));
        resultInfo.setClaimDetails(resultClaimDetails);

        BudgetClaimRespVo respVo = new BudgetClaimRespVo();
        respVo.setEsbInfo(esbRespInfo);
        respVo.setClaimApplyResult(resultInfo);
        return respVo;
    }

    @Override
    public BudgetClaimRenewRespVo authOrCancel(BudgetClaimRenewParams budgetClaimRenewParams) {
        log.info("开始处理预算付款/报销审批/撤回，params={}", budgetClaimRenewParams);

        try {
            ClaimRenewReqInfoParams claimRenewReqInfo = budgetClaimRenewParams.getClaimRenewReqInfo();
            String claimOrderNo = claimRenewReqInfo.getClaimOrderNo();
            String documentStatus = claimRenewReqInfo.getDocumentStatus();
            LocalDateTime requestTime = BudgetQueryHelperService.parseEsbRequestTime(
                    budgetClaimRenewParams.getEsbInfo() != null ? budgetClaimRenewParams.getEsbInfo().getRequestTime() : null);
            String renewYear = resolveRenewYear(claimOrderNo, claimRenewReqInfo);
            if (StringUtils.isNotBlank(renewYear) && !isCurrentYear(renewYear)
                    && ("REJECTED".equals(documentStatus) || "CANCELLED".equals(documentStatus))) {
                log.info("预算付款/报销跨年跳过回滚，仅执行删除，claimOrderNo={}, documentStatus={}, year={}, currentYear={}",
                        claimOrderNo, documentStatus, renewYear, LocalDate.now().getYear());
                return handleRejectedOrCancelledSkipRollback(claimOrderNo, documentStatus, budgetClaimRenewParams);
            }
            if ("APPROVED".equals(documentStatus)) {
                // 场景一：APPROVED
                return handleApproved(claimOrderNo, budgetClaimRenewParams, requestTime);
                
            } else if ("REJECTED".equals(documentStatus) || "CANCELLED".equals(documentStatus)) {
                // 场景二：REJECTED 或 CANCELLED
                return handleRejectedOrCancelled(claimOrderNo, documentStatus, budgetClaimRenewParams);
                
            } else {
                // 场景三：其他状态报错
                throw new IllegalArgumentException("不支持的单据状态：" + documentStatus);
            }
        } catch (IllegalArgumentException e) {
            // 业务参数校验错误，接口调用成功，返回 "S"
            log.error("预算付款/报销审批/撤回参数校验失败", e);
            return buildErrorResponse(budgetClaimRenewParams, e, true);
        } catch (IllegalStateException e) {
            // 业务状态错误，接口调用成功，返回 "S"
            log.error("预算付款/报销审批/撤回业务处理失败", e);
            return buildErrorResponse(budgetClaimRenewParams, e, true);
        } catch (Exception e) {
            // 系统异常，接口调用失败，返回 "F"
            log.error("预算付款/报销审批/撤回处理失败", e);
            return buildErrorResponse(budgetClaimRenewParams, e, false);
        }
    }

    /**
     * 构建错误响应
     *
     * @param budgetClaimRenewParams 预算付款/报销审批/撤回参数
     * @param e 异常对象
     * @param isBusinessException 是否为业务异常（true：业务异常，接口调用成功，返回 "S"；false：系统异常，接口调用失败，返回 "F"）
     * @return 响应VO
     */
    private BudgetClaimRenewRespVo buildErrorResponse(BudgetClaimRenewParams budgetClaimRenewParams, Exception e, boolean isBusinessException) {
        String errorMessage = StringUtils.isNotBlank(e.getMessage()) ? e.getMessage() : e.getClass().getSimpleName();
        String currentTime = LocalDateTime.now().format(DATE_TIME_FORMATTER);
        String instId = budgetClaimRenewParams != null && budgetClaimRenewParams.getEsbInfo() != null ? 
                budgetClaimRenewParams.getEsbInfo().getInstId() : null;
        
        // 统一返回 S（部分失败也返回 S）
        // 注意：审批/撤回参数中没有 dataSource 字段，默认使用 HLY 行为（保持原状）
        String returnStatus = "S";
        
        ESBRespInfoVo esbInfo = ESBRespInfoVo.builder()
                .instId(instId)
                .requestTime(budgetClaimRenewParams != null && budgetClaimRenewParams.getEsbInfo() != null ? 
                        budgetClaimRenewParams.getEsbInfo().getRequestTime() : null)
                .attr1(budgetClaimRenewParams != null && budgetClaimRenewParams.getEsbInfo() != null ? 
                        budgetClaimRenewParams.getEsbInfo().getAttr1() : null)
                .attr2(budgetClaimRenewParams != null && budgetClaimRenewParams.getEsbInfo() != null ? 
                        budgetClaimRenewParams.getEsbInfo().getAttr2() : null)
                .attr3(budgetClaimRenewParams != null && budgetClaimRenewParams.getEsbInfo() != null ? 
                        budgetClaimRenewParams.getEsbInfo().getAttr3() : null)
                .returnCode("E0001-CLAIM")
                .returnMsg(errorMessage)
                .returnStatus(returnStatus)
                .responseTime(currentTime)
                .build();

        BudgetClaimRenewRespVo response = new BudgetClaimRenewRespVo();
        response.setEsbInfo(esbInfo);
        return response;
    }

    /**
     * 构建成功响应
     *
     * @param budgetClaimRenewParams 预算付款/报销审批/撤回参数
     * @param message 成功消息
     * @return 预算付款/报销审批/撤回响应
     */
    private BudgetClaimRenewRespVo buildSuccessResponse(BudgetClaimRenewParams budgetClaimRenewParams, String message) {
        String currentTime = LocalDateTime.now().format(DATE_TIME_FORMATTER);
        String instId = budgetClaimRenewParams != null && budgetClaimRenewParams.getEsbInfo() != null ? 
                budgetClaimRenewParams.getEsbInfo().getInstId() : null;
        
        ESBRespInfoVo esbInfo = ESBRespInfoVo.builder()
                .instId(instId)
                .requestTime(budgetClaimRenewParams != null && budgetClaimRenewParams.getEsbInfo() != null ? 
                        budgetClaimRenewParams.getEsbInfo().getRequestTime() : null)
                .attr1(budgetClaimRenewParams != null && budgetClaimRenewParams.getEsbInfo() != null ? 
                        budgetClaimRenewParams.getEsbInfo().getAttr1() : null)
                .attr2(budgetClaimRenewParams != null && budgetClaimRenewParams.getEsbInfo() != null ? 
                        budgetClaimRenewParams.getEsbInfo().getAttr2() : null)
                .attr3(budgetClaimRenewParams != null && budgetClaimRenewParams.getEsbInfo() != null ? 
                        budgetClaimRenewParams.getEsbInfo().getAttr3() : null)
                .returnCode("A0001-CLAIM")
                .returnMsg(message)
                .returnStatus("S")
                .responseTime(currentTime)
                .build();

        BudgetClaimRenewRespVo response = new BudgetClaimRenewRespVo();
        response.setEsbInfo(esbInfo);
        return response;
    }

    /**
     * 处理 APPROVED 的场景
     *
     * @param claimOrderNo 付款/报销单号
     * @param budgetClaimRenewParams 预算付款/报销审批/撤回参数
     * @param requestTime ESB 请求时间，用于 HEAD 的 UPDATE_TIME
     * @return 预算付款/报销审批/撤回响应
     */
    private BudgetClaimRenewRespVo handleApproved(String claimOrderNo, BudgetClaimRenewParams budgetClaimRenewParams, LocalDateTime requestTime) {
        log.info("========== 场景一：APPROVED，claimOrderNo={} ==========", claimOrderNo);
        
        LambdaQueryWrapper<BudgetLedgerHead> headWrapper = new LambdaQueryWrapper<>();
        headWrapper.eq(BudgetLedgerHead::getBizCode, claimOrderNo)
                .eq(BudgetLedgerHead::getDeleted, Boolean.FALSE);
        BudgetLedgerHead existingHead = budgetLedgerHeadMapper.selectOne(headWrapper);
        
        if (existingHead == null) {
            throw new IllegalStateException("未找到对应的预算流水头，业务单号：" + claimOrderNo);
        }

        existingHead.setStatus("APPROVED");
        existingHead.setVersion(String.valueOf(identifierGenerator.nextId(null)));
        if (requestTime != null) {
            existingHead.setUpdateTime(requestTime);
        }
        budgetLedgerHeadMapper.updateById(existingHead);

        log.info("========== 场景一处理完成 ==========");
        return buildSuccessResponse(budgetClaimRenewParams, "预算付款/报销审批成功");
    }

    /**
     * 处理 REJECTED 或 CANCELLED 的场景
     *
     * @param claimOrderNo 付款/报销单号
     * @param documentStatus 单据状态
     * @param budgetClaimRenewParams 预算付款/报销审批/撤回参数
     * @return 预算付款/报销审批/撤回响应
     */
    private BudgetClaimRenewRespVo handleRejectedOrCancelled(String claimOrderNo, String documentStatus, BudgetClaimRenewParams budgetClaimRenewParams) {
        log.info("========== 场景二：REJECTED 或 CANCELLED，claimOrderNo={}, documentStatus={} ==========", claimOrderNo, documentStatus);
        
        // 步骤一：查询 BUDGET_LEDGER_HEAD
        LambdaQueryWrapper<BudgetLedgerHead> headWrapper = new LambdaQueryWrapper<>();
        headWrapper.eq(BudgetLedgerHead::getBizCode, claimOrderNo)
                .eq(BudgetLedgerHead::getDeleted, Boolean.FALSE);
        BudgetLedgerHead existingHead = budgetLedgerHeadMapper.selectOne(headWrapper);
        
        if (existingHead == null) {
            throw new IllegalStateException("未找到对应的预算流水头，业务单号：" + claimOrderNo);
        }

        // 步骤二：查询 BUDGET_LEDGER
        LambdaQueryWrapper<BudgetLedger> ledgerWrapper = new LambdaQueryWrapper<>();
        ledgerWrapper.eq(BudgetLedger::getBizCode, claimOrderNo)
                .eq(BudgetLedger::getBizType, DEFAULT_BIZ_TYPE)
                .eq(BudgetLedger::getDeleted, Boolean.FALSE);
        List<BudgetLedger> allLedgers = budgetLedgerMapper.selectList(ledgerWrapper);

        // 构建 Map<String, BudgetLedger>，key 为 bizCode + "@" + bizItemCode
        Map<String, BudgetLedger> needToCancelBudgetLedgerMap = allLedgers.stream()
                .collect(Collectors.toMap(
                        ledger -> ledger.getBizCode() + "@" + ledger.getBizItemCode(),
                        Function.identity(),
                        (a, b) -> a
                ));
        
        log.info("========== 查询到 {} 条 BudgetLedger 需要回滚 ==========", allLedgers.size());

        // 步骤三：如果 needToCancelBudgetLedgerMap 为空，直接删除头
        if (needToCancelBudgetLedgerMap.isEmpty()) {
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
                msg = "预算付款/报销驳回成功";
            } else if ("CANCELLED".equals(documentStatus)) {
                msg = "预算付款/报销撤销成功";
            } else {
                msg = "预算付款/报销处理成功";
            }
            return buildSuccessResponse(budgetClaimRenewParams, msg);
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
        
        // 步骤四：过滤掉不受控的ledger，只对受控的ledger进行预算余额查询和回滚
        Map<String, BudgetLedger> controlledLedgerMap = new HashMap<>();
        for (Map.Entry<String, BudgetLedger> entry : needToCancelBudgetLedgerMap.entrySet()) {
            BudgetLedger ledger = entry.getValue();
            // 跳过不受控的ledger（不受控明细会跳过预算余额查询和更新）
            if (!isUncontrolledLedger(ledger, ehrCdToOrgCdExtMap, erpAcctCdToAcctCdExtMap)) {
                controlledLedgerMap.put(entry.getKey(), ledger);
            } else {
                log.info("========== handleRejectedOrCancelled - 不受控明细跳过预算余额查询: bizKey={}, morgCode={}, budgetSubjectCode={}, masterProjectCode={} ==========",
                        entry.getKey(), ledger.getMorgCode(), ledger.getBudgetSubjectCode(), ledger.getMasterProjectCode());
            }
        }
        
        // 使用 helper 方法查询所有季度的 BudgetQuota 和 BudgetBalance 和对应相关流水集合数据（只查询受控的ledger）
        BudgetQueryHelperService.BudgetQuotaBalanceResult result = 
                budgetQueryHelperService.queryQuotaAndBalanceByAllQuarters(controlledLedgerMap, ehrCdToOrgCdMap, erpAcctCdToAcctCdMap, erpAssetTypeToMemberCdMap);
        
        if (result.hasError()) {
            log.error("========== handleRejectedOrCancelled - queryQuotaAndBalanceByAllQuarters 失败, claimOrderNo={}, documentStatus={}, controlledLedgerCount={}, errorDetailKey={}, errorMessage={} ==========",
                    claimOrderNo, documentStatus, controlledLedgerMap.size(), result.getErrorDetailKey(), result.getErrorMessage());
            throw new IllegalStateException(String.format(
                    "驳回/撤销时查询预算额度与余额失败，明细[%s]：%s",
                    result.getErrorDetailKey() != null ? result.getErrorDetailKey() : "-",
                    result.getErrorMessage() != null ? result.getErrorMessage() : "未知原因"));
        }
        
        log.info("========== handleRejectedOrCancelled - queryQuotaAndBalanceByAllQuarters 成功, claimOrderNo={}, quotaMapSize={}, balanceMapSize={}, relatedLedgerBizKeyCount={} ==========",
                claimOrderNo,
                result.getQuotaMap() != null ? result.getQuotaMap().size() : 0,
                result.getBalanceMap() != null ? result.getBalanceMap().size() : 0,
                result.getRelatedBudgetLedgerMap() != null ? result.getRelatedBudgetLedgerMap().size() : 0);
        
        Map<String, BudgetQuota> needToRollBackBudgetQuotaMap = new HashMap<>();
        if (result.getQuotaMap() != null) {
            needToRollBackBudgetQuotaMap.putAll(result.getQuotaMap());
        }
        Map<String, BudgetBalance> needToRollBackBudgetBalanceMap = new HashMap<>();
        if (result.getBalanceMap() != null) {
            needToRollBackBudgetBalanceMap.putAll(result.getBalanceMap());
        }
        
        // 获取关联预算流水Map（用于判断是否有关联单据）
        Map<String, List<BudgetLedger>> relatedBudgetLedgerMap = new HashMap<>();
        if (result.getRelatedBudgetLedgerMap() != null) {
            relatedBudgetLedgerMap.putAll(result.getRelatedBudgetLedgerMap());
        }

        List<BudgetQuotaHistory> needToAddBudgetQuotaHistory = new ArrayList<>();
        List<BudgetBalanceHistory> needToAddBudgetBalanceHistory = new ArrayList<>();
        
        // 用于收集需要更新的关联预算流水
        Map<String, List<BudgetLedger>> updatedRelatedBudgetLedgerMap = new HashMap<>();

        // 步骤五：回滚 BudgetBalance 和 BudgetQuota
        // 遍历所有 ledger，执行跨季度回滚
        // 注意：使用 needToCancelBudgetLedgerMap.values() 而不是 allLedgers，避免重复处理相同的 bizKey
        // 如果数据库中存在重复的 ledger（相同的 bizCode 和 bizItemCode），needToCancelBudgetLedgerMap 会自动去重
        for (BudgetLedger ledger : needToCancelBudgetLedgerMap.values()) {
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
            
            // 使用 helper service 计算需要回滚的季度
            BudgetQueryHelperService.RollbackQuartersResult rollbackResult = budgetQueryHelperService.calculateRollbackQuarters(ledger);
            List<String> quartersToRollback = rollbackResult.getQuartersToRollback();
            Map<String, BigDecimal> quarterRollbackAmountMap = rollbackResult.getQuarterRollbackAmountMap();
            
            if (quartersToRollback.isEmpty()) {
                log.info("========== handleRejectedOrCancelled - 无需回滚: bizKey={} ==========", bizKey);
                continue;
            }
            
            // 对每个需要回滚的季度进行处理
            for (String rollbackQuarter : quartersToRollback) {
                BigDecimal rollbackAmount = quarterRollbackAmountMap.get(rollbackQuarter);
                String bizKeyQuarter = bizKey + "@" + rollbackQuarter;
                
                BudgetBalance balance;
                if (StringUtils.isNotBlank(ledger.getPoolDimensionKey())) {
                    balance = budgetQueryHelperService.selectBudgetBalanceByPoolDimensionKey(ledger.getPoolDimensionKey(), rollbackQuarter);
                    needToRollBackBudgetBalanceMap.put(bizKeyQuarter, balance);
                } else {
                    balance = needToRollBackBudgetBalanceMap.get(bizKeyQuarter);
                }
                if (balance == null) {
                    log.error("========== bizKeyQuarter={} 在 needToRollBackBudgetBalanceMap 中找不到对应的 balance ==========", bizKeyQuarter);
                    throw new IllegalStateException(
                        String.format("明细 [%s] 回滚时在季度 %s 未找到对应的预算余额。" +
                                     "请检查 BUDGET_POOL_DEM_R 表是否配置了该维度组合的预算。",
                                     bizKey, rollbackQuarter)
                    );
                }
                
                Long poolId = balance.getPoolId();

                // 释放实际金额（使用该季度的回滚金额）
                BudgetBalanceHistory balanceHistory = new BudgetBalanceHistory();
                BeanUtils.copyProperties(balance, balanceHistory);
                balanceHistory.setId(identifierGenerator.nextId(balanceHistory).longValue());
                balanceHistory.setBalanceId(balance.getId());
                balanceHistory.setDeleted(Boolean.FALSE);

                // 使用 rollbackBalanceAmountForSameDimension 方法来回滚，这样会检查关联单据并回滚到关联单据（handleRejectedOrCancelled 不汇总，传 null）
                Map<String, List<BudgetLedger>> returnedRelatedBudgetLedgerMap = rollbackBalanceAmountForSameDimension(
                        balance, balanceHistory, ledger, rollbackQuarter, rollbackAmount, poolId, relatedBudgetLedgerMap, null, null);
                
                // 收集更新后的关联预算流水
                if (returnedRelatedBudgetLedgerMap != null && !returnedRelatedBudgetLedgerMap.isEmpty()) {
                    updatedRelatedBudgetLedgerMap.putAll(returnedRelatedBudgetLedgerMap);
                }

                balance.setVersion(ledger.getVersion());
                needToAddBudgetBalanceHistory.add(balanceHistory);
            }
        }

        // 步骤六：新建 needToAddBudgetLedgerHistory 和 needToCancelBudgetLedgerSet
        // 用 needToCancelBudgetLedgerMap 组装 needToCancelBudgetLedgerSet
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

        // 步骤七：更新数据库（按 balanceId 去重，避免同一池子多 bizKeyQuarter 重复更新）
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
                // 去重（根据 id）：同一合同流水可能被多个付款明细引用，保留 amountAvailable 最大的那条（回滚后的最终状态）
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
            
            // 1. 删除 BUDGET_LEDGER_SELF_R 表中 ID IN needToCancelBudgetLedgerSet 的记录
            // bizType 传 null：与插入时一致（付款流水 id 为 SELF_R.id，biz_type 为关联单据类型如 CONTRACT/APPLY），避免只按 CLAIM 删导致框架合同关联行残留
            int deletedByIdCount = budgetLedgerSelfRMapper.deleteByIdsAndBizType(needToCancelBudgetLedgerSet, null);
            if (deletedByIdCount > 0) {
                log.info("========== 删除关联的预算流水关系（ID IN）: 删除了 {} 条关系记录 ==========", deletedByIdCount);
            }
            
            // 2. 删除 BUDGET_LEDGER_SELF_R 表中 RELATED_ID IN needToCancelBudgetLedgerSet 的记录
            LambdaQueryWrapper<BudgetLedgerSelfR> deleteWrapper = new LambdaQueryWrapper<>();
            deleteWrapper.in(BudgetLedgerSelfR::getRelatedId, needToCancelBudgetLedgerSet)
                    .eq(BudgetLedgerSelfR::getBizType, DEFAULT_BIZ_TYPE);
            int deletedByRelatedIdCount = budgetLedgerSelfRMapper.delete(deleteWrapper);
            if (deletedByRelatedIdCount > 0) {
                log.info("========== 删除关联的预算流水关系（RELATED_ID IN）: 删除了 {} 条关系记录 ==========", deletedByRelatedIdCount);
            }
        }
        if (!needToAddBudgetLedgerHistory.isEmpty()) {
            budgetLedgerHistoryMapper.insertBatch(needToAddBudgetLedgerHistory);
        }

        // 步骤八：处理 BudgetLedgerHead
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
            msg = "预算付款/报销驳回成功";
        } else if ("CANCELLED".equals(documentStatus)) {
            msg = "预算付款/报销撤销成功";
        } else {
            msg = "预算付款/报销处理成功";
        }
        return buildSuccessResponse(budgetClaimRenewParams, msg);
    }

    // ========== 实现 AbstractBudgetService 的抽象方法 ==========

    @Override
    protected List<BudgetLedger> queryExistingLedgers(List<ExtDetailVo> extDetailsForQuery, String type) {
        // 只处理 CLAIM 类型的查询
        if (!DEFAULT_BIZ_TYPE.equals(type)) {
            return Collections.emptyList();
        }
        
        // 将 ExtDetailVo 转换为 ClaimExtDetailVo
        List<ClaimExtDetailVo> claimExtDetailsForQuery = new ArrayList<>();
        for (ExtDetailVo extDetail : extDetailsForQuery) {
            ClaimExtDetailVo claimExtDetail = new ClaimExtDetailVo();
            // 字段映射
            claimExtDetail.setClaimOrderNo(extDetail.getDocumentNo());
            claimExtDetail.setClaimDetailLineNo(extDetail.getDetailLineNo());
            claimExtDetail.setClaimYear(extDetail.getYear());
            claimExtDetail.setClaimMonth(extDetail.getMonth());
            claimExtDetail.setCompany(extDetail.getCompany());
            claimExtDetail.setDepartment(extDetail.getDepartment());
            claimExtDetail.setManagementOrg(extDetail.getManagementOrg());
            claimExtDetail.setBudgetSubjectCode(extDetail.getBudgetSubjectCode());
            claimExtDetail.setBudgetSubjectName(extDetail.getBudgetSubjectName());
            claimExtDetail.setMasterProjectCode(extDetail.getMasterProjectCode());
            claimExtDetail.setMasterProjectName(extDetail.getMasterProjectName());
            claimExtDetail.setErpAssetType(extDetail.getErpAssetType());
            claimExtDetail.setActualAmount(extDetail.getAmount());
            claimExtDetail.setCurrency(extDetail.getCurrency());
            claimExtDetail.setAvailableBudgetRatio(extDetail.getAvailableBudgetRatio());
            claimExtDetailsForQuery.add(claimExtDetail);
        }
        
        // 查询预算流水：只根据 biz_code（claimOrderNo）查询，不限制 biz_item_code
        // 这样可以查询到同一个单据号下所有维度的记录，包括维度变化前的记录
        // AbstractBudgetService 会根据维度差异自动分类处理（维度一致更新 vs 维度不一致回滚）
        return budgetLedgerMapper.selectByClaimExtDetails(claimExtDetailsForQuery);
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
            log.info("预算付款命中科目000000且带项目，按不受控处理并跳过预算占用/回滚，bizKey={}@{}",
                    ledger.getBizCode(), ledger.getBizItemCode());
            return true;
        }
        return super.isUncontrolledLedger(ledger, ehrCdToOrgCdExtMap, erpAcctCdToAcctCdExtMap);
    }

    @Override
    protected void rollbackBalanceAmountForDiffDimension(BudgetBalance balance, BudgetBalanceHistory balanceHistory,
                                                         BudgetLedger ledger, String rollbackQuarter,
                                                         BigDecimal rollbackAmount, Long poolId) {
        BigDecimal amountPayAvailableBefore = balance.getAmountPayAvailable() == null ? BigDecimal.ZERO : balance.getAmountPayAvailable();
        BigDecimal amountAvailableBefore = balance.getAmountAvailable() == null ? BigDecimal.ZERO : balance.getAmountAvailable();
        BigDecimal amountActualBefore = balance.getAmountActual() == null ? BigDecimal.ZERO : balance.getAmountActual();
        
        log.info("========== processDiffDimensionRollback - 释放可用金额: poolId={}, bizItemCode={}, quarter={}, rollbackAmount={}, 释放前 amountPayAvailable={}, amountAvailable={}, amountActual={} ==========",
                poolId, ledger.getBizItemCode(), rollbackQuarter, rollbackAmount, amountPayAvailableBefore, amountAvailableBefore, amountActualBefore);
        
        // 判断是否是带项目的付款单或资产类型
        boolean isProjectQuery = !"NAN".equals(ledger.getMasterProjectCode());
        String erpAssetType = ledger.getErpAssetType();
        boolean isAssetTypeQuery = !"NAN".equals(erpAssetType);
        
        // 对于付款/报销申请（CLAIM），回滚逻辑与校验和扣减逻辑保持一致：
        // - 如果 bizKey 以 @NAN@NAN 结尾（erpAssetType 和项目编码同时为空），回滚 amountAvailable（采购额）
        // - 如果是带项目的付款单，不回滚 amountAvailable（采购额），只回滚 amountPayAvailable（付款额）
        // - 如果是资产类型预算，不回滚 amountAvailable（采购额），只回滚 amountPayAvailable（付款额），与项目单据一致
        // 重要：如果 amountAvailable 原本是 NULL（表示没有采购额科目），则不应该更新它，保持为 NULL
        if ("NAN".equals(erpAssetType) && "NAN".equals(ledger.getMasterProjectCode()) && balance.getAmountAvailable() != null) {
            // erpAssetType 和项目编码同时为 NAN，且 amountAvailable 不为 NULL，回滚 amountAvailable
            balance.setAmountAvailable(amountAvailableBefore.add(rollbackAmount));
            balance.setAmountAvailableVchanged(rollbackAmount);
            log.debug("========== processDiffDimensionRollback - @NAN@NAN情况，回滚 amountAvailable: quarter={}, rollbackAmount={} ==========",
                    rollbackQuarter, rollbackAmount);
        } else if (isProjectQuery || isAssetTypeQuery) {
            // 带项目的付款单或资产类型付款单，不回滚 amountAvailable（采购额）
            log.debug("========== processDiffDimensionRollback - 带项目的付款单或资产类型付款单，不回滚 amountAvailable: quarter={}, rollbackAmount={}, isProjectQuery={}, isAssetTypeQuery={} ==========",
                    rollbackQuarter, rollbackAmount, isProjectQuery, isAssetTypeQuery);
        } else {
            // amountAvailable 为 NULL，保持为 NULL，不更新 amountAvailableVchanged
            log.debug("========== processDiffDimensionRollback - amountAvailable 为 NULL，保持为 NULL，不进行回滚: quarter={}, rollbackAmount={} ==========",
                    rollbackQuarter, rollbackAmount);
        }
        
        // amountActual 要叠减（无论回滚哪个字段，amountActual 都要减少）
        // amountActual 是累积发生数，逻辑上不会小于0
        BigDecimal newAmountActual = amountActualBefore.subtract(rollbackAmount);
        balance.setAmountActual(newAmountActual);
        balance.setAmountActualVchanged(rollbackAmount.negate());
        
        // 对于付款/报销申请（CLAIM），回滚逻辑与校验和扣减逻辑保持一致：
        // - 如果 bizKey 以 @NAN@NAN 结尾，不回滚 amountPayAvailable（因为回滚的是 amountAvailable）
        // - 如果是带项目的付款单或资产类型预算，回滚 amountPayAvailable（付款额）
        if ("NAN".equals(erpAssetType) && "NAN".equals(ledger.getMasterProjectCode())) {
            // @NAN@NAN 情况，不回滚 amountPayAvailable（因为回滚的是 amountAvailable）
            balance.setAmountPayAvailableVchanged(BigDecimal.ZERO);
            log.debug("========== processDiffDimensionRollback - @NAN@NAN情况，不回滚 amountPayAvailable: quarter={}, rollbackAmount={} ==========",
                    rollbackQuarter, rollbackAmount);
        } else if (isProjectQuery || isAssetTypeQuery) {
            // 带项目的付款单或资产类型预算，回滚 amountPayAvailable（付款额）
            balance.setAmountPayAvailable(amountPayAvailableBefore.add(rollbackAmount));
            balance.setAmountPayAvailableVchanged(rollbackAmount);
            log.info("========== processDiffDimensionRollback - 叠加amountPayAvailable: 释放前 amountPayAvailable={}, 释放后 amountPayAvailable={}, isProjectQuery={}, isAssetTypeQuery={} ==========",
                    amountPayAvailableBefore, balance.getAmountPayAvailable(), isProjectQuery, isAssetTypeQuery);
        } else {
            // 理论上不会走到这里，但为了安全起见
            balance.setAmountPayAvailableVchanged(BigDecimal.ZERO);
        }
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
        // 付款单可能关联合同单(CONTRACT)或申请单(APPLY)，按关联流水的 bizType 查 selfR
        String relatedBizType = relatedLedgers.get(0).getBizType();
        if (relatedBizType == null) {
            relatedBizType = "CONTRACT";
        }
        Set<Long> relatedLedgerIds = relatedLedgers.stream().map(BudgetLedger::getId).filter(Objects::nonNull).collect(Collectors.toSet());
        List<BudgetLedgerSelfR> selfRList = budgetLedgerSelfRMapper.selectByRelatedIdsAndBizType(relatedLedgerIds, relatedBizType);
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
            } else if (!CollectionUtils.isEmpty(relatedLedgers)) {
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

    /**
     * 付款单仅回滚「资金池」侧占用（与无关联付款扣减字段一致）：部门+科目回滚 amountAvailable；项目/资产回滚 amountPayAvailable；并调整 amountActual。
     * 不改变 amountOccupied / amountFrozen。用于无关联流水及「框架协议 + 关联合同」场景（本单 amountConsumedQ* 仅含池子部分）。
     */
    private void rollbackClaimPoolOnlyOnBalance(BudgetBalance balance, BudgetLedger ledger, String rollbackQuarter,
                                                BigDecimal rollbackAmount, Long poolId) {
        BigDecimal amountPayAvailableBefore = balance.getAmountPayAvailable() == null ? BigDecimal.ZERO : balance.getAmountPayAvailable();
        BigDecimal amountAvailableBefore = balance.getAmountAvailable() == null ? BigDecimal.ZERO : balance.getAmountAvailable();
        BigDecimal amountActualBefore = balance.getAmountActual() == null ? BigDecimal.ZERO : balance.getAmountActual();
        boolean isProjectQuery = !"NAN".equals(ledger.getMasterProjectCode());
        String erpAssetType = ledger.getErpAssetType();
        boolean isAssetTypeQuery = !"NAN".equals(erpAssetType);

        log.info("========== processSameDimensionRollback - 释放金额（仅资金池，无 amountOccupied/冻结）: poolId={}, bizItemCode={}, quarter={}, rollbackAmount={}, 释放前 amountPayAvailable={}, amountAvailable={}, amountActual={}, isProjectQuery={} ==========",
                poolId, ledger.getBizItemCode(), rollbackQuarter, rollbackAmount, amountPayAvailableBefore, amountAvailableBefore, amountActualBefore, isProjectQuery);

        if ("NAN".equals(erpAssetType) && "NAN".equals(ledger.getMasterProjectCode()) && balance.getAmountAvailable() != null) {
            balance.setAmountAvailable(amountAvailableBefore.add(rollbackAmount));
            balance.setAmountAvailableVchanged(rollbackAmount);
            log.debug("========== processSameDimensionRollback - @NAN@NAN，回滚 amountAvailable: quarter={}, rollbackAmount={} ==========",
                    rollbackQuarter, rollbackAmount);
        } else if (isProjectQuery || isAssetTypeQuery) {
            log.debug("========== processSameDimensionRollback - 项目/资产，不回滚 amountAvailable: quarter={}, rollbackAmount={} ==========",
                    rollbackQuarter, rollbackAmount);
        } else {
            log.debug("========== processSameDimensionRollback - amountAvailable 为 NULL，保持 NULL: quarter={} ==========", rollbackQuarter);
        }

        BigDecimal newAmountActual = amountActualBefore.subtract(rollbackAmount);
        balance.setAmountActual(newAmountActual);
        balance.setAmountActualVchanged(rollbackAmount.negate());

        if ("NAN".equals(erpAssetType) && "NAN".equals(ledger.getMasterProjectCode())) {
            balance.setAmountPayAvailableVchanged(BigDecimal.ZERO);
            log.debug("========== processSameDimensionRollback - @NAN@NAN，不回滚 amountPayAvailable: quarter={} ==========", rollbackQuarter);
        } else if (isProjectQuery || isAssetTypeQuery) {
            balance.setAmountPayAvailable(amountPayAvailableBefore.add(rollbackAmount));
            balance.setAmountPayAvailableVchanged(rollbackAmount);
            log.info("========== processSameDimensionRollback - 回滚 amountPayAvailable: 释放前={}, 释放后={} ==========",
                    amountPayAvailableBefore, balance.getAmountPayAvailable());
        } else {
            balance.setAmountPayAvailableVchanged(BigDecimal.ZERO);
        }
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
        
        BigDecimal amountPayAvailableBefore = balance.getAmountPayAvailable() == null ? BigDecimal.ZERO : balance.getAmountPayAvailable();
        BigDecimal amountAvailableBefore = balance.getAmountAvailable() == null ? BigDecimal.ZERO : balance.getAmountAvailable();
        BigDecimal amountActualBefore = balance.getAmountActual() == null ? BigDecimal.ZERO : balance.getAmountActual();
        
        // 判断是否是带项目的付款单
        boolean isProjectQuery = !"NAN".equals(ledger.getMasterProjectCode());
        
        if (CollectionUtils.isEmpty(relatedLedgers)) {
            log.info("========== processSameDimensionRollback - 释放金额（无关联流水）: poolId={}, bizItemCode={}, quarter={}, rollbackAmount={}, 释放前 amountPayAvailable={}, amountAvailable={}, amountActual={}, erpAssetType={}, isProjectQuery={} ==========",
                    poolId, ledger.getBizItemCode(), rollbackQuarter, rollbackAmount, amountPayAvailableBefore, amountAvailableBefore, amountActualBefore, ledger.getErpAssetType(), isProjectQuery);
            rollbackClaimPoolOnlyOnBalance(balance, ledger, rollbackQuarter, rollbackAmount, poolId);
        } else {
            // 如果不为空，需要根据关联流水的 bizType 来决定处理逻辑
            // relatedBudgetLedgerMap 获取到的集合数据里默认第一个 BudgetLedger
            BudgetLedger firstRelatedLedger = relatedLedgers.get(0);
            String relatedBizType = firstRelatedLedger.getBizType();
            if (relatedBizType == null) {
                relatedBizType = CONTRACT_BIZ_TYPE;
            }
            
            // 判断 ledger 的 erpAssetType 是否为 NAN
            String erpAssetType = ledger.getErpAssetType();
            
            boolean isAssetTypeQuery = !"NAN".equals(erpAssetType);
            boolean frameworkContract = CONTRACT_BIZ_TYPE.equals(relatedBizType) && isFrameworkAgreementContract(relatedLedgers);
            if (frameworkContract) {
                // 框架协议：先回滚资金池部分（本单 amountConsumedQ*），再按 SELF_R 回滚“关联合同先扣”产生的占用->发生转换
                log.info("========== processSameDimensionRollback - 框架协议(CONTRACT+effectType=1): 本季 rollbackAmount 仅回滚资金池字段 quarter={}, rollbackAmount={} ==========",
                        rollbackQuarter, rollbackAmount);
                rollbackClaimPoolOnlyOnBalance(balance, ledger, rollbackQuarter, rollbackAmount, poolId);
                Set<Long> frameworkRelatedIds = relatedLedgers.stream()
                        .map(BudgetLedger::getId)
                        .filter(Objects::nonNull)
                        .collect(Collectors.toSet());
                if (!CollectionUtils.isEmpty(frameworkRelatedIds)) {
                    List<BudgetLedgerSelfR> frameworkSelfRList = budgetLedgerSelfRMapper.selectByRelatedIdsAndBizType(frameworkRelatedIds, CONTRACT_BIZ_TYPE);
                    BigDecimal frameworkRollbackAmount = frameworkSelfRList.stream()
                            .filter(selfR -> selfR.getId() != null && selfR.getRelatedId() != null)
                            .filter(selfR -> selfR.getId().equals(ledger.getId()))
                            .map(selfR -> getSelfRQuarterAmount(selfR, rollbackQuarter))
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                    if (frameworkRollbackAmount.compareTo(BigDecimal.ZERO) > 0) {
                        BigDecimal amountOccupiedBefore = balance.getAmountOccupied() == null ? BigDecimal.ZERO : balance.getAmountOccupied();
                        BigDecimal amountActualBeforeForFramework = balance.getAmountActual() == null ? BigDecimal.ZERO : balance.getAmountActual();
                        balance.setAmountOccupied(amountOccupiedBefore.add(frameworkRollbackAmount));
                        BigDecimal amountOccupiedVchanged = balance.getAmountOccupiedVchanged() == null ? BigDecimal.ZERO : balance.getAmountOccupiedVchanged();
                        balance.setAmountOccupiedVchanged(amountOccupiedVchanged.add(frameworkRollbackAmount));
                        balance.setAmountActual(amountActualBeforeForFramework.subtract(frameworkRollbackAmount));
                        BigDecimal amountActualVchanged = balance.getAmountActualVchanged() == null ? BigDecimal.ZERO : balance.getAmountActualVchanged();
                        balance.setAmountActualVchanged(amountActualVchanged.subtract(frameworkRollbackAmount));
                        if (isProjectQuery || isAssetTypeQuery) {
                            balance.setAmountPayAvailable(amountPayAvailableBefore.add(frameworkRollbackAmount));
                            BigDecimal amountPayAvailableVchanged = balance.getAmountPayAvailableVchanged() == null ? BigDecimal.ZERO : balance.getAmountPayAvailableVchanged();
                            balance.setAmountPayAvailableVchanged(amountPayAvailableVchanged.add(frameworkRollbackAmount));
                        }
                    }
                }
            } else if (CONTRACT_BIZ_TYPE.equals(relatedBizType)) {
                // 如果是 CONTRACT，回滚逻辑：amountOccupied（合同占用）增加；项目/资产类型同时回滚资金池付款额
                BigDecimal amountOccupiedBefore = balance.getAmountOccupied() == null ? BigDecimal.ZERO : balance.getAmountOccupied();
                
                log.info("========== processSameDimensionRollback - 释放金额（有关联流水-CONTRACT）: poolId={}, bizItemCode={}, quarter={}, rollbackAmount={}, 释放前 amountPayAvailable={}, amountOccupied={}, amountAvailable={}, amountActual={}, erpAssetType={}, 关联流水数量={}, isProjectQuery={} ==========",
                        poolId, ledger.getBizItemCode(), rollbackQuarter, rollbackAmount, amountPayAvailableBefore, amountOccupiedBefore, amountAvailableBefore, amountActualBefore, erpAssetType, relatedLedgers.size(), isProjectQuery);
                
                balance.setAmountOccupied(amountOccupiedBefore.add(rollbackAmount));
                balance.setAmountOccupiedVchanged(rollbackAmount);
                
                // amountOccupied 叠加时，amountActual 要叠减
                // amountActual 是累积发生数，逻辑上不会小于0
                BigDecimal newAmountActual = amountActualBefore.subtract(rollbackAmount);
                balance.setAmountActual(newAmountActual);
                balance.setAmountActualVchanged(rollbackAmount.negate());
                
                // 关联流水单的付款单（项目/资产类型）：提交时已扣减资金池付款额，回滚时需同时回滚 amountPayAvailable
                if (isProjectQuery || isAssetTypeQuery) {
                    balance.setAmountPayAvailable(amountPayAvailableBefore.add(rollbackAmount));
                    balance.setAmountPayAvailableVchanged(rollbackAmount);
                    log.info("========== processSameDimensionRollback - 有关联流水(CONTRACT)，回滚资金池付款额: quarter={}, rollbackAmount={}, 回滚后 amountPayAvailable={} ==========",
                            rollbackQuarter, rollbackAmount, balance.getAmountPayAvailable());
                }
            } else {
                // 如果是 APPLY，回滚逻辑：amountFrozen（申请冻结）增加；项目/资产类型同时回滚资金池付款额
                BigDecimal amountFrozenBefore = balance.getAmountFrozen() == null ? BigDecimal.ZERO : balance.getAmountFrozen();
                
                log.info("========== processSameDimensionRollback - 释放金额（有关联流水-APPLY）: poolId={}, bizItemCode={}, quarter={}, rollbackAmount={}, 释放前 amountPayAvailable={}, amountFrozen={}, amountAvailable={}, amountActual={}, erpAssetType={}, 关联流水数量={}, isProjectQuery={} ==========",
                        poolId, ledger.getBizItemCode(), rollbackQuarter, rollbackAmount, amountPayAvailableBefore, amountFrozenBefore, amountAvailableBefore, amountActualBefore, erpAssetType, relatedLedgers.size(), isProjectQuery);
                
                balance.setAmountFrozen(amountFrozenBefore.add(rollbackAmount));
                balance.setAmountFrozenVchanged(rollbackAmount);
                
                // amountFrozen 叠加时，amountActual 要叠减
                // amountActual 是累积发生数，逻辑上不会小于0
                BigDecimal newAmountActual = amountActualBefore.subtract(rollbackAmount);
                balance.setAmountActual(newAmountActual);
                balance.setAmountActualVchanged(rollbackAmount.negate());
                
                // 关联流水单的付款单（项目/资产类型）：提交时已扣减资金池付款额，回滚时需同时回滚 amountPayAvailable
                if (isProjectQuery || isAssetTypeQuery) {
                    balance.setAmountPayAvailable(amountPayAvailableBefore.add(rollbackAmount));
                    balance.setAmountPayAvailableVchanged(rollbackAmount);
                    log.info("========== processSameDimensionRollback - 有关联流水(APPLY)，回滚资金池付款额: quarter={}, rollbackAmount={}, 回滚后 amountPayAvailable={} ==========",
                            rollbackQuarter, rollbackAmount, balance.getAmountPayAvailable());
                }
            }
            
            // 使用 BUDGET_LEDGER_SELF_R 记录的金额精确回滚每个关联单据
            // 查询 BUDGET_LEDGER_SELF_R 表，获取每个关联单据的扣减金额（按季度）
            Set<Long> relatedLedgerIds = relatedLedgers.stream()
                    .map(BudgetLedger::getId)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());
            
            List<BudgetLedgerSelfR> selfRList = budgetLedgerSelfRMapper.selectByRelatedIdsAndBizType(relatedLedgerIds, relatedBizType);
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
                    // 从 BUDGET_LEDGER_SELF_R 中获取该关联单据的扣减金额（按季度）
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
            log.info("========== getBudgetValidationResult - 无关联流水，使用默认校验方式: bizKey={} ==========", bizKey);
            return new BudgetValidationResult(false, null, BigDecimal.ZERO);
        }
        
        // 如果不为空，计算金额合计：遍历 List<BudgetLedger> 下 BudgetLedger 下 amountAvailable 的合计
        BigDecimal totalAmount = BigDecimal.ZERO;
        String relatedBizType = null;
        boolean claimFrameworkContract = false;
        for (BudgetLedger ledger : relatedLedgers) {
            BigDecimal amountAvailable = ledger.getAmountAvailable() == null ? BigDecimal.ZERO : ledger.getAmountAvailable();
            totalAmount = totalAmount.add(amountAvailable);
            // 从第一个 ledger 获取 bizType
            if (relatedBizType == null && ledger.getBizType() != null) {
                relatedBizType = ledger.getBizType();
            }
            if (CONTRACT_BIZ_TYPE.equals(ledger.getBizType()) && FRAMEWORK_CONTRACT_EFFECT_TYPE.equals(ledger.getEffectType())) {
                claimFrameworkContract = true;
            }
        }
        
        log.info("========== getBudgetValidationResult - 发现关联流水，使用自定义校验方式: bizKey={}, 关联流水数量={}, 金额合计={}, relatedBizType={}, claimFrameworkContract={} ==========",
                bizKey, relatedLedgers.size(), totalAmount, relatedBizType, claimFrameworkContract);
        
        return new BudgetValidationResult(true, relatedBizType, totalAmount, claimFrameworkContract);
    }

    @Override
    protected BigDecimal getCurrentAmountOperated(List<BudgetBalance> balanceList) {
        // CLAIM 类型返回 amountActual 之和
        BigDecimal totalAmountActual = BigDecimal.ZERO;
        for (BudgetBalance balance : balanceList) {
            BigDecimal amountActual = balance.getAmountActual() == null ? BigDecimal.ZERO : balance.getAmountActual();
            totalAmountActual = totalAmountActual.add(amountActual);
        }
        return totalAmountActual;
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
     * 是否关联框架协议合同（CONTRACT + effectType=1）
     */
    private boolean isFrameworkAgreementContract(List<BudgetLedger> relatedLedgers) {
        if (CollectionUtils.isEmpty(relatedLedgers)) {
            return false;
        }
        for (BudgetLedger ledger : relatedLedgers) {
            if (ledger != null && CONTRACT_BIZ_TYPE.equals(ledger.getBizType())
                    && FRAMEWORK_CONTRACT_EFFECT_TYPE.equals(ledger.getEffectType())) {
                return true;
            }
        }
        return false;
    }

    /**
     * 框架协议：仅扣减关联合同流水的 amountAvailable（不扣资金池）。
     * 扣减写入 relatedLedgerDeductionAmountMap → {@code BUDGET_LEDGER_SELF_R}；不写入 quarterOperateAmountMap，
     * 以便本单 {@code amountConsumedQ*} 仅含后续从资金池各季扣减部分，与 SELF_R（纯关联流水消耗）不重复。
     */
    private BigDecimal deductFrameworkContractLedgersOnly(BigDecimal remainingAmount,
                                                         List<BudgetLedger> relatedLedgers,
                                                         Long currentLedgerId,
                                                         String attributedQuarter,
                                                         Map<String, BigDecimal> relatedLedgerDeductionAmountMap) {
        if (remainingAmount.compareTo(BigDecimal.ZERO) <= 0 || CollectionUtils.isEmpty(relatedLedgers)) {
            return remainingAmount;
        }
        BigDecimal denominator = BigDecimal.ZERO;
        for (BudgetLedger ledger : relatedLedgers) {
            denominator = denominator.add(ledger.getAmountAvailable() == null ? BigDecimal.ZERO : ledger.getAmountAvailable());
        }
        BigDecimal toDeduct = remainingAmount.min(denominator);
        if (toDeduct.compareTo(BigDecimal.ZERO) <= 0) {
            return remainingAmount;
        }
        BigDecimal remainingToAllocate = toDeduct;
        BigDecimal totalDeducted = BigDecimal.ZERO;
        for (int idx = 0; idx < relatedLedgers.size(); idx++) {
            BudgetLedger ledger = relatedLedgers.get(idx);
            BigDecimal av = ledger.getAmountAvailable() == null ? BigDecimal.ZERO : ledger.getAmountAvailable();
            if (av.compareTo(BigDecimal.ZERO) <= 0) {
                continue;
            }
            BigDecimal proportion = av.divide(denominator, 10, RoundingMode.HALF_UP);
            BigDecimal part = idx == relatedLedgers.size() - 1
                    ? remainingToAllocate.subtract(totalDeducted)
                    : remainingToAllocate.multiply(proportion).setScale(2, RoundingMode.HALF_UP);
            part = part.min(av);
            ledger.setAmountAvailable(av.subtract(part));
            totalDeducted = totalDeducted.add(part);
            if (currentLedgerId != null && ledger.getId() != null) {
                String deductionKey = currentLedgerId + "@" + ledger.getId() + "@" + attributedQuarter;
                relatedLedgerDeductionAmountMap.merge(deductionKey, part, BigDecimal::add);
            }
        }
        return remainingAmount.subtract(toDeduct);
    }

    /**
     * 框架协议先扣关联合同时，同步把资金池占用转为实际发生：
     * amountOccupied 叠减，amountActual 叠加；项目/资产维度同时扣减 amountPayAvailable。
     */
    private void applyFrameworkContractTransferOnBalance(String bizKey,
                                                         String currentQuarter,
                                                         BigDecimal frameworkDeductedAmount,
                                                         Map<String, List<BudgetBalance>> balanceMap,
                                                         Map<String, BudgetBalance> needToUpdateSameDemBudgetBalanceMap) {
        if (frameworkDeductedAmount == null || frameworkDeductedAmount.compareTo(BigDecimal.ZERO) <= 0) {
            return;
        }
        BigDecimal remaining = frameworkDeductedAmount;
        int currentQuarterNum = getQuarterNumber(currentQuarter);
        String[] quarters = {"q1", "q2", "q3", "q4"};
        boolean isOrgSubject = bizKey.endsWith("@NAN@NAN");
        for (int i = currentQuarterNum - 1; i >= 0 && remaining.compareTo(BigDecimal.ZERO) > 0; i--) {
            String quarter = quarters[i];
            String bizKeyQuarter = bizKey + "@" + quarter;
            BudgetBalance balance = needToUpdateSameDemBudgetBalanceMap.get(bizKeyQuarter);
            if (balance == null) {
                continue;
            }
            List<BudgetBalance> balanceList = balanceMap.get(bizKeyQuarter);
            BigDecimal totalAmountOccupiedFromList = BigDecimal.ZERO;
            if (!CollectionUtils.isEmpty(balanceList)) {
                for (BudgetBalance balanceItem : balanceList) {
                    BigDecimal amountOccupied = balanceItem.getAmountOccupied() == null ? BigDecimal.ZERO : balanceItem.getAmountOccupied();
                    totalAmountOccupiedFromList = totalAmountOccupiedFromList.add(amountOccupied);
                }
            } else {
                totalAmountOccupiedFromList = balance.getAmountOccupied() == null ? BigDecimal.ZERO : balance.getAmountOccupied();
            }
            BigDecimal amountToTransferThisQuarter = remaining.min(totalAmountOccupiedFromList);
            if (amountToTransferThisQuarter.compareTo(BigDecimal.ZERO) <= 0) {
                continue;
            }
            BigDecimal amountOccupiedBefore = balance.getAmountOccupied() == null ? BigDecimal.ZERO : balance.getAmountOccupied();
            BigDecimal amountActualBefore = balance.getAmountActual() == null ? BigDecimal.ZERO : balance.getAmountActual();
            balance.setAmountOccupied(amountOccupiedBefore.subtract(amountToTransferThisQuarter));
            BigDecimal amountOccupiedVchanged = balance.getAmountOccupiedVchanged() == null ? BigDecimal.ZERO : balance.getAmountOccupiedVchanged();
            balance.setAmountOccupiedVchanged(amountOccupiedVchanged.subtract(amountToTransferThisQuarter));
            balance.setAmountActual(amountActualBefore.add(amountToTransferThisQuarter));
            BigDecimal amountActualVchanged = balance.getAmountActualVchanged() == null ? BigDecimal.ZERO : balance.getAmountActualVchanged();
            balance.setAmountActualVchanged(amountActualVchanged.add(amountToTransferThisQuarter));
            if (!isOrgSubject) {
                BigDecimal amountPayAvailableBefore = balance.getAmountPayAvailable() == null ? BigDecimal.ZERO : balance.getAmountPayAvailable();
                balance.setAmountPayAvailable(amountPayAvailableBefore.subtract(amountToTransferThisQuarter));
                BigDecimal amountPayAvailableVchanged = balance.getAmountPayAvailableVchanged() == null ? BigDecimal.ZERO : balance.getAmountPayAvailableVchanged();
                balance.setAmountPayAvailableVchanged(amountPayAvailableVchanged.subtract(amountToTransferThisQuarter));
            }
            remaining = remaining.subtract(amountToTransferThisQuarter);
            markSubmitPoolBudgetBalanceTouched(bizKey);
        }
        if (remaining.compareTo(BigDecimal.ZERO) > 0) {
            throw new IllegalStateException(String.format(
                    "明细 [%s] 关联框架协议合同时，合同已扣减金额 %s 在资金池占用中无法完成转换，剩余未转换 %s。",
                    bizKey, frameworkDeductedAmount, remaining));
        }
    }

    private BigDecimal getSelfRQuarterAmount(BudgetLedgerSelfR selfR, String quarter) {
        if (selfR == null) {
            return BigDecimal.ZERO;
        }
        switch (quarter) {
            case "q1":
                return selfR.getAmountConsumedQOne() != null ? selfR.getAmountConsumedQOne() : BigDecimal.ZERO;
            case "q2":
                return selfR.getAmountConsumedQTwo() != null ? selfR.getAmountConsumedQTwo() : BigDecimal.ZERO;
            case "q3":
                return selfR.getAmountConsumedQThree() != null ? selfR.getAmountConsumedQThree() : BigDecimal.ZERO;
            case "q4":
                return selfR.getAmountConsumedQFour() != null ? selfR.getAmountConsumedQFour() : BigDecimal.ZERO;
            default:
                return BigDecimal.ZERO;
        }
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
        final boolean frameworkContractLedgerFirst = isFrameworkAgreementContract(relatedLedgers);
        if (frameworkContractLedgerFirst && !CollectionUtils.isEmpty(relatedLedgers)) {
            BigDecimal amountBeforeFrameworkDeduction = remainingAmount;
            remainingAmount = deductFrameworkContractLedgersOnly(remainingAmount, relatedLedgers, currentLedgerId, currentQuarter,
                    relatedLedgerDeductionAmountMap);
            BigDecimal frameworkDeductedAmount = amountBeforeFrameworkDeduction.subtract(remainingAmount);
            applyFrameworkContractTransferOnBalance(bizKey, currentQuarter, frameworkDeductedAmount, balanceMap, needToUpdateSameDemBudgetBalanceMap);
            log.info("========== performMultiQuarterDeduction - 框架协议：已先扣减合同流水 amountAvailable，剩余待扣资金池 remainingAmount={} ==========", remainingAmount);
        }
        
        // 从当前季度开始，依次往上季度扣减（往前推，即从当前季度到q1）
        String[] quarters = {"q1", "q2", "q3", "q4"};
        for (int i = currentQuarterNum - 1; i >= 0 && remainingAmount.compareTo(BigDecimal.ZERO) > 0; i--) {
            String quarter = quarters[i];
            String bizKeyQuarter = bizKey + "@" + quarter;
            
            // 从 needToUpdateSameDemBudgetBalanceMap 获取单个 balance（扣减时应该只使用单个balance，和回滚逻辑一致）
            BudgetBalance balance = needToUpdateSameDemBudgetBalanceMap.get(bizKeyQuarter);
            // 当资金池无该维度 balance 但有关联预算流水时，仅对关联流水扣减，不抛异常
            if (balance == null) {
                if (!CollectionUtils.isEmpty(relatedLedgers)) {
                    if (frameworkContractLedgerFirst) {
                        if (remainingAmount.compareTo(BigDecimal.ZERO) <= 0) {
                            break;
                        }
                        log.error("========== performMultiQuarterDeduction - 框架协议付款仍有余额但季度 {} 无资金池 balance: bizKey={} ==========", quarter, bizKey);
                        throw new IllegalStateException(String.format(
                                "明细 [%s] 关联框架协议合同，合同可用已扣减后仍有余额 %s，但季度 %s 未找到预算余额，无法从资金池扣减。",
                                bizKey, remainingAmount, quarter));
                    }
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
            
            // 从 balanceMap 获取 balanceList 备用（用于校验可用余额的合计值，但实际扣减只操作单个balance）
            List<BudgetBalance> balanceList = balanceMap.get(bizKeyQuarter);
            
            // 累加 balanceList 下所有 balance 的 amountAvailable、amountPayAvailable 合计备用（用于校验）
            BigDecimal totalAmountAvailableFromList = BigDecimal.ZERO;
            BigDecimal totalAmountPayAvailableFromList = BigDecimal.ZERO;
            if (!CollectionUtils.isEmpty(balanceList)) {
                for (BudgetBalance balanceItem : balanceList) {
                    BigDecimal amountAvailable = balanceItem.getAmountAvailable();
                    BigDecimal amountPayAvailable = balanceItem.getAmountPayAvailable() == null ? BigDecimal.ZERO : balanceItem.getAmountPayAvailable();
                    // 如果amountAvailable为null，忽略该balance；如果有数字（包括0），才累加
                    if (amountAvailable != null) {
                        totalAmountAvailableFromList = totalAmountAvailableFromList.add(amountAvailable);
                    }
                    totalAmountPayAvailableFromList = totalAmountPayAvailableFromList.add(amountPayAvailable);
                }
            }
            
            // 使用单个 balance 的值
            BigDecimal quarterAmountAvailable = balance.getAmountAvailable() == null ? BigDecimal.ZERO : balance.getAmountAvailable();
            BigDecimal quarterAmountPayAvailable = balance.getAmountPayAvailable() == null ? BigDecimal.ZERO : balance.getAmountPayAvailable();
            
            if (CollectionUtils.isEmpty(relatedLedgers) || frameworkContractLedgerFirst) {
                // 如果获取到的 List<BudgetLedger> 集合数据为空；框架协议时已先扣完合同，仅走资金池扣减
                // 使用 balanceList 的合计值来校验可用余额（totalAmountAvailableFromList），但实际扣减只操作单个balance
                // 如果 bizKey 以 @NAN@NAN 结尾（erpAssetType 和项目编码同时为空），则不校验 amountPayAvailable，直接使用 amountAvailable
                BigDecimal amountToOperateThisQuarter;
                
                // 判断是否是带项目的付款单：从 bizKey 中提取 masterProjectCode
                // bizKey格式：bizCode@isInternal@managementOrg@budgetSubjectCode@masterProjectCode@erpAssetType
                String[] bizKeyParts = bizKey.split("@");
                boolean isProjectQuery = bizKeyParts.length >= 5 && !"NAN".equals(bizKeyParts[4]); // bizKeyParts[4] 是 masterProjectCode
                
                if (bizKey.endsWith("@NAN@NAN")) {
                    // 组织+科目：使用资金池 amountAvailable，最多扣当季聚合可用额
                    amountToOperateThisQuarter = remainingAmount.min(totalAmountAvailableFromList);
                } else if (isProjectQuery) {
                    // 带项目的付款单：只使用 amountPayAvailable 来扣减（付款额），最多扣当季聚合付款可用额
                    amountToOperateThisQuarter = remainingAmount.min(totalAmountPayAvailableFromList);
                } else {
                    // 资产类型预算（非项目）：同样只使用 amountPayAvailable，最多扣当季聚合付款可用额
                    amountToOperateThisQuarter = remainingAmount.min(totalAmountPayAvailableFromList);
                }
                
                if (amountToOperateThisQuarter.compareTo(BigDecimal.ZERO) > 0) {
                    // 记录本季度操作的金额（累加，如果该季度已经有操作金额）
                    BigDecimal existingAmount = quarterOperateAmountMap.getOrDefault(quarter, BigDecimal.ZERO);
                    quarterOperateAmountMap.put(quarter, existingAmount.add(amountToOperateThisQuarter));
                    
                    // 只操作单个 balance（和回滚逻辑一致，不按比例分配到多个balance）
                    BigDecimal balanceAmountActual = balance.getAmountActual() == null ? BigDecimal.ZERO : balance.getAmountActual();
                    
                    // 更新本季度的余额（付款操作）
                    // 判断是否是带项目的付款单或资产类型：从 bizKey 中提取 masterProjectCode 和 erpAssetType
                    // bizKey格式：bizCode@isInternal@managementOrg@budgetSubjectCode@masterProjectCode@erpAssetType
                    String[] bizKeyPartsForDeduction = bizKey.split("@");
                    boolean isProjectQueryForDeduction = bizKeyPartsForDeduction.length >= 5 && !"NAN".equals(bizKeyPartsForDeduction[4]); // bizKeyParts[4] 是 masterProjectCode
                    boolean isAssetTypeQueryForDeduction = bizKeyPartsForDeduction.length >= 6 && !"NAN".equals(bizKeyPartsForDeduction[5]); // bizKeyParts[5] 是 erpAssetType
                    
                    // 对于带项目的付款单和资产类型付款单，不扣减 amountAvailable（采购额），只扣减 amountPayAvailable（付款额）
                    // 对于组织+科目（erpAssetType 和项目编码同时为 NAN），扣减 amountAvailable
                    // 重要：如果 amountAvailable 原本是 NULL（表示没有采购额科目），则不应该更新它，保持为 NULL
                    // 只有当 amountAvailable 原本不是 NULL 时，才进行扣减
                    if (bizKey.endsWith("@NAN@NAN") && balance.getAmountAvailable() != null) {
                        // 组织+科目：扣减 amountAvailable
                        balance.setAmountAvailable(quarterAmountAvailable.subtract(amountToOperateThisQuarter));
                        BigDecimal amountAvailableVchanged = balance.getAmountAvailableVchanged() == null ? BigDecimal.ZERO : balance.getAmountAvailableVchanged();
                        balance.setAmountAvailableVchanged(amountAvailableVchanged.subtract(amountToOperateThisQuarter));
                    } else if (isProjectQueryForDeduction || isAssetTypeQueryForDeduction) {
                        // 带项目的付款单或资产类型付款单，不扣减 amountAvailable
                        log.debug("========== performMultiQuarterDeduction - 带项目的付款单或资产类型付款单，不扣减 amountAvailable: quarter={}, amountToOperateThisQuarter={}, isProjectQuery={}, isAssetTypeQuery={} ==========",
                                quarter, amountToOperateThisQuarter, isProjectQueryForDeduction, isAssetTypeQueryForDeduction);
                    } else {
                        // amountAvailable 为 NULL，保持为 NULL，不更新 amountAvailableVchanged
                        log.debug("========== performMultiQuarterDeduction - amountAvailable 为 NULL，保持为 NULL，不进行扣减: quarter={}, amountToOperateThisQuarter={} ==========",
                                quarter, amountToOperateThisQuarter);
                    }
                    
                    // amountAvailable 扣减时，amountActual 要叠加
                    // amountActual 是累积发生数，记录实际发生的金额
                    balance.setAmountActual(balanceAmountActual.add(amountToOperateThisQuarter));
                    BigDecimal amountActualVchanged = balance.getAmountActualVchanged() == null ? BigDecimal.ZERO : balance.getAmountActualVchanged();
                    balance.setAmountActualVchanged(amountActualVchanged.add(amountToOperateThisQuarter));
                    
                    if (!bizKey.endsWith("@NAN@NAN")) {
                        // erpAssetType 或项目编码有值，需要扣减 amountPayAvailable
                        // 如果当前季度的 amountPayAvailable=0，但累积值足够（预算校验已通过），允许扣成负数
                        balance.setAmountPayAvailable(quarterAmountPayAvailable.subtract(amountToOperateThisQuarter));
                        BigDecimal amountPayAvailableVchanged = balance.getAmountPayAvailableVchanged() == null ? BigDecimal.ZERO : balance.getAmountPayAvailableVchanged();
                        balance.setAmountPayAvailableVchanged(amountPayAvailableVchanged.subtract(amountToOperateThisQuarter));
                    }
                    
                    remainingAmount = remainingAmount.subtract(amountToOperateThisQuarter);
                    markSubmitPoolBudgetBalanceTouched(bizKey);
                    
                    log.info("========== performMultiQuarterDeduction - 季度 {} 扣减（无关联流水）: 操作金额={}, 剩余待操作={}, 扣减后amountAvailable={}, amountActual={}, amountPayAvailable={}, erpAssetType和项目编码同时为空={} ==========",
                            quarter, amountToOperateThisQuarter, remainingAmount, balance.getAmountAvailable(), balance.getAmountActual(), balance.getAmountPayAvailable(), bizKey.endsWith("@NAN@NAN"));
                }
            } else {
                // 如果获取到的 List<BudgetLedger> 集合数据不为空
                // 每个季度要获取的就是 List<BudgetLedger> 集合数据下对应季度的值作为要扣减的数
                // 获取第一个 BudgetLedger 的 bizType 来决定处理逻辑
                BudgetLedger firstRelatedLedger = relatedLedgers.get(0);
                String relatedBizType = firstRelatedLedger.getBizType();
                
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
                
                // 比较 remainingAmount 和 availableAmountFromRelatedLedgers，同时考虑需要扣减的账户余额
                // 如果 bizKey 以 @NAN@NAN 结尾（erpAssetType 和项目编码同时为空），则不校验 amountPayAvailable
                // 累加 balanceList 下所有BudgetBalance的amountOccupied和amountFrozen合计（用于校验），但实际扣减只操作单个balance
                BigDecimal totalAmountOccupiedFromList = BigDecimal.ZERO;
                BigDecimal totalAmountFrozenFromList = BigDecimal.ZERO;
                if (!CollectionUtils.isEmpty(balanceList)) {
                    for (BudgetBalance balanceItem : balanceList) {
                        BigDecimal amountOccupied = balanceItem.getAmountOccupied() == null ? BigDecimal.ZERO : balanceItem.getAmountOccupied();
                        BigDecimal amountFrozen = balanceItem.getAmountFrozen() == null ? BigDecimal.ZERO : balanceItem.getAmountFrozen();
                        totalAmountOccupiedFromList = totalAmountOccupiedFromList.add(amountOccupied);
                        totalAmountFrozenFromList = totalAmountFrozenFromList.add(amountFrozen);
                    }
                }
                
                // 使用单个 balance 的值
                BigDecimal quarterAmountOccupied = balance.getAmountOccupied() == null ? BigDecimal.ZERO : balance.getAmountOccupied();
                BigDecimal quarterAmountFrozen = balance.getAmountFrozen() == null ? BigDecimal.ZERO : balance.getAmountFrozen();
                
                BigDecimal amountToFreezeThisQuarter;
                // 关联合同/申请单时：只按「关联单据可用余额」上限扣减，不再与资金池 amountPayAvailable 取 min，与 AbstractBudgetService 校验逻辑一致（合同/申请已把池子转成占用/冻结，付款只消耗关联单据 amountAvailable）。
                if (CONTRACT_BIZ_TYPE.equals(relatedBizType)) {
                    // CONTRACT：有关联流水时，扣减的是关联单据的 amountAvailable，不扣资金池的 amountPayAvailable
                    amountToFreezeThisQuarter = remainingAmount.min(availableAmountFromRelatedLedgers);
                } else {
                    // APPLY：有关联流水时，扣减的是关联单据的 amountAvailable，不扣资金池的 amountPayAvailable
                    amountToFreezeThisQuarter = remainingAmount.min(availableAmountFromRelatedLedgers);
                }
                
                log.info("========== performMultiQuarterDeduction - 可扣减金额计算: quarter={}, remainingAmount={}, availableAmountFromRelatedLedgers={}, totalAmountOccupiedFromList={}, totalAmountFrozenFromList={}, totalAmountPayAvailableFromList={}, amountToFreezeThisQuarter={}, relatedBizType={} ==========",
                        quarter, remainingAmount, availableAmountFromRelatedLedgers, totalAmountOccupiedFromList, totalAmountFrozenFromList, totalAmountPayAvailableFromList, amountToFreezeThisQuarter, relatedBizType);
                
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
                    
                    // 根据关联流水的 bizType 来决定处理逻辑
                    // 对于所有类型的付款单（组织+科目、组织+资产类型、组织+项目），有关联流水时，不扣减资金池的 amountPayAvailable（只扣减关联单据的 amountAvailable）
                    if (CONTRACT_BIZ_TYPE.equals(relatedBizType)) {
                        // 如果是 CONTRACT，扣减 amountOccupied
                        // 只操作单个 balance（和回滚逻辑一致，不按比例分配到多个balance）
                        BigDecimal balanceAmountActual = balance.getAmountActual() == null ? BigDecimal.ZERO : balance.getAmountActual();
                        
                        balance.setAmountOccupied(quarterAmountOccupied.subtract(amountToFreezeThisQuarter));
                        BigDecimal amountOccupiedVchanged = balance.getAmountOccupiedVchanged() == null ? BigDecimal.ZERO : balance.getAmountOccupiedVchanged();
                        balance.setAmountOccupiedVchanged(amountOccupiedVchanged.subtract(amountToFreezeThisQuarter));
                        
                        // amountOccupied 扣减时，amountActual 要叠加
                        // amountActual 是累积发生数，记录实际发生的金额
                        balance.setAmountActual(balanceAmountActual.add(amountToFreezeThisQuarter));
                        BigDecimal amountActualVchanged = balance.getAmountActualVchanged() == null ? BigDecimal.ZERO : balance.getAmountActualVchanged();
                        balance.setAmountActualVchanged(amountActualVchanged.add(amountToFreezeThisQuarter));
                        
                        // 关联流水单的付款单（项目/资产类型）：除扣减关联流水外，同时扣减资金池付款额
                        if (!bizKey.endsWith("@NAN@NAN")) {
                            BigDecimal payAvail = balance.getAmountPayAvailable() == null ? BigDecimal.ZERO : balance.getAmountPayAvailable();
                            balance.setAmountPayAvailable(payAvail.subtract(amountToFreezeThisQuarter));
                            BigDecimal amountPayAvailableVchanged = balance.getAmountPayAvailableVchanged() == null ? BigDecimal.ZERO : balance.getAmountPayAvailableVchanged();
                            balance.setAmountPayAvailableVchanged(amountPayAvailableVchanged.subtract(amountToFreezeThisQuarter));
                        }
                        
                        remainingAmount = remainingAmount.subtract(amountToFreezeThisQuarter);
                        
                        log.info("========== performMultiQuarterDeduction - 季度 {} 扣减（有关联流水-CONTRACT）: 操作金额={}, 剩余待操作={}, 扣减后amountOccupied={}, amountActual={}, amountPayAvailable={}, erpAssetType和项目编码同时为空={} ==========",
                                quarter, amountToFreezeThisQuarter, remainingAmount, balance.getAmountOccupied(), balance.getAmountActual(), balance.getAmountPayAvailable(), bizKey.endsWith("@NAN@NAN"));
                    } else {
                        // 如果是 APPLY，扣减 amountFrozen
                        // 只操作单个 balance（和回滚逻辑一致，不按比例分配到多个balance）
                        BigDecimal balanceAmountActual = balance.getAmountActual() == null ? BigDecimal.ZERO : balance.getAmountActual();
                        
                        balance.setAmountFrozen(quarterAmountFrozen.subtract(amountToFreezeThisQuarter));
                        BigDecimal amountFrozenVchanged = balance.getAmountFrozenVchanged() == null ? BigDecimal.ZERO : balance.getAmountFrozenVchanged();
                        balance.setAmountFrozenVchanged(amountFrozenVchanged.subtract(amountToFreezeThisQuarter));
                        
                        // amountFrozen 扣减时，amountActual 要叠加
                        // amountActual 是累积发生数，记录实际发生的金额
                        balance.setAmountActual(balanceAmountActual.add(amountToFreezeThisQuarter));
                        BigDecimal amountActualVchanged = balance.getAmountActualVchanged() == null ? BigDecimal.ZERO : balance.getAmountActualVchanged();
                        balance.setAmountActualVchanged(amountActualVchanged.add(amountToFreezeThisQuarter));
                        
                        // 关联流水单的付款单（项目/资产类型）：除扣减关联流水外，同时扣减资金池付款额
                        if (!bizKey.endsWith("@NAN@NAN")) {
                            BigDecimal payAvail = balance.getAmountPayAvailable() == null ? BigDecimal.ZERO : balance.getAmountPayAvailable();
                            balance.setAmountPayAvailable(payAvail.subtract(amountToFreezeThisQuarter));
                            BigDecimal amountPayAvailableVchanged = balance.getAmountPayAvailableVchanged() == null ? BigDecimal.ZERO : balance.getAmountPayAvailableVchanged();
                            balance.setAmountPayAvailableVchanged(amountPayAvailableVchanged.subtract(amountToFreezeThisQuarter));
                        }
                        
                        remainingAmount = remainingAmount.subtract(amountToFreezeThisQuarter);
                        
                        log.info("========== performMultiQuarterDeduction - 季度 {} 扣减（有关联流水-APPLY）: 操作金额={}, 剩余待操作={}, 扣减后amountFrozen={}, amountActual={}, amountPayAvailable={}, erpAssetType和项目编码同时为空={} ==========",
                                quarter, amountToFreezeThisQuarter, remainingAmount, balance.getAmountFrozen(), balance.getAmountActual(), balance.getAmountPayAvailable(), bizKey.endsWith("@NAN@NAN"));
                    }
                    markSubmitPoolBudgetBalanceTouched(bizKey);
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
            BigDecimal q1PayAvail = q1Balance.getAmountPayAvailable() == null ? BigDecimal.ZERO : q1Balance.getAmountPayAvailable();
            BigDecimal q1Actual = q1Balance.getAmountActual() == null ? BigDecimal.ZERO : q1Balance.getAmountActual();
            BigDecimal q1AvailV = q1Balance.getAmountAvailableVchanged() == null ? BigDecimal.ZERO : q1Balance.getAmountAvailableVchanged();
            BigDecimal q1PayV = q1Balance.getAmountPayAvailableVchanged() == null ? BigDecimal.ZERO : q1Balance.getAmountPayAvailableVchanged();
            BigDecimal q1ActualV = q1Balance.getAmountActualVchanged() == null ? BigDecimal.ZERO : q1Balance.getAmountActualVchanged();

            // 记录 Q1 的操作金额（累加模式）
            BigDecimal existingQ1 = quarterOperateAmountMap.getOrDefault("q1", BigDecimal.ZERO);
            quarterOperateAmountMap.put("q1", existingQ1.add(remainingAmount));

            // 维度判断：组织+科目 vs 项目/资产类型
            String[] bizKeyPartsForQ1 = bizKey.split("@");
            boolean isProjectQ1 = bizKeyPartsForQ1.length >= 5 && !"NAN".equals(bizKeyPartsForQ1[4]);
            boolean isAssetQ1 = bizKeyPartsForQ1.length >= 6 && !"NAN".equals(bizKeyPartsForQ1[5]);
            boolean isOrgSubjectQ1 = bizKey.endsWith("@NAN@NAN");

            if (isOrgSubjectQ1 && q1Balance.getAmountAvailable() != null) {
                // 组织+科目：透支采购可用额
                q1Balance.setAmountAvailable(q1Avail.subtract(remainingAmount));
                q1Balance.setAmountAvailableVchanged(q1AvailV.subtract(remainingAmount));
            } else if (isProjectQ1 || isAssetQ1) {
                // 项目 / 资产类型：透支付款可用额
                q1Balance.setAmountPayAvailable(q1PayAvail.subtract(remainingAmount));
                q1Balance.setAmountPayAvailableVchanged(q1PayV.subtract(remainingAmount));
            }

            // 实际发生数始终叠加
            q1Balance.setAmountActual(q1Actual.add(remainingAmount));
            q1Balance.setAmountActualVchanged(q1ActualV.add(remainingAmount));
            markSubmitPoolBudgetBalanceTouched(bizKey);

            log.info("========== performMultiQuarterDeduction - Q1 透支扣减: 透支金额={}, amountAvailable={}, amountPayAvailable={}, amountActual={} ==========",
                    remainingAmount, q1Balance.getAmountAvailable(), q1Balance.getAmountPayAvailable(), q1Balance.getAmountActual());

            remainingAmount = BigDecimal.ZERO;
        }

        // 验证：所有金额都应该被扣减完毕（因为已经通过预算校验）；绕过预算校验时允许有剩余不拦
        if (remainingAmount.compareTo(BigDecimal.ZERO) > 0 && !isSkipBudgetValidation()) {
            log.error("========== performMultiQuarterDeduction - 扣减后仍有剩余金额未操作: remainingAmount={} ==========", remainingAmount);
            throw new IllegalStateException("扣减后仍有剩余金额未操作，remainingAmount=" + remainingAmount);
        }
    }

    /**
     * 获取指定季度的消耗金额
     *
     * @param ledger 预算流水
     * @param quarter 季度（q1、q2、q3、q4）
     * @return 该季度的消耗金额
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
                log.warn("========== getQuarterConsumedAmount - 未知季度: {} ==========", quarter);
                return BigDecimal.ZERO;
        }
    }

    /**
     * 设置指定季度的消耗金额
     *
     * @param ledger 预算流水
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
}
