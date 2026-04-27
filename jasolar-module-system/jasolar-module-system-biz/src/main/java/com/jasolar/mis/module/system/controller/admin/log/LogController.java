package com.jasolar.mis.module.system.controller.admin.log;

import com.jasolar.mis.framework.common.pojo.CommonResult;
import com.jasolar.mis.framework.common.pojo.PageResult;
import com.jasolar.mis.module.system.controller.admin.log.resp.LogResp;
import com.jasolar.mis.module.system.controller.admin.log.vo.LogPageVo;
import com.jasolar.mis.module.system.service.admin.log.LogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Description:
 * Author : Zhou Hai
 * Date : 27/08/2025 14:58
 * Version : 1.0
 */

@Tag(name = "系统日志")
@RestController
@RequestMapping("/system/log")
public class LogController {


    @Autowired
    private LogService logService;

    @Operation(summary = "1.分页查询日志")
    @PostMapping("/logPage")
    public CommonResult<PageResult<LogResp>> logPage(@RequestBody LogPageVo logPageVo){
        PageResult<LogResp> page = logService.logPage(logPageVo);
        return CommonResult.success(page);
    }



}
