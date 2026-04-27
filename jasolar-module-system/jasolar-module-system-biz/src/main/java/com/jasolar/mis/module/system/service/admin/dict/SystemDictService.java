package com.jasolar.mis.module.system.service.admin.dict;

import com.jasolar.mis.framework.common.pojo.PageResult;
import com.jasolar.mis.framework.common.pojo.PrimaryParam;
import com.jasolar.mis.module.system.controller.admin.dict.vo.DictAddVo;
import com.jasolar.mis.module.system.controller.admin.dict.vo.DictEditVo;
import com.jasolar.mis.module.system.controller.admin.dict.vo.DictInfoByCodeVo;
import com.jasolar.mis.module.system.controller.admin.dict.vo.DictSearchParams;
import jakarta.validation.Valid;

import java.util.Map;

public interface SystemDictService {

    /**
     * 添加字段
     * @param dictAddVo
     */
    void addDict(@Valid DictAddVo dictAddVo);

    /**
     * 修改
     * @param dictEditVo
     */
    void editDict(@Valid DictEditVo dictEditVo);

    /**
     * 分页查询
     * @param dictSearchParams
     * @return
     */
    PageResult<DictEditVo> page(DictSearchParams dictSearchParams);

    /**
     * 删除
     * @param primaryParam
     */
    void del(@Valid PrimaryParam primaryParam);

    /**
     * 根据 codes 查询相关字典信息
     * @param dictInfoByCodeVo
     * @return
     */
    Map<String, DictEditVo> getByCode(DictInfoByCodeVo dictInfoByCodeVo);



    String getFieldLabel(Map<String, DictEditVo> map,String key,String fieldKey );

}