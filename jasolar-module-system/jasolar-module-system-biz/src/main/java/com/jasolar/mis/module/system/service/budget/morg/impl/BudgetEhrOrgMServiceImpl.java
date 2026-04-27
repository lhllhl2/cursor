package com.jasolar.mis.module.system.service.budget.morg.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.incrementer.IdentifierGenerator;
import com.jasolar.mis.module.system.domain.ehr.EhrOrgManageExtR;
import com.jasolar.mis.module.system.domain.ehr.EhrOrgManageOneR;
import com.jasolar.mis.module.system.domain.ehr.EhrOrgManageR;
import com.jasolar.mis.module.system.mapper.ehr.EhrOrgManageExtRMapper;
import com.jasolar.mis.module.system.mapper.ehr.EhrOrgManageOneRMapper;
import com.jasolar.mis.module.system.mapper.ehr.EhrOrgManageRMapper;
import com.jasolar.mis.module.system.service.budget.morg.BudgetEhrOrgMService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * EHR组织管理扩展关系 Service 实现
 */
@Service
@Slf4j
public class BudgetEhrOrgMServiceImpl implements BudgetEhrOrgMService {

    @Resource
    private EhrOrgManageRMapper ehrOrgManageRMapper;

    @Resource
    private EhrOrgManageExtRMapper ehrOrgManageExtRMapper;

    @Resource
    private EhrOrgManageOneRMapper ehrOrgManageOneRMapper;

    @Resource
    private IdentifierGenerator identifierGenerator;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String syncEhrManageRData(List<String> ehrCodes) {
        log.info("开始同步EHR管理组织关系数据，ehrCodes: {}", ehrCodes);

        if (ehrCodes == null || ehrCodes.isEmpty()) {
            return "EHR编码列表为空";
        }

        // Oracle IN子句最多支持1000个表达式，定义批次大小
        final int batchSize = 1000;

        // 第一步：批量查询EHR_ORG_MANAGE_R表，获取初始数据（只查询year=2026的数据）
        // 分批查询，避免Oracle IN子句超过1000个的限制
        List<EhrOrgManageR> initialList = new ArrayList<>();
        for (int i = 0; i < ehrCodes.size(); i += batchSize) {
            int end = Math.min(i + batchSize, ehrCodes.size());
            List<String> batch = ehrCodes.subList(i, end);
            List<EhrOrgManageR> batchList = ehrOrgManageRMapper.selectList(
                    new LambdaQueryWrapper<EhrOrgManageR>()
                            .in(EhrOrgManageR::getEhrCd, batch)
                            .eq(EhrOrgManageR::getDeleted, false)
                            .eq(EhrOrgManageR::getYear, "2026")
            );
            initialList.addAll(batchList);
        }

        if (initialList.isEmpty()) {
            return "未找到对应的EHR组织数据";
        }

        // 建立ehrCd到EhrOrgManageR的映射
        Map<String, EhrOrgManageR> ehrCodeMap = initialList.stream()
                .collect(Collectors.toMap(EhrOrgManageR::getEhrCd, e -> e, (e1, e2) -> e1));

        // 第二步：对于每个ehrCode，向上追溯找到CONTROL_LEVEL=1的父级
        // Map<原始EHR_CD, CONTROL_LEVEL=1的父级EHR_CD>
        Map<String, String> ehrCodeToParentMap = new HashMap<>();
        // 存储找不到CONTROL_LEVEL=1父级的EHR编码
        Set<String> ehrCodesWithoutControlLevelOne = new HashSet<>();

        for (String ehrCode : ehrCodes) {
            EhrOrgManageR current = ehrCodeMap.get(ehrCode);
            if (current == null) {
                throw new RuntimeException("未找到EHR编码对应的数据: " + ehrCode);
            }

            // 特别检查 506155
            if ("506155".equals(ehrCode)) {
                log.info("【调试】开始处理 506155: CONTROL_LEVEL={}, EHR_PAR_CD={}, YEAR={}, ORG_CD={}, DELETED={}", 
                        current.getControlLevel(), current.getEhrParCd(), current.getYear(), 
                        current.getOrgCd(), current.getDeleted());
            }

            // 如果当前就是CONTROL_LEVEL=1，则父级就是自己
            if ("1".equals(current.getControlLevel())) {
                ehrCodeToParentMap.put(ehrCode, ehrCode);
                if ("506155".equals(ehrCode)) {
                    log.info("【调试】506155 本身就是 CONTROL_LEVEL=1，父级是自己");
                }
                continue;
            }

            // 向上追溯
            String parentEhrCode = findControlLevelOneParent(current.getEhrParCd(), new HashSet<>());
            if (parentEhrCode == null) {
                // 找不到CONTROL_LEVEL=1的父级，标记但不抛出异常（参考SubjectExtInfoServiceImpl的处理方式）
                ehrCodesWithoutControlLevelOne.add(ehrCode);
                log.warn("EHR编码 {} 向上追溯后仍然找不到CONTROL_LEVEL=1的父级，将在后续处理中特殊处理", ehrCode);
                if ("506155".equals(ehrCode)) {
                    log.warn("【调试】506155 向上追溯失败，当前 EHR_PAR_CD={}，将检查父级链", current.getEhrParCd());
                    // 手动追踪父级链
                    String currentParCd = current.getEhrParCd();
                    int level = 1;
                    while (StringUtils.isNotBlank(currentParCd) && level < 10) {
                        List<EhrOrgManageR> parentRecord = ehrOrgManageRMapper.selectList(
                                new LambdaQueryWrapper<EhrOrgManageR>()
                                        .eq(EhrOrgManageR::getEhrCd, currentParCd)
                                        .eq(EhrOrgManageR::getDeleted, false)
                                        .eq(EhrOrgManageR::getYear, "2026")
                        );
                        if (!parentRecord.isEmpty()) {
                            EhrOrgManageR parent = parentRecord.get(0);
                            log.warn("【调试】506155 的第{}级父级: EHR_CD={}, CONTROL_LEVEL={}, EHR_PAR_CD={}, ORG_CD={}", 
                                    level, currentParCd, parent.getControlLevel(), parent.getEhrParCd(), parent.getOrgCd());
                            currentParCd = parent.getEhrParCd();
                            level++;
                        } else {
                            log.warn("【调试】506155 的第{}级父级 {} 不存在（year=2026）", level, currentParCd);
                            break;
                        }
                    }
                }
            } else {
                ehrCodeToParentMap.put(ehrCode, parentEhrCode);
                if ("506155".equals(ehrCode)) {
                    log.info("【调试】506155 找到 CONTROL_LEVEL=1 的父级: {}", parentEhrCode);
                }
            }
        }

        log.info("EHR编码到父级映射: {}", ehrCodeToParentMap);
        if (!ehrCodesWithoutControlLevelOne.isEmpty()) {
            log.info("找不到CONTROL_LEVEL=1父级的EHR编码（共 {} 个）: {}", 
                    ehrCodesWithoutControlLevelOne.size(), ehrCodesWithoutControlLevelOne);
        }

        // 第三步：对于每个CONTROL_LEVEL=1的父级，找出所有子集
        Set<String> uniqueParentCodes = new HashSet<>(ehrCodeToParentMap.values());
        
        // 批量查询所有父级对应的 EhrOrgManageR 记录，避免在循环中查询（只查询year=2026的数据）
        // 分批查询，避免Oracle IN子句超过1000个的限制
        List<EhrOrgManageR> parentRecords = new ArrayList<>();
        List<String> parentCodeList = new ArrayList<>(uniqueParentCodes);
        for (int i = 0; i < parentCodeList.size(); i += batchSize) {
            int end = Math.min(i + batchSize, parentCodeList.size());
            List<String> batch = parentCodeList.subList(i, end);
            List<EhrOrgManageR> batchList = ehrOrgManageRMapper.selectList(
                    new LambdaQueryWrapper<EhrOrgManageR>()
                            .in(EhrOrgManageR::getEhrCd, batch)
                            .eq(EhrOrgManageR::getDeleted, false)
                            .eq(EhrOrgManageR::getYear, "2026")
            );
            parentRecords.addAll(batchList);
        }
        // 建立 ehrCd 到 EhrOrgManageR 的映射
        Map<String, EhrOrgManageR> parentCodeToRecordMap = parentRecords.stream()
                .collect(Collectors.toMap(EhrOrgManageR::getEhrCd, e -> e, (e1, e2) -> e1));
        
        // Map<父级EHR_CD, 所有子集EHR_CD列表（包含父级本身）>
        Map<String, Set<String>> parentToChildrenMap = new HashMap<>();

        for (String parentCode : uniqueParentCodes) {
            Set<String> childrenSet = findAllChildren(parentCode, new HashSet<>());
            // 将父级本身也加入到子集中（因为findAllChildren只添加子节点，不添加父节点本身）
            childrenSet.add(parentCode);
            parentToChildrenMap.put(parentCode, childrenSet);
        }

        log.info("父级到子集映射: {}", parentToChildrenMap);
        
        // 特别检查 506155 是否在任何控制层级的子集中
        boolean found506155 = false;
        for (Map.Entry<String, Set<String>> entry : parentToChildrenMap.entrySet()) {
            if (entry.getValue().contains("506155")) {
                log.info("【调试】506155 在控制层级 {} 的子集中，该控制层级包含 {} 个节点", entry.getKey(), entry.getValue().size());
                found506155 = true;
            }
        }
        if (!found506155) {
            log.warn("【调试】506155 不在任何控制层级的子集中");
        }

        // 第四步：对于所有子集的ehrCd（包含所有节点），批量查询获取对应的orgCd
        Set<String> allChildrenCodes = parentToChildrenMap.values().stream()
                .flatMap(Set::stream)
                .collect(Collectors.toSet());

        // 分批查询，避免Oracle IN子句超过1000个的限制（只查询year=2026的数据）
        List<EhrOrgManageR> childrenList = new ArrayList<>();
        List<String> childrenCodeList = new ArrayList<>(allChildrenCodes);
        for (int i = 0; i < childrenCodeList.size(); i += batchSize) {
            int end = Math.min(i + batchSize, childrenCodeList.size());
            List<String> batch = childrenCodeList.subList(i, end);
            List<EhrOrgManageR> batchList = ehrOrgManageRMapper.selectList(
                    new LambdaQueryWrapper<EhrOrgManageR>()
                            .in(EhrOrgManageR::getEhrCd, batch)
                            .eq(EhrOrgManageR::getDeleted, false)
                            .eq(EhrOrgManageR::getYear, "2026")
            );
            childrenList.addAll(batchList);
        }

        // Map<EHR_CD, List<ORG_CD>> - 每个ehr_cd在EHR_ORG_MANAGE_R表中对应的所有orgCd（用于检查是否只对应一个ORG_CD）
        Map<String, List<String>> ehrCdToOrgCodesMap = new HashMap<>();
        for (EhrOrgManageR ehrOrgManageR : childrenList) {
            String ehrCd = ehrOrgManageR.getEhrCd();
            String orgCd = ehrOrgManageR.getOrgCd();
            if (StringUtils.isNotBlank(ehrCd) && StringUtils.isNotBlank(orgCd)) {
                ehrCdToOrgCodesMap.computeIfAbsent(ehrCd, k -> new ArrayList<>()).add(orgCd);
            }
        }
        
        // 对每个ehr_cd的orgCd列表去重
        Map<String, List<String>> ehrCdToUniqueOrgCodesMap = new HashMap<>();
        for (Map.Entry<String, List<String>> entry : ehrCdToOrgCodesMap.entrySet()) {
            List<String> uniqueOrgCodes = entry.getValue().stream()
                    .distinct()
                    .collect(Collectors.toList());
            ehrCdToUniqueOrgCodesMap.put(entry.getKey(), uniqueOrgCodes);
        }

        log.info("EHR_CD到ORG_CD映射（用于检查）: {}", ehrCdToUniqueOrgCodesMap);
        
        // 特别检查 506155 的 ORG_CD 映射
        if (ehrCdToUniqueOrgCodesMap.containsKey("506155")) {
            log.info("【调试】506155 在EHR_CD到ORG_CD映射中，ORG_CD列表: {}", ehrCdToUniqueOrgCodesMap.get("506155"));
        } else {
            log.warn("【调试】506155 不在EHR_CD到ORG_CD映射中（可能不在任何控制层级的子集中）");
        }

        // 第五步：收集控制层级下所有能找到ORG_CD的节点（包括向上查找找到的）
        // Map<父级EHR_CD, Set<ORG_CD>> - 父级控制层级下所有能找到的ORG_CD（去重后）
        Map<String, Set<String>> parentToOrgCodesSetMap = new HashMap<>();
        for (Map.Entry<String, Set<String>> entry : parentToChildrenMap.entrySet()) {
            String parentCode = entry.getKey();
            Set<String> childrenSet = entry.getValue();
            Set<String> orgCodesSet = new HashSet<>();
            
            log.debug("处理控制层级 {}，包含 {} 个子节点", parentCode, childrenSet.size());
            
            // 对于控制层级下的每个节点，向上查找（包括节点本身）找到有 ORG_CD 的节点
            // 收集所有找到的 ORG_CD（去重）
            for (String ehrCd : childrenSet) {
                // 先检查该ehr_cd在EHR_ORG_MANAGE_R表中是否只对应一个ORG_CD（数据校验）
                List<String> nodeOrgCodes = ehrCdToUniqueOrgCodesMap.getOrDefault(ehrCd, Collections.emptyList());
                
                // 如果该ehr_cd在EHR_ORG_MANAGE_R表中对应多个ORG_CD，则是数据错误，跳过该节点
                if (nodeOrgCodes.size() > 1) {
                    log.warn("EHR_CD: {} 在EHR_ORG_MANAGE_R表中对应多个ORG_CD: {}，数据错误，跳过该节点", ehrCd, nodeOrgCodes);
                    if ("506155".equals(ehrCd)) {
                        log.warn("【调试】506155 在EHR_ORG_MANAGE_R表中对应多个ORG_CD，被跳过");
                    }
                    continue;
                }
                
                // 向上查找（包括节点本身）找到有 ORG_CD 的节点
                OrgCodeSearchResult searchResult = findOrgCodeFromParentWithPath(ehrCd, new HashSet<>());
                
                if (searchResult != null && StringUtils.isNotBlank(searchResult.getOrgCode())) {
                    // 找到 ORG_CD，加入集合（去重）
                    orgCodesSet.add(searchResult.getOrgCode());
                    log.debug("EHR_CD: {} 向上查找到 ORG_CD: {}", ehrCd, searchResult.getOrgCode());
                    if ("506155".equals(ehrCd)) {
                        log.info("【调试】506155 向上查找到 ORG_CD: {}", searchResult.getOrgCode());
                    }
                } else {
                    if ("506155".equals(ehrCd)) {
                        log.warn("【调试】506155 向上查找未找到 ORG_CD");
                    }
                }
            }
            
            parentToOrgCodesSetMap.put(parentCode, orgCodesSet);
            log.debug("控制层级 {} 收集到 {} 个ORG_CD", parentCode, orgCodesSet.size());
        }

        log.info("控制层级到ORG_CD映射: {}", parentToOrgCodesSetMap);

        // 第六步：组装最终结果 Map<EHR_CD, List<ORG_CD>>
        // 对于每个父级控制层级下的每个节点，共享父级控制层级下所有能找到的ORG_CD列表
        Map<String, List<String>> finalResultMap = new HashMap<>();
        for (Map.Entry<String, Set<String>> entry : parentToChildrenMap.entrySet()) {
            String parentCode = entry.getKey();
            Set<String> childrenSet = entry.getValue();
            Set<String> orgCodesSet = parentToOrgCodesSetMap.getOrDefault(parentCode, Collections.emptySet());
            List<String> orgCodes = new ArrayList<>(orgCodesSet);
            
            // 如果控制层级下没有找到任何ORG_CD，跳过该控制层级的所有节点
            if (orgCodes.isEmpty()) {
                log.debug("控制层级 {} 下没有找到任何ORG_CD，跳过所有节点", parentCode);
                if (childrenSet.contains("506155")) {
                    log.warn("【调试】506155 所在的控制层级 {} 下没有找到任何ORG_CD，被跳过", parentCode);
                }
                continue;
            }
            
            // 对于每个子节点，检查其在EHR_ORG_MANAGE_R表中是否只对应一个ORG_CD
            for (String ehrCd : childrenSet) {
                List<String> nodeOrgCodes = ehrCdToUniqueOrgCodesMap.getOrDefault(ehrCd, Collections.emptyList());
                
                // 如果该ehr_cd在EHR_ORG_MANAGE_R表中对应多个ORG_CD，则是数据错误，不加入finalResultMap
                if (nodeOrgCodes.size() > 1) {
                    log.warn("EHR_CD: {} 在EHR_ORG_MANAGE_R表中对应多个ORG_CD: {}，数据错误，不加入EHR_ORG_MANAGE_EXT_R表", ehrCd, nodeOrgCodes);
                    if ("506155".equals(ehrCd)) {
                        log.warn("【调试】506155 在EHR_ORG_MANAGE_R表中对应多个ORG_CD，不加入EHR_ORG_MANAGE_EXT_R表");
                    }
                    continue;
                }
                
                // 共享父级的ORG_CD列表
                // 如果finalResultMap中已存在该ehrCd，说明它属于多个控制层级，记录警告并使用新的列表
                if (finalResultMap.containsKey(ehrCd)) {
                    List<String> existingOrgCodes = finalResultMap.get(ehrCd);
                    if (!existingOrgCodes.equals(orgCodes)) {
                        log.warn("EHR_CD: {} 属于多个控制层级，之前的ORG_CD列表: {}，当前控制层级{}的ORG_CD列表: {}，使用当前列表覆盖", 
                                ehrCd, existingOrgCodes, parentCode, orgCodes);
                    }
                }
                
                finalResultMap.put(ehrCd, orgCodes);
                if ("506155".equals(ehrCd)) {
                    log.info("【调试】506155 已加入 finalResultMap，ORG_CD列表: {}", orgCodes);
                }
            }
        }
        
        // 第六步（续）：对于找不到CONTROL_LEVEL=1父级的EHR编码，映射到NAN
        // 参考SubjectExtInfoServiceImpl的处理方式，将找不到父级的编码映射到NAN
        // 同时，也要找出这些编码的所有子集（所有节点），并将它们也映射到NAN
        // 但需要检查每个ehr_cd是否只对应一个ORG_CD，如果对应多个ORG_CD，则不加入
        List<String> nanList = Collections.singletonList("NAN");
        for (String ehrCode : ehrCodesWithoutControlLevelOne) {
            // 检查该编码是否只对应一个ORG_CD
            List<String> orgCodes = ehrCdToUniqueOrgCodesMap.getOrDefault(ehrCode, Collections.emptyList());
            if (orgCodes.size() > 1) {
                log.warn("找不到CONTROL_LEVEL=1父级的EHR编码 {} 对应多个ORG_CD: {}，数据错误，不加入EHR_ORG_MANAGE_EXT_R表", ehrCode, orgCodes);
            } else {
                finalResultMap.put(ehrCode, nanList);
                log.info("找不到CONTROL_LEVEL=1父级的EHR编码 {} 映射到NAN", ehrCode);
            }
            
            // 找出该编码的所有子集（所有节点），并将它们也映射到NAN
            Set<String> childrenSet = findAllChildren(ehrCode, new HashSet<>());
            // 该编码本身已经通过findAllChildren包含在childrenSet中（如果它有子节点），或者需要单独添加（如果它没有子节点）
            // 但由于findAllChildren只添加子节点，不添加父节点本身，所以需要检查是否需要添加
            if (!childrenSet.contains(ehrCode)) {
                childrenSet.add(ehrCode);
            }
            
            // 将所有子集也映射到NAN，但需要检查每个ehr_cd是否只对应一个ORG_CD
            for (String childEhrCd : childrenSet) {
                if (!finalResultMap.containsKey(childEhrCd)) {
                    // 检查该子节点是否只对应一个ORG_CD
                    List<String> childOrgCodes = ehrCdToUniqueOrgCodesMap.getOrDefault(childEhrCd, Collections.emptyList());
                    if (childOrgCodes.size() > 1) {
                        log.warn("找不到CONTROL_LEVEL=1父级的EHR编码 {} 的子集 {} 对应多个ORG_CD: {}，数据错误，不加入EHR_ORG_MANAGE_EXT_R表", 
                                ehrCode, childEhrCd, childOrgCodes);
                    } else {
                        finalResultMap.put(childEhrCd, nanList);
                        log.debug("找不到CONTROL_LEVEL=1父级的EHR编码 {} 的子集 {} 也映射到NAN", ehrCode, childEhrCd);
                    }
                }
            }
            
            if (!childrenSet.isEmpty()) {
                log.info("找不到CONTROL_LEVEL=1父级的EHR编码 {} 有 {} 个子集（所有节点）也被映射到NAN", ehrCode, childrenSet.size());
            }
        }

        log.info("最终结果映射: {}", finalResultMap);
        
        // 特别检查 506155 是否在最终结果映射中
        if (finalResultMap.containsKey("506155")) {
            log.info("【调试】506155 在最终结果映射中，ORG_CD列表: {}", finalResultMap.get("506155"));
        } else {
            log.warn("【调试】506155 不在最终结果映射中，不会被写入EHR_ORG_MANAGE_EXT_R表");
        }

        // 第七步：将finalResultMap的数据写入EHR_ORG_MANAGE_EXT_R表
        // 先物理删除 finalResultMap 的 key（ehrCd）对应的所有记录
        Set<String> ehrCdsToDelete = finalResultMap.keySet();
        if (!ehrCdsToDelete.isEmpty()) {
            // 分批删除，避免Oracle IN子句超过1000个的限制
            List<String> ehrCdsList = new ArrayList<>(ehrCdsToDelete);
            int totalDeletedCount = 0;
            for (int i = 0; i < ehrCdsList.size(); i += batchSize) {
                int end = Math.min(i + batchSize, ehrCdsList.size());
                List<String> batch = ehrCdsList.subList(i, end);
                int deletedCount = ehrOrgManageExtRMapper.deleteByEhrCds(batch);
                totalDeletedCount += deletedCount;
            }
            log.info("物理删除 EHR_ORG_MANAGE_EXT_R 表记录: {} 条，涉及 {} 个 EHR_CD", totalDeletedCount, ehrCdsToDelete.size());
        }

        // 将 finalResultMap 铺开成 EhrOrgManageExtR 列表（ehrCd 对每个 orgCode 创建一条记录）
        // 在写入之前，再次检查每个ehr_cd在EHR_ORG_MANAGE_R表中是否只对应一个ORG_CD
        List<EhrOrgManageExtR> insertList = new ArrayList<>();
        // 用于去重，避免重复添加相同的(ehrCd, orgCd)组合
        Set<String> processedKeys = new HashSet<>();
        
        for (Map.Entry<String, List<String>> entry : finalResultMap.entrySet()) {
            String ehrCd = entry.getKey();
            List<String> orgCodes = entry.getValue();

            // 再次检查该ehr_cd在EHR_ORG_MANAGE_R表中是否只对应一个ORG_CD（确保数据准确性）
            List<String> dbOrgCodes = ehrCdToUniqueOrgCodesMap.getOrDefault(ehrCd, Collections.emptyList());
            if (dbOrgCodes.size() > 1) {
                log.warn("EHR_CD: {} 在EHR_ORG_MANAGE_R表中对应多个ORG_CD: {}，数据错误，不加入EHR_ORG_MANAGE_EXT_R表", ehrCd, dbOrgCodes);
                if ("506155".equals(ehrCd)) {
                    log.warn("【调试】506155 在最终写入前检查时发现对应多个ORG_CD，被跳过");
                }
                continue;
            }

            // orgCodes 应该不为空（因为只有向上查找到ORG_CD的节点才会被加入finalResultMap）
            // 但为了代码健壮性，仍然进行空值检查
            if (orgCodes == null || orgCodes.isEmpty()) {
                log.warn("EHR_CD: {} 的ORG_CD列表为空，这不应该发生，跳过", ehrCd);
                if ("506155".equals(ehrCd)) {
                    log.warn("【调试】506155 的ORG_CD列表为空，被跳过");
                }
                continue;
            }
            
            if ("506155".equals(ehrCd)) {
                log.info("【调试】506155 准备写入EHR_ORG_MANAGE_EXT_R表，ORG_CD列表: {}", orgCodes);
            }

            // 为每个ORG_CD创建一条记录（理论上每个ehrCd只对应一个ORG_CD，因为是通过向上查找找到的）
            for (String orgCd : orgCodes) {
                if (StringUtils.isBlank(orgCd)) {
                    log.warn("EHR_CD: {} 的ORG_CD为空，跳过", ehrCd);
                    continue;
                }

                String key = ehrCd + "|" + orgCd;
                if (processedKeys.contains(key)) {
                    continue; // 已经处理过，跳过
                }
                processedKeys.add(key);

                // 创建新记录
                EhrOrgManageExtR entity = EhrOrgManageExtR.builder()
                        .id(identifierGenerator.nextId(null).longValue())
                        .ehrCd(ehrCd)
                        .orgCd(orgCd)
                        .build();
                entity.setDeleted(false);

                insertList.add(entity);
            }
        }

        // 批量插入
        int insertCount = 0;
        if (!insertList.isEmpty()) {
            try {
                Boolean result = ehrOrgManageExtRMapper.insertBatch(insertList);
                if (Boolean.TRUE.equals(result)) {
                    insertCount = insertList.size();
                    log.info("成功批量插入 {} 条数据到EHR_ORG_MANAGE_EXT_R表", insertCount);
                } else {
                    log.warn("批量插入返回失败，实际插入数量可能少于预期");
                }
            } catch (Exception e) {
                log.error("批量插入数据失败", e);
                // 如果批量插入失败，尝试逐条插入以获取更详细的错误信息
                for (EhrOrgManageExtR entity : insertList) {
                    try {
                        int result = ehrOrgManageExtRMapper.insert(entity);
                        if (result > 0) {
                            insertCount++;
                        }
                    } catch (Exception ex) {
                        log.error("插入数据失败，EHR_CD: {}, ORG_CD: {}", entity.getEhrCd(), entity.getOrgCd(), ex);
                    }
                }
                log.info("逐条插入完成，成功插入 {} 条数据到EHR_ORG_MANAGE_EXT_R表", insertCount);
            }
        } else {
            log.info("没有需要插入的新数据");
        }

        return String.format("同步成功，处理了 %d 个EHR编码，成功插入 %d 条数据", ehrCodes.size(), insertCount);
    }

    /**
     * 向上追溯找到CONTROL_LEVEL=1的父级
     *
     * @param ehrParCd 当前EHR父级编码
     * @param visited  已访问的节点，防止循环
     * @return CONTROL_LEVEL=1的父级EHR_CD，如果找不到返回null
     */
    private String findControlLevelOneParent(String ehrParCd, Set<String> visited) {
        if (StringUtils.isBlank(ehrParCd)) {
            return null;
        }

        // 防止循环
        if (visited.contains(ehrParCd)) {
            log.warn("检测到循环引用，EHR_PAR_CD: {}", ehrParCd);
            return null;
        }
        visited.add(ehrParCd);

        // 查询父级记录（添加 year=2026 条件，避免同一 ehrCd 有多条记录导致 TooManyResultsException）
        // 使用 selectList 然后取第一条（按 ID 降序，取最新的记录），避免 TooManyResultsException
        List<EhrOrgManageR> parentList = ehrOrgManageRMapper.selectList(
                new LambdaQueryWrapper<EhrOrgManageR>()
                        .eq(EhrOrgManageR::getEhrCd, ehrParCd)
                        .eq(EhrOrgManageR::getDeleted, false)
                        .eq(EhrOrgManageR::getYear, "2026")
                        .orderByDesc(EhrOrgManageR::getId)
        );
        EhrOrgManageR parent = parentList.isEmpty() ? null : parentList.get(0);

        if (parent == null) {
            log.warn("未找到EHR父级数据，EHR_PAR_CD: {}", ehrParCd);
            return null;
        }

        // 如果父级的CONTROL_LEVEL=1，返回父级的EHR_CD
        if ("1".equals(parent.getControlLevel())) {
            return parent.getEhrCd();
        }

        // 继续向上追溯
        return findControlLevelOneParent(parent.getEhrParCd(), visited);
    }

    /**
     * 递归查找所有子集（包括子集的子集）
     * 添加所有节点（不仅仅是叶子节点）到结果集中
     * 注意：如果遇到CONTROL_LEVEL=1的节点，停止递归，因为它的子节点属于另一个控制层级
     *
     * @param parentEhrCd 父级EHR_CD
     * @param result      结果集合
     * @return 所有子集的EHR_CD集合（包含所有节点）
     */
    private Set<String> findAllChildren(String parentEhrCd, Set<String> result) {
        if (StringUtils.isBlank(parentEhrCd)) {
            return result;
        }

        // 查询直接子集（添加 year=2026 条件，确保只查询2026年的数据）
        List<EhrOrgManageR> children = ehrOrgManageRMapper.selectList(
                new LambdaQueryWrapper<EhrOrgManageR>()
                        .eq(EhrOrgManageR::getEhrParCd, parentEhrCd)
                        .eq(EhrOrgManageR::getDeleted, false)
                        .eq(EhrOrgManageR::getYear, "2026")
        );

        for (EhrOrgManageR child : children) {
            String childEhrCd = child.getEhrCd();
            if (StringUtils.isBlank(childEhrCd)) {
                continue;
            }
            
            // 添加所有节点（不仅仅是叶子节点）到结果集中
            if (!result.contains(childEhrCd)) {
                result.add(childEhrCd);
            }
            
            // 如果子节点是CONTROL_LEVEL=1，停止递归，因为它的子节点属于另一个控制层级
            if ("1".equals(child.getControlLevel())) {
                continue;
            }
            
            // 继续递归查找子集的子集
            findAllChildren(childEhrCd, result);
        }

        return result;
    }
    
    /**
     * 判断指定 EHR_CD 是否是叶子节点（没有子节点）
     * 判断方式：用ehrCode作为ehrParCd去查询是否有子集，如果没有子集，就是叶子节点
     *
     * @param ehrCd EHR编码
     * @return true 如果是叶子节点，false 如果不是
     */
    private boolean isLeafNode(String ehrCd) {
        if (StringUtils.isBlank(ehrCd)) {
            return false;
        }
        
        // 查询是否有子节点（用ehrCd作为ehrParCd去查询，添加 year=2026 条件）
        Long count = ehrOrgManageRMapper.selectCount(
                new LambdaQueryWrapper<EhrOrgManageR>()
                        .eq(EhrOrgManageR::getEhrParCd, ehrCd)
                        .eq(EhrOrgManageR::getDeleted, false)
                        .eq(EhrOrgManageR::getYear, "2026")
        );
        
        // 如果没有子节点，则是叶子节点
        return count == null || count == 0;
    }

    /**
     * 向上查找父级获取orgCode的结果类
     */
    private static class OrgCodeSearchResult {
        private final String orgCode;
        private final List<String> path; // 从当前节点到找到orgCode的父级之间的路径（不包含当前节点）

        public OrgCodeSearchResult(String orgCode, List<String> path) {
            this.orgCode = orgCode;
            this.path = path;
        }

        public String getOrgCode() {
            return orgCode;
        }

        public List<String> getPath() {
            return path;
        }
    }

    /**
     * 向上查找父级获取orgCode（带路径信息）
     * 从当前ehrCode开始，向上查找parent，如果找到有orgCode的父级就返回那个orgCode和路径
     * 如果没有，就一直向上查找，直到找到父级为控制层级（ControlLevel为1）的数据
     * 如果都没有orgCode，返回null
     *
     * @param ehrCd   当前EHR编码
     * @param visited 已访问的节点，防止循环
     * @return 查找结果，包含orgCode和路径，如果找不到返回null
     */
    private OrgCodeSearchResult findOrgCodeFromParentWithPath(String ehrCd, Set<String> visited) {
        if (StringUtils.isBlank(ehrCd)) {
            return null;
        }

        // 防止循环
        if (visited.contains(ehrCd)) {
            log.warn("检测到循环引用，EHR_CD: {}", ehrCd);
            return null;
        }
        visited.add(ehrCd);

        // 查询当前记录（添加 year=2026 条件，避免同一 ehrCd 有多条记录导致 TooManyResultsException）
        // 使用 selectList 然后取第一条（按 ID 降序，取最新的记录），避免 TooManyResultsException
        List<EhrOrgManageR> currentList = ehrOrgManageRMapper.selectList(
                new LambdaQueryWrapper<EhrOrgManageR>()
                        .eq(EhrOrgManageR::getEhrCd, ehrCd)
                        .eq(EhrOrgManageR::getDeleted, false)
                        .eq(EhrOrgManageR::getYear, "2026")
                        .orderByDesc(EhrOrgManageR::getId)
        );
        EhrOrgManageR current = currentList.isEmpty() ? null : currentList.get(0);

        if (current == null) {
            log.warn("未找到EHR数据，EHR_CD: {}", ehrCd);
            return null;
        }

        // 如果当前记录有orgCode，将当前节点也包含在路径中（因为需要共享orgCode）
        if (StringUtils.isNotBlank(current.getOrgCd())) {
            List<String> path = new ArrayList<>();
            path.add(ehrCd); // 将找到orgCode的节点本身也包含在路径中
            return new OrgCodeSearchResult(current.getOrgCd(), path);
        }

        // 如果当前是控制层级（ControlLevel为1），检查是否有orgCode，没有则返回null
        if ("1".equals(current.getControlLevel())) {
            log.debug("EHR_CD: {} 是控制层级但无ORG_CD", ehrCd);
            return null;
        }

        // 继续向上查找父级
        if (StringUtils.isBlank(current.getEhrParCd())) {
            log.debug("EHR_CD: {} 没有父级编码", ehrCd);
            return null;
        }

        // 递归查找父级
        OrgCodeSearchResult parentResult = findOrgCodeFromParentWithPath(current.getEhrParCd(), visited);
        if (parentResult != null) {
            // 将当前ehrCd添加到路径中
            List<String> path = new ArrayList<>(parentResult.getPath());
            path.add(ehrCd);
            return new OrgCodeSearchResult(parentResult.getOrgCode(), path);
        }

        return null;
    }

    /**
     * 向上查找父级获取orgCode
     * 从当前ehrCode开始，向上查找parent，如果找到有orgCode的父级就返回那个orgCode
     * 如果没有，就一直向上查找，直到找到父级为控制层级（ControlLevel为1）的数据
     * 如果都没有orgCode，返回null
     *
     * @param ehrCd   当前EHR编码
     * @param visited 已访问的节点，防止循环
     * @return 找到的orgCode，如果找不到返回null
     */
    private String findOrgCodeFromParent(String ehrCd, Set<String> visited) {
        OrgCodeSearchResult result = findOrgCodeFromParentWithPath(ehrCd, visited);
        return result != null ? result.getOrgCode() : null;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String synEhrManageOneRData() {
        log.info("开始同步EHR管理组织一对一关系数据（year=2026）");
        
        // 第一步：查询 EHR_ORG_MANAGE_R 表，获取所有未删除且 year=2026 的数据
        List<EhrOrgManageR> allEhrOrgManageRList = ehrOrgManageRMapper.selectList(
                new LambdaQueryWrapper<EhrOrgManageR>()
                        .eq(EhrOrgManageR::getDeleted, false)
                        .eq(EhrOrgManageR::getYear, "2026")
        );
        
        log.info("查询到 {} 条 EHR_ORG_MANAGE_R 数据（year=2026）", allEhrOrgManageRList.size());
        
        if (allEhrOrgManageRList.isEmpty()) {
            return "未找到EHR组织数据";
        }
        
        // 第二步：提取所有 ehrCd，作为 ehrParCd 条件查询，找出叶子节点
        // 叶子节点：没有子节点的节点（即该 ehrCd 作为 ehrParCd 查询不到数据）
        List<EhrOrgManageR> leafNodes = new ArrayList<>();
        
        for (EhrOrgManageR ehrOrgManageR : allEhrOrgManageRList) {
            String ehrCd = ehrOrgManageR.getEhrCd();
            if (StringUtils.isBlank(ehrCd)) {
                continue;
            }
            
            // 将该 ehrCd 作为 ehrParCd 查询，看是否有子节点
            Long childCount = ehrOrgManageRMapper.selectCount(
                    new LambdaQueryWrapper<EhrOrgManageR>()
                            .eq(EhrOrgManageR::getEhrParCd, ehrCd)
                            .eq(EhrOrgManageR::getDeleted, false)
            );
            
            // 如果查不到数据（count = 0），说明是叶子节点
            if (childCount == null || childCount == 0) {
                leafNodes.add(ehrOrgManageR);
            }
        }
        
        log.info("找到 {} 个叶子节点", leafNodes.size());
        
        // 第三步：处理叶子节点，组装 EhrOrgManageOneR 数据
        List<EhrOrgManageOneR> insertList = new ArrayList<>();
        // 用于去重，避免重复添加相同的(ehrCd, orgCd)组合
        Set<String> processedKeys = new HashSet<>();
        
        // Oracle IN子句最多支持1000个表达式，定义批次大小
        final int batchSize = 1000;
        
        // 先物理删除所有旧数据
        List<EhrOrgManageOneR> allExistingList = ehrOrgManageOneRMapper.selectList(
                new LambdaQueryWrapper<EhrOrgManageOneR>()
                        .eq(EhrOrgManageOneR::getDeleted, false)
        );
        if (!allExistingList.isEmpty()) {
            List<String> existingEhrCds = allExistingList.stream()
                    .map(EhrOrgManageOneR::getEhrCd)
                    .filter(StringUtils::isNotBlank)
                    .distinct()
                    .collect(Collectors.toList());
            
            // 分批删除
            int totalDeletedCount = 0;
            for (int i = 0; i < existingEhrCds.size(); i += batchSize) {
                int end = Math.min(i + batchSize, existingEhrCds.size());
                List<String> batch = existingEhrCds.subList(i, end);
                // 物理删除
                for (String ehrCd : batch) {
                    ehrOrgManageOneRMapper.delete(
                            new LambdaQueryWrapper<EhrOrgManageOneR>()
                                    .eq(EhrOrgManageOneR::getEhrCd, ehrCd)
                                    .eq(EhrOrgManageOneR::getDeleted, false)
                    );
                }
                totalDeletedCount += batch.size();
            }
            log.info("物理删除 EHR_ORG_MANAGE_ONE_R 表记录: {} 条", totalDeletedCount);
        }
        
        for (EhrOrgManageR leafNode : leafNodes) {
            String ehrCd = leafNode.getEhrCd();
            if (StringUtils.isBlank(ehrCd)) {
                continue;
            }
            
            String orgCd = leafNode.getOrgCd();
            
            // 如果 orgCode 不为空，直接组装
            if (StringUtils.isNotBlank(orgCd)) {
                String key = ehrCd + "|" + orgCd;
                if (!processedKeys.contains(key)) {
                    processedKeys.add(key);
                    EhrOrgManageOneR entity = EhrOrgManageOneR.builder()
                            .id(identifierGenerator.nextId(null).longValue())
                            .ehrCd(ehrCd)
                            .orgCd(orgCd)
                            .build();
                    entity.setDeleted(false);
                    insertList.add(entity);
                }
            } else {
                // 如果 orgCode 为空，向上追溯父级
                log.debug("叶子节点 EHR_CD: {} 没有ORG_CD，尝试向上查找父级", ehrCd);
                OrgCodeSearchResult searchResult = findOrgCodeFromParentWithPath(ehrCd, new HashSet<>());
                
                if (searchResult != null && StringUtils.isNotBlank(searchResult.getOrgCode())) {
                    // 为路径上的所有ehrCode创建记录（路径已包含从当前ehrCd到找到orgCode的父级之间的所有节点，包括找到orgCode的节点本身）
                    for (String pathEhrCd : searchResult.getPath()) {
                        String key = pathEhrCd + "|" + searchResult.getOrgCode();
                        if (!processedKeys.contains(key)) {
                            processedKeys.add(key);
                            EhrOrgManageOneR entity = EhrOrgManageOneR.builder()
                                    .id(identifierGenerator.nextId(null).longValue())
                                    .ehrCd(pathEhrCd)
                                    .orgCd(searchResult.getOrgCode())
                                    .build();
                            entity.setDeleted(false);
                            insertList.add(entity);
                            log.debug("为路径上的EHR_CD: {} 创建记录，ORG_CD: {}", pathEhrCd, searchResult.getOrgCode());
                        }
                    }
                } else {
                    log.debug("叶子节点 EHR_CD: {} 向上查找父级后仍未找到ORG_CD，跳过", ehrCd);
                }
            }
        }
        
        // 批量插入
        int insertCount = 0;
        if (!insertList.isEmpty()) {
            try {
                // 分批插入，避免Oracle IN子句超过1000个的限制
                for (int i = 0; i < insertList.size(); i += batchSize) {
                    int end = Math.min(i + batchSize, insertList.size());
                    List<EhrOrgManageOneR> batch = insertList.subList(i, end);
                    for (EhrOrgManageOneR entity : batch) {
                        ehrOrgManageOneRMapper.insert(entity);
                        insertCount++;
                    }
                }
                log.info("成功批量插入 {} 条数据到EHR_ORG_MANAGE_ONE_R表", insertCount);
            } catch (Exception e) {
                log.error("批量插入数据失败", e);
                throw e;
            }
        } else {
            log.info("没有需要插入的新数据");
        }
        
        return String.format("同步成功，处理了 %d 个叶子节点，成功插入 %d 条数据", leafNodes.size(), insertCount);
    }
}

