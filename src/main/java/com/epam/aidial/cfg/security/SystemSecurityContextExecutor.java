package com.epam.aidial.cfg.security;

import com.epam.aidial.cfg.web.security.UserRole;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.Callable;

@Component
public class SystemSecurityContextExecutor {

    public void runAsSystemUser(Runnable task) {
        SecurityContext previous = SecurityContextHolder.getContext();

        try {
            setSystemContext();
            task.run();
        } finally {
            setPreviousContextOrClear(previous);
        }
    }

    public <T> T runAsSystemUser(Callable<T> task) throws Exception {
        SecurityContext previous = SecurityContextHolder.getContext();

        try {
            setSystemContext();
            return task.call();
        } finally {
            setPreviousContextOrClear(previous);
        }
    }

    private void setSystemContext() {
        Authentication auth = createAuth();

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(auth);

        SecurityContextHolder.setContext(context);
    }

    private Authentication createAuth() {
        SimpleGrantedAuthority fullAdminAuthority = new SimpleGrantedAuthority(UserRole.FULL_ADMIN.name());
        return new SystemAuthenticationToken(List.of(fullAdminAuthority));
    }

    private void setPreviousContextOrClear(SecurityContext previous) {
        if (previous == null) {
            SecurityContextHolder.clearContext();
        } else {
            SecurityContextHolder.setContext(previous);
        }
    }
}
