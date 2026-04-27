package com.jasolar.mis.module.system.controller.admin.role.vo;

import com.jasolar.mis.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * Description:
 * Author : Zhou Hai
 * Date : 24/07/2025 18:44
 * Version : 1.0
 */
@Schema(description = "角色分页查询参数")
@Data
public class RolePageVo extends PageParam {

    @Schema(description = "角色名称")
    private String name;

    @Schema(description = "角色编码")
    private String code;

    @Schema(description = "状态")
    private String status;

}
