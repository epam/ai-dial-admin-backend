package com.epam.aidial.cfg.domain.validator;

import com.epam.aidial.cfg.domain.model.DeploymentInterface;
import com.epam.aidial.cfg.domain.model.Features;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;

/**
 * Validates the {@code interfaces} map of a deployment. Unlike DIAL Core, which silently drops
 * unsupported interface types on config read, the admin backend rejects them to fail fast
 * at authoring time.
 */
@Component
public class DeploymentInterfacesValidator {

    private final FeaturesValidator featuresValidator;

    public DeploymentInterfacesValidator(FeaturesValidator featuresValidator) {
        this.featuresValidator = featuresValidator;
    }

    public void validate(Map<String, DeploymentInterface> interfaces,
                         Set<String> allowedTypes,
                         String entityKind,
                         String entityName) {
        if (MapUtils.isEmpty(interfaces)) {
            return;
        }

        interfaces.forEach((type, deploymentInterface) -> {
            if (!allowedTypes.contains(type)) {
                throw new IllegalArgumentException(
                        "Unsupported interface type '%s'. %s supports: %s. %s: %s"
                                .formatted(type, entityKind, String.join(", ", allowedTypes.stream().sorted().toList()),
                                        entityKind, entityName));
            }
            if (deploymentInterface == null) {
                return;
            }
            validateBaseUrl(deploymentInterface.getBaseUrl(), type, entityKind, entityName);
            validateFeatures(deploymentInterface.getFeatures(), type, entityKind, entityName);
        });
    }

    private void validateBaseUrl(String baseUrl, String type, String entityKind, String entityName) {
        if (StringUtils.isBlank(baseUrl)) {
            return;
        }
        if (EndpointValidator.isInvalidUrl(baseUrl)) {
            throw new IllegalArgumentException(
                    "Invalid base URL '%s' for interface '%s'. %s: %s"
                            .formatted(baseUrl, type, entityKind, entityName));
        }
    }

    /**
     * Reuses {@link FeaturesValidator}, which validates the four feature endpoints, and adds the
     * interface type and owning entity to its message so an authoring error points at the right entry.
     */
    private void validateFeatures(Features features, String type, String entityKind, String entityName) {
        try {
            featuresValidator.validate(features);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(
                    "%s for interface '%s'. %s: %s".formatted(e.getMessage(), type, entityKind, entityName), e);
        }
    }
}
