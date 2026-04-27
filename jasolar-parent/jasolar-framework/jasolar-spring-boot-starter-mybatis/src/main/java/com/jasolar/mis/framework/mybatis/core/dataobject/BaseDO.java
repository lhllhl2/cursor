package com.jasolar.mis.framework.mybatis.core.dataobject;

import java.io.Serializable;
import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;
import org.apache.ibatis.type.JdbcType;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fhs.core.trans.vo.TransPojo;
import com.jasolar.mis.framework.mybatis.core.type.OracleLocalDateTimeTypeHandler;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * 基础实体对象。infra/system/bpm这3个服务中id使用序列的会继承此类
 *
 * 为什么实现 {@link TransPojo} 接口？
 * 因为使用 Easy-Trans TransType.SIMPLE 模式，集成 MyBatis Plus 查询
 *
 * @author zhaohuang
 */
@SuppressWarnings("serial")
@Data
@SuperBuilder
@NoArgsConstructor
@JsonIgnoreProperties(value = "transMap") // 由于 Easy-Trans 会添加 transMap 属性，避免 Jackson 在 Spring Cache 反序列化报错
public abstract class BaseDO implements Serializable, TransPojo {

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT, typeHandler = OracleLocalDateTimeTypeHandler.class)
    @Schema(description = "创建时间")
    private LocalDateTime createTime;
    /**
     * 最后更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE, typeHandler = OracleLocalDateTimeTypeHandler.class)
    @Schema(description = "最后更新时间")
    private LocalDateTime updateTime;
    /**
     * 创建者，目前使用 SysUser 的 id 编号
     *
     * 使用 String 类型的原因是，未来可能会存在非数值的情况，留好拓展性。
     */
    @TableField(fill = FieldFill.INSERT, jdbcType = JdbcType.VARCHAR)
    @Schema(description = "创建者")
    private String creator;
    /**
     * 更新者，目前使用 SysUser 的 id 编号
     *
     * 使用 String 类型的原因是，未来可能会存在非数值的情况，留好拓展性。
     */
    @TableField(fill = FieldFill.INSERT_UPDATE, jdbcType = JdbcType.VARCHAR)
    @Schema(description = "更新者")
    private String updater;
    /**
     * 是否删除
     */
    @TableLogic
    @Schema(description = "是否删除")
    private Boolean deleted;

}
