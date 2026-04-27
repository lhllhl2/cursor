package com.jasolar.mis.framework.rpc.core.interceptor;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import com.jasolar.mis.framework.common.security.LoginServletUtils;
import com.jasolar.mis.framework.common.security.LoginUser;
import com.jasolar.mis.framework.common.security.LoginUserUtils;
import com.jasolar.mis.framework.common.util.json.JsonUtils;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import lombok.extern.slf4j.Slf4j;

/**
 * LoginUser 的 RequestInterceptor 实现类：Feign 请求时，将 {@link LoginUser} 设置到 header 中，继续透传给被调用的服务
 *
 * @author zhaohuang
 */
@Slf4j
public class LoginUserRequestInterceptor implements RequestInterceptor {

    /** 内部feign请求header */
    public static final String HEADER_FEIGN = "x-fii-feign";

    @Override
    public void apply(RequestTemplate template) {
        template.header(HEADER_FEIGN, Boolean.TRUE.toString());
        LoginUser user = LoginServletUtils.getLoginUser();
        if (user == null) {
            return;
        }

        String header = URLEncoder.encode(JsonUtils.toJsonString(user), StandardCharsets.UTF_8); // 编码，避免中文乱码
        template.header(LoginUserUtils.HEADER_LOGIN_USER, header);
    }

}
