package com.epam.aidial.cfg.functional;

import com.epam.aidial.cfg.functional.config.MsSqlServerFunctionalTestConfiguration;
import com.epam.aidial.cfg.functional.tests.AdapterFunctionalTest;
import com.epam.aidial.cfg.functional.tests.AddonFunctionalTest;
import com.epam.aidial.cfg.functional.tests.AdminSettingsFunctionalTest;
import com.epam.aidial.cfg.functional.tests.ApplicationFunctionalTest;
import com.epam.aidial.cfg.functional.tests.ApplicationTypeSchemaFunctionalTest;
import com.epam.aidial.cfg.functional.tests.AssistantFunctionalTest;
import com.epam.aidial.cfg.functional.tests.AssistantsPropertyFunctionalTest;
import com.epam.aidial.cfg.functional.tests.ConfigTransferFunctionalTest;
import com.epam.aidial.cfg.functional.tests.CoreConfigAutoImportOnBootstrapFunctionalTest;
import com.epam.aidial.cfg.functional.tests.InterceptorFunctionalTest;
import com.epam.aidial.cfg.functional.tests.InterceptorRunnerFunctionalTest;
import com.epam.aidial.cfg.functional.tests.KeyFunctionalTest;
import com.epam.aidial.cfg.functional.tests.ModelFunctionalTest;
import com.epam.aidial.cfg.functional.tests.RolesFunctionalTest;
import com.epam.aidial.cfg.functional.tests.RouteFunctionalTest;
import com.epam.aidial.cfg.functional.tests.ToolSetFunctionalTest;
import com.epam.aidial.cfg.functional.tests.history.ActivityAuditFunctionalTest;
import com.epam.aidial.cfg.functional.tests.history.AdapterHistoryFunctionalTest;
import com.epam.aidial.cfg.functional.tests.history.AddonHistoryFunctionalTest;
import com.epam.aidial.cfg.functional.tests.history.ApplicationHistoryFunctionalTest;
import com.epam.aidial.cfg.functional.tests.history.ApplicationTypeSchemaHistoryFunctionalTest;
import com.epam.aidial.cfg.functional.tests.history.AssistantHistoryFunctionalTest;
import com.epam.aidial.cfg.functional.tests.history.AssistantsPropertyHistoryFunctionalTest;
import com.epam.aidial.cfg.functional.tests.history.GeneralHistoryFunctionalTest;
import com.epam.aidial.cfg.functional.tests.history.InterceptorHistoryFunctionalTest;
import com.epam.aidial.cfg.functional.tests.history.InterceptorRunnerHistoryFunctionalTest;
import com.epam.aidial.cfg.functional.tests.history.KeyHistoryFunctionalTest;
import com.epam.aidial.cfg.functional.tests.history.ModelHistoryFunctionalTest;
import com.epam.aidial.cfg.functional.tests.history.RolesHistoryFunctionalTest;
import com.epam.aidial.cfg.functional.tests.history.RouteHistoryFunctionalTest;
import com.epam.aidial.cfg.functional.tests.history.ToolSetHistoryFunctionalTest;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.junit.jupiter.api.Nested;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.testcontainers.containers.MSSQLServerContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.sql.SQLException;
import java.sql.Statement;

@DataJpaTest
@TestPropertySource(properties = {
        "datasource.vendor=MS_SQL_SERVER",
})
@Import(MsSqlServerFunctionalTestConfiguration.class)
@Testcontainers
public class MsSqlServerFunctionalTests extends FunctionalTestSuite {

    public static final String TEST_DB_NAME = "test";

    @Container
    @ServiceConnection
    private static final MSSQLServerContainer<?> MS_SQL_SERVER = new MSSQLServerContainer<>("mcr.microsoft.com/mssql/server:2022-CU18-ubuntu-22.04");

    static {
        MS_SQL_SERVER.acceptLicense();
        MS_SQL_SERVER.start();

        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setJdbcUrl(MS_SQL_SERVER.getJdbcUrl());
        hikariConfig.setUsername(MS_SQL_SERVER.getUsername());
        hikariConfig.setPassword(MS_SQL_SERVER.getPassword());

        try (
                HikariDataSource dataSource = new HikariDataSource(hikariConfig);
                Statement statement = dataSource.getConnection().createStatement()
        ) {
            statement.execute("CREATE DATABASE [%s] COLLATE SQL_Latin1_General_CP1_CS_AS;".formatted(TEST_DB_NAME));
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @DynamicPropertySource
    static void overrideProps(DynamicPropertyRegistry registry) {
        String jdbcUrl = MS_SQL_SERVER.withUrlParam("database", TEST_DB_NAME).getJdbcUrl();
        registry.add("spring.datasource.url", () -> jdbcUrl);
    }


    @Nested
    class AddonTests extends AddonFunctionalTest {
    }

    @Nested
    class ApplicationTests extends ApplicationFunctionalTest {
    }

    @Nested
    class ApplicationTypeSchemaTests extends ApplicationTypeSchemaFunctionalTest {
    }

    @Nested
    class AssistantTests extends AssistantFunctionalTest {
    }

    @Nested
    class AssistantsPropertyTests extends AssistantsPropertyFunctionalTest {
    }

    @Nested
    class ConfigTransferTests extends ConfigTransferFunctionalTest {
    }

    @Nested
    class CoreConfigAutoImportOnBootstrapTests extends CoreConfigAutoImportOnBootstrapFunctionalTest {
    }

    @Nested
    class InterceptorTests extends InterceptorFunctionalTest {
    }

    @Nested
    class InterceptorRunnerTests extends InterceptorRunnerFunctionalTest {
    }

    @Nested
    class KeyTests extends KeyFunctionalTest {
    }

    @Nested
    class ModelTests extends ModelFunctionalTest {
    }

    @Nested
    class RolesTests extends RolesFunctionalTest {
    }

    @Nested
    class RouteTests extends RouteFunctionalTest {
    }

    @Nested
    class AdapterTest extends AdapterFunctionalTest {
    }

    @Nested
    class ToolSetTests extends ToolSetFunctionalTest {
    }

    @Nested
    class AdminSettingsTests extends AdminSettingsFunctionalTest {
    }

    @Nested
    class AddonHistoryTests extends AddonHistoryFunctionalTest {
    }

    @Nested
    class ApplicationHistoryTests extends ApplicationHistoryFunctionalTest {
    }

    @Nested
    class ApplicationTypeSchemaHistoryTests extends ApplicationTypeSchemaHistoryFunctionalTest {
    }

    @Nested
    class AssistantHistoryTests extends AssistantHistoryFunctionalTest {
    }

    @Nested
    class AssistantsPropertyHistoryTests extends AssistantsPropertyHistoryFunctionalTest {
    }

    @Nested
    class InterceptorHistoryTests extends InterceptorHistoryFunctionalTest {
    }

    @Nested
    class InterceptorRunnerHistoryTests extends InterceptorRunnerHistoryFunctionalTest {
    }

    @Nested
    class KeyHistoryTests extends KeyHistoryFunctionalTest {
    }

    @Nested
    class ModelHistoryTests extends ModelHistoryFunctionalTest {
    }

    @Nested
    class RolesHistoryTests extends RolesHistoryFunctionalTest {
    }

    @Nested
    class RouteHistoryTest extends RouteHistoryFunctionalTest {
    }

    @Nested
    class ActivityAuditTest extends ActivityAuditFunctionalTest {
    }

    @Nested
    class AdapterHistoryTest extends AdapterHistoryFunctionalTest {
    }

    @Nested
    class ToolSetHistoryTests extends ToolSetHistoryFunctionalTest {
    }

    @Nested
    class GeneralHistoryTests extends GeneralHistoryFunctionalTest {
    }
}
