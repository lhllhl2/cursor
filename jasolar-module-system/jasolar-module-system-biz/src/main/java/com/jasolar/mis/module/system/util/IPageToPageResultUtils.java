package com.jasolar.mis.module.system.util;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.jasolar.mis.framework.common.pojo.PageResult;

/**
 * Description:
 * Author : Zhou Hai
 * Date : 23/07/2025 17:04
 * Version : 1.0
 */
public class IPageToPageResultUtils {


    public static <T> PageResult<T> transfer(IPage<T> page){
        PageResult<T> pageResult = new PageResult<>();
        pageResult.setTotal(page.getTotal());
        pageResult.setList(page.getRecords());
       return pageResult;
    }


}
