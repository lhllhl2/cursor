package com.jasolar.mis.framework.common.util.freemarker;

import cn.hutool.crypto.digest.DigestUtil;
import freemarker.cache.StringTemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.Template;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

/**
 * 使用静态字符串模板. 方法中的模板名均视为模板内容
 * 
 * @author galuo
 * @date 2025-04-16 10:55
 *
 */
@Slf4j
public class StringTemplateFreemarker extends Freemarker {

    /** 默认的实例 */
    private static final StringTemplateFreemarker INSTANCE = new StringTemplateFreemarker();

    /** 默认实例 */
    public static StringTemplateFreemarker getInstance() {
        return INSTANCE;
    }

    /**
     * 配置对象
     * 
     * @param config
     */
    private StringTemplateFreemarker() {
        super(null, new Configuration(Configuration.DEFAULT_INCOMPATIBLE_IMPROVEMENTS));
        config.setTemplateLoader(new StringTemplateLoader());
    }

    /**
     * 得到指定的模板对象
     *
     * @param content 模板内容
     * @return Template
     */
    @SneakyThrows
    protected Template getTemplate(String content) {
        StringTemplateLoader loader = (StringTemplateLoader) config.getTemplateLoader();
        // 计算MD5值,如果直接已content作为模板名称, 当已/开头的时候, config底层会去掉开头的/造成模板无法找到
        String name = DigestUtil.md5Hex(content);
        Object exists = loader.findTemplateSource(name);
        if (exists == null) {
            log.warn("模板不存在, 先注入模板: {}, {}", name, content);
            loader.putTemplate(name, content);
        }

        return config.getTemplate(name);
    }
}
