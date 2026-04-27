package com.jasolar.mis.module.system.service.budget.query.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jasolar.mis.framework.common.pojo.PageResult;
import com.jasolar.mis.module.system.controller.budget.vo.EhrControlLevelQueryParams;
import com.jasolar.mis.module.system.controller.budget.vo.EhrControlLevelQueryVo;
import com.jasolar.mis.module.system.domain.ehr.EhrControlLevelView;
import com.jasolar.mis.module.system.mapper.ehr.EhrControlLevelViewMapper;
import com.jasolar.mis.module.system.service.budget.query.EhrControlLevelQueryService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Description: EHR控制层级查询服务实现类
 * Author : Auto Generated
 * Date : 2025-01-XX
 * Version : 1.0
 */
@Service
@Slf4j
public class EhrControlLevelQueryServiceImpl implements EhrControlLevelQueryService {

    @Resource
    private EhrControlLevelViewMapper ehrControlLevelViewMapper;

    @Override
    public PageResult<EhrControlLevelQueryVo> queryEhrControlLevel(EhrControlLevelQueryParams params) {
        log.info("开始查询EHR控制层级数据，params={}", params);

        // 构建查询条件
        LambdaQueryWrapper<EhrControlLevelView> wrapper = buildQueryWrapper(params);

        // 分页查询
        Page<EhrControlLevelView> page = new Page<>(params.getPageNo(), params.getPageSize());
        IPage<EhrControlLevelView> pageResult = ehrControlLevelViewMapper.selectPage(page, wrapper);

        // 转换为VO
        List<EhrControlLevelQueryVo> voList = convertToVoList(pageResult.getRecords());

        // 构建分页结果
        PageResult<EhrControlLevelQueryVo> result = new PageResult<>(voList, pageResult.getTotal());
        log.info("EHR控制层级查询完成，总记录数={}, 当前页记录数={}", pageResult.getTotal(), voList.size());

        return result;
    }

    @Override
    public List<EhrControlLevelQueryVo> queryEhrControlLevelAll(EhrControlLevelQueryParams params) {
        log.info("开始查询EHR控制层级数据（全量导出），params={}", params);
        LambdaQueryWrapper<EhrControlLevelView> wrapper = buildQueryWrapper(params);
        List<EhrControlLevelView> list = ehrControlLevelViewMapper.selectList(wrapper);
        List<EhrControlLevelQueryVo> voList = convertToVoList(list);
        log.info("EHR控制层级全量查询完成，记录数={}", voList.size());
        return voList;
    }

    /**
     * 构建查询条件
     * 支持对所有字段（编码和名称）进行模糊搜索
     * 编码字段：EHR_CD、CONTROL_EHR_CD、BUDGET_ORG_CD、BUDGET_EHR_CD
     * 名称字段：EHR_NM、CONTROL_EHR_NM、BUDGET_ORG_NM、BUDGET_EHR_NM
     *
     * @param params 查询参数
     * @return 查询条件包装器
     */
    private LambdaQueryWrapper<EhrControlLevelView> buildQueryWrapper(EhrControlLevelQueryParams params) {
        LambdaQueryWrapper<EhrControlLevelView> wrapper = new LambdaQueryWrapper<>();

        // 编码字段模糊搜索
        // EHR组织代码模糊搜索
        if (StringUtils.hasText(params.getEhrCd())) {
            wrapper.like(EhrControlLevelView::getEhrCd, params.getEhrCd());
        }

        // 控制层级EHR组织代码模糊搜索
        if (StringUtils.hasText(params.getControlEhrCd())) {
            wrapper.like(EhrControlLevelView::getControlEhrCd, params.getControlEhrCd());
        }

        // 预算组织编码模糊搜索
        if (StringUtils.hasText(params.getBudgetOrgCd())) {
            wrapper.like(EhrControlLevelView::getBudgetOrgCd, params.getBudgetOrgCd());
        }

        // 预算层级EHR组织编码模糊搜索
        if (StringUtils.hasText(params.getBudgetEhrCd())) {
            wrapper.like(EhrControlLevelView::getBudgetEhrCd, params.getBudgetEhrCd());
        }

        // 名称字段模糊搜索
        // EHR组织名称模糊搜索
        if (StringUtils.hasText(params.getEhrNm())) {
            wrapper.like(EhrControlLevelView::getEhrNm, params.getEhrNm());
        }

        // 控制层级EHR组织名称模糊搜索
        if (StringUtils.hasText(params.getControlEhrNm())) {
            wrapper.like(EhrControlLevelView::getControlEhrNm, params.getControlEhrNm());
        }

        // 预算组织名称模糊搜索
        if (StringUtils.hasText(params.getBudgetOrgNm())) {
            wrapper.like(EhrControlLevelView::getBudgetOrgNm, params.getBudgetOrgNm());
        }

        // 预算层级EHR组织名称模糊搜索
        if (StringUtils.hasText(params.getBudgetEhrNm())) {
            wrapper.like(EhrControlLevelView::getBudgetEhrNm, params.getBudgetEhrNm());
        }

        // ERP部门编码模糊搜索
        if (StringUtils.hasText(params.getErpDepart())) {
            wrapper.like(EhrControlLevelView::getErpDepart, params.getErpDepart());
        }

        // 按EHR_CD排序（可选，根据业务需求调整）
        wrapper.orderByAsc(EhrControlLevelView::getEhrCd);

        return wrapper;
    }

    /**
     * 转换为VO列表
     *
     * @param viewList 视图实体列表
     * @return VO列表
     */
    private List<EhrControlLevelQueryVo> convertToVoList(List<EhrControlLevelView> viewList) {
        if (viewList == null || viewList.isEmpty()) {
            return new ArrayList<>();
        }

        List<EhrControlLevelQueryVo> voList = new ArrayList<>();
        for (EhrControlLevelView view : viewList) {
            EhrControlLevelQueryVo vo = new EhrControlLevelQueryVo();
            BeanUtils.copyProperties(view, vo);
            voList.add(vo);
        }

        return voList;
    }
}

