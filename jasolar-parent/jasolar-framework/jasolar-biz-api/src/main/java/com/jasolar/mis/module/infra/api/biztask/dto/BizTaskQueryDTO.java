package com.jasolar.mis.module.infra.api.biztask.dto;

import java.time.LocalDateTime;
import java.util.List;

import com.jasolar.mis.framework.common.enums.UserTypeEnum;
import com.jasolar.mis.framework.common.pojo.PageParam;

import lombok.Data;

/**
 * 待办查询条件
 *
 * @author galuo
 * @date 2025-04-14 16:11
 *
 */
@SuppressWarnings("serial")
@Data
public class BizTaskQueryDTO extends PageParam {

    /** 所属模块;参见枚举: com.jasolar.mis.module.bpm.enums.ModuleEnum */
    private List<String> modules;
    /** 字典:common_biz_task_type。待办类型;java中定义为枚举类型供业务端调用 */
    private List<String> types;

    /** 字典:bmp_biz_type。业务类型;业务类型, 与流程中的业务类型一致 */
    private List<String> bizTypes;
    /** 业务ID */
    private List<Long> bizIds;
    /** 业务单号 */
    private List<String> bizNos;

    /**
     * 用户类型;0.采购平台用户, 1供应商用户. 只能同时查询一种用户
     *
     * @see UserTypeEnum
     */
    private Integer userType;

    /** 用户编号 */
    private List<String> userNos;

    /** 标题模糊搜索 */
    private String title;

    /** 查询的状态 */
    private String status;

    /** 发送人姓名模糊搜索 */
    private String senderName;

    /** 发送时间开始 */
    private LocalDateTime submitTimeStart;

    /** 发送时间结束 */
    private LocalDateTime submitTimeEnd;
}