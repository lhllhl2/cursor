package com.jasolar.mis.module.system.service.ehr;

import com.jasolar.mis.framework.common.pojo.PageResult;
import com.jasolar.mis.module.system.controller.ehr.vo.ProjectControlRExcelVO;
import com.jasolar.mis.module.system.controller.ehr.vo.ProjectControlRSearchVo;
import com.jasolar.mis.module.system.domain.ehr.ProjectControlR;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.multipart.MultipartFile;

/**
 * ProjectControlR Service 接口
 */
public interface ProjectControlRService {
    
    /**
     * 分页查询ProjectControlR数据
     * @param searchVo 查询参数
     * @return 分页结果
     */
    PageResult<ProjectControlR> searchPage(ProjectControlRSearchVo searchVo);
    
    /**
     * 导出ProjectControlR数据到Excel
     * @param searchVo 查询参数
     * @param response HTTP响应对象
     * @throws Exception 导出异常
     */
    void exportExcel(ProjectControlRSearchVo searchVo, HttpServletResponse response) throws Exception;
    
    /**
     * 从Excel导入ProjectControlR数据并批量更新
     * @param file Excel文件
     * @return 导入结果信息
     */
    String importExcelAndUpdate(MultipartFile file) throws Exception;
    /**
     * 项目数据同步任务
     */
    void syncProjectToBusiness();


    /**
     * 修改项目控制等级
     * @param projectControlR
     */
    void changeControlLevel(ProjectControlR projectControlR);

}