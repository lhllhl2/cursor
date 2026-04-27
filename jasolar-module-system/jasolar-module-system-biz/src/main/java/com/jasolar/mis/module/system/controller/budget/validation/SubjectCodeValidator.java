package com.jasolar.mis.module.system.controller.budget.validation;

import com.jasolar.mis.module.system.config.BudgetSubjectCodeConfig;
import com.jasolar.mis.module.system.controller.budget.vo.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 科目编码校验工具类
 * 用于检查明细列表中的科目编码是否不在白名单中（不以配置的前缀开头）
 * 如果不在白名单中，将直接返回合并后的参数，不执行后续流程
 * 
 * @author Auto Generated
 * @date 2025-01-XX
 */
@Component
public class SubjectCodeValidator {
    
    @Autowired
    private BudgetSubjectCodeConfig budgetSubjectCodeConfig;
    
    /**
     * 检查调整明细列表中是否有科目编码不在白名单中（不以配置的前缀开头）
     * 
     * @param adjustDetails 调整明细列表
     * @return true 如果有任何明细的科目编码不在白名单中（需要直接返回），false 否则（继续后续流程）
     */
    public boolean hasNotInWhitelistSubjectCodeInAdjust(List<AdjustDetailDetailVo> adjustDetails) {
        if (adjustDetails == null || adjustDetails.isEmpty()) {
            return false;
        }
        
        return adjustDetails.stream()
                .map(AdjustDetailDetailVo::getBudgetSubjectCode)
                .filter(StringUtils::isNotBlank)
                .filter(code -> !"NAN-NAN".equals(code))
                .anyMatch(code -> !budgetSubjectCodeConfig.isInWhitelist(code));
    }
    
    /**
     * 检查申请明细列表中是否有科目编码不在白名单中（不以配置的前缀开头）
     * 
     * @param applyDetails 申请明细列表
     * @return true 如果有任何明细的科目编码不在白名单中（需要直接返回），false 否则（继续后续流程）
     */
    public boolean hasNotInWhitelistSubjectCodeInApply(List<ApplyDetailDetalVo> applyDetails) {
        if (applyDetails == null || applyDetails.isEmpty()) {
            return false;
        }
        
        return applyDetails.stream()
                .map(ApplyDetailDetalVo::getBudgetSubjectCode)
                .filter(StringUtils::isNotBlank)
                .filter(code -> !"NAN-NAN".equals(code))
                .anyMatch(code -> !budgetSubjectCodeConfig.isInWhitelist(code));
    }
    
    /**
     * 检查付款/报销明细列表中是否有科目编码不在白名单中（不以配置的前缀开头）
     * 
     * @param claimDetails 付款/报销明细列表
     * @return true 如果有任何明细的科目编码不在白名单中（需要直接返回），false 否则（继续后续流程）
     */
    public boolean hasNotInWhitelistSubjectCodeInClaim(List<ClaimDetailDetailVo> claimDetails) {
        if (claimDetails == null || claimDetails.isEmpty()) {
            return false;
        }
        
        return claimDetails.stream()
                .map(ClaimDetailDetailVo::getBudgetSubjectCode)
                .filter(StringUtils::isNotBlank)
                .filter(code -> !"NAN-NAN".equals(code))
                .anyMatch(code -> !budgetSubjectCodeConfig.isInWhitelist(code));
    }
    
    /**
     * 检查合同明细列表中是否有科目编码不在白名单中（不以配置的前缀开头）
     * 
     * @param contractDetails 合同明细列表
     * @return true 如果有任何明细的科目编码不在白名单中（需要直接返回），false 否则（继续后续流程）
     */
    public boolean hasNotInWhitelistSubjectCodeInContract(List<ContractDetailDetailVo> contractDetails) {
        if (contractDetails == null || contractDetails.isEmpty()) {
            return false;
        }
        
        return contractDetails.stream()
                .map(ContractDetailDetailVo::getBudgetSubjectCode)
                .filter(StringUtils::isNotBlank)
                .filter(code -> !"NAN-NAN".equals(code))
                .anyMatch(code -> !budgetSubjectCodeConfig.isInWhitelist(code));
    }
    
    /**
     * 检查查询明细列表中是否有科目编码不在白名单中（不以配置的前缀开头）
     * 
     * @param queryDetails 查询明细列表
     * @return true 如果有任何明细的科目编码不在白名单中（需要直接返回），false 否则（继续后续流程）
     */
    public boolean hasNotInWhitelistSubjectCodeInQuery(List<QueryDetailDetailVo> queryDetails) {
        if (queryDetails == null || queryDetails.isEmpty()) {
            return false;
        }
        
        return queryDetails.stream()
                .map(QueryDetailDetailVo::getBudgetSubjectCode)
                .filter(StringUtils::isNotBlank)
                .filter(code -> !"NAN-NAN".equals(code))
                .anyMatch(code -> !budgetSubjectCodeConfig.isInWhitelist(code));
    }
    
    /**
     * 过滤出调整明细列表中在白名单中的明细（用于预算校验处理）
     * 规则：
     * 1. 科目编码为空、为 "NAN-NAN" 或在白名单中的明细，需要校验
     * 2. 带项目的明细（masterProjectCode 不为空且不是 "NAN"），即使科目编码不在白名单中，也需要校验
     * 
     * @param adjustDetails 调整明细列表
     * @return 白名单中的明细列表
     */
    public List<AdjustDetailDetailVo> filterWhitelistAdjustDetails(List<AdjustDetailDetailVo> adjustDetails) {
        if (adjustDetails == null || adjustDetails.isEmpty()) {
            return new ArrayList<>();
        }
        
        return adjustDetails.stream()
                .filter(detail -> {
                    String code = detail.getBudgetSubjectCode();
                    String masterProjectCode = detail.getMasterProjectCode();
                    
                    // 科目编码在白名单中（为空、为 "NAN-NAN" 或在配置的白名单中）
                    boolean isSubjectCodeInWhitelist = StringUtils.isBlank(code) || "NAN-NAN".equals(code) || budgetSubjectCodeConfig.isInWhitelist(code);
                    
                    // 带项目的明细（masterProjectCode 不为空且不是 "NAN"），即使科目编码不在白名单中，也需要校验
                    boolean hasProjectCode = StringUtils.isNotBlank(masterProjectCode) && !"NAN".equals(masterProjectCode);
                    
                    return isSubjectCodeInWhitelist || hasProjectCode;
                })
                .collect(Collectors.toList());
    }
    
    /**
     * 过滤出申请明细列表中在白名单中的明细（用于预算校验处理）
     * 规则：
     * 1. 科目编码为空、为 "NAN-NAN" 或在白名单中的明细，需要校验
     * 2. 带项目的明细（masterProjectCode 不为空且不是 "NAN"），即使科目编码不在白名单中，也需要校验
     * 
     * @param applyDetails 申请明细列表
     * @return 白名单中的明细列表
     */
    public List<ApplyDetailDetalVo> filterWhitelistApplyDetails(List<ApplyDetailDetalVo> applyDetails) {
        if (applyDetails == null || applyDetails.isEmpty()) {
            return new ArrayList<>();
        }
        
        return applyDetails.stream()
                .filter(detail -> {
                    String code = detail.getBudgetSubjectCode();
                    String masterProjectCode = detail.getMasterProjectCode();
                    
                    // 科目编码在白名单中（为空、为 "NAN-NAN" 或在配置的白名单中）
                    boolean isSubjectCodeInWhitelist = StringUtils.isBlank(code) || "NAN-NAN".equals(code) || budgetSubjectCodeConfig.isInWhitelist(code);
                    
                    // 带项目的明细（masterProjectCode 不为空且不是 "NAN"），即使科目编码不在白名单中，也需要校验
                    boolean hasProjectCode = StringUtils.isNotBlank(masterProjectCode) && !"NAN".equals(masterProjectCode);
                    
                    return isSubjectCodeInWhitelist || hasProjectCode;
                })
                .collect(Collectors.toList());
    }
    
    /**
     * 过滤出付款/报销明细列表中在白名单中的明细（用于预算校验处理）
     * 规则：
     * 1. 科目编码为空、为 "NAN-NAN" 或在白名单中的明细，需要校验
     * 2. 带项目的明细（masterProjectCode 不为空且不是 "NAN"），即使科目编码不在白名单中，也需要校验
     * 
     * @param claimDetails 付款/报销明细列表
     * @return 白名单中的明细列表
     */
    public List<ClaimDetailDetailVo> filterWhitelistClaimDetails(List<ClaimDetailDetailVo> claimDetails) {
        if (claimDetails == null || claimDetails.isEmpty()) {
            return new ArrayList<>();
        }
        
        return claimDetails.stream()
                .filter(detail -> {
                    String code = detail.getBudgetSubjectCode();
                    String masterProjectCode = detail.getMasterProjectCode();
                    
                    // 科目编码在白名单中（为空、为 "NAN-NAN" 或在配置的白名单中）
                    boolean isSubjectCodeInWhitelist = StringUtils.isBlank(code) || "NAN-NAN".equals(code) || budgetSubjectCodeConfig.isInWhitelist(code);
                    
                    // 带项目的明细（masterProjectCode 不为空且不是 "NAN"），即使科目编码不在白名单中，也需要校验
                    boolean hasProjectCode = StringUtils.isNotBlank(masterProjectCode) && !"NAN".equals(masterProjectCode);
                    
                    return isSubjectCodeInWhitelist || hasProjectCode;
                })
                .collect(Collectors.toList());
    }
    
    /**
     * 过滤出合同明细列表中在白名单中的明细（用于预算校验处理）
     * 规则：
     * 1. 科目编码为空、为 "NAN-NAN" 或在白名单中的明细，需要校验
     * 2. 带项目的明细（masterProjectCode 不为空且不是 "NAN"），即使科目编码不在白名单中，也需要校验
     * 
     * @param contractDetails 合同明细列表
     * @return 白名单中的明细列表
     */
    public List<ContractDetailDetailVo> filterWhitelistContractDetails(List<ContractDetailDetailVo> contractDetails) {
        if (contractDetails == null || contractDetails.isEmpty()) {
            return new ArrayList<>();
        }
        
        return contractDetails.stream()
                .filter(detail -> {
                    String code = detail.getBudgetSubjectCode();
                    String masterProjectCode = detail.getMasterProjectCode();
                    
                    // 科目编码在白名单中（为空、为 "NAN-NAN" 或在配置的白名单中）
                    boolean isSubjectCodeInWhitelist = StringUtils.isBlank(code) || "NAN-NAN".equals(code) || budgetSubjectCodeConfig.isInWhitelist(code);
                    
                    // 带项目的明细（masterProjectCode 不为空且不是 "NAN"），即使科目编码不在白名单中，也需要校验
                    boolean hasProjectCode = StringUtils.isNotBlank(masterProjectCode) && !"NAN".equals(masterProjectCode);
                    
                    return isSubjectCodeInWhitelist || hasProjectCode;
                })
                .collect(Collectors.toList());
    }
    
    /**
     * 过滤出查询明细列表中在白名单中的明细（用于预算校验处理）
     * 规则：
     * 1. 科目编码为空、为 "NAN-NAN" 或在白名单中的明细，需要校验
     * 2. 带项目的明细（masterProjectCode 不为空且不是 "NAN"），即使科目编码不在白名单中，也需要校验
     * 
     * @param queryDetails 查询明细列表
     * @return 白名单中的明细列表
     */
    public List<QueryDetailDetailVo> filterWhitelistQueryDetails(List<QueryDetailDetailVo> queryDetails) {
        if (queryDetails == null || queryDetails.isEmpty()) {
            return new ArrayList<>();
        }
        
        return queryDetails.stream()
                .filter(detail -> {
                    String code = detail.getBudgetSubjectCode();
                    String masterProjectCode = detail.getMasterProjectCode();
                    
                    // 科目编码在白名单中（为空、为 "NAN-NAN" 或在配置的白名单中）
                    boolean isSubjectCodeInWhitelist = StringUtils.isBlank(code) || "NAN-NAN".equals(code) || budgetSubjectCodeConfig.isInWhitelist(code);
                    
                    // 带项目的明细（masterProjectCode 不为空且不是 "NAN"），即使科目编码不在白名单中，也需要校验
                    boolean hasProjectCode = StringUtils.isNotBlank(masterProjectCode) && !"NAN".equals(masterProjectCode);
                    
                    return isSubjectCodeInWhitelist || hasProjectCode;
                })
                .collect(Collectors.toList());
    }
}

