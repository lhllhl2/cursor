package com.jasolar.mis.module.system.service.admin.outer;

import com.jasolar.mis.module.system.controller.admin.outer.resp.EsbInfoResp;
import com.jasolar.mis.module.system.controller.admin.outer.vo.synuser.ExtendFieldVo;
import com.jasolar.mis.module.system.controller.admin.outer.vo.synuser.RequestInfoVo;
import com.jasolar.mis.module.system.controller.admin.outer.vo.synuser.SynUserParams;
import com.jasolar.mis.module.system.domain.admin.user.SystemUserDo;
import com.jasolar.mis.module.system.enums.UserEnums;
import com.jasolar.mis.module.system.mapper.admin.org.SystemUserOrgRMapper;
import com.jasolar.mis.module.system.mapper.admin.user.SystemUserMapper;
import com.jasolar.mis.module.system.properties.IDaaSProperties;
import com.jasolar.mis.module.system.util.BCryptUtil;
import org.springframework.beans.factory.NoUniqueBeanDefinitionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.yaml.snakeyaml.events.Event;

import java.util.Date;
import java.util.Objects;

/**
 * Description:
 * Author : Zhou Hai
 * Date : 06/08/2025 17:47
 * Version : 1.0
 */
@Service
public class SynUserServiceImpl implements SynUserService {


    @Autowired
    private SystemUserMapper systemUserMapper;

    @Autowired
    private SystemUserOrgRMapper userOrgRMapper;

    @Autowired
    private IDaaSProperties idaSProperties;

    @Transactional
    @Override
    public EsbInfoResp sysAddUser(SynUserParams synUserParams) {
        String userName = synUserParams.getRequestInfo().getUserName();
        SystemUserDo userDo = systemUserMapper.getByUserName(userName);
        if(Objects.nonNull(userDo)){
            return returnEsInfo(synUserParams.getEsbInfo().getRequestTime(),
                    synUserParams.getEsbInfo().getInstId(),
                    "A0003",
                    "用户已存在"
            );
        }

        RequestInfoVo requestInfo = synUserParams.getRequestInfo();
        ExtendFieldVo ex = requestInfo.getExtendField();
        boolean exHas = Objects.nonNull(ex);

        Integer gender = exHas ? ex.getGender() : null;
        String initPwd = BCryptUtil.encode(idaSProperties.getInit());

        userDo = SystemUserDo.builder()
                .userName(requestInfo.getUserName())
                .displayName(requestInfo.getDisplayName())
                .gender(Objects.isNull(gender) ? null: String.valueOf(gender))
                .email(requestInfo.getEmails())
                .phoneRegion(requestInfo.getPhoneRegion())
                .phoneNumber(requestInfo.getPhoneNumbers())
                .status(UserEnums.Status.RUN.getCode())
                .directManagerCode(exHas? ex.getDirectManagerEmpoyeeId() : null)
                .cardNo(exHas ?  ex.getCardNo() : null)
                .cardType(exHas ? ex.getCardType() : null)
                .companyCode(exHas ? ex.getCompanyCode() : null)
                .birthday(exHas ? ex.getBirthday() : null)
                .officeLocation(exHas ? ex.getOfficeLocation() : null)
                .post(exHas ? ex.getPost() : null)
                .inductionDate(exHas ? ex.getInductionDate() : null)
                .leaveDate(exHas ? ex.getLeavingDate() : null)
                .pwd(initPwd)
                .build();

        systemUserMapper.insert(userDo);

        return EsbInfoResp.builder()
                .requestTime(synUserParams.getEsbInfo().getRequestTime())
                .instId(synUserParams.getEsbInfo().getInstId())
                .returnCode("A0001")
                .returnMsg("用户增加成功")
                .responseTime(new Date())
                .returnStatus("S")
                .build();
    }


    @Override
    public EsbInfoResp sysUpdateUser(SynUserParams synUserParams) {
        String userName = synUserParams.getRequestInfo().getUserName();
        SystemUserDo userDo = systemUserMapper.getByUserName(userName);
        if(Objects.isNull(userDo)){
            return returnEsInfo(synUserParams.getEsbInfo().getRequestTime(),
                    synUserParams.getEsbInfo().getInstId(),
                    "A0002",
                    "用户不存在"
                    );
        }
        RequestInfoVo requestInfo = synUserParams.getRequestInfo();

        ExtendFieldVo ex = requestInfo.getExtendField();
        boolean exHas = Objects.nonNull(ex);
        Integer gender = exHas ? ex.getGender() : null;

        // 修改
        userDo.setUserName(requestInfo.getUserName());
        userDo.setDisplayName(requestInfo.getDisplayName());
        userDo.setGender(Objects.isNull(gender) ? null: String.valueOf(gender));
        userDo.setEmail(requestInfo.getEmails());
        userDo.setPhoneRegion(requestInfo.getPhoneRegion());
        userDo.setPhoneNumber(requestInfo.getPhoneNumbers());
        userDo.setStatus(UserEnums.Status.RUN.getCode());
        userDo.setDirectManagerCode(exHas ? ex.getDirectManagerEmpoyeeId() : null);
        userDo.setCardNo(exHas ? ex.getCardNo() : null);
        userDo.setCardType(exHas ? ex.getCardType() : null);
        userDo.setCompanyCode(exHas ? ex.getCompanyCode() : null);
        userDo.setBirthday(exHas ? ex.getBirthday() : null);
        userDo.setOfficeLocation(exHas ? ex.getOfficeLocation() : null);
        userDo.setPost(exHas ? ex.getPost() : null);
        userDo.setInductionDate(exHas ? ex.getInductionDate() : null);
        userDo.setLeaveDate(exHas ? ex.getLeavingDate() : null);

        systemUserMapper.updateById(userDo);
        return returnEsInfo(synUserParams.getEsbInfo().getRequestTime(),
                synUserParams.getEsbInfo().getInstId(),
                "A0001",
                "用户修改成功");
    }


    private EsbInfoResp returnEsInfo(String requestTime,String instId,String code,String msg ){
       return EsbInfoResp.builder()
                .requestTime(requestTime)
                .instId(instId)
                .returnCode(code)
                .returnMsg(msg)
                .responseTime(new Date())
                .returnStatus("S")
                .build();
    }


    @Transactional
    @Override
    public EsbInfoResp delete(SynUserParams synUserParams) {
        String userName = synUserParams.getRequestInfo().getUserName();
        SystemUserDo userDo = systemUserMapper.getByUserName(userName);
        if(Objects.isNull(userDo)){
            return returnEsInfo(synUserParams.getEsbInfo().getRequestTime(),
                    synUserParams.getEsbInfo().getInstId(),
                    "A0002",
                    "用户不存在"
            );
        }
        systemUserMapper.deleteById(userDo);

        return returnEsInfo(synUserParams.getEsbInfo().getRequestTime(),
                synUserParams.getEsbInfo().getInstId(),
                "A0001",
                "用户删除成功");
    }
}
