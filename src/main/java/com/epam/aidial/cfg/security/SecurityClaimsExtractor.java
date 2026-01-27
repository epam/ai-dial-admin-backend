package com.epam.aidial.cfg.security;

import com.epam.aidial.cfg.web.security.UserSecurityDetails;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

@Slf4j
public class SecurityClaimsExtractor {

    public static String getAuthor() {
        SecurityContext context = SecurityContextHolder.getContext();
        if (context == null || context.getAuthentication() == null) {
            log.debug("Security context or authentication is null");
            return null;
        }
        Authentication authentication = context.getAuthentication();
        log.trace("Authentication: {}", authentication);

        return authentication.getName();
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
            if (jwtAuthenticationToken.getDetails() instanceof UserSecurityDetails details) {
                return details.email();
            }
            return null;
        }
        return null;
    }
}