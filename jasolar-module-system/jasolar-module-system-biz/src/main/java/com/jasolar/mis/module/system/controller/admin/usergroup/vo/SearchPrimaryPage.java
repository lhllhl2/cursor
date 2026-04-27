package com.jasolar.mis.module.system.controller.admin.usergroup.vo;

import com.jasolar.mis.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * Description:
 * Author : Zhou Hai
 * Date : 24/07/2025 14:26
 * Version : 1.0
 */
@Schema(description = "查询用户组关联角色参数")
@Data
public class SearchPrimaryPage extends PageParam {

    @Schema(description = "主键")
    private Long id;

}
