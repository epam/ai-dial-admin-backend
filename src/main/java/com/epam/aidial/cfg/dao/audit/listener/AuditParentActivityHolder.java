package com.epam.aidial.cfg.dao.audit.listener;

import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
public class AuditParentActivityHolder {
    private final ThreadLocal<UUID> parentActivityId = new ThreadLocal<>();

    public Scope openScope(UUID parentId) {
        UUID previous = parentActivityId.get();

        if (parentId != null) {
            parentActivityId.set(parentId);
        }

        return () -> {
            if (previous == null) {
                parentActivityId.remove();
            } else {
                parentActivityId.set(previous);
            }
        };
    }

    public Optional<UUID> getParentActivityId() {
        return Optional.ofNullable(parentActivityId.get());
    }

    public interface Scope extends AutoCloseable {
        @Override
        void close();
    }
}