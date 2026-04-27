package com.jasolar.mis.module.system.service.ehr;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.jasolar.mis.framework.common.exception.ServiceException;
import com.jasolar.mis.framework.common.pojo.PageParam;
import com.jasolar.mis.framework.common.pojo.PageResult;
import com.jasolar.mis.framework.excel.core.util.ExcelUtils;
import com.jasolar.mis.module.system.controller.ehr.vo.ProjectControlRExcelVO;
import com.jasolar.mis.module.system.controller.ehr.vo.ProjectControlRSearchVo;
import com.jasolar.mis.module.system.domain.ehr.ProjectControlR;
import com.jasolar.mis.module.system.domain.ehr.ProjectView;
import com.jasolar.mis.module.system.enums.EhrEnums;
import com.jasolar.mis.module.system.enums.ProjectEnum;
import com.jasolar.mis.module.system.mapper.ehr.ProjectControlRMapper;
import com.jasolar.mis.module.system.mapper.ehr.ProjectViewMapper;
import com.jasolar.mis.module.system.resp.ProjectComBo;
import com.jasolar.mis.module.system.resp.ProjectExceptResp;
import com.jasolar.mis.module.system.util.PageExportUtil;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;
import java.util.stream.Collectors;

/**
 * ProjectControlR Service 实现类
 */
@Slf4j
@Service
public class ProjectControlRServiceImpl implements ProjectControlRService{
    
    @Autowired
    private ProjectControlRMapper projectControlRMapper;
    
    @Autowired
    private ProjectViewMapper projectViewMapper;

    @Autowired
    private ThreadPoolTaskExecutor taskExecutor;

    @Autowired
    private ProjectControlExtRService projectControlExtRService;


    @Override
    public PageResult<ProjectControlR> searchPage(ProjectControlRSearchVo searchVo) {
        return projectControlRMapper.searchPage(searchVo);
    }

    
    @Override
    public void exportExcel(ProjectControlRSearchVo searchVo, HttpServletResponse response) throws Exception {
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
            "项目控制层级配置.xlsx",
            ProjectControlRExcelVO.class,
            this::convertToExcelVO
        );
    }
    
    /**
     * 将ProjectControlR转换为ProjectControlRExcelVO
     */
    private ProjectControlRExcelVO convertToExcelVO(ProjectControlR entity) {
        ProjectControlRExcelVO excelVO = new ProjectControlRExcelVO();
        BeanUtils.copyProperties(entity, excelVO);
        excelVO.setChange(ProjectEnum.ExcelChangeType.UN_CHANGE.getDesc());
        excelVO.setId(String.valueOf(entity.getId()));

        return excelVO;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public String importExcelAndUpdate(MultipartFile file) throws Exception {
        if (file == null || file.isEmpty()) {
            return "文件为空，请选择要导入的Excel文件";
        }
        
        // 使用EasyExcel读取Excel文件
        List<ProjectControlRExcelVO> excelDataList = ExcelUtils.readSync(file.getInputStream(), ProjectControlRExcelVO.class);
        
        if (excelDataList.isEmpty()) {
            return "Excel文件中没有有效数据";
        }
        
        // 转换为实体对象并批量更新
        List<ProjectControlR> updateList = convertToEntityList(excelDataList, ProjectEnum.ExcelChangeType.CHANGE.getDesc());
        List<Long> controlLevelNullList = updateList.stream()
                .filter(x -> !StringUtils.hasLength(x.getControlLevel()))
                .map(ProjectControlR::getId)
                .toList();
        while (!controlLevelNullList.isEmpty()){
            if (controlLevelNullList.size() > 50){
                // 防止数量太多，超过sql的长度，进行分批处理
                List<Long> longs = controlLevelNullList.subList(0, 50);
                projectControlRMapper.batchUpdateControlLevelById(longs, null);
                controlLevelNullList = controlLevelNullList.subList(50, controlLevelNullList.size());
                log.info("批量更新控制层级为null的记录 ---> {}",50);
                continue;
            }
            log.info("批量更新控制层级为null的记录 ---> {}",controlLevelNullList.size());
            projectControlRMapper.batchUpdateControlLevelById(controlLevelNullList, null);
            if(controlLevelNullList.size() <= 50){
                break;
            }
        }

        //update
        for (ProjectControlR projectControlR : updateList) {
            changeControlLevel(projectControlR);
        }


        return String.format("导入成功！共读取 %d 条数据，成功更新 %d 条", excelDataList.size(), updateList.size());
    }
    
    /**
     * 将Excel数据转换为实体对象列表
     */
    private List<ProjectControlR> convertToEntityList(List<ProjectControlRExcelVO> excelDataList,String change) {
        return excelDataList.stream()
                .filter(excelData -> Objects.equals(excelData.getChange(), change))
                .map(excelData -> {
            ProjectControlR entity = new ProjectControlR();
            // 设置ID和其他字段从Excel数据映射
            if(StringUtils.hasLength(excelData.getId())){
                entity.setId(Long.valueOf(excelData.getId()));
            }
            entity.setPrjCd(excelData.getPrjCd());
            entity.setPrjNm(excelData.getPrjNm());
            entity.setParCd(excelData.getParCd());
            entity.setParNm(excelData.getParNm());
            entity.setLeaf(excelData.getLeaf());
            entity.setControlLevel(excelData.getControlLevel());
            entity.setYear(excelData.getYear());
            return entity;
        }).collect(Collectors.toList());
    }


    @Override
    public void syncProjectToBusiness() {
        Calendar calendar = Calendar.getInstance();
        String year = String.valueOf(calendar.get(Calendar.YEAR)) ;
        Long count = projectControlRMapper.selectCountByYear(year);
        if(count == 0L){
            log.info("同步项目初始化开始....");
            init(year);
            return;
        }
        List<ProjectExceptResp> exceptData = projectControlRMapper.getExceptData(year);
        if(CollectionUtils.isEmpty(exceptData)){
            log.info("项目数据无差异，结束....");
            return;
        }

        List<ProjectExceptResp> addOrUpdateList = new ArrayList<>(exceptData.stream().filter(x -> x.getToAdd().equals("1")).toList());

        Map<Long, ProjectExceptResp> updateOrDelMap = exceptData.stream().filter(x -> x.getToAdd().equals("2"))
                .collect(Collectors.toMap(ProjectExceptResp::getId, x -> x));

        // 获取需要更新的数据
        List<ProjectExceptResp> updateList = new LinkedList<>();
        Iterator<ProjectExceptResp> iterator = addOrUpdateList.iterator();
        while (iterator.hasNext()) {
            ProjectExceptResp next = iterator.next();
            if (updateOrDelMap.containsKey(next.getId())) {
                updateList.add(next);
                iterator.remove();
                updateOrDelMap.remove(next.getId());
            }
        }

        if(!CollectionUtils.isEmpty(addOrUpdateList)){
            List<ProjectControlR> addList = addOrUpdateList.stream().map(x -> {
                ProjectControlR build = ProjectControlR.builder()
                        .prjId(x.getId())
                        .prjCd(x.getPrjCd())
                        .prjNm(x.getPrjNm())
                        .prjCd(x.getPrjCd())
                        .parNm(x.getParNm())
                        .leaf(x.isLeaf())
                        .year(year)
                        .build();
                return build;
            }).toList();
            projectControlRMapper.insertBatch(addList);
        }

        if(!CollectionUtils.isEmpty(updateList)){
            List<ProjectControlR> uList = updateList.stream().map(x -> {
                ProjectControlR build = ProjectControlR.builder()
                        .prjId(x.getId())
                        .prjCd(x.getPrjCd())
                        .prjNm(x.getPrjNm())
                        .parCd(x.getPrjCd())
                        .parNm(x.getParNm())
                        .leaf(x.isLeaf())
                        .build();
                return build;
            }).toList();
            // 分批处理，每批最多500条，避免MERGE语句过大导致性能问题
            int batchSize = 500;
            for (int i = 0; i < uList.size(); i += batchSize) {
                int end = Math.min(i + batchSize, uList.size());
                List<ProjectControlR> batch = uList.subList(i, end);
                projectControlRMapper.updateBatchByPrjCdAndYear(batch, year);
                log.debug("批量更新项目数据，第 {}-{} 条，共 {} 条", i + 1, end, batch.size());
            }
        }

        // 删除
//        if(!updateOrDelMap.isEmpty()){
//            List<Long> keyList = updateOrDelMap.keySet().stream().toList();
//            projectControlRMapper.deleteByIdsAndYear(keyList, year);
//        }
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
            PageResult<ProjectView> pageResult = projectViewMapper.selectPage(pageParam, new QueryWrapper<>());
            
            // 处理查询到的数据
            List<ProjectView> projectViews = pageResult.getList();
            
            // 如果没有更多数据，则退出循环
            if (projectViews.isEmpty()) {
                break;
            }
            List<ProjectControlR> list = projectViews.stream().map(projectView -> {
                ProjectControlR projectControlR = ProjectControlR.builder()
                        .prjId(projectView.getId())
                        .prjCd(projectView.getPrjCd())
                        .prjNm(projectView.getPrjNm())
                        .parCd(projectView.getParCd())
                        .parNm(projectView.getParNm())
                        .leaf(projectView.isLeaf())
                        .year(year)
                        .build();
                return projectControlR;
            }).toList();
            projectControlRMapper.insertBatch(list);

            if( projectViews.size() < pageSize){
                break;
            }

            // 移动到下一页
            currentPage++;
        }
    }


    @Transactional
    @Override
    public void changeControlLevel(ProjectControlR projectControlR) {
        checkParentAndChildren(projectControlR);
        projectControlRMapper.updateWithFields(projectControlR);

        // 移除自动同步调用，改为通过 BudgetEhrOrgMController 手动调用
        // taskExecutor.execute(() -> { projectControlExtRService.syncProjectControlRData(List.of(projectControlR.getPrjCd())); });

    }


    private void checkParentAndChildren(ProjectControlR projectControlR) {
        if(StringUtils.hasLength(projectControlR.getControlLevel())){
            ProjectComBo parComBo = parentHasControlLevel(projectControlR.getParCd(), projectControlR.getYear());
            if(parComBo.isHasControlLevel()){
                ProjectControlR p = parComBo.getProjectControlR();
                throw new ServiceException("Project-100004",
                        "正在执行项目【" + projectControlR.getPrjCd() + "--" + projectControlR.getPrjNm() +
                        "】修改控制层级失败，父项目【" + p.getPrjCd() + "--" + p.getPrjNm() + "】已勾选控制层级"
                        );
            }
            ProjectComBo projectComBo = childrenHasControlLevel(projectControlR.getPrjCd(), projectControlR.getYear());
            if(projectComBo.isHasControlLevel()){
                ProjectControlR c = projectComBo.getProjectControlR();
                throw new ServiceException("Project-100004",
                        "正在执行项目【" + projectControlR.getPrjCd() + "--" + projectControlR.getPrjNm() +
                        "】修改控制层级失败，子项目【" + c.getPrjCd() + "--" + c.getPrjNm() + "】已勾选控制层级"
                        );
            }

        }
    }


    public ProjectComBo parentHasControlLevel(String parPrjCd,String year) {
        return parentHasControlLevel(parPrjCd, year, new HashSet<>());
    }

    private ProjectComBo parentHasControlLevel(String parPrjCd, String year, Set<String> visited) {
        if(!StringUtils.hasLength(parPrjCd)){
            return ProjectComBo.builder().hasControlLevel(false).build();
        }
        
        // 防止循环引用
        if (visited.contains(parPrjCd)) {
            log.warn("检测到循环引用，PRJ_CD: {}，已访问的节点: {}", parPrjCd, visited);
            // 循环引用是数据异常，返回false允许操作继续，但记录警告日志便于排查数据问题
            return ProjectComBo.builder().hasControlLevel(false).build();
        }
        visited.add(parPrjCd);
        
        ProjectControlR parent = projectControlRMapper.selectOne(ProjectControlR::getPrjCd, parPrjCd,ProjectControlR::getYear, year);
        if(parent == null){
            return ProjectComBo.builder().hasControlLevel(false).build();
        }
        if(Objects.equals(EhrEnums.CommonControlLevel.YES.getCode(),parent.getControlLevel())){
            return ProjectComBo.builder().hasControlLevel(true)
                    .projectControlR(parent)
                    .build();
        }
        // 查父项目的 Code
        return parentHasControlLevel(parent.getParCd(), year, visited);
    }


    public ProjectComBo childrenHasControlLevel(String prjCd, String year) {
        return childrenHasControlLevel(prjCd, year, new HashSet<>());
    }

    private ProjectComBo childrenHasControlLevel(String prjCd, String year, Set<String> visited) {
        // 先检查参数有效性
        if(!StringUtils.hasLength(prjCd)){
            return ProjectComBo.builder().hasControlLevel(false).build();
        }
        
        // 防止循环引用
        if (visited.contains(prjCd)) {
            log.warn("检测到循环引用（子项目），PRJ_CD: {}，已访问的节点: {}", prjCd, visited);
            return ProjectComBo.builder().hasControlLevel(false).build();
        }
        visited.add(prjCd);
        
        List<ProjectControlR> child = projectControlRMapper.selectByCombo(prjCd,year);
        if(CollectionUtils.isEmpty(child)){
            return ProjectComBo.builder().hasControlLevel(false).build();
        }
        for (ProjectControlR projectControlR : child) {
            if(Objects.equals(EhrEnums.CommonControlLevel.YES.getCode(),projectControlR.getControlLevel())){
                return ProjectComBo.builder().hasControlLevel(true)
                        .projectControlR(projectControlR)
                        .build();
            }
        }
        for (ProjectControlR projectControlR : child){
            ProjectComBo result = childrenHasControlLevel(projectControlR.getPrjCd(), year, visited);
            if(result.isHasControlLevel()){
                return result;
            }
        }
        return ProjectComBo.builder().hasControlLevel(false).build();
    }



}