package com.jasolar.mis.module.system.service.ehr;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jasolar.mis.framework.common.exception.ServiceException;
import com.jasolar.mis.framework.common.pojo.PageParam;
import com.jasolar.mis.framework.common.pojo.PageResult;
import com.fasterxml.jackson.core.type.TypeReference;
import com.jasolar.mis.framework.common.util.json.JsonUtils;
import com.jasolar.mis.framework.excel.core.util.ExcelUtils;
import com.jasolar.mis.framework.web.core.util.WebFrameworkUtils;
import com.jasolar.mis.module.system.controller.budget.vo.EhrDetailResultVo;
import com.jasolar.mis.module.system.controller.ehr.vo.EhrOrgManageRExcelVO;
import com.jasolar.mis.module.system.controller.ehr.vo.EhrSearchVo;
import com.jasolar.mis.module.system.domain.ehr.EhrOrgManageR;
import com.jasolar.mis.module.system.enums.EhrEnums;
import com.jasolar.mis.module.system.mapper.admin.org.SystemOrgMapper;
import com.jasolar.mis.module.system.mapper.ehr.EhrOrgManageRMapper;
import com.jasolar.mis.module.system.resp.*;
import com.jasolar.mis.module.system.service.budget.morg.BudgetEhrOrgMService;
import com.jasolar.mis.module.system.service.budget.snapshot.EhrControlLevelSnapshotService;
import com.jasolar.mis.module.system.util.PageExportUtil;
import com.jasolar.mis.module.system.util.TreeConvertUtil;
import com.jasolar.mis.framework.redis.util.RedisUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletResponse;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;


/**
 * Description:
 * Author : Zhou Hai
 * Date : 09/12/2025 14:57
 * Version : 1.0
 */
@Slf4j
@Service
public class EhrOrgManageRServiceImpl implements EhrOrgManageRService{
    
    /**
     * EHR组织管理分页查询的缓存Key
     * KEY 格式：ehr_org_manage_r:searchPage:{year}:{ehrOrgKey}:{manageOrgKey}:{pageNo}:{pageSize}
     */
    private static final String CACHE_KEY_EHR_ORG_MANAGE_R_SEARCH_PAGE = "ehr_org_manage_r:searchPage";
    
    @Autowired
    private EhrOrgManageRMapper ehrOrgManageRMapper;

    @Autowired
    private SystemOrgMapper systemOrgMapper;

    @Autowired
    private BudgetEhrOrgMService budgetEhrOrgMService;

    @Autowired
    private ThreadPoolTaskExecutor taskExecutor;

    @Autowired
    private EhrControlLevelSnapshotService ehrControlLevelSnapshotService;




    /**
     * 分页查询EHR组织管理数据
     * 性能优化：
     * 1. 如果year为空，设置默认值为当前年份，确保使用索引
     * 2. 使用Redis缓存，相同查询条件的结果缓存1小时，大幅提升重复查询性能
     * 
     * @param searchVo 查询参数
     * @return 分页结果
     */
    @Override
    public PageResult<EhrOrgManageR> searchPage(EhrSearchVo searchVo) {
        // 性能优化：如果year为空，设置默认值为当前年份
        // 这样可以充分利用复合索引 IDX_EHR_ORG_YEAR_DELETED_ID，避免全表扫描
        if (searchVo.getYear() == null || searchVo.getYear().isEmpty()) {
            Calendar calendar = Calendar.getInstance();
            String currentYear = String.valueOf(calendar.get(Calendar.YEAR));
            searchVo.setYear(currentYear);
            log.debug("year参数为空，使用当前年份：{}", currentYear);
        }
        
        // 构建缓存Key
        String cacheKey = buildCacheKey(searchVo);
        log.info("EHR查询 - 缓存Key: {}, pageSize: {}", cacheKey, searchVo.getPageSize());
        
        // 尝试从缓存获取
        try {
            String cachedJson = RedisUtils.getStr(cacheKey);
            if (cachedJson != null && !cachedJson.isEmpty()) {
                PageResult<EhrOrgManageR> cachedResult = JsonUtils.parseObject(cachedJson, 
                    new TypeReference<PageResult<EhrOrgManageR>>() {});
                if (cachedResult != null && cachedResult.getTotal() > 0) {
                    log.info("✅ 从缓存获取数据，key: {}, 数据量: {} 条", cacheKey, cachedResult.getList().size());
                    return cachedResult;
                }
            } else {
                log.info("❌ 缓存未命中，key: {}", cacheKey);
            }
        } catch (Exception e) {
            log.warn("从缓存读取数据失败，将查询数据库: {}", e.getMessage(), e);
        }
        
        // 缓存未命中，查询数据库
        long startTime = System.currentTimeMillis();
        PageResult<EhrOrgManageR> result = ehrOrgManageRMapper.searchPage(searchVo);
        long queryTime = System.currentTimeMillis() - startTime;
        log.info("📊 数据库查询完成，耗时: {} ms, 数据量: {} 条", queryTime, result != null ? result.getList().size() : 0);
        
        // 将结果存入缓存（1小时过期）
        if (result != null && result.getTotal() > 0) {
            try {
                String jsonValue = JsonUtils.toJsonString(result);
                RedisUtils.set(cacheKey, jsonValue, 1, TimeUnit.HOURS);
                log.info("💾 数据已缓存，key: {}, 数据大小: {} KB", cacheKey, jsonValue.length() / 1024);
            } catch (Exception e) {
                log.error("❌ 缓存数据失败: {}", e.getMessage(), e);
            }
        }
        
        return result;
    }
    
    /**
     * 构建缓存Key
     * 
     * @param searchVo 查询参数
     * @return 缓存Key
     */
    private String buildCacheKey(EhrSearchVo searchVo) {
        String year = searchVo.getYear() != null ? searchVo.getYear() : "null";
        String ehrOrgKey = searchVo.getEhrOrgKey() != null ? searchVo.getEhrOrgKey() : "null";
        String manageOrgKey = searchVo.getManageOrgKey() != null ? searchVo.getManageOrgKey() : "null";
        Integer pageNo = searchVo.getPageNo() != null ? searchVo.getPageNo() : 1;
        Integer pageSize = searchVo.getPageSize() != null ? searchVo.getPageSize() : 10;
        
        return String.format("%s:%s:%s:%s:%s:%s", 
            CACHE_KEY_EHR_ORG_MANAGE_R_SEARCH_PAGE, year, ehrOrgKey, manageOrgKey, pageNo, pageSize);
    }
    
    /**
     * 清除EHR组织管理查询缓存
     * 数据更新后调用此方法清除所有相关缓存
     */
    private void clearSearchPageCache() {
        try {
            // 使用通配符删除所有匹配的缓存key
            List<String> keys = RedisUtils.scan(CACHE_KEY_EHR_ORG_MANAGE_R_SEARCH_PAGE + "*");
            if (keys != null && !keys.isEmpty()) {
                RedisUtils.del(keys.toArray(new String[0]));
                log.debug("已清除EHR组织管理查询缓存，共{}个key", keys.size());
            }
        } catch (Exception e) {
            log.warn("清除缓存失败: {}", e.getMessage());
        }
    }

    
    @Override
    public void exportExcel(EhrSearchVo searchVo, HttpServletResponse response) throws Exception {
        //
        if(Objects.isNull(searchVo.getYear())){
            Calendar calendar = Calendar.getInstance();
            String year = String.valueOf(calendar.get(Calendar.YEAR)) ;
            searchVo.setYear(year);
        }
        if (searchVo.getPageNo() == null || searchVo.getPageNo() <= 0) {
            searchVo.setPageNo(1);
        }
        if (searchVo.getPageSize() == null || searchVo.getPageSize() <= 0) {
            searchVo.setPageSize(1000);
        }
        PageExportUtil.exportExcel(
            searchVo,
            this::searchPage,
            response,
            "EHR组织管理数据.xlsx",
            EhrOrgManageRExcelVO.class,
            this::convertToExcelVO
        );
    }
    
    /**
     * 将EhrOrgManageR转换为EhrOrgManageRExcelVO
     */
    private EhrOrgManageRExcelVO convertToExcelVO(EhrOrgManageR entity) {
        EhrOrgManageRExcelVO excelVO = new EhrOrgManageRExcelVO();
        excelVO.setChange(EhrEnums.ChangeType.UN_CHANGE.getDesc());
        BeanUtils.copyProperties(entity, excelVO);
        excelVO.setId(String.valueOf(entity.getId()));
        return excelVO;
    }
    
    /**
     * 从Excel导入EHR组织管理数据并批量更新
     * 清除缓存：数据更新后清除查询缓存，保证数据一致性（缓存启用后生效）
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    // @CacheEvict(value = CACHE_KEY_EHR_ORG_MANAGE_R_SEARCH_PAGE, allEntries = true)
    public String importExcelAndUpdate(MultipartFile file) throws Exception {
        if (file == null || file.isEmpty()) {
            return "文件为空，请选择要导入的Excel文件";
        }
        
        // 使用EasyExcel读取Excel文件
        List<EhrOrgManageRExcelVO> excelDataList = ExcelUtils.readSync(file.getInputStream(), EhrOrgManageRExcelVO.class);
        log.info("导入的Excel数据为 ---> {}",JsonUtils.toJsonString(excelDataList));
        if (excelDataList.isEmpty()) {
            return "Excel文件中没有有效数据";
        }

        List<EhrOrgManageR> updateList = filterChange(excelDataList,EhrEnums.ChangeType.CHANGE.getDesc());

        // 收集所有涉及 CONTROL_LEVEL 更新的记录的 ehrCd（用于后续异步调用 syncEhrManageRData）
        Set<String> controlLevelUpdatedEhrCds = new HashSet<>();

        // budgetOrg is null
        List<Long> budgetOrgNullList = updateList.stream()
                .filter(x -> Objects.isNull(x.getOrgCd()))
                .map(EhrOrgManageR::getId)
                .toList();
        while (!budgetOrgNullList.isEmpty()){
            if(budgetOrgNullList.size() > 50){
                List<Long> ids = budgetOrgNullList.subList(0, 50);
                ehrOrgManageRMapper.batchUpdateOrgCdByIds(ids, null, WebFrameworkUtils.getLoginUserNo());
                budgetOrgNullList = budgetOrgNullList.subList(50, budgetOrgNullList.size());
                log.info("budgetOrg is null , 更新50");
                continue;
            }
            log.info("budgetOrg is null , 更新{}",budgetOrgNullList.size());
            ehrOrgManageRMapper.batchUpdateOrgCdByIds(budgetOrgNullList, null, WebFrameworkUtils.getLoginUserNo());
           if(budgetOrgNullList.size() <= 50) {
               break;
           }
        }

        // control level is null
        List<Long> controlNullList = updateList.stream()
                .filter(x -> Objects.isNull(x.getControlLevel()))
                .map(EhrOrgManageR::getId)
                .toList();
        // 收集这些记录的 ehrCd（通过 updateList 中对应的记录）
        // 创建 final 副本用于 lambda 表达式
        final List<Long> controlNullListFinal = new ArrayList<>(controlNullList);
        if (!controlNullListFinal.isEmpty()) {
            controlLevelUpdatedEhrCds.addAll(updateList.stream()
                    .filter(e -> controlNullListFinal.contains(e.getId()))
                    .map(EhrOrgManageR::getEhrCd)
                    .filter(StringUtils::hasText)
                    .collect(Collectors.toList()));
        }
        while (!controlNullList.isEmpty()){
            if(controlNullList.size() > 50){
                List<Long> ids = controlNullList.subList(0, 50);
                ehrOrgManageRMapper.batchUpdateControlLevelByIds(ids, null, WebFrameworkUtils.getLoginUserNo());
                controlNullList = controlNullList.subList(50, controlNullList.size());
                log.info("control is null , 更新50");
                continue;
            }
            log.info("control is null , 更新{}",controlNullList.size());
            ehrOrgManageRMapper.batchUpdateControlLevelByIds(controlNullList, null, WebFrameworkUtils.getLoginUserNo());
            if(controlNullList.size() <= 50){
                break;
            }
        }

        // bz level is null
        List<Long> bzNullList = updateList.stream()
                .filter(x -> Objects.isNull(x.getBzLevel()))
                .map(EhrOrgManageR::getId)
                .toList();
        while (!bzNullList.isEmpty()){
            if(bzNullList.size() > 50){
                List<Long> ids = bzNullList.subList(0, 50);
                ehrOrgManageRMapper.batchUpdateBzLevelByIds(ids, null, WebFrameworkUtils.getLoginUserNo());
                bzNullList = bzNullList.subList(50, bzNullList.size());
                log.info("control is null , 更新50");
                continue;
            }
            log.info("control is null , 更新{}",bzNullList.size());
            ehrOrgManageRMapper.batchUpdateBzLevelByIds(bzNullList, null, WebFrameworkUtils.getLoginUserNo());
            if(bzNullList.size() <= 50){
                break;
            }
        }


        // 转换为实体对象并批量更新
        List<EhrOrgManageR> addList = filterChange(excelDataList,EhrEnums.ChangeType.ADD.getDesc());
        for (EhrOrgManageR e : addList) {
            long count = ehrOrgManageRMapper.getCountByEhrCd(e.getEhrCd(), e.getYear());
            if (count > 0) {
                throw new ServiceException("Ehr-100001",
                        "EHR组织【" + e.getEhrCd() + "】已存在，请勿重复添加！"
                );
            }
            List<EhrOrgManageRExtend> parentList = ehrOrgManageRMapper.getParentByEhrCd(e.getEhrCd(), e.getYear());
            if(!CollectionUtils.isEmpty(parentList)){
                checkBudgetOrg(e, parentList);
                checkControlLevel(e, parentList);
                checkBzLevel(e, parentList);
            }

            List<EhrOrgManageRExtend> childList = ehrOrgManageRMapper.getChildByEhrCd(e.getEhrCd(), e.getYear());
            if(CollectionUtils.isEmpty(childList)){
                ehrOrgManageRMapper.insert(e);
                continue;
            }
            checkBudgetOrg(e, childList);
            checkControlLevel(e, childList);
            checkBzLevel(e, childList);
            ehrOrgManageRMapper.insert(e);
        }

        for (EhrOrgManageR e : updateList) {
            log.info("导入的修改数据为 ---> {}",e);
            // 检查 Excel 数据中是否包含 controlLevel 字段（如果包含，说明这个字段被更新了）
            boolean controlLevelInExcel = excelDataList.stream()
                    .anyMatch(excel -> excel.getId() != null && excel.getId().equals(String.valueOf(e.getId()))
                            && excel.getChange().equals(EhrEnums.ChangeType.CHANGE.getDesc())
                            && excel.getControlLevel() != null);
            
            List<EhrOrgManageRExtend> pc = ehrOrgManageRMapper.getParentAndChildByEhrCd(e.getEhrCd(), e.getYear());
            log.info("导入的修改数据对应的父级和子级组织为 ---> {}", JsonUtils.toJsonString(pc));
            if(CollectionUtils.isEmpty(pc)){
                ehrOrgManageRMapper.updateWithNullFields(e);
                // 如果 Excel 数据中包含 controlLevel 字段，收集 ehrCd
                if (controlLevelInExcel && StringUtils.hasText(e.getEhrCd())) {
                    controlLevelUpdatedEhrCds.add(e.getEhrCd());
                }
                continue;
            }
            checkBudgetOrg(e, pc);
            checkControlLevel(e, pc);
            checkBzLevel(e,pc);
            ehrOrgManageRMapper.updateWithNullFields(e);
            // 如果 Excel 数据中包含 controlLevel 字段，收集 ehrCd
            if (controlLevelInExcel && StringUtils.hasText(e.getEhrCd())) {
                controlLevelUpdatedEhrCds.add(e.getEhrCd());
            }
        }

        // 移除自动同步调用，改为通过 BudgetEhrOrgMController 手动调用
        // taskExecutor.execute(() -> { ... });

        // 清除查询缓存
        clearSearchPageCache();
        scheduleRefreshEhrControlLevelSnapshotCache();
        
        return String.format("导入成功！共读取 %d 条数据，成功更新 %d 条", excelDataList.size(), addList.size() + updateList.size());
    }

    private static void checkBzLevel(EhrOrgManageR e, List<EhrOrgManageRExtend> pc) {
        if (!Objects.isNull(e.getBzLevel())){
            for (EhrOrgManageRExtend eh : pc) {
                if(Objects.equals(eh.getBzLevel(), e.getBzLevel())){
                    throw new ServiceException("Ehr-100004",
                            "正在执行的组织为：" + e.getEhrCd() + "，" + EhrEnums.Level.getDesc(eh.getNodeLevel()) + "组织["+ eh.getEhrCd() + "--" + eh.getEhrNm() + "]已勾选编制层级，请勿重复勾选！"
                    );
                }
            }
        }
    }

    private static void checkControlLevel(EhrOrgManageR e, List<EhrOrgManageRExtend> pc) {
        if(!Objects.isNull(e.getControlLevel())){
            for (EhrOrgManageRExtend eh : pc) {
                if(Objects.equals(eh.getControlLevel(), e.getControlLevel())){
                    throw new ServiceException("Ehr-100003",
                            "正在执行的组织为：" + e.getEhrCd() + "，" + EhrEnums.Level.getDesc(eh.getNodeLevel()) + "组织["+ eh.getEhrCd() + "--" + eh.getEhrNm() + "]已勾选控制层级，请勿重复勾选！"
                    );
                }
            }
        }
    }

    private static void checkBudgetOrg(EhrOrgManageR e, List<EhrOrgManageRExtend> pc) {
        if(!Objects.isNull(e.getOrgCd())){
            for (EhrOrgManageRExtend eh : pc) {
                if( Objects.equals(eh.getOrgCd(), e.getOrgCd())){
                    throw new ServiceException("Ehr-100002",
                          "正在执行的组织为：" + e.getEhrCd() + "，" +  EhrEnums.Level.getDesc(eh.getNodeLevel()) + "组织["+ eh.getEhrCd() + "--" + eh.getEhrNm() + "]已拥有该管理组织【"+eh.getOrgCd()+"】，请勿重复添加！"
                    );
                }
            }
        }
    }

    /**
     * 将Excel数据转换为实体对象列表
     */
    private List<EhrOrgManageR> filterChange(List<EhrOrgManageRExcelVO> excelDataList,String change) {
        return excelDataList.stream()
                .filter(x -> Objects.equals(x.getChange(),change))
                .map(excelData -> {
                        EhrOrgManageR entity = new EhrOrgManageR();
                        if(StringUtils.hasLength(excelData.getId())){
                            entity.setId(Long.valueOf(excelData.getId()));
                        }
                        entity.setOrgCd(excelData.getOrgCd());
                        entity.setOrgNm(excelData.getOrgNm());
                        entity.setEhrCd(excelData.getEhrCd());
                        entity.setEhrNm(excelData.getEhrNm());
                        entity.setEhrParCd(excelData.getEhrParCd());
                        entity.setEhrParNm(excelData.getEhrParNm());
                        entity.setYear(excelData.getYear());
                        entity.setControlLevel(excelData.getControlLevel());
                        entity.setBzLevel(excelData.getBzLevel());
                        entity.setErpDepart(excelData.getErpDepart());
                        return entity;
        }).collect(Collectors.toList());
    }


    @Override
    public List<EhrDetailResultVo> queryAllDetail() {
        return ehrOrgManageRMapper.queryAllDetail();
    }


    /**
     * 修改单个组织信息
     * 清除缓存：数据更新后清除查询缓存，保证数据一致性（缓存启用后生效）
     */
    @Override
    // @CacheEvict(value = CACHE_KEY_EHR_ORG_MANAGE_R_SEARCH_PAGE, allEntries = true)
    public void changeSingle(EhrOrgManageR ehrOrgManageR) {
        if(!Objects.isNull(ehrOrgManageR.getOrgCd())){
            List<EhrOrgManageRExtend> parentAndChild = ehrOrgManageRMapper.getParentAndChildByEhrCd(ehrOrgManageR.getEhrCd(),ehrOrgManageR.getYear());
            if(!CollectionUtils.isEmpty(parentAndChild)){
                for (EhrOrgManageRExtend r : parentAndChild) {
                    if(!Objects.equals(r.getEhrCd(),ehrOrgManageR.getEhrCd()) && Objects.equals(r.getOrgCd(),ehrOrgManageR.getOrgCd())){
                        throw new ServiceException("Ehr-100002",
                                EhrEnums.Level.getDesc(r.getNodeLevel()) + "组织["+ r.getEhrCd() + "--" + r.getEhrNm() + "]已拥有该管理组织【"+r.getOrgNm()+"】，请勿重复添加！"
                        );
                    }
                }
            }
        }
        ehrOrgManageRMapper.updateWithNullFields(ehrOrgManageR);
        
        // 清除查询缓存
        clearSearchPageCache();
        scheduleRefreshEhrControlLevelSnapshotCache();

        // 移除自动同步调用，改为通过 BudgetEhrOrgMController 手动调用
        // taskExecutor.execute(() -> { budgetEhrOrgMService.synEhrManageOneRData(); });

    }

    /**
     * 修改控制层级
     * 清除缓存：数据更新后清除查询缓存，保证数据一致性（缓存启用后生效）
     */
    @Override
    // @CacheEvict(value = CACHE_KEY_EHR_ORG_MANAGE_R_SEARCH_PAGE, allEntries = true)
    public void changeControlLevel(EhrOrgManageR ehrOrgManageR) {
        if(!Objects.isNull(ehrOrgManageR.getControlLevel())){
            List<EhrOrgManageRExtend> parentAndChild = ehrOrgManageRMapper.getParentAndChildByEhrCd(ehrOrgManageR.getEhrCd(),ehrOrgManageR.getYear());
            if(!CollectionUtils.isEmpty(parentAndChild)){
                for (EhrOrgManageRExtend r : parentAndChild) {
                    if(!Objects.equals(r.getEhrCd(),ehrOrgManageR.getEhrCd()) && Objects.equals(r.getControlLevel(),ehrOrgManageR.getControlLevel())){
                        throw new ServiceException("Ehr-100003",
                                EhrEnums.Level.getDesc(r.getNodeLevel()) + "组织["+ r.getEhrCd() + "--" + r.getEhrNm() + "]已勾选控制层级，请勿重复勾选！"
                        );
                    }
                }
            }
        }
        ehrOrgManageRMapper.updateWithNullFields(ehrOrgManageR);
        
        // 清除查询缓存
        clearSearchPageCache();
        scheduleRefreshEhrControlLevelSnapshotCache();

        // 移除自动同步调用，改为通过 BudgetEhrOrgMController 手动调用
        // taskExecutor.execute(() -> { ... });

    }


    /**
     * 修改编制层级
     * 清除缓存：数据更新后清除查询缓存，保证数据一致性（缓存启用后生效）
     */
    @Override
    // @CacheEvict(value = CACHE_KEY_EHR_ORG_MANAGE_R_SEARCH_PAGE, allEntries = true)
    public void changeBzLevel(EhrOrgManageR ehrOrgManageR) {
        if(!Objects.isNull(ehrOrgManageR.getBzLevel())){
            List<EhrOrgManageRExtend> parentAndChild = ehrOrgManageRMapper.getParentAndChildByEhrCd(ehrOrgManageR.getEhrCd(),ehrOrgManageR.getYear());
            if(!CollectionUtils.isEmpty(parentAndChild)){
                for (EhrOrgManageRExtend r : parentAndChild) {
                    if(!Objects.equals(r.getEhrCd(),ehrOrgManageR.getEhrCd()) && Objects.equals(r.getBzLevel(),ehrOrgManageR.getBzLevel())){
                        throw new ServiceException("Ehr-100004",
                                EhrEnums.Level.getDesc(r.getNodeLevel()) + "组织["+ r.getEhrCd() + "--" + r.getEhrNm() + "]已勾选编制层级，请勿重复勾选！"
                        );
                    }
                }
            }
        }
        ehrOrgManageRMapper.updateWithNullFields(ehrOrgManageR);
        
        // 清除查询缓存
        clearSearchPageCache();
        scheduleRefreshEhrControlLevelSnapshotCache();

        // 移除自动同步调用，改为通过 BudgetEhrOrgMController 手动调用
        // taskExecutor.execute(() -> { budgetEhrOrgMService.synEhrManageOneRData(); });
    }

    /**
     * 修改ERP部门
     * 清除缓存：数据更新后清除查询缓存，保证数据一致性（缓存启用后生效）
     */
    @Override
    // @CacheEvict(value = CACHE_KEY_EHR_ORG_MANAGE_R_SEARCH_PAGE, allEntries = true)
    public void changeErpDepart(EhrOrgManageR ehrOrgManageR) {
        if (ehrOrgManageR == null || ehrOrgManageR.getId() == null) {
            throw new ServiceException("Ehr-100005", "组织记录ID不能为空");
        }
        String updater = StringUtils.hasText(ehrOrgManageR.getUpdater())
                ? ehrOrgManageR.getUpdater()
                : WebFrameworkUtils.getLoginUserNo();
        ehrOrgManageRMapper.updateErpDepartById(
                ehrOrgManageR.getId(),
                ehrOrgManageR.getErpDepart(),
                updater);
        clearSearchPageCache();
        scheduleRefreshEhrControlLevelSnapshotCache();
    }

    @Override
    public List<BudgetOrgResp> getBudgetOrg() {
        List<BudgetOrgResp> budgetOrgRespList = ehrOrgManageRMapper.getBudgetOrg();
        return convertToTree(budgetOrgRespList);
    }


    private List<BudgetOrgResp> convertToTree(List<BudgetOrgResp> flatList) {
        if (flatList == null || flatList.isEmpty()) {
            return new ArrayList<>();
        }
        
        Map<String, BudgetOrgResp> nodeMap = flatList.stream()
                .collect(Collectors.toMap(BudgetOrgResp::getOrgCd, node -> node, (existing, replacement) -> existing));
        
        List<BudgetOrgResp> rootNodes = new ArrayList<>();
        
        for (BudgetOrgResp node : flatList) {
            String parentCode = node.getParCd();
            
            if (parentCode == null || parentCode.isEmpty()) {
                if (node.getChildren() == null) {
                    node.setChildren(new ArrayList<>());
                }
                rootNodes.add(node);
            } else {
                BudgetOrgResp parentNode = nodeMap.get(parentCode);
                if (parentNode != null) {
                    if (parentNode.getChildren() == null) {
                        parentNode.setChildren(new ArrayList<>());
                    }
                    parentNode.getChildren().add(node);
                }
                else {
                    if (node.getChildren() == null) {
                        node.setChildren(new ArrayList<>());
                    }
                    rootNodes.add(node);
                }
            }
        }
        return rootNodes;
    }


    @Override
    public void syncProjectToBusiness() {
        Calendar calendar = Calendar.getInstance();
        String year = String.valueOf(calendar.get(Calendar.YEAR)) ;
        long count =  ehrOrgManageRMapper.selectCountByYear(year);
        if(count == 0){
            log.info("同步组织初始化...");
            init(year);
            // 移除自动同步调用，改为通过 BudgetEhrOrgMController 手动调用
            // taskExecutor.execute(() -> { budgetEhrOrgMService.synEhrManageOneRData(); });
            // 清除查询缓存（init方法会插入数据）
            clearSearchPageCache();
            return;
        }
        List<EhrOrgManageRExceptResp> exceptData = ehrOrgManageRMapper.getExceptData(year);
        if(CollectionUtils.isEmpty(exceptData)){
            log.info("组织数据无差异，结束....");
            return;
        }
        List<EhrOrgManageRExceptResp> list = exceptData.stream().filter(x -> x.getToAdd().equals("1")).toList();
        List<EhrOrgManageRExceptResp> addOrUpdateList = new ArrayList<>(list);

        Map<String, EhrOrgManageRExceptResp> updateOrDelMap = exceptData.stream().filter(x -> x.getToAdd().equals("2"))
                .collect(Collectors.toMap(EhrOrgManageRExceptResp::getEhrCd, x -> x));

        List<EhrOrgManageRExceptResp> updateList = new LinkedList<>();
        Iterator<EhrOrgManageRExceptResp> iterator = addOrUpdateList.iterator();
        while (iterator.hasNext()){
            EhrOrgManageRExceptResp exceptResp = iterator.next();
            if(updateOrDelMap.containsKey(exceptResp.getEhrCd())){
                updateList.add(exceptResp);
                iterator.remove();
                updateOrDelMap.remove(exceptResp.getEhrCd());
            }
        }

        if(!CollectionUtils.isEmpty(addOrUpdateList)){
            List<EhrOrgManageR> addList = addOrUpdateList.stream().map(x -> {
                return EhrOrgManageR.builder()
                        .ehrCd(x.getEhrCd())
                        .ehrNm(x.getEhrNm())
                        .ehrParCd(x.getEhrParCd())
                        .ehrParNm(x.getEhrParNm())
                        .year(year)
                        .build();
            }).toList();
            ehrOrgManageRMapper.insertBatch(addList);
        }

        if(!CollectionUtils.isEmpty(updateList)){
            List<EhrOrgManageR> uList = updateList.stream().map(x -> {
                return EhrOrgManageR.builder()
                        .ehrCd(x.getEhrCd())
                        .ehrNm(x.getEhrNm())
                        .ehrParCd(x.getEhrParCd())
                        .ehrParNm(x.getEhrParNm())
                        .year(year)
                        .build();
            }).toList();
            ehrOrgManageRMapper.updateBatch(uList);
        }

//        if(!updateOrDelMap.isEmpty()){
//            List<String> keyList = updateOrDelMap.keySet().stream().toList();
//            ehrOrgManageRMapper.deleteByEhrCds(keyList);
//        }

        // 移除自动同步调用，改为通过 BudgetEhrOrgMController 手动调用
        // taskExecutor.execute(() -> { budgetEhrOrgMService.synEhrManageOneRData(); });
        
        // 清除查询缓存（syncProjectToBusiness方法会批量插入和更新数据）
        clearSearchPageCache();
        scheduleRefreshEhrControlLevelSnapshotCache();

    }


    private List<BudgetOrgResp> convertToTreeUsingUtil(List<BudgetOrgResp> flatList) {
        return TreeConvertUtil.convertToTree(
            flatList,
            BudgetOrgResp::getOrgCd,           // 获取节点编码的函数
            BudgetOrgResp::getParCd,           // 获取父节点编码的函数
            BudgetOrgResp::getChildren,        // 获取子节点列表的函数
            BudgetOrgResp::setChildren        // 设置子节点列表的函数
        );
    }


    private void init(String year) {
        // 每次查询200条记录，直到查询完所有数据
        int pageSize = 200;
        int currentPage = 1;
        // 循环查询直到没有更多数据
        while (true) {
            // 构建分页参数
            PageParam pageParam = new PageParam();
            pageParam.setPageNo(currentPage);
            pageParam.setPageSize(pageSize);

            // 使用projectViewMapper分页查询数据
            Page<SystemOrgResp> page = new Page<>(currentPage, pageSize);
            page = systemOrgMapper.pageForMapper(page);


            // 处理查询到的数据
            List<SystemOrgResp> systemOrgDOS = page.getRecords();

            // 如果没有更多数据，则退出循环
            if (systemOrgDOS.isEmpty()) {
                break;
            }
            List<EhrOrgManageR> list = systemOrgDOS.stream().map(sys -> {
                EhrOrgManageR ehrOrgManageR = EhrOrgManageR.builder()
                        .id(IdWorker.getId())
                        .ehrCd(sys.getId())
                        .ehrNm(sys.getOrgName())
                        .ehrParCd(sys.getParentId())
                        .ehrParNm(sys.getParentName())
                        .year(year)
                        .build();
                return ehrOrgManageR;
            }).toList();
            ehrOrgManageRMapper.insertBatch(list);

            if( systemOrgDOS.size() < pageSize){
                break;
            }
            // 移动到下一页
            currentPage++;
        }
    }

    /**
     * 事务提交后刷新 EHR 控制层级快照缓存。
     * 若当前不在事务中，则立即刷新。
     */
    private void scheduleRefreshEhrControlLevelSnapshotCache() {
        Runnable refreshTask = () -> {
            try {
                ehrControlLevelSnapshotService.refreshAllCache();
                log.info("EHR控制层级快照缓存刷新成功");
            } catch (Exception e) {
                log.error("EHR控制层级快照缓存刷新失败", e);
            }
        };
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    refreshTask.run();
                }
            });
        } else {
            refreshTask.run();
        }
    }




}