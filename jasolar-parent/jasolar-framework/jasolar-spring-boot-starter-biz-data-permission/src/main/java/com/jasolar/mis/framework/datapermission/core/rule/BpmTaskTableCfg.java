package com.jasolar.mis.framework.datapermission.core.rule;

import com.jasolar.mis.framework.datapermission.core.annotation.BpmTaskTable;
import com.jasolar.mis.framework.mybatis.core.dataobject.BaseBpmTaskDO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * BPM_USER表的配置
 * 
 * @author galuo
 * @date 2025-03-11 17:38
 * 
 * @see BaseBpmTaskDO
 * @see BpmTaskTable
 *
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BpmTaskTableCfg {

    /** 表名 */
    private String name;

    /** 业务id字段对应的列名 */
    private String bizColumn;

    /** 人员账号字段对应的列名 */
    private String[] userColumns;

    /** 流程定义字段对应的列名 */
    private String bizTypeColumn;

    /**
     * 业务类型,每个业务表可以对应多个业务类型.
     * 因为业务单号基本上是全局唯一, 所以一般不用指定，直接使用bizNo查询即可。
     * 如果指定了此参数，则会添加bizType字段作为查询条件
     */
    private String[] bizTypes;
}
