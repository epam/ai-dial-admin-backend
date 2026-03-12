package com.epam.aidial.cfg.utils;

import com.epam.aidial.cfg.web.security.IdentityProviderUtils;
import com.epam.aidial.cfg.web.security.JwtAuthenticationConverterFactory;
import com.epam.aidial.cfg.web.security.JwtProviderConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.Set;

/**
 * Test converter factory for read-only admin security tests.
 * Maps FullAdminRole → FULL_ADMIN and ReadOnlyRole → READ_ONLY_ADMIN.
 */
@Configuration
public class ReadOnlyAdminConverterFactory {

    @Bean
    public JwtAuthenticationConverterFactory getAuthenticationConverterFactory() {
        var config = IdentityProviderTestHelper.createJwtProviderConfig();
        config.setAllowedRoles(Set.of("FullAdminRole:[FULL_ADMIN]", "ReadOnlyRole:[READ_ONLY_ADMIN]"));
        return new JwtAuthenticationConverterFactory(
                List.of(JwtProviderConfig.from(config.getIssuer(), config)),
                new IdentityProviderUtils(
                        Set.of("FullAdminRole:[FULL_ADMIN]", "ReadOnlyRole:[READ_ONLY_ADMIN]"),
                        "unique_name", "oid", false)
        );
    }
}
