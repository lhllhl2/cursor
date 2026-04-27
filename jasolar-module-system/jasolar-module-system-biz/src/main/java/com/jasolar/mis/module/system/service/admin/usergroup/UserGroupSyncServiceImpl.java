package com.jasolar.mis.module.system.service.admin.usergroup;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.jasolar.mis.module.system.domain.admin.user.SystemUserDo;
import com.jasolar.mis.module.system.domain.admin.usergroup.SystemUserGroupDo;
import com.jasolar.mis.module.system.domain.admin.usergroup.SystemUserGroupRDo;
import com.jasolar.mis.module.system.mapper.admin.user.SystemUserMapper;
import com.jasolar.mis.module.system.mapper.admin.usergroup.SystemUserGroupRMapper;
import com.jasolar.mis.module.system.mapper.admin.usergroup.UserGroupMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 用户组同步 Service 实现类
 *
 * @author jasolar
 */
@Service
@Slf4j
public class UserGroupSyncServiceImpl implements UserGroupSyncService {

    @Resource
    private RedissonClient redissonClient;
    
    @Resource
    private SystemUserGroupRMapper systemUserGroupRMapper;
    
    @Resource
    private SystemUserMapper systemUserMapper;
    
    @Resource
    private UserGroupMapper userGroupMapper;
    
    // 帆软数据库配置（使用默认值，配置可选）
    @Value("${spring.datasource.dynamic.datasource.fr-oracle.url:}")
    private String frOracleUrl;
    
    @Value("${spring.datasource.dynamic.datasource.fr-oracle.username:}")
    private String frOracleUsername;
    
    @Value("${spring.datasource.dynamic.datasource.fr-oracle.password:}")
    private String frOraclePassword;
    
    // 帆软数据库Schema配置 - 从数据源配置中获取（使用默认值）
    @Value("${spring.datasource.dynamic.datasource.fr-oracle.username:}")
    private String frSchema;
    
    // StarRocks数据库配置（使用默认值，配置可选）
    @Value("${spring.datasource.dynamic.datasource.starrocks.url:}")
    private String starRocksUrl;
    
    @Value("${spring.datasource.dynamic.datasource.starrocks.username:}")
    private String starRocksUsername;
    
    @Value("${spring.datasource.dynamic.datasource.starrocks.password:}")
    private String starRocksPassword;

    @Override
    public void syncFrUserGroupRelation() {
        RLock lock = redissonClient.getLock("syncFrUserGroupRelationLock");

        try {
            boolean b = lock.tryLock(10, 30, TimeUnit.SECONDS);
            if(!b){
                return;
            }

            try {
                log.info("开始同步帆软用户组关系...");
                
                // 执行同步逻辑
                doSyncUserGroupRelation();
                
                log.info("帆软用户组关系同步完成");
            }finally {
                // 检查锁是否仍然被当前线程持有
                if (lock.isHeldByCurrentThread()) {
                    lock.unlock();
                }
            }
        }catch (InterruptedException e){
            Thread.currentThread().interrupt();
            log.info("线程被中断,正在退出。。。");
        }
    }

    @Override
    public void syncFrUserGroup() {
        RLock lock = redissonClient.getLock("syncFrUserGroupLock");

        try {
            boolean b = lock.tryLock(10, 30, TimeUnit.SECONDS);
            if(!b){
                return;
            }

            try {
                log.info("开始同步帆软用户组...");
                
                // 执行同步逻辑
                doSyncUserGroup();
                
                log.info("帆软用户组同步完成");
            }finally {
                // 检查锁是否仍然被当前线程持有
                if (lock.isHeldByCurrentThread()) {
                    lock.unlock();
                }
            }
        }catch (InterruptedException e){
            Thread.currentThread().interrupt();
            log.info("线程被中断,正在退出。。。");
        }
    }

    @Override
    public void syncStarRocksUserGroupRelation() {
        RLock lock = redissonClient.getLock("syncStarRocksUserGroupRelationLock");

        try {
            boolean b = lock.tryLock(10, 30, TimeUnit.SECONDS);
            if(!b){
                return;
            }

            try {
                log.info("开始同步StarRocks用户组关系...");
                
                // 执行同步逻辑
                doSyncStarRocksUserGroupRelation();
                
                log.info("StarRocks用户组关系同步完成");
            }finally {
                // 检查锁是否仍然被当前线程持有
                if (lock.isHeldByCurrentThread()) {
                    lock.unlock();
                }
            }
        }catch (InterruptedException e){
            Thread.currentThread().interrupt();
            log.info("线程被中断,正在退出。。。");
        }
    }

    @Override
    public void syncStarRocksUserGroup() {
        RLock lock = redissonClient.getLock("syncStarRocksUserGroupLock");

        try {
            boolean b = lock.tryLock(10, 30, TimeUnit.SECONDS);
            if(!b){
                return;
            }

            try {
                log.info("开始同步StarRocks用户组...");
                
                // 执行同步逻辑
                doSyncStarRocksUserGroup();
                
                log.info("StarRocks用户组同步完成");
            }finally {
                // 检查锁是否仍然被当前线程持有
                if (lock.isHeldByCurrentThread()) {
                    lock.unlock();
                }
            }
        }catch (InterruptedException e){
            Thread.currentThread().interrupt();
            log.info("线程被中断,正在退出。。。");
        }
    }

    /**
     * 执行用户组关系同步逻辑
     */
    private void doSyncUserGroupRelation() {
        log.info("开始执行用户组关系同步逻辑...");
        
        try {
            // 步骤1: 查询SYSTEM_USER_GROUP_R表type为3的数据
            Set<UserGroupRelation> currentRelations = getCurrentUserGroupRelations();
            log.info("当前系统用户组关系数量: {}", currentRelations.size());
            
            // 步骤5: 查询帆软数据库中的用户组关系数据
            Set<UserGroupRelation> frRelations = getFrUserGroupRelations();
            log.info("帆软数据库用户组关系数量: {}", frRelations.size());
            
            // 步骤6: 计算需要新增的关系
            Set<UserGroupRelation> toAdd = new HashSet<>(currentRelations);
            toAdd.removeAll(frRelations);
            log.info("需要新增的用户组关系数量: {}", toAdd.size());
            
            // 步骤6: 计算需要删除的关系
            Set<UserGroupRelation> toDelete = new HashSet<>(frRelations);
            toDelete.removeAll(currentRelations);
            log.info("需要删除的用户组关系数量: {}", toDelete.size());
            
            // 步骤6: 执行新增操作
            if (!toAdd.isEmpty()) {
                addUserGroupRelations(toAdd);
            }
            
            // 步骤6: 执行删除操作
            if (!toDelete.isEmpty()) {
                deleteUserGroupRelations(toDelete);
            }
            
            log.info("用户组关系同步完成，新增: {}, 删除: {}", toAdd.size(), toDelete.size());
            
        } catch (Exception e) {
            log.error("用户组关系同步失败", e);
            throw new RuntimeException("用户组关系同步失败", e);
        }
    }
    
    /**
     * 获取当前系统的用户组关系
     */
    private Set<UserGroupRelation> getCurrentUserGroupRelations() {
        Set<UserGroupRelation> relations = new HashSet<>();
        
        // 步骤1: 查询SYSTEM_USER_GROUP_R表type为1（菜单权限）、type为2（报表类型）、type为3（组织类型）和type为4（数据类型）的数据
        LambdaQueryWrapper<SystemUserGroupRDo> wrapper = new LambdaQueryWrapper<SystemUserGroupRDo>()
                .in(SystemUserGroupRDo::getType, "1", "2", "3", "4");
        List<SystemUserGroupRDo> userGroupRelations = systemUserGroupRMapper.selectList(wrapper);
        log.info("查询到type为1、2、3和4的用户组关系数量: {}", userGroupRelations.size());
        
        if (userGroupRelations.isEmpty()) {
            return relations;
        }
        
        // 收集userId和groupId
        Set<Long> userIdSet = userGroupRelations.stream()
                .map(SystemUserGroupRDo::getUserId)
                .collect(Collectors.toSet());
        Set<Long> groupIdSet = userGroupRelations.stream()
                .map(SystemUserGroupRDo::getGroupId)
                .collect(Collectors.toSet());
        
        log.info("去重后的userId数量: {}, groupId数量: {}", userIdSet.size(), groupIdSet.size());
        
        // 步骤2: 批量查询SYSTEM_USER表，组装userId->User的map
        Map<Long, SystemUserDo> userMap = new HashMap<>();
        if (!userIdSet.isEmpty()) {
            LambdaQueryWrapper<SystemUserDo> userWrapper = new LambdaQueryWrapper<SystemUserDo>()
                    .in(SystemUserDo::getId, userIdSet);
            List<SystemUserDo> users = systemUserMapper.selectList(userWrapper);
            userMap = users.stream()
                    .collect(Collectors.toMap(SystemUserDo::getId, user -> user));
            log.info("查询到用户数量: {}", userMap.size());
        }
        
        // 步骤3: 批量查询SYSTEM_USER_GROUP表，组装groupId->UserGroup的map
        Map<Long, SystemUserGroupDo> groupMap = new HashMap<>();
        if (!groupIdSet.isEmpty()) {
            LambdaQueryWrapper<SystemUserGroupDo> groupWrapper = new LambdaQueryWrapper<SystemUserGroupDo>()
                    .in(SystemUserGroupDo::getId, groupIdSet);
            List<SystemUserGroupDo> groups = userGroupMapper.selectList(groupWrapper);
            groupMap = groups.stream()
                    .collect(Collectors.toMap(SystemUserGroupDo::getId, group -> group));
            log.info("查询到用户组数量: {}", groupMap.size());
        }
        
        // 步骤4: 结合两个map，对号入座形成userName-groupCode的set
        for (SystemUserGroupRDo relation : userGroupRelations) {
            Long userId = relation.getUserId();
            Long groupId = relation.getGroupId();
            
            SystemUserDo user = userMap.get(userId);
            SystemUserGroupDo group = groupMap.get(groupId);
            
            if (user != null && group != null) {
                String userName = user.getUserName();
                String groupCode = group.getCode();
                relations.add(new UserGroupRelation(userName, groupCode));
            } else {
                if (user == null) {
                    log.debug("未找到用户ID: {}", userId);
                }
                if (group == null) {
                    log.debug("未找到用户组ID: {}", groupId);
                }
            }
        }
        
        log.info("最终组装完成的用户组关系数量: {}", relations.size());
        return relations;
    }
    
    /**
     * 获取帆软数据库中的用户组关系
     */
    private Set<UserGroupRelation> getFrUserGroupRelations() {
        Set<UserGroupRelation> relations = new HashSet<>();
        
        log.info("开始查询帆软数据库中的用户组关系...");
        String tableName = frSchema + ".FR_USER_GROUP_RELATION";
        log.info("使用的表名: {}", tableName);
        
        try (Connection connection = DriverManager.getConnection(frOracleUrl, frOracleUsername, frOraclePassword);
             Statement statement = connection.createStatement()) {
            
            log.info("手动连接帆软数据库成功");
            
            // 查询帆软数据库中的所有用户组关系
            try (ResultSet rs = statement.executeQuery("SELECT * FROM " + tableName)) {
                while (rs.next()) {
                    String userName = rs.getString("USER_NAME");
                    String groupCode = rs.getString("GROUP_CODE");
                    relations.add(new UserGroupRelation(userName, groupCode));
                }
                log.info("帆软数据库查询成功，获取到 {} 条记录", relations.size());
            }
            
        } catch (Exception e) {
            log.error("查询帆软数据库失败", e);
            log.error("错误详情: {}", e.getMessage());
            throw new RuntimeException("查询帆软数据库失败", e);
        }
        
        return relations;
    }
    
    /**
     * 新增用户组关系
     */
    private void addUserGroupRelations(Set<UserGroupRelation> relations) {
        if (relations.isEmpty()) {
            return;
        }
        
        log.info("开始批量新增用户组关系，数量: {}", relations.size());
        
        try (Connection connection = DriverManager.getConnection(frOracleUrl, frOracleUsername, frOraclePassword)) {
            connection.setAutoCommit(false);
            
            try (Statement statement = connection.createStatement()) {
                int successCount = 0;
                LocalDateTime now = LocalDateTime.now();
                String nowStr = now.format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                
                String tableName = frSchema + ".FR_USER_GROUP_RELATION";
                for (UserGroupRelation relation : relations) {
                    // 使用雪花算法生成ID
                    Long id = System.currentTimeMillis(); // 临时使用时间戳，后续可以注入IdentifierGenerator
                    String insertSql = String.format(
                        "INSERT INTO %s (ID, USER_NAME, GROUP_CODE, CREATOR, CREATE_TIME, UPDATER, UPDATE_TIME) " +
                        "VALUES (%d, '%s', '%s', 'SYSTEM', TO_DATE('%s', 'YYYY-MM-DD HH24:MI:SS'), 'SYSTEM', TO_DATE('%s', 'YYYY-MM-DD HH24:MI:SS'))",
                        tableName, id, relation.getUserName(), relation.getGroupCode(), nowStr, nowStr
                    );
                    
                    statement.executeUpdate(insertSql);
                    successCount++;
                }
                
                connection.commit();
                log.info("批量新增用户组关系完成，成功数量: {}", successCount);
                
            } catch (Exception e) {
                connection.rollback();
                log.error("批量新增用户组关系失败", e);
                throw new RuntimeException("批量新增用户组关系失败", e);
            }
            
        } catch (Exception e) {
            log.error("连接帆软数据库失败", e);
            throw new RuntimeException("连接帆软数据库失败", e);
        }
    }
    
    /**
     * 删除用户组关系
     */
    private void deleteUserGroupRelations(Set<UserGroupRelation> relations) {
        if (relations.isEmpty()) {
            return;
        }
        
        log.info("开始批量删除用户组关系，数量: {}", relations.size());
        
        try (Connection connection = DriverManager.getConnection(frOracleUrl, frOracleUsername, frOraclePassword)) {
            connection.setAutoCommit(false);
            
            try (Statement statement = connection.createStatement()) {
                int successCount = 0;
                
                String tableName = frSchema + ".FR_USER_GROUP_RELATION";
                for (UserGroupRelation relation : relations) {
                    String deleteSql = String.format(
                        "DELETE FROM %s WHERE USER_NAME = '%s' AND GROUP_CODE = '%s'",
                        tableName, relation.getUserName(), relation.getGroupCode()
                    );
                    
                    int affectedRows = statement.executeUpdate(deleteSql);
                    if (affectedRows > 0) {
                        successCount++;
                    }
                }
                
                connection.commit();
                log.info("批量删除用户组关系完成，成功数量: {}", successCount);
                
            } catch (Exception e) {
                connection.rollback();
                log.error("批量删除用户组关系失败", e);
                throw new RuntimeException("批量删除用户组关系失败", e);
            }
            
        } catch (Exception e) {
            log.error("连接帆软数据库失败", e);
            throw new RuntimeException("连接帆软数据库失败", e);
        }
    }
    
    /**
     * 用户组关系内部类
     */
    private static class UserGroupRelation {
        private final String userName;
        private final String groupCode;
        
        public UserGroupRelation(String userName, String groupCode) {
            this.userName = userName;
            this.groupCode = groupCode;
        }
        
        public String getUserName() {
            return userName;
        }
        
        public String getGroupCode() {
            return groupCode;
        }
        
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            UserGroupRelation that = (UserGroupRelation) o;
            return Objects.equals(userName, that.userName) && 
                   Objects.equals(groupCode, that.groupCode);
        }
        
        @Override
        public int hashCode() {
            return Objects.hash(userName, groupCode);
        }
        
        @Override
        public String toString() {
            return "UserGroupRelation{" +
                    "userName='" + userName + '\'' +
                    ", groupCode='" + groupCode + '\'' +
                    '}';
        }
    }

    /**
     * 执行用户组同步逻辑
     */
    private void doSyncUserGroup() {
        log.info("开始执行用户组同步逻辑...");
        
        try {
            // 步骤1: 查询SYSTEM_USER_GROUP表type为3的数据
            Map<String, SystemUserGroupDo> currentUserGroupMap = getCurrentUserGroupMap();
            Set<String> currentGroupCodes = currentUserGroupMap.keySet();
            log.info("当前系统用户组编码数量: {}", currentGroupCodes.size());
            
            // 步骤3: 查询帆软数据库中的用户组数据
            Set<String> frGroupCodes = getFrUserGroupCodes();
            log.info("帆软数据库用户组编码数量: {}", frGroupCodes.size());
            
            // 步骤4: 计算需要新增的用户组
            Set<String> toAdd = new HashSet<>(currentGroupCodes);
            toAdd.removeAll(frGroupCodes);
            log.info("需要新增的用户组数量: {}", toAdd.size());
            
            // 步骤4: 计算需要删除的用户组
            Set<String> toDelete = new HashSet<>(frGroupCodes);
            toDelete.removeAll(currentGroupCodes);
            log.info("需要删除的用户组数量: {}", toDelete.size());
            
            // 步骤4: 执行新增操作
            if (!toAdd.isEmpty()) {
                addUserGroups(toAdd, currentUserGroupMap);
            }
            
            // 步骤4: 执行删除操作
            if (!toDelete.isEmpty()) {
                deleteUserGroups(toDelete);
            }
            
            log.info("用户组同步完成，新增: {}, 删除: {}", toAdd.size(), toDelete.size());
            
        } catch (Exception e) {
            log.error("用户组同步失败", e);
            throw new RuntimeException("用户组同步失败", e);
        }
    }
    
    /**
     * 获取当前系统的用户组映射
     */
    private Map<String, SystemUserGroupDo> getCurrentUserGroupMap() {
        // 步骤1: 查询SYSTEM_USER_GROUP表type为1（菜单权限）、type为3（组织类型）和type为4（数据类型）的数据
        LambdaQueryWrapper<SystemUserGroupDo> wrapper = new LambdaQueryWrapper<SystemUserGroupDo>()
                .in(SystemUserGroupDo::getType, "1", "3", "4");
        List<SystemUserGroupDo> userGroups = userGroupMapper.selectList(wrapper);
        log.info("查询到type为1、3和4的用户组数量: {}", userGroups.size());
        
        // 步骤2: 将查询出的list数据，按照Map，key为groupCode，value为UserGroup实体放入map
        Map<String, SystemUserGroupDo> groupMap = new HashMap<>();
        for (SystemUserGroupDo userGroup : userGroups) {
            String groupCode = userGroup.getCode();
            groupMap.put(groupCode, userGroup);
        }
        
        log.info("收集到的用户组编码数量: {}", groupMap.size());
        return groupMap;
    }
    
    /**
     * 获取帆软数据库中的用户组编码
     */
    private Set<String> getFrUserGroupCodes() {
        Set<String> groupCodes = new HashSet<>();
        
        log.info("开始查询帆软数据库中的用户组...");
        String tableName = frSchema + ".FR_USER_GROUP";
        log.info("使用的表名: {}", tableName);
        
        try (Connection connection = DriverManager.getConnection(frOracleUrl, frOracleUsername, frOraclePassword);
             Statement statement = connection.createStatement()) {
            
            log.info("手动连接帆软数据库成功");
            
            // 查询帆软数据库中的所有用户组
            try (ResultSet rs = statement.executeQuery("SELECT * FROM " + tableName)) {
                while (rs.next()) {
                    String groupCode = rs.getString("CODE");
                    groupCodes.add(groupCode);
                }
                log.info("帆软数据库查询成功，获取到 {} 条记录", groupCodes.size());
            }
            
        } catch (Exception e) {
            log.error("查询帆软数据库失败", e);
            log.error("错误详情: {}", e.getMessage());
            throw new RuntimeException("查询帆软数据库失败", e);
        }
        
        return groupCodes;
    }
    
    /**
     * 新增用户组
     */
    private void addUserGroups(Set<String> groupCodes, Map<String, SystemUserGroupDo> userGroupMap) {
        if (groupCodes.isEmpty()) {
            return;
        }
        
        log.info("开始批量新增用户组，数量: {}", groupCodes.size());
        
        try (Connection connection = DriverManager.getConnection(frOracleUrl, frOracleUsername, frOraclePassword)) {
            connection.setAutoCommit(false);
            
            try (Statement statement = connection.createStatement()) {
                int successCount = 0;
                LocalDateTime now = LocalDateTime.now();
                String nowStr = now.format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                
                String tableName = frSchema + ".FR_USER_GROUP";
                for (String groupCode : groupCodes) {
                    // 从map中获取用户组详细信息
                    SystemUserGroupDo userGroup = userGroupMap.get(groupCode);
                    if (userGroup == null) {
                        log.warn("未找到用户组编码: {}", groupCode);
                        continue;
                    }
                    
                    // 使用雪花算法生成ID
                    Long id = System.currentTimeMillis(); // 临时使用时间戳，后续可以注入IdentifierGenerator
                    String insertSql = String.format(
                        "INSERT INTO %s (ID, CODE, NAME, REMARK, CREATOR, CREATE_TIME, UPDATER, UPDATE_TIME) " +
                        "VALUES (%d, '%s', '%s', '%s', 'SYSTEM', TO_DATE('%s', 'YYYY-MM-DD HH24:MI:SS'), 'SYSTEM', TO_DATE('%s', 'YYYY-MM-DD HH24:MI:SS'))",
                        tableName, id, userGroup.getCode(), userGroup.getName(), 
                        userGroup.getRemark() != null ? userGroup.getRemark() : "", nowStr, nowStr
                    );
                    
                    statement.executeUpdate(insertSql);
                    successCount++;
                }
                
                connection.commit();
                log.info("批量新增用户组完成，成功数量: {}", successCount);
                
            } catch (Exception e) {
                connection.rollback();
                log.error("批量新增用户组失败", e);
                throw new RuntimeException("批量新增用户组失败", e);
            }
            
        } catch (Exception e) {
            log.error("连接帆软数据库失败", e);
            throw new RuntimeException("连接帆软数据库失败", e);
        }
    }
    
    /**
     * 删除用户组
     */
    private void deleteUserGroups(Set<String> groupCodes) {
        if (groupCodes.isEmpty()) {
            return;
        }
        
        log.info("开始批量删除用户组，数量: {}", groupCodes.size());
        
        try (Connection connection = DriverManager.getConnection(frOracleUrl, frOracleUsername, frOraclePassword)) {
            connection.setAutoCommit(false);
            
            try (Statement statement = connection.createStatement()) {
                int successCount = 0;
                
                String tableName = frSchema + ".FR_USER_GROUP";
                for (String groupCode : groupCodes) {
                    String deleteSql = String.format(
                        "DELETE FROM %s WHERE CODE = '%s'",
                        tableName, groupCode
                    );
                    
                    int affectedRows = statement.executeUpdate(deleteSql);
                    if (affectedRows > 0) {
                        successCount++;
                    }
                }
                
                connection.commit();
                log.info("批量删除用户组完成，成功数量: {}", successCount);
                
            } catch (Exception e) {
                connection.rollback();
                log.error("批量删除用户组失败", e);
                throw new RuntimeException("批量删除用户组失败", e);
            }
            
        } catch (Exception e) {
            log.error("连接帆软数据库失败", e);
            throw new RuntimeException("连接帆软数据库失败", e);
        }
    }
    
    /**
     * 执行StarRocks用户组关系同步逻辑
     */
    private void doSyncStarRocksUserGroupRelation() {
        log.info("开始执行StarRocks用户组关系同步逻辑...");
        
        try {
            // 步骤1: 查询当前系统的用户组关系数据
            Set<UserGroupRelation> currentRelations = getCurrentUserGroupRelations();
            log.info("当前系统用户组关系数量: {}", currentRelations.size());
            
            // 步骤2: 查询StarRocks数据库中的用户组关系数据
            Set<UserGroupRelation> starRocksRelations = getStarRocksUserGroupRelations();
            log.info("StarRocks数据库用户组关系数量: {}", starRocksRelations.size());
            
            // 步骤3: 计算需要新增的关系
            Set<UserGroupRelation> toAdd = new HashSet<>(currentRelations);
            toAdd.removeAll(starRocksRelations);
            log.info("需要新增的用户组关系数量: {}", toAdd.size());
            
            // 步骤4: 计算需要删除的关系
            Set<UserGroupRelation> toDelete = new HashSet<>(starRocksRelations);
            toDelete.removeAll(currentRelations);
            log.info("需要删除的用户组关系数量: {}", toDelete.size());
            
            // 步骤5: 执行新增操作
            if (!toAdd.isEmpty()) {
                addStarRocksUserGroupRelations(toAdd);
            }
            
            // 步骤6: 执行删除操作
            if (!toDelete.isEmpty()) {
                deleteStarRocksUserGroupRelations(toDelete);
            }
            
            log.info("StarRocks用户组关系同步完成，新增: {}, 删除: {}", toAdd.size(), toDelete.size());
            
        } catch (Exception e) {
            log.error("StarRocks用户组关系同步失败", e);
            throw new RuntimeException("StarRocks用户组关系同步失败", e);
        }
    }
    
    /**
     * 执行StarRocks用户组同步逻辑
     */
    private void doSyncStarRocksUserGroup() {
        log.info("开始执行StarRocks用户组同步逻辑...");
        
        try {
            // 步骤1: 查询当前系统的用户组数据
            Map<String, SystemUserGroupDo> currentUserGroupMap = getCurrentUserGroupMap();
            Set<String> currentGroupCodes = currentUserGroupMap.keySet();
            log.info("当前系统用户组编码数量: {}", currentGroupCodes.size());
            
            // 步骤2: 查询StarRocks数据库中的用户组数据
            Set<String> starRocksGroupCodes = getStarRocksUserGroupCodes();
            log.info("StarRocks数据库用户组编码数量: {}", starRocksGroupCodes.size());
            
            // 步骤3: 计算需要新增的用户组
            Set<String> toAdd = new HashSet<>(currentGroupCodes);
            toAdd.removeAll(starRocksGroupCodes);
            log.info("需要新增的用户组数量: {}", toAdd.size());
            
            // 步骤4: 计算需要删除的用户组
            Set<String> toDelete = new HashSet<>(starRocksGroupCodes);
            toDelete.removeAll(currentGroupCodes);
            log.info("需要删除的用户组数量: {}", toDelete.size());
            
            // 步骤5: 执行新增操作
            if (!toAdd.isEmpty()) {
                addStarRocksUserGroups(toAdd, currentUserGroupMap);
            }
            
            // 步骤6: 执行删除操作
            if (!toDelete.isEmpty()) {
                deleteStarRocksUserGroups(toDelete);
            }
            
            log.info("StarRocks用户组同步完成，新增: {}, 删除: {}", toAdd.size(), toDelete.size());
            
        } catch (Exception e) {
            log.error("StarRocks用户组同步失败", e);
            throw new RuntimeException("StarRocks用户组同步失败", e);
        }
    }
    
    /**
     * 获取StarRocks数据库中的用户组关系
     */
    private Set<UserGroupRelation> getStarRocksUserGroupRelations() {
        Set<UserGroupRelation> relations = new HashSet<>();
        
        log.info("开始查询StarRocks数据库中的用户组关系...");
        String tableName = "fin_fr_user_group_relation";
        log.info("使用的表名: {}", tableName);
        
        try (Connection connection = DriverManager.getConnection(starRocksUrl, starRocksUsername, starRocksPassword);
             Statement statement = connection.createStatement()) {
            
            log.info("手动连接StarRocks数据库成功");
            
            // 查询StarRocks数据库中的所有用户组关系
            try (ResultSet rs = statement.executeQuery("SELECT * FROM " + tableName)) {
                while (rs.next()) {
                    String userName = rs.getString("user_name");
                    String groupCode = rs.getString("group_code");
                    relations.add(new UserGroupRelation(userName, groupCode));
                }
                log.info("StarRocks数据库查询成功，获取到 {} 条记录", relations.size());
            }
            
        } catch (Exception e) {
            log.error("查询StarRocks数据库失败", e);
            log.error("错误详情: {}", e.getMessage());
            throw new RuntimeException("查询StarRocks数据库失败", e);
        }
        
        return relations;
    }
    
    /**
     * 获取StarRocks数据库中的用户组编码
     */
    private Set<String> getStarRocksUserGroupCodes() {
        Set<String> groupCodes = new HashSet<>();
        
        log.info("开始查询StarRocks数据库中的用户组...");
        String tableName = "fin_fr_user_group";
        log.info("使用的表名: {}", tableName);
        
        try (Connection connection = DriverManager.getConnection(starRocksUrl, starRocksUsername, starRocksPassword);
             Statement statement = connection.createStatement()) {
            
            log.info("手动连接StarRocks数据库成功");
            
            // 查询StarRocks数据库中的所有用户组
            try (ResultSet rs = statement.executeQuery("SELECT * FROM " + tableName)) {
                while (rs.next()) {
                    String groupCode = rs.getString("code");
                    groupCodes.add(groupCode);
                }
                log.info("StarRocks数据库查询成功，获取到 {} 条记录", groupCodes.size());
            }
            
        } catch (Exception e) {
            log.error("查询StarRocks数据库失败", e);
            log.error("错误详情: {}", e.getMessage());
            throw new RuntimeException("查询StarRocks数据库失败", e);
        }
        
        return groupCodes;
    }
    
    /**
     * 新增StarRocks用户组关系
     */
    private void addStarRocksUserGroupRelations(Set<UserGroupRelation> relations) {
        if (relations.isEmpty()) {
            return;
        }
        
        log.info("开始批量新增StarRocks用户组关系，数量: {}", relations.size());
        
        try (Connection connection = DriverManager.getConnection(starRocksUrl, starRocksUsername, starRocksPassword)) {
            connection.setAutoCommit(false);
            
            try (Statement statement = connection.createStatement()) {
                int successCount = 0;
                LocalDateTime now = LocalDateTime.now();
                String nowStr = now.format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                
                String tableName = "fin_fr_user_group_relation";
                for (UserGroupRelation relation : relations) {
                    // 使用雪花算法生成ID
                    Long id = System.currentTimeMillis(); // 临时使用时间戳，后续可以注入IdentifierGenerator
                    String insertSql = String.format(
                        "INSERT INTO %s (id, user_name, group_code, creator, create_time, updater, update_time) " +
                        "VALUES (%d, '%s', '%s', 'SYSTEM', '%s', 'SYSTEM', '%s')",
                        tableName, id, relation.getUserName(), relation.getGroupCode(), nowStr, nowStr
                    );
                    
                    statement.executeUpdate(insertSql);
                    successCount++;
                }
                
                connection.commit();
                log.info("批量新增StarRocks用户组关系完成，成功数量: {}", successCount);
                
            } catch (Exception e) {
                connection.rollback();
                log.error("批量新增StarRocks用户组关系失败", e);
                throw new RuntimeException("批量新增StarRocks用户组关系失败", e);
            }
            
        } catch (Exception e) {
            log.error("连接StarRocks数据库失败", e);
            throw new RuntimeException("连接StarRocks数据库失败", e);
        }
    }
    
    /**
     * 删除StarRocks用户组关系
     */
    private void deleteStarRocksUserGroupRelations(Set<UserGroupRelation> relations) {
        if (relations.isEmpty()) {
            return;
        }
        
        log.info("开始批量删除StarRocks用户组关系，数量: {}", relations.size());
        
        try (Connection connection = DriverManager.getConnection(starRocksUrl, starRocksUsername, starRocksPassword)) {
            connection.setAutoCommit(false);
            
            try (Statement statement = connection.createStatement()) {
                int successCount = 0;
                
                String tableName = "fin_fr_user_group_relation";
                for (UserGroupRelation relation : relations) {
                    String deleteSql = String.format(
                        "DELETE FROM %s WHERE user_name = '%s' AND group_code = '%s'",
                        tableName, relation.getUserName(), relation.getGroupCode()
                    );
                    
                    int affectedRows = statement.executeUpdate(deleteSql);
                    if (affectedRows > 0) {
                        successCount++;
                    }
                }
                
                connection.commit();
                log.info("批量删除StarRocks用户组关系完成，成功数量: {}", successCount);
                
            } catch (Exception e) {
                connection.rollback();
                log.error("批量删除StarRocks用户组关系失败", e);
                throw new RuntimeException("批量删除StarRocks用户组关系失败", e);
            }
            
        } catch (Exception e) {
            log.error("连接StarRocks数据库失败", e);
            throw new RuntimeException("连接StarRocks数据库失败", e);
        }
    }
    
    /**
     * 新增StarRocks用户组
     */
    private void addStarRocksUserGroups(Set<String> groupCodes, Map<String, SystemUserGroupDo> userGroupMap) {
        if (groupCodes.isEmpty()) {
            return;
        }
        
        log.info("开始批量新增StarRocks用户组，数量: {}", groupCodes.size());
        
        try (Connection connection = DriverManager.getConnection(starRocksUrl, starRocksUsername, starRocksPassword)) {
            connection.setAutoCommit(false);
            
            try (Statement statement = connection.createStatement()) {
                int successCount = 0;
                LocalDateTime now = LocalDateTime.now();
                String nowStr = now.format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                
                String tableName = "fin_fr_user_group";
                for (String groupCode : groupCodes) {
                    // 从map中获取用户组详细信息
                    SystemUserGroupDo userGroup = userGroupMap.get(groupCode);
                    if (userGroup == null) {
                        log.warn("未找到用户组编码: {}", groupCode);
                        continue;
                    }
                    
                    // 使用雪花算法生成ID
                    Long id = System.currentTimeMillis(); // 临时使用时间戳，后续可以注入IdentifierGenerator
                    String insertSql = String.format(
                        "INSERT INTO %s (id, code, name, remark, creator, create_time, updater, update_time) " +
                        "VALUES (%d, '%s', '%s', '%s', 'SYSTEM', '%s', 'SYSTEM', '%s')",
                        tableName, id, userGroup.getCode(), userGroup.getName(), 
                        userGroup.getRemark() != null ? userGroup.getRemark() : "", nowStr, nowStr
                    );
                    
                    statement.executeUpdate(insertSql);
                    successCount++;
                }
                
                connection.commit();
                log.info("批量新增StarRocks用户组完成，成功数量: {}", successCount);
                
            } catch (Exception e) {
                connection.rollback();
                log.error("批量新增StarRocks用户组失败", e);
                throw new RuntimeException("批量新增StarRocks用户组失败", e);
            }
            
        } catch (Exception e) {
            log.error("连接StarRocks数据库失败", e);
            throw new RuntimeException("连接StarRocks数据库失败", e);
        }
    }
    
    /**
     * 删除StarRocks用户组
     */
    private void deleteStarRocksUserGroups(Set<String> groupCodes) {
        if (groupCodes.isEmpty()) {
            return;
        }
        
        log.info("开始批量删除StarRocks用户组，数量: {}", groupCodes.size());
        
        try (Connection connection = DriverManager.getConnection(starRocksUrl, starRocksUsername, starRocksPassword)) {
            connection.setAutoCommit(false);
            
            try (Statement statement = connection.createStatement()) {
                int successCount = 0;
                
                String tableName = "fin_fr_user_group";
                for (String groupCode : groupCodes) {
                    String deleteSql = String.format(
                        "DELETE FROM %s WHERE code = '%s'",
                        tableName, groupCode
                    );
                    
                    int affectedRows = statement.executeUpdate(deleteSql);
                    if (affectedRows > 0) {
                        successCount++;
                    }
                }
                
                connection.commit();
                log.info("批量删除StarRocks用户组完成，成功数量: {}", successCount);
                
            } catch (Exception e) {
                connection.rollback();
                log.error("批量删除StarRocks用户组失败", e);
                throw new RuntimeException("批量删除StarRocks用户组失败", e);
            }
            
        } catch (Exception e) {
            log.error("连接StarRocks数据库失败", e);
            throw new RuntimeException("连接StarRocks数据库失败", e);
        }
    }
}
