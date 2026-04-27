package com.jasolar.mis.module.system.controller.ehr.vo;

import com.jasolar.mis.framework.common.pojo.PageParam;
import lombok.Data;

/**
 * ProjectControlR 查询参数 VO
 */
@Data
public class ProjectControlRSearchVo extends PageParam {

    private String year;

    private String prjCd;

    private String prjNm;

}