package com.epam.aidial.cfg.dao.jpa;

import com.epam.aidial.cfg.dao.model.CatalogSchemaEntity;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.Collection;
import java.util.List;
import java.util.Set;

public interface CatalogSchemaJpaRepository extends CrudRepository<CatalogSchemaEntity, String> {

    List<CatalogSchemaEntity> findByIdNotIn(Collection<String> ids);

    @Query("SELECT c.schemaId FROM CatalogSchemaEntity c")
    Set<String> findAllIds();
}
