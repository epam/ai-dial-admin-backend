package com.epam.aidial.cfg.dao.jpa;

import com.epam.aidial.cfg.dao.model.ApplicationTypeSchemaEntity;
import org.springframework.data.repository.CrudRepository;

import java.util.Collection;
import java.util.List;

public interface ApplicationTypeSchemaJpaRepository extends CrudRepository<ApplicationTypeSchemaEntity, String> {

    List<ApplicationTypeSchemaEntity> findByIdNotIn(Collection<String> ids);
}
