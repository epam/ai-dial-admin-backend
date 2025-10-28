package com.epam.aidial.cfg.utils;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.Map;
import javax.crypto.spec.SecretKeySpec;

public class JwtUtils {

    public static final String SECRET_KEY = "test_secret_key_for_token_signature";

    public static String generateTestTokenAsString(final String audience,
                                                   final String issuer,
                                                   final Map<String, Object> claims) {
        final var nowMillis = System.currentTimeMillis();
        final var now = new Date(nowMillis);
        final var exp = new Date(nowMillis + 900000);

        final var keySpec = new SecretKeySpec(SECRET_KEY.getBytes(StandardCharsets.UTF_8), SignatureAlgorithm.HS256.getValue());

        return Jwts.builder()
            .setAudience(audience)
            .setIssuer(issuer)
            .addClaims(claims)
            .setIssuedAt(now)
            .setExpiration(exp)
            .signWith(SignatureAlgorithm.HS256, keySpec)
            .compact();
    }

    public static Jwt generateTestToken(Map<String, Object> claims) {
        return new Jwt(
                "tokenValue",
                Instant.now(),
                Instant.now().plusSeconds(3600),
                Map.of("alg", "HS256", "typ", "JWT"),
                claims
        );
    }
}