package com.epam.aidial.cfg.dao.jpa;

import com.epam.aidial.cfg.dao.model.ApplicationTypeSchemaEntity;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.Collection;
import java.util.List;
import java.util.Set;

public interface ApplicationTypeSchemaJpaRepository extends CrudRepository<ApplicationTypeSchemaEntity, String> {

    List<ApplicationTypeSchemaEntity> findByIdNotIn(Collection<String> ids);

    @Query("SELECT a.schemaId FROM ApplicationTypeSchemaEntity a")
    Set<String> findAllIds();
}
