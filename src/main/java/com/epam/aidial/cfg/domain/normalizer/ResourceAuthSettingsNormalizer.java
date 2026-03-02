package com.epam.aidial.cfg.domain.normalizer;

import com.epam.aidial.cfg.domain.model.ResourceAuthSettings;
import com.nimbusds.oauth2.sdk.pkce.CodeChallenge;
import com.nimbusds.oauth2.sdk.pkce.CodeChallengeMethod;
import com.nimbusds.oauth2.sdk.pkce.CodeVerifier;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

@Component
public class ResourceAuthSettingsNormalizer {

    public void normalize(ResourceAuthSettings resourceAuthSettings) {
        if (resourceAuthSettings == null) {
            return;
        }

        normalizePkceData(resourceAuthSettings);
    }

    private void normalizePkceData(ResourceAuthSettings resourceAuthSettings) {
        String codeChallengeMethodStr = resourceAuthSettings.getCodeChallengeMethod();
        if (StringUtils.isEmpty(codeChallengeMethodStr)) {
            return;
        }

        String codeVerifier = resourceAuthSettings.getCodeVerifier();
        String codeChallenge = resourceAuthSettings.getCodeChallenge();

        boolean hasVerifier = codeVerifier != null;
        boolean hasChallenge = codeChallenge != null;

        // XOR condition: if one is present but not the other, it's an invalid state.
        if (hasVerifier ^ hasChallenge) {
            throw new IllegalArgumentException("Both code_verifier and code_challenge must be provided, or neither.");
        }

        CodeChallengeMethod codeChallengeMethod = CodeChallengeMethod.parse(codeChallengeMethodStr);
        if (hasVerifier) {
            // If both are present, validate them against each other.
            validatePkcePair(codeVerifier, codeChallenge, codeChallengeMethod);
        } else {
            // If neither is present, generate a new pair.
            generateAndSetPkcePair(resourceAuthSettings, codeChallengeMethod);
        }
    }

    /**
     * Validates that the provided code challenge is correctly derived from the code verifier.
     */
    private void validatePkcePair(String codeVerifierStr, String codeChallengeStr, CodeChallengeMethod method) {
        CodeVerifier verifier = new CodeVerifier(codeVerifierStr);
        CodeChallenge computedChallenge = CodeChallenge.compute(method, verifier);

        // Throws an exception if the provided challenge does not match the computed one.
        if (!computedChallenge.getValue().equals(codeChallengeStr)) {
            throw new IllegalArgumentException("Provided code_challenge does not match the one computed from the code_verifier.");
        }
    }

    /**
     * Generates a new code verifier and challenge and sets them on the settings object.
     */
    private void generateAndSetPkcePair(ResourceAuthSettings resourceAuthSettings, CodeChallengeMethod method) {
        CodeVerifier newVerifier = new CodeVerifier();
        CodeChallenge newChallenge = CodeChallenge.compute(method, newVerifier);

        resourceAuthSettings.setCodeVerifier(newVerifier.getValue());
        resourceAuthSettings.setCodeChallenge(newChallenge.getValue());
    }
}
