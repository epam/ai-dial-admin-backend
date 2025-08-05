package com.epam.aidial.cfg.security.s2s;

import static com.epam.aidial.cfg.security.s2s.InnerSystemUserContextSecurityAspect.ORDER;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.security.concurrent.DelegatingSecurityContextRunnable;
import org.springframework.stereotype.Component;

@Aspect
@Order(ORDER)
@RequiredArgsConstructor
@Component
@Slf4j
public class InnerSystemUserContextSecurityAspect {

    public static final int ORDER = Ordered.HIGHEST_PRECEDENCE;

    private final S2STokenService s2STokenService;

    @SneakyThrows
    private static void proceed(final ProceedingJoinPoint joinPoint) {
        joinPoint.proceed();
    }

    @Around("annotatedWithInnerSystemUserSecurityContext()")
    public void aspectAround(final ProceedingJoinPoint joinPoint) {
        log.debug("Start getting service context.");
        new DelegatingSecurityContextRunnable(() -> proceed(joinPoint), s2STokenService.getSecurityContext()).run();
    }

    @Pointcut("@annotation(InnerSystemUserSecurityContext)")
    public void annotatedWithInnerSystemUserSecurityContext() {
    }

}
