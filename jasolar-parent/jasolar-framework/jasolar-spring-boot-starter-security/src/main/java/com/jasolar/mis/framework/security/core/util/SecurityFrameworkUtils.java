package com.jasolar.mis.framework.security.core.util;

import java.util.Collections;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;

import com.jasolar.mis.framework.common.security.LoginServletUtils;
import com.jasolar.mis.framework.common.security.LoginUser;

import jakarta.annotation.Nullable;
import jakarta.servlet.http.HttpServletRequest;

/**
 * 安全服务工具类
 *
 * @author zhaohuang
 */
public final class SecurityFrameworkUtils {

    private SecurityFrameworkUtils() {
    }

    /**
     * 获得当前认证信息
     *
     * @return 认证信息
     */
    public static Authentication getAuthentication() {
        SecurityContext context = SecurityContextHolder.getContext();
        if (context == null) {
            return null;
        }
        return context.getAuthentication();
    }

    // /**
    // * 获取当前用户
    // *
    // * @return 当前用户
    // */
    // @Nullable
    // @Deprecated
    // public static LoginUser getLoginUser() {
    // Authentication authentication = getAuthentication();
    // if (authentication == null) {
    // return null;
    // }
    // return authentication.getPrincipal() instanceof LoginUser usr ? usr : null;
    // }

    /**
     * 
     * 获得当前登录用户的ID，从上下文中
     * 
     * @return 当前登录用户的ID
     * 
     * @deprecated 建议使用{@link LoginServletUtils#getLoginUserId()}
     */
    @Nullable
    @Deprecated
    public static Long getLoginUserId() {
        return LoginServletUtils.getLoginUserId();
    }
    //
    // /**
    // * 获得当前用户的编号，从上下文中
    // *
    // * @return 用户编号
    // */
    // @Nullable
    // public static String getLoginUserNo() {
    // LoginUser loginUser = getLoginUser();
    // return loginUser != null ? loginUser.getNo() : null;
    // }

    // /**
    // * 获得当前用户的昵称，从上下文中
    // *
    // * @return 昵称
    // */
    // @Nullable
    // public static String getLoginUserNickname() {
    // LoginUser loginUser = getLoginUser();
    // return loginUser != null ? loginUser.getName() : null;
    // }
    //
    // /**
    // * 获得当前用户的部门编号，从上下文中
    // *
    // * @return 部门编号
    // */
    // @Nullable
    // public static Long getLoginUserDeptId() {
    // LoginUser loginUser = getLoginUser();
    // return loginUser != null ? 0L : null;
    // }

    /**
     * 设置当前用户
     *
     * @param loginUser 登录用户
     * @param request 请求
     */
    public static void setLoginUser(LoginUser loginUser, HttpServletRequest request) {
        // 创建 Authentication，并设置到上下文
        Authentication authentication = buildAuthentication(loginUser, request);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // 设置用户
        LoginServletUtils.setLoginUser(request, loginUser);

        // // 额外设置到 request 中，用于 ApiAccessLogFilter 可以获取到用户编号；
        // // 原因是，Spring Security 的 Filter 在 ApiAccessLogFilter 后面，在它记录访问日志时，线上上下文已经没有用户编号等信息
        // WebFrameworkUtils.setLoginUserId(request, loginUser.getId());
        // WebFrameworkUtils.setLoginUserNo(request, loginUser.getNo());
        // WebFrameworkUtils.setLoginUserType(request, loginUser.getUserType());
    }

    private static Authentication buildAuthentication(LoginUser loginUser, HttpServletRequest request) {
        // 创建 UsernamePasswordAuthenticationToken 对象
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(loginUser, null,
                Collections.emptyList());
        authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        return authenticationToken;
    }

}
