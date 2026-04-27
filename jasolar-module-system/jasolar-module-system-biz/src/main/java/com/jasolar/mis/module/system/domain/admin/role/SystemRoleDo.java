package com.jasolar.mis.module.system.domain.admin.role;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.jasolar.mis.framework.mybatis.core.dataobject.BaseDO;
import lombok.*;

/**
 * Description:
 * Author : Zhou Hai
 * Date : 24/07/2025 16:48
 * Version : 1.0
 */
@TableName("system_role")
@Data
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SystemRoleDo extends BaseDO {

    @TableId(value = "id",type = IdType.ASSIGN_ID)
    private Long id;

    private String code;

    private String name;

    private String status;

    private String remark;


}
