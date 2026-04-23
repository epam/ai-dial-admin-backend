package com.epam.aidial.cfg.domain.validator;

import com.epam.aidial.cfg.domain.model.Adapter;
import com.epam.aidial.cfg.domain.model.source.AdapterContainerSource;
import com.epam.aidial.cfg.domain.model.source.AdapterEndpointsSource;
import com.epam.aidial.cfg.domain.model.source.AdapterSource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.regex.Pattern;

@Slf4j
@Component
public class AdapterValidator {

    private final IdFieldValidator idFieldValidator;
    private final DisplayFieldsValidator displayFieldsValidator;
    private final String adapterNameValidationPattern;

    public AdapterValidator(IdFieldValidator idFieldValidator,
                            DisplayFieldsValidator displayFieldsValidator,
                            @Value("${validation.adapter.name:}") String adapterNameValidationPattern) {
        this.idFieldValidator = idFieldValidator;
        this.displayFieldsValidator = displayFieldsValidator;
        this.adapterNameValidationPattern = adapterNameValidationPattern;
    }

    public void validateCreation(Adapter adapter) {
        final String adapterName = adapter.getName();
        idFieldValidator.validateName("Adapter", adapterName);

        if (StringUtils.isEmpty(adapterNameValidationPattern)) {
            log.debug("Adapter name validation pattern is empty, skipping validation for adapter: {}", adapterName);
        } else if (!Pattern.matches(adapterNameValidationPattern, adapterName)) {
            throw new IllegalArgumentException("Adapter name '" + adapterName
                    + "' does not match the required pattern: " + adapterNameValidationPattern);
        }
        displayFieldsValidator.validateDisplayName(adapter.getDisplayName(), "Adapter", adapterName);
        validateAdapterSource(adapter);
    }

    public void validateUpdate(String adapterName, Adapter adapter) {
        if (!Objects.equals(adapterName, adapter.getName())) {
            throw new IllegalArgumentException("Adapter with name: '" + adapterName + "' can not be renamed. New adapter name: '" + adapter.getName() + "'");
        }
        displayFieldsValidator.validateDisplayName(adapter.getDisplayName(), "Adapter", adapterName);
        validateAdapterSource(adapter);
    }

    private void validateAdapterSource(Adapter adapter) {
        AdapterSource source = adapter.getSource();
        String adapterName = adapter.getName();
        String baseEndpoint = adapter.getBaseEndpoint();

        if (source != null) {
            if (source instanceof AdapterEndpointsSource) {
                validateEndpointsSource(baseEndpoint, adapterName);
            } else if (source instanceof AdapterContainerSource containerSource) {
                validateContainerSource(containerSource, adapterName);
            } else {
                throw new IllegalArgumentException(
                        "Unsupported adapter source: %s. Adapter: %s".formatted(source, adapterName)
                );
            }
            return;
        }

        validateBaseEndpoint(baseEndpoint, adapterName);
    }

    private void validateEndpointsSource(String baseEndpoint, String adapterName) {
        if (baseEndpoint == null) {
            throw new IllegalArgumentException("Base endpoint is required when source type is 'Adapter endpoints'. Adapter: %s"
                    .formatted(adapterName));
        }
        validateBaseEndpoint(baseEndpoint, adapterName);
    }

    private void validateContainerSource(AdapterContainerSource containerSource, String adapterName) {
        validateEndpointPath(containerSource.getCompletionEndpointPath(), adapterName);
    }

    private void validateBaseEndpoint(String baseEndpoint, String adapterName) {
        if (baseEndpoint != null && EndpointValidator.isInvalidUrl(baseEndpoint)) {
            throw new IllegalArgumentException("Invalid base endpoint: '%s'. Adapter: %s".formatted(baseEndpoint, adapterName));
        }
    }

    private void validateEndpointPath(String endpoint, String adapterName) {
        if (StringUtils.isNotEmpty(endpoint) && EndpointValidator.isInvalidUrlPath(endpoint)) {
            throw new IllegalArgumentException("Invalid endpoint path: '%s'. Adapter: %s".formatted(endpoint, adapterName));
        }
    }
}
