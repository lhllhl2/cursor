package com.jasolar.mis.module.system.mapper.ehr;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.jasolar.mis.framework.common.pojo.PageResult;
import com.jasolar.mis.framework.mybatis.core.mapper.BaseMapperX;
import com.jasolar.mis.module.system.controller.ehr.vo.SubjectInfoSearchVo;
import com.jasolar.mis.module.system.domain.ehr.SubjectInfo;
import com.jasolar.mis.module.system.resp.SubjectExceptResp;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Objects;

/**
 * ProjectInfo Mapper 接口
 */
@Mapper
public interface SubjectInfoMapper extends BaseMapperX<SubjectInfo> {
    default PageResult<SubjectInfo> searchPage(SubjectInfoSearchVo searchVo) {
        LambdaQueryWrapper<SubjectInfo> queryWrapper = Wrappers.lambdaQuery();
        // 根据年份查询
        queryWrapper.eq(searchVo.getYear() != null && !searchVo.getYear().isEmpty(),
                SubjectInfo::getYear, searchVo.getYear());
        if(StringUtils.hasLength( searchVo.getCust1Cd())){
            queryWrapper.and( wrapper -> wrapper.like(SubjectInfo::getCust1Cd, searchVo.getCust1Cd()));
        }
        if( StringUtils.hasLength( searchVo.getCust1Nm())){
             queryWrapper.and( wrapper -> wrapper.like(SubjectInfo::getCust1Nm, searchVo.getCust1Nm()));
        }
        if(StringUtils.hasLength(searchVo.getErpAcctKey())){
             queryWrapper.and( wrapper -> wrapper.like(SubjectInfo::getErpAcctCd, searchVo.getErpAcctKey())
                     .or()
                     .like( SubjectInfo::getErpAcctNm, searchVo.getErpAcctKey())
             );
        }
        // 项目关键字查询 (匹配 acctCd 或 acctNm)
        if (searchVo.getAcctKey() != null && !searchVo.getAcctKey().isEmpty()) {
            queryWrapper.and(wrapper -> wrapper.like(SubjectInfo::getAcctCd, searchVo.getAcctKey())
                    .or()
                    .like(SubjectInfo::getAcctNm, searchVo.getAcctKey()));
        }

        // 按照ID降序排列
        queryWrapper.orderByDesc(SubjectInfo::getId);

        return selectPage(searchVo, queryWrapper);
    }
    
    List<SubjectExceptResp> getExceptData(@Param("year") String year);


    default Long selectCountByYear(String year){

        LambdaQueryWrapper<SubjectInfo> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.eq(SubjectInfo::getYear,year);
        return selectCount(queryWrapper);

    }


    int deleteByComFields(List<SubjectExceptResp> deleteListResp);

    /**
     * 根据 (cust1Cd, acctCd) 组合列表批量查询（要求 control_level = '1'）
     * @param subjectInfoList 科目信息列表，用于提取 (cust1Cd, acctCd) 组合
     * @return 查询结果
     */
    List<SubjectInfo> selectByCust1CdAndAcctCdList(@Param("list") List<SubjectInfo> subjectInfoList);

    /**
     * 根据 (cust1Cd, acctCd) 组合列表批量查询（不限制 control_level）
     * 用于查询叶子节点的 ERP_ACCT_CD，因为叶子节点的 controlLevel 可能不是 1
     * @param subjectInfoList 科目信息列表，用于提取 (cust1Cd, acctCd) 组合
     * @return 查询结果
     */
    List<SubjectInfo> selectByCust1CdAndAcctCdListWithoutControlLevel(@Param("list") List<SubjectInfo> subjectInfoList);

    void updateWithNullFields(@Param("subjectInfo") SubjectInfo subjectInfo);

   default List<SubjectInfo> selectByCombo(String custCd, String acctCd, String erpAcctCd,String year){
       LambdaQueryWrapper<SubjectInfo> queryWrapper = Wrappers.lambdaQuery();
       queryWrapper.eq(SubjectInfo::getCust1Cd,custCd);
       queryWrapper.eq(SubjectInfo::getAcctParCd,acctCd);
       queryWrapper.eq(SubjectInfo::getYear,year);
       queryWrapper.eq(Objects.isNull(erpAcctCd),SubjectInfo::getErpAcctCd,erpAcctCd);
       return selectList(queryWrapper);
   }

    void batchUpdateControlLevelById(@Param("ids") List<Long> ids,@Param("controlLevel") String controlLevel);

   default long getCountByCust1CdAndAcctCdAndErp(String cust1Cd, String acctCd, String erpAcctCd,String year){
       LambdaQueryWrapper<SubjectInfo> wrapper = Wrappers.lambdaQuery();
       wrapper.eq(SubjectInfo::getCust1Cd,cust1Cd);
       wrapper.eq(SubjectInfo::getAcctCd,acctCd);
       wrapper.eq(SubjectInfo::getYear,year);
       wrapper.eq(!Objects.isNull(erpAcctCd),SubjectInfo::getErpAcctCd,erpAcctCd);
       return  selectCount(wrapper);
   }

   default SubjectInfo selectComboOne(String cust1Cd, String acctCd, String erpAcctCd, String year){

       LambdaQueryWrapper<SubjectInfo> wrapper = Wrappers.lambdaQuery();
       wrapper.eq(SubjectInfo::getCust1Cd,cust1Cd);
       wrapper.eq(SubjectInfo::getAcctCd,acctCd);
       wrapper.eq(SubjectInfo::getYear,year);
       wrapper.eq(Objects.isNull(erpAcctCd),SubjectInfo::getErpAcctCd,erpAcctCd);
       return selectOne(wrapper);
   }

   /**
    * 查询 SUBJECT_INFO 中 ERP_ACCT_CD 有值的编码列表（去重）
    */
   List<String> selectDistinctErpAcctCdsNotBlank();

}