package com.jasolar.mis.module.system.controller.budget.commonapi;

import com.jasolar.mis.module.system.controller.budget.validation.DetailMerger;
import com.jasolar.mis.module.system.controller.budget.validation.SubjectCodeValidator;
import com.jasolar.mis.module.system.controller.budget.validation.BudgetResponseBuilder;
import com.jasolar.mis.module.system.controller.budget.vo.*;
import com.jasolar.mis.module.system.service.budget.adjust.BudgetAdjustService;
import com.jasolar.mis.module.system.config.BudgetSubjectCodeConfig;
import com.jasolar.mis.framework.redis.lock.RedisLockExecutor;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.concurrent.TimeUnit;


/**
 * Description: 预算调整控制器
 * Author : Auto Generated
 * Date : 2025-01-XX
 * Version : 1.0
 */
@RestController
@RequestMapping(value = "/budget/adjust")
@Slf4j
@Api(tags = "预算调整")
public class BudgetAdjustController {

    @Resource
    private BudgetAdjustService budgetAdjustService;
    
    @Resource
    private SubjectCodeValidator subjectCodeValidator;
    
    @Resource
    private BudgetSubjectCodeConfig budgetSubjectCodeConfig;
    
    @Resource
    private RedisLockExecutor redisLockExecutor;

    /**
     * 预算调整的申请操作
     * 
     * @param budgetAdjustApplyParams 预算调整申请参数
     * @return 预算调整响应
     */
    @PostMapping("/apply")
    @ApiOperation("预算调整申请")
    public BudgetAdjustRespVo apply(@RequestBody @Valid BudgetAdjustApplyParams budgetAdjustApplyParams) {
        log.info("预算调整申请参数 --> {}", budgetAdjustApplyParams);
        
        // 使用全局锁，确保同一时间只有一个请求能进入此接口
        String lockKey = "LOCK:BUDGET:ADJUST:APPLY";
        
        // 使用分布式锁包装业务逻辑，等待时间设置为较长（30秒），锁持有时间设置为较长（300秒），确保业务逻辑执行完成
        BudgetAdjustRespVo result = redisLockExecutor.tryExecute(lockKey, () -> {
            // 合并相同维度的明细
            if (budgetAdjustApplyParams.getAdjustApplyReqInfo() != null 
                && budgetAdjustApplyParams.getAdjustApplyReqInfo().getAdjustDetails() != null) {
                
                // 保存原始明细列表（合并前），用于返回时映射
                List<com.jasolar.mis.module.system.controller.budget.vo.AdjustDetailDetailVo> originalDetails = 
                    new java.util.ArrayList<>(budgetAdjustApplyParams.getAdjustApplyReqInfo().getAdjustDetails());
                
                // OA 和 HLY 都执行合并逻辑（统一处理）
                DetailMerger.MergeResult<com.jasolar.mis.module.system.controller.budget.vo.AdjustDetailDetailVo> mergeResult = 
                    DetailMerger.mergeAdjustDetails(
                        budgetAdjustApplyParams.getAdjustApplyReqInfo().getAdjustDetails(),
                        budgetAdjustApplyParams.getAdjustApplyReqInfo().getIsInternal()
                    );
                
                // 构建映射关系：原始明细索引 -> 合并后明细索引
                // 用于返回时将合并后的校验结果映射回原始明细
                java.util.Map<Integer, Integer> originalToMergedIndexMap = new java.util.HashMap<>();
                if (mergeResult.hasMerged()) {
                    // 如果发生了合并，需要构建映射关系
                    List<com.jasolar.mis.module.system.controller.budget.vo.AdjustDetailDetailVo> mergedDetails = mergeResult.getMergedDetails();
                    
                    // 使用维度key来匹配原始明细和合并后明细
                    java.util.Map<String, Integer> dimensionKeyToMergedIndex = new java.util.HashMap<>();
                    for (int i = 0; i < mergedDetails.size(); i++) {
                        com.jasolar.mis.module.system.controller.budget.vo.AdjustDetailDetailVo mergedDetail = mergedDetails.get(i);
                        // 提取维度key（复用DetailMerger的逻辑）
                        String dimensionKey = buildDimensionKey(mergedDetail, budgetAdjustApplyParams.getAdjustApplyReqInfo().getIsInternal());
                        dimensionKeyToMergedIndex.put(dimensionKey, i);
                    }
                    
                    // 为每个原始明细找到对应的合并后明细索引
                    for (int i = 0; i < originalDetails.size(); i++) {
                        com.jasolar.mis.module.system.controller.budget.vo.AdjustDetailDetailVo originalDetail = originalDetails.get(i);
                        String dimensionKey = buildDimensionKey(originalDetail, budgetAdjustApplyParams.getAdjustApplyReqInfo().getIsInternal());
                        Integer mergedIndex = dimensionKeyToMergedIndex.get(dimensionKey);
                        if (mergedIndex != null) {
                            originalToMergedIndexMap.put(i, mergedIndex);
                        }
                    }
                    
                    // 更新明细列表为合并后的明细
                    budgetAdjustApplyParams.getAdjustApplyReqInfo().setAdjustDetails(mergedDetails);
                } else {
                    // 没有合并，原始索引和合并后索引一一对应
                    for (int i = 0; i < originalDetails.size(); i++) {
                        originalToMergedIndexMap.put(i, i);
                    }
                }
                
                // 不再过滤，将所有明细传给Service，Service层会识别不受控的明细并处理
                // Service层会跳过不受控明细的预算校验和预算余额更新，但仍保存数据到BUDGET_LEDGER表
                List<com.jasolar.mis.module.system.controller.budget.vo.AdjustDetailDetailVo> mergedDetails = 
                    budgetAdjustApplyParams.getAdjustApplyReqInfo().getAdjustDetails();
                
                // 直接调用Service，传递所有明细（包括不受控的）
                BudgetAdjustRespVo adjustResult = budgetAdjustService.apply(budgetAdjustApplyParams);
                
                // 根据 dataSource 决定返回逻辑
                String dataSource = budgetAdjustApplyParams.getAdjustApplyReqInfo().getDataSource();
                if ("OA".equalsIgnoreCase(dataSource)) {
                    // OA：返回时映射回原始明细（保持原始条数）
                    if (adjustResult != null && adjustResult.getAdjustApplyResult() != null) {
                        List<AdjustDetailRespVo> whitelistResultDetails = adjustResult.getAdjustApplyResult().getAdjustDetails();
                        if (whitelistResultDetails != null && !whitelistResultDetails.isEmpty()) {
                            // 构建通过detailLineNo的映射：detailLineNo -> AdjustDetailRespVo（用于直接匹配错误信息）
                            java.util.Map<String, AdjustDetailRespVo> detailLineNoToResultMap = new java.util.HashMap<>();
                            for (AdjustDetailRespVo resultDetail : whitelistResultDetails) {
                                // Service层返回的AdjustDetailRespVo继承自AdjustDetailDetailVo，包含adjustDetailLineNo字段
                                if (resultDetail.getAdjustDetailLineNo() != null) {
                                    detailLineNoToResultMap.put(resultDetail.getAdjustDetailLineNo(), resultDetail);
                                }
                            }
                            
                            // 构建白名单明细的映射：合并后索引 -> 白名单结果索引
                            java.util.Map<Integer, Integer> mergedToWhitelistResultIndexMap = new java.util.HashMap<>();
                            int whitelistResultIndex = 0;
                            for (int i = 0; i < mergedDetails.size(); i++) {
                                com.jasolar.mis.module.system.controller.budget.vo.AdjustDetailDetailVo mergedDetail = mergedDetails.get(i);
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
                            List<AdjustDetailRespVo> finalDetails = new java.util.ArrayList<>();
                            for (int i = 0; i < originalDetails.size(); i++) {
                                com.jasolar.mis.module.system.controller.budget.vo.AdjustDetailDetailVo originalDetail = originalDetails.get(i);
                                String code = originalDetail.getBudgetSubjectCode();
                                String masterProjectCode = originalDetail.getMasterProjectCode();
                                String detailLineNo = originalDetail.getAdjustDetailLineNo();
                                
                                // 科目编码在白名单中（为空、为 "NAN-NAN" 或在配置的白名单中）
                                boolean isSubjectCodeInWhitelist = StringUtils.isBlank(code) || "NAN-NAN".equals(code) || budgetSubjectCodeConfig.isInWhitelist(code);
                                
                                // 带项目的明细（masterProjectCode 不为空且不是 "NAN"），即使科目编码不在白名单中，也需要校验
                                boolean hasProjectCode = StringUtils.isNotBlank(masterProjectCode) && !"NAN".equals(masterProjectCode);
                                
                                boolean isInWhitelist = isSubjectCodeInWhitelist || hasProjectCode;
                                
                                // 优先通过detailLineNo直接匹配（确保能获取到Service层的错误信息）
                                AdjustDetailRespVo matchedResult = null;
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
                                    AdjustDetailRespVo originalResultDetail = new AdjustDetailRespVo();
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
                                    finalDetails.add(originalResultDetail);
                                } else if (isInWhitelist) {
                                    // 在白名单中，但通过detailLineNo和索引映射都没找到，创建成功响应
                                    // 这种情况理论上不应该发生，但为了容错性保留此逻辑
                                    AdjustDetailRespVo skipDetail = new AdjustDetailRespVo();
                                    BeanUtils.copyProperties(originalDetail, skipDetail);
                                    if (originalDetail.getMetadata() != null) {
                                        skipDetail.setMetadata(new java.util.HashMap<>(originalDetail.getMetadata()));
                                    }
                                    skipDetail.setValidationResult("0");
                                    skipDetail.setValidationMessage("处理成功 Successfully Processed");
                                    finalDetails.add(skipDetail);
                                } else {
                                    // 不在白名单中，创建成功响应（跳过预算校验）
                                    AdjustDetailRespVo skipDetail = new AdjustDetailRespVo();
                                    BeanUtils.copyProperties(originalDetail, skipDetail);
                                    if (originalDetail.getMetadata() != null) {
                                        skipDetail.setMetadata(new java.util.HashMap<>(originalDetail.getMetadata()));
                                    }
                                    skipDetail.setValidationResult("0");
                                    skipDetail.setValidationMessage("处理成功 Successfully Processed");
                                    finalDetails.add(skipDetail);
                                }
                            }
                            adjustResult.getAdjustApplyResult().setAdjustDetails(finalDetails);
                        }
                    }
                }
                // HLY：直接返回合并后的结果（保持原有逻辑，不映射）
                
                return adjustResult;
            }
            
            return budgetAdjustService.apply(budgetAdjustApplyParams);
        }, TimeUnit.SECONDS.toMillis(30), TimeUnit.SECONDS.toMillis(300));
        
        // 如果获取锁失败，返回锁失败响应
        if (result == null) {
            log.warn("获取分布式锁失败，接口正在处理其他请求");
            return BudgetResponseBuilder.buildAdjustLockFailedResponse(budgetAdjustApplyParams);
        }
        
        return result;
    }


    /**
     * 预算调整的审批及撤回操作
     * 
     * @param budgetAdjustRenewParams 预算调整审批/撤回参数
     * @return 预算调整审批/撤回响应
     */
    @PostMapping("/authOrCancel")
    @ApiOperation("预算调整审批/撤回")
    public BudgetAdjustRenewRespVo authOrCancel(@RequestBody @Valid BudgetAdjustRenewParams budgetAdjustRenewParams) {
        log.info("预算调整审批/撤回参数 --> {}", budgetAdjustRenewParams);
        
        // 使用全局锁，确保同一时间只有一个请求能进入此接口
        String lockKey = "LOCK:BUDGET:ADJUST:AUTH_OR_CANCEL";
        
        // 使用分布式锁包装业务逻辑，等待时间设置为较长（30秒），锁持有时间设置为较长（300秒），确保业务逻辑执行完成
        BudgetAdjustRenewRespVo result = redisLockExecutor.tryExecute(lockKey, () -> {
            return budgetAdjustService.authOrCancel(budgetAdjustRenewParams);
        }, TimeUnit.SECONDS.toMillis(30), TimeUnit.SECONDS.toMillis(300));
        
        // 如果获取锁失败，抛出异常
        if (result == null) {
            log.warn("获取分布式锁失败，接口正在处理其他请求");
            throw new IllegalStateException("接口正在处理其他请求，请稍后重试");
        }
        
        return result;
    }
    
    /**
     * 构建调整明细的维度key（用于匹配原始明细和合并后明细）
     * 规则与DetailMerger中的DetailDimension.getDimensionKey()保持一致
     */
    private String buildDimensionKey(com.jasolar.mis.module.system.controller.budget.vo.AdjustDetailDetailVo detail, String docIsInternal) {
        String managementOrg = detail.getManagementOrg();
        String budgetSubjectCode = detail.getBudgetSubjectCode();
        String masterProjectCode = detail.getMasterProjectCode();
        String erpAssetType = detail.getErpAssetType();
        String isInternal = docIsInternal; // 调整明细使用单据级的isInternal
        String effectType = detail.getEffectType();
        
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
            // 非调整明细：不包含 effectType（理论上调整明细应该有effectType，这里做兼容处理）
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
     * 标准化字符串（处理空值和null）
     */
    private String normalize(String value) {
        if (StringUtils.isBlank(value)) {
            return "";
        }
        return value.trim();
    }
}

