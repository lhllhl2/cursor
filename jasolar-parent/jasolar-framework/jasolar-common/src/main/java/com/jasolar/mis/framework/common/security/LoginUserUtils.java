package com.jasolar.mis.framework.common.security;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

import com.jasolar.mis.framework.common.enums.UserTypeEnum;
import com.jasolar.mis.framework.common.security.LoginUser.LoginUserBuilder;
import com.jasolar.mis.framework.common.util.json.JsonUtils;

import cn.hutool.core.text.StrPool;
import jakarta.validation.constraints.NotNull;

/**
 * 登录相关
 * 
 * @author galuo
 * @date 2025-03-18 09:42
 *
 */
public interface LoginUserUtils {

    /** HEADER 认证头 value 的前缀 */
    String AUTHORIZATION_BEARER = "Bearer ";

    /** 请求中是否没有token,如果此参数为ture,则表明网关已经删除了token,后续不要使用 */
    String HEADER_TOKEN_REMOVED = "x-token-removed";

    /** 存放整个人员信息的请求头,从网关传递到后台服务 */
    String HEADER_LOGIN_USER = "x-login-user";

    /** 存放用户类型的请求头,从网关传递到后台服务 */
    String HEADER_LOGIN_USER_TYPE = "x-login-user-type";

    /** 存放整个人员信息的请求头,从网关传递到后台服务 */
    String ATTR_LOGIN_USER = "user";

    /** 存放人员类型的请求头 */
    String ATTR_LOGIN_USER_TYPE = "userType";

    /**
     * 将登录人员数据转换为请求头使用的参数值
     * 
     * @param user 登录信息
     * @return 可写入请求头的数据,使用{@link URLEncoder#encode(String, java.nio.charset.Charset)}方法进行编码
     */
    static String toRequestHeader(LoginUser user) {
        return URLEncoder.encode(JsonUtils.toJsonString(user), StandardCharsets.UTF_8); // 编码，避免中文乱码
    }

    /**
     * 根据token生成mock用户. 注意在底层filter中需要先判断是否开启了mock
     * 
     * @param token mock的字符串,必须以{@link #}开头, 格式为JSON或者字符串:usreId,userNo(,userType默认为0)
     * @param prefix 配置的mock token前缀
     * 
     * @return mock用户信息或null
     */
    static LoginUser mock(String token, @NotNull String prefix) {
        if (StringUtils.isBlank(token)) {
            return null;
        }
        if (!StringUtils.startsWithIgnoreCase(token, prefix)) {
            return null;
        }
        token = token.substring(prefix.length()).trim();
        LoginUser user = null;
        if (JsonUtils.isJson(token)) {
            // JSON
            user = JsonUtils.parseObject(token, LoginUser.class);
        } else {
            // 至少2个字段
            int userIdIndex = 0;
            int userNoIndex = 1;
            int userTypeIndex = 2;
            // 构建模拟用户
            String[] arr = token.split(StrPool.COMMA);
            if (arr.length <= userNoIndex) {
                // 模拟用户必须配置id和no
                return null;
            }

            LoginUserBuilder builder = LoginUser.builder().id(NumberUtils.toLong(arr[userIdIndex])).no(arr[userNoIndex]);
            if (arr.length > userTypeIndex) {
                builder.userType(NumberUtils.toInt(arr[userTypeIndex]));
            }
            user = builder.build();
        }
        // mock token长期有效
        if (user.getExpiresTime() == null) {
            user.setExpiresTime(LocalDateTime.now().plusDays(1L));
        }
        if (user.getUserType() == null) {
            // 默认为管理员账号
            user.setUserType(UserTypeEnum.ADMIN.getValue());
        }

        return user;
    }
}
