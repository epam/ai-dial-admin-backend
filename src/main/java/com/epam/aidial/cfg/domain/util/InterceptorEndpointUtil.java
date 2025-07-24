package com.epam.aidial.cfg.domain.util;

import com.epam.aidial.cfg.client.dto.DeploymentInfoDto;
import com.epam.aidial.cfg.domain.service.ExternalDeploymentService;
import com.epam.aidial.cfg.domain.validator.DeploymentInfoValidator;
import lombok.experimental.UtilityClass;

import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * Utility class for handling interceptor endpoint operations.
 */
@UtilityClass
public final class InterceptorEndpointUtil {

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
     * @param deploymentService service to get deployment info
     * @param deploymentInfoValidator validator for deployment info
     * @param containerId the container ID
     * @param pathProvider object containing endpoint paths
     * @param completionPathExtractor function to extract completion path from pathProvider
     * @param configPathExtractor function to extract configuration path from pathProvider
     * @param endpointConsumer consumer to set the resolved endpoints on target object
     * @param target the object to receive the resolved endpoints
     */
    public static <T, R> void processContainerEndpoints(
            ExternalDeploymentService deploymentService,
            DeploymentInfoValidator deploymentInfoValidator,
            String containerId,
            T pathProvider,
            Function<T, String> completionPathExtractor,
            Function<T, String> configPathExtractor,
            BiConsumer<R, String[]> endpointConsumer,
            R target) {
        
        DeploymentInfoDto deploymentInfo = deploymentService.getById(containerId);
        deploymentInfoValidator.validateDeploymentInfo(deploymentInfo, containerId);
        
        String containerUrl = deploymentInfo.getUrl();
        String completionPath = completionPathExtractor.apply(pathProvider);
        String configPath = configPathExtractor.apply(pathProvider);
        
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
    public static String[] resolveEndpoints(
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
    public static String resolveEndpoint(final String url, final String path) {
        return url + Optional.ofNullable(path).orElse("");
    }
}