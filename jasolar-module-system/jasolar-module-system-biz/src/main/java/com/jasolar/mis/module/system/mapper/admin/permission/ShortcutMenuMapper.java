package com.jasolar.mis.module.system.mapper.admin.permission;

import com.jasolar.mis.framework.common.pojo.PageResult;
import com.jasolar.mis.framework.mybatis.core.mapper.BaseMapperX;
import com.jasolar.mis.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.jasolar.mis.module.system.controller.admin.permission.vo.ShortcutMenuPageReqVO;
import com.jasolar.mis.module.system.domain.admin.permission.ShortcutMenuDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 用户快捷菜单 Mapper
 *
 * @author 管理员
 */
@Mapper
public interface ShortcutMenuMapper extends BaseMapperX<ShortcutMenuDO> {

    default PageResult<ShortcutMenuDO> selectPage(ShortcutMenuPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<ShortcutMenuDO>()
                .eqIfPresent(ShortcutMenuDO::getUserNo, reqVO.getUserNo())
                .eqIfPresent(ShortcutMenuDO::getUserId, reqVO.getUserId())
                .eqIfPresent(ShortcutMenuDO::getMenuId, reqVO.getMenuId())
                .eqIfPresent(ShortcutMenuDO::getSort, reqVO.getSort())
                .eqIfPresent(ShortcutMenuDO::getIsPinned, reqVO.getIsPinned())
                .betweenIfPresent(ShortcutMenuDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(ShortcutMenuDO::getId));
    }



    default List<ShortcutMenuDO> selectList(Long userId, String userNo) {
        return selectList( new LambdaQueryWrapperX<ShortcutMenuDO>()
                .eqIfPresent(ShortcutMenuDO::getUserNo, userNo)
                .eqIfPresent(ShortcutMenuDO::getUserId, userId)
                .orderByDesc(ShortcutMenuDO::getIsPinned)
                .orderByAsc(ShortcutMenuDO::getSort)
        );
    }

}