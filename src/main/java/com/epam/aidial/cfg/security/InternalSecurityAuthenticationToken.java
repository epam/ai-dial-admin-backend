package com.epam.aidial.cfg.security;

import com.epam.aidial.cfg.web.security.UserSecurityDetails;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;
import java.util.Optional;
import java.util.Set;

public class InternalSecurityAuthenticationToken extends AbstractAuthenticationToken {

    public static final String SYSTEM_PRINCIPAL = "system";
    public static final String SYSTEM_PRINCIPAL_EMAIL = "system@dial.admin";

    private static final Set<String> RESERVED_PRINCIPALS = Set.of(SYSTEM_PRINCIPAL);

    private final String principal;

    public InternalSecurityAuthenticationToken(String principal, String email, Collection<? extends GrantedAuthority> authorities) {
        super(authorities);

        Optional<String> reservedInternalPrincipal = getReservedInternalPrincipal(principal);
        if (reservedInternalPrincipal.isEmpty()) {
            throw new IllegalArgumentException("Principal '%s' is not a reserved internal principal".formatted(principal));
        }

        this.principal = reservedInternalPrincipal.get();
        setDetails(new UserSecurityDetails(email));
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

    public static boolean isReservedInternalPrincipal(String principal) {
        return getReservedInternalPrincipal(principal).isPresent();
    }

    private static Optional<String> getReservedInternalPrincipal(String principal) {
        if (principal == null) {
            return Optional.empty();
        }

        return RESERVED_PRINCIPALS.stream()
                .filter(r -> r.equalsIgnoreCase(principal))
                .findFirst();
    }
}
