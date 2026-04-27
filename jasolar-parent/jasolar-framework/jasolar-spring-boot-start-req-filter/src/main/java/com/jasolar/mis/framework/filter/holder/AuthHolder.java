package com.jasolar.mis.framework.filter.holder;

import com.jasolar.mis.framework.common.constant.AuthenticationHolder;
import com.jasolar.mis.framework.filter.entity.UserEntity;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.util.StringUtils;

/**
 * Description:
 * Author : Zhou Hai
 * Date : 28/07/2025 9:15
 * Version : 1.0
 */
public class AuthHolder {


    private static ThreadLocal<UserEntity> CURRENT_USER_INFO = new ThreadLocal<>();


    public static void setCurrentUserInfo( HttpServletRequest req){
        UserEntity user = new UserEntity();
        String userId = req.getHeader(AuthenticationHolder.USER_ID);
        if(StringUtils.hasLength(userId)){
            user.setId(Long.valueOf(userId));
        }
        user.setUserName(req.getHeader(AuthenticationHolder.NAME));
        user.setDisplayName(req.getHeader(AuthenticationHolder.DISPLAY_NAME));
        CURRENT_USER_INFO.set(user);
    }


    public static void release() {
        CURRENT_USER_INFO.remove();
    }


    public static UserEntity getCurrentUser(){
        return CURRENT_USER_INFO.get();
    }

    public static Long getCurrentUserId(){
        UserEntity user = CURRENT_USER_INFO.get();
        if(user == null){
            return null;
        }
        return user.getId();
    }


    public static String getCurrentUserName(){
        UserEntity user = CURRENT_USER_INFO.get();
        if(user == null){
            return null;
        }
        return user.getUserName();
    }


}
