package com.jasolar.mis.module.system.mapper.ehr;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.jasolar.mis.framework.common.pojo.PageParam;
import com.jasolar.mis.framework.common.pojo.PageResult;
import com.jasolar.mis.framework.mybatis.core.mapper.BaseMapperX;
import com.jasolar.mis.module.system.domain.ehr.ProjectControlView;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 项目控制层级视图（优化版） Mapper
 * 
 * @author Auto Generated
 * @date 2025-01-XX
 */
@Mapper
public interface ProjectControlViewMapper extends BaseMapperX<ProjectControlView> {

    /**
     * 根据PRJ_CD查询
     * 
     * @param prjCd 项目编码
     * @return 项目控制层级视图
     */
    default ProjectControlView selectByPrjCd(String prjCd) {
        LambdaQueryWrapper<ProjectControlView> wrapper = new LambdaQueryWrapper<ProjectControlView>()
                .eq(ProjectControlView::getPrjCd, prjCd);
        return selectOne(wrapper);
    }

    /**
     * 根据CONTROL_PRJ_CD查询
     * 
     * @param controlPrjCd 控制层级项目编码
     * @return 项目控制层级视图列表
     */
    default List<ProjectControlView> selectByControlPrjCd(String controlPrjCd) {
        LambdaQueryWrapper<ProjectControlView> wrapper = new LambdaQueryWrapper<ProjectControlView>()
                .eq(ProjectControlView::getControlPrjCd, controlPrjCd);
        return selectList(wrapper);
    }

    /**
     * 根据PRJ_CD列表批量查询
     * 
     * @param prjCds 项目编码列表
     * @return 项目控制层级视图列表
     */
    default List<ProjectControlView> selectByPrjCds(List<String> prjCds) {
        LambdaQueryWrapper<ProjectControlView> wrapper = new LambdaQueryWrapper<ProjectControlView>()
                .in(ProjectControlView::getPrjCd, prjCds);
        return selectList(wrapper);
    }

    /**
     * 根据CONTROL_PRJ_CD列表批量查询
     * 
     * @param controlPrjCds 控制层级项目编码列表
     * @return 项目控制层级视图列表
     */
    default List<ProjectControlView> selectByControlPrjCds(List<String> controlPrjCds) {
        LambdaQueryWrapper<ProjectControlView> wrapper = new LambdaQueryWrapper<ProjectControlView>()
                .in(ProjectControlView::getControlPrjCd, controlPrjCds);
        return selectList(wrapper);
    }

    /**
     * 分页查询项目控制层级视图数据
     * 
     * @param pageParam 分页参数
     * @param queryWrapper 查询条件
     * @return 分页结果
     */
    default PageResult<ProjectControlView> selectPage(PageParam pageParam, LambdaQueryWrapper<ProjectControlView> queryWrapper) {
        com.baomidou.mybatisplus.extension.plugins.pagination.Page<ProjectControlView> mpPage = 
            new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(pageParam.getPageNo(), pageParam.getPageSize());
        com.baomidou.mybatisplus.extension.plugins.pagination.Page<ProjectControlView> pageResult = this.selectPage(mpPage, queryWrapper);
        return new PageResult<>(pageResult.getRecords(), pageResult.getTotal());
    }
}

