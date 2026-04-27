package com.jasolar.mis.module.system.mapper.admin.org;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.jasolar.mis.framework.mybatis.core.mapper.BaseMapperX;
import com.jasolar.mis.module.system.domain.admin.org.SystemManageOrgDO;
import com.jasolar.mis.module.system.resp.ManageOrgExceptResp;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 组织 Mapper
 *
 * @author jasolar
 */
@Mapper
public interface SystemManageOrgMapper extends BaseMapperX<SystemManageOrgDO> {


    /**
     * 查询法人组织列表
     * 查询TYPE为LE且IS_LAST_LVL为Y的组织数据
     * 只返回PID, CODE, NAME三个字段
     *
     * @return 法人组织DO列表
     */
    default List<SystemManageOrgDO> selectLegalOrgList() {
        LambdaQueryWrapper<SystemManageOrgDO> queryWrapper = new LambdaQueryWrapper<SystemManageOrgDO>()
                .eq(SystemManageOrgDO::getIsLastLvl, true)
                .select(SystemManageOrgDO::getPCode, SystemManageOrgDO::getCode, SystemManageOrgDO::getName);
        
        return selectList(queryWrapper);
    }

    /**
     * 根据年份查询组织数量
     * @return
     */
    default long selectCountByYear() {
        return selectCount();
    }

    /**
     * 获取组织差异数据
     * @return
     */
    List<ManageOrgExceptResp> getExceptData();


}