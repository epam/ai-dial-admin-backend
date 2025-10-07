package com.epam.aidial.cfg.functional.tests;

import com.epam.aidial.cfg.client.dto.DeploymentInfoDto;
import com.epam.aidial.cfg.domain.service.DeploymentManagerService;
import com.epam.aidial.cfg.dto.ApplicationDto;
import com.epam.aidial.cfg.dto.InterceptorDto;
import com.epam.aidial.cfg.dto.ModelDto;
import com.epam.aidial.cfg.dto.source.InterceptorContainerSourceDto;
import com.epam.aidial.cfg.exception.EntityAlreadyExistsException;
import com.epam.aidial.cfg.exception.EntityNotFoundException;
import com.epam.aidial.cfg.exception.OptimisticLockConflictException;
import com.epam.aidial.cfg.web.facade.ApplicationFacade;
import com.epam.aidial.cfg.web.facade.InterceptorFacade;
import com.epam.aidial.cfg.web.facade.ModelFacade;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.epam.aidial.cfg.functional.utils.FunctionalTestHelper.createApplicationDtoWithEndpoint;
import static com.epam.aidial.cfg.functional.utils.FunctionalTestHelper.createInterceptorDto;
import static com.epam.aidial.cfg.functional.utils.FunctionalTestHelper.createInterceptorDtoWithEntities;
import static com.epam.aidial.cfg.functional.utils.FunctionalTestHelper.createModelDtoWithEndpoint;

public abstract class InterceptorFunctionalTest {

    @Autowired
    private InterceptorFacade interceptorFacade;
    @Autowired
    private ApplicationFacade applicationFacade;
    @Autowired
    private ModelFacade modelFacade;
    @Autowired
    private DeploymentManagerService deploymentManagerService;

    @Test
    public void shouldSuccessfullyCreateAndGetInterceptors() {
        String firstSuffix = "1";
        String secondSuffix = "2";
        // create application1
        applicationFacade.createApplication(createApplicationDtoWithEndpoint(firstSuffix));

        // create interceptor1 with application1
        InterceptorDto interceptorDto = createDtoWithDefaults(firstSuffix);
        interceptorFacade.createInterceptor(interceptorDto);

        InterceptorDto actual = interceptorFacade.getInterceptor(interceptorDto.getName());
        InterceptorDto expected1 = createDtoWithDefaults(firstSuffix);

        assertInterceptorWithDefaults(actual, expected1);

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
        Assertions.assertEquals("Optimistic lock conflict on update: interceptorName:'interceptor1'"
                + ". Reload the data.", exception.getMessage());
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

        DeploymentInfoDto deploymentInfoDto = new DeploymentInfoDto();
        deploymentInfoDto.setId(UUID.fromString(containerId));
        deploymentInfoDto.setName("Test Container");
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

        DeploymentInfoDto initialDeploymentInfo = new DeploymentInfoDto();
        initialDeploymentInfo.setId(UUID.fromString(containerId));
        initialDeploymentInfo.setName(deploymentName);
        initialDeploymentInfo.setUrl(initialUrl);

        DeploymentInfoDto updatedDeploymentInfo = new DeploymentInfoDto();
        updatedDeploymentInfo.setId(UUID.fromString(containerId));
        updatedDeploymentInfo.setName(deploymentName);
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
}
