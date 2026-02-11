package com.epam.aidial.cfg.web.security;

import com.epam.aidial.cfg.utils.SecretUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.core.OAuth2AuthenticatedPrincipal;
import org.springframework.security.oauth2.server.resource.introspection.BadOpaqueTokenException;
import org.springframework.security.oauth2.server.resource.introspection.OpaqueTokenIntrospector;

import java.util.List;

@RequiredArgsConstructor
@Slf4j
public class MultiIssuerOpaqueTokenIntrospector implements OpaqueTokenIntrospector {

    private final List<OpaqueTokenIntrospector> opaqueTokenIntrospectors;

    @Override
    public OAuth2AuthenticatedPrincipal introspect(String token) {
        for (var introspector : opaqueTokenIntrospectors) {
            try {
                return introspector.introspect(token);
            } catch (Exception ex) {
                log.debug(
                        "Token introspection failed. Will try to use next introspector. Token: {}",
                        SecretUtils.mask(token),
                        ex
                );
            }
        }

        throw new BadOpaqueTokenException("Token introspection failed. Unable to find applicable introspector");
    }
}