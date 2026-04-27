package com.jasolar.mis.module.system.service.budget.morg.impl;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.read.listener.ReadListener;
import com.baomidou.mybatisplus.core.incrementer.IdentifierGenerator;
import com.jasolar.mis.module.system.domain.morg.SystemDepartment;
import com.jasolar.mis.module.system.domain.morg.SystemManagementOrganization;
import com.jasolar.mis.module.system.domain.morg.SystemOrganization;
import com.jasolar.mis.module.system.mapper.morg.SystemDepartmentMapper;
import com.jasolar.mis.module.system.mapper.morg.SystemManagementOrganizationMapper;
import com.jasolar.mis.module.system.mapper.morg.SystemOrganizationMapper;
import com.jasolar.mis.module.system.service.budget.morg.BudgetMOrgService;
import jakarta.annotation.Resource;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * 管理组织导入 Service 实现
 */
@Service
@Slf4j
public class BudgetMOrgServiceImpl implements BudgetMOrgService {

    private static final String SHEET_NAME = "sheet1";
    private static final String DELIMITER = "@";  // 统一使用 @ 作为分隔符，避免值中包含 - 导致分割错误

    @Resource
    private IdentifierGenerator identifierGenerator;
    @Resource
    private SystemOrganizationMapper systemOrganizationMapper;
    @Resource
    private SystemDepartmentMapper systemDepartmentMapper;
    @Resource
    private SystemManagementOrganizationMapper systemManagementOrganizationMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String importData(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return "文件为空，请上传包含管理组织信息的 Excel 文件";
        }

        List<MOrgExcelRow> rows = new ArrayList<>();
        try {
            EasyExcel.read(file.getInputStream(), MOrgExcelRow.class, new MorgExcelListener(rows))
                    .sheet(SHEET_NAME)
                    .headRowNumber(3)
                    .doRead();
        } catch (IOException e) {
            log.error("读取管理组织 Excel 失败", e);
            return "读取 Excel 文件失败：" + e.getMessage();
        } catch (Exception ex) {
            log.error("导入管理组织数据失败", ex);
            return "导入失败：" + ex.getMessage();
        }

        if (rows.isEmpty()) {
            return "Excel 中未解析到可用数据";
        }

        Set<String> orgSet = new LinkedHashSet<>();
        Set<String> deptSet = new LinkedHashSet<>();
        Set<String> morgSet = new LinkedHashSet<>();

        for (MOrgExcelRow row : rows) {
            String orgCode = StringUtils.trimToNull(row.getOrgCode());
            String orgName = StringUtils.trimToNull(row.getOrgName());
            String deptCode = StringUtils.trimToNull(row.getDeptCode());
            String deptName = StringUtils.trimToNull(row.getDeptName());
            String morgCode = StringUtils.trimToNull(row.getMorgCode());
            String morgName = StringUtils.trimToNull(row.getMorgName());

            if (StringUtils.isNoneBlank(orgCode, orgName)) {
                orgSet.add(joinKey(orgCode, orgName));
            }
            if (StringUtils.isNoneBlank(deptCode, deptName)) {
                deptSet.add(joinKey(deptCode, deptName));
            }
            if (StringUtils.isNoneBlank(orgCode, deptCode, morgCode, morgName)) {
                morgSet.add(joinKey(orgCode, deptCode, morgCode, morgName));
            }
        }

        int orgCount = saveOrganizations(orgSet);
        int deptCount = saveDepartments(deptSet);
        int morgCount = saveManagementOrganizations(morgSet);

        return String.format("导入完成：组织 %d 条，部门 %d 条，管理组织 %d 条", orgCount, deptCount, morgCount);
    }

    private int saveOrganizations(Set<String> orgSet) {
        if (orgSet.isEmpty()) {
            return 0;
        }
        List<SystemOrganization> entities = new ArrayList<>(orgSet.size());
        for (String key : orgSet) {
            String[] parts = splitKey(key, 2, DELIMITER);
            if (parts == null || parts.length != 2) {
                log.warn("跳过无法解析的组织数据：{}", key);
                continue;
            }
            SystemOrganization entity = SystemOrganization.builder()
                    .id(identifierGenerator.nextId(null).longValue())
                    .orgCode(parts[0])
                    .orgName(parts[1])
                    .parentOrgCode(null)
                    .build();
            entity.setDeleted(false);
            entities.add(entity);
        }
        if (entities.isEmpty()) {
            return 0;
        }
        boolean success = systemOrganizationMapper.insertBatch(entities);
        return success ? entities.size() : 0;
    }

    private int saveDepartments(Set<String> deptSet) {
        if (deptSet.isEmpty()) {
            return 0;
        }
        List<SystemDepartment> entities = new ArrayList<>(deptSet.size());
        for (String key : deptSet) {
            String[] parts = splitKey(key, 2, DELIMITER);
            if (parts == null || parts.length != 2) {
                log.warn("跳过无法解析的部门数据：{}", key);
                continue;
            }
            SystemDepartment entity = SystemDepartment.builder()
                    .id(identifierGenerator.nextId(null).longValue())
                    .deptCode(parts[0])
                    .deptName(parts[1])
                    .parentDeptCode(null)
                    .build();
            entity.setDeleted(false);
            entities.add(entity);
        }
        if (entities.isEmpty()) {
            return 0;
        }
        boolean success = systemDepartmentMapper.insertBatch(entities);
        return success ? entities.size() : 0;
    }

    private int saveManagementOrganizations(Set<String> morgSet) {
        if (morgSet.isEmpty()) {
            return 0;
        }
        List<SystemManagementOrganization> entities = new ArrayList<>(morgSet.size());
        for (String key : morgSet) {
            String[] parts = splitKey(key, 4, DELIMITER);
            if (parts == null || parts.length != 4) {
                log.warn("跳过无法解析的管理组织数据：{}", key);
                continue;
            }
            SystemManagementOrganization entity = SystemManagementOrganization.builder()
                    .id(identifierGenerator.nextId(null).longValue())
                    .orgCode(parts[0])
                    .deptCode(parts[1])
                    .morgCode(parts[2])
                    .morgName(parts[3])
                    .build();
            entity.setDeleted(false);
            entities.add(entity);
        }
        if (entities.isEmpty()) {
            return 0;
        }
        boolean success = systemManagementOrganizationMapper.insertBatch(entities);
        return success ? entities.size() : 0;
    }

    /**
     * 将多个值用分隔符连接成字符串（统一使用 @ 作为分隔符）
     */
    private String joinKey(String... values) {
        return String.join(DELIMITER, values);
    }

    /**
     * 分割字符串，支持不同的分隔符
     */
    private String[] splitKey(String key, int expectedParts, String delimiter) {
        if (key == null || key.isEmpty()) {
            return null;
        }
        String[] parts = key.split(delimiter, -1);
        if (parts.length != expectedParts) {
            return null;
        }
        return parts;
    }

    /**
     * Excel 数据读取监听器
     */
    private class MorgExcelListener implements ReadListener<MOrgExcelRow> {
        private final List<MOrgExcelRow> dataList;

        public MorgExcelListener(List<MOrgExcelRow> dataList) {
            this.dataList = dataList;
        }

        @Override
        public void invoke(MOrgExcelRow data, AnalysisContext context) {
            dataList.add(data);
        }

        @Override
        public void doAfterAllAnalysed(AnalysisContext context) {
            log.info("Excel读取完成，共读取 {} 条数据", dataList.size());
        }
    }

    /**
     * Excel 数据模型类
     * C列(索引2)=ORG_CODE, D列(索引3)=ORG_NAME
     * E列(索引4)=DEPT_CODE, F列(索引5)=DEPT_NAME
     * G列(索引6)=MORG_CODE, H列(索引7)=MORG_NAME
     */
    @Data
    public static class MOrgExcelRow {
        // C列（索引2）：组织编码
        @ExcelProperty(index = 2)
        private String orgCode;

        // D列（索引3）：组织名称
        @ExcelProperty(index = 3)
        private String orgName;

        // E列（索引4）：部门编码
        @ExcelProperty(index = 4)
        private String deptCode;

        // F列（索引5）：部门名称
        @ExcelProperty(index = 5)
        private String deptName;

        // G列（索引6）：管理组织编码
        @ExcelProperty(index = 6)
        private String morgCode;

        // H列（索引7）：管理组织名称
        @ExcelProperty(index = 7)
        private String morgName;
    }
}
