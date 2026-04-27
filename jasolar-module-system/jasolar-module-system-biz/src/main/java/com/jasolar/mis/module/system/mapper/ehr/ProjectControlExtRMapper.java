package com.jasolar.mis.module.system.mapper.ehr;

import com.jasolar.mis.framework.mybatis.core.mapper.BaseMapperX;
import com.jasolar.mis.module.system.domain.ehr.ProjectControlExtR;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 项目控制扩展关系 Mapper 接口
 */
@Mapper
public interface ProjectControlExtRMapper extends BaseMapperX<ProjectControlExtR> {

    /**
     * 根据PRJ_CD列表物理删除记录
     *
     * @param prjCds 项目编码列表
     * @return 删除的记录数
     */
    @Delete("<script>" +
            "DELETE FROM project_control_ext_r WHERE PRJ_CD IN " +
            "<foreach item='prjCd' collection='prjCds' open='(' separator=',' close=')'>" +
            "#{prjCd}" +
            "</foreach>" +
            "</script>")
    int deleteByPrjCds(@Param("prjCds") List<String> prjCds);
}

