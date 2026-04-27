package com.jasolar.mis.module.system.controller.admin.outer.resp;

import com.jasolar.mis.module.system.controller.admin.outer.vo.synuser.EsbInfoVo;
import lombok.Data;
import lombok.experimental.SuperBuilder;

import java.util.Date;

/**
 * Description:
 * Author : Zhou Hai
 * Date : 06/08/2025 17:07
 * Version : 1.0
 */
@SuperBuilder
@Data
public class EsbInfoResp extends EsbInfoVo {

    private String returnCode;

    private String returnMsg;

    private Date responseTime;

    private String returnStatus;

}


