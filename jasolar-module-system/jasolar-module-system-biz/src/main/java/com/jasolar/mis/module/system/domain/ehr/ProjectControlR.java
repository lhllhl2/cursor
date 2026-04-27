package com.jasolar.mis.module.system.domain.ehr;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.jasolar.mis.framework.mybatis.core.dataobject.BaseDO;
import lombok.*;

/**
 * Description:
 * Author : Zhou Hai
 * Date : 11/12/2025 10:22
 * Version : 1.0
 */
@TableName(value = "project_control_r", autoResultMap = true)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class ProjectControlR extends BaseDO {

    /** 主键,; */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id ;

    private Long prjId;

    /** 项目编码,; */
    private String prjCd ;
    /** 项目名称,; */
    private String prjNm ;
    /** 父项目编码,; */
    private String parCd ;
    /** 父项目名称,; */
    private String parNm ;
    /** 是否为叶节点 1是 0否,; */
    private boolean leaf ;
    /** 控制层级,; */
    private String controlLevel ;
    /** 年份,; */
    private String year ;


}
