package com.jasolar.mis.framework.mybatis.core.dataobject;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * 所有业务表中的基类。id使用雪花算法生成
 * 
 * @author galuo
 * @date 2025-03-12 09:46
 *
 */
@Data
@SuperBuilder
@NoArgsConstructor
@SuppressWarnings("serial")
public abstract class BaseIdentityDO extends BaseDO {

    /** ID */
    @TableId(type = IdType.ASSIGN_ID)
    @Schema(description = "ID")
    private Long id;

}
