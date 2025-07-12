package com.epam.aidial.cfg.functional.tests;

import com.epam.aidial.cfg.configuration.CoreConfigVersionProperties;
import com.epam.aidial.cfg.configuration.JsonMapperConfiguration;
import com.epam.aidial.cfg.domain.model.ExportApplicationTypeSchemaInfo;
import com.epam.aidial.cfg.domain.model.ExportConfig;
import com.epam.aidial.cfg.domain.model.ExportConfigComponentType;
import com.epam.aidial.cfg.domain.model.ExportConfigPreview;
import com.epam.aidial.cfg.domain.model.ExportFormat;
import com.epam.aidial.cfg.domain.model.ExportKeyInfo;
import com.epam.aidial.cfg.domain.model.ImportConfigPreview;
import com.epam.aidial.cfg.dto.AdapterDto;
import com.epam.aidial.cfg.dto.AddonDto;
import com.epam.aidial.cfg.dto.ApplicationDto;
import com.epam.aidial.cfg.dto.ApplicationTypeSchemaDto;
import com.epam.aidial.cfg.dto.AssistantDto;
import com.epam.aidial.cfg.dto.AssistantsPropertyDto;
import com.epam.aidial.cfg.dto.InterceptorDto;
import com.epam.aidial.cfg.dto.InterceptorRunnerDto;
import com.epam.aidial.cfg.dto.KeyDto;
import com.epam.aidial.cfg.dto.LimitDto;
import com.epam.aidial.cfg.dto.ModelDto;
import com.epam.aidial.cfg.dto.RoleDto;
import com.epam.aidial.cfg.dto.RouteDto;
import com.epam.aidial.cfg.dto.ShareResourceLimitDto;
import com.epam.aidial.cfg.exception.EntityNotFoundException;
import com.epam.aidial.cfg.features.flag.aspect.FeatureFlagGateEvaluationAspect;
import com.epam.aidial.cfg.model.ConfigImportOptions;
import com.epam.aidial.cfg.model.ExportConfigComponent;
import com.epam.aidial.cfg.model.FullExportRequest;
import com.epam.aidial.cfg.model.SelectedItemsExportRequest;
import com.epam.aidial.cfg.service.export.ConflictResolutionPolicy;
import com.epam.aidial.cfg.service.transfer.ConfigTransfer;
import com.epam.aidial.cfg.utils.ResourceUtils;
import com.epam.aidial.cfg.web.facade.AdapterFacade;
import com.epam.aidial.cfg.web.facade.AddonFacade;
import com.epam.aidial.cfg.web.facade.ApplicationFacade;
import com.epam.aidial.cfg.web.facade.ApplicationTypeSchemaFacade;
import com.epam.aidial.cfg.web.facade.AssistantFacade;
import com.epam.aidial.cfg.web.facade.AssistantsPropertyFacade;
import com.epam.aidial.cfg.web.facade.InterceptorFacade;
import com.epam.aidial.cfg.web.facade.InterceptorRunnerFacade;
import com.epam.aidial.cfg.web.facade.KeyFacade;
import com.epam.aidial.cfg.web.facade.ModelFacade;
import com.epam.aidial.cfg.web.facade.RoleFacade;
import com.epam.aidial.cfg.web.facade.RouteFacade;
import com.epam.aidial.core.config.Config;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.apache.commons.io.FileUtils;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import static org.mockito.ArgumentMatchers.any;
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
    private AssistantsPropertyFacade assistantsPropertyFacade;
    @Autowired
    private FeatureFlagGateEvaluationAspect featureFlagAspect;
    @Autowired
    private AdapterFacade adapterFacade;
    @Autowired
    private CoreConfigVersionProperties versionProperties;

    private final ObjectMapper jsonMapper = JsonMapperConfiguration.createJsonMapper();

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
            Assertions.assertThat(modelDto.getDefaults())
                    .containsExactlyInAnyOrderEntriesOf(Map.of("max_tokens", 8000));
        });
        Assertions.assertThat(models.get("testModel2"))
                .satisfies(modelDto -> Assertions.assertThat(modelDto.getIsPublic()).isTrue());
        ApplicationDto applicationDto = applicationFacade.getApplication("testApplication1");
        Assertions.assertThat(applicationDto.getInterceptors()).hasSize(1).first().isEqualTo("testInterceptor1");
        Assertions.assertThat(applicationDto.getCustomAppSchemaId().toString()).isEqualTo("https://test-schema-id.example");
        ApplicationDto applicationDto2 = applicationFacade.getApplication("testApplication2");
        Assertions.assertThat(applicationDto2.getDefaults()).containsExactlyInAnyOrderEntriesOf(Map.of("defaults_key", "defaults_value"));
        Collection<InterceptorDto> interceptors = interceptorFacade.getAllInterceptors();
        Assertions.assertThat(interceptors).isNotEmpty().hasSize(1).first().satisfies(i ->
                Assertions.assertThat(i.getEntities()).containsExactlyInAnyOrder("testModel1", "testApplication1"));
        Collection<String> allKeys = keyFacade.getAllKeys().stream().map(KeyDto::getName).toList();
        Assertions.assertThat(allKeys).containsExactlyInAnyOrder("testKey1", "testKey2");
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
                .hasMessageContaining("unable to find roles: [testRole1]");
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
                .hasMessageContaining("Deployment with name testApplication1 does not exist");
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
    void testExport_CoreFormatModelWithInterceptor_FullRequest() throws IOException {
        // given
        Set<ExportConfigComponentType> componentTypes = Set.of(ExportConfigComponentType.MODEL,
                ExportConfigComponentType.INTERCEPTOR);
        FullExportRequest request = new FullExportRequest();
        request.setExportFormat(ExportFormat.CORE);
        request.setComponentTypes(componentTypes);
        InterceptorDto interceptorDto = new InterceptorDto();
        interceptorDto.setName("interceptorName");
        interceptorDto.setEndpoint("https://endpoint.test.com/interceptor");
        ModelDto modelDto = new ModelDto();
        modelDto.setName("modelName");
        modelDto.setInterceptors(List.of("interceptorName"));
        var dto = jsonMapper.readValue(getAppRunnerDto(), new TypeReference<ApplicationTypeSchemaDto>() {
        });
        applicationTypeSchemaFacade.create(dto);
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
                    .containsOnlyKeys("modelName")
                    .satisfies(models ->
                            Assertions.assertThat(models.get("modelName").getInterceptors()).containsExactlyInAnyOrder("interceptorName"));
            Assertions.assertThat(config.getInterceptors()).isNotEmpty().containsOnlyKeys("interceptorName");
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

        InterceptorDto interceptorDto = new InterceptorDto();
        interceptorDto.setName("interceptorName");
        interceptorDto.setEndpoint("https://endpoint.test.com/interceptor");

        ModelDto modelDto = new ModelDto();
        modelDto.setName("modelName");
        modelDto.setInterceptors(List.of("interceptorName"));

        RoleDto roleDto = new RoleDto();
        roleDto.setName("testRole");

        LimitDto limitDto = new LimitDto();
        limitDto.setMinute(5L);
        modelDto.setRoleLimits(Map.of("testRole", limitDto));

        interceptorFacade.createInterceptor(interceptorDto);
        roleFacade.createRole(roleDto);
        modelFacade.createModel(modelDto);

        ShareResourceLimitDto shareResourceLimitDto = new ShareResourceLimitDto();
        shareResourceLimitDto.setInvitationTtl(120);
        shareResourceLimitDto.setMaxAcceptedUsers(10);
        roleDto.setShare(Map.of("modelName", shareResourceLimitDto));
        roleDto.setLimits(Map.of("modelName", limitDto));

        roleFacade.updateRole("testRole", roleDto);

        // when
        StreamingResponseBody streamingResponseBody = configTransfer.exportConfig(request);

        // then
        ExportConfig result = extractConfigFromZip(streamingResponseBody);
        Assertions.assertThat(result).isNotNull().satisfies(config -> {
            Assertions.assertThat(config.getModels()).isNotEmpty()
                    .containsOnlyKeys("modelName")
                    .satisfies(models -> {
                        Assertions.assertThat(models.get("modelName").getInterceptors()).containsExactlyInAnyOrder("interceptorName");
                        Assertions.assertThat(models.get("modelName").getDeployment().getRoleLimits()).isNull();
                    });
            Assertions.assertThat(config.getInterceptors()).isNotEmpty().containsOnlyKeys("interceptorName");
            Assertions.assertThat(config.getRoles()).isNotEmpty().containsKey("testRole");
            Assertions.assertThat(config.getRoles().get("testRole").getLimits()).isNotEmpty().hasSize(1).first()
                    .satisfies(limit -> Assertions.assertThat(limit.getDeploymentName()).isEqualTo("modelName"));
            Assertions.assertThat(config.getRoles().get("testRole").getShare()).isNotEmpty().hasSize(1).first()
                    .satisfies(share -> {
                        Assertions.assertThat(share.getDeploymentName()).isEqualTo("modelName");
                        Assertions.assertThat(share.getLimit()).isNotNull();
                        Assertions.assertThat(share.getLimit().getMaxAcceptedUsers()).isEqualTo(10);
                        Assertions.assertThat(share.getLimit().getInvitationTtl()).isEqualTo(120);
                    });
            Assertions.assertThat(config.getApplications()).isEmpty();
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

        RouteDto routeDto = new RouteDto();
        routeDto.setName("routeName");
        routeDto.setPaths(List.of("/test/"));
        RoleDto roleDto = new RoleDto();
        roleDto.setName("testRole");
        LimitDto limitDto = new LimitDto();
        limitDto.setMinute(5L);
        routeDto.setRoleLimits(Map.of("testRole", limitDto));

        roleFacade.createRole(roleDto);
        routeFacade.createRoute(routeDto);
        // when
        StreamingResponseBody streamingResponseBody = configTransfer.exportConfig(request);
        // then
        ExportConfig result = extractConfigFromZip(streamingResponseBody);
        Assertions.assertThat(result).isNotNull().satisfies(config -> {
            Assertions.assertThat(config.getRoutes()).isNotEmpty()
                    .containsOnlyKeys("routeName")
                    .satisfies(routes ->
                            Assertions.assertThat(routes.get("routeName").getDeployment().getRoleLimits()).isNull());
            Assertions.assertThat(config.getRoles()).isNotEmpty().containsKey("testRole");
            Assertions.assertThat(config.getRoles().get("testRole").getLimits()).isNotEmpty().hasSize(1).first()
                    .satisfies(limit -> Assertions.assertThat(limit.getDeploymentName()).isEqualTo("routeName"));
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
        InterceptorDto interceptorDto = new InterceptorDto();
        interceptorDto.setName("interceptorName");
        interceptorDto.setEndpoint("https://endpoint.test.com/interceptor");
        ApplicationDto applicationDto = new ApplicationDto();
        applicationDto.setName("applicationName");
        applicationDto.setInterceptors(List.of("interceptorName"));
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
                    .containsOnlyKeys("applicationName")
                    .satisfies(apps -> {
                        Assertions.assertThat(apps.get("applicationName").getInterceptors()).containsExactlyInAnyOrder("interceptorName");
                        Assertions.assertThat(apps.get("applicationName").getApplicationTypeSchemaId()).isEqualTo(customAppSchemaId);
                    });
            Assertions.assertThat(config.getInterceptors()).isNotEmpty().containsOnlyKeys("interceptorName");
            Assertions.assertThat(config.getApplicationTypeSchemas()).isNotEmpty().containsOnlyKeys("https://test-schema-id.example");
            Assertions.assertThat(result.getModels()).isEmpty();
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
        interceptorRunnerFacade.createInterceptorRunner(runnerDto);
        
        // Create interceptors associated with the runner
        InterceptorDto interceptor1 = new InterceptorDto();
        interceptor1.setName("testInterceptor1");
        interceptor1.setDescription("Test interceptor 1");
        interceptor1.setInterceptorRunner("testRunner");
        
        InterceptorDto interceptor2 = new InterceptorDto();
        interceptor2.setName("testInterceptor2");
        interceptor2.setDescription("Test interceptor 2");
        interceptor2.setInterceptorRunner("testRunner");
        
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
                    .containsOnlyKeys("testInterceptor1", "testInterceptor2")
                    .satisfies(interceptors -> {
                        Assertions.assertThat(interceptors.get("testInterceptor1").getInterceptorRunner()).isEqualTo("testRunner");
                        Assertions.assertThat(interceptors.get("testInterceptor2").getInterceptorRunner()).isEqualTo("testRunner");
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
        RoleDto roleDto = new RoleDto();
        roleDto.setName("testRole");
        ApplicationDto applicationDto = new ApplicationDto();
        applicationDto.setName("applicationName");
        LimitDto limitDto = new LimitDto();
        limitDto.setMinute(5L);
        applicationDto.setRoleLimits(Map.of("testRole", limitDto));
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
                    .containsOnlyKeys("applicationName")
                    .satisfies(apps -> {

                        Assertions.assertThat(apps.get("applicationName").getApplicationTypeSchemaId()).isEqualTo(customAppSchemaId);
                        Assertions.assertThat(apps.get("applicationName").getDeployment().getRoleLimits()).isNull();
                    });

            Assertions.assertThat(config.getApplicationRunners()).isNotEmpty().containsOnlyKeys("https://test-schema-id.example");
            Assertions.assertThat(config.getApplicationRunners().get("https://test-schema-id.example")).isNotNull();
            Assertions.assertThat(config.getApplicationRunners().get("https://test-schema-id.example").getApplications()).isNull();
            Assertions.assertThat(config.getRoles()).containsOnlyKeys("testRole", "default");
            Assertions.assertThat(config.getRoles().get("testRole").getLimits()).isNotEmpty().hasSize(1).first()
                    .satisfies(limit -> Assertions.assertThat(limit.getDeploymentName()).isEqualTo("applicationName"));
            Assertions.assertThat(result.getModels()).isEmpty();
        });
    }

    @ParameterizedTest
    @MethodSource("addSecrets")
    void testExport_CoreFormatKeyWithAllDependencies_SelectedItemsExportRequest(boolean addSecrets, String expectedKey) throws IOException {
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
        SelectedItemsExportRequest request = new SelectedItemsExportRequest();
        request.setAddSecrets(addSecrets);
        request.setExportFormat(ExportFormat.CORE);
        request.setComponents(List.of(new ExportConfigComponent(
                "testKey1",
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
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        streamingResponseBody.writeTo(outputStream);

        Config result = jsonMapper.readValue(outputStream.toString(), Config.class);
        Assertions.assertThat(result).isNotNull().satisfies(config -> {
            Assertions.assertThat(config.getKeys()).containsOnlyKeys("testKey1")
                    .satisfies(keys -> {
                        Assertions.assertThat(keys.get("testKey1").getRoles()).containsExactly("default");
                        Assertions.assertThat(keys.get("testKey1").getKey()).isEqualTo(expectedKey);
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
            Assertions.assertThat(config.getApplicationTypeSchemas()).isNotEmpty().containsOnlyKeys("https://test-schema-id.example");
        });
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
        Assertions.assertThat(adapterNames).containsAll(Set.of(models.get("testModel1").getAdapter(), models.get("testModel2").getAdapter()));
    }

    @Test
    void testImport_ImportModelWithAdapterConflictingByBaseEndpoint() throws IOException {
        // given
        AdapterDto adapterDto1 = new AdapterDto();
        adapterDto1.setName("adapter1");
        adapterDto1.setBaseEndpoint("http://endpoint1/");

        AdapterDto adapterDto2 = new AdapterDto();
        adapterDto2.setName("adapter2");
        adapterDto2.setBaseEndpoint("http://endpoint1/");

        AdapterDto adapterDto3 = new AdapterDto();
        adapterDto3.setName("adapter3");
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
        String model1AdapterName = models.get("testModel1").getAdapter();
        String model2AdapterName = models.get("testModel2").getAdapter();
        Assertions.assertThat(model1AdapterName).isEqualTo("adapter1");
        Assertions.assertThat(model2AdapterName).isEqualTo("adapter3");
    }

    private static Stream<Arguments> addSecrets() {
        return Stream.of(
                Arguments.of(false, null),
                Arguments.of(true, "testKey1")
        );
    }

    @Test
    void testExportPreview_CoreFormatKeyWithAllDependencies_SelectedItemsExportRequest() throws IOException {
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
        SelectedItemsExportRequest request = new SelectedItemsExportRequest();
        request.setExportFormat(ExportFormat.CORE);
        request.setComponents(List.of(new ExportConfigComponent(
                "testKey1",
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
                                        Assertions.assertThat(key.getName()).isEqualTo("testKey1");
                                        Assertions.assertThat(key.getRoles()).containsExactly("default");
                                    });
                    Assertions.assertThat(preview.getRoles()).hasSize(1).first()
                            .satisfies(role -> Assertions.assertThat(role.getName()).isEqualTo("default"));
                    Assertions.assertThat(preview.getModels()).hasSize(1).first()
                            .satisfies(model -> Assertions.assertThat(model.getName()).isEqualTo("testModel1"));
                    Assertions.assertThat(preview.getApplications()).hasSize(1).first()
                            .satisfies(app -> Assertions.assertThat(app.getName()).isEqualTo("testApplication1"));
                    Assertions.assertThat(preview.getRoutes()).hasSize(1).first()
                            .satisfies(route -> Assertions.assertThat(route.getName()).isEqualTo("test_route1"));
                    Assertions.assertThat(preview.getInterceptors()).hasSize(1).first()
                            .satisfies(interceptor -> Assertions.assertThat(interceptor.getName()).isEqualTo("testInterceptor1"));
                    Assertions.assertThat(preview.getApplicationRunners()).hasSize(1).first()
                            .isInstanceOfSatisfying(ExportApplicationTypeSchemaInfo.class,
                                    appRunner -> Assertions.assertThat(appRunner.getId()).isEqualTo("https://test-schema-id.example"));
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
            Assertions.assertThat(modelDto.getAdapter()).isEqualTo("adapter1");
        });
        Assertions.assertThat(models.get("testModel2"))
                .satisfies(modelDto -> Assertions.assertThat(modelDto.getIsPublic()).isTrue());
        ApplicationDto applicationDto = applicationFacade.getApplication("testApplication1");
        Assertions.assertThat(applicationDto.getInterceptors()).hasSize(1).first().isEqualTo("testInterceptor1");
        Assertions.assertThat(applicationDto.getCustomAppSchemaId().toString()).isEqualTo("https://test-schema-id.example");
        Collection<InterceptorDto> interceptors = interceptorFacade.getAllInterceptors();
        Assertions.assertThat(interceptors).isNotEmpty().hasSize(1).first().satisfies(i ->
                Assertions.assertThat(i.getEntities()).containsExactlyInAnyOrder("testModel1", "testApplication1"));
        Collection<String> allKeys = keyFacade.getAllKeys().stream().map(KeyDto::getName).toList();
        Assertions.assertThat(allKeys).containsExactlyInAnyOrder("testKey1", "testKey2");

        Map<String, AdapterDto> adapters = adapterFacade.getAllAdapters().stream().collect(Collectors.toMap(AdapterDto::getName, a -> a));
        Assertions.assertThat(adapters.get("adapter1")).satisfies(adapterDto -> {
            Assertions.assertThat(adapterDto.getName()).isEqualTo("adapter1");
            Assertions.assertThat(adapterDto.getBaseEndpoint()).isEqualTo("http://endpoint1/");
            Assertions.assertThat(adapterDto.getDescription()).isEqualTo("test adapter");
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
                    Assertions.assertThat(roleLimit.getMinute()).isNull();
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

        // when
        var importConfigPreview = configTransfer.importPreview(List.of(mockFile), ConflictResolutionPolicy.OVERRIDE);
        // then
        var expected = ResourceUtils.readResource("/import/import_preview.json");
        var expectedPreview = jsonMapper.readValue(expected, ImportConfigPreview.class);
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
        var modelName = "versionTestModel";
        var interceptorName = "versionTestInterceptor";
        var endpoint = "https://endpoint.test.com/interceptor";

        var interceptorDto = new InterceptorDto();
        interceptorDto.setName(interceptorName);
        interceptorDto.setEndpoint(endpoint);
        interceptorDto.setConfigurationEndpoint("https://endpoint.test.com/interceptor/conf");

        var modelDto = new ModelDto();
        modelDto.setName(modelName);
        modelDto.setInterceptors(List.of("versionTestInterceptor"));

        interceptorFacade.createInterceptor(interceptorDto);
        modelFacade.createModel(modelDto);

        var request = new FullExportRequest();
        request.setExportFormat(ExportFormat.CORE);
        request.setComponentTypes(Set.of(ExportConfigComponentType.MODEL, ExportConfigComponentType.INTERCEPTOR));

        // when
        StreamingResponseBody streamingResponseBody = configTransfer.exportConfig(request);

        // then
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        streamingResponseBody.writeTo(outputStream);

        Config result = jsonMapper.readValue(outputStream.toString(), Config.class);

        Assertions.assertThat(result).isNotNull();
        Assertions.assertThat(result.getModels()).containsKey(modelName);
        Assertions.assertThat(result.getInterceptors()).containsKey(interceptorName);

        // this verifies versioning works correctly, since configuration endpoint is not present in schema v0.30.0
        var exportedInterceptor = result.getInterceptors().get(interceptorName);
        Assertions.assertThat(exportedInterceptor.getEndpoint()).isEqualTo(endpoint);
        Assertions.assertThat(exportedInterceptor.getConfigurationEndpoint()).isNull();
    }

    @Test
    void testExportCoreConfig_VersionFieldSerialization() throws IOException {
        // given
        var modelName = "versionFieldsModel";
        var author = "Test Author";
        var createdAt = 1624987654321L;
        var updatedAt = 1624987654999L;

        var modelDto = new ModelDto();
        modelDto.setName(modelName);
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
            Assertions.assertThat(exportedModel.getCreatedAt()).isEqualTo(createdAt);
            Assertions.assertThat(exportedModel.getUpdatedAt()).isEqualTo(updatedAt);

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

    private String getAppRunnerDto() {
        return """
            {"$id": "https://test-schema-id.example",
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
                    "required": []
                  }""";
    }

    private static ConfigImportOptions overrideAndCreateRoleAndCreateNew() {
        return new ConfigImportOptions(ConflictResolutionPolicy.OVERRIDE, true, true);
    }

    private static ConfigImportOptions overrideAndNotCreateRoleAndCreateNew() {
        return new ConfigImportOptions(ConflictResolutionPolicy.OVERRIDE, false, true);
    }
}