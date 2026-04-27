package com.jasolar.mis.module.system.controller.budget.validation;

import com.jasolar.mis.module.system.controller.budget.vo.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;

import java.math.BigDecimal;
import java.util.*;

/**
 * 明细合并工具类
 * 用于合并相同维度的明细，金额叠加
 * 
 * @author Auto Generated
 * @date 2025-01-XX
 */
public class DetailMerger {

    /**
     * 合并结果
     */
    public static class MergeResult<T> {
        private final List<T> mergedDetails;
        private final boolean hasMerged;

        public MergeResult(List<T> mergedDetails, boolean hasMerged) {
            this.mergedDetails = mergedDetails;
            this.hasMerged = hasMerged;
        }

        public List<T> getMergedDetails() {
            return mergedDetails;
        }

        public boolean hasMerged() {
            return hasMerged;
        }
    }

    /**
     * 检测相同维度明细的结果
     */
    public static class DuplicateDimensionResult<T> {
        private final Map<Integer, String> duplicateDetailErrors; // key: 明细索引, value: 错误信息
        private final boolean hasDuplicate;

        public DuplicateDimensionResult(Map<Integer, String> duplicateDetailErrors, boolean hasDuplicate) {
            this.duplicateDetailErrors = duplicateDetailErrors != null ? duplicateDetailErrors : new HashMap<>();
            this.hasDuplicate = hasDuplicate;
        }

        public Map<Integer, String> getDuplicateDetailErrors() {
            return duplicateDetailErrors;
        }

        public boolean hasDuplicate() {
            return hasDuplicate;
        }
    }

    /**
     * 合并调整明细列表
     * 
     * @param adjustDetails 调整明细列表
     * @param docIsInternal 单据级的集团内/集团外标识（因为明细的isInternal是@JsonIgnore的）
     * @return 合并结果
     */
    public static MergeResult<AdjustDetailDetailVo> mergeAdjustDetails(
            List<AdjustDetailDetailVo> adjustDetails, String docIsInternal) {
        if (adjustDetails == null || adjustDetails.isEmpty()) {
            return new MergeResult<>(new ArrayList<>(), false);
        }
        return mergeDetails(adjustDetails, DetailType.ADJUST, docIsInternal, null);
    }

    /**
     * 合并申请明细列表
     * 
     * @param applyDetails 申请明细列表
     * @return 合并结果
     */
    public static MergeResult<ApplyDetailDetalVo> mergeApplyDetails(List<ApplyDetailDetalVo> applyDetails) {
        if (applyDetails == null || applyDetails.isEmpty()) {
            return new MergeResult<>(new ArrayList<>(), false);
        }
        return mergeDetails(applyDetails, DetailType.APPLY, null, null);
    }

    /**
     * 合并付款/报销明细列表
     * 
     * @param claimDetails 付款/报销明细列表
     * @return 合并结果
     */
    public static MergeResult<ClaimDetailDetailVo> mergeClaimDetails(List<ClaimDetailDetailVo> claimDetails) {
        if (claimDetails == null || claimDetails.isEmpty()) {
            return new MergeResult<>(new ArrayList<>(), false);
        }
        return mergeDetails(claimDetails, DetailType.CLAIM, null, null);
    }

    /**
     * 合并合同明细列表
     * 
     * @param contractDetails 合同明细列表
     * @return 合并结果
     */
    public static MergeResult<ContractDetailDetailVo> mergeContractDetails(List<ContractDetailDetailVo> contractDetails) {
        if (contractDetails == null || contractDetails.isEmpty()) {
            return new MergeResult<>(new ArrayList<>(), false);
        }
        return mergeDetails(contractDetails, DetailType.CONTRACT, null, null);
    }

    /**
     * 合并查询明细列表
     * 
     * @param queryDetails 查询明细列表
     * @param adjustType 调整类型（如果是调整单查询，传入此参数；否则传null）
     * @return 合并结果
     */
    public static MergeResult<QueryDetailDetailVo> mergeQueryDetails(List<QueryDetailDetailVo> queryDetails, String adjustType) {
        if (queryDetails == null || queryDetails.isEmpty()) {
            return new MergeResult<>(new ArrayList<>(), false);
        }
        return mergeDetails(queryDetails, DetailType.QUERY, null, adjustType);
    }
    
    /**
     * 合并查询明细列表（兼容旧方法，adjustType传null）
     * 
     * @param queryDetails 查询明细列表
     * @return 合并结果
     */
    public static MergeResult<QueryDetailDetailVo> mergeQueryDetails(List<QueryDetailDetailVo> queryDetails) {
        return mergeQueryDetails(queryDetails, null);
    }

    /**
     * 检测调整明细列表中是否有相同维度的明细
     * 
     * @param adjustDetails 调整明细列表
     * @param docIsInternal 单据级的集团内/集团外标识
     * @return 检测结果，包含重复明细的索引和错误信息
     */
    public static DuplicateDimensionResult<AdjustDetailDetailVo> checkDuplicateAdjustDetails(
            List<AdjustDetailDetailVo> adjustDetails, String docIsInternal) {
        if (adjustDetails == null || adjustDetails.isEmpty()) {
            return new DuplicateDimensionResult<>(new HashMap<>(), false);
        }
        return checkDuplicateDetails(adjustDetails, DetailType.ADJUST, docIsInternal, null);
    }

    /**
     * 检测申请明细列表中是否有相同维度的明细
     * 
     * @param applyDetails 申请明细列表
     * @return 检测结果，包含重复明细的索引和错误信息
     */
    public static DuplicateDimensionResult<ApplyDetailDetalVo> checkDuplicateApplyDetails(
            List<ApplyDetailDetalVo> applyDetails) {
        if (applyDetails == null || applyDetails.isEmpty()) {
            return new DuplicateDimensionResult<>(new HashMap<>(), false);
        }
        return checkDuplicateDetails(applyDetails, DetailType.APPLY, null, null);
    }

    /**
     * 检测付款/报销明细列表中是否有相同维度的明细
     * 
     * @param claimDetails 付款/报销明细列表
     * @return 检测结果，包含重复明细的索引和错误信息
     */
    public static DuplicateDimensionResult<ClaimDetailDetailVo> checkDuplicateClaimDetails(
            List<ClaimDetailDetailVo> claimDetails) {
        if (claimDetails == null || claimDetails.isEmpty()) {
            return new DuplicateDimensionResult<>(new HashMap<>(), false);
        }
        return checkDuplicateDetails(claimDetails, DetailType.CLAIM, null, null);
    }

    /**
     * 检测合同明细列表中是否有相同维度的明细
     * 
     * @param contractDetails 合同明细列表
     * @return 检测结果，包含重复明细的索引和错误信息
     */
    public static DuplicateDimensionResult<ContractDetailDetailVo> checkDuplicateContractDetails(
            List<ContractDetailDetailVo> contractDetails) {
        if (contractDetails == null || contractDetails.isEmpty()) {
            return new DuplicateDimensionResult<>(new HashMap<>(), false);
        }
        return checkDuplicateDetails(contractDetails, DetailType.CONTRACT, null, null);
    }

    /**
     * 检测查询明细列表中是否有相同维度的明细
     * 
     * @param queryDetails 查询明细列表
     * @param adjustType 调整类型（如果是调整单查询，传入此参数；否则传null）
     * @return 检测结果，包含重复明细的索引和错误信息
     */
    public static DuplicateDimensionResult<QueryDetailDetailVo> checkDuplicateQueryDetails(
            List<QueryDetailDetailVo> queryDetails, String adjustType) {
        if (queryDetails == null || queryDetails.isEmpty()) {
            return new DuplicateDimensionResult<>(new HashMap<>(), false);
        }
        return checkDuplicateDetails(queryDetails, DetailType.QUERY, null, adjustType);
    }
    
    /**
     * 检测查询明细列表中是否有相同维度的明细（兼容旧方法，adjustType传null）
     * 
     * @param queryDetails 查询明细列表
     * @return 检测结果，包含重复明细的索引和错误信息
     */
    public static DuplicateDimensionResult<QueryDetailDetailVo> checkDuplicateQueryDetails(
            List<QueryDetailDetailVo> queryDetails) {
        return checkDuplicateQueryDetails(queryDetails, null);
    }

    /**
     * 通用检测相同维度明细的方法
     */
    @SuppressWarnings("unchecked")
    private static <T> DuplicateDimensionResult<T> checkDuplicateDetails(
            List<T> details, DetailType detailType, String docIsInternal, String adjustType) {
        Map<Integer, String> duplicateDetailErrors = new HashMap<>();
        
        if (details == null || details.size() <= 1) {
            return new DuplicateDimensionResult<>(duplicateDetailErrors, false);
        }

        // 使用维度key作为分组依据，记录每个维度对应的明细索引列表
        Map<String, List<Integer>> dimensionKeyToIndices = new LinkedHashMap<>();

        for (int i = 0; i < details.size(); i++) {
            T detail = details.get(i);
            DetailDimension dimension = extractDimension(detail, detailType, i, docIsInternal, adjustType);
            String dimensionKey = dimension.getDimensionKey();

            dimensionKeyToIndices.computeIfAbsent(dimensionKey, k -> new ArrayList<>()).add(i);
        }

        // 检查是否有重复的维度
        boolean hasDuplicate = false;
        for (Map.Entry<String, List<Integer>> entry : dimensionKeyToIndices.entrySet()) {
            List<Integer> indices = entry.getValue();
            if (indices.size() > 1) {
                hasDuplicate = true;
                // 为所有重复的明细设置错误信息
                // 判断是否需要包含"调整类型"：对于调整明细或查询明细（如果包含effectType）
                boolean includeEffectType = false;
                if (detailType == DetailType.ADJUST) {
                    includeEffectType = true;
                } else if (detailType == DetailType.QUERY && !indices.isEmpty()) {
                    // 检查第一个明细是否有 effectType
                    Object firstDetail = details.get(indices.get(0));
                    if (firstDetail instanceof QueryDetailDetailVo) {
                        QueryDetailDetailVo queryDetail = (QueryDetailDetailVo) firstDetail;
                        includeEffectType = StringUtils.isNotBlank(queryDetail.getEffectType()) 
                            || (queryDetail.getMetadata() != null && queryDetail.getMetadata().containsKey("effectType"));
                    }
                }
                
                String dimensionDesc = includeEffectType
                    ? "管理组织、预算科目、项目编码、资产类型、集团内外、调整类型均相同"
                    : "管理组织、预算科目、项目编码、资产类型、集团内外均相同";
                String errorMessage = String.format("存在相同维度的明细（%s），明细行号: %s Duplicate Items with Same Dimension",
                        dimensionDesc,
                        indices.stream()
                                .map(idx -> getDetailLineNo(details.get(idx), detailType))
                                .reduce((a, b) -> a + ", " + b)
                                .orElse(""));
                
                for (Integer idx : indices) {
                    duplicateDetailErrors.put(idx, errorMessage);
                }
            }
        }

        return new DuplicateDimensionResult<>(duplicateDetailErrors, hasDuplicate);
    }

    /**
     * 获取明细的行号
     */
    @SuppressWarnings("unchecked")
    private static <T> String getDetailLineNo(T detail, DetailType detailType) {
        if (detailType == DetailType.ADJUST) {
            return ((AdjustDetailDetailVo) detail).getAdjustDetailLineNo();
        } else if (detailType == DetailType.APPLY) {
            return ((ApplyDetailDetalVo) detail).getDemandDetailLineNo();
        } else if (detailType == DetailType.CLAIM) {
            return ((ClaimDetailDetailVo) detail).getClaimDetailLineNo();
        } else if (detailType == DetailType.CONTRACT) {
            return ((ContractDetailDetailVo) detail).getContractDetailLineNo();
        } else if (detailType == DetailType.QUERY) {
            return ((QueryDetailDetailVo) detail).getDetailLineNo();
        }
        return String.valueOf(System.identityHashCode(detail));
    }

    /**
     * 通用合并方法
     */
    @SuppressWarnings("unchecked")
    private static <T> MergeResult<T> mergeDetails(List<T> details, DetailType detailType, String docIsInternal, String adjustType) {
        if (details == null || details.size() <= 1) {
            return new MergeResult<>(details != null ? new ArrayList<>(details) : new ArrayList<>(), false);
        }

        // 使用维度key作为分组依据
        Map<String, T> dimensionKeyToDetail = new LinkedHashMap<>();
        boolean hasMerged = false;

        for (T detail : details) {
            DetailDimension dimension = extractDimension(detail, detailType, 0, docIsInternal, adjustType);
            String dimensionKey = dimension.getDimensionKey();

            if (dimensionKeyToDetail.containsKey(dimensionKey)) {
                // 找到相同维度的明细，进行合并
                T existingDetail = dimensionKeyToDetail.get(dimensionKey);
                T mergedDetail = mergeDetail(existingDetail, detail, detailType);
                dimensionKeyToDetail.put(dimensionKey, mergedDetail);
                hasMerged = true;
            } else {
                // 新的维度，直接添加
                dimensionKeyToDetail.put(dimensionKey, detail);
            }
        }

        List<T> mergedDetails = new ArrayList<>(dimensionKeyToDetail.values());
        
        // 注意：合并时保留第一个明细的metadata（由createNewDetail方法复制）
        // 不再清除metadata，以保留原始明细的元数据信息（如mxid等）
        // 如果发生了合并，metadata会保留第一个被合并明细的metadata

        return new MergeResult<>(mergedDetails, hasMerged);
    }

    /**
     * 合并两个明细
     */
    @SuppressWarnings("unchecked")
    private static <T> T mergeDetail(T detail1, T detail2, DetailType detailType) {
        T merged = createNewDetail(detail1, detailType);
        
        if (detailType == DetailType.ADJUST) {
            mergeAdjustDetail((AdjustDetailDetailVo) merged, (AdjustDetailDetailVo) detail1, (AdjustDetailDetailVo) detail2);
        } else if (detailType == DetailType.APPLY) {
            mergeApplyDetail((ApplyDetailDetalVo) merged, (ApplyDetailDetalVo) detail1, (ApplyDetailDetalVo) detail2);
        } else if (detailType == DetailType.CLAIM) {
            mergeClaimDetail((ClaimDetailDetailVo) merged, (ClaimDetailDetailVo) detail1, (ClaimDetailDetailVo) detail2);
        } else if (detailType == DetailType.CONTRACT) {
            mergeContractDetail((ContractDetailDetailVo) merged, (ContractDetailDetailVo) detail1, (ContractDetailDetailVo) detail2);
        } else if (detailType == DetailType.QUERY) {
            // 查询明细没有金额字段，不需要合并金额，只保留第一个
            return detail1;
        }
        
        return merged;
    }

    /**
     * 创建新的明细对象（复制第一个明细的基础信息）
     */
    @SuppressWarnings("unchecked")
    private static <T> T createNewDetail(T source, DetailType detailType) {
        if (detailType == DetailType.ADJUST) {
            AdjustDetailDetailVo newDetail = new AdjustDetailDetailVo();
            BeanUtils.copyProperties(source, newDetail);
            return (T) newDetail;
        } else if (detailType == DetailType.APPLY) {
            ApplyDetailDetalVo newDetail = new ApplyDetailDetalVo();
            BeanUtils.copyProperties(source, newDetail);
            return (T) newDetail;
        } else if (detailType == DetailType.CLAIM) {
            ClaimDetailDetailVo newDetail = new ClaimDetailDetailVo();
            BeanUtils.copyProperties(source, newDetail);
            return (T) newDetail;
        } else if (detailType == DetailType.CONTRACT) {
            ContractDetailDetailVo newDetail = new ContractDetailDetailVo();
            BeanUtils.copyProperties(source, newDetail);
            return (T) newDetail;
        } else if (detailType == DetailType.QUERY) {
            QueryDetailDetailVo newDetail = new QueryDetailDetailVo();
            BeanUtils.copyProperties(source, newDetail);
            return (T) newDetail;
        }
        throw new IllegalArgumentException("不支持的明细类型: " + detailType);
    }

    /**
     * 合并调整明细的金额
     */
    private static void mergeAdjustDetail(AdjustDetailDetailVo merged, AdjustDetailDetailVo detail1, AdjustDetailDetailVo detail2) {
        BeanUtils.copyProperties(detail1, merged);
        
        // 合并Q1-Q4金额
        merged.setAdjustAmountQ1(addBigDecimal(detail1.getAdjustAmountQ1(), detail2.getAdjustAmountQ1()));
        merged.setAdjustAmountQ2(addBigDecimal(detail1.getAdjustAmountQ2(), detail2.getAdjustAmountQ2()));
        merged.setAdjustAmountQ3(addBigDecimal(detail1.getAdjustAmountQ3(), detail2.getAdjustAmountQ3()));
        merged.setAdjustAmountQ4(addBigDecimal(detail1.getAdjustAmountQ4(), detail2.getAdjustAmountQ4()));
        
        // 合并全年合计金额
        merged.setAdjustAmountTotalInvestment(
            addBigDecimal(detail1.getAdjustAmountTotalInvestment(), detail2.getAdjustAmountTotalInvestment()));
    }

    /**
     * 合并申请明细的金额
     */
    private static void mergeApplyDetail(ApplyDetailDetalVo merged, ApplyDetailDetalVo detail1, ApplyDetailDetalVo detail2) {
        BeanUtils.copyProperties(detail1, merged);
        merged.setDemandAmount(addBigDecimal(detail1.getDemandAmount(), detail2.getDemandAmount()));
    }

    /**
     * 合并付款/报销明细的金额
     */
    private static void mergeClaimDetail(ClaimDetailDetailVo merged, ClaimDetailDetailVo detail1, ClaimDetailDetailVo detail2) {
        BeanUtils.copyProperties(detail1, merged);
        merged.setActualAmount(addBigDecimal(detail1.getActualAmount(), detail2.getActualAmount()));
    }

    /**
     * 合并合同明细的金额
     */
    private static void mergeContractDetail(ContractDetailDetailVo merged, ContractDetailDetailVo detail1, ContractDetailDetailVo detail2) {
        BeanUtils.copyProperties(detail1, merged);
        merged.setContractAmount(addBigDecimal(detail1.getContractAmount(), detail2.getContractAmount()));
    }

    /**
     * 清除明细的metadata
     */
    @SuppressWarnings("unchecked")
    private static <T> void clearMetadata(T detail, DetailType detailType) {
        if (detailType == DetailType.ADJUST) {
            ((AdjustDetailDetailVo) detail).setMetadata(null);
        } else if (detailType == DetailType.APPLY) {
            ((ApplyDetailDetalVo) detail).setMetadata(null);
        } else if (detailType == DetailType.CLAIM) {
            ((ClaimDetailDetailVo) detail).setMetadata(null);
        } else if (detailType == DetailType.CONTRACT) {
            ((ContractDetailDetailVo) detail).setMetadata(null);
        } else if (detailType == DetailType.QUERY) {
            ((QueryDetailDetailVo) detail).setMetadata(null);
        }
    }

    /**
     * 安全地相加两个BigDecimal
     */
    private static BigDecimal addBigDecimal(BigDecimal a, BigDecimal b) {
        if (a == null && b == null) {
            return null;
        }
        if (a == null) {
            return b;
        }
        if (b == null) {
            return a;
        }
        return a.add(b);
    }

    /**
     * 提取明细的维度信息（复用DetailDimensionValidator的逻辑）
     */
    private static DetailDimension extractDimension(Object detail, DetailType detailType, int index, String docIsInternal, String adjustType) {
        DetailDimension dimension = new DetailDimension();
        
        if (detail instanceof AdjustDetailDetailVo) {
            AdjustDetailDetailVo adjustDetail = (AdjustDetailDetailVo) detail;
            dimension.setManagementOrg(adjustDetail.getManagementOrg());
            dimension.setBudgetSubjectCode(adjustDetail.getBudgetSubjectCode());
            dimension.setMasterProjectCode(adjustDetail.getMasterProjectCode());
            dimension.setErpAssetType(adjustDetail.getErpAssetType());
            dimension.setIsInternal(docIsInternal);
            dimension.setEffectType(adjustDetail.getEffectType());
        } else if (detail instanceof ApplyDetailDetalVo) {
            ApplyDetailDetalVo applyDetail = (ApplyDetailDetalVo) detail;
            dimension.setManagementOrg(applyDetail.getManagementOrg());
            dimension.setBudgetSubjectCode(applyDetail.getBudgetSubjectCode());
            dimension.setMasterProjectCode(applyDetail.getMasterProjectCode());
            dimension.setErpAssetType(applyDetail.getErpAssetType());
            dimension.setIsInternal(applyDetail.getIsInternal());
            // 申请明细没有 effectType，显式设置为 null
            dimension.setEffectType(null);
        } else if (detail instanceof ClaimDetailDetailVo) {
            ClaimDetailDetailVo claimDetail = (ClaimDetailDetailVo) detail;
            dimension.setManagementOrg(claimDetail.getManagementOrg());
            dimension.setBudgetSubjectCode(claimDetail.getBudgetSubjectCode());
            dimension.setMasterProjectCode(claimDetail.getMasterProjectCode());
            dimension.setErpAssetType(claimDetail.getErpAssetType());
            dimension.setIsInternal(claimDetail.getIsInternal());
            // 付款/报销明细没有 effectType，显式设置为 null
            dimension.setEffectType(null);
        } else if (detail instanceof ContractDetailDetailVo) {
            ContractDetailDetailVo contractDetail = (ContractDetailDetailVo) detail;
            dimension.setManagementOrg(contractDetail.getManagementOrg());
            dimension.setBudgetSubjectCode(contractDetail.getBudgetSubjectCode());
            dimension.setMasterProjectCode(contractDetail.getMasterProjectCode());
            dimension.setErpAssetType(contractDetail.getErpAssetType());
            dimension.setIsInternal(contractDetail.getIsInternal());
            // 合同明细没有 effectType，显式设置为 null
            dimension.setEffectType(null);
        } else if (detail instanceof QueryDetailDetailVo) {
            QueryDetailDetailVo queryDetail = (QueryDetailDetailVo) detail;
            dimension.setManagementOrg(queryDetail.getManagementOrg());
            dimension.setBudgetSubjectCode(queryDetail.getBudgetSubjectCode());
            dimension.setMasterProjectCode(queryDetail.getMasterProjectCode());
            dimension.setErpAssetType(queryDetail.getErpAssetType());
            dimension.setIsInternal(queryDetail.getIsInternal());
            // 查询明细：优先使用明细的 effectType 字段，如果没有则从 metadata 中获取，最后才使用传入的 adjustType
            String effectType = null;
            if (StringUtils.isNotBlank(queryDetail.getEffectType())) {
                effectType = queryDetail.getEffectType();
            } else if (queryDetail.getMetadata() != null && queryDetail.getMetadata().containsKey("effectType")) {
                effectType = queryDetail.getMetadata().get("effectType");
            } else if (StringUtils.isNotBlank(adjustType)) {
                effectType = adjustType;
            }
            dimension.setEffectType(effectType);
        } else {
            throw new IllegalArgumentException("不支持的明细类型: " + detail.getClass().getName());
        }
        
        return dimension;
    }

    /**
     * 明细类型枚举
     */
    private enum DetailType {
        ADJUST, APPLY, CLAIM, CONTRACT, QUERY
    }

    /**
     * 明细维度信息（内部类，复用DetailDimensionValidator的逻辑）
     */
    private static class DetailDimension {
        private String managementOrg;
        private String budgetSubjectCode;
        private String masterProjectCode;
        private String erpAssetType;
        private String isInternal;
        private String effectType; // 调整类型（仅用于AdjustDetailDetailVo）

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

        public String getEffectType() {
            return effectType;
        }

        public void setEffectType(String effectType) {
            this.effectType = effectType;
        }

        /**
         * 获取维度唯一标识（用于检测重复明细）
         * 规则：
         * - 如果没带项目编码（为空或"NAN"）：不包含集团内/集团外（不检查isInternal）
         * - 如果带了项目编码：包含集团内/集团外（检查isInternal）
         * - 对于调整明细（AdjustDetailDetailVo），包含调整类型（effectType）
         */
        public String getDimensionKey() {
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
         * 标准化字符串（处理空值和null）
         */
        private String normalize(String value) {
            if (StringUtils.isBlank(value)) {
                return "";
            }
            return value.trim();
        }
    }
}

