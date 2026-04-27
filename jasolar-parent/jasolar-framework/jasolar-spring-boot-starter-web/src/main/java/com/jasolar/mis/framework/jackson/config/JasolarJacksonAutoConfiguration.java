package com.jasolar.mis.framework.jackson.config;

import java.util.List;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jasolar.mis.framework.common.util.json.JsonUtils;

import cn.hutool.core.collection.CollUtil;
import lombok.extern.slf4j.Slf4j;

@AutoConfiguration
@Slf4j
public class JasolarJacksonAutoConfiguration {

    @Bean
    public JsonUtils jsonUtils(List<ObjectMapper> objectMappers) {
        JsonUtils.config(objectMappers);
        // 2. 设置 objectMapper 到 JsonUtils
        JsonUtils.init(CollUtil.getFirst(objectMappers));
        log.info("[init][初始化 JsonUtils 成功]");
        return new JsonUtils();
    }

}
