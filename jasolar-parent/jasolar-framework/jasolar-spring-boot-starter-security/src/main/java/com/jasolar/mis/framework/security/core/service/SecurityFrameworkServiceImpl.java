package com.jasolar.mis.framework.security.core.service;

import lombok.AllArgsConstructor;

/**
 * 默认的 {@link SecurityFrameworkService} 实现类
 *
 * @author zhaohuang
 */
@AllArgsConstructor
public class SecurityFrameworkServiceImpl implements SecurityFrameworkService {
    @Override
    public boolean hasPermission(String permission) {
        return true;
    }

    @Override
    public boolean hasAnyPermissions(String... permissions) {
        return true;
    }

    @Override
    public boolean hasRole(String role) {
        return true;
    }

    @Override
    public boolean hasAnyRoles(String... roles) {
        return true;
    }

    @Override
    public boolean hasScope(String scope) {
        return true;
    }

    @Override
    public boolean hasAnyScopes(String... scope) {
        return true;
    }

    // private final PermissionApi permissionApi;
    //
    // /**
    // * 针对 {@link #hasAnyRoles(String...)} 的缓存
    // */
    // private final LoadingCache<KeyValue<Long, List<String>>, Boolean> hasAnyRolesCache = buildCache(Duration.ofMinutes(1L), // 过期时间 1 分钟
    // new CacheLoader<KeyValue<Long, List<String>>, Boolean>() {
    //
    // @Override
    // public Boolean load(KeyValue<Long, List<String>> key) {
    // return permissionApi.hasAnyRoles(key.getKey(), key.getValue().toArray(new String[0])).getCheckedData();
    // }
    //
    // });
    //
    // /**
    // * 针对 {@link #hasAnyPermissions(String...)} 的缓存
    // */
    // private final LoadingCache<KeyValue<Long, List<String>>, Boolean> hasAnyPermissionsCache = buildCache(Duration.ofMinutes(1L), // 过期时间
    // 1
    // // 分钟
    // new CacheLoader<KeyValue<Long, List<String>>, Boolean>() {
    //
    // @Override
    // public Boolean load(KeyValue<Long, List<String>> key) {
    // return permissionApi.hasAnyPermissions(key.getKey(), key.getValue().toArray(new String[0])).getCheckedData();
    // }
    //
    // });
    //
    // @Override
    // public boolean hasPermission(String permission) {
    // return hasAnyPermissions(permission);
    // }
    //
    // @Override
    // @SneakyThrows
    // public boolean hasAnyPermissions(String... permissions) {
    // LoginUser user = getLoginUser();
    // if (user == null || !user.isAuthorized()) {
    // return false;
    // }
    //
    // // 如果是供应商账号,直接视为有权限. 因为供应商账号没有后台账号,无法分配权限
    // if (UserTypeEnum.SUPPLIER == user.userType()) {
    // return true;
    // }
    //
    // return hasAnyPermissionsCache.get(new KeyValue<>(user.getId(), Arrays.asList(permissions)));
    // }
    //
    // @Override
    // public boolean hasRole(String role) {
    // return hasAnyRoles(role);
    // }
    //
    // @Override
    // @SneakyThrows
    // public boolean hasAnyRoles(String... roles) {
    // LoginUser user = getLoginUser();
    // if (user == null || !user.isAuthorized()) {
    // return false;
    // }
    //
    // // 如果是供应商账号,直接视为有权限. 因为供应商账号没有后台账号,无法分配权限
    // if (UserTypeEnum.SUPPLIER == user.userType()) {
    // return true;
    // }
    // return hasAnyRolesCache.get(new KeyValue<>(user.getId(), Arrays.asList(roles)));
    // }
    //
    // @Override
    // public boolean hasScope(String scope) {
    // return hasAnyScopes(scope);
    // }
    //
    // @Override
    // public boolean hasAnyScopes(String... scope) {
    // LoginUser user = getLoginUser();
    // if (!user.isAuthorized()) {
    // return false;
    // }
    // return CollUtil.containsAny(user.getScopes(), Arrays.asList(scope));
    // }

}
