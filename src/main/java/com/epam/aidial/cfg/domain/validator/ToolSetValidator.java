package com.epam.aidial.cfg.domain.validator;

import com.epam.aidial.cfg.client.dto.DeploymentInfoDto;
import com.epam.aidial.cfg.client.dto.McpDeploymentInfoDto;
import com.epam.aidial.cfg.domain.model.ToolSet;
import com.epam.aidial.cfg.domain.model.source.ToolSetContainerSource;
import com.epam.aidial.cfg.domain.model.source.ToolSetEndpointsSource;
import com.epam.aidial.cfg.domain.model.source.ToolSetSource;
import com.epam.aidial.cfg.domain.service.DeploymentManagerService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

@Slf4j
@Component
public class ToolSetValidator {

    private final DeploymentManagerService deploymentManagerService;
    private final DeploymentInfoValidator deploymentInfoValidator;
    private final DeploymentValidator deploymentValidator;
    private final DisplayFieldsValidator displayFieldsValidator;

    private final String toolSetNameValidationPattern;

    public ToolSetValidator(DeploymentManagerService deploymentManagerService,
                            DeploymentInfoValidator deploymentInfoValidator,
                            DeploymentValidator deploymentValidator,
                            DisplayFieldsValidator displayFieldsValidator, @Value("${validation.toolSet.name:}") String toolSetNameValidationPattern) {
        this.deploymentManagerService = deploymentManagerService;
        this.deploymentInfoValidator = deploymentInfoValidator;
        this.deploymentValidator = deploymentValidator;
        this.displayFieldsValidator = displayFieldsValidator;
        this.toolSetNameValidationPattern = toolSetNameValidationPattern;
    }

    public void validateCreation(ToolSet toolSet) {
        final String toolSetName = toolSet.getDeployment().getName();

        deploymentValidator.validateCreation("ToolSet", toolSetName);

        if (StringUtils.isEmpty(toolSetNameValidationPattern)) {
            log.debug("ToolSet name validation pattern is empty, skipping name pattern validation for ToolSet: {}", toolSetName);
        } else if (!Pattern.matches(toolSetNameValidationPattern, toolSetName)) {
            throw new IllegalArgumentException("toolSet name '" + toolSetName
                        + "' does not match the required pattern: " + toolSetNameValidationPattern);
        }
        displayFieldsValidator.validateDisplayName(toolSet.getDisplayName(), "ToolSet", toolSetName);
        validateToolSetSource(toolSet);
    }

    public void validateUpdate(String toolSetName, ToolSet toolSet) {
        deploymentValidator.validateUpdate(toolSetName, toolSet.getDeployment(), "ToolSet");
        displayFieldsValidator.validateDisplayName(toolSet.getDisplayName(), "ToolSet", toolSetName);
        validateToolSetSource(toolSet);
    }

    private void validateToolSetSource(ToolSet toolSet) {
        ToolSetSource source = toolSet.getSource();
        String name = toolSet.getDeployment().getName();

        if (source != null) {
            if (source instanceof ToolSetEndpointsSource) {
                validateEndpointsSource(toolSet);
            } else if (source instanceof ToolSetContainerSource containerSource) {
                validateContainerSource(containerSource, name);
            } else {
                throw new IllegalArgumentException(
                    "Unsupported toolset source: %s. Toolset: %s".formatted(source, toolSet.getDeployment().getName())
                );
            }
            return;
        }

        validateEndpoint(toolSet.getEndpoint(), name);
    }

    private void validateEndpointsSource(ToolSet toolSet) {
        String name = toolSet.getDeployment().getName();
        String endpoint = toolSet.getEndpoint();
        if (endpoint == null) {
            throw new IllegalArgumentException("Endpoint is required when source type is 'Toolset endpoints'. Toolset: %s"
                    .formatted(toolSet.getDeployment().getName()));
        }
        validateEndpoint(endpoint, name);
    }

    private void validateContainerSource(ToolSetContainerSource containerSource, String toolSetName) {
        String containerId = containerSource.getContainerId();
        DeploymentInfoDto deploymentInfo = deploymentManagerService.getById(containerId);
        deploymentInfoValidator.validateDeploymentInfo(deploymentInfo, containerId);
        McpDeploymentInfoDto mcpDeploymentInfoDto = validateDeploymentType(deploymentInfo, toolSetName);
        validateToolsetTransport(mcpDeploymentInfoDto, toolSetName);
        validateEndpointPath(containerSource.getCompletionEndpointPath(), toolSetName);
    }

    private void validateToolsetTransport(McpDeploymentInfoDto mcpDeploymentInfoDto, String toolSetName) {
        if (mcpDeploymentInfoDto.getTransport() == null) {
            log.warn("Toolset deployment transport is not defined. toolSetName: {}. mcpDeploymentInfoDto: {}",
                    toolSetName, mcpDeploymentInfoDto);
            throw new IllegalArgumentException("Toolset deployment transport is not defined. toolSetName: " + toolSetName);
        }
    }

    private McpDeploymentInfoDto validateDeploymentType(DeploymentInfoDto deploymentInfo, String toolSetName) {
        if (deploymentInfo instanceof McpDeploymentInfoDto mcpDeploymentInfoDto) {
            return mcpDeploymentInfoDto;
        }
        log.warn("Toolset deployment type must be mcp. toolSetName: {}. deploymentInfo: {}", toolSetName, deploymentInfo);
        throw new IllegalArgumentException("Toolset deployment type must be mcp. toolSetName: " + toolSetName);
    }

    private void validateEndpoint(String endpoint, String name) {
        if (endpoint != null && EndpointValidator.isInvalidUrl(endpoint)) {
            throw new IllegalArgumentException("Invalid endpoint: '%s'. Toolset: %s".formatted(endpoint, name));
        }
    }

    private void validateEndpointPath(String endpoint, String name) {
        if (StringUtils.isNotEmpty(endpoint) && EndpointValidator.isInvalidUrlPath(endpoint)) {
            throw new IllegalArgumentException("Invalid endpoint path: '%s'. Toolset: %s".formatted(endpoint, name));
        }
    }

}
