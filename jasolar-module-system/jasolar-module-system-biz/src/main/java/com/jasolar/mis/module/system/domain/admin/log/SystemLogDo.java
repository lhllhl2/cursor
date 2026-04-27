package com.jasolar.mis.module.system.domain.admin.log;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.jasolar.mis.framework.mybatis.core.dataobject.BaseDO;
import lombok.*;

/**
 * Description:
 * Author : Zhou Hai
 * Date : 27/08/2025 14:35
 * Version : 1.0
 */
@TableName(value = "system_log", autoResultMap = true)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class SystemLogDo extends BaseDO {

    @TableId(value = "id",type = IdType.ASSIGN_ID)
    private Long id ;

    private String userName;

    private String displayName;

    private String ip;

    private String logType;





}
