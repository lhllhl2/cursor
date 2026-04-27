package com.jasolar.mis.module.system.controller.ehr;

import com.jasolar.mis.framework.common.pojo.CommonResult;
import com.jasolar.mis.framework.common.pojo.PageResult;
import com.jasolar.mis.module.system.controller.ehr.vo.EhrSearchVo;
import com.jasolar.mis.module.system.domain.ehr.EhrOrgManageR;
import com.jasolar.mis.module.system.resp.BudgetOrgResp;
import com.jasolar.mis.module.system.service.ehr.EhrOrgManageRService;
import com.jasolar.mis.module.system.util.DistributedLockUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

import static com.jasolar.mis.framework.common.pojo.CommonResult.success;

/**
 * Description:
 * Author : Zhou Hai
 * Date : 09/12/2025 14:54
 * Version : 1.0
 */
@Tag(name = "ehr - 组织")
@Slf4j
@RestController
@RequestMapping("/ehr")
public class EhrOrgManageRController {
    
    @Autowired
    private EhrOrgManageRService ehrOrgManageRService;

    @Autowired
    private RedissonClient redissonClient;
    
    @PostMapping("/searchPage")
    @Operation(summary = "分页查询EHR组织管理数据")
    public CommonResult<PageResult<EhrOrgManageR>> searchPage( @RequestBody EhrSearchVo searchVo) {
        PageResult<EhrOrgManageR> pageResult = ehrOrgManageRService.searchPage(searchVo);
        return success(pageResult);
    }
    
    @PostMapping("/export")
    @Operation(summary = "导出EHR组织管理数据")
    public void export(@RequestBody EhrSearchVo searchVo, HttpServletResponse response) throws Exception {
        ehrOrgManageRService.exportExcel(searchVo, response);
    }
    
    @PostMapping("/import")
    @Operation(summary = "导入EHR组织管理数据并更新")
    public CommonResult<String> importExcel(@RequestParam("file") MultipartFile file) throws Exception {
        String result = ehrOrgManageRService.importExcelAndUpdate(file);
        return success(result);
    }


    @GetMapping("/sync")
    public CommonResult<Void> syncProjectToBusiness() {
        DistributedLockUtils.lock(redissonClient,"ehr_org_syn_key",() -> {
            log.info("开始执行项目数据同步任务");
            ehrOrgManageRService.syncProjectToBusiness();
            // 执行原始组织数据同步
            log.info("项目数据同步任务执行完成");
        });
        return success();
    }


    @PostMapping("/changeBudget")
    @Operation(summary = "修改预算组织")
    public CommonResult<Void> changeBudgetOrg(@RequestBody EhrOrgManageR ehrOrgManageR){
        ehrOrgManageRService.changeSingle(ehrOrgManageR);

        return success();
    }


    @PostMapping("/changeControlLevel")
    @Operation(summary = "修改控制层级")
    public CommonResult<Void> changeControlLevel(@RequestBody EhrOrgManageR ehrOrgManageR){
        ehrOrgManageRService.changeControlLevel(ehrOrgManageR);
        return success();
    }



    @PostMapping("/changeBzLevel")
    @Operation(summary = "修改编制层级")
    public CommonResult<Void> changeBzLevel(@RequestBody EhrOrgManageR ehrOrgManageR){
        ehrOrgManageRService.changeBzLevel(ehrOrgManageR);
        return success();
    }

    @PostMapping("/changeErpDepart")
    @Operation(summary = "修改ERP部门")
    public CommonResult<Void> changeErpDepart(@RequestBody EhrOrgManageR ehrOrgManageR) {
        ehrOrgManageRService.changeErpDepart(ehrOrgManageR);
        return success();
    }

    @PostMapping("/budgetOrg")
    public CommonResult<List<BudgetOrgResp>> getBudgetOrg(){
        List<BudgetOrgResp> orgList = ehrOrgManageRService.getBudgetOrg();
        return CommonResult.success(orgList);
    }


}