package com.jasolar.mis.module.system.controller.budget.commonapi;

import com.jasolar.mis.module.system.controller.budget.validation.DetailMerger;
import com.jasolar.mis.module.system.controller.budget.validation.SubjectCodeValidator;
import com.jasolar.mis.module.system.controller.budget.validation.BudgetResponseBuilder;
import com.jasolar.mis.module.system.controller.budget.vo.*;
import com.jasolar.mis.module.system.service.budget.application.BudgetApplicationService;
import com.jasolar.mis.module.system.config.BudgetSubjectCodeConfig;
import com.jasolar.mis.framework.redis.lock.RedisLockExecutor;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.annotation.Resource;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Description: 预算申请控制器
 * Author : Auto Generated
 * Date : 2025-01-XX
 * Version : 1.0
 */
@RestController
@RequestMapping(value = "/budget/application")
@Slf4j
@Api(tags = "预算申请")
public class BudgetApplicationController {


    @Resource
    private BudgetApplicationService budgetApplicationService;
    
    @Resource
    private SubjectCodeValidator subjectCodeValidator;
    
    @Resource
    private BudgetSubjectCodeConfig budgetSubjectCodeConfig;
    
    @Resource
    private RedisLockExecutor redisLockExecutor;

    /**
     * 申请单创建
     * 
     * @param budgetApplicationParams 预算申请参数
     * @return 预算申请响应
     */
    @PostMapping("/apply")
    @ApiOperation("预算申请创建")
    public BudgetApplicationRespVo apply(@RequestBody @Valid BudgetApplicationParams budgetApplicationParams) {
        log.info("预算申请参数 --> {}", budgetApplicationParams);
        
        // 使用全局锁，确保同一时间只有一个请求能进入此接口
        String lockKey = "LOCK:BUDGET:APPLICATION:APPLY";
        
        // 使用分布式锁包装业务逻辑，等待时间设置为较长（30秒），锁持有时间设置为较长（300秒），确保业务逻辑执行完成
        BudgetApplicationRespVo result = redisLockExecutor.tryExecute(lockKey, () -> {
            // 合并相同维度的明细
            if (budgetApplicationParams.getApplyReqInfo() != null 
                && budgetApplicationParams.getApplyReqInfo().getDemandDetails() != null) {
                
                // 保存原始明细列表（合并前），用于返回时映射
                List<ApplyDetailDetalVo> originalDetails = 
                    new java.util.ArrayList<>(budgetApplicationParams.getApplyReqInfo().getDemandDetails());
                
                // OA 和 HLY 都执行合并逻辑（统一处理）
                DetailMerger.MergeResult<ApplyDetailDetalVo> mergeResult = 
                    DetailMerger.mergeApplyDetails(
                        budgetApplicationParams.getApplyReqInfo().getDemandDetails()
                    );
                
                // 构建映射关系：原始明细索引 -> 合并后明细索引
                java.util.Map<Integer, Integer> originalToMergedIndexMap = new java.util.HashMap<>();
                if (mergeResult.hasMerged()) {
                    // 如果发生了合并，需要构建映射关系
                    List<ApplyDetailDetalVo> mergedDetails = mergeResult.getMergedDetails();
                    
                    // 使用维度key来匹配原始明细和合并后明细
                    java.util.Map<String, Integer> dimensionKeyToMergedIndex = new java.util.HashMap<>();
                    for (int i = 0; i < mergedDetails.size(); i++) {
                        ApplyDetailDetalVo mergedDetail = mergedDetails.get(i);
                        String dimensionKey = buildDimensionKey(mergedDetail);
                        dimensionKeyToMergedIndex.put(dimensionKey, i);
                    }
                    
                    // 为每个原始明细找到对应的合并后明细索引
                    for (int i = 0; i < originalDetails.size(); i++) {
                        ApplyDetailDetalVo originalDetail = originalDetails.get(i);
                        String dimensionKey = buildDimensionKey(originalDetail);
                        Integer mergedIndex = dimensionKeyToMergedIndex.get(dimensionKey);
                        if (mergedIndex != null) {
                            originalToMergedIndexMap.put(i, mergedIndex);
                        }
                    }
                    
                    // 更新明细列表为合并后的明细
                    budgetApplicationParams.getApplyReqInfo().setDemandDetails(mergedDetails);
                } else {
                    // 没有合并，原始索引和合并后索引一一对应
                    for (int i = 0; i < originalDetails.size(); i++) {
                        originalToMergedIndexMap.put(i, i);
                    }
                }
                
                // 不再过滤，将所有明细传给Service，Service层会识别不受控的明细并处理
                // Service层会跳过不受控明细的预算校验和预算余额更新，但仍保存数据到BUDGET_LEDGER表
                List<ApplyDetailDetalVo> mergedDetails = 
                    budgetApplicationParams.getApplyReqInfo().getDemandDetails();
                
                // 直接调用Service，传递所有明细（包括不受控的）
                BudgetApplicationRespVo applyResult = budgetApplicationService.apply(budgetApplicationParams);
                
                // 根据 dataSource 决定返回逻辑
                String dataSource = budgetApplicationParams.getApplyReqInfo().getDataSource();
                if ("OA".equalsIgnoreCase(dataSource)) {
                    // OA：返回时映射回原始明细（保持原始条数）
                    if (applyResult != null && applyResult.getApplyResult() != null) {
                        List<ApplyDetailRespVo> whitelistResultDetails = applyResult.getApplyResult().getDemandDetails();
                    if (whitelistResultDetails != null && !whitelistResultDetails.isEmpty()) {
                        // 构建通过detailLineNo的映射：detailLineNo -> ApplyDetailRespVo（用于直接匹配错误信息）
                        java.util.Map<String, ApplyDetailRespVo> detailLineNoToResultMap = new java.util.HashMap<>();
                        for (ApplyDetailRespVo resultDetail : whitelistResultDetails) {
                            // Service层返回的ApplyDetailRespVo继承自ApplyDetailDetalVo，包含demandDetailLineNo字段
                            if (resultDetail.getDemandDetailLineNo() != null) {
                                detailLineNoToResultMap.put(resultDetail.getDemandDetailLineNo(), resultDetail);
                            }
                        }
                        
                        // 构建白名单明细的映射：合并后索引 -> 白名单结果索引
                        java.util.Map<Integer, Integer> mergedToWhitelistResultIndexMap = new java.util.HashMap<>();
                        int whitelistResultIndex = 0;
                        for (int i = 0; i < mergedDetails.size(); i++) {
                            ApplyDetailDetalVo mergedDetail = mergedDetails.get(i);
                            String code = mergedDetail.getBudgetSubjectCode();
                            String masterProjectCode = mergedDetail.getMasterProjectCode();
                            
                            // 科目编码在白名单中（为空、为 "NAN-NAN" 或在配置的白名单中）
                            boolean isSubjectCodeInWhitelist = StringUtils.isBlank(code) || "NAN-NAN".equals(code) || budgetSubjectCodeConfig.isInWhitelist(code);
                            
                            // 带项目的明细（masterProjectCode 不为空且不是 "NAN"），即使科目编码不在白名单中，也需要校验
                            boolean hasProjectCode = StringUtils.isNotBlank(masterProjectCode) && !"NAN".equals(masterProjectCode);
                            
                            boolean isInWhitelist = isSubjectCodeInWhitelist || hasProjectCode;
                            if (isInWhitelist) {
                                mergedToWhitelistResultIndexMap.put(i, whitelistResultIndex++);
                            }
                        }
                        
                        // 为所有原始明细设置校验结果
                        List<ApplyDetailRespVo> finalDetails = new java.util.ArrayList<>();
                        for (int i = 0; i < originalDetails.size(); i++) {
                            ApplyDetailDetalVo originalDetail = originalDetails.get(i);
                            String code = originalDetail.getBudgetSubjectCode();
                            String masterProjectCode = originalDetail.getMasterProjectCode();
                            String detailLineNo = originalDetail.getDemandDetailLineNo();
                            
                            // 科目编码在白名单中（为空、为 "NAN-NAN" 或在配置的白名单中）
                            boolean isSubjectCodeInWhitelist = StringUtils.isBlank(code) || "NAN-NAN".equals(code) || budgetSubjectCodeConfig.isInWhitelist(code);
                            
                            // 带项目的明细（masterProjectCode 不为空且不是 "NAN"），即使科目编码不在白名单中，也需要校验
                            boolean hasProjectCode = StringUtils.isNotBlank(masterProjectCode) && !"NAN".equals(masterProjectCode);
                            
                            boolean isInWhitelist = isSubjectCodeInWhitelist || hasProjectCode;
                            
                            // 优先通过detailLineNo直接匹配（确保能获取到Service层的错误信息）
                            ApplyDetailRespVo matchedResult = null;
                            if (StringUtils.isNotBlank(detailLineNo)) {
                                matchedResult = detailLineNoToResultMap.get(detailLineNo);
                            }
                            
                            // 如果通过detailLineNo没找到，尝试通过索引映射（处理合并的情况）
                            if (matchedResult == null && isInWhitelist) {
                                Integer mergedIndex = originalToMergedIndexMap.get(i);
                                if (mergedIndex != null) {
                                    Integer whitelistIndex = mergedToWhitelistResultIndexMap.get(mergedIndex);
                                    if (whitelistIndex != null && whitelistIndex < whitelistResultDetails.size()) {
                                        matchedResult = whitelistResultDetails.get(whitelistIndex);
                                    }
                                }
                            }
                            
                            if (matchedResult != null) {
                                // 找到了匹配的结果（可能包含错误信息），使用该结果
                                ApplyDetailRespVo originalResultDetail = new ApplyDetailRespVo();
                                // 复制原始明细的基础信息
                                BeanUtils.copyProperties(originalDetail, originalResultDetail);
                                if (originalDetail.getMetadata() != null) {
                                    originalResultDetail.setMetadata(new java.util.HashMap<>(originalDetail.getMetadata()));
                                }
                                // 使用匹配到的校验结果（保留Service层的错误信息）
                                originalResultDetail.setValidationResult(matchedResult.getValidationResult());
                                originalResultDetail.setValidationMessage(matchedResult.getValidationMessage());
                                // 保留其他返回字段
                                originalResultDetail.setManagementOrgName(matchedResult.getManagementOrgName());
                                originalResultDetail.setBudgetSubjectName(matchedResult.getBudgetSubjectName());
                                originalResultDetail.setAvailableBudgetRatio(matchedResult.getAvailableBudgetRatio());
                                originalResultDetail.setAmountAvailable(matchedResult.getAmountAvailable());
                                finalDetails.add(originalResultDetail);
                            } else if (isInWhitelist) {
                                // 在白名单中，但通过detailLineNo和索引映射都没找到，创建成功响应
                                // 这种情况理论上不应该发生，但为了容错性保留此逻辑
                                ApplyDetailRespVo skipDetail = new ApplyDetailRespVo();
                                BeanUtils.copyProperties(originalDetail, skipDetail);
                                if (originalDetail.getMetadata() != null) {
                                    skipDetail.setMetadata(new java.util.HashMap<>(originalDetail.getMetadata()));
                                }
                                skipDetail.setValidationResult("0");
                                skipDetail.setValidationMessage("该科目不在控制范围内,直接通过 Successfully Processed");
                                finalDetails.add(skipDetail);
                            } else {
                                // 不在白名单中，创建成功响应（跳过预算校验）
                                ApplyDetailRespVo skipDetail = new ApplyDetailRespVo();
                                BeanUtils.copyProperties(originalDetail, skipDetail);
                                if (originalDetail.getMetadata() != null) {
                                    skipDetail.setMetadata(new java.util.HashMap<>(originalDetail.getMetadata()));
                                }
                                skipDetail.setValidationResult("0");
                                skipDetail.setValidationMessage("该科目不在控制范围内,直接通过 Successfully Processed");
                                finalDetails.add(skipDetail);
                            }
                        }
                        applyResult.getApplyResult().setDemandDetails(finalDetails);
                    }
                    }
                }
                // HLY：直接返回合并后的结果（保持原有逻辑，不映射）
                
                return applyResult;
            }
            
            return budgetApplicationService.apply(budgetApplicationParams);
        }, TimeUnit.SECONDS.toMillis(30), TimeUnit.SECONDS.toMillis(300));
        
        // 如果获取锁失败，返回锁失败响应
        if (result == null) {
            log.warn("获取分布式锁失败，接口正在处理其他请求");
            return BudgetResponseBuilder.buildApplicationLockFailedResponse(budgetApplicationParams);
        }
        
        return result;
    }


    /**
     * 事前申请的审批和撤回操作
     * 
     * @param budgetRenewParams 预算事前申请审批/撤回参数
     * @return 预算事前申请审批/撤回响应
     */
    @PostMapping("/authOrCancel")
    @ApiOperation("预算申请审批/撤回")
    public BudgetRenewRespVo authOrCancel(@RequestBody @Valid BudgetRenewParams budgetRenewParams) {
        log.info("预算事前申请审批/撤回参数 --> {}", budgetRenewParams);
        
        // 使用全局锁，确保同一时间只有一个请求能进入此接口
        String lockKey = "LOCK:BUDGET:APPLICATION:AUTH_OR_CANCEL";
        
        // 使用分布式锁包装业务逻辑，等待时间设置为较长（30秒），锁持有时间设置为较长（300秒），确保业务逻辑执行完成
        BudgetRenewRespVo result = redisLockExecutor.tryExecute(lockKey, () -> {
            return budgetApplicationService.renew(budgetRenewParams);
        }, TimeUnit.SECONDS.toMillis(30), TimeUnit.SECONDS.toMillis(300));
        
        // 如果获取锁失败，抛出异常
        if (result == null) {
            log.warn("获取分布式锁失败，接口正在处理其他请求");
            throw new IllegalStateException("接口正在处理其他请求，请稍后重试");
        }
        
        return result;
    }
    
    /**
     * 构建申请明细的维度key（用于匹配原始明细和合并后明细）
     * 规则与DetailMerger中的DetailDimension.getDimensionKey()保持一致
     */
    private String buildDimensionKey(ApplyDetailDetalVo detail) {
        String managementOrg = detail.getManagementOrg();
        String budgetSubjectCode = detail.getBudgetSubjectCode();
        String masterProjectCode = detail.getMasterProjectCode();
        String erpAssetType = detail.getErpAssetType();
        String isInternal = detail.getIsInternal();
        
        boolean hasProjectCode = !isProjectCodeEmpty(masterProjectCode);
        
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
    
    /**
     * 判断项目编码是否为空（包括"NAN"）
     */
    private boolean isProjectCodeEmpty(String projectCode) {
        return StringUtils.isBlank(projectCode) || "NAN".equals(projectCode);
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

