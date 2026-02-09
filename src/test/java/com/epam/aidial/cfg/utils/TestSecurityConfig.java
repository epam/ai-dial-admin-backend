package com.epam.aidial.cfg.utils;

import com.epam.aidial.cfg.web.security.JwtProvidersProperties;
import com.epam.aidial.cfg.web.security.NimbusJwtDecoderResolver;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;

import java.nio.charset.StandardCharsets;
import java.util.Set;
import javax.crypto.spec.SecretKeySpec;

import static com.epam.aidial.cfg.utils.JwtUtils.SECRET_KEY;

@Configuration
public class TestSecurityConfig {

    @Primary
    @Bean
    public JwtProvidersProperties jwtProvidersProperties() {
        var config = JwtProviderTestHelper.createProviderConfig();
        config.setAllowedRoles(Set.of("testRole"));

        var config2 = JwtProviderTestHelper.createProviderConfig();
        config2.setIssuer("https://sts.windows.net/issuer_test2/");
        config2.setJwkSetUri("https://test2/keys");

        JwtProvidersProperties properties = new JwtProvidersProperties();
        properties.getProviders().put("test", config);
        properties.getProviders().put("test2", config2);

        return properties;
    }

    /**
     * Test implementation of NimbusJwtDecoderResolver
     * Uses SecretKey as signature mechanism instead of production JWKS approach
     */
    @Primary
    @Bean
    public NimbusJwtDecoderResolver nimbusJwtDecoderResolver() {
        return config -> NimbusJwtDecoder.withSecretKey(
                        new SecretKeySpec(
                                SECRET_KEY.getBytes(StandardCharsets.UTF_8),
                                SignatureAlgorithm.HS256.getValue()))
                .build();
    }
}