package com.jasolar.mis.module.system.controller.admin.dict.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * Description:
 * Author : Zhou Hai
 * Date : 31/07/2025 14:47
 * Version : 1.0
 */

@Schema(description = "管理后台 - 字典修改参数")
@Data
public class DictEditVo extends DictAddVo{

    @Schema(description = "字典主键",requiredMode = Schema.RequiredMode.REQUIRED)
    private Long id;
}
