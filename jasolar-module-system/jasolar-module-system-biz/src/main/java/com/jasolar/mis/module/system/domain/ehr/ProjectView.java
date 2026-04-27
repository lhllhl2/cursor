package com.jasolar.mis.module.system.domain.ehr;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

/**
 * Description:
 * Author : Zhou Hai
 * Date : 15/12/2025 10:22
 * Version : 1.0
 */
@TableName(value = "dataintegration.view_hsp_project", autoResultMap = true)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectView {

    @TableId(value = "prj_id")
    private Long id ;

    private String prjCd ;
    /** 项目名称,; */
    private String prjNm ;
    /** 父项目编码,; */
    private String parCd ;
    /** 父项目名称,; */
    private String parNm ;
    /** 是否为叶节点 1是 0否,; */
    @TableField(value = "is_leaf")
    private boolean leaf ;

}