package com.jasolar.mis.framework.env.core.fegin;

import cn.hutool.core.text.CharSequenceUtil;
import com.jasolar.mis.framework.env.core.context.EnvContextHolder;
import com.jasolar.mis.framework.env.core.util.EnvUtils;
import feign.RequestInterceptor;
import feign.RequestTemplate;

/**
 * 多环境的 {@link RequestInterceptor} 实现类：Feign 请求时，将 tag 设置到 header 中，继续透传给被调用的服务
 *
 * @author zhaohuang
 */
public class EnvRequestInterceptor implements RequestInterceptor {

    @Override
    public void apply(RequestTemplate requestTemplate) {
        String tag = EnvContextHolder.getTag();
        if (CharSequenceUtil.isNotEmpty(tag)) {
            EnvUtils.setTag(requestTemplate, tag);
        }
    }

}
