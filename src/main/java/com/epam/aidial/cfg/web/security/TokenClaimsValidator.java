package com.epam.aidial.cfg.web.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.MapUtils;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.Set;

@RequiredArgsConstructor
@Slf4j
public class TokenClaimsValidator implements OAuth2TokenValidator<Jwt> {

    private static final String ERROR_CODE = "invalid_token";
    private static final String JWT_TOKEN_VALIDATION_FAILED = "JWT token validation failed: {}";

    private static final String CLAIM_NAME_ISS = "iss";
    private static final String CLAIM_NAME_AUD = "aud";

    private final Set<String> acceptedIssuers;
    private final Set<String> acceptedAudiences;

    public OAuth2TokenValidatorResult validate(final Jwt jwt) {
        if (MapUtils.isEmpty(jwt.getClaims())) {
            final var description = "token must contain claims";
            log.warn(JWT_TOKEN_VALIDATION_FAILED, description);
            return createFailureValidationResult(description);
        }

        if (!jwt.hasClaim(CLAIM_NAME_ISS)) {
            final var description = String.format("token must contain issuer (%s) claim", CLAIM_NAME_ISS);
            log.warn(JWT_TOKEN_VALIDATION_FAILED, description);
            return createFailureValidationResult(description);
        }

        if (!jwt.hasClaim(CLAIM_NAME_AUD)) {
            final var description = String.format("token must contain audience (%s) claim", CLAIM_NAME_AUD);
            log.warn(JWT_TOKEN_VALIDATION_FAILED, description);
            return createFailureValidationResult(description);
        }

        final var audience = jwt.getAudience();
        if (acceptedAudiences.stream().noneMatch(audience::contains)) {
            final var description = String.format("Invalid Audience (%s) claim: %s", CLAIM_NAME_AUD, audience);
            log.warn(JWT_TOKEN_VALIDATION_FAILED, description);
            return createFailureValidationResult(description);
        }

        final var issuer = jwt.getIssuer();
        if (acceptedIssuers.stream().noneMatch(x -> x.equals(issuer.toString()))) {
            final var description = String.format("Invalid Issuer (%s) claim: %s", CLAIM_NAME_ISS, issuer.toString());
            log.warn(JWT_TOKEN_VALIDATION_FAILED, description);
            return createFailureValidationResult(description);
        }

        return OAuth2TokenValidatorResult.success();
    }

    private OAuth2TokenValidatorResult createFailureValidationResult(final String description) {
        final var error = new OAuth2Error(ERROR_CODE, description, null);
        return OAuth2TokenValidatorResult.failure(error);
    }

}
