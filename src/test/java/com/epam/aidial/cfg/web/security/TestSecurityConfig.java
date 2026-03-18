package com.epam.aidial.cfg.web.security;

import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;

import java.nio.charset.StandardCharsets;
import javax.crypto.spec.SecretKeySpec;

import static com.epam.aidial.cfg.utils.JwtUtils.SECRET_KEY;

@Configuration
public class TestSecurityConfig {

    /**
     * Test implementation of {@link com.epam.aidial.cfg.web.security.NimbusJwtDecoderResolver}.
     * Uses SecretKey as signature mechanism instead of production JWKS approach
     */
    @Primary
    @Bean
    public NimbusJwtDecoderResolver testNimbusJwtDecoderResolver() {
        return config -> NimbusJwtDecoder.withSecretKey(
                        new SecretKeySpec(
                                SECRET_KEY.getBytes(StandardCharsets.UTF_8),
                                SignatureAlgorithm.HS256.getValue()))
                .build();
    }
}