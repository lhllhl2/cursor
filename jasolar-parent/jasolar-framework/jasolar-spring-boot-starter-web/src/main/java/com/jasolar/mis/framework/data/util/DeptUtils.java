package com.jasolar.mis.framework.data.util;

import cn.hutool.core.util.ObjectUtil;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.jasolar.mis.framework.common.util.cache.CacheUtils;
import com.jasolar.mis.framework.data.RedissonUtils;
import com.jasolar.mis.framework.data.core.Dept;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RBucket;

/**
 * 部门信息工具类
 * 
 * @author galuo
 * @date 2025-03-28 11:45
 *
 */
@Slf4j
public class DeptUtils extends RedissonUtils {

    /** load不存在时返回的默认值 */
    private static final Dept NULL = new Dept();

    /** 用于获取部门数据 */
    // public static DeptApi API;

    /** 按CODE缓存部门数据前缀 */
    public static final String PREFIX = DATA_KEY_PREFIX + "SYSTEM:DEPTS:CODE:";

    /** 内存缓存 */
    private static final LoadingCache<String, Dept> CACHE = CacheUtils.buildAsyncReloadingCache(MEMORY_DURATION,
            // 缓存不存在时获取数据
            CacheLoader.from(userNo -> load(userNo)));

    /** 初始化, 用于注入依赖的Spring Bean等 */
    public static void init() {
        // API = SpringUtils.getBean(DeptApi.class);

        // 部门数据有3W多条,不全部初始化
        // try {
        // loadAll();
        // } catch (Exception ignore) {
        // log.warn("初始化所有部门缓存失败", ignore);
        // }
    }

    /**
     * 缓存对象
     * 
     * @param deptCode 工号
     * @return 缓存对象
     */
    static final RBucket<Dept> getBucket(String deptCode) {
        RBucket<Dept> bucket = REDISSON.getBucket(PREFIX + deptCode.toUpperCase());
        return bucket;
    }

    /**
     * 查询单个部门, 优先从内存中查询
     * 
     * @param deptCode 部门编码
     * @return 部门信息
     */
    @SneakyThrows
    public static Dept get(String deptCode) {
        if (StringUtils.isBlank(deptCode)) {
            return null;
        }
        return nullIfDefault(CACHE.get(deptCode), NULL);
    }

    @SneakyThrows
    public static String getName(String deptCode) {
        if (StringUtils.isBlank(deptCode)) {
            return null;
        }
        Dept dept = get(deptCode);
        return ObjectUtil.isNull(dept) ? null : dept.getDeptName();
    }

    /**
     * 查询单个部门
     * 
     * @param deptCode 部门编码
     * @return 部门信息
     */
    public static Dept load(String deptCode) {
        RBucket<Dept> bucket = getBucket(deptCode);
        if (bucket.isExists()) {
            return bucket.get();
        }

        log.warn("DeptApi已被删除，无法查询部门信息: {}", deptCode);
        return NULL;
    }



    /**
     * 删除指定部门的缓存
     * 
     * @param deptCode 部门编码
     */
    public static void removeCache(String deptCode) {
        CACHE.invalidate(deptCode);
        getBucket(deptCode).delete();
    }

    /** 清空缓存 */
    public static void clear() {
        CACHE.invalidateAll();
        clear(PREFIX);
    }
}
