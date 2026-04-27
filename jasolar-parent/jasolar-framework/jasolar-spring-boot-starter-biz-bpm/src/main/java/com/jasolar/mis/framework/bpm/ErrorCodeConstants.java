package com.jasolar.mis.framework.bpm;

import com.jasolar.mis.framework.common.exception.ErrorCode;

/**
 * 
 * Bpm 错误码枚举类
 * 
 * @author galuo
 * @date 2025-05-22 00:53
 *
 */
public interface ErrorCodeConstants {

    /** 根据提交的数据没有找到任何符合条件的流程定义 */
    ErrorCode BPM_PROCESS_DEFINITION_NOT_FOUND = new ErrorCode("err.bpm.process.definition.not_found", "没有找到符合条件的流程定义");

    /** 根据提交的数据找到多个符合条件的流程定义 */
    ErrorCode BPM_PROCESS_DEFINITION_MULTIPLE = new ErrorCode("err.bpm.process.definition.multiple", "有多个符合条件的流程定义");
    /** 有需要手工指定审批人的流程节点 */
    ErrorCode BPM_PROCESS_HAS_MANUAL_NODES = new ErrorCode("err.bpm.process.has_manual_nodes", "有需要手工指定审批人的流程节点");

    /** 重复提交流程 */
    ErrorCode BPM_PROCESS_REPEATED_SUBMIT = new ErrorCode("err.bpm.process.repeated_submit", "请勿重复提交");
}
