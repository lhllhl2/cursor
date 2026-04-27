package com.jasolar.mis.framework.security.core.filter;

import java.io.IOException;

import org.apache.commons.lang3.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import com.jasolar.mis.framework.common.security.LoginServletUtils;
import com.jasolar.mis.framework.common.security.LoginUser;
import com.jasolar.mis.framework.common.security.LoginUserUtils;
import com.jasolar.mis.framework.common.security.SecurityProperties;
import com.jasolar.mis.framework.security.core.util.SecurityFrameworkUtils;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Token 过滤器，验证 token 的有效性
 * 验证通过后，获得 {@link LoginUser} 信息，并加入到 Spring Security 上下文
 *
 * @author zhaohuang
 */
@RequiredArgsConstructor
@Slf4j
public class TokenAuthenticationFilter extends OncePerRequestFilter {

    private final SecurityProperties props;

    // private final GlobalExceptionHandler globalExceptionHandler;
    //
    // private final OAuth2TokenApi oauth2TokenApi;
    //
    // private final SecurityExceptionHandler securityExceptionHandler;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        // 情况一，基于 header[login-user] 获得用户，例如说来自 Gateway 或者其它服务透传
        LoginUser loginUser = LoginServletUtils.getLoginUser(request);

        // 情况二，基于 Token 获得用户
        // 注意，这里主要满足直接使用 Nginx 直接转发到 Spring Cloud 服务的场景。
        if (!loginUser.isAuthorized()) {
            String token = LoginServletUtils.getToken(request, props.getTokenHeader(), props.getTokenParameter());
            if (StringUtils.isNotBlank(token) && props.getMockEnable()) {
                // 本地swagger请求时可以跳过网关传入mock token直接调用
                loginUser = LoginUserUtils.mock(token, props.getMockSecret());
            }
        }

        // 设置当前用户
        if (loginUser == null || loginUser.isAuthorized()) {
            SecurityFrameworkUtils.setLoginUser(loginUser, request);
        }
        // 继续过滤链
        chain.doFilter(request, response);
    }

    // private LoginUser buildLoginUserByToken(String token, Integer userType) {
    // try {
    // // 校验访问令牌
    // OAuth2AccessTokenCheckRespDTO accessToken = oauth2TokenApi.checkAccessToken(token).getCheckedData();
    // if (accessToken == null) {
    // return null;
    // }
    // // 用户类型不匹配，无权限
    // // 注意：只有 /admin-api/* 和 /app-api/* 有 userType，才需要比对用户类型
    // // 类似 WebSocket 的 /ws/* 连接地址，是不需要比对用户类型的
    // if (userType != null && ObjectUtil.notEqual(accessToken.getUserType(), userType)) {
    // throw new AccessDeniedException("错误的用户类型");
    // }
    // // 构建登录用户
    // return LoginUser.builder().id(accessToken.getUserId()).no(accessToken.getUserNo()).name(accessToken.getUserName())
    // .userType(accessToken.getUserType()) // 额外的用户信息
    // .tenantId(accessToken.getTenantId()).scopes(accessToken.getScopes()).expiresTime(accessToken.getExpiresTime()).build();
    // } catch (ServiceException serviceException) {
    // // 校验 Token 不通过时，考虑到一些接口是无需登录的，所以直接返回 null 即可
    // return null;
    // }
    // }

}
