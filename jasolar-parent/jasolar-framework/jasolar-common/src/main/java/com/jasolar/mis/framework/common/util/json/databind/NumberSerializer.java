package com.jasolar.mis.framework.common.util.json.databind;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JacksonStdImpl;

/**
 * Long 序列化规则
 *
 * 会将超长 long 值转换为 string，解决前端 JavaScript 最大安全整数是 2^53-1 的问题
 *
 * @author zhaohuang
 */
@SuppressWarnings("serial")
@JacksonStdImpl
public class NumberSerializer extends com.fasterxml.jackson.databind.ser.std.NumberSerializer {

    /** 最大的安全数字, {@code 2}<sup>{@code 53}</sup>-1 */
    private static final long MAX_SAFE = 0x1FFFFFFFFFFFFFL;
    /** 最小的安全数字, -({@code 2}<sup>{@code 53}</sup>-1) */
    private static final long MIN_SAFE = -MAX_SAFE;

    /** 默认实例 */
    public static final NumberSerializer INSTANCE = new NumberSerializer(Number.class);

    /**
     * 要处理的数字类型
     * 
     * @param rawType
     */
    public NumberSerializer(Class<? extends Number> rawType) {
        super(rawType);
    }

    @Override
    public void serialize(Number value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        if (value.longValue() > MIN_SAFE && value.longValue() < MAX_SAFE) {
            super.serialize(value, gen, serializers);
        } else {
            // 超出范围 序列化为字符串
            gen.writeString(value.toString());
        }
    }
}
