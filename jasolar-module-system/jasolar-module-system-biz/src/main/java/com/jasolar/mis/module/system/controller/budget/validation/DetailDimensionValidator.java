package com.jasolar.mis.module.system.controller.budget.validation;

import com.jasolar.mis.module.system.controller.budget.vo.*;
import org.apache.commons.lang3.StringUtils;

import java.util.*;

/**
 * 明细维度校验工具类
 * 用于校验明细列表中所有明细的维度是否相同
 * 
 * @author Auto Generated
 * @date 2025-01-XX
 */
public class DetailDimensionValidator {

    /**
     * 校验结果类
     */
    public static class ValidationResult {
        private final Map<String, String> detailValidationResultMap;
        private final Map<String, String> detailValidationMessageMap;
        private final boolean hasError;

        public ValidationResult(Map<String, String> detailValidationResultMap, 
                               Map<String, String> detailValidationMessageMap) {
            this.detailValidationResultMap = detailValidationResultMap;
            this.detailValidationMessageMap = detailValidationMessageMap;
            this.hasError = detailValidationResultMap != null && 
                           detailValidationResultMap.values().stream().anyMatch(result -> "1".equals(result));
        }

        public Map<String, String> getDetailValidationResultMap() {
            return detailValidationResultMap;
        }

        public Map<String, String> getDetailValidationMessageMap() {
            return detailValidationMessageMap;
        }

        public boolean hasError() {
            return hasError;
        }
    }

    /**
     * 校验调整明细列表的维度是否相同，返回校验结果
     * 
     * @param adjustDetails 调整明细列表
     * @param docIsInternal 单据级的集团内/集团外标识（因为明细的isInternal是@JsonIgnore的）
     * @return 校验结果，包含每个明细的校验状态和错误信息
     */
    public static ValidationResult validateAdjustDetails(List<AdjustDetailDetailVo> adjustDetails, String docIsInternal) {
        if (adjustDetails == null || adjustDetails.isEmpty()) {
            return new ValidationResult(new HashMap<>(), new HashMap<>());
        }
        return validateDetails(adjustDetails, DetailType.ADJUST, docIsInternal);
    }

    /**
     * 校验申请明细列表的维度是否相同，返回校验结果
     * 
     * @param applyDetails 申请明细列表
     * @return 校验结果，包含每个明细的校验状态和错误信息
     */
    public static ValidationResult validateApplyDetails(List<ApplyDetailDetalVo> applyDetails) {
        if (applyDetails == null || applyDetails.isEmpty()) {
            return new ValidationResult(new HashMap<>(), new HashMap<>());
        }
        return validateDetails(applyDetails, DetailType.APPLY, null);
    }

    /**
     * 校验付款/报销明细列表的维度是否相同，返回校验结果
     * 
     * @param claimDetails 付款/报销明细列表
     * @return 校验结果，包含每个明细的校验状态和错误信息
     */
    public static ValidationResult validateClaimDetails(List<ClaimDetailDetailVo> claimDetails) {
        if (claimDetails == null || claimDetails.isEmpty()) {
            return new ValidationResult(new HashMap<>(), new HashMap<>());
        }
        return validateDetails(claimDetails, DetailType.CLAIM, null);
    }

    /**
     * 校验合同明细列表的维度是否相同，返回校验结果
     * 
     * @param contractDetails 合同明细列表
     * @return 校验结果，包含每个明细的校验状态和错误信息
     */
    public static ValidationResult validateContractDetails(List<ContractDetailDetailVo> contractDetails) {
        if (contractDetails == null || contractDetails.isEmpty()) {
            return new ValidationResult(new HashMap<>(), new HashMap<>());
        }
        return validateDetails(contractDetails, DetailType.CONTRACT, null);
    }

    /**
     * 校验查询明细列表的维度是否相同，返回校验结果
     * 
     * @param queryDetails 查询明细列表
     * @return 校验结果，包含每个明细的校验状态和错误信息
     */
    public static ValidationResult validateQueryDetails(List<QueryDetailDetailVo> queryDetails) {
        if (queryDetails == null || queryDetails.isEmpty()) {
            return new ValidationResult(new HashMap<>(), new HashMap<>());
        }
        return validateDetails(queryDetails, DetailType.QUERY, null);
    }

    /**
     * 通用校验方法
     * 
     * @param details 明细列表
     * @param detailType 明细类型
     * @param docIsInternal 单据级的集团内/集团外标识（仅用于ADJUST类型）
     * @return 校验结果，包含每个明细的校验状态和错误信息
     */
    private static ValidationResult validateDetails(List<?> details, DetailType detailType, String docIsInternal) {
        Map<String, String> detailValidationResultMap = new HashMap<>();
        Map<String, String> detailValidationMessageMap = new HashMap<>();
        
        if (details == null || details.size() <= 1) {
            // 如果只有0或1条明细，所有明细都通过
            if (details != null && details.size() == 1) {
                String detailLineNo = getDetailLineNo(details.get(0), detailType);
                detailValidationResultMap.put(detailLineNo, "0");
                detailValidationMessageMap.put(detailLineNo, "校验通过 Verification Passed");
            }
            return new ValidationResult(detailValidationResultMap, detailValidationMessageMap);
        }

        // 提取维度信息并检查重复明细
        Set<String> dimensionKeys = new HashSet<>();
        Map<String, Integer> dimensionKeyToFirstIndex = new HashMap<>();
        Map<String, DetailDimension> dimensionKeyToDimension = new HashMap<>();
        
        for (int i = 0; i < details.size(); i++) {
            Object detail = details.get(i);
            DetailDimension dimension = extractDimension(detail, detailType, i, docIsInternal);
            String detailLineNo = getDetailLineNo(detail, detailType);
            
            // 检查是否有完全相同的明细（重复明细）
            String dimensionKey = dimension.getDimensionKey();
            if (dimensionKeys.contains(dimensionKey)) {
                // 找到重复的明细
                Integer firstIndex = dimensionKeyToFirstIndex.get(dimensionKey);
                DetailDimension firstDimension = dimensionKeyToDimension.get(dimensionKey);
                String errorMessage = String.format("明细列表中存在重复的明细，第%d条明细与第%d条明细维度完全相同。维度信息：组织编码=%s, 科目编码=%s, 资产类型编码=%s, 项目编码=%s, 集团内/集团外=%s",
                    i + 1, firstIndex + 1, dimension.getManagementOrg(), dimension.getBudgetSubjectCode(),
                    dimension.getErpAssetType(), dimension.getMasterProjectCode(), dimension.getIsInternal());
                
                // 标记两条明细都失败
                String firstDetailLineNo = getDetailLineNo(details.get(firstIndex), detailType);
                detailValidationResultMap.put(firstDetailLineNo, "1");
                detailValidationMessageMap.put(firstDetailLineNo, errorMessage);
                detailValidationResultMap.put(detailLineNo, "1");
                detailValidationMessageMap.put(detailLineNo, errorMessage);
            } else {
                dimensionKeys.add(dimensionKey);
                dimensionKeyToFirstIndex.put(dimensionKey, i);
                dimensionKeyToDimension.put(dimensionKey, dimension);
                // 非重复明细，标记为通过
                detailValidationResultMap.put(detailLineNo, "0");
                detailValidationMessageMap.put(detailLineNo, "校验通过 Verification Passed");
            }
        }
        
        return new ValidationResult(detailValidationResultMap, detailValidationMessageMap);
    }

    /**
     * 获取明细行号
     */
    private static String getDetailLineNo(Object detail, DetailType detailType) {
        if (detail instanceof AdjustDetailDetailVo) {
            return ((AdjustDetailDetailVo) detail).getAdjustDetailLineNo();
        } else if (detail instanceof ApplyDetailDetalVo) {
            return ((ApplyDetailDetalVo) detail).getDemandDetailLineNo();
        } else if (detail instanceof ClaimDetailDetailVo) {
            return ((ClaimDetailDetailVo) detail).getClaimDetailLineNo();
        } else if (detail instanceof ContractDetailDetailVo) {
            return ((ContractDetailDetailVo) detail).getContractDetailLineNo();
        } else if (detail instanceof QueryDetailDetailVo) {
            return ((QueryDetailDetailVo) detail).getDetailLineNo();
        }
        return "UNKNOWN";
    }

    /**
     * 提取明细的维度信息
     */
    private static DetailDimension extractDimension(Object detail, DetailType detailType, int index, String docIsInternal) {
        DetailDimension dimension = new DetailDimension();
        
        if (detail instanceof AdjustDetailDetailVo) {
            AdjustDetailDetailVo adjustDetail = (AdjustDetailDetailVo) detail;
            dimension.setManagementOrg(adjustDetail.getManagementOrg());
            dimension.setBudgetSubjectCode(adjustDetail.getBudgetSubjectCode());
            dimension.setMasterProjectCode(adjustDetail.getMasterProjectCode());
            dimension.setErpAssetType(adjustDetail.getErpAssetType());
            // AdjustDetailDetailVo的isInternal是@JsonIgnore的，使用单据级的isInternal
            dimension.setIsInternal(docIsInternal);
        } else if (detail instanceof ApplyDetailDetalVo) {
            ApplyDetailDetalVo applyDetail = (ApplyDetailDetalVo) detail;
            dimension.setManagementOrg(applyDetail.getManagementOrg());
            dimension.setBudgetSubjectCode(applyDetail.getBudgetSubjectCode());
            dimension.setMasterProjectCode(applyDetail.getMasterProjectCode());
            dimension.setErpAssetType(applyDetail.getErpAssetType());
            dimension.setIsInternal(applyDetail.getIsInternal());
        } else if (detail instanceof ClaimDetailDetailVo) {
            ClaimDetailDetailVo claimDetail = (ClaimDetailDetailVo) detail;
            dimension.setManagementOrg(claimDetail.getManagementOrg());
            dimension.setBudgetSubjectCode(claimDetail.getBudgetSubjectCode());
            dimension.setMasterProjectCode(claimDetail.getMasterProjectCode());
            dimension.setErpAssetType(claimDetail.getErpAssetType());
            dimension.setIsInternal(claimDetail.getIsInternal());
        } else if (detail instanceof ContractDetailDetailVo) {
            ContractDetailDetailVo contractDetail = (ContractDetailDetailVo) detail;
            dimension.setManagementOrg(contractDetail.getManagementOrg());
            dimension.setBudgetSubjectCode(contractDetail.getBudgetSubjectCode());
            dimension.setMasterProjectCode(contractDetail.getMasterProjectCode());
            dimension.setErpAssetType(contractDetail.getErpAssetType());
            dimension.setIsInternal(contractDetail.getIsInternal());
        } else if (detail instanceof QueryDetailDetailVo) {
            QueryDetailDetailVo queryDetail = (QueryDetailDetailVo) detail;
            dimension.setManagementOrg(queryDetail.getManagementOrg());
            dimension.setBudgetSubjectCode(queryDetail.getBudgetSubjectCode());
            dimension.setMasterProjectCode(queryDetail.getMasterProjectCode());
            dimension.setErpAssetType(queryDetail.getErpAssetType());
            dimension.setIsInternal(queryDetail.getIsInternal());
        } else {
            throw new IllegalArgumentException("不支持的明细类型: " + detail.getClass().getName());
        }
        
        return dimension;
    }


    /**
     * 标准化字符串（处理空值和null）
     */
    private static String normalize(String value) {
        if (StringUtils.isBlank(value)) {
            return "";
        }
        return value.trim();
    }

    /**
     * 明细类型枚举
     */
    private enum DetailType {
        ADJUST, APPLY, CLAIM, CONTRACT, QUERY
    }

    /**
     * 明细维度信息
     */
    private static class DetailDimension {
        private String managementOrg;
        private String budgetSubjectCode;
        private String masterProjectCode;
        private String erpAssetType;
        private String isInternal;

        public String getManagementOrg() {
            return managementOrg;
        }

        public void setManagementOrg(String managementOrg) {
            this.managementOrg = managementOrg;
        }

        public String getBudgetSubjectCode() {
            return budgetSubjectCode;
        }

        public void setBudgetSubjectCode(String budgetSubjectCode) {
            this.budgetSubjectCode = budgetSubjectCode;
        }

        public String getMasterProjectCode() {
            return masterProjectCode;
        }

        public void setMasterProjectCode(String masterProjectCode) {
            this.masterProjectCode = masterProjectCode;
        }

        public String getErpAssetType() {
            return erpAssetType;
        }

        public void setErpAssetType(String erpAssetType) {
            this.erpAssetType = erpAssetType;
        }

        public String getIsInternal() {
            return isInternal;
        }

        public void setIsInternal(String isInternal) {
            this.isInternal = isInternal;
        }

        /**
         * 获取维度唯一标识（用于检测重复明细）
         * 规则：
         * - 如果没带项目编码（为空或"NAN"）：不包含集团内/集团外（不检查isInternal）
         * - 如果带了项目编码：包含集团内/集团外（检查isInternal）
         */
        public String getDimensionKey() {
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
    }
}

