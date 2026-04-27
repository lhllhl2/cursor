package com.jasolar.mis.module.system.controller.admin.user.vo.user;

import com.jasolar.mis.framework.mybatis.core.dataobject.BaseDO;
import lombok.Data;

import java.util.List;

/**
 * Description:
 * Author : Zhou Hai
 * Date : 22/07/2025 18:00
 * Version : 1.0
 */
@Data
public class UserPageVo extends BaseDO {

    private Long id;

    private String userName;

    private String displayName ;

    private String email;

    private String phoneNumber ;

    private String leaveStatus ;

    /** 考勤地点 */
    private String officeLocation ;

    /** 岗位 */
    private String post ;

    /** 生日 */
    private String birthday ;

    /** 入职时间 */
    private String inductionDate ;


    /** 离职时间 */
    private String leaveDate ;


    /** 性别 0:男  1：女 */
    private String gender ;

    private String genderDes;



    /** 直属领导工号 */
    private String directManagerCode ;


    /** 组织CODE */
    private String organizationCode ;

    /** 用户所属用户组ID集合 */
    private List<Long> groupIds;

    private List<UserGroupInfoVo> userGroupInfos;
}
