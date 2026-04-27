package com.jasolar.mis.module.system.service.admin.org.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.jasolar.mis.framework.common.pojo.PageParam;
import com.jasolar.mis.framework.common.pojo.PageResult;
import com.jasolar.mis.framework.common.security.LoginUser;
import com.jasolar.mis.framework.web.core.util.WebFrameworkUtils;
import com.jasolar.mis.module.system.api.org.vo.OrgRespVO;
import com.jasolar.mis.module.system.controller.admin.dict.vo.DictEditVo;
import com.jasolar.mis.module.system.controller.admin.dict.vo.DictInfoByCodeVo;
import com.jasolar.mis.module.system.controller.admin.dict.vo.DictLabelVo;
import com.jasolar.mis.module.system.controller.admin.org.vo.ManageOrgListReqVO;
import com.jasolar.mis.module.system.controller.admin.org.vo.OrgBindUserGroupReqVO;
import com.jasolar.mis.module.system.controller.admin.org.vo.OrgPageReqVO;
import com.jasolar.mis.module.system.enums.ImportModeEnum;
import com.jasolar.mis.module.system.domain.admin.org.SystemManageOrgDO;
import com.jasolar.mis.module.system.domain.admin.org.UserGroupOrganizationDO;
import com.jasolar.mis.module.system.domain.admin.user.SystemUserDo;
import com.jasolar.mis.module.system.domain.admin.usergroup.SystemUserGroupRDo;
import com.jasolar.mis.module.system.domain.ehr.ManageOrgView;
import com.jasolar.mis.module.system.mapper.admin.org.MangeOrgViewMapper;
import com.jasolar.mis.module.system.mapper.admin.org.SystemManageOrgMapper;
import com.jasolar.mis.module.system.mapper.admin.org.UserGroupOrganizationMapper;
import com.jasolar.mis.module.system.mapper.admin.user.SystemUserMapper;
import com.jasolar.mis.module.system.mapper.admin.usergroup.SystemUserGroupRMapper;
import com.jasolar.mis.module.system.resp.ManageOrgExceptResp;
import com.jasolar.mis.module.system.service.admin.dict.SystemDictService;
import com.jasolar.mis.module.system.service.admin.org.SystemManageOrgService;
import com.jasolar.mis.module.system.util.TreeConvertUtil;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.validation.annotation.Validated;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 组织 Service 实现类
 *
 * @author jasolar
 */
@Service
@Validated
@Slf4j
public class SystemManageOrgServiceImpl implements SystemManageOrgService {

    @Resource
    private SystemManageOrgMapper organizationMapper;
    
    @Resource
    private UserGroupOrganizationMapper userGroupOrganizationMapper;
    
    @Resource
    private SystemUserGroupRMapper systemUserGroupRMapper;
    
    @Resource
    private SystemUserMapper systemUserMapper;
    
    @Resource
    private SystemDictService systemDictService;
    
    @Resource
    private MangeOrgViewMapper mangeOrgViewMapper;

    @Autowired(required = false)
    private ThreadPoolTaskExecutor taskExecutor;





    @Override
    public PageResult<OrgRespVO> getOrgPage(OrgPageReqVO reqVO) {
        log.info("开始查询组织数据，请求参数：{}", reqVO);
        
        // 1. 查询字典数据，用于组装typeDes
        List<String> types = List.of("orgType");
        DictInfoByCodeVo info = DictInfoByCodeVo.builder()
                .codes(types)
                .build();
        Map<String, DictEditVo> dictMap = systemDictService.getByCode(info);
        log.info("查询字典数据完成，字典类型：{}", types);
        
        // 2. 查询所有组织数据（根据type过滤）
        com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<SystemManageOrgDO> queryWrapper =
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<SystemManageOrgDO>();
        
        List<SystemManageOrgDO> allOrgs = organizationMapper.selectList(queryWrapper);
        log.info("查询所有组织数据，数量：{}", allOrgs.size());
        
        if (allOrgs.isEmpty()) {
            log.warn("未找到组织数据，返回空结果");
            return new PageResult<>(new ArrayList<>(), 0L);
        }
        
        // 3. 构建组织Map，方便查找
        Map<String, SystemManageOrgDO> allOrgsMap = allOrgs.stream()
            .collect(Collectors.toMap(SystemManageOrgDO::getCode, org -> org));
        
        // 4. 找到根组织（层级2的组织）
        List<SystemManageOrgDO> rootOrgs = allOrgs.stream()
            .collect(Collectors.toList());
        
        // 5. 为每个根组织构建树形结构
        List<OrgRespVO> result = rootOrgs.stream()
            .map(root -> buildTreeRecursively(root, allOrgsMap, dictMap))
            .collect(Collectors.toList());
        
        log.info("组织查询完成，返回结果数量：{}", result.size());
        return new PageResult<>(result, (long) result.size());
    }
    
    @Override
    public PageResult<OrgRespVO> searchOrgPage(OrgPageReqVO reqVO) {
        log.info("开始搜索组织数据，请求参数：{}", reqVO);
        
        // 步骤0：查询字典数据，用于组装typeDes
        List<String> types = List.of("orgType");
        DictInfoByCodeVo info = DictInfoByCodeVo.builder()
                .codes(types)
                .build();
        Map<String, DictEditVo> dictMap = systemDictService.getByCode(info);
        log.info("查询字典数据完成，字典类型：{}", types);
        
        // 步骤1：查询符合条件的组织数据
        List<SystemManageOrgDO> matchedOrgs = findMatchedOrganizations(reqVO);
        log.info("找到符合条件的组织数量：{}", matchedOrgs.size());
        
        if (matchedOrgs.isEmpty()) {
            log.info("未找到符合条件的组织，返回空结果");
            return new PageResult<>(new ArrayList<>(), 0L);
        }
        
        // 步骤2：整合path，构建树形结构编码
        Set<String> requiredCodes = extractRequiredCodes(matchedOrgs);
        log.info("需要查询的组织编码集合：{}", requiredCodes);
        
        // 步骤3：批量查询组织数据
        List<SystemManageOrgDO> allOrgs = findOrganizationsByCodes(requiredCodes);
        log.info("批量查询到的组织数量：{}", allOrgs.size());
        
        // 步骤4：对照树形结构组装OrgRespVO，满足sortOrder排序
        List<OrgRespVO> result = assembleTree(allOrgs, dictMap);
        log.info("搜索组织查询完成，返回结果数量：{}", result.size());
        
        return new PageResult<>(result, (long) result.size());
    }

    /**
     * 查询层级1组织（集团公司，只有1条）
     */
    private SystemManageOrgDO getLevel1Organization() {
        // 先查询所有数据，看看表里有什么
        List<SystemManageOrgDO> allOrgs = organizationMapper.selectList(null);
        log.info("表中所有组织数据数量：{}", allOrgs.size());
        if (!allOrgs.isEmpty()) {
            log.info("第一条数据详情：{}", allOrgs.get(0));
        }
        
        // 查询层级1的组织，按sortOrder排序，取第一条
        List<SystemManageOrgDO> list = organizationMapper.selectList(
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<SystemManageOrgDO>()
        );
        log.info("查询层级1组织，SQL结果数量：{}", list.size());
        if (!list.isEmpty()) {
            log.info("层级1组织详情：{}", list.get(0));
        }
        
        // 尝试不同的查询方式
        List<SystemManageOrgDO> list2 = organizationMapper.selectList(
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<SystemManageOrgDO>()
                .eq(SystemManageOrgDO::getDeleted, 0)
        );
        log.info("查询层级1组织（包含deleted条件），SQL结果数量：{}", list2.size());
        
        return list.isEmpty() ? null : list.get(0);
    }

    /**
     * 查询层级2组织（分页查询）
     */
    private List<SystemManageOrgDO> getLevel2Organizations(OrgPageReqVO reqVO) {
        // 构建查询条件
        com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<SystemManageOrgDO> queryWrapper =
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<SystemManageOrgDO>();
        
        // 使用MyBatis-Plus的分页功能，它会自动处理Oracle和MySQL的差异
        com.baomidou.mybatisplus.extension.plugins.pagination.Page<SystemManageOrgDO> page =
            new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(reqVO.getPageNo(), reqVO.getPageSize());
        
        return organizationMapper.selectPage(page, queryWrapper).getRecords();
    }

    /**
     * 查询层级2组织总数
     */
    private long getLevel2OrganizationCount(OrgPageReqVO reqVO) {
        return organizationMapper.selectCount(
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<SystemManageOrgDO>()
        );
    }

    /**
     * 查询指定组织的直接子组织
     */
    private List<SystemManageOrgDO> getChildrenOrganizations(String parentCode) {
        if (parentCode == null || parentCode.trim().isEmpty()) {
            return new ArrayList<>();
        }
        
        // 查询直接子组织
        return organizationMapper.selectList(
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<SystemManageOrgDO>()
        );
    }
    
    /**
     * 递归构建子组织的树形结构
     */
    private List<OrgRespVO> buildChildrenTree(String parentCode, Map<String, SystemManageOrgDO> allOrgsMap, Map<String, DictEditVo> dictMap) {
        if (parentCode == null || parentCode.trim().isEmpty()) {
            return new ArrayList<>();
        }
        
        // 查询直接子组织
        List<SystemManageOrgDO> children = getChildrenOrganizations(parentCode);
        if (children.isEmpty()) {
            return new ArrayList<>();
        }
        
        // 将子组织添加到allOrgsMap中
        children.forEach(child -> allOrgsMap.put(child.getCode(), child));
        
        // 转换为VO列表
        List<OrgRespVO> childrenVO = convertToRespVOList(children, dictMap);
        
        // 设置父级组织名称
        setParentNames(childrenVO, allOrgsMap);
        
        // 递归构建每个子组织的子组织
        for (OrgRespVO childVO : childrenVO) {
            List<OrgRespVO> grandChildrenVO = buildChildrenTree(childVO.getCode(), allOrgsMap, dictMap);
            childVO.setChildren(grandChildrenVO);
        }
        
        return childrenVO;
    }
    
    /**
     * 步骤1：查询符合条件的组织数据
     */
    private List<SystemManageOrgDO> findMatchedOrganizations(OrgPageReqVO reqVO) {
        // 根据搜索条件找到所有匹配的组织
        List<SystemManageOrgDO> matchedOrgs = organizationMapper.selectList(
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<SystemManageOrgDO>()
                .like(reqVO.getName() != null && !reqVO.getName().trim().isEmpty(), 
                      SystemManageOrgDO::getName, reqVO.getName())
                .like(reqVO.getCode() != null && !reqVO.getCode().trim().isEmpty(), 
                      SystemManageOrgDO::getCode, reqVO.getCode())
        );
        
        log.info("根据搜索条件找到匹配的组织数量：{}", matchedOrgs.size());
        return matchedOrgs;
    }
    
    /**
     * 步骤2：整合path，构建树形结构编码
     * 从所有匹配组织的path中提取树形结构
     * 例如：
     * 组织A: path="/ORG001/ORG002/ORG003/" -> 需要包含编码: ORG001,ORG002,ORG003
     * 组织B: path="/ORG001/ORG002/ORG004/" -> 需要包含编码: ORG001,ORG002,ORG004
     * 组织C: path="/ORG001/ORG005/ORG006/" -> 需要包含编码: ORG001,ORG005,ORG006
     * 
     * 最终需要的编码集合: {ORG001,ORG002,ORG003,ORG004,ORG005,ORG006}
     */
    private Set<String> extractRequiredCodes(List<SystemManageOrgDO> matchedOrgs) {
        Set<String> requiredCodes = new HashSet<>();
        

        
        log.info("从path中提取的编码集合：{}", requiredCodes);
        return requiredCodes;
    }
    
    /**
     * 解析path为编码列表
     * 例如："/ORG001/ORG002/ORG003/" -> ["ORG001", "ORG002", "ORG003"]
     */
    private List<String> parsePathToCodes(String path) {
        if (path == null || path.trim().isEmpty()) {
            return new ArrayList<>();
        }
        
        return Arrays.stream(path.split("/"))
            .filter(s -> !s.isEmpty())
            .collect(Collectors.toList());
    }
    
    /**
     * 步骤3：批量查询组织数据
     */
    private List<SystemManageOrgDO> findOrganizationsByCodes(Set<String> requiredCodes) {
        if (requiredCodes.isEmpty()) {
            return new ArrayList<>();
        }
        
        // 根据编码集合批量查询组织数据
        List<SystemManageOrgDO> allOrgs = organizationMapper.selectList(
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<SystemManageOrgDO>()
                .in(SystemManageOrgDO::getCode, requiredCodes)
        );
        
        log.info("根据编码集合批量查询到的组织数量：{}", allOrgs.size());
        return allOrgs;
    }
    
    /**
     * 步骤4：对照树形结构组装OrgRespVO，满足sortOrder排序
     */
    private List<OrgRespVO> assembleTree(List<SystemManageOrgDO> allOrgs, Map<String, DictEditVo> dictMap) {
        if (allOrgs.isEmpty()) {
            return new ArrayList<>();
        }
        
        // 将组织数据转换为Map，方便查找
        Map<String, SystemManageOrgDO> orgMap = allOrgs.stream()
            .collect(Collectors.toMap(SystemManageOrgDO::getCode, org -> org));
        
        // 找到根组织（层级2的组织）
        List<SystemManageOrgDO> rootOrgs = allOrgs.stream()
            .collect(Collectors.toList());
        
        // 为每个根组织构建树
        List<OrgRespVO> rootTrees = rootOrgs.stream()
            .map(root -> buildTreeRecursively(root, orgMap, dictMap))
            .collect(Collectors.toList());
        
        return rootTrees;
    }
    
    /**
     * 递归构建树状结构
     */
    private OrgRespVO buildTreeRecursively(SystemManageOrgDO currentOrg, Map<String, SystemManageOrgDO> orgMap, Map<String, DictEditVo> dictMap) {
        OrgRespVO currentVO = convertToRespVO(currentOrg);
        
        // 设置父级组织名称
        if (currentOrg.getPCode() != null && !currentOrg.getPCode().trim().isEmpty()) {
            // 通过code直接查找父组织
            SystemManageOrgDO parentOrg = orgMap.get(currentOrg.getCode());
            currentVO.setPName(parentOrg != null ? parentOrg.getName() : null);
        }
        
        // 查找子组织
        List<SystemManageOrgDO> children = orgMap.values().stream()
            .filter(org -> Objects.equals(org.getPCode(), currentOrg.getCode()))
            .collect(Collectors.toList());
        
        // 递归构建子组织
        List<OrgRespVO> childrenVO = children.stream()
            .map(child -> buildTreeRecursively(child, orgMap, dictMap))
            .collect(Collectors.toList());
        
        currentVO.setChildren(childrenVO);
        return currentVO;
    }
    
    
    

    /**
     * 转换为响应VO
     */
    private OrgRespVO convertToRespVO(SystemManageOrgDO organization) {
        if (organization == null) {
            return null;
        }
        OrgRespVO respVO = new OrgRespVO();
        respVO.setId(organization.getId());
        respVO.setName(organization.getName());
        respVO.setCode(organization.getCode());
        respVO.setPCode(organization.getPCode());
        respVO.setPName(organization.getPName());
        respVO.setOrgType(organization.getOrgType());
        respVO.setIsApprovalLastLvl(organization.getIsApprovalLastLvl());
        respVO.setScriptType(organization.getScriptType());
        respVO.setCreateTime(organization.getCreateTime());
        respVO.setUpdateTime(organization.getUpdateTime());
        respVO.setCreator(organization.getCreator());
        respVO.setUpdater(organization.getUpdater());
        return respVO;
    }

    /**
     * 转换为响应VO列表
     */
    private List<OrgRespVO> convertToRespVOList(List<SystemManageOrgDO> organizations, Map<String, DictEditVo> dictMap) {
        if (CollUtil.isEmpty(organizations)) {
            return new ArrayList<>();
        }
        
        return organizations.stream()
                .map(org -> convertToRespVO(org))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }
    
    /**
     * 根据字典Map获取字段标签
     */
    private String getFieldLabel(Map<String, DictEditVo> map, String key, String fieldKey) {
        DictEditVo dictEditVo = map.get(key);
        if (Objects.isNull(dictEditVo)) {
            return null;
        }
        List<DictLabelVo> labelList = dictEditVo.getLabelList();
        for (DictLabelVo dictLabelVo : labelList) {
            if (Objects.equals(fieldKey, dictLabelVo.getFieldKey())) {
                return dictLabelVo.getFieldLabel();
            }
        }
        return null;
    }
    
    /**
     * 设置父级组织名称（使用已有的组织数据，避免重复查询数据库）
     */
    private void setParentNames(List<OrgRespVO> orgList, Map<String, SystemManageOrgDO> orgMap) {
        if (CollUtil.isEmpty(orgList) || orgMap == null) {
            return;
        }

    }
    
    @Override
    public void bindUserGroups(OrgBindUserGroupReqVO reqVO) {
        log.info("开始绑定组织用户组，请求参数：{}", reqVO);
        
        List<Long> organizationIds = reqVO.getOrganizationIds();
        List<Long> userGroupIds = reqVO.getUserGroupIds();
        ImportModeEnum importMode = reqVO.getImportMode();
        
        // 如果未指定导入模式或为覆盖模式，使用原有逻辑（先删除再插入）
        if (importMode == null || importMode == ImportModeEnum.OVERWRITE) {
            // 先删除已存在的绑定关系
            userGroupOrganizationMapper.delete(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<UserGroupOrganizationDO>()
                    .in(UserGroupOrganizationDO::getOrganizationId, organizationIds)
            );
            
            // 批量插入新的绑定关系
            List<UserGroupOrganizationDO> bindList = new ArrayList<>();
            for (Long organizationId : organizationIds) {
                for (Long userGroupId : userGroupIds) {
                    UserGroupOrganizationDO bind = UserGroupOrganizationDO.builder()
                        .organizationId(organizationId)
                        .userGroupId(userGroupId)
                        .build();
                    bindList.add(bind);
                }
            }
            
            if (!bindList.isEmpty()) {
                userGroupOrganizationMapper.insertBatch(bindList);
            }
            
            log.info("组织用户组绑定完成（覆盖模式），共绑定 {} 个关系", bindList.size());
        } else if (importMode == ImportModeEnum.APPEND) {
            // 追加模式：只插入不存在的记录
            List<UserGroupOrganizationDO> bindList = new ArrayList<>();
            for (Long organizationId : organizationIds) {
                for (Long userGroupId : userGroupIds) {
                    // 查询关系是否已存在
                    UserGroupOrganizationDO existingRelation = userGroupOrganizationMapper.selectOne(
                        new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<UserGroupOrganizationDO>()
                            .eq(UserGroupOrganizationDO::getOrganizationId, organizationId)
                            .eq(UserGroupOrganizationDO::getUserGroupId, userGroupId)
                            .eq(UserGroupOrganizationDO::getDeleted, false)
                    );
                    
                    // 如果不存在，则添加到插入列表
                    if (existingRelation == null) {
                        UserGroupOrganizationDO bind = UserGroupOrganizationDO.builder()
                            .organizationId(organizationId)
                            .userGroupId(userGroupId)
                            .build();
                        bindList.add(bind);
                    }
                }
            }
            
            // 批量插入不存在的记录
            if (!bindList.isEmpty()) {
                userGroupOrganizationMapper.insertBatch(bindList);
                log.info("组织用户组绑定完成（追加模式），共新增 {} 个关系", bindList.size());
            } else {
                log.info("组织用户组绑定完成（追加模式），所有关系已存在，无需新增");
            }
        }
    }
    
    @Override
    public List<Long> deduplicateOrgIdsByPath(List<Long> orgIds) {
        if (CollUtil.isEmpty(orgIds)) {
            return new ArrayList<>();
        }
        
        log.info("开始去重组织ID列表，原始列表：{}", orgIds);
        
        // 1. 根据ID列表查询组织信息
        List<SystemManageOrgDO> organizations = organizationMapper.selectBatchIds(orgIds);
        if (CollUtil.isEmpty(organizations)) {
            log.warn("未找到对应的组织信息，返回空列表");
            return new ArrayList<>();
        }
        

        
        // 3. 去重逻辑：如果某个组织的路径是其他组织路径的前缀，则只保留父级
        List<Long> result = new ArrayList<>();

        
        log.info("组织ID去重完成，原始数量：{}，去重后数量：{}，结果：{}", 
                orgIds.size(), result.size(), result);
        
        return result;
    }

    @Override
    public List<SystemManageOrgDO> getOrgList(ManageOrgListReqVO reqVO) {
        return findMatchedOrganizationsForList(reqVO);
    }
    
    /**
     * 按type查询组织列表（无搜索条件）
     */
    private List<OrgRespVO> getOrgListByType(ManageOrgListReqVO reqVO) {

        // 1. 查询字典数据，用于组装typeDes
        List<String> types = List.of("orgType");
        DictInfoByCodeVo info = DictInfoByCodeVo.builder()
                .codes(types)
                .build();
        Map<String, DictEditVo> dictMap = systemDictService.getByCode(info);
        log.info("查询字典数据完成，字典类型：{}", types);
        
        // 2. 查询所有组织数据（根据type过滤，排除code为INTERNAL的父节点）
        com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<SystemManageOrgDO> queryWrapper =
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<SystemManageOrgDO>();

        List<SystemManageOrgDO> allOrgs = organizationMapper.selectList(queryWrapper);
        log.info("查询所有组织数据，数量：{}", allOrgs.size());
        
        if (allOrgs.isEmpty()) {
            log.warn("未找到组织数据，返回空结果");
            return new ArrayList<>();
        }
        
        // 3. 转换为VO并组装typeDes
        List<OrgRespVO> result = allOrgs.stream()
                .map(this::convertToRespVO)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        
        log.info("组织列表查询完成，返回数量：{}", result.size());
        return result;
    }
    
    /**
     * 搜索组织列表（有搜索条件）
     */
    private List<OrgRespVO> searchOrgList(ManageOrgListReqVO reqVO) {

        // 步骤1：查询符合条件的组织数据
        List<SystemManageOrgDO> systemManageOrgDOS = findMatchedOrganizationsForList(reqVO);
        if(systemManageOrgDOS.isEmpty()){
            return Collections.emptyList();
        }
        List<OrgRespVO> orgRespVOS = systemManageOrgDOS.stream().map(
                this::convertToRespVO
        ).toList();
        return TreeConvertUtil.convertToTree(
                orgRespVOS,
                OrgRespVO::getCode,
                OrgRespVO::getPCode,
                OrgRespVO::getChildren,
                OrgRespVO::setChildren
        );
    }
    
    /**
     * 查找符合条件的组织数据（用于列表查询）
     */
    private List<SystemManageOrgDO> findMatchedOrganizationsForList(ManageOrgListReqVO reqVO) {
        LambdaQueryWrapper<SystemManageOrgDO> queryWrapper =
            new LambdaQueryWrapper<SystemManageOrgDO>(); // 排除code为INTERNAL的父节点
        
        // 添加name和code的模糊查询条件
        if (reqVO.getName() != null && !reqVO.getName().trim().isEmpty()) {
            queryWrapper.like(SystemManageOrgDO::getName, reqVO.getName().trim());
        }
        if (reqVO.getCode() != null && !reqVO.getCode().trim().isEmpty()) {
            queryWrapper.like(SystemManageOrgDO::getCode, reqVO.getCode().trim());
        }
        
        return organizationMapper.selectList(queryWrapper);
    }
    
    /**
     * 查找符合条件的组织数据（用于树形查询，支持权限过滤）
     */
    private List<SystemManageOrgDO> findMatchedOrganizationsForTree(ManageOrgListReqVO reqVO, List<Long> authorizedOrgIds) {
        com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<SystemManageOrgDO> queryWrapper =
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<SystemManageOrgDO>();

        // 添加权限过滤条件：如果用户有权限限制，则只查询有权限的组织
        if (authorizedOrgIds != null && !authorizedOrgIds.isEmpty()) {
            queryWrapper.in(SystemManageOrgDO::getId, authorizedOrgIds);
            log.info("应用权限过滤，限制组织ID范围：{}", authorizedOrgIds);
        }
        
        // 添加name和code的模糊查询条件
        if (reqVO.getName() != null && !reqVO.getName().trim().isEmpty()) {
            queryWrapper.like(SystemManageOrgDO::getName, reqVO.getName().trim());
        }
        if (reqVO.getCode() != null && !reqVO.getCode().trim().isEmpty()) {
            queryWrapper.like(SystemManageOrgDO::getCode, reqVO.getCode().trim());
        }
        
        return organizationMapper.selectList(queryWrapper);
    }
    
    
    @Override
    public List<OrgRespVO> getOrgTree(ManageOrgListReqVO reqVO) {
        log.info("开始查询组织树列表，请求参数：{}", reqVO);
        
        // 统一使用searchOrgTree方法，该方法已包含完整的权限过滤和树形结构构建逻辑
        return searchOrgTree(reqVO);
    }
    
    /**
     * 搜索组织树列表（有搜索条件）
     */
    private List<OrgRespVO> searchOrgTree(ManageOrgListReqVO reqVO) {
        log.info("搜索组织树列表，搜索条件：name={}, code={}", reqVO.getName(), reqVO.getCode());
        
        // 获取当前登录用户信息
        String username = null;
        try {
            LoginUser loginUser = WebFrameworkUtils.getLoginUser();
            username = loginUser.getNo();
            log.info("当前登录用户：{}", username);
        } catch (Exception e) {
            log.warn("获取当前用户信息失败：{}", e.getMessage());
        }
        
        // 根据username查询用户有权限的组织ID集合
        List<Long> authorizedOrgIds = new ArrayList<>();
        if (username != null) {
            try {
                // 1. 根据username查询SYSTEM_USER_GROUP_R表，获取type为3（组织类型）的GROUP_ID列表
                List<Long> groupIds = getUserGroupIdsByUsername(username);
                log.info("用户{}的组织类型用户组ID列表：{}", username, groupIds);
                
                if (!groupIds.isEmpty()) {
                    // 2. 根据GROUP_ID列表查询SYSTEM_USER_GROUP_ORGANIZATION_R表，获取ORGANIZATION_ID集合
                    List<Long> orgIds = getOrganizationIdsByGroupIds(groupIds);
                    log.info("用户{}有权限的组织ID列表：{}", username, orgIds);
                    
                    // 3. 去重
                    authorizedOrgIds = orgIds.stream()
                            .distinct()
                            .collect(Collectors.toList());
                    log.info("去重后的组织ID列表：{}", authorizedOrgIds);
                }
            } catch (Exception e) {
                log.error("查询用户权限组织失败：{}", e.getMessage(), e);
            }
        }
        
        // 步骤1：查询字典数据，用于组装typeDes
        List<String> types = List.of("orgType");
        DictInfoByCodeVo info = DictInfoByCodeVo.builder()
                .codes(types)
                .build();
        Map<String, DictEditVo> dictMap = systemDictService.getByCode(info);
        log.info("查询字典数据完成，字典类型：{}", types);
        
        // 步骤2：查询符合条件的组织数据，使用authorizedOrgIds进行过滤
        List<SystemManageOrgDO> matchedOrgs = findMatchedOrganizationsForTree(reqVO, authorizedOrgIds);
        log.info("找到符合条件的组织数量：{}", matchedOrgs.size());
        
        if (matchedOrgs.isEmpty()) {
            log.info("未找到符合条件的组织，返回空结果");
            return new ArrayList<>();
        }
        
        // 步骤3：整合path，构建树形结构编码
        Set<String> requiredCodes = extractRequiredCodes(matchedOrgs);
        log.info("需要查询的组织编码集合：{}", requiredCodes);
        
        // 步骤4：批量查询组织数据
        List<SystemManageOrgDO> allOrgs = findOrganizationsByCodes(requiredCodes);
        log.info("批量查询到的组织数量：{}", allOrgs.size());
        
        // 步骤5：再次过滤掉code为INTERNAL的父节点数据
        List<SystemManageOrgDO> filteredOrgs = allOrgs.stream()
                .filter(org -> !"INTERNAL".equals(org.getCode()))
                .collect(Collectors.toList());
        log.info("过滤掉INTERNAL父节点后的组织数量：{}", filteredOrgs.size());
        
        // 步骤6：调用buildTreeRecursively方法进行递归组装
        List<OrgRespVO> result = assembleTree(filteredOrgs, dictMap);
        log.info("组织树搜索完成，返回结果数量：{}", result.size());
        
        return result;
    }
    
    
    /**
     * 根据username查询SYSTEM_USER_GROUP_R表，获取type为3（组织类型）的GROUP_ID列表
     */
    private List<Long> getUserGroupIdsByUsername(String username) {
        log.info("查询用户{}的组织类型用户组", username);
        
        try {
            // 1. 根据username获取userId
            SystemUserDo user = systemUserMapper.getByUserName(username);
            if (user == null) {
                log.warn("未找到用户：{}", username);
                return new ArrayList<>();
            }
            
            Long userId = user.getId();
            log.info("用户{}的ID：{}", username, userId);
            
           // 2. 根据userId和type=3、4查询SYSTEM_USER_GROUP_R表，获取GROUP_ID列表
           com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<SystemUserGroupRDo> queryWrapper =
               new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<SystemUserGroupRDo>()
                   .eq(SystemUserGroupRDo::getUserId, userId)
                   .in(SystemUserGroupRDo::getType, List.of("3","4")); // 组织类型
            
            List<SystemUserGroupRDo> userGroupRList = systemUserGroupRMapper.selectList(queryWrapper);
            log.info("查询到用户{}的组织类型用户组关系数量：{}", username, userGroupRList.size());
            
            // 3. 提取GROUP_ID列表
            List<Long> groupIds = userGroupRList.stream()
                    .map(SystemUserGroupRDo::getGroupId)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
            
            log.info("查询到用户{}的组织类型用户组ID数量：{}", username, groupIds.size());
            return groupIds;
            
        } catch (Exception e) {
            log.error("查询用户{}的组织类型用户组失败：{}", username, e.getMessage(), e);
            return new ArrayList<>();
        }
    }
    
    /**
     * 根据GROUP_ID列表查询SYSTEM_USER_GROUP_ORGANIZATION_R表，获取ORGANIZATION_ID集合
     */
    private List<Long> getOrganizationIdsByGroupIds(List<Long> groupIds) {
        log.info("根据用户组ID列表查询组织ID，用户组ID数量：{}", groupIds.size());
        
        if (groupIds.isEmpty()) {
            return new ArrayList<>();
        }
        
        try {
           // 根据GROUP_ID列表查询SYSTEM_USER_GROUP_ORGANIZATION_R表
           com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<UserGroupOrganizationDO> queryWrapper = 
               new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<UserGroupOrganizationDO>()
                   .in(UserGroupOrganizationDO::getUserGroupId, groupIds);
            
            List<UserGroupOrganizationDO> userGroupOrgList = userGroupOrganizationMapper.selectList(queryWrapper);
            log.info("查询到用户组组织关系数量：{}", userGroupOrgList.size());
            
            // 提取ORGANIZATION_ID列表
            List<Long> orgIds = userGroupOrgList.stream()
                    .map(UserGroupOrganizationDO::getOrganizationId)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
            
            log.info("查询到组织ID数量：{}", orgIds.size());
            return orgIds;
            
        } catch (Exception e) {
            log.error("根据用户组ID列表查询组织ID失败：{}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    @Override
    public List<Long> filterLastLevelOrgIds(List<Long> orgIds) {
        log.info("开始过滤末级组织ID，输入组织ID数量：{}", orgIds != null ? orgIds.size() : 0);
        
        if (CollUtil.isEmpty(orgIds)) {
            log.warn("输入的组织ID列表为空，返回空结果");
            return new ArrayList<>();
        }
        
        try {
            // 查询这些组织ID对应的记录，筛选出IS_LAST_LVL为'Y'的末级节点
            com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<SystemManageOrgDO> queryWrapper =
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<SystemManageOrgDO>()
                    .in(SystemManageOrgDO::getId, orgIds)
                    .eq(SystemManageOrgDO::getIsLastLvl, true);
            
            List<SystemManageOrgDO> lastLevelOrgs = organizationMapper.selectList(queryWrapper);
            log.info("查询到末级组织数量：{}", lastLevelOrgs.size());
            
            // 提取末级组织的ID列表
            List<Long> lastLevelOrgIds = lastLevelOrgs.stream()
                    .map(SystemManageOrgDO::getId)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
            
            log.info("过滤末级组织ID完成，返回数量：{}", lastLevelOrgIds.size());
            return lastLevelOrgIds;
            
        } catch (Exception e) {
            log.error("过滤末级组织ID失败：{}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }


    @Override
    public void syncManageOrgToBusiness() {
        Calendar calendar = Calendar.getInstance();
        String year = String.valueOf(calendar.get(Calendar.YEAR)) ;
        Long count = organizationMapper.selectCountByYear();
        if(count == 0L){
            log.info("同步组织初始化开始....");
            init(year);
            return;
        }
        List<ManageOrgExceptResp> exceptData = organizationMapper.getExceptData();
        if(CollectionUtils.isEmpty(exceptData)){
            log.info("组织数据无差异，结束....");
            return;
        }

        List<ManageOrgExceptResp> addOrUpdateList = new ArrayList<>(exceptData.stream().filter(x -> x.getToAdd().equals("1")).toList());

        Map<Long, ManageOrgExceptResp> updateOrDelMap = exceptData.stream().filter(x -> x.getToAdd().equals("2"))
                .collect(Collectors.toMap(ManageOrgExceptResp::getId, x -> x));

        // 获取需要更新的数据
        List<ManageOrgExceptResp> updateList = new LinkedList<>();
        Iterator<ManageOrgExceptResp> iterator = addOrUpdateList.iterator();
        while (iterator.hasNext()) {
            ManageOrgExceptResp next = iterator.next();
            if (updateOrDelMap.containsKey(next.getId())) {
                updateList.add(next);
                iterator.remove();
                updateOrDelMap.remove(next.getId());
            }
        }

        if(!CollectionUtils.isEmpty(addOrUpdateList)){
            List<SystemManageOrgDO> addList = addOrUpdateList.stream().map(x -> {
                SystemManageOrgDO build = SystemManageOrgDO.builder()
                        .id(x.getId())
                        .name(x.getName())
                        .code(x.getCode())
                        .pCode(x.getPCode())
                        .pName(x.getPName())
                        .isLastLvl(x.getIsLastLvl())
                        .build();
                return build;
            }).toList();
            organizationMapper.insertBatch(addList);
            
            // 异步处理新增组织的用户组关系继承
            if (taskExecutor != null) {
                taskExecutor.execute(() -> {
                    try {
                        inheritUserGroupRelationsFromParents(addOrUpdateList);
                    } catch (Exception e) {
                        log.error("异步处理新增组织的用户组关系继承失败", e);
                    }
                });
            } else {
                // 如果没有线程池，同步执行
                inheritUserGroupRelationsFromParents(addOrUpdateList);
            }
        }

        if(!CollectionUtils.isEmpty(updateList)){
            List<SystemManageOrgDO> uList = updateList.stream().map(x -> {
                SystemManageOrgDO build = SystemManageOrgDO.builder()
                        .id(x.getId())
                        .name(x.getName())
                        .code(x.getCode())
                        .pCode(x.getPCode())
                        .pName(x.getPName())
                        .isLastLvl(x.getIsLastLvl())
                        .build();
                return build;
            }).toList();
            organizationMapper.updateBatch(uList);
        }

        // 删除
//        if(!updateOrDelMap.isEmpty()){
//            List<Long> keyList = updateOrDelMap.keySet().stream().toList();
//            organizationMapper.deleteByIds(keyList);
//        }
    }
    
    private void init(String year) {
        // 每次查询200条记录，直到查询完所有数据
        int pageSize = 200;
        int currentPage = 1;
        // 循环查询直到没有更多数据
        while (true) {
            // 构建分页参数
            PageParam pageParam = new PageParam();
            pageParam.setPageNo(currentPage);
            pageParam.setPageSize(pageSize);
            
            // 使用mangeOrgViewMapper分页查询数据
            PageResult<ManageOrgView> pageResult = mangeOrgViewMapper.selectPage(pageParam, new QueryWrapper<>());
            
            // 处理查询到的数据
            List<ManageOrgView> manageOrgViews = pageResult.getList();
            
            // 如果没有更多数据，则退出循环
            if (manageOrgViews.isEmpty()) {
                break;
            }
            List<SystemManageOrgDO> list = manageOrgViews.stream().map(manageOrgView -> {
                SystemManageOrgDO systemManageOrgDO = SystemManageOrgDO.builder()
                        .id(manageOrgView.getId())
                        .name(manageOrgView.getName())
                        .code(manageOrgView.getCode())
                        .pCode(manageOrgView.getPCode())
                        .pName(manageOrgView.getPName())
                        .isLastLvl(manageOrgView.getIsLastLvl())
                        .build();
                return systemManageOrgDO;
            }).toList();
            organizationMapper.insertBatch(list);

            if( manageOrgViews.size() < pageSize){
                break;
            }

            // 移动到下一页
            currentPage++;
        }
    }

    /**
     * 继承父级组织的用户组关系
     * 对于新增的组织，追溯其所有父级组织，继承父级在 SYSTEM_USER_GROUP_ORGANIZATION_R 表中的用户组关系
     *
     * @param addOrUpdateList 新增的组织列表
     */
    private void inheritUserGroupRelationsFromParents(List<ManageOrgExceptResp> addOrUpdateList) {
        if (CollectionUtils.isEmpty(addOrUpdateList)) {
            log.info("新增组织列表为空，无需处理用户组关系继承");
            return;
        }

        log.info("开始处理新增组织的用户组关系继承，新增组织数量：{}", addOrUpdateList.size());

        // 构建组织编码到组织信息的映射，方便查找
        Map<String, ManageOrgExceptResp> newOrgCodeMap = addOrUpdateList.stream()
                .filter(org -> org.getCode() != null)
                .collect(Collectors.toMap(ManageOrgExceptResp::getCode, org -> org, (o1, o2) -> o1));

        // 查询所有已存在的组织数据，用于追溯父级
        List<SystemManageOrgDO> allOrgs = organizationMapper.selectList(
                new LambdaQueryWrapper<SystemManageOrgDO>().eq(SystemManageOrgDO::getDeleted, false)
        );
        Map<String, SystemManageOrgDO> existingOrgCodeMap = allOrgs.stream()
                .filter(org -> org.getCode() != null)
                .collect(Collectors.toMap(SystemManageOrgDO::getCode, org -> org, (o1, o2) -> o1));

        // 结果Map：key为组织ID，value为该组织应该继承的所有USER_GROUP_ID集合
        Map<Long, Set<Long>> orgIdToUserGroupIdsMap = new HashMap<>();

        // 遍历新增的组织，追溯其所有父级
        for (ManageOrgExceptResp newOrg : addOrUpdateList) {
            Long orgId = newOrg.getId();
            Set<Long> inheritedUserGroupIds = new HashSet<>();

            // 追溯所有父级组织
            String currentPCode = newOrg.getPCode();
            Set<String> visitedCodes = new HashSet<>(); // 防止循环引用

            while (currentPCode != null && !currentPCode.isEmpty() && !"0".equals(currentPCode)) {
                // 防止循环引用
                if (visitedCodes.contains(currentPCode)) {
                    log.warn("检测到循环引用，组织编码：{}", currentPCode);
                    break;
                }
                visitedCodes.add(currentPCode);

                // 优先在已存在的组织中查找父级
                SystemManageOrgDO parentOrg = existingOrgCodeMap.get(currentPCode);
                if (parentOrg != null) {
                    // 父级在已存在的组织中，查询其用户组关系
                    Long parentOrgId = parentOrg.getId();
                    List<Long> parentUserGroupIds = userGroupOrganizationMapper.getGroupIdsByOrgId(parentOrgId);
                    if (!CollectionUtils.isEmpty(parentUserGroupIds)) {
                        inheritedUserGroupIds.addAll(parentUserGroupIds);
                        log.debug("组织ID={}，从父级组织ID={}(编码={})继承用户组：{}", 
                                orgId, parentOrgId, currentPCode, parentUserGroupIds);
                    }
                    // 继续向上追溯
                    currentPCode = parentOrg.getPCode();
                } else {
                    // 如果父级不在已存在的组织中，检查是否在新增列表中
                    ManageOrgExceptResp parentInNewList = newOrgCodeMap.get(currentPCode);
                    if (parentInNewList != null) {
                        // 父级也在新增列表中，由于父级也是新增的，可能还没有用户组关系
                        // 但我们可以继续向上追溯父级的父级
                        currentPCode = parentInNewList.getPCode();
                    } else {
                        // 找不到父级，停止追溯
                        log.debug("组织ID={}，无法找到父级组织编码：{}", orgId, currentPCode);
                        break;
                    }
                }
            }

            // 如果有继承的用户组，放入Map
            if (!inheritedUserGroupIds.isEmpty()) {
                orgIdToUserGroupIdsMap.put(orgId, inheritedUserGroupIds);
                log.info("组织ID={}，继承的用户组ID集合：{}", orgId, inheritedUserGroupIds);
            } else {
                log.debug("组织ID={}，没有需要继承的用户组关系", orgId);
            }
        }

        // 批量插入用户组组织关系
        if (!orgIdToUserGroupIdsMap.isEmpty()) {
            List<UserGroupOrganizationDO> relationsToInsert = new ArrayList<>();
            for (Map.Entry<Long, Set<Long>> entry : orgIdToUserGroupIdsMap.entrySet()) {
                Long organizationId = entry.getKey();
                Set<Long> userGroupIds = entry.getValue();

                for (Long userGroupId : userGroupIds) {
                    // 检查关系是否已存在，避免重复插入
                    UserGroupOrganizationDO existingRelation = userGroupOrganizationMapper.selectOne(
                            new LambdaQueryWrapper<UserGroupOrganizationDO>()
                                    .eq(UserGroupOrganizationDO::getUserGroupId, userGroupId)
                                    .eq(UserGroupOrganizationDO::getOrganizationId, organizationId)
                                    .eq(UserGroupOrganizationDO::getDeleted, false)
                    );

                    if (existingRelation == null) {
                        UserGroupOrganizationDO relation = UserGroupOrganizationDO.builder()
                                .userGroupId(userGroupId)
                                .organizationId(organizationId)
                                .build();
                        relationsToInsert.add(relation);
                    }
                }
            }

            if (!relationsToInsert.isEmpty()) {
                userGroupOrganizationMapper.insertBatch(relationsToInsert);
                log.info("成功为新增组织继承用户组关系，插入关系数量：{}，涉及组织数量：{}", 
                        relationsToInsert.size(), orgIdToUserGroupIdsMap.size());
            } else {
                log.info("所有用户组关系已存在，无需插入");
            }
        } else {
            log.info("没有需要继承的用户组关系");
        }
    }
}