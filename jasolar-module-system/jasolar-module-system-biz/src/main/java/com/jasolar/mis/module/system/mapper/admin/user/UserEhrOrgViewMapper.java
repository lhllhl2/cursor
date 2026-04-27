package com.jasolar.mis.module.system.mapper.admin.user;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.jasolar.mis.framework.common.pojo.PageParam;
import com.jasolar.mis.framework.common.pojo.PageResult;
import com.jasolar.mis.framework.mybatis.core.mapper.BaseMapperX;
import com.jasolar.mis.module.system.domain.admin.user.UserEhrOrgView;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 用户EHR组织关联视图 Mapper
 * 
 * @author jasolar
 */
@Mapper
public interface UserEhrOrgViewMapper extends BaseMapperX<UserEhrOrgView> {

    /**
     * 根据用户名查询
     * 
     * @param userName 用户名
     * @return 用户EHR组织关联列表
     */
    default List<UserEhrOrgView> selectByUserName(String userName) {
        LambdaQueryWrapper<UserEhrOrgView> wrapper = new LambdaQueryWrapper<UserEhrOrgView>()
                .eq(UserEhrOrgView::getUserName, userName);
        return selectList(wrapper);
    }

    /**
     * 根据管理组织编码查询
     * 
     * @param morgCode 管理组织编码
     * @return 用户EHR组织关联列表
     */
    default List<UserEhrOrgView> selectByMorgCode(String morgCode) {
        LambdaQueryWrapper<UserEhrOrgView> wrapper = new LambdaQueryWrapper<UserEhrOrgView>()
                .eq(UserEhrOrgView::getMorgCode, morgCode);
        return selectList(wrapper);
    }

    /**
     * 根据EHR组织编码查询
     * 
     * @param ehrCd EHR组织编码
     * @return 用户EHR组织关联列表
     */
    default List<UserEhrOrgView> selectByEhrCd(String ehrCd) {
        LambdaQueryWrapper<UserEhrOrgView> wrapper = new LambdaQueryWrapper<UserEhrOrgView>()
                .eq(UserEhrOrgView::getEhrCd, ehrCd);
        return selectList(wrapper);
    }

    /**
     * 根据控制层级1的EHR组织编码查询
     * 
     * @param controlEhrCd 控制层级1的EHR组织编码
     * @return 用户EHR组织关联列表
     */
    default List<UserEhrOrgView> selectByControlEhrCd(String controlEhrCd) {
        LambdaQueryWrapper<UserEhrOrgView> wrapper = new LambdaQueryWrapper<UserEhrOrgView>()
                .eq(UserEhrOrgView::getControlEhrCd, controlEhrCd);
        return selectList(wrapper);
    }

    /**
     * 根据用户名和管理组织编码查询
     * 
     * @param userName 用户名
     * @param morgCode 管理组织编码
     * @return 用户EHR组织关联列表
     */
    default List<UserEhrOrgView> selectByUserNameAndMorgCode(String userName, String morgCode) {
        LambdaQueryWrapper<UserEhrOrgView> wrapper = new LambdaQueryWrapper<UserEhrOrgView>()
                .eq(UserEhrOrgView::getUserName, userName)
                .eq(UserEhrOrgView::getMorgCode, morgCode);
        return selectList(wrapper);
    }

    /**
     * 分页查询用户EHR组织关联视图数据
     * 
     * @param pageParam 分页参数
     * @param queryWrapper 查询条件
     * @return 分页结果
     */
    default PageResult<UserEhrOrgView> selectPage(PageParam pageParam, LambdaQueryWrapper<UserEhrOrgView> queryWrapper) {
        com.baomidou.mybatisplus.extension.plugins.pagination.Page<UserEhrOrgView> mpPage = 
            new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(pageParam.getPageNo(), pageParam.getPageSize());
        com.baomidou.mybatisplus.extension.plugins.pagination.Page<UserEhrOrgView> pageResult = this.selectPage(mpPage, queryWrapper);
        return new PageResult<>(pageResult.getRecords(), pageResult.getTotal());
    }
}

