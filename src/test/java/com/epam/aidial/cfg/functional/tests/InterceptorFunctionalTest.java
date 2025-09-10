package com.epam.aidial.cfg.functional.tests;

import com.epam.aidial.cfg.client.dto.DeploymentInfoDto;
import com.epam.aidial.cfg.domain.service.DeploymentManagerService;
import com.epam.aidial.cfg.dto.ApplicationDto;
import com.epam.aidial.cfg.dto.InterceptorDto;
import com.epam.aidial.cfg.dto.ModelDto;
import com.epam.aidial.cfg.dto.source.InterceptorContainerSourceDto;
import com.epam.aidial.cfg.exception.EntityAlreadyExistsException;
import com.epam.aidial.cfg.exception.EntityNotFoundException;
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
        applicationFacade.createApplication(createApplicationDto(firstSuffix));

        // create interceptor1 with application1
        InterceptorDto interceptorDto = createDtoWithDefaults(firstSuffix);
        interceptorFacade.createInterceptor(interceptorDto);

        InterceptorDto actual = interceptorFacade.getInterceptor(interceptorDto.getName());
        InterceptorDto expected1 = createDtoWithDefaults(firstSuffix);

        assertInterceptorWithDefaults(actual, expected1);

        // create application1
        applicationFacade.createApplication(createApplicationDto(secondSuffix));

        // create interceptor2 with application2
        InterceptorDto expected2 = createDto(secondSuffix);
        interceptorFacade.createInterceptor(expected2);

        Collection<InterceptorDto> actualInterceptors = interceptorFacade.getAllInterceptors();
        assertInterceptors(actualInterceptors, List.of(expected1, expected2));
    }

    @Test
    public void shouldSuccessfullyCreateAndDeleteInterceptor() {
        String firstSuffix = "1";
        //create application1
        ApplicationDto applicationDto = createApplicationDto(firstSuffix);
        applicationFacade.createApplication(applicationDto);

        // create interceptor1
        InterceptorDto interceptorDto = createDto(firstSuffix);
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
        ApplicationDto applicationDto = createApplicationDto("1");
        applicationFacade.createApplication(applicationDto);
        ApplicationDto applicationDto2 = createApplicationDto("2");
        applicationFacade.createApplication(applicationDto2);

        InterceptorDto interceptorDto = createDto("1");
        interceptorFacade.createInterceptor(interceptorDto);

        assertAppInterceptors(applicationDto.getName(), List.of("interceptor1"));

        InterceptorDto updatedInterceptor = createDto("1");
        updatedInterceptor.setEntities(List.of("application2"));

        interceptorFacade.updateInterceptor(interceptorDto.getName(), updatedInterceptor);

        assertAppInterceptors(applicationDto.getName(), List.of());
        assertAppInterceptors(applicationDto2.getName(), List.of("interceptor1"));

        InterceptorDto actual = interceptorFacade.getInterceptor(interceptorDto.getName());
        var expected = createDto("1");
        expected.setEntities(List.of("application2"));
        assertInterceptor(actual, expected);
    }

    @Test
    public void shouldSuccessfullyCreateAndUpdateInterceptorWithModels() {
        ModelDto modelDto1 = createModelDto("1");
        modelFacade.createModel(modelDto1);
        ModelDto modelDto2 = createModelDto("2");
        modelFacade.createModel(modelDto2);

        InterceptorDto interceptorDto = createDto("1");
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
        ApplicationDto applicationDto1 = createApplicationDto("1");
        applicationFacade.createApplication(applicationDto1);
        ApplicationDto applicationDto2 = createApplicationDto("2");
        applicationFacade.createApplication(applicationDto2);

        InterceptorDto interceptorDto = createDto("1");
        interceptorDto.setEntities(List.of("application1", "application2"));
        interceptorFacade.createInterceptor(interceptorDto);

        InterceptorDto actualInterceptor = interceptorFacade.getInterceptor("interceptor1");
        org.assertj.core.api.Assertions.assertThat(actualInterceptor.getEntities())
                .containsExactlyInAnyOrderElementsOf(List.of("application1", "application2"));

        applicationDto1.setInterceptors(List.of("interceptor1", "interceptor1", "interceptor1"));
        applicationFacade.updateApplication(applicationDto1.getName(), applicationDto1);

        actualInterceptor = interceptorFacade.getInterceptor("interceptor1");
        org.assertj.core.api.Assertions.assertThat(actualInterceptor.getEntities())
                .containsExactlyInAnyOrderElementsOf(List.of("application1", "application2"));

        ApplicationDto actualApplication1 = applicationFacade.getApplication(applicationDto1.getName());
        Assertions.assertEquals(actualApplication1.getInterceptors(), List.of("interceptor1", "interceptor1", "interceptor1"));
    }

    @Test
    public void shouldThrowExceptionWhenCreateInterceptorWithExistingName() {
        applicationFacade.createApplication(createApplicationDto("1"));

        InterceptorDto interceptorDto = createDto("1");
        interceptorFacade.createInterceptor(interceptorDto);

        EntityAlreadyExistsException exception = Assertions.assertThrows(
                EntityAlreadyExistsException.class,
                () -> interceptorFacade.createInterceptor(createDto("1"))
        );

        Assertions.assertEquals("Interceptor with name interceptor1 already exists", exception.getMessage());
    }

    @Test
    public void shouldThrowExceptionWhenUpdateInterceptorWithExistingName() {
        applicationFacade.createApplication(createApplicationDto("1"));
        applicationFacade.createApplication(createApplicationDto("2"));

        InterceptorDto interceptorDto = createDto("1");
        interceptorFacade.createInterceptor(interceptorDto);

        InterceptorDto interceptorDto2 = createDto("2");
        interceptorFacade.createInterceptor(interceptorDto2);

        interceptorDto.setName("interceptor2");

        IllegalArgumentException exception = Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> interceptorFacade.updateInterceptor("interceptor1", interceptorDto)
        );

        Assertions.assertEquals("Interceptor with name: 'interceptor1' can not be renamed. New interceptor name: 'interceptor2'", exception.getMessage());
    }

    private ApplicationDto createApplicationDto(String suffix) {
        ApplicationDto application = new ApplicationDto();
        application.setName("application" + suffix);
        application.setEndpoint("endpoint");
        return application;
    }

    private ModelDto createModelDto(String suffix) {
        ModelDto model = new ModelDto();
        model.setName("model" + suffix);
        model.setEndpoint("https://endpoint");
        return model;
    }

    private InterceptorDto createDtoWithDefaults(String suffix) {
        InterceptorDto dto = createDto(suffix);
        dto.setDefaults(Map.of("max_limit", 7000));
        return dto;
    }

    private InterceptorDto createDto(String suffix) {
        InterceptorDto interceptorDto = new InterceptorDto();
        interceptorDto.setName("interceptor" + suffix);
        interceptorDto.setDescription("description" + suffix);
        interceptorDto.setDisplayName("displayName" + suffix);
        interceptorDto.setEndpoint("https://endpoint.test.com/interceptor" + suffix);
        interceptorDto.setEntities(List.of("application" + suffix));
        return interceptorDto;
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
        interceptorDto.setDescription("Container interceptor");

        InterceptorContainerSourceDto sourceDto = new InterceptorContainerSourceDto(
                containerId,
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

        InterceptorDto interceptorDto = new InterceptorDto();
        interceptorDto.setName("refresh-interceptor");
        interceptorDto.setDescription("Refresh interceptor");

        InterceptorContainerSourceDto sourceDto = new InterceptorContainerSourceDto(
                containerId,
                completionPath,
                configPath
        );

        interceptorDto.setSource(sourceDto);
        interceptorFacade.createInterceptor(interceptorDto);

        InterceptorDto initialResult = interceptorFacade.getInterceptor("refresh-interceptor");
        Assertions.assertEquals(initialUrl + completionPath, initialResult.getEndpoint());
        Assertions.assertEquals(initialUrl + configPath, initialResult.getFeatures().getConfigurationEndpoint());

        // When
        interceptorFacade.refreshEndpoints();

        // Then
        InterceptorDto refreshedResult = interceptorFacade.getInterceptor("refresh-interceptor");
        Assertions.assertEquals(updatedUrl + completionPath, refreshedResult.getEndpoint());
        Assertions.assertEquals(updatedUrl + configPath, refreshedResult.getFeatures().getConfigurationEndpoint());

        Mockito.verify(deploymentManagerService, Mockito.atLeast(2)).getById(containerId);
    }
}
