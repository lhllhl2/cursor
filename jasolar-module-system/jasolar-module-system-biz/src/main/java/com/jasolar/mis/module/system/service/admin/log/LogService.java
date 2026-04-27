package com.jasolar.mis.module.system.service.admin.log;

import com.jasolar.mis.framework.common.pojo.PageResult;
import com.jasolar.mis.module.system.controller.admin.log.resp.LogResp;
import com.jasolar.mis.module.system.controller.admin.log.vo.LogPageVo;

/**
 * Description:
 * Author : Zhou Hai
 * Date : 27/08/2025 15:09
 * Version : 1.0
 */

public interface LogService {



    PageResult<LogResp> logPage(LogPageVo logPageVo);

}
