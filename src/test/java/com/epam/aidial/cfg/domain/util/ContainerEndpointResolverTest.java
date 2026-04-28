package com.epam.aidial.cfg.domain.util;

import com.epam.aidial.cfg.client.dto.ApplicationDeploymentInfoDto;
import com.epam.aidial.cfg.client.dto.DeploymentInfoDto;
import com.epam.aidial.cfg.client.dto.InferenceDeploymentInfoDto;
import com.epam.aidial.cfg.client.dto.InterceptorDeploymentInfoDto;
import com.epam.aidial.cfg.client.dto.McpDeploymentInfoDto;
import com.epam.aidial.cfg.client.dto.NimDeploymentInfoDto;
import com.epam.aidial.cfg.dao.model.AdapterContainerEntity;
import com.epam.aidial.cfg.dao.model.AdapterEntity;
import com.epam.aidial.cfg.dao.model.ApplicationContainerEntity;
import com.epam.aidial.cfg.dao.model.ApplicationEntity;
import com.epam.aidial.cfg.dao.model.FeaturesEntity;
import com.epam.aidial.cfg.dao.model.InterceptorContainerEntity;
import com.epam.aidial.cfg.dao.model.InterceptorEntity;
import com.epam.aidial.cfg.dao.model.McpEntity;
import com.epam.aidial.cfg.dao.model.ModelContainerEntity;
import com.epam.aidial.cfg.dao.model.ModelEntity;
import com.epam.aidial.cfg.dao.model.ToolSetContainerEntity;
import com.epam.aidial.cfg.dao.model.ToolSetEntity;
import com.epam.aidial.cfg.domain.model.Adapter;
import com.epam.aidial.cfg.domain.model.Application;
import com.epam.aidial.cfg.domain.model.Features;
import com.epam.aidial.cfg.domain.model.Interceptor;
import com.epam.aidial.cfg.domain.model.Mcp;
import com.epam.aidial.cfg.domain.model.Model;
import com.epam.aidial.cfg.domain.model.ToolSet;
import com.epam.aidial.cfg.domain.model.source.AdapterContainerSource;
import com.epam.aidial.cfg.domain.model.source.ApplicationContainerSource;
import com.epam.aidial.cfg.domain.model.source.InterceptorContainerSource;
import com.epam.aidial.cfg.domain.model.source.ModelContainerSource;
import com.epam.aidial.cfg.domain.model.source.ToolSetContainerSource;
import com.epam.aidial.cfg.domain.service.DeploymentManagerService;
import com.epam.aidial.cfg.domain.validator.DeploymentInfoValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ContainerEndpointResolverTest {

    private static final String CONTAINER_ID = "550e8400-e29b-41d4-a716-446655440000";
    private static final String CONTAINER_NAME = "test-container";
    private static final String CONTAINER_URL = "https://test-container.com";
    private static final String COMPLETION_PATH = "/api/completion";
    private static final String RESPONSES_PATH = "/api/responses";
    private static final String CONFIG_PATH = "/api/config";

    @Mock
    private DeploymentManagerService deploymentManagerService;

    @Mock
    private DeploymentInfoValidator deploymentInfoValidator;

    private ContainerEndpointResolver containerEndpointResolver;

    @BeforeEach
    void setUp() {
        containerEndpointResolver = new ContainerEndpointResolver(deploymentManagerService, deploymentInfoValidator);
    }

    @Test
    void processContainerEndpoints_ForAdapter_ShouldSetBaseEndpointAndResponsesEndpointAndContainerName() {
        // given
        Adapter adapter = new Adapter();
        AdapterContainerSource containerSource = new AdapterContainerSource(CONTAINER_ID, null, COMPLETION_PATH, RESPONSES_PATH);
        adapter.setSource(containerSource);

        DeploymentInfoDto deploymentInfo = new InferenceDeploymentInfoDto();
        deploymentInfo.setUrl(CONTAINER_URL);
        deploymentInfo.setDisplayName(CONTAINER_NAME);

        when(deploymentManagerService.getById(CONTAINER_ID)).thenReturn(deploymentInfo);

        // when
        containerEndpointResolver.processContainerEndpoints(adapter);

        // then
        verify(deploymentManagerService).getById(CONTAINER_ID);
        verify(deploymentInfoValidator).validateDeploymentInfo(deploymentInfo, CONTAINER_ID);
        assertThat(adapter.getBaseEndpoint()).isEqualTo(CONTAINER_URL + COMPLETION_PATH);
        assertThat(adapter.getResponsesEndpoint()).isEqualTo(CONTAINER_URL + RESPONSES_PATH);
        assertThat(containerSource.getContainerName()).isEqualTo(CONTAINER_NAME);
    }

    @Test
    void processContainerEndpoints_ForAdapterEntity_ShouldSetBaseEndpointAndResponsesEndpointAndContainerName() {
        // given
        AdapterEntity adapterEntity = new AdapterEntity();
        AdapterContainerEntity containerEntity = new AdapterContainerEntity();
        containerEntity.setContainerId(CONTAINER_ID);
        containerEntity.setCompletionEndpointPath(COMPLETION_PATH);
        containerEntity.setResponsesEndpointPath(RESPONSES_PATH);
        adapterEntity.setAdapterContainer(containerEntity);

        DeploymentInfoDto deploymentInfo = new InferenceDeploymentInfoDto();
        deploymentInfo.setUrl(CONTAINER_URL);
        deploymentInfo.setDisplayName(CONTAINER_NAME);

        when(deploymentManagerService.getById(CONTAINER_ID)).thenReturn(deploymentInfo);

        // when
        containerEndpointResolver.processContainerEndpoints(adapterEntity);

        // then
        verify(deploymentManagerService).getById(CONTAINER_ID);
        verify(deploymentInfoValidator).validateDeploymentInfo(deploymentInfo, CONTAINER_ID);
        assertThat(adapterEntity.getBaseEndpoint()).isEqualTo(CONTAINER_URL + COMPLETION_PATH);
        assertThat(adapterEntity.getResponsesEndpoint()).isEqualTo(CONTAINER_URL + RESPONSES_PATH);
        assertThat(containerEntity.getContainerName()).isEqualTo(CONTAINER_NAME);
    }

    @Test
    void processContainerEndpoints_ForAdapter_WithNullCompletionPathAndResponsesPath_ShouldSetEndpointsToNull() {
        // given
        Adapter adapter = new Adapter();
        AdapterContainerSource containerSource = new AdapterContainerSource(CONTAINER_ID, null, null, null);
        adapter.setSource(containerSource);

        InferenceDeploymentInfoDto deploymentInfo = new InferenceDeploymentInfoDto();
        deploymentInfo.setUrl(CONTAINER_URL);

        when(deploymentManagerService.getById(CONTAINER_ID)).thenReturn(deploymentInfo);

        // when
        containerEndpointResolver.processContainerEndpoints(adapter);

        // then
        assertThat(adapter.getBaseEndpoint()).isNull();
        assertThat(adapter.getResponsesEndpoint()).isNull();
    }

    @Test
    void processContainerEndpoints_ForModel_ShouldSetEndpointAndResponsesEndpoint() {
        // given
        Model model = new Model();
        ModelContainerSource containerSource = new ModelContainerSource(CONTAINER_ID, CONTAINER_NAME, COMPLETION_PATH, RESPONSES_PATH);
        model.setSource(containerSource);

        DeploymentInfoDto deploymentInfo = new NimDeploymentInfoDto();
        deploymentInfo.setUrl(CONTAINER_URL);

        when(deploymentManagerService.getById(CONTAINER_ID)).thenReturn(deploymentInfo);

        // when
        containerEndpointResolver.processContainerEndpoints(model);

        // then
        verify(deploymentManagerService).getById(CONTAINER_ID);
        verify(deploymentInfoValidator).validateDeploymentInfo(deploymentInfo, CONTAINER_ID);
        assertThat(model.getEndpoint()).isEqualTo(CONTAINER_URL + COMPLETION_PATH);
        assertThat(model.getResponsesEndpoint()).isEqualTo(CONTAINER_URL + RESPONSES_PATH);
    }

    @Test
    void processContainerEndpoints_ForModelEntity_ShouldSetEndpointAndResponsesEndpoint() {
        // given
        ModelEntity modelEntity = new ModelEntity();
        ModelContainerEntity containerEntity = new ModelContainerEntity();
        containerEntity.setContainerId(CONTAINER_ID);
        containerEntity.setCompletionEndpointPath(COMPLETION_PATH);
        containerEntity.setResponsesEndpointPath(RESPONSES_PATH);
        modelEntity.setModelContainer(containerEntity);

        DeploymentInfoDto deploymentInfo = new NimDeploymentInfoDto();
        deploymentInfo.setUrl(CONTAINER_URL);

        when(deploymentManagerService.getById(CONTAINER_ID)).thenReturn(deploymentInfo);

        // when
        containerEndpointResolver.processContainerEndpoints(modelEntity);

        // then
        verify(deploymentManagerService).getById(CONTAINER_ID);
        verify(deploymentInfoValidator).validateDeploymentInfo(deploymentInfo, CONTAINER_ID);
        assertThat(modelEntity.getEndpoint()).isEqualTo(CONTAINER_URL + COMPLETION_PATH);
        assertThat(modelEntity.getResponsesEndpoint()).isEqualTo(CONTAINER_URL + RESPONSES_PATH);
    }

    @Test
    void processContainerEndpoints_ForInterceptor_ShouldSetEndpoints() {
        // given
        Interceptor interceptor = new Interceptor();
        InterceptorContainerSource containerSource = new InterceptorContainerSource(CONTAINER_ID, CONTAINER_NAME, COMPLETION_PATH, CONFIG_PATH);
        interceptor.setSource(containerSource);

        DeploymentInfoDto deploymentInfo = new InferenceDeploymentInfoDto();
        deploymentInfo.setUrl(CONTAINER_URL);

        when(deploymentManagerService.getById(CONTAINER_ID)).thenReturn(deploymentInfo);

        // when
        containerEndpointResolver.processContainerEndpoints(interceptor);

        // then
        verify(deploymentManagerService).getById(CONTAINER_ID);
        verify(deploymentInfoValidator).validateDeploymentInfo(deploymentInfo, CONTAINER_ID);
        assertThat(interceptor.getEndpoint()).isEqualTo(CONTAINER_URL + COMPLETION_PATH);
        assertThat(interceptor.getFeatures().getConfigurationEndpoint()).isEqualTo(CONTAINER_URL + CONFIG_PATH);
    }

    @Test
    void processContainerEndpoints_ForInterceptorEntity_ShouldSetEndpoints() {
        // given
        InterceptorEntity interceptorEntity = new InterceptorEntity();
        InterceptorContainerEntity containerEntity = new InterceptorContainerEntity();
        containerEntity.setContainerId(CONTAINER_ID);
        containerEntity.setCompletionEndpointPath(COMPLETION_PATH);
        containerEntity.setConfigurationEndpointPath(CONFIG_PATH);
        interceptorEntity.setInterceptorContainer(containerEntity);

        DeploymentInfoDto deploymentInfo = new InferenceDeploymentInfoDto();
        deploymentInfo.setUrl(CONTAINER_URL);

        when(deploymentManagerService.getById(CONTAINER_ID)).thenReturn(deploymentInfo);

        // when
        containerEndpointResolver.processContainerEndpoints(interceptorEntity);

        // then
        verify(deploymentManagerService).getById(CONTAINER_ID);
        verify(deploymentInfoValidator).validateDeploymentInfo(deploymentInfo, CONTAINER_ID);
        assertThat(interceptorEntity.getEndpoint()).isEqualTo(CONTAINER_URL + COMPLETION_PATH);
        assertThat(interceptorEntity.getFeatures().getConfigurationEndpoint()).isEqualTo(CONTAINER_URL + CONFIG_PATH);
    }

    @Test
    void processContainerEndpoints_ForToolSet_ShouldSetEndpoint() {
        // given
        ToolSet toolSet = new ToolSet();
        ToolSetContainerSource containerSource = new ToolSetContainerSource(CONTAINER_ID, CONTAINER_NAME, COMPLETION_PATH);
        toolSet.setSource(containerSource);

        McpDeploymentInfoDto deploymentInfo = new McpDeploymentInfoDto();
        deploymentInfo.setUrl(CONTAINER_URL);
        deploymentInfo.setTransport(McpDeploymentInfoDto.McpTransport.HTTP_STREAMING);

        when(deploymentManagerService.getById(CONTAINER_ID)).thenReturn(deploymentInfo);

        // when
        containerEndpointResolver.processContainerEndpoints(toolSet);

        // then
        verify(deploymentManagerService).getById(CONTAINER_ID);
        verify(deploymentInfoValidator).validateDeploymentInfo(deploymentInfo, CONTAINER_ID);
        assertThat(toolSet.getEndpoint()).isEqualTo(CONTAINER_URL + COMPLETION_PATH);
    }

    @Test
    void processContainerEndpoints_ForToolSetEntity_ShouldSetEndpoint() {
        // given
        ToolSetEntity toolSetEntity = new ToolSetEntity();
        ToolSetContainerEntity containerEntity = new ToolSetContainerEntity();
        containerEntity.setContainerId(CONTAINER_ID);
        containerEntity.setCompletionEndpointPath(COMPLETION_PATH);
        toolSetEntity.setToolSetContainer(containerEntity);

        McpDeploymentInfoDto deploymentInfo = new McpDeploymentInfoDto();
        deploymentInfo.setUrl(CONTAINER_URL);
        deploymentInfo.setTransport(McpDeploymentInfoDto.McpTransport.HTTP_STREAMING);

        when(deploymentManagerService.getById(CONTAINER_ID)).thenReturn(deploymentInfo);

        // when
        containerEndpointResolver.processContainerEndpoints(toolSetEntity);

        // then
        verify(deploymentManagerService).getById(CONTAINER_ID);
        verify(deploymentInfoValidator).validateDeploymentInfo(deploymentInfo, CONTAINER_ID);
        assertThat(toolSetEntity.getEndpoint()).isEqualTo(CONTAINER_URL + COMPLETION_PATH);
    }

    @Test
    void processContainerEndpoints_ForApplication_WithMcpNullAndPathBlank_ShouldKeepMcpNull() {
        // given
        Application application = new Application();
        ApplicationContainerSource containerSource = new ApplicationContainerSource(CONTAINER_ID, null, COMPLETION_PATH, null);
        application.setSource(containerSource);

        DeploymentInfoDto deploymentInfo = new ApplicationDeploymentInfoDto();
        deploymentInfo.setUrl(CONTAINER_URL);
        deploymentInfo.setDisplayName(CONTAINER_NAME);

        when(deploymentManagerService.getById(CONTAINER_ID)).thenReturn(deploymentInfo);

        // when
        containerEndpointResolver.processContainerEndpoints(application);

        // then
        assertThat(application.getEndpoint()).isEqualTo(CONTAINER_URL + COMPLETION_PATH);
        assertThat(containerSource.getContainerName()).isEqualTo(CONTAINER_NAME);
        assertThat(application.getMcp()).isNull();
    }

    @Test
    void processContainerEndpoints_ForApplication_WithMcpNullAndPathProvided_ShouldCreateMcpWithResolvedEndpoint() {
        // given
        Application application = new Application();
        ApplicationContainerSource containerSource = new ApplicationContainerSource(CONTAINER_ID, null, COMPLETION_PATH, CONFIG_PATH);
        application.setSource(containerSource);

        DeploymentInfoDto deploymentInfo = new ApplicationDeploymentInfoDto();
        deploymentInfo.setUrl(CONTAINER_URL);
        deploymentInfo.setDisplayName(CONTAINER_NAME);

        when(deploymentManagerService.getById(CONTAINER_ID)).thenReturn(deploymentInfo);

        // when
        containerEndpointResolver.processContainerEndpoints(application);

        // then
        assertThat(application.getEndpoint()).isEqualTo(CONTAINER_URL + COMPLETION_PATH);
        assertThat(containerSource.getContainerName()).isEqualTo(CONTAINER_NAME);
        assertThat(application.getMcp()).isNotNull();
        assertThat(application.getMcp().getEndpoint()).isEqualTo(CONTAINER_URL + CONFIG_PATH);
    }

    @Test
    void processContainerEndpoints_ForApplication_WithMcpProvidedAndPathBlank_ShouldSetMcpEndpointToContainerUrl() {
        // given
        Application application = new Application();
        ApplicationContainerSource containerSource = new ApplicationContainerSource(CONTAINER_ID, null, COMPLETION_PATH, null);
        application.setSource(containerSource);
        Mcp existingMcp = new Mcp();
        existingMcp.setEndpoint("stale-endpoint");
        application.setMcp(existingMcp);

        DeploymentInfoDto deploymentInfo = new ApplicationDeploymentInfoDto();
        deploymentInfo.setUrl(CONTAINER_URL);
        deploymentInfo.setDisplayName(CONTAINER_NAME);

        when(deploymentManagerService.getById(CONTAINER_ID)).thenReturn(deploymentInfo);

        // when
        containerEndpointResolver.processContainerEndpoints(application);

        // then
        assertThat(application.getEndpoint()).isEqualTo(CONTAINER_URL + COMPLETION_PATH);
        assertThat(containerSource.getContainerName()).isEqualTo(CONTAINER_NAME);
        assertThat(application.getMcp()).isSameAs(existingMcp);
        assertThat(application.getMcp().getEndpoint()).isEqualTo(CONTAINER_URL);
    }

    @Test
    void processContainerEndpoints_ForApplication_WithMcpProvidedAndPathProvided_ShouldSetMcpEndpointToContainerUrlPlusPath() {
        // given
        Application application = new Application();
        ApplicationContainerSource containerSource = new ApplicationContainerSource(CONTAINER_ID, null, COMPLETION_PATH, CONFIG_PATH);
        application.setSource(containerSource);
        Mcp existingMcp = new Mcp();
        existingMcp.setEndpoint("stale-endpoint");
        application.setMcp(existingMcp);

        DeploymentInfoDto deploymentInfo = new ApplicationDeploymentInfoDto();
        deploymentInfo.setUrl(CONTAINER_URL);
        deploymentInfo.setDisplayName(CONTAINER_NAME);

        when(deploymentManagerService.getById(CONTAINER_ID)).thenReturn(deploymentInfo);

        // when
        containerEndpointResolver.processContainerEndpoints(application);

        // then
        assertThat(application.getEndpoint()).isEqualTo(CONTAINER_URL + COMPLETION_PATH);
        assertThat(containerSource.getContainerName()).isEqualTo(CONTAINER_NAME);
        assertThat(application.getMcp()).isSameAs(existingMcp);
        assertThat(application.getMcp().getEndpoint()).isEqualTo(CONTAINER_URL + CONFIG_PATH);
    }

    @Test
    void processContainerEndpoints_ForApplicationEntity_WithMcpNullAndPathBlank_ShouldKeepMcpNull() {
        // given
        ApplicationEntity applicationEntity = new ApplicationEntity();
        ApplicationContainerEntity containerEntity = new ApplicationContainerEntity();
        containerEntity.setContainerId(CONTAINER_ID);
        containerEntity.setCompletionEndpointPath(COMPLETION_PATH);
        applicationEntity.setApplicationContainer(containerEntity);

        DeploymentInfoDto deploymentInfo = new ApplicationDeploymentInfoDto();
        deploymentInfo.setUrl(CONTAINER_URL);
        deploymentInfo.setDisplayName(CONTAINER_NAME);

        when(deploymentManagerService.getById(CONTAINER_ID)).thenReturn(deploymentInfo);

        // when
        containerEndpointResolver.processContainerEndpoints(applicationEntity);

        // then
        assertThat(applicationEntity.getEndpoint()).isEqualTo(CONTAINER_URL + COMPLETION_PATH);
        assertThat(containerEntity.getContainerName()).isEqualTo(CONTAINER_NAME);
        assertThat(applicationEntity.getMcp()).isNull();
    }

    @Test
    void processContainerEndpoints_ForApplicationEntity_WithMcpNullAndPathProvided_ShouldCreateMcpWithResolvedEndpoint() {
        // given
        ApplicationEntity applicationEntity = new ApplicationEntity();
        ApplicationContainerEntity containerEntity = new ApplicationContainerEntity();
        containerEntity.setContainerId(CONTAINER_ID);
        containerEntity.setCompletionEndpointPath(COMPLETION_PATH);
        containerEntity.setMcpEndpointPath(CONFIG_PATH);
        applicationEntity.setApplicationContainer(containerEntity);

        DeploymentInfoDto deploymentInfo = new ApplicationDeploymentInfoDto();
        deploymentInfo.setUrl(CONTAINER_URL);
        deploymentInfo.setDisplayName(CONTAINER_NAME);

        when(deploymentManagerService.getById(CONTAINER_ID)).thenReturn(deploymentInfo);

        // when
        containerEndpointResolver.processContainerEndpoints(applicationEntity);

        // then
        assertThat(applicationEntity.getEndpoint()).isEqualTo(CONTAINER_URL + COMPLETION_PATH);
        assertThat(containerEntity.getContainerName()).isEqualTo(CONTAINER_NAME);
        assertThat(applicationEntity.getMcp()).isNotNull();
        assertThat(applicationEntity.getMcp().getEndpoint()).isEqualTo(CONTAINER_URL + CONFIG_PATH);
    }

    @Test
    void processContainerEndpoints_ForApplicationEntity_WithMcpProvidedAndPathBlank_ShouldSetMcpEndpointToContainerUrl() {
        // given
        ApplicationEntity applicationEntity = new ApplicationEntity();
        ApplicationContainerEntity containerEntity = new ApplicationContainerEntity();
        containerEntity.setContainerId(CONTAINER_ID);
        containerEntity.setCompletionEndpointPath(COMPLETION_PATH);
        applicationEntity.setApplicationContainer(containerEntity);
        McpEntity existingMcp = new McpEntity();
        existingMcp.setEndpoint("stale-endpoint");
        applicationEntity.setMcp(existingMcp);

        DeploymentInfoDto deploymentInfo = new ApplicationDeploymentInfoDto();
        deploymentInfo.setUrl(CONTAINER_URL);
        deploymentInfo.setDisplayName(CONTAINER_NAME);

        when(deploymentManagerService.getById(CONTAINER_ID)).thenReturn(deploymentInfo);

        // when
        containerEndpointResolver.processContainerEndpoints(applicationEntity);

        // then
        assertThat(applicationEntity.getMcp()).isSameAs(existingMcp);
        assertThat(applicationEntity.getMcp().getEndpoint()).isEqualTo(CONTAINER_URL);
    }

    @Test
    void processContainerEndpoints_ForApplicationEntity_WithMcpProvidedAndPathProvided_ShouldSetMcpEndpointToContainerUrlPlusPath() {
        // given
        ApplicationEntity applicationEntity = new ApplicationEntity();
        ApplicationContainerEntity containerEntity = new ApplicationContainerEntity();
        containerEntity.setContainerId(CONTAINER_ID);
        containerEntity.setCompletionEndpointPath(COMPLETION_PATH);
        containerEntity.setMcpEndpointPath(CONFIG_PATH);
        applicationEntity.setApplicationContainer(containerEntity);
        McpEntity existingMcp = new McpEntity();
        existingMcp.setEndpoint("stale-endpoint");
        applicationEntity.setMcp(existingMcp);

        DeploymentInfoDto deploymentInfo = new ApplicationDeploymentInfoDto();
        deploymentInfo.setUrl(CONTAINER_URL);
        deploymentInfo.setDisplayName(CONTAINER_NAME);

        when(deploymentManagerService.getById(CONTAINER_ID)).thenReturn(deploymentInfo);

        // when
        containerEndpointResolver.processContainerEndpoints(applicationEntity);

        // then
        assertThat(applicationEntity.getMcp()).isSameAs(existingMcp);
        assertThat(applicationEntity.getMcp().getEndpoint()).isEqualTo(CONTAINER_URL + CONFIG_PATH);
    }

    @Test
    void processContainerEndpoints_WhenDeploymentInfoNull_ShouldDelegateValidation() {
        // given
        Model model = new Model();
        ModelContainerSource containerSource = new ModelContainerSource(CONTAINER_ID, CONTAINER_NAME, COMPLETION_PATH, RESPONSES_PATH);
        model.setSource(containerSource);

        when(deploymentManagerService.getById(CONTAINER_ID)).thenReturn(null);
        doThrow(new IllegalArgumentException("Container not found")).when(deploymentInfoValidator)
                .validateDeploymentInfo(null, CONTAINER_ID);

        // when/then
        assertThatThrownBy(() -> containerEndpointResolver.processContainerEndpoints(model))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Container not found");
    }

    @Test
    void processContainerEndpoints_WithNullCompletionPathAndResponsesPath_ShouldHandleGracefully() {
        // given
        Model model = new Model();
        ModelContainerSource containerSource = new ModelContainerSource(CONTAINER_ID, CONTAINER_NAME, null, null);
        model.setSource(containerSource);

        InferenceDeploymentInfoDto deploymentInfo = new InferenceDeploymentInfoDto();
        deploymentInfo.setUrl(CONTAINER_URL);

        when(deploymentManagerService.getById(CONTAINER_ID)).thenReturn(deploymentInfo);

        // when
        containerEndpointResolver.processContainerEndpoints(model);

        // then
        assertThat(model.getEndpoint()).isNull();
        assertThat(model.getResponsesEndpoint()).isNull();
    }

    @Test
    void processContainerEndpoints_WithNullConfigPath_ShouldHandleGracefully() {
        // given
        Interceptor interceptor = new Interceptor();
        InterceptorContainerSource containerSource = new InterceptorContainerSource(CONTAINER_ID, CONTAINER_NAME, COMPLETION_PATH, null);
        interceptor.setSource(containerSource);

        DeploymentInfoDto deploymentInfo = new InterceptorDeploymentInfoDto();
        deploymentInfo.setUrl(CONTAINER_URL);

        when(deploymentManagerService.getById(CONTAINER_ID)).thenReturn(deploymentInfo);

        // when
        containerEndpointResolver.processContainerEndpoints(interceptor);

        // then
        assertThat(interceptor.getEndpoint()).isEqualTo(CONTAINER_URL + COMPLETION_PATH);
        assertThat(interceptor.getFeatures().getConfigurationEndpoint()).isEqualTo(CONTAINER_URL);
    }

    @Test
    void processContainerEndpoints_ForInterceptorWithExistingFeatures_ShouldUpdateFeatures() {
        // given
        Interceptor interceptor = new Interceptor();
        InterceptorContainerSource containerSource = new InterceptorContainerSource(CONTAINER_ID, CONTAINER_NAME, COMPLETION_PATH, CONFIG_PATH);
        interceptor.setSource(containerSource);
        
        Features existingFeatures = new Features();
        existingFeatures.setConfigurationEndpoint("old-endpoint");
        interceptor.setFeatures(existingFeatures);

        DeploymentInfoDto deploymentInfo = new InterceptorDeploymentInfoDto();
        deploymentInfo.setUrl(CONTAINER_URL);

        when(deploymentManagerService.getById(CONTAINER_ID)).thenReturn(deploymentInfo);

        // when
        containerEndpointResolver.processContainerEndpoints(interceptor);

        // then
        assertThat(interceptor.getFeatures()).isSameAs(existingFeatures);
        assertThat(interceptor.getFeatures().getConfigurationEndpoint()).isEqualTo(CONTAINER_URL + CONFIG_PATH);
    }

    @Test
    void processContainerEndpoints_ForInterceptorEntityWithExistingFeatures_ShouldUpdateFeatures() {
        // given
        InterceptorEntity interceptorEntity = new InterceptorEntity();
        InterceptorContainerEntity containerEntity = new InterceptorContainerEntity();
        containerEntity.setContainerId(CONTAINER_ID);
        containerEntity.setCompletionEndpointPath(COMPLETION_PATH);
        containerEntity.setConfigurationEndpointPath(CONFIG_PATH);
        interceptorEntity.setInterceptorContainer(containerEntity);
        
        FeaturesEntity existingFeatures = new FeaturesEntity();
        existingFeatures.setConfigurationEndpoint("old-endpoint");
        interceptorEntity.setFeatures(existingFeatures);

        DeploymentInfoDto deploymentInfo = new InterceptorDeploymentInfoDto();
        deploymentInfo.setUrl(CONTAINER_URL);

        when(deploymentManagerService.getById(CONTAINER_ID)).thenReturn(deploymentInfo);

        // when
        containerEndpointResolver.processContainerEndpoints(interceptorEntity);

        // then
        assertThat(interceptorEntity.getFeatures()).isSameAs(existingFeatures);
        assertThat(interceptorEntity.getFeatures().getConfigurationEndpoint()).isEqualTo(CONTAINER_URL + CONFIG_PATH);
    }

    @ParameterizedTest
    @MethodSource("slashCompatibilityTestCases")
    void processContainerEndpoints_ForModel_WithDifferentSlashCombinations_ShouldResolveCorrectly(
            String url, String path, String responsesPath, String expectedEndpoint, String expectedResponsesEndpoint,
            String testDescription) {
        // given
        Model model = new Model();
        ModelContainerSource containerSource = new ModelContainerSource(CONTAINER_ID, CONTAINER_NAME, path, responsesPath);
        model.setSource(containerSource);

        DeploymentInfoDto deploymentInfo = new NimDeploymentInfoDto();
        deploymentInfo.setUrl(url);

        when(deploymentManagerService.getById(CONTAINER_ID)).thenReturn(deploymentInfo);

        // when
        containerEndpointResolver.processContainerEndpoints(model);

        // then
        assertThat(model.getEndpoint())
                .as(testDescription)
                .isEqualTo(expectedEndpoint);
        assertThat(model.getResponsesEndpoint())
                .as(testDescription)
                .isEqualTo(expectedResponsesEndpoint);
    }

    private static Stream<Arguments> slashCompatibilityTestCases() {
        return Stream.of(
                Arguments.of(
                        CONTAINER_URL + "/",
                        COMPLETION_PATH,
                        RESPONSES_PATH,
                        CONTAINER_URL + COMPLETION_PATH,
                        CONTAINER_URL + RESPONSES_PATH,
                        "URL ends with slash and path starts with slash - should avoid double slash"
                ),
                Arguments.of(
                        CONTAINER_URL,
                        "api/completion",
                        "api/responses",
                        CONTAINER_URL + "/api/completion",
                        CONTAINER_URL + "/api/responses",
                        "URL has no trailing slash and path has no leading slash - should add slash"
                ),
                Arguments.of(
                        CONTAINER_URL + "/",
                        "api/completion",
                        "api/responses",
                        CONTAINER_URL + "/api/completion",
                        CONTAINER_URL + "/api/responses",
                        "URL ends with slash and path has no leading slash - should concatenate correctly"
                ),
                Arguments.of(
                        CONTAINER_URL,
                        COMPLETION_PATH,
                        RESPONSES_PATH,
                        CONTAINER_URL + COMPLETION_PATH,
                        CONTAINER_URL + RESPONSES_PATH,
                        "URL has no trailing slash and path starts with slash - should concatenate correctly"
                ),
                Arguments.of(
                        CONTAINER_URL,
                        "",
                        "",
                        null,
                        null,
                        "Empty path - should return URL only"
                ),
                Arguments.of(
                        CONTAINER_URL + "/",
                        "",
                        "",
                        null,
                        null,
                        "URL ends with slash and empty path - should return URL with trailing slash"
                )
        );
    }
}