package com.epam.aidial.cfg.migration.db;

import com.github.f4b6a3.uuid.UuidCreator;
import lombok.extern.slf4j.Slf4j;
import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

@Slf4j
//CHECKSTYLE:OFF
public class V1_45__MigrateDefaultRoleLimits extends BaseJavaMigration {

    //CHECKSTYLE:ON

    private static final String SELECT_LIMITS_FROM_DEPLOYMENT_ENTITY = """
            select name, default_minute, default_day, default_week, default_month, default_request_hour,
            default_request_day
            from deployment_entity""";
    private static final String INSERT_INTO_ROLE_LIMIT_ENTITY = """
            insert into role_limit_entity (role_name, deployment_name, enabled, default_minute, default_day,
            default_week, default_month, default_request_hour, default_request_day)
            values (?, ?, ?, ?, ?, ?, ?, ?, ?)""";
    private static final String INSERT_INTO_REV_INFO = """
            insert into revinfo (author, timestamp)
            VALUES (?, ?);""";
    private static final String INSERT_INTO_ROLE_LIMIT_ENTITY_AUD = """
            insert into role_limit_entity_aud (rev, revtype, role_name, deployment_name, enabled, default_minute,
            default_day, default_week, default_month, default_request_hour, default_request_day)
            values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)""";
    private static final String INSERT_INTO_ACTIVITY_AUDIT_ENTITY = """
            insert into audit_activity_entity (activity_id, activity_type, resource_type, resource_id,
            epoch_timestamp_ms, initiated_author, initiated_email, revision)
            values (?, ?, ?, ?, ?, ?, ?, ?)""";

    private static final String AUTHOR = "system";

    @Override
    public void migrate(Context context) throws Exception {
        Connection connection = context.getConnection();
        long currentEpochMs = System.currentTimeMillis();

        try (Statement selectStatement = connection.createStatement(); ResultSet result = selectStatement.executeQuery(SELECT_LIMITS_FROM_DEPLOYMENT_ENTITY)) {

            List<DeploymentDefaultLimit> deploymentDefaultLimits = insertDefaultRoleLimits(connection, result);

            if (!deploymentDefaultLimits.isEmpty()) {
                int revInfoId = insertRevInfoStatement(connection, currentEpochMs);
                insertDefaultRoleLimitsIntoAudit(connection, deploymentDefaultLimits, revInfoId, currentEpochMs);
            }
        }
    }

    private List<DeploymentDefaultLimit> insertDefaultRoleLimits(Connection connection, ResultSet result) throws SQLException {
        List<DeploymentDefaultLimit> deploymentDefaultLimits = new ArrayList<>();

        try (var insertIntoRoleLimitEntityStatement = connection.prepareStatement(INSERT_INTO_ROLE_LIMIT_ENTITY)) {
            while (result.next()) {
                DeploymentDefaultLimit deploymentDefaultLimit = new DeploymentDefaultLimit(
                        "default",
                        result.getString("name"),
                        true,
                        readLong(result, "default_minute"),
                        readLong(result, "default_day"),
                        readLong(result, "default_week"),
                        readLong(result, "default_month"),
                        readLong(result, "default_request_hour"),
                        readLong(result, "default_request_day")
                );

                deploymentDefaultLimits.add(deploymentDefaultLimit);

                insertIntoRoleLimitEntityStatement.setString(1, deploymentDefaultLimit.roleName());
                insertIntoRoleLimitEntityStatement.setString(2, deploymentDefaultLimit.deploymentName());
                insertIntoRoleLimitEntityStatement.setBoolean(3, deploymentDefaultLimit.enabled());
                insertIntoRoleLimitEntityStatement.setObject(4, deploymentDefaultLimit.defaultMinute());
                insertIntoRoleLimitEntityStatement.setObject(5, deploymentDefaultLimit.defaultDay());
                insertIntoRoleLimitEntityStatement.setObject(6, deploymentDefaultLimit.defaultWeek());
                insertIntoRoleLimitEntityStatement.setObject(7, deploymentDefaultLimit.defaultMonth());
                insertIntoRoleLimitEntityStatement.setObject(8, deploymentDefaultLimit.defaultRequestHour());
                insertIntoRoleLimitEntityStatement.setObject(9, deploymentDefaultLimit.defaultRequestDay());
                insertIntoRoleLimitEntityStatement.addBatch();
            }

            insertIntoRoleLimitEntityStatement.executeBatch();
        }

        return deploymentDefaultLimits;
    }

    private int insertRevInfoStatement(Connection connection, long currentEpochMs) throws SQLException {
        try (var insertRevInfoStatement = connection.prepareStatement(INSERT_INTO_REV_INFO, Statement.RETURN_GENERATED_KEYS)) {
            insertRevInfoStatement.setString(1, AUTHOR);
            insertRevInfoStatement.setLong(2, currentEpochMs);
            insertRevInfoStatement.executeUpdate();

            try (ResultSet generatedKeys = insertRevInfoStatement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    return generatedKeys.getInt(1);
                } else {
                    throw new RuntimeException("Unable to get id of inserted revision entity");
                }
            }
        }
    }

    private void insertDefaultRoleLimitsIntoAudit(Connection connection,
                                                  List<DeploymentDefaultLimit> deploymentDefaultLimits,
                                                  int revInfoId,
                                                  long currentEpochMs) throws SQLException {
        try (var insertIntoRoleLimitEntityAudStatement = connection.prepareStatement(INSERT_INTO_ROLE_LIMIT_ENTITY_AUD);) {
            for (var deploymentDefaultLimit : deploymentDefaultLimits) {
                insertIntoRoleLimitEntityAudStatement.setInt(1, revInfoId);
                insertIntoRoleLimitEntityAudStatement.setInt(2, 0);
                insertIntoRoleLimitEntityAudStatement.setString(3, deploymentDefaultLimit.roleName());
                insertIntoRoleLimitEntityAudStatement.setString(4, deploymentDefaultLimit.deploymentName());
                insertIntoRoleLimitEntityAudStatement.setBoolean(5, deploymentDefaultLimit.enabled());
                insertIntoRoleLimitEntityAudStatement.setObject(6, deploymentDefaultLimit.defaultMinute());
                insertIntoRoleLimitEntityAudStatement.setObject(7, deploymentDefaultLimit.defaultDay());
                insertIntoRoleLimitEntityAudStatement.setObject(8, deploymentDefaultLimit.defaultWeek());
                insertIntoRoleLimitEntityAudStatement.setObject(9, deploymentDefaultLimit.defaultMonth());
                insertIntoRoleLimitEntityAudStatement.setObject(10, deploymentDefaultLimit.defaultRequestHour());
                insertIntoRoleLimitEntityAudStatement.setObject(11, deploymentDefaultLimit.defaultRequestDay());
                insertIntoRoleLimitEntityAudStatement.addBatch();
            }
            insertIntoRoleLimitEntityAudStatement.executeBatch();
        }

        try (var insertIntoActivityAuditEntityStatement = connection.prepareStatement(INSERT_INTO_ACTIVITY_AUDIT_ENTITY);) {
            for (var deploymentDefaultLimit : deploymentDefaultLimits) {
                var id = "RoleLimitId(deploymentName=%s, roleName=%s)"
                        .formatted(deploymentDefaultLimit.deploymentName(), deploymentDefaultLimit.roleName());
                insertIntoActivityAuditEntityStatement.setString(1, UuidCreator.getTimeOrderedEpoch().toString());
                insertIntoActivityAuditEntityStatement.setString(2, "Create");
                insertIntoActivityAuditEntityStatement.setString(3, "RoleLimit");
                insertIntoActivityAuditEntityStatement.setString(4, id);
                insertIntoActivityAuditEntityStatement.setLong(5, currentEpochMs);
                insertIntoActivityAuditEntityStatement.setString(6, AUTHOR);
                insertIntoActivityAuditEntityStatement.setString(7, null);
                insertIntoActivityAuditEntityStatement.setInt(8, revInfoId);
                insertIntoActivityAuditEntityStatement.addBatch();
            }
            insertIntoActivityAuditEntityStatement.executeBatch();
        }
    }

    private Long readLong(ResultSet resultSet, String columnName) throws SQLException {
        var value = resultSet.getLong(columnName);
        return resultSet.wasNull() ? null : value;
    }

    private record DeploymentDefaultLimit(
            String roleName,
            String deploymentName,
            boolean enabled,
            Long defaultMinute,
            Long defaultDay,
            Long defaultWeek,
            Long defaultMonth,
            Long defaultRequestHour,
            Long defaultRequestDay
    ) {
    }
}
