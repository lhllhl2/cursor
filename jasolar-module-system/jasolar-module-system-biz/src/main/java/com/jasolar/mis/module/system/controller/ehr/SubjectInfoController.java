package com.jasolar.mis.module.system.controller.ehr;

import com.jasolar.mis.framework.common.pojo.CommonResult;
import com.jasolar.mis.framework.common.pojo.PageResult;
import com.jasolar.mis.module.system.controller.ehr.vo.ErpUnmappedAccountVO;
import com.jasolar.mis.module.system.controller.ehr.vo.SubjectInfoSearchVo;
import com.jasolar.mis.module.system.domain.ehr.SubjectInfo;
import com.jasolar.mis.module.system.domain.ehr.SubjectInfoControlLevelView;
import com.jasolar.mis.module.system.service.ehr.SubjectInfoService;
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
 * ProjectInfo Controller
 */
@Tag(name = "ehr - 项目信息")
@Slf4j
@RestController
@RequestMapping("/subj")
public class SubjectInfoController {

    @Autowired
    private RedissonClient redissonClient;


    @Autowired
    private SubjectInfoService subjectInfoService;

    @PostMapping("/searchPage")
    @Operation(summary = "分页查询科目信息数据")
    public CommonResult<PageResult<SubjectInfoControlLevelView>> searchPage(@RequestBody SubjectInfoSearchVo searchVo) {
        PageResult<SubjectInfoControlLevelView> pageResult = subjectInfoService.searchPage(searchVo);
        return success(pageResult);
    }

    @PostMapping("/export")
    @Operation(summary = "导出科目信息数据")
    public void export(@RequestBody SubjectInfoSearchVo searchVo, HttpServletResponse response) throws Exception {
        subjectInfoService.exportExcel(searchVo, response);
    }

    @PostMapping("/import")
    @Operation(summary = "导入科目信息数据并更新")
    public CommonResult<String> importExcel(@RequestParam("file") MultipartFile file) throws Exception {
        String result = subjectInfoService.importExcelAndUpdate(file);
        return success(result);
    }

    @GetMapping("/sync")
    public CommonResult<Void> syncSubjectToBusiness() {

        DistributedLockUtils.lock(redissonClient,"subject_syn_key",() -> {
            log.info("开始执行科目数据同步任务");
            subjectInfoService.syncSubjectToBusiness();
            log.info("科目数据同步任务执行完成");
        });

        return success();
    }


    @PostMapping("/changeControlLevel")
    @Operation(summary = "修改控制层级")
    public CommonResult<Void> changeControlLevel(@RequestBody SubjectInfo subjectInfo){
        subjectInfoService.changeControlLevel(subjectInfo);
        return success();
    }

    @GetMapping("/erp/unmapped")
    @Operation(summary = "查询ERP视图中未映射科目")
    public CommonResult<List<ErpUnmappedAccountVO>> listUnmappedErpAccounts() {
        return success(subjectInfoService.listUnmappedErpAccounts());
    }


}