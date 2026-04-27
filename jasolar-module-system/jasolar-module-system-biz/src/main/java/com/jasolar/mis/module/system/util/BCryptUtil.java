package com.jasolar.mis.module.system.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Description:
 * Author : Zhou Hai
 * Date : 09/09/2025 9:29
 * Version : 1.0
 */
public class BCryptUtil {


    private static final PasswordEncoder encoder = new BCryptPasswordEncoder();


    /**
     * 加密
     * @param pwd
     * @return
     */
    public static String encode (String pwd){
        return encoder.encode(pwd);
    }


    /**
     *
     * @param rawPwd
     * @param encodedPassword
     * @return
     */
    public static boolean matches(String rawPwd,String encodedPassword){
        return encoder.matches(rawPwd,encodedPassword);
    }


}
