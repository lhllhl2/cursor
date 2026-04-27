package com.jasolar.mis.framework.web.core.filter;

import cn.hutool.core.text.CharSequenceUtil;
import com.jasolar.mis.framework.web.config.WebProperties;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * 过滤 /admin-api、/app-api 等 API 请求的过滤器
 *
 * @author zhaohuang
 */
@RequiredArgsConstructor
public abstract class ApiRequestFilter extends OncePerRequestFilter {

    protected final WebProperties webProperties;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        // 只过滤 API 请求的地址
        return !CharSequenceUtil.startWithAny(request.getRequestURI(),
                webProperties.getAdminApi().getPrefix(),
                webProperties.getAppApi().getPrefix(),
                webProperties.getOauthApi().getPrefix(),
                webProperties.getBudgetApi().getPrefix()
        );
    }

}
