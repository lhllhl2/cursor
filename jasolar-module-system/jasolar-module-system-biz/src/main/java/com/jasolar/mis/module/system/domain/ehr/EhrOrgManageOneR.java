package com.jasolar.mis.module.system.domain.ehr;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.jasolar.mis.framework.mybatis.core.dataobject.BaseDO;
import lombok.*;

/**
 * EHR组织管理一对一关系实体
 */
@TableName(value = "EHR_ORG_MANAGE_ONE_R", autoResultMap = true)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class EhrOrgManageOneR extends BaseDO {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** EHR组织编码 */
    private String ehrCd;

    /** 管理组织编码 */
    private String orgCd;
}

