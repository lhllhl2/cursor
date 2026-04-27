package com.jasolar.mis.module.system.service.admin.usergroup;

/**
 * 用户组同步 Service 接口
 *
 * @author jasolar
 */
public interface UserGroupSyncService {

    /**
     * 同步帆软用户组关系
     * 
     * 将用户与用户组的绑定关系同步到帆软系统
     */
    void syncFrUserGroupRelation();

    /**
     * 同步帆软用户组
     * 
     * 将用户组信息同步到帆软系统
     */
    void syncFrUserGroup();

    /**
     * 同步StarRocks用户组关系
     * 
     * 将用户与用户组的绑定关系同步到StarRocks系统
     */
    void syncStarRocksUserGroupRelation();

    /**
     * 同步StarRocks用户组
     * 
     * 将用户组信息同步到StarRocks系统
     */
    void syncStarRocksUserGroup();
}
