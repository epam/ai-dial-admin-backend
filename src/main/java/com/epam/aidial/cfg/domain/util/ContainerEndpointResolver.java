package com.epam.aidial.cfg.domain.util;

import com.epam.aidial.cfg.client.dto.DeploymentInfoDto;
import com.epam.aidial.cfg.client.dto.McpDeploymentInfoDto;
import com.epam.aidial.cfg.configuration.logging.LogExecution;
import com.epam.aidial.cfg.dao.model.AdapterContainerEntity;
import com.epam.aidial.cfg.dao.model.AdapterEntity;
import com.epam.aidial.cfg.dao.model.FeaturesEntity;
import com.epam.aidial.cfg.dao.model.InterceptorContainerEntity;
import com.epam.aidial.cfg.dao.model.InterceptorEntity;
import com.epam.aidial.cfg.dao.model.ModelContainerEntity;
import com.epam.aidial.cfg.dao.model.ModelEntity;
import com.epam.aidial.cfg.dao.model.ToolSetContainerEntity;
import com.epam.aidial.cfg.dao.model.ToolSetEntity;
import com.epam.aidial.cfg.domain.model.Adapter;
import com.epam.aidial.cfg.domain.model.Features;
import com.epam.aidial.cfg.domain.model.Interceptor;
import com.epam.aidial.cfg.domain.model.Model;
import com.epam.aidial.cfg.domain.model.ToolSet;
import com.epam.aidial.cfg.domain.model.source.AdapterContainerSource;
import com.epam.aidial.cfg.domain.model.source.InterceptorContainerSource;
import com.epam.aidial.cfg.domain.model.source.ModelContainerSource;
import com.epam.aidial.cfg.domain.model.source.ToolSetContainerSource;
import com.epam.aidial.cfg.domain.service.DeploymentManagerService;
import com.epam.aidial.cfg.domain.validator.DeploymentInfoValidator;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * Service class for handling container endpoint operations.
 */
@Service
@RequiredArgsConstructor
@LogExecution
public class ContainerEndpointResolver {

    private final DeploymentManagerService deploymentManagerService;
    private final DeploymentInfoValidator deploymentInfoValidator;

    public void processContainerEndpoints(ToolSet toolSet) {
        ToolSetContainerSource containerSource = (ToolSetContainerSource) toolSet.getSource();
        String containerId = containerSource.getContainerId();
        DeploymentInfoDto deploymentInfo = deploymentManagerService.getById(containerId);
        deploymentInfoValidator.validateDeploymentInfo(deploymentInfo, containerId);

        processContainerEndpoints(
                containerSource,
                ToolSetContainerSource::getCompletionEndpointPath,
                null,
                deploymentInfo,
                (ToolSet target, ContainerEndpoints endpoints) -> {
                    ToolSetContainerSource targetSource = (ToolSetContainerSource) target.getSource();
                    targetSource.setContainerName(endpoints.containerName());
                    target.setEndpoint(endpoints.completionEndpoint());
                    // Sync Transport from MCP Deployment
                    if (deploymentInfo instanceof McpDeploymentInfoDto mcpDeployment) {
                        if (mcpDeployment.getTransport() != null) {
                            target.setTransport(convertTransport(mcpDeployment.getTransport()));
                        }
                    }
                },
                toolSet
        );
    }

    public void processContainerEndpoints(ToolSetEntity toolSetEntity) {
        ToolSetContainerEntity containerEntity = toolSetEntity.getToolSetContainer();
        String containerId = containerEntity.getContainerId();
        DeploymentInfoDto deploymentInfo = deploymentManagerService.getById(containerId);
        deploymentInfoValidator.validateDeploymentInfo(deploymentInfo, containerId);

        processContainerEndpoints(
                containerEntity,
                ToolSetContainerEntity::getCompletionEndpointPath,
                null,
                deploymentInfo,
                (ToolSetEntity entity, ContainerEndpoints endpoints) -> {
                    ToolSetContainerEntity targetContainer = entity.getToolSetContainer();
                    targetContainer.setContainerName(endpoints.containerName());
                    entity.setEndpoint(endpoints.completionEndpoint());
                    // Sync Transport from MCP Deployment
                    if (deploymentInfo instanceof McpDeploymentInfoDto mcpDeployment) {
                        if (mcpDeployment.getTransport() != null) {
                            entity.setTransport(convertTransportEntity(mcpDeployment.getTransport()));
                        }
                    }
                },
                toolSetEntity
        );
    }

    public void processContainerEndpoints(Adapter adapter) {
        AdapterContainerSource containerSource = (AdapterContainerSource) adapter.getSource();
        processContainerEndpoints(
                containerSource.getContainerId(),
                containerSource,
                AdapterContainerSource::getCompletionEndpointPath,
                null,
                (target, endpoints) -> {
                    AdapterContainerSource targetSource = (AdapterContainerSource) target.getSource();
                    targetSource.setContainerName(endpoints.containerName());
                    target.setBaseEndpoint(endpoints.completionEndpoint());
                },
                adapter
        );
    }

    public void processContainerEndpoints(AdapterEntity adapterEntity) {
        AdapterContainerEntity adapterContainerEntity = adapterEntity.getAdapterContainer();
        processContainerEndpoints(
                adapterContainerEntity.getContainerId(),
                adapterContainerEntity,
                AdapterContainerEntity::getCompletionEndpointPath,
                null,
                (entity, endpoints) -> {
                    AdapterContainerEntity targetContainer = entity.getAdapterContainer();
                    targetContainer.setContainerName(endpoints.containerName());
                    entity.setBaseEndpoint(endpoints.completionEndpoint());
                },
                adapterEntity
        );
    }

    public void processContainerEndpoints(Model model) {
        ModelContainerSource containerSource = (ModelContainerSource) model.getSource();
        processContainerEndpoints(
                containerSource.getContainerId(),
                containerSource,
                ModelContainerSource::getCompletionEndpointPath,
                null,
                (target, endpoints) -> {
                    ModelContainerSource targetSource = (ModelContainerSource) target.getSource();
                    targetSource.setContainerName(endpoints.containerName());
                    target.setEndpoint(endpoints.completionEndpoint());
                },
                model
        );
    }

    public void processContainerEndpoints(ModelEntity modelEntity) {
        ModelContainerEntity modelContainerEntity = modelEntity.getModelContainer();
        processContainerEndpoints(
                modelContainerEntity.getContainerId(),
                modelContainerEntity,
                ModelContainerEntity::getCompletionEndpointPath,
                null,
                (entity, endpoints) -> {
                    ModelContainerEntity targetContainer = entity.getModelContainer();
                    targetContainer.setContainerName(endpoints.containerName());
                    entity.setEndpoint(endpoints.completionEndpoint());
                },
                modelEntity
        );
    }

    public void processContainerEndpoints(Interceptor interceptor) {
        InterceptorContainerSource containerSource = (InterceptorContainerSource) interceptor.getSource();
        processContainerEndpoints(
                containerSource.getContainerId(),
                containerSource,
                InterceptorContainerSource::getCompletionEndpointPath,
                InterceptorContainerSource::getConfigurationEndpointPath,
                (target, endpoints) -> {
                    InterceptorContainerSource targetSource = (InterceptorContainerSource) target.getSource();
                    targetSource.setContainerName(endpoints.containerName());
                    target.setEndpoint(endpoints.completionEndpoint());
                    Features features = Optional.ofNullable(target.getFeatures()).orElse(new Features());
                    target.setFeatures(features);
                    features.setConfigurationEndpoint(endpoints.configurationEndpoint());
                },
                interceptor
        );
    }

    public void processContainerEndpoints(InterceptorEntity interceptorEntity) {
        InterceptorContainerEntity interceptorContainerEntity = interceptorEntity.getInterceptorContainer();
        processContainerEndpoints(
                interceptorContainerEntity.getContainerId(),
                interceptorContainerEntity,
                InterceptorContainerEntity::getCompletionEndpointPath,
                InterceptorContainerEntity::getConfigurationEndpointPath,
                (entity, endpoints) -> {
                    InterceptorContainerEntity targetContainer = entity.getInterceptorContainer();
                    targetContainer.setContainerName(endpoints.containerName());
                    entity.setEndpoint(endpoints.completionEndpoint());
                    FeaturesEntity features = Optional.ofNullable(entity.getFeatures()).orElse(new FeaturesEntity());
                    entity.setFeatures(features);
                    features.setConfigurationEndpoint(endpoints.configurationEndpoint());
                },
                interceptorEntity
        );
    }

    /**
     * Processes container endpoints and applies them using the provided consumer.
     * This method handles the common pattern of:
     * 1. Getting deployment info (if not provided)
     * 2. Validating deployment info
     * 3. Resolving endpoints
     * 4. Setting endpoints on target object
     *
     * @param <T> the type of object containing endpoint paths
     * @param <R> the type of object to receive the resolved endpoints
     * @param containerId the container ID
     * @param pathProvider object containing endpoint paths
     * @param completionPathExtractor function to extract completion path from pathProvider
     * @param configPathExtractor function to extract configuration path from pathProvider
     * @param endpointConsumer consumer to set the resolved endpoints on target object
     * @param target the object to receive the resolved endpoints
     */
    private <T, R> void processContainerEndpoints(
            String containerId,
            T pathProvider,
            Function<T, String> completionPathExtractor,
            @Nullable Function<T, String> configPathExtractor,
            BiConsumer<R, ContainerEndpoints> endpointConsumer,
            R target) {

        DeploymentInfoDto deploymentInfo = deploymentManagerService.getById(containerId);
        deploymentInfoValidator.validateDeploymentInfo(deploymentInfo, containerId);
        processContainerEndpoints(pathProvider, completionPathExtractor, configPathExtractor, deploymentInfo, endpointConsumer, target);
    }

    /**
     * Processes container endpoints with a pre-fetched DeploymentInfoDto.
     * This overload is used when DeploymentInfoDto is already available (e.g., for Transport sync).
     *
     * @param <T> the type of object containing endpoint paths
     * @param <R> the type of object to receive the resolved endpoints
     * @param pathProvider object containing endpoint paths
     * @param completionPathExtractor function to extract completion path from pathProvider
     * @param configPathExtractor function to extract configuration path from pathProvider
     * @param deploymentInfo the pre-fetched deployment info
     * @param endpointConsumer consumer to set the resolved endpoints on target object
     * @param target the object to receive the resolved endpoints
     */
    private <T, R> void processContainerEndpoints(
            T pathProvider,
            Function<T, String> completionPathExtractor,
            @Nullable Function<T, String> configPathExtractor,
            DeploymentInfoDto deploymentInfo,
            BiConsumer<R, ContainerEndpoints> endpointConsumer,
            R target) {

        String containerName = deploymentInfo.getDisplayName();
        String containerUrl = deploymentInfo.getUrl();
        String completionPath = completionPathExtractor.apply(pathProvider);
        String configPath = configPathExtractor != null ? configPathExtractor.apply(pathProvider) : null;

        ContainerEndpoints containerEndpoints = resolveEndpoints(containerName, containerUrl, completionPath, configPath);
        endpointConsumer.accept(target, containerEndpoints);
    }

    /**
     * Resolves endpoints based on container URL and endpoint paths.
     *
     * @param containerName the name of the container
     * @param containerUrl the base URL of the container
     * @param completionEndpointPath the path for the completion endpoint
     * @param configurationEndpointPath the path for the configuration endpoint
     * @return ContainerEndpoints
     */
    private static ContainerEndpoints resolveEndpoints(
            String containerName,
            String containerUrl,
            String completionEndpointPath,
            String configurationEndpointPath) {
        String completionEndpoint = resolveEndpoint(containerUrl, completionEndpointPath);
        String configurationEndpoint = resolveEndpoint(containerUrl, configurationEndpointPath);
        return new ContainerEndpoints(containerName, completionEndpoint, configurationEndpoint);
    }

    /**
     * Resolves a single endpoint by combining URL and path.
     * Handles slash compatibility to avoid double slashes or missing slashes.
     *
     * @param url the base URL
     * @param path the endpoint path
     * @return the complete endpoint URL
     */
    private static String resolveEndpoint(final String url, final String path) {
        if (StringUtils.isBlank(path)) {
            return url;
        }

        boolean urlEndsWithSlash = url.endsWith("/");
        boolean pathStartsWithSlash = path.startsWith("/");

        if (urlEndsWithSlash && pathStartsWithSlash) {
            // Both have slashes - remove one to avoid double slash
            return url + path.substring(1);
        } else if (!urlEndsWithSlash && !pathStartsWithSlash) {
            // Neither has slash - add one
            return url + "/" + path;
        } else {
            // One has slash, one doesn't - concatenate as is
            return url + path;
        }
    }

    private record ContainerEndpoints(String containerName, String completionEndpoint, String configurationEndpoint) { }

    /**
     * Converts McpDeploymentInfoDto.McpTransport to ToolSet.Transport
     */
    private static ToolSet.Transport convertTransport(McpDeploymentInfoDto.McpTransport transport) {
        if (transport == null) {
            return null;
        }
        return switch (transport) {
            case HTTP_STREAMING -> ToolSet.Transport.HTTP;
            case SSE -> ToolSet.Transport.SSE;
        };
    }

    /**
     * Converts McpDeploymentInfoDto.McpTransport to ToolSetEntity.TransportEntity
     */
    private static ToolSetEntity.TransportEntity convertTransportEntity(McpDeploymentInfoDto.McpTransport transport) {
        if (transport == null) {
            return null;
        }
        return switch (transport) {
            case HTTP_STREAMING -> ToolSetEntity.TransportEntity.HTTP;
            case SSE -> ToolSetEntity.TransportEntity.SSE;
        };
    }
}