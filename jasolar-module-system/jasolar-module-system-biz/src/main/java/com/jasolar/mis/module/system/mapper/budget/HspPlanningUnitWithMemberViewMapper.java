package com.jasolar.mis.module.system.mapper.budget;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.jasolar.mis.framework.mybatis.core.mapper.BaseMapperX;
import com.jasolar.mis.module.system.domain.budget.HspPlanningUnitWithMemberView;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * HSP 规划单元与成员映射视图 Mapper
 */
@Mapper
public interface HspPlanningUnitWithMemberViewMapper extends BaseMapperX<HspPlanningUnitWithMemberView> {

    default List<HspPlanningUnitWithMemberView> selectForInitialPush(String scenarioName,
                                                                      String versionId,
                                                                      Collection<String> targetEntityIds,
                                                                      Integer processState) {
        return selectList(new LambdaQueryWrapper<HspPlanningUnitWithMemberView>()
                .eq(HspPlanningUnitWithMemberView::getScenarioName, scenarioName)
                .eq(HspPlanningUnitWithMemberView::getVersionId, versionId)
                .eq(HspPlanningUnitWithMemberView::getProcessState, processState)
                .isNotNull(HspPlanningUnitWithMemberView::getPathPrimaryMemberCd)
                .isNotNull(HspPlanningUnitWithMemberView::getMorgCode)
                .apply("((IS_APPROVAL_LAST_LVL = 1 AND PATH_PRIMARY_MEMBER_CD <> MORG_CODE) " +
                        "OR (IS_APPROVAL_LAST_LVL = 0 AND PATH_PRIMARY_MEMBER_CD = MORG_CODE))")
                .in(HspPlanningUnitWithMemberView::getMorgCode, targetEntityIds));
    }

    @Select({
            "<script>",
            "SELECT x.plan_unit_id AS planUnitId, cu.name AS employeeNo",
            "  FROM (",
            "        SELECT l.plan_unit_id, u.sid,",
            "               ROW_NUMBER() OVER (PARTITION BY l.plan_unit_id ORDER BY l.status_changed DESC) AS rn",
            "          FROM JAHP.HSP_PLANNING_UNIT_LOG l",
            "          JOIN JAHP.HSP_USERS u ON u.user_id = l.author_id",
            "         WHERE l.action = 6",
            "           AND l.plan_unit_id IN",
            "           <foreach collection='planUnitIds' item='id' open='(' separator=',' close=')'>",
            "             #{id}",
            "           </foreach>",
            "       ) x",
            "  LEFT JOIN FOUNDATION.CSS_USERS cu ON cu.identity_id = x.sid",
            " WHERE x.rn = 1",
            "</script>"
    })
    List<Map<String, Object>> selectEmployeeNoByPlanUnitIds(@Param("planUnitIds") Collection<String> planUnitIds);

    @Select({
            "<script>",
            "SELECT code AS morgCode, employee_no AS employeeNo",
            "  FROM SYSTEM_MANAGE_ORG",
            " WHERE code IN",
            " <foreach collection='morgCodes' item='code' open='(' separator=',' close=')'>",
            "   #{code}",
            " </foreach>",
            "</script>"
    })
    List<Map<String, Object>> selectEmployeeNoByMorgCodes(@Param("morgCodes") Collection<String> morgCodes);

    @Select("SELECT * FROM DATAINTEGRATION.VIEW_BUDGET_TO_OA_PLAN1_RC_T1 WHERE ZXBM = #{morgCode}")
    List<Map<String, Object>> selectPlan1RcT1ByMorgCode(@Param("morgCode") String morgCode);

    @Select("SELECT * FROM DATAINTEGRATION.VIEW_BUDGET_TO_OA_PLAN1_RC_T2 WHERE ZXBM = #{morgCode}")
    List<Map<String, Object>> selectPlan1RcT2ByMorgCode(@Param("morgCode") String morgCode);

    @Select("SELECT * FROM DATAINTEGRATION.VIEW_BUDGET_TO_OA_PLAN1_RC_T3 WHERE ZXBM = #{morgCode}")
    List<Map<String, Object>> selectPlan1RcT3ByMorgCode(@Param("morgCode") String morgCode);
}
