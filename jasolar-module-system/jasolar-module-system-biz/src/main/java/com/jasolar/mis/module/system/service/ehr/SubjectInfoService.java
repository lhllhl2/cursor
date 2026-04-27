package com.jasolar.mis.module.system.service.ehr;

import com.jasolar.mis.framework.common.pojo.PageResult;
import com.jasolar.mis.module.system.controller.ehr.vo.ErpUnmappedAccountVO;
import com.jasolar.mis.module.system.controller.ehr.vo.SubjectInfoSearchVo;
import com.jasolar.mis.module.system.domain.ehr.SubjectInfo;
import com.jasolar.mis.module.system.domain.ehr.SubjectInfoControlLevelView;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * ProjectInfo Service 接口
 */
public interface SubjectInfoService {

    /**
     * 分页查询项目信息数据
     * @param searchVo 查询参数
     * @return 分页结果
     */
    PageResult<SubjectInfoControlLevelView> searchPage(SubjectInfoSearchVo searchVo);

    /**
     * 导出项目信息数据到Excel
     * @param searchVo 查询参数
     * @param response HTTP响应对象
     * @throws Exception 导出异常
     */
    void exportExcel(SubjectInfoSearchVo searchVo, HttpServletResponse response) throws Exception;

    /**
     * 从Excel导入项目信息数据并批量更新
     * @param file Excel文件
     * @return 导入结果信息
     */
    String importExcelAndUpdate(MultipartFile file) throws Exception;

    /**
     * 从Excel导入 ERP 科目映射并更新 SUBJECT_INFO 中的 ERP_ACCT_CD、ERP_ACCT_NM
     * @param file Excel 文件
     * @return 导入结果信息
     */
    String importErpAcctFromExcel(MultipartFile file) throws Exception;

    /**
     * 科目数据同步任务
     */
    void syncSubjectToBusiness();

    /**
     * 修改控制层级
     * @param subjectInfo
     */
    void changeControlLevel(SubjectInfo subjectInfo);

    /**
     * 查询 ERP 视图中未在 SUBJECT_INFO.ERP_ACCT_CD 中维护的科目
     */
    List<ErpUnmappedAccountVO> listUnmappedErpAccounts();
}