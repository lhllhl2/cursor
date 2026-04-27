package com.jasolar.mis.module.bpm.api;

import java.util.List;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import com.jasolar.mis.bizapi.Apis;
import com.jasolar.mis.framework.common.pojo.CommonResult;
import com.jasolar.mis.framework.common.validation.group.Query;
import com.jasolar.mis.framework.common.validation.group.Submit;
import com.jasolar.mis.module.bpm.api.dto.BizOperationDTO;
import com.jasolar.mis.module.bpm.api.dto.BpmActivityNodeDTO;
import com.jasolar.mis.module.bpm.api.dto.BpmParamDTO;
import com.jasolar.mis.module.bpm.api.dto.BpmProcessDefinitionDTO;
import com.jasolar.mis.module.bpm.api.dto.BpmProcessInstanceCreateReqDTO;
import com.jasolar.mis.module.bpm.enums.BpmBizOperationActionEnum;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

@FeignClient(name = Apis.BPM)
@Tag(name = "RPC 服务 - 流程实例")
public interface BpmProcessInstanceApi {

    String PREFIX = Apis.BPM_PREFIX + "/process-instance";

    @PostMapping(PREFIX + "/create")
    @Operation(summary = "创建流程实例（提供给内部），返回实例编号")
    @Parameter(name = "userId", description = "用户编号", required = true, example = "1")
    CommonResult<String> createProcessInstance(@RequestParam("userId") Long userId,
            @Validated(Submit.class) @RequestBody BpmProcessInstanceCreateReqDTO reqDTO);

    /**
     * 查询可以使用的流程参数
     * 
     * @param processCategory 流程分类
     * @return
     */
    @GetMapping(PREFIX + "/{processCategory}/params")
    @Operation(summary = "查询流程分类下可使用的流程参数")
    CommonResult<List<BpmParamDTO>> findAvailableParams(@PathVariable("processCategory") String processCategory);

    /**
     * 查询可以使用的流程定义
     * 
     * @param processCategory 流程分类
     * @param userId 申请人
     * @param reqDTO 创建流程实例时使用的参数
     * @return
     */
    @PostMapping(PREFIX + "/{processCategory}/definitions")
    @Operation(summary = "根据创建流程的参数查询可以使用的流程定义")
    CommonResult<List<BpmProcessDefinitionDTO>> findProcessDefinitions(@PathVariable("processCategory") String processCategory,
            @Parameter(name = "userId", description = "用户ID", required = true, example = "1") @RequestParam("userId") Long userId,
            @Validated(Query.class) @RequestBody BpmProcessInstanceCreateReqDTO reqDTO);

    /**
     * 查询需要申请人指定审批人的流程节点
     * 
     * @param userId 申请人
     * @param reqDTO 创建流程实例时使用的参数
     * @return
     */
    @PostMapping(PREFIX + "/manual-nodes")
    @Operation(summary = "根据创建流程的参数查询需要申请人指定审批人的节点数据")
    CommonResult<List<BpmActivityNodeDTO>> findManualNodes(
            @Parameter(name = "userId", description = "用户ID", required = true, example = "1") @RequestParam("userId") Long userId,
            @Validated(Submit.class) @RequestBody BpmProcessInstanceCreateReqDTO reqDTO);

    /**
     * 提交被审批人退回到申请人的流程
     * 
     * @param id 流程ID
     * @param reqDTO 流程提交对象, 仅会更新流程标题和流程变量
     * @return
     */
    @PutMapping(PREFIX + "/submit-returned/{id}")
    @Operation(summary = "流程激活,当审批人退回到申请人,申请人重新提交后重新激活流程")
    CommonResult<Void> submitReturned(@PathVariable("id") String id, @RequestBody BpmProcessInstanceCreateReqDTO reqDTO);

    /**
     * 流程挂起,当审批人退回到申请人时,将流程挂起,待申请人重新提交后重新激活
     * 
     * @param id 流程ID
     * @return
     */
    @PutMapping(PREFIX + "/suspend")
    @Operation(summary = "流程挂起,当审批人退回到申请人时,将流程挂起,待申请人重新提交后重新激活")
    CommonResult<Void> suspend(@RequestParam("id") String id);

    /**
     * 查询审批记录
     * 
     * @param id 流程实例ID
     * @param actions 要筛选的核签动作,可以为空,则表示筛选所有历史. 参考{@link BpmBizOperationActionEnum}.
     *     如仅需要查询审批通过历史记录,则传入: <code>BpmBizOperationActionEnum.APPROVAL.name() </code>
     * @return
     */
    @GetMapping(PREFIX + "/{id}/operations")
    @Operation(summary = "获得指定业务ID的审批历史记录")
    CommonResult<List<BizOperationDTO>> findOperations(@PathVariable("id") String id,
            @RequestParam(required = false, name = "actions") List<String> actions);
}
