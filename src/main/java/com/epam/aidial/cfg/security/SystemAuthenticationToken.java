package com.epam.aidial.cfg.security;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

public class SystemAuthenticationToken extends AbstractAuthenticationToken {

    public static final String SYSTEM_PRINCIPAL = "system";

    private final String principal;

    public SystemAuthenticationToken(String principal, Collection<? extends GrantedAuthority> authorities) {
        super(authorities);
        this.principal = principal;
        setAuthenticated(true);
    }

    @Override
    public Object getCredentials() {
        return null;
    }

    @Override
    public Object getPrincipal() {
        return principal;
    }
}
