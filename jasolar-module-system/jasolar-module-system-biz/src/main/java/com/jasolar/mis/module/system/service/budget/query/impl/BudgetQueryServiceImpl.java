package com.jasolar.mis.module.system.service.budget.query.impl;

import com.alibaba.excel.util.DateUtils;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jasolar.mis.module.system.controller.budget.vo.*;
import com.jasolar.mis.module.system.domain.budget.*;
import com.jasolar.mis.module.system.mapper.budget.*;
import com.jasolar.mis.module.system.service.budget.query.BudgetQueryService;
import com.jasolar.mis.module.system.service.budget.helper.BudgetQueryHelperService;
import com.jasolar.mis.module.system.service.ehr.EhrOrgManageRService;
import com.jasolar.mis.module.system.domain.ehr.EhrOrgManageHierarchyView;
import com.jasolar.mis.module.system.mapper.ehr.EhrOrgManageHierarchyViewMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import jakarta.annotation.Resource;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;
import java.util.Objects;

/**
 * Description: 预算查询服务实现类
 * Author : Auto Generated
 * Date : 2025-01-XX
 * Version : 1.0
 */
@Service
@Slf4j
public class BudgetQueryServiceImpl implements BudgetQueryService {

    private static final String APPLY_BIZ_TYPE = "APPLY";
    private static final String CONTRACT_BIZ_TYPE = "CONTRACT";
    private static final String CLAIM_BIZ_TYPE = "CLAIM";

    @Resource
    private BudgetLedgerHeadMapper budgetLedgerHeadMapper;
    
    @Resource
    private BudgetLedgerMapper budgetLedgerMapper;
    
    @Resource
    private BudgetPoolDemRMapper budgetPoolDemRMapper;
    
    @Resource
    private BudgetBalanceMapper budgetBalanceMapper;
    
    @Resource
    private BudgetLedgerSelfRMapper budgetLedgerSelfRMapper;
    
    @Resource
    private ObjectMapper objectMapper;

    @Resource
    private EhrOrgManageRService ehrOrgManageRService;
    
    @Resource
    private BudgetQueryHelperService budgetQueryHelperService;
    
    @Resource
    private EhrOrgManageHierarchyViewMapper ehrOrgManageHierarchyViewMapper;


    @Override
    public BudgetQueryRespVo query(BudgetQueryParams budgetQueryParams) {
        try {
            // 性能优化：减少日志输出，只在DEBUG级别输出
            if (log.isDebugEnabled()) {
                log.debug("开始处理预算查询，入参={}", budgetQueryParams);
            }
            
            QueryReqInfoParams queryReqInfo = budgetQueryParams.getQueryReqInfoParams();
            String demandOrderNo = queryReqInfo.getDemandOrderNo();
            String contractNo = queryReqInfo.getContractNo();
            String claimOrderNo = queryReqInfo.getClaimOrderNo();
            String adjustType = queryReqInfo.getAdjustType();
            List<QueryDetailDetailVo> details = queryReqInfo.getDetails();
            
            // 判断查询类型
            boolean hasDemand = StringUtils.isNotBlank(demandOrderNo);
            boolean hasContract = StringUtils.isNotBlank(contractNo);
            boolean hasClaim = StringUtils.isNotBlank(claimOrderNo);
            boolean hasAdjustType = StringUtils.isNotBlank(adjustType);
            boolean hasOrderNo = hasDemand || hasContract || hasClaim;
            
            Object queryResult;
            
            // 如果传了 adjustType，按调整类型查询
            if (hasAdjustType) {
                queryResult = queryByAdjustType(adjustType, details);
            } else if (hasDemand && !hasContract && !hasClaim) {
                // 只传了需求单号
                queryResult = queryByDemandOrderNo(demandOrderNo, details);
            } else if (hasContract && !hasDemand && !hasClaim) {
                // 只传了合同号
                queryResult = queryByContractNo(contractNo, details);
            } else if (hasClaim && !hasDemand && !hasContract) {
                // 只传了付款/报销单号
                queryResult = queryByClaimOrderNo(claimOrderNo, details);
            } else if (hasContract && hasDemand && !hasClaim) {
                // 传了合同号和需求单号
                queryResult = queryByContractAndDemand(contractNo, demandOrderNo, details);
            } else if (hasClaim && hasContract) {
                // 传了付款单号和合同号，不管传没传需求单号，都只查询合同单，忽略需求单
                // 如果付款单明细号以@NAN结尾，需要查询从q1到当前季度的balance并计算amountPayAvailable合计值，与合同可用余额比较取较小值
                queryResult = queryByContractWithClaim(contractNo, details);
            } else if (hasClaim && hasDemand && !hasContract) {
                // 传了付款单号和需求单号，直接查询需求单
                // 如果付款单明细号以@NAN结尾，需要查询从q1到当前季度的balance并计算amountPayAvailable合计值，与需求单可用余额比较取较小值
                queryResult = queryByDemandWithClaim(demandOrderNo, details);
            } else {
                // 其他组合情况：三个都传了，是不合理的组合，直接报错
                // 注意：如果三个都没传，应该已经被 Bean Validation 拦截了
                throw new IllegalArgumentException("不支持同时传入需求单号、合同号和付款单号三种单号");
            }
            
            // 组装响应
            BudgetQueryRespVo response = new BudgetQueryRespVo();
            response.setQueryResult(queryResult);
            
            // 组装 ESB 响应信息
            ESBRespInfoVo esbInfo = new ESBRespInfoVo();
            esbInfo.setReturnStatus("S");
            esbInfo.setReturnCode("A0001-BudgetQuery");
            esbInfo.setReturnMsg("查询成功");
            esbInfo.setResponseTime(java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS")));
            response.setEsbInfo(esbInfo);
            
            return response;
            
        } catch (Exception e) {
            log.error("预算查询失败", e);
            
            // 组装错误响应
            BudgetQueryRespVo errorResponse = new BudgetQueryRespVo();
            ESBRespInfoVo esbInfo = new ESBRespInfoVo();
            esbInfo.setReturnStatus("E");
            esbInfo.setReturnCode("E0001-BudgetQuery");
            esbInfo.setReturnMsg("查询失败: " + e.getMessage());
            esbInfo.setResponseTime(java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS")));
            errorResponse.setEsbInfo(esbInfo);
            
            return errorResponse;
        }
    }

    /**
     * 根据需求单号查询
     */
    private ApplyQueryResultVo queryByDemandOrderNo(String demandOrderNo, List<QueryDetailDetailVo> details) {
        // 查询 BudgetLedgerHead
        BudgetLedgerHead head = budgetLedgerHeadMapper.selectOne(
            new LambdaQueryWrapper<BudgetLedgerHead>()
                .eq(BudgetLedgerHead::getBizCode, demandOrderNo)
                .eq(BudgetLedgerHead::getBizType, APPLY_BIZ_TYPE)
                .eq(BudgetLedgerHead::getDeleted, Boolean.FALSE)
        );
        
        ApplyQueryResultVo result = new ApplyQueryResultVo();
        
        if (head != null) {
            // 有流水头，通过维度筛选 BudgetLedger
            List<BudgetLedger> ledgers = filterLedgersByDimensions(demandOrderNo, APPLY_BIZ_TYPE, details);
            
            if (CollectionUtils.isEmpty(ledgers)) {
                throw new IllegalArgumentException("传入维度有误，没有对应明细");
            }
            
            // 组装结果
            result.setDemandOrderNo(head.getBizCode());
            result.setDocumentName(head.getDocumentName());
            result.setDataSource(head.getDataSource());
            result.setDocumentStatus(head.getStatus());
            
            // 提取组织和科目编码，查询名称映射
            Set<String> managementOrgSet = ledgers.stream()
                .map(BudgetLedger::getMorgCode)
                .filter(StringUtils::isNotBlank)
                .collect(Collectors.toSet());
            Set<String> budgetSubjectCodeSet = ledgers.stream()
                .map(BudgetLedger::getBudgetSubjectCode)
                .filter(StringUtils::isNotBlank)
                .filter(code -> !"NAN-NAN".equals(code))
                .collect(Collectors.toSet());
            Map<String, String> ehrCdToEhrNmMap = CollectionUtils.isEmpty(managementOrgSet) 
                ? Collections.emptyMap() 
                : budgetQueryHelperService.queryEhrCdToOrgCdOneToOneMap(managementOrgSet).getEhrCdToEhrNmMap();
            Map<String, String> erpAcctCdToErpAcctNmMap = CollectionUtils.isEmpty(budgetSubjectCodeSet) 
                ? Collections.emptyMap() 
                : budgetQueryHelperService.queryErpAcctCdToAcctCdOneToOneMap(budgetSubjectCodeSet).getErpAcctCdToErpAcctNmMap();
            
            // 组装明细
            List<ApplyQueryRespDetailVo> detailVos = ledgers.stream()
                .map(ledger -> convertToApplyDetailVo(ledger, details, ehrCdToEhrNmMap, erpAcctCdToErpAcctNmMap))
                .collect(Collectors.toList());
            result.setDemandDetails(detailVos);
            
        } else {
            // 没有流水头，通过维度获取 BudgetPoolDemR 关系，然后获取 balance
            List<ApplyQueryRespDetailVo> detailVos = queryByDimensionsForApply(details);
            
            result.setDemandOrderNo(demandOrderNo);
            result.setDemandDetails(detailVos);
        }
        
        return result;
    }

    /**
     * 根据需求单号和付款单号查询（传了付款单号和需求单号的情况）
     * 如果付款单明细号以@NAN结尾，需要查询从q1到当前季度的balance并计算amountPayAvailable合计值，与需求单可用余额比较取较小值
     */
    private ApplyQueryResultVo queryByDemandWithClaim(String demandOrderNo, List<QueryDetailDetailVo> details) {
        // 查询 BudgetLedgerHead
        BudgetLedgerHead head = budgetLedgerHeadMapper.selectOne(
            new LambdaQueryWrapper<BudgetLedgerHead>()
                .eq(BudgetLedgerHead::getBizCode, demandOrderNo)
                .eq(BudgetLedgerHead::getBizType, APPLY_BIZ_TYPE)
                .eq(BudgetLedgerHead::getDeleted, Boolean.FALSE)
        );
        
        ApplyQueryResultVo result = new ApplyQueryResultVo();
        
        if (head != null) {
            // 有流水头，通过维度筛选 BudgetLedger
            List<BudgetLedger> ledgers = filterLedgersByDimensions(demandOrderNo, APPLY_BIZ_TYPE, details);
            
            if (CollectionUtils.isEmpty(ledgers)) {
                throw new IllegalArgumentException("传入维度有误，没有对应明细");
            }
            
            // 组装结果
            result.setDemandOrderNo(head.getBizCode());
            result.setDocumentName(head.getDocumentName());
            result.setDataSource(head.getDataSource());
            result.setDocumentStatus(head.getStatus());
            
            // 提取组织和科目编码，查询名称映射
            Set<String> managementOrgSet = ledgers.stream()
                .map(BudgetLedger::getMorgCode)
                .filter(StringUtils::isNotBlank)
                .collect(Collectors.toSet());
            Set<String> budgetSubjectCodeSet = ledgers.stream()
                .map(BudgetLedger::getBudgetSubjectCode)
                .filter(StringUtils::isNotBlank)
                .filter(code -> !"NAN-NAN".equals(code))
                .collect(Collectors.toSet());
            Map<String, String> ehrCdToEhrNmMap = CollectionUtils.isEmpty(managementOrgSet) 
                ? Collections.emptyMap() 
                : budgetQueryHelperService.queryEhrCdToOrgCdOneToOneMap(managementOrgSet).getEhrCdToEhrNmMap();
            Map<String, String> erpAcctCdToErpAcctNmMap = CollectionUtils.isEmpty(budgetSubjectCodeSet) 
                ? Collections.emptyMap() 
                : budgetQueryHelperService.queryErpAcctCdToAcctCdOneToOneMap(budgetSubjectCodeSet).getErpAcctCdToErpAcctNmMap();
            
            // 组装明细，对于以@NAN结尾的detailLineNo，需要计算amountPayAvailable并比较
            List<ApplyQueryRespDetailVo> detailVos = new ArrayList<>();
            for (BudgetLedger ledger : ledgers) {
                ApplyQueryRespDetailVo vo = convertToApplyDetailVo(ledger, details, ehrCdToEhrNmMap, erpAcctCdToErpAcctNmMap);
                
                // 查找对应的detail，检查detailLineNo是否以@NAN结尾
                QueryDetailDetailVo matchedDetail = details.stream()
                    .filter(detail -> matchDimensions(ledger, detail))
                    .findFirst()
                    .orElse(null);
                
                if (matchedDetail != null) {
                    String detailLineNo = matchedDetail.getDetailLineNo();
                    if (detailLineNo != null && detailLineNo.endsWith("@NAN")) {
                        // 如果以@NAN结尾，查询从q1到当前季度的balance并计算amountPayAvailable合计值
                        BigDecimal totalAmountPayAvailable = calculateTotalAmountPayAvailable(matchedDetail);
                        // 与需求单可用余额比较，取较小值
                        BigDecimal demandAmountAvailable = vo.getAmountAvailable() != null ? vo.getAmountAvailable() : BigDecimal.ZERO;
                        vo.setAmountAvailable(demandAmountAvailable.min(totalAmountPayAvailable));
                    }
                }
                
                detailVos.add(vo);
            }
            result.setDemandDetails(detailVos);
            
        } else {
            // 没有流水头，通过维度获取 BudgetPoolDemR 关系，然后获取 balance
            List<ApplyQueryRespDetailVo> detailVos = queryByDimensionsForApplyWithClaim(details);
            
            result.setDemandOrderNo(demandOrderNo);
            result.setDemandDetails(detailVos);
        }
        
        return result;
    }

    /**
     * 根据合同号查询
     */
    private ContractQueryResultVo queryByContractNo(String contractNo, List<QueryDetailDetailVo> details) {
        // 查询 BudgetLedgerHead
        BudgetLedgerHead head = budgetLedgerHeadMapper.selectOne(
            new LambdaQueryWrapper<BudgetLedgerHead>()
                .eq(BudgetLedgerHead::getBizCode, contractNo)
                .eq(BudgetLedgerHead::getBizType, CONTRACT_BIZ_TYPE)
                .eq(BudgetLedgerHead::getDeleted, Boolean.FALSE)
        );
        
        ContractQueryResultVo result = new ContractQueryResultVo();
        
        if (head != null) {
            // 有流水头，通过维度筛选 BudgetLedger
            List<BudgetLedger> ledgers = filterLedgersByDimensions(contractNo, CONTRACT_BIZ_TYPE, details);
            
            if (CollectionUtils.isEmpty(ledgers)) {
                throw new IllegalArgumentException("传入维度有误，没有对应明细");
            }
            
            // 组装结果
            result.setContractNo(head.getBizCode());
            result.setDocumentName(head.getDocumentName());
            result.setDataSource(head.getDataSource());
            result.setDocumentStatus(head.getStatus());
            
            // 提取组织和科目编码，查询名称映射
            Set<String> managementOrgSet = ledgers.stream()
                .map(BudgetLedger::getMorgCode)
                .filter(StringUtils::isNotBlank)
                .collect(Collectors.toSet());
            Set<String> budgetSubjectCodeSet = ledgers.stream()
                .map(BudgetLedger::getBudgetSubjectCode)
                .filter(StringUtils::isNotBlank)
                .filter(code -> !"NAN-NAN".equals(code))
                .collect(Collectors.toSet());
            Map<String, String> ehrCdToEhrNmMap = CollectionUtils.isEmpty(managementOrgSet) 
                ? Collections.emptyMap() 
                : budgetQueryHelperService.queryEhrCdToOrgCdOneToOneMap(managementOrgSet).getEhrCdToEhrNmMap();
            Map<String, String> erpAcctCdToErpAcctNmMap = CollectionUtils.isEmpty(budgetSubjectCodeSet) 
                ? Collections.emptyMap() 
                : budgetQueryHelperService.queryErpAcctCdToAcctCdOneToOneMap(budgetSubjectCodeSet).getErpAcctCdToErpAcctNmMap();
            
            // 组装明细
            List<ContractQueryRespDetailVo> detailVos = ledgers.stream()
                .map(ledger -> convertToContractDetailVo(ledger, details, ehrCdToEhrNmMap, erpAcctCdToErpAcctNmMap))
                .collect(Collectors.toList());
            result.setContractDetails(detailVos);
            
        } else {
            // 没有流水头，通过 BudgetLedgerSelfR 查关联的需求单
            List<BudgetLedger> relatedLedgers = findRelatedApplyLedgers(contractNo, details);
            
            if (!CollectionUtils.isEmpty(relatedLedgers)) {
                // 提取组织和科目编码，查询名称映射
                Set<String> relatedManagementOrgSet = relatedLedgers.stream()
                    .map(BudgetLedger::getMorgCode)
                    .filter(StringUtils::isNotBlank)
                    .collect(Collectors.toSet());
                Set<String> relatedBudgetSubjectCodeSet = relatedLedgers.stream()
                    .map(BudgetLedger::getBudgetSubjectCode)
                    .filter(StringUtils::isNotBlank)
                    .filter(code -> !"NAN-NAN".equals(code))
                    .collect(Collectors.toSet());
                Map<String, String> relatedEhrCdToEhrNmMap = CollectionUtils.isEmpty(relatedManagementOrgSet) 
                    ? Collections.emptyMap() 
                    : budgetQueryHelperService.queryEhrCdToOrgCdOneToOneMap(relatedManagementOrgSet).getEhrCdToEhrNmMap();
                Map<String, String> relatedErpAcctCdToErpAcctNmMap = CollectionUtils.isEmpty(relatedBudgetSubjectCodeSet) 
                    ? Collections.emptyMap() 
                    : budgetQueryHelperService.queryErpAcctCdToAcctCdOneToOneMap(relatedBudgetSubjectCodeSet).getErpAcctCdToErpAcctNmMap();
                
                // 有关联需求单，使用关联需求单的 BudgetLedger 的 amountAvailable
                List<ContractQueryRespDetailVo> detailVos = relatedLedgers.stream()
                    .map(ledger -> convertToContractDetailVo(ledger, details, relatedEhrCdToEhrNmMap, relatedErpAcctCdToErpAcctNmMap))
                    .collect(Collectors.toList());
                result.setContractNo(contractNo);
                result.setContractDetails(detailVos);
            } else {
                // 没有关联需求单，通过维度获取 BudgetPoolDemR 关系，然后获取 balance
                List<ContractQueryRespDetailVo> detailVos = queryByDimensionsForContract(details);
                result.setContractNo(contractNo);
                result.setContractDetails(detailVos);
            }
        }
        
        return result;
    }

    /**
     * 根据合同号和付款单号查询（传了付款单号和合同号的情况）
     * 如果付款单明细号以@NAN结尾，需要查询从q1到当前季度的balance并计算amountPayAvailable合计值，与合同可用余额比较取较小值
     */
    private ContractQueryResultVo queryByContractWithClaim(String contractNo, List<QueryDetailDetailVo> details) {
        // 查询 BudgetLedgerHead
        BudgetLedgerHead head = budgetLedgerHeadMapper.selectOne(
            new LambdaQueryWrapper<BudgetLedgerHead>()
                .eq(BudgetLedgerHead::getBizCode, contractNo)
                .eq(BudgetLedgerHead::getBizType, CONTRACT_BIZ_TYPE)
                .eq(BudgetLedgerHead::getDeleted, Boolean.FALSE)
        );
        
        ContractQueryResultVo result = new ContractQueryResultVo();
        
        if (head != null) {
            // 有流水头，通过维度筛选 BudgetLedger
            List<BudgetLedger> ledgers = filterLedgersByDimensions(contractNo, CONTRACT_BIZ_TYPE, details);
            
            if (CollectionUtils.isEmpty(ledgers)) {
                throw new IllegalArgumentException("传入维度有误，没有对应明细");
            }
            
            // 组装结果
            result.setContractNo(head.getBizCode());
            result.setDocumentName(head.getDocumentName());
            result.setDataSource(head.getDataSource());
            result.setDocumentStatus(head.getStatus());
            
            // 提取组织和科目编码，查询名称映射
            Set<String> managementOrgSet = ledgers.stream()
                .map(BudgetLedger::getMorgCode)
                .filter(StringUtils::isNotBlank)
                .collect(Collectors.toSet());
            Set<String> budgetSubjectCodeSet = ledgers.stream()
                .map(BudgetLedger::getBudgetSubjectCode)
                .filter(StringUtils::isNotBlank)
                .filter(code -> !"NAN-NAN".equals(code))
                .collect(Collectors.toSet());
            Map<String, String> ehrCdToEhrNmMap = CollectionUtils.isEmpty(managementOrgSet) 
                ? Collections.emptyMap() 
                : budgetQueryHelperService.queryEhrCdToOrgCdOneToOneMap(managementOrgSet).getEhrCdToEhrNmMap();
            Map<String, String> erpAcctCdToErpAcctNmMap = CollectionUtils.isEmpty(budgetSubjectCodeSet) 
                ? Collections.emptyMap() 
                : budgetQueryHelperService.queryErpAcctCdToAcctCdOneToOneMap(budgetSubjectCodeSet).getErpAcctCdToErpAcctNmMap();
            
            // 组装明细，对于以@NAN结尾的detailLineNo，需要计算amountPayAvailable并比较
            List<ContractQueryRespDetailVo> detailVos = new ArrayList<>();
            for (BudgetLedger ledger : ledgers) {
                ContractQueryRespDetailVo vo = convertToContractDetailVo(ledger, details, ehrCdToEhrNmMap, erpAcctCdToErpAcctNmMap);
                
                // 查找对应的detail，检查detailLineNo是否以@NAN结尾
                QueryDetailDetailVo matchedDetail = details.stream()
                    .filter(detail -> matchDimensions(ledger, detail))
                    .findFirst()
                    .orElse(null);
                
                if (matchedDetail != null) {
                    String detailLineNo = matchedDetail.getDetailLineNo();
                    if (detailLineNo != null && detailLineNo.endsWith("@NAN")) {
                        // 如果以@NAN结尾，查询从q1到当前季度的balance并计算amountPayAvailable合计值
                        BigDecimal totalAmountPayAvailable = calculateTotalAmountPayAvailable(matchedDetail);
                        // 与合同可用余额比较，取较小值
                        BigDecimal contractAmountAvailable = vo.getAmountAvailable() != null ? vo.getAmountAvailable() : BigDecimal.ZERO;
                        vo.setAmountAvailable(contractAmountAvailable.min(totalAmountPayAvailable));
                    }
                }
                
                detailVos.add(vo);
            }
            result.setContractDetails(detailVos);
            
        } else {
            // 没有流水头，通过 BudgetLedgerSelfR 查关联的需求单
            List<BudgetLedger> relatedLedgers = findRelatedApplyLedgers(contractNo, details);
            
            if (!CollectionUtils.isEmpty(relatedLedgers)) {
                // 提取组织和科目编码，查询名称映射
                Set<String> relatedManagementOrgSet = relatedLedgers.stream()
                    .map(BudgetLedger::getMorgCode)
                    .filter(StringUtils::isNotBlank)
                    .collect(Collectors.toSet());
                Set<String> relatedBudgetSubjectCodeSet = relatedLedgers.stream()
                    .map(BudgetLedger::getBudgetSubjectCode)
                    .filter(StringUtils::isNotBlank)
                    .filter(code -> !"NAN-NAN".equals(code))
                    .collect(Collectors.toSet());
                Map<String, String> relatedEhrCdToEhrNmMap = CollectionUtils.isEmpty(relatedManagementOrgSet) 
                    ? Collections.emptyMap() 
                    : budgetQueryHelperService.queryEhrCdToOrgCdOneToOneMap(relatedManagementOrgSet).getEhrCdToEhrNmMap();
                Map<String, String> relatedErpAcctCdToErpAcctNmMap = CollectionUtils.isEmpty(relatedBudgetSubjectCodeSet) 
                    ? Collections.emptyMap() 
                    : budgetQueryHelperService.queryErpAcctCdToAcctCdOneToOneMap(relatedBudgetSubjectCodeSet).getErpAcctCdToErpAcctNmMap();
                
                // 有关联需求单，使用关联需求单的 BudgetLedger 的 amountAvailable
                List<ContractQueryRespDetailVo> detailVos = new ArrayList<>();
                for (BudgetLedger ledger : relatedLedgers) {
                    ContractQueryRespDetailVo vo = convertToContractDetailVo(ledger, details, relatedEhrCdToEhrNmMap, relatedErpAcctCdToErpAcctNmMap);
                    
                    // 查找对应的detail，检查detailLineNo是否以@NAN结尾
                    QueryDetailDetailVo matchedDetail = details.stream()
                        .filter(detail -> matchDimensions(ledger, detail))
                        .findFirst()
                        .orElse(null);
                    
                    if (matchedDetail != null) {
                        String detailLineNo = matchedDetail.getDetailLineNo();
                        if (detailLineNo != null && detailLineNo.endsWith("@NAN")) {
                            // 如果以@NAN结尾，查询从q1到当前季度的balance并计算amountPayAvailable合计值
                            BigDecimal totalAmountPayAvailable = calculateTotalAmountPayAvailable(matchedDetail);
                            // 与合同可用余额比较，取较小值
                            BigDecimal contractAmountAvailable = vo.getAmountAvailable() != null ? vo.getAmountAvailable() : BigDecimal.ZERO;
                            vo.setAmountAvailable(contractAmountAvailable.min(totalAmountPayAvailable));
                        }
                    }
                    
                    detailVos.add(vo);
                }
                result.setContractNo(contractNo);
                result.setContractDetails(detailVos);
            } else {
                // 没有关联需求单，通过维度获取 BudgetPoolDemR 关系，然后获取 balance
                List<ContractQueryRespDetailVo> detailVos = queryByDimensionsForContractWithClaim(details);
                result.setContractNo(contractNo);
                result.setContractDetails(detailVos);
            }
        }
        
        return result;
    }

    /**
     * 计算从q1到当前季度的amountPayAvailable合计值
     */
    private BigDecimal calculateTotalAmountPayAvailable(QueryDetailDetailVo detail) {
        String currentQuarter = convertMonthToQuarter(detail.getQueryMonth());
        if (currentQuarter == null) {
            throw new IllegalArgumentException("无效的查询月份: " + detail.getQueryMonth());
        }
        
        // 获取从 q1 到当前季度的所有季度
        List<String> quarters = getQuartersUpTo(currentQuarter);
        String isInternal = calculateIsInternal(detail);
        
        // 映射 erpAssetType：如果以 "1" 或 "M" 开头，需要通过映射表映射
        String originalErpAssetType = detail.getErpAssetType();
        String masterProjectCode = detail.getMasterProjectCode();
        
        // 批量提取需要映射的 erpAssetType（单个明细的情况）
        Set<String> erpAssetTypeSet = new HashSet<>();
        boolean isNoProject = "NAN".equals(masterProjectCode) || StringUtils.isBlank(masterProjectCode);
        if (isNoProject && StringUtils.isNotBlank(originalErpAssetType) && !"NAN".equals(originalErpAssetType)
                && (originalErpAssetType.startsWith("1") || originalErpAssetType.startsWith("M"))) {
            erpAssetTypeSet.add(originalErpAssetType);
        }
        
        // 批量查询 VIEW_BUDGET_MEMBER_NAME_CODE 视图，获取 MEMBER_CD2 对应的 MEMBER_CD（一一对应关系）
        Map<String, String> erpAssetTypeToMemberCdMap = budgetQueryHelperService.queryErpAssetTypeToMemberCdMap(erpAssetTypeSet);
        
        // 使用新的映射方法
        BudgetQueryHelperService.MapErpAssetTypeResult erpAssetTypeResult = budgetQueryHelperService.mapErpAssetTypeForQuery(
                originalErpAssetType, masterProjectCode, erpAssetTypeToMemberCdMap,
                "查询 BudgetPoolDemR 时，明细 [" + detail.getDetailLineNo() + "]");
        if (erpAssetTypeResult.hasError()) {
            throw new IllegalArgumentException(erpAssetTypeResult.getErrorMessage());
        }
        String mappedErpAssetType = erpAssetTypeResult.getMappedValue();
        
        // 构建维度参数
        List<BudgetPoolDemRMapper.DimensionParam> dimensionParams = new ArrayList<>();
        for (String quarter : quarters) {
            dimensionParams.add(new BudgetPoolDemRMapper.DimensionParam(
                detail.getQueryYear(),
                quarter,
                isInternal,
                detail.getManagementOrg(),
                detail.getBudgetSubjectCode(),
                detail.getMasterProjectCode(),
                mappedErpAssetType
            ));
        }
        
        // 查询 BudgetPoolDemR（分批查询，避免IN子句参数过多导致超时）
        List<BudgetPoolDemR> poolDemRs = budgetQueryHelperService.batchSelectByDimensionsWithYearAndQuarter(dimensionParams, 100);
        
        if (CollectionUtils.isEmpty(poolDemRs)) {
            return BigDecimal.ZERO;
        }
        
        // 获取 poolId 列表
        Set<Long> poolIds = poolDemRs.stream()
            .map(BudgetPoolDemR::getId)
            .collect(Collectors.toSet());
        
        // 查询 BudgetBalance（使用分批查询方法，避免IN子句超过1000个限制）
        List<BudgetBalance> balances = budgetQueryHelperService.loadBudgetBalancesByPoolIds(new ArrayList<>(poolIds));
        
        // 建立维度 -> poolId 的映射
        Map<String, Long> dimensionToPoolIdMap = new HashMap<>();
        for (BudgetPoolDemR poolDemR : poolDemRs) {
            String poolIsInternal = "NAN".equals(poolDemR.getMasterProjectCode()) ? "1" : poolDemR.getIsInternal();
            String dimensionKey = poolDemR.getYear() + "@" + poolDemR.getQuarter() + "@" + poolIsInternal + "@" +
                poolDemR.getMorgCode() + "@" + poolDemR.getBudgetSubjectCode() + "@" +
                poolDemR.getMasterProjectCode() + "@" + poolDemR.getErpAssetType();
            dimensionToPoolIdMap.put(dimensionKey, poolDemR.getId());
        }
        
        // 建立 poolId -> BudgetBalance 的映射
        Map<Long, BudgetBalance> balanceMap = balances.stream()
            .collect(Collectors.toMap(BudgetBalance::getPoolId, b -> b, (b1, b2) -> b1));
        
        // 累加从 q1 到当前季度的所有 balance 的 amountPayAvailable
        BigDecimal totalAmountPayAvailable = BigDecimal.ZERO;
        for (String quarter : quarters) {
            String dimensionKey = detail.getQueryYear() + "@" + quarter + "@" + isInternal + "@" +
                detail.getManagementOrg() + "@" + detail.getBudgetSubjectCode() + "@" +
                detail.getMasterProjectCode() + "@" + detail.getErpAssetType();
            
            Long poolId = dimensionToPoolIdMap.get(dimensionKey);
            if (poolId != null) {
                BudgetBalance balance = balanceMap.get(poolId);
                if (balance != null && balance.getAmountPayAvailable() != null) {
                    totalAmountPayAvailable = totalAmountPayAvailable.add(balance.getAmountPayAvailable());
                }
            }
        }
        
        return totalAmountPayAvailable;
    }

    /**
     * 通过维度查询 BudgetBalance（合同+付款单场景）
     * 对于以@NAN结尾的detailLineNo，需要计算amountPayAvailable并与合同可用余额比较取较小值
     */
    private List<ContractQueryRespDetailVo> queryByDimensionsForContractWithClaim(List<QueryDetailDetailVo> details) {
        // 先调用原有的queryByDimensionsForContract方法获取合同可用余额
        List<ContractQueryRespDetailVo> contractDetailVos = queryByDimensionsForContract(details);
        
        // 建立detail到vo的映射（通过detailLineNo的前缀部分匹配，因为vo不包含季度信息）
        Map<String, ContractQueryRespDetailVo> detailKeyToVoMap = new HashMap<>();
        for (ContractQueryRespDetailVo vo : contractDetailVos) {
            // 构建key：year@isInternal@morgCode@budgetSubjectCode@masterProjectCode@erpAssetType
            String isInternal = vo.getIsInternal() != null ? vo.getIsInternal() : "0";
            String key = vo.getContractYear() + "@" + isInternal + "@" +
                vo.getManagementOrg() + "@" + vo.getBudgetSubjectCode() + "@" +
                vo.getMasterProjectCode() + "@" + vo.getErpAssetType();
            detailKeyToVoMap.put(key, vo);
        }
        
        // 对于以@NAN结尾的detailLineNo，计算amountPayAvailable并比较
        for (QueryDetailDetailVo detail : details) {
            String detailLineNo = detail.getDetailLineNo();
            if (detailLineNo != null && detailLineNo.endsWith("@NAN")) {
                // 构建key来匹配vo
                String isInternal = calculateIsInternal(detail);
                String key = detail.getQueryYear() + "@" + isInternal + "@" +
                    detail.getManagementOrg() + "@" + detail.getBudgetSubjectCode() + "@" +
                    detail.getMasterProjectCode() + "@" + detail.getErpAssetType();
                
                ContractQueryRespDetailVo vo = detailKeyToVoMap.get(key);
                if (vo != null) {
                    // 如果以@NAN结尾，查询从q1到当前季度的balance并计算amountPayAvailable合计值
                    BigDecimal totalAmountPayAvailable = calculateTotalAmountPayAvailable(detail);
                    // 与合同可用余额比较，取较小值
                    BigDecimal contractAmountAvailable = vo.getAmountAvailable() != null ? vo.getAmountAvailable() : BigDecimal.ZERO;
                    vo.setAmountAvailable(contractAmountAvailable.min(totalAmountPayAvailable));
                }
            }
        }
        
        return contractDetailVos;
    }

    /**
     * 根据付款/报销单号查询
     */
    private ClaimQueryResultVo queryByClaimOrderNo(String claimOrderNo, List<QueryDetailDetailVo> details) {
        // 查询 BudgetLedgerHead
        BudgetLedgerHead head = budgetLedgerHeadMapper.selectOne(
            new LambdaQueryWrapper<BudgetLedgerHead>()
                .eq(BudgetLedgerHead::getBizCode, claimOrderNo)
                .eq(BudgetLedgerHead::getBizType, CLAIM_BIZ_TYPE)
                .eq(BudgetLedgerHead::getDeleted, Boolean.FALSE)
        );
        
        ClaimQueryResultVo result = new ClaimQueryResultVo();
        
        if (head != null) {
            // 有流水头，通过维度筛选 BudgetLedger
            List<BudgetLedger> ledgers = filterLedgersByDimensions(claimOrderNo, CLAIM_BIZ_TYPE, details);
            
            if (CollectionUtils.isEmpty(ledgers)) {
                throw new IllegalArgumentException("传入维度有误，没有对应明细");
            }
            
            // 组装结果
            result.setClaimOrderNo(head.getBizCode());
            result.setDocumentName(head.getDocumentName());
            result.setDataSource(head.getDataSource());
            result.setDocumentStatus(head.getStatus());
            
            // 提取组织和科目编码，查询名称映射
            Set<String> managementOrgSet = ledgers.stream()
                .map(BudgetLedger::getMorgCode)
                .filter(StringUtils::isNotBlank)
                .collect(Collectors.toSet());
            Set<String> budgetSubjectCodeSet = ledgers.stream()
                .map(BudgetLedger::getBudgetSubjectCode)
                .filter(StringUtils::isNotBlank)
                .filter(code -> !"NAN-NAN".equals(code))
                .collect(Collectors.toSet());
            Map<String, String> ehrCdToEhrNmMap = CollectionUtils.isEmpty(managementOrgSet) 
                ? Collections.emptyMap() 
                : budgetQueryHelperService.queryEhrCdToOrgCdOneToOneMap(managementOrgSet).getEhrCdToEhrNmMap();
            Map<String, String> erpAcctCdToErpAcctNmMap = CollectionUtils.isEmpty(budgetSubjectCodeSet) 
                ? Collections.emptyMap() 
                : budgetQueryHelperService.queryErpAcctCdToAcctCdOneToOneMap(budgetSubjectCodeSet).getErpAcctCdToErpAcctNmMap();
            
            // 组装明细
            List<ClaimQueryRespDetailVo> detailVos = ledgers.stream()
                .map(ledger -> convertToClaimDetailVo(ledger, details, ehrCdToEhrNmMap, erpAcctCdToErpAcctNmMap))
                .collect(Collectors.toList());
            result.setClaimDetails(detailVos);
            
        } else {
            // 没有流水头，通过 BudgetLedgerSelfR 查关联的需求单
            List<BudgetLedger> relatedLedgers = findRelatedApplyLedgers(claimOrderNo, details);
            
            if (!CollectionUtils.isEmpty(relatedLedgers)) {
                // 提取组织和科目编码，查询名称映射
                Set<String> relatedManagementOrgSet = relatedLedgers.stream()
                    .map(BudgetLedger::getMorgCode)
                    .filter(StringUtils::isNotBlank)
                    .collect(Collectors.toSet());
                Set<String> relatedBudgetSubjectCodeSet = relatedLedgers.stream()
                    .map(BudgetLedger::getBudgetSubjectCode)
                    .filter(StringUtils::isNotBlank)
                    .filter(code -> !"NAN-NAN".equals(code))
                    .collect(Collectors.toSet());
                Map<String, String> relatedEhrCdToEhrNmMap = CollectionUtils.isEmpty(relatedManagementOrgSet) 
                    ? Collections.emptyMap() 
                    : budgetQueryHelperService.queryEhrCdToOrgCdOneToOneMap(relatedManagementOrgSet).getEhrCdToEhrNmMap();
                Map<String, String> relatedErpAcctCdToErpAcctNmMap = CollectionUtils.isEmpty(relatedBudgetSubjectCodeSet) 
                    ? Collections.emptyMap() 
                    : budgetQueryHelperService.queryErpAcctCdToAcctCdOneToOneMap(relatedBudgetSubjectCodeSet).getErpAcctCdToErpAcctNmMap();
                
                // 有关联需求单，使用关联需求单的 BudgetLedger 的 amountAvailable
                List<ClaimQueryRespDetailVo> detailVos = relatedLedgers.stream()
                    .map(ledger -> convertToClaimDetailVo(ledger, details, relatedEhrCdToEhrNmMap, relatedErpAcctCdToErpAcctNmMap))
                    .collect(Collectors.toList());
                result.setClaimOrderNo(claimOrderNo);
                result.setClaimDetails(detailVos);
            } else {
                // 没有关联需求单，通过维度获取 BudgetPoolDemR 关系，然后获取 balance
                List<ClaimQueryRespDetailVo> detailVos = queryByDimensionsForClaim(details);
                result.setClaimOrderNo(claimOrderNo);
                result.setClaimDetails(detailVos);
            }
        }
        
        return result;
    }

    /**
     * 根据合同号和需求单号查询
     */
    private ContractQueryResultVo queryByContractAndDemand(String contractNo, String demandOrderNo, List<QueryDetailDetailVo> details) {
        // 先校验需求单是否存在
        BudgetLedgerHead demandHead = budgetLedgerHeadMapper.selectOne(
            new LambdaQueryWrapper<BudgetLedgerHead>()
                .eq(BudgetLedgerHead::getBizCode, demandOrderNo)
                .eq(BudgetLedgerHead::getBizType, APPLY_BIZ_TYPE)
                .eq(BudgetLedgerHead::getDeleted, Boolean.FALSE)
        );
        
        if (demandHead == null) {
            throw new IllegalArgumentException("需求单不存在: " + demandOrderNo);
        }
        
        // 查询合同流水头
        BudgetLedgerHead contractHead = budgetLedgerHeadMapper.selectOne(
            new LambdaQueryWrapper<BudgetLedgerHead>()
                .eq(BudgetLedgerHead::getBizCode, contractNo)
                .eq(BudgetLedgerHead::getBizType, CONTRACT_BIZ_TYPE)
                .eq(BudgetLedgerHead::getDeleted, Boolean.FALSE)
        );
        
        ContractQueryResultVo result = new ContractQueryResultVo();
        
        if (contractHead != null) {
            // 合同存在，查询合同流水的对应维度明细
            List<BudgetLedger> ledgers = filterLedgersByDimensions(contractNo, CONTRACT_BIZ_TYPE, details);
            
            if (CollectionUtils.isEmpty(ledgers)) {
                throw new IllegalArgumentException("对应维度明细不存在");
            }
            
            // amountAvailable 对应合同流水的 BudgetLedger 的 amountAvailable
            result.setContractNo(contractNo);
            result.setDocumentName(contractHead.getDocumentName());
            result.setDataSource(contractHead.getDataSource());
            result.setDocumentStatus(contractHead.getStatus());
            
            // 提取组织和科目编码，查询名称映射
            Set<String> managementOrgSet = ledgers.stream()
                .map(BudgetLedger::getMorgCode)
                .filter(StringUtils::isNotBlank)
                .collect(Collectors.toSet());
            Set<String> budgetSubjectCodeSet = ledgers.stream()
                .map(BudgetLedger::getBudgetSubjectCode)
                .filter(StringUtils::isNotBlank)
                .filter(code -> !"NAN-NAN".equals(code))
                .collect(Collectors.toSet());
            Map<String, String> ehrCdToEhrNmMap = CollectionUtils.isEmpty(managementOrgSet) 
                ? Collections.emptyMap() 
                : budgetQueryHelperService.queryEhrCdToOrgCdOneToOneMap(managementOrgSet).getEhrCdToEhrNmMap();
            Map<String, String> erpAcctCdToErpAcctNmMap = CollectionUtils.isEmpty(budgetSubjectCodeSet) 
                ? Collections.emptyMap() 
                : budgetQueryHelperService.queryErpAcctCdToAcctCdOneToOneMap(budgetSubjectCodeSet).getErpAcctCdToErpAcctNmMap();
            
            List<ContractQueryRespDetailVo> detailVos = ledgers.stream()
                .map(ledger -> convertToContractDetailVo(ledger, details, ehrCdToEhrNmMap, erpAcctCdToErpAcctNmMap))
                .collect(Collectors.toList());
            result.setContractDetails(detailVos);
            
        } else {
            // 合同不存在，检查需求单
            List<BudgetLedger> ledgers = filterLedgersByDimensions(demandOrderNo, APPLY_BIZ_TYPE, details);
            
            if (CollectionUtils.isEmpty(ledgers)) {
                throw new IllegalArgumentException("需求单对应维度明细不存在");
            }
            
            // 提取组织和科目编码，查询名称映射
            Set<String> demandManagementOrgSet = ledgers.stream()
                .map(BudgetLedger::getMorgCode)
                .filter(StringUtils::isNotBlank)
                .collect(Collectors.toSet());
            Set<String> demandBudgetSubjectCodeSet = ledgers.stream()
                .map(BudgetLedger::getBudgetSubjectCode)
                .filter(StringUtils::isNotBlank)
                .filter(code -> !"NAN-NAN".equals(code))
                .collect(Collectors.toSet());
            Map<String, String> demandEhrCdToEhrNmMap = CollectionUtils.isEmpty(demandManagementOrgSet) 
                ? Collections.emptyMap() 
                : budgetQueryHelperService.queryEhrCdToOrgCdOneToOneMap(demandManagementOrgSet).getEhrCdToEhrNmMap();
            Map<String, String> demandErpAcctCdToErpAcctNmMap = CollectionUtils.isEmpty(demandBudgetSubjectCodeSet) 
                ? Collections.emptyMap() 
                : budgetQueryHelperService.queryErpAcctCdToAcctCdOneToOneMap(demandBudgetSubjectCodeSet).getErpAcctCdToErpAcctNmMap();
            
            // amountAvailable 对应需求单的 BudgetLedger 的 amountAvailable
            result.setContractNo(contractNo);
            List<ContractQueryRespDetailVo> detailVos = ledgers.stream()
                .map(ledger -> convertToContractDetailVo(ledger, details, demandEhrCdToEhrNmMap, demandErpAcctCdToErpAcctNmMap))
                .collect(Collectors.toList());
            result.setContractDetails(detailVos);
        }
        
        return result;
    }

    /**
     * 通过维度筛选 BudgetLedger
     */
    private List<BudgetLedger> filterLedgersByDimensions(String bizCode, String bizType, List<QueryDetailDetailVo> details) {
        // 先查询该业务单号的所有流水
        List<BudgetLedger> allLedgers = budgetLedgerMapper.selectList(
            new LambdaQueryWrapper<BudgetLedger>()
                .eq(BudgetLedger::getBizCode, bizCode)
                .eq(BudgetLedger::getBizType, bizType)
                .eq(BudgetLedger::getDeleted, Boolean.FALSE)
        );
        
        if (CollectionUtils.isEmpty(allLedgers) || CollectionUtils.isEmpty(details)) {
            return allLedgers;
        }
        
        // 根据维度筛选
        List<BudgetLedger> filteredLedgers = new ArrayList<>();
        for (QueryDetailDetailVo detail : details) {
            for (BudgetLedger ledger : allLedgers) {
                if (matchDimensions(ledger, detail)) {
                    filteredLedgers.add(ledger);
                }
            }
        }
        
        return filteredLedgers;
    }

    /**
     * 判断 BudgetLedger 是否匹配维度
     * 实际上就是比较 detail 组合的维度 key 值（detailLineNo）是否匹配 ledger 的 bizItemCode
     * bizItemCode 不可能为空，为空是数据有误
     */
    private boolean matchDimensions(BudgetLedger ledger, QueryDetailDetailVo detail) {
        // 直接比较维度 key：detailLineNo 和 bizItemCode
        // bizItemCode 格式：isInternal@morgCode@budgetSubjectCode@masterProjectCode@erpAssetType（不再包含year@quarter）
        // detailLineNo 格式：isInternal@managementOrg@budgetSubjectCode@masterProjectCode@erpAssetType（不再包含year@quarter）
        return detail.getDetailLineNo().equals(ledger.getBizItemCode());
    }

    /**
     * 查找关联的需求单流水
     */
    private List<BudgetLedger> findRelatedApplyLedgers(String bizCode, List<QueryDetailDetailVo> details) {
        // 查询合同或付款单的流水
        List<BudgetLedger> contractLedgers = budgetLedgerMapper.selectList(
            new LambdaQueryWrapper<BudgetLedger>()
                .eq(BudgetLedger::getBizCode, bizCode)
                .eq(BudgetLedger::getDeleted, Boolean.FALSE)
        );
        
        if (CollectionUtils.isEmpty(contractLedgers)) {
            return Collections.emptyList();
        }
        
        // 获取合同/付款单流水的ID
        Set<Long> contractLedgerIds = contractLedgers.stream()
            .map(BudgetLedger::getId)
            .collect(Collectors.toSet());
        
        // 查询 BudgetLedgerSelfR，其中 id 是合同/付款单流水的ID，relatedId 是关联的需求单流水ID
        List<BudgetLedgerSelfR> selfRs = budgetLedgerSelfRMapper.selectByIdsAndBizType(contractLedgerIds, APPLY_BIZ_TYPE);
        
        if (CollectionUtils.isEmpty(selfRs)) {
            return Collections.emptyList();
        }
        
        // 获取关联的需求单流水ID（relatedId）
        Set<Long> applyLedgerIds = selfRs.stream()
            .map(BudgetLedgerSelfR::getRelatedId)
            .filter(Objects::nonNull)
            .collect(Collectors.toSet());
        
        if (CollectionUtils.isEmpty(applyLedgerIds)) {
            return Collections.emptyList();
        }
        
        // 查询关联的需求单流水
        List<BudgetLedger> applyLedgers = budgetLedgerMapper.selectList(
            new LambdaQueryWrapper<BudgetLedger>()
                .in(BudgetLedger::getId, applyLedgerIds)
                .eq(BudgetLedger::getDeleted, Boolean.FALSE)
        );
        
        // 根据维度筛选
        if (CollectionUtils.isEmpty(details)) {
            return applyLedgers;
        }
        
        List<BudgetLedger> filteredLedgers = new ArrayList<>();
        for (QueryDetailDetailVo detail : details) {
            for (BudgetLedger ledger : applyLedgers) {
                if (matchDimensions(ledger, detail)) {
                    filteredLedgers.add(ledger);
                }
            }
        }
        
        return filteredLedgers;
    }

    /**
     * 通过维度查询 BudgetBalance（需求单）
     * 获取本年度当前季度往前累积的可用余额
     */
    private List<ApplyQueryRespDetailVo> queryByDimensionsForApply(List<QueryDetailDetailVo> details) {
        // 批量提取 managementOrg 字段（对应 EHR_CD）
        Set<String> managementOrgSet = details.stream()
                .map(QueryDetailDetailVo::getManagementOrg)
                .filter(StringUtils::isNotBlank)
                .collect(Collectors.toSet());
        
        // 批量查询 EHR_ORG_MANAGE_R 表，获取 EHR_CD 对应的 ORG_CD（一一对应关系）
        // Map<EHR_CD, ORG_CD>
        BudgetQueryHelperService.EhrCdToOrgCdMapResult ehrCdToOrgCdMapResult = budgetQueryHelperService.queryEhrCdToOrgCdOneToOneMap(managementOrgSet);
        Map<String, String> ehrCdToOrgCdMap = ehrCdToOrgCdMapResult.getEhrCdToOrgCdMap();
        Map<String, String> ehrCdToEhrNmMap = ehrCdToOrgCdMapResult.getEhrCdToEhrNmMap();
        
        // 批量查询 EHR_ORG_MANAGE_EXT_R 表，获取 EHR_CD 对应的所有 ORG_CD（一对多关系）
        // Map<EHR_CD, List<ORG_CD>>
        Map<String, List<String>> ehrCdToOrgCdExtMap = budgetQueryHelperService.queryEhrCdToOrgCdMap(managementOrgSet);
        
        // 批量提取 budgetSubjectCode 字段
        Set<String> budgetSubjectCodeSet = details.stream()
                .map(QueryDetailDetailVo::getBudgetSubjectCode)
                .filter(StringUtils::isNotBlank)
                .filter(code -> !"NAN-NAN".equals(code))
                .collect(Collectors.toSet());
        
        // 批量查询 SUBJECT_INFO 表，获取 ERP_ACCT_CD 对应的 ACCT_CD（一一对应关系）
        // Map<ERP_ACCT_CD, ACCT_CD>
        BudgetQueryHelperService.ErpAcctCdToAcctCdMapResult erpAcctCdToAcctCdMapResult = budgetQueryHelperService.queryErpAcctCdToAcctCdOneToOneMap(budgetSubjectCodeSet);
        Map<String, String> erpAcctCdToAcctCdMap = erpAcctCdToAcctCdMapResult.getErpAcctCdToAcctCdMap();
        Map<String, String> erpAcctCdToErpAcctNmMap = erpAcctCdToAcctCdMapResult.getErpAcctCdToErpAcctNmMap();
        
        // 批量查询 SUBJECT_EXT_INFO 表，获取 ERP_ACCT_CD 对应的所有 ACCT_CD（一对多关系）
        // Map<ERP_ACCT_CD, List<ACCT_CD>>
        Map<String, List<String>> erpAcctCdToAcctCdExtMap = budgetQueryHelperService.queryErpAcctCdToAcctCdMap(budgetSubjectCodeSet);

        
        // 检查 erpAcctCdToAcctCdExtMap 中是否有任何 value（List）包含 "NAN-NAN"
        // 如果包含，说明没有映射到且是白名单，原样返回入参
        if (erpAcctCdToAcctCdExtMap != null && !erpAcctCdToAcctCdExtMap.isEmpty()) {
            for (Map.Entry<String, List<String>> entry : erpAcctCdToAcctCdExtMap.entrySet()) {
                List<String> acctCdList = entry.getValue();
                if (acctCdList != null && acctCdList.contains("NAN-NAN")) {
                    log.info("检测到 erpAcctCdToAcctCdExtMap 中包含 NAN-NAN，说明是白名单且没有映射，原样返回入参");
                    // 将入参的 details 转换为 ApplyQueryRespDetailVo
                    List<ApplyQueryRespDetailVo> result = new ArrayList<>();
                    for (QueryDetailDetailVo detail : details) {
                        ApplyQueryRespDetailVo vo = new ApplyQueryRespDetailVo();
                        vo.setDemandYear(detail.getQueryYear());
                        vo.setDemandMonth(detail.getQueryMonth());
                        vo.setCompany(detail.getCompany());
                        vo.setDepartment(detail.getDepartment());
                        vo.setManagementOrg(detail.getManagementOrg());
                        vo.setBudgetSubjectCode(detail.getBudgetSubjectCode());
                        vo.setMasterProjectCode(detail.getMasterProjectCode());
                        vo.setErpAssetType(detail.getErpAssetType());
                        vo.setIsInternal(detail.getIsInternal());
                        vo.setCurrency(detail.getCurrency());
                        vo.setAmountAvailable(BigDecimal.ZERO);
                        if (detail.getMetadata() != null && !detail.getMetadata().isEmpty()) {
                            vo.setMetadata(new HashMap<>(detail.getMetadata()));
                        } else {
                            vo.setMetadata(new HashMap<>());
                        }
                        result.add(vo);
                    }
                    return result;
                }
            }
        }
        
        // 批量提取 masterProjectCode 字段
        Set<String> masterProjectCodeSet = details.stream()
                .map(QueryDetailDetailVo::getMasterProjectCode)
                .filter(StringUtils::isNotBlank)
                .filter(code -> !"NAN".equals(code))
                .collect(Collectors.toSet());
        
        // 批量查询 PROJECT_CONTROL_EXT_R 表，获取 PRJ_CD 对应的所有 RELATED_PRJ_CD（一对多关系）
        // Map<PRJ_CD, List<RELATED_PRJ_CD>>
        Map<String, List<String>> prjCdToRelatedPrjCdExtMap = budgetQueryHelperService.queryPrjCdToRelatedPrjCdMap(masterProjectCodeSet);
        
        // 为每个 detail 构建临时的 BudgetLedger 对象，用于调用 queryQuotaAndBalanceByAllQuartersAllDem
        Map<String, BudgetLedger> tempLedgerMap = new HashMap<>();
        Map<String, QueryDetailDetailVo> bizKeyToDetailMap = new HashMap<>();
        Map<String, List<String>> detailKeyToQuartersMap = new HashMap<>();
        
        for (QueryDetailDetailVo detail : details) {
            String currentQuarter = convertMonthToQuarter(detail.getQueryMonth());
            if (currentQuarter == null) {
                throw new IllegalArgumentException("无效的查询月份: " + detail.getQueryMonth());
            }
            
            // 获取从 q1 到当前季度的所有季度
            List<String> quarters = getQuartersUpTo(currentQuarter);
            String isInternal = calculateIsInternal(detail);
            
            // 构建 bizItemCode，参考 ApplyDetailDetalVo.getDemandDetailLineNo() 的格式
            // 格式: isInternal@managementOrg@budgetSubjectCode@masterProjectCode@erpAssetType
            String bizItemCode = isInternal + "@" + 
                detail.getManagementOrg() + "@" + 
                detail.getBudgetSubjectCode() + "@" + 
                detail.getMasterProjectCode() + "@" + 
                detail.getErpAssetType();
            
            // 构建唯一的 bizCode
            String bizCode = "QUERY";
            
            // 构建 bizKey（格式：bizCode + "@" + bizItemCode）
            String bizKey = bizCode + "@" + bizItemCode;
            
            // 创建临时的 BudgetLedger 对象
            // 注意：morgCode 和 budgetSubjectCode 存储的是原始值（EHR_CD 和 ERP_ACCT_CD），
            // queryQuotaAndBalanceByAllQuartersAllDem 方法内部会进行映射转换
            BudgetLedger tempLedger = BudgetLedger.builder()
                    .bizType(APPLY_BIZ_TYPE)
                    .bizCode(bizCode)
                    .bizItemCode(bizItemCode)
                    .year(detail.getQueryYear())
                    .month(detail.getQueryMonth())
                    .morgCode(detail.getManagementOrg()) // EHR_CD
                    .budgetSubjectCode(detail.getBudgetSubjectCode()) // ERP_ACCT_CD
                    .masterProjectCode(detail.getMasterProjectCode())
                    .erpAssetType(detail.getErpAssetType())
                    .isInternal(isInternal)
                    .currency(detail.getCurrency())
                    .build();
            
            tempLedgerMap.put(bizKey, tempLedger);
            bizKeyToDetailMap.put(bizKey, detail);
            
            // 记录每个 detail 对应的季度列表
            String detailKey = detail.getQueryYear() + "@" + isInternal + "@" + 
                detail.getManagementOrg() + "@" + detail.getBudgetSubjectCode() + "@" + 
                detail.getMasterProjectCode() + "@" + detail.getErpAssetType();
            detailKeyToQuartersMap.put(detailKey, quarters);
        }
        
        // 批量提取需要映射的 erpAssetType 字段（以 "1" 或 "M" 开头且不带项目的）
        Set<String> erpAssetTypeSet = details.stream()
                .filter(detail -> {
                    String masterProjectCode = detail.getMasterProjectCode();
                    // 只提取不带项目的明细的 erpAssetType
                    return "NAN".equals(masterProjectCode) || StringUtils.isBlank(masterProjectCode);
                })
                .map(QueryDetailDetailVo::getErpAssetType)
                .filter(StringUtils::isNotBlank)
                .filter(code -> !"NAN".equals(code))
                .filter(code -> code.startsWith("1") || code.startsWith("M")) // 只提取需要映射的
                .collect(Collectors.toSet());
        
        // 批量查询 VIEW_BUDGET_MEMBER_NAME_CODE 视图，获取 MEMBER_CD2 对应的 MEMBER_CD（一一对应关系）
        Map<String, String> erpAssetTypeToMemberCdMap = budgetQueryHelperService.queryErpAssetTypeToMemberCdMap(erpAssetTypeSet);
        log.info("========== 获取到 erpAssetType 映射表，共 {} 条记录 ==========", erpAssetTypeToMemberCdMap.size());
        
        // 先调用 queryQuotaAndBalanceByAllQuarters 查询所有季度的 quota 和 balance（一对一映射）
        BudgetQueryHelperService.BudgetQuotaBalanceResult firstQueryResult = 
                budgetQueryHelperService.queryQuotaAndBalanceByAllQuarters(
                        tempLedgerMap,
                        ehrCdToOrgCdMap,
                        erpAcctCdToAcctCdMap,
                        erpAssetTypeToMemberCdMap);
        
        // 获取返回的 quotaMap 和 balanceMap，作为 needToUpdateSameDemBudgetQuotaMap 和 needToUpdateSameDemBudgetBalanceMap
        Map<String, BudgetQuota> needToUpdateSameDemBudgetQuotaMap = firstQueryResult.getQuotaMap();
        Map<String, BudgetBalance> needToUpdateSameDemBudgetBalanceMap = firstQueryResult.getBalanceMap();
        
        // 调用 queryQuotaAndBalanceByAllQuartersAllDem 查询所有季度的 balance（支持扩展映射）
        BudgetQueryHelperService.BudgetQuotaBalanceSimpleResult queryResult = 
                budgetQueryHelperService.queryQuotaAndBalanceByAllQuartersAllDem(
                        tempLedgerMap,
                        needToUpdateSameDemBudgetQuotaMap,
                        needToUpdateSameDemBudgetBalanceMap,
                        ehrCdToOrgCdMap,
                        ehrCdToOrgCdExtMap,
                        erpAcctCdToAcctCdMap,
                        erpAcctCdToAcctCdExtMap,
                        prjCdToRelatedPrjCdExtMap,
                        erpAssetTypeToMemberCdMap);
        
        // 获取返回的 balanceMap
        // key 格式：bizCode + "@" + bizItemCode + "@" + quarter
        Map<String, List<BudgetBalance>> balanceMap = queryResult.getBalanceMap();
        
        // 组装结果（累计可用余额计算与申请校验一致：历史季度需加上 amountOperated（申请单为 amountFrozen），当前季度只加 amountAvailable）
        List<ApplyQueryRespDetailVo> result = new ArrayList<>();
        for (Map.Entry<String, QueryDetailDetailVo> entry : bizKeyToDetailMap.entrySet()) {
            String bizKey = entry.getKey();
            QueryDetailDetailVo detail = entry.getValue();
            
            String currentQuarter = convertMonthToQuarter(detail.getQueryMonth());
            String isInternal = calculateIsInternal(detail);
            
            // 获取从 q1 到当前季度的所有季度
            String detailKey = detail.getQueryYear() + "@" + isInternal + "@" + 
                detail.getManagementOrg() + "@" + detail.getBudgetSubjectCode() + "@" + 
                detail.getMasterProjectCode() + "@" + detail.getErpAssetType();
            List<String> quarters = detailKeyToQuartersMap.get(detailKey);
            
            // 按与申请校验相同的规则累加累计可用余额：历史季度 = amountAvailable + amountOperated(当<0时)，当前季度 = amountAvailable
            BigDecimal totalAmountAvailable = BigDecimal.ZERO;
            BudgetLedger tempLedger = tempLedgerMap.get(bizKey);
            String bizCode = tempLedger.getBizCode();
            String bizItemCode = tempLedger.getBizItemCode();
            
            for (int qi = 0; qi < quarters.size(); qi++) {
                String quarter = quarters.get(qi);
                String bizKeyQuarter = bizCode + "@" + bizItemCode + "@" + quarter;
                List<BudgetBalance> balanceList = balanceMap.get(bizKeyQuarter);
                if (CollectionUtils.isEmpty(balanceList)) {
                    continue;
                }
                BigDecimal quarterAmountAvailable = BigDecimal.ZERO;
                for (BudgetBalance balance : balanceList) {
                    if (balance != null && balance.getAmountAvailable() != null) {
                        quarterAmountAvailable = quarterAmountAvailable.add(balance.getAmountAvailable());
                    }
                }
                boolean isCurrentQuarter = (qi == quarters.size() - 1);
                if (isCurrentQuarter) {
                    totalAmountAvailable = totalAmountAvailable.add(quarterAmountAvailable);
                } else {
                    // 历史季度：与 AbstractBudgetService 校验逻辑一致，当 amountOperated < 0 时累计加上 amountOperated（申请单为 amountFrozen）
                    BigDecimal quarterAmountOperated = getApplyQuarterAmountOperated(balanceList);
                    if (quarterAmountOperated.compareTo(BigDecimal.ZERO) < 0) {
                        totalAmountAvailable = totalAmountAvailable.add(quarterAmountAvailable).add(quarterAmountOperated);
                    } else {
                        totalAmountAvailable = totalAmountAvailable.add(quarterAmountAvailable);
                    }
                }
            }
            
            ApplyQueryRespDetailVo vo = new ApplyQueryRespDetailVo();
            vo.setDemandYear(detail.getQueryYear());
            vo.setDemandMonth(detail.getQueryMonth());
            vo.setCompany(detail.getCompany());
            vo.setDepartment(detail.getDepartment());
            vo.setManagementOrg(detail.getManagementOrg());
            
            // 设置管理组织名称
            String managementOrg = detail.getManagementOrg();
            if (StringUtils.isNotBlank(managementOrg) && ehrCdToEhrNmMap != null) {
                String managementOrgName = ehrCdToEhrNmMap.get(managementOrg);
                vo.setManagementOrgName(managementOrgName);
            }
            
            vo.setBudgetSubjectCode(detail.getBudgetSubjectCode());
            
            // 设置预算科目名称
            String budgetSubjectCode = detail.getBudgetSubjectCode();
            if (StringUtils.isNotBlank(budgetSubjectCode) && erpAcctCdToErpAcctNmMap != null) {
                String budgetSubjectName = erpAcctCdToErpAcctNmMap.get(budgetSubjectCode);
                vo.setBudgetSubjectName(budgetSubjectName);
            }
            
            vo.setMasterProjectCode(detail.getMasterProjectCode());
            vo.setErpAssetType(detail.getErpAssetType());
            vo.setIsInternal(detail.getIsInternal());
            vo.setCurrency(detail.getCurrency());
            vo.setAmountAvailable(totalAmountAvailable);
            
            // 设置 metadata：使用传入的 metadata
            if (detail.getMetadata() != null && !detail.getMetadata().isEmpty()) {
                vo.setMetadata(detail.getMetadata());
            } else {
                vo.setMetadata(new HashMap<>());
            }
            
            result.add(vo);
        }
        
        return result;
    }

    /**
     * 通过维度查询 BudgetBalance（需求单+付款单场景）
     * 对于以@NAN结尾的detailLineNo，需要计算amountPayAvailable并与需求单可用余额比较取较小值
     */
    private List<ApplyQueryRespDetailVo> queryByDimensionsForApplyWithClaim(List<QueryDetailDetailVo> details) {
        // 先调用原有的queryByDimensionsForApply方法获取需求单可用余额
        List<ApplyQueryRespDetailVo> applyDetailVos = queryByDimensionsForApply(details);
        
        // 建立detail到vo的映射（通过detailLineNo的前缀部分匹配，因为vo不包含季度信息）
        Map<String, ApplyQueryRespDetailVo> detailKeyToVoMap = new HashMap<>();
        for (ApplyQueryRespDetailVo vo : applyDetailVos) {
            // 构建key：year@isInternal@morgCode@budgetSubjectCode@masterProjectCode@erpAssetType
            String isInternal = vo.getIsInternal() != null ? vo.getIsInternal() : "0";
            String key = vo.getDemandYear() + "@" + isInternal + "@" +
                vo.getManagementOrg() + "@" + vo.getBudgetSubjectCode() + "@" +
                vo.getMasterProjectCode() + "@" + vo.getErpAssetType();
            detailKeyToVoMap.put(key, vo);
        }
        
        // 对于以@NAN结尾的detailLineNo，计算amountPayAvailable并比较
        for (QueryDetailDetailVo detail : details) {
            String detailLineNo = detail.getDetailLineNo();
            if (detailLineNo != null && detailLineNo.endsWith("@NAN")) {
                // 构建key来匹配vo
                String isInternal = calculateIsInternal(detail);
                String key = detail.getQueryYear() + "@" + isInternal + "@" +
                    detail.getManagementOrg() + "@" + detail.getBudgetSubjectCode() + "@" +
                    detail.getMasterProjectCode() + "@" + detail.getErpAssetType();
                
                ApplyQueryRespDetailVo vo = detailKeyToVoMap.get(key);
                if (vo != null) {
                    // 如果以@NAN结尾，查询从q1到当前季度的balance并计算amountPayAvailable合计值
                    BigDecimal totalAmountPayAvailable = calculateTotalAmountPayAvailable(detail);
                    // 与需求单可用余额比较，取较小值
                    BigDecimal demandAmountAvailable = vo.getAmountAvailable() != null ? vo.getAmountAvailable() : BigDecimal.ZERO;
                    vo.setAmountAvailable(demandAmountAvailable.min(totalAmountPayAvailable));
                }
            }
        }
        
        return applyDetailVos;
    }

    /**
     * 通过维度查询 BudgetBalance（合同）
     * 获取本年度当前季度往前累积的可用余额
     */
    private List<ContractQueryRespDetailVo> queryByDimensionsForContract(List<QueryDetailDetailVo> details) {
        // 批量提取 managementOrg 字段（对应 EHR_CD）
        Set<String> managementOrgSet = details.stream()
                .map(QueryDetailDetailVo::getManagementOrg)
                .filter(StringUtils::isNotBlank)
                .collect(Collectors.toSet());
        
        // 批量查询 EHR_ORG_MANAGE_R 表，获取 EHR_CD 对应的 ORG_CD（一一对应关系）
        // Map<EHR_CD, ORG_CD>
        BudgetQueryHelperService.EhrCdToOrgCdMapResult ehrCdToOrgCdMapResult = budgetQueryHelperService.queryEhrCdToOrgCdOneToOneMap(managementOrgSet);
        Map<String, String> ehrCdToOrgCdMap = ehrCdToOrgCdMapResult.getEhrCdToOrgCdMap();
        Map<String, String> ehrCdToEhrNmMap = ehrCdToOrgCdMapResult.getEhrCdToEhrNmMap();
        
        // 批量查询 EHR_ORG_MANAGE_EXT_R 表，获取 EHR_CD 对应的所有 ORG_CD（一对多关系）
        // Map<EHR_CD, List<ORG_CD>>
        Map<String, List<String>> ehrCdToOrgCdExtMap = budgetQueryHelperService.queryEhrCdToOrgCdMap(managementOrgSet);
        
        // 批量提取 budgetSubjectCode 字段
        Set<String> budgetSubjectCodeSet = details.stream()
                .map(QueryDetailDetailVo::getBudgetSubjectCode)
                .filter(StringUtils::isNotBlank)
                .filter(code -> !"NAN-NAN".equals(code))
                .collect(Collectors.toSet());
        
        // 批量查询 SUBJECT_INFO 表，获取 ERP_ACCT_CD 对应的 ACCT_CD（一一对应关系）
        // Map<ERP_ACCT_CD, ACCT_CD>
        BudgetQueryHelperService.ErpAcctCdToAcctCdMapResult erpAcctCdToAcctCdMapResult = budgetQueryHelperService.queryErpAcctCdToAcctCdOneToOneMap(budgetSubjectCodeSet);
        Map<String, String> erpAcctCdToAcctCdMap = erpAcctCdToAcctCdMapResult.getErpAcctCdToAcctCdMap();
        Map<String, String> erpAcctCdToErpAcctNmMap = erpAcctCdToAcctCdMapResult.getErpAcctCdToErpAcctNmMap();
        
        // 批量查询 SUBJECT_EXT_INFO 表，获取 ERP_ACCT_CD 对应的所有 ACCT_CD（一对多关系）
        // Map<ERP_ACCT_CD, List<ACCT_CD>>
        Map<String, List<String>> erpAcctCdToAcctCdExtMap = budgetQueryHelperService.queryErpAcctCdToAcctCdMap(budgetSubjectCodeSet);
        
        // 检查 erpAcctCdToAcctCdExtMap 中是否有任何 value（List）包含 "NAN-NAN"
        // 如果包含，说明没有映射到且是白名单，原样返回入参
        if (erpAcctCdToAcctCdExtMap != null && !erpAcctCdToAcctCdExtMap.isEmpty()) {
            for (Map.Entry<String, List<String>> entry : erpAcctCdToAcctCdExtMap.entrySet()) {
                List<String> acctCdList = entry.getValue();
                if (acctCdList != null && acctCdList.contains("NAN-NAN")) {
                    log.info("检测到 erpAcctCdToAcctCdExtMap 中包含 NAN-NAN，说明是白名单且没有映射，原样返回入参");
                    // 将入参的 details 转换为 ContractQueryRespDetailVo
                    List<ContractQueryRespDetailVo> result = new ArrayList<>();
                    for (QueryDetailDetailVo detail : details) {
                        ContractQueryRespDetailVo vo = new ContractQueryRespDetailVo();
                        vo.setContractYear(detail.getQueryYear());
                        vo.setContractMonth(detail.getQueryMonth());
                        vo.setCompany(detail.getCompany());
                        vo.setDepartment(detail.getDepartment());
                        vo.setManagementOrg(detail.getManagementOrg());
                        vo.setBudgetSubjectCode(detail.getBudgetSubjectCode());
                        vo.setMasterProjectCode(detail.getMasterProjectCode());
                        vo.setErpAssetType(detail.getErpAssetType());
                        vo.setIsInternal(detail.getIsInternal());
                        vo.setCurrency(detail.getCurrency());
                        vo.setAmountAvailable(BigDecimal.ZERO);
                        if (detail.getMetadata() != null && !detail.getMetadata().isEmpty()) {
                            vo.setMetadata(new HashMap<>(detail.getMetadata()));
                        } else {
                            vo.setMetadata(new HashMap<>());
                        }
                        result.add(vo);
                    }
                    return result;
                }
            }
        }
        
        // 批量提取 masterProjectCode 字段
        Set<String> masterProjectCodeSet = details.stream()
                .map(QueryDetailDetailVo::getMasterProjectCode)
                .filter(StringUtils::isNotBlank)
                .filter(code -> !"NAN".equals(code))
                .collect(Collectors.toSet());
        
        // 批量查询 PROJECT_CONTROL_EXT_R 表，获取 PRJ_CD 对应的所有 RELATED_PRJ_CD（一对多关系）
        // Map<PRJ_CD, List<RELATED_PRJ_CD>>
        Map<String, List<String>> prjCdToRelatedPrjCdExtMap = budgetQueryHelperService.queryPrjCdToRelatedPrjCdMap(masterProjectCodeSet);
        
        // 为每个 detail 构建临时的 BudgetLedger 对象，用于调用 queryQuotaAndBalanceByAllQuartersAllDem
        Map<String, BudgetLedger> tempLedgerMap = new HashMap<>();
        Map<String, QueryDetailDetailVo> bizKeyToDetailMap = new HashMap<>();
        Map<String, List<String>> detailKeyToQuartersMap = new HashMap<>();
        
        for (QueryDetailDetailVo detail : details) {
            String currentQuarter = convertMonthToQuarter(detail.getQueryMonth());
            if (currentQuarter == null) {
                throw new IllegalArgumentException("无效的查询月份: " + detail.getQueryMonth());
            }
            
            // 获取从 q1 到当前季度的所有季度
            List<String> quarters = getQuartersUpTo(currentQuarter);
            String isInternal = calculateIsInternal(detail);
            
            // 构建 bizItemCode，参考 ContractDetailDetailVo.getContractDetailLineNo() 的格式
            // 格式: isInternal@managementOrg@budgetSubjectCode@masterProjectCode@erpAssetType
            String bizItemCode = isInternal + "@" + 
                detail.getManagementOrg() + "@" + 
                detail.getBudgetSubjectCode() + "@" + 
                detail.getMasterProjectCode() + "@" + 
                detail.getErpAssetType();
            
            // 构建唯一的 bizCode
            String bizCode = "QUERY";
            
            // 构建 bizKey（格式：bizCode + "@" + bizItemCode）
            String bizKey = bizCode + "@" + bizItemCode;
            
            // 创建临时的 BudgetLedger 对象
            // 注意：morgCode 和 budgetSubjectCode 存储的是原始值（EHR_CD 和 ERP_ACCT_CD），
            // queryQuotaAndBalanceByAllQuartersAllDem 方法内部会进行映射转换
            BudgetLedger tempLedger = BudgetLedger.builder()
                    .bizType(CONTRACT_BIZ_TYPE)
                    .bizCode(bizCode)
                    .bizItemCode(bizItemCode)
                    .year(detail.getQueryYear())
                    .month(detail.getQueryMonth())
                    .morgCode(detail.getManagementOrg()) // EHR_CD
                    .budgetSubjectCode(detail.getBudgetSubjectCode()) // ERP_ACCT_CD
                    .masterProjectCode(detail.getMasterProjectCode())
                    .erpAssetType(detail.getErpAssetType())
                    .isInternal(isInternal)
                    .currency(detail.getCurrency())
                    .build();
            
            tempLedgerMap.put(bizKey, tempLedger);
            bizKeyToDetailMap.put(bizKey, detail);
            
            // 记录每个 detail 对应的季度列表
            String detailKey = detail.getQueryYear() + "@" + isInternal + "@" + 
                detail.getManagementOrg() + "@" + detail.getBudgetSubjectCode() + "@" + 
                detail.getMasterProjectCode() + "@" + detail.getErpAssetType();
            detailKeyToQuartersMap.put(detailKey, quarters);
        }
        
        // 批量提取需要映射的 erpAssetType 字段（以 "1" 或 "M" 开头且不带项目的）
        Set<String> erpAssetTypeSet = details.stream()
                .filter(detail -> {
                    String masterProjectCode = detail.getMasterProjectCode();
                    // 只提取不带项目的明细的 erpAssetType
                    return "NAN".equals(masterProjectCode) || StringUtils.isBlank(masterProjectCode);
                })
                .map(QueryDetailDetailVo::getErpAssetType)
                .filter(StringUtils::isNotBlank)
                .filter(code -> !"NAN".equals(code))
                .filter(code -> code.startsWith("1") || code.startsWith("M")) // 只提取需要映射的
                .collect(Collectors.toSet());
        
        // 批量查询 VIEW_BUDGET_MEMBER_NAME_CODE 视图，获取 MEMBER_CD2 对应的 MEMBER_CD（一一对应关系）
        Map<String, String> erpAssetTypeToMemberCdMap = budgetQueryHelperService.queryErpAssetTypeToMemberCdMap(erpAssetTypeSet);
        log.info("========== 获取到 erpAssetType 映射表，共 {} 条记录 ==========", erpAssetTypeToMemberCdMap.size());
        
        // 先调用 queryQuotaAndBalanceByAllQuarters 查询所有季度的 quota 和 balance（一对一映射）
        BudgetQueryHelperService.BudgetQuotaBalanceResult firstQueryResult = 
                budgetQueryHelperService.queryQuotaAndBalanceByAllQuarters(
                        tempLedgerMap,
                        ehrCdToOrgCdMap,
                        erpAcctCdToAcctCdMap,
                        erpAssetTypeToMemberCdMap);
        
        // 获取返回的 quotaMap 和 balanceMap，作为 needToUpdateSameDemBudgetQuotaMap 和 needToUpdateSameDemBudgetBalanceMap
        Map<String, BudgetQuota> needToUpdateSameDemBudgetQuotaMap = firstQueryResult.getQuotaMap();
        Map<String, BudgetBalance> needToUpdateSameDemBudgetBalanceMap = firstQueryResult.getBalanceMap();
        
        // 调用 queryQuotaAndBalanceByAllQuartersAllDem 查询所有季度的 balance（支持扩展映射）
        BudgetQueryHelperService.BudgetQuotaBalanceSimpleResult queryResult = 
                budgetQueryHelperService.queryQuotaAndBalanceByAllQuartersAllDem(
                        tempLedgerMap,
                        needToUpdateSameDemBudgetQuotaMap,
                        needToUpdateSameDemBudgetBalanceMap,
                        ehrCdToOrgCdMap,
                        ehrCdToOrgCdExtMap,
                        erpAcctCdToAcctCdMap,
                        erpAcctCdToAcctCdExtMap,
                        prjCdToRelatedPrjCdExtMap,
                        erpAssetTypeToMemberCdMap);
        
        // 获取返回的 balanceMap
        // key 格式：bizCode + "@" + bizItemCode + "@" + quarter
        Map<String, List<BudgetBalance>> balanceMap = queryResult.getBalanceMap();
        
        // 组装结果（累计可用余额计算与合同校验一致：历史季度需加上 amountOperated（合同单为 amountOccupied），当前季度只加 amountAvailable）
        List<ContractQueryRespDetailVo> result = new ArrayList<>();
        for (Map.Entry<String, QueryDetailDetailVo> entry : bizKeyToDetailMap.entrySet()) {
            String bizKey = entry.getKey();
            QueryDetailDetailVo detail = entry.getValue();
            
            String currentQuarter = convertMonthToQuarter(detail.getQueryMonth());
            String isInternal = calculateIsInternal(detail);
            
            // 获取从 q1 到当前季度的所有季度
            String detailKey = detail.getQueryYear() + "@" + isInternal + "@" + 
                detail.getManagementOrg() + "@" + detail.getBudgetSubjectCode() + "@" + 
                detail.getMasterProjectCode() + "@" + detail.getErpAssetType();
            List<String> quarters = detailKeyToQuartersMap.get(detailKey);
            
            // 按与合同校验相同的规则累加累计可用余额：历史季度 = amountAvailable + amountOperated(当<0时)，当前季度 = amountAvailable
            BigDecimal totalAmountAvailable = BigDecimal.ZERO;
            BudgetLedger tempLedger = tempLedgerMap.get(bizKey);
            String bizCode = tempLedger.getBizCode();
            String bizItemCode = tempLedger.getBizItemCode();
            
            for (int qi = 0; qi < quarters.size(); qi++) {
                String quarter = quarters.get(qi);
                String bizKeyQuarter = bizCode + "@" + bizItemCode + "@" + quarter;
                List<BudgetBalance> balanceList = balanceMap.get(bizKeyQuarter);
                if (CollectionUtils.isEmpty(balanceList)) {
                    continue;
                }
                BigDecimal quarterAmountAvailable = BigDecimal.ZERO;
                for (BudgetBalance balance : balanceList) {
                    if (balance != null && balance.getAmountAvailable() != null) {
                        quarterAmountAvailable = quarterAmountAvailable.add(balance.getAmountAvailable());
                    }
                }
                boolean isCurrentQuarter = (qi == quarters.size() - 1);
                if (isCurrentQuarter) {
                    totalAmountAvailable = totalAmountAvailable.add(quarterAmountAvailable);
                } else {
                    // 历史季度：与 AbstractBudgetService 校验逻辑一致，当 amountOperated < 0 时累计加上 amountOperated（合同单为 amountOccupied）
                    BigDecimal quarterAmountOperated = getContractQuarterAmountOperated(balanceList);
                    if (quarterAmountOperated.compareTo(BigDecimal.ZERO) < 0) {
                        totalAmountAvailable = totalAmountAvailable.add(quarterAmountAvailable).add(quarterAmountOperated);
                    } else {
                        totalAmountAvailable = totalAmountAvailable.add(quarterAmountAvailable);
                    }
                }
            }
            
            ContractQueryRespDetailVo vo = new ContractQueryRespDetailVo();
            vo.setContractYear(detail.getQueryYear());
            vo.setContractMonth(detail.getQueryMonth());
            vo.setCompany(detail.getCompany());
            vo.setDepartment(detail.getDepartment());
            vo.setManagementOrg(detail.getManagementOrg());
            
            // 设置管理组织名称
            String managementOrg = detail.getManagementOrg();
            if (StringUtils.isNotBlank(managementOrg) && ehrCdToEhrNmMap != null) {
                String managementOrgName = ehrCdToEhrNmMap.get(managementOrg);
                vo.setManagementOrgName(managementOrgName);
            }
            
            vo.setBudgetSubjectCode(detail.getBudgetSubjectCode());
            
            // 设置预算科目名称
            String budgetSubjectCode = detail.getBudgetSubjectCode();
            if (StringUtils.isNotBlank(budgetSubjectCode) && erpAcctCdToErpAcctNmMap != null) {
                String budgetSubjectName = erpAcctCdToErpAcctNmMap.get(budgetSubjectCode);
                vo.setBudgetSubjectName(budgetSubjectName);
            }
            
            vo.setMasterProjectCode(detail.getMasterProjectCode());
            vo.setErpAssetType(detail.getErpAssetType());
            vo.setIsInternal(detail.getIsInternal());
            vo.setCurrency(detail.getCurrency());
            vo.setAmountAvailable(totalAmountAvailable);
            
            // 设置 metadata：使用传入的 metadata
            if (detail.getMetadata() != null && !detail.getMetadata().isEmpty()) {
                vo.setMetadata(detail.getMetadata());
            } else {
                vo.setMetadata(new HashMap<>());
            }
            
            result.add(vo);
        }
        
        return result;
    }

    /**
     * 通过维度查询 BudgetBalance（付款/报销）
     * 获取本年度当前季度往前累积的可用余额
     */
    private List<ClaimQueryRespDetailVo> queryByDimensionsForClaim(List<QueryDetailDetailVo> details) {
        // 批量提取 managementOrg 字段（对应 EHR_CD）
        Set<String> managementOrgSet = details.stream()
                .map(QueryDetailDetailVo::getManagementOrg)
                .filter(StringUtils::isNotBlank)
                .collect(Collectors.toSet());
        
        // 批量查询 EHR_ORG_MANAGE_R 表，获取 EHR_CD 对应的 ORG_CD（一一对应关系）
        // Map<EHR_CD, ORG_CD>
        BudgetQueryHelperService.EhrCdToOrgCdMapResult ehrCdToOrgCdMapResult = budgetQueryHelperService.queryEhrCdToOrgCdOneToOneMap(managementOrgSet);
        Map<String, String> ehrCdToOrgCdMap = ehrCdToOrgCdMapResult.getEhrCdToOrgCdMap();
        Map<String, String> ehrCdToEhrNmMap = ehrCdToOrgCdMapResult.getEhrCdToEhrNmMap();
        
        // 批量查询 EHR_ORG_MANAGE_EXT_R 表，获取 EHR_CD 对应的所有 ORG_CD（一对多关系）
        // Map<EHR_CD, List<ORG_CD>>
        Map<String, List<String>> ehrCdToOrgCdExtMap = budgetQueryHelperService.queryEhrCdToOrgCdMap(managementOrgSet);
        
        // 批量提取 budgetSubjectCode 字段
        Set<String> budgetSubjectCodeSet = details.stream()
                .map(QueryDetailDetailVo::getBudgetSubjectCode)
                .filter(StringUtils::isNotBlank)
                .filter(code -> !"NAN-NAN".equals(code))
                .collect(Collectors.toSet());
        
        // 批量查询 SUBJECT_INFO 表，获取 ERP_ACCT_CD 对应的 ACCT_CD（一一对应关系）
        // Map<ERP_ACCT_CD, ACCT_CD>
        BudgetQueryHelperService.ErpAcctCdToAcctCdMapResult erpAcctCdToAcctCdMapResult = budgetQueryHelperService.queryErpAcctCdToAcctCdOneToOneMap(budgetSubjectCodeSet);
        Map<String, String> erpAcctCdToAcctCdMap = erpAcctCdToAcctCdMapResult.getErpAcctCdToAcctCdMap();
        Map<String, String> erpAcctCdToErpAcctNmMap = erpAcctCdToAcctCdMapResult.getErpAcctCdToErpAcctNmMap();
        
        // 批量查询 SUBJECT_EXT_INFO 表，获取 ERP_ACCT_CD 对应的所有 ACCT_CD（一对多关系）
        // Map<ERP_ACCT_CD, List<ACCT_CD>>
        Map<String, List<String>> erpAcctCdToAcctCdExtMap = budgetQueryHelperService.queryErpAcctCdToAcctCdMap(budgetSubjectCodeSet);
        
        // 检查 erpAcctCdToAcctCdExtMap 中是否有任何 value（List）包含 "NAN-NAN"
        // 如果包含，说明没有映射到且是白名单，原样返回入参
        if (erpAcctCdToAcctCdExtMap != null && !erpAcctCdToAcctCdExtMap.isEmpty()) {
            for (Map.Entry<String, List<String>> entry : erpAcctCdToAcctCdExtMap.entrySet()) {
                List<String> acctCdList = entry.getValue();
                if (acctCdList != null && acctCdList.contains("NAN-NAN")) {
                    log.info("检测到 erpAcctCdToAcctCdExtMap 中包含 NAN-NAN，说明是白名单且没有映射，原样返回入参");
                    // 将入参的 details 转换为 ClaimQueryRespDetailVo
                    List<ClaimQueryRespDetailVo> result = new ArrayList<>();
                    for (QueryDetailDetailVo detail : details) {
                        ClaimQueryRespDetailVo vo = new ClaimQueryRespDetailVo();
                        vo.setClaimYear(detail.getQueryYear());
                        vo.setClaimMonth(detail.getQueryMonth());
                        vo.setActualYear(detail.getQueryYear());
                        vo.setActualMonth(detail.getQueryMonth());
                        vo.setCompany(detail.getCompany());
                        vo.setDepartment(detail.getDepartment());
                        vo.setManagementOrg(detail.getManagementOrg());
                        vo.setBudgetSubjectCode(detail.getBudgetSubjectCode());
                        vo.setMasterProjectCode(detail.getMasterProjectCode());
                        vo.setErpAssetType(detail.getErpAssetType());
                        vo.setIsInternal(detail.getIsInternal());
                        vo.setCurrency(detail.getCurrency());
                        vo.setAmountAvailable(BigDecimal.ZERO);
                        if (detail.getMetadata() != null && !detail.getMetadata().isEmpty()) {
                            vo.setMetadata(new HashMap<>(detail.getMetadata()));
                        } else {
                            vo.setMetadata(new HashMap<>());
                        }
                        result.add(vo);
                    }
                    return result;
                }
            }
        }
        
        // 批量提取 masterProjectCode 字段
        Set<String> masterProjectCodeSet = details.stream()
                .map(QueryDetailDetailVo::getMasterProjectCode)
                .filter(StringUtils::isNotBlank)
                .filter(code -> !"NAN".equals(code))
                .collect(Collectors.toSet());
        
        // 批量查询 PROJECT_CONTROL_EXT_R 表，获取 PRJ_CD 对应的所有 RELATED_PRJ_CD（一对多关系）
        // Map<PRJ_CD, List<RELATED_PRJ_CD>>
        Map<String, List<String>> prjCdToRelatedPrjCdExtMap = budgetQueryHelperService.queryPrjCdToRelatedPrjCdMap(masterProjectCodeSet);
        
        // 为每个 detail 构建临时的 BudgetLedger 对象，用于调用 queryQuotaAndBalanceByAllQuartersAllDem
        Map<String, BudgetLedger> tempLedgerMap = new HashMap<>();
        Map<String, QueryDetailDetailVo> bizKeyToDetailMap = new HashMap<>();
        Map<String, List<String>> detailKeyToQuartersMap = new HashMap<>();
        
        for (QueryDetailDetailVo detail : details) {
            String currentQuarter = convertMonthToQuarter(detail.getQueryMonth());
            if (currentQuarter == null) {
                throw new IllegalArgumentException("无效的查询月份: " + detail.getQueryMonth());
            }
            
            // 获取从 q1 到当前季度的所有季度
            List<String> quarters = getQuartersUpTo(currentQuarter);
            String isInternal = calculateIsInternal(detail);
            
            // 构建 bizItemCode，参考 ClaimDetailDetailVo.getClaimDetailLineNo() 的格式
            // 格式: isInternal@managementOrg@budgetSubjectCode@masterProjectCode@erpAssetType
            String bizItemCode = isInternal + "@" + 
                detail.getManagementOrg() + "@" + 
                detail.getBudgetSubjectCode() + "@" + 
                detail.getMasterProjectCode() + "@" + 
                detail.getErpAssetType();
            
            // 构建唯一的 bizCode
            String bizCode = "QUERY";
            
            // 构建 bizKey（格式：bizCode + "@" + bizItemCode）
            String bizKey = bizCode + "@" + bizItemCode;
            
            // 创建临时的 BudgetLedger 对象
            // 注意：morgCode 和 budgetSubjectCode 存储的是原始值（EHR_CD 和 ERP_ACCT_CD），
            // queryQuotaAndBalanceByAllQuartersAllDem 方法内部会进行映射转换
            BudgetLedger tempLedger = BudgetLedger.builder()
                    .bizType(CLAIM_BIZ_TYPE)
                    .bizCode(bizCode)
                    .bizItemCode(bizItemCode)
                    .year(detail.getQueryYear())
                    .month(detail.getQueryMonth())
                    .morgCode(detail.getManagementOrg()) // EHR_CD
                    .budgetSubjectCode(detail.getBudgetSubjectCode()) // ERP_ACCT_CD
                    .masterProjectCode(detail.getMasterProjectCode())
                    .erpAssetType(detail.getErpAssetType())
                    .isInternal(isInternal)
                    .currency(detail.getCurrency())
                    .build();
            
            tempLedgerMap.put(bizKey, tempLedger);
            bizKeyToDetailMap.put(bizKey, detail);
            
            // 记录每个 detail 对应的季度列表
            String detailKey = detail.getQueryYear() + "@" + isInternal + "@" + 
                detail.getManagementOrg() + "@" + detail.getBudgetSubjectCode() + "@" + 
                detail.getMasterProjectCode() + "@" + detail.getErpAssetType();
            detailKeyToQuartersMap.put(detailKey, quarters);
        }
        
        // 批量提取需要映射的 erpAssetType 字段（以 "1" 或 "M" 开头且不带项目的）
        Set<String> erpAssetTypeSet = details.stream()
                .filter(detail -> {
                    String masterProjectCode = detail.getMasterProjectCode();
                    // 只提取不带项目的明细的 erpAssetType
                    return "NAN".equals(masterProjectCode) || StringUtils.isBlank(masterProjectCode);
                })
                .map(QueryDetailDetailVo::getErpAssetType)
                .filter(StringUtils::isNotBlank)
                .filter(code -> !"NAN".equals(code))
                .filter(code -> code.startsWith("1") || code.startsWith("M")) // 只提取需要映射的
                .collect(Collectors.toSet());
        
        // 批量查询 VIEW_BUDGET_MEMBER_NAME_CODE 视图，获取 MEMBER_CD2 对应的 MEMBER_CD（一一对应关系）
        Map<String, String> erpAssetTypeToMemberCdMap = budgetQueryHelperService.queryErpAssetTypeToMemberCdMap(erpAssetTypeSet);
        log.info("========== 获取到 erpAssetType 映射表，共 {} 条记录 ==========", erpAssetTypeToMemberCdMap.size());
        
        // 先调用 queryQuotaAndBalanceByAllQuarters 查询所有季度的 quota 和 balance（一对一映射）
        BudgetQueryHelperService.BudgetQuotaBalanceResult firstQueryResult = 
                budgetQueryHelperService.queryQuotaAndBalanceByAllQuarters(
                        tempLedgerMap,
                        ehrCdToOrgCdMap,
                        erpAcctCdToAcctCdMap,
                        erpAssetTypeToMemberCdMap);
        
        // 获取返回的 quotaMap 和 balanceMap，作为 needToUpdateSameDemBudgetQuotaMap 和 needToUpdateSameDemBudgetBalanceMap
        Map<String, BudgetQuota> needToUpdateSameDemBudgetQuotaMap = firstQueryResult.getQuotaMap();
        Map<String, BudgetBalance> needToUpdateSameDemBudgetBalanceMap = firstQueryResult.getBalanceMap();
        
        // 调用 queryQuotaAndBalanceByAllQuartersAllDem 查询所有季度的 balance（支持扩展映射）
        BudgetQueryHelperService.BudgetQuotaBalanceSimpleResult queryResult = 
                budgetQueryHelperService.queryQuotaAndBalanceByAllQuartersAllDem(
                        tempLedgerMap,
                        needToUpdateSameDemBudgetQuotaMap,
                        needToUpdateSameDemBudgetBalanceMap,
                        ehrCdToOrgCdMap,
                        ehrCdToOrgCdExtMap,
                        erpAcctCdToAcctCdMap,
                        erpAcctCdToAcctCdExtMap,
                        prjCdToRelatedPrjCdExtMap,
                        erpAssetTypeToMemberCdMap);
        
        // 获取返回的 balanceMap
        // key 格式：bizCode + "@" + bizItemCode + "@" + quarter
        Map<String, List<BudgetBalance>> balanceMap = queryResult.getBalanceMap();
        
        // 组装结果
        List<ClaimQueryRespDetailVo> result = new ArrayList<>();
        for (Map.Entry<String, QueryDetailDetailVo> entry : bizKeyToDetailMap.entrySet()) {
            String bizKey = entry.getKey();
            QueryDetailDetailVo detail = entry.getValue();
            
            String currentQuarter = convertMonthToQuarter(detail.getQueryMonth());
            String isInternal = calculateIsInternal(detail);
            
            // 获取从 q1 到当前季度的所有季度
            String detailKey = detail.getQueryYear() + "@" + isInternal + "@" + 
                detail.getManagementOrg() + "@" + detail.getBudgetSubjectCode() + "@" + 
                detail.getMasterProjectCode() + "@" + detail.getErpAssetType();
            List<String> quarters = detailKeyToQuartersMap.get(detailKey);
            
            // 累加从 q1 到当前季度的所有 balance 的可用余额
            // 如果 amountAvailable 为 null（带项目且没有采购额），使用 amountPayAvailable
            BigDecimal totalAmountAvailable = BigDecimal.ZERO;
            BudgetLedger tempLedger = tempLedgerMap.get(bizKey);
            String bizCode = tempLedger.getBizCode();
            String bizItemCode = tempLedger.getBizItemCode();

            for (String quarter : quarters) {
                String bizKeyQuarter = bizCode + "@" + bizItemCode + "@" + quarter;
                List<BudgetBalance> balanceList = balanceMap.get(bizKeyQuarter);
                if (!CollectionUtils.isEmpty(balanceList)) {
                    for (BudgetBalance balance : balanceList) {
                        if (balance != null) {
                            // 如果 amountAvailable 不为 null，使用 amountAvailable
                            if (balance.getAmountAvailable() != null) {
                                totalAmountAvailable = totalAmountAvailable.add(balance.getAmountAvailable());
                            } else if (balance.getAmountPayAvailable() != null) {
                                // 如果 amountAvailable 为 null，使用 amountPayAvailable（带项目且没有采购额的情况）
                                totalAmountAvailable = totalAmountAvailable.add(balance.getAmountPayAvailable());
                            }
                        }
                    }
                }
            }
            
            ClaimQueryRespDetailVo vo = new ClaimQueryRespDetailVo();
            vo.setClaimYear(detail.getQueryYear());
            vo.setClaimMonth(detail.getQueryMonth());
            vo.setActualYear(detail.getQueryYear());
            vo.setActualMonth(detail.getQueryMonth());
            vo.setCompany(detail.getCompany());
            vo.setDepartment(detail.getDepartment());
            vo.setManagementOrg(detail.getManagementOrg());
            
            // 设置管理组织名称
            String managementOrg = detail.getManagementOrg();
            if (StringUtils.isNotBlank(managementOrg) && ehrCdToEhrNmMap != null) {
                String managementOrgName = ehrCdToEhrNmMap.get(managementOrg);
                vo.setManagementOrgName(managementOrgName);
            }
            
            vo.setBudgetSubjectCode(detail.getBudgetSubjectCode());
            
            // 设置预算科目名称
            String budgetSubjectCode = detail.getBudgetSubjectCode();
            if (StringUtils.isNotBlank(budgetSubjectCode) && erpAcctCdToErpAcctNmMap != null) {
                String budgetSubjectName = erpAcctCdToErpAcctNmMap.get(budgetSubjectCode);
                vo.setBudgetSubjectName(budgetSubjectName);
            }
            
            vo.setMasterProjectCode(detail.getMasterProjectCode());
            vo.setErpAssetType(detail.getErpAssetType());
            vo.setIsInternal(detail.getIsInternal());
            vo.setCurrency(detail.getCurrency());
            vo.setAmountAvailable(totalAmountAvailable);
            
            // 设置 metadata：使用传入的 metadata
            if (detail.getMetadata() != null && !detail.getMetadata().isEmpty()) {
                vo.setMetadata(detail.getMetadata());
            } else {
                vo.setMetadata(new HashMap<>());
            }
            
            result.add(vo);
        }
        
        return result;
    }

    /**
     * 转换 BudgetLedger 为 ApplyQueryRespDetailVo
     */
    private ApplyQueryRespDetailVo convertToApplyDetailVo(BudgetLedger ledger, List<QueryDetailDetailVo> details,
                                                           Map<String, String> ehrCdToEhrNmMap,
                                                           Map<String, String> erpAcctCdToErpAcctNmMap) {
        ApplyQueryRespDetailVo vo = new ApplyQueryRespDetailVo();
        vo.setDemandYear(ledger.getYear());
        vo.setDemandMonth(ledger.getMonth());
        vo.setManagementOrg(ledger.getMorgCode());
        
        // 设置管理组织名称
        String managementOrg = ledger.getMorgCode();
        if (StringUtils.isNotBlank(managementOrg) && ehrCdToEhrNmMap != null) {
            String managementOrgName = ehrCdToEhrNmMap.get(managementOrg);
            vo.setManagementOrgName(managementOrgName);
        }
        
        vo.setBudgetSubjectCode(ledger.getBudgetSubjectCode());
        
        // 设置预算科目名称
        String budgetSubjectCode = ledger.getBudgetSubjectCode();
        if (StringUtils.isNotBlank(budgetSubjectCode) && erpAcctCdToErpAcctNmMap != null) {
            String budgetSubjectName = erpAcctCdToErpAcctNmMap.get(budgetSubjectCode);
            vo.setBudgetSubjectName(budgetSubjectName);
        }
        
        vo.setMasterProjectCode(ledger.getMasterProjectCode());
        vo.setErpAssetType(ledger.getErpAssetType());
        vo.setIsInternal(ledger.getIsInternal());
        vo.setCurrency(ledger.getCurrency());
        vo.setAmountAvailable(ledger.getAmountAvailable());
        
        // 转换 metadata：如果传了metadata就使用传入的，如果没传就使用数据库原有的
        Map<String, String> metadataMap = null;
        // 查找匹配的 detail，看是否有传入的 metadata
        if (details != null) {
            QueryDetailDetailVo matchedDetail = details.stream()
                .filter(detail -> matchDimensions(ledger, detail))
                .findFirst()
                .orElse(null);
            if (matchedDetail != null && matchedDetail.getMetadata() != null && !matchedDetail.getMetadata().isEmpty()) {
                metadataMap = matchedDetail.getMetadata();
            }
        }
        
        // 如果没传 metadata，则使用数据库原有的
        if (metadataMap == null) {
            if (StringUtils.isNotBlank(ledger.getMetadata())) {
                try {
                    metadataMap = objectMapper.readValue(
                        ledger.getMetadata(),
                        new TypeReference<Map<String, String>>() {}
                    );
                } catch (Exception e) {
                    log.warn("解析 metadata JSON 失败: {}", ledger.getMetadata(), e);
                    metadataMap = new HashMap<>();
                }
            } else {
                metadataMap = new HashMap<>();
            }
        }
        vo.setMetadata(metadataMap);
        
        return vo;
    }

    /**
     * 转换 BudgetLedger 为 ContractQueryRespDetailVo
     */
    private ContractQueryRespDetailVo convertToContractDetailVo(BudgetLedger ledger, List<QueryDetailDetailVo> details,
                                                                Map<String, String> ehrCdToEhrNmMap,
                                                                Map<String, String> erpAcctCdToErpAcctNmMap) {
        ContractQueryRespDetailVo vo = new ContractQueryRespDetailVo();
        vo.setContractYear(ledger.getYear());
        vo.setContractMonth(ledger.getMonth());
        vo.setManagementOrg(ledger.getMorgCode());
        
        // 设置管理组织名称
        String managementOrg = ledger.getMorgCode();
        if (StringUtils.isNotBlank(managementOrg) && ehrCdToEhrNmMap != null) {
            String managementOrgName = ehrCdToEhrNmMap.get(managementOrg);
            vo.setManagementOrgName(managementOrgName);
        }
        
        vo.setBudgetSubjectCode(ledger.getBudgetSubjectCode());
        
        // 设置预算科目名称
        String budgetSubjectCode = ledger.getBudgetSubjectCode();
        if (StringUtils.isNotBlank(budgetSubjectCode) && erpAcctCdToErpAcctNmMap != null) {
            String budgetSubjectName = erpAcctCdToErpAcctNmMap.get(budgetSubjectCode);
            vo.setBudgetSubjectName(budgetSubjectName);
        }
        
        vo.setMasterProjectCode(ledger.getMasterProjectCode());
        vo.setErpAssetType(ledger.getErpAssetType());
        vo.setIsInternal(ledger.getIsInternal());
        vo.setCurrency(ledger.getCurrency());
        vo.setAmountAvailable(ledger.getAmountAvailable());
        
        // 转换 metadata：如果传了metadata就使用传入的，如果没传就使用数据库原有的
        Map<String, String> metadataMap = null;
        // 查找匹配的 detail，看是否有传入的 metadata
        if (details != null) {
            QueryDetailDetailVo matchedDetail = details.stream()
                .filter(detail -> matchDimensions(ledger, detail))
                .findFirst()
                .orElse(null);
            if (matchedDetail != null && matchedDetail.getMetadata() != null && !matchedDetail.getMetadata().isEmpty()) {
                metadataMap = matchedDetail.getMetadata();
            }
        }
        
        // 如果没传 metadata，则使用数据库原有的
        if (metadataMap == null) {
            if (StringUtils.isNotBlank(ledger.getMetadata())) {
                try {
                    metadataMap = objectMapper.readValue(
                        ledger.getMetadata(),
                        new TypeReference<Map<String, String>>() {}
                    );
                } catch (Exception e) {
                    log.warn("解析 metadata JSON 失败: {}", ledger.getMetadata(), e);
                    metadataMap = new HashMap<>();
                }
            } else {
                metadataMap = new HashMap<>();
            }
        }
        vo.setMetadata(metadataMap);
        
        return vo;
    }

    /**
     * 转换 BudgetLedger 为 ClaimQueryRespDetailVo
     */
    private ClaimQueryRespDetailVo convertToClaimDetailVo(BudgetLedger ledger, List<QueryDetailDetailVo> details,
                                                          Map<String, String> ehrCdToEhrNmMap,
                                                          Map<String, String> erpAcctCdToErpAcctNmMap) {
        ClaimQueryRespDetailVo vo = new ClaimQueryRespDetailVo();
        vo.setClaimYear(ledger.getYear());
        vo.setClaimMonth(ledger.getMonth());
        vo.setActualYear(ledger.getActualYear());
        vo.setActualMonth(ledger.getActualMonth());
        vo.setManagementOrg(ledger.getMorgCode());
        
        // 设置管理组织名称
        String managementOrg = ledger.getMorgCode();
        if (StringUtils.isNotBlank(managementOrg) && ehrCdToEhrNmMap != null) {
            String managementOrgName = ehrCdToEhrNmMap.get(managementOrg);
            vo.setManagementOrgName(managementOrgName);
        }
        
        vo.setBudgetSubjectCode(ledger.getBudgetSubjectCode());
        
        // 设置预算科目名称
        String budgetSubjectCode = ledger.getBudgetSubjectCode();
        if (StringUtils.isNotBlank(budgetSubjectCode) && erpAcctCdToErpAcctNmMap != null) {
            String budgetSubjectName = erpAcctCdToErpAcctNmMap.get(budgetSubjectCode);
            vo.setBudgetSubjectName(budgetSubjectName);
        }
        
        vo.setMasterProjectCode(ledger.getMasterProjectCode());
        vo.setErpAssetType(ledger.getErpAssetType());
        vo.setIsInternal(ledger.getIsInternal());
        vo.setCurrency(ledger.getCurrency());
        vo.setAmountAvailable(ledger.getAmountAvailable());
        
        // 转换 metadata：如果传了metadata就使用传入的，如果没传就使用数据库原有的
        Map<String, String> metadataMap = null;
        // 查找匹配的 detail，看是否有传入的 metadata
        if (details != null) {
            QueryDetailDetailVo matchedDetail = details.stream()
                .filter(detail -> matchDimensions(ledger, detail))
                .findFirst()
                .orElse(null);
            if (matchedDetail != null && matchedDetail.getMetadata() != null && !matchedDetail.getMetadata().isEmpty()) {
                metadataMap = matchedDetail.getMetadata();
            }
        }
        
        // 如果没传 metadata，则使用数据库原有的
        if (metadataMap == null) {
            if (StringUtils.isNotBlank(ledger.getMetadata())) {
                try {
                    metadataMap = objectMapper.readValue(
                        ledger.getMetadata(),
                        new TypeReference<Map<String, String>>() {}
                    );
                } catch (Exception e) {
                    log.warn("解析 metadata JSON 失败: {}", ledger.getMetadata(), e);
                    metadataMap = new HashMap<>();
                }
            } else {
                metadataMap = new HashMap<>();
            }
        }
        vo.setMetadata(metadataMap);
        
        return vo;
    }

    /**
     * 将月份转换为季度
     */
    private String convertMonthToQuarter(String month) {
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
     * 获取从 q1 到指定季度的所有季度列表
     * 例如：q1 -> [q1], q3 -> [q1, q2, q3]
     */
    private List<String> getQuartersUpTo(String currentQuarter) {
        List<String> quarters = new ArrayList<>();
        if ("q1".equals(currentQuarter)) {
            quarters.add("q1");
        } else if ("q2".equals(currentQuarter)) {
            quarters.add("q1");
            quarters.add("q2");
        } else if ("q3".equals(currentQuarter)) {
            quarters.add("q1");
            quarters.add("q2");
            quarters.add("q3");
        } else if ("q4".equals(currentQuarter)) {
            quarters.add("q1");
            quarters.add("q2");
            quarters.add("q3");
            quarters.add("q4");
        }
        return quarters;
    }

    /**
     * 申请单维度下某季度的「操作金额」：取该季度所有 balance 的 amountFrozen 之和（与 BudgetApplicationServiceImpl.getCurrentAmountOperated 一致）。
     * 用于预算查询累计可用余额计算，与申请校验逻辑对齐。
     */
    private BigDecimal getApplyQuarterAmountOperated(List<BudgetBalance> balanceList) {
        if (CollectionUtils.isEmpty(balanceList)) {
            return BigDecimal.ZERO;
        }
        BigDecimal total = BigDecimal.ZERO;
        for (BudgetBalance balance : balanceList) {
            BigDecimal amountFrozen = balance.getAmountFrozen() == null ? BigDecimal.ZERO : balance.getAmountFrozen();
            total = total.add(amountFrozen);
        }
        return total;
    }

    /**
     * 合同单维度下某季度的「操作金额」：取该季度所有 balance 的 amountOccupied 之和（与 BudgetContractServiceImpl.getCurrentAmountOperated 一致）。
     * 用于预算查询累计可用余额计算，与合同校验逻辑对齐。
     */
    private BigDecimal getContractQuarterAmountOperated(List<BudgetBalance> balanceList) {
        if (CollectionUtils.isEmpty(balanceList)) {
            return BigDecimal.ZERO;
        }
        BigDecimal total = BigDecimal.ZERO;
        for (BudgetBalance balance : balanceList) {
            BigDecimal amountOccupied = balance.getAmountOccupied() == null ? BigDecimal.ZERO : balance.getAmountOccupied();
            total = total.add(amountOccupied);
        }
        return total;
    }

    /**
     * 计算 isInternal 值
     * 如果 masterProjectCode 为空或空字符串，代表部门预算，isInternal 默认为 "1"
     * 
     * @param detail 查询明细
     * @return isInternal 字符串值（"1" 或 "0"）
     */
    private String calculateIsInternal(QueryDetailDetailVo detail) {
        // 如果项目编码为空或空字符串，代表部门预算，isInternal 默认为 1
        if (StringUtils.isBlank(detail.getMasterProjectCode())) {
            return "1";
        }
        // 否则使用传入的 isInternal 值（已经是字符串类型："1" 或 "0"）
        return StringUtils.isNotBlank(detail.getIsInternal()) ? detail.getIsInternal() : "0";
    }


    @Override
    public BudgetQueryRelationsVo queryOrgRelations(BudgetQueryOrgRelationsParams relationsParams) {
        // 从视图查询（视图已实现向上追溯逻辑，性能优化）
        List<EhrOrgManageHierarchyView> viewList = ehrOrgManageHierarchyViewMapper.selectList(null);
        
        // 转换为EhrDetailResultVo（字段完全匹配，直接复制属性）
        List<EhrDetailResultVo> detailResultVos = viewList.stream().map(view -> {
            EhrDetailResultVo vo = new EhrDetailResultVo();
            BeanUtils.copyProperties(view, vo);
            return vo;
        }).collect(Collectors.toList());
        
        BudgetOrgResultVo resultVo = BudgetOrgResultVo.builder()
                .orgDetails(detailResultVos)
                .build();

        ESBRespInfoVo esbRespInfoVo = ESBRespInfoVo.builder()
                .instId(relationsParams.getEsbInfo().getInstId())
                .requestTime(relationsParams.getEsbInfo().getRequestTime())
                .returnCode("E0001-QueryOrgRelations")
                .returnMsg("查询组织关系成功")
                .returnStatus("S")
                .responseTime(DateUtils.format(new Date(), "yyyy-MM-dd HH:mm:ss.SSS"))
                .build();

       return BudgetQueryRelationsVo.builder()
                .orgResult(resultVo)
                .esbInfo(esbRespInfoVo)
                .build();
    }

    /**
     * 根据调整类型查询预算余额
     * effectType=0 或 1：查询所有季度 Balance 的 amountAvailable 之和
     * effectType=2：查询所有季度 Balance 的 amountPayAvailable 之和
     */
    private AdjustQueryResultVo queryByAdjustType(String adjustType, List<QueryDetailDetailVo> details) {
        AdjustQueryResultVo result = new AdjustQueryResultVo();
        result.setAdjustOrderNo(null); // 按调整类型查询没有单号
        result.setDocumentName("预算调整查询");
        result.setDataSource("SYSTEM");
        result.setDocumentStatus(null);
        
        // 批量提取 managementOrg 字段（对应 EHR_CD）
        Set<String> managementOrgSet = details.stream()
                .map(QueryDetailDetailVo::getManagementOrg)
                .filter(StringUtils::isNotBlank)
                .collect(Collectors.toSet());
        
        // 批量查询 EHR_ORG_MANAGE_R 表，获取 EHR_CD 对应的 ORG_CD（一一对应关系）
        BudgetQueryHelperService.EhrCdToOrgCdMapResult ehrCdToOrgCdMapResult = budgetQueryHelperService.queryEhrCdToOrgCdOneToOneMap(managementOrgSet);
        Map<String, String> ehrCdToOrgCdMap = ehrCdToOrgCdMapResult.getEhrCdToOrgCdMap();
        Map<String, String> ehrCdToEhrNmMap = ehrCdToOrgCdMapResult.getEhrCdToEhrNmMap();
        
        // 批量查询 EHR_ORG_MANAGE_EXT_R 表，获取 EHR_CD 对应的所有 ORG_CD（一对多关系）
        Map<String, List<String>> ehrCdToOrgCdExtMap = budgetQueryHelperService.queryEhrCdToOrgCdMap(managementOrgSet);
        
        // 批量提取 budgetSubjectCode 字段
        Set<String> budgetSubjectCodeSet = details.stream()
                .map(QueryDetailDetailVo::getBudgetSubjectCode)
                .filter(StringUtils::isNotBlank)
                .filter(code -> !"NAN-NAN".equals(code))
                .collect(Collectors.toSet());
        
        // 批量查询 SUBJECT_INFO 表，获取 ERP_ACCT_CD 对应的 ACCT_CD（一一对应关系）
        BudgetQueryHelperService.ErpAcctCdToAcctCdMapResult erpAcctCdToAcctCdMapResult = budgetQueryHelperService.queryErpAcctCdToAcctCdOneToOneMap(budgetSubjectCodeSet);
        Map<String, String> erpAcctCdToAcctCdMap = erpAcctCdToAcctCdMapResult.getErpAcctCdToAcctCdMap();
        Map<String, String> erpAcctCdToErpAcctNmMap = erpAcctCdToAcctCdMapResult.getErpAcctCdToErpAcctNmMap();
        
        // 批量查询 SUBJECT_EXT_INFO 表，获取 ERP_ACCT_CD 对应的所有 ACCT_CD（一对多关系）
        Map<String, List<String>> erpAcctCdToAcctCdExtMap = budgetQueryHelperService.queryErpAcctCdToAcctCdMap(budgetSubjectCodeSet);
        
        // 检查 erpAcctCdToAcctCdExtMap 中是否有任何 value（List）包含 "NAN-NAN"
        // 如果包含，说明没有映射到且是白名单，原样返回入参
        if (erpAcctCdToAcctCdExtMap != null && !erpAcctCdToAcctCdExtMap.isEmpty()) {
            for (Map.Entry<String, List<String>> entry : erpAcctCdToAcctCdExtMap.entrySet()) {
                List<String> acctCdList = entry.getValue();
                if (acctCdList != null && acctCdList.contains("NAN-NAN")) {
                    log.info("检测到 erpAcctCdToAcctCdExtMap 中包含 NAN-NAN，说明是白名单且没有映射，原样返回入参");
                    // 将入参的 details 转换为 AdjustQueryResultDetailVo
                    List<AdjustQueryResultDetailVo> resultDetails = new ArrayList<>();
                    for (QueryDetailDetailVo detail : details) {
                        AdjustQueryResultDetailVo vo = new AdjustQueryResultDetailVo();
                        vo.setAdjustYear(detail.getQueryYear());
                        vo.setAdjustMonth(detail.getQueryMonth());
                        vo.setCompany(detail.getCompany());
                        vo.setDepartment(detail.getDepartment());
                        vo.setManagementOrg(detail.getManagementOrg());
                        vo.setBudgetSubjectCode(detail.getBudgetSubjectCode());
                        vo.setMasterProjectCode(detail.getMasterProjectCode());
                        vo.setErpAssetType(detail.getErpAssetType());
                        vo.setIsInternal(detail.getIsInternal());
                        vo.setCurrency(detail.getCurrency());
                        vo.setAmountAvailable(BigDecimal.ZERO);
                        if (detail.getMetadata() != null && !detail.getMetadata().isEmpty()) {
                            vo.setMetadata(new HashMap<>(detail.getMetadata()));
                        } else {
                            vo.setMetadata(new HashMap<>());
                        }
                        resultDetails.add(vo);
                    }
                    result.setAdjustDetails(resultDetails);
                    return result;
                }
            }
        }
        
        // 批量提取 masterProjectCode 字段
        Set<String> masterProjectCodeSet = details.stream()
                .map(QueryDetailDetailVo::getMasterProjectCode)
                .filter(StringUtils::isNotBlank)
                .filter(code -> !"NAN".equals(code))
                .collect(Collectors.toSet());
        
        // 批量查询 PROJECT_CONTROL_EXT_R 表，获取 PRJ_CD 对应的所有 RELATED_PRJ_CD（一对多关系）
        Map<String, List<String>> prjCdToRelatedPrjCdExtMap = budgetQueryHelperService.queryPrjCdToRelatedPrjCdMap(masterProjectCodeSet);
        
        // 为每个 detail 构建临时的 BudgetLedger 对象，用于调用 queryQuotaAndBalanceByAllQuartersAllDem
        Map<String, BudgetLedger> tempLedgerMap = new HashMap<>();
        Map<String, QueryDetailDetailVo> bizKeyToDetailMap = new HashMap<>();
        
        for (QueryDetailDetailVo detail : details) {
            String isInternal = calculateIsInternal(detail);
            
            // 获取 effectType：优先使用 detail.getEffectType()，如果没有则从 metadata 中获取，最后使用传入的 adjustType
            String effectType = adjustType;
            if (StringUtils.isNotBlank(detail.getEffectType())) {
                effectType = detail.getEffectType();
            } else if (detail.getMetadata() != null && detail.getMetadata().containsKey("effectType")) {
                effectType = detail.getMetadata().get("effectType");
            }
            
            // 构建 bizItemCode，格式: isInternal@managementOrg@budgetSubjectCode@masterProjectCode@erpAssetType@effectType
            // 注意：需要包含 effectType，因为不同的 effectType 需要查询不同的金额字段
            String bizItemCode = isInternal + "@" + 
                detail.getManagementOrg() + "@" + 
                detail.getBudgetSubjectCode() + "@" + 
                detail.getMasterProjectCode() + "@" + 
                detail.getErpAssetType() + "@" +
                (effectType != null ? effectType : "");
            
            // 构建唯一的 bizCode
            String bizCode = "QUERY";
            
            // 构建 bizKey（格式：bizCode + "@" + bizItemCode）
            String bizKey = bizCode + "@" + bizItemCode;
            
            // 创建临时的 BudgetLedger 对象
            BudgetLedger tempLedger = BudgetLedger.builder()
                    .bizType("ADJUST")
                    .bizCode(bizCode)
                    .bizItemCode(bizItemCode)
                    .year(detail.getQueryYear())
                    .month(detail.getQueryMonth())
                    .morgCode(detail.getManagementOrg()) // EHR_CD
                    .budgetSubjectCode(detail.getBudgetSubjectCode()) // ERP_ACCT_CD
                    .masterProjectCode(detail.getMasterProjectCode())
                    .erpAssetType(detail.getErpAssetType())
                    .isInternal(isInternal)
                    .currency(detail.getCurrency())
                    .build();
            
            tempLedgerMap.put(bizKey, tempLedger);
            bizKeyToDetailMap.put(bizKey, detail);
        }
        
        // 批量提取需要映射的 erpAssetType 字段（以 "1" 或 "M" 开头且不带项目的）
        Set<String> erpAssetTypeSet = details.stream()
                .filter(detail -> {
                    String masterProjectCode = detail.getMasterProjectCode();
                    // 只提取不带项目的明细的 erpAssetType
                    return "NAN".equals(masterProjectCode) || StringUtils.isBlank(masterProjectCode);
                })
                .map(QueryDetailDetailVo::getErpAssetType)
                .filter(StringUtils::isNotBlank)
                .filter(code -> !"NAN".equals(code))
                .filter(code -> code.startsWith("1") || code.startsWith("M")) // 只提取需要映射的
                .collect(Collectors.toSet());
        
        // 批量查询 VIEW_BUDGET_MEMBER_NAME_CODE 视图，获取 MEMBER_CD2 对应的 MEMBER_CD（一一对应关系）
        Map<String, String> erpAssetTypeToMemberCdMap = budgetQueryHelperService.queryErpAssetTypeToMemberCdMap(erpAssetTypeSet);
        log.info("========== 获取到 erpAssetType 映射表，共 {} 条记录 ==========", erpAssetTypeToMemberCdMap.size());
        
        // 先调用 queryQuotaAndBalanceByAllQuarters 查询所有季度的 quota 和 balance（一对一映射）
        BudgetQueryHelperService.BudgetQuotaBalanceResult firstQueryResult = 
                budgetQueryHelperService.queryQuotaAndBalanceByAllQuarters(
                        tempLedgerMap,
                        ehrCdToOrgCdMap,
                        erpAcctCdToAcctCdMap,
                        erpAssetTypeToMemberCdMap);
        
        // 获取返回的 quotaMap 和 balanceMap，作为 needToUpdateSameDemBudgetQuotaMap 和 needToUpdateSameDemBudgetBalanceMap
        Map<String, BudgetQuota> needToUpdateSameDemBudgetQuotaMap = firstQueryResult.getQuotaMap();
        Map<String, BudgetBalance> needToUpdateSameDemBudgetBalanceMap = firstQueryResult.getBalanceMap();
        
        // 调用 queryQuotaAndBalanceByAllQuartersAllDem 查询所有季度的 balance（支持扩展映射）
        BudgetQueryHelperService.BudgetQuotaBalanceSimpleResult queryResult = 
                budgetQueryHelperService.queryQuotaAndBalanceByAllQuartersAllDem(
                        tempLedgerMap,
                        needToUpdateSameDemBudgetQuotaMap,
                        needToUpdateSameDemBudgetBalanceMap,
                        ehrCdToOrgCdMap,
                        ehrCdToOrgCdExtMap,
                        erpAcctCdToAcctCdMap,
                        erpAcctCdToAcctCdExtMap,
                        prjCdToRelatedPrjCdExtMap,
                        erpAssetTypeToMemberCdMap);
        
        // 获取返回的 balanceMap
        // key 格式：bizCode + "@" + bizItemCode + "@" + quarter
        Map<String, List<BudgetBalance>> balanceMap = queryResult.getBalanceMap();
        
        // 组装结果
        List<AdjustQueryResultDetailVo> resultDetails = new ArrayList<>();
        String[] quarters = {"q1", "q2", "q3", "q4"};
        
        for (Map.Entry<String, QueryDetailDetailVo> entry : bizKeyToDetailMap.entrySet()) {
            String bizKey = entry.getKey();
            QueryDetailDetailVo detail = entry.getValue();
            
            // 优先使用 detail.getEffectType()，如果没有则从 metadata 中获取，最后使用传入的 adjustType
            String effectType = adjustType;
            if (StringUtils.isNotBlank(detail.getEffectType())) {
                effectType = detail.getEffectType();
            } else if (detail.getMetadata() != null && detail.getMetadata().containsKey("effectType")) {
                effectType = detail.getMetadata().get("effectType");
            }
            
            // 获取 metadata 中的 mxid，用于日志标识
            String mxid = detail.getMetadata() != null ? detail.getMetadata().get("mxid") : null;
            
            log.info("========== 调整查询 - 明细 effectType: mxid={}, bizKey={}, detail.getEffectType()={}, metadata.effectType={}, 最终effectType={} ==========",
                    mxid, bizKey, detail.getEffectType(), 
                    detail.getMetadata() != null ? detail.getMetadata().get("effectType") : null, 
                    effectType);
            
            BudgetLedger tempLedger = tempLedgerMap.get(bizKey);
            String bizCode = tempLedger.getBizCode();
            String bizItemCode = tempLedger.getBizItemCode();
            
            log.info("========== 调整查询 - 开始累加余额: mxid={}, bizKey={}, bizCode={}, bizItemCode={}, effectType={} ==========",
                    mxid, bizKey, bizCode, bizItemCode, effectType);
            
            // 累加所有季度的 balance 的可用余额
            BigDecimal totalAmount = BigDecimal.ZERO;
            
            // 打印 balanceMap 的所有 key，用于调试
            log.info("========== 调整查询 - balanceMap的所有key: mxid={}, balanceMap大小={}, keys={} ==========",
                    mxid, balanceMap.size(), balanceMap.keySet());
            
            for (String quarter : quarters) {
                String bizKeyQuarter = bizCode + "@" + bizItemCode + "@" + quarter;
                List<BudgetBalance> balanceList = balanceMap.get(bizKeyQuarter);
                log.info("========== 调整查询 - 查询季度余额: mxid={}, quarter={}, bizKeyQuarter={}, balanceList是否为空={}, balanceList大小={} ==========",
                        mxid, quarter, bizKeyQuarter, CollectionUtils.isEmpty(balanceList), 
                        balanceList != null ? balanceList.size() : 0);
                
                // 如果查询不到，尝试打印所有可能的 key 进行匹配
                if (CollectionUtils.isEmpty(balanceList)) {
                    log.warn("========== 调整查询 - 未找到balance数据，尝试匹配: mxid={}, quarter={}, bizKeyQuarter={}, 所有可能的key={} ==========",
                            mxid, quarter, bizKeyQuarter, 
                            balanceMap.keySet().stream()
                                    .filter(key -> key.contains(bizItemCode) && key.contains(quarter))
                                    .collect(java.util.stream.Collectors.toList()));
                }
                
                if (!CollectionUtils.isEmpty(balanceList)) {
                    for (BudgetBalance balance : balanceList) {
                        if (balance != null) {
                            // effectType=0 或 1：查询 amountAvailable
                            // effectType=2：查询 amountPayAvailable
                            if ("2".equals(effectType)) {
                                BigDecimal amountPayAvailable = balance.getAmountPayAvailable() == null ? BigDecimal.ZERO : balance.getAmountPayAvailable();
                                BigDecimal amountAvailable = balance.getAmountAvailable() == null ? BigDecimal.ZERO : balance.getAmountAvailable();
                                totalAmount = totalAmount.add(amountPayAvailable);
                                log.info("========== 调整查询 - effectType=2 累加: mxid={}, quarter={}, poolId={}, amountPayAvailable={}, amountAvailable={}, 累加后totalAmount={} ==========",
                                        mxid, quarter, balance.getPoolId(), amountPayAvailable, amountAvailable, totalAmount);
                            } else {
                                // effectType=0 或 1：优先使用 amountAvailable，如果为 null（带项目且没有采购额），使用 amountPayAvailable
                                BigDecimal amountAvailable = balance.getAmountAvailable();
                                BigDecimal amountPayAvailable = balance.getAmountPayAvailable();
                                if (amountAvailable != null) {
                                    totalAmount = totalAmount.add(amountAvailable);
                                    log.info("========== 调整查询 - effectType={} 累加amountAvailable: mxid={}, quarter={}, poolId={}, amountAvailable={}, 累加后totalAmount={} ==========",
                                            effectType, mxid, quarter, balance.getPoolId(), amountAvailable, totalAmount);
                                } else if (amountPayAvailable != null) {
                                    totalAmount = totalAmount.add(amountPayAvailable);
                                    log.info("========== 调整查询 - effectType={} 累加amountPayAvailable: mxid={}, quarter={}, poolId={}, amountPayAvailable={}, 累加后totalAmount={} ==========",
                                            effectType, mxid, quarter, balance.getPoolId(), amountPayAvailable, totalAmount);
                                } else {
                                    log.info("========== 调整查询 - effectType={} balance的amountAvailable和amountPayAvailable都为null: mxid={}, quarter={}, poolId={} ==========",
                                            effectType, mxid, quarter, balance.getPoolId());
                                }
                            }
                        }
                    }
                } else {
                    log.info("========== 调整查询 - 季度无balance数据: mxid={}, quarter={}, bizKeyQuarter={} ==========",
                            mxid, quarter, bizKeyQuarter);
                }
            }
            
            log.info("========== 调整查询 - 最终结果: mxid={}, bizKey={}, effectType={}, totalAmount={}, totalAmount是否为null={} ==========",
                    mxid, bizKey, effectType, totalAmount, totalAmount == null);
            
            AdjustQueryResultDetailVo vo = new AdjustQueryResultDetailVo();
            vo.setAdjustYear(detail.getQueryYear());
            vo.setAdjustMonth(detail.getQueryMonth());
            vo.setCompany(detail.getCompany());
            vo.setDepartment(detail.getDepartment());
            vo.setManagementOrg(detail.getManagementOrg());
            
            // 设置管理组织名称
            String managementOrg = detail.getManagementOrg();
            if (StringUtils.isNotBlank(managementOrg) && ehrCdToEhrNmMap != null) {
                String managementOrgName = ehrCdToEhrNmMap.get(managementOrg);
                vo.setManagementOrgName(managementOrgName);
            }
            
            vo.setBudgetSubjectCode(detail.getBudgetSubjectCode());
            
            // 设置预算科目名称
            String budgetSubjectCode = detail.getBudgetSubjectCode();
            if (StringUtils.isNotBlank(budgetSubjectCode) && erpAcctCdToErpAcctNmMap != null) {
                String budgetSubjectName = erpAcctCdToErpAcctNmMap.get(budgetSubjectCode);
                vo.setBudgetSubjectName(budgetSubjectName);
            }
            
            vo.setMasterProjectCode(detail.getMasterProjectCode());
            vo.setErpAssetType(detail.getErpAssetType());
            vo.setIsInternal(detail.getIsInternal());
            vo.setCurrency(detail.getCurrency());
            
            log.info("========== 调整查询 - 设置effectType前: mxid={}, effectType={}, effectType是否为null={} ==========",
                    mxid, effectType, effectType == null);
            
            vo.setEffectType(effectType); // 设置 effectType，用于后续的维度匹配
            
            log.info("========== 调整查询 - 设置effectType后: mxid={}, vo.getEffectType()={}, vo.getEffectType()是否为null={} ==========",
                    mxid, vo.getEffectType(), vo.getEffectType() == null);
            
            log.info("========== 调整查询 - 设置amountAvailable前: mxid={}, totalAmount={}, totalAmount是否为null={} ==========",
                    mxid, totalAmount, totalAmount == null);
            
            vo.setAmountAvailable(totalAmount);
            
            log.info("========== 调整查询 - 设置amountAvailable后: mxid={}, vo.getAmountAvailable()={}, vo.getAmountAvailable()是否为null={} ==========",
                    mxid, vo.getAmountAvailable(), vo.getAmountAvailable() == null);
            
            // 设置 metadata：使用传入的 metadata
            if (detail.getMetadata() != null && !detail.getMetadata().isEmpty()) {
                vo.setMetadata(detail.getMetadata());
            } else {
                vo.setMetadata(new HashMap<>());
            }
            
            log.info("========== 调整查询 - 明细组装完成: mxid={}, vo.getAmountAvailable()={}, vo.getEffectType()={} ==========",
                    mxid, vo.getAmountAvailable(), vo.getEffectType());
            
            resultDetails.add(vo);
        }
        
        result.setAdjustDetails(resultDetails);
        return result;
    }
}
