package com.jasolar.mis.module.system.mapper.ehr;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.jasolar.mis.framework.common.pojo.PageParam;
import com.jasolar.mis.framework.common.pojo.PageResult;
import com.jasolar.mis.framework.mybatis.core.mapper.BaseMapperX;
import com.jasolar.mis.module.system.domain.ehr.SubjectErpControlView;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 科目ERP控制层级视图 Mapper
 * 
 * @author Auto Generated
 * @date 2025-01-XX
 */
@Mapper
public interface SubjectErpControlViewMapper extends BaseMapperX<SubjectErpControlView> {

    /**
     * 根据ERP_ACCT_CD查询
     * 
     * @param erpAcctCd ERP科目编码
     * @return 科目ERP控制层级视图列表
     */
    default List<SubjectErpControlView> selectByErpAcctCd(String erpAcctCd) {
        LambdaQueryWrapper<SubjectErpControlView> wrapper = new LambdaQueryWrapper<SubjectErpControlView>()
                .eq(SubjectErpControlView::getErpAcctCd, erpAcctCd);
        return selectList(wrapper);
    }

    /**
     * 根据CUST1_CD和ACCT_CD查询
     * 
     * @param cust1Cd 客户1编码
     * @param acctCd 科目编码
     * @return 科目ERP控制层级视图列表
     */
    default List<SubjectErpControlView> selectByCust1CdAndAcctCd(String cust1Cd, String acctCd) {
        LambdaQueryWrapper<SubjectErpControlView> wrapper = new LambdaQueryWrapper<SubjectErpControlView>()
                .eq(SubjectErpControlView::getCust1Cd, cust1Cd)
                .eq(SubjectErpControlView::getAcctCd, acctCd);
        return selectList(wrapper);
    }

    /**
     * 根据CONTROL_ACCT_CD查询
     * 
     * @param controlAcctCd 控制层级ACCT_CD
     * @return 科目ERP控制层级视图列表
     */
    default List<SubjectErpControlView> selectByControlAcctCd(String controlAcctCd) {
        LambdaQueryWrapper<SubjectErpControlView> wrapper = new LambdaQueryWrapper<SubjectErpControlView>()
                .eq(SubjectErpControlView::getControlAcctCd, controlAcctCd);
        return selectList(wrapper);
    }

    /**
     * 根据CUST1_CD和CONTROL_ACCT_CD查询
     * 
     * @param cust1Cd 客户1编码
     * @param controlAcctCd 控制层级ACCT_CD
     * @return 科目ERP控制层级视图列表
     */
    default List<SubjectErpControlView> selectByCust1CdAndControlAcctCd(String cust1Cd, String controlAcctCd) {
        LambdaQueryWrapper<SubjectErpControlView> wrapper = new LambdaQueryWrapper<SubjectErpControlView>()
                .eq(SubjectErpControlView::getCust1Cd, cust1Cd)
                .eq(SubjectErpControlView::getControlAcctCd, controlAcctCd);
        return selectList(wrapper);
    }

    /**
     * 根据ERP_ACCT_CD列表批量查询
     * 
     * @param erpAcctCds ERP科目编码列表
     * @return 科目ERP控制层级视图列表
     */
    default List<SubjectErpControlView> selectByErpAcctCds(List<String> erpAcctCds) {
        LambdaQueryWrapper<SubjectErpControlView> wrapper = new LambdaQueryWrapper<SubjectErpControlView>()
                .in(SubjectErpControlView::getErpAcctCd, erpAcctCds);
        return selectList(wrapper);
    }

    /**
     * 根据CONTROL_ACCT_CD列表批量查询
     * 
     * @param controlAcctCds 控制层级ACCT_CD列表
     * @return 科目ERP控制层级视图列表
     */
    default List<SubjectErpControlView> selectByControlAcctCds(List<String> controlAcctCds) {
        LambdaQueryWrapper<SubjectErpControlView> wrapper = new LambdaQueryWrapper<SubjectErpControlView>()
                .in(SubjectErpControlView::getControlAcctCd, controlAcctCds);
        return selectList(wrapper);
    }

    /**
     * 分页查询科目ERP控制层级视图数据
     * 
     * @param pageParam 分页参数
     * @param queryWrapper 查询条件
     * @return 分页结果
     */
    default PageResult<SubjectErpControlView> selectPage(PageParam pageParam, LambdaQueryWrapper<SubjectErpControlView> queryWrapper) {
        com.baomidou.mybatisplus.extension.plugins.pagination.Page<SubjectErpControlView> mpPage = 
            new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(pageParam.getPageNo(), pageParam.getPageSize());
        com.baomidou.mybatisplus.extension.plugins.pagination.Page<SubjectErpControlView> pageResult = this.selectPage(mpPage, queryWrapper);
        return new PageResult<>(pageResult.getRecords(), pageResult.getTotal());
    }
}

