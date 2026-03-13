package com.epam.aidial.cfg.functional.tests;

import com.epam.aidial.cfg.client.dto.DeploymentInfoDto;
import com.epam.aidial.cfg.client.dto.InferenceDeploymentInfoDto;
import com.epam.aidial.cfg.client.dto.InterceptorDeploymentInfoDto;
import com.epam.aidial.cfg.configuration.JsonMapperConfiguration;
import com.epam.aidial.cfg.domain.service.DeploymentManagerService;
import com.epam.aidial.cfg.dto.ApplicationDto;
import com.epam.aidial.cfg.dto.ApplicationTypeSchemaDto;
import com.epam.aidial.cfg.dto.EntitySyncStateDto;
import com.epam.aidial.cfg.dto.EntitySyncStateStatusDto;
import com.epam.aidial.cfg.dto.FeaturesDto;
import com.epam.aidial.cfg.dto.InterceptorDto;
import com.epam.aidial.cfg.dto.InterceptorRunnerDto;
import com.epam.aidial.cfg.dto.ModelDto;
import com.epam.aidial.cfg.dto.source.InterceptorContainerSourceDto;
import com.epam.aidial.cfg.dto.source.InterceptorRunnerSourceDto;
import com.epam.aidial.cfg.exception.EntityAlreadyExistsException;
import com.epam.aidial.cfg.exception.EntityNotFoundException;
import com.epam.aidial.cfg.exception.OptimisticLockConflictException;
import com.epam.aidial.cfg.service.config.reload.CoreConfigReloadCache;
import com.epam.aidial.cfg.transaction.timestamp.TransactionTimestampContext;
import com.epam.aidial.cfg.utils.ResourceUtils;
import com.epam.aidial.cfg.web.facade.ApplicationFacade;
import com.epam.aidial.cfg.web.facade.ApplicationTypeSchemaFacade;
import com.epam.aidial.cfg.web.facade.InterceptorFacade;
import com.epam.aidial.cfg.web.facade.InterceptorRunnerFacade;
import com.epam.aidial.cfg.web.facade.ModelFacade;
import com.epam.aidial.core.config.CoreInterceptor;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.epam.aidial.cfg.functional.utils.FunctionalTestHelper.createApplicationDtoWithEndpoint;
import static com.epam.aidial.cfg.functional.utils.FunctionalTestHelper.createInterceptorDto;
import static com.epam.aidial.cfg.functional.utils.FunctionalTestHelper.createInterceptorDtoWithEntities;
import static com.epam.aidial.cfg.functional.utils.FunctionalTestHelper.createInterceptorRunnerDto;
import static com.epam.aidial.cfg.functional.utils.FunctionalTestHelper.createModelDtoWithEndpoint;
import static com.epam.aidial.cfg.functional.utils.FunctionalTestHelper.defaultCoreFeatures;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

public abstract class InterceptorFunctionalTest {

    private static final ObjectMapper OBJECT_MAPPER = JsonMapperConfiguration.createJsonMapper();

    @Autowired
    private InterceptorFacade interceptorFacade;
    @Autowired
    private ApplicationFacade applicationFacade;
    @Autowired
    private ModelFacade modelFacade;
    @Autowired
    private ApplicationTypeSchemaFacade typeSchemaFacade;
    @Autowired
    private InterceptorRunnerFacade interceptorRunnerFacade;
    @Autowired
    private DeploymentManagerService deploymentManagerService;
    @Autowired
    private TransactionTimestampContext transactionTimestampContext;
    @Autowired
    private CoreConfigReloadCache coreConfigReloadCache;

    @Test
    public void shouldSuccessfullyCreateAndGetInterceptors() {
        String firstSuffix = "1";
        String secondSuffix = "2";
        // create application1
        applicationFacade.createApplication(createApplicationDtoWithEndpoint(firstSuffix));

        // create interceptor1 with application1
        InterceptorDto interceptorDto = createDtoWithDefaults(firstSuffix);
        interceptorDto.getFeatures().setConfigurationEndpoint("https://endpoint.test.com/interceptor/configuration");
        interceptorFacade.createInterceptor(interceptorDto);

        InterceptorDto actual = interceptorFacade.getInterceptor(interceptorDto.getName());
        InterceptorDto expected1 = createDtoWithDefaults(firstSuffix);

        assertInterceptorWithDefaults(actual, expected1);
        assertThat(actual.getFeatures().getConfigurationEndpoint()).isEqualTo("https://endpoint.test.com/interceptor/configuration");

        // create application1
        applicationFacade.createApplication(createApplicationDtoWithEndpoint(secondSuffix));

        // create interceptor2 with application2
        InterceptorDto expected2 = createInterceptorDto(secondSuffix);
        interceptorFacade.createInterceptor(expected2);

        Collection<InterceptorDto> actualInterceptors = interceptorFacade.getAllInterceptors();
        assertInterceptors(actualInterceptors, List.of(expected1, expected2));
    }

    @Test
    public void shouldSuccessfullyCreateAndDeleteInterceptor() {
        String firstSuffix = "1";
        //create application1
        ApplicationDto applicationDto = createApplicationDtoWithEndpoint(firstSuffix);
        applicationFacade.createApplication(applicationDto);

        // create interceptor1
        InterceptorDto interceptorDto = createInterceptorDtoWithEntities(firstSuffix);
        interceptorFacade.createInterceptor(interceptorDto);

        assertAppInterceptors(applicationDto.getName(), List.of("interceptor" + firstSuffix));

        interceptorFacade.deleteInterceptor(interceptorDto.getName());

        Assertions.assertThrows(EntityNotFoundException.class, () -> interceptorFacade.getInterceptor(interceptorDto.getName()));
        Assertions.assertTrue(interceptorFacade.getAllInterceptors().isEmpty());
        assertAppInterceptors(applicationDto.getName(), List.of());
    }

    private void assertAppInterceptors(String name, List<String> expected) {
        ApplicationDto applicationDto = applicationFacade.getApplication(name);
        List<String> interceptorsNames = applicationDto.getInterceptors();
        Assertions.assertEquals(expected, interceptorsNames);
    }

    @Test
    public void shouldSuccessfullyCreateAndUpdateInterceptor() {
        ApplicationDto applicationDto = createApplicationDtoWithEndpoint("1");
        applicationFacade.createApplication(applicationDto);
        ApplicationDto applicationDto2 = createApplicationDtoWithEndpoint("2");
        applicationFacade.createApplication(applicationDto2);

        InterceptorDto interceptorDto = createInterceptorDtoWithEntities("1");
        interceptorFacade.createInterceptor(interceptorDto);

        assertAppInterceptors(applicationDto.getName(), List.of("interceptor1"));

        InterceptorDto updatedInterceptor = createInterceptorDtoWithEntities("1");
        updatedInterceptor.setEntities(List.of("application2"));

        interceptorFacade.updateInterceptor(interceptorDto.getName(), updatedInterceptor, "*");

        assertAppInterceptors(applicationDto.getName(), List.of());
        assertAppInterceptors(applicationDto2.getName(), List.of("interceptor1"));

        InterceptorDto actual = interceptorFacade.getInterceptor(interceptorDto.getName());
        var expected = createInterceptorDtoWithEntities("1");
        expected.setEntities(List.of("application2"));
        assertInterceptor(actual, expected);
    }

    @Test
    public void shouldSuccessfullyCreateAndUpdateInterceptorWithModels() {
        ModelDto modelDto1 = createModelDtoWithEndpoint("1");
        modelFacade.createModel(modelDto1);
        ModelDto modelDto2 = createModelDtoWithEndpoint("2");
        modelFacade.createModel(modelDto2);

        InterceptorDto interceptorDto = createInterceptorDtoWithEntities("1");
        interceptorDto.setEntities(List.of("model1", "model2"));
        interceptorFacade.createInterceptor(interceptorDto);

        InterceptorDto actualInterceptor = interceptorFacade.getInterceptor("interceptor1");
        org.assertj.core.api.Assertions.assertThat(actualInterceptor.getEntities())
                .containsExactlyInAnyOrderElementsOf(List.of("model1", "model2"));

        modelDto1.setInterceptors(List.of("interceptor1", "interceptor1", "interceptor1"));
        modelFacade.updateModel(modelDto1.getName(), modelDto1, "*");

        actualInterceptor = interceptorFacade.getInterceptor("interceptor1");
        org.assertj.core.api.Assertions.assertThat(actualInterceptor.getEntities())
                .containsExactlyInAnyOrderElementsOf(List.of("model1", "model2"));

        ModelDto actualModel1 = modelFacade.getModel(modelDto1.getName());
        Assertions.assertEquals(actualModel1.getInterceptors(), List.of("interceptor1", "interceptor1", "interceptor1"));
    }

    @Test
    public void shouldSuccessfullyCreateAndUpdateInterceptorWithApplications() {
        ApplicationDto applicationDto1 = createApplicationDtoWithEndpoint("1");
        applicationFacade.createApplication(applicationDto1);
        ApplicationDto applicationDto2 = createApplicationDtoWithEndpoint("2");
        applicationFacade.createApplication(applicationDto2);

        InterceptorDto interceptorDto = createInterceptorDtoWithEntities("1");
        interceptorDto.setEntities(List.of("application1", "application2"));
        interceptorFacade.createInterceptor(interceptorDto);

        InterceptorDto actualInterceptor = interceptorFacade.getInterceptor("interceptor1");
        org.assertj.core.api.Assertions.assertThat(actualInterceptor.getEntities())
                .containsExactlyInAnyOrderElementsOf(List.of("application1", "application2"));

        applicationDto1.setInterceptors(List.of("interceptor1", "interceptor1", "interceptor1"));
        applicationFacade.updateApplication(applicationDto1.getName(), applicationDto1, "*");

        actualInterceptor = interceptorFacade.getInterceptor("interceptor1");
        org.assertj.core.api.Assertions.assertThat(actualInterceptor.getEntities())
                .containsExactlyInAnyOrderElementsOf(List.of("application1", "application2"));

        ApplicationDto actualApplication1 = applicationFacade.getApplication(applicationDto1.getName());
        Assertions.assertEquals(actualApplication1.getInterceptors(), List.of("interceptor1", "interceptor1", "interceptor1"));
    }

    @Test
    public void shouldSuccessfullyCreateAndUpdateInterceptorWithSchemas() throws JsonProcessingException {
        var dtosJson = ResourceUtils.readResource("/application_type_schema_dto.json");
        var applicationTypeSchemaDto = OBJECT_MAPPER.readValue(dtosJson, new TypeReference<ApplicationTypeSchemaDto>() {
        });
        var applicationTypeSchemaDto2 = OBJECT_MAPPER.readValue(dtosJson, new TypeReference<ApplicationTypeSchemaDto>() {
        });
        applicationTypeSchemaDto2.setId(applicationTypeSchemaDto2.getId() + 2);
        typeSchemaFacade.create(applicationTypeSchemaDto);
        typeSchemaFacade.create(applicationTypeSchemaDto2);

        InterceptorDto interceptorDto = createInterceptorDto("1");
        interceptorDto.setApplicationTypeSchemas(List.of("https://test-schema.example", "https://test-schema.example", "https://test-schema.example2"));
        interceptorFacade.createInterceptor(interceptorDto);

        InterceptorDto actualInterceptor = interceptorFacade.getInterceptor("interceptor1");
        Assertions.assertEquals(actualInterceptor.getApplicationTypeSchemas(), List.of("https://test-schema.example", "https://test-schema.example2"));

        interceptorDto.setApplicationTypeSchemas(List.of("https://test-schema.example2"));
        interceptorFacade.updateInterceptor("interceptor1", interceptorDto, "*");
        actualInterceptor = interceptorFacade.getInterceptor("interceptor1");
        Assertions.assertEquals(actualInterceptor.getApplicationTypeSchemas(), List.of("https://test-schema.example2"));

        var actualApplicationTypeSchema = typeSchemaFacade.get("https://test-schema.example");
        Assertions.assertTrue(actualApplicationTypeSchema.getInterceptors().isEmpty());

        interceptorDto.setApplicationTypeSchemas(List.of());
        interceptorFacade.updateInterceptor("interceptor1", interceptorDto, "*");
        actualInterceptor = interceptorFacade.getInterceptor("interceptor1");
        Assertions.assertTrue(actualInterceptor.getApplicationTypeSchemas().isEmpty());

        var actualApplicationTypeSchema2 = typeSchemaFacade.get("https://test-schema.example2");
        Assertions.assertTrue(actualApplicationTypeSchema2.getInterceptors().isEmpty());
    }

    @Test
    public void shouldThrowExceptionWhenCreateInterceptorWithExistingName() {
        applicationFacade.createApplication(createApplicationDtoWithEndpoint("1"));

        InterceptorDto interceptorDto = createInterceptorDtoWithEntities("1");
        interceptorFacade.createInterceptor(interceptorDto);

        EntityAlreadyExistsException exception = Assertions.assertThrows(
                EntityAlreadyExistsException.class,
                () -> interceptorFacade.createInterceptor(createInterceptorDtoWithEntities("1"))
        );

        Assertions.assertEquals("Interceptor with name interceptor1 already exists", exception.getMessage());
    }

    @Test
    public void shouldThrowExceptionWhenUpdateInterceptorWithExistingName() {
        applicationFacade.createApplication(createApplicationDtoWithEndpoint("1"));
        applicationFacade.createApplication(createApplicationDtoWithEndpoint("2"));

        InterceptorDto interceptorDto = createInterceptorDtoWithEntities("1");
        interceptorFacade.createInterceptor(interceptorDto);

        InterceptorDto interceptorDto2 = createInterceptorDtoWithEntities("2");
        interceptorFacade.createInterceptor(interceptorDto2);

        interceptorDto.setName("interceptor2");

        IllegalArgumentException exception = Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> interceptorFacade.updateInterceptor("interceptor1", interceptorDto, "*")
        );

        Assertions.assertEquals("Interceptor with name: 'interceptor1' can not be renamed. New interceptor name: 'interceptor2'", exception.getMessage());
    }

    @Test
    public void shouldThrowExceptionWhenInterceptorConcurrencyOverwrite() {
        ApplicationDto applicationDto1 = createApplicationDtoWithEndpoint("1");
        applicationFacade.createApplication(applicationDto1);
        InterceptorDto interceptorDto = createInterceptorDtoWithEntities("1");
        interceptorFacade.createInterceptor(interceptorDto);

        OptimisticLockConflictException exception = Assertions.assertThrows(
                OptimisticLockConflictException.class,
                () -> interceptorFacade.updateInterceptor(interceptorDto.getName(), interceptorDto, "test")
        );
        Assertions.assertEquals("Unable to update Interceptor 'interceptor1'. The data may have been modified by another user, "
                        + "or the name/ID may already exist. Please reload the data and try again.",
                exception.getMessage());
    }

    @Test
    public void shouldThrowExceptionWhenHashIsNull() {
        ApplicationDto applicationDto1 = createApplicationDtoWithEndpoint("1");
        applicationFacade.createApplication(applicationDto1);
        InterceptorDto interceptorDto = createInterceptorDtoWithEntities("1");
        interceptorFacade.createInterceptor(interceptorDto);

        IllegalArgumentException exception = Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> interceptorFacade.updateInterceptor(interceptorDto.getName(), interceptorDto, null)
        );
        Assertions.assertEquals("Hash must not be null. Use \"*\" to skip optimistic check. Interceptor:interceptor1.",
                exception.getMessage());
    }

    @Test
    public void shouldSuccessfullyUpdateInterceptorWithCorrectHash() {
        ApplicationDto applicationDto = createApplicationDtoWithEndpoint("1");
        applicationFacade.createApplication(applicationDto);
        ApplicationDto applicationDto2 = createApplicationDtoWithEndpoint("2");
        applicationFacade.createApplication(applicationDto2);
        InterceptorDto interceptorDto = createInterceptorDtoWithEntities("1");
        interceptorFacade.createInterceptor(interceptorDto);
        assertAppInterceptors(applicationDto.getName(), List.of("interceptor1"));

        InterceptorDto updatedInterceptor = createInterceptorDtoWithEntities("1");
        updatedInterceptor.setEntities(List.of("application2"));

        var hash = interceptorFacade.getInterceptorWithHash(interceptorDto.getName()).hash();

        interceptorFacade.updateInterceptor(interceptorDto.getName(), updatedInterceptor, hash);

        InterceptorDto actual = interceptorFacade.getInterceptor(interceptorDto.getName());
        var expected = createInterceptorDtoWithEntities("1");
        expected.setEntities(List.of("application2"));
        assertInterceptor(actual, expected);
    }

    @Test
    public void shouldSuccessfullyUpdateInterceptorWithNewContainerSource() {
        ApplicationDto applicationDto = createApplicationDtoWithEndpoint("1");
        applicationFacade.createApplication(applicationDto);
        InterceptorDto interceptorDto = createInterceptorDtoWithEntities("1");
        interceptorFacade.createInterceptor(interceptorDto);
        assertAppInterceptors(applicationDto.getName(), List.of("interceptor1"));

        String containerId = "550e8400-e29b-41d4-a716-446655440000";
        String containerUrl = "https://container-url.com/";
        String completionPath = "api/completion";
        String configPath = "api/config";
        String containerName = "Test Container";

        InterceptorDto updatedInterceptor = createInterceptorDtoWithEntities("1");
        InterceptorContainerSourceDto source = new InterceptorContainerSourceDto(containerId, containerName, completionPath, configPath);
        updatedInterceptor.setSource(source);

        DeploymentInfoDto deploymentInfoDto = new InterceptorDeploymentInfoDto();
        deploymentInfoDto.setId(containerId);
        deploymentInfoDto.setDisplayName(containerName);
        deploymentInfoDto.setUrl(containerUrl);

        Mockito.when(deploymentManagerService.getById(containerId)).thenReturn(deploymentInfoDto);

        var hash = interceptorFacade.getInterceptorWithHash(interceptorDto.getName()).hash();

        interceptorFacade.updateInterceptor(interceptorDto.getName(), updatedInterceptor, hash);

        InterceptorDto actual = interceptorFacade.getInterceptor(interceptorDto.getName());
        var expected = createInterceptorDtoWithEntities("1");
        expected.setSource(source);
        expected.setEndpoint(containerUrl + completionPath);
        var features = new FeaturesDto();
        features.setConfigurationEndpoint(containerUrl + configPath);
        expected.setFeatures(features);
        assertInterceptor(actual, expected);
    }

    @Test
    public void shouldThrowWhenUpdateInterceptorWithIncorrectHash() {
        ApplicationDto applicationDto = createApplicationDtoWithEndpoint("1");
        applicationFacade.createApplication(applicationDto);
        ApplicationDto applicationDto2 = createApplicationDtoWithEndpoint("2");
        applicationFacade.createApplication(applicationDto2);
        InterceptorDto interceptorDto = createInterceptorDtoWithEntities("1");
        interceptorFacade.createInterceptor(interceptorDto);
        assertAppInterceptors(applicationDto.getName(), List.of("interceptor1"));

        InterceptorDto updatedInterceptor = createInterceptorDtoWithEntities("1");
        updatedInterceptor.setEntities(List.of("application2"));

        Assertions.assertThrows(OptimisticLockConflictException.class,
                () -> interceptorFacade.updateInterceptor(interceptorDto.getName(), updatedInterceptor, "test"));
    }

    private InterceptorDto createDtoWithDefaults(String suffix) {
        InterceptorDto dto = createInterceptorDtoWithEntities(suffix);
        dto.setDefaults(Map.of("max_limit", 7000));
        return dto;
    }

    private void assertInterceptorWithDefaults(InterceptorDto actual, InterceptorDto expected) {
        assertInterceptor(actual, expected);
        Assertions.assertEquals(expected.getDefaults(), actual.getDefaults());
    }

    private void assertInterceptor(InterceptorDto actual, InterceptorDto expected) {
        Assertions.assertEquals(expected.getName(), actual.getName());
        Assertions.assertEquals(expected.getEntities(), actual.getEntities());
        Assertions.assertEquals(expected.getEndpoint(), actual.getEndpoint());
        Assertions.assertEquals(expected.getSource(), actual.getSource());
    }

    private Map<String, InterceptorDto> toMap(Collection<InterceptorDto> dtos) {
        return dtos.stream()
                .collect(Collectors.toMap(InterceptorDto::getName, Function.identity()));
    }

    private void assertInterceptors(Collection<InterceptorDto> actual, Collection<InterceptorDto> expected) {
        Map<String, InterceptorDto> actualMap = toMap(actual);
        Map<String, InterceptorDto> expectedMap = toMap(expected);
        Assertions.assertEquals(expectedMap.keySet(), actualMap.keySet());
        for (String name : actualMap.keySet()) {
            assertInterceptor(actualMap.get(name), expectedMap.get(name));
        }
    }

    @Test
    public void shouldResolveEndpointsForContainerSource() {
        // Given
        String containerId = "550e8400-e29b-41d4-a716-446655440000";
        String containerName = "test-container";
        String containerUrl = "https://container-url.com";
        String completionPath = "/api/completion";
        String configPath = "/api/config";

        DeploymentInfoDto deploymentInfoDto = new InterceptorDeploymentInfoDto();
        deploymentInfoDto.setId(containerId);
        deploymentInfoDto.setDisplayName("Test Container");
        deploymentInfoDto.setUrl(containerUrl);

        Mockito.when(deploymentManagerService.getById(containerId)).thenReturn(deploymentInfoDto);

        InterceptorDto interceptorDto = new InterceptorDto();
        interceptorDto.setName("container-interceptor");
        interceptorDto.setDisplayName("container-interceptor");
        interceptorDto.setDescription("Container interceptor");

        InterceptorContainerSourceDto sourceDto = new InterceptorContainerSourceDto(
                containerId,
                containerName,
                completionPath,
                configPath
        );

        interceptorDto.setSource(sourceDto);

        // When
        interceptorFacade.createInterceptor(interceptorDto);

        // Then
        InterceptorDto result = interceptorFacade.getInterceptor("container-interceptor");

        Assertions.assertEquals(containerUrl + completionPath, result.getEndpoint());
        Assertions.assertEquals(containerUrl + configPath, result.getFeatures().getConfigurationEndpoint());

        Mockito.verify(deploymentManagerService, Mockito.atLeast(2)).getById(containerId);
    }

    @Test
    public void shouldRefreshEndpointsForContainerSource() {
        // Given
        String deploymentName = "Test Container";
        String containerId = "550e8400-e29b-41d4-a716-446655440000";
        String containerName = "test-container";
        String initialUrl = "https://initial-url.com";
        String updatedUrl = "https://updated-url.com";
        String completionPath = "/api/completion";
        String configPath = "/api/config";

        DeploymentInfoDto initialDeploymentInfo = new InterceptorDeploymentInfoDto();
        initialDeploymentInfo.setId(containerId);
        initialDeploymentInfo.setDisplayName(deploymentName);
        initialDeploymentInfo.setUrl(initialUrl);

        DeploymentInfoDto updatedDeploymentInfo = new InterceptorDeploymentInfoDto();
        updatedDeploymentInfo.setId(containerId);
        updatedDeploymentInfo.setDisplayName(deploymentName);
        updatedDeploymentInfo.setUrl(updatedUrl);

        Mockito.when(deploymentManagerService.getById(containerId))
                .thenReturn(initialDeploymentInfo)
                .thenReturn(initialDeploymentInfo)
                .thenReturn(updatedDeploymentInfo)
                .thenReturn(updatedDeploymentInfo);

        InterceptorDto interceptorDto = createInterceptorDto("-refresh");
        InterceptorContainerSourceDto sourceDto = new InterceptorContainerSourceDto(
                containerId,
                containerName,
                completionPath,
                configPath
        );
        interceptorDto.setSource(sourceDto);
        interceptorFacade.createInterceptor(interceptorDto);

        InterceptorDto initialResult = interceptorFacade.getInterceptor("interceptor-refresh");
        Assertions.assertEquals(initialUrl + completionPath, initialResult.getEndpoint());
        Assertions.assertEquals(initialUrl + configPath, initialResult.getFeatures().getConfigurationEndpoint());

        // When
        interceptorFacade.refreshEndpoints();

        // Then
        InterceptorDto refreshedResult = interceptorFacade.getInterceptor("interceptor-refresh");
        Assertions.assertEquals(updatedUrl + completionPath, refreshedResult.getEndpoint());
        Assertions.assertEquals(updatedUrl + configPath, refreshedResult.getFeatures().getConfigurationEndpoint());

        Mockito.verify(deploymentManagerService, Mockito.atLeast(2)).getById(containerId);
    }

    @Test
    public void shouldSuccessfullyGetCoreInterceptor() {
        InterceptorDto interceptorDto = createInterceptorDto("1");
        interceptorFacade.createInterceptor(interceptorDto);

        CoreInterceptor expected = new CoreInterceptor();
        expected.setName(interceptorDto.getName());
        expected.setDisplayName(interceptorDto.getDisplayName());
        expected.setDescription(interceptorDto.getDescription());
        expected.setEndpoint(interceptorDto.getEndpoint());
        expected.setFeatures(defaultCoreFeatures());
        expected.setForwardAuthToken(interceptorDto.getForwardAuthToken());

        CoreInterceptor actual = interceptorFacade.getCoreInterceptorWithHash(interceptorDto.getName()).core();
        actual.setCreatedAt(null);
        actual.setUpdatedAt(null);

        Assertions.assertEquals(expected, actual);
    }

    @Test
    public void shouldResetRunnerToNullWhenChangingInterceptorSourceFromAdapterToContainer() {
        // Create runner
        InterceptorRunnerDto interceptorRunnerDto = createInterceptorRunnerDto("1");
        interceptorRunnerFacade.createInterceptorRunner(interceptorRunnerDto);

        // Create interceptor with runner source
        InterceptorDto interceptorDto = createInterceptorDto("1");
        interceptorDto.setSource(new InterceptorRunnerSourceDto(interceptorRunnerDto.getName()));
        interceptorFacade.createInterceptor(interceptorDto);

        // Verify interceptor has runner source
        InterceptorDto actualInterceptor = interceptorFacade.getInterceptor(interceptorDto.getName());
        Assertions.assertNotNull(actualInterceptor.getSource());
        Assertions.assertInstanceOf(InterceptorRunnerSourceDto.class, actualInterceptor.getSource());
        InterceptorRunnerSourceDto interceptorRunnerSource = (InterceptorRunnerSourceDto) actualInterceptor.getSource();
        Assertions.assertEquals(interceptorRunnerDto.getName(), interceptorRunnerSource.runnerName());

        // Verify runner has the interceptor in its interceptors list
        InterceptorRunnerDto actualInterceptorRunner = interceptorRunnerFacade.getInterceptorRunner(interceptorRunnerDto.getName());
        Assertions.assertTrue(actualInterceptorRunner.getInterceptors().contains(interceptorDto.getName()));

        // Update interceptor to container source
        String containerId = "container-123";
        DeploymentInfoDto deploymentInfo = new InferenceDeploymentInfoDto();
        deploymentInfo.setUrl("http://dial-test-host-name.ooops/yes/no/true/false");
        when(deploymentManagerService.getById(containerId)).thenReturn(deploymentInfo);

        InterceptorDto updatedInterceptorDto = createInterceptorDto("1");
        updatedInterceptorDto.setSource(new InterceptorContainerSourceDto(containerId, "test-container", "/chat/completions", "/configuration"));
        interceptorFacade.updateInterceptor(updatedInterceptorDto.getName(), updatedInterceptorDto, "*");

        // Verify interceptor now has container source (not runner source)
        actualInterceptor = interceptorFacade.getInterceptor(interceptorDto.getName());
        Assertions.assertNotNull(actualInterceptor.getSource());
        Assertions.assertInstanceOf(InterceptorContainerSourceDto.class, actualInterceptor.getSource());
        InterceptorContainerSourceDto containerSource = (InterceptorContainerSourceDto) actualInterceptor.getSource();
        Assertions.assertEquals(containerId, containerSource.containerId());

        // Verify runner no longer has the model in its models list
        actualInterceptorRunner = interceptorRunnerFacade.getInterceptorRunner(interceptorRunnerDto.getName());
        Assertions.assertFalse(actualInterceptorRunner.getInterceptors().contains(updatedInterceptorDto.getName()),
                "Runner should not contain the interceptor after switching to container source");

        // Add updated interceptor (with container) to runner and save runner. Interceptor source should be switched to runner back
        // Simulate adding the interceptor (now container source) back to the runner
        interceptorRunnerDto.setInterceptors(List.of(updatedInterceptorDto.getName()));
        interceptorRunnerFacade.updateInterceptorRunner(interceptorRunnerDto.getName(), interceptorRunnerDto, "*");

        // Verify the interceptor now has runner source again
        actualInterceptor = interceptorFacade.getInterceptor(interceptorDto.getName());
        Assertions.assertNotNull(actualInterceptor.getSource());
        Assertions.assertInstanceOf(InterceptorRunnerSourceDto.class, actualInterceptor.getSource());
        InterceptorRunnerSourceDto runnerSourceAgain = (InterceptorRunnerSourceDto) actualInterceptor.getSource();
        Assertions.assertEquals(interceptorRunnerDto.getName(), runnerSourceAgain.runnerName());

        // Verify the runner has the interceptor again in its models list
        actualInterceptorRunner = interceptorRunnerFacade.getInterceptorRunner(interceptorRunnerDto.getName());
        Assertions.assertTrue(actualInterceptorRunner.getInterceptors().contains(updatedInterceptorDto.getName()),
                "Runner should contain the interceptor after switching back to runner source");
    }


    @Test
    public void shouldSuccessfullyGetFullySyncedEntitySyncStateWhenInterceptorIsEqualToConfigInterceptor() throws JsonProcessingException {
        doReturn(1000L).when(transactionTimestampContext).getTimestamp();
        InterceptorDto interceptorDto = createInterceptorDto("1");
        interceptorFacade.createInterceptor(interceptorDto);

        JsonNode config = coreConfig();
        CoreConfigReloadCache.Entry cacheEntry = new CoreConfigReloadCache.Entry(config, 1000);
        when(coreConfigReloadCache.get()).thenReturn(cacheEntry);

        JsonNode interceptorState = config.get("interceptors").get("interceptor1");

        EntitySyncStateDto actualSyncState = interceptorFacade.getSyncState(interceptorDto.getName(), "*");

        assertThat(actualSyncState.getCurrentState()).isEqualTo(interceptorState);
        assertThat(actualSyncState.getConfigState()).isEqualTo(interceptorState);
        assertThat(actualSyncState.getStatus()).isEqualTo(EntitySyncStateStatusDto.FULLY_SYNCED);
    }

    @Test
    public void shouldSuccessfullyGetInProgressTooLongEntitySyncStateWhenInterceptorIsNotEqualToConfigInterceptorAndUpdatedLongAgo() throws JsonProcessingException {
        doReturn(1000L).when(transactionTimestampContext).getTimestamp();
        InterceptorDto interceptorDto = createInterceptorDto("1");
        interceptorDto.setDescription("description OLD");
        interceptorFacade.createInterceptor(interceptorDto);

        JsonNode config = coreConfig();
        CoreConfigReloadCache.Entry cacheEntry = new CoreConfigReloadCache.Entry(config, 122000);
        when(coreConfigReloadCache.get()).thenReturn(cacheEntry);

        JsonNode configInterceptorState = config.get("interceptors").get("interceptor1");
        JsonNode currentInterceptorState = configInterceptorState.deepCopy();
        ((ObjectNode) currentInterceptorState).put("description", "description OLD");

        EntitySyncStateDto actualSyncState = interceptorFacade.getSyncState(interceptorDto.getName(), "*");

        assertThat(actualSyncState.getCurrentState()).isEqualTo(currentInterceptorState);
        assertThat(actualSyncState.getConfigState()).isEqualTo(configInterceptorState);
        assertThat(actualSyncState.getStatus()).isEqualTo(EntitySyncStateStatusDto.IN_PROGRESS_TOO_LONG);
    }

    private JsonNode coreConfig() throws JsonProcessingException {
        String config = """
                {
                  "interceptors": {
                    "interceptor1": {
                      "name": "interceptor1",
                      "userRoles": null,
                      "endpoint": "https://endpoint.test.com/interceptor1",
                      "displayName": "displayName1",
                      "displayVersion": null,
                      "iconUrl": null,
                      "description": "description1",
                      "reference": null,
                      "forwardAuthToken": false,
                      "features": {
                        "system_prompt_supported": true,
                        "tools_supported": false,
                        "seed_supported": false,
                        "url_attachments_supported": false,
                        "folder_attachments_supported": false,
                        "allow_resume": true,
                        "accessible_by_per_request_key": true,
                        "content_parts_supported": false,
                        "temperature_supported": true,
                        "parallel_tool_calls_supported": true,
                        "assistant_attachments_in_request_supported": false
                      },
                      "inputAttachmentTypes": null,
                      "maxInputAttachments": null,
                      "defaults": {},
                      "interceptors": [],
                      "descriptionKeywords": [],
                      "maxRetryAttempts": 1,
                      "author": null,
                      "createdAt": 1000,
                      "updatedAt": 1000,
                      "dependencies": []
                    }
                  }
                }
                """;
        return OBJECT_MAPPER.readTree(config);
    }
}