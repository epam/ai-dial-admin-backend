package com.epam.aidial.cfg.model;

import com.epam.aidial.cfg.service.config.export.ConflictResolutionPolicy;

public record ConfigImportOptions(ConflictResolutionPolicy conflictResolutionPolicy,
                                  boolean createRoleIfAbsent,
                                  boolean createAdapterIfAbsent) {
    public ConfigImportOptions(ConflictResolutionPolicy conflictResolutionPolicy) {
        this(conflictResolutionPolicy, true, true);
    }
}
