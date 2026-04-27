package com.jasolar.mis.module.system.service.budget.query.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jasolar.mis.framework.common.pojo.PageResult;
import com.jasolar.mis.framework.common.security.LoginServletUtils;
import com.jasolar.mis.framework.common.security.LoginUser;
import com.jasolar.mis.module.system.controller.budget.vo.BudgetAmountVo;
import com.jasolar.mis.module.system.controller.budget.vo.BudgetAssetBalanceQueryParams;
import com.jasolar.mis.module.system.controller.budget.vo.BudgetBalanceQueryParams;
import com.jasolar.mis.module.system.controller.budget.vo.BudgetBalanceVo;
import com.jasolar.mis.module.system.controller.budget.vo.BudgetClaimStateVo;
import com.jasolar.mis.module.system.controller.budget.vo.BudgetProjectBalanceQueryParams;
import com.jasolar.mis.module.system.controller.budget.vo.PaymentAmountVo;
import com.jasolar.mis.module.system.controller.budget.vo.PaymentStatusQueryParams;
import com.jasolar.mis.module.system.domain.budget.BudgetClaimMonthlyAggregate;
import com.jasolar.mis.module.system.domain.budget.BudgetProjectQuarterlyAggregate;
import com.jasolar.mis.module.system.domain.budget.BudgetQuarterlyAggregate;
import com.jasolar.mis.module.system.domain.budget.BudgetQuarterlyAggregateByType;
import com.jasolar.mis.module.system.mapper.budget.BudgetClaimMonthlyAggregateMapper;
import com.jasolar.mis.module.system.mapper.budget.BudgetProjectQuarterlyAggregateMapper;
import com.jasolar.mis.module.system.mapper.budget.BudgetQuarterlyAggregateMapper;
import com.jasolar.mis.module.system.mapper.budget.BudgetQuarterlyAggregateByTypeMapper;
import com.jasolar.mis.module.system.mapper.admin.user.UserEhrOrgViewMapper;
import com.jasolar.mis.module.system.mapper.admin.user.UserProjectViewMapper;
import com.jasolar.mis.module.system.mapper.admin.user.SystemUserMapper;
import com.jasolar.mis.module.system.mapper.admin.usergroup.SystemUserGroupRMapper;
import com.jasolar.mis.module.system.mapper.admin.usergroup.UserGroupMapper;
import com.jasolar.mis.module.system.domain.admin.usergroup.SystemUserGroupRDo;
import com.jasolar.mis.module.system.domain.admin.usergroup.SystemUserGroupDo;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.jasolar.mis.module.system.service.budget.query.BudgetBalanceQueryService;
import com.jasolar.mis.module.system.service.admin.dict.SystemDictService;
import com.jasolar.mis.module.system.controller.admin.dict.vo.DictEditVo;
import com.jasolar.mis.module.system.controller.admin.dict.vo.DictInfoByCodeVo;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Description: 预算余额查询服务实现类
 * Author : Auto Generated
 * Date : 2025-01-XX
 * Version : 1.0
 */
@Service
@Slf4j
public class BudgetBalanceQueryServiceImpl implements BudgetBalanceQueryService {

    @Resource
    private BudgetQuarterlyAggregateMapper budgetQuarterlyAggregateMapper;

    @Resource
    private BudgetQuarterlyAggregateByTypeMapper budgetQuarterlyAggregateByTypeMapper;

    @Resource
    private BudgetProjectQuarterlyAggregateMapper budgetProjectQuarterlyAggregateMapper;

    @Resource
    private BudgetClaimMonthlyAggregateMapper budgetClaimMonthlyAggregateMapper;

    @Resource
    private UserEhrOrgViewMapper userEhrOrgViewMapper;

    @Resource
    private UserProjectViewMapper userProjectViewMapper;

    @Resource
    private SystemUserMapper systemUserMapper;

    @Resource
    private SystemUserGroupRMapper systemUserGroupRMapper;

    @Resource
    private UserGroupMapper userGroupMapper;

    @Resource
    private SystemDictService systemDictService;

    @Resource
    private com.jasolar.mis.module.system.mapper.budget.BudgetQuarterlyDetailMapper budgetQuarterlyDetailMapper;

    @Resource
    private com.jasolar.mis.module.system.mapper.ehr.EhrControlLevelViewMapper ehrControlLevelViewMapper;

    @Resource
    private com.jasolar.mis.module.system.mapper.budget.BudgetQuarterlyAggregateByMorgMapper budgetQuarterlyAggregateByMorgMapper;

    @Override
    public PageResult<BudgetBalanceVo> queryDeptBudgetBalance(BudgetBalanceQueryParams params) {
        log.info("开始查询部门费用可用预算，params={}", params);
        
        // 步骤一：获取登录用户名
        String userName = getLoginUserName();
        if (StringUtils.hasText(userName)) {
            log.info("当前登录用户名: {}", userName);
        } else {
            log.warn("无法获取登录用户名，将跳过权限过滤");
        }

        // 判断是否是管理员，如果是管理员则跳过权限过滤
        boolean isAdmin = false;
        if (StringUtils.hasText(userName)) {
            isAdmin = isAdminUser(userName);
            if (isAdmin) {
                log.info("用户 {} 是管理员，跳过权限过滤", userName);
            }
        }

        // 步骤二：获取用户权限数据（控制层级1的EHR组织编码）
        // 注意：部门费用预算查询不涉及项目，只需要EHR组织权限
        // 注意：数据视图已按控制层级1聚合，因此权限过滤也应使用CONTROL_EHR_CD
        // 如果是管理员，则跳过权限查询
        Set<String> allowedEhrCds = new HashSet<>();
        
        if (StringUtils.hasText(userName) && !isAdmin) {
            // 通过用户名从V_USER_EHR_ORG视图查询，获取CONTROL_EHR_CD集合（去重）
            List<com.jasolar.mis.module.system.domain.admin.user.UserEhrOrgView> ehrOrgViews = 
                    userEhrOrgViewMapper.selectByUserName(userName);
            if (!CollectionUtils.isEmpty(ehrOrgViews)) {
                allowedEhrCds = ehrOrgViews.stream()
                        .map(com.jasolar.mis.module.system.domain.admin.user.UserEhrOrgView::getControlEhrCd)
                        .filter(StringUtils::hasText)
                        .collect(Collectors.toSet());
                log.info("用户 {} 有权限的控制层级1 EHR组织编码数量: {}", userName, allowedEhrCds.size());
            }
        }
        
        // 如果EHR组织权限为空且不是管理员，返回空结果（用户没有权限）
        if (!isAdmin && allowedEhrCds.isEmpty() && StringUtils.hasText(userName)) {
            log.warn("用户 {} 没有任何EHR组织权限，返回空结果", userName);
            PageResult<BudgetBalanceVo> pageResult = new PageResult<>();
            pageResult.setList(Collections.emptyList());
            pageResult.setTotal(0L);
            return pageResult;
        }
        
        // 将权限集合设置到查询参数中
        if (!allowedEhrCds.isEmpty()) {
            params.setEhrCdList(new ArrayList<>(allowedEhrCds));
        }
        
        // 确保分页参数不为空
        if (params.getPageNo() == null || params.getPageNo() <= 0) {
            params.setPageNo(1);
        }
        if (params.getPageSize() == null || params.getPageSize() <= 0) {
            params.setPageSize(20);
        }
        
        log.info("分页参数：pageNo={}, pageSize={}", params.getPageNo(), params.getPageSize());
        
        // 先查询总数
        Long total = budgetQuarterlyAggregateMapper.selectCountByParams(params);
        log.info("查询总数：total={}", total);
        
        // 计算分页的起始位置和结束位置（Oracle ROW_NUMBER从1开始）
        int startRow = (params.getPageNo() - 1) * params.getPageSize() + 1;
        int endRow = params.getPageNo() * params.getPageSize();
        log.info("分页范围：startRow={}, endRow={}", startRow, endRow);
        
        // 查询视图数据（使用ROW_NUMBER窗口函数分页，直接返回List，避免MyBatis-Plus分页插件拦截）
        List<BudgetQuarterlyAggregate> records = budgetQuarterlyAggregateMapper.selectPageByParams(startRow, endRow, params);
        
        log.info("查询结果：records size={}, 预期size={}", records.size(), params.getPageSize());
        
        // 如果返回的记录数超过pageSize，说明分页没有生效，需要手动截取（临时保护措施）
        if (records.size() > params.getPageSize()) {
            log.error("分页未生效！返回了{}条记录，预期{}条，手动截取前{}条", records.size(), params.getPageSize(), params.getPageSize());
            records = records.subList(0, params.getPageSize());
        }
        
        // 转换为 BudgetBalanceVo
        List<BudgetBalanceVo> voList = convertToBudgetBalanceVoList(records);
        
        // 构建返回结果
        PageResult<BudgetBalanceVo> pageResult = new PageResult<>();
        pageResult.setList(voList);
        pageResult.setTotal(total); // 使用查询的总数
        
        log.info("返回分页结果：total={}, list size={}", pageResult.getTotal(), pageResult.getList().size());
        
        return pageResult;
    }

    @Override
    public List<BudgetBalanceVo> queryDeptBudgetBalanceAll(BudgetBalanceQueryParams params) {
        log.info("开始查询部门费用可用预算（全量导出），params={}", params);
        
        // 步骤一：获取登录用户名
        String userName = getLoginUserName();
        if (StringUtils.hasText(userName)) {
            log.info("当前登录用户名: {}", userName);
        } else {
            log.warn("无法获取登录用户名，将跳过权限过滤");
        }

        // 判断是否是管理员，如果是管理员则跳过权限过滤
        boolean isAdmin = false;
        if (StringUtils.hasText(userName)) {
            isAdmin = isAdminUser(userName);
            if (isAdmin) {
                log.info("用户 {} 是管理员，跳过权限过滤", userName);
            }
        }

        // 步骤二：获取用户权限数据（控制层级1的EHR组织编码）
        // 注意：部门费用预算查询不涉及项目，只需要EHR组织权限
        // 注意：数据视图已按控制层级1聚合，因此权限过滤也应使用CONTROL_EHR_CD
        // 如果是管理员，则跳过权限查询
        Set<String> allowedEhrCds = new HashSet<>();
        
        if (StringUtils.hasText(userName) && !isAdmin) {
            // 通过用户名从V_USER_EHR_ORG视图查询，获取CONTROL_EHR_CD集合（去重）
            List<com.jasolar.mis.module.system.domain.admin.user.UserEhrOrgView> ehrOrgViews = 
                    userEhrOrgViewMapper.selectByUserName(userName);
            if (!CollectionUtils.isEmpty(ehrOrgViews)) {
                allowedEhrCds = ehrOrgViews.stream()
                        .map(com.jasolar.mis.module.system.domain.admin.user.UserEhrOrgView::getControlEhrCd)
                        .filter(StringUtils::hasText)
                        .collect(Collectors.toSet());
                log.info("用户 {} 有权限的控制层级1 EHR组织编码数量: {}", userName, allowedEhrCds.size());
            }
        }
        
        // 如果EHR组织权限为空且不是管理员，返回空结果（用户没有权限）
        if (!isAdmin && allowedEhrCds.isEmpty() && StringUtils.hasText(userName)) {
            log.warn("用户 {} 没有任何EHR组织权限，返回空结果", userName);
            return Collections.emptyList();
        }
        
        // 将权限集合设置到查询参数中
        if (!allowedEhrCds.isEmpty()) {
            params.setEhrCdList(new ArrayList<>(allowedEhrCds));
        }
        
        // 查询全量数据（不分页）
        List<BudgetQuarterlyAggregate> records = budgetQuarterlyAggregateMapper.selectListByParams(params);
        log.info("查询全量数据结果：records size={}", records.size());
        
        // 转换为 BudgetBalanceVo
        List<BudgetBalanceVo> voList = convertToBudgetBalanceVoList(records);
        
        log.info("返回全量数据结果：list size={}", voList.size());
        
        return voList;
    }

    /**
     * 将 BudgetQuarterlyAggregate 列表转换为 BudgetBalanceVo 列表
     */
    private List<BudgetBalanceVo> convertToBudgetBalanceVoList(List<BudgetQuarterlyAggregate> aggregateList) {
        List<BudgetBalanceVo> voList = new ArrayList<>();
        
        for (BudgetQuarterlyAggregate aggregate : aggregateList) {
            BudgetBalanceVo vo = new BudgetBalanceVo();
            
            // 设置基本信息
            vo.setEhrCode(aggregate.getControlEhrCode());
            vo.setEhrName(aggregate.getControlEhrName());
            vo.setSubjectCode(aggregate.getControlAccountSubjectCode());
            vo.setSubjectName(aggregate.getControlAccountSubjectName());
            vo.setControlCust1Cd(aggregate.getControlCust1Cd());
            vo.setControlCust1Name(aggregate.getControlCust1Name());
            
            // 设置去年使用预算数
            vo.setLastYearUsedBudget(aggregate.getLastYearUsedBudget());
            
            // 设置Q1季度数据
            vo.setQ1(createBudgetAmountVo(
                aggregate.getQ1AmountTotal(),
                aggregate.getQ1AmountAdj(),
                aggregate.getQ1TotalBudget(),
                aggregate.getQ1AmountFrozen(),
                aggregate.getQ1AmountOccupied(),
                aggregate.getQ1AmountActual(),
                aggregate.getQ1AmountActualApproved(),
                aggregate.getQ1AmountAvailable()
            ));
            
            // 设置Q2季度数据
            vo.setQ2(createBudgetAmountVo(
                aggregate.getQ2AmountTotal(),
                aggregate.getQ2AmountAdj(),
                aggregate.getQ2TotalBudget(),
                aggregate.getQ2AmountFrozen(),
                aggregate.getQ2AmountOccupied(),
                aggregate.getQ2AmountActual(),
                aggregate.getQ2AmountActualApproved(),
                aggregate.getQ2AmountAvailable()
            ));
            
            // 设置Q3季度数据
            vo.setQ3(createBudgetAmountVo(
                aggregate.getQ3AmountTotal(),
                aggregate.getQ3AmountAdj(),
                aggregate.getQ3TotalBudget(),
                aggregate.getQ3AmountFrozen(),
                aggregate.getQ3AmountOccupied(),
                aggregate.getQ3AmountActual(),
                aggregate.getQ3AmountActualApproved(),
                aggregate.getQ3AmountAvailable()
            ));
            
            // 设置Q4季度数据
            vo.setQ4(createBudgetAmountVo(
                aggregate.getQ4AmountTotal(),
                aggregate.getQ4AmountAdj(),
                aggregate.getQ4TotalBudget(),
                aggregate.getQ4AmountFrozen(),
                aggregate.getQ4AmountOccupied(),
                aggregate.getQ4AmountActual(),
                aggregate.getQ4AmountActualApproved(),
                aggregate.getQ4AmountAvailable()
            ));
            
            // 计算总计
            vo.setTotal(calculateTotal(vo.getQ1(), vo.getQ2(), vo.getQ3(), vo.getQ4()));
            
            voList.add(vo);
        }
        
        return voList;
    }

    /**
     * 创建 BudgetAmountVo 对象（包含已批准实际金额）
     */
    private BudgetAmountVo createBudgetAmountVo(
            BigDecimal amountTotal,
            BigDecimal amountAdj,
            BigDecimal totalBudget,
            BigDecimal amountFrozen,
            BigDecimal amountOccupied,
            BigDecimal amountActual,
            BigDecimal amountActualApproved,
            BigDecimal amountAvailable) {
        BudgetAmountVo vo = new BudgetAmountVo();
        vo.setAmountTotal(totalBudget); // 总金额取视图的XX_TOTAL_BUDGET（年度预算数 + 预算调整数）
        vo.setAmountYearTotal(amountTotal); // 年度总金额取视图的XX_AMOUNT_TOTAL（年度预算数）
        vo.setAmountAdj(amountAdj);
        vo.setAmountFrozen(amountFrozen);
        vo.setAmountOccupied(amountOccupied);
        vo.setAmountActual(amountActual);
        vo.setAmountActualApproved(amountActualApproved);
        vo.setAmountAvailable(amountAvailable);
        return vo;
    }

    /**
     * 创建 BudgetAmountVo 对象（不包含已批准实际金额，兼容旧代码）
     */
    private BudgetAmountVo createBudgetAmountVo(
            BigDecimal amountTotal,
            BigDecimal amountAdj,
            BigDecimal totalBudget,
            BigDecimal amountFrozen,
            BigDecimal amountOccupied,
            BigDecimal amountActual,
            BigDecimal amountAvailable) {
        return createBudgetAmountVo(amountTotal, amountAdj, totalBudget, amountFrozen, amountOccupied, amountActual, null, amountAvailable);
    }

    /**
     * 计算四个季度的总计
     */
    private BudgetAmountVo calculateTotal(BudgetAmountVo q1, BudgetAmountVo q2, BudgetAmountVo q3, BudgetAmountVo q4) {
        BudgetAmountVo total = new BudgetAmountVo();
        total.setAmountTotal(add(q1.getAmountTotal(), q2.getAmountTotal(), q3.getAmountTotal(), q4.getAmountTotal()));
        total.setAmountYearTotal(add(q1.getAmountYearTotal(), q2.getAmountYearTotal(), q3.getAmountYearTotal(), q4.getAmountYearTotal()));
        total.setAmountAdj(add(q1.getAmountAdj(), q2.getAmountAdj(), q3.getAmountAdj(), q4.getAmountAdj()));
        total.setAmountFrozen(add(q1.getAmountFrozen(), q2.getAmountFrozen(), q3.getAmountFrozen(), q4.getAmountFrozen()));
        total.setAmountOccupied(add(q1.getAmountOccupied(), q2.getAmountOccupied(), q3.getAmountOccupied(), q4.getAmountOccupied()));
        total.setAmountActual(add(q1.getAmountActual(), q2.getAmountActual(), q3.getAmountActual(), q4.getAmountActual()));
        total.setAmountActualApproved(add(q1.getAmountActualApproved(), q2.getAmountActualApproved(), q3.getAmountActualApproved(), q4.getAmountActualApproved()));
        total.setAmountAvailable(add(q1.getAmountAvailable(), q2.getAmountAvailable(), q3.getAmountAvailable(), q4.getAmountAvailable()));
        return total;
    }

    /**
     * 安全的 BigDecimal 相加
     */
    private BigDecimal add(BigDecimal... values) {
        BigDecimal result = BigDecimal.ZERO;
        for (BigDecimal value : values) {
            if (value != null) {
                result = result.add(value);
            }
        }
        return result;
    }

    @Override
    public PageResult<BudgetBalanceVo> queryDeptAssetBudgetBalance(BudgetAssetBalanceQueryParams params) {
        log.info("开始查询部门资产可用预算，params={}", params);
        
        // 步骤一：获取登录用户名
        String userName = getLoginUserName();
        if (StringUtils.hasText(userName)) {
            log.info("当前登录用户名: {}", userName);
        } else {
            log.warn("无法获取登录用户名，将跳过权限过滤");
        }

        // 判断是否是管理员，如果是管理员则跳过权限过滤
        boolean isAdmin = false;
        if (StringUtils.hasText(userName)) {
            isAdmin = isAdminUser(userName);
            if (isAdmin) {
                log.info("用户 {} 是管理员，跳过权限过滤", userName);
            }
        }

        // 步骤二：获取用户权限数据（控制层级1的EHR组织编码）
        // 注意：部门资产预算查询不涉及项目，只需要EHR组织权限
        // 注意：数据视图已按控制层级1聚合，因此权限过滤也应使用CONTROL_EHR_CD
        // 如果是管理员，则跳过权限查询
        Set<String> allowedEhrCds = new HashSet<>();
        
        if (StringUtils.hasText(userName) && !isAdmin) {
            // 通过用户名从V_USER_EHR_ORG视图查询，获取CONTROL_EHR_CD集合（去重）
            List<com.jasolar.mis.module.system.domain.admin.user.UserEhrOrgView> ehrOrgViews = 
                    userEhrOrgViewMapper.selectByUserName(userName);
            if (!CollectionUtils.isEmpty(ehrOrgViews)) {
                allowedEhrCds = ehrOrgViews.stream()
                        .map(com.jasolar.mis.module.system.domain.admin.user.UserEhrOrgView::getControlEhrCd)
                        .filter(StringUtils::hasText)
                        .collect(Collectors.toSet());
                log.info("用户 {} 有权限的控制层级1 EHR组织编码数量: {}", userName, allowedEhrCds.size());
            }
        }
        
        // 如果EHR组织权限为空且不是管理员，返回空结果（用户没有权限）
        if (!isAdmin && allowedEhrCds.isEmpty() && StringUtils.hasText(userName)) {
            log.warn("用户 {} 没有任何EHR组织权限，返回空结果", userName);
            PageResult<BudgetBalanceVo> pageResult = new PageResult<>();
            pageResult.setList(Collections.emptyList());
            pageResult.setTotal(0L);
            return pageResult;
        }
        
        // 将权限集合设置到查询参数中
        if (!allowedEhrCds.isEmpty()) {
            params.setEhrCdList(new ArrayList<>(allowedEhrCds));
        }
        
        // 确保分页参数不为空
        if (params.getPageNo() == null || params.getPageNo() <= 0) {
            params.setPageNo(1);
        }
        if (params.getPageSize() == null || params.getPageSize() <= 0) {
            params.setPageSize(20);
        }
        
        // 如果year为空，默认使用当前年份
        if (params.getYear() == null || params.getYear().isEmpty()) {
            Calendar calendar = Calendar.getInstance();
            String currentYear = String.valueOf(calendar.get(Calendar.YEAR));
            params.setYear(currentYear);
            log.info("year参数为空，使用当前年份：{}", currentYear);
        }
        
        log.info("分页参数：pageNo={}, pageSize={}, year={}, budgetType={}", 
                params.getPageNo(), params.getPageSize(), params.getYear(), params.getBudgetType());
        
        // 先查询总数
        Long total = budgetQuarterlyAggregateByTypeMapper.selectCountByParams(params);
        log.info("查询总数：total={}", total);
        
        // 计算分页的起始位置和结束位置（Oracle ROW_NUMBER从1开始）
        int startRow = (params.getPageNo() - 1) * params.getPageSize() + 1;
        int endRow = params.getPageNo() * params.getPageSize();
        log.info("分页范围：startRow={}, endRow={}", startRow, endRow);
        
        // 查询视图数据（使用ROW_NUMBER窗口函数分页，直接返回List，避免MyBatis-Plus分页插件拦截）
        List<BudgetQuarterlyAggregateByType> records = budgetQuarterlyAggregateByTypeMapper.selectPageByParams(startRow, endRow, params);
        
        log.info("查询结果：records size={}, 预期size={}", records.size(), params.getPageSize());
        
        // 如果返回的记录数超过pageSize，说明分页没有生效，需要手动截取（临时保护措施）
        if (records.size() > params.getPageSize()) {
            log.error("分页未生效！返回了{}条记录，预期{}条，手动截取前{}条", records.size(), params.getPageSize(), params.getPageSize());
            records = records.subList(0, params.getPageSize());
        }
        
        // 转换为 BudgetBalanceVo（根据 budgetType 选择对应的字段）
        List<BudgetBalanceVo> voList = convertToBudgetBalanceVoListByType(records, params.getBudgetType());
        
        // 构建返回结果
        PageResult<BudgetBalanceVo> pageResult = new PageResult<>();
        pageResult.setList(voList);
        pageResult.setTotal(total); // 使用查询的总数
        
        log.info("返回分页结果：total={}, list size={}", pageResult.getTotal(), pageResult.getList().size());
        
        return pageResult;
    }

    /**
     * 将 BudgetQuarterlyAggregateByType 列表转换为 BudgetBalanceVo 列表
     * 根据 budgetType 选择对应的字段（PURCHASE 或 PAYMENT）
     */
    private List<BudgetBalanceVo> convertToBudgetBalanceVoListByType(
            List<BudgetQuarterlyAggregateByType> aggregateList, 
            String budgetType) {
        List<BudgetBalanceVo> voList = new ArrayList<>();
        
        boolean isPurchase = "PURCHASE".equalsIgnoreCase(budgetType);
        boolean isPayment = "PAYMENT".equalsIgnoreCase(budgetType);
        
        for (BudgetQuarterlyAggregateByType aggregate : aggregateList) {
            BudgetBalanceVo vo = new BudgetBalanceVo();
            
            // 设置基本信息
            vo.setEhrCode(aggregate.getEhrCd());
            vo.setEhrName(aggregate.getEhrName());
            vo.setErpAssetType(aggregate.getErpAssetType());
            vo.setErpAssetTypeName(aggregate.getErpAssetTypeName());
            
            // 根据 budgetType 设置去年使用预算数
            if (isPurchase) {
                // 采购额：使用 purchaseLastYearUsage
                vo.setLastYearUsedBudget(aggregate.getPurchaseLastYearUsage());
            } else if (isPayment) {
                // 付款额：使用 paymentLastYearUsage
                vo.setLastYearUsedBudget(aggregate.getPaymentLastYearUsage());
            } else {
                // 默认使用采购额
                vo.setLastYearUsedBudget(aggregate.getPurchaseLastYearUsage());
            }
            
            // 根据 budgetType 选择对应的字段
            if (isPurchase) {
                // 采购额：使用 PURCHASE_* 开头的字段
                vo.setQ1(createBudgetAmountVo(
                    aggregate.getPurchaseQ1AmountTotal(),
                    aggregate.getPurchaseQ1AmountAdj(),
                    aggregate.getPurchaseQ1TotalBudget(),
                    aggregate.getPurchaseQ1AmountFrozen(),
                    aggregate.getPurchaseQ1AmountOccupied(),
                    aggregate.getPurchaseQ1AmountActual(),
                    aggregate.getPurchaseQ1AmountActualApproved(),
                    aggregate.getPurchaseQ1AmountAvailable()
                ));
                
                vo.setQ2(createBudgetAmountVo(
                    aggregate.getPurchaseQ2AmountTotal(),
                    aggregate.getPurchaseQ2AmountAdj(),
                    aggregate.getPurchaseQ2TotalBudget(),
                    aggregate.getPurchaseQ2AmountFrozen(),
                    aggregate.getPurchaseQ2AmountOccupied(),
                    aggregate.getPurchaseQ2AmountActual(),
                    aggregate.getPurchaseQ2AmountActualApproved(),
                    aggregate.getPurchaseQ2AmountAvailable()
                ));
                
                vo.setQ3(createBudgetAmountVo(
                    aggregate.getPurchaseQ3AmountTotal(),
                    aggregate.getPurchaseQ3AmountAdj(),
                    aggregate.getPurchaseQ3TotalBudget(),
                    aggregate.getPurchaseQ3AmountFrozen(),
                    aggregate.getPurchaseQ3AmountOccupied(),
                    aggregate.getPurchaseQ3AmountActual(),
                    aggregate.getPurchaseQ3AmountActualApproved(),
                    aggregate.getPurchaseQ3AmountAvailable()
                ));
                
                vo.setQ4(createBudgetAmountVo(
                    aggregate.getPurchaseQ4AmountTotal(),
                    aggregate.getPurchaseQ4AmountAdj(),
                    aggregate.getPurchaseQ4TotalBudget(),
                    aggregate.getPurchaseQ4AmountFrozen(),
                    aggregate.getPurchaseQ4AmountOccupied(),
                    aggregate.getPurchaseQ4AmountActual(),
                    aggregate.getPurchaseQ4AmountActualApproved(),
                    aggregate.getPurchaseQ4AmountAvailable()
                ));
            } else if (isPayment) {
                // 付款额：使用 PAYMENT_* 开头的字段
                vo.setQ1(createBudgetAmountVo(
                    aggregate.getPaymentQ1AmountTotal(),
                    aggregate.getPaymentQ1AmountAdj(),
                    aggregate.getPaymentQ1TotalBudget(),
                    aggregate.getPaymentQ1AmountFrozen(),
                    aggregate.getPaymentQ1AmountOccupied(),
                    aggregate.getPaymentQ1AmountActual(),
                    aggregate.getPaymentQ1AmountActualApproved(),
                    aggregate.getPaymentQ1AmountAvailable()
                ));
                
                vo.setQ2(createBudgetAmountVo(
                    aggregate.getPaymentQ2AmountTotal(),
                    aggregate.getPaymentQ2AmountAdj(),
                    aggregate.getPaymentQ2TotalBudget(),
                    aggregate.getPaymentQ2AmountFrozen(),
                    aggregate.getPaymentQ2AmountOccupied(),
                    aggregate.getPaymentQ2AmountActual(),
                    aggregate.getPaymentQ2AmountActualApproved(),
                    aggregate.getPaymentQ2AmountAvailable()
                ));
                
                vo.setQ3(createBudgetAmountVo(
                    aggregate.getPaymentQ3AmountTotal(),
                    aggregate.getPaymentQ3AmountAdj(),
                    aggregate.getPaymentQ3TotalBudget(),
                    aggregate.getPaymentQ3AmountFrozen(),
                    aggregate.getPaymentQ3AmountOccupied(),
                    aggregate.getPaymentQ3AmountActual(),
                    aggregate.getPaymentQ3AmountActualApproved(),
                    aggregate.getPaymentQ3AmountAvailable()
                ));
                
                vo.setQ4(createBudgetAmountVo(
                    aggregate.getPaymentQ4AmountTotal(),
                    aggregate.getPaymentQ4AmountAdj(),
                    aggregate.getPaymentQ4TotalBudget(),
                    aggregate.getPaymentQ4AmountFrozen(),
                    aggregate.getPaymentQ4AmountOccupied(),
                    aggregate.getPaymentQ4AmountActual(),
                    aggregate.getPaymentQ4AmountActualApproved(),
                    aggregate.getPaymentQ4AmountAvailable()
                ));
            } else {
                log.warn("未知的预算类型：{}，默认使用采购额字段", budgetType);
                // 默认使用采购额字段
                vo.setQ1(createBudgetAmountVo(
                    aggregate.getPurchaseQ1AmountTotal(),
                    aggregate.getPurchaseQ1AmountAdj(),
                    aggregate.getPurchaseQ1TotalBudget(),
                    aggregate.getPurchaseQ1AmountFrozen(),
                    aggregate.getPurchaseQ1AmountOccupied(),
                    aggregate.getPurchaseQ1AmountActual(),
                    aggregate.getPurchaseQ1AmountActualApproved(),
                    aggregate.getPurchaseQ1AmountAvailable()
                ));
                
                vo.setQ2(createBudgetAmountVo(
                    aggregate.getPurchaseQ2AmountTotal(),
                    aggregate.getPurchaseQ2AmountAdj(),
                    aggregate.getPurchaseQ2TotalBudget(),
                    aggregate.getPurchaseQ2AmountFrozen(),
                    aggregate.getPurchaseQ2AmountOccupied(),
                    aggregate.getPurchaseQ2AmountActual(),
                    aggregate.getPurchaseQ2AmountActualApproved(),
                    aggregate.getPurchaseQ2AmountAvailable()
                ));
                
                vo.setQ3(createBudgetAmountVo(
                    aggregate.getPurchaseQ3AmountTotal(),
                    aggregate.getPurchaseQ3AmountAdj(),
                    aggregate.getPurchaseQ3TotalBudget(),
                    aggregate.getPurchaseQ3AmountFrozen(),
                    aggregate.getPurchaseQ3AmountOccupied(),
                    aggregate.getPurchaseQ3AmountActual(),
                    aggregate.getPurchaseQ3AmountActualApproved(),
                    aggregate.getPurchaseQ3AmountAvailable()
                ));
                
                vo.setQ4(createBudgetAmountVo(
                    aggregate.getPurchaseQ4AmountTotal(),
                    aggregate.getPurchaseQ4AmountAdj(),
                    aggregate.getPurchaseQ4TotalBudget(),
                    aggregate.getPurchaseQ4AmountFrozen(),
                    aggregate.getPurchaseQ4AmountOccupied(),
                    aggregate.getPurchaseQ4AmountActual(),
                    aggregate.getPurchaseQ4AmountActualApproved(),
                    aggregate.getPurchaseQ4AmountAvailable()
                ));
            }
            
            // 计算总计
            vo.setTotal(calculateTotal(vo.getQ1(), vo.getQ2(), vo.getQ3(), vo.getQ4()));
            
            voList.add(vo);
        }
        
        return voList;
    }

    @Override
    public List<BudgetBalanceVo> queryDeptAssetBudgetBalanceAll(BudgetAssetBalanceQueryParams params) {
        log.info("开始查询部门资产可用预算（全量导出），params={}", params);
        
        // 步骤一：获取登录用户名
        String userName = getLoginUserName();
        if (StringUtils.hasText(userName)) {
            log.info("当前登录用户名: {}", userName);
        } else {
            log.warn("无法获取登录用户名，将跳过权限过滤");
        }

        // 判断是否是管理员，如果是管理员则跳过权限过滤
        boolean isAdmin = false;
        if (StringUtils.hasText(userName)) {
            isAdmin = isAdminUser(userName);
            if (isAdmin) {
                log.info("用户 {} 是管理员，跳过权限过滤", userName);
            }
        }

        // 步骤二：获取用户权限数据（控制层级1的EHR组织编码）
        // 注意：部门资产预算查询不涉及项目，只需要EHR组织权限
        // 注意：数据视图已按控制层级1聚合，因此权限过滤也应使用CONTROL_EHR_CD
        // 如果是管理员，则跳过权限查询
        Set<String> allowedEhrCds = new HashSet<>();
        
        if (StringUtils.hasText(userName) && !isAdmin) {
            // 通过用户名从V_USER_EHR_ORG视图查询，获取CONTROL_EHR_CD集合（去重）
            List<com.jasolar.mis.module.system.domain.admin.user.UserEhrOrgView> ehrOrgViews = 
                    userEhrOrgViewMapper.selectByUserName(userName);
            if (!CollectionUtils.isEmpty(ehrOrgViews)) {
                allowedEhrCds = ehrOrgViews.stream()
                        .map(com.jasolar.mis.module.system.domain.admin.user.UserEhrOrgView::getControlEhrCd)
                        .filter(StringUtils::hasText)
                        .collect(Collectors.toSet());
                log.info("用户 {} 有权限的控制层级1 EHR组织编码数量: {}", userName, allowedEhrCds.size());
            }
        }
        
        // 如果EHR组织权限为空且不是管理员，返回空结果（用户没有权限）
        if (!isAdmin && allowedEhrCds.isEmpty() && StringUtils.hasText(userName)) {
            log.warn("用户 {} 没有任何EHR组织权限，返回空结果", userName);
            return Collections.emptyList();
        }
        
        // 将权限集合设置到查询参数中
        if (!allowedEhrCds.isEmpty()) {
            params.setEhrCdList(new ArrayList<>(allowedEhrCds));
        }
        
        // 如果year为空，默认使用当前年份
        if (params.getYear() == null || params.getYear().isEmpty()) {
            Calendar calendar = Calendar.getInstance();
            String currentYear = String.valueOf(calendar.get(Calendar.YEAR));
            params.setYear(currentYear);
            log.info("year参数为空，使用当前年份：{}", currentYear);
        }
        
        log.info("全量查询参数：year={}, budgetType={}", params.getYear(), params.getBudgetType());
        
        // 查询全量数据（不分页）
        List<BudgetQuarterlyAggregateByType> records = budgetQuarterlyAggregateByTypeMapper.selectListByParams(params);
        log.info("查询全量数据结果：records size={}", records.size());
        
        // 转换为 BudgetBalanceVo（根据 budgetType 选择对应的字段）
        List<BudgetBalanceVo> voList = convertToBudgetBalanceVoListByType(records, params.getBudgetType());
        
        log.info("返回全量数据结果：list size={}", voList.size());
        
        return voList;
    }

    @Override
    public PageResult<BudgetBalanceVo> queryProjectBudgetBalance(BudgetProjectBalanceQueryParams params) {
        log.info("开始查询项目可用预算，params={}", params);

        // 步骤一：获取登录用户名
        String userName = getLoginUserName();
        if (StringUtils.hasText(userName)) {
            log.info("当前登录用户名: {}", userName);
        } else {
            log.warn("无法获取登录用户名，将跳过权限过滤");
        }

        // 判断是否是管理员，如果是管理员则跳过权限过滤
        boolean isAdmin = false;
        if (StringUtils.hasText(userName)) {
            isAdmin = isAdminUser(userName);
            if (isAdmin) {
                log.info("用户 {} 是管理员，跳过权限过滤", userName);
            }
        }

        // 步骤二：获取用户权限数据（项目编码）
        // 注意：项目预算查询只需要项目权限
        // 如果是管理员，则跳过权限查询
        Set<String> allowedProjectCodes = new HashSet<>();
        
        if (StringUtils.hasText(userName) && !isAdmin) {
            // 通过用户名从V_USER_PROJECT视图查询，获取projectCode集合
            List<com.jasolar.mis.module.system.domain.admin.user.UserProjectView> projectViews = 
                    userProjectViewMapper.selectByUserName(userName);
            if (!CollectionUtils.isEmpty(projectViews)) {
                allowedProjectCodes = projectViews.stream()
                        .map(com.jasolar.mis.module.system.domain.admin.user.UserProjectView::getProjectCode)
                        .filter(StringUtils::hasText)
                        .collect(Collectors.toSet());
                log.info("用户 {} 有权限的项目编码数量: {}", userName, allowedProjectCodes.size());
            }
        }
        
        // 如果项目权限为空且不是管理员，返回空结果（用户没有权限）
        if (!isAdmin && allowedProjectCodes.isEmpty() && StringUtils.hasText(userName)) {
            log.warn("用户 {} 没有任何项目权限，返回空结果", userName);
            PageResult<BudgetBalanceVo> pageResult = new PageResult<>();
            pageResult.setList(Collections.emptyList());
            pageResult.setTotal(0L);
            return pageResult;
        }
        
        // 将权限集合设置到查询参数中
        if (!allowedProjectCodes.isEmpty()) {
            params.setPrjCdList(new ArrayList<>(allowedProjectCodes));
        }

        // 确保分页参数不为空
        if (params.getPageNo() == null || params.getPageNo() <= 0) {
            params.setPageNo(1);
        }
        if (params.getPageSize() == null || params.getPageSize() <= 0) {
            params.setPageSize(20);
        }

        // 如果year为空，默认使用当前年份
        if (params.getYear() == null || params.getYear().isEmpty()) {
            Calendar calendar = Calendar.getInstance();
            String currentYear = String.valueOf(calendar.get(Calendar.YEAR));
            params.setYear(currentYear);
            log.info("year参数为空，使用当前年份：{}", currentYear);
        }

        log.info("分页参数：pageNo={}, pageSize={}, year={}, budgetType={}", 
                params.getPageNo(), params.getPageSize(), params.getYear(), params.getBudgetType());

        // 先查询总数
        Long total = budgetProjectQuarterlyAggregateMapper.selectCountByParams(params);
        log.info("查询总数：total={}", total);

        // 计算分页的起始位置和结束位置（Oracle ROW_NUMBER从1开始）
        int startRow = (params.getPageNo() - 1) * params.getPageSize() + 1;
        int endRow = params.getPageNo() * params.getPageSize();
        log.info("分页范围：startRow={}, endRow={}", startRow, endRow);

        // 查询视图数据（使用ROW_NUMBER窗口函数分页，直接返回List，避免MyBatis-Plus分页插件拦截）
        List<BudgetProjectQuarterlyAggregate> records = budgetProjectQuarterlyAggregateMapper.selectPageByParams(startRow, endRow, params);

        log.info("查询结果：records size={}, 预期size={}", records.size(), params.getPageSize());

        // 如果返回的记录数超过pageSize，说明分页没有生效，需要手动截取（临时保护措施）
        if (records.size() > params.getPageSize()) {
            log.error("分页未生效！返回了{}条记录，预期{}条，手动截取前{}条", records.size(), params.getPageSize(), params.getPageSize());
            records = records.subList(0, params.getPageSize());
        }

        // 转换为 BudgetBalanceVo，根据 budgetType 选择字段
        List<BudgetBalanceVo> voList = convertToBudgetBalanceVoListByProject(records, params.getBudgetType());

        // 构建返回结果
        PageResult<BudgetBalanceVo> pageResult = new PageResult<>();
        pageResult.setList(voList);
        pageResult.setTotal(total); // 使用查询的总数

        log.info("返回分页结果：total={}, list size={}", pageResult.getTotal(), pageResult.getList().size());

        return pageResult;
    }

    /**
     * 将 BudgetProjectQuarterlyAggregate 列表转换为 BudgetBalanceVo 列表
     * 根据 budgetType 选择对应的字段：
     * - TOTALINVESTMENT：设置 total 字段（投资额，Q1-Q4聚合）
     * - PAYMENT：设置 q1, q2, q3, q4 字段（付款额，每个季度分开展示）
     */
    private List<BudgetBalanceVo> convertToBudgetBalanceVoListByProject(
            List<BudgetProjectQuarterlyAggregate> aggregateList, 
            String budgetType) {
        List<BudgetBalanceVo> voList = new ArrayList<>();

        boolean isTotalInvestment = "TOTALINVESTMENT".equalsIgnoreCase(budgetType);
        boolean isPayment = "PAYMENT".equalsIgnoreCase(budgetType);

        for (BudgetProjectQuarterlyAggregate aggregate : aggregateList) {
            BudgetBalanceVo vo = new BudgetBalanceVo();

            // 设置基本信息
            vo.setEhrCode(aggregate.getMorgCode());
            vo.setEhrName(aggregate.getMorgName());
            vo.setProjectCode(aggregate.getPrjCd());
            vo.setProjectName(aggregate.getPrjName());

            // 根据 budgetType 设置以前年度已使用金额（去年冻结+占用+发生，无则展示0）
            java.math.BigDecimal priorYearUsed = isTotalInvestment
                    ? aggregate.getInvestmentLastYearUsed()
                    : aggregate.getPaymentLastYearUsed();
            vo.setLastYearUsedBudget(priorYearUsed != null ? priorYearUsed : java.math.BigDecimal.ZERO);

            // 根据 budgetType 选择对应的字段
            if (isTotalInvestment) {
                // 投资额：设置 total 字段（Q1-Q4聚合）
                vo.setTotal(createBudgetAmountVo(
                    aggregate.getInvestmentAmountTotal(),
                    aggregate.getInvestmentAmountAdj(),
                    aggregate.getInvestmentTotalBudget(),
                    aggregate.getInvestmentAmountFrozen(),
                    aggregate.getInvestmentAmountOccupied(),
                    aggregate.getInvestmentAmountActual(),
                    aggregate.getInvestmentAmountActualApproved(),
                    aggregate.getInvestmentAmountAvailable()
                ));
            } else if (isPayment) {
                // 付款额：设置 q1, q2, q3, q4 字段（每个季度分开展示）
                vo.setQ1(createBudgetAmountVo(
                    aggregate.getPaymentQ1AmountTotal(),
                    aggregate.getPaymentQ1AmountAdj(),
                    aggregate.getPaymentQ1TotalBudget(),
                    aggregate.getPaymentQ1AmountFrozen(),
                    aggregate.getPaymentQ1AmountOccupied(),
                    aggregate.getPaymentQ1AmountActual(),
                    aggregate.getPaymentQ1AmountActualApproved(),
                    aggregate.getPaymentQ1AmountAvailable()
                ));

                vo.setQ2(createBudgetAmountVo(
                    aggregate.getPaymentQ2AmountTotal(),
                    aggregate.getPaymentQ2AmountAdj(),
                    aggregate.getPaymentQ2TotalBudget(),
                    aggregate.getPaymentQ2AmountFrozen(),
                    aggregate.getPaymentQ2AmountOccupied(),
                    aggregate.getPaymentQ2AmountActual(),
                    aggregate.getPaymentQ2AmountActualApproved(),
                    aggregate.getPaymentQ2AmountAvailable()
                ));

                vo.setQ3(createBudgetAmountVo(
                    aggregate.getPaymentQ3AmountTotal(),
                    aggregate.getPaymentQ3AmountAdj(),
                    aggregate.getPaymentQ3TotalBudget(),
                    aggregate.getPaymentQ3AmountFrozen(),
                    aggregate.getPaymentQ3AmountOccupied(),
                    aggregate.getPaymentQ3AmountActual(),
                    aggregate.getPaymentQ3AmountActualApproved(),
                    aggregate.getPaymentQ3AmountAvailable()
                ));

                vo.setQ4(createBudgetAmountVo(
                    aggregate.getPaymentQ4AmountTotal(),
                    aggregate.getPaymentQ4AmountAdj(),
                    aggregate.getPaymentQ4TotalBudget(),
                    aggregate.getPaymentQ4AmountFrozen(),
                    aggregate.getPaymentQ4AmountOccupied(),
                    aggregate.getPaymentQ4AmountActual(),
                    aggregate.getPaymentQ4AmountActualApproved(),
                    aggregate.getPaymentQ4AmountAvailable()
                ));
            } else {
                log.warn("未知的预算类型：{}，默认使用投资额字段", budgetType);
                // 默认使用投资额字段
                vo.setTotal(createBudgetAmountVo(
                    aggregate.getInvestmentAmountTotal(),
                    aggregate.getInvestmentAmountAdj(),
                    aggregate.getInvestmentTotalBudget(),
                    aggregate.getInvestmentAmountFrozen(),
                    aggregate.getInvestmentAmountOccupied(),
                    aggregate.getInvestmentAmountActual(),
                    aggregate.getInvestmentAmountActualApproved(),
                    aggregate.getInvestmentAmountAvailable()
                ));
            }

            voList.add(vo);
        }

        return voList;
    }

    @Override
    public PageResult<BudgetClaimStateVo> queryPaymentStatus(PaymentStatusQueryParams params) {
        log.info("开始查询付款情况，params={}", params);
        
        // 步骤一：获取登录用户名
        String userName = getLoginUserName();
        if (StringUtils.hasText(userName)) {
            log.info("当前登录用户名: {}", userName);
        } else {
            log.warn("无法获取登录用户名，将跳过权限过滤");
        }

        // 判断是否是管理员，如果是管理员则跳过权限过滤
        boolean isAdmin = false;
        if (StringUtils.hasText(userName)) {
            isAdmin = isAdminUser(userName);
            if (isAdmin) {
                log.info("用户 {} 是管理员，跳过权限过滤", userName);
            }
        }

        // 步骤二和步骤三：获取用户权限数据（EHR组织编码和项目编码）
        // 如果是管理员，则跳过权限查询
        Set<String> allowedEhrCds = new HashSet<>();
        Set<String> allowedProjectCodes = new HashSet<>();
        
        if (StringUtils.hasText(userName) && !isAdmin) {
            // 步骤二：通过用户名从V_USER_EHR_ORG视图查询，获取ehrCd集合
            List<com.jasolar.mis.module.system.domain.admin.user.UserEhrOrgView> ehrOrgViews = 
                    userEhrOrgViewMapper.selectByUserName(userName);
            if (!CollectionUtils.isEmpty(ehrOrgViews)) {
                allowedEhrCds = ehrOrgViews.stream()
                        .map(com.jasolar.mis.module.system.domain.admin.user.UserEhrOrgView::getEhrCd)
                        .filter(StringUtils::hasText)
                        .collect(Collectors.toSet());
                log.info("用户 {} 有权限的EHR组织编码数量: {}", userName, allowedEhrCds.size());
            }
            
            // 步骤三：通过用户名从V_USER_PROJECT视图查询，获取projectCode集合
            List<com.jasolar.mis.module.system.domain.admin.user.UserProjectView> projectViews = 
                    userProjectViewMapper.selectByUserName(userName);
            if (!CollectionUtils.isEmpty(projectViews)) {
                allowedProjectCodes = projectViews.stream()
                        .map(com.jasolar.mis.module.system.domain.admin.user.UserProjectView::getProjectCode)
                        .filter(StringUtils::hasText)
                        .collect(Collectors.toSet());
                log.info("用户 {} 有权限的项目编码数量: {}", userName, allowedProjectCodes.size());
            }
        }
        
        // 如果两个set都为空且不是管理员，返回空结果（用户没有权限）
        if (!isAdmin && allowedEhrCds.isEmpty() && allowedProjectCodes.isEmpty() && StringUtils.hasText(userName)) {
            log.warn("用户 {} 没有任何权限（EHR组织和项目权限都为空），返回空结果", userName);
            PageResult<BudgetClaimStateVo> pageResult = new PageResult<>();
            pageResult.setList(Collections.emptyList());
            pageResult.setTotal(0L);
            return pageResult;
        }
        
        // 将权限集合拆分成多个批次（每个批次最多1000个，避免Oracle IN子句超过1000个限制）
        List<List<String>> allowedEhrCdsBatches = splitIntoBatches(allowedEhrCds, 1000);
        List<List<String>> allowedProjectCodesBatches = splitIntoBatches(allowedProjectCodes, 1000);
        
        log.info("EHR组织编码拆分成 {} 个批次，项目编码拆分成 {} 个批次", 
                allowedEhrCdsBatches.size(), allowedProjectCodesBatches.size());
        
        // 将权限批次集合设置到查询参数中
        if (!allowedEhrCdsBatches.isEmpty()) {
            params.setEhrCdListBatches(allowedEhrCdsBatches);
        }
        if (!allowedProjectCodesBatches.isEmpty()) {
            params.setPrjCdListBatches(allowedProjectCodesBatches);
        }
        
        // 确保分页参数不为空
        if (params.getPageNo() == null || params.getPageNo() <= 0) {
            params.setPageNo(1);
        }
        if (params.getPageSize() == null || params.getPageSize() <= 0) {
            params.setPageSize(20);
        }
        
        log.info("分页参数：pageNo={}, pageSize={}", params.getPageNo(), params.getPageSize());
        
        // 先查询总数
        Long total = budgetClaimMonthlyAggregateMapper.selectCountByParams(params);
        log.info("查询总数：total={}", total);
        
        // 计算分页的起始位置和结束位置（Oracle ROW_NUMBER从1开始）
        int startRow = (params.getPageNo() - 1) * params.getPageSize() + 1;
        int endRow = params.getPageNo() * params.getPageSize();
        log.info("分页范围：startRow={}, endRow={}", startRow, endRow);
        
        // 查询视图数据（使用ROW_NUMBER窗口函数分页，直接返回List，避免MyBatis-Plus分页插件拦截）
        List<BudgetClaimMonthlyAggregate> records = 
                budgetClaimMonthlyAggregateMapper.selectPageByParams(startRow, endRow, params);
        
        log.info("查询结果：records size={}, 预期size={}", records.size(), params.getPageSize());
        
        // 如果返回的记录数超过pageSize，说明分页没有生效，需要手动截取（临时保护措施）
        if (records.size() > params.getPageSize()) {
            log.error("分页未生效！返回了{}条记录，预期{}条，手动截取前{}条", records.size(), params.getPageSize(), params.getPageSize());
            records = records.subList(0, params.getPageSize());
        }
        
        // 转换为 BudgetClaimStateVo
        List<BudgetClaimStateVo> voList = convertToBudgetClaimStateVoList(records);
        
        // 构建分页结果
        PageResult<BudgetClaimStateVo> result = new PageResult<>();
        result.setList(voList);
        result.setTotal(total);
        
        log.info("付款情况查询完成，返回{}条记录，总数{}", voList.size(), total);
        return result;
    }

    /**
     * 将 BudgetClaimMonthlyAggregate 列表转换为 BudgetClaimStateVo 列表
     */
    private List<BudgetClaimStateVo> convertToBudgetClaimStateVoList(
            List<BudgetClaimMonthlyAggregate> aggregateList) {
        List<BudgetClaimStateVo> voList = new ArrayList<>();
        
        // 批量查询字典信息
        List<String> dictCodes = List.of("internal");
        DictInfoByCodeVo dictInfoByCodeVo = DictInfoByCodeVo.builder()
                .codes(dictCodes)
                .build();
        Map<String, DictEditVo> dictMap = systemDictService.getByCode(dictInfoByCodeVo);
        
        for (BudgetClaimMonthlyAggregate aggregate : aggregateList) {
            BudgetClaimStateVo vo = new BudgetClaimStateVo();
            
            // 设置基本信息
            vo.setYear(aggregate.getYear());
            vo.setEhrCode(aggregate.getMorgCode());
            vo.setEhrName(aggregate.getEhrName());
            // 将NAN-NAN或NAN转换为null（NAN在数据库中是空值的占位符）
            String budgetSubjectCode = aggregate.getBudgetSubjectCode();
            if (budgetSubjectCode == null || "NAN-NAN".equals(budgetSubjectCode) || "NAN".equals(budgetSubjectCode)) {
                vo.setErpAcctCd(null);
                vo.setErpAcctNm(null);
            } else {
                vo.setErpAcctCd(budgetSubjectCode);
                vo.setErpAcctNm(aggregate.getErpAcctNm());
            }
            // 将NAN转换为null（NAN在数据库中是空值的占位符）
            String erpAssetType = aggregate.getErpAssetType();
            vo.setErpAssetType("NAN".equals(erpAssetType) ? null : erpAssetType);
            String masterProjectCode = aggregate.getMasterProjectCode();
            vo.setProjectCode("NAN".equals(masterProjectCode) ? null : masterProjectCode);
            // 项目名称通过视图的LEFT JOIN PROJECT_CONTROL_R获取（PRJ_CD = MASTER_PROJECT_CODE，PRJ_NM作为PROJECT_NAME）
            // 当项目编码为NAN时，项目名称也应该为null（因为JOIN无法匹配）
            vo.setProjectName("NAN".equals(masterProjectCode) ? null : aggregate.getProjectName());
            // 设置是否内部项目
            vo.setIsInternal(aggregate.getIsInternal());
            
            // 设置是否内部项目描述
            if (StringUtils.hasText(aggregate.getIsInternal())) {
                String isInternalDes = systemDictService.getFieldLabel(dictMap, "internal", aggregate.getIsInternal());
                vo.setIsInternalDes(isInternalDes);
            }
            
            // 设置付款额（12个月）
            PaymentAmountVo paymentAmount = new PaymentAmountVo();
            BigDecimal amount01 = aggregate.getM1AmountAvailable();
            BigDecimal amount02 = aggregate.getM2AmountAvailable();
            BigDecimal amount03 = aggregate.getM3AmountAvailable();
            BigDecimal amount04 = aggregate.getM4AmountAvailable();
            BigDecimal amount05 = aggregate.getM5AmountAvailable();
            BigDecimal amount06 = aggregate.getM6AmountAvailable();
            BigDecimal amount07 = aggregate.getM7AmountAvailable();
            BigDecimal amount08 = aggregate.getM8AmountAvailable();
            BigDecimal amount09 = aggregate.getM9AmountAvailable();
            BigDecimal amount10 = aggregate.getM10AmountAvailable();
            BigDecimal amount11 = aggregate.getM11AmountAvailable();
            BigDecimal amount12 = aggregate.getM12AmountAvailable();
            
            paymentAmount.setAmount01(amount01);
            paymentAmount.setAmount02(amount02);
            paymentAmount.setAmount03(amount03);
            paymentAmount.setAmount04(amount04);
            paymentAmount.setAmount05(amount05);
            paymentAmount.setAmount06(amount06);
            paymentAmount.setAmount07(amount07);
            paymentAmount.setAmount08(amount08);
            paymentAmount.setAmount09(amount09);
            paymentAmount.setAmount10(amount10);
            paymentAmount.setAmount11(amount11);
            paymentAmount.setAmount12(amount12);
            
            // 计算全年合计（将12个月金额相加）
            BigDecimal total = BigDecimal.ZERO;
            if (amount01 != null) total = total.add(amount01);
            if (amount02 != null) total = total.add(amount02);
            if (amount03 != null) total = total.add(amount03);
            if (amount04 != null) total = total.add(amount04);
            if (amount05 != null) total = total.add(amount05);
            if (amount06 != null) total = total.add(amount06);
            if (amount07 != null) total = total.add(amount07);
            if (amount08 != null) total = total.add(amount08);
            if (amount09 != null) total = total.add(amount09);
            if (amount10 != null) total = total.add(amount10);
            if (amount11 != null) total = total.add(amount11);
            if (amount12 != null) total = total.add(amount12);
            paymentAmount.setTotalAmount(total);
            vo.setPaymentAmount(paymentAmount);
            
            voList.add(vo);
        }
        
        return voList;
    }

    @Override
    public List<BudgetClaimStateVo> queryPaymentStatusAll(PaymentStatusQueryParams params) {
        log.info("开始全量查询付款情况，params={}", params);
        
        // 步骤一：获取登录用户名
        String userName = getLoginUserName();
        if (StringUtils.hasText(userName)) {
            log.info("当前登录用户名: {}", userName);
        } else {
            log.warn("无法获取登录用户名，将跳过权限过滤");
        }

        // 判断是否是管理员，如果是管理员则跳过权限过滤
        boolean isAdmin = false;
        if (StringUtils.hasText(userName)) {
            isAdmin = isAdminUser(userName);
            if (isAdmin) {
                log.info("用户 {} 是管理员，跳过权限过滤", userName);
            }
        }

        // 步骤二和步骤三：获取用户权限数据（EHR组织编码和项目编码）
        // 如果是管理员，则跳过权限查询
        Set<String> allowedEhrCds = new HashSet<>();
        Set<String> allowedProjectCodes = new HashSet<>();
        
        if (StringUtils.hasText(userName) && !isAdmin) {
            // 步骤二：通过用户名从V_USER_EHR_ORG视图查询，获取ehrCd集合
            List<com.jasolar.mis.module.system.domain.admin.user.UserEhrOrgView> ehrOrgViews = 
                    userEhrOrgViewMapper.selectByUserName(userName);
            if (!CollectionUtils.isEmpty(ehrOrgViews)) {
                allowedEhrCds = ehrOrgViews.stream()
                        .map(com.jasolar.mis.module.system.domain.admin.user.UserEhrOrgView::getEhrCd)
                        .filter(StringUtils::hasText)
                        .collect(Collectors.toSet());
                log.info("用户 {} 有权限的EHR组织编码数量: {}", userName, allowedEhrCds.size());
            }
            
            // 步骤三：通过用户名从V_USER_PROJECT视图查询，获取projectCode集合
            List<com.jasolar.mis.module.system.domain.admin.user.UserProjectView> projectViews = 
                    userProjectViewMapper.selectByUserName(userName);
            if (!CollectionUtils.isEmpty(projectViews)) {
                allowedProjectCodes = projectViews.stream()
                        .map(com.jasolar.mis.module.system.domain.admin.user.UserProjectView::getProjectCode)
                        .filter(StringUtils::hasText)
                        .collect(Collectors.toSet());
                log.info("用户 {} 有权限的项目编码数量: {}", userName, allowedProjectCodes.size());
            }
        }
        
        // 如果两个set都为空且不是管理员，返回空结果（用户没有权限）
        if (!isAdmin && allowedEhrCds.isEmpty() && allowedProjectCodes.isEmpty() && StringUtils.hasText(userName)) {
            log.warn("用户 {} 没有任何权限（EHR组织和项目权限都为空），返回空结果", userName);
            return Collections.emptyList();
        }
        
        // 将权限集合拆分成多个批次（每个批次最多1000个，避免Oracle IN子句超过1000个限制）
        List<List<String>> allowedEhrCdsBatches = splitIntoBatches(allowedEhrCds, 1000);
        List<List<String>> allowedProjectCodesBatches = splitIntoBatches(allowedProjectCodes, 1000);
        
        log.info("EHR组织编码拆分成 {} 个批次，项目编码拆分成 {} 个批次", 
                allowedEhrCdsBatches.size(), allowedProjectCodesBatches.size());
        
        // 将权限批次集合设置到查询参数中
        if (!allowedEhrCdsBatches.isEmpty()) {
            params.setEhrCdListBatches(allowedEhrCdsBatches);
        }
        if (!allowedProjectCodesBatches.isEmpty()) {
            params.setPrjCdListBatches(allowedProjectCodesBatches);
        }
        
        // 查询全量数据（不分页）
        List<BudgetClaimMonthlyAggregate> records = 
                budgetClaimMonthlyAggregateMapper.selectAllByParams(params);
        
        log.info("查询全量数据结果：records size={}", records.size());
        
        // 转换为 BudgetClaimStateVo
        List<BudgetClaimStateVo> voList = convertToBudgetClaimStateVoList(records);
        
        log.info("付款情况全量查询完成，返回{}条记录", voList.size());
        return voList;
    }

    /**
     * 获取登录用户名
     * 通过 LoginUser 的 id 查询 SystemUserDo 获取 userName
     * 
     * @return 登录用户名，如果无法获取则返回null
     */
    private String getLoginUserName() {
        try {
            LoginUser loginUser = LoginServletUtils.getLoginUser();
            if (loginUser != null && loginUser.isAuthorized() && loginUser.getId() != null) {
                // 通过 LoginUser 的 id 查询 SystemUserDo 获取 userName
                com.jasolar.mis.module.system.domain.admin.user.SystemUserDo systemUser = 
                        systemUserMapper.selectById(loginUser.getId());
                if (systemUser != null && StringUtils.hasText(systemUser.getUserName())) {
                    return systemUser.getUserName();
                }
            }
        } catch (Exception e) {
            log.warn("获取登录用户信息失败", e);
        }
        return null;
    }

    /**
     * 判断用户是否是管理员
     * 判断逻辑：
     * 1. 通过 userName 查询 SYSTEM_USER 表获取 ID
     * 2. 根据这个 ID 去 SYSTEM_USER_GROUP_R 表查询，条件是 type='1'
     * 3. 获取到的 USER_GROUP_ID 作为 SYSTEM_USER_GROUP 表的 ID 去查询
     * 4. 如果 NAME 是"管理员用户组"或"集团用户"，那么这个用户就是管理员
     * 
     * @param userName 用户名
     * @return 是否是管理员
     */
    private boolean isAdminUser(String userName) {
        try {
            if (!StringUtils.hasText(userName)) {
                return false;
            }

            // 1. 通过 userName 查询 SYSTEM_USER 表获取 ID
            com.jasolar.mis.module.system.domain.admin.user.SystemUserDo systemUser = 
                    systemUserMapper.selectOne(new LambdaQueryWrapper<com.jasolar.mis.module.system.domain.admin.user.SystemUserDo>()
                            .eq(com.jasolar.mis.module.system.domain.admin.user.SystemUserDo::getUserName, userName)
                            .eq(com.jasolar.mis.module.system.domain.admin.user.SystemUserDo::getDeleted, 0));
            
            if (systemUser == null || systemUser.getId() == null) {
                log.warn("用户 {} 不存在", userName);
                return false;
            }

            Long userId = systemUser.getId();

            // 2. 根据这个 ID 去 SYSTEM_USER_GROUP_R 表查询，条件是 type='1'
            List<SystemUserGroupRDo> userGroupRList = systemUserGroupRMapper.selectList(
                    new LambdaQueryWrapper<SystemUserGroupRDo>()
                            .eq(SystemUserGroupRDo::getUserId, userId)
                            .eq(SystemUserGroupRDo::getType, "1")
                            .eq(SystemUserGroupRDo::getDeleted, 0));

            if (CollectionUtils.isEmpty(userGroupRList)) {
                log.debug("用户 {} 没有 type=1 的用户组", userName);
                return false;
            }

            // 3. 获取到的 USER_GROUP_ID 作为 SYSTEM_USER_GROUP 表的 ID 去查询
            // 4. 如果 NAME 是"管理员用户组"或"集团用户"，那么这个用户就是管理员
            List<Long> groupIds = userGroupRList.stream()
                    .map(SystemUserGroupRDo::getGroupId)
                    .filter(id -> id != null)
                    .collect(Collectors.toList());

            if (CollectionUtils.isEmpty(groupIds)) {
                return false;
            }

            // 查询用户组，检查是否有"管理员用户组"或"集团用户"
            List<SystemUserGroupDo> userGroups = userGroupMapper.selectList(
                    new LambdaQueryWrapper<SystemUserGroupDo>()
                            .in(SystemUserGroupDo::getId, groupIds)
                            .eq(SystemUserGroupDo::getDeleted, 0));

            if (!CollectionUtils.isEmpty(userGroups)) {
                boolean isAdmin = userGroups.stream()
                        .anyMatch(group -> {
                            String groupName = group.getName();
                            return "管理员用户组".equals(groupName) || "集团用户".equals(groupName);
                        });
                if (isAdmin) {
                    log.info("用户 {} 是管理员用户组或集团用户组成员", userName);
                }
                return isAdmin;
            }

            return false;
        } catch (Exception e) {
            log.warn("判断用户是否是管理员失败，userName={}", userName, e);
            return false;
        }
    }

    /**
     * 将集合拆分成多个批次，每个批次最多包含batchSize个元素
     * 用于解决Oracle IN子句最多支持1000个表达式的限制
     *
     * @param collection 要拆分的集合
     * @param batchSize 每个批次的大小
     * @return 拆分后的批次列表
     */
    private List<List<String>> splitIntoBatches(Set<String> collection, int batchSize) {
        if (CollectionUtils.isEmpty(collection)) {
            return Collections.emptyList();
        }
        
        List<String> list = new ArrayList<>(collection);
        List<List<String>> batches = new ArrayList<>();
        
        for (int i = 0; i < list.size(); i += batchSize) {
            int end = Math.min(i + batchSize, list.size());
            batches.add(list.subList(i, end));
        }
        
        return batches;
    }

    @Override
    public List<BudgetBalanceVo> queryProjectBudgetBalanceAll(BudgetProjectBalanceQueryParams params) {
        log.info("开始查询项目可用预算（全量导出），params={}", params);

        // 步骤一：获取登录用户名
        String userName = getLoginUserName();
        if (StringUtils.hasText(userName)) {
            log.info("当前登录用户名: {}", userName);
        } else {
            log.warn("无法获取登录用户名，将跳过权限过滤");
        }

        // 判断是否是管理员，如果是管理员则跳过权限过滤
        boolean isAdmin = false;
        if (StringUtils.hasText(userName)) {
            isAdmin = isAdminUser(userName);
            if (isAdmin) {
                log.info("用户 {} 是管理员，跳过权限过滤", userName);
            }
        }

        // 步骤二：获取用户权限数据（项目编码）
        // 注意：项目预算查询只需要项目权限
        // 如果是管理员，则跳过权限查询
        Set<String> allowedProjectCodes = new HashSet<>();
        
        if (StringUtils.hasText(userName) && !isAdmin) {
            // 通过用户名从V_USER_PROJECT视图查询，获取projectCode集合
            List<com.jasolar.mis.module.system.domain.admin.user.UserProjectView> projectViews = 
                    userProjectViewMapper.selectByUserName(userName);
            if (!CollectionUtils.isEmpty(projectViews)) {
                allowedProjectCodes = projectViews.stream()
                        .map(com.jasolar.mis.module.system.domain.admin.user.UserProjectView::getProjectCode)
                        .filter(StringUtils::hasText)
                        .collect(Collectors.toSet());
                log.info("用户 {} 有权限的项目编码数量: {}", userName, allowedProjectCodes.size());
            }
        }
        
        // 如果项目权限为空且不是管理员，返回空结果（用户没有权限）
        if (!isAdmin && allowedProjectCodes.isEmpty() && StringUtils.hasText(userName)) {
            log.warn("用户 {} 没有任何项目权限，返回空结果", userName);
            return Collections.emptyList();
        }
        
        // 将权限集合设置到查询参数中
        if (!allowedProjectCodes.isEmpty()) {
            params.setPrjCdList(new ArrayList<>(allowedProjectCodes));
        }

        // 如果year为空，默认使用当前年份
        if (params.getYear() == null || params.getYear().isEmpty()) {
            Calendar calendar = Calendar.getInstance();
            String currentYear = String.valueOf(calendar.get(Calendar.YEAR));
            params.setYear(currentYear);
            log.info("year参数为空，使用当前年份：{}", currentYear);
        }

        log.info("全量查询参数：year={}, budgetType={}", params.getYear(), params.getBudgetType());

        // 查询全量数据（不分页）
        List<BudgetProjectQuarterlyAggregate> records = budgetProjectQuarterlyAggregateMapper.selectListByParams(params);
        log.info("查询全量数据结果：records size={}", records.size());

        // 转换为 BudgetBalanceVo，根据 budgetType 选择字段
        List<BudgetBalanceVo> voList = convertToBudgetBalanceVoListByProject(records, params.getBudgetType());

        log.info("返回全量数据结果：list size={}", voList.size());

        return voList;
    }

    @Override
    public List<BudgetBalanceVo> queryBudgetQuarterlyDetail(
            com.jasolar.mis.module.system.controller.budget.vo.BudgetBalanceDetailQueryParams params) {
        log.info("开始查询预算季度明细，params={}", params);

        // 步骤一：获取登录用户名
        String userName = getLoginUserName();
        if (StringUtils.hasText(userName)) {
            log.info("当前登录用户名: {}", userName);
        } else {
            log.warn("无法获取登录用户名，将跳过权限过滤");
        }

        // 判断是否是管理员，如果是管理员则跳过权限过滤
        boolean isAdmin = false;
        if (StringUtils.hasText(userName)) {
            isAdmin = isAdminUser(userName);
            if (isAdmin) {
                log.info("用户 {} 是管理员，跳过权限过滤", userName);
            }
        }

        // 步骤二：根据用户类型获取组织编码列表
        List<String> morgCodeList = new ArrayList<>();

        if (isAdmin) {
            // 管理员或集团用户：查询 V_EHR_CONTROL_LEVEL 视图，根据 CONTROL_EHR_CD 查询所有 BUDGET_ORG_CD
            log.info("管理员用户，查询 V_EHR_CONTROL_LEVEL 视图，controlEhrCd={}", params.getControlEhrCd());
            List<com.jasolar.mis.module.system.domain.ehr.EhrControlLevelView> ehrControlLevelViews = 
                    ehrControlLevelViewMapper.selectByControlEhrCd(params.getControlEhrCd());
            if (!CollectionUtils.isEmpty(ehrControlLevelViews)) {
                morgCodeList = ehrControlLevelViews.stream()
                        .map(com.jasolar.mis.module.system.domain.ehr.EhrControlLevelView::getBudgetOrgCd)
                        .filter(StringUtils::hasText)
                        .distinct()
                        .collect(Collectors.toList());
                log.info("管理员用户，从 V_EHR_CONTROL_LEVEL 查询到 BUDGET_ORG_CD 数量: {}", morgCodeList.size());
            }
        } else {
            // 正常用户：查询 V_USER_EHR_ORG 视图，根据 CONTROL_EHR_CD 和 USER_NAME 查询所有 MORG_CODE
            if (StringUtils.hasText(userName)) {
                log.info("正常用户，查询 V_USER_EHR_ORG 视图，userName={}, controlEhrCd={}", userName, params.getControlEhrCd());
                // 使用 LambdaQueryWrapper 同时查询 userName 和 controlEhrCd
                LambdaQueryWrapper<com.jasolar.mis.module.system.domain.admin.user.UserEhrOrgView> wrapper = 
                        new LambdaQueryWrapper<com.jasolar.mis.module.system.domain.admin.user.UserEhrOrgView>()
                                .eq(com.jasolar.mis.module.system.domain.admin.user.UserEhrOrgView::getUserName, userName)
                                .eq(com.jasolar.mis.module.system.domain.admin.user.UserEhrOrgView::getControlEhrCd, params.getControlEhrCd());
                List<com.jasolar.mis.module.system.domain.admin.user.UserEhrOrgView> userEhrOrgViews = 
                        userEhrOrgViewMapper.selectList(wrapper);
                if (!CollectionUtils.isEmpty(userEhrOrgViews)) {
                    morgCodeList = userEhrOrgViews.stream()
                            .map(com.jasolar.mis.module.system.domain.admin.user.UserEhrOrgView::getMorgCode)
                            .filter(StringUtils::hasText)
                            .distinct()
                            .collect(Collectors.toList());
                    log.info("正常用户，从 V_USER_EHR_ORG 查询到 MORG_CODE 数量: {}", morgCodeList.size());
                }
            }
        }

        // 如果组织编码列表为空，返回空结果
        if (CollectionUtils.isEmpty(morgCodeList)) {
            log.warn("组织编码列表为空，返回空结果");
            return Collections.emptyList();
        }

        // 步骤三：根据年度和组织编码列表查询 V_BUDGET_QUARTERLY_DETAIL 视图
        log.info("查询 V_BUDGET_QUARTERLY_DETAIL 视图，year={}, morgCodeList size={}", params.getYear(), morgCodeList.size());
        List<com.jasolar.mis.module.system.domain.budget.BudgetQuarterlyDetail> result = 
                budgetQuarterlyDetailMapper.selectListByYearAndMorgCodes(params.getYear(), morgCodeList);
        log.info("查询结果数量: {}", result.size());

        // 转换为 BudgetBalanceVo
        List<BudgetBalanceVo> voList = convertBudgetQuarterlyDetailToBudgetBalanceVoList(result);
        log.info("返回数据结果：list size={}", voList.size());
        return voList;
    }

    /**
     * 将 BudgetQuarterlyDetail 列表转换为 BudgetBalanceVo 列表
     */
    private List<BudgetBalanceVo> convertBudgetQuarterlyDetailToBudgetBalanceVoList(
            List<com.jasolar.mis.module.system.domain.budget.BudgetQuarterlyDetail> detailList) {
        List<BudgetBalanceVo> voList = new ArrayList<>();
        for (com.jasolar.mis.module.system.domain.budget.BudgetQuarterlyDetail detail : detailList) {
            BudgetBalanceVo vo = new BudgetBalanceVo();
            vo.setEhrCode(detail.getMorgCode());
            vo.setEhrName(detail.getMorgName());
            vo.setSubjectCode(detail.getAccountSubjectCode());
            vo.setSubjectName(detail.getAccountSubjectName());
            vo.setControlCust1Cd(detail.getCust1Cd());
            vo.setControlCust1Name(detail.getCust1Name());

            vo.setQ1(createBudgetAmountVo(
                    detail.getQ1AmountTotal(),
                    detail.getQ1AmountAdj(),
                    detail.getQ1TotalBudget(),
                    detail.getQ1AmountFrozen(),
                    detail.getQ1AmountOccupied(),
                    detail.getQ1AmountActual(),
                    detail.getQ1AmountActualApproved(),
                    detail.getQ1AmountAvailable()
            ));
            vo.setQ2(createBudgetAmountVo(
                    detail.getQ2AmountTotal(),
                    detail.getQ2AmountAdj(),
                    detail.getQ2TotalBudget(),
                    detail.getQ2AmountFrozen(),
                    detail.getQ2AmountOccupied(),
                    detail.getQ2AmountActual(),
                    detail.getQ2AmountActualApproved(),
                    detail.getQ2AmountAvailable()
            ));
            vo.setQ3(createBudgetAmountVo(
                    detail.getQ3AmountTotal(),
                    detail.getQ3AmountAdj(),
                    detail.getQ3TotalBudget(),
                    detail.getQ3AmountFrozen(),
                    detail.getQ3AmountOccupied(),
                    detail.getQ3AmountActual(),
                    detail.getQ3AmountActualApproved(),
                    detail.getQ3AmountAvailable()
            ));
            vo.setQ4(createBudgetAmountVo(
                    detail.getQ4AmountTotal(),
                    detail.getQ4AmountAdj(),
                    detail.getQ4TotalBudget(),
                    detail.getQ4AmountFrozen(),
                    detail.getQ4AmountOccupied(),
                    detail.getQ4AmountActual(),
                    detail.getQ4AmountActualApproved(),
                    detail.getQ4AmountAvailable()
            ));
            // 季度明细视图中 Q4 为全年累计，total 取 Q4 值
            vo.setTotal(createBudgetAmountVo(
                    detail.getQ4AmountTotal(),
                    detail.getQ4AmountAdj(),
                    detail.getQ4TotalBudget(),
                    detail.getQ4AmountFrozen(),
                    detail.getQ4AmountOccupied(),
                    detail.getQ4AmountActual(),
                    detail.getQ4AmountActualApproved(),
                    detail.getQ4AmountAvailable()
            ));
            voList.add(vo);
        }
        return voList;
    }

    @Override
    public List<BudgetBalanceVo> queryBudgetQuarterlyAggregateByMorg(
            com.jasolar.mis.module.system.controller.budget.vo.BudgetQuarterlyAggregateByMorgQueryParams params) {
        log.info("开始查询预算季度聚合数据（按原始组织），params={}", params);

        // 步骤一：获取登录用户名
        String userName = getLoginUserName();
        if (StringUtils.hasText(userName)) {
            log.info("当前登录用户名: {}", userName);
        } else {
            log.warn("无法获取登录用户名，将跳过权限过滤");
        }

        // 判断是否是管理员，如果是管理员则跳过权限过滤
        boolean isAdmin = false;
        if (StringUtils.hasText(userName)) {
            isAdmin = isAdminUser(userName);
            if (isAdmin) {
                log.info("用户 {} 是管理员，跳过权限过滤", userName);
            }
        }

        // 步骤二：根据用户类型获取组织编码列表
        List<String> morgCodeList = new ArrayList<>();

        if (isAdmin) {
            // 管理员或集团用户：查询 V_EHR_CONTROL_LEVEL 视图，根据 CONTROL_EHR_CD 查询所有 BUDGET_ORG_CD
            log.info("管理员用户，查询 V_EHR_CONTROL_LEVEL 视图，controlEhrCd={}", params.getControlEhrCd());
            List<com.jasolar.mis.module.system.domain.ehr.EhrControlLevelView> ehrControlLevelViews = 
                    ehrControlLevelViewMapper.selectByControlEhrCd(params.getControlEhrCd());
            if (!CollectionUtils.isEmpty(ehrControlLevelViews)) {
                morgCodeList = ehrControlLevelViews.stream()
                        .map(com.jasolar.mis.module.system.domain.ehr.EhrControlLevelView::getBudgetOrgCd)
                        .filter(StringUtils::hasText)
                        .distinct()
                        .collect(Collectors.toList());
                log.info("管理员用户，从 V_EHR_CONTROL_LEVEL 查询到 BUDGET_ORG_CD 数量: {}", morgCodeList.size());
            }
        } else {
            // 正常用户：查询 V_USER_EHR_ORG 视图，根据 CONTROL_EHR_CD 和 USER_NAME 查询所有 MORG_CODE
            if (StringUtils.hasText(userName)) {
                log.info("正常用户，查询 V_USER_EHR_ORG 视图，userName={}, controlEhrCd={}", userName, params.getControlEhrCd());
                // 使用 LambdaQueryWrapper 同时查询 userName 和 controlEhrCd
                LambdaQueryWrapper<com.jasolar.mis.module.system.domain.admin.user.UserEhrOrgView> wrapper = 
                        new LambdaQueryWrapper<com.jasolar.mis.module.system.domain.admin.user.UserEhrOrgView>()
                                .eq(com.jasolar.mis.module.system.domain.admin.user.UserEhrOrgView::getUserName, userName)
                                .eq(com.jasolar.mis.module.system.domain.admin.user.UserEhrOrgView::getControlEhrCd, params.getControlEhrCd());
                List<com.jasolar.mis.module.system.domain.admin.user.UserEhrOrgView> userEhrOrgViews = 
                        userEhrOrgViewMapper.selectList(wrapper);
                if (!CollectionUtils.isEmpty(userEhrOrgViews)) {
                    morgCodeList = userEhrOrgViews.stream()
                            .map(com.jasolar.mis.module.system.domain.admin.user.UserEhrOrgView::getMorgCode)
                            .filter(StringUtils::hasText)
                            .distinct()
                            .collect(Collectors.toList());
                    log.info("正常用户，从 V_USER_EHR_ORG 查询到 MORG_CODE 数量: {}", morgCodeList.size());
                }
            }
        }

        // 如果组织编码列表为空，返回空结果
        if (CollectionUtils.isEmpty(morgCodeList)) {
            log.warn("组织编码列表为空，返回空结果");
            return Collections.emptyList();
        }

        // 步骤三：根据年度和组织编码列表查询 V_BUDGET_QUARTERLY_AGGREGATE_BY_MORG 视图
        log.info("查询 V_BUDGET_QUARTERLY_AGGREGATE_BY_MORG 视图，year={}, morgCodeList size={}", params.getYear(), morgCodeList.size());
        List<com.jasolar.mis.module.system.domain.budget.BudgetQuarterlyAggregateByMorg> records = 
                budgetQuarterlyAggregateByMorgMapper.selectListByYearAndMorgCodes(params.getYear(), morgCodeList);
        log.info("查询结果数量: {}", records.size());

        // 转换为 BudgetBalanceVo，根据 budgetType 选择对应的字段
        List<BudgetBalanceVo> voList = convertToBudgetBalanceVoListByMorg(records, params.getBudgetType());
        log.info("返回数据结果：list size={}", voList.size());

        return voList;
    }

    /**
     * 将 BudgetQuarterlyAggregateByMorg 列表转换为 BudgetBalanceVo 列表
     * 根据 budgetType 选择对应的字段（PURCHASE 或 PAYMENT）
     */
    private List<BudgetBalanceVo> convertToBudgetBalanceVoListByMorg(
            List<com.jasolar.mis.module.system.domain.budget.BudgetQuarterlyAggregateByMorg> aggregateList, 
            String budgetType) {
        List<BudgetBalanceVo> voList = new ArrayList<>();
        
        boolean isPurchase = "PURCHASE".equalsIgnoreCase(budgetType);
        boolean isPayment = "PAYMENT".equalsIgnoreCase(budgetType);
        
        for (com.jasolar.mis.module.system.domain.budget.BudgetQuarterlyAggregateByMorg aggregate : aggregateList) {
            BudgetBalanceVo vo = new BudgetBalanceVo();
            
            // 设置基本信息
            vo.setEhrCode(aggregate.getMorgCode());
            vo.setEhrName(aggregate.getMorgName());
            vo.setErpAssetType(aggregate.getErpAssetType());
            vo.setErpAssetTypeName(aggregate.getErpAssetTypeName());
            
            // 根据 budgetType 选择对应的字段
            if (isPurchase) {
                // 采购额：使用 PURCHASE_* 开头的字段
                vo.setQ1(createBudgetAmountVo(
                    aggregate.getPurchaseQ1AmountTotal(),
                    aggregate.getPurchaseQ1AmountAdj(),
                    aggregate.getPurchaseQ1TotalBudget(),
                    aggregate.getPurchaseQ1AmountFrozen(),
                    aggregate.getPurchaseQ1AmountOccupied(),
                    aggregate.getPurchaseQ1AmountActual(),
                    aggregate.getPurchaseQ1AmountActualApproved(),
                    aggregate.getPurchaseQ1AmountAvailable()
                ));
                
                vo.setQ2(createBudgetAmountVo(
                    aggregate.getPurchaseQ2AmountTotal(),
                    aggregate.getPurchaseQ2AmountAdj(),
                    aggregate.getPurchaseQ2TotalBudget(),
                    aggregate.getPurchaseQ2AmountFrozen(),
                    aggregate.getPurchaseQ2AmountOccupied(),
                    aggregate.getPurchaseQ2AmountActual(),
                    aggregate.getPurchaseQ2AmountActualApproved(),
                    aggregate.getPurchaseQ2AmountAvailable()
                ));
                
                vo.setQ3(createBudgetAmountVo(
                    aggregate.getPurchaseQ3AmountTotal(),
                    aggregate.getPurchaseQ3AmountAdj(),
                    aggregate.getPurchaseQ3TotalBudget(),
                    aggregate.getPurchaseQ3AmountFrozen(),
                    aggregate.getPurchaseQ3AmountOccupied(),
                    aggregate.getPurchaseQ3AmountActual(),
                    aggregate.getPurchaseQ3AmountActualApproved(),
                    aggregate.getPurchaseQ3AmountAvailable()
                ));
                
                vo.setQ4(createBudgetAmountVo(
                    aggregate.getPurchaseQ4AmountTotal(),
                    aggregate.getPurchaseQ4AmountAdj(),
                    aggregate.getPurchaseQ4TotalBudget(),
                    aggregate.getPurchaseQ4AmountFrozen(),
                    aggregate.getPurchaseQ4AmountOccupied(),
                    aggregate.getPurchaseQ4AmountActual(),
                    aggregate.getPurchaseQ4AmountActualApproved(),
                    aggregate.getPurchaseQ4AmountAvailable()
                ));
            } else if (isPayment) {
                // 付款额：使用 PAYMENT_* 开头的字段
                vo.setQ1(createBudgetAmountVo(
                    aggregate.getPaymentQ1AmountTotal(),
                    aggregate.getPaymentQ1AmountAdj(),
                    aggregate.getPaymentQ1TotalBudget(),
                    aggregate.getPaymentQ1AmountFrozen(),
                    aggregate.getPaymentQ1AmountOccupied(),
                    aggregate.getPaymentQ1AmountActual(),
                    aggregate.getPaymentQ1AmountActualApproved(),
                    aggregate.getPaymentQ1AmountAvailable()
                ));
                
                vo.setQ2(createBudgetAmountVo(
                    aggregate.getPaymentQ2AmountTotal(),
                    aggregate.getPaymentQ2AmountAdj(),
                    aggregate.getPaymentQ2TotalBudget(),
                    aggregate.getPaymentQ2AmountFrozen(),
                    aggregate.getPaymentQ2AmountOccupied(),
                    aggregate.getPaymentQ2AmountActual(),
                    aggregate.getPaymentQ2AmountActualApproved(),
                    aggregate.getPaymentQ2AmountAvailable()
                ));
                
                vo.setQ3(createBudgetAmountVo(
                    aggregate.getPaymentQ3AmountTotal(),
                    aggregate.getPaymentQ3AmountAdj(),
                    aggregate.getPaymentQ3TotalBudget(),
                    aggregate.getPaymentQ3AmountFrozen(),
                    aggregate.getPaymentQ3AmountOccupied(),
                    aggregate.getPaymentQ3AmountActual(),
                    aggregate.getPaymentQ3AmountActualApproved(),
                    aggregate.getPaymentQ3AmountAvailable()
                ));
                
                vo.setQ4(createBudgetAmountVo(
                    aggregate.getPaymentQ4AmountTotal(),
                    aggregate.getPaymentQ4AmountAdj(),
                    aggregate.getPaymentQ4TotalBudget(),
                    aggregate.getPaymentQ4AmountFrozen(),
                    aggregate.getPaymentQ4AmountOccupied(),
                    aggregate.getPaymentQ4AmountActual(),
                    aggregate.getPaymentQ4AmountActualApproved(),
                    aggregate.getPaymentQ4AmountAvailable()
                ));
            }
            
            // 计算总计
            vo.setTotal(calculateTotal(vo.getQ1(), vo.getQ2(), vo.getQ3(), vo.getQ4()));
            
            voList.add(vo);
        }
        
        return voList;
    }
}

