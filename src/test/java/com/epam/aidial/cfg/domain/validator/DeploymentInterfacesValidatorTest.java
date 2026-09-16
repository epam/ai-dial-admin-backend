package com.epam.aidial.cfg.domain.validator;

import com.epam.aidial.cfg.domain.model.DeploymentInterface;
import com.epam.aidial.cfg.domain.model.Features;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DeploymentInterfacesValidatorTest {

    private static final Set<String> ALLOWED_TYPES =
            Set.of("openaiChatCompletions", "openaiResponses", "anthropicMessages");
    private static final String ENTITY_KIND = "Model";
    private static final String ENTITY_NAME = "gpt-4";

    private DeploymentInterfacesValidator validator;

    @BeforeEach
    void setUp() {
        validator = new DeploymentInterfacesValidator(new FeaturesValidator());
    }

    @ParameterizedTest
    @NullAndEmptySource
    void validate_noInterfaces_passes(Map<String, DeploymentInterface> interfaces) {
        assertThatCode(() -> validate(interfaces)).doesNotThrowAnyException();
    }

    @Test
    void validate_unsupportedInterfaceType_throws() {
        assertThatThrownBy(() -> validate(Map.of("anthropicResponses", deploymentInterface("http://adapter", null))))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Unsupported interface type 'anthropicResponses'")
                .hasMessageContaining(ENTITY_NAME);
    }

    @Test
    void validate_invalidBaseUrl_throws() {
        assertThatThrownBy(() -> validate(Map.of("openaiChatCompletions", deploymentInterface("not a url", null))))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid base URL 'not a url' for interface 'openaiChatCompletions'")
                .hasMessageContaining(ENTITY_NAME);
    }

    @Test
    void validate_blankBaseUrl_passes() {
        assertThatCode(() -> validate(Map.of("openaiChatCompletions", deploymentInterface(" ", null))))
                .doesNotThrowAnyException();
    }

    @Test
    void validate_nullInterfaceValue_passes() {
        Map<String, DeploymentInterface> interfaces = new HashMap<>();
        interfaces.put("openaiChatCompletions", null);

        assertThatCode(() -> validate(interfaces)).doesNotThrowAnyException();
    }

    @Test
    void validate_nullInterfaceFeatures_passes() {
        assertThatCode(() -> validate(Map.of("openaiChatCompletions", deploymentInterface("http://adapter", null))))
                .doesNotThrowAnyException();
    }

    @Test
    void validate_validInterfaceFeatures_passes() {
        Features features = new Features();
        features.setRateEndpoint("http://adapter/rate");
        features.setTokenizeEndpoint("http://adapter/tokenize");

        assertThatCode(() -> validate(Map.of("openaiChatCompletions", deploymentInterface("http://adapter", features))))
                .doesNotThrowAnyException();
    }

    @Test
    void validate_invalidInterfaceFeatureEndpoint_throwsWithInterfaceAndEntityContext() {
        Features features = new Features();
        features.setRateEndpoint("not a url");

        assertThatThrownBy(() -> validate(Map.of("anthropicMessages", deploymentInterface("http://adapter", features))))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid features rate endpoint: 'not a url'")
                .hasMessageContaining("for interface 'anthropicMessages'")
                .hasMessageContaining(ENTITY_KIND + ": " + ENTITY_NAME);
    }

    @Test
    void validate_invalidInterfaceFeatureEndpointOnInterfaceWithoutBaseUrl_stillThrows() {
        // a blank baseUrl short-circuits the URL check, but must not skip feature validation
        Features features = new Features();
        features.setConfigurationEndpoint("not a url");

        assertThatThrownBy(() -> validate(Map.of("openaiResponses", deploymentInterface(null, features))))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid features configuration endpoint: 'not a url'")
                .hasMessageContaining("for interface 'openaiResponses'");
    }

    private void validate(Map<String, DeploymentInterface> interfaces) {
        validator.validate(interfaces, ALLOWED_TYPES, ENTITY_KIND, ENTITY_NAME);
    }

    private static DeploymentInterface deploymentInterface(String baseUrl, Features features) {
        DeploymentInterface deploymentInterface = new DeploymentInterface();
        deploymentInterface.setBaseUrl(baseUrl);
        deploymentInterface.setFeatures(features);
        return deploymentInterface;
    }
}
