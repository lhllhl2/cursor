package com.jasolar.mis.module.system.controller.admin.outer.vo.synuser;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

/**
 * Description:
 * Author : Zhou Hai
 * Date : 06/08/2025 16:46
 * Version : 1.0
 */
@Data
public class UserJobsVo {


     // 公司编码
    private String companyCode;

     // 部门编码
    private String custDepNum;

     // 职位
    private String position;

     // 是否主岗
    private Boolean isCompanyMainPosition;


     // 岗位序列
    private String positionsequence;

    // 岗位职级
    private String postrank;

    // 岗位赋值
    private String positionassignment;

    // 直接上司工号（对应岗位的上司）
    private String principalStaffCode;

    // 岗位ID（人员在此岗位的唯一标识）
    private String struId;

    /**
     * 用工类型
     * 1、全日制用工
     * ext_30、临时工
     * ext_50、外包实习生
     * ext_51、自招实习生
     * ext_1、劳务派遣
     * ext_2、劳务外包
     * 15、外聘人员
     * 6、退休返聘
     * 5、实习生
     */
    private String identity;

    /**
     * 合同签署单位名称
     */
    @JsonFormat(pattern = "Companyname")
    private String companyName;

}
