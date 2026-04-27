package com.jasolar.mis.framework.common.util.object;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.function.Consumer;

import org.apache.commons.lang3.StringUtils;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.ReflectUtil;

/**
 * Object 工具类
 *
 * @author zhaohuang
 */
public class ObjectUtils {

    private ObjectUtils() {
    }

    /**
     * 复制对象，并忽略 Id 编号
     *
     * @param object 被复制对象
     * @param consumer 消费者，可以二次编辑被复制对象
     * @return 复制后的对象
     */
    public static <T> T cloneIgnoreId(T object, Consumer<T> consumer) {
        T result = ObjectUtil.clone(object);
        // 忽略 id 编号
        Field field = ReflectUtil.getField(object.getClass(), "id");
        if (field != null) {
            ReflectUtil.setFieldValue(result, field, null);
        }
        // 二次编辑
        if (result != null) {
            consumer.accept(result);
        }
        return result;
    }

    public static <T extends Comparable<T>> T max(T obj1, T obj2) {
        if (obj1 == null) {
            return obj2;
        }
        if (obj2 == null) {
            return obj1;
        }
        return obj1.compareTo(obj2) > 0 ? obj1 : obj2;
    }

    @SafeVarargs
    public static <T> T defaultIfNull(T... array) {
        for (T item : array) {
            if (item != null) {
                return item;
            }
        }
        return null;
    }

    @SafeVarargs
    public static <T> boolean equalsAny(T obj, T... array) {
        return Arrays.asList(array).contains(obj);
    }

    /**
     * 如果参数val等于null, 则根据参数类型返回默认值:
     * <ol>
     * 1.字符串默认为空字符串
     * 2.数字默认为0
     * 3.日期默认为1970-01-01 00:00:00
     * 4.Boolean默认为false
     * 5.List/Set/Map/Collection默认为空列表
     * </ol>
     * 
     * @param <T>
     * @param clazz 数据类型
     * @param val 数据的值
     * @return
     */
    @SuppressWarnings("unchecked")
    public static <T> T defaultIfNull(Class<T> clazz, T val) {
        if (val != null) {
            return val;
        }
        if (CharSequence.class.isAssignableFrom(clazz)) {
            return (T) StringUtils.EMPTY;
        }

        if (Byte.class.isAssignableFrom(clazz) || Byte.TYPE.equals(clazz)) {
            return (T) Byte.valueOf((byte) 0);
        }
        if (Short.class.isAssignableFrom(clazz) || Short.TYPE.equals(clazz)) {
            return (T) Short.valueOf((short) 0);
        }
        if (Character.class.isAssignableFrom(clazz) || Character.TYPE.equals(clazz)) {
            return (T) Character.valueOf((char) 0);
        }
        if (Integer.class.isAssignableFrom(clazz) || Integer.TYPE.equals(clazz)) {
            return (T) Integer.valueOf(0);
        }
        if (Long.class.isAssignableFrom(clazz) || Long.TYPE.equals(clazz)) {
            return (T) Long.valueOf(0);
        }
        if (Float.class.isAssignableFrom(clazz) || Float.TYPE.equals(clazz)) {
            return (T) Float.valueOf(0);
        }
        if (Double.class.isAssignableFrom(clazz) || Double.TYPE.equals(clazz)) {
            return (T) Double.valueOf(0);
        }
        if (BigDecimal.class.isAssignableFrom(clazz)) {
            return (T) BigDecimal.ZERO;
        }
        if (BigInteger.class.isAssignableFrom(clazz)) {
            return (T) BigInteger.ZERO;
        }
        if (Number.class.isAssignableFrom(clazz)) {
            return (T) Integer.valueOf(0);
        }
        if (Boolean.class.isAssignableFrom(clazz)) {
            return (T) Boolean.FALSE;
        }
        if (Date.class.isAssignableFrom(clazz)) {
            return (T) new Date(0);
        }
        if (LocalDate.class.isAssignableFrom(clazz)) {
            return (T) LocalDate.of(1970, 1, 1);
        }
        if (LocalDateTime.class.isAssignableFrom(clazz)) {
            return (T) LocalDateTime.of(1970, 1, 1, 0, 0, 0);
        }
        if (SortedSet.class.isAssignableFrom(clazz)) {
            return (T) Collections.emptySortedSet();
        }
        if (Set.class.isAssignableFrom(clazz)) {
            return (T) Collections.emptySet();
        }
        if (SortedMap.class.isAssignableFrom(clazz)) {
            return (T) Collections.emptySortedMap();
        }
        if (Map.class.isAssignableFrom(clazz)) {
            return (T) Collections.emptyMap();
        }
        if (List.class.isAssignableFrom(clazz) || Collection.class.isAssignableFrom(clazz)) {
            return (T) Collections.emptyList();
        }

        return null;
    }

}
