package com.jasolar.mis.module.system.controller.budget.vo;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Description: 预算流水返回对象
 * Author : Auto Generated
 * Date : 2025-01-XX
 * Version : 1.0
 */
@Data
@ToString
@ApiModel("预算流水返回对象")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BudgetLedgerVo {

    /**
     * 业务类型
     */
    @ApiModelProperty(value = "业务类型")
    private String bizType;

    /**
     * 业务类型描述
     */
    @ApiModelProperty(value = "业务类型描述")
    private String bizTypeDes;

    /**
     * 状态
     */
    @ApiModelProperty(value = "状态")
    private String status;

    /**
     * 状态描述
     */
    @ApiModelProperty(value = "状态描述")
    private String statusDes;

    /**
     * 业务单号
     */
    @ApiModelProperty(value = "业务单号")
    private String bizCode;

    /**
     * 合同号列表
     */
    @ApiModelProperty(value = "合同号列表")
    private List<String> contractNos;

    /**
     * 需求单号列表
     */
    @ApiModelProperty(value = "需求单号列表")
    private List<String> demandOrderNos;

    /**
     * 影响类型
     */
    @ApiModelProperty(value = "影响类型")
    private String effectType;

    /**
     * 影响类型描述
     */
    @ApiModelProperty(value = "影响类型描述")
    private String effectTypeDes;

    /**
     * 预算年度
     */
    @ApiModelProperty(value = "预算年度")
    private String year;

    /**
     * 预算月份
     */
    @ApiModelProperty(value = "预算月份")
    private String month;

    /**
     * 实际年度
     */
    @ApiModelProperty(value = "实际年度")
    private String actualYear;

    /**
     * 实际月份
     */
    @ApiModelProperty(value = "实际月份")
    private String actualMonth;

    /**
     * 管理组织编码
     */
    @ApiModelProperty(value = "管理组织编码")
    private String morgCode;
    
    /**
     * 管理组织名称
     */
    @ApiModelProperty(value = "管理组织名称")
    private String morgName;

    /**
     * 控制层级EHR组织代码
     */
    @ApiModelProperty(value = "控制层级EHR组织代码")
    private String controlEhrCd;

    /**
     * 控制层级EHR组织名称
     */
    @ApiModelProperty(value = "控制层级EHR组织名称")
    private String controlEhrNm;

    /**
     * 预算组织编码
     */
    @ApiModelProperty(value = "预算组织编码")
    private String budgetOrgCd;

    /**
     * 预算组织名称
     */
    @ApiModelProperty(value = "预算组织名称")
    private String budgetOrgNm;

    /**
     * 预算科目编码
     */
    @ApiModelProperty(value = "预算科目编码")
    private String budgetSubjectCode;

    /**
     * 获取预算科目编码，如果是"NAN-NAN"则返回空字符串
     */
    public String getBudgetSubjectCode() {
        if ("NAN-NAN".equals(budgetSubjectCode)) {
            return "";
        }
        return budgetSubjectCode;
    }

    /**
     * 预算科目名称
     */
    @ApiModelProperty(value = "预算科目名称")
    private String budgetSubjectName;

    /**
     * 主数据项目编码
     */
    @ApiModelProperty(value = "主数据项目编码")
    private String masterProjectCode;

    /**
     * 获取主数据项目编码，如果是"NAN"则返回空字符串
     */
    public String getMasterProjectCode() {
        if ("NAN".equals(masterProjectCode)) {
            return "";
        }
        return masterProjectCode;
    }

    /**
     * 主数据项目名称
     */
    @ApiModelProperty(value = "主数据项目名称")
    private String masterProjectName;

    /**
     * ERP资产类型编码
     */
    @ApiModelProperty(value = "ERP资产类型编码")
    private String erpAssetType;

    /**
     * 获取ERP资产类型编码，如果是"NAN"则返回空字符串
     */
    public String getErpAssetType() {
        if ("NAN".equals(erpAssetType)) {
            return "";
        }
        return erpAssetType;
    }

    /**
     * ERP资产类型名称
     */
    @ApiModelProperty(value = "ERP资产类型名称")
    private String erpAssetTypeName;

    /**
     * 是否内部项目
     */
    @ApiModelProperty(value = "是否内部项目")
    private String isInternal;

    /**
     * 是否内部项目描述
     */
    @ApiModelProperty(value = "是否内部项目描述")
    private String isInternalDes;

    /**
     * 币种
     */
    @ApiModelProperty(value = "币种")
    private String currency;

    /**
     * 预算总金额
     */
    @ApiModelProperty(value = "预算总金额")
    private BigDecimal amount;

    /**
     * 可用金额
     */
    @ApiModelProperty(value = "可用金额")
    private BigDecimal amountAvailable;

    /**
     * 第一季度金额（当bizType为ADJUST时，effectType为0或2时使用）
     */
    @ApiModelProperty(value = "第一季度金额")
    private BigDecimal amountConsumedQOne;

    /**
     * 第二季度金额（当bizType为ADJUST时，effectType为0或2时使用）
     */
    @ApiModelProperty(value = "第二季度金额")
    private BigDecimal amountConsumedQTwo;

    /**
     * 第三季度金额（当bizType为ADJUST时，effectType为0或2时使用）
     */
    @ApiModelProperty(value = "第三季度金额")
    private BigDecimal amountConsumedQThree;

    /**
     * 第四季度金额（当bizType为ADJUST时，effectType为0或2时使用）
     */
    @ApiModelProperty(value = "第四季度金额")
    private BigDecimal amountConsumedQFour;

    /**
     * 版本号
     */
    @ApiModelProperty(value = "版本号")
    private String version;

    /**
     * 上一版本号
     */
    @ApiModelProperty(value = "上一版本号")
    private String versionPre;

    /**
     * 创建时间
     */
    @ApiModelProperty(value = "创建时间")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @ApiModelProperty(value = "更新时间")
    private LocalDateTime updateTime;

    /**
     * 创建者
     */
    @ApiModelProperty(value = "创建者")
    private String creator;

    /**
     * 更新者
     */
    @ApiModelProperty(value = "更新者")
    private String updater;

    /**
     * 操作人（来自 BUDGET_LEDGER_HEAD 表）
     */
    @ApiModelProperty(value = "操作人")
    private String operator;

    /**
     * 操作人工号（来自 BUDGET_LEDGER_HEAD 表）
     */
    @ApiModelProperty(value = "操作人工号")
    private String operatorNo;

    /**
     * 数据来源（来自 BUDGET_LEDGER_HEAD 表）
     */
    @ApiModelProperty(value = "数据来源")
    private String dataSource;

    /**
     * 流程名称（来自 BUDGET_LEDGER_HEAD 表）
     */
    @ApiModelProperty(value = "流程名称")
    private String processName;

    /**
     * 关联明细列表
     */
    @ApiModelProperty(value = "关联明细列表")
    private List<BudgetLedgerVo> relatedDetails;
}

