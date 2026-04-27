package com.jasolar.mis.module.system.mapper.ehr;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.jasolar.mis.framework.common.pojo.PageParam;
import com.jasolar.mis.framework.common.pojo.PageResult;
import com.jasolar.mis.framework.mybatis.core.mapper.BaseMapperX;
import com.jasolar.mis.module.system.domain.ehr.EhrOrgManageHierarchyView;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * EHR组织管理层级视图 Mapper
 * 
 * @author Auto Generated
 * @date 2025-01-XX
 */
@Mapper
public interface EhrOrgManageHierarchyViewMapper extends BaseMapperX<EhrOrgManageHierarchyView> {

    /**
     * 根据EHR组织编码查询
     * 
     * @param ehrOrgCode EHR组织编码
     * @return EHR组织管理层级视图
     */
    default EhrOrgManageHierarchyView selectByEhrOrgCode(String ehrOrgCode) {
        LambdaQueryWrapper<EhrOrgManageHierarchyView> wrapper = new LambdaQueryWrapper<EhrOrgManageHierarchyView>()
                .eq(EhrOrgManageHierarchyView::getEhrOrgCode, ehrOrgCode);
        return selectOne(wrapper);
    }

    /**
     * 根据控制层级EHR编码查询
     * 
     * @param planOrgCode 控制层级EHR编码
     * @return EHR组织管理层级视图列表
     */
    default List<EhrOrgManageHierarchyView> selectByPlanOrgCode(String planOrgCode) {
        LambdaQueryWrapper<EhrOrgManageHierarchyView> wrapper = new LambdaQueryWrapper<EhrOrgManageHierarchyView>()
                .eq(EhrOrgManageHierarchyView::getPlanOrgCode, planOrgCode);
        return selectList(wrapper);
    }

    /**
     * 根据管理组织编码查询
     * 
     * @param morgCode 管理组织编码
     * @return EHR组织管理层级视图列表
     */
    default List<EhrOrgManageHierarchyView> selectByMorgCode(String morgCode) {
        LambdaQueryWrapper<EhrOrgManageHierarchyView> wrapper = new LambdaQueryWrapper<EhrOrgManageHierarchyView>()
                .eq(EhrOrgManageHierarchyView::getMorgCode, morgCode);
        return selectList(wrapper);
    }

    /**
     * 根据EHR组织编码列表批量查询
     * 
     * @param ehrOrgCodes EHR组织编码列表
     * @return EHR组织管理层级视图列表
     */
    default List<EhrOrgManageHierarchyView> selectByEhrOrgCodes(List<String> ehrOrgCodes) {
        LambdaQueryWrapper<EhrOrgManageHierarchyView> wrapper = new LambdaQueryWrapper<EhrOrgManageHierarchyView>()
                .in(EhrOrgManageHierarchyView::getEhrOrgCode, ehrOrgCodes);
        return selectList(wrapper);
    }

    /**
     * 根据ERP部门编码查询
     * 
     * @param erpDeptCode ERP部门编码
     * @return EHR组织管理层级视图列表
     */
    default List<EhrOrgManageHierarchyView> selectByErpDeptCode(String erpDeptCode) {
        LambdaQueryWrapper<EhrOrgManageHierarchyView> wrapper = new LambdaQueryWrapper<EhrOrgManageHierarchyView>()
                .eq(EhrOrgManageHierarchyView::getErpDeptCode, erpDeptCode);
        return selectList(wrapper);
    }

    /**
     * 分页查询EHR组织管理层级视图数据
     * 
     * @param pageParam 分页参数
     * @param queryWrapper 查询条件
     * @return 分页结果
     */
    default PageResult<EhrOrgManageHierarchyView> selectPage(PageParam pageParam, LambdaQueryWrapper<EhrOrgManageHierarchyView> queryWrapper) {
        com.baomidou.mybatisplus.extension.plugins.pagination.Page<EhrOrgManageHierarchyView> mpPage = 
            new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(pageParam.getPageNo(), pageParam.getPageSize());
        com.baomidou.mybatisplus.extension.plugins.pagination.Page<EhrOrgManageHierarchyView> pageResult = this.selectPage(mpPage, queryWrapper);
        return new PageResult<>(pageResult.getRecords(), pageResult.getTotal());
    }
}

