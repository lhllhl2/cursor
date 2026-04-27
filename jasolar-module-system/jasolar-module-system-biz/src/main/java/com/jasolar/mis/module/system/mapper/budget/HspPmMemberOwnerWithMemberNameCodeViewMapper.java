package com.jasolar.mis.module.system.mapper.budget;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.jasolar.mis.framework.mybatis.core.mapper.BaseMapperX;
import com.jasolar.mis.module.system.domain.budget.HspPmMemberOwnerWithMemberNameCodeView;
import org.apache.ibatis.annotations.Mapper;

import java.util.Collection;
import java.util.List;

/**
 * PM成员与Owner及成员编码名称联合视图 Mapper
 */
@Mapper
public interface HspPmMemberOwnerWithMemberNameCodeViewMapper extends BaseMapperX<HspPmMemberOwnerWithMemberNameCodeView> {

    default List<HspPmMemberOwnerWithMemberNameCodeView> selectByPrimaryMemberId(String primaryMemberId) {
        return selectList(new LambdaQueryWrapper<HspPmMemberOwnerWithMemberNameCodeView>()
                .eq(HspPmMemberOwnerWithMemberNameCodeView::getPrimaryMemberId, primaryMemberId));
    }

    default HspPmMemberOwnerWithMemberNameCodeView selectOneByMemberCd(String memberCd) {
        return selectOne(new LambdaQueryWrapper<HspPmMemberOwnerWithMemberNameCodeView>()
                .eq(HspPmMemberOwnerWithMemberNameCodeView::getMemberCd, memberCd)
                .last("FETCH FIRST 1 ROWS ONLY"));
    }

    default List<HspPmMemberOwnerWithMemberNameCodeView> selectByMemberCds(Collection<String> memberCds) {
        return selectList(new LambdaQueryWrapper<HspPmMemberOwnerWithMemberNameCodeView>()
                .in(HspPmMemberOwnerWithMemberNameCodeView::getMemberCd, memberCds));
    }
}
