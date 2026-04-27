package com.jasolar.mis.framework.mybatis.core.enums;

import com.baomidou.mybatisplus.core.conditions.ISqlSegment;

import lombok.AllArgsConstructor;

/**
 * 扩展的SQL关键字, 主要用于增加postgresql的ilike
 * 
 * @author galuo
 * @date 2025-04-09 09:51
 *
 */
@AllArgsConstructor
public enum ExtSqlKeyword implements ISqlSegment {

    /** 忽略大小写查询 */
    ILIKE("ILIKE");

    private final String keyword;

    @Override
    public String getSqlSegment() {
        return this.keyword;
    }

}
