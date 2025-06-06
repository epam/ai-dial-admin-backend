package com.epam.aidial.cfg.dao.audit.jpa;

import com.epam.aidial.cfg.dao.audit.model.AuditActivityEntity;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.UUID;

public interface AuditActivityJpaRepository extends CrudRepository<AuditActivityEntity, UUID>,
        PagingAndSortingRepository<AuditActivityEntity, UUID>,
        JpaSpecificationExecutor<AuditActivityEntity> {
}
