package com.jasolar.mis.module.system.mapper.ehr;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.jasolar.mis.framework.common.pojo.PageParam;
import com.jasolar.mis.framework.common.pojo.PageResult;
import com.jasolar.mis.framework.mybatis.core.mapper.BaseMapperX;
import com.jasolar.mis.module.system.domain.ehr.SubjectControlLevelView;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 科目控制层级视图 Mapper
 * 
 * @author Auto Generated
 * @date 2025-01-XX
 */
@Mapper
public interface SubjectControlLevelViewMapper extends BaseMapperX<SubjectControlLevelView> {

    /**
     * 根据ERP_ACCT_CD查询
     * 
     * @param erpAcctCd ERP科目编码
     * @return 科目控制层级视图列表
     */
    default List<SubjectControlLevelView> selectByErpAcctCd(String erpAcctCd) {
        LambdaQueryWrapper<SubjectControlLevelView> wrapper = new LambdaQueryWrapper<SubjectControlLevelView>()
                .eq(SubjectControlLevelView::getErpAcctCd, erpAcctCd);
        return selectList(wrapper);
    }

    /**
     * 根据CONTROL_CUST1_CD和CONTROL_ACCT_CD查询
     * 
     * @param controlCust1Cd 控制层级CUST1_CD
     * @param controlAcctCd 控制层级ACCT_CD
     * @return 科目控制层级视图列表
     */
    default List<SubjectControlLevelView> selectByControlCust1CdAndAcctCd(String controlCust1Cd, String controlAcctCd) {
        LambdaQueryWrapper<SubjectControlLevelView> wrapper = new LambdaQueryWrapper<SubjectControlLevelView>()
                .eq(SubjectControlLevelView::getControlCust1Cd, controlCust1Cd)
                .eq(SubjectControlLevelView::getControlAcctCd, controlAcctCd);
        return selectList(wrapper);
    }

    /**
     * 根据ERP_ACCT_CD列表批量查询
     * 
     * @param erpAcctCds ERP科目编码列表
     * @return 科目控制层级视图列表
     */
    default List<SubjectControlLevelView> selectByErpAcctCds(List<String> erpAcctCds) {
        LambdaQueryWrapper<SubjectControlLevelView> wrapper = new LambdaQueryWrapper<SubjectControlLevelView>()
                .in(SubjectControlLevelView::getErpAcctCd, erpAcctCds);
        return selectList(wrapper);
    }

    /**
     * 分页查询科目控制层级视图数据
     * 
     * @param pageParam 分页参数
     * @param queryWrapper 查询条件
     * @return 分页结果
     */
    default PageResult<SubjectControlLevelView> selectPage(PageParam pageParam, LambdaQueryWrapper<SubjectControlLevelView> queryWrapper) {
        com.baomidou.mybatisplus.extension.plugins.pagination.Page<SubjectControlLevelView> mpPage = 
            new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(pageParam.getPageNo(), pageParam.getPageSize());
        com.baomidou.mybatisplus.extension.plugins.pagination.Page<SubjectControlLevelView> pageResult = this.selectPage(mpPage, queryWrapper);
        return new PageResult<>(pageResult.getRecords(), pageResult.getTotal());
    }
}

