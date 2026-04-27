package com.jasolar.mis.module.system.service.budget;

import com.jasolar.mis.framework.common.pojo.PageResult;
import com.jasolar.mis.module.system.controller.budget.vo.BudgetAssetTypeMappingPageVo;
import com.jasolar.mis.module.system.controller.budget.vo.BudgetAssetTypeMappingQueryParams;
import com.jasolar.mis.module.system.controller.budget.vo.BudgetAssetTypeMappingUpdateReqVO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 预算资产类型映射服务
 */
public interface BudgetAssetTypeMappingService {

    /**
     * 分页查询预算资产类型映射
     * year、changeStatus 精确匹配；assetTypeName、erpAssetType、assetMajorCategoryName、
     * assetMajorCategoryCode、budgetAssetTypeName、budgetAssetTypeCode 支持模糊搜索
     *
     * @param params 查询参数
     * @return 分页结果
     */
    PageResult<BudgetAssetTypeMappingPageVo> pageQuery(BudgetAssetTypeMappingQueryParams params);

    /**
     * 全量查询预算资产类型映射（支持与分页相同的筛选条件，用于导出）
     *
     * @param params 查询参数
     * @return 全量列表
     */
    List<BudgetAssetTypeMappingPageVo> listAll(BudgetAssetTypeMappingQueryParams params);

    /**
     * 批量导入预算资产类型映射
     * 根据「是否变更」列：不变-跳过，新增-插入，修改-按主键更新
     *
     * @param file Excel 文件（需包含主键列，修改时根据主键更新）
     * @return 导入结果说明
     */
    String importExcel(MultipartFile file) throws Exception;

    /**
     * 从 DATAINTEGRATION.VIEW_BUDGET_MEMBER_NAME_CODE 增量同步到 budget_asset_type_mapping：
     * 拉取 MEMBER_CD 以 CU205 开头的数据，表里已有（同 budget_asset_type_code + 当年）的不修改，仅新增
     *
     * @return 同步结果说明
     */
    String syncFromView();

    /**
     * 根据主键获取单条，用于页面编辑回显
     *
     * @param id 主键
     * @return 单条记录，不存在返回 null
     */
    BudgetAssetTypeMappingPageVo getById(Long id);

    /**
     * 保存手工维护字段（资产大类编码/名称、资产类型编码/名称、年份、是否变更），不修改 budgetAssetTypeCode、budgetAssetTypeName
     *
     * @param req 含 id 及手工维护字段
     */
    void updateManualFields(BudgetAssetTypeMappingUpdateReqVO req);
}
