package com.epam.aidial.cfg.domain.validator;

import com.epam.aidial.cfg.domain.model.DeploymentInterface;
import com.epam.aidial.cfg.domain.model.Features;
import com.epam.aidial.cfg.domain.model.InterfaceMode;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Validates the {@code interfaces} map of a deployment. Unlike DIAL Core, which silently drops
 * unsupported interface types on config read, the admin backend rejects them to fail fast
 * at authoring time.
 */
@Component
public class DeploymentInterfacesValidator {

    private static final Pattern PATH_TOKEN = Pattern.compile("\\{([^}]*)}");
    private static final Set<String> SUPPORTED_PATH_TOKENS = Set.of("id", "overrideName");

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
            validateOverridePaths(deploymentInterface.getOverridePaths(), deploymentInterface.getMode(),
                    type, entityKind, entityName);
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

    /**
     * Rejects any {@code {token}} DIAL Core does not substitute. Core renders only {@code {id}} and
     * {@code {overrideName}} and forwards every other character as literal path text, so a typo such as
     * {@code {override_name}} would silently become part of the upstream path. Keys are not checked: the
     * set of interface path mappings is owned by Core and grows independently of the admin backend.
     *
     * <p>Also rejects a blank override path value (Core treats an empty template as invalid), and
     * rejects {@code overridePaths} declared on an interface in {@link InterfaceMode#TRANSLATOR} mode,
     * since Core ignores {@code overridePaths} entirely for translated interfaces.
     */
    private void validateOverridePaths(Map<String, String> overridePaths, InterfaceMode mode, String type,
                                       String entityKind, String entityName) {
        if (MapUtils.isEmpty(overridePaths)) {
            return;
        }

        if (mode == InterfaceMode.TRANSLATOR) {
            throw new IllegalArgumentException(
                    "overridePaths has no effect for interface '%s' in TRANSLATOR mode. %s: %s"
                            .formatted(type, entityKind, entityName));
        }

        overridePaths.forEach((mapping, path) -> {
            if (path == null) {
                return;
            }
            if (StringUtils.isBlank(path)) {
                throw new IllegalArgumentException(
                        "Override path is empty for mapping '%s' for interface '%s'. %s: %s"
                                .formatted(mapping, type, entityKind, entityName));
            }
            Matcher matcher = PATH_TOKEN.matcher(path);
            while (matcher.find()) {
                String token = matcher.group(1);
                if (!SUPPORTED_PATH_TOKENS.contains(token)) {
                    throw new IllegalArgumentException(
                            ("Unsupported token '{%s}' in override path '%s' of mapping '%s' for interface '%s'. "
                                    + "Supported tokens: {id}, {overrideName}. %s: %s")
                                    .formatted(token, path, mapping, type, entityKind, entityName));
                }
            }
        });
    }
}
