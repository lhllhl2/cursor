package com.jasolar.mis.module.system.api.org.vo;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class OrgListReqVO {

    @Size(max = 255, message = "组织名称长度不能超过255个字符")
    private String name;

    @Size(max = 100, message = "组织编码长度不能超过100个字符")
    private String code;

    @NotBlank(message = "组织类型不能为空")
    private String type;
}

