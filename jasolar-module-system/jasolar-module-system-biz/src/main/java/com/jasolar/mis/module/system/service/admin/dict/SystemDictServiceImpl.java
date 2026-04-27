package com.jasolar.mis.module.system.service.admin.dict;

import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jasolar.mis.framework.common.exception.ServiceException;
import com.jasolar.mis.framework.common.pojo.PageResult;
import com.jasolar.mis.framework.common.pojo.PrimaryParam;
import com.jasolar.mis.module.system.controller.admin.dict.vo.*;
import com.jasolar.mis.module.system.domain.admin.dict.SystemDictDo;
import com.jasolar.mis.module.system.domain.admin.dict.SystemDictLabelDo;
import com.jasolar.mis.module.system.exceptioncode.DictErrorCodeConstants;
import com.jasolar.mis.module.system.exceptioncode.DictLabelErrorCodeConstants;
import com.jasolar.mis.module.system.mapper.admin.dict.SystemDictLabelMapper;
import com.jasolar.mis.module.system.mapper.admin.dict.SystemDictMapper;
import com.jasolar.mis.module.system.util.IPageToPageResultUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class SystemDictServiceImpl implements SystemDictService {


    @Autowired
    private SystemDictMapper systemDictMapper;

    @Autowired
    private SystemDictLabelMapper systemDictLabelMapper;


    @Transactional
    @Override
    public void addDict(DictAddVo dictAddVo) {

        boolean codeExist = systemDictMapper.filedExist(SystemDictDo::getCode,dictAddVo.getCode());
        if(codeExist){
            throw new ServiceException(DictErrorCodeConstants.DICT_CODE_REPLICATION);
        }
        boolean titleExist = systemDictMapper.filedExist(SystemDictDo::getTitle, dictAddVo.getTitle());
        if(titleExist){
            throw new ServiceException(DictErrorCodeConstants.DICT_TITLE_REPLICATION);
        }

        SystemDictDo systemDictDo = SystemDictDo.builder()
                .code(dictAddVo.getCode())
                .title(dictAddVo.getTitle())
                .build();
        systemDictMapper.insert(systemDictDo);

        if(CollectionUtil.isNotEmpty(dictAddVo.getLabelList())){
            List<DictLabelVo> labels = dictAddVo.getLabelList();
            // key
            List<String> fieldKeys = labels.stream().map(DictLabelVo::getFieldKey).toList();
            boolean fieldReplicate = replicate(fieldKeys);
            if(fieldReplicate){
                throw new ServiceException(DictLabelErrorCodeConstants.DICT_LABEL_KEY_VALUE_REPLICATION);
            }

            // label
            List<String> fieldLabels = labels.stream().map(DictLabelVo::getFieldLabel).toList();
            boolean labelReplicate = replicate(fieldLabels);
            if(labelReplicate){
                throw new ServiceException(DictLabelErrorCodeConstants.DICT_LABEL_VALUE_REPLICATION);
            }

            List<SystemDictLabelDo> labelList = dictAddVo.getLabelList().stream()
                    .map(x ->
                            SystemDictLabelDo.builder()
                                    .dictId(systemDictDo.getId())
                                    .fieldKey(x.getFieldKey())
                                    .fieldLabel(x.getFieldLabel())
                                    .build()
                    ).toList();
            systemDictLabelMapper.insertBatch(labelList);
        }
    }


    private <S> boolean replicate(List<S> data){
        if(CollectionUtil.isEmpty(data)){
            return false;
        }
        Set<S> set = new HashSet<>(data);
        return set.size() != data.size();
    }

    @Transactional
    @Override
    public void editDict(DictEditVo dictEditVo) {
        SystemDictDo systemDictDo = systemDictMapper.selectById(dictEditVo.getId());
        if(Objects.isNull(systemDictDo)){
            throw new ServiceException(DictErrorCodeConstants.DICT_NOT_EXIST);
        }
        boolean codeExist = systemDictMapper.filedExistNotSelf(SystemDictDo::getCode,
                dictEditVo.getCode(),
                dictEditVo.getId());
        if(codeExist){
            throw new ServiceException(DictErrorCodeConstants.DICT_CODE_REPLICATION);
        }
        boolean titleExist = systemDictMapper.filedExistNotSelf(SystemDictDo::getTitle,
                dictEditVo.getTitle(),
                dictEditVo.getId());
        if(titleExist){
            throw new ServiceException(DictErrorCodeConstants.DICT_TITLE_REPLICATION);
        }
        systemDictDo.setCode(dictEditVo.getCode());
        systemDictDo.setTitle(dictEditVo.getTitle());
        systemDictMapper.updateById(systemDictDo);

        List<SystemDictLabelDo> labelDos = systemDictLabelMapper.selectBySimpleFields(SystemDictLabelDo::getDictId,dictEditVo.getId());
        List<DictLabelVo> labelList = dictEditVo.getLabelList();
        if(CollectionUtil.isEmpty(labelDos)){
            if(CollectionUtil.isNotEmpty(labelList)){
                List<SystemDictLabelDo> addDictLabelList = labelList.stream().map(x ->
                        SystemDictLabelDo.builder()
                                .dictId(systemDictDo.getId())
                                .fieldKey(x.getFieldKey())
                                .fieldLabel(x.getFieldLabel())
                                .build()
                ).toList();
                systemDictLabelMapper.insertBatch(addDictLabelList);
            }
            return;
        }

        List<String> fieldKeyList = labelList.stream().map(DictLabelVo::getFieldKey).toList();
        if(replicate(fieldKeyList)){
            throw new ServiceException(DictLabelErrorCodeConstants.DICT_LABEL_KEY_VALUE_REPLICATION);
        }

        List<String> fieldLabelList = labelList.stream().map(DictLabelVo::getFieldLabel).toList();
        if(replicate(fieldLabelList)){
            throw new ServiceException(DictLabelErrorCodeConstants.DICT_LABEL_VALUE_REPLICATION);
        }

        // 增集
        List<DictLabelVo> addList = labelList.stream().filter(x -> Objects.isNull(x.getId())).toList();
        List<SystemDictLabelDo> addDictLabels = addList.stream().map(x ->
                SystemDictLabelDo.builder()
                        .dictId(systemDictDo.getId())
                        .fieldKey(x.getFieldKey())
                        .fieldLabel(x.getFieldLabel())
                        .build()
        ).toList();
        systemDictLabelMapper.insertBatch(addDictLabels);

        // 改集
        Map<Long, SystemDictLabelDo> oldMap = labelDos.stream().collect(Collectors.toMap(SystemDictLabelDo::getId, x -> x));
        List<SystemDictLabelDo> updateList = labelList.stream().filter(x -> oldMap.containsKey(x.getId()))
                .map(x -> {
                    SystemDictLabelDo systemDictLabelDo = oldMap.get(x.getId());
                    systemDictLabelDo.setFieldKey(x.getFieldKey());
                    systemDictLabelDo.setFieldLabel(x.getFieldLabel());
                    return systemDictLabelDo;
                }).toList();
        if(CollectionUtil.isNotEmpty(updateList)){
            systemDictLabelMapper.updateBatch(updateList);
        }


        // 删集
        List<Long> oldIds = labelDos.stream().map(SystemDictLabelDo::getId).toList();
        List<Long> paramIds = labelList.stream().map(DictLabelVo::getId).toList();

        List<Long> deleteIds = CollectionUtil.subtractToList(oldIds, paramIds);
        if(CollectionUtil.isNotEmpty(deleteIds)){
            systemDictLabelMapper.deleteByIds(deleteIds);
        }

    }


    @Override
    public PageResult<DictEditVo> page(DictSearchParams dictSearchParams) {
        IPage<DictEditVo> iPage = new Page<>(dictSearchParams.getPageNo(),dictSearchParams.getPageSize());
        iPage =  systemDictMapper.selectPageBy(iPage,dictSearchParams);
        PageResult<DictEditVo> pageResult = IPageToPageResultUtils.transfer(iPage);
        List<DictEditVo> row = pageResult.getList();
        if(CollectionUtil.isNotEmpty(row)){
            List<Long> ids = row.stream().map(DictEditVo::getId).toList();
            List<DictLabelVo> labelList =  systemDictLabelMapper.searchByDictIds(ids);
            if(CollectionUtil.isNotEmpty(labelList)){
                Map<Long, List<DictLabelVo>> labelMaps = labelList.stream().collect(Collectors.groupingBy(DictLabelVo::getDictId));
                row.forEach(x -> {
                    if (labelMaps.containsKey(x.getId())) {
                        x.setLabelList(labelMaps.get(x.getId()));
                    }
                });
            }
        }
        return pageResult;
    }


    @Transactional
    @Override
    public void del(PrimaryParam primaryParam) {
        SystemDictDo systemDictDo = systemDictMapper.selectById(primaryParam.getId());
        if(Objects.isNull(systemDictDo)){
            throw new ServiceException(DictErrorCodeConstants.DICT_NOT_EXIST);
        }
        systemDictMapper.deleteById(systemDictDo);

        systemDictLabelMapper.deleteByDictId(primaryParam.getId());


    }


    @Override
    public Map<String, DictEditVo> getByCode(DictInfoByCodeVo dictInfoByCodeVo) {
        List<DictEditVo> list =  systemDictMapper.selectDictSimpleInfoByCode(dictInfoByCodeVo.getCodes());
        if(CollectionUtil.isNotEmpty(list)){
            List<Long> ids = list.stream().map(DictEditVo::getId).toList();
            List<DictLabelVo> labelList =  systemDictLabelMapper.searchByDictIds(ids);
            if(CollectionUtil.isNotEmpty(labelList)){
                Map<Long, List<DictLabelVo>> labelMaps = labelList.stream().collect(Collectors.groupingBy(DictLabelVo::getDictId));
                list.forEach(x -> {
                    if (labelMaps.containsKey(x.getId())) {
                        x.setLabelList(labelMaps.get(x.getId()));
                    }
                });
            }
        }
        Map<String, DictEditVo> map = list.stream().collect(Collectors.toMap(DictEditVo::getCode, x -> x));
        return CollectionUtil.isNotEmpty(list) ? map : Map.of();
    }



    public String getFieldLabel(Map<String, DictEditVo> map,String key,String fieldKey ){

        DictEditVo dictEditVo = map.get(key);
        if(Objects.isNull(dictEditVo)){
            return null;
        }
        List<DictLabelVo> labelList = dictEditVo.getLabelList();
        for (DictLabelVo dictLabelVo : labelList) {
            if(Objects.equals(fieldKey,dictLabelVo.getFieldKey())){
                return dictLabelVo.getFieldLabel();
            }
        }
        return null;
    }
}



