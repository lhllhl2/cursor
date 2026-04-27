package com.jasolar.mis.module.system.controller.admin.outer.vo.synorg;

import com.jasolar.mis.module.system.controller.admin.outer.vo.synuser.EsbInfoVo;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * Description:
 * Author : Zhou Hai
 * Date : 07/08/2025 10:56
 * Version : 1.0
 */
@Data
public class OrgParams {

    private EsbInfoVo esbInfo;

    @NotNull(message = "组织信息不能为空")
    private OrgRequestInfoVo requestInfo;



}
