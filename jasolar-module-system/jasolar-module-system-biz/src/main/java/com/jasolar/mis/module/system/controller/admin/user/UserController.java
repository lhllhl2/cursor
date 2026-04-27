package com.jasolar.mis.module.system.controller.admin.user;

import com.jasolar.mis.framework.common.pojo.CommonResult;
import com.jasolar.mis.framework.common.pojo.PageResult;
import com.jasolar.mis.module.system.controller.admin.user.resp.CurrentUserInfoResp;
import com.jasolar.mis.module.system.controller.admin.user.vo.user.*;
import com.jasolar.mis.module.system.controller.admin.usergroup.resp.GroupUserResp;
import com.jasolar.mis.module.system.domain.admin.user.SystemUserDo;
import com.jasolar.mis.module.system.service.admin.user.SystemUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Tag(name = "管理后台 - 用户")
@Slf4j
@RestController
@RequestMapping("/system/user")
public class UserController {


    @Autowired
    private SystemUserService systemUserService;

    @Operation(summary = "1.用户分页查询")
    @PostMapping(value = "/userPage")
    public CommonResult<PageResult<UserPageVo>> userPage(@RequestBody UserPageReqVO userPageReqVO){
        PageResult<UserPageVo> page =  systemUserService.userPage(userPageReqVO);
        return CommonResult.success(page);
    }



    @Operation(summary = "2.通过关键字查询用户")
    @PostMapping(value = "/userForGroup")
    public CommonResult<List<GroupUserResp>> userForGroup(@RequestBody UserForGroupVo forGroupVo){
        List<GroupUserResp> res = systemUserService.userForGroup(forGroupVo);
        return CommonResult.success(res);
    }


    @GetMapping("/currentUserInfo")
    public CommonResult<CurrentUserInfoResp> currentUserInfo(){
        CurrentUserInfoResp resp = systemUserService.currentUserInfo();
        return CommonResult.success(resp);
    }


    @Operation(summary = "3.根据用户ID修改用户名")
    @PostMapping(value = "/updateUserName")
    public CommonResult<Void> updateUserName(@RequestBody @Valid UserUpdateUserNameReqVO reqVO){
        systemUserService.updateUserName(reqVO);
        return CommonResult.success(null);
    }


    @Operation(summary = "4.修改密码")
    @PostMapping(value = "/updatePwd")
    public CommonResult<Void> updatePwd(@RequestBody UserUpdatePwdVo userUpdatePwdVo){
        systemUserService.updatePwd(userUpdatePwdVo);
        return CommonResult.success();
    }


    @Operation(summary = "5.批量导出用户（按模板）")
    @GetMapping(value = "/batchExport")
    public ResponseEntity<byte[]> batchExport(){
        try{
            // 1. 读取模板
            String templatePath = "excel/用户导入模板(用户管理界面).xlsx";
            InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(templatePath);
            if(is == null){
                return ResponseEntity.internalServerError().body(("模板不存在:" + templatePath).getBytes());
            }
            Workbook workbook = WorkbookFactory.create(is);
            Sheet sheet = workbook.getSheetAt(0);

            // 2. 查询数据（未删除）
            List<SystemUserDo> users = systemUserService.listActiveUsersForExport();

            // 3. 按模板填充：第一行为表头，从第二行开始写入
            int rowIndex = 1; // 从第二行开始
            for (SystemUserDo u : users) {
                org.apache.poi.ss.usermodel.Row row = sheet.getRow(rowIndex);
                if(row == null){
                    row = sheet.createRow(rowIndex);
                }
                // 第一列：人员工号 => userName
                org.apache.poi.ss.usermodel.Cell c0 = row.getCell(0);
                if(c0 == null){ c0 = row.createCell(0); }
                c0.setCellValue(u.getUserName());

                // 第二列：人员名称 => displayName
                org.apache.poi.ss.usermodel.Cell c1 = row.getCell(1);
                if(c1 == null){ c1 = row.createCell(1); }
                c1.setCellValue(u.getDisplayName());

                rowIndex++;
            }

            // 4. 写出为字节并返回
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            workbook.write(bos);
            workbook.close();

            String fileName = "用户导入模板(用户管理界面)-导出.xlsx";
            // 使用 RFC 5987 标准格式，同时设置 filename 和 filename* 以兼容不同浏览器
            String encodedFileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8.toString())
                    .replaceAll("\\+", "%20"); // URLEncoder 会将空格编码为 +，需要替换为 %20
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            // 同时设置 filename 和 filename*，filename* 使用 RFC 5987 格式
            headers.set(HttpHeaders.CONTENT_DISPOSITION, 
                    "attachment; filename=\"" + encodedFileName + "\"; filename*=UTF-8''" + encodedFileName);
            return ResponseEntity.ok().headers(headers).body(bos.toByteArray());
        }catch (Exception e){
            return ResponseEntity.internalServerError().body(e.getMessage().getBytes());
        }
    }




 
    /** 获取单元格的值并转换为字符串 */
    private String getCellValueAsString(org.apache.poi.ss.usermodel.Cell cell) {
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
                    double numericValue = cell.getNumericCellValue();
                    if (numericValue == Math.floor(numericValue)) {
                        return String.valueOf((long) numericValue);
                    } else {
                        return String.valueOf(numericValue);
                    }
                }
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                try {
                    return cell.getStringCellValue();
                } catch (IllegalStateException e) {
                    double numericValue = cell.getNumericCellValue();
                    if (numericValue == Math.floor(numericValue)) {
                        return String.valueOf((long) numericValue);
                    } else {
                        return String.valueOf(numericValue);
                    }
                }
            case BLANK:
                return "";
            default:
                return "";
        }
    }

    @Operation(summary = "6.上传Excel并读取人员工号和姓名，与数据库对比进行增量新增")
    @PostMapping(value = "/readUserInfoFromExcel")
    public CommonResult<String> readUserInfoFromExcel(@RequestParam("file") MultipartFile file) {
        long startTime = System.currentTimeMillis();
        log.info("=== 开始处理Excel文件（读取人员工号和姓名，与数据库对比） ===");

        if (file == null || file.isEmpty()) {
            log.error("文件参数为空");
            return CommonResult.error("500", "文件参数为空，请检查请求格式");
        }

        log.info("文件名: {}, 文件大小: {} bytes", file.getOriginalFilename(), file.getSize());

        try {
            // 读取Excel文件
            Workbook workbook = WorkbookFactory.create(file.getInputStream());

            // 查找名为"用户数据"的sheet页
            Sheet targetSheet = null;
            String targetSheetName = "用户数据";

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

            // 用于存储Excel中人员工号和姓名的Set（格式：工号_姓名）
            java.util.Set<String> excelUserInfoSet = new java.util.HashSet<>();

            // 统计信息
            int totalRows = 0;
            int validRows = 0;
            int invalidRows = 0;
            List<String> errorMessages = new java.util.ArrayList<>();

            // 从第二行开始读取（索引1，因为第一行是表头）
            for (int rowIndex = 1; rowIndex <= targetSheet.getLastRowNum(); rowIndex++) {
                org.apache.poi.ss.usermodel.Row row = targetSheet.getRow(rowIndex);

                if (row == null) {
                    continue;
                }

                totalRows++;

                // 获取第一列的值（人员工号）
                org.apache.poi.ss.usermodel.Cell firstCell = row.getCell(0); // 索引0表示第一列
                String userNo = firstCell != null ? getCellValueAsString(firstCell).trim() : "";

                // 获取第二列的值（人员名称）
                org.apache.poi.ss.usermodel.Cell secondCell = row.getCell(1); // 索引1表示第二列
                String userName = secondCell != null ? getCellValueAsString(secondCell).trim() : "";

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
            List<String> addErrorMessages = new java.util.ArrayList<>();

            if (!toAddSet.isEmpty()) {
                log.info("=== 开始执行增量新增操作 ===");
                for (String userInfo : toAddSet) {
                    try {
                        String[] parts = userInfo.split("_");
                        if (parts.length == 2) {
                            String userNo2 = parts[0];
                            String userName2 = parts[1];

                            // 调用系统用户服务新增用户
                            systemUserService.createUserFromExcel(userNo2, userName2);
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
    // 7. 重置用户密码
    @Operation(summary = "7.重置用户密码")
    @PostMapping(value = "/resetPwd")
    public CommonResult<Void> resetPwd(@RequestBody @Valid ResetPwdReqVO reqVO) {
        systemUserService.resetPwd(reqVO);
        return CommonResult.success();
    }
}
