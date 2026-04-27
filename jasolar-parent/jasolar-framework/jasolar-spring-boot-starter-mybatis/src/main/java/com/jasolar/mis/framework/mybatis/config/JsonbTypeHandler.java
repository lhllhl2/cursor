package com.jasolar.mis.framework.mybatis.config;

import java.lang.reflect.Field;
import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedJdbcTypes;
import org.apache.ibatis.type.MappedTypes;
import org.postgresql.util.PGobject;

import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonGenerator.Feature;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.jasolar.mis.framework.common.util.json.JsonUtils;

@MappedTypes({ Object.class })
@MappedJdbcTypes({ JdbcType.VARCHAR })
public class JsonbTypeHandler extends JacksonTypeHandler {

    static final String JSONB = "jsonb";
    static final String JSON = "json";

    public JsonbTypeHandler(Class<?> type) {
        super(type);
    }

    public JsonbTypeHandler(Class<?> type, Field field) {
        super(type, field);
    }

    /**
     * 构建新的ObjectMapper
     * 
     * @return
     */
    protected static ObjectMapper newObjectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setSerializationInclusion(Include.NON_NULL);
        objectMapper.setVisibility(objectMapper.getSerializationConfig().getDefaultVisibilityChecker()
                .withFieldVisibility(JsonAutoDetect.Visibility.ANY).withGetterVisibility(JsonAutoDetect.Visibility.NONE)
                .withSetterVisibility(JsonAutoDetect.Visibility.NONE).withCreatorVisibility(JsonAutoDetect.Visibility.NONE));
        objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        objectMapper.enable(Feature.WRITE_BIGDECIMAL_AS_PLAIN);
        objectMapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);

        JsonUtils.compact(objectMapper);

        return objectMapper;
    }

    static {
        setObjectMapper(newObjectMapper());
    }

    // @SneakyThrows
    // @Override
    // public String toJson(Object obj) {
    // return getObjectMapper().writeValueAsString(obj);
    // }

    // @SneakyThrows
    // @Override
    // public Object parse(String json) {
    // ObjectMapper objectMapper = getObjectMapper();
    // TypeFactory typeFactory = objectMapper.getTypeFactory();
    // JavaType javaType = typeFactory.constructType(getFieldType());
    // try {
    // return objectMapper.readValue(json, javaType);
    // } catch (JacksonException e) {
    // log.error("deserialize json: " + json + " to " + javaType + " error ", e);
    // throw new RuntimeException(e);
    // }
    // }

    /**
     * 重写设置参数
     * 
     * @param ps
     * @param i
     * @param parameter
     * @param jdbcType
     * @throws SQLException
     */
    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, Object parameter, JdbcType jdbcType) throws SQLException {
        PGobject jsonObject = new PGobject();
        jsonObject.setType(JSONB);
        jsonObject.setValue(toJson(parameter));
        ps.setObject(i, jsonObject);
    }

    /**
     * 根据列名，获取可以为空的结果
     * 
     * @param rs
     * @param columnName
     * @return
     * @throws SQLException
     */
    @Override
    public Object getNullableResult(ResultSet rs, String columnName) throws SQLException {
        Object v = rs.getObject(columnName);
        return toFill(v);
    }

    /**
     * 根据列索引，获取可以为空的结果
     * 
     * @param rs
     * @param columnIndex
     * @return
     * @throws SQLException
     */
    @Override
    public Object getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        Object v = rs.getObject(columnIndex);
        return toFill(v);
    }

    @Override
    public Object getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        Object v = cs.getObject(columnIndex);
        return toFill(v);
    }

    /**
     * 必须将 v 转成 PGObject 处理
     * 
     * @param v
     * @return
     */
    private Object toFill(Object v) {
        if (v instanceof PGobject pg) {
            String pv = pg.getValue();
            if (StringUtils.isNotBlank(pv) && (JSONB.equals(pg.getType()) || JSON.equals(pg.getType()))) {
                return parse(pv);
            }
        }
        return v;
    }

}
