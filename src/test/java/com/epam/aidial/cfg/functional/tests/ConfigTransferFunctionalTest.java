package com.epam.aidial.cfg.functional.tests;

import com.epam.aidial.cfg.configuration.CoreConfigVersionProperties;
import com.epam.aidial.cfg.configuration.JsonMapperConfiguration;
import com.epam.aidial.cfg.domain.model.Adapter;
import com.epam.aidial.cfg.domain.model.Application;
import com.epam.aidial.cfg.domain.model.ExportApplicationTypeSchemaInfo;
import com.epam.aidial.cfg.domain.model.ExportConfig;
import com.epam.aidial.cfg.domain.model.ExportConfigComponentType;
import com.epam.aidial.cfg.domain.model.ExportConfigPreview;
import com.epam.aidial.cfg.domain.model.ExportFormat;
import com.epam.aidial.cfg.domain.model.ExportKeyInfo;
import com.epam.aidial.cfg.domain.model.ImportConfigPreview;
import com.epam.aidial.cfg.domain.model.Model;
import com.epam.aidial.cfg.domain.model.route.DependentRoute;
import com.epam.aidial.cfg.domain.model.source.InterceptorRunnerSource;
import com.epam.aidial.cfg.domain.model.source.ModelAdapterSource;
import com.epam.aidial.cfg.domain.service.DatabaseService;
import com.epam.aidial.cfg.dto.AdapterDto;
import com.epam.aidial.cfg.dto.AddonDto;
import com.epam.aidial.cfg.dto.ApplicationDto;
import com.epam.aidial.cfg.dto.ApplicationTypeSchemaDto;
import com.epam.aidial.cfg.dto.AssistantDto;
import com.epam.aidial.cfg.dto.AssistantsPropertyDto;
import com.epam.aidial.cfg.dto.AttachmentPathDto;
import com.epam.aidial.cfg.dto.AuthenticationTypeDto;
import com.epam.aidial.cfg.dto.FeaturesDto;
import com.epam.aidial.cfg.dto.GlobalSettingsDto;
import com.epam.aidial.cfg.dto.InterceptorDto;
import com.epam.aidial.cfg.dto.InterceptorRunnerDto;
import com.epam.aidial.cfg.dto.KeyDto;
import com.epam.aidial.cfg.dto.LimitDto;
import com.epam.aidial.cfg.dto.ModelDto;
import com.epam.aidial.cfg.dto.ResourceAuthSettingsDto;
import com.epam.aidial.cfg.dto.ResourceTypeDto;
import com.epam.aidial.cfg.dto.ResponseDto;
import com.epam.aidial.cfg.dto.RoleDto;
import com.epam.aidial.cfg.dto.ShareResourceLimitDto;
import com.epam.aidial.cfg.dto.ToolSetDto;
import com.epam.aidial.cfg.dto.ToolSetDto.TransportDto;
import com.epam.aidial.cfg.dto.UpstreamDto;
import com.epam.aidial.cfg.dto.route.DependentRouteDto;
import com.epam.aidial.cfg.dto.route.DependentRouteDto.ResourceAccessType;
import com.epam.aidial.cfg.dto.route.RouteDto;
import com.epam.aidial.cfg.dto.source.InterceptorRunnerSourceDto;
import com.epam.aidial.cfg.dto.source.ModelAdapterSourceDto;
import com.epam.aidial.cfg.exception.EntityNotFoundException;
import com.epam.aidial.cfg.features.flag.aspect.FeatureFlagGateEvaluationAspect;
import com.epam.aidial.cfg.model.ConfigImportOptions;
import com.epam.aidial.cfg.model.ExportConfigComponent;
import com.epam.aidial.cfg.model.FullExportRequest;
import com.epam.aidial.cfg.model.ResourceType;
import com.epam.aidial.cfg.model.SelectedItemsExportRequest;
import com.epam.aidial.cfg.service.config.export.ConflictResolutionPolicy;
import com.epam.aidial.cfg.service.config.transfer.ConfigTransfer;
import com.epam.aidial.cfg.transaction.timestamp.TransactionTimestampContext;
import com.epam.aidial.cfg.utils.ResourceUtils;
import com.epam.aidial.cfg.web.facade.AdapterFacade;
import com.epam.aidial.cfg.web.facade.AddonFacade;
import com.epam.aidial.cfg.web.facade.ApplicationFacade;
import com.epam.aidial.cfg.web.facade.ApplicationTypeSchemaFacade;
import com.epam.aidial.cfg.web.facade.AssistantFacade;
import com.epam.aidial.cfg.web.facade.AssistantsPropertyFacade;
import com.epam.aidial.cfg.web.facade.GlobalSettingsFacade;
import com.epam.aidial.cfg.web.facade.InterceptorFacade;
import com.epam.aidial.cfg.web.facade.InterceptorRunnerFacade;
import com.epam.aidial.cfg.web.facade.KeyFacade;
import com.epam.aidial.cfg.web.facade.ModelFacade;
import com.epam.aidial.cfg.web.facade.RoleFacade;
import com.epam.aidial.cfg.web.facade.RouteFacade;
import com.epam.aidial.cfg.web.facade.ToolSetFacade;
import com.epam.aidial.core.config.Config;
import com.epam.aidial.core.config.CoreApplicationTypeSchema;
import com.epam.aidial.core.config.CoreRoute;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.assertj.core.api.Assertions;
import org.json.JSONException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.skyscreamer.jsonassert.JSONAssert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import static com.epam.aidial.cfg.functional.utils.FunctionalTestHelper.createAdapterDto;
import static com.epam.aidial.cfg.functional.utils.FunctionalTestHelper.createBaseApplicationDto;
import static com.epam.aidial.cfg.functional.utils.FunctionalTestHelper.createInterceptorDto;
import static com.epam.aidial.cfg.functional.utils.FunctionalTestHelper.createInterceptorRunnerDto;
import static com.epam.aidial.cfg.functional.utils.FunctionalTestHelper.createKeyDto;
import static com.epam.aidial.cfg.functional.utils.FunctionalTestHelper.createModelDto;
import static com.epam.aidial.cfg.functional.utils.FunctionalTestHelper.createModelDtoWithAdapter;
import static com.epam.aidial.cfg.functional.utils.FunctionalTestHelper.createModelDtoWithLimitsAndEndpoint;
import static com.epam.aidial.cfg.functional.utils.FunctionalTestHelper.createRoleDto;
import static com.epam.aidial.cfg.functional.utils.FunctionalTestHelper.createRouteDto;
import static com.epam.aidial.cfg.functional.utils.FunctionalTestHelper.createRouteDtoWithLimits;
import static com.epam.aidial.cfg.functional.utils.FunctionalTestHelper.createToolSetDtoWithoutRoleLimits;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;

public abstract class ConfigTransferFunctionalTest {

    @Autowired
    private ModelFacade modelFacade;
    @Autowired
    private ApplicationFacade applicationFacade;
    @Autowired
    private InterceptorFacade interceptorFacade;
    @Autowired
    private InterceptorRunnerFacade interceptorRunnerFacade;
    @Autowired
    private KeyFacade keyFacade;
    @Autowired
    private ConfigTransfer configTransfer;
    @Autowired
    private ApplicationTypeSchemaFacade applicationTypeSchemaFacade;
    @Autowired
    private RoleFacade roleFacade;
    @Autowired
    private RouteFacade routeFacade;
    @Autowired
    private AddonFacade addonFacade;
    @Autowired
    private AssistantFacade assistantFacade;
    @Autowired
    private ToolSetFacade toolSetFacade;
    @Autowired
    private AssistantsPropertyFacade assistantsPropertyFacade;
    @Autowired
    private FeatureFlagGateEvaluationAspect featureFlagAspect;
    @Autowired
    private AdapterFacade adapterFacade;
    @Autowired
    private CoreConfigVersionProperties versionProperties;
    @Autowired
    private TransactionTimestampContext transactionTimestampContext;
    @Autowired
    private DatabaseService databaseService;
    @Autowired
    private GlobalSettingsFacade globalSettingsFacade;

    private final ObjectMapper jsonMapper = JsonMapperConfiguration.createJsonMapper();

    @BeforeEach
    void setUp() {
        versionProperties.setTarget("latest");
    }

    @Test
    void testImport_WithoutConflict() throws IOException {
        // given
        String config = FileUtils.readFileToString(new File("src/test/resources/import/import_configWithoutAssistants.json"),
                StandardCharsets.UTF_8);
        MockMultipartFile mockFile = new MockMultipartFile(
                "file",
                "test.json",
                "application/json",
                config.getBytes()
        );

        // when
        configTransfer.importConfig(List.of(mockFile), overrideAndCreateRoleAndCreateNew());

        // then
        Map<String, ModelDto> models = modelFacade.getAll().stream().collect(Collectors.toMap(ModelDto::getName, a -> a));
        Assertions.assertThat(models).containsOnlyKeys("testModel1", "testModel2");
        Assertions.assertThat(models.get("testModel1")).satisfies(modelDto -> {
            Assertions.assertThat(modelDto.getRoleLimits()).containsOnlyKeys("testRole1", "testRole2", "testRole3", "default");
            Assertions.assertThat(modelDto.getRoleLimits().get("testRole1")).satisfies(limit1 -> {
                Assertions.assertThat(limit1.isEnabled()).isTrue();
                Assertions.assertThat(limit1.getDay()).isEqualTo(1);
                Assertions.assertThat(limit1.getMinute()).isEqualTo(Long.MAX_VALUE);
            });
            Assertions.assertThat(modelDto.getRoleLimits().get("testRole2")).satisfies(limit2 -> {
                Assertions.assertThat(limit2.isEnabled()).isFalse();
                Assertions.assertThat(limit2.getDay()).isEqualTo(Long.MAX_VALUE);
                Assertions.assertThat(limit2.getMinute()).isEqualTo(Long.MAX_VALUE);
            });
            Assertions.assertThat(modelDto.getRoleLimits().get("testRole3")).satisfies(limit3 -> {
                Assertions.assertThat(limit3.isEnabled()).isTrue();
                Assertions.assertThat(limit3.getDay()).isEqualTo(Long.MAX_VALUE);
                Assertions.assertThat(limit3.getMinute()).isEqualTo(Long.MAX_VALUE);
            });
            Assertions.assertThat(modelDto.getRoleLimits().get("default")).satisfies(limitDefault -> {
                Assertions.assertThat(limitDefault.isEnabled()).isFalse();
                Assertions.assertThat(limitDefault.getDay()).isEqualTo(3);
                Assertions.assertThat(limitDefault.getMinute()).isEqualTo(Long.MAX_VALUE);
            });
            Assertions.assertThat(modelDto.getInterceptors()).isNotEmpty()
                    .hasSize(1).first().isEqualTo("testInterceptor1");
            Assertions.assertThat(modelDto.getDefaults())
                    .containsExactlyInAnyOrderEntriesOf(Map.of("max_tokens", 8000));
            Assertions.assertThat(modelDto.getFeatures()).isEqualTo(new FeaturesDto());
        });
        Assertions.assertThat(models.get("testModel2"))
                .satisfies(modelDto -> Assertions.assertThat(modelDto.getIsPublic()).isTrue());

        ApplicationDto applicationDto = applicationFacade.getApplication("testApplication1");
        Assertions.assertThat(applicationDto.getInterceptors()).hasSize(1).first().isEqualTo("testInterceptor1");
        Assertions.assertThat(applicationDto.getCustomAppSchemaId().toString()).isEqualTo("https://test-schema-id.example");
        ApplicationDto applicationDto2 = applicationFacade.getApplication("testApplication2");
        Assertions.assertThat(applicationDto2.getDefaults()).containsExactlyInAnyOrderEntriesOf(Map.of("defaults_key", "defaults_value"));

        Collection<InterceptorDto> interceptors = interceptorFacade.getAllInterceptors();
        Assertions.assertThat(interceptors).isNotEmpty().hasSize(1).first().satisfies(i -> {
            Assertions.assertThat(i.getDefaults()).containsExactlyInAnyOrderEntriesOf(Map.of("defaults_key", "defaults_value"));
            Assertions.assertThat(i.getEntities()).containsExactlyInAnyOrder("testModel1", "testApplication1");
        });

        Collection<String> allKeys = keyFacade.getAllKeys().stream().map(KeyDto::getName).toList();
        Assertions.assertThat(allKeys).hasSize(2).allSatisfy(key ->
                Assertions.assertThatCode(() -> UUID.fromString(key)).doesNotThrowAnyException()
        );

        Assertions.assertThat(roleFacade.getRole("testRole1").getShare()).hasSize(1).satisfies(share -> {
            ShareResourceLimitDto shareResourceLimit = share.get(ResourceTypeDto.APPLICATION);
            Assertions.assertThat(shareResourceLimit.getInvitationTtl()).isEqualTo(432000000);
            Assertions.assertThat(shareResourceLimit.getMaxAcceptedUsers()).isEqualTo(10);
        });

        ResourceAuthSettingsDto expectedAuthSettings = getAuthSettingsDto();

        ToolSetDto toolSet1 = toolSetFacade.getToolSet("toolset1");
        Assertions.assertThat(toolSet1).satisfies(t -> {
            Assertions.assertThat(t.getEndpoint()).isEqualTo("http://sample-endpoint/api");
            Assertions.assertThat(t.getTransport()).isEqualTo(TransportDto.SSE);
            Assertions.assertThat(t.getAllowedTools()).isEqualTo(List.of("branch", "remote"));
            Assertions.assertThat(t.getAuthSettings()).isEqualTo(expectedAuthSettings);
        });
    }

    @Test
    void testImport_WithConflict() throws IOException {
        // given
        String startConfigString = FileUtils.readFileToString(new File("src/test/resources/import_models.json"), StandardCharsets.UTF_8);
        MockMultipartFile startConfig = new MockMultipartFile(
                "file",
                "test.json",
                "application/json",
                startConfigString.getBytes()
        );

        configTransfer.importConfig(List.of(startConfig), overrideAndCreateRoleAndCreateNew());
        // when
        configTransfer.importConfig(List.of(startConfig), overrideAndCreateRoleAndCreateNew());
        // then
        Map<String, ModelDto> models = modelFacade.getAll().stream().collect(Collectors.toMap(ModelDto::getName, a -> a));
        Assertions.assertThat(models).containsOnlyKeys("testModel1", "testModel2");
        Assertions.assertThat(models.get("testModel1")).satisfies(model -> {
            Assertions.assertThat(model.getDisplayName()).isEqualTo("Test Model1");
            Assertions.assertThat(model.getDisplayVersion()).isEqualTo("2.0.0");
            Assertions.assertThat(model.getEndpoint()).isNull();
            Assertions.assertThat(model.getSource() instanceof ModelAdapterSource);
        });
    }

    @Test
    void testImportWithoutExistingRole_Exception() throws IOException {
        // given
        String config = FileUtils.readFileToString(new File("src/test/resources/import/configWithNotExistingRole.json"), StandardCharsets.UTF_8);
        MockMultipartFile mockFile = new MockMultipartFile(
                "file",
                "test.json",
                "application/json",
                config.getBytes()
        );
        // when
        Assertions.assertThatThrownBy(() -> configTransfer.importConfig(List.of(mockFile), overrideAndNotCreateRoleAndCreateNew()))
                //then
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Unable to find role: testRole1");
    }

    @Test
    void testImportWithoutExistingRole_CreateRole() throws IOException {
        // given
        String config = FileUtils.readFileToString(new File("src/test/resources/import/configWithNotExistingRole.json"), StandardCharsets.UTF_8);
        MockMultipartFile mockFile = new MockMultipartFile(
                "file",
                "test.json",
                "application/json",
                config.getBytes()
        );
        // when
        configTransfer.importConfig(List.of(mockFile), overrideAndCreateRoleAndCreateNew());
        // then
        RoleDto testRole = roleFacade.getRole("testRole1");
        Assertions.assertThat(testRole).isNotNull().satisfies(role -> {
            Assertions.assertThat(role.getLimits()).isNotEmpty();
            Assertions.assertThat(role.getLimits().get("testModel4")).isNotNull();
        });
    }

    @Test
    void testImport_RoleDoesNotExistInFileButExistsInDb() throws IOException {
        // given
        String startConfig = FileUtils.readFileToString(new File("src/test/resources/import/import_configWithoutAssistants.json"), StandardCharsets.UTF_8);
        MockMultipartFile startData = new MockMultipartFile(
                "file",
                "test.json",
                "application/json",
                startConfig.getBytes()
        );
        configTransfer.importConfig(List.of(startData), overrideAndCreateRoleAndCreateNew());

        String config = FileUtils.readFileToString(new File("src/test/resources/import/configWithNotExistingRole.json"), StandardCharsets.UTF_8);
        MockMultipartFile mockFile = new MockMultipartFile(
                "file",
                "test.json",
                "application/json",
                config.getBytes()
        );
        // when
        configTransfer.importConfig(List.of(mockFile), overrideAndCreateRoleAndCreateNew());
        // then
        ModelDto modelDto = modelFacade.getModel("testModel4");
        Assertions.assertThat(modelDto).isNotNull()
                .satisfies(model -> Assertions.assertThat(model.getRoleLimits()).containsOnlyKeys("testRole1"));
    }

    @Test
    void testImport_LimitForNotExistingRole_Exception() throws IOException {
        // given
        String config = FileUtils.readFileToString(new File("src/test/resources/configWithDefaultRoleAndNotExistingApp.json"), StandardCharsets.UTF_8);
        MockMultipartFile mockFile = new MockMultipartFile(
                "file",
                "test.json",
                "application/json",
                config.getBytes()
        );
        // when
        Assertions.assertThatThrownBy(() -> configTransfer.importConfig(List.of(mockFile), overrideAndCreateRoleAndCreateNew()))
                // then
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("unable to find deployments: [testApplication1]");
    }

    @Test
    void testImportDefaultRole_EntityDoesNotExistInFileButExistsInDb() throws IOException {
        // given
        String startConfig = FileUtils.readFileToString(new File("src/test/resources/import/import_configWithoutAssistants.json"), StandardCharsets.UTF_8);
        MockMultipartFile startData = new MockMultipartFile(
                "file",
                "test.json",
                "application/json",
                startConfig.getBytes()
        );
        configTransfer.importConfig(List.of(startData), overrideAndCreateRoleAndCreateNew());

        String config = FileUtils.readFileToString(new File("src/test/resources/configWithDefaultRoleAndNotExistingApp.json"), StandardCharsets.UTF_8);
        MockMultipartFile mockFile = new MockMultipartFile(
                "file",
                "test.json",
                "application/json",
                config.getBytes()
        );
        // when
        configTransfer.importConfig(List.of(mockFile), overrideAndCreateRoleAndCreateNew());
        // then
        ApplicationDto applicationDto = applicationFacade.getApplication("testApplication1");
        Assertions.assertThat(applicationDto).isNotNull().satisfies(app ->
                Assertions.assertThat(app.getDefaultRoleLimit()).isNotNull().satisfies(defaultRoleLimit -> {
                    Assertions.assertThat(defaultRoleLimit.isEnabled()).isTrue();
                    Assertions.assertThat(defaultRoleLimit.getMinute()).isEqualTo(null);
                    Assertions.assertThat(defaultRoleLimit.getDay()).isEqualTo(null);
                    Assertions.assertThat(defaultRoleLimit.getWeek()).isEqualTo(null);
                    Assertions.assertThat(defaultRoleLimit.getMonth()).isEqualTo(null);
                    Assertions.assertThat(defaultRoleLimit.getRequestHour()).isEqualTo(null);
                    Assertions.assertThat(defaultRoleLimit.getRequestDay()).isEqualTo(null);
                })
        );
        Assertions.assertThat(applicationDto.getRoleLimits()).isNotEmpty().satisfies(roleLimits ->
                Assertions.assertThat(roleLimits.get("default")).isNotNull().satisfies(defaultRoleLimit -> {
                    Assertions.assertThat(defaultRoleLimit.getMinute()).isEqualTo(100000000L);
                    Assertions.assertThat(defaultRoleLimit.getDay()).isEqualTo(100000000L);
                })
        );
    }

    @Test
    void testImportRole_EntityDoesNotExistInFileButExistsInDb() throws IOException {
        // given
        String startConfig = FileUtils.readFileToString(new File("src/test/resources/import/import_configWithoutAssistants.json"), StandardCharsets.UTF_8);
        MockMultipartFile startData = new MockMultipartFile(
                "file",
                "test.json",
                "application/json",
                startConfig.getBytes()
        );
        configTransfer.importConfig(List.of(startData), overrideAndCreateRoleAndCreateNew());

        String config = FileUtils.readFileToString(new File("src/test/resources/configWithRoleAndNotExistingApp.json"), StandardCharsets.UTF_8);
        MockMultipartFile mockFile = new MockMultipartFile(
                "file",
                "test.json",
                "application/json",
                config.getBytes()
        );
        // when
        configTransfer.importConfig(List.of(mockFile), overrideAndCreateRoleAndCreateNew());
        // then
        ApplicationDto applicationDto = applicationFacade.getApplication("testApplication1");
        Assertions.assertThat(applicationDto).isNotNull().satisfies(app ->
                Assertions.assertThat(app.getRoleLimits().get("testRole1")).isNotNull().satisfies(defaultRoleLimit -> {
                    Assertions.assertThat(defaultRoleLimit.getMinute()).isEqualTo(100000000L);
                    Assertions.assertThat(defaultRoleLimit.getDay()).isEqualTo(100000000L);
                })
        );
    }

    @Test
    void testImport_ModelAndRoleExist_ImportModelAndRoleWithDayLimit() {
        // given
        importModelAndRole();

        String config = ResourceUtils.readResource("/import/import_modelAndRoleWithDayLimit.json");
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.json",
                "application/json",
                config.getBytes()
        );

        // when
        configTransfer.importConfig(List.of(file), overrideAndCreateRoleAndCreateNew());

        // then
        ModelDto kkTestModel2 = modelFacade.getModel("KK_testModel2");
        Assertions.assertThat(kkTestModel2).isNotNull().satisfies(model -> {
                    Assertions.assertThat(model.getIsPublic()).isFalse();

                    var roleLimits = model.getRoleLimits();
                    Assertions.assertThat(roleLimits).hasSize(1);

                    var roleLimit = roleLimits.get("KK_testRole2");
                    Assertions.assertThat(roleLimit).isNotNull().satisfies(limitDto ->
                            assertDayWithRestUnlimitedRoleLimit(limitDto, 10L, true)
                    );
                }
        );

        RoleDto kkTestRole2 = roleFacade.getRole("KK_testRole2");
        Assertions.assertThat(kkTestRole2).isNotNull().satisfies(role -> {
                    var roleLimits = role.getLimits();
                    Assertions.assertThat(roleLimits).hasSize(1);

                    var roleLimit = roleLimits.get("KK_testModel2");
                    Assertions.assertThat(roleLimit).isNotNull().satisfies(limitDto ->
                            assertDayWithRestUnlimitedRoleLimit(limitDto, 10L, true)
                    );
                }
        );
    }

    @Test
    void testImport_TwoModelsAndRoleExist_ImportModelWithNewUserRolesAndExistingRoleWithNewLimit() {
        // given
        var dial22ModelDto = new ModelDto();
        dial22ModelDto.setName("DIAL22");
        dial22ModelDto.setDisplayName("DIAL22");
        dial22ModelDto.setIsPublic(true);
        modelFacade.createModel(dial22ModelDto);

        importModelAndRole();

        String config = ResourceUtils.readResource("/import/import_modelWithNewUserRolesAndExistingRoleWithNewLimit.json");
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.json",
                "application/json",
                config.getBytes()
        );

        // when
        configTransfer.importConfig(List.of(file), overrideAndCreateRoleAndCreateNew());

        // then
        ModelDto kkTestModel2 = modelFacade.getModel("KK_testModel2");
        Assertions.assertThat(kkTestModel2).isNotNull().satisfies(model -> {
                    Assertions.assertThat(model.getIsPublic()).isFalse();

                    var roleLimits = model.getRoleLimits();
                    Assertions.assertThat(roleLimits).hasSize(3);

                    var roleLimit1 = roleLimits.get("KK_testRole2");
                    Assertions.assertThat(roleLimit1).isNotNull().satisfies(this::assertUnlimitedDisabledRoleLimit);

                    var roleLimit2 = roleLimits.get("newRole2");
                    Assertions.assertThat(roleLimit2).isNotNull().satisfies(this::assertEmptyEnabledRoleLimit);

                    var roleLimit3 = roleLimits.get("newRole2");
                    Assertions.assertThat(roleLimit3).isNotNull().satisfies(this::assertEmptyEnabledRoleLimit);
                }
        );

        ModelDto dial22Model = modelFacade.getModel("DIAL22");
        Assertions.assertThat(dial22Model).isNotNull().satisfies(model -> {
                    Assertions.assertThat(model.getIsPublic()).isTrue();

                    var roleLimits = model.getRoleLimits();
                    Assertions.assertThat(roleLimits).hasSize(1);

                    var roleLimit = roleLimits.get("KK_testRole2");
                    Assertions.assertThat(roleLimit).isNotNull().satisfies(limitDto ->
                            assertDayWithRestUnlimitedRoleLimit(limitDto, 500L, false)
                    );
                }
        );

        RoleDto kkTestRole2 = roleFacade.getRole("KK_testRole2");
        Assertions.assertThat(kkTestRole2).isNotNull().satisfies(role -> {
                    var roleLimits = role.getLimits();
                    Assertions.assertThat(roleLimits).hasSize(2);

                    var roleLimit1 = roleLimits.get("KK_testModel2");
                    Assertions.assertThat(roleLimit1).isNotNull().satisfies(this::assertUnlimitedDisabledRoleLimit);

                    var roleLimit2 = roleLimits.get("DIAL22");
                    Assertions.assertThat(roleLimit2).isNotNull().satisfies(limitDto ->
                            assertDayWithRestUnlimitedRoleLimit(limitDto, 500L, false)
                    );
                }
        );

        RoleDto newRole1 = roleFacade.getRole("newRole1");
        Assertions.assertThat(newRole1).isNotNull().satisfies(role -> {
                    var roleLimits = role.getLimits();
                    Assertions.assertThat(roleLimits).hasSize(1);

                    var roleLimit = roleLimits.get("KK_testModel2");
                    Assertions.assertThat(roleLimit).isNotNull().satisfies(this::assertEmptyEnabledRoleLimit);
                }
        );

        RoleDto newRole2 = roleFacade.getRole("newRole2");
        Assertions.assertThat(newRole2).isNotNull().satisfies(role -> {
                    var roleLimits = role.getLimits();
                    Assertions.assertThat(roleLimits).hasSize(1);

                    var roleLimit = roleLimits.get("KK_testModel2");
                    Assertions.assertThat(roleLimit).isNotNull().satisfies(this::assertEmptyEnabledRoleLimit);
                }
        );
    }

    @Test
    void testImport_ModelAndRoleExist_ImportModelWithoutUserRolesAndRoleWithDayLimit() {
        // given
        importModelAndRole();

        String config = ResourceUtils.readResource("/import/import_modelWithoutUserRolesAndRoleWithDayLimit.json");
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.json",
                "application/json",
                config.getBytes()
        );

        // when
        configTransfer.importConfig(List.of(file), overrideAndCreateRoleAndCreateNew());

        // then
        ModelDto kkTestModel2 = modelFacade.getModel("KK_testModel2");
        Assertions.assertThat(kkTestModel2).isNotNull().satisfies(model -> {
                    Assertions.assertThat(model.getIsPublic()).isTrue();

                    var roleLimits = model.getRoleLimits();
                    Assertions.assertThat(roleLimits).hasSize(1);

                    var roleLimit = roleLimits.get("KK_testRole2");
                    Assertions.assertThat(roleLimit).isNotNull().satisfies(limitDto ->
                            assertDayWithRestUnlimitedRoleLimit(limitDto, 10L, false)
                    );
                }
        );

        RoleDto kkTestRole2 = roleFacade.getRole("KK_testRole2");
        Assertions.assertThat(kkTestRole2).isNotNull().satisfies(role -> {
                    var roleLimits = role.getLimits();
                    Assertions.assertThat(roleLimits).hasSize(1);

                    var roleLimit = roleLimits.get("KK_testModel2");
                    Assertions.assertThat(roleLimit).isNotNull().satisfies(limitDto ->
                            assertDayWithRestUnlimitedRoleLimit(limitDto, 10L, false)
                    );
                }
        );
    }

    @Test
    void testImport_ModelAndRoleExist_ImportModelWithNewUserRoles() {
        // given
        importModelAndRole();

        String config = ResourceUtils.readResource("/import/import_modelWithNewUserRoles.json");
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.json",
                "application/json",
                config.getBytes()
        );

        // when
        configTransfer.importConfig(List.of(file), overrideAndCreateRoleAndCreateNew());

        // then
        ModelDto kkTestModel2 = modelFacade.getModel("KK_testModel2");
        Assertions.assertThat(kkTestModel2).isNotNull().satisfies(model -> {
                    Assertions.assertThat(model.getIsPublic()).isFalse();

                    var roleLimits = model.getRoleLimits();
                    Assertions.assertThat(roleLimits).hasSize(3);

                    var roleLimit1 = roleLimits.get("KK_testRole2");
                    Assertions.assertThat(roleLimit1).isNotNull().satisfies(this::assertUnlimitedDisabledRoleLimit);

                    var roleLimit2 = roleLimits.get("newRole2");
                    Assertions.assertThat(roleLimit2).isNotNull().satisfies(this::assertEmptyEnabledRoleLimit);

                    var roleLimit3 = roleLimits.get("newRole2");
                    Assertions.assertThat(roleLimit3).isNotNull().satisfies(this::assertEmptyEnabledRoleLimit);
                }
        );

        RoleDto kkTestRole2 = roleFacade.getRole("KK_testRole2");
        Assertions.assertThat(kkTestRole2).isNotNull().satisfies(role -> {
                    var roleLimits = role.getLimits();
                    Assertions.assertThat(roleLimits).hasSize(1);

                    var roleLimit = roleLimits.get("KK_testModel2");
                    Assertions.assertThat(roleLimit).isNotNull().satisfies(this::assertUnlimitedDisabledRoleLimit);
                }
        );

        RoleDto newRole1 = roleFacade.getRole("newRole1");
        Assertions.assertThat(newRole1).isNotNull().satisfies(role -> {
                    var roleLimits = role.getLimits();
                    Assertions.assertThat(roleLimits).hasSize(1);

                    var roleLimit = roleLimits.get("KK_testModel2");
                    Assertions.assertThat(roleLimit).isNotNull().satisfies(this::assertEmptyEnabledRoleLimit);
                }
        );

        RoleDto newRole2 = roleFacade.getRole("newRole2");
        Assertions.assertThat(newRole2).isNotNull().satisfies(role -> {
                    var roleLimits = role.getLimits();
                    Assertions.assertThat(roleLimits).hasSize(1);

                    var roleLimit = roleLimits.get("KK_testModel2");
                    Assertions.assertThat(roleLimit).isNotNull().satisfies(this::assertEmptyEnabledRoleLimit);
                }
        );
    }

    @Test
    void testImport_ModelAndRoleExist_ImportModelWithoutUserRoles() {
        // given
        importModelAndRole();

        String config = ResourceUtils.readResource("/import/import_modelWithoutUserRoles.json");
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.json",
                "application/json",
                config.getBytes()
        );

        // when
        configTransfer.importConfig(List.of(file), overrideAndCreateRoleAndCreateNew());

        // then
        ModelDto kkTestModel2 = modelFacade.getModel("KK_testModel2");
        Assertions.assertThat(kkTestModel2).isNotNull().satisfies(model -> {
                    Assertions.assertThat(model.getIsPublic()).isTrue();

                    var roleLimits = model.getRoleLimits();
                    Assertions.assertThat(roleLimits).hasSize(1);

                    var roleLimit = roleLimits.get("KK_testRole2");
                    Assertions.assertThat(roleLimit).isNotNull().satisfies(this::assertUnlimitedDisabledRoleLimit);
                }
        );

        RoleDto kkTestRole2 = roleFacade.getRole("KK_testRole2");
        Assertions.assertThat(kkTestRole2).isNotNull().satisfies(role -> {
                    var roleLimits = role.getLimits();
                    Assertions.assertThat(roleLimits).hasSize(1);

                    var roleLimit = roleLimits.get("KK_testModel2");
                    Assertions.assertThat(roleLimit).isNotNull().satisfies(this::assertUnlimitedDisabledRoleLimit);
                }
        );
    }

    @Test
    void testImport_ModelAndRoleExist_ImportModelWithEmptyUserRoles() {
        // given
        importModelAndRole();

        String config = ResourceUtils.readResource("/import/import_modelWithEmptyUserRoles.json");
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.json",
                "application/json",
                config.getBytes()
        );

        // when
        configTransfer.importConfig(List.of(file), overrideAndCreateRoleAndCreateNew());

        // then
        ModelDto kkTestModel2 = modelFacade.getModel("KK_testModel2");
        Assertions.assertThat(kkTestModel2).isNotNull().satisfies(model -> {
                    Assertions.assertThat(model.getIsPublic()).isFalse();

                    var roleLimits = model.getRoleLimits();
                    Assertions.assertThat(roleLimits).hasSize(1);

                    var roleLimit = roleLimits.get("KK_testRole2");
                    Assertions.assertThat(roleLimit).isNotNull().satisfies(this::assertUnlimitedDisabledRoleLimit);
                }
        );

        RoleDto kkTestRole2 = roleFacade.getRole("KK_testRole2");
        Assertions.assertThat(kkTestRole2).isNotNull().satisfies(role -> {
                    var roleLimits = role.getLimits();
                    Assertions.assertThat(roleLimits).hasSize(1);

                    var roleLimit = roleLimits.get("KK_testModel2");
                    Assertions.assertThat(roleLimit).isNotNull().satisfies(this::assertUnlimitedDisabledRoleLimit);
                }
        );
    }

    @Test
    void testImport_ModelAndRoleExist_ImportRoleWithDayLimit() {
        // given
        importModelAndRole();

        String config = ResourceUtils.readResource("/import/import_roleWithDayLimit.json");
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.json",
                "application/json",
                config.getBytes()
        );

        // when
        configTransfer.importConfig(List.of(file), overrideAndCreateRoleAndCreateNew());

        // then
        ModelDto kkTestModel2 = modelFacade.getModel("KK_testModel2");
        Assertions.assertThat(kkTestModel2).isNotNull().satisfies(model -> {
                    Assertions.assertThat(model.getIsPublic()).isFalse();

                    var roleLimits = model.getRoleLimits();
                    Assertions.assertThat(roleLimits).hasSize(1);

                    var roleLimit = roleLimits.get("KK_testRole2");
                    Assertions.assertThat(roleLimit).isNotNull().satisfies(limitDto ->
                            assertDayWithRestUnlimitedRoleLimit(limitDto, 10L, true)
                    );
                }
        );

        RoleDto kkTestRole2 = roleFacade.getRole("KK_testRole2");
        Assertions.assertThat(kkTestRole2).isNotNull().satisfies(role -> {
                    var roleLimits = role.getLimits();
                    Assertions.assertThat(roleLimits).hasSize(1);

                    var roleLimit = roleLimits.get("KK_testModel2");
                    Assertions.assertThat(roleLimit).isNotNull().satisfies(limitDto ->
                            assertDayWithRestUnlimitedRoleLimit(limitDto, 10L, true)
                    );
                }
        );
    }

    @Test
    void testExport_CoreFormatModelWithInterceptor_FullRequest() throws IOException {
        // given
        Set<ExportConfigComponentType> componentTypes = Set.of(ExportConfigComponentType.MODEL,
                ExportConfigComponentType.INTERCEPTOR);
        FullExportRequest request = new FullExportRequest();
        request.setExportFormat(ExportFormat.CORE);
        request.setComponentTypes(componentTypes);

        InterceptorDto interceptorDto = createInterceptorDto("1");

        InterceptorRunnerDto runnerDto = new InterceptorRunnerDto();
        runnerDto.setName("someRunner");
        runnerDto.setDisplayName("someRunner");
        runnerDto.setCompletionEndpoint("https://endpoint.test.com/api");
        runnerDto.setConfigurationEndpoint("https://endpoint.test.com/config");
        runnerDto.setTopics(new TreeSet<>(Set.of("topic1", "topic2")));

        InterceptorRunnerSourceDto runnerSource = new InterceptorRunnerSourceDto("someRunner");
        interceptorDto.setSource(runnerSource);

        ModelDto modelDto = createModelDto("1");
        modelDto.setInterceptors(List.of("interceptor1"));

        interceptorRunnerFacade.createInterceptorRunner(runnerDto);
        interceptorFacade.createInterceptor(interceptorDto);
        modelFacade.createModel(modelDto);

        // when
        StreamingResponseBody streamingResponseBody = configTransfer.exportConfig(request);

        // then
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        streamingResponseBody.writeTo(outputStream);

        Config result = jsonMapper.readValue(outputStream.toString(), Config.class);

        Assertions.assertThat(result).isNotNull().satisfies(config -> {
            Assertions.assertThat(config.getModels()).isNotEmpty()
                    .containsOnlyKeys("model1")
                    .satisfies(models ->
                            Assertions.assertThat(models.get("model1").getInterceptors()).containsExactlyInAnyOrder("interceptor1"));
            Assertions.assertThat(config.getInterceptors()).isNotEmpty()
                    .containsOnlyKeys("interceptor1")
                    .satisfies(interceptors -> {
                        var interceptor = interceptors.get("interceptor1");
                        Assertions.assertThat(interceptor.getEndpoint()).isEqualTo("https://endpoint.test.com/api");
                        Assertions.assertThat(interceptor.getFeatures().getConfigurationEndpoint()).isEqualTo("https://endpoint.test.com/config");
                    });
            Assertions.assertThat(result.getApplications()).isEmpty();
        });
    }

    @Test
    void testExport_AdminFormatModelWithInterceptorAndRole_FullRequest() throws IOException {
        // given
        Set<ExportConfigComponentType> componentTypes = Set.of(ExportConfigComponentType.MODEL,
                ExportConfigComponentType.INTERCEPTOR,
                ExportConfigComponentType.ROLE);

        FullExportRequest request = new FullExportRequest();
        request.setExportFormat(ExportFormat.ADMIN);
        request.setComponentTypes(componentTypes);

        InterceptorDto interceptorDto = createInterceptorDto("1");
        RoleDto roleDto = createRoleDto("1");
        AdapterDto adapterDto = createAdapterDto("1");

        ModelDto modelDto = createModelDtoWithLimitsAndEndpoint("1");
        modelDto.setInterceptors(List.of("interceptor1"));
        modelDto.setSource(new ModelAdapterSourceDto("adapter1", "/chat/completions"));

        interceptorFacade.createInterceptor(interceptorDto);
        roleFacade.createRole(roleDto);
        adapterFacade.createAdapter(adapterDto);
        modelFacade.createModel(modelDto);

        ShareResourceLimitDto shareResourceLimitDto = new ShareResourceLimitDto();
        shareResourceLimitDto.setInvitationTtl(120L);
        shareResourceLimitDto.setMaxAcceptedUsers(10);
        roleDto.setShare(Map.of(ResourceTypeDto.APPLICATION, shareResourceLimitDto));
        roleDto.setLimits(Map.of("model1", new LimitDto()));

        roleFacade.updateRole("role1", roleDto, "*");

        // when
        StreamingResponseBody streamingResponseBody = configTransfer.exportConfig(request);

        // then
        ExportConfig result = extractConfigFromZip(streamingResponseBody);
        Assertions.assertThat(result).isNotNull().satisfies(config -> {
            Assertions.assertThat(config.getModels()).isNotEmpty()
                    .containsOnlyKeys("model1")
                    .satisfies(models -> {
                        Model model = models.get("model1");
                        Assertions.assertThat(model.getInterceptors()).containsExactlyInAnyOrder("interceptor1");
                        Assertions.assertThat(model.getDeployment().getRoleLimits()).isNull();
                        Assertions.assertThat(model.getSource()).isNull();
                    });
            Assertions.assertThat(config.getInterceptors()).isNotEmpty().containsOnlyKeys("interceptor1");
            Assertions.assertThat(config.getRoles()).isNotEmpty().containsKey("role1");
            Assertions.assertThat(config.getRoles().get("role1").getLimits()).isNotEmpty().hasSize(1).first()
                    .satisfies(limit -> Assertions.assertThat(limit.getDeploymentName()).isEqualTo("model1"));
            Assertions.assertThat(config.getRoles().get("role1").getShare()).isNotEmpty().hasSize(1)
                    .satisfies(share -> {
                        Assertions.assertThat(share.get(ResourceType.APPLICATION)).isNotNull();
                        Assertions.assertThat(share.get(ResourceType.APPLICATION).getMaxAcceptedUsers()).isEqualTo(10);
                        Assertions.assertThat(share.get(ResourceType.APPLICATION).getInvitationTtl()).isEqualTo(120);
                    });
            Assertions.assertThat(config.getApplications()).isEmpty();
            Assertions.assertThat(config.getAdapters()).isEmpty();
        });
    }

    @Test
    void testExport_AdminFormatApplicationAndAppTypeSchemaWithRoutes() throws IOException {
        // Given
        Set<ExportConfigComponentType> componentTypes = Set.of(ExportConfigComponentType.APPLICATION, ExportConfigComponentType.APPLICATION_TYPE_SCHEMA);
        FullExportRequest request = new FullExportRequest();
        request.setExportFormat(ExportFormat.ADMIN);
        request.setComponentTypes(componentTypes);

        // Routes
        var route1 = getDependentRouteDto("route1");
        var route2 = getDependentRouteDto("route2");
        List<DependentRouteDto> routeDtos = List.of(route1, route2);

        // Application
        ApplicationDto applicationDto = createBaseApplicationDto("1");
        applicationDto.setEndpoint("http://sample.com/app");
        applicationDto.setRoutes(routeDtos);

        applicationFacade.createApplication(applicationDto);

        // Application Type Schema
        ApplicationTypeSchemaDto typeSchemaDto = jsonMapper.readValue(getAppRunnerDto(), new TypeReference<>() {
        });
        typeSchemaDto.setApplicationTypeRoutes(routeDtos);

        applicationTypeSchemaFacade.create(typeSchemaDto);

        // When
        StreamingResponseBody streamingResponseBody = configTransfer.exportConfig(request);

        // Then
        ExportConfig result = extractConfigFromZip(streamingResponseBody);

        Assertions.assertThat(result).isNotNull().satisfies(config -> {
            Assertions.assertThat(config.getApplications()).isNotEmpty()
                    .containsOnlyKeys(applicationDto.getName())
                    .satisfies(apps -> {
                        List<DependentRoute> routes = apps.get(applicationDto.getName()).getRoutes();
                        Assertions.assertThat(routes).isNotEmpty();
                        Assertions.assertThat(routes.size()).isEqualTo(2);

                        DependentRoute route = routes.get(0);
                        Assertions.assertThat(route.getDescription()).isEqualTo(route1.getDescription());
                        Assertions.assertThat(route.getOrder()).isEqualTo(route1.getOrder());
                        Assertions.assertThat(route.getMaxRetryAttempts()).isEqualTo(route1.getMaxRetryAttempts());
                        Assertions.assertThat(route.getPaths()).isEqualTo(route1.getPaths());
                        Assertions.assertThat(route.getMethods()).isEqualTo(route1.getMethods());
                        Assertions.assertThat(route.getResponse())
                                .satisfies(response -> {
                                    Assertions.assertThat(response).isNotNull();
                                    Assertions.assertThat(response.getStatus()).isEqualTo(route1.getResponse().getStatus());
                                    Assertions.assertThat(response.getBody()).isEqualTo(route1.getResponse().getBody());
                                });
                        Assertions.assertThat(route.getPermissions())
                                .satisfies(permissions -> {
                                    assertTrue(permissions.contains(DependentRoute.ResourceAccessType.READ));
                                    assertTrue(permissions.contains(DependentRoute.ResourceAccessType.WRITE));
                                });
                        Assertions.assertThat(route.getAttachmentPaths())
                                .satisfies(attachmentPaths -> {
                                    Assertions.assertThat(attachmentPaths).isNotNull();
                                    Assertions.assertThat(attachmentPaths.getRequestBody()).isEqualTo(route1.getAttachmentPaths().getRequestBody());
                                    Assertions.assertThat(attachmentPaths.getResponseBody()).isEqualTo(route1.getAttachmentPaths().getResponseBody());
                                });
                        Assertions.assertThat(route.getUpstreams())
                                .satisfies(upstreams -> {
                                    Assertions.assertThat(upstreams).isNotEmpty();
                                    var upstream = upstreams.get(0);
                                    var expectedUpstream = route1.getUpstreams().get(0);
                                    Assertions.assertThat(upstream.getEndpoint()).isEqualTo(expectedUpstream.getEndpoint());
                                    Assertions.assertThat(upstream.getKey()).isEqualTo(expectedUpstream.getKey());
                                    Assertions.assertThat(upstream.getExtraData()).isEqualTo(expectedUpstream.getExtraData());
                                });
                    });

            final String schemaId = "https://test-schema-id.example";
            Assertions.assertThat(config.getApplicationRunners()).isNotEmpty()
                    .containsOnlyKeys(schemaId)
                    .satisfies(runner -> {
                        List<DependentRoute> routes = runner.get(schemaId).getApplicationTypeRoutes();
                        Assertions.assertThat(routes).isNotEmpty();
                        Assertions.assertThat(routes.size()).isEqualTo(2);

                        DependentRoute route = routes.get(0);
                        Assertions.assertThat(route.getDescription()).isEqualTo(route1.getDescription());
                        Assertions.assertThat(route.getOrder()).isEqualTo(route1.getOrder());
                        Assertions.assertThat(route.getMaxRetryAttempts()).isEqualTo(route1.getMaxRetryAttempts());
                        Assertions.assertThat(route.getPaths()).isEqualTo(route1.getPaths());
                        Assertions.assertThat(route.getMethods()).isEqualTo(route1.getMethods());
                        Assertions.assertThat(route.getResponse())
                                .satisfies(response -> {
                                    Assertions.assertThat(response).isNotNull();
                                    Assertions.assertThat(response.getStatus()).isEqualTo(route1.getResponse().getStatus());
                                    Assertions.assertThat(response.getBody()).isEqualTo(route1.getResponse().getBody());
                                });
                        Assertions.assertThat(route.getPermissions())
                                .satisfies(permissions -> {
                                    assertTrue(permissions.contains(DependentRoute.ResourceAccessType.READ));
                                    assertTrue(permissions.contains(DependentRoute.ResourceAccessType.WRITE));
                                });
                        Assertions.assertThat(route.getAttachmentPaths())
                                .satisfies(attachmentPaths -> {
                                    Assertions.assertThat(attachmentPaths).isNotNull();
                                    Assertions.assertThat(attachmentPaths.getRequestBody()).isEqualTo(route1.getAttachmentPaths().getRequestBody());
                                    Assertions.assertThat(attachmentPaths.getResponseBody()).isEqualTo(route1.getAttachmentPaths().getResponseBody());
                                });
                        Assertions.assertThat(route.getUpstreams())
                                .satisfies(upstreams -> {
                                    Assertions.assertThat(upstreams).isNotEmpty();
                                    var upstream = upstreams.get(0);
                                    var expectedUpstream = route1.getUpstreams().get(0);
                                    Assertions.assertThat(upstream.getEndpoint()).isEqualTo(expectedUpstream.getEndpoint());
                                    Assertions.assertThat(upstream.getKey()).isEqualTo(expectedUpstream.getKey());
                                    Assertions.assertThat(upstream.getExtraData()).isEqualTo(expectedUpstream.getExtraData());
                                });
                    });
        });
    }

    @Test
    void testExport_AdminFormatAll_FullRequestWithTopics() throws IOException, URISyntaxException {
        // Given
        FullExportRequest request = new FullExportRequest();
        request.setExportFormat(ExportFormat.ADMIN);
        request.setComponentTypes(Arrays.stream(ExportConfigComponentType.values()).collect(Collectors.toSet()));
        request.setTopics(Set.of("a", "b"));

        ApplicationTypeSchemaDto typeSchemaDto = jsonMapper.readValue(getAppRunnerDto(), new TypeReference<>() {
        });
        typeSchemaDto.setTopics(new TreeSet<>(Set.of("c", "d")));
        applicationTypeSchemaFacade.create(typeSchemaDto);

        URI customAppSchemaId = new URI("https://test-schema-id.example");

        ApplicationDto applicationDto1 = createBaseApplicationDto("1");
        applicationDto1.setCustomAppSchemaId(customAppSchemaId);
        applicationDto1.setTopics(new TreeSet<>(Set.of("b", "c", "d")));
        applicationFacade.createApplication(applicationDto1);

        ApplicationDto applicationDto2 = createBaseApplicationDto("2");
        applicationDto2.setCustomAppSchemaId(customAppSchemaId);
        applicationDto2.setTopics(new TreeSet<>(Set.of("c", "d")));
        applicationFacade.createApplication(applicationDto2);

        ModelDto modelDto1 = createModelDto("1");
        modelDto1.setTopics(new TreeSet<>(Set.of("a")));
        modelFacade.createModel(modelDto1);

        ModelDto modelDto2 = createModelDto("2");
        modelDto2.setTopics(new TreeSet<>(Set.of("c")));
        modelFacade.createModel(modelDto2);

        ToolSetDto toolSetDto1 = createToolSetDtoWithoutRoleLimits("1");
        toolSetDto1.setDescriptionKeywords(new TreeSet<>(Set.of("a", "b", "c")));
        toolSetFacade.createToolSet(toolSetDto1);

        ToolSetDto toolSetDto2 = createToolSetDtoWithoutRoleLimits("2");
        toolSetDto2.setDescriptionKeywords(new TreeSet<>(Set.of("e", "f")));
        toolSetFacade.createToolSet(toolSetDto2);

        KeyDto keyDto1 = createKeyDto("1");
        keyDto1.setTopics(new TreeSet<>(Set.of("a", "c")));
        keyFacade.createKey(keyDto1);

        KeyDto keyDto2 = createKeyDto("2");
        keyDto2.setTopics(new TreeSet<>(Set.of("b", "c")));
        keyFacade.createKey(keyDto2);

        RouteDto routeDto1 = createRouteDto("1");
        routeDto1.setTopics(new TreeSet<>(Set.of("c")));
        routeFacade.createRoute(routeDto1);

        RouteDto routeDto2 = createRouteDto("2");
        routeDto2.setTopics(new TreeSet<>(Set.of("b", "d")));
        routeFacade.createRoute(routeDto2);

        RoleDto roleDto1 = createRoleDto("1");
        roleDto1.setTopics(new TreeSet<>(Set.of("a")));
        roleFacade.createRole(roleDto1);

        RoleDto roleDto2 = createRoleDto("2");
        roleDto2.setTopics(new TreeSet<>(Set.of("c", "d", "e")));
        roleFacade.createRole(roleDto2);

        AdapterDto adapterDto1 = createAdapterDto("1");
        adapterDto1.setTopics(new TreeSet<>(Set.of("a", "c")));
        adapterFacade.createAdapter(adapterDto1);

        AdapterDto adapterDto2 = createAdapterDto("2");
        adapterDto2.setTopics(new TreeSet<>(Set.of("b", "c")));
        adapterFacade.createAdapter(adapterDto2);

        InterceptorDto interceptorDto1 = createInterceptorDto("1");
        interceptorDto1.setTopics(new TreeSet<>(Set.of("c")));
        interceptorFacade.createInterceptor(interceptorDto1);

        InterceptorDto interceptorDto2 = createInterceptorDto("2");
        interceptorDto2.setTopics(new TreeSet<>(Set.of("b", "c")));
        interceptorFacade.createInterceptor(interceptorDto2);

        InterceptorRunnerDto interceptorRunnerDto1 = new InterceptorRunnerDto();
        interceptorRunnerDto1.setTopics(new TreeSet<>(Set.of("b", "c")));
        interceptorRunnerDto1.setName("interceptorRunnerDto1");
        interceptorRunnerDto1.setDisplayName("interceptorRunnerDto1");
        interceptorRunnerFacade.createInterceptorRunner(interceptorRunnerDto1);

        // When
        StreamingResponseBody streamingResponseBody = configTransfer.exportConfig(request);

        // Then
        ExportConfig result = extractConfigFromZip(streamingResponseBody);

        Assertions.assertThat(result).isNotNull().satisfies(config -> {
            Assertions.assertThat(config.getApplicationRunners()).isNotEmpty()
                    .containsOnlyKeys(customAppSchemaId.toString());
            Assertions.assertThat(config.getApplications()).isNotEmpty()
                    .containsOnlyKeys(applicationDto1.getName())
                    .satisfies(apps -> {
                        Application app = apps.get(applicationDto1.getName());
                        Assertions.assertThat(app.getApplicationTypeSchemaId()).isEqualTo(customAppSchemaId);
                    });
            Assertions.assertThat(config.getModels()).isNotEmpty()
                    .containsOnlyKeys(modelDto1.getName());
            Assertions.assertThat(config.getToolsets()).isNotEmpty()
                    .containsOnlyKeys(toolSetDto1.getName());
            Assertions.assertThat(config.getKeys()).isNotEmpty()
                    .containsOnlyKeys(keyDto1.getName(), keyDto2.getName());
            Assertions.assertThat(config.getRoles()).isNotEmpty()
                    .containsOnlyKeys(roleDto1.getName());
            Assertions.assertThat(config.getRoutes()).isNotEmpty()
                    .containsOnlyKeys(routeDto2.getName());
            Assertions.assertThat(config.getAdapters()).isNotEmpty()
                    .containsOnlyKeys(adapterDto1.getName(), adapterDto2.getName());
            Assertions.assertThat(config.getInterceptors()).isNotEmpty()
                    .containsOnlyKeys(interceptorDto2.getName());
            Assertions.assertThat(config.getInterceptorRunners()).isNotEmpty()
                    .containsOnlyKeys(interceptorRunnerDto1.getName());
        });
    }

    @Test
    void testExport_AdminFormatApplication_FullRequestWithTopics() throws IOException, URISyntaxException {
        // Given
        FullExportRequest request = new FullExportRequest();
        request.setExportFormat(ExportFormat.ADMIN);
        request.setComponentTypes(Set.of(ExportConfigComponentType.APPLICATION));
        request.setTopics(Set.of("a", "b"));

        ApplicationTypeSchemaDto typeSchemaDto = jsonMapper.readValue(getAppRunnerDto(), new TypeReference<>() {
        });
        typeSchemaDto.setTopics(new TreeSet<>(Set.of("b", "a")));
        applicationTypeSchemaFacade.create(typeSchemaDto);

        URI customAppSchemaId = new URI("https://test-schema-id.example");

        ApplicationDto applicationDto1 = createBaseApplicationDto("1");
        applicationDto1.setCustomAppSchemaId(customAppSchemaId);
        applicationDto1.setTopics(new TreeSet<>(Set.of("b", "c", "d")));
        applicationFacade.createApplication(applicationDto1);

        // When
        StreamingResponseBody streamingResponseBody = configTransfer.exportConfig(request);

        // Then
        ExportConfig result = extractConfigFromZip(streamingResponseBody);

        Assertions.assertThat(result).isNotNull().satisfies(config -> {
            Assertions.assertThat(config.getApplicationRunners()).isEmpty();
            Assertions.assertThat(config.getApplications()).isNotEmpty()
                    .containsOnlyKeys(applicationDto1.getName())
                    .satisfies(apps -> {
                        Application app = apps.get(applicationDto1.getName());
                        Assertions.assertThat(app.getApplicationTypeSchemaId()).isNull();
                    });
        });
    }

    @Test
    void testExport_AdminFormatModelWithInterceptorAndRoleAndAdapter_FullRequest() throws IOException {
        // given
        Set<ExportConfigComponentType> componentTypes = Set.of(
                ExportConfigComponentType.MODEL,
                ExportConfigComponentType.INTERCEPTOR,
                ExportConfigComponentType.ROLE,
                ExportConfigComponentType.ADAPTER);

        FullExportRequest request = new FullExportRequest();
        request.setExportFormat(ExportFormat.ADMIN);
        request.setComponentTypes(componentTypes);

        InterceptorDto interceptorDto = createInterceptorDto("1");
        interceptorDto.setEndpoint("https://endpoint.test.com/interceptor");

        LimitDto limitDto = new LimitDto();
        limitDto.setMinute(5L);

        AdapterDto adapterDto = createAdapterDto("1");

        ModelDto modelDto = createModelDto("1");
        modelDto.setInterceptors(List.of("interceptor1"));
        modelDto.setRoleLimits(Map.of("role1", limitDto));
        modelDto.setSource(new ModelAdapterSourceDto("adapter1", "/chat/completions"));

        RoleDto roleDto = createRoleDto("1");

        interceptorFacade.createInterceptor(interceptorDto);
        roleFacade.createRole(roleDto);
        adapterFacade.createAdapter(adapterDto);
        modelFacade.createModel(modelDto);

        ShareResourceLimitDto shareResourceLimitDto = new ShareResourceLimitDto();
        shareResourceLimitDto.setInvitationTtl(120L);
        shareResourceLimitDto.setMaxAcceptedUsers(10);
        roleDto.setShare(Map.of(ResourceTypeDto.APPLICATION, shareResourceLimitDto));
        roleDto.setLimits(Map.of("model1", limitDto));

        roleFacade.updateRole("role1", roleDto, "*");

        // when
        StreamingResponseBody streamingResponseBody = configTransfer.exportConfig(request);

        // then
        ExportConfig result = extractConfigFromZip(streamingResponseBody);
        Assertions.assertThat(result).isNotNull().satisfies(config -> {
            Assertions.assertThat(config.getModels()).isNotEmpty()
                    .containsOnlyKeys("model1")
                    .satisfies(models -> {
                        Model model = models.get("model1");
                        Assertions.assertThat(model.getInterceptors()).containsExactlyInAnyOrder("interceptor1");
                        Assertions.assertThat(model.getDeployment().getRoleLimits()).isNull();
                        Assertions.assertThat(model.getSource()).isEqualTo(new ModelAdapterSource("adapter1", "/chat/completions"));
                    });
            Assertions.assertThat(config.getInterceptors()).isNotEmpty().containsOnlyKeys("interceptor1");
            Assertions.assertThat(config.getRoles()).isNotEmpty().containsKey("role1");
            Assertions.assertThat(config.getRoles().get("role1").getLimits()).isNotEmpty().hasSize(1).first()
                    .satisfies(limit -> Assertions.assertThat(limit.getDeploymentName()).isEqualTo("model1"));
            Assertions.assertThat(config.getRoles().get("role1").getShare()).isNotEmpty().hasSize(1)
                    .satisfies(share -> {
                        Assertions.assertThat(share.get(ResourceType.APPLICATION)).isNotNull();
                        Assertions.assertThat(share.get(ResourceType.APPLICATION).getMaxAcceptedUsers()).isEqualTo(10);
                        Assertions.assertThat(share.get(ResourceType.APPLICATION).getInvitationTtl()).isEqualTo(120);
                    });
            Assertions.assertThat(config.getApplications()).isEmpty();
            Assertions.assertThat(config.getAdapters()).isNotEmpty().hasSize(1)
                    .satisfies(adapters -> {
                        Adapter adapter = adapters.get("adapter1");
                        Assertions.assertThat(adapter.getName()).isEqualTo("adapter1");
                        Assertions.assertThat(adapter.getModels()).isEmpty();
                    });
        });
    }

    @Test
    void testExport_AdminFormatModelWithoutAdapter_SelectedItemsExportRequest() throws IOException {
        // given
        AdapterDto adapterDto = createAdapterDto("1");

        String completionEndpointPath = "/chat/completions";
        ModelDto modelDto = createModelDto("1");
        modelDto.setSource(new ModelAdapterSourceDto("adapter1", completionEndpointPath));

        String expectedEndpoint = adapterDto.getBaseEndpoint() + completionEndpointPath;

        adapterFacade.createAdapter(adapterDto);
        modelFacade.createModel(modelDto);

        SelectedItemsExportRequest request = new SelectedItemsExportRequest();
        request.setExportFormat(ExportFormat.ADMIN);
        request.setComponents(List.of(new ExportConfigComponent(
                "model1",
                ExportConfigComponentType.MODEL,
                Set.of()
        )));

        // when
        StreamingResponseBody streamingResponseBody = configTransfer.exportConfig(request);

        // then
        ExportConfig result = extractConfigFromZip(streamingResponseBody);

        Assertions.assertThat(result).isNotNull().satisfies(config -> {
            Assertions.assertThat(config.getModels())
                    .isNotEmpty()
                    .containsOnlyKeys("model1")
                    .satisfies(models -> {
                        Model model = models.get("model1");
                        Assertions.assertThat(model.getSource()).isNull();
                        Assertions.assertThat(model.getEndpoint()).isEqualTo(expectedEndpoint);
                    });
            Assertions.assertThat(config.getAdapters()).isEmpty();
        });
    }

    @Test
    void testExport_AdminFormatRouteWithRole_FullRequest() throws IOException {
        // given
        Set<ExportConfigComponentType> componentTypes = Set.of(ExportConfigComponentType.ROUTE,
                ExportConfigComponentType.ROLE);
        FullExportRequest request = new FullExportRequest();
        request.setExportFormat(ExportFormat.ADMIN);
        request.setComponentTypes(componentTypes);

        RouteDto routeDto = createRouteDtoWithLimits("1");
        routeDto.setPaths(List.of("/test/"));
        RoleDto roleDto = createRoleDto("1");
        LimitDto limitDto = new LimitDto();
        limitDto.setMinute(5L);
        ShareResourceLimitDto shareResourceLimitDto = new ShareResourceLimitDto();
        shareResourceLimitDto.setMaxAcceptedUsers(10);
        routeDto.setRoleLimits(Map.of("role1", limitDto));

        roleFacade.createRole(roleDto);
        routeFacade.createRoute(routeDto);
        // when
        StreamingResponseBody streamingResponseBody = configTransfer.exportConfig(request);
        // then
        ExportConfig result = extractConfigFromZip(streamingResponseBody);
        Assertions.assertThat(result).isNotNull().satisfies(config -> {
            Assertions.assertThat(config.getRoutes()).isNotEmpty().containsOnlyKeys("route1").satisfies(routes -> {
                        Assertions.assertThat(routes.get("route1").getDeployment().getRoleLimits()).isNull();
                    }
            );
            Assertions.assertThat(config.getRoles()).isNotEmpty().containsKey("role1");
            Assertions.assertThat(config.getRoles().get("role1").getLimits()).isNotEmpty().hasSize(1).first()
                    .satisfies(limit -> Assertions.assertThat(limit.getDeploymentName()).isEqualTo("route1"));
        });
    }

    @Test
    void testExport_CoreFormatAppWithAppRunnerAndInterceptor_FullRequest() throws IOException, URISyntaxException {
        // given
        Set<ExportConfigComponentType> componentTypes = Set.of(ExportConfigComponentType.APPLICATION,
                ExportConfigComponentType.INTERCEPTOR,
                ExportConfigComponentType.APPLICATION_TYPE_SCHEMA);
        FullExportRequest request = new FullExportRequest();
        request.setExportFormat(ExportFormat.CORE);
        request.setComponentTypes(componentTypes);
        InterceptorDto interceptorDto = createInterceptorDto("1");
        interceptorDto.setEndpoint("https://endpoint.test.com/interceptor");
        ApplicationDto applicationDto = createBaseApplicationDto("1");
        applicationDto.setInterceptors(List.of("interceptor1"));
        URI customAppSchemaId = new URI("https://test-schema-id.example");
        applicationDto.setCustomAppSchemaId(customAppSchemaId);
        var dto = jsonMapper.readValue(getAppRunnerDto(), new TypeReference<ApplicationTypeSchemaDto>() {
        });
        applicationTypeSchemaFacade.create(dto);
        interceptorFacade.createInterceptor(interceptorDto);
        applicationFacade.createApplication(applicationDto);
        // when
        StreamingResponseBody streamingResponseBody = configTransfer.exportConfig(request);
        // then
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        streamingResponseBody.writeTo(outputStream);

        Config result = jsonMapper.readValue(outputStream.toString(), Config.class);
        Assertions.assertThat(result).isNotNull().satisfies(config -> {
            Assertions.assertThat(config.getApplications()).isNotEmpty()
                    .containsOnlyKeys("application1")
                    .satisfies(apps -> {
                        Assertions.assertThat(apps.get("application1").getInterceptors()).containsExactlyInAnyOrder("interceptor1");
                        Assertions.assertThat(apps.get("application1").getApplicationTypeSchemaId()).isEqualTo(customAppSchemaId);
                    });
            Assertions.assertThat(config.getInterceptors()).isNotEmpty().containsOnlyKeys("interceptor1");
            Assertions.assertThat(config.getApplicationTypeSchemas()).isNotEmpty().containsOnlyKeys("https://test-schema-id.example");
            Assertions.assertThat(result.getModels()).isEmpty();
        });
    }

    @Test
    void testExport_CoreFormatAppDoesNotConformToAppRunner_FullRequest() throws IOException, URISyntaxException {
        // given
        FullExportRequest request = new FullExportRequest();
        request.setExportFormat(ExportFormat.CORE);
        request.setComponentTypes(Set.of(
                ExportConfigComponentType.APPLICATION,
                ExportConfigComponentType.APPLICATION_TYPE_SCHEMA
        ));

        URI customAppSchemaId = new URI("https://test-schema-id.example");
        ApplicationDto applicationDto = createBaseApplicationDto("1");
        applicationDto.setCustomAppSchemaId(customAppSchemaId);

        ApplicationTypeSchemaDto schemaDto = jsonMapper.readValue(
                getAppRunnerDtoWithRequiredFields(List.of("external_url")),
                new TypeReference<>() {
                }
        );

        applicationTypeSchemaFacade.create(schemaDto);
        applicationFacade.createApplication(applicationDto);

        // when
        StreamingResponseBody streamingResponseBody = configTransfer.exportConfig(request);

        // then
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        streamingResponseBody.writeTo(outputStream);

        Config result = jsonMapper.readValue(outputStream.toString(), Config.class);
        Assertions.assertThat(result).isNotNull().satisfies(config -> {
            Assertions.assertThat(config.getApplications()).isEmpty();
            Assertions.assertThat(config.getApplicationTypeSchemas()).isNotEmpty().containsOnlyKeys("https://test-schema-id.example");
        });
    }

    @Test
    void testExport_CoreFormatKeyWithoutRoles_FullRequest() throws IOException {
        // given
        FullExportRequest request = new FullExportRequest();
        request.setExportFormat(ExportFormat.CORE);
        request.setComponentTypes(Set.of(ExportConfigComponentType.KEY));

        KeyDto keyDto = createKeyDto("1");
        keyFacade.createKey(keyDto);

        // when
        StreamingResponseBody streamingResponseBody = configTransfer.exportConfig(request);

        // then
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        streamingResponseBody.writeTo(outputStream);

        Config result = jsonMapper.readValue(outputStream.toString(), Config.class);
        Assertions.assertThat(result).isNotNull().satisfies(config -> {
            Assertions.assertThat(config.getKeys()).isEmpty();
        });
    }

    @Test
    void testExport_AdminFormatInterceptorRunnerWithInterceptors_FullRequest() throws IOException {
        // given
        Set<ExportConfigComponentType> componentTypes = Set.of(ExportConfigComponentType.INTERCEPTOR_RUNNER,
                ExportConfigComponentType.INTERCEPTOR);
        FullExportRequest request = new FullExportRequest();
        request.setExportFormat(ExportFormat.ADMIN);
        request.setComponentTypes(componentTypes);

        // Create interceptor runner
        InterceptorRunnerDto runnerDto = new InterceptorRunnerDto();
        runnerDto.setName("testRunner");
        runnerDto.setDisplayName("Test Runner");
        runnerDto.setDescription("Test interceptor runner");
        runnerDto.setCompletionEndpoint("https://test.com/completion");
        runnerDto.setConfigurationEndpoint("https://test.com/configuration");
        runnerDto.setTopics(new TreeSet(Set.of("topic2", "topic1")));
        interceptorRunnerFacade.createInterceptorRunner(runnerDto);

        // Create interceptors associated with the runner
        InterceptorDto interceptor1 = createInterceptorDto("1");
        interceptor1.setSource(new InterceptorRunnerSourceDto("testRunner"));
        InterceptorDto interceptor2 = createInterceptorDto("2");
        interceptor2.setSource(new InterceptorRunnerSourceDto("testRunner"));
        interceptorFacade.createInterceptor(interceptor1);
        interceptorFacade.createInterceptor(interceptor2);

        // when
        StreamingResponseBody streamingResponseBody = configTransfer.exportConfig(request);

        // then
        ExportConfig result = extractConfigFromZip(streamingResponseBody);
        Assertions.assertThat(result).isNotNull().satisfies(config -> {
            Assertions.assertThat(config.getInterceptorRunners()).isNotEmpty()
                    .containsOnlyKeys("testRunner")
                    .satisfies(runners -> {
                        Assertions.assertThat(runners.get("testRunner").getDisplayName()).isEqualTo("Test Runner");
                        Assertions.assertThat(runners.get("testRunner").getDescription()).isEqualTo("Test interceptor runner");
                        Assertions.assertThat(runners.get("testRunner").getCompletionEndpoint()).isEqualTo("https://test.com/completion");
                        Assertions.assertThat(runners.get("testRunner").getConfigurationEndpoint()).isEqualTo("https://test.com/configuration");
                        // Verify interceptors are not included in the runner (they're exported separately)
                        Assertions.assertThat(runners.get("testRunner").getInterceptors()).isNull();
                    });

            Assertions.assertThat(config.getInterceptors()).isNotEmpty()
                    .containsOnlyKeys("interceptor1", "interceptor2")
                    .satisfies(interceptors -> {
                        Assertions.assertThat(((InterceptorRunnerSource) interceptors.get("interceptor1").getSource()).getRunnerName()).isEqualTo("testRunner");
                        Assertions.assertThat(((InterceptorRunnerSource) interceptors.get("interceptor2").getSource()).getRunnerName()).isEqualTo("testRunner");
                    });
        });
    }

    @Test
    void testExport_AdminFormatAppWithAppRunnerAndRole_FullRequest() throws IOException, URISyntaxException {
        // given
        Set<ExportConfigComponentType> componentTypes = Set.of(ExportConfigComponentType.APPLICATION,
                ExportConfigComponentType.ROLE,
                ExportConfigComponentType.APPLICATION_TYPE_SCHEMA);
        FullExportRequest request = new FullExportRequest();
        request.setExportFormat(ExportFormat.ADMIN);
        request.setComponentTypes(componentTypes);
        RoleDto roleDto = createRoleDto("1");
        ApplicationDto applicationDto = createBaseApplicationDto("1");
        LimitDto limitDto = new LimitDto();
        limitDto.setMinute(5L);
        ShareResourceLimitDto shareResourceLimitDto = new ShareResourceLimitDto();
        shareResourceLimitDto.setMaxAcceptedUsers(10);
        applicationDto.setRoleLimits(Map.of("role1", limitDto));
        URI customAppSchemaId = new URI("https://test-schema-id.example");
        applicationDto.setCustomAppSchemaId(customAppSchemaId);
        var dto = jsonMapper.readValue(getAppRunnerDto(), new TypeReference<ApplicationTypeSchemaDto>() {
        });
        applicationTypeSchemaFacade.create(dto);
        roleFacade.createRole(roleDto);
        applicationFacade.createApplication(applicationDto);
        // when
        StreamingResponseBody streamingResponseBody = configTransfer.exportConfig(request);
        // then
        ExportConfig result = extractConfigFromZip(streamingResponseBody);
        Assertions.assertThat(result).isNotNull().satisfies(config -> {
            Assertions.assertThat(config.getApplications()).isNotEmpty()
                    .containsOnlyKeys("application1")
                    .satisfies(apps -> {
                        Assertions.assertThat(apps.get("application1").getApplicationTypeSchemaId()).isEqualTo(customAppSchemaId);
                        Assertions.assertThat(apps.get("application1").getDeployment().getRoleLimits()).isNull();
                    });

            Assertions.assertThat(config.getApplicationRunners()).isNotEmpty().containsOnlyKeys("https://test-schema-id.example");
            Assertions.assertThat(config.getApplicationRunners().get("https://test-schema-id.example")).isNotNull();
            Assertions.assertThat(config.getApplicationRunners().get("https://test-schema-id.example").getApplications()).isNull();
            Assertions.assertThat(config.getRoles()).containsOnlyKeys("role1", "default");
            Assertions.assertThat(config.getRoles().get("role1").getLimits()).isNotEmpty().hasSize(1).first()
                    .satisfies(limit -> Assertions.assertThat(limit.getDeploymentName()).isEqualTo("application1"));
            Assertions.assertThat(result.getModels()).isEmpty();
        });
    }

    @Test
    void testExport_CoreFormatApplicationAndAppTypeSchemaWithRoutes() throws IOException {
        // Given
        String importConfig = FileUtils.readFileToString(new File("src/test/resources/import_for_export_dependent_routes.json"),
                StandardCharsets.UTF_8);
        MockMultipartFile mockFile = new MockMultipartFile(
                "file",
                "test.json",
                "application/json",
                importConfig.getBytes()
        );

        String expectedSchemaJson = FileUtils.readFileToString(new File("src/test/resources/app_type_schema_with_routes.json"),
                StandardCharsets.UTF_8);

        configTransfer.importConfig(List.of(mockFile), overrideAndCreateRoleAndCreateNew());

        Set<ExportConfigComponentType> componentTypes = Set.of(
                ExportConfigComponentType.APPLICATION, ExportConfigComponentType.APPLICATION_TYPE_SCHEMA
        );
        FullExportRequest request = new FullExportRequest();
        request.setExportFormat(ExportFormat.CORE);
        request.setComponentTypes(componentTypes);

        // When
        StreamingResponseBody streamingResponseBody = configTransfer.exportConfig(request);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        streamingResponseBody.writeTo(outputStream);

        Config result = jsonMapper.readValue(outputStream.toString(), Config.class);

        // Then
        Assertions.assertThat(result).isNotNull().satisfies(config -> {
            Assertions.assertThat(config.getApplications()).isNotEmpty()
                    .containsOnlyKeys("app-1")
                    .satisfies(apps -> {
                        var routes = apps.get("app-1").getRoutes();
                        Assertions.assertThat(routes).isNotEmpty();
                        Assertions.assertThat(routes.size()).isEqualTo(2);

                        CoreRoute route = routes.get("route1");
                        Assertions.assertThat(route.getName()).isEqualTo("route1");
                        Assertions.assertThat(route.getOrder()).isEqualTo(3);
                        Assertions.assertThat(route.getMaxRetryAttempts()).isEqualTo(0);
                        Assertions.assertThat(route.getPaths()).isNotEmpty();
                        Assertions.assertThat(route.getMethods()).isEqualTo(Set.of("POST", "GET"));
                        Assertions.assertThat(route.getResponse())
                                .satisfies(response -> {
                                    Assertions.assertThat(response).isNotNull();
                                    Assertions.assertThat(response.getStatus()).isEqualTo(200);
                                    Assertions.assertThat(response.getBody()).isEqualTo("success");
                                });
                        Assertions.assertThat(route.getPermissions())
                                .satisfies(permissions -> {
                                    assertTrue(permissions.contains(CoreRoute.ResourceAccessType.READ));
                                    assertTrue(permissions.contains(CoreRoute.ResourceAccessType.WRITE));
                                });
                        Assertions.assertThat(route.getAttachmentPaths())
                                .satisfies(attachmentPaths -> {
                                    Assertions.assertThat(attachmentPaths).isNotNull();
                                    Assertions.assertThat(attachmentPaths.getRequestBody()).isEqualTo(List.of("/first", "/second"));
                                    Assertions.assertThat(attachmentPaths.getResponseBody()).isEqualTo(List.of("/third"));
                                });
                        Assertions.assertThat(route.getUpstreams())
                                .satisfies(upstreams -> {
                                    Assertions.assertThat(upstreams).isNotEmpty();
                                    var upstream = upstreams.get(0);
                                    Assertions.assertThat(upstream.getEndpoint()).isEqualTo("http://upstream.com/api");
                                    Assertions.assertThat(upstream.getKey()).isEqualTo("123");
                                    Assertions.assertThat(upstream.getExtraData()).isEqualTo("{\"field1\":\"val1\"}");
                                });
                    });

            final String schemaId = "https://schema2";
            Assertions.assertThat(config.getApplicationTypeSchemas()).isNotEmpty()
                    .containsOnlyKeys(schemaId)
                    .satisfies(runner -> {
                        String schemaJson = runner.get(schemaId);
                        Assertions.assertThat(schemaJson).isNotEmpty();
                        JSONAssert.assertEquals(expectedSchemaJson, schemaJson, true);
                    });
        });
    }

    @ParameterizedTest
    @MethodSource("addSecrets")
    void testExport_CoreFormatKeyWithAllDependencies_SelectedItemsExportRequest(boolean addSecrets) throws IOException {
        // given
        String importConfig = FileUtils.readFileToString(new File("src/test/resources/import_for_export.json"),
                StandardCharsets.UTF_8);
        MockMultipartFile mockFile = new MockMultipartFile(
                "file",
                "test.json",
                "application/json",
                importConfig.getBytes()
        );

        // when
        configTransfer.importConfig(List.of(mockFile), overrideAndCreateRoleAndCreateNew());
        String testKey1Name = findKeyNameByProject("testProject1");

        SelectedItemsExportRequest request = new SelectedItemsExportRequest();
        request.setAddSecrets(addSecrets);
        request.setExportFormat(ExportFormat.CORE);
        request.setComponents(List.of(new ExportConfigComponent(
                        testKey1Name,
                        ExportConfigComponentType.KEY,
                        Set.of(ExportConfigComponentType.APPLICATION,
                                ExportConfigComponentType.MODEL,
                                ExportConfigComponentType.ROUTE,
                                ExportConfigComponentType.ROLE,
                                ExportConfigComponentType.INTERCEPTOR,
                                ExportConfigComponentType.APPLICATION_TYPE_SCHEMA)),
                new ExportConfigComponent(
                        "toolset1",
                        ExportConfigComponentType.TOOL_SET,
                        Set.of()
                )
        ));
        // when
        StreamingResponseBody streamingResponseBody = configTransfer.exportConfig(request);
        // then
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        streamingResponseBody.writeTo(outputStream);

        Config result = jsonMapper.readValue(outputStream.toString(), Config.class);
        Assertions.assertThat(result).isNotNull().satisfies(config -> {
            if (addSecrets) {
                Assertions.assertThat(config.getKeys()).containsOnlyKeys("testKey1")
                        .satisfies(keys -> {
                            Assertions.assertThat(keys.get("testKey1").getRoles()).containsExactly("default");
                            Assertions.assertThat(keys.get("testKey1").getAllowedIpAddressRanges())
                                    .containsExactly("198.51.100.14/24", "2002::1234:abcd:ffff:c0a8:101/64");
                        });
                Assertions.assertThat(config.getToolsets()).containsOnlyKeys("toolset1")
                        .satisfies(toolsets -> {
                            Assertions.assertThat(toolsets.get("toolset1").getAuthSettings().getClientId()).isEqualTo("some-client-id");
                            Assertions.assertThat(toolsets.get("toolset1").getAuthSettings().getClientSecret()).isEqualTo("some-client-secret");
                        });
            } else {
                Assertions.assertThat(config.getKeys()).isEmpty();
                Assertions.assertThat(config.getToolsets()).containsOnlyKeys("toolset1")
                        .satisfies(toolsets -> {
                            Assertions.assertThat(toolsets.get("toolset1").getAuthSettings().getClientId()).isEqualTo("some-client-id");
                            Assertions.assertThat(toolsets.get("toolset1").getAuthSettings().getClientSecret()).isNull();
                        });
            }
            Assertions.assertThat(config.getRoles()).containsOnlyKeys("default");
            Assertions.assertThat(config.getApplications()).isNotEmpty().containsOnlyKeys("testApplication1")
                    .satisfies(apps -> {
                        Assertions.assertThat(apps.get("testApplication1").getInterceptors()).containsExactlyInAnyOrder("testInterceptor1");
                        Assertions.assertThat(apps.get("testApplication1").getApplicationTypeSchemaId()).isEqualTo(new URI("https://test-schema-id.example"));
                    });
            Assertions.assertThat(config.getModels()).isNotEmpty().containsOnlyKeys("testModel1")
                    .satisfies(models ->
                            Assertions.assertThat(models.get("testModel1").getInterceptors()).containsExactlyInAnyOrder("testInterceptor1"));
            Assertions.assertThat(config.getRoutes()).isNotEmpty().containsOnlyKeys("test_route1");
            Assertions.assertThat(config.getInterceptors()).isNotEmpty().containsOnlyKeys("testInterceptor1");
            Assertions.assertThat(config.getApplicationTypeSchemas()).isNotEmpty().containsOnlyKeys("https://test-schema-id.example");
        });
    }

    @Test
    void testExport_CoreFormatRoleWithoutDependencies_SelectedItemsExportRequest() throws IOException {
        // given
        String importConfig = ResourceUtils.readResource("/import_for_export.json");
        MockMultipartFile mockFile = new MockMultipartFile(
                "file",
                "test.json",
                "application/json",
                importConfig.getBytes()
        );

        configTransfer.importConfig(List.of(mockFile), overrideAndCreateRoleAndCreateNew());

        SelectedItemsExportRequest request = new SelectedItemsExportRequest();
        request.setExportFormat(ExportFormat.CORE);
        request.setComponents(List.of(
                new ExportConfigComponent("testRole1", ExportConfigComponentType.ROLE, Set.of())
        ));

        // when
        StreamingResponseBody streamingResponseBody = configTransfer.exportConfig(request);

        // then
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        streamingResponseBody.writeTo(outputStream);

        Config result = jsonMapper.readValue(outputStream.toString(), Config.class);
        Assertions.assertThat(result).isNotNull().satisfies(config -> {
            Assertions.assertThat(config.getKeys()).isEmpty();
            Assertions.assertThat(config.getModels()).isEmpty();
            Assertions.assertThat(config.getApplications()).isEmpty();
            Assertions.assertThat(config.getRoutes()).isEmpty();
            Assertions.assertThat(config.getRoles()).containsOnlyKeys("testRole1")
                    .satisfies(roles -> {
                        var role = roles.get("testRole1");
                        Assertions.assertThat(role).isNotNull();
                        Assertions.assertThat(role.getLimits()).isEmpty();
                    });
        });
    }

    @Test
    void testExport_CoreFormatGlobalInterceptorsWithoutDependencies_SelectedItemsExportRequest() throws IOException {
        // given
        String importConfig = ResourceUtils.readResource("/import_for_export.json");
        MockMultipartFile mockFile = new MockMultipartFile(
                "file",
                "test.json",
                "application/json",
                importConfig.getBytes()
        );

        configTransfer.importConfig(List.of(mockFile), overrideAndCreateRoleAndCreateNew());

        SelectedItemsExportRequest request = new SelectedItemsExportRequest();
        request.setExportFormat(ExportFormat.CORE);
        request.setComponents(List.of(
                new ExportConfigComponent("globalInterceptors", ExportConfigComponentType.GLOBAL_INTERCEPTOR, Set.of())
        ));

        // when
        StreamingResponseBody streamingResponseBody = configTransfer.exportConfig(request);

        // then
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        streamingResponseBody.writeTo(outputStream);

        Config result = jsonMapper.readValue(outputStream.toString(), Config.class);
        Assertions.assertThat(result).isNotNull().satisfies(config -> {
            Assertions.assertThat(config.getKeys()).isEmpty();
            Assertions.assertThat(config.getModels()).isEmpty();
            Assertions.assertThat(config.getApplications()).isEmpty();
            Assertions.assertThat(config.getRoutes()).isEmpty();
            Assertions.assertThat(config.getGlobalInterceptors()).isEqualTo(List.of("testInterceptor2", "testInterceptor1"));
            Assertions.assertThat(config.getInterceptors()).isEmpty();
        });
    }

    @Test
    void testExport_CoreFormatGlobalInterceptorsWithDependencies_SelectedItemsExportRequest() throws IOException {
        // given
        String importConfig = ResourceUtils.readResource("/import_for_export.json");
        MockMultipartFile mockFile = new MockMultipartFile(
                "file",
                "test.json",
                "application/json",
                importConfig.getBytes()
        );

        configTransfer.importConfig(List.of(mockFile), overrideAndCreateRoleAndCreateNew());

        SelectedItemsExportRequest request = new SelectedItemsExportRequest();
        request.setExportFormat(ExportFormat.CORE);
        request.setComponents(List.of(
                new ExportConfigComponent("globalInterceptors", ExportConfigComponentType.GLOBAL_INTERCEPTOR,
                        Set.of(ExportConfigComponentType.INTERCEPTOR))
        ));

        // when
        StreamingResponseBody streamingResponseBody = configTransfer.exportConfig(request);

        // then
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        streamingResponseBody.writeTo(outputStream);

        Config result = jsonMapper.readValue(outputStream.toString(), Config.class);
        Assertions.assertThat(result).isNotNull().satisfies(config -> {
            Assertions.assertThat(config.getKeys()).isEmpty();
            Assertions.assertThat(config.getModels()).isEmpty();
            Assertions.assertThat(config.getApplications()).isEmpty();
            Assertions.assertThat(config.getRoutes()).isEmpty();
            Assertions.assertThat(config.getGlobalInterceptors()).isEqualTo(List.of("testInterceptor2", "testInterceptor1"));
            Assertions.assertThat(config.getInterceptors()).isNotEmpty().containsOnlyKeys("testInterceptor1", "testInterceptor2");
        });
    }

    @Test
    void testExport_CoreFormatGlobalInterceptors_FullRequest() throws IOException {
        // given
        String importConfig = ResourceUtils.readResource("/import_for_export.json");
        MockMultipartFile mockFile = new MockMultipartFile(
                "file",
                "test.json",
                "application/json",
                importConfig.getBytes()
        );

        configTransfer.importConfig(List.of(mockFile), overrideAndCreateRoleAndCreateNew());
        FullExportRequest request = new FullExportRequest();
        request.setExportFormat(ExportFormat.CORE);
        request.setComponentTypes(Set.of(ExportConfigComponentType.GLOBAL_INTERCEPTOR));

        // When
        StreamingResponseBody streamingResponseBody = configTransfer.exportConfig(request);

        // Then
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        streamingResponseBody.writeTo(outputStream);

        Config result = jsonMapper.readValue(outputStream.toString(), Config.class);

        Assertions.assertThat(result).isNotNull().satisfies(config -> {
            Assertions.assertThat(config.getInterceptors()).isEmpty();
            Assertions.assertThat(config.getGlobalInterceptors()).isEqualTo(List.of("testInterceptor2", "testInterceptor1"));
        });
    }

    @Test
    void testExport_CoreFormatGlobalInterceptorsAndInterceptors_FullRequest() throws IOException {
        // given
        String importConfig = ResourceUtils.readResource("/import_for_export.json");
        MockMultipartFile mockFile = new MockMultipartFile(
                "file",
                "test.json",
                "application/json",
                importConfig.getBytes()
        );

        configTransfer.importConfig(List.of(mockFile), overrideAndCreateRoleAndCreateNew());
        FullExportRequest request = new FullExportRequest();
        request.setExportFormat(ExportFormat.CORE);
        request.setComponentTypes(Set.of(ExportConfigComponentType.GLOBAL_INTERCEPTOR, ExportConfigComponentType.INTERCEPTOR));

        // When
        StreamingResponseBody streamingResponseBody = configTransfer.exportConfig(request);

        // Then
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        streamingResponseBody.writeTo(outputStream);

        Config result = jsonMapper.readValue(outputStream.toString(), Config.class);

        Assertions.assertThat(result).isNotNull().satisfies(config -> {
            Assertions.assertThat(config.getInterceptors()).isNotEmpty().containsOnlyKeys("testInterceptor1", "testInterceptor2", "testInterceptor3");
            Assertions.assertThat(config.getGlobalInterceptors()).isEqualTo(List.of("testInterceptor2", "testInterceptor1"));
        });
    }

    @Test
    void testExport_CoreFormatAll_FullRequest() throws IOException, JSONException {
        // given
        String importConfig = ResourceUtils.readResource("/import_for_export.json");
        MockMultipartFile mockFile = new MockMultipartFile(
                "file",
                "test.json",
                "application/json",
                importConfig.getBytes()
        );

        String expectedConfig = ResourceUtils.readResource("/full_core_export.json");

        doReturn(123L).when(transactionTimestampContext).getTimestamp();

        configTransfer.importConfig(List.of(mockFile), overrideAndCreateRoleAndCreateNew());

        FullExportRequest request = new FullExportRequest();
        request.setExportFormat(ExportFormat.CORE);
        request.setComponentTypes(Arrays.stream(ExportConfigComponentType.values()).collect(Collectors.toSet()));

        // when
        StreamingResponseBody streamingResponseBody = configTransfer.exportConfig(request);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        streamingResponseBody.writeTo(outputStream);

        // then
        JSONAssert.assertEquals(expectedConfig, outputStream.toString(), true);
    }

    @ParameterizedTest
    @MethodSource("addSecrets")
    void testExport_AdminFormatKeyWithAllDependencies_SelectedItemsExportRequest(boolean addSecrets, String expectedKey) throws IOException {
        // given
        String importConfig = FileUtils.readFileToString(new File("src/test/resources/import_for_export.json"),
                StandardCharsets.UTF_8);
        MockMultipartFile mockFile = new MockMultipartFile(
                "file",
                "test.json",
                "application/json",
                importConfig.getBytes()
        );

        configTransfer.importConfig(List.of(mockFile), overrideAndCreateRoleAndCreateNew());
        String testKey1Name = findKeyNameByProject("testProject1");

        SelectedItemsExportRequest request = new SelectedItemsExportRequest();
        request.setAddSecrets(addSecrets);
        request.setExportFormat(ExportFormat.ADMIN);
        request.setComponents(List.of(new ExportConfigComponent(
                testKey1Name,
                ExportConfigComponentType.KEY,
                Set.of(ExportConfigComponentType.APPLICATION,
                        ExportConfigComponentType.MODEL,
                        ExportConfigComponentType.ROUTE,
                        ExportConfigComponentType.ROLE,
                        ExportConfigComponentType.INTERCEPTOR,
                        ExportConfigComponentType.APPLICATION_TYPE_SCHEMA)
        )));
        // when
        StreamingResponseBody streamingResponseBody = configTransfer.exportConfig(request);
        // then
        ExportConfig result = extractConfigFromZip(streamingResponseBody);
        Assertions.assertThat(result).isNotNull().satisfies(config -> {
            Assertions.assertThat(config.getKeys()).containsOnlyKeys(testKey1Name)
                    .satisfies(keys -> {
                        Assertions.assertThat(keys.get(testKey1Name).getRoles()).containsExactly("default");
                        Assertions.assertThat(keys.get(testKey1Name).getKey()).isEqualTo(expectedKey);
                    });
            Assertions.assertThat(config.getRoles()).containsOnlyKeys("default");
            Assertions.assertThat(config.getApplications()).isNotEmpty().containsOnlyKeys("testApplication1")
                    .satisfies(apps -> {
                        Assertions.assertThat(apps.get("testApplication1").getInterceptors()).containsExactlyInAnyOrder("testInterceptor1");
                        Assertions.assertThat(apps.get("testApplication1").getApplicationTypeSchemaId()).isEqualTo(new URI("https://test-schema-id.example"));
                    });
            Assertions.assertThat(config.getModels()).isNotEmpty().containsOnlyKeys("testModel1")
                    .satisfies(models ->
                            Assertions.assertThat(models.get("testModel1").getInterceptors()).containsExactlyInAnyOrder("testInterceptor1"));
            Assertions.assertThat(config.getRoutes()).isNotEmpty().containsOnlyKeys("test_route1");
            Assertions.assertThat(config.getInterceptors()).isNotEmpty().containsOnlyKeys("testInterceptor1");
            Assertions.assertThat(config.getApplicationRunners()).isNotEmpty().containsOnlyKeys("https://test-schema-id.example");
        });
    }

    @Test
    void testImport_ImportGlobalInterceptorsWithOverride() throws IOException {
        // given
        String config = FileUtils.readFileToString(new File("src/test/resources/import/import_interceptorsAndGlobalInterceptors.json"), StandardCharsets.UTF_8);
        MockMultipartFile mockFile = new MockMultipartFile(
                "file",
                "test.json",
                "application/json",
                config.getBytes()
        );
        InterceptorDto interceptorDto1 = createInterceptorDto("1");
        InterceptorDto interceptorDto2 = createInterceptorDto("2");
        interceptorFacade.createInterceptor(interceptorDto1);
        interceptorFacade.createInterceptor(interceptorDto2);
        GlobalSettingsDto globalSettingsDto = new GlobalSettingsDto();
        globalSettingsDto.setGlobalInterceptors(List.of("interceptor1", "interceptor2", "interceptor2"));
        globalSettingsFacade.updateGlobalSettings(globalSettingsDto);

        configTransfer.importConfig(List.of(mockFile), new ConfigImportOptions(ConflictResolutionPolicy.OVERRIDE, true, true));

        Set<String> interceptorNames = interceptorFacade.getAllInterceptors().stream().map(InterceptorDto::getName).collect(Collectors.toSet());
        GlobalSettingsDto globalSettings = globalSettingsFacade.getGlobalSettings();
        Assertions.assertThat(interceptorNames).containsAll(Set.of("interceptor1", "interceptor2", "interceptor3"));
        Assertions.assertThat((globalSettings.getGlobalInterceptors()))
                .containsExactly("interceptor1", "interceptor1", "interceptor3");
    }

    @Test
    void testImport_ImportGlobalInterceptorsWithSkip() throws IOException {
        // given
        String config = FileUtils.readFileToString(new File("src/test/resources/import/import_interceptorsAndGlobalInterceptors.json"), StandardCharsets.UTF_8);
        MockMultipartFile mockFile = new MockMultipartFile(
                "file",
                "test.json",
                "application/json",
                config.getBytes()
        );
        InterceptorDto interceptorDto1 = createInterceptorDto("1");
        InterceptorDto interceptorDto2 = createInterceptorDto("2");
        interceptorFacade.createInterceptor(interceptorDto1);
        interceptorFacade.createInterceptor(interceptorDto2);
        GlobalSettingsDto globalSettingsDto = new GlobalSettingsDto();
        globalSettingsDto.setGlobalInterceptors(List.of("interceptor1", "interceptor1", "interceptor2"));
        globalSettingsFacade.updateGlobalSettings(globalSettingsDto);

        configTransfer.importConfig(List.of(mockFile), new ConfigImportOptions(ConflictResolutionPolicy.SKIP, true, true));

        Set<String> interceptorNames = interceptorFacade.getAllInterceptors().stream().map(InterceptorDto::getName).collect(Collectors.toSet());
        GlobalSettingsDto globalSettings = globalSettingsFacade.getGlobalSettings();
        Assertions.assertThat(interceptorNames).containsAll(Set.of("interceptor1", "interceptor2", "interceptor3"));
        Assertions.assertThat((globalSettings.getGlobalInterceptors()))
                .containsExactly("interceptor1", "interceptor1", "interceptor2");
    }

    @Test
    void testImport_ImportGlobalInterceptorsWithSkipAndGlobalInterceptorsIsEmpty() throws IOException {
        // given
        String config = FileUtils.readFileToString(new File("src/test/resources/import/import_interceptorsAndGlobalInterceptors.json"), StandardCharsets.UTF_8);
        MockMultipartFile mockFile = new MockMultipartFile(
                "file",
                "test.json",
                "application/json",
                config.getBytes()
        );
        InterceptorDto interceptorDto1 = createInterceptorDto("1");
        InterceptorDto interceptorDto2 = createInterceptorDto("2");
        interceptorFacade.createInterceptor(interceptorDto1);
        interceptorFacade.createInterceptor(interceptorDto2);
        GlobalSettingsDto globalSettingsDto = new GlobalSettingsDto();
        globalSettingsFacade.updateGlobalSettings(globalSettingsDto);

        configTransfer.importConfig(List.of(mockFile), new ConfigImportOptions(ConflictResolutionPolicy.SKIP, true, true));

        Set<String> interceptorNames = interceptorFacade.getAllInterceptors().stream().map(InterceptorDto::getName).collect(Collectors.toSet());
        GlobalSettingsDto globalSettings = globalSettingsFacade.getGlobalSettings();
        Assertions.assertThat(interceptorNames).containsAll(Set.of("interceptor1", "interceptor2", "interceptor3"));
        Assertions.assertThat((globalSettings.getGlobalInterceptors()))
                .containsExactly("interceptor1", "interceptor1", "interceptor3");
    }

    @Test
    void testImport_ImportEmptyGlobalInterceptors() throws IOException {
        // given
        String config = FileUtils.readFileToString(new File("src/test/resources/import/import_interceptorsAndEmptyGlobalInterceptors.json"), StandardCharsets.UTF_8);
        MockMultipartFile mockFile = new MockMultipartFile(
                "file",
                "test.json",
                "application/json",
                config.getBytes()
        );
        InterceptorDto interceptorDto1 = createInterceptorDto("1");
        InterceptorDto interceptorDto2 = createInterceptorDto("2");
        interceptorFacade.createInterceptor(interceptorDto1);
        interceptorFacade.createInterceptor(interceptorDto2);
        GlobalSettingsDto globalSettingsDto = new GlobalSettingsDto();
        globalSettingsDto.setGlobalInterceptors(List.of("interceptor1", "interceptor1", "interceptor2"));
        globalSettingsFacade.updateGlobalSettings(globalSettingsDto);

        configTransfer.importConfig(List.of(mockFile), new ConfigImportOptions(ConflictResolutionPolicy.SKIP, true, true));

        Set<String> interceptorNames = interceptorFacade.getAllInterceptors().stream().map(InterceptorDto::getName).collect(Collectors.toSet());
        GlobalSettingsDto globalSettings = globalSettingsFacade.getGlobalSettings();
        Assertions.assertThat(interceptorNames).containsAll(Set.of("interceptor1", "interceptor2", "interceptor3"));
        Assertions.assertThat((globalSettings.getGlobalInterceptors()))
                .containsExactly("interceptor1", "interceptor1", "interceptor2");
    }

    @Test
    void testImport_ImportModelWithAdapter() throws IOException {
        // given
        String config = FileUtils.readFileToString(new File("src/test/resources/import/import_modelWithAdapter.json"), StandardCharsets.UTF_8);
        MockMultipartFile mockFile = new MockMultipartFile(
                "file",
                "test.json",
                "application/json",
                config.getBytes()
        );

        Assertions.assertThatThrownBy(() -> configTransfer
                        .importConfig(List.of(mockFile), new ConfigImportOptions(ConflictResolutionPolicy.OVERRIDE, true, false)))
                .hasMessageContaining("Unable to import adapters, adapter with endpoint http://endpoint2/ does not exist");
        configTransfer.importConfig(List.of(mockFile), new ConfigImportOptions(ConflictResolutionPolicy.OVERRIDE, true, true));

        Set<String> adapterNames = adapterFacade.getAllAdapters().stream().map(AdapterDto::getName).collect(Collectors.toSet());
        Set<String> adapterEndpoints = adapterFacade.getAllAdapters().stream().map(AdapterDto::getBaseEndpoint).collect(Collectors.toSet());
        Assertions.assertThat(adapterEndpoints).isEqualTo(Set.of("http://endpoint1/", "http://endpoint2/"));
        Map<String, ModelDto> models = modelFacade.getAll().stream().collect(Collectors.toMap(ModelDto::getName, Function.identity()));
        Assertions.assertThat(adapterNames).containsAll(Set.of(
                ((ModelAdapterSourceDto) models.get("testModel1").getSource()).adapterName(),
                ((ModelAdapterSourceDto) models.get("testModel2").getSource()).adapterName()
        ));
    }

    @Test
    void testImport_ImportModelWithAdapterConflictingByBaseEndpoint() throws IOException {
        // given
        AdapterDto adapterDto1 = createAdapterDto("1");
        adapterDto1.setBaseEndpoint("http://endpoint1/");
        AdapterDto adapterDto2 = createAdapterDto("2");
        adapterDto2.setBaseEndpoint("http://endpoint1/");
        AdapterDto adapterDto3 = createAdapterDto("3");
        adapterDto3.setBaseEndpoint("http://endpoint2/");

        adapterFacade.createAdapter(adapterDto1);
        adapterFacade.createAdapter(adapterDto2);
        adapterFacade.createAdapter(adapterDto3);

        String config = FileUtils.readFileToString(new File("src/test/resources/import/import_modelWithAdapter.json"), StandardCharsets.UTF_8);
        MockMultipartFile mockFile = new MockMultipartFile(
                "file",
                "test.json",
                "application/json",
                config.getBytes()
        );

        // when
        configTransfer.importConfig(List.of(mockFile), overrideAndCreateRoleAndCreateNew());

        // then
        Map<String, ModelDto> models = modelFacade.getAll().stream()
                .collect(Collectors.toMap(ModelDto::getName, Function.identity()));

        Assertions.assertThat(models.get("testModel1").getSource()).isEqualTo(new ModelAdapterSourceDto("adapter1", "testModel1/embeddings"));
        Assertions.assertThat(models.get("testModel2").getSource()).isEqualTo(new ModelAdapterSourceDto("adapter3", "testModel2/embeddings"));
    }

    private static Stream<Arguments> addSecrets() {
        return Stream.of(
                Arguments.of(false, null),
                Arguments.of(true, "testKey1")
        );
    }

    @ParameterizedTest
    @CsvSource({"true", "false"})
    void testExportPreview_CoreFormatKeyWithAllDependencies_SelectedItemsExportRequest(boolean addSecrets) throws IOException {
        // given
        String importConfig = FileUtils.readFileToString(new File("src/test/resources/import_for_export.json"),
                StandardCharsets.UTF_8);
        MockMultipartFile mockFile = new MockMultipartFile(
                "file",
                "test.json",
                "application/json",
                importConfig.getBytes()
        );

        // when
        configTransfer.importConfig(List.of(mockFile), overrideAndCreateRoleAndCreateNew());
        String testKey1Name = findKeyNameByProject("testProject1");

        SelectedItemsExportRequest request = new SelectedItemsExportRequest();
        request.setExportFormat(ExportFormat.CORE);
        request.setAddSecrets(addSecrets);
        request.setComponents(List.of(new ExportConfigComponent(
                testKey1Name,
                ExportConfigComponentType.KEY,
                Set.of(ExportConfigComponentType.APPLICATION,
                        ExportConfigComponentType.MODEL,
                        ExportConfigComponentType.ROUTE,
                        ExportConfigComponentType.ROLE,
                        ExportConfigComponentType.INTERCEPTOR,
                        ExportConfigComponentType.APPLICATION_TYPE_SCHEMA)
        )));
        // when
        ExportConfigPreview configPreview = configTransfer.exportPreview(request);
        // then
        Assertions.assertThat(configPreview).isNotNull()
                .satisfies(preview -> {
                    if (addSecrets) {
                        Assertions.assertThat(preview.getKeys()).hasSize(1).first()
                                .isInstanceOfSatisfying(ExportKeyInfo.class,
                                        key -> {
                                            Assertions.assertThatCode(() -> UUID.fromString(key.getName())).doesNotThrowAnyException();
                                            Assertions.assertThat(key.getRoles()).containsExactly("default");
                                        });
                    } else {
                        Assertions.assertThat(preview.getKeys()).isEmpty();
                    }
                    Assertions.assertThat(preview.getRoles()).hasSize(1).first()
                            .satisfies(role -> {
                                Assertions.assertThat(role.getName()).isEqualTo("default");
                                Assertions.assertThat(role.getDisplayName()).isEqualTo("default");
                            });
                    Assertions.assertThat(preview.getModels()).hasSize(1).first()
                            .satisfies(model -> {
                                Assertions.assertThat(model.getName()).isEqualTo("testModel1");
                                Assertions.assertThat(model.getName()).isEqualTo("testModel1");
                            });
                    Assertions.assertThat(preview.getApplications()).hasSize(1).first()
                            .satisfies(app -> {
                                Assertions.assertThat(app.getName()).isEqualTo("testApplication1");
                                Assertions.assertThat(app.getDisplayName()).isEqualTo("Test Application1");
                            });
                    Assertions.assertThat(preview.getRoutes()).hasSize(1).first()
                            .satisfies(route -> {
                                Assertions.assertThat(route.getName()).isEqualTo("test_route1");
                                Assertions.assertThat(route.getDisplayName()).isEqualTo("test_route1");
                            });
                    Assertions.assertThat(preview.getInterceptors()).hasSize(1).first()
                            .satisfies(interceptor -> {
                                Assertions.assertThat(interceptor.getName()).isEqualTo("testInterceptor1");
                                Assertions.assertThat(interceptor.getDisplayName()).isEqualTo("Test Interceptor1");
                            });
                    Assertions.assertThat(preview.getApplicationRunners()).hasSize(1).first()
                            .isInstanceOfSatisfying(ExportApplicationTypeSchemaInfo.class,
                                    appRunner -> Assertions.assertThat(appRunner.getId()).isEqualTo("https://test-schema-id.example"));
                });
    }

    @ParameterizedTest
    @CsvSource({"true", "false"})
    void testExportPreview_AdminFormatKeyWithAllDependencies_SelectedItemsExportRequest(boolean addSecrets) throws IOException {
        // given
        String importConfig = FileUtils.readFileToString(new File("src/test/resources/import_for_export.json"),
                StandardCharsets.UTF_8);
        MockMultipartFile mockFile = new MockMultipartFile(
                "file",
                "test.json",
                "application/json",
                importConfig.getBytes()
        );

        configTransfer.importConfig(List.of(mockFile), overrideAndCreateRoleAndCreateNew());
        String testKey1Name = findKeyNameByProject("testProject1");

        SelectedItemsExportRequest request = new SelectedItemsExportRequest();
        request.setExportFormat(ExportFormat.ADMIN);
        request.setAddSecrets(addSecrets);
        request.setComponents(List.of(new ExportConfigComponent(
                testKey1Name,
                ExportConfigComponentType.KEY,
                Set.of(ExportConfigComponentType.APPLICATION,
                        ExportConfigComponentType.MODEL,
                        ExportConfigComponentType.ROUTE,
                        ExportConfigComponentType.ROLE,
                        ExportConfigComponentType.INTERCEPTOR,
                        ExportConfigComponentType.APPLICATION_TYPE_SCHEMA)
        )));

        // when
        ExportConfigPreview configPreview = configTransfer.exportPreview(request);

        // then
        Assertions.assertThat(configPreview).isNotNull()
                .satisfies(preview -> {
                    Assertions.assertThat(preview.getKeys()).hasSize(1).first()
                            .isInstanceOfSatisfying(ExportKeyInfo.class,
                                    key -> {
                                        Assertions.assertThatCode(() -> UUID.fromString(key.getName())).doesNotThrowAnyException();
                                        Assertions.assertThat(key.getRoles()).containsExactly("default");
                                    });
                    Assertions.assertThat(preview.getRoles()).hasSize(1).first()
                            .satisfies(role -> {
                                Assertions.assertThat(role.getName()).isEqualTo("default");
                                Assertions.assertThat(role.getDisplayName()).isEqualTo("default");
                            });
                    Assertions.assertThat(preview.getModels()).hasSize(1).first()
                            .satisfies(model -> Assertions.assertThat(model.getName()).isEqualTo("testModel1"));
                    Assertions.assertThat(preview.getApplications()).hasSize(1).first()
                            .satisfies(app -> Assertions.assertThat(app.getName()).isEqualTo("testApplication1"));
                    Assertions.assertThat(preview.getRoutes()).hasSize(1).first()
                            .satisfies(route -> {
                                Assertions.assertThat(route.getName()).isEqualTo("test_route1");
                                Assertions.assertThat(route.getDisplayName()).isEqualTo("test_route1");
                            });
                    Assertions.assertThat(preview.getInterceptors()).hasSize(1).first()
                            .satisfies(interceptor -> Assertions.assertThat(interceptor.getName()).isEqualTo("testInterceptor1"));
                    Assertions.assertThat(preview.getApplicationRunners()).hasSize(1).first()
                            .isInstanceOfSatisfying(ExportApplicationTypeSchemaInfo.class,
                                    appRunner -> Assertions.assertThat(appRunner.getId()).isEqualTo("https://test-schema-id.example"));
                });
    }

    @Test
    void testExportPreview_AdminFormatModelWithAdapterDependencies_SelectedItemsExportRequest() {
        // given
        var adapter = createAdapterDto("1");
        adapterFacade.createAdapter(adapter);

        var model = createModelDtoWithAdapter("1");
        modelFacade.createModel(model);

        SelectedItemsExportRequest request = new SelectedItemsExportRequest();
        request.setExportFormat(ExportFormat.ADMIN);
        request.setAddSecrets(false);
        request.setComponents(List.of(new ExportConfigComponent(
                "model1",
                ExportConfigComponentType.MODEL,
                Set.of(ExportConfigComponentType.ADAPTER)
        )));

        // when
        ExportConfigPreview configPreview = configTransfer.exportPreview(request);

        // then
        Assertions.assertThat(configPreview).isNotNull()
                .satisfies(preview -> {
                    Assertions.assertThat(preview.getModels()).hasSize(1).first()
                            .satisfies(m -> Assertions.assertThat(m.getName()).isEqualTo("model1"));
                    Assertions.assertThat(preview.getAdapters()).hasSize(1).first()
                            .satisfies(a -> Assertions.assertThat(a.getName()).isEqualTo("adapter1"));
                });
    }

    @Test
    void testExportPreview_AdminFormatAppTypeSchemaWithInterceptorAndInterceptorRunnerDependencies_SelectedItemsExportRequest() throws IOException {
        // given
        InterceptorRunnerDto interceptorRunnerDto = createInterceptorRunnerDto("1");
        interceptorRunnerFacade.createInterceptorRunner(interceptorRunnerDto);

        InterceptorDto interceptorDto = createInterceptorDto("1");
        interceptorDto.setEndpoint(null);
        interceptorDto.setSource(new InterceptorRunnerSourceDto(interceptorRunnerDto.getName()));
        interceptorFacade.createInterceptor(interceptorDto);

        var appRunnerDto = jsonMapper.readValue(getAppRunnerDto(), new TypeReference<ApplicationTypeSchemaDto>() {
        });
        appRunnerDto.setInterceptors(List.of(interceptorDto.getName()));
        applicationTypeSchemaFacade.create(appRunnerDto);

        SelectedItemsExportRequest request = new SelectedItemsExportRequest();
        request.setExportFormat(ExportFormat.ADMIN);
        request.setAddSecrets(false);
        request.setComponents(List.of(new ExportConfigComponent(
                "https://test-schema-id.example",
                ExportConfigComponentType.APPLICATION_TYPE_SCHEMA,
                Set.of(ExportConfigComponentType.INTERCEPTOR, ExportConfigComponentType.INTERCEPTOR_RUNNER)
        )));


        // when
        StreamingResponseBody streamingResponseBody = configTransfer.exportConfig(request);

        // then
        ExportConfig result = extractConfigFromZip(streamingResponseBody);
        Assertions.assertThat(result).isNotNull()
                .satisfies(config -> {
                    Assertions.assertThat(config.getApplicationRunners()).hasSize(1)
                            .satisfies(schemas -> {
                                var schema = schemas.get(appRunnerDto.getId());
                                Assertions.assertThat(schema).isNotNull();
                                Assertions.assertThat(schema.getInterceptors()).isEqualTo(List.of(interceptorDto.getName()));
                            });
                    Assertions.assertThat(config.getInterceptors()).hasSize(1)
                            .satisfies(interceptors -> {
                                var interceptor = interceptors.get(interceptorDto.getName());
                                Assertions.assertThat(interceptor).isNotNull();
                                Assertions.assertThat(interceptor.getApplicationTypeSchemas()).isNull();
                                Assertions.assertThat(interceptor.getSource()).isEqualTo(new InterceptorRunnerSource(interceptorRunnerDto.getName()));
                            });
                    Assertions.assertThat(config.getInterceptorRunners()).hasSize(1)
                            .satisfies(interceptorRunners -> {
                                var interceptorRunner = interceptorRunners.get(interceptorRunnerDto.getName());
                                Assertions.assertThat(interceptorRunner).isNotNull();
                                Assertions.assertThat(interceptorRunner.getInterceptors()).isNull();
                            });
                });
    }

    @Test
    void testImportZip() throws IOException {
        // given
        var inputStream = getZipInputStreamWithAdminConfig();
        var zipFile = new MockMultipartFile("file", inputStream);

        // when
        configTransfer.importConfigZip(zipFile, overrideAndCreateRoleAndCreateNew());

        // then
        Map<String, ModelDto> models = modelFacade.getAll().stream().collect(Collectors.toMap(ModelDto::getName, a -> a));

        Assertions.assertThat(models).containsOnlyKeys("testModel1", "testModel2");
        Assertions.assertThat(models.get("testModel1")).satisfies(modelDto -> {
            Assertions.assertThat(modelDto.getRoleLimits()).containsOnlyKeys("testRole1", "testRole2", "testRole3");
            Assertions.assertThat(modelDto.getRoleLimits().get("testRole1")).satisfies(limit1 -> {
                Assertions.assertThat(limit1.isEnabled()).isTrue();
                Assertions.assertThat(limit1.getDay()).isEqualTo(1);
                Assertions.assertThat(limit1.getMinute()).isNull();
            });
            Assertions.assertThat(modelDto.getRoleLimits().get("testRole2"))
                    .satisfies(limit2 -> Assertions.assertThat(limit2.isEnabled()).isFalse());
            Assertions.assertThat(modelDto.getRoleLimits().get("testRole3")).satisfies(limit3 -> {
                Assertions.assertThat(limit3.isEnabled()).isTrue();
                Assertions.assertThat(limit3.getDay()).isNull();
                Assertions.assertThat(limit3.getMinute()).isNull();
            });
            Assertions.assertThat(modelDto.getInterceptors()).isNotEmpty()
                    .hasSize(1).first().isEqualTo("testInterceptor1");
            Assertions.assertThat(modelDto.getSource()).isEqualTo(new ModelAdapterSourceDto("adapter1", "embeddings"));
            Assertions.assertThat(modelDto.getDefaultRoleLimit()).satisfies(limit -> {
                Assertions.assertThat(limit.getDay()).isEqualTo(3);
            });
        });
        Assertions.assertThat(models.get("testModel2"))
                .satisfies(modelDto -> Assertions.assertThat(modelDto.getIsPublic()).isTrue());

        ApplicationDto application1 = applicationFacade.getApplication("testApplication1");
        ApplicationDto application2 = applicationFacade.getApplication("testApplication2");

        Assertions.assertThat(application1.getInterceptors()).hasSize(1).first().isEqualTo("testInterceptor1");
        Assertions.assertThat(application1.getCustomAppSchemaId().toString()).isEqualTo("https://test-schema-id.example");

        var appRoute = application2.getRoutes().get(0);
        var expectedRoute = getDependentRouteDto("route1");
        Assertions.assertThat(appRoute).isEqualTo(expectedRoute);

        Map<String, InterceptorDto> interceptors = interceptorFacade.getAllInterceptors().stream().collect(Collectors.toMap(InterceptorDto::getName, i -> i));

        Assertions.assertThat(interceptors.get("testInterceptor1")).satisfies(i -> {
            Assertions.assertThat(i.getEntities()).containsExactlyInAnyOrder("testModel1", "testApplication1");
            Assertions.assertThat(i.getDisplayName()).isEqualTo("testInterceptor1");
        });
        Assertions.assertThat(interceptors.get("testInterceptor2")).satisfies(i ->
                Assertions.assertThat(((InterceptorRunnerSourceDto) i.getSource()).runnerName()).isEqualTo("testRunner1")
        );

        Collection<InterceptorRunnerDto> interceptorRunners = interceptorRunnerFacade.getAllInterceptorRunners();

        Assertions.assertThat(interceptorRunners).hasSize(1).first().satisfies(r -> {
            Assertions.assertThat(r.getName()).isEqualTo("testRunner1");
            Assertions.assertThat(r.getCompletionEndpoint()).isEqualTo("https://template.test.com/api");
            Assertions.assertThat(r.getConfigurationEndpoint()).isEqualTo("https://template.test.com/conf");
        });

        Map<String, KeyDto> keys = keyFacade.getAllKeys().stream().collect(Collectors.toMap(KeyDto::getName, Function.identity()));
        Assertions.assertThat(keys.keySet()).containsExactlyInAnyOrder("testKey1", "testKey2");
        Assertions.assertThat(keys.get("testKey2")).satisfies(key -> Assertions.assertThat(key.getProject()).isEqualTo("_UNDEFINED_"));

        Map<String, AdapterDto> adapters = adapterFacade.getAllAdapters().stream().collect(Collectors.toMap(AdapterDto::getName, a -> a));

        Assertions.assertThat(adapters.get("adapter1")).satisfies(adapterDto -> {
            Assertions.assertThat(adapterDto.getName()).isEqualTo("adapter1");
            Assertions.assertThat(adapterDto.getBaseEndpoint()).isEqualTo("http://endpoint1/");
            Assertions.assertThat(adapterDto.getDescription()).isEqualTo("test adapter");
        });

        Map<String, ApplicationTypeSchemaDto> appTypeSchemas = applicationTypeSchemaFacade.getAll().stream()
                .collect(Collectors.toMap(ApplicationTypeSchemaDto::getId, a -> a));
        var appTypeSchema = appTypeSchemas.get("https://test-schema-id.example");
        var appTypeSchemaRoute = appTypeSchema.getApplicationTypeRoutes().get(0);
        Assertions.assertThat(appTypeSchemaRoute).isEqualTo(expectedRoute);

        Assertions.assertThat(roleFacade.getRole("testRole3")).satisfies(role -> {
            Assertions.assertThat(role.getShare()).hasSize(1).satisfies(share -> {
                ShareResourceLimitDto shareResourceLimit = share.get(ResourceTypeDto.APPLICATION);
                Assertions.assertThat(shareResourceLimit.getInvitationTtl()).isEqualTo(120);
                Assertions.assertThat(shareResourceLimit.getMaxAcceptedUsers()).isEqualTo(10);
            });
            Assertions.assertThat(role.getCostLimit()).satisfies(costLimit -> {
                Assertions.assertThat(costLimit.getMinute()).isEqualTo(BigDecimal.valueOf(111.222333444));
                Assertions.assertThat(costLimit.getDay()).isEqualTo(BigDecimal.valueOf(222));
                Assertions.assertThat(costLimit.getWeek()).isEqualTo(BigDecimal.valueOf(333));
                Assertions.assertThat(costLimit.getMonth()).isEqualTo(BigDecimal.valueOf(444));
            });
        });
    }

    @Test
    void testImportZip_WithConflict() throws IOException {
        var inputStream = getZipInputStreamWithAdminConfig();
        var zipFile = new MockMultipartFile("file", inputStream);

        configTransfer.importConfigZip(zipFile, overrideAndCreateRoleAndCreateNew());

        // when
        configTransfer.importConfigZip(zipFile, overrideAndCreateRoleAndCreateNew());

        // then
        Map<String, ModelDto> models = modelFacade.getAll().stream().collect(Collectors.toMap(ModelDto::getName, a -> a));

        Assertions.assertThat(models).containsOnlyKeys("testModel1", "testModel2");
        Assertions.assertThat(models.get("testModel1")).satisfies(modelDto -> {
            Assertions.assertThat(modelDto.getRoleLimits()).containsOnlyKeys("testRole1", "testRole2", "testRole3");
            Assertions.assertThat(modelDto.getRoleLimits().get("testRole1")).satisfies(limit1 -> {
                Assertions.assertThat(limit1.isEnabled()).isTrue();
                Assertions.assertThat(limit1.getDay()).isEqualTo(1);
                Assertions.assertThat(limit1.getMinute()).isNull();
            });
            Assertions.assertThat(modelDto.getRoleLimits().get("testRole2"))
                    .satisfies(limit2 -> Assertions.assertThat(limit2.isEnabled()).isFalse());
            Assertions.assertThat(modelDto.getRoleLimits().get("testRole3")).satisfies(limit3 -> {
                Assertions.assertThat(limit3.isEnabled()).isTrue();
                Assertions.assertThat(limit3.getDay()).isNull();
                Assertions.assertThat(limit3.getMinute()).isNull();
            });
            Assertions.assertThat(modelDto.getInterceptors()).isNotEmpty()
                    .hasSize(1).first().isEqualTo("testInterceptor1");
            Assertions.assertThat(modelDto.getSource()).isEqualTo(new ModelAdapterSourceDto("adapter1", "embeddings"));
        });
        Assertions.assertThat(models.get("testModel2"))
                .satisfies(modelDto -> Assertions.assertThat(modelDto.getIsPublic()).isTrue());

        ApplicationDto application1 = applicationFacade.getApplication("testApplication1");
        ApplicationDto application2 = applicationFacade.getApplication("testApplication2");

        Assertions.assertThat(application1.getInterceptors()).hasSize(1).first().isEqualTo("testInterceptor1");
        Assertions.assertThat(application1.getCustomAppSchemaId().toString()).isEqualTo("https://test-schema-id.example");

        var appRoute = application2.getRoutes().get(0);
        var expectedRoute = getDependentRouteDto("route1");
        Assertions.assertThat(appRoute).isEqualTo(expectedRoute);

        Map<String, InterceptorDto> interceptors = interceptorFacade.getAllInterceptors().stream().collect(Collectors.toMap(InterceptorDto::getName, i -> i));

        Assertions.assertThat(interceptors.get("testInterceptor1")).satisfies(i -> {
            Assertions.assertThat(i.getEntities()).containsExactlyInAnyOrder("testModel1", "testApplication1");
            Assertions.assertThat(i.getDisplayName()).isEqualTo("testInterceptor1");
        });
        Assertions.assertThat(interceptors.get("testInterceptor2")).satisfies(i ->
                Assertions.assertThat(((InterceptorRunnerSourceDto) i.getSource()).runnerName()).isEqualTo("testRunner1")
        );

        Collection<InterceptorRunnerDto> interceptorRunners = interceptorRunnerFacade.getAllInterceptorRunners();

        Assertions.assertThat(interceptorRunners).hasSize(1).first().satisfies(r -> {
            Assertions.assertThat(r.getName()).isEqualTo("testRunner1");
            Assertions.assertThat(r.getCompletionEndpoint()).isEqualTo("https://template.test.com/api");
            Assertions.assertThat(r.getConfigurationEndpoint()).isEqualTo("https://template.test.com/conf");
        });

        Map<String, KeyDto> keys = keyFacade.getAllKeys().stream().collect(Collectors.toMap(KeyDto::getName, Function.identity()));
        Assertions.assertThat(keys.keySet()).containsExactlyInAnyOrder("testKey1", "testKey2");
        Assertions.assertThat(keys.get("testKey2")).satisfies(key -> Assertions.assertThat(key.getProject()).isEqualTo("_UNDEFINED_"));

        Map<String, AdapterDto> adapters = adapterFacade.getAllAdapters().stream().collect(Collectors.toMap(AdapterDto::getName, a -> a));

        Assertions.assertThat(adapters.get("adapter1")).satisfies(adapterDto -> {
            Assertions.assertThat(adapterDto.getName()).isEqualTo("adapter1");
            Assertions.assertThat(adapterDto.getBaseEndpoint()).isEqualTo("http://endpoint1/");
            Assertions.assertThat(adapterDto.getDescription()).isEqualTo("test adapter");
        });

        Map<String, ApplicationTypeSchemaDto> appTypeSchemas = applicationTypeSchemaFacade.getAll().stream()
                .collect(Collectors.toMap(ApplicationTypeSchemaDto::getId, a -> a));
        var appTypeSchema = appTypeSchemas.get("https://test-schema-id.example");
        var appTypeSchemaRoute = appTypeSchema.getApplicationTypeRoutes().get(0);
        Assertions.assertThat(appTypeSchemaRoute).isEqualTo(expectedRoute);

        Assertions.assertThat(roleFacade.getRole("testRole3").getShare()).hasSize(1).satisfies(share -> {
            ShareResourceLimitDto shareResourceLimit = share.get(ResourceTypeDto.APPLICATION);
            Assertions.assertThat(shareResourceLimit.getInvitationTtl()).isEqualTo(120);
            Assertions.assertThat(shareResourceLimit.getMaxAcceptedUsers()).isEqualTo(10);
        });
    }

    @Test
    void testImport_ModelAndSpecificRoleDifferentFiles() throws IOException {
        // given
        String model = FileUtils.readFileToString(new File("src/test/resources/import/import_modelWithRoleDependency.json"), StandardCharsets.UTF_8);
        MockMultipartFile modelFile = new MockMultipartFile(
                "file",
                "test.json",
                "application/json",
                model.getBytes()
        );

        String role = FileUtils.readFileToString(new File("src/test/resources/import/import_roleWithModelDependency.json"), StandardCharsets.UTF_8);
        MockMultipartFile roleFile = new MockMultipartFile(
                "file",
                "test.json",
                "application/json",
                role.getBytes()
        );
        // when
        configTransfer.importConfig(List.of(modelFile, roleFile), overrideAndCreateRoleAndCreateNew());
        // then
        ModelDto modelDto = modelFacade.getModel("testModel1");
        Assertions.assertThat(modelDto).isNotNull().satisfies(importedModel ->
                Assertions.assertThat(importedModel.getRoleLimits().get("testRole1")).isNotNull().satisfies(roleLimit -> {
                    Assertions.assertThat(roleLimit.getMinute()).isEqualTo(Long.MAX_VALUE);
                    Assertions.assertThat(roleLimit.getDay()).isEqualTo(1L);
                })
        );
    }

    @Test
    void testImport_AppAndSameAppWithDependencies() throws IOException {
        // given
        String appWithDependencies = FileUtils.readFileToString(new File("src/test/resources/import/import_appWithAppRunnerAndInterceptor.json"), StandardCharsets.UTF_8);
        MockMultipartFile appWithDependenciesFile = new MockMultipartFile(
                "file",
                "test.json",
                "application/json",
                appWithDependencies.getBytes()
        );

        String appWithDependencies2 = FileUtils.readFileToString(new File("src/test/resources/import/import_appWithAppRunnerAndInterceptor2.json"), StandardCharsets.UTF_8);
        MockMultipartFile appWithDependencies2File = new MockMultipartFile(
                "file",
                "test.json",
                "application/json",
                appWithDependencies2.getBytes()
        );
        // when
        configTransfer.importConfig(List.of(appWithDependenciesFile, appWithDependencies2File), overrideAndCreateRoleAndCreateNew());
        // then
        ApplicationDto applicationDto = applicationFacade.getApplication("testApplication1");
        Assertions.assertThat(applicationDto).isNotNull().satisfies(app -> {
            Assertions.assertThat(app.getCustomAppSchemaId()).isEqualTo(new URI("https://test2-schema-id.example"));
            Assertions.assertThat(app.getInterceptors()).containsExactlyInAnyOrder("testInterceptor1", "testInterceptor2");
        });
    }

    @Test
    void testImport_AppWithLinkToAlreadyExistingAppRunner() throws IOException {
        // given
        var appRunnerDto = jsonMapper.readValue(getAppRunnerDto(), new TypeReference<ApplicationTypeSchemaDto>() {
        });
        applicationTypeSchemaFacade.create(appRunnerDto);

        String application = ResourceUtils.readResource("/import/import_applicationWithLinkToExistingAppRunner.json");
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.json",
                "application/json",
                application.getBytes()
        );

        // when
        configTransfer.importConfig(List.of(file), overrideAndCreateRoleAndCreateNew());

        // then
        ApplicationDto applicationDto = applicationFacade.getApplication("testApplication1");
        Assertions.assertThat(applicationDto).isNotNull().satisfies(app -> {
            Assertions.assertThat(app.getCustomAppSchemaId()).isEqualTo(new URI("https://test-schema-id.example"));
        });
    }

    @Test
    void testImportAddons_Successfully() throws IOException {
        // given
        String config = FileUtils.readFileToString(new File("src/test/resources/import/import_addons.json"),
                StandardCharsets.UTF_8);
        MockMultipartFile mockFile = new MockMultipartFile(
                "file",
                "test.json",
                "application/json",
                config.getBytes()
        );

        // when
        configTransfer.importConfig(List.of(mockFile), new ConfigImportOptions(ConflictResolutionPolicy.OVERRIDE));
        // then
        Collection<AddonDto> allAddons = addonFacade.getAllAddons();
        Assertions.assertThat(allAddons).isNotEmpty()
                .hasSize(1)
                .first().satisfies(addon -> {
                    Assertions.assertThat(addon.getName()).isEqualTo("testAddon1");
                    Assertions.assertThat(addon.getEndpoint()).isEqualTo("https://endpoint.test.com/embeddings");
                });
    }

    @Test
    void testImportAddons_UnsupportedException() throws IOException {
        // given
        String config = FileUtils.readFileToString(new File("src/test/resources/import/import_addons.json"),
                StandardCharsets.UTF_8);
        MockMultipartFile mockFile = new MockMultipartFile(
                "file",
                "test.json",
                "application/json",
                config.getBytes()
        );
        doThrow(new UnsupportedOperationException("Feature flag 'addonsSupported' is disabled.")).when(featureFlagAspect).evaluate(any(), any());
        // when
        org.assertj.core.api.Assertions.assertThatThrownBy(() -> configTransfer.importConfig(List.of(mockFile), new ConfigImportOptions(ConflictResolutionPolicy.OVERRIDE)))
                // then
                .isInstanceOf(UnsupportedOperationException.class)
                .hasMessageContaining("Feature flag 'addonsSupported' is disabled.");
    }

    @Test
    void testImportsAssistants_UnsupportedException() throws IOException {
        // given
        String config = FileUtils.readFileToString(new File("src/test/resources/import/import_assistants.json"),
                StandardCharsets.UTF_8);
        MockMultipartFile mockFile = new MockMultipartFile(
                "file",
                "test.json",
                "application/json",
                config.getBytes()
        );
        doThrow(new UnsupportedOperationException("Feature flag 'assistantsSupported' is disabled.")).when(featureFlagAspect).evaluate(any(), any());
        // when
        Assertions.assertThatThrownBy(() -> configTransfer.importConfig(List.of(mockFile), new ConfigImportOptions(ConflictResolutionPolicy.OVERRIDE)))
                .isInstanceOf(UnsupportedOperationException.class)
                .hasMessageContaining("Feature flag 'assistantsSupported' is disabled.");
    }

    @Test
    void testImportAssistants_SuccessfullyWithoutConflict() throws IOException {
        // given
        String config = FileUtils.readFileToString(new File("src/test/resources/import/import_assistants.json"),
                StandardCharsets.UTF_8);
        MockMultipartFile mockFile = new MockMultipartFile(
                "file",
                "test.json",
                "application/json",
                config.getBytes()
        );
        // when
        configTransfer.importConfig(List.of(mockFile), new ConfigImportOptions(ConflictResolutionPolicy.OVERRIDE));
        // then
        AssistantsPropertyDto assistantsPropertyDto = assistantsPropertyFacade.getAssistantsProperty();
        Collection<AssistantDto> allAssistants = assistantFacade.getAllAssistants();
        Assertions.assertThat(assistantsPropertyDto).isNotNull().satisfies(assistantsProperty ->
                Assertions.assertThat(assistantsProperty.getEndpoint()).isEqualTo("https://test-assistant.local/openai/deployments/assistant/chat/completions"));
        Assertions.assertThat(allAssistants).isNotEmpty().hasSize(1).first().satisfies(assistant -> {
            Assertions.assertThat(assistant.getName()).isEqualTo("testAssistant1");
            Assertions.assertThat(assistant.getIconUrl()).isEqualTo("https://test.com/icon");
        });
    }

    @Test
    void testImportAssistants_SuccessfullyOverride() throws IOException {
        // given
        String config = FileUtils.readFileToString(new File("src/test/resources/import/import_assistants_for_override.json"),
                StandardCharsets.UTF_8);
        MockMultipartFile mockFile = new MockMultipartFile(
                "file",
                "test.json",
                "application/json",
                config.getBytes()
        );
        // when
        configTransfer.importConfig(List.of(mockFile), new ConfigImportOptions(ConflictResolutionPolicy.OVERRIDE));
        // then
        AssistantsPropertyDto assistantsPropertyDto = assistantsPropertyFacade.getAssistantsProperty();
        Collection<AssistantDto> allAssistants = assistantFacade.getAllAssistants();
        Assertions.assertThat(assistantsPropertyDto).isNotNull().satisfies(assistantsProperty ->
                Assertions.assertThat(assistantsProperty.getEndpoint()).isEqualTo("https://test-assistant2.local/openai/deployments/assistant/chat/completions"));
        Assertions.assertThat(allAssistants).isNotEmpty().hasSize(1).first().satisfies(assistant -> {
            Assertions.assertThat(assistant.getName()).isEqualTo("testAssistant1");
            Assertions.assertThat(assistant.getIconUrl()).isEqualTo("https://test2.com/icon");
        });
    }

    @Test
    void testImportPreview() throws IOException {
        // given
        String config = FileUtils.readFileToString(new File("src/test/resources/import/import_configWithoutAssistants.json"),
                StandardCharsets.UTF_8);
        MockMultipartFile mockFile = new MockMultipartFile(
                "file",
                "test.json",
                "application/json",
                config.getBytes()
        );
        ConfigImportOptions configImportOptions = new ConfigImportOptions(ConflictResolutionPolicy.OVERRIDE, false, true);

        // when
        var importConfigPreview = configTransfer.importPreview(List.of(mockFile), configImportOptions);
        // then
        var expected = ResourceUtils.readResource("/import/import_preview.json");
        var expectedPreview = jsonMapper.readValue(expected, ImportConfigPreview.class);
        Assertions.assertThat(databaseService.isInitializedEmptyDatabase()).isTrue();
        Assertions.assertThat(importConfigPreview).usingRecursiveAssertion().isEqualTo(expectedPreview);
    }

    @Test
    void testImportZipPreview() throws IOException {
        // given
        var inputStream = getZipInputStreamWithAdminConfig();
        var zipFile = new MockMultipartFile("file", inputStream);
        // when
        var importConfigPreview = configTransfer.importPreviewZip(zipFile, ConflictResolutionPolicy.OVERRIDE);
        // then
        var expected = ResourceUtils.readResource("/import/import_zip_preview.json");
        var expectedPreview = jsonMapper.readValue(expected, ImportConfigPreview.class);
        Assertions.assertThat(databaseService.isInitializedEmptyDatabase()).isTrue();
        Assertions.assertThat(importConfigPreview).usingRecursiveAssertion().isEqualTo(expectedPreview);
    }

    @Test
    void testImportPreview_ImportModelWithAdapter() throws IOException {
        // given
        doReturn(123L).when(transactionTimestampContext).getTimestamp();

        AdapterDto adapterDto = createAdapterDto("1");
        adapterDto.setBaseEndpoint("http://endpoint1/");
        adapterFacade.createAdapter(adapterDto);

        String config = FileUtils.readFileToString(new File("src/test/resources/import/import_modelWithAdapter.json"), StandardCharsets.UTF_8);
        MockMultipartFile mockFile = new MockMultipartFile(
                "file",
                "test.json",
                "application/json",
                config.getBytes()
        );
        ConfigImportOptions configImportOptions = new ConfigImportOptions(ConflictResolutionPolicy.OVERRIDE, false, true);

        // when
        var importConfigPreview = configTransfer.importPreview(List.of(mockFile), configImportOptions);

        // then
        var expected = ResourceUtils.readResource("/import/import_modelWithAdapter_preview.json");
        var expectedPreview = jsonMapper.readValue(expected, ImportConfigPreview.class);
        Assertions.assertThat(importConfigPreview).usingRecursiveAssertion().isEqualTo(expectedPreview);
    }

    @SneakyThrows
    private InputStream getZipInputStreamWithAdminConfig() {
        byte[] data;
        try (
                var baos = new ByteArrayOutputStream();
                var zos = new ZipOutputStream(baos)
        ) {
            String config = ResourceUtils.readResource("/config_in_admin_format.json");
            ExportConfig exportConfig = jsonMapper.readValue(config, ExportConfig.class);
            zos.putNextEntry(new ZipEntry("aidial.config.json"));
            zos.write(jsonMapper.writeValueAsBytes(exportConfig));
            zos.closeEntry();

            zos.finish();

            data = baos.toByteArray();
        }

        return new ByteArrayInputStream(data);
    }

    private ExportConfig extractConfigFromZip(StreamingResponseBody streamingResponseBody) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        streamingResponseBody.writeTo(baos);
        try (InputStream zipInputStream = new ByteArrayInputStream(baos.toByteArray())) {
            ZipInputStream zis = new ZipInputStream(zipInputStream);
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                if ("aidial.config.json".equals(entry.getName())) {
                    ByteArrayOutputStream jsonBaos = new ByteArrayOutputStream();
                    byte[] buffer = new byte[1024];
                    int bytesRead;
                    while ((bytesRead = zis.read(buffer)) != -1) {
                        jsonBaos.write(buffer, 0, bytesRead);
                    }
                    String jsonContent = jsonBaos.toString(StandardCharsets.UTF_8);
                    return jsonMapper.readValue(jsonContent, ExportConfig.class);
                } else {
                    return null;
                }
            }
        }
        return null;
    }

    @Test
    void testExportCoreConfig_VersionFiltering() throws IOException {
        // given
        var routeDto = createRouteDto("1");
        routeFacade.createRoute(routeDto);
        int order = 5;
        String routeName = routeDto.getName();

        var request = new FullExportRequest();
        request.setExportFormat(ExportFormat.CORE);
        request.setComponentTypes(Set.of(ExportConfigComponentType.ROUTE));

        String originalVersion = versionProperties.getTarget();
        versionProperties.setTarget("0.30.0");

        try {
            // when
            StreamingResponseBody streamingResponseBody = configTransfer.exportConfig(request);

            // then
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            streamingResponseBody.writeTo(outputStream);

            Config result = jsonMapper.readValue(outputStream.toString(), Config.class);

            Assertions.assertThat(result).isNotNull();
            Assertions.assertThat(result.getRoutes()).containsKey(routeName);

            // this verifies versioning works correctly, since 'order' is not present in Route in v0.30.0
            var exportedRoute = result.getRoutes().get(routeName);
            Assertions.assertThat(exportedRoute.getOrder()).isNotEqualTo(order);

        } finally {
            versionProperties.setTarget(originalVersion);
        }
    }

    @Test
    void testExportCoreConfigFullExportRequest_ExportGlobalSettings() throws IOException {
        // given
        InterceptorDto interceptorDto1 = createInterceptorDto("1");
        InterceptorDto interceptorDto2 = createInterceptorDto("2");

        interceptorFacade.createInterceptor(interceptorDto1);
        interceptorFacade.createInterceptor(interceptorDto2);

        GlobalSettingsDto globalSettingsDto = new GlobalSettingsDto();
        globalSettingsDto.setGlobalInterceptors(List.of("interceptor1", "interceptor2", "interceptor2"));

        globalSettingsFacade.updateGlobalSettings(globalSettingsDto);

        var request = new FullExportRequest();
        request.setExportFormat(ExportFormat.CORE);
        request.setComponentTypes(Set.of(ExportConfigComponentType.GLOBAL_INTERCEPTOR));

        String originalVersion = versionProperties.getTarget();
        versionProperties.setTarget("0.38.0");

        try {
            // when
            StreamingResponseBody streamingResponseBody = configTransfer.exportConfig(request);

            // then
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            streamingResponseBody.writeTo(outputStream);

            Config result = jsonMapper.readValue(outputStream.toString(), Config.class);

            Assertions.assertThat(result).isNotNull();
            Assertions.assertThat(result.getGlobalInterceptors())
                    .containsExactly("interceptor1", "interceptor2", "interceptor2");
        } finally {
            versionProperties.setTarget(originalVersion);
        }
    }

    @Test
    void testExportCoreConfig_VersionFieldSerialization() throws IOException {
        // given
        var modelName = "versionFieldsModel";
        var author = "Test Author";
        var createdAt = Instant.parse("2025-06-01T12:00:00Z");
        var updatedAt = Instant.parse("2025-07-01T15:00:00Z");

        var modelDto = new ModelDto();
        modelDto.setName(modelName);
        modelDto.setDisplayName(modelName);
        modelDto.setAuthor(author);
        modelDto.setCreatedAt(createdAt);
        modelDto.setUpdatedAt(updatedAt);

        modelFacade.createModel(modelDto);

        var request = new FullExportRequest();
        request.setExportFormat(ExportFormat.CORE);
        request.setComponentTypes(Set.of(ExportConfigComponentType.MODEL));

        // Save original version setting
        String originalVersion = versionProperties.getTarget();

        try {
            // Part 1: Test with current version - author/createdAt/updatedAt should be present
            // when
            StreamingResponseBody streamingResponseBody = configTransfer.exportConfig(request);

            // then
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            streamingResponseBody.writeTo(outputStream);

            Config result = jsonMapper.readValue(outputStream.toString(), Config.class);

            Assertions.assertThat(result).isNotNull();
            Assertions.assertThat(result.getModels()).containsKey(modelName);

            var exportedModel = result.getModels().get(modelName);
            Assertions.assertThat(exportedModel.getAuthor()).isEqualTo(author);
            Assertions.assertThat(exportedModel.getCreatedAt()).isNotNull();
            Assertions.assertThat(exportedModel.getUpdatedAt()).isNotNull();
            Assertions.assertThat(exportedModel.getCreatedAt()).isNotEqualTo(createdAt.toEpochMilli());
            Assertions.assertThat(exportedModel.getUpdatedAt()).isNotEqualTo(updatedAt.toEpochMilli());

            // Part 2: Test with version 0.23.0 - author/createdAt/updatedAt should be absent
            // given
            versionProperties.setTarget("0.23.0");

            // when
            streamingResponseBody = configTransfer.exportConfig(request);

            // then
            outputStream = new ByteArrayOutputStream();
            streamingResponseBody.writeTo(outputStream);

            result = jsonMapper.readValue(outputStream.toString(), Config.class);

            Assertions.assertThat(result).isNotNull();
            Assertions.assertThat(result.getModels()).containsKey(modelName);

            exportedModel = result.getModels().get(modelName);
            Assertions.assertThat(exportedModel.getAuthor()).isNull();
            Assertions.assertThat(exportedModel.getCreatedAt()).isNull();
            Assertions.assertThat(exportedModel.getUpdatedAt()).isNull();
        } finally {
            // Restore original version setting
            versionProperties.setTarget(originalVersion);
        }
    }

    @Test
    void testExportCoreConfig_ApplicationTypeSchemaVersionFieldFiltering() throws IOException {
        // given
        String schemaId = "https://test-schema-id.example";

        String schemaDtoJson = ResourceUtils.readResource("/filtering/app_type_schema_dto.json");
        ApplicationTypeSchemaDto schemaDto = jsonMapper.readValue(
                schemaDtoJson,
                new TypeReference<>() {
                }
        );

        applicationTypeSchemaFacade.create(schemaDto);

        var request = new FullExportRequest();
        request.setExportFormat(ExportFormat.CORE);
        request.setComponentTypes(Set.of(ExportConfigComponentType.APPLICATION_TYPE_SCHEMA));

        // Save original version setting
        String originalVersion = versionProperties.getTarget();

        try {
            // Test with the latest version - applicationTypeIconUrl, applicationTypeRoutes and applicationTypePlaybackSupport should be present
            // when
            StreamingResponseBody streamingResponseBody = configTransfer.exportConfig(request);

            // then
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            streamingResponseBody.writeTo(outputStream);

            Config result = jsonMapper.readValue(outputStream.toString(), Config.class);

            String exportedSchemaJson = result.getApplicationTypeSchemas().get(schemaId);
            String expectedExportedCoreSchemaJson = ResourceUtils.readResource("/filtering/core_app_type_schema.json");
            assertEquals(expectedExportedCoreSchemaJson, exportedSchemaJson);

            // Test with version 0.28.0 - applicationTypeIconUrl, applicationTypeRoutes and applicationTypePlaybackSupport should be absent
            // given
            versionProperties.setTarget("0.28.0");

            // when
            streamingResponseBody = configTransfer.exportConfig(request);

            // then
            outputStream = new ByteArrayOutputStream();
            streamingResponseBody.writeTo(outputStream);

            result = jsonMapper.readValue(outputStream.toString(), Config.class);

            exportedSchemaJson = result.getApplicationTypeSchemas().get(schemaId);
            var exportedSchema = jsonMapper.readValue(exportedSchemaJson, CoreApplicationTypeSchema.class);
            String expectedExportedCoreSchemaWithoutRoutesJson = ResourceUtils.readResource("/filtering/core_app_type_schema_without_newer_fields.json");
            var expectedExportedCoreSchemaWithoutRoutes = jsonMapper.readValue(expectedExportedCoreSchemaWithoutRoutesJson, CoreApplicationTypeSchema.class);

            Assertions.assertThat(exportedSchema).usingRecursiveAssertion().isEqualTo(expectedExportedCoreSchemaWithoutRoutes);

        } finally {
            // Restore original version setting
            versionProperties.setTarget(originalVersion);
        }
    }

    private static DependentRouteDto getDependentRouteDto(String routeName) {
        var routeDto = new DependentRouteDto();
        routeDto.setName(routeName);
        routeDto.setDescription("some desc");
        routeDto.setPaths(List.of("/first", "/second"));
        routeDto.setMethods(Set.of("GET", "POST"));
        routeDto.setMaxRetryAttempts(5);
        routeDto.setOrder(1);
        routeDto.setPermissions(Set.of(ResourceAccessType.WRITE, ResourceAccessType.READ));

        var response = new ResponseDto();
        response.setStatus(200);
        response.setBody("success");
        routeDto.setResponse(response);

        var attachmentPath = new AttachmentPathDto();
        attachmentPath.setRequestBody(List.of("/one", "/two"));
        attachmentPath.setResponseBody(List.of("/three"));
        routeDto.setAttachmentPaths(attachmentPath);

        List<UpstreamDto> upstreams = new ArrayList<>();
        var upstream = new UpstreamDto();
        upstream.setEndpoint("http://sample.com");
        upstream.setKey("someKey");
        upstream.setExtraData("{\"key1\":\"val1\"}");
        upstreams.add(upstream);
        routeDto.setUpstreams(upstreams);

        return routeDto;
    }

    private String getAppRunnerDto() {
        return getAppRunnerDtoWithRequiredFields(null);
    }

    private String getAppRunnerDtoWithRequiredFields(List<String> requiredFields) {
        String requiredFieldsString = CollectionUtils.emptyIfNull(requiredFields).stream()
                .map(field -> "\"" + field + "\"")
                .collect(Collectors.joining(", "));
        return """
                {
                    "$id": "https://test-schema-id.example",
                    "dial:applicationTypeEditorUrl": "https://test.com/billings",
                    "dial:applicationTypeViewerUrl": "https://test.com/claims",
                    "dial:applicationTypeDisplayName": "runner display name",
                    "dial:applicationTypeCompletionEndpoint": "https://test.io/openai/deployments/mindmap/chat/completions",
                    "dial:applicationTypeConfigurationEndpoint": "https://test.com/conf",
                    "dial:applicationTypeRateEndpoint": "https://test.com/rate",
                    "dial:applicationTypeTokenizeEndpoint": "https://test.com/tokenize",
                    "dial:applicationTypeTruncatePromptEndpoint": "https://test.com/truncate-prompt",
                    "dial:appendApplicationPropertiesHeader": false,
                    "$defs": {},
                    "properties": {},
                    "required": [%s]
                }""".formatted(requiredFieldsString);
    }


    private ResourceAuthSettingsDto getAuthSettingsDto() {
        ResourceAuthSettingsDto authSettings = new ResourceAuthSettingsDto();
        authSettings.setAuthenticationType(AuthenticationTypeDto.API_KEY);
        authSettings.setClientId("some-client-id");
        authSettings.setClientSecret("some-client-secret");
        authSettings.setAuthorizationEndpoint("https://some-auth-endpoint");
        authSettings.setTokenEndpoint("https://some-token-endpoint");
        authSettings.setRedirectUri("https://some-redirect-uri");
        authSettings.setCodeChallenge("someCodeChallenge");
        authSettings.setCodeChallengeMethod("someCodeChallengeMethod");
        authSettings.setCodeVerifier("someCodeVerifier");
        authSettings.setApiKeyHeader("someApiKeyHeader");
        authSettings.setScopesSupported(List.of("first", "second"));
        return authSettings;
    }

    private String findKeyNameByProject(String project) {
        return keyFacade.getAllKeys().stream()
                .filter(key -> key.getProject().equals(project))
                .map(KeyDto::getName)
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Test failed. Key with project: '" + project + "' doesn't exist"));
    }

    private void importModelAndRole() {
        String modelAndRoleConfig = ResourceUtils.readResource("/import/import_modelAndRole.json");
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.json",
                "application/json",
                modelAndRoleConfig.getBytes()
        );
        configTransfer.importConfig(List.of(file), overrideAndCreateRoleAndCreateNew());
    }

    private void assertEmptyEnabledRoleLimit(LimitDto roleLimit) {
        assertEmptyRoleLimit(roleLimit, true);
    }

    private void assertEmptyDisabledRoleLimit(LimitDto roleLimit) {
        assertEmptyRoleLimit(roleLimit, false);
    }

    private void assertEmptyRoleLimit(LimitDto roleLimit, boolean enabled) {
        Assertions.assertThat(roleLimit).isNotNull().satisfies(limitDto -> {
            Assertions.assertThat(limitDto.isEnabled()).isEqualTo(enabled);
            Assertions.assertThat(limitDto.getMinute()).isNull();
            Assertions.assertThat(limitDto.getDay()).isNull();
            Assertions.assertThat(limitDto.getWeek()).isNull();
            Assertions.assertThat(limitDto.getMonth()).isNull();
            Assertions.assertThat(limitDto.getRequestHour()).isNull();
            Assertions.assertThat(limitDto.getRequestDay()).isNull();
        });
    }

    private void assertUnlimitedEnabledRoleLimit(LimitDto roleLimit) {
        assertUnlimitedRoleLimit(roleLimit, true);
    }

    private void assertUnlimitedDisabledRoleLimit(LimitDto roleLimit) {
        assertUnlimitedRoleLimit(roleLimit, false);
    }

    private void assertUnlimitedRoleLimit(LimitDto roleLimit, boolean enabled) {
        Assertions.assertThat(roleLimit).isNotNull().satisfies(limitDto -> {
            Assertions.assertThat(limitDto.isEnabled()).isEqualTo(enabled);
            Assertions.assertThat(limitDto.getMinute()).isEqualTo(Long.MAX_VALUE);
            Assertions.assertThat(limitDto.getDay()).isEqualTo(Long.MAX_VALUE);
            Assertions.assertThat(limitDto.getWeek()).isEqualTo(Long.MAX_VALUE);
            Assertions.assertThat(limitDto.getMonth()).isEqualTo(Long.MAX_VALUE);
            Assertions.assertThat(limitDto.getRequestHour()).isEqualTo(Long.MAX_VALUE);
            Assertions.assertThat(limitDto.getRequestDay()).isEqualTo(Long.MAX_VALUE);
        });
    }

    private void assertDayWithRestUnlimitedRoleLimit(LimitDto roleLimit, Long day, boolean enabled) {
        Assertions.assertThat(roleLimit).isNotNull().satisfies(limitDto -> {
            Assertions.assertThat(limitDto.isEnabled()).isEqualTo(enabled);
            Assertions.assertThat(limitDto.getMinute()).isEqualTo(Long.MAX_VALUE);
            Assertions.assertThat(limitDto.getDay()).isEqualTo(day);
            Assertions.assertThat(limitDto.getWeek()).isEqualTo(Long.MAX_VALUE);
            Assertions.assertThat(limitDto.getMonth()).isEqualTo(Long.MAX_VALUE);
            Assertions.assertThat(limitDto.getRequestHour()).isEqualTo(Long.MAX_VALUE);
            Assertions.assertThat(limitDto.getRequestDay()).isEqualTo(Long.MAX_VALUE);
        });
    }

    private static ConfigImportOptions overrideAndCreateRoleAndCreateNew() {
        return new ConfigImportOptions(ConflictResolutionPolicy.OVERRIDE, true, true);
    }

    private static ConfigImportOptions overrideAndNotCreateRoleAndCreateNew() {
        return new ConfigImportOptions(ConflictResolutionPolicy.OVERRIDE, false, true);
    }
}