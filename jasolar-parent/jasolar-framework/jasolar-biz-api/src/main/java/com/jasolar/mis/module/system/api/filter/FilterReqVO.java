package com.jasolar.mis.module.system.api.filter;

import com.jasolar.mis.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * 高级筛选条件请求VO
 *
 * @author DTT
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "管理后台 - 高级筛选条件 Request VO")
public class FilterReqVO extends PageParam implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 包含条件映射
     * key: 字段名
     * value: 该字段的筛选条件列表
     */
    @Schema(description = "包含条件映射")
    private Map<String, List<FilterCondition>> includeMap;

    /**
     * 排除条件映射
     * key: 字段名
     * value: 该字段的筛选条件列表
     */
    @Schema(description = "排除条件映射")
    private Map<String, List<FilterCondition>> excludeMap;

} 