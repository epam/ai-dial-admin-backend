package com.epam.aidial.cfg.domain.validator;

import com.epam.aidial.cfg.domain.model.DeploymentInterface;
import com.epam.aidial.cfg.domain.model.Features;
import com.epam.aidial.cfg.domain.model.InterfaceMode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

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

    @ParameterizedTest
    @NullAndEmptySource
    void validate_noOverridePaths_passes(Map<String, String> overridePaths) {
        assertThatCode(() -> validate(Map.of("openaiResponses", withOverridePaths(overridePaths))))
                .doesNotThrowAnyException();
    }

    @Test
    void validate_supportedOverridePathTokens_passes() {
        Map<String, String> overridePaths = Map.of(
                "createResponse", "/v1/{overrideName}/responses",
                "getResponse", "/v1/{overrideName}/responses/{id}",
                "noTokens", "/v1/responses");

        assertThatCode(() -> validate(Map.of("openaiResponses", withOverridePaths(overridePaths))))
                .doesNotThrowAnyException();
    }

    @Test
    void validate_unknownOverridePathToken_throwsWithMappingInterfaceAndEntityContext() {
        Map<String, String> overridePaths = Map.of("getResponse", "/v1/{override_name}/responses/{id}");

        assertThatThrownBy(() -> validate(Map.of("openaiResponses", withOverridePaths(overridePaths))))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Unsupported token '{override_name}'")
                .hasMessageContaining("of mapping 'getResponse'")
                .hasMessageContaining("for interface 'openaiResponses'")
                .hasMessageContaining(ENTITY_KIND + ": " + ENTITY_NAME);
    }

    @Test
    void validate_emptyOverridePathToken_throws() {
        Map<String, String> overridePaths = Map.of("getResponse", "/v1/responses/{}");

        assertThatThrownBy(() -> validate(Map.of("openaiResponses", withOverridePaths(overridePaths))))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Unsupported token '{}'");
    }

    @Test
    void validate_nullOverridePathValue_passes() {
        Map<String, String> overridePaths = new HashMap<>();
        overridePaths.put("getResponse", null);

        assertThatCode(() -> validate(Map.of("openaiResponses", withOverridePaths(overridePaths))))
                .doesNotThrowAnyException();
    }

    @ParameterizedTest
    @ValueSource(strings = {"", " "})
    void validate_blankOverridePathValue_throws(String blankPath) {
        Map<String, String> overridePaths = Map.of("getResponse", blankPath);

        assertThatThrownBy(() -> validate(Map.of("openaiResponses", withOverridePaths(overridePaths))))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Override path is empty")
                .hasMessageContaining("for mapping 'getResponse'")
                .hasMessageContaining("for interface 'openaiResponses'")
                .hasMessageContaining(ENTITY_KIND + ": " + ENTITY_NAME);
    }

    @Test
    void validate_translatorModeWithOverridePaths_throws() {
        Map<String, String> overridePaths = Map.of("getResponse", "/v1/{overrideName}/responses/{id}");
        DeploymentInterface deploymentInterface = withOverridePaths(overridePaths);
        deploymentInterface.setMode(InterfaceMode.TRANSLATOR);

        assertThatThrownBy(() -> validate(Map.of("openaiResponses", deploymentInterface)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("overridePaths has no effect")
                .hasMessageContaining("for interface 'openaiResponses'")
                .hasMessageContaining("TRANSLATOR")
                .hasMessageContaining(ENTITY_KIND + ": " + ENTITY_NAME);
    }

    @ParameterizedTest
    @NullAndEmptySource
    void validate_translatorModeWithoutOverridePaths_passes(Map<String, String> overridePaths) {
        DeploymentInterface deploymentInterface = withOverridePaths(overridePaths);
        deploymentInterface.setMode(InterfaceMode.TRANSLATOR);

        assertThatCode(() -> validate(Map.of("openaiResponses", deploymentInterface)))
                .doesNotThrowAnyException();
    }

    @Test
    void validate_knownMappingKeyMatchingInterfaceAndIdUsage_passes() {
        Map<String, DeploymentInterface> interfaces = Map.of(
                "anthropicMessages",
                withOverridePaths(Map.of("postAnthropicMessages", "/v1/{overrideName}/messages")),
                "openaiResponses",
                withOverridePaths(Map.of("getOpenaiResponsesById", "/v1/{overrideName}/responses/{id}")));

        assertThatCode(() -> validate(interfaces)).doesNotThrowAnyException();
    }

    @Test
    void validate_knownMappingKeyOnWrongInterface_throws() {
        Map<String, String> overridePaths = Map.of("postAnthropicMessages", "/v1/{overrideName}/messages");

        assertThatThrownBy(() -> validate(Map.of("openaiResponses", withOverridePaths(overridePaths))))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Override path key 'postAnthropicMessages' belongs to interface 'anthropicMessages'")
                .hasMessageContaining("not 'openaiResponses'")
                .hasMessageContaining(ENTITY_KIND + ": " + ENTITY_NAME);
    }

    @Test
    void validate_knownMappingKeyWithoutIdUsingId_throws() {
        Map<String, String> overridePaths = Map.of("postAnthropicMessages", "/v1/{overrideName}/messages/{id}");

        assertThatThrownBy(() -> validate(Map.of("anthropicMessages", withOverridePaths(overridePaths))))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("references {id}")
                .hasMessageContaining("postAnthropicMessages")
                .hasMessageContaining("for interface 'anthropicMessages'")
                .hasMessageContaining(ENTITY_KIND + ": " + ENTITY_NAME);
    }

    @Test
    void validate_knownMappingKeyRequiringIdWithoutId_passes() {
        // {id} is optional even where idApplicable=true: the path just won't render an id-specific route
        Map<String, String> overridePaths = Map.of("getOpenaiResponsesById", "/v1/{overrideName}/responses");

        assertThatCode(() -> validate(Map.of("openaiResponses", withOverridePaths(overridePaths))))
                .doesNotThrowAnyException();
    }

    @Test
    void validate_unknownMappingKey_skipsMappingChecksEvenWithId() {
        // "getResponse" is not a real DIAL Core InterfacePathMapping key: it must be left unvalidated,
        // proving unknown/future keys are tolerated rather than rejected by a whitelist.
        Map<String, String> overridePaths = Map.of("getResponse", "/v1/{overrideName}/responses/{id}");

        assertThatCode(() -> validate(Map.of("anthropicMessages", withOverridePaths(overridePaths))))
                .doesNotThrowAnyException();
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

    private static DeploymentInterface withOverridePaths(Map<String, String> overridePaths) {
        DeploymentInterface deploymentInterface = deploymentInterface("http://adapter", null);
        deploymentInterface.setOverridePaths(overridePaths);
        return deploymentInterface;
    }
}
