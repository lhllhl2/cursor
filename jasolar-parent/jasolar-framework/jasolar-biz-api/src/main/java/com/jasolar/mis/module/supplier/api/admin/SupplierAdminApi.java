package com.jasolar.mis.module.supplier.api.admin;

import com.jasolar.mis.bizapi.Apis;
import com.jasolar.mis.framework.common.pojo.CommonResult;
import com.jasolar.mis.framework.common.pojo.PageResult;
import com.jasolar.mis.module.supplier.api.admin.dto.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(name = Apis.SUPPLIER)
@Tag(name = "RPC 服务 - 供应商门户相关")
public interface SupplierAdminApi {

    String PREFIX = Apis.SUPPLIER_PREFIX + "/admin";

    @PostMapping(PREFIX + "/page")
    @Operation(summary = "获取供应商分页信息")
    @Parameter(name = "supplierPageReqDTO", description = "获取供应商Page信息", example = "1,2", required = true)
    CommonResult<PageResult<SupplierRespDTO>> queryPageSuppliers(@RequestBody SupplierPageReqDTO supplierPageReqDTO);

    @PostMapping(PREFIX + "/list")
    @Operation(summary = "获取供应商List信息")
    @Parameter(name = "supplierReqDTO", description = "获取供应商List信息", example = "1,2", required = true)
    CommonResult<List<SupplierRespDTO>> querySupplierList(@RequestBody SupplierReqDTO supplierReqDTO);

    @GetMapping(PREFIX + "/get")
    @Operation(summary = "获取供应商信息")
    @Parameter(name = "supplierNo", description = "供应商编号", example = "1111", required = true)
    CommonResult<SupplierRespDTO> getSupplier(@RequestParam("supplierNo") String supplierNo);


    @GetMapping(PREFIX + "/get-for-cache")
    @Operation(summary = "获取供应商信息")
    @Parameter(name = "supplierNo", description = "供应商编号", example = "1111", required = true)
    CommonResult<SupplierRespDTO> getSupplier4Cache(@RequestParam("supplierNo") String supplierNo);


    @PostMapping(PREFIX + "/list-supplier")
    @Operation(summary = "批量获取供应商信息（根据供应商code）")
    CommonResult<List<SupplierRespDTO>> listSupplierByCodes(@RequestBody List<String> supplierCodes);

    @PostMapping(PREFIX + "/list-by-nos")
    @Operation(summary = "批量获取供应商信息（根据供应商登录号）")
    CommonResult<List<SupplierRespDTO>> listSupplierByNos(@RequestBody List<String> supplierNos);

    @PostMapping(PREFIX + "/list-for-order")
    @Operation(summary = "查询供应商银行信息")
    CommonResult<List<BankAccountDTO>> getBankAccountList4Order(@Valid @RequestBody BankAccountQueryDTO queryVO);

    @GetMapping(PREFIX + "/get-by-no")
    @Operation(summary = "通过供应商登录号获得供应商明细信息")
    CommonResult<SupplierDTO> getSupplierBySupplierNo(@RequestParam("supplierNo") String supplierNo);

    @PostMapping(PREFIX + "/get-by-no-batch")
    @Operation(summary = "通过供应商登录号集合获得供应商明细信息")
    CommonResult<List<SupplierDTO>> getSupplierBySupplierNoBatch(@RequestBody List<String> supplierNos);

    @PostMapping(PREFIX + "/list-contact-by-nos")
    @Operation(summary = "根据供应商Nos查询联系人信息")
    CommonResult<List<SupplierContactDTO>> listContactBySupNos(@RequestBody List<String> supplierNos);

    /**
     * 查询供应商在法人下的统制科目. 一个供应商一个法人对应的应该只有一个统制科目
     * 
     * @param supplierNo 供应商NO
     * @param legalCode 法人CODE
     * @return
     */
    @GetMapping(PREFIX + "/get-controlled-subject")
    @Operation(summary = "根据supplierNo和legalCode查询统制科目")
    CommonResult<ControlledSubjectDTO> getControlledSubject(@RequestParam("supplierNo") String supplierNo,
            @RequestParam("legalCode") String legalCode);

    @GetMapping(PREFIX + "/get-by-tax-reg-number")
    @Operation(summary = "通过税务登记号获得供应商信息")
    CommonResult<SupplierDTO> getByTaxRegNumber(@RequestParam("taxRgeNumber") String taxRegNumber);
}
