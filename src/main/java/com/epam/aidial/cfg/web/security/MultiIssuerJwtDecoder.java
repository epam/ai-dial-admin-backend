package com.epam.aidial.cfg.web.security;

import com.nimbusds.jwt.SignedJWT;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.server.resource.InvalidBearerTokenException;

import java.text.ParseException;
import java.util.Map;

import static org.apache.commons.lang3.StringUtils.left;

import com.epam.aidial.cfg.utils.SecretUtils;

@Slf4j
@RequiredArgsConstructor
public class MultiIssuerJwtDecoder implements JwtDecoder {

    private final Map<String, JwtDecoder> issuerToDecoderMap;

    @Override
    public Jwt decode(final String token) throws JwtException {
        if (log.isTraceEnabled()) {
            log.trace("decode. token: {}", token);
        } else {
            log.debug("decode. token: {}", SecretUtils.mask(token));
        }

        final var issuer = getIssuer(token);
        final var jwtDecoder = issuerToDecoderMap.get(issuer);

        if (jwtDecoder == null) {
            throw new InvalidBearerTokenException("Unrecognized issuer: " + issuer);
        }

        return jwtDecoder.decode(token);
    }

    private String getIssuer(final String token) {
        try {
            return SignedJWT.parse(token)
                .getJWTClaimsSet()
                .getIssuer();
        } catch (ParseException e) {
            log.warn("Failed to authenticate since the JWT token (value = {}***) was invalid: ",
                    left(token, 5));
            throw new InvalidBearerTokenException(e.getMessage(), e);
        }
    }

}
