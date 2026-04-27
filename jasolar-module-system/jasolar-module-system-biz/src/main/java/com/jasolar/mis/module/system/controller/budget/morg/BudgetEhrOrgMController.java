package com.jasolar.mis.module.system.controller.budget.morg;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.jasolar.mis.framework.common.pojo.CommonResult;
import com.jasolar.mis.module.system.domain.ehr.EhrOrgManageR;
import com.jasolar.mis.module.system.domain.ehr.SubjectInfo;
import com.jasolar.mis.module.system.domain.ehr.ProjectControlR;
import com.jasolar.mis.module.system.mapper.ehr.EhrOrgManageRMapper;
import com.jasolar.mis.module.system.mapper.ehr.ProjectControlRMapper;
import com.jasolar.mis.module.system.mapper.ehr.SubjectInfoMapper;
import com.jasolar.mis.module.system.service.budget.morg.BudgetEhrOrgMService;
import com.jasolar.mis.module.system.service.ehr.ProjectControlExtRService;
import com.jasolar.mis.module.system.service.ehr.SubjectExtInfoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

/**
 * EHR组织管理扩展关系控制器
 */
@Tag(name = "预算管理 - EHR组织管理扩展关系")
@RestController
@RequestMapping("/sync/ehr-org-m")
@Slf4j
public class BudgetEhrOrgMController {

    @Resource
    private BudgetEhrOrgMService budgetEhrOrgMService;

    @Resource
    private SubjectExtInfoService subjectExtInfoService;

    @Resource
    private ProjectControlExtRService projectControlExtRService;

    @Resource
    private SubjectInfoMapper subjectInfoMapper;

    @Resource
    private EhrOrgManageRMapper ehrOrgManageRMapper;

    @Resource
    private ProjectControlRMapper projectControlRMapper;

    /**
     * 同步EHR管理组织关系数据
     * 从 EHR_ORG_MANAGE_R 表中查询所有 controlLevel=1 且 Deleted=0 且 year=2026 的数据，提取 ehrCd 进行同步
     *
     * @return 处理结果
     */
    @PostMapping("/syncEhrManageRData")
    @Operation(summary = "同步EHR管理组织关系数据")
    public CommonResult<String> syncEhrManageRData() {
        log.info("开始同步EHR管理组织关系数据，查询所有 controlLevel=1 且 Deleted=0 且 year=2026 的数据");
        
        // 查询所有 controlLevel=1 且 Deleted=0 且 year=2026 的数据
        List<EhrOrgManageR> ehrOrgManageRList = ehrOrgManageRMapper.selectList(
                new LambdaQueryWrapper<EhrOrgManageR>()
                        .eq(EhrOrgManageR::getControlLevel, "1")
                        .eq(EhrOrgManageR::getDeleted, false)
                        .eq(EhrOrgManageR::getYear, "2026")
        );
        
        // 提取 ehrCd，过滤掉空值
        List<String> ehrCodes = ehrOrgManageRList.stream()
                .map(EhrOrgManageR::getEhrCd)
                .filter(StringUtils::hasText)
                .collect(Collectors.toList());
        
        log.info("查询到 {} 条 controlLevel=1 且 year=2026 的数据，其中 {} 条有 ehrCd", 
                ehrOrgManageRList.size(), ehrCodes.size());
        
        if (ehrCodes.isEmpty()) {
            return CommonResult.success("未找到符合条件的EHR组织数据（controlLevel=1 且 year=2026 且 ehrCd 不为空）");
        }
        
        String result = budgetEhrOrgMService.syncEhrManageRData(ehrCodes);
        return CommonResult.success(result);
    }

    /**
     * 同步EHR管理组织一对一关系数据
     * 从 EHR_ORG_MANAGE_R 表中查询所有 controlLevel=1 且 Deleted=0 的数据，提取 ehrCd 进行同步
     *
     * @return 处理结果
     */
    @PostMapping("/synEhrManageOneRData")
    @Operation(summary = "同步EHR管理组织一对一关系数据")
    public CommonResult<String> synEhrManageOneRData() {
        String result = budgetEhrOrgMService.synEhrManageOneRData();
        return CommonResult.success(result);
    }

    /**
     * 同步科目信息数据
     * 从 SUBJECT_INFO 表中查询所有 Deleted=0 且 year=2026 的数据，由 Service 层处理向上追溯逻辑
     * 对于 controlLevel=1 的科目直接使用，对于 controlLevel≠1 的科目向上追溯找到 controlLevel=1 的父级
     * 如果向上追溯后仍然找不到 controlLevel=1 的父级，则映射到 NAN-NAN
     *
     * @return 处理结果
     */
    @PostMapping("/syncSubjectInfoData")
    @Operation(summary = "同步科目信息数据")
    public CommonResult<String> syncSubjectInfoData() {
        log.info("开始同步科目信息数据，查询所有 Deleted=0 且 year=2026 的数据（不限制 controlLevel，由 Service 层处理向上追溯逻辑）");
        
        // 查询所有 Deleted=0 且 year=2026 的数据（不限制 controlLevel，让 Service 层处理向上追溯逻辑）
        List<SubjectInfo> subjectInfoList = subjectInfoMapper.selectList(
                new LambdaQueryWrapper<SubjectInfo>()
                        .eq(SubjectInfo::getDeleted, false)
                        .eq(SubjectInfo::getYear, "2026")
        );
        
        // 过滤掉 acctCd 为空的记录
        List<SubjectInfo> validSubjectInfoList = subjectInfoList.stream()
                .filter(subject -> StringUtils.hasText(subject.getAcctCd()))
                .collect(Collectors.toList());
        
        log.info("查询到 {} 条数据（year=2026，不限制 controlLevel），其中 {} 条有 acctCd", 
                subjectInfoList.size(), validSubjectInfoList.size());
        
        // 统计 controlLevel 分布
        long controlLevelOneCount = validSubjectInfoList.stream()
                .filter(subject -> "1".equals(subject.getControlLevel()))
                .count();
        log.info("其中 controlLevel=1 的数据有 {} 条，controlLevel≠1 的数据有 {} 条", 
                controlLevelOneCount, validSubjectInfoList.size() - controlLevelOneCount);
        
        // 打印前10条数据用于调试
        log.info("前10条数据详情：");
        validSubjectInfoList.stream().limit(10).forEach(subject -> {
            log.info("  CUST1_CD={}, ACCT_CD={}, ERP_ACCT_CD={}, CONTROL_LEVEL={}, LEAF={}", 
                    subject.getCust1Cd(), subject.getAcctCd(), subject.getErpAcctCd(), 
                    subject.getControlLevel(), subject.getLeaf());
        });
        
        // 特别检查目标数据是否存在
        log.info("检查目标数据：ACCT_CD=CU10903-A01030301010101020101 或父级 ACCT_CD=CU10903-A010303010101010201");
        List<SubjectInfo> targetSubjects = subjectInfoList.stream()
                .filter(subject -> {
                    String acctCd = subject.getAcctCd();
                    return acctCd != null && (
                            acctCd.equals("CU10903-A01030301010101020101") || 
                            acctCd.equals("CU10903-A010303010101010201")
                    );
                })
                .collect(Collectors.toList());
        if (!targetSubjects.isEmpty()) {
            log.info("找到目标数据 {} 条：", targetSubjects.size());
            targetSubjects.forEach(subject -> {
                log.info("  目标数据: CUST1_CD={}, ACCT_CD={}, ERP_ACCT_CD={}, CONTROL_LEVEL={}, LEAF={}, ACCT_PAR_CD={}", 
                        subject.getCust1Cd(), subject.getAcctCd(), subject.getErpAcctCd(), 
                        subject.getControlLevel(), subject.getLeaf(), subject.getAcctParCd());
            });
        } else {
            log.warn("未找到目标数据（ACCT_CD=CU10903-A01030301010101020101 或 CU10903-A010303010101010201）");
        }
        
        // 特别检查ERP_ACCT_CD=660204是否存在
        log.info("检查目标数据：ERP_ACCT_CD=660204");
        List<SubjectInfo> target660204List = subjectInfoList.stream()
                .filter(subject -> "660204".equals(subject.getErpAcctCd()))
                .collect(Collectors.toList());
        if (!target660204List.isEmpty()) {
            log.info("找到ERP_ACCT_CD=660204的数据 {} 条：", target660204List.size());
            target660204List.forEach(subject -> {
                log.info("  ERP_ACCT_CD=660204: CUST1_CD={}, ACCT_CD={}, ERP_ACCT_CD={}, CONTROL_LEVEL={}, LEAF={}, ACCT_PAR_CD={}, YEAR={}, DELETED={}", 
                        subject.getCust1Cd(), subject.getAcctCd(), subject.getErpAcctCd(), 
                        subject.getControlLevel(), subject.getLeaf(), subject.getAcctParCd(), 
                        subject.getYear(), subject.getDeleted());
            });
        } else {
            log.warn("未找到ERP_ACCT_CD=660204的数据！");
        }
        
        if (validSubjectInfoList.isEmpty()) {
            return CommonResult.success("未找到符合条件的科目数据（acctCd 不为空）");
        }
        
        String result = subjectExtInfoService.syncSubjectInfoData(validSubjectInfoList);
        return CommonResult.success(result);
    }

    /**
     * 同步项目控制关系数据
     * 从 PROJECT_CONTROL_R 表中查询所有 controlLevel=1 且 Deleted=0 且 year=2026 的数据，提取 prjCd 进行同步
     *
     * @return 处理结果
     */
    @PostMapping("/syncProjectControlRData")
    @Operation(summary = "同步项目控制关系数据")
    public CommonResult<String> syncProjectControlRData() {
        log.info("开始同步项目控制关系数据，查询所有 controlLevel=1 且 Deleted=0 且 year=2026 的数据");
        
        // 查询所有 controlLevel=1 且 Deleted=0 且 year=2026 的数据
        List<ProjectControlR> projectControlRList = projectControlRMapper.selectList(
                new LambdaQueryWrapper<ProjectControlR>()
                        .eq(ProjectControlR::getControlLevel, "1")
                        .eq(ProjectControlR::getDeleted, false)
                        .eq(ProjectControlR::getYear, "2026")
        );
        
        // 提取 prjCd，过滤掉空值
        List<String> prjCds = projectControlRList.stream()
                .map(ProjectControlR::getPrjCd)
                .filter(StringUtils::hasText)
                .collect(Collectors.toList());
        
        log.info("查询到 {} 条 controlLevel=1 且 year=2026 的数据，其中 {} 条有 prjCd", 
                projectControlRList.size(), prjCds.size());
        
        if (prjCds.isEmpty()) {
            return CommonResult.success("未找到符合条件的项目数据（controlLevel=1 且 year=2026 且 prjCd 不为空）");
        }
        
        String result = projectControlExtRService.syncProjectControlRData(prjCds);
        return CommonResult.success(result);
    }
}

