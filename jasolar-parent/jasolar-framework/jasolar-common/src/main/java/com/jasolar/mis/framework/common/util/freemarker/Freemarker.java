package com.jasolar.mis.framework.common.util.freemarker;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import org.springframework.util.ClassUtils;

import freemarker.ext.beans.BeansWrapper;
import freemarker.ext.beans.BeansWrapperBuilder;
import freemarker.template.Configuration;
import freemarker.template.Template;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

/**
 *
 * 用于处理Freemark模板
 *
 * @author galuo
 * @date 2023-07-14 18:30
 *
 */
@RequiredArgsConstructor
public class Freemarker {

    /** 用于访问静态方法等 */
    public static final String KEY_STATIC_MODEL = "statics";

    /** BeansWrapper对象 */
    public static final BeansWrapper BEANS_WRAPPER = new BeansWrapperBuilder(Configuration.DEFAULT_INCOMPATIBLE_IMPROVEMENTS).build();

    /** 模板文件的目录 */
    @Getter
    protected final String root;

    /** FreeMarker的配置对象，默认为class根目录下的templates目录 */
    @Getter
    protected final Configuration config;

    /**
     * 指定模板目录
     *
     * @param root 模板所在跟目录
     * @return
     */
    public static final Freemarker of(String root) {
        Configuration config = new Configuration(Configuration.DEFAULT_INCOMPATIBLE_IMPROVEMENTS);
        config.setClassLoaderForTemplateLoading(ClassUtils.getDefaultClassLoader(), root);
        return new Freemarker(root, config);
    }

    /**
     * 静态字符串模板
     * 
     * @return 使用静态字符串作为模板
     */
    public static final Freemarker stringTemplate() {
        return StringTemplateFreemarker.getInstance();
    }

    /**
     * 将Bean数据转为Map,加入了一些固定的参数: {@link #KEY_STATIC_MODEL}
     *
     * @param data
     * @return
     */
    public static Map<String, Object> toMapModel(Object data) {
        Map<String, Object> model = FreemarkerUtils.toMap(data);
        if (!model.containsKey(KEY_STATIC_MODEL)) {
            try {
                model.put(KEY_STATIC_MODEL, BEANS_WRAPPER.getStaticModels());
            } catch (UnsupportedOperationException ex) {
                // 可能是UnmodifiableMap
                model = new HashMap<>(model);
                model.put(KEY_STATIC_MODEL, BEANS_WRAPPER.getStaticModels());
            }
        }

        return model;
    }

    /**
     * 得到指定的模板对象
     *
     * @param name 模板文件
     * @return Template
     */
    @SneakyThrows
    protected Template getTemplate(String name) {
        return config.getTemplate(name);
    }

    /**
     * 判断指定的模板是否存在.
     *
     * @param name 模板文件
     * @return 存在则返回true,否则返回false
     */
    public boolean exists(String name) {
        return getTemplate(name) != null;
    }

    /**
     * 处理模板,将内容写入{@code out}参数
     *
     * @param name 模板文件名,包括全路径和后缀名
     * @param data 数据
     * @param out 输出流
     */
    @SneakyThrows
    public void process(String name, Object data, Writer out) {
        Template template = getTemplate(name);
        Map<String, Object> model = toMapModel(data);
        template.process(model, out);
    }

    /**
     * 解析模板并写入文件, 使用UTF-8编码
     *
     * @param name 模板文件名,包括全路径和后缀名.
     * @param data 数据
     * @param file 输出文件
     */
    @SneakyThrows
    public void process(String name, Object data, File file) {
        process(name, data, file, StandardCharsets.UTF_8);
    }

    /**
     * 解析模板并写入文件, 使用指定的编码
     *
     * @param name 模板文件名,包括全路径和后缀名.
     * @param data 数据
     * @param file 输出文件
     * @param charset 写入文件时使用的编码
     */
    @SneakyThrows
    public void process(String name, Object data, File file, Charset charset) {
        try (FileOutputStream stream = new FileOutputStream(file); Writer out = new OutputStreamWriter(stream, charset)) {
            process(name, data, out);
        }
    }

    /**
     * 解析模板并写入文件, 使用UTF-8编码
     *
     * @param name 模板文件名,包括全路径和后缀名.
     * @param data 数据
     * @param out 输出流
     * @param charset 写入文件时使用的编码
     */
    @SneakyThrows
    public void process(String name, Object data, OutputStream out, Charset charset) {
        try (Writer writer = new OutputStreamWriter(out, StandardCharsets.UTF_8)) {
            process(name, data, writer);
        }
    }

    /**
     * 解析模板并返回内容
     *
     * @param name 模板文件名
     * @param data 数据
     * @return 解析到的数据
     */
    public String get(String name, Object data) {
        StringWriter out = new StringWriter();
        process(name, data, out);
        return out.toString().trim();
    }

    /**
     * 解析模板并返回内容
     *
     * @param name 模板名
     * @param data 数据
     * @param vars 默认变量,同名参数会被data覆盖
     * @return 解析到的数据
     */
    public String get(String name, Object data, Map<?, ?> vars) {
        if (vars == null || vars.isEmpty()) {
            return get(name, data);
        }

        Map<Object, Object> map = new HashMap<>(vars);
        map.putAll(toMapModel(data));
        return get(name, map);
    }
}
