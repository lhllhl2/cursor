package com.jasolar.mis.module.system.controller.admin.log.vo;

import com.jasolar.mis.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * Description:
 * Author : Zhou Hai
 * Date : 27/08/2025 15:06
 * Version : 1.0
 */
@Schema(description = "管理后台 - log ")
@Data
public class LogPageVo extends PageParam {

    @Schema(description = "用户工号")
    private String userName;

    @Schema(description = "登录类型")
    private String logType;


}
