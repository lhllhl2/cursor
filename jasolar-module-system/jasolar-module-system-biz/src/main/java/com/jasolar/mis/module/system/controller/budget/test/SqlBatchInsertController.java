package com.jasolar.mis.module.system.controller.budget.test;

import com.jasolar.mis.framework.common.pojo.CommonResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 预算测试 - SQL 批量插入
 * <p>
 * 用于读取上传的 Oracle 导出 SQL 文件（INSERT 语句），按行解析后批量执行插入。
 * 支持 BUDGET_LEDGER、BUDGET_LEDGER_HEAD、SUBJECT_INFO、PROJECT_CONTROL_R 等表的导入。
 * </p>
 */
@Tag(name = "预算管理 - 测试 - SQL批量插入")
@Slf4j
@RestController
@RequestMapping("/budget/test/sql")
public class SqlBatchInsertController {

    private static final int BATCH_SIZE = 200;

    @Resource
    private JdbcTemplate jdbcTemplate;

    @Resource
    private PlatformTransactionManager transactionManager;

    /**
     * 上传一个或多个 SQL 文件，解析其中的 INSERT 语句并批量执行
     *
     * @param files    SQL 文件（可多选，每行一条 INSERT）
     * @param batchSize 每批执行的条数，默认 200，传 0 使用默认值
     */
    @Operation(summary = "上传 SQL 文件并批量执行 INSERT")
    @PostMapping(value = "/batchInsert")
    public CommonResult<String> batchInsert(
            @RequestPart("files") MultipartFile[] files,
            @RequestParam(required = false, defaultValue = "200") int batchSize) {
        if (files == null || files.length == 0) {
            return CommonResult.error("400", "请至少上传一个 SQL 文件");
        }
        if (batchSize <= 0) {
            batchSize = BATCH_SIZE;
        }

        StringBuilder result = new StringBuilder();
        int totalExecuted = 0;
        int totalFailed = 0;

        for (MultipartFile file : files) {
            if (file.isEmpty()) {
                continue;
            }
            String fileName = file.getOriginalFilename();
            result.append("文件: ").append(fileName).append("\n");

            try {
                List<String> statements = readInsertStatements(file);
                result.append("  解析到 INSERT 条数: ").append(statements.size()).append("\n");

                int[] executed = executeInBatches(statements, batchSize);
                totalExecuted += executed[0];
                totalFailed += executed[1];
                result.append("  执行成功: ").append(executed[0])
                        .append(", 失败: ").append(executed[1]).append("\n");
            } catch (Exception e) {
                log.error("处理文件 {} 失败", fileName, e);
                result.append("  错误: ").append(e.getMessage()).append("\n");
                totalFailed += 1;
            }
            result.append("\n");
        }

        result.insert(0, "汇总 - 成功: " + totalExecuted + ", 失败: " + totalFailed + "\n\n");
        return CommonResult.success(result.toString());
    }

    /**
     * 从上传文件中按行读取 INSERT 语句（每行一条完整 INSERT，忽略空行和注释）
     */
    private List<String> readInsertStatements(MultipartFile file) throws Exception {
        List<String> list = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("--")) {
                    continue;
                }
                if (line.endsWith(";")) {
                    line = line.substring(0, line.length() - 1).trim();
                }
                if (line.toUpperCase().startsWith("INSERT ")) {
                    list.add(line);
                }
            }
        }
        return list;
    }

    /**
     * 分批执行 SQL，每批一个事务；同一批内任一条失败则该批回滚并记录失败数
     */
    private int[] executeInBatches(List<String> statements, int batchSize) {
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);
        TransactionTemplate txTemplate = new TransactionTemplate(transactionManager);

        for (int i = 0; i < statements.size(); i += batchSize) {
            int end = Math.min(i + batchSize, statements.size());
            List<String> batch = statements.subList(i, end);

            try {
                txTemplate.execute(new TransactionCallbackWithoutResult() {
                    @Override
                    protected void doInTransactionWithoutResult(TransactionStatus status) {
                        for (String sql : batch) {
                            if (StringUtils.isBlank(sql)) {
                                continue;
                            }
                            jdbcTemplate.execute(sql);
                            successCount.incrementAndGet();
                        }
                    }
                });
            } catch (Exception e) {
                log.warn("本批执行失败，本批条数: {}, 错误: {}", batch.size(), e.getMessage());
                failCount.addAndGet(batch.size());
            }
        }

        return new int[]{successCount.get(), failCount.get()};
    }
}
