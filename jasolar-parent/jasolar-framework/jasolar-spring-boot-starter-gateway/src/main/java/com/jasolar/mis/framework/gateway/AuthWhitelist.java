package com.jasolar.mis.framework.gateway;

import java.util.Set;
import java.util.TreeSet;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;

import lombok.Data;

/**
 * 冒烟测试时开启登录白名单
 * 
 * @author galuo
 * @date 2025-06-18 15:05
 *
 */
@Data
@RefreshScope
@ConfigurationProperties(prefix = "jasolar.security.auth-whitelist")
public class AuthWhitelist {

    /** 是否开启白名单登录 */
    private boolean enabled;

    /** 开启白名单后,允许的白名单账号 */
    private final Set<String> users = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);

    /**
     * 是否允许账号登录
     * 
     * @param userNo 登录的账号
     * @return 允许则返回true,否则返回false
     */
    public boolean accept(String userNo) {
        return this.enabled && this.users.contains(userNo);
    }
}
