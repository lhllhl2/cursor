package com.jasolar.mis.framework.mybatis.core.dataobject;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * 默认业务实体基类，包含所有数据权限字段。 id使用雪花算法自动生成
 * 
 * @author galuo
 * @date 2025-03-04 11:07
 *
 */
@SuppressWarnings("serial")
@Data
@SuperBuilder
@NoArgsConstructor
public abstract class BaseScopeDO extends BaseOrgScopeDO implements IBaseScopeDO {

    /** 数据所属法人编号 */
    @Schema(description = "数据所属法人编号")
    private String legalCode;

    /** 事业处编号 */
    @Schema(description = " 事业处编号")
    private String businessUnitCode;

    /** 事业群编号 */
    @Schema(description = " 事业群编号")
    private String businessGroupCode;

}
