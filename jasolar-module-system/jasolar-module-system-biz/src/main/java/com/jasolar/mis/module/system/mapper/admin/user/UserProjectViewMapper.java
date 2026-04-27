package com.jasolar.mis.module.system.mapper.admin.user;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.jasolar.mis.framework.common.pojo.PageParam;
import com.jasolar.mis.framework.common.pojo.PageResult;
import com.jasolar.mis.framework.mybatis.core.mapper.BaseMapperX;
import com.jasolar.mis.module.system.domain.admin.user.UserProjectView;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 用户项目关联视图 Mapper
 * 
 * @author jasolar
 */
@Mapper
public interface UserProjectViewMapper extends BaseMapperX<UserProjectView> {

    /**
     * 根据用户名查询
     * 
     * @param userName 用户名
     * @return 用户项目关联列表
     */
    default List<UserProjectView> selectByUserName(String userName) {
        LambdaQueryWrapper<UserProjectView> wrapper = new LambdaQueryWrapper<UserProjectView>()
                .eq(UserProjectView::getUserName, userName);
        return selectList(wrapper);
    }

    /**
     * 根据项目编码查询
     * 
     * @param projectCode 项目编码
     * @return 用户项目关联列表
     */
    default List<UserProjectView> selectByProjectCode(String projectCode) {
        LambdaQueryWrapper<UserProjectView> wrapper = new LambdaQueryWrapper<UserProjectView>()
                .eq(UserProjectView::getProjectCode, projectCode);
        return selectList(wrapper);
    }

    /**
     * 根据用户名和项目编码查询
     * 
     * @param userName 用户名
     * @param projectCode 项目编码
     * @return 用户项目关联列表
     */
    default List<UserProjectView> selectByUserNameAndProjectCode(String userName, String projectCode) {
        LambdaQueryWrapper<UserProjectView> wrapper = new LambdaQueryWrapper<UserProjectView>()
                .eq(UserProjectView::getUserName, userName)
                .eq(UserProjectView::getProjectCode, projectCode);
        return selectList(wrapper);
    }

    /**
     * 根据用户名列表查询
     * 
     * @param userNames 用户名列表
     * @return 用户项目关联列表
     */
    default List<UserProjectView> selectByUserNames(List<String> userNames) {
        LambdaQueryWrapper<UserProjectView> wrapper = new LambdaQueryWrapper<UserProjectView>()
                .in(UserProjectView::getUserName, userNames);
        return selectList(wrapper);
    }

    /**
     * 根据项目编码列表查询
     * 
     * @param projectCodes 项目编码列表
     * @return 用户项目关联列表
     */
    default List<UserProjectView> selectByProjectCodes(List<String> projectCodes) {
        LambdaQueryWrapper<UserProjectView> wrapper = new LambdaQueryWrapper<UserProjectView>()
                .in(UserProjectView::getProjectCode, projectCodes);
        return selectList(wrapper);
    }

    /**
     * 分页查询用户项目关联视图数据
     * 
     * @param pageParam 分页参数
     * @param queryWrapper 查询条件
     * @return 分页结果
     */
    default PageResult<UserProjectView> selectPage(PageParam pageParam, LambdaQueryWrapper<UserProjectView> queryWrapper) {
        com.baomidou.mybatisplus.extension.plugins.pagination.Page<UserProjectView> mpPage = 
            new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(pageParam.getPageNo(), pageParam.getPageSize());
        com.baomidou.mybatisplus.extension.plugins.pagination.Page<UserProjectView> pageResult = this.selectPage(mpPage, queryWrapper);
        return new PageResult<>(pageResult.getRecords(), pageResult.getTotal());
    }
}

