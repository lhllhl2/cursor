package com.jasolar.mis.module.system.service.oauth;

import cn.hutool.jwt.JWTUtil;
import com.jasolar.mis.framework.common.exception.ServiceException;
import com.jasolar.mis.framework.common.exception.enums.AuthErrorCodeConstants;
import com.jasolar.mis.framework.redis.util.RedisUtils;
import com.jasolar.mis.module.system.constant.AuthConstant;
import com.jasolar.mis.module.system.domain.admin.user.SystemUserDo;
import com.jasolar.mis.module.system.oauth.UserResponse;
import com.jasolar.mis.module.system.oauth.VerifyRequest;
import com.jasolar.mis.module.system.oauth.VerifyResponse;
import com.jasolar.mis.module.system.properties.JwtProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Objects;

/**
 * Description:
 * Author : Zhou Hai
 * Date : 22/07/2025 16:28
 * Version : 1.0
 */
@Slf4j
@Service
public class SysAuthServiceImpl implements SysAuthService{

    @Autowired
    private JwtProperties jwtProperties;
    @Override
    public VerifyResponse verify(VerifyRequest verifyRequest, String token) {

        // token,session 校验
        boolean verify = JWTUtil.verify(token, jwtProperties.getTokenKey().getBytes());
        if(!verify){
            throw new ServiceException(AuthErrorCodeConstants.TOKEN_VERIFY_ERROR);
        }
        String userId = JWTUtil.parseToken(token)
                .getPayload("userId")
                .toString();

        log.info("verify userId ---> {} ",userId);

        SystemUserDo systemUserDo = RedisUtils.get(AuthConstant.USER_INFO_KEY_PRE + userId, SystemUserDo.class);



        if(Objects.isNull(systemUserDo)){
            throw new ServiceException(AuthErrorCodeConstants.AUTH_EXPIRE);
        }
        UserResponse userResponse = UserResponse.builder()
                .id(systemUserDo.getId())
                .userName(systemUserDo.getUserName())
                .displayName(systemUserDo.getDisplayName())
                .build();

        return VerifyResponse.builder()
                .success(true)
                .user(userResponse)
                .build();
    }
}
