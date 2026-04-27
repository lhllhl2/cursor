package com.jasolar.mis.module.system.controller.admin.project;

import com.jasolar.mis.framework.common.pojo.CommonResult;
import com.jasolar.mis.module.system.controller.admin.project.vo.BindUserGroupVo;
import com.jasolar.mis.module.system.controller.admin.project.vo.ProjectSearchVo;
import com.jasolar.mis.module.system.domain.project.SystemProject;
import com.jasolar.mis.module.system.service.admin.project.SystemProjectService;
import com.jasolar.mis.module.system.service.admin.project.SystemUserGroupProjectRService;
import com.jasolar.mis.module.system.util.DistributedLockUtils;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * Description:
 * Author : Zhou Hai
 * Date : 30/12/2025 11:10
 * Version : 1.0
 */
@Tag(name = "管理后台 - 组织管理")
@RestController
@RequestMapping("/system/project")
@Validated
@Slf4j
public class SystemProjectController {


    @Autowired
    private RedissonClient redissonClient;

    @Autowired
    private SystemProjectService systemProjectService;

    @Autowired
    private SystemUserGroupProjectRService systemUserGroupProjectRService;



    @PostMapping("/list")
    public CommonResult<List<SystemProject>> getProjectList(@RequestBody ProjectSearchVo projectSearchVo) {
        List<SystemProject> result = systemProjectService.getProjectList(projectSearchVo);
        return CommonResult.success(result);
    }


    @PostMapping("/bindUserGroup")
    public CommonResult<Void> bindUserGroup(@RequestBody BindUserGroupVo bindUserGroupVo) {
        systemProjectService.bindUserGroup(bindUserGroupVo);
        return CommonResult.success();
    }


    @GetMapping("/getGroupIdsByProjectId")
    public CommonResult<List<Long>> getGroupIdsByProjectId(@RequestParam("projectId") Long projectId) {
        List<Long> groupIds = systemUserGroupProjectRService.getGroupIdsByProjectId(projectId);
        return CommonResult.success(groupIds);
    }


    @PostMapping("/export")
    public void exportExcel(HttpServletResponse response) throws Exception {
        systemUserGroupProjectRService.exportExcel(response);

    }


    @PostMapping("/import")
    public  CommonResult<String> importExcel(@RequestParam("file") MultipartFile file) throws Exception {
        String info = systemUserGroupProjectRService.importExcel(file);
        return CommonResult.success(info);
    }


    @GetMapping("/sync")
    public CommonResult<Void> syn(){
        DistributedLockUtils.lock(redissonClient,"auth_project_syn_key",() -> {
            log.info("开始执行项目数据同步任务");
            systemProjectService.syncProjectToBusiness();
            // 执行原始组织数据同步
            log.info("项目数据同步任务执行完成");
        });
        return CommonResult.success();
    }


}
