package com.epam.aidial.cfg.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.util.Objects;

@Slf4j
public class SecurityClaimsExtractor {
    private static final String DEFAULT_CLAIMS_EMAIL_KEY = "unique_name";
    private static final String CLAIMS_EMAIL_KEY = System.getenv().getOrDefault("CLAIMS_EMAIL_KEY", DEFAULT_CLAIMS_EMAIL_KEY);
    private static final String CLAIMS_NAME_KEY = System.getenv().get("CLAIMS_NAME_KEY");

    public static String getAuthor() {
        SecurityContext context = SecurityContextHolder.getContext();
        if (context == null || context.getAuthentication() == null) {
            log.debug("Security context or authentication is null");
            return null;
        }
        Authentication authentication = context.getAuthentication();
        log.trace("Authentication: {}", authentication);
        if (CLAIMS_NAME_KEY == null) {
            return authentication.getName();
        }
        if (context.getAuthentication() instanceof JwtAuthenticationToken jwtAuthenticationToken) {
            Jwt token = jwtAuthenticationToken.getToken();
            log.trace("token claims: {}", token.getClaims());
            Object name = token.getClaims().get(CLAIMS_NAME_KEY);
            if (name != null) {
                return Objects.toString(name);
            }
        }
        return null;
    }

    public static String getEmail() {
        SecurityContext context = SecurityContextHolder.getContext();
        if (context == null || context.getAuthentication() == null) {
            log.debug("Security context or authentication is null");
            return null;
        }
        Authentication authentication = context.getAuthentication();
        log.trace("Authentication: {}", authentication);
        if (context.getAuthentication() instanceof JwtAuthenticationToken jwtAuthenticationToken) {
            Jwt token = jwtAuthenticationToken.getToken();
            log.trace("token claims: {}", token.getClaims());
            Object uniqueName = token.getClaims().get(CLAIMS_EMAIL_KEY);
            if (uniqueName != null) {
                return Objects.toString(uniqueName);
            }
        }
        return null;
    }
}
