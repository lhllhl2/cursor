package com.jasolar.mis.module.system.mapper.budget;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.jasolar.mis.framework.common.pojo.PageParam;
import com.jasolar.mis.framework.common.pojo.PageResult;
import com.jasolar.mis.framework.mybatis.core.mapper.BaseMapperX;
import com.jasolar.mis.module.system.domain.budget.BudgetMemberNameCodeView;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 预算成员名称编码视图 Mapper
 * 
 * @author Auto Generated
 * @date 2025-01-XX
 */
@Mapper
public interface BudgetMemberNameCodeViewMapper extends BaseMapperX<BudgetMemberNameCodeView> {

    /**
     * 根据成员名称查询
     * 
     * @param memberNm 成员名称
     * @return 预算成员名称编码视图列表
     */
    default List<BudgetMemberNameCodeView> selectByMemberNm(String memberNm) {
        LambdaQueryWrapper<BudgetMemberNameCodeView> wrapper = new LambdaQueryWrapper<BudgetMemberNameCodeView>()
                .eq(BudgetMemberNameCodeView::getMemberNm, memberNm);
        return selectList(wrapper);
    }

    /**
     * 根据成员编码查询
     * 
     * @param memberCd 成员编码
     * @return 预算成员名称编码视图列表
     */
    default List<BudgetMemberNameCodeView> selectByMemberCd(String memberCd) {
        LambdaQueryWrapper<BudgetMemberNameCodeView> wrapper = new LambdaQueryWrapper<BudgetMemberNameCodeView>()
                .eq(BudgetMemberNameCodeView::getMemberCd, memberCd);
        return selectList(wrapper);
    }

    /**
     * 根据成员编码2查询
     * 
     * @param memberCd2 成员编码2
     * @return 预算成员名称编码视图列表
     */
    default List<BudgetMemberNameCodeView> selectByMemberCd2(String memberCd2) {
        LambdaQueryWrapper<BudgetMemberNameCodeView> wrapper = new LambdaQueryWrapper<BudgetMemberNameCodeView>()
                .eq(BudgetMemberNameCodeView::getMemberCd2, memberCd2);
        return selectList(wrapper);
    }

    /**
     * 根据成员编码列表批量查询
     * 
     * @param memberCds 成员编码列表
     * @return 预算成员名称编码视图列表
     */
    default List<BudgetMemberNameCodeView> selectByMemberCds(List<String> memberCds) {
        LambdaQueryWrapper<BudgetMemberNameCodeView> wrapper = new LambdaQueryWrapper<BudgetMemberNameCodeView>()
                .in(BudgetMemberNameCodeView::getMemberCd, memberCds);
        return selectList(wrapper);
    }

    /**
     * 根据成员编码2列表批量查询
     * 
     * @param memberCd2s 成员编码2列表
     * @return 预算成员名称编码视图列表
     */
    default List<BudgetMemberNameCodeView> selectByMemberCd2s(List<String> memberCd2s) {
        LambdaQueryWrapper<BudgetMemberNameCodeView> wrapper = new LambdaQueryWrapper<BudgetMemberNameCodeView>()
                .in(BudgetMemberNameCodeView::getMemberCd2, memberCd2s);
        return selectList(wrapper);
    }

    /**
     * 分页查询预算成员名称编码视图数据
     * 
     * @param pageParam 分页参数
     * @param queryWrapper 查询条件
     * @return 分页结果
     */
    default PageResult<BudgetMemberNameCodeView> selectPage(PageParam pageParam, LambdaQueryWrapper<BudgetMemberNameCodeView> queryWrapper) {
        com.baomidou.mybatisplus.extension.plugins.pagination.Page<BudgetMemberNameCodeView> mpPage = 
            new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(pageParam.getPageNo(), pageParam.getPageSize());
        com.baomidou.mybatisplus.extension.plugins.pagination.Page<BudgetMemberNameCodeView> pageResult = this.selectPage(mpPage, queryWrapper);
        return new PageResult<>(pageResult.getRecords(), pageResult.getTotal());
    }
}

