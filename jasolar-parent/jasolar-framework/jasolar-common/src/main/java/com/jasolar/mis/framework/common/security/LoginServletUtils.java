package com.jasolar.mis.framework.common.security;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

import javax.annotation.Nonnull;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.jasolar.mis.framework.common.enums.UserTypeEnum;
import com.jasolar.mis.framework.common.util.json.JsonUtils;

import cn.hutool.core.util.ObjectUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

/**
 * 
 * HttpServlet的工具类
 * 
 * @author galuo
 * @date 2025-03-18 10:57
 *
 */
@Slf4j
public final class LoginServletUtils {

    /**
     * 从请求中，获得认证 Token
     *
     * @param request 请求
     * @param headerName 认证 Token 对应的 Header 名字
     * @param parameterName 认证 Token 对应的 Parameter 名字
     * @return 认证 Token
     */
    public static String getToken(HttpServletRequest request, String headerName, String parameterName) {
        String removedHeader = request.getHeader(LoginUserUtils.HEADER_TOKEN_REMOVED);
        boolean removed = Boolean.parseBoolean(removedHeader);
        if (removed) {
            // 没有token
            return null;
        }

        // 1. 获得 Token。优先级：Header > Parameter
        String token = request.getHeader(headerName);
        if (StringUtils.isBlank(token)) {
            token = request.getParameter(parameterName);
        }
        if (StringUtils.isBlank(token)) {
            return null;
        }
        token = token.trim();
        // 去除 Token 中带的 Bearer
        if (StringUtils.startsWithIgnoreCase(token, LoginUserUtils.AUTHORIZATION_BEARER)) {
            return token.substring(LoginUserUtils.AUTHORIZATION_BEARER.length()).trim();
        }
        return token;
    }

    /**
     * 从请求中查找登录用户信息.
     * 
     * @param request HttpServletRequest
     * @return 用户信息,不会返回null防止空指针异常. 是否登录需要调用方法 {@link LoginUser#isAuthorized()} 判断
     */
    @Nonnull
    public static LoginUser getLoginUser(HttpServletRequest request) {
        if (request == null) {
            // 没有request则可能是系统调用
            return LoginUser.SYSTEM;
        }

        LoginUser user = (LoginUser) request.getAttribute(LoginUserUtils.ATTR_LOGIN_USER);
        if (user != null) {
            return user;
        }

        // 从网关或者feign中获取请求header
        String header = request.getHeader(LoginUserUtils.HEADER_LOGIN_USER);
        if (StringUtils.isBlank(header)) {
            // 没有用户,防止空指针异常
            return LoginUser.ANONYMOUS;
        }
        // 解码，解决中文乱码问题
        String json = URLDecoder.decode(header, StandardCharsets.UTF_8);
        // log.info("通过Header获取到用户信息: {}", json);
        user = JsonUtils.parseObject(json, LoginUser.class);
        setLoginUser(request, user);
        return user;
    }

    /**
     * 将登陆信息写入到请求属性中
     * 
     * @param request HttpServletRequest
     * @param user 登录信息
     */
    public static void setLoginUser(HttpServletRequest request, @Nonnull LoginUser user) {
        user.setUserType(UserTypeEnum.ADMIN.getValue());
        request.setAttribute(LoginUserUtils.ATTR_LOGIN_USER, user);
        if (user.getUserType() == null) {
            log.error("用户信息有误, userType为null: {}", user);
        }
        request.setAttribute(LoginUserUtils.ATTR_LOGIN_USER_TYPE, user.getUserType());
    }

    /**
     * 获得当前用户的类型
     * 注意：该方法仅限于 web 相关的 framework 组件使用！！！
     *
     * @param request 请求
     * @return 用户编号
     */
    public static UserTypeEnum getLoginUserType(HttpServletRequest request) {
        // 1. 优先，从 Attribute 中获取
        Integer userType = (Integer) request.getAttribute(LoginUserUtils.ATTR_LOGIN_USER_TYPE);
        if (userType != null) {
            return UserTypeEnum.valueOf(userType);
        }

        // 2. 从 HEADER 中获取
        String header = request.getHeader(LoginUserUtils.HEADER_LOGIN_USER_TYPE);
        if (StringUtils.isNotBlank(header)) {
            return UserTypeEnum.valueOf(NumberUtils.toInt(header));
        }

        return null;
    }

    /**
     * 获取当前登录用户
     * 
     * @return
     */
    public static LoginUser getLoginUser() {
        HttpServletRequest request = getRequest();
        LoginUser user = getLoginUser(request);
        return user;
    }

    /**
     * 获取当前登录用户ID
     * 
     * @return 登录用户ID
     */
    public static Long getLoginUserId() {
        HttpServletRequest request = getRequest();
        LoginUser user = getLoginUser(request);
        return ObjectUtil.isNull(user) ? null : user.getId();
    }

    /**
     * 当前的请求request
     * 
     * @return
     */
    public static HttpServletRequest getRequest() {
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        if (!(requestAttributes instanceof ServletRequestAttributes)) {
            return null;
        }
        ServletRequestAttributes servletRequestAttributes = (ServletRequestAttributes) requestAttributes;
        return servletRequestAttributes.getRequest();
    }
}
