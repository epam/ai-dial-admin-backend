package com.epam.aidial.cfg.security.aspect;

import com.epam.aidial.cfg.configuration.logging.LogExecution;
import com.epam.aidial.cfg.security.InternalSecurityAuthenticationToken;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Aspect
@Component
@LogExecution
public class RunAsInternalUserAspect {

    @Around("@annotation(runAsInternalUser)")
    public Object evaluate(ProceedingJoinPoint joinPoint, RunAsInternalUser runAsInternalUser) throws Throwable {
        checkPrincipal(runAsInternalUser);

        SecurityContext previous = SecurityContextHolder.getContext();

        try {
            setSecurityContext(runAsInternalUser);
            return joinPoint.proceed();
        } finally {
            setPreviousContextOrClear(previous);
        }
    }

    private void checkPrincipal(RunAsInternalUser runAsInternalUser) {
        String principal = runAsInternalUser.principal();
        if (!InternalSecurityAuthenticationToken.isReservedInternalPrincipal(principal)) {
            throw new IllegalArgumentException("Principal '%s' is not a reserved internal principal".formatted(principal));
        }
    }

    private void setSecurityContext(RunAsInternalUser runAsInternalUser) {
        Authentication auth = createAuth(runAsInternalUser);

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(auth);

        SecurityContextHolder.setContext(context);
    }

    private Authentication createAuth(RunAsInternalUser runAsInternalUser) {
        String principal = runAsInternalUser.principal();
        String email = runAsInternalUser.email();

        List<SimpleGrantedAuthority> authorities = Arrays.stream(runAsInternalUser.roles())
                .map(Enum::name)
                .map(SimpleGrantedAuthority::new)
                .toList();

        return new InternalSecurityAuthenticationToken(principal, email, authorities);
    }

    private void setPreviousContextOrClear(SecurityContext previous) {
        if (previous == null) {
            SecurityContextHolder.clearContext();
        } else {
            SecurityContextHolder.setContext(previous);
        }
    }
}