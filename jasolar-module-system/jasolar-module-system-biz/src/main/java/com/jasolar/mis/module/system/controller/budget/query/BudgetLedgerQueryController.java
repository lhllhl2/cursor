package com.jasolar.mis.module.system.controller.budget.query;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.write.metadata.WriteSheet;
import com.jasolar.mis.module.system.util.ExcelExportStyleUtil;
import com.jasolar.mis.framework.common.pojo.CommonResult;
import com.jasolar.mis.framework.common.pojo.PageResult;
import com.jasolar.mis.module.system.controller.budget.vo.AdjustOrderExcelVO;
import com.jasolar.mis.module.system.controller.budget.vo.BudgetLedgerQueryParams;
import com.jasolar.mis.module.system.controller.budget.vo.BudgetLedgerVo;
import com.jasolar.mis.module.system.controller.budget.vo.ContractOrderExcelVO;
import com.jasolar.mis.module.system.controller.budget.vo.DemandOrderExcelVO;
import com.jasolar.mis.module.system.controller.budget.vo.PaymentReimbursementExcelVO;
import com.jasolar.mis.module.system.service.budget.query.BudgetLedgerQueryService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Description: 预算流水查询控制器（需要登录）
 * Author : Auto Generated
 * Date : 2025-01-XX
 * Version : 1.0
 */
@RestController
@RequestMapping(value = "/query")
@Slf4j
@Api(tags = "预算流水查询（需要登录）")
public class BudgetLedgerQueryController {

    @Resource
    private BudgetLedgerQueryService budgetLedgerQueryService;

    /**
     * 查询预算流水数据
     * 
     * @param params 查询参数
     * @return 查询结果
     */
    @PostMapping("/queryData")
    @ApiOperation("查询预算流水数据")
    public CommonResult<PageResult<BudgetLedgerVo>> queryData(@RequestBody @Valid BudgetLedgerQueryParams params) {
        log.info("开始处理预算流水查询，params={}", params);
        PageResult<BudgetLedgerVo> result = budgetLedgerQueryService.queryData(params);
        return CommonResult.success(result);
    }

    /**
     * 导出预算流水数据（动态生成Excel，根据bizType选择不同格式）
     * 
     * @param params 查询参数（支持原有搜索条件）
     * @param response HTTP响应对象
     * @throws IOException 导出异常
     */
    @PostMapping("/export")
    @ApiOperation("导出预算流水数据")
    public void export(@RequestBody @Valid BudgetLedgerQueryParams params, HttpServletResponse response) throws IOException {
        log.info("开始导出预算流水数据，params={}", params);
        try {
            // 设置headless模式，避免Docker容器中缺少字体库导致的错误
            System.setProperty("java.awt.headless", "true");
            try {
                System.setProperty("sun.java2d.fontpath", "");
            } catch (Exception e) {
                // 忽略设置失败
            }
            
            String bizType = params.getBizType();
            if (!StringUtils.hasText(bizType)) {
                throw new IllegalArgumentException("业务类型不能为空");
            }
            
            // 查询全量数据（支持原有搜索条件，但不分页）
            List<BudgetLedgerVo> voList = budgetLedgerQueryService.queryAllData(params);
            log.info("查询到全量数据：{} 条", voList.size());
            
            // 根据bizType选择Excel VO类和文件名
            if ("APPLY".equals(bizType)) {
                String fileName = "需求单报表.xlsx";
                List<DemandOrderExcelVO> excelVoList = voList.stream()
                        .map(this::convertToDemandOrderExcelVO)
                        .collect(Collectors.toList());
                exportExcel(response, fileName, excelVoList, DemandOrderExcelVO.class, "需求单报表");
                log.info("预算流水数据导出完成，共导出 {} 条数据", excelVoList.size());
            } else if ("CONTRACT".equals(bizType)) {
                String fileName = "合同单报表.xlsx";
                List<ContractOrderExcelVO> excelVoList = voList.stream()
                        .map(this::convertToContractOrderExcelVO)
                        .collect(Collectors.toList());
                exportExcel(response, fileName, excelVoList, ContractOrderExcelVO.class, "合同单报表");
                log.info("预算流水数据导出完成，共导出 {} 条数据", excelVoList.size());
            } else if ("CLAIM".equals(bizType)) {
                String fileName = "付款报销单报表.xlsx";
                List<PaymentReimbursementExcelVO> excelVoList = voList.stream()
                        .map(this::convertToPaymentReimbursementExcelVO)
                        .collect(Collectors.toList());
                exportExcel(response, fileName, excelVoList, PaymentReimbursementExcelVO.class, "付款报销单报表");
                log.info("预算流水数据导出完成，共导出 {} 条数据", excelVoList.size());
            } else if ("ADJUST".equals(bizType)) {
                String fileName = "调整单报表.xlsx";
                List<AdjustOrderExcelVO> excelVoList = voList.stream()
                        .map(this::convertToAdjustOrderExcelVO)
                        .collect(Collectors.toList());
                exportExcel(response, fileName, excelVoList, AdjustOrderExcelVO.class, "调整单报表");
                log.info("预算流水数据导出完成，共导出 {} 条数据", excelVoList.size());
            } else {
                throw new IllegalArgumentException("不支持的业务类型：" + bizType);
            }
        } catch (Exception e) {
            log.error("导出预算流水数据失败", e);
            throw new IOException("导出失败：" + e.getMessage(), e);
        }
    }

    /**
     * 通用Excel导出方法（动态生成）
     */
    private <T> void exportExcel(HttpServletResponse response, String fileName, List<T> excelVoList, 
                                  Class<T> excelClass, String sheetName) throws IOException {
        // 设置响应头
        String encodedFileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8);
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("utf-8");
        response.setHeader("Content-Disposition", "attachment; filename=" + encodedFileName);
        
        // 直接使用 EasyExcel API，确保 inMemory(true) 生效，避免字体库依赖
        // 表头：浅灰色背景 + 微软雅黑；内容：微软雅黑
        ExcelWriter excelWriter = EasyExcel.write(response.getOutputStream(), excelClass)
                .inMemory(true) // 使用内存模式，避免 SXSSF 的字体依赖
                .autoCloseStream(false) // 不要自动关闭，交给 Servlet 自己处理
                .registerWriteHandler(ExcelExportStyleUtil.createDefaultStyleStrategy())
                .build();
        
        WriteSheet writeSheet = EasyExcel.writerSheet(sheetName).build();
        excelWriter.write(excelVoList, writeSheet);
        excelWriter.finish();
    }

    /**
     * 将BudgetLedgerVo转换为DemandOrderExcelVO
     */
    private DemandOrderExcelVO convertToDemandOrderExcelVO(BudgetLedgerVo vo) {
        DemandOrderExcelVO excelVO = new DemandOrderExcelVO();
        excelVO.setBizCode(vo.getBizCode());
        excelVO.setStatus(formatStatusDisplay(vo.getStatus()));
        excelVO.setYear(vo.getYear());
        excelVO.setMonth(vo.getMonth());
        excelVO.setMorgCode(vo.getMorgCode());
        excelVO.setMorgName(vo.getMorgName());
        excelVO.setControlEhrCd(vo.getControlEhrCd());
        excelVO.setControlEhrNm(vo.getControlEhrNm());
        excelVO.setBudgetOrgCd(vo.getBudgetOrgCd());
        excelVO.setBudgetOrgNm(vo.getBudgetOrgNm());
        excelVO.setBudgetSubjectCode(vo.getBudgetSubjectCode());
        excelVO.setBudgetSubjectName(vo.getBudgetSubjectName());
        excelVO.setErpAssetType(vo.getErpAssetType());
        excelVO.setErpAssetTypeName(vo.getErpAssetTypeName());
        excelVO.setMasterProjectCode(vo.getMasterProjectCode());
        excelVO.setMasterProjectName(vo.getMasterProjectName());
        excelVO.setIsInternal(formatIsInternalDisplay(vo.getIsInternal()));
        excelVO.setAmount(vo.getAmount());
        excelVO.setAmountAvailable(vo.getAmountAvailable());
        excelVO.setOperator(vo.getOperator());
        excelVO.setOperateTime(formatOperateTime(vo.getUpdateTime(), vo.getCreateTime()));
        excelVO.setDataSource(vo.getDataSource());
        excelVO.setProcessName(vo.getProcessName());
        return excelVO;
    }

    /**
     * 将BudgetLedgerVo转换为ContractOrderExcelVO
     */
    private ContractOrderExcelVO convertToContractOrderExcelVO(BudgetLedgerVo vo) {
        ContractOrderExcelVO excelVO = new ContractOrderExcelVO();
        excelVO.setBizCode(vo.getBizCode());
        // 拼接需求单号列表
        if (!CollectionUtils.isEmpty(vo.getDemandOrderNos())) {
            excelVO.setDemandOrderNos(String.join(";", vo.getDemandOrderNos()));
        }
        excelVO.setStatus(formatStatusDisplay(vo.getStatus()));
        excelVO.setYear(vo.getYear());
        excelVO.setMonth(vo.getMonth());
        excelVO.setMorgCode(vo.getMorgCode());
        excelVO.setMorgName(vo.getMorgName());
        excelVO.setControlEhrCd(vo.getControlEhrCd());
        excelVO.setControlEhrNm(vo.getControlEhrNm());
        excelVO.setBudgetOrgCd(vo.getBudgetOrgCd());
        excelVO.setBudgetOrgNm(vo.getBudgetOrgNm());
        excelVO.setBudgetSubjectCode(vo.getBudgetSubjectCode());
        excelVO.setBudgetSubjectName(vo.getBudgetSubjectName());
        excelVO.setErpAssetType(vo.getErpAssetType());
        excelVO.setErpAssetTypeName(vo.getErpAssetTypeName());
        excelVO.setMasterProjectCode(vo.getMasterProjectCode());
        excelVO.setMasterProjectName(vo.getMasterProjectName());
        excelVO.setIsInternal(formatIsInternalDisplay(vo.getIsInternal()));
        excelVO.setAmount(vo.getAmount());
        excelVO.setAmountAvailable(vo.getAmountAvailable());
        excelVO.setOperator(vo.getOperator());
        excelVO.setOperateTime(formatOperateTime(vo.getUpdateTime(), vo.getCreateTime()));
        excelVO.setDataSource(vo.getDataSource());
        excelVO.setProcessName(vo.getProcessName());
        return excelVO;
    }

    /**
     * 将BudgetLedgerVo转换为PaymentReimbursementExcelVO
     */
    private PaymentReimbursementExcelVO convertToPaymentReimbursementExcelVO(BudgetLedgerVo vo) {
        PaymentReimbursementExcelVO excelVO = new PaymentReimbursementExcelVO();
        excelVO.setBizCode(vo.getBizCode());
        // 拼接合同单号列表
        if (!CollectionUtils.isEmpty(vo.getContractNos())) {
            excelVO.setContractNos(String.join(";", vo.getContractNos()));
        }
        // 拼接需求单号列表
        if (!CollectionUtils.isEmpty(vo.getDemandOrderNos())) {
            excelVO.setDemandOrderNos(String.join(";", vo.getDemandOrderNos()));
        }
        excelVO.setStatus(formatStatusDisplay(vo.getStatus()));
        excelVO.setYear(vo.getYear());
        excelVO.setMonth(vo.getMonth());
        excelVO.setMorgCode(vo.getMorgCode());
        excelVO.setMorgName(vo.getMorgName());
        excelVO.setControlEhrCd(vo.getControlEhrCd());
        excelVO.setControlEhrNm(vo.getControlEhrNm());
        excelVO.setBudgetOrgCd(vo.getBudgetOrgCd());
        excelVO.setBudgetOrgNm(vo.getBudgetOrgNm());
        excelVO.setBudgetSubjectCode(vo.getBudgetSubjectCode());
        excelVO.setBudgetSubjectName(vo.getBudgetSubjectName());
        excelVO.setErpAssetType(vo.getErpAssetType());
        excelVO.setErpAssetTypeName(vo.getErpAssetTypeName());
        excelVO.setMasterProjectCode(vo.getMasterProjectCode());
        excelVO.setMasterProjectName(vo.getMasterProjectName());
        excelVO.setIsInternal(formatIsInternalDisplay(vo.getIsInternal()));
        excelVO.setAmount(vo.getAmount());
        excelVO.setOperator(vo.getOperator());
        excelVO.setOperateTime(formatOperateTime(vo.getUpdateTime(), vo.getCreateTime()));
        excelVO.setDataSource(vo.getDataSource());
        excelVO.setProcessName(vo.getProcessName());
        return excelVO;
    }

    /**
     * 将BudgetLedgerVo转换为AdjustOrderExcelVO
     */
    private AdjustOrderExcelVO convertToAdjustOrderExcelVO(BudgetLedgerVo vo) {
        AdjustOrderExcelVO excelVO = new AdjustOrderExcelVO();
        excelVO.setBizCode(vo.getBizCode());
        excelVO.setStatus(formatStatusDisplay(vo.getStatus()));
        excelVO.setYear(vo.getYear());
        excelVO.setMonth(vo.getMonth());
        excelVO.setMorgCode(vo.getMorgCode());
        excelVO.setMorgName(vo.getMorgName());
        excelVO.setControlEhrCd(vo.getControlEhrCd());
        excelVO.setControlEhrNm(vo.getControlEhrNm());
        excelVO.setBudgetOrgCd(vo.getBudgetOrgCd());
        excelVO.setBudgetOrgNm(vo.getBudgetOrgNm());
        excelVO.setBudgetSubjectCode(vo.getBudgetSubjectCode());
        excelVO.setBudgetSubjectName(vo.getBudgetSubjectName());
        excelVO.setErpAssetType(vo.getErpAssetType());
        excelVO.setErpAssetTypeName(vo.getErpAssetTypeName());
        excelVO.setMasterProjectCode(vo.getMasterProjectCode());
        excelVO.setMasterProjectName(vo.getMasterProjectName());
        excelVO.setIsInternal(formatIsInternalDisplay(vo.getIsInternal()));
        excelVO.setEffectType(vo.getEffectType());
        excelVO.setAmount(vo.getAmount());
        excelVO.setAmountAvailable(vo.getAmountAvailable());
        excelVO.setAmountConsumedQOne(vo.getAmountConsumedQOne());
        excelVO.setAmountConsumedQTwo(vo.getAmountConsumedQTwo());
        excelVO.setAmountConsumedQThree(vo.getAmountConsumedQThree());
        excelVO.setAmountConsumedQFour(vo.getAmountConsumedQFour());
        excelVO.setOperator(vo.getOperator());
        excelVO.setOperateTime(formatOperateTime(vo.getUpdateTime(), vo.getCreateTime()));
        excelVO.setDataSource(vo.getDataSource());
        excelVO.setProcessName(vo.getProcessName());
        return excelVO;
    }

    /**
     * 格式化操作时间：优先取更新时间，为空则取创建时间
     */
    private static String formatOperateTime(LocalDateTime updateTime, LocalDateTime createTime) {
        LocalDateTime time = updateTime != null ? updateTime : createTime;
        return time == null ? null : time.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    /**
     * 状态码转导出展示文案：SUBMITTED->已提交，APPROVED->已审批，APPROVED_UPDATE->汇联易_审批后更新
     */
    private static String formatStatusDisplay(String status) {
        if (status == null) {
            return null;
        }
        return switch (status) {
            case "SUBMITTED" -> "已提交";
            case "APPROVED" -> "已审批";
            case "APPROVED_UPDATE" -> "汇联易_审批后更新";
            default -> status;
        };
    }

    /**
     * 集团内/集团外转导出展示文案：1->集团内，0->集团外
     */
    private static String formatIsInternalDisplay(String isInternal) {
        if (isInternal == null) {
            return null;
        }
        return "1".equals(isInternal) ? "集团内" : "0".equals(isInternal) ? "集团外" : isInternal;
    }
}

