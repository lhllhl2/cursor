package com.jasolar.mis.module.system.service.admin.project;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.jasolar.mis.framework.common.pojo.PageParam;
import com.jasolar.mis.framework.common.pojo.PageResult;
import com.jasolar.mis.module.system.controller.admin.project.vo.BindUserGroupVo;
import com.jasolar.mis.module.system.controller.admin.project.vo.ProjectSearchVo;
import com.jasolar.mis.module.system.domain.ehr.ProjectView;
import com.jasolar.mis.module.system.domain.project.SystemProject;
import com.jasolar.mis.module.system.domain.project.SystemUserGroupProjectR;
import com.jasolar.mis.module.system.enums.ImportModeEnum;
import com.jasolar.mis.module.system.mapper.admin.project.SystemProjectMapper;
import com.jasolar.mis.module.system.mapper.admin.project.SystemUserGroupProjectRMapper;
import com.jasolar.mis.module.system.mapper.ehr.ProjectViewMapper;
import com.jasolar.mis.module.system.resp.ProjectExceptResp;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Description:
 * Author : Zhou Hai
 * Date : 30/12/2025 11:21
 * Version : 1.0
 */
@Slf4j
@Service
public class SystemProjectServiceImpl implements SystemProjectService{


    @Autowired
    private SystemProjectMapper systemProjectMapper;

    @Autowired
    private SystemUserGroupProjectRMapper userGroupProjectRMapper;

    @Autowired
    private ProjectViewMapper projectViewMapper;

    @Autowired(required = false)
    private ThreadPoolTaskExecutor taskExecutor;



    @Override
    public List<SystemProject> getProjectList(ProjectSearchVo projectSearchVo) {
        return systemProjectMapper.getProjectList(projectSearchVo);
    }

    @Override
    public void bindUserGroup(BindUserGroupVo bindUserGroupVo) {
        log.info("开始绑定项目用户组，请求参数：{}", bindUserGroupVo);
        
        List<Long> userGroupIds = bindUserGroupVo.getUserGroupIds();
        List<Long> projectIds = bindUserGroupVo.getProjectIds();
        ImportModeEnum importMode = bindUserGroupVo.getImportMode();
        
        // 如果未指定导入模式或为覆盖模式，使用原有逻辑（先删除再插入）
        if (importMode == null || importMode == ImportModeEnum.OVERWRITE) {
            // 先删除已存在的绑定关系
            userGroupProjectRMapper.deleteByProjectIds(projectIds);
            
            // 批量插入新的绑定关系
            List<SystemUserGroupProjectR> bindList = new ArrayList<>(userGroupIds.size() * projectIds.size());
            for (Long projectId : projectIds) {
                for (Long userGroupId : userGroupIds) {
                    SystemUserGroupProjectR bind = new SystemUserGroupProjectR();
                    bind.setProjectId(projectId);
                    bind.setUserGroupId(userGroupId);
                    bindList.add(bind);
                }
            }
            
            if (!bindList.isEmpty()) {
                userGroupProjectRMapper.insertBatch(bindList);
            }
            
            log.info("项目用户组绑定完成（覆盖模式），共绑定 {} 个关系", bindList.size());
        } else if (importMode == ImportModeEnum.APPEND) {
            // 追加模式：只插入不存在的记录
            List<SystemUserGroupProjectR> bindList = new ArrayList<>();
            for (Long projectId : projectIds) {
                for (Long userGroupId : userGroupIds) {
                    // 查询关系是否已存在
                    SystemUserGroupProjectR existingRelation = userGroupProjectRMapper.selectOne(
                        new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<SystemUserGroupProjectR>()
                            .eq(SystemUserGroupProjectR::getProjectId, projectId)
                            .eq(SystemUserGroupProjectR::getUserGroupId, userGroupId)
                            .eq(SystemUserGroupProjectR::getDeleted, false)
                    );
                    
                    // 如果不存在，则添加到插入列表
                    if (existingRelation == null) {
                        SystemUserGroupProjectR bind = new SystemUserGroupProjectR();
                        bind.setProjectId(projectId);
                        bind.setUserGroupId(userGroupId);
                        bindList.add(bind);
                    }
                }
            }
            
            // 批量插入不存在的记录
            if (!bindList.isEmpty()) {
                userGroupProjectRMapper.insertBatch(bindList);
                log.info("项目用户组绑定完成（追加模式），共新增 {} 个关系", bindList.size());
            } else {
                log.info("项目用户组绑定完成（追加模式），所有关系已存在，无需新增");
            }
        }
    }


    @Override
    public void syncProjectToBusiness() {
        Long count = systemProjectMapper.selectCount();
        if(count == 0L){
            log.info("权限项目初始化开始。。。");
            init();
            return;
        }

        List<ProjectExceptResp> exceptList = systemProjectMapper.selectExceptData();
        if(exceptList.isEmpty()){
            log.info("权限项目数据无差异，结束。。。");
            return;
        }
        List<ProjectExceptResp> addOrUpdateList = new ArrayList<>(exceptList.stream().filter(x -> x.getToAdd().equals("1")).toList());
        Map<Long, ProjectExceptResp> updateOrDelMap = exceptList.stream().filter(x -> x.getToAdd().equals("2")).collect(Collectors.toMap(ProjectExceptResp::getId, x -> x));
        List<ProjectExceptResp> updateList = new LinkedList<>();
        Iterator<ProjectExceptResp> iterator = addOrUpdateList.iterator();
        while (iterator.hasNext()){
            ProjectExceptResp next = iterator.next();
            if(updateOrDelMap.containsKey(next.getId())){
                updateList.add(next);
                iterator.remove();
                updateOrDelMap.remove(next.getId());
            }
        }

        if(!CollectionUtils.isEmpty(addOrUpdateList)){
            List<SystemProject> addList = addOrUpdateList.stream().map(x -> {
                SystemProject project = SystemProject.builder()
                        .id(x.getId())
                        .projectCode(x.getPrjCd())
                        .projectName(x.getPrjNm())
                        .parentProjectCode(x.getParCd())
                        .parentProjectName(x.getParNm())
                        .leaf(x.isLeaf())
                        .build();
                return project;
            }).toList();
            systemProjectMapper.insertBatch(addList);
            
            // 异步处理新增项目的用户组关系继承
            if (taskExecutor != null) {
                taskExecutor.execute(() -> {
                    try {
                        inheritUserGroupRelationsFromParents(addOrUpdateList);
                    } catch (Exception e) {
                        log.error("异步处理新增项目的用户组关系继承失败", e);
                    }
                });
            } else {
                // 如果没有线程池，同步执行
                inheritUserGroupRelationsFromParents(addOrUpdateList);
            }
        }

        if(!CollectionUtils.isEmpty(updateList)){
            List<SystemProject> uList = updateList.stream().map(x -> {
                SystemProject project = SystemProject.builder()
                        .id(x.getId())
                        .projectCode(x.getPrjCd())
                        .projectName(x.getPrjNm())
                        .parentProjectCode(x.getParCd())
                        .parentProjectName(x.getParNm())
                        .leaf(x.isLeaf())
                        .build();
                return project;
            }).toList();
            systemProjectMapper.updateBatch(uList);
        }



    }


    private void init(){
        int pageSize = 200;
        int currentPage = 1;
        while (true){
            PageParam pageParam = new PageParam();
            pageParam.setPageNo(currentPage);
            pageParam.setPageSize(pageSize);
            PageResult<ProjectView> pageResult = projectViewMapper.selectPage(pageParam, new QueryWrapper<>());
            List<ProjectView> projectViews = pageResult.getList();
            if(projectViews.isEmpty()){
                break;
            }
            List<SystemProject> projectList = projectViews.stream().map(x -> {
                SystemProject project = SystemProject.builder()
                        .id(x.getId())
                        .projectCode(x.getPrjCd())
                        .projectName(x.getPrjNm())
                        .parentProjectCode(x.getParCd())
                        .parentProjectName(x.getParNm())
                        .leaf(x.isLeaf())
                        .build();
                return project;
            }).toList();
            systemProjectMapper.insertBatch(projectList);
            if(projectList.size() < pageSize){
                break;
            }
            currentPage++;
        }
    }

    /**
     * 继承父级项目的用户组关系
     * 对于新增的项目，追溯其所有父级项目，继承父级在 SYSTEM_USER_GROUP_PROJECT_R 表中的用户组关系
     *
     * @param addOrUpdateList 新增的项目列表
     */
    private void inheritUserGroupRelationsFromParents(List<ProjectExceptResp> addOrUpdateList) {
        if (CollectionUtils.isEmpty(addOrUpdateList)) {
            log.info("新增项目列表为空，无需处理用户组关系继承");
            return;
        }

        log.info("开始处理新增项目的用户组关系继承，新增项目数量：{}", addOrUpdateList.size());

        // 构建项目编码到项目信息的映射，方便查找
        Map<String, ProjectExceptResp> newProjectCodeMap = addOrUpdateList.stream()
                .filter(project -> project.getPrjCd() != null)
                .collect(Collectors.toMap(ProjectExceptResp::getPrjCd, project -> project, (o1, o2) -> o1));

        // 查询所有已存在的项目数据，用于追溯父级
        List<SystemProject> allProjects = systemProjectMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<SystemProject>()
                        .eq(SystemProject::getDeleted, false)
        );
        Map<String, SystemProject> existingProjectCodeMap = allProjects.stream()
                .filter(project -> project.getProjectCode() != null)
                .collect(Collectors.toMap(SystemProject::getProjectCode, project -> project, (o1, o2) -> o1));

        // 结果Map：key为项目ID，value为该项目应该继承的所有USER_GROUP_ID集合
        Map<Long, Set<Long>> projectIdToUserGroupIdsMap = new HashMap<>();

        // 遍历新增的项目，追溯其所有父级
        for (ProjectExceptResp newProject : addOrUpdateList) {
            Long projectId = newProject.getId();
            Set<Long> inheritedUserGroupIds = new HashSet<>();

            // 追溯所有父级项目
            String currentParentCode = newProject.getParCd();
            Set<String> visitedCodes = new HashSet<>(); // 防止循环引用

            while (currentParentCode != null && !currentParentCode.isEmpty()) {
                // 防止循环引用
                if (visitedCodes.contains(currentParentCode)) {
                    log.warn("检测到循环引用，项目编码：{}", currentParentCode);
                    break;
                }
                visitedCodes.add(currentParentCode);

                // 优先在已存在的项目中查找父级
                SystemProject parentProject = existingProjectCodeMap.get(currentParentCode);
                if (parentProject != null) {
                    // 父级在已存在的项目中，查询其用户组关系
                    Long parentProjectId = parentProject.getId();
                    List<Long> parentUserGroupIds = userGroupProjectRMapper.getGroupIdsByProjectId(parentProjectId);
                    if (!CollectionUtils.isEmpty(parentUserGroupIds)) {
                        inheritedUserGroupIds.addAll(parentUserGroupIds);
                        log.debug("项目ID={}，从父级项目ID={}(编码={})继承用户组：{}", 
                                projectId, parentProjectId, currentParentCode, parentUserGroupIds);
                    }
                    // 继续向上追溯
                    currentParentCode = parentProject.getParentProjectCode();
                } else {
                    // 如果父级不在已存在的项目中，检查是否在新增列表中
                    ProjectExceptResp parentInNewList = newProjectCodeMap.get(currentParentCode);
                    if (parentInNewList != null) {
                        // 父级也在新增列表中，由于父级也是新增的，可能还没有用户组关系
                        // 但我们可以继续向上追溯父级的父级
                        currentParentCode = parentInNewList.getParCd();
                    } else {
                        // 找不到父级，停止追溯
                        log.debug("项目ID={}，无法找到父级项目编码：{}", projectId, currentParentCode);
                        break;
                    }
                }
            }

            // 如果有继承的用户组，放入Map
            if (!inheritedUserGroupIds.isEmpty()) {
                projectIdToUserGroupIdsMap.put(projectId, inheritedUserGroupIds);
                log.info("项目ID={}，继承的用户组ID集合：{}", projectId, inheritedUserGroupIds);
            } else {
                log.debug("项目ID={}，没有需要继承的用户组关系", projectId);
            }
        }

        // 批量插入用户组项目关系
        if (!projectIdToUserGroupIdsMap.isEmpty()) {
            List<SystemUserGroupProjectR> relationsToInsert = new ArrayList<>();
            for (Map.Entry<Long, Set<Long>> entry : projectIdToUserGroupIdsMap.entrySet()) {
                Long projectId = entry.getKey();
                Set<Long> userGroupIds = entry.getValue();

                for (Long userGroupId : userGroupIds) {
                    // 检查关系是否已存在，避免重复插入
                    SystemUserGroupProjectR existingRelation = userGroupProjectRMapper.selectOne(
                            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<SystemUserGroupProjectR>()
                                    .eq(SystemUserGroupProjectR::getUserGroupId, userGroupId)
                                    .eq(SystemUserGroupProjectR::getProjectId, projectId)
                                    .eq(SystemUserGroupProjectR::getDeleted, false)
                    );

                    if (existingRelation == null) {
                        SystemUserGroupProjectR relation = SystemUserGroupProjectR.builder()
                                .userGroupId(userGroupId)
                                .projectId(projectId)
                                .build();
                        relationsToInsert.add(relation);
                    }
                }
            }

            if (!relationsToInsert.isEmpty()) {
                userGroupProjectRMapper.insertBatch(relationsToInsert);
                log.info("成功为新增项目继承用户组关系，插入关系数量：{}，涉及项目数量：{}", 
                        relationsToInsert.size(), projectIdToUserGroupIdsMap.size());
            } else {
                log.info("所有用户组关系已存在，无需插入");
            }
        } else {
            log.info("没有需要继承的用户组关系");
        }
    }
}
