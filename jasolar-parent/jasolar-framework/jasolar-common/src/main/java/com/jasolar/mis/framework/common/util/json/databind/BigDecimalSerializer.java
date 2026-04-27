package com.jasolar.mis.framework.common.util.json.databind;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;
import java.math.BigDecimal;

/**
 * BigDecimal 序列化规则
 * 
 * 规范化 BigDecimal 的序列化格式，避免科学计数法和多余的小数位
 * - 值为 0 时序列化为 "0"
 * - 其他值使用普通数字格式，去除末尾的 0
 *
 * @author Auto Generated
 */
public class BigDecimalSerializer extends JsonSerializer<BigDecimal> {

    /** 默认实例 */
    public static final BigDecimalSerializer INSTANCE = new BigDecimalSerializer();

    @Override
    public void serialize(BigDecimal value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        if (value == null) {
            gen.writeNull();
            return;
        }
        
        // 使用 stripTrailingZeros() 去除末尾的 0
        BigDecimal normalized = value.stripTrailingZeros();
        
        // 使用 writeRawValue() 和 toPlainString() 确保使用普通数字格式，避免科学计数法
        // toPlainString() 会返回不使用科学计数法的字符串表示
        gen.writeRawValue(normalized.toPlainString());
    }
}

