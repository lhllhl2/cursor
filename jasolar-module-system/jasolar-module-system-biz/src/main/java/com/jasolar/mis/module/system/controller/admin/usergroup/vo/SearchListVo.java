package com.jasolar.mis.module.system.controller.admin.usergroup.vo;

import com.jasolar.mis.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * Description:
 * Author : Zhou Hai
 * Date : 23/07/2025 15:47
 * Version : 1.0
 */
@Schema(description = "用户组-分页查询参数 ")
@Data
public class SearchListVo extends PageParam {

    @Schema(description = "类型: 1.菜单2.报表3.组织 ")
    private List<String> types;

    @Schema(description = "用户组名")
    private String userGroupName;


}
