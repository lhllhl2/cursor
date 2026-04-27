package com.jasolar.mis.module.system.service.ehr;

import com.jasolar.mis.framework.common.pojo.PageResult;
import com.jasolar.mis.module.system.controller.budget.vo.EhrDetailResultVo;
import com.jasolar.mis.module.system.controller.ehr.vo.BudgetOrgSearchVo;
import com.jasolar.mis.module.system.controller.ehr.vo.EhrOrgManageRExcelVO;
import com.jasolar.mis.module.system.controller.ehr.vo.EhrSearchVo;
import com.jasolar.mis.module.system.domain.ehr.EhrOrgManageR;
import com.jasolar.mis.module.system.resp.BudgetOrgResp;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * Description:
 * Author : Zhou Hai
 * Date : 09/12/2025 14:57
 * Version : 1.0
 */
public interface EhrOrgManageRService {
    
    /**
     * 分页查询EHR组织管理数据
     * @param searchVo 查询参数
     * @return 分页结果
     */
    PageResult<EhrOrgManageR> searchPage(EhrSearchVo searchVo);
    

    /**
     * 导出EHR组织管理数据到Excel
     * @param searchVo 查询参数
     * @param response HTTP响应对象
     * @throws Exception 导出异常
     */
    void exportExcel(EhrSearchVo searchVo, HttpServletResponse response) throws Exception;
    
    /**
     * 从Excel导入EHR组织管理数据并批量更新
     * @param file Excel文件
     * @return 导入结果信息
     */
    String importExcelAndUpdate(MultipartFile file) throws Exception;

    /**
     * 全部明细查询
     * @return
     */
    List<EhrDetailResultVo> queryAllDetail();

    /**
     * 修改单个组织信息
     * @param ehrOrgManageR
     */
    void changeSingle(EhrOrgManageR ehrOrgManageR);

    /**
     * 修改 控制层级
     * @param ehrOrgManageR
     */
    void changeControlLevel(EhrOrgManageR ehrOrgManageR);

    /**
     * 修改 编制层级
     * @param ehrOrgManageR
     */
    void changeBzLevel(EhrOrgManageR ehrOrgManageR);

    /**
     * 修改 ERP部门
     * @param ehrOrgManageR 至少包含 id、erpDepart
     */
    void changeErpDepart(EhrOrgManageR ehrOrgManageR);

    /**
     * 查询所有的预算组织
     * @return
     */
    List<BudgetOrgResp> getBudgetOrg();

    /**
     *  SYSTEM_ORG to EHR_ORG_MANAGE_R
     */
    void syncProjectToBusiness();


}