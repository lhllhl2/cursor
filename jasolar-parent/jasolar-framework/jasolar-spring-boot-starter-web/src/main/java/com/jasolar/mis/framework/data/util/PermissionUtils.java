package com.jasolar.mis.framework.data.util;

import java.util.Collections;
import java.util.Set;

import org.redisson.api.RSet;

import com.jasolar.mis.framework.common.pojo.CommonResult;
import com.jasolar.mis.framework.common.util.spring.SpringUtils;
import com.jasolar.mis.framework.data.RedissonUtils;
import com.jasolar.mis.module.system.api.permission.PermissionApi;

/**
 * 主要用于字段权限, 接口权限通过filter拦截
 * 
 * @author galuo
 * @date 2025-04-10 14:57
 *
 */
public class PermissionUtils extends RedissonUtils {

    /** 权限API Feign客户端 */
    private static PermissionApi API;

    /** 按用户账号缓存前缀 */
    public static final String PREFIX = DATA_KEY_PREFIX + "SYSTEM:PERMISSIONS:USERNO:";

    /** 初始化, 用于注入依赖的Spring Bean等 */
    public static void init() {
        API = SpringUtils.getBean(PermissionApi.class);
    }

    /**
     * 查询指定用户的权限标识
     * 
     * @param userNo
     * @return
     */
    static final RSet<String> getPermissions(String userNo) {
        RSet<String> set = REDISSON.getSet(PREFIX + userNo);
        return set;
    }

    /**
     * 加载分类的所有字典数据
     * 
     * @param type 字典分类
     * @return 分类下的所有字典
     */
    public static Set<String> load(String userNo) {
        // 通过api读取
        CommonResult<Set<String>> r = API.findUserPermissions(userNo);
        if (r == null || r.isError()) {
            return Collections.emptySet();
        }

        RSet<String> set = getPermissions(userNo);
        Set<String> permissions = r.getData();
        if (permissions != null) {
            set.addAll(permissions);
        }
        set.expire(REDIS_DURATION);

        return permissions == null ? Collections.emptySet() : permissions;
    }

    /**
     * 判断人员是否有指定的任意一个权限标识
     * 
     * @param userNo 用户工号
     * @param permissions 要判断的权限标识
     * @return
     */
    public static boolean hasAnyPermission(String userNo, String... permissions) {
        RSet<String> set = getPermissions(userNo);
        Set<String> caches = set;
        if (!set.isExists()) {
            caches = load(userNo);
        }

        if (caches.isEmpty()) {
            return false;
        }

        for (String permission : permissions) {
            if (caches.contains(permission)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 删除人员的权限缓存
     * 
     * @param userNo 人员工号
     */
    public static void removeCache(String userNo) {
        getPermissions(userNo).delete();
    }

    /** 清空缓存 */
    public static void clear() {
        clear(PREFIX);
    }
}
