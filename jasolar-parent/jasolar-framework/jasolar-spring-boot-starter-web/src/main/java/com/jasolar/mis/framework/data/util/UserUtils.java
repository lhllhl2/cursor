package com.jasolar.mis.framework.data.util;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RBucket;
import org.springframework.util.CollectionUtils;

import com.jasolar.mis.framework.common.pojo.CommonResult;
import com.jasolar.mis.framework.common.util.cache.CacheUtils;
import com.jasolar.mis.framework.common.util.spring.SpringUtils;
import com.jasolar.mis.framework.data.RedissonUtils;
import com.jasolar.mis.framework.data.convert.UserConverter;
import com.jasolar.mis.framework.data.core.User;
// import com.jasolar.mis.module.system.api.user.AdminUserApi;
// import com.jasolar.mis.module.system.api.user.dto.AdminUserRespDTO;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.ImmutableMap;

import lombok.SneakyThrows;

/**
 * 人员信息工具类
 * 
 * @author galuo
 * @date 2025-03-28 11:45
 *
 */
public final class UserUtils extends RedissonUtils {

    /** load不存在时返回的默认值 */
    private static final User NULL = new User();

    /** 用于获取用户数据 */
    // public static AdminUserApi API;

    /** 按NO缓存人员数据 */
    public static final String PREFIX = DATA_KEY_PREFIX + "SYSTEM:USERS:NO:";

    /** 按ID缓存人员工号前缀 */
    public static final String PREFIX_USER_NO = DATA_KEY_PREFIX + "SYSTEM:USERS:ID:";

    /** 使用工号缓存 */
    private static final LoadingCache<String, User> CACHE = CacheUtils.buildAsyncReloadingCache(Duration.ofMinutes(20L),
            CacheLoader.from(userNo -> load(userNo)));

    /** 使用工号缓存 */
    private static final LoadingCache<Long, String> CACHE_USER_NO = CacheUtils.buildAsyncReloadingCache(Duration.ofMinutes(20L),
            CacheLoader.from(userId -> loadUserNo(userId)));

    /** 初始化, 用于注入依赖的Spring Bean等 */
    public static void init() {
        // API = SpringUtils.getBean(AdminUserApi.class);

        // TODO 将常用用户同步到缓存中. 将有分配角色的账号添加到缓存.
        // 从HR同步的账号信息很多,绝大部分人员在系统中应该是没有权限的
    }

    /**
     * 缓存对象
     * 
     * @param userNo 工号
     * @return 缓存对象
     */
    private static final RBucket<User> getBucket(String userNo) {
        RBucket<User> bucket = REDISSON.getBucket(PREFIX + userNo.toUpperCase());
        return bucket;
    }

    /**
     * 使用ID缓存工号的RBucket对象
     * 
     * @param userId ID
     * @return 缓存对象
     */
    private static final RBucket<String> getUserNoBucket(Long userId) {
        RBucket<String> bucket = REDISSON.getBucket(PREFIX_USER_NO + userId);
        return bucket;
    }

    /**
     * 根据用户ID获取用户工号
     * 
     * @param userId
     * @return
     */
    private static String loadUserNo(Long userId) {
        RBucket<String> bucket = getUserNoBucket(userId);
        if (bucket.isExists()) {
            return bucket.get();
        }
        User user = loadById(userId);
        return user.getUserNo();
    }

    /**
     * 查询单个用户, 优先从内存中查询
     * 
     * @param userNo 工号
     * @return 用户信息
     * @deprecated 使用{@link #get(String)}方法, 统一所有缓存命名的风格
     */
    @SneakyThrows
    @Deprecated
    public static User getUser(String userNo) {
        if (StringUtils.isBlank(userNo)) {
            return null;
        }
        return get(userNo);
    }

    /**
     * 通过账号获取名称
     * 
     * @param userNo 账号
     * @return 名称
     */
    @SneakyThrows
    public static String getName(String userNo) {
        if (StringUtils.isBlank(userNo)) {
            return null;
        }
        User user = get(userNo);
        return user == null ? null : user.getUserName();
    }

    /**
     * 通过工号查询单个用户, 优先从内存中查询
     * 
     * @param userNo 工号
     * @return 用户信息
     */
    @SneakyThrows
    public static User get(String userNo) {
        if (StringUtils.isBlank(userNo)) {
            return null;
        }
        return nullIfDefault(CACHE.get(userNo), NULL);
    }

    /**
     * 通过ID查询单个用户, 优先从内存中查询
     * 
     * @param userId ID
     * @return 用户信息
     */
    @SneakyThrows
    public static User get(Long userId) {
        if (userId == null) {
            return null;
        }

        String userNo = getUserNo(userId);
        return get(userNo);
    }

    /**
     * 根据用户ID获取用户工号
     * 
     * @param userId
     * @return
     */
    @SneakyThrows
    public static String getUserNo(Long userId) {
        return CACHE_USER_NO.get(userId);
    }

    /**
     * 获取指定用户编号的所有用户信息。
     *
     * @param userNos 用户编号列表
     * @return 用户编号与用户信息的映射
     */
    @SneakyThrows
    public static Map<String, User> getAll(List<String> userNos) {
        ImmutableMap<String, User> caches = CACHE.getAllPresent(userNos);
        List<String> uncaches = userNos.stream().filter(c -> !caches.containsKey(c)).toList();
        // 通过API查询 - 暂时注释掉，因为AdminUserApi已被删除
        // if (!CollectionUtils.isEmpty(uncaches)) {
        //     CommonResult<List<AdminUserRespDTO>> list = API.getUsers(uncaches);
        //     if (list.isSuccess() && !CollectionUtils.isEmpty(list.getData())) {
        //         list.getData().parallelStream().forEach(dto -> {
        //             User user = putRedis(dto);
        //             CACHE.put(user.getUserNo(), user);
        //         });
        //     }
        // }

        return removeIf(CACHE.getAll(userNos), e -> NULL.equals(e.getValue()));
    }

    /**
     * 获取指定用户ID的所有用户信息。
     *
     * @param userIds 用户ID列表
     * @return 用户ID与用户信息的映射
     */
    @SneakyThrows
    public static Map<String, User> getAllById(Collection<Long> userIds) {
        ImmutableMap<Long, String> caches = CACHE_USER_NO.getAllPresent(userIds);
        List<Long> uncaches = userIds.stream().filter(c -> !caches.containsKey(c)).toList();
        List<String> userNos = new ArrayList<>(caches.values());
        // 通过API查询 - 暂时注释掉，因为AdminUserApi已被删除
        // if (!CollectionUtils.isEmpty(uncaches)) {
        //     CommonResult<List<AdminUserRespDTO>> list = API.getUserList(uncaches);
        //     if (list.isSuccess() && !CollectionUtils.isEmpty(list.getData())) {
        //         list.getData().parallelStream().forEach(dto -> {
        //             User user = putRedis(dto);
        //             CACHE.put(user.getUserNo(), user);
        //             CACHE_USER_NO.put(user.getId(), user.getUserNo());
        //             userNos.add(user.getUserNo());
        //         });
        //     }
        // }
        return removeIf(CACHE.getAll(userNos), e -> NULL.equals(e.getValue()));
    }

    /**
     * 查询单个用户
     * 
     * @param userNo 工号
     * @return 用户信息, 如果不存在会返回一个仅有工号的数据
     */
    private static User load(String userNo) {
        RBucket<User> bucket = getBucket(userNo);
        if (bucket.isExists()) {
            return bucket.get();
        }

        // 通过api读取 - 暂时注释掉，因为AdminUserApi已被删除
        // CommonResult<AdminUserRespDTO> r = API.getUser(userNo);
        // if (r == null || r.isError()) {
        //     return NULL;
        // }
        // return putRedis(r.getData());
        
        // 暂时返回NULL，因为AdminUserApi已被删除
        return NULL;
    }

    /**
     * 查询单个用户
     * 
     * @param userNo 工号
     * @return 用户信息, 如果不存在会返回一个仅有工号的数据
     */
    private static User loadById(Long userId) {
        // 通过api读取 - 暂时注释掉，因为AdminUserApi已被删除
        // CommonResult<AdminUserRespDTO> r = API.getUser(userId);
        // if (r == null || r.isError()) {
        //     return NULL;
        // }
        // return putRedis(r.getData());
        
        // 暂时返回NULL，因为AdminUserApi已被删除
        return NULL;
    }

    /**
     * 
     * 增加一个用户信息到缓存, 并返回用户信息
     * 
     * @param dto 从system查询到的用户DTO
     * @return 转换后的用户
     */
    // private static User putRedis(AdminUserRespDTO dto) {
    //     User user = UserConverter.INSTANCE.convert(dto);
    //     RBucket<User> bucket = getBucket(user.getUserNo());
    //     bucket.set(user, REDIS_DURATION);

    //     RBucket<String> b = getUserNoBucket(user.getId());
    //     b.set(user.getUserNo(), REDIS_DURATION);
    //     return user;
    // }

    /**
     * 删除指定用户的缓存
     * 
     * @param userNo 用户工号
     */
    public static void removeCache(String userNo) {
        CACHE.invalidate(userNo);
        getBucket(userNo).delete();
    }

    /** 清空缓存 */
    public static void clear() {
        CACHE.invalidateAll();
        clear(PREFIX);

        clear(PREFIX_USER_NO);
    }

}
