package com.epam.aidial.cfg.domain.validator;

import com.epam.aidial.cfg.domain.model.Features;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatNoException;

class FeaturesValidatorTest {

    private FeaturesValidator featuresValidator;

    @BeforeEach
    void setUp() {
        featuresValidator = new FeaturesValidator();
    }

    @Test
    void validate_allEndpointsAreValid_noException() {
        // given
        Features features = new Features();
        features.setRateEndpoint("http://rate");
        features.setTokenizeEndpoint("http://tokenize");
        features.setTruncatePromptEndpoint("http://truncate");
        features.setConfigurationEndpoint("http://config");

        // when/then
        assertThatNoException().isThrownBy(() -> featuresValidator.validate(features));
    }

    @Test
    void validate_allEndpointAreNulls_noException() {
        //given
        Features features = new Features();
        features.setRateEndpoint(null);
        features.setTokenizeEndpoint(null);
        features.setTruncatePromptEndpoint(null);
        features.setConfigurationEndpoint(null);

        // when/then
        assertThatNoException().isThrownBy(() -> featuresValidator.validate(features));
    }

    @Test
    void validate_invalidRateEndpoint_throwsException() {
        //given
        Features features = new Features();
        features.setRateEndpoint("invalid-rate");
        features.setTokenizeEndpoint("http://tokenize");
        features.setTruncatePromptEndpoint("http://truncate");
        features.setConfigurationEndpoint("http://config");

        // when/then
        Assertions.assertThatThrownBy(() -> featuresValidator.validate(features))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Invalid features rate endpoint: 'invalid-rate'");
    }

    @Test
    void validate_invalidTokenizeEndpoint_throwsException() {
        //given
        Features features = new Features();
        features.setRateEndpoint("http://rate");
        features.setTokenizeEndpoint("invalid-tokenize");
        features.setTruncatePromptEndpoint("http://truncate");
        features.setConfigurationEndpoint("http://config");

        // when/then
        Assertions.assertThatThrownBy(() -> featuresValidator.validate(features))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Invalid features tokenize endpoint: 'invalid-tokenize'");
    }

    @Test
    void validate_invalidTruncatePromptEndpoint_throwsException() {
        //given
        Features features = new Features();
        features.setRateEndpoint("http://rate");
        features.setTokenizeEndpoint("http://tokenize");
        features.setTruncatePromptEndpoint("invalid-truncate-prompt");
        features.setConfigurationEndpoint("http://config");

        // when/then
        Assertions.assertThatThrownBy(() -> featuresValidator.validate(features))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Invalid features truncate prompt endpoint: 'invalid-truncate-prompt'");
    }

    @Test
    void validate_invalidConfigurationEndpoint_throwsException() {
        //given
        Features features = new Features();
        features.setRateEndpoint("http://rate");
        features.setTokenizeEndpoint("http://tokenize");
        features.setTruncatePromptEndpoint("http://truncate");
        features.setConfigurationEndpoint("invalid-config");

        // when/then
        Assertions.assertThatThrownBy(() -> featuresValidator.validate(features))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Invalid features configuration endpoint: 'invalid-config'");
    }
}