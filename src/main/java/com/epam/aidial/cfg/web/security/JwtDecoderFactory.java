package com.epam.aidial.cfg.web.security;

import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtTimestampValidator;

import java.util.Set;

public class JwtDecoderFactory {

    public JwtDecoder createDecoder(Set<String> acceptedIssuers,
                                    Set<String> acceptedAudiences,
                                    JwtProvidersProperties.ProviderConfig config,
                                    NimbusJwtDecoderResolver nimbusJwtDecoderResolver) {
        var claimsValidator = new TokenClaimsValidator(acceptedIssuers, acceptedAudiences);
        var expirationDateValidator = new JwtTimestampValidator();
        var tokenValidator = new DelegatingOAuth2TokenValidator<>(claimsValidator, expirationDateValidator);

        var jwtDecoder = nimbusJwtDecoderResolver.resolve(config);
        jwtDecoder.setJwtValidator(tokenValidator);

        return jwtDecoder;
    }

}
