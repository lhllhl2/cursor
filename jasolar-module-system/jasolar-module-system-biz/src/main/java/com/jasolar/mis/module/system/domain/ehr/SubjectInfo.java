package com.jasolar.mis.module.system.domain.ehr;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.jasolar.mis.framework.mybatis.core.dataobject.BaseDO;
import lombok.*;

/**
 * Description:
 * Author : Zhou Hai
 * Date : 10/12/2025 14:39
 * Version : 1.0
 */
@TableName(value = "subject_info", autoResultMap = true)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode(callSuper = true)
public class SubjectInfo extends BaseDO {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id ;

    private String cust1Cd ;

    private String cust1Nm ;

    /** 编码,; */
    private String acctCd ;
    /** 科目名称,; */
    private String acctNm ;
    /** 父编码,; */
    private String acctParCd ;
    /** 父科目名称,; */
    private String acctParNm ;
    /** 是否叶节点：1 是  0 否,; */
    private Boolean leaf ;
    /** ERP科目编码,; */
    private String erpAcctCd ;
    /** ERP科目名称,; */
    private String erpAcctNm ;
    /** 年份,; */
    private String year ;
    /** 控制层级,; */
    private String controlLevel ;



}
