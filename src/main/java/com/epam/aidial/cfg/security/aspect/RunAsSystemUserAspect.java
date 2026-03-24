package com.epam.aidial.cfg.security.aspect;

import com.epam.aidial.cfg.configuration.logging.LogExecution;
import com.epam.aidial.cfg.security.SystemAuthenticationToken;
import lombok.RequiredArgsConstructor;
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
@RequiredArgsConstructor
@LogExecution
public class RunAsSystemUserAspect {

    @Around("@annotation(runAsSystemUser)")
    public Object evaluate(ProceedingJoinPoint joinPoint, RunAsSystemUser runAsSystemUser) throws Throwable {
        SecurityContext previous = SecurityContextHolder.getContext();

        try {
            setSystemContext(runAsSystemUser);
            return joinPoint.proceed();
        } finally {
            setPreviousContextOrClear(previous);
        }
    }

    private void setSystemContext(RunAsSystemUser runAsSystemUser) {
        Authentication auth = createAuth(runAsSystemUser);

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(auth);

        SecurityContextHolder.setContext(context);
    }

    private Authentication createAuth(RunAsSystemUser runAsSystemUser) {
        String username = runAsSystemUser.username();

        List<SimpleGrantedAuthority> authorities = Arrays.stream(runAsSystemUser.roles())
                .map(Enum::name)
                .map(SimpleGrantedAuthority::new)
                .toList();

        return new SystemAuthenticationToken(username, authorities);
    }

    private void setPreviousContextOrClear(SecurityContext previous) {
        if (previous == null) {
            SecurityContextHolder.clearContext();
        } else {
            SecurityContextHolder.setContext(previous);
        }
    }
}