package com.jasolar.mis.module.system.mapper.admin.org;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.jasolar.mis.framework.common.pojo.PageParam;
import com.jasolar.mis.framework.common.pojo.PageResult;
import com.jasolar.mis.framework.mybatis.core.mapper.BaseMapperX;
import com.jasolar.mis.module.system.domain.ehr.ManageOrgView;
import org.apache.ibatis.annotations.Mapper;

/**
 * Description: 
 * Author : Zhou Hai
 * Date : 29/12/2025 18:19
 * Version : 1.0
 */
@Mapper
public interface MangeOrgViewMapper extends BaseMapperX<ManageOrgView> {
    
    /**
     * 分页查询组织视图数据
     * @param pageParam
     * @param queryWrapper
     * @return
     */
    default PageResult<ManageOrgView> selectPage(PageParam pageParam, LambdaQueryWrapper<ManageOrgView> queryWrapper) {
        com.baomidou.mybatisplus.extension.plugins.pagination.Page<ManageOrgView> mpPage = 
            new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(pageParam.getPageNo(), pageParam.getPageSize());
        com.baomidou.mybatisplus.extension.plugins.pagination.Page<ManageOrgView> pageResult = this.selectPage(mpPage, queryWrapper);
        return new PageResult<>(pageResult.getRecords(), pageResult.getTotal());
    }
}