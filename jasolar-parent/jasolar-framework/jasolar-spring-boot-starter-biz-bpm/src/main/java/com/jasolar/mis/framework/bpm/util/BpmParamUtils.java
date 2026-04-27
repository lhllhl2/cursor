package com.jasolar.mis.framework.bpm.util;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import com.jasolar.mis.framework.common.util.object.BeanUtils;
import com.jasolar.mis.framework.data.util.UserUtils;
import com.jasolar.mis.module.bpm.api.dto.BpmBizDTO;

/**
 * 一些特殊的流程参数,
 * 
 * @author galuo
 * @date 2025-05-22 12:21
 *
 */
public interface BpmParamUtils {

    /** 特殊配置的变量, 流程实例标题, 此参数不会写入流程变量中, 而是写入流程实例的name字段. 如果没有配置此参数,则默认使用流程名称作为流程实例的标题. 判断条件时可以使用,会注入到条件解析的参数中 */
    String TITLE = "title";

    /** 业务类型 */
    String BIZ_TYPE = "bizType";
    /** 业务ID */
    String BIZ_ID = "bizId";
    // /** 与流程实例的business_key一致 */
    // String BIZ_NO = "bizNo";

    /** 申请人工号 */
    String USER_NO = "userNo";
    /** 申请人名称 */
    String USER_NAME = "userName";
    /** 申请部门 */
    String DEPT_CODE = "deptCode";
    /** 申请法人 */
    String LEGAL_CODE = "legalCode";
    /** 申请事业群 */
    String BUSINESS_GROUP_CODE = "businessGroupCode";
    /** 申请事业处 */
    String BUSINESS_UNIT_CODE = "businessUnitCode";

    /** 需要从业务字段中写入的流程变量 */
    Set<String> WRITE_PARAMS = new HashSet<>(
            Arrays.asList(BIZ_TYPE, BIZ_ID, USER_NO, DEPT_CODE, LEGAL_CODE, BUSINESS_GROUP_CODE, BUSINESS_UNIT_CODE));

    /**
     * 初始化流程变量, 将业务字段写入变量参数{@link WRITE_PARAMS}中. 如果变量已存在或者字段为null则不会写入
     * 
     * @param dto 流程DTO
     */
    static void writeVariables(BpmBizDTO dto) {
        for (String key : WRITE_PARAMS) {
            Object value = BeanUtils.getPropertyValue(dto, key);
            if (value != null) {
                dto.setVariableIfAbsent(key, value);
            }
        }

        String userNo = dto.getVariable(USER_NO);
        if (StringUtils.isNotBlank(userNo)) {
            // 写入申请人名称
            dto.setVariableIfAbsent(USER_NAME, UserUtils.getName(userNo));
        }
    }

    /**
     * 从变量参数{@link WRITE_PARAMS}中读取业务字段. 如果参数值为null则不会读取
     * 
     * @param dto 流程DTO
     */
    static void readVariables(BpmBizDTO dto) {
        for (String key : WRITE_PARAMS) {
            Object value = dto.getVariable(key);
            if (value != null) {
                BeanUtils.setPropertyValue(dto, key, value);
            }
        }
    }

}
