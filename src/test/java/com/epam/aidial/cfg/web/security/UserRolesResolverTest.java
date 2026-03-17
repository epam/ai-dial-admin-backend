package com.epam.aidial.cfg.web.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class UserRolesResolverTest {

    private UserRolesResolver resolver;

    @BeforeEach
    void setUp() {
        Map<String, Set<UserRole>> rolesMapping = Map.of(
                "ROLE_ADMIN", Set.of(UserRole.FULL_ADMIN),
                "ROLE_USER", Set.of(UserRole.READ_ONLY_ADMIN),
                "ROLE_VIEWER", Set.of(UserRole.READ_ONLY_ADMIN)
        );

        resolver = new UserRolesResolver(rolesMapping);
    }

    @Test
    void shouldResolveSingleRole() {
        // given
        Collection<? extends GrantedAuthority> authorities =
                List.of(new SimpleGrantedAuthority("ROLE_ADMIN"));

        // when
        Collection<? extends GrantedAuthority> result = resolver.resolve(authorities);

        // then
        assertThat(result)
                .extracting(GrantedAuthority::getAuthority)
                .containsExactlyInAnyOrder(UserRole.FULL_ADMIN.name());
    }

    @Test
    void shouldResolveMultipleRolesFromMultipleAuthorities() {
        // given
        Collection<? extends GrantedAuthority> authorities = List.of(
                new SimpleGrantedAuthority("ROLE_USER"),
                new SimpleGrantedAuthority("ROLE_ADMIN")
        );

        // when
        Collection<? extends GrantedAuthority> result = resolver.resolve(authorities);

        // then
        assertThat(result)
                .extracting(GrantedAuthority::getAuthority)
                .containsExactlyInAnyOrder(UserRole.FULL_ADMIN.name(), UserRole.READ_ONLY_ADMIN.name());
    }

    @Test
    void shouldRemoveDuplicatedUserRoles() {
        // given
        Collection<? extends GrantedAuthority> authorities = List.of(
                new SimpleGrantedAuthority("ROLE_USER"),
                new SimpleGrantedAuthority("ROLE_VIEWER")
        );

        // when
        Collection<? extends GrantedAuthority> result = resolver.resolve(authorities);

        // then
        assertThat(result)
                .extracting(GrantedAuthority::getAuthority)
                .containsExactlyInAnyOrder(UserRole.READ_ONLY_ADMIN.name());
    }

    @Test
    void shouldIgnoreUnknownAuthorities() {
        // given
        Collection<? extends GrantedAuthority> authorities =
                List.of(new SimpleGrantedAuthority("ROLE_UNKNOWN"));

        // when
        Collection<? extends GrantedAuthority> result = resolver.resolve(authorities);

        // then
        assertThat(result).isEmpty();
    }

    @Test
    void shouldReturnEmptyWhenGrantedAuthoritiesIsEmpty() {
        // given
        Collection<? extends GrantedAuthority> authorities = List.of();

        // when
        Collection<? extends GrantedAuthority> result = resolver.resolve(authorities);

        // then
        assertThat(result).isEmpty();
    }

    @Test
    void shouldHandleMixedKnownAndUnknownAuthorities() {
        // given
        Collection<? extends GrantedAuthority> authorities = List.of(
                new SimpleGrantedAuthority("ROLE_ADMIN"),
                new SimpleGrantedAuthority("ROLE_UNKNOWN")
        );

        // when
        Collection<? extends GrantedAuthority> result = resolver.resolve(authorities);

        // then
        assertThat(result)
                .extracting(GrantedAuthority::getAuthority)
                .containsExactly(UserRole.FULL_ADMIN.name());
    }

}