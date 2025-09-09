package com.epam.aidial.cfg.domain.util;

import com.epam.aidial.cfg.client.dto.DeploymentInfoDto;
import com.epam.aidial.cfg.dao.model.FeaturesEntity;
import com.epam.aidial.cfg.dao.model.InterceptorContainerEntity;
import com.epam.aidial.cfg.dao.model.InterceptorEntity;
import com.epam.aidial.cfg.dao.model.ModelContainerEntity;
import com.epam.aidial.cfg.dao.model.ModelEntity;
import com.epam.aidial.cfg.domain.model.Features;
import com.epam.aidial.cfg.domain.model.Interceptor;
import com.epam.aidial.cfg.domain.model.Model;
import com.epam.aidial.cfg.domain.model.source.InterceptorContainerSource;
import com.epam.aidial.cfg.domain.model.source.ModelContainerSource;
import com.epam.aidial.cfg.domain.service.DeploymentManagerService;
import com.epam.aidial.cfg.domain.validator.DeploymentInfoValidator;
import lombok.RequiredArgsConstructor;
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
public class ContainerEndpointResolver {

    private final DeploymentManagerService deploymentManagerService;
    private final DeploymentInfoValidator deploymentInfoValidator;

    public void processContainerEndpoints(Model model) {
        ModelContainerSource containerSource = (ModelContainerSource) model.getSource();
        processContainerEndpoints(
                containerSource.getContainerId(),
                containerSource,
                ModelContainerSource::getCompletionEndpointPath,
                null,
                (target, endpoints) -> target.setEndpoint(endpoints[0]),
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
                (entity, endpoints) -> entity.setEndpoint(endpoints[0]),
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
                    target.setEndpoint(endpoints[0]);
                    Features features = Optional.ofNullable(target.getFeatures()).orElse(new Features());
                    target.setFeatures(features);
                    features.setConfigurationEndpoint(endpoints[1]);
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
                    entity.setEndpoint(endpoints[0]);
                    FeaturesEntity features = Optional.ofNullable(entity.getFeatures()).orElse(new FeaturesEntity());
                    features.setConfigurationEndpoint(endpoints[1]);
                },
                interceptorEntity
        );
    }

    /**
     * Processes container endpoints and applies them using the provided consumer.
     * This method handles the common pattern of:
     * 1. Getting deployment info
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
            BiConsumer<R, String[]> endpointConsumer,
            R target) {
        
        DeploymentInfoDto deploymentInfo = deploymentManagerService.getById(containerId);
        deploymentInfoValidator.validateDeploymentInfo(deploymentInfo, containerId);
        
        String containerUrl = deploymentInfo.getUrl();
        String completionPath = completionPathExtractor.apply(pathProvider);
        String configPath = configPathExtractor != null ? configPathExtractor.apply(pathProvider) : null;
        
        String[] endpoints = resolveEndpoints(containerUrl, completionPath, configPath);
        endpointConsumer.accept(target, endpoints);
    }

    /**
     * Resolves endpoints based on container URL and endpoint paths.
     *
     * @param containerUrl the base URL of the container
     * @param completionEndpointPath the path for the completion endpoint
     * @param configurationEndpointPath the path for the configuration endpoint
     * @return array containing [completionEndpoint, configurationEndpoint]
     */
    private static String[] resolveEndpoints(
            String containerUrl,
            String completionEndpointPath,
            String configurationEndpointPath) {
        String completionEndpoint = resolveEndpoint(containerUrl, completionEndpointPath);
        String configurationEndpoint = resolveEndpoint(containerUrl, configurationEndpointPath);
        return new String[]{completionEndpoint, configurationEndpoint};
    }

    /**
     * Resolves a single endpoint by combining URL and path.
     *
     * @param url the base URL
     * @param path the endpoint path
     * @return the complete endpoint URL
     */
    private static String resolveEndpoint(final String url, final String path) {
        return url + Optional.ofNullable(path).orElse("");
    }
}