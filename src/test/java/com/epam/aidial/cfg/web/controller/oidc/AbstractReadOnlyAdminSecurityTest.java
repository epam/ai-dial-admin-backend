package com.epam.aidial.cfg.web.controller.oidc;

import com.epam.aidial.cfg.configuration.JsonMapperConfiguration;
import com.epam.aidial.cfg.configuration.RestTemplateConfig;
import com.epam.aidial.cfg.utils.ReadOnlyAdminConverterFactory;
import com.epam.aidial.cfg.utils.TestIdentityProviderConfig;
import com.epam.aidial.cfg.utils.TestTokenDecoderFactory;
import com.epam.aidial.cfg.web.security.SecurityPackage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

@TestPropertySource(properties = {
        "config.rest.security.mode=oidc",
        "config.rest.security.default.email-claim=unique_name",
        "config.rest.security.default.principal-claim=oid",
        "config.rest.security.default.allowedRoles=FullAdminRole:[FULL_ADMIN],ReadOnlyRole:[READ_ONLY_ADMIN]"
})
@ComponentScan(basePackageClasses = {SecurityPackage.class})
@Import({
        JsonMapperConfiguration.class,
        TestTokenDecoderFactory.class,
        ReadOnlyAdminConverterFactory.class,
        TestIdentityProviderConfig.class,
        RestTemplateConfig.class
})
public abstract class AbstractReadOnlyAdminSecurityTest {

    protected static final String TEST_AUDIENCE = "audience_test";
    protected static final String TEST_ISSUER = "https://sts.windows.net/issuer_test/";
    protected static final String ROLES_CLAIM = "roles";
    protected static final String PRINCIPAL_CLAIM = "oid";

    @Autowired
    protected MockMvc mockMvc;
}
