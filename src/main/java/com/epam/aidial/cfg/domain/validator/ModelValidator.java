package com.epam.aidial.cfg.domain.validator;

import com.epam.aidial.cfg.client.dto.DeploymentInfoDto;
import com.epam.aidial.cfg.domain.model.Model;
import com.epam.aidial.cfg.domain.model.ModelType;
import com.epam.aidial.cfg.domain.model.source.AdapterSource;
import com.epam.aidial.cfg.domain.model.source.ModelContainerSource;
import com.epam.aidial.cfg.domain.model.source.ModelEndpointsSource;
import com.epam.aidial.cfg.domain.model.source.ModelSource;
import com.epam.aidial.cfg.domain.service.DeploymentManagerService;
import com.epam.aidial.cfg.domain.utils.ModelEndpointUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.regex.Pattern;

@Slf4j
@Component
public class ModelValidator {

    private static final Map<Boolean, String> ENDPOINT_ENDING_MAP = Map.of(
            true, "chat/completions",
            false, "embeddings"
    );

    private final DeploymentManagerService deploymentManagerService;
    private final DeploymentInfoValidator deploymentInfoValidator;
    private final DisplayFieldsValidator displayFieldsValidator;
    private final DeploymentValidator deploymentValidator;
    private final ModelEndpointUtils modelEndpointUtils;

    private final String modelNameValidationPattern;

    public ModelValidator(DeploymentManagerService deploymentManagerService,
                          DeploymentInfoValidator deploymentInfoValidator,
                          DisplayFieldsValidator displayFieldsValidator,
                          DeploymentValidator deploymentValidator,
                          ModelEndpointUtils modelEndpointUtils,
                          @Value("${validation.model.name:}") String modelNameValidationPattern) {
        this.deploymentManagerService = deploymentManagerService;
        this.deploymentInfoValidator = deploymentInfoValidator;
        this.displayFieldsValidator = displayFieldsValidator;
        this.deploymentValidator = deploymentValidator;
        this.modelEndpointUtils = modelEndpointUtils;
        this.modelNameValidationPattern = modelNameValidationPattern;
    }

    public void validateCreation(Model model) {
        validateModelName(model);
        displayFieldsValidator.validateDisplayNameDisplayVersion(model.getDisplayName(), model.getDisplayVersion());
        validateModelSource(model);
    }

    public void validateUpdate(String modelName, Model model) {
        deploymentValidator.validateUpdate(modelName, model.getDeployment(), "Model");
        displayFieldsValidator.validateDisplayNameDisplayVersion(model.getDisplayName(), model.getDisplayVersion());
        validateModelSource(model);
    }

    private void validateModelName(Model model) {
        final String modelName = model.getDeployment().getName();

        deploymentValidator.validateCreation("Model", modelName);

        if (StringUtils.isEmpty(modelNameValidationPattern)) {
            log.debug("Model name validation pattern is empty, skipping validation for model: {}", modelName);
            return;
        }

        if (!Pattern.matches(modelNameValidationPattern, modelName)) {
            throw new IllegalArgumentException("Model name '" + modelName
                    + "' does not match the required pattern: " + modelNameValidationPattern);
        }
    }

    private void validateModelSource(Model model) {
        ModelSource source = model.getSource();
        String endpoint = model.getEndpoint();

        if (source != null) {
            if (source instanceof ModelEndpointsSource) {
                validateEndpointsSource(endpoint);
            } else if (source instanceof AdapterSource adapterSource) {
                validateAdapterSource(adapterSource, model.getType());
            } else if (source instanceof ModelContainerSource containerSource) {
                validateContainerSource(containerSource);
            } else {
                throw new IllegalArgumentException(
                    "Unsupported model source: %s. Model: %s".formatted(source, model.getDeployment().getName())
                );
            }
            return;
        }

        validateEndpoint(endpoint);
    }

    private void validateEndpointsSource(String completionEndpoint) {
        if (completionEndpoint == null) {
            throw new IllegalArgumentException("Completion endpoint is required when source type is 'Model endpoints'");
        }
        validateEndpoint(completionEndpoint);
    }

    private void validateAdapterSource(AdapterSource adapterSource, ModelType type) {
        if (StringUtils.isBlank(adapterSource.getAdapterName())) {
            throw new IllegalArgumentException("Adapter name is required when source type is 'Adapter'");
        }

        boolean isChat = modelEndpointUtils.isChat(type);
        String endpointEnding = ENDPOINT_ENDING_MAP.get(isChat);
        String completionEndpointPath = adapterSource.getCompletionEndpointPath();

        if (completionEndpointPath == null || !completionEndpointPath.endsWith(endpointEnding)) {
            throw new IllegalArgumentException("Completion endpoint path should be provided and end with '%s' when model type is '%s'"
                    .formatted(endpointEnding, type));
        }
    }

    private void validateContainerSource(ModelContainerSource containerSource) {
        String containerId = containerSource.getContainerId();
        DeploymentInfoDto deploymentInfo = deploymentManagerService.getById(containerId);
        deploymentInfoValidator.validateDeploymentInfo(deploymentInfo, containerId);

        validateEndpointPath(containerSource.getCompletionEndpointPath());
    }

    private void validateEndpoint(String endpoint) {
        if (endpoint != null && EndpointValidator.isInvalidUrl(endpoint)) {
            throw new IllegalArgumentException("Invalid completion endpoint: '%s'".formatted(endpoint));
        }
    }

    private void validateEndpointPath(String endpoint) {
        if (endpoint != null && EndpointValidator.isInvalidUrlPath(endpoint)) {
            throw new IllegalArgumentException("Invalid completion endpoint path: '%s'".formatted(endpoint));
        }
    }
}
