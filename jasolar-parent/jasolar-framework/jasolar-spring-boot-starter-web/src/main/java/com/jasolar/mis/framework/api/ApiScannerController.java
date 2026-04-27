package com.jasolar.mis.framework.api;

import com.jasolar.mis.framework.common.pojo.CommonResult;
import com.jasolar.mis.module.system.api.permission.dto.ApiImportResult;
import com.jasolar.mis.module.system.api.permission.dto.InterfaceResourceDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;
import java.util.Map;

import static com.jasolar.mis.framework.common.pojo.CommonResult.success;

/**
 * API扫描控制器
 * 提供手动触发扫描和查询接口资源的REST接口
 */
@Tag(name = "管理：接口资源")
@RequestMapping("/api/scanner")
@RequiredArgsConstructor
public class ApiScannerController {

    private final ApiScannerUtil apiScannerUtil;

    private final InterfaceResourceCaller interfaceResourceCaller;

    @GetMapping("/scan")
    @Operation(summary = "扫描API接口")
    public CommonResult<List<InterfaceResource>> scan() {
        return CommonResult.success(apiScannerUtil.scan());
    }

    @GetMapping("/list")
    @Operation(summary = "获取所有接口资源")
    public CommonResult<List<InterfaceResource>> getAll() {
        return CommonResult.success(apiScannerUtil.getInterfaceResources());
    }

    @GetMapping("/categories")
    @Operation(summary = "按分类获取接口资源")
    public CommonResult<Map<String, List<InterfaceResource>>> getByCategory() {
        return CommonResult.success(apiScannerUtil.getInterfaceResourcesByCategory());
    }

    @GetMapping("/dto-list")
    @Operation(summary = "获取DTO格式的接口资源列表")
    public CommonResult<List<InterfaceResourceDTO>> getDtoList() {
        return CommonResult.success(apiScannerUtil.getInterfaceResourceList(
                apiScannerUtil.getInterfaceResources()));
    }

    @PostMapping("/import")
    @Operation(summary = "导入扫描到的API到数据库")
    public CommonResult<ApiImportResult> importToDb() {
        List<InterfaceResource> apis = apiScannerUtil.getInterfaceResources();
        ApiImportResult apiImportResult = new ApiImportResult();
        if (apis.isEmpty() || null == interfaceResourceCaller) {
            return success(apiImportResult);
        }
        return CommonResult.success(interfaceResourceCaller.importData(apiScannerUtil.getInterfaceResourceList(apis)));
    }
} 