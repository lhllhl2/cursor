package com.jasolar.mis.module.system.mapper.budget;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.jasolar.mis.framework.common.pojo.PageParam;
import com.jasolar.mis.framework.common.pojo.PageResult;
import com.jasolar.mis.framework.mybatis.core.mapper.BaseMapperX;
import com.jasolar.mis.module.system.domain.budget.BudgetApiRequestLogView;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 预算接口请求报文记录视图 Mapper
 * 对应视图：V_BUDGET_API_REQUEST_LOG
 * 
 * @author Auto Generated
 */
@Mapper
public interface BudgetApiRequestLogViewMapper extends BaseMapperX<BudgetApiRequestLogView> {

    /**
     * 根据接口名称查询
     * 
     * @param interfaceName 接口名称
     * @return 预算接口请求报文记录视图列表
     */
    default List<BudgetApiRequestLogView> selectByInterfaceName(String interfaceName) {
        LambdaQueryWrapper<BudgetApiRequestLogView> wrapper = new LambdaQueryWrapper<BudgetApiRequestLogView>()
                .eq(BudgetApiRequestLogView::getInterfaceName, interfaceName)
                .orderByDesc(BudgetApiRequestLogView::getCreateTime);
        return selectList(wrapper);
    }

    /**
     * 根据单据号查询
     * 
     * @param docNo 单据号
     * @return 预算接口请求报文记录视图列表
     */
    default List<BudgetApiRequestLogView> selectByDocNo(String docNo) {
        LambdaQueryWrapper<BudgetApiRequestLogView> wrapper = new LambdaQueryWrapper<BudgetApiRequestLogView>()
                .eq(BudgetApiRequestLogView::getDocNo, docNo)
                .orderByDesc(BudgetApiRequestLogView::getCreateTime);
        return selectList(wrapper);
    }

    /**
     * 根据Controller名称查询
     * 
     * @param controllerName Controller类名
     * @return 预算接口请求报文记录视图列表
     */
    default List<BudgetApiRequestLogView> selectByControllerName(String controllerName) {
        LambdaQueryWrapper<BudgetApiRequestLogView> wrapper = new LambdaQueryWrapper<BudgetApiRequestLogView>()
                .eq(BudgetApiRequestLogView::getControllerName, controllerName)
                .orderByDesc(BudgetApiRequestLogView::getCreateTime);
        return selectList(wrapper);
    }

    /**
     * 根据Controller名称和方法名查询
     * 
     * @param controllerName Controller类名
     * @param methodName 方法名
     * @return 预算接口请求报文记录视图列表
     */
    default List<BudgetApiRequestLogView> selectByControllerAndMethod(String controllerName, String methodName) {
        LambdaQueryWrapper<BudgetApiRequestLogView> wrapper = new LambdaQueryWrapper<BudgetApiRequestLogView>()
                .eq(BudgetApiRequestLogView::getControllerName, controllerName)
                .eq(BudgetApiRequestLogView::getMethodName, methodName)
                .orderByDesc(BudgetApiRequestLogView::getCreateTime);
        return selectList(wrapper);
    }

    /**
     * 根据状态查询
     * 
     * @param status 状态（SUCCESS/ERROR）
     * @return 预算接口请求报文记录视图列表
     */
    default List<BudgetApiRequestLogView> selectByStatus(String status) {
        LambdaQueryWrapper<BudgetApiRequestLogView> wrapper = new LambdaQueryWrapper<BudgetApiRequestLogView>()
                .eq(BudgetApiRequestLogView::getStatus, status)
                .orderByDesc(BudgetApiRequestLogView::getCreateTime);
        return selectList(wrapper);
    }

    /**
     * 分页查询（按创建时间降序）
     * 
     * @param pageParam 分页参数
     * @return 分页结果
     */
    default PageResult<BudgetApiRequestLogView> selectPage(PageParam pageParam) {
        return selectPage(pageParam, new LambdaQueryWrapper<BudgetApiRequestLogView>()
                .orderByDesc(BudgetApiRequestLogView::getCreateTime));
    }

    /**
     * 根据接口名称分页查询
     * 
     * @param pageParam 分页参数
     * @param interfaceName 接口名称
     * @return 分页结果
     */
    default PageResult<BudgetApiRequestLogView> selectPageByInterfaceName(PageParam pageParam, String interfaceName) {
        return selectPage(pageParam, new LambdaQueryWrapper<BudgetApiRequestLogView>()
                .eq(BudgetApiRequestLogView::getInterfaceName, interfaceName)
                .orderByDesc(BudgetApiRequestLogView::getCreateTime));
    }

    /**
     * 根据单据号分页查询
     * 
     * @param pageParam 分页参数
     * @param docNo 单据号
     * @return 分页结果
     */
    default PageResult<BudgetApiRequestLogView> selectPageByDocNo(PageParam pageParam, String docNo) {
        return selectPage(pageParam, new LambdaQueryWrapper<BudgetApiRequestLogView>()
                .eq(BudgetApiRequestLogView::getDocNo, docNo)
                .orderByDesc(BudgetApiRequestLogView::getCreateTime));
    }

    /**
     * 根据创建时间范围查询
     * 
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 预算接口请求报文记录视图列表
     */
    default List<BudgetApiRequestLogView> selectByCreateTimeRange(java.time.LocalDateTime startTime, java.time.LocalDateTime endTime) {
        LambdaQueryWrapper<BudgetApiRequestLogView> wrapper = new LambdaQueryWrapper<BudgetApiRequestLogView>()
                .ge(BudgetApiRequestLogView::getCreateTime, startTime)
                .le(BudgetApiRequestLogView::getCreateTime, endTime)
                .orderByDesc(BudgetApiRequestLogView::getCreateTime);
        return selectList(wrapper);
    }

    /**
     * 根据单据号和方法名查询，按更新时间降序（用于取 UPDATE_TIME 最近且 methodName=apply 的一条）
     *
     * @param docNo 单据号（对应视图 DOC_NO）
     * @param methodName 方法名，如 "apply"
     * @return 列表，按 UPDATE_TIME 降序，取第一条即为最新
     */
    default List<BudgetApiRequestLogView> selectByDocNoAndMethodNameOrderByUpdateTimeDesc(String docNo, String methodName) {
        LambdaQueryWrapper<BudgetApiRequestLogView> wrapper = new LambdaQueryWrapper<BudgetApiRequestLogView>()
                .eq(BudgetApiRequestLogView::getDocNo, docNo)
                .eq(BudgetApiRequestLogView::getMethodName, methodName)
                .orderByDesc(BudgetApiRequestLogView::getUpdateTime);
        return selectList(wrapper);
    }
}

