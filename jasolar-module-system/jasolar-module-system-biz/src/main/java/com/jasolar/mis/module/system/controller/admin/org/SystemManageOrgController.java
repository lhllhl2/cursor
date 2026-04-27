package com.jasolar.mis.module.system.controller.admin.org;

import com.jasolar.mis.framework.common.pojo.CommonResult;
import com.jasolar.mis.framework.common.pojo.PageResult;
import com.jasolar.mis.module.system.api.org.OrgApi;
import com.jasolar.mis.module.system.api.org.vo.OrgRespVO;

import com.jasolar.mis.module.system.controller.admin.org.vo.OrgBindUserGroupReqVO;
import com.jasolar.mis.module.system.controller.admin.org.vo.ManageOrgListReqVO;
import com.jasolar.mis.module.system.controller.admin.org.vo.OrgPageReqVO;
import com.jasolar.mis.module.system.domain.admin.org.SystemManageOrgDO;
import com.jasolar.mis.module.system.domain.admin.org.UserGroupOrganizationDO;
import com.jasolar.mis.module.system.domain.admin.usergroup.SystemUserGroupDo;
import com.jasolar.mis.module.system.mapper.admin.org.SystemManageOrgMapper;
import com.jasolar.mis.module.system.mapper.admin.org.UserGroupOrganizationMapper;
import com.jasolar.mis.module.system.mapper.admin.usergroup.UserGroupMapper;
import com.jasolar.mis.module.system.service.admin.org.SystemManageOrgService;
import com.jasolar.mis.module.system.service.admin.usergroup.UserGroupService;
import com.jasolar.mis.module.system.util.DistributedLockUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.annotation.security.PermitAll;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Tag(name = "管理后台 - 组织管理")
@RestController
@RequestMapping("/system/org")
@Validated
@Slf4j
public class SystemManageOrgController implements OrgApi {

    @Resource
    private SystemManageOrgService organizationService;
    
    @Resource
    private UserGroupOrganizationMapper userGroupOrganizationMapper;
    
    @Resource
    private SystemManageOrgMapper organizationMapper;
    
    @Resource
    private UserGroupMapper userGroupMapper;
    
    @Resource
    private UserGroupService userGroupService;

    @Autowired
    private ThreadPoolTaskExecutor taskExecutor;



    @Autowired
    private RedissonClient redissonClient;

    @Autowired
    private SystemManageOrgService systemManageOrgService;


    @PostMapping("/page")
    @Operation(summary = "分页查询组织列表")
    @PermitAll
    public CommonResult<PageResult<OrgRespVO>> getOrgPage(@Valid @RequestBody OrgPageReqVO reqVO) {
        // 检查是否有搜索条件
        boolean hasSearchCondition = hasSearchCondition(reqVO);
        
        PageResult<OrgRespVO> result;
        if (hasSearchCondition) {
            // 有搜索条件，调用搜索方法
            result = organizationService.searchOrgPage(reqVO);
        } else {
            // 无搜索条件，调用原有的分页方法
            result = organizationService.getOrgPage(reqVO);
        }
        
        return CommonResult.success(result);
    }
    
    /**
     * 检查是否有搜索条件
     */
    private boolean hasSearchCondition(OrgPageReqVO reqVO) {
        return (reqVO.getName() != null && !reqVO.getName().trim().isEmpty()) ||
               (reqVO.getCode() != null && !reqVO.getCode().trim().isEmpty());
    }
    
    @PostMapping("/bind-user-groups")
    @Operation(summary = "组织绑定用户组")
    @PermitAll
    public CommonResult<Boolean> bindUserGroups(@Valid @RequestBody OrgBindUserGroupReqVO reqVO) {
        // 先执行绑定操作
        organizationService.bindUserGroups(reqVO);
        return CommonResult.success(true);
    }
    

    @PostMapping("/list")
    @Operation(summary = "查询组织列表（不分页）")
    @PermitAll
    public CommonResult<List<SystemManageOrgDO>> getOrgList(@Valid @RequestBody ManageOrgListReqVO reqVO) {
        List<SystemManageOrgDO> result = organizationService.getOrgList(reqVO);
        return CommonResult.success(result);
    }
    
    /**
     * 实现 OrgApi 接口的方法
     * 查询组织树列表（不分页）
     * 同时支持 Controller 直接调用和 Feign 远程调用
     */
    @PostMapping("/tree")
    @Operation(summary = "查询组织树列表（不分页）")
    @PermitAll
    @Override
    public CommonResult<List<OrgRespVO>> getOrgTreeList(@Valid @RequestBody com.jasolar.mis.module.system.api.org.vo.OrgListReqVO reqVO) {
        // 将 API VO 转换为 Controller VO（字段相同，直接复制）
        ManageOrgListReqVO controllerReqVO = new ManageOrgListReqVO();
        controllerReqVO.setName(reqVO.getName());
        controllerReqVO.setCode(reqVO.getCode());

        // 调用服务层方法
        List<OrgRespVO> controllerResult = organizationService.getOrgTree(controllerReqVO);
        
        // 将 Controller VO 转换为 API VO（字段相同，但类型不同需要转换）
        List<com.jasolar.mis.module.system.api.org.vo.OrgRespVO> apiResult = convertToApiRespVO(controllerResult);
        
        return CommonResult.success(apiResult);
    }
    
    /**
     * 将 Controller VO 转换为 API VO
     */
    private List<OrgRespVO> convertToApiRespVO(List<OrgRespVO> controllerList) {
        if (controllerList == null || controllerList.isEmpty()) {
            return new ArrayList<>();
        }
        
        return controllerList.stream()
                .map(this::convertSingleToApiRespVO)
                .collect(java.util.stream.Collectors.toList());
    }
    
    /**
     * 转换单个 VO（包括递归转换 children）
     */
    private OrgRespVO convertSingleToApiRespVO(OrgRespVO controllerVO) {
        com.jasolar.mis.module.system.api.org.vo.OrgRespVO apiVO = new com.jasolar.mis.module.system.api.org.vo.OrgRespVO();
        apiVO.setId(controllerVO.getId());
        apiVO.setName(controllerVO.getName());
        apiVO.setCode(controllerVO.getCode());
        apiVO.setPCode(controllerVO.getPCode());
        apiVO.setPName(controllerVO.getPName());
        apiVO.setOrgType(controllerVO.getOrgType());
        apiVO.setIsApprovalLastLvl(controllerVO.getIsApprovalLastLvl());
        apiVO.setScriptType(controllerVO.getScriptType());
        apiVO.setCreateTime(controllerVO.getCreateTime());
        apiVO.setUpdateTime(controllerVO.getUpdateTime());
        apiVO.setCreator(controllerVO.getCreator());
        apiVO.setUpdater(controllerVO.getUpdater());
        
        // 递归转换 children
        if (controllerVO.getChildren() != null && !controllerVO.getChildren().isEmpty()) {
            List<com.jasolar.mis.module.system.api.org.vo.OrgRespVO> apiChildren = controllerVO.getChildren().stream()
                    .map(this::convertSingleToApiRespVO)
                    .collect(java.util.stream.Collectors.toList());
            apiVO.setChildren(apiChildren);
        }
        
        return apiVO;
    }
    
    @PostMapping("/batch-export")
    @Operation(summary = "批量导出组织用户组关系")
    @PermitAll
    public ResponseEntity<byte[]> batchExport() throws IOException {
        String DELIM = "§";
        
        // 1. 加载Excel模板文件
        ClassPathResource templateResource = new ClassPathResource("excel/用户组&组织导入模板(组织管理界面).xlsx");
        try (InputStream templateInputStream = templateResource.getInputStream();
             XSSFWorkbook workbook = new XSSFWorkbook(templateInputStream)) {
            
            Sheet sheet = workbook.getSheetAt(0); // 获取第一个Sheet
            
            // 2. 查询所有用户组组织关系（DELETED = 0）
            List<UserGroupOrganizationDO> relationList = userGroupOrganizationMapper.selectList(null);
            
            // 3. 组装currentOrgUserGroupRelationMap
            Map<Long, String> currentOrgUserGroupRelationMap = new HashMap<>();
            Set<Long> currentOrgIds = new HashSet<>();
            Set<Long> currentUserGroupIds = new HashSet<>();
            
            for (UserGroupOrganizationDO relation : relationList) {
                if (relation.getDeleted() != null && relation.getDeleted()) {
                    continue; // 跳过已删除的记录
                }
                String value = relation.getOrganizationId() + DELIM + relation.getUserGroupId();
                currentOrgUserGroupRelationMap.put(relation.getId(), value);
                currentOrgIds.add(relation.getOrganizationId());
                currentUserGroupIds.add(relation.getUserGroupId());
            }
            
            // 4. 查询组织数据
            // 4. 查询组织数据（分批处理，避免ORA-01795错误）
            Map<Long, String> currentOrgMap = new HashMap<>();
            if (!currentOrgIds.isEmpty()) {
                List<Long> orgIdList = new ArrayList<>(currentOrgIds);
                int batchSize = 500; // Oracle限制为1000，使用500更安全

                for (int i = 0; i < orgIdList.size(); i += batchSize) {
                    int endIndex = Math.min(i + batchSize, orgIdList.size());
                    List<Long> batchIds = orgIdList.subList(i, endIndex);

                    List<SystemManageOrgDO> orgBatchList = organizationMapper.selectBatchIds(batchIds);
                    for (SystemManageOrgDO org : orgBatchList) {
                        if (org.getDeleted() != null && org.getDeleted()) {
                            continue;
                        }
                        String value = org.getCode() + DELIM + org.getName();
                        currentOrgMap.put(org.getId(), value);
                    }
                }
            }

            // 5. 查询用户组数据（分批处理，避免ORA-01795错误）
            Map<Long, String> currentUserGroupMap = new HashMap<>();
            if (!currentUserGroupIds.isEmpty()) {
                List<Long> userGroupIdList = new ArrayList<>(currentUserGroupIds);
                int batchSize = 500; // Oracle限制为1000，使用500更安全

                for (int i = 0; i < userGroupIdList.size(); i += batchSize) {
                    int endIndex = Math.min(i + batchSize, userGroupIdList.size());
                    List<Long> batchIds = userGroupIdList.subList(i, endIndex);

                    List<SystemUserGroupDo> userGroupBatchList = userGroupMapper.selectBatchIds(batchIds);
                    for (SystemUserGroupDo userGroup : userGroupBatchList) {
                        if (userGroup.getDeleted() != null && userGroup.getDeleted()) {
                            continue;
                        }
                        currentUserGroupMap.put(userGroup.getId(), userGroup.getName());
                    }
                }
            }


            // 6. 遍历currentOrgUserGroupRelationMap，替换value
            Map<Long, String> newMap = new HashMap<>();
            for (Map.Entry<Long, String> entry : currentOrgUserGroupRelationMap.entrySet()) {
                Long id = entry.getKey();
                String value = entry.getValue();
                String[] parts = value.split(Pattern.quote(DELIM));
                
                Long organizationId = Long.parseLong(parts[0]);
                Long userGroupId = Long.parseLong(parts[1]);
                
                String orgValue = currentOrgMap.get(organizationId);
                String userGroupValue = currentUserGroupMap.get(userGroupId);
                
                if (orgValue != null && userGroupValue != null) {
                    String newValue = orgValue + DELIM + userGroupValue;
                    newMap.put(id, newValue);
                }
            }
            
            // 7. 填充数据到Excel
            int startRow = 1; // 从第2行开始填充数据（第1行是表头）
            for (Map.Entry<Long, String> entry : newMap.entrySet()) {
                String value = entry.getValue();
                String[] parts = value.split(Pattern.quote(DELIM));
                
                if (parts.length >= 3) {
                    Row row = sheet.createRow(startRow++);
                    setCellValue(row, 0, parts[0]); // A列：组织编码
                    setCellValue(row, 1, parts[1]); // B列：组织名称
                    setCellValue(row, 2, parts[2]); // C列：组织用户组名称
                }
            }
            
            // 8. 写入字节数组并返回
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            byte[] excelData = outputStream.toByteArray();
            
            String fileName = "组织用户组关系导出.xlsx";
            // 使用 RFC 5987 标准格式，同时设置 filename 和 filename* 以兼容不同浏览器
            String encodedFileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8.toString())
                    .replaceAll("\\+", "%20"); // URLEncoder 会将空格编码为 +，需要替换为 %20
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            // 同时设置 filename 和 filename*，filename* 使用 RFC 5987 格式
            headers.set(HttpHeaders.CONTENT_DISPOSITION, 
                    "attachment; filename=\"" + encodedFileName + "\"; filename*=UTF-8''" + encodedFileName);
            
            return ResponseEntity.ok().headers(headers).body(excelData);
        }
    }
    
    /**
     * 设置单元格值
     */
    private void setCellValue(Row row, int columnIndex, Object value) {
        Cell cell = row.createCell(columnIndex);
        if (value == null) {
            cell.setBlank();
        } else {
            cell.setCellValue(value.toString());
        }
    }
    
    @Operation(summary = "上传Excel并同步组织与用户组关系")
    @PostMapping(value = "/syncOrgUserGroupRelation")
    @PermitAll
    public CommonResult<String> syncOrgUserGroupRelation(@RequestParam("file") MultipartFile file) {
        long startTime = System.currentTimeMillis();
        log.info("=== 开始处理Excel文件（同步组织与用户组关系） ===");
        
        if (file == null || file.isEmpty()) {
            log.error("文件参数为空");
            return CommonResult.error("500", "文件参数为空，请检查请求格式");
        }
        
        log.info("文件名: {}, 文件大小: {} bytes", file.getOriginalFilename(), file.getSize());
        
        try {
            // 读取Excel文件
            Workbook workbook = WorkbookFactory.create(file.getInputStream());
            
            // 查找名为"组织用户组关系数据"的sheet页
            Sheet targetSheet = null;
            String targetSheetName = "组织用户组关系数据";
            
            for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
                Sheet sheet = workbook.getSheetAt(i);
                if (targetSheetName.equals(sheet.getSheetName())) {
                    targetSheet = sheet;
                    log.info("找到目标Sheet页: {}", targetSheetName);
                    break;
                }
            }
            
            if (targetSheet == null) {
                workbook.close();
                log.error("未找到名为 '{}' 的Sheet页", targetSheetName);
                return CommonResult.error("500", "未找到名为 '" + targetSheetName + "' 的Sheet页");
            }
            
            // 用于存储Excel中的组织-用户组关系（格式：userGroupId_organizationId）
            Set<String> excelRelations = new HashSet<>();
            
            // 统计信息
            int totalRows = 0;
            int validRows = 0;
            int invalidRows = 0;
            List<String> errorMessages = new ArrayList<>();
            
            // 从第二行开始读取（索引1，因为第一行是表头）
            for (int rowIndex = 1; rowIndex <= targetSheet.getLastRowNum(); rowIndex++) {
                Row row = targetSheet.getRow(rowIndex);
                
                if (row == null) {
                    continue;
                }
                
                totalRows++;
                
                // 获取第一列的值（组织编码）
                Cell firstCell = row.getCell(0);
                String orgCode = firstCell != null ? getCellValueAsString(firstCell).trim() : "";
                
                // 获取第三列的值（用户组名称）
                Cell thirdCell = row.getCell(2);
                String groupName = thirdCell != null ? getCellValueAsString(thirdCell).trim() : "";
                // 验证数据完整性
                if (orgCode.isEmpty() || groupName.isEmpty()) {
                    invalidRows++;
                    errorMessages.add(String.format("行%d: 组织编码或用户组名称为空 (组织编码: %s, 用户组: %s)", 
                        rowIndex + 1, orgCode, groupName));
                    continue;
                }
                
                // 查询用户组ID
                Long userGroupId = userGroupService.getIdByName(groupName);
                if (userGroupId == null) {
                    invalidRows++;
                    errorMessages.add(String.format("行%d: 未找到用户组 '%s'", rowIndex + 1, groupName));
                    continue;
                }
                
                // 查询组织ID
                Long organizationId = userGroupService.getOrganizationIdByCode(orgCode);
                if (organizationId == null) {
                    invalidRows++;
                    errorMessages.add(String.format("行%d: 未找到组织编码为 '%s' 的组织", rowIndex + 1, orgCode));
                    continue;
                }
                
                // 添加到关系集合（格式：userGroupId_organizationId）
                String relation = userGroupId + "_" + organizationId;
                excelRelations.add(relation);
                validRows++;
            }
            
            workbook.close();
            
            log.info("Excel读取完成 - 总行数: {}, 有效行数: {}, 无效行数: {}", totalRows, validRows, invalidRows);
            
            // 查询数据库中现有的组织-用户组关系
            log.info("=== 开始查询数据库中的组织-用户组关系 ===");
            Set<String> dbRelations = userGroupService.getAllOrgUserGroupRelations();
            log.info("数据库中现有关系数量: {}", dbRelations.size());
            
            // 对比两个集合
            Set<String> toAdd = new HashSet<>(excelRelations);
            toAdd.removeAll(dbRelations); // Excel中有但数据库中没有的，需要添加
            
            Set<String> existing = new HashSet<>(excelRelations);
            existing.retainAll(dbRelations); // 两个集合都有的，已存在
            
            log.info("=== 关系对比结果 ===");
            log.info("需要添加的关系数量: {}", toAdd.size());
            log.info("已存在的关系数量: {}", existing.size());
            
            // 添加新关系到数据库
            int addedCount = 0;
            if (!toAdd.isEmpty()) {
                log.info("=== 开始添加新的组织-用户组关系 ===");
                for (String relation : toAdd) {
                    try {
                        String[] parts = relation.split("_");
                        Long userGroupId = Long.parseLong(parts[0]);
                        Long organizationId = Long.parseLong(parts[1]);
                        
                        userGroupService.addOrgUserGroupRelation(userGroupId, organizationId);
                        addedCount++;
                        log.info("✓ 成功添加关系: 用户组ID={}, 组织ID={}", userGroupId, organizationId);
                    } catch (Exception e) {
                        log.error("✗ 添加关系失败: {}, 错误: {}", relation, e.getMessage());
                    }
                }
            }

            long duration = System.currentTimeMillis() - startTime;
            
            // 打印详细错误信息
            if (!errorMessages.isEmpty()) {
                log.warn("=== 数据验证错误详情 ===");
                for (String error : errorMessages) {
                    log.warn(error);
                }
            }
            
            String resultMessage = String.format(
                "Excel处理成功！总行数: %d, 有效数据: %d, 无效数据: %d, " +
                "需要添加: %d, 已存在: %d, 实际添加: %d, 耗时: %dms",
                totalRows, validRows, invalidRows,
                toAdd.size(), existing.size(), addedCount, duration
            );
            
            log.info("=== 处理完成 ===");
            log.info(resultMessage);
            
            return CommonResult.success(resultMessage);
            
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            log.error("=== Excel处理失败 ===");
            log.error("错误信息: {}, 耗时: {}ms", e.getMessage(), duration, e);
            return CommonResult.error("500", "Excel处理失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取单元格值（转换为字符串）
     */
    private String getCellValueAsString(Cell cell) {
        if (cell == null) {
            return "";
        }
        
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getDateCellValue().toString();
                } else {
                    // 检查是否为整数
                    double numericValue = cell.getNumericCellValue();
                    if (numericValue == Math.floor(numericValue)) {
                        // 是整数，转换为长整型避免小数点
                        return String.valueOf((long) numericValue);
                    } else {
                        // 是小数，保留原样
                        return String.valueOf(numericValue);
                    }
                }
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                try {
                    return cell.getStringCellValue();
                } catch (IllegalStateException e) {
                    // 公式计算结果是数字类型
                    double numericValue = cell.getNumericCellValue();
                    // 检查是否为整数
                    if (numericValue == Math.floor(numericValue)) {
                        // 是整数，转换为长整型避免小数点
                        return String.valueOf((long) numericValue);
                    } else {
                        // 是小数，保留原样
                        return String.valueOf(numericValue);
                    }
                }
            case BLANK:
                return "";
            default:
                return "";
        }
    }

    @PostMapping("/attr-export")
    @Operation(summary = "导出组织属性模板数据")
    @PermitAll
    public ResponseEntity<byte[]> exportOrgAttributes() throws IOException {
        ClassPathResource templateResource = new ClassPathResource("excel/组织属性模板-导出.xlsx");
        try (InputStream templateInputStream = templateResource.getInputStream();
             XSSFWorkbook workbook = new XSSFWorkbook(templateInputStream)) {

            Sheet sheet = workbook.getSheetAt(0);
            List<SystemManageOrgDO> orgList = organizationMapper.selectList(
                    new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<SystemManageOrgDO>()
                            .eq(SystemManageOrgDO::getDeleted, false)
            );

            int startRow = 1; // 从第2行开始
            for (SystemManageOrgDO org : orgList) {
                Row row = sheet.createRow(startRow++);
                setCellValue(row, 0, org.getCode());
                setCellValue(row, 1, org.getName());
                setCellValue(row, 2, org.getOrgType());
                setCellValue(row, 3, convertApprovalText(org.getIsApprovalLastLvl()));
                setCellValue(row, 4, org.getScriptType());
            }

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            byte[] excelData = outputStream.toByteArray();

            String fileName = "组织属性模板-导出.xlsx";
            String encodedFileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8)
                    .replaceAll("\\+", "%20");

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.set(HttpHeaders.CONTENT_DISPOSITION,
                    "attachment; filename=\"" + encodedFileName + "\"; filename*=UTF-8''" + encodedFileName);

            return ResponseEntity.ok().headers(headers).body(excelData);
        }
    }

    @Operation(summary = "导入组织属性模板数据")
    @PostMapping(value = "/attr-import")
    @PermitAll
    public CommonResult<String> importOrgAttributes(@RequestParam("file") MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return CommonResult.error("500", "文件参数为空，请检查请求格式");
        }

        long startTime = System.currentTimeMillis();
        int totalRows = 0;
        int inScopeRows = 0;
        int updatedRows = 0;
        int invalidRows = 0;
        List<String> errorMessages = new ArrayList<>();

        try (Workbook workbook = WorkbookFactory.create(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);
            List<SystemManageOrgDO> orgList = organizationMapper.selectList(
                    new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<SystemManageOrgDO>()
                            .eq(SystemManageOrgDO::getDeleted, false)
            );
            Map<String, SystemManageOrgDO> orgByCode = orgList.stream()
                    .filter(org -> org.getCode() != null)
                    .collect(Collectors.toMap(SystemManageOrgDO::getCode, org -> org, (a, b) -> a));

            for (int rowIndex = 1; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
                Row row = sheet.getRow(rowIndex);
                if (row == null) {
                    continue;
                }
                totalRows++;

                String orgCode = getCellValueAsString(row.getCell(0)).trim();
                String orgName = getCellValueAsString(row.getCell(1)).trim();
                String orgType = getCellValueAsString(row.getCell(2)).trim();
                String approvalText = getCellValueAsString(row.getCell(3)).trim();
                String scriptType = getCellValueAsString(row.getCell(4)).trim();

                // 有值的才纳入提交范围
                if (approvalText.isEmpty()) {
                    continue;
                }
                inScopeRows++;

                SystemManageOrgDO org = orgByCode.get(orgCode);
                if (org == null) {
                    invalidRows++;
                    errorMessages.add(String.format("行%d: 未找到组织编码 '%s' 对应数据", rowIndex + 1, orgCode));
                    continue;
                }

                if (!orgName.isEmpty() && !Objects.equals(org.getName(), orgName)) {
                    invalidRows++;
                    errorMessages.add(String.format("行%d: 组织编码 '%s' 名称不匹配（模板:%s, 数据库:%s）",
                            rowIndex + 1, orgCode, orgName, org.getName()));
                    continue;
                }

                Boolean isApprovalLastLvl = parseApprovalText(approvalText);
                if (isApprovalLastLvl == null) {
                    invalidRows++;
                    errorMessages.add(String.format("行%d: 是否审批末级仅支持 是/否/1/0，当前值: %s",
                            rowIndex + 1, approvalText));
                    continue;
                }

                SystemManageOrgDO updateDO = new SystemManageOrgDO();
                updateDO.setId(org.getId());
                updateDO.setOrgType(orgType.isEmpty() ? null : orgType);
                updateDO.setIsApprovalLastLvl(isApprovalLastLvl);
                updateDO.setScriptType(scriptType.isEmpty() ? null : scriptType);
                organizationMapper.updateById(updateDO);
                updatedRows++;
            }

            long duration = System.currentTimeMillis() - startTime;
            String resultMessage = String.format(
                    "组织属性导入完成：总行数=%d，纳入提交范围=%d，更新成功=%d，无效=%d，耗时=%dms",
                    totalRows, inScopeRows, updatedRows, invalidRows, duration
            );

            if (!errorMessages.isEmpty()) {
                log.warn("组织属性导入存在无效数据：{}", errorMessages);
            }

            return CommonResult.success(resultMessage);
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            log.error("组织属性导入失败，耗时: {}ms", duration, e);
            return CommonResult.error("500", "组织属性导入失败: " + e.getMessage());
        }
    }

    private String convertApprovalText(Boolean value) {
        if (value == null) {
            return "";
        }
        return value ? "是" : "否";
    }

    private Boolean parseApprovalText(String text) {
        if (text == null || text.trim().isEmpty()) {
            return null;
        }
        String normalized = text.trim();
        if ("是".equals(normalized) || "1".equals(normalized)) {
            return true;
        }
        if ("否".equals(normalized) || "0".equals(normalized)) {
            return false;
        }
        return null;
    }

    @PostMapping("/sync-original-organization-to-business")
    public CommonResult<Void> synManageOrg(){

        DistributedLockUtils.lock(redissonClient,"organization_syn_key",() -> {
            log.info("开始执行项目数据同步任务");

            systemManageOrgService.syncManageOrgToBusiness();

            // 执行原始组织数据同步
            log.info("项目数据同步任务执行完成");
        });
        return CommonResult.success();
    }





} 