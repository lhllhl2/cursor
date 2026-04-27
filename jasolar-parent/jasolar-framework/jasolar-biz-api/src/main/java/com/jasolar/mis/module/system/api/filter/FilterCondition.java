package com.jasolar.mis.module.system.api.filter;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 筛选条件
 *
 * @author DTT
 */
@Data
@Schema(description = "筛选条件")
public class FilterCondition implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 操作符
     * eq-等于
     * neq-不等于
     * gt-大于
     * gte-大于等于
     * lt-小于
     * lte-小于等于
     * range-范围
     * not_range-排除范围
     * like-模糊匹配
     */
    @Schema(description = "操作符", example = "eq")
    @JsonProperty("operator")
    private String operator;

    /**
     * 比较值
     * 当operator为range或not_range时，值应为包含两个元素的数组
     */
    @Schema(description = "比较值", example = "18")
    @JsonProperty("value")
    private Object value;

} 