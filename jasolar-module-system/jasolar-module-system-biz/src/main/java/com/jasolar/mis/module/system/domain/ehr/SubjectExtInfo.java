package com.jasolar.mis.module.system.domain.ehr;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.jasolar.mis.framework.mybatis.core.dataobject.BaseDO;
import lombok.*;

/**
 * 科目扩展信息实体类
 * 
 * @author Auto Generated
 * @date 2025-01-XX
 */
@TableName(value = "subject_ext_info", autoResultMap = true)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class SubjectExtInfo extends BaseDO {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    
    /** ERP科目编码 */
    private String erpAcctCd;
    
    /** 编码 */
    private String acctCd;

}

