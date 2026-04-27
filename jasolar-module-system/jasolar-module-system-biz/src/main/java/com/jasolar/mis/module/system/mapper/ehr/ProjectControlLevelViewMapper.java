package com.jasolar.mis.module.system.mapper.ehr;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.jasolar.mis.framework.common.pojo.PageParam;
import com.jasolar.mis.framework.common.pojo.PageResult;
import com.jasolar.mis.framework.mybatis.core.mapper.BaseMapperX;
import com.jasolar.mis.module.system.domain.ehr.ProjectControlLevelView;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 项目控制层级视图 Mapper
 * 
 * @author Auto Generated
 * @date 2025-01-XX
 */
@Mapper
public interface ProjectControlLevelViewMapper extends BaseMapperX<ProjectControlLevelView> {

    /**
     * 根据PRJ_CD查询
     * 
     * @param prjCd 项目编码
     * @return 项目控制层级视图
     */
    default ProjectControlLevelView selectByPrjCd(String prjCd) {
        LambdaQueryWrapper<ProjectControlLevelView> wrapper = new LambdaQueryWrapper<ProjectControlLevelView>()
                .eq(ProjectControlLevelView::getPrjCd, prjCd);
        return selectOne(wrapper);
    }

    /**
     * 根据CONTROL_PRJ_CD查询
     * 
     * @param controlPrjCd 控制层级项目编码
     * @return 项目控制层级视图列表
     */
    default List<ProjectControlLevelView> selectByControlPrjCd(String controlPrjCd) {
        LambdaQueryWrapper<ProjectControlLevelView> wrapper = new LambdaQueryWrapper<ProjectControlLevelView>()
                .eq(ProjectControlLevelView::getControlPrjCd, controlPrjCd);
        return selectList(wrapper);
    }

    /**
     * 根据PRJ_CD列表批量查询
     * 
     * @param prjCds 项目编码列表
     * @return 项目控制层级视图列表
     */
    default List<ProjectControlLevelView> selectByPrjCds(List<String> prjCds) {
        LambdaQueryWrapper<ProjectControlLevelView> wrapper = new LambdaQueryWrapper<ProjectControlLevelView>()
                .in(ProjectControlLevelView::getPrjCd, prjCds);
        return selectList(wrapper);
    }

    /**
     * 分页查询项目控制层级视图数据
     * 
     * @param pageParam 分页参数
     * @param queryWrapper 查询条件
     * @return 分页结果
     */
    default PageResult<ProjectControlLevelView> selectPage(PageParam pageParam, LambdaQueryWrapper<ProjectControlLevelView> queryWrapper) {
        com.baomidou.mybatisplus.extension.plugins.pagination.Page<ProjectControlLevelView> mpPage = 
            new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(pageParam.getPageNo(), pageParam.getPageSize());
        com.baomidou.mybatisplus.extension.plugins.pagination.Page<ProjectControlLevelView> pageResult = this.selectPage(mpPage, queryWrapper);
        return new PageResult<>(pageResult.getRecords(), pageResult.getTotal());
    }
}

