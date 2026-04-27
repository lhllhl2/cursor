package com.jasolar.mis.module.system.mapper.budget;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * OA回调涉及的规划单元与缓存刷新操作 Mapper
 */
@Mapper
public interface OaCallbackPlanningUnitMapper {

    @Select("SELECT MEMBER_ID FROM DATAINTEGRATION.VIEW_BUDGET_MEMBER_ID_NAME_CODE " +
            "WHERE MEMBER_NM = #{memberNm} FETCH FIRST 1 ROWS ONLY")
    String selectMemberIdByMemberNm(@Param("memberNm") String memberNm);

    @Select({
            "<script>",
            "SELECT CODE FROM SYSTEM_MANAGE_ORG",
            "WHERE P_CODE = #{pCode}",
            "  AND ORG_TYPE = #{orgType}",
            "</script>"
    })
    List<String> selectOrgCodesByParentAndType(@Param("pCode") String pCode,
                                               @Param("orgType") String orgType);

    @Select("SELECT P_CODE FROM SYSTEM_MANAGE_ORG WHERE CODE = #{code} FETCH FIRST 1 ROWS ONLY")
    String selectParentCodeByCode(@Param("code") String code);

    @Select({
            "<script>",
            "SELECT MEMBER_ID FROM DATAINTEGRATION.VIEW_BUDGET_MEMBER_ID_NAME_CODE",
            "WHERE MEMBER_CD IN",
            "<foreach collection='memberCds' item='item' open='(' separator=',' close=')'>",
            "#{item}",
            "</foreach>",
            "</script>"
    })
    List<String> selectMemberIdsByMemberCds(@Param("memberCds") List<String> memberCds);

    @Select("SELECT MEMBER_ID FROM DATAINTEGRATION.VIEW_BUDGET_MEMBER_ID_NAME_CODE " +
            "WHERE MEMBER_CD = #{memberCd} FETCH FIRST 1 ROWS ONLY")
    String selectMemberIdByMemberCd(@Param("memberCd") String memberCd);

    @Select({
            "<script>",
            "SELECT COUNT(1) FROM JAHP.HSP_PLANNING_UNIT",
            "WHERE VERSION_ID = #{versionId}",
            "  AND SCENARIO_ID = #{scenarioId}",
            "  AND ENTITY_ID IN",
            "  <foreach collection='entityIds' item='item' open='(' separator=',' close=')'>",
            "    #{item}",
            "  </foreach>",
            "  AND PROCESS_STATE &lt;&gt; 3",
            "</script>"
    })
    long countNotApprovedPlanningUnits(@Param("entityIds") List<String> entityIds,
                                       @Param("versionId") String versionId,
                                       @Param("scenarioId") String scenarioId);

    @Update("UPDATE JAHP.HSP_PLANNING_UNIT " +
            "SET PROCESS_STATE = 3 " +
            "WHERE ENTITY_ID = #{entityId} " +
            "  AND VERSION_ID = #{versionId} " +
            "  AND SCENARIO_ID = #{scenarioId} " +
            "  AND PROCESS_STATE = 2")
    int updateProcessStateApproved(@Param("entityId") String entityId,
                                   @Param("versionId") String versionId,
                                   @Param("scenarioId") String scenarioId);

    @Update({
            "<script>",
            "UPDATE JAHP.HSP_PLANNING_UNIT",
            "SET PROCESS_STATE = 3",
            "WHERE PROCESS_STATE = 2",
            "  AND (ENTITY_ID, VERSION_ID, SCENARIO_ID) IN",
            "  <foreach collection='pairs' item='item' open='(' separator=',' close=')'>",
            "    (#{item.entityId}, #{item.versionId}, #{item.scenarioId})",
            "  </foreach>",
            "</script>"
    })
    int updateProcessStateApprovedBatch(@Param("pairs") List<PlanningUnitPair> pairs);

    @Update("UPDATE JAHP.HSP_PLANNING_UNIT " +
            "SET PATH_NODE_ID = #{pathNodeId}, OWNER_GROUP_ID = #{ownerGroupId} " +
            "WHERE ENTITY_ID = #{entityId} " +
            "  AND VERSION_ID = #{versionId} " +
            "  AND SCENARIO_ID = #{scenarioId}")
    int updatePathAndOwner(@Param("entityId") String entityId,
                           @Param("versionId") String versionId,
                           @Param("scenarioId") String scenarioId,
                           @Param("pathNodeId") String pathNodeId,
                           @Param("ownerGroupId") String ownerGroupId);

    @Update("UPDATE JAHP.HSP_PLANNING_UNIT " +
            "SET PATH_NODE_ID = #{pathNodeId}, OWNER_GROUP_ID = #{ownerGroupId}, PROCESS_STATE = 2 " +
            "WHERE ENTITY_ID = #{entityId} " +
            "  AND VERSION_ID = #{versionId} " +
            "  AND SCENARIO_ID = #{scenarioId}")
    int updatePathOwnerAndProcessStateRejected(@Param("entityId") String entityId,
                                               @Param("versionId") String versionId,
                                               @Param("scenarioId") String scenarioId,
                                               @Param("pathNodeId") String pathNodeId,
                                               @Param("ownerGroupId") String ownerGroupId);

    @Update({
            "<script>",
            "UPDATE JAHP.HSP_PLANNING_UNIT",
            "SET PATH_NODE_ID = CASE",
            "  <foreach collection='updates' item='item'>",
            "    WHEN ENTITY_ID = #{item.entityId} AND VERSION_ID = #{item.versionId} AND SCENARIO_ID = #{item.scenarioId} THEN #{item.pathNodeId}",
            "  </foreach>",
            "  ELSE PATH_NODE_ID",
            "END,",
            "OWNER_GROUP_ID = CASE",
            "  <foreach collection='updates' item='item'>",
            "    WHEN ENTITY_ID = #{item.entityId} AND VERSION_ID = #{item.versionId} AND SCENARIO_ID = #{item.scenarioId} THEN #{item.ownerGroupId}",
            "  </foreach>",
            "  ELSE OWNER_GROUP_ID",
            "END",
            "WHERE (ENTITY_ID, VERSION_ID, SCENARIO_ID) IN",
            "  <foreach collection='updates' item='item' open='(' separator=',' close=')'>",
            "    (#{item.entityId}, #{item.versionId}, #{item.scenarioId})",
            "  </foreach>",
            "</script>"
    })
    int updatePathAndOwnerBatch(@Param("updates") List<PlanningUnitOwnerUpdate> updates);

    @Insert("INSERT INTO JAHP.HSP_ACTION (ID, FROM_ID, TO_ID, ACTION_ID, OBJECT_TYPE, MESSAGE, ACTION_TIME, PRIMARY_KEY) " +
            "VALUES (JAHP.HSP_ACTION_SEQ.NEXTVAL, 0, 0, 2, -999, 'CACHE RESET', SYSTIMESTAMP, NULL)")
    int insertCacheResetAction();

    class PlanningUnitPair {
        private final String entityId;
        private final String versionId;
        private final String scenarioId;

        public PlanningUnitPair(String entityId, String versionId, String scenarioId) {
            this.entityId = entityId;
            this.versionId = versionId;
            this.scenarioId = scenarioId;
        }

        public String getEntityId() {
            return entityId;
        }

        public String getVersionId() {
            return versionId;
        }

        public String getScenarioId() {
            return scenarioId;
        }
    }

    class PlanningUnitOwnerUpdate {
        private final String entityId;
        private final String versionId;
        private final String scenarioId;
        private final String pathNodeId;
        private final String ownerGroupId;

        public PlanningUnitOwnerUpdate(String entityId, String versionId, String scenarioId, String pathNodeId, String ownerGroupId) {
            this.entityId = entityId;
            this.versionId = versionId;
            this.scenarioId = scenarioId;
            this.pathNodeId = pathNodeId;
            this.ownerGroupId = ownerGroupId;
        }

        public String getEntityId() {
            return entityId;
        }

        public String getVersionId() {
            return versionId;
        }

        public String getScenarioId() {
            return scenarioId;
        }

        public String getPathNodeId() {
            return pathNodeId;
        }

        public String getOwnerGroupId() {
            return ownerGroupId;
        }
    }
}
