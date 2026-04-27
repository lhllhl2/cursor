package com.jasolar.mis.module.system.domain.admin.user;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.jasolar.mis.framework.mybatis.core.dataobject.BaseDO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Description:
 * Author : Zhou Hai
 * Date : 21/07/2025 14:22
 * Version : 1.0
 */

@TableName(value = "system_user", autoResultMap = true)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SystemUserDo extends BaseDO {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id ;
    /** 员工名 */

    private String userName ;

    private String pwd;

    private String pwdChanged;

    /** 员工拼音 */
    private String displayName ;

    /** 性别 0:男  1：女 */
    private String gender ;

    /** 邮箱 */
    private String email ;

    /** 手机区号 */
    private String phoneRegion ;

    /** 电话 */
    private String phoneNumber ;

    /** 组织CODE */
    private String organizationCode ;

    /** 状态（0:禁用，1:启用） */
    private String status ;

    /** 直属领导工号 */
    private String directManagerCode ;

    /** 证件号 */
    private String cardNo ;

    /** 证件类别 */
    private String cardType ;

    /** 公司编码 */
    private String companyCode ;

    /** 考勤地点 */
    private String officeLocation ;

    /** 入职时间 */
    private String inductionDate ;

    /** 在职离职状态 */
    private String leaveStatus ;

    /** 离职时间 */
    private String leaveDate ;

    /** 岗位 */
    private String post ;

    /** 生日 */
    private String birthday ;


}
