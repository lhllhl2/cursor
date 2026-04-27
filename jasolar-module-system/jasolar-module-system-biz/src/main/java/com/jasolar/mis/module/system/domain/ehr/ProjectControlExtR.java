package com.jasolar.mis.module.system.domain.ehr;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.jasolar.mis.framework.mybatis.core.dataobject.BaseDO;
import lombok.*;

/**
 * 项目控制扩展关系实体类
 * 
 * @author Auto Generated
 * @date 2025-01-XX
 */
@TableName(value = "project_control_ext_r", autoResultMap = true)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class ProjectControlExtR extends BaseDO {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    
    /** 项目编码 */
    private String prjCd;
    
    /** 关联项目编码 */
    private String relatedPrjCd;

}

