package com.jasolar.mis.framework.datapermission.core.service;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.redisson.codec.JsonJacksonCodec;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jasolar.mis.framework.common.pojo.CommonResult;
import com.jasolar.mis.framework.common.util.cache.CacheUtils;
import com.jasolar.mis.framework.common.util.json.JsonUtils;
import com.jasolar.mis.framework.data.RedissonUtils;
import com.jasolar.mis.framework.datapermission.core.scope.DataScope;
import com.jasolar.mis.framework.datapermission.core.scope.ReadWrite;
import com.jasolar.mis.framework.datapermission.core.scope.ScopeLevel;
import com.jasolar.mis.framework.datapermission.core.scope.ScopeType;
import com.jasolar.mis.module.system.api.permission.PermissionApi;
import com.jasolar.mis.module.system.api.permission.dto.DataPermissionDTO;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

import lombok.SneakyThrows;

/**
 * DataPermissionService的实现
 * 
 * @author galuo
 * @date 2025-03-04 09:57
 *
 */
@Service
public class DataScopeServiceImpl implements DataScopeService, InitializingBean {

    /** 缓存的前缀 */
    public static final String CACHE_KEY_PREFIX = "PERMISSION:DATA:USER:";

    /** 分布式锁的前缀 */
    public static final String LOCK_KEY_PREFIX = "LOCK:" + CACHE_KEY_PREFIX;

    /** 星号 */
    static final String ASTERISK = "*";

    /** 权限API Feign客户端 */
    private final PermissionApi permissionApi;

    /** Redisson客户端 */
    private final RedissonClient redisson;

    /** 权限在REDIS中的缓存时间,单位秒 */
    private int redisCacheTtl = 36000;

    /** 权限在内存中的缓存时间,单位秒 */
    private int memoryCacheTtl = 60;

    /** 权限在内存中的缓存数量 */
    private int memoryCacheCount = 2000;

    /** 使用工号缓存 */
    private LoadingCache<String, List<DataScope>> cache;

    // /**
    // * 必须的依赖bean
    // *
    // * @param permissionApi
    // * @param redisson
    // */
    // public DataScopeServiceImpl(PermissionApi permissionApi, RedissonClient redisson) {
    // super();
    // this.permissionApi = permissionApi;
    // this.redisson = redisson;
    // }

    public DataScopeServiceImpl(PermissionApi permissionApi, RedissonClient redisson, int redisCacheTtl, int memoryCacheTtl,
            int memoryCacheCount) {
        super();
        this.permissionApi = permissionApi;
        this.redisson = redisson;
        this.redisCacheTtl = redisCacheTtl;
        this.memoryCacheTtl = memoryCacheTtl;
        this.memoryCacheCount = memoryCacheCount;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        cache = CacheUtils.buildAsyncReloadingCache(Duration.ofSeconds(memoryCacheTtl), memoryCacheCount, CacheLoader.from(this::load));
    }

    /**
     * 查询用户的所有数据权限
     * 
     * @param userNo
     * @return
     */
    public List<DataScope> load(String userNo) {
        RBucket<List<DataScope>> bucket = redisson.getBucket(CACHE_KEY_PREFIX + userNo);
        if (bucket.isExists()) {
            return bucket.get();
        }
        return RedissonUtils.execute(LOCK_KEY_PREFIX + userNo, () -> {
            List<DataScope> scopes = bucket.get();
            if (scopes != null) {
                return scopes;
            }

            CommonResult<List<DataPermissionDTO>> r = permissionApi.getUserDataPermissions(userNo);
            if (r.isError()) {
                // 数据权限范围获取异常
                return Collections.emptyList();
            }

            if (CollectionUtils.isEmpty(r.getData())) {
                scopes = Collections.emptyList();
            } else {
                // 慎用toList()方法, 返回的List不可变,并且可能造成redis无法写入List的class
                scopes = r.getData().stream().map(d -> {
                    // 转换为DataScope
                    return DataScope.builder().type(ScopeType.of(d.getScopeType())).level(ScopeLevel.of(d.getScopeLevel()))
                            .menuId(d.getMenuId()).nullable(d.getNullable() != null && d.getNullable()).rw(ReadWrite.of(d.getReadWrite()))
                            .dataIds(d.getDataIds() == null ? new HashSet<>() : d.getDataIds()).build();
                }).collect(Collectors.toList());

                // 人员权限添加自己的权限
                scopes.parallelStream().filter(scope -> ScopeType.USER == scope.getType() && !scope.getDataIds().contains(userNo))
                        .forEach(scope -> scope.getDataIds().add(userNo));
            }

            // 缓存人员的数据权限
            bucket.set(scopes, Duration.ofSeconds(redisCacheTtl));

            return scopes;
        });
    }

    @SneakyThrows
    @Override
    public List<DataScope> findDataPermissionScopes(String userNo, Long menuId, Predicate<? super DataScope> predicate) {
        List<DataScope> scopes = cache.get(userNo);
        if (scopes.isEmpty()) {
            return Collections.emptyList();
        }

        // 过滤出菜单的权限
        List<DataScope> menuScopes = scopes.parallelStream().filter(s -> Objects.equals(menuId, s.getMenuId())).toList();
        if (menuScopes.isEmpty() && menuId != null) {
            // 菜单权限没有找到则使用没有配置菜单ID的默认权限
            menuScopes = scopes.parallelStream().filter(s -> s.getMenuId() == null).toList();
        }
        return menuScopes.stream().filter(predicate).toList();
    }

    @Override
    public void clearCache() {
        // redisson.getKeys().deleteByPatternAsync(CACHE_KEY_PREFIX + ASTERISK);
        RedissonUtils.clear(CACHE_KEY_PREFIX);
    }

    @Override
    public void removeCache(String userNo) {
        RedissonUtils.delete(CACHE_KEY_PREFIX + userNo);
    }

    // /**
    // * 构建查询自己数据的权限
    // *
    // * @param userId 人员ID
    // * @return 数据范围
    // */
    // static DataScope self(Long userId) {
    // DataScope scope = new DataScope();
    // scope.setRw(ReadWrite.ALL);
    // scope.setType(ScopeType.USER);
    // scope.setDataIds(SetUtils.asSet(userId));
    // return scope;
    // }

    public static void main(String[] args) {
        String json = "[\"java.util.ArrayList\", [ {\"@class\":\"com.fiifoxconn.mis.framework.datapermission.core.scope.DataScope\",\"nullable\":false}] ]";

        ObjectMapper objectMapper = JsonJacksonCodec.INSTANCE.getObjectMapper();
        JsonUtils.compact(objectMapper);

        DataScope s = new DataScope();
        s.setNullable(true);

        try {
            System.out.print(objectMapper.writeValueAsString(new ArrayList<>(Arrays.asList(s))));
            List<?> list = objectMapper.readValue(json, List.class);

            System.out.print(list);

        } catch (JsonMappingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (JsonProcessingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

}
