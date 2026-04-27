package com.jasolar.mis.module.system.service.oauth;

import cn.hutool.jwt.JWT;
import com.jasolar.mis.framework.common.exception.ServiceException;
import com.jasolar.mis.framework.common.exception.enums.UserErrorConstants;
import com.jasolar.mis.framework.redis.util.RedisUtils;
import com.jasolar.mis.framework.web.core.util.WebFrameworkUtils;
import com.jasolar.mis.module.system.constant.AuthConstant;
import com.jasolar.mis.module.system.controller.oauth.vo.LocalLoginVo;
import com.jasolar.mis.module.system.domain.admin.log.SystemLogDo;
import com.jasolar.mis.module.system.domain.admin.user.SystemUserDo;
import com.jasolar.mis.module.system.enums.UserEnums;
import com.jasolar.mis.module.system.mapper.admin.log.SystemLogMapper;
import com.jasolar.mis.module.system.mapper.admin.user.SystemUserMapper;
import com.jasolar.mis.module.system.oauth.LocalLoginResponse;
import com.jasolar.mis.module.system.properties.JwtProperties;
import com.jasolar.mis.module.system.properties.SessionTimeProperties;
import com.jasolar.mis.module.system.util.BCryptUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Objects;

/**
 * Description:
 * Author : Zhou Hai
 * Date : 05/09/2025 10:09
 * Version : 1.0
 */
@Slf4j
@Service
public class LocalLoginServiceImpl implements LocalLoginService {

    @Autowired
    private SystemUserMapper systemUserMapper;

    @Autowired
    private JwtProperties jwtProperties;

    @Autowired
    private SessionTimeProperties timeProperties;

    @Autowired
    private SystemLogMapper systemLogMapper;


    @Autowired
    private ThreadPoolTaskExecutor taskExecutor;




    @Override
    public LocalLoginResponse localLogin(LocalLoginVo localLoginVo) {

        SystemUserDo systemUserDo = systemUserMapper.getByUserName(localLoginVo.getUserName());
        if(Objects.isNull(systemUserDo)){
            throw new ServiceException(UserErrorConstants.USER_NO_EXISTS);
        }
        if(Objects.equals(systemUserDo.getStatus(), UserEnums.Status.BAN.getCode())){
            throw new ServiceException(UserErrorConstants.USER_HAS_BAN);
        }
        boolean matches = BCryptUtil.matches(localLoginVo.getPwd(), systemUserDo.getPwd());
        if(!matches){
            throw new ServiceException(UserErrorConstants.USER_NAME_OR_PWD_ERR);
        }

        boolean needChange = Objects.equals(systemUserDo.getPwdChanged(), UserEnums.PwdChanged.NO.getCode());

        String token = getToken(systemUserDo);
        setLog(systemUserDo);
        return LocalLoginResponse.builder()
                .token(token)
                .expiresIn(timeProperties.getExpirePeriod() * 60 * 60 * 1000)
                .type(timeProperties.getType())
                .needChanged(needChange)
                .build();
    }


    private String getToken(SystemUserDo systemUserDo){
        Date date = new Date();

        Integer expirePeriod = timeProperties.getExpirePeriod();

        long expires = date.getTime() + expirePeriod * 60 * 60 * 1000;

        // 生成token
        String jwt = JWT.create()
                .setSubject(jwtProperties.getSubject())
                .setPayload("userId",systemUserDo.getId())
                .setPayload("userName", systemUserDo.getUserName())
                .setPayload("displayName", systemUserDo.getDisplayName())
                .setIssuedAt(date)
                .setExpiresAt(new Date(expires))
                .setKey(jwtProperties.getTokenKey().getBytes())
                .sign();

        String userInfoKey =  AuthConstant.USER_INFO_KEY_PRE + systemUserDo.getId();

        // 将用户信息存储到redis
        RedisUtils.set(userInfoKey,systemUserDo,expirePeriod * 60 * 60);

        return jwt;

    }

    private void setLog(SystemUserDo systemUserDo){
        String clientIP = WebFrameworkUtils.getClientIP(WebFrameworkUtils.getRequest());
        taskExecutor.execute(() -> {
            SystemLogDo login = SystemLogDo.builder()
                    .userName(systemUserDo.getUserName())
                    .displayName(systemUserDo.getDisplayName())
                    .ip(clientIP)
                    .logType("LOGIN")
                    .build();
            systemLogMapper.insert(login);

            log.info("写入登录日志成功：{}",login.toString());

        });
    }


    @Override
    public void localLogout() {
        String userInfoKey =  AuthConstant.USER_INFO_KEY_PRE + WebFrameworkUtils.getLoginUserId();
        RedisUtils.del(userInfoKey);
    }
}
