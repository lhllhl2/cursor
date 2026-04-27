package com.jasolar.mis.module.system.domain.budget;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.jasolar.mis.framework.mybatis.core.dataobject.BaseDO;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;

/**
 * 预算流水实体
 */
@Data
@NoArgsConstructor
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
@TableName(value = "budget_ledger", autoResultMap = true)
public class BudgetLedger extends BaseDO {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private String bizType;

    private String bizCode;

    private String bizItemCode;

    private String effectType;

    /**
     * 预算年度
     */
    private String year;

    /**
     * 预算月份
     */
    private String month;

    /**
     * 实际年度
     */
    private String actualYear;

    /**
     * 实际月份
     */
    private String actualMonth;

    /**
     * 管理组织编码
     */
    private String morgCode;
    
    /**
     * 控制层级EHR组织代码
     */
    private String controlEhrCd;
    
    /**
     * 控制层级EHR组织名称
     */
    private String controlEhrNm;
    
    /**
     * 预算组织编码
     */
    private String budgetOrgCd;
    
    /**
     * 预算组织名称
     */
    private String budgetOrgNm;

    /**
     * 预算科目编码
     */
    private String budgetSubjectCode;

    /**
     * 主数据项目编码
     */
    private String masterProjectCode;

    /**
     * ERP资产类型编码
     */
    private String erpAssetType;

    /**
     * 是否内部项目
     */
    private String isInternal;

    /**
     * 扣减在资金池时：记录资金池维度键（重跑数据不变）。
     * 格式：year@isInternal@morgCode@budgetSubjectCode@masterProjectCode@erpAssetType；
     * 重跑后可根据本字段+季度查 BUDGET_POOL_DEM_R/BUDGET_BALANCE 得当前 pool_id。
     * 付款单（CLAIM）若同时扣关联流水与资金池（如框架协议合同不足部分走池子），可与 deductionFromLedgerBizKey 同时有值（以 amountConsumedQ* 是否发生池侧扣减为准）。
     * 纯关联流水扣减、未动资金池时本字段为空。若因 bug 未扣减或历史数据未记录，两键均可为空。
     */
    private String poolDimensionKey;

    /**
     * 扣减在关联流水上时：记录被扣减流水的业务键，多条时用分号分隔（重跑数据不变）。
     * 单条格式：bizCode@bizItemCode；多条格式：key1;key2;key3（分隔符为分号）。
     * 纯资金池扣减时本字段为空。CLAIM 混合扣减时与 poolDimensionKey 可同时有值。若因 bug 未扣减或历史数据未记录，两键均可为空。
     */
    private String deductionFromLedgerBizKey;

    /** 扣减来源为多条关联流水时，各 bizKey 之间的分隔符 */
    public static final String DEDUCTION_FROM_LEDGER_KEYS_SEP = ";";

    private String currency;

    /**
     * 预算总金额
     */
    private BigDecimal amount;

    /**
     * 第一季度消耗金额
     */
    private BigDecimal amountConsumedQOne;

    /**
     * 第二季度消耗金额
     */
    private BigDecimal amountConsumedQTwo;

    /**
     * 第三季度消耗金额
     */
    private BigDecimal amountConsumedQThree;

    /**
     * 第四季度消耗金额
     */
    private BigDecimal amountConsumedQFour;

    /**
     * 可用金额
     */
    private BigDecimal amountAvailable;

    private String version;

    /**
     * 上一版本号
     */
    private String versionPre;

    /**
     * 元数据（扩展信息，JSON 字符串格式）
     */
    private String metadata;

    /**
     * 操作人（申请时传入的operator字段）
     */
    private String operator;

    /**
     * 操作人工号（申请时传入的operator字段，用于存储工号）
     */
    private String operatorNo;

    /**
     * 根据当前维度字段组装资金池维度键（与 BUDGET_POOL_DEM_R 查询维度一致，不含 quarter）。
     * 仅当扣减在资金池时使用；扣减在关联流水时用 {@link #buildDeductionFromLedgerBizKey(String, String)}。
     *
     * @return 格式：year@isInternal@morgCode@budgetSubjectCode@masterProjectCode@erpAssetType，空用 NAN 占位
     */
    public String buildPoolDimensionKey() {
        String y = (year == null || year.isEmpty()) ? "NAN" : year;
        String ii = (isInternal == null || isInternal.isEmpty()) ? "NAN" : isInternal;
        String m = (morgCode == null || morgCode.isEmpty()) ? "NAN" : morgCode;
        String s = (budgetSubjectCode == null || budgetSubjectCode.isEmpty()) ? "NAN-NAN" : budgetSubjectCode;
        String p = (masterProjectCode == null || masterProjectCode.isEmpty()) ? "NAN" : masterProjectCode;
        String e = (erpAssetType == null || erpAssetType.isEmpty()) ? "NAN" : erpAssetType;
        return y + "@" + ii + "@" + m + "@" + s + "@" + p + "@" + e;
    }

    /**
     * 根据 BUDGET_BALANCE 的维度字段组装资金池维度键（与 BUDGET_POOL_DEM_R/BUDGET_BALANCE 查询维度一致，不含 quarter）。
     * 扣减在资金池时，应使用实际扣减的 Balance 的维度拼接，以便重跑后可根据本字段+季度准确查到同一资金池。
     * 科目部分与资金池一致：CUSTOM_CODE + "-" + ACCOUNT_SUBJECT_CODE（仅用 accountSubjectCode 无法唯一对应资金池）。
     *
     * @param balance 实际发生扣减的预算余额记录，不可为 null
     * @return 格式：year@isInternal@morgCode@(customCode-accountSubjectCode)@projectCode@erpAssetType，空用 NAN 占位
     */
    public static String buildPoolDimensionKeyFromBalance(BudgetBalance balance) {
        if (balance == null) {
            return null;
        }
        String y = (balance.getYear() == null || balance.getYear().isEmpty()) ? "NAN" : balance.getYear();
        String ii = (balance.getIsInternal() == null || balance.getIsInternal().isEmpty()) ? "NAN" : balance.getIsInternal();
        String m = (balance.getMorgCode() == null || balance.getMorgCode().isEmpty()) ? "NAN" : balance.getMorgCode();
        String customCode = balance.getCustomCode();
        String accountSubjectCode = balance.getAccountSubjectCode();
        boolean customBlank = customCode == null || customCode.isEmpty() || "NAN".equals(customCode);
        boolean acctBlank = accountSubjectCode == null || accountSubjectCode.isEmpty() || "NAN".equals(accountSubjectCode);
        String s = (customBlank && acctBlank) ? "NAN-NAN" : (customBlank ? "NAN" : customCode) + "-" + (acctBlank ? "NAN" : accountSubjectCode);
        String p = (balance.getProjectCode() == null || balance.getProjectCode().isEmpty()) ? "NAN" : balance.getProjectCode();
        String e = (balance.getErpAssetType() == null || balance.getErpAssetType().isEmpty()) ? "NAN" : balance.getErpAssetType();
        return y + "@" + ii + "@" + m + "@" + s + "@" + p + "@" + e;
    }

    /**
     * 扣减在关联流水上时，组装单条被扣减流水的业务键（重跑数据不变）。
     *
     * @param relatedBizCode    被扣减流水的 BIZ_CODE（业务单号）
     * @param relatedBizItemCode 被扣减流水的 BIZ_ITEM_CODE（行号）
     * @return 格式 bizCode@bizItemCode，用于填入 deductionFromLedgerBizKey
     */
    public static String buildDeductionFromLedgerBizKey(String relatedBizCode, String relatedBizItemCode) {
        String code = (relatedBizCode == null || relatedBizCode.isEmpty()) ? "" : relatedBizCode;
        String item = (relatedBizItemCode == null || relatedBizItemCode.isEmpty()) ? "" : relatedBizItemCode;
        return code + "@" + item;
    }

    /**
     * 扣减在多条关联流水上时，组装多条被扣减流水的业务键（用分号拼接，重跑数据不变）。
     *
     * @param relatedLedgers 被扣减的流水列表（取 bizCode、bizItemCode）
     * @return 格式 key1;key2;key3，空列表返回空字符串
     */
    public static String buildDeductionFromLedgerBizKeyMultiple(java.util.List<BudgetLedger> relatedLedgers) {
        if (relatedLedgers == null || relatedLedgers.isEmpty()) {
            return "";
        }
        return relatedLedgers.stream()
                .map(l -> buildDeductionFromLedgerBizKey(l.getBizCode(), l.getBizItemCode()))
                .filter(s -> !s.equals("@"))
                .distinct()
                .collect(java.util.stream.Collectors.joining(DEDUCTION_FROM_LEDGER_KEYS_SEP));
    }

    /**
     * 解析 deductionFromLedgerBizKey 为多条时返回各 bizKey 列表（单条时返回一个元素的列表）。
     */
    public static java.util.List<String> parseDeductionFromLedgerBizKeys(String deductionFromLedgerBizKey) {
        if (deductionFromLedgerBizKey == null || deductionFromLedgerBizKey.isEmpty()) {
            return java.util.Collections.emptyList();
        }
        return java.util.Arrays.stream(deductionFromLedgerBizKey.split(DEDUCTION_FROM_LEDGER_KEYS_SEP))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(java.util.stream.Collectors.toList());
    }

    /**
     * 是否已记录扣减来源（资金池或关联流水）。
     * 若为 false 表示两条键都为空，可能为历史数据、或因 bug 未正确写入扣减来源，可用于排查异常流水。
     */
    public boolean hasRecordedDeductionSource() {
        return (poolDimensionKey != null && !poolDimensionKey.isEmpty())
                || (deductionFromLedgerBizKey != null && !deductionFromLedgerBizKey.isEmpty());
    }
}

