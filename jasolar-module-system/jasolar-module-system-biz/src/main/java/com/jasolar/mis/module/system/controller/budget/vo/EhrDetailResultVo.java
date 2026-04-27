package com.jasolar.mis.module.system.controller.budget.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Description:
 * Author : Zhou Hai
 * Date : 18/12/2025 14:28
 * Version : 1.0
 */
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
@ApiModel("组织信息")
public class EhrDetailResultVo {

    @ApiModelProperty(value = "ehr组织代码",example = "012-030-010-018-079", required = true)
    private String ehrOrgCode;

    @ApiModelProperty(value = "erp组织代码",example = "0214", required = false)
    private String erpDeptCode;

    @ApiModelProperty(value = "编制层级EHR编码",example = "012-030-010-018-079", required = false)
    private String planOrgCode;

    @ApiModelProperty(value = "预算组织",example = "E010102032109",required = false)
    private String morgCode;

    @ApiModelProperty(value = "预算组织名称",example = "预算组织名称",required = false)
    private String morgName;

    @ApiModelProperty(value = "预算组织上级组织",example = "012-030-010-018-079",required = false)
    private String parentMorgCode;

    @ApiModelProperty(value = "创建时间",example = "2025-12-18 14:28:00",required = false)
    private LocalDateTime createTime;

    @ApiModelProperty(value = "更新时间",example = "2025-12-18 14:28:00",required = false)
    private LocalDateTime updateTime;



}
