package com.jasolar.mis.module.system.service.admin.project;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jasolar.mis.framework.common.exception.ServiceException;
import com.jasolar.mis.framework.common.pojo.PageParam;
import com.jasolar.mis.framework.common.pojo.PageResult;
import com.jasolar.mis.framework.excel.core.util.ExcelUtils;
import com.jasolar.mis.framework.web.core.util.WebFrameworkUtils;
import com.jasolar.mis.module.system.controller.admin.project.vo.ProjectUserGroupExcelVo;
import com.jasolar.mis.module.system.domain.admin.usergroup.SystemUserGroupDo;
import com.jasolar.mis.module.system.domain.project.SystemProject;
import com.jasolar.mis.module.system.domain.project.SystemUserGroupProjectR;
import com.jasolar.mis.module.system.enums.ProjectEnum;
import com.jasolar.mis.module.system.mapper.admin.project.SystemProjectMapper;
import com.jasolar.mis.module.system.mapper.admin.project.SystemUserGroupProjectRMapper;
import com.jasolar.mis.module.system.mapper.admin.usergroup.UserGroupMapper;
import com.jasolar.mis.module.system.resp.ProjectUserGroupResp;
import com.jasolar.mis.module.system.util.IPageToPageResultUtils;
import com.jasolar.mis.module.system.util.PageExportUtil;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Description:
 * Author : Zhou Hai
 * Date : 30/12/2025 17:20
 * Version : 1.0
 */
@Component
public class SystemUserGroupProjectRServiceImpl implements SystemUserGroupProjectRService{

    @Autowired
    private SystemUserGroupProjectRMapper userGroupProjectRMapper;


    @Autowired
    private UserGroupMapper userGroupMapper;

    @Autowired
    private SystemProjectMapper projectMapper;



    @Override
    public List<Long> getGroupIdsByProjectId(Long projectId) {

        return userGroupProjectRMapper.getGroupIdsByProjectId(projectId);
    }

    @Override
    public void exportExcel(HttpServletResponse response) throws Exception {
        PageParam pageParam = new PageParam();
        pageParam.setPageNo(1);
        pageParam.setPageSize(1000);
        PageResult<ProjectUserGroupResp> pageResult = searchPage(pageParam);
        if(CollectionUtils.isEmpty(pageResult.getList())){
            PageExportUtil.writeEmptyExcel(response);
        }



        PageExportUtil.exportExcel(
                pageParam,
                this::searchPage,
                response,
                "项目.xlsx",
                ProjectUserGroupExcelVo.class,
                this::convertToExcelVO
        );

    }

    @Override
    public PageResult<ProjectUserGroupResp> searchPage(PageParam pageParam) {
        IPage<ProjectUserGroupResp> page = new Page<>(pageParam.getPageNo(), pageParam.getPageSize());
        IPage<ProjectUserGroupResp> proPage = userGroupProjectRMapper.getRelationPage(page);
        return IPageToPageResultUtils.transfer(proPage);
    }

    private ProjectUserGroupExcelVo convertToExcelVO(ProjectUserGroupResp projectUserGroupResp) {
        ProjectUserGroupExcelVo excelVo = ProjectUserGroupExcelVo.builder()
                .prjCd(projectUserGroupResp.getPrjCd())
                .prjNm(projectUserGroupResp.getPrjNm())
                .userGroupName(projectUserGroupResp.getUserGroupName())
                .build();
        return excelVo;
    }

    @Transactional
    @Override
    public String importExcel(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new ServiceException("700001","文件为空，请选择要导入的Excel文件");
        }
        List<ProjectUserGroupExcelVo> projectUserGroupExcelVos = ExcelUtils.readSync(file.getInputStream(), ProjectUserGroupExcelVo.class);
        if(projectUserGroupExcelVos.isEmpty()){
            throw new ServiceException("700002","导入文件内容为空");
        }
        List<ProjectUserGroupExcelVo> addList = projectUserGroupExcelVos.stream()
                .filter(excelVo -> Objects.equals(excelVo.getChange(), ProjectEnum.AuthType.ADD.getDesc())).toList();
        if(CollectionUtils.isEmpty(addList)){
            throw new ServiceException("700003","没有需要添加的权限");
        }

        List<String> userGroupNameSet = addList.stream()
                .map(ProjectUserGroupExcelVo::getUserGroupName)
                .distinct()
                .toList();
        List<SystemUserGroupDo> userGroupDos = new ArrayList<>(userGroupNameSet.size());
        while (!CollectionUtils.isEmpty(userGroupNameSet)){
            if (userGroupNameSet.size() > 50){
                List<String> strings = userGroupNameSet.subList(0, 50);
                List<SystemUserGroupDo> us = userGroupMapper.getByNames(strings);
                if(!CollectionUtils.isEmpty(us)){
                    userGroupDos.addAll(us);
                }
                userGroupNameSet = userGroupNameSet.subList(50, userGroupNameSet.size());
            }else {
                List<SystemUserGroupDo> us = userGroupMapper.getByNames(userGroupNameSet);
                if(!CollectionUtils.isEmpty(us)){
                    userGroupDos.addAll(us);
                }
                break;
            }
        }
        if(CollectionUtils.isEmpty(userGroupDos)){
            throw new ServiceException("700004","没有找到对应的用户组");
        }
        Map<String, SystemUserGroupDo> userGroupMap = userGroupDos.stream()
                .collect(Collectors.toMap(SystemUserGroupDo::getName, v -> v));

        List<String> prjCdSet = addList.stream()
                .map(ProjectUserGroupExcelVo::getPrjCd)
                .distinct()
                .toList();
        List<SystemProject> projectDos = new ArrayList<>(prjCdSet.size());
        while (!CollectionUtils.isEmpty(prjCdSet)){
            if (prjCdSet.size() > 50){
                List<String> pc = prjCdSet.subList(0, 50);
                List<SystemProject> us = projectMapper.getByCds(pc);
                if(!CollectionUtils.isEmpty(us)){
                    projectDos.addAll(us);
                }
                prjCdSet = prjCdSet.subList(50, prjCdSet.size());
            }else {
                List<SystemProject> us = projectMapper.getByCds(prjCdSet);
                if(!CollectionUtils.isEmpty(us)){
                    projectDos.addAll(us);
                }
                break;
            }
        }
        if (CollectionUtils.isEmpty(projectDos)){
            throw new ServiceException("700006","没有找到对应的项目");
        }
        Map<String, SystemProject> projectMap = projectDos.stream()
                .collect(Collectors.toMap(SystemProject::getProjectCode, v -> v));

        List<SystemUserGroupProjectR> rs = new LinkedList<>();
        int count = 0;
        LocalDateTime now = LocalDateTime.now();
        for (ProjectUserGroupExcelVo excelVo : addList) {
            SystemUserGroupDo systemUserGroupDo = userGroupMap.get(excelVo.getUserGroupName());
            if(Objects.isNull(systemUserGroupDo)){
                continue;
            }
            SystemProject systemProject = projectMap.get(excelVo.getPrjCd());
            if(Objects.isNull(systemProject)){
                continue;
            }
            SystemUserGroupProjectR r = SystemUserGroupProjectR.builder()
                    .id(IdWorker.getId())
                    .userGroupId(systemUserGroupDo.getId())
                    .projectId(systemProject.getId())
                    .createTime(now)
                    .updateTime( now)
                    .creator(WebFrameworkUtils.getLoginUserNo())
                    .updater(WebFrameworkUtils.getLoginUserNo())
                    .deleted(false)
                    .build();
            rs.add(r);
            if (rs.size() >= 50){
                userGroupProjectRMapper.insertBatchExsit(rs);
                rs.clear();
            }
            count++;
        }
        if (!CollectionUtils.isEmpty(rs)){
            userGroupProjectRMapper.insertBatchExsit(rs);
        }
        return String.format("导入内容%d,新增内容%d",projectUserGroupExcelVos.size(),count);

    }
}
