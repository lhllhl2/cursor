package com.jasolar.mis.module.system.controller.admin.user.vo.user;

import lombok.Data;

/**
 * Description:
 * Author : Zhou Hai
 * Date : 05/09/2025 13:49
 * Version : 1.0
 */
@Data
public class UserUpdatePwdVo {

    private String pwd;

    private String newPwd;

    private String confirmPwd;

}
