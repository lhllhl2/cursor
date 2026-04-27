package com.jasolar.mis.module.system.controller.admin.outer.vo.synuser;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * Description:
 * Author : Zhou Hai
 * Date : 06/08/2025 16:20
 * Version : 1.0
 */
@NoArgsConstructor
@AllArgsConstructor
@Data
@ToString
public class ExtendFieldVo {

    /**
     * "0":普通账号；
     * "1":企业自建专属账号;
     * "2":钉钉自建专属账号;
     */
    private String isExclusive;

    private Integer gender;

    private String birthday;

    // 考勤地点
    private String officeLocation;

    // 岗位
    private String post;

    // 入职日期 格式：yyyy-MM-dd
    private String inductionDate;

    // 离职日期
    private String leavingDate;

    // 公司编码
    private String companyCode;

    // 直属领导工号
    private String directManagerEmpoyeeId;

    private String cardNo;

    private String cardType;


    /**
     * 用户岗位
     */
//    @JsonDeserialize(using = UserJobsListDeserializer.class)
//    private List<UserJobsVo> userJobsDtos;



}
