package com.jasolar.mis.module.system.service.oauth;

import com.jasolar.mis.module.system.mapper.admin.outer.OutClientInfoMapper;
import com.jasolar.mis.module.system.oauth.ClientInfoResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Description:
 * Author : Zhou Hai
 * Date : 04/11/2025 14:28
 * Version : 1.0
 */
@Service
public class OutClientInfoServiceImpl implements OutClientInfoService{

    @Autowired
    private OutClientInfoMapper outClientInfoMapper;

    @Override
    public ClientInfoResponse getClientInfo(String clientName) {
        return outClientInfoMapper.getClientInfo(clientName);
    }
}
