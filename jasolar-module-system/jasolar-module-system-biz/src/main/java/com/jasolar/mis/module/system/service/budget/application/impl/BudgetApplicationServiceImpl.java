package com.jasolar.mis.module.system.service.budget.application.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.incrementer.IdentifierGenerator;
import com.jasolar.mis.module.system.controller.budget.vo.*;
import com.jasolar.mis.module.system.domain.budget.*;
import com.jasolar.mis.module.system.mapper.budget.*;
import com.jasolar.mis.module.system.service.budget.AbstractBudgetService;
import com.jasolar.mis.module.system.service.budget.application.BudgetApplicationService;
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
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 预算申请 Service 实现
 */
@Service
@Slf4j
public class BudgetApplicationServiceImpl extends AbstractBudgetService implements BudgetApplicationService {

    private static final String DEFAULT_BIZ_TYPE = "APPLY";
    private static final String CONTRACT_BIZ_TYPE = "CONTRACT";
    private static final String CLAIM_BIZ_TYPE = "CLAIM";
    private static final String DEFAULT_CURRENCY = "CNY";
    private static final String INITIAL_SUBMITTED = "INITIAL_SUBMITTED";
    private static final String PROJECT_SKIP_BUDGET_SUBJECT_CODE = "000000";
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
    private static final DateTimeFormatter PROCESS_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /** 去除业务单号可能带有的 BOM(U+FEFF) 和首尾空格，避免按 bizCode 查询不到流水头 */
    private static String normalizeBizCode(String bizCode) {
        if (bizCode == null) {
            return null;
        }
        return bizCode.replace("\uFEFF", "").trim();
    }

    private boolean isCurrentYear(String year) {
        return StringUtils.isNotBlank(year) && String.valueOf(LocalDate.now().getYear()).equals(StringUtils.trim(year));
    }

    private String extractApplyYear(List<ApplyDetailDetalVo> demandDetails) {
        if (CollectionUtils.isEmpty(demandDetails)) {
            return null;
        }
        for (ApplyDetailDetalVo detail : demandDetails) {
            if (detail != null && StringUtils.isNotBlank(detail.getDemandYear())) {
                return StringUtils.trim(detail.getDemandYear());
            }
        }
        return null;
    }

    private String resolveRenewYear(String demandOrderNo, RenewApplyReqInfoParams renewInfo) {
        if (StringUtils.isBlank(demandOrderNo)) {
            return null;
        }
        LambdaQueryWrapper<BudgetLedger> ledgerYearWrapper = new LambdaQueryWrapper<>();
        ledgerYearWrapper.eq(BudgetLedger::getBizCode, demandOrderNo)
                .eq(BudgetLedger::getBizType, DEFAULT_BIZ_TYPE)
                .eq(BudgetLedger::getDeleted, Boolean.FALSE);
        List<BudgetLedger> ledgers = budgetLedgerMapper.selectList(ledgerYearWrapper);
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
            return "预算申请审批成功";
        }
        if ("REJECTED".equals(documentStatus)) {
            return "预算申请驳回成功";
        }
        if ("CANCELLED".equals(documentStatus)) {
            return "预算申请撤销成功";
        }
        if ("CLOSED".equals(documentStatus)) {
            return "预算申请关闭成功";
        }
        return "预算申请处理成功";
    }

    private boolean isProjectSkipBudgetDetail(String budgetSubjectCode, String masterProjectCode) {
        return PROJECT_SKIP_BUDGET_SUBJECT_CODE.equals(StringUtils.trim(budgetSubjectCode))
                && StringUtils.isNotBlank(masterProjectCode)
                && !"NAN".equals(StringUtils.trim(masterProjectCode));
    }

    private boolean isProjectSkipBudgetLedger(BudgetLedger ledger) {
        return ledger != null && isProjectSkipBudgetDetail(ledger.getBudgetSubjectCode(), ledger.getMasterProjectCode());
    }

    private void resetLedgerConsumedFields(BudgetLedger ledger, BigDecimal demandAmount) {
        if (ledger == null) {
            return;
        }
        BigDecimal amount = demandAmount == null ? BigDecimal.ZERO : demandAmount;
        ledger.setAmount(amount);
        ledger.setAmountAvailable(amount);
        ledger.setAmountConsumedQOne(BigDecimal.ZERO);
        ledger.setAmountConsumedQTwo(BigDecimal.ZERO);
        ledger.setAmountConsumedQThree(BigDecimal.ZERO);
        ledger.setAmountConsumedQFour(BigDecimal.ZERO);
    }

    private BudgetApplicationRespVo handleApplyCrossYearNoDeduction(BudgetApplicationParams budgetApplicationParams,
                                                                     ApplyReqInfoParams applyInfo,
                                                                     List<ApplyDetailDetalVo> demandDetails,
                                                                     LocalDateTime requestTime,
                                                                     Map<String, String> detailValidationResultMap,
                                                                     Map<String, String> detailValidationMessageMap) {
        String demandOrderNo = applyInfo.getDemandOrderNo();
        String operator = applyInfo.getOperator();
        String operatorNo = applyInfo.getOperatorNo();

        List<ApplyExtDetailVo> applyExtDetailsForQuery = new ArrayList<>();
        for (ApplyDetailDetalVo detail : demandDetails) {
            ApplyExtDetailVo extDetail = new ApplyExtDetailVo();
            BeanUtils.copyProperties(detail, extDetail);
            extDetail.setDemandOrderNo(demandOrderNo);
            if (detail.getMetadata() != null && !detail.getMetadata().isEmpty()) {
                extDetail.setMetadataJson(convertMapToJson(detail.getMetadata()));
            }
            applyExtDetailsForQuery.add(extDetail);
        }

        List<BudgetLedger> existingLedgers = budgetLedgerMapper.selectByExtDetails(applyExtDetailsForQuery);
        Map<String, BudgetLedger> existingBudgetLedgerMap = existingLedgers.stream()
                .collect(Collectors.toMap(
                        ledger -> ledger.getBizCode() + "@" + ledger.getBizItemCode(),
                        Function.identity(),
                        (a, b) -> a
                ));

        Set<String> retainedKeys = applyExtDetailsForQuery.stream()
                .map(ext -> ext.getDemandOrderNo() + "@" + ext.getDemandDetailLineNo())
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
            budgetLedgerMapper.deleteByIds(new ArrayList<>(deleteIds));
            LambdaQueryWrapper<BudgetLedgerSelfR> deleteWrapper = new LambdaQueryWrapper<>();
            deleteWrapper.in(BudgetLedgerSelfR::getRelatedId, deleteIds)
                    .eq(BudgetLedgerSelfR::getBizType, DEFAULT_BIZ_TYPE);
            budgetLedgerSelfRMapper.delete(deleteWrapper);
        }

        List<BudgetLedger> needUpdateLedgers = new ArrayList<>();
        List<BudgetLedger> needInsertLedgers = new ArrayList<>();
        List<BudgetLedgerHistory> updateHistories = new ArrayList<>();
        for (ApplyExtDetailVo extDetail : applyExtDetailsForQuery) {
            String key = extDetail.getDemandOrderNo() + "@" + extDetail.getDemandDetailLineNo();
            String detailLineNo = extDetail.getDemandDetailLineNo();
            BudgetLedger existing = existingBudgetLedgerMap.get(key);
            if (existing != null) {
                BudgetLedgerHistory history = new BudgetLedgerHistory();
                BeanUtils.copyProperties(existing, history);
                history.setId(identifierGenerator.nextId(history).longValue());
                history.setLedgerId(existing.getId());
                history.setDeleted(Boolean.FALSE);
                updateHistories.add(history);

                existing.setYear(extDetail.getDemandYear());
                existing.setMonth(extDetail.getDemandMonth());
                existing.setIsInternal(extDetail.getIsInternal());
                existing.setMorgCode(extDetail.getManagementOrg());
                existing.setBudgetSubjectCode(StringUtils.defaultIfBlank(extDetail.getBudgetSubjectCode(), "NAN-NAN"));
                existing.setMasterProjectCode(StringUtils.defaultIfBlank(extDetail.getMasterProjectCode(), "NAN"));
                existing.setErpAssetType(StringUtils.defaultIfBlank(extDetail.getErpAssetType(), "NAN"));
                existing.setCurrency(StringUtils.defaultIfBlank(extDetail.getCurrency(), DEFAULT_CURRENCY));
                resetLedgerConsumedFields(existing, extDetail.getDemandAmount());
                existing.setMetadata(extDetail.getMetadataJson());
                existing.setVersionPre(existing.getVersion());
                existing.setVersion(String.valueOf(identifierGenerator.nextId(null)));
                existing.setOperator(operator);
                existing.setOperatorNo(operatorNo);
                if (requestTime != null) {
                    existing.setUpdateTime(requestTime);
                }
                needUpdateLedgers.add(existing);
            } else {
                Long ledgerId = identifierGenerator.nextId(null).longValue();
                BudgetLedger newLedger = budgetQueryHelperService.createBudgetLedger(
                        ledgerId,
                        DEFAULT_BIZ_TYPE,
                        extDetail.getDemandOrderNo(),
                        extDetail.getDemandDetailLineNo(),
                        extDetail.getDemandYear(),
                        extDetail.getDemandMonth(),
                        extDetail.getIsInternal(),
                        extDetail.getManagementOrg(),
                        extDetail.getBudgetSubjectCode(),
                        extDetail.getMasterProjectCode(),
                        extDetail.getErpAssetType(),
                        StringUtils.defaultIfBlank(extDetail.getCurrency(), DEFAULT_CURRENCY),
                        extDetail.getDemandAmount() == null ? BigDecimal.ZERO : extDetail.getDemandAmount(),
                        null,
                        identifierGenerator,
                        operator,
                        operatorNo,
                        requestTime
                );
                resetLedgerConsumedFields(newLedger, extDetail.getDemandAmount());
                newLedger.setMetadata(extDetail.getMetadataJson());
                needInsertLedgers.add(newLedger);
            }
            detailValidationResultMap.put(detailLineNo, "0");
            detailValidationMessageMap.put(detailLineNo, "处理成功");
        }

        if (!updateHistories.isEmpty()) {
            budgetLedgerHistoryMapper.insertBatch(updateHistories);
        }
        if (!needUpdateLedgers.isEmpty()) {
            budgetLedgerMapper.updateBatchById(sortLedgersById(needUpdateLedgers));
        }
        if (!needInsertLedgers.isEmpty()) {
            budgetLedgerMapper.insertBatch(needInsertLedgers);
        }

        budgetQueryHelperService.createOrUpdateBudgetLedgerHead(demandOrderNo, DEFAULT_BIZ_TYPE,
                applyInfo.getDocumentName(), applyInfo.getDataSource(), applyInfo.getProcessName(), "SUBMITTED",
                identifierGenerator, operator, operatorNo, requestTime);

        return buildResponse(budgetApplicationParams, detailValidationResultMap, detailValidationMessageMap,
                Collections.emptyMap(), Collections.emptyMap(), Collections.emptyMap());
    }

    private BudgetRenewRespVo handleRejectedOrCancelledSkipRollback(String demandOrderNo, String documentStatus, BudgetRenewParams budgetRenewParams) {
        LambdaQueryWrapper<BudgetLedgerHead> headWrapper = new LambdaQueryWrapper<>();
        headWrapper.eq(BudgetLedgerHead::getBizCode, demandOrderNo)
                .eq(BudgetLedgerHead::getDeleted, Boolean.FALSE);
        BudgetLedgerHead existingHead = budgetLedgerHeadMapper.selectOne(headWrapper);
        if (existingHead == null) {
            throw new IllegalStateException("未找到对应的预算流水头，业务单号：" + demandOrderNo);
        }

        LambdaQueryWrapper<BudgetLedger> ledgerWrapper = new LambdaQueryWrapper<>();
        ledgerWrapper.eq(BudgetLedger::getBizCode, demandOrderNo)
                .eq(BudgetLedger::getBizType, DEFAULT_BIZ_TYPE)
                .eq(BudgetLedger::getDeleted, Boolean.FALSE);
        List<BudgetLedger> allLedgers = budgetLedgerMapper.selectList(ledgerWrapper);

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
            LambdaQueryWrapper<BudgetLedgerSelfR> deleteWrapper = new LambdaQueryWrapper<>();
            deleteWrapper.in(BudgetLedgerSelfR::getRelatedId, needToCancelBudgetLedgerSet)
                    .eq(BudgetLedgerSelfR::getBizType, DEFAULT_BIZ_TYPE);
            budgetLedgerSelfRMapper.delete(deleteWrapper);
        }

        BudgetLedgerHeadHistory headHistory = new BudgetLedgerHeadHistory();
        BeanUtils.copyProperties(existingHead, headHistory);
        headHistory.setId(identifierGenerator.nextId(headHistory).longValue());
        headHistory.setLedgerHeadId(existingHead.getId());
        headHistory.setDeleted(Boolean.FALSE);
        budgetLedgerHeadHistoryMapper.insertBatch(Collections.singletonList(headHistory));
        budgetLedgerHeadMapper.deleteById(existingHead.getId());

        return buildSuccessResponse(budgetRenewParams, resolveRenewSuccessMessage(documentStatus));
    }

    private BudgetApplicationRespVo handleDetailDeletedCrossYearNoRollback(BudgetApplicationParams budgetApplicationParams,
                                                                          ReqInfoParams reqInfo,
                                                                          List<DetailDetailVo> details) {
        String demandOrderNo = reqInfo.getDocumentNo();

        LambdaQueryWrapper<BudgetLedger> ledgerWrapper = new LambdaQueryWrapper<>();
        ledgerWrapper.eq(BudgetLedger::getBizCode, demandOrderNo)
                .eq(BudgetLedger::getBizType, DEFAULT_BIZ_TYPE)
                .eq(BudgetLedger::getDeleted, Boolean.FALSE);
        List<BudgetLedger> ledgers = budgetLedgerMapper.selectList(ledgerWrapper);
        Map<String, BudgetLedger> ledgerByBizItemCode = ledgers.stream()
                .collect(Collectors.toMap(BudgetLedger::getBizItemCode, Function.identity(), (a, b) -> a));

        List<BudgetLedgerHistory> histories = new ArrayList<>();
        Set<Long> deletedLedgerIds = new HashSet<>();
        Map<String, String> detailValidationResultMap = new HashMap<>();
        Map<String, String> detailValidationMessageMap = new HashMap<>();
        LocalDateTime requestTime = parseRequestTime(budgetApplicationParams.getEsbInfo());
        String operator = reqInfo.getOperator();
        String operatorNo = reqInfo.getOperatorNo();

        for (DetailDetailVo detail : details) {
            String detailLineNo = detail.getDetailLineNo();
            BudgetLedger ledger = ledgerByBizItemCode.get(detailLineNo);
            if (ledger == null) {
                detailValidationResultMap.put(detailLineNo, "1");
                detailValidationMessageMap.put(detailLineNo, "未找到可删除的明细");
                continue;
            }
            BudgetLedgerHistory history = new BudgetLedgerHistory();
            BeanUtils.copyProperties(ledger, history);
            history.setId(identifierGenerator.nextId(history).longValue());
            history.setLedgerId(ledger.getId());
            history.setDeleted(Boolean.FALSE);
            histories.add(history);

            ledger.setOperator(operator);
            ledger.setOperatorNo(operatorNo);
            ledger.setUpdateTime(requestTime != null ? requestTime : LocalDateTime.now());
            budgetLedgerMapper.updateById(ledger);
            deletedLedgerIds.add(ledger.getId());
            detailValidationResultMap.put(detailLineNo, "0");
            detailValidationMessageMap.put(detailLineNo, "明细删除成功");
        }

        if (!histories.isEmpty()) {
            budgetLedgerHistoryMapper.insertBatch(histories);
        }
        if (!deletedLedgerIds.isEmpty()) {
            budgetLedgerMapper.deleteByIds(new ArrayList<>(deletedLedgerIds));
            LambdaQueryWrapper<BudgetLedgerSelfR> deleteWrapper = new LambdaQueryWrapper<>();
            deleteWrapper.in(BudgetLedgerSelfR::getRelatedId, deletedLedgerIds)
                    .eq(BudgetLedgerSelfR::getBizType, DEFAULT_BIZ_TYPE);
            budgetLedgerSelfRMapper.delete(deleteWrapper);
        }

        LambdaQueryWrapper<BudgetLedger> remainedWrapper = new LambdaQueryWrapper<>();
        remainedWrapper.eq(BudgetLedger::getBizCode, demandOrderNo)
                .eq(BudgetLedger::getBizType, DEFAULT_BIZ_TYPE)
                .eq(BudgetLedger::getDeleted, Boolean.FALSE);
        Long remainedCount = budgetLedgerMapper.selectCount(remainedWrapper);
        if (remainedCount != null && remainedCount == 0L) {
            LambdaQueryWrapper<BudgetLedgerHead> headWrapper = new LambdaQueryWrapper<>();
            headWrapper.eq(BudgetLedgerHead::getBizCode, demandOrderNo)
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

        ESBInfoParams esbInfo = budgetApplicationParams.getEsbInfo();
        String instId = esbInfo != null ? esbInfo.getInstId() : null;
        if (StringUtils.isBlank(instId)) {
            String uuid = UUID.randomUUID().toString().replace("-", "");
            instId = "N" + uuid.substring(0, 8) + "." + uuid.substring(8, 16) + ".N"
                    + uuid.substring(16, 18) + "." + uuid.substring(18, 28) + ".N" + uuid.substring(28, 32);
        }
        boolean hasError = detailValidationResultMap.values().stream().anyMatch("1"::equals);
        ESBRespInfoVo esbRespInfo = ESBRespInfoVo.builder()
                .instId(instId)
                .requestTime(esbInfo != null ? esbInfo.getRequestTime() : null)
                .attr1(esbInfo != null ? esbInfo.getAttr1() : null)
                .attr2(esbInfo != null ? esbInfo.getAttr2() : null)
                .attr3(esbInfo != null ? esbInfo.getAttr3() : null)
                .returnCode(hasError ? "E0001-BUDGET" : "A0001-BUDGET")
                .returnMsg(hasError ? "部分明细删除失败" : "明细删除处理完成")
                .returnStatus("S")
                .responseTime(LocalDateTime.now().format(DATE_TIME_FORMATTER))
                .build();

        List<ApplyDetailRespVo> respDetails = new ArrayList<>();
        for (DetailDetailVo d : details) {
            ApplyDetailRespVo rd = new ApplyDetailRespVo();
            rd.setDemandDetailLineNo(d.getDetailLineNo());
            rd.setDemandYear(d.getYear());
            rd.setDemandMonth(d.getMonth());
            rd.setCompany(d.getCompany());
            rd.setDepartment(d.getDepartment());
            rd.setManagementOrg(d.getManagementOrg());
            rd.setBudgetSubjectCode(d.getBudgetSubjectCode());
            rd.setBudgetSubjectName(d.getBudgetSubjectName());
            rd.setMasterProjectCode(d.getMasterProjectCode());
            rd.setMasterProjectName(d.getMasterProjectName());
            rd.setErpAssetType(d.getErpAssetType());
            rd.setIsInternal(d.getIsInternal());
            rd.setDemandAmount(d.getAmount());
            rd.setCurrency(d.getCurrency());
            rd.setValidationResult(detailValidationResultMap.getOrDefault(d.getDetailLineNo(), "1"));
            rd.setValidationMessage(detailValidationMessageMap.getOrDefault(d.getDetailLineNo(), "未处理"));
            respDetails.add(rd);
        }

        ApplyResultInfoRespVo applyResult = new ApplyResultInfoRespVo();
        applyResult.setDemandOrderNo(demandOrderNo);
        applyResult.setProcessTime(LocalDateTime.now().format(PROCESS_TIME_FORMATTER));
        applyResult.setDemandDetails(respDetails);

        BudgetApplicationRespVo out = new BudgetApplicationRespVo();
        out.setEsbInfo(esbRespInfo);
        out.setApplyResult(applyResult);
        return out;
    }

    @Resource
    private BudgetLedgerMapper budgetLedgerMapper;
    @Resource
    private BudgetLedgerHistoryMapper budgetLedgerHistoryMapper;
    @Resource
    private BudgetPoolDemRMapper budgetPoolDemRMapper;
    @Resource
    private BudgetQuotaMapper budgetQuotaMapper;
    @Resource
    private BudgetBalanceMapper budgetBalanceMapper;
    @Resource
    private BudgetQuotaHistoryMapper budgetQuotaHistoryMapper;
    @Resource
    private BudgetBalanceHistoryMapper budgetBalanceHistoryMapper;
    @Resource
    private SystemProjectBudgetMapper systemProjectBudgetMapper;
    @Resource
    private BudgetLedgerHeadMapper budgetLedgerHeadMapper;
    @Resource
    private BudgetLedgerHeadHistoryMapper budgetLedgerHeadHistoryMapper;
    @Resource
    private BudgetLedgerSelfRMapper budgetLedgerSelfRMapper;
    @Resource
    private IdentifierGenerator identifierGenerator;
    @Resource
    private BudgetQueryHelperService budgetQueryHelperService;
    
    @Resource
    private BudgetSubjectCodeConfig budgetSubjectCodeConfig;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BudgetApplicationRespVo apply(BudgetApplicationParams budgetApplicationParams) {
        log.info("开始处理预算申请，params={}", budgetApplicationParams);
        
        ApplyReqInfoParams applyInfo = budgetApplicationParams.getApplyReqInfo();

        // 删除单据明细分支：直接复用 AbstractBudgetService 的通用逻辑
        // 仅使用 documentStatus=DETAIL_DELETED 触发
        String documentStatus = applyInfo != null ? applyInfo.getDocumentStatus() : null;
        // 兼容历史调用可能传入的 "DETAIL-DELETED"
        boolean isDetailDeleted = "DETAIL_DELETED".equals(documentStatus)
                || "DETAIL-DELETED".equalsIgnoreCase(documentStatus);
        if (applyInfo != null && isDetailDeleted) {
            BudgetParams budgetParams = new BudgetParams();
            budgetParams.setEsbInfo(budgetApplicationParams.getEsbInfo());
            ReqInfoParams reqInfo = new ReqInfoParams();
            reqInfo.setDocumentNo(applyInfo.getDemandOrderNo());
            reqInfo.setDocumentName(applyInfo.getDocumentName());
            reqInfo.setDataSource(applyInfo.getDataSource());
            reqInfo.setProcessName(applyInfo.getProcessName());
            // 统一透传为 "DETAIL_DELETED"，避免下游分支匹配失败
            reqInfo.setDocumentStatus("DETAIL_DELETED");
            reqInfo.setOperator(applyInfo.getOperator());
            reqInfo.setOperatorNo(applyInfo.getOperatorNo());
            reqInfo.setOperateTime(applyInfo.getOperateTime());

            List<ApplyDetailDetalVo> demandDetails = defaultList(applyInfo.getDemandDetails());
            List<DetailDetailVo> details = new ArrayList<>();
            for (ApplyDetailDetalVo d : demandDetails) {
                DetailDetailVo detail = new DetailDetailVo();
                detail.setDetailLineNo(d.getDemandDetailLineNo());
                detail.setYear(d.getDemandYear());
                detail.setMonth(d.getDemandMonth());
                detail.setCompany(d.getCompany());
                detail.setDepartment(d.getDepartment());
                detail.setManagementOrg(d.getManagementOrg());
                detail.setBudgetSubjectCode(d.getBudgetSubjectCode());
                detail.setBudgetSubjectName(d.getBudgetSubjectName());
                detail.setMasterProjectCode(d.getMasterProjectCode());
                detail.setMasterProjectName(d.getMasterProjectName());
                detail.setErpAssetType(d.getErpAssetType());
                detail.setIsInternal(d.getIsInternal());
                // 金额在 DETAIL_DELETED 分支不作为定位依据，沿用 Bean 校验要求由调用方传入（可传 0）
                detail.setAmount(d.getDemandAmount());
                detail.setCurrency(d.getCurrency());
                detail.setMetadata(d.getMetadata() != null ? convertMapToJson(d.getMetadata()) : null);
                details.add(detail);
            }
            reqInfo.setDetails(details);
            budgetParams.setReqInfo(reqInfo);
            String detailDeletedYear = extractApplyYear(demandDetails);
            if (StringUtils.isNotBlank(detailDeletedYear) && !isCurrentYear(detailDeletedYear)) {
                log.info("预算申请明细删除跨年跳过预算回滚，demandOrderNo={}, year={}, currentYear={}",
                        reqInfo.getDocumentNo(), detailDeletedYear, LocalDate.now().getYear());
                return handleDetailDeletedCrossYearNoRollback(budgetApplicationParams, reqInfo, details);
            }

            try {
                BudgetRespVo respVo = superDetailDeleted(budgetParams, DEFAULT_BIZ_TYPE);
                // 将通用 BudgetRespVo 转为申请单返回结构
                BudgetApplicationRespVo out = new BudgetApplicationRespVo();
                // 默认沿用通用返回头；若存在明细失败，则按申请单约定改写 returnCode/returnMsg（returnStatus 仍为 S）
                ESBRespInfoVo esbInfoOut = respVo.getEsbInfo();
                ApplyResultInfoRespVo applyResult = new ApplyResultInfoRespVo();
                applyResult.setDemandOrderNo(reqInfo.getDocumentNo());
                applyResult.setProcessTime(respVo.getResultInfo() != null ? respVo.getResultInfo().getProcessTime() : null);

                List<ApplyDetailRespVo> respDetails = new ArrayList<>();
                String firstErrorMsg = null;
                boolean hasError = respVo.getResultInfo() != null && "1".equals(respVo.getResultInfo().getValidationResult());
                if (respVo.getResultInfo() != null && respVo.getResultInfo().getDetails() != null) {
                    for (DetailDetailVo d : respVo.getResultInfo().getDetails()) {
                        ApplyDetailRespVo rd = new ApplyDetailRespVo();
                        rd.setDemandDetailLineNo(d.getDetailLineNo());
                        rd.setDemandYear(d.getYear());
                        rd.setDemandMonth(d.getMonth());
                        rd.setCompany(d.getCompany());
                        rd.setDepartment(d.getDepartment());
                        rd.setManagementOrg(d.getManagementOrg());
                        rd.setManagementOrgName(d.getManagementOrgName());
                        rd.setBudgetSubjectCode(d.getBudgetSubjectCode());
                        rd.setBudgetSubjectName(d.getBudgetSubjectName());
                        rd.setMasterProjectCode(d.getMasterProjectCode());
                        rd.setMasterProjectName(d.getMasterProjectName());
                        rd.setErpAssetType(d.getErpAssetType());
                        rd.setIsInternal(d.getIsInternal());
                        rd.setDemandAmount(d.getAmount());
                        rd.setCurrency(d.getCurrency());
                        rd.setValidationResult(d.getValidationResult());
                        rd.setValidationMessage(d.getValidationMessage());
                        rd.setAvailableBudgetRatio(d.getAvailableBudgetRatio());
                        rd.setAmountAvailable(d.getAmountAvailable());
                        respDetails.add(rd);
                        if (firstErrorMsg == null && "1".equals(d.getValidationResult()) && StringUtils.isNotBlank(d.getValidationMessage())) {
                            firstErrorMsg = d.getValidationMessage();
                        }
                    }
                }
                applyResult.setDemandDetails(respDetails);
                if (hasError && esbInfoOut != null) {
                    esbInfoOut.setReturnCode("E0001-BUDGET");
                    esbInfoOut.setReturnMsg(StringUtils.isNotBlank(firstErrorMsg) ? firstErrorMsg : "部分明细删除失败");
                    esbInfoOut.setReturnStatus("S");
                }
                out.setEsbInfo(esbInfoOut);
                out.setApplyResult(applyResult);
                return out;
            } catch (Exception ex) {
                // DETAIL_DELETED 分支：不将异常抛到全局异常处理器，按业务响应返回失败信息
                String responseTime = LocalDateTime.now().format(DATE_TIME_FORMATTER);
                ESBRespInfoVo esbRespInfo = ESBRespInfoVo.builder()
                        .instId(budgetApplicationParams.getEsbInfo() != null ? budgetApplicationParams.getEsbInfo().getInstId() : null)
                        .requestTime(budgetApplicationParams.getEsbInfo() != null ? budgetApplicationParams.getEsbInfo().getRequestTime() : null)
                        .attr1(budgetApplicationParams.getEsbInfo() != null ? budgetApplicationParams.getEsbInfo().getAttr1() : null)
                        .attr2(budgetApplicationParams.getEsbInfo() != null ? budgetApplicationParams.getEsbInfo().getAttr2() : null)
                        .attr3(budgetApplicationParams.getEsbInfo() != null ? budgetApplicationParams.getEsbInfo().getAttr3() : null)
                        .returnCode("E0001-BUDGET")
                        .returnMsg(ex.getMessage() != null ? ex.getMessage() : "明细删除失败")
                        // 业务失败：仍返回 S（避免被当成系统异常/通信失败）
                        .returnStatus("S")
                        .responseTime(responseTime)
                        .build();

                ApplyResultInfoRespVo applyResult = new ApplyResultInfoRespVo();
                applyResult.setDemandOrderNo(reqInfo.getDocumentNo());
                applyResult.setProcessTime(LocalDateTime.now().format(PROCESS_TIME_FORMATTER));

                List<ApplyDetailRespVo> respDetails = new ArrayList<>();
                for (DetailDetailVo d : details) {
                    ApplyDetailRespVo rd = new ApplyDetailRespVo();
                    rd.setDemandDetailLineNo(d.getDetailLineNo());
                    rd.setDemandYear(d.getYear());
                    rd.setDemandMonth(d.getMonth());
                    rd.setCompany(d.getCompany());
                    rd.setDepartment(d.getDepartment());
                    rd.setManagementOrg(d.getManagementOrg());
                    rd.setBudgetSubjectCode(d.getBudgetSubjectCode());
                    rd.setBudgetSubjectName(d.getBudgetSubjectName());
                    rd.setMasterProjectCode(d.getMasterProjectCode());
                    rd.setMasterProjectName(d.getMasterProjectName());
                    rd.setErpAssetType(d.getErpAssetType());
                    rd.setIsInternal(d.getIsInternal());
                    rd.setDemandAmount(d.getAmount());
                    rd.setCurrency(d.getCurrency());
                    rd.setValidationResult("1");
                    rd.setValidationMessage(ex.getMessage() != null ? ex.getMessage() : "明细删除失败");
                    respDetails.add(rd);
                }
                applyResult.setDemandDetails(respDetails);

                BudgetApplicationRespVo out = new BudgetApplicationRespVo();
                out.setEsbInfo(esbRespInfo);
                out.setApplyResult(applyResult);
                return out;
            }
        }
        
        // 用于存储每个明细的校验结果
        Map<String, String> detailValidationResultMap = new HashMap<>();
        Map<String, String> detailValidationMessageMap = new HashMap<>();

        try {
            String documentName = applyInfo.getDocumentName();
            String dataSource = applyInfo.getDataSource();
            String processName = applyInfo.getProcessName();
            String operator = applyInfo.getOperator();
            String operatorNo = applyInfo.getOperatorNo();
            List<ApplyDetailDetalVo> demandDetails = defaultList(applyInfo.getDemandDetails());
            // ESB requestTime 用于 BUDGET_LEDGER/BUDGET_LEDGER_HEAD 的 CREATE_TIME、UPDATE_TIME
            LocalDateTime requestTime = parseRequestTime(budgetApplicationParams.getEsbInfo());
            
            // 整单级别校验（这些是业务逻辑校验，不是Bean Validation能处理的）
            // 注意：这些校验失败会导致所有明细都报同样的错
            
            // 校验申请单状态
            if (!INITIAL_SUBMITTED.equals(applyInfo.getDocumentStatus())) {
                throw new IllegalArgumentException("申请单状态必须为 INITIAL_SUBMITTED，当前状态：" + applyInfo.getDocumentStatus());
            }

            // 校验明细列表不能为空（防御性编程）
            if (CollectionUtils.isEmpty(demandDetails)) {
                throw new IllegalArgumentException("需求明细列表不能为空");
            }

            // 跨年场景：非本年度不做预算校验/资金池扣减/单据扣减，但仍需落库
            String applyYear = extractApplyYear(demandDetails);
            if (StringUtils.isNotBlank(applyYear) && !isCurrentYear(applyYear)) {
                log.info("预算申请跨年跳过预算处理，demandOrderNo={}, year={}, currentYear={}",
                        applyInfo.getDemandOrderNo(), applyYear, LocalDate.now().getYear());
                return handleApplyCrossYearNoDeduction(budgetApplicationParams, applyInfo, demandDetails, requestTime,
                        detailValidationResultMap, detailValidationMessageMap);
            }

            // 组装 ApplyExtDetailVo 列表
            String year = null;
            List<ApplyExtDetailVo> applyExtDetailsForQuery = new ArrayList<>();
            
            // 批量提取 managementOrg 字段（对应 EHR_CD）
            Set<String> managementOrgSet = demandDetails.stream()
                    .map(ApplyDetailDetalVo::getManagementOrg)
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
            
            // 校验组织映射：未映射的 EHR_CD 逐明细提示
            // 注意：带项目的明细（masterProjectCode 不为空且不是 "NAN"），如果组织映射没有找到，直接使用传入的 EHR 组织编码，不报错
            Set<String> unmappedEhrCds = new HashSet<>(managementOrgSet);
            unmappedEhrCds.removeAll(ehrCdToOrgCdMap.keySet());
            unmappedEhrCds.removeAll(ehrCdToOrgCdExtMap.keySet());
            if (!unmappedEhrCds.isEmpty()) {
                for (ApplyDetailDetalVo detail : demandDetails) {
                    String detailLineNo = detail.getDemandDetailLineNo();
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
                        detailValidationResultMap.putIfAbsent(detailLineNo, "0");
                        detailValidationMessageMap.putIfAbsent(detailLineNo, "校验通过 Verification Passed");
                    }
                }
                // 检查是否还有需要报错的明细（不带项目的明细）
                boolean hasError = detailValidationResultMap.values().stream().anyMatch("1"::equals);
                if (hasError) {
                    throw new IllegalStateException("部分明细处理失败，详见明细错误信息");
                }
            }
            
            // 批量提取 budgetSubjectCode 字段
            Set<String> budgetSubjectCodeSet = demandDetails.stream()
                    .map(ApplyDetailDetalVo::getBudgetSubjectCode)
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
            
            // 将白名单科目标记为成功
            if (!whitelistSubjectCodes.isEmpty()) {
                for (ApplyDetailDetalVo detail : demandDetails) {
                    String detailLineNo = detail.getDemandDetailLineNo();
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
            
            // 批量提取 masterProjectCode 字段
            Set<String> masterProjectCodeSet = demandDetails.stream()
                    .filter(detail -> !isProjectSkipBudgetDetail(detail.getBudgetSubjectCode(), detail.getMasterProjectCode()))
                    .map(ApplyDetailDetalVo::getMasterProjectCode)
                    .filter(StringUtils::isNotBlank)
                    .filter(code -> !"NAN".equals(code))
                    .collect(Collectors.toSet());
            
            // 校验预算科目映射：未映射的 ERP_ACCT_CD 逐明细提示，避免整单一起报错
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
                for (ApplyDetailDetalVo detail : demandDetails) {
                    String detailLineNo = detail.getDemandDetailLineNo();
                    if (StringUtils.isBlank(detailLineNo)) {
                        continue;
                    }
                    String subjectCode = detail.getBudgetSubjectCode();
                    String masterProjectCode = detail.getMasterProjectCode();
                    
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
                        detailValidationResultMap.putIfAbsent(detailLineNo, "0");
                        detailValidationMessageMap.putIfAbsent(detailLineNo, "校验通过 Verification Passed");
                    }
                }
                // 检查是否还有需要报错的明细（不带项目的明细）
                boolean hasError = detailValidationResultMap.values().stream().anyMatch("1"::equals);
                if (hasError) {
                    throw new IllegalStateException("部分明细处理失败，详见明细错误信息");
                }
            }
            
            // 批量查询 PROJECT_CONTROL_EXT_R 表，获取 PRJ_CD 对应的所有 RELATED_PRJ_CD（一对多关系）
            // Map<PRJ_CD, List<RELATED_PRJ_CD>>
            Map<String, List<String>> prjCdToRelatedPrjCdExtMap = budgetQueryHelperService.queryPrjCdToRelatedPrjCdMap(masterProjectCodeSet);
            
            // 校验项目编码映射：未映射的 masterProjectCode 逐明细提示，避免整单一起报错
            // 带有效项目编码的明细必须能通过项目关联映射，与白名单科目无关
            Set<String> unmappedPrjCds = new HashSet<>(masterProjectCodeSet);
            unmappedPrjCds.removeAll(prjCdToRelatedPrjCdExtMap.keySet());
            if (!unmappedPrjCds.isEmpty()) {
                Set<String> normalizedUnmappedPrjCds = unmappedPrjCds.stream()
                        .filter(StringUtils::isNotBlank)
                        .map(StringUtils::trim)
                        .collect(Collectors.toSet());
                boolean hasMappedProjectErrorToDetail = false;
                for (ApplyDetailDetalVo detail : demandDetails) {
                    String detailLineNo = detail.getDemandDetailLineNo();
                    if (StringUtils.isBlank(detailLineNo)) {
                        continue;
                    }
                    String masterProjectCode = detail.getMasterProjectCode();
                    String normalizedMasterProjectCode = StringUtils.trim(masterProjectCode);
                    // 只检查有效的项目编码（非空且不是 "NAN"）
                    if (StringUtils.isNotBlank(normalizedMasterProjectCode) && !"NAN".equals(normalizedMasterProjectCode)
                            && normalizedUnmappedPrjCds.contains(normalizedMasterProjectCode)) {
                        detailValidationResultMap.put(detailLineNo, "1");
                        detailValidationMessageMap.put(detailLineNo,
                                "[" + normalizedMasterProjectCode + "]未找到对应的关联项目,还请联系管理员增加项目映射 Invalid Project Code");
                        hasMappedProjectErrorToDetail = true;
                    } else {
                        detailValidationResultMap.putIfAbsent(detailLineNo, "0");
                        detailValidationMessageMap.putIfAbsent(detailLineNo, "校验通过 Verification Passed");
                    }
                }
                // 兜底：只要存在未映射项目编码，必须返回失败，避免因项目编码格式差异导致漏标失败
                if (!hasMappedProjectErrorToDetail) {
                    for (ApplyDetailDetalVo detail : demandDetails) {
                        String detailLineNo = detail.getDemandDetailLineNo();
                        if (StringUtils.isBlank(detailLineNo)) {
                            continue;
                        }
                        String normalizedMasterProjectCode = StringUtils.trim(detail.getMasterProjectCode());
                        if (StringUtils.isNotBlank(normalizedMasterProjectCode)
                                && !"NAN".equals(normalizedMasterProjectCode)
                                && normalizedUnmappedPrjCds.contains(normalizedMasterProjectCode)) {
                            detailValidationResultMap.put(detailLineNo, "1");
                            detailValidationMessageMap.put(detailLineNo,
                                    "[" + normalizedMasterProjectCode + "]未找到对应的关联项目,还请联系管理员增加项目映射 Invalid Project Code");
                            hasMappedProjectErrorToDetail = true;
                        }
                    }
                }
                if (!hasMappedProjectErrorToDetail && !demandDetails.isEmpty()) {
                    ApplyDetailDetalVo fallbackDetail = demandDetails.get(0);
                    if (StringUtils.isNotBlank(fallbackDetail.getDemandDetailLineNo())) {
                        detailValidationResultMap.put(fallbackDetail.getDemandDetailLineNo(), "1");
                        detailValidationMessageMap.put(fallbackDetail.getDemandDetailLineNo(),
                                "存在未映射项目编码: " + String.join(",", normalizedUnmappedPrjCds));
                    }
                }
                // 不抛出异常，而是标记错误，在方法最后统一返回错误响应，避免事务回滚导致 UnexpectedRollbackException
                log.error("部分明细处理失败，直接构造带明细错误的响应返回");
                // 直接使用前面已经查询好的 ehrCdToEhrNmMap 和 erpAcctCdToErpAcctNmMap，避免重复查询
                return buildResponseWithDetailErrors(budgetApplicationParams, detailValidationResultMap, detailValidationMessageMap, ehrCdToEhrNmMap, erpAcctCdToErpAcctNmMap);
            }
            
            // 包含所有明细（含白名单科目），白名单科目也需生成流水明细记录，仅跳过预算校验/余额扣减（在后续流程中通过不受控逻辑识别并跳过）
            for (ApplyDetailDetalVo detail : demandDetails) {
                ApplyExtDetailVo extDetail = new ApplyExtDetailVo();
                BeanUtils.copyProperties(detail, extDetail);
                extDetail.setDemandOrderNo(applyInfo.getDemandOrderNo());
                
                // 转换 metadata: Map<String, String> -> JSON 字符串
                if (detail.getMetadata() != null && !detail.getMetadata().isEmpty()) {
                    String metadataJson = convertMapToJson(detail.getMetadata());
                    extDetail.setMetadataJson(metadataJson);
                }
                
                applyExtDetailsForQuery.add(extDetail);
                if (year == null && detail.getDemandYear() != null) {
                    year = detail.getDemandYear();
                }
            }

            // 查询预算流水
            List<BudgetLedger> existingLedgers = budgetLedgerMapper.selectByExtDetails(applyExtDetailsForQuery);
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

            // 补充已有流水的 morgCode 到 EHR 组织映射（一对一 + 扩展），便于：1）维度比较时将「同一预算组织」的不同 EHR 编码视为一致；2）后续更新阶段查询 quota/balance 时扩展映射能解析流水上的 morgCode
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
                    for (Map.Entry<String, String> e : existingMorgMapResult.getEhrCdToOrgCdMap().entrySet()) {
                        if (!ehrCdToOrgCdExtMap.containsKey(e.getKey())) {
                            ehrCdToOrgCdExtMap.put(e.getKey(), Collections.singletonList(e.getValue()));
                        }
                    }
                }
                Map<String, List<String>> existingExtMap = budgetQueryHelperService.queryEhrCdToOrgCdMap(missingMorgCodesForMap);
                if (existingExtMap != null && !existingExtMap.isEmpty()) {
                    ehrCdToOrgCdExtMap.putAll(existingExtMap);
                    log.info("========== 申请单 已补充已有流水 morgCode 的 EHR 组织扩展映射，本次补充 {} 条，用于 quota/balance 查询 ==========", existingExtMap.size());
                }
            }

            // 再次提交：整单回滚本单已有占用并逻辑删除本次明细未包含的流水（与付款/合同语义一致）
            Set<String> applyRetainedLedgerBizKeys = applyExtDetailsForQuery.stream()
                    .map(ext -> ext.getDemandOrderNo() + "@" + ext.getDemandDetailLineNo())
                    .filter(StringUtils::isNotBlank)
                    .collect(Collectors.toSet());
            Set<String> erpAssetTypeSetForResubmitRollback = new HashSet<>();
            for (ApplyDetailDetalVo detail : demandDetails) {
                String masterProjectCode = detail.getMasterProjectCode();
                boolean isNoProject = "NAN".equals(masterProjectCode) || StringUtils.isBlank(masterProjectCode);
                if (!isNoProject) {
                    continue;
                }
                String erpAssetType = detail.getErpAssetType();
                if (StringUtils.isNotBlank(erpAssetType) && !"NAN".equals(erpAssetType)
                        && (erpAssetType.startsWith("1") || erpAssetType.startsWith("M"))) {
                    erpAssetTypeSetForResubmitRollback.add(erpAssetType);
                }
            }
            erpAssetTypeSetForResubmitRollback.addAll(existingLedgers.stream()
                    .filter(ledger -> {
                        String mpc = ledger.getMasterProjectCode();
                        return "NAN".equals(mpc) || StringUtils.isBlank(mpc);
                    })
                    .map(BudgetLedger::getErpAssetType)
                    .filter(StringUtils::isNotBlank)
                    .filter(code -> !"NAN".equals(code))
                    .filter(code -> code.startsWith("1") || code.startsWith("M"))
                    .collect(Collectors.toSet()));
            Map<String, String> erpAssetTypeToMemberCdMapForResubmitRollback =
                    budgetQueryHelperService.queryErpAssetTypeToMemberCdMap(erpAssetTypeSetForResubmitRollback);
            erpAssetTypeToMemberCdMapForResubmitRollback = erpAssetTypeToMemberCdMapForResubmitRollback == null
                    ? new HashMap<>() : new HashMap<>(erpAssetTypeToMemberCdMapForResubmitRollback);
            handleApplyResubmitRollbackIfNeeded(applyInfo.getDemandOrderNo(), applyInfo.getDocumentStatus(),
                    existingLedgers, existingBudgetLedgerMap, applyRetainedLedgerBizKeys, operator, operatorNo,
                    ehrCdToOrgCdMap, erpAcctCdToAcctCdMap, erpAssetTypeToMemberCdMapForResubmitRollback,
                    ehrCdToOrgCdExtMap, erpAcctCdToAcctCdExtMap, requestTime);

            // 分类处理
            List<ApplyExtDetailVo> needToAddBudgetLedgerList = new ArrayList<>();
            Map<String, BudgetLedger> needRollbackDiffDemBudgetLedgerMap = new HashMap<>();
            Map<String, String> recoverMonthSameDemBudgetLedgerMap = new HashMap<>();
            Map<String, BigDecimal> recoverAmountSameDemBudgetLedgerMap = new HashMap<>();
            Map<String, BudgetLedger> needUpdateSameDemBudgetLedgerMap = new HashMap<>();
            // 存储需要新增的ledger（维度不一致时创建的新记录，如果生成了新ID）
            Map<String, BudgetLedger> needToAddBudgetLedgerMap = new HashMap<>();
            // 存储需要更新的ledger对应的metadataJson，key为ledgerKey（bizCode + "@" + bizItemCode）
            Map<String, String> recoverMetadataSameDemBudgetLedgerMap = new HashMap<>();
            
            // 标记是否有明细处理失败
            boolean hasDetailError = false;

            for (ApplyExtDetailVo extDetail : applyExtDetailsForQuery) {
                String key = extDetail.getDemandOrderNo() + "@" + extDetail.getDemandDetailLineNo();
                String detailLineNo = extDetail.getDemandDetailLineNo();
                
                try {
                    BudgetLedger existingLedger = existingBudgetLedgerMap.get(key);
                    
                    // 如果通过key没有找到，可能是维度变化了，需要遍历同一bizCode下的所有记录进行维度匹配
                    if (existingLedger == null) {
                        // 遍历所有查询到的existingLedgers，查找同一bizCode下是否有维度匹配的记录
                        BudgetLedger matchedLedger = null;
                        for (BudgetLedger ledger : existingLedgers) {
                            // 只检查同一bizCode下的记录
                            if (Objects.equals(ledger.getBizCode(), extDetail.getDemandOrderNo())) {
                                // 检查维度是否匹配（传入 ehrCdToOrgCdMap 时按预算组织比较，同一预算组织的不同 EHR 编码如 015-044-005/015-044-005-001 视为一致）
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
                            recoverMonthSameDemBudgetLedgerMap.put(key, extDetail.getDemandMonth());
                            recoverAmountSameDemBudgetLedgerMap.put(key, extDetail.getDemandAmount());
                            // 存储metadataJson，如果传了就用传入的，如果没传就保留数据库原有的（在更新时处理）
                            if (extDetail.getMetadataJson() != null) {
                                recoverMetadataSameDemBudgetLedgerMap.put(ledgerKey, extDetail.getMetadataJson());
                            }
                            detailValidationResultMap.put(detailLineNo, "0");
                            detailValidationMessageMap.put(detailLineNo, "校验通过 Verification Passed");
                            log.info("========== 维度一致，需要更新（通过维度匹配找到）: bizItemCode={}, 旧amount={}, 旧amountConsumedQOne={}, 旧amountConsumedQTwo={}, 旧amountConsumedQThree={}, 旧amountConsumedQFour={}, 新demandAmount={}, 旧month={}, 新month={} ==========",
                                    extDetail.getDemandDetailLineNo(),
                                    matchedLedger.getAmount() == null ? BigDecimal.ZERO : matchedLedger.getAmount(),
                                    matchedLedger.getAmountConsumedQOne() == null ? BigDecimal.ZERO : matchedLedger.getAmountConsumedQOne(),
                                    matchedLedger.getAmountConsumedQTwo() == null ? BigDecimal.ZERO : matchedLedger.getAmountConsumedQTwo(),
                                    matchedLedger.getAmountConsumedQThree() == null ? BigDecimal.ZERO : matchedLedger.getAmountConsumedQThree(),
                                    matchedLedger.getAmountConsumedQFour() == null ? BigDecimal.ZERO : matchedLedger.getAmountConsumedQFour(),
                                    extDetail.getDemandAmount(),
                                    matchedLedger.getMonth(), extDetail.getDemandMonth());
                        } else {
                            // 维度不一致时，通过行号匹配同一业务行的旧记录并回滚
                            // 因为行号代表业务上的同一行，维度变化（包括预算科目变化）时都应该回滚旧记录
                            // 注意：同一行号可能有多条旧记录（不同预算科目），需要全部回滚
                            List<BudgetLedger> rollbackLedgers = new ArrayList<>();
                            
                            for (BudgetLedger ledger : existingLedgers) {
                                // 只检查同一bizCode下的记录
                                if (Objects.equals(ledger.getBizCode(), extDetail.getDemandOrderNo())) {
                                    // 提取行号部分（第一个@之前的部分）
                                    String detailLineNoPrefix = extDetail.getDemandDetailLineNo();
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
                                String newSubjectCode = extractBudgetSubjectCode(extDetail.getDemandDetailLineNo());
                                for (BudgetLedger rollbackLedger : rollbackLedgers) {
                                    String ledgerKey = rollbackLedger.getBizCode() + "@" + rollbackLedger.getBizItemCode();
                                    needRollbackDiffDemBudgetLedgerMap.put(ledgerKey, rollbackLedger);
                                }
                                log.info("========== 维度不一致，需要回滚（行号匹配）: 新明细行号={}, 新budgetSubjectCode={}, 同行号旧流水条数={}, 旧bizItemCodes={}, 新demandAmount={} ==========",
                                        extDetail.getDemandDetailLineNo(),
                                        newSubjectCode,
                                        rollbackLedgers.size(),
                                        rollbackLedgers.stream().map(BudgetLedger::getBizItemCode).collect(Collectors.toList()),
                                        extDetail.getDemandAmount());
                                detailValidationResultMap.put(detailLineNo, "0");
                                detailValidationMessageMap.put(detailLineNo, "校验通过 Verification Passed");
                            } else {
                                // 没有找到匹配的记录，当作新增处理
                                needToAddBudgetLedgerList.add(extDetail);
                                detailValidationResultMap.put(detailLineNo, "0");
                                detailValidationMessageMap.put(detailLineNo, "校验通过 Verification Passed");
                                log.info("========== 新增 BudgetLedger: bizItemCode={}, demandAmount={} ==========",
                                        extDetail.getDemandDetailLineNo(), extDetail.getDemandAmount());
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
                            log.info("========== 维度不一致，需要回滚: 新明细行号={}, 旧ledger bizItemCode={}, ledgerKey={}, 旧amount={}, 旧amountConsumedQOne={}, 旧amountConsumedQTwo={}, 旧amountConsumedQThree={}, 旧amountConsumedQFour={}, 新demandAmount={} ==========",
                                    extDetail.getDemandDetailLineNo(),
                                    existingLedger.getBizItemCode(),
                                    ledgerKey,
                                    existingLedger.getAmount() == null ? BigDecimal.ZERO : existingLedger.getAmount(),
                                    existingLedger.getAmountConsumedQOne() == null ? BigDecimal.ZERO : existingLedger.getAmountConsumedQOne(),
                                    existingLedger.getAmountConsumedQTwo() == null ? BigDecimal.ZERO : existingLedger.getAmountConsumedQTwo(),
                                    existingLedger.getAmountConsumedQThree() == null ? BigDecimal.ZERO : existingLedger.getAmountConsumedQThree(),
                                    existingLedger.getAmountConsumedQFour() == null ? BigDecimal.ZERO : existingLedger.getAmountConsumedQFour(),
                                    extDetail.getDemandAmount());
                        } else {
                            String ledgerKey = existingLedger.getBizCode() + "@" + existingLedger.getBizItemCode();
                            needUpdateSameDemBudgetLedgerMap.put(ledgerKey, existingLedger);
                            recoverMonthSameDemBudgetLedgerMap.put(key, extDetail.getDemandMonth());
                            recoverAmountSameDemBudgetLedgerMap.put(key, extDetail.getDemandAmount());
                            // 存储metadataJson，如果传了就用传入的，如果没传就保留数据库原有的（在更新时处理）
                            if (extDetail.getMetadataJson() != null) {
                                recoverMetadataSameDemBudgetLedgerMap.put(ledgerKey, extDetail.getMetadataJson());
                            }
                            detailValidationResultMap.put(detailLineNo, "0");
                            detailValidationMessageMap.put(detailLineNo, "校验通过 Verification Passed");
                        log.info("========== 维度一致，需要更新: bizItemCode={}, 旧amount={}, 旧amountConsumedQOne={}, 旧amountConsumedQTwo={}, 旧amountConsumedQThree={}, 旧amountConsumedQFour={}, 新demandAmount={}, 旧month={}, 新month={} ==========",
                                extDetail.getDemandDetailLineNo(),
                                existingLedger.getAmount() == null ? BigDecimal.ZERO : existingLedger.getAmount(),
                                existingLedger.getAmountConsumedQOne() == null ? BigDecimal.ZERO : existingLedger.getAmountConsumedQOne(),
                                existingLedger.getAmountConsumedQTwo() == null ? BigDecimal.ZERO : existingLedger.getAmountConsumedQTwo(),
                                existingLedger.getAmountConsumedQThree() == null ? BigDecimal.ZERO : existingLedger.getAmountConsumedQThree(),
                                existingLedger.getAmountConsumedQFour() == null ? BigDecimal.ZERO : existingLedger.getAmountConsumedQFour(),
                                extDetail.getDemandAmount(),
                                existingLedger.getMonth(), extDetail.getDemandMonth());
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
                throw new IllegalStateException("部分明细处理失败，详见明细错误信息");
            }
            
            log.info("========== 分类统计: 新增={}, 维度不一致回滚={}, 维度一致更新={} ==========",
                    needToAddBudgetLedgerList.size(), needRollbackDiffDemBudgetLedgerMap.size(), needUpdateSameDemBudgetLedgerMap.size());

            // 批量提取需要映射的 erpAssetType 字段（以 "1" 或 "M" 开头且不带项目的）
            Set<String> erpAssetTypeSet = new HashSet<>();
            if (!needRollbackDiffDemBudgetLedgerMap.isEmpty()) {
                erpAssetTypeSet.addAll(needRollbackDiffDemBudgetLedgerMap.values().stream()
                        .filter(ledger -> {
                            String masterProjectCode = ledger.getMasterProjectCode();
                            return "NAN".equals(masterProjectCode) || StringUtils.isBlank(masterProjectCode);
                        })
                        .map(BudgetLedger::getErpAssetType)
                        .filter(StringUtils::isNotBlank)
                        .filter(code -> !"NAN".equals(code))
                        .filter(code -> code.startsWith("1") || code.startsWith("M"))
                        .collect(Collectors.toSet()));
            }
            if (!needUpdateSameDemBudgetLedgerMap.isEmpty()) {
                erpAssetTypeSet.addAll(needUpdateSameDemBudgetLedgerMap.values().stream()
                        .filter(ledger -> {
                            String masterProjectCode = ledger.getMasterProjectCode();
                            return "NAN".equals(masterProjectCode) || StringUtils.isBlank(masterProjectCode);
                        })
                        .map(BudgetLedger::getErpAssetType)
                        .filter(StringUtils::isNotBlank)
                        .filter(code -> !"NAN".equals(code))
                        .filter(code -> code.startsWith("1") || code.startsWith("M"))
                        .collect(Collectors.toSet()));
            }
            // 从新增的明细中提取 erpAssetType（这些明细后续会转换为 BudgetLedger 并调用 processSameDimensionUpdate）
            if (!needToAddBudgetLedgerList.isEmpty()) {
                erpAssetTypeSet.addAll(needToAddBudgetLedgerList.stream()
                        .filter(detail -> {
                            String masterProjectCode = detail.getMasterProjectCode();
                            return "NAN".equals(masterProjectCode) || StringUtils.isBlank(masterProjectCode);
                        })
                        .map(ApplyExtDetailVo::getErpAssetType)
                        .filter(StringUtils::isNotBlank)
                        .filter(code -> !"NAN".equals(code))
                        .filter(code -> code.startsWith("1") || code.startsWith("M"))
                        .collect(Collectors.toSet()));
            }
            
            // 批量查询 VIEW_BUDGET_MEMBER_NAME_CODE 视图，获取 MEMBER_CD2 对应的 MEMBER_CD（一一对应关系）
            Map<String, String> erpAssetTypeToMemberCdMap = budgetQueryHelperService.queryErpAssetTypeToMemberCdMap(erpAssetTypeSet);
            log.info("========== 获取到 erpAssetType 映射表，共 {} 条记录 ==========", erpAssetTypeToMemberCdMap.size());

            // 处理维度不一致的数据（回滚逻辑）
            Map<String, BudgetQuota> needToUpdateDiffDemBudgetQuotaMap = new HashMap<>();
            Map<String, BudgetBalance> needToUpdateDiffDemBudgetBalanceMap = new HashMap<>();
            List<BudgetQuotaHistory> needToAddBudgetQuotaHistory = new ArrayList<>();
            List<BudgetBalanceHistory> needToAddBudgetBalanceHistory = new ArrayList<>();

            if (!needRollbackDiffDemBudgetLedgerMap.isEmpty()) {
                log.info("========== 准备处理维度不一致回滚，needRollbackDiffDemBudgetLedgerMap size={}, keys={} ==========",
                        needRollbackDiffDemBudgetLedgerMap.size(), needRollbackDiffDemBudgetLedgerMap.keySet());
                for (Map.Entry<String, BudgetLedger> entry : needRollbackDiffDemBudgetLedgerMap.entrySet()) {
                    BudgetLedger ledger = entry.getValue();
                    log.info("========== needRollbackDiffDemBudgetLedgerMap 中的记录: key={}, id={}, bizItemCode={}, amount={} ==========",
                            entry.getKey(), ledger.getId(), ledger.getBizItemCode(), ledger.getAmount());
                }
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
                    Set<String> ledgerKeysAlreadyRolledBack = needRollbackDiffDemBudgetLedgerMap.isEmpty() ? null : new HashSet<>(needRollbackDiffDemBudgetLedgerMap.keySet());
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
                    // 设置操作人字段
                    ledger.setOperator(operator);
                    ledger.setOperatorNo(operatorNo);
                    // 如果没传metadata，ledger.getMetadata()保持数据库原有的值，不需要修改
                    log.info("========== 更新 BudgetLedger: bizKey={}, bizItemCode={}, 旧amount={}, 新amount={}, 旧amountAvailable={}, 新amountAvailable={}, 旧month={}, 新month={}, 旧version={}, 新version={}, metadata={} ==========",
                            entry.getKey(), ledger.getBizItemCode(), oldAmount, newAmount, oldAmountAvailable, newAmount, oldMonth, newMonth, oldVersion, ledger.getVersion(), 
                            newMetadata != null ? "已更新" : "保留原值");
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
            List<ApplyExtDetailVo> applyDetailsIncludeExtNewDemForQueryBoth = new ArrayList<>();
            for (ApplyExtDetailVo extDetail : applyExtDetailsForQuery) {
                String demandDetailLineNo = extDetail.getDemandDetailLineNo();
                // 提取行号部分（第一个@之前的部分）
                String rowNo = demandDetailLineNo.contains("@") ? demandDetailLineNo.substring(0, demandDetailLineNo.indexOf("@")) : demandDetailLineNo;
                if (diffDemRowNoSet.contains(rowNo)) {
                    applyDetailsIncludeExtNewDemForQueryBoth.add(extDetail);
                }
            }

            // 收集需要删除的旧BudgetLedger ID（维度不一致的旧记录）
            // 注意：同一行号可能有多条旧记录（不同预算科目），需要全部删除
            Set<Long> needToDeleteOldLedgerIds = new HashSet<>();
            // 从needRollbackDiffDemBudgetLedgerMap中收集所有需要删除的旧记录ID
            for (BudgetLedger oldLedger : needRollbackDiffDemBudgetLedgerMap.values()) {
                needToDeleteOldLedgerIds.add(oldLedger.getId());
            }
            
            if (!applyDetailsIncludeExtNewDemForQueryBoth.isEmpty()) {
                log.info("========== 维度不一致，需要创建新的 BudgetLedger，共 {} 条明细 ==========", applyDetailsIncludeExtNewDemForQueryBoth.size());
                // 创建新的 BudgetLedger
                for (ApplyExtDetailVo extDetail : applyDetailsIncludeExtNewDemForQueryBoth) {
                    // 通过行号找到对应的旧 BudgetLedger（维度不一致时，行号相同的旧记录需要被回滚）
                    String demandDetailLineNo = extDetail.getDemandDetailLineNo();
                    String rowNo = demandDetailLineNo.contains("@") ? demandDetailLineNo.substring(0, demandDetailLineNo.indexOf("@")) : demandDetailLineNo;
                    List<BudgetLedger> oldLedgersForRow = diffDemRowNoToOldLedgersMap.get(rowNo);
                    if (oldLedgersForRow == null || oldLedgersForRow.isEmpty()) {
                        log.warn("========== 维度不一致创建新BudgetLedger时，未找到同行号旧流水: rowNo={} ==========", rowNo);
                        continue;
                    }
                    BudgetLedger oldLedger = oldLedgersForRow.get(0);
                    String oldBizItemCode = oldLedger.getBizItemCode();

                    // 检查新维度的记录是否已经存在（避免使用错误的旧ID更新已存在的新记录）
                    // 如果新维度的记录已经存在，应该直接更新它，而不是用旧ID创建新记录
                    String newDetailLineNo = extDetail.getDemandDetailLineNo();
                    String newKey = extDetail.getDemandOrderNo() + "@" + newDetailLineNo;
                    BudgetLedger existingNewLedger = existingBudgetLedgerMap.get(newKey);
                    if (existingNewLedger != null) {
                        needToDeleteOldLedgerIds.remove(existingNewLedger.getId());
                        log.warn("========== 维度不一致创建新BudgetLedger时，发现新维度的记录已存在，跳过创建以避免覆盖: newKey={}, existingLedgerId={}, 已从待删除列表移除 ledgerId={} ==========",
                                newKey, existingNewLedger.getId(), existingNewLedger.getId());
                        continue;
                    }

                    BigDecimal demandAmount = extDetail.getDemandAmount() == null ? BigDecimal.ZERO : extDetail.getDemandAmount();
                    // 维度不一致时，总是生成新ID，不复用旧ID
                    // 因为维度不一致意味着这是完全不同的记录，应该删除旧记录，创建新记录
                    Long ledgerId = identifierGenerator.nextId(null).longValue();
                    log.info("========== 维度不一致创建新BudgetLedger时，生成新ID: 旧ID={}, 新ID={}, oldBizItemCode={}, newBizItemCode={} ==========", 
                            oldLedger.getId(), ledgerId, oldBizItemCode, newDetailLineNo);
                    // 创建新的 BudgetLedger（createBudgetLedger 方法内部会处理空值：budgetSubjectCode->NAN-NAN, masterProjectCode->NAN, erpAssetType->NAN）
                    BudgetLedger newLedger = budgetQueryHelperService.createBudgetLedger(
                            ledgerId,
                            DEFAULT_BIZ_TYPE,
                            extDetail.getDemandOrderNo(),
                            extDetail.getDemandDetailLineNo(),
                            extDetail.getDemandYear(),
                            extDetail.getDemandMonth(),
                            extDetail.getIsInternal(),
                            extDetail.getManagementOrg(),
                            extDetail.getBudgetSubjectCode(),
                            extDetail.getMasterProjectCode(),
                            extDetail.getErpAssetType(),
                            DEFAULT_CURRENCY,
                            demandAmount,
                            oldLedger.getVersion(),
                            identifierGenerator,
                            operator,
                            operatorNo,
                            requestTime
                    );

                    // 设置 metadata 字段：如果传了metadata就使用传入的，如果没传就保留旧ledger的metadata
                    if (extDetail.getMetadataJson() != null) {
                        newLedger.setMetadata(extDetail.getMetadataJson());
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
                    // （如仍为 66020202）在检查 existingBudgetLedgerMap.get(newKey) 时会得到 null，导致重复创建新 ledger
                    // 且不会从 needToDeleteOldLedgerIds 中移除该旧 ledger id，最终会误删已更新的流水，流水明细只剩一个科目。
                    String oldLedgerKey = oldLedger.getBizCode() + "@" + oldLedger.getBizItemCode();
                    if (Objects.equals(ledgerKey, oldLedgerKey)) {
                        existingBudgetLedgerMap.remove(oldLedgerKey);
                        existingBudgetLedgerMap.put(ledgerKey, newLedger);
                    } else {
                        existingBudgetLedgerMap.put(ledgerKey, newLedger);
                    }
                }
            } else {
                log.info("========== 维度不一致，但 applyDetailsIncludeExtNewDemForQueryBoth 为空，未创建新的 BudgetLedger ==========");
            }

            // 合并从维度不一致处理中创建的 map，并处理新增数据
            // 注意：这些新创建的 ledger 已经在 needUpdateSameDemBudgetLedgerMap 中
            // 将完全新增的 ledger 也合并到 needUpdateSameDemBudgetLedgerMap 中，统一使用 processSameDimensionUpdate 处理
            
            // 用于存储可用预算数值信息的 Map
            Map<String, DetailNumberVo> availableBudgetRatioMap = new HashMap<>();
            
            // 用于收集所有需要新增的 BudgetPoolDemR
            Map<String, BudgetPoolDemR> allNeedToAddPoolDemRMap = new HashMap<>();
            
            // 记录每个关联单据的扣减金额（按季度），key格式：ledgerId + "@" + relatedLedgerId + "@" + quarter，value为扣减金额
            // 这个Map会在 performMultiQuarterDeduction 中填充，然后在插入 BUDGET_LEDGER_SELF_R 时使用
            Map<String, BigDecimal> relatedLedgerDeductionAmountMap = new HashMap<>();
            
            // 处理维度一致更新的 ledger（加入明细级别错误处理）
            // 若有维度不一致回滚，传入回滚后的 balanceMap，使同池同季度使用回滚后金额（含被跳过同维回滚的 ledger），避免 amountFrozen 等错误
            if (!needUpdateSameDemBudgetLedgerMap.isEmpty()) {
                try {
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
                for (ApplyExtDetailVo extDetail : needToAddBudgetLedgerList) {
                    String key = extDetail.getDemandOrderNo() + "@" + extDetail.getDemandDetailLineNo();
                    
                    // 使用雪花算法生成器生成 id 和 version
                    Long ledgerId = identifierGenerator.nextId(null).longValue();
                    BigDecimal demandAmount = extDetail.getDemandAmount() == null ? BigDecimal.ZERO : extDetail.getDemandAmount();
                    
                    // 使用 budgetQueryHelperService.createBudgetLedger 创建 BudgetLedger（方法内部会处理空值转换）
                    BudgetLedger ledger = budgetQueryHelperService.createBudgetLedger(
                            ledgerId,
                            DEFAULT_BIZ_TYPE,
                            extDetail.getDemandOrderNo(),
                            extDetail.getDemandDetailLineNo(),
                            extDetail.getDemandYear(),
                            extDetail.getDemandMonth(),
                            extDetail.getIsInternal(),
                            extDetail.getManagementOrg(),
                            extDetail.getBudgetSubjectCode(),
                            extDetail.getMasterProjectCode(),
                            extDetail.getErpAssetType(),
                            extDetail.getCurrency() != null ? extDetail.getCurrency() : DEFAULT_CURRENCY,
                            demandAmount,
                            null, // versionPre 为 null（新建）
                            identifierGenerator,
                            operator,
                            operatorNo,
                            requestTime
                    );
                    
                    // 设置 metadata 字段：如果传了metadata就使用传入的，如果没传就不设置（新建时可以为null）
                    if (extDetail.getMetadataJson() != null) {
                        ledger.setMetadata(extDetail.getMetadataJson());
                    }
                    
                    // 将新增的 ledger 保存到 needToAddBudgetLedgerMap 中（用于后续 insertBatch）
                    needToAddBudgetLedgerMap.put(key, ledger);
                    log.info("========== 创建新的 BudgetLedger: key={}, id={}, bizCode={}, bizItemCode={}, amount={}, version={} ==========",
                            key, ledgerId, ledger.getBizCode(), ledger.getBizItemCode(), demandAmount, ledger.getVersion());
                }
            }
            
            // 单独处理新增的 ledger（包括完全新增的明细和维度不一致创建的新记录），使用 processSameDimensionUpdate 方法
            // 若有维度不一致回滚，传入回滚后的 balanceMap，使同池同季度使用回滚后金额，避免 amountFrozen 等被重复扣减成负数
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
                log.error("部分明细处理失败，直接构造带明细错误的响应返回");
                // 直接构造并返回带明细错误信息的响应，而不是抛出异常
                return buildResponseWithDetailErrors(budgetApplicationParams, detailValidationResultMap, detailValidationMessageMap, ehrCdToEhrNmMap, erpAcctCdToErpAcctNmMap);
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

            // 12. 处理 BUDGET_LEDGER_HEAD
            String bizCode = applyInfo.getDemandOrderNo();
            budgetQueryHelperService.createOrUpdateBudgetLedgerHead(bizCode, DEFAULT_BIZ_TYPE, 
                    documentName, dataSource, processName, "SUBMITTED", identifierGenerator, operator, operatorNo, requestTime);

            // 构建返回结果
            return buildResponse(budgetApplicationParams, detailValidationResultMap, detailValidationMessageMap, availableBudgetRatioMap, ehrCdToEhrNmMap, erpAcctCdToErpAcctNmMap);
        } catch (IllegalStateException e) {
            // 明细级别的错误，返回带有明细错误信息的响应
            if ("部分明细处理失败，详见明细错误信息".equals(e.getMessage())) {
                log.error("部分明细处理失败", e);
                // 从 demandDetails 中提取 managementOrgSet 和 budgetSubjectCodeSet
                Set<String> managementOrgSetForError = applyInfo != null && applyInfo.getDemandDetails() != null 
                    ? applyInfo.getDemandDetails().stream()
                        .map(ApplyDetailDetalVo::getManagementOrg)
                        .filter(StringUtils::isNotBlank)
                        .collect(Collectors.toSet())
                    : Collections.emptySet();
                Set<String> budgetSubjectCodeSetForError = applyInfo != null && applyInfo.getDemandDetails() != null 
                    ? applyInfo.getDemandDetails().stream()
                        .map(ApplyDetailDetalVo::getBudgetSubjectCode)
                        .filter(StringUtils::isNotBlank)
                        .filter(code -> !"NAN-NAN".equals(code))
                        .collect(Collectors.toSet())
                    : Collections.emptySet();
                Map<String, String> ehrCdToEhrNmMap = CollectionUtils.isEmpty(managementOrgSetForError) 
                    ? Collections.emptyMap() 
                    : budgetQueryHelperService.queryEhrCdToOrgCdOneToOneMap(managementOrgSetForError).getEhrCdToEhrNmMap();
                Map<String, String> erpAcctCdToErpAcctNmMap = CollectionUtils.isEmpty(budgetSubjectCodeSetForError) 
                    ? Collections.emptyMap() 
                    : budgetQueryHelperService.queryErpAcctCdToAcctCdOneToOneMap(budgetSubjectCodeSetForError).getErpAcctCdToErpAcctNmMap();
                return buildResponseWithDetailErrors(budgetApplicationParams, detailValidationResultMap, detailValidationMessageMap, ehrCdToEhrNmMap, erpAcctCdToErpAcctNmMap);
            }
            // 其他 IllegalStateException，作为整单错误处理
            log.error("预算申请处理失败", e);
            // 从 demandDetails 中提取 managementOrgSet 和 budgetSubjectCodeSet
            Set<String> managementOrgSetForError = applyInfo != null && applyInfo.getDemandDetails() != null 
                ? applyInfo.getDemandDetails().stream()
                    .map(ApplyDetailDetalVo::getManagementOrg)
                    .filter(StringUtils::isNotBlank)
                    .collect(Collectors.toSet())
                : Collections.emptySet();
            Set<String> budgetSubjectCodeSetForError = applyInfo != null && applyInfo.getDemandDetails() != null 
                ? applyInfo.getDemandDetails().stream()
                    .map(ApplyDetailDetalVo::getBudgetSubjectCode)
                    .filter(StringUtils::isNotBlank)
                    .filter(code -> !"NAN-NAN".equals(code))
                    .collect(Collectors.toSet())
                : Collections.emptySet();
            Map<String, String> ehrCdToEhrNmMap = CollectionUtils.isEmpty(managementOrgSetForError) 
                ? Collections.emptyMap() 
                : budgetQueryHelperService.queryEhrCdToOrgCdOneToOneMap(managementOrgSetForError).getEhrCdToEhrNmMap();
            Map<String, String> erpAcctCdToErpAcctNmMap = CollectionUtils.isEmpty(budgetSubjectCodeSetForError) 
                ? Collections.emptyMap() 
                : budgetQueryHelperService.queryErpAcctCdToAcctCdOneToOneMap(budgetSubjectCodeSetForError).getErpAcctCdToErpAcctNmMap();
            return buildErrorResponseForAllDetails(budgetApplicationParams, e, ehrCdToEhrNmMap, erpAcctCdToErpAcctNmMap);
        } catch (Exception e) {
            // 其他异常，作为整单错误处理
            log.error("预算申请处理失败", e);
            // 从 demandDetails 中提取 managementOrgSet 和 budgetSubjectCodeSet
            Set<String> managementOrgSetForError = applyInfo != null && applyInfo.getDemandDetails() != null 
                ? applyInfo.getDemandDetails().stream()
                    .map(ApplyDetailDetalVo::getManagementOrg)
                    .filter(StringUtils::isNotBlank)
                    .collect(Collectors.toSet())
                : Collections.emptySet();
            Set<String> budgetSubjectCodeSetForError = applyInfo != null && applyInfo.getDemandDetails() != null 
                ? applyInfo.getDemandDetails().stream()
                    .map(ApplyDetailDetalVo::getBudgetSubjectCode)
                    .filter(StringUtils::isNotBlank)
                    .filter(code -> !"NAN-NAN".equals(code))
                    .collect(Collectors.toSet())
                : Collections.emptySet();
            Map<String, String> ehrCdToEhrNmMap = CollectionUtils.isEmpty(managementOrgSetForError) 
                ? Collections.emptyMap() 
                : budgetQueryHelperService.queryEhrCdToOrgCdOneToOneMap(managementOrgSetForError).getEhrCdToEhrNmMap();
            Map<String, String> erpAcctCdToErpAcctNmMap = CollectionUtils.isEmpty(budgetSubjectCodeSetForError) 
                ? Collections.emptyMap() 
                : budgetQueryHelperService.queryErpAcctCdToAcctCdOneToOneMap(budgetSubjectCodeSetForError).getErpAcctCdToErpAcctNmMap();
            return buildErrorResponseForAllDetails(budgetApplicationParams, e, ehrCdToEhrNmMap, erpAcctCdToErpAcctNmMap);
        }
    }

    private boolean isDimensionSame(ApplyExtDetailVo extDetail, BudgetLedger ledger) {
        return isDimensionSame(extDetail, ledger, null);
    }

    /**
     * 判断维度是否一致。当传入 ehrCdToOrgCdMap 时，管理组织按「预算组织」比较：
     * 若请求与流水的 EHR 编码（如 015-044-005 与 015-044-005-001）映射到同一 ORG_CD，则视为同一组织。
     */
    private boolean isDimensionSame(ApplyExtDetailVo extDetail, BudgetLedger ledger, Map<String, String> ehrCdToOrgCdMap) {
        // 处理空值转换（包括 null 和空字符串）
        String extBudgetSubjectCode = StringUtils.isBlank(extDetail.getBudgetSubjectCode()) ? "NAN-NAN" : extDetail.getBudgetSubjectCode();
        String extMasterProjectCode = StringUtils.isBlank(extDetail.getMasterProjectCode()) ? "NAN" : extDetail.getMasterProjectCode();
        String extErpAssetType = StringUtils.isBlank(extDetail.getErpAssetType()) ? "NAN" : extDetail.getErpAssetType();
        String extIsInternal = StringUtils.isBlank(extDetail.getIsInternal()) ? "1" : extDetail.getIsInternal();
        String ledgerIsInternal = StringUtils.isBlank(ledger.getIsInternal()) ? "1" : ledger.getIsInternal();
        boolean needCheckIsInternal = !"NAN".equals(extMasterProjectCode);

        // 管理组织：有映射表时按「预算组织」比较，否则按原始编码比较
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

    @Override
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
        // 调用父类方法，父类会调用抽象方法 rollbackBalanceAmount
        super.processDiffDimensionRollback(needRollbackDiffDemBudgetLedgerMap, needToUpdateDiffDemBudgetQuotaMap,
                needToUpdateDiffDemBudgetBalanceMap, needToAddBudgetQuotaHistory, needToAddBudgetBalanceHistory, ehrCdToOrgCdMap, erpAcctCdToAcctCdMap, erpAssetTypeToMemberCdMap, ehrCdToOrgCdExtMap, erpAcctCdToAcctCdExtMap);
        // TODO: 使用 ehrCdToOrgCdMap 和 erpAcctCdToAcctCdMap 实现具体逻辑
    }

    @Override
    protected void rollbackBalanceAmountForDiffDimension(BudgetBalance balance, BudgetBalanceHistory balanceHistory,
                                                         BudgetLedger ledger, String rollbackQuarter, 
                                                         BigDecimal rollbackAmount, Long poolId) {
        BigDecimal amountFrozenBefore = balance.getAmountFrozen() == null ? BigDecimal.ZERO : balance.getAmountFrozen();
        BigDecimal amountAvailableBefore = balance.getAmountAvailable() == null ? BigDecimal.ZERO : balance.getAmountAvailable();
        
        log.info("========== processDiffDimensionRollback - 释放冻结金额: poolId={}, bizItemCode={}, quarter={}, rollbackAmount={}, 释放前 amountFrozen={}, amountAvailable={} ==========",
                poolId, ledger.getBizItemCode(), rollbackQuarter, rollbackAmount, amountFrozenBefore, amountAvailableBefore);
        
        // 维度不一致的回滚，不能扣减为负数，如果回滚金额大于当前冻结金额，则最多回滚当前冻结金额
        BigDecimal actualRollbackAmount = rollbackAmount;
        if (rollbackAmount.compareTo(amountFrozenBefore) > 0) {
            actualRollbackAmount = amountFrozenBefore;
            log.warn("========== processDiffDimensionRollback - 回滚金额大于当前冻结金额，调整为当前冻结金额: poolId={}, bizItemCode={}, quarter={}, 原回滚金额={}, 调整后回滚金额={} ==========",
                    poolId, ledger.getBizItemCode(), rollbackQuarter, rollbackAmount, actualRollbackAmount);
        }
        
        BigDecimal newAmountFrozen = amountFrozenBefore.subtract(actualRollbackAmount);
        balance.setAmountFrozen(newAmountFrozen);
        balance.setAmountFrozenVchanged(actualRollbackAmount.negate());
        balance.setAmountAvailable(amountAvailableBefore.add(actualRollbackAmount));
        balance.setAmountAvailableVchanged(actualRollbackAmount);
    }

    @Override
    protected Map<String, List<BudgetLedger>> rollbackBalanceAmountForSameDimension(BudgetBalance balance, BudgetBalanceHistory balanceHistory,
                                                         BudgetLedger ledger, String rollbackQuarter, 
                                                         BigDecimal rollbackAmount, Long poolId,
                                                         Map<String, List<BudgetLedger>> relatedBudgetLedgerMap,
                                                         Map<Long, Map<String, BigDecimal>> aggregatedRelatedRollbackMap,
                                                         Set<String> appliedRelatedLedgerQuarterSet) {
        BigDecimal amountFrozenBefore = balance.getAmountFrozen() == null ? BigDecimal.ZERO : balance.getAmountFrozen();
        BigDecimal amountAvailableBefore = balance.getAmountAvailable() == null ? BigDecimal.ZERO : balance.getAmountAvailable();
        
        log.info("========== processSameDimensionRollback - 释放冻结金额: poolId={}, bizItemCode={}, quarter={}, rollbackAmount={}, 释放前 amountFrozen={}, amountAvailable={} ==========",
                poolId, ledger.getBizItemCode(), rollbackQuarter, rollbackAmount, amountFrozenBefore, amountAvailableBefore);
        
        // 维度一致的回滚，可以扣减为负数，因为回滚完之后还会再冻结或占用
        balance.setAmountFrozen(amountFrozenBefore.subtract(rollbackAmount));
        balance.setAmountFrozenVchanged(rollbackAmount.negate());
        balance.setAmountAvailable(amountAvailableBefore.add(rollbackAmount));
        balance.setAmountAvailableVchanged(rollbackAmount);
        
        return relatedBudgetLedgerMap;
    }

    @Override
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
        // 调用父类方法，父类会调用抽象方法 rollbackBalanceAmountForSameDimension
        return super.processSameDimensionRollback(needUpdateSameDemBudgetLedgerMap, needToUpdateSameDemBudgetQuotaMap,
                needToUpdateSameDemBudgetBalanceMap, needToAddBudgetQuotaHistory, needToAddBudgetBalanceHistory, ehrCdToOrgCdMap, erpAcctCdToAcctCdMap, erpAssetTypeToMemberCdMap, ehrCdToOrgCdExtMap, erpAcctCdToAcctCdExtMap, ledgerKeysAlreadyRolledBackByDiffDimension);
    }

    @Override
    protected String getBizType() {
        return DEFAULT_BIZ_TYPE;
    }

    @Override
    protected BudgetValidationResult getBudgetValidationResult(String bizKey, 
                                                                Map<String, List<BudgetLedger>> updatedRelatedBudgetLedgerMap) {
        // APPLY 类型使用默认计算方式
        return new BudgetValidationResult(false, null, BigDecimal.ZERO);
    }

    @Override
    protected BigDecimal getCurrentAmountOperated(List<BudgetBalance> balanceList) {
        // APPLY 类型返回 amountFrozen 之和
        BigDecimal totalAmountFrozen = BigDecimal.ZERO;
        for (BudgetBalance balance : balanceList) {
            BigDecimal amountFrozen = balance.getAmountFrozen() == null ? BigDecimal.ZERO : balance.getAmountFrozen();
            totalAmountFrozen = totalAmountFrozen.add(amountFrozen);
        }
        return totalAmountFrozen;
    }

    @Override
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
        // 调用父类方法，父类会调用抽象方法 performMultiQuarterDeduction
        super.processSameDimensionUpdate(needUpdateSameDemBudgetLedgerMap, needToUpdateSameDemBudgetQuotaMap,
                needToUpdateSameDemBudgetBalanceMap, needToAddBudgetQuotaHistory, needToAddBudgetBalanceHistory, 
                availableBudgetRatioMap, updatedRelatedBudgetLedgerMap,
                ehrCdToOrgCdMap, ehrCdToOrgCdExtMap, erpAcctCdToAcctCdMap, erpAcctCdToAcctCdExtMap, prjCdToRelatedPrjCdExtMap,
                needToAddPoolDemRMap, erpAssetTypeToMemberCdMap, relatedLedgerDeductionAmountMap, needToUpdateDiffDemBudgetBalanceMapForMerge);
    }


    @Override
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
        
        // 1. 更新 BUDGET_BALANCE（维度不一致）
        if (!needToUpdateDiffDemBudgetBalanceMap.isEmpty()) {
            List<BudgetBalance> balancesToUpdate = sortBalancesById(new ArrayList<>(needToUpdateDiffDemBudgetBalanceMap.values()));
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

        // 6.5 填充扣减来源：扣减在关联流水时填 deductionFromLedgerBizKey，否则填 poolDimensionKey（优先用 BUDGET_BALANCE 维度）
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
                log.info("========== 准备更新 BudgetLedger: id={}, bizCode={}, bizItemCode={}, amount={}, amountAvailable={}, version={} ==========",
                        ledger.getId(), ledger.getBizCode(), ledger.getBizItemCode(), ledger.getAmount(), ledger.getAmountAvailable(), ledger.getVersion());
            }
            budgetLedgerMapper.updateBatchById(ledgersToUpdate);
            log.info("========== 批量更新 BudgetLedger 完成，共更新 {} 条记录 ==========", ledgersToUpdate.size());
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
    }

    private List<BudgetQuota> loadBudgetQuotasByPoolIds(List<Long> poolIds) {
        return budgetQueryHelperService.loadBudgetQuotasByPoolIds(poolIds);
    }

    private List<BudgetBalance> loadBudgetBalancesByPoolIds(List<Long> poolIds) {
        return budgetQueryHelperService.loadBudgetBalancesByPoolIds(poolIds);
    }

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

    private String buildBudgetSubjectCode(String custom1, String account) {
        if (StringUtils.isAnyBlank(custom1, account)) {
            return null;
        }
        return custom1 + "-" + account;
    }

    @Override
    protected String convertMonthToQuarter(String month) {
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

    private int compareQuarter(String quarter1, String quarter2) {
        if (quarter1 == null || quarter2 == null) {
            return 0;
        }
        Map<String, Integer> quarterMap = Map.of("q1", 1, "q2", 2, "q3", 3, "q4", 4);
        Integer q1 = quarterMap.get(quarter1);
        Integer q2 = quarterMap.get(quarter2);
        if (q1 == null || q2 == null) {
            return 0;
        }
        return Integer.compare(q1, q2);
    }

    private BigDecimal calculateQuarterAmount(String morgCode, String budgetSubjectCode, String masterProjectCode,
                                              String fromQuarter, String toQuarter,
                                              Map<String, SystemProjectBudget> budgetDataMap) {
        String key = morgCode + "@" + budgetSubjectCode + "@" + masterProjectCode;
        SystemProjectBudget budget = budgetDataMap.get(key);
        if (budget == null) {
            return BigDecimal.ZERO;
        }

        int fromQ = getQuarterNumber(fromQuarter);
        int toQ = getQuarterNumber(toQuarter);
        
        // 如果 fromQ > toQ，交换它们以确保循环能正常执行
        if (fromQ > toQ) {
            int temp = fromQ;
            fromQ = toQ;
            toQ = temp;
        }
        
        BigDecimal total = BigDecimal.ZERO;
        for (int i = fromQ; i <= toQ; i++) {
            BigDecimal amount = getQuarterAmount(budget, i);
            if (amount != null) {
                total = total.add(amount);
            }
        }
        return total;
    }

    private BigDecimal getQuarterAmount(SystemProjectBudget budget, int quarter) {
        switch (quarter) {
            case 1: return budget.getQ1() == null ? BigDecimal.ZERO : budget.getQ1();
            case 2: return budget.getQ2() == null ? BigDecimal.ZERO : budget.getQ2();
            case 3: return budget.getQ3() == null ? BigDecimal.ZERO : budget.getQ3();
            case 4: return budget.getQ4() == null ? BigDecimal.ZERO : budget.getQ4();
            default: return BigDecimal.ZERO;
        }
    }


    /**
     * 根据月份获取季度消耗金额（从 BudgetLedger 的季度字段中读取）
     */
    @Override
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
     * 执行跨季度扣减计算
     * 从当前季度开始，依次往上季度扣减，直到第一个季度
     * 
     * @param bizKey 业务key
     * @param currentQuarter 当前季度
     * @param totalAmountToOperate 需要操作的总金额
     * @param balanceMap 季度余额Map
     * @param quarterOperateAmountMap 输出参数：每个季度操作的金额（key为季度，value为操作金额）
     */
    @Override
    protected void performMultiQuarterDeduction(Long currentLedgerId, String bizKey, String currentQuarter, BigDecimal totalAmountToOperate,
                                          Map<String, List<BudgetBalance>> balanceMap,
                                          Map<String, BudgetBalance> needToUpdateSameDemBudgetBalanceMap,
                                          Map<String, BigDecimal> quarterOperateAmountMap,
                                          Map<String, List<BudgetLedger>> updatedRelatedBudgetLedgerMap,
                                          Map<String, BigDecimal> relatedLedgerDeductionAmountMap) {
        BigDecimal remainingAmount = totalAmountToOperate;
        int currentQuarterNum = getQuarterNumber(currentQuarter);

        // 从当前季度开始，依次往上季度扣减（往前推，即从当前季度到 q1）
        String[] quarters = {"q1", "q2", "q3", "q4"};
        for (int i = currentQuarterNum - 1; i >= 0 && remainingAmount.compareTo(BigDecimal.ZERO) > 0; i--) {
            String quarter = quarters[i];
            String bizKeyQuarter = bizKey + "@" + quarter;

            BudgetBalance balance = needToUpdateSameDemBudgetBalanceMap.get(bizKeyQuarter);
            if (balance == null) {
                log.error("========== performMultiQuarterDeduction - 季度 {} 的 balance 不存在 ==========", quarter);
                throw new IllegalStateException(
                        String.format("明细 [%s] 的维度组合在季度 %s 未找到对应的预算余额。" +
                                      "请检查 BUDGET_POOL_DEM_R 表是否配置了该维度组合在该季度的预算。",
                                      bizKey, quarter)
                );
            }

            // 从 balanceMap 获取 balanceList 备用
            List<BudgetBalance> balanceList = balanceMap.get(bizKeyQuarter);

            // 累加 balanceList 下所有 balance 的 amountAvailable 合计备用
            BigDecimal totalAmountAvailableFromList = BigDecimal.ZERO;
            if (!CollectionUtils.isEmpty(balanceList)) {
                for (BudgetBalance balanceItem : balanceList) {
                    BigDecimal amountAvailable = balanceItem.getAmountAvailable() == null ? BigDecimal.ZERO : balanceItem.getAmountAvailable();
                    totalAmountAvailableFromList = totalAmountAvailableFromList.add(amountAvailable);
                }
            }

            BigDecimal quarterAmountAvailable = balance.getAmountAvailable() == null ? BigDecimal.ZERO : balance.getAmountAvailable();
            BigDecimal quarterAmountFrozen = balance.getAmountFrozen() == null ? BigDecimal.ZERO : balance.getAmountFrozen();

            // 统一逻辑：每个季度最多只扣当季（含控制层级聚合）的可用额；不再在 skipBudgetValidation=true 时一次性把剩余全扣在当前季
            BigDecimal amountToFreezeThisQuarter = remainingAmount.min(totalAmountAvailableFromList);

            if (amountToFreezeThisQuarter.compareTo(BigDecimal.ZERO) > 0) {
                // 记录本季度操作的金额（累加，如果该季度已经有操作金额）
                BigDecimal existingAmount = quarterOperateAmountMap.getOrDefault(quarter, BigDecimal.ZERO);
                quarterOperateAmountMap.put(quarter, existingAmount.add(amountToFreezeThisQuarter));

                // 更新本季度的余额
                balance.setAmountAvailable(quarterAmountAvailable.subtract(amountToFreezeThisQuarter));
                balance.setAmountFrozen(quarterAmountFrozen.add(amountToFreezeThisQuarter));

                // 更新变化量
                BigDecimal amountFrozenVchanged = balance.getAmountFrozenVchanged() == null ? BigDecimal.ZERO : balance.getAmountFrozenVchanged();
                BigDecimal amountAvailableVchanged = balance.getAmountAvailableVchanged() == null ? BigDecimal.ZERO : balance.getAmountAvailableVchanged();
                balance.setAmountFrozenVchanged(amountFrozenVchanged.add(amountToFreezeThisQuarter));
                balance.setAmountAvailableVchanged(amountAvailableVchanged.subtract(amountToFreezeThisQuarter));

                remainingAmount = remainingAmount.subtract(amountToFreezeThisQuarter);

                log.info("========== performMultiQuarterDeduction - 季度 {} 扣减: 冻结金额={}, 剩余待冻结={}, 扣减后amountAvailable={}, amountFrozen={} ==========",
                        quarter, amountToFreezeThisQuarter, remainingAmount, balance.getAmountAvailable(), balance.getAmountFrozen());
            }
        }

        // 关闭预算校验（重跑场景）时：若跨季度用光所有正向可用额后仍有缺口，则只允许在 q1 继续透支
        if (isSkipBudgetValidation() && remainingAmount.compareTo(BigDecimal.ZERO) > 0) {
            String q1Key = bizKey + "@q1";
            BudgetBalance q1Balance = needToUpdateSameDemBudgetBalanceMap.get(q1Key);
            if (q1Balance == null) {
                log.error("========== performMultiQuarterDeduction - Q1 的 balance 不存在，无法进行透支扣减: bizKey={} ==========", bizKey);
                throw new IllegalStateException(
                        String.format("明细 [%s] 的维度组合在季度 q1 未找到对应的预算余额，无法进行重跑透支扣减。" +
                                      "请检查 BUDGET_POOL_DEM_R 表是否配置了该维度组合在该季度的预算。",
                                      bizKey)
                );
            }

            BigDecimal q1Avail = q1Balance.getAmountAvailable() == null ? BigDecimal.ZERO : q1Balance.getAmountAvailable();
            BigDecimal q1Frozen = q1Balance.getAmountFrozen() == null ? BigDecimal.ZERO : q1Balance.getAmountFrozen();
            BigDecimal q1AvailVchanged = q1Balance.getAmountAvailableVchanged() == null ? BigDecimal.ZERO : q1Balance.getAmountAvailableVchanged();
            BigDecimal q1FrozenVchanged = q1Balance.getAmountFrozenVchanged() == null ? BigDecimal.ZERO : q1Balance.getAmountFrozenVchanged();

            // 记录 Q1 的操作金额（累加模式）
            BigDecimal existingQ1 = quarterOperateAmountMap.getOrDefault("q1", BigDecimal.ZERO);
            quarterOperateAmountMap.put("q1", existingQ1.add(remainingAmount));

            // 允许 Q1 变成负数：amountAvailable = 原可用 - 剩余缺口
            q1Balance.setAmountAvailable(q1Avail.subtract(remainingAmount));
            q1Balance.setAmountFrozen(q1Frozen.add(remainingAmount));
            q1Balance.setAmountAvailableVchanged(q1AvailVchanged.subtract(remainingAmount));
            q1Balance.setAmountFrozenVchanged(q1FrozenVchanged.add(remainingAmount));

            log.info("========== performMultiQuarterDeduction - Q1 透支扣减: 透支金额={}, 扣减后amountAvailable={}, amountFrozen={} ==========",
                    remainingAmount, q1Balance.getAmountAvailable(), q1Balance.getAmountFrozen());

            remainingAmount = BigDecimal.ZERO;
        }

        // 开启预算校验模式下仍保持原有校验：扣减结束后必须没有剩余
        if (remainingAmount.compareTo(BigDecimal.ZERO) > 0 && !isSkipBudgetValidation()) {
            log.error("========== performMultiQuarterDeduction - 扣减后仍有剩余金额未冻结: remainingAmount={} ==========", remainingAmount);
            throw new IllegalStateException("扣减后仍有剩余金额未冻结，remainingAmount=" + remainingAmount);
        }
    }


    /**
     * 根据月份设置季度消耗金额（设置到 BudgetLedger 的对应季度字段中）
     */
    private void setConsumedAmountByMonth(BudgetLedger ledger, BigDecimal amount) {
        if (ledger == null || StringUtils.isBlank(ledger.getMonth()) || amount == null) {
            return;
        }
        String quarter = convertMonthToQuarter(ledger.getMonth());
        if (quarter == null) {
            return;
        }
        switch (quarter) {
            case "q1": ledger.setAmountConsumedQOne(amount); break;
            case "q2": ledger.setAmountConsumedQTwo(amount); break;
            case "q3": ledger.setAmountConsumedQThree(amount); break;
            case "q4": ledger.setAmountConsumedQFour(amount); break;
        }
    }

    private List<ApplyDetailDetalVo> defaultList(List<ApplyDetailDetalVo> demandDetails) {
        return demandDetails == null ? Collections.emptyList() : demandDetails;
    }

    private BudgetApplicationRespVo buildResponse(BudgetApplicationParams budgetApplicationParams,
                                                  Map<String, String> detailValidationResultMap,
                                                  Map<String, String> detailValidationMessageMap,
                                                  Map<String, DetailNumberVo> availableBudgetRatioMap,
                                                  Map<String, String> ehrCdToEhrNmMap,
                                                  Map<String, String> erpAcctCdToErpAcctNmMap) {
        ESBInfoParams esbInfo = budgetApplicationParams.getEsbInfo();
        ApplyReqInfoParams applyInfo = budgetApplicationParams.getApplyReqInfo();
        
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
                .returnCode("A0001-BUDGET")
                .returnMsg("预算申请处理成功")
                .returnStatus("S")
                .responseTime(responseTime)
                .build();

        // 构建返回的需求明细列表（包含每个明细的校验结果）
        List<ApplyDetailRespVo> resultDemandDetails = new ArrayList<>();
        List<ApplyDetailDetalVo> demandDetails = applyInfo.getDemandDetails();
        if (demandDetails != null) {
            for (ApplyDetailDetalVo detail : demandDetails) {
                ApplyDetailRespVo resultDetail = new ApplyDetailRespVo();
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
                String detailLineNo = detail.getDemandDetailLineNo();
                resultDetail.setValidationResult(detailValidationResultMap.getOrDefault(detailLineNo, "0"));
                resultDetail.setValidationMessage(detailValidationMessageMap.getOrDefault(detailLineNo, "处理成功"));

                // 设置预算相关数值
                DetailNumberVo detailNumberVo = availableBudgetRatioMap != null ? availableBudgetRatioMap.get(detailLineNo) : null;
                if (detailNumberVo != null) {
                    resultDetail.setAvailableBudgetRatio(detailNumberVo.getAvailableBudgetRatio());
                    resultDetail.setAmountQuota(detailNumberVo.getAmountQuota());
                    resultDetail.setAmountFrozen(detailNumberVo.getAmountFrozen());
                    resultDetail.setAmountActual(detailNumberVo.getAmountActual());
                    resultDetail.setAmountAvailable(detailNumberVo.getAmountAvailable());
                }
                
                resultDemandDetails.add(resultDetail);
            }
        }

        ApplyResultInfoRespVo applyResult = new ApplyResultInfoRespVo();
        applyResult.setDemandOrderNo(applyInfo.getDemandOrderNo());
        applyResult.setProcessTime(LocalDateTime.now().format(PROCESS_TIME_FORMATTER));
        applyResult.setDemandDetails(resultDemandDetails);

        BudgetApplicationRespVo respVo = new BudgetApplicationRespVo();
        respVo.setEsbInfo(esbRespInfo);
        respVo.setApplyResult(applyResult);
        return respVo;
    }

    /**
     * 构建整单错误响应（所有明细都报同样的错）
     */
    private BudgetApplicationRespVo buildErrorResponseForAllDetails(BudgetApplicationParams budgetApplicationParams, Exception e, Map<String, String> ehrCdToEhrNmMap, Map<String, String> erpAcctCdToErpAcctNmMap) {
        ESBInfoParams esbInfo = budgetApplicationParams.getEsbInfo();
        ApplyReqInfoParams applyInfo = budgetApplicationParams.getApplyReqInfo();
        
        String errorMessage = e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName();
        
        String instId = esbInfo.getInstId();
        if (instId == null || instId.isEmpty()) {
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
        
        ESBRespInfoVo esbRespInfo = ESBRespInfoVo.builder()
                .instId(instId)
                .requestTime(esbInfo.getRequestTime())
                .attr1(esbInfo.getAttr1())
                .attr2(esbInfo.getAttr2())
                .attr3(esbInfo.getAttr3())
                .returnCode("E0001-BUDGET")
                .returnMsg(errorMessage)
                .returnStatus(returnStatus)
                .responseTime(responseTime)
                .build();

        // 构建返回的需求明细列表（错误情况下，所有明细都标记为失败）
        List<ApplyDetailRespVo> resultDemandDetails = new ArrayList<>();
        if (applyInfo != null && applyInfo.getDemandDetails() != null) {
            for (ApplyDetailDetalVo detail : applyInfo.getDemandDetails()) {
                ApplyDetailRespVo resultDetail = new ApplyDetailRespVo();
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
                resultDemandDetails.add(resultDetail);
            }
        }

        ApplyResultInfoRespVo applyResult = new ApplyResultInfoRespVo();
        applyResult.setDemandOrderNo(applyInfo != null ? applyInfo.getDemandOrderNo() : null);
        applyResult.setProcessTime(LocalDateTime.now().format(PROCESS_TIME_FORMATTER));
        applyResult.setDemandDetails(resultDemandDetails);

        BudgetApplicationRespVo respVo = new BudgetApplicationRespVo();
        respVo.setEsbInfo(esbRespInfo);
        respVo.setApplyResult(applyResult);
        return respVo;
    }
    
    /**
     * 构建明细级别错误响应（部分明细成功，部分明细失败）
     */
    private BudgetApplicationRespVo buildResponseWithDetailErrors(BudgetApplicationParams budgetApplicationParams,
                                                                  Map<String, String> detailValidationResultMap,
                                                                  Map<String, String> detailValidationMessageMap,
                                                                  Map<String, String> ehrCdToEhrNmMap,
                                                                  Map<String, String> erpAcctCdToErpAcctNmMap) {
        ESBInfoParams esbInfo = budgetApplicationParams.getEsbInfo();
        ApplyReqInfoParams applyInfo = budgetApplicationParams.getApplyReqInfo();
        
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
                .returnCode(hasError ? "E0001-BUDGET" : "A0001-BUDGET")
                .returnMsg(hasError ? "部分明细处理失败" : "预算申请处理成功")
                .returnStatus(returnStatus)
                .responseTime(responseTime)
                .build();

        // 构建返回的需求明细列表（包含每个明细的校验结果）
        List<ApplyDetailRespVo> resultDemandDetails = new ArrayList<>();
        List<ApplyDetailDetalVo> demandDetails = applyInfo.getDemandDetails();
        if (demandDetails != null) {
            for (ApplyDetailDetalVo detail : demandDetails) {
                ApplyDetailRespVo resultDetail = new ApplyDetailRespVo();
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
                String detailLineNo = detail.getDemandDetailLineNo();
                resultDetail.setValidationResult(detailValidationResultMap.getOrDefault(detailLineNo, "1"));
                resultDetail.setValidationMessage(detailValidationMessageMap.getOrDefault(detailLineNo, "未处理"));
                
                resultDemandDetails.add(resultDetail);
            }
        }

        ApplyResultInfoRespVo applyResult = new ApplyResultInfoRespVo();
        applyResult.setDemandOrderNo(applyInfo.getDemandOrderNo());
        applyResult.setProcessTime(LocalDateTime.now().format(PROCESS_TIME_FORMATTER));
        applyResult.setDemandDetails(resultDemandDetails);

        BudgetApplicationRespVo respVo = new BudgetApplicationRespVo();
        respVo.setEsbInfo(esbRespInfo);
        respVo.setApplyResult(applyResult);
        return respVo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BudgetRenewRespVo renew(BudgetRenewParams budgetRenewParams) {
        log.info("开始处理预算申请审批/驳回/撤销，params={}", budgetRenewParams);

        try {
            RenewApplyReqInfoParams renewInfo = budgetRenewParams.getRenewApplyReqInfo();
            // 去除 BOM 和首尾空格，避免入参带 ﻿(U+FEFF) 导致按 bizCode 查不到流水头
            String demandOrderNo = normalizeBizCode(renewInfo.getDemandOrderNo());
            String documentStatus = renewInfo.getDocumentStatus();
            LocalDateTime requestTime = parseRequestTime(budgetRenewParams.getEsbInfo());
            String renewYear = resolveRenewYear(demandOrderNo, renewInfo);
            if (StringUtils.isNotBlank(renewYear) && !isCurrentYear(renewYear)
                    && ("REJECTED".equals(documentStatus) || "CANCELLED".equals(documentStatus) || "CLOSED".equals(documentStatus))) {
                log.info("预算申请跨年跳过审批/驳回/撤销处理，demandOrderNo={}, documentStatus={}, year={}, currentYear={}",
                        demandOrderNo, documentStatus, renewYear, LocalDate.now().getYear());
                return handleRejectedOrCancelledSkipRollback(demandOrderNo, documentStatus, budgetRenewParams);
            }
            if ("APPROVED".equals(documentStatus)) {
                // 场景二：APPROVED 但明细没有金额
                return handleApprovedWithoutAmount(demandOrderNo, budgetRenewParams, requestTime);
            } else if ("REJECTED".equals(documentStatus) || "CANCELLED".equals(documentStatus) || "CLOSED".equals(documentStatus)) {
                // 场景三：REJECTED、CANCELLED 或 CLOSED
                return handleRejectedOrCancelled(demandOrderNo, documentStatus, budgetRenewParams);
            } else {
                // 其他状态报错
                throw new IllegalArgumentException("不支持的单据状态：" + documentStatus);
            }
        } catch (IllegalArgumentException e) {
            // 业务参数校验错误，接口调用成功，返回 "S"
            log.error("预算申请审批/驳回/撤销参数校验失败", e);
            return buildErrorResponse(budgetRenewParams, e, true);
        } catch (IllegalStateException e) {
            // 业务状态错误，接口调用成功，返回 "S"
            log.error("预算申请审批/驳回/撤销业务处理失败", e);
            return buildErrorResponse(budgetRenewParams, e, true);
        } catch (Exception e) {
            // 系统异常，接口调用失败，返回 "F"
            log.error("预算申请审批/驳回/撤销处理失败", e);
            return buildErrorResponse(budgetRenewParams, e, false);
        }
    }

    /**
     * 场景一：APPROVED 且明细有金额
     * 注：当前 renew() 未调用此方法，若后续接入需传入 requestTime
     */
    private BudgetRenewRespVo handleApprovedWithAmount(String demandOrderNo, RenewApplyReqInfoParams renewInfo, BudgetRenewParams budgetRenewParams, LocalDateTime requestTime) {
        log.info("========== 场景一：APPROVED 且明细有金额，demandOrderNo={} ==========", demandOrderNo);
        
        // 步骤一：查询 BUDGET_LEDGER_HEAD
        LambdaQueryWrapper<BudgetLedgerHead> headWrapper = new LambdaQueryWrapper<>();
        headWrapper.eq(BudgetLedgerHead::getBizCode, demandOrderNo)
                .eq(BudgetLedgerHead::getDeleted, Boolean.FALSE);
        BudgetLedgerHead existingHead = budgetLedgerHeadMapper.selectOne(headWrapper);
        
        if (existingHead == null) {
            throw new IllegalStateException("未找到对应的预算流水头，业务单号：" + demandOrderNo);
        }
        log.info("========== 查询到 BudgetLedgerHead: id={}, status={}, version={} ==========",
                existingHead.getId(), existingHead.getStatus(), existingHead.getVersion());

        // 步骤二：处理明细
        Map<String, RenewExtDetailVo> renewExtDetailMap = new HashMap<>();
        List<RenewDetailDetailVo> demandDetails = renewInfo.getDemandDetails() != null ? 
                renewInfo.getDemandDetails() : Collections.emptyList();
        
        for (RenewDetailDetailVo detail : demandDetails) {
            if (detail.getDemandAmount() != null && detail.getDemandAmount().compareTo(BigDecimal.ZERO) > 0) {
                RenewExtDetailVo extDetail = new RenewExtDetailVo();
                BeanUtils.copyProperties(detail, extDetail);
                extDetail.setDemandOrderNo(demandOrderNo);
                String key = demandOrderNo + "@" + detail.getDemandDetailLineNo();
                renewExtDetailMap.put(key, extDetail);
                log.info("========== 添加 RenewExtDetailVo: key={}, demandAmount={} ==========",
                        key, extDetail.getDemandAmount());
            }
        }

        if (renewExtDetailMap.isEmpty()) {
            throw new IllegalStateException("没有有效的明细金额数据");
        }

        // 查询 BUDGET_LEDGER
        List<RenewExtDetailVo> renewExtDetailList = new ArrayList<>(renewExtDetailMap.values());
        List<BudgetLedger> existingLedgers = budgetLedgerMapper.selectByRenewExtDetails(renewExtDetailList);
        
        // 白名单校验：如果所有记录的科目编码都不在白名单中且不带项目编码，直接返回成功，不进行后续处理
        if (!existingLedgers.isEmpty()) {
            boolean allNotInWhitelist = true;
            for (BudgetLedger ledger : existingLedgers) {
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
                // 所有记录的科目编码都不在白名单中且不带项目编码，直接返回成功，不进行后续处理
                log.info("所有记录的科目编码都不在白名单中且不带项目编码，直接返回成功响应，跳过预算校验");
                return buildSuccessResponse(budgetRenewParams, "预算申请审批成功");
            }
        }
        
        Map<String, BudgetLedger> existingBudgetLedgerMap = existingLedgers.stream()
                .collect(Collectors.toMap(
                        ledger -> ledger.getBizCode() + "@" + ledger.getBizItemCode(),
                        Function.identity(),
                        (a, b) -> a
                ));
        log.info("========== 查询到 {} 条 BudgetLedger ==========", existingLedgers.size());

        // 处理金额比较和更新
        Map<String, BudgetLedger> needToUpdateBudgetLedgerMap = new HashMap<>();
        List<BudgetLedgerHistory> needToAddBudgetLedgerHistory = new ArrayList<>();
        // 保存每个季度减少的冻结金额，key: bizCode+"@"+bizItemCode+"@"+quarter
        Map<String, BigDecimal> quarterFrozenReductionMap = new HashMap<>();

        for (Map.Entry<String, BudgetLedger> entry : existingBudgetLedgerMap.entrySet()) {
            String key = entry.getKey();
            BudgetLedger ledger = entry.getValue();
            RenewExtDetailVo extDetail = renewExtDetailMap.get(key);
            
            if (extDetail == null) {
                continue;
            }

            BigDecimal ledgerAmountFrozenConsumed = ledger.getAmount() == null ? BigDecimal.ZERO : ledger.getAmount();
            BigDecimal extAmount = extDetail.getDemandAmount() == null ? BigDecimal.ZERO : extDetail.getDemandAmount();

            log.info("========== 比较金额: key={}, ledgerAmountFrozenConsumed={}, extAmount={} ==========",
                    key, ledgerAmountFrozenConsumed, extAmount);

            if (extAmount.compareTo(ledgerAmountFrozenConsumed) > 0) {
                throw new IllegalStateException("审批不能超过提交的冻结金额，明细行号：" + extDetail.getDemandDetailLineNo() + 
                        "，提交金额：" + ledgerAmountFrozenConsumed + "，审批金额：" + extAmount);
            }

            if (extAmount.compareTo(ledgerAmountFrozenConsumed) < 0) {
                // 需要减少金额
                BigDecimal diff = ledgerAmountFrozenConsumed.subtract(extAmount);
                // 使用维度组合作为 key: year + "@" + quarter + "@" + morgCode + "@" + budgetSubjectCode + "@" + masterProjectCode + "@" + erpAssetType
                String currentQuarter = convertMonthToQuarter(ledger.getMonth());
                if (currentQuarter == null) {
                    throw new IllegalStateException("无法确定季度，month=" + ledger.getMonth() + "，业务单号=" + ledger.getBizCode() + "，业务行号=" + ledger.getBizItemCode());
                }
                String isInternal = "NAN".equals(ledger.getMasterProjectCode()) ? "1" : ledger.getIsInternal();
                String dimensionKey = ledger.getYear() + "@" + currentQuarter + "@" + isInternal + "@" + ledger.getMorgCode() + "@" + ledger.getBudgetSubjectCode() + "@" + ledger.getMasterProjectCode() + "@" + ledger.getErpAssetType();

                // 创建历史记录
                BudgetLedgerHistory history = new BudgetLedgerHistory();
                BeanUtils.copyProperties(ledger, history);
                history.setId(identifierGenerator.nextId(history).longValue());
                history.setLedgerId(ledger.getId());
                history.setDeleted(Boolean.FALSE);
                needToAddBudgetLedgerHistory.add(history);

                // 更新 BudgetLedger
                // 计算实际扣减金额：如果 amountAvailable 小于 diff，则实际扣减金额为 amountAvailable
                BigDecimal currentAmount = ledger.getAmount() == null ? BigDecimal.ZERO : ledger.getAmount();
                BigDecimal currentAmountAvailable = ledger.getAmountAvailable() == null ? BigDecimal.ZERO : ledger.getAmountAvailable();
                
                // 实际扣减金额取 diff 和 currentAmountAvailable 的较小值
                BigDecimal actualDiff = currentAmountAvailable.compareTo(diff) < 0 ? currentAmountAvailable : diff;
                
                // amount 减去 diff，amountAvailable 减去 actualDiff
                ledger.setAmount(currentAmount.subtract(diff));
                ledger.setAmountAvailable(currentAmountAvailable.subtract(actualDiff));
                
                // 重新分配 actualDiff 到各个季度的 amountConsumedQ 字段
                // 从第一个季度开始，一直到当前季度（currentQuarter 已在前面定义）
                BigDecimal remainingDiff = actualDiff;
                String[] quarters = {"q1", "q2", "q3", "q4"};
                String bizKey = ledger.getBizCode() + "@" + ledger.getBizItemCode();
                
                for (String quarter : quarters) {
                    if (remainingDiff.compareTo(BigDecimal.ZERO) <= 0) {
                        break;
                    }
                    
                    // 获取当前季度的消耗金额
                    BigDecimal quarterConsumed;
                    switch (quarter) {
                        case "q1":
                            quarterConsumed = ledger.getAmountConsumedQOne() == null ? BigDecimal.ZERO : ledger.getAmountConsumedQOne();
                            break;
                        case "q2":
                            quarterConsumed = ledger.getAmountConsumedQTwo() == null ? BigDecimal.ZERO : ledger.getAmountConsumedQTwo();
                            break;
                        case "q3":
                            quarterConsumed = ledger.getAmountConsumedQThree() == null ? BigDecimal.ZERO : ledger.getAmountConsumedQThree();
                            break;
                        case "q4":
                            quarterConsumed = ledger.getAmountConsumedQFour() == null ? BigDecimal.ZERO : ledger.getAmountConsumedQFour();
                            break;
                        default:
                            quarterConsumed = BigDecimal.ZERO;
                            break;
                    }
                    
                    // 计算该季度需要扣减的金额
                    BigDecimal quarterDiff = remainingDiff.min(quarterConsumed);
                    
                    if (quarterDiff.compareTo(BigDecimal.ZERO) > 0) {
                        // 检查季度消耗金额是否足够
                        if (quarterConsumed.compareTo(quarterDiff) < 0) {
                            throw new IllegalStateException(quarter.toUpperCase() + "季度消耗金额不足，无法减少金额：业务单号=" + ledger.getBizCode() + 
                                    "，业务行号=" + ledger.getBizItemCode() + "，当前amountConsumed" + quarter.toUpperCase() + "=" + quarterConsumed + "，需要减少=" + quarterDiff);
                        }
                        
                        // 扣减该季度的消耗金额
                        switch (quarter) {
                            case "q1":
                                ledger.setAmountConsumedQOne(quarterConsumed.subtract(quarterDiff));
                                break;
                            case "q2":
                                ledger.setAmountConsumedQTwo(quarterConsumed.subtract(quarterDiff));
                                break;
                            case "q3":
                                ledger.setAmountConsumedQThree(quarterConsumed.subtract(quarterDiff));
                                break;
                            case "q4":
                                ledger.setAmountConsumedQFour(quarterConsumed.subtract(quarterDiff));
                                break;
                        }
                        
                        // 保存该季度减少的冻结金额到 map
                        String quarterKey = bizKey + "@" + quarter;
                        quarterFrozenReductionMap.put(quarterKey, quarterDiff);
                        
                        remainingDiff = remainingDiff.subtract(quarterDiff);
                    }
                    
                    // 如果到达当前季度，停止处理
                    if (quarter.equals(currentQuarter)) {
                        break;
                    }
                }
                
                // 如果还有剩余未分配的 actualDiff，报错
                if (remainingDiff.compareTo(BigDecimal.ZERO) > 0) {
                    throw new IllegalStateException("实际扣减金额无法完全分配到各季度：业务单号=" + ledger.getBizCode() + 
                            "，业务行号=" + ledger.getBizItemCode() + "，剩余未分配金额=" + remainingDiff + "，当前季度=" + currentQuarter);
                }
                
                ledger.setVersionPre(ledger.getVersion());
                ledger.setVersion(String.valueOf(identifierGenerator.nextId(null)));
                needToUpdateBudgetLedgerMap.put(dimensionKey, ledger);

                log.info("========== 需要减少金额: dimensionKey={}, 原始diff={}, 实际扣减={}, 新amount={}, 新amountAvailable={} ==========",
                        dimensionKey, diff, actualDiff, ledger.getAmount(), ledger.getAmountAvailable());
            }
        }

        // 步骤三：更新 BudgetBalance
        Map<String, BudgetBalance> needToUpdateBudgetBalanceMap = new HashMap<>(); // key: bizCode+"@"+bizItemCode+"@"+quarter
        List<BudgetBalanceHistory> needToAddBudgetBalanceHistory = new ArrayList<>();

        if (!needToUpdateBudgetLedgerMap.isEmpty()) {
            // 批量提取需要映射的 erpAssetType 字段（以 "1" 或 "M" 开头且不带项目的）
            Set<String> erpAssetTypeSet = needToUpdateBudgetLedgerMap.values().stream()
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
            
            // 步骤一：构建查询参数，只使用 managementOrg, budgetSubjectCode, masterProjectCode, demandYear（ledger 的 year）
            // 为每个 ledger 创建 4 个查询参数（每个季度一个：q1, q2, q3, q4）
            List<BudgetPoolDemRMapper.DimensionParam> dimensionParams = new ArrayList<>();
            Map<String, BudgetLedger> bizKeyToLedgerMap = new HashMap<>(); // bizCode+"@"+bizItemCode -> ledger
            
            for (BudgetLedger ledger : needToUpdateBudgetLedgerMap.values()) {
                String bizKey = ledger.getBizCode() + "@" + ledger.getBizItemCode();
                bizKeyToLedgerMap.put(bizKey, ledger);
                
                String year = ledger.getYear(); // 使用 ledger 的 year（对应 demandYear）
                String isInternal = "NAN".equals(ledger.getMasterProjectCode()) ? "1" : ledger.getIsInternal();
                String morgCode = ledger.getMorgCode();
                String budgetSubjectCode = ledger.getBudgetSubjectCode();
                String masterProjectCode = ledger.getMasterProjectCode();
                String originalErpAssetType = ledger.getErpAssetType();
                
                // 映射 erpAssetType：如果以 "1" 或 "M" 开头，需要通过映射表映射
                // 注意：带项目时不需要映射 erpAssetType
                BudgetQueryHelperService.MapErpAssetTypeResult erpAssetTypeResult = budgetQueryHelperService.mapErpAssetTypeForQuery(
                        originalErpAssetType, masterProjectCode, erpAssetTypeToMemberCdMap,
                        "查询 BudgetPoolDemR 时，明细 [" + bizKey + "]");
                if (erpAssetTypeResult.hasError()) {
                    throw new IllegalStateException(erpAssetTypeResult.getErrorMessage());
                }
                String erpAssetType = erpAssetTypeResult.getMappedValue();
                
                // 为每个季度创建查询参数（q1, q2, q3, q4）
                String[] quarters = {"q1", "q2", "q3", "q4"};
                for (String quarter : quarters) {
                    dimensionParams.add(new BudgetPoolDemRMapper.DimensionParam(year, quarter, isInternal, morgCode, budgetSubjectCode, masterProjectCode, erpAssetType));
                }
            }
            
            // 步骤二：查询所有季度的 BudgetPoolDemR（分批查询，避免IN子句参数过多导致超时）
            List<BudgetPoolDemR> poolDemRs = budgetQueryHelperService.batchSelectByDimensionsWithYearAndQuarter(dimensionParams, 100);
            
            // 建立 (year, quarter, isInternal, morgCode, budgetSubjectCode, masterProjectCode, erpAssetType) -> poolId 的映射
            Map<String, Long> dimensionKeyToPoolIdMap = new HashMap<>();
            for (BudgetPoolDemR poolDemR : poolDemRs) {
                String isInternal = "NAN".equals(poolDemR.getMasterProjectCode()) ? "1" : poolDemR.getIsInternal();
                String key = poolDemR.getYear() + "@" + poolDemR.getQuarter() + "@" + isInternal + "@" + 
                            poolDemR.getMorgCode() + "@" + poolDemR.getBudgetSubjectCode() + "@" + 
                            poolDemR.getMasterProjectCode() + "@" + poolDemR.getErpAssetType();
                dimensionKeyToPoolIdMap.put(key, poolDemR.getId());
            }
            
            // 收集所有 poolId
            Set<Long> poolIdSet = new HashSet<>(dimensionKeyToPoolIdMap.values());
            List<BudgetBalance> balances = loadBudgetBalancesByPoolIds(new ArrayList<>(poolIdSet));
            
            // 步骤三：组装 balances 到 needToUpdateBudgetBalanceMap，key 为 bizCode+"@"+bizItemCode+"@"+quarter
            for (BudgetLedger ledger : needToUpdateBudgetLedgerMap.values()) {
                String bizKey = ledger.getBizCode() + "@" + ledger.getBizItemCode();
                String year = ledger.getYear();
                String isInternal = "NAN".equals(ledger.getMasterProjectCode()) ? "1" : ledger.getIsInternal();
                String morgCode = ledger.getMorgCode();
                String budgetSubjectCode = ledger.getBudgetSubjectCode();
                String masterProjectCode = ledger.getMasterProjectCode();
                String erpAssetType = ledger.getErpAssetType();
                
                String[] quarters = {"q1", "q2", "q3", "q4"};
                for (String quarter : quarters) {
                    String dimensionKey = year + "@" + quarter + "@" + isInternal + "@" + morgCode + "@" + budgetSubjectCode + "@" + masterProjectCode + "@" + erpAssetType;
                    Long poolId = dimensionKeyToPoolIdMap.get(dimensionKey);
                    
                    if (poolId != null) {
                        BudgetBalance balance = balances.stream()
                                .filter(b -> poolId.equals(b.getPoolId()))
                                .findFirst()
                                .orElse(null);
                        
                        if (balance != null) {
                            String balanceKey = bizKey + "@" + quarter;
                            needToUpdateBudgetBalanceMap.put(balanceKey, balance);
                        }
                    }
                }
            }

            // 步骤四：循环 needToUpdateBudgetLedgerMap 去释放
            for (Map.Entry<String, BudgetLedger> entry : needToUpdateBudgetLedgerMap.entrySet()) {
                BudgetLedger ledger = entry.getValue();
                
                // 项目非 NAN 且 isInternal=1 时跳过释放
                if (!"NAN".equals(ledger.getMasterProjectCode()) && "1".equals(ledger.getIsInternal())) {
                    log.info("========== 内部项目跳过释放: bizKey={}, masterProjectCode={}, isInternal={} ==========",
                            ledger.getBizCode() + "@" + ledger.getBizItemCode(), ledger.getMasterProjectCode(), ledger.getIsInternal());
                    continue;
                }
                
                String bizKey = ledger.getBizCode() + "@" + ledger.getBizItemCode();
                
                // 对每个季度进行处理
                String[] quarters = {"q1", "q2", "q3", "q4"};
                
                for (String quarter : quarters) {
                    // 从 quarterFrozenReductionMap 获取该季度减少的冻结金额
                    String quarterKey = bizKey + "@" + quarter;
                    BigDecimal quarterReduction = quarterFrozenReductionMap.getOrDefault(quarterKey, BigDecimal.ZERO);
                    
                    if (quarterReduction.compareTo(BigDecimal.ZERO) <= 0) {
                        continue;
                    }
                    
                    String balanceKey = bizKey + "@" + quarter;
                    BudgetBalance balance = needToUpdateBudgetBalanceMap.get(balanceKey);
                    if (balance == null) {
                        continue;
                    }
                    
                    // 计算该季度需要释放的金额：不能超过 quarterReduction，也不能超过 amountFrozen
                    BigDecimal currentAmountFrozen = balance.getAmountFrozen() == null ? BigDecimal.ZERO : balance.getAmountFrozen();
                    BigDecimal releaseAmount = quarterReduction.min(currentAmountFrozen);
                    
                    if (releaseAmount.compareTo(BigDecimal.ZERO) <= 0) {
                        continue;
                    }
                    
                    // 创建历史记录
                    BudgetBalanceHistory balanceHistory = new BudgetBalanceHistory();
                    BeanUtils.copyProperties(balance, balanceHistory);
                    balanceHistory.setId(identifierGenerator.nextId(balanceHistory).longValue());
                    balanceHistory.setBalanceId(balance.getId());
                    balanceHistory.setDeleted(Boolean.FALSE);
                    needToAddBudgetBalanceHistory.add(balanceHistory);

                    // 更新 BudgetBalance
                    BigDecimal currentAmountAvailable = balance.getAmountAvailable() == null ? BigDecimal.ZERO : balance.getAmountAvailable();
                    BigDecimal currentAmountFrozenVchanged = balance.getAmountFrozenVchanged() == null ? BigDecimal.ZERO : balance.getAmountFrozenVchanged();
                    BigDecimal currentAmountAvailableVchanged = balance.getAmountAvailableVchanged() == null ? BigDecimal.ZERO : balance.getAmountAvailableVchanged();

                    balance.setAmountFrozen(currentAmountFrozen.subtract(releaseAmount));
                    balance.setAmountFrozenVchanged(currentAmountFrozenVchanged.subtract(releaseAmount));
                    balance.setAmountAvailable(currentAmountAvailable.add(releaseAmount));
                    balance.setAmountAvailableVchanged(currentAmountAvailableVchanged.add(releaseAmount));
                    balance.setVersion(ledger.getVersion());

                    log.info("========== 更新 BudgetBalance: bizKey={}, quarter={}, releaseAmount={}, amountFrozen={}, amountAvailable={} ==========",
                            bizKey, quarter, releaseAmount, balance.getAmountFrozen(), balance.getAmountAvailable());
                }
            }
        }

        // 步骤四：更新数据库
        if (!needToUpdateBudgetBalanceMap.isEmpty()) {
            List<BudgetBalance> sortedBalances = sortBalancesById(new ArrayList<>(needToUpdateBudgetBalanceMap.values()));
            budgetBalanceMapper.updateBatchById(sortedBalances);
        }
        if (!needToAddBudgetBalanceHistory.isEmpty()) {
            budgetBalanceHistoryMapper.insertBatch(needToAddBudgetBalanceHistory);
        }
        if (!needToUpdateBudgetLedgerMap.isEmpty()) {
            List<BudgetLedger> sortedLedgers = sortLedgersById(new ArrayList<>(needToUpdateBudgetLedgerMap.values()));
            if (requestTime != null) {
                for (BudgetLedger ledger : sortedLedgers) {
                    ledger.setUpdateTime(requestTime);
                }
            }
            budgetLedgerMapper.updateBatchById(sortedLedgers);
        }
        if (!needToAddBudgetLedgerHistory.isEmpty()) {
            budgetLedgerHistoryMapper.insertBatch(needToAddBudgetLedgerHistory);
        }

        // 更新 BudgetLedgerHead
        BudgetLedgerHeadHistory headHistory = new BudgetLedgerHeadHistory();
        BeanUtils.copyProperties(existingHead, headHistory);
        headHistory.setId(identifierGenerator.nextId(headHistory).longValue());
        headHistory.setLedgerHeadId(existingHead.getId());
        headHistory.setDeleted(Boolean.FALSE);
        budgetLedgerHeadHistoryMapper.insertBatch(Collections.singletonList(headHistory));

        existingHead.setStatus("APPROVED");
        existingHead.setVersion(String.valueOf(identifierGenerator.nextId(null)));
        if (requestTime != null) {
            existingHead.setUpdateTime(requestTime);
        }
        budgetLedgerHeadMapper.updateById(existingHead);

        log.info("========== 场景一处理完成 ==========");
        return buildSuccessResponse(budgetRenewParams, "预算申请审批成功");
    }

    /**
     * 场景二：APPROVED 但明细没有金额
     */
    private BudgetRenewRespVo handleApprovedWithoutAmount(String demandOrderNo, BudgetRenewParams budgetRenewParams, LocalDateTime requestTime) {
        log.info("========== 场景二：APPROVED 但明细没有金额，demandOrderNo={} ==========", demandOrderNo);
        
        LambdaQueryWrapper<BudgetLedgerHead> headWrapper = new LambdaQueryWrapper<>();
        headWrapper.eq(BudgetLedgerHead::getBizCode, demandOrderNo)
                .eq(BudgetLedgerHead::getDeleted, Boolean.FALSE);
        BudgetLedgerHead existingHead = budgetLedgerHeadMapper.selectOne(headWrapper);
        
        if (existingHead == null) {
            throw new IllegalStateException("未找到对应的预算流水头，业务单号：" + demandOrderNo);
        }

        // 白名单校验：查询该单据下的所有 BudgetLedger，如果所有记录的科目编码都不在白名单中，直接返回成功
        LambdaQueryWrapper<BudgetLedger> ledgerWrapper = new LambdaQueryWrapper<>();
        ledgerWrapper.eq(BudgetLedger::getBizCode, demandOrderNo)
                .eq(BudgetLedger::getBizType, DEFAULT_BIZ_TYPE)
                .eq(BudgetLedger::getDeleted, Boolean.FALSE);
        List<BudgetLedger> allLedgers = budgetLedgerMapper.selectList(ledgerWrapper);
        
        if (!allLedgers.isEmpty()) {
            boolean allNotInWhitelist = true;
            for (BudgetLedger ledger : allLedgers) {
                String code = ledger.getBudgetSubjectCode();
                if (StringUtils.isNotBlank(code) && !"NAN-NAN".equals(code) && budgetSubjectCodeConfig.isInWhitelist(code)) {
                    // 发现有白名单中的科目编码，需要正常处理
                    allNotInWhitelist = false;
                    break;
                }
            }
            
            if (allNotInWhitelist) {
                // 所有记录的科目编码都不在白名单中，跳过预算校验，但仍需要更新 BudgetLedgerHead 的 status 为 APPROVED
                log.info("所有记录的科目编码都不在白名单中，跳过预算校验，但仍更新审批状态");
                existingHead.setStatus("APPROVED");
                existingHead.setVersion(String.valueOf(identifierGenerator.nextId(null)));
                if (requestTime != null) {
                    existingHead.setUpdateTime(requestTime);
                }
                budgetLedgerHeadMapper.updateById(existingHead);
                log.info("========== 场景二处理完成（不受控明细，跳过预算校验） ==========");
                return buildSuccessResponse(budgetRenewParams, "预算申请审批成功");
            }
        }

        existingHead.setStatus("APPROVED");
        existingHead.setVersion(String.valueOf(identifierGenerator.nextId(null)));
        if (requestTime != null) {
            existingHead.setUpdateTime(requestTime);
        }
        budgetLedgerHeadMapper.updateById(existingHead);

        log.info("========== 场景二处理完成 ==========");
        return buildSuccessResponse(budgetRenewParams, "预算申请审批成功");
    }

    /**
     * 场景三：REJECTED 或 CANCELLED
     */
    private BudgetRenewRespVo handleRejectedOrCancelled(String demandOrderNo, String documentStatus, BudgetRenewParams budgetRenewParams) {
        log.info("========== 场景三：{}，demandOrderNo={} ==========", documentStatus, demandOrderNo);
        
        // 查询 BUDGET_LEDGER_HEAD
        LambdaQueryWrapper<BudgetLedgerHead> headWrapper = new LambdaQueryWrapper<>();
        headWrapper.eq(BudgetLedgerHead::getBizCode, demandOrderNo)
                .eq(BudgetLedgerHead::getDeleted, Boolean.FALSE);
        BudgetLedgerHead existingHead = budgetLedgerHeadMapper.selectOne(headWrapper);
        
        if (existingHead == null) {
            throw new IllegalStateException("未找到对应的预算流水头，业务单号：" + demandOrderNo);
        }

        // 查询 BUDGET_LEDGER
        LambdaQueryWrapper<BudgetLedger> ledgerWrapper = new LambdaQueryWrapper<>();
        ledgerWrapper.eq(BudgetLedger::getBizCode, demandOrderNo)
                .eq(BudgetLedger::getBizType, DEFAULT_BIZ_TYPE)
                .eq(BudgetLedger::getDeleted, Boolean.FALSE);
        List<BudgetLedger> allLedgers = budgetLedgerMapper.selectList(ledgerWrapper);
        
        // 白名单校验：与预算调整一致——仅当所有明细均「科目不在白名单、无项目、无资产类型」时才跳过余额/额度回滚；否则走完整回滚
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
                    LambdaQueryWrapper<BudgetLedgerSelfR> deleteWrapper = new LambdaQueryWrapper<>();
                    deleteWrapper.in(BudgetLedgerSelfR::getRelatedId, needToCancelBudgetLedgerSet)
                            .eq(BudgetLedgerSelfR::getBizType, DEFAULT_BIZ_TYPE);
                    int deletedCount = budgetLedgerSelfRMapper.delete(deleteWrapper);
                    if (deletedCount > 0) {
                        log.info("========== 白名单外撤回：删除关联的预算流水关系 {} 条 ==========", deletedCount);
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
                log.info("========== 场景三处理完成（白名单外撤回，已归档删除流水与头） ==========");
                String msg;
                if ("REJECTED".equals(documentStatus)) {
                    msg = "预算申请驳回成功";
                } else if ("CANCELLED".equals(documentStatus)) {
                    msg = "预算申请撤销成功";
                } else if ("CLOSED".equals(documentStatus)) {
                    msg = "预算申请关闭成功";
                } else {
                    msg = "预算申请处理成功";
                }
                return buildSuccessResponse(budgetRenewParams, msg);
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
            
            log.info("========== 场景三处理完成（无流水） ==========");
            String msg;
            if ("REJECTED".equals(documentStatus)) {
                msg = "预算申请驳回成功";
            } else if ("CANCELLED".equals(documentStatus)) {
                msg = "预算申请撤销成功";
            } else if ("CLOSED".equals(documentStatus)) {
                msg = "预算申请关闭成功";
            } else {
                msg = "预算申请处理成功";
            }
            return buildSuccessResponse(budgetRenewParams, msg);
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
        
        // 检查是否有错误
        if (result.hasError()) {
            throw new IllegalStateException(result.getErrorMessage());
        }
        
        Map<String, BudgetQuota> needToRollBackBudgetQuotaMap = result.getQuotaMap();
        Map<String, BudgetBalance> needToRollBackBudgetBalanceMap = result.getBalanceMap();

        // 回滚逻辑
        List<BudgetQuotaHistory> needToAddBudgetQuotaHistory = new ArrayList<>();
        List<BudgetBalanceHistory> needToAddBudgetBalanceHistory = new ArrayList<>();

        // 回滚 BudgetBalance 和 BudgetQuota
        // 遍历所有 ledger，执行跨季度回滚
        for (BudgetLedger ledger : allLedgers) {
            // 项目非 NAN 且 isInternal=1 时跳过回滚
            if (!"NAN".equals(ledger.getMasterProjectCode()) && "1".equals(ledger.getIsInternal())) {
                log.info("========== handleRejectedOrCancelled - 内部项目跳过回滚: bizKey={}, masterProjectCode={}, isInternal={} ==========",
                        ledger.getBizCode() + "@" + ledger.getBizItemCode(), ledger.getMasterProjectCode(), ledger.getIsInternal());
                continue;
            }
            
            String bizKey = ledger.getBizCode() + "@" + ledger.getBizItemCode();
            
            // 判断是否不受控（需要跳过预算余额回滚，但仍删除数据）
            if (isUncontrolledLedger(ledger, ehrCdToOrgCdExtMap, erpAcctCdToAcctCdExtMap)) {
                log.info("========== handleRejectedOrCancelled - 不受控明细跳过预算余额回滚: bizKey={}, morgCode={}, budgetSubjectCode={}, masterProjectCode={} ==========",
                        bizKey, ledger.getMorgCode(), ledger.getBudgetSubjectCode(), ledger.getMasterProjectCode());
                continue;
            }

            // 回滚规则：先按 SELF_R 的 amountConsumedQ* 回滚关联流水（申请单无上游，无此步），再按本单 amountConsumedQ* 回滚资金池
            // 使用 helper 计算需要回滚的季度及金额（基于本单 amountConsumedQOne~Four，与提交时写入一致）
            BudgetQueryHelperService.RollbackQuartersResult rollbackResult = budgetQueryHelperService.calculateRollbackQuarters(ledger);
            List<String> quartersToRollback = rollbackResult.getQuartersToRollback();
            Map<String, BigDecimal> quarterRollbackAmountMap = rollbackResult.getQuarterRollbackAmountMap();
            
            if (quartersToRollback.isEmpty()) {
                log.info("========== handleRejectedOrCancelled - 无需回滚: bizKey={} ==========", bizKey);
                continue;
            }
            
            // quarterRollbackAmountMap 中已经包含了按 amountAvailable 分配好的各季度回滚金额
            // 直接使用这些值作为释放金额（不需要再按比例计算，因为 totalAmount 就是 amountAvailable）
            
            // 对每个需要回滚的季度进行处理
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
                    log.error("========== bizKeyQuarter={} 在 needToRollBackBudgetBalanceMap 中找不到对应的 balance ==========", bizKeyQuarter);
                    throw new IllegalStateException(
                        String.format("明细 [%s] 回滚时在季度 %s 未找到对应的预算余额。" +
                                     "请检查 BUDGET_POOL_DEM_R 表是否配置了该维度组合的预算。",
                                bizKey, rollbackQuarter)
                    );
                }
                
                Long poolId = balance.getPoolId();
                
                // 释放冻结金额
                BudgetBalanceHistory balanceHistory = new BudgetBalanceHistory();
                BeanUtils.copyProperties(balance, balanceHistory);
                balanceHistory.setId(identifierGenerator.nextId(balanceHistory).longValue());
                balanceHistory.setBalanceId(balance.getId());
                balanceHistory.setDeleted(Boolean.FALSE);
                
                BigDecimal amountFrozen = balance.getAmountFrozen() == null ? BigDecimal.ZERO : balance.getAmountFrozen();
                BigDecimal amountAvailable = balance.getAmountAvailable() == null ? BigDecimal.ZERO : balance.getAmountAvailable();
                
                log.info("========== handleRejectedOrCancelled - 释放冻结金额: poolId={}, bizItemCode={}, quarter={}, releaseAmount={}, 释放前 amountFrozen={}, amountAvailable={} ==========",
                        poolId, ledger.getBizItemCode(), rollbackQuarter, releaseAmount, amountFrozen, amountAvailable);
                
                // 执行释放
                if (amountFrozen.compareTo(releaseAmount) >= 0) {
                    balance.setAmountFrozen(amountFrozen.subtract(releaseAmount));
                    balance.setAmountFrozenVchanged(releaseAmount.negate());
                    balance.setAmountAvailable(amountAvailable.add(releaseAmount));
                    balance.setAmountAvailableVchanged(releaseAmount);
                } else {
                    balance.setAmountFrozen(BigDecimal.ZERO);
                    balance.setAmountFrozenVchanged(amountFrozen.negate());
                    balance.setAmountAvailable(amountAvailable.add(amountFrozen));
                    balance.setAmountAvailableVchanged(amountFrozen);
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
        if (!needToCancelBudgetLedgerSet.isEmpty()) {
            budgetLedgerMapper.deleteByIds(new ArrayList<>(needToCancelBudgetLedgerSet));
            
            // 删除与 needToCancelBudgetLedgerSet 相关联的预算流水关系
            // 通过 needToCancelBudgetLedgerSet 下 BudgetLedger 的 ID 作为 BUDGET_LEDGER_SELF_R 的 RELATED_ID 字段值对应所有关系都删除掉
            LambdaQueryWrapper<BudgetLedgerSelfR> deleteWrapper = new LambdaQueryWrapper<>();
            deleteWrapper.in(BudgetLedgerSelfR::getRelatedId, needToCancelBudgetLedgerSet)
                    .eq(BudgetLedgerSelfR::getBizType, DEFAULT_BIZ_TYPE);
            int deletedCount = budgetLedgerSelfRMapper.delete(deleteWrapper);
            if (deletedCount > 0) {
                log.info("========== 删除关联的预算流水关系: 删除了 {} 条关系记录 ==========", deletedCount);
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

        log.info("========== 场景三处理完成 ==========");
        String msg;
        if ("REJECTED".equals(documentStatus)) {
            msg = "预算申请驳回成功";
        } else if ("CANCELLED".equals(documentStatus)) {
            msg = "预算申请撤销成功";
        } else if ("CLOSED".equals(documentStatus)) {
            msg = "预算申请关闭成功";
        } else {
            msg = "预算申请处理成功";
        }
        return buildSuccessResponse(budgetRenewParams, msg);
    }

    /**
     * 构建成功响应
     */
    private BudgetRenewRespVo buildSuccessResponse(BudgetRenewParams params, String message) {
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
                .returnCode("A0001-BUDGET")
                .returnMsg(message)
                .returnStatus("S")
                .responseTime(currentTime)
                .build();

        BudgetRenewRespVo response = new BudgetRenewRespVo();
        response.setEsbInfo(esbInfo);
        return response;
    }

    /**
     * 构建错误响应
     */
    private BudgetRenewRespVo buildErrorResponse(BudgetRenewParams params, Exception e, boolean isBusinessException) {
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
                .returnCode("E0001-BUDGET")
                .returnMsg(errorMessage)
                .returnStatus(returnStatus)
                .responseTime(currentTime)
                .build();

        BudgetRenewRespVo response = new BudgetRenewRespVo();
        response.setEsbInfo(esbInfo);
        return response;
    }

    @Override
    protected List<BudgetLedger> queryExistingLedgers(List<com.jasolar.mis.module.system.controller.budget.vo.ExtDetailVo> extDetailsForQuery, String type) {
        // 只处理 APPLY 类型的查询
        if (!DEFAULT_BIZ_TYPE.equals(type)) {
            return Collections.emptyList();
        }
        
        // 将 ExtDetailVo 转换为 ApplyExtDetailVo
        List<ApplyExtDetailVo> applyExtDetailsForQuery = new ArrayList<>();
        for (com.jasolar.mis.module.system.controller.budget.vo.ExtDetailVo extDetail : extDetailsForQuery) {
            ApplyExtDetailVo applyExtDetail = new ApplyExtDetailVo();
            // 字段映射
            applyExtDetail.setDemandOrderNo(extDetail.getDocumentNo());
            applyExtDetail.setDemandDetailLineNo(extDetail.getDetailLineNo());
            applyExtDetail.setDemandYear(extDetail.getYear());
            applyExtDetail.setDemandMonth(extDetail.getMonth());
            applyExtDetail.setCompany(extDetail.getCompany());
            applyExtDetail.setDepartment(extDetail.getDepartment());
            applyExtDetail.setManagementOrg(extDetail.getManagementOrg());
            applyExtDetail.setBudgetSubjectCode(extDetail.getBudgetSubjectCode());
            applyExtDetail.setBudgetSubjectName(extDetail.getBudgetSubjectName());
            applyExtDetail.setMasterProjectCode(extDetail.getMasterProjectCode());
            applyExtDetail.setMasterProjectName(extDetail.getMasterProjectName());
            applyExtDetail.setErpAssetType(extDetail.getErpAssetType());
            applyExtDetail.setDemandAmount(extDetail.getAmount());
            applyExtDetail.setCurrency(extDetail.getCurrency());
            applyExtDetailsForQuery.add(applyExtDetail);
        }
        
        // 查询预算流水
        return budgetLedgerMapper.selectByExtDetails(applyExtDetailsForQuery);
    }

    @Override
    protected boolean isUncontrolledLedger(BudgetLedger ledger,
                                           Map<String, List<String>> ehrCdToOrgCdExtMap,
                                           Map<String, List<String>> erpAcctCdToAcctCdExtMap) {
        if (isProjectSkipBudgetLedger(ledger)) {
            log.info("预算申请命中科目000000且带项目，按不受控处理并跳过预算占用/回滚，bizKey={}@{}",
                    ledger.getBizCode(), ledger.getBizItemCode());
            return true;
        }
        return super.isUncontrolledLedger(ledger, ehrCdToOrgCdExtMap, erpAcctCdToAcctCdExtMap);
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

}

