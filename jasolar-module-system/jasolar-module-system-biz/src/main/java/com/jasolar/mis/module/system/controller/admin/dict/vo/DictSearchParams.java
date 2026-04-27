package com.jasolar.mis.module.system.controller.admin.dict.vo;

import com.jasolar.mis.framework.common.pojo.PageParam;
import lombok.Data;

/**
 * Description:
 * Author : Zhou Hai
 * Date : 31/07/2025 16:20
 * Version : 1.0
 */
@Data
public class DictSearchParams extends PageParam {


    private String code;

    private String title;

}
