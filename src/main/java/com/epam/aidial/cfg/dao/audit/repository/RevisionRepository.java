package com.epam.aidial.cfg.dao.audit.repository;

import org.hibernate.envers.AuditReader;

import java.util.List;

public class RevisionRepository {

    @SuppressWarnings("unchecked")
    public <T> List<T> getEntitiesAtRevision(Number revision, AuditReader auditReader, Class<T> modelEntityClass) {
        return auditReader.createQuery()
                .forEntitiesAtRevision(modelEntityClass, revision)
                .getResultList();
    }
}
