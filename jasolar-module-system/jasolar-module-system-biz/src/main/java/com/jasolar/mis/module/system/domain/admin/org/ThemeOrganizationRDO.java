package com.jasolar.mis.module.system.domain.admin.org;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.jasolar.mis.framework.mybatis.core.dataobject.BaseDO;
import lombok.*;

/**
 * 主题组织关联 DO
 *
 * @author jasolar
 */
@TableName("system_theme_organization_r")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ThemeOrganizationRDO extends BaseDO {

    /**
     * 主键ID（雪花算法）
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 组织代码
     */
    private String organizationCode;

    /**
     * 主题
     */
    private String theme;


    private String status;


}

