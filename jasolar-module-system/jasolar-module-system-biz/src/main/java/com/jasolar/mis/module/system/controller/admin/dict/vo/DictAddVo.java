package com.jasolar.mis.module.system.controller.admin.dict.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

/**
 * Description:
 * Author : Zhou Hai
 * Date : 31/07/2025 13:40
 * Version : 1.0
 */
@Schema(description = "管理后台 - 字典新增参数")
@Data
public class DictAddVo {

    @Schema(description = "编码",requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "编码不能为空")
    private String code;

    @Schema(description = "标题",requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "标题不能为空")
    private String title;


    private List<DictLabelVo> labelList;


}
