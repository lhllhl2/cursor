package com.jasolar.mis.module.system.service.budget.log;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.jasolar.mis.framework.common.pojo.PageParam;
import com.jasolar.mis.framework.common.pojo.PageResult;
import com.jasolar.mis.module.system.controller.budget.vo.BudgetApiRequestLogQueryParams;
import com.jasolar.mis.module.system.controller.budget.vo.BudgetApiRequestLogViewVo;
import com.jasolar.mis.module.system.domain.budget.BudgetApiRequestLogView;
import com.jasolar.mis.module.system.mapper.budget.BudgetApiRequestLogViewMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 预算接口请求报文记录视图服务实现类
 * 
 * @author Auto Generated
 */
@Service
@Slf4j
public class BudgetApiRequestLogViewServiceImpl implements BudgetApiRequestLogViewService {

    @Resource
    private BudgetApiRequestLogViewMapper budgetApiRequestLogViewMapper;

    @Override
    public PageResult<BudgetApiRequestLogViewVo> pageQuery(BudgetApiRequestLogQueryParams params) {
        log.info("开始分页查询预算接口请求报文记录，params={}", params);

        // 构建查询条件
        LambdaQueryWrapper<BudgetApiRequestLogView> wrapper = buildQueryWrapper(params);

        // 分页查询
        PageParam pageParam = new PageParam();
        pageParam.setPageNo(params.getPageNo());
        pageParam.setPageSize(params.getPageSize());
        PageResult<BudgetApiRequestLogView> pageResult = budgetApiRequestLogViewMapper.selectPage(pageParam, wrapper);

        // 转换为VO
        List<BudgetApiRequestLogViewVo> voList = pageResult.getList().stream()
                .map(this::convertToVo)
                .collect(Collectors.toList());

        // 构建返回结果
        PageResult<BudgetApiRequestLogViewVo> result = new PageResult<>();
        result.setList(voList);
        result.setTotal(pageResult.getTotal());

        log.info("分页查询完成，总记录数={}, 当前页记录数={}", result.getTotal(), voList.size());
        return result;
    }

    /**
     * 构建查询条件
     * 支持DOC_NO、USER_IP、INTERFACE_NAME、RESPONSE_RESULT四个字段的模糊搜索
     * STATUS字段支持精确搜索
     * 注意：RESPONSE_RESULT是CLOB类型，需要使用DBMS_LOB.INSTR进行模糊搜索
     * 
     * @param params 查询参数
     * @return 查询条件包装器
     */
    private LambdaQueryWrapper<BudgetApiRequestLogView> buildQueryWrapper(BudgetApiRequestLogQueryParams params) {
        LambdaQueryWrapper<BudgetApiRequestLogView> wrapper = new LambdaQueryWrapper<BudgetApiRequestLogView>()
                .orderByDesc(BudgetApiRequestLogView::getCreateTime);

        // 单据号模糊搜索
        if (StringUtils.hasText(params.getDocNo())) {
            wrapper.like(BudgetApiRequestLogView::getDocNo, params.getDocNo());
        }

        // 用户IP模糊搜索
        if (StringUtils.hasText(params.getUserIp())) {
            wrapper.like(BudgetApiRequestLogView::getUserIp, params.getUserIp());
        }

        // 接口名称模糊搜索
        if (StringUtils.hasText(params.getInterfaceName())) {
            wrapper.like(BudgetApiRequestLogView::getInterfaceName, params.getInterfaceName());
        }

        // 请求参数模糊搜索（CLOB字段，使用DBMS_LOB.INSTR）
        if (StringUtils.hasText(params.getRequestParams())) {
            wrapper.apply("DBMS_LOB.INSTR(REQUEST_PARAMS, {0}, 1, 1) > 0", params.getRequestParams());
        }

        // 响应结果模糊搜索（CLOB字段，使用DBMS_LOB.INSTR）
        if (StringUtils.hasText(params.getResponseResult())) {
            wrapper.apply("DBMS_LOB.INSTR(RESPONSE_RESULT, {0}, 1, 1) > 0", params.getResponseResult());
        }

        // Controller名称精确匹配
        if (StringUtils.hasText(params.getControllerName())) {
            wrapper.eq(BudgetApiRequestLogView::getControllerName, params.getControllerName());
        }

        // 方法名精确匹配
        if (StringUtils.hasText(params.getMethodName())) {
            wrapper.eq(BudgetApiRequestLogView::getMethodName, params.getMethodName());
        }

        // 状态精确匹配
        if (StringUtils.hasText(params.getStatus())) {
            wrapper.eq(BudgetApiRequestLogView::getStatus, params.getStatus());
        }

        return wrapper;
    }

    /**
     * 转换为VO
     * 
     * @param view 视图实体
     * @return VO对象
     */
    private BudgetApiRequestLogViewVo convertToVo(BudgetApiRequestLogView view) {
        BudgetApiRequestLogViewVo vo = new BudgetApiRequestLogViewVo();
        BeanUtils.copyProperties(view, vo);
        return vo;
    }
}

