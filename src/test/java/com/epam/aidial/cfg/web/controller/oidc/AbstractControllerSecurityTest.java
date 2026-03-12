package com.epam.aidial.cfg.web.controller.oidc;

import com.epam.aidial.cfg.configuration.JsonMapperConfiguration;
import com.epam.aidial.cfg.configuration.RestTemplateConfig;
import com.epam.aidial.cfg.utils.JwtUtils;
import com.epam.aidial.cfg.utils.TestAuthenticationConverterFactory;
import com.epam.aidial.cfg.utils.TestIdentityProviderConfig;
import com.epam.aidial.cfg.utils.TestTokenDecoderFactory;
import com.epam.aidial.cfg.web.security.SecurityPackage;
import org.junit.jupiter.params.provider.Arguments;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import com.epam.aidial.cfg.web.security.AdminRole;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static com.epam.aidial.cfg.web.controller.oidc.AbstractControllerSecurityTest.PRINCIPAL_CLAIM;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

@TestPropertySource(properties = {
        "config.rest.security.mode=oidc",
        "config.rest.security.default.email-claim=unique_name",
        "config.rest.security.default.principal-claim=" + PRINCIPAL_CLAIM,
        "config.rest.security.default.allowedRoles=ConfigAdmin,admin"
})
@ComponentScan(basePackageClasses = {
        SecurityPackage.class,
})
@Import({
        JsonMapperConfiguration.class,
        TestTokenDecoderFactory.class,
        TestAuthenticationConverterFactory.class,
        TestIdentityProviderConfig.class,
        RestTemplateConfig.class
})
public abstract class AbstractControllerSecurityTest {

    protected static final String TEST_AUDIENCE = "audience_test";
    protected static final String WRONG_TEST_AUDIENCE = "wrong_audience_test";

    protected static final String TEST_ISSUER = "https://sts.windows.net/issuer_test/";
    protected static final String WRONG_TEST_ISSUER = "wrong_issuer_test";

    protected static final String ROLES_CLAIM = "roles";
    protected static final String PRINCIPAL_CLAIM = "oid";
    protected static final String EMAIL_CLAIM = "email";

    @Autowired
    protected MockMvc mockMvc;

    protected static Stream<Arguments> unauthorizedArguments() {
        return Stream.of(
                Arguments.of((String) null),
                Arguments.of("invalid_jwt"),
                Arguments.of(
                        JwtUtils.generateTestToken(
                                WRONG_TEST_AUDIENCE,
                                TEST_ISSUER,
                                Map.of(
                                        PRINCIPAL_CLAIM, "user_test",
                                        ROLES_CLAIM, "ConfigAdmin"
                                )
                        )
                ),
                Arguments.of(
                        JwtUtils.generateTestToken(
                                TEST_AUDIENCE,
                                WRONG_TEST_ISSUER,
                                Map.of(
                                        PRINCIPAL_CLAIM, "user_test",
                                        ROLES_CLAIM, "ConfigAdmin"
                                )
                        )
                )
        );
    }

    protected static Stream<Arguments> forbiddenArguments() {
        return Stream.of(
                Arguments.of(
                        JwtUtils.generateTestToken(
                                TEST_AUDIENCE,
                                TEST_ISSUER,
                                Map.of(
                                        PRINCIPAL_CLAIM, "user_test",
                                        ROLES_CLAIM, "ConfigAdmin1"
                                )
                        )
                ),
                Arguments.of(
                        JwtUtils.generateTestToken(
                                TEST_AUDIENCE,
                                TEST_ISSUER,
                                Map.of(
                                        PRINCIPAL_CLAIM, "user_test",
                                        "resource_access", "testRole"
                                )
                        )
                )
        );
    }

    protected static Stream<Arguments> okArguments() {
        return Stream.of(
                Arguments.of(
                        JwtUtils.generateTestToken(
                                TEST_AUDIENCE,
                                TEST_ISSUER,
                                Map.of(
                                        PRINCIPAL_CLAIM, "user_test",
                                        "resource_access", Map.of(ROLES_CLAIM, "testRole"),
                                        EMAIL_CLAIM, "test@email.com"
                                )
                        ),
                        "user_test",
                        "test@email.com",
                        List.of(new SimpleGrantedAuthority(AdminRole.FULL_ADMIN.name()))
                ),
                Arguments.of(
                        JwtUtils.generateTestToken(
                                TEST_AUDIENCE,
                                TEST_ISSUER,
                                Map.of(
                                        PRINCIPAL_CLAIM, "user_test",
                                        ROLES_CLAIM, "testRole",
                                        "resource_access", "testRole")
                        ),
                        "user_test",
                        null,
                        List.of(new SimpleGrantedAuthority(AdminRole.FULL_ADMIN.name()))
                ),
                Arguments.of(
                        JwtUtils.generateTestToken(
                                TEST_AUDIENCE,
                                TEST_ISSUER,
                                Map.of(
                                        PRINCIPAL_CLAIM, "user_test",
                                        ROLES_CLAIM, "testRole",
                                        "unique_name", "test@email.com"
                                )
                        ),
                        "user_test",
                        null,
                        List.of(new SimpleGrantedAuthority(AdminRole.FULL_ADMIN.name()))
                )
        );
    }

    protected ResultActions performGet(final String url,
                                       final String jwtToken,
                                       final Object... uriVariables) throws Exception {
        return mockMvc.perform(get(url, uriVariables)
                .header("Authorization", "Bearer " + jwtToken));
    }

}