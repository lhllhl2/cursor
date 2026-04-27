package com.jasolar.mis.module.masterdata.api.material;

import java.util.List;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.jasolar.mis.bizapi.Apis;
import com.jasolar.mis.framework.common.pojo.CommonResult;
import com.jasolar.mis.module.masterdata.dto.MaterialCategoryDTO;
import com.jasolar.mis.module.masterdata.dto.MaterialCategoryQueryDTO;
import com.jasolar.mis.module.masterdata.dto.MaterialDTO;
import com.jasolar.mis.module.masterdata.dto.MaterialPriceDTO;
import com.jasolar.mis.module.masterdata.dto.MaterialPriceQueryDTO;
import com.jasolar.mis.module.masterdata.dto.MaterialQueryDTO;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * @Classname MaterialApi
 * @Description
 * @Date 07/04/2025 20:58
 * @Created by yiptan
 */
@FeignClient(name = Apis.MASTERDATA)
@Tag(name = "物料信息")
public interface MaterialApi {


    String PREFIX = Apis.MASTERDATA_PREFIX + "/materials";

    /**
     * 查询料号主数据
     * 
     * @param query
     * @return
     */
    @PostMapping(PREFIX)
    @Operation(summary = "查询料号主数据")
    CommonResult<List<MaterialDTO>> search(@RequestBody MaterialQueryDTO query);

    /**
     * 查询价格数据
     * 
     * @param query
     * @return
     */
    @PostMapping(PREFIX + "/prices")
    @Operation(summary = "查询料号价格（价格维度）")
    CommonResult<List<MaterialPriceDTO>> listMaterialPrice(@RequestBody MaterialPriceQueryDTO query);

    /**
     * 查询物料分类
     * 
     * @param query 查询条件
     * @return
     */
    @PostMapping(Apis.MASTERDATA_PREFIX + "/categories")
    @Operation(summary = "查询物料分类主数据")
    CommonResult<List<MaterialCategoryDTO>> searchMaterialCategories(@RequestBody MaterialCategoryQueryDTO query);



    /**
     * 区域加料号查询最新价格
     *
     * @param query
     * @return
     */
    @PostMapping(PREFIX + "/batch-only-price")
    @Operation(summary = "区域加料号批量查询价格(仅查询最新跟是否有效无关)")
    CommonResult<List<MaterialPriceDTO>> batchOnlyPrice(@RequestBody List<MaterialPriceQueryDTO> query);

}
