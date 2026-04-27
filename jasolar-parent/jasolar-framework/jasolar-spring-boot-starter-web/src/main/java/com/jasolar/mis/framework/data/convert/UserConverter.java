package com.jasolar.mis.framework.data.convert;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

import com.jasolar.mis.framework.common.security.LoginUser;
import com.jasolar.mis.framework.data.core.User;
import com.jasolar.mis.module.system.api.user.dto.AdminUserRespDTO;

/**
 * 
 * 人员信息的数据转换
 * 
 * @author galuo
 * @date 2025-03-28 16:15
 *
 */
@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UserConverter {

    /** 默认的实例 */
    UserConverter INSTANCE = Mappers.getMapper(UserConverter.class);

    /**
     * 将查询对象转换为缓存对象
     * 
     * @param dto 查询到的DTO
     * @return 缓存对象
     */
    @Mapping(source = "username", target = "userNo")
    @Mapping(source = "nickname", target = "userName")
    User convert(AdminUserRespDTO dto);

    /**
     * 登录人信息转换为用户
     * 
     * @param loginUser
     * @return
     */
    @Mapping(source = "no", target = "userNo")
    @Mapping(source = "name", target = "userName")
    @Mapping(source = "user.deptCode", target = "deptCode")
    @Mapping(source = "user.legalCode", target = "legalCode")
    @Mapping(source = "user.businessUnitCode", target = "businessUnitCode")
    @Mapping(source = "user.businessGroupCode", target = "businessGroupCode")
    User convert(LoginUser loginUser);

}
