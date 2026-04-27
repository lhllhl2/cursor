package com.jasolar.mis.module.system.mapper.ehr;

import com.jasolar.mis.framework.mybatis.core.mapper.BaseMapperX;
import com.jasolar.mis.module.system.domain.ehr.EhrOrgManageExtR;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * EHR组织管理扩展关系 Mapper
 */
@Mapper
public interface EhrOrgManageExtRMapper extends BaseMapperX<EhrOrgManageExtR> {
    
    /**
     * 物理删除指定 EHR_CD 列表对应的记录
     *
     * @param ehrCds EHR_CD 列表
     * @return 删除的记录数
     */
    @Delete("<script>" +
            "DELETE FROM EHR_ORG_MANAGE_EXT_R WHERE EHR_CD IN " +
            "<foreach collection='ehrCds' item='ehrCd' open='(' separator=',' close=')'>" +
            "#{ehrCd}" +
            "</foreach>" +
            "</script>")
    int deleteByEhrCds(@Param("ehrCds") List<String> ehrCds);
}

