package com.jasolar.mis.framework.data.core;

import java.io.Serializable;

import com.jasolar.mis.framework.common.enums.CommonStatusEnum;

import lombok.Data;

/**
 * 字典分类
 * 
 * @author galuo
 * @date 2025-03-28 13:46
 *
 */
@SuppressWarnings("serial")
@Data
public class DictType implements Serializable {

    /** 字典名称 */
    private String name;
    /** 字典类型 */
    private String type;
    /** 上级字典类型 */
    private String parentType;

    /**
     * 状态
     *
     * 枚举 {@link CommonStatusEnum}
     */
    private Integer status;

}
