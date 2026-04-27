package com.jasolar.mis.framework.datapermission.core.rpc;

import com.jasolar.mis.framework.datapermission.core.aop.DataPermissionContextHolder;
import com.jasolar.mis.framework.datapermission.core.scope.RequestContextHolder;

import feign.RequestInterceptor;
import feign.RequestTemplate;

/**
 * 如果禁用了数据权限,则需要传递到下游
 * 
 * @author galuo
 * @date 2025-03-04 18:40
 *
 */
public class DataPermissionInterceptor implements RequestInterceptor {

    @Override
    public void apply(RequestTemplate requestTemplate) {
        if (DataPermissionContextHolder.isDisabled()) {
            // 是否禁用
            requestTemplate.header(DataPermissionWebFilter.HEADER_DISABLED, Boolean.TRUE.toString());
        }
        if (RequestContextHolder.readWrite() != null) {
            // 读/写权限
            requestTemplate.header(RequestContextHolder.READWRITE_HEADER_NAME, Integer.toString(RequestContextHolder.readWrite().value()));
        }
    }

}
