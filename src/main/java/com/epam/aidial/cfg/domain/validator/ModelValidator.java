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
    private final FeaturesValidator featuresValidator;
    private final ModelEndpointUtils modelEndpointUtils;

    private final String modelNameValidationPattern;

    public ModelValidator(DeploymentManagerService deploymentManagerService,
                          DeploymentInfoValidator deploymentInfoValidator,
                          DisplayFieldsValidator displayFieldsValidator,
                          DeploymentValidator deploymentValidator,
                          FeaturesValidator featuresValidator,
                          ModelEndpointUtils modelEndpointUtils,
                          @Value("${validation.model.name:}") String modelNameValidationPattern) {
        this.deploymentManagerService = deploymentManagerService;
        this.deploymentInfoValidator = deploymentInfoValidator;
        this.displayFieldsValidator = displayFieldsValidator;
        this.deploymentValidator = deploymentValidator;
        this.featuresValidator = featuresValidator;
        this.modelEndpointUtils = modelEndpointUtils;
        this.modelNameValidationPattern = modelNameValidationPattern;
    }

    public void validateCreation(Model model) {
        validateModelName(model);
        validateDisplayNameDisplayVersion(model);
        validateModelSource(model);
        featuresValidator.validate(model.getFeatures());
    }

    public void validateUpdate(String modelName, Model model) {
        deploymentValidator.validateUpdate(modelName, model.getDeployment(), "Model");
        validateDisplayNameDisplayVersion(model);
        validateModelSource(model);
        featuresValidator.validate(model.getFeatures());
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

    private void validateDisplayNameDisplayVersion(Model model) {
        String modelName = model.getDeployment().getName();
        displayFieldsValidator.validateDisplayNameDisplayVersion(
                model.getDisplayName(),
                model.getDisplayVersion(),
                "Model",
                modelName
        );
    }

    private void validateModelSource(Model model) {
        ModelSource source = model.getSource();
        String modelName = model.getDeployment().getName();

        // Model source types are mutually exclusive: a model can have either AdapterSource,
        // ModelContainerSource, or ModelEndpointsSource, but not multiple sources simultaneously.
        // This is enforced by the type system (Model has a single 'source' field of type ModelSource).
        // Additional validation in ModelEntityMapper.toEntity() ensures adapter and container
        // are not both set at the entity level.
        if (source != null) {
            if (source instanceof ModelEndpointsSource) {
                validateEndpointsSource(model);
            } else if (source instanceof AdapterSource adapterSource) {
                validateAdapterSource(adapterSource, model);
            } else if (source instanceof ModelContainerSource containerSource) {
                validateContainerSource(containerSource, model);
            } else {
                throw new IllegalArgumentException(
                        "Unsupported model source: %s. Model: %s".formatted(source, model.getDeployment().getName())
                );
            }
            return;
        }

        validateEndpoint(model.getEndpoint(), modelName);
    }

    private void validateEndpointsSource(Model model) {
        String name = model.getDeployment().getName();
        String completionEndpoint = model.getEndpoint();
        validateEndpointEnding(model.getType(), completionEndpoint, name);
        validateEndpoint(completionEndpoint, name);
    }

    private void validateAdapterSource(AdapterSource adapterSource, Model model) {
        String name = model.getDeployment().getName();

        if (StringUtils.isBlank(adapterSource.getAdapterName())) {
            throw new IllegalArgumentException("Adapter name is required when source type is 'Adapter'. Model: %s"
                    .formatted(name));
        }

        String completionPath = adapterSource.getCompletionEndpointPath();
        validateEndpointEnding(model.getType(), completionPath, name);
        validateEndpointPath(completionPath, name);
    }

    private void validateContainerSource(ModelContainerSource containerSource, Model model) {
        String containerId = containerSource.getContainerId();
        DeploymentInfoDto deploymentInfo = deploymentManagerService.getById(containerId);
        deploymentInfoValidator.validateDeploymentInfo(deploymentInfo, containerId);

        String name = model.getDeployment().getName();
        String completionPath = containerSource.getCompletionEndpointPath();
        validateEndpointEnding(model.getType(), completionPath, name);
        // TODO: partial revert for https://github.com/epam/ai-dial-admin-backend/pull/547. will fix review env
        // validateEndpointPath(completionPath, name);
    }

    private void validateEndpointEnding(ModelType type, String endpoint, String modelName) {
        boolean isChat = modelEndpointUtils.isChat(type);
        String endpointEnding = ENDPOINT_ENDING_MAP.get(isChat);

        if (endpoint == null || !endpoint.endsWith(endpointEnding)) {
            throw new IllegalArgumentException("Completion endpoint path should be provided and end with '%s' when model type is '%s'. Model: %s"
                    .formatted(endpointEnding, type, modelName));
        }
    }

    private void validateEndpoint(String endpoint, String modelName) {
        if (endpoint != null && EndpointValidator.isInvalidUrl(endpoint)) {
            throw new IllegalArgumentException("Invalid completion endpoint: '%s'. Model: %s".formatted(endpoint, modelName));
        }
    }

    private void validateEndpointPath(String endpoint, String modelName) {
        if (endpoint != null && EndpointValidator.isInvalidUrlPath(endpoint)) {
            throw new IllegalArgumentException("Invalid completion endpoint path: '%s'. Model: %s".formatted(endpoint, modelName));
        }
    }
}