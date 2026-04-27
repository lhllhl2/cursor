package com.jasolar.mis.module.system.controller.budget.validation;

import com.jasolar.mis.module.system.controller.budget.vo.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * 预算响应构建工具类
 * 用于快速构建预算相关的响应对象
 * 
 * @author Auto Generated
 * @date 2025-01-XX
 */
public class BudgetResponseBuilder {
    
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
    private static final DateTimeFormatter PROCESS_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    /**
     * 构建预算调整响应
     */
    public static BudgetAdjustRespVo buildAdjustResponse(BudgetAdjustApplyParams params) {
        ESBInfoParams esbInfo = params.getEsbInfo();
        AdjustApplyReqInfoParams adjustInfo = params.getAdjustApplyReqInfo();
        
        String instId = generateInstId(esbInfo.getInstId());
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
        
        List<AdjustDetailRespVo> resultAdjustDetails = new ArrayList<>();
        if (adjustInfo != null && adjustInfo.getAdjustDetails() != null) {
            for (AdjustDetailDetailVo detail : adjustInfo.getAdjustDetails()) {
                AdjustDetailRespVo resultDetail = new AdjustDetailRespVo();
                BeanUtils.copyProperties(detail, resultDetail);
                // 显式复制 metadata，确保 Map 类型字段被正确复制
                if (detail.getMetadata() != null) {
                    resultDetail.setMetadata(new java.util.HashMap<>(detail.getMetadata()));
                }
                resultDetail.setValidationResult("0");
                resultDetail.setValidationMessage("处理成功 Successfully Processed");
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
     * 构建预算申请响应
     */
    public static BudgetApplicationRespVo buildApplicationResponse(BudgetApplicationParams params) {
        ESBInfoParams esbInfo = params.getEsbInfo();
        ApplyReqInfoParams applyInfo = params.getApplyReqInfo();
        
        String instId = generateInstId(esbInfo.getInstId());
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
        
        List<ApplyDetailRespVo> resultDemandDetails = new ArrayList<>();
        if (applyInfo != null && applyInfo.getDemandDetails() != null) {
            for (ApplyDetailDetalVo detail : applyInfo.getDemandDetails()) {
                ApplyDetailRespVo resultDetail = new ApplyDetailRespVo();
                BeanUtils.copyProperties(detail, resultDetail);
                // 显式复制 metadata，确保 Map 类型字段被正确复制
                if (detail.getMetadata() != null) {
                    resultDetail.setMetadata(new java.util.HashMap<>(detail.getMetadata()));
                }
                resultDetail.setValidationResult("0");
                resultDetail.setValidationMessage("处理成功 Successfully Processed");
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
     * 构建付款/报销响应
     */
    public static BudgetClaimRespVo buildClaimResponse(BudgetClaimApplyParams params) {
        ESBInfoParams esbInfo = params.getEsbInfo();
        ClaimApplyReqInfoParams claimInfo = params.getClaimApplyReqInfo();
        
        String instId = generateInstId(esbInfo.getInstId());
        String responseTime = LocalDateTime.now().format(DATE_TIME_FORMATTER);
        
        ESBRespInfoVo esbRespInfo = ESBRespInfoVo.builder()
                .instId(instId)
                .requestTime(esbInfo.getRequestTime())
                .attr1(esbInfo.getAttr1())
                .attr2(esbInfo.getAttr2())
                .attr3(esbInfo.getAttr3())
                .returnCode("A0001-CLAIM")
                .returnMsg("付款/报销申请处理成功")
                .returnStatus("S")
                .responseTime(responseTime)
                .build();
        
        List<ClaimDetailRespVo> resultClaimDetails = new ArrayList<>();
        if (claimInfo != null && claimInfo.getClaimDetails() != null) {
            for (ClaimDetailDetailVo detail : claimInfo.getClaimDetails()) {
                ClaimDetailRespVo resultDetail = new ClaimDetailRespVo();
                BeanUtils.copyProperties(detail, resultDetail);
                // 显式复制 metadata，确保 Map 类型字段被正确复制
                if (detail.getMetadata() != null) {
                    resultDetail.setMetadata(new java.util.HashMap<>(detail.getMetadata()));
                }
                resultDetail.setValidationResult("0");
                resultDetail.setValidationMessage("处理成功 Successfully Processed");
                resultClaimDetails.add(resultDetail);
            }
        }
        
        ClaimApplyResultInfoRespVo claimApplyResult = new ClaimApplyResultInfoRespVo();
        claimApplyResult.setClaimOrderNo(claimInfo != null ? claimInfo.getClaimOrderNo() : null);
        claimApplyResult.setProcessTime(LocalDateTime.now().format(PROCESS_TIME_FORMATTER));
        claimApplyResult.setClaimDetails(resultClaimDetails);
        
        BudgetClaimRespVo respVo = new BudgetClaimRespVo();
        respVo.setEsbInfo(esbRespInfo);
        respVo.setClaimApplyResult(claimApplyResult);
        return respVo;
    }
    
    /**
     * 构建合同响应
     */
    public static BudgetContractRespVo buildContractResponse(BudgetContractApplyParams params) {
        ESBInfoParams esbInfo = params.getEsbInfo();
        ContractApplyReqInfoParams contractInfo = params.getContractApplyReqInfo();
        
        String instId = generateInstId(esbInfo.getInstId());
        String responseTime = LocalDateTime.now().format(DATE_TIME_FORMATTER);
        
        ESBRespInfoVo esbRespInfo = ESBRespInfoVo.builder()
                .instId(instId)
                .requestTime(esbInfo.getRequestTime())
                .attr1(esbInfo.getAttr1())
                .attr2(esbInfo.getAttr2())
                .attr3(esbInfo.getAttr3())
                .returnCode("A0001-CONTRACT")
                .returnMsg("预算合同申请处理成功")
                .returnStatus("S")
                .responseTime(responseTime)
                .build();
        
        List<ContractDetailRespVo> resultContractDetails = new ArrayList<>();
        if (contractInfo != null && contractInfo.getContractDetails() != null) {
            for (ContractDetailDetailVo detail : contractInfo.getContractDetails()) {
                ContractDetailRespVo resultDetail = new ContractDetailRespVo();
                BeanUtils.copyProperties(detail, resultDetail);
                // 显式复制 metadata，确保 Map 类型字段被正确复制
                if (detail.getMetadata() != null) {
                    resultDetail.setMetadata(new java.util.HashMap<>(detail.getMetadata()));
                }
                resultDetail.setValidationResult("0");
                resultDetail.setValidationMessage("处理成功 Successfully Processed");
                resultContractDetails.add(resultDetail);
            }
        }
        
        ContractApplyResultInfoRespVo contractApplyResult = new ContractApplyResultInfoRespVo();
        contractApplyResult.setContractNo(contractInfo != null ? contractInfo.getContractNo() : null);
        contractApplyResult.setProcessTime(LocalDateTime.now().format(PROCESS_TIME_FORMATTER));
        contractApplyResult.setContractDetails(resultContractDetails);
        
        BudgetContractRespVo respVo = new BudgetContractRespVo();
        respVo.setEsbInfo(esbRespInfo);
        respVo.setContractApplyResult(contractApplyResult);
        return respVo;
    }
    
    /**
     * 构建查询响应（查询接口需要特殊处理，因为返回结构不同）
     * 当科目编码匹配配置的前缀时，返回空的查询结果
     */
    public static BudgetQueryRespVo buildQueryResponse(BudgetQueryParams params) {
        ESBInfoParams esbInfo = params.getEsbInfo();
        
        String instId = generateInstId(esbInfo.getInstId());
        String responseTime = LocalDateTime.now().format(DATE_TIME_FORMATTER);
        
        ESBRespInfoVo esbRespInfo = ESBRespInfoVo.builder()
                .instId(instId)
                .requestTime(esbInfo.getRequestTime())
                .attr1(esbInfo.getAttr1())
                .attr2(esbInfo.getAttr2())
                .attr3(esbInfo.getAttr3())
                .returnCode("A0001-BudgetQuery")
                .returnMsg("查询成功")
                .returnStatus("S")
                .responseTime(responseTime)
                .build();
        
        // 根据查询类型构建空的查询结果
        QueryReqInfoParams queryReqInfo = params.getQueryReqInfoParams();
        Object queryResult = null;
        
        // 根据查询类型返回对应的空结果对象
        if (queryReqInfo != null) {
            String demandOrderNo = queryReqInfo.getDemandOrderNo();
            String contractNo = queryReqInfo.getContractNo();
            String claimOrderNo = queryReqInfo.getClaimOrderNo();
            String adjustType = queryReqInfo.getAdjustType();
            
            if (StringUtils.isNotBlank(adjustType)) {
                // 调整单查询，返回空的AdjustQueryResultVo
                queryResult = new AdjustQueryResultVo();
                ((AdjustQueryResultVo) queryResult).setAdjustDetails(new ArrayList<>());
            } else if (StringUtils.isNotBlank(demandOrderNo)) {
                // 需求单查询，返回空的ApplyQueryResultVo
                queryResult = new ApplyQueryResultVo();
                ((ApplyQueryResultVo) queryResult).setDemandDetails(new ArrayList<>());
            } else if (StringUtils.isNotBlank(contractNo)) {
                // 合同查询，返回空的ContractQueryResultVo
                queryResult = new ContractQueryResultVo();
                ((ContractQueryResultVo) queryResult).setContractDetails(new ArrayList<>());
            } else if (StringUtils.isNotBlank(claimOrderNo)) {
                // 付款/报销查询，返回空的ClaimQueryResultVo
                queryResult = new ClaimQueryResultVo();
                ((ClaimQueryResultVo) queryResult).setClaimDetails(new ArrayList<>());
            }
        }
        
        BudgetQueryRespVo respVo = new BudgetQueryRespVo();
        respVo.setEsbInfo(esbRespInfo);
        respVo.setQueryResult(queryResult);
        return respVo;
    }

    /**
     * 构建查询响应（带明细错误）
     */
    public static BudgetQueryRespVo buildQueryResponseWithDetailErrors(
            BudgetQueryParams params, java.util.Map<Integer, String> duplicateDetailErrors) {
        ESBInfoParams esbInfo = params.getEsbInfo();
        QueryReqInfoParams queryReqInfo = params.getQueryReqInfoParams();
        
        String instId = generateInstId(esbInfo.getInstId());
        String responseTime = LocalDateTime.now().format(DATE_TIME_FORMATTER);
        
        ESBRespInfoVo esbRespInfo = ESBRespInfoVo.builder()
                .instId(instId)
                .requestTime(esbInfo.getRequestTime())
                .attr1(esbInfo.getAttr1())
                .attr2(esbInfo.getAttr2())
                .attr3(esbInfo.getAttr3())
                .returnCode("E0001-BudgetQuery")
                .returnMsg("存在相同维度的明细 Duplicate Items with Same Dimension")
                .returnStatus("S")
                .responseTime(responseTime)
                .build();
        
        // 根据查询类型构建对应的查询结果对象
        Object queryResult = null;
        if (queryReqInfo != null) {
            String demandOrderNo = queryReqInfo.getDemandOrderNo();
            String contractNo = queryReqInfo.getContractNo();
            String claimOrderNo = queryReqInfo.getClaimOrderNo();
            String adjustType = queryReqInfo.getAdjustType();
            List<QueryDetailDetailVo> details = queryReqInfo.getDetails();
            
            if (StringUtils.isNotBlank(adjustType)) {
                // 调整单查询
                AdjustQueryResultVo adjustResult = new AdjustQueryResultVo();
                List<AdjustQueryResultDetailVo> adjustDetails = new ArrayList<>();
                if (details != null) {
                    for (int i = 0; i < details.size(); i++) {
                        QueryDetailDetailVo detail = details.get(i);
                        AdjustQueryResultDetailVo resultDetail = new AdjustQueryResultDetailVo();
                        BeanUtils.copyProperties(detail, resultDetail);
                        // 字段映射
                        resultDetail.setAdjustYear(detail.getQueryYear());
                        resultDetail.setAdjustMonth(detail.getQueryMonth());
                        if (detail.getMetadata() != null) {
                            resultDetail.setMetadata(new java.util.HashMap<>(detail.getMetadata()));
                        }
                        
                        // 设置明细级别的校验结果
                        String errorMessage = duplicateDetailErrors.get(i);
                        if (errorMessage != null) {
                            resultDetail.setValidationResult("1");
                            resultDetail.setValidationMessage(errorMessage);
                        } else {
                            resultDetail.setValidationResult("0");
                            resultDetail.setValidationMessage("处理成功 Successfully Processed");
                        }
                        adjustDetails.add(resultDetail);
                    }
                }
                adjustResult.setAdjustDetails(adjustDetails);
                queryResult = adjustResult;
            } else if (StringUtils.isNotBlank(demandOrderNo)) {
                // 需求单查询
                ApplyQueryResultVo applyResult = new ApplyQueryResultVo();
                List<ApplyQueryRespDetailVo> demandDetails = new ArrayList<>();
                if (details != null) {
                    for (int i = 0; i < details.size(); i++) {
                        QueryDetailDetailVo detail = details.get(i);
                        ApplyQueryRespDetailVo resultDetail = new ApplyQueryRespDetailVo();
                        BeanUtils.copyProperties(detail, resultDetail);
                        // 字段映射
                        resultDetail.setDemandYear(detail.getQueryYear());
                        resultDetail.setDemandMonth(detail.getQueryMonth());
                        if (detail.getMetadata() != null) {
                            resultDetail.setMetadata(new java.util.HashMap<>(detail.getMetadata()));
                        }
                        
                        // 设置明细级别的校验结果
                        String errorMessage = duplicateDetailErrors.get(i);
                        if (errorMessage != null) {
                            resultDetail.setValidationResult("1");
                            resultDetail.setValidationMessage(errorMessage);
                        } else {
                            resultDetail.setValidationResult("0");
                            resultDetail.setValidationMessage("处理成功 Successfully Processed");
                        }
                        demandDetails.add(resultDetail);
                    }
                }
                applyResult.setDemandDetails(demandDetails);
                queryResult = applyResult;
            } else if (StringUtils.isNotBlank(contractNo)) {
                // 合同查询
                ContractQueryResultVo contractResult = new ContractQueryResultVo();
                List<ContractQueryRespDetailVo> contractDetails = new ArrayList<>();
                if (details != null) {
                    for (int i = 0; i < details.size(); i++) {
                        QueryDetailDetailVo detail = details.get(i);
                        ContractQueryRespDetailVo resultDetail = new ContractQueryRespDetailVo();
                        BeanUtils.copyProperties(detail, resultDetail);
                        // 字段映射
                        resultDetail.setContractYear(detail.getQueryYear());
                        resultDetail.setContractMonth(detail.getQueryMonth());
                        if (detail.getMetadata() != null) {
                            resultDetail.setMetadata(new java.util.HashMap<>(detail.getMetadata()));
                        }
                        
                        // 设置明细级别的校验结果
                        String errorMessage = duplicateDetailErrors.get(i);
                        if (errorMessage != null) {
                            resultDetail.setValidationResult("1");
                            resultDetail.setValidationMessage(errorMessage);
                        } else {
                            resultDetail.setValidationResult("0");
                            resultDetail.setValidationMessage("处理成功 Successfully Processed");
                        }
                        contractDetails.add(resultDetail);
                    }
                }
                contractResult.setContractDetails(contractDetails);
                queryResult = contractResult;
            } else if (StringUtils.isNotBlank(claimOrderNo)) {
                // 付款/报销查询
                ClaimQueryResultVo claimResult = new ClaimQueryResultVo();
                List<ClaimQueryRespDetailVo> claimDetails = new ArrayList<>();
                if (details != null) {
                    for (int i = 0; i < details.size(); i++) {
                        QueryDetailDetailVo detail = details.get(i);
                        ClaimQueryRespDetailVo resultDetail = new ClaimQueryRespDetailVo();
                        BeanUtils.copyProperties(detail, resultDetail);
                        // 字段映射
                        resultDetail.setClaimYear(detail.getQueryYear());
                        resultDetail.setClaimMonth(detail.getQueryMonth());
                        if (detail.getMetadata() != null) {
                            resultDetail.setMetadata(new java.util.HashMap<>(detail.getMetadata()));
                        }
                        
                        // 设置明细级别的校验结果
                        String errorMessage = duplicateDetailErrors.get(i);
                        if (errorMessage != null) {
                            resultDetail.setValidationResult("1");
                            resultDetail.setValidationMessage(errorMessage);
                        } else {
                            resultDetail.setValidationResult("0");
                            resultDetail.setValidationMessage("处理成功 Successfully Processed");
                        }
                        claimDetails.add(resultDetail);
                    }
                }
                claimResult.setClaimDetails(claimDetails);
                queryResult = claimResult;
            }
        }
        
        BudgetQueryRespVo respVo = new BudgetQueryRespVo();
        respVo.setEsbInfo(esbRespInfo);
        respVo.setQueryResult(queryResult);
        return respVo;
    }
    
    /**
     * 构建预算调整响应（带明细错误）
     */
    public static BudgetAdjustRespVo buildAdjustResponseWithDetailErrors(
            BudgetAdjustApplyParams params, java.util.Map<Integer, String> duplicateDetailErrors) {
        ESBInfoParams esbInfo = params.getEsbInfo();
        AdjustApplyReqInfoParams adjustInfo = params.getAdjustApplyReqInfo();
        
        String instId = generateInstId(esbInfo.getInstId());
        String responseTime = LocalDateTime.now().format(DATE_TIME_FORMATTER);
        
        ESBRespInfoVo esbRespInfo = ESBRespInfoVo.builder()
                .instId(instId)
                .requestTime(esbInfo.getRequestTime())
                .attr1(esbInfo.getAttr1())
                .attr2(esbInfo.getAttr2())
                .attr3(esbInfo.getAttr3())
                .returnCode("E0001-ADJUST")
                .returnMsg("存在相同维度的明细 Duplicate Items with Same Dimension")
                .returnStatus("S")
                .responseTime(responseTime)
                .build();
        
        List<AdjustDetailRespVo> resultAdjustDetails = new ArrayList<>();
        if (adjustInfo != null && adjustInfo.getAdjustDetails() != null) {
            for (int i = 0; i < adjustInfo.getAdjustDetails().size(); i++) {
                AdjustDetailDetailVo detail = adjustInfo.getAdjustDetails().get(i);
                AdjustDetailRespVo resultDetail = new AdjustDetailRespVo();
                BeanUtils.copyProperties(detail, resultDetail);
                if (detail.getMetadata() != null) {
                    resultDetail.setMetadata(new java.util.HashMap<>(detail.getMetadata()));
                }
                
                // 设置明细级别的校验结果
                String errorMessage = duplicateDetailErrors.get(i);
                if (errorMessage != null) {
                    resultDetail.setValidationResult("1");
                    resultDetail.setValidationMessage(errorMessage);
                } else {
                    resultDetail.setValidationResult("0");
                    resultDetail.setValidationMessage("处理成功 Successfully Processed");
                }
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
     * 构建预算申请响应（带明细错误）
     */
    public static BudgetApplicationRespVo buildApplicationResponseWithDetailErrors(
            BudgetApplicationParams params, java.util.Map<Integer, String> duplicateDetailErrors) {
        ESBInfoParams esbInfo = params.getEsbInfo();
        ApplyReqInfoParams applyInfo = params.getApplyReqInfo();
        
        String instId = generateInstId(esbInfo.getInstId());
        String responseTime = LocalDateTime.now().format(DATE_TIME_FORMATTER);
        
        ESBRespInfoVo esbRespInfo = ESBRespInfoVo.builder()
                .instId(instId)
                .requestTime(esbInfo.getRequestTime())
                .attr1(esbInfo.getAttr1())
                .attr2(esbInfo.getAttr2())
                .attr3(esbInfo.getAttr3())
                .returnCode("E0001-BUDGET")
                .returnMsg("存在相同维度的明细 Duplicate Items with Same Dimension")
                .returnStatus("S")
                .responseTime(responseTime)
                .build();
        
        List<ApplyDetailRespVo> resultDemandDetails = new ArrayList<>();
        if (applyInfo != null && applyInfo.getDemandDetails() != null) {
            for (int i = 0; i < applyInfo.getDemandDetails().size(); i++) {
                ApplyDetailDetalVo detail = applyInfo.getDemandDetails().get(i);
                ApplyDetailRespVo resultDetail = new ApplyDetailRespVo();
                BeanUtils.copyProperties(detail, resultDetail);
                if (detail.getMetadata() != null) {
                    resultDetail.setMetadata(new java.util.HashMap<>(detail.getMetadata()));
                }
                
                // 设置明细级别的校验结果
                String errorMessage = duplicateDetailErrors.get(i);
                if (errorMessage != null) {
                    resultDetail.setValidationResult("1");
                    resultDetail.setValidationMessage(errorMessage);
                } else {
                    resultDetail.setValidationResult("0");
                    resultDetail.setValidationMessage("处理成功 Successfully Processed");
                }
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
     * 构建付款/报销响应（带明细错误）
     */
    public static BudgetClaimRespVo buildClaimResponseWithDetailErrors(
            BudgetClaimApplyParams params, java.util.Map<Integer, String> duplicateDetailErrors) {
        ESBInfoParams esbInfo = params.getEsbInfo();
        ClaimApplyReqInfoParams claimInfo = params.getClaimApplyReqInfo();
        
        String instId = generateInstId(esbInfo.getInstId());
        String responseTime = LocalDateTime.now().format(DATE_TIME_FORMATTER);
        
        ESBRespInfoVo esbRespInfo = ESBRespInfoVo.builder()
                .instId(instId)
                .requestTime(esbInfo.getRequestTime())
                .attr1(esbInfo.getAttr1())
                .attr2(esbInfo.getAttr2())
                .attr3(esbInfo.getAttr3())
                .returnCode("E0001-CLAIM")
                .returnMsg("存在相同维度的明细 Duplicate Items with Same Dimension")
                .returnStatus("S")
                .responseTime(responseTime)
                .build();
        
        List<ClaimDetailRespVo> resultClaimDetails = new ArrayList<>();
        if (claimInfo != null && claimInfo.getClaimDetails() != null) {
            for (int i = 0; i < claimInfo.getClaimDetails().size(); i++) {
                ClaimDetailDetailVo detail = claimInfo.getClaimDetails().get(i);
                ClaimDetailRespVo resultDetail = new ClaimDetailRespVo();
                BeanUtils.copyProperties(detail, resultDetail);
                if (detail.getMetadata() != null) {
                    resultDetail.setMetadata(new java.util.HashMap<>(detail.getMetadata()));
                }
                
                // 设置明细级别的校验结果
                String errorMessage = duplicateDetailErrors.get(i);
                if (errorMessage != null) {
                    resultDetail.setValidationResult("1");
                    resultDetail.setValidationMessage(errorMessage);
                } else {
                    resultDetail.setValidationResult("0");
                    resultDetail.setValidationMessage("处理成功 Successfully Processed");
                }
                resultClaimDetails.add(resultDetail);
            }
        }
        
        ClaimApplyResultInfoRespVo claimApplyResult = new ClaimApplyResultInfoRespVo();
        claimApplyResult.setClaimOrderNo(claimInfo != null ? claimInfo.getClaimOrderNo() : null);
        claimApplyResult.setProcessTime(LocalDateTime.now().format(PROCESS_TIME_FORMATTER));
        claimApplyResult.setClaimDetails(resultClaimDetails);
        
        BudgetClaimRespVo respVo = new BudgetClaimRespVo();
        respVo.setEsbInfo(esbRespInfo);
        respVo.setClaimApplyResult(claimApplyResult);
        return respVo;
    }

    /**
     * 构建合同响应（带明细错误）
     */
    public static BudgetContractRespVo buildContractResponseWithDetailErrors(
            BudgetContractApplyParams params, java.util.Map<Integer, String> duplicateDetailErrors) {
        ESBInfoParams esbInfo = params.getEsbInfo();
        ContractApplyReqInfoParams contractInfo = params.getContractApplyReqInfo();
        
        String instId = generateInstId(esbInfo.getInstId());
        String responseTime = LocalDateTime.now().format(DATE_TIME_FORMATTER);
        
        ESBRespInfoVo esbRespInfo = ESBRespInfoVo.builder()
                .instId(instId)
                .requestTime(esbInfo.getRequestTime())
                .attr1(esbInfo.getAttr1())
                .attr2(esbInfo.getAttr2())
                .attr3(esbInfo.getAttr3())
                .returnCode("E0001-CONTRACT")
                .returnMsg("存在相同维度的明细 Duplicate Items with Same Dimension")
                .returnStatus("S")
                .responseTime(responseTime)
                .build();
        
        List<ContractDetailRespVo> resultContractDetails = new ArrayList<>();
        if (contractInfo != null && contractInfo.getContractDetails() != null) {
            for (int i = 0; i < contractInfo.getContractDetails().size(); i++) {
                ContractDetailDetailVo detail = contractInfo.getContractDetails().get(i);
                ContractDetailRespVo resultDetail = new ContractDetailRespVo();
                BeanUtils.copyProperties(detail, resultDetail);
                if (detail.getMetadata() != null) {
                    resultDetail.setMetadata(new java.util.HashMap<>(detail.getMetadata()));
                }
                
                // 设置明细级别的校验结果
                String errorMessage = duplicateDetailErrors.get(i);
                if (errorMessage != null) {
                    resultDetail.setValidationResult("1");
                    resultDetail.setValidationMessage(errorMessage);
                } else {
                    resultDetail.setValidationResult("0");
                    resultDetail.setValidationMessage("处理成功 Successfully Processed");
                }
                resultContractDetails.add(resultDetail);
            }
        }
        
        ContractApplyResultInfoRespVo contractApplyResult = new ContractApplyResultInfoRespVo();
        contractApplyResult.setContractNo(contractInfo != null ? contractInfo.getContractNo() : null);
        contractApplyResult.setProcessTime(LocalDateTime.now().format(PROCESS_TIME_FORMATTER));
        contractApplyResult.setContractDetails(resultContractDetails);
        
        BudgetContractRespVo respVo = new BudgetContractRespVo();
        respVo.setEsbInfo(esbRespInfo);
        respVo.setContractApplyResult(contractApplyResult);
        return respVo;
    }

    /**
     * 构建付款/报销锁失败响应
     */
    public static BudgetClaimRespVo buildClaimLockFailedResponse(BudgetClaimApplyParams params) {
        ESBInfoParams esbInfo = params.getEsbInfo();
        ClaimApplyReqInfoParams claimInfo = params.getClaimApplyReqInfo();
        
        String instId = generateInstId(esbInfo != null ? esbInfo.getInstId() : null);
        String responseTime = LocalDateTime.now().format(DATE_TIME_FORMATTER);
        
        ESBRespInfoVo esbRespInfo = ESBRespInfoVo.builder()
                .instId(instId)
                .requestTime(esbInfo != null ? esbInfo.getRequestTime() : null)
                .attr1(esbInfo != null ? esbInfo.getAttr1() : null)
                .attr2(esbInfo != null ? esbInfo.getAttr2() : null)
                .attr3(esbInfo != null ? esbInfo.getAttr3() : null)
                .returnCode("E0002-CLAIM")
                .returnMsg("订单正在处理中，请勿重复提交")
                .returnStatus("S")
                .responseTime(responseTime)
                .build();
        
        List<ClaimDetailRespVo> resultClaimDetails = new ArrayList<>();
        if (claimInfo != null && claimInfo.getClaimDetails() != null) {
            for (ClaimDetailDetailVo detail : claimInfo.getClaimDetails()) {
                ClaimDetailRespVo resultDetail = new ClaimDetailRespVo();
                BeanUtils.copyProperties(detail, resultDetail);
                if (detail.getMetadata() != null) {
                    resultDetail.setMetadata(new java.util.HashMap<>(detail.getMetadata()));
                }
                resultDetail.setValidationResult("1");
                resultDetail.setValidationMessage("订单正在处理中，请勿重复提交");
                resultClaimDetails.add(resultDetail);
            }
        }
        
        ClaimApplyResultInfoRespVo claimApplyResult = new ClaimApplyResultInfoRespVo();
        claimApplyResult.setClaimOrderNo(claimInfo != null ? claimInfo.getClaimOrderNo() : null);
        claimApplyResult.setProcessTime(LocalDateTime.now().format(PROCESS_TIME_FORMATTER));
        claimApplyResult.setClaimDetails(resultClaimDetails);
        
        BudgetClaimRespVo respVo = new BudgetClaimRespVo();
        respVo.setEsbInfo(esbRespInfo);
        respVo.setClaimApplyResult(claimApplyResult);
        return respVo;
    }

    /**
     * 构建预算调整锁失败响应
     */
    public static BudgetAdjustRespVo buildAdjustLockFailedResponse(BudgetAdjustApplyParams params) {
        ESBInfoParams esbInfo = params.getEsbInfo();
        AdjustApplyReqInfoParams adjustInfo = params.getAdjustApplyReqInfo();
        
        String instId = generateInstId(esbInfo != null ? esbInfo.getInstId() : null);
        String responseTime = LocalDateTime.now().format(DATE_TIME_FORMATTER);
        
        ESBRespInfoVo esbRespInfo = ESBRespInfoVo.builder()
                .instId(instId)
                .requestTime(esbInfo != null ? esbInfo.getRequestTime() : null)
                .attr1(esbInfo != null ? esbInfo.getAttr1() : null)
                .attr2(esbInfo != null ? esbInfo.getAttr2() : null)
                .attr3(esbInfo != null ? esbInfo.getAttr3() : null)
                .returnCode("E0002-ADJUST")
                .returnMsg("订单正在处理中，请勿重复提交")
                .returnStatus("S")
                .responseTime(responseTime)
                .build();
        
        List<AdjustDetailRespVo> resultAdjustDetails = new ArrayList<>();
        if (adjustInfo != null && adjustInfo.getAdjustDetails() != null) {
            for (AdjustDetailDetailVo detail : adjustInfo.getAdjustDetails()) {
                AdjustDetailRespVo resultDetail = new AdjustDetailRespVo();
                BeanUtils.copyProperties(detail, resultDetail);
                if (detail.getMetadata() != null) {
                    resultDetail.setMetadata(new java.util.HashMap<>(detail.getMetadata()));
                }
                resultDetail.setValidationResult("1");
                resultDetail.setValidationMessage("订单正在处理中，请勿重复提交");
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
     * 构建合同锁失败响应
     */
    public static BudgetContractRespVo buildContractLockFailedResponse(BudgetContractApplyParams params) {
        ESBInfoParams esbInfo = params.getEsbInfo();
        ContractApplyReqInfoParams contractInfo = params.getContractApplyReqInfo();
        
        String instId = generateInstId(esbInfo != null ? esbInfo.getInstId() : null);
        String responseTime = LocalDateTime.now().format(DATE_TIME_FORMATTER);
        
        ESBRespInfoVo esbRespInfo = ESBRespInfoVo.builder()
                .instId(instId)
                .requestTime(esbInfo != null ? esbInfo.getRequestTime() : null)
                .attr1(esbInfo != null ? esbInfo.getAttr1() : null)
                .attr2(esbInfo != null ? esbInfo.getAttr2() : null)
                .attr3(esbInfo != null ? esbInfo.getAttr3() : null)
                .returnCode("E0002-CONTRACT")
                .returnMsg("订单正在处理中，请勿重复提交")
                .returnStatus("S")
                .responseTime(responseTime)
                .build();
        
        List<ContractDetailRespVo> resultContractDetails = new ArrayList<>();
        if (contractInfo != null && contractInfo.getContractDetails() != null) {
            for (ContractDetailDetailVo detail : contractInfo.getContractDetails()) {
                ContractDetailRespVo resultDetail = new ContractDetailRespVo();
                BeanUtils.copyProperties(detail, resultDetail);
                if (detail.getMetadata() != null) {
                    resultDetail.setMetadata(new java.util.HashMap<>(detail.getMetadata()));
                }
                resultDetail.setValidationResult("1");
                resultDetail.setValidationMessage("订单正在处理中，请勿重复提交");
                resultContractDetails.add(resultDetail);
            }
        }
        
        ContractApplyResultInfoRespVo contractApplyResult = new ContractApplyResultInfoRespVo();
        contractApplyResult.setContractNo(contractInfo != null ? contractInfo.getContractNo() : null);
        contractApplyResult.setProcessTime(LocalDateTime.now().format(PROCESS_TIME_FORMATTER));
        contractApplyResult.setContractDetails(resultContractDetails);
        
        BudgetContractRespVo respVo = new BudgetContractRespVo();
        respVo.setEsbInfo(esbRespInfo);
        respVo.setContractApplyResult(contractApplyResult);
        return respVo;
    }

    /**
     * 构建申请锁失败响应
     */
    public static BudgetApplicationRespVo buildApplicationLockFailedResponse(BudgetApplicationParams params) {
        ESBInfoParams esbInfo = params.getEsbInfo();
        ApplyReqInfoParams applyInfo = params.getApplyReqInfo();
        
        String instId = generateInstId(esbInfo != null ? esbInfo.getInstId() : null);
        String responseTime = LocalDateTime.now().format(DATE_TIME_FORMATTER);
        
        ESBRespInfoVo esbRespInfo = ESBRespInfoVo.builder()
                .instId(instId)
                .requestTime(esbInfo != null ? esbInfo.getRequestTime() : null)
                .attr1(esbInfo != null ? esbInfo.getAttr1() : null)
                .attr2(esbInfo != null ? esbInfo.getAttr2() : null)
                .attr3(esbInfo != null ? esbInfo.getAttr3() : null)
                .returnCode("E0002-BUDGET")
                .returnMsg("订单正在处理中，请勿重复提交")
                .returnStatus("S")
                .responseTime(responseTime)
                .build();
        
        List<ApplyDetailRespVo> resultDemandDetails = new ArrayList<>();
        if (applyInfo != null && applyInfo.getDemandDetails() != null) {
            for (ApplyDetailDetalVo detail : applyInfo.getDemandDetails()) {
                ApplyDetailRespVo resultDetail = new ApplyDetailRespVo();
                BeanUtils.copyProperties(detail, resultDetail);
                if (detail.getMetadata() != null) {
                    resultDetail.setMetadata(new java.util.HashMap<>(detail.getMetadata()));
                }
                resultDetail.setValidationResult("1");
                resultDetail.setValidationMessage("订单正在处理中，请勿重复提交");
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
     * 生成实例ID
     */
    private static String generateInstId(String existingInstId) {
        if (existingInstId != null && !existingInstId.isEmpty()) {
            return existingInstId;
        }
        String uuid = UUID.randomUUID().toString().replace("-", "");
        return "N" + uuid.substring(0, 8) + "." + uuid.substring(8, 16) + ".N"
                + uuid.substring(16, 18) + "." + uuid.substring(18, 28) + ".N" + uuid.substring(28, 32);
    }
}

