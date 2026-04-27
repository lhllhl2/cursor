package com.jasolar.mis.framework.datapermission.core.rule;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.function.Consumer;

/**
 * 自定义配置权限规则
 * 
 * @author galuo
 * @date 2025-03-04 15:25
 *
 * @param <T> 权限规则的具体实现类
 */
public interface DataPermissionRuleCustomizer<T extends DataPermissionRule> {

    /**
     * 配置权限规则
     * 
     * @param rule 权限规则
     */
    void customize(T rule);

    /**
     * 是否可以处理{@code rule}对象. 默认是的实现是判断rule是否为泛型{@code T}的实现类
     * 
     * @param rule 要处理的对象
     * @return 返回true则可以调用{@link #customize(DataPermissionRule)}方法对{@code rule}进行处理,否则不可以处理
     */
    @SuppressWarnings("unchecked")
    default boolean accept(DataPermissionRule rule) {
        Type type = this.getClass().getGenericSuperclass();
        if (type instanceof ParameterizedType) {
            ParameterizedType paramType = (ParameterizedType) type;
            Class<?> clazz = (Class<T>) paramType.getActualTypeArguments()[0];
            return clazz.isAssignableFrom(rule.getClass());
        }
        return false;
    }

    /**
     * 构建一个针对指定规则的配置对象
     * 
     * @param <T> 规则类型
     * @param ruleClass 规则的class
     * @return
     */
    static <T extends DataPermissionRule> DataPermissionRuleCustomizer<T> of(Class<T> ruleClass, Consumer<T> consumer) {

        return new DataPermissionRuleCustomizer<T>() {

            @Override
            public boolean accept(DataPermissionRule rule) {
                return ruleClass.isInstance(rule);
            }

            @Override
            public void customize(T rule) {
                consumer.accept(rule);
            }
        };
    }
}
