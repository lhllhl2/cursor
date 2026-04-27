package com.jasolar.mis.module.system.mapper.ehr;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.jasolar.mis.framework.common.pojo.PageParam;
import com.jasolar.mis.framework.common.pojo.PageResult;
import com.jasolar.mis.framework.mybatis.core.mapper.BaseMapperX;
import com.jasolar.mis.module.system.domain.ehr.EhrControlLevelView;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * EHR组织控制层级视图 Mapper
 * 
 * @author Auto Generated
 * @date 2025-01-XX
 */
@Mapper
public interface EhrControlLevelViewMapper extends BaseMapperX<EhrControlLevelView> {

    /**
     * 根据EHR_CD查询
     * 
     * @param ehrCd EHR组织代码
     * @return EHR控制层级视图
     */
    default EhrControlLevelView selectByEhrCd(String ehrCd) {
        LambdaQueryWrapper<EhrControlLevelView> wrapper = new LambdaQueryWrapper<EhrControlLevelView>()
                .eq(EhrControlLevelView::getEhrCd, ehrCd);
        return selectOne(wrapper);
    }

    /**
     * 根据CONTROL_EHR_CD查询
     * 
     * @param controlEhrCd 控制层级EHR组织代码
     * @return EHR控制层级视图列表
     */
    default List<EhrControlLevelView> selectByControlEhrCd(String controlEhrCd) {
        LambdaQueryWrapper<EhrControlLevelView> wrapper = new LambdaQueryWrapper<EhrControlLevelView>()
                .eq(EhrControlLevelView::getControlEhrCd, controlEhrCd);
        return selectList(wrapper);
    }

    /**
     * 根据EHR_CD列表批量查询
     * 
     * @param ehrCds EHR组织代码列表
     * @return EHR控制层级视图列表
     */
    default List<EhrControlLevelView> selectByEhrCds(List<String> ehrCds) {
        LambdaQueryWrapper<EhrControlLevelView> wrapper = new LambdaQueryWrapper<EhrControlLevelView>()
                .in(EhrControlLevelView::getEhrCd, ehrCds);
        return selectList(wrapper);
    }

    /**
     * 根据预算组织编码查询
     * 
     * @param budgetOrgCd 预算组织编码
     * @return EHR控制层级视图列表
     */
    default List<EhrControlLevelView> selectByBudgetOrgCd(String budgetOrgCd) {
        LambdaQueryWrapper<EhrControlLevelView> wrapper = new LambdaQueryWrapper<EhrControlLevelView>()
                .eq(EhrControlLevelView::getBudgetOrgCd, budgetOrgCd);
        return selectList(wrapper);
    }

    /**
     * 根据预算层级EHR组织编码查询
     * 
     * @param budgetEhrCd 预算层级EHR组织编码
     * @return EHR控制层级视图列表
     */
    default List<EhrControlLevelView> selectByBudgetEhrCd(String budgetEhrCd) {
        LambdaQueryWrapper<EhrControlLevelView> wrapper = new LambdaQueryWrapper<EhrControlLevelView>()
                .eq(EhrControlLevelView::getBudgetEhrCd, budgetEhrCd);
        return selectList(wrapper);
    }

    /**
     * 分页查询EHR控制层级视图数据
     * 
     * @param pageParam 分页参数
     * @param queryWrapper 查询条件
     * @return 分页结果
     */
    default PageResult<EhrControlLevelView> selectPage(PageParam pageParam, LambdaQueryWrapper<EhrControlLevelView> queryWrapper) {
        com.baomidou.mybatisplus.extension.plugins.pagination.Page<EhrControlLevelView> mpPage = 
            new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(pageParam.getPageNo(), pageParam.getPageSize());
        com.baomidou.mybatisplus.extension.plugins.pagination.Page<EhrControlLevelView> pageResult = this.selectPage(mpPage, queryWrapper);
        return new PageResult<>(pageResult.getRecords(), pageResult.getTotal());
    }
}

