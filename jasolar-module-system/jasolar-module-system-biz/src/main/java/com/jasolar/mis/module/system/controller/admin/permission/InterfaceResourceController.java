package com.jasolar.mis.module.system.controller.admin.permission;

import com.jasolar.mis.framework.apilog.core.annotation.ApiAccessLog;
import com.jasolar.mis.framework.common.pojo.CommonResult;
import com.jasolar.mis.framework.common.pojo.PageParam;
import com.jasolar.mis.framework.common.pojo.PageResult;
import com.jasolar.mis.framework.common.util.object.BeanUtils;
import com.jasolar.mis.framework.excel.core.util.ExcelUtils;
import com.jasolar.mis.module.system.controller.admin.permission.vo.interfaceResource.InterfaceResourcePageReqVO;
import com.jasolar.mis.module.system.controller.admin.permission.vo.interfaceResource.InterfaceResourceRespVO;
import com.jasolar.mis.module.system.controller.admin.permission.vo.interfaceResource.InterfaceResourceSaveReqVO;
import com.jasolar.mis.module.system.domain.admin.permission.InterfaceResourceDO;
import com.jasolar.mis.module.system.service.permission.InterfaceResourceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

import static com.jasolar.mis.framework.apilog.core.enums.OperateTypeEnum.EXPORT;
import static com.jasolar.mis.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 接口资源")
@RestController
@RequestMapping("/system/interface-resource")
@Validated
public class InterfaceResourceController {

    @Resource
    private InterfaceResourceService interfaceResourceService;

    @PostMapping("/create")
    @Operation(summary = "创建接口资源")
    public CommonResult<Long> createInterfaceResource(@Valid @RequestBody InterfaceResourceSaveReqVO createReqVO) {
        return success(interfaceResourceService.createInterfaceResource(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新接口资源")
    public CommonResult<Boolean> updateInterfaceResource(@Valid @RequestBody InterfaceResourceSaveReqVO updateReqVO) {
        interfaceResourceService.updateInterfaceResource(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除接口资源")
    @Parameter(name = "id", description = "编号", required = true)
    public CommonResult<Boolean> deleteInterfaceResource(@RequestParam("id") Long id) {
        interfaceResourceService.deleteInterfaceResource(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得接口资源")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    public CommonResult<InterfaceResourceRespVO> getInterfaceResource(@RequestParam("id") Long id) {
        InterfaceResourceDO interfaceResource = interfaceResourceService.getInterfaceResource(id);
        return success(BeanUtils.toBean(interfaceResource, InterfaceResourceRespVO.class));
    }

    @GetMapping("/page")
    @Operation(summary = "获得接口资源分页")
    public CommonResult<PageResult<InterfaceResourceRespVO>> getInterfaceResourcePage(@Valid InterfaceResourcePageReqVO pageReqVO) {
        PageResult<InterfaceResourceDO> pageResult = interfaceResourceService.getInterfaceResourcePage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, InterfaceResourceRespVO.class));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出接口资源 Excel")
    @ApiAccessLog(operateType = EXPORT)
    public void exportInterfaceResourceExcel(@Valid InterfaceResourcePageReqVO pageReqVO,
                                             HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<InterfaceResourceDO> list = interfaceResourceService.getInterfaceResourcePage(pageReqVO).getList();
        // 导出 Excel
        ExcelUtils.write(response, "接口资源.xls", "数据", InterfaceResourceRespVO.class,
                BeanUtils.toBean(list, InterfaceResourceRespVO.class));
    }

    @GetMapping("/service-names")
    @Operation(summary = "获取所有服务名称")
    public CommonResult<List<String>> getAllServiceName() {
        return CommonResult.success(interfaceResourceService.getAllServiceName());
    }

    @GetMapping("/category-names")
    @Operation(summary = "根据给定服务名称获取分类列表")
    @Parameter(name = "serviceName", description = "服务名称", required = true, example = "system-service")
    public CommonResult<List<String>> getAllCategoryNamesByServiceName(@RequestParam("serviceName") String serviceName) {
        return CommonResult.success(interfaceResourceService.getAllCategoryNamesByServiceName(serviceName));
    }


}