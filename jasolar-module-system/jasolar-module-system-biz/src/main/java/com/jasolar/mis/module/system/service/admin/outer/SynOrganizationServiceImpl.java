package com.jasolar.mis.module.system.service.admin.outer;

import com.jasolar.mis.framework.common.exception.ServiceException;
import com.jasolar.mis.module.system.controller.admin.outer.resp.EsbInfoResp;
import com.jasolar.mis.module.system.controller.admin.outer.vo.synorg.OrgExtendFieldVo;
import com.jasolar.mis.module.system.controller.admin.outer.vo.synorg.OrgParams;
import com.jasolar.mis.module.system.controller.admin.outer.vo.synorg.OrgRequestInfoVo;
import com.jasolar.mis.module.system.domain.admin.org.SystemOrgDO;
import com.jasolar.mis.module.system.exceptioncode.OrgErrorCodeConstants;
import com.jasolar.mis.module.system.mapper.admin.org.SystemOrgMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Objects;

/**
 * Description:
 * Author : Zhou Hai
 * Date : 07/08/2025 15:48
 * Version : 1.0
 */
@Service
public class SynOrganizationServiceImpl implements SynOrganizationService{


    @Autowired
    private SystemOrgMapper systemOrgMapper;

    @Override
    public EsbInfoResp add(OrgParams orgParams) {
        String id = orgParams.getRequestInfo().getOrganizationUuid();
        SystemOrgDO systemOrgDO = systemOrgMapper.selectById(id);
        if(Objects.nonNull(systemOrgDO)){
            throw new ServiceException(OrgErrorCodeConstants.ORG_ALREADY_EXISTS);
        }

        OrgRequestInfoVo orgInfo = orgParams.getRequestInfo();
        OrgExtendFieldVo ex = orgInfo.getExtendFields();
        boolean exHas = Objects.nonNull(ex);

        systemOrgDO = SystemOrgDO.builder()
                .id(orgInfo.getOrganizationUuid())
                .orgName(orgInfo.getOrganization())
                .parentId(orgInfo.getParentUuid())
                .rootNode(orgInfo.getRootNode() != null && orgInfo.getRootNode() ? "1" :"0")
                .companyName(exHas ? ex.getCompanyName() : null)
                .companyCode(exHas ? ex.getCompanyCode() : null)
                .costOrgName(exHas? ex.getCostName() : null)
                .orgHeadId(exHas ? ex.getOrgHeadId() : null)
                .orgFgId(exHas ? ex.getOrgFgId() : null)
                .orgAttribute(exHas ? ex.getBmsx() : null)
                .orgFullPath(exHas ? ex.getOrgFullPath() : null)
                .build();

        systemOrgMapper.insert(systemOrgDO);


        return EsbInfoResp.builder()
                .requestTime(orgParams.getEsbInfo().getRequestTime())
                .instId(orgParams.getEsbInfo().getInstId())
                .returnCode("A0001-PORTAL")
                .returnMsg("组织新增成功")
                .responseTime(new Date())
                .returnStatus("S")
                .build();
    }


    @Override
    public EsbInfoResp update(OrgParams orgParams) {
        String id = orgParams.getRequestInfo().getOrganizationUuid();
        SystemOrgDO systemOrgDO = systemOrgMapper.selectById(id);
        if(Objects.isNull(systemOrgDO)){
            return returnEsInfo(orgParams.getEsbInfo().getRequestTime(),
                    orgParams.getEsbInfo().getInstId(),
                    "A0002",
                    "组织不存在"
            );
        }
        OrgRequestInfoVo orgInfo = orgParams.getRequestInfo();
        OrgExtendFieldVo ex = orgInfo.getExtendFields();
        boolean exHas = Objects.nonNull(ex);

        systemOrgDO = SystemOrgDO.builder()
                .id(orgInfo.getOrganizationUuid())
                .orgName(orgInfo.getOrganization())
                .parentId(orgInfo.getParentUuid())
                .companyName(exHas ? ex.getCompanyName() : null)
                .companyCode(exHas ? ex.getCompanyCode() : null)
                .costOrgName(exHas ? ex.getCostName() : null)
                .orgHeadId(exHas ? ex.getOrgHeadId() : null)
                .orgFgId(exHas ? ex.getOrgFgId() : null)
                .orgAttribute(exHas ? ex.getBmsx() : null)
                .orgFullPath(exHas ? ex.getOrgFullPath() : null)
                .build();
        systemOrgMapper.updateById(systemOrgDO);


        return EsbInfoResp.builder()
                .requestTime(orgParams.getEsbInfo().getRequestTime())
                .instId(orgParams.getEsbInfo().getInstId())
                .returnCode("A0001-PORTAL")
                .returnMsg("组织修改成功")
                .responseTime(new Date())
                .returnStatus("S")
                .build();
    }


    @Override
    public EsbInfoResp deleted(OrgParams orgParams) {
        String id = orgParams.getRequestInfo().getOrganizationUuid();
        SystemOrgDO systemOrgDO = systemOrgMapper.selectById(id);
        if(Objects.isNull(systemOrgDO)){
            return returnEsInfo(orgParams.getEsbInfo().getRequestTime(),
                    orgParams.getEsbInfo().getInstId(),
                    "A0002",
                        "组织不存在"
                    );
        }
        systemOrgMapper.deleteById(systemOrgDO);


        return returnEsInfo(orgParams.getEsbInfo().getRequestTime(),
                orgParams.getEsbInfo().getInstId(),
                "A0001",
                "组织删除成功"
        );
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
}
