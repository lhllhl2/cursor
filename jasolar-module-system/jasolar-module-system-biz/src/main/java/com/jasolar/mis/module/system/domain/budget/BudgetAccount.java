package com.jasolar.mis.module.system.domain.budget;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.jasolar.mis.framework.mybatis.core.dataobject.BaseDO;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * 预算科目实体类
 *
 * @author Auto Generated
 * @date 2025-01-XX
 */
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@TableName(value = "budget_account", autoResultMap = true)
public class BudgetAccount extends BaseDO {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 自定义编码
     */
    private String customCode;

    /**
     * 预算科目编码
     */
    private String accountSubjectCode;

    /**
     * 预算科目名称
     */
    private String accountName;

    /**
     * 父级预算科目编码
     */
    private String parentAccountSubjectCode;

    /**
     * 是否叶子节点(1:是,0:否)
     */
    private Integer isLeaf;
}

