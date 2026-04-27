package com.jasolar.mis.framework.common.security;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.jasolar.mis.framework.common.enums.UserTypeEnum;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 统一封装的登录用户信息, 包括采购用户和供应商
 *
 * @author zhaohuang
 */
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class LoginUser {

    /** 用于非HTTP请求如MQ监听,异步调用等情况获取用户信息 */
    static final LoginUser SYSTEM = LoginUser.builder().id(0L).no("system").name("system").userType(0)
            .expiresTime(LocalDateTime.of(9999, 12, 31, 0, 0)).build();

    /** request中没有用户信息则返回匿名用户 */
    static final LoginUser ANONYMOUS = LoginUser.builder().id(-1L).no("anonymous").name("anonymous").userType(0)
            .expiresTime(LocalDateTime.of(9999, 12, 31, 0, 0)).build();

    /** 用户ID */
    private Long id;

    /** 用户账号 */
    private String no;

    /** 用户名称 */
    private String name;

    /**
     * 用户类型
     *
     * 关联 {@link UserTypeEnum}
     */
    private Integer userType;

    /** 采购平台用户信息. 当userType等于{@link UserTypeEnum#ADMIN}时有效 */
    private LoginAdminUser user;

    /** 供应商信息. 当userType等于{@link UserTypeEnum#SUPPLIER}时有效 */
    private LoginSupplier supplier;

    // /** 额外的用户信息 */
    // private Map<String, String> info;

    /** 租户编号 */
    private Long tenantId;
    /** 授权范围 */
    private List<String> scopes;
    /** 过期时间 */
    private LocalDateTime expiresTime;

    /**
     * 是否已经过期
     * 
     * @return {@link LoginUser#expiresTime}是否小于当前时间
     */
    @JsonIgnore
    public boolean isExpired() {
        return expiresTime != null && expiresTime.isBefore(LocalDateTime.now());
    }

    /**
     * 是否未登录用户
     * 
     * @return {@link ANONYMOUS} == this
     */
    @JsonIgnore
    public boolean isAnonymous() {
        return this == ANONYMOUS || Objects.equals(this.id, ANONYMOUS.id);
    }

    /**
     * 是否有效的用户, 未超时,且非匿名用户
     * 
     * @return 未超时,且非匿名用户则返回true,否则返回false
     */
    @JsonIgnore
    public boolean isAuthorized() {
        return !this.isExpired() && !this.isAnonymous();
    }

    /**
     * @return 用户类型枚举
     */
    public UserTypeEnum userType() {
        return UserTypeEnum.valueOf(userType);
    }

    // /**
    // * 写入参数
    // *
    // * @param key 参数名
    // * @param value 参数值，如果为null则删除参数
    // * @return this
    // */
    // public LoginUser infoUser(String key, String value) {
    // if (info == null) {
    // info = new HashMap<>();
    // }
    // if (value == null) {
    // info.remove(key);
    // } else {
    // info.put(key, value);
    // }
    //
    // return this;
    // }
    //
    // /**
    // * 获取参数的值
    // *
    // * @param key 参数名
    // * @return 参数值
    // */
    // public String info(String key) {
    // return info == null ? null : info.get(key);
    // }
    //
    // // ========== 上下文 ==========
    // /**
    // * 上下文字段，不进行持久化
    // *
    // * 1. 用于基于 LoginUser 维度的临时缓存
    // */
    // @JsonIgnore
    // private Map<String, Object> context;
    //
    // public void setContext(String key, Object value) {
    // if (context == null) {
    // context = new HashMap<>();
    // }
    // context.put(key, value);
    // }
    //
    // public <T> T getContext(String key, Class<T> type) {
    // return MapUtil.get(context, key, type);
    // }

}
