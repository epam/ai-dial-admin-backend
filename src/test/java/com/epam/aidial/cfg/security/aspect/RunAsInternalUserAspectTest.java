package com.epam.aidial.cfg.security.aspect;

import com.epam.aidial.cfg.security.InternalSecurityAuthenticationToken;
import com.epam.aidial.cfg.web.security.UserRole;
import com.epam.aidial.cfg.web.security.UserSecurityDetails;
import org.aspectj.lang.ProceedingJoinPoint;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class RunAsInternalUserAspectTest {

    private final RunAsInternalUserAspect aspect = new RunAsInternalUserAspect();

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void throwsWhenPrincipalNotReserved() throws Throwable {
        // given
        ProceedingJoinPoint joinPoint = mock(ProceedingJoinPoint.class);
        RunAsInternalUser annotation = mock(RunAsInternalUser.class);
        when(annotation.principal()).thenReturn("attacker");

        // when & then
        assertThatThrownBy(() -> aspect.evaluate(joinPoint, annotation))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Principal 'attacker' is not a reserved internal principal");

        verify(joinPoint, never()).proceed();
    }

    @Test
    void restoresPreviousSecurityContextAfterProceed() throws Throwable {
        // given
        Authentication previousAuth = new UsernamePasswordAuthenticationToken("user", "pwd", List.of());

        SecurityContext previous = SecurityContextHolder.getContext();
        previous.setAuthentication(previousAuth);

        ProceedingJoinPoint joinPoint = mock(ProceedingJoinPoint.class);
        when(joinPoint.proceed()).thenReturn("result");

        RunAsInternalUser annotation = mock(RunAsInternalUser.class);
        when(annotation.principal()).thenReturn(InternalSecurityAuthenticationToken.SYSTEM_PRINCIPAL);
        when(annotation.email()).thenReturn(InternalSecurityAuthenticationToken.SYSTEM_PRINCIPAL_EMAIL);
        when(annotation.roles()).thenReturn(new UserRole[]{UserRole.FULL_ADMIN});

        // when
        Object result = aspect.evaluate(joinPoint, annotation);

        // then
        assertThat(result).isEqualTo("result");
        assertThat(SecurityContextHolder.getContext()).isSameAs(previous);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isSameAs(previousAuth);
    }

    @ParameterizedTest
    @CsvSource({"system", "System", "SYSTEM"})
    void duringProceedAuthenticationIsInternalTokenWithExpectedPrincipalAndEmailAndAuthorities(String principal) throws Throwable {
        // given
        ProceedingJoinPoint joinPoint = mock(ProceedingJoinPoint.class);
        doAnswer(invocation -> {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            assertThat(auth).isInstanceOf(InternalSecurityAuthenticationToken.class);
            assertThat(auth.getPrincipal()).isEqualTo(InternalSecurityAuthenticationToken.SYSTEM_PRINCIPAL);
            assertThat(auth.getDetails()).isInstanceOfSatisfying(
                    UserSecurityDetails.class,
                    userSecurityDetails -> assertThat(userSecurityDetails.email()).isEqualTo(InternalSecurityAuthenticationToken.SYSTEM_PRINCIPAL_EMAIL));
            assertThat(auth.getAuthorities())
                    .extracting(GrantedAuthority::getAuthority)
                    .containsExactly(UserRole.FULL_ADMIN.name());
            return "result";
        }).when(joinPoint).proceed();

        RunAsInternalUser annotation = mock(RunAsInternalUser.class);
        when(annotation.principal()).thenReturn(principal);
        when(annotation.email()).thenReturn(InternalSecurityAuthenticationToken.SYSTEM_PRINCIPAL_EMAIL);
        when(annotation.roles()).thenReturn(new UserRole[]{UserRole.FULL_ADMIN});

        // when
        Object result = aspect.evaluate(joinPoint, annotation);

        // then
        assertThat(result).isEqualTo("result");
    }

    @Test
    void restoresPreviousContextWhenProceedThrows() throws Throwable {
        // given
        Authentication previousAuth = new UsernamePasswordAuthenticationToken("user", "pwd", List.of());

        SecurityContext previous = SecurityContextHolder.getContext();
        previous.setAuthentication(previousAuth);

        ProceedingJoinPoint joinPoint = mock(ProceedingJoinPoint.class);
        doThrow(new IllegalStateException("boom")).when(joinPoint).proceed();

        RunAsInternalUser annotation = mock(RunAsInternalUser.class);
        when(annotation.principal()).thenReturn(InternalSecurityAuthenticationToken.SYSTEM_PRINCIPAL);
        when(annotation.email()).thenReturn(InternalSecurityAuthenticationToken.SYSTEM_PRINCIPAL_EMAIL);
        when(annotation.roles()).thenReturn(new UserRole[]{UserRole.FULL_ADMIN});

        // when & then
        assertThatThrownBy(() -> aspect.evaluate(joinPoint, annotation))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("boom");

        assertThat(SecurityContextHolder.getContext()).isSameAs(previous);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isSameAs(previousAuth);
    }
}
