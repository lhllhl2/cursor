package com.jasolar.mis.module.system.mapper.ehr;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.jasolar.mis.framework.common.pojo.PageResult;
import com.jasolar.mis.framework.mybatis.core.mapper.BaseMapperX;
import com.jasolar.mis.module.system.controller.ehr.vo.ProjectControlRSearchVo;
import com.jasolar.mis.module.system.domain.ehr.ProjectControlR;
import com.jasolar.mis.module.system.resp.ProjectExceptResp;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * ProjectControlR Mapper
 */
@Mapper
public interface ProjectControlRMapper extends BaseMapperX<ProjectControlR> {
    
    default PageResult<ProjectControlR> searchPage(ProjectControlRSearchVo searchVo){
        LambdaQueryWrapper<ProjectControlR> queryWrapper = Wrappers.lambdaQuery();
        
        // 根据年份查询
        queryWrapper.eq(searchVo.getYear() != null && !searchVo.getYear().isEmpty(),
                ProjectControlR::getYear, searchVo.getYear());

        // 项目关键字查询 (匹配 prjCd 或 prjNm)
        if (StringUtils.hasLength(searchVo.getPrjCd())) {
            queryWrapper.and(wrapper -> wrapper.like(ProjectControlR::getPrjCd, searchVo.getPrjCd()));
        }
        if(StringUtils.hasLength(searchVo.getPrjNm())){
            queryWrapper.and(wrapper -> wrapper.like(ProjectControlR::getPrjNm, searchVo.getPrjNm()));
        }
        // 按照ID降序排列
        queryWrapper.orderByDesc(ProjectControlR::getId);

        return selectPage(searchVo, queryWrapper);
    }


    List<ProjectExceptResp> getExceptData(@Param("year") String year);

    default Long selectCountByYear(String year){
        return selectCount(Wrappers.lambdaQuery(ProjectControlR.class)
                .eq(ProjectControlR::getYear, year));
    }

    /**
     * 根据项目编码更新项目信息
     */
    void updateWithFields(ProjectControlR projectControlR);

    default List<ProjectControlR> selectByCombo(String parPrjCd, String year){
        return selectList(Wrappers.lambdaQuery(ProjectControlR.class)
                .eq(ProjectControlR::getParCd, parPrjCd)
                .eq(ProjectControlR::getYear, year));

    }

    void batchUpdateControlLevelById(@Param("ids") List<Long> ids, @Param("controlLevel") String controlLevel);

    void updateBatchByPrjCdAndYear(@Param("prjList") List<ProjectControlR> uList,@Param("year") String year);

    default void deleteByIdsAndYear(List<Long> keyList, String year){
        delete(Wrappers.lambdaQuery(ProjectControlR.class)
                .eq(ProjectControlR::getYear, year)
                .in(ProjectControlR::getPrjId, keyList));
    }
}