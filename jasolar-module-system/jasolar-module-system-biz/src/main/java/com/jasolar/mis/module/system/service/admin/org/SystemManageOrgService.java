package com.jasolar.mis.module.system.service.admin.org;

import com.jasolar.mis.framework.common.pojo.PageResult;
import com.jasolar.mis.module.system.api.org.vo.OrgRespVO;
import com.jasolar.mis.module.system.controller.admin.dict.vo.DictEditVo;
import com.jasolar.mis.module.system.controller.admin.org.vo.*;
import com.jasolar.mis.module.system.domain.admin.org.SystemManageOrgDO;
import jakarta.validation.Valid;

import java.util.List;
import java.util.Map;

/**
 * 组织 Service 接口
 *
 * @author jasolar
 */
public interface SystemManageOrgService {

    /**
     * 分页查询组织列表
     *
     * @param reqVO 查询条件
     * @return 组织分页结果
     */
    PageResult<OrgRespVO> getOrgPage(@Valid OrgPageReqVO reqVO);
    
    /**
     * 搜索组织列表（带搜索条件的查询）
     *
     * @param reqVO 查询条件
     * @return 组织分页结果
     */
    PageResult<OrgRespVO> searchOrgPage(@Valid OrgPageReqVO reqVO);
    
    /**
     * 组织绑定用户组
     *
     * @param reqVO 绑定请求
     */
    void bindUserGroups(@Valid OrgBindUserGroupReqVO reqVO);
    
    /**
     * 查询组织列表（不分页）
     *
     * @param reqVO 查询条件
     * @return 组织列表
     */
    List<SystemManageOrgDO> getOrgList(@Valid ManageOrgListReqVO reqVO);
    
    /**
     * 去重组织ID列表（如果某个组织是其他组织的父级，则只返回父级）
     * 通过路径比较，如果某个组织的路径是其他组织路径的前缀，则只保留父级组织
     *
     * @param orgIds 组织ID列表
     * @return 去重后的组织ID列表
     */
    List<Long> deduplicateOrgIdsByPath(List<Long> orgIds);
    
    /**
     * 查询组织树列表（不分页）
     *
     * @param reqVO 查询条件
     * @return 组织树列表
     */
    List<OrgRespVO> getOrgTree(@Valid ManageOrgListReqVO reqVO);
    
    /**
     * 过滤末级组织ID列表
     * 从传入的组织ID列表中筛选出IS_LAST_LVL字段为'Y'的末级节点组织ID
     *
     * @param orgIds 组织ID列表
     * @return 末级节点组织ID列表
     */
    List<Long> filterLastLevelOrgIds(List<Long> orgIds);


    void syncManageOrgToBusiness();

}