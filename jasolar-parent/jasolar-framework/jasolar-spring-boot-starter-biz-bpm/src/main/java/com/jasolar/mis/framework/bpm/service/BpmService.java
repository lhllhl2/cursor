package com.jasolar.mis.framework.bpm.service;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import org.apache.commons.lang3.StringUtils;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.jasolar.mis.framework.bpm.ErrorCodeConstants;
import com.jasolar.mis.framework.bpm.util.BpmParamUtils;
import com.jasolar.mis.framework.bpm.util.BpmProcessConverter;
import com.jasolar.mis.framework.bpm.util.BpmUtils;
import com.jasolar.mis.framework.common.exception.DataServiceException;
import com.jasolar.mis.framework.common.exception.ServiceException;
import com.jasolar.mis.framework.common.pojo.CommonResult;
import com.jasolar.mis.framework.common.util.json.JsonUtils;
import com.jasolar.mis.framework.common.util.spring.SpelUtils;
import com.jasolar.mis.framework.data.core.User;
import com.jasolar.mis.framework.data.util.UserUtils;
import com.jasolar.mis.framework.mybatis.core.dataobject.IBpmBizDO;
import com.jasolar.mis.framework.mybatis.core.enums.BpmStatusEnum;
import com.jasolar.mis.framework.mybatis.core.mapper.BaseMapperX;
import com.jasolar.mis.framework.web.core.util.WebFrameworkUtils;
import com.jasolar.mis.module.bpm.api.BpmProcessInstanceApi;
import com.jasolar.mis.module.bpm.api.dto.BizOperationDTO;
import com.jasolar.mis.module.bpm.api.dto.BpmActivityNodeDTO;
import com.jasolar.mis.module.bpm.api.dto.BpmParamDTO;
import com.jasolar.mis.module.bpm.api.dto.BpmProcessDefinitionDTO;
import com.jasolar.mis.module.bpm.api.dto.BpmProcessInstanceCreateReqDTO;
import com.jasolar.mis.module.bpm.enums.BpmBizOperationActionEnum;
import com.jasolar.mis.module.bpm.enums.ModuleEnum;

import jakarta.annotation.Nullable;
import jakarta.annotation.Resource;

/**
 * 
 * @author galuo
 * @date 2025-04-02 18:58
 *
 * @param <T> 数据库实体DO
 * @param <BizMapper> 数据库实体Mapper
 */
public interface BpmService<T extends IBpmBizDO, BizMapper extends BaseMapperX<T>> {

    /**
     * 注入FeignClient
     * 
     * @param bpmApi bpm服务的FeignClient
     */
    @Resource
    void setBpmApi(BpmProcessInstanceApi bpmApi);

    /**
     * @return bpm服务的FeignClient
     */
    BpmProcessInstanceApi getBpmApi();

    /**
     * 注入数据库实体的Mapper
     * 
     * @param mapper 数据库实体Mapper
     */
    @Resource
    void setBizMapper(BizMapper bizMapper);

    /**
     * @return 数据库实体Mapper
     */
    BizMapper getBizMapper();

    /** 所属模块 */
    ModuleEnum getModule();

    /**
     * 
     * 提交审批流, 不需要额外的流程变量, 流程变量全部通过entity获取
     * 
     * @param entity 业务数据
     * @return 流程实例ID
     */
    @Transactional(rollbackFor = Exception.class)
    @Deprecated
    default String submit(T entity) {
        return this.submit(entity, dto -> initParamValues(dto, entity), null);
    }

    /**
     * 提交审批流, 通过流程的参数配置读取流程变量
     *
     * @param entity 业务实体
     * @param root 读取流程变量的根节点对象,一般是业务实体对应的DTO类, 其中应该初始化了所有参数需要的字段, 包括明细行等
     * @return 流程实例ID
     */
    @Transactional(rollbackFor = Exception.class)
    default String submit(T entity, Object root) {
        return this.submit(entity, dto -> initParamValues(dto, root), null);
    }

    /**
     * 初始化流程变量, 通过流程的参数配置获取对应的变量.
     * 注意流程参数中不要与系统中的默认变量重名,否则可能引起系统问题
     * 参见: com.jasolar.mis.module.bpm.framework.flowable.core.enums.BpmnVariableConstants
     *
     * @param dto 流程创建请求参数DTO对象
     * @param root 获取流程变量的根节点对象,一般为业务实体的DTO对象, 其中应该初始化了所有参数需要的字段, 包括明细行等
     */
    default void initParamValues(BpmProcessInstanceCreateReqDTO dto, Object root) {
        String processCategory = BpmUtils.getProcessCategory(dto.getBizType());
        List<BpmParamDTO> params = getBpmApi().findAvailableParams(processCategory).getCheckedData();
        if (CollectionUtils.isEmpty(params)) {
            // 未配置任何变量
            return;
        }

        params.parallelStream().filter(param -> StringUtils.isNotBlank(param.getParamExpression())).forEach(param -> {
            Object val = SpelUtils.getValue(param.getParamExpression(), root);
            dto.setVariable(param.getParamKey(), val);
        });

        // 流程实例的标题
        dto.setName(dto.removeVariable(BpmParamUtils.TITLE));
    }

    /**
     * 启动新的审批人流程
     * 
     * @param entity 数据实体
     * @param dto 流程启动时的参数
     * @return
     */
    default String start(String bizType, BpmProcessInstanceCreateReqDTO dto) {
        // LoginUser loginUser = LoginServletUtils.getLoginUser();
        // long userId = loginUser.getId();
        // if (UserTypeEnum.SUPPLIER == loginUser.userType()) {
        // // 主要用于供应商注册流程, 邀请人作为发起人
        // User usr = UserUtils.get(dto.getUserNo());
        // userId = usr.getId();
        // }
        User usr = UserUtils.get(dto.getUserNo());
        String processCategory = BpmUtils.getProcessCategory(bizType);
        List<BpmProcessDefinitionDTO> defs = getBpmApi().findProcessDefinitions(processCategory, usr.getId(), dto).getCheckedData();
        if (CollectionUtils.isEmpty(defs)) {
            throw new ServiceException(ErrorCodeConstants.BPM_PROCESS_DEFINITION_NOT_FOUND);
        }

        if (defs.size() > 1) {
            // 有多个符合条件的流程, 先判断是否有header中提交流程定义
            String header = WebFrameworkUtils.getRequest().getHeader(BpmUtils.HEADER_PROCESS_CATEGORY);
            if (StringUtils.isBlank(header) || defs.stream().noneMatch(def -> def.getProcessDefinitionKey().equals(header))) {
                // 没有则返回错误给前端有前端选择一个流程定义
                throw new DataServiceException(defs, ErrorCodeConstants.BPM_PROCESS_DEFINITION_MULTIPLE);
            }
            dto.setProcessDefinitionKey(header);
        } else {
            dto.setProcessDefinitionKey(defs.get(0).getProcessDefinitionKey());
        }

        // 手工选择的审批人
        String assigneesHeader = WebFrameworkUtils.getRequest().getHeader(BpmUtils.HEADER_MANUAL_ASSIGNEES);
        if (StringUtils.isNotBlank(assigneesHeader)) {
            String json = URLDecoder.decode(assigneesHeader, StandardCharsets.UTF_8);
            Map<String, List<Long>> assignees = JsonUtils.parseObject(json, new TypeReference<Map<String, List<Long>>>() {
            });
            if (!assignees.isEmpty()) {
                dto.setManualAssigness(assignees);
            }
        } else {
            // 解析流程图,判断哪些节点需要选择审批人, 并返回给前端
            List<BpmActivityNodeDTO> nodes = getBpmApi().findManualNodes(usr.getId(), dto).getCheckedData();
            if (!CollectionUtils.isEmpty(nodes)) {
                throw new DataServiceException(nodes, ErrorCodeConstants.BPM_PROCESS_HAS_MANUAL_NODES);
            }
        }

        CommonResult<String> result = getBpmApi().createProcessInstance(usr.getId(), dto);
        return result.getCheckedData();
    }

    /**
     * 提交审批流
     * 
     * @param entity 业务数据. 注意如果实体中有title字段会默认作为流程的名称
     * @param dataInitializing 用于初始化创建流程实例的请求参数. 通用字段已经从entity中获取设置
     * @param callback 回调函数, 参数为entity和发起流程的DTO, 不需要保存entity, 回调后会调用保存entity
     * @return 流程实例ID
     */
    default String submit(T entity, @Nullable Consumer<BpmProcessInstanceCreateReqDTO> dataInitializing,
            @Nullable BiConsumer<T, BpmProcessInstanceCreateReqDTO> callback) {
        return BpmUtils.execute(entity.getBizType(), entity.getNo(), () -> {
            BpmProcessInstanceCreateReqDTO dto = BpmProcessConverter.INSTANCE.toRequestDTO(entity);
            dto.setModule(getModule());
            if (dataInitializing != null) {
                // 数据初始化, 一般用于初始化BPM的流程变量
                dataInitializing.accept(dto);
            }

            String procInstId = entity.getProcInstId();
            if (BpmStatusEnum.RETURNED.name().equals(entity.getStatus())) {
                // 退回状态, 重新提交给退回的审批人
                getBpmApi().submitReturned(procInstId, dto).checkError();
            } else {
                // 发起新的审批流
                procInstId = this.start(entity.getBizType(), dto);
                entity.setProcDefKey(dto.getProcessDefinitionKey());
                entity.setProcInstId(procInstId);
                entity.setSubmitTime(LocalDateTime.now());
            }

            // 修改为审批中
            entity.setStatus(BpmStatusEnum.APPROVING.name());
            if (callback != null) {
                // 回调中可以修改实体状态等
                callback.accept(entity, dto);
            }

            this.getBizMapper().updateById(entity);

            return entity.getProcInstId();
        });
    }

    /**
     * 根据流程实例ID查询审批历史记录
     * 
     * @param procInstId 流程实例ID
     * @param actions 要筛选的签核动作,可以为null
     * @return
     */
    default List<BizOperationDTO> findOperations(String procInstId, @Nullable BpmBizOperationActionEnum... actions) {
        List<String> list = actions == null ? Collections.emptyList() : Arrays.stream(actions).map(a -> a.name()).toList();
        return this.getBpmApi().findOperations(procInstId, list).getCheckedData();
    }
}
