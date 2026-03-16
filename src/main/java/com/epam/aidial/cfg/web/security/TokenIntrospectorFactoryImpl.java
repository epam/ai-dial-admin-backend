package com.epam.aidial.cfg.web.security;

import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.server.resource.introspection.OpaqueTokenIntrospector;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
public class TokenIntrospectorFactoryImpl implements TokenIntrospectorFactory {

    private final IdentityProviderUtils identityProviderUtils;
    private final List<OpaqueTokenProviderConfig> providerConfigs;
    private final RestTemplate restTemplate;

    @Override
    public OpaqueTokenIntrospector createOpaqueTokenIntrospector() {
        List<OpaqueTokenIntrospector> opaqueTokenIntrospectors = new ArrayList<>(providerConfigs.size());

        for (var config : providerConfigs) {
            var grantedAuthoritiesConverter = new MultiPathGrantedAuthoritiesConverter<>();
            var authoritiesPaths = config.getRoleClaims().stream()
                    .map(String::trim)
                    .toList();
            grantedAuthoritiesConverter.setAuthoritiesPaths(authoritiesPaths);
            grantedAuthoritiesConverter.setAuthorityPrefix("");

            var opaqueTokenIntrospector = new DefaultOpaqueTokenIntrospector(
                    restTemplate,
                    config,
                    grantedAuthoritiesConverter,
                    identityProviderUtils.getPrincipalClaim(config.getPrincipalClaim()),
                    identityProviderUtils.getEmailClaims(config.getEmailClaims())
            );
            opaqueTokenIntrospectors.add(opaqueTokenIntrospector);
        }

        return new MultiIssuerOpaqueTokenIntrospector(opaqueTokenIntrospectors);
    }
}