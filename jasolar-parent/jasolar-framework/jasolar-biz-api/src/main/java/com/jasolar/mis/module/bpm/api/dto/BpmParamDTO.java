package com.jasolar.mis.module.bpm.api.dto;

import lombok.Data;

/**
 * 流程参数定义
 * 
 * @author galuo
 * @date 2025-05-22 00:32
 *
 */
@Data
public class BpmParamDTO {
    /**
     * 参数编码/key;提交给流程的变量名
     */
    private String paramKey;
    /**
     * 参数名称
     */
    private String paramName;
    /**
     * 对象中参数读取的表达式;用于从业务DTO中中读取参数值的EL表达式, 如: ${ no } 表示获取业务单号, ${ items.![ code ] }表示获取明细行中的code字段列表, 还有通过T调用静态工具类: ${
     * T(com.fiifoxconn.mis.framework.common.util.collection.CollectionUtils).distinct( items.![ code ] ) }表示获取明细行中去重后的code字段列表
     */
    private String paramExpression;

}
