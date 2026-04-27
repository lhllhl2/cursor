package com.jasolar.mis.module.system.controller.admin.dict;

import com.jasolar.mis.framework.common.pojo.CommonResult;
import com.jasolar.mis.framework.common.pojo.PageResult;
import com.jasolar.mis.framework.common.pojo.PrimaryParam;
import com.jasolar.mis.module.system.controller.admin.dict.vo.DictAddVo;
import com.jasolar.mis.module.system.controller.admin.dict.vo.DictEditVo;
import com.jasolar.mis.module.system.controller.admin.dict.vo.DictInfoByCodeVo;
import com.jasolar.mis.module.system.controller.admin.dict.vo.DictSearchParams;
import com.jasolar.mis.module.system.service.admin.dict.SystemDictService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

import static com.jasolar.mis.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 字典定义")
@RestController
@RequestMapping("/system/dict")
public class SystemDictController {

    @Resource
    private SystemDictService systemDictService;

    @Operation(summary = "1.新增字典")
    @PostMapping("/add")
    public CommonResult<Void> add(@Valid @RequestBody DictAddVo dictAddVo) {
        systemDictService.addDict(dictAddVo);
        return success();
    }

    @Operation(summary = "2.修改字典")
    @PostMapping("/edit")
    public CommonResult<Void> edit(@Valid @RequestBody DictEditVo dictEditVo){
        systemDictService.editDict(dictEditVo);
        return success();
    }

    @Operation(summary = "3.分页查询字典")
    @PostMapping("/page")
    public CommonResult<PageResult<DictEditVo>> page(@RequestBody DictSearchParams dictSearchParams) {
        PageResult<DictEditVo> page = systemDictService.page(dictSearchParams);
        return success(page);
    }


    @Operation(summary = "4.删除")
    @PostMapping("/del")
    public CommonResult<Void> del(@Valid @RequestBody PrimaryParam primaryParam){
        systemDictService.del(primaryParam);

        return success();
    }

    @Operation(summary = "5.根据codes获取字典相关信息")
    @PostMapping("/getByCode")
    public CommonResult<Map<String,DictEditVo>> getByCode(@RequestBody DictInfoByCodeVo dictInfoByCodeVo){
        Map<String,DictEditVo> map = systemDictService.getByCode(dictInfoByCodeVo);
        return success(map);
    }






}