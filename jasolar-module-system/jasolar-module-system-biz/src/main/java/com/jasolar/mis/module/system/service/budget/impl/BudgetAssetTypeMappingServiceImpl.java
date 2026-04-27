package com.jasolar.mis.module.system.service.budget.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.jasolar.mis.framework.common.pojo.PageParam;
import com.jasolar.mis.framework.common.pojo.PageResult;
import com.jasolar.mis.framework.excel.core.util.ExcelUtils;
import com.jasolar.mis.framework.web.core.util.WebFrameworkUtils;
import com.jasolar.mis.module.system.controller.budget.vo.BudgetAssetTypeMappingExcelVO;
import com.jasolar.mis.module.system.controller.budget.vo.BudgetAssetTypeMappingPageVo;
import com.jasolar.mis.module.system.controller.budget.vo.BudgetAssetTypeMappingQueryParams;
import com.jasolar.mis.module.system.controller.budget.vo.BudgetAssetTypeMappingUpdateReqVO;
import com.jasolar.mis.module.system.domain.budget.BudgetAssetTypeMapping;
import com.jasolar.mis.module.system.domain.budget.BudgetMemberNameCodeView;
import com.jasolar.mis.module.system.mapper.budget.BudgetAssetTypeMappingMapper;
import com.jasolar.mis.module.system.mapper.budget.BudgetMemberNameCodeViewMapper;
import com.jasolar.mis.module.system.service.budget.BudgetAssetTypeMappingService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.time.Year;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 预算资产类型映射服务实现
 */
@Service
@Slf4j
public class BudgetAssetTypeMappingServiceImpl implements BudgetAssetTypeMappingService {

    @Resource
    private BudgetAssetTypeMappingMapper budgetAssetTypeMappingMapper;

    @Resource
    private BudgetMemberNameCodeViewMapper budgetMemberNameCodeViewMapper;

    @Override
    public PageResult<BudgetAssetTypeMappingPageVo> pageQuery(BudgetAssetTypeMappingQueryParams params) {
        log.info("分页查询预算资产类型映射，params={}", params);

        LambdaQueryWrapper<BudgetAssetTypeMapping> wrapper = buildQueryWrapper(params);

        PageParam pageParam = new PageParam();
        pageParam.setPageNo(params.getPageNo());
        pageParam.setPageSize(params.getPageSize());
        PageResult<BudgetAssetTypeMapping> pageResult = budgetAssetTypeMappingMapper.selectPage(pageParam, wrapper);

        List<BudgetAssetTypeMappingPageVo> voList = pageResult.getList().stream()
                .map(this::convertToVo)
                .collect(Collectors.toList());

        PageResult<BudgetAssetTypeMappingPageVo> result = new PageResult<>();
        result.setList(voList);
        result.setTotal(pageResult.getTotal());

        log.info("分页查询完成，总记录数={}, 当前页记录数={}", result.getTotal(), voList.size());
        return result;
    }

    @Override
    public List<BudgetAssetTypeMappingPageVo> listAll(BudgetAssetTypeMappingQueryParams params) {
        LambdaQueryWrapper<BudgetAssetTypeMapping> wrapper = buildQueryWrapper(params);
        List<BudgetAssetTypeMapping> list = budgetAssetTypeMappingMapper.selectList(wrapper);
        return list.stream().map(this::convertToVo).collect(Collectors.toList());
    }

    /**
     * 构建查询条件：year、changeStatus 多选精确匹配（in）；其余 6 个字段模糊搜索
     */
    private LambdaQueryWrapper<BudgetAssetTypeMapping> buildQueryWrapper(BudgetAssetTypeMappingQueryParams params) {
        LambdaQueryWrapper<BudgetAssetTypeMapping> wrapper = new LambdaQueryWrapper<BudgetAssetTypeMapping>()
                .orderByDesc(BudgetAssetTypeMapping::getUpdateTime);

        if (!CollectionUtils.isEmpty(params.getYear())) {
            wrapper.in(BudgetAssetTypeMapping::getYear, params.getYear());
        }
        if (!CollectionUtils.isEmpty(params.getChangeStatus())) {
            wrapper.in(BudgetAssetTypeMapping::getChangeStatus, params.getChangeStatus());
        }
        if (StringUtils.hasText(params.getBudgetAssetTypeCode())) {
            wrapper.like(BudgetAssetTypeMapping::getBudgetAssetTypeCode, params.getBudgetAssetTypeCode());
        }
        if (StringUtils.hasText(params.getBudgetAssetTypeName())) {
            wrapper.like(BudgetAssetTypeMapping::getBudgetAssetTypeName, params.getBudgetAssetTypeName());
        }
        if (StringUtils.hasText(params.getAssetMajorCategoryCode())) {
            wrapper.like(BudgetAssetTypeMapping::getAssetMajorCategoryCode, params.getAssetMajorCategoryCode());
        }
        if (StringUtils.hasText(params.getAssetMajorCategoryName())) {
            wrapper.like(BudgetAssetTypeMapping::getAssetMajorCategoryName, params.getAssetMajorCategoryName());
        }
        if (StringUtils.hasText(params.getErpAssetType())) {
            wrapper.like(BudgetAssetTypeMapping::getErpAssetType, params.getErpAssetType());
        }
        if (StringUtils.hasText(params.getAssetTypeName())) {
            wrapper.like(BudgetAssetTypeMapping::getAssetTypeName, params.getAssetTypeName());
        }

        return wrapper;
    }

    private BudgetAssetTypeMappingPageVo convertToVo(BudgetAssetTypeMapping entity) {
        BudgetAssetTypeMappingPageVo vo = new BudgetAssetTypeMappingPageVo();
        BeanUtils.copyProperties(entity, vo);
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String importExcel(MultipartFile file) throws Exception {
        if (file == null || file.isEmpty()) {
            return "文件为空，请选择要导入的 Excel 文件";
        }
        List<BudgetAssetTypeMappingExcelVO> list = ExcelUtils.readSync(file.getInputStream(), BudgetAssetTypeMappingExcelVO.class);
        if (list == null || list.isEmpty()) {
            return "Excel 文件中没有有效数据";
        }
        String operator = getOperator();
        int skipCount = 0;
        int addCount = 0;
        int modifyCount = 0;
        int failCount = 0;
        StringBuilder failMsg = new StringBuilder();
        for (int i = 0; i < list.size(); i++) {
            BudgetAssetTypeMappingExcelVO row = list.get(i);
            int rowNum = i + 2; // 表头占第1行
            String action = parseChangeStatus(row.getChangeStatus());
            try {
                if ("UNCHANGED".equals(action) || action == null) {
                    skipCount++;
                    continue;
                }
                if ("NEW".equals(action)) {
                    BudgetAssetTypeMapping entity = excelVoToEntity(row, null);
                    entity.setCreator(operator);
                    entity.setUpdater(operator);
                    budgetAssetTypeMappingMapper.insert(entity);
                    addCount++;
                    continue;
                }
                if ("MODIFY".equals(action)) {
                    Long id = parseLongId(row.getId());
                    if (id == null) {
                        failCount++;
                        failMsg.append("第").append(rowNum).append("行：修改时主键不能为空或格式错误。");
                        continue;
                    }
                    BudgetAssetTypeMapping existing = budgetAssetTypeMappingMapper.selectById(id);
                    if (existing == null) {
                        failCount++;
                        failMsg.append("第").append(rowNum).append("行：主键[").append(id).append("]不存在。");
                        continue;
                    }
                    BudgetAssetTypeMapping entity = excelVoToEntity(row, existing.getId());
                    entity.setCreator(existing.getCreator());
                    entity.setCreateTime(existing.getCreateTime());
                    entity.setUpdater(operator);
                    entity.setDeleted(existing.getDeleted());
                    budgetAssetTypeMappingMapper.updateById(entity);
                    modifyCount++;
                }
            } catch (Exception e) {
                failCount++;
                failMsg.append("第").append(rowNum).append("行：").append(e.getMessage() != null ? e.getMessage() : e.toString()).append("；");
                log.warn("导入第{}行失败", rowNum, e);
            }
        }
        String result = String.format("导入完成：跳过 %d 条，新增 %d 条，修改 %d 条", skipCount, addCount, modifyCount);
        if (failCount > 0) {
            result += String.format("；失败 %d 条：%s", failCount, failMsg);
        }
        result += "。";
        log.info("预算资产类型映射导入：{}", result);
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String syncFromView() {
        String currentYear = String.valueOf(Year.now().getValue());
        LambdaQueryWrapper<BudgetMemberNameCodeView> viewWrapper = new LambdaQueryWrapper<BudgetMemberNameCodeView>()
                .likeRight(BudgetMemberNameCodeView::getMemberCd, "CU205");
        List<BudgetMemberNameCodeView> viewList = budgetMemberNameCodeViewMapper.selectList(viewWrapper);
        if (viewList == null || viewList.isEmpty()) {
            log.info("视图 DATAINTEGRATION.VIEW_BUDGET_MEMBER_NAME_CODE 中无 MEMBER_CD 以 CU205 开头的数据");
            return "同步完成：视图无符合条件数据（MEMBER_CD 以 CU205 开头），新增 0 条。";
        }
        Set<String> existingCodes = budgetAssetTypeMappingMapper.selectList(
                new LambdaQueryWrapper<BudgetAssetTypeMapping>()
                        .eq(BudgetAssetTypeMapping::getYear, currentYear)
                        .select(BudgetAssetTypeMapping::getBudgetAssetTypeCode))
                .stream()
                .map(BudgetAssetTypeMapping::getBudgetAssetTypeCode)
                .filter(StringUtils::hasText)
                .collect(Collectors.toSet());
        String operator = getOperator();
        int addCount = 0;
        int skipCount = 0;
        for (BudgetMemberNameCodeView view : viewList) {
            String memberCd = view.getMemberCd();
            if (!StringUtils.hasText(memberCd)) {
                continue;
            }
            if (existingCodes.contains(memberCd)) {
                skipCount++;
                continue;
            }
            BudgetAssetTypeMapping entity = new BudgetAssetTypeMapping();
            entity.setBudgetAssetTypeCode(memberCd);
            entity.setBudgetAssetTypeName(view.getMemberNm());
            entity.setYear(currentYear);
            entity.setCreator(operator);
            entity.setUpdater(operator);
            entity.setChangeStatus("NEW");
            budgetAssetTypeMappingMapper.insert(entity);
            existingCodes.add(memberCd);
            addCount++;
        }
        String result = String.format("同步完成：从视图拉取 %d 条（MEMBER_CD 以 CU205 开头），新增 %d 条，已存在跳过 %d 条。", viewList.size(), addCount, skipCount);
        log.info("预算资产类型映射同步：{}", result);
        return result;
    }

    @Override
    public BudgetAssetTypeMappingPageVo getById(Long id) {
        if (id == null) {
            return null;
        }
        BudgetAssetTypeMapping entity = budgetAssetTypeMappingMapper.selectById(id);
        return entity == null ? null : convertToVo(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateManualFields(BudgetAssetTypeMappingUpdateReqVO req) {
        if (req == null || req.getId() == null) {
            throw new IllegalArgumentException("id 不能为空");
        }
        BudgetAssetTypeMapping entity = budgetAssetTypeMappingMapper.selectById(req.getId());
        if (entity == null) {
            throw new IllegalArgumentException("记录不存在，id=" + req.getId());
        }
        entity.setAssetMajorCategoryCode(req.getAssetMajorCategoryCode());
        entity.setAssetMajorCategoryName(req.getAssetMajorCategoryName());
        entity.setErpAssetType(req.getErpAssetType());
        entity.setAssetTypeName(req.getAssetTypeName());
        entity.setYear(req.getYear());
        entity.setChangeStatus(req.getChangeStatus());
        entity.setUpdater(getOperator());
        budgetAssetTypeMappingMapper.updateById(entity);
        log.info("更新预算资产类型映射手工维护字段，id={}", req.getId());
    }

    /** 解析「是否变更」：不变->UNCHANGED，新增->NEW，修改->MODIFY；空或未知视为不变 */
    private String parseChangeStatus(String changeStatus) {
        if (!StringUtils.hasText(changeStatus)) {
            return "UNCHANGED";
        }
        String s = changeStatus.trim();
        if ("不变".equals(s)) return "UNCHANGED";
        if ("新增".equals(s)) return "NEW";
        if ("修改".equals(s)) return "MODIFY";
        if ("UNCHANGED".equalsIgnoreCase(s)) return "UNCHANGED";
        if ("NEW".equalsIgnoreCase(s)) return "NEW";
        if ("MODIFY".equalsIgnoreCase(s)) return "MODIFY";
        return "UNCHANGED";
    }

    private BudgetAssetTypeMapping excelVoToEntity(BudgetAssetTypeMappingExcelVO vo, Long id) {
        BudgetAssetTypeMapping entity = new BudgetAssetTypeMapping();
        entity.setId(id);
        entity.setBudgetAssetTypeCode(vo.getBudgetAssetTypeCode());
        entity.setBudgetAssetTypeName(vo.getBudgetAssetTypeName());
        entity.setAssetMajorCategoryCode(vo.getAssetMajorCategoryCode());
        entity.setAssetMajorCategoryName(vo.getAssetMajorCategoryName());
        entity.setErpAssetType(vo.getErpAssetType());
        entity.setAssetTypeName(vo.getAssetTypeName());
        entity.setYear(vo.getYear());
        entity.setChangeStatus(parseChangeStatus(vo.getChangeStatus()));
        return entity;
    }

    /** 从 Excel 读入的主键字符串解析为 Long，避免数字精度丢失；空或非法返回 null */
    private static Long parseLongId(String idStr) {
        if (!StringUtils.hasText(idStr)) {
            return null;
        }
        String s = idStr.trim();
        try {
            return Long.parseLong(s);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static String getOperator() {
        try {
            String no = WebFrameworkUtils.getLoginUserNo();
            return no != null ? no : "import";
        } catch (Exception e) {
            return "import";
        }
    }
}
