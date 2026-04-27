package com.jasolar.mis.module.system.service.oauth;

import cn.hutool.core.net.URLEncodeUtil;
import cn.hutool.http.HttpStatus;
import cn.hutool.jwt.JWT;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.jasolar.mis.framework.common.exception.ServiceException;
import com.jasolar.mis.framework.common.exception.enums.AuthErrorCodeConstants;
import com.jasolar.mis.framework.common.exception.enums.UserErrorConstants;
import com.jasolar.mis.framework.common.util.http.HttpUtils;
import com.jasolar.mis.framework.redis.util.RedisUtils;
import com.jasolar.mis.framework.web.core.util.WebFrameworkUtils;
import com.jasolar.mis.module.system.constant.AuthConstant;
import com.jasolar.mis.module.system.domain.admin.log.SystemLogDo;
import com.jasolar.mis.module.system.domain.admin.user.SystemUserDo;
import com.jasolar.mis.module.system.mapper.admin.log.SystemLogMapper;
import com.jasolar.mis.module.system.oauth.*;
import com.jasolar.mis.module.system.properties.IDaaSProperties;
import com.jasolar.mis.module.system.properties.JwtProperties;
import com.jasolar.mis.module.system.properties.SessionTimeProperties;
import com.jasolar.mis.module.system.service.admin.user.SystemUserService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import java.nio.charset.Charset;
import java.util.Date;
import java.util.Objects;

/**
 * Description:
 * Author : Zhou Hai
 * Date : 18/07/2025 11:00
 * Version : 1.0
 */

@Slf4j
@Service
public class OauthSsoServiceImpl implements OauthSsoService{

    @Autowired
    private IDaaSProperties iDaaSProperties;

    @Autowired
    private SystemUserService userService;

    @Autowired
    private JwtProperties jwtProperties;

    @Autowired
    private SessionTimeProperties timeProperties;

    @Autowired
    private SystemLogMapper systemLogMapper;


    @Autowired
    private ThreadPoolTaskExecutor taskExecutor;



    @Override
    public SsoLogInUrlInfo getSsoLoginRedirectUrl() {

        String url = iDaaSProperties.getSso().getAuthorizeUri() +
                "?" + "client_id=" + iDaaSProperties.getClientId() +
                "&" + "response_type=" + iDaaSProperties.getSso().getResponseType() +
                "&" + "redirect_uri=" + URLEncodeUtil.encode(iDaaSProperties.getSso().getSsoCallBackUri(), Charset.defaultCharset()) +
                "&" + "scope=" + iDaaSProperties.getSso().getScope();

        return SsoLogInUrlInfo
                .builder()
                .redirectUri(url)
                .build();
    }


    @Override
    public TokenResponse ssoCallBack(SsoCallbackParams ssoCallbackParams) {
        // 根据 code 获取 accessToken
        SsoAccessTokenResponse accessToken = getAccessToken(ssoCallbackParams.getCode());
        log.info("current_token --> {}",accessToken);

        // 获取 userInfo
        SsoUserInfoResponse oauthUserInfo = getOauthUserInfo(accessToken.getAccessToken());
        log.info("current_user --> {}",oauthUserInfo);

        // 用户本地登录并生成token
        return localLogin(oauthUserInfo, accessToken);
    }



    private SsoAccessTokenResponse getAccessToken(String code){

        String sb = "client_id=" + iDaaSProperties.getClientId() + "&" +
                "client_secret=" + iDaaSProperties.getClientSecret() + "&" +
                "grant_type=authorization_code" + "&" +
                "redirect_uri=" + iDaaSProperties.getSso().getTokenCallBackUri() + "&" +
                "code=" + code;

        log.info("get token url --> {}",iDaaSProperties.getSso().getGetTokenUri());
        log.info("get token body --> {}",sb);
        String res = HttpUtils.post(iDaaSProperties.getSso().getGetTokenUri(),
                null,
                sb,
                "application/x-www-form-urlencoded");
        if(Objects.isNull(res)){
            throw new ServiceException(AuthErrorCodeConstants.GET_OAUTH_TOKEN_ERROR);
        }
        JSONObject jo = JSONObject.parseObject(res);
        if(Objects.nonNull(jo.getString("error"))){
            throw new ServiceException(AuthErrorCodeConstants.GET_OAUTH_TOKEN_ERROR.getCode(),
                    jo.getString("error_description")
                    );
        }

        return JSON.parseObject(res, SsoAccessTokenResponse.class);
    }


    private SsoUserInfoResponse getOauthUserInfo(String accessToken){
        String url = iDaaSProperties.getSso().getGetUserinfoUri() + "?access_token=" + accessToken;
        String res = HttpUtils.get(url, null);
        if(Objects.isNull(res)){
            throw new ServiceException(AuthErrorCodeConstants.GET_OAUTH_TOKEN_ERROR);
        }
        JSONObject js = JSON.parseObject(res);
        if(!Objects.equals(js.getString("code"),"200")){
            throw new ServiceException(js.getString("code"),js.getString("message"));
        }
        return js.getObject("data", SsoUserInfoResponse.class);
    }



    private TokenResponse localLogin(SsoUserInfoResponse oauthUserInfo,SsoAccessTokenResponse accessToken){
        SystemUserDo systemUserDo = userService.getByUserName(oauthUserInfo.getUsername());
        if(Objects.isNull(systemUserDo)){
            throw new ServiceException(UserErrorConstants.USER_NO_EXISTS);
        }
        if(Objects.equals(systemUserDo.getStatus(),"0")){
            throw new ServiceException(UserErrorConstants.USER_HAS_BAN);
        }
        String token = getToken(systemUserDo, accessToken);

        // 日志记录
        setLog(systemUserDo);

        return TokenResponse.builder()
                .token(token)
                .expiresIn(timeProperties.getExpirePeriod() * 60 * 60 * 1000)
                .type(timeProperties.getType())
                .build();
    }


    private String getToken(SystemUserDo systemUserDo,SsoAccessTokenResponse accessToken){

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

        String userIDaaSKey = AuthConstant.USER_IDAAS_KEY_PRE + systemUserDo.getId();
        // 存储IDaaS token
        RedisUtils.set(userIDaaSKey,accessToken.getAccessToken(),accessToken.getExpiresIn());
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
    public void logout(HttpServletResponse response) {
        String userInfoKey =  AuthConstant.USER_INFO_KEY_PRE + WebFrameworkUtils.getLoginUserId();
        RedisUtils.del(userInfoKey);

        String userIDaaSKey = AuthConstant.USER_IDAAS_KEY_PRE + WebFrameworkUtils.getLoginUserId();
        String accessToken = RedisUtils.get(userIDaaSKey, String.class);
        RedisUtils.del(userIDaaSKey);

        ssoLoginOut(accessToken,response);


    }

    private void ssoLoginOut(String accessToken,HttpServletResponse response) {
        String logoutUrl = iDaaSProperties.getSso().getLogoutUri()
                + iDaaSProperties.getAppId()
                + "?access_token=" + accessToken;
        response.setStatus(HttpStatus.HTTP_UNAUTHORIZED);
        response.setHeader("Location",logoutUrl);
        response.setHeader("Access-Control-Expose-Head","Location");
    }






}
