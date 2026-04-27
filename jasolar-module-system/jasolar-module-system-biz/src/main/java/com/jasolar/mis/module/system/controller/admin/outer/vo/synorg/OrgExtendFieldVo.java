package com.jasolar.mis.module.system.controller.admin.outer.vo.synorg;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * Description:
 * Author : Zhou Hai
 * Date : 07/08/2025 10:24
 * Version : 1.0
 */
@Data
public class OrgExtendFieldVo {

    // 公司编码
    @JsonProperty("companycode")
    private String companyCode;

    // 公司名称
    @JsonProperty( "companyname")
    private String companyName;

    // 成本组织名称
    @JsonProperty("costname")
    private String costName;

    // 传【部门负责人】对应员工编码（当部门负责人不存在时，传【组织负责人】）
    @JsonProperty("orgheadid")
    private String orgHeadId;

    // 部门分管领导员工编号
    @JsonProperty("orgfgid")
    private String orgFgId;

    // 分管领导上级的员工编号
    private String fgsjId;

    //部门属性
    private String bmsx;

    // 组织全路径
    @JsonProperty( "orgfullpath")
    private String orgFullPath;







}
