package com.jasolar.mis.module.system.controller.admin.usergroup.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * Description:
 * Author : Zhou Hai
 * Date : 25/07/2025 15:01
 * Version : 1.0
 */

@Schema(description = "用户组-List查询参数 ")
@Data
public class SearchSimpleListVo {

    @Schema(description = "类型: 1.菜单2.报表3.组织 ")
    private List<String> types;


}
