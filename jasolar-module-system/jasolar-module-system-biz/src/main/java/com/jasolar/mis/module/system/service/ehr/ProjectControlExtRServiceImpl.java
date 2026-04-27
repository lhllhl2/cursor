package com.jasolar.mis.module.system.service.ehr;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.jasolar.mis.module.system.domain.ehr.ProjectControlExtR;
import com.jasolar.mis.module.system.domain.ehr.ProjectControlR;
import com.jasolar.mis.module.system.mapper.ehr.ProjectControlExtRMapper;
import com.jasolar.mis.module.system.mapper.ehr.ProjectControlRMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 项目控制扩展关系 Service 实现类
 */
@Slf4j
@Service
public class ProjectControlExtRServiceImpl implements ProjectControlExtRService {

    @Autowired
    private ProjectControlRMapper projectControlRMapper;

    @Autowired
    private ProjectControlExtRMapper projectControlExtRMapper;

    @Override
    public String syncProjectControlRData(List<String> prjCds) {
        if (CollectionUtils.isEmpty(prjCds)) {
            log.warn("项目编码列表为空，无需同步");
            return "项目编码列表为空";
        }

        log.info("开始同步项目控制关系数据，项目编码数量: {}", prjCds.size());

        // Oracle IN子句最多支持1000个表达式，定义批次大小
        final int batchSize = 1000;

        // 第一步：批量查询PROJECT_CONTROL_R表，通过PRJ_CD字段（分批处理，避免Oracle IN子句超过1000个限制）
        List<ProjectControlR> projectControlRList = new ArrayList<>();
        for (int i = 0; i < prjCds.size(); i += batchSize) {
            int end = Math.min(i + batchSize, prjCds.size());
            List<String> batch = prjCds.subList(i, end);
            List<ProjectControlR> batchResult = projectControlRMapper.selectList(
                    new LambdaQueryWrapper<ProjectControlR>()
                            .in(ProjectControlR::getPrjCd, batch)
                            .eq(ProjectControlR::getDeleted, false)
            );
            projectControlRList.addAll(batchResult);
            log.debug("分批查询第 {}-{} 条，查询到 {} 条数据", i + 1, end, batchResult.size());
        }

        if (CollectionUtils.isEmpty(projectControlRList)) {
            log.warn("未找到对应的项目控制关系数据");
            return "未找到对应的项目控制关系数据";
        }

        // 构建PRJ_CD到ProjectControlR的映射
        Map<String, ProjectControlR> prjCdToProjectMap = projectControlRList.stream()
                .collect(Collectors.toMap(
                        ProjectControlR::getPrjCd,
                        project -> project,
                        (existing, replacement) -> existing
                ));

        // 第二步：对于每个PRJ_CD，向上溯源找到CONTROL_LEVEL=1的父级
        Map<String, String> prjCdToControlLevelOnePrjCdMap = new HashMap<>();

        for (String prjCd : prjCds) {
            ProjectControlR current = prjCdToProjectMap.get(prjCd);
            if (current == null) {
                throw new RuntimeException("未找到项目编码对应的数据: " + prjCd);
            }

            String currentPrjCd = current.getPrjCd();
            if (!StringUtils.hasText(currentPrjCd)) {
                throw new RuntimeException("项目编码 " + prjCd + " 对应的PRJ_CD为空");
            }

            // 如果当前就是CONTROL_LEVEL=1，则父级就是自己
            if ("1".equals(current.getControlLevel())) {
                prjCdToControlLevelOnePrjCdMap.put(prjCd, currentPrjCd);
                continue;
            }

            // 向上追溯
            String controlLevelOnePrjCd = findControlLevelOneParent(current.getParCd(), new HashSet<>());
            if (controlLevelOnePrjCd == null) {
                throw new RuntimeException("项目编码 " + prjCd + " (PRJ_CD: " + currentPrjCd + ") 无法追溯到CONTROL_LEVEL=1的父级");
            }
            prjCdToControlLevelOnePrjCdMap.put(prjCd, controlLevelOnePrjCd);
        }

        log.info("项目编码到控制层级为1的父级映射: {}", prjCdToControlLevelOnePrjCdMap);

        // 第三步：对于每个CONTROL_LEVEL=1的父级，找出所有子集的PRJ_CD
        Set<String> uniqueControlLevelOnePrjCds = new HashSet<>(prjCdToControlLevelOnePrjCdMap.values());
        
        // 批量查询所有父级对应的 ProjectControlR 记录，避免在循环中查询（分批处理，避免Oracle IN子句超过1000个限制）
        List<ProjectControlR> parentRecords = new ArrayList<>();
        List<String> uniqueControlLevelOnePrjCdList = new ArrayList<>(uniqueControlLevelOnePrjCds);
        for (int i = 0; i < uniqueControlLevelOnePrjCdList.size(); i += batchSize) {
            int end = Math.min(i + batchSize, uniqueControlLevelOnePrjCdList.size());
            List<String> batch = uniqueControlLevelOnePrjCdList.subList(i, end);
            List<ProjectControlR> batchResult = projectControlRMapper.selectList(
                    new LambdaQueryWrapper<ProjectControlR>()
                            .in(ProjectControlR::getPrjCd, batch)
                            .eq(ProjectControlR::getDeleted, false)
            );
            parentRecords.addAll(batchResult);
            log.debug("分批查询父级记录第 {}-{} 条，查询到 {} 条数据", i + 1, end, batchResult.size());
        }
        // 建立 prjCd 到 ProjectControlR 的映射
        Map<String, ProjectControlR> parentCodeToRecordMap = parentRecords.stream()
                .collect(Collectors.toMap(ProjectControlR::getPrjCd, e -> e, (e1, e2) -> e1));
        
        // Map<父级PRJ_CD, 所有子集PRJ_CD列表（包含父级本身，如果父级是叶子节点）>
        Map<String, List<String>> parentToChildrenMap = new HashMap<>();

        for (String parentPrjCd : uniqueControlLevelOnePrjCds) {
            List<String> childrenPrjCds = findAllChildren(parentPrjCd, new HashSet<>());
            // 只有当 parentPrjCd 对应的 ProjectControlR 是叶子节点（leaf = true）时，才将父级本身加入到子集中
            ProjectControlR parentRecord = parentCodeToRecordMap.get(parentPrjCd);
            if (parentRecord != null && parentRecord.isLeaf()) {
                if (!childrenPrjCds.contains(parentPrjCd)) {
                    childrenPrjCds.add(parentPrjCd);
                }
            }
            parentToChildrenMap.put(parentPrjCd, childrenPrjCds);
        }

        log.info("父级到所有子集映射: {}", parentToChildrenMap);

        // 第四步：构建最终的Map: PRJ_CD -> List<PRJ_CD>
        // parentToChildrenMap下的childrenPrjCds都共享同一个List<PRJ_CD>（父级对应的PRJ_CD列表）
        Map<String, List<String>> prjCdToPrjCdListMap = new HashMap<>();
        for (Map.Entry<String, List<String>> entry : parentToChildrenMap.entrySet()) {
            String parentPrjCd = entry.getKey();
            List<String> childrenPrjCds = entry.getValue();
            // 将childrenPrjCds中的所有PRJ_CD都映射到同一个List<PRJ_CD>
            for (String prjCd : childrenPrjCds) {
                prjCdToPrjCdListMap.put(prjCd, childrenPrjCds);
            }
        }

        log.info("最终结果Map (PRJ_CD -> List<PRJ_CD>): {}", prjCdToPrjCdListMap);

        // 第五步：将数据写入PROJECT_CONTROL_EXT_R表
        int insertCount = saveToProjectControlExtR(prjCdToPrjCdListMap);
        
        return String.format("同步成功，处理了 %d 个项目编码，成功插入 %d 条数据", prjCds.size(), insertCount);
    }

    /**
     * 递归向上追溯，找到CONTROL_LEVEL=1的父级
     *
     * @param parCd  父级PRJ_CD
     * @param visited 已访问的PRJ_CD集合，防止循环引用
     * @return CONTROL_LEVEL=1的父级PRJ_CD，如果找不到返回null
     */
    private String findControlLevelOneParent(String parCd, Set<String> visited) {
        if (!StringUtils.hasText(parCd)) {
            return null;
        }

        // 防止循环引用
        if (visited.contains(parCd)) {
            log.warn("检测到循环引用，PRJ_CD: {}", parCd);
            return null;
        }
        visited.add(parCd);

        // 查询父级
        ProjectControlR parent = projectControlRMapper.selectOne(
                new LambdaQueryWrapper<ProjectControlR>()
                        .eq(ProjectControlR::getPrjCd, parCd)
                        .eq(ProjectControlR::getDeleted, false)
        );

        if (parent == null) {
            log.warn("未找到父级数据，PRJ_CD: {}", parCd);
            return null;
        }

        // 如果父级的CONTROL_LEVEL=1，返回父级的PRJ_CD
        if ("1".equals(parent.getControlLevel())) {
            return parent.getPrjCd();
        }

        // 继续向上追溯
        return findControlLevelOneParent(parent.getParCd(), visited);
    }

    /**
     * 递归查找所有子集（包括子集的子集）
     *
     * @param parentPrjCd 父级PRJ_CD
     * @param result      结果集合
     * @return 所有子集的PRJ_CD列表
     */
    private List<String> findAllChildren(String parentPrjCd, Set<String> result) {
        return findAllChildren(parentPrjCd, result, new HashSet<>());
    }

    /**
     * 递归查找所有子集（包括子集的子集）
     *
     * @param parentPrjCd 父级PRJ_CD
     * @param result      结果集合
     * @param visited     已访问的节点集合，用于防止循环引用
     * @return 所有子集的PRJ_CD列表
     */
    private List<String> findAllChildren(String parentPrjCd, Set<String> result, Set<String> visited) {
        if (!StringUtils.hasText(parentPrjCd)) {
            return new ArrayList<>(result);
        }

        // 防止循环引用：如果已经访问过该节点，直接返回
        if (visited.contains(parentPrjCd)) {
            log.warn("检测到循环引用，跳过节点: {}", parentPrjCd);
            return new ArrayList<>(result);
        }

        // 标记当前节点为已访问
        visited.add(parentPrjCd);

        // 查询直接子集
        List<ProjectControlR> children = projectControlRMapper.selectList(
                new LambdaQueryWrapper<ProjectControlR>()
                        .eq(ProjectControlR::getParCd, parentPrjCd)
                        .eq(ProjectControlR::getDeleted, false)
        );

        for (ProjectControlR child : children) {
            String childPrjCd = child.getPrjCd();
            if (!StringUtils.hasText(childPrjCd)) {
                continue;
            }

            // 只有当 childPrjCd 对应的 ProjectControlR 是叶子节点（leaf = true）时，才添加到结果集中
            if (child.isLeaf() && !result.contains(childPrjCd)) {
                result.add(childPrjCd);
            }

            // 无论是否是叶子节点，都要继续递归查找子集的子集（因为非叶子节点可能有叶子子节点）
            // 传递 visited 集合，防止循环引用
            findAllChildren(childPrjCd, result, visited);
        }

        return new ArrayList<>(result);
    }

    /**
     * 将Map数据写入PROJECT_CONTROL_EXT_R表
     *
     * @param prjCdToPrjCdListMap PRJ_CD -> List<PRJ_CD> 的映射
     * @return 成功插入的数据条数
     */
    @Transactional(rollbackFor = Exception.class)
    private int saveToProjectControlExtR(Map<String, List<String>> prjCdToPrjCdListMap) {
        if (CollectionUtils.isEmpty(prjCdToPrjCdListMap)) {
            log.warn("没有数据需要写入PROJECT_CONTROL_EXT_R表");
            return 0;
        }

        // 收集所有需要删除的PRJ_CD
        Set<String> prjCdsToDelete = prjCdToPrjCdListMap.keySet();

        // 先物理删除这些PRJ_CD对应的旧数据（分批处理，避免Oracle IN子句超过1000个限制）
        if (!CollectionUtils.isEmpty(prjCdsToDelete)) {
            int totalDeletedCount = 0;
            int batchSize = 1000; // Oracle IN子句最多支持1000个表达式
            List<String> prjCdsToDeleteList = new ArrayList<>(prjCdsToDelete);
            for (int i = 0; i < prjCdsToDeleteList.size(); i += batchSize) {
                int end = Math.min(i + batchSize, prjCdsToDeleteList.size());
                List<String> batch = prjCdsToDeleteList.subList(i, end);
                int deletedCount = projectControlExtRMapper.deleteByPrjCds(batch);
                totalDeletedCount += deletedCount;
                log.debug("分批删除第 {}-{} 条，删除了 {} 条数据", i + 1, end, deletedCount);
            }
            log.info("物理删除 PROJECT_CONTROL_EXT_R 表记录: {} 条，涉及 {} 个 PRJ_CD", totalDeletedCount, prjCdsToDelete.size());
        }

        // 构建需要插入的数据列表
        List<ProjectControlExtR> insertList = new ArrayList<>();
        for (Map.Entry<String, List<String>> entry : prjCdToPrjCdListMap.entrySet()) {
            String prjCd = entry.getKey();
            List<String> relatedPrjCdList = entry.getValue();

            if (CollectionUtils.isEmpty(relatedPrjCdList)) {
                continue;
            }

            // 为每个关联项目编码创建一条记录
            for (String relatedPrjCd : relatedPrjCdList) {
                if (!StringUtils.hasText(relatedPrjCd)) {
                    continue;
                }

                ProjectControlExtR projectControlExtR = ProjectControlExtR.builder()
                        .prjCd(prjCd)
                        .relatedPrjCd(relatedPrjCd)
                        .build();
                projectControlExtR.setDeleted(false);
                insertList.add(projectControlExtR);
            }
        }

        // 批量插入数据
        if (!CollectionUtils.isEmpty(insertList)) {
            projectControlExtRMapper.insertBatch(insertList);
            log.info("已成功插入 {} 条数据到PROJECT_CONTROL_EXT_R表", insertList.size());
            return insertList.size();
        } else {
            log.warn("没有有效数据需要插入");
            return 0;
        }
    }

}

