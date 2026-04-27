package com.jasolar.mis.module.system.mapper.budget;

import com.jasolar.mis.framework.mybatis.core.mapper.BaseMapperX;
import com.jasolar.mis.module.system.domain.budget.BudgetPoolDemR;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 预算池维度关系 Mapper
 */
@Mapper
public interface BudgetPoolDemRMapper extends BaseMapperX<BudgetPoolDemR> {

    /**
     * 根据维度条件批量查询预算池维度关系
     *
     * @param details 扩展明细列表（包含 managementOrg, budgetSubjectCode, masterProjectCode）
     * @return 预算池维度关系列表
     */
    List<BudgetPoolDemR> selectByDimensions(@Param("details") List<com.jasolar.mis.module.system.controller.budget.vo.ApplyExtDetailVo> details);

    /**
     * 根据维度条件批量查询预算池维度关系（合同申请）
     *
     * @param details 扩展明细列表（包含 managementOrg, budgetSubjectCode, masterProjectCode）
     * @return 预算池维度关系列表
     */
    List<BudgetPoolDemR> selectByDimensionsForContract(@Param("details") List<com.jasolar.mis.module.system.controller.budget.vo.ContractExtDetailVo> details);

    /**
     * 根据维度条件批量查询预算池维度关系（付款/报销）
     *
     * @param details 扩展明细列表
     * @return 预算池维度关系列表
     */
    List<BudgetPoolDemR> selectByDimensionsForClaim(@Param("details") List<com.jasolar.mis.module.system.controller.budget.vo.ClaimExtDetailVo> details);

    /**
     * 根据维度条件批量查询预算池维度关系（包含 year 和 quarter）
     *
     * @param dimensionParams 维度参数列表，每个元素包含 year, quarter, morgCode, budgetSubjectCode, masterProjectCode
     * @return 预算池维度关系列表
     */
    List<BudgetPoolDemR> selectByDimensionsWithYearAndQuarter(@Param("dimensionParams") List<DimensionParam> dimensionParams);

    /**
     * 根据维度条件批量查询预算池维度关系（仅项目维度：year, quarter, isInternal, morgCode, masterProjectCode）
     *
     * @param dimensionParams 维度参数列表，每个元素包含 year, quarter, isInternal, morgCode, masterProjectCode
     * @return 预算池维度关系列表
     */
    List<BudgetPoolDemR> selectByDimensionsWithYearAndQuarterForProject(@Param("dimensionParams") List<DimensionParam> dimensionParams);

    /**
     * 按 PROJECT_ID 查询（项目编码变更时回退查找，避免重复创建 pool）
     *
     * @param projectIdParams 参数列表，每个元素包含 year, quarter, isInternal, projectId
     * @return 预算池维度关系列表
     */
    List<BudgetPoolDemR> selectByProjectIdDimensions(@Param("projectIdParams") List<ProjectIdDimensionParam> projectIdParams);

    /**
     * 按 PROJECT_ID 查询用的参数类
     */
    class ProjectIdDimensionParam {
        private String year;
        private String quarter;
        private String isInternal;
        private String projectId;

        public ProjectIdDimensionParam(String year, String quarter, String isInternal, String projectId) {
            this.year = year;
            this.quarter = quarter;
            this.isInternal = isInternal;
            this.projectId = projectId;
        }

        public String getYear() { return year; }
        public String getQuarter() { return quarter; }
        public String getIsInternal() { return isInternal; }
        public String getProjectId() { return projectId; }
    }

    /**
     * 维度参数类
     */
    class DimensionParam {
        private String year;
        private String quarter;
        private String morgCode;
        private String budgetSubjectCode;
        private String masterProjectCode;
        private String erpAssetType;
        private String isInternal;

        public DimensionParam(String year, String quarter, String isInternal, String morgCode, String budgetSubjectCode, String masterProjectCode, String erpAssetType) {
            this.year = year;
            this.quarter = quarter;
            this.isInternal = isInternal;
            this.morgCode = morgCode;
            this.budgetSubjectCode = budgetSubjectCode;
            this.masterProjectCode = masterProjectCode;
            this.erpAssetType = erpAssetType;
        }

        public String getYear() { return year; }
        public void setYear(String year) { this.year = year; }
        public String getQuarter() { return quarter; }
        public void setQuarter(String quarter) { this.quarter = quarter; }
        public String getIsInternal() { return isInternal; }
        public void setIsInternal(String isInternal) { this.isInternal = isInternal; }
        public String getMorgCode() { return morgCode; }
        public void setMorgCode(String morgCode) { this.morgCode = morgCode; }
        public String getBudgetSubjectCode() { return budgetSubjectCode; }
        public void setBudgetSubjectCode(String budgetSubjectCode) { this.budgetSubjectCode = budgetSubjectCode; }
        public String getMasterProjectCode() { return masterProjectCode; }
        public void setMasterProjectCode(String masterProjectCode) { this.masterProjectCode = masterProjectCode; }
        public String getErpAssetType() { return erpAssetType; }
        public void setErpAssetType(String erpAssetType) { this.erpAssetType = erpAssetType; }
    }
}

