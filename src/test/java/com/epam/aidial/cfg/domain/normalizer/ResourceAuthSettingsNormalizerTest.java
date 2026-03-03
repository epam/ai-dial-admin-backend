package com.epam.aidial.cfg.domain.normalizer;

import com.epam.aidial.cfg.domain.model.ResourceAuthSettings;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ResourceAuthSettingsNormalizerTest {

    private ResourceAuthSettingsNormalizer normalizer;

    @BeforeEach
    void setUp() {
        normalizer = new ResourceAuthSettingsNormalizer();
    }

    @Test
    void normalize_shouldDoNothingWhenSettingsIsNull() {
        assertThatNoException()
                .isThrownBy(() -> normalizer.normalize(null));
    }

    @ParameterizedTest
    @CsvSource(value = {"null", "''"}, nullValues = "null")
    void normalize_shouldDoNothingWhenCodeChallengeMethodIsEmptyAndCodeVerifierAndCodeChallengeAreNotProvided(String codeChallengeMethod) {
        ResourceAuthSettings resourceAuthSettings = new ResourceAuthSettings();
        resourceAuthSettings.setCodeChallengeMethod(codeChallengeMethod);

        normalizer.normalize(resourceAuthSettings);

        Assertions.assertThat(resourceAuthSettings.getCodeVerifier()).isNull();
        Assertions.assertThat(resourceAuthSettings.getCodeChallenge()).isNull();
    }

    @ParameterizedTest
    @CsvSource(value = {"null", "''"}, nullValues = "null")
    void normalize_shouldThrowExceptionWhenCodeChallengeMethodIsEmptyAndCodeVerifierIsProvided(String codeChallengeMethod) {
        ResourceAuthSettings resourceAuthSettings = new ResourceAuthSettings();
        resourceAuthSettings.setCodeChallengeMethod(codeChallengeMethod);
        resourceAuthSettings.setCodeVerifier("43_characters_verifier_____________________");

        assertThatThrownBy(() -> normalizer.normalize(resourceAuthSettings))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Both code_verifier and code_challenge must not be provided when empty code_challenge_method");
    }

    @ParameterizedTest
    @CsvSource(value = {"null", "''"}, nullValues = "null")
    void normalize_shouldThrowExceptionWhenCodeChallengeMethodIsEmptyAndCodeChallengeIsProvided(String codeChallengeMethod) {
        ResourceAuthSettings resourceAuthSettings = new ResourceAuthSettings();
        resourceAuthSettings.setCodeChallengeMethod(codeChallengeMethod);
        resourceAuthSettings.setCodeChallenge("code-challenge");

        assertThatThrownBy(() -> normalizer.normalize(resourceAuthSettings))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Both code_verifier and code_challenge must not be provided when empty code_challenge_method");
    }

    @Test
    void normalize_shouldThrowExceptionWhenCodeVerifierAndCodeChallengeDoNotMatch() {
        ResourceAuthSettings resourceAuthSettings = new ResourceAuthSettings();
        resourceAuthSettings.setCodeChallengeMethod("plain");
        resourceAuthSettings.setCodeVerifier("43_characters_verifier_____________________");
        resourceAuthSettings.setCodeChallenge("wrong-challenge");

        assertThatThrownBy(() -> normalizer.normalize(resourceAuthSettings))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Provided code_challenge does not match the one computed from the code_verifier.");
    }

    @Test
    void normalize_shouldThrowExceptionWhenOnlyCodeVerifierIsProvided() {
        ResourceAuthSettings resourceAuthSettings = new ResourceAuthSettings();
        resourceAuthSettings.setCodeChallengeMethod("plain");
        resourceAuthSettings.setCodeVerifier("verifier");

        assertThatThrownBy(() -> normalizer.normalize(resourceAuthSettings))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Both code_verifier and code_challenge must be provided, or neither.");
    }

    @Test
    void normalize_shouldThrowExceptionWhenOnlyCodeChallengeIsProvided() {
        ResourceAuthSettings resourceAuthSettings = new ResourceAuthSettings();
        resourceAuthSettings.setCodeChallengeMethod("plain");
        resourceAuthSettings.setCodeChallenge("challenge");

        assertThatThrownBy(() -> normalizer.normalize(resourceAuthSettings))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Both code_verifier and code_challenge must be provided, or neither.");
    }

    @Test
    void normalize_shouldDoNothingWhenValidPkcePairIsProvided() {
        ResourceAuthSettings resourceAuthSettings = new ResourceAuthSettings();
        resourceAuthSettings.setCodeChallengeMethod("S256");
        resourceAuthSettings.setCodeVerifier("dBjftJeZ4CVP-mB92K27uhbUJU1p1r_wW1gFWFOEjXk");
        // corresponding challenge
        resourceAuthSettings.setCodeChallenge("E9Melhoa2OwvFrEMTJguCHaoeK1t8URWbuGJSstw-cM");

        normalizer.normalize(resourceAuthSettings);

        Assertions.assertThat(resourceAuthSettings.getCodeVerifier()).isEqualTo("dBjftJeZ4CVP-mB92K27uhbUJU1p1r_wW1gFWFOEjXk");
        Assertions.assertThat(resourceAuthSettings.getCodeChallenge()).isEqualTo("E9Melhoa2OwvFrEMTJguCHaoeK1t8URWbuGJSstw-cM");
    }

    @Test
    void normalize_shouldSetNewPkcePairWhenPkcePairIsNotProvided() {
        ResourceAuthSettings resourceAuthSettings = new ResourceAuthSettings();
        resourceAuthSettings.setCodeChallengeMethod("S256");

        normalizer.normalize(resourceAuthSettings);

        Assertions.assertThat(resourceAuthSettings.getCodeVerifier()).isNotNull();
        Assertions.assertThat(resourceAuthSettings.getCodeChallenge()).isNotNull();
    }
}