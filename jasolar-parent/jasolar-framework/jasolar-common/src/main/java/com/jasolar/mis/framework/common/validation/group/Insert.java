package com.jasolar.mis.framework.common.validation.group;

import jakarta.validation.groups.Default;

/**
 * 仅在新增数据时校验. 比如某些增加后不可修改的字段, 如bizType
 * 
 * @author galuo
 * @date 2025-03-26 10:30
 *
 */
public interface Insert extends Default {

}
