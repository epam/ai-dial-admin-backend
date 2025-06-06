package com.epam.aidial.cfg.model;

import com.epam.aidial.cfg.service.export.ConflictResolutionPolicy;
import lombok.Getter;

@Getter
public class ConfigImportOptions {

    private final ConflictResolutionPolicy conflictResolutionPolicy;
    private final boolean createRoleIfAbsent;

    public ConfigImportOptions(ConflictResolutionPolicy conflictResolutionPolicy,
                               boolean createRoleIfAbsent) {
        this.conflictResolutionPolicy = conflictResolutionPolicy;
        this.createRoleIfAbsent = createRoleIfAbsent;
    }
}
