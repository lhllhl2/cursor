package com.jasolar.mis.framework.common.util.collection;

import java.util.Set;

import cn.hutool.core.collection.CollUtil;

/**
 * Set 工具类
 *
 * @author zhaohuang
 */
public class SetUtils {

    private SetUtils() {
    }

    @SafeVarargs
    public static <T> Set<T> asSet(T... objs) {
        return CollUtil.newLinkedHashSet(objs);
    }

}
