package com.jasolar.mis.module.system.service.budget.query.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jasolar.mis.framework.common.pojo.PageResult;
import com.jasolar.mis.module.system.controller.budget.vo.BudgetLedgerQueryParams;
import com.jasolar.mis.module.system.controller.budget.vo.BudgetLedgerVo;
import com.jasolar.mis.module.system.domain.budget.BudgetLedger;
import com.jasolar.mis.module.system.domain.budget.BudgetLedgerHead;
import com.jasolar.mis.module.system.domain.budget.BudgetLedgerSelfR;
import com.jasolar.mis.module.system.domain.budget.BudgetLedgerWithNames;
import com.jasolar.mis.module.system.controller.admin.dict.vo.DictEditVo;
import com.jasolar.mis.module.system.controller.admin.dict.vo.DictInfoByCodeVo;
import com.jasolar.mis.module.system.mapper.budget.BudgetLedgerHeadMapper;
import com.jasolar.mis.module.system.mapper.budget.BudgetLedgerMapper;
import com.jasolar.mis.module.system.mapper.budget.BudgetLedgerSelfRMapper;
import com.jasolar.mis.module.system.service.admin.dict.SystemDictService;
import com.jasolar.mis.module.system.service.budget.query.BudgetLedgerQueryService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import com.jasolar.mis.framework.common.security.LoginServletUtils;
import com.jasolar.mis.framework.common.security.LoginUser;
import com.jasolar.mis.module.system.domain.admin.usergroup.SystemUserGroupRDo;
import com.jasolar.mis.module.system.domain.admin.usergroup.SystemUserGroupDo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Description: 预算流水查询服务实现类
 * Author : Auto Generated
 * Date : 2025-01-XX
 * Version : 1.0
 */
@Service
@Slf4j
public class BudgetLedgerQueryServiceImpl implements BudgetLedgerQueryService {

    @Resource
    private BudgetLedgerMapper budgetLedgerMapper;
    
    @Resource
    private BudgetLedgerSelfRMapper budgetLedgerSelfRMapper;
    
    @Resource
    private BudgetLedgerHeadMapper budgetLedgerHeadMapper;
    
    @Resource
    private SystemDictService systemDictService;
    
    @Resource
    private com.jasolar.mis.module.system.mapper.admin.user.UserEhrOrgViewMapper userEhrOrgViewMapper;
    
    @Resource
    private com.jasolar.mis.module.system.mapper.admin.user.UserProjectViewMapper userProjectViewMapper;
    
    @Resource
    private com.jasolar.mis.module.system.mapper.admin.user.SystemUserMapper systemUserMapper;

    @Resource
    private com.jasolar.mis.module.system.mapper.admin.usergroup.SystemUserGroupRMapper systemUserGroupRMapper;

    @Resource
    private com.jasolar.mis.module.system.mapper.admin.usergroup.UserGroupMapper userGroupMapper;

    @Override
    public PageResult<BudgetLedgerVo> queryData(BudgetLedgerQueryParams params) {
        log.info("开始查询预算流水数据，params={}", params);

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
            PageResult<BudgetLedgerVo> pageResult = new PageResult<>();
            pageResult.setList(Collections.emptyList());
            pageResult.setTotal(0L);
            return pageResult;
        }

        // 构建分页对象
        IPage<BudgetLedgerWithNames> page = new Page<>(params.getPageNo(), params.getPageSize());

        // 根据不同的 bizType 处理关联查询逻辑
        Set<Long> targetIds = handleRelatedQuery(params);
        log.info("关联查询结果，targetIds={}, size={}", targetIds, targetIds != null ? targetIds.size() : 0);

        // 如果 targetIds 为空集合，直接返回空结果（注意：NAN 的情况会返回 null，需要在 SQL 中处理）
        if (targetIds != null && targetIds.isEmpty()) {
            log.warn("关联查询结果为空集合，返回空结果");
            PageResult<BudgetLedgerVo> pageResult = new PageResult<>();
            pageResult.setList(Collections.emptyList());
            pageResult.setTotal(0L);
            return pageResult;
        }

        // 将权限集合拆分成多个批次（每个批次最多1000个，避免Oracle IN子句超过1000个限制）
        List<List<String>> allowedEhrCdsBatches = splitIntoBatches(allowedEhrCds, 1000);
        List<List<String>> allowedProjectCodesBatches = splitIntoBatches(allowedProjectCodes, 1000);
        
        log.info("EHR组织编码拆分成 {} 个批次，项目编码拆分成 {} 个批次", 
                allowedEhrCdsBatches.size(), allowedProjectCodesBatches.size());

        // 使用 JOIN 查询（关联 BUDGET_LEDGER_HEAD 表，支持 status 条件）
        // 传入权限过滤条件：allowedEhrCdsBatches 和 allowedProjectCodesBatches
        IPage<BudgetLedgerWithNames> result = budgetLedgerMapper.selectPageWithHead(
                page, params, targetIds, allowedEhrCdsBatches, allowedProjectCodesBatches);
        
        // 调试日志：检查查询结果
        log.info("查询结果：total={}, records数量={}", result.getTotal(), result.getRecords() != null ? result.getRecords().size() : 0);
        if (result.getRecords() != null && !result.getRecords().isEmpty()) {
            // 检查是否有重复的ID
            Map<Long, Long> idCountMap = result.getRecords().stream()
                    .map(BudgetLedger::getId)
                    .filter(id -> id != null)
                    .collect(Collectors.groupingBy(id -> id, Collectors.counting()));
            idCountMap.entrySet().stream()
                    .filter(entry -> entry.getValue() > 1)
                    .forEach(entry -> log.warn("发现重复的ID: {}, 出现次数: {}", entry.getKey(), entry.getValue()));
            
            // 打印前几条记录的ID
            log.info("前5条记录的ID: {}", result.getRecords().stream()
                    .limit(5)
                    .map(BudgetLedger::getId)
                    .collect(Collectors.toList()));
        }

        // 查询关联的合同号和需求单号
        Map<Long, List<String>> contractNosMap = new HashMap<>();
        Map<Long, List<String>> demandOrderNosMap = new HashMap<>();
        if (!CollectionUtils.isEmpty(result.getRecords())) {
            // 提取 result 中的所有 id
            Set<Long> resultIds = result.getRecords().stream()
                    .map(BudgetLedger::getId)
                    .filter(id -> id != null)
                    .collect(Collectors.toSet());
            
            if (!resultIds.isEmpty()) {
                // 查询 BUDGET_LEDGER_SELF_R，条件是 ID IN (resultIds)
                // 分别查询 CONTRACT 和 APPLY 类型的关联关系
                // 注意：BudgetLedgerSelfR.id 是原始 BudgetLedger 的 id，relatedId 是关联的 BudgetLedger 的 id
                List<BudgetLedgerSelfR> contractSelfRs = budgetLedgerSelfRMapper.selectByIdsAndBizType(resultIds, "CONTRACT");
                List<BudgetLedgerSelfR> applySelfRs = budgetLedgerSelfRMapper.selectByIdsAndBizType(resultIds, "APPLY");
                
                // 处理合同单关联（CONTRACT 类型）
                if (!CollectionUtils.isEmpty(contractSelfRs)) {
                    // 提取关联的合同单 id（relatedId）
                    Set<Long> contractIds = contractSelfRs.stream()
                            .map(BudgetLedgerSelfR::getRelatedId)
                            .filter(id -> id != null)
                            .collect(Collectors.toSet());
                    
                    if (!contractIds.isEmpty()) {
                        // 查询关联的合同单
                        List<BudgetLedger> contractLedgers = budgetLedgerMapper.selectBatchIds(contractIds);
                        if (!CollectionUtils.isEmpty(contractLedgers)) {
                            // 建立合同单 id 到合同单的映射
                            Map<Long, BudgetLedger> contractIdToLedgerMap = contractLedgers.stream()
                                    .collect(Collectors.toMap(BudgetLedger::getId, ledger -> ledger, (existing, replacement) -> existing));
                            
                            // 建立原始 id 到合同单号的映射
                            for (BudgetLedgerSelfR selfR : contractSelfRs) {
                                Long originalId = selfR.getId();
                                Long contractId = selfR.getRelatedId();
                                BudgetLedger contractLedger = contractIdToLedgerMap.get(contractId);
                                if (contractLedger != null && StringUtils.hasText(contractLedger.getBizCode())) {
                                    contractNosMap.computeIfAbsent(originalId, k -> new ArrayList<>()).add(contractLedger.getBizCode());
                                }
                            }
                        }
                    }
                }
                
                // 处理需求单关联（APPLY 类型）
                if (!CollectionUtils.isEmpty(applySelfRs)) {
                    // 提取关联的需求单 id（relatedId）
                    Set<Long> applyIds = applySelfRs.stream()
                            .map(BudgetLedgerSelfR::getRelatedId)
                            .filter(id -> id != null)
                            .collect(Collectors.toSet());
                    
                    if (!applyIds.isEmpty()) {
                        // 查询关联的需求单
                        List<BudgetLedger> applyLedgers = budgetLedgerMapper.selectBatchIds(applyIds);
                        if (!CollectionUtils.isEmpty(applyLedgers)) {
                            // 建立需求单 id 到需求单的映射
                            Map<Long, BudgetLedger> applyIdToLedgerMap = applyLedgers.stream()
                                    .collect(Collectors.toMap(BudgetLedger::getId, ledger -> ledger, (existing, replacement) -> existing));
                            
                            // 建立原始 id 到需求单号的映射
                            for (BudgetLedgerSelfR selfR : applySelfRs) {
                                Long originalId = selfR.getId();
                                Long applyId = selfR.getRelatedId();
                                BudgetLedger applyLedger = applyIdToLedgerMap.get(applyId);
                                if (applyLedger != null && StringUtils.hasText(applyLedger.getBizCode())) {
                                    demandOrderNosMap.computeIfAbsent(originalId, k -> new ArrayList<>()).add(applyLedger.getBizCode());
                                }
                            }
                        }
                    }
                }
                
                // 对于付款单（CLAIM），还需要查询通过合同单间接关联的需求单
                String bizType = params.getBizType();
                if ("CLAIM".equals(bizType) && !contractNosMap.isEmpty()) {
                    // 获取所有关联的合同单 id
                    Set<Long> allContractIds = new HashSet<>();
                    for (List<String> contractNos : contractNosMap.values()) {
                        // 这里需要根据合同单号查询合同单 id，但我们已经有了合同单的 id
                        // 实际上，我们需要从 contractSelfRs 中获取合同单 id
                    }
                    
                    // 从 contractSelfRs 中提取所有合同单 id
                    Set<Long> contractIdsFromSelfR = contractSelfRs.stream()
                            .map(BudgetLedgerSelfR::getRelatedId)
                            .filter(id -> id != null)
                            .collect(Collectors.toSet());
                    
                    if (!contractIdsFromSelfR.isEmpty()) {
                        // 查询这些合同单关联的需求单
                        List<BudgetLedgerSelfR> contractToApplySelfRs = budgetLedgerSelfRMapper.selectByIdsAndBizType(contractIdsFromSelfR, "APPLY");
                        if (!CollectionUtils.isEmpty(contractToApplySelfRs)) {
                            // 提取关联的需求单 id
                            Set<Long> indirectApplyIds = contractToApplySelfRs.stream()
                                    .map(BudgetLedgerSelfR::getRelatedId)
                                    .filter(id -> id != null)
                                    .collect(Collectors.toSet());
                            
                            if (!indirectApplyIds.isEmpty()) {
                                // 查询间接关联的需求单
                                List<BudgetLedger> indirectApplyLedgers = budgetLedgerMapper.selectBatchIds(indirectApplyIds);
                                if (!CollectionUtils.isEmpty(indirectApplyLedgers)) {
                                    // 建立需求单 id 到需求单的映射
                                    Map<Long, BudgetLedger> indirectApplyIdToLedgerMap = indirectApplyLedgers.stream()
                                            .collect(Collectors.toMap(BudgetLedger::getId, ledger -> ledger, (existing, replacement) -> existing));
                                    
                                    // 建立合同单 id 到付款单 id 的映射（通过 contractSelfRs）
                                    Map<Long, Set<Long>> contractIdToClaimIdsMap = new HashMap<>();
                                    for (BudgetLedgerSelfR selfR : contractSelfRs) {
                                        Long claimId = selfR.getId();
                                        Long contractId = selfR.getRelatedId();
                                        contractIdToClaimIdsMap.computeIfAbsent(contractId, k -> new HashSet<>()).add(claimId);
                                    }
                                    
                                    // 建立合同单 id 到需求单 id 的映射（通过 contractToApplySelfRs）
                                    Map<Long, Set<Long>> contractIdToApplyIdsMap = new HashMap<>();
                                    for (BudgetLedgerSelfR selfR : contractToApplySelfRs) {
                                        Long contractId = selfR.getId();
                                        Long applyId = selfR.getRelatedId();
                                        contractIdToApplyIdsMap.computeIfAbsent(contractId, k -> new HashSet<>()).add(applyId);
                                    }
                                    
                                    // 为每个付款单添加间接关联的需求单号
                                    for (Map.Entry<Long, Set<Long>> contractEntry : contractIdToClaimIdsMap.entrySet()) {
                                        Long contractId = contractEntry.getKey();
                                        Set<Long> claimIds = contractEntry.getValue();
                                        Set<Long> applyIds = contractIdToApplyIdsMap.get(contractId);
                                        
                                        if (applyIds != null && !applyIds.isEmpty()) {
                                            for (Long claimId : claimIds) {
                                                for (Long applyId : applyIds) {
                                                    BudgetLedger applyLedger = indirectApplyIdToLedgerMap.get(applyId);
                                                    if (applyLedger != null && StringUtils.hasText(applyLedger.getBizCode())) {
                                                        // 检查是否已经存在（避免重复）
                                                        List<String> existingDemandOrderNos = demandOrderNosMap.get(claimId);
                                                        if (existingDemandOrderNos == null || !existingDemandOrderNos.contains(applyLedger.getBizCode())) {
                                                            demandOrderNosMap.computeIfAbsent(claimId, k -> new ArrayList<>()).add(applyLedger.getBizCode());
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // 转换为 BudgetLedgerVo（包含 status 字段和名称字段）
        // 注意：去重应该在SQL层面完成，这里不应该再去重，否则会影响分页结果
        List<BudgetLedgerVo> voList = convertToVoList(result.getRecords(), params.getBizType(), contractNosMap, demandOrderNosMap);

        // 构建返回结果
        PageResult<BudgetLedgerVo> pageResult = new PageResult<>();
        pageResult.setList(voList);
        pageResult.setTotal(result.getTotal());
        
        return pageResult;
    }

    @Override
    public List<BudgetLedgerVo> queryAllData(BudgetLedgerQueryParams params) {
        log.info("开始查询预算流水全量数据，params={}", params);
        
        List<BudgetLedgerVo> allData = new ArrayList<>();
        int pageNo = 1;
        int pageSize = 1000; // 每页查询1000条
        
        while (true) {
            // 设置分页参数
            params.setPageNo(pageNo);
            params.setPageSize(pageSize);
            
            // 查询当前页数据
            PageResult<BudgetLedgerVo> pageResult = queryData(params);
            
            if (pageResult == null || CollectionUtils.isEmpty(pageResult.getList())) {
                break;
            }
            
            // 添加到全量列表
            allData.addAll(pageResult.getList());
            
            // 如果当前页数据少于pageSize，说明已经是最后一页
            if (pageResult.getList().size() < pageSize) {
                break;
            }
            
            pageNo++;
        }
        
        log.info("查询预算流水全量数据完成，共{}条", allData.size());
        return allData;
    }

    /**
     * 处理关联查询逻辑
     * @param params 查询参数
     * @return 目标ID集合，如果为null表示不需要关联查询（包括NAN的情况，需要在SQL中处理），如果为空集合表示关联查询无结果
     */
    private Set<Long> handleRelatedQuery(BudgetLedgerQueryParams params) {
        String bizType = params.getBizType();
        
        if ("APPLY".equals(bizType)) {
            // APPLY 类型直接查询，不需要关联
            return null;
        } else if ("CONTRACT".equals(bizType)) {
            // CONTRACT 类型：如果填了需求单号，需要关联查询
            if (StringUtils.hasText(params.getDemandOrderNo())) {
                // 如果需求单号是 "NAN"，表示查询没关联需求单号的合同单，在 SQL 中处理
                if ("NAN".equals(params.getDemandOrderNo())) {
                    return null; // 返回 null，在 SQL 中使用 LEFT JOIN + IS NULL 排除已关联的记录
                }
                return handleContractRelatedQuery(params.getDemandOrderNo());
            }
            // 没有填需求单号，直接查询
            return null;
        } else if ("CLAIM".equals(bizType)) {
            // CLAIM 类型：如果填了合同单号或需求单号，需要关联查询
            boolean hasContractNo = StringUtils.hasText(params.getContractNo());
            boolean hasDemandOrderNo = StringUtils.hasText(params.getDemandOrderNo());
            
            // 如果合同单号或需求单号是 "NAN"，表示查询没关联的数据，在 SQL 中处理
            boolean contractNoIsNan = hasContractNo && "NAN".equals(params.getContractNo());
            boolean demandOrderNoIsNan = hasDemandOrderNo && "NAN".equals(params.getDemandOrderNo());
            
            if (contractNoIsNan || demandOrderNoIsNan) {
                // 如果都是 NAN，或者一个是 NAN 另一个没填，在 SQL 中处理
                // 如果一个是 NAN 另一个有值，需要特殊处理（先按有值的查询，再排除 NAN 对应的关联）
                if (contractNoIsNan && demandOrderNoIsNan) {
                    // 两个都是 NAN：查询既没关联合同单号，也没关联需求单号的付款单
                    return null; // 在 SQL 中处理
                } else if (contractNoIsNan && !hasDemandOrderNo) {
                    // 只有合同单号是 NAN：查询没关联合同单号的付款单
                    return null; // 在 SQL 中处理
                } else if (demandOrderNoIsNan && !hasContractNo) {
                    // 只有需求单号是 NAN：查询没关联需求单号的付款单
                    return null; // 在 SQL 中处理
                } else {
                    // 一个是 NAN，另一个有值：先按有值的查询，然后在 SQL 中排除 NAN 对应的关联
                    // 这种情况比较复杂，需要先查询有值的关联，然后在 SQL 中排除 NAN 对应的关联
                    if (contractNoIsNan) {
                        // 合同单号是 NAN，需求单号有值：先查询关联了该需求单号的付款单，然后排除关联了合同单号的
                        return handleClaimRelatedQuery(null, params.getDemandOrderNo());
                    } else {
                        // 需求单号是 NAN，合同单号有值：先查询关联了该合同单号的付款单，然后排除关联了需求单号的
                        return handleClaimRelatedQuery(params.getContractNo(), null);
                    }
                }
            } else if (hasContractNo || hasDemandOrderNo) {
                // 正常情况：有合同单号或需求单号（都不是 NAN）
                return handleClaimRelatedQuery(params.getContractNo(), params.getDemandOrderNo());
            }
            // 没有填合同单号和需求单号，直接查询
            return null;
        }
        
        // 其他类型直接查询
        return null;
    }

    /**
     * 处理 CONTRACT 类型的关联查询
     * @param demandOrderNo 需求单号
     * @return 目标ID集合
     */
    private Set<Long> handleContractRelatedQuery(String demandOrderNo) {
        // 1. 先查询 BUDGET_LEDGER 表，BIZ_TYPE='APPLY', BIZ_CODE 支持模糊搜索（%xxx%）
        LambdaQueryWrapper<BudgetLedger> applyWrapper = new LambdaQueryWrapper<>();
        applyWrapper.eq(BudgetLedger::getDeleted, 0)
                .eq(BudgetLedger::getBizType, "APPLY")
                .like(BudgetLedger::getBizCode, demandOrderNo);
        List<BudgetLedger> applyLedgers = budgetLedgerMapper.selectList(applyWrapper);
        
        if (CollectionUtils.isEmpty(applyLedgers)) {
            // 如果没有找到对应的 APPLY 记录，返回空集合
            return Collections.emptySet();
        }
        
        // 2. 获取 APPLY 记录的 id 集合
        Set<Long> applyIds = applyLedgers.stream()
                .map(BudgetLedger::getId)
                .collect(Collectors.toSet());
        
        // 3. 查询 BUDGET_LEDGER_SELF_R 表，BIZ_TYPE='APPLY'（关联的 APPLY 记录）
        // 先按 id 在 applyIds 中查：即关系为 id=需求单ID, related_id=合同单ID
        List<BudgetLedgerSelfR> selfRs = budgetLedgerSelfRMapper.selectByIdsAndBizType(applyIds, "APPLY");
        
        // 若按 id 查不到，再按 related_id 反查：即关系为 id=合同单ID, related_id=需求单ID（实际数据常按此存储）
        if (CollectionUtils.isEmpty(selfRs)) {
            selfRs = budgetLedgerSelfRMapper.selectByRelatedIdsAndBizType(applyIds, "APPLY");
            if (!CollectionUtils.isEmpty(selfRs)) {
                // 反向关系：CONTRACT 的 id 在 selfR 的 id 列
                return selfRs.stream()
                        .map(BudgetLedgerSelfR::getId)
                        .filter(id -> id != null)
                        .collect(Collectors.toSet());
            }
            return Collections.emptySet();
        }
        
        // 4. 正向关系：CONTRACT 的 id 在 related_id 列
        return selfRs.stream()
                .map(BudgetLedgerSelfR::getRelatedId)
                .filter(id -> id != null)
                .collect(Collectors.toSet());
    }

    /**
     * 处理 CLAIM 类型的关联查询
     * @param contractNo 合同单号
     * @param demandOrderNo 需求单号
     * @return 目标ID集合
     */
    private Set<Long> handleClaimRelatedQuery(String contractNo, String demandOrderNo) {
        log.info("开始处理 CLAIM 类型的关联查询，contractNo={}, demandOrderNo={}", contractNo, demandOrderNo);
        Set<Long> contractClaimIds = null;
        Set<Long> applyClaimIds = null;
        
        // 1. 如果填了合同单号，查询 BUDGET_LEDGER 表，BIZ_TYPE='CONTRACT', BIZ_CODE 支持模糊搜索（%xxx%）
        if (StringUtils.hasText(contractNo)) {
            log.info("查询合同单号对应的合同单记录，contractNo={}", contractNo);
            LambdaQueryWrapper<BudgetLedger> contractWrapper = new LambdaQueryWrapper<>();
            contractWrapper.eq(BudgetLedger::getDeleted, 0)
                    .eq(BudgetLedger::getBizType, "CONTRACT")
                    .like(BudgetLedger::getBizCode, contractNo);
            List<BudgetLedger> contractLedgers = budgetLedgerMapper.selectList(contractWrapper);
            log.info("查询到合同单记录数量: {}", contractLedgers != null ? contractLedgers.size() : 0);
            if (!CollectionUtils.isEmpty(contractLedgers)) {
                Set<Long> contractIds = contractLedgers.stream()
                        .map(BudgetLedger::getId)
                        .collect(Collectors.toSet());
                log.info("合同单ID集合: {}", contractIds);
                
                // 查询 BUDGET_LEDGER_SELF_R 表，BIZ_TYPE='CONTRACT'（关联的 CONTRACT 记录），id 在 contractIds 中
                List<BudgetLedgerSelfR> contractSelfRs = budgetLedgerSelfRMapper.selectByIdsAndBizType(contractIds, "CONTRACT");
                log.info("查询到合同单关联关系数量（通过id查询）: {}", contractSelfRs != null ? contractSelfRs.size() : 0);
                
                // 如果通过 id 查询没有找到，尝试通过 related_id 反向查询
                if (CollectionUtils.isEmpty(contractSelfRs)) {
                    log.info("尝试通过 related_id 反向查询关联关系");
                    contractSelfRs = budgetLedgerSelfRMapper.selectByRelatedIdsAndBizType(contractIds, "CONTRACT");
                    log.info("查询到合同单关联关系数量（通过related_id查询）: {}", contractSelfRs != null ? contractSelfRs.size() : 0);
                    if (!CollectionUtils.isEmpty(contractSelfRs)) {
                        // 如果通过 related_id 查询找到了，说明关联方向是反的，应该使用 id 而不是 related_id
                        contractClaimIds = contractSelfRs.stream()
                                .map(BudgetLedgerSelfR::getId)
                                .filter(id -> id != null)
                                .collect(Collectors.toSet());
                        log.info("通过合同单关联查询到的付款单ID集合（反向查询）: {}", contractClaimIds);
                    }
                } else {
                    // 正常方向：id 是 CONTRACT 的 id，related_id 是 CLAIM 的 id
                    contractClaimIds = contractSelfRs.stream()
                            .map(BudgetLedgerSelfR::getRelatedId)
                            .filter(id -> id != null)
                            .collect(Collectors.toSet());
                    log.info("通过合同单关联查询到的付款单ID集合: {}", contractClaimIds);
                }
            }
        }
        
        // 2. 如果填了需求单号，查询 BUDGET_LEDGER 表，BIZ_TYPE='APPLY', BIZ_CODE 支持模糊搜索（%xxx%）
        if (StringUtils.hasText(demandOrderNo)) {
            log.info("查询需求单号对应的需求单记录，demandOrderNo={}", demandOrderNo);
            LambdaQueryWrapper<BudgetLedger> applyWrapper = new LambdaQueryWrapper<>();
            applyWrapper.eq(BudgetLedger::getDeleted, 0)
                    .eq(BudgetLedger::getBizType, "APPLY")
                    .like(BudgetLedger::getBizCode, demandOrderNo);
            List<BudgetLedger> applyLedgers = budgetLedgerMapper.selectList(applyWrapper);
            log.info("查询到需求单记录数量: {}", applyLedgers != null ? applyLedgers.size() : 0);
            if (!CollectionUtils.isEmpty(applyLedgers)) {
                Set<Long> applyIds = applyLedgers.stream()
                        .map(BudgetLedger::getId)
                        .collect(Collectors.toSet());
                log.info("需求单ID集合: {}", applyIds);
                
                // 查询 BUDGET_LEDGER_SELF_R 表，BIZ_TYPE='APPLY'（关联的 APPLY 记录），id 在 applyIds 中
                List<BudgetLedgerSelfR> applySelfRs = budgetLedgerSelfRMapper.selectByIdsAndBizType(applyIds, "APPLY");
                log.info("查询到需求单关联关系数量（通过id查询）: {}", applySelfRs != null ? applySelfRs.size() : 0);
                
                // 如果通过 id 查询没有找到，尝试通过 related_id 反向查询
                if (CollectionUtils.isEmpty(applySelfRs)) {
                    log.info("尝试通过 related_id 反向查询关联关系");
                    applySelfRs = budgetLedgerSelfRMapper.selectByRelatedIdsAndBizType(applyIds, "APPLY");
                    log.info("查询到需求单关联关系数量（通过related_id查询）: {}", applySelfRs != null ? applySelfRs.size() : 0);
                    if (!CollectionUtils.isEmpty(applySelfRs)) {
                        // 如果通过 related_id 查询找到了，说明关联方向是反的，应该使用 id 而不是 related_id
                        applyClaimIds = applySelfRs.stream()
                                .map(BudgetLedgerSelfR::getId)
                                .filter(id -> id != null)
                                .collect(Collectors.toSet());
                        log.info("通过需求单关联查询到的付款单ID集合（反向查询）: {}", applyClaimIds);
                    }
                } else {
                    // 正常方向：id 是 APPLY 的 id，related_id 是 CLAIM 的 id
                    applyClaimIds = applySelfRs.stream()
                            .map(BudgetLedgerSelfR::getRelatedId)
                            .filter(id -> id != null)
                            .collect(Collectors.toSet());
                    log.info("通过需求单关联查询到的付款单ID集合: {}", applyClaimIds);
                }
                
                if (CollectionUtils.isEmpty(applySelfRs)) {
                    log.warn("需求单ID集合 {} 在 BUDGET_LEDGER_SELF_R 表中没有找到关联关系（BIZ_TYPE='APPLY'）", applyIds);
                }
            } else {
                log.warn("需求单号 {} 在 BUDGET_LEDGER 表中不存在（BIZ_TYPE='APPLY'）", demandOrderNo);
            }
        }
        
        // 3. 如果两个都填了，取交集；如果只填了一个，就用那个结果
        if (contractClaimIds != null && applyClaimIds != null) {
            // 两个都填了，取交集
            contractClaimIds.retainAll(applyClaimIds);
            log.info("两个都填了，取交集后的付款单ID集合: {}", contractClaimIds);
            return contractClaimIds;
        } else if (contractClaimIds != null) {
            // 只填了合同单号
            log.info("只填了合同单号，返回付款单ID集合: {}", contractClaimIds);
            return contractClaimIds;
        } else if (applyClaimIds != null) {
            // 只填了需求单号
            log.info("只填了需求单号，返回付款单ID集合: {}", applyClaimIds);
            return applyClaimIds;
        } else {
            // 两个都没填（理论上不会到这里，因为调用前已经判断过）
            log.warn("两个都没填或都没有找到关联关系，返回空集合");
            return Collections.emptySet();
        }
    }

    /**
     * 将 BudgetLedgerWithNames 列表转换为 BudgetLedgerVo 列表（包含 status 字段和字典描述）
     */
    private List<BudgetLedgerVo> convertToVoList(List<BudgetLedgerWithNames> ledgerList, String bizType,
                                                  Map<Long, List<String>> contractNosMap, Map<Long, List<String>> demandOrderNosMap) {
        if (CollectionUtils.isEmpty(ledgerList)) {
            return Collections.emptyList();
        }
        
        // 批量查询 BudgetLedgerHead 获取 status
        Set<String> bizCodes = ledgerList.stream()
                .map(BudgetLedger::getBizCode)
                .filter(StringUtils::hasText)
                .collect(Collectors.toSet());
        
        Map<String, BudgetLedgerHead> bizCodeToHeadMap = new HashMap<>();
        if (!bizCodes.isEmpty()) {
            LambdaQueryWrapper<BudgetLedgerHead> headWrapper = new LambdaQueryWrapper<>();
            headWrapper.eq(BudgetLedgerHead::getDeleted, 0)
                    .eq(BudgetLedgerHead::getBizType, bizType)
                    .in(BudgetLedgerHead::getBizCode, bizCodes);
            List<BudgetLedgerHead> heads = budgetLedgerHeadMapper.selectList(headWrapper);
            
            if (!CollectionUtils.isEmpty(heads)) {
                bizCodeToHeadMap = heads.stream()
                        .collect(Collectors.toMap(
                                BudgetLedgerHead::getBizCode,
                                head -> head,
                                (existing, replacement) -> existing
                        ));
            }
        }
        
        // 批量查询字典信息
        List<String> dictCodes = List.of("budgetLedgerBizType", "receiptStatus", "internal", "effectType");
        DictInfoByCodeVo dictInfoByCodeVo = DictInfoByCodeVo.builder()
                .codes(dictCodes)
                .build();
        Map<String, DictEditVo> dictMap = systemDictService.getByCode(dictInfoByCodeVo);
        
        // 转换为 VO，并设置 status 和字典描述
        final Map<String, BudgetLedgerHead> finalHeadMap = bizCodeToHeadMap;
        final Map<Long, List<String>> finalContractNosMap = contractNosMap;
        final Map<Long, List<String>> finalDemandOrderNosMap = demandOrderNosMap;
        return ledgerList.stream()
                .map(ledger -> convertToVo(ledger, finalHeadMap.get(ledger.getBizCode()), dictMap, 
                        finalContractNosMap, finalDemandOrderNosMap))
                .collect(Collectors.toList());
    }

    /**
     * 将 BudgetLedgerWithNames 转换为 BudgetLedgerVo
     */
    private BudgetLedgerVo convertToVo(BudgetLedgerWithNames ledger, BudgetLedgerHead head, Map<String, DictEditVo> dictMap,
                                       Map<Long, List<String>> contractNosMap, Map<Long, List<String>> demandOrderNosMap) {
        BudgetLedgerVo vo = new BudgetLedgerVo();
        
        // 复制字段（排除 id 和 deleted）
        BeanUtils.copyProperties(ledger, vo);
        
        // 设置名称字段
        vo.setMorgName(ledger.getMorgName());
        vo.setBudgetSubjectName(ledger.getBudgetSubjectName());
        vo.setMasterProjectName(ledger.getMasterProjectName());
        vo.setErpAssetTypeName(ledger.getErpAssetTypeName());
        
        // 设置合同号和需求单号列表
        Long ledgerId = ledger.getId();
        if (ledgerId != null) {
            // 设置合同号列表
            List<String> contractNos = contractNosMap.get(ledgerId);
            vo.setContractNos(contractNos != null ? contractNos : new ArrayList<>());
            
            // 设置需求单号列表
            List<String> demandOrderNos = demandOrderNosMap.get(ledgerId);
            vo.setDemandOrderNos(demandOrderNos != null ? demandOrderNos : new ArrayList<>());
        } else {
            vo.setContractNos(new ArrayList<>());
            vo.setDemandOrderNos(new ArrayList<>());
        }
        
        // 从 BUDGET_LEDGER_HEAD 设置 status、操作人、操作时间、来源系统、流程名称
        if (head != null) {
            vo.setStatus(head.getStatus());
            vo.setOperator(head.getOperator());
            vo.setOperatorNo(head.getOperatorNo());
            vo.setUpdateTime(head.getUpdateTime());
            vo.setDataSource(head.getDataSource());
            vo.setProcessName(head.getProcessName());
        } else {
            // 无 head 时回退为 SQL 中 join 的 ledger 映射值（可能为 null）
            vo.setOperator(ledger.getOperator());
            vo.setOperatorNo(ledger.getOperatorNo());
            vo.setDataSource(ledger.getDataSource());
            vo.setProcessName(ledger.getProcessName());
        }
        // updateTime：有 head 时已取 head.getUpdateTime()；无 head 时由 BeanUtils.copyProperties(ledger, vo) 带入
        
        // 设置字典描述
        // bizTypeDes 对应字典 code: budgetLedgerBizType
        String bizTypeDes = systemDictService.getFieldLabel(dictMap, "budgetLedgerBizType", ledger.getBizType());
        vo.setBizTypeDes(bizTypeDes);
        
        // statusDes 对应字典 code: receiptStatus
        if (head != null && StringUtils.hasText(head.getStatus())) {
            String statusDes = systemDictService.getFieldLabel(dictMap, "receiptStatus", head.getStatus());
            vo.setStatusDes(statusDes);
        }
        
        // isInternalDes 对应字典 code: internal
        if (StringUtils.hasText(ledger.getIsInternal())) {
            String isInternalDes = systemDictService.getFieldLabel(dictMap, "internal", ledger.getIsInternal());
            vo.setIsInternalDes(isInternalDes);
        }
        
        // effectTypeDes 对应字典 code: effectType
        if (StringUtils.hasText(ledger.getEffectType())) {
            String effectTypeDes = systemDictService.getFieldLabel(dictMap, "effectType", ledger.getEffectType());
            vo.setEffectTypeDes(effectTypeDes);
        }
        
        // 初始化关联明细列表为空列表
        vo.setRelatedDetails(new ArrayList<>());
        
        return vo;
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
}

