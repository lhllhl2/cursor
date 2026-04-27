package com.jasolar.mis.module.system.service.admin.usergroup;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.pinyin.PinyinUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jasolar.mis.framework.common.exception.ServiceException;
import com.jasolar.mis.framework.common.pojo.CommonResult;
import com.jasolar.mis.framework.common.pojo.PageResult;
import com.jasolar.mis.framework.common.pojo.PrimaryParam;
import com.jasolar.mis.module.system.controller.admin.dict.vo.DictEditVo;
import com.jasolar.mis.module.system.controller.admin.dict.vo.DictInfoByCodeVo;
import com.jasolar.mis.module.system.controller.admin.usergroup.resp.*;
import com.jasolar.mis.module.system.controller.admin.usergroup.vo.*;
import com.jasolar.mis.module.system.domain.admin.dict.SystemDictDo;
import com.jasolar.mis.module.system.domain.admin.dict.SystemDictLabelDo;
import com.jasolar.mis.module.system.domain.admin.org.SystemManageOrgDO;
import com.jasolar.mis.module.system.domain.admin.org.UserGroupOrganizationDO;
import com.jasolar.mis.module.system.domain.admin.usergroup.SystemUserGroupDo;
import com.jasolar.mis.module.system.domain.admin.usergroup.SystemUserGroupRDo;
import com.jasolar.mis.module.system.enums.UserGroupEnums;
import com.jasolar.mis.module.system.exceptioncode.UserGroupErrorCodeConstants;
import com.jasolar.mis.module.system.mapper.admin.dict.SystemDictLabelMapper;
import com.jasolar.mis.module.system.mapper.admin.dict.SystemDictMapper;
import com.jasolar.mis.module.system.mapper.admin.org.SystemManageOrgMapper;
import com.jasolar.mis.module.system.mapper.admin.org.UserGroupOrganizationMapper;
import com.jasolar.mis.module.system.mapper.admin.user.SystemUserMapper;
import com.jasolar.mis.module.system.mapper.admin.usergroup.SystemUserGroupRMapper;
import com.jasolar.mis.module.system.mapper.admin.usergroup.UserGroupMapper;
import com.jasolar.mis.module.system.service.admin.dict.SystemDictService;
import com.jasolar.mis.module.system.util.IPageToPageResultUtils;
import io.swagger.v3.oas.annotations.Operation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Description:
 * Author : Zhou Hai
 * Date : 23/07/2025 11:02
 * Version : 1.0
 */
@Service
public class UserGroupServiceImpl implements UserGroupService{

    private static final Logger log = LoggerFactory.getLogger(UserGroupServiceImpl.class);

    @Autowired
    private UserGroupMapper userGroupMapper;

    @Autowired
    private SystemUserGroupRMapper userGroupRMapper;

    @Autowired
    private UserGroupOrganizationMapper userGroupOrganizationMapper;

    @Autowired
    private SystemManageOrgMapper systemManageOrgMapper;

    @Autowired
    private SystemDictService systemDictService;
    @Autowired
    private SystemDictMapper systemDictMapper;
    @Autowired
    private SystemDictLabelMapper systemDictLabelMapper;
    @Autowired
    private SystemUserGroupRMapper systemUserGroupRMapper;




    @Autowired
    private SystemUserMapper systemUserMapper;


    @Override
    public PageResult<SearchListResp> getList(SearchListVo searchListVo) {
        IPage<SearchListResp> page = new Page<>(searchListVo.getPageNo(),searchListVo.getPageSize());
        IPage<SearchListResp>  listRespIPage = userGroupMapper.getListPage(page,searchListVo);
        PageResult<SearchListResp> pageResult = new PageResult<>();
        pageResult.setList(listRespIPage.getRecords());
        pageResult.setTotal(listRespIPage.getTotal());

        List<SearchListResp> rows = pageResult.getList();
        if(CollectionUtil.isNotEmpty(rows)){
            List<String> userGroupType = List.of("userGroupType");
            DictInfoByCodeVo ug = DictInfoByCodeVo.builder()
                    .codes(userGroupType)
                    .build();
            Map<String, DictEditVo> dictMap = systemDictService.getByCode(ug);
            for (SearchListResp row : rows) {
                String typeDes = systemDictService.getFieldLabel(dictMap, "userGroupType", row.getType());
                row.setTypeDes(typeDes);
            }
        }
        return pageResult;
    }

    @Override
    public List<SearchSimpleListResp> getSimpleList(SearchSimpleListVo searchSimpleListVo) {
        return userGroupMapper.getSimpleList(searchSimpleListVo);
    }

    @Override
    public void addUserGroup(UserGroupAddVo userGroupAddVo) {
        boolean exist = userGroupMapper.hasName(userGroupAddVo.getName());
        if(exist){
            throw new ServiceException(UserGroupErrorCodeConstants.NAME_REPLICATION);
        }
        String pinyin = PinyinUtil.getPinyin(userGroupAddVo.getName(), StrUtil.EMPTY);
        String code = userGroupAddVo.getType() + "_" + pinyin;

        SystemUserGroupDo userGroupDo = SystemUserGroupDo.builder()
                .name(userGroupAddVo.getName())
                .type(userGroupAddVo.getType())
                .code(code)
                .remark(userGroupAddVo.getRemark())
                .build();
        userGroupMapper.insert(userGroupDo);


        if(!CollectionUtils.isEmpty(userGroupAddVo.getUserIds())){
            List<SystemUserGroupRDo> userGroupRDos = userGroupAddVo.getUserIds().stream()
                    .map(x -> {
                        SystemUserGroupRDo userGroupRDo = SystemUserGroupRDo.builder()
                                .userId(x)
                                .groupId(userGroupDo.getId())
                                .type(userGroupAddVo.getType())
                                .build();
                        return userGroupRDo;

                    }).collect(Collectors.toList());
            userGroupRMapper.insertBatch(userGroupRDos);
        }
    }


    @Override
    public void del(PrimaryParam primaryParam) {
        SystemUserGroupDo userGroupDo = userGroupMapper.selectById(primaryParam.getId());
        if(Objects.isNull(userGroupDo)){
            throw new ServiceException(UserGroupErrorCodeConstants.USER_GROUP_NOT_EXIST);
        }
        userGroupMapper.logicDelById(primaryParam.getId());

        userGroupRMapper.logicDelByGroupId(primaryParam.getId());

    }

    @Override
    public List<UserGroupRoleResp> searchRoleByUserGroup(SearchRoleByUserGroupVo searchRoleByUserGroupVo) {

        return userGroupMapper.searchRoleByUserGroup(searchRoleByUserGroupVo.getId(),
                UserGroupEnums.Type.MENU.getCode(),
                false);

    }


    @Override
    public PageResult<GroupUserResp> searchGroupUser(SearchPrimaryPage searchPrimaryPage) {
        IPage<GroupUserResp> page = new Page<>(searchPrimaryPage.getPageNo(),searchPrimaryPage.getPageSize());
        IPage<GroupUserResp> respIPage = userGroupMapper.searchGroupUser(page,searchPrimaryPage.getId());
        PageResult<GroupUserResp> pageResult = IPageToPageResultUtils.transfer(respIPage);
        List<GroupUserResp> list = pageResult.getList();
        if(!CollectionUtils.isEmpty(list)){
            for (GroupUserResp g : list) {
                g.setPath(g.getOfficeLocation() + "/" + g.getPost());
            }
        }
        return pageResult;
    }

    @Override
    public List<GroupUserResp> searchGroupList(PrimaryParam primaryParam) {
        return userGroupMapper.searchGroupUserList(primaryParam.getId());
    }

    @Override
    public void groupUserSave(GroupUserSaveVo groupUserSaveVo) {

        SystemUserGroupDo userGroupDo = userGroupMapper.selectById(groupUserSaveVo.getGroupId());
        if(Objects.isNull(userGroupDo)){
            throw new ServiceException(UserGroupErrorCodeConstants.USER_GROUP_NOT_EXIST);
        }
        List<SystemUserGroupRDo> userGroupRDos = userGroupRMapper.searchByGroupId(groupUserSaveVo.getGroupId());
        if(CollectionUtils.isEmpty(userGroupRDos)){
            if(!CollectionUtils.isEmpty(groupUserSaveVo.getUserIds())){
                List<SystemUserGroupRDo> list = groupUserSaveVo.getUserIds().stream()
                        .map(x -> {
                            SystemUserGroupRDo userGroupRDo = SystemUserGroupRDo.builder()
                                    .type(userGroupDo.getType())
                                    .userId(x)
                                    .groupId(groupUserSaveVo.getGroupId())
                                    .build();
                            return userGroupRDo;
                        }).toList();
                userGroupRMapper.insertBatch(list);
            }
            return;
        }

        List<Long> news = groupUserSaveVo.getUserIds();
        List<Long> olds = userGroupRDos
                .stream()
                .map(SystemUserGroupRDo::getUserId)
                .toList();

        List<Long> deletes  = CollectionUtil.subtractToList(olds, news);
        if(CollectionUtil.isNotEmpty(deletes)){
            userGroupRMapper.logicDelByGroupIdAndUserId(groupUserSaveVo.getGroupId(),deletes);
        }

        List<Long> adds = CollectionUtil.subtractToList(news, olds);
        if (CollectionUtil.isNotEmpty(adds)){
            List<SystemUserGroupRDo> addList = adds.stream().map(x -> {
                SystemUserGroupRDo userGroupRDo = SystemUserGroupRDo.builder()
                        .type(userGroupDo.getType())
                        .userId(x)
                        .groupId(groupUserSaveVo.getGroupId())
                        .build();
                return userGroupRDo;
            }).toList();
            userGroupRMapper.insertBatch(addList);
        }
    }

    @Override
    public List<Long> getGroupIdsByOrgId(Long orgId) {
        return userGroupOrganizationMapper.getGroupIdsByOrgId(orgId);
    }

    @Override
    public boolean existsByName(String name) {
        return userGroupMapper.hasName(name);
    }

    @Override
    public void createUserGroupFromExcel(String name, String type) {
        // 生成拼音编码
        String pinyin = PinyinUtil.getPinyin(name, StrUtil.EMPTY);
        String code = type + "_" + pinyin;
        
        // 创建用户组对象
        SystemUserGroupDo userGroupDo = SystemUserGroupDo.builder()
                .name(name)
                .type(type)
                .code(code)
                .remark("从Excel自动导入")
                .build();
        
        // 插入数据库
        userGroupMapper.insert(userGroupDo);
    }

    @Override
    public Long getIdByName(String name) {
        // 注意：使用 selectOne 时，如果有多条记录会抛异常
        // 但用户组名称应该是唯一的，所以这里不应该有多条记录
        SystemUserGroupDo userGroup = userGroupMapper.selectOne(
            new LambdaQueryWrapper<SystemUserGroupDo>()
                .eq(SystemUserGroupDo::getName, name)
                .eq(SystemUserGroupDo::getDeleted, 0)
        );
        return userGroup != null ? userGroup.getId() : null;
    }

    @Override
    public Set<String> getAllUserGroupRelations() {
        List<SystemUserGroupRDo> relations = userGroupRMapper.selectList(
            new LambdaQueryWrapper<SystemUserGroupRDo>()
                .eq(SystemUserGroupRDo::getDeleted, 0)
        );
        
        return relations.stream()
            .map(r -> r.getGroupId() + "_" + r.getUserId())
            .collect(Collectors.toSet());
    }

    @Override
    public void addUserGroupRelation(Long groupId, Long userId) {
        // 查询用户组类型
        SystemUserGroupDo userGroup = userGroupMapper.selectById(groupId);
        if (userGroup == null) {
            throw new ServiceException(UserGroupErrorCodeConstants.USER_GROUP_NOT_EXIST);
        }
        
        // 先查询是否存在（包括已删除的）
        SystemUserGroupRDo existingRelation = userGroupRMapper.selectOne(
            new LambdaQueryWrapper<SystemUserGroupRDo>()
                .eq(SystemUserGroupRDo::getGroupId, groupId)
                .eq(SystemUserGroupRDo::getUserId, userId)
        );
        
        if (existingRelation != null) {
            // 记录已存在
            if (existingRelation.getDeleted() != null && existingRelation.getDeleted()) {
                // 已删除的记录，恢复它
                existingRelation.setDeleted(false);
                existingRelation.setType(userGroup.getType()); // 更新类型
                userGroupRMapper.updateById(existingRelation);
            }
            // 如果 deleted = false，说明已经存在且有效，不需要操作
            return;
        }
        
        // 记录不存在，创建新关系
        SystemUserGroupRDo relation = SystemUserGroupRDo.builder()
                .groupId(groupId)
                .userId(userId)
                .type(userGroup.getType())
                .build();
        
        userGroupRMapper.insert(relation);
    }



    @Override
    public Set<String> getAllOrgUserGroupRelations() {
        List<UserGroupOrganizationDO> relations =
            userGroupOrganizationMapper.selectList(null);
        
        return relations.stream()
            .map(r -> r.getUserGroupId() + "_" + r.getOrganizationId())
            .collect(Collectors.toSet());
    }

    @Override
    public void addOrgUserGroupRelation(Long userGroupId, Long organizationId) {
        // 先查询是否存在（包括已删除的）
        UserGroupOrganizationDO existingRelation =
            userGroupOrganizationMapper.selectOne(
                new LambdaQueryWrapper<UserGroupOrganizationDO>()
                    .eq(UserGroupOrganizationDO::getUserGroupId, userGroupId)
                    .eq(UserGroupOrganizationDO::getOrganizationId, organizationId)
            );
        
        if (existingRelation != null) {
            // 记录已存在，直接返回（组织关系表可能没有deleted字段，不做恢复操作）
            return;
        }
        
        // 记录不存在，创建新关系
        UserGroupOrganizationDO relation =
            UserGroupOrganizationDO.builder()
                .userGroupId(userGroupId)
                .organizationId(organizationId)
                .build();
        
        userGroupOrganizationMapper.insert(relation);
    }


    @Override
    public List<UserGroupTreeResp> getUserGroupTree() {
        log.info("开始获取用户组树");
        
        // 2. 获取所有的用户组，然后 标识当前用户组，然后拼接成树形结构
        log.debug("步骤2: 获取所有用户组");
        List<SystemUserGroupDo> systemUserGroupDos = userGroupMapper.selectList();
        log.debug("总共获取到用户组数量: {}", systemUserGroupDos.size());
        
        // 根据type分成map集合
        HashMap<String, List<UserGroupTreeNodeResp>> map = new HashMap<>();
        log.debug("步骤3: 按类型分组用户组");
        for (SystemUserGroupDo systemUserGroupDo : systemUserGroupDos) {
            UserGroupTreeNodeResp userGroupTreeNodeResp = new UserGroupTreeNodeResp(systemUserGroupDo);
            if (map.containsKey(systemUserGroupDo.getType())){
                map.get(systemUserGroupDo.getType()).add(userGroupTreeNodeResp);
            }else{
                map.put(systemUserGroupDo.getType(), new ArrayList<>(List.of(userGroupTreeNodeResp)));
            }
        }
        log.debug("按类型分组完成，分组数量: {}", map.size());
        // 3. 填充字典
        log.debug("步骤4: 获取字典数据");
        SystemDictDo userGroupType = systemDictMapper.selectOne(
                new LambdaQueryWrapper<SystemDictDo>()
                        .eq(SystemDictDo::getCode, "userGroupType")
        );
        List<SystemDictLabelDo> systemDictLabelDos = systemDictLabelMapper.selectList(
                new LambdaQueryWrapper<SystemDictLabelDo>()
                        .eq(SystemDictLabelDo::getDictId, userGroupType.getId())
        );
        Map<String, SystemDictLabelDo> dictMap = systemDictLabelDos.stream().collect(Collectors.toMap(SystemDictLabelDo::getFieldKey, v -> v));
        ArrayList<UserGroupTreeResp> userGroupTreeResps = new ArrayList<>();
        map.forEach((k,v)->{
            userGroupTreeResps.add(UserGroupTreeResp.builder()
                            .type( k)
                    .name(dictMap.get(k).getFieldLabel())
                    .children(v)
                    .build()
            );
        });
        
        log.info("用户组树构建完成，返回节点数量: {}", userGroupTreeResps.size());
        // 对每个节点排序， isEnable=true的节点优先
//        userGroupTreeResps.forEach(v->{
//            v.getChildren().sort((o1, o2) -> {
//                if (o1.getIsEnable() && !o2.getIsEnable()){
//                    return -1;
//                }else if (!o1.getIsEnable() && o2.getIsEnable()){
//                    return 1;
//                }else{
//                    return o1.getName().compareTo(o2.getName());
//                }
//            });
//        });
        return userGroupTreeResps;
    }

    @Override
    @Transactional
    public CommonResult saveUserGroup(UserGroupTreeSaveVo userGroupTreeSaveVo) {
        // 添加空值检查
        if (userGroupTreeSaveVo == null) {
            log.warn("用户组树保存参数为空");
            return CommonResult.error("404","参数不能为空");
        }

        List<UserGroupTreeResp> tree = userGroupTreeSaveVo.getTree();
        log.info("开始保存用户组树，用户ID: {}", userGroupTreeSaveVo.getUserId());
        
        ArrayList<UserGroupTreeNodeResp> userGroupNodeResps = new ArrayList<>();
        for (UserGroupTreeResp userGroupTreeResp : tree) {
            userGroupNodeResps.addAll(userGroupTreeResp.getChildren());
        }
        log.debug("收集用户组节点数量: {}", userGroupNodeResps.size());
        
        // 1. 赛选出 tree里面的用户组的id
        ArrayList<Long> userGroupIds = new ArrayList<>();
        for (UserGroupTreeResp userGroupTreeResp : tree) {
            for (UserGroupTreeNodeResp child : userGroupTreeResp.getChildren()) {
                if (child.getIsEnable()){
                    userGroupIds.add(child.getId());
                }
            }
        }
        log.debug("筛选出启用的用户组ID数量: {}", userGroupIds.size());
        
        // 2. 查询原有的
        List<SystemUserGroupRDo> systemUserGroupRDos = systemUserGroupRMapper.selectList(
                new LambdaQueryWrapper<SystemUserGroupRDo>()
                        .eq(SystemUserGroupRDo::getUserId, userGroupTreeSaveVo.getUserId())
        );
        List<Long> oldUserGroupIds = systemUserGroupRDos.stream().map(SystemUserGroupRDo::getGroupId).toList();
        log.debug("查询到用户原有的用户组关系数量: {}", oldUserGroupIds.size());
        
        // 3. 原来有的 现在没了，要删除；  原来没得，现在有的，要添加
        List<Long> deletedGroupIds = new ArrayList<>();
        for (Long oldUserGroupId : oldUserGroupIds) {
            if (!userGroupIds.contains(oldUserGroupId)) {
                deletedGroupIds.add(oldUserGroupId);
            }
        }
        log.debug("需要删除的用户组关系数量: {}", deletedGroupIds.size());
        
        List<Long> insertGroupIds = new ArrayList<>();
        for (Long userGroupId : userGroupIds) {
            if (!oldUserGroupIds.contains(userGroupId)) {
                insertGroupIds.add(userGroupId);
            }
        }
        log.debug("需要新增的用户组关系数量: {}", insertGroupIds.size());
        
        if (deletedGroupIds.size() > 0) {
            log.debug("删除用户组关系: {}", deletedGroupIds);
            systemUserGroupRMapper.delete(
                    new LambdaQueryWrapper<SystemUserGroupRDo>()
                            .eq(SystemUserGroupRDo::getUserId, userGroupTreeSaveVo.getUserId())
                            .in(SystemUserGroupRDo::getGroupId, deletedGroupIds)
            );
            log.debug("成功删除用户组关系数量: {}", deletedGroupIds.size());
        }
        
        if (insertGroupIds.size() > 0) {
            log.debug("添加用户组关系: {}", insertGroupIds);
            // 批量新增
            List<UserGroupTreeNodeResp> list = userGroupNodeResps.stream().filter(node -> insertGroupIds.contains(node.getId())
            ).toList();
            List<SystemUserGroupRDo> list1 = list.stream().map(
                    group -> SystemUserGroupRDo.builder()
                            .userId(userGroupTreeSaveVo.getUserId())
                            .groupId(group.getId())
                            .type(group.getType())
                            .build()
            ).toList();
            systemUserGroupRMapper.insertBatch(list1);
            log.debug("成功添加用户组关系数量: {}", list1.size());

        }
        
        log.info("用户组树保存完成，用户ID: {}, 删除关系数: {}, 新增关系数: {}", 
                 userGroupTreeSaveVo.getUserId(), deletedGroupIds.size(), insertGroupIds.size());
        return CommonResult.success();

    }

    @Override
    @Transactional
    public CommonResult<Void> copyUserGroupTree(CopyUserGroupTreeVo copyUserGroupTreeVo) {
        log.info("开始复制用户组树，源用户ID: {}, 目标用户数量: {}", 
                 copyUserGroupTreeVo.getFromUserId(), copyUserGroupTreeVo.getToUserIds().size());
        
        // 1. 先查询 fromUser的所有的用户组
        List<SystemUserGroupRDo> systemUserGroupRDos = systemUserGroupRMapper.selectList(
                new LambdaQueryWrapper<SystemUserGroupRDo>()
                        .eq(SystemUserGroupRDo::getUserId, copyUserGroupTreeVo.getFromUserId())
        );
        log.debug("查询到源用户组关系数量: {}", systemUserGroupRDos.size());
        
        // 2. 删除 toUser的用户组
        int delete = systemUserGroupRMapper.delete(
                new LambdaQueryWrapper<SystemUserGroupRDo>()
                        .in(SystemUserGroupRDo::getUserId, copyUserGroupTreeVo.getToUserIds())
        );
        log.debug("删除目标用户原有用户组关系数量: {}", delete);
        
        // 3. 添加 toUser的用户组
        List<SystemUserGroupRDo> insertUserGroups = new ArrayList<>();
        copyUserGroupTreeVo.getToUserIds().forEach(toUserId -> {
            for (SystemUserGroupRDo systemUserGroupRDo : systemUserGroupRDos) {
                insertUserGroups.add(SystemUserGroupRDo.builder()
                        .userId(toUserId)
                        .groupId(systemUserGroupRDo.getGroupId())
                        .type(systemUserGroupRDo.getType())
                        .build());
            }
        });
        log.debug("构建待插入用户组关系数量: {}", insertUserGroups.size());
        
        Boolean b = systemUserGroupRMapper.insertBatch(insertUserGroups);
        if (!b) {
            log.error("添加用户组关系失败");
            return CommonResult.error("404","添加用户组关系失败");
        }
        
        log.info("成功复制用户组树，插入用户组关系数量: {}", insertUserGroups.size());
        return CommonResult.success();
    }

    @Override
    public CommonResult<Void> addUserGroupTree(CopyUserGroupTreeVo addUserGroupTreeVo) {
        // 1. 先查询 fromUser的所有的用户组
        List<SystemUserGroupRDo> systemUserGroupRDos = systemUserGroupRMapper.selectList(
                new LambdaQueryWrapper<SystemUserGroupRDo>()
                        .eq(SystemUserGroupRDo::getUserId, addUserGroupTreeVo.getFromUserId())
        );
        // 2. 查出所有原有的 用户组
        List<SystemUserGroupRDo> oldUserGroupRDos = systemUserGroupRMapper.selectList(
                new LambdaQueryWrapper<SystemUserGroupRDo>()
                        .in(SystemUserGroupRDo::getUserId, addUserGroupTreeVo.getToUserIds())
        );
        // 3. 添加 toUser的用户组
        List<SystemUserGroupRDo> insertUserGroups = new ArrayList<>();
        addUserGroupTreeVo.getToUserIds().forEach(toUserId -> {
            for (SystemUserGroupRDo systemUserGroupRDo : systemUserGroupRDos) {
                insertUserGroups.add(SystemUserGroupRDo.builder()
                        .userId(toUserId)
                        .groupId(systemUserGroupRDo.getGroupId())
                        .type(systemUserGroupRDo.getType())
                        .build());
            }
        });
        List<SystemUserGroupRDo> filterInsertUserGroups = insertUserGroups.stream().filter(insertUserGroup -> {
            for (SystemUserGroupRDo oldUserGroupRDo : oldUserGroupRDos) {
                if (insertUserGroup.getGroupId().equals(oldUserGroupRDo.getGroupId())) {
                    return false;
                }
            }
            return true;
        }).toList();
        Boolean b = systemUserGroupRMapper.insertBatch(insertUserGroups);
        if (!b) {
            return CommonResult.error("404","添加用户组关系失败");
        }
        return CommonResult.success();
    }


    @Override
    public Long getOrganizationIdByCode(String orgCode) {
        SystemManageOrgDO organization = systemManageOrgMapper.selectOne(
                new LambdaQueryWrapper<SystemManageOrgDO>()
                        .eq(SystemManageOrgDO::getCode, orgCode)
        );
        return organization != null ? organization.getId() : null;
    }
    @Override
    public CommonResult<Void> editUserGroup(EditUserGroupVo editUserGroupVo) {
        log.info("开始修改用户组，用户组ID: {}", editUserGroupVo.getId());
        SystemUserGroupDo systemUserGroupDo = SystemUserGroupDo.builder()
                .id(editUserGroupVo.getId())
                .build();
        if (editUserGroupVo.getName() != null){
            systemUserGroupDo.setName(editUserGroupVo.getName());
        }
        if (editUserGroupVo.getRemark() != null){
            systemUserGroupDo.setRemark(editUserGroupVo.getRemark());
        }
        userGroupMapper.updateById(systemUserGroupDo);
        log.info("修改用户组成功，用户组ID: {}", editUserGroupVo.getId());
        return CommonResult.success();
    }
}
