package com.jasolar.mis.framework.web.core.util;

import java.util.Arrays;
import java.util.List;

import cn.hutool.core.util.ObjectUtil;
import jodd.util.StringUtil;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.jasolar.mis.framework.common.enums.RpcConstants;
import com.jasolar.mis.framework.common.enums.TerminalEnum;
import com.jasolar.mis.framework.common.enums.UserTypeEnum;
import com.jasolar.mis.framework.common.pojo.CommonResult;
import com.jasolar.mis.framework.common.security.LoginServletUtils;
import com.jasolar.mis.framework.common.security.LoginUser;
import com.jasolar.mis.framework.web.config.WebProperties;
import com.github.xingfudeshi.knife4j.core.util.CollectionUtils;

import cn.hutool.core.net.NetUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.extra.servlet.ServletUtil;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.HttpServletRequest;

/**
 * 专属于 web 包的工具类
 *
 * @author zhaohuang
 */
public class WebFrameworkUtils {

    // private static final String REQUEST_ATTRIBUTE_LOGIN_USER_ID = "login_user_id";
    // private static final String REQUEST_ATTRIBUTE_LOGIN_USER_NO = "login_user_no";
    // private static final String REQUEST_ATTRIBUTE_LOGIN_USER_TYPE = "login_user_type";
    private static final String REQUEST_ATTRIBUTE_COMMON_RESULT = "common_result";

    public static final String HEADER_TENANT_ID = "tenant-id";

    /**
     * 终端的 Header
     *
     * @see com.fiifoxconn.mis.framework.common.enums.TerminalEnum
     */
    public static final String HEADER_TERMINAL = "terminal";

    private static WebProperties properties;

    public WebFrameworkUtils(WebProperties webProperties) {
        WebFrameworkUtils.properties = webProperties;
    }

    /**
     * 获得租户编号，从 header 中
     * 考虑到其它 framework 组件也会使用到租户编号，所以不得不放在 WebFrameworkUtils 统一提供
     *
     * @param request 请求
     * @return 租户编号
     */
    public static Long getTenantId(HttpServletRequest request) {
        String tenantId = request.getHeader(HEADER_TENANT_ID);
        return NumberUtil.isNumber(tenantId) ? Long.valueOf(tenantId) : null;
    }

    /**
     * 获取当前登录用户
     * 
     * @return
     */
    public static LoginUser getLoginUser() {
        HttpServletRequest request = getRequest();
        return LoginServletUtils.getLoginUser(request);
    }

    /**
     * 获得当前用户的编号，从请求中
     * 注意：该方法仅限于 framework 框架使用！！！
     *
     * @param request 请求
     * @return 用户ID
     */
    public static Long getLoginUserId(HttpServletRequest request) {
        LoginUser user = LoginServletUtils.getLoginUser(request);
        return ObjectUtil.isNull(user) ? null : user.getId();
    }

    /**
     * 获得当前用户的编号，从请求中
     * 注意：该方法仅限于 framework 框架使用！！！
     *
     * @param request 请求
     * @return 用户编号
     */
    public static String getLoginUserNo(HttpServletRequest request) {
        LoginUser user = LoginServletUtils.getLoginUser(request);
        return ObjectUtil.isNull(user)  ? null : user.getNo();
    }

    /**
     * 获得当前用户的类型
     * 注意：该方法仅限于 web 相关的 framework 组件使用！！！
     *
     * @param request 请求
     * @return 用户编号
     */
    public static Integer getLoginUserType(HttpServletRequest request) {
        if (ObjectUtil.isNull(request)) {
            return UserTypeEnum.ADMIN.getValue();
        }
        UserTypeEnum type = LoginServletUtils.getLoginUserType(request);
        if (ObjectUtil.isNull(type) && StringUtil.isNotBlank(request.getServletPath())) {
            // 2. 其次，基于 URL 前缀的约定
            if (request.getServletPath().startsWith(properties.getAdminApi().getPrefix())) {
                return UserTypeEnum.ADMIN.getValue();
            }
            if (request.getServletPath().startsWith(properties.getAppApi().getPrefix())) {
                return UserTypeEnum.SUPPLIER.getValue();
            }
        }
        // 默认返回admin
        return ObjectUtil.isNull(type) ? UserTypeEnum.ADMIN.getValue() : type.getValue();
    }

    public static Integer getLoginUserType() {
        HttpServletRequest request = getRequest();
        return getLoginUserType(request);
    }

    public static Long getLoginUserId() {
        HttpServletRequest request = getRequest();
        return getLoginUserId(request);
    }

    public static String getLoginUserNo() {
        HttpServletRequest request = getRequest();
        return getLoginUserNo(request);
    }

    public static Integer getTerminal() {
        HttpServletRequest request = getRequest();
        if (request == null) {
            return TerminalEnum.UNKNOWN.getTerminal();
        }
        String terminalValue = request.getHeader(HEADER_TERMINAL);
        return NumberUtil.parseInt(terminalValue, TerminalEnum.UNKNOWN.getTerminal());
    }

    public static void setCommonResult(ServletRequest request, CommonResult<?> result) {
        request.setAttribute(REQUEST_ATTRIBUTE_COMMON_RESULT, result);
    }

    public static CommonResult<?> getCommonResult(ServletRequest request) {
        return (CommonResult<?>) request.getAttribute(REQUEST_ATTRIBUTE_COMMON_RESULT);
    }

    public static HttpServletRequest getRequest() {
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        if (!(requestAttributes instanceof ServletRequestAttributes)) {
            return null;
        }
        ServletRequestAttributes servletRequestAttributes = (ServletRequestAttributes) requestAttributes;
        return servletRequestAttributes.getRequest();
    }

    /**
     * 判断是否为 RPC 请求
     *
     * @param request 请求
     * @return 是否为 RPC 请求
     */
    public static boolean isRpcRequest(HttpServletRequest request) {
        return request.getRequestURI().startsWith(RpcConstants.RPC_API_PREFIX);
    }

    /**
     * 判断是否为 RPC 请求
     *
     * 约定大于配置，只要以 Api 结尾，都认为是 RPC 接口
     *
     * @param className 类名
     * @return 是否为 RPC 请求
     */
    public static boolean isRpcRequest(String className) {
        return className.endsWith("Api");
    }

    /** 获取客户端IP的默认请求头 */
    public static final List<String> DEFAULT_CLIENT_IP_HEADERS = Arrays.asList("X-Forwarded-For", "X-Real-IP", "Proxy-Client-IP",
            "WL-Proxy-Client-IP", "HTTP_CLIENT_IP", "HTTP_X_FORWARDED_FOR");

    /**
     * 获得客户端 IP
     * <p>
     * 参考 {@link ServletUtil} 的 getClientIP 方法
     *
     * @param exchange 请求
     * @param otherHeaderNames 其它 header 名字的数组
     * @return 客户端 IP
     */
    public static String getClientIP(HttpServletRequest request) {
        // 方式一，通过 header 获取
        List<String> headers = properties == null ? DEFAULT_CLIENT_IP_HEADERS : properties.getClientIpHeaders();
        if (CollectionUtils.isEmpty(headers)) {
            headers = DEFAULT_CLIENT_IP_HEADERS;
        }
        String ip;
        for (String header : headers) {
            ip = request.getHeader(header);
            if (!NetUtil.isUnknown(ip)) {
                return NetUtil.getMultistageReverseProxyIp(ip);
            }
        }
        // 方式二，通过 remoteAddress 获取
        ip = request.getRemoteAddr();
        return NetUtil.getMultistageReverseProxyIp(ip);
    }
}
