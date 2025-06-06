package com.epam.aidial.cfg.dao.jpa;

import com.epam.aidial.cfg.dao.model.ApplicationTypeSchemaEntity;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ApplicationTypeSchemaJpaRepository extends CrudRepository<ApplicationTypeSchemaEntity, String> {

    @Query("DELETE FROM ApplicationTypeSchemaEntity a WHERE a.schemaId NOT IN :ids")
    @Modifying
    void deleteAllExcept(@Param("ids") List<String> ids);
}
