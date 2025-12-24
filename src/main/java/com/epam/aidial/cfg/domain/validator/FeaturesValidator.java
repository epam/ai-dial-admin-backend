package com.epam.aidial.cfg.domain.validator;

import com.epam.aidial.cfg.domain.model.Features;
import org.springframework.stereotype.Component;

@Component
public class FeaturesValidator {

    private static final String INVALID_ENDPOINT_MESSAGE_TEMPLATE = "Invalid features %s endpoint: '%s'";

    public void validate(Features features) {
        validateEndpoints(features);
    }

    private void validateEndpoints(Features features) {
        String rateEndpoint = features.getRateEndpoint();
        if (isInvalidEndpoint(rateEndpoint)) {
            throw new IllegalArgumentException(INVALID_ENDPOINT_MESSAGE_TEMPLATE.formatted("rate", rateEndpoint));
        }

        String tokenizeEndpoint = features.getTokenizeEndpoint();
        if (isInvalidEndpoint(tokenizeEndpoint)) {
            throw new IllegalArgumentException(INVALID_ENDPOINT_MESSAGE_TEMPLATE.formatted("tokenize", tokenizeEndpoint));
        }

        String truncatePromptEndpoint = features.getTruncatePromptEndpoint();
        if (isInvalidEndpoint(truncatePromptEndpoint)) {
            throw new IllegalArgumentException(INVALID_ENDPOINT_MESSAGE_TEMPLATE.formatted("truncate prompt", truncatePromptEndpoint));
        }

        String configurationEndpoint = features.getConfigurationEndpoint();
        if (isInvalidEndpoint(configurationEndpoint)) {
            throw new IllegalArgumentException(INVALID_ENDPOINT_MESSAGE_TEMPLATE.formatted("configuration", configurationEndpoint));
        }
    }

    private boolean isInvalidEndpoint(String endpoint) {
        return endpoint != null && EndpointValidator.isInvalidUrl(endpoint);
    }
}
