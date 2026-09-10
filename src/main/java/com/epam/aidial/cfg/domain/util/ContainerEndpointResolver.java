package com.epam.aidial.cfg.domain.util;

import com.epam.aidial.cfg.client.dto.DeploymentInfoDto;
import com.epam.aidial.cfg.client.dto.McpDeploymentInfoDto;
import com.epam.aidial.cfg.configuration.logging.LogExecution;
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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * Service class for handling container endpoint operations.
 */
@Slf4j
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
                AdapterContainerSource::getResponsesEndpointPath,
                (target, endpoints) -> {
                    AdapterContainerSource targetSource = (AdapterContainerSource) target.getSource();
                    targetSource.setContainerName(endpoints.containerName());
                    if (StringUtils.isNotBlank(targetSource.getCompletionEndpointPath())) {
                        target.setBaseEndpoint(endpoints.completionEndpoint());
                    } else {
                        target.setBaseEndpoint(null);
                    }
                    if (StringUtils.isNotBlank(targetSource.getResponsesEndpointPath())) {
                        target.setResponsesEndpoint(endpoints.configurationEndpoint());
                    } else {
                        target.setResponsesEndpoint(null);
                    }
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
                AdapterContainerEntity::getResponsesEndpointPath,
                (entity, endpoints) -> {
                    AdapterContainerEntity targetContainer = entity.getAdapterContainer();
                    targetContainer.setContainerName(endpoints.containerName());
                    if (StringUtils.isNotBlank(targetContainer.getCompletionEndpointPath())) {
                        entity.setBaseEndpoint(endpoints.completionEndpoint());
                    } else {
                        entity.setBaseEndpoint(null);
                    }
                    if (StringUtils.isNotBlank(targetContainer.getResponsesEndpointPath())) {
                        entity.setResponsesEndpoint(endpoints.configurationEndpoint());
                    } else {
                        entity.setResponsesEndpoint(null);
                    }
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
                ModelContainerSource::getResponsesEndpointPath,
                (target, endpoints) -> {
                    ModelContainerSource targetSource = (ModelContainerSource) target.getSource();
                    targetSource.setContainerName(endpoints.containerName());
                    if (StringUtils.isNotBlank(targetSource.getCompletionEndpointPath())) {
                        target.setEndpoint(endpoints.completionEndpoint());
                    } else {
                        target.setEndpoint(null);
                    }
                    if (StringUtils.isNotBlank(targetSource.getResponsesEndpointPath())) {
                        target.setResponsesEndpoint(endpoints.configurationEndpoint());
                    } else {
                        target.setResponsesEndpoint(null);
                    }
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
                ModelContainerEntity::getResponsesEndpointPath,
                (entity, endpoints) -> {
                    ModelContainerEntity targetContainer = entity.getModelContainer();
                    targetContainer.setContainerName(endpoints.containerName());
                    if (StringUtils.isNotBlank(targetContainer.getCompletionEndpointPath())) {
                        entity.setEndpoint(endpoints.completionEndpoint());
                    } else {
                        entity.setEndpoint(null);
                    }
                    if (StringUtils.isNotBlank(targetContainer.getResponsesEndpointPath())) {
                        entity.setResponsesEndpoint(endpoints.configurationEndpoint());
                    } else {
                        entity.setResponsesEndpoint(null);
                    }
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

    public void processContainerEndpoints(Application application) {
        ApplicationContainerSource containerSource = (ApplicationContainerSource) application.getSource();
        processContainerEndpoints(
                containerSource.getContainerId(),
                containerSource,
                ApplicationContainerSource::getCompletionEndpointPath,
                ApplicationContainerSource::getMcpEndpointPath,
                (target, endpoints) -> {
                    ApplicationContainerSource targetSource = (ApplicationContainerSource) target.getSource();
                    targetSource.setContainerName(endpoints.containerName());
                    target.setEndpoint(endpoints.completionEndpoint());
                    boolean shouldSetMcp = (target.getMcp() != null
                            || StringUtils.isNotBlank(targetSource.getMcpEndpointPath()))
                            && endpoints.configurationEndpoint() != null;
                    if (shouldSetMcp) {
                        if (target.getMcp() == null) {
                            target.setMcp(new Mcp());
                        }
                        target.getMcp().setEndpoint(endpoints.configurationEndpoint());
                    }
                },
                application
        );
    }

    public void processContainerEndpoints(ApplicationEntity applicationEntity) {
        ApplicationContainerEntity container = applicationEntity.getApplicationContainer();
        processContainerEndpoints(
                container.getContainerId(),
                container,
                ApplicationContainerEntity::getCompletionEndpointPath,
                ApplicationContainerEntity::getMcpEndpointPath,
                (entity, endpoints) -> {
                    ApplicationContainerEntity targetContainer = entity.getApplicationContainer();
                    targetContainer.setContainerName(endpoints.containerName());
                    entity.setEndpoint(endpoints.completionEndpoint());
                    boolean shouldSetMcp = (entity.getMcp() != null
                            || StringUtils.isNotBlank(targetContainer.getMcpEndpointPath()))
                            && endpoints.configurationEndpoint() != null;
                    if (shouldSetMcp) {
                        McpEntity mcp = entity.getMcp();
                        if (mcp == null) {
                            mcp = new McpEntity();
                            entity.setMcp(mcp);
                        }
                        mcp.setEndpoint(endpoints.configurationEndpoint());
                    }
                },
                applicationEntity
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

    /**
     * Attempts to resolve container endpoints for the adapter.
     * If resolution fails (e.g. container URL is unavailable), retains the existing endpoints.
     */
    public void tryProcessContainerEndpoints(Adapter adapter, AdapterEntity existingEntity) {
        try {
            processContainerEndpoints(adapter);
        } catch (IllegalArgumentException e) {
            AdapterContainerSource source = (AdapterContainerSource) adapter.getSource();
            AdapterContainerEntity existingContainer = existingEntity.getAdapterContainer();
            log.warn("Failed to resolve container endpoints for adapter '{}', container '{}'. "
                    + "Retaining existing endpoints. Reason: {}",
                    adapter.getName(), source.getContainerId(), e.getMessage());
            adapter.setBaseEndpoint(existingEntity.getBaseEndpoint());
            adapter.setResponsesEndpoint(existingEntity.getResponsesEndpoint());
            source.setContainerName(existingContainer.getContainerName());
        }
    }

    /**
     * Attempts to resolve container endpoints for the application.
     * If resolution fails (e.g. container URL is unavailable), retains the existing endpoints.
     */
    public void tryProcessContainerEndpoints(Application application, ApplicationEntity existingEntity) {
        try {
            processContainerEndpoints(application);
        } catch (IllegalArgumentException e) {
            ApplicationContainerSource source = (ApplicationContainerSource) application.getSource();
            ApplicationContainerEntity existingContainer = existingEntity.getApplicationContainer();
            log.warn("Failed to resolve container endpoints for application '{}', container '{}'. "
                    + "Retaining existing endpoints. Reason: {}",
                    application.getDeployment().getName(), source.getContainerId(), e.getMessage());
            application.setEndpoint(existingEntity.getEndpoint());
            source.setContainerName(existingContainer.getContainerName());
            McpEntity existingMcp = existingEntity.getMcp();
            if (existingMcp != null && existingMcp.getEndpoint() != null) {
                if (application.getMcp() == null) {
                    application.setMcp(new Mcp());
                }
                application.getMcp().setEndpoint(existingMcp.getEndpoint());
            }
        }
    }

    /**
     * Attempts to resolve container endpoints for the model.
     * If resolution fails (e.g. container URL is unavailable), retains the existing endpoints.
     */
    public void tryProcessContainerEndpoints(Model model, ModelEntity existingEntity) {
        try {
            processContainerEndpoints(model);
        } catch (IllegalArgumentException e) {
            ModelContainerSource source = (ModelContainerSource) model.getSource();
            ModelContainerEntity existingContainer = existingEntity.getModelContainer();
            log.warn("Failed to resolve container endpoints for model '{}', container '{}'. "
                    + "Retaining existing endpoints. Reason: {}",
                    model.getDeployment().getName(), source.getContainerId(), e.getMessage());
            model.setEndpoint(existingEntity.getEndpoint());
            model.setResponsesEndpoint(existingEntity.getResponsesEndpoint());
            source.setContainerName(existingContainer.getContainerName());
        }
    }

    /**
     * Attempts to resolve container endpoints for the interceptor.
     * If resolution fails (e.g. container URL is unavailable), retains the existing endpoints.
     */
    public void tryProcessContainerEndpoints(Interceptor interceptor, InterceptorEntity existingEntity) {
        try {
            processContainerEndpoints(interceptor);
        } catch (IllegalArgumentException e) {
            InterceptorContainerSource source = (InterceptorContainerSource) interceptor.getSource();
            InterceptorContainerEntity existingContainer = existingEntity.getInterceptorContainer();
            log.warn("Failed to resolve container endpoints for interceptor '{}', container '{}'. "
                    + "Retaining existing endpoints. Reason: {}",
                    interceptor.getName(), source.getContainerId(), e.getMessage());
            interceptor.setEndpoint(existingEntity.getEndpoint());
            source.setContainerName(existingContainer.getContainerName());
            FeaturesEntity existingFeatures = existingEntity.getFeatures();
            if (existingFeatures != null) {
                Features features = Optional.ofNullable(interceptor.getFeatures()).orElse(new Features());
                interceptor.setFeatures(features);
                features.setConfigurationEndpoint(existingFeatures.getConfigurationEndpoint());
            }
        }
    }

    /**
     * Attempts to resolve container endpoints for the toolset.
     * If resolution fails (e.g. container URL is unavailable), retains the existing endpoints.
     */
    public void tryProcessContainerEndpoints(ToolSet toolSet, ToolSetEntity existingEntity) {
        try {
            processContainerEndpoints(toolSet);
        } catch (IllegalArgumentException e) {
            ToolSetContainerSource source = (ToolSetContainerSource) toolSet.getSource();
            ToolSetContainerEntity existingContainer = existingEntity.getToolSetContainer();
            log.warn("Failed to resolve container endpoints for toolset '{}', container '{}'. "
                    + "Retaining existing endpoints. Reason: {}",
                    toolSet.getDeployment().getName(), source.getContainerId(), e.getMessage());
            toolSet.setEndpoint(existingEntity.getEndpoint());
            source.setContainerName(existingContainer.getContainerName());
            if (existingEntity.getTransport() != null) {
                toolSet.setTransport(ToolSet.Transport.valueOf(existingEntity.getTransport().name()));
            }
        }
    }
}