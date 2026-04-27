package com.jasolar.mis.module.system.domain.admin.dict;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.jasolar.mis.framework.mybatis.core.dataobject.BaseDO;
import lombok.*;

/**
 * Description:
 * Author : Zhou Hai
 * Date : 31/07/2025 11:41
 * Version : 1.0
 */
@TableName(value = "system_dict_label", autoResultMap = true)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class SystemDictLabelDo extends BaseDO {

    @TableId(value = "id",type = IdType.ASSIGN_ID)
    private Long id ;

    /** 字典定义表id */
    private Long dictId ;

    /** 数据库真实的类型 */
    private String fieldKey ;

    /** 标签【前端映射】 */
    private String fieldLabel ;


}
