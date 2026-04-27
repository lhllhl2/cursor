package com.jasolar.mis.module.system.controller.admin.usergroup;

import com.jasolar.mis.framework.common.pojo.CommonResult;
import com.jasolar.mis.framework.common.pojo.PageResult;
import com.jasolar.mis.framework.common.pojo.PrimaryParam;
import com.jasolar.mis.module.system.controller.admin.usergroup.resp.*;
import com.jasolar.mis.module.system.controller.admin.usergroup.vo.*;
import com.jasolar.mis.module.system.domain.admin.user.SystemUserDo;
import com.jasolar.mis.module.system.domain.admin.usergroup.SystemUserGroupDo;
import com.jasolar.mis.module.system.domain.admin.usergroup.SystemUserGroupRDo;
import com.jasolar.mis.module.system.mapper.admin.user.SystemUserMapper;
import com.jasolar.mis.module.system.mapper.admin.usergroup.SystemUserGroupRMapper;
import com.jasolar.mis.module.system.mapper.admin.usergroup.UserGroupMapper;
import com.jasolar.mis.module.system.service.admin.user.SystemUserService;
import com.jasolar.mis.module.system.service.admin.usergroup.UserGroupService;
import com.jasolar.mis.module.system.service.admin.usergroup.UserGroupSyncService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.lang.management.ManagementFactory;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Description:
 * Author : Zhou Hai
 * Date : 23/07/2025 10:15
 * Version : 1.0
 */

@Tag(name = "管理后台 - 用户组")
@RestController
@RequestMapping("/system/userGroup")
@Slf4j
public class UserGroupController {

    /** Oracle IN 列表最大数量，超过会报 ORA-01795 */
    private static final int ORACLE_IN_MAX = 1000;

    @Autowired
    private UserGroupService userGroupService;

    @Resource
    private UserGroupSyncService userGroupSyncService;

    @Autowired
    private SystemUserService systemUserService;

    @Resource
    private SystemUserGroupRMapper systemUserGroupRMapper;

    @Resource
    private UserGroupMapper userGroupMapper;

    @Resource
    private SystemUserMapper systemUserMapper;

    @Autowired
    private ThreadPoolTaskExecutor taskExecutor;

    @Operation(summary = "1.用户组分页查询")
    @PostMapping(value = "/getPage")
    public CommonResult<PageResult<SearchListResp>> getPage(@RequestBody SearchListVo searchListVo){
        PageResult<SearchListResp> page = userGroupService.getList(searchListVo);
        return CommonResult.success(page);
    }


    @Operation(summary = "2.用户组List查询")
    @PostMapping(value = "/getList")
    public CommonResult<List<SearchSimpleListResp>> getList(@RequestBody SearchSimpleListVo searchSimpleListVo){
        List<SearchSimpleListResp> list = userGroupService.getSimpleList(searchSimpleListVo);
        return CommonResult.success(list);
    }

    @Operation(summary = "3.用户组新增")
    @PostMapping(value = "/addUserGroup")
    public CommonResult<Void> addUserGroup(@RequestBody @Validated UserGroupAddVo userGroupAddVo){
        userGroupService.addUserGroup(userGroupAddVo);

        return CommonResult.success();
    }

    @Operation(summary = "4.用户组删除")
    @PostMapping(value = "/del")
    public CommonResult<Void> del(@RequestBody PrimaryParam primaryParam){
        userGroupService.del(primaryParam);
        return CommonResult.success();
    }


    @Operation(summary = "5.通过角色id查询用户组")
    @PostMapping(value = "/searchRoleByUserGroup")
    public CommonResult<List<UserGroupRoleResp>> searchRoleByUserGroup(@RequestBody SearchRoleByUserGroupVo searchRoleByUserGroupVo){
        List<UserGroupRoleResp> roleRespList = userGroupService.searchRoleByUserGroup(searchRoleByUserGroupVo);
        return CommonResult.success(roleRespList);
    }



    @Operation(summary = "6.通过用户组id查询对应用户【暂时不用】")
    @PostMapping(value = "/searchGroupUser")
    public CommonResult<PageResult<GroupUserResp>> searchGroupUser(@RequestBody SearchPrimaryPage searchPrimaryPage){
        PageResult<GroupUserResp> pageResult = userGroupService.searchGroupUser(searchPrimaryPage);
        return CommonResult.success(pageResult);
    }

    @Operation(summary = "7.通过用户组id查询对应用户List")
    @PostMapping(value = "/searchGroupUserList")
    public CommonResult<List<GroupUserResp>> searchGroupList(@RequestBody PrimaryParam primaryParam){
        long startTime = System.currentTimeMillis();
        String threadName = Thread.currentThread().getName();
        log.info("=== 开始查询 searchGroupUserList ===");
        log.info("线程: {}, groupId: {}, 时间: {}", threadName, primaryParam.getId(), startTime);
        
        // 连接池状态监控已移除，避免JMX访问失败警告
        
        try {
            List<GroupUserResp> respList = userGroupService.searchGroupList(primaryParam);
            long duration = System.currentTimeMillis() - startTime;
            log.info("=== 查询完成 ===");
            log.info("线程: {}, 耗时: {}ms, 结果数量: {}", threadName, duration, respList.size());
            return CommonResult.success(respList);
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            log.error("=== 查询失败 ===");
            log.error("线程: {}, 耗时: {}ms, 错误: {}", threadName, duration, e.getMessage(), e);
            throw e;
        }
    }


    @Operation(summary = "8.用户合用户组的关联关系 - 新增或修改")
    @PostMapping(value = "/groupUserRSave")
    public CommonResult<Void> groupUserSave(@RequestBody GroupUserSaveVo groupUserSaveVo){
        userGroupService.groupUserSave(groupUserSaveVo);
        return CommonResult.success();
    }



    @Operation(summary = "9.根据组织ID查询用户组ID列表")
    @GetMapping(value = "/getGroupIdsByOrgId")
    public CommonResult<List<Long>> getGroupIdsByOrgId(@RequestParam("orgId") Long orgId){
        List<Long> groupIds = userGroupService.getGroupIdsByOrgId(orgId);
        return CommonResult.success(groupIds);
    }

    private String uploadAndReadExcelInternal(org.springframework.web.multipart.MultipartFile file) {
        long startTime = System.currentTimeMillis();
        log.info("=== 开始处理Excel文件 ===");
        
        // 添加详细的调试信息
        log.info("请求参数检查:");
        log.info("- file参数是否为null: {}", file == null);
        if (file != null) {
            log.info("- 文件名: {}", file.getOriginalFilename());
            log.info("- 文件大小: {} bytes", file.getSize());
            log.info("- 文件是否为空: {}", file.isEmpty());
            log.info("- Content-Type: {}", file.getContentType());
        } else {
            log.error("file参数为null，这可能是multipart请求解析失败");
            return "文件参数为空，请检查请求格式";
        }
        
        try {
            // 读取Excel文件
            Workbook workbook = WorkbookFactory.create(file.getInputStream());
            
            // 查找名为"用户组数据"的sheet页
            Sheet targetSheet = null;
            String targetSheetName = "用户组数据";
            
            for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
                Sheet sheet = workbook.getSheetAt(i);
                if (targetSheetName.equals(sheet.getSheetName())) {
                    targetSheet = sheet;
                    log.info("找到目标Sheet页: {}", targetSheetName);
                    break;
                }
            }
            
            if (targetSheet == null) {
                log.error("未找到名为 '{}' 的Sheet页", targetSheetName);
                return "未找到名为 '" + targetSheetName + "' 的Sheet页";
            }
            
            // 统计与分类：按第一列类型代码（1/2/3/4）归集用户组名称
            int totalRows = 0;
            Set<String> userGroupNamesType1 = new HashSet<>();
            Set<String> userGroupNamesType2 = new HashSet<>();
            Set<String> userGroupNamesType3 = new HashSet<>();
            Set<String> userGroupNamesType4 = new HashSet<>();
            
            // 从第二行开始读取（索引1，因为第一行是表头）
            for (int rowIndex = 1; rowIndex <= targetSheet.getLastRowNum(); rowIndex++) {
                Row row = targetSheet.getRow(rowIndex);
                
                if (row == null) {
                    continue; // 跳过空行
                }
                
                totalRows++;
                
                // 第一列：类型代码（1/2/3/4）
                Cell firstCell = row.getCell(0);
                String typeCode = firstCell != null ? getCellValueAsString(firstCell).trim() : "";
                // 第三列：用户组名称（第四列为人员工号）
                Cell thirdCell = row.getCell(2);
                String groupName = thirdCell != null ? getCellValueAsString(thirdCell).trim() : "";

                if (typeCode.isEmpty() || groupName.isEmpty()) {
                    continue;
                }

                switch (typeCode) {
                    case "1":
                        userGroupNamesType1.add(groupName);
                        break;
                    case "2":
                        userGroupNamesType2.add(groupName);
                        break;
                    case "3":
                        userGroupNamesType3.add(groupName);
                        break;
                    case "4":
                        userGroupNamesType4.add(groupName);
                        break;
                    default:
                        // 非法类型代码，忽略
                        break;
                }
            }
            
            // 关闭workbook
            workbook.close();
            
            // ========== 处理类型1的用户组 ==========
            log.info("=== 开始查询数据库（类型1） ===");
            log.info("从Excel中提取的类型1用户组名称总数: {}", userGroupNamesType1.size());
            
            List<String> existingGroupsType1 = new java.util.ArrayList<>();
            List<String> nonExistingGroupsType1 = new java.util.ArrayList<>();
            List<String> newlyCreatedGroupsType1 = new java.util.ArrayList<>();
            
            for (String name : userGroupNamesType1) {
                boolean exists = userGroupService.existsByName(name);
                if (exists) {
                    existingGroupsType1.add(name);
                    log.info("✓ [类型1] 已存在: {}", name);
                } else {
                    nonExistingGroupsType1.add(name);
                    log.info("✗ [类型1] 不存在: {}", name);
                }
            }
            
            // 新增不存在的用户组（类型1）
            if (!nonExistingGroupsType1.isEmpty()) {
                log.info("=== 开始新增不存在的用户组（类型1） ===");
                for (String name : nonExistingGroupsType1) {
                    try {
                        userGroupService.createUserGroupFromExcel(name, "1");
                        newlyCreatedGroupsType1.add(name);
                        log.info("✓ [类型1] 成功新增用户组: {}", name);
                    } catch (Exception e) {
                        log.error("✗ [类型1] 新增用户组失败: {}, 错误: {}", name, e.getMessage());
                    }
                }
            }
            
            // ========== 处理类型2的用户组（报表类型） ==========
            log.info("=== 开始查询数据库（类型2-报表类型） ===");
            log.info("从Excel中提取的类型2用户组名称总数: {}", userGroupNamesType2.size());
            
            List<String> existingGroupsType2 = new java.util.ArrayList<>();
            List<String> nonExistingGroupsType2 = new java.util.ArrayList<>();
            List<String> newlyCreatedGroupsType2 = new java.util.ArrayList<>();
            
            for (String name : userGroupNamesType2) {
                boolean exists = userGroupService.existsByName(name);
                if (exists) {
                    existingGroupsType2.add(name);
                    log.info("✓ [类型2] 已存在: {}", name);
                } else {
                    nonExistingGroupsType2.add(name);
                    log.info("✗ [类型2] 不存在: {}", name);
                }
            }
            
            // 新增不存在的用户组（类型2-报表类型）
            if (!nonExistingGroupsType2.isEmpty()) {
                log.info("=== 开始新增不存在的用户组（类型2-报表类型） ===");
                for (String name : nonExistingGroupsType2) {
                    try {
                        userGroupService.createUserGroupFromExcel(name, "2");
                        newlyCreatedGroupsType2.add(name);
                        log.info("✓ [类型2] 成功新增用户组: {}", name);
                    } catch (Exception e) {
                        log.error("✗ [类型2] 新增用户组失败: {}, 错误: {}", name, e.getMessage());
                    }
                }
            }

            // ========== 处理类型3的用户组 ==========
            log.info("=== 开始查询数据库（类型3） ===");
            log.info("从Excel中提取的类型3用户组名称总数: {}", userGroupNamesType3.size());

            List<String> existingGroupsType3 = new java.util.ArrayList<>();
            List<String> nonExistingGroupsType3 = new java.util.ArrayList<>();
            List<String> newlyCreatedGroupsType3 = new java.util.ArrayList<>();

            for (String name : userGroupNamesType3) {
                boolean exists = userGroupService.existsByName(name);
                if (exists) {
                    existingGroupsType3.add(name);
                    log.info("✓ [类型3] 已存在: {}", name);
                } else {
                    nonExistingGroupsType3.add(name);
                    log.info("✗ [类型3] 不存在: {}", name);
                }
            }

            if (!nonExistingGroupsType3.isEmpty()) {
                log.info("=== 开始新增不存在的用户组（类型3） ===");
                for (String name : nonExistingGroupsType3) {
                    try {
                        userGroupService.createUserGroupFromExcel(name, "3");
                        newlyCreatedGroupsType3.add(name);
                        log.info("✓ [类型3] 成功新增用户组: {}", name);
                    } catch (Exception e) {
                        log.error("✗ [类型3] 新增用户组失败: {}, 错误: {}", name, e.getMessage());
                    }
                }
            }

            // ========== 处理类型4的用户组 ==========
            log.info("=== 开始查询数据库（类型4） ===");
            log.info("从Excel中提取的类型4用户组名称总数: {}", userGroupNamesType4.size());

            List<String> existingGroupsType4 = new java.util.ArrayList<>();
            List<String> nonExistingGroupsType4 = new java.util.ArrayList<>();
            List<String> newlyCreatedGroupsType4 = new java.util.ArrayList<>();

            for (String name : userGroupNamesType4) {
                boolean exists = userGroupService.existsByName(name);
                if (exists) {
                    existingGroupsType4.add(name);
                    log.info("✓ [类型4] 已存在: {}", name);
                } else {
                    nonExistingGroupsType4.add(name);
                    log.info("✗ [类型4] 不存在: {}", name);
                }
            }

            if (!nonExistingGroupsType4.isEmpty()) {
                log.info("=== 开始新增不存在的用户组（类型4） ===");
                for (String name : nonExistingGroupsType4) {
                    try {
                        userGroupService.createUserGroupFromExcel(name, "4");
                        newlyCreatedGroupsType4.add(name);
                        log.info("✓ [类型4] 成功新增用户组: {}", name);
                    } catch (Exception e) {
                        log.error("✗ [类型4] 新增用户组失败: {}, 错误: {}", name, e.getMessage());
                    }
                }
            }
            
            long duration = System.currentTimeMillis() - startTime;
            
            // 打印统计信息
            log.info("=== Excel处理完成 ===");
            log.info("Sheet页名称: {}", targetSheetName);
            log.info("总数据行数（不含表头）: {}", totalRows);
            log.info("类型1记录数: {}", userGroupNamesType1.size());
            log.info("类型2记录数: {}", userGroupNamesType2.size());
            log.info("类型3记录数: {}", userGroupNamesType3.size());
            log.info("类型4记录数: {}", userGroupNamesType4.size());
            log.info("处理耗时: {}ms", duration);
            
            // 不再打印“开通”记录详情
            
            // 打印数据库查询结果统计 - 类型1
            log.info("=== 数据库查询结果统计（类型1） ===");
            log.info("[类型1] 已存在的用户组数量: {}", existingGroupsType1.size());
            log.info("[类型1] 原本不存在的用户组数量: {}", nonExistingGroupsType1.size());
            log.info("[类型1] 成功新增的用户组数量: {}", newlyCreatedGroupsType1.size());
            
            if (!existingGroupsType1.isEmpty()) {
                log.info("=== [类型1] 已存在的用户组列表 ===");
                for (String name : existingGroupsType1) {
                    log.info("  - {}", name);
                }
            }
            
            if (!nonExistingGroupsType1.isEmpty()) {
                log.info("=== [类型1] 原本不存在的用户组列表 ===");
                for (String name : nonExistingGroupsType1) {
                    log.info("  - {}", name);
                }
            }
            
            if (!newlyCreatedGroupsType1.isEmpty()) {
                log.info("=== [类型1] 成功新增的用户组列表 ===");
                for (String name : newlyCreatedGroupsType1) {
                    log.info("  - {}", name);
                }
            }
            
            // 打印数据库查询结果统计 - 类型2
            log.info("=== 数据库查询结果统计（类型2-报表类型） ===");
            log.info("[类型2] 已存在的用户组数量: {}", existingGroupsType2.size());
            log.info("[类型2] 原本不存在的用户组数量: {}", nonExistingGroupsType2.size());
            log.info("[类型2] 成功新增的用户组数量: {}", newlyCreatedGroupsType2.size());

            if (!existingGroupsType2.isEmpty()) {
                log.info("=== [类型2] 已存在的用户组列表 ===");
                for (String name : existingGroupsType2) {
                    log.info("  - {}", name);
                }
            }

            if (!nonExistingGroupsType2.isEmpty()) {
                log.info("=== [类型2] 原本不存在的用户组列表 ===");
                for (String name : nonExistingGroupsType2) {
                    log.info("  - {}", name);
                }
            }

            if (!newlyCreatedGroupsType2.isEmpty()) {
                log.info("=== [类型2] 成功新增的用户组列表 ===");
                for (String name : newlyCreatedGroupsType2) {
                    log.info("  - {}", name);
                }
            }

            // 打印数据库查询结果统计 - 类型3
            log.info("=== 数据库查询结果统计（类型3） ===");
            log.info("[类型3] 已存在的用户组数量: {}", existingGroupsType3.size());
            log.info("[类型3] 原本不存在的用户组数量: {}", nonExistingGroupsType3.size());
            log.info("[类型3] 成功新增的用户组数量: {}", newlyCreatedGroupsType3.size());
            
            if (!existingGroupsType3.isEmpty()) {
                log.info("=== [类型3] 已存在的用户组列表 ===");
                for (String name : existingGroupsType3) {
                    log.info("  - {}", name);
                }
            }
            
            if (!nonExistingGroupsType3.isEmpty()) {
                log.info("=== [类型3] 原本不存在的用户组列表 ===");
                for (String name : nonExistingGroupsType3) {
                    log.info("  - {}", name);
                }
            }
            
            if (!newlyCreatedGroupsType3.isEmpty()) {
                log.info("=== [类型3] 成功新增的用户组列表 ===");
                for (String name : newlyCreatedGroupsType3) {
                    log.info("  - {}", name);
                }
            }

            // 打印数据库查询结果统计 - 类型4
            log.info("=== 数据库查询结果统计（类型4） ===");
            log.info("[类型4] 已存在的用户组数量: {}", existingGroupsType4.size());
            log.info("[类型4] 原本不存在的用户组数量: {}", nonExistingGroupsType4.size());
            log.info("[类型4] 成功新增的用户组数量: {}", newlyCreatedGroupsType4.size());

            if (!existingGroupsType4.isEmpty()) {
                log.info("=== [类型4] 已存在的用户组列表 ===");
                for (String name : existingGroupsType4) {
                    log.info("  - {}", name);
                }
            }

            if (!nonExistingGroupsType4.isEmpty()) {
                log.info("=== [类型4] 原本不存在的用户组列表 ===");
                for (String name : nonExistingGroupsType4) {
                    log.info("  - {}", name);
                }
            }

            if (!newlyCreatedGroupsType4.isEmpty()) {
                log.info("=== [类型4] 成功新增的用户组列表 ===");
                for (String name : newlyCreatedGroupsType4) {
                    log.info("  - {}", name);
                }
            }
            
            String resultMessage = String.format(
                "Excel处理成功！Sheet: %s, 总行: %d; " +
                "类型1: %d (已存在: %d, 新增: %d); " +
                "类型2: %d (已存在: %d, 新增: %d); " +
                "类型3: %d (已存在: %d, 新增: %d); " +
                "类型4: %d (已存在: %d, 新增: %d); 耗时: %dms",
                targetSheetName, totalRows,
                userGroupNamesType1.size(), existingGroupsType1.size(), newlyCreatedGroupsType1.size(),
                userGroupNamesType2.size(), existingGroupsType2.size(), newlyCreatedGroupsType2.size(),
                userGroupNamesType3.size(), existingGroupsType3.size(), newlyCreatedGroupsType3.size(),
                userGroupNamesType4.size(), existingGroupsType4.size(), newlyCreatedGroupsType4.size(),
                duration
            );
            
            return resultMessage;
            
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            log.error("=== Excel处理失败 ===");
            log.error("错误信息: {}, 耗时: {}ms", e.getMessage(), duration, e);
            return "Excel处理失败: " + e.getMessage();
        }
    }
    
    private String syncUserGroupRelationInternal(org.springframework.web.multipart.MultipartFile file) {
        long startTime = System.currentTimeMillis();
        log.info("=== 开始处理Excel文件（同步用户组与用户关系） ===");
        
        if (file == null || file.isEmpty()) {
            log.error("文件参数为空");
            return "文件参数为空，请检查请求格式";
        }
        
        log.info("文件名: {}, 文件大小: {} bytes", file.getOriginalFilename(), file.getSize());
        
        try {
            // 读取Excel文件
            Workbook workbook = WorkbookFactory.create(file.getInputStream());
            
            // 查找名为"用户组数据"的sheet页
            Sheet targetSheet = null;
            String targetSheetName = "用户组数据";
            
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
                return "未找到名为 '" + targetSheetName + "' 的Sheet页";
            }
            
            // 用于存储Excel中的用户组-用户关系（格式：groupId_userId）
            Set<String> excelRelations = new HashSet<>();
            
            // 统计信息
            int totalRows = 0;
            int validRows = 0;
            int invalidRows = 0;
            List<String> errorMessages = new java.util.ArrayList<>();
            
            // 从第二行开始读取（索引1，因为第一行是表头）
            for (int rowIndex = 1; rowIndex <= targetSheet.getLastRowNum(); rowIndex++) {
                Row row = targetSheet.getRow(rowIndex);
                
                if (row == null) {
                    continue;
                }
                
                totalRows++;
                
                // 获取第一列的值（类型）
                Cell firstCell = row.getCell(0);
                String type = firstCell != null ? getCellValueAsString(firstCell).trim() : "";
                
                // 获取第三列的值（用户组名称）
                Cell thirdCell = row.getCell(2);
                String groupName = thirdCell != null ? getCellValueAsString(thirdCell).trim() : "";
                
                // 获取第四列的值（工号）
                Cell fourthCell = row.getCell(3);
                String userNo = fourthCell != null ? getCellValueAsString(fourthCell).trim() : "";
                
                // 验证数据完整性
                if (groupName.isEmpty() || userNo.isEmpty()) {
                    invalidRows++;
                    errorMessages.add(String.format("行%d: 用户组名称或工号为空 (用户组: %s, 工号: %s)", 
                        rowIndex + 1, groupName, userNo));
                    continue;
                }
                
                // 查询用户组ID
                Long groupId = userGroupService.getIdByName(groupName);
                if (groupId == null) {
                    invalidRows++;
                    errorMessages.add(String.format("行%d: 未找到用户组 '%s'", rowIndex + 1, groupName));
                    continue;
                }
                
                // 查询用户ID（通过SystemUserService）
                SystemUserDo user = systemUserService.getByUserName(userNo);
                if (user == null) {
                    invalidRows++;
                    errorMessages.add(String.format("行%d: 未找到工号为 '%s' 的用户", rowIndex + 1, userNo));
                    continue;
                }
                Long userId = user.getId();
                
                // 添加到关系集合（格式：groupId_userId）
                String relation = groupId + "_" + userId;
                excelRelations.add(relation);
                validRows++;
            }
            
            workbook.close();
            
            log.info("Excel读取完成 - 总行数: {}, 有效行数: {}, 无效行数: {}", totalRows, validRows, invalidRows);
            
            // 查询数据库中现有的用户组-用户关系
            log.info("=== 开始查询数据库中的用户组-用户关系 ===");
            Set<String> dbRelations = userGroupService.getAllUserGroupRelations();
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
                log.info("=== 开始添加新的用户组-用户关系 ===");
                for (String relation : toAdd) {
                    try {
                        String[] parts = relation.split("_");
                        Long groupId = Long.parseLong(parts[0]);
                        Long userId = Long.parseLong(parts[1]);
                        
                        userGroupService.addUserGroupRelation(groupId, userId);
                        addedCount++;
                        log.info("✓ 成功添加关系: 用户组ID={}, 用户ID={}", groupId, userId);
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
            
            return resultMessage;
            
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            log.error("=== Excel处理失败 ===");
            log.error("错误信息: {}, 耗时: {}ms", e.getMessage(), duration, e);
            return "Excel处理失败: " + e.getMessage();
        }
    }
    
    // 该方法已迁移到 OrgController 中，请使用 /system/org/syncOrgUserGroupRelation
    /*
    @Operation(summary = "12.上传Excel并同步组织与用户组关系")
    @PostMapping(value = "/syncOrgUserGroupRelation")
    public CommonResult<String> syncOrgUserGroupRelation(@RequestParam("file") org.springframework.web.multipart.MultipartFile file) {
        long startTime = System.currentTimeMillis();
        log.info("=== 开始处理Excel文件（同步组织与用户组关系） ===");
        
        if (file == null || file.isEmpty()) {
            log.error("文件参数为空");
            return CommonResult.error("500", "文件参数为空，请检查请求格式");
        }
        
        log.info("文件名: {}, 文件大小: {} bytes", file.getOriginalFilename(), file.getSize());
        
        try {
            // 读取Excel文件
            org.apache.poi.ss.usermodel.Workbook workbook = org.apache.poi.ss.usermodel.WorkbookFactory.create(file.getInputStream());
            
            // 查找名为"组织&角色组"的sheet页
            org.apache.poi.ss.usermodel.Sheet targetSheet = null;
            String targetSheetName = "组织&角色组";
            
            for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
                org.apache.poi.ss.usermodel.Sheet sheet = workbook.getSheetAt(i);
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
            java.util.Set<String> excelRelations = new java.util.HashSet<>();
            
            // 统计信息
            int totalRows = 0;
            int validRows = 0;
            int invalidRows = 0;
            java.util.List<String> errorMessages = new java.util.ArrayList<>();
            
            // 从第二行开始读取（索引1，因为第一行是表头）
            for (int rowIndex = 1; rowIndex <= targetSheet.getLastRowNum(); rowIndex++) {
                org.apache.poi.ss.usermodel.Row row = targetSheet.getRow(rowIndex);
                
                if (row == null) {
                    continue;
                }
                
                totalRows++;
                
                // 获取第一列的值（组织编码）
                org.apache.poi.ss.usermodel.Cell firstCell = row.getCell(0);
                String orgCode = firstCell != null ? getCellValueAsString(firstCell).trim() : "";
                
                // 获取第三列的值（用户组名称）
                org.apache.poi.ss.usermodel.Cell thirdCell = row.getCell(3);
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
            java.util.Set<String> dbRelations = userGroupService.getAllOrgUserGroupRelations();
            log.info("数据库中现有关系数量: {}", dbRelations.size());
            
            // 对比两个集合
            java.util.Set<String> toAdd = new java.util.HashSet<>(excelRelations);
            toAdd.removeAll(dbRelations); // Excel中有但数据库中没有的，需要添加
            
            java.util.Set<String> existing = new java.util.HashSet<>(excelRelations);
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
    */
    
    // 该方法已迁移到 FrReportController 中，请使用 /fr/report/syncReportUserGroupRelation
    /*
    @Operation(summary = "13.上传Excel并同步报表与用户组关系")
    @PostMapping(value = "/syncReportUserGroupRelation")
    public CommonResult<String> syncReportUserGroupRelation(@RequestParam("file") org.springframework.web.multipart.MultipartFile file) {
        long startTime = System.currentTimeMillis();
        log.info("=== 开始处理Excel文件（同步报表与用户组关系） ===");
        
        if (file == null || file.isEmpty()) {
            log.error("文件参数为空");
            return CommonResult.error("500", "文件参数为空，请检查请求格式");
        }
        
        log.info("文件名: {}, 文件大小: {} bytes", file.getOriginalFilename(), file.getSize());
        
        try {
            // 读取Excel文件
            org.apache.poi.ss.usermodel.Workbook workbook = org.apache.poi.ss.usermodel.WorkbookFactory.create(file.getInputStream());
            
            // 查找名为"报表&角色组"的sheet页
            org.apache.poi.ss.usermodel.Sheet targetSheet = null;
            String targetSheetName = "报表&角色组";
            
            for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
                org.apache.poi.ss.usermodel.Sheet sheet = workbook.getSheetAt(i);
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
            
            // 用于存储Excel中的报表-用户组关系（格式：userGroupId_reportId）
            java.util.Set<String> excelRelations = new java.util.HashSet<>();
            
            // 统计信息
            int totalRows = 0;
            int validRows = 0;
            int invalidRows = 0;
            java.util.List<String> errorMessages = new java.util.ArrayList<>();
            
            // 从第二行开始读取（索引1，因为第一行是表头）
            for (int rowIndex = 1; rowIndex <= targetSheet.getLastRowNum(); rowIndex++) {
                org.apache.poi.ss.usermodel.Row row = targetSheet.getRow(rowIndex);
                
                if (row == null) {
                    continue;
                }
                
                totalRows++;
                
                // 获取第一列的值（表单名称）
                org.apache.poi.ss.usermodel.Cell firstCell = row.getCell(0);
                String reportName = firstCell != null ? getCellValueAsString(firstCell).trim() : "";
                
                // 获取第二列的值（用户组名称）
                org.apache.poi.ss.usermodel.Cell secondCell = row.getCell(1);
                String groupName = secondCell != null ? getCellValueAsString(secondCell).trim() : "";
                
                // 验证数据完整性
                if (reportName.isEmpty() || groupName.isEmpty()) {
                    invalidRows++;
                    errorMessages.add(String.format("行%d: 表单名称或用户组名称为空 (表单: %s, 用户组: %s)", 
                        rowIndex + 1, reportName, groupName));
                    continue;
                }
                
                // 查询用户组ID
                Long userGroupId = userGroupService.getIdByName(groupName);
                if (userGroupId == null) {
                    invalidRows++;
                    errorMessages.add(String.format("行%d: 未找到用户组 '%s'", rowIndex + 1, groupName));
                    continue;
                }
                
                // 查询报表ID（通过报表名称）
                String reportId = userGroupService.getReportIdByName(reportName);
                if (reportId == null) {
                    invalidRows++;
                    errorMessages.add(String.format("行%d: 未找到报表 '%s'", rowIndex + 1, reportName));
                    continue;
                }
                
                // 添加到关系集合（格式：userGroupId_reportId）
                String relation = userGroupId + "_" + reportId;
                excelRelations.add(relation);
                validRows++;
            }
            
            workbook.close();
            
            log.info("Excel读取完成 - 总行数: {}, 有效行数: {}, 无效行数: {}", totalRows, validRows, invalidRows);
            
            // 查询数据库中现有的报表-用户组关系
            log.info("=== 开始查询数据库中的报表-用户组关系 ===");
            java.util.Set<String> dbRelations = userGroupService.getAllReportUserGroupRelations();
            log.info("数据库中现有关系数量: {}", dbRelations.size());
            
            // 对比两个集合
            java.util.Set<String> toAdd = new java.util.HashSet<>(excelRelations);
            toAdd.removeAll(dbRelations); // Excel中有但数据库中没有的，需要添加
            
            java.util.Set<String> existing = new java.util.HashSet<>(excelRelations);
            existing.retainAll(dbRelations); // 两个集合都有的，已存在
            
            log.info("=== 关系对比结果 ===");
            log.info("需要添加的关系数量: {}", toAdd.size());
            log.info("已存在的关系数量: {}", existing.size());
            
            // 添加新关系到数据库
            int addedCount = 0;
            if (!toAdd.isEmpty()) {
                log.info("=== 开始添加新的报表-用户组关系 ===");
                for (String relation : toAdd) {
                    try {
                        String[] parts = relation.split("_");
                        Long userGroupId = Long.parseLong(parts[0]);
                        String reportId = parts[1];
                        
                        userGroupService.addReportUserGroupRelation(userGroupId, reportId);
                        addedCount++;
                        log.info("✓ 成功添加关系: 用户组ID={}, 报表ID={}", userGroupId, reportId);
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
    */

    @Operation(summary = "14.导出【用户组&用户】模板并回填当前数据")
    @GetMapping(value = "/batchExport", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public ResponseEntity<byte[]> batchExport() {
        try {
            final String DELIM = "§"; // 作为字段分隔符，避免名称中的下划线干扰
            // 1. 读取模板
            String templatePath = "excel/用户组&用户导入模板(用户组管理界面).xlsx";
            InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(templatePath);
            if (is == null) {
                return ResponseEntity.internalServerError().body(("模板不存在: " + templatePath).getBytes());
            }
            Workbook workbook = WorkbookFactory.create(is);
            Sheet sheet = workbook.getSheetAt(0);

            // 2. 查询 SYSTEM_USER_GROUP_R（deleted=0）并组装 currentUserGroupRMap、收集 groupIdSet / userIdSet
            List<SystemUserGroupRDo> relations =
                    systemUserGroupRMapper.selectList(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<SystemUserGroupRDo>()
                            .eq(SystemUserGroupRDo::getDeleted, 0));

            Map<Long, String> currentUserGroupRMap = new HashMap<>();
            Set<Long> groupIdSet = new HashSet<>();
            Set<Long> userIdSet = new HashSet<>();
            for (SystemUserGroupRDo r : relations) {
                if (r.getId() == null || r.getGroupId() == null || r.getUserId() == null) continue;
                currentUserGroupRMap.put(r.getId(), r.getGroupId() + DELIM + r.getUserId());
                groupIdSet.add(r.getGroupId());
                userIdSet.add(r.getUserId());
            }

            // 3. 查询 SYSTEM_USER_GROUP（with relations）并映射成 currentUserGroupMapWithRelations（Oracle IN 最多 1000 项，需分批）
            List<SystemUserGroupDo> groupsWith = groupIdSet.isEmpty() ? java.util.Collections.emptyList() : selectBatchIdsInChunks(groupIdSet, userGroupMapper::selectBatchIds);

            Map<Long, String> currentUserGroupMapWithRelations = new HashMap<>();
            for (SystemUserGroupDo g : groupsWith) {
                if (g == null || Boolean.TRUE.equals(g.getDeleted())) continue;
                String type = g.getType();
                String typeName;
                if ("1".equals(type)) typeName = "菜单";
                else if ("2".equals(type)) typeName = "项目";
                else if ("3".equals(type)) typeName = "组织";
                else if ("4".equals(type)) typeName = "数据";
                else typeName = "";
                currentUserGroupMapWithRelations.put(g.getId(), (type == null ? "" : type) + DELIM + typeName + DELIM + (g.getName() == null ? "" : g.getName()));
            }

            // 4. 查询 SYSTEM_USER_GROUP 全量（deleted=0） -> currentUserGroupMap；再得出 withoutRelations
            List<SystemUserGroupDo> allGroups =
                    userGroupMapper.selectList(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<SystemUserGroupDo>()
                            .eq(SystemUserGroupDo::getDeleted, 0));
            Map<Long, String> currentUserGroupMap = new HashMap<>();
            for (SystemUserGroupDo g : allGroups) {
                String type = g.getType();
                String typeName;
                if ("1".equals(type)) typeName = "菜单";
                else if ("2".equals(type)) typeName = "项目";
                else if ("3".equals(type)) typeName = "组织";
                else if ("4".equals(type)) typeName = "数据";
                else typeName = "";
                currentUserGroupMap.put(g.getId(), (type == null ? "" : type) + DELIM + typeName + DELIM + (g.getName() == null ? "" : g.getName()));
            }
            Map<Long, String> currentUserGroupMapWithoutRelations = currentUserGroupMap.entrySet().stream()
                    .filter(e -> !currentUserGroupMapWithRelations.containsKey(e.getKey()))
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

            // 5. 查询 SYSTEM_USER（with relations） -> currentUserMapWithRelations（Oracle IN 最多 1000 项，需分批）
            List<SystemUserDo> usersWith = userIdSet.isEmpty() ? java.util.Collections.emptyList() : selectBatchIdsInChunks(userIdSet, systemUserMapper::selectBatchIds);
            Map<Long, String> currentUserMapWithRelations = new HashMap<>();
            for (SystemUserDo u : usersWith) {
                if (u == null || Boolean.TRUE.equals(u.getDeleted())) continue;
                currentUserMapWithRelations.put(u.getId(),
                        (u.getUserName() == null ? "" : u.getUserName()) + DELIM + (u.getDisplayName() == null ? "" : u.getDisplayName()));
            }

            // 6. 用映射替换 currentUserGroupRMap 的 value 为五段字段
            for (Map.Entry<Long, String> e : currentUserGroupRMap.entrySet()) {
                String[] parts = e.getValue().split(java.util.regex.Pattern.quote(DELIM));
                if (parts.length != 2) continue;
                Long gid = Long.parseLong(parts[0]);
                Long uid = Long.parseLong(parts[1]);
                String vGroup = currentUserGroupMapWithRelations.get(gid);
                if (vGroup == null) vGroup = "-" + DELIM + "-" + DELIM + "-"; // 占位三段
                String vUser = currentUserMapWithRelations.get(uid);
                if (vUser == null) vUser = "-" + DELIM + "-"; // 占位两段
                e.setValue(vGroup + DELIM + vUser); // 共五段
            }

            // 7. 写入 Excel：先 relations（五列），再无关联用户的组（前三列）
            int rowIndex = 1; // 第二行开始
            for (String val : currentUserGroupRMap.values()) {
                Row row = sheet.getRow(rowIndex);
                if (row == null) row = sheet.createRow(rowIndex);
                String[] cols = val.split(java.util.regex.Pattern.quote(DELIM));
                for (int i = 0; i < Math.min(cols.length, 5); i++) {
                    Cell cell = row.getCell(i);
                    if (cell == null) cell = row.createCell(i);
                    cell.setCellValue(cols[i]);
                }
                rowIndex++;
            }

            for (String val : currentUserGroupMapWithoutRelations.values()) {
                Row row = sheet.getRow(rowIndex);
                if (row == null) row = sheet.createRow(rowIndex);
                String[] cols = val.split(java.util.regex.Pattern.quote(DELIM));
                for (int i = 0; i < Math.min(cols.length, 3); i++) {
                    Cell cell = row.getCell(i);
                    if (cell == null) cell = row.createCell(i);
                    cell.setCellValue(cols[i]);
                }
                rowIndex++;
            }

            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            workbook.write(bos);
            workbook.close();

            String fileName = "用户组&用户导入模板-导出.xlsx";
            // 使用 RFC 5987 标准格式，同时设置 filename 和 filename* 以兼容不同浏览器
            String encodedFileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8.toString())
                    .replaceAll("\\+", "%20"); // URLEncoder 会将空格编码为 +，需要替换为 %20
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            // 同时设置 filename 和 filename*，filename* 使用 RFC 5987 格式
            headers.set(HttpHeaders.CONTENT_DISPOSITION, 
                    "attachment; filename=\"" + encodedFileName + "\"; filename*=UTF-8''" + encodedFileName);
            return ResponseEntity.ok().headers(headers).body(bos.toByteArray());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(e.getMessage().getBytes());
        }
    }

    /**
     * 按批查询（每批最多 ORACLE_IN_MAX 条），避免 Oracle ORA-01795（IN 列表超过 1000 项）
     */
    private <T> List<T> selectBatchIdsInChunks(Collection<Long> ids, Function<List<Long>, List<T>> selector) {
        if (ids == null || ids.isEmpty()) return Collections.emptyList();
        List<Long> idList = new ArrayList<>(ids);
        List<T> result = new ArrayList<>();
        for (int i = 0; i < idList.size(); i += ORACLE_IN_MAX) {
            int end = Math.min(i + ORACLE_IN_MAX, idList.size());
            List<Long> batch = idList.subList(i, end);
            result.addAll(selector.apply(batch));
        }
        return result;
    }
    
    /**
     * 获取单元格的值并转换为字符串
     */
    private String getCellValueAsString(Cell cell) {
        if (cell == null) {
            return "";
        }
        
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                if (org.apache.poi.ss.usermodel.DateUtil.isCellDateFormatted(cell)) {
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

    /* @Operation(summary = "14.上传Excel并读取人员工号和姓名，与数据库对比进行增量新增")
    @PostMapping(value = "/readUserInfoFromExcel")
    public CommonResult<String> readUserInfoFromExcel(@RequestParam("file") org.springframework.web.multipart.MultipartFile file) {
        long startTime = System.currentTimeMillis();
        log.info("=== 开始处理Excel文件（读取人员工号和姓名，与数据库对比） ===");
        
        if (file == null || file.isEmpty()) {
            log.error("文件参数为空");
            return CommonResult.error("500", "文件参数为空，请检查请求格式");
        }
        
        log.info("文件名: {}, 文件大小: {} bytes", file.getOriginalFilename(), file.getSize());
        
        try {
            // 读取Excel文件
            org.apache.poi.ss.usermodel.Workbook workbook = org.apache.poi.ss.usermodel.WorkbookFactory.create(file.getInputStream());
            
            // 查找名为"角色组&用户"的sheet页
            org.apache.poi.ss.usermodel.Sheet targetSheet = null;
            String targetSheetName = "角色组&用户";
            
            for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
                org.apache.poi.ss.usermodel.Sheet sheet = workbook.getSheetAt(i);
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
            
            // 用于存储Excel中人员工号和姓名的Set（格式：工号_姓名）
            java.util.Set<String> excelUserInfoSet = new java.util.HashSet<>();
            
            // 统计信息
            int totalRows = 0;
            int validRows = 0;
            int invalidRows = 0;
            java.util.List<String> errorMessages = new java.util.ArrayList<>();
            
            // 从第二行开始读取（索引1，因为第一行是表头）
            for (int rowIndex = 1; rowIndex <= targetSheet.getLastRowNum(); rowIndex++) {
                org.apache.poi.ss.usermodel.Row row = targetSheet.getRow(rowIndex);
                
                if (row == null) {
                    continue;
                }
                
                totalRows++;
                
                // 获取第五列的值（人员工号）
                org.apache.poi.ss.usermodel.Cell fifthCell = row.getCell(4); // 索引4表示第五列
                String userNo = fifthCell != null ? getCellValueAsString(fifthCell).trim() : "";
                
                // 获取第六列的值（人员名称）
                org.apache.poi.ss.usermodel.Cell sixthCell = row.getCell(5); // 索引5表示第六列
                String userName = sixthCell != null ? getCellValueAsString(sixthCell).trim() : "";
                
                // 验证数据完整性
                if (userNo.isEmpty() || userName.isEmpty()) {
                    invalidRows++;
                    errorMessages.add(String.format("行%d: 人员工号或人员名称为空 (工号: %s, 姓名: %s)", 
                        rowIndex + 1, userNo, userName));
                    continue;
                }
                
                // 将工号和姓名用"_"连接起来
                String userInfo = userNo + "_" + userName;
                excelUserInfoSet.add(userInfo);
                validRows++;
            }
            
            workbook.close();
            
            log.info("=== Excel读取完成 ===");
            log.info("总数据行数: {}, 有效数据: {}, 无效数据: {}, 提取人员信息: {}条", 
                    totalRows, validRows, invalidRows, excelUserInfoSet.size());
            
            // ========== 查询数据库中的用户信息 ==========
            log.info("=== 开始查询数据库中的用户信息 ===");
            java.util.Set<String> dbUserInfoSet = systemUserService.getAllActiveUserInfo();
            log.info("数据库中现有用户信息数量: {}", dbUserInfoSet.size());
            
            // ========== 对比两个Set，找出需要新增的用户 ==========
            log.info("=== 开始对比Excel和数据库数据 ===");
            
            // Excel中有但数据库中没有的，需要新增
            java.util.Set<String> toAddSet = new java.util.HashSet<>(excelUserInfoSet);
            toAddSet.removeAll(dbUserInfoSet);
            
            // Excel中有且数据库中也有，已存在，不做任何改变
            java.util.Set<String> existingSet = new java.util.HashSet<>(excelUserInfoSet);
            existingSet.retainAll(dbUserInfoSet);
            
            // Excel中没有但数据库中有的，忽略
            java.util.Set<String> toIgnoreSet = new java.util.HashSet<>(dbUserInfoSet);
            toIgnoreSet.removeAll(excelUserInfoSet);
            
            log.info("=== 对比结果统计 ===");
            log.info("需要新增: {}, 已存在: {}, 需要忽略: {}", 
                    toAddSet.size(), existingSet.size(), toIgnoreSet.size());
            
            // ========== 执行增量新增操作 ==========
            int addedCount = 0;
            java.util.List<String> addErrorMessages = new java.util.ArrayList<>();
            
            if (!toAddSet.isEmpty()) {
                log.info("=== 开始执行增量新增操作 ===");
                for (String userInfo : toAddSet) {
                    try {
                        String[] parts = userInfo.split("_");
                        if (parts.length == 2) {
                            String userNo = parts[0];
                            String userName = parts[1];
                            
                            // 调用系统用户服务新增用户
                            systemUserService.createUserFromExcel(userNo, userName);
                            addedCount++;
                        } else {
                            addErrorMessages.add(String.format("用户信息格式错误: %s", userInfo));
                            log.error("✗ 用户信息格式错误: {}", userInfo);
                        }
                    } catch (Exception e) {
                        addErrorMessages.add(String.format("新增用户失败: %s, 错误: %s", userInfo, e.getMessage()));
                        log.error("✗ 新增用户失败: {}, 错误: {}", userInfo, e.getMessage());
                    }
                }
                log.info("新增操作完成，成功新增: {} 个用户", addedCount);
            }
            
            long duration = System.currentTimeMillis() - startTime;
            
            // 打印错误信息
            if (!errorMessages.isEmpty()) {
                log.warn("Excel数据验证错误: {} 条", errorMessages.size());
            }
            
            if (!addErrorMessages.isEmpty()) {
                log.warn("新增用户错误: {} 条", addErrorMessages.size());
            }
            
            String resultMessage = String.format(
                "Excel处理成功！总行数: %d, 有效数据: %d, 无效数据: %d, " +
                "Excel用户数: %d, 数据库用户数: %d, 需要新增: %d, 已存在: %d, 实际新增: %d, 耗时: %dms",
                totalRows, validRows, invalidRows, 
                excelUserInfoSet.size(), dbUserInfoSet.size(), 
                toAddSet.size(), existingSet.size(), addedCount, duration
            );
            
            log.info("=== 处理完成 ===");
            
            return CommonResult.success(resultMessage);
            
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            log.error("=== Excel处理失败 ===");
            log.error("错误信息: {}, 耗时: {}ms", e.getMessage(), duration, e);
            return CommonResult.error("500", "Excel处理失败: " + e.getMessage());
        }
    }
    */

    /**
     * 打印连接池状态
     */
    private void printConnectionPoolStatus(String methodName) {
        try {
            MBeanServer mBeanServer = ManagementFactory.getPlatformMBeanServer();
            ObjectName objectName = new ObjectName("com.zaxxer.hikari:type=Pool (master)");
            
            Integer activeConnections = (Integer) mBeanServer.getAttribute(objectName, "ActiveConnections");
            Integer idleConnections = (Integer) mBeanServer.getAttribute(objectName, "IdleConnections");
            Integer totalConnections = (Integer) mBeanServer.getAttribute(objectName, "TotalConnections");
            Integer threadsAwaitingConnection = (Integer) mBeanServer.getAttribute(objectName, "ThreadsAwaitingConnection");
            
            log.info("=== 连接池状态 [{}] ===", methodName);
            log.info("活跃连接: {}, 空闲连接: {}, 总连接: {}, 等待连接线程: {}", 
                    activeConnections, idleConnections, totalConnections, threadsAwaitingConnection);
            
            // 如果等待连接线程大于0，说明连接池可能有问题
            if (threadsAwaitingConnection > 0) {
                log.warn("警告: 有 {} 个线程正在等待数据库连接！", threadsAwaitingConnection);
            }
            
        } catch (Exception e) {
            log.warn("无法获取连接池状态: {}", e.getMessage());
        }
    }

    @Operation(summary = "15.上传Excel并导入用户组基础数据与用户关系")
    @PostMapping(value = "/importUserGroupAndRelations")
    public CommonResult<String> importUserGroupAndRelations(@RequestParam("file") org.springframework.web.multipart.MultipartFile file) {
        try {
            String baseResult = uploadAndReadExcelInternal(file);
            String relationResult = syncUserGroupRelationInternal(file);

            StringBuilder sb = new StringBuilder();
            if (baseResult != null && !baseResult.trim().isEmpty()) {
                sb.append("用户组基本数据导入情况:\n");
                sb.append(baseResult).append('\n');
            }
            if (relationResult != null && !relationResult.trim().isEmpty()) {
                sb.append("用户组关系数据导入情况:\n");
                sb.append(relationResult).append('\n');
            }

            String msg = sb.length() == 0 ? "无可展示的导入结果" : sb.toString();
            return CommonResult.success(msg);
        } catch (Exception e) {
            return CommonResult.error("500", e.getMessage());
        }
    }

    @Operation(summary = "16.获取用户组树")
    @GetMapping(value = "/getUserGroupTree")
    public CommonResult<List<UserGroupTreeResp>> getUserGroupTree() {
        List<UserGroupTreeResp> tree = userGroupService.getUserGroupTree();
        return CommonResult.success(tree);
    }
    @Operation(summary = "17.保存用户组")
    @PostMapping(value = "/saveUserGroupByTree")
    public CommonResult<Void> saveUserGroup(@RequestBody @Validated UserGroupTreeSaveVo userGroupTreeSaveVo) {
        return  userGroupService.saveUserGroup(userGroupTreeSaveVo);
    }
    @Operation(summary = "18.复制用户组树")
    @PostMapping(value = "/copyUserGroupTree")
    public CommonResult<Void> copyUserGroupTree(@RequestBody @Validated CopyUserGroupTreeVo copyUserGroupTreeVo ) {
        return userGroupService.copyUserGroupTree(copyUserGroupTreeVo);
    }
    @Operation(summary = "19.新增用户组树")
    @PostMapping(value = "/addUserGroupTree")
    public CommonResult<Void> addUserGroupTree(@RequestBody @Validated CopyUserGroupTreeVo addUserGroupTreeVo) {
        return userGroupService.addUserGroupTree(addUserGroupTreeVo);
    }
    // 编辑用户组，  可编辑字段有名称、备注
    @Operation(summary = "20.编辑用户组")
    @PostMapping(value = "/editUserGroup")
    public CommonResult<Void> editUserGroup(@RequestBody @Validated EditUserGroupVo editUserGroupVo) {
        return userGroupService.editUserGroup(editUserGroupVo);
    }
}
