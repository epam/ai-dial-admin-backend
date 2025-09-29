package com.epam.aidial.cfg.domain.util;

import com.epam.aidial.cfg.client.dto.DeploymentInfoDto;
import com.epam.aidial.cfg.dao.model.FeaturesEntity;
import com.epam.aidial.cfg.dao.model.InterceptorContainerEntity;
import com.epam.aidial.cfg.dao.model.InterceptorEntity;
import com.epam.aidial.cfg.dao.model.ModelContainerEntity;
import com.epam.aidial.cfg.dao.model.ModelEntity;
import com.epam.aidial.cfg.dao.model.ToolSetContainerEntity;
import com.epam.aidial.cfg.dao.model.ToolSetEntity;
import com.epam.aidial.cfg.domain.model.Features;
import com.epam.aidial.cfg.domain.model.Interceptor;
import com.epam.aidial.cfg.domain.model.Model;
import com.epam.aidial.cfg.domain.model.ToolSet;
import com.epam.aidial.cfg.domain.model.source.InterceptorContainerSource;
import com.epam.aidial.cfg.domain.model.source.ModelContainerSource;
import com.epam.aidial.cfg.domain.model.source.ToolSetContainerSource;
import com.epam.aidial.cfg.domain.service.DeploymentManagerService;
import com.epam.aidial.cfg.domain.validator.DeploymentInfoValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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
    void processContainerEndpoints_ForModel_ShouldSetEndpoint() {
        // given
        Model model = new Model();
        ModelContainerSource containerSource = new ModelContainerSource(CONTAINER_ID, CONTAINER_NAME, COMPLETION_PATH);
        model.setSource(containerSource);

        DeploymentInfoDto deploymentInfo = new DeploymentInfoDto();
        deploymentInfo.setUrl(CONTAINER_URL);

        when(deploymentManagerService.getById(CONTAINER_ID)).thenReturn(deploymentInfo);

        // when
        containerEndpointResolver.processContainerEndpoints(model);

        // then
        verify(deploymentManagerService).getById(CONTAINER_ID);
        verify(deploymentInfoValidator).validateDeploymentInfo(deploymentInfo, CONTAINER_ID);
        assertThat(model.getEndpoint()).isEqualTo(CONTAINER_URL + COMPLETION_PATH);
    }

    @Test
    void processContainerEndpoints_ForModelEntity_ShouldSetEndpoint() {
        // given
        ModelEntity modelEntity = new ModelEntity();
        ModelContainerEntity containerEntity = new ModelContainerEntity();
        containerEntity.setContainerId(CONTAINER_ID);
        containerEntity.setCompletionEndpointPath(COMPLETION_PATH);
        modelEntity.setModelContainer(containerEntity);

        DeploymentInfoDto deploymentInfo = new DeploymentInfoDto();
        deploymentInfo.setUrl(CONTAINER_URL);

        when(deploymentManagerService.getById(CONTAINER_ID)).thenReturn(deploymentInfo);

        // when
        containerEndpointResolver.processContainerEndpoints(modelEntity);

        // then
        verify(deploymentManagerService).getById(CONTAINER_ID);
        verify(deploymentInfoValidator).validateDeploymentInfo(deploymentInfo, CONTAINER_ID);
        assertThat(modelEntity.getEndpoint()).isEqualTo(CONTAINER_URL + COMPLETION_PATH);
    }

    @Test
    void processContainerEndpoints_ForInterceptor_ShouldSetEndpoints() {
        // given
        Interceptor interceptor = new Interceptor();
        InterceptorContainerSource containerSource = new InterceptorContainerSource(CONTAINER_ID, CONTAINER_NAME, COMPLETION_PATH, CONFIG_PATH);
        interceptor.setSource(containerSource);

        DeploymentInfoDto deploymentInfo = new DeploymentInfoDto();
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

        DeploymentInfoDto deploymentInfo = new DeploymentInfoDto();
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

        DeploymentInfoDto deploymentInfo = new DeploymentInfoDto();
        deploymentInfo.setUrl(CONTAINER_URL);

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

        DeploymentInfoDto deploymentInfo = new DeploymentInfoDto();
        deploymentInfo.setUrl(CONTAINER_URL);

        when(deploymentManagerService.getById(CONTAINER_ID)).thenReturn(deploymentInfo);

        // when
        containerEndpointResolver.processContainerEndpoints(toolSetEntity);

        // then
        verify(deploymentManagerService).getById(CONTAINER_ID);
        verify(deploymentInfoValidator).validateDeploymentInfo(deploymentInfo, CONTAINER_ID);
        assertThat(toolSetEntity.getEndpoint()).isEqualTo(CONTAINER_URL + COMPLETION_PATH);
    }

    @Test
    void processContainerEndpoints_WhenDeploymentInfoNull_ShouldDelegateValidation() {
        // given
        Model model = new Model();
        ModelContainerSource containerSource = new ModelContainerSource(CONTAINER_ID, CONTAINER_NAME, COMPLETION_PATH);
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
    void processContainerEndpoints_WithNullCompletionPath_ShouldHandleGracefully() {
        // given
        Model model = new Model();
        ModelContainerSource containerSource = new ModelContainerSource(CONTAINER_ID, CONTAINER_NAME, null);
        model.setSource(containerSource);

        DeploymentInfoDto deploymentInfo = new DeploymentInfoDto();
        deploymentInfo.setUrl(CONTAINER_URL);

        when(deploymentManagerService.getById(CONTAINER_ID)).thenReturn(deploymentInfo);

        // when
        containerEndpointResolver.processContainerEndpoints(model);

        // then
        assertThat(model.getEndpoint()).isEqualTo(CONTAINER_URL);
    }

    @Test
    void processContainerEndpoints_WithNullConfigPath_ShouldHandleGracefully() {
        // given
        Interceptor interceptor = new Interceptor();
        InterceptorContainerSource containerSource = new InterceptorContainerSource(CONTAINER_ID, CONTAINER_NAME, COMPLETION_PATH, null);
        interceptor.setSource(containerSource);

        DeploymentInfoDto deploymentInfo = new DeploymentInfoDto();
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

        DeploymentInfoDto deploymentInfo = new DeploymentInfoDto();
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

        DeploymentInfoDto deploymentInfo = new DeploymentInfoDto();
        deploymentInfo.setUrl(CONTAINER_URL);

        when(deploymentManagerService.getById(CONTAINER_ID)).thenReturn(deploymentInfo);

        // when
        containerEndpointResolver.processContainerEndpoints(interceptorEntity);

        // then
        assertThat(interceptorEntity.getFeatures()).isSameAs(existingFeatures);
        assertThat(interceptorEntity.getFeatures().getConfigurationEndpoint()).isEqualTo(CONTAINER_URL + CONFIG_PATH);
    }
}