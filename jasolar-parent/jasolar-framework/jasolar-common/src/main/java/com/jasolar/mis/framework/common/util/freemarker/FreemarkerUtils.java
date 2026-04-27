package com.jasolar.mis.framework.common.util.freemarker;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import cn.hutool.core.bean.BeanUtil;

/**
 * 默认处理"/ftl"下的模板文件
 *
 * @author LuoGang
 * @date 2017-04-11 15:57
 */
public class FreemarkerUtils {

    /** 默认的模板文件目录 */
    private static String ROOT = System.getProperty("freemarker.templates", "/ftl");

    /** 默认处理对象 */
    private static Freemarker DEFAULT = Freemarker.of(ROOT);

    /**
     * 处理默认目录的模板, 默认目录为/ftl, 可以通过{@link #setDefaultRoot(String)}方法修改
     * 
     * @return {@link #DEFAULT}, 处理默认目录下模板的freemaker
     */
    public static Freemarker getDefault() {
        return DEFAULT;
    }

    /**
     * 修改模板的默认根目录
     * 
     * @param root 模板根目录
     */
    public static void setDefaultRoot(String root) {
        if (ROOT.equals(root)) {
            return;
        }
        ROOT = root;
        DEFAULT = Freemarker.of(ROOT);
    }

    /**
     * 使用静态字符串作为模板
     * 
     * @return 处理字符串模板的freemaker
     */
    public static Freemarker getStringTemplate() {
        return Freemarker.stringTemplate();
    }

    /**
     * 将Bean数据转为Map
     *
     * @param data java bean, 也可以是Map
     * 
     * @return 转换为Map参数, 为null的字段也在map中
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public static Map<String, Object> toMap(Object data) {
        Map<String, Object> model;
        if (data == null || data == Collections.EMPTY_MAP) {
            model = new HashMap<>();
        } else if (data instanceof Map) {
            model = (Map<String, Object>) data;
        } else {
            model = (Map) BeanUtil.beanToMap(data, false, false);
        }

        return model;
    }

}
