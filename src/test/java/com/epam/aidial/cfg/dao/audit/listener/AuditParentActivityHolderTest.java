package com.epam.aidial.cfg.dao.audit.listener;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class AuditParentActivityHolderTest {

    private AuditParentActivityHolder holder;

    @BeforeEach
    void setUp() {
        holder = new AuditParentActivityHolder();
    }

    @Test
    void getParentActivityId_returnsEmptyWhenNoScopeOpen() {
        assertThat(holder.getParentActivityId()).isEmpty();
    }

    @Test
    void openScope_setsParentId() {
        UUID id = UUID.randomUUID();
        holder.openScope(id);
        assertThat(holder.getParentActivityId()).contains(id);
    }

    @Test
    void closeScope_restoresToEmpty() {
        UUID id = UUID.randomUUID();
        var scope = holder.openScope(id);
        scope.close();
        assertThat(holder.getParentActivityId()).isEmpty();
    }

    @Test
    void nestedScopes_restorePreviousValue() {
        UUID outerUuid = UUID.randomUUID();
        UUID innerUuid = UUID.randomUUID();

        var outerScope = holder.openScope(outerUuid);
        assertThat(holder.getParentActivityId()).contains(outerUuid);

        var innerScope = holder.openScope(innerUuid);
        assertThat(holder.getParentActivityId()).contains(innerUuid);

        innerScope.close();
        assertThat(holder.getParentActivityId()).contains(outerUuid);

        outerScope.close();
        assertThat(holder.getParentActivityId()).isEmpty();
    }

    @Test
    void openScopeWithNull_doesNotOverrideCurrent() {
        UUID id = UUID.randomUUID();
        var outerScope = holder.openScope(id);

        var nullScope = holder.openScope(null);
        assertThat(holder.getParentActivityId()).contains(id);

        nullScope.close();
        assertThat(holder.getParentActivityId()).contains(id);

        outerScope.close();
        assertThat(holder.getParentActivityId()).isEmpty();
    }

    @Test
    void tryWithResources_autoCloses() {
        UUID id = UUID.randomUUID();
        try (var scope = holder.openScope(id)) {
            assertThat(holder.getParentActivityId()).contains(id);
        }
        assertThat(holder.getParentActivityId()).isEmpty();
    }
}
