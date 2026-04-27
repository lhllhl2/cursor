package com.jasolar.mis.module.infra.api.biztask.dto;

import java.io.Serializable;

import com.jasolar.mis.framework.common.enums.UserTypeEnum;

import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 待办的业务主键, 用于完成待办等
 * 
 * @author galuo
 * @date 2025-04-11 18:18
 *
 */
@Data
@SuppressWarnings("serial")
public class BizTaskKeyDTO implements Serializable {

    /** 所属模块;参见枚举: com.jasolar.mis.module.bpm.enums.ModuleEnum */
    @Size(max = 30)
    private String module;
    /** 字典:common_biz_task_type。待办类型;java中定义为枚举类型供业务端调用 */
    @Size(max = 50)
    private String type;

    /** 字典:bpm_biz_type。业务类型;业务类型, 与流程中的业务类型一致 */
    @Size(max = 30)
    private String bizType;

    /** 业务ID */
    private Long bizId;

    /** 业务单号 */
    @Size(max = 30)
    private String bizNo;

    /**
     * 用户类型;0.采购平台用户, 1供应商用户
     * 
     * @see UserTypeEnum
     */
    private Integer userType;
    /** 用户编号 */
    @Size(max = 30)
    private String userNo;
}
