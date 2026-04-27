package com.jasolar.mis.module.system.domain.morg;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.jasolar.mis.framework.mybatis.core.dataobject.BaseDO;
import java.time.LocalDateTime;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@TableName(value = "system_management_organization", autoResultMap = true)
public class SystemManagementOrganization extends BaseDO {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private String orgCode;

    private String deptCode;

    private String morgCode;

    private String morgName;

    private LocalDateTime effectiveStart;

    private LocalDateTime effectiveEnd;
}

