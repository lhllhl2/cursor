package com.jasolar.mis.module.system.controller.budget.oa;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.jasolar.mis.framework.common.pojo.CommonResult;
import com.jasolar.mis.framework.common.util.http.HttpUtils;
import com.jasolar.mis.module.system.domain.budget.BudgetOaApprovalPushTask;
import com.jasolar.mis.module.system.domain.budget.HspPlanningUnitWithMemberView;
import com.jasolar.mis.module.system.mapper.budget.BudgetOaApprovalPushTaskMapper;
import com.jasolar.mis.module.system.mapper.budget.HspPlanningUnitWithMemberViewMapper;
import com.alibaba.fastjson.JSONObject;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.annotation.security.PermitAll;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * OA 审批流定时任务测试接口
 */
@Slf4j
@RestController
@Tag(name = "OA - 审批流定时任务")
@RequestMapping("/budget/oa/cron")
public class OaApprovalFlowCronController {

    private static final String FIXED_SCENARIO_NAME = "预算";
    private static final String FIXED_VERSION_ID = "54338";
    private static final Integer FIXED_PROCESS_STATE = 2;
    private static final String FIXED_STATUS_SUBMITTED = "SUBMITTED";
    private static final String FIXED_STATUS_REJECTED = "REJECTED";
    private static final String FIXED_STATUS_CALCULATING = "CALCULATING";
    private static final String FIXED_STATUS_CALCULATED = "CALCULATED";
    private static final String FIXED_STATUS_SENDED = "SENDED";
    private static final String OA_CREATE_URL = "http://esbqas.jasolar.com:8011/ESB_OASB/OAStandardCreationProcessSyncRestProxy";
    private static final String OA_UPDATE_URL = "http://esbqas.jasolar.com:8011/ESB_OASB/OAStandardUpdateProcessSyncRestProxy";
    private static final String OA_USERNAME = "hsp_bcs01";
    private static final String OA_PASSWORD = "4936b9a9d01c416f9a2778292061d83c";
    private static final String OA_REQUEST_NAME = "预算汇总编制流程";
    private static final String OA_WORKFLOW_ID = "698924";
    private static final Set<String> FIXED_ENTITY_IDS = new HashSet<>(Arrays.asList(
            "E01010202", "E0101020208", "E0101020205", "E0101020207",
            "E0101020204", "E0101020206", "E0101020203", "E0101020209"
    ));

    @Resource
    private HspPlanningUnitWithMemberViewMapper hspPlanningUnitWithMemberViewMapper;

    @Resource
    private BudgetOaApprovalPushTaskMapper budgetOaApprovalPushTaskMapper;

    @Value("${budget.oa.python-run-url:http://172.28.49.16:18080/run}")
    private String pythonRunUrl;

    @GetMapping("/hello")
    @PermitAll
    @Operation(summary = "OA 审批流定时任务测试接口")
    public CommonResult<String> hello() {
        StringBuilder resultSummary = new StringBuilder();
        // 第一步：从 DATAINTEGRATION.V_HSP_PLANNING_UNIT_WITH_MEMBER 视图获取
        // SCENARIO_NAME=预算、VERSION_ID=54338、ENTITY_ID（固定列表）且 PROCESS_STATE=2，
        // 且 PATH_PRIMARY_MEMBER_CD 与 MORG_CODE 相同且两者非空的数据，
        // 并插入 BUDGET_OA_APPROVAL_PUSH_TASK 表。
        List<HspPlanningUnitWithMemberView> sourceRows =
                hspPlanningUnitWithMemberViewMapper.selectForInitialPush(
                        FIXED_SCENARIO_NAME,
                        FIXED_VERSION_ID,
                        FIXED_ENTITY_IDS,
                        FIXED_PROCESS_STATE
                );
        int insertedCount = 0;
        int resetRejectedToSubmitted = 0;
        if (sourceRows == null || sourceRows.isEmpty()) {
            log.info("OA approval flow cron step1 no data, scenario={}, versionId={}, processState={}",
                    FIXED_SCENARIO_NAME, FIXED_VERSION_ID, FIXED_PROCESS_STATE);
        } else {
            Set<String> sourceEntityIds = sourceRows.stream()
                    .map(HspPlanningUnitWithMemberView::getEntityId)
                    .collect(Collectors.toSet());
            List<BudgetOaApprovalPushTask> existingTasks = budgetOaApprovalPushTaskMapper.selectList(
                    new LambdaQueryWrapper<BudgetOaApprovalPushTask>()
                            .in(BudgetOaApprovalPushTask::getEntityId, sourceEntityIds)
                            .eq(BudgetOaApprovalPushTask::getDeleted, false)
            );
            Set<String> existingEntityIdSet = existingTasks.stream()
                    .map(BudgetOaApprovalPushTask::getEntityId)
                    .collect(Collectors.toSet());
            Set<String> rejectedEntityIds = existingTasks.stream()
                    .filter(task -> FIXED_STATUS_REJECTED.equals(task.getStatus()))
                    .map(BudgetOaApprovalPushTask::getEntityId)
                    .collect(Collectors.toSet());

            for (String entityId : rejectedEntityIds) {
                resetRejectedToSubmitted += budgetOaApprovalPushTaskMapper.update(
                        null,
                        new LambdaUpdateWrapper<BudgetOaApprovalPushTask>()
                                .set(BudgetOaApprovalPushTask::getStatus, FIXED_STATUS_SUBMITTED)
                                .eq(BudgetOaApprovalPushTask::getEntityId, entityId)
                                .eq(BudgetOaApprovalPushTask::getDeleted, false)
                                .eq(BudgetOaApprovalPushTask::getStatus, FIXED_STATUS_REJECTED)
                );
            }

            List<HspPlanningUnitWithMemberView> pendingRows = sourceRows.stream()
                    .filter(row -> !existingEntityIdSet.contains(row.getEntityId()))
                    .collect(Collectors.toList());

            Map<String, String> employeeNoByPlanUnitId = buildEmployeeNoByPlanUnitId(pendingRows);
            Map<String, String> employeeNoByMorgCode = buildEmployeeNoByMorgCode(pendingRows);

            List<BudgetOaApprovalPushTask> insertTasks = pendingRows.stream()
                    .map(row -> {
                        long id = IdWorker.getId();
                        return BudgetOaApprovalPushTask.builder()
                                .id(id)
                                .taskId(String.valueOf(id))
                                .entityId(row.getEntityId())
                                .morgCode(row.getMorgCode())
                                .parentMorgCode(Objects.equals(row.getIsApprovalLastLvl(), 1)
                                        ? row.getParentMorgCode() : null)
                                .morgType(row.getOrgType())
                                .morgName(row.getMorgName())
                                .status(FIXED_STATUS_SUBMITTED)
                                .versionId(row.getVersionId())
                                .versionName(row.getVersionName())
                                .sendTimes(0)
                                .isApprovalLevel(row.getIsApprovalLastLvl())
                                .scriptType(row.getScriptType())
                                .employeeNo(resolveEmployeeNo(row, employeeNoByPlanUnitId, employeeNoByMorgCode))
                                .build();
                    })
                    .collect(Collectors.toList());

            if (!insertTasks.isEmpty()) {
                budgetOaApprovalPushTaskMapper.insertBatch(insertTasks);
            }
            insertedCount = insertTasks.size();
            log.info("OA approval flow cron step1 finished, sourceRows={}, inserted={}, resetRejectedToSubmitted={}, skipped={}",
                    sourceRows.size(), insertTasks.size(), resetRejectedToSubmitted, sourceRows.size() - insertTasks.size());
        }
        resultSummary.append("step1 done, inserted=").append(insertedCount)
                .append(", resetRejectedToSubmitted=").append(resetRejectedToSubmitted);

        // 第二步：尽量执行，不阻断第三步
        String step2Result = executeStep2();
        resultSummary.append("; ").append(step2Result);

        // 第三步：CALCULATED 且 send_times >=0 的任务推送 OA
        String step3Result = executeStep3();
        resultSummary.append("; ").append(step3Result);
        return CommonResult.success(resultSummary.toString());
    }

    private String executeStep2() {
        BudgetOaApprovalPushTask occupiedTask = budgetOaApprovalPushTaskMapper.selectOne(
                new LambdaQueryWrapper<BudgetOaApprovalPushTask>()
                        .eq(BudgetOaApprovalPushTask::getDeleted, false)
                        .in(BudgetOaApprovalPushTask::getStatus, FIXED_STATUS_CALCULATING, FIXED_STATUS_CALCULATED)
                        .last("FETCH FIRST 1 ROWS ONLY")
        );
        if (Objects.nonNull(occupiedTask)) {
            log.info("OA approval flow cron step2 skipped, found occupied task, taskId={}, status={}",
                    occupiedTask.getTaskId(), occupiedTask.getStatus());
            return "step2 skipped, task occupied";
        }

        BudgetOaApprovalPushTask submitTask = budgetOaApprovalPushTaskMapper.selectOne(
                new LambdaQueryWrapper<BudgetOaApprovalPushTask>()
                        .eq(BudgetOaApprovalPushTask::getDeleted, false)
                        .eq(BudgetOaApprovalPushTask::getStatus, FIXED_STATUS_SUBMITTED)
                        .orderByAsc(BudgetOaApprovalPushTask::getUpdateTime)
                        .orderByAsc(BudgetOaApprovalPushTask::getId)
                        .last("FETCH FIRST 1 ROWS ONLY")
        );
        if (Objects.isNull(submitTask)) {
            log.info("OA approval flow cron step2 skipped, no SUBMITTED task");
            return "step2 skipped, no submitted task";
        }

        boolean lockSuccess = budgetOaApprovalPushTaskMapper.update(
                null,
                new LambdaUpdateWrapper<BudgetOaApprovalPushTask>()
                        .set(BudgetOaApprovalPushTask::getStatus, FIXED_STATUS_CALCULATING)
                        .eq(BudgetOaApprovalPushTask::getId, submitTask.getId())
                        .eq(BudgetOaApprovalPushTask::getDeleted, false)
                        .eq(BudgetOaApprovalPushTask::getStatus, FIXED_STATUS_SUBMITTED)
        ) == 1;
        if (!lockSuccess) {
            log.info("OA approval flow cron step2 skipped, task lock failed, taskId={}", submitTask.getTaskId());
            return "step2 skipped, task changed";
        }

        try {
            Map<String, Object> query = new HashMap<>(4);
            query.put("entity", submitTask.getMorgName());
            query.put("version", submitTask.getVersionName());
            query.put("script", submitTask.getScriptType());
            String requestUrl = HttpUtils.append(pythonRunUrl, query, null, false);
            String responseBody = HttpUtils.get(requestUrl, null);
            JSONObject responseJson = JSONObject.parseObject(responseBody);
            boolean accepted = responseJson != null
                    && responseJson.getBooleanValue("ok")
                    && "ACCEPTED".equalsIgnoreCase(responseJson.getString("code"));
            if (!accepted) {
                rollbackSubmittedStatus(submitTask.getId());
                log.warn("OA approval flow cron step2 python rejected, taskId={}, response={}",
                        submitTask.getTaskId(), responseBody);
                return "step2 rejected by python";
            }
            log.info("OA approval flow cron step2 submitted to python, taskId={}, entity={}, version={}, script={}",
                    submitTask.getTaskId(), submitTask.getMorgName(), submitTask.getVersionName(), submitTask.getScriptType());
            return "step2 pushed taskId=" + submitTask.getTaskId();
        } catch (Exception ex) {
            rollbackSubmittedStatus(submitTask.getId());
            log.error("OA approval flow cron step2 python call failed, taskId={}", submitTask.getTaskId(), ex);
            return "step2 failed to call python";
        }
    }

    private String executeStep3() {
        BudgetOaApprovalPushTask calculatedTask = budgetOaApprovalPushTaskMapper.selectOne(
                new LambdaQueryWrapper<BudgetOaApprovalPushTask>()
                        .eq(BudgetOaApprovalPushTask::getDeleted, false)
                        .eq(BudgetOaApprovalPushTask::getStatus, FIXED_STATUS_CALCULATED)
                        .orderByAsc(BudgetOaApprovalPushTask::getUpdateTime)
                        .orderByAsc(BudgetOaApprovalPushTask::getId)
                        .last("FETCH FIRST 1 ROWS ONLY")
        );
        if (calculatedTask == null) {
            return "step3 skipped, no calculated task";
        }
        int sendTimes = calculatedTask.getSendTimes() == null ? 0 : calculatedTask.getSendTimes();
        List<Map<String, Object>> t1Rows = hspPlanningUnitWithMemberViewMapper.selectPlan1RcT1ByMorgCode(calculatedTask.getMorgCode());
        List<Map<String, Object>> t2Rows = hspPlanningUnitWithMemberViewMapper.selectPlan1RcT2ByMorgCode(calculatedTask.getMorgCode());
        List<Map<String, Object>> t3Rows = hspPlanningUnitWithMemberViewMapper.selectPlan1RcT3ByMorgCode(calculatedTask.getMorgCode());
        boolean hasT1 = t1Rows != null && !t1Rows.isEmpty();
        boolean hasT2 = t2Rows != null && !t2Rows.isEmpty();
        boolean hasT3 = t3Rows != null && !t3Rows.isEmpty();
        if (!hasT1 && !hasT2 && !hasT3) {
            String msg = "step3 failed, all OA views have no data, taskId=" + calculatedTask.getTaskId();
            log.error(msg);
            return msg;
        }

        JSONObject payload = buildOaPayload(calculatedTask, sendTimes, t1Rows, t2Rows, t3Rows);
        String targetUrl = sendTimes <= 0 ? OA_CREATE_URL : OA_UPDATE_URL;
        Map<String, String> headers = buildBasicHeaders();
        try {
            String responseBody = HttpUtils.post(targetUrl, headers, payload.toJSONString(), "application/json");
            JSONObject responseJson = JSONObject.parseObject(responseBody);
            if (!isOaSuccess(responseJson)) {
                log.error("OA approval flow cron step3 OA rejected, taskId={}, response={}",
                        calculatedTask.getTaskId(), responseBody);
                return "step3 failed, OA rejected taskId=" + calculatedTask.getTaskId();
            }
            String requestId = extractRequestId(responseJson, calculatedTask.getRequestId());
            boolean updated = budgetOaApprovalPushTaskMapper.update(
                    null,
                    new LambdaUpdateWrapper<BudgetOaApprovalPushTask>()
                            .set(BudgetOaApprovalPushTask::getStatus, FIXED_STATUS_SENDED)
                            .set(BudgetOaApprovalPushTask::getRequestId, requestId)
                            .setSql("SEND_TIMES = NVL(SEND_TIMES, 0) + 1")
                            .eq(BudgetOaApprovalPushTask::getId, calculatedTask.getId())
                            .eq(BudgetOaApprovalPushTask::getDeleted, false)
                            .eq(BudgetOaApprovalPushTask::getStatus, FIXED_STATUS_CALCULATED)
            ) == 1;
            if (!updated) {
                return "step3 warning, OA sent but db update skipped taskId=" + calculatedTask.getTaskId();
            }
            return "step3 sent taskId=" + calculatedTask.getTaskId() + ", requestId=" + requestId;
        } catch (Exception ex) {
            log.error("OA approval flow cron step3 call failed, taskId={}", calculatedTask.getTaskId(), ex);
            return "step3 failed to call OA";
        }
    }

    private JSONObject buildOaPayload(BudgetOaApprovalPushTask task,
                                      int sendTimes,
                                      List<Map<String, Object>> t1Rows,
                                      List<Map<String, Object>> t2Rows,
                                      List<Map<String, Object>> t3Rows) {
        JSONObject root = new JSONObject(true);
        JSONObject esbInfo = new JSONObject(true);
        esbInfo.put("requestTime", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS")));
        esbInfo.put("instId", "cron-" + task.getTaskId() + "-" + System.currentTimeMillis());
        esbInfo.put("attr1", "");
        esbInfo.put("attr2", "");
        esbInfo.put("attr3", "");
        root.put("esbInfo", esbInfo);

        JSONObject requestInfo = new JSONObject(true);
        if (sendTimes <= 0) {
            requestInfo.put("requestName", OA_REQUEST_NAME);
            requestInfo.put("requestLevel", 0);
            requestInfo.put("workflowId", OA_WORKFLOW_ID);
        } else {
            requestInfo.put("requestId", task.getRequestId());
        }
        String operatorNo = task.getEmployeeNo();
        requestInfo.put("userid", operatorNo);
        requestInfo.put("remark", "");
        requestInfo.put("mainData", buildMainData(task, operatorNo));
        requestInfo.put("detailData", buildDetailData(t1Rows, t2Rows, t3Rows));
        root.put("requestInfo", requestInfo);
        return root;
    }

    private List<JSONObject> buildMainData(BudgetOaApprovalPushTask task, String operatorNo) {
        List<JSONObject> fields = new ArrayList<>();
        fields.add(field("fqr", operatorNo));
        fields.add(field("fqbm", operatorNo));
        fields.add(field("gs", operatorNo));
        fields.add(field("fqrq", LocalDate.now().toString()));
        fields.add(field("bbh", task.getVersionId()));
        fields.add(field("ysbzh", task.getTaskId()));
        fields.add(field("sjly", "0"));
        return fields;
    }

    private List<JSONObject> buildDetailData(List<Map<String, Object>> t1Rows,
                                             List<Map<String, Object>> t2Rows,
                                             List<Map<String, Object>> t3Rows) {
        List<JSONObject> detailData = new ArrayList<>();
        if (t1Rows != null && !t1Rows.isEmpty()) {
            detailData.add(detailTable("formtable_main_960_dt1", t1Rows));
        }
        if (t2Rows != null && !t2Rows.isEmpty()) {
            detailData.add(detailTable("formtable_main_960_dt2", t2Rows));
        }
        if (t3Rows != null && !t3Rows.isEmpty()) {
            detailData.add(detailTable("formtable_main_960_dt3", t3Rows));
        }
        return detailData;
    }

    private JSONObject detailTable(String tableName, List<Map<String, Object>> rows) {
        JSONObject table = new JSONObject(true);
        table.put("tableDBName", tableName);
        List<JSONObject> records = new ArrayList<>();
        for (int i = 0; i < rows.size(); i++) {
            JSONObject record = new JSONObject(true);
            record.put("recordOrder", i);
            List<JSONObject> fields = new ArrayList<>();
            for (Map.Entry<String, Object> entry : rows.get(i).entrySet()) {
                String key = entry.getKey();
                if (key == null) {
                    continue;
                }
                Object value = entry.getValue();
                if (value == null) {
                    continue;
                }
                fields.add(field(key.toLowerCase(), value));
            }
            record.put("workflowRequestTableFields", fields);
            records.add(record);
        }
        table.put("workflowRequestTableRecords", records);
        return table;
    }

    private JSONObject field(String fieldName, Object fieldValue) {
        JSONObject field = new JSONObject(true);
        field.put("fieldName", fieldName);
        field.put("fieldValue", fieldValue);
        return field;
    }

    private Map<String, String> buildBasicHeaders() {
        Map<String, String> headers = new LinkedHashMap<>();
        String token = OA_USERNAME + ":" + OA_PASSWORD;
        String base64 = Base64.getEncoder().encodeToString(token.getBytes(StandardCharsets.UTF_8));
        headers.put("Authorization", "Basic " + base64);
        headers.put("Content-Type", "application/json");
        return headers;
    }

    private boolean isOaSuccess(JSONObject responseJson) {
        if (responseJson == null) {
            return false;
        }
        JSONObject esbInfo = responseJson.getJSONObject("esbInfo");
        JSONObject resultInfo = responseJson.getJSONObject("resultInfo");
        String returnCode = esbInfo == null ? null : esbInfo.getString("returnCode");
        String returnStatus = esbInfo == null ? null : esbInfo.getString("returnStatus");
        String code = resultInfo == null ? null : resultInfo.getString("code");
        return "0".equals(returnCode)
                && "S".equalsIgnoreCase(returnStatus)
                && "SUCCESS".equalsIgnoreCase(code);
    }

    private String extractRequestId(JSONObject responseJson, String fallbackRequestId) {
        if (responseJson == null) {
            return fallbackRequestId;
        }
        JSONObject resultInfo = responseJson.getJSONObject("resultInfo");
        JSONObject data = resultInfo == null ? null : resultInfo.getJSONObject("data");
        Object requestId = data == null ? null : data.get("requestid");
        if (requestId == null) {
            requestId = data == null ? null : data.get("requestId");
        }
        return requestId == null ? fallbackRequestId : String.valueOf(requestId);
    }

    private Map<String, String> buildEmployeeNoByPlanUnitId(List<HspPlanningUnitWithMemberView> sourceRows) {
        Set<String> planUnitIds = sourceRows.stream()
                .filter(row -> Objects.equals(row.getIsApprovalLastLvl(), 1))
                .map(HspPlanningUnitWithMemberView::getPlanUnitId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        if (planUnitIds.isEmpty()) {
            return Collections.emptyMap();
        }
        List<Map<String, Object>> queryRows = hspPlanningUnitWithMemberViewMapper.selectEmployeeNoByPlanUnitIds(planUnitIds);
        if (queryRows == null || queryRows.isEmpty()) {
            return Collections.emptyMap();
        }
        Map<String, String> result = new HashMap<>(queryRows.size());
        for (Map<String, Object> row : queryRows) {
            Object key = readMapValue(row, "planUnitId");
            Object value = readMapValue(row, "employeeNo");
            if (key != null && value != null) {
                result.put(String.valueOf(key), String.valueOf(value));
            }
        }
        return result;
    }

    private Map<String, String> buildEmployeeNoByMorgCode(List<HspPlanningUnitWithMemberView> sourceRows) {
        Set<String> morgCodes = sourceRows.stream()
                .filter(row -> Objects.equals(row.getIsApprovalLastLvl(), 0))
                .map(HspPlanningUnitWithMemberView::getMorgCode)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        if (morgCodes.isEmpty()) {
            return Collections.emptyMap();
        }
        List<Map<String, Object>> queryRows = hspPlanningUnitWithMemberViewMapper.selectEmployeeNoByMorgCodes(morgCodes);
        if (queryRows == null || queryRows.isEmpty()) {
            return Collections.emptyMap();
        }
        Map<String, String> result = new HashMap<>(queryRows.size());
        for (Map<String, Object> row : queryRows) {
            Object key = readMapValue(row, "morgCode");
            Object value = readMapValue(row, "employeeNo");
            if (key != null && value != null) {
                result.put(String.valueOf(key), String.valueOf(value));
            }
        }
        return result;
    }

    private String resolveEmployeeNo(HspPlanningUnitWithMemberView row,
                                     Map<String, String> employeeNoByPlanUnitId,
                                     Map<String, String> employeeNoByMorgCode) {
        if (Objects.equals(row.getIsApprovalLastLvl(), 1)) {
            return employeeNoByPlanUnitId.get(row.getPlanUnitId());
        }
        if (Objects.equals(row.getIsApprovalLastLvl(), 0)) {
            return employeeNoByMorgCode.get(row.getMorgCode());
        }
        return null;
    }

    private Object readMapValue(Map<String, Object> row, String key) {
        Object value = row.get(key);
        if (value != null) {
            return value;
        }
        value = row.get(key.toUpperCase());
        if (value != null) {
            return value;
        }
        return row.get(key.toLowerCase());
    }

    private void rollbackSubmittedStatus(Long taskId) {
        budgetOaApprovalPushTaskMapper.update(
                null,
                new LambdaUpdateWrapper<BudgetOaApprovalPushTask>()
                        .set(BudgetOaApprovalPushTask::getStatus, FIXED_STATUS_SUBMITTED)
                        .eq(BudgetOaApprovalPushTask::getId, taskId)
                        .eq(BudgetOaApprovalPushTask::getDeleted, false)
                        .eq(BudgetOaApprovalPushTask::getStatus, FIXED_STATUS_CALCULATING)
        );
    }
}
