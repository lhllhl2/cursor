package com.jasolar.mis.module.system.service.ehr;

import com.jasolar.mis.module.system.controller.ehr.vo.ErpUnmappedAccountVO;
import com.jasolar.mis.module.system.config.ErpOracleProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class ErpAccountViewRepository {

    private static final String QUERY_SQL =
            "SELECT MEMBERCODE, MEMBERNAME " +
            "FROM APPS.CUX_JA_ACCOUNT_V " +
            "WHERE MEMBERCODE IS NOT NULL AND TRIM(MEMBERCODE) IS NOT NULL";

    private final ErpOracleProperties erpOracleProperties;

    public List<ErpUnmappedAccountVO> queryAllMemberCodeAndName() {
        validateConfig();
        List<ErpUnmappedAccountVO> result = new ArrayList<>();
        try {
            Class.forName(erpOracleProperties.getDriverClassName());
            try (Connection conn = DriverManager.getConnection(
                    erpOracleProperties.getUrl(),
                    erpOracleProperties.getUsername(),
                    erpOracleProperties.getPassword());
                 PreparedStatement ps = conn.prepareStatement(QUERY_SQL);
                 ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {
                    String memberCode = rs.getString("MEMBERCODE");
                    if (!StringUtils.hasText(memberCode)) {
                        continue;
                    }
                    result.add(ErpUnmappedAccountVO.builder()
                            .memberCode(memberCode.trim())
                            .memberName(rs.getString("MEMBERNAME"))
                            .build());
                }
            }
        } catch (Exception ex) {
            throw new IllegalStateException("查询ERP视图 APPS.CUX_JA_ACCOUNT_V 失败", ex);
        }
        return result;
    }

    private void validateConfig() {
        if (!StringUtils.hasText(erpOracleProperties.getUrl())
                || !StringUtils.hasText(erpOracleProperties.getUsername())
                || !StringUtils.hasText(erpOracleProperties.getPassword())) {
            throw new IllegalStateException("ERP Oracle连接配置不完整，请检查 erp.oracle.*");
        }
    }
}
