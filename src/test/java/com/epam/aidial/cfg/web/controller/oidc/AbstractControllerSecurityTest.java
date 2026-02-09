package com.epam.aidial.cfg.web.controller.oidc;

import com.epam.aidial.cfg.configuration.JsonMapperConfiguration;
import com.epam.aidial.cfg.utils.JwtUtils;
import com.epam.aidial.cfg.utils.TestSecurityConfig;
import com.epam.aidial.cfg.web.security.SecurityPackage;
import com.google.common.collect.ImmutableList;
import org.junit.jupiter.params.provider.Arguments;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.List;
import java.util.Map;

import static com.epam.aidial.cfg.web.controller.oidc.AbstractControllerSecurityTest.PRINCIPAL_CLAIM;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

@TestPropertySource(properties = {
        "config.rest.security.mode=oidc",
        "config.rest.security.principal-claim=" + PRINCIPAL_CLAIM,
        "config.rest.security.default.allowedRoles=ConfigAdmin,admin"
})
@ComponentScan(basePackageClasses = {
    SecurityPackage.class,
})
@Import({
        JsonMapperConfiguration.class,
        TestSecurityConfig.class,
})
@TestPropertySource(properties = "spring.main.allow-bean-definition-overriding=true")
public abstract class AbstractControllerSecurityTest {

    protected static final String TEST_AUDIENCE = "audience_test";
    protected static final String WRONG_TEST_AUDIENCE = "wrong_audience_test";

    protected static final String TEST_ISSUER = "https://sts.windows.net/issuer_test/";
    protected static final String WRONG_TEST_ISSUER = "wrong_issuer_test";

    protected static final String ROLES_CLAIM = "roles";
    protected static final String PRINCIPAL_CLAIM = "oid";

    @Autowired
    protected MockMvc mockMvc;

    protected static List<Arguments> arguments() {
        return ImmutableList
            .<Arguments>builder()

            .add(Arguments.of(
                null,
                HttpStatus.UNAUTHORIZED
            ))

            .add(Arguments.of(
                "invalid_jwt",
                HttpStatus.UNAUTHORIZED
            ))

            .add(Arguments.of(
                    JwtUtils.generateTestToken(
                    WRONG_TEST_AUDIENCE,
                    TEST_ISSUER,
                    Map.of(
                        PRINCIPAL_CLAIM, "user_test",
                        ROLES_CLAIM, "ConfigAdmin"
                    )
                ),
                HttpStatus.UNAUTHORIZED
            ))

            .add(Arguments.of(
                    JwtUtils.generateTestToken(
                    TEST_AUDIENCE,
                    WRONG_TEST_ISSUER,
                    Map.of(
                        PRINCIPAL_CLAIM, "user_test",
                        ROLES_CLAIM, "ConfigAdmin"
                    )
                ),
                HttpStatus.UNAUTHORIZED
            ))
                .add(Arguments.of(
                        JwtUtils.generateTestToken(
                                TEST_AUDIENCE,
                                TEST_ISSUER,
                                Map.of(
                                        PRINCIPAL_CLAIM, "user_test",
                                        "resource_access", Map.of(ROLES_CLAIM, "testRole")
                                )
                        ),
                        HttpStatus.OK
                ))
                .add(Arguments.of(
                        JwtUtils.generateTestToken(
                                TEST_AUDIENCE,
                                TEST_ISSUER,
                                Map.of(
                                        PRINCIPAL_CLAIM, "user_test",
                                        "resource_access", "testRole")
                        ),
                        HttpStatus.FORBIDDEN
                ))
                .add(Arguments.of(
                        JwtUtils.generateTestToken(
                                TEST_AUDIENCE,
                                TEST_ISSUER,
                                Map.of(
                                        PRINCIPAL_CLAIM, "user_test",
                                        ROLES_CLAIM, "testRole",
                                        "resource_access", "testRole")
                        ),
                        HttpStatus.OK
                ))
                .add(Arguments.of(
                        JwtUtils.generateTestToken(
                                TEST_AUDIENCE,
                                TEST_ISSUER,
                                Map.of(
                                        PRINCIPAL_CLAIM, "user_test",
                                        ROLES_CLAIM, "testRole"
                                )
                        ),
                        HttpStatus.OK
                ))
            .addAll(addForbiddenArguments())
            .build();
    }

    protected static List<Arguments> addForbiddenArguments() {
        return ImmutableList
            .<Arguments>builder()

            .add(Arguments.of(
                    JwtUtils.generateTestToken(
                    TEST_AUDIENCE,
                    TEST_ISSUER,
                    Map.of(
                        PRINCIPAL_CLAIM, "user_test",
                        ROLES_CLAIM, "ConfigAdmin1"
                    )
                ),
                HttpStatus.FORBIDDEN
            ))

            .build();
    }

    protected ResultActions performGet(final String url,
                                       final String jwtToken,
                                       final Object... uriVariables) throws Exception {
        return mockMvc.perform(get(url, uriVariables)
                .header("Authorization", "Bearer " + jwtToken));
    }

}