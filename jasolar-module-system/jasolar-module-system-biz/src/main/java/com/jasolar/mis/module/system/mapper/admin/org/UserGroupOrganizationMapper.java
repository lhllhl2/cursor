package com.jasolar.mis.module.system.mapper.admin.org;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.jasolar.mis.framework.mybatis.core.mapper.BaseMapperX;
import com.jasolar.mis.module.system.domain.admin.org.UserGroupOrganizationDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.util.List;

/**
 * 用户组组织关系 Mapper
 *
 * @author jasolar
 */
@Mapper
public interface UserGroupOrganizationMapper extends BaseMapperX<UserGroupOrganizationDO> {

    /**
     * 根据组织ID查询用户组ID列表
     * @param orgId 组织ID
     * @return 用户组ID列表
     */
    default List<Long> getGroupIdsByOrgId(@Param("orgId") Long orgId) {
        LambdaQueryWrapper<UserGroupOrganizationDO> wrapper = new LambdaQueryWrapper<UserGroupOrganizationDO>()
                .eq(UserGroupOrganizationDO::getOrganizationId, orgId)
                .select(UserGroupOrganizationDO::getUserGroupId);
        
        return selectObjs(wrapper).stream()
                .map(obj -> {
                    if (obj instanceof BigDecimal) {
                        return ((BigDecimal) obj).longValue();
                    } else if (obj instanceof Number) {
                        return ((Number) obj).longValue();
                    }
                    return (Long) obj;
                })
                .toList();
    }

} 