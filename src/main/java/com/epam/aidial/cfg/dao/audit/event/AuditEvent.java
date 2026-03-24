package com.epam.aidial.cfg.dao.audit.event;

import com.epam.aidial.cfg.domain.model.activity.ActivityResourceType;
import com.epam.aidial.cfg.domain.model.activity.ActivityType;
import com.epam.aidial.cfg.model.ExportRequest;
import com.epam.aidial.cfg.model.ImportResources;
import com.epam.aidial.cfg.model.ImportResourcesFileResult;
import org.springframework.lang.Nullable;

/**
 * Sealed event hierarchy for audit operations on external resources.
 * Published by services, handled by {@link AuditEventListener}.
 */
public sealed interface AuditEvent permits
        AuditEvent.AssetChanged,
        AuditEvent.FileUploaded,
        AuditEvent.ConfigExported,
        AuditEvent.RawConfigExported,
        AuditEvent.PublicationChanged {

    /** Simple CRUD on external resources (files, prompts, application resources, toolsets). */
    record AssetChanged(
            ActivityType action,
            ActivityResourceType resourceType,
            String resourceId
    ) implements AuditEvent {}

    /** File or ZIP upload operation with result metadata. */
    record FileUploaded(
            ActivityType action,
            ImportResources importResources,
            @Nullable String zipArchiveName,
            @Nullable String fileNames,
            @Nullable ImportResourcesFileResult result
    ) implements AuditEvent {}

    /** Config export (CORE or ADMIN format). */
    record ConfigExported(
            ExportRequest request
    ) implements AuditEvent {}

    /** Raw config export (core config + secrets as ZIP). */
    record RawConfigExported(
            boolean addSecrets
    ) implements AuditEvent {}

    /** Publication action: approve, reject, create, delete, or update (child). */
    record PublicationChanged(
            String publicationPath,
            ActivityType action,
            @Nullable String comment
    ) implements AuditEvent {}
}
