package com.jasolar.mis.framework.common.util.json;

import java.io.IOException;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalTimeSerializer;
import com.jasolar.mis.framework.common.util.date.DateUtils;
import com.jasolar.mis.framework.common.util.json.databind.BigDecimalSerializer;
import com.jasolar.mis.framework.common.util.json.databind.DateDeserializer;
import com.jasolar.mis.framework.common.util.json.databind.LocalDateDeserializer;
import com.jasolar.mis.framework.common.util.json.databind.LocalDateTimeDeserializer;
import com.jasolar.mis.framework.common.util.json.databind.NumberSerializer;

import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.PrimitiveArrayUtil;
import cn.hutool.json.JSONUtil;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

/**
 * JSON 工具类
 *
 * @author zhaohuang
 */
@Slf4j
public class JsonUtils {

    /** 默认使用的ObjectMapper对象 */
    private static ObjectMapper objectMapper = new ObjectMapper();

    static {
        compact(objectMapper);
    }

    /**
     * 统一配置日期格式:
     * 
     * <pre>
     * Date/LocalDateTime: {@link DateUtils#FORMAT_DATETIME}
     * LocalDate: {@link DateUtils#FORMAT_DATE}
     * LocalTime: {@link DateUtils#FORMAT_TIME}
     * </pre>
     * 
     * @param objectMappers
     */
    private static void doConfig(Collection<ObjectMapper> objectMappers) {
        // 1.1 创建 SimpleModule 对象
        SimpleModule simpleModule = new SimpleModule();
        simpleModule.addDeserializer(Date.class, new DateDeserializer())
                // LocalTime
                .addSerializer(LocalTime.class, new LocalTimeSerializer(DateTimeFormatter.ofPattern(DateUtils.FORMAT_TIME)))
                .addDeserializer(LocalTime.class, new LocalTimeDeserializer(DateTimeFormatter.ofPattern(DateUtils.FORMAT_TIME)))

                // 新增 LocalDateTime 序列化、反序列化规则，使用 Long 时间戳
                .addSerializer(LocalDateTime.class, new LocalDateTimeSerializer(DateTimeFormatter.ofPattern(DateUtils.FORMAT_DATETIME)))
                .addDeserializer(LocalDateTime.class, new LocalDateTimeDeserializer(DateUtils.FORMAT_DATETIME))

                // 新增 LocalDate 序列化、反序列化规则，使用 Long 时间戳
                .addSerializer(LocalDate.class, new LocalDateSerializer(DateTimeFormatter.ofPattern(DateUtils.FORMAT_DATE)))
                .addDeserializer(LocalDate.class, new LocalDateDeserializer(DateUtils.FORMAT_DATE));

        // 1.2 注册到 objectMapper
        objectMappers.forEach(objectMapper -> {
            objectMapper.registerModules(new JavaTimeModule()); // 解决 LocalDateTime 的序列化
            objectMapper.registerModule(simpleModule);

            objectMapper.setDateFormat(new SimpleDateFormat(DateUtils.FORMAT_DATETIME));
            objectMapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
            objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        });

    }

    /**
     * 统一配置,用于初始化ObjectMapper. 此方法主要用于WEB端, 配置Long类型防止JS丢失精度
     * 
     * @param objectMappers
     * 
     * @see #doConfig(Collection)
     */
    public static void config(Collection<ObjectMapper> objectMappers) {
        // 1.1 创建 SimpleModule 对象
        SimpleModule simpleModule = new SimpleModule();
        // 新增 Long 类型序列化规则，数值超过 2^53-1，在 JS 会出现精度丢失问题，因此 Long 自动序列化为字符串类型
        simpleModule.addSerializer(Long.class, NumberSerializer.INSTANCE).addSerializer(Long.TYPE, NumberSerializer.INSTANCE);
        // 新增 BigDecimal 类型序列化规则，规范化 BigDecimal 的序列化格式，避免科学计数法和多余的小数位
        simpleModule.addSerializer(BigDecimal.class, BigDecimalSerializer.INSTANCE);

        // 1.2 注册到 objectMapper
        objectMappers.forEach(objectMapper -> {
            objectMapper.registerModule(simpleModule);
        });

        // 配置日期格式
        doConfig(objectMappers);
    }

    /**
     * 简化的JSON输出配置,不包含null字段
     * 
     * @param objectMapper ObjectMapper 对象
     * @see #doConfig(Collection)
     */
    public static void compact(ObjectMapper objectMapper) {
        doConfig(Arrays.asList(objectMapper));
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
    }

    /**
     * 初始化 objectMapper 属性
     * <p>
     * 通过这样的方式，使用 Spring 创建的 ObjectMapper Bean. 这里仅添加了不包含null字段功能,反序列化时不存在的字段直接忽略
     *
     * @param objectMapper ObjectMapper 对象
     */
    public static void init(ObjectMapper objectMapper) {
        JsonUtils.objectMapper = objectMapper.copy();
        JsonUtils.objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
    }

    @SneakyThrows
    public static String toJsonString(Object object) {
        return objectMapper.writeValueAsString(object);
    }

    @SneakyThrows
    public static byte[] toJsonByte(Object object) {
        return objectMapper.writeValueAsBytes(object);
    }

    @SneakyThrows
    public static String toJsonPrettyString(Object object) {
        return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(object);
    }

    public static <T> T parseObject(String text, Class<T> clazz) {
        if (CharSequenceUtil.isEmpty(text)) {
            return null;
        }
        try {
            return objectMapper.readValue(text, clazz);
        } catch (IOException e) {
            log.error("json parse err,json:{}", text, e);
            throw new RuntimeException(e);
        }
    }

    public static <T> T parseObject(String text, String path, Class<T> clazz) {
        if (CharSequenceUtil.isEmpty(text)) {
            return null;
        }
        try {
            JsonNode treeNode = objectMapper.readTree(text);
            JsonNode pathNode = treeNode.path(path);
            return objectMapper.readValue(pathNode.toString(), clazz);
        } catch (IOException e) {
            log.error("json parse err,json:{}", text, e);
            throw new RuntimeException(e);
        }
    }

    public static <T> T parseObject(String text, Type type) {
        if (CharSequenceUtil.isEmpty(text)) {
            return null;
        }
        try {
            return objectMapper.readValue(text, objectMapper.getTypeFactory().constructType(type));
        } catch (IOException e) {
            log.error("json parse err,json:{}", text, e);
            throw new RuntimeException(e);
        }
    }

    /**
     * 将字符串解析成指定类型的对象
     * 使用 {@link #parseObject(String, Class)} 时，在@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS) 的场景下，
     * 如果 text 没有 class 属性，则会报错。此时，使用这个方法，可以解决。
     *
     * @param text 字符串
     * @param clazz 类型
     * @return 对象
     */
    public static <T> T parseObject2(String text, Class<T> clazz) {
        if (CharSequenceUtil.isEmpty(text)) {
            return null;
        }
        return JSONUtil.toBean(text, clazz);
    }

    public static <T> T parseObject(byte[] bytes, Class<T> clazz) {
        if (PrimitiveArrayUtil.isEmpty(bytes)) {
            return null;
        }
        try {
            return objectMapper.readValue(bytes, clazz);
        } catch (IOException e) {
            log.error("json parse err,json:{}", bytes, e);
            throw new RuntimeException(e);
        }
    }

    public static <T> T parseObject(String text, TypeReference<T> typeReference) {
        try {
            return objectMapper.readValue(text, typeReference);
        } catch (IOException e) {
            log.error("json parse err,json:{}", text, e);
            throw new RuntimeException(e);
        }
    }

    /**
     * 解析 JSON 字符串成指定类型的对象，如果解析失败，则返回 null
     *
     * @param text 字符串
     * @param typeReference 类型引用
     * @return 指定类型的对象
     */
    public static <T> T parseObjectQuietly(String text, TypeReference<T> typeReference) {
        try {
            return objectMapper.readValue(text, typeReference);
        } catch (IOException e) {
            return null;
        }
    }

    public static <T> List<T> parseArray(String text, Class<T> clazz) {
        if (CharSequenceUtil.isEmpty(text)) {
            return new ArrayList<>();
        }
        try {
            return objectMapper.readValue(text, objectMapper.getTypeFactory().constructCollectionType(List.class, clazz));
        } catch (IOException e) {
            log.error("json parse err,json:{}", text, e);
            throw new RuntimeException(e);
        }
    }

    public static <T> List<T> parseArray(String text, String path, Class<T> clazz) {
        if (CharSequenceUtil.isEmpty(text)) {
            return Collections.emptyList();
        }
        try {
            JsonNode treeNode = objectMapper.readTree(text);
            JsonNode pathNode = treeNode.path(path);
            return objectMapper.readValue(pathNode.toString(), objectMapper.getTypeFactory().constructCollectionType(List.class, clazz));
        } catch (IOException e) {
            log.error("json parse err,json:{}", text, e);
            throw new RuntimeException(e);
        }
    }

    public static JsonNode parseTree(String text) {
        try {
            return objectMapper.readTree(text);
        } catch (IOException e) {
            log.error("json parse err,json:{}", text, e);
            throw new RuntimeException(e);
        }
    }

    public static JsonNode parseTree(byte[] text) {
        try {
            return objectMapper.readTree(text);
        } catch (IOException e) {
            log.error("json parse err,json:{}", text, e);
            throw new RuntimeException(e);
        }
    }

    public static boolean isJson(String text) {
        return JSONUtil.isTypeJSON(text);
    }

}
