package com.jasolar.mis.module.system.service.ehr;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.jasolar.mis.module.system.domain.ehr.SubjectExtInfo;
import com.jasolar.mis.module.system.domain.ehr.SubjectInfo;
import com.jasolar.mis.module.system.mapper.ehr.SubjectExtInfoMapper;
import com.jasolar.mis.module.system.mapper.ehr.SubjectInfoMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 科目扩展信息 Service 实现类
 */
@Slf4j
@Service
public class SubjectExtInfoServiceImpl implements SubjectExtInfoService {

    @Autowired
    private SubjectInfoMapper subjectInfoMapper;

    @Autowired
    private SubjectExtInfoMapper subjectExtInfoMapper;

    @Override
    public String syncSubjectInfoData(List<SubjectInfo> subjectInfoList) {
        if (CollectionUtils.isEmpty(subjectInfoList)) {
            log.warn("科目信息列表为空，无需同步");
            return "科目信息列表为空";
        }

        log.info("开始同步科目信息数据，科目数量: {}", subjectInfoList.size());
        
        // 检查目标ERP_ACCT_CD=6602010304是否在传入的subjectInfoList中
        List<SubjectInfo> input6602010304List = subjectInfoList.stream()
                .filter(item -> "6602010304".equals(item.getErpAcctCd()))
                .collect(Collectors.toList());
        if (!input6602010304List.isEmpty()) {
            log.info("【6602010304调试】传入的subjectInfoList中找到ERP_ACCT_CD=6602010304的记录，共 {} 条: {}", 
                    input6602010304List.size(),
                    input6602010304List.stream()
                            .map(item -> String.format("CUST1_CD=%s, ACCT_CD=%s, ERP_ACCT_CD=%s, CONTROL_LEVEL=%s, ACCT_PAR_CD=%s",
                                    item.getCust1Cd(), item.getAcctCd(), item.getErpAcctCd(), 
                                    item.getControlLevel(), item.getAcctParCd()))
                            .collect(Collectors.joining("; ")));
        } else {
            log.warn("【6602010304调试】传入的subjectInfoList中未找到ERP_ACCT_CD=6602010304的记录！");
        }

        // 第一步：使用 (cust1Cd, acctCd) 组合批量查询SUBJECT_INFO表（不限制controlLevel）
        // 这样可以确保唯一性，避免同一个acctCd对应多条记录的问题
        List<SubjectInfo> queriedSubjectInfoList = subjectInfoMapper.selectByCust1CdAndAcctCdListWithoutControlLevel(subjectInfoList);

        if (CollectionUtils.isEmpty(queriedSubjectInfoList)) {
            log.warn("未找到对应的科目信息数据");
            return "未找到对应的科目信息数据";
        }
        
        // 检查目标ERP_ACCT_CD=6602010304是否在查询结果中
        List<SubjectInfo> target6602010304List = queriedSubjectInfoList.stream()
                .filter(item -> "6602010304".equals(item.getErpAcctCd()))
                .collect(Collectors.toList());
        if (!target6602010304List.isEmpty()) {
            log.info("【6602010304调试】第一步查询中找到ERP_ACCT_CD=6602010304的记录，共 {} 条: {}", 
                    target6602010304List.size(),
                    target6602010304List.stream()
                            .map(item -> String.format("CUST1_CD=%s, ACCT_CD=%s, ERP_ACCT_CD=%s, CONTROL_LEVEL=%s, ACCT_PAR_CD=%s",
                                    item.getCust1Cd(), item.getAcctCd(), item.getErpAcctCd(), 
                                    item.getControlLevel(), item.getAcctParCd()))
                            .collect(Collectors.joining("; ")));
        } else {
            log.warn("【6602010304调试】第一步查询中未找到ERP_ACCT_CD=6602010304的记录！");
            // 检查传入的subjectInfoList中是否有6602010304
            if (!input6602010304List.isEmpty()) {
                log.warn("【6602010304调试】传入的subjectInfoList中有ERP_ACCT_CD=6602010304的记录，但查询结果中没有，可能cust1Cd或acctCd不匹配: {}",
                        input6602010304List.stream()
                                .map(item -> String.format("CUST1_CD=%s, ACCT_CD=%s, ERP_ACCT_CD=%s",
                                        item.getCust1Cd(), item.getAcctCd(), item.getErpAcctCd()))
                                .collect(Collectors.joining("; ")));
            }
        }

        // 构建 (cust1Cd + "-" + acctCd) 到 SubjectInfo 的映射，确保唯一性
        // 如果同一个key对应多条记录，优先选择CONTROL_LEVEL=1的记录，如果都没有CONTROL_LEVEL=1，则取第一条
        Map<String, SubjectInfo> keyToSubjectMap = queriedSubjectInfoList.stream()
                .collect(Collectors.toMap(
                        subject -> buildKey(subject.getCust1Cd(), subject.getAcctCd()),
                        subject -> subject,
                        (existing, replacement) -> {
                            String key = buildKey(existing.getCust1Cd(), existing.getAcctCd());
                            // 优先选择CONTROL_LEVEL=1的记录
                            boolean existingIsControlLevelOne = "1".equals(existing.getControlLevel());
                            boolean replacementIsControlLevelOne = "1".equals(replacement.getControlLevel());
                            
                            if (replacementIsControlLevelOne && !existingIsControlLevelOne) {
                                return replacement;
                            } else if (existingIsControlLevelOne && !replacementIsControlLevelOne) {
                                return existing;
                            } else {
                                // 如果都是或都不是CONTROL_LEVEL=1，优先选择ERP_ACCT_CD不为空的记录
                                // 但如果existing和replacement都有ERP_ACCT_CD，需要合并处理（因为一个key可能对应多个ERP_ACCT_CD）
                                boolean existingHasErpAcctCd = existing.getErpAcctCd() != null && !existing.getErpAcctCd().trim().isEmpty();
                                boolean replacementHasErpAcctCd = replacement.getErpAcctCd() != null && !replacement.getErpAcctCd().trim().isEmpty();
                                
                                if (replacementHasErpAcctCd && !existingHasErpAcctCd) {
                                    // replacement有ERP_ACCT_CD，existing没有，选择replacement
                                    return replacement;
                                } else if (existingHasErpAcctCd && !replacementHasErpAcctCd) {
                                    // existing有ERP_ACCT_CD，replacement没有，保留existing
                                    return existing;
                                } else if (existingHasErpAcctCd && replacementHasErpAcctCd) {
                                    // 都有ERP_ACCT_CD，优先选择CONTROL_LEVEL不为null的，如果都为null，保留existing
                                    boolean existingHasControlLevel = existing.getControlLevel() != null && !existing.getControlLevel().trim().isEmpty();
                                    boolean replacementHasControlLevel = replacement.getControlLevel() != null && !replacement.getControlLevel().trim().isEmpty();
                                    
                                    if (replacementHasControlLevel && !existingHasControlLevel) {
                                        return replacement;
                                    } else {
                                        // 保留existing（如果replacement的CONTROL_LEVEL为null，或者existing的CONTROL_LEVEL不为null）
                                        return existing;
                                    }
                                } else {
                                    // 都没有ERP_ACCT_CD，保留第一条（existing）
                                    return existing;
                                }
                            }
                        }
                ));
        
        // 检查keyToSubjectMap中6602010304的记录
        SubjectInfo target6602010304InMap = keyToSubjectMap.values().stream()
                .filter(item -> "6602010304".equals(item.getErpAcctCd()))
                .findFirst()
                .orElse(null);
        if (target6602010304InMap != null) {
            log.info("【6602010304调试】keyToSubjectMap中找到ERP_ACCT_CD=6602010304的记录: KEY={}, CUST1_CD={}, ACCT_CD={}, ERP_ACCT_CD={}, CONTROL_LEVEL={}, ACCT_PAR_CD={}",
                    buildKey(target6602010304InMap.getCust1Cd(), target6602010304InMap.getAcctCd()),
                    target6602010304InMap.getCust1Cd(), target6602010304InMap.getAcctCd(),
                    target6602010304InMap.getErpAcctCd(), target6602010304InMap.getControlLevel(),
                    target6602010304InMap.getAcctParCd());
        } else {
            log.warn("【6602010304调试】keyToSubjectMap中未找到ERP_ACCT_CD=6602010304的记录！");
        }

        // 第二步：对于每个科目，判断其controlLevel，如果是1则直接使用，否则向上追溯
        // 使用 cust1Cd + "-" + acctCd 作为 key，确保唯一性
        Map<String, String> keyToControlLevelOneKeyMap = new HashMap<>();
        // 存储找不到controlLevel=1父级的科目，这些科目的ERP_ACCT_CD需要映射到NAN-NAN
        Set<String> keysWithoutControlLevelOne = new HashSet<>();

        for (SubjectInfo subject : subjectInfoList) {
            String key = buildKey(subject.getCust1Cd(), subject.getAcctCd());
            SubjectInfo current = keyToSubjectMap.get(key);
            if (current == null) {
                throw new RuntimeException("未找到科目对应的数据: " + key);
            }

            String currentAcctCd = current.getAcctCd();
            if (!StringUtils.hasText(currentAcctCd)) {
                throw new RuntimeException("科目 " + key + " 对应的ACCT_CD为空");
            }
            
            // 特别检查目标ERP_ACCT_CD=6602010304
            boolean isTarget6602010304 = "6602010304".equals(current.getErpAcctCd());
            if (isTarget6602010304) {
                log.info("【6602010304调试】处理ERP_ACCT_CD=6602010304: KEY={}, ACCT_CD={}, CONTROL_LEVEL={}, ACCT_PAR_CD={}",
                        key, currentAcctCd, current.getControlLevel(), current.getAcctParCd());
            }

            // 判断当前记录的controlLevel
            if ("1".equals(current.getControlLevel())) {
                // 如果当前记录就是controlLevel=1，直接使用自己作为父级
                keyToControlLevelOneKeyMap.put(key, buildKey(current.getCust1Cd(), currentAcctCd));
                if (isTarget6602010304) {
                    log.info("【6602010304调试】ERP_ACCT_CD=6602010304 的controlLevel=1，直接使用自己作为父级");
                }
            } else {
                // 如果当前记录的controlLevel不是1，需要向上追溯找到controlLevel=1的父级
                String controlLevelOneAcctCd = findControlLevelOneParent(current.getAcctParCd(), current.getCust1Cd(), new HashSet<>());
                if (controlLevelOneAcctCd != null) {
                    // 找到了controlLevel=1的父级，使用父级作为key
                    String controlLevelOneKey = buildKey(current.getCust1Cd(), controlLevelOneAcctCd);
                    keyToControlLevelOneKeyMap.put(key, controlLevelOneKey);
                    if (isTarget6602010304) {
                        log.info("【6602010304调试】ERP_ACCT_CD=6602010304 向上追溯找到controlLevel=1的父级: {}", controlLevelOneKey);
                    }
                } else {
                    // 向上追溯后仍然找不到controlLevel=1的父级，标记为需要映射到NAN-NAN
                    keysWithoutControlLevelOne.add(key);
                    if (isTarget6602010304) {
                        log.warn("【6602010304调试】ERP_ACCT_CD=6602010304 向上追溯后仍然找不到controlLevel=1的父级，将映射到NAN-NAN，KEY={}", key);
                    } else {
                        log.warn("科目 {} 向上追溯后仍然找不到controlLevel=1的父级，将映射到NAN-NAN", key);
                    }
                }
            }
        }

        log.info("科目编码到控制层级为1的父级映射: {}", keyToControlLevelOneKeyMap);

        // 第三步：对于每个CONTROL_LEVEL=1的父级，找出所有LEAF=1的末级子节点
        Set<String> uniqueControlLevelOneKeys = new HashSet<>(keyToControlLevelOneKeyMap.values());
        // Map<父级KEY (cust1Cd + "-" + acctCd), 所有末级子节点KEY列表>
        Map<String, List<String>> parentToLeafChildrenMap = new HashMap<>();

        for (String parentKey : uniqueControlLevelOneKeys) {
            // 从 key 中解析出 cust1Cd 和 acctCd
            String[] parts = parentKey.split("-", 2);
            String parentCust1Cd = parts.length > 0 ? parts[0] : null;
            String parentAcctCd = parts.length > 1 ? parts[1] : null;
            
            if (!StringUtils.hasText(parentAcctCd)) {
                log.warn("父级KEY格式错误: {}", parentKey);
                continue;
            }
            
            List<String> leafChildrenKeys = findAllLeafChildren(parentAcctCd, parentCust1Cd, new HashSet<>());
            parentToLeafChildrenMap.put(parentKey, leafChildrenKeys);
        }

        log.info("父级到末级子节点映射: {}", parentToLeafChildrenMap);

        // 第四步：对于每个父级控制层级下的所有叶子节点，批量查询获取对应的ERP_ACCT_CD
        Set<String> allLeafChildrenKeys = parentToLeafChildrenMap.values().stream()
                .flatMap(List::stream)
                .collect(Collectors.toSet());

        // 从 key (cust1Cd + "-" + acctCd) 中提取 cust1Cd 和 acctCd，构建 SubjectInfo 列表用于批量查询
        List<SubjectInfo> leafChildrenForQuery = new ArrayList<>();
        for (String key : allLeafChildrenKeys) {
            String[] parts = key.split("-", 2);
            String cust1Cd = parts.length > 0 ? parts[0] : null;
            String acctCd = parts.length > 1 ? parts[1] : null;
            if (StringUtils.hasText(acctCd)) {
                SubjectInfo subject = new SubjectInfo();
                subject.setCust1Cd(cust1Cd);
                subject.setAcctCd(acctCd);
                leafChildrenForQuery.add(subject);
            }
        }

        // 批量查询所有叶子节点对应的SubjectInfo记录，获取它们的ERP_ACCT_CD
        List<SubjectInfo> leafChildrenList = new ArrayList<>();
        if (!leafChildrenForQuery.isEmpty()) {
            log.info("准备查询 {} 个叶子节点，获取ERP_ACCT_CD", leafChildrenForQuery.size());
            
            // 使用不限制 controlLevel 的查询方法，因为叶子节点的 controlLevel 可能不是 1
            // 这样可以确保所有叶子节点都能被查询到，包括 controlLevel 不是 1 的叶子节点
            leafChildrenList = subjectInfoMapper.selectByCust1CdAndAcctCdListWithoutControlLevel(leafChildrenForQuery);
            log.info("查询到 {} 个叶子节点记录（不限制 controlLevel）", leafChildrenList.size());
        }

        // Map<KEY (cust1Cd + "-" + acctCd), List<ERP_ACCT_CD>> - 一个key可能对应多个ERP_ACCT_CD
        
        Map<String, List<String>> keyToErpAcctCdsMap = leafChildrenList.stream()
                .filter(e -> StringUtils.hasText(e.getAcctCd()) && StringUtils.hasText(e.getErpAcctCd()))
                .collect(Collectors.groupingBy(
                        e -> buildKey(e.getCust1Cd(), e.getAcctCd()),
                        Collectors.mapping(
                                SubjectInfo::getErpAcctCd,
                                Collectors.toList()
                        )
                ));

        log.info("KEY到ERP_ACCT_CD映射: {}", keyToErpAcctCdsMap);

        // 第五步：构建最终的Map: 对于每个传入的科目，找到它对应的父级控制层级下所有叶子节点对应的ERP_ACCT_CD集合作为key，这些叶子节点的key集合作为value
        Map<String, List<String>> erpAcctCdToKeyListMap = new HashMap<>();
        for (Map.Entry<String, String> entry : keyToControlLevelOneKeyMap.entrySet()) {
            String key = entry.getKey();
            String controlLevelOneKey = entry.getValue();
            List<String> leafChildrenKeys = parentToLeafChildrenMap.get(controlLevelOneKey);
            if (leafChildrenKeys != null && !leafChildrenKeys.isEmpty()) {
                // 对于该父级下的所有叶子节点，找到它们对应的所有ERP_ACCT_CD
                Set<String> leafErpAcctCds = new HashSet<>();
                for (String leafKey : leafChildrenKeys) {
                    List<String> leafErpAcctCdList = keyToErpAcctCdsMap.get(leafKey);
                    if (leafErpAcctCdList != null) {
                        leafErpAcctCds.addAll(leafErpAcctCdList);
                    }
                }
                
                // 对于该父级下的每个叶子节点对应的ERP_ACCT_CD，都映射到相同的key列表
                for (String leafErpAcctCd : leafErpAcctCds) {
                    erpAcctCdToKeyListMap.put(leafErpAcctCd, leafChildrenKeys);
                }
            }
        }
        
        // 第五步（续）：对于找不到controlLevel=1父级的科目，收集它们的ERP_ACCT_CD，映射到NAN-NAN
        if (!keysWithoutControlLevelOne.isEmpty()) {
            log.info("开始处理找不到controlLevel=1父级的科目，共 {} 个", keysWithoutControlLevelOne.size());
            // 收集这些科目的所有ERP_ACCT_CD
            // 注意：同一个KEY可能对应多个ERP_ACCT_CD，需要从原始查询结果中查找所有匹配的记录
            Set<String> erpAcctCdsForNan = new HashSet<>();
            for (String key : keysWithoutControlLevelOne) {
                // 从原始查询结果中查找所有匹配的记录（同一个KEY可能对应多个ERP_ACCT_CD）
                List<SubjectInfo> allSubjects = queriedSubjectInfoList.stream()
                        .filter(item -> key.equals(buildKey(item.getCust1Cd(), item.getAcctCd())))
                        .collect(Collectors.toList());
                
                if (allSubjects.isEmpty()) {
                    // 如果原始查询结果中也没有，再从keyToSubjectMap中查找（用于调试和兼容）
                    SubjectInfo subject = keyToSubjectMap.get(key);
                    if (subject != null && StringUtils.hasText(subject.getErpAcctCd())) {
                        erpAcctCdsForNan.add(subject.getErpAcctCd());
                        boolean isTarget6602010304 = "6602010304".equals(subject.getErpAcctCd());
                        if (isTarget6602010304) {
                            log.warn("【6602010304调试】ERP_ACCT_CD=6602010304 的科目 KEY={} 将映射到NAN-NAN，CUST1_CD={}, ACCT_CD={}", 
                                    key, subject.getCust1Cd(), subject.getAcctCd());
                        } else {
                            log.info("科目 {} 的ERP_ACCT_CD={} 将映射到NAN-NAN", key, subject.getErpAcctCd());
                        }
                    } else {
                        // 检查是否是6602010304相关的KEY
                        if (key != null && (key.contains("6602010304") || key.contains("A010303010101010103"))) {
                            log.warn("【6602010304调试】keysWithoutControlLevelOne中的KEY={} 在原始查询结果和keyToSubjectMap中都找不到对应的SubjectInfo，或ERP_ACCT_CD为空", key);
                        }
                    }
                } else {
                    // 从原始查询结果中收集所有非空的ERP_ACCT_CD
                    for (SubjectInfo subject : allSubjects) {
                        if (subject != null && StringUtils.hasText(subject.getErpAcctCd())) {
                            erpAcctCdsForNan.add(subject.getErpAcctCd());
                            boolean isTarget6602010304 = "6602010304".equals(subject.getErpAcctCd());
                            if (isTarget6602010304) {
                                log.warn("【6602010304调试】ERP_ACCT_CD=6602010304 的科目 KEY={} 将映射到NAN-NAN，CUST1_CD={}, ACCT_CD={}", 
                                        key, subject.getCust1Cd(), subject.getAcctCd());
                            } else {
                                log.info("科目 {} 的ERP_ACCT_CD={} 将映射到NAN-NAN", key, subject.getErpAcctCd());
                            }
                        }
                    }
                    
                    // 如果从原始查询结果中找到了记录，但都没有ERP_ACCT_CD，记录警告
                    boolean hasErpAcctCd = allSubjects.stream()
                            .anyMatch(s -> s != null && StringUtils.hasText(s.getErpAcctCd()));
                    if (!hasErpAcctCd) {
                        if (key != null && (key.contains("6602010304") || key.contains("A010303010101010103"))) {
                            log.warn("【6602010304调试】keysWithoutControlLevelOne中的KEY={} 在原始查询结果中找到 {} 条记录，但所有记录的ERP_ACCT_CD都为空", 
                                    key, allSubjects.size());
                        }
                    }
                }
            }
            
            // 对于这些ERP_ACCT_CD，映射到NAN-NAN（使用List包含单个元素"NAN-NAN"）
            List<String> nanNanList = Collections.singletonList("NAN-NAN");
            for (String erpAcctCd : erpAcctCdsForNan) {
                erpAcctCdToKeyListMap.put(erpAcctCd, nanNanList);
                if ("6602010304".equals(erpAcctCd)) {
                    log.warn("【6602010304调试】ERP_ACCT_CD=6602010304 映射到NAN-NAN");
                } else {
                    log.info("ERP_ACCT_CD={} 映射到NAN-NAN", erpAcctCd);
                }
            }
            log.info("找不到controlLevel=1父级的科目处理完成，共 {} 个ERP_ACCT_CD映射到NAN-NAN", erpAcctCdsForNan.size());
            
            // 检查6602010304是否在NAN-NAN映射中
            if (erpAcctCdsForNan.contains("6602010304")) {
                log.warn("【6602010304调试】ERP_ACCT_CD=6602010304 被映射到NAN-NAN，原因：找不到CONTROL_LEVEL=1的父级");
            } else {
                log.warn("【6602010304调试】ERP_ACCT_CD=6602010304 未在erpAcctCdsForNan集合中，可能的原因：");
                log.warn("【6602010304调试】  1. 该科目不在keysWithoutControlLevelOne集合中");
                log.warn("【6602010304调试】  2. 该科目在原始查询结果中找不到对应的记录");
                log.warn("【6602010304调试】  3. 该科目的ERP_ACCT_CD为空");
                // 检查keysWithoutControlLevelOne中是否有相关的KEY
                boolean foundRelatedKey = keysWithoutControlLevelOne.stream()
                        .anyMatch(k -> k != null && (k.contains("6602010304") || k.contains("A010303010101010103")));
                if (foundRelatedKey) {
                    log.warn("【6602010304调试】在keysWithoutControlLevelOne中找到相关KEY，但ERP_ACCT_CD可能为空或原始查询结果中找不到记录");
                    keysWithoutControlLevelOne.stream()
                            .filter(k -> k != null && (k.contains("6602010304") || k.contains("A010303010101010103")))
                            .forEach(k -> {
                                // 从原始查询结果中查找所有匹配的记录
                                List<SubjectInfo> relatedSubjects = queriedSubjectInfoList.stream()
                                        .filter(item -> k.equals(buildKey(item.getCust1Cd(), item.getAcctCd())))
                                        .collect(Collectors.toList());
                                
                                if (!relatedSubjects.isEmpty()) {
                                    log.warn("【6602010304调试】相关KEY={}, 在原始查询结果中找到 {} 条记录:", k, relatedSubjects.size());
                                    for (SubjectInfo s : relatedSubjects) {
                                        log.warn("【6602010304调试】  - ERP_ACCT_CD={}, CUST1_CD={}, ACCT_CD={}, CONTROL_LEVEL={}", 
                                                s.getErpAcctCd(), s.getCust1Cd(), s.getAcctCd(), s.getControlLevel());
                                    }
                                } else {
                                    // 如果原始查询结果中没有，再从keyToSubjectMap中查找（用于调试）
                                    SubjectInfo s = keyToSubjectMap.get(k);
                                    if (s != null) {
                                        log.warn("【6602010304调试】相关KEY={}, 在keyToSubjectMap中找到, ERP_ACCT_CD={}, CUST1_CD={}, ACCT_CD={}", 
                                                k, s.getErpAcctCd(), s.getCust1Cd(), s.getAcctCd());
                                    } else {
                                        log.warn("【6602010304调试】相关KEY={}, 在原始查询结果和keyToSubjectMap中都不存在", k);
                                    }
                                }
                            });
                }
            }
        }

        // 统计最终结果Map的数据条数（用于分析数据变化）
        int totalRecords = erpAcctCdToKeyListMap.values().stream()
                .mapToInt(List::size)
                .sum();
        int nanNanRecords = erpAcctCdToKeyListMap.entrySet().stream()
                .filter(entry -> entry.getValue().contains("NAN-NAN"))
                .mapToInt(entry -> entry.getValue().size())
                .sum();
        int normalRecords = totalRecords - nanNanRecords;
        log.info("最终结果Map统计: 总记录数={}, 正常映射记录数={}, NAN-NAN映射记录数={}, ERP_ACCT_CD总数={}", 
                totalRecords, normalRecords, nanNanRecords, erpAcctCdToKeyListMap.size());
        
        log.info("最终结果Map (ERP_ACCT_CD -> List<KEY (CUST1_CD-ACCT_CD)>): {}", erpAcctCdToKeyListMap);
        
        // 特别检查目标 ERP_ACCT_CD=6602010304
        if (erpAcctCdToKeyListMap.containsKey("6602010304")) {
            log.info("【6602010304调试】找到目标 ERP_ACCT_CD=6602010304 的映射: {}", erpAcctCdToKeyListMap.get("6602010304"));
        } else {
            log.warn("【6602010304调试】未找到目标 ERP_ACCT_CD=6602010304 的映射！");
            log.warn("【6602010304调试】最终结果Map中ERP_ACCT_CD总数: {}", erpAcctCdToKeyListMap.size());
            // 检查是否有包含6602010304的映射
            boolean foundRelated = false;
            for (Map.Entry<String, List<String>> entry : erpAcctCdToKeyListMap.entrySet()) {
                if (entry.getKey().contains("6602010304") || entry.getValue().stream().anyMatch(v -> v.contains("6602010304"))) {
                    log.warn("【6602010304调试】找到相关映射: ERP_ACCT_CD={}, KEY列表={}", entry.getKey(), entry.getValue());
                    foundRelated = true;
                }
            }
            if (!foundRelated) {
                log.warn("【6602010304调试】未找到任何包含6602010304的相关映射");
            }
        }
        

        // 第六步：将数据写入SUBJECT_EXT_INFO表
        int insertCount = saveToSubjectExtInfo(erpAcctCdToKeyListMap);
        
        // 检查6602010304是否被写入
        if (!erpAcctCdToKeyListMap.containsKey("6602010304")) {
            log.warn("【6602010304调试】ERP_ACCT_CD=6602010304 未在最终结果Map中，可能的原因：");
            log.warn("【6602010304调试】  1. 第一步查询时未找到对应的记录（cust1Cd或acctCd不匹配）");
            log.warn("【6602010304调试】  2. 向上追溯时找不到CONTROL_LEVEL=1的父级，但未被映射到NAN-NAN");
            log.warn("【6602010304调试】  3. 父级下没有找到叶子节点");
            log.warn("【6602010304调试】  4. 叶子节点查询时没有找到对应的ERP_ACCT_CD");
            log.warn("【6602010304调试】  5. 该科目在keysWithoutControlLevelOne中，但keyToSubjectMap中找不到对应的ERP_ACCT_CD");
        } else {
            log.info("【6602010304调试】ERP_ACCT_CD=6602010304 已在最终结果Map中，应该会被写入SUBJECT_EXT_INFO表，映射值: {}", 
                    erpAcctCdToKeyListMap.get("6602010304"));
        }
        
        return String.format("同步成功，处理了 %d 个科目，成功插入 %d 条数据", subjectInfoList.size(), insertCount);
    }

    /**
     * 构建唯一key: cust1Cd + "-" + acctCd
     */
    private String buildKey(String cust1Cd, String acctCd) {
        if (cust1Cd == null) {
            cust1Cd = "";
        }
        return cust1Cd + "-" + acctCd;
    }

    /**
     * 将Map数据写入SUBJECT_EXT_INFO表
     * 
     * @param erpAcctCdToAcctCdListMap ERP_ACCT_CD -> List<CUST1_CD-ACCT_CD> 的映射（格式为 cust1Cd + "-" + acctCd）
     * @return 成功插入的数据条数
     */
    @Transactional(rollbackFor = Exception.class)
    private int saveToSubjectExtInfo(Map<String, List<String>> erpAcctCdToAcctCdListMap) {
        if (CollectionUtils.isEmpty(erpAcctCdToAcctCdListMap)) {
            log.warn("没有数据需要写入SUBJECT_EXT_INFO表");
            return 0;
        }

        // 收集所有需要删除的ERP_ACCT_CD
        Set<String> erpAcctCdsToDelete = erpAcctCdToAcctCdListMap.keySet();
        
        // 先物理删除这些ERP_ACCT_CD对应的旧数据
        if (!CollectionUtils.isEmpty(erpAcctCdsToDelete)) {
            int deletedCount = subjectExtInfoMapper.deleteByErpAcctCds(new ArrayList<>(erpAcctCdsToDelete));
            log.info("物理删除 SUBJECT_EXT_INFO 表记录: {} 条，涉及 {} 个 ERP_ACCT_CD", deletedCount, erpAcctCdsToDelete.size());
        }

        // 构建需要插入的数据列表
        List<SubjectExtInfo> insertList = new ArrayList<>();
        for (Map.Entry<String, List<String>> entry : erpAcctCdToAcctCdListMap.entrySet()) {
            String erpAcctCd = entry.getKey();
            List<String> acctCdList = entry.getValue();
            
            if (CollectionUtils.isEmpty(acctCdList)) {
                continue;
            }

            // 为每个格式化值（cust1Cd + "-" + acctCd）创建一条记录
            for (String formattedAcctCd : acctCdList) {
                if (!StringUtils.hasText(formattedAcctCd)) {
                    continue;
                }
                
                SubjectExtInfo subjectExtInfo = SubjectExtInfo.builder()
                        .erpAcctCd(erpAcctCd)
                        .acctCd(formattedAcctCd) // 存储格式为 cust1Cd + "-" + acctCd
                        .build();
                subjectExtInfo.setDeleted(false);
                insertList.add(subjectExtInfo);
            }
        }

        // 批量插入数据
        if (!CollectionUtils.isEmpty(insertList)) {
            subjectExtInfoMapper.insertBatch(insertList);
            log.info("已成功插入 {} 条数据到SUBJECT_EXT_INFO表", insertList.size());
            return insertList.size();
        } else {
            log.warn("没有有效数据需要插入");
            return 0;
        }
    }

    /**
     * 递归向上追溯，找到CONTROL_LEVEL=1的父级
     *
     * @param acctParCd 父级ACCT_CD
     * @param cust1Cd   当前节点的CUST1_CD，用于查询父级
     * @param visited   已访问的ACCT_CD集合，防止循环引用
     * @return CONTROL_LEVEL=1的父级ACCT_CD，如果找不到返回null
     */
    private String findControlLevelOneParent(String acctParCd, String cust1Cd, Set<String> visited) {
        if (!StringUtils.hasText(acctParCd)) {
            return null;
        }

        // 防止循环引用
        if (visited.contains(acctParCd)) {
            log.warn("检测到循环引用，ACCT_CD: {}", acctParCd);
            return null;
        }
        visited.add(acctParCd);

        // 查询父级，必须同时匹配 acctCd 和 cust1Cd
        LambdaQueryWrapper<SubjectInfo> queryWrapper = new LambdaQueryWrapper<SubjectInfo>()
                .eq(SubjectInfo::getAcctCd, acctParCd)
                .eq(SubjectInfo::getDeleted, false);
        
        // 如果 cust1Cd 不为空，则添加 cust1Cd 条件
        if (StringUtils.hasText(cust1Cd)) {
            queryWrapper.eq(SubjectInfo::getCust1Cd, cust1Cd);
        } else {
            // 如果 cust1Cd 为空，则查询 cust1Cd 为 null 或空字符串的记录
            queryWrapper.and(wrapper -> wrapper.isNull(SubjectInfo::getCust1Cd)
                    .or()
                    .eq(SubjectInfo::getCust1Cd, ""));
        }
        
        // 支持多条记录的正常业务场景（同一个ACCT_CD和CUST1_CD可能对应多个ERP_ACCT_CD）
        List<SubjectInfo> parentList = subjectInfoMapper.selectList(queryWrapper);
        
        if (parentList == null || parentList.isEmpty()) {
            log.warn("未找到父级数据，ACCT_CD: {}, CUST1_CD: {}", acctParCd, cust1Cd);
            return null;
        }
        
        // 如果有多条记录，记录日志
        if (parentList.size() > 1) {
            log.info("查询父级时返回多条记录（正常业务场景，对应多个ERP_ACCT_CD）！ACCT_CD: {}, CUST1_CD: {}, 返回记录数: {}", 
                    acctParCd, cust1Cd, parentList.size());
            for (int i = 0; i < parentList.size(); i++) {
                SubjectInfo info = parentList.get(i);
                log.info("  记录[{}]: ID={}, ACCT_CD={}, CUST1_CD={}, ERP_ACCT_CD={}, CONTROL_LEVEL={}", 
                        i, info.getId(), info.getAcctCd(), info.getCust1Cd(), 
                        info.getErpAcctCd(), info.getControlLevel());
            }
        }
        
        // 优先查找CONTROL_LEVEL=1的记录
        SubjectInfo parent = null;
        for (SubjectInfo info : parentList) {
            if ("1".equals(info.getControlLevel())) {
                parent = info;
                log.info("在多条父级记录中找到CONTROL_LEVEL=1的记录: ID={}, ACCT_CD={}, CUST1_CD={}", 
                        parent.getId(), parent.getAcctCd(), parent.getCust1Cd());
                break;
            }
        }
        
        // 如果没有找到CONTROL_LEVEL=1的记录，使用第一条记录继续向上追溯
        if (parent == null) {
            parent = parentList.get(0);
            if (parentList.size() > 1) {
                log.info("多条父级记录中未找到CONTROL_LEVEL=1的记录，使用第一条记录继续向上追溯: ID={}, ACCT_CD={}, CUST1_CD={}, CONTROL_LEVEL={}", 
                        parent.getId(), parent.getAcctCd(), parent.getCust1Cd(), parent.getControlLevel());
            }
        }

        // 如果父级的CONTROL_LEVEL=1，返回父级的ACCT_CD
        if ("1".equals(parent.getControlLevel())) {
            return parent.getAcctCd();
        }

        // 继续向上追溯，使用父级的 cust1Cd
        return findControlLevelOneParent(parent.getAcctParCd(), parent.getCust1Cd(), visited);
    }

    /**
     * 递归查找所有末级子节点（LEAF=1）
     * 注意：如果父级本身是末级节点，也会被包含在结果中
     *
     * @param parentAcctCd 父级ACCT_CD
     * @param parentCust1Cd 父级CUST1_CD，用于查询条件
     * @param result       结果集合
     * @return 所有末级子节点的格式为 cust1Cd + "-" + acctCd 的列表（包含父级本身，如果父级是末级节点）
     */
    private List<String> findAllLeafChildren(String parentAcctCd, String parentCust1Cd, Set<String> result) {
        if (!StringUtils.hasText(parentAcctCd)) {
            return new ArrayList<>(result);
        }

        // 先检查父级本身是否为末级节点，查询时必须同时匹配 acctCd 和 cust1Cd 以确保唯一性
        LambdaQueryWrapper<SubjectInfo> parentQueryWrapper = new LambdaQueryWrapper<SubjectInfo>()
                .eq(SubjectInfo::getAcctCd, parentAcctCd)
                .eq(SubjectInfo::getDeleted, false);
        
        // 必须添加 cust1Cd 条件，因为同一个 acctCd 可能对应多条记录（不同的 cust1Cd）
        if (StringUtils.hasText(parentCust1Cd)) {
            parentQueryWrapper.eq(SubjectInfo::getCust1Cd, parentCust1Cd);
        } else {
            // 如果 parentCust1Cd 为空，则查询 cust1Cd 为 null 或空字符串的记录
            parentQueryWrapper.and(wrapper -> wrapper.isNull(SubjectInfo::getCust1Cd)
                    .or()
                    .eq(SubjectInfo::getCust1Cd, ""));
        }
        
        // 先查询所有匹配的记录
        List<SubjectInfo> parentList = subjectInfoMapper.selectList(parentQueryWrapper);
        
        // 支持多条记录的正常业务场景（同一个ACCT_CD和CUST1_CD可能对应多个ERP_ACCT_CD）
        if (parentList != null && parentList.size() > 1) {
            log.info("查询父级时返回多条记录（正常业务场景，对应多个ERP_ACCT_CD）！ACCT_CD: {}, CUST1_CD: {}, 返回记录数: {}", 
                    parentAcctCd, parentCust1Cd, parentList.size());
            for (int i = 0; i < parentList.size(); i++) {
                SubjectInfo info = parentList.get(i);
                log.info("  记录[{}]: ID={}, ACCT_CD={}, CUST1_CD={}, ERP_ACCT_CD={}, CONTROL_LEVEL={}, DELETED={}, LEAF={}", 
                        i, info.getId(), info.getAcctCd(), info.getCust1Cd(), info.getErpAcctCd(),
                        info.getControlLevel(), info.getDeleted(), info.getLeaf());
            }
            // 由于子节点结构相同（通过acctParCd关联，不依赖ERP_ACCT_CD），只需要处理一次即可
            // 使用第一条记录继续处理子节点
        }
        
        SubjectInfo parent = parentList != null && !parentList.isEmpty() ? parentList.get(0) : null;
        
        if (parent != null && parent.getLeaf()) {
            // 如果父级本身是末级节点，添加到结果中，格式为 cust1Cd + "-" + acctCd
            String cust1Cd = parent.getCust1Cd();
            String formattedValue = (StringUtils.hasText(cust1Cd) ? cust1Cd : "") + "-" + parentAcctCd;
            if (!result.contains(formattedValue)) {
                result.add(formattedValue);
            }
        }

        // 查询直接子节点，必须同时匹配 acctParCd 和 cust1Cd
        LambdaQueryWrapper<SubjectInfo> childrenQueryWrapper = new LambdaQueryWrapper<SubjectInfo>()
                .eq(SubjectInfo::getAcctParCd, parentAcctCd)
                .eq(SubjectInfo::getDeleted, false);
        
        // 如果 parentCust1Cd 不为空，则添加 cust1Cd 条件
        if (StringUtils.hasText(parentCust1Cd)) {
            childrenQueryWrapper.eq(SubjectInfo::getCust1Cd, parentCust1Cd);
        } else {
            // 如果 parentCust1Cd 为空，则查询 cust1Cd 为 null 或空字符串的记录
            childrenQueryWrapper.and(wrapper -> wrapper.isNull(SubjectInfo::getCust1Cd)
                    .or()
                    .eq(SubjectInfo::getCust1Cd, ""));
        }
        
        List<SubjectInfo> children = subjectInfoMapper.selectList(childrenQueryWrapper);

        for (SubjectInfo child : children) {
            String childAcctCd = child.getAcctCd();
            if (!StringUtils.hasText(childAcctCd)) {
                continue;
            }

            // 如果是末级节点（LEAF=1），添加到结果中，格式为 cust1Cd + "-" + acctCd
            if (child.getLeaf()) {
                String cust1Cd = child.getCust1Cd();
                String formattedValue = (StringUtils.hasText(cust1Cd) ? cust1Cd : "") + "-" + childAcctCd;
                if (!result.contains(formattedValue)) {
                    result.add(formattedValue);
                }
            } else {
                // 如果不是末级节点，递归查找子节点的子节点，传入子节点的 cust1Cd
                findAllLeafChildren(childAcctCd, child.getCust1Cd(), result);
            }
        }

        return new ArrayList<>(result);
    }

}

