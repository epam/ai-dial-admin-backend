package com.epam.aidial.cfg.web.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jetbrains.annotations.NotNull;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * Filter that injects a synthetic {@code FULL_ADMIN} authority into the security context.
 * Used only in no-security mode ({@code config.rest.security.mode=none}) so that
 * {@code @PreAuthorize("hasAuthority('FULL_ADMIN')")} passes for all requests.
 */
public class SyntheticFullAdminAuthenticationFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(@NotNull HttpServletRequest request,
                                    @NotNull HttpServletResponse response,
                                    @NotNull FilterChain filterChain) throws ServletException, IOException {
        var authority = new SimpleGrantedAuthority(AdminRole.FULL_ADMIN.name());
        var authentication = new PreAuthenticatedAuthenticationToken(
                "synthetic-full-admin",
                null,
                List.of(authority)
        );
        authentication.setDetails(new UserSecurityDetails(null));
        SecurityContextHolder.getContext().setAuthentication(authentication);
        filterChain.doFilter(request, response);
    }
}
