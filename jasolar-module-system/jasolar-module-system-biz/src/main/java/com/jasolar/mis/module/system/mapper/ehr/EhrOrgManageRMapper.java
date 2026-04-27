package com.jasolar.mis.module.system.mapper.ehr;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.jasolar.mis.framework.common.pojo.PageResult;
import com.jasolar.mis.framework.mybatis.core.mapper.BaseMapperX;
import com.jasolar.mis.module.system.controller.budget.vo.EhrDetailResultVo;
import com.jasolar.mis.module.system.controller.ehr.vo.EhrSearchVo;
import com.jasolar.mis.module.system.domain.ehr.EhrOrgManageR;
import com.jasolar.mis.module.system.resp.BudgetOrgResp;
import com.jasolar.mis.module.system.resp.EhrOrgManageRExceptResp;
import com.jasolar.mis.module.system.resp.EhrOrgManageRExtend;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * Description:
 * Author : Zhou Hai
 * Date : 09/12/2025 15:01
 * Version : 1.0
 */
@Mapper
public interface EhrOrgManageRMapper extends BaseMapperX<EhrOrgManageR> {
   /**
    * 分页查询EHR组织管理数据（性能优化版本）
    * 
    * 优化策略：
    * 1. 优先使用自定义SQL查询（selectPageOptimized），性能更好
    * 2. 使用ROW_NUMBER分页，避免MyBatis-Plus分页插件开销
    * 3. 充分利用索引 IDX_EHR_ORG_YEAR_DELETED_ID
    * 
    * @param searchVo 查询参数
    * @return 分页查询结果
    */
   default PageResult<EhrOrgManageR> searchPage(EhrSearchVo searchVo){
       // 使用优化的自定义SQL查询（性能更好，充分利用索引）
       // 计算分页参数
       int pageNo = searchVo.getPageNo() != null && searchVo.getPageNo() > 0 ? searchVo.getPageNo() : 1;
       int pageSize = searchVo.getPageSize() != null && searchVo.getPageSize() > 0 ? searchVo.getPageSize() : 10;
       int startRow = (pageNo - 1) * pageSize + 1;
       int endRow = pageNo * pageSize;
       
       long startTime = System.currentTimeMillis();
       
       // 查询总数（使用优化的COUNT查询）
       Long total = selectCountOptimized(searchVo);
       long countTime = System.currentTimeMillis() - startTime;
       
       // 查询分页数据（使用优化的分页查询）
       long queryStartTime = System.currentTimeMillis();
       List<EhrOrgManageR> records = selectPageOptimized(searchVo, startRow, endRow);
       long queryTime = System.currentTimeMillis() - queryStartTime;
       
       long totalTime = System.currentTimeMillis() - startTime;
       
       // 记录性能日志（仅在开发环境或需要调试时启用）
       // log.debug("EHR分页查询性能: COUNT={}ms, QUERY={}ms, TOTAL={}ms, year={}, total={}", 
       //          countTime, queryTime, totalTime, searchVo.getYear(), total);
       
       return new PageResult<>(records, total);
    }

    List<EhrDetailResultVo> queryAllDetail();

    List<EhrOrgManageRExtend> getParentAndChildByEhrCd(@Param("ehrCd") String ehrCd, @Param("year") String year);
    
    /**
     * 更新EhrOrgManageR记录，包括null字段
     * @param ehrOrgManageR 包含更新数据的实体对象
     * @return 更新影响的行数
     */
    int updateWithNullFields(@Param("ehrOrgManageR") EhrOrgManageR ehrOrgManageR);
    
    /**
     * 批量更新EhrOrgManageR记录的orgCd字段，包括设置为null
     * @param ids 要更新的记录ID列表
     * @param orgCd 新的orgCd值（可以为null）
     * @return 更新影响的行数
     */
    int batchUpdateOrgCdByIds(@Param("ids") List<Long> ids, @Param("orgCd") String orgCd, @Param("updater") String updater);
    
    /**
     * 批量更新EhrOrgManageR记录的controlLevel字段，包括设置为null
     * @param ids 要更新的记录ID列表
     * @param controlLevel 新的controlLevel值（可以为null）
     * @param updater 更新人
     * @return 更新影响的行数
     */
    int batchUpdateControlLevelByIds(@Param("ids") List<Long> ids, @Param("controlLevel") String controlLevel, @Param("updater") String updater);
    
    /**
     * 批量更新EhrOrgManageR记录的bzLevel字段，包括设置为null
     * @param ids 要更新的记录ID列表
     * @param bzLevel 新的bzLevel值（可以为null）
     * @param updater 更新人
     * @return 更新影响的行数
     */
    int batchUpdateBzLevelByIds(@Param("ids") List<Long> ids, @Param("bzLevel") String bzLevel, @Param("updater") String updater);

    /**
     * 按ID仅更新ERP部门字段
     * @param id 记录ID
     * @param erpDepart 新的ERP部门值（可为null）
     * @param updater 更新人
     * @return 更新影响的行数
     */
    int updateErpDepartById(@Param("id") Long id, @Param("erpDepart") String erpDepart, @Param("updater") String updater);

    default long getCountByEhrCd(String ehrCd, String year){


        return selectCount(new LambdaQueryWrapper<EhrOrgManageR>()
                .eq(EhrOrgManageR::getEhrCd, ehrCd)
                .eq(EhrOrgManageR::getYear, year));
    }

    List<BudgetOrgResp> getBudgetOrg();

    /**
     * 根据ehrCd获取父级组织，包含 ehrCd 本身
     * @param ehrCd
     * @param year
     * @return
     */
    List<EhrOrgManageRExtend> getParentByEhrCd(@Param("ehrCd") String ehrCd,@Param("year") String year);

    /**
     * 根据ehrCd获取子级组织，不包含 ehrCd 本身
     * @param ehrCd
     * @param year
     * @return
     */
    List<EhrOrgManageRExtend> getChildByEhrCd(String ehrCd, String year);

    default long selectCountByYear(String year){
        return selectCount(new LambdaQueryWrapper<EhrOrgManageR>().eq(EhrOrgManageR::getYear, year));
    }

    /**
     * 获取所有 Except 数据
     * @param year
     * @return
     */
    List<EhrOrgManageRExceptResp> getExceptData(String year);

    void deleteByEhrCds(@Param("ehrCds") List<String> keyList);

    /**
     * 优化的分页查询（使用自定义SQL，性能更好）
     * 注意：此方法需要配合 selectCountOptimized 使用
     * 
     * @param searchVo 查询参数
     * @param startRow 起始行号（从1开始）
     * @param endRow 结束行号
     * @return 查询结果列表
     */
    List<EhrOrgManageR> selectPageOptimized(@Param("searchVo") EhrSearchVo searchVo, 
                                             @Param("startRow") int startRow, 
                                             @Param("endRow") int endRow);

    /**
     * 优化的COUNT查询（使用自定义SQL，性能更好）
     * 
     * @param searchVo 查询参数
     * @return 总记录数
     */
    Long selectCountOptimized(@Param("searchVo") EhrSearchVo searchVo);
}