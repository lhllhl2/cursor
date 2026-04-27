package com.jasolar.mis.module.system.controller.admin.outer.vo.synorg;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * Description:
 * Author : Zhou Hai
 * Date : 07/08/2025 10:21
 * Version : 1.0
 */
@Data
public class OrgRequestInfoVo {

    // 组织机构的名称
    private String organization;

    // 本组织机构的uuid或外部ID
    @NotBlank(message = "组织id不能为空")
    private String organizationUuid;

    // 所属父级组织机构的uuid或外部ID
    @NotBlank(message = "组织父id不能为空")
    private String parentUuid;

    private Boolean rootNode;

    private OrgExtendFieldVo extendFields;



}
