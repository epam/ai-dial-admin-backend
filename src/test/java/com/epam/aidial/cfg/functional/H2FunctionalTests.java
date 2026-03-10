package com.epam.aidial.cfg.functional;

import com.epam.aidial.cfg.functional.config.H2FunctionalTestConfiguration;
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
import com.epam.aidial.cfg.functional.tests.history.InterceptorHistoryFunctionalTest;
import com.epam.aidial.cfg.functional.tests.history.InterceptorRunnerHistoryFunctionalTest;
import com.epam.aidial.cfg.functional.tests.history.KeyHistoryFunctionalTest;
import com.epam.aidial.cfg.functional.tests.history.ModelHistoryFunctionalTest;
import com.epam.aidial.cfg.functional.tests.history.RolesHistoryFunctionalTest;
import com.epam.aidial.cfg.functional.tests.history.RouteHistoryFunctionalTest;
import com.epam.aidial.cfg.functional.tests.history.ToolSetHistoryFunctionalTest;
import org.junit.jupiter.api.Nested;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;

@DataJpaTest
@TestPropertySource(properties = {
        "datasource.vendor=H2",
})
@Import(H2FunctionalTestConfiguration.class)
public class H2FunctionalTests extends FunctionalTestSuite {

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
    class CoreConfigAutoImportMergeJsonTests extends CoreConfigAutoImportOnBootstrapFunctionalTest.MergeJsonTests {
    }

    @Nested
    class CoreConfigAutoImportSequentialTests extends CoreConfigAutoImportOnBootstrapFunctionalTest.SequentialTests {
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
}
