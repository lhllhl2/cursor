package com.jasolar.mis.module.system.controller.admin.outer.convert;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jasolar.mis.module.system.controller.admin.outer.vo.synuser.UserJobsVo;

import java.io.IOException;
import java.util.List;

/**
 * Description:
 * Author : Zhou Hai
 * Date : 13/08/2025 16:28
 * Version : 1.0
 */

public class UserJobsListDeserializer extends JsonDeserializer<List<UserJobsVo>> {

    @Override
    public List<UserJobsVo> deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        if (p.currentToken() == JsonToken.VALUE_STRING) {
            // 字符串类型：解析 JSON 数组字符串
            String json = p.getText();
            return new ObjectMapper().readValue(json, new TypeReference<List<UserJobsVo>>() {});
        } else if (p.currentToken() == JsonToken.START_ARRAY) {
            // 数组类型：直接反序列化
            return ctxt.readValue(p, List.class);
        }
        throw new RuntimeException("Expected String or Array");
    }

}
