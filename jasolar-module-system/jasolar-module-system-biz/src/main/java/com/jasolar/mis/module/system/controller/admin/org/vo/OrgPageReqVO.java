package com.jasolar.mis.module.system.controller.admin.org.vo;

import com.jasolar.mis.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Schema(description = "管理后台 - 组织分页查询 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
public class OrgPageReqVO extends PageParam {

    @Schema(description = "组织名称", example = "晶澳集团")
    @Size(max = 255, message = "组织名称长度不能超过255个字符")
    private String name;

    @Schema(description = "组织编码", example = "ORG001")
    @Size(max = 100, message = "组织编码长度不能超过100个字符")
    private String code;

    @Schema(description = "组织类型：LE法人组织,ME管理组织", example = "LE")
    private String type;
} 