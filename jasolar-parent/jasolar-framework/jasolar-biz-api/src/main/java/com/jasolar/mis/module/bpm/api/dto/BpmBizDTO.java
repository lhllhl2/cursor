package com.jasolar.mis.module.bpm.api.dto;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 流程业务DTO
 * 
 * @author galuo
 * @date 2025-05-22 12:42
 *
 */
@Data
@SuppressWarnings("serial")
public class BpmBizDTO extends BizDTO {

    @Schema(description = "业务标题")
    @Size(max = 100)
    private String name;

    @Schema(description = "申请人账号")
    @NotBlank
    @Size(max = 30)
    private String userNo;

    /** 数据所属部门ID */
    @Schema(description = "数据所属部门编号")
    @Size(max = 30)
    private String deptCode;

    /** 数据所属法人编号 */
    @Schema(description = "数据所属法人编号")
    @Size(max = 30)
    private String legalCode;

    /** 事业处编号 */
    @Schema(description = " 事业处编号")
    @Size(max = 30)
    private String businessUnitCode;

    /** 事业群编号 */
    @Schema(description = " 事业群编号")
    @Size(max = 30)
    private String businessGroupCode;

    /** 流程变量, 使用ConcurrentHashMap防止并发问题, 注意ConcurrentHashMap的key和value都不允许为NULL */
    @Schema(description = "流程变量")
    private final Map<String, Object> variables = new ConcurrentHashMap<>();

    /**
     * 设置变量, 如果已有变量会被清空
     * 
     * @param variables
     */
    public void setVariables(@Nullable Map<String, Object> variables) {
        this.variables.clear();
        this.putVariables(variables);
    }

    /**
     * 添加多个变量
     * 
     * @param variables
     */
    public void putVariables(@Nullable Map<String, Object> variables) {
        if (variables != null) {
            variables.forEach(this::setVariable);
        }
    }

    /**
     * 设置一个变量, 如果变量存在则不做修改
     * 
     * @param key 变量名
     * @param value 变量值
     */
    public void setVariableIfAbsent(String key, @Nullable Object value) {
        if (value != null) {
            this.variables.putIfAbsent(key, value);
        }
    }

    /**
     * 设置一个变量
     * 
     * @param key 变量名
     * @param value 变量值, 如果为null则表示删除变量
     */
    public void setVariable(String key, @Nullable Object value) {
        if (value == null) {
            this.removeVariable(key);
        } else {
            this.variables.put(key, value);
        }
    }

    /**
     * 
     * 获取变量的值
     * 
     * @param <T> 变量类型
     * @param key 变量名
     * @return 变量值
     */
    @SuppressWarnings("unchecked")
    public <T> T getVariable(String key) {
        return (T) variables.get(key);
    }

    /**
     * 删除变量,并返回变量值
     * 
     * @param <T> 变量类型
     * 
     * @param key 变量名
     * @return 返回被删除的变量的值
     */
    @SuppressWarnings("unchecked")
    public <T> T removeVariable(String key) {
        return (T) variables.remove(key);
    }

}
