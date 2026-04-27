package com.jasolar.mis.module.system.controller.budget;

import com.jasolar.mis.module.system.controller.budget.vo.*;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.http.HttpStatus;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 预算模块全局异常处理器
 * 用于捕获 Bean Validation 的校验异常，并转换为统一的响应格式
 */
@RestControllerAdvice(basePackages = "com.jasolar.mis.module.system.controller.budget")
@Slf4j
public class BudgetValidationExceptionHandler {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
    private static final DateTimeFormatter PROCESS_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * 处理 @Valid 注解触发的校验异常
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.OK) // 返回 200，但业务状态为失败
    public Object handleMethodArgumentNotValid(MethodArgumentNotValidException ex) {
        log.error("参数校验失败", ex);
        
        // 尝试获取原始参数
        Object target = ex.getBindingResult().getTarget();
        
        // 根据参数类型返回相应的响应格式
        if (target instanceof BudgetApplicationParams) {
            return buildApplicationErrorResponse((BudgetApplicationParams) target, ex);
        } else if (target instanceof BudgetContractApplyParams) {
            return buildContractErrorResponse((BudgetContractApplyParams) target, ex);
        } else if (target instanceof BudgetClaimApplyParams) {
            return buildClaimErrorResponse((BudgetClaimApplyParams) target, ex);
        } else if (target instanceof BudgetAdjustApplyParams) {
            return buildAdjustErrorResponse((BudgetAdjustApplyParams) target, ex);
        } else if (target instanceof BudgetRenewParams) {
            // 审批/撤回没有明细，直接构建整单错误
            String errorMessage = extractErrorMessage(ex);
            return buildRenewErrorResponse((BudgetRenewParams) target, errorMessage);
        }
        
        // 默认返回通用错误响应
        String errorMessage = extractErrorMessage(ex);
        return buildGenericErrorResponse(errorMessage);
    }
    
    /**
     * 提取错误信息
     */
    private String extractErrorMessage(MethodArgumentNotValidException ex) {
        return ex.getBindingResult().getAllErrors().stream()
                .map(error -> {
                    if (error instanceof FieldError) {
                        FieldError fieldError = (FieldError) error;
                        return fieldError.getField() + ": " + fieldError.getDefaultMessage();
                    }
                    return error.getDefaultMessage();
                })
                .collect(Collectors.joining("; "));
    }

    /**
     * 处理 @Validated 注解触发的约束违反异常
     */
    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.OK)
    public Object handleConstraintViolation(ConstraintViolationException ex) {
        log.error("参数约束校验失败", ex);
        
        String errorMessage = ex.getConstraintViolations().stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.joining("; "));
        
        return buildGenericErrorResponse(errorMessage);
    }

    /**
     * 构建预算申请的错误响应（区分整单错误和明细错误）
     */
    private BudgetApplicationRespVo buildApplicationErrorResponse(BudgetApplicationParams params, MethodArgumentNotValidException ex) {
        ESBInfoParams esbInfo = params.getEsbInfo();
        ApplyReqInfoParams applyInfo = params.getApplyReqInfo();
        
        // 解析错误信息，区分整单错误和明细错误
        ValidationErrorResult errorResult = parseValidationErrors(ex, "applyReqInfo.demandDetails");
        
        String instId = esbInfo != null ? esbInfo.getInstId() : null;
        if (instId == null || instId.isEmpty()) {
            String uuid = UUID.randomUUID().toString().replace("-", "");
            instId = "N" + uuid.substring(0, 8) + "." + uuid.substring(8, 16) + ".N"
                    + uuid.substring(16, 18) + "." + uuid.substring(18, 28) + ".N" + uuid.substring(28, 32);
        }
        String responseTime = LocalDateTime.now().format(DATE_TIME_FORMATTER);
        
        // 如果有整单错误，returnMsg 显示整单错误；否则显示"部分明细校验失败"
        String returnMsg = !errorResult.documentErrors.isEmpty() 
                ? errorResult.documentErrors.stream().collect(Collectors.joining("; "))
                : "部分明细校验失败";
        
        ESBRespInfoVo esbRespInfo = ESBRespInfoVo.builder()
                .instId(instId)
                .requestTime(esbInfo != null ? esbInfo.getRequestTime() : null)
                .attr1(esbInfo != null ? esbInfo.getAttr1() : null)
                .attr2(esbInfo != null ? esbInfo.getAttr2() : null)
                .attr3(esbInfo != null ? esbInfo.getAttr3() : null)
                .returnCode("E0001-BUDGET")
                .returnMsg(returnMsg)
                .returnStatus("S")
                .responseTime(responseTime)
                .build();

        // 构建返回的需求明细列表
        List<ApplyDetailRespVo> resultDemandDetails = new ArrayList<>();
        if (applyInfo != null && applyInfo.getDemandDetails() != null) {
            for (int i = 0; i < applyInfo.getDemandDetails().size(); i++) {
                ApplyDetailDetalVo detail = applyInfo.getDemandDetails().get(i);
                ApplyDetailRespVo resultDetail = new ApplyDetailRespVo();
                BeanUtils.copyProperties(detail, resultDetail);
                
                // 确定该明细的错误信息
                String detailError = errorResult.detailErrors.get(i);
                String documentError = !errorResult.documentErrors.isEmpty() 
                        ? errorResult.documentErrors.stream().collect(Collectors.joining("; "))
                        : null;
                
                if (detailError != null && documentError != null) {
                    // 既有明细错误，又有整单错误
                    resultDetail.setValidationResult("1");
                    resultDetail.setValidationMessage(documentError + "; " + detailError);
                } else if (detailError != null) {
                    // 只有明细错误
                    resultDetail.setValidationResult("1");
                    resultDetail.setValidationMessage(detailError);
                } else if (documentError != null) {
                    // 只有整单错误
                    resultDetail.setValidationResult("1");
                    resultDetail.setValidationMessage(documentError);
                } else {
                    // 没有错误
                    resultDetail.setValidationResult("0");
                    resultDetail.setValidationMessage("校验通过 Verification Passed");
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
     * 解析校验错误，区分整单错误和明细错误
     */
    private ValidationErrorResult parseValidationErrors(MethodArgumentNotValidException ex, String detailFieldPrefix) {
        ValidationErrorResult result = new ValidationErrorResult();
        
        for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
            String field = fieldError.getField();
            String errorMsg = fieldError.getField() + ": " + fieldError.getDefaultMessage();
            
            // 判断是否为明细字段错误（格式：applyReqInfo.demandDetails[索引].字段名）
            if (field.contains(detailFieldPrefix + "[")) {
                // 提取明细索引
                int startIdx = field.indexOf(detailFieldPrefix + "[") + detailFieldPrefix.length() + 1;
                int endIdx = field.indexOf("]", startIdx);
                if (startIdx > 0 && endIdx > startIdx) {
                    try {
                        int index = Integer.parseInt(field.substring(startIdx, endIdx));
                        result.detailErrors.put(index, 
                                result.detailErrors.getOrDefault(index, "") + 
                                (result.detailErrors.containsKey(index) ? "; " : "") + errorMsg);
                    } catch (NumberFormatException e) {
                        log.warn("无法解析明细索引: {}", field);
                        result.documentErrors.add(errorMsg);
                    }
                } else {
                    result.documentErrors.add(errorMsg);
                }
            } else {
                // 整单级别的错误
                result.documentErrors.add(errorMsg);
            }
        }
        
        return result;
    }
    
    /**
     * 校验错误结果
     */
    private static class ValidationErrorResult {
        List<String> documentErrors = new ArrayList<>();  // 整单级别的错误
        java.util.Map<Integer, String> detailErrors = new java.util.HashMap<>();  // 明细级别的错误（key: 明细索引, value: 错误信息）
    }

    /**
     * 构建合同申请的错误响应（区分整单错误和明细错误）
     */
    private BudgetContractRespVo buildContractErrorResponse(BudgetContractApplyParams params, MethodArgumentNotValidException ex) {
        ESBInfoParams esbInfo = params.getEsbInfo();
        ContractApplyReqInfoParams contractInfo = params.getContractApplyReqInfo();
        
        // 解析错误信息，区分整单错误和明细错误
        ValidationErrorResult errorResult = parseValidationErrors(ex, "contractApplyReqInfo.contractDetails");
        
        String instId = esbInfo != null ? esbInfo.getInstId() : null;
        if (instId == null || instId.isEmpty()) {
            String uuid = UUID.randomUUID().toString().replace("-", "");
            instId = "N" + uuid.substring(0, 8) + "." + uuid.substring(8, 16) + ".N"
                    + uuid.substring(16, 18) + "." + uuid.substring(18, 28) + ".N" + uuid.substring(28, 32);
        }
        String responseTime = LocalDateTime.now().format(DATE_TIME_FORMATTER);
        
        String returnMsg = !errorResult.documentErrors.isEmpty() 
                ? errorResult.documentErrors.stream().collect(Collectors.joining("; "))
                : "部分明细校验失败";
        
        ESBRespInfoVo esbRespInfo = ESBRespInfoVo.builder()
                .instId(instId)
                .requestTime(esbInfo != null ? esbInfo.getRequestTime() : null)
                .attr1(esbInfo != null ? esbInfo.getAttr1() : null)
                .attr2(esbInfo != null ? esbInfo.getAttr2() : null)
                .attr3(esbInfo != null ? esbInfo.getAttr3() : null)
                .returnCode("E0001-CONTRACT")
                .returnMsg(returnMsg)
                .returnStatus("S")
                .responseTime(responseTime)
                .build();

        // 构建返回的合同明细列表
        List<ContractDetailRespVo> resultContractDetails = new ArrayList<>();
        if (contractInfo != null && contractInfo.getContractDetails() != null) {
            for (int i = 0; i < contractInfo.getContractDetails().size(); i++) {
                ContractDetailDetailVo detail = contractInfo.getContractDetails().get(i);
                ContractDetailRespVo resultDetail = new ContractDetailRespVo();
                BeanUtils.copyProperties(detail, resultDetail);
                
                String detailError = errorResult.detailErrors.get(i);
                String documentError = !errorResult.documentErrors.isEmpty() 
                        ? errorResult.documentErrors.stream().collect(Collectors.joining("; "))
                        : null;
                
                if (detailError != null && documentError != null) {
                    resultDetail.setValidationResult("1");
                    resultDetail.setValidationMessage(documentError + "; " + detailError);
                } else if (detailError != null) {
                    resultDetail.setValidationResult("1");
                    resultDetail.setValidationMessage(detailError);
                } else if (documentError != null) {
                    resultDetail.setValidationResult("1");
                    resultDetail.setValidationMessage(documentError);
                } else {
                    resultDetail.setValidationResult("0");
                    resultDetail.setValidationMessage("校验通过 Verification Passed");
                }
                
                resultContractDetails.add(resultDetail);
            }
        }

        ContractApplyResultInfoRespVo contractResult = new ContractApplyResultInfoRespVo();
        contractResult.setContractNo(contractInfo != null ? contractInfo.getContractNo() : null);
        contractResult.setProcessTime(LocalDateTime.now().format(PROCESS_TIME_FORMATTER));
        contractResult.setContractDetails(resultContractDetails);

        BudgetContractRespVo respVo = new BudgetContractRespVo();
        respVo.setEsbInfo(esbRespInfo);
        respVo.setContractApplyResult(contractResult);
        return respVo;
    }

    /**
     * 构建付款/报销申请的错误响应（区分整单错误和明细错误）
     */
    private BudgetClaimRespVo buildClaimErrorResponse(BudgetClaimApplyParams params, MethodArgumentNotValidException ex) {
        ESBInfoParams esbInfo = params.getEsbInfo();
        ClaimApplyReqInfoParams claimInfo = params.getClaimApplyReqInfo();
        
        // 解析错误信息，区分整单错误和明细错误
        ValidationErrorResult errorResult = parseValidationErrors(ex, "claimApplyReqInfo.claimDetails");
        
        String instId = esbInfo != null ? esbInfo.getInstId() : null;
        if (instId == null || instId.isEmpty()) {
            String uuid = UUID.randomUUID().toString().replace("-", "");
            instId = "N" + uuid.substring(0, 8) + "." + uuid.substring(8, 16) + ".N"
                    + uuid.substring(16, 18) + "." + uuid.substring(18, 28) + ".N" + uuid.substring(28, 32);
        }
        String responseTime = LocalDateTime.now().format(DATE_TIME_FORMATTER);
        
        String returnMsg = !errorResult.documentErrors.isEmpty() 
                ? errorResult.documentErrors.stream().collect(Collectors.joining("; "))
                : "部分明细校验失败";
        
        ESBRespInfoVo esbRespInfo = ESBRespInfoVo.builder()
                .instId(instId)
                .requestTime(esbInfo != null ? esbInfo.getRequestTime() : null)
                .attr1(esbInfo != null ? esbInfo.getAttr1() : null)
                .attr2(esbInfo != null ? esbInfo.getAttr2() : null)
                .attr3(esbInfo != null ? esbInfo.getAttr3() : null)
                .returnCode("E0001-CLAIM")
                .returnMsg(returnMsg)
                .returnStatus("S")
                .responseTime(responseTime)
                .build();

        // 构建返回的付款明细列表
        List<ClaimDetailRespVo> resultClaimDetails = new ArrayList<>();
        if (claimInfo != null && claimInfo.getClaimDetails() != null) {
            for (int i = 0; i < claimInfo.getClaimDetails().size(); i++) {
                ClaimDetailDetailVo detail = claimInfo.getClaimDetails().get(i);
                ClaimDetailRespVo resultDetail = new ClaimDetailRespVo();
                BeanUtils.copyProperties(detail, resultDetail);
                
                String detailError = errorResult.detailErrors.get(i);
                String documentError = !errorResult.documentErrors.isEmpty() 
                        ? errorResult.documentErrors.stream().collect(Collectors.joining("; "))
                        : null;
                
                if (detailError != null && documentError != null) {
                    resultDetail.setValidationResult("1");
                    resultDetail.setValidationMessage(documentError + "; " + detailError);
                } else if (detailError != null) {
                    resultDetail.setValidationResult("1");
                    resultDetail.setValidationMessage(detailError);
                } else if (documentError != null) {
                    resultDetail.setValidationResult("1");
                    resultDetail.setValidationMessage(documentError);
                } else {
                    resultDetail.setValidationResult("0");
                    resultDetail.setValidationMessage("校验通过 Verification Passed");
                }
                
                resultClaimDetails.add(resultDetail);
            }
        }

        ClaimApplyResultInfoRespVo claimResult = new ClaimApplyResultInfoRespVo();
        claimResult.setClaimOrderNo(claimInfo != null ? claimInfo.getClaimOrderNo() : null);
        claimResult.setProcessTime(LocalDateTime.now().format(PROCESS_TIME_FORMATTER));
        claimResult.setClaimDetails(resultClaimDetails);

        BudgetClaimRespVo respVo = new BudgetClaimRespVo();
        respVo.setEsbInfo(esbRespInfo);
        respVo.setClaimApplyResult(claimResult);
        return respVo;
    }

    /**
     * 构建调整申请的错误响应（区分整单错误和明细错误）
     */
    private BudgetAdjustRespVo buildAdjustErrorResponse(BudgetAdjustApplyParams params, MethodArgumentNotValidException ex) {
        ESBInfoParams esbInfo = params.getEsbInfo();
        AdjustApplyReqInfoParams adjustInfo = params.getAdjustApplyReqInfo();
        
        // 解析错误信息，区分整单错误和明细错误
        ValidationErrorResult errorResult = parseValidationErrors(ex, "adjustApplyReqInfo.adjustDetails");
        
        String instId = esbInfo != null ? esbInfo.getInstId() : null;
        if (instId == null || instId.isEmpty()) {
            String uuid = UUID.randomUUID().toString().replace("-", "");
            instId = "N" + uuid.substring(0, 8) + "." + uuid.substring(8, 16) + ".N"
                    + uuid.substring(16, 18) + "." + uuid.substring(18, 28) + ".N" + uuid.substring(28, 32);
        }
        String responseTime = LocalDateTime.now().format(DATE_TIME_FORMATTER);
        
        String returnMsg = !errorResult.documentErrors.isEmpty() 
                ? errorResult.documentErrors.stream().collect(Collectors.joining("; "))
                : "部分明细校验失败";
        
        ESBRespInfoVo esbRespInfo = ESBRespInfoVo.builder()
                .instId(instId)
                .requestTime(esbInfo != null ? esbInfo.getRequestTime() : null)
                .attr1(esbInfo != null ? esbInfo.getAttr1() : null)
                .attr2(esbInfo != null ? esbInfo.getAttr2() : null)
                .attr3(esbInfo != null ? esbInfo.getAttr3() : null)
                .returnCode("E0001-ADJUST")
                .returnMsg(returnMsg)
                .returnStatus("S")
                .responseTime(responseTime)
                .build();

        // 构建返回的调整明细列表
        List<AdjustDetailRespVo> resultAdjustDetails = new ArrayList<>();
        if (adjustInfo != null && adjustInfo.getAdjustDetails() != null) {
            for (int i = 0; i < adjustInfo.getAdjustDetails().size(); i++) {
                AdjustDetailDetailVo detail = adjustInfo.getAdjustDetails().get(i);
                AdjustDetailRespVo resultDetail = new AdjustDetailRespVo();
                BeanUtils.copyProperties(detail, resultDetail);
                
                String detailError = errorResult.detailErrors.get(i);
                String documentError = !errorResult.documentErrors.isEmpty() 
                        ? errorResult.documentErrors.stream().collect(Collectors.joining("; "))
                        : null;
                
                if (detailError != null && documentError != null) {
                    resultDetail.setValidationResult("1");
                    resultDetail.setValidationMessage(documentError + "; " + detailError);
                } else if (detailError != null) {
                    resultDetail.setValidationResult("1");
                    resultDetail.setValidationMessage(detailError);
                } else if (documentError != null) {
                    resultDetail.setValidationResult("1");
                    resultDetail.setValidationMessage(documentError);
                } else {
                    resultDetail.setValidationResult("0");
                    resultDetail.setValidationMessage("校验通过 Verification Passed");
                }
                
                resultAdjustDetails.add(resultDetail);
            }
        }

        AdjustApplyResultInfoRespVo adjustResult = new AdjustApplyResultInfoRespVo();
        adjustResult.setAdjustOrderNo(adjustInfo != null ? adjustInfo.getAdjustOrderNo() : null);
        adjustResult.setProcessTime(LocalDateTime.now().format(PROCESS_TIME_FORMATTER));
        adjustResult.setAdjustDetails(resultAdjustDetails);

        BudgetAdjustRespVo respVo = new BudgetAdjustRespVo();
        respVo.setEsbInfo(esbRespInfo);
        respVo.setAdjustApplyResult(adjustResult);
        return respVo;
    }

    /**
     * 构建审批/撤回的错误响应
     */
    private BudgetRenewRespVo buildRenewErrorResponse(BudgetRenewParams params, String errorMessage) {
        ESBInfoParams esbInfo = params.getEsbInfo();
        
        String instId = esbInfo != null ? esbInfo.getInstId() : null;
        if (instId == null || instId.isEmpty()) {
            String uuid = UUID.randomUUID().toString().replace("-", "");
            instId = "N" + uuid.substring(0, 8) + "." + uuid.substring(8, 16) + ".N"
                    + uuid.substring(16, 18) + "." + uuid.substring(18, 28) + ".N" + uuid.substring(28, 32);
        }
        String responseTime = LocalDateTime.now().format(DATE_TIME_FORMATTER);
        
        ESBRespInfoVo esbRespInfo = ESBRespInfoVo.builder()
                .instId(instId)
                .requestTime(esbInfo != null ? esbInfo.getRequestTime() : null)
                .attr1(esbInfo != null ? esbInfo.getAttr1() : null)
                .attr2(esbInfo != null ? esbInfo.getAttr2() : null)
                .attr3(esbInfo != null ? esbInfo.getAttr3() : null)
                .returnCode("E0001-BUDGET")
                .returnMsg(errorMessage)
                .returnStatus("E")
                .responseTime(responseTime)
                .build();

        BudgetRenewRespVo response = new BudgetRenewRespVo();
        response.setEsbInfo(esbRespInfo);
        return response;
    }

    /**
     * 构建通用错误响应
     */
    private Object buildGenericErrorResponse(String errorMessage) {
        String uuid = UUID.randomUUID().toString().replace("-", "");
        String instId = "N" + uuid.substring(0, 8) + "." + uuid.substring(8, 16) + ".N"
                + uuid.substring(16, 18) + "." + uuid.substring(18, 28) + ".N" + uuid.substring(28, 32);
        String responseTime = LocalDateTime.now().format(DATE_TIME_FORMATTER);
        
        ESBRespInfoVo esbRespInfo = ESBRespInfoVo.builder()
                .instId(instId)
                .requestTime(null)
                .attr1(null)
                .attr2(null)
                .attr3(null)
                .returnCode("E0001-BUDGET")
                .returnMsg(errorMessage)
                .returnStatus("E")
                .responseTime(responseTime)
                .build();

        BudgetRenewRespVo response = new BudgetRenewRespVo();
        response.setEsbInfo(esbRespInfo);
        return response;
    }
}

