package com.jasolar.mis.module.system.controller.budget.test;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.jasolar.mis.framework.common.pojo.CommonResult;
import com.jasolar.mis.framework.common.util.json.JsonUtils;
import com.jasolar.mis.module.system.controller.budget.vo.ApplyDetailDetalVo;
import com.jasolar.mis.module.system.controller.budget.vo.ApplyReqInfoParams;
import com.jasolar.mis.module.system.controller.budget.vo.BudgetApplicationParams;
import com.jasolar.mis.module.system.controller.budget.vo.BudgetApplicationRespVo;
import com.jasolar.mis.module.system.controller.budget.vo.BudgetClaimApplyParams;
import com.jasolar.mis.module.system.controller.budget.vo.BudgetClaimRespVo;
import com.jasolar.mis.module.system.controller.budget.vo.BudgetRenewParams;
import com.jasolar.mis.module.system.controller.budget.vo.RenewApplyReqInfoParams;
import com.jasolar.mis.module.system.controller.budget.vo.BudgetRenewRespVo;
import com.jasolar.mis.module.system.controller.budget.vo.BudgetContractApplyParams;
import com.jasolar.mis.module.system.controller.budget.vo.BudgetContractRespVo;
import com.jasolar.mis.module.system.controller.budget.vo.BudgetContractRenewParams;
import com.jasolar.mis.module.system.controller.budget.vo.BudgetContractRenewRespVo;
import com.jasolar.mis.module.system.controller.budget.vo.BudgetClaimRenewParams;
import com.jasolar.mis.module.system.controller.budget.vo.BudgetClaimRenewRespVo;
import com.jasolar.mis.module.system.controller.budget.vo.ContractApplyReqInfoParams;
import com.jasolar.mis.module.system.controller.budget.vo.ContractApplyDetailVo;
import com.jasolar.mis.module.system.controller.budget.vo.ContractDetailDetailVo;
import com.jasolar.mis.module.system.controller.budget.vo.ContractRenewReqInfoParams;
import com.jasolar.mis.module.system.controller.budget.vo.ClaimApplyDetailVo;
import com.jasolar.mis.module.system.controller.budget.vo.ClaimApplyReqInfoParams;
import com.jasolar.mis.module.system.controller.budget.vo.ClaimContractDetailVo;
import com.jasolar.mis.module.system.controller.budget.vo.ClaimDetailDetailVo;
import com.jasolar.mis.module.system.controller.budget.vo.ClaimRenewReqInfoParams;
import com.jasolar.mis.module.system.controller.budget.vo.ESBRespInfoVo;
import com.jasolar.mis.module.system.controller.budget.commonapi.BudgetApplicationController;
import com.jasolar.mis.module.system.controller.budget.commonapi.BudgetClaimController;
import com.jasolar.mis.module.system.controller.budget.commonapi.BudgetContractController;
import com.jasolar.mis.module.system.controller.budget.vo.ESBInfoParams;
import com.jasolar.mis.module.system.controller.budget.vo.SubmitFailListExcelRowVo;
import com.jasolar.mis.module.system.controller.budget.vo.AdjustApplyReqInfoParams;
import com.jasolar.mis.module.system.controller.budget.vo.AdjustDetailDetailVo;
import com.jasolar.mis.module.system.controller.budget.vo.AdjustRenewReqInfoParams;
import com.jasolar.mis.module.system.controller.budget.vo.BudgetAdjustApplyParams;
import com.jasolar.mis.module.system.controller.budget.vo.BudgetAdjustRenewParams;
import com.jasolar.mis.module.system.controller.budget.vo.BudgetAdjustRespVo;
import com.jasolar.mis.module.system.controller.budget.vo.BudgetAdjustRenewRespVo;
import com.jasolar.mis.module.system.controller.budget.commonapi.BudgetAdjustController;
import com.jasolar.mis.module.system.domain.budget.BudgetLedger;
import com.jasolar.mis.module.system.domain.budget.BudgetLedgerHead;
import com.jasolar.mis.module.system.domain.budget.BudgetLedgerHeadRecoverBak;
import com.jasolar.mis.module.system.domain.budget.BudgetLedgerRecoverBak;
import com.jasolar.mis.module.system.domain.budget.BudgetLedgerHeadForOperate;
import com.jasolar.mis.module.system.domain.budget.BudgetLedgerForOperate;
import com.jasolar.mis.module.system.domain.budget.BudgetLedgerSelfRForOperate;
import com.jasolar.mis.module.system.domain.budget.BudgetOperateBatchRunRecord;
import com.jasolar.mis.module.system.mapper.budget.BudgetLedgerHeadMapper;
import com.jasolar.mis.module.system.mapper.budget.BudgetLedgerHeadRecoverBakMapper;
import com.jasolar.mis.module.system.mapper.budget.BudgetLedgerMapper;
import com.jasolar.mis.module.system.mapper.budget.BudgetLedgerRecoverBakMapper;
import com.jasolar.mis.module.system.mapper.budget.BudgetApiRequestLogMapper;
import com.jasolar.mis.module.system.mapper.budget.BudgetLedgerHeadForOperateMapper;
import com.jasolar.mis.module.system.mapper.budget.BudgetLedgerForOperateMapper;
import com.jasolar.mis.module.system.mapper.budget.BudgetLedgerSelfRForOperateMapper;
import com.jasolar.mis.module.system.mapper.budget.BudgetOperateBatchRunRecordMapper;
import com.jasolar.mis.module.system.mapper.budget.BudgetApiRequestLogViewMapper;
import com.jasolar.mis.module.system.mapper.budget.BudgetBalanceMapper;
import com.jasolar.mis.module.system.mapper.budget.BudgetPoolDemRMapper;
import com.jasolar.mis.module.system.mapper.budget.BudgetQuotaMapper;
import com.jasolar.mis.module.system.domain.budget.BudgetApiRequestLog;
import com.jasolar.mis.module.system.domain.budget.BudgetApiRequestLogView;
import com.jasolar.mis.module.system.domain.budget.BudgetBalance;
import com.jasolar.mis.module.system.domain.budget.BudgetPoolDemR;
import com.jasolar.mis.module.system.domain.budget.BudgetQuota;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.beans.BeanUtils;
import com.jasolar.mis.module.system.service.ehr.SubjectInfoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.read.listener.ReadListener;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;

import org.springframework.beans.factory.annotation.Value;

import jakarta.servlet.http.HttpServletResponse;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.net.URLEncoder;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import java.util.UUID;

@Tag(name = "预算管理 - 测试")
@Slf4j
@RestController
@RequestMapping("/budget/test")
public class TestController {

    @Autowired
    private SubjectInfoService subjectInfoService;

    @Resource
    private BudgetLedgerHeadMapper budgetLedgerHeadMapper;

    @Resource
    private BudgetLedgerMapper budgetLedgerMapper;

    @Resource
    private BudgetLedgerHeadRecoverBakMapper budgetLedgerHeadRecoverBakMapper;

    @Resource
    private BudgetLedgerRecoverBakMapper budgetLedgerRecoverBakMapper;

    @Resource
    private BudgetApplicationController budgetApplicationController;

    @Resource
    private BudgetClaimController budgetClaimController;

    @Resource
    private BudgetContractController budgetContractController;

    @Resource
    private BudgetApiRequestLogMapper budgetApiRequestLogMapper;

    @Resource
    private BudgetLedgerHeadForOperateMapper budgetLedgerHeadForOperateMapper;

    @Resource
    private BudgetLedgerForOperateMapper budgetLedgerForOperateMapper;

    @Resource
    private BudgetLedgerSelfRForOperateMapper budgetLedgerSelfRForOperateMapper;

    @Resource
    private BudgetOperateBatchRunRecordMapper budgetOperateBatchRunRecordMapper;

    @Resource
    private BudgetApiRequestLogViewMapper budgetApiRequestLogViewMapper;

    @Resource
    private BudgetQuotaMapper budgetQuotaMapper;

    @Resource
    private BudgetBalanceMapper budgetBalanceMapper;

    @Resource
    private BudgetPoolDemRMapper budgetPoolDemRMapper;

    /** CSV 回填结果保存目录（Pod/服务器本地路径，便于 Kuboard 或 kubectl cp 下载），默认 /tmp/budget-csv-export */
    @Value("${budget.csv.export-dir:/tmp/budget-csv-export}")
    private String csvExportDir;

    @Resource
    private BudgetAdjustController budgetAdjustController;

    @Operation(summary = "测试接口")
    @GetMapping(value = "/test")
    public CommonResult<String> test() {
        return CommonResult.success("Test");
    }

    @Operation(summary = "逻辑删除预算（按项目编码集合，跳过NAN）")
    @PostMapping(value = "/logicDeleteBudgetByProjectCodes")
    @Transactional
    public CommonResult<Map<String, Object>> logicDeleteBudgetByProjectCodes(
            @RequestBody LogicDeleteBudgetByProjectCodesReq req) {
        try {
            List<String> projectCodes = req == null ? null : req.getProjectCodes();
            if (projectCodes == null || projectCodes.isEmpty()) {
                return CommonResult.error("400", "projectCodes 不能为空");
            }

            List<String> safeCodes = projectCodes.stream()
                    .filter(StringUtils::isNotBlank)
                    .map(String::trim)
                    .filter(StringUtils::isNotBlank)
                    .distinct()
                    .collect(Collectors.toList());
            if (safeCodes.isEmpty()) {
                return CommonResult.error("400", "projectCodes 为空");
            }

            List<String> nanCodes = safeCodes.stream()
                    .filter(c -> "NAN".equals(c))
                    .collect(Collectors.toList());
            List<String> notNanCodes = safeCodes.stream()
                    .filter(c -> !"NAN".equals(c))
                    .collect(Collectors.toList());

            List<Map<String, Object>> details = new ArrayList<>();
            int totalQuotaUpdated = 0;
            int totalBalanceUpdated = 0;
            int totalPoolDemRUpdated = 0;

            for (String code : notNanCodes) {
                // 走 TableLogic 逻辑删除：避免手动 update/set deleted 与逻辑删除规则不一致
                LambdaQueryWrapper<BudgetQuota> quotaDeleteWrapper = new LambdaQueryWrapper<>();
                quotaDeleteWrapper.eq(BudgetQuota::getProjectCode, code)
                        .eq(BudgetQuota::getDeleted, Boolean.FALSE);
                int quotaUpdated = budgetQuotaMapper.delete(quotaDeleteWrapper);

                LambdaQueryWrapper<BudgetBalance> balanceDeleteWrapper = new LambdaQueryWrapper<>();
                balanceDeleteWrapper.eq(BudgetBalance::getProjectCode, code)
                        .eq(BudgetBalance::getDeleted, Boolean.FALSE);
                int balanceUpdated = budgetBalanceMapper.delete(balanceDeleteWrapper);

                LambdaQueryWrapper<BudgetPoolDemR> poolDemRDeleteWrapper = new LambdaQueryWrapper<>();
                poolDemRDeleteWrapper.eq(BudgetPoolDemR::getMasterProjectCode, code)
                        .eq(BudgetPoolDemR::getDeleted, Boolean.FALSE);
                int poolDemRUpdated = budgetPoolDemRMapper.delete(poolDemRDeleteWrapper);

                totalQuotaUpdated += quotaUpdated;
                totalBalanceUpdated += balanceUpdated;
                totalPoolDemRUpdated += poolDemRUpdated;

                Map<String, Object> d = new LinkedHashMap<>();
                d.put("projectCode", code);
                d.put("budgetQuotaUpdated", quotaUpdated);
                d.put("budgetBalanceUpdated", balanceUpdated);
                d.put("budgetPoolDemRUpdated", poolDemRUpdated);
                details.add(d);
            }

            Map<String, Object> resp = new LinkedHashMap<>();
            resp.put("skippedNanCount", nanCodes.size());
            resp.put("projectsProcessed", notNanCodes.size());
            resp.put("totalQuotaUpdated", totalQuotaUpdated);
            resp.put("totalBalanceUpdated", totalBalanceUpdated);
            resp.put("totalPoolDemRUpdated", totalPoolDemRUpdated);
            resp.put("details", details);
            return CommonResult.success(resp);
        } catch (Exception e) {
            log.error("逻辑删除预算失败", e);
            return CommonResult.error("500", "处理失败: " + e.getMessage());
        }
    }

    @Operation(summary = "操作 BUDGET_LEDGER（按 bizCode 或全表逐行更新）")
    @PostMapping(value = "/operateBudgetLedger")
    public CommonResult<Map<String, Object>> operateBudgetLedger(@RequestBody OperateBudgetLedgerReq req) {
        try {
            OperateBudgetLedgerReq safeReq = req == null ? new OperateBudgetLedgerReq() : req;
            boolean hasBizCodes = safeReq.getBizCode() != null && !safeReq.getBizCode().isEmpty();

            List<LedgerFieldChange> changes = safeReq.buildChangesInOrder();
            for (LedgerFieldChange c : changes) {
                if (c.pair == null) {
                    continue;
                }
                if (!org.springframework.util.StringUtils.hasText(c.pair.getOriginal())
                        || !org.springframework.util.StringUtils.hasText(c.pair.getNewValue())) {
                    return CommonResult.error("400", "字段 " + c.fieldName + " 传参不完整：original/new 不能为空");
                }
            }

            int processed = 0;
            int updated = 0;
            int skipped = 0;

            if (hasBizCodes) {
                List<String> bizCodes = safeReq.getBizCode().stream()
                        .filter(org.springframework.util.StringUtils::hasText)
                        .map(String::trim)
                        .distinct()
                        .collect(Collectors.toList());
                if (bizCodes.isEmpty()) {
                    return CommonResult.error("400", "bizCode 传了但为空");
                }
                LambdaQueryWrapper<BudgetLedger> qw = new LambdaQueryWrapper<>();
                qw.in(BudgetLedger::getBizCode, bizCodes).eq(BudgetLedger::getDeleted, Boolean.FALSE);
                List<BudgetLedger> rows = budgetLedgerMapper.selectList(qw);

                Map<String, List<BudgetLedger>> byBiz = rows.stream().collect(Collectors.groupingBy(BudgetLedger::getBizCode));
                for (String code : bizCodes) {
                    if (!byBiz.containsKey(code)) {
                        return CommonResult.error("400", "未找到 BUDGET_LEDGER 数据，bizCode=" + code);
                    }
                }

                for (BudgetLedger row : rows) {
                    processed++;
                    boolean rowUpdated = applyChangesToRow(row, changes, true);
                    if (rowUpdated) {
                        updated++;
                    } else {
                        skipped++;
                    }
                }

                if (!rows.isEmpty()) {
                    budgetLedgerMapper.updateBatchById(rows);
                }
            } else {
                long current = 1L;
                long size = 500L;
                while (true) {
                    com.baomidou.mybatisplus.extension.plugins.pagination.Page<BudgetLedger> page =
                            new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(current, size);
                    LambdaQueryWrapper<BudgetLedger> qw = new LambdaQueryWrapper<>();
                    qw.eq(BudgetLedger::getDeleted, Boolean.FALSE);
                    qw.orderByAsc(BudgetLedger::getId);
                    com.baomidou.mybatisplus.core.metadata.IPage<BudgetLedger> p = budgetLedgerMapper.selectPage(page, qw);
                    List<BudgetLedger> records = p.getRecords();
                    if (records == null || records.isEmpty()) {
                        break;
                    }

                    List<BudgetLedger> needUpdate = new ArrayList<>();
                    for (BudgetLedger row : records) {
                        processed++;
                        boolean rowUpdated = applyChangesToRow(row, changes, false);
                        if (rowUpdated) {
                            updated++;
                            needUpdate.add(row);
                        } else {
                            skipped++;
                        }
                    }
                    if (!needUpdate.isEmpty()) {
                        budgetLedgerMapper.updateBatchById(needUpdate);
                    }
                    if (p.getCurrent() >= p.getPages()) {
                        break;
                    }
                    current++;
                }
            }

            Map<String, Object> resp = new LinkedHashMap<>();
            resp.put("processed", processed);
            resp.put("updated", updated);
            resp.put("skipped", skipped);
            resp.put("mode", hasBizCodes ? "byBizCode" : "all");
            return CommonResult.success(resp);
        } catch (IllegalArgumentException e) {
            return CommonResult.error("400", e.getMessage());
        } catch (Exception e) {
            log.error("操作 BUDGET_LEDGER 失败", e);
            return CommonResult.error("500", "处理失败: " + e.getMessage());
        }
    }

    private boolean applyChangesToRow(BudgetLedger row, List<LedgerFieldChange> changes, boolean strict) {
        if (row == null || changes == null || changes.isEmpty()) {
            return false;
        }
        BizItemCodeParts parts = BizItemCodeParts.parse(row.getBizItemCode(), strict, row.getBizCode());

        boolean any = false;
        for (LedgerFieldChange c : changes) {
            if (c.pair == null) {
                continue;
            }
            String expected = c.pair.getOriginal();
            String target = c.pair.getNewValue();
            String actual = c.getFieldValue(row);
            boolean match = Objects.equals(blankToNull(actual), blankToNull(expected));
            if (!match) {
                if (strict) {
                    throw new IllegalArgumentException("bizCode=" + row.getBizCode()
                            + " 字段=" + c.fieldName
                            + " original期望=" + expected
                            + " 实际=" + (actual == null ? "null" : actual));
                }
                continue;
            }

            c.setFieldValue(row, target);
            if ("morgCode".equals(c.fieldName)) {
                parts.morgCode = normalizeNan(target, "NAN");
            } else if ("budgetSubjectCode".equals(c.fieldName)) {
                parts.budgetSubjectCode = normalizeNan(target, "NAN-NAN");
            } else if ("masterProjectCode".equals(c.fieldName)) {
                parts.masterProjectCode = normalizeNan(target, "NAN");
            } else if ("erpAssetType".equals(c.fieldName)) {
                parts.erpAssetType = normalizeNan(target, "NAN");
            }
            any = true;
        }

        if (any) {
            row.setBizItemCode(parts.toBizItemCode());
        }
        return any;
    }

    private static String blankToNull(String s) {
        if (s == null) {
            return null;
        }
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }

    @lombok.Data
    private static class LogicDeleteBudgetByProjectCodesReq {
        private List<String> projectCodes;
    }

    private static String normalizeNan(String val, String nanToken) {
        String t = blankToNull(val);
        return t == null ? nanToken : t;
    }

    private static class BizItemCodeParts {
        String lineNo;
        String morgCode;
        String budgetSubjectCode;
        String masterProjectCode;
        String erpAssetType;

        static BizItemCodeParts parse(String bizItemCode, boolean strict, String bizCode) {
            if (!org.springframework.util.StringUtils.hasText(bizItemCode)) {
                if (strict) {
                    throw new IllegalArgumentException("bizCode=" + bizCode + " BIZ_ITEM_CODE 为空");
                }
                BizItemCodeParts p = new BizItemCodeParts();
                p.lineNo = "1";
                p.morgCode = "NAN";
                p.budgetSubjectCode = "NAN-NAN";
                p.masterProjectCode = "NAN";
                p.erpAssetType = "NAN";
                return p;
            }
            String[] arr = bizItemCode.split("@", -1);
            if (arr.length < 5) {
                if (strict) {
                    throw new IllegalArgumentException("bizCode=" + bizCode + " BIZ_ITEM_CODE 格式不正确: " + bizItemCode);
                }
                BizItemCodeParts p = new BizItemCodeParts();
                p.lineNo = arr.length > 0 ? arr[0] : "1";
                p.morgCode = arr.length > 1 ? arr[1] : "NAN";
                p.budgetSubjectCode = arr.length > 2 ? arr[2] : "NAN-NAN";
                p.masterProjectCode = arr.length > 3 ? arr[3] : "NAN";
                p.erpAssetType = arr.length > 4 ? arr[4] : "NAN";
                return p;
            }
            BizItemCodeParts p = new BizItemCodeParts();
            p.lineNo = arr[0];
            p.morgCode = org.springframework.util.StringUtils.hasText(arr[1]) ? arr[1] : "NAN";
            p.budgetSubjectCode = org.springframework.util.StringUtils.hasText(arr[2]) ? arr[2] : "NAN-NAN";
            p.masterProjectCode = org.springframework.util.StringUtils.hasText(arr[3]) ? arr[3] : "NAN";
            p.erpAssetType = org.springframework.util.StringUtils.hasText(arr[4]) ? arr[4] : "NAN";
            return p;
        }

        String toBizItemCode() {
            return (lineNo == null ? "" : lineNo) + "@"
                    + normalizeNan(morgCode, "NAN") + "@"
                    + normalizeNan(budgetSubjectCode, "NAN-NAN") + "@"
                    + normalizeNan(masterProjectCode, "NAN") + "@"
                    + normalizeNan(erpAssetType, "NAN");
        }
    }

    @lombok.Data
    private static class OperateBudgetLedgerReq {
        private List<String> bizCode;
        private ChangePair morgCode;
        private ChangePair budgetSubjectCode;
        private ChangePair masterProjectCode;
        private ChangePair erpAssetType;

        List<LedgerFieldChange> buildChangesInOrder() {
            List<LedgerFieldChange> list = new ArrayList<>();
            if (morgCode != null) {
                list.add(LedgerFieldChange.morgCode(morgCode));
            }
            if (budgetSubjectCode != null) {
                list.add(LedgerFieldChange.budgetSubjectCode(budgetSubjectCode));
            }
            if (masterProjectCode != null) {
                list.add(LedgerFieldChange.masterProjectCode(masterProjectCode));
            }
            if (erpAssetType != null) {
                list.add(LedgerFieldChange.erpAssetType(erpAssetType));
            }
            return list;
        }
    }

    @lombok.Data
    private static class ChangePair {
        private String original;
        @JsonProperty("new")
        private String newValue;
    }

    private static class LedgerFieldChange {
        final String fieldName;
        final ChangePair pair;

        private LedgerFieldChange(String fieldName, ChangePair pair) {
            this.fieldName = fieldName;
            this.pair = pair;
        }

        static LedgerFieldChange morgCode(ChangePair pair) {
            return new LedgerFieldChange("morgCode", pair);
        }

        static LedgerFieldChange budgetSubjectCode(ChangePair pair) {
            return new LedgerFieldChange("budgetSubjectCode", pair);
        }

        static LedgerFieldChange masterProjectCode(ChangePair pair) {
            return new LedgerFieldChange("masterProjectCode", pair);
        }

        static LedgerFieldChange erpAssetType(ChangePair pair) {
            return new LedgerFieldChange("erpAssetType", pair);
        }

        String getFieldValue(BudgetLedger row) {
            return switch (fieldName) {
                case "morgCode" -> row.getMorgCode();
                case "budgetSubjectCode" -> row.getBudgetSubjectCode();
                case "masterProjectCode" -> row.getMasterProjectCode();
                case "erpAssetType" -> row.getErpAssetType();
                default -> null;
            };
        }

        void setFieldValue(BudgetLedger row, String newVal) {
            switch (fieldName) {
                case "morgCode" -> row.setMorgCode(newVal);
                case "budgetSubjectCode" -> row.setBudgetSubjectCode(newVal);
                case "masterProjectCode" -> row.setMasterProjectCode(newVal);
                case "erpAssetType" -> row.setErpAssetType(newVal);
                default -> {
                }
            }
        }
    }

    /**
     * 从上传的 Excel 中读取 ERP 科目映射并更新 SUBJECT_INFO 的 ERP 字段
     */
    @Operation(summary = "测试导入 ERP 科目映射并更新 SUBJECT_INFO")
    @PostMapping(value = "/importErpSubject")
    public CommonResult<String> importErpSubject(@RequestPart("file") MultipartFile file) throws Exception {
        String result = subjectInfoService.importErpAcctFromExcel(file);
        return CommonResult.success(result);
    }

    /**
     * 上传 CSV（表头：BIZ_CODE, BIZ_TYPE, LEDGER_HEAD_STATUS, REQUEST_PARAMS），
     * 按 BIZ_CODE 从视图 V_BUDGET_API_REQUEST_LOG 查询 DOC_NO=BIZ_CODE、METHOD_NAME=apply、UPDATE_TIME 最近的一条的 REQUEST_PARAMS，
     * 回填到第四列并保存到服务器/Pod 本地目录，避免大文件直传浏览器过慢；可通过 Kuboard 进入 Pod 下载。
     */
    @Operation(summary = "上传CSV回填REQUEST_PARAMS并保存到服务器（Pod内下载）")
    @PostMapping(value = "/fillRequestParamsAndExportCsv")
    public CommonResult<Map<String, String>> fillRequestParamsAndExportCsv(
            @RequestPart("file") MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            return CommonResult.error("400", "请上传CSV文件");
        }
        List<String> headerNames;
        List<Map<String, String>> rows = new ArrayList<>();
        try (InputStreamReader reader = new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8);
             CSVParser parser = new CSVParser(reader, CSVFormat.DEFAULT.withFirstRecordAsHeader().withIgnoreHeaderCase().withTrim())) {
            headerNames = parser.getHeaderNames();
            if (headerNames == null || headerNames.isEmpty()) {
                return CommonResult.error("400", "CSV 表头为空");
            }
            for (CSVRecord record : parser) {
                Map<String, String> row = new LinkedHashMap<>();
                for (String h : headerNames) {
                    String v = record.get(h);
                    row.put(h, v != null ? v : "");
                }
                rows.add(row);
            }
        }
        for (Map<String, String> row : rows) {
            String bizCode = row.get("BIZ_CODE");
            if (org.springframework.util.StringUtils.hasText(bizCode)) {
                List<BudgetApiRequestLogView> list = budgetApiRequestLogViewMapper
                        .selectByDocNoAndMethodNameOrderByUpdateTimeDesc(bizCode.trim(), "apply");
                if (!list.isEmpty()) {
                    String requestParams = list.get(0).getRequestParams();
                    row.put("REQUEST_PARAMS", requestParams != null ? requestParams : "");
                }
            }
        }
        String filename = "biz_request_params_" + System.currentTimeMillis() + ".csv";
        Path exportPath = Paths.get(csvExportDir);
        Files.createDirectories(exportPath);
        Path filePath = exportPath.resolve(filename);
        // 使用 UTF-8 并写入 BOM，避免 Excel 等用系统编码打开时中文乱码
        try (FileOutputStream fos = new FileOutputStream(filePath.toFile());
             OutputStreamWriter writer = new OutputStreamWriter(fos, StandardCharsets.UTF_8);
             CSVPrinter printer = new CSVPrinter(writer, CSVFormat.DEFAULT.withHeader(headerNames.toArray(new String[0])))) {
            writer.write('\uFEFF'); // UTF-8 BOM
            for (Map<String, String> row : rows) {
                List<String> values = new ArrayList<>();
                for (String h : headerNames) {
                    String v = row.get(h);
                    values.add(v != null ? v : "");
                }
                printer.printRecord(values);
            }
            printer.flush();
        }
        String absolutePath = filePath.toAbsolutePath().toString();
        log.info("CSV 回填完成，已保存至: {}", absolutePath);
        Map<String, String> result = new LinkedHashMap<>();
        result.put("savedPath", absolutePath);
        result.put("filename", filename);
        result.put("message", "文件已保存至服务器。可通过 Kuboard 进入对应 Pod → 终端或文件管理，路径: " + absolutePath + " ；或使用 kubectl cp <namespace>/<pod>:<savedPath> ./ 下载。");
        return CommonResult.success(result);
    }

    /**
     * 查询并组装预算申请和报销申请参数
     */
    @Operation(summary = "查询并组装预算申请和报销申请参数")
    @GetMapping(value = "/assembleBudgetParams")
    public CommonResult<String> assembleBudgetParams() {
        try {
            // 1. 查询bizType为APPLY的BUDGET_LEDGER_HEAD_RECOVER_BAK，放入map1
            LambdaQueryWrapper<BudgetLedgerHeadRecoverBak> applyHeadWrapper = new LambdaQueryWrapper<>();
            applyHeadWrapper.eq(BudgetLedgerHeadRecoverBak::getBizType, "APPLY")
                    .eq(BudgetLedgerHeadRecoverBak::getDeleted, Boolean.FALSE);
            List<BudgetLedgerHeadRecoverBak> applyHeadsBak = budgetLedgerHeadRecoverBakMapper.selectList(applyHeadWrapper);
            // 转换为原实体类型
            List<BudgetLedgerHead> applyHeads = applyHeadsBak.stream().map(this::convertHeadBakToHead).collect(Collectors.toList());
            Map<String, BudgetLedgerHead> map1 = applyHeads.stream()
                    .collect(Collectors.toMap(BudgetLedgerHead::getBizCode, head -> head, (a, b) -> a));

            // 2. 查询bizType为APPLY的BUDGET_LEDGER_RECOVER_BAK，按bizCode分组放入map2
            LambdaQueryWrapper<BudgetLedgerRecoverBak> applyLedgerWrapper = new LambdaQueryWrapper<>();
            applyLedgerWrapper.eq(BudgetLedgerRecoverBak::getBizType, "APPLY")
                    .eq(BudgetLedgerRecoverBak::getDeleted, Boolean.FALSE);
            List<BudgetLedgerRecoverBak> applyLedgersBak = budgetLedgerRecoverBakMapper.selectList(applyLedgerWrapper);
            // 转换为原实体类型
            List<BudgetLedger> applyLedgers = applyLedgersBak.stream().map(this::convertLedgerBakToLedger).collect(Collectors.toList());
            Map<String, List<BudgetLedger>> map2 = applyLedgers.stream()
                    .collect(Collectors.groupingBy(BudgetLedger::getBizCode));

            // 3. 查询bizType为CLAIM的BUDGET_LEDGER_HEAD_RECOVER_BAK，放入map3
            LambdaQueryWrapper<BudgetLedgerHeadRecoverBak> claimHeadWrapper = new LambdaQueryWrapper<>();
            claimHeadWrapper.eq(BudgetLedgerHeadRecoverBak::getBizType, "CLAIM")
                    .eq(BudgetLedgerHeadRecoverBak::getDeleted, Boolean.FALSE);
            List<BudgetLedgerHeadRecoverBak> claimHeadsBak = budgetLedgerHeadRecoverBakMapper.selectList(claimHeadWrapper);
            // 转换为原实体类型
            List<BudgetLedgerHead> claimHeads = claimHeadsBak.stream().map(this::convertHeadBakToHead).collect(Collectors.toList());
            Map<String, BudgetLedgerHead> map3 = claimHeads.stream()
                    .collect(Collectors.toMap(BudgetLedgerHead::getBizCode, head -> head, (a, b) -> a));

            // 4. 查询bizType为CLAIM的BUDGET_LEDGER_RECOVER_BAK，按bizCode分组放入map4
            LambdaQueryWrapper<BudgetLedgerRecoverBak> claimLedgerWrapper = new LambdaQueryWrapper<>();
            claimLedgerWrapper.eq(BudgetLedgerRecoverBak::getBizType, "CLAIM")
                    .eq(BudgetLedgerRecoverBak::getDeleted, Boolean.FALSE);
            List<BudgetLedgerRecoverBak> claimLedgersBak = budgetLedgerRecoverBakMapper.selectList(claimLedgerWrapper);
            // 转换为原实体类型
            List<BudgetLedger> claimLedgers = claimLedgersBak.stream().map(this::convertLedgerBakToLedger).collect(Collectors.toList());
            Map<String, List<BudgetLedger>> map4 = claimLedgers.stream()
                    .collect(Collectors.groupingBy(BudgetLedger::getBizCode));

            // 5. 根据map1和map2组装BudgetApplicationParams，放入map5
            Map<String, BudgetApplicationParams> map5 = new HashMap<>();
            for (Map.Entry<String, BudgetLedgerHead> entry : map1.entrySet()) {
                String bizCode = entry.getKey();
                BudgetLedgerHead head = entry.getValue();
                List<BudgetLedger> ledgers = map2.getOrDefault(bizCode, Collections.emptyList());

                BudgetApplicationParams params = assembleBudgetApplicationParams(head, ledgers);
                map5.put(bizCode, params);
            }

            // 6. 根据map3和map4组装BudgetClaimApplyParams，放入map6
            Map<String, BudgetClaimApplyParams> map6 = new HashMap<>();
            for (Map.Entry<String, BudgetLedgerHead> entry : map3.entrySet()) {
                String bizCode = entry.getKey();
                BudgetLedgerHead head = entry.getValue();
                List<BudgetLedger> ledgers = map4.getOrDefault(bizCode, Collections.emptyList());

                BudgetClaimApplyParams params = assembleBudgetClaimApplyParams(head, ledgers);
                map6.put(bizCode, params);
            }

            // 7. 循环遍历map5，调用BudgetApplicationController的apply接口
            int map5SuccessCount = 0;
            int map5FailCount = 0;
            StringBuilder map5Result = new StringBuilder();
            for (Map.Entry<String, BudgetApplicationParams> entry : map5.entrySet()) {
                String bizCode = entry.getKey();
                BudgetApplicationParams params = entry.getValue();
                try {
                    log.info("========== 开始调用预算申请接口，bizCode={} ==========", bizCode);
                    BudgetApplicationRespVo respVo = budgetApplicationController.apply(params);
                    map5SuccessCount++;
                    log.info("========== 预算申请接口调用成功，bizCode={}, 响应={} ==========", bizCode, JsonUtils.toJsonString(respVo));
                    map5Result.append(String.format("bizCode=%s: 成功\n", bizCode));
                } catch (Exception e) {
                    map5FailCount++;
                    log.error("========== 预算申请接口调用失败，bizCode={} ==========", bizCode, e);
                    map5Result.append(String.format("bizCode=%s: 失败 - %s\n", bizCode, e.getMessage()));
                }
            }

            // 8. 循环遍历map6，调用BudgetClaimController的apply接口
            int map6SuccessCount = 0;
            int map6FailCount = 0;
            StringBuilder map6Result = new StringBuilder();
            for (Map.Entry<String, BudgetClaimApplyParams> entry : map6.entrySet()) {
                String bizCode = entry.getKey();
                BudgetClaimApplyParams params = entry.getValue();
                try {
                    log.info("========== 开始调用预算付款/报销接口，bizCode={} ==========", bizCode);
                    BudgetClaimRespVo respVo = budgetClaimController.apply(params);
                    map6SuccessCount++;
                    log.info("========== 预算付款/报销接口调用成功，bizCode={}, 响应={} ==========", bizCode, JsonUtils.toJsonString(respVo));
                    map6Result.append(String.format("bizCode=%s: 成功\n", bizCode));
                } catch (Exception e) {
                    map6FailCount++;
                    log.error("========== 预算付款/报销接口调用失败，bizCode={} ==========", bizCode, e);
                    map6Result.append(String.format("bizCode=%s: 失败 - %s\n", bizCode, e.getMessage()));
                }
            }

            // 9. 返回汇总结果
            String summary = String.format("组装完成，map5数量: %d (成功: %d, 失败: %d), map6数量: %d (成功: %d, 失败: %d)\n\n" +
                    "map5调用结果:\n%s\nmap6调用结果:\n%s",
                    map5.size(), map5SuccessCount, map5FailCount,
                    map6.size(), map6SuccessCount, map6FailCount,
                    map5Result.toString(), map6Result.toString());
            
            return CommonResult.success(summary);
        } catch (Exception e) {
            log.error("组装预算参数失败", e);
            return CommonResult.error("500", "组装失败: " + e.getMessage());
        }
    }

    /**
     * 组装BudgetApplicationParams
     */
    private BudgetApplicationParams assembleBudgetApplicationParams(BudgetLedgerHead head, List<BudgetLedger> ledgers) {
        BudgetApplicationParams params = new BudgetApplicationParams();

        // 组装ESBInfoParams
        ESBInfoParams esbInfo = ESBInfoParams.builder()
                .requestTime(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS")))
                .build();
        params.setEsbInfo(esbInfo);

        // 组装ApplyReqInfoParams
        ApplyReqInfoParams applyReqInfo = new ApplyReqInfoParams();
        applyReqInfo.setDemandOrderNo(head.getBizCode());
        applyReqInfo.setDocumentName(head.getDocumentName());
        applyReqInfo.setDataSource(head.getDataSource());
        applyReqInfo.setDocumentStatus("INITIAL_SUBMITTED");
        applyReqInfo.setOperator(head.getCreator());
        applyReqInfo.setOperateTime(head.getCreateTime() != null 
                ? head.getCreateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) 
                : LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));

        // 计算总投资额
        BigDecimal totalInvestmentAmount = ledgers.stream()
                .map(BudgetLedger::getAmount)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        applyReqInfo.setTotalInvestmentAmount(totalInvestmentAmount);

        // 组装明细列表
        List<ApplyDetailDetalVo> demandDetails = new ArrayList<>();
        for (BudgetLedger ledger : ledgers) {
            ApplyDetailDetalVo detail = new ApplyDetailDetalVo();
            detail.setDemandYear(ledger.getYear());
            detail.setDemandMonth(ledger.getMonth());
            detail.setManagementOrg(ledger.getMorgCode());
            // 科目如果为NAN-NAN，设置为空
            String budgetSubjectCode = ledger.getBudgetSubjectCode();
            detail.setBudgetSubjectCode("NAN-NAN".equals(budgetSubjectCode) ? null : budgetSubjectCode);
            // 项目如果为NAN，设置为空
            String masterProjectCode = ledger.getMasterProjectCode();
            detail.setMasterProjectCode("NAN".equals(masterProjectCode) ? null : masterProjectCode);
            // 资产类型如果为NAN，设置为空
            String erpAssetType = ledger.getErpAssetType();
            detail.setErpAssetType("NAN".equals(erpAssetType) ? null : erpAssetType);
            detail.setIsInternal(ledger.getIsInternal());
            detail.setDemandAmount(ledger.getAmount());
            detail.setCurrency(ledger.getCurrency());
            
            // 解析metadata
            if (StringUtils.isNotBlank(ledger.getMetadata())) {
                Map<String, String> metadata = parseJsonToMap(ledger.getMetadata());
                detail.setMetadata(metadata);
            }

            demandDetails.add(detail);
        }
        applyReqInfo.setDemandDetails(demandDetails);

        params.setApplyReqInfo(applyReqInfo);
        return params;
    }

    /**
     * 组装BudgetClaimApplyParams
     */
    private BudgetClaimApplyParams assembleBudgetClaimApplyParams(BudgetLedgerHead head, List<BudgetLedger> ledgers) {
        BudgetClaimApplyParams params = new BudgetClaimApplyParams();

        // 组装ESBInfoParams
        ESBInfoParams esbInfo = ESBInfoParams.builder()
                .requestTime(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS")))
                .build();
        params.setEsbInfo(esbInfo);

        // 组装ClaimApplyReqInfoParams
        ClaimApplyReqInfoParams claimApplyReqInfo = new ClaimApplyReqInfoParams();
        claimApplyReqInfo.setClaimOrderNo(head.getBizCode());
        claimApplyReqInfo.setDocumentName(head.getDocumentName());
        claimApplyReqInfo.setDataSource(head.getDataSource());
        claimApplyReqInfo.setDocumentStatus("INITIAL_SUBMITTED");
        claimApplyReqInfo.setOperator(head.getCreator());
        claimApplyReqInfo.setOperateTime(head.getCreateTime() != null 
                ? head.getCreateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) 
                : LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));

        // 组装明细列表
        List<ClaimDetailDetailVo> claimDetails = new ArrayList<>();
        for (BudgetLedger ledger : ledgers) {
            ClaimDetailDetailVo detail = new ClaimDetailDetailVo();
            detail.setClaimYear(ledger.getYear());
            detail.setClaimMonth(ledger.getMonth());
            detail.setActualYear(ledger.getActualYear());
            detail.setActualMonth(ledger.getActualMonth());
            detail.setManagementOrg(ledger.getMorgCode());
            // 科目如果为NAN-NAN，设置为空
            String budgetSubjectCode = ledger.getBudgetSubjectCode();
            detail.setBudgetSubjectCode("NAN-NAN".equals(budgetSubjectCode) ? null : budgetSubjectCode);
            // 项目如果为NAN，设置为空
            String masterProjectCode = ledger.getMasterProjectCode();
            detail.setMasterProjectCode("NAN".equals(masterProjectCode) ? null : masterProjectCode);
            // 资产类型如果为NAN，设置为空
            String erpAssetType = ledger.getErpAssetType();
            detail.setErpAssetType("NAN".equals(erpAssetType) ? null : erpAssetType);
            detail.setIsInternal(ledger.getIsInternal());
            detail.setActualAmount(ledger.getAmount());
            detail.setCurrency(ledger.getCurrency());
            
            // 解析metadata
            if (StringUtils.isNotBlank(ledger.getMetadata())) {
                Map<String, String> metadata = parseJsonToMap(ledger.getMetadata());
                detail.setMetadata(metadata);
            }

            claimDetails.add(detail);
        }
        claimApplyReqInfo.setClaimDetails(claimDetails);

        params.setClaimApplyReqInfo(claimApplyReqInfo);
        return params;
    }

    /**
     * 将 JSON 字符串解析为 Map<String, String>
     */
    private Map<String, String> parseJsonToMap(String json) {
        Map<String, String> map = new HashMap<>();
        if (StringUtils.isBlank(json)) {
            return map;
        }
        // 简单的 JSON 解析（只处理简单的 key-value 对）
        json = json.trim();
        if (json.startsWith("{") && json.endsWith("}")) {
            json = json.substring(1, json.length() - 1);
            String[] pairs = json.split(",");
            for (String pair : pairs) {
                String[] kv = pair.split(":", 2);
                if (kv.length == 2) {
                    String key = kv[0].trim().replaceAll("^\"|\"$", "");
                    String value = kv[1].trim().replaceAll("^\"|\"$", "");
                    // 反转义 JSON 特殊字符
                    value = unescapeJson(value);
                    map.put(key, value);
                }
            }
        }
        return map;
    }

    /**
     * 反转义 JSON 字符串中的特殊字符
     */
    private String unescapeJson(String str) {
        if (str == null) {
            return "";
        }
        return str.replace("\\t", "\t")
                .replace("\\r", "\r")
                .replace("\\n", "\n")
                .replace("\\\"", "\"")
                .replace("\\\\", "\\");
    }

    /**
     * 将备份表实体转换为原实体
     */
    private BudgetLedgerHead convertHeadBakToHead(BudgetLedgerHeadRecoverBak bak) {
        BudgetLedgerHead head = new BudgetLedgerHead();
        BeanUtils.copyProperties(bak, head);
        return head;
    }

    /**
     * 将备份表实体转换为原实体
     */
    private BudgetLedger convertLedgerBakToLedger(BudgetLedgerRecoverBak bak) {
        BudgetLedger ledger = new BudgetLedger();
        BeanUtils.copyProperties(bak, ledger);
        return ledger;
    }

    /**
     * 处理已归档的单号，查询BUDGET_API_REQUEST_LOG表中对应的apply请求参数
     * 对于每个单号，如果有多个METHOD_NAME为apply的记录，取UPDATE_TIME最晚的那一个
     * 然后组装成申请单参数，处理NAN-NAN和NAN值，调用申请接口和审批通过接口
     */
    @Operation(summary = "处理已归档单号的apply请求参数并重新提交")
    @GetMapping(value = "/processArchivedOrderNos")
    public CommonResult<String> processArchivedOrderNos() {
        try {
            // 已归档的单号列表
            List<String> archivedOrderNos = Arrays.asList(
                    "3891850", "3892654", "3892956", "3893076", "3893120"
            );

            StringBuilder result = new StringBuilder();
            result.append("========== 已归档单号apply请求参数处理结果 ==========\n\n");

            int successCount = 0;
            int failCount = 0;

            for (String orderNo : archivedOrderNos) {
                result.append(String.format("【单号：%s】\n", orderNo));

                try {
                    // 查询REQUEST_PARAMS中包含该单号且METHOD_NAME为apply的记录
                    // 由于REQUEST_PARAMS是CLOB类型，需要使用DBMS_LOB.INSTR进行查询
                    LambdaQueryWrapper<BudgetApiRequestLog> wrapper = new LambdaQueryWrapper<>();
                    wrapper.eq(BudgetApiRequestLog::getMethodName, "apply")
                            .eq(BudgetApiRequestLog::getDeleted, Boolean.FALSE)
                            // 使用apply方法添加Oracle CLOB字段的查询条件
                            .apply("DBMS_LOB.INSTR(REQUEST_PARAMS, {0}, 1, 1) > 0", orderNo)
                            .orderByDesc(BudgetApiRequestLog::getUpdateTime);

                    List<BudgetApiRequestLog> logs = budgetApiRequestLogMapper.selectList(wrapper);

                    if (logs.isEmpty()) {
                        result.append("  未找到对应的apply请求记录\n");
                        failCount++;
                    } else {
                        // 取UPDATE_TIME最晚的那一个（由于已经按UPDATE_TIME降序排列，第一个就是最新的）
                        BudgetApiRequestLog latestLog = logs.get(0);
                        result.append(String.format("  找到 %d 条记录，使用UPDATE_TIME最晚的记录\n", logs.size()));

                        // 解析JSON请求参数
                        String requestParamsJson = latestLog.getRequestParams();
                        if (StringUtils.isBlank(requestParamsJson)) {
                            result.append("  请求参数为空，跳过处理\n");
                            failCount++;
                            continue;
                        }

                        // 解析JSON为BudgetApplicationParams
                        BudgetApplicationParams params = JsonUtils.parseObject(requestParamsJson, BudgetApplicationParams.class);
                        if (params == null || params.getApplyReqInfo() == null) {
                            result.append("  解析请求参数失败，跳过处理\n");
                            failCount++;
                            continue;
                        }

                        // 处理NAN-NAN和NAN值
                        processNanValues(params);

                        // 更新ESB信息中的请求时间
                        if (params.getEsbInfo() != null) {
                            params.getEsbInfo().setRequestTime(
                                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS"))
                            );
                        }

                        // 调用预算申请接口
                        result.append("  开始调用预算申请接口...\n");
                        log.info("========== 开始调用预算申请接口，单号={} ==========", orderNo);
                        BudgetApplicationRespVo applyResp = budgetApplicationController.apply(params);
                        result.append(String.format("  预算申请接口调用成功，响应：%s\n", 
                                JsonUtils.toJsonString(applyResp)));

                        // 调用审批通过接口
                        result.append("  开始调用审批通过接口...\n");
                        log.info("========== 开始调用审批通过接口，单号={} ==========", orderNo);
                        
                        BudgetRenewParams renewParams = new BudgetRenewParams();
                        // 组装ESB信息
                        ESBInfoParams esbInfo = ESBInfoParams.builder()
                                .requestTime(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS")))
                                .build();
                        renewParams.setEsbInfo(esbInfo);
                        
                        // 组装审批信息
                        RenewApplyReqInfoParams renewApplyReqInfo = new RenewApplyReqInfoParams();
                        renewApplyReqInfo.setDemandOrderNo(orderNo);
                        renewApplyReqInfo.setDocumentStatus("APPROVED");
                        renewApplyReqInfo.setApprover("系统自动审批");
                        renewApplyReqInfo.setApproveTime(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
                        renewApplyReqInfo.setApproveComment("已归档单据自动审批通过");
                        renewParams.setRenewApplyReqInfo(renewApplyReqInfo);
                        
                        BudgetRenewRespVo renewResp = budgetApplicationController.authOrCancel(renewParams);
                        result.append(String.format("  审批通过接口调用成功，响应：%s\n", 
                                JsonUtils.toJsonString(renewResp)));
                        
                        result.append("  ✓ 处理成功\n");
                        successCount++;
                    }
                } catch (Exception e) {
                    result.append(String.format("  ✗ 处理失败：%s\n", e.getMessage()));
                    log.error("处理单号 {} 失败", orderNo, e);
                    failCount++;
                }
                result.append("\n");
            }

            result.append(String.format("========== 处理完成 ==========\n"));
            result.append(String.format("成功：%d 个，失败：%d 个\n", successCount, failCount));

            log.info("已归档单号apply请求参数处理完成，成功：{}，失败：{}", successCount, failCount);
            return CommonResult.success(result.toString());
        } catch (Exception e) {
            log.error("处理已归档单号失败", e);
            return CommonResult.error("500", "处理失败: " + e.getMessage());
        }
    }

    /**
     * 处理申请参数中的NAN-NAN和NAN值
     * 科目编码如果为NAN-NAN要变为空
     * 项目编码如果为NAN要变为空
     * 资产类型编码如果为NAN要变为空
     */
    private void processNanValues(BudgetApplicationParams params) {
        if (params == null || params.getApplyReqInfo() == null 
                || params.getApplyReqInfo().getDemandDetails() == null) {
            return;
        }

        List<ApplyDetailDetalVo> details = params.getApplyReqInfo().getDemandDetails();
        for (ApplyDetailDetalVo detail : details) {
            // 科目编码如果为NAN-NAN，设置为空
            if ("NAN-NAN".equals(detail.getBudgetSubjectCode())) {
                detail.setBudgetSubjectCode(null);
            }
            // 项目编码如果为NAN，设置为空
            if ("NAN".equals(detail.getMasterProjectCode())) {
                detail.setMasterProjectCode(null);
            }
            // 资产类型编码如果为NAN，设置为空
            if ("NAN".equals(detail.getErpAssetType())) {
                detail.setErpAssetType(null);
            }
        }
    }

    /**
     * 处理未归档的单号，根据类型执行对应的申请单处理
     * 参数：APPLY-预算申请单，CONTRACT-合同申请单，CLAIM-付款申请单
     * 对于每个单号，如果有多个METHOD_NAME为apply的记录，取UPDATE_TIME最晚的那一个
     * 处理NAN-NAN和NAN值，组装参数并调用接口，统计业务异常
     */
    @Operation(summary = "处理未归档单号并执行对应类型的申请单")
    @GetMapping(value = "/processUnarchivedOrderNos")
    public CommonResult<String> processUnarchivedOrderNos(@RequestParam String type) {
        try {
            // 验证参数
            if (StringUtils.isBlank(type) || 
                (!"APPLY".equalsIgnoreCase(type) && !"CONTRACT".equalsIgnoreCase(type) && !"CLAIM".equalsIgnoreCase(type))) {
                return CommonResult.error("400", "参数错误，type必须为APPLY、CONTRACT或CLAIM");
            }

            // 未归档的单号列表
            List<String> unarchivedOrderNos = Arrays.asList(
                    "3890436", "3891554", "3891588", "3891672", "3891738",
                    "3891830", "3892103", "3892508", "3892821", "3893080",
                    "3893193", "3893363", "3893402", "3893514", "3893858",
                    "3894122", "3894142", "3894158", "3894244", "3894361",
                    "3895443", "3895587", "3895592", "3895689", "3895690",
                    "3895693", "3895736", "3895836", "3896092", "3896191",
                    "3896694", "3899128", "3899370", "3899508"
            );

            StringBuilder result = new StringBuilder();
            String typeName = "APPLY".equalsIgnoreCase(type) ? "预算申请单" : 
                             "CONTRACT".equalsIgnoreCase(type) ? "合同申请单" : "付款申请单";
            result.append(String.format("========== 未归档单号处理结果（%s） ==========\n\n", typeName));

            // 先归类筛选出对应类型的单号
            List<String> targetOrders = new ArrayList<>();
            for (String orderNo : unarchivedOrderNos) {
                try {
                    LambdaQueryWrapper<BudgetApiRequestLog> wrapper = new LambdaQueryWrapper<>();
                    wrapper.eq(BudgetApiRequestLog::getMethodName, "apply")
                            .eq(BudgetApiRequestLog::getDeleted, Boolean.FALSE)
                            .apply("DBMS_LOB.INSTR(REQUEST_PARAMS, {0}, 1, 1) > 0", orderNo)
                            .orderByDesc(BudgetApiRequestLog::getUpdateTime);

                    List<BudgetApiRequestLog> logs = budgetApiRequestLogMapper.selectList(wrapper);
                    if (logs.isEmpty()) {
                        continue;
                    }

                    BudgetApiRequestLog latestLog = logs.get(0);
                    String requestParamsJson = latestLog.getRequestParams();
                    if (StringUtils.isBlank(requestParamsJson)) {
                        continue;
                    }

                    // 根据类型筛选
                    boolean isMatch = false;
                    if ("APPLY".equalsIgnoreCase(type)) {
                        isMatch = requestParamsJson.contains("applyReqInfo");
                    } else if ("CONTRACT".equalsIgnoreCase(type)) {
                        isMatch = requestParamsJson.contains("contractApplyReqInfo");
                    } else if ("CLAIM".equalsIgnoreCase(type)) {
                        isMatch = requestParamsJson.contains("claimApplyReqInfo");
                    }

                    if (isMatch) {
                        targetOrders.add(orderNo);
                    }
                } catch (Exception e) {
                    log.error("筛选单号 {} 失败", orderNo, e);
                }
            }

            result.append(String.format("筛选出 %s：%d 个\n\n", typeName, targetOrders.size()));

            // 统计结果
            int successCount = 0;
            int failCount = 0;
            List<String> successOrders = new ArrayList<>();
            List<String> failOrders = new ArrayList<>();
            Map<String, String> failReasons = new HashMap<>();

            // 处理每个单号
            for (String orderNo : targetOrders) {
                result.append(String.format("【单号：%s】\n", orderNo));
                try {
                    // 查询请求参数
                    LambdaQueryWrapper<BudgetApiRequestLog> wrapper = new LambdaQueryWrapper<>();
                    wrapper.eq(BudgetApiRequestLog::getMethodName, "apply")
                            .eq(BudgetApiRequestLog::getDeleted, Boolean.FALSE)
                            .apply("DBMS_LOB.INSTR(REQUEST_PARAMS, {0}, 1, 1) > 0", orderNo)
                            .orderByDesc(BudgetApiRequestLog::getUpdateTime);

                    List<BudgetApiRequestLog> logs = budgetApiRequestLogMapper.selectList(wrapper);
                    if (logs.isEmpty()) {
                        result.append("  未找到对应的apply请求记录\n");
                        failCount++;
                        failOrders.add(orderNo);
                        failReasons.put(orderNo, "未找到对应的apply请求记录");
                        continue;
                    }

                    BudgetApiRequestLog latestLog = logs.get(0);
                    String requestParamsJson = latestLog.getRequestParams();
                    if (StringUtils.isBlank(requestParamsJson)) {
                        result.append("  请求参数为空\n");
                        failCount++;
                        failOrders.add(orderNo);
                        failReasons.put(orderNo, "请求参数为空");
                        continue;
                    }

                    // 根据类型解析并处理
                    if ("APPLY".equalsIgnoreCase(type)) {
                        processBudgetApplication(orderNo, requestParamsJson, result, successOrders, failOrders, failReasons);
                    } else if ("CONTRACT".equalsIgnoreCase(type)) {
                        processContractApplication(orderNo, requestParamsJson, result, successOrders, failOrders, failReasons);
                    } else if ("CLAIM".equalsIgnoreCase(type)) {
                        processClaimApplication(orderNo, requestParamsJson, result, successOrders, failOrders, failReasons);
                    }

                    if (successOrders.contains(orderNo)) {
                        successCount++;
                    } else {
                        failCount++;
                    }
                } catch (Exception e) {
                    result.append(String.format("  处理失败：%s\n", e.getMessage()));
                    log.error("处理单号 {} 失败", orderNo, e);
                    failCount++;
                    failOrders.add(orderNo);
                    failReasons.put(orderNo, e.getMessage());
                }
                result.append("\n");
            }

            // 输出统计结果
            result.append(String.format("========== 处理完成 ==========\n"));
            result.append(String.format("总单号数：%d 个\n", targetOrders.size()));
            result.append(String.format("成功：%d 个\n", successCount));
            result.append(String.format("失败：%d 个\n", failCount));

            if (!successOrders.isEmpty()) {
                result.append(String.format("\n成功单号列表：\n"));
                for (String orderNo : successOrders) {
                    result.append(String.format("  %s\n", orderNo));
                }
            }

            if (!failOrders.isEmpty()) {
                result.append(String.format("\n失败单号列表：\n"));
                for (String orderNo : failOrders) {
                    result.append(String.format("  %s - %s\n", orderNo, failReasons.getOrDefault(orderNo, "未知错误")));
                }
            }

            log.info("未归档单号处理完成，类型：{}，成功：{}，失败：{}", type, successCount, failCount);
            return CommonResult.success(result.toString());
        } catch (Exception e) {
            log.error("处理未归档单号失败", e);
            return CommonResult.error("500", "处理失败: " + e.getMessage());
        }
    }

    /**
     * 处理预算申请单
     */
    private void processBudgetApplication(String orderNo, String requestParamsJson, StringBuilder result,
                                          List<String> successOrders, List<String> failOrders, Map<String, String> failReasons) {
        try {
            BudgetApplicationParams params = JsonUtils.parseObject(requestParamsJson, BudgetApplicationParams.class);
            if (params == null || params.getApplyReqInfo() == null) {
                result.append("  解析请求参数失败\n");
                failOrders.add(orderNo);
                failReasons.put(orderNo, "解析请求参数失败");
                return;
            }

            // 处理NAN-NAN和NAN值
            processNanValues(params);

            // 更新ESB信息中的请求时间
            if (params.getEsbInfo() != null) {
                params.getEsbInfo().setRequestTime(
                        LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS"))
                );
            }

            // 调用预算申请接口
            result.append("  开始调用预算申请接口...\n");
            log.info("========== 开始调用预算申请接口，单号={} ==========", orderNo);
            BudgetApplicationRespVo resp = budgetApplicationController.apply(params);
            
            // 检查响应是否有业务异常
            if (resp != null && resp.getEsbInfo() != null) {
                String returnStatus = resp.getEsbInfo().getReturnStatus();
                String returnCode = resp.getEsbInfo().getReturnCode();
                if (!"S".equals(returnStatus) || (returnCode != null && returnCode.startsWith("E"))) {
                    String errorMsg = resp.getEsbInfo().getReturnMsg();
                    result.append(String.format("  业务异常：%s\n", errorMsg));
                    failOrders.add(orderNo);
                    failReasons.put(orderNo, errorMsg != null ? errorMsg : "业务处理失败");
                    return;
                }
            }

            result.append("  预算申请接口调用成功\n");
            successOrders.add(orderNo);
        } catch (Exception e) {
            result.append(String.format("  调用接口失败：%s\n", e.getMessage()));
            failOrders.add(orderNo);
            failReasons.put(orderNo, e.getMessage());
            throw e;
        }
    }

    /**
     * 处理合同申请单
     */
    private void processContractApplication(String orderNo, String requestParamsJson, StringBuilder result,
                                          List<String> successOrders, List<String> failOrders, Map<String, String> failReasons) {
        try {
            BudgetContractApplyParams params = JsonUtils.parseObject(requestParamsJson, BudgetContractApplyParams.class);
            if (params == null || params.getContractApplyReqInfo() == null) {
                result.append("  解析请求参数失败\n");
                failOrders.add(orderNo);
                failReasons.put(orderNo, "解析请求参数失败");
                return;
            }

            // 处理NAN-NAN和NAN值
            processContractNanValues(params);

            // 更新ESB信息中的请求时间
            if (params.getEsbInfo() != null) {
                params.getEsbInfo().setRequestTime(
                        LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS"))
                );
            }

            // 调用合同申请接口
            result.append("  开始调用合同申请接口...\n");
            log.info("========== 开始调用合同申请接口，单号={} ==========", orderNo);
            BudgetContractRespVo resp = budgetContractController.apply(params);
            
            // 检查响应是否有业务异常
            if (resp != null && resp.getEsbInfo() != null) {
                String returnStatus = resp.getEsbInfo().getReturnStatus();
                String returnCode = resp.getEsbInfo().getReturnCode();
                if (!"S".equals(returnStatus) || (returnCode != null && returnCode.startsWith("E"))) {
                    String errorMsg = resp.getEsbInfo().getReturnMsg();
                    result.append(String.format("  业务异常：%s\n", errorMsg));
                    failOrders.add(orderNo);
                    failReasons.put(orderNo, errorMsg != null ? errorMsg : "业务处理失败");
                    return;
                }
            }

            result.append("  合同申请接口调用成功\n");
            successOrders.add(orderNo);
        } catch (Exception e) {
            result.append(String.format("  调用接口失败：%s\n", e.getMessage()));
            failOrders.add(orderNo);
            failReasons.put(orderNo, e.getMessage());
            throw e;
        }
    }

    /**
     * 处理付款申请单
     */
    private void processClaimApplication(String orderNo, String requestParamsJson, StringBuilder result,
                                        List<String> successOrders, List<String> failOrders, Map<String, String> failReasons) {
        try {
            BudgetClaimApplyParams params = JsonUtils.parseObject(requestParamsJson, BudgetClaimApplyParams.class);
            if (params == null || params.getClaimApplyReqInfo() == null) {
                result.append("  解析请求参数失败\n");
                failOrders.add(orderNo);
                failReasons.put(orderNo, "解析请求参数失败");
                return;
            }

            // 处理NAN-NAN和NAN值
            processClaimNanValues(params);

            // 更新ESB信息中的请求时间
            if (params.getEsbInfo() != null) {
                params.getEsbInfo().setRequestTime(
                        LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS"))
                );
            }

            // 调用付款申请接口
            result.append("  开始调用付款申请接口...\n");
            log.info("========== 开始调用付款申请接口，单号={} ==========", orderNo);
            BudgetClaimRespVo resp = budgetClaimController.apply(params);
            
            // 检查响应是否有业务异常
            if (resp != null && resp.getEsbInfo() != null) {
                String returnStatus = resp.getEsbInfo().getReturnStatus();
                String returnCode = resp.getEsbInfo().getReturnCode();
                if (!"S".equals(returnStatus) || (returnCode != null && returnCode.startsWith("E"))) {
                    String errorMsg = resp.getEsbInfo().getReturnMsg();
                    result.append(String.format("  业务异常：%s\n", errorMsg));
                    failOrders.add(orderNo);
                    failReasons.put(orderNo, errorMsg != null ? errorMsg : "业务处理失败");
                    return;
                }
            }

            result.append("  付款申请接口调用成功\n");
            successOrders.add(orderNo);
        } catch (Exception e) {
            result.append(String.format("  调用接口失败：%s\n", e.getMessage()));
            failOrders.add(orderNo);
            failReasons.put(orderNo, e.getMessage());
            throw e;
        }
    }

    /**
     * 处理合同申请参数中的NAN-NAN和NAN值
     */
    private void processContractNanValues(BudgetContractApplyParams params) {
        if (params == null || params.getContractApplyReqInfo() == null 
                || params.getContractApplyReqInfo().getContractDetails() == null) {
            return;
        }

        List<ContractDetailDetailVo> details = params.getContractApplyReqInfo().getContractDetails();
        for (ContractDetailDetailVo detail : details) {
            // 科目编码如果为NAN-NAN，设置为空
            if ("NAN-NAN".equals(detail.getBudgetSubjectCode())) {
                detail.setBudgetSubjectCode(null);
            }
            // 项目编码如果为NAN，设置为空
            if ("NAN".equals(detail.getMasterProjectCode())) {
                detail.setMasterProjectCode(null);
            }
            // 资产类型编码如果为NAN，设置为空
            if ("NAN".equals(detail.getErpAssetType())) {
                detail.setErpAssetType(null);
            }
        }
    }

    /**
     * 处理付款申请参数中的NAN-NAN和NAN值
     */
    private void processClaimNanValues(BudgetClaimApplyParams params) {
        if (params == null || params.getClaimApplyReqInfo() == null 
                || params.getClaimApplyReqInfo().getClaimDetails() == null) {
            return;
        }

        List<ClaimDetailDetailVo> details = params.getClaimApplyReqInfo().getClaimDetails();
        for (ClaimDetailDetailVo detail : details) {
            // 科目编码如果为NAN-NAN，设置为空
            if ("NAN-NAN".equals(detail.getBudgetSubjectCode())) {
                detail.setBudgetSubjectCode(null);
            }
            // 项目编码如果为NAN，设置为空
            if ("NAN".equals(detail.getMasterProjectCode())) {
                detail.setMasterProjectCode(null);
            }
            // 资产类型编码如果为NAN，设置为空
            if ("NAN".equals(detail.getErpAssetType())) {
                detail.setErpAssetType(null);
            }
        }
    }

    /**
     * 查询并组装预算调整申请参数
     * 从 BUDGET_LEDGER_HEAD_FOR_OPERATE 和 BUDGET_LEDGER_FOR_OPERATE 表中读取 BIZ_TYPE 为 ADJUST 的数据
     * 组装成 BudgetAdjustApplyParams 并调用 apply 接口
     * 如果状态为 APPROVED，还会调用 authOrCancel 接口
     */
    @Operation(summary = "查询并组装预算调整申请参数")
    @GetMapping(value = "/assembleBudgetAdjustParams")
    public CommonResult<String> assembleBudgetAdjustParams() {
        try {
            // 1. 查询已落库的调整单号（从 BUDGET_LEDGER_HEAD 表查询 BIZ_TYPE 为 ADJUST 的单号）
            LambdaQueryWrapper<BudgetLedgerHead> existingHeadWrapper = new LambdaQueryWrapper<>();
            existingHeadWrapper.eq(BudgetLedgerHead::getBizType, "ADJUST")
                    .eq(BudgetLedgerHead::getDeleted, Boolean.FALSE);
            List<BudgetLedgerHead> existingHeads = budgetLedgerHeadMapper.selectList(existingHeadWrapper);
            Set<String> existingBizCodes = existingHeads.stream()
                    .map(BudgetLedgerHead::getBizCode)
                    .collect(Collectors.toSet());
            log.info("========== 查询到已落库的调整单号，共 {} 个 ==========", existingBizCodes.size());

            // 2. 查询 BUDGET_LEDGER_HEAD_FOR_OPERATE 表中 BIZ_TYPE 为 ADJUST 的数据，放入 map1
            LambdaQueryWrapper<BudgetLedgerHeadForOperate> adjustHeadWrapper = new LambdaQueryWrapper<>();
            adjustHeadWrapper.eq(BudgetLedgerHeadForOperate::getBizType, "ADJUST")
                    .eq(BudgetLedgerHeadForOperate::getDeleted, Boolean.FALSE);
            List<BudgetLedgerHeadForOperate> adjustHeads = budgetLedgerHeadForOperateMapper.selectList(adjustHeadWrapper);
            // 过滤掉已落库的单号
            List<BudgetLedgerHeadForOperate> adjustHeadsToProcess = adjustHeads.stream()
                    .filter(head -> !existingBizCodes.contains(head.getBizCode()))
                    .collect(Collectors.toList());
            Map<String, BudgetLedgerHeadForOperate> map1 = adjustHeadsToProcess.stream()
                    .collect(Collectors.toMap(BudgetLedgerHeadForOperate::getBizCode, head -> head, (a, b) -> a));
            log.info("========== 查询到需要处理的调整单头，共 {} 个（已过滤已落库的 {} 个）==========", 
                    map1.size(), adjustHeads.size() - map1.size());

            // 3. 查询 BUDGET_LEDGER_FOR_OPERATE 表中 BIZ_TYPE 为 ADJUST 的数据，按 bizCode 分组放入 map2
            LambdaQueryWrapper<BudgetLedgerForOperate> adjustLedgerWrapper = new LambdaQueryWrapper<>();
            adjustLedgerWrapper.eq(BudgetLedgerForOperate::getBizType, "ADJUST")
                    .eq(BudgetLedgerForOperate::getDeleted, Boolean.FALSE);
            List<BudgetLedgerForOperate> adjustLedgers = budgetLedgerForOperateMapper.selectList(adjustLedgerWrapper);
            // 只保留 map1 中存在的单号的明细
            Map<String, List<BudgetLedgerForOperate>> map2 = adjustLedgers.stream()
                    .filter(ledger -> map1.containsKey(ledger.getBizCode()))
                    .collect(Collectors.groupingBy(BudgetLedgerForOperate::getBizCode));
            log.info("========== 查询到需要处理的调整单明细，共 {} 个单号 ==========", map2.size());

            // 4. 根据 map1 和 map2 组装 BudgetAdjustApplyParams，放入 map3
            Map<String, BudgetAdjustApplyParams> map3 = new HashMap<>();
            for (Map.Entry<String, BudgetLedgerHeadForOperate> entry : map1.entrySet()) {
                String bizCode = entry.getKey();
                BudgetLedgerHeadForOperate head = entry.getValue();
                List<BudgetLedgerForOperate> ledgers = map2.getOrDefault(bizCode, Collections.emptyList());

                BudgetAdjustApplyParams params = assembleBudgetAdjustApplyParams(head, ledgers);
                map3.put(bizCode, params);
            }
            log.info("========== 组装完成，共 {} 个调整单参数 ==========", map3.size());

            // 5. 循环遍历 map3，调用 BudgetAdjustController 的 apply 接口
            int successCount = 0;
            int failCount = 0;
            StringBuilder result = new StringBuilder();
            result.append("========== 预算调整申请处理结果 ==========\n\n");

            for (Map.Entry<String, BudgetAdjustApplyParams> entry : map3.entrySet()) {
                String bizCode = entry.getKey();
                BudgetAdjustApplyParams params = entry.getValue();
                BudgetLedgerHeadForOperate head = map1.get(bizCode);

                result.append(String.format("【调整单号：%s】\n", bizCode));
                try {
                    log.info("========== 开始调用预算调整申请接口，bizCode={} ==========", bizCode);
                    BudgetAdjustRespVo applyResp = budgetAdjustController.apply(params);
                    
                    // 检查响应是否有业务异常
                    if (applyResp != null && applyResp.getEsbInfo() != null) {
                        String returnStatus = applyResp.getEsbInfo().getReturnStatus();
                        String returnCode = applyResp.getEsbInfo().getReturnCode();
                        if (!"S".equals(returnStatus) || (returnCode != null && returnCode.startsWith("E"))) {
                            String errorMsg = applyResp.getEsbInfo().getReturnMsg();
                            result.append(String.format("  业务异常：%s\n", errorMsg));
                            throw new IllegalStateException("预算调整申请业务异常：" + (errorMsg != null ? errorMsg : "业务处理失败"));
                        }
                    }
                    
                    result.append("  预算调整申请接口调用成功\n");
                    log.info("========== 预算调整申请接口调用成功，bizCode={}, 响应={} ==========", 
                            bizCode, JsonUtils.toJsonString(applyResp));

                    // 6. 如果状态为 APPROVED，调用 authOrCancel 接口
                    if ("APPROVED".equals(head.getStatus())) {
                        result.append("  开始调用审批通过接口...\n");
                        log.info("========== 开始调用审批通过接口，bizCode={} ==========", bizCode);
                        
                        BudgetAdjustRenewParams renewParams = new BudgetAdjustRenewParams();
                        // 组装ESB信息
                        ESBInfoParams esbInfo = ESBInfoParams.builder()
                                .requestTime(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS")))
                                .build();
                        renewParams.setEsbInfo(esbInfo);
                        
                        // 组装审批信息
                        AdjustRenewReqInfoParams renewApplyReqInfo = new AdjustRenewReqInfoParams();
                        renewApplyReqInfo.setAdjustOrderNo(bizCode);
                        renewApplyReqInfo.setDocumentStatus("APPROVED");
                        renewApplyReqInfo.setApprover(head.getOperator() != null ? head.getOperator() : "系统自动审批");
                        renewApplyReqInfo.setApproveTime(head.getUpdateTime() != null 
                                ? head.getUpdateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) 
                                : LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
                        renewApplyReqInfo.setApproveComment("从操作表自动审批通过");
                        renewParams.setAdjustRenewReqInfo(renewApplyReqInfo);
                        
                        BudgetAdjustRenewRespVo renewResp = budgetAdjustController.authOrCancel(renewParams);
                        
                        // 检查响应是否有业务异常
                        if (renewResp != null && renewResp.getEsbInfo() != null) {
                            String returnStatus = renewResp.getEsbInfo().getReturnStatus();
                            String returnCode = renewResp.getEsbInfo().getReturnCode();
                            if (!"S".equals(returnStatus) || (returnCode != null && returnCode.startsWith("E"))) {
                                String errorMsg = renewResp.getEsbInfo().getReturnMsg();
                                result.append(String.format("  审批接口业务异常：%s\n", errorMsg));
                                throw new IllegalStateException("预算调整审批业务异常：" + (errorMsg != null ? errorMsg : "业务处理失败"));
                            }
                        }
                        
                        result.append("  审批通过接口调用成功\n");
                        log.info("========== 审批通过接口调用成功，bizCode={}, 响应={} ==========", 
                                bizCode, JsonUtils.toJsonString(renewResp));
                    }
                    
                    result.append("  ✓ 处理成功\n");
                    successCount++;
                } catch (Exception e) {
                    result.append(String.format("  ✗ 处理失败：%s\n", e.getMessage()));
                    log.error("========== 处理调整单 {} 失败 ==========", bizCode, e);
                    failCount++;
                    // 任何业务异常都要抛出，停止继续跑流程
                    throw new IllegalStateException("处理调整单 " + bizCode + " 失败: " + e.getMessage(), e);
                }
                result.append("\n");
            }

            // 7. 返回汇总结果
            String summary = String.format("========== 处理完成 ==========\n" +
                    "总调整单数：%d 个\n" +
                    "成功：%d 个\n" +
                    "失败：%d 个\n\n" +
                    "处理详情：\n%s",
                    map3.size(), successCount, failCount, result.toString());
            
            log.info("预算调整申请处理完成，成功：{}，失败：{}", successCount, failCount);
            return CommonResult.success(summary);
        } catch (Exception e) {
            log.error("组装预算调整参数失败", e);
            return CommonResult.error("500", "处理失败: " + e.getMessage());
        }
    }

    /**
     * 从操作表批量跑单（GET，无文件）：仅支持从操作表读数据，与原有逻辑一致。
     * 可选传 bizType（ADJUST/APPLY/CONTRACT/CLAIM）仅跑该类型单据。
     */
    @Operation(summary = "从操作表批量跑单（GET，无 CSV 时使用）")
    @GetMapping(value = "/batchRunFromOperateTables")
    public CommonResult<String> batchRunFromOperateTablesGet(
            @RequestParam(value = "startFromBizCode", required = false) String startFromBizCode,
            @RequestParam(value = "bizType", required = false) String bizType) {
        return batchRunFromOperateTables(startFromBizCode, null, null, bizType);
    }

    /**
     * 从操作表批量跑单：按更新时间从早到晚顺序提交申请，状态为 APPROVED 的再走审批通过。
     * 入参 startFromBizCode 有值则只跑该单据号；不传则从第一个单据开始跑全部。
     * 入参 bizType 非必传；若传 ADJUST、APPLY、CONTRACT 或 CLAIM，则仅跑该类型单据（如 ADJUST 只跑预算调整单，APPLY 只跑预算申请单等）；不传则按原逻辑跑全部类型。
     * 若传入 excelFile（Excel 表「提交不上去单据清单」），则按 Excel 逻辑：根据表中 BIZ_CODE 及处理方法重跑单据，并按五种处理方法更新参数后提交。
     * 若传入 file（CSV），则按 CSV 逻辑：从第二行起每行 BIZ_CODE、BIZ_TYPE、LEDGER_HEAD_STATUS、REQUEST_PARAMS，用 REQUEST_PARAMS 调申请接口，若 LEDGER_HEAD_STATUS=APPROVED 再调审批接口；不传 file 则保持原逻辑从操作表读数据。
     * 单个单据失败不中断，会继续处理后续单据，最后统一返回成功/失败汇总；若有失败则结果为 error，汇总中含各单详情。
     */
    @Operation(summary = "从操作表批量跑单（申请+审批），可选传 Excel/CSV 按清单或报文跑单")
    @PostMapping(value = "/batchRunFromOperateTables")
    public CommonResult<String> batchRunFromOperateTables(
            @RequestParam(value = "startFromBizCode", required = false) String startFromBizCode,
            @RequestPart(value = "file", required = false) MultipartFile file,
            @RequestPart(value = "excelFile", required = false) MultipartFile excelFile,
            @RequestParam(value = "bizType", required = false) String bizType) {
        try {
            // 若传入 Excel 文件（提交不上去单据清单），走 Excel 逻辑：按 BIZ_CODE 与处理方法重跑并更新参数
            if (excelFile != null && !excelFile.isEmpty()) {
                return batchRunFromExcel(excelFile);
            }
            // 若传入 CSV 文件，走 CSV 逻辑（按 REQUEST_PARAMS 报文执行申请，按 LEDGER_HEAD_STATUS 决定是否审批）
            if (file != null && !file.isEmpty()) {
                return batchRunFromCsv(file);
            }
            // 步骤一：从 BUDGET_LEDGER_HEAD_FOR_OPERATE 读取表单，按 UPDATED_TIME 升序（时间最早优先）；若传 bizType 则只查该类型
            LambdaQueryWrapper<BudgetLedgerHeadForOperate> headWrapper = new LambdaQueryWrapper<>();
            headWrapper.eq(BudgetLedgerHeadForOperate::getDeleted, Boolean.FALSE)
                    .orderByAsc(BudgetLedgerHeadForOperate::getUpdateTime);
            if (StringUtils.isNotBlank(bizType) && ("ADJUST".equals(bizType) || "APPLY".equals(bizType) || "CONTRACT".equals(bizType) || "CLAIM".equals(bizType))) {
                headWrapper.eq(BudgetLedgerHeadForOperate::getBizType, bizType);
            }
            List<BudgetLedgerHeadForOperate> headList = budgetLedgerHeadForOperateMapper.selectList(headWrapper);
            if (headList == null || headList.isEmpty()) {
                return CommonResult.success("无待处理单据");
            }

            // 若传入 startFromBizCode，则只跑该单据；否则从第一个开始跑全部
            int startIndex = 0;
            int endIndex = headList.size();
            if (StringUtils.isNotBlank(startFromBizCode)) {
                startIndex = -1;
                for (int i = 0; i < headList.size(); i++) {
                    if (startFromBizCode.equals(headList.get(i).getBizCode())) {
                        startIndex = i;
                        break;
                    }
                }
                if (startIndex < 0) {
                    return CommonResult.error("400", "未找到单据号: " + startFromBizCode);
                }
                endIndex = startIndex + 1;
            }

            StringBuilder result = new StringBuilder();
            int successCount = 0;
            int failCount = 0;
            boolean printResponse = StringUtils.isNotBlank(startFromBizCode);

            for (int i = startIndex; i < endIndex; i++) {
                BudgetLedgerHeadForOperate head = headList.get(i);
                String bizCode = head.getBizCode();
                String headBizType = head.getBizType();

                result.append(String.format("【%s】%s\n", bizCode, headBizType));

                try {
                    // 步骤二：按 bizCode 查询 BUDGET_LEDGER_FOR_OPERATE 明细（DELETED=0）
                    LambdaQueryWrapper<BudgetLedgerForOperate> ledgerWrapper = new LambdaQueryWrapper<>();
                    ledgerWrapper.eq(BudgetLedgerForOperate::getBizCode, bizCode)
                            .eq(BudgetLedgerForOperate::getDeleted, Boolean.FALSE);
                    List<BudgetLedgerForOperate> ledgers = budgetLedgerForOperateMapper.selectList(ledgerWrapper);

                    // 步骤三&四：按类型组装提交参数并调用申请接口；若返回的 esb 层包含「失败」则中断
                    if ("APPLY".equals(headBizType)) {
                        BudgetApplicationParams applyParams = assembleApplyParamsFromOperate(head, ledgers);
                        BudgetApplicationRespVo applyResp = budgetApplicationController.apply(applyParams);
                        if (printResponse && applyResp != null) {
                            result.append("  申请接口返回: ").append(JsonUtils.toJsonString(applyResp)).append("\n");
                        }
                        checkEsbFailureAndRecord(applyResp != null ? applyResp.getEsbInfo() : null, applyResp, bizCode, headBizType);
                    } else if ("CONTRACT".equals(headBizType)) {
                        BudgetContractApplyParams contractParams = assembleContractParamsFromOperate(head, ledgers);
                        BudgetContractRespVo contractResp = budgetContractController.apply(contractParams);
                        if (printResponse && contractResp != null) {
                            result.append("  申请接口返回: ").append(JsonUtils.toJsonString(contractResp)).append("\n");
                        }
                        checkEsbFailureAndRecord(contractResp != null ? contractResp.getEsbInfo() : null, contractResp, bizCode, headBizType);
                    } else if ("CLAIM".equals(headBizType)) {
                        BudgetClaimApplyParams claimParams = assembleClaimParamsFromOperate(head, ledgers);
                        BudgetClaimRespVo claimResp = budgetClaimController.apply(claimParams);
                        if (printResponse && claimResp != null) {
                            result.append("  申请接口返回: ").append(JsonUtils.toJsonString(claimResp)).append("\n");
                        }
                        checkEsbFailureAndRecord(claimResp != null ? claimResp.getEsbInfo() : null, claimResp, bizCode, headBizType);
                    } else if ("ADJUST".equals(headBizType)) {
                        BudgetAdjustApplyParams adjustParams = assembleAdjustParamsFromOperate(head, ledgers);
                        BudgetAdjustRespVo adjustResp = budgetAdjustController.apply(adjustParams);
                        if (printResponse && adjustResp != null) {
                            result.append("  申请接口返回: ").append(JsonUtils.toJsonString(adjustResp)).append("\n");
                        }
                        checkEsbFailureAndRecord(adjustResp != null ? adjustResp.getEsbInfo() : null, adjustResp, bizCode, headBizType);
                    } else {
                        result.append("  跳过未知类型\n\n");
                        saveBatchRunRecord(bizCode, headBizType, "FAILURE", "未知类型，已跳过");
                        continue;
                    }

                    // 步骤五&六：若状态为 APPROVED，再走审批通过；若返回的 esb 层包含「失败」则中断
                    if ("APPROVED".equals(head.getStatus())) {
                        if ("APPLY".equals(headBizType)) {
                            BudgetRenewParams renewParams = assembleApplyRenewParamsFromOperate(head);
                            BudgetRenewRespVo renewResp = budgetApplicationController.authOrCancel(renewParams);
                            if (printResponse && renewResp != null) {
                                result.append("  审批接口返回: ").append(JsonUtils.toJsonString(renewResp)).append("\n");
                            }
                            checkEsbFailureAndRecord(renewResp != null ? renewResp.getEsbInfo() : null, renewResp, bizCode, headBizType);
                        } else if ("CONTRACT".equals(headBizType)) {
                            BudgetContractRenewParams renewParams = assembleContractRenewParamsFromOperate(head);
                            BudgetContractRenewRespVo renewResp = budgetContractController.authOrCancel(renewParams);
                            if (printResponse && renewResp != null) {
                                result.append("  审批接口返回: ").append(JsonUtils.toJsonString(renewResp)).append("\n");
                            }
                            checkEsbFailureAndRecord(renewResp != null ? renewResp.getEsbInfo() : null, renewResp, bizCode, headBizType);
                        } else if ("CLAIM".equals(headBizType)) {
                            BudgetClaimRenewParams renewParams = assembleClaimRenewParamsFromOperate(head);
                            BudgetClaimRenewRespVo renewResp = budgetClaimController.authOrCancel(renewParams);
                            if (printResponse && renewResp != null) {
                                result.append("  审批接口返回: ").append(JsonUtils.toJsonString(renewResp)).append("\n");
                            }
                            checkEsbFailureAndRecord(renewResp != null ? renewResp.getEsbInfo() : null, renewResp, bizCode, headBizType);
                        } else if ("ADJUST".equals(headBizType)) {
                            BudgetAdjustRenewParams renewParams = assembleAdjustRenewParamsFromOperate(head);
                            BudgetAdjustRenewRespVo renewResp = budgetAdjustController.authOrCancel(renewParams);
                            if (printResponse && renewResp != null) {
                                result.append("  审批接口返回: ").append(JsonUtils.toJsonString(renewResp)).append("\n");
                            }
                            checkEsbFailureAndRecord(renewResp != null ? renewResp.getEsbInfo() : null, renewResp, bizCode, headBizType);
                        }
                    }

                    result.append("  ✓ 成功\n\n");
                    successCount++;
                    saveBatchRunRecord(bizCode, headBizType, "SUCCESS", null);
                } catch (Exception e) {
                    failCount++;
                    String shortMsg = e.getMessage() != null ? e.getMessage() : (e.getClass().getSimpleName() + (e.getCause() != null ? ": " + e.getCause().getMessage() : ""));
                    result.append(String.format("  ✗ 失败: %s\n\n", shortMsg));
                    log.error("批量跑单失败 bizCode={}", bizCode, e);
                    // ESB 校验失败已在 checkEsbFailureAndRecord 中写记录，此处只记录其他异常（完整异常信息+堆栈）
                    if (!(e instanceof EsbFailureException)) {
                        saveBatchRunRecord(bizCode, headBizType, "FAILURE", getFullExceptionMessage(e));
                    }
                    // 不中断，继续处理后续单据
                }
            }

            String summary = String.format("全部完成。成功: %d, 失败: %d\n\n详情:\n%s", successCount, failCount, result.toString());
            if (failCount > 0) {
                return CommonResult.error("500", summary);
            }
            return CommonResult.success(summary);
        } catch (Exception e) {
            log.error("批量跑单异常", e);
            return CommonResult.error("500", "处理失败: " + e.getMessage());
        }
    }

    /** 处理方法：直接重新提交即可（此类忽略不重跑） */
    private static final String PROCESS_METHOD_DIRECT_RESUBMIT = "直接重新提交即可";
    /** 处理方法：与申请单报文中的组织保持一致 */
    private static final String PROCESS_METHOD_ORG_FROM_APPLY = "与申请单报文中的组织保持一致";
    /** 处理方法：与申请单报文中的科目保持一致 */
    private static final String PROCESS_METHOD_SUBJECT_FROM_APPLY = "与申请单报文中的科目保持一致";
    /** 处理方法：报文中更新项目编码（更新后编码见右侧） */
    private static final String PROCESS_METHOD_UPDATE_PROJECT_CODE = "报文中更新项目编码（更新后编码见右侧）";
    /** 处理方法：修改下报文中提单的E-HR组织编码（见右侧） */
    private static final String PROCESS_METHOD_UPDATE_EHR_ORG = "修改下报文中提单的E-HR组织编码（见右侧）";

    /**
     * 从 Excel「提交不上去单据清单」批量跑单：第一、二行忽略，数据从第三行开始；按 BIZ_CODE 从操作表组装参数，再按五种处理方法更新参数后提交。
     */
    private CommonResult<String> batchRunFromExcel(MultipartFile excelFile) {
        List<SubmitFailListExcelRowVo> rows = new ArrayList<>();
        try {
            EasyExcel.read(excelFile.getInputStream(), SubmitFailListExcelRowVo.class, new ReadListener<SubmitFailListExcelRowVo>() {
                @Override
                public void invoke(SubmitFailListExcelRowVo data, com.alibaba.excel.context.AnalysisContext context) {
                    rows.add(data);
                }
                @Override
                public void doAfterAllAnalysed(com.alibaba.excel.context.AnalysisContext context) { }
            }).sheet("提交不上去单据清单").headRowNumber(2).doRead();
        } catch (Exception e) {
            log.error("读取 Excel 提交不上去单据清单 失败", e);
            return CommonResult.error("400", "读取 Excel 失败: " + e.getMessage());
        }
        if (rows.isEmpty()) {
            return CommonResult.success("Excel 无数据行");
        }
        StringBuilder result = new StringBuilder();
        int successCount = 0;
        int failCount = 0;
        for (SubmitFailListExcelRowVo row : rows) {
            String bizCode = row != null ? row.getBizCode() : null;
            String bizType = row != null ? row.getBizType() : null;
            if (StringUtils.isBlank(bizCode)) {
                result.append("【空 BIZ_CODE】跳过\n\n");
                continue;
            }
            String processMethod = row.getProcessMethod() != null ? row.getProcessMethod().trim() : "";
            if (PROCESS_METHOD_DIRECT_RESUBMIT.equals(processMethod)) {
                result.append(String.format("【%s】%s 跳过：直接重新提交即可（已重跑过）\n\n", bizCode, bizType));
                continue;
            }
            result.append(String.format("【%s】%s 处理方法=%s\n", bizCode, bizType, processMethod));
            try {
                BudgetLedgerHeadForOperate head = budgetLedgerHeadForOperateMapper.selectOne(
                        new LambdaQueryWrapper<BudgetLedgerHeadForOperate>()
                                .eq(BudgetLedgerHeadForOperate::getBizCode, bizCode)
                                .eq(BudgetLedgerHeadForOperate::getDeleted, Boolean.FALSE));
                if (head == null) {
                    result.append("  未找到操作表头，跳过\n\n");
                    failCount++;
                    saveBatchRunRecord(bizCode, bizType, "FAILURE", "未找到 BUDGET_LEDGER_HEAD_FOR_OPERATE");
                    continue;
                }
                LambdaQueryWrapper<BudgetLedgerForOperate> ledgerWrapper = new LambdaQueryWrapper<>();
                ledgerWrapper.eq(BudgetLedgerForOperate::getBizCode, bizCode).eq(BudgetLedgerForOperate::getDeleted, Boolean.FALSE);
                List<BudgetLedgerForOperate> ledgers = budgetLedgerForOperateMapper.selectList(ledgerWrapper);
                String extractedCode = StringUtils.trimToEmpty(row.getExtractedOrgOrProjectCode());
                String targetCode = StringUtils.trimToEmpty(row.getTargetProjectOrOrgCode());

                if ("APPLY".equals(bizType)) {
                    BudgetApplicationParams applyParams = assembleApplyParamsFromOperate(head, ledgers);
                    applyExcelRule4Or5ToApplyParams(applyParams, processMethod, extractedCode, targetCode);
                    BudgetApplicationRespVo applyResp = budgetApplicationController.apply(applyParams);
                    checkEsbFailureAndRecord(applyResp != null ? applyResp.getEsbInfo() : null, applyResp, bizCode, bizType);
                    if ("APPROVED".equals(head.getStatus())) {
                        BudgetRenewParams renewParams = assembleApplyRenewParamsFromOperate(head);
                        BudgetRenewRespVo renewResp = budgetApplicationController.authOrCancel(renewParams);
                        checkEsbFailureAndRecord(renewResp != null ? renewResp.getEsbInfo() : null, renewResp, bizCode, bizType);
                    }
                } else if ("CONTRACT".equals(bizType)) {
                    BudgetContractApplyParams contractParams = assembleContractParamsFromOperate(head, ledgers);
                    applyExcelRules2Or3ToContractParams(contractParams, ledgers, processMethod);
                    applyExcelRule4Or5ToContractParams(contractParams, processMethod, extractedCode, targetCode);
                    BudgetContractRespVo contractResp = budgetContractController.apply(contractParams);
                    checkEsbFailureAndRecord(contractResp != null ? contractResp.getEsbInfo() : null, contractResp, bizCode, bizType);
                    if ("APPROVED".equals(head.getStatus())) {
                        BudgetContractRenewParams renewParams = assembleContractRenewParamsFromOperate(head);
                        BudgetContractRenewRespVo renewResp = budgetContractController.authOrCancel(renewParams);
                        checkEsbFailureAndRecord(renewResp != null ? renewResp.getEsbInfo() : null, renewResp, bizCode, bizType);
                    }
                } else if ("CLAIM".equals(bizType)) {
                    BudgetClaimApplyParams claimParams = assembleClaimParamsFromOperate(head, ledgers);
                    applyExcelRules2Or3ToClaimParams(claimParams, ledgers, processMethod);
                    applyExcelRule4Or5ToClaimParams(claimParams, processMethod, extractedCode, targetCode);
                    BudgetClaimRespVo claimResp = budgetClaimController.apply(claimParams);
                    checkEsbFailureAndRecord(claimResp != null ? claimResp.getEsbInfo() : null, claimResp, bizCode, bizType);
                    if ("APPROVED".equals(head.getStatus())) {
                        BudgetClaimRenewParams renewParams = assembleClaimRenewParamsFromOperate(head);
                        BudgetClaimRenewRespVo renewResp = budgetClaimController.authOrCancel(renewParams);
                        checkEsbFailureAndRecord(renewResp != null ? renewResp.getEsbInfo() : null, renewResp, bizCode, bizType);
                    }
                } else if ("ADJUST".equals(bizType)) {
                    BudgetAdjustApplyParams adjustParams = assembleAdjustParamsFromOperate(head, ledgers);
                    applyExcelRule4Or5ToAdjustParams(adjustParams, processMethod, extractedCode, targetCode);
                    BudgetAdjustRespVo adjustResp = budgetAdjustController.apply(adjustParams);
                    checkEsbFailureAndRecord(adjustResp != null ? adjustResp.getEsbInfo() : null, adjustResp, bizCode, bizType);
                    if ("APPROVED".equals(head.getStatus())) {
                        BudgetAdjustRenewParams renewParams = assembleAdjustRenewParamsFromOperate(head);
                        BudgetAdjustRenewRespVo renewResp = budgetAdjustController.authOrCancel(renewParams);
                        checkEsbFailureAndRecord(renewResp != null ? renewResp.getEsbInfo() : null, renewResp, bizCode, bizType);
                    }
                } else {
                    result.append("  跳过未知类型\n\n");
                    saveBatchRunRecord(bizCode, bizType, "FAILURE", "未知类型，已跳过");
                    failCount++;
                    continue;
                }
                result.append("  ✓ 成功\n\n");
                successCount++;
                saveBatchRunRecord(bizCode, bizType, "SUCCESS", null);
            } catch (Exception e) {
                failCount++;
                String shortMsg = e.getMessage() != null ? e.getMessage() : (e.getClass().getSimpleName() + (e.getCause() != null ? ": " + e.getCause().getMessage() : ""));
                result.append(String.format("  ✗ 失败: %s\n\n", shortMsg));
                log.error("Excel 批量跑单失败 bizCode={}", bizCode, e);
                if (!(e instanceof EsbFailureException)) {
                    saveBatchRunRecord(bizCode, bizType, "FAILURE", getFullExceptionMessage(e));
                }
            }
        }
        String summary = String.format("Excel 跑单完成。成功: %d, 失败: %d\n\n详情:\n%s", successCount, failCount, result.toString());
        if (failCount > 0) {
            return CommonResult.error("500", summary);
        }
        return CommonResult.success(summary);
    }

    /** 规则2/3：合同参数中关联申请单的明细，用申请单明细的组织/科目覆盖当前明细 */
    private void applyExcelRules2Or3ToContractParams(BudgetContractApplyParams params, List<BudgetLedgerForOperate> ledgers, String processMethod) {
        if (params == null || params.getContractApplyReqInfo() == null || params.getContractApplyReqInfo().getContractDetails() == null) {
            return;
        }
        List<ContractDetailDetailVo> details = params.getContractApplyReqInfo().getContractDetails();
        for (int i = 0; i < details.size() && i < ledgers.size(); i++) {
            List<String> applyBizCodes = getRelatedBizCodesForOperate(ledgers.get(i).getId(), "APPLY");
            if (applyBizCodes.isEmpty()) {
                continue;
            }
            LambdaQueryWrapper<BudgetLedgerForOperate> w = new LambdaQueryWrapper<>();
            w.eq(BudgetLedgerForOperate::getBizCode, applyBizCodes.get(0)).eq(BudgetLedgerForOperate::getDeleted, Boolean.FALSE);
            List<BudgetLedgerForOperate> applyLedgers = budgetLedgerForOperateMapper.selectList(w);
            if (applyLedgers.isEmpty()) {
                continue;
            }
            BudgetLedgerForOperate firstApply = applyLedgers.get(0);
            if (PROCESS_METHOD_ORG_FROM_APPLY.equals(processMethod)) {
                details.get(i).setManagementOrg(firstApply.getMorgCode());
            } else if (PROCESS_METHOD_SUBJECT_FROM_APPLY.equals(processMethod)) {
                String subj = firstApply.getBudgetSubjectCode();
                details.get(i).setBudgetSubjectCode("NAN-NAN".equals(subj) ? null : subj);
            }
        }
    }

    /** 规则2/3：报销参数中关联申请单的明细，用申请单明细的组织/科目覆盖当前明细 */
    private void applyExcelRules2Or3ToClaimParams(BudgetClaimApplyParams params, List<BudgetLedgerForOperate> ledgers, String processMethod) {
        if (params == null || params.getClaimApplyReqInfo() == null || params.getClaimApplyReqInfo().getClaimDetails() == null) {
            return;
        }
        List<ClaimDetailDetailVo> details = params.getClaimApplyReqInfo().getClaimDetails();
        for (int i = 0; i < details.size() && i < ledgers.size(); i++) {
            List<String> applyBizCodes = getRelatedBizCodesForOperate(ledgers.get(i).getId(), "APPLY");
            if (applyBizCodes.isEmpty()) {
                continue;
            }
            LambdaQueryWrapper<BudgetLedgerForOperate> w = new LambdaQueryWrapper<>();
            w.eq(BudgetLedgerForOperate::getBizCode, applyBizCodes.get(0)).eq(BudgetLedgerForOperate::getDeleted, Boolean.FALSE);
            List<BudgetLedgerForOperate> applyLedgers = budgetLedgerForOperateMapper.selectList(w);
            if (applyLedgers.isEmpty()) {
                continue;
            }
            BudgetLedgerForOperate firstApply = applyLedgers.get(0);
            if (PROCESS_METHOD_ORG_FROM_APPLY.equals(processMethod)) {
                details.get(i).setManagementOrg(firstApply.getMorgCode());
            } else if (PROCESS_METHOD_SUBJECT_FROM_APPLY.equals(processMethod)) {
                String subj = firstApply.getBudgetSubjectCode();
                details.get(i).setBudgetSubjectCode("NAN-NAN".equals(subj) ? null : subj);
            }
        }
    }

    /** 规则4/5：申请参数中替换项目编码或组织编码 */
    private void applyExcelRule4Or5ToApplyParams(BudgetApplicationParams params, String processMethod, String extractedCode, String targetCode) {
        if (params == null || params.getApplyReqInfo() == null || params.getApplyReqInfo().getDemandDetails() == null || StringUtils.isBlank(extractedCode)) {
            return;
        }
        for (ApplyDetailDetalVo d : params.getApplyReqInfo().getDemandDetails()) {
            if (PROCESS_METHOD_UPDATE_PROJECT_CODE.equals(processMethod) && extractedCode.equals(nullToEmpty(d.getMasterProjectCode()))) {
                d.setMasterProjectCode(targetCode);
            } else if (PROCESS_METHOD_UPDATE_EHR_ORG.equals(processMethod) && extractedCode.equals(nullToEmpty(d.getManagementOrg()))) {
                d.setManagementOrg(targetCode);
            }
        }
    }

    /** 规则4/5：合同参数中替换项目编码或组织编码 */
    private void applyExcelRule4Or5ToContractParams(BudgetContractApplyParams params, String processMethod, String extractedCode, String targetCode) {
        if (params == null || params.getContractApplyReqInfo() == null || params.getContractApplyReqInfo().getContractDetails() == null || StringUtils.isBlank(extractedCode)) {
            return;
        }
        for (ContractDetailDetailVo d : params.getContractApplyReqInfo().getContractDetails()) {
            if (PROCESS_METHOD_UPDATE_PROJECT_CODE.equals(processMethod) && extractedCode.equals(nullToEmpty(d.getMasterProjectCode()))) {
                d.setMasterProjectCode(targetCode);
            } else if (PROCESS_METHOD_UPDATE_EHR_ORG.equals(processMethod) && extractedCode.equals(nullToEmpty(d.getManagementOrg()))) {
                d.setManagementOrg(targetCode);
            }
        }
    }

    /** 规则4/5：报销参数中替换项目编码或组织编码 */
    private void applyExcelRule4Or5ToClaimParams(BudgetClaimApplyParams params, String processMethod, String extractedCode, String targetCode) {
        if (params == null || params.getClaimApplyReqInfo() == null || params.getClaimApplyReqInfo().getClaimDetails() == null || StringUtils.isBlank(extractedCode)) {
            return;
        }
        for (ClaimDetailDetailVo d : params.getClaimApplyReqInfo().getClaimDetails()) {
            if (PROCESS_METHOD_UPDATE_PROJECT_CODE.equals(processMethod) && extractedCode.equals(nullToEmpty(d.getMasterProjectCode()))) {
                d.setMasterProjectCode(targetCode);
            } else if (PROCESS_METHOD_UPDATE_EHR_ORG.equals(processMethod) && extractedCode.equals(nullToEmpty(d.getManagementOrg()))) {
                d.setManagementOrg(targetCode);
            }
        }
    }

    /** 规则4/5：调整参数中替换项目编码或组织编码 */
    private void applyExcelRule4Or5ToAdjustParams(BudgetAdjustApplyParams params, String processMethod, String extractedCode, String targetCode) {
        if (params == null || params.getAdjustApplyReqInfo() == null || params.getAdjustApplyReqInfo().getAdjustDetails() == null || StringUtils.isBlank(extractedCode)) {
            return;
        }
        for (AdjustDetailDetailVo d : params.getAdjustApplyReqInfo().getAdjustDetails()) {
            if (PROCESS_METHOD_UPDATE_PROJECT_CODE.equals(processMethod) && extractedCode.equals(nullToEmpty(d.getMasterProjectCode()))) {
                d.setMasterProjectCode(targetCode);
            } else if (PROCESS_METHOD_UPDATE_EHR_ORG.equals(processMethod) && extractedCode.equals(nullToEmpty(d.getManagementOrg()))) {
                d.setManagementOrg(targetCode);
            }
        }
    }

    private static String nullToEmpty(String s) {
        return s == null ? "" : s;
    }

    /**
     * 从 CSV 批量跑单：表头 BIZ_CODE, BIZ_TYPE, LEDGER_HEAD_STATUS, REQUEST_PARAMS；从第二行起每行用 REQUEST_PARAMS 调申请接口，若 LEDGER_HEAD_STATUS=APPROVED 再调审批接口。
     */
    private CommonResult<String> batchRunFromCsv(MultipartFile file) throws IOException {
        List<String> headerNames;
        List<Map<String, String>> rows = new ArrayList<>();
        try (InputStreamReader reader = new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8);
             CSVParser parser = new CSVParser(reader, CSVFormat.DEFAULT.withFirstRecordAsHeader().withIgnoreHeaderCase().withTrim())) {
            headerNames = parser.getHeaderNames();
            if (headerNames == null || headerNames.isEmpty()) {
                return CommonResult.error("400", "CSV 表头为空");
            }
            for (CSVRecord record : parser) {
                Map<String, String> row = new LinkedHashMap<>();
                for (String h : headerNames) {
                    String v = record.get(h);
                    row.put(h, v != null ? v : "");
                }
                rows.add(row);
            }
        }
        StringBuilder result = new StringBuilder();
        int successCount = 0;
        int failCount = 0;
        for (Map<String, String> row : rows) {
            String bizCode = row.get("BIZ_CODE");
            String bizType = row.get("BIZ_TYPE");
            String ledgerHeadStatus = row.get("LEDGER_HEAD_STATUS");
            String requestParamsJson = row.get("REQUEST_PARAMS");
            if (!org.springframework.util.StringUtils.hasText(bizCode) || !org.springframework.util.StringUtils.hasText(requestParamsJson)) {
                result.append(String.format("【%s】%s 跳过：BIZ_CODE 或 REQUEST_PARAMS 为空\n\n", bizCode, bizType));
                continue;
            }
            result.append(String.format("【%s】%s\n", bizCode, bizType));
            try {
                // 按 BIZ_TYPE 解析 REQUEST_PARAMS 并调用申请接口
                if ("APPLY".equals(bizType)) {
                    BudgetApplicationParams applyParams = JsonUtils.parseObject(requestParamsJson, BudgetApplicationParams.class);
                    if (applyParams == null) {
                        throw new IllegalArgumentException("REQUEST_PARAMS 解析失败");
                    }
                    BudgetApplicationRespVo applyResp = budgetApplicationController.apply(applyParams);
                    checkEsbFailureAndRecord(applyResp != null ? applyResp.getEsbInfo() : null, applyResp, bizCode, bizType);
                    if ("APPROVED".equals(ledgerHeadStatus)) {
                        BudgetRenewParams renewParams = buildApplyRenewParamsFromApplyResp(applyResp, bizCode);
                        BudgetRenewRespVo renewResp = budgetApplicationController.authOrCancel(renewParams);
                        checkEsbFailureAndRecord(renewResp != null ? renewResp.getEsbInfo() : null, renewResp, bizCode, bizType);
                    }
                } else if ("CONTRACT".equals(bizType)) {
                    BudgetContractApplyParams applyParams = JsonUtils.parseObject(requestParamsJson, BudgetContractApplyParams.class);
                    if (applyParams == null) {
                        throw new IllegalArgumentException("REQUEST_PARAMS 解析失败");
                    }
                    BudgetContractRespVo applyResp = budgetContractController.apply(applyParams);
                    checkEsbFailureAndRecord(applyResp != null ? applyResp.getEsbInfo() : null, applyResp, bizCode, bizType);
                    if ("APPROVED".equals(ledgerHeadStatus)) {
                        BudgetContractRenewParams renewParams = buildContractRenewParamsFromApplyResp(applyResp, bizCode);
                        BudgetContractRenewRespVo renewResp = budgetContractController.authOrCancel(renewParams);
                        checkEsbFailureAndRecord(renewResp != null ? renewResp.getEsbInfo() : null, renewResp, bizCode, bizType);
                    }
                } else if ("CLAIM".equals(bizType)) {
                    BudgetClaimApplyParams applyParams = JsonUtils.parseObject(requestParamsJson, BudgetClaimApplyParams.class);
                    if (applyParams == null) {
                        throw new IllegalArgumentException("REQUEST_PARAMS 解析失败");
                    }
                    BudgetClaimRespVo applyResp = budgetClaimController.apply(applyParams);
                    checkEsbFailureAndRecord(applyResp != null ? applyResp.getEsbInfo() : null, applyResp, bizCode, bizType);
                    if ("APPROVED".equals(ledgerHeadStatus)) {
                        BudgetClaimRenewParams renewParams = buildClaimRenewParamsFromApplyResp(applyResp, bizCode);
                        BudgetClaimRenewRespVo renewResp = budgetClaimController.authOrCancel(renewParams);
                        checkEsbFailureAndRecord(renewResp != null ? renewResp.getEsbInfo() : null, renewResp, bizCode, bizType);
                    }
                } else if ("ADJUST".equals(bizType)) {
                    BudgetAdjustApplyParams applyParams = JsonUtils.parseObject(requestParamsJson, BudgetAdjustApplyParams.class);
                    if (applyParams == null) {
                        throw new IllegalArgumentException("REQUEST_PARAMS 解析失败");
                    }
                    BudgetAdjustRespVo applyResp = budgetAdjustController.apply(applyParams);
                    checkEsbFailureAndRecord(applyResp != null ? applyResp.getEsbInfo() : null, applyResp, bizCode, bizType);
                    if ("APPROVED".equals(ledgerHeadStatus)) {
                        BudgetAdjustRenewParams renewParams = buildAdjustRenewParamsFromApplyResp(applyResp, bizCode);
                        BudgetAdjustRenewRespVo renewResp = budgetAdjustController.authOrCancel(renewParams);
                        checkEsbFailureAndRecord(renewResp != null ? renewResp.getEsbInfo() : null, renewResp, bizCode, bizType);
                    }
                } else {
                    result.append("  跳过未知类型\n\n");
                    saveBatchRunRecord(bizCode, bizType, "FAILURE", "未知类型，已跳过");
                    failCount++;
                    continue;
                }
                result.append("  ✓ 成功\n\n");
                successCount++;
                saveBatchRunRecord(bizCode, bizType, "SUCCESS", null);
            } catch (Exception e) {
                failCount++;
                String shortMsg = e.getMessage() != null ? e.getMessage() : (e.getClass().getSimpleName() + (e.getCause() != null ? ": " + e.getCause().getMessage() : ""));
                result.append(String.format("  ✗ 失败: %s\n\n", shortMsg));
                log.error("CSV 批量跑单失败 bizCode={}", bizCode, e);
                if (!(e instanceof EsbFailureException)) {
                    saveBatchRunRecord(bizCode, bizType, "FAILURE", getFullExceptionMessage(e));
                }
            }
        }
        String summary = String.format("CSV 跑单完成。成功: %d, 失败: %d\n\n详情:\n%s", successCount, failCount, result.toString());
        if (failCount > 0) {
            return CommonResult.error("500", summary);
        }
        return CommonResult.success(summary);
    }

    private BudgetRenewParams buildApplyRenewParamsFromApplyResp(BudgetApplicationRespVo applyResp, String bizCode) {
        BudgetRenewParams params = new BudgetRenewParams();
        ESBRespInfoVo esb = applyResp != null ? applyResp.getEsbInfo() : null;
        params.setEsbInfo(ESBInfoParams.builder()
                .instId(esb != null ? esb.getInstId() : null)
                .requestTime(esb != null && org.springframework.util.StringUtils.hasText(esb.getRequestTime()) ? esb.getRequestTime() : formatRequestTime(LocalDateTime.now()))
                .attr1("").attr2("").attr3("")
                .build());
        RenewApplyReqInfoParams renew = new RenewApplyReqInfoParams();
        renew.setDemandOrderNo(bizCode);
        renew.setDocumentStatus("APPROVED");
        renew.setApprover("system");
        renew.setApproveTime(formatOperateTime(LocalDateTime.now()));
        renew.setApproveComment("同意审批");
        renew.setDemandDetails(Collections.emptyList());
        params.setRenewApplyReqInfo(renew);
        return params;
    }

    private BudgetContractRenewParams buildContractRenewParamsFromApplyResp(BudgetContractRespVo applyResp, String bizCode) {
        BudgetContractRenewParams params = new BudgetContractRenewParams();
        ESBRespInfoVo esb = applyResp != null ? applyResp.getEsbInfo() : null;
        params.setEsbInfo(ESBInfoParams.builder()
                .instId(esb != null ? esb.getInstId() : null)
                .requestTime(esb != null && org.springframework.util.StringUtils.hasText(esb.getRequestTime()) ? esb.getRequestTime() : formatRequestTime(LocalDateTime.now()))
                .attr1("").attr2("").attr3("")
                .build());
        ContractRenewReqInfoParams renew = new ContractRenewReqInfoParams();
        renew.setContractNo(bizCode);
        renew.setDocumentStatus("APPROVED");
        renew.setApprover("system");
        renew.setApproveTime(formatOperateTime(LocalDateTime.now()));
        renew.setApproveComment("同意审批");
        params.setContractRenewReqInfo(renew);
        return params;
    }

    private BudgetClaimRenewParams buildClaimRenewParamsFromApplyResp(BudgetClaimRespVo applyResp, String bizCode) {
        BudgetClaimRenewParams params = new BudgetClaimRenewParams();
        ESBRespInfoVo esb = applyResp != null ? applyResp.getEsbInfo() : null;
        params.setEsbInfo(ESBInfoParams.builder()
                .instId(esb != null ? esb.getInstId() : null)
                .requestTime(esb != null && org.springframework.util.StringUtils.hasText(esb.getRequestTime()) ? esb.getRequestTime() : formatRequestTime(LocalDateTime.now()))
                .attr1("").attr2("").attr3("")
                .build());
        ClaimRenewReqInfoParams renew = new ClaimRenewReqInfoParams();
        renew.setClaimOrderNo(bizCode);
        renew.setDocumentStatus("APPROVED");
        renew.setApprover("system");
        renew.setApproveTime(formatOperateTime(LocalDateTime.now()));
        renew.setApproveComment("同意审批");
        renew.setClaimDetails(null);
        params.setClaimRenewReqInfo(renew);
        return params;
    }

    private BudgetAdjustRenewParams buildAdjustRenewParamsFromApplyResp(BudgetAdjustRespVo applyResp, String bizCode) {
        BudgetAdjustRenewParams params = new BudgetAdjustRenewParams();
        ESBRespInfoVo esb = applyResp != null ? applyResp.getEsbInfo() : null;
        params.setEsbInfo(ESBInfoParams.builder()
                .instId(esb != null ? esb.getInstId() : null)
                .requestTime(esb != null && org.springframework.util.StringUtils.hasText(esb.getRequestTime()) ? esb.getRequestTime() : formatRequestTime(LocalDateTime.now()))
                .attr1("").attr2("").attr3("")
                .build());
        AdjustRenewReqInfoParams renew = new AdjustRenewReqInfoParams();
        renew.setAdjustOrderNo(bizCode);
        renew.setDocumentStatus("APPROVED");
        renew.setApprover("system");
        renew.setApproveTime(formatOperateTime(LocalDateTime.now()));
        renew.setApproveComment("同意审批");
        params.setAdjustRenewReqInfo(renew);
        return params;
    }

    /** 拼完整异常信息（含 cause 链与堆栈），便于 ERROR_MSG 全量留存 */
    private String getFullExceptionMessage(Throwable e) {
        if (e == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        sb.append(e.getClass().getName()).append(": ").append(e.getMessage() != null ? e.getMessage() : "").append("\n");
        Throwable cause = e.getCause();
        while (cause != null) {
            sb.append("Caused by: ").append(cause.getClass().getName()).append(": ").append(cause.getMessage() != null ? cause.getMessage() : "").append("\n");
            cause = cause.getCause();
        }
        StringWriter sw = new StringWriter();
        e.printStackTrace(new PrintWriter(sw));
        sb.append(sw.toString());
        return sb.toString();
    }

    /** 写入批量跑单执行记录（成功/失败均记录）；同一 BIZ_CODE 已存在则覆盖更新，不存在则新增 */
    private void saveBatchRunRecord(String bizCode, String bizType, String runStatus, String errorMsg) {
        try {
            BudgetOperateBatchRunRecord existing = budgetOperateBatchRunRecordMapper.selectOne(
                    BudgetOperateBatchRunRecord::getBizCode, bizCode);
            if (existing != null) {
                existing.setBizType(bizType);
                existing.setRunStatus(runStatus);
                existing.setErrorMsg(errorMsg);
                budgetOperateBatchRunRecordMapper.updateById(existing);
            } else {
                BudgetOperateBatchRunRecord record = BudgetOperateBatchRunRecord.builder()
                        .bizCode(bizCode)
                        .bizType(bizType)
                        .runStatus(runStatus)
                        .errorMsg(errorMsg)
                        .build();
                budgetOperateBatchRunRecordMapper.insert(record);
            }
        } catch (Exception e) {
            log.warn("写入批量跑单执行记录失败 bizCode={}", bizCode, e);
        }
    }

    /** 生成 ESB instId，格式与现有逻辑一致（UUID 分段） */
    private String generateInstId() {
        String uuid = UUID.randomUUID().toString().replace("-", "");
        return "N" + uuid.substring(0, 8) + "." + uuid.substring(8, 16) + ".N"
                + uuid.substring(16, 18) + "." + uuid.substring(18, 28) + ".N" + uuid.substring(28, 32);
    }

    private String formatRequestTime(LocalDateTime updateTime) {
        if (updateTime == null) {
            return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS"));
        }
        return updateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS"));
    }

    private String formatOperateTime(LocalDateTime updateTime) {
        if (updateTime == null) {
            return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        }
        return updateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    /**
     * 校验 ESB 响应：若 returnStatus 非 S，或 returnCode 为 E 开头（业务错误），或 returnMsg 包含「失败」，则先写入执行记录表（完整返回参数 JSON）再抛异常。
     * fullResponse 为接口完整返回对象（如 BudgetApplicationRespVo），errorMsg 会整份序列化落库。
     */
    private void checkEsbFailureAndRecord(ESBRespInfoVo esbInfo, Object fullResponse, String bizCode, String bizType) {
        if (esbInfo == null) {
            return;
        }
        String code = esbInfo.getReturnCode();
        String msg = esbInfo.getReturnMsg();
        boolean failed = !"S".equals(esbInfo.getReturnStatus())
                || (code != null && code.startsWith("E"))
                || (msg != null && msg.contains("失败"));
        if (failed) {
            String errorMsg = fullResponse != null ? JsonUtils.toJsonString(fullResponse) : JsonUtils.toJsonString(esbInfo);
            saveBatchRunRecord(bizCode, bizType, "FAILURE", errorMsg);
            String shortMsg = esbInfo.getReturnCode() != null ? esbInfo.getReturnCode() + " " : "";
            shortMsg += esbInfo.getReturnMsg() != null ? esbInfo.getReturnMsg() : "ESB返回失败";
            throw new EsbFailureException(shortMsg);
        }
    }

    /** ESB 校验失败异常：表示已按 returnCode/returnMsg 写入执行记录，catch 时无需再写 */
    private static class EsbFailureException extends RuntimeException {
        EsbFailureException(String message) {
            super(message);
        }
    }

    /**
     * 组装预算申请（APPLY）提交参数
     */
    private BudgetApplicationParams assembleApplyParamsFromOperate(BudgetLedgerHeadForOperate head, List<BudgetLedgerForOperate> ledgers) {
        BudgetApplicationParams params = new BudgetApplicationParams();
        params.setEsbInfo(ESBInfoParams.builder()
                .instId(generateInstId())
                .requestTime(formatRequestTime(head.getUpdateTime()))
                .attr1("")
                .attr2("")
                .attr3("")
                .build());

        ApplyReqInfoParams applyReqInfo = new ApplyReqInfoParams();
        applyReqInfo.setDemandOrderNo(head.getBizCode());
        applyReqInfo.setDocumentName(head.getDocumentName());
        applyReqInfo.setDataSource(head.getDataSource());
        if (StringUtils.isNotBlank(head.getProcessName())) {
            applyReqInfo.setProcessName(head.getProcessName());
        }
        applyReqInfo.setDocumentStatus("APPROVED_UPDATE".equals(head.getStatus()) ? "APPROVED_UPDATE" : "INITIAL_SUBMITTED");
        applyReqInfo.setOperator(head.getOperator() != null ? head.getOperator() : "");
        applyReqInfo.setOperateTime(formatOperateTime(head.getUpdateTime()));
        if (StringUtils.isNotBlank(head.getOperatorNo())) {
            applyReqInfo.setOperatorNo(head.getOperatorNo());
        }

        List<ApplyDetailDetalVo> demandDetails = new ArrayList<>();
        for (BudgetLedgerForOperate ledger : ledgers) {
            ApplyDetailDetalVo d = new ApplyDetailDetalVo();
            d.setDemandYear(ledger.getYear());
            d.setDemandMonth(ledger.getMonth());
            d.setManagementOrg(ledger.getMorgCode());
            d.setBudgetSubjectCode("NAN-NAN".equals(ledger.getBudgetSubjectCode()) ? null : ledger.getBudgetSubjectCode());
            d.setMasterProjectCode("NAN".equals(ledger.getMasterProjectCode()) ? null : ledger.getMasterProjectCode());
            d.setErpAssetType("NAN".equals(ledger.getErpAssetType()) ? null : ledger.getErpAssetType());
            d.setIsInternal(ledger.getIsInternal() != null ? ledger.getIsInternal() : "0");
            d.setDemandAmount(ledger.getAmount());
            d.setCurrency(ledger.getCurrency());
            if (StringUtils.isNotBlank(ledger.getMetadata())) {
                d.setMetadata(parseJsonToMap(ledger.getMetadata()));
            }
            demandDetails.add(d);
        }
        applyReqInfo.setDemandDetails(demandDetails);
        BigDecimal totalInvestmentAmount = ledgers.stream()
                .map(BudgetLedgerForOperate::getAmount)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        applyReqInfo.setTotalInvestmentAmount(totalInvestmentAmount);
        params.setApplyReqInfo(applyReqInfo);
        return params;
    }

    /**
     * 组装预算合同（CONTRACT）提交参数
     */
    private BudgetContractApplyParams assembleContractParamsFromOperate(BudgetLedgerHeadForOperate head, List<BudgetLedgerForOperate> ledgers) {
        BudgetContractApplyParams params = new BudgetContractApplyParams();
        params.setEsbInfo(ESBInfoParams.builder()
                .instId(generateInstId())
                .requestTime(formatRequestTime(head.getUpdateTime()))
                .build());

        ContractApplyReqInfoParams contractApplyReqInfo = new ContractApplyReqInfoParams();
        contractApplyReqInfo.setContractNo(head.getBizCode());
        contractApplyReqInfo.setDocumentName(head.getDocumentName());
        contractApplyReqInfo.setDataSource(head.getDataSource());
        if (StringUtils.isNotBlank(head.getProcessName())) {
            contractApplyReqInfo.setProcessName(head.getProcessName());
        }
        contractApplyReqInfo.setDocumentStatus("APPROVED_UPDATE".equals(head.getStatus()) ? "APPROVED_UPDATE" : "INITIAL_SUBMITTED");
        contractApplyReqInfo.setOperator(head.getOperator() != null ? head.getOperator() : "");
        contractApplyReqInfo.setOperateTime(formatOperateTime(head.getUpdateTime()));
        if (StringUtils.isNotBlank(head.getOperatorNo())) {
            contractApplyReqInfo.setOperatorNo(head.getOperatorNo());
        }

        List<ContractDetailDetailVo> contractDetails = new ArrayList<>();
        for (BudgetLedgerForOperate ledger : ledgers) {
            ContractDetailDetailVo d = new ContractDetailDetailVo();
            d.setContractYear(ledger.getYear());
            d.setContractMonth(ledger.getMonth());
            d.setManagementOrg(ledger.getMorgCode());
            d.setBudgetSubjectCode("NAN-NAN".equals(ledger.getBudgetSubjectCode()) ? null : ledger.getBudgetSubjectCode());
            d.setMasterProjectCode("NAN".equals(ledger.getMasterProjectCode()) ? null : ledger.getMasterProjectCode());
            d.setErpAssetType("NAN".equals(ledger.getErpAssetType()) ? null : ledger.getErpAssetType());
            d.setIsInternal(ledger.getIsInternal() != null ? ledger.getIsInternal() : "0");
            d.setContractAmount(ledger.getAmount());
            d.setCurrency(ledger.getCurrency());
            if (StringUtils.isNotBlank(ledger.getMetadata())) {
                d.setMetadata(parseJsonToMap(ledger.getMetadata()));
            }
            // 关联申请单：通过 BUDGET_LEDGER_SELF_R_FOR_OPERATE（bizType=APPLY）查 relatedId，再查 BUDGET_LEDGER_FOR_OPERATE 得去重 demandOrderNo
            d.setContractApplyDetails(getRelatedDemandOrderNosForOperate(ledger.getId()));
            contractDetails.add(d);
        }
        contractApplyReqInfo.setContractDetails(contractDetails);
        params.setContractApplyReqInfo(contractApplyReqInfo);
        return params;
    }

    /**
     * 根据 BUDGET_LEDGER_FOR_OPERATE 的 id 查 BUDGET_LEDGER_SELF_R_FOR_OPERATE（bizType=APPLY）得 relatedId，再查 BUDGET_LEDGER_FOR_OPERATE 得去重 demandOrderNo。
     * 若不存在 BUDGET_LEDGER_SELF_R_FOR_OPERATE 表或查询异常则返回空列表。
     */
    private List<ContractApplyDetailVo> getRelatedDemandOrderNosForOperate(Long ledgerForOperateId) {
        List<String> bizCodes = getRelatedBizCodesForOperate(ledgerForOperateId, "APPLY");
        if (bizCodes == null || bizCodes.isEmpty()) {
            return Collections.emptyList();
        }
        List<ContractApplyDetailVo> list = new ArrayList<>();
        for (String demandOrderNo : bizCodes) {
            list.add(new ContractApplyDetailVo(demandOrderNo, null, null));
        }
        return list;
    }

    /**
     * 根据 BUDGET_LEDGER_FOR_OPERATE 的 id 查 BUDGET_LEDGER_SELF_R_FOR_OPERATE（bizType=APPLY/CONTRACT）得 relatedId，再查 BUDGET_LEDGER_FOR_OPERATE 得去重单号。
     * 若不存在该表或查询异常则返回空列表。
     */
    private List<String> getRelatedBizCodesForOperate(Long ledgerForOperateId, String bizType) {
        try {
            List<BudgetLedgerSelfRForOperate> selfRList = budgetLedgerSelfRForOperateMapper.selectByIdsAndBizType(
                    Collections.singleton(ledgerForOperateId), bizType);
            if (selfRList == null || selfRList.isEmpty()) {
                return Collections.emptyList();
            }
            Set<Long> relatedIds = selfRList.stream()
                    .map(BudgetLedgerSelfRForOperate::getRelatedId)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());
            if (relatedIds.isEmpty()) {
                return Collections.emptyList();
            }
            LambdaQueryWrapper<BudgetLedgerForOperate> wrapper = new LambdaQueryWrapper<>();
            wrapper.in(BudgetLedgerForOperate::getId, relatedIds)
                    .eq(BudgetLedgerForOperate::getDeleted, Boolean.FALSE);
            List<BudgetLedgerForOperate> relatedLedgers = budgetLedgerForOperateMapper.selectList(wrapper);
            if (relatedLedgers == null || relatedLedgers.isEmpty()) {
                return Collections.emptyList();
            }
            return relatedLedgers.stream()
                    .map(BudgetLedgerForOperate::getBizCode)
                    .filter(StringUtils::isNotBlank)
                    .distinct()
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.warn("查询 BUDGET_LEDGER_SELF_R_FOR_OPERATE 关联单号失败 ledgerForOperateId={}, bizType={}", ledgerForOperateId, bizType, e);
            return Collections.emptyList();
        }
    }

    /**
     * 组装预算报销/付款（CLAIM）提交参数
     */
    private BudgetClaimApplyParams assembleClaimParamsFromOperate(BudgetLedgerHeadForOperate head, List<BudgetLedgerForOperate> ledgers) {
        BudgetClaimApplyParams params = new BudgetClaimApplyParams();
        params.setEsbInfo(ESBInfoParams.builder()
                .instId(generateInstId())
                .requestTime(formatRequestTime(head.getUpdateTime()))
                .attr1("")
                .attr2("")
                .attr3("")
                .build());

        ClaimApplyReqInfoParams claimApplyReqInfo = new ClaimApplyReqInfoParams();
        claimApplyReqInfo.setClaimOrderNo(head.getBizCode());
        claimApplyReqInfo.setDocumentName(head.getDocumentName());
        claimApplyReqInfo.setDataSource(head.getDataSource());
        if (StringUtils.isNotBlank(head.getProcessName())) {
            claimApplyReqInfo.setProcessName(head.getProcessName());
        }
        claimApplyReqInfo.setDocumentStatus("APPROVED_UPDATE".equals(head.getStatus()) ? "APPROVED_UPDATE" : "INITIAL_SUBMITTED");
        claimApplyReqInfo.setOperator(head.getOperator() != null ? head.getOperator() : "");
        claimApplyReqInfo.setOperateTime(formatOperateTime(head.getUpdateTime()));
        if (StringUtils.isNotBlank(head.getOperatorNo())) {
            claimApplyReqInfo.setOperatorNo(head.getOperatorNo());
        }

        List<ClaimDetailDetailVo> claimDetails = new ArrayList<>();
        for (BudgetLedgerForOperate ledger : ledgers) {
            ClaimDetailDetailVo d = new ClaimDetailDetailVo();
            d.setClaimYear(ledger.getYear());
            d.setClaimMonth(ledger.getMonth());
            if (StringUtils.isNotBlank(ledger.getActualYear())) {
                d.setActualYear(ledger.getActualYear());
            }
            if (StringUtils.isNotBlank(ledger.getActualMonth())) {
                d.setActualMonth(ledger.getActualMonth());
            }
            d.setManagementOrg(ledger.getMorgCode());
            d.setBudgetSubjectCode("NAN-NAN".equals(ledger.getBudgetSubjectCode()) ? null : ledger.getBudgetSubjectCode());
            d.setMasterProjectCode("NAN".equals(ledger.getMasterProjectCode()) ? null : ledger.getMasterProjectCode());
            d.setErpAssetType("NAN".equals(ledger.getErpAssetType()) ? null : ledger.getErpAssetType());
            d.setIsInternal(ledger.getIsInternal() != null ? ledger.getIsInternal() : "0");
            d.setActualAmount(ledger.getAmount());
            d.setCurrency(ledger.getCurrency());
            if (StringUtils.isNotBlank(ledger.getMetadata())) {
                d.setMetadata(parseJsonToMap(ledger.getMetadata()));
            }
            List<String> demandOrderNos = getRelatedBizCodesForOperate(ledger.getId(), "APPLY");
            List<ClaimApplyDetailVo> claimApplyDetails = new ArrayList<>();
            for (String no : demandOrderNos) {
                claimApplyDetails.add(new ClaimApplyDetailVo(no, null, null));
            }
            d.setClaimApplyDetails(claimApplyDetails.isEmpty() ? null : claimApplyDetails);
            List<String> contractNos = getRelatedBizCodesForOperate(ledger.getId(), "CONTRACT");
            List<ClaimContractDetailVo> claimContractDetails = new ArrayList<>();
            for (String no : contractNos) {
                claimContractDetails.add(new ClaimContractDetailVo(no, null, null));
            }
            d.setClaimContractDetails(claimContractDetails.isEmpty() ? null : claimContractDetails);
            claimDetails.add(d);
        }
        claimApplyReqInfo.setClaimDetails(claimDetails);
        params.setClaimApplyReqInfo(claimApplyReqInfo);
        return params;
    }

    private BudgetRenewParams assembleApplyRenewParamsFromOperate(BudgetLedgerHeadForOperate head) {
        BudgetRenewParams params = new BudgetRenewParams();
        params.setEsbInfo(ESBInfoParams.builder()
                .instId(generateInstId())
                .requestTime(formatRequestTime(head.getUpdateTime()))
                .attr1("")
                .attr2("")
                .attr3("")
                .build());
        RenewApplyReqInfoParams renew = new RenewApplyReqInfoParams();
        renew.setDemandOrderNo(head.getBizCode());
        renew.setDocumentStatus("APPROVED");
        renew.setApprover("system");
        renew.setApproveTime(formatOperateTime(head.getUpdateTime()));
        renew.setApproveComment("同意审批");
        renew.setDemandDetails(Collections.emptyList());
        params.setRenewApplyReqInfo(renew);
        return params;
    }

    private BudgetContractRenewParams assembleContractRenewParamsFromOperate(BudgetLedgerHeadForOperate head) {
        BudgetContractRenewParams params = new BudgetContractRenewParams();
        params.setEsbInfo(ESBInfoParams.builder()
                .instId(generateInstId())
                .requestTime(formatRequestTime(head.getUpdateTime()))
                .attr1("")
                .attr2("")
                .attr3("")
                .build());
        ContractRenewReqInfoParams renew = new ContractRenewReqInfoParams();
        renew.setContractNo(head.getBizCode());
        renew.setDocumentStatus("APPROVED");
        renew.setApprover("system");
        renew.setApproveTime(formatOperateTime(head.getUpdateTime()));
        renew.setApproveComment("同意审批");
        params.setContractRenewReqInfo(renew);
        return params;
    }

    private BudgetClaimRenewParams assembleClaimRenewParamsFromOperate(BudgetLedgerHeadForOperate head) {
        BudgetClaimRenewParams params = new BudgetClaimRenewParams();
        params.setEsbInfo(ESBInfoParams.builder()
                .instId(generateInstId())
                .requestTime(formatRequestTime(head.getUpdateTime()))
                .attr1("")
                .attr2("")
                .attr3("")
                .build());
        ClaimRenewReqInfoParams renew = new ClaimRenewReqInfoParams();
        renew.setClaimOrderNo(head.getBizCode());
        renew.setDocumentStatus("APPROVED");
        renew.setApprover("system");
        renew.setApproveTime(formatOperateTime(head.getUpdateTime()));
        renew.setApproveComment("同意审批");
        params.setClaimRenewReqInfo(renew);
        return params;
    }

    /**
     * 组装预算调整（ADJUST）提交参数（从操作表）
     */
    private BudgetAdjustApplyParams assembleAdjustParamsFromOperate(BudgetLedgerHeadForOperate head, List<BudgetLedgerForOperate> ledgers) {
        BudgetAdjustApplyParams params = new BudgetAdjustApplyParams();
        params.setEsbInfo(ESBInfoParams.builder()
                .instId(generateInstId())
                .requestTime(formatRequestTime(head.getUpdateTime()))
                .build());

        AdjustApplyReqInfoParams adjustApplyReqInfo = new AdjustApplyReqInfoParams();
        adjustApplyReqInfo.setAdjustOrderNo(head.getBizCode());
        adjustApplyReqInfo.setDocumentName(head.getDocumentName());
        adjustApplyReqInfo.setDataSource(head.getDataSource());
        if (StringUtils.isNotBlank(head.getProcessName())) {
            adjustApplyReqInfo.setProcessName(head.getProcessName());
        }
        adjustApplyReqInfo.setDocumentStatus("APPROVED_UPDATE".equals(head.getStatus()) ? "APPROVED_UPDATE" : "INITIAL_SUBMITTED");
        adjustApplyReqInfo.setOperator(head.getOperator() != null ? head.getOperator() : "");
        adjustApplyReqInfo.setOperateTime(formatOperateTime(head.getUpdateTime()));
        if (StringUtils.isNotBlank(head.getOperatorNo())) {
            adjustApplyReqInfo.setOperatorNo(head.getOperatorNo());
        }

        String isInternal = ledgers.isEmpty() ? "1" : ledgers.get(0).getIsInternal();
        if (StringUtils.isBlank(isInternal)) {
            isInternal = "1";
        }
        adjustApplyReqInfo.setIsInternal(isInternal);

        List<AdjustDetailDetailVo> adjustDetails = new ArrayList<>();
        for (BudgetLedgerForOperate ledger : ledgers) {
            AdjustDetailDetailVo detail = new AdjustDetailDetailVo();
            detail.setAdjustYear(ledger.getYear());
            detail.setAdjustMonth(ledger.getMonth());
            detail.setEffectType(ledger.getEffectType());
            detail.setManagementOrg(ledger.getMorgCode());
            detail.setBudgetSubjectCode("NAN-NAN".equals(ledger.getBudgetSubjectCode()) ? null : ledger.getBudgetSubjectCode());
            detail.setMasterProjectCode("NAN".equals(ledger.getMasterProjectCode()) ? null : ledger.getMasterProjectCode());
            detail.setErpAssetType("NAN".equals(ledger.getErpAssetType()) ? null : ledger.getErpAssetType());
            detail.setCurrency(ledger.getCurrency());
            if ("1".equals(ledger.getEffectType())) {
                detail.setAdjustAmountTotalInvestment(ledger.getAmount());
                detail.setAdjustAmountQ1(BigDecimal.ZERO);
                detail.setAdjustAmountQ2(BigDecimal.ZERO);
                detail.setAdjustAmountQ3(BigDecimal.ZERO);
                detail.setAdjustAmountQ4(BigDecimal.ZERO);
            } else {
                detail.setAdjustAmountQ1(ledger.getAmountConsumedQOne());
                detail.setAdjustAmountQ2(ledger.getAmountConsumedQTwo());
                detail.setAdjustAmountQ3(ledger.getAmountConsumedQThree());
                detail.setAdjustAmountQ4(ledger.getAmountConsumedQFour());
            }
            if (StringUtils.isNotBlank(ledger.getMetadata())) {
                detail.setMetadata(parseJsonToMap(ledger.getMetadata()));
            }
            adjustDetails.add(detail);
        }
        adjustApplyReqInfo.setAdjustDetails(adjustDetails);
        params.setAdjustApplyReqInfo(adjustApplyReqInfo);
        return params;
    }

    /**
     * 组装预算调整（ADJUST）审批参数（从操作表）
     */
    private BudgetAdjustRenewParams assembleAdjustRenewParamsFromOperate(BudgetLedgerHeadForOperate head) {
        BudgetAdjustRenewParams params = new BudgetAdjustRenewParams();
        params.setEsbInfo(ESBInfoParams.builder()
                .instId(generateInstId())
                .requestTime(formatRequestTime(head.getUpdateTime()))
                .attr1("")
                .attr2("")
                .attr3("")
                .build());
        AdjustRenewReqInfoParams renew = new AdjustRenewReqInfoParams();
        renew.setAdjustOrderNo(head.getBizCode());
        renew.setDocumentStatus("APPROVED");
        renew.setApprover("system");
        renew.setApproveTime(formatOperateTime(head.getUpdateTime()));
        renew.setApproveComment("同意审批");
        params.setAdjustRenewReqInfo(renew);
        return params;
    }

    /**
     * 组装 BudgetAdjustApplyParams
     */
    private BudgetAdjustApplyParams assembleBudgetAdjustApplyParams(BudgetLedgerHeadForOperate head, List<BudgetLedgerForOperate> ledgers) {
        BudgetAdjustApplyParams params = new BudgetAdjustApplyParams();

        // 组装ESBInfoParams
        ESBInfoParams esbInfo = ESBInfoParams.builder()
                .requestTime(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS")))
                .build();
        params.setEsbInfo(esbInfo);

        // 组装AdjustApplyReqInfoParams
        AdjustApplyReqInfoParams adjustApplyReqInfo = new AdjustApplyReqInfoParams();
        adjustApplyReqInfo.setAdjustOrderNo(head.getBizCode());
        adjustApplyReqInfo.setDocumentName(head.getDocumentName());
        adjustApplyReqInfo.setDataSource(head.getDataSource());
        adjustApplyReqInfo.setDocumentStatus("INITIAL_SUBMITTED");
        adjustApplyReqInfo.setOperator(head.getOperator() != null ? head.getOperator() : head.getCreator());
        adjustApplyReqInfo.setOperatorNo(head.getOperatorNo());
        adjustApplyReqInfo.setOperateTime(head.getCreateTime() != null 
                ? head.getCreateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) 
                : LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));

        // 从第一条明细获取 isInternal（单据级，所有明细一致）
        String isInternal = ledgers.isEmpty() ? "1" : ledgers.get(0).getIsInternal();
        if (StringUtils.isBlank(isInternal)) {
            isInternal = "1";
        }
        adjustApplyReqInfo.setIsInternal(isInternal);

        // 组装明细列表
        List<AdjustDetailDetailVo> adjustDetails = new ArrayList<>();
        for (BudgetLedgerForOperate ledger : ledgers) {
            AdjustDetailDetailVo detail = new AdjustDetailDetailVo();
            detail.setAdjustYear(ledger.getYear());
            detail.setAdjustMonth(ledger.getMonth());
            detail.setManagementOrg(ledger.getMorgCode());
            // 科目如果为NAN-NAN，设置为空
            String budgetSubjectCode = ledger.getBudgetSubjectCode();
            detail.setBudgetSubjectCode("NAN-NAN".equals(budgetSubjectCode) ? null : budgetSubjectCode);
            // 项目如果为NAN，设置为空
            String masterProjectCode = ledger.getMasterProjectCode();
            detail.setMasterProjectCode("NAN".equals(masterProjectCode) ? null : masterProjectCode);
            // 资产类型如果为NAN，设置为空
            String erpAssetType = ledger.getErpAssetType();
            detail.setErpAssetType("NAN".equals(erpAssetType) ? null : erpAssetType);
            detail.setCurrency(ledger.getCurrency());
            detail.setEffectType(ledger.getEffectType());
            
            // 根据 effectType 设置不同的金额字段
            if ("1".equals(ledger.getEffectType())) {
                // effectType=1：投资额调整，使用 amount 字段作为 adjustAmountTotalInvestment
                detail.setAdjustAmountTotalInvestment(ledger.getAmount());
            } else {
                // effectType=0 或 2：使用季度金额
                detail.setAdjustAmountQ1(ledger.getAmountConsumedQOne());
                detail.setAdjustAmountQ2(ledger.getAmountConsumedQTwo());
                detail.setAdjustAmountQ3(ledger.getAmountConsumedQThree());
                detail.setAdjustAmountQ4(ledger.getAmountConsumedQFour());
            }
            
            // 解析metadata
            if (StringUtils.isNotBlank(ledger.getMetadata())) {
                Map<String, String> metadata = parseJsonToMap(ledger.getMetadata());
                detail.setMetadata(metadata);
            }

            adjustDetails.add(detail);
        }
        adjustApplyReqInfo.setAdjustDetails(adjustDetails);

        params.setAdjustApplyReqInfo(adjustApplyReqInfo);
        return params;
    }
}

