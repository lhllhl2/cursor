package com.jasolar.mis.module.system.domain.ehr;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.jasolar.mis.framework.mybatis.core.dataobject.BaseDO;
import lombok.*;

/**
 * Description:
 * Author : Zhou Hai
 * Date : 09/12/2025 14:51
 * Version : 1.0
 */
@TableName(value = "ehr_org_manage_r", autoResultMap = true)
@Data
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class EhrOrgManageR extends BaseDO {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id ;
    /** 管理组织编码,; */
    private String orgCd ;
    /** 管理组织名称,; */
    private String orgNm ;

    /** EHR组织父级编码,; */
    private String ehrCd ;

    /** EHR组织父级编码,; */
    private String ehrNm ;

    private String ehrParCd;

    private String ehrParNm;

    /** 控制层级,; */
    private String controlLevel ;

    /** 编制层级,; */
    private String bzLevel ;

    /** ERP部门,; */
    private String erpDepart ;

    /** 年份,; */
    private String year ;

    /** 是否末级节点, 0=否 1=是 */
    private String leaf ;

}
