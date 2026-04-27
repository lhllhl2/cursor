package com.jasolar.mis.module.system.service.admin.project;

import com.jasolar.mis.framework.common.pojo.PageParam;
import com.jasolar.mis.framework.common.pojo.PageResult;
import com.jasolar.mis.module.system.resp.ProjectUserGroupResp;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

/**
 * Description:
 * Author : Zhou Hai
 * Date : 30/12/2025 17:20
 * Version : 1.0
 */
public interface SystemUserGroupProjectRService {

    List<Long> getGroupIdsByProjectId(Long projectId);

    /**
     * 导出
     * @param response
     */
    void exportExcel(HttpServletResponse response) throws Exception;


    /**
     * 分页查询
     * @param pageParam
     * @return
     */
    PageResult<ProjectUserGroupResp> searchPage(PageParam pageParam);


    /**
     * 导入
     * @param file
     */
    String importExcel(MultipartFile file) throws IOException;
}
