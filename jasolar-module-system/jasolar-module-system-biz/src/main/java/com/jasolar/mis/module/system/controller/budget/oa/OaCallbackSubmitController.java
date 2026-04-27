package com.jasolar.mis.module.system.controller.budget.oa;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.jasolar.mis.module.system.controller.budget.vo.ESBInfoParams;
import com.jasolar.mis.module.system.controller.budget.vo.ESBRespInfoVo;
import com.jasolar.mis.module.system.domain.budget.BudgetOaApprovalPushTask;
import com.jasolar.mis.module.system.domain.budget.HspPmMemberOwnerWithMemberNameCodeView;
import com.jasolar.mis.module.system.mapper.budget.BudgetOaApprovalPushTaskMapper;
import com.jasolar.mis.module.system.mapper.budget.HspPmMemberOwnerWithMemberNameCodeViewMapper;
import com.jasolar.mis.module.system.mapper.budget.OaCallbackPlanningUnitMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.annotation.security.PermitAll;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * OA 回调预算系统接口（REQUEST01）
 */
@Slf4j
@RestController
@Tag(name = "OA - 回调预算系统")
@RequestMapping("/budget/oa/callback")
public class OaCallbackSubmitController {

    private static final String RETURN_CODE_SUCCESS = "A0001-SAP";
    private static final String RETURN_CODE_FAILED = "A0002-SAP";
    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
    private static final String OPERATION_APPROVED = "APPROVED";
    private static final String OPERATION_REJECTED = "REJECTED";
    private static final String FIXED_SCENARIO_NAME = "预算";
    private static final String ORG_TYPE_SECOND_REGION = "二级区域";
    private static final String TASK_STATUS_REJECTED = "REJECTED";
    private static final String TASK_STATUS_SUBMITTED = "SUBMITTED";
    private static final Integer ORG_CALLBACK_TYPE_REJECTED = 1;

    @Resource
    private BudgetOaApprovalPushTaskMapper budgetOaApprovalPushTaskMapper;

    @Resource
    private OaCallbackPlanningUnitMapper oaCallbackPlanningUnitMapper;

    @Resource
    private HspPmMemberOwnerWithMemberNameCodeViewMapper hspPmMemberOwnerWithMemberNameCodeViewMapper;

    @PostMapping("/submit")
    @PermitAll
    @Operation(summary = "REQUEST01 - OA回调提交接口")
    @Transactional(rollbackFor = Exception.class)
    public OaCallbackSubmitResponse submit(
            @RequestBody @Valid OaCallbackSubmitRequest request) {
        log.info("oa callback submit request, instId={}, requestInfo={}",
                request.getEsbInfo().getInstId(),
                request.getRequestInfo());

        try {
            processRequestInfo(request.getRequestInfo());
            return buildResponse(
                    request.getEsbInfo(),
                    "S",
                    RETURN_CODE_SUCCESS,
                    "接收成功"
            );
        } catch (Exception ex) {
            log.error("oa callback submit failed, instId={}, msg={}", request.getEsbInfo().getInstId(), ex.getMessage(), ex);
            return buildResponse(
                    request.getEsbInfo(),
                    "E",
                    RETURN_CODE_FAILED,
                    ex.getMessage()
            );
        }
    }

    private void processRequestInfo(RequestInfo requestInfo) {
        String operationType = normalizeOperationType(requestInfo.getOperationType());
        String scenarioId = resolveScenarioId(FIXED_SCENARIO_NAME);
        List<OrgNode> requestOrgs = requestInfo.getRequestData().getOrgs();
        List<OrgNode> effectiveOrgs = requestOrgs;
        if (OPERATION_REJECTED.equals(operationType) && requestOrgs.size() > 1) {
            boolean hasTypeInRequest = requestOrgs.stream().anyMatch(org -> org.getType() != null);
            if (hasTypeInRequest) {
                effectiveOrgs = requestOrgs.stream()
                        .filter(org -> Objects.equals(org.getType(), ORG_CALLBACK_TYPE_REJECTED))
                        .collect(Collectors.toList());
                if (effectiveOrgs.isEmpty()) {
                    throw new IllegalArgumentException("多组织驳回时未传入 type=1 的组织");
                }
            }
        }

        Set<String> orgCds = effectiveOrgs.stream()
                .map(OrgNode::getOrgCd)
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toCollection(HashSet::new));
        if (orgCds.isEmpty()) {
            throw new IllegalArgumentException("orgs 为空");
        }
        boolean allowMissingTask = OPERATION_REJECTED.equals(operationType) && orgCds.size() > 1;
        Map<String, BudgetOaApprovalPushTask> taskByMorgCode = queryTaskByMorgCodes(orgCds, allowMissingTask);
        List<BudgetOaApprovalPushTask> selectedTasks = orgCds.stream()
                .map(taskByMorgCode::get)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        if (selectedTasks.isEmpty()) {
            throw new IllegalArgumentException("组织任务不存在，orgCds=" + String.join(",", orgCds));
        }

        if (OPERATION_APPROVED.equals(operationType)) {
            if (orgCds.size() == 1) {
                handleApprovedSingle(selectedTasks.get(0), scenarioId);
            } else {
                handleApprovedMulti(selectedTasks, scenarioId);
            }
            return;
        }

        if (OPERATION_REJECTED.equals(operationType)) {
            if (orgCds.size() == 1) {
                handleRejectedSingle(selectedTasks.get(0), scenarioId);
            } else {
                handleRejectedMulti(selectedTasks, orgCds, scenarioId);
            }
            return;
        }

        throw new IllegalArgumentException("不支持的 operationType: " + requestInfo.getOperationType());
    }

    private String resolveScenarioId(String scenarioName) {
        String scenarioId = oaCallbackPlanningUnitMapper.selectMemberIdByMemberNm(scenarioName);
        if (scenarioId == null || scenarioId.trim().isEmpty()) {
            throw new IllegalArgumentException("未找到场景ID，memberNm=" + scenarioName);
        }
        return scenarioId.trim();
    }

    private String normalizeOperationType(String operationType) {
        if (operationType == null || operationType.trim().isEmpty()) {
            throw new IllegalArgumentException("operationType 为空");
        }
        return operationType.trim().toUpperCase(Locale.ROOT);
    }

    private Map<String, BudgetOaApprovalPushTask> queryTaskByMorgCodes(Set<String> orgCds, boolean allowMissingTask) {
        List<BudgetOaApprovalPushTask> tasks = budgetOaApprovalPushTaskMapper.selectList(
                new LambdaQueryWrapper<BudgetOaApprovalPushTask>()
                        .in(BudgetOaApprovalPushTask::getMorgCode, orgCds)
                        .eq(BudgetOaApprovalPushTask::getDeleted, false)
        );
        Map<String, BudgetOaApprovalPushTask> taskByMorgCode = new HashMap<>(tasks.size());
        for (BudgetOaApprovalPushTask task : tasks) {
            BudgetOaApprovalPushTask existed = taskByMorgCode.putIfAbsent(task.getMorgCode(), task);
            if (existed != null) {
                throw new IllegalArgumentException("组织存在多条任务数据，orgCd=" + task.getMorgCode());
            }
        }
        List<String> notFoundOrgCds = orgCds.stream()
                .filter(orgCd -> !taskByMorgCode.containsKey(orgCd))
                .collect(Collectors.toList());
        if (!notFoundOrgCds.isEmpty()) {
            if (allowMissingTask) {
                log.warn("部分组织未查到任务，已按忽略策略继续，orgCd={}", String.join(",", notFoundOrgCds));
                return taskByMorgCode;
            }
            throw new IllegalArgumentException("组织任务不存在，orgCd=" + String.join(",", notFoundOrgCds));
        }
        return taskByMorgCode;
    }

    private void handleApprovedSingle(BudgetOaApprovalPushTask task, String scenarioId) {
        if (Objects.equals(task.getIsApprovalLevel(), 1)) {
            approvePlanningUnitWithoutCache(task, scenarioId);
            processParentPlanningUnitAfterSingleApproved(task, scenarioId);
            oaCallbackPlanningUnitMapper.insertCacheResetAction();
            budgetOaApprovalPushTaskMapper.deleteByIdPhysical(task.getId());
            return;
        }

        if (!Objects.equals(task.getIsApprovalLevel(), 0)) {
            throw new IllegalArgumentException("IS_APPROVAL_LEVEL 非法，orgCd=" + task.getMorgCode());
        }

        Long childCount = budgetOaApprovalPushTaskMapper.selectCount(
                new LambdaQueryWrapper<BudgetOaApprovalPushTask>()
                        .eq(BudgetOaApprovalPushTask::getDeleted, false)
                        .eq(BudgetOaApprovalPushTask::getParentMorgCode, task.getMorgCode())
        );
        if (childCount != null && childCount > 0) {
            throw new IllegalArgumentException("存在未处理子组织任务，不允许通过，orgCd=" + task.getMorgCode());
        }

        approvePlanningUnitWithoutCache(task, scenarioId);
        oaCallbackPlanningUnitMapper.insertCacheResetAction();
        budgetOaApprovalPushTaskMapper.deleteByIdPhysical(task.getId());
    }

    private void handleApprovedMulti(List<BudgetOaApprovalPushTask> selectedTasks, String scenarioId) {
        List<BudgetOaApprovalPushTask> level0Tasks = selectedTasks.stream()
                .filter(task -> Objects.equals(task.getIsApprovalLevel(), 0))
                .collect(Collectors.toList());
        if (level0Tasks.size() != 1) {
            throw new IllegalArgumentException("多组织通过时必须有且仅有一条 IS_APPROVAL_LEVEL=0 的组织");
        }

        BudgetOaApprovalPushTask level0Task = level0Tasks.get(0);
        approvePlanningUnitAndRefreshCache(level0Task, scenarioId);
        deleteTasksPhysically(selectedTasks);
    }

    private void handleRejectedSingle(BudgetOaApprovalPushTask task, String scenarioId) {
        if (!Objects.equals(task.getIsApprovalLevel(), 1)) {
            throw new IllegalArgumentException("单组织驳回时必须是 IS_APPROVAL_LEVEL=1，orgCd=" + task.getMorgCode());
        }

        rejectPlanningUnitAndRefreshCache(task, scenarioId);
        budgetOaApprovalPushTaskMapper.update(
                null,
                new LambdaUpdateWrapper<BudgetOaApprovalPushTask>()
                        .set(BudgetOaApprovalPushTask::getStatus, TASK_STATUS_REJECTED)
                        .eq(BudgetOaApprovalPushTask::getId, task.getId())
                        .eq(BudgetOaApprovalPushTask::getDeleted, false)
        );
    }

    private void handleRejectedMulti(List<BudgetOaApprovalPushTask> selectedTasks, Set<String> requestOrgCds, String scenarioId) {
        List<BudgetOaApprovalPushTask> level0Tasks = selectedTasks.stream()
                .filter(task -> Objects.equals(task.getIsApprovalLevel(), 0))
                .collect(Collectors.toList());
        if (level0Tasks.isEmpty()) {
            throw new IllegalArgumentException("多组织驳回时必须包含 IS_APPROVAL_LEVEL=0 的组织");
        }

        Set<String> level0MorgCodes = level0Tasks.stream()
                .map(BudgetOaApprovalPushTask::getMorgCode)
                .collect(Collectors.toSet());
        Set<String> expectedChildMorgCodes = new HashSet<>();
        for (String level0MorgCode : level0MorgCodes) {
            List<String> childCodes = oaCallbackPlanningUnitMapper
                    .selectOrgCodesByParentAndType(level0MorgCode, ORG_TYPE_SECOND_REGION);
            if (childCodes != null) {
                expectedChildMorgCodes.addAll(childCodes);
            }
        }
        Set<String> requestLevel0MorgCodes = level0Tasks.stream()
                .map(BudgetOaApprovalPushTask::getMorgCode)
                .collect(Collectors.toSet());
        Set<String> requestLevel1MorgCodes = requestOrgCds.stream()
                .filter(orgCd -> !requestLevel0MorgCodes.contains(orgCd))
                .collect(Collectors.toSet());
        boolean allContained = requestLevel1MorgCodes.stream().allMatch(expectedChildMorgCodes::contains);
        if (!allContained) {
            throw new IllegalArgumentException("多组织驳回校验失败，存在不属于父组织子集的组织");
        }

        String versionId = level0Tasks.get(0).getVersionId();
        Set<String> level0MorgCodesForReject = level0Tasks.stream()
                .map(BudgetOaApprovalPushTask::getMorgCode)
                .collect(Collectors.toSet());
        batchRejectPlanningUnitAndRefreshCacheByOrgCds(requestOrgCds, level0MorgCodesForReject, versionId, scenarioId);

        List<Long> level1TaskIds = selectedTasks.stream()
                .filter(task -> Objects.equals(task.getIsApprovalLevel(), 1))
                .map(BudgetOaApprovalPushTask::getId)
                .collect(Collectors.toList());
        if (!level1TaskIds.isEmpty()) {
            budgetOaApprovalPushTaskMapper.deleteByIdsPhysical(level1TaskIds);
        }

        List<Long> level0TaskIds = selectedTasks.stream()
                .filter(task -> Objects.equals(task.getIsApprovalLevel(), 0))
                .map(BudgetOaApprovalPushTask::getId)
                .collect(Collectors.toList());
        if (!level0TaskIds.isEmpty()) {
            budgetOaApprovalPushTaskMapper.update(
                    null,
                    new LambdaUpdateWrapper<BudgetOaApprovalPushTask>()
                            .set(BudgetOaApprovalPushTask::getStatus, TASK_STATUS_REJECTED)
                            .in(BudgetOaApprovalPushTask::getId, level0TaskIds)
                            .eq(BudgetOaApprovalPushTask::getDeleted, false)
            );
        }
    }

    private void batchApprovePlanningUnitAndRefreshCache(List<BudgetOaApprovalPushTask> tasks, String scenarioId) {
        Map<String, OaCallbackPlanningUnitMapper.PlanningUnitPair> pairMap = new HashMap<>();
        for (BudgetOaApprovalPushTask task : tasks) {
            String key = task.getEntityId() + "|" + task.getVersionId() + "|" + scenarioId;
            pairMap.putIfAbsent(key, new OaCallbackPlanningUnitMapper.PlanningUnitPair(task.getEntityId(), task.getVersionId(), scenarioId));
        }
        int updateCount = oaCallbackPlanningUnitMapper.updateProcessStateApprovedBatch(new ArrayList<>(pairMap.values()));
        if (updateCount <= 0) {
            throw new IllegalArgumentException("批量审批通过更新规划单元失败");
        }
        oaCallbackPlanningUnitMapper.insertCacheResetAction();
    }

    private void processParentPlanningUnitAfterSingleApproved(BudgetOaApprovalPushTask task, String scenarioId) {
        if (task.getParentMorgCode() == null || task.getParentMorgCode().trim().isEmpty()) {
            return;
        }
        String parentMorgCode = task.getParentMorgCode().trim();
        List<String> childOrgCodes = oaCallbackPlanningUnitMapper
                .selectOrgCodesByParentAndType(parentMorgCode, ORG_TYPE_SECOND_REGION);
        if (childOrgCodes == null || childOrgCodes.isEmpty()) {
            return;
        }

        List<String> childMemberIds = oaCallbackPlanningUnitMapper.selectMemberIdsByMemberCds(childOrgCodes);
        if (childMemberIds == null || childMemberIds.isEmpty()) {
            return;
        }
        long notApprovedCount = oaCallbackPlanningUnitMapper.countNotApprovedPlanningUnits(
                childMemberIds,
                task.getVersionId(),
                scenarioId
        );
        if (notApprovedCount > 0) {
            return;
        }

        String parentMemberId = oaCallbackPlanningUnitMapper.selectMemberIdByMemberCd(parentMorgCode);
        if (parentMemberId == null || parentMemberId.trim().isEmpty()) {
            throw new IllegalArgumentException("未找到父组织memberId，parentMorgCode=" + parentMorgCode);
        }
        HspPmMemberOwnerWithMemberNameCodeView memberOwner = hspPmMemberOwnerWithMemberNameCodeViewMapper
                .selectOneByMemberCd(parentMorgCode);
        if (memberOwner == null || memberOwner.getPmMemberId() == null || memberOwner.getPmOwnerId() == null) {
            throw new IllegalArgumentException("未找到父组织对应PM成员数据，parentMorgCode=" + parentMorgCode);
        }
        int parentUpdateCount = oaCallbackPlanningUnitMapper.updatePathAndOwner(
                parentMemberId.trim(),
                task.getVersionId(),
                scenarioId,
                memberOwner.getPmMemberId(),
                memberOwner.getPmOwnerId()
        );
        if (parentUpdateCount <= 0) {
            throw new IllegalArgumentException("父组织规划单元回写失败，parentMorgCode=" + parentMorgCode);
        }
    }

    private void approvePlanningUnitWithoutCache(BudgetOaApprovalPushTask task, String scenarioId) {
        int updateCount = oaCallbackPlanningUnitMapper.updateProcessStateApproved(task.getEntityId(), task.getVersionId(), scenarioId);
        if (updateCount <= 0) {
            throw new IllegalArgumentException("审批通过更新规划单元失败，entityId=" + task.getEntityId()
                    + ", versionId=" + task.getVersionId() + ", scenarioId=" + scenarioId);
        }
    }

    private void approvePlanningUnitAndRefreshCache(BudgetOaApprovalPushTask task, String scenarioId) {
        approvePlanningUnitWithoutCache(task, scenarioId);
        oaCallbackPlanningUnitMapper.insertCacheResetAction();
    }

    private void rejectPlanningUnitAndRefreshCache(BudgetOaApprovalPushTask task, String scenarioId) {
        HspPmMemberOwnerWithMemberNameCodeView memberOwner = hspPmMemberOwnerWithMemberNameCodeViewMapper
                .selectOneByMemberCd(task.getMorgCode());
        if (memberOwner == null) {
            throw new IllegalArgumentException("未找到组织对应的PM成员数据，orgCd=" + task.getMorgCode());
        }
        if (memberOwner.getPmMemberId() == null || memberOwner.getPmOwnerId() == null) {
            throw new IllegalArgumentException("PM成员数据不完整，orgCd=" + task.getMorgCode());
        }
        int updateCount = oaCallbackPlanningUnitMapper.updatePathOwnerAndProcessStateRejected(
                task.getEntityId(),
                task.getVersionId(),
                scenarioId,
                memberOwner.getPmMemberId(),
                memberOwner.getPmOwnerId()
        );
        if (updateCount <= 0) {
            throw new IllegalArgumentException("驳回更新规划单元失败，entityId=" + task.getEntityId()
                    + ", versionId=" + task.getVersionId() + ", scenarioId=" + scenarioId);
        }
        oaCallbackPlanningUnitMapper.insertCacheResetAction();
    }

    private void batchRejectPlanningUnitAndRefreshCache(List<BudgetOaApprovalPushTask> tasks, String scenarioId) {
        Set<String> morgCodes = tasks.stream()
                .map(BudgetOaApprovalPushTask::getMorgCode)
                .collect(Collectors.toSet());
        List<HspPmMemberOwnerWithMemberNameCodeView> memberOwners =
                hspPmMemberOwnerWithMemberNameCodeViewMapper.selectByMemberCds(morgCodes);
        Map<String, HspPmMemberOwnerWithMemberNameCodeView> memberOwnerMap = new HashMap<>();
        for (HspPmMemberOwnerWithMemberNameCodeView memberOwner : memberOwners) {
            memberOwnerMap.putIfAbsent(memberOwner.getMemberCd(), memberOwner);
        }

        Map<String, OaCallbackPlanningUnitMapper.PlanningUnitOwnerUpdate> updateMap = new HashMap<>();
        for (BudgetOaApprovalPushTask task : tasks) {
            HspPmMemberOwnerWithMemberNameCodeView memberOwner = memberOwnerMap.get(task.getMorgCode());
            if (memberOwner == null) {
                throw new IllegalArgumentException("未找到组织对应的PM成员数据，orgCd=" + task.getMorgCode());
            }
            if (memberOwner.getPmMemberId() == null || memberOwner.getPmOwnerId() == null) {
                throw new IllegalArgumentException("PM成员数据不完整，orgCd=" + task.getMorgCode());
            }
            String key = task.getEntityId() + "|" + task.getVersionId() + "|" + scenarioId;
            updateMap.put(key, new OaCallbackPlanningUnitMapper.PlanningUnitOwnerUpdate(
                    task.getEntityId(),
                    task.getVersionId(),
                    scenarioId,
                    memberOwner.getPmMemberId(),
                    memberOwner.getPmOwnerId()
            ));
        }

        int updateCount = oaCallbackPlanningUnitMapper.updatePathAndOwnerBatch(new ArrayList<>(updateMap.values()));
        if (updateCount <= 0) {
            throw new IllegalArgumentException("批量驳回更新规划单元失败");
        }
        oaCallbackPlanningUnitMapper.insertCacheResetAction();
    }

    private void batchRejectPlanningUnitAndRefreshCacheByOrgCds(Set<String> orgCds,
                                                                Set<String> level0MorgCodes,
                                                                String versionId,
                                                                String scenarioId) {
        List<HspPmMemberOwnerWithMemberNameCodeView> memberOwners =
                hspPmMemberOwnerWithMemberNameCodeViewMapper.selectByMemberCds(orgCds);
        Map<String, HspPmMemberOwnerWithMemberNameCodeView> memberOwnerMap = new HashMap<>();
        for (HspPmMemberOwnerWithMemberNameCodeView memberOwner : memberOwners) {
            memberOwnerMap.putIfAbsent(memberOwner.getMemberCd(), memberOwner);
        }

        int updateCount = 0;
        for (String orgCd : orgCds) {
            String entityId = oaCallbackPlanningUnitMapper.selectMemberIdByMemberCd(orgCd);
            if (entityId == null || entityId.trim().isEmpty()) {
                throw new IllegalArgumentException("未找到组织对应 MEMBER_ID，orgCd=" + orgCd);
            }
            String pathNodeId;
            String ownerGroupId;
            if (level0MorgCodes.contains(orgCd)) {
                pathNodeId = null;
                ownerGroupId = null;
            } else {
                HspPmMemberOwnerWithMemberNameCodeView memberOwner = memberOwnerMap.get(orgCd);
                if (memberOwner == null || memberOwner.getPmMemberId() == null || memberOwner.getPmOwnerId() == null) {
                    throw new IllegalArgumentException("PM成员数据不完整，orgCd=" + orgCd);
                }
                pathNodeId = memberOwner.getPmMemberId();
                ownerGroupId = memberOwner.getPmOwnerId();
            }
            int currentUpdateCount = oaCallbackPlanningUnitMapper.updatePathOwnerAndProcessStateRejected(
                    entityId.trim(),
                    versionId,
                    scenarioId,
                    pathNodeId,
                    ownerGroupId
            );
            if (currentUpdateCount <= 0) {
                throw new IllegalArgumentException("组织驳回更新失败，orgCd=" + orgCd
                        + ", entityId=" + entityId + ", versionId=" + versionId + ", scenarioId=" + scenarioId);
            }
            updateCount += currentUpdateCount;
        }
        if (updateCount <= 0) {
            throw new IllegalArgumentException("批量驳回更新规划单元失败");
        }
        oaCallbackPlanningUnitMapper.insertCacheResetAction();
    }

    private void deleteTasksPhysically(List<BudgetOaApprovalPushTask> tasks) {
        List<Long> ids = tasks.stream()
                .map(BudgetOaApprovalPushTask::getId)
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(ArrayList::new));
        if (ids.isEmpty()) {
            return;
        }
        budgetOaApprovalPushTaskMapper.deleteByIdsPhysical(ids);
    }

    private OaCallbackSubmitResponse buildResponse(ESBInfoParams reqEsbInfo,
                                                   String returnStatus,
                                                   String returnCode,
                                                   String returnMsg) {
        String now = LocalDateTime.now().format(DATETIME_FORMATTER);

        ESBRespInfoVo esbInfo = ESBRespInfoVo.builder()
                .instId(reqEsbInfo.getInstId())
                .requestTime(reqEsbInfo.getRequestTime())
                .attr1(reqEsbInfo.getAttr1())
                .attr2(reqEsbInfo.getAttr2())
                .attr3(reqEsbInfo.getAttr3())
                .returnStatus(returnStatus)
                .returnCode(returnCode)
                .returnMsg(returnMsg)
                .responseTime(now)
                .build();

        ResultInfo resultInfo = new ResultInfo();
        resultInfo.setStatus("S".equals(returnStatus) ? "SUCCESS" : "FAILED");
        resultInfo.setMessage(returnMsg);
        resultInfo.setProcessTime(now.substring(0, 19));

        OaCallbackSubmitResponse response = new OaCallbackSubmitResponse();
        response.setEsbInfo(esbInfo);
        response.setResultInfo(resultInfo);
        return response;
    }

    @Data
    public static class OaCallbackSubmitRequest {
        @NotNull
        @Valid
        private ESBInfoParams esbInfo;

        @NotNull
        @Valid
        private RequestInfo requestInfo;
    }

    @Data
    public static class RequestInfo {
        @NotBlank
        private String operationType;

        @NotNull
        @Valid
        private RequestData requestData;
    }

    @Data
    public static class RequestData {
        @NotBlank
        private String versionCd;

        @NotBlank
        private String scenCd;

        @NotEmpty
        @Valid
        private List<OrgNode> orgs;
    }

    @Data
    public static class OrgNode {
        @NotBlank
        private String orgCd;

        @NotBlank
        @JsonProperty("orgNM")
        @JsonAlias({"orgNm"})
        private String orgNM;

        @JsonAlias({"orgType"})
        private Integer type;
//
//        @NotNull
//        @Valid
//        private List<OrgChildNode> orgChildren;
    }

    @Data
    public static class OrgChildNode {
        @NotBlank
        private String orgCd;

        @NotBlank
        @JsonProperty("orgNM")
        @JsonAlias({"orgNm"})
        private String orgNM;
    }

    @Data
    public static class OaCallbackSubmitResponse {
        private ESBRespInfoVo esbInfo;
        private ResultInfo resultInfo;
    }

    @Data
    public static class ResultInfo {
        private String status;
        private String message;
        private String processTime;
    }
}
