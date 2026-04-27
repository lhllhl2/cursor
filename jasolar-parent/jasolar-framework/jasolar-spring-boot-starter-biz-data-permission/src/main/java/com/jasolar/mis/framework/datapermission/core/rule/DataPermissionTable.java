package com.jasolar.mis.framework.datapermission.core.rule;

import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.reflection.property.PropertyNamer;

import com.baomidou.mybatisplus.core.metadata.TableFieldInfo;
import com.baomidou.mybatisplus.core.metadata.TableInfo;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.baomidou.mybatisplus.core.toolkit.LambdaUtils;
import com.baomidou.mybatisplus.core.toolkit.StringPool;
import com.baomidou.mybatisplus.core.toolkit.support.LambdaMeta;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import com.jasolar.mis.framework.datapermission.core.annotation.DataTable;

import jakarta.annotation.Nullable;
import lombok.Data;
import net.sf.jsqlparser.schema.Table;

/**
 * 从{@link DataTable}中解析的表对象
 */
@Data
public class DataPermissionTable {

    /** schema名称 */
    private String schema;

    /** 原始表名 */
    private String name;

    /** 查询使用的表别名，一般有别名则不需要指定原始表名和schema */
    private String alias;

    /**
     * 使用的列名,可以有多个列，多个列之间用逗号隔开,一个表有多列则多列之间是OR的关系。
     * 未指定则使用规则的默认列名.
     */
    private final Set<String> columns = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);

    // /** 允许查询NULL值的列, 必须属于{@link #columns()}中的某一列. 此参数值只能通过注解获取,默认没有 */
    // private final Set<String> nullableColumns = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);

    /** 使用的BPM_USER表DO对象 */
    private BpmTaskTableCfg bpmTaskTable;

    /**
     * 增加允许列
     * 
     * @param columns
     */
    public void addColumns(String... columns) {
        if (columns != null) {
            for (String c : columns) {
                this.columns.add(c);
            }
        }
    }

    // /**
    // * 判断列是否允许NULL值查询
    // *
    // * @param columnName 列名
    // * @return 是否允许查询NULL值
    // */
    // public boolean nullable(String columnName) {
    // return this.nullableColumns.contains(columnName);
    // }
    //
    // /**
    // * 增加允许null值的列
    // *
    // * @param nullableColumns 允许查询NULL值的列
    // */
    // public void addNullableColumns(String... nullableColumns) {
    // if (nullableColumns != null) {
    // for (String c : nullableColumns) {
    // this.nullableColumns.add(c);
    // }
    // }
    // }

    /**
     * 设置表名, 可能是schema.tablename的格式
     * 
     * @param tableName 表名,可能含有schema
     */
    public void setName(String tableName) {
        if (StringUtils.isBlank(tableName) || !tableName.contains(StringPool.DOT)) {
            this.name = tableName;
            return;
        }
        String[] arr = tableName.split(".");
        if (arr.length > 1) {
            this.schema = arr[0];
            this.name = arr[1];
        } else {
            this.name = arr[0];
        }
    }

    /**
     * 是否和解析出来的表一致
     * 
     * @param table 解析到的表对象
     * @return 如果是一致的表则返回true，否则返回false
     */
    public boolean match(Table table) {
        if (StringUtils.isNotBlank(schema) && !schema.equalsIgnoreCase(table.getSchemaName())) {
            // schema不一致
            return false;
        }
        if (StringUtils.isNotBlank(name) && !name.equalsIgnoreCase(table.getName())) {
            // 原始表名 不一致
            return false;
        }
        if (StringUtils.isNotBlank(alias) && (table.getAlias() == null || !alias.equalsIgnoreCase(table.getAlias().getName()))) {
            // 表别名 不一致
            return false;
        }

        return true;
    }

    /**
     * 
     * 构建数据
     * 
     * @param schema 表的Schema
     * @param tableName 表名
     * @param alias 表别名
     * @param columns 所有列名
     * @return 表数据
     */
    public static DataPermissionTable of(@Nullable String schema, @Nullable String tableName, @Nullable String alias,
            @Nullable String[] columns) {
        DataPermissionTable t = new DataPermissionTable();
        t.setName(tableName);
        if (columns != null) {
            t.addColumns(columns);
        }
        // if (nullableColumns != null) {
        // t.addNullableColumns(nullableColumns);
        // }
        return t;
    }

    /**
     * 通过实体类和get方法构建表
     * 
     * @param entityClass 实体类
     * @param fields 对应的列字段
     * @return 表数据
     */
    @SuppressWarnings("unchecked")
    public static <T> DataPermissionTable of(Class<T> entityClass, @Nullable SFunction<? extends T, ?>... fields) {
        DataPermissionTable t = new DataPermissionTable();
        TableInfo ti = TableInfoHelper.getTableInfo(entityClass);
        t.setName(ti.getTableName());

        if (fields == null || fields.length < 1) {
            return t;
        }
        Set<String> fieldNames = new HashSet<>();
        Set<String> columns = new HashSet<>();
        for (SFunction<?, ?> column : fields) {
            LambdaMeta meta = LambdaUtils.extract(column);
            String fieldName = PropertyNamer.methodToProperty(meta.getImplMethodName());
            if (fieldName.equals(ti.getKeyProperty())) {
                columns.add(ti.getKeyColumn());
            } else {
                fieldNames.add(fieldName);
            }
        }

        if (!fieldNames.isEmpty()) {
            ti.getFieldList().stream().filter(f -> fieldNames.contains(f.getProperty())).map(TableFieldInfo::getColumn)
                    .forEach(columns::add);
        }
        t.getColumns().addAll(columns);
        return t;

    }

    /**
     * 获取表对象字段的列名
     * 
     * @param <T>
     * @param entityClass 表对象的class
     * @param field 字段函数
     * @return 列名
     */
    public static <T> String getColumnName(Class<T> entityClass, @Nullable SFunction<? extends T, ?> field) {
        DataPermissionTable t = new DataPermissionTable();
        TableInfo ti = TableInfoHelper.getTableInfo(entityClass);
        t.setName(ti.getTableName());

        LambdaMeta meta = LambdaUtils.extract(field);
        String fieldName = PropertyNamer.methodToProperty(meta.getImplMethodName());
        if (fieldName.equals(ti.getKeyProperty())) {
            return ti.getKeyColumn();
        }

        return ti.getFieldList().stream().filter(f -> fieldName.equals(f.getProperty())).map(TableFieldInfo::getColumn).findAny().get();
    }
}
