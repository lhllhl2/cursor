package com.jasolar.mis.module.system.controller.admin.dict.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * Description:
 * Author : Zhou Hai
 * Date : 01/08/2025 13:35
 * Version : 1.0
 */
@Schema(description = "字典-根据code查询字典相关的信息")
@Builder
@Data
public class DictInfoByCodeVo {

    private List<String> codes;




}
