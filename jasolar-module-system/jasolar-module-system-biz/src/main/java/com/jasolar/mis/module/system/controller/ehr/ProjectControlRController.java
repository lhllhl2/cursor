package com.jasolar.mis.module.system.controller.ehr;

import com.jasolar.mis.framework.common.pojo.CommonResult;
import com.jasolar.mis.framework.common.pojo.PageResult;
import com.jasolar.mis.module.system.controller.ehr.vo.ProjectControlRSearchVo;
import com.jasolar.mis.module.system.domain.ehr.ProjectControlR;
import com.jasolar.mis.module.system.service.ehr.ProjectControlRService;
import com.jasolar.mis.module.system.util.DistributedLockUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import static com.jasolar.mis.framework.common.pojo.CommonResult.success;

/**
 * ProjectControlR Controller
 */
@Tag(name = "ehr - 项目控制")
@Slf4j
@RestController
@RequestMapping("/proj")
public class ProjectControlRController {

    @Autowired
    private RedissonClient redissonClient;


    @Autowired
    private ProjectControlRService projectControlRService;

    @Autowired
    private ThreadPoolTaskExecutor taskExecutor;

    
    @PostMapping("/searchPage")
    @Operation(summary = "分页查询项目控制数据")
    public CommonResult<PageResult<ProjectControlR>> searchPage( @RequestBody ProjectControlRSearchVo searchVo) {
        PageResult<ProjectControlR> pageResult = projectControlRService.searchPage(searchVo);
        return success(pageResult);
    }
    
    @PostMapping("/export")
    @Operation(summary = "导出项目控制数据")
    public void export(@RequestBody ProjectControlRSearchVo searchVo, HttpServletResponse response) throws Exception {
        projectControlRService.exportExcel(searchVo, response);
    }
    
    @PostMapping("/import")
    @Operation(summary = "导入项目控制数据并更新")
    public CommonResult<String> importExcel(@RequestParam("file") MultipartFile file) throws Exception {
        String result = projectControlRService.importExcelAndUpdate(file);
        return success(result);
    }

    @GetMapping("/sync")
    public CommonResult<Void> syncProjectToBusiness() {
        DistributedLockUtils.lock(redissonClient,"project_syn_key",() -> {
            log.info("开始执行项目数据同步任务");
            projectControlRService.syncProjectToBusiness();
            // 执行原始组织数据同步
            log.info("项目数据同步任务执行完成");
        });
        return success();
    }

    @PostMapping("/changeControlLevel")
    @Operation(summary = "修改项目控制等级")
    public CommonResult<Void> changeControlLevel(@RequestBody ProjectControlR projectControlR) {
        projectControlRService.changeControlLevel(projectControlR);
        return success();
    }


}