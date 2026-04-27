package com.jasolar.mis.framework.data.util;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RMap;

import com.jasolar.mis.framework.common.pojo.CommonResult;
import com.jasolar.mis.framework.common.util.cache.CacheUtils;
import com.jasolar.mis.framework.common.util.collection.CollectionUtils;
import com.jasolar.mis.framework.common.util.spring.SpringUtils;
import com.jasolar.mis.framework.data.RedissonUtils;
import com.jasolar.mis.framework.data.convert.DictConverter;
import com.jasolar.mis.framework.data.core.DictData;
// import com.jasolar.mis.module.system.api.dict.DictDataApi;
// import com.jasolar.mis.module.system.api.dict.dto.DictDataRespDTO;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

import lombok.SneakyThrows;

/**
 * 
 * 字典工具类
 * 
 * @author galuo
 * @date 2025-03-28 13:44
 *
 */
public class DictUtils extends RedissonUtils {

    /** 用于获取字典数据 */
    // public static DictDataApi API;

    /** 按字典分类缓存字典信息 */
    public static final String PREFIX = DATA_KEY_PREFIX + "SYSTEM:DICTS:TYPE:";

    /** 内存缓存 */
    private static final LoadingCache<String, Map<String, DictData>> CACHE = CacheUtils.buildAsyncReloadingCache(MEMORY_DURATION,
            CacheLoader.from(key -> load(key)));

    /** 初始化, 用于注入依赖的Spring Bean等 */
    public static void init() {
        // API = SpringUtils.getBean(DictDataApi.class);
    }

    /**
     * 加载分类的所有字典数据
     * 
     * @param type 字典分类
     * @return 分类下的所有字典
     */
    public static Map<String, DictData> load(String type) {
        RMap<String, DictData> dict = getDict(type);
        if (dict.isExists()) {
            return dict.readAllMap();
        }

        // 通过api读取 - 暂时注释掉，因为DictDataApi已被删除
        // CommonResult<List<DictDataRespDTO>> r = API.getDictDataList(type);
        // if (r == null || r.isError()) {
        //     return Collections.emptyMap();
        // }
        // List<DictData> list = DictConverter.INSTANCE.convert(r.getData());
        // Map<String, DictData> map = CollectionUtils.convertMap(list, DictData::getValue,dictData->dictData);
        // dict.putAll(map);
        // dict.expire(REDIS_DURATION);
        // return map;
        
        // 暂时返回空Map，因为DictDataApi已被删除
        return Collections.emptyMap();
    }

    /**
     * 查询指定分类的所有字典数据
     * 
     * @param type 字典分类
     * @return
     */
    static final RMap<String, DictData> getDict(String type) {
        RMap<String, DictData> data = REDISSON.getMap(PREFIX + type);
        return data;
    }

    /**
     * 得到单个数据
     * 
     * @param type 字典分类
     * @param value 字典数据值
     * @return 单个字典数据
     */
    @SneakyThrows
    public static DictData getData(String type, String value) {
        if (StringUtils.isBlank(type) || StringUtils.isBlank(value)) {
            return null;
        }
        Map<String, DictData> map = CACHE.get(type);
        if (map.isEmpty()) {
            return null;
        }
        return map.get(value);
    }
    /**
     * 得到单个数据label值
     *
     * @param type 字典分类
     * @param value 字典数据值
     * @return 单个字典数据
     */
    @SneakyThrows
    public static String getLabel(String type, String value) {
        DictData data = getData(type, value);
        return data == null ? StringUtils.EMPTY : data.getLabel();
    }

    /**
     * 得到指定分类的字典数据列表
     * 
     * @param type 字典分类
     * @return 字典数据列表
     */
    @SneakyThrows
    public static List<DictData> listData(String type) {
        Map<String, DictData> map = CACHE.get(type);

        // 按sort排序
        return map.values().parallelStream().sorted().toList();
    }

    /**
     * 得到指定分类的字典数据
     * 
     * @param type 字典分类
     * @param predicate 字典数据筛选函数
     * @return 字典数据列表
     */
    public static List<DictData> listData(String type, Predicate<DictData> predicate) {
        return listData(type).stream().filter(predicate).toList();
    }

    /**
     * 删除指定字典的缓存
     * 
     * @param type 字典分类
     */
    public static void removeCache(String type) {
        CACHE.invalidate(type);
        getDict(type).delete();
    }

    /** 清空缓存 */
    public static void clear() {
        CACHE.invalidateAll();
        clear(PREFIX);
    }

}
