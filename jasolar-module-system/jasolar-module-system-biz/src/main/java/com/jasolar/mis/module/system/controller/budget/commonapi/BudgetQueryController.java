package com.jasolar.mis.module.system.controller.budget.commonapi;

import com.jasolar.mis.module.system.controller.budget.validation.DetailMerger;
import com.jasolar.mis.module.system.controller.budget.validation.SubjectCodeValidator;
import com.jasolar.mis.module.system.controller.budget.validation.BudgetResponseBuilder;
import com.jasolar.mis.module.system.controller.budget.vo.*;
import com.jasolar.mis.module.system.service.budget.query.BudgetQueryService;
import com.jasolar.mis.module.system.config.BudgetSubjectCodeConfig;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Description: 预算查询控制器
 * Author : Auto Generated
 * Date : 2025-01-XX
 * Version : 1.0
 */
@RestController
@RequestMapping(value = "/budget/query")
@Slf4j
@Api(tags = "预算查询")
public class BudgetQueryController {

    @Resource
    private BudgetQueryService budgetQueryService;
    
    @Resource
    private SubjectCodeValidator subjectCodeValidator;
    
    @Resource
    private BudgetSubjectCodeConfig budgetSubjectCodeConfig;

    /**
     * 预算查询操作
     * 
     * @param budgetQueryParams 预算查询参数
     * @return 预算查询响应
     */
    @PostMapping("/query")
    @ApiOperation("预算查询")
    public BudgetQueryRespVo query(@RequestBody @Valid BudgetQueryParams budgetQueryParams) {
        // 性能优化：减少日志输出，只在DEBUG级别输出
        if (log.isDebugEnabled()) {
            log.debug("开始处理预算查询，params={}", budgetQueryParams);
        }
        
        // 合并相同维度的明细
        if (budgetQueryParams.getQueryReqInfoParams() != null 
            && budgetQueryParams.getQueryReqInfoParams().getDetails() != null) {
            
            // 保存原始明细列表（合并前），用于返回时映射
            List<QueryDetailDetailVo> originalDetails = 
                new java.util.ArrayList<>(budgetQueryParams.getQueryReqInfoParams().getDetails());
            
            // OA 和 HLY 都执行合并逻辑（统一处理）
            String adjustType = budgetQueryParams.getQueryReqInfoParams().getAdjustType();
            DetailMerger.MergeResult<QueryDetailDetailVo> mergeResult = 
                DetailMerger.mergeQueryDetails(
                    budgetQueryParams.getQueryReqInfoParams().getDetails(),
                    adjustType
                );
            
            // 构建映射关系：原始明细索引 -> 合并后明细索引
            java.util.Map<Integer, Integer> originalToMergedIndexMap = new java.util.HashMap<>();
            if (mergeResult.hasMerged()) {
                // 如果发生了合并，需要构建映射关系
                List<QueryDetailDetailVo> mergedDetails = mergeResult.getMergedDetails();
                
                // 使用维度key来匹配原始明细和合并后明细
                java.util.Map<String, Integer> dimensionKeyToMergedIndex = new java.util.HashMap<>();
                for (int i = 0; i < mergedDetails.size(); i++) {
                    QueryDetailDetailVo mergedDetail = mergedDetails.get(i);
                    String dimensionKey = buildDimensionKey(mergedDetail, adjustType);
                    dimensionKeyToMergedIndex.put(dimensionKey, i);
                }
                
                // 为每个原始明细找到对应的合并后明细索引
                for (int i = 0; i < originalDetails.size(); i++) {
                    QueryDetailDetailVo originalDetail = originalDetails.get(i);
                    String dimensionKey = buildDimensionKey(originalDetail, adjustType);
                    Integer mergedIndex = dimensionKeyToMergedIndex.get(dimensionKey);
                    if (mergedIndex != null) {
                        originalToMergedIndexMap.put(i, mergedIndex);
                    }
                }
                
                // 更新明细列表为合并后的明细
                budgetQueryParams.getQueryReqInfoParams().setDetails(mergedDetails);
            } else {
                // 没有合并，原始索引和合并后索引一一对应
                for (int i = 0; i < originalDetails.size(); i++) {
                    originalToMergedIndexMap.put(i, i);
                }
            }
            
            // 过滤出白名单中的明细，只对这些明细进行预算校验处理
            List<QueryDetailDetailVo> mergedDetails = 
                budgetQueryParams.getQueryReqInfoParams().getDetails();
            List<QueryDetailDetailVo> whitelistDetails = 
                subjectCodeValidator.filterWhitelistQueryDetails(mergedDetails);
            
            if (whitelistDetails.isEmpty()) {
                // 如果所有明细都不在白名单中，根据 dataSource 决定返回逻辑
                String dataSource = budgetQueryParams.getQueryReqInfoParams().getDataSource();
                if ("OA".equalsIgnoreCase(dataSource)) {
                    // OA：返回原始明细（保持原始条数）
                    log.info("所有明细都不在白名单中，OA返回原始明细");
                    BudgetQueryParams originalParams = new BudgetQueryParams();
                    org.springframework.beans.BeanUtils.copyProperties(budgetQueryParams, originalParams);
                    if (originalParams.getQueryReqInfoParams() != null) {
                        originalParams.getQueryReqInfoParams().setDetails(originalDetails);
                    }
                    return BudgetResponseBuilder.buildQueryResponse(originalParams);
                } else {
                    // HLY：返回合并后的明细（保持原有逻辑）
                    log.info("所有明细都不在白名单中，HLY返回合并后的明细");
                return BudgetResponseBuilder.buildQueryResponse(budgetQueryParams);
                }
            }
            
            // 创建临时参数对象，只包含白名单中的明细进行预算校验
            BudgetQueryParams tempParams = new BudgetQueryParams();
            org.springframework.beans.BeanUtils.copyProperties(budgetQueryParams, tempParams);
            if (tempParams.getQueryReqInfoParams() != null) {
                tempParams.getQueryReqInfoParams().setDetails(whitelistDetails);
            }
            
            // 使用临时参数进行预算查询处理（合并后的明细）
            BudgetQueryRespVo result = budgetQueryService.query(tempParams);
            
            // 根据 dataSource 决定返回逻辑
            String dataSource = budgetQueryParams.getQueryReqInfoParams().getDataSource();
            if ("OA".equalsIgnoreCase(dataSource)) {
                // OA：返回时映射回原始明细（保持原始条数）
                if (result != null && result.getQueryResult() != null) {
                    // 根据不同的查询结果类型进行映射
                    mapQueryResultToOriginalDetails(result, originalDetails, mergedDetails, originalToMergedIndexMap, adjustType);
                }
            }
            // HLY：直接返回合并后的结果（保持原有逻辑，不映射）
            
            return result;
        }
        
        return budgetQueryService.query(budgetQueryParams);
    }

    /**
     * 将查询结果映射回原始明细列表（保持原始条数）
     */
    private void mapQueryResultToOriginalDetails(BudgetQueryRespVo result, 
                                                  List<QueryDetailDetailVo> originalDetails,
                                                  List<QueryDetailDetailVo> mergedDetails,
                                                  java.util.Map<Integer, Integer> originalToMergedIndexMap,
                                                  String adjustType) {
        Object queryResult = result.getQueryResult();
        if (queryResult == null) {
            return;
        }
        
        // 根据不同的查询结果类型进行映射
        if (queryResult instanceof ApplyQueryResultVo) {
            ApplyQueryResultVo applyResult = (ApplyQueryResultVo) queryResult;
            List<ApplyQueryRespDetailVo> mergedResultDetails = applyResult.getDemandDetails();
            if (mergedResultDetails != null && !mergedResultDetails.isEmpty()) {
                List<ApplyQueryRespDetailVo> finalDetails = mapApplyQueryDetails(
                    originalDetails, mergedDetails, mergedResultDetails, originalToMergedIndexMap);
                applyResult.setDemandDetails(finalDetails);
            }
        } else if (queryResult instanceof ContractQueryResultVo) {
            ContractQueryResultVo contractResult = (ContractQueryResultVo) queryResult;
            List<ContractQueryRespDetailVo> mergedResultDetails = contractResult.getContractDetails();
            if (mergedResultDetails != null && !mergedResultDetails.isEmpty()) {
                List<ContractQueryRespDetailVo> finalDetails = mapContractQueryDetails(
                    originalDetails, mergedDetails, mergedResultDetails, originalToMergedIndexMap);
                contractResult.setContractDetails(finalDetails);
            }
        } else if (queryResult instanceof ClaimQueryResultVo) {
            ClaimQueryResultVo claimResult = (ClaimQueryResultVo) queryResult;
            List<ClaimQueryRespDetailVo> mergedResultDetails = claimResult.getClaimDetails();
            if (mergedResultDetails != null && !mergedResultDetails.isEmpty()) {
                List<ClaimQueryRespDetailVo> finalDetails = mapClaimQueryDetails(
                    originalDetails, mergedDetails, mergedResultDetails, originalToMergedIndexMap);
                claimResult.setClaimDetails(finalDetails);
            }
        } else if (queryResult instanceof AdjustQueryResultVo) {
            AdjustQueryResultVo adjustResult = (AdjustQueryResultVo) queryResult;
            List<AdjustQueryResultDetailVo> mergedResultDetails = adjustResult.getAdjustDetails();
            if (mergedResultDetails != null && !mergedResultDetails.isEmpty()) {
                List<AdjustQueryResultDetailVo> finalDetails = mapAdjustQueryDetails(
                    originalDetails, mergedDetails, mergedResultDetails, originalToMergedIndexMap, adjustType);
                adjustResult.setAdjustDetails(finalDetails);
            }
        }
    }
    
    /**
     * 映射申请查询明细
     */
    private List<ApplyQueryRespDetailVo> mapApplyQueryDetails(
            List<QueryDetailDetailVo> originalDetails,
            List<QueryDetailDetailVo> mergedDetails,
            List<ApplyQueryRespDetailVo> mergedResultDetails,
            java.util.Map<Integer, Integer> originalToMergedIndexMap) {
        List<ApplyQueryRespDetailVo> finalDetails = new java.util.ArrayList<>();
        
        // 构建 mergedDetails 到 mergedResultDetails 的映射（通过维度 key 匹配）
        // 注意：mergedResultDetails 的顺序可能与 mergedDetails 不一致，需要通过维度 key 匹配
        java.util.Map<String, ApplyQueryRespDetailVo> dimensionKeyToResultMap = new java.util.HashMap<>();
        for (ApplyQueryRespDetailVo mergedResultDetail : mergedResultDetails) {
            // 从 mergedResultDetail 中提取维度信息，构建维度 key
            for (QueryDetailDetailVo mergedDetail : mergedDetails) {
                // 通过维度匹配：managementOrg, budgetSubjectCode, masterProjectCode, erpAssetType, isInternal
                if (isApplyDimensionMatch(mergedDetail, mergedResultDetail)) {
                    String dimensionKey = buildDimensionKey(mergedDetail, null);
                    // 如果维度 key 已存在，说明有多个匹配，使用第一个
                    if (!dimensionKeyToResultMap.containsKey(dimensionKey)) {
                        dimensionKeyToResultMap.put(dimensionKey, mergedResultDetail);
                    }
                    break; // 找到匹配后跳出内层循环
                }
            }
        }
        
        for (int i = 0; i < originalDetails.size(); i++) {
            QueryDetailDetailVo originalDetail = originalDetails.get(i);
            Integer mergedIndex = originalToMergedIndexMap.get(i);
            if (mergedIndex != null && mergedIndex < mergedDetails.size()) {
                QueryDetailDetailVo mergedDetail = mergedDetails.get(mergedIndex);
                String dimensionKey = buildDimensionKey(mergedDetail, null);
                ApplyQueryRespDetailVo mergedResultDetail = dimensionKeyToResultMap.get(dimensionKey);
                
                if (mergedResultDetail != null) {
                    ApplyQueryRespDetailVo originalResultDetail = new ApplyQueryRespDetailVo();
                    org.springframework.beans.BeanUtils.copyProperties(mergedResultDetail, originalResultDetail);
                    // 保留原始明细的基础信息
                    originalResultDetail.setDemandYear(originalDetail.getQueryYear());
                    originalResultDetail.setDemandMonth(originalDetail.getQueryMonth());
                    originalResultDetail.setCompany(originalDetail.getCompany());
                    originalResultDetail.setDepartment(originalDetail.getDepartment());
                    if (originalDetail.getMetadata() != null) {
                        originalResultDetail.setMetadata(new java.util.HashMap<>(originalDetail.getMetadata()));
                    }
                    finalDetails.add(originalResultDetail);
                } else {
                    // 找不到对应的查询结果，创建空结果
                    ApplyQueryRespDetailVo emptyDetail = createEmptyApplyQueryDetail(originalDetail);
                    finalDetails.add(emptyDetail);
                }
            } else {
                ApplyQueryRespDetailVo emptyDetail = createEmptyApplyQueryDetail(originalDetail);
                finalDetails.add(emptyDetail);
            }
        }
        return finalDetails;
    }
    
    /**
     * 映射合同查询明细
     */
    private List<ContractQueryRespDetailVo> mapContractQueryDetails(
            List<QueryDetailDetailVo> originalDetails,
            List<QueryDetailDetailVo> mergedDetails,
            List<ContractQueryRespDetailVo> mergedResultDetails,
            java.util.Map<Integer, Integer> originalToMergedIndexMap) {
        List<ContractQueryRespDetailVo> finalDetails = new java.util.ArrayList<>();
        
        // 构建 mergedDetails 到 mergedResultDetails 的映射（通过维度 key 匹配）
        // 注意：mergedResultDetails 的顺序可能与 mergedDetails 不一致，需要通过维度 key 匹配
        java.util.Map<String, ContractQueryRespDetailVo> dimensionKeyToResultMap = new java.util.HashMap<>();
        for (ContractQueryRespDetailVo mergedResultDetail : mergedResultDetails) {
            // 从 mergedResultDetail 中提取维度信息，构建维度 key
            for (QueryDetailDetailVo mergedDetail : mergedDetails) {
                // 通过维度匹配：managementOrg, budgetSubjectCode, masterProjectCode, erpAssetType, isInternal
                if (isContractDimensionMatch(mergedDetail, mergedResultDetail)) {
                    String dimensionKey = buildDimensionKey(mergedDetail, null);
                    // 如果维度 key 已存在，说明有多个匹配，使用第一个
                    if (!dimensionKeyToResultMap.containsKey(dimensionKey)) {
                        dimensionKeyToResultMap.put(dimensionKey, mergedResultDetail);
                    }
                    break; // 找到匹配后跳出内层循环
                }
            }
        }
        
        for (int i = 0; i < originalDetails.size(); i++) {
            QueryDetailDetailVo originalDetail = originalDetails.get(i);
            Integer mergedIndex = originalToMergedIndexMap.get(i);
            if (mergedIndex != null && mergedIndex < mergedDetails.size()) {
                QueryDetailDetailVo mergedDetail = mergedDetails.get(mergedIndex);
                String dimensionKey = buildDimensionKey(mergedDetail, null);
                ContractQueryRespDetailVo mergedResultDetail = dimensionKeyToResultMap.get(dimensionKey);
                
                if (mergedResultDetail != null) {
                    ContractQueryRespDetailVo originalResultDetail = new ContractQueryRespDetailVo();
                    org.springframework.beans.BeanUtils.copyProperties(mergedResultDetail, originalResultDetail);
                    // 保留原始明细的基础信息
                    originalResultDetail.setContractYear(originalDetail.getQueryYear());
                    originalResultDetail.setContractMonth(originalDetail.getQueryMonth());
                    originalResultDetail.setCompany(originalDetail.getCompany());
                    originalResultDetail.setDepartment(originalDetail.getDepartment());
                    if (originalDetail.getMetadata() != null) {
                        originalResultDetail.setMetadata(new java.util.HashMap<>(originalDetail.getMetadata()));
                    }
                    finalDetails.add(originalResultDetail);
                } else {
                    ContractQueryRespDetailVo emptyDetail = createEmptyContractQueryDetail(originalDetail);
                    finalDetails.add(emptyDetail);
                }
            } else {
                ContractQueryRespDetailVo emptyDetail = createEmptyContractQueryDetail(originalDetail);
                finalDetails.add(emptyDetail);
            }
        }
        return finalDetails;
    }
    
    /**
     * 映射付款/报销查询明细
     */
    private List<ClaimQueryRespDetailVo> mapClaimQueryDetails(
            List<QueryDetailDetailVo> originalDetails,
            List<QueryDetailDetailVo> mergedDetails,
            List<ClaimQueryRespDetailVo> mergedResultDetails,
            java.util.Map<Integer, Integer> originalToMergedIndexMap) {
        List<ClaimQueryRespDetailVo> finalDetails = new java.util.ArrayList<>();
        
        // 构建 mergedDetails 到 mergedResultDetails 的映射（通过维度 key 匹配）
        // 注意：mergedResultDetails 的顺序可能与 mergedDetails 不一致，需要通过维度 key 匹配
        java.util.Map<String, ClaimQueryRespDetailVo> dimensionKeyToResultMap = new java.util.HashMap<>();
        for (ClaimQueryRespDetailVo mergedResultDetail : mergedResultDetails) {
            // 从 mergedResultDetail 中提取维度信息，构建维度 key
            for (QueryDetailDetailVo mergedDetail : mergedDetails) {
                // 通过维度匹配：managementOrg, budgetSubjectCode, masterProjectCode, erpAssetType, isInternal
                if (isClaimDimensionMatch(mergedDetail, mergedResultDetail)) {
                    String dimensionKey = buildDimensionKey(mergedDetail, null);
                    // 如果维度 key 已存在，说明有多个匹配，使用第一个
                    if (!dimensionKeyToResultMap.containsKey(dimensionKey)) {
                        dimensionKeyToResultMap.put(dimensionKey, mergedResultDetail);
                    }
                    break; // 找到匹配后跳出内层循环
                }
            }
        }
        
        for (int i = 0; i < originalDetails.size(); i++) {
            QueryDetailDetailVo originalDetail = originalDetails.get(i);
            Integer mergedIndex = originalToMergedIndexMap.get(i);
            if (mergedIndex != null && mergedIndex < mergedDetails.size()) {
                QueryDetailDetailVo mergedDetail = mergedDetails.get(mergedIndex);
                String dimensionKey = buildDimensionKey(mergedDetail, null);
                ClaimQueryRespDetailVo mergedResultDetail = dimensionKeyToResultMap.get(dimensionKey);
                
                if (mergedResultDetail != null) {
                    ClaimQueryRespDetailVo originalResultDetail = new ClaimQueryRespDetailVo();
                    org.springframework.beans.BeanUtils.copyProperties(mergedResultDetail, originalResultDetail);
                    // 保留原始明细的基础信息
                    originalResultDetail.setClaimYear(originalDetail.getQueryYear());
                    originalResultDetail.setClaimMonth(originalDetail.getQueryMonth());
                    originalResultDetail.setCompany(originalDetail.getCompany());
                    originalResultDetail.setDepartment(originalDetail.getDepartment());
                    if (originalDetail.getMetadata() != null) {
                        originalResultDetail.setMetadata(new java.util.HashMap<>(originalDetail.getMetadata()));
                    }
                    finalDetails.add(originalResultDetail);
                } else {
                    ClaimQueryRespDetailVo emptyDetail = createEmptyClaimQueryDetail(originalDetail);
                    finalDetails.add(emptyDetail);
                }
            } else {
                ClaimQueryRespDetailVo emptyDetail = createEmptyClaimQueryDetail(originalDetail);
                finalDetails.add(emptyDetail);
            }
        }
        return finalDetails;
    }
    
    /**
     * 映射调整查询明细
     */
    private List<AdjustQueryResultDetailVo> mapAdjustQueryDetails(
            List<QueryDetailDetailVo> originalDetails,
            List<QueryDetailDetailVo> mergedDetails,
            List<AdjustQueryResultDetailVo> mergedResultDetails,
            java.util.Map<Integer, Integer> originalToMergedIndexMap,
            String adjustType) {
        List<AdjustQueryResultDetailVo> finalDetails = new java.util.ArrayList<>();
        
        // 构建 mergedDetails 到 mergedResultDetails 的映射（通过维度 key 匹配）
        // 注意：mergedResultDetails 的顺序可能与 mergedDetails 不一致，需要通过维度 key 匹配
        java.util.Map<String, AdjustQueryResultDetailVo> dimensionKeyToResultMap = new java.util.HashMap<>();
        for (AdjustQueryResultDetailVo mergedResultDetail : mergedResultDetails) {
            log.debug("========== mapAdjustQueryDetails - 处理mergedResultDetail: managementOrg={}, masterProjectCode={}, erpAssetType={}, isInternal={}, effectType={}, amountAvailable={} ==========",
                    mergedResultDetail.getManagementOrg(), mergedResultDetail.getMasterProjectCode(),
                    mergedResultDetail.getErpAssetType(), mergedResultDetail.getIsInternal(),
                    mergedResultDetail.getEffectType(), mergedResultDetail.getAmountAvailable());
            
            // 从 mergedResultDetail 中提取维度信息，构建维度 key
            // 注意：mergedResultDetail 中现在有 effectType 字段，可以通过 isDimensionMatch 匹配
            for (QueryDetailDetailVo mergedDetail : mergedDetails) {
                // 通过维度匹配：managementOrg, budgetSubjectCode, masterProjectCode, erpAssetType, isInternal, effectType
                if (isDimensionMatch(mergedDetail, mergedResultDetail)) {
                    String dimensionKey = buildDimensionKey(mergedDetail, adjustType);
                    // 如果维度 key 已存在，说明有多个匹配，使用第一个
                    if (!dimensionKeyToResultMap.containsKey(dimensionKey)) {
                        dimensionKeyToResultMap.put(dimensionKey, mergedResultDetail);
                        log.debug("========== mapAdjustQueryDetails - 匹配成功: dimensionKey={}, amountAvailable={}, effectType={} ==========",
                                dimensionKey, mergedResultDetail.getAmountAvailable(), mergedResultDetail.getEffectType());
                    } else {
                        log.warn("========== mapAdjustQueryDetails - 维度key已存在，跳过: dimensionKey={}, 已存在的amountAvailable={}, 当前amountAvailable={} ==========",
                                dimensionKey, dimensionKeyToResultMap.get(dimensionKey).getAmountAvailable(),
                                mergedResultDetail.getAmountAvailable());
                    }
                    break; // 找到匹配后跳出内层循环
                }
            }
        }
        
        log.info("========== mapAdjustQueryDetails - 开始映射: originalDetails大小={}, mergedDetails大小={}, mergedResultDetails大小={}, dimensionKeyToResultMap大小={} ==========",
                originalDetails.size(), mergedDetails.size(), mergedResultDetails.size(), dimensionKeyToResultMap.size());
        
        for (int i = 0; i < originalDetails.size(); i++) {
            QueryDetailDetailVo originalDetail = originalDetails.get(i);
            String mxid = originalDetail.getMetadata() != null ? originalDetail.getMetadata().get("mxid") : null;
            Integer mergedIndex = originalToMergedIndexMap.get(i);
            log.info("========== mapAdjustQueryDetails - 处理原始明细: mxid={}, i={}, mergedIndex={} ==========",
                    mxid, i, mergedIndex);
            
            if (mergedIndex != null && mergedIndex < mergedDetails.size()) {
                QueryDetailDetailVo mergedDetail = mergedDetails.get(mergedIndex);
                String dimensionKey = buildDimensionKey(mergedDetail, adjustType);
                AdjustQueryResultDetailVo mergedResultDetail = dimensionKeyToResultMap.get(dimensionKey);
                
                log.info("========== mapAdjustQueryDetails - 查找结果: mxid={}, mergedIndex={}, dimensionKey={}, mergedResultDetail是否为空={} ==========",
                        mxid, mergedIndex, dimensionKey, mergedResultDetail == null);
                
                if (mergedResultDetail != null) {
                    AdjustQueryResultDetailVo originalResultDetail = new AdjustQueryResultDetailVo();
                    
                    log.info("========== mapAdjustQueryDetails - 复制前: mxid={}, mergedResultDetail.amountAvailable={} ==========",
                            mxid, mergedResultDetail.getAmountAvailable());
                    
                    org.springframework.beans.BeanUtils.copyProperties(mergedResultDetail, originalResultDetail);
                    
                    log.info("========== mapAdjustQueryDetails - 复制后: mxid={}, originalResultDetail.amountAvailable={} ==========",
                            mxid, originalResultDetail.getAmountAvailable());
                    
                    // 保留原始明细的基础信息
                    originalResultDetail.setAdjustYear(originalDetail.getQueryYear());
                    originalResultDetail.setAdjustMonth(originalDetail.getQueryMonth());
                    originalResultDetail.setCompany(originalDetail.getCompany());
                    originalResultDetail.setDepartment(originalDetail.getDepartment());
                    
                    // 显式设置 amountAvailable，确保不会被覆盖
                    originalResultDetail.setAmountAvailable(mergedResultDetail.getAmountAvailable());
                    
                    log.info("========== mapAdjustQueryDetails - 显式设置后: mxid={}, originalResultDetail.amountAvailable={} ==========",
                            mxid, originalResultDetail.getAmountAvailable());
                    
                    if (originalDetail.getMetadata() != null) {
                        originalResultDetail.setMetadata(new java.util.HashMap<>(originalDetail.getMetadata()));
                    }
                    finalDetails.add(originalResultDetail);
                } else {
                    log.warn("========== mapAdjustQueryDetails - 未找到结果，创建空明细: mxid={}, mergedIndex={}, dimensionKey={} ==========",
                            mxid, mergedIndex, dimensionKey);
                    AdjustQueryResultDetailVo emptyDetail = createEmptyAdjustQueryDetail(originalDetail);
                    finalDetails.add(emptyDetail);
                }
            } else {
                log.warn("========== mapAdjustQueryDetails - 未找到合并索引，创建空明细: mxid={}, i={} ==========",
                        mxid, i);
                AdjustQueryResultDetailVo emptyDetail = createEmptyAdjustQueryDetail(originalDetail);
                finalDetails.add(emptyDetail);
            }
        }
        return finalDetails;
    }
    
    /**
     * 构建合并后明细到查询结果的索引映射
     */
    private java.util.Map<Integer, Integer> buildMergedToResultIndexMap(
            List<QueryDetailDetailVo> mergedDetails, int resultSize) {
        java.util.Map<Integer, Integer> map = new java.util.HashMap<>();
        int resultIndex = 0;
        for (int i = 0; i < mergedDetails.size() && resultIndex < resultSize; i++) {
            map.put(i, resultIndex++);
        }
        return map;
    }
    
    /**
     * 创建空的申请查询明细
     */
    private ApplyQueryRespDetailVo createEmptyApplyQueryDetail(QueryDetailDetailVo originalDetail) {
        ApplyQueryRespDetailVo detail = new ApplyQueryRespDetailVo();
        detail.setDemandYear(originalDetail.getQueryYear());
        detail.setDemandMonth(originalDetail.getQueryMonth());
        detail.setCompany(originalDetail.getCompany());
        detail.setDepartment(originalDetail.getDepartment());
        detail.setManagementOrg(originalDetail.getManagementOrg());
        detail.setBudgetSubjectCode(originalDetail.getBudgetSubjectCode());
        detail.setMasterProjectCode(originalDetail.getMasterProjectCode());
        detail.setErpAssetType(originalDetail.getErpAssetType());
        detail.setIsInternal(originalDetail.getIsInternal());
        detail.setCurrency(originalDetail.getCurrency());
        if (originalDetail.getMetadata() != null) {
            detail.setMetadata(new java.util.HashMap<>(originalDetail.getMetadata()));
        }
        return detail;
    }
    
    /**
     * 创建空的合同查询明细
     */
    private ContractQueryRespDetailVo createEmptyContractQueryDetail(QueryDetailDetailVo originalDetail) {
        ContractQueryRespDetailVo detail = new ContractQueryRespDetailVo();
        detail.setContractYear(originalDetail.getQueryYear());
        detail.setContractMonth(originalDetail.getQueryMonth());
        detail.setCompany(originalDetail.getCompany());
        detail.setDepartment(originalDetail.getDepartment());
        detail.setManagementOrg(originalDetail.getManagementOrg());
        detail.setBudgetSubjectCode(originalDetail.getBudgetSubjectCode());
        detail.setMasterProjectCode(originalDetail.getMasterProjectCode());
        detail.setErpAssetType(originalDetail.getErpAssetType());
        detail.setIsInternal(originalDetail.getIsInternal());
        detail.setCurrency(originalDetail.getCurrency());
        if (originalDetail.getMetadata() != null) {
            detail.setMetadata(new java.util.HashMap<>(originalDetail.getMetadata()));
        }
        return detail;
    }
    
    /**
     * 创建空的付款/报销查询明细
     */
    private ClaimQueryRespDetailVo createEmptyClaimQueryDetail(QueryDetailDetailVo originalDetail) {
        ClaimQueryRespDetailVo detail = new ClaimQueryRespDetailVo();
        detail.setClaimYear(originalDetail.getQueryYear());
        detail.setClaimMonth(originalDetail.getQueryMonth());
        detail.setCompany(originalDetail.getCompany());
        detail.setDepartment(originalDetail.getDepartment());
        detail.setManagementOrg(originalDetail.getManagementOrg());
        detail.setBudgetSubjectCode(originalDetail.getBudgetSubjectCode());
        detail.setMasterProjectCode(originalDetail.getMasterProjectCode());
        detail.setErpAssetType(originalDetail.getErpAssetType());
        detail.setIsInternal(originalDetail.getIsInternal());
        detail.setCurrency(originalDetail.getCurrency());
        if (originalDetail.getMetadata() != null) {
            detail.setMetadata(new java.util.HashMap<>(originalDetail.getMetadata()));
        }
        return detail;
    }
    
    /**
     * 创建空的调整查询明细
     */
    private AdjustQueryResultDetailVo createEmptyAdjustQueryDetail(QueryDetailDetailVo originalDetail) {
        AdjustQueryResultDetailVo detail = new AdjustQueryResultDetailVo();
        detail.setAdjustYear(originalDetail.getQueryYear());
        detail.setAdjustMonth(originalDetail.getQueryMonth());
        detail.setCompany(originalDetail.getCompany());
        detail.setDepartment(originalDetail.getDepartment());
        detail.setManagementOrg(originalDetail.getManagementOrg());
        detail.setBudgetSubjectCode(originalDetail.getBudgetSubjectCode());
        detail.setMasterProjectCode(originalDetail.getMasterProjectCode());
        detail.setErpAssetType(originalDetail.getErpAssetType());
        detail.setIsInternal(originalDetail.getIsInternal());
        detail.setCurrency(originalDetail.getCurrency());
        detail.setEffectType(originalDetail.getEffectType());
        if (originalDetail.getMetadata() != null) {
            detail.setMetadata(new java.util.HashMap<>(originalDetail.getMetadata()));
        }
        return detail;
    }
    
    /**
     * 构建查询明细的维度key（用于匹配原始明细和合并后明细）
     * 规则与DetailMerger中的DetailDimension.getDimensionKey()保持一致
     */
    private String buildDimensionKey(QueryDetailDetailVo detail, String adjustType) {
        String managementOrg = detail.getManagementOrg();
        String budgetSubjectCode = detail.getBudgetSubjectCode();
        String masterProjectCode = detail.getMasterProjectCode();
        String erpAssetType = detail.getErpAssetType();
        String isInternal = detail.getIsInternal();
        // 查询明细：优先使用明细的 effectType 字段，如果没有则从 metadata 中获取，最后才使用传入的 adjustType
        String effectType = null;
        if (StringUtils.isNotBlank(detail.getEffectType())) {
            effectType = detail.getEffectType();
        } else if (detail.getMetadata() != null && detail.getMetadata().containsKey("effectType")) {
            effectType = detail.getMetadata().get("effectType");
        } else if (StringUtils.isNotBlank(adjustType)) {
            effectType = adjustType;
        }
        
        boolean hasProjectCode = !isProjectCodeEmpty(masterProjectCode);
        boolean hasEffectType = StringUtils.isNotBlank(effectType);
        
        if (hasEffectType) {
            // 调整明细：包含 effectType
            if (hasProjectCode) {
                // 带了项目编码，包含集团内/集团外（检查isInternal）
                return String.format("%s@%s@%s@%s@%s@%s",
                    normalize(managementOrg),
                    normalize(budgetSubjectCode),
                    normalize(masterProjectCode),
                    normalize(erpAssetType),
                    normalize(isInternal),
                    normalize(effectType));
            } else {
                // 没带项目编码，不包含集团内/集团外（不检查isInternal）
                return String.format("%s@%s@%s@%s@%s",
                    normalize(managementOrg),
                    normalize(budgetSubjectCode),
                    normalize(masterProjectCode),
                    normalize(erpAssetType),
                    normalize(effectType));
            }
        } else {
            // 非调整明细：不包含 effectType
            if (hasProjectCode) {
                // 带了项目编码，包含集团内/集团外（检查isInternal）
                return String.format("%s@%s@%s@%s@%s",
                    normalize(managementOrg),
                    normalize(budgetSubjectCode),
                    normalize(masterProjectCode),
                    normalize(erpAssetType),
                    normalize(isInternal));
            } else {
                // 没带项目编码，不包含集团内/集团外（不检查isInternal）
                return String.format("%s@%s@%s@%s",
                    normalize(managementOrg),
                    normalize(budgetSubjectCode),
                    normalize(masterProjectCode),
                    normalize(erpAssetType));
            }
        }
    }
    
    /**
     * 判断项目编码是否为空（包括"NAN"）
     */
    private boolean isProjectCodeEmpty(String projectCode) {
        return StringUtils.isBlank(projectCode) || "NAN".equals(projectCode);
    }
    
    /**
     * 判断 QueryDetailDetailVo 和 AdjustQueryResultDetailVo 的维度是否匹配
     * 匹配的维度：managementOrg, budgetSubjectCode, masterProjectCode, erpAssetType, isInternal, effectType
     */
    private boolean isDimensionMatch(QueryDetailDetailVo detail, AdjustQueryResultDetailVo resultDetail) {
        // 获取 detail 的 effectType
        String detailEffectType = detail.getEffectType();
        if (StringUtils.isBlank(detailEffectType) && detail.getMetadata() != null) {
            detailEffectType = detail.getMetadata().get("effectType");
        }
        
        // 获取 resultDetail 的 effectType
        String resultEffectType = resultDetail.getEffectType();
        
        boolean managementOrgMatch = StringUtils.equals(normalize(detail.getManagementOrg()), normalize(resultDetail.getManagementOrg()));
        boolean budgetSubjectCodeMatch = StringUtils.equals(normalize(detail.getBudgetSubjectCode()), normalize(resultDetail.getBudgetSubjectCode()));
        boolean masterProjectCodeMatch = StringUtils.equals(normalize(detail.getMasterProjectCode()), normalize(resultDetail.getMasterProjectCode()));
        boolean erpAssetTypeMatch = StringUtils.equals(normalize(detail.getErpAssetType()), normalize(resultDetail.getErpAssetType()));
        boolean isInternalMatch = StringUtils.equals(normalize(detail.getIsInternal()), normalize(resultDetail.getIsInternal()));
        boolean effectTypeMatch = StringUtils.equals(normalize(detailEffectType), normalize(resultEffectType));
        
        log.debug("========== isDimensionMatch - 匹配检查: detailEffectType={}, resultEffectType={}, effectTypeMatch={}, managementOrgMatch={}, budgetSubjectCodeMatch={}, masterProjectCodeMatch={}, erpAssetTypeMatch={}, isInternalMatch={} ==========",
                detailEffectType, resultEffectType, effectTypeMatch, managementOrgMatch, budgetSubjectCodeMatch, masterProjectCodeMatch, erpAssetTypeMatch, isInternalMatch);
        
        return managementOrgMatch && budgetSubjectCodeMatch && masterProjectCodeMatch && erpAssetTypeMatch && isInternalMatch && effectTypeMatch;
    }
    
    /**
     * 判断 QueryDetailDetailVo 和 ClaimQueryRespDetailVo 的维度是否匹配
     * 匹配的维度：managementOrg, budgetSubjectCode, masterProjectCode, erpAssetType, isInternal
     * 注意：如果没带项目编码（为空或"NAN"），不检查isInternal（与buildDimensionKey和DetailMerger的逻辑保持一致）
     */
    private boolean isClaimDimensionMatch(QueryDetailDetailVo detail, ClaimQueryRespDetailVo resultDetail) {
        boolean managementOrgMatch = StringUtils.equals(normalize(detail.getManagementOrg()), normalize(resultDetail.getManagementOrg()));
        boolean budgetSubjectCodeMatch = StringUtils.equals(normalize(detail.getBudgetSubjectCode()), normalize(resultDetail.getBudgetSubjectCode()));
        boolean masterProjectCodeMatch = StringUtils.equals(normalize(detail.getMasterProjectCode()), normalize(resultDetail.getMasterProjectCode()));
        boolean erpAssetTypeMatch = StringUtils.equals(normalize(detail.getErpAssetType()), normalize(resultDetail.getErpAssetType()));
        
        // 如果没带项目编码，不检查isInternal（与buildDimensionKey和DetailMerger的逻辑保持一致）
        boolean hasProjectCode = !isProjectCodeEmpty(detail.getMasterProjectCode());
        boolean isInternalMatch = true; // 默认匹配
        if (hasProjectCode) {
            // 带了项目编码，需要检查isInternal
            isInternalMatch = StringUtils.equals(normalize(detail.getIsInternal()), normalize(resultDetail.getIsInternal()));
        }
        
        return managementOrgMatch && budgetSubjectCodeMatch && masterProjectCodeMatch && erpAssetTypeMatch && isInternalMatch;
    }
    
    /**
     * 判断 QueryDetailDetailVo 和 ApplyQueryRespDetailVo 的维度是否匹配
     * 匹配的维度：managementOrg, budgetSubjectCode, masterProjectCode, erpAssetType, isInternal
     * 注意：如果没带项目编码（为空或"NAN"），不检查isInternal（与buildDimensionKey和DetailMerger的逻辑保持一致）
     */
    private boolean isApplyDimensionMatch(QueryDetailDetailVo detail, ApplyQueryRespDetailVo resultDetail) {
        boolean managementOrgMatch = StringUtils.equals(normalize(detail.getManagementOrg()), normalize(resultDetail.getManagementOrg()));
        boolean budgetSubjectCodeMatch = StringUtils.equals(normalize(detail.getBudgetSubjectCode()), normalize(resultDetail.getBudgetSubjectCode()));
        boolean masterProjectCodeMatch = StringUtils.equals(normalize(detail.getMasterProjectCode()), normalize(resultDetail.getMasterProjectCode()));
        boolean erpAssetTypeMatch = StringUtils.equals(normalize(detail.getErpAssetType()), normalize(resultDetail.getErpAssetType()));
        
        // 如果没带项目编码，不检查isInternal（与buildDimensionKey和DetailMerger的逻辑保持一致）
        boolean hasProjectCode = !isProjectCodeEmpty(detail.getMasterProjectCode());
        boolean isInternalMatch = true; // 默认匹配
        if (hasProjectCode) {
            // 带了项目编码，需要检查isInternal
            isInternalMatch = StringUtils.equals(normalize(detail.getIsInternal()), normalize(resultDetail.getIsInternal()));
        }
        
        return managementOrgMatch && budgetSubjectCodeMatch && masterProjectCodeMatch && erpAssetTypeMatch && isInternalMatch;
    }
    
    /**
     * 判断 QueryDetailDetailVo 和 ContractQueryRespDetailVo 的维度是否匹配
     * 匹配的维度：managementOrg, budgetSubjectCode, masterProjectCode, erpAssetType, isInternal
     * 注意：如果没带项目编码（为空或"NAN"），不检查isInternal（与buildDimensionKey和DetailMerger的逻辑保持一致）
     */
    private boolean isContractDimensionMatch(QueryDetailDetailVo detail, ContractQueryRespDetailVo resultDetail) {
        boolean managementOrgMatch = StringUtils.equals(normalize(detail.getManagementOrg()), normalize(resultDetail.getManagementOrg()));
        boolean budgetSubjectCodeMatch = StringUtils.equals(normalize(detail.getBudgetSubjectCode()), normalize(resultDetail.getBudgetSubjectCode()));
        boolean masterProjectCodeMatch = StringUtils.equals(normalize(detail.getMasterProjectCode()), normalize(resultDetail.getMasterProjectCode()));
        boolean erpAssetTypeMatch = StringUtils.equals(normalize(detail.getErpAssetType()), normalize(resultDetail.getErpAssetType()));
        
        // 如果没带项目编码，不检查isInternal（与buildDimensionKey和DetailMerger的逻辑保持一致）
        boolean hasProjectCode = !isProjectCodeEmpty(detail.getMasterProjectCode());
        boolean isInternalMatch = true; // 默认匹配
        if (hasProjectCode) {
            // 带了项目编码，需要检查isInternal
            isInternalMatch = StringUtils.equals(normalize(detail.getIsInternal()), normalize(resultDetail.getIsInternal()));
        }
        
        return managementOrgMatch && budgetSubjectCodeMatch && masterProjectCodeMatch && erpAssetTypeMatch && isInternalMatch;
    }
    
    /**
     * 标准化字符串（处理空值和null）
     */
    private String normalize(String value) {
        if (StringUtils.isBlank(value)) {
            return "";
        }
        return value.trim();
    }
}

