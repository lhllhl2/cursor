package com.jasolar.mis.framework.mybatis.core.type;

import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedJdbcTypes;
import org.apache.ibatis.type.MappedTypes;
import lombok.extern.slf4j.Slf4j;

import java.sql.*;
import java.time.LocalDateTime;

/**
 * Oracle LocalDateTime 类型处理器
 * 解决 Oracle JDBC 驱动（ojdbc8 23.x）在转换 LocalDateTime 时的类型转换问题
 * 
 * 问题原因：Oracle JDBC 驱动 23.x 版本在尝试直接调用 getLocalDateTime() 时可能失败，
 * 特别是当数据库字段类型为 DATE 或格式不标准时。
 * 解决方案：通过 Timestamp 作为中间类型进行转换，确保兼容性。
 *
 * @author Auto Generated
 */
@Slf4j
@MappedTypes(LocalDateTime.class)
@MappedJdbcTypes({JdbcType.TIMESTAMP, JdbcType.DATE})
public class OracleLocalDateTimeTypeHandler extends BaseTypeHandler<LocalDateTime> {

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, LocalDateTime parameter, JdbcType jdbcType) throws SQLException {
        // setNonNullParameter 方法只处理非 null 值，null 值由 BaseTypeHandler.setParameter 处理
        // 使用标准的 setTimestamp 方法，这是最可靠和兼容的方式
        ps.setTimestamp(i, Timestamp.valueOf(parameter));
    }

    @Override
    public LocalDateTime getNullableResult(ResultSet rs, String columnName) throws SQLException {
        try {
            // 优先尝试使用 Timestamp 获取
            Timestamp timestamp = rs.getTimestamp(columnName);
            return getLocalDateTime(timestamp);
        } catch (SQLException e) {
            // 如果 Timestamp 获取失败，尝试使用 Object 获取后转换
            try {
                Object obj = rs.getObject(columnName);
                return convertToLocalDateTime(obj);
            } catch (Exception ex) {
                log.error("========== OracleLocalDateTimeTypeHandler 转换失败，columnName: {}, error: {} ==========", columnName, ex.getMessage(), ex);
                throw new SQLException("Failed to convert column " + columnName + " to LocalDateTime", ex);
            }
        }
    }

    @Override
    public LocalDateTime getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        try {
            // 优先尝试使用 Timestamp 获取
            Timestamp timestamp = rs.getTimestamp(columnIndex);
            return getLocalDateTime(timestamp);
        } catch (SQLException e) {
            // 如果 Timestamp 获取失败，尝试使用 Object 获取后转换
            try {
                Object obj = rs.getObject(columnIndex);
                return convertToLocalDateTime(obj);
            } catch (Exception ex) {
                log.error("========== OracleLocalDateTimeTypeHandler 转换失败，columnIndex: {}, error: {} ==========", columnIndex, ex.getMessage(), ex);
                throw new SQLException("Failed to convert column at index " + columnIndex + " to LocalDateTime", ex);
            }
        }
    }

    @Override
    public LocalDateTime getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        try {
            // 优先尝试使用 Timestamp 获取
            Timestamp timestamp = cs.getTimestamp(columnIndex);
            return getLocalDateTime(timestamp);
        } catch (SQLException e) {
            // 如果 Timestamp 获取失败，尝试使用 Object 获取后转换
            try {
                Object obj = cs.getObject(columnIndex);
                return convertToLocalDateTime(obj);
            } catch (Exception ex) {
                throw new SQLException("Failed to convert column at index " + columnIndex + " to LocalDateTime", ex);
            }
        }
    }

    /**
     * 将 Timestamp 转换为 LocalDateTime
     * 如果 Timestamp 为 null，返回 null
     */
    private LocalDateTime getLocalDateTime(Timestamp timestamp) {
        if (timestamp == null) {
            return null;
        }
        return timestamp.toLocalDateTime();
    }

    /**
     * 将 Object 转换为 LocalDateTime
     * 支持多种类型的转换，包括 Oracle 返回的字符串格式
     */
    private LocalDateTime convertToLocalDateTime(Object obj) {
        if (obj == null) {
            return null;
        }
        
        if (obj instanceof Timestamp) {
            return ((Timestamp) obj).toLocalDateTime();
        }
        
        if (obj instanceof java.sql.Date) {
            return ((java.sql.Date) obj).toLocalDate().atStartOfDay();
        }
        
        if (obj instanceof java.util.Date) {
            return new Timestamp(((java.util.Date) obj).getTime()).toLocalDateTime();
        }
        
        if (obj instanceof LocalDateTime) {
            return (LocalDateTime) obj;
        }
        
        // 如果是字符串，尝试解析 Oracle 日期格式
        if (obj instanceof String) {
            String str = (String) obj;
            try {
                // Oracle 日期格式：21-NOV-25 02.41.32.090036075 PM
                // 转换为标准格式：2025-11-21 14:41:32.090036075
                return parseOracleDateTimeString(str);
            } catch (Exception e) {
                // 如果解析失败，尝试其他格式
                try {
                    // 尝试标准 ISO 格式
                    return LocalDateTime.parse(str.replace(' ', 'T'));
                } catch (Exception ex) {
                    throw new IllegalArgumentException("Cannot convert String to LocalDateTime: " + str, ex);
                }
            }
        }
        
        // 如果都不匹配，尝试转换为字符串后解析
        String str = obj.toString();
        try {
            return parseOracleDateTimeString(str);
        } catch (Exception e) {
            try {
                return LocalDateTime.parse(str.replace(' ', 'T'));
            } catch (Exception ex) {
                throw new IllegalArgumentException("Cannot convert " + obj.getClass().getName() + " to LocalDateTime: " + str, ex);
            }
        }
    }
    
    /**
     * 解析 Oracle 日期时间字符串格式
     * 支持的格式：
     * 1. 只有日期部分：21-DEC-25（时间部分默认为 00:00:00）
     * 2. 日期时间格式：21-NOV-25 02.41.32.090036075 PM
     */
    private LocalDateTime parseOracleDateTimeString(String str) {
        if (str == null || str.trim().isEmpty()) {
            return null;
        }
        
        try {
            // Oracle 格式：DD-MON-YY 或 DD-MON-YY HH.MI.SS.FF AM/PM
            // 例如：21-DEC-25 或 21-NOV-25 02.41.32.090036075 PM
            
            String trimmedStr = str.trim();
            String[] parts = trimmedStr.split("\\s+");
            
            String datePart = parts[0]; // 21-DEC-25 或 21-NOV-25
            String timePart = parts.length > 1 ? parts[1] : null; // 02.41.32.090036075 或 null
            String amPm = parts.length > 2 ? parts[2] : null; // PM 或 null
            
            // 解析日期部分
            String[] dateParts = datePart.split("-");
            if (dateParts.length != 3) {
                throw new IllegalArgumentException("Invalid date part format: " + datePart);
            }
            
            int day = Integer.parseInt(dateParts[0]);
            String monthStr = dateParts[1].toUpperCase();
            int year = Integer.parseInt(dateParts[2]);
            // Oracle 年份可能是 2 位，需要转换为 4 位
            if (year < 50) {
                year += 2000;
            } else {
                year += 1900;
            }
            
            // 月份映射
            java.util.Map<String, Integer> monthMap = new java.util.HashMap<>();
            monthMap.put("JAN", 1); monthMap.put("FEB", 2); monthMap.put("MAR", 3);
            monthMap.put("APR", 4); monthMap.put("MAY", 5); monthMap.put("JUN", 6);
            monthMap.put("JUL", 7); monthMap.put("AUG", 8); monthMap.put("SEP", 9);
            monthMap.put("OCT", 10); monthMap.put("NOV", 11); monthMap.put("DEC", 12);
            
            Integer month = monthMap.get(monthStr);
            if (month == null) {
                throw new IllegalArgumentException("Invalid month: " + monthStr);
            }
            
            // 解析时间部分（如果没有时间部分，默认为 00:00:00）
            int hour = 0;
            int minute = 0;
            int second = 0;
            
            if (timePart != null && !timePart.isEmpty()) {
                String[] timeParts = timePart.split("\\.");
                hour = Integer.parseInt(timeParts[0]);
                minute = timeParts.length > 1 ? Integer.parseInt(timeParts[1]) : 0;
                second = timeParts.length > 2 ? Integer.parseInt(timeParts[2]) : 0;
                
                // 处理 AM/PM
                if (amPm != null && amPm.equalsIgnoreCase("PM") && hour != 12) {
                    hour += 12;
                } else if (amPm != null && amPm.equalsIgnoreCase("AM") && hour == 12) {
                    hour = 0;
                }
            }
            
            return LocalDateTime.of(year, month, day, hour, minute, second);
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to parse Oracle date string: " + str, e);
        }
    }
}

