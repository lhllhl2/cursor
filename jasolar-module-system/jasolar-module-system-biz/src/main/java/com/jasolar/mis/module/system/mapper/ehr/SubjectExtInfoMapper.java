package com.jasolar.mis.module.system.mapper.ehr;

import com.jasolar.mis.framework.mybatis.core.mapper.BaseMapperX;
import com.jasolar.mis.module.system.domain.ehr.SubjectExtInfo;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 科目扩展信息 Mapper 接口
 */
@Mapper
public interface SubjectExtInfoMapper extends BaseMapperX<SubjectExtInfo> {

    /**
     * 根据ERP_ACCT_CD列表物理删除记录
     *
     * @param erpAcctCds ERP科目编码列表
     * @return 删除的记录数
     */
    @Delete("<script>" +
            "DELETE FROM subject_ext_info WHERE ERP_ACCT_CD IN " +
            "<foreach item='erpAcctCd' collection='erpAcctCds' open='(' separator=',' close=')'>" +
            "#{erpAcctCd}" +
            "</foreach>" +
            "</script>")
    int deleteByErpAcctCds(@Param("erpAcctCds") List<String> erpAcctCds);
}

