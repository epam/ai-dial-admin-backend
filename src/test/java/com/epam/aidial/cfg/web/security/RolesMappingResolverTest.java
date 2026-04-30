package com.epam.aidial.cfg.web.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class RolesMappingResolverTest {

    private RolesMappingResolver resolver;

    @BeforeEach
    void setUp() {
        resolver = new RolesMappingResolver();
    }

    @Test
    void shouldReturnCombinedRolesMappingWhenDefaultAndProviderMapping() {
        // given
        Map<String, Set<UserRole>> defaultMapping = Map.of(
                "ROLE_A", Set.of(UserRole.FULL_ADMIN)
        );

        Map<String, Set<UserRole>> providerMapping = Map.of(
                "ROLE_B", Set.of(UserRole.READ_ONLY_ADMIN)
        );

        Map<String, Set<UserRole>> expected = Map.of(
                "ROLE_A", Set.of(UserRole.FULL_ADMIN),
                "ROLE_B", Set.of(UserRole.READ_ONLY_ADMIN)
        );

        // when
        Map<String, Set<UserRole>> result = resolver.resolve(
                defaultMapping,
                providerMapping
        );

        // then
        assertThat(result).containsExactlyInAnyOrderEntriesOf(expected);
    }

    @Test
    void shouldOverrideDefaultMappingWithProviderMapping() {
        // given
        Map<String, Set<UserRole>> defaultMapping = Map.of(
                "ROLE_A", Set.of(UserRole.READ_ONLY_ADMIN)
        );

        Map<String, Set<UserRole>> providerMapping = Map.of(
                "ROLE_A", Set.of(UserRole.FULL_ADMIN)
        );

        Map<String, Set<UserRole>> expected = Map.of(
                "ROLE_A", Set.of(UserRole.FULL_ADMIN)
        );

        // when
        Map<String, Set<UserRole>> result = resolver.resolve(
                defaultMapping,
                providerMapping
        );

        // then
        assertThat(result).containsExactlyInAnyOrderEntriesOf(expected);
    }

    @Test
    void shouldReturnDefaultRolesMappingWhenNoProviderMapping() {
        // given
        Map<String, Set<UserRole>> defaultMapping = Map.of(
                "ROLE_A", Set.of(UserRole.FULL_ADMIN),
                "ROLE_B", Set.of(UserRole.READ_ONLY_ADMIN)
        );

        // when
        Map<String, Set<UserRole>> result = resolver.resolve(
                defaultMapping,
                null
        );

        // then
        assertThat(result).isEqualTo(defaultMapping);
    }

    @Test
    void shouldReturnEmptyMapWhenAllInputsAreNullOrEmpty() {
        // when
        Map<String, Set<UserRole>> result = resolver.resolve(
                null,
                null
        );

        // then
        assertThat(result).isEmpty();
    }
}