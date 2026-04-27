package com.jasolar.mis.module.system.domain.admin.org;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.jasolar.mis.framework.mybatis.core.dataobject.BaseDO;
import lombok.*;

/**
 * Description:
 * Author : Zhou Hai
 * Date : 11/08/2025 15:07
 * Version : 1.0
 */
@TableName(value = "system_user_org_r", autoResultMap = true)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class SystemUserOrgRDo extends BaseDO {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long userId;

    private String orgId;

    // 是否为 主要组织 1：是  0：否
    private String mainOu;




}
