package com.jasolar.mis.framework.common.util.object;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

import org.springframework.util.ReflectionUtils;

import com.jasolar.mis.framework.common.pojo.PageResult;
import com.jasolar.mis.framework.common.util.collection.CollectionUtils;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import lombok.SneakyThrows;

/**
 * Bean 工具类
 *
 * 1. 默认使用 {@link cn.hutool.core.bean.BeanUtil} 作为实现类，虽然不同 bean 工具的性能有差别，但是对绝大多数同学的项目，不用在意这点性能
 * 2. 针对复杂的对象转换，可以搜参考 AuthConvert 实现，通过 mapstruct + default 配合实现
 *
 * @author zhaohuang
 */
public class BeanUtils extends org.springframework.beans.BeanUtils {

    private BeanUtils() {
    }

    public static <T> T toBean(Object source, Class<T> targetClass) {
        return BeanUtil.toBean(source, targetClass);
    }

    public static <T> T toBean(Object source, Class<T> targetClass, Consumer<T> peek) {
        T target = toBean(source, targetClass);
        if (target != null) {
            peek.accept(target);
        }
        return target;
    }

    public static <S, T> List<T> toBean(List<S> source, Class<T> targetType) {
        if (source == null) {
            return Collections.emptyList();
        }
        return CollectionUtils.convertList(source, s -> toBean(s, targetType));
    }

    public static <S, T> List<T> toBean(List<S> source, Class<T> targetType, Consumer<T> peek) {
        List<T> list = toBean(source, targetType);
        if (list != null) {
            list.forEach(peek);
        }
        return list;
    }

    public static <S, T> PageResult<T> toBean(PageResult<S> source, Class<T> targetType) {
        return toBean(source, targetType, null);
    }

    public static <S, T> PageResult<T> toBean(PageResult<S> source, Class<T> targetType, Consumer<T> peek) {
        if (source == null) {
            return null;
        }
        List<T> list = toBean(source.getList(), targetType);
        if (peek != null) {
            list.forEach(peek);
        }
        return new PageResult<>(list, source.getTotal());
    }

    // public static void copyProperties(Object source, Object target) {
    // if (source == null || target == null) {
    // return;
    // }
    // BeanUtil.copyProperties(source, target, false);
    // }

    /**
     * 复制非空的字段
     * 
     * @param source 源对象
     * @param target 目标对象
     */
    public static void copyNonnull(Object source, Object target) {
        BeanUtil.copyProperties(source, target, CopyOptions.create().ignoreNullValue());
    }

    /**
     * 获取字段的值, 优先使用getter方法获取, 其次通过字段反射获取
     * 
     * @param bean Bean对象
     * @param propertyName 字段名
     * @return 字段的值
     */
    @SneakyThrows
    public static Object getPropertyValue(Object bean, String propertyName) {
        if (bean == null) {
            return null;
        }
        PropertyDescriptor pd = getPropertyDescriptor(bean.getClass(), propertyName);
        if (pd != null) {
            Method readMethod = pd.getReadMethod();
            if (readMethod != null) {
                ReflectionUtils.makeAccessible(readMethod);
                return readMethod.invoke(bean);
            }
        }

        return BeanUtil.getFieldValue(bean, propertyName);
    }

    /**
     * 设置字段的值, 优先使用setter方法, 其次通过字段反射设值
     * 
     * @param bean Bean对象
     * @param propertyName 字段名
     * @param propertyValue 字段的值
     */
    @SneakyThrows
    public static void setPropertyValue(Object bean, String propertyName, Object propertyValue) {
        if (bean == null) {
            return;
        }
        PropertyDescriptor pd = getPropertyDescriptor(bean.getClass(), propertyName);
        if (pd != null) {
            Method writeMethod = pd.getWriteMethod();
            if (writeMethod != null) {
                ReflectionUtils.makeAccessible(writeMethod);
                writeMethod.invoke(bean, propertyValue);
                return;
            }
        }

        BeanUtil.setFieldValue(bean, propertyName, propertyValue);
    }

}