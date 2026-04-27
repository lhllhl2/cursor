package com.jasolar.mis.framework.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 文档地址
 *
 * @author zhaohuang
 */
@Getter
@AllArgsConstructor
public enum DocumentEnum {

    REDIS_INSTALL("https://www.yuque.com/xiangdong-m7nzy/rr/rk9z40n8v7hveron?singleDoc#", "Redis 安装文档"),
    TENANT("https://www.yuque.com/xiangdong-m7nzy/rr/rk9z40n8v7hveron?singleDoc#", "SaaS 多租户文档");
    
    private final String url;
    private final String memo;

}
