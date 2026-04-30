package com.epam.aidial.cfg.web.controller.oidc;

import com.epam.aidial.cfg.configuration.JsonMapperConfiguration;
import com.epam.aidial.cfg.configuration.RestTemplateConfig;
import com.epam.aidial.cfg.utils.JwtUtils;
import com.epam.aidial.cfg.web.security.SecurityPackage;
import com.epam.aidial.cfg.web.security.TestSecurityConfig;
import com.epam.aidial.cfg.web.security.UserRole;
import org.junit.jupiter.params.provider.Arguments;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static com.epam.aidial.cfg.web.controller.oidc.AbstractControllerSecurityTest.PRINCIPAL_CLAIM;
import static com.epam.aidial.cfg.web.controller.oidc.AbstractControllerSecurityTest.TEST_ISSUER;
import static com.epam.aidial.cfg.web.controller.oidc.AbstractControllerSecurityTest.TEST_ISSUER_2;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

@TestPropertySource(properties = {
        "config.rest.security.mode=oidc",
        "config.rest.security.default.email-claim=unique_name",
        "config.rest.security.default.principal-claim=" + PRINCIPAL_CLAIM,
        "config.rest.security.default.allowedRoles=ConfigAdmin,admin",
        "config.rest.security.default.roles-mapping={}",

        "providers.test.issuer=" + TEST_ISSUER,
        "providers.test.jwk-set-uri=https://test/keys",
        "providers.test.audiences=" + AbstractControllerSecurityTest.TEST_AUDIENCE,
        "providers.test.role-claims=roles, resource_access.roles",
        "providers.test.allowed-roles=testRole",
        "providers.test.email-claims=email",

        "providers.test2.issuer=" + TEST_ISSUER_2,
        "providers.test2.jwk-set-uri=https://test/keys",
        "providers.test2.audiences=" + AbstractControllerSecurityTest.TEST_AUDIENCE,
        "providers.test2.role-claims=roles, resource_access.roles",
        "providers.test2.roles-mapping={\"testRole\":[\"READ_ONLY_ADMIN\"]}",
        "providers.test2.email-claims=email",
})
@ComponentScan(basePackageClasses = {
        SecurityPackage.class,
})
@Import({
        JsonMapperConfiguration.class,
        TestSecurityConfig.class,
        RestTemplateConfig.class
})
public abstract class AbstractControllerSecurityTest {

    protected static final String TEST_AUDIENCE = "audience_test";
    protected static final String WRONG_TEST_AUDIENCE = "wrong_audience_test";

    protected static final String TEST_ISSUER = "https://sts.windows.net/issuer_test/";
    protected static final String TEST_ISSUER_2 = "https://sts.windows.net/issuer_test/2";
    protected static final String WRONG_TEST_ISSUER = "wrong_issuer_test";

    protected static final String ROLES_CLAIM = "roles";
    protected static final String PRINCIPAL_CLAIM = "oid";
    protected static final String EMAIL_CLAIM = "email";

    @Autowired
    protected MockMvc mockMvc;

    protected static Stream<Arguments> invalidJwt() {
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

    protected static Stream<Arguments> notAllowedRoles() {
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

    protected static Stream<Arguments> reservedPrincipal() {
        return Stream.of(
                Arguments.of(
                        JwtUtils.generateTestToken(
                                TEST_AUDIENCE,
                                TEST_ISSUER,
                                Map.of(
                                        PRINCIPAL_CLAIM, "system",
                                        "resource_access", Map.of(ROLES_CLAIM, "testRole"),
                                        EMAIL_CLAIM, "test@email.com"
                                )
                        )
                ),
                Arguments.of(
                        JwtUtils.generateTestToken(
                                TEST_AUDIENCE,
                                TEST_ISSUER,
                                Map.of(
                                        PRINCIPAL_CLAIM, "System",
                                        "resource_access", Map.of(ROLES_CLAIM, "testRole"),
                                        EMAIL_CLAIM, "test@email.com"
                                )
                        )
                ),
                Arguments.of(
                        JwtUtils.generateTestToken(
                                TEST_AUDIENCE,
                                TEST_ISSUER,
                                Map.of(
                                        PRINCIPAL_CLAIM, "SYSTEM",
                                        "resource_access", Map.of(ROLES_CLAIM, "testRole"),
                                        EMAIL_CLAIM, "test@email.com"
                                )
                        )
                )
        );
    }

    protected static Stream<Arguments> fullAdminRoles() {
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
                        List.of(new SimpleGrantedAuthority(UserRole.FULL_ADMIN.name()))
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
                        List.of(new SimpleGrantedAuthority(UserRole.FULL_ADMIN.name()))
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
                        List.of(new SimpleGrantedAuthority(UserRole.FULL_ADMIN.name()))
                )
        );
    }

    protected static Stream<Arguments> readOnlyAdminRoles() {
        return Stream.of(
                Arguments.of(
                        JwtUtils.generateTestToken(
                                TEST_AUDIENCE,
                                TEST_ISSUER_2,
                                Map.of(
                                        PRINCIPAL_CLAIM, "user_test",
                                        "resource_access", Map.of(ROLES_CLAIM, "testRole"),
                                        EMAIL_CLAIM, "test@email.com"
                                )
                        ),
                        "user_test",
                        "test@email.com",
                        List.of(new SimpleGrantedAuthority(UserRole.READ_ONLY_ADMIN.name()))
                ),
                Arguments.of(
                        JwtUtils.generateTestToken(
                                TEST_AUDIENCE,
                                TEST_ISSUER_2,
                                Map.of(
                                        PRINCIPAL_CLAIM, "user_test",
                                        ROLES_CLAIM, "testRole",
                                        "resource_access", "testRole")
                        ),
                        "user_test",
                        null,
                        List.of(new SimpleGrantedAuthority(UserRole.READ_ONLY_ADMIN.name()))
                ),
                Arguments.of(
                        JwtUtils.generateTestToken(
                                TEST_AUDIENCE,
                                TEST_ISSUER_2,
                                Map.of(
                                        PRINCIPAL_CLAIM, "user_test",
                                        ROLES_CLAIM, "testRole",
                                        "unique_name", "test@email.com"
                                )
                        ),
                        "user_test",
                        null,
                        List.of(new SimpleGrantedAuthority(UserRole.READ_ONLY_ADMIN.name()))
                )
        );
    }

    protected ResultActions performGet(final String url,
                                       final String jwtToken,
                                       final Object... uriVariables) throws Exception {
        return mockMvc.perform(get(url, uriVariables)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtToken)
                .header(HttpHeaders.IF_NONE_MATCH, "*"));
    }

    protected ResultActions performPost(final String url, final String jwtToken, final String jsonBody) throws Exception {
        return mockMvc.perform(post(url)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonBody != null ? jsonBody : "{}"));
    }

    protected ResultActions performPut(final String url, final String jwtToken, final String jsonBody, final Object... uriVariables) throws Exception {
        return mockMvc.perform(put(url, uriVariables)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtToken)
                .header(HttpHeaders.IF_MATCH, "*")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonBody != null ? jsonBody : "{}"));
    }

    protected ResultActions performDelete(final String url, final String jwtToken, final Object... uriVariables) throws Exception {
        return mockMvc.perform(delete(url, uriVariables)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtToken));
    }

}