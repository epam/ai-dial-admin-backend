package com.epam.aidial.cfg.web.security;

import org.springframework.security.oauth2.server.resource.introspection.OpaqueTokenIntrospector;

public interface TokenIntrospectorFactory {

    OpaqueTokenIntrospector createOpaqueTokenIntrospector();
}
