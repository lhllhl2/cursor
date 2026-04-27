package com.jasolar.mis.module.system.service.budget.account.impl;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.read.listener.ReadListener;
import com.baomidou.mybatisplus.core.incrementer.IdentifierGenerator;
import com.jasolar.mis.module.system.domain.budget.BudgetAccount;
import com.jasolar.mis.module.system.mapper.budget.BudgetAccountMapper;
import com.jasolar.mis.module.system.service.budget.account.BudgetAccountService;
import jakarta.annotation.Resource;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 预算科目 Service 实现类
 *
 * @author Auto Generated
 * @date 2025-01-XX
 */
@Service
@Slf4j
public class BudgetAccountServiceImpl implements BudgetAccountService {

    @Resource
    private BudgetAccountMapper budgetAccountMapper;

    @Resource
    private IdentifierGenerator identifierGenerator;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String importData(MultipartFile file) {
        log.info("开始导入预算科目数据，文件名: {}", file.getOriginalFilename());

        if (file.isEmpty()) {
            return "文件为空，请选择要导入的Excel文件";
        }

        List<BudgetAccountExcelData> excelDataList = new ArrayList<>();
        String sheetName = "预算科目主数据示例";

        try {
            // 使用 EasyExcel 读取指定 sheet
            EasyExcel.read(file.getInputStream(), BudgetAccountExcelData.class, 
                    new BudgetAccountReadListener(excelDataList))
                    .sheet(sheetName)
                    .headRowNumber(1) // 跳过第一行表头
                    .doRead();

            log.info("成功读取 {} 条数据", excelDataList.size());

            if (excelDataList.isEmpty()) {
                return "Excel文件中没有有效数据";
            }

            // 转换为实体对象并批量插入
            List<BudgetAccount> accountList = convertToEntityList(excelDataList);
            Boolean success = budgetAccountMapper.insertBatch(accountList);
            int successCount = success ? accountList.size() : 0;

            return String.format("导入成功！共读取 %d 条数据，成功插入 %d 条", excelDataList.size(), successCount);

        } catch (IOException e) {
            log.error("读取Excel文件失败", e);
            return "读取Excel文件失败: " + e.getMessage();
        } catch (Exception e) {
            log.error("导入数据失败", e);
            return "导入数据失败: " + e.getMessage();
        }
    }

    /**
     * 将Excel数据转换为实体对象列表
     */
    private List<BudgetAccount> convertToEntityList(List<BudgetAccountExcelData> excelDataList) {
        List<BudgetAccount> accountList = new ArrayList<>();

        for (BudgetAccountExcelData excelData : excelDataList) {
            BudgetAccount account = new BudgetAccount();
            // 使用雪花算法生成ID
            account.setId(identifierGenerator.nextId(null).longValue());
            account.setCustomCode(excelData.getCustomCode());
            account.setAccountSubjectCode(excelData.getAccountSubjectCode());
            account.setAccountName(excelData.getAccountName());
            account.setParentAccountSubjectCode(excelData.getParentAccountSubjectCode());
            account.setIsLeaf(null); // IS_LEAF 为空
            // 设置删除标记为 false（对应数据库的 0）
            account.setDeleted(false);
            // CREATOR, CREATE_TIME, UPDATER, UPDATE_TIME 由 BaseDO 提供，框架会自动填充

            accountList.add(account);
        }

        return accountList;
    }

    /**
     * Excel 数据读取监听器
     */
    private class BudgetAccountReadListener implements ReadListener<BudgetAccountExcelData> {
        private final List<BudgetAccountExcelData> dataList;

        public BudgetAccountReadListener(List<BudgetAccountExcelData> dataList) {
            this.dataList = dataList;
        }

        @Override
        public void invoke(BudgetAccountExcelData data, AnalysisContext context) {
            dataList.add(data);
        }

        @Override
        public void doAfterAllAnalysed(AnalysisContext context) {
            log.info("Excel读取完成，共读取 {} 条数据", dataList.size());
        }
    }

    /**
     * Excel 数据模型类
     * 对应Excel列：第1列(索引0)=CUSTOM_CODE, 第2列(索引1)=ACCOUNT_SUBJECT_CODE,
     * 第3列(索引2)=PARENT_ACCOUNT_SUBJECT_CODE, 第4列(索引3)=ACCOUNT_NAME
     */
    @Data
    public static class BudgetAccountExcelData {
        // 第1列（索引0）：CUSTOM_CODE
        @ExcelProperty(index = 0)
        private String customCode;

        // 第2列（索引1）：ACCOUNT_SUBJECT_CODE
        @ExcelProperty(index = 1)
        private String accountSubjectCode;

        // 第3列（索引2）：PARENT_ACCOUNT_SUBJECT_CODE
        @ExcelProperty(index = 2)
        private String parentAccountSubjectCode;

        // 第4列（索引3）：ACCOUNT_NAME
        @ExcelProperty(index = 3)
        private String accountName;
    }
}

