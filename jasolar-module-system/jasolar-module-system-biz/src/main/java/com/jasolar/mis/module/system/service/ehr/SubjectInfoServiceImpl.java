package com.jasolar.mis.module.system.service.ehr;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.jasolar.mis.framework.common.exception.ServiceException;
import com.jasolar.mis.framework.common.pojo.PageParam;
import com.jasolar.mis.framework.common.pojo.PageResult;
import com.jasolar.mis.framework.excel.core.util.ExcelUtils;
import com.jasolar.mis.module.system.controller.ehr.vo.SubjectInfoExcelVO;
import com.jasolar.mis.module.system.controller.ehr.vo.ErpUnmappedAccountVO;
import com.jasolar.mis.module.system.controller.ehr.vo.SubjectInfoSearchVo;
import com.jasolar.mis.module.system.domain.ehr.SubjectInfo;
import com.jasolar.mis.module.system.domain.ehr.SubjectInfoControlLevelView;
import com.jasolar.mis.module.system.domain.ehr.SubjectView;
import com.jasolar.mis.module.system.enums.EhrEnums;
import com.jasolar.mis.module.system.enums.SubjectEnums;
import com.jasolar.mis.module.system.mapper.ehr.SubjectInfoMapper;
import com.jasolar.mis.module.system.mapper.ehr.SubjectInfoControlLevelViewMapper;
import com.jasolar.mis.module.system.mapper.ehr.SubjectViewMapper;
import com.jasolar.mis.module.system.resp.SubjectComBo;
import com.jasolar.mis.module.system.resp.SubjectExceptResp;
import com.jasolar.mis.module.system.util.PageExportUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletResponse;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * ProjectInfo Service 实现类
 */
@Slf4j
@Service
public class SubjectInfoServiceImpl implements SubjectInfoService {

    @Autowired
    private SubjectInfoMapper subjectInfoMapper;

    @Autowired
    private SubjectInfoControlLevelViewMapper subjectInfoControlLevelViewMapper;
    
    @Autowired
    private SubjectViewMapper subjectViewMapper;

    @Autowired
    private ThreadPoolTaskExecutor taskExecutor;

    @Autowired
    private SubjectExtInfoService subjectExtInfoService;

    @Autowired
    private ErpAccountViewRepository erpAccountViewRepository;


    @Override
    public PageResult<SubjectInfoControlLevelView> searchPage(SubjectInfoSearchVo searchVo) {
        // 先查询所有符合条件的数据（不分页）
        List<SubjectInfoControlLevelView> allList = subjectInfoControlLevelViewMapper.selectList(buildQueryWrapper(searchVo));
        
        if (CollectionUtils.isEmpty(allList)) {
            return new PageResult<>(new ArrayList<>(), 0L);
        }
        
        // 按树形结构排序
        List<SubjectInfoControlLevelView> sortedList = sortByTreeStructure(allList);
        
        // 手动分页
        int pageNo = searchVo.getPageNo() != null && searchVo.getPageNo() > 0 ? searchVo.getPageNo() : 1;
        int pageSize = searchVo.getPageSize() != null && searchVo.getPageSize() > 0 ? searchVo.getPageSize() : 10;
        int start = (pageNo - 1) * pageSize;
        int end = Math.min(start + pageSize, sortedList.size());
        
        List<SubjectInfoControlLevelView> pageList = start < sortedList.size() 
            ? sortedList.subList(start, end) 
            : new ArrayList<>();
        
        return new PageResult<>(pageList, (long) sortedList.size());
    }
    
    /**
     * 构建查询条件（不包含排序和分页）
     */
    private LambdaQueryWrapper<SubjectInfoControlLevelView> buildQueryWrapper(SubjectInfoSearchVo searchVo) {
        LambdaQueryWrapper<SubjectInfoControlLevelView> queryWrapper = Wrappers.lambdaQuery();
        // 根据年份查询
        queryWrapper.eq(searchVo.getYear() != null && !searchVo.getYear().isEmpty(),
                SubjectInfoControlLevelView::getYear, searchVo.getYear());
        if(StringUtils.hasLength( searchVo.getCust1Cd())){
            queryWrapper.and( wrapper -> wrapper.like(SubjectInfoControlLevelView::getCust1Cd, searchVo.getCust1Cd()));
        }
        if( StringUtils.hasLength( searchVo.getCust1Nm())){
             queryWrapper.and( wrapper -> wrapper.like(SubjectInfoControlLevelView::getCust1Nm, searchVo.getCust1Nm()));
        }
        if(StringUtils.hasLength(searchVo.getErpAcctKey())){
             queryWrapper.and( wrapper -> wrapper.like(SubjectInfoControlLevelView::getErpAcctCd, searchVo.getErpAcctKey())
                     .or()
                     .like( SubjectInfoControlLevelView::getErpAcctNm, searchVo.getErpAcctKey())
             );
        }
        // 项目关键字查询 (匹配 acctCd 或 acctNm)
        if (searchVo.getAcctKey() != null && !searchVo.getAcctKey().isEmpty()) {
            queryWrapper.and(wrapper -> wrapper.like(SubjectInfoControlLevelView::getAcctCd, searchVo.getAcctKey())
                    .or()
                    .like(SubjectInfoControlLevelView::getAcctNm, searchVo.getAcctKey()));
        }
        if (StringUtils.hasText(searchVo.getControlAcctCd())) {
            queryWrapper.and(wrapper -> wrapper.like(SubjectInfoControlLevelView::getControlAcctCd, searchVo.getControlAcctCd()));
        }
        if (StringUtils.hasText(searchVo.getControlAcctNm())) {
            queryWrapper.and(wrapper -> wrapper.like(SubjectInfoControlLevelView::getControlAcctNm, searchVo.getControlAcctNm()));
        }
        return queryWrapper;
    }
    
    /**
     * 按树形结构排序（深度优先遍历）
     * 排序规则：父级在前，子级紧跟在父级后面
     * 层级关系基于：cust1Cd + acctCd，子节点的acctParCd应该等于父节点的acctCd
     */
    private List<SubjectInfoControlLevelView> sortByTreeStructure(List<SubjectInfoControlLevelView> allList) {
        if (CollectionUtils.isEmpty(allList)) {
            return new ArrayList<>();
        }
        
        // 构建父子关系Map：key为父科目的key（cust1Cd-acctCd），value为子科目列表
        // 注意：同一个科目（相同的cust1Cd和acctCd）可能有多条记录（不同的erpAcctCd），它们共享相同的子节点
        Map<String, List<SubjectInfoControlLevelView>> parentChildMap = new HashMap<>();
        Map<String, List<SubjectInfoControlLevelView>> subjectGroupMap = new HashMap<>();
        
        // 先按科目分组（cust1Cd + acctCd），同一个科目可能有多条记录
        for (SubjectInfoControlLevelView subject : allList) {
            String subjectKey = buildSubjectKey(subject);
            subjectGroupMap.computeIfAbsent(subjectKey, k -> new ArrayList<>()).add(subject);
        }
        
        // 构建父子关系：子节点的acctParCd应该等于父节点的acctCd
        for (SubjectInfoControlLevelView subject : allList) {
            String parentKey = buildParentKey(subject);
            if (StringUtils.hasText(parentKey) && subjectGroupMap.containsKey(parentKey)) {
                // 找到父节点，将当前节点添加到父节点的子节点列表
                parentChildMap.computeIfAbsent(parentKey, k -> new ArrayList<>()).add(subject);
            }
        }
        
        // 找到所有根节点（acctParCd为空或null，或者父节点不在当前查询结果中的）
        List<String> rootKeys = new ArrayList<>();
        for (SubjectInfoControlLevelView subject : allList) {
            String parentKey = buildParentKey(subject);
            String subjectKey = buildSubjectKey(subject);
            if (!StringUtils.hasText(parentKey) || !subjectGroupMap.containsKey(parentKey)) {
                if (!rootKeys.contains(subjectKey)) {
                    rootKeys.add(subjectKey);
                }
            }
        }
        
        // 对根节点按ID降序排序（取每组中ID最大的记录作为代表）
        rootKeys.sort((a, b) -> {
            List<SubjectInfoControlLevelView> listA = subjectGroupMap.get(a);
            List<SubjectInfoControlLevelView> listB = subjectGroupMap.get(b);
            Long maxIdA = listA.stream().mapToLong(SubjectInfo::getId).max().orElse(0L);
            Long maxIdB = listB.stream().mapToLong(SubjectInfo::getId).max().orElse(0L);
            return Long.compare(maxIdB, maxIdA);
        });
        
        // 深度优先遍历，收集所有节点
        List<SubjectInfoControlLevelView> result = new ArrayList<>();
        for (String rootKey : rootKeys) {
            List<SubjectInfoControlLevelView> rootGroup = subjectGroupMap.get(rootKey);
            // 对同一科目的多条记录按ID降序排序
            rootGroup.sort((a, b) -> Long.compare(b.getId(), a.getId()));
            // 先添加第一条记录（ID最大的）作为代表进行树遍历
            if (!rootGroup.isEmpty()) {
                dfsTraverse(rootGroup.get(0), parentChildMap, subjectGroupMap, result, true);
                // 添加该科目的其他记录（如果有）
                for (int i = 1; i < rootGroup.size(); i++) {
                    result.add(rootGroup.get(i));
                }
            }
        }
        
        return result;
    }
    
    /**
     * 构建科目的唯一标识key（用于层级关系，不包含erpAcctCd）
     * 格式：cust1Cd-acctCd
     */
    private String buildSubjectKey(SubjectInfo subject) {
        String cust1Cd = subject.getCust1Cd() != null ? subject.getCust1Cd() : "";
        return cust1Cd + "-" + subject.getAcctCd();
    }
    
    /**
     * 构建父科目的key（用于查找父节点）
     * 格式：cust1Cd-acctParCd
     */
    private String buildParentKey(SubjectInfoControlLevelView subject) {
        if (!StringUtils.hasText(subject.getAcctParCd())) {
            return null;
        }
        String cust1Cd = subject.getCust1Cd() != null ? subject.getCust1Cd() : "";
        return cust1Cd + "-" + subject.getAcctParCd();
    }
    
    /**
     * 深度优先遍历树形结构
     * @param node 当前节点
     * @param parentChildMap 父子关系Map
     * @param subjectGroupMap 科目分组Map
     * @param result 结果列表
     * @param isFirstInGroup 是否是组内第一条记录（用于决定是否递归遍历子节点）
     */
    private void dfsTraverse(SubjectInfoControlLevelView node, Map<String, List<SubjectInfoControlLevelView>> parentChildMap, 
                            Map<String, List<SubjectInfoControlLevelView>> subjectGroupMap, List<SubjectInfoControlLevelView> result,
                            boolean isFirstInGroup) {
        // 添加当前节点
        result.add(node);
        
        // 只有组内第一条记录才需要递归遍历子节点（避免重复）
        if (!isFirstInGroup) {
            return;
        }
        
        // 获取当前节点的key
        String nodeKey = buildSubjectKey(node);
        
        // 获取子节点列表（按科目分组）
        List<SubjectInfoControlLevelView> children = parentChildMap.get(nodeKey);
        if (!CollectionUtils.isEmpty(children)) {
            // 按科目分组，每个科目只取一条记录进行递归
            Map<String, List<SubjectInfoControlLevelView>> childrenGroupMap = new HashMap<>();
            for (SubjectInfoControlLevelView child : children) {
                String childKey = buildSubjectKey(child);
                childrenGroupMap.computeIfAbsent(childKey, k -> new ArrayList<>()).add(child);
            }
            
            // 对每个子科目组，按ID降序排序
            List<String> childKeys = new ArrayList<>(childrenGroupMap.keySet());
            childKeys.sort((a, b) -> {
                List<SubjectInfoControlLevelView> listA = childrenGroupMap.get(a);
                List<SubjectInfoControlLevelView> listB = childrenGroupMap.get(b);
                Long maxIdA = listA.stream().mapToLong(SubjectInfo::getId).max().orElse(0L);
                Long maxIdB = listB.stream().mapToLong(SubjectInfo::getId).max().orElse(0L);
                return Long.compare(maxIdB, maxIdA);
            });
            
            // 递归遍历子节点
            for (String childKey : childKeys) {
                List<SubjectInfoControlLevelView> childGroup = childrenGroupMap.get(childKey);
                childGroup.sort((a, b) -> Long.compare(b.getId(), a.getId()));
                // 递归遍历第一条记录
                dfsTraverse(childGroup.get(0), parentChildMap, subjectGroupMap, result, true);
                // 添加该科目的其他记录
                for (int i = 1; i < childGroup.size(); i++) {
                    result.add(childGroup.get(i));
                }
            }
        }
    }

    @Override
    public void exportExcel(SubjectInfoSearchVo searchVo, HttpServletResponse response) throws Exception {
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
            "科目控制层级配置.xlsx",
            SubjectInfoExcelVO.class,
            this::convertToExcelVO
        );
    }
    
    /**
     * 将ProjectInfo转换为ProjectInfoExcelVO
     */
    private SubjectInfoExcelVO convertToExcelVO(SubjectInfoControlLevelView entity) {
        SubjectInfoExcelVO excelVO = new SubjectInfoExcelVO();
        excelVO.setChange(SubjectEnums.ExcelChangeType.UN_CHANGE.getDesc());
        BeanUtils.copyProperties(entity, excelVO);
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
        List<SubjectInfoExcelVO> excelDataList = ExcelUtils.readSync(file.getInputStream(), SubjectInfoExcelVO.class);
        
        if (excelDataList.isEmpty()) {
            return "Excel文件中没有有效数据";
        }
        
        // 转换为实体对象并批量更新
        List<SubjectInfo> updateList = convertToEntityList(excelDataList, SubjectEnums.ExcelChangeType.CHANGE.getDesc());
        
        // 收集所有涉及 CONTROL_LEVEL 更新的记录的 year（用于后续异步调用 syncSubjectInfoData）
        Set<String> controlLevelUpdatedYears = new HashSet<>();
        
        List<Long> controlLevelNullList = updateList.stream()
                .filter(x -> !StringUtils.hasLength(x.getControlLevel()))
                .map(SubjectInfo::getId)
                .toList();
        // 收集这些记录的 year
        // 创建 final 副本用于 lambda 表达式
        final List<Long> controlLevelNullListFinal = new ArrayList<>(controlLevelNullList);
        if (!controlLevelNullListFinal.isEmpty()) {
            controlLevelUpdatedYears.addAll(updateList.stream()
                    .filter(e -> controlLevelNullListFinal.contains(e.getId()))
                    .map(SubjectInfo::getYear)
                    .filter(StringUtils::hasText)
                    .collect(Collectors.toList()));
        }
        while (!controlLevelNullList.isEmpty()){
            if(controlLevelNullList.size() > 50){
                List<Long> ids = controlLevelNullList.subList(0, 50);
                subjectInfoMapper.batchUpdateControlLevelById(ids, null);
                controlLevelNullList = controlLevelNullList.subList(50, controlLevelNullList.size());
                log.info("批量更新控制层级为null的记录 ---> {}",50);
                continue;
            }
            log.info("批量更新控制层级为null的记录 ---> {}",controlLevelNullList.size());
            subjectInfoMapper.batchUpdateControlLevelById(controlLevelNullList, null);
            if(controlLevelNullList.size() <= 50){
                break;
            }
        }

        // add
        List<SubjectInfo> addList = convertToEntityList(excelDataList, SubjectEnums.ExcelChangeType.ADD.getDesc());
        checkAddListParams(addList);


        for (SubjectInfo subjectInfo : addList) {
            log.info("导入的添加数据为 ---> {}",subjectInfo);
            long count = subjectInfoMapper.getCountByCust1CdAndAcctCdAndErp(subjectInfo.getCust1Cd(), subjectInfo.getAcctCd(),subjectInfo.getErpAcctCd(),subjectInfo.getYear());
            if(count > 0){
                throw new ServiceException("Subject--100002","费用类型【" + subjectInfo.getCust1Cd()+ "--" + subjectInfo.getCust1Nm()
                        + "】--科目【" + subjectInfo.getAcctCd() + "--" + subjectInfo.getAcctNm() + "】--ERP[" + subjectInfo.getErpAcctCd() + "--" + subjectInfo.getErpAcctNm() + "]"
                        +  "已存在");
            }
            checkParentAndChildren(subjectInfo);
            subjectInfoMapper.insert(subjectInfo);
        }

        // update
        for (SubjectInfo subjectInfo : updateList) {
            log.info("导入的修改数据为 ---> {}",subjectInfo);
            // 检查 Excel 数据中是否包含 controlLevel 字段（如果包含，说明这个字段被更新了）
            boolean controlLevelInExcel = excelDataList.stream()
                    .anyMatch(excel -> excel.getId() != null && excel.getId().equals(String.valueOf(subjectInfo.getId()))
                            && excel.getChange().equals(SubjectEnums.ExcelChangeType.CHANGE.getDesc())
                            && excel.getControlLevel() != null);
            
            // 如果 Excel 数据中包含 controlLevel 字段，收集 year
            if (controlLevelInExcel && StringUtils.hasText(subjectInfo.getYear())) {
                controlLevelUpdatedYears.add(subjectInfo.getYear());
            }
            
            changeControlLevel(subjectInfo);
        }
        
        // 移除自动同步调用，改为通过 BudgetEhrOrgMController 手动调用
        // if (!controlLevelUpdatedYears.isEmpty()) {
        //     taskExecutor.execute(() -> { ... });
        // }
        
        return String.format("导入成功！共读取 %d 条数据，成功更新 %d 条", excelDataList.size(),updateList.size() + addList.size());
    }

    private void checkAddListParams(List<SubjectInfo> addList){
        for (SubjectInfo subjectInfo : addList) {
            if (!StringUtils.hasLength(subjectInfo.getCust1Cd())) {
                throw new ServiceException("Subject--100001","新增费用类型不能为空");
            }
            if (!StringUtils.hasLength(subjectInfo.getCust1Nm())) {
                throw new ServiceException("Subject--100002","新增费用类型名称不能为空");
            }
            if (!StringUtils.hasLength(subjectInfo.getAcctCd())) {
                throw new ServiceException("Subject--100003","新增科目代码不能为空");
            }
            if (!StringUtils.hasLength(subjectInfo.getAcctNm())) {
                throw new ServiceException("Subject--100004","新增科目名称不能为空");
            }

//            if(Objects.isNull(subjectInfo.getLeaf())){
//                throw new ServiceException("Subject--100005","新增是否叶子节点不能为空");
//            }

            if (!StringUtils.hasLength(subjectInfo.getYear())) {
                throw new ServiceException("Subject--100007","新增年度不能为空");
            }
        }
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public String importErpAcctFromExcel(MultipartFile file) throws Exception {
        if (file == null || file.isEmpty()) {
            return "文件为空，请选择要导入的Excel文件";
        }

        // 1. 读取数据库中已存在的科目（默认只查未删除的）
        List<SubjectInfo> subjectList = subjectInfoMapper.selectList(null);
        if (CollectionUtils.isEmpty(subjectList)) {
            return "数据库中没有可更新的科目信息";
        }

        // 2. 使用 EasyExcel 按行读取原始数据，从第三行开始，仅关注 B、E、F 三列
        // B 列索引 = 1，E 列索引 = 4，F 列索引 = 5（0 基）
        List<Map<Integer, String>> excelRows =
                EasyExcel.read(file.getInputStream()).sheet(0).doReadSync();

        if (CollectionUtils.isEmpty(excelRows) || excelRows.size() <= 2) {
            return "Excel 文件中没有有效数据（第三行及之后为空）";
        }

        // Map 的 key 格式为 "cust1Cd-acctCd"，value 为 "erpCd@erpNm"
        Map<String, String> excelMap = new HashMap<>();
        for (int i = 2; i < excelRows.size(); i++) { // 从第三行开始
            Map<Integer, String> row = excelRows.get(i);
            if (row == null || row.isEmpty()) {
                continue;
            }
            String bVal = trimToNull(row.get(1)); // B 列，格式为 CU10901-A01030301
            String eVal = trimToNull(row.get(4)); // E 列
            String fVal = trimToNull(row.get(5)); // F 列

            // F 列为空或 B 列为空则过滤掉
            if (!StringUtils.hasText(fVal) || !StringUtils.hasText(bVal)) {
                continue;
            }

            // B 列形如 CU10901-A01030301，保持完整格式作为 key
            // 这样 key 就是 "CU10901-A01030301"，后续匹配时需要同时匹配 cust1Cd 和 acctCd
            String key = bVal; // 保持完整格式：cust1Cd-acctCd

            String value = fVal + "@" + (eVal == null ? "" : eVal);
            excelMap.put(key, value);
        }

        if (excelMap.isEmpty()) {
            return "Excel 数据中没有符合条件的记录（F 列非空）";
        }

        // 3. 遍历 SUBJECT_INFO 列表，用 cust1Cd + "-" + acctCd 去 map 中匹配，匹配到则更新 ERP 字段
        // 必须同时满足 cust1Cd 和 acctCd 才能匹配
        List<SubjectInfo> needUpdateList = new ArrayList<>();
        for (SubjectInfo subject : subjectList) {
            String acctCd = subject.getAcctCd();
            String cust1Cd = subject.getCust1Cd();
            
            if (!StringUtils.hasText(acctCd)) {
                continue;
            }
            
            // 构建匹配 key：cust1Cd + "-" + acctCd
            // 如果 cust1Cd 为空，则 key 为 "-" + acctCd
            String matchKey = (StringUtils.hasText(cust1Cd) ? cust1Cd : "") + "-" + acctCd;
            
            String value = excelMap.get(matchKey);
            if (value == null) {
                continue;
            }
            
            String[] parts = value.split("@", 2);
            String erpCd = parts.length > 0 ? parts[0] : null;
            String erpNm = parts.length > 1 ? parts[1] : null;

            subject.setErpAcctCd(erpCd);
            subject.setErpAcctNm(erpNm);
            needUpdateList.add(subject);
        }

        if (needUpdateList.isEmpty()) {
            return String.format("处理完成，但没有任何科目编码在 Excel 中匹配到；Excel 有效行数：%d", excelMap.size());
        }

        // 4. 批量按 ID 更新，仅更新过的 SubjectInfo；这里复用已有的批量更新能力
        boolean success = subjectInfoMapper.updateBatch(needUpdateList);
        int updateCount = success ? needUpdateList.size() : 0;

        return String.format(
                "处理完成，总科目数 %d，Excel 有效行数 %d，成功匹配并更新 %d 条",
                subjectList.size(), excelMap.size(), updateCount);
    }

    /**
     * 将字符串 trim 后，若为空返回 null
     */
    private String trimToNull(String str) {
        if (str == null) {
            return null;
        }
        String s = str.trim();
        return s.isEmpty() ? null : s;
    }

    /**
     * 将Excel数据转换为实体对象列表
     */
    private List<SubjectInfo> convertToEntityList(List<SubjectInfoExcelVO> excelDataList,String change) {
        return excelDataList.stream()
                .filter(excelData -> Objects.equals(excelData.getChange(),change))
                .map(excelData -> {
                    SubjectInfo entity = new SubjectInfo();
                    if(StringUtils.hasLength(excelData.getId())){
                        entity.setId(Long.valueOf(excelData.getId()));
                    }
                    entity.setCust1Cd(excelData.getCust1Cd());
                    entity.setCust1Nm(excelData.getCust1Nm());
                    entity.setAcctCd(excelData.getAcctCd());
                    entity.setAcctNm(excelData.getAcctNm());
                    entity.setAcctParCd(excelData.getAcctParCd());
                    entity.setAcctParNm(excelData.getAcctParNm());
                    entity.setLeaf(excelData.getLeaf());
                    entity.setYear(excelData.getYear());
                    entity.setControlLevel(excelData.getControlLevel());
                    entity.setErpAcctCd(excelData.getErpAcctCd());
                    entity.setErpAcctNm(excelData.getErpAcctNm());
                return entity;
        }).collect(Collectors.toList());
    }
    
    @Override
    public void syncSubjectToBusiness() {
        Calendar calendar = Calendar.getInstance();
        String year = String.valueOf(calendar.get(Calendar.YEAR)) ;
        Long count = subjectInfoMapper.selectCountByYear(year);
        log.info("{}年的数据为{}",year,count);
        if(count == 0L){
            log.info("同步科目初始化开始....");
            init(year);
            return;
        }
        List<SubjectExceptResp> exceptData = subjectInfoMapper.getExceptData(year);
        if(CollectionUtils.isEmpty(exceptData)){
            log.info("科目数据无差异，结束....");
            return;
        }
        log.info("差异的数据条数为{}",exceptData.size());


        List<SubjectExceptResp> deleteListResp = exceptData.stream().filter(x -> x.getToAdd().equals("2")).toList();

        // 删除
        if(!deleteListResp.isEmpty()){
            subjectInfoMapper.deleteByComFields(deleteListResp);
        }

        List<SubjectExceptResp> addListResp = exceptData.stream().filter(x -> x.getToAdd().equals("1")).toList();
        if(!CollectionUtils.isEmpty(addListResp)){
            List<SubjectInfo> addList = addListResp.stream().map(x -> {
                SubjectInfo build = SubjectInfo.builder()
                        .id(IdWorker.getId())
                        .cust1Cd(x.getCust1Cd())
                        .cust1Nm(x.getCust1Nm())
                        .acctCd(x.getAcctCd())
                        .acctNm(x.getAcctNm())
                        .acctParCd(x.getParAcctCd())
                        .acctParNm(x.getParAcctNm())
                        .year(year)
                        .leaf(x.isLeaf())
                        .build();
                return build;
            }).toList();
            subjectInfoMapper.insertBatch(addList);
        }



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
            
            // 使用subjectViewMapper分页查询数据
            PageResult<SubjectView> pageResult = subjectViewMapper.selectPage(pageParam, new QueryWrapper<>());

            // 处理查询到的数据
            List<SubjectView> subjectViews = pageResult.getList();
            log.info("正在处理第 {} 页数据，共 {} 条数据", currentPage, subjectViews.size());

            if (subjectViews.isEmpty()) {
                break;
            }

            List<SubjectInfo> list = subjectViews.stream().map(subjectView -> {
                log.info("正在处理数据：{}", subjectView);
                SubjectInfo subjectInfo = SubjectInfo.builder()
                        .id(IdWorker.getId())
                        .cust1Cd(subjectView.getCust1Cd())
                        .cust1Nm(subjectView.getCust1Nm())
                        .acctCd(subjectView.getAcctCd())
                        .acctNm(subjectView.getAcctNm())
                        .acctParCd(subjectView.getParAcctCd())
                        .acctParNm(subjectView.getParAcctNm())
                        .leaf(subjectView.isLeaf())
                        .year(year)
                        .build();
                return subjectInfo;
            }).toList();
            subjectInfoMapper.insertBatch(list);
            log.info("已处理 {} 页数据，共 {} 条数据", currentPage, subjectViews.size());

            // 如果没有更多数据，则退出循环
            if (subjectViews.size() < pageSize) {
                break;
            }

            // 移动到下一页
            currentPage++;
        }
    }

    @Transactional
    @Override
    public void changeControlLevel(SubjectInfo subjectInfo) {
        checkParentAndChildren(subjectInfo);
        subjectInfoMapper.updateWithNullFields(subjectInfo);

        // 移除自动同步调用，改为通过 BudgetEhrOrgMController 手动调用
        // taskExecutor.execute(() -> { ... });

    }

    private void checkParentAndChildren(SubjectInfo subjectInfo) {
        if(StringUtils.hasLength(subjectInfo.getControlLevel())){
            SubjectComBo parentComBo = parentHasControlLevel(subjectInfo.getCust1Cd(), subjectInfo.getAcctParCd(), subjectInfo.getErpAcctCd(), subjectInfo.getYear());
            if(parentComBo.isHasControlLevel()){
                SubjectInfo r = parentComBo.getSubjectInfo();
                throw new ServiceException("Subject-100004",
                        "正执行科目[" + subjectInfo.getAcctCd() + "--" + subjectInfo.getAcctNm() + "]" +
                        "费用类型[" + subjectInfo.getCust1Cd() + "],ERP科目组织["+
                                subjectInfo.getErpAcctCd()+ "--" + subjectInfo.getErpAcctNm()+"],父级科目["+
                                r.getAcctCd() + "--" + r.getAcctNm() + "]已勾选编制层级，请勿重复勾选！"
                );
            }

            SubjectComBo childComBo = childHasControlLevel(subjectInfo.getCust1Cd(), subjectInfo.getAcctCd(), subjectInfo.getErpAcctCd(), subjectInfo.getYear());
            if(childComBo.isHasControlLevel()){
                SubjectInfo r = childComBo.getSubjectInfo();
                throw new ServiceException("Subject-100005",
                        "正执行科目[" + subjectInfo.getAcctCd() + "--" + subjectInfo.getAcctNm() + "]" +
                        "费用类型[" + subjectInfo.getCust1Cd() + "],ERP科目组织["+
                                subjectInfo.getErpAcctCd()+ "--" + subjectInfo.getErpAcctNm()+"],子级科目["+
                                r.getAcctCd() + "--" + r.getAcctNm() + "]已勾选编制层级，请勿重复勾选！"
                );
            }
        }
    }


    /**
     * 递归判断父级组织是否已勾选编制层级
     */
    public SubjectComBo parentHasControlLevel(String custCd,String parAcctCd,String erpAcctCd, String year) {
        if ((!StringUtils.hasLength(custCd)) ||  (!StringUtils.hasLength(parAcctCd))){
            return SubjectComBo.builder().hasControlLevel(false).build();
        }
        SubjectInfo parent = subjectInfoMapper.selectComboOne(custCd, parAcctCd,erpAcctCd ,year);
        if (parent == null) {
            return SubjectComBo.builder().hasControlLevel(false).build();
        }
        if(Objects.equals(EhrEnums.CommonControlLevel.YES.getCode(),parent.getControlLevel())){
            return SubjectComBo.builder().hasControlLevel(true).subjectInfo(parent).build();
        }
        return parentHasControlLevel(custCd,parent.getAcctParCd(), erpAcctCd, year);
    }

    /**
     * 递归判断子级组织是否已勾选编制层级 dfs 方式
     */

    
    private SubjectComBo childHasControlLevel(String custCd, String acctCd, String erpAcctCd, String year) {
       List<SubjectInfo> child  = subjectInfoMapper.selectByCombo(custCd, acctCd,erpAcctCd, year);
       if(CollectionUtils.isEmpty(child)){
           return SubjectComBo.builder().hasControlLevel(false).build();
       }
        for (SubjectInfo subjectInfo : child) {
            if(Objects.equals(EhrEnums.CommonControlLevel.YES.getCode(),subjectInfo.getControlLevel())){
                return SubjectComBo.builder().hasControlLevel(true).subjectInfo(subjectInfo).build();
            }
        }

       for (SubjectInfo subjectInfo : child) {
           SubjectComBo ch =  childHasControlLevel(custCd, subjectInfo.getAcctCd(), erpAcctCd, year);
           if(ch.isHasControlLevel()){
               return ch;
           }
       }
       return SubjectComBo.builder().hasControlLevel(false).build();
    }

    @Override
    public List<ErpUnmappedAccountVO> listUnmappedErpAccounts() {
        List<String> localErpAcctCds = subjectInfoMapper.selectDistinctErpAcctCdsNotBlank();
        Set<String> localErpAcctSet = localErpAcctCds == null ? new HashSet<>() :
                localErpAcctCds.stream()
                        .filter(StringUtils::hasText)
                        .map(String::trim)
                        .collect(Collectors.toSet());

        List<ErpUnmappedAccountVO> erpRows = erpAccountViewRepository.queryAllMemberCodeAndName();
        if (CollectionUtils.isEmpty(erpRows)) {
            return Collections.emptyList();
        }

        return erpRows.stream()
                .filter(item -> StringUtils.hasText(item.getMemberCode()))
                .filter(item -> !localErpAcctSet.contains(item.getMemberCode().trim()))
                .collect(Collectors.toMap(
                        item -> item.getMemberCode().trim(),
                        Function.identity(),
                        (first, second) -> first,
                        LinkedHashMap::new
                ))
                .values()
                .stream()
                .collect(Collectors.toList());
    }



}