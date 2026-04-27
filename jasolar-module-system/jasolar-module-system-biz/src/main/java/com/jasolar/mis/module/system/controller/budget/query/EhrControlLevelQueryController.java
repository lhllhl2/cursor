package com.jasolar.mis.module.system.controller.budget.query;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.write.metadata.WriteSheet;
import com.jasolar.mis.framework.common.pojo.CommonResult;
import com.jasolar.mis.framework.common.pojo.PageResult;
import com.jasolar.mis.module.system.controller.budget.vo.EhrControlLevelExcelVO;
import com.jasolar.mis.module.system.controller.budget.vo.EhrControlLevelQueryParams;
import com.jasolar.mis.module.system.controller.budget.vo.EhrControlLevelQueryVo;
import com.jasolar.mis.module.system.service.budget.query.EhrControlLevelQueryService;
import com.jasolar.mis.module.system.util.ExcelExportStyleUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Description: EHR控制层级查询控制器
 * Author : Auto Generated
 * Date : 2025-01-XX
 * Version : 1.0
 */
@RestController
@RequestMapping(value = "/query")
@Slf4j
@Api(tags = "EHR控制层级查询")
public class EhrControlLevelQueryController {

    @Resource
    private EhrControlLevelQueryService ehrControlLevelQueryService;

    /**
     * EHR控制层级分页查询
     * 
     * @param params 查询参数
     * @return 查询结果
     */
    @PostMapping("/queryEhrControlLevel")
    @ApiOperation("EHR控制层级分页查询（支持模糊搜索）")
    public CommonResult<PageResult<EhrControlLevelQueryVo>> queryEhrControlLevel(@RequestBody @Valid EhrControlLevelQueryParams params) {
        log.info("开始处理EHR控制层级查询，params={}", params);
        PageResult<EhrControlLevelQueryVo> result = ehrControlLevelQueryService.queryEhrControlLevel(params);
        return CommonResult.success(result);
    }

    /**
     * 导出EHR控制层级（动态生成Excel）
     *
     * @param params   查询参数（支持原有搜索条件）
     * @param response HTTP响应对象
     * @throws IOException 导出异常
     */
    @PostMapping("/exportEhrControlLevel")
    @ApiOperation("导出EHR控制层级")
    public void exportEhrControlLevel(@RequestBody @Valid EhrControlLevelQueryParams params, HttpServletResponse response) throws IOException {
        log.info("开始导出EHR控制层级，params={}", params);
        try {
            System.setProperty("java.awt.headless", "true");
            try {
                System.setProperty("sun.java2d.fontpath", "");
            } catch (Exception e) {
                // 忽略设置失败
            }

            List<EhrControlLevelQueryVo> voList = ehrControlLevelQueryService.queryEhrControlLevelAll(params);
            log.info("查询到全量数据：{} 条", voList.size());

            List<EhrControlLevelExcelVO> excelVoList = voList.stream()
                    .map(this::convertToExcelVO)
                    .collect(Collectors.toList());

            String fileName = "EHR控制层级.xlsx";
            String encodedFileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8);
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setCharacterEncoding("utf-8");
            response.setHeader("Content-Disposition", "attachment; filename=" + encodedFileName);

            ExcelWriter excelWriter = EasyExcel.write(response.getOutputStream(), EhrControlLevelExcelVO.class)
                    .inMemory(true)
                    .autoCloseStream(false)
                    .registerWriteHandler(ExcelExportStyleUtil.createDefaultStyleStrategy())
                    .build();

            WriteSheet writeSheet = EasyExcel.writerSheet("EHR控制层级").build();
            excelWriter.write(excelVoList, writeSheet);
            excelWriter.finish();

            log.info("EHR控制层级导出完成，共导出 {} 条数据", excelVoList.size());
        } catch (Exception e) {
            log.error("导出EHR控制层级失败", e);
            throw new IOException("导出失败：" + e.getMessage(), e);
        }
    }

    private EhrControlLevelExcelVO convertToExcelVO(EhrControlLevelQueryVo vo) {
        EhrControlLevelExcelVO excelVO = new EhrControlLevelExcelVO();
        excelVO.setEhrCd(vo.getEhrCd());
        excelVO.setEhrNm(vo.getEhrNm());
        excelVO.setControlEhrCd(vo.getControlEhrCd());
        excelVO.setControlEhrNm(vo.getControlEhrNm());
        excelVO.setBudgetOrgCd(vo.getBudgetOrgCd());
        excelVO.setBudgetOrgNm(vo.getBudgetOrgNm());
        excelVO.setBudgetEhrCd(vo.getBudgetEhrCd());
        excelVO.setBudgetEhrNm(vo.getBudgetEhrNm());
        excelVO.setErpDepart(vo.getErpDepart());
        return excelVO;
    }
}

