package com.epam.aidial.cfg.migration.db;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.List;
import java.util.function.BiConsumer;

@Slf4j
//CHECKSTYLE:OFF
public class V1_36__MigrateDeploymentType extends BaseJavaMigration {
    //CHECKSTYLE:ON
    @Override
    public void migrate(Context context) throws Exception {
        Connection connection = context.getConnection();
        List<DeploymentContext> deploymentContexts = List.of(
                new DeploymentContext("addon_entity", "ADDON"),
                new DeploymentContext("application_entity", "APPLICATION"),
                new DeploymentContext("assistant_entity", "ASSISTANT"),
                new DeploymentContext("model_entity", "MODEL"),
                new DeploymentContext("route_entity", "ROUTE")
        );
        List<DeploymentContext> auditDeploymentContexts = List.of(
                new DeploymentContext("addon_entity_aud", "ADDON"),
                new DeploymentContext("application_entity_aud", "APPLICATION"),
                new DeploymentContext("assistant_entity_aud", "ASSISTANT"),
                new DeploymentContext("model_entity_aud", "MODEL"),
                new DeploymentContext("route_entity_aud", "ROUTE")
        );

        migrate(connection, deploymentContexts, this::migrateDeployment);
        migrate(connection, auditDeploymentContexts, this::migrateDeploymentAud);
    }

    private void migrate(Connection connection, List<DeploymentContext> deploymentContexts, BiConsumer<Connection, DeploymentContext> migrate) {
        for (DeploymentContext deploymentContext : deploymentContexts) {
            log.debug("Preparing to migrate deployment: {}", deploymentContext);
            migrate.accept(connection, deploymentContext);
        }
    }

    @SneakyThrows
    private void migrateDeployment(Connection connection, DeploymentContext deploymentContext) {
        String table = deploymentContext.table();
        String type = deploymentContext.type();
        try (Statement statement = connection.createStatement(); ResultSet result = statement.executeQuery("select deployment_name from " + table)) {

            try (var preparedStatement = connection.prepareStatement("update deployment_entity set type=? where name=?")) {
                while (result.next()) {
                    String deploymentName = result.getString("deployment_name");
                    log.debug("found deployment: {}", deploymentName);

                    preparedStatement.setString(1, type);
                    preparedStatement.setString(2, deploymentName);
                    preparedStatement.addBatch();
                }
                preparedStatement.executeBatch();
            }
        }
    }

    @SneakyThrows
    private void migrateDeploymentAud(Connection connection, DeploymentContext deploymentContext) {
        String table = deploymentContext.table();
        String type = deploymentContext.type();
        try (Statement statement = connection.createStatement(); ResultSet result = statement.executeQuery("select deployment_name, rev from " + table)) {

            try (var preparedStatement = connection.prepareStatement("update deployment_entity_aud set type=? where name=? and rev=?")) {
                while (result.next()) {
                    String deploymentName = result.getString("deployment_name");
                    int rev = result.getInt("rev");
                    log.debug("found deployment: {}", deploymentName);

                    preparedStatement.setString(1, type);
                    preparedStatement.setString(2, deploymentName);
                    preparedStatement.setInt(3, rev);
                    preparedStatement.addBatch();
                }
                preparedStatement.executeBatch();
            }
        }
    }

    record DeploymentContext(String table, String type) {
    }
}
